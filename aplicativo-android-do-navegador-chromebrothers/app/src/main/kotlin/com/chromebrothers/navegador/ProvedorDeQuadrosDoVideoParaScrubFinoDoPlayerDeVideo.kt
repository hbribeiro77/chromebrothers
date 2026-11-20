package com.chromebrothers.navegador

import android.app.Activity
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.util.ArrayDeque
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * Miniaturas da filmstrip só via MediaMetadataRetriever.
 * Não seeka o player — seek+TextureView devolvia tela preta ao arrastar.
 * Pedidos avulsos entram na frente da fila para acompanhar o dedo.
 */
class ProvedorDeQuadrosDoVideoParaScrubFinoDoPlayerDeVideo(
    private val activity: Activity,
    private val obterEndereco: () -> String,
    private val obterReferer: () -> String = { "" },
    private val obterAgente: () -> String = { "" },
) {
    private val executor = Executors.newSingleThreadExecutor()
    private val trava = Any()
    private val fila = ArrayDeque<Trabalho>()
    private var retriever: MediaMetadataRetriever? = null
    private var enderecoAberto = ""
    private var geracao = 0
    private var processando = false

    private sealed class Trabalho {
        abstract val pedido: Int

        class Lote(
            override val pedido: Int,
            val itens: List<Pair<Int, Long>>,
            val aindaAtivo: () -> Boolean,
            val aoQuadro: (indice: Int, bitmap: Bitmap?) -> Unit,
            val aoFim: () -> Unit,
        ) : Trabalho()

        class Avulso(
            override val pedido: Int,
            val tempoMs: Long,
            val aoPronto: (Bitmap?) -> Unit,
        ) : Trabalho()
    }

    fun cancelar() {
        synchronized(trava) {
            geracao++
            while (fila.isNotEmpty()) {
                when (val trabalho = fila.removeFirst()) {
                    is Trabalho.Lote -> runCatching { trabalho.aoFim() }
                    is Trabalho.Avulso -> runCatching { trabalho.aoPronto(null) }
                }
            }
        }
    }

    fun abrir() {
        executor.execute {
            sincronizarRetriever(obterEndereco().trim())
        }
    }

    fun fechar() {
        cancelar()
        executor.execute {
            runCatching { retriever?.release() }
            retriever = null
            enderecoAberto = ""
        }
    }

    fun preencher(
        tempos: List<Long>,
        aindaAtivo: () -> Boolean,
        aoQuadro: (indice: Int, bitmap: Bitmap?) -> Unit,
        aoFim: () -> Unit,
        indiceCentral: Int = 0,
    ) {
        val pedido: Int
        synchronized(trava) {
            geracao++
            pedido = geracao
            while (fila.isNotEmpty()) {
                when (val trabalho = fila.removeFirst()) {
                    is Trabalho.Lote -> runCatching { trabalho.aoFim() }
                    is Trabalho.Avulso -> runCatching { trabalho.aoPronto(null) }
                }
            }
            val itens = tempos.indices
                .sortedBy { abs(it - indiceCentral.coerceIn(0, (tempos.size - 1).coerceAtLeast(0))) }
                .map { indice -> indice to tempos[indice] }
            fila.addLast(
                Trabalho.Lote(
                    pedido = pedido,
                    itens = itens,
                    aindaAtivo = aindaAtivo,
                    aoQuadro = aoQuadro,
                    aoFim = aoFim,
                ),
            )
        }
        disparar()
    }

    fun capturarEm(tempoMs: Long, aoPronto: (Bitmap?) -> Unit) {
        val pedido: Int
        synchronized(trava) {
            pedido = geracao
            fila.addFirst(
                Trabalho.Avulso(
                    pedido = pedido,
                    tempoMs = tempoMs,
                    aoPronto = aoPronto,
                ),
            )
        }
        disparar()
    }

    private fun disparar() {
        synchronized(trava) {
            if (processando) return
            processando = true
        }
        executor.execute { drenarFila() }
    }

    private fun drenarFila() {
        while (true) {
            val trabalho = synchronized(trava) {
                if (fila.isEmpty()) {
                    processando = false
                    return@synchronized null
                }
                fila.removeFirst()
            } ?: return
            when (trabalho) {
                is Trabalho.Lote -> executarLote(trabalho)
                is Trabalho.Avulso -> executarAvulso(trabalho)
            }
        }
    }

    private fun executarLote(trabalho: Trabalho.Lote) {
        val endereco = obterEndereco().trim()
        if (endereco.isBlank() || !enderecoUtilizavel(endereco)) {
            trabalho.aoFim()
            return
        }
        sincronizarRetriever(endereco)
        for ((indice, tempoMs) in trabalho.itens) {
            if (trabalho.pedido != geracao || !trabalho.aindaAtivo()) {
                trabalho.aoFim()
                return
            }
            // Se entrou pedido avulso urgente, devolve o restante do lote pro fim da fila.
            val temAvulsoNaFrente = synchronized(trava) {
                fila.firstOrNull() is Trabalho.Avulso
            }
            if (temAvulsoNaFrente) {
                val posAtual = trabalho.itens.indexOfFirst { it.first == indice }
                val aPartirDaqui = if (posAtual >= 0) trabalho.itens.drop(posAtual) else emptyList()
                synchronized(trava) {
                    if (trabalho.pedido == geracao && aPartirDaqui.isNotEmpty()) {
                        fila.addLast(
                            Trabalho.Lote(
                                pedido = trabalho.pedido,
                                itens = aPartirDaqui,
                                aindaAtivo = trabalho.aindaAtivo,
                                aoQuadro = trabalho.aoQuadro,
                                aoFim = trabalho.aoFim,
                            ),
                        )
                    } else {
                        trabalho.aoFim()
                    }
                }
                return
            }
            trabalho.aoQuadro(indice, quadroUtil(tempoMs))
        }
        trabalho.aoFim()
    }

    private fun executarAvulso(trabalho: Trabalho.Avulso) {
        if (trabalho.pedido != geracao) {
            trabalho.aoPronto(null)
            return
        }
        val endereco = obterEndereco().trim()
        if (endereco.isBlank() || !enderecoUtilizavel(endereco)) {
            trabalho.aoPronto(null)
            return
        }
        sincronizarRetriever(endereco)
        trabalho.aoPronto(quadroUtil(trabalho.tempoMs))
    }

    private fun quadroUtil(tempoMs: Long): Bitmap? {
        val bitmap = quadroEm(tempoMs) ?: return null
        if (quadroQuasePreto(bitmap)) {
            if (!bitmap.isRecycled) bitmap.recycle()
            return null
        }
        return bitmap
    }

    private fun quadroEm(tempoMs: Long): Bitmap? {
        return try {
            val micros = tempoMs.coerceAtLeast(0L) * 1000L
            val bruto = retriever?.getFrameAtTime(
                micros,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
            ) ?: retriever?.getFrameAtTime(
                micros,
                MediaMetadataRetriever.OPTION_CLOSEST,
            ) ?: retriever?.getScaledFrameAtTime(
                micros,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                160,
                90,
            )
            bruto?.copy(Bitmap.Config.ARGB_8888, false)?.also {
                if (it !== bruto && !bruto.isRecycled) bruto.recycle()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun quadroQuasePreto(bitmap: Bitmap): Boolean {
        val largura = bitmap.width.coerceAtLeast(1)
        val altura = bitmap.height.coerceAtLeast(1)
        val passoX = (largura / 8).coerceAtLeast(1)
        val passoY = (altura / 8).coerceAtLeast(1)
        var amostras = 0
        var soma = 0L
        var y = 0
        while (y < altura) {
            var x = 0
            while (x < largura) {
                val cor = bitmap.getPixel(x, y)
                val r = (cor shr 16) and 0xFF
                val g = (cor shr 8) and 0xFF
                val b = cor and 0xFF
                soma += r + g + b
                amostras++
                x += passoX
            }
            y += passoY
        }
        if (amostras == 0) return true
        return (soma / amostras) < 18
    }

    private fun sincronizarRetriever(endereco: String) {
        if (endereco == enderecoAberto && retriever != null) return
        runCatching { retriever?.release() }
        retriever = null
        enderecoAberto = ""
        if (endereco.isBlank() || !enderecoUtilizavel(endereco)) return
        val novo = MediaMetadataRetriever()
        try {
            abrirFonte(novo, endereco)
            retriever = novo
            enderecoAberto = endereco
        } catch (_: Exception) {
            runCatching { novo.release() }
        }
    }

    private fun enderecoUtilizavel(endereco: String): Boolean {
        val esquema = Uri.parse(endereco).scheme?.lowercase().orEmpty()
        return when {
            esquema == "http" || esquema == "https" || esquema == "file" || esquema == "content" -> true
            endereco.startsWith("/") -> true
            else -> false
        }
    }

    private fun abrirFonte(retriever: MediaMetadataRetriever, endereco: String) {
        val uri = Uri.parse(endereco)
        when (uri.scheme?.lowercase()) {
            "content", "file" -> retriever.setDataSource(activity, uri)
            "http", "https" -> {
                val cabecalhos = mutableMapOf<String, String>()
                val agente = obterAgente().trim()
                val referer = obterReferer().trim()
                cabecalhos["User-Agent"] = agente.ifBlank { "Mozilla/5.0" }
                if (referer.isNotBlank()) cabecalhos["Referer"] = referer
                retriever.setDataSource(endereco, cabecalhos)
            }
            else -> {
                if (endereco.startsWith("/")) retriever.setDataSource(endereco)
                else error("esquema nao suportado")
            }
        }
    }
}

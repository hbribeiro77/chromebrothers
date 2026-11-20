package com.chromebrothers.navegador

import android.graphics.Bitmap
import android.view.View
import com.chromebrothers.navegador.regras.MensagemDeVideoTocandoNaPagina
import com.chromebrothers.navegador.regras.ModoDeApresentacaoDoSite

class PaginaDeUmaAbaDoNavegadorChromebrothers(
    val id: Long,
    val webView: SuperficieDaPaginaNoMotorDoFirefox,
) {
    var agenteOriginal: String = ""
    var modoForcado: ModoDeApresentacaoDoSite? = null
    /** Reabrir o IME ao voltar a esta aba (havia input web focado). */
    var deveRestaurarTecladoAoFocar: Boolean = false
    var miniaturaDaPagina: Bitmap? = null
    var videoAtual: MensagemDeVideoTocandoNaPagina? = null
    var temLimitesDeVideo: Boolean = false
    var esquerdaDoVideo: Float = 0f
    var topoDoVideo: Float = 0f
    var larguraDoVideo: Float = 0f
    var alturaDoVideo: Float = 0f
    var larguraDoViewport: Float = 0f
    var alturaDoViewport: Float = 0f
    private val pedidosDeMidia = mutableListOf<com.chromebrothers.navegador.regras.PedidoDeMidiaObservado>()
    private val referersDosPedidos = mutableMapOf<String, String>()
    private val agentesDosPedidos = mutableMapOf<String, String>()
    private val travaDosPedidos = Any()
    private val geracaoDosPedidos = java.util.concurrent.atomic.AtomicInteger(0)

    fun geracaoAtualDosPedidosDeMidia(): Int = geracaoDosPedidos.get()

    fun limparPedidosDeMidia() {
        geracaoDosPedidos.incrementAndGet()
        synchronized(travaDosPedidos) {
            pedidosDeMidia.clear()
            referersDosPedidos.clear()
            agentesDosPedidos.clear()
        }
    }

    fun registrarPedidoDeMidia(
        endereco: String,
        tipo: com.chromebrothers.navegador.regras.TipoDePedidoDeMidiaParaDownload,
        referer: String,
        agente: String,
        geracao: Int,
        tamanho: Long = -1,
        mime: String = "",
    ): Boolean {
        if (geracao != geracaoDosPedidos.get()) return false
        synchronized(travaDosPedidos) {
            if (geracao != geracaoDosPedidos.get()) return false
            val anterior = pedidosDeMidia.find { it.endereco == endereco }
            val tamanhoFinal = when {
                tamanho < 0 -> anterior?.tamanho ?: -1L
                anterior == null || anterior.tamanho < 0 -> tamanho
                else -> maxOf(anterior.tamanho, tamanho)
            }
            val novo = anterior == null
            pedidosDeMidia.removeAll { it.endereco == endereco }
            val ordem = (pedidosDeMidia.maxOfOrNull { it.ordem } ?: 0L) + 1L
            pedidosDeMidia.add(
                com.chromebrothers.navegador.regras.PedidoDeMidiaObservado(
                    endereco,
                    tipo,
                    ordem,
                    tamanhoFinal,
                    mime.substringBefore(';').trim().ifBlank { anterior?.mime.orEmpty() },
                ),
            )
            while (pedidosDeMidia.size > 40) {
                val indiceDeArquivo = pedidosDeMidia.indexOfFirst {
                    it.tipo == com.chromebrothers.navegador.regras.TipoDePedidoDeMidiaParaDownload.ARQUIVO
                }
                val removido = pedidosDeMidia.removeAt(if (indiceDeArquivo >= 0) indiceDeArquivo else 0)
                referersDosPedidos.remove(removido.endereco)
                agentesDosPedidos.remove(removido.endereco)
            }
            if (referer.isNotBlank()) referersDosPedidos[endereco] = referer
            if (agente.isNotBlank()) agentesDosPedidos[endereco] = agente
            return novo
        }
    }

    fun atualizarTamanhoDoPedidoDeMidia(endereco: String, tamanho: Long) {
        if (tamanho < 0) return
        synchronized(travaDosPedidos) {
            val indice = pedidosDeMidia.indexOfFirst { it.endereco == endereco }
            if (indice < 0) return
            val atual = pedidosDeMidia[indice]
            val melhor = if (atual.tamanho < 0) tamanho else maxOf(atual.tamanho, tamanho)
            if (melhor != atual.tamanho) {
                pedidosDeMidia[indice] = atual.copy(tamanho = melhor)
            }
        }
    }

    fun pedidosDeMidiaObservados(): List<com.chromebrothers.navegador.regras.PedidoDeMidiaObservado> {
        return synchronized(travaDosPedidos) { pedidosDeMidia.toList() }
    }

    fun refererDoPedidoDeMidia(endereco: String): String {
        return synchronized(travaDosPedidos) { referersDosPedidos[endereco].orEmpty() }
    }

    fun agenteDoPedidoDeMidia(endereco: String): String {
        return synchronized(travaDosPedidos) { agentesDosPedidos[endereco].orEmpty() }
    }

    fun substituirMiniatura(nova: Bitmap) {
        val antiga = miniaturaDaPagina
        miniaturaDaPagina = nova
        if (antiga != null && antiga !== nova && !antiga.isRecycled) {
            webView.post {
                if (!antiga.isRecycled) antiga.recycle()
            }
        }
    }

    fun liberarMiniatura() {
        val antiga = miniaturaDaPagina
        miniaturaDaPagina = null
        if (antiga != null && !antiga.isRecycled) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                if (!antiga.isRecycled) antiga.recycle()
            }
        }
    }
}

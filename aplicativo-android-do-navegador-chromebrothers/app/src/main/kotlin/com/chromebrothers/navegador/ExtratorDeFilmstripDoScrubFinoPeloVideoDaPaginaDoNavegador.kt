package com.chromebrothers.navegador

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import kotlin.math.abs

/**
 * Miniaturas do player da página via seek + captura do canvas do &lt;video&gt;.
 * Pedidos avulsos entram na fila; captura "agora" não seeka (serve no arraste).
 */
class ExtratorDeFilmstripDoScrubFinoPeloVideoDaPaginaDoNavegador(
    private val seekParaSegundos: (Double) -> Unit,
    private val lerPosicaoSegundos: (aoReceber: (Double?) -> Unit) -> Unit,
    private val capturarBitmap: (aoReceber: (Bitmap?) -> Unit) -> Unit,
    private val principal: Handler = Handler(Looper.getMainLooper()),
) {
    private var geracao = 0
    private var tarefa: Runnable? = null
    private var ocupado = false
    private val fila = ArrayDeque<Pedido>()

    private sealed class Pedido {
        class ComSeek(
            val tempoMs: Long,
            val aoPronto: (Bitmap?) -> Unit,
        ) : Pedido()

        class Agora(
            val aoPronto: (Bitmap?) -> Unit,
        ) : Pedido()
    }

    fun cancelar() {
        geracao++
        fila.clear()
        ocupado = false
        cancelarTarefa()
    }

    fun capturarAgora(aoPronto: (Bitmap?) -> Unit) {
        fila.addFirst(Pedido.Agora(aoPronto))
        processarFila()
    }

    fun capturarEm(tempoMs: Long, aoPronto: (Bitmap?) -> Unit) {
        fila.addLast(Pedido.ComSeek(tempoMs, aoPronto))
        processarFila()
    }

    fun preencher(
        tempos: List<Long>,
        indiceCentral: Int,
        aindaAtivo: () -> Boolean,
        aoQuadro: (indice: Int, bitmap: Bitmap?) -> Unit,
        aoFim: () -> Unit,
        podeSeekar: () -> Boolean = { true },
    ) {
        cancelar()
        val pedido = geracao
        val ordem = tempos.indices.sortedBy { abs(it - indiceCentral.coerceIn(0, (tempos.size - 1).coerceAtLeast(0))) }
        var cursor = 0

        fun terminar() {
            if (pedido != geracao) return
            aoFim()
        }

        fun pedirProximo() {
            if (pedido != geracao || !aindaAtivo()) {
                terminar()
                return
            }
            if (cursor >= ordem.size) {
                terminar()
                return
            }
            if (!podeSeekar()) {
                tarefa = Runnable { pedirProximo() }
                principal.postDelayed(tarefa!!, 140L)
                return
            }
            val indice = ordem[cursor]
            val alvoMs = tempos[indice]
            capturarEm(alvoMs) { bitmap ->
                if (pedido != geracao) return@capturarEm
                aoQuadro(indice, bitmap)
                cursor++
                principal.post { pedirProximo() }
            }
        }

        pedirProximo()
    }

    private fun processarFila() {
        if (ocupado) return
        val pedido = if (fila.isEmpty()) null else fila.removeFirst()
        if (pedido == null) return
        ocupado = true
        val token = geracao
        when (pedido) {
            is Pedido.Agora -> {
                capturarBitmap { bitmap ->
                    if (token == geracao) pedido.aoPronto(bitmap) else pedido.aoPronto(null)
                    ocupado = false
                    processarFila()
                }
            }
            is Pedido.ComSeek -> {
                val alvoSegundos = pedido.tempoMs / 1000.0
                seekParaSegundos(alvoSegundos)
                var tentativas = 0
                val verificador = object : Runnable {
                    override fun run() {
                        if (token != geracao) {
                            pedido.aoPronto(null)
                            ocupado = false
                            return
                        }
                        tentativas++
                        lerPosicaoSegundos { posicao ->
                            if (token != geracao) {
                                pedido.aoPronto(null)
                                ocupado = false
                                return@lerPosicaoSegundos
                            }
                            val perto = posicao != null && abs(posicao - alvoSegundos) <= 0.55
                            if (perto || tentativas >= 14) {
                                capturarBitmap { bitmap ->
                                    if (token == geracao) pedido.aoPronto(bitmap) else pedido.aoPronto(null)
                                    ocupado = false
                                    processarFila()
                                }
                            } else {
                                tarefa = this
                                principal.postDelayed(this, 70L)
                            }
                        }
                    }
                }
                tarefa = verificador
                principal.postDelayed(verificador, 90L)
            }
        }
    }

    private fun cancelarTarefa() {
        tarefa?.let { principal.removeCallbacks(it) }
        tarefa = null
    }
}

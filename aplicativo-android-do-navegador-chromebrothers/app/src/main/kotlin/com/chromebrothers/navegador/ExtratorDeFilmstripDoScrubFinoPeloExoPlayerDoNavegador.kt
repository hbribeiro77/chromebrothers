package com.chromebrothers.navegador

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.TextureView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlin.math.abs

class ExtratorDeFilmstripDoScrubFinoPeloExoPlayerDoNavegador(
    private val obterPlayer: () -> ExoPlayer?,
    private val obterTextura: () -> TextureView?,
    private val principal: Handler = Handler(Looper.getMainLooper()),
) {
    private var geracao = 0
    private var tarefa: Runnable? = null
    private var ocupado = false
    private val fila = ArrayDeque<Pedido>()

    private data class Pedido(
        val tempoMs: Long,
        val larguraPx: Int,
        val alturaPx: Int,
        val aoPronto: (Bitmap?) -> Unit,
    )

    fun cancelar() {
        geracao++
        fila.clear()
        ocupado = false
        cancelarTarefa()
    }

    fun capturarEm(
        tempoMs: Long,
        larguraPx: Int,
        alturaPx: Int,
        aoPronto: (Bitmap?) -> Unit,
    ) {
        fila.addLast(Pedido(tempoMs, larguraPx, alturaPx, aoPronto))
        processarFila()
    }

    private fun processarFila() {
        if (ocupado) return
        val pedido = fila.removeFirstOrNull() ?: return
        ocupado = true
        val token = geracao
        val reprodutor = obterPlayer()
        if (reprodutor == null) {
            pedido.aoPronto(null)
            ocupado = false
            processarFila()
            return
        }
        reprodutor.playWhenReady = false
        reprodutor.pause()
        reprodutor.seekTo(pedido.tempoMs)
        var tentativas = 0
        val verificador = object : Runnable {
            override fun run() {
                if (token != geracao) {
                    ocupado = false
                    return
                }
                tentativas++
                val pronto = reprodutor.playbackState == Player.STATE_READY && !reprodutor.isLoading
                val perto = abs(reprodutor.currentPosition - pedido.tempoMs) <= 700L
                if ((pronto && perto) || tentativas >= 18) {
                    pedido.aoPronto(capturarCopia(pedido.larguraPx, pedido.alturaPx))
                    ocupado = false
                    processarFila()
                } else {
                    tarefa = this
                    principal.postDelayed(this, 45L)
                }
            }
        }
        tarefa = verificador
        principal.postDelayed(verificador, 70L)
    }

    private fun capturarCopia(larguraPx: Int, alturaPx: Int): Bitmap? {
        return try {
            val textura = obterTextura() ?: return null
            val bruto = when {
                textura.width <= 0 || textura.height <= 0 -> textura.bitmap
                else -> textura.getBitmap(larguraPx.coerceAtLeast(1), alturaPx.coerceAtLeast(1))
            } ?: return null
            val copia = bruto.copy(Bitmap.Config.ARGB_8888, false)
            if (copia != null && copia !== bruto && !bruto.isRecycled) bruto.recycle()
            copia ?: bruto
        } catch (_: Exception) {
            null
        }
    }

    private fun cancelarTarefa() {
        tarefa?.let { principal.removeCallbacks(it) }
        tarefa = null
    }
}

package com.chromebrothers.navegador

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
import kotlin.math.abs

/**
 * Visor em tela cheia sobre o conteúdo do componente (flutuante ou ancorado na gaveta).
 * Deslize horizontal para anterior/próxima imagem; destaque na grade via [aoMudarIndice].
 */
class VisorDePaginaDoVisualizadorDeMidiaNaColecaoDoNavegador(
    private val activity: AppCompatActivity,
    private val host: FrameLayout,
    private val leitor: LeituraDeBytesDeMidiaLocalOuRemotaParaOComponenteDaColecao,
    private val aoMudarIndice: (uriSemMiniatura: String) -> Unit,
    private val aoFechar: () -> Unit = {},
    /** Toque longo na foto (rawX/rawY): arma o fantasma; soltar no lugar abre o menu. */
    private val aoToqueLongo: (uri: String, xNaTela: Float, yNaTela: Float) -> Unit = { _, _, _ -> },
) {
    private var overlay: FrameLayout? = null
    private var foto: ImageView? = null
    private var progresso: ProgressBar? = null
    private var uris: List<String> = emptyList()
    private var indice = 0
    private var geracao = 0

    fun estaAberto(): Boolean = overlay?.parent != null

    fun uriAtual(): String? = if (estaAberto()) uris.getOrNull(indice) else null

    fun esconderDuranteArraste() {
        if (estaAberto()) overlay?.visibility = View.INVISIBLE
    }

    fun estaEscondidoPeloArraste(): Boolean = estaAberto() && overlay?.visibility == View.INVISIBLE

    fun reaparecerAposArraste() {
        if (estaAberto()) overlay?.visibility = View.VISIBLE
    }

    fun mostrar(listaDeUris: List<String>, indiceInicial: Int) {
        val limpa = listaDeUris.map { InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao.uriSemParametroDeMiniatura(it) }
            .filter { it.isNotBlank() }
        if (limpa.isEmpty()) return
        val alvo = indiceInicial.coerceIn(0, limpa.lastIndex)
        uris = limpa
        indice = alvo
        if (overlay == null) {
            montarOverlay()
        }
        if (overlay?.parent == null) {
            host.addView(
                overlay,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
        }
        host.bringChildToFront(overlay)
        overlay?.visibility = View.VISIBLE
        exibirIndiceAtual()
    }

    fun fechar() {
        val estavaAberto = estaAberto()
        geracao++
        overlay?.visibility = View.GONE
        (overlay?.parent as? ViewGroup)?.removeView(overlay)
        if (estavaAberto) aoFechar()
    }

    private fun montarOverlay() {
        val densidade = activity.resources.displayMetrics.density
        val raiz = FrameLayout(activity).apply {
            setBackgroundColor(Color.parseColor("#E60A0C10"))
            elevation = 8f * densidade
        }
        val fotoView = ImageView(activity).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
        }
        val barra = ProgressBar(activity).apply {
            isIndeterminate = true
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER,
            )
        }
        val fechar = TextView(activity).apply {
            text = activity.getString(R.string.fechar_imagem_ampliada_da_colecao)
            setTextColor(Color.parseColor("#F4F7FB"))
            textSize = 15f
            val margem = (12 * densidade).toInt()
            setPadding(margem, margem, margem, margem)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END,
            )
            setOnClickListener { this@VisorDePaginaDoVisualizadorDeMidiaNaColecaoDoNavegador.fechar() }
        }
        val detector = GestureDetectorCompat(
            activity,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true

                override fun onLongPress(e: MotionEvent) {
                    val uri = uris.getOrNull(indice) ?: return
                    fotoView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    aoToqueLongo(uri, e.rawX, e.rawY)
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float,
                ): Boolean {
                    if (e1 == null) return false
                    val dx = e2.x - e1.x
                    if (abs(dx) < 48 * densidade || abs(velocityX) < 220) return false
                    if (dx < 0) irPara(indice + 1) else irPara(indice - 1)
                    return true
                }
            },
        )
        fotoView.setOnTouchListener { _, evento -> detector.onTouchEvent(evento) }
        raiz.setOnClickListener { this@VisorDePaginaDoVisualizadorDeMidiaNaColecaoDoNavegador.fechar() }
        fotoView.setOnClickListener { /* absorve; não fecha ao tocar na foto */ }
        raiz.addView(fotoView)
        raiz.addView(barra)
        raiz.addView(fechar)
        overlay = raiz
        foto = fotoView
        progresso = barra
    }

    private fun irPara(novo: Int) {
        if (uris.isEmpty()) return
        val i = novo.coerceIn(0, uris.lastIndex)
        if (i == indice) return
        indice = i
        exibirIndiceAtual()
    }

    private fun exibirIndiceAtual() {
        val uri = uris.getOrNull(indice) ?: return
        aoMudarIndice(uri)
        val esta = ++geracao
        progresso?.visibility = View.VISIBLE
        foto?.setImageDrawable(null)
        Thread {
            val bmp = try {
                val bytes = leitor.ler(uri)
                when {
                    bytes == null || bytes.isEmpty() -> null
                    else -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            } catch (_: Exception) {
                null
            }
            activity.runOnUiThread {
                if (esta != geracao || overlay?.visibility != View.VISIBLE) {
                    bmp?.recycle()
                    return@runOnUiThread
                }
                progresso?.visibility = View.GONE
                if (bmp != null) {
                    foto?.setImageBitmap(bmp)
                } else {
                    foto?.setImageDrawable(null)
                }
            }
        }.start()
    }
}

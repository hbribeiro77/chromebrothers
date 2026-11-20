package com.chromebrothers.navegador

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.AbsSeekBar
import android.widget.FrameLayout
import com.chromebrothers.navegador.regras.GestoDeSwipeEntreAbasNoCorpoDaGavetaDoChat

/**
 * Corpo da gaveta do chat (chat, gravador e componentes ancorados): swipe horizontal troca de aba
 * e o conteúdo acompanha o dedo, como no visor de imagens. Quem decide é [GestoDeSwipeEntreAbasNoCorpoDaGavetaDoChat].
 */
class CorpoDasAbasDaGavetaDoChatComSwipeEntreAbas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    var temAbaVizinha: (paraAProxima: Boolean) -> Boolean = { false }
    var consultarSeAPaginaQuerOGesto: (xNaTela: Float, yNaTela: Float, responder: (Boolean) -> Unit) -> Unit =
        { _, _, responder -> responder(false) }
    var trocarParaAbaVizinha: (paraAProxima: Boolean) -> Unit = {}

    private val regra = GestoDeSwipeEntreAbasNoCorpoDaGavetaDoChat
    private val densidade = resources.displayMetrics.density
    private val folga = ViewConfiguration.get(context).scaledTouchSlop
    private val tempoDoToqueLongo = ViewConfiguration.getLongPressTimeout().toLong()

    private var xInicio = 0f
    private var yInicio = 0f
    private var tempoInicio = 0L
    private var comecouNaBorda = false
    private var toqueLongo = false
    private var saiuDaFolga = false
    private var paginaQuerOGesto: Boolean? = null
    private var consulta = 0
    private var desistiu = false
    private var arrastando = false
    private var baseDoArraste = 0f
    private var rastreador: VelocityTracker? = null
    private var animacao: ValueAnimator? = null

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = acompanhar(ev)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!arrastando) {
            acompanhar(ev)
            return ev.actionMasked == MotionEvent.ACTION_DOWN || arrastando
        }
        rastreador?.addMovement(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_MOVE -> deslocarFilhos(ev.x - baseDoArraste)
            MotionEvent.ACTION_UP -> {
                val vel = rastreador?.let {
                    it.computeCurrentVelocity(1000)
                    it.xVelocity
                } ?: 0f
                concluir(ev.x - baseDoArraste, vel)
            }
            MotionEvent.ACTION_CANCEL -> concluir(0f, 0f)
        }
        return true
    }

    private fun acompanhar(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (animacao?.isRunning == true) {
                    desistiu = true
                    return false
                }
                xInicio = ev.x
                yInicio = ev.y
                tempoInicio = ev.eventTime
                val borda = regra.BORDA_DP * densidade
                comecouNaBorda = ev.x < borda || ev.x > width - borda
                toqueLongo = false
                saiuDaFolga = false
                desistiu = false
                arrastando = false
                paginaQuerOGesto = null
                rastreador?.recycle()
                rastreador = VelocityTracker.obtain().also { it.addMovement(ev) }
                val estaConsulta = ++consulta
                if (comecouNaBorda) {
                    paginaQuerOGesto = true
                } else {
                    consultarSeAPaginaQuerOGesto(ev.rawX, ev.rawY) { quer ->
                        if (estaConsulta == consulta) paginaQuerOGesto = quer
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                rastreador?.addMovement(ev)
                if (desistiu || arrastando) return arrastando
                if (ev.pointerCount > 1) {
                    desistiu = true
                    return false
                }
                val dx = ev.x - xInicio
                val dy = ev.y - yInicio
                if (!saiuDaFolga && (kotlin.math.abs(dx) > folga || kotlin.math.abs(dy) > folga)) {
                    saiuDaFolga = true
                    toqueLongo = ev.eventTime - tempoInicio >= tempoDoToqueLongo
                }
                val paraAProxima = dx < 0f
                val decisao = regra.decidir(
                    dxDp = dx / densidade,
                    dyDp = dy / densidade,
                    comecouNaBorda = comecouNaBorda,
                    toqueLongo = toqueLongo,
                    paginaQuerOGesto = paginaQuerOGesto,
                    conteudoRolaNaDirecao = algumaVistaRolaSob(this, ev.rawX, ev.rawY, if (paraAProxima) 1 else -1),
                    temAbaNaDirecao = temAbaVizinha(paraAProxima),
                )
                when (decisao) {
                    GestoDeSwipeEntreAbasNoCorpoDaGavetaDoChat.Decisao.TROCAR_DE_ABA -> {
                        arrastando = true
                        baseDoArraste = ev.x - (if (paraAProxima) -folga else folga).toFloat()
                        parent?.requestDisallowInterceptTouchEvent(true)
                        return true
                    }
                    GestoDeSwipeEntreAbasNoCorpoDaGavetaDoChat.Decisao.DEIXAR_PARA_A_PAGINA -> desistiu = true
                    GestoDeSwipeEntreAbasNoCorpoDaGavetaDoChat.Decisao.AGUARDAR -> Unit
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                rastreador?.recycle()
                rastreador = null
            }
        }
        return false
    }

    private fun algumaVistaRolaSob(grupo: ViewGroup, xNaTela: Float, yNaTela: Float, direcao: Int): Boolean {
        val caixa = Rect()
        for (i in grupo.childCount - 1 downTo 0) {
            val filho = grupo.getChildAt(i) ?: continue
            if (filho.visibility != View.VISIBLE || !filho.getGlobalVisibleRect(caixa)) continue
            if (!caixa.contains(xNaTela.toInt(), yNaTela.toInt())) continue
            if (filho is AbsSeekBar) return true
            if (filho.canScrollHorizontally(direcao)) return true
            if (filho is ViewGroup && algumaVistaRolaSob(filho, xNaTela, yNaTela, direcao)) return true
        }
        return false
    }

    private fun deslocarFilhos(deslocamento: Float) {
        for (i in 0 until childCount) getChildAt(i).translationX = deslocamento
    }

    private fun concluir(deslocamento: Float, velocidade: Float) {
        arrastando = false
        rastreador?.recycle()
        rastreador = null
        val paraAProxima = deslocamento < 0f
        val confirmar = regra.deveConfirmarAoSoltar(deslocamento, width.toFloat(), velocidade, densidade)
        if (!confirmar) {
            animarFilhos(deslocamento, 0f, 180L) {}
            return
        }
        val saida = if (paraAProxima) -width.toFloat() else width.toFloat()
        animarFilhos(deslocamento, saida, 160L) {
            trocarParaAbaVizinha(paraAProxima)
            animarFilhos(-saida, 0f, 200L) {}
        }
    }

    private fun animarFilhos(de: Float, para: Float, duracao: Long, aoFim: () -> Unit) {
        animacao?.cancel()
        animacao = ValueAnimator.ofFloat(de, para).apply {
            duration = duracao
            interpolator = DecelerateInterpolator()
            addUpdateListener { deslocarFilhos(it.animatedValue as Float) }
            addListener(object : AnimatorListenerAdapter() {
                private var cancelada = false
                override fun onAnimationCancel(animation: Animator) {
                    cancelada = true
                }
                override fun onAnimationEnd(animation: Animator) {
                    if (cancelada) return
                    deslocarFilhos(para)
                    aoFim()
                }
            })
            start()
        }
    }
}

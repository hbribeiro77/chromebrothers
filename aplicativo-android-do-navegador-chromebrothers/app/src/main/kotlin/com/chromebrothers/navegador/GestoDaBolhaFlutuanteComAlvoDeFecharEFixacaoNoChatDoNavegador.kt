package com.chromebrothers.navegador

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.TextView
import com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante
import com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.FaseDoArrasteDaJanelaPelaBarra
import com.chromebrothers.navegador.regras.GestoDaBolhaDoComponenteComAlvoDeFecharEFixacaoNoChat
import com.chromebrothers.navegador.regras.GestoDaBolhaDoComponenteComAlvoDeFecharEFixacaoNoChat.AcaoAoSoltarABolha

/**
 * Toque na bolha de um componente flutuante:
 * toque curto abre; toque longo mostra o "✕" embaixo (soltar nele fecha);
 * arrastar até a faixa de abas do chat e parar fixa no chat sem precisar soltar.
 */
class GestoDaBolhaFlutuanteComAlvoDeFecharEFixacaoNoChatDoNavegador(
    private val activity: Activity,
    private val bolha: View,
    private val aoComecar: () -> Unit,
    private val aoMover: (dx: Float, dy: Float) -> Unit,
    private val aoAbrir: () -> Unit,
    private val aoFechar: () -> Unit,
    private val aoFixarNoChat: () -> Unit,
    private val consultarFaixaDoChat: (x: Float, y: Float, fase: FaseDoArrasteDaJanelaPelaBarra) -> Boolean,
) {
    private val densidade = activity.resources.displayMetrics.density
    private val folga = ViewConfiguration.get(activity).scaledTouchSlop.toFloat()
    private val tempoDoLongo = ViewConfiguration.getLongPressTimeout().toLong()
    private var origemX = 0f
    private var origemY = 0f
    private var ultimoX = 0f
    private var ultimoY = 0f
    private var moveu = false
    private var longo = false
    private var fixou = false
    private var ativo = false
    private var sobreOAlvo = false
    private var conferirFixacao: Runnable? = null
    private var alvo: TextView? = null

    private val marcarLongo = Runnable {
        if (!ativo || moveu) return@Runnable
        longo = true
        bolha.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        bolha.animate().scaleX(1.12f).scaleY(1.12f).setDuration(120L).start()
        mostrarAlvo()
    }

    fun ligar() {
        bolha.setOnTouchListener { _, evento ->
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    aoComecar()
                    origemX = evento.rawX
                    origemY = evento.rawY
                    ultimoX = origemX
                    ultimoY = origemY
                    moveu = false
                    longo = false
                    fixou = false
                    sobreOAlvo = false
                    ativo = true
                    bolha.removeCallbacks(marcarLongo)
                    bolha.postDelayed(marcarLongo, tempoDoLongo)
                }
                MotionEvent.ACTION_MOVE -> if (ativo && !fixou) mover(evento.rawX, evento.rawY)
                MotionEvent.ACTION_UP -> if (ativo) soltar(evento.rawX, evento.rawY)
                MotionEvent.ACTION_CANCEL -> if (ativo) cancelar()
            }
            true
        }
    }

    private fun mover(x: Float, y: Float) {
        ultimoX = x
        ultimoY = y
        val dx = x - origemX
        val dy = y - origemY
        if (!moveu && dx * dx + dy * dy > folga * folga) {
            moveu = true
            if (!longo) bolha.removeCallbacks(marcarLongo)
        }
        if (!moveu) return
        aoMover(dx, dy)
        if (longo) atualizarSobreOAlvo(x, y)
        val sobreAFaixa = consultarFaixaDoChat(x, y, FaseDoArrasteDaJanelaPelaBarra.MOVENDO)
        if (sobreAFaixa && conferirFixacao == null) agendarConferenciaDaFixacao()
    }

    private fun agendarConferenciaDaFixacao() {
        val tarefa = Runnable {
            conferirFixacao = null
            if (!ativo || fixou) return@Runnable
            if (consultarFaixaDoChat(ultimoX, ultimoY, FaseDoArrasteDaJanelaPelaBarra.CONSULTAR)) {
                fixou = true
                consultarFaixaDoChat(ultimoX, ultimoY, FaseDoArrasteDaJanelaPelaBarra.SOLTOU)
                bolha.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                restaurarBolha()
                esconderAlvo()
                aoFixarNoChat()
            } else if (consultarFaixaDoChat(ultimoX, ultimoY, FaseDoArrasteDaJanelaPelaBarra.MOVENDO)) {
                agendarConferenciaDaFixacao()
            }
        }
        conferirFixacao = tarefa
        bolha.postDelayed(tarefa, ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.ATRASO_MS + 40L)
    }

    private fun soltar(x: Float, y: Float) {
        val armada = !fixou && moveu &&
            consultarFaixaDoChat(x, y, FaseDoArrasteDaJanelaPelaBarra.SOLTOU)
        val acao = GestoDaBolhaDoComponenteComAlvoDeFecharEFixacaoNoChat.acaoAoSoltar(
            moveu = moveu,
            toqueLongo = longo,
            jaFixou = fixou,
            sobreOAlvoDeFechar = sobreOAlvo,
            armadaNaFaixaDoChat = armada,
        )
        encerrar()
        when (acao) {
            AcaoAoSoltarABolha.ABRIR -> aoAbrir()
            AcaoAoSoltarABolha.FECHAR -> aoFechar()
            AcaoAoSoltarABolha.FIXAR_NO_CHAT -> aoFixarNoChat()
            AcaoAoSoltarABolha.SO_MOVER, AcaoAoSoltarABolha.NADA -> Unit
        }
    }

    private fun cancelar() {
        if (moveu && !fixou) consultarFaixaDoChat(ultimoX, ultimoY, FaseDoArrasteDaJanelaPelaBarra.CANCELOU)
        encerrar()
    }

    private fun encerrar() {
        ativo = false
        bolha.removeCallbacks(marcarLongo)
        conferirFixacao?.let { bolha.removeCallbacks(it) }
        conferirFixacao = null
        restaurarBolha()
        esconderAlvo()
    }

    private fun restaurarBolha() {
        bolha.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(120L).start()
    }

    private fun atualizarSobreOAlvo(x: Float, y: Float) {
        val vista = alvo ?: return
        val local = IntArray(2)
        vista.getLocationOnScreen(local)
        val raio = vista.width / 2f
        val agora = GestoDaBolhaDoComponenteComAlvoDeFecharEFixacaoNoChat.sobreOAlvoDeFechar(
            x,
            y,
            local[0] + raio,
            local[1] + vista.height / 2f,
            raio,
        )
        if (agora == sobreOAlvo) return
        sobreOAlvo = agora
        if (agora) vista.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        pintarAlvo(vista, agora)
        vista.animate().scaleX(if (agora) 1.25f else 1f).scaleY(if (agora) 1.25f else 1f).setDuration(120L).start()
        bolha.animate().alpha(if (agora) 0.6f else 1f).setDuration(120L).start()
    }

    private fun pintarAlvo(vista: TextView, ativoNoAlvo: Boolean) {
        vista.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(if (ativoNoAlvo) "#5C2B29" else "#E61A1D24"))
            setStroke((2 * densidade).toInt(), Color.parseColor(if (ativoNoAlvo) "#F28B82" else "#4A505C"))
        }
        vista.setTextColor(Color.parseColor(if (ativoNoAlvo) "#F28B82" else "#F4F7FB"))
    }

    private fun mostrarAlvo() {
        val pai = bolha.parent as? ViewGroup ?: return
        val lado = (64 * densidade).toInt()
        val vista = alvo ?: TextView(activity).apply {
            text = "✕"
            textSize = 22f
            gravity = Gravity.CENTER
            contentDescription = activity.getString(R.string.alvo_para_fechar_a_bolha_do_componente)
        }.also { alvo = it }
        if (vista.parent != pai) {
            (vista.parent as? ViewGroup)?.removeView(vista)
            pai.addView(vista, pai.indexOfChild(bolha).coerceAtLeast(0), ViewGroup.LayoutParams(lado, lado))
        }
        vista.elevation = bolha.elevation
        pintarAlvo(vista, false)
        vista.animate().cancel()
        vista.x = (pai.width - lado) / 2f
        vista.y = pai.height - lado - 72 * densidade
        vista.scaleX = 0.6f
        vista.scaleY = 0.6f
        vista.alpha = 0f
        vista.visibility = View.VISIBLE
        vista.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(160L).start()
    }

    private fun esconderAlvo() {
        sobreOAlvo = false
        val vista = alvo ?: return
        vista.animate().cancel()
        vista.animate().alpha(0f).scaleX(0.6f).scaleY(0.6f).setDuration(140L).withEndAction {
            if (ativo && longo) return@withEndAction
            (vista.parent as? ViewGroup)?.removeView(vista)
        }.start()
    }
}

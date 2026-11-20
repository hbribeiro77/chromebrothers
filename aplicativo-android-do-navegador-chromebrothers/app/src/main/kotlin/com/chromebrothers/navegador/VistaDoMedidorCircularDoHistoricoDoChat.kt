package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.chromebrothers.navegador.regras.FaixaDoMedidorDoHistoricoDoChat

/** Anel pequeno que mostra quanto do espaço do histórico de conversas já foi usado (0 a 100). */
class VistaDoMedidorCircularDoHistoricoDoChat @JvmOverloads constructor(
    context: Context,
    atributos: AttributeSet? = null,
    estiloPadrao: Int = 0,
) : View(context, atributos, estiloPadrao) {

    private val densidade = resources.displayMetrics.density
    private val espessura = 3f * densidade
    private val tintaDoFundo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = espessura
        color = Color.parseColor("#3C4043")
    }
    private val tintaDoProgresso = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = espessura
        strokeCap = Paint.Cap.ROUND
        color = COR_OK
    }
    private val area = RectF()
    private var percentual = 0

    init {
        definirDescricao()
    }

    fun setProgresso(percentual: Int, faixa: FaixaDoMedidorDoHistoricoDoChat) {
        this.percentual = percentual.coerceIn(0, 100)
        tintaDoProgresso.color = when (faixa) {
            FaixaDoMedidorDoHistoricoDoChat.OK -> COR_OK
            FaixaDoMedidorDoHistoricoDoChat.ATENCAO -> COR_ATENCAO
            FaixaDoMedidorDoHistoricoDoChat.CHEIO -> COR_CHEIO
        }
        definirDescricao()
        invalidate()
    }

    override fun onMeasure(larguraSpec: Int, alturaSpec: Int) {
        val padrao = (28 * densidade).toInt()
        val largura = resolveSize(padrao, larguraSpec)
        val altura = resolveSize(padrao, alturaSpec)
        setMeasuredDimension(largura, altura)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val lado = minOf(width - paddingLeft - paddingRight, height - paddingTop - paddingBottom).toFloat()
        if (lado <= espessura) return
        val esquerda = paddingLeft + (width - paddingLeft - paddingRight - lado) / 2f + espessura / 2f
        val topo = paddingTop + (height - paddingTop - paddingBottom - lado) / 2f + espessura / 2f
        area.set(esquerda, topo, esquerda + lado - espessura, topo + lado - espessura)
        canvas.drawArc(area, 0f, 360f, false, tintaDoFundo)
        if (percentual > 0) {
            canvas.drawArc(area, -90f, 360f * percentual / 100f, false, tintaDoProgresso)
        }
    }

    private fun definirDescricao() {
        contentDescription = context.getString(R.string.chat_medidor_do_historico_descricao, percentual)
    }

    private companion object {
        val COR_OK = Color.parseColor("#8AB4F8")
        val COR_ATENCAO = Color.parseColor("#FDD663")
        val COR_CHEIO = Color.parseColor("#F28B82")
    }
}

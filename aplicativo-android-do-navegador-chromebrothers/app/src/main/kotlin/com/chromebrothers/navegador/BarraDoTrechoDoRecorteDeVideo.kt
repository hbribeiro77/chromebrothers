package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Linha do tempo do vídeo inteiro no modo recorte: marquinhas dos quadros-chave, trecho escolhido em
 * destaque, alças de início/fim e a posição atual. Tocar/arrastar avisa o tempo em [aoEscolherTempo].
 */
class BarraDoTrechoDoRecorteDeVideo @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    var duracaoMs: Long = 0L
        set(valor) { field = valor; invalidate() }
    var quadrosChave: List<Long> = emptyList()
        set(valor) { field = valor; invalidate() }
    var inicioMs: Long? = null
        set(valor) { field = valor; invalidate() }
    var fimMs: Long? = null
        set(valor) { field = valor; invalidate() }
    var posicaoMs: Long = 0L
        set(valor) { if (field != valor) { field = valor; invalidate() } }
    var aoEscolherTempo: ((Long) -> Unit)? = null

    private val densidade = resources.displayMetrics.density
    private val margem = 12f * densidade
    private val trilha = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#3A4150") }
    private val marca = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8A9099")
        strokeWidth = 1f * densidade
    }
    private val trecho = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#66F5C518") }
    private val alca = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F5C518")
        strokeWidth = 3f * densidade
    }
    private val posicao = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 2f * densidade
    }
    private val retangulo = RectF()

    private fun xDe(ms: Long): Float {
        val largura = (width - 2 * margem).coerceAtLeast(1f)
        if (duracaoMs <= 0L) return margem
        return margem + largura * (ms.coerceIn(0L, duracaoMs).toFloat() / duracaoMs)
    }

    private fun tempoDe(x: Float): Long {
        val largura = (width - 2 * margem).coerceAtLeast(1f)
        val fracao = ((x - margem) / largura).coerceIn(0f, 1f)
        return (fracao * duracaoMs).toLong()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val meio = height / 2f
        val altura = 10f * densidade
        retangulo.set(margem, meio - altura / 2, width - margem, meio + altura / 2)
        canvas.drawRoundRect(retangulo, altura / 2, altura / 2, trilha)
        if (duracaoMs <= 0L) return
        // Quadros-chave: onde a cópia pura consegue começar.
        quadrosChave.forEach { ms ->
            val x = xDe(ms)
            canvas.drawLine(x, meio - altura, x, meio + altura, marca)
        }
        val a = inicioMs
        val b = fimMs
        if (a != null && b != null) {
            retangulo.set(xDe(minOf(a, b)), meio - altura, xDe(maxOf(a, b)), meio + altura)
            canvas.drawRect(retangulo, trecho)
        }
        a?.let { canvas.drawLine(xDe(it), 2f, xDe(it), height - 2f, alca) }
        b?.let { canvas.drawLine(xDe(it), 2f, xDe(it), height - 2f, alca) }
        val x = xDe(posicaoMs)
        canvas.drawLine(x, 0f, x, height.toFloat(), posicao)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (duracaoMs <= 0L) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                val tempo = tempoDe(event.x)
                posicaoMs = tempo
                aoEscolherTempo?.invoke(tempo)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> return true
        }
        return super.onTouchEvent(event)
    }
}

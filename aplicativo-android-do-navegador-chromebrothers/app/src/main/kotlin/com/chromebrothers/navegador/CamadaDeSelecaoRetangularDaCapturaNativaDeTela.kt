package com.chromebrothers.navegador

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.chromebrothers.navegador.regras.RetanguloNormalizadoDaSelecaoDeCaptura

@SuppressLint("ViewConstructor")
class CamadaDeSelecaoRetangularDaCapturaNativaDeTela(
    context: Context,
    private val aoConfirmar: (RetanguloNormalizadoDaSelecaoDeCaptura) -> Unit,
    private val aoCancelar: () -> Unit,
) : FrameLayout(context) {
    private var origemX = 0f
    private var origemY = 0f
    private var atualX = 0f
    private var atualY = 0f
    private var arrastando = false
    private val pinturaEscura = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(140, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val pinturaBorda = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f * resources.displayMetrics.density
    }
    private val desenho = object : View(context) {
        override fun onDraw(canvas: Canvas) {
            val area = RetanguloNormalizadoDaSelecaoDeCaptura.de(
                x1 = origemX,
                y1 = origemY,
                x2 = atualX,
                y2 = atualY,
                larguraMaxima = width,
                alturaMaxima = height,
            )
            if (area.largura <= 0 || area.altura <= 0) {
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), pinturaEscura)
                return
            }
            val ret = RectF(
                area.esquerda.toFloat(),
                area.topo.toFloat(),
                (area.esquerda + area.largura).toFloat(),
                (area.topo + area.altura).toFloat(),
            )
            canvas.save()
            canvas.clipOutRect(ret)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), pinturaEscura)
            canvas.restore()
            canvas.drawRect(ret, pinturaBorda)
        }
    }

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        isClickable = true
        isFocusable = true
        setBackgroundColor(Color.TRANSPARENT)
        addView(desenho, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        val dica = TextView(context).apply {
            text = context.getString(R.string.dica_da_selecao_da_captura_nativa_de_tela)
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(
                (16 * resources.displayMetrics.density).toInt(),
                (12 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (12 * resources.displayMetrics.density).toInt(),
            )
            setBackgroundColor(Color.argb(180, 20, 24, 32))
        }
        addView(
            dica,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = android.view.Gravity.TOP
            },
        )
        setOnTouchListener { _, evento ->
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    origemX = evento.x
                    origemY = evento.y
                    atualX = evento.x
                    atualY = evento.y
                    arrastando = true
                    desenho.invalidate()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    atualX = evento.x
                    atualY = evento.y
                    desenho.invalidate()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    atualX = evento.x
                    atualY = evento.y
                    arrastando = false
                    val area = RetanguloNormalizadoDaSelecaoDeCaptura.de(
                        x1 = origemX,
                        y1 = origemY,
                        x2 = atualX,
                        y2 = atualY,
                        larguraMaxima = width,
                        alturaMaxima = height,
                    )
                    // post: remover a camada no meio do dispatch do toque derruba o app.
                    post {
                        if (area.largura < 8 || area.altura < 8) {
                            aoCancelar()
                        } else {
                            aoConfirmar(area)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    arrastando = false
                    post { aoCancelar() }
                    true
                }
                else -> false
            }
        }
    }
}

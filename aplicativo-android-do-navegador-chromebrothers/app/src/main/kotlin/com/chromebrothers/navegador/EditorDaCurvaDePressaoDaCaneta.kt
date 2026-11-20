package com.chromebrothers.navegador

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.chromebrothers.navegador.regras.CurvaDePressaoDaCaneta
import kotlin.math.hypot

/**
 * Janela da curva de pressão (estilo Procreate): arrastar pontos, tocar na curva para criar um ponto,
 * segurar um ponto para tirar. Embaixo, uma área para testar com a caneta: mostra no gráfico onde a sua
 * força cai e desenha um traço com a espessura que a curva dá.
 */
class EditorDaCurvaDePressaoDaCaneta(
    private val activity: Activity,
    curvaInicial: CurvaDePressaoDaCaneta,
    private val aoConfirmar: (CurvaDePressaoDaCaneta) -> Unit,
) {
    private val d = activity.resources.displayMetrics.density
    private fun dp(v: Float) = (v * d).toInt()
    private var curva = curvaInicial

    fun mostrar() {
        val dialogo = Dialog(activity)
        val grafico = GraficoDaCurva(activity) { nova -> curva = nova }
        grafico.curva = curva
        val teste = AreaDeTeste(activity, { curva }) { pressao -> grafico.pressaoDeTeste = pressao }
        val raiz = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20f), dp(18f), dp(20f), dp(12f))
            background = GradientDrawable().apply {
                cornerRadius = 24f * d
                setColor(Color.parseColor("#1A1D24"))
            }
        }
        raiz.addView(texto("Curva de pressão", 20f, "#F4F7FB", negrito = true))
        raiz.addView(
            texto(
                "Da esquerda (toque leve) para a direita (apertando forte); para cima = traço mais grosso e forte. " +
                    "Arraste os pontos, toque na curva para criar um, segure um ponto para tirar.",
                13f,
                "#9AA0AA",
            ).apply { setPadding(0, dp(6f), 0, dp(12f)) },
        )
        raiz.addView(grafico, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(300f)))
        raiz.addView(LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(12f), 0, dp(4f))
            listOf(
                "Leve" to CurvaDePressaoDaCaneta.LEVE,
                "Normal" to CurvaDePressaoDaCaneta.LINEAR,
                "Firme" to CurvaDePressaoDaCaneta.FIRME,
            ).forEach { (nome, predefinida) ->
                addView(chip(nome) {
                    curva = predefinida
                    grafico.curva = predefinida
                    teste.limpar()
                }, LinearLayout.LayoutParams(0, dp(40f), 1f).apply { marginEnd = dp(8f) })
            }
        })
        raiz.addView(texto("Teste aqui com a caneta:", 13f, "#9AA0AA").apply { setPadding(0, dp(10f), 0, dp(6f)) })
        raiz.addView(teste, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(110f)))
        raiz.addView(LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(14f), 0, 0)
            addView(botao("Cancelar", "#8A9099") { dialogo.dismiss() })
            addView(botao("Pronto", "#8AB4F8") {
                dialogo.dismiss()
                aoConfirmar(curva)
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(8f)
            })
        })
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        dialogo.window?.setLayout(dp(460f), WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun texto(t: String, sp: Float, cor: String, negrito: Boolean = false) = TextView(activity).apply {
        text = t
        textSize = sp
        setTextColor(Color.parseColor(cor))
        if (negrito) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun chip(t: String, aoTocar: () -> Unit) = TextView(activity).apply {
        text = t
        textSize = 14f
        gravity = Gravity.CENTER
        setTextColor(Color.parseColor("#F4F7FB"))
        background = GradientDrawable().apply {
            cornerRadius = 20f * d
            setColor(Color.parseColor("#2A3140"))
        }
        setOnClickListener { aoTocar() }
    }

    private fun botao(t: String, cor: String, aoTocar: () -> Unit) = TextView(activity).apply {
        text = t
        textSize = 15f
        setTextColor(Color.parseColor(cor))
        setPadding(dp(16f), dp(10f), dp(16f), dp(10f))
        setOnClickListener { aoTocar() }
    }

    /** O gráfico: grade, diagonal de referência, a curva e os pontos arrastáveis. */
    private class GraficoDaCurva(context: Context, private val aoMudar: (CurvaDePressaoDaCaneta) -> Unit) : View(context) {
        private val d = resources.displayMetrics.density
        var curva: CurvaDePressaoDaCaneta = CurvaDePressaoDaCaneta.LINEAR
            set(valor) {
                field = valor
                invalidate()
            }
        /** Pressão crua da área de teste (−1 = nenhuma). */
        var pressaoDeTeste: Float = -1f
            set(valor) {
                field = valor
                invalidate()
            }
        private var arrastando = -1
        private var xDoToque = 0f
        private var yDoToque = 0f
        private val tirarPonto = Runnable {
            if (arrastando > 0 && arrastando < curva.pontos.lastIndex) {
                curva = curva.semPonto(arrastando)
                arrastando = -1
                aoMudar(curva)
                performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            }
        }
        private val grade = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2A2F38"); strokeWidth = 1f * d }
        private val diagonal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3A404C")
            strokeWidth = 1.5f * d
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(6f * d, 6f * d), 0f)
        }
        private val linha = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8AB4F8")
            strokeWidth = 3f * d
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        private val ponto = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F4F7FB") }
        private val pontoAtivo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#8AB4F8") }
        private val teste = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F5C518"); strokeWidth = 1.5f * d }
        private val fundo = Paint().apply { color = Color.parseColor("#12151B") }
        private val caminho = Path()
        private val margem = 14f * d

        private fun esquerda() = margem
        private fun topo() = margem
        private fun lado() = minOf(width, height) - 2 * margem
        private fun deslocamentoX() = (width - lado()) / 2f
        private fun telaX(x: Float) = deslocamentoX() + x * lado()
        private fun telaY(y: Float) = topo() + (1f - y) * lado()
        private fun curvaX(px: Float) = ((px - deslocamentoX()) / lado()).coerceIn(0f, 1f)
        private fun curvaY(py: Float) = (1f - (py - topo()) / lado()).coerceIn(0f, 1f)

        override fun onDraw(canvas: Canvas) {
            val l = lado()
            val x0 = deslocamentoX()
            canvas.drawRect(x0, topo(), x0 + l, topo() + l, fundo)
            for (i in 0..4) {
                val f = i / 4f
                canvas.drawLine(x0 + f * l, topo(), x0 + f * l, topo() + l, grade)
                canvas.drawLine(x0, topo() + f * l, x0 + l, topo() + f * l, grade)
            }
            canvas.drawLine(telaX(0f), telaY(0f), telaX(1f), telaY(1f), diagonal)
            caminho.reset()
            for (i in 0..120) {
                val x = i / 120f
                val px = telaX(x)
                val py = telaY(curva.aplicar(x))
                if (i == 0) caminho.moveTo(px, py) else caminho.lineTo(px, py)
            }
            canvas.drawPath(caminho, linha)
            if (pressaoDeTeste >= 0f) {
                val px = telaX(pressaoDeTeste)
                val py = telaY(curva.aplicar(pressaoDeTeste))
                canvas.drawLine(px, topo(), px, topo() + l, teste)
                canvas.drawCircle(px, py, 7f * d, teste)
            }
            curva.pontos.forEachIndexed { i, p ->
                canvas.drawCircle(telaX(p.x), telaY(p.y), 9f * d, if (i == arrastando) pontoAtivo else ponto)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    xDoToque = event.x
                    yDoToque = event.y
                    val maisPerto = curva.pontos.indices.minByOrNull { i ->
                        hypot(telaX(curva.pontos[i].x) - event.x, telaY(curva.pontos[i].y) - event.y)
                    } ?: -1
                    val distancia = if (maisPerto >= 0) {
                        hypot(telaX(curva.pontos[maisPerto].x) - event.x, telaY(curva.pontos[maisPerto].y) - event.y)
                    } else {
                        Float.MAX_VALUE
                    }
                    arrastando = if (distancia <= 30f * d) {
                        maisPerto
                    } else {
                        // Tocou fora de um ponto: cria um na curva, nesse x, e já arrasta.
                        curva.comPontoNovo(curvaX(event.x))?.let { (nova, indice) ->
                            curva = nova
                            aoMudar(nova)
                            indice
                        } ?: -1
                    }
                    if (arrastando > 0 && arrastando < curva.pontos.lastIndex) postDelayed(tirarPonto, 650)
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (arrastando < 0) return true
                    if (hypot(event.x - xDoToque, event.y - yDoToque) > 8f * d) removeCallbacks(tirarPonto)
                    curva = curva.comPontoMovido(arrastando, curvaX(event.x), curvaY(event.y))
                    aoMudar(curva)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    removeCallbacks(tirarPonto)
                    arrastando = -1
                    invalidate()
                }
            }
            return true
        }
    }

    /** Área de teste: desenha um traço com a espessura que a curva dá para a força da caneta. */
    private class AreaDeTeste(
        context: Context,
        private val curvaAtual: () -> CurvaDePressaoDaCaneta,
        private val aoPressionar: (Float) -> Unit,
    ) : View(context) {
        private val d = resources.displayMetrics.density
        private val pontos = mutableListOf<Triple<Float, Float, Float>>()
        private val tinta = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F4F7FB") }
        private val fundo = Paint().apply { color = Color.parseColor("#12151B") }

        fun limpar() {
            pontos.clear()
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 12f * d, 12f * d, fundo)
            val curva = curvaAtual()
            // Círculos colados ao longo do traço, com raio pela pressão já passada na curva.
            for (i in pontos.indices) {
                val (x, y, p) = pontos[i]
                val raio = 1f * d + curva.aplicar(p) * 14f * d
                if (i > 0) {
                    val (xa, ya, pa) = pontos[i - 1]
                    val raioA = 1f * d + curva.aplicar(pa) * 14f * d
                    val passos = (hypot(x - xa, y - ya) / (1.5f * d)).toInt().coerceAtLeast(1)
                    for (k in 1..passos) {
                        val t = k / passos.toFloat()
                        canvas.drawCircle(xa + (x - xa) * t, ya + (y - ya) * t, raioA + (raio - raioA) * t, tinta)
                    }
                } else {
                    canvas.drawCircle(x, y, raio, tinta)
                }
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) pontos.clear()
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                aoPressionar(-1f)
                return true
            }
            parent?.requestDisallowInterceptTouchEvent(true)
            val caneta = event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS
            for (h in 0 until event.historySize) {
                pontos += Triple(event.getHistoricalX(h), event.getHistoricalY(h), if (caneta) event.getHistoricalPressure(h) else 0.5f)
            }
            val pressao = if (caneta) event.pressure.coerceIn(0f, 1f) else 0.5f
            pontos += Triple(event.x, event.y, pressao)
            if (pontos.size > 2000) pontos.subList(0, pontos.size - 2000).clear()
            aoPressionar(pressao)
            invalidate()
            return true
        }
    }
}

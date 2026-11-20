package com.chromebrothers.navegador

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.chromebrothers.navegador.regras.MotorDeCarimbosDoPincel
import com.chromebrothers.navegador.regras.PincelDoMotor
import com.chromebrothers.navegador.regras.PontoParaCarimbar
import kotlin.math.PI
import kotlin.math.sin

/**
 * Amostra que a IA vê ao testar um pincel: em cima, uma curva em S com a pressão subindo e descendo
 * (mostra afinamento, resposta à pressão e textura); embaixo, um traço reto com pressão leve e constante
 * (mostra espaçamento, falhas e granulado). Fundo de papel claro.
 */
object AmostraDoPincelParaOChat {
    private const val LARGURA = 1100
    private const val ALTURA = 560
    private const val DIAMETRO_BASE = 34f

    fun desenhar(pincel: PincelDoMotor, cor: Int = Color.BLACK): Bitmap {
        val bitmap = Bitmap.createBitmap(LARGURA, ALTURA, Bitmap.Config.ARGB_8888)
        val tela = Canvas(bitmap)
        tela.drawColor(Color.rgb(246, 243, 236))
        val linha = Paint().apply { color = Color.rgb(225, 220, 210); strokeWidth = 2f }
        tela.drawLine(40f, ALTURA * 0.66f, LARGURA - 40f, ALTURA * 0.66f, linha)
        val renderizador = RenderizadorDeCarimbosDoPincel()
        val corOpaca = cor or 0xFF000000.toInt()
        val diametro = DIAMETRO_BASE * pincel.multiplicadorDeLargura

        // Curva em S: pressão 0 → 1 → 0 (sin), passando pelo normal.
        val curva = (0..90).map { i ->
            val t = i / 90f
            val pressao = sin(t * PI).toFloat().coerceAtLeast(0.08f)
            PontoParaCarimbar(
                x = 70f + t * (LARGURA - 140f),
                y = ALTURA * 0.33f + sin(t * 2 * PI).toFloat() * ALTURA * 0.16f,
                largura = diametro * MotorDeCarimbosDoPincel.fatorDaPressaoNoTamanho(pressao, pincel.pressaoNoTamanho),
                alpha = MotorDeCarimbosDoPincel.fatorDaPressaoNaOpacidade(pressao, pincel.pressaoNaOpacidade),
            )
        }
        renderizador.desenharTraco(tela, MotorDeCarimbosDoPincel.gerar(curva, pincel, 7L, comAfinamentoDoFim = true), corOpaca, pincel)

        // Reta com pressão leve (0,3) e constante.
        val leve = 0.3f
        val reta = (0..60).map { i ->
            val t = i / 60f
            PontoParaCarimbar(
                x = 70f + t * (LARGURA - 140f),
                y = ALTURA * 0.82f,
                largura = diametro * MotorDeCarimbosDoPincel.fatorDaPressaoNoTamanho(leve, pincel.pressaoNoTamanho),
                alpha = MotorDeCarimbosDoPincel.fatorDaPressaoNaOpacidade(leve, pincel.pressaoNaOpacidade),
            )
        }
        renderizador.desenharTraco(tela, MotorDeCarimbosDoPincel.gerar(reta, pincel, 11L, comAfinamentoDoFim = true), corOpaca, pincel)
        return bitmap
    }
}

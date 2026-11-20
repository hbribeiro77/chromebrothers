package com.chromebrothers.navegador.regras

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class TesteDaFormaLimpaNoFimDoTraco {
    private val f = FormaLimpaNoFimDoTraco
    private val aleatorio = Random(7)
    private fun tremido(x: Float, y: Float, amp: Float = 2f) =
        FormaLimpaNoFimDoTraco.Ponto(x + (aleatorio.nextFloat() - 0.5f) * amp, y + (aleatorio.nextFloat() - 0.5f) * amp)

    @Test
    fun rabiscoQuaseRetoViraLinha() {
        val pontos = (0..40).map { tremido(10f + it * 5f, 50f + it * 2f) }
        val forma = f.reconhecer(pontos)
        assertIs<FormaLimpaNoFimDoTraco.Forma.Linha>(forma)
        assertEquals(2, f.pontosDa(forma).size)
    }

    @Test
    fun voltaFechadaRedondaViraCirculo() {
        val pontos = (0..60).map { i ->
            val a = 2 * PI * i / 58
            tremido(100f + (40 * cos(a)).toFloat(), 100f + (38 * sin(a)).toFloat())
        }
        val forma = f.reconhecer(pontos)
        assertIs<FormaLimpaNoFimDoTraco.Forma.Elipse>(forma)
        assertEquals(forma.rx, forma.ry)
    }

    @Test
    fun voltaComCantosViraRetangulo() {
        val lados = listOf(
            (0..20).map { tremido(10f + it * 8f, 10f) },
            (0..12).map { tremido(170f, 10f + it * 8f) },
            (0..20).map { tremido(170f - it * 8f, 106f) },
            (0..12).map { tremido(10f, 106f - it * 8f) },
        ).flatten()
        assertIs<FormaLimpaNoFimDoTraco.Forma.Retangulo>(f.reconhecer(lados))
    }

    @Test
    fun rabiscoQualquerFicaComoEsta() {
        val zigue = (0..30).map { FormaLimpaNoFimDoTraco.Ponto(it * 6f, if (it % 2 == 0) 0f else 40f) }
        assertNull(f.reconhecer(zigue))
        assertNull(f.reconhecer(listOf(FormaLimpaNoFimDoTraco.Ponto(0f, 0f), FormaLimpaNoFimDoTraco.Ponto(1f, 1f))))
    }
}

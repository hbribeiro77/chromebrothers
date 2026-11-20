package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.PreenchimentoDoBalde as B
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoPreenchimentoDoBalde {
    private val preto = 0xFF000000.toInt()
    private val transparente = 0

    /** 10×10 transparente com um quadrado preto (contorno) de 2..7. */
    private fun quadrado(): IntArray {
        val p = IntArray(100) { transparente }
        for (i in 2..7) {
            p[2 * 10 + i] = preto; p[7 * 10 + i] = preto
            p[i * 10 + 2] = preto; p[i * 10 + 7] = preto
        }
        return p
    }

    @Test
    fun preencheSoDentroDoContorno() {
        val area = B.area(quadrado(), 10, 10, 4, 4, 0.1f, crescer = 0)!!
        assertEquals(16, area.quantidade) // 3..6 × 3..6
        assertTrue(area.mascara[4 * 10 + 4])
        assertFalse(area.mascara[0])
        assertFalse(area.mascara[2 * 10 + 2])
    }

    @Test
    fun crescerCobreABordaDaLinha() {
        val area = B.area(quadrado(), 10, 10, 4, 4, 0.1f, crescer = 1)!!
        assertTrue(area.mascara[2 * 10 + 4], "a linha de cima recebe 1 px")
        assertFalse(area.mascara[1 * 10 + 4], "mas não vaza para fora")
    }

    @Test
    fun foraDoContornoPegaTudoMenosODeDentro() {
        val area = B.area(quadrado(), 10, 10, 0, 0, 0.1f, crescer = 0)!!
        assertEquals(100 - 16 - 20, area.quantidade)
    }

    @Test
    fun toleranciaDecideOQueEParecido() {
        val cinza = 0xFF808080.toInt()
        val quaseCinza = 0xFF909090.toInt()
        val p = IntArray(4) { if (it < 2) cinza else quaseCinza }
        assertEquals(2, B.area(p, 4, 1, 0, 0, 0.02f, crescer = 0)!!.quantidade)
        assertEquals(4, B.area(p, 4, 1, 0, 0, 0.1f, crescer = 0)!!.quantidade)
    }

    @Test
    fun transparentesComCoresDiferentesContamComoIguais() {
        val p = intArrayOf(0x00FF0000, 0x0000FF00)
        assertEquals(2, B.area(p, 2, 1, 0, 0, 0f, crescer = 0)!!.quantidade)
    }

    @Test
    fun pontoForaDaImagemNaoPreenche() {
        assertNull(B.area(quadrado(), 10, 10, 10, 0, 0.1f))
        assertNull(B.area(quadrado(), 10, 10, -1, 3, 0.1f))
    }

    @Test
    fun imagemGrandeNaoEstouraAPilha() {
        val area = B.area(IntArray(1500 * 1500), 1500, 1500, 700, 700, 0.1f, crescer = 0)!!
        assertEquals(1500 * 1500, area.quantidade)
    }

    @Test
    fun escalaLimitaAReferencia() {
        assertEquals(1f, B.escalaDaReferencia(1000, 1000))
        val e = B.escalaDaReferencia(4000, 4000)
        assertTrue(4000 * e * 4000 * e <= 4_000_001f)
    }
}

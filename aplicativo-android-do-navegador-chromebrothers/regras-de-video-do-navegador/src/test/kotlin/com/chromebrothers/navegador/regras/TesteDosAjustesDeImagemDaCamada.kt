package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDosAjustesDeImagemDaCamada {
    private val a = AjustesDeImagemDaCamada
    private fun valores(
        brilho: Int = 0, contraste: Int = 0, saturacao: Int = 0, matiz: Int = 0, pb: Boolean = false,
    ) = AjustesDeImagemDaCamada.Valores(brilho, contraste, saturacao, matiz, pb)

    private val laranja = 0xFFE07020.toInt()

    @Test
    fun semAjusteNaoMudaOPixel() {
        val m = a.matriz(valores())
        assertEquals(laranja, a.aplicarNoPixel(m, laranja))
        assertTrue(valores().neutro)
    }

    @Test
    fun pretoEBrancoDeixaOsTresCanaisIguais() {
        val p = a.aplicarNoPixel(a.matriz(valores(pb = true)), laranja)
        val r = (p shr 16) and 0xFF
        val g = (p shr 8) and 0xFF
        val b = p and 0xFF
        assertTrue(kotlin.math.abs(r - g) <= 1 && kotlin.math.abs(g - b) <= 1)
        assertEquals(0xFF, (p ushr 24) and 0xFF)
    }

    @Test
    fun brilhoClareiaEContrasteAfastaDoMeio() {
        val cinza = 0xFF606060.toInt()
        val claro = a.aplicarNoPixel(a.matriz(valores(brilho = 50)), cinza)
        assertTrue((claro and 0xFF) > 0x60)
        val contrastado = a.aplicarNoPixel(a.matriz(valores(contraste = 50)), cinza)
        assertTrue((contrastado and 0xFF) < 0x60) // abaixo do meio fica mais escuro
    }

    @Test
    fun matizDe360VoltaAoMesmo() {
        val girado = a.aplicarNoPixel(a.matriz(valores(matiz = 180)), laranja)
        val devolta = a.aplicarNoPixel(a.matriz(valores(matiz = 180)), girado)
        assertTrue(kotlin.math.abs(((devolta shr 16) and 0xFF) - 0xE0) <= 3)
    }

    @Test
    fun desfoqueEspalhaUmPontoSemEscurecerAoRedorDoTransparente() {
        val w = 9
        val h = 9
        val pixels = IntArray(w * h) { 0 }
        pixels[4 * w + 4] = 0xFFFFFFFF.toInt()
        a.desfocar(pixels, w, h, 1)
        val centro = pixels[4 * w + 4]
        val vizinho = pixels[4 * w + 5]
        assertTrue(((centro ushr 24) and 0xFF) in 1..254)
        assertTrue(((vizinho ushr 24) and 0xFF) > 0)
        // A cor que vaza para a vizinhança continua branca (não puxa o preto do transparente).
        assertEquals(0xFF, (vizinho shr 16) and 0xFF)
    }
}

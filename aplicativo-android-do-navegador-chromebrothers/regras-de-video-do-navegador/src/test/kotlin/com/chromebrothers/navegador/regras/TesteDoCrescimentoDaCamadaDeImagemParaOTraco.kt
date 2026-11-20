package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoCrescimentoDaCamadaDeImagemParaOTraco {
    private val r = CrescimentoDaCamadaDeImagemParaOTraco
    private fun p(x: Float, y: Float, raio: Float = 2f) = CrescimentoDaCamadaDeImagemParaOTraco.PontoDoTraco(x, y, raio)

    @Test
    fun tracoDentroDaImagemNaoMexe() {
        assertNull(r.novoRetangulo(50f, 100f, 200f, 100f, 0f, listOf(p(60f, 120f), p(240f, 190f))))
    }

    @Test
    fun tracoQuePassaDaBordaCresceAteOTraco() {
        val novo = assertNotNull(r.novoRetangulo(50f, 100f, 200f, 100f, 0f, listOf(p(100f, 150f), p(380f, 20f, 4f))))
        assertEquals(50f, novo.esquerda)
        assertEquals(16f, novo.topo)
        assertEquals(384f, novo.direita)
        assertEquals(200f, novo.base)
    }

    @Test
    fun imagemGiradaUsaACaixaGirada() {
        // 90°: a imagem 200×100 centrada em (150,150) vira 100×200.
        val novo = assertNotNull(r.novoRetangulo(50f, 100f, 200f, 100f, 90f, listOf(p(150f, 150f), p(300f, 150f, 0f))))
        assertEquals(100f, novo.esquerda, 0.01f)
        assertEquals(50f, novo.topo, 0.01f)
        assertEquals(300f, novo.direita, 0.01f)
        assertEquals(250f, novo.base, 0.01f)
    }

    @Test
    fun escalaLimitaLadoEArea() {
        assertEquals(1f, r.escala(2000f, 1500f))
        assertEquals(0.5f, r.escala(8192f, 1000f))
        assertTrue(r.escala(5000f, 5000f) * 5000f <= 4000.5f)
    }
}

package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.RegrasDaJanelaFlutuanteDoPlayerDeVideoSobreANavegacao.Retangulo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDasRegrasDaJanelaFlutuanteDoPlayerDeVideoSobreANavegacao {
    private val regras = RegrasDaJanelaFlutuanteDoPlayerDeVideoSobreANavegacao

    @Test
    fun inicialFicaNoCantoInferiorDireitoEmProporcaoDeVideo() {
        val r = regras.inicial(larguraDaArea = 2000, alturaDaArea = 1200, densidade = 2f, alturaDaAlca = 68)
        assertEquals(900, r.largura)
        assertEquals(900 * 9 / 16 + 68, r.altura)
        assertEquals(2000 - 900 - 32, r.x)
        assertEquals(1200 - r.altura - 32, r.y)
    }

    @Test
    fun moverNaoDeixaSairDaArea() {
        val r = Retangulo(100, 100, 700, 400)
        assertEquals(Retangulo(0, 0, 700, 400), regras.mover(r, -500, -500, 2000, 1200, 2f))
        assertEquals(Retangulo(1300, 800, 700, 400), regras.mover(r, 5000, 5000, 2000, 1200, 2f))
    }

    @Test
    fun redimensionarRespeitaMinimoEBordaDaArea() {
        val r = Retangulo(1000, 600, 700, 400)
        assertEquals(Retangulo(1000, 600, 600, 380), regras.redimensionar(r, -900, -900, 2000, 1200, 2f))
        assertEquals(Retangulo(1000, 600, 1000, 600), regras.redimensionar(r, 900, 900, 2000, 1200, 2f))
    }

    @Test
    fun areaEncolhidaPuxaAJanelaParaDentro() {
        val r = regras.ajustar(Retangulo(1500, 900, 700, 400), 1000, 800, 2f)
        assertEquals(Retangulo(300, 400, 700, 400), r)
    }

    @Test
    fun textoGuardadoVoltaIgualEInvalidoViraNull() {
        val r = Retangulo(1, 2, 300, 200)
        assertEquals(r, regras.deTexto(regras.paraTexto(r)))
        assertNull(regras.deTexto("1,2,0,5"))
        assertNull(regras.deTexto(null))
        assertNull(regras.deTexto("abc"))
    }
}

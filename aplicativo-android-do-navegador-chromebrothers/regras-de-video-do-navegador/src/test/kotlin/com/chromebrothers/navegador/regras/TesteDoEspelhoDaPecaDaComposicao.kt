package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.EspelhoDaPecaDaComposicao.Eixo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoEspelhoDaPecaDaComposicao {
    private val regras = EspelhoDaPecaDaComposicao

    @Test
    fun segundoToqueNoMesmoEixoDesfaz() {
        assertTrue(regras.alternar(false))
        assertFalse(regras.alternar(true))
    }

    @Test
    fun soImagemEspelhaESoImagemDuplicaEnquantoCabeCamada() {
        assertTrue(regras.aceitaEspelho(ehCamadaDePintura = true))
        assertFalse(regras.aceitaEspelho(ehCamadaDePintura = false))
        assertTrue(regras.podeDuplicar(ehCamadaDePintura = true, quantidadeDeCamadas = 7))
        assertFalse(regras.podeDuplicar(ehCamadaDePintura = true, quantidadeDeCamadas = 8))
        assertFalse(regras.podeDuplicar(ehCamadaDePintura = false, quantidadeDeCamadas = 1))
    }

    @Test
    fun selecionarTudoSoValeEmImagemEAbreSelecaoAtivaDaFerramenta() {
        assertTrue(regras.podeSelecionarInteira(ehCamadaDePintura = true))
        assertFalse(regras.podeSelecionarInteira(ehCamadaDePintura = false))
        assertTrue(regras.selecionarTudoAbreSelecaoAtivaDaFerramenta())
    }

    @Test
    fun botoesFicamLadoALadoAbaixoDaPecaEOToqueEscolheOEixo() {
        val botoes = regras.botoes(centroX = 200f, baseY = 100f, densidade = 2f)
        assertEquals(Eixo.HORIZONTAL, botoes[0].eixo)
        assertEquals(Eixo.VERTICAL, botoes[1].eixo)
        assertTrue(botoes[0].direita() < botoes[1].esquerda)
        assertTrue(botoes[0].topo > 100f)
        assertEquals(Eixo.HORIZONTAL, regras.eixoNoPonto(botoes, botoes[0].esquerda + 4f, botoes[0].topo + 4f))
        assertEquals(Eixo.VERTICAL, regras.eixoNoPonto(botoes, botoes[1].esquerda + 4f, botoes[1].topo + 4f))
        assertNull(regras.eixoNoPonto(botoes, 0f, 0f))
    }
}

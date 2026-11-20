package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.DestinoDaAbaArrastada
import com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.JanelaSobreAFaixa
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante {
    private val regra = ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante
    private val abas = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador

    @Test
    fun abaSoltaForaDaGavetaDesvincula() {
        assertEquals(DestinoDaAbaArrastada.DESVINCULAR, regra.destinoDaAbaArrastada(false, false))
        assertEquals(DestinoDaAbaArrastada.REORDENAR, regra.destinoDaAbaArrastada(true, true))
        assertEquals(DestinoDaAbaArrastada.VOLTAR, regra.destinoDaAbaArrastada(true, false))
    }

    @Test
    fun janelaSoArmaDepoisDoTempoSobreAFaixa() {
        val entrou = regra.atualizarJanelaSobreAFaixa(JanelaSobreAFaixa(), true, true, 1000L)
        assertFalse(regra.estaArmada(entrou, 1500L))
        val parada = regra.atualizarJanelaSobreAFaixa(entrou, true, true, 1600L)
        assertEquals(entrou, parada)
        assertTrue(regra.estaArmada(parada, 1000L + regra.ATRASO_MS))
    }

    @Test
    fun sairDaFaixaOuNaoAceitarDesarma() {
        val entrou = regra.atualizarJanelaSobreAFaixa(JanelaSobreAFaixa(), true, true, 1000L)
        assertEquals(JanelaSobreAFaixa(), regra.atualizarJanelaSobreAFaixa(entrou, false, true, 1200L))
        assertEquals(JanelaSobreAFaixa(), regra.atualizarJanelaSobreAFaixa(entrou, true, false, 1200L))
        assertFalse(regra.estaArmada(JanelaSobreAFaixa(), 99999L))
    }

    @Test
    fun reordenaSoEntreComponentes() {
        val estado = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado(
            componentes = listOf(
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Aba("a", "A"),
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Aba("b", "B"),
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Aba("c", "C"),
            ),
            idAtiva = "b",
        )
        val movido = abas.moverComponente(estado, "a", 2)
        assertEquals(listOf("b", "c", "a"), movido.componentes.map { it.id })
        assertEquals("b", movido.idAtiva)
        assertEquals(listOf("c", "a", "b"), abas.moverComponente(estado, "c", -5).componentes.map { it.id })
        assertEquals(estado, abas.moverComponente(estado, "x", 0))
        assertEquals(listOf("chat", "gravador", "b", "c", "a"), abas.idsDoSwipe(movido))
    }

    @Test
    fun soltarSobreAFaixaFixaSemEsperar() {
        val regra = ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante
        assertTrue(regra.soltarFixa(sobreAFaixa = true, aceita = true))
        assertFalse(regra.soltarFixa(sobreAFaixa = false, aceita = true))
        assertFalse(regra.soltarFixa(sobreAFaixa = true, aceita = false))
    }
}

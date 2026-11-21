package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoPreCarregamentoDeSitePesadoNaAbertura {
    private val regra = PreCarregamentoDeSitePesadoNaAbertura

    @Test
    fun reconheceOSitePesadoESubdominios() {
        assertTrue(regra.ehSitePesado("https://higgsfield.ai/"))
        assertTrue(regra.ehSitePesado("https://higgsfield.ai/market"))
        assertTrue(regra.ehSitePesado("https://www.higgsfield.ai/image"))
        assertFalse(regra.ehSitePesado("https://nothiggsfield.ai/"), "outro domínio que só termina igual")
        assertFalse(regra.ehSitePesado("https://g1.globo.com/"))
        assertFalse(regra.ehSitePesado(""))
        assertFalse(regra.ehSitePesado(null))
    }

    @Test
    fun escolheAUltimaAbaDoSiteQueNaoEhAAtiva() {
        val abas = listOf(1L to "https://g1.globo.com/", 2L to "https://higgsfield.ai/", 3L to "https://higgsfield.ai/market")
        assertEquals(3L, regra.abaParaPreCarregar(abas, idAtiva = 1L))
        assertEquals(2L, regra.abaParaPreCarregar(abas.take(2), idAtiva = 1L))
        assertNull(regra.abaParaPreCarregar(listOf(1L to "https://g1.globo.com/"), idAtiva = 9L))
        assertNull(regra.abaParaPreCarregar(listOf(2L to "https://higgsfield.ai/"), idAtiva = 2L), "a ativa já carrega sozinha")
    }

    @Test
    fun qualquerPaginaDoSiteUsaAReserva() {
        assertTrue(regra.deveUsarReserva("https://higgsfield.ai/", "https://higgsfield.ai/"))
        assertTrue(regra.deveUsarReserva("https://Higgsfield.ai", "https://www.higgsfield.ai/"))
        assertTrue(regra.deveUsarReserva("https://higgsfield.ai/supercomputer", "https://higgsfield.ai/"), "o que a barra completou")
        assertFalse(regra.deveUsarReserva("https://g1.globo.com/", "https://higgsfield.ai/"))
        assertFalse(regra.deveUsarReserva("https://higgsfield.ai/", null))
        assertFalse(regra.deveUsarReserva(null, "https://higgsfield.ai/"))
    }

    @Test
    fun aReservaTrocaDeRotaSoQuandoOPedidoEhOutraPagina() {
        assertNull(regra.rotaParaAbrirNaReserva("https://higgsfield.ai", "https://higgsfield.ai/"), "já está na inicial")
        assertNull(regra.rotaParaAbrirNaReserva("https://higgsfield.ai/supercomputer", "https://higgsfield.ai/supercomputer"))
        assertEquals("/supercomputer", regra.rotaParaAbrirNaReserva("https://higgsfield.ai/supercomputer", "https://higgsfield.ai/"))
        assertEquals("/ai/video?model=seedance_2_5", regra.rotaParaAbrirNaReserva("https://higgsfield.ai/ai/video?model=seedance_2_5", "https://higgsfield.ai/"))
        assertEquals("/", regra.rotaParaAbrirNaReserva("https://higgsfield.ai/", "https://higgsfield.ai/explore"))
    }
}

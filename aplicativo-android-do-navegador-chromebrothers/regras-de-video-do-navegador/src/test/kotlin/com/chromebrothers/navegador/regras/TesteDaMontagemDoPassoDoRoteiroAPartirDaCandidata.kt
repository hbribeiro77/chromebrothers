package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaMontagemDoPassoDoRoteiroAPartirDaCandidata {
    private val porSeletor = CandidataDoElementoParaORoteiro(
        rotulo = "id #q",
        seletor = "#q",
        texto = "",
    )
    private val porTexto = CandidataDoElementoParaORoteiro(
        rotulo = "texto",
        seletor = "",
        texto = "Entrar",
    )

    @Test
    fun montaAguardarPreencherClicar() {
        val aguardar = MontagemDoPassoDoRoteiroAPartirDaCandidata.aguardar(porSeletor, 5000)
        assertTrue(aguardar is PassoDoRoteiroDeAutomacao.Aguardar)
        assertEquals("#q", (aguardar as PassoDoRoteiroDeAutomacao.Aguardar).seletor)

        val preencher = MontagemDoPassoDoRoteiroAPartirDaCandidata.preencher(porSeletor, "chrome brothers")
        assertTrue(preencher is PassoDoRoteiroDeAutomacao.Preencher)
        assertEquals("chrome brothers", (preencher as PassoDoRoteiroDeAutomacao.Preencher).valor)

        val clicar = MontagemDoPassoDoRoteiroAPartirDaCandidata.clicar(porTexto)
        assertTrue(clicar is PassoDoRoteiroDeAutomacao.Clicar)
        assertEquals("Entrar", (clicar as PassoDoRoteiroDeAutomacao.Clicar).texto)
    }

    @Test
    fun montaClicarSe() {
        val passo = MontagemDoPassoDoRoteiroAPartirDaCandidata.clicarSe(
            seletorDaLista = "#rso a",
            indice = 0,
            contem = "Wikipedia",
            seNao = "parar",
        )
        assertTrue(passo is PassoDoRoteiroDeAutomacao.ClicarSe)
        val c = passo as PassoDoRoteiroDeAutomacao.ClicarSe
        assertEquals("#rso a", c.seletor)
        assertEquals("Wikipedia", c.contem)
    }
}

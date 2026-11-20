package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador.Decisao
import com.chromebrothers.navegador.regras.GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador.Inicio
import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoGestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador {
    private val regra = GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador

    @Test
    fun comChatFechadoSoABordaDireitaDaPaginaComecaOGesto() {
        assertEquals(Inicio.ABRIR_PELA_BORDA, regra.inicio(true, false, 990f, true, 1000f, 0f))
        assertEquals(Inicio.NENHUM, regra.inicio(true, false, 970f, true, 1000f, 0f))
        assertEquals(Inicio.NENHUM, regra.inicio(true, false, 990f, false, 1000f, 0f))
    }

    @Test
    fun comChatAbertoSoAJuncaoComecaOGestoDeRecolher() {
        assertEquals(Inicio.FECHAR_PELA_JUNCAO, regra.inicio(true, true, 590f, true, 1000f, 600f))
        assertEquals(Inicio.FECHAR_PELA_JUNCAO, regra.inicio(true, true, 610f, true, 1000f, 600f))
        assertEquals(Inicio.NENHUM, regra.inicio(true, true, 620f, true, 1000f, 600f))
        assertEquals(Inicio.NENHUM, regra.inicio(true, true, 990f, true, 1000f, 600f))
    }

    @Test
    fun desabilitadoNuncaComeca() {
        assertEquals(Inicio.NENHUM, regra.inicio(false, false, 995f, true, 1000f, 0f))
        assertEquals(Inicio.NENHUM, regra.inicio(false, true, 600f, true, 1000f, 600f))
    }

    @Test
    fun abrirDisparaParaAEsquerdaEFecharParaADireita() {
        assertEquals(Decisao.DISPARAR, regra.decidir(Inicio.ABRIR_PELA_BORDA, -60f, 5f))
        assertEquals(Decisao.AGUARDAR, regra.decidir(Inicio.ABRIR_PELA_BORDA, -30f, 5f))
        assertEquals(Decisao.DESISTIR, regra.decidir(Inicio.ABRIR_PELA_BORDA, 20f, 0f))
        assertEquals(Decisao.DISPARAR, regra.decidir(Inicio.FECHAR_PELA_JUNCAO, 60f, -10f))
        assertEquals(Decisao.DESISTIR, regra.decidir(Inicio.FECHAR_PELA_JUNCAO, -20f, 0f))
    }

    @Test
    fun rolagemVerticalDesisteEDiagonalNaoDispara() {
        assertEquals(Decisao.DESISTIR, regra.decidir(Inicio.ABRIR_PELA_BORDA, -10f, 40f))
        assertEquals(Decisao.AGUARDAR, regra.decidir(Inicio.FECHAR_PELA_JUNCAO, 50f, 40f))
        assertEquals(Decisao.DESISTIR, regra.decidir(Inicio.NENHUM, 100f, 0f))
    }
}

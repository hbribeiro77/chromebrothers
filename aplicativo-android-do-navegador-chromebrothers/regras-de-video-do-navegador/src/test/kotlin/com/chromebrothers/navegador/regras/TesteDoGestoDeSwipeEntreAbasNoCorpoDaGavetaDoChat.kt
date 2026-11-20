package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.GestoDeSwipeEntreAbasNoCorpoDaGavetaDoChat.Decisao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoGestoDeSwipeEntreAbasNoCorpoDaGavetaDoChat {
    private val regra = GestoDeSwipeEntreAbasNoCorpoDaGavetaDoChat

    private fun decidir(
        dx: Float,
        dy: Float = 0f,
        borda: Boolean = false,
        longo: Boolean = false,
        pagina: Boolean? = false,
        rola: Boolean = false,
        vizinha: Boolean = true,
    ) = regra.decidir(dx, dy, borda, longo, pagina, rola, vizinha)

    @Test
    fun aguardaAteADistanciaMinima() {
        assertEquals(Decisao.AGUARDAR, decidir(dx = 10f, dy = 5f))
    }

    @Test
    fun gestoHorizontalLivreTrocaDeAba() {
        assertEquals(Decisao.TROCAR_DE_ABA, decidir(dx = -40f, dy = 5f))
    }

    @Test
    fun verticalOuDiagonalFicaComAPagina() {
        assertEquals(Decisao.DEIXAR_PARA_A_PAGINA, decidir(dx = 5f, dy = 40f))
        assertEquals(Decisao.DEIXAR_PARA_A_PAGINA, decidir(dx = 40f, dy = 30f))
    }

    @Test
    fun paginaQueQuerOGestoOuSemRespostaGanha() {
        assertEquals(Decisao.DEIXAR_PARA_A_PAGINA, decidir(dx = 40f, pagina = true))
        assertEquals(Decisao.DEIXAR_PARA_A_PAGINA, decidir(dx = 40f, pagina = null))
        assertEquals(Decisao.DEIXAR_PARA_A_PAGINA, decidir(dx = 40f, rola = true))
    }

    @Test
    fun bordaTrocaMesmoComAPaginaQuerendo() {
        assertEquals(Decisao.TROCAR_DE_ABA, decidir(dx = 40f, borda = true, pagina = true, rola = true))
    }

    @Test
    fun toqueLongoESemVizinhaNuncaTrocam() {
        assertEquals(Decisao.DEIXAR_PARA_A_PAGINA, decidir(dx = 40f, longo = true, borda = true))
        assertEquals(Decisao.DEIXAR_PARA_A_PAGINA, decidir(dx = 40f, vizinha = false))
    }

    @Test
    fun abaVizinhaSemVoltaCircular() {
        val ids = listOf("chat", "a", "b")
        assertEquals("a", regra.abaVizinha(ids, "chat", paraAProxima = true))
        assertEquals("a", regra.abaVizinha(ids, "b", paraAProxima = false))
        assertNull(regra.abaVizinha(ids, "b", paraAProxima = true))
        assertNull(regra.abaVizinha(ids, "chat", paraAProxima = false))
        assertNull(regra.abaVizinha(ids, "x", paraAProxima = true))
    }

    @Test
    fun confirmaPorDistanciaOuPorVelocidade() {
        assertTrue(regra.deveConfirmarAoSoltar(-400f, 1000f, 0f, 2f))
        assertTrue(regra.deveConfirmarAoSoltar(-100f, 1000f, -2500f, 2f))
        assertFalse(regra.deveConfirmarAoSoltar(-100f, 1000f, -300f, 2f))
        assertFalse(regra.deveConfirmarAoSoltar(-400f, 1000f, 2500f, 2f))
    }

    @Test
    fun selectECampoDeTextoParadoNaoSeguramOSwipe() {
        val script = regra.scriptDaConsulta(10f, 20f)
        assertFalse(script.contains("'SELECT'"))
        assertTrue(script.contains("document.activeElement"))
        assertTrue(script.contains("e.type==='range'"))
    }

    @Test
    fun leRespostaDoJs() {
        assertTrue(regra.lerRespostaDaConsulta("true"))
        assertFalse(regra.lerRespostaDaConsulta("false"))
        assertFalse(regra.lerRespostaDaConsulta(null))
    }

    @Test
    fun gravadorSemSessaoFicaForaDaNavegacao() {
        val abas = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador
        val estado = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado(
            componentes = listOf(AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Aba("j1", "Comp")),
        )
        assertEquals(listOf("chat", "j1"), abas.idsNavegaveis(estado))
        assertEquals(listOf("chat", "gravador", "j1"), abas.idsNavegaveis(estado.copy(gravadorAberto = true)))
        assertEquals(listOf("chat", "gravador", "j1"), abas.idsDoSwipe(estado))
    }
}

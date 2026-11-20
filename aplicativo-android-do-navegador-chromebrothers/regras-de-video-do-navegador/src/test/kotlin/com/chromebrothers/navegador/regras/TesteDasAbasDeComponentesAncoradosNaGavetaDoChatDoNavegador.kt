package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDasAbasDeComponentesAncoradosNaGavetaDoChatDoNavegador {
    @Test
    fun ancoraAteOLimiteERecusaOSexto() {
        var estado = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado()
        repeat(5) { i ->
            val r = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ancorar(
                estado,
                id = "c$i",
                titulo = "Comp $i",
            )
            assertTrue(r is AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok)
            estado = (r as AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok).estado
        }
        assertEquals(5, estado.componentes.size)
        assertEquals("c4", estado.idAtiva)
        val recusa = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ancorar(
            estado,
            id = "c5",
            titulo = "Extra",
        )
        assertTrue(recusa is AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Recusado)
    }

    @Test
    fun desancorarAbaAtivaVoltaParaChat() {
        val comDuas = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado(
            componentes = listOf(
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Aba("a", "A"),
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Aba("b", "B"),
            ),
            idAtiva = "b",
        )
        val r = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.desancorar(comDuas, "b")
        assertTrue(r is AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok)
        val estado = (r as AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok).estado
        assertEquals(1, estado.componentes.size)
        assertEquals(AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_CHAT, estado.idAtiva)
    }

    @Test
    fun reancorarMesmoIdSoAtiva() {
        val base = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado(
            componentes = listOf(AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Aba("a", "A")),
            idAtiva = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_CHAT,
        )
        val r = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ancorar(base, "a", "A2")
        val estado = (r as AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok).estado
        assertEquals(1, estado.componentes.size)
        assertEquals("a", estado.idAtiva)
    }

    @Test
    fun faixaSempreTemChatEGravador() {
        val vazio = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado()
        assertTrue(AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.deveMostrarFaixa(vazio))
        val ids = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.abasVisiveis(vazio).map { it.id }
        assertEquals(
            listOf(
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_CHAT,
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_GRAVADOR,
            ),
            ids,
        )
    }

    @Test
    fun abrirGravadorAtivaAba() {
        val aberto = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.abrirGravador(
            AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado(),
        )
        assertTrue(aberto.gravadorAberto)
        assertEquals(AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_GRAVADOR, aberto.idAtiva)
        assertTrue(
            AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.precisaAbrirSessaoDoGravador(
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado(),
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_GRAVADOR,
            ),
        )
        assertFalse(
            AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.precisaAbrirSessaoDoGravador(
                aberto,
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_GRAVADOR,
            ),
        )
    }

    @Test
    fun fecharGravadorMantemAbaEVoltaAoChat() {
        val aberto = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.abrirGravador(
            AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado(
                componentes = listOf(
                    AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Aba("a", "A"),
                ),
            ),
        )
        val fechado = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.fecharGravador(aberto)
        assertFalse(fechado.gravadorAberto)
        assertEquals(AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_CHAT, fechado.idAtiva)
        val ids = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.abasVisiveis(fechado).map { it.id }
        assertEquals(
            listOf(
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_CHAT,
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_GRAVADOR,
                "a",
            ),
            ids,
        )
    }

    @Test
    fun ativarChatComGravadorAbertoMantemSessao() {
        val aberto = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.abrirGravador(
            AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado(),
        )
        val r = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ativar(
            aberto,
            AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_CHAT,
        )
        val estado = (r as AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok).estado
        assertTrue(estado.gravadorAberto)
        assertEquals(AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_CHAT, estado.idAtiva)
    }

    @Test
    fun ancorarComGravadorAbertoPreservaFlag() {
        val aberto = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.abrirGravador(
            AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado(),
        )
        val r = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ancorar(aberto, "c1", "Comp")
        val estado = (r as AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok).estado
        assertTrue(estado.gravadorAberto)
        assertEquals("c1", estado.idAtiva)
        val ids = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.abasVisiveis(estado).map { it.id }
        assertEquals(
            listOf(
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_CHAT,
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_GRAVADOR,
                "c1",
            ),
            ids,
        )
    }

    @Test
    fun ancorarSemAtivarMantemAAbaDoChatNaFrente() {
        val base = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado()
        val r = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ancorar(base, "vis", "Visualizador", ativar = false)
        val estado = (r as AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok).estado
        assertEquals(listOf("vis"), estado.componentes.map { it.id })
        assertEquals(base.idAtiva, estado.idAtiva)
        val de_novo = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ancorar(estado, "vis", "Visualizador", ativar = false)
        assertEquals(base.idAtiva, (de_novo as AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok).estado.idAtiva)
    }
}

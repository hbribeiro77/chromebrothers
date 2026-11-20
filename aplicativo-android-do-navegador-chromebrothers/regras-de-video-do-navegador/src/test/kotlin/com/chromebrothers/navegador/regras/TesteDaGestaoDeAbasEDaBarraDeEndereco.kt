package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaGestaoDeAbasEDaBarraDeEndereco {
    @Test
    fun aPrimeiraAbaAbreAPaginaInicialEFicaAtiva() {
        val gestao = GestaoDeAbasDoNavegadorChromebrothers()
        val aba = gestao.iniciar()
        assertEquals(aba.id, gestao.idDaAbaAtiva)
        assertEquals("Nova aba", aba.titulo)
        assertEquals(EnderecoDaPaginaInicialDoNavegadorChromebrothers.URL, aba.endereco)
        assertEquals(1, gestao.listar().size)
    }

    @Test
    fun novaAbaViraAAtivaEMantemAAnterior() {
        val gestao = GestaoDeAbasDoNavegadorChromebrothers()
        val primeira = gestao.iniciar()
        val segunda = gestao.criarAba()
        assertEquals(segunda.id, gestao.idDaAbaAtiva)
        assertEquals(listOf(primeira.id, segunda.id), gestao.listar().map { it.id })
    }

    @Test
    fun fecharAAbaDoMeioAtivaADaDireita() {
        val gestao = GestaoDeAbasDoNavegadorChromebrothers()
        val esquerda = gestao.iniciar()
        val meio = gestao.criarAba(endereco = "https://exemplo.test/meio")
        val direita = gestao.criarAba(endereco = "https://exemplo.test/direita")
        gestao.ativar(meio.id)
        assertEquals(direita.id, gestao.fechar(meio.id))
        assertEquals(listOf(esquerda.id, direita.id), gestao.listar().map { it.id })
    }

    @Test
    fun fecharAUltimaAbaAbreOutraNoLugar() {
        val gestao = GestaoDeAbasDoNavegadorChromebrothers()
        val unica = gestao.iniciar()
        val novaAtiva = gestao.fechar(unica.id)
        assertTrue(novaAtiva != unica.id)
        assertEquals(1, gestao.listar().size)
        assertEquals(EnderecoDaPaginaInicialDoNavegadorChromebrothers.URL, gestao.ativa()?.endereco)
    }

    @Test
    fun atualizarTituloEEnderecoDaAba() {
        val gestao = GestaoDeAbasDoNavegadorChromebrothers()
        val aba = gestao.iniciar()
        gestao.atualizar(aba.id, titulo = "Google", endereco = "https://www.google.com/search?q=a")
        assertEquals("Google", gestao.ativa()?.titulo)
        assertEquals("https://www.google.com/search?q=a", gestao.ativa()?.endereco)
    }

    @Test
    fun ativarAbaInexistenteNaoMudaAAtiva() {
        val gestao = GestaoDeAbasDoNavegadorChromebrothers()
        val aba = gestao.iniciar()
        assertFalse(gestao.ativar(999))
        assertEquals(aba.id, gestao.idDaAbaAtiva)
    }

    @Test
    fun textoComEspacoViraPesquisaNoGoogle() {
        assertEquals(
            "https://www.google.com/search?q=gatos+laranja",
            InterpretacaoDoTextoDaBarraDeEndereco.resolver("gatos laranja"),
        )
    }

    @Test
    fun dominioGanhaHttps() {
        assertEquals(
            "https://exemplo.test/video",
            InterpretacaoDoTextoDaBarraDeEndereco.resolver("exemplo.test/video"),
        )
    }

    @Test
    fun enderecoCompletoPermanece() {
        assertEquals(
            "http://192.168.0.8/a.mp4",
            InterpretacaoDoTextoDaBarraDeEndereco.resolver("http://192.168.0.8/a.mp4"),
        )
    }

    @Test
    fun textoComEspacoUsaOMotorEscolhido() {
        assertEquals(
            "https://duckduckgo.com/?q=gatos+laranja",
            InterpretacaoDoTextoDaBarraDeEndereco.resolver("gatos laranja", MotorDeBuscaDaBarraDeEndereco.DUCKDUCKGO),
        )
    }
}

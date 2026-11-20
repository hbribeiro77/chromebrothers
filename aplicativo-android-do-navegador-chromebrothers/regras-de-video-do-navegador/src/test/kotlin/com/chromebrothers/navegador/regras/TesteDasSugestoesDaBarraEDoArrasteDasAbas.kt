package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDasSugestoesDaBarraEDoArrasteDasAbas {
    private val visitas = listOf(
        VisitaDePaginaNoNavegador("Gatos laranja", "https://exemplo.test/gatos"),
        VisitaDePaginaNoNavegador("Receitas", "https://exemplo.test/receitas"),
    )

    @Test
    fun textoVazioNaoAbreSugestoes() {
        assertTrue(ListaDeSugestoesDaBarraDeEndereco.montar("  ", visitas).isEmpty())
    }

    @Test
    fun textoQueNaoESiteColocaHistoricoAntesDaBusca() {
        val lista = ListaDeSugestoesDaBarraDeEndereco.montar("gatos", visitas)
        assertEquals(TipoDaSugestaoDaBarraDeEndereco.PAGINA, lista.first().tipo)
        assertEquals("https://exemplo.test/gatos", lista.first().endereco)
        assertEquals(TipoDaSugestaoDaBarraDeEndereco.BUSCA, lista.last().tipo)
        assertEquals("gatos", lista.last().titulo)
        assertEquals("https://www.google.com/search?q=gatos", lista.last().endereco)
    }

    @Test
    fun letraBPriorizaHostQueComecaComBAntesDaBusca() {
        val comBoosty = listOf(
            VisitaDePaginaNoNavegador("Outro", "https://exemplo.test/x"),
            VisitaDePaginaNoNavegador("Boosty", "https://boosty.to/alguem"),
        )
        val lista = ListaDeSugestoesDaBarraDeEndereco.montar("b", comBoosty)
        assertEquals("https://boosty.to/alguem", lista.first().endereco)
        assertEquals(TipoDaSugestaoDaBarraDeEndereco.BUSCA, lista.last().tipo)
        assertTrue(
            ListaDeSugestoesDaBarraDeEndereco.pontuarPagina("b", comBoosty[1]) >
                ListaDeSugestoesDaBarraDeEndereco.pontuarPagina("b", comBoosty[0]),
        )
    }

    @Test
    fun aBuscaUsaOMotorEscolhido() {
        val lista = ListaDeSugestoesDaBarraDeEndereco.montar(
            "gatos",
            visitas,
            MotorDeBuscaDaBarraDeEndereco.DUCKDUCKGO,
        )
        assertEquals("https://duckduckgo.com/?q=gatos", lista.last().endereco)
    }

    @Test
    fun enderecoDeSiteNaoViraBuscaENaoRepeteAMesmaPagina() {
        val lista = ListaDeSugestoesDaBarraDeEndereco.montar("exemplo.test/gatos", visitas)
        assertEquals(TipoDaSugestaoDaBarraDeEndereco.ENDERECO, lista.last().tipo)
        assertEquals("https://exemplo.test/gatos", lista.last().endereco)
        assertTrue(lista.none { it.tipo == TipoDaSugestaoDaBarraDeEndereco.PAGINA })
    }

    @Test
    fun oHistoricoIgnoraAPaginaInicialAtualizaOTituloETemLimite() {
        val comInicial = HistoricoDePaginasVisitadasNoNavegador.lembrar(
            emptyList(),
            "Início",
            EnderecoDaPaginaInicialDoNavegadorChromebrothers.URL,
        )
        assertTrue(comInicial.isEmpty())
        val primeira = HistoricoDePaginasVisitadasNoNavegador.lembrar(
            emptyList(),
            "Gatos",
            "https://exemplo.test/gatos",
        )
        val deNovo = HistoricoDePaginasVisitadasNoNavegador.lembrar(
            primeira,
            "Gatos laranja",
            "https://exemplo.test/gatos",
        )
        assertEquals(1, deNovo.size)
        assertEquals("Gatos laranja", deNovo.first().titulo)
        val cheio = (1..HistoricoDePaginasVisitadasNoNavegador.LIMITE).fold(deNovo) { atual, indice ->
            HistoricoDePaginasVisitadasNoNavegador.lembrar(atual, "Página $indice", "https://exemplo.test/$indice")
        }
        assertEquals(HistoricoDePaginasVisitadasNoNavegador.LIMITE, cheio.size)
        assertEquals("https://exemplo.test/${HistoricoDePaginasVisitadasNoNavegador.LIMITE}", cheio.first().endereco)
    }

    @Test
    fun esquecerTiraSoAPaginaPedida() {
        val ficou = HistoricoDePaginasVisitadasNoNavegador.esquecer(visitas, "https://exemplo.test/gatos")
        assertEquals(listOf(visitas[1]), ficou)
        assertEquals(visitas, HistoricoDePaginasVisitadasNoNavegador.esquecer(visitas, "https://outra.test"))
    }

    @Test
    fun oTextoDoHistoricoVoltaIgual() {
        val texto = TextoDoHistoricoDePaginasVisitadas.gravar(visitas)
        assertEquals(visitas, TextoDoHistoricoDePaginasVisitadas.ler(texto))
    }

    @Test
    fun moverAbaTrocaAOrdemEMantemAAtiva() {
        val gestao = GestaoDeAbasDoNavegadorChromebrothers()
        val primeira = gestao.iniciar()
        val segunda = gestao.criarAba(endereco = "https://exemplo.test/b")
        val terceira = gestao.criarAba(endereco = "https://exemplo.test/c")
        assertTrue(gestao.mover(primeira.id, 2))
        assertEquals(listOf(segunda.id, terceira.id, primeira.id), gestao.listar().map { it.id })
        assertEquals(terceira.id, gestao.idDaAbaAtiva)
    }

    @Test
    fun oDedoPassaDoMeioDaVizinha() {
        val larguras = listOf(200f, 200f, 200f)
        assertEquals(0, DestinoDoArrasteDaAbaNaFaixa.indice(0, 50f, larguras))
        assertEquals(1, DestinoDoArrasteDaAbaNaFaixa.indice(0, 200f, larguras))
        assertEquals(2, DestinoDoArrasteDaAbaNaFaixa.indice(0, 400f, larguras))
        assertEquals(1, DestinoDoArrasteDaAbaNaFaixa.indice(2, -200f, larguras))
        assertEquals(2, DestinoDoArrasteDaAbaNaFaixa.indice(2, 80f, larguras))
    }

    @Test
    fun asVizinhasDeslizamParaAbrirEspaco() {
        assertEquals(-180f, DeslocamentoDoVizinhoNoArrasteDaAba.pixels(1, 0, 2, 180f), 0.01f)
        assertEquals(0f, DeslocamentoDoVizinhoNoArrasteDaAba.pixels(0, 0, 2, 180f), 0.01f)
        assertEquals(180f, DeslocamentoDoVizinhoNoArrasteDaAba.pixels(0, 2, 0, 180f), 0.01f)
        assertEquals(0f, DeslocamentoDoVizinhoNoArrasteDaAba.pixels(3, 0, 1, 180f), 0.01f)
    }

    @Test
    fun oToqueNaBarraMostraAsPaginasRecentes() {
        val recentes = ListaDeSugestoesDaBarraDeEndereco.paginasRecentes(visitas)
        assertEquals(listOf("https://exemplo.test/gatos", "https://exemplo.test/receitas"), recentes.map { it.endereco })
        assertTrue(recentes.all { it.tipo == TipoDaSugestaoDaBarraDeEndereco.PAGINA })
    }

    @Test
    fun aFaixaDeAtalhosComecaPelosFixosENaoRepeteOSite() {
        val faixa = FaixaDeAtalhosDaBarraDeEndereco.montar(
            listOf(AtalhoDaPaginaInicial("UOL", "https://www.uol.com.br/")),
            visitas + VisitaDePaginaNoNavegador("UOL esporte", "https://uol.com.br/esporte"),
        )
        assertEquals("uol.com.br", IconeDaPaginaNaBarraDeEndereco.hospedeiro(faixa.first().endereco))
        assertEquals(listOf("uol.com.br", "exemplo.test"), faixa.map { IconeDaPaginaNaBarraDeEndereco.hospedeiro(it.endereco) })
        assertEquals("U", IconeDaPaginaNaBarraDeEndereco.letra("UOL", faixa.first().endereco))
        assertEquals(
            IconeDaPaginaNaBarraDeEndereco.cor("https://uol.com.br/"),
            IconeDaPaginaNaBarraDeEndereco.cor("https://www.uol.com.br/esporte"),
        )
    }
}

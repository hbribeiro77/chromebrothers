package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoHistoricoESugestoesComoNoChrome {
    private val agora = 1_800_000_000_000L
    private val dia = 24L * 60L * 60L * 1000L

    private fun entrada(
        endereco: String,
        titulo: String = endereco,
        visitas: Int = 1,
        digitadas: Int = 0,
        diasAtras: Int = 0,
    ) = EntradaDoHistoricoDoNavegador(endereco, titulo, visitas, digitadas, agora - diasAtras * dia)

    @Test
    fun registrarVisitaSomaContagensEGuardaTitulo() {
        val primeira = RegrasDoHistoricoCompletoDoNavegador.registrarVisita(null, "https://a.test/", "A", false, agora)
        val segunda = RegrasDoHistoricoCompletoDoNavegador.registrarVisita(primeira, "https://a.test/", "", true, agora + 5)
        assertEquals(2, segunda.visitas)
        assertEquals(1, segunda.digitadas)
        assertEquals("A", segunda.titulo)
        assertEquals(agora + 5, segunda.ultimaVisitaMs)
    }

    @Test
    fun naoGuardaPaginaInicialNemDataUrl() {
        assertTrue(!RegrasDoHistoricoCompletoDoNavegador.deveGuardar(EnderecoDaPaginaInicialDoNavegadorChromebrothers.URL))
        assertTrue(!RegrasDoHistoricoCompletoDoNavegador.deveGuardar("data:text/html,oi"))
        assertTrue(RegrasDoHistoricoCompletoDoNavegador.deveGuardar("https://chatgpt.com/c/123"))
    }

    @Test
    fun podarRemoveVelhasEMantemLimite() {
        val lista = listOf(
            entrada("https://velha.test/", diasAtras = 120),
            entrada("https://nova.test/", diasAtras = 1),
            entrada("https://media.test/", diasAtras = 30),
        )
        val podada = RegrasDoHistoricoCompletoDoNavegador.podar(lista, agora, limite = 1)
        assertEquals(listOf("https://nova.test/"), podada.map { it.endereco })
    }

    @Test
    fun guardaMuitoMaisQue80Paginas() {
        val muitas = (1..500).map { entrada("https://site$it.test/") }
        assertEquals(500, RegrasDoHistoricoCompletoDoNavegador.podar(muitas, agora).size)
    }

    @Test
    fun migraHistoricoAntigoPreservandoOrdem() {
        val antigas = listOf(
            VisitaDePaginaNoNavegador("Novo", "https://novo.test/"),
            VisitaDePaginaNoNavegador("Velho", "https://velho.test/"),
        )
        val migradas = RegrasDoHistoricoCompletoDoNavegador.migrarDoHistoricoAntigo(antigas, agora)
        assertTrue(migradas[0].ultimaVisitaMs > migradas[1].ultimaVisitaMs)
    }

    @Test
    fun siteMaisUsadoVemPrimeiroMesmoComMesmoCasamento() {
        val pouco = entrada("https://youtube-fake.test/", "YouTube falso", visitas = 1, diasAtras = 20)
        val muito = entrada("https://youtube.com/", "YouTube", visitas = 40, digitadas = 10)
        val lista = RanqueamentoDoHistoricoNaBarraDeEndereco.melhores("you", listOf(pouco, muito), agora, 5)
        assertEquals("https://youtube.com/", lista.first().endereco)
    }

    @Test
    fun todosOsTermosPrecisamCasar() {
        val gatos = entrada("https://exemplo.test/gatos", "Gatos laranja")
        assertTrue(RanqueamentoDoHistoricoNaBarraDeEndereco.casamento("gatos laranja", gatos) > 0)
        assertEquals(0, RanqueamentoDoHistoricoNaBarraDeEndereco.casamento("gatos azuis", gatos))
    }

    @Test
    fun letraSoltaNaoCasaNoMeioDaPalavra() {
        val pagina = entrada("https://exemplo.test/", "Abacate")
        assertEquals(0, RanqueamentoDoHistoricoNaBarraDeEndereco.casamento("b", pagina))
    }

    @Test
    fun autocompletaHostDigitadoAntes() {
        val yt = entrada("https://www.youtube.com/watch?v=1", "Vídeo", visitas = 3, digitadas = 1)
        assertEquals("youtube.com", RanqueamentoDoHistoricoNaBarraDeEndereco.autocompletarEmLinha("you", listOf(yt), agora))
        assertEquals("YOUtube.com", RanqueamentoDoHistoricoNaBarraDeEndereco.autocompletarEmLinha("YOU", listOf(yt), agora))
    }

    @Test
    fun naoAutocompletaSiteVistoUmaVezSemDigitar() {
        val uma = entrada("https://raro.test/", visitas = 1)
        assertNull(RanqueamentoDoHistoricoNaBarraDeEndereco.autocompletarEmLinha("ra", listOf(uma), agora))
    }

    @Test
    fun naoAutocompletaComEspacoOuHostCompleto() {
        val yt = entrada("https://youtube.com/", visitas = 5, digitadas = 2)
        assertNull(RanqueamentoDoHistoricoNaBarraDeEndereco.autocompletarEmLinha("you tube", listOf(yt), agora))
        assertNull(RanqueamentoDoHistoricoNaBarraDeEndereco.autocompletarEmLinha("youtube.com", listOf(yt), agora))
    }

    @Test
    fun hostDigitadoInteiroNaoViraUmaPaginaDoSite() {
        // "higgsfield.ai" digitado inteiro abria higgsfield.ai/supercomputer (a página mais visitada).
        val pagina = entrada("https://higgsfield.ai/supercomputer", visitas = 9, digitadas = 0)
        val inicio = entrada("https://higgsfield.ai/", visitas = 3, digitadas = 2)
        assertNull(RanqueamentoDoHistoricoNaBarraDeEndereco.autocompletarEmLinha("higgsfield.ai", listOf(pagina, inicio), agora))
        assertEquals("higgsfield.ai", RanqueamentoDoHistoricoNaBarraDeEndereco.autocompletarEmLinha("higgs", listOf(pagina, inicio), agora))
    }

    @Test
    fun autocompletaCaminhoQuandoJaDigitouABarra() {
        val repo = entrada("https://github.com/foo/bar", visitas = 4)
        assertEquals(
            "github.com/foo/bar",
            RanqueamentoDoHistoricoNaBarraDeEndereco.autocompletarEmLinha("github.com/fo", listOf(repo), agora),
        )
    }

    @Test
    fun leSugestoesNoFormatoOpenSearch() {
        val resposta = """["gat",["gatos","gatos fofos","Gatos","gato é bom"],[],{"x":1}]"""
        val lidas = SugestoesDePesquisaDoMotorDeBusca.ler(resposta, "gat")
        assertEquals(listOf("gatos", "gatos fofos", "gato é bom"), lidas)
    }

    @Test
    fun respostaQuebradaNaoDaErro() {
        assertTrue(SugestoesDePesquisaDoMotorDeBusca.ler("<html>", "x").isEmpty())
        assertTrue(SugestoesDePesquisaDoMotorDeBusca.ler("[\"x\"]", "x").isEmpty())
    }

    @Test
    fun naoConsultaMotorParaUrl() {
        assertTrue(!SugestoesDePesquisaDoMotorDeBusca.deveConsultar("https://a.test"))
        assertTrue(SugestoesDePesquisaDoMotorDeBusca.deveConsultar("gatos"))
    }

    @Test
    fun listaComoChromeTemAcaoPadraoPaginasEPesquisas() {
        val historico = listOf(entrada("https://gatos.test/", "Gatos", visitas = 3))
        val lista = ListaDeSugestoesDaBarraDeEndereco.montarComoChrome(
            digitado = "gat",
            historico = historico,
            pesquisasSugeridas = listOf("gatos", "gato persa"),
            motor = MotorDeBuscaDaBarraDeEndereco.GOOGLE,
            agoraMs = agora,
        )
        assertEquals(TipoDaSugestaoDaBarraDeEndereco.BUSCA, lista[0].tipo)
        assertEquals(TipoDaSugestaoDaBarraDeEndereco.PAGINA, lista[1].tipo)
        assertEquals(TipoDaSugestaoDaBarraDeEndereco.SUGESTAO_DE_BUSCA, lista[2].tipo)
        assertEquals("https://www.google.com/search?q=gato+persa", lista[3].endereco)
    }

    @Test
    fun comAutocompletarAPrimeiraLinhaEOSiteEDepoisAPesquisa() {
        val yt = entrada("https://youtube.com/", "YouTube", visitas = 9, digitadas = 3)
        val lista = ListaDeSugestoesDaBarraDeEndereco.montarComoChrome(
            digitado = "you",
            historico = listOf(yt),
            pesquisasSugeridas = emptyList(),
            motor = MotorDeBuscaDaBarraDeEndereco.GOOGLE,
            agoraMs = agora,
            autocompletado = "youtube.com",
        )
        assertEquals("https://youtube.com/", lista[0].endereco)
        assertEquals(TipoDaSugestaoDaBarraDeEndereco.PAGINA, lista[0].tipo)
        assertEquals(TipoDaSugestaoDaBarraDeEndereco.BUSCA, lista[1].tipo)
        assertEquals(1, lista.count { it.endereco == "https://youtube.com/" })
    }

    @Test
    fun restaurarAbasMantemOrdemEAtiva() {
        val gestao = GestaoDeAbasDoNavegadorChromebrothers()
        val abas = gestao.restaurar(
            listOf("A" to "https://a.test/", "" to "https://b.test/", "C" to ""),
            indiceDaAtiva = 1,
        )
        assertEquals(listOf("https://a.test/", "https://b.test/"), abas.map { it.endereco })
        assertEquals(abas[1].id, gestao.idDaAbaAtiva)
        assertEquals("Nova aba", abas[1].titulo)
        val nova = gestao.criarAba()
        assertTrue(nova.id > abas.last().id)
    }

    @Test
    fun restaurarSemAbasValidasAbreUmaNova() {
        val gestao = GestaoDeAbasDoNavegadorChromebrothers()
        val abas = gestao.restaurar(emptyList(), 0)
        assertEquals(1, abas.size)
        assertEquals(EnderecoDaPaginaInicialDoNavegadorChromebrothers.URL, abas[0].endereco)
    }
}

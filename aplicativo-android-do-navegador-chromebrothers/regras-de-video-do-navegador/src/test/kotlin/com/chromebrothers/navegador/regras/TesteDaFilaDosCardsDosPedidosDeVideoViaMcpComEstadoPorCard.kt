package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.FilaDosCardsDosPedidosDeVideoViaMcpComEstadoPorCard as Fila
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaFilaDosCardsDosPedidosDeVideoViaMcpComEstadoPorCard {

    private var seq = 0
    private fun id() = "c${++seq}"

    @Test
    fun criaUmCardPorVideoDeCadaMcpComNumeracaoSequencial() {
        val cards = Fila.novosCards(
            ultimoNumero = 6,
            quantidade = 2,
            mcps = listOf("topview", "sjinn"),
            modeloDoMcp = { mcp -> if (mcp == "topview") "Wan 3.0" else "wan3" },
            prompt = "chuva",
            agoraMs = 1000L,
            gerarId = ::id,
        )
        assertEquals(listOf(7, 8, 9, 10), cards.map { it.numero })
        assertEquals(listOf("topview", "topview", "sjinn", "sjinn"), cards.map { it.mcp })
        assertEquals("Wan 3.0", cards[0].modelo)
        assertEquals("wan3", cards[2].modelo)
        assertTrue(cards.all { it.status == Fila.STATUS_ENVIANDO && it.criadoEmMs == 1000L })
        assertEquals(4, cards.map { it.id }.distinct().size)
    }

    @Test
    fun verificarSoAparecaComTarefaConhecidaEDemoraOuFalha() {
        val base = card(status = Fila.STATUS_DEMOROU, tarefa = "t1")
        assertTrue(Fila.podeVerificar(base))
        assertTrue(Fila.podeVerificar(base.copy(status = Fila.STATUS_FALHOU)))
        assertFalse(Fila.podeVerificar(base.copy(status = Fila.STATUS_GERANDO)))
        assertFalse(Fila.podeVerificar(base.copy(status = Fila.STATUS_PRONTO)))
        assertFalse(Fila.podeVerificar(base.copy(idDaTarefa = "")))
    }

    @Test
    fun higgsfieldSemTarefaPodeVerificarPeloHistorico() {
        val semTarefa = card(status = Fila.STATUS_FALHOU).copy(mcp = "higgsfield")
        assertTrue(Fila.podeVerificar(semTarefa))
        assertFalse(Fila.podeVerificar(semTarefa.copy(status = Fila.STATUS_ENVIANDO)))
        assertFalse(Fila.podeVerificar(semTarefa.copy(mcp = "sjinn")))
    }

    @Test
    fun erroNaEsperaViraDemorouQuandoEstourouOTempo() {
        assertEquals(Fila.STATUS_DEMOROU, Fila.statusAposErroDaEspera(true, 600_000L, 600_000L))
        assertEquals(Fila.STATUS_FALHOU, Fila.statusAposErroDaEspera(true, 30_000L, 600_000L))
        assertEquals(Fila.STATUS_FALHOU, Fila.statusAposErroDaEspera(false, 600_000L, 600_000L))
    }

    @Test
    fun atualizarSubstituiPeloIdERemoverTira() {
        val a = card(id = "a")
        val b = card(id = "b")
        val lista = Fila.atualizar(listOf(a, b), b.copy(status = Fila.STATUS_GERANDO))
        assertEquals(Fila.STATUS_GERANDO, lista[1].status)
        assertEquals(2, lista.size)
        assertEquals(listOf("a", "b", "c"), Fila.atualizar(lista, card(id = "c")).map { it.id })
        assertEquals(listOf("b"), Fila.remover(lista, "a").map { it.id })
    }

    @Test
    fun guardaTambemOsProntosComItemDaBibliotecaEPreservaOUltimoNumero() {
        val lista = listOf(
            card(id = "a", status = Fila.STATUS_PRONTO).copy(
                idDaReceita = "r1",
                idBiblioteca = "vid1",
                fileUrl = "file:///v.mp4",
                caminhoMiniatura = "/m/v.jpg",
            ),
            card(id = "b", status = Fila.STATUS_DEMOROU, tarefa = "task \"1\"", detalhe = "omni_reference"),
        )
        val json = Fila.paraJson(lista, ultimoNumero = 12)
        val lido = Fila.deJson(json)
        assertEquals(12, lido.ultimoNumero)
        assertEquals(listOf("a", "b"), lido.cards.map { it.id })
        assertEquals(lista[0], lido.cards[0])
        val b = lido.cards[1]
        assertEquals("task \"1\"", b.idDaTarefa)
        assertEquals("omni_reference", b.detalheDaTarefa)
        assertEquals(1234L, b.criadoEmMs)
        assertEquals("prompt\ncom quebra", b.prompt)
    }

    @Test
    fun acimaDoLimiteSaemPrimeiroOsProntosMaisAntigos() {
        val lista = listOf(
            card(id = "p1", status = Fila.STATUS_PRONTO),
            card(id = "f1", status = Fila.STATUS_FALHOU),
            card(id = "p2", status = Fila.STATUS_PRONTO),
            card(id = "p3", status = Fila.STATUS_PRONTO),
            card(id = "g1", status = Fila.STATUS_GERANDO),
        )
        assertEquals(listOf("f1", "p3", "g1"), Fila.aparar(lista, limite = 3).map { it.id })
        assertEquals(listOf("g1"), Fila.aparar(lista, limite = 1).map { it.id })
        assertEquals(lista, Fila.aparar(lista, limite = 10))
    }

    @Test
    fun jsonAntigoSemCamposNovosAindaAbre() {
        val antigo = """{"ultimoNumero":2,"cards":[{"id":"x","numero":2,"criadoEmMs":5,"mcp":"sjinn","modelo":"m","prompt":"p","status":"falhou","idDaTarefa":"","detalheDaTarefa":"","mensagem":"erro"}]}"""
        val c = Fila.deJson(antigo).cards.single()
        assertEquals("", c.idDaReceita)
        assertEquals("", c.fileUrl)
    }

    @Test
    fun jsonDoCardUsaOItemGuardadoNoCardQuandoNaoPassado() {
        val pronto = card(status = Fila.STATUS_PRONTO).copy(idBiblioteca = "img9", fileUrl = "file:///i.jpg", idDaReceita = "r2")
        val json = Fila.jsonDoCard(pronto, previewDataUrl = "data:x")
        assertTrue(json.contains("\"idBiblioteca\":\"img9\""))
        assertTrue(json.contains("\"fileUrl\":\"file:///i.jpg\""))
        assertTrue(json.contains("\"idDaReceita\":\"r2\""))
    }

    @Test
    fun jsonVazioOuQuebradoNaoDerruba() {
        assertEquals(0, Fila.deJson("").ultimoNumero)
        assertTrue(Fila.deJson("lixo").cards.isEmpty())
    }

    @Test
    fun cardEmAndamentoAoReabrirViraDemorouSeTemTarefa() {
        val restaurados = Fila.aoRestaurar(
            listOf(
                card(id = "a", status = Fila.STATUS_GERANDO, tarefa = "t"),
                card(id = "b", status = Fila.STATUS_ENVIANDO),
                card(id = "c", status = Fila.STATUS_FALHOU, tarefa = "t2"),
            ),
        )
        assertEquals(Fila.STATUS_DEMOROU, restaurados[0].status)
        assertEquals(Fila.STATUS_FALHOU, restaurados[1].status)
        assertEquals(Fila.STATUS_FALHOU, restaurados[2].status)
    }

    @Test
    fun jsonDoCardLevaVerificarEItemQuandoPronto() {
        val json = Fila.jsonDoCard(
            card(status = Fila.STATUS_PRONTO, tarefa = "t"),
            idBiblioteca = "vid1",
            fileUrl = "file:///x.mp4",
            previewDataUrl = "data:image/jpeg;base64,AA",
        )
        assertTrue(json.contains("\"numero\":3"))
        assertTrue(json.contains("\"podeVerificar\":false"))
        assertTrue(json.contains("\"fileUrl\":\"file:///x.mp4\""))
        assertTrue(json.contains("\"idBiblioteca\":\"vid1\""))
        assertTrue(Fila.jsonDoCard(card(status = Fila.STATUS_DEMOROU, tarefa = "t")).contains("\"podeVerificar\":true"))
    }

    private fun card(
        id: String = "x",
        status: String = Fila.STATUS_ENVIANDO,
        tarefa: String = "",
        detalhe: String = "",
    ) = CardDoPedidoDeVideoViaMcp(
        id = id,
        numero = 3,
        criadoEmMs = 1234L,
        mcp = "topview",
        modelo = "Wan 3.0",
        prompt = "prompt\ncom quebra",
        status = status,
        idDaTarefa = tarefa,
        detalheDaTarefa = detalhe,
        mensagem = "",
    )
}

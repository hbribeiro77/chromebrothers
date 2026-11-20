package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoVivixW1NaGeracaoDeVideo {
    @Test
    fun vivixEhMcpConhecidoEApareceNaLista() {
        assertTrue(PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_VIVIX in PedidoDeGeracaoDeVideoViaMcpNaColecao.MCPS_CONHECIDOS)
        assertTrue(RespostaJsonDaPonteDeGeracaoDeVideoMcp.okMcps().contains("\"id\":\"vivix\""))
        assertEquals("vivix-w1", PedidoDeGeracaoDeVideoViaMcpNaColecao.modeloPadraoPara("vivix"))
        val lido = PedidoDeGeracaoDeVideoViaMcpNaColecao.ler("""{"mcps":["vivix"],"prompt":"lago","resolucao":"1080p","duracao":8}""")
        assertIs<ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Ok>(lido)
        assertEquals("vivix-w1", lido.pedido.modelo)
    }

    @Test
    fun ajustaResolucaoDuracaoEProporcaoAoQueAApiAceita() {
        assertEquals(PedidoDeVideoDoVivixW1.Ajustado(5, "720p", "16:9"), PedidoDeVideoDoVivixW1.ajustar(2, "480p", "adaptive"))
        assertEquals(PedidoDeVideoDoVivixW1.Ajustado(15, "1088p", "9:16"), PedidoDeVideoDoVivixW1.ajustar(30, "1080p", "9:16"))
        assertEquals(PedidoDeVideoDoVivixW1.Ajustado(8, "720p", "1:1"), PedidoDeVideoDoVivixW1.ajustar(8, "720p", "1:1"))
        assertEquals("1080p", PedidoDeVideoDoVivixW1.rotuloDaResolucao("1088p"))
    }

    @Test
    fun corpoDoJobTemSoCamposConhecidosEUmJob() {
        val corpo = PedidoDeVideoDoVivixW1.corpoDoJob(
            modelo = "Vivix W1 Fast",
            prompt = "salar \"ao\" amanhecer",
            ajustado = PedidoDeVideoDoVivixW1.ajustar(8, "1080p", "16:9"),
            urlsDeImagem = listOf("https://static.vivi-x.ai/a.png"),
            urlsDeVideo = listOf("https://static.vivi-x.ai/b.mp4"),
        )
        assertEquals(
            """{"model":"vivix-w1-fast","pe_mode":"standard","jobs":[{"prompt":"salar \"ao\" amanhecer","duration":8,""" +
                """"aspect_ratio":"16:9","resolution":"1088p","output_format":"mp4",""" +
                """"ref_images":[{"url":"https://static.vivi-x.ai/a.png"}],"ref_videos":[{"url":"https://static.vivi-x.ai/b.mp4"}]}]}""",
            corpo,
        )
        val semRefs = PedidoDeVideoDoVivixW1.corpoDoJob("vivix-w1", "x", PedidoDeVideoDoVivixW1.ajustar(5, "720p", "16:9"))
        assertFalse(semRefs.contains("ref_"))
    }

    @Test
    fun catalogoResolveIdsENomesEOPadraoEhOMaisBarato() {
        assertEquals("vivix-w1", CatalogoDeModelosDeVideoDoVivixNaColecao.idDaApi(""))
        assertEquals("vivix-w1-ultrafast", CatalogoDeModelosDeVideoDoVivixNaColecao.idDaApi("Vivix W1 Ultra Fast"))
        assertEquals("vivix-w1-fast", CatalogoDeModelosDeVideoDoVivixNaColecao.idDaApi("vivix_w1_fast"))
        assertFalse(CatalogoDeModelosDeVideoDoVivixNaColecao.ehModeloVivix("wan3"))
        val json = CatalogoDeModelosDeVideoDoVivixNaColecao.jsonDosItensParaPonte()
        assertTrue(json.startsWith("""{"ok":true,"itens":[{"id":"vivix-w1","""))
        assertTrue(json.contains("créd/s"))
    }

    @Test
    fun aceitaVideoDeReferenciaComLimitesDoVivix() {
        val v = listOf("vivix")
        assertTrue(ReferenciaDeVideoNaGeracaoMcp.aceita("vivix", "vivix-w1-fast"))
        assertNull(ReferenciaDeVideoNaGeracaoMcp.validar(listOf(10_000L, 9_000L), v, "vivix-w1"))
        assertNotNull(ReferenciaDeVideoNaGeracaoMcp.validar(listOf(16_000L), v, "vivix-w1"))
        assertNotNull(ReferenciaDeVideoNaGeracaoMcp.validar(listOf(12_000L, 9_000L), v, "vivix-w1"))
        // Um vídeo de 1 s passa no Vivix (sem mínimo)…
        assertNull(ReferenciaDeVideoNaGeracaoMcp.validar(listOf(1_000L), v, "vivix-w1"))
        assertTrue(ReferenciaDeVideoNaGeracaoMcp.dica(v, "vivix-w1").contains("20 s"))
    }

    @Test
    fun validaTamanhoDosArquivosDeReferencia() {
        assertNull(PedidoDeVideoDoVivixW1.validarVideos(listOf(5L * 1024 * 1024), listOf(5_000L)))
        assertNotNull(PedidoDeVideoDoVivixW1.validarVideos(listOf(21L * 1024 * 1024), listOf(5_000L)))
        assertNotNull(PedidoDeVideoDoVivixW1.validarImagens(listOf(11L * 1024 * 1024)))
        assertNull(PedidoDeVideoDoVivixW1.validarImagens(List(8) { 1024L }))
        assertNotNull(PedidoDeVideoDoVivixW1.validarImagens(List(9) { 1024L }))
    }

    @Test
    fun estimaCreditosComTaxasDeReferencia() {
        assertEquals(8.5, PedidoDeVideoDoVivixW1.creditosEstimados("vivix-w1", 5), 0.001)
        // 6 imagens → 2 blocos de 5 → 10 créditos; 3 s de vídeo → 60.
        assertEquals(8.5 + 10 + 60, PedidoDeVideoDoVivixW1.creditosEstimados("vivix-w1", 5, 6, 3.0), 0.001)
        assertEquals("≈ 9 créditos W", PedidoDeVideoDoVivixW1.rotuloDosCreditos(8.5))
    }

    @Test
    fun interpretaStatusEErrosDaApi() {
        assertEquals(SituacaoDoJobVivix.PRONTO, PedidoDeVideoDoVivixW1.situacaoDoJob("succeeded"))
        assertEquals(SituacaoDoJobVivix.GERANDO, PedidoDeVideoDoVivixW1.situacaoDoJob("processing"))
        assertEquals(SituacaoDoJobVivix.FALHOU, PedidoDeVideoDoVivixW1.situacaoDoJob("failed"))
        assertEquals("A Vivix cancelou a geração.", PedidoDeVideoDoVivixW1.mensagemDaFalhaDoJob("generation_cancelled", "x"))
        assertTrue(PedidoDeVideoDoVivixW1.mensagemDoErro(400, 20004, "duration must be 5-15").endsWith("duration must be 5-15"))
        assertTrue(PedidoDeVideoDoVivixW1.mensagemDoErro(402, 10005).contains("Créditos"))
        assertTrue(PedidoDeVideoDoVivixW1.ehJobId("job_abc123"))
        assertFalse(PedidoDeVideoDoVivixW1.ehJobId("../job"))
    }

    @Test
    fun toolDoChatInterpretaEPerguntaComEstimativa() {
        val r = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.GERAR_VIDEO_VIVIX,
            """{"prompt":"onda","modelo":"vivix-w1-fast","duracao":6,"resolucao":"1080p"}""",
        )
        assertIs<ResultadoDaInterpretacaoDaAcaoDoChat.Pronta>(r)
        val p = assertIs<PedidoDeAcaoConfirmadaDoChat.GerarVideoVivix>(r.pedido)
        assertEquals(PedidoDeAcaoConfirmadaDoChat.GerarVideoVivix("onda", "vivix-w1-fast", 6, "1088p", "16:9"), p)
        val pergunta = AcoesConfirmadasDoChatDaOpenRouter.pergunta(p)
        assertTrue(pergunta.contains("Vivix W1 Fast"))
        assertTrue(pergunta.contains("1080p"))
        assertTrue(pergunta.contains("créditos W"))
        assertTrue(LiberacaoDoVivixParaOChat.ehPedidoVivix(p))
        assertTrue(LiberacaoDoVivixParaOChat.segueSemPerguntar(p, liberado = true))
        assertFalse(LiberacaoDoVivixParaOChat.segueSemPerguntar(p, liberado = false))
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.GERAR_VIDEO_VIVIX in FerramentasDeLeituraDoChatDaOpenRouter.permitidas)
    }

    @Test
    fun toolDoChatRecusaValoresForaDoVivix() {
        fun recusa(args: String) = assertIs<ResultadoDaInterpretacaoDaAcaoDoChat.Recusada>(
            AcoesConfirmadasDoChatDaOpenRouter.interpretar(AcoesConfirmadasDoChatDaOpenRouter.GERAR_VIDEO_VIVIX, args),
        )
        recusa("""{"prompt":""}""")
        recusa("""{"prompt":"x","duracao":30}""")
        recusa("""{"prompt":"x","modelo":"wan3"}""")
        recusa("""{"prompt":"x","proporcao":"21:9"}""")
    }

    @Test
    fun origemVivixNaBiblioteca() {
        assertEquals("vivix", OrigemDaMidiaNaBibliotecaDoNavegador.parse("VIVIX"))
        assertEquals("Vivix", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo("vivix"))
        assertTrue(MidiaGeradaViaMcpListadaNoVisualizadorDeMidiaDaColecao.ehOrigemDoMcp("vivix"))
    }
}

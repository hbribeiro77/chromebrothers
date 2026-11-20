package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoCatalogoEExtracaoTopviewNaColecao {
    @Test
    fun catalogoDefaultWan30NaoPrime() {
        assertEquals("Wan 3.0", CatalogoDeModelosDeVideoDoTopviewNaColecao.displayNameDe("wan-3-0"))
        assertEquals("Wan 3.0", CatalogoDeModelosDeVideoDoTopviewNaColecao.displayNameDe(""))
        assertEquals("Wan 3.0 Prime", CatalogoDeModelosDeVideoDoTopviewNaColecao.displayNameDe("wan-3-0-prime"))
        assertEquals(
            PedidoDeGeracaoDeVideoViaMcpNaColecao.MODELO_PADRAO_DA_COLECAO_TOPVIEW,
            "wan-3-0",
        )
        assertEquals(480, CatalogoDeModelosDeVideoDoTopviewNaColecao.resolucaoInteira("480p"))
        assertEquals(1080, CatalogoDeModelosDeVideoDoTopviewNaColecao.resolucaoInteira("1080p"))
        assertTrue(CatalogoDeModelosDeVideoDoTopviewNaColecao.jsonDosItensParaPonte().contains("wan-3-0"))
    }

    @Test
    fun extraiTaskIdStatusEUrl() {
        val sucesso = """
            {"code":"200","result":{"taskId":"task_abc","status":"success","errorMsg":null,
            "videos":[{"filePath":"https://cdn.example.com/out.mp4","status":"success"}]}}
        """.trimIndent()
        assertEquals("task_abc", ExtracaoDaUrlDoResultadoDaTarefaTopview.taskId(sucesso))
        assertTrue(ExtracaoDaUrlDoResultadoDaTarefaTopview.tarefaConcluida(sucesso))
        assertEquals("https://cdn.example.com/out.mp4", ExtracaoDaUrlDoResultadoDaTarefaTopview.urlDoVideo(sucesso))

        val pendente = """{"result":{"taskId":"t1","status":"running"}}"""
        assertFalse(ExtracaoDaUrlDoResultadoDaTarefaTopview.tarefaConcluida(pendente))
        assertNull(ExtracaoDaUrlDoResultadoDaTarefaTopview.urlDoVideo(pendente))

        val falha = """{"result":{"taskId":"t2","status":"fail","errorMsg":"sem crédito"}}"""
        assertTrue(ExtracaoDaUrlDoResultadoDaTarefaTopview.tarefaFalhou(falha))
        assertEquals("sem crédito", ExtracaoDaUrlDoResultadoDaTarefaTopview.mensagemDeErro(falha))
    }

    @Test
    fun extraiFileIdDoUpload() {
        assertEquals(
            "file_img_001",
            ExtracaoDoFileIdDoUploadTopview.fileId("""{"result":{"fileId":"file_img_001"}}"""),
        )
        assertNull(ExtracaoDoFileIdDoUploadTopview.fileId("""{"ok":true}"""))
    }

    @Test
    fun pedidoAceitaTopviewComDefault() {
        val ok = PedidoDeGeracaoDeVideoViaMcpNaColecao.ler(
            """{"mcp":"topview","prompt":"cidade neon"}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Ok>(ok)
        assertEquals("topview", ok.pedido.mcp)
        assertEquals("wan-3-0", ok.pedido.modelo)
        assertTrue(RespostaJsonDaPonteDeGeracaoDeVideoMcp.okMcps().contains("\"id\":\"topview\""))
    }

    @Test
    fun liberacaoTopview() {
        val video = PedidoDeAcaoConfirmadaDoChat.GerarVideoTopview("onda", "seedance-2-5")
        assertTrue(LiberacaoDoTopviewParaOChat.ehPedidoTopview(video))
        assertFalse(LiberacaoDoTopviewParaOChat.segueSemPerguntar(video, liberado = false))
        assertTrue(LiberacaoDoTopviewParaOChat.segueSemPerguntar(video, liberado = true))
        val higgs = PedidoDeAcaoConfirmadaDoChat.GerarVideoHiggsfield("x", "")
        assertFalse(LiberacaoDoTopviewParaOChat.ehPedidoTopview(higgs))
    }

    @Test
    fun interpretaToolGerarVideoTopview() {
        val r = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.GERAR_VIDEO_TOPVIEW,
            """{"prompt":"chuva na rua","modelo":"Wan 2.7"}""",
        )
        assertIs<ResultadoDaInterpretacaoDaAcaoDoChat.Pronta>(r)
        val p = r.pedido
        assertIs<PedidoDeAcaoConfirmadaDoChat.GerarVideoTopview>(p)
        assertEquals("chuva na rua", p.prompt)
        assertEquals("Wan 2.7", p.modelo)
    }
}

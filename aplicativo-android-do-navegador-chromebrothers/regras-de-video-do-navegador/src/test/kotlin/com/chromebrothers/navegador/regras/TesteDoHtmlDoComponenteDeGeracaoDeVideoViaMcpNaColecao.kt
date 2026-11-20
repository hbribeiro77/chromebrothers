package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoHtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao {
    @Test
    fun idNomeEPonteDeVideo() {
        assertEquals("geracao-mcp-video", HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.ID)
        assertTrue(HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.NOME.contains("vídeo"))
        val h = HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.html()
        assertTrue(h.contains("chromebrothers.listarMcpsGeracaoVideo"))
        assertTrue(h.contains("chromebrothers.listarModelosGeracaoVideo"))
        assertTrue(h.contains("chromebrothers.gerarVideosMcp"))
        assertTrue(h.contains("wan3"))
        assertTrue(h.contains("MODELO_PADRAO_COMUM"))
        assertTrue(h.contains("480p"))
        assertTrue(h.contains("abrirVideoNoPlayer"))
        assertTrue(h.contains("Tela cheia") || h.contains("Reproduzir"))
        assertTrue(h.contains("item-video"))
        assertTrue(h.contains("previewDataUrl"), "poster da miniatura, não <video> WebView")
        assertTrue(h.contains("poster-do-video") || h.contains("img"))
        assertFalse(
            Regex("""adicionarItem[\s\S]*?v\.src\s*=\s*it\.fileUrl""").containsMatchIn(h),
            "não deve usar <video src=fileUrl> (codec Topview quebra na WebView)",
        )
        assertTrue(
            h.contains("btn.disabled = false") || !h.contains("btn.disabled = true"),
            "botão Gerar não deve ficar preso durante a geração",
        )
        assertTrue(h.contains("id=\"mcps\""))
        assertTrue(h.contains("mcpsMarcados"))
        assertTrue(h.contains("Gerações (por MCP)"))
        assertTrue(h.contains("id=\"qtd\""))
        assertTrue(h.contains("id=\"proporcao\""))
        assertFalse(h.contains("id=\"selMcp\""))
        assertFalse(h.contains("so-sjinn"))
    }

    @Test
    fun cadaVideoPedidoViraUmCardComVerificarProprio() {
        val h = HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.html()
        assertTrue(h.contains("chromebrothers.listarCardsDeVideoMcp"), "restaura os cards ao abrir")
        assertTrue(h.contains("chromebrothers.verificarCardDeVideoMcp"), "verificar por card")
        assertTrue(h.contains("chromebrothers.removerCardDeVideoMcp"))
        assertTrue(h.contains("card-skeleton"))
        assertTrue(h.contains("r.card"), "eventos por card vindos do Kotlin")
        assertTrue(h.contains("r.cards"), "cards criados na hora pela resposta do gerar")
        assertTrue(h.contains("podeVerificar"))
        assertTrue(h.contains("criadoEmMs"))
        assertTrue(h.contains("numero"))
        assertFalse(h.contains("btnPendentes"), "sem botão global de pendências")
        assertFalse(h.contains("verificarPendenciasVideoTopview"))
        assertFalse(h.contains("geracoesAtivas"), "sem contador global «Gerando 0 de N»")
    }

    @Test
    fun playNaThumbnailAbreOPlayerAoLadoComNewEMoldura() {
        val h = HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.html()
        assertTrue(h.contains("botao-play"), "botão play sobre a thumbnail")
        assertTrue(h.contains("chromebrothers.abrirVideosAoLado"), "abre no player nativo embutido ao lado")
        assertTrue(h.contains("window.__chromebrothersVideoNoVisor"), "Kotlin avisa qual vídeo está em exibição")
        assertTrue(h.contains("badge-new"))
        assertTrue(h.contains("em-exibicao"), "moldura do vídeo em exibição")
        assertTrue(h.contains("Tela cheia"), "atalho para o player em tela cheia continua")
        assertFalse(h.contains("$"), "HTML vive numa raw string Kotlin")
    }
}

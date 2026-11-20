package com.chromebrothers.navegador.regras

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

class TesteDoHtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao {
    @Test
    fun htmlTemFormularioEChamaPonte() {
        assertEquals("geracao-mcp-imagem", HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.ID)
        val h = HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html()
        assertTrue(h.contains("chromebrothers.listarMcpsGeracao"))
        assertTrue(h.contains("chromebrothers.listarModelosGeracao"))
        assertTrue(h.contains("chromebrothers.escolherImagemGeracao"))
        assertTrue(h.contains("chromebrothers.limparImagemGeracao"))
        assertTrue(h.contains("chromebrothers.gerarImagensMcp"))
        assertTrue(h.contains("__chromebrothersGeracaoMcp"))
        assertTrue(h.contains("__chromebrothersImagemGeracao"))
        assertTrue(h.contains("id=\"status\""))
        assertTrue(h.contains("id=\"grade\""))
        assertTrue(h.length < 200_000)
    }

    @Test
    fun referenciasMultiplasProporcaoResolucaoEModeloPadrao() {
        val h = HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html()
        assertTrue(h.contains("limparImagemGeracaoIndice"))
        assertTrue(h.contains("id=\"refs\""))
        assertTrue(h.contains("LIMITE_REFS = 4"))
        assertTrue(h.contains("nano_banana_2_1"))
        assertTrue(h.contains("selecionarModeloPadrao"))
        assertTrue(h.contains("grade-opcoes"))
        assertTrue(h.contains("id=\"mcps\""))
        assertTrue(h.contains("mcpsMarcados()"))
        assertTrue(h.contains("mcps.join(\",\")"))
        assertTrue(h.contains("id=\"prompt\""))
        assertTrue(h.contains("min-height: 110px"))
        val prop = h.substringAfter("id=\"proporcao\"").substringBefore("</select>")
        assertTrue(prop.contains("1:1"))
        assertTrue(prop.contains("16:9"))
        val res = h.substringAfter("id=\"resolucao\"").substringBefore("</select>")
        assertTrue(res.contains("1k"))
        assertTrue(res.contains("2k"))
        assertTrue(res.contains("4k"))
        val aplicar = h.substringAfter("function aplicarImagem").substringBefore("window.__chromebrothersImagemGeracao")
        assertTrue(aplicar.contains("r.aguardando"))
        assertTrue(aplicar.contains("pintarRefs"))
        val gerar = h.substringAfter("function gerar()").substringBefore("pintarRefs({ itens: [] })")
        assertTrue(gerar.contains("proporcao"))
        assertTrue(gerar.contains("resolucao"))
        assertTrue(gerar.contains("temReferencia: qtdRefs > 0"))
    }

    @Test
    fun resultadoEmCardsComoNoVideo() {
        val h = HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html()
        assertTrue(h.contains("chromebrothers.listarCardsDeImagemMcp"))
        assertTrue(h.contains("chromebrothers.verificarCardDeImagemMcp"))
        assertTrue(h.contains("chromebrothers.removerCardDeImagemMcp"))
        assertTrue(h.contains("card-skeleton"))
        assertTrue(h.contains("badge-new"))
        val gerar = h.substringAfter("function gerar()").substringBefore("pintarRefs({ itens: [] })")
        assertTrue(gerar.contains("r.cards"))
    }

    @Test
    fun metadadosEEstiloSemTemaRoxo() {
        assertTrue(HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.NOME.contains("Geração MCP"))
        assertEquals(
            listOf("mcp", "higgsfield", "sjinn", "imagem"),
            HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.TAGS,
        )
        val h = HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html()
        assertTrue(h.contains("#f6f7f9"))
        assertTrue(h.contains("#1a1a1a"))
        assertFalse(h.contains("purple", ignoreCase = true))
    }

    @Test
    fun quantidadeEhSelectDeUmAQuatroSemInputNumber() {
        val h = HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html()
        assertFalse(h.contains("type=\"number\""))
        assertFalse(h.contains("type='number'"))
        val bloco = h.substringAfter("id=\"qtd\"").substringBefore("</select>")
        assertTrue(bloco.contains("<option value=\"1\""))
        assertTrue(bloco.contains("<option value=\"2\""))
        assertTrue(bloco.contains("<option value=\"3\""))
        assertTrue(bloco.contains("<option value=\"4\""))
    }

    @Test
    fun gradeMostraPreviewEGuardaFileUrlParaArraste() {
        val h = HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html()
        val adicionar = h.substringAfter("function pintarCardPronto").substringBefore("function pintarCardSkeleton")
        // WebView com base https:// não carrega file:// no img.src — preview no src, fileUrl no data-cb-uri.
        val ladoSrc = adicionar.substring(adicionar.indexOf("img.src")).substringBefore(";")
        assertTrue(ladoSrc.contains("previewDataUrl"))
        assertTrue(
            !ladoSrc.contains("fileUrl") ||
                ladoSrc.indexOf("previewDataUrl") < ladoSrc.indexOf("fileUrl"),
            "previewDataUrl deve ser preferido no src (file:// quebra a miniatura na WebView)",
        )
        assertTrue(adicionar.contains("data-cb-uri"))
        assertTrue(adicionar.contains("c.fileUrl"))
    }

    @Test
    fun aoAbrirPintaAsReferenciasGuardadasECardMovidoNaoVoltaComoNovo() {
        for (h in listOf(
            HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html(),
            HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.html(),
        )) {
            val abrir = h.substringAfter("document.addEventListener(\"DOMContentLoaded\"").substringBefore("});")
            assertTrue(abrir.contains("sincronizarRefs()"), "referências guardadas aparecem ao abrir")
            assertTrue(h.contains("chromebrothers.listarReferenciasGeracao()"))
            val pintar = h.substringAfter("function pintarCard(c)").substringBefore("el.innerHTML")
            assertTrue(pintar.contains("vistos[c.fileUrl] = true"), "card movido continua visto")
            assertTrue(h.contains("r.movido"))
        }
    }

    @Test
    fun toqueNaReferenciaAbreNoVisorComMoldura() {
        for (h in listOf(
            HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html(),
            HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.html(),
        )) {
            val pintar = h.substringAfter("function pintarRefs(r)").substringBefore("refsEl.appendChild(wrap)")
            assertTrue(pintar.contains("abrirReferencia(indice)"))
            assertTrue(pintar.contains("urisDasRefs = []"), "refs repintadas zeram os endereços antigos")
            assertTrue(h.contains("chromebrothers.abrirReferenciaNoVisor("))
            assertTrue(h.contains(".ref.em-exibicao"))
            val visor = h.substringAfter("window.__chromebrothersImagemNoVisor = function")
            assertTrue(visor.contains("urisDasRefs[k]"))
        }
    }

    @Test
    fun toqueAbreOVisorDoVisualizadorComAListaEMolduraNaMiniatura() {
        val h = HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html()
        val pronto = h.substringAfter("function pintarCardPronto").substringBefore("function pintarCardSkeleton")
        assertTrue(pronto.contains("abrirNoVisor(c.fileUrl)"))
        val abrir = h.substringAfter("function abrirNoVisor").substringBefore("window.__chromebrothersImagemNoVisor")
        assertTrue(abrir.contains("chromebrothers.abrirImagensNoVisorDaColecao"))
        assertTrue(abrir.contains("imagensProntasDaGrade()"))
        // Sem a ponte nova (app antigo), continua abrindo no visor da gaveta.
        assertTrue(abrir.contains("chromebrothers.abrirImagemNoVisor"))
        assertTrue(h.contains("window.__chromebrothersImagemNoVisor = function"))
        assertTrue(h.contains(".card.em-exibicao"))
    }
}

package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoHtmlDoComponenteDoVisualizadorDeMidiaNaColecao {
    @Test
    fun htmlNativoTemGaleriaECallbackDaPasta() {
        assertEquals("visualizador-de-midia", HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.ID)
        assertEquals("Visualizador de mídia", HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.NOME)
        val h = HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.html()
        assertTrue(h.contains("chromebrothersAoEscolherPasta"))
        assertTrue(h.contains("id=\"grade\""))
        assertTrue(h.contains("id=\"barra\""))
        assertTrue(h.contains("btnSelecionar"))
        assertTrue(h.contains("btnExcluir"))
        assertTrue(h.contains("excluirArquivos"))
        assertTrue(h.contains("cb_thumb="))
        assertTrue(h.contains("data-f=\"imagens\""))
        assertTrue(h.contains("data-f=\"videos\""))
        assertTrue(h.contains("id=\"maiores\""))
        assertTrue(h.contains("chromebrothers://midia"))
        assertTrue(h.contains("cbVizDestacarCarta"))
        assertTrue(h.contains("cbVizAvisarLarguraEstavel"))
        assertTrue(h.contains("aguardarMiniaturasProntas"))
        assertTrue(h.contains("cb-viz-carregando-thumbs"))
        assertTrue(h.contains("cb-viz-aguarda-largura"))
        assertTrue(h.contains("totalEncontrado"))
        assertTrue(
            h.contains("cb_thumb=${HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.LADO_DA_MINIATURA_DA_GRADE}"),
        )
        assertTrue(h.contains("dragstart"))
        assertTrue(h.contains("-webkit-user-drag:none"))
        assertFalse(h.contains("createElement('video')"), "vídeos da grade usam capa em <img>")
        assertFalse(h.contains("grade.innerHTML='';\n    marcados={};\n    var midias"), "grade não é remontada")
        assertTrue(h.contains("carta.em-foco"))
        assertFalse(h.contains("escolherPasta("))
        assertTrue(h.length < 80_000)
    }

    @Test
    fun tagsIncluemWarmStartParaPreCarga() {
        assertTrue(
            HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.TAGS
                .any { it.equals("warm-start", ignoreCase = true) },
        )
        assertTrue(HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.TAGS.contains("galeria"))
        assertTrue(
            WarmStartDeComponentesDaColecaoDoNavegador.marcadoNoComponente(
                ItemDaBibliotecaDeComponentesHtmlDaColecao(
                    id = HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.ID,
                    nome = HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.NOME,
                    html = "<p>x</p>",
                    atualizadoEm = 1L,
                    tags = HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.TAGS,
                ),
            ),
        )
    }
}

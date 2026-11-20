package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoDestinoDoArrasteDoComponenteDaColecaoParaMcpEditorOuPagina {
    private val regra = DestinoDoArrasteDoComponenteDaColecaoParaMcpEditorOuPagina

    @Test
    fun geradorMcpVenceMesmoSobreColecao() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.REFERENCIA_MCP,
            regra.decidir(
                tipoGeradorMcp = "imagem",
                soltouSobreLinhaDeCamada = true,
                soltouSobreMoldura = true,
                soltouSobreJanelaDaColecaoNaoMcp = true,
                editavelProximoNaPagina = true,
                pontoSobrePagina = true,
            ),
        )
    }

    @Test
    fun editorFundeForaDaColecao() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.FUNDIR_EDITOR,
            regra.decidir(
                tipoGeradorMcp = "",
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = true,
                soltouSobreJanelaDaColecaoNaoMcp = false,
                editavelProximoNaPagina = false,
                pontoSobrePagina = true,
            ),
        )
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.FUNDIR_EDITOR,
            regra.decidir(
                tipoGeradorMcp = "",
                soltouSobreLinhaDeCamada = true,
                soltouSobreMoldura = false,
                soltouSobreJanelaDaColecaoNaoMcp = false,
                editavelProximoNaPagina = false,
                pontoSobrePagina = false,
            ),
        )
    }

    @Test
    fun visualizadorOuColecaoSemMcpCancelaMesmoComEditorAtras() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.CANCELAR,
            regra.decidir(
                tipoGeradorMcp = "",
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = true,
                soltouSobreJanelaDaColecaoNaoMcp = true,
                editavelProximoNaPagina = false,
                pontoSobrePagina = false,
            ),
        )
    }

    @Test
    fun paginaEditavelColaQuandoNaoHaMcpNemEditor() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.COLAR_PAGINA,
            regra.decidir(
                tipoGeradorMcp = "",
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = false,
                soltouSobreJanelaDaColecaoNaoMcp = false,
                editavelProximoNaPagina = true,
                pontoSobrePagina = false,
            ),
        )
    }

    @Test
    fun soltarNaPaginaSemVerdeSoCancela() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.CANCELAR,
            regra.decidir(
                tipoGeradorMcp = "",
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = false,
                soltouSobreJanelaDaColecaoNaoMcp = false,
                editavelProximoNaPagina = false,
                pontoSobrePagina = true,
            ),
        )
    }

    @Test
    fun foraDeTudoCancela() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.CANCELAR,
            regra.decidir(
                tipoGeradorMcp = "",
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = false,
                soltouSobreJanelaDaColecaoNaoMcp = false,
                editavelProximoNaPagina = false,
                pontoSobrePagina = false,
            ),
        )
    }

    @Test
    fun verdeQuandoMcpEditorOuPagina() {
        assertTrue(regra.corIndicaPodeSoltar(tipoGeradorMcp = "video", sobreEditor = false, editavelProximoNaPagina = false))
        assertTrue(regra.corIndicaPodeSoltar(tipoGeradorMcp = "", sobreEditor = true, editavelProximoNaPagina = false))
        assertTrue(regra.corIndicaPodeSoltar(tipoGeradorMcp = "", sobreEditor = false, editavelProximoNaPagina = true))
        assertFalse(regra.corIndicaPodeSoltar(tipoGeradorMcp = "", sobreEditor = false, editavelProximoNaPagina = false))
    }

    @Test
    fun visualizadorSoFicaTransparenteSemOEditor() {
        assertTrue(regra.deveLiberarAPaginaSobOVisualizador(editorDeImagemAberto = false))
        assertFalse(regra.deveLiberarAPaginaSobOVisualizador(editorDeImagemAberto = true))
    }

    @Test
    fun zonaDeCancelarEhOVisualizadorComOEditorAberto() {
        assertTrue(regra.estaNaZonaDeCancelar(editorDeImagemAberto = true, tipoGeradorMcp = "", sobreJanelaDaColecao = true))
        assertFalse(regra.estaNaZonaDeCancelar(editorDeImagemAberto = false, tipoGeradorMcp = "", sobreJanelaDaColecao = true))
        assertFalse(regra.estaNaZonaDeCancelar(editorDeImagemAberto = true, tipoGeradorMcp = "imagem", sobreJanelaDaColecao = true))
        assertFalse(regra.estaNaZonaDeCancelar(editorDeImagemAberto = true, tipoGeradorMcp = "", sobreJanelaDaColecao = false))
    }

    @Test
    fun soltarSobreOVisualizadorComOEditorCancelaMesmoEmCimaDaMoldura() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.CANCELAR,
            regra.decidir(
                tipoGeradorMcp = "",
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = true,
                soltouSobreJanelaDaColecaoNaoMcp = true,
                editavelProximoNaPagina = false,
            ),
        )
    }

    @Test
    fun imagemDoSiteSoltaNoVisualizadorSalva() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.SALVAR_NO_VISUALIZADOR,
            regra.decidir(
                tipoGeradorMcp = "",
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = true,
                soltouSobreJanelaDaColecaoNaoMcp = true,
                editavelProximoNaPagina = true,
                veioDaPagina = true,
                soltouSobreVisualizador = true,
            ),
        )
    }

    @Test
    fun imagemDoProprioVisualizadorNaoSeSalvaDeNovo() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.CANCELAR,
            regra.decidir(
                tipoGeradorMcp = "",
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = true,
                soltouSobreJanelaDaColecaoNaoMcp = true,
                editavelProximoNaPagina = false,
                veioDaPagina = false,
                soltouSobreVisualizador = true,
            ),
        )
    }

    @Test
    fun geradorMcpVenceOSalvarNoVisualizador() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.REFERENCIA_MCP,
            regra.decidir(
                tipoGeradorMcp = "imagem",
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = false,
                soltouSobreJanelaDaColecaoNaoMcp = false,
                editavelProximoNaPagina = false,
                veioDaPagina = true,
                soltouSobreVisualizador = true,
            ),
        )
    }

    @Test
    fun midiaGeradaSoltaNoVisualizadorMove() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.MOVER_PARA_O_VISUALIZADOR,
            regra.decidir(
                tipoGeradorMcp = "",
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = true,
                soltouSobreJanelaDaColecaoNaoMcp = true,
                editavelProximoNaPagina = false,
                veioDaPagina = false,
                soltouSobreVisualizador = true,
                midiaGeradaPodeMover = true,
            ),
        )
    }

    @Test
    fun midiaGeradaForaDoVisualizadorNaoMove() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.CANCELAR,
            regra.decidir(
                tipoGeradorMcp = "",
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = false,
                soltouSobreJanelaDaColecaoNaoMcp = false,
                editavelProximoNaPagina = false,
                veioDaPagina = false,
                soltouSobreVisualizador = false,
                midiaGeradaPodeMover = true,
            ),
        )
        assertTrue(!regra.deveMoverParaOVisualizador(veioDaPagina = true, soltouSobreVisualizador = true, midiaGeradaPodeMover = true))
    }

    @Test
    fun geradorMcpVenceOMoverParaOVisualizador() {
        assertEquals(
            DestinoDoSoltarDoArrasteDoComponenteDaColecao.REFERENCIA_MCP,
            regra.decidir(
                tipoGeradorMcp = "video",
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = false,
                soltouSobreJanelaDaColecaoNaoMcp = false,
                editavelProximoNaPagina = false,
                soltouSobreVisualizador = true,
                midiaGeradaPodeMover = true,
            ),
        )
    }

    @Test
    fun salvarNoVisualizadorPintaOMaisDeVerde() {
        assertTrue(
            regra.corIndicaPodeSoltar(
                tipoGeradorMcp = "",
                sobreEditor = false,
                editavelProximoNaPagina = false,
                salvarNoVisualizador = true,
            ),
        )
    }
}

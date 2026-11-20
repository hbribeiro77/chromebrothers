package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaReferenciaMcp.Acao
import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoDestinoDoArrasteDaBibliotecaParaReferenciaMcp {
    private fun decidir(tipoArraste: String, mime: String, componente: String) =
        DestinoDoArrasteDaBibliotecaParaReferenciaMcp.decidir(tipoArraste, mime, componente)

    @Test
    fun imagemNoGeradorDeImagemUsaOArquivo() {
        assertEquals(Acao.USAR_ARQUIVO, decidir("image", "image/jpeg", "imagem"))
    }

    @Test
    fun imagemNoGeradorDeVideoTambemUsaOArquivo() {
        assertEquals(Acao.USAR_ARQUIVO, decidir("image", "", "video"))
    }

    @Test
    fun imagemIdentificadaSoPeloMimeUsaOArquivo() {
        assertEquals(Acao.USAR_ARQUIVO, decidir("", "image/png", "imagem"))
    }

    @Test
    fun videoUsaUmFrameNosDoisGeradores() {
        assertEquals(Acao.USAR_FRAME, decidir("video", "video/mp4", "imagem"))
        assertEquals(Acao.USAR_FRAME, decidir("video", "", "video"))
        assertEquals(Acao.USAR_FRAME, decidir("", "video/mp4", "video"))
    }

    @Test
    fun componenteVazioOuDesconhecidoRecusa() {
        assertEquals(Acao.RECUSAR, decidir("image", "image/jpeg", ""))
        assertEquals(Acao.RECUSAR, decidir("image", "image/jpeg", "   "))
        assertEquals(Acao.RECUSAR, decidir("video", "video/mp4", "audio"))
    }

    @Test
    fun tipoDesconhecidoRecusa() {
        assertEquals(Acao.RECUSAR, decidir("", "", "imagem"))
        assertEquals(Acao.RECUSAR, decidir("pdf", "application/pdf", "video"))
    }

    @Test
    fun mapeiaIdDoComponenteParaTipo() {
        val regra = DestinoDoArrasteDaBibliotecaParaReferenciaMcp
        assertEquals("imagem", regra.tipoDoComponentePeloId("geracao-mcp-imagem"))
        assertEquals("video", regra.tipoDoComponentePeloId("geracao-mcp-video"))
        assertEquals("", regra.tipoDoComponentePeloId("outro"))
        assertEquals("", regra.tipoDoComponentePeloId(null))
    }

    @Test
    fun ignoraMaiusculasEEspacos() {
        assertEquals(Acao.USAR_ARQUIVO, decidir(" IMAGE ", "", " Imagem "))
        assertEquals(Acao.USAR_FRAME, decidir("", "VIDEO/MP4", "VIDEO"))
    }
}

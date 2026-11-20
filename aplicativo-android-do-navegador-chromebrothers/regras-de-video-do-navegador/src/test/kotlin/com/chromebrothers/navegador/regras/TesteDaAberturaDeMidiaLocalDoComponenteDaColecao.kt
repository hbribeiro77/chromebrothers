package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaAberturaDeMidiaLocalDoComponenteDaColecao {
    @Test
    fun aceitaEsquemasLocaisERemotos() {
        assertTrue(AberturaDeMidiaLocalDoComponenteDaColecao.esquemaAceito("content://doc/foto.jpg"))
        assertTrue(AberturaDeMidiaLocalDoComponenteDaColecao.esquemaAceito("file:///data/foto.jpg"))
        assertTrue(AberturaDeMidiaLocalDoComponenteDaColecao.esquemaAceito("https://exemplo.com/a.png"))
        assertTrue(AberturaDeMidiaLocalDoComponenteDaColecao.esquemaAceito("http://exemplo.com/a.mp4"))
        assertFalse(AberturaDeMidiaLocalDoComponenteDaColecao.esquemaAceito("javascript:alert(1)"))
        assertFalse(AberturaDeMidiaLocalDoComponenteDaColecao.esquemaAceito(""))
        assertFalse(AberturaDeMidiaLocalDoComponenteDaColecao.esquemaAceito("ftp://x/a"))
    }

    @Test
    fun contentPrecisaDeCopiaParaAba() {
        assertTrue(
            AberturaDeMidiaLocalDoComponenteDaColecao.precisaCopiarParaArquivoLocal(
                "content://com.android.providers.downloads.documents/document/42",
            ),
        )
        assertFalse(
            AberturaDeMidiaLocalDoComponenteDaColecao.precisaCopiarParaArquivoLocal(
                "file:///data/user/0/app/cache/foto.jpg",
            ),
        )
        assertFalse(
            AberturaDeMidiaLocalDoComponenteDaColecao.precisaCopiarParaArquivoLocal(
                "https://exemplo.com/foto.jpg",
            ),
        )
    }

    @Test
    fun mensagensOkEErro() {
        val ok = AberturaDeMidiaLocalDoComponenteDaColecao.mensagemOk("visor", "Abri no visor.")
        assertTrue(ok.contains("\"ok\":true"))
        assertTrue(ok.contains("\"destino\":\"visor\""))
        assertTrue(ok.contains("Abri no visor."))
        val erro = AberturaDeMidiaLocalDoComponenteDaColecao.mensagemErro("Uri vazia.")
        assertTrue(erro.contains("\"ok\":false"))
        assertTrue(erro.contains("Uri vazia."))
        assertFalse(erro.contains("\"ok\":true"))
    }

    @Test
    fun destinosConhecidos() {
        assertEquals("aba", AberturaDeMidiaLocalDoComponenteDaColecao.Destino.ABA.rotulo)
        assertEquals("visor", AberturaDeMidiaLocalDoComponenteDaColecao.Destino.VISOR.rotulo)
        assertEquals("editor", AberturaDeMidiaLocalDoComponenteDaColecao.Destino.EDITOR.rotulo)
        assertEquals("biblioteca", AberturaDeMidiaLocalDoComponenteDaColecao.Destino.BIBLIOTECA.rotulo)
        assertEquals("player", AberturaDeMidiaLocalDoComponenteDaColecao.Destino.PLAYER.rotulo)
        assertEquals("copiar", AberturaDeMidiaLocalDoComponenteDaColecao.Destino.COPIAR.rotulo)
    }

    @Test
    fun instrucaoDocumentaAberturaNativaDeMidia() {
        val texto = ManualDoAgenteDoChat.texto("""{"topico":"componentes"}""")  // a ponte chromebrothers.* agora vive no manual sob demanda
        assertTrue(texto.contains("chromebrothers.abrirImagemNaAba"))
        assertTrue(texto.contains("chromebrothers.abrirImagemNoVisor"))
        assertTrue(texto.contains("chromebrothers.abrirImagemNoEditor"))
        assertTrue(texto.contains("chromebrothers.guardarImagem"))
        assertTrue(texto.contains("chromebrothers.copiarImagem"))
        assertTrue(texto.contains("chromebrothers.abrirVideoNoPlayer"))
    }
}

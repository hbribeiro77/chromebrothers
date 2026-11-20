package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina {
    @Test
    fun classificaAsFontesUsadasNoMenuDaImagem() {
        assertEquals(
            FonteDoEnderecoDaImagemDoToqueLongo.DATA,
            ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.classificar(
                "data:image/png;base64,abc",
            ),
        )
        assertEquals(
            FonteDoEnderecoDaImagemDoToqueLongo.BLOB,
            ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.classificar(
                "blob:https://exemplo.test/1",
            ),
        )
        assertEquals(
            FonteDoEnderecoDaImagemDoToqueLongo.HTTP,
            ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.classificar(
                "https://exemplo.test/foto.png",
            ),
        )
        assertEquals(
            FonteDoEnderecoDaImagemDoToqueLongo.ARQUIVO_LOCAL,
            ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.classificar(
                "file:///data/user/0/app/cache/capturas-nativas-de-tela/captura-1.png",
            ),
        )
        assertEquals(
            FonteDoEnderecoDaImagemDoToqueLongo.INSUPORTADO,
            ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.classificar("about:blank"),
        )
    }

    @Test
    fun extraiOCaminhoDoArquivoLocalDaCaptura() {
        assertEquals(
            "/data/user/0/app/cache/capturas-nativas-de-tela/captura-1.png",
            ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.caminhoDoArquivoLocal(
                "file:///data/user/0/app/cache/capturas-nativas-de-tela/captura-1.png",
            ),
        )
        assertEquals(
            "/data/user/0/app/cache/capturas com espaço.png",
            ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.caminhoDoArquivoLocal(
                "file:///data/user/0/app/cache/capturas%20com%20espa%C3%A7o.png",
            ),
        )
        assertNull(
            ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.caminhoDoArquivoLocal(
                "https://exemplo.test/foto.png",
            ),
        )
    }

    @Test
    fun colecaoAceitaHttpEArquivoLocalDeImagem() {
        assertTrue(
            ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.podeColocarNaColecaoComoImagem(
                "https://exemplo.test/foto.png",
            ),
        )
        assertTrue(
            ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.podeColocarNaColecaoComoImagem(
                "file:///data/user/0/app/cache/capturas-nativas-de-tela/captura-1.png",
            ),
        )
        assertFalse(
            ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.podeColocarNaColecaoComoImagem(
                "blob:https://exemplo.test/1",
            ),
        )
        assertFalse(
            ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.podeColocarNaColecaoComoImagem(
                "javascript:void(0)",
            ),
        )
    }
}

package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaMensagemDoComponenteParaOChatDaOpenRouter {
    @Test
    fun limpaVazioETruncaNoLimite() {
        assertNull(MensagemDoComponenteParaOChat.limpar("   "))
        assertEquals("oi", MensagemDoComponenteParaOChat.limpar("  oi  "))
        val gigante = "a".repeat(MensagemDoComponenteParaOChat.LIMITE + 10)
        val limpo = MensagemDoComponenteParaOChat.limpar(gigante)!!
        assertEquals(MensagemDoComponenteParaOChat.LIMITE, limpo.length)
    }

    @Test
    fun instrucaoDocumentaEnviarAoChat() {
        val texto = ManualDoAgenteDoChat.texto("""{"topico":"componentes"}""")  // a ponte chromebrothers.* agora vive no manual sob demanda
        assertTrue(texto.contains("chromebrothers.enviarAoChat"))
    }

    @Test
    fun instrucaoDocumentaUrlDaAbaAtiva() {
        val texto = ManualDoAgenteDoChat.texto("""{"topico":"componentes"}""")  // a ponte chromebrothers.* agora vive no manual sob demanda
        assertTrue(texto.contains("chromebrothers.urlDaAbaAtiva"))
    }

    @Test
    fun instrucaoDocumentaContextoEAcoesDaPonteDoComponente() {
        val texto = ManualDoAgenteDoChat.texto("""{"topico":"componentes"}""")  // a ponte chromebrothers.* agora vive no manual sob demanda
        assertTrue(texto.contains("chromebrothers.tituloDaAbaAtiva"))
        assertTrue(texto.contains("chromebrothers.textoSelecionado"))
        assertTrue(texto.contains("chromebrothers.abrirNaAba"))
        assertTrue(texto.contains("chromebrothers.copiar"))
        assertTrue(texto.contains("chromebrothers.toast"))
    }

    @Test
    fun instrucaoDocumentaSeletorDeArquivoDoComponente() {
        val texto = ManualDoAgenteDoChat.texto("""{"topico":"componentes"}""")  // a ponte chromebrothers.* agora vive no manual sob demanda
        assertTrue(texto.contains("input type=\"file\"") || texto.contains("input type='file'"))
        assertTrue(texto.contains("seletor") || texto.contains("Downloads") || texto.contains("aparelho"))
    }
}

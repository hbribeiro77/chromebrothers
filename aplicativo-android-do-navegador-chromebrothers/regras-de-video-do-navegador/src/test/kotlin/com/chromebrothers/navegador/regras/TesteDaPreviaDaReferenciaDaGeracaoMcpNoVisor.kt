package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TesteDaPreviaDaReferenciaDaGeracaoMcpNoVisor {
    private val regra = PreviaDaReferenciaDaGeracaoMcpNoVisor

    @Test
    fun nomeTemPrefixoDoGeradorEMudaACadaGravacao() {
        val a = regra.nomeDoArquivo("imagem", 0, 1L, "png")
        val b = regra.nomeDoArquivo("imagem", 0, 2L, "png")
        assertTrue(a.startsWith(regra.prefixo("imagem")))
        assertTrue(a.endsWith(".png"))
        assertNotEquals(a, b, "endereço novo a cada abertura (cache de miniatura é por endereço)")
        assertFalse(regra.nomeDoArquivo("video", 0, 1L, "jpg").startsWith(regra.prefixo("imagem")))
    }

    @Test
    fun extensaoSegueOMime() {
        assertEquals("png", regra.extensaoDoMime("image/png"))
        assertEquals("webp", regra.extensaoDoMime("image/webp"))
        assertEquals("jpg", regra.extensaoDoMime("image/jpeg"))
    }

    @Test
    fun reconhecePreviaPelaPasta() {
        assertTrue(regra.ehPrevia("file:///data/user/0/app/cache/previas-de-referencia/ref-imagem-0-1.jpg"))
        assertFalse(regra.ehPrevia("file:///data/user/0/app/files/imagens-mcp/a.jpg"))
        assertFalse(regra.ehPrevia("content://media/external/images/media/12"))
    }
}

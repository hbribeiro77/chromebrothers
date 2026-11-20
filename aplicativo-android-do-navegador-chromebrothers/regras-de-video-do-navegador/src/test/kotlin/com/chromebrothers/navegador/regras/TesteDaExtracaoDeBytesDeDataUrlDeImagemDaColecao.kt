package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaExtracaoDeBytesDeDataUrlDeImagemDaColecao {
    @Test
    fun decodificaJpegBase64() {
        // bytes ASCII "ABC"
        val dataUrl = "data:image/jpeg;base64,QUJD"
        val bytes = ExtracaoDeBytesDeDataUrlDeImagemDaColecao.bytes(dataUrl)
        assertContentEquals(byteArrayOf(0x41, 0x42, 0x43), bytes)
        assertEquals("jpg", ExtracaoDeBytesDeDataUrlDeImagemDaColecao.extensao(dataUrl))
    }

    @Test
    fun rejeitaDataUrlInvalida() {
        assertNull(ExtracaoDeBytesDeDataUrlDeImagemDaColecao.bytes(""))
        assertNull(ExtracaoDeBytesDeDataUrlDeImagemDaColecao.bytes("https://x/a.png"))
        assertNull(ExtracaoDeBytesDeDataUrlDeImagemDaColecao.bytes("data:text/plain;base64,QUJD"))
    }

    @Test
    fun extensaoPng() {
        assertEquals("png", ExtracaoDeBytesDeDataUrlDeImagemDaColecao.extensao("data:image/png;base64,xx"))
        assertTrue(ExtracaoDeBytesDeDataUrlDeImagemDaColecao.extensao("data:image/jpeg;base64,xx") == "jpg")
    }
}

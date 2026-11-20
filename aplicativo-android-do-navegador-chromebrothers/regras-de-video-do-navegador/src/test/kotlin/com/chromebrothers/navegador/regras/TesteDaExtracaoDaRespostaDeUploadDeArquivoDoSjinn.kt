package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaExtracaoDaRespostaDeUploadDeArquivoDoSjinn {
    @Test
    fun leSignedUrlECdnUrlDoPresign() {
        val bruto = """
            {"success":true,"errorMsg":"","data":{
              "signed_url":"https://storage.example/put?sig=1",
              "file_name":"abc.jpg",
              "cdn_url":"https://cdn.sjinn.ai/uploads/abc.jpg"
            }}
        """.trimIndent()
        assertTrue(ExtracaoDaRespostaDeUploadDeArquivoDoSjinn.sucesso(bruto))
        assertEquals(
            "https://storage.example/put?sig=1",
            ExtracaoDaRespostaDeUploadDeArquivoDoSjinn.signedUrl(bruto),
        )
        assertEquals(
            "https://cdn.sjinn.ai/uploads/abc.jpg",
            ExtracaoDaRespostaDeUploadDeArquivoDoSjinn.cdnUrl(bruto),
        )
        assertEquals("abc.jpg", ExtracaoDaRespostaDeUploadDeArquivoDoSjinn.fileName(bruto))
    }
}

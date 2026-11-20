package com.chromebrothers.navegador.regras

import javax.crypto.Cipher
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoDownloadDeHlsCifradoEDaGaleria {
    private val hls = LeituraDePlaylistHlsParaDownload

    @Test
    fun aes128NaoEhDrmEGanhaChavePorTrecho() {
        val texto = """
            #EXTM3U
            #EXT-X-MEDIA-SEQUENCE:7
            #EXT-X-KEY:METHOD=AES-128,URI="chave.bin"
            #EXTINF:4.0,
            seg-7.ts
            #EXTINF:4.0,
            seg-8.ts
            #EXT-X-KEY:METHOD=AES-128,URI="https://k.exemplo/k2",IV=0x1F
            #EXTINF:4.0,
            seg-9.ts
            #EXT-X-KEY:METHOD=NONE
            #EXTINF:4.0,
            seg-10.ts
        """.trimIndent()
        val lida = hls.ler(texto, "https://cdn.exemplo/v/media.m3u8")
        assertFalse(lida.protegida)
        assertTrue(lida.cifrada)
        assertEquals(4, lida.chaves.size)
        assertEquals(ChaveDeTrechoHls("https://cdn.exemplo/v/chave.bin", "%032x".format(7)), lida.chaves[0])
        assertEquals(ChaveDeTrechoHls("https://cdn.exemplo/v/chave.bin", "%032x".format(8)), lida.chaves[1])
        assertEquals(ChaveDeTrechoHls("https://k.exemplo/k2", "0000000000000000000000000000001f"), lida.chaves[2])
        assertNull(lida.chaves[3])
    }

    @Test
    fun sampleAesEWidevineContinuamProtegidos() {
        val sample = hls.ler("#EXTM3U\n#EXT-X-KEY:METHOD=SAMPLE-AES,URI=\"skd://x\"\n#EXTINF:4,\na.ts", "https://c/v.m3u8")
        assertTrue(sample.protegida)
        val widevine = hls.ler(
            "#EXTM3U\n#EXT-X-KEY:METHOD=AES-128,URI=\"data:x\",KEYFORMAT=\"urn:uuid:edef8ba9\"\n#EXTINF:4,\na.ts",
            "https://c/v.m3u8",
        )
        assertTrue(widevine.protegida)
    }

    @Test
    fun semChaveNaoFicaCifrada() {
        val lida = hls.ler("#EXTM3U\n#EXTINF:4,\na.ts\n#EXTINF:4,\nb.ts", "https://c/v.m3u8")
        assertFalse(lida.cifrada)
        assertTrue(lida.chaves.isEmpty())
    }

    @Test
    fun ivHexCompletaEValida() {
        assertEquals("0000000000000000000000000000abcd", hls.ivHex("0xABCD"))
        assertNull(hls.ivHex("0xZZ"))
        assertNull(hls.ivHex(null))
    }

    @Test
    fun decifraOQueOAes128Cifrou() {
        val chave = ByteArray(16) { it.toByte() }
        val iv = hls.ivDaSequencia(42)
        val original = "trecho de vídeo de teste com mais de dezesseis bytes".toByteArray()
        val cifrado = DecifragemDeTrechoHlsAes128.cifra(chave, iv, Cipher.ENCRYPT_MODE).doFinal(original)
        val decifrado = DecifragemDeTrechoHlsAes128.decifrando(cifrado.inputStream(), chave, iv).readBytes()
        assertContentEquals(original, decifrado)
    }

    @Test
    fun mensagensClarasPorCodigo() {
        assertTrue(MensagensDeFalhaDoDownloadDeMidia.doCodigo(403).contains("login"))
        assertTrue(MensagensDeFalhaDoDownloadDeMidia.doCodigo(410).contains("expirou"))
        assertTrue(MensagensDeFalhaDoDownloadDeMidia.ehPaginaEmVezDeMidia("text/html; charset=utf-8"))
        assertFalse(MensagensDeFalhaDoDownloadDeMidia.ehPaginaEmVezDeMidia("video/mp4"))
    }

    @Test
    fun galeriaSoParaVideo() {
        assertTrue(PastaDoDownloadDeMidia.vaiParaAGaleria(DestinoDoDownloadDeMidia.GALERIA, "video/mp4"))
        assertFalse(PastaDoDownloadDeMidia.vaiParaAGaleria(DestinoDoDownloadDeMidia.GALERIA, "audio/mpeg"))
        assertFalse(PastaDoDownloadDeMidia.vaiParaAGaleria(DestinoDoDownloadDeMidia.DOWNLOADS, "video/mp4"))
        assertEquals(DestinoDoDownloadDeMidia.DOWNLOADS, DestinoDoDownloadDeMidia.peloNome("lixo"))
    }

    @Test
    fun melhorPulaAudioSozinho() {
        val audio = PedidoDeMidiaObservado(
            endereco = "https://c/audio/a.m4a",
            tipo = TipoDePedidoDeMidiaParaDownload.ARQUIVO,
            tamanho = 9_000_000,
            ordem = 2,
        )
        val video = PedidoDeMidiaObservado(
            endereco = "https://c/video/v.m3u8",
            tipo = TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS,
            tamanho = -1,
            ordem = 1,
        )
        assertEquals(video, EscolhaDoPedidoDeMidiaParaBaixar.melhor(listOf(audio, video)))
        assertNull(EscolhaDoPedidoDeMidiaParaBaixar.melhor(listOf(audio)))
    }

    @Test
    fun arquivoInteiroVemAntesDaPlaylistNoEnvioAutomatico() {
        val playlist = PedidoDeMidiaObservado("https://c/video/master.m3u8", TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS, ordem = 3)
        val mp4Grande = PedidoDeMidiaObservado("https://c/video/full.mp4", TipoDePedidoDeMidiaParaDownload.ARQUIVO, ordem = 1, tamanho = 80_000_000)
        val mp4Medio = PedidoDeMidiaObservado("https://c/video/sd.mp4", TipoDePedidoDeMidiaParaDownload.ARQUIVO, ordem = 2, tamanho = 20_000_000)
        val candidatos = EscolhaDoPedidoDeMidiaParaBaixar.candidatosParaAGaleria(listOf(playlist, mp4Medio, mp4Grande))
        assertEquals(listOf(mp4Grande, mp4Medio, playlist), candidatos)
        assertEquals(mp4Grande, EscolhaDoPedidoDeMidiaParaBaixar.melhor(listOf(playlist, mp4Medio, mp4Grande)))
    }

    @Test
    fun enderecoDoVkPerdeAFaixaParaBaixarInteiro() {
        val comFaixa = "https://ok6-3.vkuser.net/?expires=1&type=5&sig=x&ct=11&id=9&bytes=0-524287"
        assertEquals("https://ok6-3.vkuser.net/?expires=1&type=5&sig=x&ct=11&id=9", NormalizacaoDoEnderecoDeMidia.semFaixaNaQuery(comFaixa))
        val semFaixa = "https://cdn.x/v.mp4?token=abc"
        assertEquals(semFaixa, NormalizacaoDoEnderecoDeMidia.semFaixaNaQuery(semFaixa))
        // "bytes" que não é faixa fica.
        assertEquals("https://c/x?bytes=abc", NormalizacaoDoEnderecoDeMidia.semFaixaNaQuery("https://c/x?bytes=abc"))
    }

    @Test
    fun videoEAudioSeparadosPeloMime() {
        val video = PedidoDeMidiaObservado("https://ok.vk/?ct=11&type=5", TipoDePedidoDeMidiaParaDownload.ARQUIVO, 1, 16_000_000, "video/mp4")
        val audioBom = PedidoDeMidiaObservado("https://ok.vk/?ct=12&type=2", TipoDePedidoDeMidiaParaDownload.ARQUIVO, 2, 900_000, "audio/mp4")
        val audioRuim = PedidoDeMidiaObservado("https://ok.vk/?ct=12&type=0", TipoDePedidoDeMidiaParaDownload.ARQUIVO, 3, 300_000, "audio/mp4")
        val todos = listOf(audioRuim, video, audioBom)
        assertEquals(video, EscolhaDoPedidoDeMidiaParaBaixar.melhor(todos))
        assertEquals(audioBom, EscolhaDoPedidoDeMidiaParaBaixar.melhorAudio(todos))
        assertNull(EscolhaDoPedidoDeMidiaParaBaixar.melhorAudio(listOf(video)))
    }
}

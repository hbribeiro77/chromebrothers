package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDosDetalhesDaMidiaParaBaixar {
    @Test
    fun hlsMestreDaAMaiorResolucao() {
        val texto = """
            #EXTM3U
            #EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=640x360
            360.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=5000000,RESOLUTION=1920x1080
            1080.m3u8
        """.trimIndent()
        val d = LeituraDosDetalhesDoManifestoDeMidia.hls(texto)
        assertEquals(1080, d.linhas)
        assertTrue(d.variasQualidades)
    }

    @Test
    fun hlsDeTrechosSomaADuracao() {
        val d = LeituraDosDetalhesDoManifestoDeMidia.hls("#EXTM3U\n#EXTINF:10.0,\na.ts\n#EXTINF:5.5,\nb.ts")
        assertEquals(15_500, d.duracaoMs)
    }

    @Test
    fun dashLeResolucaoEDuracao() {
        val texto = """
            <MPD mediaPresentationDuration="PT1M35.2S">
              <Representation id="1" width="1280" height="720" bandwidth="1"/>
              <Representation id="2" width="1920" height="1080" bandwidth="2"/>
              <Representation id="a" bandwidth="3"/>
            </MPD>
        """.trimIndent()
        val d = LeituraDosDetalhesDoManifestoDeMidia.dash(texto)
        assertEquals(1080, d.linhas)
        assertEquals(95_200, d.duracaoMs)
    }

    @Test
    fun textoDaOpcaoMostraResolucaoTempoETamanho() {
        val video = PedidoDeMidiaObservado("https://ok.vk/?ct=11", TipoDePedidoDeMidiaParaDownload.ARQUIVO, 1, 16_547_463, "video/mp4")
        val texto = TextoDaOpcaoDeMidiaParaBaixar.texto(
            video,
            DetalhesDaMidiaParaBaixar(largura = 1920, altura = 1080, duracaoMs = 155_000, temAudio = false),
            temAudioSeparado = true,
        )
        assertEquals("1080p · 2:35 · 15,8 MB · som vem à parte (o app junta)", texto)
        val audio = PedidoDeMidiaObservado("https://ok.vk/?ct=12", TipoDePedidoDeMidiaParaDownload.ARQUIVO, 2, 911_138, "audio/mp4")
        assertTrue(TextoDaOpcaoDeMidiaParaBaixar.texto(audio, null, true).startsWith("Só áudio"))
    }

    @Test
    fun videoEmPeUsaOLadoMenor() {
        assertEquals(720, DetalhesDaMidiaParaBaixar(largura = 720, altura = 1280).linhas)
    }

    @Test
    fun duracaoFormatada() {
        assertEquals("1:35", TextoDaOpcaoDeMidiaParaBaixar.duracao(95_000))
        assertEquals("1:02:05", TextoDaOpcaoDeMidiaParaBaixar.duracao(3_725_000))
    }

    @Test
    fun ordenaPorResolucaoComAudioNoFim() {
        val a = PedidoDeMidiaObservado("a", TipoDePedidoDeMidiaParaDownload.ARQUIVO, 1, 9_000_000, "video/mp4")
        val b = PedidoDeMidiaObservado("b", TipoDePedidoDeMidiaParaDownload.ARQUIVO, 2, 3_000_000, "video/mp4")
        val som = PedidoDeMidiaObservado("s", TipoDePedidoDeMidiaParaDownload.ARQUIVO, 3, 900_000, "audio/mp4")
        val detalhes = mapOf("a" to DetalhesDaMidiaParaBaixar(640, 360), "b" to DetalhesDaMidiaParaBaixar(1920, 1080))
        assertEquals(listOf(b, a, som), TextoDaOpcaoDeMidiaParaBaixar.ordenar(listOf(som, a, b), detalhes))
    }

    @Test
    fun playlistMostraTamanhoEstimadoEmVezDoArquivoDaPlaylist() {
        val mestre = LeituraDosDetalhesDoManifestoDeMidia.hls(
            "#EXTM3U\n#EXT-X-STREAM-INF:AVERAGE-BANDWIDTH=1,BANDWIDTH=8000000,RESOLUTION=1920x1080\n1080.m3u8\n" +
                "#EXT-X-STREAM-INF:BANDWIDTH=900000,RESOLUTION=640x360\n360.m3u8",
        )
        assertEquals(8_000_000, mestre.bitsPorSegundo)
        val comDuracao = mestre.copy(duracaoMs = 60_000)
        assertEquals(60_000_000, comDuracao.tamanhoEstimado)
        val playlist = PedidoDeMidiaObservado("https://c/master.m3u8", TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS, 1, 1_024)
        val texto = TextoDaOpcaoDeMidiaParaBaixar.texto(playlist, comDuracao, temAudioSeparado = false)
        assertTrue(texto.startsWith("1080p (melhor de várias) · 1:00 · ≈ "), texto)
        assertTrue(!texto.contains("1 KB"), texto)
    }
}

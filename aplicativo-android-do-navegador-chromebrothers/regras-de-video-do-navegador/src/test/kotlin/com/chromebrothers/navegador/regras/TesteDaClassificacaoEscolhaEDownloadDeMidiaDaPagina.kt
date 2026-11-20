package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaClassificacaoEscolhaEDownloadDeMidiaDaPagina {
    @Test
    fun arquivoMp4ComTokenContinuaSendoArquivo() {
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.ARQUIVO,
            ClassificacaoDoPedidoDeMidiaParaDownload.classificar("https://cdn.exemplo/video.mp4?token=1"),
        )
    }

    @Test
    fun playlistM3u8EhHls() {
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS,
            ClassificacaoDoPedidoDeMidiaParaDownload.classificar("https://cdn.exemplo/a/master.m3u8?e=2"),
        )
    }

    @Test
    fun manifestoMpdEhDash() {
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH,
            ClassificacaoDoPedidoDeMidiaParaDownload.classificar("https://cdn.exemplo/v.mpd"),
        )
    }

    @Test
    fun trechoTsNaoEntraComoArquivo() {
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.TRECHO,
            ClassificacaoDoPedidoDeMidiaParaDownload.classificar("https://cdn.exemplo/seg-1.ts"),
        )
    }

    @Test
    fun trechoM4sNaoEntraComoArquivo() {
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.TRECHO,
            ClassificacaoDoPedidoDeMidiaParaDownload.classificar("https://cdn.exemplo/seg-1.m4s"),
        )
    }

    @Test
    fun imagemEBlobFicamDeFora() {
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.IGNORAR,
            ClassificacaoDoPedidoDeMidiaParaDownload.classificar("https://cdn.exemplo/poster.jpg"),
        )
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.IGNORAR,
            ClassificacaoDoPedidoDeMidiaParaDownload.classificar("blob:https://site/1"),
        )
    }

    @Test
    fun videoplaybackEhArquivoMesmoSemExtensao() {
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.ARQUIVO,
            ClassificacaoDoPedidoDeMidiaParaDownload.classificar(
                "https://rr.googlevideo.com/videoplayback?id=1",
            ),
        )
    }

    @Test
    fun listaEscondeTrechoERepeteOEnderecoMaisRecente() {
        val pedidos = listOf(
            pedido("https://cdn.exemplo/seg-1.ts", TipoDePedidoDeMidiaParaDownload.TRECHO, 1),
            pedido("https://cdn.exemplo/master.m3u8", TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS, 2),
            pedido("https://cdn.exemplo/filme.mp4", TipoDePedidoDeMidiaParaDownload.ARQUIVO, 3),
            pedido("https://cdn.exemplo/master.m3u8", TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS, 4),
        )
        val lista = EscolhaDoPedidoDeMidiaParaBaixar.listar(pedidos)
        assertEquals(listOf(4L, 3L), lista.map { it.ordem })
    }

    @Test
    fun audioFicaDepoisDoVideo() {
        val lista = EscolhaDoPedidoDeMidiaParaBaixar.listar(
            listOf(
                pedido("https://cdn.exemplo/audio/faixa.m3u8", TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS, 5),
                pedido("https://cdn.exemplo/video/filme.m3u8", TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS, 2),
            ),
        )
        assertEquals("https://cdn.exemplo/video/filme.m3u8", lista.first().endereco)
        assertEquals("https://cdn.exemplo/audio/faixa.m3u8", lista.last().endereco)
    }

    @Test
    fun arquivoPequenoDesceEmUmaParteSo() {
        val partes = PlanoDePartesDoArquivoDeMidia.planejar(500_000)
        assertEquals(listOf(ParteDoArquivoDeMidia(0, 499_999)), partes)
    }

    @Test
    fun arquivoGrandeCobreOIntervaloSemBuraco() {
        val partes = PlanoDePartesDoArquivoDeMidia.planejar(10_000_000)
        assertEquals(4, partes.size)
        assertEquals(0, partes.first().inicio)
        assertEquals(9_999_999, partes.last().fimInclusivo)
        partes.zipWithNext().forEach { (antes, depois) ->
            assertEquals(antes.fimInclusivo + 1, depois.inicio)
        }
    }

    @Test
    fun tamanhoDesconhecidoNaoInventaParte() {
        assertTrue(PlanoDePartesDoArquivoDeMidia.planejar(0).isEmpty())
    }

    @Test
    fun playlistHlsEscolheAMaiorBandaEResolveOCaminho() {
        val texto = """
            #EXTM3U
            #EXT-X-STREAM-INF:BANDWIDTH=800000
            low.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=2500000
            high/video.m3u8
        """.trimIndent()
        val lida = LeituraDePlaylistHlsParaDownload.ler(texto, "https://cdn.exemplo/live/master.m3u8")
        val melhor = LeituraDePlaylistHlsParaDownload.melhorVariante(lida)
        assertEquals("https://cdn.exemplo/live/high/video.m3u8", melhor?.endereco)
        assertEquals(2_500_000, melhor?.banda)
    }

    @Test
    fun playlistDeTrechosResolveMapaEAes128NaoBloqueia() {
        val texto = """
            #EXTM3U
            #EXT-X-MAP:URI="init.mp4"
            #EXTINF:4.0,
            seg-1.ts
            #EXTINF:4.0,
            https://cdn.exemplo/abs/seg-2.ts
            #EXT-X-KEY:METHOD=AES-128,URI="chave"
        """.trimIndent()
        val lida = LeituraDePlaylistHlsParaDownload.ler(texto, "https://cdn.exemplo/v/media.m3u8")
        // AES-128 simples não é DRM: a playlist continua baixável (a chave decifra os trechos seguintes).
        assertFalse(lida.protegida)
        assertEquals("https://cdn.exemplo/v/init.mp4", lida.mapa)
        assertEquals(
            listOf("https://cdn.exemplo/v/seg-1.ts", "https://cdn.exemplo/abs/seg-2.ts"),
            lida.trechos,
        )
    }

    @Test
    fun manifestoDashFicaSoComArquivoCompleto() {
        val texto = """
            <MPD>
              <Period>
                <AdaptationSet>
                  <Representation bandwidth="1000"><BaseURL>https://cdn.exemplo/a.mp4</BaseURL></Representation>
                  <Representation bandwidth="9000"><BaseURL>../b.webm</BaseURL></Representation>
                </AdaptationSet>
              </Period>
            </MPD>
        """.trimIndent()
        val arquivos = LeituraDeManifestoDashParaDownload.arquivosCompletos(
            texto,
            "https://cdn.exemplo/v/m.mpd",
        )
        assertEquals("https://cdn.exemplo/b.webm", arquivos.first().endereco)
        assertEquals(9000, arquivos.first().banda)
        assertEquals(2, arquivos.size)
    }

    @Test
    fun manifestoDashSoComTrechosNaoViraArquivo() {
        val texto = """
            <MPD><Period><AdaptationSet>
              <Representation bandwidth="1000">
                <SegmentTemplate media="seg-${'$'}Number${'$'}.m4s" initialization="init.mp4"/>
              </Representation>
            </AdaptationSet></Period></MPD>
        """.trimIndent()
        assertTrue(
            LeituraDeManifestoDashParaDownload.arquivosCompletos(texto, "https://cdn.exemplo/v/m.mpd").isEmpty(),
        )
    }

    @Test
    fun manifestoDashComSegmentTemplateNumeraOsTrechos() {
        val texto = """
            <MPD mediaPresentationDuration="PT10S">
              <Period>
                <AdaptationSet contentType="video" mimeType="video/mp4">
                  <Representation id="v" bandwidth="1000">
                    <SegmentTemplate timescale="1000" duration="2000" startNumber="1" initialization="init.mp4" media="seg-${'$'}Number${'$'}.m4s"/>
                  </Representation>
                </AdaptationSet>
              </Period>
            </MPD>
        """.trimIndent()
        val plano = LeituraDeManifestoDashParaDownload.plano(texto, "https://cdn.exemplo/v/m.mpd")
        assertFalse(plano.protegido)
        assertNull(plano.audio)
        assertNull(plano.video?.arquivo)
        assertEquals("https://cdn.exemplo/v/init.mp4", plano.video?.inicializacao)
        assertEquals(
            listOf(
                "https://cdn.exemplo/v/seg-1.m4s",
                "https://cdn.exemplo/v/seg-2.m4s",
                "https://cdn.exemplo/v/seg-3.m4s",
                "https://cdn.exemplo/v/seg-4.m4s",
                "https://cdn.exemplo/v/seg-5.m4s",
            ),
            plano.video?.trechos,
        )
    }

    @Test
    fun manifestoDashComEnderecoSemExtensaoBaixaOArquivoInteiro() {
        val texto = """
            <MPD>
              <Period>
                <AdaptationSet contentType="video" mimeType="video/mp4">
                  <Representation bandwidth="800000">
                    <BaseURL>https://vd665.okcdn.ru/expires/1/baixo</BaseURL>
                    <SegmentBase indexRange="100-200"><Initialization range="0-99"/></SegmentBase>
                  </Representation>
                  <Representation bandwidth="2500000">
                    <BaseURL>https://vd665.okcdn.ru/expires/1/video</BaseURL>
                    <SegmentBase indexRange="100-200"><Initialization range="0-99"/></SegmentBase>
                  </Representation>
                </AdaptationSet>
                <AdaptationSet contentType="audio" mimeType="audio/mp4">
                  <Representation bandwidth="128000">
                    <BaseURL>https://vd617.okcdn.ru/expires/1/audio</BaseURL>
                    <SegmentBase indexRange="40-80"><Initialization range="0-39"/></SegmentBase>
                  </Representation>
                </AdaptationSet>
              </Period>
            </MPD>
        """.trimIndent()
        val plano = LeituraDeManifestoDashParaDownload.plano(texto, "https://vd665.okcdn.ru/m.mpd")
        assertFalse(plano.protegido)
        assertEquals("https://vd665.okcdn.ru/expires/1/video", plano.video?.arquivo)
        assertEquals(2_500_000, plano.video?.banda)
        assertEquals("https://vd617.okcdn.ru/expires/1/audio", plano.audio?.arquivo)
    }

    @Test
    fun manifestoDashHerdaOTemplateESubstituiOId() {
        val texto = """
            <MPD mediaPresentationDuration="PT4S">
              <Period>
                <AdaptationSet contentType="video">
                  <SegmentTemplate timescale="1" duration="2" initialization="i.mp4" media="${'$'}RepresentationID${'$'}-${'$'}Number${'$'}.m4s"/>
                  <Representation id="baixo" bandwidth="1000"/>
                  <Representation id="alto" bandwidth="9000"/>
                </AdaptationSet>
              </Period>
            </MPD>
        """.trimIndent()
        val plano = LeituraDeManifestoDashParaDownload.plano(texto, "https://cdn.exemplo/v/m.mpd")
        assertEquals(9000, plano.video?.banda)
        assertEquals("https://cdn.exemplo/v/i.mp4", plano.video?.inicializacao)
        assertEquals(
            listOf("https://cdn.exemplo/v/alto-1.m4s", "https://cdn.exemplo/v/alto-2.m4s"),
            plano.video?.trechos,
        )
    }

    @Test
    fun manifestoDashComLinhaDoTempoUsaOInstante() {
        val texto = """
            <MPD>
              <Period>
                <AdaptationSet mimeType="video/mp4">
                  <Representation bandwidth="1000">
                    <SegmentTemplate timescale="1000" initialization="i.mp4" media="s${'$'}Time${'$'}.m4s">
                      <SegmentTimeline>
                        <S t="0" d="2000" r="1"/>
                        <S d="1000"/>
                      </SegmentTimeline>
                    </SegmentTemplate>
                  </Representation>
                </AdaptationSet>
              </Period>
            </MPD>
        """.trimIndent()
        val plano = LeituraDeManifestoDashParaDownload.plano(texto, "https://cdn.exemplo/a.mpd")
        assertEquals(
            listOf(
                "https://cdn.exemplo/s0.m4s",
                "https://cdn.exemplo/s2000.m4s",
                "https://cdn.exemplo/s4000.m4s",
            ),
            plano.video?.trechos,
        )
    }

    @Test
    fun manifestoDashComListaUsaCadaTrecho() {
        val texto = """
            <MPD>
              <Period>
                <AdaptationSet contentType="video">
                  <Representation bandwidth="1000">
                    <SegmentList>
                      <Initialization sourceURL="init.mp4"/>
                      <SegmentURL media="a.m4s"/>
                      <SegmentURL media="b.m4s"/>
                    </SegmentList>
                  </Representation>
                </AdaptationSet>
              </Period>
            </MPD>
        """.trimIndent()
        val plano = LeituraDeManifestoDashParaDownload.plano(texto, "https://cdn.exemplo/v/m.mpd")
        assertEquals("https://cdn.exemplo/v/init.mp4", plano.video?.inicializacao)
        assertEquals(
            listOf("https://cdn.exemplo/v/a.m4s", "https://cdn.exemplo/v/b.m4s"),
            plano.video?.trechos,
        )
    }

    @Test
    fun manifestoDashProtegidoNaoEntregaTrilha() {
        val texto = """
            <MPD>
              <Period>
                <AdaptationSet contentType="video">
                  <ContentProtection schemeIdUri="urn:uuid:edef8ba9-79d6-4ace-a3c8-27dcd51d21ed"/>
                  <Representation bandwidth="1000">
                    <BaseURL>https://cdn.exemplo/v.mp4</BaseURL>
                  </Representation>
                </AdaptationSet>
              </Period>
            </MPD>
        """.trimIndent()
        val plano = LeituraDeManifestoDashParaDownload.plano(texto, "https://cdn.exemplo/m.mpd")
        assertTrue(plano.protegido)
        assertNull(plano.video)
    }

    @Test
    fun manifestoDashComArquivoCompletoContinuaSendoUmArquivo() {
        val texto = """
            <MPD><Period><AdaptationSet contentType="video">
              <Representation bandwidth="1000"><BaseURL>https://cdn.exemplo/a.mp4</BaseURL></Representation>
              <Representation bandwidth="9000"><BaseURL>../b.webm</BaseURL></Representation>
            </AdaptationSet></Period></MPD>
        """.trimIndent()
        val plano = LeituraDeManifestoDashParaDownload.plano(texto, "https://cdn.exemplo/v/m.mpd")
        assertEquals("https://cdn.exemplo/b.webm", plano.video?.arquivo)
        assertEquals(9000, plano.video?.banda)
        assertNull(plano.audio)
    }

    @Test
    fun rotuloMostraTipoEHost() {
        assertEquals(
            "Playlist · cdn.exemplo",
            RotuloDoPedidoDeMidiaParaBaixar.texto(
                pedido("https://cdn.exemplo/master.m3u8", TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS, 1),
            ),
        )
    }

    @Test
    fun rotuloMostraVideoQuandoOArquivoEGrande() {
        assertEquals(
            "Vídeo · 76,3 MB · cdn.exemplo",
            RotuloDoPedidoDeMidiaParaBaixar.texto(
                PedidoDeMidiaObservado(
                    "https://cdn.exemplo/filme",
                    TipoDePedidoDeMidiaParaDownload.ARQUIVO,
                    1,
                    80_000_000,
                ),
            ),
        )
    }

    @Test
    fun rotuloMostraPedacoQuandoOArquivoEPequeno() {
        assertEquals(
            "Pedaço · 390 KB · cdn.exemplo",
            RotuloDoPedidoDeMidiaParaBaixar.texto(
                PedidoDeMidiaObservado(
                    "https://cdn.exemplo/trecho",
                    TipoDePedidoDeMidiaParaDownload.ARQUIVO,
                    1,
                    400_000,
                ),
            ),
        )
    }

    @Test
    fun listaEscondePedacoQuandoExistePlaylist() {
        val lista = EscolhaDoPedidoDeMidiaParaBaixar.listar(
            listOf(
                PedidoDeMidiaObservado("https://cdn.exemplo/trecho", TipoDePedidoDeMidiaParaDownload.ARQUIVO, 1, 400_000),
                PedidoDeMidiaObservado("https://cdn.exemplo/master.m3u8", TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS, 2, 2_000),
                PedidoDeMidiaObservado("https://cdn.exemplo/filme", TipoDePedidoDeMidiaParaDownload.ARQUIVO, 3, 80_000_000),
            ),
        )
        assertEquals(
            listOf("https://cdn.exemplo/master.m3u8", "https://cdn.exemplo/filme"),
            lista.map { it.endereco },
        )
    }

    @Test
    fun listaMostraPedacoQuandoNaoHaVideoInteiro() {
        val lista = EscolhaDoPedidoDeMidiaParaBaixar.listar(
            listOf(
                PedidoDeMidiaObservado("https://cdn.exemplo/trecho", TipoDePedidoDeMidiaParaDownload.ARQUIVO, 1, 400_000),
            ),
        )
        assertEquals("https://cdn.exemplo/trecho", lista.single().endereco)
    }

    @Test
    fun tipoDaRespostaReconheceVideoPlaylistETrecho() {
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.ARQUIVO,
            ClassificacaoDoTipoDeRespostaDeMidiaParaDownload.classificar("video/mp4; charset=binary"),
        )
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS,
            ClassificacaoDoTipoDeRespostaDeMidiaParaDownload.classificar("application/vnd.apple.mpegurl"),
        )
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH,
            ClassificacaoDoTipoDeRespostaDeMidiaParaDownload.classificar("application/dash+xml"),
        )
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.TRECHO,
            ClassificacaoDoTipoDeRespostaDeMidiaParaDownload.classificar("video/mp2t"),
        )
        assertEquals(
            TipoDePedidoDeMidiaParaDownload.IGNORAR,
            ClassificacaoDoTipoDeRespostaDeMidiaParaDownload.classificar("text/html"),
        )
    }

    @Test
    fun scriptSoLigaACapturaQuandoOInterruptorPede() {
        assertTrue(ScriptInjetadoParaDetectarVideoHtmlNaPagina.texto(false).contains("__chromebrothersCapturarMidia = false"))
        assertTrue(ScriptInjetadoParaDetectarVideoHtmlNaPagina.texto(true).contains("__chromebrothersCapturarMidia = true"))
        assertTrue(ScriptInjetadoParaDetectarVideoHtmlNaPagina.texto(true).contains("onMediaUrl"))
    }

    @Test
    fun oScriptAvisaSemCriarObjetoNaPagina() {
        val script = ScriptInjetadoParaDetectarVideoHtmlNaPagina.texto(true)
        assertTrue(script.contains("avisarChromebrothers(\"onMediaUrl\""))
        assertTrue(script.contains("JSON.stringify({ nome: nome, argumentos: argumentos })"))
        assertTrue(script.contains("__chromebrothersInstalado === 14"))
        assertFalse(script.contains("window.ChromeBrothers"))
    }

    @Test
    fun scriptDeInicioInstalaACapturaAntesDoPlayer() {
        val ligado = ScriptInjetadoParaDetectarVideoHtmlNaPagina.inicio(true)
        val desligado = ScriptInjetadoParaDetectarVideoHtmlNaPagina.inicio(false)
        assertTrue(ligado.contains("__chromebrothersCapturarMidia = true"))
        assertTrue(desligado.contains("__chromebrothersCapturarMidia = false"))
        assertTrue(ligado.contains("instalarCapturaDeMidia();"))
        assertTrue(desligado.contains("instalarCapturaDeMidia();"))
        assertTrue(ligado.contains("onMediaUrl"))
        assertTrue(ligado.contains("content-length"))
        assertTrue(ligado.contains("content-range"))
        assertFalse(ligado.contains("onVideoPlaying"))
    }

    @Test
    fun varianteSemBandaNaoEscolheNada() {
        val lida = LeituraDePlaylistHlsParaDownload.ler("#EXTM3U\n#EXTINF:1,\na.ts\n", "https://cdn.exemplo/m.m3u8")
        assertNull(LeituraDePlaylistHlsParaDownload.melhorVariante(lida))
        assertFalse(lida.protegida)
    }

    private fun pedido(endereco: String, tipo: TipoDePedidoDeMidiaParaDownload, ordem: Long) =
        PedidoDeMidiaObservado(endereco, tipo, ordem)
}

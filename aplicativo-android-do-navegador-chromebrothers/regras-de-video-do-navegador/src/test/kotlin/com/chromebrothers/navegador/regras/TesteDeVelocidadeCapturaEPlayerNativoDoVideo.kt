package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDeVelocidadeCapturaEPlayerNativoDoVideo {
    @Test
    fun velocidadePedidaAbaixoDeUmQuartoSobeParaOMinimo() {
        assertEquals(0.25, AjusteDeVelocidadeDeReproducaoDoVideo.ajustar(0.1))
    }

    @Test
    fun velocidadeMinimaDeUmQuartoPermanece() {
        assertEquals(0.25, AjusteDeVelocidadeDeReproducaoDoVideo.ajustar(0.25))
    }

    @Test
    fun velocidadeNormalPermanece() {
        assertEquals(1.0, AjusteDeVelocidadeDeReproducaoDoVideo.ajustar(1.0))
    }

    @Test
    fun velocidadeInvalidaVoltaParaReproducaoNormal() {
        assertEquals(1.0, AjusteDeVelocidadeDeReproducaoDoVideo.ajustar(Double.NaN))
    }

    @Test
    fun enderecoMp4HttpsPodeAbrirNoPlayerNativo() {
        assertTrue(
            DecisaoDeAbrirVideoNoPlayerNativo.podeAbrir("https://cdn.exemplo/video.mp4"),
        )
    }

    @Test
    fun enderecoBlobNaoPodeAbrirNoPlayerNativo() {
        assertFalse(
            DecisaoDeAbrirVideoNoPlayerNativo.podeAbrir("blob:https://site.exemplo/123"),
        )
    }

    @Test
    fun paginaHtmlNaoPodeAbrirNoPlayerNativo() {
        assertFalse(
            DecisaoDeAbrirVideoNoPlayerNativo.podeAbrir("https://site.exemplo/assistir.html?id=1"),
        )
    }

    @Test
    fun arquivoMp4AbreNoPlayerDeArquivo() {
        assertEquals(
            DestinoDoToqueNoBotaoDoVideo.PLAYER_DE_ARQUIVO,
            DecisaoDeDestinoDoToqueNoBotaoDoVideo.destino("https://cdn.exemplo/video.mp4"),
        )
    }

    @Test
    fun youtubeEBlobAbremNoPlayerDaPagina() {
        assertEquals(
            DestinoDoToqueNoBotaoDoVideo.PLAYER_DA_PAGINA,
            DecisaoDeDestinoDoToqueNoBotaoDoVideo.destino("blob:https://www.youtube.com/abc"),
        )
        assertEquals(
            DestinoDoToqueNoBotaoDoVideo.PLAYER_DA_PAGINA,
            DecisaoDeDestinoDoToqueNoBotaoDoVideo.destino("https://www.youtube.com/watch?v=abc"),
        )
        assertEquals(
            DestinoDoToqueNoBotaoDoVideo.PLAYER_DA_PAGINA,
            DecisaoDeDestinoDoToqueNoBotaoDoVideo.destino("https://boosty.to/video/1"),
        )
    }

    @Test
    fun botaoApareceComVideoTocandoMesmoSemArquivo() {
        assertTrue(DecisaoDeMostrarBotaoFlutuanteDoVideo.mostrar(temVideoTocando = true, temLimitesNaTela = true))
        assertFalse(DecisaoDeMostrarBotaoFlutuanteDoVideo.mostrar(temVideoTocando = true, temLimitesNaTela = false))
        assertFalse(DecisaoDeMostrarBotaoFlutuanteDoVideo.mostrar(temVideoTocando = false, temLimitesNaTela = true))
    }

    @Test
    fun estadoDoVideoNaPaginaLePosicaoEVelocidade() {
        val estado = LeituraDoEstadoDoVideoControladoNaPagina.ler(
            """{"pausado":false,"atual":12.5,"duracao":90,"taxa":0.1}""",
        )
        assertEquals(false, estado?.pausado)
        assertEquals(12.5, estado?.posicaoSegundos)
        assertEquals(90.0, estado?.duracaoSegundos)
        assertEquals(0.25, estado?.velocidade)
    }

    @Test
    fun mensagemDeVideoAplicaOMinimoDeVelocidade() {
        val mensagem = LeituraDaMensagemDeVideoTocando.ler("v1", " https://cdn.exemplo/a.mp4 ", "0.1")
        assertEquals("v1", mensagem.id)
        assertEquals("https://cdn.exemplo/a.mp4", mensagem.endereco)
        assertEquals(0.25, mensagem.velocidade)
    }

    @Test
    fun scriptInjetadoExpoeDeteccaoVelocidadeECaptura() {
        val script = ScriptInjetadoParaDetectarVideoHtmlNaPagina.texto()
        assertTrue(script.contains("onVideoPlaying"))
        assertTrue(script.contains("onVideoBounds"))
        assertTrue(script.contains("""avisarChromebrothers("onVideoLongPress", [idDe(video), enderecoHttpDoVideo(video)]);"""))
        assertFalse(script.contains("contextmenu"))
        assertTrue(script.contains("volumechange"))
        assertTrue(script.contains("chromebrothersDefinirVelocidade"))
        assertTrue(script.contains("chromebrothersCapturarQuadro"))
        assertTrue(script.contains("larguraMaxima"))
        assertTrue(script.contains("image/jpeg"))
        assertFalse(script.contains("tela.width = video.videoWidth"))
        assertTrue(script.contains("chromebrothersEntrarNoPlayer"))
        assertTrue(script.contains("cb-palco-do-player"))
        assertTrue(script.contains("chromebrothersDefinirReproducao"))
        assertTrue(script.contains("chromebrothersDeslocar"))
        assertTrue(script.contains("__chromebrothersQuerReproduzir"))
        assertTrue(script.contains("__cbPauseNativo"))
        assertTrue(script.contains("__cbNoPalco"))
        assertFalse(script.contains("webkitEnterFullscreen"))
        assertFalse(script.contains("webkitExitFullscreen"))
        assertTrue(script.contains("playbackRate"))
    }

    @Test
    fun detectorDeVideoNaoVarreAPaginaInteiraACadaMudanca() {
        val script = ScriptInjetadoParaDetectarVideoHtmlNaPagina.texto()
        val observador = script.substringAfter("new MutationObserver(").substringBefore(".observe(")
        assertTrue(observador.contains("procurarNoNovo"), "só os nós novos na hora")
        assertTrue(observador.contains("agendarVarreduraCompleta"), "varredura completa vai para o ocioso")
        assertFalse(observador.contains("varreduraCompletaAgora"))
        assertTrue(script.contains("requestIdleCallback"))
        assertTrue(script.contains("timeRemaining()"))
        assertTrue(script.contains("window.addEventListener(\"scroll\", agendarLimites, { capture: true, passive: true })"))
        assertTrue(script.contains("requestAnimationFrame"))
    }

    @Test
    fun miniaturaDoVideoSoQuandoChegaPertoDaTela() {
        val script = ScriptInjetadoParaDetectarVideoHtmlNaPagina.texto()
        val preparar = script.substringAfter("function prepararMiniatura(video)").substringBefore("function pintarMiniaturaAgora")
        assertTrue(preparar.contains("observadorDeMiniatura.observe(video)"), "espera o vídeo chegar perto da tela")
        assertFalse(preparar.contains("video.preload = "), "não força carregar na hora")
        assertTrue(script.contains("new IntersectionObserver("))
    }

    @Test
    fun scriptMostraOPrimeiroQuadroParadoESeguraAutoplayComSom() {
        val script = ScriptInjetadoParaDetectarVideoHtmlNaPagina.texto()
        assertTrue(script.contains("video.preload = \"metadata\""))
        assertTrue(script.contains("video.currentTime = 0.04"))
        assertTrue(script.contains("video.pause()"))
        assertTrue(script.contains("ultimoGesto > 0"))
        assertTrue(script.contains("navigator.userActivation"))
        assertFalse(script.contains("ultimoGesto < 1200"))
        assertFalse(script.contains("video.load()"))
        assertFalse(script.contains("contextmenu"))
    }

    @Test
    fun botaoFlutuanteFicaAoLadoDoVideoENaoPorCimaDele() {
        val posicao = PosicionamentoDoBotaoFlutuantePertoDoVideo.calcular(
            esquerdaCss = 100f,
            topoCss = 40f,
            larguraCss = 400f,
            alturaCss = 220f,
            larguraViewportCss = 800f,
            alturaViewportCss = 600f,
            larguraWebViewPx = 800f,
            alturaWebViewPx = 600f,
            tamanhoBotaoPx = 56f,
            margemPx = 12f,
        )
        assertTrue(posicao.visivel)
        assertEquals(512f, posicao.x)
        assertEquals(122f, posicao.y)
    }

    @Test
    fun botaoFlutuanteFicaAbaixoQuandoOVideoOcupaALarguraDaTela() {
        val posicao = PosicionamentoDoBotaoFlutuantePertoDoVideo.calcular(
            esquerdaCss = 0f,
            topoCss = 40f,
            larguraCss = 800f,
            alturaCss = 200f,
            larguraViewportCss = 800f,
            alturaViewportCss = 600f,
            larguraWebViewPx = 800f,
            alturaWebViewPx = 600f,
            tamanhoBotaoPx = 56f,
            margemPx = 12f,
        )
        assertTrue(posicao.visivel)
        assertEquals(372f, posicao.x)
        assertEquals(252f, posicao.y)
    }

    @Test
    fun botaoFlutuanteSomeQuandoOVideoSaiDaTela() {
        val posicao = PosicionamentoDoBotaoFlutuantePertoDoVideo.calcular(
            esquerdaCss = 0f,
            topoCss = -400f,
            larguraCss = 200f,
            alturaCss = 80f,
            larguraViewportCss = 800f,
            alturaViewportCss = 600f,
            larguraWebViewPx = 800f,
            alturaWebViewPx = 600f,
            tamanhoBotaoPx = 56f,
            margemPx = 12f,
        )
        assertFalse(posicao.visivel)
    }
}

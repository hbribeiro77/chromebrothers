package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDasFerramentasDePlayerVisorEBotaoFlutuanteDoVideoNoChat {
    @Test
    fun jsonPermitidasEInstrucaoIncluemPlayerVisorEBotaoFlutuante() {
        val json = FerramentasDeLeituraDoChatDaOpenRouter.json()
        val nomes = listOf(
            FerramentasDeLeituraDoChatDaOpenRouter.VIDEO_DETECTADO,
            FerramentasDeLeituraDoChatDaOpenRouter.VIDEOS_DA_BIBLIOTECA,
            FerramentasDeLeituraDoChatDaOpenRouter.IMAGENS_DA_BIBLIOTECA,
            AcoesConfirmadasDoChatDaOpenRouter.MOSTRAR_BOTAO_FLUTUANTE,
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_PLAYER_DETECTADO,
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_VIDEO_DA_BIBLIOTECA,
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_IMAGEM_DA_BIBLIOTECA,
        )
        assertEquals(7, nomes.toSet().size)
        nomes.forEach { nome ->
            assertTrue(json.contains(nome), nome)
            assertTrue(nome in FerramentasDeLeituraDoChatDaOpenRouter.permitidas, nome)
        }
        assertTrue(FerramentasDeLeituraDoChatDaOpenRouter.VIDEO_DETECTADO !in AcoesConfirmadasDoChatDaOpenRouter.nomes)
        assertTrue(FerramentasDeLeituraDoChatDaOpenRouter.VIDEOS_DA_BIBLIOTECA !in AcoesConfirmadasDoChatDaOpenRouter.nomes)
        assertTrue(FerramentasDeLeituraDoChatDaOpenRouter.IMAGENS_DA_BIBLIOTECA !in AcoesConfirmadasDoChatDaOpenRouter.nomes)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.MOSTRAR_BOTAO_FLUTUANTE in AcoesConfirmadasDoChatDaOpenRouter.nomes)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_PLAYER_DETECTADO in AcoesConfirmadasDoChatDaOpenRouter.nomes)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_VIDEO_DA_BIBLIOTECA in AcoesConfirmadasDoChatDaOpenRouter.nomes)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_IMAGEM_DA_BIBLIOTECA in AcoesConfirmadasDoChatDaOpenRouter.nomes)
        val instrucao = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: Sjinn")
        assertTrue(instrucao.contains("botão flutuante"))
        assertTrue(instrucao.contains("player"))
        assertTrue(instrucao.contains("mostrar_botao_flutuante_do_video"))
        assertTrue(instrucao.contains("abrir_player_do_video_detectado"))
        assertTrue(instrucao.contains("listar_videos_da_biblioteca"))
        assertTrue(instrucao.contains("abrir_imagem_da_biblioteca"))
        assertEquals(
            "Lendo o vídeo detectado na aba…",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloEmAndamento(
                FerramentasDeLeituraDoChatDaOpenRouter.VIDEO_DETECTADO,
            ),
        )
        assertEquals(
            "Listou os vídeos da biblioteca",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloVisivel(
                FerramentasDeLeituraDoChatDaOpenRouter.VIDEOS_DA_BIBLIOTECA,
            ),
        )
        assertEquals(
            "Listou as imagens da biblioteca",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloVisivel(
                FerramentasDeLeituraDoChatDaOpenRouter.IMAGENS_DA_BIBLIOTECA,
            ),
        )
    }

    @Test
    fun oTextoDaLeituraDescreveVideoBibliotecaEImagem() {
        assertEquals(
            "Nenhum vídeo detectado nesta aba.",
            FerramentasDeLeituraDoChatDaOpenRouter.textoDoVideoDetectadoNaAba(null, false),
        )
        val detectado = FerramentasDeLeituraDoChatDaOpenRouter.textoDoVideoDetectadoNaAba(
            "https://cdn.exemplo/aula.mp4",
            botaoPodeAparecer = true,
        )
        assertTrue(detectado.contains("https://cdn.exemplo/aula.mp4"))
        assertTrue(detectado.contains("botão flutuante"))
        val videos = FerramentasDeLeituraDoChatDaOpenRouter.textoDosVideosDaBiblioteca(
            listOf(
                ItemDaBibliotecaDeVideos(
                    chave = "chave-1",
                    titulo = "Aula",
                    enderecoParaAbrir = "https://cdn.exemplo/aula.mp4",
                    abreNoPlayerDeArquivo = true,
                    favorito = false,
                    instante = 1L,
                    nomeDaMiniatura = "",
                ),
            ),
        )
        assertTrue(videos.contains("chave-1"))
        assertTrue(videos.contains("Aula"))
        assertTrue(videos.contains("abreNoPlayerDeArquivo"))
        assertEquals(
            "Nenhum vídeo na biblioteca.",
            FerramentasDeLeituraDoChatDaOpenRouter.textoDosVideosDaBiblioteca(emptyList()),
        )
        val imagens = FerramentasDeLeituraDoChatDaOpenRouter.textoDasImagensDaBiblioteca(
            listOf(
                ItemDaBibliotecaDeImagensCapturadas(
                    id = "img-1",
                    instante = 1L,
                    nomeDoArquivo = "quadro.png",
                    naGaleria = false,
                    favorito = false,
                    nomeExibido = "Quadro da aula",
                ),
            ),
        )
        assertTrue(imagens.contains("img-1"))
        assertTrue(imagens.contains("Quadro da aula"))
        assertEquals(
            "Nenhuma imagem na biblioteca.",
            FerramentasDeLeituraDoChatDaOpenRouter.textoDasImagensDaBiblioteca(emptyList()),
        )
    }

    @Test
    fun interpretarAbreVideoEImagemEAceitaBotaoEPlayerSemArgumentos() {
        val botao = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.MOSTRAR_BOTAO_FLUTUANTE,
            "{}",
        )
        val pedidoBotao = (botao as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
        assertTrue(pedidoBotao is PedidoDeAcaoConfirmadaDoChat.MostrarBotaoFlutuanteDoVideo)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedidoBotao).contains("botão flutuante"))

        val player = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_PLAYER_DETECTADO,
            "",
        )
        val pedidoPlayer = (player as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
        assertTrue(pedidoPlayer is PedidoDeAcaoConfirmadaDoChat.AbrirPlayerDoVideoDetectado)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedidoPlayer).contains("player"))

        val video = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_VIDEO_DA_BIBLIOTECA,
            """{"chave":"chave-1"}""",
        )
        val pedidoVideo = (video as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
            as PedidoDeAcaoConfirmadaDoChat.AbrirVideoDaBiblioteca
        assertEquals("chave-1", pedidoVideo.chave)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedidoVideo).contains("chave-1"))
        val semChave = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_VIDEO_DA_BIBLIOTECA,
            "{}",
        )
        assertEquals("Faltou a chave.", (semChave as ResultadoDaInterpretacaoDaAcaoDoChat.Recusada).mensagem)

        val imagem = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_IMAGEM_DA_BIBLIOTECA,
            """{"id":"img-9"}""",
        )
        val pedidoImagem = (imagem as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
            as PedidoDeAcaoConfirmadaDoChat.AbrirImagemDaBiblioteca
        assertEquals("img-9", pedidoImagem.id)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedidoImagem).contains("img-9"))
        val semId = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_IMAGEM_DA_BIBLIOTECA,
            "{}",
        )
        assertEquals("Faltou o id.", (semId as ResultadoDaInterpretacaoDaAcaoDoChat.Recusada).mensagem)
    }

    @Test
    fun siteLiberadoPulaConfirmacaoDoBotaoDoPlayerEDaBiblioteca() {
        val sites = listOf("sjinn.ai")
        val aba = "https://www.sjinn.ai/aula"
        val botao = PedidoDeAcaoConfirmadaDoChat.MostrarBotaoFlutuanteDoVideo()
        val player = PedidoDeAcaoConfirmadaDoChat.AbrirPlayerDoVideoDetectado()
        val video = PedidoDeAcaoConfirmadaDoChat.AbrirVideoDaBiblioteca("chave-1")
        val imagem = PedidoDeAcaoConfirmadaDoChat.AbrirImagemDaBiblioteca("img-1")
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(botao, aba, sites))
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(player, aba, sites))
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(video, aba, sites))
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(imagem, aba, sites))
        assertFalse(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(botao, "https://example.com", sites))
        assertFalse(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(player, "https://example.com", sites))
        assertFalse(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(video, "https://example.com", emptyList()))
        assertFalse(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(imagem, "https://example.com", emptyList()))
        assertEquals("sjinn.ai", LiberacaoDeNavegacaoDosSitesParaOChat.hospedeiroDaAcao(player, aba))
    }

    @Test
    fun abrirOVideoNaoPedeEnderecoEBotaoFlutuantePedeAFerramenta() {
        val player = ContinuacaoDoPedidoDoChat.faltam("abre o vídeo", emptySet())
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_PLAYER_DETECTADO in player)
        assertFalse(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_ENDERECO in player)
        val botao = ContinuacaoDoPedidoDoChat.faltam("mostra o botão flutuante", emptySet())
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.MOSTRAR_BOTAO_FLUTUANTE in botao)
        assertFalse(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_ENDERECO in botao)
        val daBiblioteca = ContinuacaoDoPedidoDoChat.faltam("abre o vídeo da biblioteca", emptySet())
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_VIDEO_DA_BIBLIOTECA in daBiblioteca)
        assertFalse(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_ENDERECO in daBiblioteca)
        val imagem = ContinuacaoDoPedidoDoChat.faltam("abre a imagem da biblioteca", emptySet())
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_IMAGEM_DA_BIBLIOTECA in imagem)
        assertFalse(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_COMPONENTE in imagem)
    }
}

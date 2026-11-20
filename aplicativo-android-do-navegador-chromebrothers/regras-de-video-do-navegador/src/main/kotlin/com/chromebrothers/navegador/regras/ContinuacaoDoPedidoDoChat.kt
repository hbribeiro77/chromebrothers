package com.chromebrothers.navegador.regras

object ContinuacaoDoPedidoDoChat {
    const val LIMITE = 12

    fun faltam(pedido: String, feitas: Set<String>): Set<String> {
        return acoes(pedido) - feitas
    }

    fun precisa(
        pedido: String,
        feitas: Set<String>,
        continuacoes: Int,
        respostaDoAssistente: String = "",
    ): Boolean {
        if (continuacoes >= LIMITE) return false
        if (faltam(pedido, feitas).isNotEmpty()) return true
        return pareceIncompleta(respostaDoAssistente)
    }

    fun pareceIncompleta(resposta: String): Boolean {
        val texto = resposta.lowercase()
        if (texto.isBlank()) return false
        return Regex(
            """\b(próximo passo|proximo passo|em seguida vou|depois vou|depois eu vou|agora vou|ainda preciso|faltando|vou (capturar|publicar|montar|abrir|clicar|gerar|continuar)|posso continuar|me diga se|avise se quiser)\b""",
        ).containsMatchIn(texto)
    }

    fun mensagem(faltando: Set<String>): String {
        if (faltando.isEmpty()) return mensagemGenerica()
        val partes = mutableListOf<String>()
        if (AcoesConfirmadasDoChatDaOpenRouter.ABRIR_ENDERECO in faltando) {
            partes += "Continue até abrir o que foi pedido."
        }
        if (AcoesConfirmadasDoChatDaOpenRouter.ABRIR_COMPONENTE in faltando) {
            partes += "Continue até abrir o componente da biblioteca."
        }
        if (AcoesConfirmadasDoChatDaOpenRouter.GUARDAR_COMPONENTE in faltando) {
            partes += "Continue até guardar o componente na biblioteca."
        }
        if (AcoesConfirmadasDoChatDaOpenRouter.APAGAR_COMPONENTE in faltando) {
            partes += "Continue até apagar o componente da biblioteca."
        }
        if (FerramentasDeLeituraDoChatDaOpenRouter.LER_CODIGO_DO_COMPONENTE in faltando) {
            partes += "Continue até ler o código do componente."
        }
        if (FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_COMPONENTE in faltando) {
            partes += "Continue até capturar o componente."
        }
        if (AcoesConfirmadasDoChatDaOpenRouter.CLICAR in faltando) {
            partes += "Continue até o clique pedido."
        }
        if (AcoesConfirmadasDoChatDaOpenRouter.ESCREVER_NA_PAGINA in faltando) {
            partes += "Continue até escrever no editor da página."
        }
        if (AcoesConfirmadasDoChatDaOpenRouter.GERAR_MARKDOWN in faltando) {
            partes += "Continue até gerar o markdown."
        }
        if (AcoesConfirmadasDoChatDaOpenRouter.GERAR_ZIP in faltando) {
            partes += "Continue até gerar o ZIP."
        }
        if (FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_ABA in faltando) {
            partes += "Continue até capturar a aba."
        }
        if (AcoesConfirmadasDoChatDaOpenRouter.PUBLICAR_PAGINA in faltando) {
            partes += "Continue até publicar a página na coleção."
        }
        if (AcoesConfirmadasDoChatDaOpenRouter.MOSTRAR_BOTAO_FLUTUANTE in faltando) {
            partes += "Continue até mostrar o botão flutuante do vídeo."
        }
        if (AcoesConfirmadasDoChatDaOpenRouter.ABRIR_PLAYER_DETECTADO in faltando) {
            partes += "Continue até abrir o player do vídeo detectado."
        }
        if (AcoesConfirmadasDoChatDaOpenRouter.ABRIR_VIDEO_DA_BIBLIOTECA in faltando) {
            partes += "Continue até abrir o vídeo da biblioteca."
        }
        if (AcoesConfirmadasDoChatDaOpenRouter.ABRIR_IMAGEM_DA_BIBLIOTECA in faltando) {
            partes += "Continue até abrir a imagem da biblioteca."
        }
        if (partes.isEmpty()) return mensagemGenerica()
        partes += "Não pare para explicar o próximo passo."
        return partes.joinToString(" ")
    }

    fun mensagemGenerica(): String {
        return "Continue com as ferramentas até concluir o pedido. Não pare para explicar o próximo passo e não peça para o usuário dizer prossiga."
    }

    private fun acoes(pedido: String): Set<String> {
        val texto = pedido.lowercase()
        val acoes = mutableSetOf<String>()
        val falaDaColecao = texto.contains("webview") || texto.contains("coleç") || texto.contains("colec")
        val pedeIncluir = texto.contains("coloca") || texto.contains("montar") || texto.contains("monta ") ||
            texto.contains("adicion") || texto.contains("acrescent")
        val falaDePagina = texto.contains("página") || texto.contains("pagina")
        val falaDeComponente = texto.contains("componente")
        val bibliotecaDeComponentes = texto.contains("biblioteca") && falaDeComponente
        val pedeApagarComponente = texto.contains("apagar_componente") || (
            Regex("""\b(apaga|apagar|exclui|excluir)\b""").containsMatchIn(texto) && bibliotecaDeComponentes
        )
        val pedeColecaoEmHtml = pedeIncluir && (
            falaDaColecao || texto.contains("vídeo") || texto.contains("video") || texto.contains("imagem")
        )
        val pedeAbrir = Regex("""\b(abre|abrir)\b""").containsMatchIn(texto)
        val falaDeVideo = texto.contains("vídeo") || texto.contains("video")
        val pedeBotaoFlutuante = texto.contains("botão flutuante") || texto.contains("botao flutuante")
        val pedeVideoDaBiblioteca = pedeAbrir && texto.contains("biblioteca") && falaDeVideo && !falaDePagina
        val pedeImagemDaBiblioteca = pedeAbrir && texto.contains("biblioteca") &&
            texto.contains("imagem") && !falaDePagina
        val pedePlayerDoVideoDetectado = !texto.contains("biblioteca") && !falaDaColecao && !falaDePagina && (
            (pedeAbrir && falaDeVideo) || (pedeAbrir && texto.contains("player"))
        )
        val pedeAbrirComponente = texto.contains("abrir_componente") || (
            pedeAbrir && bibliotecaDeComponentes
        )
        val pedeGuardarComponente = texto.contains("guardar_componente") || (
            Regex("""\b(guarda|guardar|salva|salvar)\b""").containsMatchIn(texto) &&
                falaDeComponente &&
                (texto.contains("biblioteca") || texto.contains("coleç") || texto.contains("colec"))
        )
        if (pedeColecaoEmHtml) acoes += AcoesConfirmadasDoChatDaOpenRouter.PUBLICAR_PAGINA
        if (pedeAbrirComponente) {
            acoes += AcoesConfirmadasDoChatDaOpenRouter.ABRIR_COMPONENTE
        } else if (pedeVideoDaBiblioteca) {
            acoes += AcoesConfirmadasDoChatDaOpenRouter.ABRIR_VIDEO_DA_BIBLIOTECA
        } else if (pedeImagemDaBiblioteca) {
            acoes += AcoesConfirmadasDoChatDaOpenRouter.ABRIR_IMAGEM_DA_BIBLIOTECA
        } else if (pedePlayerDoVideoDetectado) {
            acoes += AcoesConfirmadasDoChatDaOpenRouter.ABRIR_PLAYER_DETECTADO
        } else if (Regex("""\b(abre|abrir|acessa|acessar)\b""").containsMatchIn(texto)) {
            acoes += AcoesConfirmadasDoChatDaOpenRouter.ABRIR_ENDERECO
        }
        if (pedeBotaoFlutuante) {
            acoes += AcoesConfirmadasDoChatDaOpenRouter.MOSTRAR_BOTAO_FLUTUANTE
        }
        if (pedeGuardarComponente) acoes += AcoesConfirmadasDoChatDaOpenRouter.GUARDAR_COMPONENTE
        if (pedeApagarComponente) acoes += AcoesConfirmadasDoChatDaOpenRouter.APAGAR_COMPONENTE
        if (Regex("""\b(clica|clicar|clique)\b""").containsMatchIn(texto)) {
            acoes += AcoesConfirmadasDoChatDaOpenRouter.CLICAR
        }
        val pedeEscreverNaPagina = texto.contains("escrever_na_pagina") ||
            (
                Regex("""\b(escreve|escrever|digita|digitar|digite)\b""").containsMatchIn(texto) &&
                    (
                        texto.contains("documento") || texto.contains("docs") ||
                            texto.contains("página") || texto.contains("pagina") ||
                            texto.contains("editor")
                        )
                )
        if (pedeEscreverNaPagina) {
            acoes += AcoesConfirmadasDoChatDaOpenRouter.ESCREVER_NA_PAGINA
        }
        if (texto.contains("markdown") || texto.contains(".md") ||
            (texto.contains("gera") && texto.contains("documento"))
        ) {
            acoes += AcoesConfirmadasDoChatDaOpenRouter.GERAR_MARKDOWN
        }
        if (texto.contains("zip") || (texto.contains("gera") && texto.contains("pacote"))) {
            acoes += AcoesConfirmadasDoChatDaOpenRouter.GERAR_ZIP
        }
        val pedeCaptura = Regex("""\b(captura|capturar|screenshot)\b""").containsMatchIn(texto) ||
            texto.contains("print da tela") ||
            texto.contains("print de tela")
        if (pedeCaptura && falaDeComponente) {
            acoes += FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_COMPONENTE
        } else if (pedeCaptura) {
            acoes += FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_ABA
        }
        val pedeLer = Regex("""(?<!\p{L})(l[eê]|ler|leia|mostra|mostrar|ver)(?!\p{L})""")
        val pedeLerCodigo = texto.contains("ler_codigo") || (
            falaDeComponente &&
                (texto.contains("código") || texto.contains("codigo") || texto.contains("html")) &&
                pedeLer.containsMatchIn(texto)
        )
        if (pedeLerCodigo) acoes += FerramentasDeLeituraDoChatDaOpenRouter.LER_CODIGO_DO_COMPONENTE
        val pedePublicarPagina =
            texto.contains("publicar_pagina") ||
                (
                    Regex("""\b(publica|publicar)\b""").containsMatchIn(texto) &&
                        (
                            texto.contains("página") || texto.contains("pagina") ||
                                texto.contains("html") || falaDaColecao
                            )
                    ) ||
                (
                    texto.contains("html") && falaDaColecao &&
                        (
                            texto.contains("cria") || texto.contains("criar") ||
                                texto.contains("montar") || texto.contains("monta ") ||
                                texto.contains("faz ") || texto.contains("fazer")
                            )
                    )
        if (pedePublicarPagina) {
            acoes += AcoesConfirmadasDoChatDaOpenRouter.PUBLICAR_PAGINA
        }
        return acoes
    }
}

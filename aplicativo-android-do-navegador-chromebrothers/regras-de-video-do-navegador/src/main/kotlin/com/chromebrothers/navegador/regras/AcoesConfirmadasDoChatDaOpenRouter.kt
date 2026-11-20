package com.chromebrothers.navegador.regras

object AcoesConfirmadasDoChatDaOpenRouter {
    const val ABRIR_ENDERECO = "abrir_endereco"
    const val CRIAR_ABA = "criar_aba"
    const val PREENCHER_CAMPO = "preencher_campo_de_texto"
    const val ESCREVER_NA_PAGINA = "escrever_na_pagina"
    const val GUARDAR_NOTA = "guardar_nota_do_site"
    const val CLICAR = "clicar_no_texto"
    const val VINCULAR_CAMPO = "vincular_campo_do_site"
    const val GERAR_MARKDOWN = "gerar_markdown"
    const val GERAR_ZIP = "gerar_zip"
    const val PUBLICAR_PAGINA = "publicar_pagina_da_colecao"
    const val MOSTRAR_MARKDOWN = "mostrar_markdown_na_colecao"
    const val GUARDAR_COMPONENTE = "guardar_componente_da_colecao"
    const val ABRIR_COMPONENTE = "abrir_componente_da_biblioteca"
    const val APAGAR_COMPONENTE = "apagar_componente_da_biblioteca"
    const val MOSTRAR_BOTAO_FLUTUANTE = "mostrar_botao_flutuante_do_video"
    const val ABRIR_PLAYER_DETECTADO = "abrir_player_do_video_detectado"
    const val ABRIR_VIDEO_DA_BIBLIOTECA = "abrir_video_da_biblioteca"
    const val ABRIR_IMAGEM_DA_BIBLIOTECA = "abrir_imagem_da_biblioteca"
    const val GERAR_IMAGEM_HIGGSFIELD = "gerar_imagem_higgsfield"
    const val GERAR_VIDEO_HIGGSFIELD = "gerar_video_higgsfield"
    const val GERAR_VIDEO_TOPVIEW = "gerar_video_topview"
    const val GERAR_VIDEO_VIVIX = "gerar_video_vivix"
    const val GUARDAR_ROTEIRO = "guardar_roteiro_de_automacao"
    const val RODAR_ROTEIRO = "rodar_roteiro_de_automacao"
    const val CLICAR_ELEMENTO = "clicar_elemento"
    const val PREENCHER_ELEMENTO = "preencher_elemento"
    const val APERTAR_TECLA = "apertar_tecla"
    const val LER_EM_SEGUNDO_PLANO = "ler_pagina_em_segundo_plano"
    const val EDITAR_PAGINA = "editar_pagina_da_colecao"
    val nomes = setOf(
        ABRIR_ENDERECO,
        CRIAR_ABA,
        PREENCHER_CAMPO,
        ESCREVER_NA_PAGINA,
        GUARDAR_NOTA,
        CLICAR,
        VINCULAR_CAMPO,
        GERAR_MARKDOWN,
        GERAR_ZIP,
        PUBLICAR_PAGINA,
        MOSTRAR_MARKDOWN,
        GUARDAR_COMPONENTE,
        ABRIR_COMPONENTE,
        APAGAR_COMPONENTE,
        MOSTRAR_BOTAO_FLUTUANTE,
        ABRIR_PLAYER_DETECTADO,
        ABRIR_VIDEO_DA_BIBLIOTECA,
        ABRIR_IMAGEM_DA_BIBLIOTECA,
        GERAR_IMAGEM_HIGGSFIELD,
        GERAR_VIDEO_HIGGSFIELD,
        GERAR_VIDEO_TOPVIEW,
        GERAR_VIDEO_VIVIX,
        GUARDAR_ROTEIRO,
        RODAR_ROTEIRO,
        CLICAR_ELEMENTO,
        PREENCHER_ELEMENTO,
        APERTAR_TECLA,
        LER_EM_SEGUNDO_PLANO,
        EDITAR_PAGINA,
    )
    const val LIMITE_DO_ENDERECO = 2000
    const val LIMITE_DO_TEXTO = 8000
    const val LIMITE_DO_ID_DA_PAGINA = 80
    const val LIMITE_DA_CHAVE_DO_VIDEO = 240

    fun definicoes(): List<String> = listOf(
        definicao(
            ABRIR_ENDERECO,
            "Pede para abrir um endereço http ou https na aba atual.",
            """{"endereco":{"type":"string"}}""",
            """["endereco"]""",
        ),
        definicao(
            CRIAR_ABA,
            "Pede para criar uma aba. Sem endereço, a aba fica em branco.",
            """{"endereco":{"type":"string"}}""",
            "[]",
        ),
        definicao(
            PREENCHER_CAMPO,
            "Pede para preencher o campo de texto já vinculado na página (ex.: título). Não envia o formulário.",
            """{"texto":{"type":"string"}}""",
            """["texto"]""",
        ),
        definicao(
            ESCREVER_NA_PAGINA,
            "Localiza um editor digitável na página aberta (caixa de texto, contenteditable ou editor tipo Google Docs, se exposto), foca nele e escreve o texto. Não depende do campo vinculado nem do foco do chat. Use quando pedirem digitar ou escrever no documento/página. Se não houver editor acessível, a ferramenta avisa.",
            """{"texto":{"type":"string"}}""",
            """["texto"]""",
        ),
        definicao(
            GUARDAR_NOTA,
            "Pede para guardar uma nota sobre como usar o site aberto.",
            """{"texto":{"type":"string"},"site":{"type":"string"}}""",
            """["texto"]""",
        ),
        definicao(
            CLICAR,
            "Pede para clicar num texto visível da página, como um menu. Um clique por vez.",
            """{"texto":{"type":"string"}}""",
            """["texto"]""",
        ),
        definicao(
            VINCULAR_CAMPO,
            "Pede para gravar a caixa de texto e o botão de envio deste site. Copie id, nome, rotulo, tag e papel da lista de campos. Pode mandar só a caixa, só o botão, ou os dois.",
            """{"campo":{"type":"object","properties":{"id":{"type":"string"},"nome":{"type":"string"},"rotulo":{"type":"string"},"tag":{"type":"string"},"papel":{"type":"string"}}},"envio":{"type":"object","properties":{"id":{"type":"string"},"nome":{"type":"string"},"rotulo":{"type":"string"},"tag":{"type":"string"},"papel":{"type":"string"}}}}""",
            "[]",
        ),
        definicao(
            GERAR_MARKDOWN,
            "Gera um arquivo markdown (.md) com pasta assets para imagens. Passe titulo, conteudo e, se quiser, imagens com endereco http https ou file e nome do arquivo. O app baixa as imagens e salva em Downloads.",
            """{"titulo":{"type":"string"},"conteudo":{"type":"string"},"imagens":{"type":"array","items":{"type":"object","properties":{"endereco":{"type":"string"},"nome":{"type":"string"}},"required":["endereco"]}}}""",
            """["titulo","conteudo"]""",
        ),
        definicao(
            GERAR_ZIP,
            "Gera um arquivo ZIP com imagens, videos ou outros arquivos. Passe nome, itens com endereco e caminho_no_zip, e se quiser conteudo_markdown para incluir um .md no pacote.",
            """{"nome":{"type":"string"},"itens":{"type":"array","items":{"type":"object","properties":{"endereco":{"type":"string"},"caminho_no_zip":{"type":"string"}},"required":["endereco"]}},"conteudo_markdown":{"type":"string"}}""",
            """["nome"]""",
        ),
        definicao(
            PUBLICAR_PAGINA,
            "Mostra uma página HTML numa janela flutuante da coleção (até 3 janelas). Na mesma resposta, publicar de novo atualiza a mesma janela; ela aparece ao usuário quando você terminar. Minimizar vira bolha. Se já houver 3, a ferramenta recusa. Use isto para vídeos, imagens e qualquer layout. Passe titulo e html com CSS e JavaScript local se quiser. Pode usar chromebrothers.gerarPdf('nome') num botão para salvar PDF igual à página. Sem script remoto nem fetch. Imagens http https ou file:// (use o file:// devolvido por capturar_aba).",
            """{"titulo":{"type":"string"},"html":{"type":"string"}}""",
            """["titulo","html"]""",
        ),
        definicao(
            MOSTRAR_MARKDOWN,
            "Mostra plano, relato ou explicação em markdown na coleção (leitor HTML) e grava o .md em Downloads/ChromeBrothers/markdown. Passe titulo e conteudo markdown. Não use para UI interativa (botões, vídeo, formulário): nesse caso use publicar_pagina_da_colecao.",
            """{"titulo":{"type":"string"},"conteudo":{"type":"string"}}""",
            """["titulo","conteudo"]""",
        ),
        definicao(
            GUARDAR_COMPONENTE,
            "Pede para guardar na biblioteca o componente HTML que está na coleção agora. Se ele veio da biblioteca, atualiza esse item. Pode passar descricao e tags. publicar_pagina_da_colecao não grava sozinho.",
            """{"descricao":{"type":"string"},"tags":{"type":"array","items":{"type":"string"}}}""",
            "[]",
        ),
        definicao(
            ABRIR_COMPONENTE,
            "Pede para abrir um componente da biblioteca na coleção flutuante. Passe id (preferencial) ou nome.",
            """{"id":{"type":"string"},"nome":{"type":"string"}}""",
            "[]",
        ),
        definicao(
            APAGAR_COMPONENTE,
            "Pede para apagar um componente da biblioteca pelo id. O componente aberto na coleção permanece; só some o vínculo.",
            """{"id":{"type":"string"}}""",
            """["id"]""",
        ),
        definicao(
            MOSTRAR_BOTAO_FLUTUANTE,
            "Mostra o botão flutuante do vídeo já detectado na aba, sem abrir o player. Não recebe argumentos. Se não houver vídeo, recusa.",
            "{}",
            "[]",
        ),
        definicao(
            ABRIR_PLAYER_DETECTADO,
            "Abre no player o vídeo já detectado na aba, o mesmo caminho do toque no botão flutuante. Não recebe argumentos. Não publica página na coleção.",
            "{}",
            "[]",
        ),
        definicao(
            ABRIR_VIDEO_DA_BIBLIOTECA,
            "Abre um vídeo da biblioteca nativa pela chave devolvida em listar_videos_da_biblioteca.",
            """{"chave":{"type":"string"}}""",
            """["chave"]""",
        ),
        definicao(
            ABRIR_IMAGEM_DA_BIBLIOTECA,
            "Abre uma imagem da biblioteca nativa no visor ampliado, pelo id devolvido em listar_imagens_da_biblioteca.",
            """{"id":{"type":"string"}}""",
            """["id"]""",
        ),
        definicao(
            GERAR_IMAGEM_HIGGSFIELD,
            "Gera uma imagem pela conta Higgsfield ligada no aparelho. Informe prompt; modelo é opcional (ex.: Nano Banana 2.1 ou id do catálogo). Para referência (img2img): se o usuário anexou a foto no chat, passe id_imagem com o valor anexo (não use listar_midia_da_pagina para achar anexo). Se a foto já está na biblioteca, use o id de listar_imagens_da_biblioteca. role_referencia é opcional (padrão image_references no Nano Banana 2.x / GPT Image 2.6, senão image). Prefira listar_modelos_higgsfield se o usuário perguntar quais modelos existem. Consome créditos. Sem conta ligada, a ferramenta avisa para ligar na engrenagem.",
            """{"prompt":{"type":"string"},"modelo":{"type":"string"},"id_imagem":{"type":"string"},"role_referencia":{"type":"string"}}""",
            """["prompt"]""",
        ),
        definicao(
            GERAR_VIDEO_HIGGSFIELD,
            "Gera um vídeo pela conta Higgsfield ligada no aparelho. Informe prompt; modelo é opcional (ex.: Wan 3 ou id do catálogo). Prefira listar_modelos_higgsfield se o usuário perguntar quais modelos existem. Consome créditos. Sem conta ligada, a ferramenta avisa para ligar na engrenagem.",
            """{"prompt":{"type":"string"},"modelo":{"type":"string"}}""",
            """["prompt"]""",
        ),
        definicao(
            GERAR_VIDEO_TOPVIEW,
            "Gera um vídeo pela conta Topview ligada no aparelho (MCP OAuth). Informe prompt; modelo é opcional (display name ou id, ex.: Wan 3.0 / wan-3-0 — não confundir com Wan 3.0 Prime). Consome créditos Topview. Sem conta ligada, a ferramenta avisa para ligar na engrenagem.",
            """{"prompt":{"type":"string"},"modelo":{"type":"string"}}""",
            """["prompt"]""",
        ),
        definicao(
            GERAR_VIDEO_VIVIX,
            "Gera um vídeo pelo Vivix W1 (API com token guardado no aparelho). Informe prompt (obrigatório, descreva cena, câmera e movimento). Opcionais: modelo (vivix-w1 padrão e mais barato ≈1,7 créd/s; vivix-w1-fast ≈5,3; vivix-w1-ultrafast ≈9,3), duracao 5–15 s (padrão 5), resolucao 720p (padrão) ou 1080p, proporcao 16:9 (padrão), 9:16, 1:1, 4:3 ou 3:4. Consome créditos W. Sem token, a ferramenta avisa para colar na engrenagem. Referências de imagem/vídeo: só no componente Geração MCP — vídeo da coleção.",
            """{"prompt":{"type":"string"},"modelo":{"type":"string","enum":["vivix-w1","vivix-w1-fast","vivix-w1-ultrafast"]},"duracao":{"type":"integer","minimum":5,"maximum":15},"resolucao":{"type":"string","enum":["720p","1080p"]},"proporcao":{"type":"string","enum":["16:9","9:16","1:1","4:3","3:4"]}}""",
            """["prompt"]""",
        ),
        definicao(
            GUARDAR_ROTEIRO,
            "Guarda um roteiro determinístico (passos estilo Playwright) para repetir depois sem IA. Leia antes ler_manual com topico roteiros: lá estão as ações, variáveis, ramificação A/B e exemplos.",
            """{"nome":{"type":"string"},"id":{"type":"string"},"passos":{"type":"array","items":{"type":"object","properties":{"acao":{"type":"string","description":"abrir|aguardar|aguardar_sumir|aguardar_url|aguardar_qualquer|marcar|ir_para|clicar|preencher|esperar_ms|rolar|clicar_se|assertar|extrair"},"url":{"type":"string"},"seletor":{"type":"string"},"texto":{"type":"string"},"valor":{"type":"string"},"timeout_ms":{"type":"integer"},"ms":{"type":"integer"},"pixels":{"type":"integer"},"indice":{"type":"integer"},"contem":{"type":"string"},"se_nao":{"type":"string"},"variavel":{"type":"string","description":"extrair ou aguardar_qualquer (padrao _ramo)"},"opcoes":{"type":"array","description":"aguardar_qualquer: 2 a 4 ramos","items":{"type":"object","properties":{"id":{"type":"string"},"seletor":{"type":"string"},"texto":{"type":"string"}},"required":["id"]}},"destino":{"type":"string","description":"ir_para: id de um marcar"},"passo":{"type":"integer","description":"ir_para: indice 1-based (alternativa a destino)"},"se":{"type":"string","description":"ir_para: condicao ex. {{_ramo}}=livre; vazio=sempre"},"id":{"type":"string","description":"marcar: id da ancora"}},"required":["acao"]}}}""",
            """["nome","passos"]""",
        ),
        definicao(
            RODAR_ROTEIRO,
            "Executa um roteiro já guardado (sem chamar o modelo). Passe id ou nome. Prefira listar_roteiros_de_automacao se não souber o id; use ler_roteiro_de_automacao para ver o JSON sem rodar.",
            """{"id":{"type":"string"},"nome":{"type":"string"}}""",
            "[]",
        ),
        definicao(
            CLICAR_ELEMENTO,
            "Clica num elemento da aba pelo seletor CSS (o que consultar_elementos devolve). Use para botões só com ícone, itens repetidos ou quando clicar_no_texto não acha. indice escolhe entre vários (começa em 0).",
            """{"seletor":{"type":"string"},"indice":{"type":"integer","minimum":0}}""",
            """["seletor"]""",
        ),
        definicao(
            PREENCHER_ELEMENTO,
            "Preenche um campo da aba pelo seletor CSS: input, textarea, contenteditable, select (valor ou texto da opção) e checkbox/radio (valor sim/não). Não envia o formulário; para enviar, use apertar_tecla Enter ou clicar_elemento no botão.",
            """{"seletor":{"type":"string"},"valor":{"type":"string"},"indice":{"type":"integer","minimum":0}}""",
            """["seletor","valor"]""",
        ),
        definicao(
            APERTAR_TECLA,
            "Aperta uma tecla na aba: no elemento do seletor (se passar) ou no que estiver com foco. Enter num campo de formulário envia o formulário.",
            """{"tecla":{"type":"string","enum":["Enter","Escape","Tab","ArrowDown","ArrowUp","ArrowLeft","ArrowRight","Backspace","Space"]},"seletor":{"type":"string"},"indice":{"type":"integer","minimum":0}}""",
            """["tecla"]""",
        ),
        definicao(
            LER_EM_SEGUNDO_PLANO,
            "Lê o texto de outra página numa aba escondida, sem mudar a aba do usuário. Use para pesquisar ou comparar sites. Devolve até 8000 caracteres por vez; para continuar, passe offset.",
            """{"endereco":{"type":"string"},"offset":{"type":"integer","minimum":0}}""",
            """["endereco"]""",
        ),
        definicao(
            EDITAR_PAGINA,
            "Edita a página aberta na coleção trocando trechos exatos do HTML, sem reenviar tudo. Cada troca: de (trecho que aparece uma única vez) e para. Tudo ou nada. titulo escolhe a janela (padrão: a em foco). Para mexer num componente guardado: abrir_componente_da_biblioteca, editar_pagina_da_colecao e guardar_componente_da_colecao.",
            """{"trocas":{"type":"array","items":{"type":"object","properties":{"de":{"type":"string"},"para":{"type":"string"}},"required":["de","para"]}},"titulo":{"type":"string"}}""",
            """["trocas"]""",
        ),
    )

    fun interpretar(nome: String, argumentos: String): ResultadoDaInterpretacaoDaAcaoDoChat {
        return when (nome) {
            ABRIR_ENDERECO -> enderecoPedido(argumentos, obrigatorio = true) { endereco ->
                PedidoDeAcaoConfirmadaDoChat.AbrirEndereco(endereco)
            }
            CRIAR_ABA -> enderecoPedido(argumentos, obrigatorio = false) { endereco ->
                PedidoDeAcaoConfirmadaDoChat.CriarAba(endereco)
            }
            PREENCHER_CAMPO -> {
                val texto = TextoJsonDoChat.valorDaChave(argumentos, "texto").orEmpty()
                when {
                    texto.isEmpty() -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltou o texto.")
                    texto.length > LIMITE_DO_TEXTO -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("O texto passou do limite.")
                    else -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(PedidoDeAcaoConfirmadaDoChat.PreencherCampo(texto))
                }
            }
            ESCREVER_NA_PAGINA -> {
                val texto = TextoJsonDoChat.valorDaChave(argumentos, "texto").orEmpty()
                when {
                    texto.isEmpty() -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltou o texto.")
                    texto.length > LIMITE_DO_TEXTO -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("O texto passou do limite.")
                    else -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(PedidoDeAcaoConfirmadaDoChat.EscreverNaPagina(texto))
                }
            }
            GUARDAR_NOTA -> {
                val texto = TextoJsonDoChat.valorDaChave(argumentos, "texto").orEmpty()
                val site = TextoJsonDoChat.valorDaChave(argumentos, "site")?.trim().orEmpty()
                when {
                    texto.isBlank() -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltou o texto.")
                    texto.length > CadernoDeNotasDosSitesParaOChat.LIMITE_DO_TEXTO ->
                        ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("O texto passou do limite.")
                    site.isNotEmpty() && !esquemaPermitido(site) ->
                        ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Só abro endereços http ou https.")
                    else -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                        PedidoDeAcaoConfirmadaDoChat.GuardarNota(site, texto.trim()),
                    )
                }
            }
            CLICAR -> {
                val texto = TextoJsonDoChat.valorDaChave(argumentos, "texto")?.trim().orEmpty()
                when {
                    texto.isEmpty() -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltou o texto.")
                    texto.length > CliqueNoTextoVisivelDaPaginaParaOChat.LIMITE ->
                        ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("O texto do clique passou do limite.")
                    else -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(PedidoDeAcaoConfirmadaDoChat.Clicar(texto))
                }
            }
            VINCULAR_CAMPO -> when (val vinculo = VinculoDoCampoParaOChat.ler(argumentos)) {
                is ResultadoDoVinculoPedidoPeloChat.Recusado ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada(vinculo.mensagem)
                is ResultadoDoVinculoPedidoPeloChat.Pronto -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                    PedidoDeAcaoConfirmadaDoChat.VincularCampo(vinculo.campo, vinculo.envio),
                )
            }
            GERAR_MARKDOWN -> when (val geracao = GeracaoDeArquivoPeloChatDaOpenRouter.lerMarkdown(argumentos)) {
                is ResultadoDaGeracaoDeMarkdownDoChat.Recusado ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada(geracao.mensagem)
                is ResultadoDaGeracaoDeMarkdownDoChat.Pronto ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(geracao.pedido)
            }
            GERAR_ZIP -> when (val geracao = GeracaoDeArquivoPeloChatDaOpenRouter.lerZip(argumentos)) {
                is ResultadoDaGeracaoDeZipDoChat.Recusado ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada(geracao.mensagem)
                is ResultadoDaGeracaoDeZipDoChat.Pronto ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(geracao.pedido)
            }
            PUBLICAR_PAGINA -> when (val pagina = PublicacaoDaPaginaDaColecaoFlutuanteDoChat.ler(argumentos)) {
                is ResultadoDaPublicacaoDaPaginaDaColecao.Recusada ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada(pagina.mensagem)
                is ResultadoDaPublicacaoDaPaginaDaColecao.Pronta ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                        PedidoDeAcaoConfirmadaDoChat.PublicarPaginaDaColecao(pagina.titulo, pagina.html),
                    )
            }
            MOSTRAR_MARKDOWN -> when (val leitor = LeitorDeMarkdownNaColecaoDoNavegador.lerPedido(argumentos)) {
                is ResultadoDoLeitorDeMarkdownNaColecao.Recusado ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada(leitor.mensagem)
                is ResultadoDoLeitorDeMarkdownNaColecao.Pronto ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(leitor.pedido)
            }
            GUARDAR_COMPONENTE -> interpretarGuardarComponente(argumentos)
            ABRIR_COMPONENTE -> interpretarAberturaDoComponente(argumentos)
            MOSTRAR_BOTAO_FLUTUANTE -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                PedidoDeAcaoConfirmadaDoChat.MostrarBotaoFlutuanteDoVideo(),
            )
            ABRIR_PLAYER_DETECTADO -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                PedidoDeAcaoConfirmadaDoChat.AbrirPlayerDoVideoDetectado(),
            )
            ABRIR_VIDEO_DA_BIBLIOTECA -> {
                val chave = TextoJsonDoChat.valorDaChave(argumentos, "chave")?.trim().orEmpty()
                when {
                    chave.isEmpty() -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltou a chave.")
                    chave.length > LIMITE_DA_CHAVE_DO_VIDEO ->
                        ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("A chave passou do limite.")
                    else -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                        PedidoDeAcaoConfirmadaDoChat.AbrirVideoDaBiblioteca(chave),
                    )
                }
            }
            ABRIR_IMAGEM_DA_BIBLIOTECA -> {
                val id = TextoJsonDoChat.valorDaChave(argumentos, "id")?.trim().orEmpty()
                when {
                    id.isEmpty() -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltou o id.")
                    id.length > LIMITE_DO_ID_DA_PAGINA ->
                        ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("O id passou do limite.")
                    else -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                        PedidoDeAcaoConfirmadaDoChat.AbrirImagemDaBiblioteca(id),
                    )
                }
            }
            APAGAR_COMPONENTE -> {
                val id = TextoJsonDoChat.valorDaChave(argumentos, "id")?.trim().orEmpty()
                when {
                    id.isEmpty() -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltou o id.")
                    id.length > LIMITE_DO_ID_DA_PAGINA ->
                        ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("O id passou do limite.")
                    else -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                        PedidoDeAcaoConfirmadaDoChat.ApagarComponenteDaBiblioteca(id),
                    )
                }
            }
            GERAR_IMAGEM_HIGGSFIELD -> when (
                val geracao = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerImagem(argumentos)
            ) {
                is ResultadoDaGeracaoHiggsfieldDoChat.Recusado ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada(geracao.mensagem)
                is ResultadoDaGeracaoHiggsfieldDoChat.ProntoImagem ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                        PedidoDeAcaoConfirmadaDoChat.GerarImagemHiggsfield(
                            geracao.prompt,
                            geracao.modelo,
                            geracao.idImagem,
                            geracao.roleReferencia,
                        ),
                    )
                is ResultadoDaGeracaoHiggsfieldDoChat.ProntoVideo ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Pedido de imagem inválido.")
            }
            GERAR_VIDEO_HIGGSFIELD -> when (
                val geracao = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerVideo(argumentos)
            ) {
                is ResultadoDaGeracaoHiggsfieldDoChat.Recusado ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada(geracao.mensagem)
                is ResultadoDaGeracaoHiggsfieldDoChat.ProntoVideo ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                        PedidoDeAcaoConfirmadaDoChat.GerarVideoHiggsfield(geracao.prompt, geracao.modelo),
                    )
                is ResultadoDaGeracaoHiggsfieldDoChat.ProntoImagem ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Pedido de vídeo inválido.")
            }
            GERAR_VIDEO_TOPVIEW -> when (
                val geracao = GeracaoDeVideoPeloTopviewNoChat.lerVideo(argumentos)
            ) {
                is ResultadoDaGeracaoTopviewDoChat.Recusado ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada(geracao.mensagem)
                is ResultadoDaGeracaoTopviewDoChat.Pronto ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                        PedidoDeAcaoConfirmadaDoChat.GerarVideoTopview(geracao.prompt, geracao.modelo),
                    )
            }
            GERAR_VIDEO_VIVIX -> when (val geracao = GeracaoDeVideoPeloVivixNoChat.lerVideo(argumentos)) {
                is ResultadoDaGeracaoVivixDoChat.Recusado -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada(geracao.mensagem)
                is ResultadoDaGeracaoVivixDoChat.Pronto -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(geracao.pedido)
            }
            GUARDAR_ROTEIRO -> when (val r = RoteiroDeAutomacaoDaPaginaDoNavegador.ler(argumentos)) {
                is ResultadoDoRoteiroDeAutomacao.Erro ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada(r.mensagem)
                is ResultadoDoRoteiroDeAutomacao.Ok ->
                    ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                        PedidoDeAcaoConfirmadaDoChat.GuardarRoteiroDeAutomacao(r.roteiro),
                    )
            }
            RODAR_ROTEIRO -> {
                val id = TextoJsonDoChat.valorDaChave(argumentos, "id")?.trim().orEmpty()
                val nome = TextoJsonDoChat.valorDaChave(argumentos, "nome")?.trim().orEmpty()
                val chave = id.ifBlank { nome }
                when {
                    chave.isEmpty() -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltou id ou nome do roteiro.")
                    chave.length > LIMITE_DO_ID_DA_PAGINA ->
                        ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Id/nome passou do limite.")
                    else -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                        PedidoDeAcaoConfirmadaDoChat.RodarRoteiroDeAutomacao(chave),
                    )
                }
            }
            CLICAR_ELEMENTO, PREENCHER_ELEMENTO, APERTAR_TECLA -> interpretarElemento(nome, argumentos)
            LER_EM_SEGUNDO_PLANO -> {
                val offset = TextoJsonDoChat.inteiroDaChave(argumentos, "offset")?.coerceAtLeast(0) ?: 0
                enderecoPedido(argumentos, obrigatorio = true) { endereco ->
                    PedidoDeAcaoConfirmadaDoChat.LerPaginaEmSegundoPlano(endereco, offset)
                }
            }
            EDITAR_PAGINA -> {
                val trocas = EdicaoPorTrechosDoHtml.ler(argumentos)
                val titulo = TextoJsonDoChat.valorDaChave(argumentos, "titulo")?.trim().orEmpty()
                if (trocas.isEmpty()) {
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltaram as trocas (lista com de e para).")
                } else {
                    ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(PedidoDeAcaoConfirmadaDoChat.EditarPaginaDaColecao(trocas, titulo))
                }
            }
            else -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Ferramenta indisponível.")
        }
    }

    private fun interpretarElemento(nome: String, argumentos: String): ResultadoDaInterpretacaoDaAcaoDoChat {
        val seletor = TextoJsonDoChat.valorDaChave(argumentos, "seletor")?.trim().orEmpty()
        val indice = TextoJsonDoChat.inteiroDaChave(argumentos, "indice")?.coerceAtLeast(0) ?: 0
        if (seletor.length > ElementosDaPaginaPeloSeletorParaOChat.LIMITE_DO_SELETOR) {
            return ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("O seletor passou do limite.")
        }
        return when (nome) {
            CLICAR_ELEMENTO ->
                if (seletor.isEmpty()) ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltou o seletor.")
                else ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(PedidoDeAcaoConfirmadaDoChat.ClicarElemento(seletor, indice))
            PREENCHER_ELEMENTO -> {
                val valor = TextoJsonDoChat.valorDaChave(argumentos, "valor") ?: ""
                when {
                    seletor.isEmpty() -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltou o seletor.")
                    valor.length > ElementosDaPaginaPeloSeletorParaOChat.LIMITE_DO_VALOR ->
                        ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("O valor passou do limite.")
                    else -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                        PedidoDeAcaoConfirmadaDoChat.PreencherElemento(seletor, valor, indice),
                    )
                }
            }
            else -> {
                val tecla = TextoJsonDoChat.valorDaChave(argumentos, "tecla")?.trim().orEmpty()
                if (tecla !in ElementosDaPaginaPeloSeletorParaOChat.teclas) {
                    ResultadoDaInterpretacaoDaAcaoDoChat.Recusada(
                        "Tecla não suportada. Use: ${ElementosDaPaginaPeloSeletorParaOChat.teclas.joinToString(", ")}.",
                    )
                } else {
                    ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(PedidoDeAcaoConfirmadaDoChat.ApertarTecla(tecla, seletor, indice))
                }
            }
        }
    }

    fun pergunta(pedido: PedidoDeAcaoConfirmadaDoChat): String = when (pedido) {
        is PedidoDeAcaoConfirmadaDoChat.AbrirEndereco -> "Abrir na aba atual?\n${pedido.endereco}"
        is PedidoDeAcaoConfirmadaDoChat.CriarAba ->
            if (pedido.endereco.isBlank()) "Criar uma aba em branco?" else "Criar uma aba?\n${pedido.endereco}"
        is PedidoDeAcaoConfirmadaDoChat.PreencherCampo ->
            "Preencher o campo da página?\n${pedido.texto.take(160)}"
        is PedidoDeAcaoConfirmadaDoChat.EscreverNaPagina ->
            "Escrever isto no editor da página?\n${pedido.texto.take(160)}"
        is PedidoDeAcaoConfirmadaDoChat.GuardarNota -> {
            val onde = if (pedido.site.isBlank()) "no site desta aba" else "em ${pedido.site}"
            "Guardar esta nota $onde?\n${pedido.texto.take(160)}"
        }
        is PedidoDeAcaoConfirmadaDoChat.Clicar -> "Clicar em ${pedido.texto}?"
        is PedidoDeAcaoConfirmadaDoChat.VincularCampo ->
            VinculoDoCampoParaOChat.pergunta(pedido.campo, pedido.envio)
        is PedidoDeAcaoConfirmadaDoChat.GerarMarkdown ->
            GeracaoDeArquivoPeloChatDaOpenRouter.perguntaMarkdown(pedido)
        is PedidoDeAcaoConfirmadaDoChat.GerarZip ->
            GeracaoDeArquivoPeloChatDaOpenRouter.perguntaZip(pedido)
        is PedidoDeAcaoConfirmadaDoChat.PublicarPaginaDaColecao ->
            PublicacaoDaPaginaDaColecaoFlutuanteDoChat.pergunta(pedido.titulo)
        is PedidoDeAcaoConfirmadaDoChat.MostrarMarkdownNaColecao ->
            LeitorDeMarkdownNaColecaoDoNavegador.pergunta(pedido)
        is PedidoDeAcaoConfirmadaDoChat.GuardarComponenteDaColecao ->
            "Guardar o componente da coleção na biblioteca?"
        is PedidoDeAcaoConfirmadaDoChat.AbrirComponenteDaBiblioteca ->
            if (pedido.id.isNotBlank()) {
                "Abrir o componente ${pedido.id} da biblioteca?"
            } else {
                "Abrir \"${pedido.nome}\" da biblioteca?"
            }
        is PedidoDeAcaoConfirmadaDoChat.ApagarComponenteDaBiblioteca ->
            "Apagar o componente ${pedido.id} da biblioteca?"
        is PedidoDeAcaoConfirmadaDoChat.MostrarBotaoFlutuanteDoVideo ->
            "Mostrar o botão flutuante do vídeo?"
        is PedidoDeAcaoConfirmadaDoChat.AbrirPlayerDoVideoDetectado ->
            "Abrir o player do vídeo detectado?"
        is PedidoDeAcaoConfirmadaDoChat.AbrirVideoDaBiblioteca ->
            "Abrir o vídeo ${pedido.chave} da biblioteca?"
        is PedidoDeAcaoConfirmadaDoChat.AbrirImagemDaBiblioteca ->
            "Abrir a imagem ${pedido.id} no visor?"
        is PedidoDeAcaoConfirmadaDoChat.GerarImagemHiggsfield ->
            GeracaoDeImagemEVideoPeloHiggsfieldNoChat.perguntaImagem(
                pedido.prompt,
                pedido.modelo,
                pedido.idImagem,
                pedido.roleReferencia,
            )
        is PedidoDeAcaoConfirmadaDoChat.GerarVideoHiggsfield ->
            GeracaoDeImagemEVideoPeloHiggsfieldNoChat.perguntaVideo(pedido.prompt, pedido.modelo)
        is PedidoDeAcaoConfirmadaDoChat.GerarVideoTopview ->
            GeracaoDeVideoPeloTopviewNoChat.perguntaVideo(pedido.prompt, pedido.modelo)
        is PedidoDeAcaoConfirmadaDoChat.GerarVideoVivix ->
            GeracaoDeVideoPeloVivixNoChat.perguntaVideo(pedido)
        is PedidoDeAcaoConfirmadaDoChat.GuardarRoteiroDeAutomacao ->
            "Guardar o roteiro «${pedido.roteiro.nome}» com ${pedido.roteiro.passos.size} passo(s)?"
        is PedidoDeAcaoConfirmadaDoChat.RodarRoteiroDeAutomacao ->
            "Rodar o roteiro «${pedido.idOuNome}» na página?"
        is PedidoDeAcaoConfirmadaDoChat.ClicarElemento ->
            "Clicar no elemento ${pedido.seletor}" + (if (pedido.indice > 0) " (nº ${pedido.indice})" else "") + "?"
        is PedidoDeAcaoConfirmadaDoChat.PreencherElemento ->
            "Preencher ${pedido.seletor}?\n${pedido.valor.take(160)}"
        is PedidoDeAcaoConfirmadaDoChat.ApertarTecla ->
            "Apertar ${pedido.tecla}" + (if (pedido.seletor.isNotBlank()) " em ${pedido.seletor}" else "") + "?"
        is PedidoDeAcaoConfirmadaDoChat.LerPaginaEmSegundoPlano ->
            "Ler em segundo plano (sua aba não muda)?\n${pedido.endereco}"
        is PedidoDeAcaoConfirmadaDoChat.EditarPaginaDaColecao ->
            "Editar a página da coleção (${pedido.trocas.size} troca(s))?"
    }

    private fun interpretarGuardarComponente(argumentos: String): ResultadoDaInterpretacaoDaAcaoDoChat {
        val descricao = if (chavePresente(argumentos, "descricao")) {
            TextoJsonDoChat.valorDaChave(argumentos, "descricao")?.trim().orEmpty()
        } else {
            null
        }
        if (descricao != null && descricao.length > BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DA_DESCRICAO) {
            return ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("A descrição passou do limite.")
        }
        val tags = when (val lidas = tagsDoArgumento(argumentos)) {
            null -> null
            is ResultadoDasTagsDoComponente.Recusada ->
                return ResultadoDaInterpretacaoDaAcaoDoChat.Recusada(lidas.mensagem)
            is ResultadoDasTagsDoComponente.Ok -> lidas.tags
        }
        return ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
            PedidoDeAcaoConfirmadaDoChat.GuardarComponenteDaColecao(descricao, tags),
        )
    }

    private fun tagsDoArgumento(argumentos: String): ResultadoDasTagsDoComponente? {
        if (!chavePresente(argumentos, "tags")) return null
        val lista = TextoJsonDoChat.textosDaChave(argumentos, "tags")
        if (lista != null) return BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.limparTags(lista)
        val texto = TextoJsonDoChat.valorDaChave(argumentos, "tags")
        if (texto != null) {
            return BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.limparTags(texto.split(','))
        }
        return ResultadoDasTagsDoComponente.Recusada("As tags não servem.")
    }

    private fun chavePresente(json: String, chave: String): Boolean {
        val marca = "\"$chave\""
        var indice = 0
        while (indice < json.length) {
            val achou = json.indexOf(marca, indice)
            if (achou < 0) return false
            var cursor = achou + marca.length
            while (cursor < json.length && json[cursor].isWhitespace()) cursor++
            if (cursor < json.length && json[cursor] == ':') return true
            indice = achou + 1
        }
        return false
    }

    private fun interpretarAberturaDoComponente(argumentos: String): ResultadoDaInterpretacaoDaAcaoDoChat {
        val id = TextoJsonDoChat.valorDaChave(argumentos, "id")?.trim().orEmpty()
        val nome = TextoJsonDoChat.valorDaChave(argumentos, "nome")?.trim().orEmpty()
        return when {
            id.isEmpty() && nome.isEmpty() ->
                ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltou o id ou o nome.")
            id.length > LIMITE_DO_ID_DA_PAGINA ->
                ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("O id passou do limite.")
            nome.length > BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_NOME ->
                ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("O nome passou do limite.")
            else -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(
                PedidoDeAcaoConfirmadaDoChat.AbrirComponenteDaBiblioteca(id, nome),
            )
        }
    }

    private fun enderecoPedido(
        argumentos: String,
        obrigatorio: Boolean,
        pronto: (String) -> PedidoDeAcaoConfirmadaDoChat,
    ): ResultadoDaInterpretacaoDaAcaoDoChat {
        val endereco = TextoJsonDoChat.valorDaChave(argumentos, "endereco")?.trim().orEmpty()
        return when {
            endereco.isEmpty() && obrigatorio -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Faltou o endereço.")
            endereco.length > LIMITE_DO_ENDERECO -> ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("O endereço passou do limite.")
            endereco.isNotEmpty() && !esquemaPermitido(endereco) ->
                ResultadoDaInterpretacaoDaAcaoDoChat.Recusada("Só abro endereços http ou https.")
            else -> ResultadoDaInterpretacaoDaAcaoDoChat.Pronta(pronto(endereco))
        }
    }

    fun esquemaPermitido(texto: String): Boolean {
        val marca = "://"
        val indice = texto.indexOf(marca)
        if (indice < 0) return true
        val esquema = texto.substring(0, indice).lowercase()
        return esquema == "http" || esquema == "https"
    }

    private fun definicao(nome: String, descricao: String, propriedades: String, obrigatorios: String): String {
        return """{"type":"function","function":{"name":"$nome","description":"${TextoJsonDoChat.escapar(descricao)}","parameters":{"type":"object","properties":$propriedades,"required":$obrigatorios}}}"""
    }
}

sealed class ResultadoDaInterpretacaoDaAcaoDoChat {
    data class Pronta(val pedido: PedidoDeAcaoConfirmadaDoChat) : ResultadoDaInterpretacaoDaAcaoDoChat()
    data class Recusada(val mensagem: String) : ResultadoDaInterpretacaoDaAcaoDoChat()
}

sealed class PedidoDeAcaoConfirmadaDoChat {
    data class AbrirEndereco(val endereco: String) : PedidoDeAcaoConfirmadaDoChat()
    data class CriarAba(val endereco: String) : PedidoDeAcaoConfirmadaDoChat()
    data class PreencherCampo(val texto: String) : PedidoDeAcaoConfirmadaDoChat()
    data class EscreverNaPagina(val texto: String) : PedidoDeAcaoConfirmadaDoChat()
    data class GuardarNota(val site: String, val texto: String) : PedidoDeAcaoConfirmadaDoChat()
    data class Clicar(val texto: String) : PedidoDeAcaoConfirmadaDoChat()
    data class VincularCampo(
        val campo: MarcaDoElementoVinculado?,
        val envio: MarcaDoElementoVinculado?,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class GerarMarkdown(
        val titulo: String,
        val conteudo: String,
        val imagens: List<ImagemDoMarkdownDoChat>,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class GerarZip(
        val nome: String,
        val itens: List<ItemDoZipDoChat>,
        val conteudoMarkdown: String?,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class PublicarPaginaDaColecao(
        val titulo: String,
        val html: String,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class MostrarMarkdownNaColecao(
        val titulo: String,
        val conteudo: String,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class GuardarComponenteDaColecao(
        val descricao: String? = null,
        val tags: List<String>? = null,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class AbrirComponenteDaBiblioteca(
        val id: String,
        val nome: String,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class ApagarComponenteDaBiblioteca(
        val id: String,
    ) : PedidoDeAcaoConfirmadaDoChat()
    class MostrarBotaoFlutuanteDoVideo : PedidoDeAcaoConfirmadaDoChat()
    class AbrirPlayerDoVideoDetectado : PedidoDeAcaoConfirmadaDoChat()
    data class AbrirVideoDaBiblioteca(
        val chave: String,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class AbrirImagemDaBiblioteca(
        val id: String,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class GerarImagemHiggsfield(
        val prompt: String,
        val modelo: String,
        val idImagem: String = "",
        val roleReferencia: String = "",
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class GerarVideoHiggsfield(
        val prompt: String,
        val modelo: String,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class GerarVideoTopview(
        val prompt: String,
        val modelo: String,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class GerarVideoVivix(
        val prompt: String,
        val modelo: String,
        val duracao: Int,
        val resolucao: String,
        val proporcao: String,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class GuardarRoteiroDeAutomacao(
        val roteiro: RoteiroDeAutomacaoDaPagina,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class RodarRoteiroDeAutomacao(
        val idOuNome: String,
    ) : PedidoDeAcaoConfirmadaDoChat()
    data class ClicarElemento(val seletor: String, val indice: Int) : PedidoDeAcaoConfirmadaDoChat()
    data class PreencherElemento(val seletor: String, val valor: String, val indice: Int) : PedidoDeAcaoConfirmadaDoChat()
    data class ApertarTecla(val tecla: String, val seletor: String, val indice: Int) : PedidoDeAcaoConfirmadaDoChat()
    data class LerPaginaEmSegundoPlano(val endereco: String, val offset: Int) : PedidoDeAcaoConfirmadaDoChat()
    data class EditarPaginaDaColecao(val trocas: List<TrocaDeTrechoDoHtml>, val titulo: String) : PedidoDeAcaoConfirmadaDoChat()
}

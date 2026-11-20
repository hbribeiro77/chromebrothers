package com.chromebrothers.navegador.regras

data class ParteDeConteudoDoChat(
    val tipo: String,
    val texto: String = "",
    val mime: String = "",
    val nome: String = "",
    val dados: String = "",
)

data class ChamadaDeFerramenta(
    val id: String,
    val nome: String,
    val argumentos: String,
)

data class PedacoDeChamadaDeFerramenta(
    val indice: Int,
    val id: String = "",
    val nome: String = "",
    val argumentos: String = "",
)

data class FimDoEnvioDoChat(
    val erro: String? = null,
    val chamadas: List<ChamadaDeFerramenta> = emptyList(),
    val uso: UsoDoChat? = null,
)

class AcumuladorDeChamadasDeFerramenta {
    private val itens = sortedMapOf<Int, MutableList<String>>()
    private val ids = sortedMapOf<Int, String>()
    private val nomes = sortedMapOf<Int, String>()

    fun acrescentar(pedaco: PedacoDeChamadaDeFerramenta) {
        if (pedaco.id.isNotBlank()) ids[pedaco.indice] = pedaco.id
        if (pedaco.nome.isNotBlank()) nomes[pedaco.indice] = pedaco.nome
        if (pedaco.argumentos.isNotEmpty()) {
            itens.getOrPut(pedaco.indice) { mutableListOf() }.add(pedaco.argumentos)
        } else {
            itens.getOrPut(pedaco.indice) { mutableListOf() }
        }
    }

    fun listar(): List<ChamadaDeFerramenta> {
        return itens.keys.map { indice ->
            ChamadaDeFerramenta(
                id = ids[indice].orEmpty().ifBlank { "chamada-$indice" },
                nome = nomes[indice].orEmpty(),
                argumentos = itens[indice].orEmpty().joinToString(""),
            )
        }
    }
}

object FerramentasDeLeituraDoChatDaOpenRouter {
    const val ENDERECO = "ler_endereco_e_titulo"
    const val TEXTO = "ler_texto_visivel"
    const val SELECAO = "ler_texto_selecionado"
    const val ABAS = "listar_abas"
    const val MIDIA = "listar_midia_da_pagina"
    const val ENDERECOS = "ler_enderecos_da_pagina"
    const val CAMPOS = "listar_campos_da_pagina"
    const val CAPTURAR_ABA = "capturar_aba"
    const val TEXTO_PAGINADO = "ler_texto_da_pagina"
    const val ELEMENTOS = "consultar_elementos"
    const val ROLAR = "rolar_pagina"
    const val COMPONENTES_DA_BIBLIOTECA = "listar_componentes_da_biblioteca"
    const val LER_CODIGO_DO_COMPONENTE = "ler_codigo_do_componente"
    const val CAPTURAR_COMPONENTE = "capturar_componente"
    const val LIMITE_DO_CODIGO_NO_CHAT = 100_000
    const val VIDEO_DETECTADO = "ler_video_detectado_na_aba"
    const val VIDEOS_DA_BIBLIOTECA = "listar_videos_da_biblioteca"
    const val IMAGENS_DA_BIBLIOTECA = "listar_imagens_da_biblioteca"
    const val MODELOS_HIGGSFIELD = "listar_modelos_higgsfield"
    const val ROTEIROS_DE_AUTOMACAO = "listar_roteiros_de_automacao"
    const val LER_ROTEIRO_DE_AUTOMACAO = "ler_roteiro_de_automacao"
    val permitidas = setOf(
        ENDERECO, TEXTO, SELECAO, ABAS, MIDIA, ENDERECOS, CAMPOS, CAPTURAR_ABA,
        TEXTO_PAGINADO, ELEMENTOS, ROLAR, COMPONENTES_DA_BIBLIOTECA,
        LER_CODIGO_DO_COMPONENTE, CAPTURAR_COMPONENTE,
        VIDEO_DETECTADO, VIDEOS_DA_BIBLIOTECA, IMAGENS_DA_BIBLIOTECA,
        MODELOS_HIGGSFIELD, ROTEIROS_DE_AUTOMACAO, LER_ROTEIRO_DE_AUTOMACAO,
        ManualDoAgenteDoChat.NOME,
    ) + AcoesConfirmadasDoChatDaOpenRouter.nomes + FerramentasDePinceisDoChat.nomes
    const val LIMITE_DE_RODADAS = 20

    /** [somenteLeitura]=true: modo chat, so consultas, sem acoes e sem abrir componente na colecao. */
    fun json(somenteLeitura: Boolean = false): String {
        val leitura = listOf(
            definicao(ENDERECO, "Le o titulo e o endereco da aba aberta no navegador."),
            definicao(TEXTO, "Le o texto visivel da aba aberta, ate 6000 caracteres. Para paginas grandes, prefira ler_texto_da_pagina (com offset) ou consultar_elementos."),
            definicao(SELECAO, "Le o texto que o usuario selecionou na aba aberta."),
            definicao(ABAS, "Lista as abas abertas e marca qual esta na frente."),
            definicao(MIDIA, "Lista vídeos e imagens da aba: o que a página pediu e o que está visível, com endereço http ou https."),
            definicao(
                COMPONENTES_DA_BIBLIOTECA,
                "Lista os componentes HTML guardados na biblioteca, com id, nome, descrição, tags e data. Não grava nem apaga.",
            ),
            definicaoComProps(
                LER_CODIGO_DO_COMPONENTE,
                "Le o HTML de um componente guardado na biblioteca, pelo id. Se o código for muito grande, devolve o começo e avisa o total.",
                """{"id":{"type":"string"}}""",
                """["id"]""",
            ),
            definicaoComProps(
                CAPTURAR_COMPONENTE,
                "Carrega um componente da biblioteca na coleção, se ainda não for esse, captura a WebView e devolve a imagem mais um endereço file://. Passe o id.",
                """{"id":{"type":"string"}}""",
                """["id"]""",
            ),
            definicao(
                VIDEO_DETECTADO,
                "Le o video detectado na aba aberta: endereco e se o botao flutuante pode aparecer. Nao abre o player.",
            ),
            definicao(
                VIDEOS_DA_BIBLIOTECA,
                "Lista videos recentes da biblioteca nativa, com chave, titulo e se abre no player de arquivo.",
            ),
            definicao(
                IMAGENS_DA_BIBLIOTECA,
                "Lista imagens da biblioteca nativa, com id e nome. Nao abre o visor.",
            ),
            definicaoComProps(
                MODELOS_HIGGSFIELD,
                "Lista modelos de imagem e vídeo disponíveis na conta Higgsfield ligada no aparelho (catálogo ao vivo). Filtro opcional (ex.: nanobanana, wan, seedance). Não gasta crédito. Use antes de gerar_imagem_higgsfield ou gerar_video_higgsfield quando o usuário perguntar quais modelos existem.",
                """{"filtro":{"type":"string"}}""",
                "[]",
            ),
            definicao(
                ROTEIROS_DE_AUTOMACAO,
                "Lista roteiros de automação guardados (id, nome, quantidade de passos). Use antes de ler_roteiro_de_automacao ou rodar_roteiro_de_automacao. Para criar um, use guardar_roteiro_de_automacao.",
            ),
            definicaoComProps(
                LER_ROTEIRO_DE_AUTOMACAO,
                "Le o JSON completo de um roteiro já guardado (passos, seletores, textos). Passe id ou nome. Use para aprender com um roteiro existente, copiar ou adaptar. Prefira listar_roteiros_de_automacao se não souber o id.",
                """{"id":{"type":"string"},"nome":{"type":"string"}}""",
                "[]",
            ),
            definicao(ENDERECOS, "Lista endereços http ou https visíveis na aba, como links e arquivos, quando a mídia da página não bastar."),
            definicao(CAMPOS, "Lista caixas de texto e botões visíveis na aba, com id, nome, rotulo, tag e papel, para vincular o campo do site."),
            definicao(
                CAPTURAR_ABA,
                "Captura a area visivel da aba aberta (screenshot), envia a imagem para voce ver o layout e devolve um endereco file:// local. Use esse endereco em publicar_pagina_da_colecao (img src). Use quando o texto nao bastar ou quando precisar reaproveitar a captura na webview da colecao.",
            ),
            definicaoComProps(
                TEXTO_PAGINADO,
                "Le o texto da pagina inteira em blocos. Passe offset (posicao inicial em caracteres) e limite (tamanho do bloco) para continuar de onde parou em paginas grandes.",
                """{"offset":{"type":"integer"},"limite":{"type":"integer"}}""",
                "[]",
            ),
            definicaoComProps(
                ELEMENTOS,
                "Consulta elementos da pagina por seletor CSS (ex.: a, h2, .card) e devolve texto e atributos de cada um. Use offset e limite para paginar o resultado. Util para listas longas e paginas grandes.",
                """{"seletor":{"type":"string"},"offset":{"type":"integer"},"limite":{"type":"integer"}}""",
                """["seletor"]""",
            ),
            definicaoComProps(
                ROLAR,
                "Rola a pagina para carregar conteudo sob demanda (lazy-load). Informe seletor para rolar ate um elemento, y para uma posicao, paginas para rolar N telas ou pixels para rolar um valor. Depois leia de novo o texto ou os elementos.",
                """{"seletor":{"type":"string"},"y":{"type":"integer"},"paginas":{"type":"number"},"pixels":{"type":"integer"}}""",
                "[]",
            ),
        )
        val comManual = leitura + ManualDoAgenteDoChat.definicao() + FerramentasDePinceisDoChat.definicoes()
        val todas = if (somenteLeitura) {
            comManual.filter { ModoConversaDoChat.podeUsar(nomeDaDefinicao(it)) }
        } else {
            comManual + AcoesConfirmadasDoChatDaOpenRouter.definicoes()
        }
        return todas.joinToString(prefix = "[", postfix = "]")
    }

    fun scriptDaSelecao(): String = """
        (function(){
          var s = window.getSelection ? String(window.getSelection()) : "";
          return s.replace(/\s+/g, " ").trim().slice(0, ${ContextoDaPaginaParaOChat.LIMITE});
        })()
    """.trimIndent()

    fun textoDaSelecao(texto: String): String {
        val limpo = texto.trim()
        return if (limpo.isEmpty()) "Nenhum texto selecionado." else limpo.take(ContextoDaPaginaParaOChat.LIMITE)
    }

    fun textoDosComponentesDaBiblioteca(itens: List<ItemDaBibliotecaDeComponentesHtmlDaColecao>): String {
        if (itens.isEmpty()) return "Nenhum componente na biblioteca."
        val formato = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(java.time.ZoneOffset.UTC)
        return itens.joinToString("\n") { item ->
            val data = formato.format(java.time.Instant.ofEpochMilli(item.atualizadoEm))
            val extras = buildList {
                if (item.descricao.isNotBlank()) add(item.descricao)
                if (item.tags.isNotEmpty()) add(item.tags.joinToString(", "))
            }
            val sufixo = if (extras.isEmpty()) "" else " — " + extras.joinToString(" — ")
            "- ${item.id} — ${item.nome} — $data$sufixo"
        }
    }

    fun textoDoCodigoDoComponente(html: String): String {
        if (html.length <= LIMITE_DO_CODIGO_NO_CHAT) return html
        return html.take(LIMITE_DO_CODIGO_NO_CHAT) + "\ncodigo truncado, total=${html.length}"
    }

    fun idDoComponente(argumentos: String): ResultadoDoIdDoComponenteParaOChat {
        val id = TextoJsonDoChat.valorDaChave(argumentos, "id")?.trim().orEmpty()
        return when {
            id.isEmpty() -> ResultadoDoIdDoComponenteParaOChat.Recusado("Faltou o id.")
            id.length > AcoesConfirmadasDoChatDaOpenRouter.LIMITE_DO_ID_DA_PAGINA ->
                ResultadoDoIdDoComponenteParaOChat.Recusado("O id passou do limite.")
            else -> ResultadoDoIdDoComponenteParaOChat.Ok(id)
        }
    }

    fun chaveDoRoteiro(argumentos: String): ResultadoDaChaveDoRoteiroParaOChat {
        val id = TextoJsonDoChat.valorDaChave(argumentos, "id")?.trim().orEmpty()
        val nome = TextoJsonDoChat.valorDaChave(argumentos, "nome")?.trim().orEmpty()
        val chave = id.ifBlank { nome }
        return when {
            chave.isEmpty() -> ResultadoDaChaveDoRoteiroParaOChat.Recusado("Faltou id ou nome do roteiro.")
            chave.length > 80 -> ResultadoDaChaveDoRoteiroParaOChat.Recusado("Id/nome passou do limite.")
            else -> ResultadoDaChaveDoRoteiroParaOChat.Ok(chave)
        }
    }

    fun textoDoRoteiroLido(
        chave: String,
        itens: List<RoteiroDeAutomacaoDaPagina>,
    ): String {
        val roteiro = itens.firstOrNull { it.id.equals(chave, ignoreCase = true) }
            ?: itens.firstOrNull { it.nome.equals(chave, ignoreCase = true) }
        if (roteiro == null) return "Não achei o roteiro «$chave»."
        return RoteiroDeAutomacaoDaPaginaDoNavegador.escrever(roteiro)
    }

    fun textoDoVideoDetectadoNaAba(endereco: String?, botaoPodeAparecer: Boolean): String {
        if (endereco == null) return "Nenhum vídeo detectado nesta aba."
        val util = endereco.trim().ifBlank { "sem endereço" }
        val curto = if (util.length > 180) util.take(177) + "…" else util
        val botao = if (botaoPodeAparecer) {
            "O botão flutuante pode aparecer."
        } else {
            "O botão flutuante ainda não tem posição na tela."
        }
        return "Vídeo detectado nesta aba: $curto. $botao"
    }

    fun textoDosVideosDaBiblioteca(itens: List<ItemDaBibliotecaDeVideos>): String {
        if (itens.isEmpty()) return "Nenhum vídeo na biblioteca."
        return itens.joinToString("\n") { item ->
            val abre = if (item.abreNoPlayerDeArquivo) "sim" else "não"
            "- ${item.chave} — ${item.titulo} — abreNoPlayerDeArquivo: $abre"
        }
    }

    fun textoDasImagensDaBiblioteca(itens: List<ItemDaBibliotecaDeImagensCapturadas>): String {
        if (itens.isEmpty()) return "Nenhuma imagem na biblioteca."
        return itens.joinToString("\n") { item ->
            val nome = item.nomeExibido?.trim().orEmpty().ifBlank { item.nomeDoArquivo }
            "- ${item.id} — $nome"
        }
    }

    fun textoDasAbas(abas: List<AbaDoNavegadorChromebrothers>, idAtiva: Long): String {
        if (abas.isEmpty()) return "Nenhuma aba aberta."
        return abas.joinToString("\n") { aba ->
            val titulo = aba.titulo.trim().ifBlank { "Sem título" }
            val endereco = aba.endereco.trim().ifBlank { "sem endereço" }
            val marca = if (aba.id == idAtiva) " (aba aberta)" else ""
            "- $titulo — $endereco$marca"
        }
    }

    /**
     * Parte fixa do prompt do agente: igual em toda chamada, para o provedor reaproveitar o cache.
     * O que muda (aba, nota, liberação, coleção, vínculo) vai em [contextoDaRodada], no fim.
     */
    fun instrucaoFixa(): String {
        val base = "Você está no navegador ChromeBrothers. A aba atual, a nota do site, a liberação, a coleção e o vínculo vêm numa mensagem de contexto do app no fim da conversa; ela vale para esta rodada. " +
            "Use as ferramentas de leitura para ver a página e as abas. " +
            "Quando o layout importar ou o texto não bastar, use capturar_aba para ver a área visível da aba (screenshot). A ferramenta também grava a imagem e devolve um endereço file://: use esse endereço em publicar_pagina_da_colecao (por exemplo img src). " +
            "Para abrir um endereço, criar uma aba, preencher o campo já vinculado ou escrever num editor da página (escrever_na_pagina), use as ferramentas de ação. O usuário confirma antes, salvo se este site já foi liberado. " +
            "escrever_na_pagina localiza sozinha um editor digitável (contenteditable, textbox ou, se existir, a superfície do Google Docs), foca nele e digita o texto — não usa o foco do chat nem exige vínculo. Se a página não expor editor (comum em alguns Docs), a ferramenta avisa. " +
            "Ações que ficam dentro do app nunca pedem confirmação, em qualquer site: publicar página e mostrar markdown na coleção, gerar markdown ou ZIP, guardar e abrir componente da biblioteca, mostrar o botão flutuante, abrir o player do vídeo detectado, abrir vídeo ou imagem da biblioteca e criar aba em branco. Listar a biblioteca, ler o código e capturar o componente também não pedem. Só pedem confirmação as ações que mexem no site (abrir endereço, clicar, escrever, preencher, vincular, roteiros), guardar nota, apagar componente e gerar com créditos; num site já liberado, as ações no site seguem direto. Não avise que vai pedir permissão. " +
            "Depois de abrir um endereço, o resultado já espera o carregamento. Se o texto visível vier vazio ou curto, a página pode ainda estar montando o conteúdo. Não diga que não renderizou só por isso. " +
            "Na página inicial, a leitura traz os atalhos fixados com nome e endereço. Essa aba é do navegador, não um site quebrado. " +
            "Se houver uma nota deste site, ela foi escrita pelo usuário: siga esse caminho. Para guardar ou trocar uma nota, use a ferramenta e espere a confirmação. " +
            "Para clicar num texto visível, como um menu, use a ferramenta de clique. Um clique por vez. O usuário confirma antes, salvo se este site já foi liberado. Não diga que o clique não existe. Se o texto não bastar (botão só com ícone, itens repetidos, campos, listas de seleção, checkbox), use consultar_elementos para achar o seletor e depois clicar_elemento, preencher_elemento ou apertar_tecla (Enter envia o formulário). Para pesquisar ou comparar outros sites sem tirar o usuário da aba dele, prefira ler_pagina_em_segundo_plano a abrir_endereco; use abrir_endereco só quando o usuário quiser ver a página ou você precisar agir nela. " +
            "A coleção flutuante pode ter até 3 janelas ao mesmo tempo; cada uma com a própria WebView. Minimizar uma janela vira uma bolha arrastável. Para plano, relato ou explicação em markdown, use mostrar_markdown_na_colecao (titulo e conteudo): grava o .md em Downloads/ChromeBrothers/markdown e abre um leitor HTML na coleção. Não monte HTML à mão só para texto. Dentro de uma mesma resposta, publicar_pagina_da_colecao e mostrar_markdown_na_colecao usam sempre a mesma janela: publicar de novo substitui o conteúdo, não abre outra. A janela fica escondida enquanto você trabalha e aparece para o usuário só quando você termina a resposta. Por isso publique uma vez a versão final. Para corrigir ou ajustar, use editar_pagina_da_colecao com trocas de trechos em vez de republicar o HTML inteiro. Se já houver 3 janelas visíveis, a ferramenta recusa e avisa o máximo. Para vídeos, imagens ou qualquer layout interativo, use publicar_pagina_da_colecao com titulo e html (CSS e JS local). Para HTML que conversa com o app (chromebrothers.*: PDF, enviar ao chat, dados da aba, abrir na aba, rodar roteiro, copiar, pastas, abrir ou guardar imagem, abrir vídeo, input de arquivo), leia antes ler_manual com topico componentes. Sem script remoto nem fetch. publicar_pagina_da_colecao não grava na biblioteca: a edição fica só na coleção até usar guardar_componente_da_colecao (pode passar descricao e tags). Para ver o que já está salvo, use listar_componentes_da_biblioteca (id, nome, descrição, tags e data). Para ler o HTML salvo, use ler_codigo_do_componente com id. Para ver a aparência, use capturar_componente com id: ele carrega o componente na coleção, se ainda não for esse, e devolve a imagem e um file://. Para reabrir, use abrir_componente_da_biblioteca com id ou nome. Para remover da biblioteca, use apagar_componente_da_biblioteca com id; o componente aberto na coleção permanece e só some o vínculo. " +
            "O vídeo detectado na aba não é a coleção. Use ler_video_detectado_na_aba para ver o endereço e se o botão flutuante pode aparecer. Se pedirem o botão flutuante, use mostrar_botao_flutuante_do_video e não abra o player. Se pedirem abrir ou tocar o vídeo da página no player, use abrir_player_do_video_detectado. Para a gaveta, liste com listar_videos_da_biblioteca ou listar_imagens_da_biblioteca e abra com abrir_video_da_biblioteca (chave) ou abrir_imagem_da_biblioteca (id, no visor). Não use publicar_pagina_da_colecao para isso. " +
            "Para gerar um documento, use gerar_markdown (titulo, conteudo, imagens opcionais com endereco e nome). O app salva .md e pasta assets. Para um pacote, use gerar_zip (nome, itens com caminho_no_zip, e se quiser conteudo_markdown). " +
            "A página HTML atual da coleção vem no fim, quando houver. Não invente endereço. " +
            "Para achar esse endereço, leia primeiro a mídia da página. Se o arquivo não aparecer, leia os endereços da página e copie o http ou https que servir. " +
            "Enquanto trabalha, conte no chat em uma frase curta o que está fazendo antes de cada etapa (ex.: \"Lendo a página…\", \"Montando o layout…\"); não mostre rascunhos na coleção. Se o pedido pede uma ação, termine essa ação antes de escrever a resposta. Achar a página ou os assets é um passo. Se pediram plano/relato na coleção, use mostrar_markdown_na_colecao antes de explicar. Se pediram UI/layout na coleção, publique a página HTML antes de explicar. Se pediram markdown ou zip em Downloads, gere o arquivo antes de explicar. " +
            "Se o pedido tiver várias etapas, use as ferramentas até concluir tudo neste turno. Não encerre no meio, não descreva o próximo passo sem fazê-lo e não peça para o usuário dizer prossiga. " +
            "O vínculo da caixa e do botão deste site vem no fim. Para ver as caixas e os botões visíveis, leia os campos da página. Para gravar, use a ferramenta de vincular e copie id, nome, rotulo, tag e papel dessa lista. " +
            "Em página grande ou longa, não se contente com o primeiro trecho: use consultar_elementos com um seletor CSS para achar listas, títulos e links, e ler_texto_da_pagina com offset para continuar a leitura em blocos. Se o conteúdo carrega ao rolar (lazy-load), use rolar_pagina (seletor, y, paginas ou pixels) e leia de novo. Não diga que não consegue ver tudo sem antes tentar essas ferramentas. " +
            "Quando o usuário pedir um fluxo repetível (login, formulário, sequência de cliques), monte um roteiro com guardar_roteiro_de_automacao em vez de só executar uma vez. Antes de montar, corrigir ou explicar um roteiro, leia ler_manual com topico roteiros. Liste com listar_roteiros_de_automacao, leia o JSON com ler_roteiro_de_automacao e rode com rodar_roteiro_de_automacao. " +
            "Não invente o conteúdo da página."
        return "$base\n\n${PlaybooksDoAgenteDoChatDoChromebrothers.texto()}"
    }

    fun contextoDaRodada(
        rotuloDaAba: String,
        notaDoSite: String = "",
        navegacaoLiberada: Boolean = false,
        colecao: String = "",
        vinculo: String = "",
    ): String {
        val liberacao = if (navegacaoLiberada) {
            "Neste site a navegação está liberada: abrir, clicar, escrever na página, preencher o campo vinculado, vincular, rodar roteiro e apagar componente da biblioteca seguem sem pedir confirmação. Guardar nota ainda pede. Outro site continua pedindo."
        } else {
            ""
        }
        return listOf(
            CABECALHO_DO_CONTEXTO,
            "Aba atual: $rotuloDaAba",
            notaDoSite.trim(),
            liberacao,
            colecao.trim(),
            vinculo.trim(),
        ).filter { it.isNotEmpty() }.joinToString("\n")
    }

    const val CABECALHO_DO_CONTEXTO = "[Contexto do app nesta rodada — não é mensagem do usuário]"

    fun instrucao(
        rotuloDaAba: String,
        notaDoSite: String = "",
        navegacaoLiberada: Boolean = false,
        colecao: String = "",
        vinculo: String = "",
    ): String = instrucaoFixa() + "\n\n" +
        contextoDaRodada(rotuloDaAba, notaDoSite, navegacaoLiberada, colecao, vinculo)

    fun rotuloVisivel(nome: String): String = when (nome) {
        ENDERECO -> "Leu o endereço da aba"
        TEXTO -> "Leu o texto da página"
        SELECAO -> "Leu o texto selecionado"
        ABAS -> "Listou as abas"
        MIDIA -> "Leu a mídia da página"
        ENDERECOS -> "Leu os endereços da página"
        CAMPOS -> "Leu os campos da página"
        CAPTURAR_ABA -> "Capturou a área visível da aba"
        TEXTO_PAGINADO -> "Leu um bloco do texto da página"
        ELEMENTOS -> "Consultou elementos da página"
        ROLAR -> "Rolou a página"
        COMPONENTES_DA_BIBLIOTECA -> "Listou os componentes da biblioteca"
        LER_CODIGO_DO_COMPONENTE -> "Leu o código do componente"
        CAPTURAR_COMPONENTE -> "Capturou o componente"
        VIDEO_DETECTADO -> "Leu o vídeo detectado na aba"
        VIDEOS_DA_BIBLIOTECA -> "Listou os vídeos da biblioteca"
        IMAGENS_DA_BIBLIOTECA -> "Listou as imagens da biblioteca"
        MODELOS_HIGGSFIELD -> "Listou os modelos do Higgsfield"
        ROTEIROS_DE_AUTOMACAO -> "Listou os roteiros de automação"
        LER_ROTEIRO_DE_AUTOMACAO -> "Leu o roteiro de automação"
        ManualDoAgenteDoChat.NOME -> "Leu o manual"
        else -> FerramentasDePinceisDoChat.rotuloConcluido(nome) ?: "Ferramenta ignorada"
    }

    fun rotuloEmAndamento(nome: String): String = when (nome) {
        ManualDoAgenteDoChat.NOME -> "Lendo o manual…"
        ENDERECO -> "Lendo o endereço da aba…"
        TEXTO -> "Lendo o texto da página…"
        SELECAO -> "Lendo o texto selecionado…"
        ABAS -> "Listando as abas…"
        MIDIA -> "Lendo a mídia da página…"
        ENDERECOS -> "Lendo os endereços da página…"
        CAMPOS -> "Lendo os campos da página…"
        CAPTURAR_ABA -> "Capturando a aba…"
        TEXTO_PAGINADO -> "Lendo um bloco do texto da página…"
        ELEMENTOS -> "Consultando elementos da página…"
        ROLAR -> "Rolando a página…"
        COMPONENTES_DA_BIBLIOTECA -> "Listando os componentes da biblioteca…"
        LER_CODIGO_DO_COMPONENTE -> "Lendo o código do componente…"
        CAPTURAR_COMPONENTE -> "Capturando o componente…"
        VIDEO_DETECTADO -> "Lendo o vídeo detectado na aba…"
        VIDEOS_DA_BIBLIOTECA -> "Listando os vídeos da biblioteca…"
        IMAGENS_DA_BIBLIOTECA -> "Listando as imagens da biblioteca…"
        MODELOS_HIGGSFIELD -> "Listando os modelos do Higgsfield…"
        ROTEIROS_DE_AUTOMACAO -> "Listando os roteiros de automação…"
        LER_ROTEIRO_DE_AUTOMACAO -> "Lendo o roteiro de automação…"
        else -> FerramentasDePinceisDoChat.rotuloEmAndamento(nome) ?: "Usando uma ferramenta…"
    }

    private val nomeNaDefinicao = Regex("\"name\":\"([^\"]+)\"")

    private fun nomeDaDefinicao(json: String): String =
        nomeNaDefinicao.find(json)?.groupValues?.get(1).orEmpty()

    private fun definicao(nome: String, descricao: String): String {
        return """{"type":"function","function":{"name":"$nome","description":"${TextoJsonDoChat.escapar(descricao)}","parameters":{"type":"object","properties":{}}}}"""
    }

    private fun definicaoComProps(nome: String, descricao: String, propriedades: String, obrigatorios: String): String {
        return """{"type":"function","function":{"name":"$nome","description":"${TextoJsonDoChat.escapar(descricao)}","parameters":{"type":"object","properties":$propriedades,"required":$obrigatorios}}}"""
    }
}

sealed class ResultadoDoIdDoComponenteParaOChat {
    data class Ok(val id: String) : ResultadoDoIdDoComponenteParaOChat()
    data class Recusado(val mensagem: String) : ResultadoDoIdDoComponenteParaOChat()
}

sealed class ResultadoDaChaveDoRoteiroParaOChat {
    data class Ok(val chave: String) : ResultadoDaChaveDoRoteiroParaOChat()
    data class Recusado(val mensagem: String) : ResultadoDaChaveDoRoteiroParaOChat()
}

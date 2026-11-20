package com.chromebrothers.navegador.regras

/**
 * Trechos longos e de uso raro do prompt do agente, carregados sob demanda pela ferramenta
 * `ler_manual`. Assim não viajam em toda chamada; o modelo lê quando o pedido precisar.
 */
object ManualDoAgenteDoChat {
    const val NOME = "ler_manual"
    const val ROTEIROS = "roteiros"
    const val COMPONENTES = "componentes"
    const val PINCEIS = "pinceis"
    val topicos = listOf(ROTEIROS, COMPONENTES, PINCEIS)

    fun definicao(): String =
        """{"type":"function","function":{"name":"$NOME","description":"${TextoJsonDoChat.escapar(DESCRICAO)}","parameters":{"type":"object","properties":{"topico":{"type":"string","enum":["$ROTEIROS","$COMPONENTES","$PINCEIS"]}},"required":["topico"]}}}"""

    private const val DESCRICAO =
        "Lê o manual de um tópico. roteiros: ações, variáveis, ramificação A/B e exemplos de guardar_roteiro_de_automacao. " +
            "componentes: a ponte chromebrothers.* do HTML da coleção (PDF, enviar ao chat, dados da aba, abrir na aba, rodar roteiro, pastas, imagens, vídeo, arquivos). " +
            "pinceis: como funciona o motor de pincéis do editor de imagem, cada parâmetro, as pontas prontas e receitas. " +
            "Leia antes de montar roteiro, HTML que fala com o app ou de criar/ajustar pincel."

    fun texto(argumentos: String): String {
        val topico = TextoJsonDoChat.valorDaChave(argumentos, "topico")?.trim()?.lowercase().orEmpty()
        return when (topico) {
            ROTEIROS -> roteiros()
            COMPONENTES -> COMPONENTES_TEXTO
            PINCEIS -> ManualDePinceisDoAgenteDoChat.texto()
            else -> "Tópico desconhecido. Use um destes: ${topicos.joinToString(", ")}."
        }
    }

    fun roteiros(): String = ROTEIROS_TEXTO + "\n\n" + ReceitaDoFluxoCondicionalDoRoteiroDeAutomacaoParaOChat.receita()

    private const val ROTEIROS_TEXTO =
        "Manual de roteiros (guardar_roteiro_de_automacao).\n" +
            "Roteiro determinístico, estilo Playwright, para repetir depois sem IA. Monte um roteiro quando o usuário pedir um fluxo repetível (login, preencher formulário, sequência de cliques) em vez de só executar uma vez.\n" +
            "Passos (campo acao): abrir(url), aguardar e aguardar_sumir (seletor ou texto, timeout_ms), aguardar_url (contem), aguardar_qualquer, marcar, ir_para, clicar e preencher (seletor ou texto; valor aceita {{variavel}}), esperar_ms (ms), rolar (pixels), clicar_se (seletor, indice, contem; se_nao=parar|seguir), assertar (confirma presença/texto), extrair (grava texto em variavel).\n" +
            "aguardar_qualquer: opcoes[{id, seletor?, texto?}] (2 a 4); o primeiro que aparecer vence e grava o id em variavel (padrão _ramo).\n" +
            "ir_para: destino (id de um marcar) OU passo (1-based), nunca os dois; se opcional, ex.: {{_ramo}}=livre; se vazio, salta sempre; se a condição falhar, segue o próximo passo.\n" +
            "marcar: âncora sem efeito (destino do ir_para). clicar_se só clica-ou-pula um elemento: NÃO serve para ramificar o fluxo.\n" +
            "Id do roteiro é opcional. Guardar pede confirmação, salvo site liberado.\n" +
            "Depois: listar_roteiros_de_automacao para ver os guardados; ler_roteiro_de_automacao (id ou nome) para ler o JSON e copiar o padrão; rodar_roteiro_de_automacao para rodar. O ▶ do chat também roda. Num componente da coleção: chromebrothers.rodarRoteiro(id), com callback opcional window.chromebrothersAoTerminarRoteiro."

    private const val COMPONENTES_TEXTO =
        "Manual dos componentes HTML da coleção (publicar_pagina_da_colecao).\n" +
            "HTML com CSS e JS locais. Sem script remoto nem fetch. Imagens e vídeos: http, https, file ou content.\n" +
            "PDF: botão que chama chromebrothers.gerarPdf('nome') salva um PDF igual à página em Downloads.\n" +
            "Falar com o chat: chromebrothers.enviarAoChat('texto') envia ao chat em segundo plano (sem abrir a gaveta) e mostra um toast.\n" +
            "Contexto da aba ativa: chromebrothers.urlDaAbaAtiva(), chromebrothers.tituloDaAbaAtiva() e chromebrothers.textoSelecionado() (strings; vazias se não houver).\n" +
            "Ações nativas: chromebrothers.abrirNaAba('url ou busca'), chromebrothers.rodarRoteiro('id-do-roteiro') (roda roteiro guardado na aba, sem IA; ao terminar chama window.chromebrothersAoTerminarRoteiro se existir), chromebrothers.copiar('texto') e chromebrothers.toast('aviso').\n" +
            "Arquivos: input type=\"file\" (com accept se quiser) abre o seletor de arquivos do aparelho, inclusive Downloads, para escolher documento, imagem ou vídeo.\n" +
            "Pastas: chromebrothers.escolherPasta() abre o seletor de pasta do Android e chama window.chromebrothersAoEscolherPasta(json) com ok, nome, uri e itens (nome, uri, mime, pasta, tamanho). Para relistar ou entrar numa subpasta, chromebrothers.listarPasta(uri). Os uri content:// dos itens podem ir direto em img/video src (o app entrega o conteúdo).\n" +
            "Mídia no app nativo: chromebrothers.abrirImagemNaAba(uri), chromebrothers.abrirImagemNoVisor(uri), chromebrothers.abrirImagemNoEditor(uri), chromebrothers.guardarImagem(uri) (salva na biblioteca de imagens), chromebrothers.copiarImagem(uri) (área de transferência) e chromebrothers.abrirVideoNoPlayer(uri); cada uma devolve JSON com ok. Aceitam content://, file://, http e https."
}

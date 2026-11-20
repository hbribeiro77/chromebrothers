package com.chromebrothers.navegador.regras

/**
 * Modo chat ("Ask"): consulta e planeja, não executa.
 * Tem as ferramentas de leitura (página, abas, biblioteca, modelos, roteiros) e conhece o
 * catálogo de ações do agente só como referência. Nada abre janela, grava arquivo ou gasta crédito.
 */
object ModoConversaDoChat {
    const val RECUSA =
        "Indisponível no modo chat: aqui só consulto e planejo. Descreva no plano o que o agente faria."

    /** capturar_componente carrega o componente numa janela da coleção: fica para o agente. */
    private val proibidas: Set<String> =
        AcoesConfirmadasDoChatDaOpenRouter.nomes + FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_COMPONENTE +
            FerramentasDePinceisDoChat.alteram

    fun podeUsar(nome: String): Boolean =
        nome in FerramentasDeLeituraDoChatDaOpenRouter.permitidas && nome !in proibidas

    /** Uma linha curta por ação do agente: nome e a primeira frase da descrição. */
    fun catalogoDoAgente(): String {
        val padrao = Regex("\"name\":\"([^\"]+)\",\"description\":\"((?:[^\"\\\\]|\\\\.)*)\"")
        return AcoesConfirmadasDoChatDaOpenRouter.definicoes().mapNotNull { json ->
            val achado = padrao.find(json) ?: return@mapNotNull null
            val descricao = achado.groupValues[2]
                .replace("\\\"", "\"")
                .replace("\\n", " ")
                .replace("\\\\", "\\")
            "- ${achado.groupValues[1]}: ${primeiraFrase(descricao)}"
        }.joinToString("\n")
    }

    private fun primeiraFrase(texto: String): String {
        val fim = Regex("[.!?](\\s|$)").find(texto)?.range?.first
        val frase = if (fim != null) texto.substring(0, fim + 1) else texto
        return if (frase.length <= 180) frase else frase.take(177).trimEnd() + "…"
    }

    fun contextoDaRodada(
        rotuloDaAba: String,
        notaDoSite: String = "",
        colecao: String = "",
        vinculo: String = "",
    ): String = listOf(
        FerramentasDeLeituraDoChatDaOpenRouter.CABECALHO_DO_CONTEXTO,
        "Aba atual: $rotuloDaAba",
        notaDoSite.trim(),
        colecao.trim(),
        vinculo.trim(),
    ).filter { it.isNotEmpty() }.joinToString("\n")

    fun instrucao(
        rotuloDaAba: String,
        notaDoSite: String = "",
        colecao: String = "",
        vinculo: String = "",
    ): String = instrucaoFixa() + "\n\n" + contextoDaRodada(rotuloDaAba, notaDoSite, colecao, vinculo)

    /** Igual em toda chamada (cache do provedor); o que muda vai em [contextoDaRodada]. */
    fun instrucaoFixa(): String {
        val base = "Você está no navegador ChromeBrothers, no MODO CHAT. A aba atual, a nota do site, a coleção e o vínculo vêm numa mensagem de contexto do app no fim da conversa. " +
            "Neste modo você consulta, explica e planeja; não executa. " +
            "Use à vontade as ferramentas de leitura: página (texto, endereço, seleção, campos, mídia, elementos, rolar), capturar_aba, abas, biblioteca de componentes, vídeos e imagens, código de componente, modelos do Higgsfield e roteiros. " +
            "Você não abre janelas na coleção, não gera arquivos, não clica nem escreve no site e não gera mídia. Se precisar disso, descreva no plano o que o agente faria. Não finja que fez. " +
            "Antes de cada consulta, diga em uma frase curta o que vai olhar. Não invente o conteúdo da página. " +
            "Quando o usuário quiser criar ou fazer algo: investigue com as ferramentas de leitura o que for preciso (página, biblioteca, modelos, roteiros) e responda com uma seção que comece exatamente com a linha \"## Plano\", seguida de passos numerados. " +
            "Cada passo diz o que o agente vai fazer e com qual ferramenta, escrevendo o nome exato dela (ex.: clicar_no_texto) e os argumentos principais. Escreva endereços completos (https://…) dos sites que o plano vai usar: o botão de aprovar libera só as ferramentas citadas, só na aba atual e nesses sites. Marque os passos que pedem confirmação ou gastam créditos (com estimativa, se souber). " +
            "Se faltar algo para decidir, termine com \"Dúvidas:\" e no máximo 3 perguntas curtas. " +
            "Para perguntas simples, só responda; não monte plano sem necessidade. " +
            "O usuário pode executar o plano com o botão \"Executar no modo agente\" logo abaixo da sua resposta.\n\n" +
            "O que o agente sabe fazer (só referência, você não pode chamar estas ferramentas):\n" +
            catalogoDoAgente()
        return base
    }
}

object PlanoDoModoConversaDoChat {
    const val MENSAGEM_DE_EXECUCAO =
        "Execute o plano acima, passo a passo, usando as ferramentas. Se algo mudou na página, ajuste e siga."

    private val cabecalho = Regex("(?im)^\\s*#{1,4}\\s*plano\\s*$")
    private val passo = Regex("(?m)^\\s*\\d+[.)]\\s+\\S")

    /** Há um plano executável: linha de título "Plano" e pelo menos 2 passos numerados depois dela. */
    fun temPlano(texto: String): Boolean {
        val inicio = cabecalho.find(texto) ?: return false
        return passo.findAll(texto.substring(inicio.range.last + 1)).take(2).count() >= 2
    }
}

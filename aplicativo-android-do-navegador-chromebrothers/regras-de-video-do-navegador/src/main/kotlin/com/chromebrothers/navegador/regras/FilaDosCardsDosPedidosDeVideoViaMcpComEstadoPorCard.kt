package com.chromebrothers.navegador.regras

/** Um vídeo pedido no componente de geração MCP, do skeleton até virar item da biblioteca. */
data class CardDoPedidoDeVideoViaMcp(
    val id: String,
    val numero: Int,
    val criadoEmMs: Long,
    val mcp: String,
    val modelo: String,
    val prompt: String,
    val status: String,
    val idDaTarefa: String,
    /** Dado extra para consultar depois: taskType no Topview, URLs de entrada no Sjinn. */
    val detalheDaTarefa: String,
    val mensagem: String,
    /** Receita (prompt + refs + parâmetros) compartilhada pelos cards do mesmo pedido. */
    val idDaReceita: String = "",
    /** Preenchidos quando o card fica pronto: item da biblioteca, arquivo e miniatura em disco. */
    val idBiblioteca: String = "",
    val fileUrl: String = "",
    val caminhoMiniatura: String = "",
)

data class EstadoGuardadoDosCardsDeVideoViaMcp(
    val ultimoNumero: Int,
    val cards: List<CardDoPedidoDeVideoViaMcp>,
)

/**
 * Estado por card dos vídeos pedidos via MCP (Higgsfield, Sjinn, Topview):
 * numeração, transições de status, quem pode ser verificado e persistência em JSON.
 */
object FilaDosCardsDosPedidosDeVideoViaMcpComEstadoPorCard {
    const val STATUS_ENVIANDO = "enviando"
    const val STATUS_GERANDO = "gerando"
    const val STATUS_PRONTO = "pronto"
    const val STATUS_DEMOROU = "demorou"
    const val STATUS_FALHOU = "falhou"
    const val LIMITE_DE_CARDS_GUARDADOS = 60
    private const val FOLGA_DO_LIMITE_DE_ESPERA_MS = 5_000L

    fun novosCards(
        ultimoNumero: Int,
        quantidade: Int,
        mcps: List<String>,
        modeloDoMcp: (String) -> String,
        prompt: String,
        agoraMs: Long,
        gerarId: () -> String,
        idDaReceita: String = "",
    ): List<CardDoPedidoDeVideoViaMcp> {
        var numero = ultimoNumero
        return mcps.flatMap { mcp ->
            List(quantidade.coerceAtLeast(1)) {
                numero += 1
                CardDoPedidoDeVideoViaMcp(
                    id = gerarId(),
                    numero = numero,
                    criadoEmMs = agoraMs,
                    mcp = mcp,
                    modelo = modeloDoMcp(mcp),
                    prompt = prompt,
                    status = STATUS_ENVIANDO,
                    idDaTarefa = "",
                    detalheDaTarefa = "",
                    mensagem = "",
                    idDaReceita = idDaReceita,
                )
            }
        }
    }

    /**
     * Com id da tarefa: consulta direta. Higgsfield sem id (a resposta da criação se perdeu na rede,
     * mas o job pode existir lá): Verificar procura o pedido no histórico do Higgsfield.
     */
    fun podeVerificar(card: CardDoPedidoDeVideoViaMcp): Boolean =
        (card.status == STATUS_DEMOROU || card.status == STATUS_FALHOU) &&
            (card.idDaTarefa.isNotBlank() || card.mcp == MCP_QUE_TEM_HISTORICO)

    const val MCP_QUE_TEM_HISTORICO = "higgsfield"

    fun statusAposErroDaEspera(temTarefa: Boolean, decorridoMs: Long, limiteMs: Long): String =
        if (temTarefa && decorridoMs >= limiteMs - FOLGA_DO_LIMITE_DE_ESPERA_MS) STATUS_DEMOROU else STATUS_FALHOU

    /** A thread que esperava morreu com o app: quem tinha tarefa vira «Demorou», o resto «Falhou». */
    fun aoRestaurar(cards: List<CardDoPedidoDeVideoViaMcp>): List<CardDoPedidoDeVideoViaMcp> =
        cards.map { card ->
            when (card.status) {
                STATUS_ENVIANDO, STATUS_GERANDO ->
                    if (card.idDaTarefa.isNotBlank()) {
                        card.copy(status = STATUS_DEMOROU)
                    } else {
                        card.copy(status = STATUS_FALHOU, mensagem = card.mensagem.ifBlank { "O app fechou antes do envio." })
                    }
                else -> card
            }
        }

    fun atualizar(
        cards: List<CardDoPedidoDeVideoViaMcp>,
        card: CardDoPedidoDeVideoViaMcp,
    ): List<CardDoPedidoDeVideoViaMcp> {
        val indice = cards.indexOfFirst { it.id == card.id }
        if (indice < 0) return cards + card
        return cards.toMutableList().also { it[indice] = card }
    }

    fun remover(cards: List<CardDoPedidoDeVideoViaMcp>, id: String): List<CardDoPedidoDeVideoViaMcp> =
        cards.filterNot { it.id == id }

    /**
     * Guarda todos (inclusive os prontos). Acima do limite saem primeiro os prontos mais antigos:
     * falhas e pedidos em andamento ainda pedem ação do usuário.
     */
    fun paraJson(cards: List<CardDoPedidoDeVideoViaMcp>, ultimoNumero: Int): String {
        val itens = aparar(cards).joinToString(",") { c ->
            """{"id":"${e(c.id)}","numero":${c.numero},"criadoEmMs":${c.criadoEmMs},"mcp":"${e(c.mcp)}","modelo":"${e(c.modelo)}","prompt":"${e(c.prompt)}","status":"${e(c.status)}","idDaTarefa":"${e(c.idDaTarefa)}","detalheDaTarefa":"${e(c.detalheDaTarefa)}","mensagem":"${e(c.mensagem)}","idDaReceita":"${e(c.idDaReceita)}","idBiblioteca":"${e(c.idBiblioteca)}","fileUrl":"${e(c.fileUrl)}","caminhoMiniatura":"${e(c.caminhoMiniatura)}"}"""
        }
        return """{"ultimoNumero":$ultimoNumero,"cards":[$itens]}"""
    }

    /** Até [limite] cards, na ordem original; descarta prontos mais antigos antes dos demais. */
    fun aparar(
        cards: List<CardDoPedidoDeVideoViaMcp>,
        limite: Int = LIMITE_DE_CARDS_GUARDADOS,
    ): List<CardDoPedidoDeVideoViaMcp> {
        var excesso = cards.size - limite
        if (excesso <= 0) return cards
        val fora = mutableSetOf<String>()
        for (c in cards) {
            if (excesso == 0) break
            if (c.status == STATUS_PRONTO) { fora += c.id; excesso-- }
        }
        for (c in cards) {
            if (excesso == 0) break
            if (c.id !in fora) { fora += c.id; excesso-- }
        }
        return cards.filterNot { it.id in fora }
    }

    fun deJson(json: String): EstadoGuardadoDosCardsDeVideoViaMcp {
        val bruto = json.trim()
        val ultimo = TextoJsonDoChat.inteiroDaChave(bruto, "ultimoNumero") ?: 0
        val blocos = TextoJsonDoChat.objetosDaChave(bruto, "cards").orEmpty()
        val cards = blocos.mapNotNull { b ->
            val id = TextoJsonDoChat.valorDaChave(b, "id").orEmpty()
            if (id.isBlank()) return@mapNotNull null
            CardDoPedidoDeVideoViaMcp(
                id = id,
                numero = TextoJsonDoChat.inteiroDaChave(b, "numero") ?: 0,
                criadoEmMs = TextoJsonDoChat.numeroDaChave(b, "criadoEmMs")?.toLong() ?: 0L,
                mcp = TextoJsonDoChat.valorDaChave(b, "mcp").orEmpty(),
                modelo = TextoJsonDoChat.valorDaChave(b, "modelo").orEmpty(),
                prompt = TextoJsonDoChat.valorDaChave(b, "prompt").orEmpty(),
                status = TextoJsonDoChat.valorDaChave(b, "status").orEmpty().ifBlank { STATUS_FALHOU },
                idDaTarefa = TextoJsonDoChat.valorDaChave(b, "idDaTarefa").orEmpty(),
                detalheDaTarefa = TextoJsonDoChat.valorDaChave(b, "detalheDaTarefa").orEmpty(),
                mensagem = TextoJsonDoChat.valorDaChave(b, "mensagem").orEmpty(),
                idDaReceita = TextoJsonDoChat.valorDaChave(b, "idDaReceita").orEmpty(),
                idBiblioteca = TextoJsonDoChat.valorDaChave(b, "idBiblioteca").orEmpty(),
                fileUrl = TextoJsonDoChat.valorDaChave(b, "fileUrl").orEmpty(),
                caminhoMiniatura = TextoJsonDoChat.valorDaChave(b, "caminhoMiniatura").orEmpty(),
            )
        }
        return EstadoGuardadoDosCardsDeVideoViaMcp(ultimoNumero = maxOf(ultimo, cards.maxOfOrNull { it.numero } ?: 0), cards = cards)
    }

    /** Objeto enviado ao HTML do componente; quando pronto leva o item da biblioteca (do card ou dos parâmetros). */
    fun jsonDoCard(
        card: CardDoPedidoDeVideoViaMcp,
        idBiblioteca: String = "",
        fileUrl: String = "",
        previewDataUrl: String = "",
    ): String {
        val idItem = idBiblioteca.ifBlank { card.idBiblioteca }
        val arquivo = fileUrl.ifBlank { card.fileUrl }
        return """{"id":"${e(card.id)}","numero":${card.numero},"criadoEmMs":${card.criadoEmMs},"mcp":"${e(card.mcp)}","modelo":"${e(card.modelo)}","prompt":"${e(card.prompt)}","status":"${e(card.status)}","mensagem":"${e(card.mensagem)}","podeVerificar":${podeVerificar(card)},"idDaReceita":"${e(card.idDaReceita)}","idBiblioteca":"${e(idItem)}","fileUrl":"${e(arquivo)}","previewDataUrl":"${e(previewDataUrl)}"}"""
    }

    private fun e(texto: String) = TextoJsonDoChat.escapar(texto)
}

package com.chromebrothers.navegador.regras

data class PedidoDeGeracaoDeVideoViaMcpNaColecao(
    /** Um ou mais MCPs; a quantidade do pedido vale **por** MCP. */
    val mcps: List<String>,
    val prompt: String,
    /** Id de família canônica (ex. wan3) ou id local do provedor. */
    val modelo: String,
    val quantidade: Int,
    val temReferencia: Boolean,
    val proporcao: String,
    val resolucao: String,
    val duracaoSegundos: Int,
) {
    /** Compat: primeiro MCP (pedidos antigos / um só). */
    val mcp: String get() = mcps.first()

    companion object {
        const val MCP_HIGGSFIELD = "higgsfield"
        const val MCP_SJINN = "sjinn"
        const val MCP_TOPVIEW = "topview"
        const val MCP_MAGNIFIC = "magnific"
        const val MCP_VIVIX = "vivix"
        val MCPS_CONHECIDOS = setOf(MCP_HIGGSFIELD, MCP_SJINN, MCP_TOPVIEW, MCP_MAGNIFIC, MCP_VIVIX)

        const val MODELO_PADRAO_DA_COLECAO_SJINN = "wan3"
        const val MODELO_PADRAO_DA_COLECAO_HIGGSFIELD =
            GeracaoDeImagemEVideoPeloHiggsfieldNoChat.MODELO_PADRAO_VIDEO
        const val MODELO_PADRAO_DA_COLECAO_TOPVIEW = "wan-3-0"
        const val MODELO_PADRAO_DA_COLECAO_MAGNIFIC = "wan-3-0"
        const val MODELO_PADRAO_DA_COLECAO_VIVIX = CatalogoDeModelosDeVideoDoVivixNaColecao.MODELO_PADRAO
        const val LIMITE_DE_REFERENCIAS = 4
        const val QUANTIDADE_MAXIMA = 4

        val PROPORCOES_ACEITAS = setOf("adaptive", "16:9", "9:16", "1:1", "4:3", "3:4")
        const val PROPORCAO_PADRAO = "16:9"

        val RESOLUCOES_ACEITAS = setOf("480p", "720p", "1080p")
        const val RESOLUCAO_PADRAO = "480p"

        const val DURACAO_MINIMA = 2
        const val DURACAO_MAXIMA = 30
        const val DURACAO_PADRAO = 5

        private val regexArrayMcps =
            Regex(""""mcps"\s*:\s*\[(.*?)\]""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        private val regexItemAspas = Regex(""""([^"]+)"""")

        fun modeloPadraoPara(mcp: String): String =
            when (mcp) {
                MCP_HIGGSFIELD -> MODELO_PADRAO_DA_COLECAO_HIGGSFIELD
                MCP_TOPVIEW -> MODELO_PADRAO_DA_COLECAO_TOPVIEW
                MCP_MAGNIFIC -> MODELO_PADRAO_DA_COLECAO_MAGNIFIC
                MCP_VIVIX -> MODELO_PADRAO_DA_COLECAO_VIVIX
                else -> MODELO_PADRAO_DA_COLECAO_SJINN
            }

        fun modeloPadraoPara(mcps: List<String>): String {
            if (mcps.size <= 1) return modeloPadraoPara(mcps.firstOrNull() ?: MCP_SJINN)
            val familia = EquivalenciaDeModelosDeVideoEntreMcps.familiaPadraoPara(mcps)
            return familia.ifBlank { MODELO_PADRAO_DA_COLECAO_SJINN }
        }

        fun resolucaoParaApi(resolucaoDaUi: String): String =
            when (resolucaoDaUi.trim().lowercase()) {
                "720p" -> "720P"
                "1080p" -> "1080P"
                else -> "480P"
            }

        fun lerMcpsDoJson(json: String): List<String> {
            val doArray = regexArrayMcps.find(json)?.groupValues?.getOrNull(1)?.let { bloco ->
                regexItemAspas.findAll(bloco).map { it.groupValues[1].trim().lowercase() }
                    .filter { it.isNotEmpty() }
                    .toList()
            }.orEmpty()
            if (doArray.isNotEmpty()) return doArray.distinct()
            val singular = TextoJsonDoChat.valorDaChave(json, "mcp")?.trim()?.lowercase().orEmpty()
            return listOf(singular.ifBlank { MCP_SJINN })
        }

        fun ler(json: String): ResultadoDoPedidoDeGeracaoDeVideoViaMcp {
            val mcps = lerMcpsDoJson(json)
            val prompt = TextoJsonDoChat.valorDaChave(json, "prompt")?.trim().orEmpty()
            val modeloBruto = TextoJsonDoChat.valorDaChave(json, "modelo")?.trim().orEmpty()
            val modelo = modeloBruto.ifBlank { modeloPadraoPara(mcps) }
            val quantidade = (TextoJsonDoChat.inteiroDaChave(json, "quantidade") ?: 1)
                .coerceIn(1, QUANTIDADE_MAXIMA)
            val temRef = TextoJsonDoChat.valorDaChave(json, "temReferencia")?.equals("true", true) == true
                || json.contains("\"temReferencia\":true")
            val proporcaoBruta = TextoJsonDoChat.valorDaChave(json, "proporcao")?.trim().orEmpty()
                .ifBlank { TextoJsonDoChat.valorDaChave(json, "aspect_ratio")?.trim().orEmpty() }
                .ifBlank { PROPORCAO_PADRAO }
            val resolucaoBruta = TextoJsonDoChat.valorDaChave(json, "resolucao")?.trim()?.lowercase().orEmpty()
                .ifBlank { TextoJsonDoChat.valorDaChave(json, "resolution")?.trim()?.lowercase().orEmpty() }
                .ifBlank { RESOLUCAO_PADRAO }
            val duracao = TextoJsonDoChat.inteiroDaChave(json, "duracao")
                ?: TextoJsonDoChat.inteiroDaChave(json, "duration")
                ?: DURACAO_PADRAO
            val desconhecido = mcps.firstOrNull { it !in MCPS_CONHECIDOS }
            return when {
                mcps.isEmpty() ->
                    ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Recusado("Escolha pelo menos um MCP.")
                desconhecido != null ->
                    ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Recusado("MCP não suportado: $desconhecido")
                prompt.isEmpty() && !temRef ->
                    ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Recusado("Faltou o prompt (ou uma referência).")
                prompt.isEmpty() && MCP_HIGGSFIELD in mcps && !temRef ->
                    ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Recusado("Faltou o prompt.")
                prompt.length > 20_000 ->
                    ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Recusado("O prompt passou do limite.")
                proporcaoBruta !in PROPORCOES_ACEITAS ->
                    ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Recusado("Proporção inválida.")
                resolucaoBruta !in RESOLUCOES_ACEITAS ->
                    ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Recusado("Resolução inválida.")
                duracao !in DURACAO_MINIMA..DURACAO_MAXIMA ->
                    ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Recusado(
                        "A duração deve ser entre $DURACAO_MINIMA e $DURACAO_MAXIMA segundos.",
                    )
                else -> ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Ok(
                    PedidoDeGeracaoDeVideoViaMcpNaColecao(
                        mcps, prompt, modelo, quantidade, temRef, proporcaoBruta, resolucaoBruta, duracao,
                    ),
                )
            }
        }
    }
}

sealed class ResultadoDoPedidoDeGeracaoDeVideoViaMcp {
    data class Ok(val pedido: PedidoDeGeracaoDeVideoViaMcpNaColecao) : ResultadoDoPedidoDeGeracaoDeVideoViaMcp()
    data class Recusado(val mensagem: String) : ResultadoDoPedidoDeGeracaoDeVideoViaMcp()
}

object RespostaJsonDaPonteDeGeracaoDeVideoMcp {
    fun okMcps() =
        """{"ok":true,"itens":[{"id":"higgsfield","nome":"Higgsfield"},{"id":"sjinn","nome":"Sjinn"},{"id":"topview","nome":"Topview"},{"id":"magnific","nome":"Magnific"},{"id":"vivix","nome":"Vivix W1"}]}"""
}

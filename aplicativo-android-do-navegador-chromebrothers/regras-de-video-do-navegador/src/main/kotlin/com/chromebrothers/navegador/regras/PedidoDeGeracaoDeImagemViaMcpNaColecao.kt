package com.chromebrothers.navegador.regras

data class PedidoDeGeracaoDeImagemViaMcpNaColecao(
    /** Um ou mais MCPs; a quantidade vale **por** MCP. */
    val mcps: List<String>,
    val prompt: String,
    val modelo: String,
    val quantidade: Int,
    val temReferencia: Boolean,
    val proporcao: String,
    val resolucao: String,
) {
    /** Compat: primeiro MCP (pedidos antigos / um só). */
    val mcp: String get() = mcps.first()

    /** Id do modelo que [mcpAlvo] entende (traduz a família quando há mais de um MCP). */
    fun modeloPara(mcpAlvo: String): String = EquivalenciaDeModelosDeImagemEntreMcps.idParaMcp(modelo, mcpAlvo)

    companion object {
        const val MCP_HIGGSFIELD = "higgsfield"
        const val MCP_SJINN = "sjinn"
        const val MCP_MAGNIFIC = "magnific"
        val MCPS_CONHECIDOS = setOf(MCP_HIGGSFIELD, MCP_SJINN, MCP_MAGNIFIC)

        /** Default do componente da coleção (não altera o default do chat). */
        const val MODELO_PADRAO_DA_COLECAO = "nano_banana_2_1"
        const val MODELO_PADRAO_DA_COLECAO_SJINN = "gpt-image-2"
        const val MODELO_PADRAO_DA_COLECAO_MAGNIFIC = "imagen-nano-banana-2-1"

        const val LIMITE_DE_REFERENCIAS = 4

        val PROPORCOES_ACEITAS = setOf("1:1", "16:9", "9:16", "4:3", "3:4")
        const val PROPORCAO_PADRAO = "1:1"

        val RESOLUCOES_ACEITAS = setOf("1k", "2k", "4k")
        const val RESOLUCAO_PADRAO = "1k"

        fun modeloPadraoPara(mcp: String): String = when (mcp) {
            MCP_SJINN -> MODELO_PADRAO_DA_COLECAO_SJINN
            MCP_MAGNIFIC -> MODELO_PADRAO_DA_COLECAO_MAGNIFIC
            else -> MODELO_PADRAO_DA_COLECAO
        }

        fun modeloPadraoPara(mcps: List<String>): String =
            if (mcps.size <= 1) modeloPadraoPara(mcps.firstOrNull() ?: MCP_HIGGSFIELD)
            else EquivalenciaDeModelosDeImagemEntreMcps.FAMILIA_PADRAO

        private val regexArrayMcps =
            Regex(""""mcps"\s*:\s*\[(.*?)\]""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        private val regexItemAspas = Regex(""""([^"]+)"""")

        /** `mcps:[...]` tem prioridade; sem ele, usa `mcp` (default Higgsfield). */
        fun lerMcpsDoJson(json: String): List<String> {
            val doArray = regexArrayMcps.find(json)?.groupValues?.getOrNull(1)?.let { bloco ->
                regexItemAspas.findAll(bloco).map { it.groupValues[1].trim().lowercase() }
                    .filter { it.isNotEmpty() }.toList()
            }.orEmpty()
            if (doArray.isNotEmpty()) return doArray.distinct()
            val singular = TextoJsonDoChat.valorDaChave(json, "mcp")?.trim()?.lowercase().orEmpty()
            return listOf(singular.ifBlank { MCP_HIGGSFIELD })
        }

        fun ler(json: String): ResultadoDoPedidoDeGeracaoDeImagemViaMcp {
            val mcps = lerMcpsDoJson(json)
            val prompt = TextoJsonDoChat.valorDaChave(json, "prompt")?.trim().orEmpty()
            val modeloBruto = TextoJsonDoChat.valorDaChave(json, "modelo")?.trim().orEmpty()
            val modelo = modeloBruto.ifBlank { modeloPadraoPara(mcps) }
            val q = TextoJsonDoChat.inteiroDaChave(json, "quantidade") ?: 1
            val temRef = TextoJsonDoChat.valorDaChave(json, "temReferencia")?.equals("true", true) == true
                || json.contains("\"temReferencia\":true")
            val proporcaoBruta = TextoJsonDoChat.valorDaChave(json, "proporcao")?.trim().orEmpty()
                .ifBlank { TextoJsonDoChat.valorDaChave(json, "aspect_ratio")?.trim().orEmpty() }
                .ifBlank { PROPORCAO_PADRAO }
            val resolucaoBruta = TextoJsonDoChat.valorDaChave(json, "resolucao")?.trim()?.lowercase().orEmpty()
                .ifBlank { TextoJsonDoChat.valorDaChave(json, "resolution")?.trim()?.lowercase().orEmpty() }
                .ifBlank { RESOLUCAO_PADRAO }
            val desconhecido = mcps.firstOrNull { it !in MCPS_CONHECIDOS }
            return when {
                prompt.isEmpty() -> ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("Faltou o prompt.")
                prompt.length > GeracaoDeImagemEVideoPeloHiggsfieldNoChat.LIMITE_DO_PROMPT ->
                    ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("O prompt passou do limite.")
                desconhecido != null ->
                    ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("MCP não suportado: $desconhecido")
                mcps.size > 1 && EquivalenciaDeModelosDeImagemEntreMcps.familiaDe(modelo) == null ->
                    ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("Este modelo não existe em todos os MCPs marcados.")
                q !in 1..4 -> ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("A quantidade deve ser entre 1 e 4.")
                proporcaoBruta !in PROPORCOES_ACEITAS ->
                    ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("Proporção inválida.")
                resolucaoBruta !in RESOLUCOES_ACEITAS ->
                    ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("Resolução inválida.")
                else -> ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok(
                    PedidoDeGeracaoDeImagemViaMcpNaColecao(
                        mcps, prompt, modelo, q, temRef, proporcaoBruta, resolucaoBruta,
                    ),
                )
            }
        }
    }
}

sealed class ResultadoDoPedidoDeGeracaoDeImagemViaMcp {
    data class Ok(val pedido: PedidoDeGeracaoDeImagemViaMcpNaColecao) : ResultadoDoPedidoDeGeracaoDeImagemViaMcp()
    data class Recusado(val mensagem: String) : ResultadoDoPedidoDeGeracaoDeImagemViaMcp()
}

data class ImagemGeradaNaColecao(
    val idBiblioteca: String,
    val fileUrl: String,
    val previewDataUrl: String,
    val mcp: String = "",
)

object RespostaJsonDaPonteDeGeracaoMcp {
    fun erro(mensagem: String) =
        """{"ok":false,"erro":"${TextoJsonDoChat.escapar(mensagem)}"}"""
    fun okAndamento() = """{"ok":true,"andamento":true}"""
    fun okMcps() =
        """{"ok":true,"itens":[{"id":"higgsfield","nome":"Higgsfield"},{"id":"sjinn","nome":"Sjinn"},{"id":"magnific","nome":"Magnific"}]}"""
    fun okLimpar() = """{"ok":true}"""

    /** Converte caminho absoluto do disco em `file://…` aceito pelo lift-and-drag da coleção. */
    fun fileUrlDeCaminho(caminhoAbsoluto: String): String =
        java.io.File(caminhoAbsoluto).toURI().toString()

    /**
     * JSON final da grade: cada item é `(idBiblioteca, fileUrl, previewDataUrl)`.
     * [fileUrl] precisa ser `file://` / `content://` / `http(s)` para o arraste ao editor.
     */
    fun jsonFinalDosItens(itens: List<Triple<String, String, String>>, erro: String?): String =
        jsonFinalDasImagens(itens.map { (id, fileUrl, preview) -> ImagemGeradaNaColecao(id, fileUrl, preview) }, erro)

    /** Igual a [jsonFinalDosItens], com o MCP de origem em cada item (`mcp`, quando informado). */
    fun jsonFinalDasImagens(itens: List<ImagemGeradaNaColecao>, erro: String?): String {
        val lista = itens.joinToString(",") { it ->
            val mcp = if (it.mcp.isBlank()) "" else ""","mcp":"${TextoJsonDoChat.escapar(it.mcp)}""""
            """{"idBiblioteca":"${TextoJsonDoChat.escapar(it.idBiblioteca)}","fileUrl":"${TextoJsonDoChat.escapar(it.fileUrl)}","previewDataUrl":"${TextoJsonDoChat.escapar(it.previewDataUrl)}"$mcp}"""
        }
        return if (erro == null) {
            """{"ok":true,"itens":[$lista]}"""
        } else {
            """{"ok":false,"itens":[$lista],"erro":"${TextoJsonDoChat.escapar(erro)}"}"""
        }
    }

    /** Snapshot das referências atuais para a UI: `(previewDataUrl, nome)`. */
    fun okReferencias(itens: List<Pair<String, String>>): String {
        val lista = itens.joinToString(",") { (preview, nome) ->
            """{"previewDataUrl":"${TextoJsonDoChat.escapar(preview)}","nome":"${TextoJsonDoChat.escapar(nome)}"}"""
        }
        return """{"ok":true,"itens":[$lista],"quantidade":${itens.size}}"""
    }
}

package com.chromebrothers.navegador.regras

/**
 * Famílias canônicas de modelos de vídeo compartilhadas entre MCPs.
 * A interseção da UI usa só famílias que têm id mapeado em **todos** os MCPs marcados.
 */
object EquivalenciaDeModelosDeVideoEntreMcps {
    data class Familia(
        val id: String,
        val nome: String,
        /** mcpId → id local do provedor */
        val idsPorMcp: Map<String, String>,
    )

    private val H = PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_HIGGSFIELD
    private val S = PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_SJINN
    private val T = PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_TOPVIEW
    private val M = PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_MAGNIFIC

    val FAMILIAS: List<Familia> = listOf(
        Familia(
            id = "wan3",
            nome = "Wan 3.0",
            idsPorMcp = mapOf(
                S to "wan3",
                H to "wan3_0",
                T to "wan-3-0",
                M to "wan-3-0",
            ),
        ),
        Familia(
            id = "wan3-prime",
            nome = "Wan 3.0 Prime",
            idsPorMcp = mapOf(
                H to "wan3_0_prime",
                T to "wan-3-0-prime",
                M to "wan-3-0-prime",
            ),
        ),
        Familia(
            id = "wan-2-7",
            nome = "Wan 2.7",
            idsPorMcp = mapOf(
                H to "wan2_7",
                T to "wan-2-7",
                M to "wan-2-7",
            ),
        ),
        Familia(
            id = "seedance-2-5",
            nome = "Seedance 2.5",
            idsPorMcp = mapOf(
                S to "seedance2.5",
                T to "seedance-2-5",
                M to "bytedance-seedance-pro-2.5",
            ),
        ),
        Familia(
            id = "seedance-2-0",
            nome = "Seedance 2.0",
            idsPorMcp = mapOf(
                S to "seedance2.0",
                H to "seedance_2_0",
                T to "seedance-2-0",
                M to "bytedance-seedance-pro-2.0",
            ),
        ),
        Familia(
            id = "kling-4",
            nome = "Kling 4.0",
            idsPorMcp = mapOf(
                S to "kling4",
                H to "kling4_0",
                T to "kling-4-0",
                M to "kling-40-omni",
            ),
        ),
        Familia(
            id = "kling-v3",
            nome = "Kling V3",
            idsPorMcp = mapOf(
                S to "kling3",
                H to "kling3_0",
                T to "kling-v3",
                M to "kling-30",
            ),
        ),
        Familia(
            id = "gemini-omni",
            nome = "Gemini Omni",
            idsPorMcp = mapOf(
                S to "gemini-omni-1.1",
                T to "gemini-omni-flash",
                M to "gemini-omni-1_1",
            ),
        ),
    )

    fun familiaPorId(familiaId: String): Familia? =
        FAMILIAS.firstOrNull { it.id.equals(familiaId.trim(), ignoreCase = true) }

    /** Resolve família a partir de id/nome de qualquer MCP. */
    fun familiaDe(modeloIdOuNome: String): Familia? {
        val bruto = modeloIdOuNome.trim()
        if (bruto.isEmpty()) return null
        familiaPorId(bruto)?.let { return it }
        val baixo = bruto.lowercase().replace('_', '-').replace(' ', '-')
        for (f in FAMILIAS) {
            if (f.nome.equals(bruto, ignoreCase = true)) return f
            if (f.idsPorMcp.values.any { it.equals(bruto, ignoreCase = true) }) return f
            if (f.idsPorMcp.values.any {
                    it.lowercase().replace('_', '-') == baixo
                }
            ) {
                return f
            }
        }
        return when {
            baixo.contains("wan3") && baixo.contains("prime") -> familiaPorId("wan3-prime")
            baixo.contains("wan3") || baixo.contains("wan-3") || baixo == "wan-3.0" -> familiaPorId("wan3")
            baixo.contains("wan2-7") || baixo.contains("wan-2-7") || baixo.contains("wan-2.7") ->
                familiaPorId("wan-2-7")
            baixo.contains("seedance") && (baixo.contains("2-5") || baixo.contains("2.5") || baixo.contains("25")) ->
                familiaPorId("seedance-2-5")
            baixo.contains("seedance") -> familiaPorId("seedance-2-0")
            baixo.contains("kling") && baixo.contains("4") -> familiaPorId("kling-4")
            baixo.contains("kling") && (baixo.contains("3") || baixo.contains("v3")) -> familiaPorId("kling-v3")
            baixo.contains("gemini") && baixo.contains("omni") -> familiaPorId("gemini-omni")
            else -> null
        }
    }

    fun idParaMcp(familiaId: String, mcp: String): String? {
        val f = familiaPorId(familiaId) ?: familiaDe(familiaId) ?: return null
        return f.idsPorMcp[mcp.trim().lowercase()]
    }

    /**
     * Modelos comuns a todos os [mcps].
     * Retorno: id = família canônica (ex. `wan3`), nome amigável.
     */
    fun intersecao(mcps: List<String>): List<Pair<String, String>> {
        val lista = mcps.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct()
        if (lista.isEmpty()) return emptyList()
        return FAMILIAS
            .filter { familia -> lista.all { mcp -> familia.idsPorMcp.containsKey(mcp) } }
            .map { it.id to it.nome }
    }

    fun jsonDaIntersecao(mcps: List<String>): String {
        val itens = intersecao(mcps)
        val lista = itens.joinToString(",") { (id, nome) ->
            """{"id":"${TextoJsonDoChat.escapar(id)}","nome":"${TextoJsonDoChat.escapar(nome)}"}"""
        }
        return """{"ok":true,"itens":[$lista]}"""
    }

    fun familiaPadraoPara(mcps: List<String>): String {
        val comuns = intersecao(mcps)
        if (comuns.any { it.first == "wan3" }) return "wan3"
        return comuns.firstOrNull()?.first.orEmpty()
    }
}

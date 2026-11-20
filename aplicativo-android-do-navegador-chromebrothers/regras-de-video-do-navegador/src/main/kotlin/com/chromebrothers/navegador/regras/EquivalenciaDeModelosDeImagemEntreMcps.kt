package com.chromebrothers.navegador.regras

/**
 * Famílias de modelos de imagem que existem em mais de um MCP.
 * Com dois MCPs marcados, a UI mostra só as famílias mapeadas em **todos** eles.
 * Só entram ids confirmados nas duas listas (Higgsfield e Sjinn).
 */
object EquivalenciaDeModelosDeImagemEntreMcps {
    data class Familia(
        val id: String,
        val nome: String,
        /** mcpId → id local do provedor */
        val idsPorMcp: Map<String, String>,
    )

    private val H = PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_HIGGSFIELD
    private val S = PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_SJINN
    private val M = PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_MAGNIFIC

    const val FAMILIA_PADRAO = "nano-banana-2-1"

    val FAMILIAS: List<Familia> = listOf(
        Familia(FAMILIA_PADRAO, "Nano Banana 2.1", mapOf(H to "nano_banana_2_1", S to "nano-banana-2.1", M to "imagen-nano-banana-2-1")),
        Familia("nano-banana-2", "Nano Banana 2", mapOf(H to "nano_banana_2", S to "nano-banana-2", M to "imagen-nano-banana-2-flash")),
        Familia("nano-banana-pro", "Nano Banana Pro", mapOf(H to "nano_banana_pro", S to "nano-banana-pro", M to "imagen-nano-banana-2")),
    )

    fun familiaPorId(id: String): Familia? =
        FAMILIAS.firstOrNull { it.id.equals(id.trim(), ignoreCase = true) }

    /** Resolve a família a partir do id canônico ou do id local de qualquer MCP. */
    fun familiaDe(modelo: String): Familia? {
        val bruto = modelo.trim()
        if (bruto.isEmpty()) return null
        familiaPorId(bruto)?.let { return it }
        return FAMILIAS.firstOrNull { f -> f.idsPorMcp.values.any { it.equals(bruto, ignoreCase = true) } }
    }

    /** Id que o [mcp] entende para o [modelo] pedido; sem família conhecida, devolve o próprio [modelo]. */
    fun idParaMcp(modelo: String, mcp: String): String =
        familiaDe(modelo)?.idsPorMcp?.get(mcp.trim().lowercase()) ?: modelo.trim()

    fun intersecao(mcps: List<String>): List<Familia> {
        val lista = mcps.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct()
        if (lista.isEmpty()) return emptyList()
        return FAMILIAS.filter { f -> lista.all { f.idsPorMcp.containsKey(it) } }
    }

    fun jsonDaIntersecao(mcps: List<String>): String {
        val lista = intersecao(mcps).joinToString(",") { f ->
            """{"id":"${TextoJsonDoChat.escapar(f.id)}","nome":"${TextoJsonDoChat.escapar(f.nome)}"}"""
        }
        return """{"ok":true,"itens":[$lista]}"""
    }
}

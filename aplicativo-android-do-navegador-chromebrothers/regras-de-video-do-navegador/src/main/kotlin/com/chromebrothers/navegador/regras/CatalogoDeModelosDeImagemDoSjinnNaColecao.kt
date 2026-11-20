package com.chromebrothers.navegador.regras

/**
 * Catálogo estático alinhado aos modelos de imagem do MCP/Tool API Sjinn.
 * Usado enquanto o OAuth MCP (CIMD) não está disponível para clientes Android custom.
 */
object CatalogoDeModelosDeImagemDoSjinnNaColecao {
    data class Item(val id: String, val nome: String, val toolType: String)

    private val ITENS = listOf(
        Item("gpt-image-2", "GPT Image 2", "gpt-image-2-api"),
        Item("gpt-image-2.5-flare", "GPT Image 2.5 Flare", "gpt-image-2.5-api"),
        Item("gpt-image-2.5-sunburst", "GPT Image 2.5 Sunburst", "gpt-image-2.5-sunburst-api"),
        Item("nano-banana-pro", "Nano Banana Pro", "nano-banana-image-pro-api"),
        Item("nano-banana-2", "Nano Banana 2", "nano-banana-image-2-api"),
        Item("nano-banana-2.1", "Nano Banana 2.1", "nano-banana-image-2.1-api"),
    )

    fun itens(): List<Pair<String, String>> = ITENS.map { it.id to it.nome }

    fun toolTypeDe(modeloId: String): String {
        val id = modeloId.trim()
        return ITENS.firstOrNull { it.id.equals(id, ignoreCase = true) }?.toolType
            ?: ITENS.first { it.id == PedidoDeGeracaoDeImagemViaMcpNaColecao.MODELO_PADRAO_DA_COLECAO_SJINN }.toolType
    }

    fun jsonDosItensParaPonte(): String {
        val lista = ITENS.joinToString(",") { item ->
            """{"id":"${TextoJsonDoChat.escapar(item.id)}","nome":"${TextoJsonDoChat.escapar(item.nome)}"}"""
        }
        return """{"ok":true,"itens":[$lista]}"""
    }
}

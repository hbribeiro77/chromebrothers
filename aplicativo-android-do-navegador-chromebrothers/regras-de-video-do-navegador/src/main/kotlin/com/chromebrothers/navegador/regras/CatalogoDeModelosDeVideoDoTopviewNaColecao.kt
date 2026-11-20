package com.chromebrothers.navegador.regras

/**
 * Catálogo estático de vídeo Topview.
 * A API/MCP exige o **display name** (ex. `"Seedance 2.5"`), não o slug.
 */
object CatalogoDeModelosDeVideoDoTopviewNaColecao {
    data class Item(val id: String, val nome: String)

    private val ITENS = listOf(
        Item("seedance-2-5", "Seedance 2.5"),
        Item("seedance-2-0", "Seedance 2.0"),
        Item("seedance-2-0-fast", "Seedance 2.0 Fast"),
        Item("kling-v3", "Kling V3"),
        Item("kling-4-0", "Kling 4.0"),
        Item("wan-2-7", "Wan 2.7"),
        Item("wan-3-0", "Wan 3.0"),
        Item("wan-3-0-prime", "Wan 3.0 Prime"),
        Item("gemini-omni-flash", "Gemini Omni Flash"),
    )

    fun itens(): List<Pair<String, String>> = ITENS.map { it.id to it.nome }

    fun displayNameDe(modeloIdOuNome: String): String {
        val bruto = modeloIdOuNome.trim()
        if (bruto.isEmpty()) {
            return ITENS.first { it.id == PedidoDeGeracaoDeVideoViaMcpNaColecao.MODELO_PADRAO_DA_COLECAO_TOPVIEW }.nome
        }
        ITENS.firstOrNull { it.id.equals(bruto, ignoreCase = true) }?.let { return it.nome }
        ITENS.firstOrNull { it.nome.equals(bruto, ignoreCase = true) }?.let { return it.nome }
        return bruto
    }

    fun resolucaoInteira(resolucaoDaUi: String): Int =
        when (resolucaoDaUi.trim().lowercase()) {
            "720p" -> 720
            "1080p" -> 1080
            else -> 480
        }

    fun jsonDosItensParaPonte(): String {
        val lista = ITENS.joinToString(",") { item ->
            """{"id":"${TextoJsonDoChat.escapar(item.id)}","nome":"${TextoJsonDoChat.escapar(item.nome)}"}"""
        }
        return """{"ok":true,"itens":[$lista]}"""
    }
}

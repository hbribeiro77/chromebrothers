package com.chromebrothers.navegador.regras

/** Catálogo estático de vídeo Sjinn (MVP: Wan 3 como default). */
object CatalogoDeModelosDeVideoDoSjinnNaColecao {
    data class Item(val id: String, val nome: String, val toolType: String)

    private val ITENS = listOf(
        Item("wan3", "Wan 3.0", "wan3-video-api"),
        Item("seedance2.5", "Seedance 2.5", "seedance25-video-api"),
        Item("seedance2.0", "Seedance 2.0", "seedance20-video-api"),
        Item("seedance2.0-fast", "Seedance 2.0 Fast", "seedance20-video-api"),
        Item("seedance2.0-mini", "Seedance 2.0 Mini", "seedance20-video-api"),
        Item("kling4", "Kling 4.0", "kling4-omni-video-api"),
        Item("kling3", "Kling 3.0 T2V", "kling3-text-to-video-api"),
        Item("veo3.1-fast", "Veo 3.1 Fast T2V", "veo3-text-to-video-fast-api"),
        Item("minimax-h3", "MiniMax H3", "minimax-h3-video-api"),
        Item("gemini-omni-1.1", "Gemini Omni 1.1", "gemini-omni-11-video-api"),
    )

    fun itens(): List<Pair<String, String>> = ITENS.map { it.id to it.nome }

    fun toolTypeDe(modeloId: String): String {
        val id = modeloId.trim()
        return ITENS.firstOrNull { it.id.equals(id, ignoreCase = true) }?.toolType
            ?: ITENS.first { it.id == PedidoDeGeracaoDeVideoViaMcpNaColecao.MODELO_PADRAO_DA_COLECAO_SJINN }.toolType
    }

    fun jsonDosItensParaPonte(): String {
        val lista = ITENS.joinToString(",") { item ->
            """{"id":"${TextoJsonDoChat.escapar(item.id)}","nome":"${TextoJsonDoChat.escapar(item.nome)}"}"""
        }
        return """{"ok":true,"itens":[$lista]}"""
    }
}

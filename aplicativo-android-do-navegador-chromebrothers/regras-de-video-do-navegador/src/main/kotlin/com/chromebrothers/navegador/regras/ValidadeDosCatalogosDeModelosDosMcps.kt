package com.chromebrothers.navegador.regras

/**
 * Quando buscar de novo a lista de modelos de um MCP.
 * As listas mudam pouco: a busca automática (início do app / abertura da gaveta) só roda com mais de 6 h.
 */
object ValidadeDosCatalogosDeModelosDosMcps {
    const val VALIDADE_MS: Long = 6 * 60 * 60 * 1000L

    /** Nunca guardado, vencido, ou relógio voltou para trás (data mudada no aparelho). */
    fun vencido(guardadoEmMs: Long, agoraMs: Long): Boolean =
        guardadoEmMs <= 0L || agoraMs < guardadoEmMs || agoraMs - guardadoEmMs >= VALIDADE_MS

    /** Só resposta de sucesso com pelo menos um modelo vale guardar. */
    fun valeGuardar(json: String): Boolean =
        json.contains("\"ok\":true") && json.contains("\"id\":")
}

package com.chromebrothers.navegador.regras

/** Lê respostas do MCP Magnific (JSON escapado dentro do tools/call, ou texto indentado). */
object ExtracaoDasCriacoesDoMagnific {
    private val regexIdJson = Regex(""""(?:identifier|creationIdentifier)\\?"\s*:\s*\\?"([A-Za-z0-9_-]{4,})""")
    private val regexIdTexto = Regex("""(?m)^\s*-?\s*identifier: "?([A-Za-z0-9_-]{4,})""")
    private val regexListaIds = Regex(""""identifiers\\?"\s*:\s*\[([^\]]*)]""")
    private val regexItem = Regex("""([A-Za-z0-9_-]{4,})""")

    /**
     * Identificadores das criações novas, na ordem. [ignorar] = identificadores das referências
     * enviadas (a resposta pode ecoá-las).
     */
    fun identificadores(bruto: String, ignorar: Set<String> = emptySet()): List<String> {
        val achados = linkedSetOf<String>()
        regexListaIds.findAll(bruto).forEach { lista ->
            regexItem.findAll(lista.groupValues[1].replace("\\\"", "\"")).forEach { achados += it.groupValues[1] }
        }
        regexIdJson.findAll(bruto).forEach { achados += it.groupValues[1] }
        regexIdTexto.findAll(bruto.replace("\\n", "\n")).forEach { achados += it.groupValues[1] }
        return achados.filter { it !in ignorar }
    }

    sealed class Estado {
        data class Pronto(val url: String) : Estado()
        data object Andando : Estado()
        data class Falhou(val mensagem: String) : Estado()
    }

    private val PRONTO = setOf("completed", "done", "success", "succeeded")
    private val FALHOU = setOf("failed", "error", "cancelled", "canceled", "rejected")

    /** `creation_status`: `{status, results:{url}}` (no structuredContent ou no texto). */
    fun estado(bruto: String): Estado {
        val limpo = bruto.replace("\\\"", "\"").replace("\\/", "/")
        val status = Regex(""""status"\s*:\s*"([a-zA-Z_]+)"""").findAll(limpo).lastOrNull()?.groupValues?.get(1)?.lowercase()
            ?: Regex("""(?m)^\s*status: (\w+)""").find(limpo.replace("\\n", "\n"))?.groupValues?.get(1)?.lowercase()
            ?: return Estado.Andando
        return when (status) {
            in PRONTO -> {
                val url = Regex(""""url"\s*:\s*"(https?://[^"]+)"""").find(limpo)?.groupValues?.get(1)
                    ?: Regex("""(?m)^\s*url: "?(https?://[^"\s]+)""").find(limpo.replace("\\n", "\n"))?.groupValues?.get(1)
                if (url.isNullOrBlank()) Estado.Falhou("O Magnific terminou mas não devolveu o arquivo.") else Estado.Pronto(url)
            }
            in FALHOU -> {
                val msg = Regex(""""(?:error|message|reason)"\s*:\s*"([^"]{1,300})"""").find(limpo)?.groupValues?.get(1)
                Estado.Falhou(msg ?: "A geração falhou no Magnific.")
            }
            else -> Estado.Andando
        }
    }
}

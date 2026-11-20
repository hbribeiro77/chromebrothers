package com.chromebrothers.navegador.regras

/** Converte a resolução da UI da coleção (`1k`/`2k`/`4k`) para o casing da API/MCP Sjinn. */
object ResolucaoDoPedidoDeGeracaoParaOMcpDoSjinn {
    fun paraApi(resolucaoDaUi: String): String =
        when (resolucaoDaUi.trim().lowercase()) {
            "2k" -> "2K"
            "4k" -> "4K"
            else -> "1K"
        }
}

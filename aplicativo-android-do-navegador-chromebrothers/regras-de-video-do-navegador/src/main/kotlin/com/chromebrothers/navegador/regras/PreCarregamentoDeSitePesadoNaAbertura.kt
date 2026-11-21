package com.chromebrothers.navegador.regras

import java.net.URI

/**
 * Sites pesados (higgsfield) levam ~1 min de processamento no Gecko deste tablet depois de abrir.
 * Na abertura do app, uma aba já existente desse site começa a carregar escondida (com prioridade
 * alta, senão o Android a prende nos núcleos lentos); quem chega nela depois encontra a página pronta.
 */
object PreCarregamentoDeSitePesadoNaAbertura {
    private val sites = listOf("higgsfield.ai")

    /** Tempo de prioridade alta da aba escondida: depois disso a página já sossegou. */
    const val DURACAO_DA_PRIORIDADE_ALTA_MS = 120_000L

    fun ehSitePesado(endereco: String?): Boolean {
        val host = hostDe(endereco) ?: return false
        return sites.any { host == it || host.endsWith(".$it") }
    }

    /** A última aba (mais à direita) do site pesado que ainda não foi aberta nesta sessão do app. */
    fun abaParaPreCarregar(naoCarregadas: List<Pair<Long, String>>, idAtiva: Long?): Long? =
        naoCarregadas.lastOrNull { (id, endereco) -> id != idAtiva && ehSitePesado(endereco) }?.first

    /**
     * Abrir o site (atalho da página inicial, endereço digitado) com a aba pré-carregada ainda
     * guardada: pula para ela em vez de carregar tudo de novo. Só para o mesmo site.
     */
    fun deveIrParaAbaPreCarregada(destino: String?, enderecoDaPreCarregada: String?): Boolean {
        val a = hostDe(destino) ?: return false
        val b = hostDe(enderecoDaPreCarregada) ?: return false
        return ehSitePesado(destino) && semWww(a) == semWww(b)
    }

    private fun semWww(host: String) = host.removePrefix("www.")

    private fun hostDe(endereco: String?): String? {
        if (endereco.isNullOrBlank()) return null
        val host = runCatching { URI(endereco.trim()).host }.getOrNull() ?: return null
        return host.lowercase().ifBlank { null }
    }
}

package com.chromebrothers.navegador.regras

import java.net.URI

/**
 * Sites pesados (higgsfield) levam ~1 min de processamento no Gecko deste tablet depois de abrir.
 * O app mantém uma "reserva": uma página do site carregando escondida (fora da barra de abas, com
 * prioridade alta, senão o Android a prende nos núcleos lentos). Quem abre o site numa aba recebe a
 * reserva pronta, e outra começa a ser preparada para a próxima vez.
 * Abas do site reabertas na partida também carregam escondidas, para estarem prontas quando abertas.
 */
object PreCarregamentoDeSitePesadoNaAbertura {
    private val sites = listOf("higgsfield.ai")

    /** É este endereço que fica pronto na reserva. */
    const val ENDERECO_PADRAO = "https://higgsfield.ai/"

    /** Tempo de prioridade alta de uma página escondida: depois disso ela já sossegou. */
    const val DURACAO_DA_PRIORIDADE_ALTA_MS = 120_000L

    /** Espera para preparar a reserva quando o processador já está ocupado com o mesmo site. */
    const val ATRASO_DA_RESERVA_COM_O_SITE_CARREGANDO_MS = 30_000L

    /** Espera para repor a reserva depois que ela foi usada (deixa a página recém-aberta assentar). */
    const val ATRASO_PARA_REPOR_A_RESERVA_MS = 10_000L

    /**
     * Com a aba ativa no próprio site, a reserva não é preparada (as duas páginas pesadas disputariam
     * CPU/GPU e a visível engasgaria): confere de novo depois deste tempo.
     */
    const val ATRASO_ENQUANTO_NO_SITE_MS = 20_000L

    fun ehSitePesado(endereco: String?): Boolean {
        val host = hostDe(endereco) ?: return false
        return sites.any { host == it || host.endsWith(".$it") }
    }

    /** A última aba (mais à direita) do site pesado que ainda não foi aberta nesta sessão do app. */
    fun abaParaPreCarregar(naoCarregadas: List<Pair<Long, String>>, idAtiva: Long?): Long? =
        naoCarregadas.lastOrNull { (id, endereco) -> id != idAtiva && ehSitePesado(endereco) }?.first

    /**
     * Abrir a página inicial do site (atalho, domínio digitado) usa a reserva pronta, que também
     * está nela. Outra página do site (higgsfield.ai/video) carrega normal: trocar a rota da
     * reserva por dentro não é confiável (o site às vezes ignora e a barra mostraria outra página).
     */
    fun deveUsarReserva(destino: String?, enderecoDaReserva: String?): Boolean {
        val a = hostDe(destino) ?: return false
        val b = hostDe(enderecoDaReserva) ?: return false
        return semWww(a) == semWww(b) && estaNaPaginaInicial(destino) && estaNaPaginaInicial(enderecoDaReserva)
    }

    /**
     * Página inicial do site pesado. Fechar uma aba que está nela guarda a página como reserva
     * (já pronta) em vez de destruí-la: reabrir o site em seguida é instantâneo.
     */
    fun estaNaPaginaInicial(endereco: String?): Boolean {
        if (!ehSitePesado(endereco)) return false
        val uri = runCatching { URI(endereco!!.trim()) }.getOrNull() ?: return false
        val caminho = uri.rawPath.orEmpty()
        return (caminho.isEmpty() || caminho == "/") && uri.rawQuery.isNullOrEmpty()
    }

    private fun semWww(host: String) = host.removePrefix("www.")

    private fun hostDe(endereco: String?): String? {
        if (endereco.isNullOrBlank()) return null
        val host = runCatching { URI(endereco.trim()).host }.getOrNull() ?: return null
        return host.lowercase().ifBlank { null }
    }
}

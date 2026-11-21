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

    fun ehSitePesado(endereco: String?): Boolean {
        val host = hostDe(endereco) ?: return false
        return sites.any { host == it || host.endsWith(".$it") }
    }

    /** A última aba (mais à direita) do site pesado que ainda não foi aberta nesta sessão do app. */
    fun abaParaPreCarregar(naoCarregadas: List<Pair<Long, String>>, idAtiva: Long?): Long? =
        naoCarregadas.lastOrNull { (id, endereco) -> id != idAtiva && ehSitePesado(endereco) }?.first

    /**
     * Abrir qualquer página do site (atalho, endereço digitado, o que a barra completar) usa a
     * reserva pronta; se a página pedida for outra, o próprio site troca de rota por dentro.
     */
    fun deveUsarReserva(destino: String?, enderecoDaReserva: String?): Boolean {
        val a = hostDe(destino) ?: return false
        val b = hostDe(enderecoDaReserva) ?: return false
        return ehSitePesado(destino) && semWww(a) == semWww(b)
    }

    /**
     * Rota (caminho + consulta + âncora) que a reserva ainda precisa abrir para chegar ao destino,
     * ou null se ela já está nele.
     */
    fun rotaParaAbrirNaReserva(destino: String?, enderecoDaReserva: String?): String? {
        val d = rotaDe(destino) ?: return null
        return if (d == rotaDe(enderecoDaReserva)) null else d
    }

    private fun rotaDe(endereco: String?): String? {
        if (endereco.isNullOrBlank()) return null
        val uri = runCatching { URI(endereco.trim()) }.getOrNull() ?: return null
        val caminho = uri.rawPath.orEmpty().ifEmpty { "/" }
        val consulta = uri.rawQuery?.let { "?$it" }.orEmpty()
        val ancora = uri.rawFragment?.let { "#$it" }.orEmpty()
        return caminho + consulta + ancora
    }

    private fun semWww(host: String) = host.removePrefix("www.")

    private fun hostDe(endereco: String?): String? {
        if (endereco.isNullOrBlank()) return null
        val host = runCatching { URI(endereco.trim()).host }.getOrNull() ?: return null
        return host.lowercase().ifBlank { null }
    }
}

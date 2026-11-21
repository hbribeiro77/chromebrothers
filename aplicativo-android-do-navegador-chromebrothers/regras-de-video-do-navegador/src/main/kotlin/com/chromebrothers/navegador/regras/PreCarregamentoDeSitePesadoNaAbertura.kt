package com.chromebrothers.navegador.regras

import java.net.URI

/**
 * Sites pesados (higgsfield) levam ~1 min de processamento no Gecko deste tablet depois de abrir.
 * O app mantém uma "reserva": uma página do site carregando escondida (fora da barra de abas, com
 * prioridade alta, senão o Android a prende nos núcleos lentos). Quem abre o site numa aba recebe a
 * reserva pronta; quando a página sai da aba (aba fechada ou outro site) ela volta a ser a reserva.
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

    /**
     * Página devolvida fora da inicial clica no "Home" do próprio site (o link para "/"), o caminho
     * normal dele. Se não chegar à inicial neste prazo, é descartada.
     */
    const val SCRIPT_DE_VOLTAR_A_PAGINA_INICIAL =
        "(function(){var a=document.querySelector('a[href=\"/\"]');if(!a)return 'sem-link';a.click();return 'ok';})()"
    const val PRAZO_PARA_VOLTAR_A_PAGINA_INICIAL_MS = 3_000L

    /** Prioridade alta da reserva: carregar (~1 min) e aquecer a página inteira. */
    const val DURACAO_DA_RESERVA_MS = 180_000L

    /** Espera, depois de a reserva terminar de carregar, para começar a aquecê-la. */
    const val ATRASO_PARA_AQUECER_MS = 15_000L

    /**
     * Aquecimento da reserva: rola a página até o fim, uma tela por vez, e volta ao topo. O site só
     * monta e desenha cada seção quando ela aparece; assim isso acontece antes de a pessoa chegar.
     */
    const val SCRIPT_DE_AQUECIMENTO =
        "(function(){if(window.__cbAquecendo)return 'ja';window.__cbAquecendo=1;var n=0;" +
            "function p(){var fim=document.documentElement.scrollHeight-innerHeight;" +
            "if(scrollY>=fim-2||n++>=60){setTimeout(function(){scrollTo({top:0,behavior:'instant'});" +
            "window.__cbAquecido=n;},1500);return;}" +
            "scrollTo({top:scrollY+Math.round(innerHeight*0.85),behavior:'instant'});setTimeout(p,900);}" +
            "p();return 'ok';})()"

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

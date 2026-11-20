package com.chromebrothers.navegador.regras

/**
 * Margens para a lista de sugestões ficar sob a caixa de endereço (estilo Chrome).
 *
 * O pai ([raiz_do_navegador]) aplica padding de system bars: as margens do FrameLayout
 * são relativas à área **interna** (depois do padding), então offsets de janela precisam
 * descontar [paddingEsquerdoDoPai] / [paddingTopoDoPai].
 */
object AlinhamentoDaListaDeSugestoesComACaixaDeEnderecoDoNavegador {
    data class Margens(val inicio: Int, val fim: Int)

    fun margens(
        esquerdaDaCaixaNoPai: Int,
        larguraDaCaixa: Int,
        larguraDoPai: Int,
        paddingEsquerdoDoPai: Int = 0,
        paddingDireitoDoPai: Int = 0,
    ): Margens {
        val larguraConteudo = (larguraDoPai - paddingEsquerdoDoPai - paddingDireitoDoPai)
            .coerceAtLeast(0)
        if (larguraConteudo <= 0 || larguraDaCaixa <= 0) {
            return Margens(inicio = 0, fim = 0)
        }
        val esquerdaNoConteudo = esquerdaDaCaixaNoPai - paddingEsquerdoDoPai
        val inicio = esquerdaNoConteudo.coerceIn(0, larguraConteudo)
        val larguraUtil = larguraDaCaixa.coerceAtMost(larguraConteudo - inicio).coerceAtLeast(0)
        val fim = (larguraConteudo - inicio - larguraUtil).coerceAtLeast(0)
        return Margens(inicio = inicio, fim = fim)
    }

    /**
     * topMargin para a moldura começar exatamente sob a âncora (barra / faixa de extensão).
     * @param topoDaAncoraNaJanela relativo ao topo do pai na janela (ancora.y - pai.y)
     */
    fun topoAbaixoDaAncora(
        topoDaAncoraNaJanela: Int,
        alturaDaAncora: Int,
        paddingTopoDoPai: Int,
    ): Int = (topoDaAncoraNaJanela + alturaDaAncora - paddingTopoDoPai).coerceAtLeast(0)
}

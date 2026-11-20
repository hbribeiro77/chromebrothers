package com.chromebrothers.navegador.regras

/**
 * Qual vista conta no arraste fantasma: flutuante = chrome da janela; ancorada = conteúdo na gaveta.
 * Sem isso o MCP na gaveta some do hit-test porque o painel fica GONE.
 */
object HitTestDaJanelaDaColecaoNoArrasteDoFantasma {
    enum class Alvo { PAINEL_FLUTUANTE, CONTEUDO_NA_GAVETA }

    fun alvo(ancoradaNaGaveta: Boolean): Alvo =
        if (ancoradaNaGaveta) Alvo.CONTEUDO_NA_GAVETA else Alvo.PAINEL_FLUTUANTE

    fun podeTestar(
        ancoradaNaGaveta: Boolean,
        painelVisivel: Boolean,
        painelLargura: Int,
        painelAltura: Int,
        conteudoVisivel: Boolean,
        conteudoLargura: Int,
        conteudoAltura: Int,
    ): Boolean = when (alvo(ancoradaNaGaveta)) {
        Alvo.PAINEL_FLUTUANTE -> painelVisivel && painelLargura > 0 && painelAltura > 0
        Alvo.CONTEUDO_NA_GAVETA -> conteudoVisivel && conteudoLargura > 0 && conteudoAltura > 0
    }

    fun pontoDentro(
        x: Float,
        y: Float,
        esquerda: Float,
        topo: Float,
        largura: Int,
        altura: Int,
    ): Boolean =
        x >= esquerda &&
            x <= esquerda + largura &&
            y >= topo &&
            y <= topo + altura
}

package com.chromebrothers.navegador.regras

/**
 * Espelho da peça no editor: vale para imagem e desenho (o desenho vira imagem; texto não), o segundo toque no mesmo eixo desfaz,
 * e os botões ficam abaixo da moldura de transformar.
 */
object EspelhoDaPecaDaComposicao {
    enum class Eixo { HORIZONTAL, VERTICAL }

    data class Botao(
        val eixo: Eixo,
        val esquerda: Float,
        val topo: Float,
        val largura: Float,
        val altura: Float,
    ) {
        fun direita(): Float = esquerda + largura

        fun contem(x: Float, y: Float): Boolean =
            x in esquerda..direita() && y in topo..(topo + altura)
    }

    fun alternar(ligado: Boolean): Boolean = !ligado

    fun aceitaEspelho(ehCamadaDePintura: Boolean): Boolean = ehCamadaDePintura

    fun podeDuplicar(ehCamadaDePintura: Boolean, quantidadeDeCamadas: Int): Boolean =
        ehCamadaDePintura && PilhaDeCamadasDaComposicao.cabeMaisUma(quantidadeDeCamadas)

    /** Imagem ou desenho: “Selecionar tudo” arma a seleção ativa do retângulo, como a ferramenta. */
    fun podeSelecionarInteira(ehCamadaDePintura: Boolean): Boolean = ehCamadaDePintura

    fun selecionarTudoAbreSelecaoAtivaDaFerramenta(): Boolean = true

    fun botoes(centroX: Float, baseY: Float, densidade: Float): List<Botao> {
        val largura = 108f * densidade
        val altura = 32f * densidade
        val folga = 8f * densidade
        val topo = baseY + 10f * densidade
        val total = largura * 2f + folga
        val esquerda = centroX - total / 2f
        return listOf(
            Botao(Eixo.HORIZONTAL, esquerda, topo, largura, altura),
            Botao(Eixo.VERTICAL, esquerda + largura + folga, topo, largura, altura),
        )
    }

    fun eixoNoPonto(botoes: List<Botao>, x: Float, y: Float): Eixo? =
        botoes.firstOrNull { it.contem(x, y) }?.eixo
}

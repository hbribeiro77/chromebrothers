package com.chromebrothers.navegador.regras

data class PontoEmPixelsDoRecorteDaImagem(val x: Float, val y: Float)

/**
 * Converte um ponto do laço (coordenadas da moldura) para o bitmap recortado,
 * usando a escala da imagem-mãe e a origem inteira do crop — não a caixa float da área.
 */
object MapeamentoDoLacoParaPixelsDoRecorteDaImagem {
    fun pontoNoRecorte(
        pontoX: Float,
        pontoY: Float,
        imagemEsquerda: Float,
        imagemTopo: Float,
        escalaX: Float,
        escalaY: Float,
        pixelsEsquerda: Int,
        pixelsTopo: Int,
    ): PontoEmPixelsDoRecorteDaImagem {
        return PontoEmPixelsDoRecorteDaImagem(
            x = (pontoX - imagemEsquerda) * escalaX - pixelsEsquerda,
            y = (pontoY - imagemTopo) * escalaY - pixelsTopo,
        )
    }
}

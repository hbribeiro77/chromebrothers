package com.chromebrothers.navegador.regras

/**
 * Converte um retângulo na moldura para a posição exata dos pixels que
 * [DecisaoDoRetanguloSobreAImagemDaComposicao.pixels] extrai — evita drift de 1px.
 */
object AlinhamentoDoRetanguloAGradeDePixelsDaImagem {
    fun daSelecao(
        recorteEsquerda: Float,
        recorteTopo: Float,
        recorteLargura: Float,
        recorteAltura: Float,
        imagemEsquerda: Float,
        imagemTopo: Float,
        imagemLargura: Float,
        imagemAltura: Float,
        bitmapLargura: Int,
        bitmapAltura: Int,
    ): AreaDoRecorteSobreAImagemDaComposicao? {
        val pixels = DecisaoDoRetanguloSobreAImagemDaComposicao.pixels(
            recorteEsquerda,
            recorteTopo,
            recorteLargura,
            recorteAltura,
            imagemEsquerda,
            imagemTopo,
            imagemLargura,
            imagemAltura,
            bitmapLargura,
            bitmapAltura,
        ) ?: return null
        if (imagemLargura <= 0f || imagemAltura <= 0f || bitmapLargura <= 0 || bitmapAltura <= 0) return null
        // Multiplicação (não divisão por escala): mesma conta que o drawBitmap da imagem-mãe.
        return AreaDoRecorteSobreAImagemDaComposicao(
            esquerda = imagemEsquerda + pixels.esquerda.toFloat() * imagemLargura / bitmapLargura,
            topo = imagemTopo + pixels.topo.toFloat() * imagemAltura / bitmapAltura,
            largura = pixels.largura.toFloat() * imagemLargura / bitmapLargura,
            altura = pixels.altura.toFloat() * imagemAltura / bitmapAltura,
        )
    }
}

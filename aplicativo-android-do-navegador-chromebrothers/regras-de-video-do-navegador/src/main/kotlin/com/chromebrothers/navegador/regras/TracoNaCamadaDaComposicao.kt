package com.chromebrothers.navegador.regras

object DestinoDoTracoNaCamadaAtiva {
    fun aceitaPintura(temCamadaAtivaCompativel: Boolean): Boolean = temCamadaAtivaCompativel
}

object VisibilidadeDaCamadaDaComposicao {
    fun aposAlternar(visivelAgora: Boolean): Boolean = !visivelAgora
}

object TracoNaCamadaDaComposicao {
    fun pontoNormalizado(
        esquerda: Float,
        topo: Float,
        largura: Float,
        altura: Float,
        angulo: Float,
        x: Float,
        y: Float,
    ): PontoLocalDaPeca? {
        if (largura <= 0f || altura <= 0f) return null
        val centroX = esquerda + largura / 2f
        val centroY = topo + altura / 2f
        val local = GiroDaPecaDaComposicao.pontoLocal(angulo, x, y, centroX, centroY)
        return PontoLocalDaPeca(
            x = (local.x - esquerda) / largura,
            y = (local.y - topo) / altura,
        )
    }

    fun paraOQuadro(
        u: Float,
        v: Float,
        esquerda: Float,
        topo: Float,
        largura: Float,
        altura: Float,
        larguraDoQuadro: Float,
        alturaDoQuadro: Float,
    ): PontoLocalDaPeca? {
        if (larguraDoQuadro <= 0f || alturaDoQuadro <= 0f) return null
        return PontoLocalDaPeca(
            x = (esquerda + u * largura) / larguraDoQuadro,
            y = (topo + v * altura) / alturaDoQuadro,
        )
    }

    fun cabeNoRetangulo(u: Float, v: Float): Boolean = u in 0f..1f && v in 0f..1f

    fun remapear(
        u: Float,
        v: Float,
        antigaEsquerda: Float,
        antigaTopo: Float,
        antigaLargura: Float,
        antigaAltura: Float,
        novaEsquerda: Float,
        novaTopo: Float,
        novaLargura: Float,
        novaAltura: Float,
        deslocamentoX: Float,
        deslocamentoY: Float,
    ): PontoLocalDaPeca? {
        if (antigaLargura <= 0f || antigaAltura <= 0f || novaLargura <= 0f || novaAltura <= 0f) return null
        val x = antigaEsquerda + u * antigaLargura + deslocamentoX
        val y = antigaTopo + v * antigaAltura + deslocamentoY
        return PontoLocalDaPeca(
            x = (x - novaEsquerda) / novaLargura,
            y = (y - novaTopo) / novaAltura,
        )
    }

    fun larguraRemapeada(fracao: Float, antigaLargura: Float, novaLargura: Float): Float {
        if (novaLargura <= 0f) return fracao
        return fracao * antigaLargura / novaLargura
    }
}

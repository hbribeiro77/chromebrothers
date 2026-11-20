package com.chromebrothers.navegador.regras

object ApagadorSobreAImagemDaComposicao {
    fun pixel(
        u: Float,
        v: Float,
        molduraLargura: Float,
        molduraAltura: Float,
        esquerda: Float,
        topo: Float,
        largura: Float,
        altura: Float,
        angulo: Float,
        bitmapLargura: Int,
        bitmapAltura: Int,
    ): PontoLocalDaPeca? {
        if (largura <= 0f || altura <= 0f || bitmapLargura <= 0 || bitmapAltura <= 0) return null
        val x = u * molduraLargura
        val y = v * molduraAltura
        val local = GiroDaPecaDaComposicao.pontoLocal(
            angulo,
            x,
            y,
            esquerda + largura / 2f,
            topo + altura / 2f,
        )
        if (local.x !in esquerda..(esquerda + largura)) return null
        if (local.y !in topo..(topo + altura)) return null
        return PontoLocalDaPeca(
            x = (local.x - esquerda) / largura * bitmapLargura,
            y = (local.y - topo) / altura * bitmapAltura,
        )
    }

    fun espessura(
        larguraNormalizada: Float,
        molduraLargura: Float,
        larguraDaPeca: Float,
        bitmapLargura: Int,
    ): Float {
        if (larguraDaPeca <= 0f || molduraLargura <= 0f || bitmapLargura <= 0) return 0f
        return larguraNormalizada * molduraLargura * (bitmapLargura / larguraDaPeca)
    }
}

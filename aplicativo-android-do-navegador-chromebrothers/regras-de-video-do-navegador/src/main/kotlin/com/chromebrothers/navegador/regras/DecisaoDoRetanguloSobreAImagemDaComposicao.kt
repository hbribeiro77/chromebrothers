package com.chromebrothers.navegador.regras

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class EfeitoDoRetanguloSobreAImagem {
    IGNORAR,
    RECORTAR,
    DESTACAR_COM_BURACO,
    COPIAR_POR_CIMA,
}

data class RetanguloEmPixelsDaImagem(
    val esquerda: Int,
    val topo: Int,
    val largura: Int,
    val altura: Int,
)

data class AreaDoRecorteSobreAImagemDaComposicao(
    val esquerda: Float,
    val topo: Float,
    val largura: Float,
    val altura: Float,
)

object DecisaoDoRetanguloSobreAImagemDaComposicao {
    private const val LADO_MINIMO = 32f

    fun cabe(largura: Float, altura: Float): Boolean = largura >= LADO_MINIMO && altura >= LADO_MINIMO

    fun intersecao(
        recorteEsquerda: Float,
        recorteTopo: Float,
        recorteLargura: Float,
        recorteAltura: Float,
        imagemEsquerda: Float,
        imagemTopo: Float,
        imagemLargura: Float,
        imagemAltura: Float,
    ): AreaDoRecorteSobreAImagemDaComposicao? {
        val esquerda = max(recorteEsquerda, imagemEsquerda)
        val topo = max(recorteTopo, imagemTopo)
        val direita = min(recorteEsquerda + recorteLargura.coerceAtLeast(0f), imagemEsquerda + imagemLargura.coerceAtLeast(0f))
        val baixo = min(recorteTopo + recorteAltura.coerceAtLeast(0f), imagemTopo + imagemAltura.coerceAtLeast(0f))
        val largura = direita - esquerda
        val altura = baixo - topo
        if (largura <= 0f || altura <= 0f) return null
        return AreaDoRecorteSobreAImagemDaComposicao(esquerda, topo, largura, altura)
    }

    fun cobreQuaseTudo(
        recorteEsquerda: Float,
        recorteTopo: Float,
        recorteLargura: Float,
        recorteAltura: Float,
        imagemEsquerda: Float,
        imagemTopo: Float,
        imagemLargura: Float,
        imagemAltura: Float,
    ): Boolean {
        return recorteLargura > imagemLargura * 0.98f &&
            recorteAltura > imagemAltura * 0.98f &&
            abs(recorteEsquerda - imagemEsquerda) < 8f &&
            abs(recorteTopo - imagemTopo) < 8f
    }

    fun efeito(destacar: Boolean, cabe: Boolean, cobreQuaseTudo: Boolean): EfeitoDoRetanguloSobreAImagem {
        if (!cabe) return EfeitoDoRetanguloSobreAImagem.IGNORAR
        if (!destacar) {
            return if (cobreQuaseTudo) {
                EfeitoDoRetanguloSobreAImagem.IGNORAR
            } else {
                EfeitoDoRetanguloSobreAImagem.RECORTAR
            }
        }
        return if (cobreQuaseTudo) {
            EfeitoDoRetanguloSobreAImagem.COPIAR_POR_CIMA
        } else {
            EfeitoDoRetanguloSobreAImagem.DESTACAR_COM_BURACO
        }
    }

    fun pixels(
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
    ): RetanguloEmPixelsDaImagem? {
        val area = intersecao(
            recorteEsquerda,
            recorteTopo,
            recorteLargura,
            recorteAltura,
            imagemEsquerda,
            imagemTopo,
            imagemLargura,
            imagemAltura,
        ) ?: return null
        if (bitmapLargura <= 0 || bitmapAltura <= 0 || imagemLargura <= 0f || imagemAltura <= 0f) return null
        val escalaX = bitmapLargura / imagemLargura
        val escalaY = bitmapAltura / imagemAltura
        val esquerda = ((area.esquerda - imagemEsquerda) * escalaX).toInt().coerceIn(0, bitmapLargura - 1)
        val topo = ((area.topo - imagemTopo) * escalaY).toInt().coerceIn(0, bitmapAltura - 1)
        val direita = ((area.esquerda + area.largura - imagemEsquerda) * escalaX).toInt().coerceIn(esquerda + 1, bitmapLargura)
        val baixo = ((area.topo + area.altura - imagemTopo) * escalaY).toInt().coerceIn(topo + 1, bitmapAltura)
        return RetanguloEmPixelsDaImagem(esquerda, topo, direita - esquerda, baixo - topo)
    }
}

package com.chromebrothers.navegador.regras

import kotlin.math.max
import kotlin.math.min

data class RetanguloLimitadoDoRecorteDaComposicao(
    val esquerda: Float,
    val topo: Float,
    val direita: Float,
    val baixo: Float,
)

object LimiteDoRetanguloDeRecorteDentroDaImagemDaComposicao {
    fun aplicar(
        esquerda: Float,
        topo: Float,
        direita: Float,
        baixo: Float,
        imagemEsquerda: Float,
        imagemTopo: Float,
        imagemLargura: Float,
        imagemAltura: Float,
        preservarTamanho: Boolean,
        ladoMinimo: Float,
    ): RetanguloLimitadoDoRecorteDaComposicao {
        val imagemDireita = imagemEsquerda + imagemLargura.coerceAtLeast(0f)
        val imagemBaixo = imagemTopo + imagemAltura.coerceAtLeast(0f)
        if (imagemLargura <= 0f || imagemAltura <= 0f) {
            return RetanguloLimitadoDoRecorteDaComposicao(
                esquerda = imagemEsquerda,
                topo = imagemTopo,
                direita = imagemEsquerda,
                baixo = imagemTopo,
            )
        }
        val minimo = ladoMinimo.coerceAtLeast(1f)
            .coerceAtMost(min(imagemLargura, imagemAltura))
        if (preservarTamanho) {
            val largura = (direita - esquerda).coerceIn(minimo, imagemLargura)
            val altura = (baixo - topo).coerceIn(minimo, imagemAltura)
            val novaEsquerda = esquerda.coerceIn(imagemEsquerda, imagemDireita - largura)
            val novoTopo = topo.coerceIn(imagemTopo, imagemBaixo - altura)
            return RetanguloLimitadoDoRecorteDaComposicao(
                esquerda = novaEsquerda,
                topo = novoTopo,
                direita = novaEsquerda + largura,
                baixo = novoTopo + altura,
            )
        }
        var novaEsquerda = min(esquerda, direita).coerceIn(imagemEsquerda, imagemDireita)
        var novaDireita = max(esquerda, direita).coerceIn(imagemEsquerda, imagemDireita)
        var novoTopo = min(topo, baixo).coerceIn(imagemTopo, imagemBaixo)
        var novoBaixo = max(topo, baixo).coerceIn(imagemTopo, imagemBaixo)
        if (novaDireita - novaEsquerda < minimo) {
            if (novaEsquerda + minimo <= imagemDireita) {
                novaDireita = novaEsquerda + minimo
            } else {
                novaEsquerda = (imagemDireita - minimo).coerceAtLeast(imagemEsquerda)
                novaDireita = imagemDireita
            }
        }
        if (novoBaixo - novoTopo < minimo) {
            if (novoTopo + minimo <= imagemBaixo) {
                novoBaixo = novoTopo + minimo
            } else {
                novoTopo = (imagemBaixo - minimo).coerceAtLeast(imagemTopo)
                novoBaixo = imagemBaixo
            }
        }
        return RetanguloLimitadoDoRecorteDaComposicao(
            esquerda = novaEsquerda,
            topo = novoTopo,
            direita = novaDireita,
            baixo = novoBaixo,
        )
    }
}

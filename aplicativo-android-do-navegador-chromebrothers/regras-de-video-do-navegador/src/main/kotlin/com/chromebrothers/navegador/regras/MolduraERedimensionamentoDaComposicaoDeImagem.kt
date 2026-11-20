package com.chromebrothers.navegador.regras

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class ProporcaoDaMolduraDaComposicao {
    QUADRADO,
    QUATRO_POR_TRES,
    DEZESSEIS_POR_NOVE,
    NOVE_POR_DEZESSEIS,
}

enum class CantoDaComposicao {
    NOROESTE,
    NORDESTE,
    SUDOESTE,
    SUDESTE,
}

data class RetanguloDaComposicao(
    val esquerda: Float,
    val topo: Float,
    val largura: Float,
    val altura: Float,
)

object MolduraDaComposicaoDeImagem {
    const val LADO_LONGO = 1920
    const val LADO_MINIMO = 64
    const val LADO_MAXIMO = 2048

    fun tamanho(proporcao: ProporcaoDaMolduraDaComposicao): Pair<Int, Int> = when (proporcao) {
        ProporcaoDaMolduraDaComposicao.QUADRADO -> LADO_LONGO to LADO_LONGO
        ProporcaoDaMolduraDaComposicao.QUATRO_POR_TRES -> LADO_LONGO to LADO_LONGO * 3 / 4
        ProporcaoDaMolduraDaComposicao.DEZESSEIS_POR_NOVE -> LADO_LONGO to LADO_LONGO * 9 / 16
        ProporcaoDaMolduraDaComposicao.NOVE_POR_DEZESSEIS -> LADO_LONGO * 9 / 16 to LADO_LONGO
    }

    fun reconhecer(largura: Int, altura: Int): ProporcaoDaMolduraDaComposicao? {
        return ProporcaoDaMolduraDaComposicao.values().firstOrNull { proporcao ->
            val (larguraPronta, alturaPronta) = tamanho(proporcao)
            larguraPronta == largura && alturaPronta == altura
        }
    }

    fun limitar(lado: Int): Int = lado.coerceIn(LADO_MINIMO, LADO_MAXIMO)

    fun tamanhoDaProporcaoOriginal(larguraImagem: Int, alturaImagem: Int): Pair<Int, Int> {
        val largura = larguraImagem.coerceAtLeast(1)
        val altura = alturaImagem.coerceAtLeast(1)
        return if (largura >= altura) {
            LADO_LONGO to limitar((LADO_LONGO.toLong() * altura / largura).toInt())
        } else {
            limitar((LADO_LONGO.toLong() * largura / altura).toInt()) to LADO_LONGO
        }
    }

    fun ehProporcaoOriginal(
        larguraMoldura: Int,
        alturaMoldura: Int,
        larguraImagem: Int,
        alturaImagem: Int,
    ): Boolean {
        val (largura, altura) = tamanhoDaProporcaoOriginal(larguraImagem, alturaImagem)
        return larguraMoldura == largura && alturaMoldura == altura
    }

    fun resolucao(largura: Int, altura: Int): String = "$largura × $altura"
}

fun redimensionarMantendoProporcao(
    retangulo: RetanguloDaComposicao,
    canto: CantoDaComposicao,
    x: Float,
    y: Float,
    minimo: Float = 32f,
): RetanguloDaComposicao {
    val ancoraNaEsquerda = canto == CantoDaComposicao.NORDESTE || canto == CantoDaComposicao.SUDESTE
    val ancoraNoTopo = canto == CantoDaComposicao.SUDOESTE || canto == CantoDaComposicao.SUDESTE
    val ancoraX = if (ancoraNaEsquerda) retangulo.esquerda else retangulo.esquerda + retangulo.largura
    val ancoraY = if (ancoraNoTopo) retangulo.topo else retangulo.topo + retangulo.altura
    val escalaX = abs(x - ancoraX) / retangulo.largura.coerceAtLeast(1f)
    val escalaY = abs(y - ancoraY) / retangulo.altura.coerceAtLeast(1f)
    val menorLado = min(retangulo.largura, retangulo.altura).coerceAtLeast(1f)
    val escala = max(escalaX, escalaY).coerceAtLeast(minimo / menorLado)
    val novaLargura = retangulo.largura * escala
    val novaAltura = retangulo.altura * escala
    val esquerda = if (ancoraNaEsquerda) ancoraX else ancoraX - novaLargura
    val topo = if (ancoraNoTopo) ancoraY else ancoraY - novaAltura
    return RetanguloDaComposicao(esquerda, topo, novaLargura, novaAltura)
}

fun encaixarNaMoldura(
    larguraImagem: Float,
    alturaImagem: Float,
    larguraMoldura: Float,
    alturaMoldura: Float,
    ocupacao: Float,
): RetanguloDaComposicao {
    val escala = min(
        larguraMoldura * ocupacao / larguraImagem.coerceAtLeast(1f),
        alturaMoldura * ocupacao / alturaImagem.coerceAtLeast(1f),
    )
    val largura = larguraImagem * escala
    val altura = alturaImagem * escala
    return RetanguloDaComposicao(
        (larguraMoldura - largura) / 2f,
        (alturaMoldura - altura) / 2f,
        largura,
        altura,
    )
}

package com.chromebrothers.navegador.regras

import kotlin.math.abs
import kotlin.math.min

enum class AlcaDoRetanguloDeRecorteDaComposicao {
    MOVER,
    NOROESTE,
    NORDESTE,
    SUDOESTE,
    SUDESTE,
}

data class RetanguloInicialDoRecorteSobreAImagemDaComposicao(
    val esquerda: Float,
    val topo: Float,
    val direita: Float,
    val baixo: Float,
) {
    companion object {
        fun de(
            esquerda: Float,
            topo: Float,
            largura: Float,
            altura: Float,
        ): RetanguloInicialDoRecorteSobreAImagemDaComposicao {
            return RetanguloInicialDoRecorteSobreAImagemDaComposicao(
                esquerda = esquerda,
                topo = topo,
                direita = esquerda + largura.coerceAtLeast(0f),
                baixo = topo + altura.coerceAtLeast(0f),
            )
        }

        fun daMoldura(largura: Float, altura: Float): RetanguloInicialDoRecorteSobreAImagemDaComposicao {
            return de(esquerda = 0f, topo = 0f, largura = largura, altura = altura)
        }
    }
}

data class AreaDaImagemParaSelecaoInteligenteDoRecorte(
    val id: Int,
    val esquerda: Float,
    val topo: Float,
    val largura: Float,
    val altura: Float,
)

object SelecaoInteligenteDoRecorteSobreAComposicao {
    fun idDaImagemNoPonto(
        x: Float,
        y: Float,
        imagensDeBaixoParaCima: List<AreaDaImagemParaSelecaoInteligenteDoRecorte>,
    ): Int? {
        return imagensDeBaixoParaCima.lastOrNull { imagem ->
            val direita = imagem.esquerda + imagem.largura.coerceAtLeast(0f)
            val baixo = imagem.topo + imagem.altura.coerceAtLeast(0f)
            x in imagem.esquerda..direita && y in imagem.topo..baixo
        }?.id
    }
}

object ToqueNasAlcasDoRetanguloDeRecorteDaComposicao {
    fun tipo(
        x: Float,
        y: Float,
        esquerda: Float,
        topo: Float,
        direita: Float,
        baixo: Float,
        alcance: Float,
    ): AlcaDoRetanguloDeRecorteDaComposicao? {
        val largura = (direita - esquerda).coerceAtLeast(0f)
        val altura = (baixo - topo).coerceAtLeast(0f)
        if (largura <= 0f || altura <= 0f) return null
        val alcanceDosCantos = alcance.coerceAtLeast(0f).coerceAtMost(min(largura, altura) / 3f)
        fun perto(px: Float, py: Float) =
            abs(x - px) <= alcanceDosCantos && abs(y - py) <= alcanceDosCantos
        return when {
            perto(esquerda, topo) -> AlcaDoRetanguloDeRecorteDaComposicao.NOROESTE
            perto(direita, topo) -> AlcaDoRetanguloDeRecorteDaComposicao.NORDESTE
            perto(esquerda, baixo) -> AlcaDoRetanguloDeRecorteDaComposicao.SUDOESTE
            perto(direita, baixo) -> AlcaDoRetanguloDeRecorteDaComposicao.SUDESTE
            x in esquerda..direita && y in topo..baixo -> AlcaDoRetanguloDeRecorteDaComposicao.MOVER
            else -> null
        }
    }
}

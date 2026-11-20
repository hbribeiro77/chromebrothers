package com.chromebrothers.navegador.regras

import kotlin.math.hypot

enum class PontaDoTracoDaComposicao { CANETA, MARCADOR, NANQUIM, LAPIS, SETA }

object GeometriaDaPontaDoTraco {
    fun multiplicadorDeLargura(ponta: PontaDoTracoDaComposicao): Float {
        return when (ponta) {
            PontaDoTracoDaComposicao.MARCADOR -> 3.2f
            PontaDoTracoDaComposicao.LAPIS -> 0.7f
            else -> 1f
        }
    }

    fun translucida(ponta: PontaDoTracoDaComposicao): Boolean = ponta == PontaDoTracoDaComposicao.MARCADOR

    fun reta(ponta: PontaDoTracoDaComposicao): Boolean = ponta == PontaDoTracoDaComposicao.MARCADOR
}

object PressaoDaPontaDoTraco {
    fun fatorDeLargura(ponta: PontaDoTracoDaComposicao, pressao: Float): Float {
        val peso = pressao.coerceIn(0f, 1f)
        return when (ponta) {
            PontaDoTracoDaComposicao.NANQUIM -> 0.06f + peso * 3.4f
            PontaDoTracoDaComposicao.LAPIS -> 0.22f + peso * 1.05f
            else -> 0.28f + peso * 1.85f
        }
    }

    fun alpha(ponta: PontaDoTracoDaComposicao, pressao: Float): Int {
        val peso = pressao.coerceIn(0f, 1f)
        return when (ponta) {
            PontaDoTracoDaComposicao.LAPIS -> (80 + peso * 110).toInt()
            PontaDoTracoDaComposicao.MARCADOR -> 102
            else -> 255
        }
    }
}

data class FlechaDoArraste(
    val caudaX: Float,
    val caudaY: Float,
    val pontaX: Float,
    val pontaY: Float,
)

data class AsasDaSeta(
    val esquerdaX: Float,
    val esquerdaY: Float,
    val direitaX: Float,
    val direitaY: Float,
)

object SetaSobreAFolha {
    fun comprimentoDaCabeca(larguraDoTraco: Float): Float = larguraDoTraco * 4f

    fun flechaDoArraste(
        caudaX: Float,
        caudaY: Float,
        dedoX: Float,
        dedoY: Float,
        larguraDoTraco: Float,
    ): FlechaDoArraste? {
        val distancia = hypot(dedoX - caudaX, dedoY - caudaY)
        if (distancia < comprimentoDaCabeca(larguraDoTraco) * 1.2f) return null
        return FlechaDoArraste(
            caudaX = caudaX,
            caudaY = caudaY,
            pontaX = dedoX,
            pontaY = dedoY,
        )
    }

    fun asas(
        origemX: Float,
        origemY: Float,
        destinoX: Float,
        destinoY: Float,
        larguraDoTraco: Float,
    ): AsasDaSeta? {
        val dx = destinoX - origemX
        val dy = destinoY - origemY
        val distancia = hypot(dx, dy)
        val cabeca = comprimentoDaCabeca(larguraDoTraco)
        if (distancia < cabeca * 1.2f) return null
        val ux = dx / distancia
        val uy = dy / distancia
        val recuo = cabeca * 0.8f
        val abertura = cabeca * 0.42f
        val baseX = destinoX - ux * recuo
        val baseY = destinoY - uy * recuo
        val perpendicularX = -uy * abertura
        val perpendicularY = ux * abertura
        return AsasDaSeta(
            esquerdaX = baseX + perpendicularX,
            esquerdaY = baseY + perpendicularY,
            direitaX = baseX - perpendicularX,
            direitaY = baseY - perpendicularY,
        )
    }
}

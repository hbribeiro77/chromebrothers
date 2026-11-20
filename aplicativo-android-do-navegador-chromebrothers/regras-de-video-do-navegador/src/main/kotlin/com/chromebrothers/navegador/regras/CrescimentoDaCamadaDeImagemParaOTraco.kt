package com.chromebrothers.navegador.regras

import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pintar numa camada de imagem menor que a moldura: o traço é gravado no bitmap da imagem, então o
 * que passava da borda se perdia. Aqui se decide o retângulo novo (imagem ∪ traço, em coordenadas da
 * moldura, sem giro) para a camada crescer antes de gravar o traço.
 */
object CrescimentoDaCamadaDeImagemParaOTraco {
    data class Retangulo(val esquerda: Float, val topo: Float, val direita: Float, val base: Float) {
        val largura: Float get() = direita - esquerda
        val altura: Float get() = base - topo
    }

    /** Ponto do traço em unidades da moldura, com o raio do pincel. */
    data class PontoDoTraco(val x: Float, val y: Float, val raio: Float)

    /** null quando o traço inteiro cabe na imagem (nada a fazer). */
    fun novoRetangulo(
        esquerda: Float,
        topo: Float,
        largura: Float,
        altura: Float,
        angulo: Float,
        pontos: List<PontoDoTraco>,
    ): Retangulo? {
        if (pontos.isEmpty() || largura <= 0f || altura <= 0f) return null
        val cx = esquerda + largura / 2f
        val cy = topo + altura / 2f
        val cabe = pontos.all { p ->
            val local = GiroDaPecaDaComposicao.pontoLocal(angulo, p.x, p.y, cx, cy)
            local.x - p.raio >= esquerda && local.x + p.raio <= esquerda + largura &&
                local.y - p.raio >= topo && local.y + p.raio <= topo + altura
        }
        if (cabe) return null
        // Caixa da imagem girada, em coordenadas da moldura.
        val rad = Math.toRadians(angulo.toDouble())
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        for ((dx, dy) in listOf(-1f to -1f, 1f to -1f, 1f to 1f, -1f to 1f)) {
            val lx = dx * largura / 2f
            val ly = dy * altura / 2f
            val x = cx + lx * c - ly * s
            val y = cy + lx * s + ly * c
            minX = min(minX, x); maxX = max(maxX, x)
            minY = min(minY, y); maxY = max(maxY, y)
        }
        for (p in pontos) {
            minX = min(minX, p.x - p.raio); maxX = max(maxX, p.x + p.raio)
            minY = min(minY, p.y - p.raio); maxY = max(maxY, p.y + p.raio)
        }
        return Retangulo(minX, minY, maxX, maxY)
    }

    /**
     * Fator (≤ 1) para o bitmap novo não passar de [ladoMaximo] px num lado nem de [pixelsMaximos] no total,
     * mantendo a densidade da imagem quando dá.
     */
    fun escala(larguraPx: Float, alturaPx: Float, ladoMaximo: Int = 4096, pixelsMaximos: Long = 16_000_000L): Float {
        if (larguraPx <= 0f || alturaPx <= 0f) return 1f
        val porLado = ladoMaximo / max(larguraPx, alturaPx)
        val porArea = sqrt(pixelsMaximos / (larguraPx.toDouble() * alturaPx)).toFloat()
        return min(1f, min(porLado, porArea))
    }

    fun tamanhoEmPixels(medida: Float, densidade: Float): Int = max(1, ceil(medida * densidade).toInt())
}

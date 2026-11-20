package com.chromebrothers.navegador.regras

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Segurar a caneta parada no fim do traço transforma o rabisco numa forma limpa:
 * linha reta, elipse (círculo) ou retângulo. Coordenadas em unidades da moldura (não normalizadas),
 * para um círculo não virar elipse numa moldura retangular.
 */
object FormaLimpaNoFimDoTraco {
    sealed class Forma {
        data class Linha(val x1: Float, val y1: Float, val x2: Float, val y2: Float) : Forma()
        data class Elipse(val cx: Float, val cy: Float, val rx: Float, val ry: Float) : Forma()
        data class Retangulo(val esquerda: Float, val topo: Float, val direita: Float, val base: Float) : Forma()
    }

    data class Ponto(val x: Float, val y: Float)

    const val TEMPO_PARADO_MS = 550L

    /** null quando o rabisco não lembra nenhuma forma (fica como está). */
    fun reconhecer(pontos: List<Ponto>): Forma? {
        if (pontos.size < 3) return null
        val primeiro = pontos.first()
        val ultimo = pontos.last()
        val comprimento = pontos.zipWithNext { a, b -> hypot(b.x - a.x, b.y - a.y) }.sum()
        if (comprimento < 8f) return null
        val minX = pontos.minOf { it.x }
        val maxX = pontos.maxOf { it.x }
        val minY = pontos.minOf { it.y }
        val maxY = pontos.maxOf { it.y }
        val diagonal = hypot(maxX - minX, maxY - minY).coerceAtLeast(1f)

        // Linha: nenhum ponto se afasta muito da reta entre o começo e o fim.
        val corda = hypot(ultimo.x - primeiro.x, ultimo.y - primeiro.y)
        if (corda > 0.6f * comprimento) {
            val desvio = pontos.maxOf { distanciaAteReta(it, primeiro, ultimo) }
            if (desvio <= max(4f, 0.08f * corda)) return Forma.Linha(primeiro.x, primeiro.y, ultimo.x, ultimo.y)
            return null
        }

        // Fechada: o fim volta perto do começo.
        if (corda > 0.25f * diagonal) return null
        val largura = maxX - minX
        val altura = maxY - minY
        if (largura < 6f || altura < 6f) return null
        val cx = (minX + maxX) / 2f
        val cy = (minY + maxY) / 2f
        val erroElipse = pontos.map { erroNaElipse(it, cx, cy, largura / 2f, altura / 2f) }.average()
        val erroRetangulo = pontos.map { erroNoRetangulo(it, minX, minY, maxX, maxY) }.average()
        val tolerancia = 0.12 * min(largura, altura)
        val melhor = min(erroElipse, erroRetangulo)
        if (melhor > tolerancia) return null
        return if (erroRetangulo < erroElipse * 0.8) {
            Forma.Retangulo(minX, minY, maxX, maxY)
        } else {
            // Quase redonda: vira círculo de verdade.
            val r = (largura + altura) / 4f
            if (abs(largura - altura) < 0.15f * max(largura, altura)) Forma.Elipse(cx, cy, r, r)
            else Forma.Elipse(cx, cy, largura / 2f, altura / 2f)
        }
    }

    /** Pontos que desenham a forma (traço fechado repete o primeiro ponto no fim). */
    fun pontosDa(forma: Forma, passosDaCurva: Int = 72): List<Ponto> = when (forma) {
        is Forma.Linha -> listOf(Ponto(forma.x1, forma.y1), Ponto(forma.x2, forma.y2))
        is Forma.Retangulo -> listOf(
            Ponto(forma.esquerda, forma.topo),
            Ponto(forma.direita, forma.topo),
            Ponto(forma.direita, forma.base),
            Ponto(forma.esquerda, forma.base),
            Ponto(forma.esquerda, forma.topo),
        )
        is Forma.Elipse -> (0..passosDaCurva).map { i ->
            val a = 2.0 * PI * i / passosDaCurva
            Ponto(forma.cx + (forma.rx * cos(a)).toFloat(), forma.cy + (forma.ry * sin(a)).toFloat())
        }
    }

    private fun distanciaAteReta(p: Ponto, a: Ponto, b: Ponto): Float {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val len = hypot(dx, dy)
        if (len < 1e-3f) return hypot(p.x - a.x, p.y - a.y)
        return abs(dy * p.x - dx * p.y + b.x * a.y - b.y * a.x) / len
    }

    private fun erroNaElipse(p: Ponto, cx: Float, cy: Float, rx: Float, ry: Float): Double {
        val ang = Math.atan2(((p.y - cy) / ry).toDouble(), ((p.x - cx) / rx).toDouble())
        val ex = cx + rx * cos(ang)
        val ey = cy + ry * sin(ang)
        return hypot(p.x - ex, p.y - ey)
    }

    private fun erroNoRetangulo(p: Ponto, l: Float, t: Float, r: Float, b: Float): Double {
        val dx = min(abs(p.x - l), abs(p.x - r))
        val dy = min(abs(p.y - t), abs(p.y - b))
        return min(dx, dy).toDouble()
    }
}

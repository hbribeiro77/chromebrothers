package com.chromebrothers.navegador.regras

import kotlin.math.pow

/**
 * Ajustes do traço da caneta no editor: estabilização (suaviza a tremedeira) e curva de pressão
 * (caneta mais leve ou mais firme).
 */
object AjusteDoTracoDaCaneta {
    /**
     * Estabilização "de corda": o ponto desenhado só anda quando a caneta se afasta mais que [raio]
     * e então é puxado atrás dela, mantendo essa distância. Não depende de quantas amostras a caneta
     * manda por segundo (a média móvel quase não fazia efeito na S Pen). null = não anda (não desenha).
     */
    fun puxarPelaCorda(x: Float, y: Float, alvoX: Float, alvoY: Float, raio: Float): Pair<Float, Float>? {
        val dx = alvoX - x
        val dy = alvoY - y
        val distancia = kotlin.math.hypot(dx, dy)
        if (distancia <= raio || distancia <= 0f) return null
        val anda = (distancia - raio) / distancia
        return (x + dx * anda) to (y + dy * anda)
    }

    /** Raio da corda na tela: até 32 dp com a estabilização no máximo. */
    fun raioDaCordaEmDp(intensidade: Float): Float = intensidade.coerceIn(0f, 1f) * 32f

    /**
     * Curva de pressão. [curva] −1 = mais firme (precisa apertar mais), 0 = linear, 1 = mais leve
     * (pouca força já dá traço cheio). Mantém 0 → 0 e 1 → 1.
     */
    fun pressao(bruta: Float, curva: Float): Float {
        val p = bruta.coerceIn(0f, 1f)
        val expoente = 2.0.pow(-curva.coerceIn(-1f, 1f) * 1.3).toFloat()
        return p.pow(expoente)
    }
}

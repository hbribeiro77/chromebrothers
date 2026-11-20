package com.chromebrothers.navegador.regras

import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.pow

/**
 * StreamLine (como no Procreate): o traço segue a ponta com um atraso suave, que tira o tremor da mão e
 * arredonda as curvas, e ao levantar a caneta **alcança** a ponta (o fim não fica curto, ao contrário da
 * estabilização de corda). Por pincel, 0..1.
 *
 * O atraso é por tempo (não por evento), para dedo (~60 Hz) e caneta (~240 Hz) ficarem iguais.
 */
object StreamLineDoTraco {
    /** Intervalo usado quando o evento não traz tempo (ou repete) e nos passos do alcance final. */
    const val INTERVALO_PADRAO_MS = 4f

    /** Máximo de passos do alcance no fim (≈ 0,6 s simulados): o fim nunca trava. */
    const val PASSOS_MAXIMOS_DO_ALCANCE = 150

    /** Constante de tempo do atraso em ms: 0 = sem atraso; 100% ≈ 110 ms. */
    fun constanteDeTempoMs(intensidade: Float): Float = 110f * intensidade.coerceIn(0f, 1f).pow(1.6f)

    /** Quanto o ponto suavizado anda em direção à ponta num intervalo de [intervaloMs] (0..1). */
    fun fator(intensidade: Float, intervaloMs: Float): Float {
        val tau = constanteDeTempoMs(intensidade)
        if (tau <= 0.5f) return 1f
        val dt = if (intervaloMs <= 0f) INTERVALO_PADRAO_MS else intervaloMs.coerceAtMost(100f)
        return (1f - exp(-dt / tau)).coerceIn(0f, 1f)
    }

    /** Próximo ponto suavizado, saindo de ([sx], [sy]) em direção à ponta ([x], [y]). */
    fun seguir(sx: Float, sy: Float, x: Float, y: Float, intensidade: Float, intervaloMs: Float): FloatArray {
        val k = fator(intensidade, intervaloMs)
        return floatArrayOf(sx + (x - sx) * k, sy + (y - sy) * k)
    }

    /**
     * Ao levantar a caneta: continua seguindo a ponta parada até ficar a menos de [distanciaMinima];
     * o último ponto é a própria ponta. A curva de chegada é a mesma do atraso (nada de risco reto).
     */
    fun alcancar(sx: Float, sy: Float, x: Float, y: Float, intensidade: Float, distanciaMinima: Float): List<FloatArray> {
        val saida = mutableListOf<FloatArray>()
        val k = fator(intensidade, INTERVALO_PADRAO_MS)
        var cx = sx
        var cy = sy
        if (k < 1f) {
            repeat(PASSOS_MAXIMOS_DO_ALCANCE) {
                if (hypot(x - cx, y - cy) <= distanciaMinima) return@repeat
                cx += (x - cx) * k
                cy += (y - cy) * k
                saida += floatArrayOf(cx, cy)
            }
        }
        saida += floatArrayOf(x, y)
        return saida
    }

    /**
     * Aplica o StreamLine num traço já gravado (área de teste do Estúdio). Cada ponto é
     * `[x, y, pressão, tempoMs]`; a saída tem o mesmo formato, mais os pontos do alcance no fim
     * (com a pressão e o tempo do último ponto).
     */
    fun suavizar(pontos: List<FloatArray>, intensidade: Float, distanciaMinima: Float): List<FloatArray> {
        if (pontos.size < 2 || constanteDeTempoMs(intensidade) <= 0.5f) return pontos
        val saida = ArrayList<FloatArray>(pontos.size + 32)
        var sx = pontos[0][0]
        var sy = pontos[0][1]
        saida += pontos[0]
        for (i in 1 until pontos.size) {
            val p = pontos[i]
            val s = seguir(sx, sy, p[0], p[1], intensidade, p[3] - pontos[i - 1][3])
            sx = s[0]
            sy = s[1]
            saida += floatArrayOf(sx, sy, p[2], p[3])
        }
        val ultimo = pontos.last()
        alcancar(sx, sy, ultimo[0], ultimo[1], intensidade, distanciaMinima).forEach {
            saida += floatArrayOf(it[0], it[1], ultimo[2], ultimo[3])
        }
        return saida
    }
}

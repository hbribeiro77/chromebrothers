package com.chromebrothers.navegador.regras

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/** Um ponto da curva: [x] = pressão da caneta (0..1), [y] = pressão que o pincel usa (0..1). */
data class PontoDaCurvaDePressao(val x: Float, val y: Float)

/**
 * Curva de pressão no estilo do Procreate: pontos arrastáveis ligados por uma curva suave que nunca
 * "volta" (monótona — apertar mais nunca afina). As pontas ficam em x = 0 e x = 1 (só sobem/descem).
 */
class CurvaDePressaoDaCaneta(pontos: List<PontoDaCurvaDePressao>) {
    val pontos: List<PontoDaCurvaDePressao> = normalizar(pontos)

    private val inclinacoes: FloatArray = inclinacoesMonotonas(this.pontos)

    /** Pressão da caneta → pressão do pincel. */
    fun aplicar(pressao: Float): Float {
        val x = pressao.coerceIn(0f, 1f)
        val p = pontos
        if (x <= p.first().x) return p.first().y
        if (x >= p.last().x) return p.last().y
        var i = 0
        while (i < p.size - 2 && x > p[i + 1].x) i++
        val a = p[i]
        val b = p[i + 1]
        val h = (b.x - a.x).coerceAtLeast(1e-6f)
        val t = (x - a.x) / h
        val t2 = t * t
        val t3 = t2 * t
        // Hermite cúbica com as inclinações de Fritsch–Carlson (sem ultrapassar os pontos).
        val y = (2 * t3 - 3 * t2 + 1) * a.y + (t3 - 2 * t2 + t) * h * inclinacoes[i] +
            (-2 * t3 + 3 * t2) * b.y + (t3 - t2) * h * inclinacoes[i + 1]
        return y.coerceIn(0f, 1f)
    }

    fun paraTexto(): String = pontos.joinToString(";") { "%.4f:%.4f".format(java.util.Locale.ROOT, it.x, it.y) }

    fun comPontoMovido(indice: Int, x: Float, y: Float): CurvaDePressaoDaCaneta {
        val novos = pontos.toMutableList()
        if (indice !in novos.indices) return this
        val ultimo = novos.lastIndex
        val novoX = when (indice) {
            0 -> 0f
            ultimo -> 1f
            else -> x.coerceIn(novos[indice - 1].x + DISTANCIA_MINIMA, novos[indice + 1].x - DISTANCIA_MINIMA)
        }
        novos[indice] = PontoDaCurvaDePressao(novoX, y.coerceIn(0f, 1f))
        return CurvaDePressaoDaCaneta(novos)
    }

    /** Novo ponto interno sobre a curva (até [MAXIMO_DE_PONTOS]); devolve a curva e o índice dele. */
    fun comPontoNovo(x: Float): Pair<CurvaDePressaoDaCaneta, Int>? {
        if (pontos.size >= MAXIMO_DE_PONTOS) return null
        val alvo = x.coerceIn(DISTANCIA_MINIMA, 1f - DISTANCIA_MINIMA)
        if (pontos.any { abs(it.x - alvo) < DISTANCIA_MINIMA }) return null
        val novos = (pontos + PontoDaCurvaDePressao(alvo, aplicar(alvo))).sortedBy { it.x }
        return CurvaDePressaoDaCaneta(novos) to novos.indexOfFirst { it.x == alvo }
    }

    /** Tira um ponto interno (as pontas ficam). */
    fun semPonto(indice: Int): CurvaDePressaoDaCaneta {
        if (indice <= 0 || indice >= pontos.lastIndex) return this
        return CurvaDePressaoDaCaneta(pontos.filterIndexed { i, _ -> i != indice })
    }

    override fun equals(other: Any?): Boolean = other is CurvaDePressaoDaCaneta && other.pontos == pontos
    override fun hashCode(): Int = pontos.hashCode()

    companion object {
        const val MAXIMO_DE_PONTOS = 7
        const val DISTANCIA_MINIMA = 0.04f

        val LINEAR = CurvaDePressaoDaCaneta(listOf(PontoDaCurvaDePressao(0f, 0f), PontoDaCurvaDePressao(1f, 1f)))

        /** Predefinições: pouca força já engrossa / normal / precisa apertar. */
        val LEVE = deExpoente(0.55f)
        val FIRME = deExpoente(1.8f)

        fun deTexto(texto: String?): CurvaDePressaoDaCaneta? {
            val pontos = texto?.split(';')?.mapNotNull { par ->
                val x = par.substringBefore(':').trim().toFloatOrNull()
                val y = par.substringAfter(':', "").trim().toFloatOrNull()
                if (x == null || y == null) null else PontoDaCurvaDePressao(x, y)
            }.orEmpty()
            return if (pontos.size >= 2) CurvaDePressaoDaCaneta(pontos) else null
        }

        /** Curva equivalente ao controle antigo (−1 firme … 1 leve), para não perder o ajuste de quem já usava. */
        fun doAjusteAntigo(curva: Float): CurvaDePressaoDaCaneta {
            if (abs(curva) < 0.01f) return LINEAR
            return deExpoente(2.0.pow(-curva.coerceIn(-1f, 1f) * 1.3).toFloat())
        }

        private fun deExpoente(expoente: Float): CurvaDePressaoDaCaneta = CurvaDePressaoDaCaneta(
            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).map { x -> PontoDaCurvaDePressao(x, x.pow(expoente)) },
        )

        private fun normalizar(pontos: List<PontoDaCurvaDePressao>): List<PontoDaCurvaDePressao> {
            val internos = pontos
                .map { PontoDaCurvaDePressao(it.x.coerceIn(0f, 1f), it.y.coerceIn(0f, 1f)) }
                .sortedBy { it.x }
            val inicio = internos.firstOrNull { it.x <= 0.001f }?.y ?: 0f
            val fim = internos.lastOrNull { it.x >= 0.999f }?.y ?: 1f
            val meio = internos.filter { it.x > 0.001f && it.x < 0.999f }
                .fold(mutableListOf<PontoDaCurvaDePressao>()) { lista, ponto ->
                    if (lista.isEmpty() || ponto.x - lista.last().x >= DISTANCIA_MINIMA) lista += ponto
                    lista
                }
                .take(MAXIMO_DE_PONTOS - 2)
            return listOf(PontoDaCurvaDePressao(0f, inicio)) + meio + PontoDaCurvaDePressao(1f, fim)
        }

        /** Inclinações de Fritsch–Carlson: a curva passa pelos pontos sem ondular nem ultrapassar. */
        private fun inclinacoesMonotonas(p: List<PontoDaCurvaDePressao>): FloatArray {
            val n = p.size
            val secantes = FloatArray(n - 1) { i -> (p[i + 1].y - p[i].y) / (p[i + 1].x - p[i].x).coerceAtLeast(1e-6f) }
            val m = FloatArray(n)
            m[0] = secantes[0]
            m[n - 1] = secantes[n - 2]
            for (i in 1 until n - 1) {
                m[i] = if (secantes[i - 1] * secantes[i] <= 0f) 0f else (secantes[i - 1] + secantes[i]) / 2f
            }
            for (i in 0 until n - 1) {
                if (secantes[i] == 0f) {
                    m[i] = 0f
                    m[i + 1] = 0f
                    continue
                }
                val a = m[i] / secantes[i]
                val b = m[i + 1] / secantes[i]
                val soma = a * a + b * b
                if (soma > 9f) {
                    val tau = 3f / sqrt(soma)
                    m[i] = tau * a * secantes[i]
                    m[i + 1] = tau * b * secantes[i]
                }
            }
            return m
        }
    }
}

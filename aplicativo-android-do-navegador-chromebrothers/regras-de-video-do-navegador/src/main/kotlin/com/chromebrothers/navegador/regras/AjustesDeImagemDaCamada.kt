package com.chromebrothers.navegador.regras

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Ajustes de imagem da camada ativa (painel "Ajustes" do editor): brilho, contraste, saturação,
 * matiz, preto e branco (matriz de cor 4×5 no formato do android.graphics.ColorMatrix) e desfoque.
 */
object AjustesDeImagemDaCamada {
    data class Valores(
        /** −100..100 */
        val brilho: Int = 0,
        /** −100..100 */
        val contraste: Int = 0,
        /** −100..100 (−100 = cinza) */
        val saturacao: Int = 0,
        /** −180..180 graus */
        val matiz: Int = 0,
        val pretoEBranco: Boolean = false,
        /** 0..25 (px na resolução da moldura) */
        val desfoque: Int = 0,
    ) {
        val semCor: Boolean get() = brilho == 0 && contraste == 0 && saturacao == 0 && matiz == 0 && !pretoEBranco
        val neutro: Boolean get() = semCor && desfoque == 0
    }

    private val IDENTIDADE = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )

    /** Matriz 4×5 (20 valores, linhas R G B A) que aplica os ajustes de cor na ordem: matiz → saturação → contraste → brilho. */
    fun matriz(v: Valores): FloatArray {
        var m = IDENTIDADE.copyOf()
        if (v.matiz != 0) m = multiplicar(matrizDeMatiz(v.matiz.toFloat()), m)
        val saturacao = if (v.pretoEBranco) 0f else 1f + v.saturacao.coerceIn(-100, 100) / 100f
        if (saturacao != 1f) m = multiplicar(matrizDeSaturacao(saturacao), m)
        if (v.contraste != 0) {
            val c = 1f + v.contraste.coerceIn(-100, 100) / 100f
            val t = 128f * (1f - c)
            m = multiplicar(floatArrayOf(c, 0f, 0f, 0f, t, 0f, c, 0f, 0f, t, 0f, 0f, c, 0f, t, 0f, 0f, 0f, 1f, 0f), m)
        }
        if (v.brilho != 0) {
            val b = v.brilho.coerceIn(-100, 100) * 1.28f
            m = multiplicar(floatArrayOf(1f, 0f, 0f, 0f, b, 0f, 1f, 0f, 0f, b, 0f, 0f, 1f, 0f, b, 0f, 0f, 0f, 1f, 0f), m)
        }
        return m
    }

    private fun matrizDeSaturacao(s: Float): FloatArray {
        val r = 0.213f * (1 - s)
        val g = 0.715f * (1 - s)
        val b = 0.072f * (1 - s)
        return floatArrayOf(
            r + s, g, b, 0f, 0f,
            r, g + s, b, 0f, 0f,
            r, g, b + s, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
    }

    /** Rotação de matiz preservando a luminância (mesma conta do filtro hue-rotate do CSS). */
    private fun matrizDeMatiz(graus: Float): FloatArray {
        val a = Math.toRadians(graus.toDouble())
        val c = cos(a).toFloat()
        val s = sin(a).toFloat()
        return floatArrayOf(
            0.213f + c * 0.787f - s * 0.213f, 0.715f - c * 0.715f - s * 0.715f, 0.072f - c * 0.072f + s * 0.928f, 0f, 0f,
            0.213f - c * 0.213f + s * 0.143f, 0.715f + c * 0.285f + s * 0.140f, 0.072f - c * 0.072f - s * 0.283f, 0f, 0f,
            0.213f - c * 0.213f - s * 0.787f, 0.715f - c * 0.715f + s * 0.715f, 0.072f + c * 0.928f + s * 0.072f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
    }

    /** a × b para matrizes de cor 4×5 (a coluna 5 é o deslocamento). */
    fun multiplicar(a: FloatArray, b: FloatArray): FloatArray {
        val r = FloatArray(20)
        for (linha in 0 until 4) {
            for (coluna in 0 until 5) {
                var soma = if (coluna == 4) a[linha * 5 + 4] else 0f
                for (k in 0 until 4) soma += a[linha * 5 + k] * b[k * 5 + coluna]
                r[linha * 5 + coluna] = soma
            }
        }
        return r
    }

    /** Aplica a matriz a um pixel ARGB (não pré-multiplicado). */
    fun aplicarNoPixel(m: FloatArray, argb: Int): Int {
        val a = (argb ushr 24) and 0xFF
        val r = (argb ushr 16) and 0xFF
        val g = (argb ushr 8) and 0xFF
        val b = argb and 0xFF
        fun canal(i: Int) = (m[i] * r + m[i + 1] * g + m[i + 2] * b + m[i + 3] * a + m[i + 4]).roundToInt().coerceIn(0, 255)
        return (canal(15) shl 24) or (canal(0) shl 16) or (canal(5) shl 8) or canal(10)
    }

    /**
     * Desfoque de caixa (3 passadas ≈ gaussiano) com cor pré-multiplicada, para a borda de áreas
     * transparentes não escurecer. [pixels] ARGB não pré-multiplicado, alterado no lugar.
     */
    fun desfocar(pixels: IntArray, largura: Int, altura: Int, raio: Int) {
        if (raio <= 0 || largura <= 0 || altura <= 0) return
        val n = largura * altura
        val a = FloatArray(n)
        val r = FloatArray(n)
        val g = FloatArray(n)
        val b = FloatArray(n)
        for (i in 0 until n) {
            val p = pixels[i]
            val alfa = ((p ushr 24) and 0xFF) / 255f
            a[i] = alfa * 255f
            r[i] = ((p ushr 16) and 0xFF) * alfa
            g[i] = ((p ushr 8) and 0xFF) * alfa
            b[i] = (p and 0xFF) * alfa
        }
        val temp = FloatArray(max(largura, altura))
        repeat(3) {
            for (canal in arrayOf(a, r, g, b)) {
                caixaHorizontal(canal, largura, altura, raio, temp)
                caixaVertical(canal, largura, altura, raio, temp)
            }
        }
        for (i in 0 until n) {
            val alfa = a[i].coerceIn(0f, 255f)
            if (alfa < 0.5f) {
                pixels[i] = 0
                continue
            }
            val k = 255f / alfa
            val cr = (r[i] * k).roundToInt().coerceIn(0, 255)
            val cg = (g[i] * k).roundToInt().coerceIn(0, 255)
            val cb = (b[i] * k).roundToInt().coerceIn(0, 255)
            pixels[i] = (alfa.roundToInt() shl 24) or (cr shl 16) or (cg shl 8) or cb
        }
    }

    private fun caixaHorizontal(c: FloatArray, w: Int, h: Int, raio: Int, temp: FloatArray) {
        val janela = 2 * raio + 1
        for (y in 0 until h) {
            val base = y * w
            var soma = 0f
            for (k in -raio..raio) soma += c[base + min(w - 1, max(0, k))]
            for (x in 0 until w) {
                temp[x] = soma / janela
                soma += c[base + min(w - 1, x + raio + 1)] - c[base + max(0, x - raio)]
            }
            System.arraycopy(temp, 0, c, base, w)
        }
    }

    private fun caixaVertical(c: FloatArray, w: Int, h: Int, raio: Int, temp: FloatArray) {
        val janela = 2 * raio + 1
        for (x in 0 until w) {
            var soma = 0f
            for (k in -raio..raio) soma += c[min(h - 1, max(0, k)) * w + x]
            for (y in 0 until h) {
                temp[y] = soma / janela
                soma += c[min(h - 1, y + raio + 1) * w + x] - c[max(0, y - raio) * w + x]
            }
            for (y in 0 until h) c[y * w + x] = temp[y]
        }
    }
}

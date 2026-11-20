package com.chromebrothers.navegador.regras

import kotlin.math.abs
import kotlin.math.max

/**
 * Balde de tinta: a área contínua com cor parecida com a do ponto tocado (pixels ARGB, linha a linha).
 * Tolerância 0..1 sobre a maior diferença entre os canais (alfa incluso). A área cresce 1 pixel no fim
 * para cobrir a borda suavizada dos traços (sem isso fica um filete claro junto da linha).
 */
object PreenchimentoDoBalde {
    const val TOLERANCIA_PADRAO = 0.25f

    class Area(val mascara: BooleanArray, val largura: Int, val altura: Int, val quantidade: Int)

    fun parecida(a: Int, b: Int, limite: Int): Boolean {
        val da = abs((a ushr 24) - (b ushr 24))
        // Dois transparentes são iguais, seja qual for a cor "por baixo" do alfa 0.
        if ((a ushr 24) == 0 && (b ushr 24) == 0) return true
        val dr = abs(((a shr 16) and 0xFF) - ((b shr 16) and 0xFF))
        val dg = abs(((a shr 8) and 0xFF) - ((b shr 8) and 0xFF))
        val db = abs((a and 0xFF) - (b and 0xFF))
        return max(max(da, dr), max(dg, db)) <= limite
    }

    /** Área a partir de ([x], [y]); null se o ponto estiver fora. [crescer] = pixels de folga na borda. */
    fun area(pixels: IntArray, largura: Int, altura: Int, x: Int, y: Int, tolerancia: Float, crescer: Int = 1): Area? {
        if (largura <= 0 || altura <= 0 || pixels.size < largura * altura) return null
        if (x !in 0 until largura || y !in 0 until altura) return null
        val limite = (tolerancia.coerceIn(0f, 1f) * 255f).toInt()
        val semente = pixels[y * largura + x]
        val mascara = BooleanArray(largura * altura)
        // Preenchimento por faixas horizontais com pilha própria (sem recursão).
        var pilha = IntArray(1024)
        var topo = 0
        fun empilhar(i: Int) {
            if (topo == pilha.size) pilha = pilha.copyOf(pilha.size * 2)
            pilha[topo++] = i
        }
        empilhar(y * largura + x)
        var quantidade = 0
        while (topo > 0) {
            val i = pilha[--topo]
            if (mascara[i] || !parecida(pixels[i], semente, limite)) continue
            val linha = i / largura
            val base = linha * largura
            var esquerda = i - base
            while (esquerda > 0 && !mascara[base + esquerda - 1] && parecida(pixels[base + esquerda - 1], semente, limite)) esquerda--
            var direita = i - base
            while (direita < largura - 1 && !mascara[base + direita + 1] && parecida(pixels[base + direita + 1], semente, limite)) direita++
            var acimaAberto = false
            var abaixoAberto = false
            for (cx in esquerda..direita) {
                val j = base + cx
                mascara[j] = true
                quantidade++
                if (linha > 0) {
                    val k = j - largura
                    val entra = !mascara[k] && parecida(pixels[k], semente, limite)
                    if (entra && !acimaAberto) empilhar(k)
                    acimaAberto = entra
                }
                if (linha < altura - 1) {
                    val k = j + largura
                    val entra = !mascara[k] && parecida(pixels[k], semente, limite)
                    if (entra && !abaixoAberto) empilhar(k)
                    abaixoAberto = entra
                }
            }
        }
        if (quantidade == 0) return null
        val final = if (crescer > 0) dilatar(mascara, largura, altura, crescer) else mascara
        return Area(final, largura, altura, final.count { it })
    }

    /** Cresce a máscara [raio] pixels (vizinhança em cruz, repetida). */
    fun dilatar(mascara: BooleanArray, largura: Int, altura: Int, raio: Int): BooleanArray {
        var atual = mascara
        repeat(raio.coerceIn(0, 8)) {
            val proxima = atual.copyOf()
            for (yy in 0 until altura) {
                val base = yy * largura
                for (xx in 0 until largura) {
                    if (atual[base + xx]) continue
                    if ((xx > 0 && atual[base + xx - 1]) || (xx < largura - 1 && atual[base + xx + 1]) ||
                        (yy > 0 && atual[base + xx - largura]) || (yy < altura - 1 && atual[base + xx + largura])
                    ) {
                        proxima[base + xx] = true
                    }
                }
            }
            atual = proxima
        }
        return atual
    }

    /** Tamanho da imagem de referência: no máximo [maximoDePixels], na proporção da moldura. */
    fun escalaDaReferencia(largura: Int, altura: Int, maximoDePixels: Int = 4_000_000): Float {
        val total = largura.toLong() * altura.toLong()
        if (total <= 0) return 1f
        if (total <= maximoDePixels) return 1f
        return kotlin.math.sqrt(maximoDePixels.toDouble() / total).toFloat()
    }
}

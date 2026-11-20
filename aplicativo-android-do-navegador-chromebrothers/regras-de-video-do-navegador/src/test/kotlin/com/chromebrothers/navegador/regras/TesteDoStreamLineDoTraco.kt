package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.StreamLineDoTraco as S
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDoStreamLineDoTraco {

    @Test
    fun desligadoSegueAPontaNaHora() {
        assertEquals(1f, S.fator(0f, 4f))
        val p = S.seguir(0f, 0f, 10f, 5f, 0f, 4f)
        assertEquals(10f, p[0])
        assertEquals(5f, p[1])
    }

    @Test
    fun maisIntensidadeAtrasaMais() {
        assertTrue(S.fator(0.8f, 8f) < S.fator(0.3f, 8f))
        assertTrue(S.fator(0.3f, 8f) < 1f)
    }

    @Test
    fun dedoECanetaAndamOMesmoNoMesmoTempo() {
        // 1 evento de 16 ms (dedo) ≈ 4 eventos de 4 ms (caneta).
        val dedo = S.seguir(0f, 0f, 100f, 0f, 0.6f, 16f)[0]
        var x = 0f
        repeat(4) { x = S.seguir(x, 0f, 100f, 0f, 0.6f, 4f)[0] }
        assertTrue(abs(dedo - x) < 0.01f, "dedo=$dedo caneta=$x")
    }

    @Test
    fun tempoRepetidoOuInvalidoNaoTrava() {
        val p = S.seguir(0f, 0f, 100f, 0f, 0.5f, 0f)
        assertTrue(p[0] > 0f && p[0] < 100f)
    }

    @Test
    fun alcanceTerminaExatamenteNaPonta() {
        val caminho = S.alcancar(0f, 0f, 50f, 20f, 0.7f, 0.5f)
        val fim = caminho.last()
        assertEquals(50f, fim[0])
        assertEquals(20f, fim[1])
        assertTrue(caminho.size > 2, "chega em curva, não num salto")
        // Sempre se aproximando.
        var anterior = Float.MAX_VALUE
        caminho.forEach {
            val d = hypot(50f - it[0], 20f - it[1])
            assertTrue(d <= anterior)
            anterior = d
        }
    }

    @Test
    fun alcanceTemLimiteDePassos() {
        val caminho = S.alcancar(0f, 0f, 1_000_000f, 0f, 1f, 0.01f)
        assertTrue(caminho.size <= S.PASSOS_MAXIMOS_DO_ALCANCE + 1)
        assertEquals(1_000_000f, caminho.last()[0])
    }

    @Test
    fun suavizarTiraOTremorEMantemOFim() {
        // Zigue-zague de ±4 em torno de y = 0, andando em x.
        val pontos = (0..40).map { i -> floatArrayOf(i * 5f, if (i % 2 == 0) 4f else -4f, 0.5f, i * 8f) }
        val suave = S.suavizar(pontos, 0.6f, 0.3f)
        val meio = suave.subList(10, 40)
        val amplitude = meio.maxOf { it[1] } - meio.minOf { it[1] }
        assertTrue(amplitude < 4f, "amplitude=$amplitude")
        assertEquals(pontos.last()[0], suave.last()[0])
        assertEquals(pontos.last()[1], suave.last()[1])
    }

    @Test
    fun suavizarDesligadoDevolveIgual() {
        val pontos = listOf(floatArrayOf(0f, 0f, 1f, 0f), floatArrayOf(5f, 5f, 1f, 4f))
        assertTrue(S.suavizar(pontos, 0f, 0.5f) === pontos)
    }
}

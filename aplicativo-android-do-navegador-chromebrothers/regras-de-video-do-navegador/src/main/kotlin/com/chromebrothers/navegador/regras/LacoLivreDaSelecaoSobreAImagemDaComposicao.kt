package com.chromebrothers.navegador.regras

import kotlin.math.hypot

enum class FormaDaSelecaoSobreAImagem { RETANGULO, LACO }

data class PontoDoLacoLivre(val x: Float, val y: Float)

data class CaixaDoLacoLivre(
    val esquerda: Float,
    val topo: Float,
    val largura: Float,
    val altura: Float,
)

object LacoLivreDaSelecaoSobreAImagemDaComposicao {
    const val RAIO_DE_FECHAMENTO = 48f
    private const val LADO_MINIMO = 32f

    fun pontoNaMoldura(x: Float, y: Float, larguraDaMoldura: Float, alturaDaMoldura: Float): PontoDoLacoLivre {
        val largura = larguraDaMoldura.coerceAtLeast(0f)
        val altura = alturaDaMoldura.coerceAtLeast(0f)
        return PontoDoLacoLivre(
            x = x.coerceIn(0f, largura),
            y = y.coerceIn(0f, altura),
        )
    }

    fun soltar(pontos: List<PontoDoLacoLivre>): List<PontoDoLacoLivre> = pontos

    fun segueOToque(ehCaneta: Boolean, comODedo: Boolean): Boolean = ehCaneta || comODedo

    fun desfazerUltimoTraco(
        contorno: List<PontoDoLacoLivre>,
        inicios: List<Int>,
    ): Pair<List<PontoDoLacoLivre>, List<Int>> {
        if (inicios.isEmpty()) return contorno to inicios
        val inicio = inicios.last().coerceIn(0, contorno.size)
        return contorno.take(inicio) to inicios.dropLast(1)
    }

    fun continuar(
        contorno: List<PontoDoLacoLivre>,
        retomada: List<PontoDoLacoLivre>,
    ): List<PontoDoLacoLivre> = contorno + retomada

    fun deveFechar(pontos: List<PontoDoLacoLivre>, raio: Float = RAIO_DE_FECHAMENTO): Boolean {
        if (pontos.size < 4) return false
        val inicio = pontos.first()
        val fim = pontos.last()
        if (hypot(fim.x - inicio.x, fim.y - inicio.y) > raio) return false
        return pontos.dropLast(1).any { hypot(it.x - inicio.x, it.y - inicio.y) > raio }
    }

    fun fechar(pontos: List<PontoDoLacoLivre>, raio: Float = RAIO_DE_FECHAMENTO): List<PontoDoLacoLivre> {
        if (!deveFechar(pontos, raio)) return pontos
        val inicio = pontos.first()
        if (pontos.last() == inicio) return pontos
        return pontos + inicio
    }

    fun caixa(pontos: List<PontoDoLacoLivre>, raio: Float = RAIO_DE_FECHAMENTO): CaixaDoLacoLivre? {
        if (!estaFechado(pontos, raio)) return null
        val esquerda = pontos.minOf { it.x }
        val topo = pontos.minOf { it.y }
        val largura = pontos.maxOf { it.x } - esquerda
        val altura = pontos.maxOf { it.y } - topo
        if (largura < LADO_MINIMO || altura < LADO_MINIMO) return null
        return CaixaDoLacoLivre(esquerda, topo, largura, altura)
    }

    fun contem(x: Float, y: Float, pontos: List<PontoDoLacoLivre>, raio: Float = RAIO_DE_FECHAMENTO): Boolean {
        val anel = anel(pontos, raio)
        if (anel.size < 3) return false
        var dentro = false
        var anterior = anel.last()
        for (atual in anel) {
            val cruza = (atual.y > y) != (anterior.y > y) &&
                x < (anterior.x - atual.x) * (y - atual.y) / (anterior.y - atual.y) + atual.x
            if (cruza) dentro = !dentro
            anterior = atual
        }
        return dentro
    }

    private fun estaFechado(pontos: List<PontoDoLacoLivre>, raio: Float): Boolean {
        if (pontos.size < 4) return false
        if (pontos.first() == pontos.last()) return true
        return deveFechar(pontos, raio)
    }

    private fun anel(pontos: List<PontoDoLacoLivre>, raio: Float): List<PontoDoLacoLivre> {
        if (!estaFechado(pontos, raio)) return emptyList()
        val inicio = pontos.first()
        return if (pontos.last() == inicio) pontos.dropLast(1) else pontos
    }
}

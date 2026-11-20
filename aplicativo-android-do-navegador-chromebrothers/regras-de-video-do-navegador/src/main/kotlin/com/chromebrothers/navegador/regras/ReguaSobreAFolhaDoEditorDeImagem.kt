package com.chromebrothers.navegador.regras

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

enum class ParteDaReguaSobreAFolha { NENHUMA, CORPO, GIRO, INTERRUPTOR }

data class PontoDaReguaSobreAFolha(val x: Float, val y: Float)

data class ReguaSobreAFolha(
    val centroX: Float,
    val centroY: Float,
    val anguloGraus: Float,
    val comprimento: Float,
    val espessura: Float,
    val raioDoCabo: Float,
    val armada: Boolean,
)

object GestoDaReguaSobreAFolha {
    private const val JANELA_DE_ENCAIXE = 6f
    private const val GIRO_DEVAGAR = 12f
    private val encaixes = floatArrayOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f)

    fun angulo(proposto: Float, deltaDoDedo: Float): Float {
        val graus = normalizar(proposto)
        if (abs(deltaDoDedo) > GIRO_DEVAGAR) return graus
        val perto = encaixes.minBy { distancia(it, graus) }
        return if (distancia(perto, graus) <= JANELA_DE_ENCAIXE) perto else graus
    }

    fun variacao(anterior: Float, atual: Float): Float = distancia(anterior, atual)

    fun pontoDoTraco(
        armada: Boolean,
        borracha: Boolean,
        x: Float,
        y: Float,
        ancoraX: Float,
        ancoraY: Float,
        anguloGraus: Float,
    ): PontoDaReguaSobreAFolha {
        if (!armada || borracha) return PontoDaReguaSobreAFolha(x, y)
        val radianos = Math.toRadians(anguloGraus.toDouble())
        val direcaoX = cos(radianos).toFloat()
        val direcaoY = sin(radianos).toFloat()
        val percorrido = (x - ancoraX) * direcaoX + (y - ancoraY) * direcaoY
        return PontoDaReguaSobreAFolha(
            ancoraX + direcaoX * percorrido,
            ancoraY + direcaoY * percorrido,
        )
    }

    fun parte(regua: ReguaSobreAFolha, x: Float, y: Float): ParteDaReguaSobreAFolha {
        val giro = cabo(regua, 1f)
        val interruptor = cabo(regua, -1f)
        if (hypot(x - interruptor.x, y - interruptor.y) <= regua.raioDoCabo) {
            return ParteDaReguaSobreAFolha.INTERRUPTOR
        }
        if (hypot(x - giro.x, y - giro.y) <= regua.raioDoCabo) return ParteDaReguaSobreAFolha.GIRO
        val local = local(regua, x, y)
        val noCorpo = abs(local.x) <= regua.comprimento / 2f && abs(local.y) <= regua.espessura / 2f
        return if (noCorpo) ParteDaReguaSobreAFolha.CORPO else ParteDaReguaSobreAFolha.NENHUMA
    }

    fun mover(regua: ReguaSobreAFolha, dx: Float, dy: Float): ReguaSobreAFolha {
        return regua.copy(
            centroX = regua.centroX + dx,
            centroY = regua.centroY + dy,
        )
    }

    fun alternar(regua: ReguaSobreAFolha): ReguaSobreAFolha = regua.copy(armada = !regua.armada)

    fun posicionar(
        largura: Int,
        altura: Int,
        comprimento: Float,
        espessura: Float,
        raioDoCabo: Float,
    ): ReguaSobreAFolha {
        return ReguaSobreAFolha(
            centroX = largura / 2f,
            centroY = altura * 0.72f,
            anguloGraus = 0f,
            comprimento = comprimento,
            espessura = espessura,
            raioDoCabo = raioDoCabo,
            armada = false,
        )
    }

    private fun cabo(regua: ReguaSobreAFolha, sentido: Float): PontoDaReguaSobreAFolha {
        val radianos = Math.toRadians(regua.anguloGraus.toDouble())
        val distancia = regua.comprimento / 2f + regua.raioDoCabo
        return PontoDaReguaSobreAFolha(
            regua.centroX + cos(radianos).toFloat() * distancia * sentido,
            regua.centroY + sin(radianos).toFloat() * distancia * sentido,
        )
    }

    private fun local(regua: ReguaSobreAFolha, x: Float, y: Float): PontoDaReguaSobreAFolha {
        val radianos = Math.toRadians(regua.anguloGraus.toDouble())
        val cosseno = cos(radianos).toFloat()
        val seno = sin(radianos).toFloat()
        val dx = x - regua.centroX
        val dy = y - regua.centroY
        return PontoDaReguaSobreAFolha(dx * cosseno + dy * seno, -dx * seno + dy * cosseno)
    }

    private fun normalizar(graus: Float): Float {
        var valor = graus % 360f
        if (valor < 0f) valor += 360f
        return valor
    }

    private fun distancia(origem: Float, destino: Float): Float {
        val diferenca = abs(normalizar(origem) - normalizar(destino))
        return min(diferenca, 360f - diferenca)
    }
}

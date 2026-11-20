package com.chromebrothers.navegador.regras

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class PontoLocalDaPeca(val x: Float, val y: Float)

object OrdemDasPecasDaComposicao {
    fun trazerParaFrente(ids: List<Int>, id: Int): List<Int> {
        if (id !in ids || ids.lastOrNull() == id) return ids
        return ids.filter { it != id } + id
    }

    fun enviarParaTras(ids: List<Int>, id: Int): List<Int> {
        if (id !in ids || ids.firstOrNull() == id) return ids
        return listOf(id) + ids.filter { it != id }
    }
}

object PilhaDeCamadasDaComposicao {
    const val LIMITE = 8

    fun cabeMaisUma(quantidade: Int): Boolean = quantidade < LIMITE

    fun mover(idsDeTrasParaFrente: List<Int>, indiceNaLista: Int, destinoNaLista: Int): List<Int> {
        val lista = idsDeTrasParaFrente.asReversed().toMutableList()
        if (indiceNaLista !in lista.indices || destinoNaLista !in lista.indices || indiceNaLista == destinoNaLista) {
            return idsDeTrasParaFrente
        }
        val id = lista.removeAt(indiceNaLista)
        lista.add(destinoNaLista, id)
        return lista.asReversed()
    }
}

object GiroDaPecaDaComposicao {
    private val encaixes = floatArrayOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f)

    fun angulo(centroX: Float, centroY: Float, x: Float, y: Float): Float {
        val graus = Math.toDegrees(atan2((y - centroY).toDouble(), (x - centroX).toDouble())).toFloat()
        return encaixar(normalizar(graus + 90f))
    }

    fun pontoLocal(anguloGraus: Float, x: Float, y: Float, centroX: Float, centroY: Float): PontoLocalDaPeca {
        val radianos = Math.toRadians(-anguloGraus.toDouble())
        val cosseno = cos(radianos)
        val seno = sin(radianos)
        val dx = x - centroX
        val dy = y - centroY
        return PontoLocalDaPeca(
            x = centroX + (dx * cosseno - dy * seno).toFloat(),
            y = centroY + (dx * seno + dy * cosseno).toFloat(),
        )
    }

    private fun normalizar(graus: Float): Float {
        val volta = graus % 360f
        return if (volta < 0f) volta + 360f else volta
    }

    private fun encaixar(graus: Float): Float {
        var melhor = graus
        var menor = 4.01f
        encaixes.forEach { encaixe ->
            val distancia = distanciaAngular(graus, encaixe)
            if (distancia <= 4f && distancia < menor) {
                menor = distancia
                melhor = encaixe
            }
        }
        return melhor
    }

    private fun distanciaAngular(a: Float, b: Float): Float {
        val bruto = abs(a - b) % 360f
        return if (bruto > 180f) 360f - bruto else bruto
    }

    private fun abs(valor: Float): Float = if (valor < 0f) -valor else valor
}

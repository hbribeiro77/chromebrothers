package com.chromebrothers.navegador.regras

object ScrubFinoPorToqueLongoNoPlayerDeVideo {
    const val PASSO_PADRAO_MS = 1_000L
    const val MAXIMO_DE_CELULAS = 24

    fun passoDaFilmstrip(
        duracaoMs: Long,
        maximoDeCelulas: Int = MAXIMO_DE_CELULAS,
        passoMinimoMs: Long = PASSO_PADRAO_MS,
    ): Long {
        val limite = duracaoNaoNegativa(duracaoMs)
        if (limite <= 0L || maximoDeCelulas <= 1) return passoMinimoMs
        val comPassoMinimo = (limite / passoMinimoMs).toInt() + 1
        if (comPassoMinimo <= maximoDeCelulas) return passoMinimoMs
        return (limite / (maximoDeCelulas - 1).toLong()).coerceAtLeast(passoMinimoMs)
    }

    fun temposDaFilmstrip(
        duracaoMs: Long,
        passoMs: Long = PASSO_PADRAO_MS,
    ): List<Long> {
        val limite = duracaoNaoNegativa(duracaoMs)
        if (limite <= 0L) return listOf(0L)
        val passo = passoMs.coerceAtLeast(1L)
        val tempos = mutableListOf<Long>()
        var atual = 0L
        while (atual < limite) {
            tempos += atual
            atual += passo
        }
        if (tempos.lastOrNull() != limite) tempos += limite
        return tempos
    }

    fun deslocamentoDaFaixaPx(
        tempoMs: Long,
        tempos: List<Long>,
        larguraDaCelulaPx: Float,
    ): Float {
        if (tempos.isEmpty() || larguraDaCelulaPx <= 0f) return 0f
        if (tempos.size == 1) return 0f
        val limite = tempos.last()
        val tempo = tempoMs.coerceIn(0L, limite)
        val indice = indiceFracionario(tempo, tempos)
        return -indice * larguraDaCelulaPx
    }

    fun tempoApartirDoDeslocamentoPx(
        deslocamentoPx: Float,
        tempos: List<Long>,
        larguraDaCelulaPx: Float,
    ): Long {
        if (tempos.isEmpty()) return 0L
        if (tempos.size == 1 || larguraDaCelulaPx <= 0f) return tempos.first()
        val maxIndice = (tempos.size - 1).toFloat()
        val indice = (-deslocamentoPx / larguraDaCelulaPx).coerceIn(0f, maxIndice)
        val base = indice.toInt().coerceIn(0, tempos.size - 1)
        if (base >= tempos.size - 1) return tempos.last()
        val frac = indice - base
        val a = tempos[base]
        val b = tempos[base + 1]
        return (a + ((b - a) * frac)).toLong().coerceIn(tempos.first(), tempos.last())
    }

    fun limitarDeslocamentoDaFaixaPx(
        deslocamentoPx: Float,
        quantidadeDeCelulas: Int,
        larguraDaCelulaPx: Float,
    ): Float {
        if (quantidadeDeCelulas <= 1 || larguraDaCelulaPx <= 0f) return 0f
        val minimo = -((quantidadeDeCelulas - 1) * larguraDaCelulaPx)
        return deslocamentoPx.coerceIn(minimo, 0f)
    }

    fun preenchimentoLateralParaCentralizar(
        larguraDoViewportPx: Int,
        larguraDaCelulaPx: Int,
    ): Int {
        if (larguraDoViewportPx <= 0 || larguraDaCelulaPx <= 0) return 0
        return ((larguraDoViewportPx - larguraDaCelulaPx) / 2).coerceAtLeast(0)
    }

    /** Largura total da strip; precisa ser explícita senão o FrameLayout corta na 1ª tela. */
    fun larguraTotalDaFaixaPx(
        quantidadeDeCelulas: Int,
        larguraDaCelulaPx: Int,
        espacoEntreCelulasPx: Int,
        preenchimentoLateralPx: Int,
    ): Int {
        if (quantidadeDeCelulas <= 0 || larguraDaCelulaPx <= 0) return (preenchimentoLateralPx * 2).coerceAtLeast(0)
        val celulas = quantidadeDeCelulas * larguraDaCelulaPx
        val espacos = (quantidadeDeCelulas - 1).coerceAtLeast(0) * espacoEntreCelulasPx.coerceAtLeast(0)
        return preenchimentoLateralPx.coerceAtLeast(0) * 2 + celulas + espacos
    }

    fun formatarTempo(ms: Long): String {
        val total = (ms.coerceAtLeast(0L) / 1000L).toInt()
        val horas = total / 3600
        val minutos = (total % 3600) / 60
        val segundos = total % 60
        return if (horas > 0) {
            "%d:%02d:%02d".format(horas, minutos, segundos)
        } else {
            "%d:%02d".format(minutos, segundos)
        }
    }

    /** Chave estável pro cache da filmstrip (vídeo + tempo arredondado ao passo). */
    fun chaveDoQuadroEmCache(
        chaveDoVideo: String,
        tempoMs: Long,
        passoMs: Long = PASSO_PADRAO_MS,
    ): String {
        val video = chaveDoVideo.trim()
        if (video.isEmpty()) return ""
        val passo = passoMs.coerceAtLeast(1L)
        val tempo = ((tempoMs.coerceAtLeast(0L) + passo / 2L) / passo) * passo
        return "$video|$tempo"
    }

    /** Com o dedo solto e o vídeo tocando, a trilha acompanha a posição atual. */
    fun deveAcompanharReproducaoNaTrilha(
        modoAtivo: Boolean,
        dedoArrastando: Boolean,
        videoTocando: Boolean,
    ): Boolean = modoAtivo && !dedoArrastando && videoTocando

    /** Toque pra controles não pode virar seek: só confirma arraste depois do limiar. */
    fun arrasteConfirmadoPeloDeslocamento(
        deslocamentoDoDedoPx: Float,
        limiarPx: Float,
    ): Boolean = kotlin.math.abs(deslocamentoDoDedoPx) >= limiarPx.coerceAtLeast(0f)

    private fun indiceFracionario(tempoMs: Long, tempos: List<Long>): Float {
        if (tempos.size <= 1) return 0f
        if (tempoMs <= tempos.first()) return 0f
        if (tempoMs >= tempos.last()) return (tempos.size - 1).toFloat()
        for (indice in 0 until tempos.size - 1) {
            val a = tempos[indice]
            val b = tempos[indice + 1]
            if (tempoMs in a..b) {
                if (b == a) return indice.toFloat()
                return indice + (tempoMs - a).toFloat() / (b - a).toFloat()
            }
        }
        return (tempos.size - 1).toFloat()
    }

    private fun duracaoNaoNegativa(duracaoMs: Long): Long = if (duracaoMs > 0L) duracaoMs else 0L
}

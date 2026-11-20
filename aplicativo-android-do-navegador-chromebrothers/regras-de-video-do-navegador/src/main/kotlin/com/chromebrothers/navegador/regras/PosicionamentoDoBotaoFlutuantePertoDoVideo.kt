package com.chromebrothers.navegador.regras

data class PosicaoDoBotaoFlutuantePertoDoVideo(
    val x: Float,
    val y: Float,
    val visivel: Boolean,
)

object PosicionamentoDoBotaoFlutuantePertoDoVideo {
    fun calcular(
        esquerdaCss: Float,
        topoCss: Float,
        larguraCss: Float,
        alturaCss: Float,
        larguraViewportCss: Float,
        alturaViewportCss: Float,
        larguraWebViewPx: Float,
        alturaWebViewPx: Float,
        tamanhoBotaoPx: Float,
        margemPx: Float,
    ): PosicaoDoBotaoFlutuantePertoDoVideo {
        if (larguraViewportCss <= 0f || alturaViewportCss <= 0f || larguraWebViewPx <= 0f || alturaWebViewPx <= 0f) {
            return PosicaoDoBotaoFlutuantePertoDoVideo(0f, 0f, false)
        }
        val escalaX = larguraWebViewPx / larguraViewportCss
        val escalaY = alturaWebViewPx / alturaViewportCss
        val esquerda = esquerdaCss * escalaX
        val topo = topoCss * escalaY
        val direita = esquerda + larguraCss * escalaX
        val base = topo + alturaCss * escalaY
        val visivelEsquerda = maxOf(0f, esquerda)
        val visivelTopo = maxOf(0f, topo)
        val visivelDireita = minOf(larguraWebViewPx, direita)
        val visivelBase = minOf(alturaWebViewPx, base)
        if (visivelDireita - visivelEsquerda < 24f || visivelBase - visivelTopo < 24f) {
            return PosicaoDoBotaoFlutuantePertoDoVideo(0f, 0f, false)
        }
        val centroY = (visivelTopo + visivelBase - tamanhoBotaoPx) / 2f
        val centroX = (visivelEsquerda + visivelDireita - tamanhoBotaoPx) / 2f
        val candidatos = listOf(
            visivelDireita + margemPx to centroY,
            visivelEsquerda - margemPx - tamanhoBotaoPx to centroY,
            centroX to visivelBase + margemPx,
            centroX to visivelTopo - margemPx - tamanhoBotaoPx,
        )
        for ((x, y) in candidatos) {
            if (cabeForaDoVideo(
                    x = x,
                    y = y,
                    tamanho = tamanhoBotaoPx,
                    margem = margemPx,
                    larguraTela = larguraWebViewPx,
                    alturaTela = alturaWebViewPx,
                    videoEsquerda = visivelEsquerda,
                    videoTopo = visivelTopo,
                    videoDireita = visivelDireita,
                    videoBase = visivelBase,
                )
            ) {
                return PosicaoDoBotaoFlutuantePertoDoVideo(x, y, true)
            }
        }
        val limiteX = (larguraWebViewPx - tamanhoBotaoPx - margemPx).coerceAtLeast(margemPx)
        val limiteY = (alturaWebViewPx - tamanhoBotaoPx - margemPx).coerceAtLeast(margemPx)
        return PosicaoDoBotaoFlutuantePertoDoVideo(
            limiteX,
            centroY.coerceIn(margemPx, limiteY),
            true,
        )
    }

    private fun cabeForaDoVideo(
        x: Float,
        y: Float,
        tamanho: Float,
        margem: Float,
        larguraTela: Float,
        alturaTela: Float,
        videoEsquerda: Float,
        videoTopo: Float,
        videoDireita: Float,
        videoBase: Float,
    ): Boolean {
        if (x < margem || y < margem) return false
        if (x + tamanho > larguraTela - margem) return false
        if (y + tamanho > alturaTela - margem) return false
        val cruzaOVideo = x < videoDireita &&
            x + tamanho > videoEsquerda &&
            y < videoBase &&
            y + tamanho > videoTopo
        return !cruzaOVideo
    }
}

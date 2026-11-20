package com.chromebrothers.navegador.regras

object ToqueDuploNoPlayerDeVideo {
    fun segundos(x: Float, largura: Float, incremento: Int): Int {
        if (largura <= 0f || incremento == 0) return 0
        return if (x < largura / 2f) -incremento else incremento
    }
}

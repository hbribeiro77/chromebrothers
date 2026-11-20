package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoToqueDuploNoPlayerDeVideo {
    @Test
    fun oLadoEsquerdoRetrocedeEODireitoAvancaOIncrementoConfigurado() {
        assertEquals(-10, ToqueDuploNoPlayerDeVideo.segundos(100f, 1000f, 10))
        assertEquals(10, ToqueDuploNoPlayerDeVideo.segundos(800f, 1000f, 10))
        assertEquals(5, ToqueDuploNoPlayerDeVideo.segundos(500f, 1000f, 5))
    }

    @Test
    fun semLarguraNaoDesloca() {
        assertEquals(0, ToqueDuploNoPlayerDeVideo.segundos(10f, 0f, 10))
    }
}

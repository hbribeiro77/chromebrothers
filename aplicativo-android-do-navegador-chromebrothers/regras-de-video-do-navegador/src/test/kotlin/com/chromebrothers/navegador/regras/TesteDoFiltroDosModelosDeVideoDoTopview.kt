package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoFiltroDosModelosDeVideoDoTopview {
    private val f = FiltroDosModelosDeVideoDoTopviewNoComponente

    @Test
    fun mantemGeracaoNormalEReferencia() {
        assertTrue(f.serve("Kling 4.0", listOf("startEndFrame")))
        assertTrue(f.serve("Wan 3.0", listOf("startEndFrame")))
        assertTrue(f.serve("Kling O3 Reference-to-Video", listOf("multiImage")))
        assertTrue(f.serve("Veo 3.1", emptyList()))
    }

    @Test
    fun tiraEditarEstenderEMovimento() {
        assertFalse(f.serve("Wan 2.7 Video Extend", emptyList()))
        assertFalse(f.serve("Kling O3 Video-Edit", emptyList()))
        assertFalse(f.serve("Kling Motion Control Pro V3", emptyList()))
        assertFalse(f.serve("Modelo Novo", listOf("videoEdit")))
        assertFalse(f.serve("Outro", listOf("motionControl")))
    }
}

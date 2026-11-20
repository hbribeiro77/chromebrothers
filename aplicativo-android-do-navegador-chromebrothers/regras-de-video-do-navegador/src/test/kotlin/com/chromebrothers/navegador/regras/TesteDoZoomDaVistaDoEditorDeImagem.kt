package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoZoomDaVistaDoEditorDeImagem {

    @Test
    fun limitar_prende_abaixo_do_minimo_e_acima_do_maximo() {
        assertEquals(0.25f, ZoomDaVistaDoEditorDeImagem.limitar(0.1f))
        assertEquals(8f, ZoomDaVistaDoEditorDeImagem.limitar(12f))
        assertEquals(1f, ZoomDaVistaDoEditorDeImagem.limitar(1f))
        assertEquals(0.5f, ZoomDaVistaDoEditorDeImagem.limitar(0.5f))
    }

    @Test
    fun percentual_e_rotulo_arredondam_para_inteiro() {
        assertEquals(100, ZoomDaVistaDoEditorDeImagem.percentualInteiro(1f))
        assertEquals(25, ZoomDaVistaDoEditorDeImagem.percentualInteiro(0.25f))
        assertEquals(67, ZoomDaVistaDoEditorDeImagem.percentualInteiro(0.666f))
        assertEquals("67%", ZoomDaVistaDoEditorDeImagem.rotuloPercentual(0.666f))
        assertEquals("100%", ZoomDaVistaDoEditorDeImagem.rotuloPercentual(ZoomDaVistaDoEditorDeImagem.ENCAIXE))
    }

    @Test
    fun encaixe_e_limites_publicos() {
        assertEquals(0.25f, ZoomDaVistaDoEditorDeImagem.MINIMO)
        assertEquals(8f, ZoomDaVistaDoEditorDeImagem.MAXIMO)
        assertEquals(1f, ZoomDaVistaDoEditorDeImagem.ENCAIXE)
    }
}

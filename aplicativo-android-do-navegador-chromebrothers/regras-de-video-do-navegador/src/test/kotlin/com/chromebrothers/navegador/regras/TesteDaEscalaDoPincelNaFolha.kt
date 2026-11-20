package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDaEscalaDoPincelNaFolha {
    @Test
    fun `referencia nao muda com a rotacao da tela`() {
        val retrato = EscalaDoPincelNaFolha.escalaDeReferencia(1600, 2560, 4000, 3000)
        val paisagem = EscalaDoPincelNaFolha.escalaDeReferencia(2560, 1600, 4000, 3000)
        assertEquals(retrato, paisagem, 0.0001f)
    }

    @Test
    fun `referencia encaixa a folha na tela pelo lado mais apertado`() {
        // 2560/4000 = 0,64; 1600/3000 = 0,533 → 0,533
        assertEquals(1600f / 3000f, EscalaDoPincelNaFolha.escalaDeReferencia(2560, 1600, 4000, 3000), 0.0001f)
    }

    @Test
    fun `diametro na folha nao depende do zoom da vista`() {
        val referencia = EscalaDoPincelNaFolha.escalaDeReferencia(2560, 1600, 4000, 3000)
        val naFolha = EscalaDoPincelNaFolha.diametroNaFolha(40f, referencia)
        assertEquals(75f, naFolha, 0.01f)
        // Na tela, com zoom 4× sobre a referência, aparece 4× maior.
        assertEquals(160f, EscalaDoPincelNaFolha.diametroNaTela(40f, referencia, referencia * 4f), 0.01f)
    }

    @Test
    fun `dimensoes invalidas caem na escala 1`() {
        assertEquals(1f, EscalaDoPincelNaFolha.escalaDeReferencia(0, 1600, 4000, 3000), 0f)
        assertEquals(1f, EscalaDoPincelNaFolha.escalaDeReferencia(2560, 1600, 0, 3000), 0f)
    }
}

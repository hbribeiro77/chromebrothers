package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaReguaSobreAFolhaDoEditorDeImagem {
    @Test
    fun giroDevagarEncaixaEmQuarentaECincoEGiroRapidoPassaReto() {
        assertEquals(0f, GestoDaReguaSobreAFolha.angulo(3f, 2f), 0.01f)
        assertEquals(45f, GestoDaReguaSobreAFolha.angulo(40f, 4f), 0.01f)
        assertEquals(3f, GestoDaReguaSobreAFolha.angulo(3f, 30f), 0.01f)
        assertEquals(0f, GestoDaReguaSobreAFolha.angulo(-3f, 1f), 0.01f)
    }

    @Test
    fun tracoLigadoSegueAOrientacaoSemColarNaRegua() {
        val horizontal = GestoDaReguaSobreAFolha.pontoDoTraco(
            armada = true,
            borracha = false,
            x = 80f,
            y = 90f,
            ancoraX = 10f,
            ancoraY = 20f,
            anguloGraus = 0f,
        )
        assertEquals(80f, horizontal.x, 0.01f)
        assertEquals(20f, horizontal.y, 0.01f)
        val vertical = GestoDaReguaSobreAFolha.pontoDoTraco(
            armada = true,
            borracha = false,
            x = 80f,
            y = 90f,
            ancoraX = 10f,
            ancoraY = 20f,
            anguloGraus = 90f,
        )
        assertEquals(10f, vertical.x, 0.01f)
        assertEquals(90f, vertical.y, 0.01f)
        val solta = GestoDaReguaSobreAFolha.pontoDoTraco(
            armada = false,
            borracha = false,
            x = 80f,
            y = 90f,
            ancoraX = 10f,
            ancoraY = 20f,
            anguloGraus = 0f,
        )
        assertEquals(80f, solta.x, 0.01f)
        assertEquals(90f, solta.y, 0.01f)
        val borracha = GestoDaReguaSobreAFolha.pontoDoTraco(
            armada = true,
            borracha = true,
            x = 80f,
            y = 90f,
            ancoraX = 10f,
            ancoraY = 20f,
            anguloGraus = 0f,
        )
        assertEquals(80f, borracha.x, 0.01f)
        assertEquals(90f, borracha.y, 0.01f)
    }

    @Test
    fun dedoAcertaOCorpoOGiroEOInterruptor() {
        val regua = reguaDeitada()
        assertEquals(ParteDaReguaSobreAFolha.CORPO, GestoDaReguaSobreAFolha.parte(regua, 100f, 100f))
        assertEquals(ParteDaReguaSobreAFolha.GIRO, GestoDaReguaSobreAFolha.parte(regua, 214f, 100f))
        assertEquals(ParteDaReguaSobreAFolha.INTERRUPTOR, GestoDaReguaSobreAFolha.parte(regua, -14f, 100f))
        assertEquals(ParteDaReguaSobreAFolha.NENHUMA, GestoDaReguaSobreAFolha.parte(regua, 100f, 40f))
    }

    @Test
    fun moverDeslocaEAlternarSoMudaOInterruptor() {
        val regua = reguaDeitada()
        val movida = GestoDaReguaSobreAFolha.mover(regua, -150f, -80f)
        assertEquals(-50f, movida.centroX, 0.01f)
        assertEquals(20f, movida.centroY, 0.01f)
        assertFalse(movida.armada)
        val armada = GestoDaReguaSobreAFolha.alternar(movida)
        assertTrue(armada.armada)
        assertEquals(-50f, armada.centroX, 0.01f)
    }

    private fun reguaDeitada(): ReguaSobreAFolha {
        return ReguaSobreAFolha(
            centroX = 100f,
            centroY = 100f,
            anguloGraus = 0f,
            comprimento = 200f,
            espessura = 20f,
            raioDoCabo = 14f,
            armada = false,
        )
    }
}

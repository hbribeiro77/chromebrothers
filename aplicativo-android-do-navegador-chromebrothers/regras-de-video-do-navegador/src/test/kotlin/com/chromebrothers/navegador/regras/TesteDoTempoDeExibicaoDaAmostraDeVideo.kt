package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TesteDoTempoDeExibicaoDaAmostraDeVideo {
    @Test
    fun quadroIntermediarioMantemOTempoMesmoVoltando() {
        assertEquals(
            listOf(0L, 100_000L, 33_333L, 66_666L),
            TempoDeExibicaoDaAmostraParaOArquivo.sequencia(listOf(0L, 100_000L, 33_333L, 66_666L)),
        )
    }

    @Test
    fun tempoNegativoViraZero() {
        assertEquals(
            listOf(0L, 10L),
            TempoDeExibicaoDaAmostraParaOArquivo.sequencia(listOf(-5L, 10L)),
        )
    }

    @Test
    fun configuracaoAvcViraSpsEPpsComCodigoDeInicio() {
        val configuracao = byteArrayOf(
            1, 0x64, 0, 0x1f, 0xFF.toByte(),
            0xE1.toByte(), 0, 3, 0x67, 0x42, 0,
            1, 0, 2, 0x68, 0xCE.toByte(),
        )
        val inicial = PreparacaoDaAmostraDeVideoAvcParaOMuxer.inicializacao(configuracao, null)
        assertNotNull(inicial)
        assertEquals(listOf(0, 0, 0, 1, 0x67, 0x42, 0), inicial[0].semSinal())
        assertEquals(listOf(0, 0, 0, 1, 0x68, 0xCE), inicial[1].semSinal())
        assertEquals(4, PreparacaoDaAmostraDeVideoAvcParaOMuxer.tamanhoDoComprimento(configuracao))
    }

    @Test
    fun amostraComTamanhoViraCodigoDeInicio() {
        val amostra = byteArrayOf(0, 0, 0, 3, 0x65, 0x88.toByte(), 0x84.toByte())
        val saida = PreparacaoDaAmostraDeVideoAvcParaOMuxer.amostra(amostra, 4)
        assertEquals(listOf(0, 0, 0, 1, 0x65, 0x88, 0x84), saida.semSinal())
    }

    @Test
    fun amostraQueJaTemCodigoDeInicioPermanece() {
        val amostra = byteArrayOf(0, 0, 0, 1, 0x65, 1)
        assertEquals(
            amostra.semSinal(),
            PreparacaoDaAmostraDeVideoAvcParaOMuxer.amostra(amostra, 4).semSinal(),
        )
    }

    private fun ByteArray.semSinal(): List<Int> = map { it.toInt() and 0xFF }
}

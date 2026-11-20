package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDoTamanhoDaExportacaoDaComposicao {
    private val t = TamanhoDaExportacaoDaComposicao

    @Test
    fun molduraPequenaComImagemNitidaOfereceAte4096() {
        val opcoes = t.opcoes(400, 400, densidadeMaxima = 6.6f)
        assertEquals(
            listOf(
                TamanhoDaExportacaoDaComposicao.Opcao(TamanhoDaExportacaoDaComposicao.Tipo.MOLDURA, 400, 400),
                TamanhoDaExportacaoDaComposicao.Opcao(TamanhoDaExportacaoDaComposicao.Tipo.LADO_1080, 1080, 1080),
                TamanhoDaExportacaoDaComposicao.Opcao(TamanhoDaExportacaoDaComposicao.Tipo.LADO_2048, 2048, 2048),
            ),
            opcoes.take(3),
        )
        val maximo = opcoes.last()
        assertEquals(TamanhoDaExportacaoDaComposicao.Tipo.MAXIMO, maximo.tipo)
        assertTrue(maximo.largura <= 4000 && maximo.largura >= 2640)
    }

    @Test
    fun mantemProporcaoDaMolduraERemoveRepetidos() {
        val opcoes = t.opcoes(1080, 1350, densidadeMaxima = 1f)
        assertEquals(1080, opcoes.first().largura)
        assertEquals(1350, opcoes.first().altura)
        val de1080 = opcoes.first { it.tipo == TamanhoDaExportacaoDaComposicao.Tipo.LADO_1080 }
        assertEquals(864, de1080.largura)
        assertEquals(1080, de1080.altura)
        // densidade 1 = mesmo tamanho da moldura: o "máximo" some como repetido.
        assertTrue(opcoes.none { it.tipo == TamanhoDaExportacaoDaComposicao.Tipo.MAXIMO })
    }

    @Test
    fun mimePelaExtensao() {
        assertEquals("image/png", t.mimePeloNome("quadro-1.png"))
        assertEquals("image/jpeg", t.mimePeloNome("quadro-1.jpg"))
    }
}

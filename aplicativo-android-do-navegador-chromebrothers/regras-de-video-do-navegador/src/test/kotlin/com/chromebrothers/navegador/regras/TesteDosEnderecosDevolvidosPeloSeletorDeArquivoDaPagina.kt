package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDosEnderecosDevolvidosPeloSeletorDeArquivoDaPagina {
    @Test
    fun juntaOEnderecoPrincipalOsClipesEOsExtrasSemRepetir() {
        assertEquals(
            listOf("content://pasta/arquivo", "content://pasta/outro"),
            EnderecosDevolvidosPeloSeletorDeArquivoDaPagina.escolher(
                principal = "content://pasta/arquivo",
                clipes = listOf("content://pasta/arquivo", "content://pasta/outro"),
                extras = listOf("  ", null),
            ),
        )
    }

    @Test
    fun usaOClipeQuandoAPastaNaoDevolveOEnderecoPrincipal() {
        assertEquals(
            listOf("content://samsung/selecionado"),
            EnderecosDevolvidosPeloSeletorDeArquivoDaPagina.escolher(
                principal = null,
                clipes = listOf(null, "content://samsung/selecionado"),
                extras = listOf("content://samsung/selecionado"),
            ),
        )
    }

    @Test
    fun resultadoVazioNaoInventaEndereco() {
        assertEquals(
            emptyList(),
            EnderecosDevolvidosPeloSeletorDeArquivoDaPagina.escolher(null, emptyList(), listOf("")),
        )
    }
}

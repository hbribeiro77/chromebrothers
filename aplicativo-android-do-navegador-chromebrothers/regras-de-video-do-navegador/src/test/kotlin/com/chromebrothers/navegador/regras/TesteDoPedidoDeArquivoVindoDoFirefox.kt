package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoPedidoDeArquivoVindoDoFirefox {
    @Test
    fun pastaQuandoOTipoEDePasta() {
        assertTrue(TraducaoDoPedidoDeArquivoDoFirefox.escolhePasta(TraducaoDoPedidoDeArquivoDoFirefox.TIPO_PASTA))
        assertFalse(TraducaoDoPedidoDeArquivoDoFirefox.escolhePasta(TraducaoDoPedidoDeArquivoDoFirefox.TIPO_UM_ARQUIVO))
    }

    @Test
    fun tiposVaziosQuandoOFirefoxNaoMandouMime() {
        assertEquals(emptyList(), TraducaoDoPedidoDeArquivoDoFirefox.tipos(null))
    }

    @Test
    fun variosArquivosQuandoOTipoEMultiplo() {
        assertTrue(
            TraducaoDoPedidoDeArquivoDoFirefox.variosArquivos(
                TraducaoDoPedidoDeArquivoDoFirefox.TIPO_VARIOS_ARQUIVOS,
            ),
        )
    }

    @Test
    fun oSeletorIgnoraTipoQueNaoEMime() {
        assertEquals(
            listOf("image/*"),
            TraducaoDoPedidoDeArquivoDoFirefox.tiposQueOSeletorEntende(
                listOf("*/*", "image/*", "capture=camera", ""),
            ),
        )
    }
}

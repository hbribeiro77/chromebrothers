package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoDownloadPedidoPelaPagina {
    @Test
    fun enderecoHttpVaiParaARedeEBlobOuDataFicamNaMemoria() {
        assertEquals(
            OrigemDoDownloadPedidoPelaPagina.REDE,
            PedidoDeDownloadDaPagina.origem("https://exemplo/arquivo.pdf"),
        )
        assertEquals(
            OrigemDoDownloadPedidoPelaPagina.MEMORIA,
            PedidoDeDownloadDaPagina.origem("blob:https://exemplo/abc"),
        )
        assertEquals(
            OrigemDoDownloadPedidoPelaPagina.MEMORIA,
            PedidoDeDownloadDaPagina.origem("data:text/plain,ola"),
        )
        assertEquals(
            OrigemDoDownloadPedidoPelaPagina.IGNORAR,
            PedidoDeDownloadDaPagina.origem("javascript:void(0)"),
        )
    }

    @Test
    fun oNomePreferidoVemDoCabecalhoENaoDoCaminho() {
        assertEquals(
            "relatorio.pdf",
            PedidoDeDownloadDaPagina.nome(
                "attachment; filename=\"outro.txt\"; filename*=UTF-8''relatorio.pdf",
                "https://exemplo/caminho/ignorado.bin",
                "application/octet-stream",
            ),
        )
        assertEquals(
            "b.pdf",
            PedidoDeDownloadDaPagina.nome("attachment; filename=\"../b.pdf\"", "", ""),
        )
    }

    @Test
    fun semCabecalhoONomeSaiDoEnderecoEGanhaExtensaoDoTipo() {
        assertEquals(
            "baixar.csv",
            PedidoDeDownloadDaPagina.nome("", "https://exemplo/baixar?id=1", "text/csv"),
        )
        assertEquals(
            "arquivo.pdf",
            PedidoDeDownloadDaPagina.nome("", "blob:https://exemplo/abc", "application/pdf"),
        )
    }

    @Test
    fun dataUrlEmBase64ViraOsBytes() {
        val bytes = ConteudoCodificadoNoEnderecoDaPagina.ler("data:text/plain;base64,YQ==")
        assertEquals("a", bytes?.decodeToString())
        assertNull(ConteudoCodificadoNoEnderecoDaPagina.ler("https://exemplo/a"))
        assertTrue(ConteudoCodificadoNoEnderecoDaPagina.ler("data:text/plain,ola")!!.decodeToString() == "ola")
    }
}

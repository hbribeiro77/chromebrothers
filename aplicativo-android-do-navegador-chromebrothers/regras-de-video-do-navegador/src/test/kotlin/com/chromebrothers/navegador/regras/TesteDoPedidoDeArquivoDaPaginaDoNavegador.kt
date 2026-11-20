package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoPedidoDeArquivoDaPaginaDoNavegador {
    @Test
    fun paginaSemTipoAceitaFotoEVideo() {
        assertEquals(
            listOf(CameraDoPedidoDeArquivoDaPagina.FOTO, CameraDoPedidoDeArquivoDaPagina.VIDEO),
            PedidoDeArquivoDaPaginaDoNavegador.cameras(emptyList(), escolhePasta = false),
        )
        assertEquals(
            listOf(CameraDoPedidoDeArquivoDaPagina.FOTO, CameraDoPedidoDeArquivoDaPagina.VIDEO),
            PedidoDeArquivoDaPaginaDoNavegador.cameras(listOf("", "*/*"), escolhePasta = false),
        )
    }

    @Test
    fun fotoEVideoSeguemOTipoAceito() {
        assertEquals(
            listOf(CameraDoPedidoDeArquivoDaPagina.FOTO),
            PedidoDeArquivoDaPaginaDoNavegador.cameras(listOf("image/*"), escolhePasta = false),
        )
        assertEquals(
            listOf(CameraDoPedidoDeArquivoDaPagina.FOTO),
            PedidoDeArquivoDaPaginaDoNavegador.cameras(listOf(".png"), escolhePasta = false),
        )
        assertEquals(
            listOf(CameraDoPedidoDeArquivoDaPagina.VIDEO),
            PedidoDeArquivoDaPaginaDoNavegador.cameras(listOf("video/mp4"), escolhePasta = false),
        )
        assertEquals(
            listOf(CameraDoPedidoDeArquivoDaPagina.FOTO, CameraDoPedidoDeArquivoDaPagina.VIDEO),
            PedidoDeArquivoDaPaginaDoNavegador.cameras(listOf("image/*", "video/*"), escolhePasta = false),
        )
    }

    @Test
    fun documentoNaoAbreCameraEPastaTambemNao() {
        assertEquals(
            emptyList(),
            PedidoDeArquivoDaPaginaDoNavegador.cameras(listOf("application/pdf"), escolhePasta = false),
        )
        assertEquals(
            emptyList(),
            PedidoDeArquivoDaPaginaDoNavegador.cameras(listOf("image/*"), escolhePasta = true),
        )
    }
}

package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaMontagemDoPedidoDeVideoTopviewComReferenciasOmni {

    @Test
    fun semReferenciasUsaTextToVideo() {
        val pedido = MontagemDoPedidoDeVideoTopviewComReferenciasOmni.montar(
            modeloDisplay = "Wan 3.0",
            prompt = "cidade neon",
            aspectRatio = "16:9",
            resolution = 720,
            duration = 5,
            fileIdsDasReferencias = emptyList(),
        )
        assertEquals("text_to_video", pedido.taskType)
        assertEquals("Wan 3.0", pedido.model)
        assertEquals("cidade neon", pedido.prompt)
        assertEquals("16:9", pedido.aspectRatio)
        assertNull(pedido.firstFrameFileId)
        assertTrue(pedido.inputImages.isEmpty())
        assertTrue(pedido.referenceImageFileIds.isEmpty())
    }

    @Test
    fun comReferenciasUsaOmniReferenceEInputImagesSemFirstFrame() {
        val pedido = MontagemDoPedidoDeVideoTopviewComReferenciasOmni.montar(
            modeloDisplay = "Wan 3.0",
            prompt = "o personagem corre",
            aspectRatio = "16:9",
            resolution = 720,
            duration = 5,
            fileIdsDasReferencias = listOf("file_a", "file_b"),
        )
        assertEquals("omni_reference", pedido.taskType)
        assertNull(pedido.firstFrameFileId)
        assertTrue(pedido.referenceImageFileIds.isEmpty())
        assertEquals(
            listOf(
                MontagemDoPedidoDeVideoTopviewComReferenciasOmni.ImagemDeEntrada("file_a", "Image1"),
                MontagemDoPedidoDeVideoTopviewComReferenciasOmni.ImagemDeEntrada("file_b", "Image2"),
            ),
            pedido.inputImages,
        )
        assertTrue(pedido.prompt!!.contains("<<<Image1>>>"))
        assertTrue(pedido.prompt!!.contains("<<<Image2>>>"))
        assertTrue(pedido.prompt!!.startsWith("o personagem corre"))
    }

    @Test
    fun umaUnicaReferenciaTambemVaiComoOmniNaoComoFirstFrame() {
        val pedido = MontagemDoPedidoDeVideoTopviewComReferenciasOmni.montar(
            modeloDisplay = "Wan 3.0",
            prompt = "aproxima",
            aspectRatio = "9:16",
            resolution = 480,
            duration = 4,
            fileIdsDasReferencias = listOf("file_unica"),
        )
        assertEquals("omni_reference", pedido.taskType)
        assertNull(pedido.firstFrameFileId)
        assertEquals(
            listOf(MontagemDoPedidoDeVideoTopviewComReferenciasOmni.ImagemDeEntrada("file_unica", "Image1")),
            pedido.inputImages,
        )
    }

    @Test
    fun naoDuplicaMarcadoresSeOPromptJaCitaAsImagens() {
        val pedido = MontagemDoPedidoDeVideoTopviewComReferenciasOmni.montar(
            modeloDisplay = "Wan 3.0",
            prompt = "Use <<<Image1>>> como estilo",
            aspectRatio = "16:9",
            resolution = 720,
            duration = 5,
            fileIdsDasReferencias = listOf("file_a"),
        )
        assertEquals("Use <<<Image1>>> como estilo", pedido.prompt)
    }

    @Test
    fun omitAspectRatioAdaptiveNoTextToVideo() {
        val pedido = MontagemDoPedidoDeVideoTopviewComReferenciasOmni.montar(
            modeloDisplay = "Wan 3.0",
            prompt = "x",
            aspectRatio = "adaptive",
            resolution = 480,
            duration = 5,
            fileIdsDasReferencias = emptyList(),
        )
        assertNull(pedido.aspectRatio)
    }
}

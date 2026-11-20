package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoVideoComoReferenciaNaGeracaoMcp {
    private val regra = ReferenciaDeVideoNaGeracaoMcp
    private val H = "higgsfield"
    private val S = "sjinn"
    private val T = "topview"

    @Test
    fun modelosPedidosAceitamVideoEmCadaMcp() {
        listOf("wan3_0", "wan3_0_prime", "seedance_2_0", "seedance_2_0_mini", "seedance_2_5", "seedance_2_5_flash", "kling4_0", "kling_4_0_flash")
            .forEach { assertTrue(regra.aceita(H, it), it) }
        listOf("wan3", "seedance2.5", "seedance2.0", "seedance2.0-fast", "kling4").forEach { assertTrue(regra.aceita(S, it), it) }
        listOf("wan-3-0", "wan-3-0-prime", "seedance-2-0", "seedance-2-0-fast", "seedance-2-5", "kling-4-0", "Wan 3.0 Prime", "Kling 4.0")
            .forEach { assertTrue(regra.aceita(T, it), it) }
    }

    @Test
    fun outrosModelosNaoAceitam() {
        assertTrue(!regra.aceita(H, "kling3_0"))
        assertTrue(!regra.aceita(S, "kling3"))
        assertTrue(!regra.aceita(T, "wan-2-7"))
        assertTrue(!regra.aceita(S, "veo3.1-fast"))
    }

    @Test
    fun familiaCanonicaComVariosMcps() {
        assertTrue(regra.aceitaEmTodos(listOf(H, S, T), "wan3"))
        assertTrue(regra.aceitaEmTodos(listOf(H, T), "wan3-prime"))
        assertTrue(regra.aceitaEmTodos(listOf(H, S, T), "kling-4"))
        assertTrue(!regra.aceitaEmTodos(listOf(H, S, T), "kling-v3"))
        assertTrue(!regra.aceitaEmTodos(emptyList(), "wan3"))
    }

    @Test
    fun validaQuantidadeEDuracao() {
        assertNull(regra.validar(listOf(5_000, 10_000), listOf(S), "wan3"))
        assertNotNull(regra.validar(listOf(10_000, 6_000), listOf(S), "wan3"))
        assertNotNull(regra.validar(listOf(1_000), listOf(H), "seedance_2_0"))
        assertNotNull(regra.validar(listOf(2_000, 2_000, 2_000, 2_000), listOf(H), "wan3_0"))
        assertNull(regra.validar(listOf(12_000, 15_000), listOf(S), "kling4"))
        assertNotNull(regra.validar(listOf(2_000), listOf(S), "kling4"))
        assertTrue(regra.validar(listOf(5_000), listOf(S), "kling3")!!.contains("não aceita"))
        assertNull(regra.validar(emptyList(), listOf(S), "kling3"))
    }

    @Test
    fun modoDoHiggsfieldComVideo() {
        assertEquals(mapOf("mode" to "omni_reference"), regra.parametrosExtrasDoHiggsfield("seedance_2_5_flash", true))
        assertEquals(mapOf("mode" to "reference-to-video"), regra.parametrosExtrasDoHiggsfield("kling4_0", true))
        assertTrue(regra.parametrosExtrasDoHiggsfield("wan3_0", true).isEmpty())
        assertTrue(regra.parametrosExtrasDoHiggsfield("kling4_0", false).isEmpty())
    }

    @Test
    fun sjinnMontaCamposDeCadaFerramenta() {
        val wan = MontagemDoPedidoDeVideoDoSjinn.montar("wan3-video-api", "wan3", "p", "16:9", "720p", 5, listOf("i"), listOf("v"))
        assertEquals(listOf("v"), wan["video_urls"])
        assertEquals("720P", wan["resolution"])
        val s20 = MontagemDoPedidoDeVideoDoSjinn.montar("seedance20-video-api", "seedance2.0-fast", "p", "adaptive", "480p", 30, listOf("i"), listOf("v"))
        assertEquals(listOf("i", "v"), s20["media_urls"])
        assertEquals("fast", s20["mode"])
        assertEquals(15, s20["duration"])
        assertEquals("16:9", s20["aspect_ratio"])
        val s25 = MontagemDoPedidoDeVideoDoSjinn.montar("seedance25-video-api", "seedance2.5", "p", "9:16", "480p", 5, emptyList(), listOf("v"))
        assertEquals("9:16", s25["ratio"])
        assertEquals("480p", s25["resolution"])
        val k4 = MontagemDoPedidoDeVideoDoSjinn.montar("kling4-omni-video-api", "kling4", "p", "adaptive", "480p", 2, emptyList(), listOf("v"))
        assertEquals("720p", k4["resolution"])
        assertEquals("auto", k4["aspect_ratio"])
        assertEquals(3, k4["duration"])
        assertTrue("mode" !in k4)
    }

    @Test
    fun topviewUsaOmniComVideos() {
        val p = MontagemDoPedidoDeVideoTopviewComReferenciasOmni.montar("Wan 3.0", "dance", "16:9", 480, 5, listOf("img"), listOf("vid"))
        assertEquals("omni_reference", p.taskType)
        assertEquals("Video1", p.inputVideos.single().name)
        assertEquals("dance\n<<<Image1>>> <<<Video1>>>", p.prompt)
        val soVideo = MontagemDoPedidoDeVideoTopviewComReferenciasOmni.montar("Kling 4.0", "", "16:9", 720, 5, emptyList(), listOf("a", "b"))
        assertEquals("omni_reference", soVideo.taskType)
        assertEquals("<<<Video1>>> <<<Video2>>>", soVideo.prompt)
    }

    @Test
    fun arrasteDeVideoSoViraVideoNoGeradorDeVideoComModeloQueAceita() {
        val d = DestinoDoArrasteDaBibliotecaParaReferenciaMcp
        assertEquals(DestinoDoArrasteDaBibliotecaParaReferenciaMcp.Acao.USAR_VIDEO, d.decidir("video", "", "video", modeloAceitaVideo = true))
        assertEquals(DestinoDoArrasteDaBibliotecaParaReferenciaMcp.Acao.USAR_FRAME, d.decidir("video", "", "video", modeloAceitaVideo = false))
        assertEquals(DestinoDoArrasteDaBibliotecaParaReferenciaMcp.Acao.USAR_FRAME, d.decidir("video", "", "imagem", modeloAceitaVideo = true))
    }

    @Test
    fun catalogoDoSjinnTemKling4ESeedance20() {
        assertEquals("kling4-omni-video-api", CatalogoDeModelosDeVideoDoSjinnNaColecao.toolTypeDe("kling4"))
        assertEquals("seedance20-video-api", CatalogoDeModelosDeVideoDoSjinnNaColecao.toolTypeDe("seedance2.0-mini"))
        assertEquals("kling-4", EquivalenciaDeModelosDeVideoEntreMcps.familiaDe("kling4_0")?.id)
        assertEquals("kling-v3", EquivalenciaDeModelosDeVideoEntreMcps.familiaDe("kling3_0")?.id)
    }
}

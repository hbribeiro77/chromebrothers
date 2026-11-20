package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoMagnificNaGeracaoMcp {
    // Trecho real (encurtado) de video_models_list.
    private val listaDeVideo = """models[3]:
  - slug: kling-40-omni
    name: Kling 4.0 Omni
    tool: video-generator
    aspectRatios[3]: "16:9","1:1","9:16"
    durations[3]: 3,5,10
    resolutions[2]: 720p,1080p
    references[2]:
      - type: image
        allowed: true
        limit: 7
      - type: video
        assetType: video
        allowed: true
        limit: 1
  - slug: kling-40
    name: Kling 4.0
    durations[2]: 5,10
    resolutions[2]: 720p,1080p
    references[1]:
      - type: video
        allowed: false
  - slug: kling-motion-control
    name: Kling 2.6 Motion Control
    durations[2]: 3,10
"""

    @Test
    fun leModelosDeVideoComDetalhes() {
        val modelos = CatalogoDeModelosDoMagnific.ler(listaDeVideo)
        assertEquals(listOf("kling-40-omni", "kling-40", "kling-motion-control"), modelos.map { it.slug })
        val omni = modelos.first()
        assertEquals("Kling 4.0 Omni", omni.nome)
        assertEquals(listOf(3, 5, 10), omni.duracoes)
        assertEquals(listOf("720p", "1080p"), omni.resolucoes)
        assertEquals(listOf("16:9", "1:1", "9:16"), omni.proporcoes)
        assertTrue(omni.aceitaVideoDeReferencia)
        assertTrue(omni.aceitaImagemDeReferencia)
        assertFalse(modelos[1].aceitaVideoDeReferencia)
        assertEquals(listOf("kling-40-omni", "kling-40"), modelos.filter(CatalogoDeModelosDoMagnific::serveParaVideo).map { it.slug })
        val json = CatalogoDeModelosDoMagnific.jsonParaPonte(modelos.take(1))
        assertTrue(json.contains("\"duracoes\":[3,5,10]"))
        assertTrue(json.contains("\"aceitaVideo\":true"))
    }

    @Test
    fun ajustaDuracaoResolucaoEProporcao() {
        val a = AjusteDoPedidoAoModeloDoMagnific
        assertEquals(5, a.duracao(5, listOf(3, 5, 10)))
        assertEquals(10, a.duracao(8, listOf(3, 5, 10)))
        assertEquals(6, a.duracao(5, listOf(4, 6, 8))) // empate vai para a maior
        assertEquals(7, a.duracao(7, emptyList()))
        assertEquals("720p", a.resolucao("480p", listOf("720p", "1080p")))
        assertEquals("480p", a.resolucao("480p", listOf("1080p", "720p", "480p", "Draft", "4K")))
        assertEquals("2k", a.resolucao("1k", listOf("2k", "4k")))
        assertNull(a.resolucao("1k", emptyList()))
        assertNull(a.proporcao("adaptive", listOf("16:9")))
        assertNull(a.proporcao("4:3", listOf("16:9", "9:16")))
        assertEquals("16:9", a.proporcao("16:9", listOf("16:9")))
    }

    @Test
    fun extraiIdentificadoresEEstado() {
        val ids = ExtracaoDasCriacoesDoMagnific.identificadores(
            """{"content":[{"type":"text","text":"{\"identifiers\":[\"AbC123\",\"XyZ789\"],\"references\":[{\"identifier\":\"REF0001\"}]}"}]}""",
            ignorar = setOf("REF0001"),
        )
        assertEquals(listOf("AbC123", "XyZ789"), ids)
        // Formato real do creation_status.
        val pronto = ExtracaoDasCriacoesDoMagnific.estado(
            """{"structuredContent":{"creationIdentifier":"VXUeSsnMMU","status":"completed","tool":"upload","results":{"url":"https:\/\/pikaso.cdnpk.net\/a.png?token=x"}}}""",
        )
        assertIs<ExtracaoDasCriacoesDoMagnific.Estado.Pronto>(pronto)
        assertEquals("https://pikaso.cdnpk.net/a.png?token=x", pronto.url)
        assertIs<ExtracaoDasCriacoesDoMagnific.Estado.Andando>(
            ExtracaoDasCriacoesDoMagnific.estado("""{"structuredContent":{"status":"processing"}}"""),
        )
        assertIs<ExtracaoDasCriacoesDoMagnific.Estado.Falhou>(
            ExtracaoDasCriacoesDoMagnific.estado("""{"structuredContent":{"status":"failed","error":"sem crédito"}}"""),
        )
    }

    @Test
    fun magnificEntraNosGruposComuns() {
        assertEquals("imagen-nano-banana-2", EquivalenciaDeModelosDeImagemEntreMcps.idParaMcp("nano-banana-pro", "magnific"))
        assertEquals(
            listOf("nano-banana-2-1", "nano-banana-2", "nano-banana-pro"),
            EquivalenciaDeModelosDeImagemEntreMcps.intersecao(listOf("higgsfield", "sjinn", "magnific")).map { it.id },
        )
        assertEquals("kling-40-omni", EquivalenciaDeModelosDeVideoEntreMcps.idParaMcp("kling-4", "magnific"))
        assertTrue(ReferenciaDeVideoNaGeracaoMcp.aceita("magnific", "bytedance-seedance-mini-2.0"))
        assertTrue(ReferenciaDeVideoNaGeracaoMcp.aceita("magnific", "kling-4"))
        assertFalse(ReferenciaDeVideoNaGeracaoMcp.aceita("magnific", "google-veo3_1"))
        assertTrue(RespostaJsonDaPonteDeGeracaoMcp.okMcps().contains("\"id\":\"magnific\""))
        assertTrue(RespostaJsonDaPonteDeGeracaoDeVideoMcp.okMcps().contains("\"id\":\"magnific\""))
    }
}

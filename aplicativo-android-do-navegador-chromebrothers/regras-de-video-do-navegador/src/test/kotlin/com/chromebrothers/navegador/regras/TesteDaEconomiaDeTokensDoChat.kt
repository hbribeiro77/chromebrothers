package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaEconomiaDeTokensDoChat {
    @Test
    fun leOUsoDoUltimoPedacoDoFluxo() {
        val linha = """data: {"choices":[],"usage":{"prompt_tokens":12000,"completion_tokens":300,"total_tokens":12300,"cost":0.0021,"prompt_tokens_details":{"cached_tokens":9000}}}"""
        val leitura = TrechosDaRespostaEmFluxoDaOpenRouter.lerLinha(linha)!!
        val uso = leitura.uso!!
        assertEquals(12000, uso.tokensDeEntrada)
        assertEquals(9000, uso.tokensEmCache)
        assertEquals(300, uso.tokensDeSaida)
        assertEquals(0.0021, uso.custo)
        assertNull(TrechosDaRespostaEmFluxoDaOpenRouter.lerLinha("""data: {"choices":[{"delta":{"content":"oi"}}]}""")!!.uso)
    }

    @Test
    fun somaELinhaDoUso() {
        val total = UsoDoChat(10000, 8000, 200, 0.001) + UsoDoChat(14000, 9000, 100, 0.002)
        assertEquals(2, total.chamadas)
        assertEquals("2 chamadas · 24,3 mil tokens (17,0 mil em cache) · US$ 0,0030", total.linha())
        assertEquals("850 tokens", UsoDoChat(800, 0, 50).linha())
    }

    @Test
    fun oPedidoPedeOUso() {
        val corpo = PedidoDeChatParaAOpenRouter.corpo("m", listOf(MensagemDeChat("user", "oi")))
        assertTrue(corpo.contains("\"usage\":{\"include\":true}"))
    }

    @Test
    fun compactaResultadoLongoEImagemDeCapturaMasMantemOResto() {
        val anexo = ParteDeConteudoDoChat("imagem", mime = "image/png", nome = "a.png", dados = "AAAA")
        val historico = listOf(
            MensagemDeChat("user", "veja", partes = listOf(anexo)),
            MensagemDeChat("assistant", "", chamadas = listOf(ChamadaDeFerramenta("c1", "ler_texto_visivel", "{}"))),
            MensagemDeChat(papel = "tool", conteudo = "x".repeat(5000), idDaChamada = "c1"),
            MensagemDeChat("assistant", "", chamadas = listOf(ChamadaDeFerramenta("c2", "capturar_aba", "{}"))),
            MensagemDeChat(papel = "tool", conteudo = "ok", idDaChamada = "c2"),
            MensagemDeChat("user", "Imagem da aba", partes = listOf(anexo)),
            MensagemDeChat("assistant", "Pronto."),
        )
        val compacto = CompactacaoDoHistoricoDoChat.compactar(historico)
        assertEquals(historico.size, compacto.size)
        assertEquals(historico[0], compacto[0])
        assertTrue(compacto[2].conteudo.startsWith("[Resultado antigo de ler_texto_visivel: 5000 caracteres"))
        assertTrue(compacto[2].conteudo.length < 400)
        assertEquals("ok", compacto[4].conteudo)
        assertTrue(compacto[5].partes.isEmpty())
        assertEquals("Pronto.", compacto[6].conteudo)
        assertEquals(compacto, CompactacaoDoHistoricoDoChat.compactar(compacto))
    }

    @Test
    fun oPromptFixoNaoMudaComAAbaNemComAColecao() {
        val a = FerramentasDeLeituraDoChatDaOpenRouter.instrucaoFixa()
        assertFalse(a.contains("Aba: Sjinn"))
        val contexto = FerramentasDeLeituraDoChatDaOpenRouter.contextoDaRodada(
            "Aba: Sjinn", "nota", navegacaoLiberada = true, colecao = "- janela", vinculo = "",
        )
        assertTrue(contexto.contains("Aba: Sjinn"))
        assertTrue(contexto.contains("navegação está liberada"))
        assertFalse(ModoConversaDoChat.instrucaoFixa().contains("Aba: Sjinn"))
    }

    @Test
    fun catalogoDoModoChatECurto() {
        val catalogo = ModoConversaDoChat.catalogoDoAgente()
        assertTrue(catalogo.contains("- gerar_video_vivix: "))
        catalogo.lines().forEach { assertTrue(it.length <= 220, it) }
    }
}

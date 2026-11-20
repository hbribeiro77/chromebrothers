package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDasFerramentasEAnexosDoChatDaOpenRouter {
    @Test
    fun oPedidoDoAgenteIncluiSoAsFerramentasDeLeitura() {
        val corpo = PedidoDeChatParaAOpenRouter.corpo(
            "deepseek/deepseek-v4.1-flash-1017",
            listOf(MensagemDeChat("user", "o que é esta página?")),
            comFerramentas = true,
        )
        assertTrue(corpo.contains("\"tools\":["))
        FerramentasDeLeituraDoChatDaOpenRouter.permitidas.forEach { nome ->
            assertTrue(corpo.contains(nome))
        }
        assertFalse(corpo.contains("navegar"))
        assertTrue(corpo.contains("\"stream\":true"))
    }

    @Test
    fun aMensagemLevaImagemArquivoERespostaDeFerramenta() {
        val usuario = MensagemDeChat(
            papel = "user",
            conteudo = "veja",
            partes = listOf(
                ParteDeConteudoDoChat("imagem", mime = "image/jpeg", dados = "QUJD"),
                ParteDeConteudoDoChat("arquivo", mime = "application/pdf", nome = "nota.pdf", dados = "UERG"),
                ParteDeConteudoDoChat("texto", texto = "Arquivo nota.md:\nolá"),
            ),
        )
        val json = PedidoDeChatParaAOpenRouter.jsonDaMensagem(usuario)
        assertTrue(json.contains("\"type\":\"image_url\""))
        assertTrue(json.contains("data:image/jpeg;base64,QUJD"))
        assertTrue(json.contains("\"filename\":\"nota.pdf\""))
        assertTrue(json.contains("data:application/pdf;base64,UERG"))
        assertTrue(json.contains("Arquivo nota.md:\\nolá"))
        val assistente = MensagemDeChat(
            papel = "assistant",
            chamadas = listOf(ChamadaDeFerramenta("call_1", FerramentasDeLeituraDoChatDaOpenRouter.TEXTO, "{}")),
        )
        val chamada = PedidoDeChatParaAOpenRouter.jsonDaMensagem(assistente)
        assertTrue(chamada.contains("\"tool_calls\""))
        assertTrue(chamada.contains("\"content\":null"))
        val ferramenta = PedidoDeChatParaAOpenRouter.jsonDaMensagem(
            MensagemDeChat(papel = "tool", conteudo = "Página aberta", idDaChamada = "call_1"),
        )
        assertTrue(ferramenta.contains("\"tool_call_id\":\"call_1\""))
    }

    @Test
    fun oFluxoJuntaOsPedaçosDaFerramenta() {
        val primeiro = TrechosDaRespostaEmFluxoDaOpenRouter.lerDados(
            """{"choices":[{"delta":{"content":null,"tool_calls":[{"index":0,"id":"call_1","type":"function","function":{"name":"ler_texto_visivel","arguments":""}}]}}]}""",
        )
        val segundo = TrechosDaRespostaEmFluxoDaOpenRouter.lerDados(
            """{"choices":[{"delta":{"tool_calls":[{"index":0,"function":{"arguments":"{\"a\":"}}]}}]}""",
        )
        val terceiro = TrechosDaRespostaEmFluxoDaOpenRouter.lerDados(
            """{"choices":[{"delta":{"content":"Olá","tool_calls":[{"index":0,"function":{"arguments":"1}"}}]}}]}""",
        )
        val acumulado = AcumuladorDeChamadasDeFerramenta()
        primeiro.chamadas.forEach(acumulado::acrescentar)
        segundo.chamadas.forEach(acumulado::acrescentar)
        terceiro.chamadas.forEach(acumulado::acrescentar)
        val chamada = acumulado.listar().single()
        assertEquals("call_1", chamada.id)
        assertEquals("ler_texto_visivel", chamada.nome)
        assertEquals("""{"a":1}""", chamada.argumentos)
        assertEquals("Olá", terceiro.texto)
        assertEquals(null, primeiro.texto)
    }

    @Test
    fun aListaDeAbasMarcaAAbertaEIgnoraFerramentaEstranha() {
        val texto = FerramentasDeLeituraDoChatDaOpenRouter.textoDasAbas(
            listOf(
                AbaDoNavegadorChromebrothers(1, "Gatos", "https://gatos.test"),
                AbaDoNavegadorChromebrothers(2, "", "https://outra.test"),
            ),
            idAtiva = 2,
        )
        assertTrue(texto.contains("Gatos — https://gatos.test"))
        assertTrue(texto.contains("Sem título — https://outra.test (aba aberta)"))
        assertFalse(FerramentasDeLeituraDoChatDaOpenRouter.permitidas.contains("executar_javascript"))
        assertEquals("Nenhum texto selecionado.", FerramentasDeLeituraDoChatDaOpenRouter.textoDaSelecao("  "))
        assertTrue(FerramentasDeLeituraDoChatDaOpenRouter.scriptDaSelecao().contains("getSelection"))
    }

    @Test
    fun oAndamentoDaLeituraFalaEnquantoAcontece() {
        assertEquals(
            "Lendo o texto da página…",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloEmAndamento(FerramentasDeLeituraDoChatDaOpenRouter.TEXTO),
        )
        assertEquals(
            "Lendo o endereço da aba…",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloEmAndamento(FerramentasDeLeituraDoChatDaOpenRouter.ENDERECO),
        )
        assertEquals(
            "Listando as abas…",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloEmAndamento(FerramentasDeLeituraDoChatDaOpenRouter.ABAS),
        )
    }
}

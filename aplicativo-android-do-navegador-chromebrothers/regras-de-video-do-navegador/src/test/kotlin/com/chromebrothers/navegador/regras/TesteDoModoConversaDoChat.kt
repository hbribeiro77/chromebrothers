package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoModoConversaDoChat {
    @Test
    fun soFerramentasDeLeituraVaoNoModoChat() {
        val json = FerramentasDeLeituraDoChatDaOpenRouter.json(somenteLeitura = true)
        assertTrue(json.contains("\"name\":\"ler_texto_visivel\""))
        assertTrue(json.contains("\"name\":\"listar_componentes_da_biblioteca\""))
        assertTrue(json.contains("\"name\":\"listar_modelos_higgsfield\""))
        assertFalse(json.contains("\"name\":\"capturar_componente\""))
        AcoesConfirmadasDoChatDaOpenRouter.nomes.forEach {
            assertFalse(json.contains("\"name\":\"$it\""), it)
        }
    }

    @Test
    fun oModoAgenteContinuaComTudo() {
        val json = FerramentasDeLeituraDoChatDaOpenRouter.json()
        assertTrue(json.contains("\"name\":\"publicar_pagina_da_colecao\""))
        assertTrue(json.contains("\"name\":\"capturar_componente\""))
    }

    @Test
    fun acoesSaoRecusadasNoModoChat() {
        assertTrue(ModoConversaDoChat.podeUsar("ler_texto_visivel"))
        assertFalse(ModoConversaDoChat.podeUsar("publicar_pagina_da_colecao"))
        assertFalse(ModoConversaDoChat.podeUsar("gerar_video_vivix"))
        assertFalse(ModoConversaDoChat.podeUsar("capturar_componente"))
        assertFalse(ModoConversaDoChat.podeUsar("inventada"))
    }

    @Test
    fun aInstrucaoTrazOCatalogoDoAgente() {
        val texto = ModoConversaDoChat.instrucao("Aba: Sjinn")
        assertTrue(texto.contains("MODO CHAT"))
        assertTrue(texto.contains("## Plano"))
        assertTrue(texto.contains("- gerar_video_vivix: "))
        assertTrue(texto.contains("- publicar_pagina_da_colecao: "))
        assertFalse(texto.contains("\\\""))
    }

    @Test
    fun corpoDoModoChatLevaSoLeitura() {
        val corpo = PedidoDeChatParaAOpenRouter.corpo(
            "m",
            listOf(MensagemDeChat("user", "oi")),
            comFerramentas = true,
            somenteLeitura = true,
        )
        assertTrue(corpo.contains("ler_texto_visivel"))
        assertFalse(corpo.contains("\"name\":\"publicar_pagina_da_colecao\""))
    }

    @Test
    fun detectaPlanoComPassosNumerados() {
        assertTrue(PlanoDoModoConversaDoChat.temPlano("Olhei a página.\n\n## Plano\n1. Ler a página\n2. Publicar o layout"))
        assertTrue(PlanoDoModoConversaDoChat.temPlano("### plano\n1) a\n2) b"))
        assertFalse(PlanoDoModoConversaDoChat.temPlano("1. a\n2. b"))
        assertFalse(PlanoDoModoConversaDoChat.temPlano("## Plano\n1. só um passo"))
        assertFalse(PlanoDoModoConversaDoChat.temPlano("## Planos de assinatura\n1. a\n2. b"))
    }
}

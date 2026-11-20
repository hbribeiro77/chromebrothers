package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDasFerramentasDeListarGuardarAbrirEApagarComponentesDaBibliotecaNoChat {
    @Test
    fun jsonPermitidasEInstrucaoIncluemABibliotecaEPublicarNaoGrava() {
        val json = FerramentasDeLeituraDoChatDaOpenRouter.json()
        assertTrue(json.contains("listar_componentes_da_biblioteca"))
        assertTrue(json.contains("guardar_componente_da_colecao"))
        assertTrue(json.contains("abrir_componente_da_biblioteca"))
        assertTrue(json.contains("apagar_componente_da_biblioteca"))
        assertTrue(json.contains("ler_codigo_do_componente"))
        assertTrue(json.contains("capturar_componente"))
        assertFalse(json.contains("listar_paginas_da_biblioteca"))
        assertFalse(json.contains("guardar_pagina_da_colecao"))
        assertTrue(
            FerramentasDeLeituraDoChatDaOpenRouter.COMPONENTES_DA_BIBLIOTECA in
                FerramentasDeLeituraDoChatDaOpenRouter.permitidas,
        )
        assertTrue(
            FerramentasDeLeituraDoChatDaOpenRouter.LER_CODIGO_DO_COMPONENTE in
                FerramentasDeLeituraDoChatDaOpenRouter.permitidas,
        )
        assertTrue(
            FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_COMPONENTE in
                FerramentasDeLeituraDoChatDaOpenRouter.permitidas,
        )
        assertFalse(
            FerramentasDeLeituraDoChatDaOpenRouter.COMPONENTES_DA_BIBLIOTECA in
                AcoesConfirmadasDoChatDaOpenRouter.nomes,
        )
        assertFalse(
            FerramentasDeLeituraDoChatDaOpenRouter.LER_CODIGO_DO_COMPONENTE in
                AcoesConfirmadasDoChatDaOpenRouter.nomes,
        )
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.GUARDAR_COMPONENTE in AcoesConfirmadasDoChatDaOpenRouter.nomes)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_COMPONENTE in AcoesConfirmadasDoChatDaOpenRouter.nomes)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.APAGAR_COMPONENTE in AcoesConfirmadasDoChatDaOpenRouter.nomes)
        val instrucao = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: Sjinn")
        assertTrue(instrucao.contains("listar_componentes_da_biblioteca"))
        assertTrue(instrucao.contains("guardar_componente_da_colecao"))
        assertTrue(instrucao.contains("abrir_componente_da_biblioteca"))
        assertTrue(instrucao.contains("apagar_componente_da_biblioteca"))
        assertTrue(instrucao.contains("ler_codigo_do_componente"))
        assertTrue(instrucao.contains("capturar_componente"))
        assertTrue(instrucao.contains("publicar_pagina_da_colecao"))
        assertTrue(instrucao.contains("não grava na biblioteca"))
        assertEquals(
            "Listando os componentes da biblioteca…",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloEmAndamento(
                FerramentasDeLeituraDoChatDaOpenRouter.COMPONENTES_DA_BIBLIOTECA,
            ),
        )
        assertEquals(
            "Listou os componentes da biblioteca",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloVisivel(
                FerramentasDeLeituraDoChatDaOpenRouter.COMPONENTES_DA_BIBLIOTECA,
            ),
        )
        assertEquals(
            "Lendo o código do componente…",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloEmAndamento(
                FerramentasDeLeituraDoChatDaOpenRouter.LER_CODIGO_DO_COMPONENTE,
            ),
        )
        assertEquals(
            "Capturando o componente…",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloEmAndamento(
                FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_COMPONENTE,
            ),
        )
    }

    @Test
    fun aListaParaOChatMostraIdNomeDataDescricaoETags() {
        val texto = FerramentasDeLeituraDoChatDaOpenRouter.textoDosComponentesDaBiblioteca(
            listOf(
                ItemDaBibliotecaDeComponentesHtmlDaColecao(
                    id = "id-1",
                    nome = "Resumo",
                    html = "<p>a</p>",
                    atualizadoEm = 1_700_000_000_000,
                    descricao = "cartão",
                    tags = listOf("aula", "pdf"),
                ),
            ),
        )
        assertTrue(texto.contains("id-1"))
        assertTrue(texto.contains("Resumo"))
        assertTrue(texto.contains("2023-11-14 22:13"))
        assertTrue(texto.contains("cartão"))
        assertTrue(texto.contains("aula, pdf"))
        assertEquals(
            "Nenhum componente na biblioteca.",
            FerramentasDeLeituraDoChatDaOpenRouter.textoDosComponentesDaBiblioteca(emptyList()),
        )
    }

    @Test
    fun codigoGrandeVemTruncadoComOTotal() {
        val curto = "<p>ok</p>"
        assertEquals(curto, FerramentasDeLeituraDoChatDaOpenRouter.textoDoCodigoDoComponente(curto))
        val html = "a".repeat(FerramentasDeLeituraDoChatDaOpenRouter.LIMITE_DO_CODIGO_NO_CHAT + 1)
        val texto = FerramentasDeLeituraDoChatDaOpenRouter.textoDoCodigoDoComponente(html)
        assertTrue(texto.startsWith("a".repeat(FerramentasDeLeituraDoChatDaOpenRouter.LIMITE_DO_CODIGO_NO_CHAT)))
        assertTrue(texto.contains("codigo truncado, total=${html.length}"))
        val semId = FerramentasDeLeituraDoChatDaOpenRouter.idDoComponente("{}")
        assertEquals("Faltou o id.", (semId as ResultadoDoIdDoComponenteParaOChat.Recusado).mensagem)
        val comId = FerramentasDeLeituraDoChatDaOpenRouter.idDoComponente("""{"id":"abc"}""")
        assertEquals("abc", (comId as ResultadoDoIdDoComponenteParaOChat.Ok).id)
    }

    @Test
    fun guardarAceitaDescricaoETagsEAbrirExigeIdOuNome() {
        val guardar = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.GUARDAR_COMPONENTE,
            "{}",
        )
        val pedidoGuardar = (guardar as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
            as PedidoDeAcaoConfirmadaDoChat.GuardarComponenteDaColecao
        assertEquals(null, pedidoGuardar.descricao)
        assertEquals(null, pedidoGuardar.tags)
        assertEquals(
            "Guardar o componente da coleção na biblioteca?",
            AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedidoGuardar),
        )
        assertTrue(
            AcoesConfirmadasDoChatDaOpenRouter.interpretar(
                AcoesConfirmadasDoChatDaOpenRouter.GUARDAR_COMPONENTE,
                "",
            ) is ResultadoDaInterpretacaoDaAcaoDoChat.Pronta,
        )
        val comMeta = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.GUARDAR_COMPONENTE,
            """{"descricao":"bloco","tags":["aula","pdf"]}""",
        )
        val meta = (comMeta as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
            as PedidoDeAcaoConfirmadaDoChat.GuardarComponenteDaColecao
        assertEquals("bloco", meta.descricao)
        assertEquals(listOf("aula", "pdf"), meta.tags)
        val porVirgula = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.GUARDAR_COMPONENTE,
            """{"tags":"aula, pdf"}""",
        )
        val virgula = (porVirgula as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
            as PedidoDeAcaoConfirmadaDoChat.GuardarComponenteDaColecao
        assertEquals(listOf("aula", "pdf"), virgula.tags)

        val porId = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_COMPONENTE,
            """{"id":"id-1","nome":"Outro"}""",
        )
        val aberto = (porId as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
            as PedidoDeAcaoConfirmadaDoChat.AbrirComponenteDaBiblioteca
        assertEquals("id-1", aberto.id)
        assertEquals("Outro", aberto.nome)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.pergunta(aberto).contains("id-1"))

        val porNome = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_COMPONENTE,
            """{"nome":"Resumo"}""",
        )
        val soNome = (porNome as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
            as PedidoDeAcaoConfirmadaDoChat.AbrirComponenteDaBiblioteca
        assertEquals("", soNome.id)
        assertEquals("Resumo", soNome.nome)

        val semAlvo = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_COMPONENTE,
            "{}",
        )
        assertEquals(
            "Faltou o id ou o nome.",
            (semAlvo as ResultadoDaInterpretacaoDaAcaoDoChat.Recusada).mensagem,
        )
    }

    @Test
    fun apagarExigeOId() {
        val pronto = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.APAGAR_COMPONENTE,
            """{"id":"id-9"}""",
        )
        val pedido = (pronto as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
            as PedidoDeAcaoConfirmadaDoChat.ApagarComponenteDaBiblioteca
        assertEquals("id-9", pedido.id)
        assertEquals(
            "Apagar o componente id-9 da biblioteca?",
            AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedido),
        )
        val vazio = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.APAGAR_COMPONENTE,
            "{}",
        )
        assertEquals("Faltou o id.", (vazio as ResultadoDaInterpretacaoDaAcaoDoChat.Recusada).mensagem)
    }

    @Test
    fun siteLiberadoPulaConfirmacaoDeGuardarAbrirEApagar() {
        val sites = listOf("sjinn.ai")
        val aba = "https://www.sjinn.ai/aula"
        val guardar = PedidoDeAcaoConfirmadaDoChat.GuardarComponenteDaColecao()
        val abrir = PedidoDeAcaoConfirmadaDoChat.AbrirComponenteDaBiblioteca("id-1", "")
        val apagar = PedidoDeAcaoConfirmadaDoChat.ApagarComponenteDaBiblioteca("id-1")
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(guardar, aba, sites))
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(abrir, aba, sites))
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(apagar, aba, sites))
        assertFalse(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(guardar, "https://example.com", sites))
        assertFalse(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(abrir, "https://example.com", sites))
        assertFalse(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(apagar, "https://example.com", emptyList()))
        assertEquals("sjinn.ai", LiberacaoDeNavegacaoDosSitesParaOChat.hospedeiroDaAcao(guardar, aba))
    }
}

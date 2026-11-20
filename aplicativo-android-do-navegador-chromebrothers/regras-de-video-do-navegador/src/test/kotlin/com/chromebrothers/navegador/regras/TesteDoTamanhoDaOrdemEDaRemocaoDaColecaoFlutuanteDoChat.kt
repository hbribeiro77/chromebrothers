package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoTamanhoDaOrdemEDaRemocaoDaColecaoFlutuanteDoChat {
    private val aula = ItemDaColecaoFlutuante(
        TipoDoItemDaColecaoFlutuante.VIDEO,
        "https://cdn.exemplo/aula.mp4",
        "Aula",
        "",
    )
    private val capa = ItemDaColecaoFlutuante(
        TipoDoItemDaColecaoFlutuante.IMAGEM,
        "https://cdn.exemplo/capa.jpg",
        "Capa",
        "nota",
        TamanhoDoItemDaColecaoFlutuante.MEDIO,
    )

    @Test
    fun oTamanhoGrandeEntraESemTamanhoFicaPequeno() {
        val lista = """
            {"itens":[
              {"tipo":"video","endereco":"https://cdn.exemplo/aula.mp4","titulo":"Aula","tamanho":"grande"},
              {"tipo":"imagem","endereco":"https://cdn.exemplo/capa.jpg","titulo":"Capa"},
              {"tipo":"imagem","endereco":"javascript:alert(1)","titulo":"ruim","tamanho":"grande"}
            ]}
        """.trimIndent()
        val pronta = MontagemDaColecaoFlutuanteDoChat.ler(lista) as ResultadoDaListaDaColecao.Pronta
        assertEquals(TamanhoDoItemDaColecaoFlutuante.GRANDE, pronta.itens[0].tamanho)
        assertEquals(TamanhoDoItemDaColecaoFlutuante.PEQUENO, pronta.itens[1].tamanho)
        assertEquals("Montar a coleção?\n- vídeo grande: Aula\n- imagem: Capa", MontagemDaColecaoFlutuanteDoChat.pergunta(pronta.itens))
        assertEquals("aula.mp4", MontagemDaColecaoFlutuanteDoChat.tituloCurto(pronta.itens[0].endereco, "Vídeo"))
        assertEquals("Imagem", MontagemDaColecaoFlutuanteDoChat.tituloCurto("https://cdn.exemplo/", "Imagem"))
    }

    @Test
    fun organizarPoeOsCitadosNaFrenteEGuardaOResto() {
        val ordem = """
            {"ordem":[
              {"endereco":"https://cdn.exemplo/capa.jpg","tamanho":"grande"},
              {"endereco":"javascript:alert(1)"},
              {"endereco":"https://cdn.exemplo/ausente.jpg"},
              {"endereco":"https://cdn.exemplo/aula.mp4"}
            ]}
        """.trimIndent()
        val lida = MontagemDaColecaoFlutuanteDoChat.lerOrdem(ordem) as ResultadoDaOrdemDaColecao.Pronta
        val resultado = MontagemDaColecaoFlutuanteDoChat.organizar(listOf(aula, capa), lida.ajustes)
        assertEquals(listOf(capa.endereco, aula.endereco), resultado.itens.map { it.endereco })
        assertEquals(TamanhoDoItemDaColecaoFlutuante.GRANDE, resultado.itens[0].tamanho)
        assertEquals("nota", resultado.itens[0].nota)
        assertEquals(TamanhoDoItemDaColecaoFlutuante.PEQUENO, resultado.itens[1].tamanho)
        assertEquals("Reorganizei a coleção. Agora são 2.", resultado.mensagem)
        assertTrue(MontagemDaColecaoFlutuanteDoChat.perguntaDaOrdem(lida.ajustes).startsWith("Reorganizar a coleção?"))
        val vazia = MontagemDaColecaoFlutuanteDoChat.organizar(listOf(aula), listOf(AjusteDaOrdemDaColecao("https://cdn.exemplo/ausente.jpg", null)))
        assertEquals("Nenhum desses endereços está na coleção.", vazia.mensagem)
        assertEquals(listOf(aula), vazia.itens)
    }

    @Test
    fun tirarRemoveSoOEnderecoCitado() {
        val pedido = """{"enderecos":["https://cdn.exemplo/aula.mp4","javascript:alert(1)","https://cdn.exemplo/aula.mp4"]}"""
        val lidos = MontagemDaColecaoFlutuanteDoChat.lerEnderecosParaTirar(pedido) as ResultadoDosEnderecosParaTirarDaColecao.Prontos
        assertEquals(listOf("https://cdn.exemplo/aula.mp4"), lidos.enderecos)
        val um = MontagemDaColecaoFlutuanteDoChat.tirar(listOf(aula, capa), lidos.enderecos)
        assertEquals(listOf(capa), um.itens)
        assertEquals("Tirei 1 item. Agora são 1.", um.mensagem)
        val nenhum = MontagemDaColecaoFlutuanteDoChat.tirar(um.itens, listOf("https://cdn.exemplo/ausente.jpg"))
        assertEquals("Nenhum desses itens está na coleção.", nenhum.mensagem)
        val vazio = MontagemDaColecaoFlutuanteDoChat.tirar(listOf(capa), listOf(capa.endereco))
        assertEquals("Tirei 1 item. A coleção ficou vazia.", vazio.mensagem)
        assertTrue(MontagemDaColecaoFlutuanteDoChat.perguntaDeTirar(lidos.enderecos).startsWith("Tirar da coleção?"))
        val semLista = MontagemDaColecaoFlutuanteDoChat.lerEnderecosParaTirar("""{"ordem":[]}""")
        assertEquals("Faltou o endereço.", (semLista as ResultadoDosEnderecosParaTirarDaColecao.Recusada).mensagem)
    }

    @Test
    fun organizarETirarNaoSaoMaisFerramentas() {
        val ordem = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            "organizar_colecao",
            """{"ordem":[{"endereco":"https://cdn.exemplo/aula.mp4","tamanho":"medio"}]}""",
        )
        val tirar = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            "tirar_da_colecao",
            """{"enderecos":["https://cdn.exemplo/aula.mp4"]}""",
        )
        val modo = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            "abrir_modo_da_colecao",
            """{"modo":"pagina"}""",
        )
        assertTrue(ordem is ResultadoDaInterpretacaoDaAcaoDoChat.Recusada)
        assertTrue(tirar is ResultadoDaInterpretacaoDaAcaoDoChat.Recusada)
        assertTrue(modo is ResultadoDaInterpretacaoDaAcaoDoChat.Recusada)
        val definicoes = AcoesConfirmadasDoChatDaOpenRouter.definicoes().joinToString()
        assertFalse(definicoes.contains("organizar_colecao"))
        assertFalse(definicoes.contains("tirar_da_colecao"))
        assertFalse(definicoes.contains("abrir_modo_da_colecao"))
        assertFalse(definicoes.contains("navegar"))
        val instrucao = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: Sjinn")
        assertFalse(instrucao.contains("ferramenta de organizar"))
        assertFalse(instrucao.contains("ferramenta de tirar"))
        assertTrue(instrucao.contains("publicar_pagina_da_colecao"))
    }

    @Test
    fun reorganizarETirarNaoPedemFerramentaDeMidia() {
        val reorganizar = "Reorganiza a coleção e deixa a miniatura grande."
        assertTrue(ContinuacaoDoPedidoDoChat.faltam(reorganizar, emptySet()).isEmpty())
        val tirar = "Tira esse vídeo da coleção."
        assertTrue(ContinuacaoDoPedidoDoChat.faltam(tirar, emptySet()).isEmpty())
        assertFalse(ContinuacaoDoPedidoDoChat.precisa("O que tem nesta página?", emptySet(), 0))
    }

    @Test
    fun aumentarAsImagensNaoPedePublicarSoPorFalarDeWebview() {
        val faltam = ContinuacaoDoPedidoDoChat.faltam("Aumenta um pouco as imagens na webview", emptySet())
        assertFalse(faltam.contains(AcoesConfirmadasDoChatDaOpenRouter.PUBLICAR_PAGINA))
        val texto = MontagemDaColecaoFlutuanteDoChat.descrever(listOf(aula, capa))
        assertTrue(texto.contains("https://cdn.exemplo/aula.mp4"))
        assertTrue(texto.contains("pequeno"))
        assertTrue(texto.contains("medio"))
        val instrucao = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: Sjinn", colecao = texto)
        assertFalse(instrucao.contains("pequeno, medio e grande"))
        assertTrue(instrucao.contains("https://cdn.exemplo/capa.jpg"))
        assertTrue(instrucao.contains("publique a página HTML antes"))
    }
}

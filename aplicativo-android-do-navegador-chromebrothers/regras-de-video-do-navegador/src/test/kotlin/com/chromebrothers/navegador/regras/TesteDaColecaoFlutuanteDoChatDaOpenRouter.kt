package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaColecaoFlutuanteDoChatDaOpenRouter {
    private val lista = """
        {"itens":[
          {"tipo":"video","endereco":"https://cdn.exemplo/aula.mp4","titulo":"Aula 1","nota":""},
          {"tipo":"imagem","endereco":"https://cdn.exemplo/capa.jpg","titulo":"Capa","nota":"um \"gato\""},
          {"tipo":"imagem","endereco":"https://cdn.exemplo/aula.mp4","titulo":"Repetida","nota":""},
          {"tipo":"imagem","endereco":"javascript:alert(1)","titulo":"ruim","nota":""}
        ]}
    """.trimIndent()

    @Test
    fun aListaGuardaVideoEImagemEDescartaEnderecoRuim() {
        val pronta = MontagemDaColecaoFlutuanteDoChat.ler(lista) as ResultadoDaListaDaColecao.Pronta
        assertEquals(2, pronta.itens.size)
        assertEquals(TipoDoItemDaColecaoFlutuante.VIDEO, pronta.itens[0].tipo)
        assertEquals("Aula 1", pronta.itens[0].titulo)
        assertEquals(TipoDoItemDaColecaoFlutuante.IMAGEM, pronta.itens[1].tipo)
        assertEquals("um \"gato\"", pronta.itens[1].nota)
        assertEquals("Montar a coleção?\n- vídeo: Aula 1\n- imagem: Capa", MontagemDaColecaoFlutuanteDoChat.pergunta(pronta.itens))
    }

    @Test
    fun recusaListaVaziaOuAusente() {
        val vazia = MontagemDaColecaoFlutuanteDoChat.ler("""{"itens":[]}""")
        assertEquals("Nenhum item válido.", (vazia as ResultadoDaListaDaColecao.Recusada).mensagem)
        val semLista = MontagemDaColecaoFlutuanteDoChat.ler("""{"titulo":"oi"}""")
        assertEquals("Faltou a lista.", (semLista as ResultadoDaListaDaColecao.Recusada).mensagem)
    }

    @Test
    fun acrescentarEvitaRepetidoEParaNoLimite() {
        val atuais = List(MontagemDaColecaoFlutuanteDoChat.LIMITE - 1) {
            ItemDaColecaoFlutuante(TipoDoItemDaColecaoFlutuante.VIDEO, "https://cdn.exemplo/$it.mp4", "V$it", "")
        }
        val novos = listOf(
            atuais.first(),
            ItemDaColecaoFlutuante(TipoDoItemDaColecaoFlutuante.IMAGEM, "https://cdn.exemplo/nova.jpg", "Nova", "prompt"),
            ItemDaColecaoFlutuante(TipoDoItemDaColecaoFlutuante.IMAGEM, "https://cdn.exemplo/extra.jpg", "Extra", ""),
        )
        val resultado = MontagemDaColecaoFlutuanteDoChat.acrescentar(atuais, novos)
        assertEquals(MontagemDaColecaoFlutuanteDoChat.LIMITE, resultado.itens.size)
        assertEquals(1, resultado.acrescentados)
        assertEquals(1, resultado.jaEstavam)
        assertEquals(1, resultado.cortados)
        assertTrue(resultado.mensagem.contains("Coloquei 1 item"))
        assertTrue(resultado.mensagem.contains("já estava"))
        assertTrue(resultado.mensagem.contains("de fora"))

        val cheia = MontagemDaColecaoFlutuanteDoChat.acrescentar(resultado.itens, novos)
        assertEquals("A coleção chegou ao limite.", cheia.mensagem)
    }

    @Test
    fun aFerramentaDeMontarSaiuEPublicarContinuaNaInstrucao() {
        val resultado = AcoesConfirmadasDoChatDaOpenRouter.interpretar("montar_colecao", lista)
        assertTrue(resultado is ResultadoDaInterpretacaoDaAcaoDoChat.Recusada)
        assertFalse(AcoesConfirmadasDoChatDaOpenRouter.definicoes().any { it.contains("montar_colecao") })
        assertFalse(AcoesConfirmadasDoChatDaOpenRouter.definicoes().joinToString().contains("navegar"))
        val texto = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: Sjinn")
        assertTrue(texto.contains("coleção flutuante"))
        assertTrue(texto.contains("publicar_pagina_da_colecao"))
        assertFalse(texto.contains("montar_colecao"))
        val pedido = PedidoDeAcaoConfirmadaDoChat.PublicarPaginaDaColecao("t", "<p>a</p>")
        assertTrue(
            LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(
                pedido,
                "https://sjinn.ai",
                listOf("sjinn.ai"),
            ),
        )
    }
}

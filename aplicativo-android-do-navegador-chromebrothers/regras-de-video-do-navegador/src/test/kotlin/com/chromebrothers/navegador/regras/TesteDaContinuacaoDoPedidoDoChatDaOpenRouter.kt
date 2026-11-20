package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaContinuacaoDoPedidoDoChatDaOpenRouter {
    @Test
    fun aColecaoDeVideosPedePublicarPaginaHtml() {
        val pedido = "Acessa a página, vê meus assets e monta uma webview com 4 vídeos."
        val feitas = setOf(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_ENDERECO, AcoesConfirmadasDoChatDaOpenRouter.CLICAR)
        assertTrue(ContinuacaoDoPedidoDoChat.precisa(pedido, feitas, 0))
        val faltam = ContinuacaoDoPedidoDoChat.faltam(pedido, feitas)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.PUBLICAR_PAGINA in faltam)
        assertTrue(ContinuacaoDoPedidoDoChat.mensagem(faltam).contains("publicar a página"))
        assertFalse(
            ContinuacaoDoPedidoDoChat.precisa(
                pedido,
                feitas + AcoesConfirmadasDoChatDaOpenRouter.PUBLICAR_PAGINA,
                0,
            ),
        )
        assertFalse(ContinuacaoDoPedidoDoChat.precisa(pedido, feitas, ContinuacaoDoPedidoDoChat.LIMITE))
    }

    @Test
    fun capturaEPublicarPaginaTambemPedemContinuacao() {
        val pedido = "Captura a tela e publica uma página html na coleção com essa imagem."
        val faltam = ContinuacaoDoPedidoDoChat.faltam(pedido, emptySet())
        assertTrue(FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_ABA in faltam)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.PUBLICAR_PAGINA in faltam)
        assertTrue(ContinuacaoDoPedidoDoChat.precisa(pedido, emptySet(), 0))
        assertTrue(
            ContinuacaoDoPedidoDoChat.mensagem(faltam).contains("capturar a aba"),
        )
        assertTrue(
            ContinuacaoDoPedidoDoChat.mensagem(faltam).contains("publicar a página"),
        )
        assertEquals(12, ContinuacaoDoPedidoDoChat.LIMITE)
    }

    @Test
    fun respostaIncompletaSemAcaoDetectadaAindaContinua() {
        assertFalse(ContinuacaoDoPedidoDoChat.precisa("O que tem nesta página?", emptySet(), 0))
        assertTrue(
            ContinuacaoDoPedidoDoChat.pareceIncompleta(
                "Li a página. Agora vou capturar e depois montar a webview.",
            ),
        )
        assertTrue(
            ContinuacaoDoPedidoDoChat.precisa(
                "Me ajuda com isso na página.",
                emptySet(),
                0,
                "Pronto o primeiro passo. Depois vou publicar a página.",
            ),
        )
        assertTrue(
            ContinuacaoDoPedidoDoChat.mensagem(emptySet()).contains("prossiga"),
        )
    }

    @Test
    fun abrirComponenteDaBibliotecaNaoPedeEndereco() {
        val pedido = "abre o componente X da biblioteca"
        val faltam = ContinuacaoDoPedidoDoChat.faltam(pedido, emptySet())
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_COMPONENTE in faltam)
        assertFalse(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_ENDERECO in faltam)
        assertTrue(ContinuacaoDoPedidoDoChat.mensagem(faltam).contains("abrir o componente da biblioteca"))
        assertFalse(
            ContinuacaoDoPedidoDoChat.precisa(
                pedido,
                setOf(AcoesConfirmadasDoChatDaOpenRouter.ABRIR_COMPONENTE),
                0,
            ),
        )
    }

    @Test
    fun guardarComponenteNaBibliotecaPedeEssaAcao() {
        val pedido = "guarda este componente na biblioteca"
        val faltam = ContinuacaoDoPedidoDoChat.faltam(pedido, emptySet())
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.GUARDAR_COMPONENTE in faltam)
        assertTrue(ContinuacaoDoPedidoDoChat.mensagem(faltam).contains("guardar o componente na biblioteca"))
    }

    @Test
    fun lerCodigoECapturarComponentePedemEssasFerramentas() {
        val codigo = ContinuacaoDoPedidoDoChat.faltam("lê o código do componente da biblioteca", emptySet())
        assertTrue(FerramentasDeLeituraDoChatDaOpenRouter.LER_CODIGO_DO_COMPONENTE in codigo)
        assertTrue(ContinuacaoDoPedidoDoChat.mensagem(codigo).contains("ler o código do componente"))
        val captura = ContinuacaoDoPedidoDoChat.faltam("captura o componente da biblioteca", emptySet())
        assertTrue(FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_COMPONENTE in captura)
        assertFalse(FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_ABA in captura)
        assertTrue(ContinuacaoDoPedidoDoChat.mensagem(captura).contains("capturar o componente"))
    }

    @Test
    fun umaPerguntaSemAcaoNaoContinua() {
        assertFalse(ContinuacaoDoPedidoDoChat.precisa("O que tem nesta página?", emptySet(), 0))
        val instrucao = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: Sjinn")
        assertTrue(instrucao.contains("publique a página HTML antes"))
        assertTrue(instrucao.contains("não encerre no meio") || instrucao.contains("Não encerre no meio"))
        assertEquals(20, FerramentasDeLeituraDoChatDaOpenRouter.LIMITE_DE_RODADAS)
    }
}

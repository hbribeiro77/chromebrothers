package com.chromebrothers.navegador.regras

/**
 * Ações do chat que só mexem dentro do app (coleção, biblioteca, player, Downloads).
 * Não tocam no site da aba, não gastam créditos e não apagam nada: seguem sem
 * cartão de confirmação, em qualquer site e na página inicial.
 *
 * Ficam de fora (continuam pedindo): ações sobre o site (abrir, clicar, escrever,
 * preencher, vincular, roteiros), notas do site (o prompt trata a nota como palavra
 * do usuário), apagar componente e geração que consome créditos.
 */
object AcoesInternasDoChatSemConfirmacao {
    fun segueSemPerguntar(pedido: PedidoDeAcaoConfirmadaDoChat): Boolean = when (pedido) {
        is PedidoDeAcaoConfirmadaDoChat.PublicarPaginaDaColecao,
        is PedidoDeAcaoConfirmadaDoChat.MostrarMarkdownNaColecao,
        is PedidoDeAcaoConfirmadaDoChat.GerarMarkdown,
        is PedidoDeAcaoConfirmadaDoChat.GerarZip,
        is PedidoDeAcaoConfirmadaDoChat.GuardarComponenteDaColecao,
        is PedidoDeAcaoConfirmadaDoChat.AbrirComponenteDaBiblioteca,
        is PedidoDeAcaoConfirmadaDoChat.MostrarBotaoFlutuanteDoVideo,
        is PedidoDeAcaoConfirmadaDoChat.AbrirPlayerDoVideoDetectado,
        is PedidoDeAcaoConfirmadaDoChat.AbrirVideoDaBiblioteca,
        is PedidoDeAcaoConfirmadaDoChat.AbrirImagemDaBiblioteca,
        is PedidoDeAcaoConfirmadaDoChat.EditarPaginaDaColecao,
        -> true
        is PedidoDeAcaoConfirmadaDoChat.CriarAba -> pedido.endereco.isBlank()
        else -> false
    }
}

/**
 * Entrega da coleção por resposta do chat: dentro de uma mesma resposta, a página
 * publicada reaproveita a mesma janela e fica escondida; só aparece quando a
 * resposta termina (ou quando o modelo pede para capturá-la).
 */
class EntregaDaColecaoPorRespostaDoChat<J : Any> {
    var emAndamento: Boolean = false
        private set
    var janela: J? = null
        private set

    fun iniciar() {
        emAndamento = true
        janela = null
    }

    /** Janela a reaproveitar nesta resposta, se ainda existir. */
    fun reaproveitar(existe: (J) -> Boolean): J? {
        if (!emAndamento) return null
        val atual = janela ?: return null
        return if (existe(atual)) atual else null.also { janela = null }
    }

    fun guardar(nova: J) {
        if (emAndamento) janela = nova
    }

    /** Encerra a resposta e devolve a janela que deve aparecer agora (se houver). */
    fun concluir(): J? {
        val pendente = if (emAndamento) janela else null
        emAndamento = false
        janela = null
        return pendente
    }
}

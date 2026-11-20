package com.chromebrothers.navegador.regras

sealed class ResultadoDaLiberacaoDeNavegacao {
    data class Liberada(
        val sites: List<String>,
        val hospedeiro: String,
    ) : ResultadoDaLiberacaoDeNavegacao()

    data class Recusada(val mensagem: String) : ResultadoDaLiberacaoDeNavegacao()
}

object LiberacaoDeNavegacaoDosSitesParaOChat {
    const val LIMITE = 40

    fun chave(site: String): String = CadernoDeNotasDosSitesParaOChat.chave(site)

    fun liberar(sites: List<String>, site: String): ResultadoDaLiberacaoDeNavegacao {
        val hospedeiro = chave(site)
        val recusa = when {
            hospedeiro.isEmpty() -> "Faltou o site."
            EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh("https://$hospedeiro/") ->
                "A página inicial não recebe liberação."
            else -> ""
        }
        if (recusa.isNotEmpty()) return ResultadoDaLiberacaoDeNavegacao.Recusada(recusa)
        val restantes = sites.filterNot { it == hospedeiro }
        if (restantes.size >= LIMITE) {
            return ResultadoDaLiberacaoDeNavegacao.Recusada("A lista chegou ao limite.")
        }
        return ResultadoDaLiberacaoDeNavegacao.Liberada(listOf(hospedeiro) + restantes, hospedeiro)
    }

    fun tirar(sites: List<String>, site: String): List<String> {
        val hospedeiro = chave(site)
        return sites.filterNot { it == hospedeiro }
    }

    fun contem(sites: List<String>, site: String): Boolean {
        val hospedeiro = chave(site)
        return hospedeiro.isNotEmpty() && sites.any { it == hospedeiro }
    }

    fun hospedeiroDaAcao(pedido: PedidoDeAcaoConfirmadaDoChat, siteDaAba: String): String {
        val site = when (pedido) {
            is PedidoDeAcaoConfirmadaDoChat.AbrirEndereco -> pedido.endereco
            is PedidoDeAcaoConfirmadaDoChat.CriarAba -> pedido.endereco
            is PedidoDeAcaoConfirmadaDoChat.LerPaginaEmSegundoPlano -> pedido.endereco
            is PedidoDeAcaoConfirmadaDoChat.Clicar,
            is PedidoDeAcaoConfirmadaDoChat.EscreverNaPagina,
            is PedidoDeAcaoConfirmadaDoChat.PreencherCampo,
            is PedidoDeAcaoConfirmadaDoChat.VincularCampo,
            is PedidoDeAcaoConfirmadaDoChat.GerarMarkdown,
            is PedidoDeAcaoConfirmadaDoChat.GerarZip,
            is PedidoDeAcaoConfirmadaDoChat.PublicarPaginaDaColecao,
            is PedidoDeAcaoConfirmadaDoChat.MostrarMarkdownNaColecao,
            is PedidoDeAcaoConfirmadaDoChat.GuardarComponenteDaColecao,
            is PedidoDeAcaoConfirmadaDoChat.AbrirComponenteDaBiblioteca,
            is PedidoDeAcaoConfirmadaDoChat.ApagarComponenteDaBiblioteca,
            is PedidoDeAcaoConfirmadaDoChat.MostrarBotaoFlutuanteDoVideo,
            is PedidoDeAcaoConfirmadaDoChat.AbrirPlayerDoVideoDetectado,
            is PedidoDeAcaoConfirmadaDoChat.AbrirVideoDaBiblioteca,
            is PedidoDeAcaoConfirmadaDoChat.AbrirImagemDaBiblioteca,
            is PedidoDeAcaoConfirmadaDoChat.GuardarRoteiroDeAutomacao,
            is PedidoDeAcaoConfirmadaDoChat.RodarRoteiroDeAutomacao,
            is PedidoDeAcaoConfirmadaDoChat.ClicarElemento,
            is PedidoDeAcaoConfirmadaDoChat.PreencherElemento,
            is PedidoDeAcaoConfirmadaDoChat.ApertarTecla,
            -> siteDaAba
            else -> ""
        }
        if (site.isBlank()) return ""
        val hospedeiro = chave(site)
        if (hospedeiro.isEmpty()) return ""
        if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh("https://$hospedeiro/")) return ""
        return hospedeiro
    }

    fun segueSemPerguntar(
        pedido: PedidoDeAcaoConfirmadaDoChat,
        siteDaAba: String,
        sites: List<String>,
    ): Boolean {
        val hospedeiro = hospedeiroDaAcao(pedido, siteDaAba)
        return hospedeiro.isNotEmpty() && sites.any { it == hospedeiro }
    }
}

object TextoDaListaDeSitesComNavegacaoLiberada {
    fun escrever(sites: List<String>): String = sites.joinToString("\n")

    fun ler(bruto: String): List<String> {
        if (bruto.isBlank()) return emptyList()
        return bruto.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
    }
}

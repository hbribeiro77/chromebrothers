package com.chromebrothers.navegador.regras

/**
 * Pasta virtual que o Visualizador de mídia (e qualquer componente) pode listar
 * via `chromebrothers.listarPasta("chromebrothers://midia")` — Downloads + Pictures/ChromeBrothers.
 */
object PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao {
    const val URI: String = "chromebrothers://midia"
    const val NOME: String = "ChromeBrothers"

    fun ehUriVirtual(uri: String): Boolean =
        uri.trim().equals(URI, ignoreCase = true)

    fun ehVisualizadorDeMidiaPeloNome(nome: String): Boolean {
        val limpo = nome.trim().lowercase()
        return limpo.contains("visualizador de mídia") || limpo.contains("visualizador de midia")
    }

    fun itemDaPasta(arquivo: ArquivoDeMidiaSalvoNaGaveta): ItemDaPastaEscolhidaPeloComponente =
        ItemDaPastaEscolhidaPeloComponente(
            nome = arquivo.nomeDoArquivo,
            uri = arquivo.uri,
            mime = arquivo.mime,
            pasta = false,
            tamanho = arquivo.tamanho.coerceAtLeast(0L),
        )

    fun montarListagem(itens: List<ItemDaPastaEscolhidaPeloComponente>): String =
        ListagemDePastaEscolhidaPeloComponenteDaColecao.mensagemOk(
            nomePasta = NOME,
            uriPasta = URI,
            itens = itens,
            limiteDeItens = LimiteDeItensExibidosNoVisualizadorDeMidiaNaColecao.LIMITE,
        )
}

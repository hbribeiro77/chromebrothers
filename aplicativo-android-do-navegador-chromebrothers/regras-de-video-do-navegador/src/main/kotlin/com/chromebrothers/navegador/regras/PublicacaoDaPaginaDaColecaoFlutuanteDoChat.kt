package com.chromebrothers.navegador.regras

sealed class ResultadoDaPublicacaoDaPaginaDaColecao {
    data class Pronta(val titulo: String, val html: String) : ResultadoDaPublicacaoDaPaginaDaColecao()
    data class Recusada(val mensagem: String) : ResultadoDaPublicacaoDaPaginaDaColecao()
}

object PublicacaoDaPaginaDaColecaoFlutuanteDoChat {
    const val LIMITE_DO_HTML = 10 * 1024 * 1024
    const val LIMITE_DO_TITULO = 120

    fun ler(argumentos: String): ResultadoDaPublicacaoDaPaginaDaColecao {
        val titulo = TextoJsonDoChat.valorDaChave(argumentos, "titulo")?.trim().orEmpty()
        val htmlBruto = TextoJsonDoChat.valorDaChave(argumentos, "html").orEmpty()
        if (titulo.isEmpty()) {
            return ResultadoDaPublicacaoDaPaginaDaColecao.Recusada("Faltou o título.")
        }
        if (titulo.length > LIMITE_DO_TITULO) {
            return ResultadoDaPublicacaoDaPaginaDaColecao.Recusada("O título passou do limite.")
        }
        if (htmlBruto.trim().isEmpty()) {
            return ResultadoDaPublicacaoDaPaginaDaColecao.Recusada("Faltou o HTML.")
        }
        if (htmlBruto.length > LIMITE_DO_HTML) {
            return ResultadoDaPublicacaoDaPaginaDaColecao.Recusada("O HTML passou do limite.")
        }
        val html = SanitizacaoDoHtmlEstaticoDaPaginaDaColecaoFlutuante.aplicar(htmlBruto)
        if (html.trim().isEmpty()) {
            return ResultadoDaPublicacaoDaPaginaDaColecao.Recusada("O HTML ficou vazio depois da limpeza.")
        }
        return ResultadoDaPublicacaoDaPaginaDaColecao.Pronta(titulo, html)
    }

    fun pergunta(titulo: String): String = "Publicar página na coleção?\n$titulo"

    fun mensagemPublicada(titulo: String): String =
        "Abri a página \"$titulo\" numa nova janela da coleção."
}

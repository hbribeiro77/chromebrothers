package com.chromebrothers.navegador.regras

/**
 * Grava .md em Downloads/ChromeBrothers/markdown e monta HTML leitor para a coleção.
 */
object LeitorDeMarkdownNaColecaoDoNavegador {
    const val PASTA_RELATIVA_DOWNLOADS = "ChromeBrothers/markdown"
    const val LIMITE_DO_TITULO = 120
    const val LIMITE_DO_CONTEUDO = 100_000

    fun slugDoTitulo(titulo: String): String {
        val limpo = titulo.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(60)
        return limpo.ifBlank { "documento" }
    }

    fun nomeDoArquivo(titulo: String): String = "${slugDoTitulo(titulo)}.md"

    fun htmlDoLeitor(titulo: String, markdown: String): String {
        val corpo = htmlDoCorpo(markdown)
        val tituloEsc = escaparHtml(titulo.trim().ifBlank { "Markdown" })
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
              <meta charset="utf-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1"/>
              <title>$tituloEsc</title>
              <style>
                :root { color-scheme: light dark; }
                body {
                  margin: 0;
                  padding: 20px 18px 40px;
                  font-family: "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                  font-size: 16px;
                  line-height: 1.55;
                  color: #1a1a1a;
                  background: #f7f6f3;
                }
                @media (prefers-color-scheme: dark) {
                  body { color: #e8e6e3; background: #1c1b1a; }
                  pre { background: #2a2928; }
                  a { color: #8ab4f8; }
                }
                h1.titulo-doc {
                  font-size: 1.45rem;
                  margin: 0 0 16px;
                  font-weight: 700;
                }
                h2 { font-size: 1.2rem; margin: 1.1em 0 0.4em; }
                strong { font-weight: 700; }
                pre {
                  overflow-x: auto;
                  padding: 12px;
                  border-radius: 8px;
                  background: #ebe8e1;
                  font-size: 0.9rem;
                }
                code { font-family: ui-monospace, Consolas, monospace; }
                a { color: #1a73e8; }
                .meta {
                  font-size: 0.8rem;
                  opacity: 0.65;
                  margin-bottom: 18px;
                }
              </style>
            </head>
            <body>
              <h1 class="titulo-doc">$tituloEsc</h1>
              <div class="meta">ChromeBrothers · markdown</div>
              <article>$corpo</article>
            </body>
            </html>
        """.trimIndent()
    }

    fun htmlDoCorpo(markdown: String): String {
        val trechos = RenderizacaoDoMarkdownDoChat.renderizar(markdown)
        return buildString {
            trechos.forEach { trecho ->
                val texto = escaparHtml(trecho.texto)
                when (trecho.estilo) {
                    EstiloDoTrechoDoMarkdownDoChat.TITULO -> append("<h2>").append(texto).append("</h2>")
                    EstiloDoTrechoDoMarkdownDoChat.NEGRITO -> append("<strong>").append(texto).append("</strong>")
                    EstiloDoTrechoDoMarkdownDoChat.CODIGO ->
                        append("<pre><code>").append(texto).append("</code></pre>")
                    EstiloDoTrechoDoMarkdownDoChat.NORMAL -> {
                        if (trecho.endereco.isNotBlank()) {
                            append("<a href=\"")
                                .append(escaparHtml(trecho.endereco))
                                .append("\">")
                                .append(texto)
                                .append("</a>")
                        } else {
                            append(texto.replace("\n", "<br>"))
                        }
                    }
                }
            }
        }
    }

    fun escaparHtml(texto: String): String = buildString {
        texto.forEach { c ->
            when (c) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(c)
            }
        }
    }

    fun lerPedido(argumentos: String): ResultadoDoLeitorDeMarkdownNaColecao {
        val titulo = TextoJsonDoChat.valorDaChave(argumentos, "titulo")?.trim().orEmpty()
        val conteudo = TextoJsonDoChat.valorDaChave(argumentos, "conteudo").orEmpty()
        return when {
            titulo.isBlank() -> ResultadoDoLeitorDeMarkdownNaColecao.Recusado("Faltou o título.")
            titulo.length > LIMITE_DO_TITULO ->
                ResultadoDoLeitorDeMarkdownNaColecao.Recusado("O título passou do limite.")
            conteudo.isBlank() -> ResultadoDoLeitorDeMarkdownNaColecao.Recusado("Faltou o conteúdo.")
            conteudo.length > LIMITE_DO_CONTEUDO ->
                ResultadoDoLeitorDeMarkdownNaColecao.Recusado("O conteúdo passou do limite.")
            else -> ResultadoDoLeitorDeMarkdownNaColecao.Pronto(
                PedidoDeAcaoConfirmadaDoChat.MostrarMarkdownNaColecao(titulo, conteudo),
            )
        }
    }

    fun pergunta(pedido: PedidoDeAcaoConfirmadaDoChat.MostrarMarkdownNaColecao): String =
        "Mostrar markdown na coleção?\n${pedido.titulo}"
}

sealed class ResultadoDoLeitorDeMarkdownNaColecao {
    data class Pronto(
        val pedido: PedidoDeAcaoConfirmadaDoChat.MostrarMarkdownNaColecao,
    ) : ResultadoDoLeitorDeMarkdownNaColecao()

    data class Recusado(val mensagem: String) : ResultadoDoLeitorDeMarkdownNaColecao()
}

package com.chromebrothers.navegador.regras

object ContextoDaPaginaParaOChat {
    const val LIMITE = 6000

    /** Teto de segurança do texto completo (caminho paginado); não é o limite de 6000 do contexto. */
    const val TETO_DO_TEXTO_COMPLETO = 2_000_000

    /** Texto da página normalizado no JS, sem corte de 6000 (só o teto de segurança); usado na leitura paginada. */
    fun scriptDoTextoCompletoNormalizado(): String = """
        (function(){
          var bruto = document.body ? (document.body.innerText || "") : "";
          return bruto.replace(/\s+/g, " ").trim().slice(0, $TETO_DO_TEXTO_COMPLETO);
        })()
    """.trimIndent()

    /** Texto visível truncado no JS (polling de carregamento e ler_texto_visivel). */
    fun scriptDoTextoVisivel(): String = """
        (function(){
          var bruto = document.body ? (document.body.innerText || "") : "";
          return bruto.replace(/\s+/g, " ").trim().slice(0, $LIMITE);
        })()
    """.trimIndent()

    fun mensagem(titulo: String, endereco: String, texto: String): String {
        val nome = titulo.trim().ifBlank { "Sem título" }
        val url = endereco.trim().ifBlank { "sem endereço" }
        val corpo = texto.trim().take(LIMITE)
        return if (corpo.isEmpty()) {
            "Página aberta: $nome\nEndereço: $url"
        } else {
            "Página aberta: $nome\nEndereço: $url\n\n$corpo"
        }
    }
}

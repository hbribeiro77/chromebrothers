package com.chromebrothers.navegador.regras

object CliqueNoTextoVisivelDaPaginaParaOChat {
    const val LIMITE = 80

    fun script(texto: String): String = """
        (function () {
          var alvo = ${literal(texto)}.replace(/\s+/g, " ").trim().toLowerCase();
          if (!alvo) return "sem-alvo";
          function visivel(el) {
            if (!el || !el.getBoundingClientRect) return false;
            var retangulo = el.getBoundingClientRect();
            if (retangulo.width < 1 || retangulo.height < 1) return false;
            var estilo = window.getComputedStyle(el);
            return estilo.visibility !== "hidden" && estilo.display !== "none";
          }
          function textoDe(el) {
            var bruto = el.innerText || el.getAttribute("aria-label") || el.getAttribute("title") || "";
            return String(bruto).replace(/\s+/g, " ").trim();
          }
          function escolher(raiz, profundidade, exato, contem) {
            if (!raiz || !raiz.querySelectorAll || profundidade > 6) return { exato: exato, contem: contem };
            var lista = raiz.querySelectorAll("a, button, summary, label, [role='button'], [role='link'], [role='menuitem'], [role='tab'], [role='option']");
            for (var i = 0; i < lista.length; i++) {
              var el = lista[i];
              if (!visivel(el)) continue;
              var textoEl = textoDe(el);
              if (!textoEl) continue;
              var baixo = textoEl.toLowerCase();
              if (baixo === alvo) return { exato: el, contem: contem };
              if (!contem && baixo.indexOf(alvo) >= 0 && textoEl.length <= 80) contem = el;
            }
            var todos = raiz.querySelectorAll("*");
            for (var j = 0; j < todos.length && profundidade < 6; j++) {
              if (todos[j].shadowRoot) {
                var interno = escolher(todos[j].shadowRoot, profundidade + 1, exato, contem);
                exato = interno.exato;
                contem = interno.contem;
                if (exato) return interno;
              }
            }
            return { exato: exato, contem: contem };
          }
          var achado = escolher(document, 0, null, null);
          var escolhido = achado.exato || achado.contem;
          if (!escolhido) return "sem-alvo";
          escolhido.click();
          return "ok";
        })()
    """.trimIndent()

    private fun literal(texto: String) = buildString {
        append('"')
        texto.forEach { caractere ->
            when (caractere) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> if (caractere.code < 32) append("\\u%04x".format(caractere.code)) else append(caractere)
            }
        }
        append('"')
    }
}

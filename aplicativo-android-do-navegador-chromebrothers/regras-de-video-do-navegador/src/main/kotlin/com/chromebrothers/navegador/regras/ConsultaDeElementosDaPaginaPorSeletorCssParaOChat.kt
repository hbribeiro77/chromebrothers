package com.chromebrothers.navegador.regras

sealed class ResultadoDoPedidoDeConsulta {
    data class Ok(val seletor: String, val offset: Int, val limite: Int) : ResultadoDoPedidoDeConsulta()
    data class Erro(val mensagem: String) : ResultadoDoPedidoDeConsulta()
}

object ConsultaDeElementosDaPaginaPorSeletorCssParaOChat {
    const val LIMITE_DO_SELETOR = 300
    const val LIMITE_DE_ITENS = 40
    private const val LIMITE_DO_TEXTO_DO_ITEM = 200
    fun lerPedido(argumentos: String): ResultadoDoPedidoDeConsulta {
        val seletor = TextoJsonDoChat.valorDaChave(argumentos, "seletor")?.trim().orEmpty()
        if (seletor.isEmpty()) return ResultadoDoPedidoDeConsulta.Erro("Informe o seletor CSS.")
        if (seletor.length > LIMITE_DO_SELETOR) {
            return ResultadoDoPedidoDeConsulta.Erro("Seletor CSS muito longo (máximo $LIMITE_DO_SELETOR caracteres).")
        }
        val offset = (TextoJsonDoChat.inteiroDaChave(argumentos, "offset") ?: 0).coerceAtLeast(0)
        val limite = TextoJsonDoChat.inteiroDaChave(argumentos, "limite")
        val limiteAceito = if (limite == null || limite < 1) LIMITE_DE_ITENS else limite.coerceAtMost(LIMITE_DE_ITENS)
        return ResultadoDoPedidoDeConsulta.Ok(seletor, offset, limiteAceito)
    }

    fun descrever(brutoDoScript: String, offset: Int = 0, limite: Int = LIMITE_DE_ITENS): String {
        val linhas = brutoDoScript.lines().filter { it.isNotBlank() }
        if (linhas.isEmpty()) return "Não foi possível consultar a página (resposta vazia)."
        val meta = linhas.first().split('\t')
        if (meta.firstOrNull() == "erro") {
            val mensagem = meta.getOrNull(1)?.trim().orEmpty().ifBlank { "erro desconhecido" }
            return "Erro ao consultar elementos: $mensagem"
        }
        if (meta.firstOrNull() != "ok") return "Não foi possível consultar a página (resposta inesperada)."
        val total = meta.getOrNull(2)?.trim()?.toIntOrNull() ?: 0
        val inicio = offset.coerceAtLeast(0)
        val maximo = limite.coerceIn(1, LIMITE_DE_ITENS)
        val itens = linhas.drop(1).mapNotNull(::lerLinha).take(maximo)
        if (total == 0 && itens.isEmpty()) return "Nenhum elemento encontrado para o seletor."
        val proximoOffset = if (itens.isEmpty()) inicio.coerceAtMost(total) else inicio + itens.size
        val temMais = proximoOffset < total
        val paginacao = "total=$total offset=$inicio proximo_offset=$proximoOffset tem_mais=${if (temMais) "sim" else "nao"}"
        val cabeca = "Elementos encontrados: $total (mostrando ${itens.size}):\n$paginacao"
        return if (itens.isEmpty()) cabeca else cabeca + "\n" + itens.joinToString("\n")
    }

    private fun lerLinha(linha: String): String? {
        val partes = linha.split('\t')
        val tag = partes.getOrNull(0)?.trim().orEmpty()
        if (tag.isEmpty()) return null
        fun campo(indice: Int) = partes.getOrNull(indice)?.trim().orEmpty()
        val texto = campo(1).take(LIMITE_DO_TEXTO_DO_ITEM)
        val href = urlHttp(campo(2))
        val src = urlHttp(campo(3))
        val alt = campo(4)
        val title = campo(5)
        val aria = campo(6)
        val filhos = campo(7).split('|')
            .filter { filho -> urlHttp(filho.substringAfter('=', "")).isNotEmpty() }
            .joinToString("|")
        val detalhes = buildList {
            if (href.isNotEmpty()) add("href=$href")
            if (src.isNotEmpty()) add("src=$src")
            if (alt.isNotEmpty()) add("alt=$alt")
            if (title.isNotEmpty()) add("title=$title")
            if (aria.isNotEmpty()) add("aria=$aria")
            if (filhos.isNotEmpty()) add("filhos=$filhos")
        }
        val cabeca = if (texto.isEmpty()) "- $tag" else "- $tag $texto"
        return if (detalhes.isEmpty()) cabeca else cabeca + " — " + detalhes.joinToString(" | ")
    }

    private fun urlHttp(valor: String): String {
        val limpo = valor.trim()
        return if (limpo.startsWith("http://") || limpo.startsWith("https://")) limpo else ""
    }

    fun script(seletor: String, offset: Int, limite: Int): String {
        val seletorJs = "\"" + TextoJsonDoChat.escapar(seletor) + "\""
        val inicio = offset.coerceAtLeast(0)
        val maximo = limite.coerceIn(1, LIMITE_DE_ITENS)
        return """
        (function () {
          var seletor = $seletorJs;
          var inicio = $inicio;
          var maximo = $maximo;
          function limpo(valor, tamanho) {
            return String(valor == null ? "" : valor).replace(/[\t\r\n]+/g, " ").replace(/\s+/g, " ").trim().slice(0, tamanho);
          }
          function atributoDeUrl(el, nome) {
            if (!el) return "";
            var direto = el[nome];
            if (typeof direto === "string") return direto;
            return (el.getAttribute && el.getAttribute(nome)) || "";
          }
          function absoluto(valor) {
            try {
              var bruto = String(valor || "");
              if (!bruto) return "";
              var url = new URL(bruto, location.href).href;
              if (!/^https?:/i.test(url)) return "";
              return url;
            } catch (erro) { return ""; }
          }
          var lista;
          try {
            lista = document.querySelectorAll(seletor);
          } catch (erro) {
            return "erro\tSeletor CSS inválido.";
          }
          var linhas = ["ok\ttotal\t" + lista.length];
          var fim = Math.min(lista.length, inicio + maximo);
          for (var i = inicio; i < fim; i++) {
            var el = lista[i];
            var filhos = [];
            var dentro = el.querySelectorAll ? el.querySelectorAll("img, a, video") : [];
            for (var j = 0; j < dentro.length && filhos.length < 10; j++) {
              var filho = dentro[j];
              var nome = (filho.tagName || "").toLowerCase();
              var bruto = nome === "a"
                ? atributoDeUrl(filho, "href")
                : ((typeof filho.currentSrc === "string" && filho.currentSrc) || atributoDeUrl(filho, "src"));
              var url = absoluto(bruto);
              if (url) filhos.push(nome + "=" + url);
            }
            linhas.push([
              limpo((el.tagName || "").toLowerCase(), 30),
              limpo(el.innerText || el.textContent || "", $LIMITE_DO_TEXTO_DO_ITEM),
              absoluto(atributoDeUrl(el, "href")),
              absoluto((typeof el.currentSrc === "string" && el.currentSrc) || atributoDeUrl(el, "src")),
              limpo(el.getAttribute ? el.getAttribute("alt") : "", 120),
              limpo(el.getAttribute ? el.getAttribute("title") : "", 120),
              limpo(el.getAttribute ? el.getAttribute("aria-label") : "", 120),
              limpo(filhos.join("|"), 1500)
            ].join("\t"));
          }
          return linhas.join("\n");
        })()
        """.trimIndent()
    }
}

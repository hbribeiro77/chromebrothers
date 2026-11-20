package com.chromebrothers.navegador.regras

data class EnderecoVisivelDaPagina(
    val tipo: String,
    val endereco: String,
    val rotulo: String,
)

object TextoDaMidiaObservadaParaOChat {
    fun descrever(pedidos: List<PedidoDeMidiaObservado>): String {
        val uteis = EscolhaDoPedidoDeMidiaParaBaixar.listar(pedidos).take(EnderecosVisiveisDaPaginaParaOChat.LIMITE_DA_MIDIA)
        if (uteis.isEmpty()) return ""
        return "Mídia pedida pela página:\n" + uteis.joinToString("\n") { pedido ->
            "- video ${pedido.endereco} — ${RotuloDoPedidoDeMidiaParaBaixar.texto(pedido)}"
        }
    }

    fun relato(pedida: String, visivel: String): String {
        val partes = listOf(pedida, visivel).filter { it.isNotBlank() }
        if (partes.isEmpty()) return "Nenhuma mídia encontrada nesta página."
        return partes.joinToString("\n\n")
    }
}

object EnderecosVisiveisDaPaginaParaOChat {
    const val LIMITE_DA_MIDIA = 40
    const val LIMITE_DOS_ENDERECOS = 80
    private const val LIMITE_DO_TEXTO = 6000
    private val tipos = setOf("imagem", "video", "link")

    fun ler(bruto: String): List<EnderecoVisivelDaPagina> {
        if (bruto.isBlank()) return emptyList()
        val itens = LinkedHashMap<String, EnderecoVisivelDaPagina>()
        bruto.lineSequence().forEach { linha ->
            val partes = linha.split('\t')
            if (partes.size < 2) return@forEach
            val tipo = partes[0].trim()
            val endereco = partes[1].trim()
            val rotulo = partes.getOrNull(2)?.trim()?.take(120).orEmpty()
            if (tipo !in tipos) return@forEach
            if (!endereco.startsWith("http://") && !endereco.startsWith("https://")) return@forEach
            if (endereco.length > 2000) return@forEach
            if (endereco !in itens) itens[endereco] = EnderecoVisivelDaPagina(tipo, endereco, rotulo)
        }
        return itens.values.toList()
    }

    fun descreverMidia(bruto: String): String {
        val itens = ler(bruto).filter { it.tipo == "imagem" || it.tipo == "video" }
        return formatar(itens, "Mídia visível na página:", LIMITE_DA_MIDIA)
    }

    fun descreverEnderecos(bruto: String): String {
        val texto = formatar(ler(bruto), "Endereços da página:", LIMITE_DOS_ENDERECOS)
        return texto.ifBlank { "Nenhum endereço http encontrado na página." }.take(LIMITE_DO_TEXTO)
    }

    fun scriptDaMidia(): String = script(limite = LIMITE_DA_MIDIA, todosOsLinks = false)

    fun scriptDosEnderecos(): String = script(limite = LIMITE_DOS_ENDERECOS, todosOsLinks = true)

    private fun formatar(itens: List<EnderecoVisivelDaPagina>, titulo: String, limite: Int): String {
        val usados = itens.take(limite)
        if (usados.isEmpty()) return ""
        return titulo + "\n" + usados.joinToString("\n") { item ->
            val nota = if (item.rotulo.isBlank()) "" else " — ${item.rotulo}"
            "- ${item.tipo} ${item.endereco}$nota"
        }
    }

    private fun script(limite: Int, todosOsLinks: Boolean): String = """
        (function () {
          var vistos = {};
          var linhas = [];
          var limite = $limite;
          var todosOsLinks = ${if (todosOsLinks) "true" else "false"};
          function absoluto(valor) {
            try {
              var url = new URL(String(valor || ""), location.href).href;
              if (!/^https?:/i.test(url)) return "";
              return url;
            } catch (erro) { return ""; }
          }
          function rotulo(el) {
            if (!el) return "";
            var bruto = el.getAttribute("alt") || el.getAttribute("aria-label") || el.getAttribute("title") || el.innerText || "";
            return String(bruto).replace(/\s+/g, " ").replace(/\t/g, " ").trim().slice(0, 120);
          }
          function por(tipo, valor, el) {
            if (linhas.length >= limite) return;
            var url = absoluto(valor);
            if (!url || vistos[url]) return;
            vistos[url] = true;
            linhas.push(tipo + "\t" + url + "\t" + rotulo(el));
          }
          function pareceArquivo(href) {
            return /\.(mp4|m4v|webm|mkv|mov|m3u8|mpd|jpg|jpeg|png|gif|webp)(\?|#|$)/i.test(href);
          }
          function andar(raiz, profundidade) {
            if (!raiz || !raiz.querySelectorAll || profundidade > 6 || linhas.length >= limite) return;
            var lista = raiz.querySelectorAll("img, video, audio, source, a, iframe");
            for (var i = 0; i < lista.length; i++) {
              var el = lista[i];
              var tag = (el.tagName || "").toLowerCase();
              if (tag === "img") por("imagem", el.currentSrc || el.src, el);
              else if (tag === "video" || tag === "audio") {
                por("video", el.currentSrc || el.src, el);
                if (el.poster) por("imagem", el.poster, el);
              } else if (tag === "source") por("video", el.src, el);
              else if (tag === "a") {
                var href = el.href || "";
                if (todosOsLinks) por("link", href, el);
                else if (pareceArquivo(href)) por(/\.(jpg|jpeg|png|gif|webp)(\?|#|$)/i.test(href) ? "imagem" : "video", href, el);
              } else if (tag === "iframe" && todosOsLinks) por("link", el.src, el);
            }
            var todos = raiz.querySelectorAll("*");
            for (var j = 0; j < todos.length && profundidade < 6; j++) {
              if (todos[j].shadowRoot) andar(todos[j].shadowRoot, profundidade + 1);
            }
          }
          andar(document, 0);
          return linhas.join("\n");
        })()
    """.trimIndent()
}

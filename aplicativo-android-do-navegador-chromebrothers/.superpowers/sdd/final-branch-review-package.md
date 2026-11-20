# Final feature review package
Also wiring in PainelDoChat + AtividadePrincipal (lerTextoPaginado/consultarElementos/rolarPagina).

## FILE: regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\TextoJsonDoChat.kt
```
package com.chromebrothers.navegador.regras

object TextoJsonDoChat {
    fun escapar(texto: String): String = buildString {
        texto.forEach { caractere ->
            when (caractere) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (caractere.code < 0x20) {
                    append("\\u%04x".format(caractere.code))
                } else {
                    append(caractere)
                }
            }
        }
    }

    fun valorDaChave(json: String, chave: String): String? {
        val marca = "\"$chave\""
        var indice = 0
        while (indice < json.length) {
            val achou = json.indexOf(marca, indice)
            if (achou < 0) return null
            var cursor = achou + marca.length
            while (cursor < json.length && json[cursor].isWhitespace()) cursor++
            if (cursor >= json.length || json[cursor] != ':') {
                indice = achou + 1
                continue
            }
            cursor++
            while (cursor < json.length && json[cursor].isWhitespace()) cursor++
            if (json.startsWith("null", cursor)) return null
            if (cursor >= json.length || json[cursor] != '"') {
                indice = achou + 1
                continue
            }
            return textoNaAspa(json, cursor)?.first
        }
        return null
    }

    fun inteiroDaChave(json: String, chave: String): Int? =
        textoDoNumeroDaChave(json, chave)?.toIntOrNull()

    fun numeroDaChave(json: String, chave: String): Double? =
        textoDoNumeroDaChave(json, chave)?.toDoubleOrNull()

    private fun textoDoNumeroDaChave(json: String, chave: String): String? {
        val marca = "\"$chave\""
        var indice = 0
        while (indice < json.length) {
            val achou = json.indexOf(marca, indice)
            if (achou < 0) return null
            var cursor = achou + marca.length
            while (cursor < json.length && json[cursor].isWhitespace()) cursor++
            if (cursor >= json.length || json[cursor] != ':') {
                indice = achou + 1
                continue
            }
            cursor++
            while (cursor < json.length && json[cursor].isWhitespace()) cursor++
            if (cursor >= json.length) return null
            if (json.startsWith("null", cursor)) return null
            if (json[cursor] == '"') return textoNaAspa(json, cursor)?.first
            return tokenNumerico(json, cursor)
        }
        return null
    }

    private fun tokenNumerico(json: String, inicio: Int): String? {
        var cursor = inicio
        if (cursor < json.length && json[cursor] == '-') cursor++
        if (cursor >= json.length || !json[cursor].isDigit()) return null
        while (cursor < json.length && json[cursor].isDigit()) cursor++
        if (cursor < json.length && json[cursor] == '.') {
            val depoisDoPonto = cursor + 1
            if (depoisDoPonto < json.length && json[depoisDoPonto].isDigit()) {
                cursor = depoisDoPonto
                while (cursor < json.length && json[cursor].isDigit()) cursor++
            }
        }
        return json.substring(inicio, cursor)
    }

    fun textoNaAspa(json: String, indiceDaAspa: Int): Pair<String, Int>? {
        val saida = StringBuilder()
        var indice = indiceDaAspa + 1
        while (indice < json.length) {
            val caractere = json[indice]
            if (caractere == '"') return saida.toString() to (indice + 1)
            if (caractere == '\\' && indice + 1 < json.length) {
                when (json[indice + 1]) {
                    'n' -> saida.append('\n')
                    'r' -> saida.append('\r')
                    't' -> saida.append('\t')
                    '"' -> saida.append('"')
                    '\\' -> saida.append('\\')
                    'u' -> {
                        if (indice + 5 < json.length) {
                            val hex = json.substring(indice + 2, indice + 6)
                            saida.append(hex.toIntOrNull(16)?.toChar() ?: '?')
                            indice += 6
                            continue
                        }
                    }
                    else -> saida.append(json[indice + 1])
                }
                indice += 2
                continue
            }
            saida.append(caractere)
            indice++
        }
        return null
    }

    fun objetosDaChave(json: String, chave: String): List<String>? {
        val marca = "\"$chave\""
        val achou = json.indexOf(marca)
        if (achou < 0) return null
        var cursor = achou + marca.length
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != ':') return null
        cursor++
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != '[') return null
        cursor++
        val objetos = mutableListOf<String>()
        while (cursor < json.length) {
            while (cursor < json.length && (json[cursor].isWhitespace() || json[cursor] == ',')) cursor++
            if (cursor >= json.length) return null
            if (json[cursor] == ']') return objetos
            if (json[cursor] != '{') return null
            val fim = fimDoObjeto(json, cursor) ?: return null
            objetos.add(json.substring(cursor, fim))
            cursor = fim
        }
        return null
    }

    fun objetoDaChave(json: String, chave: String): String? {
        val marca = "\"$chave\""
        val achou = json.indexOf(marca)
        if (achou < 0) return null
        var cursor = achou + marca.length
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != ':') return null
        cursor++
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != '{') return null
        val fim = fimDoObjeto(json, cursor) ?: return null
        return json.substring(cursor, fim)
    }

    fun textosDaChave(json: String, chave: String): List<String>? {
        val marca = "\"$chave\""
        val achou = json.indexOf(marca)
        if (achou < 0) return null
        var cursor = achou + marca.length
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != ':') return null
        cursor++
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != '[') return null
        cursor++
        val textos = mutableListOf<String>()
        while (cursor < json.length) {
            while (cursor < json.length && (json[cursor].isWhitespace() || json[cursor] == ',')) cursor++
            if (cursor >= json.length) return null
            if (json[cursor] == ']') return textos
            if (json[cursor] != '"') return null
            val lido = textoNaAspa(json, cursor) ?: return null
            textos.add(lido.first)
            cursor = lido.second
        }
        return null
    }

    private fun fimDoObjeto(json: String, inicio: Int): Int? {
        var profundidade = 0
        var indice = inicio
        while (indice < json.length) {
            val caractere = json[indice]
            if (caractere == '"') {
                indice = textoNaAspa(json, indice)?.second ?: return null
                continue
            }
            if (caractere == '{') profundidade++
            if (caractere == '}') {
                profundidade--
                if (profundidade == 0) return indice + 1
            }
            indice++
        }
        return null
    }
}

```

## FILE: regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\LeituraPaginadaDoTextoDaPaginaParaOChat.kt
```
package com.chromebrothers.navegador.regras

data class PedidoDaLeituraPaginada(
    val offset: Int,
    val limite: Int,
)

data class FatiaDoTextoDaPagina(
    val corpo: String,
    val total: Int,
    val offset: Int,
    val proximoOffset: Int,
    val temMais: Boolean,
)

object LeituraPaginadaDoTextoDaPaginaParaOChat {
    const val LIMITE_PADRAO = 6000

    fun lerPedido(argumentos: String): PedidoDaLeituraPaginada {
        val offset = TextoJsonDoChat.inteiroDaChave(argumentos, "offset") ?: 0
        val limite = TextoJsonDoChat.inteiroDaChave(argumentos, "limite")
        return PedidoDaLeituraPaginada(
            offset = offset.coerceAtLeast(0),
            limite = limiteAceito(limite),
        )
    }

    fun fatiar(textoNormalizado: String, offset: Int, limite: Int): FatiaDoTextoDaPagina {
        val total = textoNormalizado.length
        val inicio = offset.coerceAtLeast(0)
        if (inicio >= total) {
            return FatiaDoTextoDaPagina(
                corpo = "",
                total = total,
                offset = inicio,
                proximoOffset = total,
                temMais = false,
            )
        }
        val fim = (inicio + limiteAceito(limite)).coerceAtMost(total)
        return FatiaDoTextoDaPagina(
            corpo = textoNormalizado.substring(inicio, fim),
            total = total,
            offset = inicio,
            proximoOffset = fim,
            temMais = fim < total,
        )
    }

    fun mensagem(titulo: String, endereco: String, fatia: FatiaDoTextoDaPagina): String {
        val nome = titulo.trim().ifBlank { "Sem título" }
        val url = endereco.trim().ifBlank { "sem endereço" }
        val temMais = if (fatia.temMais) "sim" else "nao"
        val meta = "total=${fatia.total} offset=${fatia.offset} proximo_offset=${fatia.proximoOffset} tem_mais=$temMais"
        val cabeca = "Página aberta: $nome\nEndereço: $url\n\n$meta"
        return if (fatia.corpo.isEmpty()) cabeca else "$cabeca\n\n${fatia.corpo}"
    }

    fun normalizar(bruto: String): String =
        bruto.replace(Regex("(?U)\\s+"), " ").trim()

    private fun limiteAceito(limite: Int?): Int {
        if (limite == null || limite < 1) return LIMITE_PADRAO
        return limite.coerceAtMost(LIMITE_PADRAO)
    }
}

```

## FILE: regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\ConsultaDeElementosDaPaginaPorSeletorCssParaOChat.kt
```
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

    fun descrever(brutoDoScript: String): String {
        val linhas = brutoDoScript.lines().filter { it.isNotBlank() }
        if (linhas.isEmpty()) return "Não foi possível consultar a página (resposta vazia)."
        val meta = linhas.first().split('\t')
        if (meta.firstOrNull() == "erro") {
            val mensagem = meta.getOrNull(1)?.trim().orEmpty().ifBlank { "erro desconhecido" }
            return "Erro ao consultar elementos: $mensagem"
        }
        if (meta.firstOrNull() != "ok") return "Não foi possível consultar a página (resposta inesperada)."
        val total = meta.getOrNull(2)?.trim()?.toIntOrNull() ?: 0
        val itens = linhas.drop(1).mapNotNull(::lerLinha).take(LIMITE_DE_ITENS)
        if (total == 0 && itens.isEmpty()) return "Nenhum elemento encontrado para o seletor."
        val cabeca = "Elementos encontrados: $total (mostrando ${itens.size}):"
        return cabeca + "\n" + itens.joinToString("\n")
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
              var url = absoluto(nome === "a" ? filho.href : (filho.currentSrc || filho.src));
              if (url) filhos.push(nome + "=" + url);
            }
            linhas.push([
              limpo((el.tagName || "").toLowerCase(), 30),
              limpo(el.innerText || el.textContent || "", $LIMITE_DO_TEXTO_DO_ITEM),
              absoluto(el.href || (el.getAttribute && el.getAttribute("href"))),
              absoluto(el.currentSrc || el.src || (el.getAttribute && el.getAttribute("src"))),
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

```

## FILE: regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\RolagemDaPaginaDaAbaParaOChat.kt
```
package com.chromebrothers.navegador.regras

sealed class PedidoDeRolagemDaPagina {
    data class PorSeletor(val seletor: String) : PedidoDeRolagemDaPagina()
    data class Absoluto(val y: Int) : PedidoDeRolagemDaPagina()
    data class PorPaginas(val paginas: Double) : PedidoDeRolagemDaPagina()
    data class Relativo(val pixels: Int) : PedidoDeRolagemDaPagina()
}

sealed class ResultadoDoPedidoDeRolagem {
    data class Ok(val pedido: PedidoDeRolagemDaPagina) : ResultadoDoPedidoDeRolagem()
    data class Erro(val mensagem: String) : ResultadoDoPedidoDeRolagem()
}

object RolagemDaPaginaDaAbaParaOChat {
    const val LIMITE_DO_SELETOR = 300
    private const val MARGEM_DO_FIM_EM_PIXELS = 2

    /** Prioridade: seletor > y > paginas > pixels. */
    fun lerPedido(argumentos: String): ResultadoDoPedidoDeRolagem {
        val seletor = TextoJsonDoChat.valorDaChave(argumentos, "seletor")?.trim().orEmpty()
        if (seletor.isNotEmpty()) {
            if (seletor.length > LIMITE_DO_SELETOR) {
                return ResultadoDoPedidoDeRolagem.Erro("Seletor CSS muito longo (máximo $LIMITE_DO_SELETOR caracteres).")
            }
            return ResultadoDoPedidoDeRolagem.Ok(PedidoDeRolagemDaPagina.PorSeletor(seletor))
        }
        if (chavePresente(argumentos, "y")) {
            val y = inteiro(argumentos, "y")
                ?: return ResultadoDoPedidoDeRolagem.Erro("O valor de y precisa ser um número.")
            return ResultadoDoPedidoDeRolagem.Ok(PedidoDeRolagemDaPagina.Absoluto(y.coerceAtLeast(0)))
        }
        if (chavePresente(argumentos, "paginas")) {
            val paginas = TextoJsonDoChat.numeroDaChave(argumentos, "paginas")?.takeIf { it.isFinite() }
                ?: return ResultadoDoPedidoDeRolagem.Erro("O valor de paginas precisa ser um número.")
            return ResultadoDoPedidoDeRolagem.Ok(PedidoDeRolagemDaPagina.PorPaginas(paginas))
        }
        if (chavePresente(argumentos, "pixels")) {
            val pixels = inteiro(argumentos, "pixels")
                ?: return ResultadoDoPedidoDeRolagem.Erro("O valor de pixels precisa ser um número.")
            return ResultadoDoPedidoDeRolagem.Ok(PedidoDeRolagemDaPagina.Relativo(pixels))
        }
        return ResultadoDoPedidoDeRolagem.Erro("Informe seletor, y, paginas ou pixels para rolar a página.")
    }

    /** A chave existe com valor diferente de null (mesmo que o valor seja inválido). */
    private fun chavePresente(argumentos: String, chave: String): Boolean =
        Regex("\"" + Regex.escape(chave) + "\"\\s*:\\s*(?!null)\\S").containsMatchIn(argumentos)

    private fun inteiro(argumentos: String, chave: String): Int? =
        TextoJsonDoChat.inteiroDaChave(argumentos, chave)
            ?: TextoJsonDoChat.numeroDaChave(argumentos, chave)?.takeIf { it.isFinite() }?.toInt()

    fun mensagem(bruto: String): String {
        val linha = bruto.lines().firstOrNull { it.isNotBlank() }
            ?: return "Não foi possível rolar a página (resposta vazia)."
        val partes = linha.split('\t')
        if (partes.firstOrNull() == "erro") {
            val motivo = partes.getOrNull(1)?.trim().orEmpty().ifBlank { "erro desconhecido" }
            return "Erro ao rolar a página: $motivo"
        }
        if (partes.firstOrNull() != "ok") return "Não foi possível rolar a página (resposta inesperada)."
        val y = partes.getOrNull(1)?.trim()?.toIntOrNull()
        val altura = partes.getOrNull(2)?.trim()?.toIntOrNull()
        val viewport = partes.getOrNull(3)?.trim()?.toIntOrNull()
        if (y == null || altura == null || viewport == null) {
            return "Não foi possível rolar a página (resposta inesperada)."
        }
        val noFim = if (y + viewport >= altura - MARGEM_DO_FIM_EM_PIXELS) "sim" else "nao"
        return "Página rolada: y=$y, altura_pagina=$altura, altura_viewport=$viewport, no_fim=$noFim"
    }

    fun script(pedido: PedidoDeRolagemDaPagina): String {
        val acao = when (pedido) {
            is PedidoDeRolagemDaPagina.PorSeletor -> {
                val seletorJs = "\"" + TextoJsonDoChat.escapar(pedido.seletor) + "\""
                """
                var alvo;
                try {
                  alvo = document.querySelector($seletorJs);
                } catch (erro) {
                  return "erro\tSeletor CSS inválido.";
                }
                if (!alvo) return "erro\tElemento não encontrado.";
                alvo.scrollIntoView({ block: "center", inline: "nearest", behavior: "instant" });
                """
            }
            is PedidoDeRolagemDaPagina.Absoluto ->
                "window.scrollTo({ top: ${pedido.y}, left: window.scrollX, behavior: \"instant\" });"
            is PedidoDeRolagemDaPagina.PorPaginas ->
                "window.scrollBy({ top: Math.round(${pedido.paginas} * window.innerHeight), left: 0, behavior: \"instant\" });"
            is PedidoDeRolagemDaPagina.Relativo ->
                "window.scrollBy({ top: ${pedido.pixels}, left: 0, behavior: \"instant\" });"
        }
        return """
        (function () {
          try {
            $acao
            var raiz = document.documentElement || {};
            var corpo = document.body || {};
            var y = Math.round(window.scrollY || window.pageYOffset || raiz.scrollTop || 0);
            var altura = Math.round(Math.max(raiz.scrollHeight || 0, corpo.scrollHeight || 0));
            var viewport = Math.round(window.innerHeight || raiz.clientHeight || 0);
            return "ok\t" + y + "\t" + altura + "\t" + viewport;
          } catch (erro) {
            return "erro\tNão deu para rolar a página.";
          }
        })()
        """.trimIndent()
    }
}

```

## FILE: regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\FerramentasDeLeituraDoChatDaOpenRouter.kt
```
package com.chromebrothers.navegador.regras

data class ParteDeConteudoDoChat(
    val tipo: String,
    val texto: String = "",
    val mime: String = "",
    val nome: String = "",
    val dados: String = "",
)

data class ChamadaDeFerramenta(
    val id: String,
    val nome: String,
    val argumentos: String,
)

data class PedacoDeChamadaDeFerramenta(
    val indice: Int,
    val id: String = "",
    val nome: String = "",
    val argumentos: String = "",
)

data class FimDoEnvioDoChat(
    val erro: String? = null,
    val chamadas: List<ChamadaDeFerramenta> = emptyList(),
)

class AcumuladorDeChamadasDeFerramenta {
    private val itens = sortedMapOf<Int, MutableList<String>>()
    private val ids = sortedMapOf<Int, String>()
    private val nomes = sortedMapOf<Int, String>()

    fun acrescentar(pedaco: PedacoDeChamadaDeFerramenta) {
        if (pedaco.id.isNotBlank()) ids[pedaco.indice] = pedaco.id
        if (pedaco.nome.isNotBlank()) nomes[pedaco.indice] = pedaco.nome
        if (pedaco.argumentos.isNotEmpty()) {
            itens.getOrPut(pedaco.indice) { mutableListOf() }.add(pedaco.argumentos)
        } else {
            itens.getOrPut(pedaco.indice) { mutableListOf() }
        }
    }

    fun listar(): List<ChamadaDeFerramenta> {
        return itens.keys.map { indice ->
            ChamadaDeFerramenta(
                id = ids[indice].orEmpty().ifBlank { "chamada-$indice" },
                nome = nomes[indice].orEmpty(),
                argumentos = itens[indice].orEmpty().joinToString(""),
            )
        }
    }
}

object FerramentasDeLeituraDoChatDaOpenRouter {
    const val ENDERECO = "ler_endereco_e_titulo"
    const val TEXTO = "ler_texto_visivel"
    const val SELECAO = "ler_texto_selecionado"
    const val ABAS = "listar_abas"
    const val MIDIA = "listar_midia_da_pagina"
    const val ENDERECOS = "ler_enderecos_da_pagina"
    const val CAMPOS = "listar_campos_da_pagina"
    const val CAPTURAR_ABA = "capturar_aba"
    const val TEXTO_PAGINADO = "ler_texto_da_pagina"
    const val ELEMENTOS = "consultar_elementos"
    const val ROLAR = "rolar_pagina"
    val permitidas = setOf(
        ENDERECO, TEXTO, SELECAO, ABAS, MIDIA, ENDERECOS, CAMPOS, CAPTURAR_ABA,
        TEXTO_PAGINADO, ELEMENTOS, ROLAR,
    ) + AcoesConfirmadasDoChatDaOpenRouter.nomes
    const val LIMITE_DE_RODADAS = 10

    fun json(): String {
        val leitura = listOf(
            definicao(ENDERECO, "Le o titulo e o endereco da aba aberta no navegador."),
            definicao(TEXTO, "Le o texto visivel da aba aberta, ate 6000 caracteres. Para paginas grandes, prefira ler_texto_da_pagina (com offset) ou consultar_elementos."),
            definicao(SELECAO, "Le o texto que o usuario selecionou na aba aberta."),
            definicao(ABAS, "Lista as abas abertas e marca qual esta na frente."),
            definicao(MIDIA, "Lista vídeos e imagens da aba: o que a página pediu e o que está visível, com endereço http ou https."),
            definicao(ENDERECOS, "Lista endereços http ou https visíveis na aba, como links e arquivos, quando a mídia da página não bastar."),
            definicao(CAMPOS, "Lista caixas de texto e botões visíveis na aba, com id, nome, rotulo, tag e papel, para vincular o campo do site."),
            definicao(
                CAPTURAR_ABA,
                "Captura a area visivel da aba aberta (screenshot), envia a imagem para voce ver o layout e devolve um endereco file:// local. Use esse endereco em publicar_pagina_da_colecao (img src) ou montar_colecao (tipo imagem). Use quando o texto nao bastar ou quando precisar reaproveitar a captura na webview/colecao.",
            ),
            definicaoComProps(
                TEXTO_PAGINADO,
                "Le o texto da pagina inteira em blocos. Passe offset (posicao inicial em caracteres) e limite (tamanho do bloco) para continuar de onde parou em paginas grandes.",
                """{"offset":{"type":"integer"},"limite":{"type":"integer"}}""",
                "[]",
            ),
            definicaoComProps(
                ELEMENTOS,
                "Consulta elementos da pagina por seletor CSS (ex.: a, h2, .card) e devolve texto e atributos de cada um. Use offset e limite para paginar o resultado. Util para listas longas e paginas grandes.",
                """{"seletor":{"type":"string"},"offset":{"type":"integer"},"limite":{"type":"integer"}}""",
                """["seletor"]""",
            ),
            definicaoComProps(
                ROLAR,
                "Rola a pagina para carregar conteudo sob demanda (lazy-load). Informe seletor para rolar ate um elemento, y para uma posicao, paginas para rolar N telas ou pixels para rolar um valor. Depois leia de novo o texto ou os elementos.",
                """{"seletor":{"type":"string"},"y":{"type":"integer"},"paginas":{"type":"number"},"pixels":{"type":"integer"}}""",
                "[]",
            ),
        )
        return (leitura + AcoesConfirmadasDoChatDaOpenRouter.definicoes()).joinToString(prefix = "[", postfix = "]")
    }

    fun scriptDaSelecao(): String = """
        (function(){
          var s = window.getSelection ? String(window.getSelection()) : "";
          return s.replace(/\s+/g, " ").trim().slice(0, ${ContextoDaPaginaParaOChat.LIMITE});
        })()
    """.trimIndent()

    fun textoDaSelecao(texto: String): String {
        val limpo = texto.trim()
        return if (limpo.isEmpty()) "Nenhum texto selecionado." else limpo.take(ContextoDaPaginaParaOChat.LIMITE)
    }

    fun textoDasAbas(abas: List<AbaDoNavegadorChromebrothers>, idAtiva: Long): String {
        if (abas.isEmpty()) return "Nenhuma aba aberta."
        return abas.joinToString("\n") { aba ->
            val titulo = aba.titulo.trim().ifBlank { "Sem título" }
            val endereco = aba.endereco.trim().ifBlank { "sem endereço" }
            val marca = if (aba.id == idAtiva) " (aba aberta)" else ""
            "- $titulo — $endereco$marca"
        }
    }

    fun instrucao(
        rotuloDaAba: String,
        notaDoSite: String = "",
        navegacaoLiberada: Boolean = false,
        colecao: String = "",
        vinculo: String = "",
    ): String {
        val base = "Você está no navegador ChromeBrothers, falando da $rotuloDaAba. " +
            "Use as ferramentas de leitura para ver a página e as abas. " +
            "Quando o layout importar ou o texto não bastar, use capturar_aba para ver a área visível da aba (screenshot). A ferramenta também grava a imagem e devolve um endereço file://: use esse endereço em publicar_pagina_da_colecao (por exemplo img src) ou em montar_colecao com tipo imagem. " +
            "Para abrir um endereço, criar uma aba ou preencher o campo já vinculado, use as ferramentas de ação. O usuário confirma antes. " +
            "Num site já liberado, abrir, clicar, vincular, coleção (montar/organizar/tirar), publicar página da coleção e gerar markdown ou ZIP seguem sem confirmação. Preencher o campo e guardar nota continuam pedindo. " +
            "Depois de abrir um endereço, o resultado já espera o carregamento. Se o texto visível vier vazio ou curto, a página pode ainda estar montando o conteúdo. Não diga que não renderizou só por isso. " +
            "Na página inicial, a leitura traz os atalhos fixados com nome e endereço. Essa aba é do navegador, não um site quebrado. " +
            "Se houver uma nota deste site, ela foi escrita pelo usuário: siga esse caminho. Para guardar ou trocar uma nota, use a ferramenta e espere a confirmação. " +
            "Para clicar num texto visível, como um menu, use a ferramenta de clique. Um clique por vez. O usuário confirma antes, salvo se este site já foi liberado. Não diga que o clique não existe. " +
            "Para colocar vídeos ou imagens numa coleção flutuante, use a ferramenta de coleção. Cada item leva tipo video ou imagem, o endereço http https (vídeo/imagem) ou file:// de captura da aba (só imagem), o título, a nota ao lado e, se quiser, tamanho pequeno, medio ou grande. Sem tamanho, a miniatura fica pequena. Se a nota não estiver na página, deixe vazia. O usuário confirma antes, salvo site liberado. Não invente endereço. " +
            "Para mudar a ordem ou o tamanho do que já está na coleção, use a ferramenta de organizar. O que não citar fica no fim. Para tirar um item, use a ferramenta de tirar. Organizar e tirar pedem confirmação, salvo site liberado. " +
            "A coleção tem dois modos: midia (miniaturas) e pagina (HTML com CSS e JS local). Para publicar, use publicar_pagina_da_colecao com titulo e html. Pode incluir botão que chama chromebrothers.gerarPdf('nome') para salvar PDF igual à página em Downloads. Sem script remoto nem fetch. Imagens só http, https ou file. Republicar substitui a página. Para só trocar o modo, use abrir_modo_da_colecao com midia ou pagina. " +
            "Para gerar um documento, use gerar_markdown (titulo, conteudo, imagens opcionais com endereco e nome). O app salva .md e pasta assets. Para um pacote, use gerar_zip (nome, itens com caminho_no_zip, e se quiser conteudo_markdown). " +
            "A webview da IA é essa coleção. Os tamanhos, do menor ao maior, são pequeno, medio e grande. A coleção atual vem no fim, com endereço e tamanho. Para mudar tamanho ou ordem, use organizar com esses endereços, na mesma ordem. Não monte outra coleção no lugar. " +
            "Para achar esse endereço, leia primeiro a mídia da página. Se o arquivo não aparecer, leia os endereços da página e copie o http ou https que servir. " +
            "Se o pedido pede uma ação, termine essa ação antes de escrever a resposta. Achar a página ou os assets é um passo. Se pediram a coleção, monte a coleção antes de explicar. Se pediram para reorganizar ou tirar da coleção, faça isso antes de explicar. Se pediram markdown ou zip, gere o arquivo antes de explicar. Se pediram a página HTML da coleção, publique antes de explicar. " +
            "Se o pedido tiver várias etapas, use as ferramentas até concluir tudo neste turno. Não encerre no meio, não descreva o próximo passo sem fazê-lo e não peça para o usuário dizer prossiga. " +
            "O vínculo da caixa e do botão deste site vem no fim. Para ver as caixas e os botões visíveis, leia os campos da página. Para gravar, use a ferramenta de vincular e copie id, nome, rotulo, tag e papel dessa lista. " +
            "Em página grande ou longa, não se contente com o primeiro trecho: use consultar_elementos com um seletor CSS para achar listas, títulos e links, e ler_texto_da_pagina com offset para continuar a leitura em blocos. Se o conteúdo carrega ao rolar (lazy-load), use rolar_pagina (seletor, y, paginas ou pixels) e leia de novo. Não diga que não consegue ver tudo sem antes tentar essas ferramentas. " +
            "Não invente o conteúdo da página."
        val nota = notaDoSite.trim()
        val comNota = if (nota.isEmpty()) base else "$base\n$nota"
        val comLiberacao = if (navegacaoLiberada) {
            "$comNota\nNeste site a navegação está liberada: abrir, clicar, vincular, coleção, publicar página e gerar markdown/ZIP seguem sem pedir confirmação. Outro site continua pedindo."
        } else {
            comNota
        }
        val lista = colecao.trim()
        val comLista = if (lista.isEmpty()) comLiberacao else "$comLiberacao\n$lista"
        val marca = vinculo.trim()
        return if (marca.isEmpty()) comLista else "$comLista\n$marca"
    }

    fun rotuloVisivel(nome: String): String = when (nome) {
        ENDERECO -> "Leu o endereço da aba"
        TEXTO -> "Leu o texto da página"
        SELECAO -> "Leu o texto selecionado"
        ABAS -> "Listou as abas"
        MIDIA -> "Leu a mídia da página"
        ENDERECOS -> "Leu os endereços da página"
        CAMPOS -> "Leu os campos da página"
        CAPTURAR_ABA -> "Capturou a área visível da aba"
        TEXTO_PAGINADO -> "Leu um bloco do texto da página"
        ELEMENTOS -> "Consultou elementos da página"
        ROLAR -> "Rolou a página"
        else -> "Ferramenta ignorada"
    }

    fun rotuloEmAndamento(nome: String): String = when (nome) {
        ENDERECO -> "Lendo o endereço da aba…"
        TEXTO -> "Lendo o texto da página…"
        SELECAO -> "Lendo o texto selecionado…"
        ABAS -> "Listando as abas…"
        MIDIA -> "Lendo a mídia da página…"
        ENDERECOS -> "Lendo os endereços da página…"
        CAMPOS -> "Lendo os campos da página…"
        CAPTURAR_ABA -> "Capturando a aba…"
        TEXTO_PAGINADO -> "Lendo um bloco do texto da página…"
        ELEMENTOS -> "Consultando elementos da página…"
        ROLAR -> "Rolando a página…"
        else -> "Usando uma ferramenta…"
    }

    private fun definicao(nome: String, descricao: String): String {
        return """{"type":"function","function":{"name":"$nome","description":"$descricao","parameters":{"type":"object","properties":{}}}}"""
    }

    private fun definicaoComProps(nome: String, descricao: String, propriedades: String, obrigatorios: String): String {
        return """{"type":"function","function":{"name":"$nome","description":"$descricao","parameters":{"type":"object","properties":$propriedades,"required":$obrigatorios}}}"""
    }
}

```

## FILE: regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\ContextoDaPaginaParaOChat.kt
```
package com.chromebrothers.navegador.regras

object ContextoDaPaginaParaOChat {
    const val LIMITE = 6000

    /** Texto da página normalizado no JS, sem truncar (o corte fica no Kotlin). */
    fun scriptDoTextoCompletoNormalizado(): String = """
        (function(){
          var bruto = document.body ? (document.body.innerText || "") : "";
          return bruto.replace(/\s+/g, " ").trim();
        })()
    """.trimIndent()

    /** Mantido por compatibilidade: o script não trunca; use `take(LIMITE)` no Kotlin. */
    fun scriptDoTextoVisivel(): String = scriptDoTextoCompletoNormalizado()

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

```

## FILE: docs\superpowers\specs\2026-11-01-leitura-paginada-consultar-elementos-e-rolar-pagina-no-chat-design.md
```
# Leitura paginada, consultar elementos e rolar página no chat

Data: 2026-11-01  
Status: aprovado para plano

## Objetivo

Em páginas grandes, o agente deixa de depender só dos primeiros 6000 caracteres / 40 mídias / viewport do screenshot. Passa a **paginar texto**, **extrair nós por CSS** e **rolar a aba** (sem confirmação), para montar coleção/página HTML com dados reais.

## Abordagem

Três ferramentas novas de **leitura** (sem confirmação do usuário), na aba ativa. `ler_texto_visivel` permanece como atalho do início da página.

## Ferramentas

### `ler_texto_da_pagina`

- Args: `offset` (int ≥ 0, default 0), `limite` (int, default 6000, máx 6000).
- Fonte: `document.body.innerText` normalizado (espaços colapsados), igual ao espírito de `ler_texto_visivel`.
- Resposta textual para o modelo, em português, incluindo:
  - cabeçalho com título/endereço da aba (quando disponível no wiring, ou só o bloco se o helper for puro)
  - corpo do trecho
  - metadados explícitos: `total`, `offset`, `proximo_offset`, `tem_mais`
- Se `offset` ≥ total: corpo vazio, `tem_mais=false`, `total` informado.
- `ler_texto_visivel` continua existindo (equivalente prático a offset 0 / limite 6000 sem metadados de paginação obrigatórios).

### `consultar_elementos`

- Args: `seletor` (CSS, obrigatório, máx 300 caracteres), `limite` (default 40, máx 40), `offset` (default 0).
- Execução: `document.querySelectorAll(seletor)` — **sem** `eval`, Function, nem JS arbitrário.
- Por elemento (após aplicar offset/limite): tag; texto curto (máx ~200); `href`/`src` se existirem e forem `http`/`https`; attrs `alt`, `title`, `aria-label` quando úteis; filhos diretos relevantes `img` / `video` / `a` com URL http(s).
- Resposta: lista formatada + indicação de quantos matches totais e se há mais além do limite/offset.
- Erros: seletor vazio; seletor inválido (exceção do engine); nenhum match — mensagens curtas em pt-BR.

### `rolar_pagina`

- Sem confirmação (exploração, não muda URL).
- Args — **um** modo por chamada:
  - `pixels`: scroll relativo (delta Y)
  - `y`: scroll absoluto
  - `paginas`: múltiplos da altura do viewport (pode ser fracionário positivo/negativo via número)
  - `seletor`: `scrollIntoView` no primeiro match (mesmo limite de tamanho de seletor)
- Se nenhum modo válido ou vários conflitantes de forma ambígua: recusar com mensagem clara (preferência de interpretação: se mais de um presente, prioridade fixa `seletor` > `y` > `paginas` > `pixels`, documentada no schema/descrição).
- Resposta: `y` atual, `altura_pagina`, `altura_viewport`, `no_fim` (bool).

## Arquitetura

| Camada | Responsabilidade |
|--------|------------------|
| `regras-de-video-do-navegador` | Constantes, parse de args (`TextoJsonDoChat` + inteiros), scripts JS, formatação da resposta, validação de limites |
| `FerramentasDeLeituraDoChatDaOpenRouter` | Nomes, schema JSON com `properties`, `permitidas`, rótulos, trecho de `instrucao` |
| `PainelDoChatDaOpenRouterNoNavegador` | Dispatch das três tools no `when` de leitura (não passam por `AcoesConfirmadas`) |
| `AtividadePrincipalDoNavegadorChromebrothers` | Callbacks: `evaluateJavascript` na WebView da aba ativa; para scroll, ler posição após `scrollTo` / `scrollIntoView` |

Helpers sugeridos (nomes longos e descritivos):

- `LeituraPaginadaDoTextoDaPaginaParaOChat`
- `ConsultaDeElementosDaPaginaPorSeletorCssParaOChat`
- `RolagemDaPaginaDaAbaParaOChat`

## Instrução do sistema

Acrescentar orientação: se a página for grande ou o pedido for estruturado (listas, cards, midia+texto), usar `consultar_elementos`, `ler_texto_da_pagina` com offset e `rolar_pagina` quando o conteúdo for lazy-load; não afirmar que “não cabe / não vê tudo” sem esgotar paginação/rolagem/consulta.

## Segurança

- Apenas CSS selector + APIs DOM listadas.
- URLs só `http`/`https` nas extrações.
- Limites enforced no app.
- Scroll não dispara navegação nem clique.

## Testes (JVM)

- Paginação: fatias, `tem_mais`, offset além do fim.
- Elementos: parse/format, seletor vazio/inválido simulado via resultado do script, teto de limite.
- Scroll: escolha de modo pela prioridade, formatação, args ausentes.
- Schema: as três tools em `json()`; nomes em `permitidas`; rótulos.

## Fora de escopo

- Offset/filtro em `listar_midia_da_pagina` / `ler_enderecos_da_pagina`
- Captura full-page multimodal no chat
- Esperar rede / MutationObserver / JS livre
- Confirmação ou liberação para essas tools (sempre livres)

## Critério de sucesso

Pedido do tipo “liste os itens desta página longa com imagem/vídeo e publique na webview” deixa o agente capaz de: rolar se preciso → consultar cards → paginar texto se faltar contexto → montar/publicar, sem depender do usuário dizer “prossiga” só porque o primeiro bloco cortou.

```


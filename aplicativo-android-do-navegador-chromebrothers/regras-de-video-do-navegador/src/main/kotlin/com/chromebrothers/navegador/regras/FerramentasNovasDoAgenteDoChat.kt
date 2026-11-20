package com.chromebrothers.navegador.regras

/**
 * Ações na página pelo seletor CSS (o que consultar_elementos devolve): clicar, preencher
 * (inclusive select, checkbox e contenteditable) e apertar uma tecla.
 */
object ElementosDaPaginaPeloSeletorParaOChat {
    const val LIMITE_DO_SELETOR = 400
    const val LIMITE_DO_VALOR = 8000
    val teclas = listOf("Enter", "Escape", "Tab", "ArrowDown", "ArrowUp", "ArrowLeft", "ArrowRight", "Backspace", "Space")

    private fun texto(valor: String) = "\"" + TextoJsonDoChat.escapar(valor) + "\""

    private const val ACHAR = """
          var els; try { els = document.querySelectorAll(sel); } catch (e) { return "seletor-invalido"; }
          if (!els.length) return "sem-elemento";
          if (idx >= els.length) return "indice-fora:" + els.length;
          var el = els[idx];
          try { el.scrollIntoView({block: "center", inline: "center"}); } catch (e) {}
    """

    fun scriptDeClicar(seletor: String, indice: Int): String = """
        (function(sel, idx){
          $ACHAR
          var r = el.getBoundingClientRect();
          if (r.width === 0 && r.height === 0) return "invisivel";
          var o = {bubbles: true, cancelable: true, view: window, clientX: r.left + r.width / 2, clientY: r.top + r.height / 2};
          try { el.dispatchEvent(new PointerEvent("pointerdown", o)); } catch (e) {}
          el.dispatchEvent(new MouseEvent("mousedown", o));
          try { el.dispatchEvent(new PointerEvent("pointerup", o)); } catch (e) {}
          el.dispatchEvent(new MouseEvent("mouseup", o));
          if (typeof el.focus === "function") el.focus();
          el.click();
          return "ok:" + els.length;
        })(${texto(seletor)}, $indice)
    """.trimIndent()

    fun scriptDePreencher(seletor: String, valor: String, indice: Int): String = """
        (function(sel, idx, valor){
          $ACHAR
          var tag = el.tagName;
          function avisar(){ el.dispatchEvent(new Event("input", {bubbles: true})); el.dispatchEvent(new Event("change", {bubbles: true})); }
          if (tag === "SELECT") {
            var alvo = null;
            for (var i = 0; i < el.options.length; i++) { var op = el.options[i]; if (op.value === valor || op.text.trim() === valor) { alvo = op; break; } }
            if (!alvo) { var nomes = []; for (var j = 0; j < el.options.length && j < 30; j++) nomes.push(el.options[j].text.trim()); return "sem-opcao:" + nomes.join(" | "); }
            el.value = alvo.value; avisar(); return "ok-opcao";
          }
          if (tag === "INPUT" || tag === "TEXTAREA") {
            var tipo = (el.type || "").toLowerCase();
            if (tipo === "checkbox" || tipo === "radio") {
              var marcar = /^(1|true|sim|marcar|marcado|on)$/i.test(valor);
              if (el.checked !== marcar) el.click();
              return "ok-marcado:" + el.checked;
            }
            if (tipo === "file") return "arquivo";
            el.focus();
            var proto = tag === "INPUT" ? HTMLInputElement.prototype : HTMLTextAreaElement.prototype;
            Object.getOwnPropertyDescriptor(proto, "value").set.call(el, valor);
            avisar(); return "ok";
          }
          if (el.isContentEditable) {
            el.focus();
            document.execCommand("selectAll", false, null);
            if (!document.execCommand("insertText", false, valor)) { el.textContent = valor; el.dispatchEvent(new InputEvent("input", {bubbles: true})); }
            return "ok";
          }
          return "nao-editavel:" + tag.toLowerCase();
        })(${texto(seletor)}, $indice, ${texto(valor)})
    """.trimIndent()

    fun scriptDeTecla(tecla: String, seletor: String, indice: Int): String = """
        (function(sel, idx, tecla){
          var el = document.activeElement || document.body;
          if (sel) {
            $ACHAR
            if (typeof el.focus === "function") el.focus();
          }
          var mapa = {Enter: [13, "Enter"], Escape: [27, "Escape"], Tab: [9, "Tab"], ArrowDown: [40, "ArrowDown"], ArrowUp: [38, "ArrowUp"], ArrowLeft: [37, "ArrowLeft"], ArrowRight: [39, "ArrowRight"], Backspace: [8, "Backspace"], Space: [32, " "]};
          var m = mapa[tecla]; if (!m) return "tecla-invalida";
          var o = {key: m[1], code: tecla, keyCode: m[0], which: m[0], bubbles: true, cancelable: true};
          var seguiu = el.dispatchEvent(new KeyboardEvent("keydown", o));
          el.dispatchEvent(new KeyboardEvent("keypress", o));
          el.dispatchEvent(new KeyboardEvent("keyup", o));
          if (seguiu && tecla === "Enter" && el.tagName === "INPUT" && el.form) {
            if (el.form.requestSubmit) el.form.requestSubmit(); else el.form.submit();
            return "ok-formulario";
          }
          if (seguiu && tecla === "Tab") {
            var f = [].slice.call(document.querySelectorAll('a[href],button,input,select,textarea,[tabindex]:not([tabindex="-1"])'))
              .filter(function(e){ return !e.disabled && e.offsetParent !== null; });
            var i = f.indexOf(el); if (i >= 0 && f[i + 1]) f[i + 1].focus();
          }
          return "ok:" + (el.tagName || "").toLowerCase();
        })(${texto(seletor)}, $indice, ${texto(tecla)})
    """.trimIndent()

    fun relato(resultado: String, alvo: String): String = when {
        resultado == "ok-opcao" -> "Escolhi a opção em $alvo."
        resultado == "ok-formulario" -> "Apertei Enter em $alvo e o formulário foi enviado."
        resultado.startsWith("ok-marcado:") ->
            if (resultado.endsWith("true")) "Marquei $alvo." else "Desmarquei $alvo."
        resultado.startsWith("ok") -> {
            val total = resultado.substringAfter("ok:", "").toIntOrNull()
            if (total != null && total > 1) "Feito em $alvo (havia $total elementos com esse seletor; usei o do índice pedido)."
            else "Feito em $alvo."
        }
        resultado == "seletor-invalido" -> "O seletor $alvo é inválido."
        resultado == "sem-elemento" -> "Nenhum elemento com o seletor $alvo. Use consultar_elementos para achar o seletor certo."
        resultado.startsWith("indice-fora:") -> "Só há ${resultado.substringAfter(':')} elemento(s) com $alvo; o índice começa em 0."
        resultado == "invisivel" -> "O elemento $alvo está invisível (sem tamanho na tela)."
        resultado.startsWith("sem-opcao:") -> "Essa opção não existe em $alvo. Opções: ${resultado.substringAfter(':')}"
        resultado == "arquivo" -> "$alvo é um campo de arquivo; não consigo escolher arquivos por aqui."
        resultado.startsWith("nao-editavel:") -> "$alvo (${resultado.substringAfter(':')}) não é um campo editável."
        resultado == "tecla-invalida" -> "Tecla não suportada. Use: ${teclas.joinToString(", ")}."
        else -> "Não consegui agir em $alvo."
    }
}

/** Leitura de uma página numa WebView escondida, sem mexer na aba do usuário. */
object LeituraEmSegundoPlanoParaOChat {
    const val LIMITE_DO_BLOCO = 8000
    const val TEMPO_MAXIMO_MS = 25_000L
    const val ESPERA_DEPOIS_DE_CARREGAR_MS = 1_500L

    fun relato(titulo: String, endereco: String, texto: String, offset: Int, erro: String): String {
        if (erro.isNotBlank()) return "Não consegui ler a página em segundo plano: $erro"
        val inicio = offset.coerceIn(0, texto.length)
        val fim = (inicio + LIMITE_DO_BLOCO).coerceAtMost(texto.length)
        val cabecalho = buildString {
            append("Página lida em segundo plano (a aba do usuário não mudou): ${titulo.ifBlank { "sem título" }}\n")
            append("Endereço final: $endereco\n")
            if (texto.isBlank()) {
                append("A página não tem texto visível (pode depender de login ou de interação).")
                return@buildString
            }
            append("Caracteres $inicio–$fim de ${texto.length}.")
            if (fim < texto.length) append(" Para continuar, chame de novo com offset=$fim.")
        }
        return if (texto.isBlank()) cabecalho else "$cabecalho\n\n${texto.substring(inicio, fim)}"
    }
}

data class TrocaDeTrechoDoHtml(val de: String, val para: String)

sealed class ResultadoDaEdicaoPorTrechos {
    data class Ok(val html: String, val quantas: Int) : ResultadoDaEdicaoPorTrechos()
    data class Recusada(val mensagem: String) : ResultadoDaEdicaoPorTrechos()
}

/** Edita o HTML trocando trechos exatos, sem reenviar a página inteira. Tudo ou nada. */
object EdicaoPorTrechosDoHtml {
    const val LIMITE_DE_TROCAS = 30

    fun ler(argumentos: String): List<TrocaDeTrechoDoHtml> =
        TextoJsonDoChat.objetosDaChave(argumentos, "trocas").orEmpty().map { objeto ->
            TrocaDeTrechoDoHtml(
                de = TextoJsonDoChat.valorDaChave(objeto, "de").orEmpty(),
                para = TextoJsonDoChat.valorDaChave(objeto, "para").orEmpty(),
            )
        }

    fun aplicar(html: String, trocas: List<TrocaDeTrechoDoHtml>): ResultadoDaEdicaoPorTrechos {
        if (trocas.isEmpty()) return ResultadoDaEdicaoPorTrechos.Recusada("Faltaram as trocas.")
        if (trocas.size > LIMITE_DE_TROCAS) {
            return ResultadoDaEdicaoPorTrechos.Recusada("No máximo $LIMITE_DE_TROCAS trocas por vez.")
        }
        var atual = html
        trocas.forEachIndexed { i, troca ->
            val n = i + 1
            if (troca.de.isEmpty()) return ResultadoDaEdicaoPorTrechos.Recusada("A troca $n está sem o trecho \"de\".")
            val vezes = ocorrencias(atual, troca.de)
            if (vezes == 0) {
                return ResultadoDaEdicaoPorTrechos.Recusada(
                    "Troca $n: o trecho não existe no HTML atual (copie exatamente, com espaços). Nada foi alterado.",
                )
            }
            if (vezes > 1) {
                return ResultadoDaEdicaoPorTrechos.Recusada(
                    "Troca $n: o trecho aparece $vezes vezes; inclua mais contexto para ficar único. Nada foi alterado.",
                )
            }
            atual = atual.replace(troca.de, troca.para)
        }
        return ResultadoDaEdicaoPorTrechos.Ok(atual, trocas.size)
    }

    private fun ocorrencias(texto: String, trecho: String): Int {
        var conta = 0
        var de = texto.indexOf(trecho)
        while (de >= 0) {
            conta++
            de = texto.indexOf(trecho, de + trecho.length)
        }
        return conta
    }
}

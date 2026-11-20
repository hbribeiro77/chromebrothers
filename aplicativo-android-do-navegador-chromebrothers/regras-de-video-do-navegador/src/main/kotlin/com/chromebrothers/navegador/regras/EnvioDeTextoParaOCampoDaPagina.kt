package com.chromebrothers.navegador.regras

object EnvioDeTextoParaOCampoDaPagina {
    private const val COR_DO_CAMPO = "#1A73E8"
    private const val COR_DO_ENVIO = "#F9AB00"

    fun instalador(): String = """
        (function () {
          function pintar(elemento, cor) {
            if (!elemento || !elemento.style) return;
            elemento.style.outline = "2px solid " + cor;
            elemento.style.outlineOffset = "2px";
          }
          function limparPintura(elemento) {
            if (!elemento || !elemento.style) return;
            elemento.style.outline = "";
            elemento.style.outlineOffset = "";
          }
          function campoEditavel(alvo) {
            var no = alvo;
            while (no && no !== document) {
              if (no.nodeType === 1) {
                var edicao = no.getAttribute && no.getAttribute("contenteditable");
                if (edicao === "" || edicao === "true" || edicao === "plaintext-only") return no;
                var papel = no.getAttribute && no.getAttribute("role");
                if (papel === "textbox") return no;
                if (no.tagName === "TEXTAREA") return no;
                if (no.tagName === "INPUT") {
                  var tipo = (no.type || "text").toLowerCase();
                  var bloqueado = ["button", "checkbox", "file", "hidden", "radio", "submit", "reset", "image", "range", "color"];
                  if (bloqueado.indexOf(tipo) < 0) return no;
                }
              }
              no = no.parentNode;
            }
            return null;
          }
          function alvoDoEnvio(evento) {
            var caminho = evento.composedPath ? evento.composedPath() : [evento.target];
            var indice;
            var no;
            var botao;
            var tag;
            for (indice = 0; indice < caminho.length; indice++) {
              no = caminho[indice];
              if (!no || no.nodeType !== 1) continue;
              botao = botaoDeEnvio(no);
              if (botao) return botao;
            }
            for (indice = 0; indice < caminho.length && indice < 6; indice++) {
              no = caminho[indice];
              if (!no || no.nodeType !== 1) continue;
              if (no === document.body || no === document.documentElement) break;
              tag = no.tagName;
              if (tag === "PATH" || tag === "SVG" || tag === "CIRCLE" || tag === "RECT" || tag === "G" || tag === "USE" || tag === "SPAN") continue;
              if (campoEditavel(no) === no) return null;
              return no;
            }
            return null;
          }
          function botaoDeEnvio(alvo) {
            var no = alvo;
            while (no && no !== document) {
              if (no.nodeType === 1) {
                var papel = no.getAttribute && no.getAttribute("role");
                if (papel === "button") return no;
                if (no.tagName === "BUTTON") return no;
                if (no.tagName === "INPUT") {
                  var tipo = (no.type || "").toLowerCase();
                  if (tipo === "submit" || tipo === "button") return no;
                }
              }
              no = no.parentNode;
            }
            return null;
          }
          function rotuloEstavel(elemento) {
            return elemento.getAttribute("placeholder") || elemento.getAttribute("aria-label") || elemento.getAttribute("aria-placeholder") || elemento.getAttribute("title") || "";
          }
          function rotuloDeBotao(elemento) {
            var estavel = rotuloEstavel(elemento);
            if (estavel) return estavel;
            var escrito = elemento.innerText || elemento.value || "";
            return escrito.replace(/\s+/g, " ").trim();
          }
          function avisar(tipo, elemento) {
            if (!window.ChromeBrothersCampo || !ChromeBrothersCampo.vinculado) return;
            var rotulo = tipo === "campo" ? rotuloEstavel(elemento) : rotuloDeBotao(elemento);
            rotulo = rotulo.replace(/\s+/g, " ").trim();
            if (rotulo.length > 40) rotulo = rotulo.substring(0, 40);
            ChromeBrothersCampo.vinculado(tipo, elemento.id || "", elemento.getAttribute("name") || "", rotulo, elemento.tagName || "", elemento.getAttribute("role") || "");
          }
          if (!window.__cbCampoDeTextoInstalado) {
            window.__cbCampoDeTextoInstalado = true;
            document.addEventListener("focusin", function (evento) {
              if (window.__cbAguardandoVinculo !== "campo") return;
              var campo = campoEditavel(evento.target);
              if (!campo) return;
              window.__cbAguardandoVinculo = "";
              limparPintura(window.__cbCampoDeTexto);
              window.__cbCampoDeTexto = campo;
              pintar(campo, "$COR_DO_CAMPO");
              avisar("campo", campo);
            }, true);
            document.addEventListener("pointerdown", function (evento) {
              if (window.__cbAguardandoVinculo !== "envio") return;
              var botao = alvoDoEnvio(evento);
              if (!botao) return;
              evento.preventDefault();
              evento.stopPropagation();
              window.__cbAguardandoVinculo = "";
              limparPintura(window.__cbBotaoDeEnvio);
              window.__cbBotaoDeEnvio = botao;
              pintar(botao, "$COR_DO_ENVIO");
              avisar("envio", botao);
            }, true);
          }
          window.__cbPintarCampo = function (elemento) { pintar(elemento, "$COR_DO_CAMPO"); };
          window.__cbPintarEnvio = function (elemento) { pintar(elemento, "$COR_DO_ENVIO"); };
          window.__cbLimparPintura = limparPintura;
        })();
    """.trimIndent()

    fun armarCampo(): String = """
        (function () {
          window.__cbAguardandoVinculo = "campo";
          return "aguardando-campo";
        })();
    """.trimIndent()

    fun armarEnvio(): String = """
        (function () {
          window.__cbAguardandoVinculo = "envio";
          return "aguardando-envio";
        })();
    """.trimIndent()

    fun cancelarVinculo(): String = """
        (function () {
          window.__cbAguardandoVinculo = "";
          return "nenhum";
        })();
    """.trimIndent()

    fun estado(): String = """
        (function () {
          if (window.__cbAguardandoVinculo === "campo") return "aguardando-campo";
          if (window.__cbAguardandoVinculo === "envio") return "aguardando-envio";
          var campo = window.__cbCampoDeTexto;
          var envio = window.__cbBotaoDeEnvio;
          if (campo && !campo.isConnected) {
            if (window.__cbLimparPintura) window.__cbLimparPintura(campo);
            window.__cbCampoDeTexto = null;
            campo = null;
          }
          if (envio && !envio.isConnected) {
            if (window.__cbLimparPintura) window.__cbLimparPintura(envio);
            window.__cbBotaoDeEnvio = null;
            envio = null;
          }
          if (campo && envio) return "ambos";
          if (campo) return "campo";
          if (envio) return "envio";
          return "nenhum";
        })();
    """.trimIndent()

    fun idConfiavel(id: String): String {
        val limpo = id.trim()
        if (limpo.isEmpty() || limpo.length > 48 || limpo.contains(':')) return ""
        if (limpo.all { it.isDigit() }) return ""
        if (Regex("ember\\d+", RegexOption.IGNORE_CASE).matches(limpo)) return ""
        if (Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}").matches(limpo)) return ""
        if (Regex(".*\\d{10,}.*").matches(limpo)) return ""
        return limpo
    }

    fun reaplicar(campo: MarcaDoElementoVinculado?, envio: MarcaDoElementoVinculado?): String = """
        (function () {
          function visivel(item) {
            if (!item || item.disabled) return false;
            if (item.getAttribute("aria-hidden") === "true" || item.hasAttribute("hidden")) return false;
            var estilo = window.getComputedStyle(item);
            if (estilo.display === "none" || estilo.visibility === "hidden") return false;
            var caixa = item.getBoundingClientRect();
            return caixa.width > 0 && caixa.height > 0;
          }
          function textoEstavel(item) {
            return item.getAttribute("placeholder") || item.getAttribute("aria-label") || item.getAttribute("aria-placeholder") || item.getAttribute("title") || "";
          }
          function textoDeBotao(item) {
            var estavel = textoEstavel(item);
            if (estavel) return estavel;
            var escrito = item.innerText || item.value || "";
            return escrito.replace(/\s+/g, " ").trim();
          }
          function candidatos(tag, papel, modo) {
            var origem;
            var lista = [];
            var indice;
            var item;
            var tipo;
            var bloqueado = ["button", "checkbox", "file", "hidden", "radio", "submit", "reset", "image", "range", "color"];
            if (modo === "envio" || tag === "BUTTON" || papel === "button") {
              origem = document.querySelectorAll("button, input[type='submit'], input[type='button'], [role='button']");
            } else if (tag === "TEXTAREA") {
              origem = document.getElementsByTagName("textarea");
            } else if (tag === "INPUT") {
              origem = document.getElementsByTagName("input");
            } else {
              origem = document.querySelectorAll("[contenteditable], [role='textbox']");
            }
            for (indice = 0; indice < origem.length; indice++) {
              item = origem[indice];
              if (tag === "INPUT" && modo !== "envio") {
                tipo = (item.type || "text").toLowerCase();
                if (bloqueado.indexOf(tipo) >= 0) continue;
              }
              if (modo !== "envio" && tag !== "TEXTAREA" && tag !== "INPUT") {
                var edicao = item.getAttribute("contenteditable");
                var papelItem = item.getAttribute("role");
                if (edicao !== "" && edicao !== "true" && edicao !== "plaintext-only" && papelItem !== "textbox") continue;
              }
              if (!visivel(item)) continue;
              lista.push(item);
            }
            return lista;
          }
          function achar(id, nome, rotulo, tag, papel, modo) {
            if (!tag) return null;
            var lista = candidatos(tag, papel, modo);
            var indice;
            var item;
            var texto;
            var ehBotao = modo === "envio";
            if (id) {
              var porId = document.getElementById(id);
              if (porId && porId.tagName && porId.tagName.toUpperCase() === String(tag).toUpperCase() && visivel(porId)) return porId;
            }
            if (nome) {
              for (indice = 0; indice < lista.length; indice++) {
                item = lista[indice];
                if (item.getAttribute && item.getAttribute("name") === nome) return item;
              }
            }
            if (rotulo) {
              for (indice = 0; indice < lista.length; indice++) {
                item = lista[indice];
                texto = ehBotao ? textoDeBotao(item) : textoEstavel(item);
                texto = (texto || "").replace(/\s+/g, " ").trim();
                if (texto && (texto.indexOf(rotulo) === 0 || rotulo.indexOf(texto) === 0)) return item;
              }
            }
            return lista.length === 1 ? lista[0] : null;
          }
          if (window.__cbLimparPintura) {
            window.__cbLimparPintura(window.__cbCampoDeTexto);
            window.__cbLimparPintura(window.__cbBotaoDeEnvio);
          }
          var campo = achar(${literal(campo?.id.orEmpty())}, ${literal(campo?.nome.orEmpty())}, ${literal(campo?.rotulo.orEmpty())}, ${literal(campo?.tag.orEmpty())}, ${literal(campo?.papel.orEmpty())}, "campo");
          var envio = achar(${literal(envio?.id.orEmpty())}, ${literal(envio?.nome.orEmpty())}, ${literal(envio?.rotulo.orEmpty())}, ${literal(envio?.tag.orEmpty())}, ${literal(envio?.papel.orEmpty())}, "envio");
          window.__cbCampoDeTexto = campo;
          window.__cbBotaoDeEnvio = envio;
          if (campo && window.__cbPintarCampo) window.__cbPintarCampo(campo);
          if (envio && window.__cbPintarEnvio) window.__cbPintarEnvio(envio);
          if (campo && envio) return "ambos";
          if (campo) return "campo";
          if (envio) return "envio";
          return "nenhum";
        })();
    """.trimIndent()

    fun soltar(tipo: String): String = """
        (function () {
          if (${literal(tipo)} === "envio") {
            if (window.__cbLimparPintura) window.__cbLimparPintura(window.__cbBotaoDeEnvio);
            window.__cbBotaoDeEnvio = null;
          } else {
            if (window.__cbLimparPintura) window.__cbLimparPintura(window.__cbCampoDeTexto);
            window.__cbCampoDeTexto = null;
          }
          return "nenhum";
        })();
    """.trimIndent()

    fun espelhar(texto: String): String = """
        (function () {
          ${jsDeColocarTexto()}
          var campo = window.__cbCampoDeTexto;
          if (!campo || !campo.isConnected) return "sem-campo";
          var texto = ${literal(texto)};
          try {
            return colocarTexto(campo, texto);
          } catch (erro) {
            return "falhou";
          }
        })();
    """.trimIndent()

    fun ler(): String = """
        (function () {
          var campo = window.__cbCampoDeTexto;
          if (!campo || !campo.isConnected) return "sem-campo";
          var editavel = campo.isContentEditable || campo.getAttribute("contenteditable");
          return editavel ? (campo.innerText || "") : (campo.value || "");
        })();
    """.trimIndent()

    fun escrever(texto: String): String = """
        (function () {
          ${jsDeColocarTexto()}
          function acionar(botao) {
            var vezes = 0;
            function tentar() {
              if (!botao || !botao.isConnected) return;
              var bloqueado = botao.disabled || botao.getAttribute("aria-disabled") === "true";
              if (bloqueado && vezes < 5) {
                vezes++;
                setTimeout(tentar, 70);
                return;
              }
              var retangulo = botao.getBoundingClientRect();
              var opcoes = { bubbles: true, cancelable: true, composed: true, view: window, clientX: retangulo.left + retangulo.width / 2, clientY: retangulo.top + retangulo.height / 2, button: 0, buttons: 1 };
              if (window.PointerEvent) {
                botao.dispatchEvent(new PointerEvent("pointerdown", opcoes));
                botao.dispatchEvent(new PointerEvent("pointerup", opcoes));
              }
              botao.dispatchEvent(new MouseEvent("mousedown", opcoes));
              botao.dispatchEvent(new MouseEvent("mouseup", opcoes));
              botao.click();
            }
            setTimeout(tentar, 80);
          }
          function teclarEnter(campo) {
            setTimeout(function () {
              if (!campo || !campo.isConnected) return;
              var opcoes = { key: "Enter", code: "Enter", keyCode: 13, which: 13, bubbles: true, cancelable: true };
              campo.dispatchEvent(new KeyboardEvent("keydown", opcoes));
              campo.dispatchEvent(new KeyboardEvent("keypress", opcoes));
              campo.dispatchEvent(new KeyboardEvent("keyup", opcoes));
            }, 80);
          }
          var campo = window.__cbCampoDeTexto;
          if (!campo || !campo.isConnected) return "sem-campo";
          var texto = ${literal(texto)};
          try {
            var resultado = colocarTexto(campo, texto);
            var botao = window.__cbBotaoDeEnvio;
            if (botao && botao.isConnected) acionar(botao);
            else teclarEnter(campo);
            return resultado === "sem-campo" ? resultado : "ok";
          } catch (erro) {
            return "falhou";
          }
        })();
    """.trimIndent()

    private fun jsDeColocarTexto(): String = """
        function normalizarTexto(valor) {
          return String(valor || "").replace(/\r\n/g, "\n").replace(/\r/g, "\n").replace(/\u00a0/g, " ");
        }
        function textoDoCampo(campo) {
          if (campo.tagName === "INPUT" || campo.tagName === "TEXTAREA") return campo.value || "";
          return campo.innerText || campo.textContent || "";
        }
        function textoConferido(valor) {
          return normalizarTexto(valor).replace(/\n+$/g, "");
        }
        function jaEsta(campo, esperado) {
          return textoConferido(textoDoCampo(campo)) === textoConferido(esperado);
        }
        function selecionarConteudo(campo) {
          if (document.activeElement !== campo) campo.focus();
          var raiz = campo.getRootNode ? campo.getRootNode() : document;
          var selecao = raiz.getSelection ? raiz.getSelection() : window.getSelection();
          var faixa = document.createRange();
          faixa.selectNodeContents(campo);
          if (!selecao) return;
          selecao.removeAllRanges();
          selecao.addRange(faixa);
        }
        function colocarTexto(campo, texto) {
          var esperado = normalizarTexto(texto);
          if (jaEsta(campo, esperado)) return "igual";
          var editavel = campo.isContentEditable || (campo.getAttribute && (campo.getAttribute("contenteditable") === "" || campo.getAttribute("contenteditable") === "true" || campo.getAttribute("contenteditable") === "plaintext-only" || campo.getAttribute("role") === "textbox"));
          if (editavel && campo.tagName !== "INPUT" && campo.tagName !== "TEXTAREA") {
            selecionarConteudo(campo);
            if (esperado === "") {
              var apagar = new InputEvent("beforeinput", { bubbles: true, cancelable: true, inputType: "deleteContent", data: null });
              if (campo.dispatchEvent(apagar)) {
                document.execCommand("delete", false, null);
                campo.textContent = "";
              }
              if (!jaEsta(campo, "")) campo.textContent = "";
              campo.dispatchEvent(new InputEvent("input", { bubbles: true, inputType: "deleteContent", data: null }));
            } else {
              var antes = new InputEvent("beforeinput", { bubbles: true, cancelable: true, inputType: "insertText", data: esperado });
              if (campo.dispatchEvent(antes)) {
                var trecho = esperado.substring(0, Math.min(esperado.length, 40));
                if (!jaEsta(campo, esperado)) document.execCommand("insertText", false, esperado);
                if (!jaEsta(campo, esperado)) {
                  var html = esperado.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\n/g, "<br>");
                  campo.textContent = "";
                  selecionarConteudo(campo);
                  document.execCommand("insertHTML", false, html);
                }
                if (normalizarTexto(textoDoCampo(campo)).indexOf(trecho) < 0) {
                  campo.dispatchEvent(new InputEvent("input", { bubbles: true, inputType: "insertFromPaste", data: texto }));
                }
              }
            }
            setTimeout(function () { if (document.activeElement === campo) campo.blur(); }, 30);
            return "ok";
          }
          var anterior = campo.value || "";
          var tracker = campo._valueTracker;
          if (tracker && tracker.setValue) tracker.setValue(anterior === texto ? "\u0000" : anterior);
          var proto = campo instanceof HTMLTextAreaElement ? HTMLTextAreaElement.prototype : HTMLInputElement.prototype;
          var descricao = Object.getOwnPropertyDescriptor(proto, "value");
          if (descricao && descricao.set) descricao.set.call(campo, texto);
          else campo.value = texto;
          campo.dispatchEvent(new InputEvent("input", { bubbles: true, inputType: esperado === "" ? "deleteContent" : "insertReplacementText", data: esperado === "" ? null : texto }));
          return "ok";
        }
    """.trimIndent()

    private fun literal(texto: String) = buildString {
        append('"')
        texto.forEach { caractere ->
            when (caractere) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\u2028' -> append("\\u2028")
                '\u2029' -> append("\\u2029")
                else -> if (caractere.code < 32) append("\\u%04x".format(caractere.code)) else append(caractere)
            }
        }
        append('"')
    }
}

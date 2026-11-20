package com.chromebrothers.navegador.regras

/**
 * Localiza um editor na página (genérico ou heurística Google Docs), foca e
 * insere texto — sem depender do campo vinculado nem do foco do chat.
 */
object EscritaDeTextoNoEditorDaPaginaParaOChat {
    const val OK = "ok"
    const val SEM_EDITOR = "sem-editor"
    const val FALHOU = "falhou"
    const val SEM_PAGINA = "sem-pagina"

    fun relato(codigo: String): String = when (codigo) {
        OK -> "Escrevi o texto no editor da página."
        SEM_EDITOR ->
            "Não achei um editor digitável nesta página. " +
                "No Google Docs isso às vezes não aparece para o navegador."
        FALHOU -> "Achei um editor, mas ele não aceitou o texto."
        SEM_PAGINA -> "Não há página aberta."
        else -> "Não consegui escrever na página."
    }

    fun script(texto: String): String = """
        (function () {
          var texto = ${literal(texto)};
          function editavel(no) {
            if (!no || no.nodeType !== 1) return false;
            if (no.tagName === "TEXTAREA") return !no.disabled && !no.readOnly;
            if (no.tagName === "INPUT") {
              var tipo = (no.type || "text").toLowerCase();
              if (tipo === "hidden" || tipo === "button" || tipo === "submit" || tipo === "checkbox" || tipo === "radio" || tipo === "file") return false;
              return !no.disabled && !no.readOnly;
            }
            var ce = no.getAttribute && no.getAttribute("contenteditable");
            if (ce === "" || ce === "true" || ce === "plaintext-only") return true;
            if (no.isContentEditable) return true;
            var papel = (no.getAttribute && no.getAttribute("role") || "").toLowerCase();
            return papel === "textbox";
          }
          function visivel(no) {
            if (!no || !no.getBoundingClientRect) return false;
            var r = no.getBoundingClientRect();
            if (r.width < 2 && r.height < 2) return true;
            return r.width > 0 && r.height > 0;
          }
          function docDoFrame(frame) {
            try { return frame.contentDocument || (frame.contentWindow && frame.contentWindow.document); }
            catch (e) { return null; }
          }
          function candidatosNoDocumento(doc) {
            var lista = [];
            if (!doc) return lista;
            var sel = "textarea, input, [contenteditable=''], [contenteditable=true], [contenteditable=plaintext-only], [role='textbox'], .ProseMirror, #prompt-textarea";
            try {
              doc.querySelectorAll(sel).forEach(function (el) {
                if (editavel(el)) lista.push(el);
              });
            } catch (e) {}
            return lista;
          }
          function heuristicaDocs(raiz) {
            var achados = [];
            var frames = raiz.querySelectorAll("iframe.docs-texteventtarget-iframe, iframe[class*='textevent'], iframe[title*='editor'], iframe[title*='Editor']");
            for (var i = 0; i < frames.length; i++) {
              var d = docDoFrame(frames[i]);
              if (!d) continue;
              var corpo = d.body;
              if (editavel(corpo)) achados.push(corpo);
              candidatosNoDocumento(d).forEach(function (el) { achados.push(el); });
            }
            var kix = raiz.querySelector(".kix-appview-editor, .docs-editor-container, [aria-label*='Document'], [aria-label*='Documento']");
            if (kix) achados.push(kix);
            return achados;
          }
          function clicarParaFocar(alvo) {
            try {
              var r = alvo.getBoundingClientRect ? alvo.getBoundingClientRect() : { left: 0, top: 0, width: 1, height: 1 };
              var x = r.left + Math.min(20, Math.max(1, r.width / 2));
              var y = r.top + Math.min(20, Math.max(1, r.height / 2));
              var op = { bubbles: true, cancelable: true, composed: true, view: window, clientX: x, clientY: y, button: 0, buttons: 1 };
              if (window.PointerEvent) {
                alvo.dispatchEvent(new PointerEvent("pointerdown", op));
                alvo.dispatchEvent(new PointerEvent("pointerup", op));
              }
              alvo.dispatchEvent(new MouseEvent("mousedown", op));
              alvo.dispatchEvent(new MouseEvent("mouseup", op));
              alvo.dispatchEvent(new MouseEvent("click", op));
            } catch (e) {}
          }
          function moverCaretAoFim(campo) {
            try {
              if (campo.tagName === "INPUT" || campo.tagName === "TEXTAREA") {
                var n = (campo.value || "").length;
                campo.setSelectionRange(n, n);
                return;
              }
              var raiz = campo.getRootNode ? campo.getRootNode() : document;
              var sel = raiz.getSelection ? raiz.getSelection() : window.getSelection();
              if (!sel) return;
              var faixa = document.createRange();
              faixa.selectNodeContents(campo);
              faixa.collapse(false);
              sel.removeAllRanges();
              sel.addRange(faixa);
            } catch (e) {}
          }
          function inserir(campo, valor) {
            try {
              if (document.activeElement !== campo) {
                try { campo.focus(); } catch (e1) {}
              }
              moverCaretAoFim(campo);
              var antes = new InputEvent("beforeinput", { bubbles: true, cancelable: true, inputType: "insertText", data: valor });
              if (campo.dispatchEvent(antes)) {
                var okCmd = false;
                try { okCmd = document.execCommand("insertText", false, valor); } catch (e2) {}
                if (!okCmd && campo.tagName !== "INPUT" && campo.tagName !== "TEXTAREA") {
                  try {
                    var html = valor.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\n/g, "<br>");
                    document.execCommand("insertHTML", false, html);
                  } catch (e3) {}
                }
                if (campo.tagName === "INPUT" || campo.tagName === "TEXTAREA") {
                  var anterior = campo.value || "";
                  var tracker = campo._valueTracker;
                  if (tracker && tracker.setValue) tracker.setValue(anterior);
                  var proto = campo instanceof HTMLTextAreaElement ? HTMLTextAreaElement.prototype : HTMLInputElement.prototype;
                  var desc = Object.getOwnPropertyDescriptor(proto, "value");
                  var novo = anterior + valor;
                  if (desc && desc.set) desc.set.call(campo, novo);
                  else campo.value = novo;
                }
                campo.dispatchEvent(new InputEvent("input", { bubbles: true, inputType: "insertText", data: valor }));
              }
              for (var i = 0; i < Math.min(valor.length, 4); i++) {
                var ch = valor.charAt(i);
                var keyOp = { key: ch, code: "Key" + ch.toUpperCase(), bubbles: true, cancelable: true, composed: true };
                campo.dispatchEvent(new KeyboardEvent("keydown", keyOp));
                campo.dispatchEvent(new KeyboardEvent("keypress", keyOp));
                campo.dispatchEvent(new KeyboardEvent("keyup", keyOp));
              }
              return true;
            } catch (e) {
              return false;
            }
          }
          function pontuar(el) {
            var p = 0;
            var ce = el.getAttribute && el.getAttribute("contenteditable");
            if (ce === "true" || ce === "" || el.isContentEditable) p += 5;
            if ((el.getAttribute && el.getAttribute("role") || "").toLowerCase() === "textbox") p += 4;
            if (el.tagName === "TEXTAREA") p += 3;
            if (el.tagName === "INPUT") p += 1;
            if (visivel(el)) p += 2;
            var aria = ((el.getAttribute && el.getAttribute("aria-label") || "") + " " + (el.className || "")).toLowerCase();
            if (aria.indexOf("docs") >= 0 || aria.indexOf("kix") >= 0 || aria.indexOf("document") >= 0) p += 6;
            if (aria.indexOf("prosemirror") >= 0 || (el.classList && el.classList.contains("ProseMirror"))) p += 12;
            if (aria.indexOf("pergunte") >= 0 || aria.indexOf("ask chatgpt") >= 0) p += 10;
            return p;
          }
          var lista = [];
          heuristicaDocs(document).forEach(function (el) { lista.push(el); });
          candidatosNoDocumento(document).forEach(function (el) { lista.push(el); });
          document.querySelectorAll("iframe").forEach(function (frame) {
            var d = docDoFrame(frame);
            candidatosNoDocumento(d).forEach(function (el) { lista.push(el); });
          });
          var unicos = [];
          lista.forEach(function (el) {
            if (unicos.indexOf(el) < 0) unicos.push(el);
          });
          unicos.sort(function (a, b) { return pontuar(b) - pontuar(a); });
          if (unicos.length === 0) {
            var canvas = document.querySelector(".kix-appview-editor, .docs-editor, canvas");
            if (canvas) {
              clicarParaFocar(canvas);
              var ativo = document.activeElement;
              if (editavel(ativo)) unicos.push(ativo);
            }
          }
          if (unicos.length === 0) return "$SEM_EDITOR";
          for (var j = 0; j < unicos.length; j++) {
            var alvo = unicos[j];
            clicarParaFocar(alvo);
            try { alvo.focus(); } catch (e4) {}
            if (inserir(alvo, texto)) return "$OK";
          }
          return "$FALHOU";
        })();
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

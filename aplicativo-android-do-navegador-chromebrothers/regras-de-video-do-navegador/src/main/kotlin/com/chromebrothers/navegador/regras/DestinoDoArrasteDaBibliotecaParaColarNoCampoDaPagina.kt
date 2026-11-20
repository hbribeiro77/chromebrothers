package com.chromebrothers.navegador.regras

/**
 * Decide se o fantasma da biblioteca deve ficar verde (soltar cola/cola referência)
 * e gera o JS que acha o campo editável mais próximo do dedo na página.
 *
 * Coordenadas: pixels locais da [GeckoView] + tamanho da view; o JS converte para
 * CSS via `innerWidth/altura` (ChatGPT desktop ≠ só density do Android).
 */
object DestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina {
    const val OK: String = "ok"
    const val SEM_ALVO: String = "sem-alvo"
    /** Raio em CSS px — ChatGPT tem composer baixo; precisa folga. */
    const val RAIO_CSS_PADRAO: Double = 220.0

    fun corIndicaPodeSoltar(
        sobreGeradorMcp: Boolean,
        editavelProximoNaPagina: Boolean,
    ): Boolean = sobreGeradorMcp || editavelProximoNaPagina

    /**
     * Colar na página só com o “+” verde (editável perto).
     * Soltar amarelo mesmo em cima da página: só some o fantasma.
     */
    fun deveColarNaPaginaAoSoltar(
        editavelProximoNaPagina: Boolean,
        pontoSobrePagina: Boolean = false,
    ): Boolean {
        // pontoSobrePagina ignorado de propósito — não cola sem verde.
        return editavelProximoNaPagina
    }

    fun ehResultadoOk(bruto: String?): Boolean {
        val limpo = bruto.orEmpty().trim().removeSurrounding("\"")
        return limpo.equals(OK, ignoreCase = true)
    }

    fun scriptParaSondar(
        xLocalNaView: Double,
        yLocalNaView: Double,
        larguraDaViewPx: Double,
        alturaDaViewPx: Double,
        raioCss: Double = RAIO_CSS_PADRAO,
    ): String = script(
        xLocalNaView = xLocalNaView,
        yLocalNaView = yLocalNaView,
        larguraDaViewPx = larguraDaViewPx,
        alturaDaViewPx = alturaDaViewPx,
        raioCss = raioCss,
        focar = false,
    )

    fun scriptParaFocar(
        xLocalNaView: Double,
        yLocalNaView: Double,
        larguraDaViewPx: Double,
        alturaDaViewPx: Double,
        raioCss: Double = RAIO_CSS_PADRAO,
    ): String = script(
        xLocalNaView = xLocalNaView,
        yLocalNaView = yLocalNaView,
        larguraDaViewPx = larguraDaViewPx,
        alturaDaViewPx = alturaDaViewPx,
        raioCss = raioCss,
        focar = true,
    )

    private fun script(
        xLocalNaView: Double,
        yLocalNaView: Double,
        larguraDaViewPx: Double,
        alturaDaViewPx: Double,
        raioCss: Double,
        focar: Boolean,
    ): String {
        val xL = xLocalNaView.toString()
        val yL = yLocalNaView.toString()
        val wV = larguraDaViewPx.coerceAtLeast(1.0).toString()
        val hV = alturaDaViewPx.coerceAtLeast(1.0).toString()
        val raio = raioCss.toString()
        val blocoFoco = if (focar) {
            """
              function aplicarFoco(alvo) {
                if (!alvo) return;
                try {
                  var r = alvo.getBoundingClientRect ? alvo.getBoundingClientRect() : { left: x, top: y, width: 1, height: 1 };
                  var cx = (x >= r.left && x <= r.right) ? x : (r.left + Math.min(24, Math.max(1, r.width / 2)));
                  var cy = (y >= r.top && y <= r.bottom) ? y : (r.top + Math.min(24, Math.max(1, r.height / 2)));
                  var op = { bubbles: true, cancelable: true, composed: true, view: window, clientX: cx, clientY: cy, button: 0, buttons: 1 };
                  try { alvo.focus({ preventScroll: true }); } catch (e0) { try { alvo.focus(); } catch (e0b) {} }
                  if (window.PointerEvent) {
                    alvo.dispatchEvent(new PointerEvent("pointerdown", op));
                    alvo.dispatchEvent(new PointerEvent("pointerup", op));
                  }
                  alvo.dispatchEvent(new MouseEvent("mousedown", op));
                  alvo.dispatchEvent(new MouseEvent("mouseup", op));
                  alvo.dispatchEvent(new MouseEvent("click", op));
                  try { alvo.focus({ preventScroll: true }); } catch (e2) { try { alvo.focus(); } catch (e2b) {} }
                } catch (e1) {}
              }
            """.trimIndent()
        } else {
            "function aplicarFoco(alvo) { }"
        }
        return """
            (function () {
              var xLocal = $xL;
              var yLocal = $yL;
              var viewW = $wV;
              var viewH = $hV;
              var cssW = window.innerWidth || document.documentElement.clientWidth || viewW;
              var cssH = window.innerHeight || document.documentElement.clientHeight || viewH;
              var x = xLocal * (cssW / viewW);
              var y = yLocal * (cssH / viewH);
              var raio = $raio;
              function editavel(no) {
                if (!no || no.nodeType !== 1) return false;
                if (no.tagName === "TEXTAREA") return !no.disabled && !no.readOnly;
                if (no.tagName === "INPUT") {
                  var tipo = (no.type || "text").toLowerCase();
                  if (tipo === "hidden" || tipo === "button" || tipo === "submit" ||
                      tipo === "checkbox" || tipo === "radio" || tipo === "file" ||
                      tipo === "range" || tipo === "color" || tipo === "image") return false;
                  return !no.disabled && !no.readOnly;
                }
                var ce = no.getAttribute && no.getAttribute("contenteditable");
                if (ce === "" || ce === "true" || ce === "plaintext-only") return true;
                if (no.isContentEditable) return true;
                var papel = ((no.getAttribute && no.getAttribute("role")) || "").toLowerCase();
                if (papel === "textbox" || papel === "searchbox") return true;
                var id = ((no.id || "") + " " + (no.className || "")).toLowerCase();
                if (id.indexOf("prompt-textarea") >= 0 || id.indexOf("prosemirror") >= 0) return true;
                return false;
              }
              function sobe(no) {
                var atual = no;
                while (atual && atual !== document.documentElement) {
                  if (editavel(atual)) return atual;
                  atual = atual.parentElement;
                }
                return null;
              }
              function distanciaAoRetangulo(r) {
                var dx = 0;
                var dy = 0;
                if (x < r.left) dx = r.left - x;
                else if (x > r.right) dx = x - r.right;
                if (y < r.top) dy = r.top - y;
                else if (y > r.bottom) dy = y - r.bottom;
                return Math.sqrt(dx * dx + dy * dy);
              }
              function candidatos() {
                var sel = "textarea, input, [contenteditable=''], [contenteditable=true], [contenteditable=plaintext-only], [role='textbox'], [role='searchbox'], #prompt-textarea, .ProseMirror, [data-testid*='composer'], [data-testid*='prompt']";
                var lista = [];
                try {
                  document.querySelectorAll(sel).forEach(function (el) {
                    if (editavel(el) || el.isContentEditable) lista.push(el);
                  });
                } catch (e) {}
                return lista;
              }
              $blocoFoco
              var noPonto = null;
              try { noPonto = document.elementFromPoint(x, y); } catch (e3) { noPonto = null; }
              var direto = sobe(noPonto);
              if (direto) {
                aplicarFoco(direto);
                return "$OK";
              }
              var melhor = null;
              var melhorD = raio + 1;
              candidatos().forEach(function (el) {
                var r = el.getBoundingClientRect();
                if (!r || (r.width < 2 && r.height < 2)) return;
                var d = distanciaAoRetangulo(r);
                if (d <= raio && d < melhorD) {
                  melhorD = d;
                  melhor = el;
                }
              });
              if (melhor) {
                aplicarFoco(melhor);
                return "$OK";
              }
              return "$SEM_ALVO";
            })();
        """.trimIndent()
    }
}

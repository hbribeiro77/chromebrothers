package com.chromebrothers.navegador.regras

/**
 * Lê no documento o retângulo do campo editável focado (shadow/iframe inclusive)
 * e o tamanho do viewport CSS, para o pan do teclado na mesma unidade.
 *
 * Em sites como Twitter/X o “input” pode ser role=textbox / contenteditable alto;
 * o pan usa caret ou o topo da caixa, não o bottom distante de uma área enorme.
 *
 * Se o activeElement não for editável (ex.: wrapper do ChatGPT), sobe aos pais e,
 * como fallback, usa o melhor composer visível (.ProseMirror / aria “Pergunte…” /
 * `__cbGravadorAlvo` do pick).
 */
object PosicaoDoCampoFocadoParaOTecladoDoNavegador {
    /** Quando a caixa editável é alta e começa no topo, pan pela 1ª linha — não pelo bottom. */
    const val ALTURA_DE_LINHA_CSS_PARA_PAN = 40f

    data class Medida(
        val bottomCss: Float,
        val viewportCss: Float,
        val topCss: Float? = null,
        val caretBottomCss: Float? = null,
    ) {
        fun baseParaPan(): Float = baseCssParaPan(
            bottomCss = bottomCss,
            topCss = topCss,
            caretBottomCss = caretBottomCss,
            viewportCss = viewportCss,
        )
    }

    private val baseNoJson = Regex("\"bottom\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")
    private val topNoJson = Regex("\"top\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")
    private val caretNoJson = Regex("\"caretBottom\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")
    private val viewportNoJson = Regex("\"viewport\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")

    /**
     * Base vertical (CSS) para decidir quanto subir a página.
     * Preferência: caret → caixa baixa normal → topo+linha quando a caixa é alta no alto da tela.
     */
    fun baseCssParaPan(
        bottomCss: Float,
        topCss: Float?,
        caretBottomCss: Float?,
        viewportCss: Float,
    ): Float {
        val viewport = viewportCss.coerceAtLeast(1f)
        val top = topCss
        if (caretBottomCss != null) {
            val minimo = (top ?: (caretBottomCss - 64f)) - 2f
            val maximo = bottomCss + 2f
            if (caretBottomCss in minimo..maximo) return caretBottomCss
        }
        if (top != null) {
            val altura = bottomCss - top
            val comecaNoAlto = top < viewport * 0.4f
            val caixaAlta = altura > viewport * 0.45f
            if (comecaNoAlto && caixaAlta) {
                return minOf(bottomCss, top + ALTURA_DE_LINHA_CSS_PARA_PAN)
            }
        }
        return bottomCss
    }

    fun script(): String = """
        (function(){
          function editavel(el){
            if (!el || el === document.body || el === document.documentElement) return false;
            var tag = (el.tagName || '').toLowerCase();
            var tipo = (el.type || '').toLowerCase();
            var role = (el.getAttribute && (el.getAttribute('role') || '') || '').toLowerCase();
            var ce = el.getAttribute && el.getAttribute('contenteditable');
            if (ce != null) {
              ce = String(ce).toLowerCase();
              if (ce === 'true' || ce === '' || ce === 'plaintext-only') return true;
            }
            if (el.isContentEditable) return true;
            if (role === 'textbox' || role === 'searchbox' || role === 'combobox') return true;
            try {
              var cls = (typeof el.className === 'string' ? el.className : (el.getAttribute && el.getAttribute('class') || ''));
              if (/\bProseMirror\b|\bql-editor\b|\bCodeMirror\b/.test(cls)) return true;
            } catch (e) {}
            return tag === 'textarea'
              || tag === 'select'
              || (tag === 'input' && !/^(button|checkbox|radio|file|submit|reset|image|hidden|range|color)$/.test(tipo));
          }
          function sobeEditavel(el){
            var cur = el;
            for (var i = 0; i < 12 && cur; i++) {
              if (editavel(cur)) return cur;
              cur = cur.parentElement;
            }
            return null;
          }
          function profundo(raiz){
            var el = raiz.activeElement;
            var guarda = 0;
            while (el && guarda++ < 12) {
              if (el.shadowRoot && el.shadowRoot.activeElement) {
                el = el.shadowRoot.activeElement;
                continue;
              }
              var tag = (el.tagName || '').toUpperCase();
              if (tag === 'IFRAME' || tag === 'FRAME') {
                try {
                  var doc = el.contentDocument;
                  if (doc && doc.activeElement) { el = doc.activeElement; continue; }
                } catch (e) {}
              }
              break;
            }
            return el;
          }
          function pontuaComposer(el){
            var p = 0;
            var aria = ((el.getAttribute && el.getAttribute('aria-label') || '') + '').toLowerCase();
            var cls = (typeof el.className === 'string' ? el.className : (el.getAttribute && el.getAttribute('class') || '')).toLowerCase();
            var txt = ((el.innerText || el.textContent || '') + '').replace(/\s+/g, ' ').trim().toLowerCase().slice(0, 80);
            if (cls.indexOf('prosemirror') >= 0) p += 20;
            if (aria.indexOf('pergunte') >= 0 || aria.indexOf('ask chatgpt') >= 0 || aria.indexOf('message') >= 0) p += 18;
            if (txt.indexOf('pergunte ao chatgpt') >= 0 || txt.indexOf('ask chatgpt') >= 0) p += 14;
            if (((el.getAttribute && el.getAttribute('role') || '') + '').toLowerCase() === 'textbox') p += 8;
            if (editavel(el)) p += 6;
            try {
              var r = el.getBoundingClientRect();
              if (r.width > 40 && r.height > 16) p += 4;
              if (r.top > (window.innerHeight || 0) * 0.3) p += 3;
            } catch (e) {}
            return p;
          }
          function melhorEditorVisivel(){
            try {
              var alvo = window.__cbGravadorAlvo;
              if (alvo && alvo.isConnected) {
                var editAlvo = sobeEditavel(alvo) || (editavel(alvo) ? alvo : null);
                if (editAlvo) return editAlvo;
              }
            } catch (e) {}
            var sel = '.ProseMirror,[aria-label*="Pergunte"],[aria-label*="Ask ChatGPT"],[aria-label*="Message"],[aria-label*="ChatGPT"],[role="textbox"],[contenteditable="true"],[contenteditable=""],[contenteditable="plaintext-only"],#prompt-textarea,textarea,[data-testid*="composer"],[data-testid*="prompt"]';
            var n;
            try { n = document.querySelectorAll(sel); } catch (e) { n = []; }
            var best = null, bestP = -1;
            for (var i = 0; i < n.length; i++) {
              var el = n[i];
              var p = pontuaComposer(el);
              if (p > bestP) { bestP = p; best = el; }
            }
            if (!best || bestP < 10) {
              try {
                var todos = document.querySelectorAll('div,button,p,span,label');
                for (var j = 0; j < todos.length; j++) {
                  var el2 = todos[j];
                  var t2 = ((el2.getAttribute('aria-label') || '') + ' ' + (el2.innerText || '')).toLowerCase();
                  if (t2.indexOf('pergunte ao chatgpt') < 0 && t2.indexOf('ask chatgpt') < 0) continue;
                  var p2 = pontuaComposer(el2) + 10;
                  if (p2 > bestP) { bestP = p2; best = el2; }
                }
              } catch (e2) {}
            }
            return best ? (sobeEditavel(best) || best) : null;
          }
          var el = profundo(document);
          if (!editavel(el)) {
            el = sobeEditavel(el);
          }
          if (!editavel(el)) {
            try {
              el = document.querySelector(
                'textarea:focus, input:focus, [contenteditable="true"]:focus, [contenteditable=""]:focus, [contenteditable="plaintext-only"]:focus, [role="textbox"]:focus, [role="searchbox"]:focus, .ProseMirror:focus'
              );
            } catch (e) { el = null; }
          }
          if (!editavel(el)) el = melhorEditorVisivel();
          if (!editavel(el)) return null;
          // Sem scrollIntoView: no X/Twitter briga com sticky header e joga o campo fora da vista.
          var r = el.getBoundingClientRect();
          var viewport = document.documentElement.clientHeight || window.innerHeight || 0;
          if (!isFinite(r.bottom) || !isFinite(r.top) || !isFinite(viewport) || viewport <= 0) return null;
          if (r.width <= 0 && r.height <= 0) return null;
          if (r.bottom < -2 || r.top > viewport + 2) return null;
          var caretBottom = null;
          try {
            var raiz = el.getRootNode ? el.getRootNode() : document;
            var sel = (raiz.getSelection && raiz.getSelection()) || window.getSelection();
            if (sel && sel.rangeCount > 0) {
              var cr = sel.getRangeAt(0).getBoundingClientRect();
              if (cr && isFinite(cr.bottom) && cr.height >= 0 && (cr.width > 0 || cr.height > 0 || !sel.isCollapsed)) {
                caretBottom = cr.bottom;
              }
            }
          } catch (e) {}
          var out = { bottom: r.bottom, top: r.top, viewport: viewport };
          if (caretBottom != null && isFinite(caretBottom)) out.caretBottom = caretBottom;
          return JSON.stringify(out);
        })();
    """.trimIndent()

    fun lerMedida(json: String?): Medida? {
        val texto = json?.trim().orEmpty()
        if (texto.isEmpty() || texto == "null") return null
        val bottom = baseNoJson.find(texto)?.groupValues?.getOrNull(1)?.toFloatOrNull() ?: return null
        val viewport = viewportNoJson.find(texto)?.groupValues?.getOrNull(1)?.toFloatOrNull() ?: return null
        if (viewport <= 0f) return null
        val top = topNoJson.find(texto)?.groupValues?.getOrNull(1)?.toFloatOrNull()
        val caret = caretNoJson.find(texto)?.groupValues?.getOrNull(1)?.toFloatOrNull()
        return Medida(
            bottomCss = bottom,
            viewportCss = viewport,
            topCss = top,
            caretBottomCss = caret,
        )
    }
}

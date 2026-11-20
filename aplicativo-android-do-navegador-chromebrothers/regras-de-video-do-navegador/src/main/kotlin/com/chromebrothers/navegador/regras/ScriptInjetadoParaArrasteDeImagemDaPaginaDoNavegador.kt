package com.chromebrothers.navegador.regras

/**
 * Lift-and-drag de `<img>` na página (Twitter etc.): toque longo ~300ms sobe
 * o fantasma nativo via avisos `onImageDragStart|Move|End`.
 *
 * Fluidez: um único ouvinte no documento (delegação), sem ouvintes por `<img>` e sem observar o DOM —
 * antes cada imagem nova (feed, rolagem infinita) era armada por um MutationObserver.
 */
object ScriptInjetadoParaArrasteDeImagemDaPaginaDoNavegador {
    const val AVISO_INICIO = "onImageDragStart"
    const val AVISO_MOVER = "onImageDragMove"
    const val AVISO_FIM = "onImageDragEnd"

    fun texto(): String = """
        (function () {
          if (window.__chromebrothersArrasteImgPaginaV3) return;
          window.__chromebrothersArrasteImgPaginaV3 = true;
          function avisar(nome, argumentos) {
            try {
              document.dispatchEvent(new CustomEvent("chromebrothers-aviso", {
                detail: JSON.stringify({ nome: nome, argumentos: argumentos })
              }));
            } catch (e) {}
          }
          function uriDeImg(img) {
            return img.currentSrc || img.src || img.getAttribute("src") || "";
          }
          // clientX/Y (CSS): screenX no GeckoView Android não bate com rawX nativo.
          function telaDe(toque) {
            return { x: toque.clientX, y: toque.clientY };
          }
          function imgDoAlvo(ev) {
            var alvo = ev.target;
            try { if (ev.composedPath) alvo = ev.composedPath()[0] || alvo; } catch (e) {}
            return alvo && alvo.tagName === "IMG" ? alvo : null;
          }
          var espera = null;
          var arrastando = false;
          var vigiandoMovimento = false;
          var imgDoToque = null;
          var uri = "";
          var ultimoX = 0;
          var ultimoY = 0;
          var inicioX = 0;
          var inicioY = 0;
          function limparEspera() {
            if (espera) { clearTimeout(espera); espera = null; }
            if (vigiandoMovimento) {
              document.removeEventListener("touchmove", noMoveEspera, true);
              vigiandoMovimento = false;
            }
          }
          function noMoveEspera(ev) {
            if (!espera || !ev.touches || !ev.touches[0]) return;
            var t = telaDe(ev.touches[0]);
            ultimoX = t.x;
            ultimoY = t.y;
            var dx = t.x - inicioX;
            var dy = t.y - inicioY;
            if ((dx * dx + dy * dy) > 196) limparEspera();
          }
          function noMove(ev) {
            if (!arrastando) return;
            try { ev.preventDefault(); ev.stopPropagation(); } catch (e) {}
            if (!ev.touches || !ev.touches[0]) return;
            var t = telaDe(ev.touches[0]);
            ultimoX = t.x;
            ultimoY = t.y;
            avisar("$AVISO_MOVER", [String(ultimoX), String(ultimoY)]);
          }
          function noFim(ev) {
            if (!arrastando) return;
            arrastando = false;
            document.removeEventListener("touchmove", noMove, true);
            document.removeEventListener("touchend", noFim, true);
            document.removeEventListener("touchcancel", noFim, true);
            var x = ultimoX;
            var y = ultimoY;
            if (ev && ev.changedTouches && ev.changedTouches[0]) {
              var t = telaDe(ev.changedTouches[0]);
              x = t.x;
              y = t.y;
            }
            avisar("$AVISO_FIM", [String(x), String(y), uri]);
            uri = "";
          }
          function iniciar(tela) {
            if (arrastando || !uri) return;
            arrastando = true;
            // Só durante o arraste há um ouvinte não-passivo (para a página não rolar junto).
            document.addEventListener("touchmove", noMove, { capture: true, passive: false });
            document.addEventListener("touchend", noFim, true);
            document.addEventListener("touchcancel", noFim, true);
            avisar("$AVISO_INICIO", [uri, String(tela.x), String(tela.y)]);
          }
          document.addEventListener("contextmenu", function (ev) {
            if (!imgDoAlvo(ev)) return;
            try { ev.preventDefault(); ev.stopPropagation(); } catch (e) {}
          }, true);
          document.addEventListener("touchstart", function (ev) {
            limparEspera();
            imgDoToque = null;
            var img = imgDoAlvo(ev);
            if (!img || !ev.touches || ev.touches.length !== 1) return;
            imgDoToque = img;
            arrastando = false;
            uri = "";
            var t = telaDe(ev.touches[0]);
            ultimoX = t.x;
            ultimoY = t.y;
            inicioX = t.x;
            inicioY = t.y;
            document.addEventListener("touchmove", noMoveEspera, { capture: true, passive: true });
            vigiandoMovimento = true;
            espera = setTimeout(function () {
              espera = null;
              if (vigiandoMovimento) {
                document.removeEventListener("touchmove", noMoveEspera, true);
                vigiandoMovimento = false;
              }
              uri = imgDoToque ? (uriDeImg(imgDoToque) || "") : "";
              if (!uri) return;
              iniciar({ x: ultimoX, y: ultimoY });
            }, 300);
          }, { capture: true, passive: true });
          document.addEventListener("touchend", function () { if (!arrastando) limparEspera(); }, { capture: true, passive: true });
          document.addEventListener("touchcancel", function () { if (!arrastando) limparEspera(); }, { capture: true, passive: true });
        })();
    """.trimIndent()
}

// Sites deixam um "esqueleto" piscando (animação em loop) no lugar do anúncio até ele chegar. Com o
// anúncio bloqueado ele nunca chega, e a animação infinita obriga a tela a recompor ~25 vezes por
// segundo mesmo com a página parada (no g1: 126 quadros em 5 s parado; 0 depois de parar os esqueletos).
// "animationiteration" só dispara para animação em loop, então este ouvinte quase nunca roda.
// O esqueleto fica invisível mas mantém o espaço: nada se mexe na página enquanto a pessoa rola.
(function () {
  var PUBLICIDADE = /(^|[\s_-])(ads?|advert\w*|advertising|publicidade|anuncios?|dfp|gpt|sponsor\w*|patrocinad\w*)([\s_-]|$)/i;

  function nomeDe(elemento) {
    var classe = typeof elemento.className === "string" ? elemento.className : "";
    return classe + " " + (elemento.id || "");
  }

  function dentroDePublicidade(elemento) {
    for (var atual = elemento, nivel = 0; atual && atual !== document.body && nivel < 6; atual = atual.parentElement, nivel++) {
      if (PUBLICIDADE.test(nomeDe(atual))) return true;
    }
    return false;
  }

  function semConteudo(elemento) {
    if ((elemento.textContent || "").trim()) return false;
    return !elemento.querySelector("img, iframe, video, canvas, svg, picture, object, embed");
  }

  document.addEventListener("animationiteration", function (evento) {
    var alvo = evento.target;
    if (!alvo || alvo.nodeType !== 1 || !semConteudo(alvo) || !dentroDePublicidade(alvo)) return;
    alvo.style.setProperty("animation", "none", "important");
    alvo.style.setProperty("visibility", "hidden", "important");
  }, true);
})();

(function () {
  var porta = null;

  function serializarResultado(resultado) {
    var texto;
    try {
      texto = typeof resultado === "undefined" ? "null" : JSON.stringify(resultado);
    } catch (erro) {
      texto = "null";
    }
    if (typeof texto !== "string") texto = "null";
    return texto;
  }

  function concluir(id, resultado, profundidade) {
    if (resultado && typeof resultado.then === "function" && profundidade < 4) {
      resultado.then(function (valor) {
        concluir(id, valor, profundidade + 1);
      }, function () {
        responder(id, "");
      });
      return;
    }
    responder(id, resultado);
  }

  function responder(id, resultado) {
    if (!porta) return;
    try {
      porta.postMessage({ id: id, resultado: serializarResultado(resultado) });
    } catch (erro) {}
  }

  /**
   * Avalia no content script isolado (não no MAIN/page): o CSP de sites como
   * chatgpt.com bloqueia eval() no mundo da página e o catch virava null.
   */
  function executarNoIsolado(mensagem) {
    var id = mensagem && mensagem.id;
    var codigo = mensagem && mensagem.codigo;
    if (typeof id === "undefined" || typeof codigo !== "string") return false;
    var resultado;
    try {
      resultado = (0, eval)(codigo);
    } catch (erro) {
      resultado = "CB_ERR:" + String(erro && erro.message ? erro.message : erro);
    }
    concluir(id, resultado, 0);
    return true;
  }

  function ligar() {
    try {
      porta = browser.runtime.connectNative("chromebrothers");
    } catch (erro) {
      setTimeout(ligar, 400);
      return;
    }
    porta.onMessage.addListener(function (mensagem) {
      if (executarNoIsolado(mensagem)) return;
      // Fallback legado: dispara para o MAIN world (se ainda existir listener).
      var texto;
      try { texto = JSON.stringify(mensagem); } catch (erro) { return; }
      document.dispatchEvent(new CustomEvent("chromebrothers-executar", { detail: texto }));
    });
    porta.onDisconnect.addListener(function () {
      porta = null;
      setTimeout(ligar, 400);
    });
  }

  document.addEventListener("chromebrothers-resultado", function (evento) {
    if (!porta || typeof evento.detail !== "string") return;
    var detalhe;
    try { detalhe = JSON.parse(evento.detail); } catch (erro) { return; }
    porta.postMessage({ id: detalhe.id, resultado: detalhe.resultado });
  });

  document.addEventListener("chromebrothers-aviso", function (evento) {
    if (!porta || typeof evento.detail !== "string") return;
    var detalhe;
    try { detalhe = JSON.parse(evento.detail); } catch (erro) { return; }
    if (!detalhe || !detalhe.nome) return;
    porta.postMessage({ aviso: detalhe.nome, argumentos: detalhe.argumentos || [] });
  });

  // Mídia vista na camada de rede (fundo da extensão): só o quadro principal escuta e repassa ao app.
  function ligarCapturaDeMidia() {
    if (window !== window.top) return;
    var canal;
    try {
      canal = browser.runtime.connect({ name: "cb-midia" });
    } catch (erro) {
      setTimeout(ligarCapturaDeMidia, 800);
      return;
    }
    canal.onMessage.addListener(function (mensagem) {
      if (!porta || !mensagem || !mensagem.midia) return;
      try { porta.postMessage({ aviso: "onMediaUrl", argumentos: mensagem.midia }); } catch (erro) {}
    });
    canal.onDisconnect.addListener(function () {
      setTimeout(ligarCapturaDeMidia, 800);
    });
  }

  ligar();
  ligarCapturaDeMidia();
})();

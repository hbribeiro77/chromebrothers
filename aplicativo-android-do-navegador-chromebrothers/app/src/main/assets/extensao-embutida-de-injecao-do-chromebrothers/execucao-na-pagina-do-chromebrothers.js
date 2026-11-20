document.addEventListener("chromebrothers-executar", function (evento) {
  var detalhe = {};
  if (typeof evento.detail === "string") {
    try { detalhe = JSON.parse(evento.detail); } catch (erro) { detalhe = {}; }
  }
  var resultado;
  try {
    resultado = (0, eval)(detalhe.codigo);
  } catch (erro) {
    resultado = "CB_ERR:" + String(erro && erro.message ? erro.message : erro);
  }
  concluir(detalhe.id, resultado, 0);
});

function concluir(id, resultado, profundidade) {
  if (resultado && typeof resultado.then === "function" && profundidade < 4) {
    resultado.then(function (valor) {
      concluir(id, valor, profundidade + 1);
    }, function () {
      publicar(id, "");
    });
    return;
  }
  publicar(id, resultado);
}

function publicar(id, resultado) {
  var texto;
  try {
    texto = typeof resultado === "undefined" ? "null" : JSON.stringify(resultado);
  } catch (erro) {
    texto = "null";
  }
  if (typeof texto !== "string") texto = "null";
  document.dispatchEvent(new CustomEvent("chromebrothers-resultado", {
    detail: JSON.stringify({ id: id, resultado: texto })
  }));
}

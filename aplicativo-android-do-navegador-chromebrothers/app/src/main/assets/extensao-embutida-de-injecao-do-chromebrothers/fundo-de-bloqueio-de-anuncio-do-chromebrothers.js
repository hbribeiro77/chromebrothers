// Cada recurso da página espera esta consulta (webRequest "blocking"). As listas do EasyList têm
// milhares de domínios: em vez de indexOf num array (lento a cada pedido), viram conjuntos (consulta direta).
function paraConjunto(lista) {
  var conjunto = new Set();
  (lista || []).forEach(function (host) { if (host) conjunto.add(String(host).toLowerCase()); });
  return conjunto;
}

function montarPacote(bruto) {
  return {
    bloqueios: paraConjunto(bruto && bruto.bloqueios),
    bloqueiosDeTerceiros: paraConjunto(bruto && bruto.bloqueiosDeTerceiros),
    excecoes: paraConjunto(bruto && bruto.excecoes),
    excecoesDeTerceiros: paraConjunto(bruto && bruto.excecoesDeTerceiros)
  };
}

var pacote = montarPacote(null);

function cobre(host, conjunto) {
  if (!conjunto.size) return false;
  var atual = (host || "").toLowerCase();
  while (atual) {
    if (conjunto.has(atual)) return true;
    var ponto = atual.indexOf(".");
    if (ponto <= 0) break;
    atual = atual.substring(ponto + 1);
  }
  return false;
}

function baseRegistravel(host) {
  if (!host || host.indexOf(":") >= 0) return host || "";
  if (/^[0-9.]+$/.test(host)) return host;
  var partes = host.split(".").filter(function (parte) { return parte.length > 0; });
  if (partes.length <= 2) return partes.join(".");
  return partes.slice(partes.length - 2).join(".");
}

function mesmoSite(hostPedido, hostPagina) {
  var pedido = baseRegistravel((hostPedido || "").toLowerCase());
  var pagina = baseRegistravel((hostPagina || "").toLowerCase());
  return pedido !== "" && pedido === pagina;
}

function ligarPacoteDeBloqueio() {
  var porta;
  try {
    porta = browser.runtime.connectNative("chromebrothers-anuncios");
  } catch (erro) {
    setTimeout(ligarPacoteDeBloqueio, 400);
    return;
  }
  porta.onMessage.addListener(function (mensagem) {
    if (mensagem && mensagem.pacote) pacote = montarPacote(mensagem.pacote);
  });
  porta.onDisconnect.addListener(function () {
    setTimeout(ligarPacoteDeBloqueio, 400);
  });
}

browser.webRequest.onBeforeRequest.addListener(function (pedido) {
  if (pedido.type === "main_frame") return {};
  if (!pacote.bloqueios.size && !pacote.bloqueiosDeTerceiros.size) return {};
  var host = "";
  try { host = new URL(pedido.url).host; } catch (erro) { return {}; }
  var hostPagina = "";
  try { hostPagina = new URL(pedido.originUrl || pedido.documentUrl || "").host; } catch (erro) {}
  var terceiro = hostPagina === "" || !mesmoSite(host, hostPagina);
  if (cobre(host, pacote.excecoes) || (terceiro && cobre(host, pacote.excecoesDeTerceiros))) return {};
  if (cobre(host, pacote.bloqueios) || (terceiro && cobre(host, pacote.bloqueiosDeTerceiros))) {
    return { cancel: true };
  }
  return {};
}, { urls: ["http://*/*", "https://*/*"] }, ["blocking"]);

ligarPacoteDeBloqueio();

// ---------- Captura de mídia para download (camada de rede) ----------
// O gancho de fetch/XHR injetado roda no mundo isolado e não vê o player da página (nem workers).
// Aqui o webRequest vê toda resposta de mídia da aba e avisa o script-ponte daquela aba,
// que repassa ao app como "onMediaUrl" (o app filtra e classifica; só registra com a captura ligada).
var portasDeMidiaPorAba = {};
var midiaJaAvisadaPorAba = {};

browser.runtime.onConnect.addListener(function (porta) {
  if (porta.name !== "cb-midia") return;
  var aba = porta.sender && porta.sender.tab ? porta.sender.tab.id : -1;
  if (aba < 0) return;
  (portasDeMidiaPorAba[aba] = portasDeMidiaPorAba[aba] || []).push(porta);
  porta.onDisconnect.addListener(function () {
    var lista = portasDeMidiaPorAba[aba] || [];
    var indice = lista.indexOf(porta);
    if (indice >= 0) lista.splice(indice, 1);
    if (lista.length === 0) {
      delete portasDeMidiaPorAba[aba];
      delete midiaJaAvisadaPorAba[aba];
    }
  });
});

function cabecalho(cabecalhos, nome) {
  for (var i = 0; i < (cabecalhos || []).length; i++) {
    if ((cabecalhos[i].name || "").toLowerCase() === nome) return cabecalhos[i].value || "";
  }
  return "";
}

browser.webRequest.onHeadersReceived.addListener(function (resposta) {
  var aba = resposta.tabId;
  if (aba < 0 || !portasDeMidiaPorAba[aba]) return;
  if (resposta.statusCode < 200 || resposta.statusCode >= 300) return;
  var tipo = cabecalho(resposta.responseHeaders, "content-type");
  var endereco = resposta.url || "";
  var caminho = endereco.split("?")[0].toLowerCase();
  // Trechos soltos (HLS .ts / DASH .m4s) não servem sozinhos: a playlist/manifesto é que importa.
  if (/\.(m4s|ts|aac|vtt|webvtt)$/.test(caminho)) return;
  var pareceMidia = /^(video|audio)\//i.test(tipo) || /mpegurl|dash\+xml/i.test(tipo) ||
    /\.(m3u8|mpd|mp4|m4v|webm|mov|mkv)$/.test(caminho) || resposta.type === "media";
  if (!pareceMidia) return;
  var faixa = cabecalho(resposta.responseHeaders, "content-range");
  var total = /\/(\d+)\s*$/.exec(faixa);
  var tamanho = total ? total[1] : cabecalho(resposta.responseHeaders, "content-length");
  var chave = endereco + "|" + tamanho;
  var avisadas = (midiaJaAvisadaPorAba[aba] = midiaJaAvisadaPorAba[aba] || {});
  if (avisadas[chave]) return;
  avisadas[chave] = true;
  (portasDeMidiaPorAba[aba] || []).forEach(function (porta) {
    try { porta.postMessage({ midia: [endereco, tipo, tamanho || ""] }); } catch (erro) {}
  });
}, { urls: ["http://*/*", "https://*/*"] }, ["responseHeaders"]);

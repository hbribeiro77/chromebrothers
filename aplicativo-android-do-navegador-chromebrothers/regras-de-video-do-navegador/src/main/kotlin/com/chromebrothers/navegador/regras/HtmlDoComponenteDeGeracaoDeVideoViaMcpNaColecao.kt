package com.chromebrothers.navegador.regras

/** HTML empacotado do componente "Geração MCP — vídeo" da coleção flutuante. */
object HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao {
    const val ID: String = "geracao-mcp-video"
    const val NOME: String = "Geração MCP — vídeo"
    val TAGS: List<String> = listOf("mcp", "sjinn", "higgsfield", "topview", "vivix", "video", "wan3")

    fun html(): String = HTML

    private val HTML: String = """<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Geração MCP — vídeo</title>
<style>
* { box-sizing: border-box; }
body { margin: 0; padding: 12px; background: #f6f7f9; color: #1a1a1a; font: 14px/1.35 system-ui, sans-serif; }
main { max-width: 560px; margin: 0 auto; display: flex; flex-direction: column; gap: 10px; }
h1 { font-size: 17px; margin: 0; font-weight: 600; }
label { display: flex; flex-direction: column; gap: 4px; font-weight: 600; font-size: 12px; }
select, textarea { font: inherit; font-weight: 400; color: #1a1a1a; background: #fff; border: 1px solid #c9ced6; border-radius: 6px; padding: 8px 10px; width: 100%; }
textarea { min-height: 110px; resize: vertical; }
.linha { display: flex; gap: 6px; flex-wrap: wrap; align-items: center; }
.grade-opcoes { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
@media (min-width: 420px) { .grade-opcoes { grid-template-columns: 1fr 1fr 1fr 1fr; } }
button { font: inherit; color: #1a1a1a; background: #fff; border: 1px solid #c9ced6; border-radius: 6px; padding: 8px 12px; min-height: 40px; }
button.principal { background: #1a1a1a; color: #fff; border-color: #1a1a1a; font-weight: 600; width: 100%; }
button:disabled { opacity: 0.5; }
.bloco-ref { display: flex; flex-direction: column; gap: 6px; }
.bloco-ref > .titulo { font-weight: 600; font-size: 12px; }
#refs { display: flex; flex-wrap: wrap; gap: 6px; min-height: 0; }
.ref { position: relative; width: 64px; }
.ref img { width: 64px; height: 64px; object-fit: cover; border-radius: 6px; border: 1px solid #c9ced6; display: block; background: #fff; }
.ref button { position: absolute; top: 1px; right: 1px; min-height: 22px; padding: 0 6px; font-size: 12px; line-height: 1; }
.ref .selo-video { position: absolute; left: 3px; bottom: 3px; background: rgba(0, 0, 0, 0.72); color: #fff; font-size: 10px; font-weight: 700; padding: 1px 5px; border-radius: 8px; pointer-events: none; }
.ref.video img { border-color: #2F6FED; }
.ref.em-exibicao img { outline: 2px solid #f5c518; outline-offset: 1px; }
#contRef { font-size: 12px; color: #4a4f57; }
#dicaVideo { font-size: 12px; color: #4a4f57; }
#dicaVideo.aceita { color: #1a7f37; }
#dicaVideo.alerta { color: #b00020; }
#status { min-height: 18px; font-size: 13px; color: #4a4f57; }
#status.erro { color: #b00020; }
#grade { display: flex; flex-direction: column; gap: 8px; }
.item-video { display: flex; flex-direction: column; gap: 6px; }
.item-video .poster-do-video { width: 100%; aspect-ratio: 16 / 9; object-fit: cover; border-radius: 6px; border: 1px solid #c9ced6; background: #111; display: block; }
.item-video .poster-vazio { width: 100%; aspect-ratio: 16 / 9; border-radius: 6px; border: 1px solid #c9ced6; background: #222; color: #ccc; display: flex; align-items: center; justify-content: center; font-size: 13px; }
.item-video .btn-reproduzir { width: 100%; }
.card { border: 1px solid #c9ced6; border-radius: 8px; background: #fff; padding: 8px; }
.card .cab { display: flex; flex-wrap: wrap; gap: 4px 8px; align-items: baseline; font-size: 12px; color: #4a4f57; }
.card .cab .num { font-weight: 700; color: #1a1a1a; font-size: 13px; }
.card .estado { font-size: 12px; color: #4a4f57; }
.card .estado.erro { color: #b00020; }
.card .acoes { display: flex; gap: 6px; }
.card .acoes button { flex: 1; min-height: 34px; padding: 4px 8px; }
.card.em-exibicao { outline: 3px solid #2F6FED; outline-offset: 1px; }
.poster-box { position: relative; }
.poster-box .botao-play { position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); width: 64px; height: 64px; min-height: 0; padding: 0 0 0 5px; border: none; border-radius: 50%; background: rgba(0, 0, 0, 0.55); color: #fff; font-size: 26px; line-height: 64px; }
.badge-new { position: absolute; top: 8px; left: 8px; background: #E53935; color: #fff; font-size: 11px; font-weight: 700; letter-spacing: 0.5px; padding: 2px 8px; border-radius: 10px; }
.card-skeleton .poster-vazio { background: linear-gradient(90deg, #2a2a2a 0%, #3d3d3d 50%, #2a2a2a 100%); background-size: 200% 100%; animation: brilho 1.4s linear infinite; }
.card-skeleton.parado .poster-vazio { animation: none; background: #2a2a2a; }
@keyframes brilho { from { background-position: 100% 0; } to { background-position: -100% 0; } }
#mcps { display: flex; flex-wrap: wrap; gap: 8px 14px; }
#mcps label.mcp-opcao { flex-direction: row; align-items: center; gap: 6px; font-weight: 500; font-size: 13px; }
#mcps input { width: auto; }
</style>
</head>
<body>
<main>
<h1>Geração MCP — vídeo</h1>
<div class="bloco-ref">
<div class="titulo">MCPs</div>
<div id="mcps"></div>
</div>
<label>Prompt
<textarea id="prompt" maxlength="20000" placeholder="Descreva o vídeo"></textarea>
</label>
<div class="bloco-ref">
<div class="titulo">Referências <span id="contRef"></span></div>
<div id="dicaVideo"></div>
<div id="refs"></div>
<div class="linha">
<button type="button" id="btnGaleria">Galeria</button>
<button type="button" id="btnBiblioteca">Biblioteca</button>
<button type="button" id="btnLimpar">Limpar</button>
</div>
</div>
<div class="grade-opcoes">
<label>Modelo
<select id="selModelo"></select>
</label>
<label>Proporção
<select id="proporcao">
<option value="16:9" selected>16:9</option>
<option value="9:16">9:16</option>
<option value="1:1">1:1</option>
<option value="4:3">4:3</option>
<option value="3:4">3:4</option>
<option value="adaptive">adaptive</option>
</select>
</label>
<label>Resolução
<select id="resolucao">
<option value="480p" selected>480P</option>
<option value="720p">720P</option>
<option value="1080p">1080P</option>
</select>
</label>
<label>Duração (s)
<select id="duracao">
<option value="2">2</option>
<option value="3">3</option>
<option value="4">4</option>
<option value="5" selected>5</option>
<option value="6">6</option>
<option value="8">8</option>
<option value="10">10</option>
<option value="12">12</option>
<option value="15">15</option>
<option value="20">20</option>
<option value="25">25</option>
<option value="30">30</option>
</select>
</label>
<label>Gerações (por MCP)
<select id="qtd">
<option value="1" selected>1</option>
<option value="2">2</option>
<option value="3">3</option>
<option value="4">4</option>
</select>
</label>
</div>
<button type="button" id="btnGerar" class="principal">Gerar vídeo</button>
<div id="status" role="status"></div>
<div id="grade"></div>
</main>
<script>
var MODELO_PADRAO_COMUM = "wan3";
var LIMITE_REFS = 4;
var NOMES_DOS_MCPS = { higgsfield: "Higgsfield", sjinn: "Sjinn", topview: "Topview", magnific: "Magnific", vivix: "Vivix W1" };
var cardsPorId = {};
var vistos = {};
var uriEmExibicao = null;
var mcpsEl = document.getElementById("mcps");
var selModelo = document.getElementById("selModelo");
var selProp = document.getElementById("proporcao");
var selRes = document.getElementById("resolucao");
var selDur = document.getElementById("duracao");
var selQtd = document.getElementById("qtd");
var ta = document.getElementById("prompt");
var btn = document.getElementById("btnGerar");
var elStatus = document.getElementById("status");
var grade = document.getElementById("grade");
var refsEl = document.getElementById("refs");
var contRef = document.getElementById("contRef");
var dicaVideo = document.getElementById("dicaVideo");
var qtdRefs = 0;
var qtdVideos = 0;
var aceitaVideo = false;
var textoDaDica = "";
function pintarDica() {
  if (qtdVideos > 0 && !aceitaVideo) {
    dicaVideo.textContent = "Este modelo não aceita vídeo de referência. Troque o modelo ou tire os vídeos.";
    dicaVideo.className = "alerta";
    return;
  }
  dicaVideo.textContent = textoDaDica;
  dicaVideo.className = aceitaVideo ? "aceita" : "";
}
function informarModelo() {
  var r = ponte(function () {
    if (!chromebrothers.informarModeloGeracaoVideo) return JSON.stringify({ ok: false });
    return chromebrothers.informarModeloGeracaoVideo(mcpsMarcados().join(","), selModelo.value || "");
  });
  aceitaVideo = !!(r && r.ok && r.aceitaVideo);
  textoDaDica = (r && r.dica) || "";
  pintarDica();
}
function mcpsMarcados() {
  var out = [];
  var inputs = mcpsEl.querySelectorAll('input[type="checkbox"][data-mcp]');
  for (var i = 0; i < inputs.length; i++) {
    if (inputs[i].checked) out.push(String(inputs[i].getAttribute("data-mcp") || "").toLowerCase());
  }
  return out;
}
function modeloPadrao() { return MODELO_PADRAO_COMUM; }

function ponte(chamada) {
  try {
    if (typeof chromebrothers === "undefined") return { ok: false, erro: "Ponte indisponível." };
    return JSON.parse(chamada());
  } catch (e) {
    return { ok: false, erro: "Resposta inválida da ponte." };
  }
}
function mostrar(msg, erro) {
  elStatus.textContent = msg || "";
  elStatus.className = erro ? "erro" : "";
}
function preencher(sel, itens) {
  sel.innerHTML = "";
  (itens || []).forEach(function (it) {
    var o = document.createElement("option");
    o.value = it.id;
    o.textContent = it.nome || it.id;
    sel.appendChild(o);
  });
}
function selecionarModeloPadrao() {
  var alvo = modeloPadrao();
  for (var i = 0; i < selModelo.options.length; i++) {
    if (selModelo.options[i].value === alvo) {
      selModelo.selectedIndex = i;
      return;
    }
  }
}
/** `file://` de cada slot depois de abrir no visor (para a moldura na miniatura certa). */
var urisDasRefs = [];
function semQueryDaRef(u) {
  u = String(u || "");
  var i = u.indexOf("?");
  return i >= 0 ? u.substring(0, i) : u;
}
/** Imagem: visor do Visualizador com os bytes exatos que vão ser enviados. Vídeo: toca no player. */
function abrirReferencia(indice) {
  if (typeof chromebrothers === "undefined" || !chromebrothers.abrirReferenciaNoVisor) return;
  var r = ponte(function () { return chromebrothers.abrirReferenciaNoVisor(String(indice)); });
  if (!r.ok) { mostrar(r.erro || "Não consegui abrir a referência.", true); return; }
  urisDasRefs = r.uris || [];
}
/** Moldura na referência que está no visor; null limpa. Chamado pelo app. */
window.__chromebrothersImagemNoVisor = function (uri) {
  var alvo = uri ? semQueryDaRef(uri) : null;
  var refs = refsEl.querySelectorAll(".ref");
  for (var k = 0; k < refs.length; k++) {
    refs[k].classList.toggle("em-exibicao", !!alvo && semQueryDaRef(urisDasRefs[k]) === alvo);
  }
};
function pintarRefs(r) {
  refsEl.innerHTML = "";
  urisDasRefs = [];
  var itens = (r && r.itens) || [];
  qtdRefs = itens.length;
  qtdVideos = 0;
  contRef.textContent = qtdRefs ? "(" + qtdRefs + "/" + LIMITE_REFS + ")" : "(até " + LIMITE_REFS + ")";
  itens.forEach(function (it, indice) {
    var wrap = document.createElement("div");
    wrap.className = it.video ? "ref video" : "ref";
    var img = document.createElement("img");
    img.alt = it.nome || ((it.video ? "Vídeo " : "Ref ") + (indice + 1));
    img.src = it.previewDataUrl || "";
    img.addEventListener("click", function () { abrirReferencia(indice); });
    var x = document.createElement("button");
    x.type = "button";
    x.textContent = "×";
    x.addEventListener("click", function () { limparIndice(indice); });
    wrap.appendChild(img);
    if (it.video) {
      qtdVideos++;
      var selo = document.createElement("span");
      selo.className = "selo-video";
      selo.textContent = "▶ " + (it.duracao || "");
      wrap.appendChild(selo);
    }
    wrap.appendChild(x);
    refsEl.appendChild(wrap);
  });
  pintarDica();
  document.getElementById("btnGaleria").disabled = qtdRefs >= LIMITE_REFS;
  document.getElementById("btnBiblioteca").disabled = qtdRefs >= LIMITE_REFS;
}
function carregarMcps() {
  var r = ponte(function () { return chromebrothers.listarMcpsGeracaoVideo(); });
  if (!r.ok) { mostrar(r.erro || "Falha ao listar MCPs.", true); return; }
  mcpsEl.innerHTML = "";
  (r.itens || []).forEach(function (it) {
    var lab = document.createElement("label");
    lab.className = "mcp-opcao";
    var cb = document.createElement("input");
    cb.type = "checkbox";
    cb.setAttribute("data-mcp", it.id);
    if (it.id === "sjinn") cb.checked = true;
    cb.addEventListener("change", carregarModelosDepoisDoToque);
    lab.appendChild(cb);
    lab.appendChild(document.createTextNode(it.nome || it.id));
    mcpsEl.appendChild(lab);
  });
  carregarModelos();
}
/** Deixa o checkbox ser desenhado antes de buscar os modelos (a ponte é síncrona). */
var recargaPendente = null;
function carregarModelosDepoisDoToque() {
  if (recargaPendente) clearTimeout(recargaPendente);
  requestAnimationFrame(function () {
    recargaPendente = setTimeout(function () { recargaPendente = null; carregarModelos(); }, 30);
  });
}
function carregarModelos() {
  var mcps = mcpsMarcados();
  selModelo.innerHTML = "";
  if (mcps.length < 1) {
    var vazio = document.createElement("option");
    vazio.value = "";
    vazio.textContent = "Marque um MCP";
    selModelo.appendChild(vazio);
    mostrar("Marque pelo menos um MCP.", true);
    informarModelo();
    return;
  }
  var carregando = document.createElement("option");
  carregando.value = "";
  carregando.textContent = "Carregando…";
  selModelo.appendChild(carregando);
  var chave = mcps.join(",");
  var r = ponte(function () { return chromebrothers.listarModelosGeracaoVideo(chave); });
  if (!r.ok) {
    selModelo.innerHTML = "";
    var e = document.createElement("option");
    e.value = modeloPadrao();
    e.textContent = "Erro — Wan 3";
    selModelo.appendChild(e);
    mostrar(r.erro || "Falha ao listar modelos.", true);
    return;
  }
  if (!(r.itens || []).length) {
    selModelo.innerHTML = "";
    var sem = document.createElement("option");
    sem.value = "";
    sem.textContent = "Nenhum modelo em comum";
    selModelo.appendChild(sem);
    mostrar("Nenhum modelo comum aos MCPs marcados.", true);
    return;
  }
  preencher(selModelo, r.itens);
  selecionarModeloPadrao();
  mostrar("");
  informarModelo();
}
function aplicarImagem(r) {
  if (!r || !r.ok) { if (r && r.erro) mostrar(r.erro, true); return; }
  if (r.aguardando) { mostrar(aceitaVideo ? "Aguardando a escolha (imagem ou vídeo)…" : "Aguardando a escolha da imagem…"); return; }
  if (r.itens) { pintarRefs(r); mostrar(""); return; }
  if (r.previewDataUrl) {
    pintarRefs({ itens: [{ previewDataUrl: r.previewDataUrl, nome: r.nome || "" }] });
    mostrar("");
  }
}
window.__chromebrothersImagemGeracao = function (json) {
  var r;
  try { r = typeof json === "string" ? JSON.parse(json) : json; } catch (e) { mostrar("Imagem inválida.", true); return; }
  aplicarImagem(r);
};
function escolher(origem) {
  if (qtdRefs >= LIMITE_REFS) { mostrar("No máximo " + LIMITE_REFS + " referências.", true); return; }
  var r = ponte(function () { return chromebrothers.escolherImagemGeracao(origem); });
  if (!r.ok) { mostrar(r.erro || "Não foi possível escolher a imagem.", true); return; }
  aplicarImagem(r);
}
/** Com referências, o app confirma no modal padrão; a lista nova chega por __chromebrothersImagemGeracao. */
function limpar() {
  if (chromebrothers.pedirParaLimparReferencias) {
    var p = ponte(function () { return chromebrothers.pedirParaLimparReferencias(); });
    if (!p.ok) mostrar(p.erro || "Não foi possível limpar.", true);
    else if (p.itens) pintarRefs(p);
    return;
  }
  ponte(function () { return chromebrothers.limparImagemGeracao(); });
  pintarRefs({ itens: [] });
  mostrar("");
}
/** "×": o app confirma no modal padrão antes de tirar (fácil tocar sem querer). */
function limparIndice(indice) {
  if (chromebrothers.pedirParaRemoverReferencia) {
    var p = ponte(function () { return chromebrothers.pedirParaRemoverReferencia(String(indice)); });
    if (!p.ok) mostrar(p.erro || "Não foi possível remover.", true);
    return;
  }
  var r = ponte(function () { return chromebrothers.limparImagemGeracaoIndice(String(indice)); });
  if (!r.ok) { mostrar(r.erro || "Não foi possível remover.", true); return; }
  pintarRefs(r);
  mostrar("");
}
function abrirNoPlayer(fileUrl) {
  if (!fileUrl) return;
  if (typeof chromebrothers !== "undefined" && chromebrothers.abrirVideoNoPlayer) {
    chromebrothers.abrirVideoNoPlayer(fileUrl);
  }
}
function doisDigitos(n) { return (n < 10 ? "0" : "") + n; }
function horarioDoPedido(ms) {
  if (!ms) return "";
  var d = new Date(ms);
  var hoje = new Date();
  var hora = doisDigitos(d.getHours()) + ":" + doisDigitos(d.getMinutes());
  if (d.toDateString() === hoje.toDateString()) return hora;
  return doisDigitos(d.getDate()) + "/" + doisDigitos(d.getMonth() + 1) + " " + hora;
}
function minutosDesde(ms) {
  return Math.max(0, Math.floor((Date.now() - (ms || Date.now())) / 60000));
}
function emAndamento(c) { return c.status === "enviando" || c.status === "gerando"; }
function textoDoEstado(c) {
  if (c.verificando) return "Verificando…";
  if (c.status === "enviando") return "Enviando ao " + (NOMES_DOS_MCPS[c.mcp] || c.mcp) + "…";
  if (c.status === "gerando") {
    var min = minutosDesde(c.criadoEmMs);
    return min < 1 ? "Gerando…" : ("Gerando · " + min + " min");
  }
  if (c.status === "demorou") return "Demorou: a tarefa pode terminar depois. Toque em Verificar.";
  if (c.status === "falhou") return "Falhou.";
  if (c.status === "pronto") return "Arquivo removido";
  return "";
}
function botao(texto, aoTocar) {
  var b = document.createElement("button");
  b.type = "button";
  b.textContent = texto;
  b.addEventListener("click", function (e) {
    e.preventDefault();
    e.stopPropagation();
    aoTocar(b);
  });
  return b;
}
function cabecalhoDoCard(c) {
  var cab = document.createElement("div");
  cab.className = "cab";
  var num = document.createElement("span");
  num.className = "num";
  num.textContent = "#" + c.numero;
  cab.appendChild(num);
  [horarioDoPedido(c.criadoEmMs), NOMES_DOS_MCPS[c.mcp] || c.mcp, c.modelo].forEach(function (t) {
    if (!t) return;
    var s = document.createElement("span");
    s.textContent = t;
    cab.appendChild(s);
  });
  return cab;
}
function pintarCardPronto(el, c) {
  el.className = "card item-video" + (c.fileUrl === uriEmExibicao ? " em-exibicao" : "");
  el.setAttribute("data-cb-uri", c.fileUrl);
  el.appendChild(cabecalhoDoCard(c));
  var caixa = document.createElement("div");
  caixa.className = "poster-box";
  if (c.previewDataUrl) {
    var img = document.createElement("img");
    img.className = "poster-do-video";
    img.alt = "Prévia do vídeo #" + c.numero;
    img.src = c.previewDataUrl;
    caixa.appendChild(img);
  } else {
    var vazio = document.createElement("div");
    vazio.className = "poster-vazio";
    vazio.textContent = "Vídeo pronto";
    caixa.appendChild(vazio);
  }
  caixa.addEventListener("click", function () { abrirAoLado(c.fileUrl); });
  var play = botao("▶", function () { abrirAoLado(c.fileUrl); });
  play.className = "botao-play";
  play.setAttribute("aria-label", "Reproduzir o vídeo #" + c.numero + " ao lado");
  caixa.appendChild(play);
  if (!vistos[c.fileUrl]) {
    var novo = document.createElement("span");
    novo.className = "badge-new";
    novo.textContent = "NEW";
    caixa.appendChild(novo);
  }
  el.appendChild(caixa);
  var telaCheia = botao("Tela cheia", function () {
    marcarVisto(c.fileUrl);
    abrirNoPlayer(c.fileUrl);
  });
  telaCheia.className = "btn-reproduzir";
  el.appendChild(telaCheia);
  var acoes = document.createElement("div");
  acoes.className = "acoes";
  acoes.appendChild(botao("Usar de novo", function () { usarDeNovo(c); }));
  // Tira só do gerador: o vídeo continua na biblioteca.
  acoes.appendChild(botao("Remover", function () { removerCard(c.id); }));
  el.appendChild(acoes);
}
function marcarVisto(fileUrl) {
  if (!fileUrl || vistos[fileUrl]) return;
  vistos[fileUrl] = true;
  Object.keys(cardsPorId).forEach(function (id) {
    var c = cardsPorId[id].dados;
    if (c.fileUrl !== fileUrl) return;
    var badge = cardsPorId[id].el.querySelector(".badge-new");
    if (badge) badge.parentNode.removeChild(badge);
  });
}
function abrirAoLado(fileUrl) {
  var lista = [];
  var indice = 0;
  var prontos = grade.querySelectorAll(".card[data-cb-uri]");
  for (var i = 0; i < prontos.length; i++) {
    var id = prontos[i].getAttribute("data-card-id");
    var c = cardsPorId[id] && cardsPorId[id].dados;
    if (!c || !c.fileUrl) continue;
    if (c.fileUrl === fileUrl) indice = lista.length;
    lista.push({ fileUrl: c.fileUrl, previewDataUrl: c.previewDataUrl || "" });
  }
  if (!lista.length) return;
  var r = ponte(function () { return chromebrothers.abrirVideosAoLado(JSON.stringify(lista), String(indice)); });
  if (!r.ok) {
    marcarVisto(fileUrl);
    abrirNoPlayer(fileUrl);
  }
}
window.__chromebrothersVideoNoVisor = function (uri) {
  uriEmExibicao = uri || null;
  if (uriEmExibicao) marcarVisto(uriEmExibicao);
  Object.keys(cardsPorId).forEach(function (id) {
    var atual = cardsPorId[id];
    var emExibicao = !!uriEmExibicao && atual.dados.fileUrl === uriEmExibicao;
    atual.el.classList.toggle("em-exibicao", emExibicao);
    if (emExibicao && atual.el.scrollIntoView) atual.el.scrollIntoView({ block: "nearest", behavior: "smooth" });
  });
};
function pintarCardSkeleton(el, c) {
  el.className = "card card-skeleton item-video" + (emAndamento(c) || c.verificando ? "" : " parado");
  el.appendChild(cabecalhoDoCard(c));
  var poster = document.createElement("div");
  poster.className = "poster-vazio";
  poster.textContent = textoDoEstado(c);
  el.appendChild(poster);
  if (c.mensagem && !emAndamento(c)) {
    var msg = document.createElement("div");
    msg.className = "estado" + (c.status === "falhou" ? " erro" : "");
    msg.textContent = c.mensagem;
    el.appendChild(msg);
  }
  if (emAndamento(c)) return;
  var acoes = document.createElement("div");
  acoes.className = "acoes";
  if (c.podeVerificar) {
    var v = botao("Verificar", function () { verificarCard(c.id); });
    v.disabled = !!c.verificando;
    acoes.appendChild(v);
  }
  if (c.idDaReceita && c.status === "falhou") acoes.appendChild(botao("Tentar de novo", function () { tentarDeNovo(c); }));
  acoes.appendChild(botao("Usar de novo", function () { usarDeNovo(c); }));
  acoes.appendChild(botao("Remover", function () { removerCard(c.id); }));
  el.appendChild(acoes);
}
function pintarCard(c) {
  if (!c || !c.id) return;
  var anterior = cardsPorId[c.id];
  // Arquivo movido para a Galeria: mesmo card, endereço novo — continua visto (sem NEW de novo).
  if (anterior && anterior.dados.fileUrl && c.fileUrl && vistos[anterior.dados.fileUrl]) vistos[c.fileUrl] = true;
  var el = anterior ? anterior.el : document.createElement("div");
  el.innerHTML = "";
  el.removeAttribute("data-cb-uri");
  el.setAttribute("data-card-id", c.id);
  cardsPorId[c.id] = { el: el, dados: c };
  if (c.status === "pronto" && c.fileUrl) pintarCardPronto(el, c);
  else pintarCardSkeleton(el, c);
  if (!anterior) grade.insertBefore(el, grade.firstChild);
}
function verificarCard(id) {
  var atual = cardsPorId[id];
  if (!atual) return;
  atual.dados.verificando = true;
  pintarCard(atual.dados);
  var r = ponte(function () { return chromebrothers.verificarCardDeVideoMcp(id); });
  if (!r.ok) {
    atual.dados.verificando = false;
    atual.dados.mensagem = r.erro || "Não foi possível verificar.";
    pintarCard(atual.dados);
  }
}
function removerCard(id) {
  ponte(function () { return chromebrothers.removerCardDeVideoMcp(id); });
  var atual = cardsPorId[id];
  if (atual && atual.el.parentNode) atual.el.parentNode.removeChild(atual.el);
  delete cardsPorId[id];
}
function atualizarTemposDosCards() {
  Object.keys(cardsPorId).forEach(function (id) {
    var c = cardsPorId[id].dados;
    if (c.status === "gerando") pintarCard(c);
  });
}
function restaurarCards() {
  var r = ponte(function () { return chromebrothers.listarCardsDeVideoMcp(); });
  if (!r.ok) return;
  // Prontos de sessões anteriores já foram vistos: sem selo NEW.
  (r.cards || []).forEach(function (c) { if (c.status === "pronto" && c.fileUrl) vistos[c.fileUrl] = true; });
  (r.cards || []).forEach(pintarCard);
}
window.__chromebrothersGeracaoMcp = function (json) {
  var r;
  try { r = typeof json === "string" ? JSON.parse(json) : json; } catch (e) { return; }
  if (r && r.card) {
    r.card.verificando = false;
    pintarCard(r.card);
    if (r.movido) mostrar("#" + r.card.numero + " foi para a Galeria.");
    else if (r.card.status === "pronto") mostrar("#" + r.card.numero + " pronto e salvo na biblioteca.");
    return;
  }
  if (r && !r.ok && r.erro) mostrar(r.erro, true);
};
var TIPO_DO_GERADOR = "video";
/* ---- "Usar de novo" e "Tentar de novo" ---- */
function escolherOpcao(sel, valor) {
  if (!sel || valor === undefined || valor === null || valor === "") return false;
  valor = String(valor);
  for (var i = 0; i < sel.options.length; i++) {
    if (sel.options[i].value === valor) { sel.selectedIndex = i; return true; }
  }
  return false;
}
/** Valor da receita que não está na lista (ex.: proporção de um modelo antigo) entra como opção extra. */
function garantirOpcao(sel, valor) {
  if (!sel || valor === undefined || valor === null || valor === "") return;
  if (escolherOpcao(sel, valor)) return;
  var o = document.createElement("option");
  o.value = String(valor);
  o.textContent = String(valor);
  sel.appendChild(o);
  sel.value = String(valor);
}
var textoAntesDaReceita = null;
function mostrarAvisoDaReceita(avisos) {
  mostrar("Receita carregada: revise e toque em Gerar." + (avisos.length ? " " + avisos.join(" ") : ""), false);
  if (textoAntesDaReceita && textoAntesDaReceita !== ta.value) {
    var desfazer = botao("Desfazer texto", function () {
      ta.value = textoAntesDaReceita;
      textoAntesDaReceita = null;
      mostrar("Texto anterior restaurado.");
    });
    desfazer.style.marginLeft = "8px";
    desfazer.style.minHeight = "28px";
    desfazer.style.padding = "2px 8px";
    elStatus.appendChild(desfazer);
  }
}
function preencherComReceita(r) {
  if (!r || !r.ok) { if (r && r.erro) mostrar(r.erro, true); return; }
  var p = r.pedido || {};
  var avisos = (r.avisos || []).slice();
  textoAntesDaReceita = ta.value;
  ta.value = p.prompt || "";
  var mcps = p.mcps || (p.mcp ? [p.mcp] : []);
  if (mcps.length) {
    var inputs = mcpsEl.querySelectorAll('input[type="checkbox"][data-mcp]');
    var achou = 0;
    for (var i = 0; i < inputs.length; i++) {
      var marcar = mcps.indexOf(String(inputs[i].getAttribute("data-mcp") || "").toLowerCase()) >= 0;
      inputs[i].checked = marcar;
      if (marcar) achou++;
    }
    if (achou < mcps.length) avisos.push("Algum MCP da receita não está disponível agora.");
  }
  carregarModelos();
  if (p.modelo && !escolherOpcao(selModelo, p.modelo)) {
    avisos.push("O modelo " + p.modelo + " não está na lista agora; escolha outro.");
  }
  garantirOpcao(selProp, p.proporcao);
  garantirOpcao(selRes, p.resolucao);
  garantirOpcao(selDur, p.duracao);
  if (p.quantidade) escolherOpcao(selQtd, p.quantidade);
  informarModelo();
  pintarRefs(r.refs || { itens: [] });
  mostrarAvisoDaReceita(avisos);
  if (window.scrollTo) window.scrollTo({ top: 0, behavior: "smooth" });
}
function usarDeNovo(c) {
  // Com receita: ela inteira. Card antigo: prompt, MCP e modelo guardados no próprio card.
  preencherComReceita(ponte(function () {
    if (chromebrothers.usarCardDeNovo) return chromebrothers.usarCardDeNovo(TIPO_DO_GERADOR, c.id);
    return chromebrothers.usarReceitaDeNovo(c.idDaReceita || "");
  }));
}
function tentarDeNovo(c) {
  var r = ponte(function () { return chromebrothers.repetirCardDeVideoMcp(c.id); });
  if (!r.ok) { mostrar(r.erro || "Não foi possível tentar de novo.", true); return; }
  if (r.removido) {
    var antigo = cardsPorId[r.removido];
    if (antigo && antigo.el.parentNode) antigo.el.parentNode.removeChild(antigo.el);
    delete cardsPorId[r.removido];
  }
  (r.cards || []).forEach(pintarCard);
  mostrar("Pedido #" + c.numero + " enviado de novo.");
}
function buscarReceitaPendente() {
  var r = ponte(function () {
    return chromebrothers.receitaParaPreencher ? chromebrothers.receitaParaPreencher(TIPO_DO_GERADOR) : '{"ok":false}';
  });
  if (r && r.ok) preencherComReceita(r);
}
window.__chromebrothersPreencherReceita = buscarReceitaPendente;
function gerar() {
  if (!ta.value.trim() && qtdRefs < 1) {
    mostrar("Escreva um prompt ou escolha uma referência.", true);
    return;
  }
  var mcps = mcpsMarcados();
  if (mcps.length < 1) {
    mostrar("Marque pelo menos um MCP.", true);
    return;
  }
  if (!selModelo.value) {
    mostrar("Escolha um modelo.", true);
    return;
  }
  var q = Math.max(1, Math.min(4, parseInt(selQtd.value, 10) || 1));
  selQtd.value = q;
  var pedido = JSON.stringify({
    mcps: mcps,
    mcp: mcps[0],
    prompt: ta.value,
    modelo: selModelo.value || modeloPadrao(),
    quantidade: q,
    temReferencia: qtdRefs > 0,
    proporcao: selProp.value || "16:9",
    resolucao: selRes.value || "480p",
    duracao: parseInt(selDur.value, 10) || 5
  });
  var r = ponte(function () { return chromebrothers.gerarVideosMcp(pedido); });
  if (!r.ok) { mostrar(r.erro || "Não foi possível gerar.", true); return; }
  var novos = r.cards || [];
  novos.forEach(pintarCard);
  mostrar(novos.length === 1 ? "1 vídeo pedido." : (novos.length + " vídeos pedidos."));
}
pintarRefs({ itens: [] });
document.getElementById("btnGaleria").addEventListener("click", function () { escolher("galeria"); });
document.getElementById("btnBiblioteca").addEventListener("click", function () { escolher("biblioteca"); });
document.getElementById("btnLimpar").addEventListener("click", limpar);
selModelo.addEventListener("change", informarModelo);
btn.addEventListener("click", gerar);
setInterval(atualizarTemposDosCards, 30000);
/** Mostra as referências que o app guarda para este gerador (nada fica escondido entre aberturas). */
function sincronizarRefs() {
  if (typeof chromebrothers === "undefined" || !chromebrothers.listarReferenciasGeracao) return;
  var r = ponte(function () { return chromebrothers.listarReferenciasGeracao(); });
  if (r && r.ok) pintarRefs(r);
}
document.addEventListener("DOMContentLoaded", function () {
  carregarMcps();
  sincronizarRefs();
  restaurarCards();
  buscarReceitaPendente();
});
</script>
</body>
</html>
"""
}

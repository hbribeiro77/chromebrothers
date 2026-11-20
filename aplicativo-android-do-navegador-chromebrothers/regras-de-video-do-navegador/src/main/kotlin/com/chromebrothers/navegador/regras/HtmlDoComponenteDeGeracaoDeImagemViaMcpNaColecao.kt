package com.chromebrothers.navegador.regras

/** HTML empacotado do componente "Geração MCP — imagem" da coleção flutuante. */
object HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao {
    const val ID: String = "geracao-mcp-imagem"
    const val NOME: String = "Geração MCP — imagem"
    val TAGS: List<String> = listOf("mcp", "higgsfield", "sjinn", "imagem")

    fun html(): String = HTML

    private val HTML: String = """<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Geração MCP — imagem</title>
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
.ref.em-exibicao img { outline: 2px solid #f5c518; outline-offset: 1px; }
.ref button { position: absolute; top: 1px; right: 1px; min-height: 22px; padding: 0 6px; font-size: 12px; line-height: 1; }
#contRef { font-size: 12px; color: #4a4f57; }
#status { min-height: 18px; font-size: 13px; color: #4a4f57; }
#status.erro { color: #b00020; }
#grade { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; align-items: start; }
.card { border: 1px solid #c9ced6; border-radius: 8px; background: #fff; padding: 8px; display: flex; flex-direction: column; gap: 6px; min-width: 0; }
.card.em-exibicao { outline: 2px solid #f5c518; outline-offset: 1px; }
.card .cab { display: flex; flex-wrap: wrap; gap: 2px 8px; align-items: baseline; font-size: 12px; color: #4a4f57; min-width: 0; }
.card .cab .num { font-weight: 700; color: #1a1a1a; font-size: 13px; }
.card .cab span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%; }
.card .estado { font-size: 12px; color: #4a4f57; }
.card .estado.erro { color: #b00020; }
.card .acoes { display: flex; gap: 6px; }
.card .acoes button { flex: 1; min-height: 34px; padding: 4px 8px; }
.poster-box { position: relative; }
.poster-box img { width: 100%; border-radius: 6px; border: 1px solid #c9ced6; background: #fff; display: block; }
.poster-vazio { width: 100%; aspect-ratio: 1 / 1; border-radius: 6px; border: 1px solid #c9ced6; background: #222; color: #ccc; display: flex; align-items: center; justify-content: center; text-align: center; padding: 6px; font-size: 13px; }
.badge-new { position: absolute; top: 8px; left: 8px; background: #E53935; color: #fff; font-size: 11px; font-weight: 700; letter-spacing: 0.5px; padding: 2px 8px; border-radius: 10px; pointer-events: none; }
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
<h1>Geração MCP — imagem</h1>
<div class="bloco-ref">
<div class="titulo">MCPs</div>
<div id="mcps"></div>
</div>
<label>Prompt
<textarea id="prompt" maxlength="4000" placeholder="Descreva a imagem"></textarea>
</label>
<div class="bloco-ref">
<div class="titulo">Referências <span id="contRef"></span></div>
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
<option value="1:1" selected>1:1</option>
<option value="16:9">16:9</option>
<option value="9:16">9:16</option>
<option value="4:3">4:3</option>
<option value="3:4">3:4</option>
</select>
</label>
<label>Resolução
<select id="resolucao">
<option value="1k" selected>1k</option>
<option value="2k">2k</option>
<option value="4k">4k</option>
</select>
</label>
<label>Imagens (por MCP)
<select id="qtd">
<option value="1" selected>1</option>
<option value="2">2</option>
<option value="3">3</option>
<option value="4">4</option>
</select>
</label>
</div>
<button type="button" id="btnGerar" class="principal">Gerar imagem</button>
<div id="status" role="status"></div>
<div id="grade"></div>
</main>
<script>
var MODELO_PADRAO = "nano_banana_2_1";
/** Com mais de um MCP, os ids são de família (iguais em todos os MCPs marcados). */
var MODELO_PADRAO_COMUM = "nano-banana-2-1";
var LIMITE_REFS = 4;
var NOMES_DOS_MCPS = { higgsfield: "Higgsfield", sjinn: "Sjinn", magnific: "Magnific" };
var mcpsEl = document.getElementById("mcps");
var cardsPorId = {};
var vistos = {};
var uriEmExibicao = null;
var selModelo = document.getElementById("selModelo");
var selProp = document.getElementById("proporcao");
var selRes = document.getElementById("resolucao");
var ta = document.getElementById("prompt");
var qtd = document.getElementById("qtd");
var btn = document.getElementById("btnGerar");
var elStatus = document.getElementById("status");
var grade = document.getElementById("grade");
var refsEl = document.getElementById("refs");
var contRef = document.getElementById("contRef");
var qtdRefs = 0;

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

function mcpsMarcados() {
  var out = [];
  var inputs = mcpsEl.querySelectorAll('input[type="checkbox"][data-mcp]');
  for (var i = 0; i < inputs.length; i++) {
    if (inputs[i].checked) out.push(String(inputs[i].getAttribute("data-mcp") || "").toLowerCase());
  }
  return out;
}

function selecionarModeloPadrao(anterior) {
  var alvos = [anterior, MODELO_PADRAO, MODELO_PADRAO_COMUM];
  for (var a = 0; a < alvos.length; a++) {
    if (!alvos[a]) continue;
    for (var i = 0; i < selModelo.options.length; i++) {
      if (selModelo.options[i].value === alvos[a]) {
        selModelo.selectedIndex = i;
        return;
      }
    }
  }
}

/** `file://` de cada slot depois de abrir no visor (para a moldura na miniatura certa). */
var urisDasRefs = [];
/** Abre a referência no visor do Visualizador com os bytes exatos que vão ser enviados. */
function abrirReferencia(indice) {
  if (typeof chromebrothers === "undefined" || !chromebrothers.abrirReferenciaNoVisor) return;
  var r = ponte(function () { return chromebrothers.abrirReferenciaNoVisor(String(indice)); });
  if (!r.ok) { mostrar(r.erro || "Não consegui abrir a referência.", true); return; }
  urisDasRefs = r.uris || [];
}
function pintarRefs(r) {
  refsEl.innerHTML = "";
  urisDasRefs = [];
  var itens = (r && r.itens) || [];
  qtdRefs = itens.length;
  contRef.textContent = qtdRefs ? "(" + qtdRefs + "/" + LIMITE_REFS + ")" : "(até " + LIMITE_REFS + ")";
  itens.forEach(function (it, indice) {
    var wrap = document.createElement("div");
    wrap.className = "ref";
    var img = document.createElement("img");
    img.alt = it.nome || ("Ref " + (indice + 1));
    img.src = it.previewDataUrl || "";
    img.addEventListener("click", function () { abrirReferencia(indice); });
    var x = document.createElement("button");
    x.type = "button";
    x.textContent = "×";
    x.addEventListener("click", function () { limparIndice(indice); });
    wrap.appendChild(img);
    wrap.appendChild(x);
    refsEl.appendChild(wrap);
  });
  document.getElementById("btnGaleria").disabled = qtdRefs >= LIMITE_REFS;
  document.getElementById("btnBiblioteca").disabled = qtdRefs >= LIMITE_REFS;
}

function carregarMcps() {
  var r = ponte(function () { return chromebrothers.listarMcpsGeracao(); });
  if (!r.ok) { mostrar(r.erro || "Falha ao listar MCPs.", true); return; }
  mcpsEl.innerHTML = "";
  (r.itens || []).forEach(function (it, indice) {
    var lab = document.createElement("label");
    lab.className = "mcp-opcao";
    var cb = document.createElement("input");
    cb.type = "checkbox";
    cb.setAttribute("data-mcp", it.id);
    if (indice === 0) cb.checked = true;
    cb.addEventListener("change", carregarModelosDepoisDoToque);
    lab.appendChild(cb);
    lab.appendChild(document.createTextNode(it.nome || NOMES_DOS_MCPS[it.id] || it.id));
    mcpsEl.appendChild(lab);
  });
  carregarModelos();
}

function opcaoUnica(valor, texto) {
  selModelo.innerHTML = "";
  var o = document.createElement("option");
  o.value = valor;
  o.textContent = texto;
  selModelo.appendChild(o);
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
  var anterior = selModelo.value;
  if (mcps.length < 1) {
    opcaoUnica("", "Marque um MCP");
    mostrar("Marque pelo menos um MCP.", true);
    return;
  }
  opcaoUnica("", "Carregando…");
  var r = ponte(function () { return chromebrothers.listarModelosGeracao(mcps.join(",")); });
  if (!r.ok) {
    if (mcps.length === 1) opcaoUnica(MODELO_PADRAO, "Erro — Nano Banana 2.1");
    else opcaoUnica("", "Erro ao listar");
    mostrar(r.erro || "Falha ao listar modelos.", true);
    return;
  }
  if (!(r.itens || []).length) {
    opcaoUnica("", "Nenhum modelo em comum");
    mostrar("Nenhum modelo comum aos MCPs marcados.", true);
    return;
  }
  preencher(selModelo, r.itens);
  selecionarModeloPadrao(anterior);
  mostrar(mcps.length > 1 ? "Só aparecem os modelos que existem em todos os MCPs marcados." : "");
}

function aplicarImagem(r) {
  if (!r || !r.ok) { if (r && r.erro) mostrar(r.erro, true); return; }
  if (r.aguardando) { mostrar("Aguardando a escolha da imagem…"); return; }
  if (r.itens) {
    pintarRefs(r);
    mostrar("");
    return;
  }
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
  if (qtdRefs >= LIMITE_REFS) {
    mostrar("No máximo " + LIMITE_REFS + " referências.", true);
    return;
  }
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
  if (c.status === "demorou") return "Demorou: a imagem pode terminar depois. Toque em Verificar.";
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
function marcarVisto(fileUrl) {
  if (!fileUrl || vistos[fileUrl]) return;
  vistos[fileUrl] = true;
  Object.keys(cardsPorId).forEach(function (id) {
    if (cardsPorId[id].dados.fileUrl !== fileUrl) return;
    var badge = cardsPorId[id].el.querySelector(".badge-new");
    if (badge) badge.parentNode.removeChild(badge);
  });
}
function semQuery(u) {
  u = String(u || "");
  var i = u.indexOf("?");
  return i >= 0 ? u.substring(0, i) : u;
}
/** Imagens prontas na ordem em que aparecem na grade (para deslizar no visor). */
function imagensProntasDaGrade() {
  var out = [];
  var els = grade.querySelectorAll("[data-card-id]");
  for (var i = 0; i < els.length; i++) {
    var atual = cardsPorId[els[i].getAttribute("data-card-id")];
    if (atual && atual.dados.status === "pronto" && atual.dados.fileUrl) out.push(atual.dados.fileUrl);
  }
  return out;
}
/** Abre no mesmo visor do Visualizador de mídia; sem a ponte nova, cai no visor da gaveta. */
function abrirNoVisor(fileUrl) {
  marcarVisto(fileUrl);
  if (typeof chromebrothers === "undefined") return;
  if (chromebrothers.abrirImagensNoVisorDaColecao) {
    var lista = imagensProntasDaGrade();
    var indice = Math.max(0, lista.indexOf(fileUrl));
    if (!lista.length) lista = [fileUrl];
    var r = ponte(function () { return chromebrothers.abrirImagensNoVisorDaColecao(JSON.stringify(lista), String(indice)); });
    if (r.ok) return;
  }
  if (chromebrothers.abrirImagemNoVisor) chromebrothers.abrirImagemNoVisor(fileUrl);
}
/** Moldura na miniatura que está aberta no visor; null limpa. Chamado pelo app. */
window.__chromebrothersImagemNoVisor = function (uri) {
  uriEmExibicao = uri ? semQuery(uri) : null;
  if (uriEmExibicao) marcarVisto(uriEmExibicao);
  var refs = refsEl.querySelectorAll(".ref");
  for (var k = 0; k < refs.length; k++) {
    refs[k].classList.toggle("em-exibicao", !!uriEmExibicao && semQuery(urisDasRefs[k]) === uriEmExibicao);
  }
  Object.keys(cardsPorId).forEach(function (id) {
    var atual = cardsPorId[id];
    var emExibicao = !!uriEmExibicao && semQuery(atual.dados.fileUrl) === uriEmExibicao;
    atual.el.classList.toggle("em-exibicao", emExibicao);
    if (emExibicao && atual.el.scrollIntoView) atual.el.scrollIntoView({ block: "nearest", behavior: "smooth" });
  });
};
function pintarCardPronto(el, c) {
  el.className = "card" + (uriEmExibicao && semQuery(c.fileUrl) === uriEmExibicao ? " em-exibicao" : "");
  el.appendChild(cabecalhoDoCard(c));
  var caixa = document.createElement("div");
  caixa.className = "poster-box";
  var img = document.createElement("img");
  img.alt = "Imagem #" + c.numero;
  // WebView com base https:// não carrega file:// no src: prévia no src, arquivo no data-cb-uri (arraste ao editor).
  img.src = c.previewDataUrl || "";
  img.setAttribute("data-cb-uri", c.fileUrl);
  img.addEventListener("click", function () { abrirNoVisor(c.fileUrl); });
  caixa.appendChild(img);
  if (!vistos[c.fileUrl]) {
    var novo = document.createElement("span");
    novo.className = "badge-new";
    novo.textContent = "NEW";
    caixa.appendChild(novo);
  }
  el.appendChild(caixa);
  var acoes = document.createElement("div");
  acoes.className = "acoes";
  acoes.appendChild(botao("Usar de novo", function () { usarDeNovo(c); }));
  // Tira só do gerador: a imagem continua na biblioteca.
  acoes.appendChild(botao("Remover", function () { removerCard(c.id); }));
  el.appendChild(acoes);
}
function pintarCardSkeleton(el, c) {
  el.className = "card card-skeleton" + (emAndamento(c) || c.verificando ? "" : " parado");
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
  var r = ponte(function () { return chromebrothers.verificarCardDeImagemMcp(id); });
  if (!r.ok) {
    atual.dados.verificando = false;
    atual.dados.mensagem = r.erro || "Não foi possível verificar.";
    pintarCard(atual.dados);
  }
}
function removerCard(id) {
  ponte(function () { return chromebrothers.removerCardDeImagemMcp(id); });
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
  var r = ponte(function () { return chromebrothers.listarCardsDeImagemMcp(); });
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
    else if (r.card.status === "pronto") mostrar("#" + r.card.numero + " pronta e salva na biblioteca.");
    return;
  }
  if (r && !r.ok && r.erro) mostrar(r.erro, true);
};

var TIPO_DO_GERADOR = "imagem";
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
  if (p.quantidade) escolherOpcao(qtd, p.quantidade);
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
  var r = ponte(function () { return chromebrothers.repetirCardDeImagemMcp(c.id); });
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
  if (!ta.value.trim()) { mostrar("Escreva um prompt.", true); return; }
  var mcps = mcpsMarcados();
  if (mcps.length < 1) { mostrar("Marque pelo menos um MCP.", true); return; }
  if (!selModelo.value) { mostrar("Escolha um modelo.", true); return; }
  var q = Math.max(1, Math.min(4, parseInt(qtd.value, 10) || 1));
  qtd.value = q;
  var pedido = JSON.stringify({
    mcps: mcps,
    mcp: mcps[0],
    prompt: ta.value,
    modelo: selModelo.value,
    quantidade: q,
    temReferencia: qtdRefs > 0,
    proporcao: selProp.value || "1:1",
    resolucao: selRes.value || "1k"
  });
  var r = ponte(function () { return chromebrothers.gerarImagensMcp(pedido); });
  if (!r.ok) { mostrar(r.erro || "Não foi possível gerar.", true); return; }
  var novos = r.cards || [];
  novos.forEach(pintarCard);
  mostrar(novos.length === 1 ? "1 imagem pedida." : (novos.length + " imagens pedidas."));
}

pintarRefs({ itens: [] });
document.getElementById("btnGaleria").addEventListener("click", function () { escolher("galeria"); });
document.getElementById("btnBiblioteca").addEventListener("click", function () { escolher("biblioteca"); });
document.getElementById("btnLimpar").addEventListener("click", limpar);
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

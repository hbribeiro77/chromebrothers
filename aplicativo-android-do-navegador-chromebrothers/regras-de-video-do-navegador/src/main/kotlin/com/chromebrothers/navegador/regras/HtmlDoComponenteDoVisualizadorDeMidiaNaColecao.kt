package com.chromebrothers.navegador.regras

/**
 * HTML nativo do Visualizador de mídia — galeria já montada (sem inject que esconde pasta/PDF).
 * Semente idempotente atualiza o item da biblioteca no aparelho.
 */
object HtmlDoComponenteDoVisualizadorDeMidiaNaColecao {
    const val ID: String = "visualizador-de-midia"
    const val NOME: String = "Visualizador de mídia"
    val TAGS: List<String> = listOf(
        "galeria",
        "midia",
        "chromeBrothers",
        "warm-start",
    )

    /** Lado (px) da miniatura `?cb_thumb=` da grade; a pré-geração nativa usa o mesmo valor. */
    const val LADO_DA_MINIATURA_DA_GRADE: Int = 192

    fun html(): String = HTML

    private val HTML: String = """
<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Visualizador de mídia</title>
<style>
*{box-sizing:border-box}
html,body{margin:0;height:100%;background:#1a1d24;color:#e8eaf0;
  font:12.5px/1.25 system-ui,-apple-system,"Segoe UI",sans-serif}
#barra{position:sticky;top:0;z-index:20;display:flex;flex-wrap:wrap;gap:6px;align-items:center;
  padding:10px 12px;background:#1b1d24;border-bottom:1px solid #2e323d}
.btn{display:inline-flex;align-items:center;gap:7px;padding:7px 11px;border-radius:10px;
  border:1px solid #2e323d;background:#22252e;color:#e8eaf0;font:inherit;cursor:pointer}
.btn:hover{border-color:#3a3f4d;background:#262a34}
.btn-acc{background:#f5c518;color:#191a1f;border-color:transparent;font-weight:600}
.btn-danger{background:#ff6b6b1c;border-color:#ff6b6b3d;color:#ff8f8f;display:none}
#filtros{display:inline-flex;gap:2px;padding:2px;background:#12141a;border:1px solid #2e323d;border-radius:10px}
#filtros button{border:0;background:transparent;color:#8b91a1;font:12px/1 inherit;padding:6px 10px;border-radius:7px;cursor:pointer}
#filtros button.ativo{background:#22252e;color:#e8eaf0;box-shadow:inset 0 1px 0 #ffffff14}
#maiores.ligado{color:#f5c518;background:#f5c51814;border-color:#f5c5183d}
#contagem{margin-left:auto;color:#8b91a1;font-size:11.5px;display:none}
#status{padding:8px 12px;color:#8b91a1;font-size:12px}
#grade{display:grid;gap:8px;padding:10px 12px 24px;grid-template-columns:repeat(auto-fill,minmax(108px,1fr))}
body.grandes #grade{grid-template-columns:repeat(2,minmax(0,1fr))}
.carta{position:relative;margin:0;border-radius:12px;overflow:hidden;background:#14161c;
  border:1px solid #2a2e38;aspect-ratio:1}
.carta img,.carta video{width:100%;height:100%;object-fit:cover;display:block;background:#0a0d12;
  -webkit-user-drag:none;user-select:none;-webkit-user-select:none;-webkit-touch-callout:none}
.carta .nome{position:absolute;left:0;right:0;bottom:0;padding:18px 7px 6px;font-size:11px;
  background:linear-gradient(transparent,rgba(8,10,14,.92));white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.carta.video .play{position:absolute;inset:0;display:grid;place-items:center;pointer-events:none}
.carta.video .play span{width:36px;height:36px;border-radius:50%;background:rgba(6,18,31,.72);
  display:grid;place-items:center;color:#f5c518;font-size:14px}
.marca{position:absolute;top:6px;left:6px;z-index:5;width:22px;height:22px;border-radius:6px;
  border:1.5px solid #e8eaf0;background:rgba(15,16,20,.45);display:none;align-items:center;
  justify-content:center;color:#f5c518;font:14px/1 sans-serif}
body.selecao .marca{display:flex}
.carta.marcada .marca{background:#f5c518;border-color:#f5c518;color:#191a1f}
.carta.marcada .marca::after{content:"✓"}
body.selecao .carta.marcada{outline:2px solid #f5c518;outline-offset:1px}
.carta.em-foco{outline:2px solid #f5c518;outline-offset:1px;z-index:3}
.carta.filtro-oculto{display:none!important}
body.cb-viz-aguarda-largura #grade{opacity:0;pointer-events:none}
body.cb-viz-aguarda-largura #status{opacity:0}
body.cb-viz-carregando-thumbs #grade{opacity:0;pointer-events:none}
</style>
</head>
<body>
<div id="barra">
  <button type="button" class="btn" id="btnSelecionar">Selecionar</button>
  <span id="filtros">
    <button type="button" data-f="tudo" class="ativo">Tudo</button>
    <button type="button" data-f="imagens">Imagens</button>
    <button type="button" data-f="videos">Vídeos</button>
  </span>
  <button type="button" class="btn" id="maiores">Maiores</button>
  <button type="button" class="btn btn-danger" id="btnExcluir">Excluir</button>
  <span id="contagem"></span>
</div>
<div id="status">Carregando mídia…</div>
<div id="grade" aria-live="polite"></div>
<script>
(function(){
  var modo = false, marcados = {}, filtro = 'tudo', grandes = false, itens = [];
  var grade = document.getElementById('grade');
  var statusEl = document.getElementById('status');
  var contagem = document.getElementById('contagem');
  var btnSel = document.getElementById('btnSelecionar');
  var btnExc = document.getElementById('btnExcluir');
  var btnMai = document.getElementById('maiores');

  function api(){ return window.chromebrothers || null; }
  function limparQ(u){ if(!u) return ''; var i=u.indexOf('?'); return i>=0?u.substring(0,i):u; }
  function comThumb(uri){
    if(!uri) return uri;
    if(uri.indexOf('cb_thumb=')>=0) return uri;
    if(uri.indexOf('content:')!==0 && uri.indexOf('file:')!==0) return uri;
    return uri + (uri.indexOf('?')>=0?'&':'?') + 'cb_thumb=192';
  }
  function ehVideo(it){
    var m=(it.mime||'').toLowerCase();
    if(m.indexOf('video/')===0) return true;
    return /\.(mp4|webm|mkv|mov|m4v|avi)(\?|$)/i.test(it.nome||it.uri||'');
  }
  function normalizar(it){
    if(!it) return null;
    if(typeof it==='string') it={uri:it,nome:it.split('/').pop()};
    var uri=it.uri||it.url||it.endereco||it.path||'';
    if(!uri) return null;
    var pasta=!!(it.pasta||it.isPasta||it.dir);
    return {
      nome: it.nome||it.name||uri.split('/').pop()||'(sem nome)',
      uri: uri,
      mime: it.mime||it.tipo||it.type||'',
      pasta: pasta,
      tamanho: it.tamanho||it.size||0
    };
  }
  function lerPrefs(){
    try{
      filtro = localStorage.getItem('cb_viz_filtro') || 'tudo';
      grandes = localStorage.getItem('cb_viz_grandes') === '1';
    }catch(e){}
  }
  function gravarFiltro(f){
    try{ localStorage.setItem('cb_viz_filtro', f); }catch(e){}
  }
  function gravarGrandes(v){
    try{ localStorage.setItem('cb_viz_grandes', v?'1':'0'); }catch(e){}
  }
  function atualizarBarra(){
    btnSel.className = 'btn' + (modo?' btn-acc':'');
    btnSel.textContent = modo ? 'Pronto' : 'Selecionar';
    btnExc.style.display = modo ? 'inline-flex' : 'none';
    document.body.classList.toggle('selecao', modo);
    document.body.classList.toggle('grandes', grandes);
    btnMai.classList.toggle('ligado', grandes);
    var n=Object.keys(marcados).length;
    contagem.style.display = (modo && n) ? 'inline' : 'none';
    contagem.textContent = n ? (n+' selecionado'+(n>1?'s':'')) : '';
    var botoes=document.querySelectorAll('#filtros button');
    for(var i=0;i<botoes.length;i++){
      botoes[i].classList.toggle('ativo', botoes[i].getAttribute('data-f')===filtro);
    }
  }
  function aplicarFiltro(){
    var cartas=grade.querySelectorAll('.carta');
    for(var i=0;i<cartas.length;i++){
      var c=cartas[i], tipo=c.getAttribute('data-tipo')||'outro';
      var ok = filtro==='tudo' || (filtro==='imagens'&&tipo==='imagem') || (filtro==='videos'&&tipo==='video');
      c.classList.toggle('filtro-oculto', !ok);
    }
  }
  function uriDaCarta(carta){
    return carta.getAttribute('data-uri')||'';
  }
  window.cbVizLimparDestaqueCarta = function(){
    var cs=grade.querySelectorAll('.carta.em-foco');
    for(var i=0;i<cs.length;i++) cs[i].classList.remove('em-foco');
  };
  window.cbVizDestacarCarta = function(uri){
    window.cbVizLimparDestaqueCarta();
    if(!uri) return;
    var alvo=limparQ(uri);
    var cs=grade.querySelectorAll('.carta');
    for(var i=0;i<cs.length;i++){
      if(limparQ(uriDaCarta(cs[i]))===alvo){ cs[i].classList.add('em-foco'); break; }
    }
  };
  var preparandoMiniaturas = false;
  function liberarGradeSePronto(){
    if(preparandoMiniaturas) return;
    if(document.body.classList.contains('cb-viz-aguarda-largura')) return;
    statusEl.style.display='none';
  }
  window.cbVizAvisarLarguraEstavel = function(){
    document.body.classList.remove('cb-viz-aguarda-largura');
    try{ window.dispatchEvent(new Event('resize')); }catch(e){}
    liberarGradeSePronto();
  };
  window.cbVizListarMidiasVisiveis = function(){
    var out=[];
    var cs=grade.querySelectorAll('.carta:not(.filtro-oculto)');
    for(var i=0;i<cs.length;i++){
      out.push({uri:uriDaCarta(cs[i]), tipo:cs[i].getAttribute('data-tipo')||'imagem'});
    }
    return JSON.stringify(out);
  };
  function textoAvisoCorte(meta){
    if(!meta||!meta.cortados) return '';
  var tot = meta.total||meta.qtd||0;
  if(tot>0) return 'Mostrando '+meta.qtd+' de '+tot+' (mais recentes).';
  return 'Mostrando os '+meta.qtd+' itens mais recentes.';
  }
  function aguardarMiniaturasProntas(imgs, ocultarGrade, concluir){
    var maxMs=45000;
    var t0=Date.now();
    var pendentes=[];
    for(var i=0;i<imgs.length;i++){
      if(!(imgs[i].complete && imgs[i].naturalWidth>0)) pendentes.push(imgs[i]);
    }
    if(!pendentes.length){
      if(typeof concluir==='function') concluir();
      liberarGradeSePronto();
      return;
    }
    if(ocultarGrade){
      preparandoMiniaturas=true;
      document.body.classList.add('cb-viz-carregando-thumbs');
      statusEl.style.display='block';
      statusEl.textContent='Preparando miniaturas…';
    }
    function tick(){
      var restantes=[];
      for(var j=0;j<pendentes.length;j++){
        var img=pendentes[j];
        if(!img.complete) restantes.push(img);
      }
      pendentes=restantes;
      if(!pendentes.length || Date.now()-t0>maxMs) finalizar();
      else setTimeout(tick, 60);
    }
    function finalizar(){
      preparandoMiniaturas=false;
      document.body.classList.remove('cb-viz-carregando-thumbs');
      if(typeof concluir==='function') concluir();
      liberarGradeSePronto();
    }
    tick();
  }
  function toggleMarca(carta){
    var u=limparQ(uriDaCarta(carta));
    if(!u) return;
    if(marcados[u]){ delete marcados[u]; carta.classList.remove('marcada'); }
    else { marcados[u]=true; carta.classList.add('marcada'); }
    atualizarBarra();
  }
  var metaListagem={cortados:0,total:0,qtd:0};
  function criarCarta(it){
    var video=ehVideo(it);
    var carta=document.createElement('div');
    carta.className='carta'+(video?' video':'');
    carta.setAttribute('data-uri', it.uri);
    carta.setAttribute('data-cb-uri', limparQ(it.uri));
    carta.setAttribute('data-tipo', video?'video':'imagem');
    carta.setAttribute('data-mime', it.mime||'');
    var marca=document.createElement('div');
    marca.className='marca';
    carta.appendChild(marca);
    var img=document.createElement('img');
    img.setAttribute('data-cb-uri', limparQ(it.uri));
    img.setAttribute('draggable', 'false');
    img.loading='eager';
    img.decoding='sync';
    img.alt=it.nome||'';
    img.src=comThumb(it.uri);
    carta.appendChild(img);
    if(video){
      var play=document.createElement('div');
      play.className='play';
      play.innerHTML='<span>▶</span>';
      carta.appendChild(play);
    }
    var nome=document.createElement('div');
    nome.className='nome';
    nome.textContent=it.nome||'';
    carta.appendChild(nome);
    carta.addEventListener('click', function(ev){
      if(modo){ ev.preventDefault(); ev.stopPropagation(); toggleMarca(carta); }
    }, true);
    return carta;
  }
  grade.addEventListener('dragstart', function(ev){ ev.preventDefault(); }, true);
  function desenhar(lista, meta){
    itens = lista || [];
    metaListagem = meta || metaListagem;
    var midias = itens.filter(function(it){ return !it.pasta; });
    if(!midias.length){
      grade.innerHTML='';
      marcados={};
      preparandoMiniaturas=false;
      document.body.classList.remove('cb-viz-carregando-thumbs');
      statusEl.textContent = 'Nenhuma mídia em ChromeBrothers.';
      statusEl.style.display='block';
      atualizarBarra();
      return;
    }
    var existentes={};
    var atuais=grade.querySelectorAll('.carta');
    var primeiraVez=!atuais.length;
    for(var i=0;i<atuais.length;i++) existentes[atuais[i].getAttribute('data-uri')]=atuais[i];
    var novasImgs=[];
    var ordem=[];
    for(var k=0;k<midias.length;k++){
      var it=midias[k];
      var carta=existentes[it.uri];
      if(carta){
        delete existentes[it.uri];
      } else {
        carta=criarCarta(it);
        novasImgs.push(carta.querySelector('img'));
      }
      ordem.push(carta);
    }
    for(var u in existentes){
      var sobra=existentes[u];
      if(sobra.parentNode) sobra.parentNode.removeChild(sobra);
      delete marcados[limparQ(u)];
    }
    var atual=grade.firstElementChild;
    for(var m=0;m<ordem.length;m++){
      if(atual===ordem[m]){ atual=atual.nextElementSibling; continue; }
      grade.insertBefore(ordem[m], atual);
    }
    aplicarFiltro();
    atualizarBarra();
    metaListagem.qtd = midias.length;
    aguardarMiniaturasProntas(novasImgs, primeiraVez, function(){
      if(metaListagem.cortados>0){
        statusEl.style.display='block';
        statusEl.textContent=textoAvisoCorte(metaListagem);
        setTimeout(function(){ if(!preparandoMiniaturas) liberarGradeSePronto(); }, 2200);
      }
    });
  }
  function receberPasta(j){
    if(!j||j.ok===false){
      statusEl.style.display='block';
      statusEl.textContent=(j&&j.motivo)||'Não deu para listar a pasta.';
      return;
    }
    var brutos = j.itens||j.items||j.arquivos||[];
    var lista=[];
    for(var i=0;i<brutos.length;i++){
      var n=normalizar(brutos[i]);
      if(n) lista.push(n);
    }
    var cortados = Number(j.cortados)||0;
    var total = Number(j.totalEncontrado)||(lista.length+cortados);
    desenhar(lista, {cortados:cortados, total:total, qtd:lista.length});
  }
  window.chromebrothersAoEscolherPasta = receberPasta;

  function pedirLista(){
    var a=api();
    if(!a||typeof a.listarPasta!=='function') return;
    try{ if(typeof a.gradeNativaAtiva==='function' && a.gradeNativaAtiva()) return; }catch(e){}
    try{
      var ret=a.listarPasta('chromebrothers://midia');
      var j=null;
      try{ j=JSON.parse(ret||'{}'); }catch(e){}
      if(j) receberPasta(j);
    }catch(e){}
  }

  btnSel.addEventListener('click', function(){
    modo=!modo;
    if(!modo){
      marcados={};
      var cs=grade.querySelectorAll('.carta.marcada');
      for(var i=0;i<cs.length;i++) cs[i].classList.remove('marcada');
    }
    atualizarBarra();
  });
  btnMai.addEventListener('click', function(){
    grandes=!grandes;
    gravarGrandes(grandes);
    atualizarBarra();
  });
  document.getElementById('filtros').addEventListener('click', function(ev){
    var b=ev.target.closest('button[data-f]');
    if(!b) return;
    filtro=b.getAttribute('data-f')||'tudo';
    gravarFiltro(filtro);
    aplicarFiltro();
    atualizarBarra();
  });
  btnExc.addEventListener('click', function(){
    var uris=Object.keys(marcados);
    if(!uris.length) return;
    var a=api();
    if(!a||typeof a.excluirArquivos!=='function') return;
    var ret;
    try{ ret=a.excluirArquivos(JSON.stringify(uris)); }catch(e){ return; }
    var ok=false;
    try{ var o=JSON.parse(ret||'{}'); ok=!!o.ok; }catch(e){}
    if(!ok) return;
    itens = itens.filter(function(it){ return !marcados[limparQ(it.uri)]; });
    marcados={};
    desenhar(itens);
  });

  window.cbVizPrefetchThumbs = function(){ /* substituído por aguardarMiniaturasProntas */ };

  lerPrefs();
  atualizarBarra();
  // Fallback se o nativo ainda não empurrar a pasta.
  setTimeout(pedirLista, 80);
})();
</script>
</body>
</html>
""".trimIndent()
}

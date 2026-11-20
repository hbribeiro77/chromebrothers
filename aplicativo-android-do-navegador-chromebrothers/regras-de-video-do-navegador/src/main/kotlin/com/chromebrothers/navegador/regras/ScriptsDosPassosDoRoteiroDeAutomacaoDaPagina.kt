package com.chromebrothers.navegador.regras

/**
 * Scripts JS síncronos (uma tentativa) usados pelo executor com polling.
 * Retorno: JSON {"ok":true} ou {"ok":false,"motivo":"..."}.
 */
object ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina {
    fun aguardar(seletor: String, texto: String): String {
        val sel = TextoJsonDoChat.escapar(seletor)
        val txt = TextoJsonDoChat.escapar(texto)
        return """
            (function(){
              function textoDe(el){
                return (el.innerText||el.textContent||el.value||el.getAttribute('aria-label')||el.getAttribute('title')||'').replace(/\s+/g,' ').trim();
              }
              function bate(el,t){
                var v=textoDe(el);
                return !!(v && v.indexOf(t)>=0);
              }
              function achar(){
                var s='$sel', t='$txt';
                if(s){
                  try{
                    var lista=document.querySelectorAll(s);
                    if(t){
                      for(var i=0;i<lista.length;i++){ if(bate(lista[i],t)) return lista[i]; }
                    } else if(lista.length){
                      return lista[0];
                    }
                  }catch(_){}
                }
                if(t){
                  var preferidos=document.querySelectorAll('button,a,input,textarea,[role="button"],[role="radio"],[role="checkbox"],label');
                  for(var j=0;j<preferidos.length;j++){ if(bate(preferidos[j],t)) return preferidos[j]; }
                  var n=document.querySelectorAll('span,div,p,h1,h2,h3,li');
                  for(var k=0;k<n.length;k++){ if(bate(n[k],t)) return n[k]; }
                }
                return null;
              }
              var el=achar();
              if(!el) return JSON.stringify({ok:false,motivo:'nao_achou'});
              try{
                el.scrollIntoView({block:'center',inline:'nearest'});
              }catch(_){}
              return JSON.stringify({ok:true});
            })();
        """.trimIndent()
    }

    fun clicar(seletor: String, texto: String): String {
        val sel = TextoJsonDoChat.escapar(seletor)
        val txt = TextoJsonDoChat.escapar(texto)
        return """
            (function(){
              function textoDe(el){
                return (el.innerText||el.textContent||el.value||el.getAttribute('aria-label')||el.getAttribute('title')||'').replace(/\s+/g,' ').trim();
              }
              function bate(el,t){
                var v=textoDe(el);
                return !!(v && v.indexOf(t)>=0);
              }
              function visivel(el){
                try{
                  var r=el.getBoundingClientRect();
                  return r.width>=2 && r.height>=2 && r.bottom>0 && r.right>0 &&
                    r.top<(window.innerHeight||9999) && r.left<(window.innerWidth||9999);
                }catch(_){ return true; }
              }
              function pontuar(el,t){
                var tag=(el.tagName||'').toLowerCase();
                var role=(el.getAttribute('role')||'').toLowerCase();
                var type=(el.getAttribute('type')||'').toLowerCase();
                var v=textoDe(el);
                var score=0;
                if(tag==='button'||role==='button'||role==='radio'||role==='checkbox'||role==='switch') score+=120;
                else if(tag==='input'||tag==='a'||tag==='label'||tag==='summary') score+=90;
                else if(tag==='span'||tag==='div') score+=10;
                if(type==='radio'||type==='checkbox'||type==='submit'||type==='button') score+=40;
                if(visivel(el)) score+=25;
                if(v.length && v.length<=t.length+24) score+=20;
                score+=Math.max(0, 30-Math.min(30, Math.floor(v.length/6)));
                return score;
              }
              function melhor(candidatos,t){
                if(!candidatos.length) return null;
                var best=candidatos[0], bestScore=pontuar(best,t||'');
                for(var i=1;i<candidatos.length;i++){
                  var s=pontuar(candidatos[i],t||'');
                  if(s>bestScore){ best=candidatos[i]; bestScore=s; }
                }
                return best;
              }
              function achar(){
                var s='$sel', t='$txt';
                var candidatos=[];
                if(s){
                  try{
                    var lista=document.querySelectorAll(s);
                    if(t){
                      for(var i=0;i<lista.length;i++){ if(bate(lista[i],t)) candidatos.push(lista[i]); }
                    } else {
                      for(var i2=0;i2<lista.length;i2++) candidatos.push(lista[i2]);
                    }
                  }catch(_){}
                }
                if(!candidatos.length && t){
                  var preferidos=document.querySelectorAll('button,a,input,[role="button"],[role="radio"],[role="checkbox"],[role="switch"],label,summary');
                  for(var j=0;j<preferidos.length;j++){ if(bate(preferidos[j],t)) candidatos.push(preferidos[j]); }
                  if(!candidatos.length){
                    var n=document.querySelectorAll('span,div,p,li,h1,h2,h3');
                    for(var k=0;k<n.length;k++){ if(bate(n[k],t)) candidatos.push(n[k]); }
                  }
                }
                return {el:melhor(candidatos,t), n:candidatos.length};
              }
              var achado=achar();
              var el=achado.el;
              if(!el) return JSON.stringify({ok:false,motivo:'nao_achou',matches:0});
              try{ el.scrollIntoView({block:'center',inline:'nearest'}); }catch(_){}
              try{ el.focus(); }catch(_){}
              try{ el.click(); }catch(e){
                try{
                  el.dispatchEvent(new MouseEvent('click',{bubbles:true,cancelable:true,view:window}));
                }catch(e2){
                  return JSON.stringify({ok:false,motivo:'falha_click',matches:achado.n});
                }
              }
              return JSON.stringify({
                ok:true,
                matches:achado.n,
                tag:(el.tagName||'').toLowerCase(),
                texto:textoDe(el).slice(0,80)
              });
            })();
        """.trimIndent()
    }

    fun preencher(seletor: String, texto: String, valor: String): String {
        val sel = TextoJsonDoChat.escapar(seletor)
        val txt = TextoJsonDoChat.escapar(texto)
        val valEsc = TextoJsonDoChat.escapar(valor)
        return """
            (function(){
              function achar(){
                var s='$sel', t='$txt';
                if(s){ try{ var e=document.querySelector(s); if(e) return e; }catch(_){} }
                if(t){
                  var n=document.querySelectorAll('input,textarea,[contenteditable="true"],[contenteditable=""],[role="textbox"],.ProseMirror');
                  for(var i=0;i<n.length;i++){
                    var el=n[i];
                    var lab=(el.getAttribute('aria-label')||el.getAttribute('placeholder')||el.id||(el.innerText||'')||'').trim();
                    if(lab && lab.indexOf(t)>=0) return el;
                  }
                }
                return document.activeElement;
              }
              function inserirEditavel(el, v){
                try{ el.focus(); }catch(_){}
                try{
                  var sel=window.getSelection();
                  var faixa=document.createRange();
                  faixa.selectNodeContents(el);
                  faixa.collapse(false);
                  if(sel){ sel.removeAllRanges(); sel.addRange(faixa); }
                }catch(_){}
                var ok=false;
                try{ ok=document.execCommand('insertText', false, v); }catch(_){}
                if(!ok){
                  try{
                    el.textContent=(el.textContent||'')+v;
                    el.dispatchEvent(new InputEvent('input',{bubbles:true,data:v,inputType:'insertText'}));
                    ok=true;
                  }catch(_){}
                } else {
                  try{ el.dispatchEvent(new InputEvent('input',{bubbles:true,data:v,inputType:'insertText'})); }catch(_){}
                }
                return ok;
              }
              var el=achar();
              if(!el) return JSON.stringify({ok:false,motivo:'nao_achou'});
              var v='$valEsc';
              try{ el.focus(); }catch(_){}
              if(el.isContentEditable || (el.getAttribute && (el.getAttribute('contenteditable')==='true' || el.getAttribute('contenteditable')==='' || el.getAttribute('role')==='textbox')) || (el.classList && el.classList.contains('ProseMirror'))){
                var ok=inserirEditavel(el, v);
                return JSON.stringify({ok:!!ok});
              }
              var proto=el.tagName==='TEXTAREA'?HTMLTextAreaElement.prototype:HTMLInputElement.prototype;
              var desc=Object.getOwnPropertyDescriptor(proto,'value');
              if(desc && desc.set) desc.set.call(el,v); else el.value=v;
              el.dispatchEvent(new Event('input',{bubbles:true}));
              el.dispatchEvent(new Event('change',{bubbles:true}));
              return JSON.stringify({ok:true});
            })();
        """.trimIndent()
    }

    fun rolar(seletor: String, pixels: Int): String {
        val sel = TextoJsonDoChat.escapar(seletor)
        return """
            (function(){
              var s='$sel', p=$pixels;
              if(s){
                try{
                  var e=document.querySelector(s);
                  if(e){ e.scrollIntoView({block:'center',inline:'nearest'}); return JSON.stringify({ok:true}); }
                }catch(_){}
                return JSON.stringify({ok:false,motivo:'nao_achou'});
              }
              try{ window.scrollBy(0,p); }catch(_){ return JSON.stringify({ok:false,motivo:'falha_scroll'}); }
              return JSON.stringify({ok:true});
            })();
        """.trimIndent()
    }

    fun clicarSe(seletor: String, indice: Int, contem: String): String {
        val sel = TextoJsonDoChat.escapar(seletor)
        val txt = TextoJsonDoChat.escapar(contem)
        return """
            (function(){
              var s='$sel', i=$indice, t='$txt';
              var lista;
              try{ lista=document.querySelectorAll(s); }catch(_){
                return JSON.stringify({ok:false,motivo:'seletor_invalido'});
              }
              if(!lista || !lista.length || i>=lista.length){
                return JSON.stringify({ok:false,motivo:'nao_achou'});
              }
              var el=lista[i];
              var v=(el.innerText||el.textContent||el.value||el.getAttribute('aria-label')||el.getAttribute('href')||'').trim();
              if(!v || v.indexOf(t)<0){
                return JSON.stringify({ok:false,motivo:'nao_bateu',texto:String(v).slice(0,120)});
              }
              try{ el.scrollIntoView({block:'center',inline:'nearest'}); }catch(_){}
              try{ el.focus(); }catch(_){}
              try{ el.click(); }catch(e){
                try{
                  el.dispatchEvent(new MouseEvent('click',{bubbles:true,cancelable:true,view:window}));
                }catch(e2){
                  return JSON.stringify({ok:false,motivo:'falha_click'});
                }
              }
              return JSON.stringify({ok:true});
            })();
        """.trimIndent()
    }

    fun aguardarSumir(seletor: String, texto: String): String {
        val sel = TextoJsonDoChat.escapar(seletor)
        val txt = TextoJsonDoChat.escapar(texto)
        return """
            (function(){
              function achar(){
                var s='$sel', t='$txt';
                if(s){ try{ var e=document.querySelector(s); if(e) return e; }catch(_){} }
                if(t){
                  var n=document.querySelectorAll('a,button,input,textarea,[role="button"],label,span,div,p,h1,h2,h3,li');
                  for(var i=0;i<n.length;i++){
                    var el=n[i];
                    var v=(el.innerText||el.textContent||el.value||el.getAttribute('aria-label')||'').trim();
                    if(v && v.indexOf(t)>=0) return el;
                  }
                }
                return null;
              }
              var el=achar();
              if(el) return JSON.stringify({ok:false,motivo:'ainda_la'});
              return JSON.stringify({ok:true});
            })();
        """.trimIndent()
    }

    fun assertar(seletor: String, texto: String, contem: String): String {
        val sel = TextoJsonDoChat.escapar(seletor)
        val txt = TextoJsonDoChat.escapar(texto)
        val ct = TextoJsonDoChat.escapar(contem)
        return """
            (function(){
              function achar(){
                var s='$sel', t='$txt';
                if(s){ try{ var e=document.querySelector(s); if(e) return e; }catch(_){} }
                if(t){
                  var n=document.querySelectorAll('a,button,input,textarea,[role="button"],label,span,div,p,h1,h2,h3,li');
                  for(var i=0;i<n.length;i++){
                    var el=n[i];
                    var v=(el.innerText||el.textContent||el.value||el.getAttribute('aria-label')||'').trim();
                    if(v && v.indexOf(t)>=0) return el;
                  }
                }
                return null;
              }
              var el=achar();
              if(!el) return JSON.stringify({ok:false,motivo:'nao_achou'});
              var c='$ct';
              if(c){
                var v=(el.innerText||el.textContent||el.value||el.getAttribute('aria-label')||'').trim();
                if(!v || v.indexOf(c)<0) return JSON.stringify({ok:false,motivo:'nao_bateu',texto:String(v).slice(0,120)});
              }
              return JSON.stringify({ok:true});
            })();
        """.trimIndent()
    }

    fun extrair(seletor: String, texto: String): String {
        val sel = TextoJsonDoChat.escapar(seletor)
        val txt = TextoJsonDoChat.escapar(texto)
        return """
            (function(){
              function achar(){
                var s='$sel', t='$txt';
                if(s){ try{ var e=document.querySelector(s); if(e) return e; }catch(_){} }
                if(t){
                  var n=document.querySelectorAll('a,button,input,textarea,[role="button"],label,span,div,p,h1,h2,h3,li');
                  for(var i=0;i<n.length;i++){
                    var el=n[i];
                    var v=(el.innerText||el.textContent||el.value||el.getAttribute('aria-label')||'').trim();
                    if(v && v.indexOf(t)>=0) return el;
                  }
                }
                return null;
              }
              var el=achar();
              if(!el) return JSON.stringify({ok:false,motivo:'nao_achou'});
              var v=(el.value||el.innerText||el.textContent||el.getAttribute('aria-label')||'').trim();
              return JSON.stringify({ok:true,texto:String(v).slice(0,4000)});
            })();
        """.trimIndent()
    }

    fun aguardarQualquer(opcoes: List<OpcaoDoAguardarQualquer>): String {
        val lista = opcoes.joinToString(",") { op ->
            val sel = TextoJsonDoChat.escapar(op.seletor)
            val txt = TextoJsonDoChat.escapar(op.texto)
            val id = TextoJsonDoChat.escapar(op.id)
            "{id:'$id',s:'$sel',t:'$txt'}"
        }
        return """
            (function(){
              var ops=[$lista];
              function textoDe(el){
                return (el.innerText||el.textContent||el.value||el.getAttribute('aria-label')||el.getAttribute('title')||'').replace(/\s+/g,' ').trim();
              }
              function bate(el,t){
                var v=textoDe(el);
                return !!(v && v.indexOf(t)>=0);
              }
              function acharOp(op){
                var s=op.s, t=op.t;
                if(s){
                  try{
                    var lista=document.querySelectorAll(s);
                    if(t){
                      for(var i=0;i<lista.length;i++){ if(bate(lista[i],t)) return true; }
                    } else if(lista.length){
                      return true;
                    }
                  }catch(_){}
                }
                if(t){
                  var preferidos=document.querySelectorAll('button,a,input,textarea,[role="button"],[role="radio"],[role="checkbox"],label');
                  for(var j=0;j<preferidos.length;j++){ if(bate(preferidos[j],t)) return true; }
                  var n=document.querySelectorAll('span,div,p,h1,h2,h3,li');
                  for(var k=0;k<n.length;k++){ if(bate(n[k],t)) return true; }
                }
                return false;
              }
              for(var o=0;o<ops.length;o++){
                if(acharOp(ops[o])) return JSON.stringify({ok:true,id:ops[o].id});
              }
              return JSON.stringify({ok:false,motivo:'nao_achou'});
            })();
        """.trimIndent()
    }
}

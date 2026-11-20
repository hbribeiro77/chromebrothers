package com.chromebrothers.navegador.regras

/**
 * Pick sem listeners na página: a Activity cobre a WebView com overlay nativo
 * e consulta o DOM via evaluateJavascript em coordenadas CSS.
 */
object ScriptsDoPickDeElementoParaOGravadorDeRoteiro {
    /** Legado do prompt; mantido só por compat. */
    const val PREFIXO_DO_PROMPT = "chromebrothers:gravador-pick:"

    private val HELPERS = """
          function __cbMarca(el){
            if(!el || el===document.documentElement || el===document.body) return null;
            var tag=(el.tagName||'').toLowerCase();
            var id=el.id||'';
            var name=el.getAttribute('name')||'';
            var aria=el.getAttribute('aria-label')||'';
            var ph=el.getAttribute('placeholder')||'';
            var type=(el.getAttribute('type')||'').toLowerCase();
            var role=(el.getAttribute('role')||'').toLowerCase();
            var href=el.getAttribute('href')||'';
            var testid=el.getAttribute('data-testid')||el.getAttribute('data-test-id')||el.getAttribute('data-test')||'';
            var dataqa=el.getAttribute('data-qa')||el.getAttribute('data-cy')||'';
            var classes=(typeof el.className==='string'?el.className:(el.getAttribute('class')||'')).replace(/\s+/g,' ').trim().slice(0,120);
            var texto=(el.innerText||el.textContent||el.value||'').replace(/\s+/g,' ').trim().slice(0,80);
            var label='';
            try{
              if(id){
                var lab=document.querySelector('label[for="'+id.replace(/"/g,'')+'"]');
                if(lab) label=(lab.innerText||lab.textContent||'').replace(/\s+/g,' ').trim().slice(0,80);
              }
            }catch(_){}
            var formId='';
            try{
              var form=el.closest && el.closest('form');
              if(form && form.id) formId=form.id;
            }catch(_){}
            var inNav=false, inHeader=false;
            try{ inNav=!!(el.closest && el.closest('nav')); }catch(_){}
            try{ inHeader=!!(el.closest && el.closest('header')); }catch(_){}
            var indice=0;
            try{
              var todos=document.getElementsByTagName(tag);
              for(var i=0;i<todos.length;i++){ if(todos[i]===el){ indice=i; break; } }
            }catch(_){}
            return {
              id:id,name:name,aria:aria,placeholder:ph,texto:texto,tag:tag,indice:indice,
              type:type,role:role,href:href,testid:testid,dataqa:dataqa,classes:classes,
              formId:formId,inNav:inNav?'true':'false',inHeader:inHeader?'true':'false',label:label
            };
          }
          function __cbEhEditavel(el){
            if(!el || !el.getAttribute) return false;
            try{ if(el.isContentEditable) return true; }catch(_){}
            var ce=(el.getAttribute('contenteditable')||'').toLowerCase();
            if(ce===''||ce==='true'||ce==='plaintext-only'){
              if(el.hasAttribute('contenteditable')) return true;
            }
            var role=(el.getAttribute('role')||'').toLowerCase();
            if(role==='textbox'||role==='searchbox') return true;
            var t=(el.tagName||'').toLowerCase();
            if(t==='textarea') return true;
            if(t==='input'){
              var type=(el.getAttribute('type')||'text').toLowerCase();
              if(!/^(button|submit|checkbox|radio|file|image|reset|hidden|range|color)$/.test(type)) return true;
            }
            try{
              var cls=(typeof el.className==='string'?el.className:(el.getAttribute('class')||''));
              if(/\bProseMirror\b|\bql-editor\b|\bCodeMirror\b/.test(cls)) return true;
            }catch(_){}
            return false;
          }
          function __cbEditavel(el){
            var cur=el;
            for(var i=0;i<12 && cur;i++){
              if(__cbEhEditavel(cur)) return cur;
              cur=cur.parentElement;
            }
            return null;
          }
          function __cbEditavelNoPonto(x,y){
            var sel='[contenteditable="true"],[contenteditable=""],[contenteditable="plaintext-only"],[role="textbox"],[role="searchbox"],textarea,input:not([type]),input[type="text"],input[type="search"],input[type="email"],input[type="url"],input[type="tel"],input[type="password"],input[type="number"],.ProseMirror,#prompt-textarea,[data-testid*="composer"],[data-testid*="prompt"]';
            var n;
            try{ n=document.querySelectorAll(sel); }catch(_){ return null; }
            var best=null, bestArea=1e18;
            for(var i=0;i<n.length;i++){
              var el=n[i];
              if(!__cbEhEditavel(el) && !(el.classList && el.classList.contains('ProseMirror'))) continue;
              try{
                var r=el.getBoundingClientRect();
                if(r.width<2||r.height<2) continue;
                if(x<r.left||x>r.right||y<r.top||y>r.bottom) continue;
                var area=r.width*r.height;
                if(area<bestArea){ bestArea=area; best=el; }
              }catch(_){}
            }
            return best;
          }
          function __cbClicavel(el){
            var cur=el;
            for(var i=0;i<7 && cur;i++){
              var t=(cur.tagName||'').toLowerCase();
              var role=(cur.getAttribute('role')||'').toLowerCase();
              var type=(cur.getAttribute('type')||'').toLowerCase();
              if(t==='a'||t==='button'||t==='summary'||role==='button'||role==='link'||role==='checkbox'||role==='radio'||
                 (t==='input'&&(type==='submit'||type==='button'||type==='image'||type==='reset'||type==='checkbox'||type==='radio'))){
                return cur;
              }
              cur=cur.parentElement;
            }
            return null;
          }
          function __cbEmpacota(el){
            if(!el) return '';
            var m=__cbMarca(el);
            if(!m) return '';
            var paiEl=__cbClicavel(el);
            if(paiEl && paiEl!==el){
              var pm=__cbMarca(paiEl);
              if(pm) m.pai=pm;
            }
            if(window.__cbGravadorPreview){
              try{
                for(var i=0;i<window.__cbGravadorPreview.length;i++){
                  window.__cbGravadorPreview[i].style.outline='';
                }
              }catch(_){}
              window.__cbGravadorPreview=null;
            }
            if(window.__cbGravadorHL && window.__cbGravadorHL!==el){
              try{ window.__cbGravadorHL.style.outline=''; }catch(_){}
            }
            window.__cbGravadorAlvo=el;
            window.__cbGravadorHL=el;
            try{ el.style.outline='2px solid #8AB4F8'; }catch(_){}
            try{ el.scrollIntoView({block:'center',inline:'nearest'}); }catch(_){}
            return JSON.stringify(m);
          }
          function __cbCssDoToque(xView,yView,viewW,viewH){
            var vw=window.innerWidth||viewW||1;
            var vh=window.innerHeight||viewH||1;
            var w=Math.max(1, viewW||vw);
            var h=Math.max(1, viewH||vh);
            return { x: xView * (vw / w), y: yView * (vh / h) };
          }
          function __cbSob(x,y){
            var el=document.elementFromPoint(x,y);
            while(el && el.shadowRoot){
              var i=el.shadowRoot.elementFromPoint(x,y);
              if(!i || i===el) break;
              el=i;
            }
            var edit=__cbEditavel(el);
            if(edit) return edit;
            var noPonto=__cbEditavelNoPonto(x,y);
            if(noPonto) return noPonto;
            return el;
          }
          function __cbVisivel(el){
            try{
              var r=el.getBoundingClientRect();
              return r.width>=2 && r.height>=2 && r.bottom>0 && r.right>0 &&
                r.top< (window.innerHeight||9999) && r.left<(window.innerWidth||9999);
            }catch(_){ return false; }
          }
          function __cbListaVizinhos(){
            var sel='a,button,input,select,textarea,label,summary,li,td,th,p,h1,h2,h3,h4,span,div,img,[role="button"],[role="link"],[role="checkbox"],[role="radio"],[role="tab"],[role="menuitem"],[role="textbox"],[role="searchbox"],[contenteditable="true"],[contenteditable=""],[contenteditable="plaintext-only"],.ProseMirror';
            var n;
            try{ n=document.querySelectorAll(sel); }catch(_){ return []; }
            var out=[];
            for(var i=0;i<n.length;i++){
              if(__cbVisivel(n[i])) out.push(n[i]);
            }
            return out;
          }
    """.trimIndent()

    fun marcaNoPonto(
        xView: Double,
        yView: Double,
        larguraDaCamadaPx: Int,
        alturaDaCamadaPx: Int,
    ): String = """
        (function(){
          $HELPERS
          var pt=__cbCssDoToque($xView,$yView,$larguraDaCamadaPx,$alturaDaCamadaPx);
          var el=__cbSob(pt.x,pt.y);
          return __cbEmpacota(el);
        })();
    """.trimIndent()

    /** Quando o toque não acerta: pega o melhor editor visível (ChatGPT/ProseMirror etc.). */
    fun marcaDoMelhorEditorVisivel(): String = """
        (function(){
          $HELPERS
          function pontua(el){
            var p=0;
            var aria=(el.getAttribute('aria-label')||'').toLowerCase();
            var cls=(typeof el.className==='string'?el.className:(el.getAttribute('class')||'')).toLowerCase();
            var txt=((el.innerText||el.textContent||'')+'').replace(/\s+/g,' ').trim().toLowerCase().slice(0,80);
            if(cls.indexOf('prosemirror')>=0) p+=20;
            if(aria.indexOf('pergunte')>=0 || aria.indexOf('ask chatgpt')>=0 || aria.indexOf('message')>=0) p+=18;
            if(txt.indexOf('pergunte ao chatgpt')>=0 || txt.indexOf('ask chatgpt')>=0) p+=14;
            if((el.getAttribute('role')||'').toLowerCase()==='textbox') p+=8;
            if(__cbEhEditavel(el)) p+=6;
            try{
              var r=el.getBoundingClientRect();
              if(r.width>40 && r.height>16) p+=4;
              if(r.top > (window.innerHeight||0)*0.3) p+=3;
            }catch(_){}
            return p;
          }
          var sel='.ProseMirror,[aria-label*="Pergunte"],[aria-label*="Ask ChatGPT"],[aria-label*="Message"],[aria-label*="ChatGPT"],[role="textbox"],[contenteditable="true"],[contenteditable=""],[contenteditable="plaintext-only"],#prompt-textarea,textarea,[data-testid*="composer"],[data-testid*="prompt"],[class*="composer"]';
          var n; try{ n=document.querySelectorAll(sel); }catch(_){ n=[]; }
          var best=null, bestP=-1;
          for(var i=0;i<n.length;i++){
            var el=n[i];
            var p=pontua(el);
            if(p>bestP){ bestP=p; best=el; }
          }
          if(!best || bestP<10){
            var todos=document.querySelectorAll('div,button,p,span,label');
            for(var j=0;j<todos.length;j++){
              var el2=todos[j];
              var t2=((el2.getAttribute('aria-label')||'')+' '+(el2.innerText||'')).toLowerCase();
              if(t2.indexOf('pergunte ao chatgpt')<0 && t2.indexOf('ask chatgpt')<0) continue;
              var p2=pontua(el2)+10;
              if(p2>bestP){ bestP=p2; best=el2; }
            }
          }
          if(!best) best=__cbEditavelNoPonto((window.innerWidth||0)/2,(window.innerHeight||0)*0.72);
          if(!best){
            try{
              return JSON.stringify({tag:'debug',texto:'sem-editor',aria:String(document.querySelectorAll('.ProseMirror,[role=textbox]').length),classes:location.hostname,indice:0});
            }catch(_){ return ''; }
          }
          var edit=__cbEditavel(best)||best;
          return __cbEmpacota(edit);
        })();
    """.trimIndent()

    /**
     * Move o alvo atual: "up"|"down"|"left"|"right"|"pai".
     * Devolve a marca JSON do novo elemento, ou "" se não houver vizinho.
     */
    fun navegarAlvo(direcao: String): String {
        val dir = when (direcao.lowercase()) {
            "up", "cima" -> "up"
            "down", "baixo" -> "down"
            "left", "esquerda" -> "left"
            "right", "direita" -> "right"
            "pai", "parent" -> "pai"
            else -> direcao.lowercase()
        }
        return """
        (function(){
          $HELPERS
          var atual=window.__cbGravadorAlvo || window.__cbGravadorHL;
          if(!atual || !atual.isConnected) return '';
          if('$dir'==='pai'){
            var p=atual.parentElement;
            while(p && (p===document.body || p===document.documentElement || !__cbVisivel(p))){
              p=p.parentElement;
            }
            if(!p) return '';
            return __cbEmpacota(p);
          }
          var rect=atual.getBoundingClientRect();
          var cx=rect.left+rect.width/2;
          var cy=rect.top+rect.height/2;
          var lista=__cbListaVizinhos();
          var best=null, bestScore=1e18;
          for(var i=0;i<lista.length;i++){
            var el=lista[i];
            if(el===atual || atual.contains(el) || el.contains(atual)) continue;
            var r=el.getBoundingClientRect();
            var ex=r.left+r.width/2;
            var ey=r.top+r.height/2;
            var dx=ex-cx, dy=ey-cy;
            var primary=0, ortho=0;
            if('$dir'==='up'){ if(dy>=-1) continue; primary=-dy; ortho=Math.abs(dx); }
            else if('$dir'==='down'){ if(dy<=1) continue; primary=dy; ortho=Math.abs(dx); }
            else if('$dir'==='left'){ if(dx>=-1) continue; primary=-dx; ortho=Math.abs(dy); }
            else if('$dir'==='right'){ if(dx<=1) continue; primary=dx; ortho=Math.abs(dy); }
            else continue;
            var score=primary + ortho*1.6;
            if(score<bestScore){ bestScore=score; best=el; }
          }
          if(!best) return '';
          return __cbEmpacota(best);
        })();
        """.trimIndent()
    }

    fun destacarNoPonto(
        xView: Double,
        yView: Double,
        larguraDaCamadaPx: Int,
        alturaDaCamadaPx: Int,
    ): String = """
        (function(){
          $HELPERS
          if(window.__cbGravadorHL){
            try{ window.__cbGravadorHL.style.outline=''; }catch(_){}
          }
          var pt=__cbCssDoToque($xView,$yView,$larguraDaCamadaPx,$alturaDaCamadaPx);
          var el=__cbSob(pt.x,pt.y);
          window.__cbGravadorHL=el;
          try{ if(el) el.style.outline='2px solid #8AB4F8'; }catch(_){}
          return 'ok';
        })();
    """.trimIndent()

    fun limparDestaque(): String = """
        (function(){
          if(window.__cbGravadorHL){
            try{ window.__cbGravadorHL.style.outline=''; }catch(_){}
            window.__cbGravadorHL=null;
          }
          if(window.__cbGravadorPreview){
            try{
              for(var i=0;i<window.__cbGravadorPreview.length;i++){
                window.__cbGravadorPreview[i].style.outline='';
              }
            }catch(_){}
            window.__cbGravadorPreview=null;
          }
          return 'ok';
        })();
    """.trimIndent()

    fun limparAlvo(): String = """
        (function(){
          if(window.__cbGravadorHL){
            try{ window.__cbGravadorHL.style.outline=''; }catch(_){}
            window.__cbGravadorHL=null;
          }
          if(window.__cbGravadorPreview){
            try{
              for(var i=0;i<window.__cbGravadorPreview.length;i++){
                window.__cbGravadorPreview[i].style.outline='';
              }
            }catch(_){}
            window.__cbGravadorPreview=null;
          }
          window.__cbGravadorAlvo=null;
          return 'ok';
        })();
    """.trimIndent()

    /** OuterHTML compacto do alvo (e pai) para a IA escolher candidata. */
    fun trechoDomDoAlvo(limite: Int = 2500): String = """
        (function(){
          var el=window.__cbGravadorAlvo || window.__cbGravadorHL;
          if(!el) return '';
          function limpa(html){
            return String(html||'').replace(/\s+/g,' ').trim().slice(0,$limite);
          }
          var html='';
          try{
            var pai=el.parentElement;
            if(pai && pai!==document.body && pai!==document.documentElement){
              html=pai.outerHTML || '';
            } else {
              html=el.outerHTML || '';
            }
          }catch(_){ try{ html=el.outerHTML||''; }catch(__){} }
          return limpa(html);
        })();
    """.trimIndent()

    fun destacarCandidata(seletor: String, texto: String): String {
        val sel = TextoJsonDoChat.escapar(seletor)
        val txt = TextoJsonDoChat.escapar(texto)
        return """
            (function(){
              if(window.__cbGravadorPreview){
                try{
                  for(var i=0;i<window.__cbGravadorPreview.length;i++){
                    window.__cbGravadorPreview[i].style.outline='';
                  }
                }catch(_){}
              }
              window.__cbGravadorPreview=[];
              var s='$sel', t='$txt';
              var achados=[];
              if(s){
                try{ achados=Array.prototype.slice.call(document.querySelectorAll(s),0,12); }catch(_){}
                if(t && achados.length){
                  achados=achados.filter(function(el){
                    var v=(el.innerText||el.textContent||el.value||el.getAttribute('aria-label')||'').trim();
                    return v && v.indexOf(t)>=0;
                  });
                }
              } else if(t){
                var n=document.querySelectorAll('a,button,input,textarea,[role="button"],label,span,div');
                for(var i=0;i<n.length && achados.length<12;i++){
                  var el=n[i];
                  var v=(el.innerText||el.textContent||el.value||el.getAttribute('aria-label')||'').trim();
                  if(v && v.indexOf(t)>=0) achados.push(el);
                }
              }
              for(var j=0;j<achados.length;j++){
                try{ achados[j].style.outline='2px solid #FDD663'; }catch(_){}
                window.__cbGravadorPreview.push(achados[j]);
              }
              if(achados[0]){
                try{ achados[0].scrollIntoView({block:'center',inline:'nearest'}); }catch(_){}
              }
              return JSON.stringify({ok:true,n:achados.length});
            })();
        """.trimIndent()
    }

    /** Conta matches de várias candidatas de uma vez: [{s,t},…] → [n,…] */
    fun contarCandidatas(pares: List<Pair<String, String>>): String {
        val itens = pares.joinToString(",") { (s, t) ->
            """{"s":"${TextoJsonDoChat.escapar(s)}","t":"${TextoJsonDoChat.escapar(t)}"}"""
        }
        return """
            (function(){
              var itens=[$itens];
              function contar(s,t){
                var achados=[];
                if(s){
                  try{ achados=Array.prototype.slice.call(document.querySelectorAll(s)); }catch(_){ return 0; }
                  if(t){
                    achados=achados.filter(function(el){
                      var v=(el.innerText||el.textContent||el.value||el.getAttribute('aria-label')||'').trim();
                      return v && v.indexOf(t)>=0;
                    });
                  }
                  return achados.length;
                }
                if(!t) return 0;
                var n=document.querySelectorAll('a,button,input,textarea,[role="button"],label,span,div,p,h1,h2,h3,li');
                var c=0;
                for(var i=0;i<n.length;i++){
                  var el=n[i];
                  var v=(el.innerText||el.textContent||el.value||el.getAttribute('aria-label')||'').trim();
                  if(v && v.indexOf(t)>=0) c++;
                }
                return c;
              }
              var out=[];
              for(var i=0;i<itens.length;i++) out.push(contar(itens[i].s,itens[i].t));
              return JSON.stringify(out);
            })();
        """.trimIndent()
    }

    fun lerContagens(json: String): List<Int> {
        if (json.isBlank() || json == "null") return emptyList()
        val limpo = json.trim().removePrefix("[").removeSuffix("]")
        if (limpo.isBlank()) return emptyList()
        return limpo.split(',').mapNotNull { it.trim().toIntOrNull() }
    }

    // Mantidos para não quebrar referências antigas; no-op.
    fun instalador(): String = "('ok')"
    fun cancelar(): String = limparAlvo()
}

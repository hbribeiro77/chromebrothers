package com.chromebrothers.navegador.regras

object LimiteDaLarguraDaCapturaDeQuadroDoVideo {
    const val MINIATURA = 960
    const val QUADRO_SALVO = 1920
}

object ScriptInjetadoParaDetectarVideoHtmlNaPagina {
    fun inicio(capturarMidia: Boolean): String = """
        (function () {
          window.__chromebrothersCapturarMidia = ${if (capturarMidia) "true" else "false"};
          function avisarChromebrothers(nome, argumentos) {
            try {
              document.dispatchEvent(new CustomEvent("chromebrothers-aviso", {
                detail: JSON.stringify({ nome: nome, argumentos: argumentos })
              }));
            } catch (erro) {}
          }
          function tamanhoDaResposta(faixa, comprimento) {
            var achado = /\/(\d+)\s*$/.exec(faixa || "");
            if (achado) return achado[1];
            return comprimento || "";
          }
          function avisarMidia(endereco, tipo, tamanho) {
            if (!window.__chromebrothersCapturarMidia || !endereco) return;
            try {
              var absoluto = new URL(String(endereco), location.href).href;
              if (!/^https?:/i.test(absoluto)) return;
              avisarChromebrothers("onMediaUrl", [absoluto, tipo || "", tamanho || ""]);
            } catch (erro) {}
          }
          function instalarCapturaDeMidia() {
            if (window.__cbCapturaDeMidiaInstalada) return;
            window.__cbCapturaDeMidiaInstalada = true;
            if (typeof window.fetch === "function") {
              var fetchOriginal = window.fetch;
              window.fetch = function () {
                var argumentos = arguments;
                var endereco = "";
                try {
                  if (typeof argumentos[0] === "string") endereco = argumentos[0];
                  else if (argumentos[0] && argumentos[0].url) endereco = argumentos[0].url;
                } catch (erro) {}
                var promessa = fetchOriginal.apply(this, argumentos);
                if (!promessa || !promessa.then) return promessa;
                return promessa.then(function (resposta) {
                  try {
                    var cabecalhos = resposta && resposta.headers ? resposta.headers : null;
                    avisarMidia(
                      endereco,
                      cabecalhos ? cabecalhos.get("content-type") : "",
                      cabecalhos ? tamanhoDaResposta(cabecalhos.get("content-range"), cabecalhos.get("content-length")) : ""
                    );
                  } catch (erro) {}
                  return resposta;
                });
              };
            }
            if (window.XMLHttpRequest && !window.__cbXhrAberto) {
              window.__cbXhrAberto = XMLHttpRequest.prototype.open;
              window.__cbXhrEnviado = XMLHttpRequest.prototype.send;
              XMLHttpRequest.prototype.open = function (metodo, endereco) {
                try { this.__cbEndereco = endereco; } catch (erro) {}
                return window.__cbXhrAberto.apply(this, arguments);
              };
              XMLHttpRequest.prototype.send = function () {
                if (!this.__cbEscutaDeMidia) {
                  this.__cbEscutaDeMidia = true;
                  this.addEventListener("load", function () {
                    var tipo = "";
                    var tamanho = "";
                    try { tipo = this.getResponseHeader("content-type") || ""; } catch (erro) {}
                    try {
                      tamanho = tamanhoDaResposta(this.getResponseHeader("content-range"), this.getResponseHeader("content-length"));
                    } catch (erro) {}
                    avisarMidia(this.__cbEndereco || "", tipo, tamanho);
                  });
                }
                return window.__cbXhrEnviado.apply(this, arguments);
              };
            }
          }
          window.__chromebrothersDefinirCapturaDeMidia = function (ligada) {
            window.__chromebrothersCapturarMidia = !!ligada;
            instalarCapturaDeMidia();
          };
          instalarCapturaDeMidia();
        })();
    """.trimIndent()

    fun texto(capturarMidia: Boolean = false): String = """
        ${inicio(capturarMidia)}
        (function () {
          if (window.__chromebrothersInstalado === 14) return;
          window.__chromebrothersInstalado = 14;
          function avisarChromebrothers(nome, argumentos) {
            try {
              document.dispatchEvent(new CustomEvent("chromebrothers-aviso", {
                detail: JSON.stringify({ nome: nome, argumentos: argumentos })
              }));
            } catch (erro) {}
          }
          var seq = 0;
          var marcaDoQuadro = Math.random().toString(36).slice(2, 7);
          function idDe(video) {
            if (!video.dataset.cbId) {
              seq += 1;
              video.dataset.cbId = "v" + marcaDoQuadro + "-" + seq;
            }
            return video.dataset.cbId;
          }
          var ehQuadroInterno = false;
          try { ehQuadroInterno = window.top !== window; } catch (erro) { ehQuadroInterno = true; }
          function avisarPaiDoPalco(entrar) {
            if (!ehQuadroInterno) return;
            try { window.parent.postMessage({ chromebrothersPalcoDoQuadro: entrar ? "entrar" : "sair" }, "*"); } catch (erro) {}
          }
          function quadroQueEnviou(origem) {
            var achado = null;
            Array.prototype.forEach.call(document.querySelectorAll("iframe,frame"), function (quadro) {
              try { if (quadro.contentWindow === origem) achado = quadro; } catch (erro) {}
            });
            return achado;
          }
          window.addEventListener("message", function (evento) {
            var dados = evento.data;
            if (!dados || typeof dados !== "object" || !dados.chromebrothersPalcoDoQuadro) return;
            var quadro = quadroQueEnviou(evento.source);
            if (!quadro) return;
            var entrar = dados.chromebrothersPalcoDoQuadro === "entrar";
            if (entrar) {
              if (quadro.__cbEstiloDoQuadro === undefined) quadro.__cbEstiloDoQuadro = quadro.getAttribute("style");
              quadro.style.setProperty("position", "fixed", "important");
              quadro.style.setProperty("left", "0", "important");
              quadro.style.setProperty("top", "0", "important");
              quadro.style.setProperty("width", "100vw", "important");
              quadro.style.setProperty("height", "100vh", "important");
              quadro.style.setProperty("max-width", "none", "important");
              quadro.style.setProperty("max-height", "none", "important");
              quadro.style.setProperty("margin", "0", "important");
              quadro.style.setProperty("border", "0", "important");
              quadro.style.setProperty("transform", "none", "important");
              quadro.style.setProperty("z-index", "2147483647", "important");
              quadro.style.setProperty("background", "#000", "important");
            } else if (quadro.__cbEstiloDoQuadro !== undefined) {
              if (quadro.__cbEstiloDoQuadro === null) quadro.removeAttribute("style");
              else quadro.setAttribute("style", quadro.__cbEstiloDoQuadro);
              quadro.__cbEstiloDoQuadro = undefined;
            }
            avisarPaiDoPalco(entrar);
          });
          function temSom(video) {
            return !!(video && !video.muted && video.volume > 0);
          }
          // Fluidez: os vídeos já vistos ficam numa lista. Antes, cada mudança no DOM varria a página
          // inteira (querySelectorAll("*") atrás de shadow roots) — em feed/rolagem infinita isso
          // engasgava a rolagem. Agora: só os nós novos na hora; a varredura completa (que acha vídeo
          // dentro de shadow root) roda aos pedaços no tempo ocioso, no máximo a cada 2 s.
          var conhecidos = [];
          function registrar(video) {
            if (conhecidos.indexOf(video) < 0) conhecidos.push(video);
            ligar(video);
          }
          function procurarEmSombra(raizDaSombra) {
            if (!raizDaSombra || !raizDaSombra.querySelectorAll) return;
            Array.prototype.forEach.call(raizDaSombra.querySelectorAll("video"), registrar);
            Array.prototype.forEach.call(raizDaSombra.querySelectorAll("*"), function (elemento) {
              if (elemento.shadowRoot) procurarEmSombra(elemento.shadowRoot);
            });
          }
          function procurarNoNovo(no) {
            if (!no || no.nodeType !== 1) return;
            if (no.tagName === "VIDEO") registrar(no);
            if (no.shadowRoot) procurarEmSombra(no.shadowRoot);
            if (no.firstElementChild && no.querySelectorAll) {
              Array.prototype.forEach.call(no.querySelectorAll("video"), registrar);
            }
          }
          function varreduraCompletaAgora() {
            Array.prototype.forEach.call(document.querySelectorAll("video"), registrar);
            Array.prototype.forEach.call(document.querySelectorAll("*"), function (elemento) {
              if (elemento.shadowRoot) procurarEmSombra(elemento.shadowRoot);
            });
          }
          var andador = null;
          var completaAgendada = false;
          var ultimaCompleta = 0;
          var quandoOcioso = window.requestIdleCallback
            ? function (fn) { window.requestIdleCallback(fn, { timeout: 4000 }); }
            : function (fn) { setTimeout(function () { fn(null); }, 200); };
          function varreduraCompletaAosPedacos(prazo) {
            if (!andador) andador = document.createTreeWalker(document.documentElement || document, NodeFilter.SHOW_ELEMENT);
            var no;
            var conta = 0;
            while ((no = andador.nextNode())) {
              if (no.tagName === "VIDEO") registrar(no);
              if (no.shadowRoot) procurarEmSombra(no.shadowRoot);
              conta += 1;
              if ((conta & 255) === 0 && (!prazo || prazo.timeRemaining() < 3)) {
                quandoOcioso(varreduraCompletaAosPedacos);
                return;
              }
            }
            andador = null;
            completaAgendada = false;
            ultimaCompleta = Date.now();
          }
          function agendarVarreduraCompleta() {
            if (completaAgendada) return;
            completaAgendada = true;
            var espera = Math.max(0, 2000 - (Date.now() - ultimaCompleta));
            setTimeout(function () { quandoOcioso(varreduraCompletaAosPedacos); }, espera);
          }
          function cadaVideo(fn) {
            conhecidos = conhecidos.filter(function (video) { return video.isConnected; });
            conhecidos.slice().forEach(fn);
          }
          function algumComSom() {
            var achou = false;
            cadaVideo(function (video) {
              if (!video.paused && !video.ended && temSom(video)) achou = true;
            });
            return achou;
          }
          function videoPeloId(id) {
            function achar() {
              for (var i = 0; i < conhecidos.length; i++) {
                if (conhecidos[i].dataset.cbId === id) return conhecidos[i];
              }
              return null;
            }
            var achado = achar();
            if (achado) return achado;
            // Raro (comando do app para um vídeo que a lista ainda não viu): varre agora.
            varreduraCompletaAgora();
            return achar();
          }
          var ultimaCaptura = 0;
          var ultimoGesto = 0;
          function marcarGesto() { ultimoGesto = Date.now(); }
          window.addEventListener("pointerdown", marcarGesto, true);
          window.addEventListener("touchstart", marcarGesto, true);
          window.addEventListener("keydown", marcarGesto, true);
          // Vídeo sem capa ganha o 1º quadro só quando chega perto da tela. Antes era em todos de uma vez:
          // numa galeria (higgsfield.ai: 75 vídeos) o app baixava e decodificava tudo, brigando com a rolagem.
          var observadorDeMiniatura = window.IntersectionObserver
            ? new IntersectionObserver(function (entradas) {
                entradas.forEach(function (entrada) {
                  if (!entrada.isIntersecting) return;
                  observadorDeMiniatura.unobserve(entrada.target);
                  pintarMiniaturaAgora(entrada.target);
                });
              }, { rootMargin: "50% 0px" })
            : null;
          function prepararMiniatura(video) {
            if (video.__chromebrothersMiniatura) return;
            video.__chromebrothersMiniatura = true;
            if (video.poster || video.readyState >= 2) return;
            if (observadorDeMiniatura) observadorDeMiniatura.observe(video);
            else pintarMiniaturaAgora(video);
          }
          function pintarMiniaturaAgora(video) {
            if (video.poster || video.readyState >= 2) return;
            if (!video.preload || video.preload === "none") video.preload = "metadata";
            var pintou = false;
            function pintar() {
              if (pintou || !video.videoWidth || video.currentTime > 0.05 || !video.paused) return;
              pintou = true;
              try { video.currentTime = 0.04; } catch (erro) {}
            }
            video.addEventListener("loadedmetadata", pintar);
            video.addEventListener("loadeddata", pintar);
            if (video.readyState >= 1) pintar();
          }
          function usuarioJaInteragiu() {
            if (ultimoGesto > 0) return true;
            try {
              return !!(navigator.userActivation && navigator.userActivation.hasBeenActive);
            } catch (erro) {
              return false;
            }
          }
          document.addEventListener("play", function (evento) {
            var video = evento.target;
            if (!video || video.tagName !== "VIDEO") return;
            if (video.muted || !video.volume) return;
            if (usuarioJaInteragiu()) return;
            video.pause();
          }, true);
          function enviarLimites(video) {
            var retangulo = video.getBoundingClientRect();
            avisarChromebrothers("onVideoBounds", [
              idDe(video),
              String(retangulo.left),
              String(retangulo.top),
              String(retangulo.width),
              String(retangulo.height),
              String(window.innerWidth),
              String(window.innerHeight)
            ]);
          }
          function avisar(video) {
            if (!temSom(video)) return;
            var id = idDe(video);
            window.__chromebrothersVideoAtual = id;
            var endereco = video.currentSrc || video.src || "";
            var taxa = String(video.playbackRate || 1);
            avisarChromebrothers("onVideoPlaying", [id, endereco, taxa]);
            enviarLimites(video);
          }
          function enderecoHttpDoVideo(video) {
            var atual = video.currentSrc || "";
            var bruto = video.src || "";
            if (atual.indexOf("http://") === 0 || atual.indexOf("https://") === 0) return atual;
            if (bruto.indexOf("http://") === 0 || bruto.indexOf("https://") === 0) return bruto;
            return "";
          }
          function pedirCaptura(video) {
            var agora = Date.now();
            if (agora - ultimaCaptura < 900) return;
            ultimaCaptura = agora;
            window.__chromebrothersVideoAtual = idDe(video);
            avisarChromebrothers("onVideoLongPress", [idDe(video), enderecoHttpDoVideo(video)]);
          }
          function avisarParada() {
            if (!algumComSom()) avisarChromebrothers("onVideoStopped", []);
          }
          function ligar(video) {
            if (video.__chromebrothersLigado === 9) return;
            video.__chromebrothersLigado = 9;
            video.addEventListener("play", function () { avisar(video); });
            video.addEventListener("playing", function () { avisar(video); });
            video.addEventListener("pause", avisarParada);
            video.addEventListener("ended", avisarParada);
            video.addEventListener("volumechange", function () {
              if (!video.paused && !video.ended && temSom(video)) avisar(video);
              else avisarParada();
            });
            prepararMiniatura(video);
            ligarToqueLongo(video);
          }
          function ligarToqueLongo(video) {
            var espera = null;
            var x0 = 0;
            var y0 = 0;
            video.addEventListener("touchstart", function (evento) {
              if (!evento.touches || !evento.touches[0]) return;
              x0 = evento.touches[0].clientX;
              y0 = evento.touches[0].clientY;
              espera = setTimeout(function () { pedirCaptura(video); }, 550);
            }, { passive: true });
            function cancelar(evento) {
              if (!espera) return;
              if (evento && evento.touches && evento.touches[0]) {
                var dx = evento.touches[0].clientX - x0;
                var dy = evento.touches[0].clientY - y0;
                if (dx * dx + dy * dy > 144) {
                  clearTimeout(espera);
                  espera = null;
                }
                return;
              }
              clearTimeout(espera);
              espera = null;
            }
            video.addEventListener("touchmove", cancelar, { passive: true });
            video.addEventListener("touchend", cancelar);
            video.addEventListener("touchcancel", cancelar);
          }
          function atualizarLimitesDoVideoAtual() {
            var id = window.__chromebrothersVideoAtual;
            if (!id) return;
            var video = null;
            for (var i = 0; i < conhecidos.length; i++) {
              if (conhecidos[i].dataset.cbId === id) { video = conhecidos[i]; break; }
            }
            if (video && !video.paused && !video.ended) enviarLimites(video);
          }
          // Rolagem: no máximo uma medida por quadro e só com vídeo tocando (antes era uma por evento,
          // em qualquer área que rola, forçando a página a recalcular o layout).
          var limitesNoProximoQuadro = false;
          function agendarLimites() {
            if (limitesNoProximoQuadro || !window.__chromebrothersVideoAtual) return;
            limitesNoProximoQuadro = true;
            requestAnimationFrame(function () {
              limitesNoProximoQuadro = false;
              atualizarLimitesDoVideoAtual();
            });
          }
          varreduraCompletaAgora();
          ultimaCompleta = Date.now();
          cadaVideo(function (video) {
            if (!video.paused && !video.ended) avisar(video);
          });
          var raiz = document.documentElement || document.body;
          if (raiz) {
            new MutationObserver(function (mudancas) {
              for (var m = 0; m < mudancas.length; m++) {
                var nos = mudancas[m].addedNodes;
                for (var n = 0; n < nos.length; n++) procurarNoNovo(nos[n]);
              }
              agendarVarreduraCompleta();
            }).observe(raiz, { childList: true, subtree: true });
          }
          window.addEventListener("scroll", agendarLimites, { capture: true, passive: true });
          window.addEventListener("resize", agendarLimites, { passive: true });
          window.chromebrothersDefinirVelocidade = function (id, taxa) {
            var video = videoPeloId(id);
            if (!video) return;
            video.playbackRate = Number(taxa);
          };
          if (!window.__cbPauseDaMidia) {
            window.__cbPauseDaMidia = HTMLMediaElement.prototype.pause;
            HTMLMediaElement.prototype.pause = function () {
              if (this.__cbNoPalco && window.__chromebrothersQuerReproduzir) return;
              return window.__cbPauseDaMidia.apply(this, arguments);
            };
          }
          function colocarVideoNoPalco(video) {
            if (video.__cbEstilo === undefined) video.__cbEstilo = video.getAttribute("style") || "";
            if (!video.__cbPai) {
              video.__cbPai = video.parentNode;
              video.__cbProximo = video.nextSibling;
            }
            video.__cbNoPalco = true;
            if (!video.__cbPauseNativo) {
              video.__cbPauseNativo = window.__cbPauseDaMidia.bind(video);
              video.pause = function () {
                if (window.__chromebrothersQuerReproduzir) return;
                return video.__cbPauseNativo();
              };
            }
            var palco = document.getElementById("cb-palco-do-player");
            if (!palco) {
              palco = document.createElement("div");
              palco.id = "cb-palco-do-player";
              (document.documentElement || document.body).appendChild(palco);
            }
            if (video.parentNode !== palco) palco.appendChild(video);
            video.controls = false;
            video.removeAttribute("controls");
            var estilo = document.getElementById("cb-estilo-do-player");
            if (!estilo) {
              estilo = document.createElement("style");
              estilo.id = "cb-estilo-do-player";
              (document.head || document.documentElement).appendChild(estilo);
            }
            avisarPaiDoPalco(true);
            // 100% de um fixo é a área visível (visual viewport); em páginas um pouco mais largas/altas que a
            // tela ela fica menor que a área da página e sobra uma faixa à direita/embaixo. 100vw/100vh cobre
            // a área inteira; a barra de rolagem da página (desenhada por cima de tudo) some enquanto o palco existe.
            estilo.textContent = "html{scrollbar-width:none!important}#cb-palco-do-player{position:fixed!important;left:0!important;top:0!important;right:auto!important;bottom:auto!important;width:max(100%,100vw)!important;height:max(100%,100vh)!important;background:#000!important;z-index:2147483647!important;}#cb-palco-do-player video{width:100%!important;height:100%!important;max-width:none!important;max-height:none!important;margin:0!important;object-fit:contain!important;background:#000!important;pointer-events:none!important;}";
          }
          window.chromebrothersEntrarNoPlayer = function (id) {
            var video = videoPeloId(id);
            if (!video) return "sem_video";
            cadaVideo(function (outro) {
              if (outro !== video) outro.pause();
            });
            window.__chromebrothersVideoAtual = id;
            window.__chromebrothersQuerReproduzir = true;
            colocarVideoNoPalco(video);
            return "ok";
          };
          window.chromebrothersManterPalco = function (id) {
            var video = videoPeloId(id);
            if (video) colocarVideoNoPalco(video);
          };
          window.chromebrothersDefinirReproducao = function (id, tocar) {
            var video = videoPeloId(id);
            window.__chromebrothersQuerReproduzir = !!tocar;
            if (!video) return;
            if (tocar) {
              var promessa = video.play();
              if (promessa && promessa.catch) promessa.catch(function () {});
            } else if (video.__cbPauseNativo) {
              video.__cbPauseNativo();
            } else {
              video.pause();
            }
          };
          window.chromebrothersDeslocar = function (id, segundos) {
            var video = videoPeloId(id);
            if (!video) return;
            var destino = (video.currentTime || 0) + Number(segundos);
            if (destino < 0) destino = 0;
            var duracao = video.duration;
            if (duracao && isFinite(duracao) && destino > duracao) destino = duracao;
            video.currentTime = destino;
          };
          window.chromebrothersSairDoPlayer = function (id) {
            var video = videoPeloId(id);
            window.__chromebrothersQuerReproduzir = false;
            if (video) video.__cbNoPalco = false;
            if (video && video.__cbPauseNativo) {
              video.pause = video.__cbPauseNativo;
              video.__cbPauseNativo = null;
            }
            if (video && video.__cbPai) {
              if (video.__cbProximo && video.__cbProximo.parentNode === video.__cbPai) video.__cbPai.insertBefore(video, video.__cbProximo);
              else video.__cbPai.appendChild(video);
              if (video.__cbEstilo) video.setAttribute("style", video.__cbEstilo);
              else video.removeAttribute("style");
            }
            var palco = document.getElementById("cb-palco-do-player");
            if (palco && palco.parentNode) palco.parentNode.removeChild(palco);
            var estilo = document.getElementById("cb-estilo-do-player");
            if (estilo && estilo.parentNode) estilo.parentNode.removeChild(estilo);
            avisarPaiDoPalco(false);
          };
          window.chromebrothersAlternarReproducao = function (id) {
            var video = videoPeloId(id);
            if (!video) return;
            if (video.paused) video.play();
            else video.pause();
          };
          window.chromebrothersDefinirPosicao = function (id, segundos) {
            var video = videoPeloId(id);
            if (!video) return "sem_video";
            var alvo = Number(segundos);
            if (!isFinite(alvo)) return "alvo_invalido";
            if (alvo < 0) alvo = 0;
            var duracao = video.duration;
            if (duracao && isFinite(duracao) && alvo > duracao) alvo = duracao;
            try {
              if (typeof video.fastSeek === "function") video.fastSeek(alvo);
              else video.currentTime = alvo;
            } catch (erro) {
              try { video.currentTime = alvo; } catch (erro2) { return "erro"; }
            }
            return "ok";
          };
          window.chromebrothersLerEstado = function (id) {
            var video = videoPeloId(id);
            if (!video) return "";
            var duracao = video.duration;
            return JSON.stringify({
              pausado: !!video.paused,
              atual: video.currentTime || 0,
              duracao: duracao && isFinite(duracao) ? duracao : 0,
              taxa: video.playbackRate || 1
            });
          };
          window.chromebrothersCapturarQuadro = function (id, larguraMaxima) {
            var video = videoPeloId(id);
            if (!video || !video.videoWidth || !video.videoHeight) return "SEM_QUADRO";
            var limite = Number(larguraMaxima);
            if (!isFinite(limite) || limite < 1) limite = 960;
            var largura = video.videoWidth;
            var altura = video.videoHeight;
            if (largura > limite) {
              altura = Math.max(1, Math.round(altura * limite / largura));
              largura = Math.round(limite);
            }
            var tela = document.createElement("canvas");
            tela.width = largura;
            tela.height = altura;
            try {
              var contexto = tela.getContext("2d");
              if (!contexto) return "QUADRO_BLOQUEADO";
              contexto.drawImage(video, 0, 0, largura, altura);
              return tela.toDataURL("image/jpeg", 0.86);
            } catch (erro) {
              return "QUADRO_BLOQUEADO";
            }
          };
        })();
    """.trimIndent()
}

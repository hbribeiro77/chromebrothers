package com.chromebrothers.navegador

import com.chromebrothers.navegador.regras.AparenciaInicialDaPaginaHomeDoNavegador

object HtmlDaPaginaInicialDoNavegadorChromebrothers {
    fun montar(token: String, estadoJson: String): String {
        val estadoSeguro = estadoJson.replace("<", "\\u003c")
        val aparencia = AparenciaInicialDaPaginaHomeDoNavegador.aPartirDoEstado(estadoJson)
        val comTema = AparenciaInicialDaPaginaHomeDoNavegador.aplicarNoDocumento(DOCUMENTO, aparencia)
        return comTema
            .replace("%%TOKEN%%", token)
            .replace("%%ESTADO%%", estadoSeguro)
    }

    private val DOCUMENTO = """
        <!DOCTYPE html>
        <html lang="pt-BR" style="color-scheme:%%ESQUEMA%%;background:%%FUNDO%%">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <meta name="color-scheme" content="%%ESQUEMA%%">
          <title>Nova aba</title>
          <style>
            * { box-sizing: border-box; }
            html, body { margin: 0; height: 100%; background: %%FUNDO%%; }
            body {
              font-family: Roboto, sans-serif;
              background: %%FUNDO%%;
              color: #202124;
            }
            .miolo {
              min-height: 100%;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              padding: 48px 24px 32px;
            }
            h1 {
              margin: 0 0 28px;
              font-size: 44px;
              font-weight: 500;
              letter-spacing: -0.6px;
            }
            form { width: min(640px, 100%); }
            input[type="search"], input[type="text"], input[type="url"] {
              width: 100%;
              height: 52px;
              border: 1px solid #dadce0;
              border-radius: 26px;
              padding: 0 22px;
              font-size: 16px;
              background: #fff;
              color: #202124;
              outline: none;
            }
            input:focus { border-color: #1a73e8; }
            .atalhos {
              width: min(640px, 100%);
              display: flex;
              flex-wrap: wrap;
              gap: 18px;
              justify-content: center;
              margin-top: 36px;
            }
            .atalho, .mais {
              width: 92px;
              background: transparent;
              border: 0;
              color: inherit;
              cursor: pointer;
              font: inherit;
              padding: 0;
              text-align: center;
            }
            .bolha {
              width: 52px;
              height: 52px;
              margin: 0 auto 8px;
              border-radius: 50%;
              background: #f1f3f4;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 18px;
              position: relative;
            }
            .nome {
              display: block;
              font-size: 12px;
              line-height: 1.3;
              color: #3c4043;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            .x {
              display: none;
              position: absolute;
              top: -4px;
              right: -4px;
              width: 20px;
              height: 20px;
              border: 0;
              border-radius: 50%;
              background: #e8eaed;
              color: #3c4043;
              cursor: pointer;
            }
            .atalho.armado .x { display: block; }
            .form-atalho {
              width: min(420px, 100%);
              margin-top: 18px;
              display: none;
              gap: 8px;
            }
            .form-atalho.aberto { display: grid; }
            .ajuste {
              border: 0;
              border-radius: 16px;
              padding: 8px 14px;
              background: #f1f3f4;
              color: #202124;
              cursor: pointer;
              font: inherit;
              font-size: 13px;
            }
            .aviso { min-height: 18px; font-size: 12px; color: #d93025; margin: 8px 0 0; }
            body.escura { background: #202124; color: #e8eaed; }
            body.escura input { background: #303134; border-color: #5f6368; color: #e8eaed; }
            body.escura .bolha, body.escura .ajuste { background: #3c4043; color: #e8eaed; }
            body.escura .nome { color: #9aa0a6; }
            body.escura .x { background: #5f6368; color: #fff; }
          </style>
        </head>
        <body id="pagina-inicial-chromebrothers" class="%%CLASSE_CORPO%%">
          <main class="miolo">
            <h1>ChromeBrothers</h1>
            <form id="busca">
              <input id="campo" type="search" placeholder="Pesquise ou digite um URL" autocomplete="off">
            </form>
            <div id="atalhos" class="atalhos"></div>
            <div id="form-atalho" class="form-atalho">
              <input id="nome-atalho" type="text" placeholder="Nome" autocomplete="off">
              <input id="url-atalho" type="url" placeholder="Endereço" autocomplete="off">
              <button id="salvar-atalho" class="ajuste" type="button">Fixar</button>
            </div>
            <p id="aviso" class="aviso"></p>
          </main>
          <script id="estado" type="application/json">%%ESTADO%%</script>
          <script>
            window.ChromeBrothersInicio = {
              navegar: function (token, texto) { prompt("chromebrothers:" + JSON.stringify({a:"navegar", token:token, texto:texto})); },
              definirAparencia: function (token, valor) { prompt("chromebrothers:" + JSON.stringify({a:"aparencia", token:token, valor:valor})); },
              definirMotor: function (token, valor) { prompt("chromebrothers:" + JSON.stringify({a:"motor", token:token, valor:valor})); },
              definirModo: function (token, valor) { prompt("chromebrothers:" + JSON.stringify({a:"modo", token:token, valor:valor})); },
              abrirBiblioteca: function (token) { prompt("chromebrothers:" + JSON.stringify({a:"biblioteca", token:token})); },
              removerAtalho: function (token, endereco) { prompt("chromebrothers:" + JSON.stringify({a:"remover", token:token, endereco:endereco})); },
              adicionarAtalho: function (token, titulo, endereco) {
                return prompt("chromebrothers:" + JSON.stringify({a:"atalho", token:token, titulo:titulo, endereco:endereco})) || "";
              }
            };
          </script>
          <script>
            var token = "%%TOKEN%%";
            var estado = JSON.parse(document.getElementById("estado").textContent);
            var atalhos = estado.atalhos || [];
            document.body.classList.toggle("escura", !!estado.escura);

            function letra(titulo) {
              var texto = (titulo || "?").trim();
              return texto.charAt(0).toUpperCase();
            }
            function render() {
              var caixa = document.getElementById("atalhos");
              caixa.innerHTML = "";
              var biblioteca = document.createElement("button");
              biblioteca.className = "mais";
              biblioteca.type = "button";
              biblioteca.innerHTML = '<span class="bolha">▶</span><span class="nome">Vídeos</span>';
              biblioteca.addEventListener("contextmenu", function (evento) { evento.preventDefault(); });
              biblioteca.addEventListener("click", function () {
                ChromeBrothersInicio.abrirBiblioteca(token);
              });
              caixa.appendChild(biblioteca);
              for (var i = 0; i < atalhos.length; i++) {
                (function (atalho) {
                  var item = document.createElement("div");
                  item.className = "atalho";
                  item.innerHTML = '<span class="bolha"><span class="letra"></span><button class="x" type="button">×</button></span><span class="nome"></span>';
                  item.querySelector(".letra").textContent = letra(atalho.titulo);
                  item.querySelector(".nome").textContent = atalho.titulo;
                  item.querySelector(".x").addEventListener("click", function (evento) {
                    evento.stopPropagation();
                    ChromeBrothersInicio.removerAtalho(token, atalho.endereco);
                    atalhos = atalhos.filter(function (itemAtual) { return itemAtual.endereco !== atalho.endereco; });
                    render();
                  });
                  var pressao = null;
                  var ignorarClique = false;
                  item.addEventListener("pointerdown", function () {
                    ignorarClique = false;
                    pressao = setTimeout(function () {
                      pressao = null;
                      ignorarClique = true;
                      var armados = document.querySelectorAll(".atalho.armado");
                      for (var j = 0; j < armados.length; j++) armados[j].classList.remove("armado");
                      item.classList.add("armado");
                    }, 550);
                  });
                  function soltarPressao() {
                    if (pressao) {
                      clearTimeout(pressao);
                      pressao = null;
                    }
                  }
                  item.addEventListener("pointerup", soltarPressao);
                  item.addEventListener("pointercancel", soltarPressao);
                  item.addEventListener("contextmenu", function (evento) { evento.preventDefault(); });
                  item.addEventListener("click", function () {
                    if (ignorarClique) {
                      ignorarClique = false;
                      return;
                    }
                    ChromeBrothersInicio.navegar(token, atalho.endereco);
                  });
                  caixa.appendChild(item);
                })(atalhos[i]);
              }
              if (atalhos.length < 8) {
                var mais = document.createElement("button");
                mais.className = "mais";
                mais.type = "button";
                mais.innerHTML = '<span class="bolha">+</span><span class="nome">Fixar</span>';
                mais.addEventListener("click", function () {
                  document.getElementById("form-atalho").classList.add("aberto");
                  document.getElementById("nome-atalho").focus();
                });
                caixa.appendChild(mais);
              }
            }
            render();

            document.addEventListener("click", function (evento) {
              var alvo = evento.target;
              if (alvo && alvo.closest && alvo.closest(".atalho")) return;
              var armados = document.querySelectorAll(".atalho.armado");
              for (var i = 0; i < armados.length; i++) armados[i].classList.remove("armado");
            });

            document.getElementById("busca").addEventListener("submit", function (evento) {
              evento.preventDefault();
              ChromeBrothersInicio.navegar(token, document.getElementById("campo").value);
            });
            document.getElementById("salvar-atalho").addEventListener("click", function () {
              var nome = document.getElementById("nome-atalho").value;
              var url = document.getElementById("url-atalho").value;
              var resposta = JSON.parse(ChromeBrothersInicio.adicionarAtalho(token, nome, url));
              var aviso = document.getElementById("aviso");
              if (resposta.resultado === "ok") {
                atalhos.push({ titulo: resposta.titulo || nome || resposta.endereco, endereco: resposta.endereco });
                document.getElementById("nome-atalho").value = "";
                document.getElementById("url-atalho").value = "";
                document.getElementById("form-atalho").classList.remove("aberto");
                aviso.textContent = "";
                render();
              } else if (resposta.resultado === "limite") {
                aviso.textContent = "Dá para fixar até 8 páginas.";
              } else if (resposta.resultado === "repetido") {
                aviso.textContent = "Essa página já está fixada.";
              } else {
                aviso.textContent = "Digite um endereço http ou https.";
              }
            });
          </script>
        </body>
        </html>
    """.trimIndent()
}

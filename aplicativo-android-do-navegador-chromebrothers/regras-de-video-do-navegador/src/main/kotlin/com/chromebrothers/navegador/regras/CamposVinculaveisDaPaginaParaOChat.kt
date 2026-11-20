package com.chromebrothers.navegador.regras

data class CampoVinculavelDaPagina(
    val modo: String,
    val marca: MarcaDoElementoVinculado,
)

sealed class ResultadoDoVinculoPedidoPeloChat {
    data class Pronto(
        val campo: MarcaDoElementoVinculado?,
        val envio: MarcaDoElementoVinculado?,
    ) : ResultadoDoVinculoPedidoPeloChat()

    data class Recusado(val mensagem: String) : ResultadoDoVinculoPedidoPeloChat()
}

object CamposVinculaveisDaPaginaParaOChat {
    const val LIMITE = 40

    fun ler(bruto: String): List<CampoVinculavelDaPagina> {
        if (bruto.isBlank()) return emptyList()
        val vistos = linkedSetOf<String>()
        val itens = mutableListOf<CampoVinculavelDaPagina>()
        bruto.lineSequence().forEach { linha ->
            if (itens.size >= LIMITE) return@forEach
            val partes = linha.split('\t')
            if (partes.size < 6) return@forEach
            val modo = partes[0].trim()
            if (modo != "campo" && modo != "envio") return@forEach
            val marca = marcaLimpa(partes[1], partes[2], partes[3], partes[4], partes[5]) ?: return@forEach
            val chave = "$modo\t${marca.tag}\t${marca.id}\t${marca.nome}\t${marca.rotulo}\t${marca.papel}"
            if (chave in vistos) return@forEach
            vistos += chave
            itens += CampoVinculavelDaPagina(modo, marca)
        }
        return itens
    }

    fun descrever(itens: List<CampoVinculavelDaPagina>): String {
        if (itens.isEmpty()) return "Nenhum campo ou botão visível nesta página."
        val partes = mutableListOf<String>()
        val campos = itens.filter { it.modo == "campo" }
        val envios = itens.filter { it.modo == "envio" }
        if (campos.isNotEmpty()) {
            partes += "Campos da página:"
            partes += campos.joinToString("\n") { linha(it) }
        }
        if (envios.isNotEmpty()) {
            partes += "Botões da página:"
            partes += envios.joinToString("\n") { linha(it) }
        }
        return partes.joinToString("\n")
    }

    fun script(): String = """
        (function(){
          function visivel(item) {
            if (!item || item.disabled) return false;
            if (item.getAttribute("aria-hidden") === "true" || item.hasAttribute("hidden")) return false;
            var estilo = window.getComputedStyle(item);
            if (estilo.display === "none" || estilo.visibility === "hidden") return false;
            var caixa = item.getBoundingClientRect();
            return caixa.width > 0 && caixa.height > 0;
          }
          function limpar(texto, limite) {
            return String(texto || "").replace(/[\t\n\r]/g, " ").replace(/\s+/g, " ").trim().slice(0, limite);
          }
          function linha(modo, item, rotulo) {
            return [
              modo,
              limpar(item.id || "", 80),
              limpar(item.getAttribute("name") || "", 80),
              limpar(rotulo, 40),
              limpar(item.tagName || "", 32),
              limpar(item.getAttribute("role") || "", 40)
            ].join("\t");
          }
          var saida = [];
          var bloqueado = ["button", "checkbox", "file", "hidden", "radio", "submit", "reset", "image", "range", "color"];
          function visitar(item) {
            if (!item || item.nodeType !== 1 || !visivel(item) || saida.length >= $LIMITE) return;
            var tag = item.tagName;
            var papel = item.getAttribute("role") || "";
            var edicao = item.getAttribute("contenteditable");
            var tipo = (item.type || "").toLowerCase();
            if (tag === "TEXTAREA" || papel === "textbox" || edicao === "" || edicao === "true" || edicao === "plaintext-only") {
              saida.push(linha("campo", item, item.getAttribute("placeholder") || item.getAttribute("aria-label") || item.getAttribute("aria-placeholder") || item.getAttribute("title") || ""));
              return;
            }
            if (tag === "INPUT" && bloqueado.indexOf(tipo) < 0) {
              saida.push(linha("campo", item, item.getAttribute("placeholder") || item.getAttribute("aria-label") || item.getAttribute("aria-placeholder") || item.getAttribute("title") || ""));
              return;
            }
            if (tag === "BUTTON" || papel === "button" || tipo === "submit" || tipo === "button") {
              var estavel = item.getAttribute("aria-label") || item.getAttribute("title") || "";
              var escrito = estavel || item.innerText || item.value || "";
              saida.push(linha("envio", item, escrito));
            }
          }
          var lista = document.querySelectorAll("textarea, input, button, [contenteditable], [role='textbox'], [role='button']");
          for (var indice = 0; indice < lista.length; indice++) visitar(lista[indice]);
          return saida.join("\n");
        })()
    """.trimIndent()

    private fun linha(item: CampoVinculavelDaPagina): String {
        val marca = item.marca
        return "- tipo=${item.modo} tag=${marca.tag} id=${marca.id} nome=${marca.nome} rotulo=${marca.rotulo} papel=${marca.papel}"
    }

    private fun marcaLimpa(id: String, nome: String, rotulo: String, tag: String, papel: String): MarcaDoElementoVinculado? {
        return VinculoDoCampoParaOChat.marca(id, nome, rotulo, tag, papel)
    }
}

object DescricaoDoVinculoDoCampoParaOChat {
    fun escrever(vinculo: VinculoDoCampoNoSite?): String {
        val caixa = vinculo?.campo?.let { VinculoDoCampoParaOChat.nomeCurto(it) } ?: "nenhuma"
        val botao = vinculo?.envio?.let { VinculoDoCampoParaOChat.nomeCurto(it) } ?: "nenhum"
        return "Vínculo deste site: caixa $caixa. botão $botao."
    }
}

object VinculoDoCampoParaOChat {
    fun ler(argumentos: String): ResultadoDoVinculoPedidoPeloChat {
        val campoBruto = TextoJsonDoChat.objetoDaChave(argumentos, "campo")
        val envioBruto = TextoJsonDoChat.objetoDaChave(argumentos, "envio")
        if (campoBruto == null && envioBruto == null) {
            return ResultadoDoVinculoPedidoPeloChat.Recusado("Faltou a caixa e o botão.")
        }
        val campo = campoBruto?.let { marcaDoObjeto(it) }
        val envio = envioBruto?.let { marcaDoObjeto(it) }
        if (campoBruto != null && campo == null) {
            return ResultadoDoVinculoPedidoPeloChat.Recusado("Não reconheci a caixa.")
        }
        if (envioBruto != null && envio == null) {
            return ResultadoDoVinculoPedidoPeloChat.Recusado("Não reconheci o botão.")
        }
        return ResultadoDoVinculoPedidoPeloChat.Pronto(campo, envio)
    }

    fun pergunta(campo: MarcaDoElementoVinculado?, envio: MarcaDoElementoVinculado?): String {
        val partes = mutableListOf<String>()
        if (campo != null) partes += "a caixa ${nomeCurto(campo)}"
        if (envio != null) partes += "o botão ${nomeCurto(envio)}"
        return "Vincular ${partes.joinToString(" e ")}?"
    }

    fun nomeCurto(marca: MarcaDoElementoVinculado): String {
        return marca.rotulo.ifBlank { marca.nome.ifBlank { marca.tag } }
    }

    fun scriptDeConferencia(marca: MarcaDoElementoVinculado, modo: String): String {
        val id = js(marca.id)
        val nome = js(marca.nome)
        val rotulo = js(marca.rotulo)
        val tag = js(marca.tag)
        val papel = js(marca.papel)
        val modoJs = js(if (modo == "envio") "envio" else "campo")
        return """
            (function(){
              var id = $id;
              var nome = $nome;
              var rotulo = $rotulo;
              var tag = $tag;
              var papel = $papel;
              var modo = $modoJs;
              function visivel(item) {
                if (!item || item.disabled) return false;
                if (item.getAttribute("aria-hidden") === "true" || item.hasAttribute("hidden")) return false;
                var estilo = window.getComputedStyle(item);
                if (estilo.display === "none" || estilo.visibility === "hidden") return false;
                var caixa = item.getBoundingClientRect();
                return caixa.width > 0 && caixa.height > 0;
              }
              function texto(item) {
                var estavel = item.getAttribute("placeholder") || item.getAttribute("aria-label") || item.getAttribute("aria-placeholder") || item.getAttribute("title") || "";
                if (modo === "envio" && !estavel) estavel = item.innerText || item.value || "";
                return String(estavel).replace(/\s+/g, " ").trim();
              }
              var origem = modo === "envio"
                ? document.querySelectorAll("button, input[type='submit'], input[type='button'], [role='button']")
                : document.querySelectorAll("textarea, input, [contenteditable], [role='textbox']");
              var lista = [];
              for (var indice = 0; indice < origem.length; indice++) {
                if (visivel(origem[indice])) lista.push(origem[indice]);
              }
              if (id) {
                var porId = document.getElementById(id);
                if (porId && porId.tagName && porId.tagName.toUpperCase() === String(tag).toUpperCase() && visivel(porId)) return "ok";
              }
              var item;
              var escrito;
              if (nome) {
                for (indice = 0; indice < lista.length; indice++) {
                  item = lista[indice];
                  if (item.getAttribute && item.getAttribute("name") === nome && item.tagName.toUpperCase() === String(tag).toUpperCase()) return "ok";
                }
              }
              if (rotulo) {
                for (indice = 0; indice < lista.length; indice++) {
                  item = lista[indice];
                  if (item.tagName.toUpperCase() !== String(tag).toUpperCase()) continue;
                  if (papel && (item.getAttribute("role") || "") !== papel) continue;
                  escrito = texto(item);
                  if (escrito && (escrito.indexOf(rotulo) === 0 || rotulo.indexOf(escrito) === 0)) return "ok";
                }
              }
              return "nao";
            })()
        """.trimIndent()
    }

    fun marca(id: String, nome: String, rotulo: String, tag: String, papel: String): MarcaDoElementoVinculado? {
        val tagLimpa = tag.trim().uppercase()
        if (!Regex("[A-Z][A-Z0-9]{0,31}").matches(tagLimpa)) return null
        val idLimpo = EnvioDeTextoParaOCampoDaPagina.idConfiavel(id.trim())
        val nomeLimpo = limpar(nome, 80)
        val rotuloLimpo = limpar(rotulo, 40)
        val papelLimpo = limpar(papel, 40)
        if (idLimpo.isEmpty() && nomeLimpo.isEmpty() && rotuloLimpo.isEmpty()) return null
        return MarcaDoElementoVinculado(idLimpo, nomeLimpo, rotuloLimpo, tagLimpa, papelLimpo)
    }

    private fun marcaDoObjeto(objeto: String): MarcaDoElementoVinculado? {
        return marca(
            TextoJsonDoChat.valorDaChave(objeto, "id").orEmpty(),
            TextoJsonDoChat.valorDaChave(objeto, "nome").orEmpty(),
            TextoJsonDoChat.valorDaChave(objeto, "rotulo").orEmpty(),
            TextoJsonDoChat.valorDaChave(objeto, "tag").orEmpty(),
            TextoJsonDoChat.valorDaChave(objeto, "papel").orEmpty(),
        )
    }

    private fun limpar(texto: String, limite: Int): String {
        return texto.trim().replace('\n', ' ').replace('\t', ' ').take(limite)
    }

    private fun js(texto: String): String = "\"${TextoJsonDoChat.escapar(texto)}\""
}

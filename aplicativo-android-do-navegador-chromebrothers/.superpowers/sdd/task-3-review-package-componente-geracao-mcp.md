# Task 3 review package
## main D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers\regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.kt
Size: 7979
Contains checks will be in reviewer read of files.
## test D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers\regras-de-video-do-navegador\src\test\kotlin\com\chromebrothers\navegador\regras\TesteDoHtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.kt
package com.chromebrothers.navegador.regras

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

class TesteDoHtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao {
    @Test
    fun htmlTemFormularioEChamaPonte() {
        assertEquals("geracao-mcp-imagem", HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.ID)
        val h = HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html()
        assertTrue(h.contains("chromebrothers.listarMcpsGeracao"))
        assertTrue(h.contains("chromebrothers.listarModelosGeracao"))
        assertTrue(h.contains("chromebrothers.escolherImagemGeracao"))
        assertTrue(h.contains("chromebrothers.limparImagemGeracao"))
        assertTrue(h.contains("chromebrothers.gerarImagensMcp"))
        assertTrue(h.contains("__chromebrothersGeracaoMcp"))
        assertTrue(h.contains("__chromebrothersImagemGeracao"))
        assertTrue(h.contains("id=\"status\""))
        assertTrue(h.contains("id=\"grade\""))
        assertTrue(h.length < 200_000)
    }

    @Test
    fun metadadosEEstiloSemTemaRoxo() {
        assertTrue(HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.NOME.contains("Geração MCP"))
        assertEquals(
            listOf("mcp", "higgsfield", "imagem"),
            HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.TAGS,
        )
        val h = HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html()
        assertTrue(h.contains("#f6f7f9"))
        assertTrue(h.contains("#1a1a1a"))
        assertFalse(h.contains("purple", ignoreCase = true))
    }
}

## html() head (first 80 lines of kotlin file)
package com.chromebrothers.navegador.regras

/** HTML empacotado do componente "Geração MCP — imagem" da coleção flutuante. */
object HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao {
    const val ID: String = "geracao-mcp-imagem"
    const val NOME: String = "Geração MCP — imagem"
    val TAGS: List<String> = listOf("mcp", "higgsfield", "imagem")

    fun html(): String = HTML

    private val HTML: String = """<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Geração MCP — imagem</title>
<style>
* { box-sizing: border-box; }
body { margin: 0; padding: 16px; background: #f6f7f9; color: #1a1a1a; font: 15px/1.4 system-ui, sans-serif; }
main { max-width: 560px; margin: 0 auto; display: flex; flex-direction: column; gap: 14px; }
h1 { font-size: 18px; margin: 0; font-weight: 600; }
label { display: flex; flex-direction: column; gap: 6px; font-weight: 600; font-size: 13px; }
select, textarea, input[type=number] { font: inherit; font-weight: 400; color: #1a1a1a; background: #fff; border: 1px solid #c9ced6; border-radius: 6px; padding: 10px; width: 100%; }
textarea { min-height: 110px; resize: vertical; }
.linha { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
button { font: inherit; color: #1a1a1a; background: #fff; border: 1px solid #c9ced6; border-radius: 6px; padding: 10px 14px; min-height: 44px; }
button.principal { background: #1a1a1a; color: #fff; border-color: #1a1a1a; font-weight: 600; width: 100%; }
button:disabled { opacity: 0.5; }
#previa { max-width: 120px; max-height: 120px; border-radius: 6px; border: 1px solid #c9ced6; display: none; }
#nomeRef { font-size: 13px; color: #4a4f57; }
#status { min-height: 20px; font-size: 14px; color: #4a4f57; }
#status.erro { color: #b00020; }
#grade { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
#grade img { width: 100%; border-radius: 6px; border: 1px solid #c9ced6; background: #fff; display: block; }
</style>
</head>
<body>
<main>
<h1>Geração MCP — imagem</h1>
<label>MCP
<select id="selMcp"></select>
</label>
<label>Prompt
<textarea id="prompt" maxlength="4000" placeholder="Descreva a imagem"></textarea>
</label>
<div>
<div style="font-weight:600;font-size:13px;margin-bottom:6px">Imagem de referência (opcional)</div>
<div class="linha">
<img id="previa" alt="Referência">
<span id="nomeRef"></span>
</div>
<div class="linha" style="margin-top:8px">
<button type="button" id="btnGaleria">Galeria</button>
<button type="button" id="btnBiblioteca">Biblioteca</button>
<button type="button" id="btnLimpar">Limpar</button>
</div>
</div>
<label>Modelo
<select id="selModelo"></select>
</label>
<label>Gerações (1–4)
<input type="number" id="qtd" min="1" max="4" value="1">
</label>
<button type="button" id="btnGerar" class="principal">Gerar</button>
<div id="status" role="status"></div>
<div id="grade"></div>
</main>
<script>
var selMcp = document.getElementById("selMcp");
var selModelo = document.getElementById("selModelo");
var ta = document.getElementById("prompt");
var qtd = document.getElementById("qtd");
var btn = document.getElementById("btnGerar");
var elStatus = document.getElementById("status");
var grade = document.getElementById("grade");
var previa = document.getElementById("previa");
var nomeRef = document.getElementById("nomeRef");
var temRef = false;

function ponte(chamada) {

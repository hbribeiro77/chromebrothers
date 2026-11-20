package com.chromebrothers.navegador

import android.webkit.JavascriptInterface
import com.chromebrothers.navegador.regras.ResultadoDoAtalhoDaPaginaInicial
import org.json.JSONObject

class PonteJavascriptDaPaginaInicialDoNavegadorChromebrothers(
    private val tokenEsperado: String,
    private val aoNavegar: (String) -> Unit,
    private val aoDefinirAparencia: (Boolean) -> Unit,
    private val aoDefinirMotor: (String) -> Unit,
    private val aoDefinirModo: (String) -> Unit,
    private val aoAdicionarAtalho: (String, String) -> ResultadoDoAtalhoDaPaginaInicial,
    private val aoRemoverAtalho: (String) -> Unit,
    private val aoAbrirBiblioteca: () -> Unit,
) {
    @JavascriptInterface
    fun navegar(token: String, texto: String) {
        if (token != tokenEsperado) return
        aoNavegar(texto)
    }

    @JavascriptInterface
    fun definirAparencia(token: String, valor: String) {
        if (token != tokenEsperado) return
        aoDefinirAparencia(valor == "escura")
    }

    @JavascriptInterface
    fun definirMotor(token: String, valor: String) {
        if (token != tokenEsperado) return
        aoDefinirMotor(valor)
    }

    @JavascriptInterface
    fun definirModo(token: String, valor: String) {
        if (token != tokenEsperado) return
        aoDefinirModo(valor)
    }

    @JavascriptInterface
    fun adicionarAtalho(token: String, titulo: String, endereco: String): String {
        if (token != tokenEsperado) return json("negado", "", "")
        val url = enderecoPronto(endereco) ?: return json("invalido", "", "")
        return when (val resultado = aoAdicionarAtalho(titulo, url)) {
            ResultadoDoAtalhoDaPaginaInicial.ADICIONADO -> json("ok", titulo.ifBlank { url }, url)
            ResultadoDoAtalhoDaPaginaInicial.REPETIDO -> json("repetido", "", url)
            ResultadoDoAtalhoDaPaginaInicial.LIMITE -> json("limite", "", url)
            ResultadoDoAtalhoDaPaginaInicial.INVALIDO -> json("invalido", "", url)
        }
    }

    @JavascriptInterface
    fun abrirBiblioteca(token: String) {
        if (token != tokenEsperado) return
        aoAbrirBiblioteca()
    }

    @JavascriptInterface
    fun removerAtalho(token: String, endereco: String) {
        if (token != tokenEsperado) return
        aoRemoverAtalho(endereco)
    }

    private fun enderecoPronto(endereco: String): String? {
        val texto = endereco.trim()
        if (texto.isEmpty() || texto.any { it.isWhitespace() }) return null
        if (texto.startsWith("http://") || texto.startsWith("https://")) return texto
        if (!texto.contains(".")) return null
        return "https://$texto"
    }

    private fun json(resultado: String, titulo: String, endereco: String): String {
        return JSONObject()
            .put("resultado", resultado)
            .put("titulo", titulo)
            .put("endereco", endereco)
            .toString()
    }
}

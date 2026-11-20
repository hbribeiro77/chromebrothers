package com.chromebrothers.navegador

import android.webkit.JavascriptInterface
import com.chromebrothers.navegador.regras.LeituraDaMensagemDeVideoTocando
import com.chromebrothers.navegador.regras.MensagemDeVideoTocandoNaPagina

class PonteJavascriptParaDetectarVideoNaPagina(
    private val aoTocar: (MensagemDeVideoTocandoNaPagina) -> Unit,
    private val aoParar: () -> Unit,
    private val aoLimites: (String, Float, Float, Float, Float, Float, Float) -> Unit,
    private val aoToqueLongo: (String, String) -> Unit,
    private val aoMidia: (String, String, String) -> Unit,
) {
    @JavascriptInterface
    fun onVideoPlaying(id: String, endereco: String, velocidade: String) {
        aoTocar(LeituraDaMensagemDeVideoTocando.ler(id, endereco, velocidade))
    }

    @JavascriptInterface
    fun onVideoStopped() {
        aoParar()
    }

    @JavascriptInterface
    fun onVideoBounds(
        id: String,
        esquerda: String,
        topo: String,
        largura: String,
        altura: String,
        larguraViewport: String,
        alturaViewport: String,
    ) {
        aoLimites(
            id,
            esquerda.toFloatOrNull() ?: return,
            topo.toFloatOrNull() ?: return,
            largura.toFloatOrNull() ?: return,
            altura.toFloatOrNull() ?: return,
            larguraViewport.toFloatOrNull() ?: return,
            alturaViewport.toFloatOrNull() ?: return,
        )
    }

    @JavascriptInterface
    fun onVideoLongPress(id: String, endereco: String) {
        aoToqueLongo(id, endereco)
    }

    @JavascriptInterface
    fun onMediaUrl(endereco: String, tipo: String) {
        aoMidia(endereco, tipo, "")
    }

    @JavascriptInterface
    fun onMediaUrl(endereco: String, tipo: String, tamanho: String) {
        aoMidia(endereco, tipo, tamanho)
    }
}

package com.chromebrothers.navegador

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.chromebrothers.navegador.regras.ContextoDaPaginaParaOChat
import com.chromebrothers.navegador.regras.LeituraEmSegundoPlanoParaOChat
import org.json.JSONArray

/**
 * Carrega uma página numa WebView escondida (atrás de tudo, transparente, sem toque) e devolve
 * o texto, sem mexer na aba do usuário. Guarda a última leitura para o modelo paginar com offset
 * sem recarregar. Uma leitura por vez.
 */
class LeitorDePaginaEmSegundoPlanoParaOChat(private val activity: AppCompatActivity) {
    private class Lida(val pedido: String, val titulo: String, val endereco: String, val texto: String)

    private val principal = Handler(Looper.getMainLooper())
    private var ultima: Lida? = null
    private var atual: WebView? = null
    private var geracao = 0

    @SuppressLint("SetJavaScriptEnabled")
    fun ler(endereco: String, aoFim: (titulo: String, endereco: String, texto: String, erro: String) -> Unit) {
        ultima?.takeIf { it.pedido == endereco }?.let {
            aoFim(it.titulo, it.endereco, it.texto, "")
            return
        }
        encerrar()
        val raiz = activity.findViewById<ViewGroup>(R.id.raiz_do_navegador)
        if (raiz == null) {
            aoFim("", endereco, "", "a tela do navegador não está pronta")
            return
        }
        val minha = ++geracao
        var terminou = false
        val web = WebView(activity)
        atual = web
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.blockNetworkImage = true
        web.settings.mediaPlaybackRequiresUserGesture = true
        web.alpha = 0f
        web.isFocusable = false
        web.isClickable = false
        web.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        raiz.addView(web, 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        fun concluir(texto: String, erro: String) {
            if (terminou || minha != geracao) return
            terminou = true
            principal.removeCallbacksAndMessages(null)
            val titulo = web.title.orEmpty()
            val final = web.url.orEmpty().ifBlank { endereco }
            if (erro.isBlank()) ultima = Lida(endereco, titulo, final, texto)
            encerrar()
            aoFim(titulo, final, texto, erro)
        }

        fun extrair(tempoEsgotado: Boolean) {
            if (terminou || minha != geracao) return
            web.evaluateJavascript(ContextoDaPaginaParaOChat.scriptDoTextoCompletoNormalizado()) { bruto ->
                val texto = decodificar(bruto).take(LIMITE_DO_TEXTO)
                if (tempoEsgotado && texto.isBlank()) concluir("", "tempo esgotado") else concluir(texto, "")
            }
        }

        val esperarEExtrair = Runnable { extrair(tempoEsgotado = false) }
        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                principal.removeCallbacks(esperarEExtrair)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                principal.removeCallbacks(esperarEExtrair)
                principal.postDelayed(esperarEExtrair, LeituraEmSegundoPlanoParaOChat.ESPERA_DEPOIS_DE_CARREGAR_MS)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) concluir("", "erro de rede (${error?.description ?: "sem detalhe"})")
            }

            // Só http e https; links para apps (intent:, market:) não abrem nada em segundo plano.
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val esquema = request?.url?.scheme?.lowercase().orEmpty()
                return esquema != "http" && esquema != "https"
            }
        }
        principal.postDelayed({ extrair(tempoEsgotado = true) }, LeituraEmSegundoPlanoParaOChat.TEMPO_MAXIMO_MS)
        web.loadUrl(endereco)
    }

    fun encerrar() {
        val web = atual ?: return
        atual = null
        geracao++
        principal.removeCallbacksAndMessages(null)
        runCatching {
            (web.parent as? ViewGroup)?.removeView(web)
            web.stopLoading()
            web.destroy()
        }
    }

    private fun decodificar(bruto: String?): String {
        if (bruto.isNullOrBlank() || bruto == "null") return ""
        return runCatching { JSONArray("[$bruto]").getString(0) }.getOrDefault("")
    }

    private companion object {
        const val LIMITE_DO_TEXTO = 300_000
    }
}

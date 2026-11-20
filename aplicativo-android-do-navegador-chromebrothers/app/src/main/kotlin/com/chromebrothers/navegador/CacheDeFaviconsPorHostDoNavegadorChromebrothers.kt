package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.chromebrothers.navegador.regras.IconeDaPaginaNaBarraDeEndereco
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Favicons por host (arquivo + memória). Fallback na UI continua sendo a letra.
 */
class CacheDeFaviconsPorHostDoNavegadorChromebrothers(
    contexto: Context,
) {
    private val pasta = File(contexto.applicationContext.filesDir, "favicons-por-host")
    private val memoria = ConcurrentHashMap<String, Bitmap>()
    private val pedindo = ConcurrentHashMap.newKeySet<String>()
    private val executor = Executors.newSingleThreadExecutor()

    fun ler(enderecoOuHost: String): Bitmap? {
        val host = normalizar(enderecoOuHost) ?: return null
        memoria[host]?.let { return it }
        val arquivo = arquivo(host)
        if (!arquivo.isFile) return null
        val bmp = runCatching {
            BitmapFactory.decodeFile(arquivo.absolutePath)
        }.getOrNull() ?: return null
        memoria[host] = bmp
        return bmp
    }

    fun tem(enderecoOuHost: String): Boolean {
        val host = normalizar(enderecoOuHost) ?: return false
        return memoria.containsKey(host) || arquivo(host).isFile
    }

    fun guardarSeValido(enderecoOuHost: String, bytes: ByteArray) {
        val host = normalizar(enderecoOuHost) ?: return
        if (bytes.size < 32 || bytes.size > 512_000) return
        val bruto = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return
        val lado = 72
        val bmp = if (bruto.width == lado && bruto.height == lado) {
            bruto
        } else {
            Bitmap.createScaledBitmap(bruto, lado, lado, true).also {
                if (it !== bruto) bruto.recycle()
            }
        }
        pasta.mkdirs()
        runCatching {
            arquivo(host).outputStream().use { saida ->
                bmp.compress(Bitmap.CompressFormat.PNG, 90, saida)
            }
        }
        memoria[host] = bmp
    }

    fun pedirBaixar(
        enderecoDaPagina: String,
        urlDoIcone: String?,
        aoProntoNaUi: (() -> Unit)? = null,
        postarNaUi: (Runnable) -> Unit,
    ) {
        val host = normalizar(enderecoDaPagina) ?: return
        if (tem(host)) return
        if (!pedindo.add(host)) return
        val candidatos = mutableListOf<String>()
        urlDoIcone?.trim()?.takeIf { it.startsWith("http") }?.let { candidatos.add(it) }
        candidatos.add("https://$host/favicon.ico")
        if (!host.startsWith("www.")) {
            candidatos.add("https://www.$host/favicon.ico")
        }
        executor.execute {
            try {
                for (url in candidatos.distinct()) {
                    val bytes = baixar(url) ?: continue
                    guardarSeValido(host, bytes)
                    if (tem(host)) break
                }
            } finally {
                pedindo.remove(host)
                if (aoProntoNaUi != null && tem(host)) {
                    postarNaUi { aoProntoNaUi() }
                }
            }
        }
    }

    fun apagar(enderecoOuHost: String) {
        val host = normalizar(enderecoOuHost) ?: return
        memoria.remove(host)
        arquivo(host).delete()
    }

    private fun arquivo(host: String): File {
        val seguro = host.replace(Regex("[^a-z0-9._-]"), "_")
        return File(pasta, "$seguro.png")
    }

    private fun normalizar(enderecoOuHost: String): String? {
        val bruto = enderecoOuHost.trim()
        if (bruto.isEmpty()) return null
        val host = if (bruto.contains("://") || bruto.contains("/")) {
            IconeDaPaginaNaBarraDeEndereco.hospedeiro(bruto)
        } else {
            bruto.lowercase().removePrefix("www.")
        }
        return host.takeIf { it.isNotBlank() && it.contains('.') }
    }

    private fun baixar(url: String): ByteArray? {
        return runCatching {
            val conexao = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4_000
                readTimeout = 4_000
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("User-Agent", "ChromeBrothers/1.0")
            }
            try {
                if (conexao.responseCode !in 200..299) return null
                val tipo = conexao.contentType.orEmpty().lowercase()
                if (tipo.isNotEmpty() &&
                    !tipo.contains("image") &&
                    !tipo.contains("icon") &&
                    !tipo.contains("octet-stream")
                ) {
                    return null
                }
                conexao.inputStream.use { it.readBytes() }
            } finally {
                conexao.disconnect()
            }
        }.getOrNull()
    }

    companion object {
        fun scriptDoHrefDoFavicon(): String = """
            (function(){
              var l=document.querySelector('link[rel="icon"],link[rel="shortcut icon"],link[rel*="icon"]');
              if(l && l.href) return String(l.href);
              try{ return location.origin+'/favicon.ico'; }catch(e){ return ''; }
            })();
        """.trimIndent()
    }
}

package com.chromebrothers.navegador

import android.content.Context
import com.chromebrothers.navegador.regras.ListaDeBloqueioDeAnuncio
import com.chromebrothers.navegador.regras.TextoDaListaDeBloqueioDeAnuncio
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class AtualizacaoDaListaDeBloqueioDeAnuncioDoNavegador(
    contexto: Context,
    private val instanteSalvo: () -> Long,
    private val gravarInstante: (Long) -> Unit,
    private val aoCarregar: (ListaDeBloqueioDeAnuncio) -> Unit,
) {
    private val executor = Executors.newSingleThreadExecutor()
    private val arquivo = File(contexto.filesDir, "lista-easylist-de-bloqueio-de-anuncio.txt")

    fun carregar() {
        executor.execute {
            val guardado = lerArquivo()
            if (guardado != null) aoCarregar(TextoDaListaDeBloqueioDeAnuncio.ler(guardado))
            if (!precisaBaixar()) return@execute
            val baixado = baixar() ?: return@execute
            arquivo.writeText(baixado)
            gravarInstante(System.currentTimeMillis())
            aoCarregar(TextoDaListaDeBloqueioDeAnuncio.ler(baixado))
        }
    }

    private fun precisaBaixar(): Boolean {
        if (!arquivo.isFile || arquivo.length() < 1000L) return true
        return System.currentTimeMillis() - instanteSalvo() > QUATRO_DIAS
    }

    private fun lerArquivo(): String? {
        if (!arquivo.isFile || arquivo.length() < 1000L) return null
        return try {
            arquivo.readText()
        } catch (_: Exception) {
            null
        }
    }

    private fun baixar(): String? {
        ENDERECOS.forEach { endereco ->
            val texto = baixarDe(endereco)
            if (texto != null) return texto
        }
        return null
    }

    private fun baixarDe(endereco: String): String? {
        return try {
            val conexao = URL(endereco).openConnection() as HttpURLConnection
            conexao.instanceFollowRedirects = true
            conexao.connectTimeout = 15_000
            conexao.readTimeout = 30_000
            conexao.setRequestProperty("User-Agent", "ChromeBrothers")
            if (conexao.responseCode !in 200..299) return null
            conexao.inputStream.bufferedReader().use { leitor ->
                val texto = leitor.readText().trimStart('\uFEFF')
                if (!texto.contains("\n||") || texto.length < 1000) null else texto
            }
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        const val QUATRO_DIAS = 4L * 24 * 60 * 60 * 1000
        val ENDERECOS = listOf(
            "https://easylist.to/easylist/easylist.txt",
            "https://easylist-downloads.adblockplus.org/easylist.txt",
        )
    }
}

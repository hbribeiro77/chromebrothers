package com.chromebrothers.navegador

import android.webkit.CookieManager
import com.chromebrothers.navegador.regras.LeituraDoTamanhoNoCabecalhoContentRange
import java.net.HttpURLConnection
import java.net.URI

object MedidaDoTamanhoDoPedidoDeMidiaDetectadaNaPagina {
    fun medir(endereco: String, referer: String, agente: String): Long {
        val conexao = try {
            URI(endereco).toURL().openConnection() as HttpURLConnection
        } catch (_: Exception) {
            return -1
        }
        return try {
            conexao.instanceFollowRedirects = true
            conexao.connectTimeout = 8_000
            conexao.readTimeout = 8_000
            conexao.setRequestProperty("User-Agent", agente.ifBlank { "Mozilla/5.0" })
            conexao.setRequestProperty("Accept", "*/*")
            if (referer.isNotBlank()) conexao.setRequestProperty("Referer", referer)
            val cookie = try {
                CookieManager.getInstance().getCookie(endereco)
            } catch (_: Exception) {
                null
            }
            if (!cookie.isNullOrBlank()) conexao.setRequestProperty("Cookie", cookie)
            conexao.setRequestProperty("Range", "bytes=0-0")
            val codigo = conexao.responseCode
            val pelaFaixa = LeituraDoTamanhoNoCabecalhoContentRange.total(conexao.getHeaderField("Content-Range"))
            if (pelaFaixa > 0) return pelaFaixa
            if (codigo == HttpURLConnection.HTTP_OK) {
                conexao.getHeaderField("Content-Length")?.toLongOrNull() ?: -1L
            } else {
                -1L
            }
        } catch (_: Exception) {
            -1L
        } finally {
            conexao.disconnect()
        }
    }
}

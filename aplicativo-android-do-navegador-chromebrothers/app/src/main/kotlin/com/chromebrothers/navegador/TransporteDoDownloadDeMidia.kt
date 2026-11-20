package com.chromebrothers.navegador

import android.webkit.CookieManager
import org.mozilla.geckoview.GeckoWebExecutor
import org.mozilla.geckoview.WebRequest
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI

/** Resposta HTTP do download, igual para o motor do Firefox e para a conexão comum. */
class RespostaDoDownloadDeMidia(
    val codigo: Int,
    private val cabecalhos: Map<String, String>,
    val corpo: InputStream,
    private val fechar: () -> Unit,
) : Closeable {
    fun cabecalho(nome: String): String? = cabecalhos.entries.firstOrNull { it.key.equals(nome, ignoreCase = true) }?.value

    val tamanho: Long get() = cabecalho("Content-Length")?.trim()?.toLongOrNull() ?: -1L

    override fun close() {
        runCatching { fechar() }
    }
}

fun interface TransporteDoDownloadDeMidia {
    fun abrir(endereco: String, intervalo: String?, referer: String, agente: String): RespostaDoDownloadDeMidia
}

/**
 * Pelo motor do Firefox (Gecko): usa os mesmos cookies e a mesma sessão das páginas, então o download
 * sai "logado", como o player do site. Chamado fora da thread principal.
 */
class TransportePeloMotorDoFirefox(private val executor: GeckoWebExecutor) : TransporteDoDownloadDeMidia {
    override fun abrir(endereco: String, intervalo: String?, referer: String, agente: String): RespostaDoDownloadDeMidia {
        val pedido = WebRequest.Builder(endereco)
            .method("GET")
            .header("Accept", "*/*")
        if (agente.isNotBlank()) pedido.header("User-Agent", agente)
        if (referer.isNotBlank()) pedido.referrer(referer)
        if (intervalo != null) pedido.header("Range", intervalo)
        val resposta = executor.fetch(pedido.build()).poll(TEMPO_PARA_RESPONDER_MS)
            ?: throw java.io.IOException("O motor do Firefox não respondeu.")
        resposta.setReadTimeoutMillis(TEMPO_DE_LEITURA_MS)
        val corpo = resposta.body ?: ByteArrayInputStream(ByteArray(0))
        return RespostaDoDownloadDeMidia(resposta.statusCode, resposta.headers, corpo) { corpo.close() }
    }

    private companion object {
        const val TEMPO_PARA_RESPONDER_MS = 30_000L
        const val TEMPO_DE_LEITURA_MS = 30_000L
    }
}

/** Conexão comum (reserva): cookies só do WebView do Chrome, que as páginas do Gecko não usam. */
class TransportePorConexaoComum : TransporteDoDownloadDeMidia {
    override fun abrir(endereco: String, intervalo: String?, referer: String, agente: String): RespostaDoDownloadDeMidia {
        val conexao = URI(endereco).toURL().openConnection() as HttpURLConnection
        conexao.instanceFollowRedirects = true
        conexao.connectTimeout = 20_000
        conexao.readTimeout = 30_000
        conexao.setRequestProperty("User-Agent", agente.ifBlank { "Mozilla/5.0" })
        conexao.setRequestProperty("Accept", "*/*")
        if (referer.isNotBlank()) conexao.setRequestProperty("Referer", referer)
        val cookie = runCatching { CookieManager.getInstance().getCookie(endereco) }.getOrNull()
        if (!cookie.isNullOrBlank()) conexao.setRequestProperty("Cookie", cookie)
        if (intervalo != null) conexao.setRequestProperty("Range", intervalo)
        val codigo = conexao.responseCode
        val cabecalhos = conexao.headerFields
            .filterKeys { it != null }
            .mapValues { (_, valores) -> valores.joinToString(",") }
        val corpo = if (codigo in 200..299) conexao.inputStream else (conexao.errorStream ?: ByteArrayInputStream(ByteArray(0)))
        return RespostaDoDownloadDeMidia(codigo, cabecalhos, corpo) {
            runCatching { corpo.close() }
            conexao.disconnect()
        }
    }
}

/**
 * Tenta o motor do Firefox (sessão logada). Se ele falhar na rede, ou se o site recusar o pedido dele
 * (4xx/5xx), tenta a conexão comum — o caminho de antes, que funciona em CDNs com link assinado como o
 * do Boosty. Fica com a resposta que deu certo; se as duas falharem, fica com a do Firefox.
 */
class TransporteComReserva(
    private val principal: TransporteDoDownloadDeMidia?,
    private val reserva: TransporteDoDownloadDeMidia,
) : TransporteDoDownloadDeMidia {
    override fun abrir(endereco: String, intervalo: String?, referer: String, agente: String): RespostaDoDownloadDeMidia {
        val primeiro = principal ?: return reserva.abrir(endereco, intervalo, referer, agente)
        val doFirefox = try {
            primeiro.abrir(endereco, intervalo, referer, agente)
        } catch (erro: DownloadCancelado) {
            throw erro
        } catch (_: Throwable) {
            return reserva.abrir(endereco, intervalo, referer, agente)
        }
        if (doFirefox.codigo < 400) return doFirefox
        val comum = try {
            reserva.abrir(endereco, intervalo, referer, agente)
        } catch (erro: DownloadCancelado) {
            doFirefox.close()
            throw erro
        } catch (_: Throwable) {
            return doFirefox
        }
        return if (comum.codigo < 400) {
            doFirefox.close()
            comum
        } else {
            comum.close()
            doFirefox
        }
    }
}

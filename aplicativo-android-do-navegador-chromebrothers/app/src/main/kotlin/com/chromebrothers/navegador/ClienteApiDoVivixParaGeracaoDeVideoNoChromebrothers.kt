package com.chromebrothers.navegador

import android.util.Log
import com.chromebrothers.navegador.regras.PedidoDeVideoDoVivixW1
import com.chromebrothers.navegador.regras.SituacaoDoJobVivix
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Vivix W1 pela API REST (`https://api.vivix.ai`, `Authorization: Bearer <token>`).
 * Fluxo: upload das referências (`/v1/files/images|videos` → `data.url`) →
 * `POST /v1/videos/jobs` → poll `GET /v1/videos/jobs/{job_id}` até `succeeded` → baixa o MP4.
 * O envio do job nunca é repetido sozinho (cada envio gasta crédito).
 */
class ClienteApiDoVivixParaGeracaoDeVideoNoChromebrothers(
    private val sessao: SessaoDaContaVivixNoAparelho,
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()
    private val download = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()
    private val jsonTipo = "application/json".toMediaType()

    private class ErroDaVivix(mensagem: String) : Exception(mensagem)

    fun gerarVideo(
        prompt: String,
        modelo: String,
        aspectRatio: String = PedidoDeVideoDoVivixW1.PROPORCAO_PADRAO,
        resolution: String = PedidoDeVideoDoVivixW1.RESOLUCAO_PADRAO,
        duracaoSegundos: Int = PedidoDeVideoDoVivixW1.DURACAO_PADRAO,
        referenciasLocais: List<Pair<ByteArray, String>> = emptyList(),
        videosLocais: List<File> = emptyList(),
        aoCriarTarefa: (jobId: String) -> Unit = {},
    ): ResultadoDaGeracaoMcpHiggsfield {
        if (!sessao.temSessao()) return ResultadoDaGeracaoMcpHiggsfield.Erro(SessaoDaContaVivixNoAparelho.SEM_SESSAO)
        if (prompt.isBlank()) return ResultadoDaGeracaoMcpHiggsfield.Erro("O Vivix W1 exige um prompt.")
        val imagens = referenciasLocais.filter { it.first.isNotEmpty() }
        PedidoDeVideoDoVivixW1.validarImagens(imagens.map { it.first.size.toLong() })
            ?.let { return ResultadoDaGeracaoMcpHiggsfield.Erro(it) }
        // Durações já foram validadas no serviço (ReferenciaDeVideoNaGeracaoMcp); aqui confere tamanho/quantidade.
        PedidoDeVideoDoVivixW1.validarVideos(videosLocais.map { it.length() }, emptyList())
            ?.let { return ResultadoDaGeracaoMcpHiggsfield.Erro(it) }
        return try {
            val urlsDeImagem = imagens.map { (bytes, nome) -> enviarImagem(bytes, nome) }
            val urlsDeVideo = videosLocais.map { enviarVideo(it) }
            val ajustado = PedidoDeVideoDoVivixW1.ajustar(duracaoSegundos, resolution, aspectRatio)
            val corpo = PedidoDeVideoDoVivixW1.corpoDoJob(modelo, prompt, ajustado, urlsDeImagem, urlsDeVideo)
            registrar("POST /v1/videos/jobs ${corpo.take(800)}")
            val dados = chamar(
                Request.Builder().url("${PedidoDeVideoDoVivixW1.ORIGEM_DA_API}/v1/videos/jobs")
                    .post(corpo.toRequestBody(jsonTipo)),
                naoRepetir = true,
            )
            val primeiro = dados.optJSONArray("results")?.optJSONObject(0)
            val jobId = primeiro?.optString("job_id").orEmpty().ifBlank { dados.optString("job_id") }
            if (!PedidoDeVideoDoVivixW1.ehJobId(jobId)) {
                return ResultadoDaGeracaoMcpHiggsfield.Erro(
                    "A Vivix aceitou o pedido, mas não devolveu um job acompanhável. O envio não foi repetido.",
                )
            }
            aoCriarTarefa(jobId)
            esperarEBaixar(jobId)
        } catch (e: ErroDaVivix) {
            ResultadoDaGeracaoMcpHiggsfield.Erro(e.message ?: "A Vivix recusou o pedido.")
        } catch (e: Exception) {
            registrar("gerarVideo falhou: ${e.message}")
            ResultadoDaGeracaoMcpHiggsfield.Erro(
                "Falha de rede com a Vivix (${e.message ?: e.javaClass.simpleName}). Confira os jobs antes de reenviar.",
            )
        }
    }

    private fun esperarEBaixar(jobId: String): ResultadoDaGeracaoMcpHiggsfield {
        val inicio = System.currentTimeMillis()
        while (System.currentTimeMillis() - inicio < TIMEOUT_MS) {
            when (val consulta = consultarVideoDoJobUmaVez(jobId)) {
                is ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Pronto ->
                    return ResultadoDaGeracaoMcpHiggsfield.Pronto(consulta.bytes, consulta.enderecoRemoto, jobId, imagem = false)
                is ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou ->
                    return ResultadoDaGeracaoMcpHiggsfield.Erro(consulta.mensagem)
                ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.AindaGerando -> Thread.sleep(POLL_MS)
            }
        }
        return ResultadoDaGeracaoMcpHiggsfield.Erro("A geração Vivix está demorando (job $jobId). Toque em Verificar depois.")
    }

    /** Uma consulta do job; erro de rede vira [AindaGerando] (o job continua na Vivix). */
    fun consultarVideoDoJobUmaVez(jobId: String): ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa {
        if (!sessao.temSessao()) {
            return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(SessaoDaContaVivixNoAparelho.SEM_SESSAO)
        }
        if (!PedidoDeVideoDoVivixW1.ehJobId(jobId)) {
            return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou("Identificador de job Vivix inválido.")
        }
        val dados = try {
            chamar(Request.Builder().url("${PedidoDeVideoDoVivixW1.ORIGEM_DA_API}/v1/videos/jobs/$jobId").get())
        } catch (e: ErroDaVivix) {
            return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(e.message ?: "Falha ao consultar a Vivix.")
        } catch (e: Exception) {
            registrar("poll $jobId erro: ${e.message}")
            return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.AindaGerando
        }
        val status = dados.optString("status")
        registrar("poll $jobId status=$status")
        return when (PedidoDeVideoDoVivixW1.situacaoDoJob(status)) {
            SituacaoDoJobVivix.GERANDO -> ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.AindaGerando
            SituacaoDoJobVivix.FALHOU -> {
                val erro = dados.optJSONObject("error")
                ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(
                    PedidoDeVideoDoVivixW1.mensagemDaFalhaDoJob(
                        erro?.optString("code").orEmpty(),
                        erro?.optString("message").orEmpty(),
                    ),
                )
            }
            SituacaoDoJobVivix.PRONTO -> {
                val url = dados.optString("video_url")
                if (!url.startsWith("https://")) {
                    return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou("A Vivix terminou, mas não devolveu o endereço do vídeo.")
                }
                val bytes = baixarBytes(url)
                    ?: return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou("Job pronto, mas não baixei o vídeo da Vivix.")
                ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Pronto(bytes, url)
            }
        }
    }

    private fun enviarImagem(bytes: ByteArray, nome: String): String {
        val mime = mimeDaImagem(bytes)
        val ext = when (mime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val arquivo = nome.substringBeforeLast('.').ifBlank { "referencia" } + ".$ext"
        return enviarArquivo("/v1/files/images", arquivo, bytes.toRequestBody(mime.toMediaType()))
    }

    private fun enviarVideo(video: File): String {
        val mime = when (video.extension.lowercase()) {
            "mov" -> "video/quicktime"
            "webm" -> "video/webm"
            else -> "video/mp4"
        }
        return enviarArquivo("/v1/files/videos", video.name.ifBlank { "referencia.mp4" }, video.asRequestBody(mime.toMediaType()))
    }

    private fun enviarArquivo(caminho: String, nome: String, corpo: RequestBody): String {
        val multipart = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", nome, corpo)
            .build()
        val dados = chamar(Request.Builder().url("${PedidoDeVideoDoVivixW1.ORIGEM_DA_API}$caminho").post(multipart))
        val url = dados.optString("url")
        registrar("upload $caminho → ${url.take(120)}")
        if (!url.startsWith("https://")) throw ErroDaVivix("A Vivix não devolveu o endereço da referência enviada.")
        return url
    }

    /** Faz a chamada e devolve o `data` do envelope `{code, message, data}`; erro da API vira [ErroDaVivix]. */
    private fun chamar(construtor: Request.Builder, naoRepetir: Boolean = false): JSONObject {
        val token = sessao.apiToken()
        if (token.isBlank()) throw ErroDaVivix(SessaoDaContaVivixNoAparelho.SEM_SESSAO)
        val req = construtor.header("Authorization", "Bearer $token").build()
        http.newCall(req).execute().use { resp ->
            val texto = resp.body?.string().orEmpty()
            if (resp.code in 300..399) throw ErroDaVivix("A Vivix tentou redirecionar o pedido.")
            val json = runCatching { JSONObject(texto) }.getOrNull()
            val codigo = json?.takeIf { it.has("code") }?.optInt("code")
            if (!resp.isSuccessful || (codigo != null && codigo != 0)) {
                registrar("HTTP ${resp.code} code=$codigo ${texto.take(300)}")
                val base = PedidoDeVideoDoVivixW1.mensagemDoErro(resp.code, codigo, json?.optString("message").orEmpty())
                throw ErroDaVivix(if (naoRepetir && resp.code >= 500) "$base Confira os jobs antes de reenviar." else base)
            }
            return json?.optJSONObject("data") ?: throw ErroDaVivix("A Vivix respondeu fora do formato esperado.")
        }
    }

    private fun baixarBytes(url: String): ByteArray? =
        runCatching {
            download.newCall(Request.Builder().url(url).get().build()).execute().use { resp ->
                if (resp.isSuccessful) resp.body?.bytes() else null
            }
        }.getOrNull()

    private fun mimeDaImagem(bytes: ByteArray): String = when {
        bytes.size > 3 && bytes[0] == 0x89.toByte() && bytes[1] == 'P'.code.toByte() -> "image/png"
        bytes.size > 11 && String(bytes, 8, 4, Charsets.US_ASCII) == "WEBP" -> "image/webp"
        else -> "image/jpeg"
    }

    private fun registrar(msg: String) {
        Log.d(TAG, msg)
    }

    companion object {
        private const val TAG = "VivixApiCB"
        const val POLL_MS = 5_000L
        const val TIMEOUT_MS = 10 * 60 * 1000L
    }
}

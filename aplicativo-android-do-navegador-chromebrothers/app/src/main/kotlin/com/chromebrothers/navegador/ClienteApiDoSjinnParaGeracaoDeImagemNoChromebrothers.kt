package com.chromebrothers.navegador

import android.util.Log
import com.chromebrothers.navegador.regras.CatalogoDeModelosDeImagemDoSjinnNaColecao
import com.chromebrothers.navegador.regras.CatalogoDeModelosDeVideoDoSjinnNaColecao
import com.chromebrothers.navegador.regras.ExtracaoDaRespostaDeUploadDeArquivoDoSjinn
import com.chromebrothers.navegador.regras.ExtracaoDaUrlDoResultadoDaTarefaSjinn
import com.chromebrothers.navegador.regras.PedidoDeGeracaoDeImagemViaMcpNaColecao
import com.chromebrothers.navegador.regras.PedidoDeGeracaoDeVideoViaMcpNaColecao
import com.chromebrothers.navegador.regras.ResolucaoDoPedidoDeGeracaoParaOMcpDoSjinn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Geração de imagem via Tool API do Sjinn (`create_tool_task` / `query_tool_task_status`)
 * e upload de referência via `upload_file` (presign + PUT), espelhando o cliente web.
 */
class ClienteApiDoSjinnParaGeracaoDeImagemNoChromebrothers(
    private val sessao: SessaoDaContaSjinnNoAparelho,
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
    private val jsonTipo = "application/json".toMediaType()

    fun listarModelosJson(): String = CatalogoDeModelosDeImagemDoSjinnNaColecao.jsonDosItensParaPonte()

    fun listarModelosDeVideoJson(): String = CatalogoDeModelosDeVideoDoSjinnNaColecao.jsonDosItensParaPonte()

    fun gerarVideo(
        prompt: String,
        modelo: String,
        aspectRatio: String = "16:9",
        resolution: String = "480p",
        duracaoSegundos: Int = 5,
        referenciasLocais: List<Pair<ByteArray, String>> = emptyList(),
        videosLocais: List<java.io.File> = emptyList(),
        aoCriarTarefa: (taskId: String, urlsDeEntrada: List<String>) -> Unit = { _, _ -> },
    ): ResultadoDaGeracaoMcpHiggsfield {
        if (!sessao.temSessao()) {
            return ResultadoDaGeracaoMcpHiggsfield.Erro(SessaoDaContaSjinnNoAparelho.SEM_SESSAO)
        }
        return try {
            val urlsDeImagem = mutableListOf<String>()
            for ((bytes, nome) in referenciasLocais.take(
                PedidoDeGeracaoDeVideoViaMcpNaColecao.LIMITE_DE_REFERENCIAS,
            )) {
                if (bytes.isEmpty()) continue
                val url = enviarReferenciaLocal(bytes, nome)
                    ?: return ResultadoDaGeracaoMcpHiggsfield.Erro(
                        "Não consegui enviar a imagem de referência ao Sjinn.",
                    )
                urlsDeImagem += url
            }
            val urlsDeVideo = videosLocais.map { video ->
                enviarVideoLocal(video)
                    ?: return ResultadoDaGeracaoMcpHiggsfield.Erro("Não consegui enviar o vídeo de referência ao Sjinn.")
            }
            // Todas as entradas: o resultado nunca pode ser confundido com uma delas.
            val urls = urlsDeImagem + urlsDeVideo
            if (prompt.isBlank() && urls.isEmpty()) {
                return ResultadoDaGeracaoMcpHiggsfield.Erro("Faltou o prompt (ou uma referência).")
            }
            val modeloEfetivo = modelo.ifBlank {
                PedidoDeGeracaoDeVideoViaMcpNaColecao.MODELO_PADRAO_DA_COLECAO_SJINN
            }
            val toolType = CatalogoDeModelosDeVideoDoSjinnNaColecao.toolTypeDe(modeloEfetivo)
            val input = JSONObject(
                com.chromebrothers.navegador.regras.MontagemDoPedidoDeVideoDoSjinn.montar(
                    toolType = toolType,
                    modelo = modeloEfetivo,
                    prompt = prompt,
                    proporcao = aspectRatio,
                    resolucaoDaUi = resolution,
                    duracao = duracaoSegundos,
                    imageUrls = urlsDeImagem,
                    videoUrls = urlsDeVideo,
                ),
            )
            val corpo = JSONObject()
                .put("tool_type", toolType)
                .put("input", input)
                .toString()
            registrar("create_tool_task video $corpo")
            val criado = postJson("$BASE/create_tool_task", corpo)
            registrar("create_tool_task video result=$criado")
            if (!criado.optBoolean("success", false)) {
                return ResultadoDaGeracaoMcpHiggsfield.Erro(
                    criado.optString("errorMsg").ifBlank { "Sjinn recusou a criação do vídeo." },
                )
            }
            val taskId = criado.optJSONObject("data")?.optString("task_id").orEmpty()
            if (taskId.isBlank()) {
                return ResultadoDaGeracaoMcpHiggsfield.Erro("Sjinn não devolveu task_id do vídeo.")
            }
            aoCriarTarefa(taskId, urls.toList())
            val url = esperarUrlDaTarefa(taskId, urlsDeEntrada = urls.toSet())
                ?: return ResultadoDaGeracaoMcpHiggsfield.Erro(
                    "A geração de vídeo Sjinn demorou demais ou falhou (task $taskId).",
                )
            val bytes = baixarBytes(url)
                ?: return ResultadoDaGeracaoMcpHiggsfield.Erro("Não baixei o vídeo gerado no Sjinn.")
            ResultadoDaGeracaoMcpHiggsfield.Pronto(
                bytes = bytes,
                enderecoRemoto = url,
                jobId = taskId,
                imagem = false,
            )
        } catch (e: Exception) {
            registrar("gerarVideo falhou: ${e.message}")
            ResultadoDaGeracaoMcpHiggsfield.Erro(e.message ?: "Falha na geração de vídeo Sjinn.")
        }
    }

    fun gerarImagem(
        prompt: String,
        modelo: String,
        aspectRatio: String = "1:1",
        resolution: String = "1k",
        urlsDeReferencia: List<String> = emptyList(),
        referenciasLocais: List<Pair<ByteArray, String>> = emptyList(),
    ): ResultadoDaGeracaoMcpHiggsfield {
        if (!sessao.temSessao()) {
            return ResultadoDaGeracaoMcpHiggsfield.Erro(SessaoDaContaSjinnNoAparelho.SEM_SESSAO)
        }
        return try {
            val urls = urlsDeReferencia.toMutableList()
            for ((bytes, nome) in referenciasLocais.take(
                PedidoDeGeracaoDeImagemViaMcpNaColecao.LIMITE_DE_REFERENCIAS,
            )) {
                if (bytes.isEmpty()) continue
                val url = enviarReferenciaLocal(bytes, nome)
                    ?: return ResultadoDaGeracaoMcpHiggsfield.Erro(
                        "Não consegui enviar a imagem de referência ao Sjinn.",
                    )
                urls += url
            }
            val modeloEfetivo = modelo.ifBlank {
                PedidoDeGeracaoDeImagemViaMcpNaColecao.MODELO_PADRAO_DA_COLECAO_SJINN
            }
            val toolType = CatalogoDeModelosDeImagemDoSjinnNaColecao.toolTypeDe(modeloEfetivo)
            val resolucao = ResolucaoDoPedidoDeGeracaoParaOMcpDoSjinn.paraApi(resolution)
            val proporcao = aspectRatio.trim().ifBlank { "1:1" }
            val input = JSONObject()
                .put("prompt", prompt)
                .put("aspect_ratio", proporcao)
                .put("resolution", resolucao)
            if (urls.isNotEmpty()) {
                val arr = JSONArray()
                urls.take(PedidoDeGeracaoDeImagemViaMcpNaColecao.LIMITE_DE_REFERENCIAS)
                    .forEach { arr.put(it) }
                // GPT Image 2.5 docs: image_urls; demais (incl. gpt-image-2): image_list.
                if (toolType.contains("2.5")) {
                    input.put("image_urls", arr)
                } else {
                    input.put("image_list", arr)
                }
            }
            val corpo = JSONObject()
                .put("tool_type", toolType)
                .put("input", input)
                .toString()
            registrar("create_tool_task $corpo")
            val criado = postJson("$BASE/create_tool_task", corpo)
            registrar("create_tool_task result=$criado")
            if (!criado.optBoolean("success", false)) {
                return ResultadoDaGeracaoMcpHiggsfield.Erro(
                    criado.optString("errorMsg").ifBlank { "Sjinn recusou a criação da tarefa." },
                )
            }
            val taskId = criado.optJSONObject("data")?.optString("task_id").orEmpty()
            if (taskId.isBlank()) {
                return ResultadoDaGeracaoMcpHiggsfield.Erro("Sjinn não devolveu task_id.")
            }
            val url = esperarUrlDaTarefa(taskId, urlsDeEntrada = urls.toSet())
                ?: return ResultadoDaGeracaoMcpHiggsfield.Erro(
                    "A geração Sjinn demorou demais ou falhou (task $taskId).",
                )
            val bytes = baixarBytes(url)
                ?: return ResultadoDaGeracaoMcpHiggsfield.Erro("Não baixei o arquivo gerado no Sjinn.")
            ResultadoDaGeracaoMcpHiggsfield.Pronto(
                bytes = bytes,
                enderecoRemoto = url,
                jobId = taskId,
                imagem = true,
            )
        } catch (e: Exception) {
            registrar("gerar falhou: ${e.message}")
            ResultadoDaGeracaoMcpHiggsfield.Erro(e.message ?: "Falha na geração Sjinn.")
        }
    }

    /**
     * Presign (`upload_file`) + PUT no signed_url → devolve `cdn_url` estável.
     * Fluxo espelhado do frontend Sjinn (bucket padrão `comfy-online`).
     */
    fun enviarReferenciaLocal(bytes: ByteArray, nomeArquivo: String): String? {
        val nome = nomeArquivo.ifBlank { "referencia.jpg" }
        val contentType = when {
            nome.endsWith(".png", ignoreCase = true) -> "image/png"
            nome.endsWith(".webp", ignoreCase = true) -> "image/webp"
            nome.endsWith(".gif", ignoreCase = true) -> "image/gif"
            else -> "image/jpeg"
        }
        val pedido = JSONObject()
            .put("bucket_name", BUCKET)
            .put("content_type", contentType)
            .put("size_bytes", bytes.size)
            .toString()
        registrar("upload_file pedido=$pedido")
        val bruto = postJson("$BASE/upload_file", pedido).toString()
        registrar("upload_file result=${bruto.take(600)}")
        if (!ExtracaoDaRespostaDeUploadDeArquivoDoSjinn.sucesso(bruto)) {
            error(
                JSONObject(bruto).optString("errorMsg")
                    .ifBlank { "Sjinn recusou o upload da referência." },
            )
        }
        val signed = ExtracaoDaRespostaDeUploadDeArquivoDoSjinn.signedUrl(bruto)
            ?: error("Sjinn não devolveu signed_url no upload.")
        val cdn = ExtracaoDaRespostaDeUploadDeArquivoDoSjinn.cdnUrl(bruto)
            ?: error("Sjinn não devolveu cdn_url no upload.")
        putBytes(signed, bytes, contentType)
        return cdn
    }

    /** Mesmo presign + PUT da imagem, com o vídeo lido direto do arquivo (sem carregar na memória). */
    fun enviarVideoLocal(arquivo: java.io.File): String? {
        val contentType = when {
            arquivo.name.endsWith(".mov", ignoreCase = true) -> "video/quicktime"
            arquivo.name.endsWith(".webm", ignoreCase = true) -> "video/webm"
            else -> "video/mp4"
        }
        val pedido = JSONObject()
            .put("bucket_name", BUCKET)
            .put("content_type", contentType)
            .put("size_bytes", arquivo.length())
            .toString()
        registrar("upload_file (video) pedido=$pedido")
        val bruto = postJson("$BASE/upload_file", pedido).toString()
        registrar("upload_file (video) result=${bruto.take(600)}")
        if (!ExtracaoDaRespostaDeUploadDeArquivoDoSjinn.sucesso(bruto)) {
            error(JSONObject(bruto).optString("errorMsg").ifBlank { "Sjinn recusou o upload do vídeo." })
        }
        val signed = ExtracaoDaRespostaDeUploadDeArquivoDoSjinn.signedUrl(bruto)
            ?: error("Sjinn não devolveu signed_url no upload.")
        val cdn = ExtracaoDaRespostaDeUploadDeArquivoDoSjinn.cdnUrl(bruto)
            ?: error("Sjinn não devolveu cdn_url no upload.")
        put(signed, arquivo.asRequestBody(contentType.toMediaType()), contentType)
        return cdn
    }

    private fun putBytes(signedUrl: String, bytes: ByteArray, contentType: String) =
        put(signedUrl, bytes.toRequestBody(contentType.toMediaType()), contentType)

    private fun put(signedUrl: String, corpo: okhttp3.RequestBody, contentType: String) {
        val req = Request.Builder()
            .url(signedUrl)
            .put(corpo)
            .header("Content-Type", contentType)
            .build()
        http.newCall(req).execute().use { resp ->
            val texto = resp.body?.string().orEmpty()
            registrar("PUT upload HTTP ${resp.code} ${texto.take(200)}")
            if (!resp.isSuccessful) {
                error("Falha ao enviar bytes ao storage Sjinn (HTTP ${resp.code}).")
            }
        }
    }

    /** Um `query_tool_task_status` só; [urlsDeEntrada] evita confundir a referência com o resultado. */
    fun consultarVideoDaTarefaUmaVez(
        taskId: String,
        urlsDeEntrada: Set<String>,
    ): ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa {
        if (!sessao.temSessao()) {
            return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(SessaoDaContaSjinnNoAparelho.SEM_SESSAO)
        }
        return try {
            val bruto = postJson("$BASE/query_tool_task_status", JSONObject().put("task_id", taskId).toString()).toString()
            registrar("consulta unica $taskId ${bruto.take(500)}")
            if (ExtracaoDaUrlDoResultadoDaTarefaSjinn.tarefaFalhou(bruto)) {
                return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou("A tarefa $taskId falhou no Sjinn.")
            }
            val url = ExtracaoDaUrlDoResultadoDaTarefaSjinn.urlDoResultado(bruto, urlsDeEntrada)
                ?: return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.AindaGerando
            val bytes = baixarBytes(url)
                ?: return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou("Tarefa pronta, mas não baixei o vídeo.")
            ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Pronto(bytes, url)
        } catch (e: Exception) {
            ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(e.message ?: "Falha ao consultar o Sjinn.")
        }
    }

    private fun esperarUrlDaTarefa(taskId: String, urlsDeEntrada: Set<String>): String? {
        val inicio = System.currentTimeMillis()
        while (System.currentTimeMillis() - inicio < TIMEOUT_MS) {
            val corpo = JSONObject().put("task_id", taskId).toString()
            val bruto = try {
                postJson("$BASE/query_tool_task_status", corpo).toString()
            } catch (e: Exception) {
                registrar("poll erro: ${e.message}")
                Thread.sleep(POLL_MS)
                continue
            }
            registrar("poll $taskId status=${ExtracaoDaUrlDoResultadoDaTarefaSjinn.statusNumerico(bruto)} ${bruto.take(500)}")
            if (ExtracaoDaUrlDoResultadoDaTarefaSjinn.tarefaFalhou(bruto)) return null
            ExtracaoDaUrlDoResultadoDaTarefaSjinn.urlDoResultado(bruto, urlsDeEntrada)?.let { return it }
            Thread.sleep(POLL_MS)
        }
        return null
    }

    private fun postJson(url: String, corpo: String): JSONObject {
        val token = sessao.apiToken()
        if (token.isBlank()) error(SessaoDaContaSjinnNoAparelho.SEM_SESSAO)
        val req = Request.Builder()
            .url(url)
            .post(corpo.toRequestBody(jsonTipo))
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()
        http.newCall(req).execute().use { resp ->
            val texto = resp.body?.string().orEmpty()
            if (resp.code == 401) error("API Token Sjinn inválido ou expirado.")
            if (!resp.isSuccessful && texto.isBlank()) error("Sjinn HTTP ${resp.code}")
            val json = runCatching { JSONObject(texto) }.getOrElse {
                error("Sjinn respondeu fora de JSON: ${texto.take(200)}")
            }
            return json
        }
    }

    private fun baixarBytes(url: String): ByteArray? {
        val req = Request.Builder().url(url).get().build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            return resp.body?.bytes()
        }
    }

    private fun registrar(msg: String) {
        Log.d(TAG, msg)
    }

    companion object {
        private const val TAG = "SjinnApiCB"
        const val BASE = "https://sjinn.ai/api/un-api"
        const val BUCKET = "comfy-online"
        const val POLL_MS = 8_000L
        const val TIMEOUT_MS = 10 * 60 * 1000L
    }
}

package com.chromebrothers.navegador

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.chromebrothers.navegador.regras.CatalogoDeModelosDeVideoDoTopviewNaColecao
import com.chromebrothers.navegador.regras.ExtracaoDaUrlDoResultadoDaTarefaTopview
import com.chromebrothers.navegador.regras.ExtracaoDoFileIdDoUploadTopview
import com.chromebrothers.navegador.regras.GeracaoDeVideoPeloTopviewNoChat
import com.chromebrothers.navegador.regras.MontagemDoPedidoDeVideoTopviewComReferenciasOmni
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * OAuth (PKCE + loopback 127.0.0.1:8788) e MCP streamable HTTP em https://mcp.topview.ai/mcp.
 */
class ClienteMcpDoTopviewParaGeracaoNoChromebrothers(
    private val context: Context,
    private val sessao: SessaoDaContaTopviewNoAparelho,
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    private val principal = Handler(Looper.getMainLooper())
    private val jsonTipo = "application/json".toMediaType()
    @Volatile private var mcpSessionId: String = ""
    @Volatile private var rpcSeq: Int = 1
    private val travaMcp = Any()

    fun ligarConta(activity: android.app.Activity, aoFim: (String?) -> Unit) {
        Thread {
            try {
                val redirect = REDIRECT
                val clientId = registrarCliente(redirect)
                val verifier = codeVerifier()
                val challenge = codeChallenge(verifier)
                val state = aleatorioUrlSafe(16)
                val authorize = Uri.parse(AUTHORIZE).buildUpon()
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("client_id", clientId)
                    .appendQueryParameter("redirect_uri", redirect)
                    .appendQueryParameter("scope", "openid mcp:tools")
                    .appendQueryParameter("code_challenge", challenge)
                    .appendQueryParameter("code_challenge_method", "S256")
                    .appendQueryParameter("state", state)
                    .build()
                principal.post {
                    abrirWebViewDeLogin(activity, authorize.toString(), redirect, state) { code, erro ->
                        if (code.isNullOrBlank()) {
                            aoFim(erro ?: "Login cancelado.")
                            return@abrirWebViewDeLogin
                        }
                        Thread {
                            try {
                                trocarCodigoPorToken(clientId, code, redirect, verifier)
                                principal.post { aoFim(null) }
                            } catch (e: Exception) {
                                principal.post { aoFim(e.message ?: "Falha no token.") }
                            }
                        }.start()
                    }
                }
            } catch (e: Exception) {
                principal.post { aoFim(e.message ?: "Falha ao registrar o cliente Topview.") }
            }
        }.start()
    }

    fun gerarVideo(
        prompt: String,
        modelo: String,
        aspectRatio: String = "16:9",
        resolution: String = "480p",
        duracaoSegundos: Int = 5,
        referenciasLocais: List<Pair<ByteArray, String>> = emptyList(),
        videosLocais: List<File> = emptyList(),
        aoCriarTarefa: (taskId: String, taskType: String) -> Unit = { _, _ -> },
    ): ResultadoDaGeracaoMcpHiggsfield {
        if (!sessao.temSessao()) {
            return ResultadoDaGeracaoMcpHiggsfield.Erro(GeracaoDeVideoPeloTopviewNoChat.SEM_SESSAO)
        }
        return try {
            val submetida = synchronized(travaMcp) {
                garantirSessaoMcp()
                val display = CatalogoDeModelosDeVideoDoTopviewNaColecao.displayNameDe(modelo)
                val resolucao = CatalogoDeModelosDeVideoDoTopviewNaColecao.resolucaoInteira(resolution)
                val fileIds = referenciasLocais
                    .filter { it.first.isNotEmpty() }
                    .take(10)
                    .map { (bytes, nome) -> enviarArquivoLocal(bytes, nome) }
                val fileIdsDosVideos = videosLocais.map { enviarVideoLocal(it) }
                val montado = MontagemDoPedidoDeVideoTopviewComReferenciasOmni.montar(
                    modeloDisplay = display,
                    prompt = prompt,
                    aspectRatio = aspectRatio,
                    resolution = resolucao,
                    duration = duracaoSegundos,
                    fileIdsDasReferencias = fileIds,
                    fileIdsDosVideos = fileIdsDosVideos,
                )
                val req = JSONObject()
                    .put("taskType", montado.taskType)
                    .put("model", montado.model)
                    .put("duration", montado.duration)
                    .put("resolution", montado.resolution)
                montado.prompt?.takeIf { it.isNotBlank() }?.let { req.put("prompt", it) }
                montado.aspectRatio?.let { req.put("aspectRatio", it) }
                if (montado.inputImages.isNotEmpty()) {
                    val imagens = JSONArray()
                    montado.inputImages.forEach { img ->
                        imagens.put(
                            JSONObject()
                                .put("fileId", img.fileId)
                                .put("name", img.name),
                        )
                    }
                    req.put("inputImages", imagens)
                }
                if (montado.inputVideos.isNotEmpty()) {
                    val videos = JSONArray()
                    montado.inputVideos.forEach { v ->
                        videos.put(JSONObject().put("fileId", v.fileId).put("name", v.name))
                    }
                    req.put("inputVideos", videos)
                }
                montado.firstFrameFileId?.let { req.put("firstFrameFileId", it) }
                if (montado.referenceImageFileIds.isNotEmpty()) {
                    req.put("referenceImageFileIds", JSONArray(montado.referenceImageFileIds))
                }
                val args = JSONObject().put("req", req)
                registrarDiag("tools/call $TOOL_GENERATE_VIDEO args=$args")
                val submitBruto = toolsCall(TOOL_GENERATE_VIDEO, args)
                registrarDiag("submit=$submitBruto")
                val util = textoUtilDoToolsCall(submitBruto)
                if (ehErroDeFerramenta(submitBruto)) {
                    return ResultadoDaGeracaoMcpHiggsfield.Erro(util.ifBlank { submitBruto })
                }
                ExtracaoDaUrlDoResultadoDaTarefaTopview.urlDoVideo(util)?.let { url ->
                    val bytes = baixarBytes(url)
                        ?: return ResultadoDaGeracaoMcpHiggsfield.Erro("Não baixei o vídeo gerado.")
                    return ResultadoDaGeracaoMcpHiggsfield.Pronto(
                        bytes = bytes,
                        enderecoRemoto = url,
                        jobId = ExtracaoDaUrlDoResultadoDaTarefaTopview.taskId(util).orEmpty(),
                        imagem = false,
                    )
                }
                val taskId = ExtracaoDaUrlDoResultadoDaTarefaTopview.taskId(util)
                    ?: ExtracaoDaUrlDoResultadoDaTarefaTopview.taskId(submitBruto)
                    ?: return ResultadoDaGeracaoMcpHiggsfield.Erro(
                        "O Topview não devolveu taskId.\n${util.take(800)}",
                    )
                montado.taskType to taskId
            }
            val (taskType, taskId) = submetida
            aoCriarTarefa(taskId, taskType)
            val url = when (val espera = esperarUrlDaTarefa(taskType, taskId)) {
                is EsperaDaTarefaTopview.Url -> espera.url
                is EsperaDaTarefaTopview.Falha ->
                    return ResultadoDaGeracaoMcpHiggsfield.Erro(
                        espera.mensagem ?: "A geração falhou (task $taskId).",
                    )
                EsperaDaTarefaTopview.Timeout ->
                    return ResultadoDaGeracaoMcpHiggsfield.Erro(
                        "A geração demorou demais (task $taskId). A tarefa pode terminar depois no Topview.",
                    )
            }
            val bytes = baixarBytes(url)
                ?: return ResultadoDaGeracaoMcpHiggsfield.Erro("Não baixei o vídeo gerado.")
            ResultadoDaGeracaoMcpHiggsfield.Pronto(
                bytes = bytes,
                enderecoRemoto = url,
                jobId = taskId,
                imagem = false,
            )
        } catch (e: Exception) {
            registrarDiag("gerarVideo falhou: ${e.message}")
            ResultadoDaGeracaoMcpHiggsfield.Erro(e.message ?: "Falha na geração Topview.")
        }
    }

    /**
     * Configuração de geração de vídeo (lista de modelos com `submitModel`/`displayName`).
     * Só leitura, não gasta crédito. Devolve o texto útil do tools/call; lança em erro.
     */
    fun buscarConfiguracaoDeVideoBruta(): String {
        check(sessao.temSessao()) { GeracaoDeVideoPeloTopviewNoChat.SEM_SESSAO }
        return synchronized(travaMcp) {
            garantirSessaoMcp()
            val args = JSONObject().put("req", JSONObject().put("type", "video"))
            val bruto = toolsCall(TOOL_GET_GENERATION_CONFIG, args)
            registrarDiag("generation_config(video)=${bruto.take(400000)}")
            check(!ehErroDeFerramenta(bruto)) { textoUtilDoToolsCall(bruto).ifBlank { bruto }.take(300) }
            bruto
        }
    }

    /** Upload oficial: ta_upload_credential → PUT → ta_upload_check_file. */
    private fun enviarArquivoLocal(bytes: ByteArray, nome: String): String {
        val nomeOk = nome.ifBlank { "referencia.jpg" }
        val formato = when {
            nomeOk.endsWith(".png", true) -> "png"
            nomeOk.endsWith(".webp", true) -> "webp"
            nomeOk.endsWith(".jpeg", true) -> "jpeg"
            else -> "jpg"
        }
        val mime = when (formato) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        return enviarCorpo(bytes.toRequestBody(mime.toMediaType()), formato, mime)
    }

    /** Vídeo de referência (omni_reference → inputVideos), lido direto do arquivo. */
    private fun enviarVideoLocal(arquivo: File): String {
        val formato = when {
            arquivo.name.endsWith(".mov", true) -> "mov"
            arquivo.name.endsWith(".webm", true) -> "webm"
            else -> "mp4"
        }
        val mime = when (formato) {
            "mov" -> "video/quicktime"
            "webm" -> "video/webm"
            else -> "video/mp4"
        }
        return enviarCorpo(arquivo.asRequestBody(mime.toMediaType()), formato, mime)
    }

    private fun enviarCorpo(corpo: okhttp3.RequestBody, formato: String, mime: String): String {
        val credBruto = toolsCall(TOOL_UPLOAD_CREDENTIAL, JSONObject().put("format", formato))
        registrarDiag("upload_credential=${credBruto.take(800)}")
        val utilCred = textoUtilDoToolsCall(credBruto)
        if (ehErroDeFerramenta(credBruto)) {
            error(utilCred.ifBlank { "Upload credential Topview falhou." })
        }
        val fileId = ExtracaoDoFileIdDoUploadTopview.fileId(utilCred)
            ?: ExtracaoDoFileIdDoUploadTopview.fileId(credBruto)
            ?: error("Upload Topview sem fileId.\n${utilCred.take(400)}")
        val uploadUrl = extrairCampo(utilCred, "uploadUrl")
            ?: extrairCampo(credBruto, "uploadUrl")
            ?: extrairCampo(utilCred, "upload_url")
            ?: error("Upload Topview sem uploadUrl.\n${utilCred.take(400)}")
        val putOk = http.newCall(
            Request.Builder()
                .url(uploadUrl)
                .header("Content-Type", mime)
                .put(corpo)
                .build(),
        ).execute().use { resp ->
            registrarDiag("PUT upload HTTP ${resp.code}")
            resp.isSuccessful
        }
        if (!putOk) error("Falha no PUT da referência Topview.")
        val checkBruto = toolsCall(TOOL_UPLOAD_CHECK, JSONObject().put("fileId", fileId))
        registrarDiag("upload_check=${checkBruto.take(400)}")
        if (ehErroDeFerramenta(checkBruto)) {
            error(textoUtilDoToolsCall(checkBruto).ifBlank { "Check do upload Topview falhou." })
        }
        return fileId
    }

    /** Uma consulta de `topview_query_task`; baixa o vídeo se já estiver pronto. */
    fun consultarVideoDaTarefaUmaVez(
        taskId: String,
        taskType: String,
    ): ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa {
        if (!sessao.temSessao()) {
            return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(GeracaoDeVideoPeloTopviewNoChat.SEM_SESSAO)
        }
        return try {
            when (val estado = consultarEstadoDaTarefa(taskType.ifBlank { TASK_OMNI_REFERENCE }, taskId)) {
                is EsperaDaTarefaTopview.Url -> {
                    val bytes = baixarBytes(estado.url)
                        ?: return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(
                            "Tarefa $taskId pronta, mas não baixei o vídeo.",
                        )
                    ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Pronto(bytes, estado.url)
                }
                is EsperaDaTarefaTopview.Falha -> ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(
                    estado.mensagem ?: "Tarefa $taskId falhou no Topview.",
                )
                EsperaDaTarefaTopview.Timeout -> ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.AindaGerando
            }
        } catch (e: Exception) {
            ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(e.message ?: "Falha ao consultar o Topview.")
        }
    }

    /** Timeout aqui quer dizer «ainda sem resultado nesta consulta». */
    private fun consultarEstadoDaTarefa(taskType: String, taskId: String): EsperaDaTarefaTopview {
        val req = JSONObject()
            .put("taskType", taskType)
            .put("taskId", taskId)
            .put("needCloudFrontUrl", true)
            .put("shortenUrls", true)
        val bruto = runCatching {
            synchronized(travaMcp) {
                if (mcpSessionId.isBlank()) garantirSessaoMcp()
                toolsCall(TOOL_QUERY_TASK, JSONObject().put("req", req))
            }
        }.getOrNull().orEmpty()
        val util = textoUtilDoToolsCall(bruto).ifBlank { bruto }
        registrarDiag("query $taskId=$util")
        if (util.isBlank() || ehErroDeFerramenta(bruto)) return EsperaDaTarefaTopview.Timeout
        if (ExtracaoDaUrlDoResultadoDaTarefaTopview.tarefaFalhou(util)) {
            return EsperaDaTarefaTopview.Falha(ExtracaoDaUrlDoResultadoDaTarefaTopview.mensagemDeErro(util))
        }
        return ExtracaoDaUrlDoResultadoDaTarefaTopview.urlDoVideo(util)
            ?.let { EsperaDaTarefaTopview.Url(it) }
            ?: EsperaDaTarefaTopview.Timeout
    }

    private fun esperarUrlDaTarefa(taskType: String, taskId: String): EsperaDaTarefaTopview {
        val limite = System.currentTimeMillis() + TIMEOUT_MS
        while (System.currentTimeMillis() < limite) {
            val estado = consultarEstadoDaTarefa(taskType, taskId)
            if (estado != EsperaDaTarefaTopview.Timeout) return estado
            Thread.sleep(POLL_MS)
        }
        return EsperaDaTarefaTopview.Timeout
    }

    private sealed class EsperaDaTarefaTopview {
        data class Url(val url: String) : EsperaDaTarefaTopview()
        data class Falha(val mensagem: String?) : EsperaDaTarefaTopview()
        data object Timeout : EsperaDaTarefaTopview()
    }

    private fun extrairCampo(bruto: String, chave: String): String? {
        Regex(""""$chave"\s*:\s*"([^"]+)"""").find(bruto)?.groupValues?.getOrNull(1)
            ?.takeIf { it.isNotBlank() }?.let { return it }
        return null
    }

    private fun baixarBytes(url: String): ByteArray? {
        val req = Request.Builder().url(url).get().build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            return resp.body?.bytes()
        }
    }

    private fun garantirSessaoMcp() {
        refreshSePreciso()
        mcpSessionId = ""
        rpcSeq = 1
        mcpJsonRpc(
            method = "initialize",
            params = JSONObject()
                .put("protocolVersion", "2025-03-26")
                .put("capabilities", JSONObject())
                .put(
                    "clientInfo",
                    JSONObject().put("name", "chromebrothers").put("version", "1.0"),
                ),
        )
        mcpNotify("notifications/initialized", JSONObject())
        runCatching {
            val tools = mcpJsonRpc(method = "tools/list", params = JSONObject())
            val lista = tools?.optJSONArray("tools")
            if (lista != null) {
                for (i in 0 until lista.length()) {
                    val t = lista.optJSONObject(i) ?: continue
                    val nome = t.optString("name")
                    if (nome.startsWith("topview_") || nome.startsWith("ta_upload")) {
                        registrarDiag("schema $nome=${t.optJSONObject("inputSchema")}")
                    }
                }
            }
        }
    }

    private fun toolsCall(nome: String, arguments: JSONObject): String {
        val result = mcpJsonRpc(
            method = "tools/call",
            params = JSONObject()
                .put("name", nome)
                .put("arguments", arguments),
        )
        return result?.toString().orEmpty()
    }

    private fun textoUtilDoToolsCall(bruto: String): String {
        val trim = bruto.trim()
        if (trim.isEmpty()) return ""
        val json = runCatching { JSONObject(trim) }.getOrNull() ?: return trim
        val partes = mutableListOf<String>()
        val content = json.optJSONArray("content")
        if (content != null) {
            for (i in 0 until content.length()) {
                val item = content.optJSONObject(i) ?: continue
                val text = item.optString("text").trim()
                if (text.isNotEmpty()) partes += text
            }
        }
        json.optJSONObject("structuredContent")?.let { partes += it.toString(2) }
        if (partes.isEmpty()) {
            json.opt("result")?.let { partes += it.toString() }
        }
        return if (partes.isEmpty()) trim else partes.joinToString("\n")
    }

    private fun ehErroDeFerramenta(bruto: String): Boolean {
        val json = runCatching { JSONObject(bruto.trim()) }.getOrNull() ?: return false
        if (json.optBoolean("isError")) return true
        val text = textoUtilDoToolsCall(bruto).lowercase()
        return text.contains("invalid input") || text.contains("\"iserror\":true")
    }

    private fun registrarCliente(redirect: String): String {
        val corpo = JSONObject()
            .put("client_name", "ChromeBrothers")
            .put("redirect_uris", JSONArray().put(redirect))
            .put("grant_types", JSONArray().put("authorization_code").put("refresh_token"))
            .put("response_types", JSONArray().put("code"))
            .put("token_endpoint_auth_method", "none")
            .toString()
        val req = Request.Builder()
            .url(REGISTER)
            .post(corpo.toRequestBody(jsonTipo))
            .header("Content-Type", "application/json")
            .build()
        http.newCall(req).execute().use { resp ->
            val texto = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) error("Registro OAuth Topview: HTTP ${resp.code} $texto")
            val id = JSONObject(texto).optString("client_id")
            if (id.isBlank()) error("Registro OAuth Topview sem client_id.")
            return id
        }
    }

    private fun trocarCodigoPorToken(clientId: String, code: String, redirect: String, verifier: String) {
        val form = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", redirect)
            .add("client_id", clientId)
            .add("code_verifier", verifier)
            .build()
        val req = Request.Builder().url(TOKEN).post(form).build()
        http.newCall(req).execute().use { resp ->
            val texto = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) error("Token Topview: HTTP ${resp.code} $texto")
            val json = JSONObject(texto)
            val access = json.optString("access_token")
            val refresh = json.optString("refresh_token")
            if (access.isBlank()) error("Token Topview sem access_token.")
            sessao.gravarTokens(access, refresh, clientId)
        }
    }

    private fun refreshSePreciso() {
        val refresh = sessao.refreshToken()
        if (refresh.isBlank()) return
        val form = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refresh)
            .add("client_id", sessao.clientId())
            .build()
        val req = Request.Builder().url(TOKEN).post(form).build()
        runCatching {
            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return
                val json = JSONObject(resp.body?.string().orEmpty())
                val access = json.optString("access_token")
                if (access.isNotBlank()) {
                    val novoRefresh = json.optString("refresh_token").ifBlank { refresh }
                    sessao.gravarTokens(access, novoRefresh, sessao.clientId())
                }
            }
        }
    }

    private fun mcpJsonRpc(method: String, params: JSONObject?): JSONObject? {
        val id = rpcSeq++
        val payload = JSONObject()
            .put("jsonrpc", "2.0")
            .put("id", id)
            .put("method", method)
        if (params != null) payload.put("params", params)
        val corpo = mcpPost(payload.toString())
        val json = parseMcpPayload(corpo) ?: return null
        if (json.has("error")) {
            error(json.optJSONObject("error")?.optString("message") ?: json.toString())
        }
        return json.optJSONObject("result") ?: json
    }

    private fun mcpNotify(method: String, params: JSONObject) {
        val payload = JSONObject()
            .put("jsonrpc", "2.0")
            .put("method", method)
            .put("params", params)
        runCatching { mcpPost(payload.toString()) }
    }

    private fun mcpPost(jsonBody: String): String {
        val token = sessao.accessToken()
        if (token.isBlank()) error("Sem token Topview.")
        val builder = Request.Builder()
            .url(MCP)
            .post(jsonBody.toRequestBody(jsonTipo))
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json, text/event-stream")
        if (mcpSessionId.isNotBlank()) {
            builder.header("Mcp-Session-Id", mcpSessionId)
        }
        http.newCall(builder.build()).execute().use { resp ->
            val sessionHeader = resp.header("Mcp-Session-Id")
                ?: resp.header("mcp-session-id")
                ?: resp.headers.names().firstOrNull { it.equals("mcp-session-id", ignoreCase = true) }
                    ?.let { resp.header(it) }
            sessionHeader?.trim()?.takeIf { it.isNotEmpty() }?.let { mcpSessionId = it }
            val texto = resp.body?.string().orEmpty()
            registrarDiag("MCP ${resp.code} session=$mcpSessionId body=${texto.take(800)}")
            if (resp.code == 401) {
                refreshSePreciso()
                error("Sessão Topview expirada. Ligue de novo na engrenagem.")
            }
            if (!resp.isSuccessful) error("MCP Topview HTTP ${resp.code}: $texto")
            return texto
        }
    }

    private fun parseMcpPayload(corpo: String): JSONObject? {
        val trim = corpo.trim()
        if (trim.startsWith("{")) return runCatching { JSONObject(trim) }.getOrNull()
        val dataLines = trim.lineSequence()
            .filter { it.startsWith("data:") }
            .map { it.removePrefix("data:").trim() }
            .filter { it.isNotEmpty() && it != "[DONE]" }
            .toList()
        for (linha in dataLines.asReversed()) {
            runCatching { JSONObject(linha) }.getOrNull()?.let { return it }
        }
        return null
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun abrirWebViewDeLogin(
        activity: android.app.Activity,
        url: String,
        redirectPrefix: String,
        stateEsperado: String,
        aoCodigo: (code: String?, erro: String?) -> Unit,
    ) {
        val feito = AtomicBoolean(false)
        val web = WebView(activity).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            setBackgroundColor(Color.BLACK)
        }
        val dialog = Dialog(activity, android.R.style.Theme_DeviceDefault_NoActionBar)
        dialog.setContentView(
            FrameLayout(activity).apply {
                addView(
                    web,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    ),
                )
            },
        )
        dialog.setOnCancelListener {
            if (feito.compareAndSet(false, true)) aoCodigo(null, "Login cancelado.")
        }
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val destino = request?.url?.toString().orEmpty()
                return interceptar(destino)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, urlAntiga: String?): Boolean {
                return interceptar(urlAntiga.orEmpty())
            }

            private fun interceptar(destino: String): Boolean {
                if (!destino.startsWith(redirectPrefix)) return false
                if (!feito.compareAndSet(false, true)) return true
                val uri = Uri.parse(destino)
                val state = uri.getQueryParameter("state").orEmpty()
                val code = uri.getQueryParameter("code").orEmpty()
                val erro = uri.getQueryParameter("error_description")
                    ?: uri.getQueryParameter("error")
                dialog.dismiss()
                when {
                    state != stateEsperado -> aoCodigo(null, "State OAuth inválido.")
                    code.isNotBlank() -> aoCodigo(code, null)
                    else -> aoCodigo(null, erro ?: "Sem código OAuth.")
                }
                return true
            }
        }
        dialog.show()
        web.loadUrl(url)
    }

    private fun codeVerifier(): String = aleatorioUrlSafe(64)

    private fun codeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private fun aleatorioUrlSafe(bytes: Int): String {
        val buf = ByteArray(bytes)
        SecureRandom().nextBytes(buf)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf)
    }

    private fun registrarDiag(msg: String) {
        Log.e(TAG, msg)
        runCatching {
            File(context.cacheDir, "topview-mcp-diag.txt").appendText(
                "${System.currentTimeMillis()} $msg\n",
            )
        }
    }

    companion object {
        private const val TAG = "TopviewCB"
        const val MCP = "https://mcp.topview.ai/mcp"
        const val REGISTER = "https://www.topview.ai/mcp_oauth/oauth/register"
        const val AUTHORIZE = "https://www.topview.ai/mcp_oauth/oauth/authorize"
        const val TOKEN = "https://www.topview.ai/mcp_oauth/oauth/token"
        const val REDIRECT = "http://127.0.0.1:8788/callback"
        const val TIMEOUT_MS = 10 * 60 * 1000L
        const val POLL_MS = 4_000L
        const val TOOL_GENERATE_VIDEO = "topview_generate_video"
        const val TOOL_GET_GENERATION_CONFIG = "topview_get_generation_config"
        const val TOOL_QUERY_TASK = "topview_query_task"
        const val TOOL_UPLOAD_CREDENTIAL = "ta_upload_credential"
        const val TOOL_UPLOAD_CHECK = "ta_upload_check_file"
        const val TASK_TEXT_TO_VIDEO = MontagemDoPedidoDeVideoTopviewComReferenciasOmni.TASK_TEXT_TO_VIDEO
        const val TASK_OMNI_REFERENCE = MontagemDoPedidoDeVideoTopviewComReferenciasOmni.TASK_OMNI_REFERENCE
        @Deprecated("Refs agora usam omni_reference; mantido só por compat de logs antigos.")
        const val TASK_IMAGE_TO_VIDEO = "image_to_video"
    }
}
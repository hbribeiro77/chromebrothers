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
import com.chromebrothers.navegador.regras.ExtracaoDasCriacoesDoMagnific
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
 * Magnific (ex-Freepik): OAuth (Keycloak, registro dinâmico + PKCE, retorno em 127.0.0.1 interceptado
 * na WebView) e MCP streamable HTTP em https://mcp.magnific.com.
 */
class ClienteMcpDoMagnificParaGeracaoNoChromebrothers(
    private val context: Context,
    private val sessao: SessaoDaContaMagnificNoAparelho,
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
    private val travaToken = Any()

    fun ligarConta(activity: android.app.Activity, aoFim: (String?) -> Unit) {
        Thread {
            try {
                val clientId = sessao.clientId().ifBlank { registrarCliente().also { sessao.gravarClientId(it) } }
                val verifier = aleatorioUrlSafe(64)
                val state = aleatorioUrlSafe(16)
                val authorize = Uri.parse(AUTHORIZE).buildUpon()
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("client_id", clientId)
                    .appendQueryParameter("redirect_uri", REDIRECT)
                    .appendQueryParameter("scope", SCOPE)
                    .appendQueryParameter("code_challenge", codeChallenge(verifier))
                    .appendQueryParameter("code_challenge_method", "S256")
                    .appendQueryParameter("state", state)
                    .appendQueryParameter("resource", MCP)
                    .build()
                principal.post {
                    abrirWebViewDeLogin(activity, authorize.toString(), state) { code, erro ->
                        if (code.isNullOrBlank()) {
                            aoFim(erro ?: "Login cancelado.")
                            return@abrirWebViewDeLogin
                        }
                        Thread {
                            try {
                                trocarCodigoPorToken(clientId, code, verifier)
                                principal.post { aoFim(null) }
                                // Confere a conexão e grava os schemas das ferramentas (diagnóstico).
                                runCatching { diagnosticar() }
                                    .onFailure { registrarDiag("diagnostico falhou: ${it.message}") }
                            } catch (e: Exception) {
                                principal.post { aoFim(e.message ?: "Falha no token Magnific.") }
                            }
                        }.start()
                    }
                }
            } catch (e: Exception) {
                principal.post { aoFim(e.message ?: "Falha ao registrar o app no Magnific.") }
            }
        }.start()
    }

    /**
     * Abre a sessão MCP, lista as ferramentas e grava no diagnóstico o schema das que a geração usa.
     * Só leitura: não gasta crédito. Devolve um resumo curto.
     */
    fun diagnosticar(): String = synchronized(travaMcp) {
        garantirSessaoMcp()
        val tools = mcpJsonRpc("tools/list", JSONObject())?.optJSONArray("tools") ?: JSONArray()
        val nomes = (0 until tools.length()).mapNotNull { tools.optJSONObject(it)?.optString("name") }
        registrarDiag("tools(${nomes.size})=${nomes.joinToString(",")}")
        for (i in 0 until tools.length()) {
            val t = tools.optJSONObject(i) ?: continue
            if (t.optString("name") in FERRAMENTAS_DE_INTERESSE) {
                registrarDiag("schema ${t.optString("name")}=${t.optJSONObject("inputSchema")}")
            }
        }
        "${nomes.size} ferramentas"
    }

    /** Lista de modelos (`images_models_list` / `video_models_list`). Só leitura. Devolve o result bruto. */
    fun listarModelosBruto(ferramenta: String): String {
        require(ferramenta == TOOL_IMAGES_MODELS_LIST || ferramenta == TOOL_VIDEO_MODELS_LIST)
        return synchronized(travaMcp) {
            garantirSessaoMcp()
            val bruto = toolsCall(ferramenta, JSONObject())
            registrarDiag("$ferramenta=${bruto.take(400_000)}")
            bruto
        }
    }

    /**
     * Uma chamada de ferramenta reaproveitando a sessão MCP (abre se não houver; reabre uma vez se falhar).
     * A trava cobre só a chamada, não a espera: vários vídeos podem esperar em paralelo.
     */
    private fun chamar(nome: String, arguments: JSONObject): String = synchronized(travaMcp) {
        renovarTokenSePreciso()
        if (mcpSessionId.isBlank()) garantirSessaoMcp()
        val bruto = try {
            toolsCall(nome, arguments)
        } catch (e: Exception) {
            if (e.message?.contains("expirou") == true) throw e
            garantirSessaoMcp()
            toolsCall(nome, arguments)
        }
        if (ehErroDeFerramenta(bruto)) error(textoDoErro(bruto))
        bruto
    }

    private fun ehErroDeFerramenta(bruto: String): Boolean =
        runCatching { JSONObject(bruto).optBoolean("isError") }.getOrDefault(false)

    private fun textoDoErro(bruto: String): String {
        val json = runCatching { JSONObject(bruto) }.getOrNull() ?: return bruto.take(300)
        val texto = json.optJSONArray("content")?.optJSONObject(0)?.optString("text").orEmpty()
        return texto.ifBlank { bruto }.take(400)
    }

    /** Envia um arquivo do aparelho (pedido → PUT → confirmação) e devolve o identificador da criação. */
    fun enviarArquivo(bytes: ByteArray, mime: String, nome: String): String =
        enviarCorpo(bytes.toRequestBody(mime.toMediaType()), mime, nome)

    /** Vídeo direto do disco (até 200 MB), sem carregar tudo na memória. */
    fun enviarVideo(arquivo: File): String {
        val mime = if (arquivo.extension.equals("webm", ignoreCase = true)) "video/webm" else "video/mp4"
        return enviarCorpo(arquivo.asRequestBody(mime.toMediaType()), mime, arquivo.name)
    }

    private fun enviarCorpo(corpo: okhttp3.RequestBody, mime: String, nome: String): String {
        val pedido = chamar("creations_request_upload", JSONObject().put("mimeType", mime))
        val dados = estruturado(pedido)
        val url = dados?.optString("proxyUploadUrl").orEmpty()
        val caminho = dados?.optString("path").orEmpty()
        if (url.isBlank() || caminho.isBlank()) error("O Magnific não devolveu o endereço de envio.")
        val put = Request.Builder().url(url).put(corpo).build()
        http.newCall(put).execute().use { resp ->
            if (!resp.isSuccessful) error("Envio ao Magnific falhou: HTTP ${resp.code}")
        }
        val fim = chamar(
            "creations_finalize_upload",
            JSONObject().put("path", caminho).put("fileName", nome.ifBlank { "referencia" }).put("visible", false),
        )
        return estruturado(fim)?.optString("identifier")?.takeIf { it.isNotBlank() }
            ?: ExtracaoDasCriacoesDoMagnific.identificadores(fim).firstOrNull()
            ?: error("O Magnific não devolveu o identificador do arquivo enviado.")
    }

    private fun estruturado(bruto: String): JSONObject? {
        val json = runCatching { JSONObject(bruto) }.getOrNull() ?: return null
        json.optJSONObject("structuredContent")?.let { return it }
        val texto = json.optJSONArray("content")?.optJSONObject(0)?.optString("text").orEmpty()
        return runCatching { JSONObject(texto) }.getOrNull()
    }

    /**
     * Pede [quantidade] imagens numa chamada (`count`) e devolve os identificadores das criações.
     * [referencias] = identificadores já enviados. Lança em erro.
     */
    fun pedirImagens(
        prompt: String,
        slug: String,
        proporcao: String?,
        resolucao: String?,
        referencias: List<String>,
        quantidade: Int,
    ): List<String> {
        val args = JSONObject().put("prompt", prompt).put("mode", slug).put("count", quantidade.coerceIn(1, 8))
        proporcao?.let { args.put("aspectRatio", it) }
        resolucao?.let { args.put("resolution", it) }
        if (referencias.isNotEmpty()) {
            val refs = JSONArray()
            referencias.forEach { refs.put(JSONObject().put("type", "image").put("identifier", it)) }
            args.put("references", refs)
        }
        val bruto = chamar("images_generate", args)
        registrarDiag("images_generate=${bruto.take(3000)}")
        return ExtracaoDasCriacoesDoMagnific.identificadores(bruto, ignorar = referencias.toSet())
            .ifEmpty { error("O Magnific não devolveu as imagens.") }
    }

    /** Espera uma imagem pedida por [pedirImagens] e baixa. */
    fun esperarImagem(identificador: String): ResultadoDaGeracaoMcpHiggsfield =
        esperarEBaixar(identificador, TIMEOUT_IMAGEM_MS, imagem = true)

    /**
     * Cria o vídeo e espera. Imagens vão como referências se o modelo aceitar; senão a primeira vira
     * quadro inicial. Vídeos vão como `references[].type = video`.
     */
    fun gerarVideo(
        prompt: String,
        slug: String,
        duracao: Int,
        proporcao: String?,
        resolucao: String?,
        imagens: List<String>,
        videos: List<String>,
        imagemComoReferencia: Boolean,
        aoCriarTarefa: (identificador: String) -> Unit,
    ): ResultadoDaGeracaoMcpHiggsfield {
        val args = JSONObject().put("slug", slug).put("duration", duracao)
        if (prompt.isNotBlank()) args.put("prompt", prompt)
        proporcao?.let { args.put("aspectRatio", it) }
        resolucao?.let { args.put("resolution", it) }
        val refs = JSONArray()
        if (imagemComoReferencia) {
            imagens.forEach { refs.put(JSONObject().put("type", "image").put("url", it)) }
        } else if (imagens.isNotEmpty()) {
            args.put("keyframes", JSONObject().put("start", JSONObject().put("type", "image").put("url", imagens.first())))
        }
        videos.forEach { refs.put(JSONObject().put("type", "video").put("url", it)) }
        if (refs.length() > 0) args.put("references", refs)
        val bruto = try {
            chamar("video_generate", args)
        } catch (e: Exception) {
            return ResultadoDaGeracaoMcpHiggsfield.Erro(e.message ?: "Falha ao pedir o vídeo ao Magnific.")
        }
        registrarDiag("video_generate=${bruto.take(3000)}")
        val id = ExtracaoDasCriacoesDoMagnific.identificadores(bruto, ignorar = (imagens + videos).toSet()).firstOrNull()
            ?: return ResultadoDaGeracaoMcpHiggsfield.Erro("O Magnific não devolveu o identificador do vídeo.")
        aoCriarTarefa(id)
        return esperarEBaixar(id, TIMEOUT_MS, imagem = false)
    }

    /** Uma consulta só (botão "Verificar" do card). */
    fun consultarCriacaoUmaVez(identificador: String): ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa =
        try {
            when (val e = ExtracaoDasCriacoesDoMagnific.estado(
                chamar("creation_status", JSONObject().put("creationIdentifier", identificador)),
            )) {
                is ExtracaoDasCriacoesDoMagnific.Estado.Pronto -> baixar(e.url)
                    ?.let { ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Pronto(it, e.url) }
                    ?: ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou("Não baixei o arquivo do Magnific.")
                ExtracaoDasCriacoesDoMagnific.Estado.Andando -> ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.AindaGerando
                is ExtracaoDasCriacoesDoMagnific.Estado.Falhou -> ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(e.mensagem)
            }
        } catch (e: Exception) {
            ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(e.message ?: "Falha ao consultar o Magnific.")
        }

    private fun esperarEBaixar(identificador: String, limiteMs: Long, imagem: Boolean): ResultadoDaGeracaoMcpHiggsfield {
        val inicio = System.currentTimeMillis()
        var falhasSeguidas = 0
        while (System.currentTimeMillis() - inicio < limiteMs) {
            Thread.sleep(if (imagem) 3_000L else POLL_MS)
            val bruto = try {
                chamar("creation_status", JSONObject().put("creationIdentifier", identificador)).also { falhasSeguidas = 0 }
            } catch (e: Exception) {
                if (e.message?.contains("expirou") == true || ++falhasSeguidas >= 5) {
                    return ResultadoDaGeracaoMcpHiggsfield.Erro(e.message ?: "Falha ao consultar o Magnific.")
                }
                continue
            }
            when (val e = ExtracaoDasCriacoesDoMagnific.estado(bruto)) {
                is ExtracaoDasCriacoesDoMagnific.Estado.Pronto -> {
                    val bytes = baixar(e.url)
                        ?: return ResultadoDaGeracaoMcpHiggsfield.Erro("Não baixei o arquivo gerado no Magnific.")
                    return ResultadoDaGeracaoMcpHiggsfield.Pronto(bytes, e.url, identificador, imagem)
                }
                is ExtracaoDasCriacoesDoMagnific.Estado.Falhou -> return ResultadoDaGeracaoMcpHiggsfield.Erro(e.mensagem)
                ExtracaoDasCriacoesDoMagnific.Estado.Andando -> Unit
            }
        }
        return ResultadoDaGeracaoMcpHiggsfield.Erro(
            "O Magnific demorou demais ($identificador). A criação pode terminar depois; toque em Verificar.",
        )
    }

    private fun baixar(url: String): ByteArray? = runCatching {
        http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            if (resp.isSuccessful) resp.body?.bytes() else null
        }
    }.getOrNull()

    /** Diagnóstico de ferramentas grátis (upload, consulta, custo). Nunca chama geração. */
    fun chamarParaDiagnostico(nome: String, arguments: JSONObject): String {
        require(nome in FERRAMENTAS_GRATIS_DO_DIAGNOSTICO) { "Ferramenta fora do diagnóstico: $nome" }
        return synchronized(travaMcp) {
            garantirSessaoMcp()
            toolsCall(nome, arguments).also { registrarDiag("$nome(${arguments.toString().take(300)})=${it.take(20_000)}") }
        }
    }

    /** PUT dos bytes no endereço devolvido por `creations_request_upload` (diagnóstico). */
    fun enviarBytesParaDiagnostico(url: String, bytes: ByteArray, mime: String): Int {
        val req = Request.Builder().url(url).put(bytes.toRequestBody(mime.toMediaType())).build()
        return http.newCall(req).execute().use { resp ->
            registrarDiag("PUT upload HTTP ${resp.code} ${resp.body?.string().orEmpty().take(300)}")
            resp.code
        }
    }

    private fun toolsCall(nome: String, arguments: JSONObject): String =
        mcpJsonRpc("tools/call", JSONObject().put("name", nome).put("arguments", arguments))?.toString().orEmpty()

    private fun garantirSessaoMcp() {
        renovarTokenSePreciso()
        mcpSessionId = ""
        rpcSeq = 1
        mcpJsonRpc(
            "initialize",
            JSONObject()
                .put("protocolVersion", "2025-03-26")
                .put("capabilities", JSONObject())
                .put("clientInfo", JSONObject().put("name", "chromebrothers").put("version", "1.0")),
        )
        mcpNotify("notifications/initialized", JSONObject())
    }

    private fun registrarCliente(): String {
        val corpo = JSONObject()
            .put("client_name", "ChromeBrothers")
            .put("redirect_uris", JSONArray().put(REDIRECT))
            .put("grant_types", JSONArray().put("authorization_code").put("refresh_token"))
            .put("response_types", JSONArray().put("code"))
            .put("token_endpoint_auth_method", "none")
            .toString()
        val req = Request.Builder().url(REGISTER).post(corpo.toRequestBody(jsonTipo)).build()
        http.newCall(req).execute().use { resp ->
            val texto = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) error("Registro no Magnific: HTTP ${resp.code} ${texto.take(300)}")
            return JSONObject(texto).optString("client_id").ifBlank { error("Registro no Magnific sem client_id.") }
        }
    }

    private fun trocarCodigoPorToken(clientId: String, code: String, verifier: String) {
        val form = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", REDIRECT)
            .add("client_id", clientId)
            .add("code_verifier", verifier)
            .build()
        http.newCall(Request.Builder().url(TOKEN).post(form).build()).execute().use { resp ->
            val texto = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) error("Token Magnific: HTTP ${resp.code} ${texto.take(300)}")
            gravarRespostaDoToken(JSONObject(texto), refreshAnterior = "")
        }
    }

    private fun gravarRespostaDoToken(json: JSONObject, refreshAnterior: String) {
        val access = json.optString("access_token")
        if (access.isBlank()) error("Token Magnific sem access_token.")
        val refresh = json.optString("refresh_token").ifBlank { refreshAnterior }
        val expiraEm = System.currentTimeMillis() + json.optLong("expires_in", 300L) * 1000L
        sessao.gravarTokens(access, refresh, expiraEm)
    }

    /** O access token do Keycloak dura minutos: renova antes de cada sessão MCP se faltar menos de 1 min. */
    private fun renovarTokenSePreciso(forcar: Boolean = false) = synchronized(travaToken) {
        if (!forcar && sessao.accessToken().isNotBlank() &&
            System.currentTimeMillis() < sessao.expiraEmMs() - 60_000L
        ) {
            return@synchronized
        }
        val refresh = sessao.refreshToken()
        if (refresh.isBlank()) error(SessaoDaContaMagnificNoAparelho.SEM_SESSAO)
        val form = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refresh)
            .add("client_id", sessao.clientId())
            .build()
        http.newCall(Request.Builder().url(TOKEN).post(form).build()).execute().use { resp ->
            val texto = resp.body?.string().orEmpty()
            if (resp.code == 400 || resp.code == 401) {
                // Refresh vencido ou revogado: precisa logar de novo.
                sessao.apagar()
                error("A sessão Magnific expirou. Ligue de novo na engrenagem do chat.")
            }
            if (!resp.isSuccessful) error("Renovação do token Magnific: HTTP ${resp.code}")
            gravarRespostaDoToken(JSONObject(texto), refreshAnterior = refresh)
        }
    }

    private fun mcpJsonRpc(method: String, params: JSONObject?): JSONObject? {
        val payload = JSONObject().put("jsonrpc", "2.0").put("id", rpcSeq++).put("method", method)
        if (params != null) payload.put("params", params)
        val json = parseMcpPayload(mcpPost(payload.toString())) ?: return null
        if (json.has("error")) error(json.optJSONObject("error")?.optString("message") ?: json.toString())
        return json.optJSONObject("result") ?: json
    }

    private fun mcpNotify(method: String, params: JSONObject) {
        val payload = JSONObject().put("jsonrpc", "2.0").put("method", method).put("params", params)
        runCatching { mcpPost(payload.toString()) }
    }

    private fun mcpPost(jsonBody: String, jaRenovou: Boolean = false): String {
        val token = sessao.accessToken()
        if (token.isBlank()) error(SessaoDaContaMagnificNoAparelho.SEM_SESSAO)
        val builder = Request.Builder()
            .url(MCP)
            .post(jsonBody.toRequestBody(jsonTipo))
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json, text/event-stream")
        if (mcpSessionId.isNotBlank()) builder.header("Mcp-Session-Id", mcpSessionId)
        http.newCall(builder.build()).execute().use { resp ->
            resp.headers.names().firstOrNull { it.equals("mcp-session-id", ignoreCase = true) }
                ?.let { resp.header(it) }?.trim()?.takeIf { it.isNotEmpty() }?.let { mcpSessionId = it }
            val texto = resp.body?.string().orEmpty()
            registrarDiag("MCP ${resp.code} body=${texto.take(800)}")
            if (resp.code == 401 && !jaRenovou) {
                renovarTokenSePreciso(forcar = true)
                return mcpPost(jsonBody, jaRenovou = true)
            }
            if (!resp.isSuccessful) error("MCP Magnific HTTP ${resp.code}: ${texto.take(300)}")
            return texto
        }
    }

    private fun parseMcpPayload(corpo: String): JSONObject? {
        val trim = corpo.trim()
        if (trim.startsWith("{")) return runCatching { JSONObject(trim) }.getOrNull()
        return trim.lineSequence()
            .filter { it.startsWith("data:") }
            .map { it.removePrefix("data:").trim() }
            .filter { it.isNotEmpty() && it != "[DONE]" }
            .toList()
            .asReversed()
            .firstNotNullOfOrNull { runCatching { JSONObject(it) }.getOrNull() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun abrirWebViewDeLogin(
        activity: android.app.Activity,
        url: String,
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
                addView(web, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            },
        )
        dialog.setOnCancelListener {
            if (feito.compareAndSet(false, true)) aoCodigo(null, "Login cancelado.")
        }
        dialog.setOnDismissListener { web.destroy() }
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
                interceptar(request?.url?.toString().orEmpty())

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, urlAntiga: String?): Boolean =
                interceptar(urlAntiga.orEmpty())

            private fun interceptar(destino: String): Boolean {
                if (!destino.startsWith(REDIRECT)) return false
                if (!feito.compareAndSet(false, true)) return true
                val uri = Uri.parse(destino)
                val code = uri.getQueryParameter("code").orEmpty()
                val erro = uri.getQueryParameter("error_description") ?: uri.getQueryParameter("error")
                dialog.dismiss()
                when {
                    uri.getQueryParameter("state").orEmpty() != stateEsperado -> aoCodigo(null, "State OAuth inválido.")
                    code.isNotBlank() -> aoCodigo(code, null)
                    else -> aoCodigo(null, erro ?: "O Magnific não devolveu o código de login.")
                }
                return true
            }
        }
        dialog.show()
        web.loadUrl(url)
    }

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
        Log.i(TAG, msg.take(4000))
        runCatching { File(context.cacheDir, "magnific-mcp-diag.txt").appendText("${System.currentTimeMillis()} $msg\n") }
    }

    companion object {
        private const val TAG = "MagnificCB"
        const val MCP = "https://mcp.magnific.com"
        private const val REALM = "https://auth.magnific.com/realms/mcp"
        const val REGISTER = "$REALM/clients-registrations/openid-connect"
        const val AUTHORIZE = "$REALM/protocol/openid-connect/auth"
        const val TOKEN = "$REALM/protocol/openid-connect/token"
        /** O Keycloak guarda o retorno de loopback sem porta; a WebView intercepta antes de carregar. */
        const val REDIRECT = "http://127.0.0.1/callback"
        const val SCOPE = "openid offline_access mcp:custom-audience"
        const val TIMEOUT_MS = 15 * 60 * 1000L
        const val TIMEOUT_IMAGEM_MS = 5 * 60 * 1000L
        const val POLL_MS = 5_000L
        const val TOOL_IMAGES_MODELS_LIST = "images_models_list"
        const val TOOL_VIDEO_MODELS_LIST = "video_models_list"

        private val FERRAMENTAS_GRATIS_DO_DIAGNOSTICO = setOf(
            "creations_request_upload", "creations_finalize_upload", "creation_status", "creations_get",
            "simulate_cost", "account_balance",
        )

        private val FERRAMENTAS_DE_INTERESSE = setOf(
            "images_generate", "video_generate",
            "images_models_list", "images_models_show", "images_models_settings",
            "video_models_list", "video_models_show",
            "creations_request_upload", "creations_finalize_upload", "creations_upload_file",
            "creation_status", "creations_wait", "creations_get", "simulate_cost",
        )
    }
}

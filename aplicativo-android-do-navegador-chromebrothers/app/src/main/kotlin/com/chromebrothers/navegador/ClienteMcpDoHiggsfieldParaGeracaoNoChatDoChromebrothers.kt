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
 * OAuth (PKCE + loopback 127.0.0.1) e MCP streamable HTTP em https://mcp.higgsfield.ai/mcp.
 */
class ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers(
    private val context: Context,
    private val sessao: SessaoDaContaHiggsfieldNoAparelho,
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
                    .appendQueryParameter("scope", "openid email offline_access")
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
                principal.post { aoFim(e.message ?: "Falha ao registrar o cliente Higgsfield.") }
            }
        }.start()
    }

    fun gerarImagem(
        prompt: String,
        modelo: String,
        referencia: ByteArray? = null,
        nomeArquivoReferencia: String = "referencia.jpg",
        roleReferencia: String = "",
        quantidade: Int = 1,
        referencias: List<Pair<ByteArray, String>> = emptyList(),
        aspectRatio: String = "1:1",
        resolution: String = "1k",
    ): ResultadoDaGeracaoMcpHiggsfield {
        val lista = when {
            referencias.isNotEmpty() -> referencias
            referencia != null && referencia.isNotEmpty() ->
                listOf(referencia to nomeArquivoReferencia)
            else -> emptyList()
        }
        return gerar(
            tool = "generate_image",
            prompt = prompt,
            modelo = modelo,
            imagem = true,
            referencias = lista,
            roleReferencia = roleReferencia,
            quantidade = quantidade,
            aspectRatio = aspectRatio,
            resolution = resolution,
        )
    }

    fun gerarVideo(
        prompt: String,
        modelo: String,
        aspectRatio: String = "16:9",
        resolution: String = "480p",
        duracaoSegundos: Int = 5,
        referencias: List<Pair<ByteArray, String>> = emptyList(),
    ): ResultadoDaGeracaoMcpHiggsfield {
        return gerar(
            tool = "generate_video",
            prompt = prompt,
            modelo = modelo,
            imagem = false,
            referencias = referencias,
            aspectRatio = aspectRatio,
            resolution = resolution,
            duracaoSegundos = duracaoSegundos,
        )
    }

    /**
     * Cria o job de vídeo (RPC sincronizado) e devolve só o job_id —
     * para disparar N gerações e esperar em paralelo.
     */
    fun criarJobDeVideo(
        prompt: String,
        modelo: String,
        aspectRatio: String = "16:9",
        resolution: String = "480p",
        duracaoSegundos: Int = 5,
        mediaIds: List<Pair<String, String>> = emptyList(),
        /** Parâmetros do modelo além dos comuns (ex. `mode` com vídeo de referência). */
        parametrosExtras: Map<String, String> = emptyMap(),
    ): ResultadoDaCriacaoDeJobHiggsfield {
        if (!sessao.temSessao()) {
            return ResultadoDaCriacaoDeJobHiggsfield.Erro(
                com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO,
            )
        }
        return try {
            synchronized(travaMcp) {
                garantirSessaoMcp()
                val modeloEfetivo = modelo.ifBlank {
                    com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat.MODELO_PADRAO_VIDEO
                }
                val params = JSONObject()
                    .put("model", modeloEfetivo)
                    .put("prompt", prompt)
                    .put("aspect_ratio", aspectRatio.trim().ifBlank { "16:9" })
                    .put("resolution", resolution.trim().lowercase().ifBlank { "480p" })
                    .put("duration", duracaoSegundos.coerceIn(2, 30))
                parametrosExtras.forEach { (chave, valor) -> params.put(chave, valor) }
                if (mediaIds.isNotEmpty()) {
                    val medias = JSONArray()
                    // Wan 3.0: não misturar start_image/end_image com image_references (422).
                    mediaIds.forEach { (mediaId, role) ->
                        medias.put(
                            JSONObject()
                                .put("value", mediaId)
                                .put("role", role.ifBlank { "image_references" }),
                        )
                    }
                    params.put("medias", medias)
                }
                val args = JSONObject().put("params", params)
                registrarDiag("tools/call generate_video (create) args=$args")
                val jobBruto = toolsCall("generate_video", args)
                registrarDiag("tools/call generate_video (create) result=$jobBruto")
                if (ehErroDeFerramenta(jobBruto)) {
                    return ResultadoDaCriacaoDeJobHiggsfield.Erro(
                        textoUtilDoToolsCall(jobBruto).ifBlank { jobBruto },
                    )
                }
                val jobId = extrairJobId(jobBruto)
                    ?: return ResultadoDaCriacaoDeJobHiggsfield.Erro(
                        "O Higgsfield não devolveu job_id.\n$jobBruto",
                    )
                ResultadoDaCriacaoDeJobHiggsfield.Criado(jobId)
            }
        } catch (e: Exception) {
            ResultadoDaCriacaoDeJobHiggsfield.Erro(e.message ?: "Falha ao criar job de vídeo.")
        }
    }

    fun aguardarEBaixarVideoDoJob(jobId: String): ResultadoDaGeracaoMcpHiggsfield {
        return try {
            val url = esperarUrlDoJob(jobId)
                ?: return ResultadoDaGeracaoMcpHiggsfield.Erro(
                    "A geração demorou demais ou falhou (job $jobId).",
                )
            val bytes = baixarBytes(url)
                ?: return ResultadoDaGeracaoMcpHiggsfield.Erro("Não baixei o vídeo gerado.")
            ResultadoDaGeracaoMcpHiggsfield.Pronto(
                bytes = bytes,
                enderecoRemoto = url,
                jobId = jobId,
                imagem = false,
            )
        } catch (e: Exception) {
            ResultadoDaGeracaoMcpHiggsfield.Erro(e.message ?: "Falha ao baixar o vídeo.")
        }
    }

    /** Um `job_status` só (sem esperar); baixa o vídeo se o job já concluiu. */
    fun consultarVideoDoJobUmaVez(jobId: String): ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa {
        if (!sessao.temSessao()) {
            return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(
                com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO,
            )
        }
        return try {
            synchronized(travaMcp) {
                if (mcpSessionId.isBlank()) garantirSessaoMcp()
            }
            val regras = com.chromebrothers.navegador.regras.ExtracaoDaUrlDoResultadoDoJobHiggsfield
            val status = toolsCall("job_status", JSONObject().put("jobId", jobId).put("sync", true))
            registrarDiag("consulta unica job_status=$status")
            if (ehErroDeFerramenta(status)) {
                return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(
                    textoUtilDoToolsCall(status).ifBlank { "O Higgsfield recusou a consulta do job $jobId." },
                )
            }
            if (regras.jobFalhou(status)) {
                return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou("O job $jobId falhou no Higgsfield.")
            }
            if (!regras.jobConcluido(status)) return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.AindaGerando
            val url = regras.urlDoResultado(status)
                ?: regras.urlDoResultado(
                    runCatching {
                        toolsCall(
                            "show_generation_by_ids",
                            JSONObject().put(
                                "jobs",
                                JSONArray().put(JSONObject().put("index", 0).put("job_id", jobId)),
                            ),
                        )
                    }.getOrNull().orEmpty(),
                )
                ?: return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.AindaGerando
            val bytes = baixarBytes(url)
                ?: return ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou("Job pronto, mas não baixei o vídeo.")
            ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Pronto(bytes, url)
        } catch (e: Exception) {
            ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou(e.message ?: "Falha ao consultar o Higgsfield.")
        }
    }

    /** Upload sincronizado de referências; reutilizável antes de N jobs paralelos. */
    fun enviarReferenciasDeVideo(
        referencias: List<Pair<ByteArray, String>>,
        videos: List<File> = emptyList(),
    ): ResultadoDoUploadDeReferenciasHiggsfield {
        if (referencias.isEmpty() && videos.isEmpty()) return ResultadoDoUploadDeReferenciasHiggsfield.Ok(emptyList())
        if (!sessao.temSessao()) {
            return ResultadoDoUploadDeReferenciasHiggsfield.Erro(
                com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO,
            )
        }
        return try {
            synchronized(travaMcp) {
                garantirSessaoMcp()
                val ids = mutableListOf<Pair<String, String>>()
                val lista = referencias.take(4).filter { it.first.isNotEmpty() }
                // 1 ref → start_image (i2v); 2+ → só image_references (Wan 3 não aceita misturar).
                // Com vídeo junto também é modo referência: start_image não se mistura com *_references.
                val roleUnica = if (lista.size <= 1 && videos.isEmpty()) "start_image" else "image_references"
                for ((bytes, nome) in lista) {
                    val mediaId = enviarReferenciaDeImagem(bytes, nome.ifBlank { "referencia.jpg" })
                        ?: return ResultadoDoUploadDeReferenciasHiggsfield.Erro(
                            "Não consegui enviar a imagem de referência ao Higgsfield.",
                        )
                    ids += mediaId to roleUnica
                }
                for (video in videos) {
                    val mediaId = enviarMidia(
                        corpo = video.asRequestBody(tipoDeVideo(video.name).toMediaType()),
                        nome = video.name,
                        contentType = tipoDeVideo(video.name),
                        tipo = "video",
                    ) ?: return ResultadoDoUploadDeReferenciasHiggsfield.Erro(
                        "Não consegui enviar o vídeo de referência ao Higgsfield.",
                    )
                    ids += mediaId to "video_references"
                }
                ResultadoDoUploadDeReferenciasHiggsfield.Ok(ids)
            }
        } catch (e: Exception) {
            ResultadoDoUploadDeReferenciasHiggsfield.Erro(e.message ?: "Falha no upload das referências.")
        }
    }

    fun listarModelos(filtro: String): String {
        if (!sessao.temSessao()) {
            return com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO
        }
        return try {
            garantirSessaoMcp()
            val filtroNorm = filtro.trim().lowercase()
            val soImagem = filtroNorm.isEmpty() ||
                filtroNorm == "image" || filtroNorm == "imagem" || filtroNorm.contains("image")
            val args = when {
                filtroNorm.isEmpty() || filtroNorm == "image" || filtroNorm == "imagem" ->
                    JSONObject().put("action", "list").put("type", "image")
                soImagem ->
                    JSONObject()
                        .put("action", "search")
                        .put("query", filtro)
                        .put("type", "image")
                else ->
                    JSONObject()
                        .put("action", "search")
                        .put("query", filtro)
            }
            val bruto = textoUtilDoToolsCall(toolsCall("models_explore", args))
            registrarDiag("listarModelos filtro=$filtro bruto=${bruto.take(4000)}")
            runCatching {
                File(context.cacheDir, "higgsfield-modelos-listagem.txt").writeText(bruto)
            }
            if (bruto.isBlank() || ehErroDeFerramenta(bruto)) {
                val msg = textoUtilDoToolsCall(bruto).ifBlank { bruto }
                return msg.ifBlank { "O Higgsfield não devolveu o catálogo de modelos." }
            }
            com.chromebrothers.navegador.regras.ListagemDeModelosDoHiggsfieldNoChat.formatarCatalogo(bruto, filtro)
        } catch (e: Exception) {
            registrarDiag("listarModelos falhou: ${e.message}")
            e.message ?: "Falha ao listar modelos do Higgsfield."
        }
    }

    /**
     * Catálogo BRUTO (texto útil do models_explore list type=image) para a ponte JSON da coleção.
     * Lança exceção com mensagem legível em caso de falha.
     */
    fun listarModelosDeImagemBruto(): String =
        listarModelosBrutoPorTipo(
            tipo = "image",
            mensagemSemCatalogo = "O Higgsfield não devolveu o catálogo de modelos.",
        )

    fun listarModelosDeVideoBruto(): String =
        listarModelosBrutoPorTipo(
            tipo = "video",
            mensagemSemCatalogo = "O Higgsfield não devolveu o catálogo de modelos de vídeo.",
        )

    /**
     * Percorre todas as páginas do `models_explore` (`after` = `next_page_token`).
     * Sem isso o select da coleção fica só com os ~20 primeiros e o Wan 3.0 some.
     */
    private fun listarModelosBrutoPorTipo(tipo: String, mensagemSemCatalogo: String): String {
        if (!sessao.temSessao()) {
            error(com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO)
        }
        garantirSessaoMcp()
        val paginas = mutableListOf<String>()
        var after: String? = null
        var pagina = 0
        while (pagina < LIMITE_DE_PAGINAS_DO_CATALOGO) {
            val args = JSONObject()
                .put("action", "list")
                .put("type", tipo)
                .put("limit", TAMANHO_DA_PAGINA_DO_CATALOGO)
            if (!after.isNullOrBlank()) args.put("after", after)
            val bruto = textoUtilDoToolsCall(toolsCall("models_explore", args))
            if (bruto.isBlank() || ehErroDeFerramenta(bruto)) {
                if (paginas.isEmpty()) {
                    error(bruto.ifBlank { mensagemSemCatalogo })
                }
                break
            }
            paginas += bruto
            after = com.chromebrothers.navegador.regras.ListagemDeModelosDoHiggsfieldNoChat
                .cursorDaProximaPagina(bruto)
            if (after == null) break
            pagina++
        }
        if (paginas.isEmpty()) error(mensagemSemCatalogo)
        var mesclado = com.chromebrothers.navegador.regras.ListagemDeModelosDoHiggsfieldNoChat
            .mesclarPaginasDeCatalogo(paginas)
        if (tipo == "video") {
            mesclado = garantirModeloDeVideoNoCatalogo(
                mesclado,
                com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat.MODELO_PADRAO_VIDEO,
            )
        }
        registrarDiag("catalogo $tipo paginas=${paginas.size} chars=${mesclado.length}")
        return mesclado
    }

    private fun garantirModeloDeVideoNoCatalogo(mesclado: String, modelId: String): String {
        if (mesclado.contains("\"id\":\"$modelId\"")) return mesclado
        val getBruto = textoUtilDoToolsCall(
            toolsCall(
                "models_explore",
                JSONObject().put("action", "get").put("model_id", modelId),
            ),
        )
        if (getBruto.isBlank() || ehErroDeFerramenta(getBruto)) {
            registrarDiag("garantirModelo $modelId falhou: ${getBruto.take(300)}")
            return mesclado
        }
        val paginaSintetica = when {
            getBruto.contains("\"items\"") -> getBruto
            getBruto.contains("\"id\"") -> """{"items":[$getBruto]}"""
            else -> return mesclado
        }
        return com.chromebrothers.navegador.regras.ListagemDeModelosDoHiggsfieldNoChat
            .mesclarPaginasDeCatalogo(listOf(paginaSintetica, mesclado))
    }

    /**
     * Histórico recente do Higgsfield (todos os estados): resposta bruta de `show_generations`.
     * Serve para achar um job criado lá cuja resposta de criação se perdeu na rede.
     */
    fun listarGeracoesRecentesBruto(tipo: String, tamanho: Int = 30): String {
        if (!sessao.temSessao()) return ""
        synchronized(travaMcp) {
            if (mcpSessionId.isBlank()) garantirSessaoMcp()
        }
        val bruto = toolsCall(
            "show_generations",
            JSONObject().put("type", tipo).put("only_completed", false).put("size", tamanho.coerceIn(1, 100)),
        )
        registrarDiag("show_generations($tipo) result=${bruto.take(4000)}")
        return bruto
    }

    /** Diagnóstico: todas as ferramentas do MCP com descrição e schema (procurar histórico de gerações etc.). */
    fun diagnosticarFerramentas(): String {
        if (!sessao.temSessao()) return "sem sessão"
        synchronized(travaMcp) { garantirSessaoMcp() }
        val tools = synchronized(travaMcp) { mcpJsonRpc(method = "tools/list", params = JSONObject()) }
        return tools?.toString(2).orEmpty()
    }

    /** Dump schemas + models_explore.get para investigar imagem-de-referência no MCP. */
    fun diagnosticarSchemasDeReferenciaDeImagem(modelo: String): String {
        if (!sessao.temSessao()) return "sem sessão"
        garantirSessaoMcp()
        val sb = StringBuilder()
        runCatching {
            val tools = mcpJsonRpc(method = "tools/list", params = JSONObject())
            val lista = tools?.optJSONArray("tools")
            if (lista != null) {
                for (i in 0 until lista.length()) {
                    val t = lista.optJSONObject(i) ?: continue
                    val nome = t.optString("name")
                    if (
                        nome == "media_upload" || nome == "media_confirm" ||
                        nome == "media_import_url" || nome == "media_upload_widget" ||
                        nome == "generate_image"
                    ) {
                        val schema = t.optJSONObject("inputSchema")?.toString(2).orEmpty()
                        registrarDiag("schema_ref $nome=$schema")
                        sb.appendLine("=== $nome ===").appendLine(schema).appendLine()
                    }
                }
            }
        }
        val getArgs = JSONObject()
            .put("action", "get")
            .put("model_id", modelo)
        val getBruto = toolsCall("models_explore", getArgs)
        registrarDiag("models_explore get $modelo=$getBruto")
        sb.appendLine("=== models_explore.get $modelo ===")
        sb.appendLine(textoUtilDoToolsCall(getBruto).take(6000))
        File(context.cacheDir, "higgsfield-schema-referencia-imagem.txt").writeText(sb.toString())
        return sb.toString()
    }

    /** Gera com JPEG de referência (media_upload → PUT → media_confirm → generate_image). */
    fun diagnosticarGeracaoComReferenciaDeTeste(prompt: String, modelo: String): String {
        if (!sessao.temSessao()) return "sem sessão"
        val jpeg = jpegDeTesteParaReferenciaHiggsfield()
        val resultado = gerarImagem(
            prompt = prompt,
            modelo = modelo,
            referencia = jpeg,
            nomeArquivoReferencia = "referencia-teste.jpg",
            roleReferencia = "image_references",
        )
        val texto = when (resultado) {
            is ResultadoDaGeracaoMcpHiggsfield.Erro -> "ERRO: ${resultado.mensagem}"
            is ResultadoDaGeracaoMcpHiggsfield.Pronto -> {
                File(context.cacheDir, "higgsfield-diag-ref-teste.png").writeBytes(resultado.bytes)
                "OK bytes=${resultado.bytes.size} job=${resultado.jobId} url=${resultado.enderecoRemoto}"
            }
        }
        registrarDiag("DIAG gerar_com_referencia=$texto")
        File(context.cacheDir, "higgsfield-diag-ref-resultado.txt").writeText(texto)
        return texto
    }

    /**
     * Testa vários formatos de arguments do generate_image contra o MCP real e,
     * se algum funcionar, espera o job e baixa os bytes (só diagnóstico).
     */
    fun diagnosticarPayloadsEGerarImagemDeTeste(prompt: String, modelo: String): String {
        if (!sessao.temSessao()) {
            return "sem sessão"
        }
        garantirSessaoMcp()
        val modeloEfetivo = modelo.ifBlank {
            com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat.MODELO_PADRAO_IMAGEM
        }
        val sb = StringBuilder()
        val args = JSONObject().put(
            "params",
            JSONObject()
                .put("model", modeloEfetivo)
                .put("prompt", prompt)
                .put("aspect_ratio", "1:1"),
        )
        registrarDiag("DIAG generate_image args=$args")
        val bruto = toolsCall("generate_image", args)
        registrarDiag("DIAG generate_image result=$bruto")
        sb.appendLine(textoUtilDoToolsCall(bruto).take(400))
        if (ehErroDeFerramenta(bruto)) {
            val explore = toolsCall("models_explore", JSONObject().put("action", "list"))
            registrarDiag("DIAG models_explore list=${explore.take(1200)}")
            sb.appendLine("models_explore => ${textoUtilDoToolsCall(explore).take(400)}")
            return sb.toString()
        }
        val jobId = extrairJobId(bruto)
            ?: return sb.appendLine("sem job_id").toString()
        val url = esperarUrlDoJob(jobId)
        sb.appendLine("job=$jobId url=$url")
        if (url != null) {
            val bytes = baixarBytes(url)
            sb.appendLine("download bytes=${bytes?.size ?: 0}")
            if (bytes != null && bytes.isNotEmpty()) {
                File(context.cacheDir, "higgsfield-diag-teste.png").writeBytes(bytes)
                sb.appendLine("salvo cache/higgsfield-diag-teste.png")
            }
        }
        return sb.toString()
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

    private fun gerar(
        tool: String,
        prompt: String,
        modelo: String,
        imagem: Boolean,
        referencias: List<Pair<ByteArray, String>> = emptyList(),
        roleReferencia: String = "",
        quantidade: Int = 1,
        aspectRatio: String = "1:1",
        resolution: String = "1k",
        duracaoSegundos: Int = 5,
    ): ResultadoDaGeracaoMcpHiggsfield {
        if (!sessao.temSessao()) {
            return ResultadoDaGeracaoMcpHiggsfield.Erro(
                com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO,
            )
        }
        return try {
            synchronized(travaMcp) {
                garantirSessaoMcp()
                val modeloEfetivo = modelo.ifBlank {
                    if (imagem) {
                        com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat.MODELO_PADRAO_IMAGEM
                    } else {
                        com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat.MODELO_PADRAO_VIDEO
                    }
                }
                val proporcao = aspectRatio.trim().ifBlank { if (imagem) "1:1" else "16:9" }
                val resolucao = resolution.trim().lowercase().ifBlank { if (imagem) "1k" else "480p" }
                val params = JSONObject()
                    .put("model", modeloEfetivo)
                    .put("prompt", prompt)
                    .put("aspect_ratio", proporcao)
                params.put("resolution", resolucao)
                if (!imagem) {
                    params.put("duration", duracaoSegundos.coerceIn(2, 30))
                }
                if (imagem && quantidade > 1) {
                    params.put("count", quantidade.coerceIn(1, 4))
                }
                if (referencias.isNotEmpty()) {
                    val medias = JSONArray()
                    val refs = referencias.take(4).filter { it.first.isNotEmpty() }
                    val roleVideo = if (refs.size <= 1) "start_image" else "image_references"
                    for ((bytes, nome) in refs) {
                        val mediaId = enviarReferenciaDeImagem(bytes, nome.ifBlank { "referencia.jpg" })
                            ?: return ResultadoDaGeracaoMcpHiggsfield.Erro(
                                "Não consegui enviar a imagem de referência ao Higgsfield.",
                            )
                        val role = if (imagem) {
                            com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat
                                .resolverRoleReferencia(modeloEfetivo, roleReferencia)
                        } else {
                            roleVideo
                        }
                        medias.put(JSONObject().put("value", mediaId).put("role", role))
                    }
                    if (medias.length() > 0) params.put("medias", medias)
                }
                val args = JSONObject().put("params", params)
                registrarDiag("tools/call $tool args=$args")
                val jobBruto = toolsCall(tool, args)
                registrarDiag("tools/call $tool result=$jobBruto")
                if (ehErroDeFerramenta(jobBruto)) {
                    return ResultadoDaGeracaoMcpHiggsfield.Erro(textoUtilDoToolsCall(jobBruto).ifBlank { jobBruto })
                }
                val jobId = extrairJobId(jobBruto)
                    ?: return ResultadoDaGeracaoMcpHiggsfield.Erro("O Higgsfield não devolveu job_id.\n$jobBruto")
                val url = esperarUrlDoJob(jobId)
                    ?: return ResultadoDaGeracaoMcpHiggsfield.Erro("A geração demorou demais ou falhou (job $jobId).")
                val bytes = baixarBytes(url)
                    ?: return ResultadoDaGeracaoMcpHiggsfield.Erro("Não baixei o arquivo gerado.")
                ResultadoDaGeracaoMcpHiggsfield.Pronto(
                    bytes = bytes,
                    enderecoRemoto = url,
                    jobId = jobId,
                    imagem = imagem,
                )
            }
        } catch (e: Exception) {
            registrarDiag("gerar falhou: ${e.message}")
            ResultadoDaGeracaoMcpHiggsfield.Erro(e.message ?: "Falha na geração Higgsfield.")
        }
    }

    private fun jpegDeTesteParaReferenciaHiggsfield(): ByteArray {
        val bmp = android.graphics.Bitmap.createBitmap(96, 96, android.graphics.Bitmap.Config.ARGB_8888)
        bmp.eraseColor(0xFF2F6FED.toInt())
        val saida = java.io.ByteArrayOutputStream()
        bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, saida)
        bmp.recycle()
        return saida.toByteArray()
    }

    /**
     * media_upload → PUT bytes → media_confirm → media_id utilizável em params.medias.
     */
    private fun enviarReferenciaDeImagem(bytes: ByteArray, nomeArquivo: String): String? {
        val nome = nomeArquivo.ifBlank { "referencia.jpg" }
        val contentType = when {
            nome.endsWith(".png", ignoreCase = true) -> "image/png"
            nome.endsWith(".webp", ignoreCase = true) -> "image/webp"
            else -> "image/jpeg"
        }
        return enviarMidia(bytes.toRequestBody(contentType.toMediaType()), nome, contentType, "image")
    }

    private fun tipoDeVideo(nome: String): String = when {
        nome.endsWith(".mov", ignoreCase = true) -> "video/quicktime"
        nome.endsWith(".webm", ignoreCase = true) -> "video/webm"
        else -> "video/mp4"
    }

    /** media_upload → PUT → media_confirm para imagem ou vídeo ([tipo] = "image" | "video"). */
    private fun enviarMidia(corpo: okhttp3.RequestBody, nome: String, contentType: String, tipo: String): String? {
        val uploadArgs = JSONObject()
            .put("filename", nome)
            .put("content_type", contentType)
            .put("method", "upload_url")
        val uploadBruto = toolsCall("media_upload", uploadArgs)
        registrarDiag("media_upload=$uploadBruto")
        if (ehErroDeFerramenta(uploadBruto)) {
            error(textoUtilDoToolsCall(uploadBruto).ifBlank { "media_upload falhou." })
        }
        val mediaId = extrairCampoMedia(uploadBruto, "media_id")
            ?: error("media_upload sem media_id.\n$uploadBruto")
        val uploadUrl = extrairCampoMedia(uploadBruto, "upload_url")
            ?: error("media_upload sem upload_url.\n$uploadBruto")
        val tipoUpload = extrairCampoMedia(uploadBruto, "content_type")?.ifBlank { null } ?: contentType
        // URL assinada exige Content-Type + If-None-Match (SignedHeaders=content-type;host;if-none-match).
        val putOk = http.newCall(
            Request.Builder()
                .url(uploadUrl)
                .header("Content-Type", tipoUpload)
                .header("If-None-Match", "*")
                .put(corpo)
                .build(),
        ).execute().use { resp ->
            val corpoPut = resp.body?.string().orEmpty().take(500)
            registrarDiag("PUT referencia ($tipo) HTTP ${resp.code} body=$corpoPut")
            resp.isSuccessful
        }
        if (!putOk) error(if (tipo == "video") "Falha no envio do vídeo de referência." else "Falha no PUT da imagem de referência.")
        val confirmArgs = JSONObject()
            .put("type", tipo)
            .put("media_id", mediaId)
        val confirmBruto = toolsCall("media_confirm", confirmArgs)
        registrarDiag("media_confirm=$confirmBruto")
        if (ehErroDeFerramenta(confirmBruto)) {
            error(textoUtilDoToolsCall(confirmBruto).ifBlank { "media_confirm falhou." })
        }
        return extrairCampoMedia(confirmBruto, "media_id")
            ?: extrairCampoMedia(confirmBruto, "id")
            ?: mediaId
    }

    private fun extrairCampoMedia(bruto: String, chave: String): String? {
        val texto = bruto.replace("\\/", "/")
        Regex(""""$chave"\s*:\s*"([^"]+)"""").find(texto)?.groupValues?.getOrNull(1)
            ?.takeIf { it.isNotBlank() }?.let { return it }
        val json = texto.trim().takeIf { it.startsWith("{") }
            ?.let { runCatching { JSONObject(it) }.getOrNull() }
            ?: return null
        json.optString(chave).takeIf { it.isNotBlank() }?.let { return it }
        json.optJSONObject("structuredContent")?.optString(chave)?.takeIf { it.isNotBlank() }?.let { return it }
        val content = json.optJSONArray("content") ?: return null
        for (i in 0 until content.length()) {
            val item = content.optJSONObject(i) ?: continue
            item.optString(chave).takeIf { it.isNotBlank() }?.let { return it }
            val nested = item.optString("text")
            Regex(""""$chave"\s*:\s*"([^"]+)"""").find(nested)?.groupValues?.getOrNull(1)
                ?.takeIf { it.isNotBlank() }?.let { return it }
        }
        return null
    }

    private fun garantirSessaoMcp() {
        refreshSePreciso()
        mcpSessionId = ""
        rpcSeq = 1
        val init = mcpJsonRpc(
            method = "initialize",
            params = JSONObject()
                .put("protocolVersion", "2025-03-26")
                .put("capabilities", JSONObject())
                .put(
                    "clientInfo",
                    JSONObject().put("name", "chromebrothers").put("version", "1.0"),
                ),
        )
        registrarDiag("initialize result=$init session=$mcpSessionId")
        mcpNotify("notifications/initialized", JSONObject())
        runCatching {
            val tools = mcpJsonRpc(method = "tools/list", params = JSONObject())
            val lista = tools?.optJSONArray("tools")
            if (lista != null) {
                for (i in 0 until lista.length()) {
                    val t = lista.optJSONObject(i) ?: continue
                    val nome = t.optString("name")
                    if (
                        nome == "generate_image" || nome == "models_explore" || nome == "generate_video" ||
                        nome == "jobs_wait" || nome == "job_status" || nome == "job_display" ||
                        nome == "show_generation_by_ids"
                    ) {
                        registrarDiag("schema $nome=${t.optJSONObject("inputSchema")}")
                    }
                }
            }
        }
    }

    private fun toolsCall(nome: String, arguments: JSONObject): String {
        // Serializa o JSON-RPC: jobs paralelos podem esperar juntos, mas cada call MCP é exclusiva.
        synchronized(travaMcp) {
            val result = mcpJsonRpc(
                method = "tools/call",
                params = JSONObject()
                    .put("name", nome)
                    .put("arguments", arguments),
            )
            return result?.toString().orEmpty()
        }
    }

    private fun ehErroDeFerramenta(bruto: String): Boolean {
        val json = runCatching { JSONObject(bruto.trim()) }.getOrNull() ?: return false
        if (json.optBoolean("isError")) return true
        val text = textoUtilDoToolsCall(bruto).lowercase()
        return text.contains("invalid input") || text.contains("invalid option") ||
            text.contains("\"iserror\":true")
    }

    private fun registrarDiag(msg: String) {
        Log.e(TAG, msg)
        runCatching {
            File(context.cacheDir, "higgsfield-mcp-diag.txt").appendText(
                "${System.currentTimeMillis()} $msg\n",
            )
        }
    }

    private fun esperarUrlDoJob(jobId: String): String? {
        // Server: job_status com sync:true (texto-only); jobs_wait com jobs:[id].
        val limite = System.currentTimeMillis() + TIMEOUT_MS
        while (System.currentTimeMillis() < limite) {
            val status = runCatching {
                toolsCall(
                    "job_status",
                    JSONObject()
                        .put("jobId", jobId)
                        .put("sync", true),
                )
            }.getOrNull().orEmpty()
            if (status.isNotBlank()) {
                registrarDiag("job_status=$status")
                if (!ehErroDeFerramenta(status)) {
                    val regras = com.chromebrothers.navegador.regras.ExtracaoDaUrlDoResultadoDoJobHiggsfield
                    if (regras.jobFalhou(status)) return null
                    // Não baixar URL de media_input enquanto in_progress — era a referência.
                    if (regras.jobConcluido(status)) {
                        regras.urlDoResultado(status)?.let { return it }
                        val display = runCatching {
                            toolsCall(
                                "show_generation_by_ids",
                                JSONObject().put(
                                    "jobs",
                                    JSONArray().put(
                                        JSONObject().put("index", 0).put("job_id", jobId),
                                    ),
                                ),
                            )
                        }.getOrNull().orEmpty().ifBlank {
                            runCatching {
                                toolsCall("job_display", JSONObject().put("jobId", jobId))
                            }.getOrNull().orEmpty()
                        }
                        regras.urlDoResultado(display)?.let { return it }
                    }
                }
            }
            val waitArgs = JSONObject()
                .put(
                    "jobs",
                    JSONArray().put(
                        JSONObject().put("index", 0).put("job_id", jobId),
                    ),
                )
                .put("timeout_seconds", 15)
            val waitBruto = runCatching { toolsCall("jobs_wait", waitArgs) }.getOrNull().orEmpty()
            if (waitBruto.isNotBlank()) {
                registrarDiag("jobs_wait=$waitBruto")
                if (!ehErroDeFerramenta(waitBruto)) {
                    val regras = com.chromebrothers.navegador.regras.ExtracaoDaUrlDoResultadoDoJobHiggsfield
                    if (regras.jobFalhou(waitBruto)) return null
                    regras.urlDoResultado(waitBruto)?.let { return it }
                }
            }
            Thread.sleep(2_000L)
        }
        return null
    }

    private fun extrairJobId(bruto: String): String? {
        val uuid =
            Regex("""[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}""", RegexOption.IGNORE_CASE)
        val json = bruto.trim().takeIf { it.startsWith("{") }?.let { runCatching { JSONObject(it) }.getOrNull() }
        if (json != null) {
            json.optString("job_id").takeIf { it.isNotBlank() }?.let { return it }
            json.optString("jobId").takeIf { it.isNotBlank() }?.let { return it }
            val structured = json.optJSONObject("structuredContent")
            if (structured != null) {
                structured.optString("job_id").takeIf { it.isNotBlank() }?.let { return it }
                structured.optJSONArray("results")?.optJSONObject(0)?.optString("id")
                    ?.takeIf { it.isNotBlank() }?.let { return it }
            }
            val content = json.optJSONArray("content")
            if (content != null) {
                for (i in 0 until content.length()) {
                    val item = content.optJSONObject(i) ?: continue
                    val text = item.optString("text")
                    Regex(""""?job_id"?\s*[:=]\s*"([^"]+)"""").find(text)?.groupValues?.getOrNull(1)?.let { return it }
                    uuid.find(text)?.value?.let { return it }
                }
            }
        }
        return Regex(""""?job_id"?\s*[:=]\s*"([^"]+)"""").find(bruto)?.groupValues?.getOrNull(1)
            ?: uuid.find(bruto)?.value
    }

    private fun extrairUrlHttp(bruto: String): String? =
        com.chromebrothers.navegador.regras.ExtracaoDaUrlDoResultadoDoJobHiggsfield.urlDoResultado(bruto)

    private fun baixarBytes(url: String): ByteArray? {
        val req = Request.Builder().url(url).get().build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            return resp.body?.bytes()
        }
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
            if (!resp.isSuccessful) error("Registro OAuth: HTTP ${resp.code} $texto")
            val id = JSONObject(texto).optString("client_id")
            if (id.isBlank()) error("Registro OAuth sem client_id.")
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
            if (!resp.isSuccessful) error("Token: HTTP ${resp.code} $texto")
            val json = JSONObject(texto)
            val access = json.optString("access_token")
            val refresh = json.optString("refresh_token")
            if (access.isBlank()) error("Token sem access_token.")
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
        if (token.isBlank()) error("Sem token Higgsfield.")
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
            if (mcpSessionId.isBlank()) {
                val nomes = resp.headers.names().joinToString(",")
                registrarDiag("MCP headers=$nomes")
            }
            val texto = resp.body?.string().orEmpty()
            registrarDiag("MCP ${resp.code} session=$mcpSessionId body=${texto.take(800)}")
            if (resp.code == 401) {
                refreshSePreciso()
                error("Sessão Higgsfield expirada. Ligue de novo na engrenagem.")
            }
            if (!resp.isSuccessful) error("MCP HTTP ${resp.code}: $texto")
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

    private val travaMcp = Any()

    companion object {
        private const val TAG = "HiggsfieldCB"
        private const val LIMITE_DE_PAGINAS_DO_CATALOGO = 20
        private const val TAMANHO_DA_PAGINA_DO_CATALOGO = 50
        const val MCP = "https://mcp.higgsfield.ai/mcp"
        const val REGISTER = "https://mcp.higgsfield.ai/oauth2/register"
        const val AUTHORIZE = "https://mcp.higgsfield.ai/oauth2/authorize"
        const val TOKEN = "https://mcp.higgsfield.ai/oauth2/token"
        const val REDIRECT = "http://127.0.0.1:8787/callback"
        const val TIMEOUT_MS = 10 * 60 * 1000L
    }
}

sealed class ResultadoDaGeracaoMcpHiggsfield {
    data class Pronto(
        val bytes: ByteArray,
        val enderecoRemoto: String,
        val jobId: String,
        val imagem: Boolean,
    ) : ResultadoDaGeracaoMcpHiggsfield()

    data class Erro(val mensagem: String) : ResultadoDaGeracaoMcpHiggsfield()
}

sealed class ResultadoDaCriacaoDeJobHiggsfield {
    data class Criado(val jobId: String) : ResultadoDaCriacaoDeJobHiggsfield()
    data class Erro(val mensagem: String) : ResultadoDaCriacaoDeJobHiggsfield()
}

sealed class ResultadoDoUploadDeReferenciasHiggsfield {
    data class Ok(val mediaIds: List<Pair<String, String>>) : ResultadoDoUploadDeReferenciasHiggsfield()
    data class Erro(val mensagem: String) : ResultadoDoUploadDeReferenciasHiggsfield()
}

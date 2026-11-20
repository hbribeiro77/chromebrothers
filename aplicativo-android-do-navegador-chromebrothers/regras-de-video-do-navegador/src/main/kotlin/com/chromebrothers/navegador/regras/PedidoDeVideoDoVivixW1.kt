package com.chromebrothers.navegador.regras

import java.util.Locale

/**
 * Vivix W1 (API REST `https://api.vivix.ai`, token Bearer) — world video generation.
 *
 * Contrato confirmado em 2026-11-15 (doc + teste com a chave):
 * - `POST /v1/videos/jobs` com `{model, pe_mode, jobs:[{prompt, duration, aspect_ratio, resolution, ...}]}`
 *   (exatamente 1 job; campo desconhecido → 20004).
 * - `jobs[0].resolution` não está na doc, mas a API responde
 *   "must be 720p or 1088p" — é o que o playground chama de 720/1080.
 * - Duração 5–15 s; proporções 16:9, 9:16, 1:1, 4:3, 3:4; prompt obrigatório.
 * - Referências só por URL de upload (`/v1/files/images|audios|videos`): até 8 imagens (10 MB),
 *   4 vídeos (20 MB, ≤ 15 s cada, ≤ 20 s somados; mp4/mov/webm).
 * - first/last frame não combinam com referências (fora do escopo aqui).
 */
object PedidoDeVideoDoVivixW1 {
    const val ORIGEM_DA_API = "https://api.vivix.ai"

    const val DURACAO_MINIMA = 5
    const val DURACAO_MAXIMA = 15
    const val DURACAO_PADRAO = 5

    const val RESOLUCAO_PADRAO = "720p"
    val RESOLUCOES_DA_API = setOf("720p", "1088p")

    val PROPORCOES_ACEITAS = setOf("16:9", "9:16", "1:1", "4:3", "3:4")
    const val PROPORCAO_PADRAO = "16:9"

    const val LIMITE_DE_IMAGENS = 8
    const val TAMANHO_MAXIMO_DA_IMAGEM = 10L * 1024 * 1024
    const val LIMITE_DE_VIDEOS = 4
    const val TAMANHO_MAXIMO_DO_VIDEO = 20L * 1024 * 1024
    const val DURACAO_MAXIMA_DE_CADA_VIDEO_MS = 15_000L
    const val DURACAO_MAXIMA_DOS_VIDEOS_SOMADOS_MS = 20_000L

    const val LIMITE_DO_PROMPT = 20_000

    private val regexDoJobId = Regex("^job_[A-Za-z0-9][A-Za-z0-9_-]{2,78}$")

    data class Ajustado(val duracao: Int, val resolucao: String, val proporcao: String)

    /**
     * Encaixa os valores genéricos do componente no que o Vivix aceita:
     * 480p/720p → 720p, 1080p → 1088p; duração presa em 5–15; `adaptive` → 16:9.
     */
    fun ajustar(duracao: Int, resolucaoDaUi: String, proporcao: String): Ajustado =
        Ajustado(
            duracao = duracao.coerceIn(DURACAO_MINIMA, DURACAO_MAXIMA),
            resolucao = resolucaoParaApi(resolucaoDaUi),
            proporcao = proporcao.trim().takeIf { it in PROPORCOES_ACEITAS } ?: PROPORCAO_PADRAO,
        )

    fun resolucaoParaApi(resolucaoDaUi: String): String =
        when (resolucaoDaUi.trim().lowercase(Locale.ROOT)) {
            "1080p", "1088p", "1080" -> "1088p"
            else -> RESOLUCAO_PADRAO
        }

    /** Rótulo para a pessoa (a API usa 1088p, mas o que se espera ver é "1080p"). */
    fun rotuloDaResolucao(resolucaoDaApi: String): String =
        if (resolucaoDaApi == "1088p") "1080p" else "720p"

    /** Corpo JSON do `POST /v1/videos/jobs`. Só inclui campos que a API conhece. */
    fun corpoDoJob(
        modelo: String,
        prompt: String,
        ajustado: Ajustado,
        urlsDeImagem: List<String> = emptyList(),
        urlsDeVideo: List<String> = emptyList(),
    ): String {
        val job = buildList {
            add(""""prompt":"${TextoJsonDoChat.escapar(prompt.trim())}"""")
            add(""""duration":${ajustado.duracao}""")
            add(""""aspect_ratio":"${ajustado.proporcao}"""")
            add(""""resolution":"${ajustado.resolucao}"""")
            add(""""output_format":"mp4"""")
            if (urlsDeImagem.isNotEmpty()) add(""""ref_images":${listaDeUrls(urlsDeImagem.take(LIMITE_DE_IMAGENS))}""")
            if (urlsDeVideo.isNotEmpty()) add(""""ref_videos":${listaDeUrls(urlsDeVideo.take(LIMITE_DE_VIDEOS))}""")
        }.joinToString(",")
        val m = CatalogoDeModelosDeVideoDoVivixNaColecao.idDaApi(modelo)
        return """{"model":"$m","pe_mode":"standard","jobs":[{$job}]}"""
    }

    private fun listaDeUrls(urls: List<String>): String =
        urls.joinToString(",", "[", "]") { """{"url":"${TextoJsonDoChat.escapar(it)}"}""" }

    fun ehJobId(jobId: String): Boolean = regexDoJobId.matches(jobId)

    /**
     * Confere os vídeos de referência antes de gastar crédito.
     * [tamanhosBytes] e [duracoesMs] na mesma ordem.
     */
    fun validarVideos(tamanhosBytes: List<Long>, duracoesMs: List<Long>): String? {
        if (tamanhosBytes.isEmpty()) return null
        if (tamanhosBytes.size > LIMITE_DE_VIDEOS) return "Vivix: no máximo $LIMITE_DE_VIDEOS vídeos de referência."
        if (tamanhosBytes.any { it > TAMANHO_MAXIMO_DO_VIDEO }) {
            return "Vivix: cada vídeo de referência pode ter no máximo 20 MB."
        }
        if (duracoesMs.any { it > DURACAO_MAXIMA_DE_CADA_VIDEO_MS }) {
            return "Vivix: cada vídeo de referência pode ter no máximo 15 s."
        }
        if (duracoesMs.sum() > DURACAO_MAXIMA_DOS_VIDEOS_SOMADOS_MS) {
            return "Vivix: os vídeos de referência somam mais de 20 s."
        }
        return null
    }

    fun validarImagens(tamanhosBytes: List<Long>): String? {
        if (tamanhosBytes.size > LIMITE_DE_IMAGENS) return "Vivix: no máximo $LIMITE_DE_IMAGENS imagens de referência."
        if (tamanhosBytes.any { it > TAMANHO_MAXIMO_DA_IMAGEM }) {
            return "Vivix: cada imagem de referência pode ter no máximo 10 MB."
        }
        return null
    }

    /**
     * Créditos W estimados (preço publicado em 2026-11, com promoção):
     * base por segundo + taxa de imagens (5 créditos a cada bloco de 5) + 20 créditos por segundo de vídeo.
     */
    fun creditosEstimados(modelo: String, duracao: Int, quantidadeDeImagens: Int = 0, segundosDeVideo: Double = 0.0): Double {
        val porSegundo = CatalogoDeModelosDeVideoDoVivixNaColecao.creditosPorSegundo(modelo)
        val imagens = if (quantidadeDeImagens <= 0) 0.0 else 5.0 * ((quantidadeDeImagens + 4) / 5)
        val videos = 20.0 * segundosDeVideo
        return porSegundo * duracao + imagens + videos
    }

    fun rotuloDosCreditos(creditos: Double): String =
        "≈ " + String.format(Locale.ROOT, "%.0f", kotlin.math.ceil(creditos)) + " créditos W"

    /** Situação de um job, a partir dos campos `status` e `error.code` do `GET /v1/videos/jobs/{id}`. */
    fun situacaoDoJob(status: String): SituacaoDoJobVivix = when (status.trim().lowercase(Locale.ROOT)) {
        "succeeded" -> SituacaoDoJobVivix.PRONTO
        "failed", "timeout" -> SituacaoDoJobVivix.FALHOU
        "queued", "processing", "" -> SituacaoDoJobVivix.GERANDO
        else -> SituacaoDoJobVivix.GERANDO
    }

    fun mensagemDaFalhaDoJob(codigo: String, mensagem: String): String = when (codigo.trim()) {
        "generation_cancelled" -> "A Vivix cancelou a geração."
        "generation_timeout" -> "A geração na Vivix passou do tempo."
        else -> mensagem.trim().ifBlank { "A geração na Vivix não foi concluída." }
    }

    /** Mensagem para a pessoa a partir do HTTP e do `code` do envelope `{code, message, data}`. */
    fun mensagemDoErro(httpStatus: Int, codigo: Int?, mensagemDaApi: String = ""): String = when {
        codigo == 10001 || httpStatus == 401 -> "Token Vivix ausente ou inválido. Confira na engrenagem → Vivix."
        codigo == 10003 -> "A Vivix recusou o token. Confira na engrenagem → Vivix."
        codigo == 10004 || codigo == 10005 || httpStatus == 402 ->
            "Créditos W insuficientes ou cobrança da Vivix inativa."
        codigo == 30001 -> "A moderação da Vivix recusou o pedido."
        codigo == 20008 || httpStatus == 415 -> "A Vivix não aceitou o tipo desse arquivo."
        codigo == 20004 || httpStatus == 400 ->
            "A Vivix recusou os parâmetros" + mensagemDaApi.trim().takeIf { it.isNotEmpty() }?.let { ": $it" }.orEmpty()
        codigo == 20005 || httpStatus == 404 -> "Pedido não encontrado nesta conta Vivix."
        codigo == 10008 || codigo == 30008 || httpStatus == 429 -> "A Vivix pediu para tentar de novo mais tarde."
        httpStatus >= 500 || codigo == 50001 || codigo == 50003 -> "Erro temporário na Vivix. O envio não foi repetido."
        else -> "A Vivix recusou o pedido" + mensagemDaApi.trim().takeIf { it.isNotEmpty() }?.let { ": $it" }.orEmpty()
    }
}

enum class SituacaoDoJobVivix { GERANDO, PRONTO, FALHOU }

/** Modelos W1 (o `vivix-w1-auto` existe na doc, mas não está liberado na conta testada). */
object CatalogoDeModelosDeVideoDoVivixNaColecao {
    data class Modelo(val id: String, val nome: String, val creditosPorSegundo: Double)

    val MODELOS: List<Modelo> = listOf(
        Modelo("vivix-w1", "Vivix W1", 1.7),
        Modelo("vivix-w1-fast", "Vivix W1 Fast", 5.3),
        Modelo("vivix-w1-ultrafast", "Vivix W1 Ultra Fast", 9.3),
    )

    const val MODELO_PADRAO = "vivix-w1"

    fun modelo(idOuNome: String): Modelo? {
        val bruto = idOuNome.trim()
        if (bruto.isEmpty()) return null
        val normal = bruto.lowercase(Locale.ROOT).replace('_', '-').replace(' ', '-')
        return MODELOS.firstOrNull { it.id == normal || it.nome.equals(bruto, ignoreCase = true) }
            ?: when (normal) {
                "w1", "vivix" -> MODELOS[0]
                "fast", "w1-fast", "vivix-fast" -> MODELOS[1]
                "ultrafast", "ultra-fast", "w1-ultrafast", "vivix-w1-ultra-fast" -> MODELOS[2]
                else -> null
            }
    }

    fun ehModeloVivix(idOuNome: String): Boolean = modelo(idOuNome) != null

    fun idDaApi(idOuNome: String): String = modelo(idOuNome)?.id ?: MODELO_PADRAO

    fun nome(idOuNome: String): String = modelo(idOuNome)?.nome ?: MODELOS[0].nome

    fun creditosPorSegundo(idOuNome: String): Double = (modelo(idOuNome) ?: MODELOS[0]).creditosPorSegundo

    fun jsonDosItensParaPonte(): String =
        MODELOS.joinToString(",", """{"ok":true,"itens":[""", "]}") {
            val rotulo = "${it.nome} · ≈${String.format(Locale.ROOT, "%.1f", it.creditosPorSegundo).replace('.', ',')} créd/s"
            """{"id":"${it.id}","nome":"${TextoJsonDoChat.escapar(rotulo)}"}"""
        }
}

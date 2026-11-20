package com.chromebrothers.navegador.regras

/**
 * Vídeo como referência no gerador MCP de vídeo.
 *
 * Quem aceita (confirmado nos schemas/documentação de cada provedor em 2026-11):
 * - Higgsfield: `medias[].role = video_references` em Wan 3.0 (+Prime), Seedance 2.0 (+Mini),
 *   Seedance 2.5 (+Flash) e Kling 4.0 (+Flash).
 * - Sjinn: `video_urls` em Wan 3.0, Seedance 2.5 e Kling 4 (omni); `media_urls` no Seedance 2.0.
 * - Topview: `omni_reference` + `inputVideos` (prompt com `<<<VideoN>>>`).
 * - Vivix W1: `ref_videos[].url` em todos os modelos W1 (≤ 15 s cada, ≤ 20 s somados).
 */
object ReferenciaDeVideoNaGeracaoMcp {
    const val LIMITE_DE_VIDEOS = 3
    const val TAMANHO_MAXIMO_BYTES = 200L * 1024 * 1024
    const val MODELOS_ACEITOS_TEXTO = "Wan 3.0, Wan 3.0 Prime, Seedance 2.0, Seedance 2.5, Kling 4.0 e Vivix W1"

    private val H = PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_HIGGSFIELD
    private val S = PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_SJINN
    private val T = PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_TOPVIEW
    private val M = PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_MAGNIFIC
    private val V = PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_VIVIX

    private val ACEITOS: Map<String, Set<String>> = mapOf(
        H to setOf(
            "wan3_0", "wan3_0_prime",
            "seedance_2_0", "seedance_2_0_mini",
            "seedance_2_5", "seedance_2_5_flash",
            "kling4_0", "kling_4_0_flash",
        ),
        S to setOf("wan3", "seedance2.5", "seedance2.0", "seedance2.0-fast", "seedance2.0-mini", "kling4"),
        T to setOf(
            "wan-3-0", "wan-3-0-prime", "seedance-2-0", "seedance-2-0-fast", "seedance-2-5", "kling-4-0",
            // O Topview recebe o display name no pedido.
            "wan 3.0", "wan 3.0 prime", "seedance 2.0", "seedance 2.0 fast", "seedance 2.0 mini", "seedance 2.5",
            "kling 4.0",
        ),
        // Magnific: `references[].type = video` (confirmado na lista de modelos deles).
        M to setOf(
            "wan-3-0", "wan-3-0-prime",
            "bytedance-seedance-pro-2.0", "bytedance-seedance-fast-2.0", "bytedance-seedance-mini-2.0",
            "bytedance-seedance-pro-2.5",
            "kling-40-omni", "kling-40-flash",
        ),
        V to CatalogoDeModelosDeVideoDoVivixNaColecao.MODELOS.map { it.id }.toSet(),
    )

    /** Id local que o [mcp] usa para [modelo] (que pode ser família canônica ou id do provedor). */
    fun idLocal(mcp: String, modelo: String): String {
        val m = mcp.trim().lowercase()
        val bruto = modelo.trim()
        return EquivalenciaDeModelosDeVideoEntreMcps.familiaPorId(bruto)?.idsPorMcp?.get(m) ?: bruto
    }

    fun aceita(mcp: String, modelo: String): Boolean {
        val m = mcp.trim().lowercase()
        val local = idLocal(m, modelo).lowercase()
        if (m == V) return CatalogoDeModelosDeVideoDoVivixNaColecao.ehModeloVivix(local)
        return local in ACEITOS[m].orEmpty()
    }

    fun aceitaEmTodos(mcps: List<String>, modelo: String): Boolean =
        mcps.isNotEmpty() && mcps.all { aceita(it, modelo) }

    private fun ehKling4(mcp: String, modelo: String): Boolean {
        val local = idLocal(mcp, modelo).lowercase().replace('_', '-').replace(' ', '-')
        return local.contains("kling") && local.contains("4")
    }

    private data class Limites(val minimoMs: Long, val cadaMaxMs: Long, val totalMaxMs: Long)

    /** Wan/Seedance: cada ≥ 2 s, soma ≤ 15 s. Kling 4: cada ≥ 3 s, soma ≤ 30 s. Vivix: cada ≤ 15 s, soma ≤ 20 s. */
    private fun limitesDe(mcp: String, modelo: String): Limites = when {
        mcp.trim().lowercase() == V -> Limites(0L, 15_000L, 20_000L)
        ehKling4(mcp, modelo) -> Limites(3_000L, 30_000L, 30_000L)
        else -> Limites(2_000L, 15_000L, 15_000L)
    }

    /** Com vários MCPs marcados, vale o limite mais restrito de cada um. */
    private fun limitesComuns(mcps: List<String>, modelo: String): Limites {
        val todos = mcps.map { limitesDe(it, modelo) }
        return Limites(
            minimoMs = todos.maxOf { it.minimoMs },
            cadaMaxMs = todos.minOf { it.cadaMaxMs },
            totalMaxMs = todos.minOf { it.totalMaxMs },
        )
    }

    /** Valida os vídeos antes de gastar crédito. [duracoesMs] na ordem das referências. */
    fun validar(duracoesMs: List<Long>, mcps: List<String>, modelo: String): String? {
        if (duracoesMs.isEmpty()) return null
        if (!aceitaEmTodos(mcps, modelo)) {
            return "Este modelo não aceita vídeo de referência. Use $MODELOS_ACEITOS_TEXTO, ou tire os vídeos."
        }
        if (duracoesMs.size > LIMITE_DE_VIDEOS) return "No máximo $LIMITE_DE_VIDEOS vídeos de referência."
        val (minimoMs, cadaMaxMs, totalMaxMs) = limitesComuns(mcps, modelo)
        if (minimoMs > 0 && duracoesMs.any { it in 1 until minimoMs }) {
            return "Cada vídeo de referência precisa ter pelo menos ${minimoMs / 1000} s."
        }
        if (cadaMaxMs < totalMaxMs && duracoesMs.any { it > cadaMaxMs }) {
            return "Cada vídeo de referência pode ter no máximo ${cadaMaxMs / 1000} s."
        }
        val total = duracoesMs.sum()
        if (total > totalMaxMs) {
            return "Os vídeos de referência somam ${segundos(total)} s; o limite deste modelo é ${totalMaxMs / 1000} s."
        }
        return null
    }

    /** Parâmetros extras do Higgsfield quando há vídeo de referência. */
    fun parametrosExtrasDoHiggsfield(modelo: String, temVideo: Boolean): Map<String, String> {
        if (!temVideo) return emptyMap()
        val id = modelo.trim().lowercase()
        return when {
            id.startsWith("seedance_2_5") -> mapOf("mode" to "omni_reference")
            id == "kling4_0" || id == "kling_4_0_flash" -> mapOf("mode" to "reference-to-video")
            else -> emptyMap()
        }
    }

    /** Dica curta para o componente sob as referências. */
    fun dica(mcps: List<String>, modelo: String): String =
        if (aceitaEmTodos(mcps, modelo)) {
            val total = limitesComuns(mcps, modelo).totalMaxMs / 1000
            "Aceita vídeo de referência (até $LIMITE_DE_VIDEOS, total ≤ $total s)."
        } else {
            "Vídeo de referência: só em $MODELOS_ACEITOS_TEXTO."
        }

    fun duracaoCurta(ms: Long): String {
        val total = (ms / 1000).coerceAtLeast(0)
        return if (total < 60) "${total}s" else "${total / 60}:${(total % 60).toString().padStart(2, '0')}"
    }

    private fun segundos(ms: Long): String {
        val s = ms / 1000.0
        return if (s == s.toLong().toDouble()) s.toLong().toString() else String.format(java.util.Locale.ROOT, "%.1f", s)
    }
}

/**
 * Monta o `input` do `create_tool_task` de vídeo do Sjinn conforme a ferramenta
 * (cada uma tem nomes de campo próprios — ver mcp.sjinn.ai/docs/api/tool/...).
 */
object MontagemDoPedidoDeVideoDoSjinn {
    fun montar(
        toolType: String,
        modelo: String,
        prompt: String,
        proporcao: String,
        resolucaoDaUi: String,
        duracao: Int,
        imageUrls: List<String>,
        videoUrls: List<String>,
    ): Map<String, Any> {
        val saida = linkedMapOf<String, Any>()
        val res = resolucaoDaUi.trim().lowercase().ifBlank { "480p" }
        val ratio = proporcao.trim().ifBlank { "16:9" }
        when (toolType) {
            "seedance20-video-api" -> {
                if (prompt.isNotBlank()) saida["prompt"] = prompt
                saida["duration"] = duracao.coerceIn(4, 15)
                saida["aspect_ratio"] = if (ratio == "adaptive") "16:9" else ratio
                saida["resolution"] = res
                saida["mode"] = when {
                    modelo.endsWith("-fast", true) -> "fast"
                    modelo.endsWith("-mini", true) -> "mini"
                    else -> "pro"
                }
                val midias = imageUrls + videoUrls
                if (midias.isNotEmpty()) saida["media_urls"] = midias
            }
            "seedance25-video-api" -> {
                saida["prompt"] = prompt
                saida["duration"] = duracao.coerceIn(4, 30)
                saida["ratio"] = ratio
                saida["resolution"] = res
                if (imageUrls.isNotEmpty()) saida["image_urls"] = imageUrls
                if (videoUrls.isNotEmpty()) saida["video_urls"] = videoUrls
            }
            "kling4-omni-video-api" -> {
                saida["prompt"] = prompt
                saida["duration"] = duracao.coerceIn(3, 30)
                saida["resolution"] = if (res == "1080p") "1080p" else "720p"
                saida["aspect_ratio"] = if (ratio == "adaptive") "auto" else ratio
                if (imageUrls.isNotEmpty()) saida["image_urls"] = imageUrls
                if (videoUrls.isNotEmpty()) saida["video_urls"] = videoUrls
            }
            else -> {
                // Formato que já era usado (Wan 3 e demais ferramentas antigas).
                saida["prompt"] = prompt
                saida["aspect_ratio"] = ratio
                saida["resolution"] = PedidoDeGeracaoDeVideoViaMcpNaColecao.resolucaoParaApi(res)
                saida["duration"] = duracao.coerceIn(
                    PedidoDeGeracaoDeVideoViaMcpNaColecao.DURACAO_MINIMA,
                    PedidoDeGeracaoDeVideoViaMcpNaColecao.DURACAO_MAXIMA,
                )
                if (imageUrls.isNotEmpty()) saida["image_urls"] = imageUrls
                if (videoUrls.isNotEmpty()) saida["video_urls"] = videoUrls
            }
        }
        return saida
    }
}

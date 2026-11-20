package com.chromebrothers.navegador.regras

/**
 * Monta o `req` de `topview_generate_video`.
 * Com imagens de referência usa **omni_reference** + `inputImages`
 * (não first-frame de `image_to_video`).
 */
object MontagemDoPedidoDeVideoTopviewComReferenciasOmni {
    const val TASK_TEXT_TO_VIDEO = "text_to_video"
    const val TASK_OMNI_REFERENCE = "omni_reference"

    data class ImagemDeEntrada(val fileId: String, val name: String)

    data class Pedido(
        val taskType: String,
        val model: String,
        val prompt: String?,
        val aspectRatio: String?,
        val resolution: Int,
        val duration: Int,
        val firstFrameFileId: String? = null,
        val referenceImageFileIds: List<String> = emptyList(),
        val inputImages: List<ImagemDeEntrada> = emptyList(),
        /** omni_reference: vídeos de referência (`inputVideos`, citados como `<<<VideoN>>>`). */
        val inputVideos: List<ImagemDeEntrada> = emptyList(),
    )

    fun montar(
        modeloDisplay: String,
        prompt: String,
        aspectRatio: String,
        resolution: Int,
        duration: Int,
        fileIdsDasReferencias: List<String>,
        fileIdsDosVideos: List<String> = emptyList(),
    ): Pedido {
        val fileIds = fileIdsDasReferencias.map { it.trim() }.filter { it.isNotEmpty() }.take(10)
        val videoIds = fileIdsDosVideos.map { it.trim() }.filter { it.isNotEmpty() }.take(ReferenciaDeVideoNaGeracaoMcp.LIMITE_DE_VIDEOS)
        val ratio = aspectRatio.trim().takeIf { it.isNotEmpty() && !it.equals("adaptive", ignoreCase = true) }
        if (fileIds.isEmpty() && videoIds.isEmpty()) {
            return Pedido(
                taskType = TASK_TEXT_TO_VIDEO,
                model = modeloDisplay,
                prompt = prompt.takeIf { it.isNotBlank() },
                aspectRatio = ratio,
                resolution = resolution,
                duration = duration,
            )
        }
        val imagens = fileIds.mapIndexed { i, id ->
            ImagemDeEntrada(fileId = id, name = "Image${i + 1}")
        }
        val videos = videoIds.mapIndexed { i, id ->
            ImagemDeEntrada(fileId = id, name = "Video${i + 1}")
        }
        return Pedido(
            taskType = TASK_OMNI_REFERENCE,
            model = modeloDisplay,
            prompt = enriquecerPromptComMarcadores(prompt, imagens.size, videos.size),
            aspectRatio = ratio,
            resolution = resolution,
            duration = duration,
            inputImages = imagens,
            inputVideos = videos,
        )
    }

    fun enriquecerPromptComMarcadores(prompt: String, quantidadeDeImagens: Int, quantidadeDeVideos: Int = 0): String {
        val base = prompt.trim()
        val partes = mutableListOf<String>()
        if (quantidadeDeImagens > 0 && !base.contains("<<<Image")) {
            partes += (1..quantidadeDeImagens).map { "<<<Image$it>>>" }
        }
        if (quantidadeDeVideos > 0 && !base.contains("<<<Video")) {
            partes += (1..quantidadeDeVideos).map { "<<<Video$it>>>" }
        }
        if (partes.isEmpty()) return base
        val marcadores = partes.joinToString(" ")
        return if (base.isEmpty()) marcadores else "$base\n$marcadores"
    }
}

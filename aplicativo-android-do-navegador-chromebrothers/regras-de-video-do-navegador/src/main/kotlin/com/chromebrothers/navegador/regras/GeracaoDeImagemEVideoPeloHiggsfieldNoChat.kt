package com.chromebrothers.navegador.regras

sealed class ResultadoDaGeracaoHiggsfieldDoChat {
    data class ProntoImagem(
        val prompt: String,
        val modelo: String,
        val idImagem: String = "",
        val roleReferencia: String = "",
    ) : ResultadoDaGeracaoHiggsfieldDoChat()

    data class ProntoVideo(val prompt: String, val modelo: String) : ResultadoDaGeracaoHiggsfieldDoChat()
    data class Recusado(val mensagem: String) : ResultadoDaGeracaoHiggsfieldDoChat()
}

object GeracaoDeImagemEVideoPeloHiggsfieldNoChat {
    const val LIMITE_DO_PROMPT = 4000
    const val LIMITE_DO_MODELO = 120
    const val LIMITE_DO_ID_IMAGEM = 120
    const val LIMITE_DA_ROLE = 64
    /** id_imagem especial: usa a última imagem anexada pelo usuário nesta conversa. */
    const val ID_IMAGEM_ANEXO = "anexo"
    const val SEM_SESSAO = "Ligue o Higgsfield na engrenagem do chat."
    /** Default oficial nas instruções do MCP Higgsfield (2025-03). */
    const val MODELO_PADRAO_IMAGEM = "gpt_image_2_6"
    /** No catálogo Higgsfield, Wan 3.0 é o id `wan3_0` (não confundir com `wan2_7`). */
    const val MODELO_PADRAO_VIDEO = "wan3_0"

    fun ehReferenciaDeAnexoDoChat(idImagem: String): Boolean {
        val chave = idImagem.trim().lowercase()
        return chave == ID_IMAGEM_ANEXO || chave == "anexo_recente" || chave == "anexo do chat"
    }

    fun lerImagem(argumentos: String): ResultadoDaGeracaoHiggsfieldDoChat {
        return when (val lido = lerPromptModeloEReferencia(argumentos)) {
            is Lido.Recusado -> ResultadoDaGeracaoHiggsfieldDoChat.Recusado(lido.mensagem)
            is Lido.Ok -> {
                val modelo = resolverModeloImagem(lido.modelo)
                val role = if (lido.idImagem.isBlank()) {
                    ""
                } else {
                    resolverRoleReferencia(modelo, lido.role)
                }
                ResultadoDaGeracaoHiggsfieldDoChat.ProntoImagem(
                    prompt = lido.prompt,
                    modelo = modelo,
                    idImagem = lido.idImagem,
                    roleReferencia = role,
                )
            }
        }
    }

    fun lerVideo(argumentos: String): ResultadoDaGeracaoHiggsfieldDoChat {
        return when (val lido = lerPromptEModelo(argumentos)) {
            is Lido.Recusado -> ResultadoDaGeracaoHiggsfieldDoChat.Recusado(lido.mensagem)
            is Lido.Ok -> ResultadoDaGeracaoHiggsfieldDoChat.ProntoVideo(
                lido.prompt,
                resolverModeloVideo(lido.modelo),
            )
        }
    }

    fun resolverModeloImagem(modelo: String): String {
        val chave = modelo.trim().lowercase().replace('_', ' ').replace('-', ' ')
            .replace(Regex("\\s+"), " ")
        if (chave.isBlank()) return MODELO_PADRAO_IMAGEM
        return when {
            chave == "image auto" || chave == "image_auto" || chave == "auto" -> MODELO_PADRAO_IMAGEM
            chave.contains("nano banana pro") || chave == "nano_banana_pro" -> "nano_banana_pro"
            chave.contains("nano banana 2.1") || chave.contains("nanobana 2.1") ||
                chave.contains("nano banana flash") || chave == "nano_banana_flash" ||
                chave == "nano_banana_2_1" ->
                "nano_banana_2_1"
            chave.contains("nano banana 2 lite") || chave == "nano_banana_2_lite" -> "nano_banana_2_lite"
            chave == "nano banana 2" || chave == "nano_banana_2" || chave == "nano banana2" ->
                "nano_banana_2"
            chave.contains("nanobanana") || chave == "nano banana" || chave == "nano_banana" ->
                "nano_banana_2_1"
            else -> modelo.trim()
        }
    }

    fun resolverModeloVideo(modelo: String): String {
        val chave = modelo.trim().lowercase().replace('_', ' ').replace('-', ' ')
            .replace(Regex("\\s+"), " ")
        if (chave.isBlank()) return MODELO_PADRAO_VIDEO
        return when {
            chave.contains("wan 3.0 prime") || chave == "wan3 0 prime" || chave == "wan3_0_prime" ->
                "wan3_0_prime"
            chave.contains("wan 3") || chave == "wan3" || chave == "wan3 0" || chave == "wan3_0" ->
                "wan3_0"
            chave.contains("wan 2.7") || chave == "wan2 7" || chave == "wan2_7" -> "wan2_7"
            chave.contains("wan 2.6") || chave == "wan2 6" || chave == "wan2_6" -> "wan2_6"
            chave.contains("seedance") || chave == "seedance_2_0" -> "seedance_2_0"
            chave.contains("kling 3") || chave == "kling3" || chave == "kling3_0" -> "kling3_0"
            else -> modelo.trim()
        }
    }

    /**
     * Role MCP em params.medias[].role. Nano Banana 2.x / GPT Image 2.6 usam
     * `image_references`; a maioria dos outros aceita `image`.
     */
    fun resolverRoleReferencia(modelo: String, roleInformada: String): String {
        val informada = roleInformada.trim()
        if (informada.isNotEmpty()) return informada
        val id = resolverModeloImagem(modelo)
        return when {
            id.startsWith("nano_banana_2") || id == "gpt_image_2_6" || id == "gpt_image_2_6_flare" ||
                id == "gpt_image_2_5" ->
                "image_references"
            else -> "image"
        }
    }

    fun perguntaImagem(
        prompt: String,
        modelo: String,
        idImagem: String = "",
        roleReferencia: String = "",
    ): String {
        val linhaModelo = if (modelo.isBlank()) "Modelo: padrão" else "Modelo: $modelo"
        val ref = if (idImagem.isBlank()) {
            ""
        } else {
            val role = if (roleReferencia.isBlank()) "" else " ($roleReferencia)"
            val rotulo = if (ehReferenciaDeAnexoDoChat(idImagem)) "anexo do chat" else "id_imagem=$idImagem"
            "\nReferência: $rotulo$role"
        }
        return "Gerar imagem pelo Higgsfield?\n$linhaModelo$ref\n$prompt"
    }

    fun perguntaVideo(prompt: String, modelo: String): String {
        val linhaModelo = if (modelo.isBlank()) "Modelo: padrão" else "Modelo: $modelo"
        return "Gerar vídeo pelo Higgsfield?\n$linhaModelo\n$prompt"
    }

    fun sucessoImagem(idNaBiblioteca: String, prompt: String): String {
        return "Imagem gerada e salva na biblioteca (id $idNaBiblioteca). Prompt: $prompt"
    }

    fun sucessoVideo(idNaBiblioteca: String, prompt: String): String {
        return "Vídeo gerado e salvo na biblioteca (id $idNaBiblioteca). Prompt: $prompt"
    }

    private sealed class Lido {
        data class Ok(
            val prompt: String,
            val modelo: String,
            val idImagem: String = "",
            val role: String = "",
        ) : Lido()

        data class Recusado(val mensagem: String) : Lido()
    }

    private fun lerPromptEModelo(argumentos: String): Lido {
        val prompt = TextoJsonDoChat.valorDaChave(argumentos, "prompt")?.trim().orEmpty()
        val modelo = TextoJsonDoChat.valorDaChave(argumentos, "modelo")?.trim().orEmpty()
        return when {
            prompt.isEmpty() -> Lido.Recusado("Faltou o prompt.")
            prompt.length > LIMITE_DO_PROMPT -> Lido.Recusado("O prompt passou do limite.")
            modelo.length > LIMITE_DO_MODELO -> Lido.Recusado("O modelo passou do limite.")
            else -> Lido.Ok(prompt, modelo)
        }
    }

    private fun lerPromptModeloEReferencia(argumentos: String): Lido {
        val base = lerPromptEModelo(argumentos)
        if (base is Lido.Recusado) return base
        base as Lido.Ok
        val idImagem = (
            TextoJsonDoChat.valorDaChave(argumentos, "id_imagem")
                ?: TextoJsonDoChat.valorDaChave(argumentos, "imagem_id")
            )?.trim().orEmpty()
        val role = TextoJsonDoChat.valorDaChave(argumentos, "role_referencia")?.trim().orEmpty()
        return when {
            idImagem.length > LIMITE_DO_ID_IMAGEM -> Lido.Recusado("O id da imagem passou do limite.")
            role.length > LIMITE_DA_ROLE -> Lido.Recusado("A role da referência passou do limite.")
            else -> Lido.Ok(base.prompt, base.modelo, idImagem, role)
        }
    }
}

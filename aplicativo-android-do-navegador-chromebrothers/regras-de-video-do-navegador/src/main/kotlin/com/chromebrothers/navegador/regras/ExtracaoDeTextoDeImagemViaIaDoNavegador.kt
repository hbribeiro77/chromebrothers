package com.chromebrothers.navegador.regras

object ExtracaoDeTextoDeImagemViaIaDoNavegador {
    const val SEM_TEXTO = "Não encontrei texto nesta imagem."
    const val FALHA_GENERICA = "Não deu para extrair o texto. Tente de novo."
    const val FALHA_MODELO_SEM_VISAO =
        "Este modelo não lê imagem. Troque o modelo no chat e tente de novo."

    fun promptDoUsuario(): String =
        "Extraia todo o texto visível nesta imagem. Devolva somente o texto literal, " +
            "sem comentários, sem markdown e sem introdução."

    fun limparResposta(bruto: String): String {
        var t = bruto.trim()
        if (!t.startsWith("```")) return t
        t = t.removePrefix("```")
        val fimDaLinhaDaCerca = t.indexOf('\n')
        if (fimDaLinhaDaCerca >= 0) {
            t = t.substring(fimDaLinhaDaCerca + 1)
        }
        t = t.trimEnd()
        if (t.endsWith("```")) t = t.removeSuffix("```")
        return t.trim()
    }

    fun textoParaExibir(bruto: String): String {
        val limpo = limparResposta(bruto)
        return limpo.ifBlank { SEM_TEXTO }
    }

    fun botoesDeAcaoHabilitados(textoExibido: String): Boolean =
        textoExibido.isNotBlank() && textoExibido != SEM_TEXTO

    fun mensagemDeFalha(erroApi: String?): String {
        val e = erroApi.orEmpty().lowercase()
        val mencionaImagemOuVisao = e.contains("image") || e.contains("vision")
        val semVisao =
            e.contains("vision") ||
                e.contains("multimodal") ||
                e.contains("image_url") ||
                e.contains("image input") ||
                e.contains("support image") ||
                e.contains("supports image") ||
                ((e.contains("does not support") || e.contains("não suport")) && mencionaImagemOuVisao)
        if (e.isNotBlank() && semVisao) return FALHA_MODELO_SEM_VISAO
        return FALHA_GENERICA
    }

    fun mostrarAcao(temChave: Boolean): Boolean = temChave
}

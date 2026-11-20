package com.chromebrothers.navegador.regras

/**
 * Limites e textos da captura da área visível enviada ao chat.
 */
object PreparacaoDaCapturaDaAbaParaOChat {
    const val LADO_MAXIMO = 1280
    const val QUALIDADE_JPEG = 75

    fun medidasReduzidas(largura: Int, altura: Int): Pair<Int, Int> {
        val w = largura.coerceAtLeast(1)
        val h = altura.coerceAtLeast(1)
        val maior = maxOf(w, h)
        if (maior <= LADO_MAXIMO) return w to h
        val escala = LADO_MAXIMO.toFloat() / maior.toFloat()
        return (w * escala).toInt().coerceAtLeast(1) to (h * escala).toInt().coerceAtLeast(1)
    }

    fun relato(largura: Int, altura: Int, enderecoArquivo: String = ""): String {
        val base = "Capturei a área visível da aba (${largura}x${altura})."
        val arquivo = enderecoArquivo.trim()
        return if (arquivo.isEmpty()) {
            base
        } else {
            "$base Use este endereço na página ou na coleção: $arquivo"
        }
    }

    fun mensagemComAImagem(): String = "Segue a captura da área visível da aba."

    fun relatoDoComponente(largura: Int, altura: Int, enderecoArquivo: String = ""): String {
        val base = "Capturei o componente (${largura}x${altura})."
        val arquivo = enderecoArquivo.trim()
        return if (arquivo.isEmpty()) {
            base
        } else {
            "$base Use este endereço na coleção: $arquivo"
        }
    }

    fun mensagemComAImagemDoComponente(): String = "Segue a captura do componente."
}

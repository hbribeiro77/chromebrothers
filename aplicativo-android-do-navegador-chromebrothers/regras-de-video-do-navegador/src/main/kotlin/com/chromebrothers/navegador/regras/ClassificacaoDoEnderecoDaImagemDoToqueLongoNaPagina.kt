package com.chromebrothers.navegador.regras

import java.net.URI

enum class FonteDoEnderecoDaImagemDoToqueLongo {
    DATA,
    BLOB,
    HTTP,
    ARQUIVO_LOCAL,
    INSUPORTADO,
}

/**
 * Decide como tratar o src da imagem no toque longo — inclusive captura nativa em file://.
 */
object ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina {
    fun classificar(enderecoBruto: String): FonteDoEnderecoDaImagemDoToqueLongo {
        val endereco = enderecoBruto.trim()
        return when {
            endereco.startsWith("data:") -> FonteDoEnderecoDaImagemDoToqueLongo.DATA
            endereco.startsWith("blob:") -> FonteDoEnderecoDaImagemDoToqueLongo.BLOB
            endereco.startsWith("http://") || endereco.startsWith("https://") ->
                FonteDoEnderecoDaImagemDoToqueLongo.HTTP
            endereco.startsWith("file:") -> FonteDoEnderecoDaImagemDoToqueLongo.ARQUIVO_LOCAL
            else -> FonteDoEnderecoDaImagemDoToqueLongo.INSUPORTADO
        }
    }

    fun caminhoDoArquivoLocal(enderecoBruto: String): String? {
        if (classificar(enderecoBruto) != FonteDoEnderecoDaImagemDoToqueLongo.ARQUIVO_LOCAL) return null
        return try {
            val uri = URI(enderecoBruto.trim())
            val caminho = uri.path?.takeIf { it.isNotBlank() } ?: return null
            caminho
        } catch (_: Exception) {
            null
        }
    }

    fun podeColocarNaColecaoComoImagem(enderecoBruto: String): Boolean {
        return when (classificar(enderecoBruto)) {
            FonteDoEnderecoDaImagemDoToqueLongo.HTTP,
            FonteDoEnderecoDaImagemDoToqueLongo.ARQUIVO_LOCAL,
            -> true
            else -> false
        }
    }
}

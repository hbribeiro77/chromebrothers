package com.chromebrothers.navegador.regras

import java.net.URI

object AberturaDeMidiaLocalDoComponenteDaColecao {
    enum class Destino(val rotulo: String) {
        ABA("aba"),
        VISOR("visor"),
        EDITOR("editor"),
        BIBLIOTECA("biblioteca"),
        PLAYER("player"),
        COPIAR("copiar"),
    }

    private val esquemasAceitos = setOf("content", "file", "http", "https")

    fun esquemaAceito(endereco: String): Boolean {
        val bruto = endereco.trim()
        if (bruto.isEmpty()) return false
        val uri = try {
            URI(bruto)
        } catch (_: Exception) {
            return false
        }
        val esquema = uri.scheme?.lowercase() ?: return false
        return esquema in esquemasAceitos
    }

    fun precisaCopiarParaArquivoLocal(endereco: String): Boolean {
        val bruto = endereco.trim()
        if (bruto.isEmpty()) return false
        val uri = try {
            URI(bruto)
        } catch (_: Exception) {
            return false
        }
        return uri.scheme?.lowercase() == "content"
    }

    fun mensagemOk(destino: String, detalhe: String = ""): String =
        """{"ok":true,"destino":"${escapar(destino)}","detalhe":"${escapar(detalhe)}"}"""

    fun mensagemErro(motivo: String): String =
        """{"ok":false,"motivo":"${escapar(motivo)}"}"""

    private fun escapar(texto: String): String = buildString(texto.length + 8) {
        for (c in texto) {
            when (c) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
    }
}

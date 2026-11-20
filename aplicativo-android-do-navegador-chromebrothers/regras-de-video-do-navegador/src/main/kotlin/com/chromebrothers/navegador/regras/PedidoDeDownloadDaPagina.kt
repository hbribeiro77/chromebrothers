package com.chromebrothers.navegador.regras

import java.net.URLDecoder
import java.util.Base64

enum class OrigemDoDownloadPedidoPelaPagina { REDE, MEMORIA, IGNORAR }

object PedidoDeDownloadDaPagina {
    fun origem(endereco: String): OrigemDoDownloadPedidoPelaPagina {
        val texto = endereco.trim().lowercase()
        return when {
            texto.startsWith("https://") || texto.startsWith("http://") -> OrigemDoDownloadPedidoPelaPagina.REDE
            texto.startsWith("blob:") || texto.startsWith("data:") -> OrigemDoDownloadPedidoPelaPagina.MEMORIA
            else -> OrigemDoDownloadPedidoPelaPagina.IGNORAR
        }
    }

    fun nome(disposicao: String, endereco: String, mime: String): String {
        val escolhido = nomeDaDisposicao(disposicao) ?: nomeDoEndereco(endereco) ?: "arquivo"
        return comExtensao(sanitizar(escolhido), mime)
    }

    private fun nomeDaDisposicao(disposicao: String): String? {
        val estrelado = Regex("(?i)filename\\*\\s*=\\s*([^;]+)").find(disposicao)?.groupValues?.getOrNull(1)
        if (!estrelado.isNullOrBlank()) {
            val semAspas = estrelado.trim().trim('"')
            val valor = semAspas.substringAfter("''", semAspas)
            return urlDecode(valor).takeIf { it.isNotBlank() }
        }
        val simples = Regex("(?i)filename\\s*=\\s*(\"[^\"]*\"|[^;]+)").find(disposicao)?.groupValues?.getOrNull(1)
        return simples?.trim()?.trim('"')?.takeIf { it.isNotBlank() }?.let(::urlDecode)
    }

    private fun nomeDoEndereco(endereco: String): String? {
        val texto = endereco.trim()
        if (!texto.startsWith("http://") && !texto.startsWith("https://")) return null
        val segmento = texto.substringBefore('?').substringBefore('#').substringAfterLast('/').trim()
        return segmento.takeIf { it.isNotBlank() && !it.contains("://") }
    }

    private fun sanitizar(nome: String): String {
        val segmento = nome.substringAfterLast('/').substringAfterLast('\\')
        val limpo = segmento.replace(Regex("[^A-Za-z0-9._-]"), "_").trim('_', '.').take(120)
        return limpo.ifBlank { "arquivo" }
    }

    private fun comExtensao(nome: String, mime: String): String {
        val sufixo = nome.substringAfterLast('.', "")
        if (nome.contains('.') && sufixo.length in 1..5) return nome
        val extensao = extensaoDoMime(mime) ?: return nome
        return "$nome.$extensao"
    }

    private fun extensaoDoMime(mime: String): String? {
        return when (mime.substringBefore(';').trim().lowercase()) {
            "application/pdf" -> "pdf"
            "text/csv" -> "csv"
            "text/plain" -> "txt"
            "application/json" -> "json"
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            "video/mp4" -> "mp4"
            "audio/mpeg" -> "mp3"
            "application/zip" -> "zip"
            else -> null
        }
    }

    private fun urlDecode(valor: String): String {
        return runCatching { URLDecoder.decode(valor.replace("+", "%2B"), "UTF-8") }.getOrDefault(valor)
    }
}

object ConteudoCodificadoNoEnderecoDaPagina {
    fun ler(endereco: String): ByteArray? {
        if (!endereco.startsWith("data:", ignoreCase = true)) return null
        val resto = endereco.substring(5)
        val virgula = resto.indexOf(',')
        if (virgula < 0) return null
        val meta = resto.substring(0, virgula).lowercase()
        val carga = resto.substring(virgula + 1)
        return if (meta.contains("base64")) {
            runCatching { Base64.getDecoder().decode(carga) }.getOrNull()
        } else {
            runCatching { URLDecoder.decode(carga, "UTF-8").toByteArray(Charsets.UTF_8) }.getOrNull()
        }
    }
}

package com.chromebrothers.navegador.regras

enum class EstadoDoDownloadDeMidiaNaGaveta {
    EM_ANDAMENTO,
    PRONTO,
    FALHOU,
}

data class DownloadDeMidiaNaGaveta(
    val id: String,
    val titulo: String,
    val uri: String,
    val baixado: Long,
    val total: Long,
    val estado: EstadoDoDownloadDeMidiaNaGaveta,
    val aviso: String,
    val instante: Long,
    val tamanho: Long = -1,
    val nomeDoArquivo: String = "",
)

data class ArquivoDeMidiaSalvoNaGaveta(
    val uri: String,
    val nomeDoArquivo: String,
    val instante: Long,
    val tamanho: Long = -1,
    val mime: String = "",
)

class AcompanhamentoDosDownloadsDeMidiaNaGaveta {
    private val itens = mutableListOf<DownloadDeMidiaNaGaveta>()
    private val ouvintes = mutableListOf<() -> Unit>()
    private val ultimoAviso = mutableMapOf<String, Long>()
    private val trava = Any()

    fun observar(ouvinte: () -> Unit): () -> Unit {
        synchronized(trava) { ouvintes += ouvinte }
        return { synchronized(trava) { ouvintes -= ouvinte } }
    }

    fun iniciar(id: String, titulo: String, instante: Long) {
        synchronized(trava) {
            itens.removeAll { it.id == id }
            itens += DownloadDeMidiaNaGaveta(
                id = id,
                titulo = titulo.ifBlank { "Vídeo" },
                uri = "",
                baixado = 0,
                total = -1,
                estado = EstadoDoDownloadDeMidiaNaGaveta.EM_ANDAMENTO,
                aviso = "",
                instante = instante,
            )
            ultimoAviso.remove(id)
        }
        avisar()
    }

    fun progresso(id: String, baixado: Long, total: Long, instante: Long = System.currentTimeMillis()) {
        val mudou = synchronized(trava) {
            val indice = itens.indexOfFirst { it.id == id && it.estado == EstadoDoDownloadDeMidiaNaGaveta.EM_ANDAMENTO }
            if (indice < 0) return@synchronized false
            val atual = itens[indice]
            itens[indice] = atual.copy(baixado = baixado.coerceAtLeast(0), total = total)
            val ultimo = ultimoAviso[id] ?: 0L
            if (total > 0 && baixado >= total) {
                ultimoAviso[id] = instante
                true
            } else if (instante - ultimo < 400) {
                false
            } else {
                ultimoAviso[id] = instante
                true
            }
        }
        if (mudou) avisar()
    }

    fun concluir(id: String, uri: String, titulo: String, instante: Long, nomeDoArquivo: String = "") {
        synchronized(trava) {
            val atual = itens.find { it.id == id }
            val tamanho = atual?.total ?: -1
            itens.removeAll { it.id == id }
            itens += DownloadDeMidiaNaGaveta(
                id = id,
                titulo = titulo.ifBlank { atual?.titulo ?: "Vídeo" },
                uri = uri,
                baixado = tamanho.coerceAtLeast(0),
                total = tamanho,
                estado = EstadoDoDownloadDeMidiaNaGaveta.PRONTO,
                aviso = "",
                instante = instante,
                tamanho = tamanho,
                nomeDoArquivo = nomeDoArquivo,
            )
        }
        avisar()
    }

    fun falhar(id: String, aviso: String) {
        synchronized(trava) {
            val indice = itens.indexOfFirst { it.id == id }
            if (indice < 0) return@synchronized
            val atual = itens[indice]
            itens[indice] = atual.copy(
                estado = EstadoDoDownloadDeMidiaNaGaveta.FALHOU,
                aviso = aviso.ifBlank { "O download não terminou." },
            )
        }
        avisar()
    }

    fun cancelar(id: String) {
        synchronized(trava) {
            itens.removeAll { it.id == id && it.estado == EstadoDoDownloadDeMidiaNaGaveta.EM_ANDAMENTO }
            ultimoAviso.remove(id)
        }
        avisar()
    }

    fun esquecer(uri: String) {
        if (uri.isBlank()) return
        synchronized(trava) { itens.removeAll { it.uri == uri } }
        avisar()
    }

    fun listar(salvos: List<ArquivoDeMidiaSalvoNaGaveta>): List<DownloadDeMidiaNaGaveta> {
        val locais = synchronized(trava) { itens.toList() }
        val urisSalvas = salvos.map { it.uri }.toSet()
        val semRepetir = locais.filter { it.estado != EstadoDoDownloadDeMidiaNaGaveta.PRONTO || it.uri !in urisSalvas }
        val urisLocais = semRepetir.map { it.uri }.filter { it.isNotBlank() }.toSet()
        val gravados = salvos.filter { FiltroDeArquivoDeVideoBaixadoParaAGaveta.ehVideo(it.nomeDoArquivo, it.mime) }
            .filter { it.uri !in urisLocais }
            .map { salvo ->
            DownloadDeMidiaNaGaveta(
                id = salvo.uri,
                titulo = TituloDoArquivoDeMidiaBaixadoParaAGaveta.texto(salvo.nomeDoArquivo),
                uri = salvo.uri,
                baixado = 0,
                total = salvo.tamanho,
                estado = EstadoDoDownloadDeMidiaNaGaveta.PRONTO,
                aviso = "",
                instante = salvo.instante,
                tamanho = salvo.tamanho,
                nomeDoArquivo = salvo.nomeDoArquivo,
            )
        }.sortedByDescending { it.instante }
        val andamento = semRepetir.filter { it.estado == EstadoDoDownloadDeMidiaNaGaveta.EM_ANDAMENTO }
            .sortedByDescending { it.instante }
        val falhas = semRepetir.filter { it.estado == EstadoDoDownloadDeMidiaNaGaveta.FALHOU }
            .sortedByDescending { it.instante }
        val prontos = semRepetir.filter { it.estado == EstadoDoDownloadDeMidiaNaGaveta.PRONTO }
            .sortedByDescending { it.instante }
        return andamento + falhas + prontos + gravados
    }

    private fun avisar() {
        val copia = synchronized(trava) { ouvintes.toList() }
        copia.forEach { ouvinte ->
            try {
                ouvinte()
            } catch (_: Exception) {
            }
        }
    }
}

object TextoDoProgressoDoDownloadDeMidiaNaGaveta {
    fun texto(item: DownloadDeMidiaNaGaveta): String {
        return when (item.estado) {
            EstadoDoDownloadDeMidiaNaGaveta.FALHOU -> item.aviso.ifBlank { "O download não terminou." }
            EstadoDoDownloadDeMidiaNaGaveta.PRONTO -> {
                val tamanho = FormatoDoTamanhoDeMidiaParaDownload.texto(item.tamanho)
                if (tamanho.isBlank()) "Pronto" else "Pronto · $tamanho"
            }
            EstadoDoDownloadDeMidiaNaGaveta.EM_ANDAMENTO -> {
                if (item.total <= 0) {
                    "Preparando"
                } else {
                    val percentual = ((item.baixado * 100) / item.total).toInt().coerceIn(0, 100)
                    val tamanho = FormatoDoTamanhoDeMidiaParaDownload.texto(item.total)
                    if (tamanho.isBlank()) "$percentual%" else "$percentual% · $tamanho"
                }
            }
        }
    }
}

object TituloDoArquivoDeMidiaBaixadoParaAGaveta {
    fun texto(nome: String): String {
        val semExtensao = nome.substringBeforeLast('.', nome)
        val semPrefixo = semExtensao.removePrefix("chromebrothers-")
        return semPrefixo.replace(Regex("-\\d{10,}$"), "").trim().ifBlank { semPrefixo.ifBlank { nome } }
    }
}

object FiltroDeArquivoDeVideoBaixadoParaAGaveta {
    private val extensoes = listOf(".mp4", ".m4v", ".webm", ".mkv", ".mov", ".ogv", ".ts")

    fun ehVideo(nome: String, mime: String): Boolean {
        val tipo = mime.substringBefore(';').trim().lowercase()
        if (tipo.startsWith("audio/")) return false
        if (tipo.startsWith("video/")) return true
        val caminho = nome.substringBefore('?').lowercase()
        return extensoes.any { caminho.endsWith(it) }
    }
}

object NomeRenomeadoDoArquivoDeMidiaBaixado {
    fun montar(nomeAtual: String, nomeDigitado: String): String? {
        val extensao = nomeAtual.substringAfterLast('.', "").lowercase().filter { it.isLetterOrDigit() }
        val digitado = nomeDigitado.replace(Regex("""[\\/:*?"<>|]"""), " ").replace(Regex("\\s+"), " ").trim()
        val semExtensao = if (extensao.isNotEmpty() && digitado.endsWith(".$extensao", ignoreCase = true)) {
            digitado.dropLast(extensao.length + 1).trim()
        } else {
            digitado
        }.take(60).trim()
        if (semExtensao.isBlank()) return null
        return if (extensao.isBlank()) semExtensao else "$semExtensao.$extensao"
    }
}

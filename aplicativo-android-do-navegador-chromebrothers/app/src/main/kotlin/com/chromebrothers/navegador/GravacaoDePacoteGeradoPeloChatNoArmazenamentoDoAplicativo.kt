package com.chromebrothers.navegador

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.chromebrothers.navegador.regras.GeracaoDeArquivoPeloChatDaOpenRouter
import com.chromebrothers.navegador.regras.LeitorDeMarkdownNaColecaoDoNavegador
import com.chromebrothers.navegador.regras.PedidoDeAcaoConfirmadaDoChat
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class GravacaoDePacoteGeradoPeloChatNoArmazenamentoDoAplicativo(
    private val contexto: Context,
) {
    fun gerarMarkdown(
        pedido: PedidoDeAcaoConfirmadaDoChat.GerarMarkdown,
        referer: String,
        agente: String,
    ): String {
        val pasta = GeracaoDeArquivoPeloChatDaOpenRouter.nomeDeArquivoSeguro(pedido.titulo)
        val mapa = linkedMapOf<String, String>()
        var falhas = 0
        pedido.imagens.forEach { imagem ->
            val bytes = baixarBytes(imagem.endereco, referer, agente)
            if (bytes == null) {
                falhas++
                return@forEach
            }
            val nome = GeracaoDeArquivoPeloChatDaOpenRouter.nomeDeArquivoSeguro(imagem.nome)
                .ifBlank { "imagem.png" }
            val ok = publicar(
                bytes = bytes,
                nome = nome,
                mime = mimePeloNome(nome),
                pastaRelativa = "ChromeBrothers/$pasta/assets",
            )
            if (ok) mapa[imagem.endereco] = "assets/$nome" else falhas++
        }
        val corpo = GeracaoDeArquivoPeloChatDaOpenRouter.reescreverComAssets(pedido.conteudo, mapa)
        val mdOk = publicar(
            bytes = corpo.toByteArray(Charsets.UTF_8),
            nome = "$pasta.md",
            mime = "text/markdown",
            pastaRelativa = "ChromeBrothers/$pasta",
        )
        if (!mdOk) return "Não deu para gravar o markdown."
        return when {
            falhas == 0 && mapa.isEmpty() -> "Markdown salvo em Downloads/ChromeBrothers/$pasta."
            falhas == 0 -> "Markdown salvo com ${mapa.size} imagem(ns) em Downloads/ChromeBrothers/$pasta."
            mapa.isEmpty() -> "Markdown salvo, mas nenhuma imagem baixou ($falhas falha(s))."
            else -> "Markdown salvo com ${mapa.size} imagem(ns); $falhas falha(s) no download."
        }
    }

    fun mostrarMarkdownNaColecao(
        pedido: PedidoDeAcaoConfirmadaDoChat.MostrarMarkdownNaColecao,
    ): String {
        val nome = LeitorDeMarkdownNaColecaoDoNavegador.nomeDoArquivo(pedido.titulo)
        val ok = publicar(
            bytes = pedido.conteudo.toByteArray(Charsets.UTF_8),
            nome = nome,
            mime = "text/markdown",
            pastaRelativa = LeitorDeMarkdownNaColecaoDoNavegador.PASTA_RELATIVA_DOWNLOADS,
        )
        if (!ok) return "Não deu para gravar o markdown."
        return "Markdown salvo em Downloads/${LeitorDeMarkdownNaColecaoDoNavegador.PASTA_RELATIVA_DOWNLOADS}/$nome."
    }

    fun gerarZip(
        pedido: PedidoDeAcaoConfirmadaDoChat.GerarZip,
        referer: String,
        agente: String,
    ): String {
        val saida = ByteArrayOutputStream()
        var ok = 0
        var falhas = 0
        ZipOutputStream(saida).use { zip ->
            pedido.itens.forEach { item ->
                val bytes = baixarBytes(item.endereco, referer, agente)
                if (bytes == null) {
                    falhas++
                    return@forEach
                }
                zip.putNextEntry(ZipEntry(item.caminhoNoZip))
                zip.write(bytes)
                zip.closeEntry()
                ok++
            }
            val markdown = pedido.conteudoMarkdown
            if (!markdown.isNullOrBlank()) {
                zip.putNextEntry(ZipEntry("readme.md"))
                zip.write(markdown.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                ok++
            }
        }
        if (ok == 0) return "Não deu para montar o ZIP."
        val nome = GeracaoDeArquivoPeloChatDaOpenRouter.nomeDeArquivoSeguro(pedido.nome) + ".zip"
        val publicado = publicar(
            bytes = saida.toByteArray(),
            nome = nome,
            mime = "application/zip",
            pastaRelativa = "ChromeBrothers",
        )
        if (!publicado) return "Não deu para gravar o ZIP."
        return if (falhas == 0) {
            "ZIP salvo em Downloads/ChromeBrothers/$nome ($ok arquivo(s))."
        } else {
            "ZIP salvo com $ok arquivo(s); $falhas falha(s) no download."
        }
    }

    private fun baixarBytes(endereco: String, referer: String, agente: String): ByteArray? {
        if (endereco.startsWith("file:")) {
            return CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.bytesDeArquivoLocal(endereco)
                ?.takeIf { it.isNotEmpty() && it.size <= LIMITE_POR_ARQUIVO }
        }
        if (!endereco.startsWith("http://") && !endereco.startsWith("https://")) return null
        val temporario = File(contexto.cacheDir, "chat-pacote-${System.nanoTime()}")
        return try {
            val conexao = URI(endereco).toURL().openConnection() as HttpURLConnection
            conexao.instanceFollowRedirects = true
            conexao.connectTimeout = 20_000
            conexao.readTimeout = 60_000
            conexao.setRequestProperty("User-Agent", agente.ifBlank { "Mozilla/5.0" })
            conexao.setRequestProperty("Accept", "*/*")
            if (referer.isNotBlank()) conexao.setRequestProperty("Referer", referer)
            conexao.connect()
            if (conexao.responseCode !in 200..299) return null
            conexao.inputStream.use { entrada ->
                temporario.outputStream().use { saida -> entrada.copyTo(saida) }
            }
            if (temporario.length() <= 0L || temporario.length() > LIMITE_POR_ARQUIVO) return null
            temporario.readBytes()
        } catch (_: Exception) {
            null
        } finally {
            temporario.delete()
        }
    }

    private fun publicar(bytes: ByteArray, nome: String, mime: String, pastaRelativa: String): Boolean {
        if (bytes.isEmpty() || bytes.size > LIMITE_POR_ARQUIVO) return false
        val valores = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, nome)
            put(MediaStore.Downloads.MIME_TYPE, mime)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/" + pastaRelativa)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val colecao = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = contexto.contentResolver.insert(colecao, valores) ?: return false
        return try {
            contexto.contentResolver.openOutputStream(uri)?.use { saida ->
                saida.write(bytes)
            } ?: error("sem saida")
            valores.clear()
            valores.put(MediaStore.Downloads.IS_PENDING, 0)
            contexto.contentResolver.update(uri, valores, null, null)
            true
        } catch (_: Exception) {
            contexto.contentResolver.delete(uri, null, null)
            false
        }
    }

    private fun mimePeloNome(nome: String): String {
        val baixo = nome.lowercase()
        return when {
            baixo.endsWith(".png") -> "image/png"
            baixo.endsWith(".jpg") || baixo.endsWith(".jpeg") -> "image/jpeg"
            baixo.endsWith(".webp") -> "image/webp"
            baixo.endsWith(".gif") -> "image/gif"
            baixo.endsWith(".md") -> "text/markdown"
            baixo.endsWith(".mp4") -> "video/mp4"
            baixo.endsWith(".webm") -> "video/webm"
            else -> "application/octet-stream"
        }
    }

    private companion object {
        const val LIMITE_POR_ARQUIVO = 40L * 1024L * 1024L
    }
}

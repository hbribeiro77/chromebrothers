# Task 3 re-review after sync fix


===== FILE: app\src\main\kotlin\com\chromebrothers\navegador\GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers.kt =====

package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat
import com.chromebrothers.navegador.regras.OrigemDaMidiaNaBibliotecaDoNavegador
import java.io.File
import java.util.UUID

data class ImagemHiggsfieldGuardada(
    val id: String,
    val caminhoArquivo: String,
)

data class VideoHiggsfieldGuardado(
    val id: String,
    val caminhoArquivo: String,
)

class GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers(
    private val context: Context,
) {
    fun guardarImagemComMetadados(
        bytes: ByteArray,
        prompt: String,
        origem: String,
        modelo: String = "",
        refsBytes: List<Pair<ByteArray, String>> = emptyList(),
    ): ImagemHiggsfieldGuardada? {
        synchronized(travaDaBibliotecaGerada) {
            val gravacao = GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo(context)
            val biblioteca = gravacao.carregar()
            val item = gravacao.guardarQuadro(biblioteca, bytes, naGaleria = false) ?: return null
            val pastaDaBiblioteca = File(context.filesDir, "biblioteca-de-imagens-capturadas")
            val caminho = File(pastaDaBiblioteca, item.nomeDoArquivo).absolutePath
            val refs = copiarReferencias(item.id, refsBytes)
            biblioteca.atualizarMetadadosDeGeracao(
                id = item.id,
                origem = OrigemDaMidiaNaBibliotecaDoNavegador.parse(origem),
                modelo = modelo,
                prompt = prompt,
                refs = refs,
                caminhoLocal = caminho,
            )
            gravacao.gravar(biblioteca)
            return ImagemHiggsfieldGuardada(
                id = item.id,
                caminhoArquivo = caminho,
            )
        }
    }

    fun guardarImagem(
        bytes: ByteArray,
        prompt: String,
        origem: String = OrigemDaMidiaNaBibliotecaDoNavegador.HIGGSFIELD,
        modelo: String = "",
        refsBytes: List<Pair<ByteArray, String>> = emptyList(),
    ): String {
        val guardada = guardarImagemComMetadados(bytes, prompt, origem, modelo, refsBytes)
            ?: return "Gerei a imagem, mas não consegui salvar na biblioteca."
        return GeracaoDeImagemEVideoPeloHiggsfieldNoChat.sucessoImagem(guardada.id, prompt)
    }

    fun guardarVideoComMetadados(
        bytes: ByteArray,
        prompt: String,
        origem: String,
        modelo: String = "",
        refsBytes: List<Pair<ByteArray, String>> = emptyList(),
    ): VideoHiggsfieldGuardado? {
        if (bytes.isEmpty()) return null
        synchronized(travaDaBibliotecaGerada) {
            val pasta = File(context.filesDir, "biblioteca-de-videos-higgsfield")
            pasta.mkdirs()
            val origemNormalizada = OrigemDaMidiaNaBibliotecaDoNavegador.parse(origem)
            val id = UUID.randomUUID().toString()
            val chave = "$origemNormalizada-$id"
            val arquivo = File(pasta, "$chave.mp4")
            arquivo.writeBytes(bytes)
            val refs = copiarReferencias(chave, refsBytes)
            val gravacao = GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo(context)
            val biblioteca = gravacao.carregar()
            val titulo = prompt.take(80).ifBlank { "Vídeo gerado" }
            // file:// para o ExoPlayer / abrirArquivoBaixado (caminho absoluto falhava no Decisao http-only).
            val endereco = arquivo.toURI().toString()
            biblioteca.registrar(
                chave = chave,
                titulo = titulo,
                enderecoParaAbrir = endereco,
                abreNoPlayerDeArquivo = true,
                instante = System.currentTimeMillis(),
                origem = origemNormalizada,
                modelo = modelo,
                prompt = prompt,
                refs = refs,
                caminhoLocal = arquivo.absolutePath,
            )
            quadroDoArquivoLocal(arquivo)?.let { quadro ->
                val nomeMiniatura = gravacao.salvarMiniatura(chave, quadro)
                if (nomeMiniatura != null) biblioteca.definirMiniatura(chave, nomeMiniatura)
                if (!quadro.isRecycled) quadro.recycle()
            }
            gravacao.gravar(biblioteca)
            return VideoHiggsfieldGuardado(id = chave, caminhoArquivo = arquivo.absolutePath)
        }
    }

    fun guardarVideo(
        bytes: ByteArray,
        prompt: String,
        origem: String = OrigemDaMidiaNaBibliotecaDoNavegador.HIGGSFIELD,
        modelo: String = "",
        refsBytes: List<Pair<ByteArray, String>> = emptyList(),
    ): String {
        val guardado = guardarVideoComMetadados(bytes, prompt, origem, modelo, refsBytes)
            ?: return "Gerei o vídeo, mas não consegui salvar na biblioteca."
        return GeracaoDeImagemEVideoPeloHiggsfieldNoChat.sucessoVideo(guardado.id, prompt)
    }

    /** Copia cada referência para `refs-de-geracao-da-biblioteca/<chave>-ref-<i>.<ext>`; devolve os nomes dos arquivos. */
    private fun copiarReferencias(chave: String, refsBytes: List<Pair<ByteArray, String>>): List<String> {
        if (refsBytes.isEmpty()) return emptyList()
        val pasta = File(context.filesDir, "refs-de-geracao-da-biblioteca")
        pasta.mkdirs()
        val nomes = mutableListOf<String>()
        refsBytes.forEachIndexed { indice, (bytesDaRef, nomeOuMime) ->
            if (bytesDaRef.isEmpty()) return@forEachIndexed
            val nome = "$chave-ref-$indice.${extensaoDaReferencia(nomeOuMime)}"
            runCatching { File(pasta, nome).writeBytes(bytesDaRef) }
                .onSuccess { nomes += nome }
        }
        return nomes
    }

    private fun extensaoDaReferencia(nomeOuMime: String): String {
        val texto = nomeOuMime.trim().lowercase()
        if (texto.startsWith("image/")) {
            return when (texto.substringAfter('/').substringBefore(';')) {
                "png" -> "png"
                "webp" -> "webp"
                "gif" -> "gif"
                else -> "jpg"
            }
        }
        val ext = texto.substringAfterLast('.', "").takeWhile { it.isLetterOrDigit() }
        return if (ext.length in 2..5) ext else "jpg"
    }
    private fun quadroDoArquivoLocal(arquivo: File): Bitmap? {
        if (!arquivo.isFile || arquivo.length() <= 0L) return null
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(arquivo.absolutePath)
            retriever.getScaledFrameAtTime(
                1_000_000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                960,
                540,
            )
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    companion object {
        private val travaDaBibliotecaGerada = Any()
    }
}


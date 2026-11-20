package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.chromebrothers.navegador.regras.DocumentoDoRascunhoDaEdicaoDeImagem
import com.chromebrothers.navegador.regras.FerramentaGravadaDoRascunhoDaEdicao
import com.chromebrothers.navegador.regras.IdentificadorDaPastaDoRascunhoDaEdicao
import com.chromebrothers.navegador.regras.ImagemDoRascunhoDaEdicao
import com.chromebrothers.navegador.regras.TextoDoRascunhoDaEdicaoDeImagem
import java.io.File
import kotlin.math.min
import kotlin.random.Random

class GravacaoDoRascunhoDaEdicaoDeImagem(
    private val raiz: File,
) {
    private val trava = Any()

    constructor(contexto: Context) : this(File(contexto.filesDir, "rascunhos-da-edicao-de-imagem"))

    fun listar(): List<String> {
        migrarPastaUnica()
        val pastas = raiz.listFiles()?.filter { pasta ->
            IdentificadorDaPastaDoRascunhoDaEdicao.aceito(pasta.name) && documento(pasta).isFile
        }.orEmpty()
        return pastas.sortedByDescending { it.lastModified() }.map { it.name }
    }

    fun novoId(): String {
        var id = System.currentTimeMillis().toString()
        while (File(raiz, id).exists()) {
            id = "${System.currentTimeMillis()}-${Random.nextInt(1000, 10000)}"
        }
        return id
    }

    fun instante(id: String): Long = pasta(id)?.lastModified() ?: 0L

    fun arquivoDaMiniatura(id: String): File? = synchronized(trava) {
        val pasta = pasta(id) ?: return null
        val texto = runCatching { documento(pasta).readText() }.getOrNull() ?: return null
        val nome = TextoDoRascunhoDaEdicaoDeImagem.ler(texto)?.arquivoDaPrimeiraImagem() ?: return null
        val pastaCanonica = pasta.canonicalFile
        val arquivo = File(pastaCanonica, nome).canonicalFile
        if (arquivo.parentFile != pastaCanonica || !arquivo.isFile) return null
        arquivo
    }

    fun miniatura(arquivo: File, lado: Int): Bitmap? = synchronized(trava) {
        val medida = lado.coerceAtLeast(1)
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(arquivo.absolutePath, limites)
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null
        val opcoes = BitmapFactory.Options().apply {
            inSampleSize = (min(limites.outWidth, limites.outHeight) / medida).coerceAtLeast(1)
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        BitmapFactory.decodeFile(arquivo.absolutePath, opcoes)
    }

    fun guardar(id: String, exportacao: EditorDeRecorteETracoDaImagemDaBiblioteca.Exportacao): Boolean = synchronized(trava) {
        val pasta = pasta(id) ?: return false
        if (exportacao.largura <= 0 || exportacao.altura <= 0) return false
        pasta.mkdirs()
        val imagens = exportacao.imagens.mapIndexed { indice, imagem ->
            val nome = "peca-$indice.png"
            val parcial = File(pasta, "$nome.parte")
            parcial.outputStream().use { saida ->
                if (!imagem.bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)) {
                    parcial.delete()
                    return false
                }
            }
            ImagemDoRascunhoDaEdicao(
                imagem.id,
                nome,
                imagem.esquerda,
                imagem.topo,
                imagem.largura,
                imagem.altura,
            ) to parcial
        }
        val descricao = imagens.map { it.first }
        val documento = DocumentoDoRascunhoDaEdicaoDeImagem(
            largura = exportacao.largura,
            altura = exportacao.altura,
            proximoId = exportacao.proximoId,
            ferramenta = when (exportacao.ferramenta) {
                EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BRANCO -> FerramentaGravadaDoRascunhoDaEdicao.BRANCO
                EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA -> FerramentaGravadaDoRascunhoDaEdicao.BORRACHA
                EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE -> FerramentaGravadaDoRascunhoDaEdicao.RECORTE
                EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO -> FerramentaGravadaDoRascunhoDaEdicao.VERMELHO
            },
            imagens = descricao,
            textos = exportacao.textos,
            tracos = exportacao.tracos,
            corDaFolha = exportacao.corDaFolha,
            angulos = exportacao.angulos,
            espelhos = exportacao.espelhos,
            ordem = exportacao.ordem,
            camadasVazias = exportacao.camadasVazias,
            opacidades = exportacao.opacidades,
            ocultas = exportacao.ocultas,
            bloqueadas = exportacao.bloqueadas,
            pinceis = exportacao.pinceis,
        )
        val texto = TextoDoRascunhoDaEdicaoDeImagem.escrever(documento)
        val documentoParcial = File(pasta, "documento.txt.parte")
        documentoParcial.writeText(texto)
        for ((imagem, parcial) in imagens) {
            val destino = File(pasta, imagem.arquivo)
            if (destino.exists() && !destino.delete()) {
                parcial.delete()
                return false
            }
            if (!parcial.renameTo(destino)) {
                parcial.delete()
                return false
            }
        }
        val documentoPronto = documento(pasta)
        if (documentoPronto.exists() && !documentoPronto.delete()) {
            documentoParcial.delete()
            return false
        }
        if (!documentoParcial.renameTo(documentoPronto)) {
            documentoParcial.delete()
            return false
        }
        val nomes = descricao.map { it.arquivo }.toSet()
        pasta.listFiles()?.forEach { arquivo ->
            val nome = arquivo.name
            if (nome.endsWith(".parte") || (nome.startsWith("peca-") && nome !in nomes)) arquivo.delete()
        }
        true
    }

    fun carregar(id: String): RascunhoCarregadoDaEdicaoDeImagem? = synchronized(trava) {
        val pasta = pasta(id) ?: return null
        val texto = runCatching { documento(pasta).readText() }.getOrNull() ?: return null
        val documento = TextoDoRascunhoDaEdicaoDeImagem.ler(texto) ?: return null
        val pastaCanonica = pasta.canonicalFile
        val bitmaps = mutableMapOf<String, Bitmap>()
        for (imagem in documento.imagens) {
            val arquivo = File(pastaCanonica, imagem.arquivo).canonicalFile
            if (arquivo.parentFile != pastaCanonica || !arquivo.isFile) {
                bitmaps.values.forEach { if (!it.isRecycled) it.recycle() }
                return null
            }
            val decodificado = BitmapFactory.decodeFile(arquivo.absolutePath)
            val bitmap = decodificado?.copy(Bitmap.Config.ARGB_8888, true)
            if (bitmap == null) {
                if (decodificado != null && !decodificado.isRecycled) decodificado.recycle()
                bitmaps.values.forEach { if (!it.isRecycled) it.recycle() }
                return null
            }
            if (decodificado !== bitmap && !decodificado.isRecycled) decodificado.recycle()
            bitmaps[imagem.arquivo] = bitmap
        }
        return RascunhoCarregadoDaEdicaoDeImagem(documento, bitmaps)
    }

    fun apagar(id: String) {
        synchronized(trava) {
            val pasta = pasta(id) ?: return
            pasta.listFiles()?.forEach { it.delete() }
            pasta.delete()
        }
    }

    private fun migrarPastaUnica() {
        val legado = File(raiz.parentFile, "rascunho-da-edicao-de-imagem")
        if (!documento(legado).isFile) return
        raiz.mkdirs()
        val destino = File(raiz, novoId())
        if (destino.exists()) return
        legado.renameTo(destino)
    }

    private fun pasta(id: String): File? {
        if (!IdentificadorDaPastaDoRascunhoDaEdicao.aceito(id)) return null
        return File(raiz, id)
    }

    private fun documento(pasta: File): File = File(pasta, "documento-do-rascunho-da-edicao.txt")
}

class RascunhoCarregadoDaEdicaoDeImagem(
    val documento: DocumentoDoRascunhoDaEdicaoDeImagem,
    val bitmaps: Map<String, Bitmap>,
)

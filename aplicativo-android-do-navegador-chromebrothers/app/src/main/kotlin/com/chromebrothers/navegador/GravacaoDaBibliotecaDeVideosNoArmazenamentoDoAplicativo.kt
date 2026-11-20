package com.chromebrothers.navegador

import android.graphics.Bitmap
import com.chromebrothers.navegador.regras.AvaliacaoDeQuadroParaMiniaturaDeVideo
import com.chromebrothers.navegador.regras.BibliotecaDeVideosDoNavegador
import com.chromebrothers.navegador.regras.ItemDaBibliotecaDeVideos
import com.chromebrothers.navegador.regras.OrigemDaMidiaNaBibliotecaDoNavegador
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

class GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo(
    private val pasta: File,
) {
    constructor(contexto: android.content.Context) : this(
        File(contexto.filesDir, "biblioteca-de-videos-com-miniatura"),
    )

    fun carregar(): BibliotecaDeVideosDoNavegador {
        val arquivo = File(pasta, "itens-da-biblioteca.json")
        if (!arquivo.isFile) return BibliotecaDeVideosDoNavegador()
        return try {
            val lista = mutableListOf<ItemDaBibliotecaDeVideos>()
            val array = JSONArray(arquivo.readText())
            for (indice in 0 until array.length()) {
                val objeto = array.getJSONObject(indice)
                val chave = objeto.optString("chave")
                val endereco = objeto.optString("enderecoParaAbrir")
                if (chave.isBlank() || endereco.isBlank()) continue
                lista += ItemDaBibliotecaDeVideos(
                    chave = chave,
                    titulo = objeto.optString("titulo").ifBlank { endereco },
                    enderecoParaAbrir = endereco,
                    abreNoPlayerDeArquivo = objeto.optBoolean("abreNoPlayerDeArquivo"),
                    favorito = objeto.optBoolean("favorito"),
                    instante = objeto.optLong("instante"),
                    nomeDaMiniatura = objeto.optString("nomeDaMiniatura"),
                    foraDoHistorico = objeto.optBoolean("foraDoHistorico"),
                    origem = OrigemDaMidiaNaBibliotecaDoNavegador.parse(objeto.optString("origem")),
                    modelo = objeto.optString("modelo"),
                    prompt = objeto.optString("prompt"),
                    refs = referenciasDeTextoNoArrayJson(objeto.optJSONArray("refs")),
                    caminhoLocal = objeto.optString("caminhoLocal"),
                    exportado = objeto.optBoolean("exportado", false),
                    idDaReceita = objeto.optString("idDaReceita"),
                )
            }
            BibliotecaDeVideosDoNavegador(lista)
        } catch (_: Exception) {
            BibliotecaDeVideosDoNavegador()
        }
    }

    fun gravar(biblioteca: BibliotecaDeVideosDoNavegador) {
        pasta.mkdirs()
        val array = JSONArray()
        biblioteca.listar().forEach { item ->
            array.put(
                JSONObject()
                    .put("chave", item.chave)
                    .put("titulo", item.titulo)
                    .put("enderecoParaAbrir", item.enderecoParaAbrir)
                    .put("abreNoPlayerDeArquivo", item.abreNoPlayerDeArquivo)
                    .put("favorito", item.favorito)
                    .put("instante", item.instante)
                    .put("nomeDaMiniatura", item.nomeDaMiniatura)
                    .put("foraDoHistorico", item.foraDoHistorico)
                    .put("origem", item.origem)
                    .put("modelo", item.modelo)
                    .put("prompt", item.prompt)
                    .put("refs", JSONArray(item.refs))
                    .put("caminhoLocal", item.caminhoLocal)
                    .put("exportado", item.exportado)
                    .put("idDaReceita", item.idDaReceita),
            )
        }
        File(pasta, "itens-da-biblioteca.json").writeText(array.toString())
    }

    fun apagarMiniatura(nome: String) {
        arquivoDaMiniatura(nome)?.delete()
    }

    fun arquivoDaMiniatura(nome: String): File? {
        if (nome.isBlank() || nome.contains('/') || nome.contains('\\')) return null
        val arquivo = File(pasta, nome)
        return if (arquivo.isFile) arquivo else null
    }

    fun salvarMiniatura(chave: String, origem: Bitmap): String? {
        if (chave.isBlank() || origem.width <= 0 || origem.height <= 0) return null
        val largura = origem.width
        val reduzida = if (largura <= LARGURA_DA_MINIATURA) {
            origem
        } else {
            val altura = (origem.height.toLong() * LARGURA_DA_MINIATURA / largura).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(origem, LARGURA_DA_MINIATURA, altura, true)
        }
        val criada = reduzida !== origem
        val serve = AvaliacaoDeQuadroParaMiniaturaDeVideo.serve(amostrar(reduzida))
        if (!serve) {
            if (criada && !reduzida.isRecycled) reduzida.recycle()
            return null
        }
        pasta.mkdirs()
        val nome = "miniatura-${resumo(chave)}.jpg"
        File(pasta, nome).outputStream().use { saida ->
            reduzida.compress(Bitmap.CompressFormat.JPEG, 86, saida)
        }
        if (criada && !reduzida.isRecycled) reduzida.recycle()
        return nome
    }

    private fun amostrar(bitmap: Bitmap): IntArray {
        val passoX = (bitmap.width / 24).coerceAtLeast(1)
        val passoY = (bitmap.height / 24).coerceAtLeast(1)
        val valores = mutableListOf<Int>()
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val cor = bitmap.getPixel(x, y)
                val vermelho = (cor shr 16) and 0xff
                val verde = (cor shr 8) and 0xff
                val azul = cor and 0xff
                valores += (vermelho * 3 + verde * 6 + azul) / 10
                x += passoX
            }
            y += passoY
        }
        return valores.toIntArray()
    }

    private fun referenciasDeTextoNoArrayJson(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val textos = mutableListOf<String>()
        for (indice in 0 until array.length()) {
            val texto = array.optString(indice)
            if (texto.isNotBlank()) textos += texto
        }
        return textos
    }

    private fun resumo(chave: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(chave.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val LARGURA_DA_MINIATURA = 960
    }
}

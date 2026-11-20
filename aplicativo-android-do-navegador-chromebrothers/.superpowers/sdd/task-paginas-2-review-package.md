# Task 2
## FILE
```kotlin
package com.chromebrothers.navegador

import android.content.Context
import com.chromebrothers.navegador.regras.BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador
import com.chromebrothers.navegador.regras.ItemDaBibliotecaDePaginasHtmlDaColecao
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class GravacaoDaBibliotecaDePaginasHtmlDaColecaoNoArmazenamentoDoAplicativo(
    private val pasta: File,
) {
    constructor(contexto: Context) : this(
        File(contexto.filesDir, "biblioteca-de-paginas-da-colecao"),
    )

    fun carregar(): BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador {
        val arquivo = File(pasta, "itens.json")
        if (!arquivo.isFile) return BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador()
        return try {
            val lista = mutableListOf<ItemDaBibliotecaDePaginasHtmlDaColecao>()
            val array = JSONArray(arquivo.readText())
            for (indice in 0 until array.length()) {
                val objeto = array.getJSONObject(indice)
                val id = objeto.optString("id")
                val titulo = objeto.optString("titulo")
                if (id.isBlank() || titulo.isBlank()) continue
                val html = try {
                    lerHtml(id)
                } catch (_: Exception) {
                    null
                }
                if (html.isNullOrBlank()) continue
                lista += ItemDaBibliotecaDePaginasHtmlDaColecao(
                    id = id,
                    titulo = titulo,
                    html = html,
                    atualizadoEm = objeto.optLong("atualizadoEm"),
                )
            }
            BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador(lista)
        } catch (_: Exception) {
            BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador()
        }
    }

    fun gravar(biblioteca: BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador) {
        pasta.mkdirs()
        val array = JSONArray()
        biblioteca.listar().forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("titulo", item.titulo)
                    .put("atualizadoEm", item.atualizadoEm),
            )
            escreverHtml(item.id, item.html)
        }
        File(pasta, "itens.json").writeText(array.toString())
    }

    fun lerHtml(id: String): String? {
        val arquivo = arquivoHtml(id) ?: return null
        if (!arquivo.isFile) return null
        return arquivo.readText()
    }

    fun escreverHtml(id: String, html: String) {
        val arquivo = arquivoHtml(id) ?: return
        pasta.mkdirs()
        arquivo.writeText(html)
    }

    fun apagarArquivos(id: String) {
        arquivoHtml(id)?.delete()
    }

    private fun arquivoHtml(id: String): File? {
        if (!idServeComoNomeDeArquivo(id)) return null
        return File(pasta, "$id.html")
    }

    private fun idServeComoNomeDeArquivo(id: String): Boolean {
        if (id.isBlank() || id.length > 80) return false
        return id.all { it.isLetterOrDigit() || it == '-' || it == '_' }
    }
}

```

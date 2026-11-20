package com.chromebrothers.navegador

import android.content.Context
import com.chromebrothers.navegador.regras.BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador
import com.chromebrothers.navegador.regras.ItemDaBibliotecaDeComponentesHtmlDaColecao
import com.chromebrothers.navegador.regras.LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml
import com.chromebrothers.navegador.regras.MetadadosDoComponenteNoIndiceDaBiblioteca
import java.io.File

class GravacaoDaBibliotecaDeComponentesHtmlDaColecaoNoArmazenamentoDoAplicativo(
    private val pasta: File,
) {
    constructor(contexto: Context) : this(
        File(contexto.filesDir, "biblioteca-de-paginas-da-colecao"),
    )

    fun carregar(): BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador {
        val arquivo = File(pasta, "itens.json")
        if (!arquivo.isFile) return BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador()
        return try {
            val lista = mutableListOf<ItemDaBibliotecaDeComponentesHtmlDaColecao>()
            LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml.ler(arquivo.readText()).forEach { meta ->
                val html = try {
                    lerHtml(meta.id)
                } catch (_: Exception) {
                    null
                }
                if (html.isNullOrBlank()) return@forEach
                lista += ItemDaBibliotecaDeComponentesHtmlDaColecao(
                    id = meta.id,
                    nome = meta.nome,
                    html = html,
                    atualizadoEm = meta.atualizadoEm,
                    descricao = meta.descricao,
                    tags = meta.tags,
                    larguraPx = meta.larguraPx,
                    alturaPx = meta.alturaPx,
                )
            }
            BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador(lista)
        } catch (_: Exception) {
            BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador()
        }
    }

    fun gravar(biblioteca: BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador) {
        pasta.mkdirs()
        val indice = biblioteca.listar().map { item ->
            escreverHtml(item.id, item.html)
            MetadadosDoComponenteNoIndiceDaBiblioteca(
                id = item.id,
                nome = item.nome,
                descricao = item.descricao,
                tags = item.tags,
                atualizadoEm = item.atualizadoEm,
                larguraPx = item.larguraPx,
                alturaPx = item.alturaPx,
            )
        }
        File(pasta, "itens.json").writeText(
            LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml.escrever(indice),
        )
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

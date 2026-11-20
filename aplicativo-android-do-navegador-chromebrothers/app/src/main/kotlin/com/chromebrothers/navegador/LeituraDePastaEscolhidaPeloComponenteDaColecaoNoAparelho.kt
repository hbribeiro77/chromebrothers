package com.chromebrothers.navegador

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.chromebrothers.navegador.regras.ItemDaPastaEscolhidaPeloComponente
import com.chromebrothers.navegador.regras.ListagemDePastaEscolhidaPeloComponenteDaColecao

class LeituraDePastaEscolhidaPeloComponenteDaColecaoNoAparelho(
    private val context: Context,
) {
    fun descrever(uri: Uri): String {
        val pasta = DocumentFile.fromTreeUri(context, uri)
            ?: DocumentFile.fromSingleUri(context, uri)
            ?: return ListagemDePastaEscolhidaPeloComponenteDaColecao.mensagemErro(
                "Não deu para abrir essa pasta.",
            )
        if (!pasta.exists()) {
            return ListagemDePastaEscolhidaPeloComponenteDaColecao.mensagemErro(
                "Essa pasta não existe mais.",
            )
        }
        if (!pasta.isDirectory) {
            return ListagemDePastaEscolhidaPeloComponenteDaColecao.mensagemErro(
                "Isso não é uma pasta.",
            )
        }
        val filhos = pasta.listFiles().orEmpty()
            .sortedWith(compareByDescending<DocumentFile> { it.isDirectory }.thenBy { it.name.orEmpty().lowercase() })
            .map { filho ->
                ItemDaPastaEscolhidaPeloComponente(
                    nome = filho.name.orEmpty().ifBlank { filho.uri.lastPathSegment.orEmpty() },
                    uri = filho.uri.toString(),
                    mime = filho.type.orEmpty(),
                    pasta = filho.isDirectory,
                    tamanho = if (filho.isFile) filho.length().coerceAtLeast(0L) else 0L,
                )
            }
        return ListagemDePastaEscolhidaPeloComponenteDaColecao.mensagemOk(
            nomePasta = pasta.name.orEmpty().ifBlank { "Pasta" },
            uriPasta = uri.toString(),
            itens = filhos,
        )
    }
}

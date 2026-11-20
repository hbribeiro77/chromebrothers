package com.chromebrothers.navegador.regras

data class ItemDaBibliotecaDeComponentesHtmlDaColecao(
    val id: String,
    val nome: String,
    val html: String,
    val atualizadoEm: Long,
    val descricao: String = "",
    val tags: List<String> = emptyList(),
    val larguraPx: Int? = null,
    val alturaPx: Int? = null,
)

sealed class ResultadoDaBibliotecaDeComponentesHtml {
    data class Ok(
        val item: ItemDaBibliotecaDeComponentesHtmlDaColecao,
        val criou: Boolean,
    ) : ResultadoDaBibliotecaDeComponentesHtml()

    data class Recusada(val mensagem: String) : ResultadoDaBibliotecaDeComponentesHtml()
}

sealed class ResultadoDasTagsDoComponente {
    data class Ok(val tags: List<String>) : ResultadoDasTagsDoComponente()
    data class Recusada(val mensagem: String) : ResultadoDasTagsDoComponente()
}

class BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador(
    inicial: List<ItemDaBibliotecaDeComponentesHtmlDaColecao> = emptyList(),
) {
    private val itens = inicial.toMutableList()

    fun listar(): List<ItemDaBibliotecaDeComponentesHtmlDaColecao> = itens.toList()

    fun buscar(id: String): ItemDaBibliotecaDeComponentesHtmlDaColecao? = itens.find { it.id == id }

    fun buscarPorNome(nome: String): ItemDaBibliotecaDeComponentesHtmlDaColecao? {
        val alvo = nome.trim()
        return itens.find { it.nome.equals(alvo, ignoreCase = true) }
    }

    fun guardar(
        nome: String,
        html: String,
        idExistente: String?,
        agora: Long,
        descricao: String? = null,
        tags: List<String>? = null,
        larguraPx: Int? = null,
        alturaPx: Int? = null,
        novoId: () -> String,
    ): ResultadoDaBibliotecaDeComponentesHtml {
        val nomeLimpo = nome.trim()
        val htmlLimpo = html.trim()
        val descricaoInformada = descricao?.trim()
        val tagsAvaliadas = tags?.let { limparTags(it) }
        val recusa = when {
            nomeLimpo.isEmpty() -> "Faltou o nome."
            nomeLimpo.length > LIMITE_DO_NOME -> "O nome passou do limite."
            descricaoInformada != null && descricaoInformada.length > LIMITE_DA_DESCRICAO ->
                "A descrição passou do limite."
            tagsAvaliadas is ResultadoDasTagsDoComponente.Recusada -> tagsAvaliadas.mensagem
            htmlLimpo.isEmpty() -> "Faltou o HTML."
            htmlLimpo.length > LIMITE_DO_HTML -> "O HTML passou do limite."
            else -> ""
        }
        if (recusa.isNotEmpty()) return ResultadoDaBibliotecaDeComponentesHtml.Recusada(recusa)
        val tagsLimpas = (tagsAvaliadas as? ResultadoDasTagsDoComponente.Ok)?.tags

        val indice = if (idExistente == null) -1 else itens.indexOfFirst { it.id == idExistente }
        if (indice >= 0) {
            val atual = itens[indice]
            val atualizado = atual.copy(
                nome = nomeLimpo,
                html = htmlLimpo,
                atualizadoEm = agora,
                descricao = descricaoInformada ?: atual.descricao,
                tags = tagsLimpas ?: atual.tags,
                larguraPx = dimensaoOuMantida(larguraPx, atual.larguraPx),
                alturaPx = dimensaoOuMantida(alturaPx, atual.alturaPx),
            )
            itens.removeAt(indice)
            itens.add(0, atualizado)
            return ResultadoDaBibliotecaDeComponentesHtml.Ok(atualizado, criou = false)
        }
        val criado = ItemDaBibliotecaDeComponentesHtmlDaColecao(
            id = novoId(),
            nome = nomeLimpo,
            html = htmlLimpo,
            atualizadoEm = agora,
            descricao = descricaoInformada.orEmpty(),
            tags = tagsLimpas.orEmpty(),
            larguraPx = larguraPx?.takeIf { it > 0 },
            alturaPx = alturaPx?.takeIf { it > 0 },
        )
        itens.add(0, criado)
        return ResultadoDaBibliotecaDeComponentesHtml.Ok(criado, criou = true)
    }

    fun atualizarDimensoes(id: String, larguraPx: Int, alturaPx: Int, agora: Long): Boolean {
        if (larguraPx <= 0 || alturaPx <= 0) return false
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0) return false
        val atual = itens[indice]
        itens[indice] = atual.copy(
            larguraPx = larguraPx,
            alturaPx = alturaPx,
            atualizadoEm = agora,
        )
        return true
    }

    fun apagar(id: String): Boolean = itens.removeAll { it.id == id }

    private fun dimensaoOuMantida(nova: Int?, atual: Int?): Int? {
        val candidata = nova?.takeIf { it > 0 }
        return candidata ?: atual?.takeIf { it > 0 }
    }

    fun substituirPor(novos: List<ItemDaBibliotecaDeComponentesHtmlDaColecao>) {
        itens.clear()
        itens.addAll(novos)
    }

    companion object {
        const val LIMITE_DO_NOME = 120
        const val LIMITE_DA_DESCRICAO = 500
        const val LIMITE_DE_TAGS = 8
        const val LIMITE_DO_TAMANHO_DA_TAG = 40
        const val LIMITE_DO_HTML = 10 * 1024 * 1024

        fun limparTags(brutas: List<String>): ResultadoDasTagsDoComponente {
            val limpas = brutas.map { it.trim() }.filter { it.isNotEmpty() }
            if (limpas.size > LIMITE_DE_TAGS) {
                return ResultadoDasTagsDoComponente.Recusada("As tags passaram do limite.")
            }
            if (limpas.any { it.length > LIMITE_DO_TAMANHO_DA_TAG }) {
                return ResultadoDasTagsDoComponente.Recusada("Uma tag passou do limite.")
            }
            return ResultadoDasTagsDoComponente.Ok(limpas)
        }
    }
}

package com.chromebrothers.navegador.regras

data class ItemDaBibliotecaDeVideos(
    val chave: String,
    val titulo: String,
    val enderecoParaAbrir: String,
    val abreNoPlayerDeArquivo: Boolean,
    val favorito: Boolean,
    val instante: Long,
    val nomeDaMiniatura: String,
    val foraDoHistorico: Boolean = false,
    val origem: String = OrigemDaMidiaNaBibliotecaDoNavegador.DESCONHECIDO,
    val modelo: String = "",
    val prompt: String = "",
    val refs: List<String> = emptyList(),
    val caminhoLocal: String = "",
    val exportado: Boolean = false,
    /** Receita da geração MCP (prompt, parâmetros e refs) para "Usar de novo". */
    val idDaReceita: String = "",
)

data class ResultadoDaRemocaoDoHistoricoDeVideos(
    val apagouOItem: Boolean,
    val nomeDaMiniatura: String,
)

class BibliotecaDeVideosDoNavegador(
    inicial: List<ItemDaBibliotecaDeVideos> = emptyList(),
    private val limiteDeRecentes: Int = 40,
) {
    private val itens = inicial.toMutableList()

    init {
        val vistos = mutableSetOf<String>()
        val unicos = mutableListOf<ItemDaBibliotecaDeVideos>()
        itens.forEach { item ->
            if (item.chave in vistos) return@forEach
            vistos += item.chave
            unicos += item
        }
        itens.clear()
        itens.addAll(unicos)
        aparar()
    }

    fun listar(): List<ItemDaBibliotecaDeVideos> = itens.toList()

    fun recentes(): List<ItemDaBibliotecaDeVideos> {
        return itens.filter { !it.foraDoHistorico }.take(limiteDeRecentes)
    }

    fun favoritos(): List<ItemDaBibliotecaDeVideos> = itens.filter { it.favorito }

    fun buscar(chave: String): ItemDaBibliotecaDeVideos? = itens.find { it.chave == chave }

    fun substituirPor(novos: List<ItemDaBibliotecaDeVideos>) {
        itens.clear()
        itens.addAll(novos)
        val vistos = mutableSetOf<String>()
        val unicos = mutableListOf<ItemDaBibliotecaDeVideos>()
        itens.forEach { item ->
            if (item.chave in vistos) return@forEach
            vistos += item.chave
            unicos += item
        }
        itens.clear()
        itens.addAll(unicos)
        aparar()
    }

    fun registrar(
        chave: String,
        titulo: String,
        enderecoParaAbrir: String,
        abreNoPlayerDeArquivo: Boolean,
        instante: Long,
        origem: String? = null,
        modelo: String? = null,
        prompt: String? = null,
        refs: List<String>? = null,
        caminhoLocal: String? = null,
        idDaReceita: String? = null,
    ): ItemDaBibliotecaDeVideos {
        val anterior = itens.find { it.chave == chave }
        itens.removeAll { it.chave == chave }
        val item = ItemDaBibliotecaDeVideos(
            chave = chave,
            titulo = titulo.trim().ifBlank { anterior?.titulo.orEmpty() }.ifBlank { enderecoParaAbrir },
            enderecoParaAbrir = enderecoParaAbrir,
            abreNoPlayerDeArquivo = abreNoPlayerDeArquivo,
            favorito = anterior?.favorito ?: false,
            instante = instante,
            nomeDaMiniatura = anterior?.nomeDaMiniatura.orEmpty(),
            foraDoHistorico = false,
            origem = origem?.let { OrigemDaMidiaNaBibliotecaDoNavegador.parse(it) }
                ?: anterior?.origem
                ?: OrigemDaMidiaNaBibliotecaDoNavegador.DESCONHECIDO,
            modelo = modelo ?: anterior?.modelo.orEmpty(),
            prompt = prompt ?: anterior?.prompt.orEmpty(),
            refs = refs?.toList() ?: anterior?.refs.orEmpty(),
            caminhoLocal = caminhoLocal ?: anterior?.caminhoLocal.orEmpty(),
            exportado = anterior?.exportado ?: false,
            idDaReceita = idDaReceita ?: anterior?.idDaReceita.orEmpty(),
        )
        itens.add(0, item)
        aparar()
        return itens.first { it.chave == chave }
    }

    fun definirFavorito(chave: String, favorito: Boolean): ItemDaBibliotecaDeVideos? {
        val indice = itens.indexOfFirst { it.chave == chave }
        if (indice < 0) return null
        itens[indice] = itens[indice].copy(favorito = favorito)
        aparar()
        return itens.find { it.chave == chave }
    }

    fun removerDoHistorico(chave: String): ResultadoDaRemocaoDoHistoricoDeVideos? {
        val indice = itens.indexOfFirst { it.chave == chave }
        if (indice < 0) return null
        val item = itens[indice]
        if (item.favorito) {
            itens[indice] = item.copy(foraDoHistorico = true)
            return ResultadoDaRemocaoDoHistoricoDeVideos(
                apagouOItem = false,
                nomeDaMiniatura = item.nomeDaMiniatura,
            )
        }
        itens.removeAt(indice)
        return ResultadoDaRemocaoDoHistoricoDeVideos(
            apagouOItem = true,
            nomeDaMiniatura = item.nomeDaMiniatura,
        )
    }

    fun definirMiniatura(chave: String, nomeDaMiniatura: String): ItemDaBibliotecaDeVideos? {
        val indice = itens.indexOfFirst { it.chave == chave }
        if (indice < 0) return null
        itens[indice] = itens[indice].copy(nomeDaMiniatura = nomeDaMiniatura)
        return itens[indice]
    }

    fun marcarExportado(chave: String): ItemDaBibliotecaDeVideos? {
        val indice = itens.indexOfFirst { it.chave == chave }
        if (indice < 0) return null
        itens[indice] = itens[indice].copy(exportado = true)
        return itens[indice]
    }

    private fun aparar() {
        val noHistorico = itens.filter { !it.foraDoHistorico }.take(limiteDeRecentes).map { it.chave }.toSet()
        itens.removeAll { !it.favorito && it.chave !in noHistorico }
    }
}

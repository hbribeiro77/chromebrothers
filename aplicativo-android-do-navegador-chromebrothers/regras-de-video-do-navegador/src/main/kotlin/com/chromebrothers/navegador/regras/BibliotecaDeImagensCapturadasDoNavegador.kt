package com.chromebrothers.navegador.regras

data class ItemDaBibliotecaDeImagensCapturadas(
    val id: String,
    val instante: Long,
    val nomeDoArquivo: String,
    val naGaleria: Boolean,
    val favorito: Boolean,
    val foraDoHistorico: Boolean = false,
    val uriDaGaleria: String? = null,
    val nomeExibido: String? = null,
    val origem: String = OrigemDaMidiaNaBibliotecaDoNavegador.DESCONHECIDO,
    val modelo: String = "",
    val prompt: String = "",
    val refs: List<String> = emptyList(),
    val caminhoLocal: String = "",
    val exportado: Boolean = false,
    /** Receita da geração MCP (prompt, parâmetros e refs) para "Usar de novo". */
    val idDaReceita: String = "",
)

data class ResultadoDaRemocaoDoHistoricoDeImagens(
    val apagouOItem: Boolean,
    val nomeDoArquivo: String,
)

class BibliotecaDeImagensCapturadasDoNavegador(
    inicial: List<ItemDaBibliotecaDeImagensCapturadas> = emptyList(),
    private val limiteDoHistorico: Int = 40,
) {
    private val itens = inicial.toMutableList()

    init {
        val vistos = mutableSetOf<String>()
        val unicos = mutableListOf<ItemDaBibliotecaDeImagensCapturadas>()
        itens.forEach { item ->
            if (item.id in vistos) return@forEach
            vistos += item.id
            unicos += item
        }
        itens.clear()
        itens.addAll(unicos)
        aparar()
    }

    fun listar(): List<ItemDaBibliotecaDeImagensCapturadas> = itens.toList()

    fun historico(): List<ItemDaBibliotecaDeImagensCapturadas> {
        return itens.filter { !it.foraDoHistorico }.take(limiteDoHistorico)
    }

    fun favoritos(): List<ItemDaBibliotecaDeImagensCapturadas> = itens.filter { it.favorito }

    fun copiadasSemBaixar(): List<ItemDaBibliotecaDeImagensCapturadas> = itens.filter { !it.naGaleria }

    fun buscar(id: String): ItemDaBibliotecaDeImagensCapturadas? = itens.find { it.id == id }

    fun substituirPor(novos: List<ItemDaBibliotecaDeImagensCapturadas>) {
        itens.clear()
        itens.addAll(novos)
        val vistos = mutableSetOf<String>()
        val unicos = mutableListOf<ItemDaBibliotecaDeImagensCapturadas>()
        itens.forEach { item ->
            if (item.id in vistos) return@forEach
            vistos += item.id
            unicos += item
        }
        itens.clear()
        itens.addAll(unicos)
        aparar()
    }

    fun registrar(
        id: String,
        instante: Long,
        nomeDoArquivo: String,
        naGaleria: Boolean,
        origem: String? = null,
        modelo: String? = null,
        prompt: String? = null,
        refs: List<String>? = null,
        caminhoLocal: String? = null,
        idDaReceita: String? = null,
    ): ItemDaBibliotecaDeImagensCapturadas {
        val anterior = itens.find { it.id == id }
        itens.removeAll { it.id == id }
        val item = ItemDaBibliotecaDeImagensCapturadas(
            id = id,
            instante = instante,
            nomeDoArquivo = nomeDoArquivo,
            naGaleria = naGaleria,
            favorito = anterior?.favorito ?: false,
            foraDoHistorico = false,
            uriDaGaleria = anterior?.uriDaGaleria,
            nomeExibido = anterior?.nomeExibido,
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
        return itens.first { it.id == id }
    }

    /** Atualiza só os metadados de geração, mantendo posição, instante e demais campos do item. */
    fun atualizarMetadadosDeGeracao(
        id: String,
        origem: String? = null,
        modelo: String? = null,
        prompt: String? = null,
        refs: List<String>? = null,
        caminhoLocal: String? = null,
        idDaReceita: String? = null,
    ): ItemDaBibliotecaDeImagensCapturadas? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0) return null
        val atual = itens[indice]
        itens[indice] = atual.copy(
            origem = origem?.let { OrigemDaMidiaNaBibliotecaDoNavegador.parse(it) } ?: atual.origem,
            modelo = modelo ?: atual.modelo,
            prompt = prompt ?: atual.prompt,
            refs = refs?.toList() ?: atual.refs,
            caminhoLocal = caminhoLocal ?: atual.caminhoLocal,
            idDaReceita = idDaReceita ?: atual.idDaReceita,
        )
        return itens[indice]
    }

    fun definirFavorito(id: String, favorito: Boolean): ItemDaBibliotecaDeImagensCapturadas? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0) return null
        itens[indice] = itens[indice].copy(favorito = favorito)
        aparar()
        return itens.find { it.id == id }
    }

    fun marcarComoBaixada(id: String): ItemDaBibliotecaDeImagensCapturadas? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0) return null
        itens[indice] = itens[indice].copy(naGaleria = true)
        return itens[indice]
    }

    fun vincularGaleria(id: String, uri: String, nome: String): ItemDaBibliotecaDeImagensCapturadas? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0 || uri.isBlank() || nome.isBlank()) return null
        itens[indice] = itens[indice].copy(naGaleria = true, uriDaGaleria = uri, nomeExibido = nome)
        return itens[indice]
    }

    fun marcarExportado(id: String): ItemDaBibliotecaDeImagensCapturadas? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0) return null
        itens[indice] = itens[indice].copy(exportado = true)
        return itens[indice]
    }

    fun renomear(id: String, nome: String): ItemDaBibliotecaDeImagensCapturadas? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0 || nome.isBlank()) return null
        val item = itens[indice]
        if (item.uriDaGaleria.isNullOrBlank()) return null
        itens[indice] = item.copy(nomeExibido = nome)
        return itens[indice]
    }

    fun removerDoHistorico(id: String): ResultadoDaRemocaoDoHistoricoDeImagens? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0) return null
        val item = itens[indice]
        if (item.favorito) {
            itens[indice] = item.copy(foraDoHistorico = true)
            return ResultadoDaRemocaoDoHistoricoDeImagens(
                apagouOItem = false,
                nomeDoArquivo = item.nomeDoArquivo,
            )
        }
        itens.removeAt(indice)
        return ResultadoDaRemocaoDoHistoricoDeImagens(
            apagouOItem = true,
            nomeDoArquivo = item.nomeDoArquivo,
        )
    }

    private fun aparar() {
        val noHistorico = itens.filter { !it.foraDoHistorico }.take(limiteDoHistorico).map { it.id }.toSet()
        itens.removeAll { !it.favorito && it.id !in noHistorico }
    }
}

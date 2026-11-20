package com.chromebrothers.navegador.regras

object FiltroPorOrigemNaBibliotecaDeMidia {
    fun filtrarVideos(
        itens: List<ItemDaBibliotecaDeVideos>,
        origemOuVazio: String,
    ): List<ItemDaBibliotecaDeVideos> {
        if (semFiltro(origemOuVazio)) return itens
        val origem = OrigemDaMidiaNaBibliotecaDoNavegador.parse(origemOuVazio)
        return itens.filter { OrigemDaMidiaNaBibliotecaDoNavegador.parse(it.origem) == origem }
    }

    fun filtrarImagens(
        itens: List<ItemDaBibliotecaDeImagensCapturadas>,
        origemOuVazio: String,
    ): List<ItemDaBibliotecaDeImagensCapturadas> {
        if (semFiltro(origemOuVazio)) return itens
        val origem = OrigemDaMidiaNaBibliotecaDoNavegador.parse(origemOuVazio)
        return itens.filter { OrigemDaMidiaNaBibliotecaDoNavegador.parse(it.origem) == origem }
    }

    private fun semFiltro(origemOuVazio: String): Boolean {
        val texto = origemOuVazio.trim()
        return texto.isEmpty() || texto.equals("todos", ignoreCase = true)
    }
}

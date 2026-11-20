package com.chromebrothers.navegador.regras

data class ItemDoMenuOverflowDoComponenteGravado(
    val id: String,
    val rotulo: String,
)

object ItensDoMenuOverflowDosComponentesGravadosDaColecao {
    const val LIMITE = 8

    fun montar(
        itens: List<ItemDaBibliotecaDeComponentesHtmlDaColecao>,
    ): List<ItemDoMenuOverflowDoComponenteGravado> {
        return itens
            .sortedByDescending { it.atualizadoEm }
            .take(LIMITE)
            .map { item ->
                ItemDoMenuOverflowDoComponenteGravado(
                    id = item.id,
                    rotulo = item.nome.trim().ifEmpty { item.id },
                )
            }
    }
}

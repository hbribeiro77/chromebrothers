package com.chromebrothers.navegador.regras

data class ItemDaPastaEscolhidaPeloComponente(
    val nome: String,
    val uri: String,
    val mime: String,
    val pasta: Boolean,
    val tamanho: Long,
)

object ListagemDePastaEscolhidaPeloComponenteDaColecao {
    const val LIMITE_DE_ITENS = 100

    fun mensagemCancelada(): String =
        """{"ok":false,"motivo":"cancelado"}"""

    fun mensagemErro(motivo: String): String =
        """{"ok":false,"motivo":"${escapar(motivo)}"}"""

    fun mensagemOk(
        nomePasta: String,
        uriPasta: String,
        itens: List<ItemDaPastaEscolhidaPeloComponente>,
        limiteDeItens: Int = LIMITE_DE_ITENS,
    ): String {
        val escolhidos = itens.take(limiteDeItens)
        val cortados = (itens.size - escolhidos.size).coerceAtLeast(0)
        val lista = escolhidos.joinToString(",") { item ->
            """{"nome":"${escapar(item.nome)}","uri":"${escapar(item.uri)}","mime":"${escapar(item.mime)}","pasta":${item.pasta},"tamanho":${item.tamanho}}"""
        }
        return """{"ok":true,"nome":"${escapar(nomePasta)}","uri":"${escapar(uriPasta)}","itens":[$lista],"cortados":$cortados,"totalEncontrado":${itens.size}}"""
    }

    private fun escapar(texto: String): String = buildString(texto.length + 8) {
        for (c in texto) {
            when (c) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
    }
}

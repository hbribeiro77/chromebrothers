package com.chromebrothers.navegador.regras

enum class EstiloDoTrechoDoMarkdownDoChat { NORMAL, NEGRITO, CODIGO, TITULO }

data class TrechoDoMarkdownDoChat(
    val texto: String,
    val estilo: EstiloDoTrechoDoMarkdownDoChat,
    val endereco: String = "",
)

object RenderizacaoDoMarkdownDoChat {
    fun renderizar(markdown: String): List<TrechoDoMarkdownDoChat> {
        val saida = mutableListOf<TrechoDoMarkdownDoChat>()
        val linhas = markdown.replace("\r\n", "\n").split('\n')
        var cerca = false
        val bloco = StringBuilder()
        fun soltarBloco() {
            if (bloco.isEmpty()) return
            saida += TrechoDoMarkdownDoChat(bloco.toString().trimEnd('\n'), EstiloDoTrechoDoMarkdownDoChat.CODIGO)
            bloco.clear()
        }
        linhas.forEach { linha ->
            if (linha.trim().startsWith("```")) {
                if (cerca) {
                    cerca = false
                    soltarBloco()
                } else {
                    cerca = true
                }
                return@forEach
            }
            if (cerca) {
                if (bloco.isNotEmpty()) bloco.append('\n')
                bloco.append(linha)
                return@forEach
            }
            if (saida.isNotEmpty()) saida += TrechoDoMarkdownDoChat("\n", EstiloDoTrechoDoMarkdownDoChat.NORMAL)
            val titulo = Regex("""^#{1,3}\s+(.*)$""").find(linha)
            if (titulo != null) {
                saida += inline(titulo.groupValues[1], EstiloDoTrechoDoMarkdownDoChat.TITULO)
                return@forEach
            }
            val item = Regex("""^[-*]\s+(.*)$""").find(linha)
            if (item != null) {
                saida += TrechoDoMarkdownDoChat("• ", EstiloDoTrechoDoMarkdownDoChat.NORMAL)
                saida += inline(item.groupValues[1], EstiloDoTrechoDoMarkdownDoChat.NORMAL)
                return@forEach
            }
            val numero = Regex("""^(\d+)\.\s+(.*)$""").find(linha)
            if (numero != null) {
                saida += TrechoDoMarkdownDoChat("${numero.groupValues[1]}. ", EstiloDoTrechoDoMarkdownDoChat.NORMAL)
                saida += inline(numero.groupValues[2], EstiloDoTrechoDoMarkdownDoChat.NORMAL)
                return@forEach
            }
            saida += inline(linha, EstiloDoTrechoDoMarkdownDoChat.NORMAL)
        }
        if (cerca) soltarBloco()
        return saida.filter { it.texto.isNotEmpty() }
    }

    private fun inline(linha: String, base: EstiloDoTrechoDoMarkdownDoChat): List<TrechoDoMarkdownDoChat> {
        val saida = mutableListOf<TrechoDoMarkdownDoChat>()
        val atual = StringBuilder()
        fun soltar() {
            if (atual.isEmpty()) return
            saida += TrechoDoMarkdownDoChat(atual.toString(), base)
            atual.clear()
        }
        var indice = 0
        while (indice < linha.length) {
            if (linha.startsWith("**", indice)) {
                val fim = linha.indexOf("**", indice + 2)
                if (fim > indice + 2) {
                    soltar()
                    saida += TrechoDoMarkdownDoChat(
                        linha.substring(indice + 2, fim),
                        EstiloDoTrechoDoMarkdownDoChat.NEGRITO,
                    )
                    indice = fim + 2
                    continue
                }
            }
            if (linha[indice] == '`') {
                val fim = linha.indexOf('`', indice + 1)
                if (fim > indice + 1) {
                    soltar()
                    saida += TrechoDoMarkdownDoChat(
                        linha.substring(indice + 1, fim),
                        EstiloDoTrechoDoMarkdownDoChat.CODIGO,
                    )
                    indice = fim + 1
                    continue
                }
            }
            if (linha[indice] == '[') {
                val achou = Regex("""^\[([^\]]+)\]\((https?://[^)\s]+)\)""").find(linha.substring(indice))
                if (achou != null) {
                    soltar()
                    saida += TrechoDoMarkdownDoChat(achou.groupValues[1], base, achou.groupValues[2])
                    indice += achou.value.length
                    continue
                }
            }
            atual.append(linha[indice])
            indice++
        }
        soltar()
        return saida
    }
}

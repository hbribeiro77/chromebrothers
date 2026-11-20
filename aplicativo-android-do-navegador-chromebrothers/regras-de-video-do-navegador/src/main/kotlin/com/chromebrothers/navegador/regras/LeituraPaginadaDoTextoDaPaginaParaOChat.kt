package com.chromebrothers.navegador.regras

data class PedidoDaLeituraPaginada(
    val offset: Int,
    val limite: Int,
)

data class FatiaDoTextoDaPagina(
    val corpo: String,
    val total: Int,
    val offset: Int,
    val proximoOffset: Int,
    val temMais: Boolean,
)

object LeituraPaginadaDoTextoDaPaginaParaOChat {
    const val LIMITE_PADRAO = 6000

    fun lerPedido(argumentos: String): PedidoDaLeituraPaginada {
        val offset = TextoJsonDoChat.inteiroDaChave(argumentos, "offset") ?: 0
        val limite = TextoJsonDoChat.inteiroDaChave(argumentos, "limite")
        return PedidoDaLeituraPaginada(
            offset = offset.coerceAtLeast(0),
            limite = limiteAceito(limite),
        )
    }

    fun fatiar(textoNormalizado: String, offset: Int, limite: Int): FatiaDoTextoDaPagina {
        val total = textoNormalizado.length
        val inicio = offset.coerceAtLeast(0)
        if (inicio >= total) {
            return FatiaDoTextoDaPagina(
                corpo = "",
                total = total,
                offset = inicio,
                proximoOffset = total,
                temMais = false,
            )
        }
        val fim = (inicio + limiteAceito(limite)).coerceAtMost(total)
        return FatiaDoTextoDaPagina(
            corpo = textoNormalizado.substring(inicio, fim),
            total = total,
            offset = inicio,
            proximoOffset = fim,
            temMais = fim < total,
        )
    }

    fun mensagem(titulo: String, endereco: String, fatia: FatiaDoTextoDaPagina): String {
        val nome = titulo.trim().ifBlank { "Sem título" }
        val url = endereco.trim().ifBlank { "sem endereço" }
        val temMais = if (fatia.temMais) "sim" else "nao"
        val meta = "total=${fatia.total} offset=${fatia.offset} proximo_offset=${fatia.proximoOffset} tem_mais=$temMais"
        val cabeca = "Página aberta: $nome\nEndereço: $url\n\n$meta"
        return if (fatia.corpo.isEmpty()) cabeca else "$cabeca\n\n${fatia.corpo}"
    }

    /** Colapsa espaços sem Regex (Android não aceita `(?U)`); inclui NBSP e outros separadores Unicode. */
    fun normalizar(bruto: String): String {
        val saida = StringBuilder(bruto.length)
        var emEspaco = false
        for (c in bruto) {
            if (Character.isWhitespace(c) || Character.isSpaceChar(c)) {
                emEspaco = true
            } else {
                if (emEspaco && saida.isNotEmpty()) saida.append(' ')
                emEspaco = false
                saida.append(c)
            }
        }
        return saida.toString()
    }

    private fun limiteAceito(limite: Int?): Int {
        if (limite == null || limite < 1) return LIMITE_PADRAO
        return limite.coerceAtMost(LIMITE_PADRAO)
    }
}

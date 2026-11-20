package com.chromebrothers.navegador.regras

/**
 * Players como o do VK/Boosty pedem o arquivo em pedaços pondo a faixa no próprio endereço
 * (`...&bytes=0-524287`). Para baixar o arquivo inteiro (e não repetir 200 vezes o mesmo vídeo na
 * lista), o endereço fica sem esse parâmetro de faixa.
 */
object NormalizacaoDoEnderecoDeMidia {
    private val nomesDeFaixa = setOf("bytes", "range")
    private val valorDeFaixa = Regex("""^\d+-\d*$""")

    fun semFaixaNaQuery(endereco: String): String {
        val interrogacao = endereco.indexOf('?')
        if (interrogacao < 0) return endereco
        val base = endereco.substring(0, interrogacao)
        val resto = endereco.substring(interrogacao + 1)
        val ancora = resto.indexOf('#').takeIf { it >= 0 }
        val query = if (ancora != null) resto.substring(0, ancora) else resto
        val fragmento = if (ancora != null) resto.substring(ancora) else ""
        val partes = query.split('&').filter { parte ->
            val nome = parte.substringBefore('=').lowercase()
            val valor = parte.substringAfter('=', "")
            !(nome in nomesDeFaixa && valorDeFaixa.matches(valor))
        }
        if (partes.size == query.split('&').size) return endereco
        return base + (if (partes.isEmpty()) "" else "?" + partes.joinToString("&")) + fragmento
    }
}

package com.chromebrothers.navegador.regras

object MenuDoToqueLongoNoLinkDaPagina {
    fun endereco(ehImagem: Boolean, link: String?): String? {
        if (ehImagem) return null
        val texto = link?.trim().orEmpty()
        if (texto.startsWith("https://") || texto.startsWith("http://")) return texto
        return null
    }
}

package com.chromebrothers.navegador.regras

object PaletaDoEditorDeImagem {
    const val BRANCO = 0xFFFFFFFF.toInt()
    const val PRETO = 0xFF000000.toInt()
    const val VERMELHO = 0xFFE53935.toInt()
    const val AZUL = 0xFF1A73E8.toInt()
    const val AMARELO = 0xFFF9A825.toInt()
    const val VERDE = 0xFF34A853.toInt()
    const val corDaFolhaInicial = BRANCO
    val cores = intArrayOf(BRANCO, PRETO, VERMELHO, AZUL, AMARELO, VERDE)

    fun indice(cor: Int): Int {
        val opaca = cor or 0xFF000000.toInt()
        val achada = cores.indexOf(opaca)
        return if (achada < 0) 0 else achada
    }

    fun posicao(cor: Int): Int = cores.indexOf(cor or 0xFF000000.toInt())

    fun comOpacidade(cor: Int, marcador: Boolean): Int {
        val alpha = if (marcador) 102 else 255
        return (cor and 0x00FFFFFF) or (alpha shl 24)
    }
}

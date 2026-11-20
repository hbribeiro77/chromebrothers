package com.chromebrothers.navegador.regras

/**
 * Hold-to-erase estilo Samsung Notes: com o botão lateral da S Pen pressionado,
 * a ponta apaga; ao soltar, o traço volta ao último pincel (a barra não precisa
 * ficar em “borracha”).
 *
 * Máscaras alinhadas a [android.view.MotionEvent] (BUTTON_STYLUS_* / BUTTON_SECONDARY).
 */
object BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem {
    const val BOTAO_SECUNDARIO = 1 shl 1
    const val BOTAO_STYLUS_PRIMARIO = 1 shl 5
    const val BOTAO_STYLUS_SECUNDARIO = 1 shl 6

    fun botaoLateralPressionado(buttonState: Int): Boolean {
        val estado = buttonState
        return (estado and BOTAO_STYLUS_PRIMARIO) != 0 ||
            (estado and BOTAO_STYLUS_SECUNDARIO) != 0 ||
            (estado and BOTAO_SECUNDARIO) != 0
    }

    /** Transição apertado → solto (também quando o SO só atualiza buttonState no MOVE/UP). */
    fun soltouBotaoLateral(estavaPressionado: Boolean, estaPressionado: Boolean): Boolean =
        estavaPressionado && !estaPressionado

    fun tracoEhBorracha(
        ferramentaEhBorrachaNaBarra: Boolean,
        pontaFisicaEhBorracha: Boolean,
        botaoLateralPressionado: Boolean,
    ): Boolean =
        ferramentaEhBorrachaNaBarra || pontaFisicaEhBorracha || botaoLateralPressionado

    /**
     * Com botão lateral ou ponta física de borracha, a caneta apaga/risca
     * mesmo com seleção de recorte ou moldura de transformar ativas.
     */
    fun canetaPriorizaTracoSobreSelecaoOuTransformar(
        botaoLateralPressionado: Boolean,
        pontaFisicaEhBorracha: Boolean,
    ): Boolean = botaoLateralPressionado || pontaFisicaEhBorracha

    /**
     * Se a borracha era só pelo botão, o “modo efetivo” volta ao pincel lembrado
     * (também ao sair do recorte/seleção). Se a barra está em borracha de propósito, não troca.
     */
    fun pincelAposSoltarBotao(
        ferramentaDaBarraEhBorracha: Boolean,
        idDoUltimoPincel: String?,
    ): String? {
        if (ferramentaDaBarraEhBorracha) return null
        return idDoUltimoPincel
    }
}

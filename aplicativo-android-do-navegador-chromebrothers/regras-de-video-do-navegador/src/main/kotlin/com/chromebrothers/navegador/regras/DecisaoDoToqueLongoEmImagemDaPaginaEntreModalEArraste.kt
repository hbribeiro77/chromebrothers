package com.chromebrothers.navegador.regras

/**
 * Toque longo em `<img>` da página:
 * - sem arrastar (dedo quase parado) → modal copiar/salvar/etc.
 * - com arrastar além do limiar → fantasma / colar / editor.
 */
object DecisaoDoToqueLongoEmImagemDaPaginaEntreModalEArraste {
    /** ~mesmo limiar do cancelamento da espera no JS (14 CSS px ≈ 28 device px em dens. 2). */
    const val LIMIAR_DE_ARRASTE_PX: Float = 28f

    fun deveAbrirModalEmVezDeSoltarArraste(
        xInicio: Float,
        yInicio: Float,
        xFim: Float,
        yFim: Float,
        limiarPx: Float = LIMIAR_DE_ARRASTE_PX,
    ): Boolean {
        val dx = xFim - xInicio
        val dy = yFim - yInicio
        return dx * dx + dy * dy <= limiarPx * limiarPx
    }
}

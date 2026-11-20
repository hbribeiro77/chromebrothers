package com.chromebrothers.navegador.regras

/**
 * O GeckoView pisca branco se o layout mudar de altura a cada frame do IME.
 * Por isso a raiz só pade as barras do sistema; a página sobe com translationY
 * e a faixa inferior mostra o fundo da área (sem redimensionar a surface).
 *
 * O deslocamento não é a altura inteira do teclado: sobe só o bastante para o
 * campo focado ficar acima da linha do IME. A conta usa o viewport CSS da página
 * e escala para pixels da área — sem multiplicar densidade à parte.
 */
object EspacoDoTecladoSobreAPaginaDoNavegador {
    fun paddingInferior(barraDoSistema: Int, teclado: Int): Int = maxOf(barraDoSistema, teclado)

    /** Padding da raiz: ignora o teclado para não encolher o GeckoView. */
    fun paddingInferiorDaRaiz(barraDoSistema: Int): Int = barraDoSistema.coerceAtLeast(0)

    fun folgaExtra(barraDoSistema: Int, teclado: Int): Int =
        (teclado - barraDoSistema).coerceAtLeast(0)

    /**
     * Deslocamento cheio pela folga do teclado (legado / fallback).
     * Preferir [deslocamentoVerticalComFocoNoViewport] quando a base do campo for conhecida.
     */
    fun deslocamentoVerticalDaPagina(barraDoSistema: Int, teclado: Int): Float {
        val folga = folgaExtra(barraDoSistema, teclado)
        return if (folga == 0) 0f else -folga.toFloat()
    }

    fun deslocamentoParaManterFocoVisivel(
        baseDoFoco: Float,
        topoDoTeclado: Float,
        margem: Float = 16f,
    ): Float {
        val cobertura = baseDoFoco + margem - topoDoTeclado
        return if (cobertura <= 0f) 0f else -cobertura
    }

    fun deslocamentoLimitadoPelaFolgaDoTeclado(deslocamento: Float, folgaDoTeclado: Float): Float {
        val folga = folgaDoTeclado.coerceAtLeast(0f)
        return deslocamento.coerceIn(-folga, 0f)
    }

    fun deslocamentoVerticalComFoco(
        baseDoFoco: Float?,
        alturaDaArea: Float,
        folgaDoTeclado: Float,
        margem: Float = 16f,
    ): Float {
        val folga = folgaDoTeclado.coerceAtLeast(0f)
        if (folga == 0f || baseDoFoco == null) return 0f
        val topoDoTeclado = alturaDaArea - folga
        val desejado = deslocamentoParaManterFocoVisivel(
            baseDoFoco = baseDoFoco,
            topoDoTeclado = topoDoTeclado,
            margem = margem,
        )
        return deslocamentoLimitadoPelaFolgaDoTeclado(desejado, folga)
    }

    fun deslocamentoVerticalComFocoNoViewport(
        baseDoFocoCss: Float?,
        alturaDoViewportCss: Float?,
        folgaDoTecladoPx: Float,
        alturaDaAreaPx: Float,
        margemCss: Float = 16f,
    ): Float {
        val folga = folgaDoTecladoPx.coerceAtLeast(0f)
        val viewport = alturaDoViewportCss ?: return 0f
        val base = baseDoFocoCss ?: return 0f
        if (folga == 0f || viewport <= 0f || alturaDaAreaPx <= 0f) return 0f
        val escala = alturaDaAreaPx / viewport
        val folgaCss = folga / escala
        val topoCss = viewport - folgaCss
        val desejadoCss = deslocamentoParaManterFocoVisivel(
            baseDoFoco = base,
            topoDoTeclado = topoCss,
            margem = margemCss,
        )
        return deslocamentoLimitadoPelaFolgaDoTeclado(desejadoCss * escala, folga)
    }

    fun interpolar(inicio: Int, fim: Int, fracao: Float): Int {
        val t = fracao.coerceIn(0f, 1f)
        return (inicio + (fim - inicio) * t).toInt()
    }
}

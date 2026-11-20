package com.chromebrothers.navegador.regras

/**
 * Parâmetros da animação de abrir/fechar a lista (deslize + fade leve).
 */
object AnimacaoDaListaDeSugestoesDaBarraDeEnderecoDoNavegador {
    const val DURACAO_ABRIR_MS = 200L
    const val DURACAO_FECHAR_MS = 160L
    const val DESLOCAMENTO_MINIMO_DP = 28f
    const val FRACAO_DA_ALTURA = 0.4f

    fun deslocamentoInicialEmPixels(alturaDaListaPx: Int, densidade: Float): Float {
        val porAltura = if (alturaDaListaPx > 0) {
            -(alturaDaListaPx * FRACAO_DA_ALTURA)
        } else {
            0f
        }
        val minimo = -(DESLOCAMENTO_MINIMO_DP * densidade)
        return minOf(porAltura, minimo)
    }

    fun deveAnimarAbertura(listaJaVisivel: Boolean): Boolean = !listaJaVisivel
}

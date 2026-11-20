package com.chromebrothers.navegador.regras

import kotlin.math.abs

/**
 * Swipe que troca de vídeo no player embutido ao lado do componente, sem roubar os gestos do player:
 * toque e toque duplo não se movem; o scrub fino nasce do toque longo (o arraste vem depois da espera);
 * a barra de progresso consome o próprio toque antes de chegar aqui.
 */
object RegrasDoSwipeEntreVideosNoPlayerEmbutidoDoVisorDaColecao {
    private const val DOMINANCIA_HORIZONTAL = 1.3f

    enum class Gesto { INDEFINIDO, SWIPE, DEIXAR_PARA_O_PLAYER }

    fun decidir(
        dx: Float,
        dy: Float,
        folga: Float,
        decorridoMs: Long,
        esperaDoToqueLongoMs: Long,
        scrubAtivo: Boolean,
        ponteiros: Int,
    ): Gesto {
        if (scrubAtivo || ponteiros > 1) return Gesto.DEIXAR_PARA_O_PLAYER
        if (!RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao.moveuAlemDaFolga(dx, dy, folga)) return Gesto.INDEFINIDO
        if (decorridoMs >= esperaDoToqueLongoMs) return Gesto.DEIXAR_PARA_O_PLAYER
        return if (abs(dx) >= abs(dy) * DOMINANCIA_HORIZONTAL) Gesto.SWIPE else Gesto.DEIXAR_PARA_O_PLAYER
    }

    fun textoDaPosicao(indice: Int, total: Int): String =
        if (total <= 0) "" else "${indice.coerceIn(0, total - 1) + 1} de $total"
}

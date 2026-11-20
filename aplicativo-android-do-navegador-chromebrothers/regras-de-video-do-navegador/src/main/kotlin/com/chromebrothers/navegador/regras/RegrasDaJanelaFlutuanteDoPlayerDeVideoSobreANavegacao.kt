package com.chromebrothers.navegador.regras

/**
 * Posição e tamanho da janela flutuante (picture-in-picture dentro do app) do player de vídeo.
 * Tudo em px; a janela nunca sai da área das páginas.
 */
object RegrasDaJanelaFlutuanteDoPlayerDeVideoSobreANavegacao {
    const val LARGURA_MINIMA_DP: Int = 300
    const val ALTURA_MINIMA_DP: Int = 190
    const val LARGURA_INICIAL_MAXIMA_DP: Int = 480
    const val MARGEM_DP: Int = 16

    data class Retangulo(val x: Int, val y: Int, val largura: Int, val altura: Int)

    /** Canto inferior direito, ~45% da largura (vídeo 16:9 + alça), sem passar de 480dp. */
    fun inicial(larguraDaArea: Int, alturaDaArea: Int, densidade: Float, alturaDaAlca: Int): Retangulo {
        val largura = minOf((larguraDaArea * 0.45f).toInt(), (LARGURA_INICIAL_MAXIMA_DP * densidade).toInt())
        val altura = largura * 9 / 16 + alturaDaAlca
        val margem = (MARGEM_DP * densidade).toInt()
        return ajustar(
            Retangulo(larguraDaArea - largura - margem, alturaDaArea - altura - margem, largura, altura),
            larguraDaArea,
            alturaDaArea,
            densidade,
        )
    }

    /** Tamanho entre o mínimo e a área; posição puxada para dentro da área. */
    fun ajustar(r: Retangulo, larguraDaArea: Int, alturaDaArea: Int, densidade: Float): Retangulo {
        val minimaL = minOf((LARGURA_MINIMA_DP * densidade).toInt(), larguraDaArea.coerceAtLeast(1))
        val minimaA = minOf((ALTURA_MINIMA_DP * densidade).toInt(), alturaDaArea.coerceAtLeast(1))
        val largura = r.largura.coerceIn(minimaL, larguraDaArea.coerceAtLeast(minimaL))
        val altura = r.altura.coerceIn(minimaA, alturaDaArea.coerceAtLeast(minimaA))
        val x = r.x.coerceIn(0, (larguraDaArea - largura).coerceAtLeast(0))
        val y = r.y.coerceIn(0, (alturaDaArea - altura).coerceAtLeast(0))
        return Retangulo(x, y, largura, altura)
    }

    fun mover(r: Retangulo, dx: Int, dy: Int, larguraDaArea: Int, alturaDaArea: Int, densidade: Float): Retangulo =
        ajustar(r.copy(x = r.x + dx, y = r.y + dy), larguraDaArea, alturaDaArea, densidade)

    /** Redimensiona pelo canto inferior direito: o canto superior esquerdo fica parado. */
    fun redimensionar(r: Retangulo, dx: Int, dy: Int, larguraDaArea: Int, alturaDaArea: Int, densidade: Float): Retangulo {
        val maxL = (larguraDaArea - r.x).coerceAtLeast(1)
        val maxA = (alturaDaArea - r.y).coerceAtLeast(1)
        val minimaL = minOf((LARGURA_MINIMA_DP * densidade).toInt(), maxL)
        val minimaA = minOf((ALTURA_MINIMA_DP * densidade).toInt(), maxA)
        return r.copy(
            largura = (r.largura + dx).coerceIn(minimaL, maxL),
            altura = (r.altura + dy).coerceIn(minimaA, maxA),
        )
    }

    fun paraTexto(r: Retangulo): String = "${r.x},${r.y},${r.largura},${r.altura}"

    fun deTexto(texto: String?): Retangulo? {
        val partes = texto?.split(',')?.mapNotNull { it.trim().toIntOrNull() } ?: return null
        if (partes.size != 4 || partes[2] <= 0 || partes[3] <= 0) return null
        return Retangulo(partes[0], partes[1], partes[2], partes[3])
    }
}

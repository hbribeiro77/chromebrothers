package com.chromebrothers.navegador.regras

/**
 * Abertura: blocos-esqueleto borrados/translúcidos animam o split.
 * Fechamento: deslize simples da gaveta (sem blocos).
 */
object AnimacaoDaGavetaDoChatDoNavegador {
    const val DURACAO_MS = 320L
    /** Crossfade dos blocos → conteúdo real na abertura. */
    const val DURACAO_DO_CROSSFADE_MS = 180L
    /** Entrada suave da máscara de blocos. */
    const val DURACAO_DA_ENTRADA_DOS_BLOCOS_MS = 140L
    /** Folga após o split, antes de começar a sumir os blocos. */
    const val ESPERA_ANTES_DO_CROSSFADE_MS = 200L
    const val RAIO_DO_BLUR_DOS_BLOCOS = 26f
    /** 99% opaco. */
    const val ALPHA_DOS_BLOCOS_ESQUELETO = 0.99f
    /** Cor do bloco do chat (#1A1D24). */
    const val COR_DO_BLOCO_DO_CHAT = 0xFF1A1D24.toInt()
    /** Barrinha split visual (#E8ECF2). */
    const val COR_DA_BARRINHA_SPLIT = 0xFFE8ECF2.toInt()
    const val ALPHA_DA_LINHA_DA_BARRINHA_SPLIT = 0.60f
    const val ALPHA_DA_PILULA_DA_BARRINHA_SPLIT = 0.80f
    const val LARGURA_DA_LINHA_DA_BARRINHA_SPLIT_DP = 2f
    const val LARGURA_DA_PILULA_DA_BARRINHA_SPLIT_DP = 4f
    const val ALTURA_DA_PILULA_DA_BARRINHA_SPLIT_DP = 36f
    const val DURACAO_DO_FADE_DA_BARRINHA_SPLIT_MS = 180L
    private const val LARGURA_PREFERIDA_DP = 380
    private const val FRACAO_MAXIMA_DO_PAI = 0.46f

    data class GeometriaDosBlocos(
        val larguraDaPagina: Int,
        val larguraDoChat: Int,
    )

    /**
     * Centro X da barrinha na junção página ↔ chat, em float contínuo
     * (não usa geometria inteira — evita trepidação a ~1 px/frame).
     */
    fun posicaoXDaBarrinhaSplit(
        larguraDaFaixa: Int,
        larguraDoChatAlvo: Int,
        fracao: Float,
    ): Float {
        if (larguraDaFaixa <= 0) return 0f
        val t = fracao.coerceIn(0f, 1f)
        val chat = larguraDoChatAlvo.coerceAtLeast(0) * t
        return (larguraDaFaixa - chat).coerceIn(0f, larguraDaFaixa.toFloat())
    }

    fun calcularLarguraAlvo(densidade: Float, larguraDoPai: Int): Int {
        val preferida = (LARGURA_PREFERIDA_DP * densidade).toInt()
        if (larguraDoPai <= 0) return preferida
        val maximo = (larguraDoPai * FRACAO_MAXIMA_DO_PAI).toInt()
        return preferida.coerceAtMost(maximo)
    }

    fun margemAoLadoDoSite(): Int = 0

    fun margemDeSobreposicao(larguraAlvo: Int): Int = -larguraAlvo.coerceAtLeast(0)

    fun translationAberta(): Float = 0f

    fun translationAoFechar(larguraAtual: Int): Float = larguraAtual.coerceAtLeast(0).toFloat()

    /** Chat real desliza junto com os blocos (fracao 0 = fora; 1 = no lugar). */
    fun translationDoChatDuranteAbertura(larguraAlvo: Int, fracao: Float): Float {
        val t = fracao.coerceIn(0f, 1f)
        return larguraAlvo.coerceAtLeast(0) * (1f - t)
    }

    /**
     * @param fracao 0 = só página; 1 = página encolhida + chat na largura alvo.
     */
    fun geometriaDosBlocos(
        larguraDaFaixa: Int,
        larguraDoChatAlvo: Int,
        fracao: Float,
    ): GeometriaDosBlocos {
        if (larguraDaFaixa <= 0) return GeometriaDosBlocos(0, 0)
        val t = fracao.coerceIn(0f, 1f)
        val chat = (larguraDoChatAlvo.coerceAtLeast(0) * t).toInt()
            .coerceAtMost(larguraDaFaixa)
        val pagina = (larguraDaFaixa - chat).coerceAtLeast(0)
        return GeometriaDosBlocos(larguraDaPagina = pagina, larguraDoChat = chat)
    }
}

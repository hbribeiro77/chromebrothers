package com.chromebrothers.navegador.regras

import kotlin.math.abs

/**
 * Gestos do visor em tela cheia que abre quando o Visualizador de mídia está na gaveta do chat.
 * A grade fica na gaveta; este visor só decide zoom, troca de imagem e se a carta precisa rolar até a moldura.
 */
object RegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat {
    const val ZOOM_MINIMO = 1f
    const val ZOOM_MAXIMO = 4f
    const val LADO_MAXIMO_DA_DECODIFICACAO = 2048
    private const val FOLGA_DE_ZOOM = 0.02f
    private const val FRACAO_DA_LARGURA_PARA_TROCAR = 0.18f
    private const val VELOCIDADE_MINIMA_PARA_TROCAR = 900f
    private const val DESLOCAMENTO_MINIMO_NO_FLING = 36f
    private const val RESISTENCIA_NA_BORDA = 0.35f

    enum class Troca { FICAR, ANTERIOR, PROXIMA }

    fun deveAbrirForaDaGaveta(ancoradoNaGaveta: Boolean): Boolean = ancoradoNaGaveta

    fun zoomAposPinca(atual: Float, fator: Float): Float =
        (atual * fator).coerceIn(ZOOM_MINIMO, ZOOM_MAXIMO)

    fun estaAmpliada(zoom: Float): Boolean = zoom > ZOOM_MINIMO + FOLGA_DE_ZOOM

    fun trocaAoSoltar(deslocamentoX: Float, largura: Float, velocidadeX: Float, zoom: Float): Troca {
        if (estaAmpliada(zoom) || largura <= 0f) return Troca.FICAR
        val distancia = abs(deslocamentoX) > largura * FRACAO_DA_LARGURA_PARA_TROCAR
        val fling = abs(velocidadeX) >= VELOCIDADE_MINIMA_PARA_TROCAR &&
            abs(deslocamentoX) >= DESLOCAMENTO_MINIMO_NO_FLING
        if (!distancia && !fling) return Troca.FICAR
        return if (deslocamentoX < 0f) Troca.PROXIMA else Troca.ANTERIOR
    }

    fun indiceAposTroca(indice: Int, total: Int, troca: Troca): Int {
        if (total <= 0) return 0
        val destino = when (troca) {
            Troca.FICAR -> indice
            Troca.ANTERIOR -> indice - 1
            Troca.PROXIMA -> indice + 1
        }
        return destino.coerceIn(0, total - 1)
    }

    /** Na primeira imagem o arraste para a direita cede; na última, o arraste para a esquerda. */
    fun deslocamentoComBorda(deslocamento: Float, indice: Int, total: Int, largura: Float): Float {
        if (largura <= 0f) return 0f
        val semAnterior = indice <= 0 && deslocamento > 0f
        val semProxima = total <= 0 || (indice >= total - 1 && deslocamento < 0f)
        return if (semAnterior || semProxima) deslocamento * RESISTENCIA_NA_BORDA else deslocamento
    }

    fun panLimitado(pan: Float, zoom: Float, tamanho: Float): Float {
        if (!estaAmpliada(zoom) || tamanho <= 0f) return 0f
        val folga = tamanho * (zoom - ZOOM_MINIMO) / 2f
        return pan.coerceIn(-folga, folga)
    }

    fun deveDispararToqueLongo(dx: Float, dy: Float, folga: Float, zoom: Float): Boolean =
        !estaAmpliada(zoom) &&
            !RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao.moveuAlemDaFolga(dx, dy, folga)

    fun deveRolarGradeAteACarta(posicao: Int, primeiraCompletamenteVisivel: Int, ultimaCompletamenteVisivel: Int): Boolean {
        if (posicao < 0) return false
        if (primeiraCompletamenteVisivel < 0 || ultimaCompletamenteVisivel < 0) return true
        return posicao < primeiraCompletamenteVisivel || posicao > ultimaCompletamenteVisivel
    }

    fun ladoAlvoDaDecodificacao(maiorLadoDaTela: Int): Int =
        maiorLadoDaTela.coerceIn(720, LADO_MAXIMO_DA_DECODIFICACAO)

    /** Potência de 2 que deixa o lado maior ainda pelo menos [ladoAlvo]. */
    fun amostraParaCaber(largura: Int, altura: Int, ladoAlvo: Int): Int {
        if (largura <= 0 || altura <= 0 || ladoAlvo <= 0) return 1
        var amostra = 1
        while (largura / (amostra * 2) >= ladoAlvo && altura / (amostra * 2) >= ladoAlvo) {
            amostra *= 2
        }
        return amostra
    }
}

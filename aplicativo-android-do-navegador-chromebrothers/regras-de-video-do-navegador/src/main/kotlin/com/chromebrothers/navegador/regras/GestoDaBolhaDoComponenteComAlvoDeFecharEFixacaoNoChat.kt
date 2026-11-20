package com.chromebrothers.navegador.regras

/**
 * Bolha de componente flutuante (estilo bolhas da Samsung):
 * - toque curto abre a janela;
 * - toque longo mostra o alvo "✕" embaixo; soltar sobre ele fecha;
 * - parada [ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.ATRASO_MS] sobre a faixa de abas do chat já fixa.
 */
object GestoDaBolhaDoComponenteComAlvoDeFecharEFixacaoNoChat {
    /** O alvo "pega" o dedo um pouco além do próprio círculo. */
    const val FATOR_DO_IMA_DO_ALVO: Float = 1.6f

    enum class AcaoAoSoltarABolha { NADA, ABRIR, FECHAR, FIXAR_NO_CHAT, SO_MOVER }

    fun sobreOAlvoDeFechar(
        xDedo: Float,
        yDedo: Float,
        xCentroDoAlvo: Float,
        yCentroDoAlvo: Float,
        raioDoAlvo: Float,
    ): Boolean {
        if (raioDoAlvo <= 0f) return false
        val dx = xDedo - xCentroDoAlvo
        val dy = yDedo - yCentroDoAlvo
        val alcance = raioDoAlvo * FATOR_DO_IMA_DO_ALVO
        return dx * dx + dy * dy <= alcance * alcance
    }

    fun acaoAoSoltar(
        moveu: Boolean,
        toqueLongo: Boolean,
        jaFixou: Boolean,
        sobreOAlvoDeFechar: Boolean,
        armadaNaFaixaDoChat: Boolean,
    ): AcaoAoSoltarABolha = when {
        jaFixou -> AcaoAoSoltarABolha.NADA
        toqueLongo && sobreOAlvoDeFechar -> AcaoAoSoltarABolha.FECHAR
        armadaNaFaixaDoChat -> AcaoAoSoltarABolha.FIXAR_NO_CHAT
        !moveu && !toqueLongo -> AcaoAoSoltarABolha.ABRIR
        moveu -> AcaoAoSoltarABolha.SO_MOVER
        else -> AcaoAoSoltarABolha.NADA
    }
}

package com.chromebrothers.navegador.regras

/**
 * Componente entre o chat e a tela, por arraste:
 * - aba da faixa arrastada para fora da gaveta → vira janela flutuante (soltar na faixa reordena);
 * - janela flutuante arrastada pela barra até a faixa → soltar ali fixa no chat na hora;
 * - a bolha parada sobre a faixa por [ATRASO_MS] ("armada") fixa sozinha, sem precisar soltar.
 */
object ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante {
    const val ATRASO_MS: Long = TrocaDeAbaPorPairarFantasmaDoArrasteDoNavegador.ATRASO_MS

    /** CONSULTAR: ainda arrastando, só pergunta se já está armada (a bolha fixa sem precisar soltar). */
    enum class FaseDoArrasteDaJanelaPelaBarra { MOVENDO, CONSULTAR, SOLTOU, CANCELOU }

    enum class DestinoDaAbaArrastada { DESVINCULAR, REORDENAR, VOLTAR }

    fun destinoDaAbaArrastada(dentroDaGaveta: Boolean, sobreAFaixa: Boolean): DestinoDaAbaArrastada = when {
        !dentroDaGaveta -> DestinoDaAbaArrastada.DESVINCULAR
        sobreAFaixa -> DestinoDaAbaArrastada.REORDENAR
        else -> DestinoDaAbaArrastada.VOLTAR
    }

    data class JanelaSobreAFaixa(val desdeMs: Long = 0L)

    fun atualizarJanelaSobreAFaixa(
        estado: JanelaSobreAFaixa,
        sobreAFaixa: Boolean,
        aceita: Boolean,
        agoraMs: Long,
    ): JanelaSobreAFaixa = when {
        !sobreAFaixa || !aceita -> JanelaSobreAFaixa()
        estado.desdeMs > 0L -> estado
        else -> JanelaSobreAFaixa(desdeMs = agoraMs.coerceAtLeast(1L))
    }

    fun estaArmada(estado: JanelaSobreAFaixa, agoraMs: Long): Boolean =
        estado.desdeMs > 0L && agoraMs - estado.desdeMs >= ATRASO_MS

    /** Soltar é escolha clara: sobre a faixa (e aceita), fixa sem esperar o [ATRASO_MS]. */
    fun soltarFixa(sobreAFaixa: Boolean, aceita: Boolean): Boolean = sobreAFaixa && aceita
}

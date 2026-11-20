package com.chromebrothers.navegador.regras

enum class DestinoDoToqueAposMoverComMolduraDeTransformar {
    GESTO_DA_PECA,
    ENCERRAR_MOLDURA,
    FLUXO_NORMAL,
}

/**
 * Depois do Mover (moldura de transformar ativa): só gesto da peça ou encerrar;
 * caneta e dedo não pintam nem começam novo recorte.
 */
object ToqueAposMoverComMolduraDeTransformarDaPeca {
    fun destino(
        pecaEmModoTransformar: Boolean,
        toqueNaPecaOuChrome: Boolean,
        aguardandoSoltarAposArmarTransformar: Boolean = false,
    ): DestinoDoToqueAposMoverComMolduraDeTransformar {
        if (!pecaEmModoTransformar) return DestinoDoToqueAposMoverComMolduraDeTransformar.FLUXO_NORMAL
        // O toque do chip Mover ainda está fora da peça: não encerrar a moldura amarela no mesmo gesto.
        if (aguardandoSoltarAposArmarTransformar) {
            return DestinoDoToqueAposMoverComMolduraDeTransformar.FLUXO_NORMAL
        }
        return if (toqueNaPecaOuChrome) {
            DestinoDoToqueAposMoverComMolduraDeTransformar.GESTO_DA_PECA
        } else {
            DestinoDoToqueAposMoverComMolduraDeTransformar.ENCERRAR_MOLDURA
        }
    }

    fun deveConsumirToqueEnquantoAguardaSoltarAposArmar(
        aguardandoSoltarAposArmarTransformar: Boolean,
    ): Boolean = aguardandoSoltarAposArmarTransformar

    fun prendeCanetaEDedoNoGestoDaPeca(
        pecaEmModoTransformar: Boolean,
        selecaoAtivaDeRecorte: Boolean,
    ): Boolean = pecaEmModoTransformar && !selecaoAtivaDeRecorte

    /** Caneta no modo transformar não pode cancelar o gesto a cada MOVE. */
    fun canetaSegueGestoSemCancelar(
        pecaEmModoTransformar: Boolean,
        selecaoAtivaDeRecorte: Boolean,
    ): Boolean = prendeCanetaEDedoNoGestoDaPeca(pecaEmModoTransformar, selecaoAtivaDeRecorte)

    fun encerrarEntraNoHistorico(
        pecaEmModoTransformar: Boolean,
        idDaPecaSelecionada: Int,
    ): Boolean = pecaEmModoTransformar && idDaPecaSelecionada != -1
}

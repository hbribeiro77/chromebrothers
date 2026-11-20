package com.chromebrothers.navegador.regras

enum class DestinoDoToqueNoModoColarDaSelecaoCopiada {
    CONFIRMAR_COLAR,
    GESTO_DA_PREVIA,
    CANCELAR,
    IGNORAR,
}

/**
 * Destino do Colar só pela lista de camadas.
 * No quadro: cabo de giro, chip Colar, gesto na prévia, ou cancelar (fora) — nunca “escolher destino”
 * pelo toque (camada de cima em tela cheia roubava o toque).
 * Cabo de giro tem prioridade sobre o chip: o Colar no centro acima cobria o ponto de rotacionar.
 */
object ToqueNoModoColarDaSelecaoCopiadaNaComposicao {
    fun destino(
        modoColarAtivo: Boolean,
        toqueNoChipColar: Boolean,
        toqueNaPreviaOuChrome: Boolean,
        toqueNoCaboDeGiro: Boolean = false,
    ): DestinoDoToqueNoModoColarDaSelecaoCopiada {
        if (!modoColarAtivo) return DestinoDoToqueNoModoColarDaSelecaoCopiada.IGNORAR
        if (toqueNoCaboDeGiro) return DestinoDoToqueNoModoColarDaSelecaoCopiada.GESTO_DA_PREVIA
        if (toqueNoChipColar) return DestinoDoToqueNoModoColarDaSelecaoCopiada.CONFIRMAR_COLAR
        if (toqueNaPreviaOuChrome) return DestinoDoToqueNoModoColarDaSelecaoCopiada.GESTO_DA_PREVIA
        return DestinoDoToqueNoModoColarDaSelecaoCopiada.CANCELAR
    }
}

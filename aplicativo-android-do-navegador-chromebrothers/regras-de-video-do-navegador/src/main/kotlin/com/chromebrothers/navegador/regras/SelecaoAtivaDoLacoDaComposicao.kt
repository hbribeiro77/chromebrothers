package com.chromebrothers.navegador.regras

enum class AcaoDaSelecaoAtivaDoLaco {
    COPIAR,
    MOVER,
    EXCLUIR,
    INVERTER,
    RECORTAR,
    COLAR,
}

enum class EfeitoDaAcaoDaSelecaoAtivaDoLaco {
    COPIAR_SEM_BURACO,
    DESTACAR_COM_BURACO,
    EXCLUIR_COM_BURACO,
    INVERTER_MASCARA,
    REDUZIR_MOLDURA_A_SELECAO,
    CONFIRMAR_COLAR_FUNDIR,
    ;

    companion object {
        fun da(acao: AcaoDaSelecaoAtivaDoLaco): EfeitoDaAcaoDaSelecaoAtivaDoLaco {
            return when (acao) {
                AcaoDaSelecaoAtivaDoLaco.COPIAR -> COPIAR_SEM_BURACO
                AcaoDaSelecaoAtivaDoLaco.MOVER -> DESTACAR_COM_BURACO
                AcaoDaSelecaoAtivaDoLaco.EXCLUIR -> EXCLUIR_COM_BURACO
                AcaoDaSelecaoAtivaDoLaco.INVERTER -> INVERTER_MASCARA
                AcaoDaSelecaoAtivaDoLaco.RECORTAR -> REDUZIR_MOLDURA_A_SELECAO
                AcaoDaSelecaoAtivaDoLaco.COLAR -> CONFIRMAR_COLAR_FUNDIR
            }
        }
    }
}

object RecortarDaSelecaoAtivaDoLaco {
    fun exigeConfirmacao(): Boolean = true

    fun mantemSelecaoAoCancelarModal(): Boolean = true

    fun mantemSelecaoAoConfirmar(): Boolean = false
}

object InverterSelecaoAtivaDoLaco {
    fun aposInverter(invertidaAgora: Boolean): Boolean = !invertidaAgora
}

enum class LadoDaBarraFlutuanteDaSelecaoAtiva {
    ACIMA,
    ABAIXO,
}

object PosicaoDaBarraFlutuanteDaSelecaoAtiva {
    fun decidir(
        bboxTopo: Float,
        bboxBaixo: Float,
        alturaVisivel: Float,
        alturaBarra: Float,
        folga: Float,
    ): LadoDaBarraFlutuanteDaSelecaoAtiva {
        if (bboxTopo - folga - alturaBarra >= 0f) return LadoDaBarraFlutuanteDaSelecaoAtiva.ACIMA
        return LadoDaBarraFlutuanteDaSelecaoAtiva.ABAIXO
    }
}

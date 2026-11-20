package com.chromebrothers.navegador.regras

object DecisaoDeMostrarBotaoFlutuanteDoVideo {
    fun mostrar(temVideoTocando: Boolean, temLimitesNaTela: Boolean): Boolean {
        return temVideoTocando && temLimitesNaTela
    }
}

package com.chromebrothers.navegador.regras

object ExtensoesQueOUsuarioPodeGerirNoNavegador {
    fun visivel(id: String, embutida: Boolean, idEmbutido: String): Boolean {
        return !embutida && id != idEmbutido
    }
}

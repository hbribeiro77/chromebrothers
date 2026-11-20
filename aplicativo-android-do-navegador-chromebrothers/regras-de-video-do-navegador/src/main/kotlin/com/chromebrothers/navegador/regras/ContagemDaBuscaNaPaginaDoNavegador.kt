package com.chromebrothers.navegador.regras

object ContagemDaBuscaNaPaginaDoNavegador {
    fun texto(atual: Int, total: Int, encontrado: Boolean): String {
        if (!encontrado || total <= 0) return "0 de 0"
        return "$atual de $total"
    }
}

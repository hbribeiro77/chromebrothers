package com.chromebrothers.navegador.regras

/**
 * A modal de login OAuth fica amarrada à aba que a abriu:
 * visível só enquanto essa aba estiver ativa; trocar de aba só esconde.
 */
object VisibilidadeDaModalDeLoginPorAbaDoNavegador {
    fun deveExibir(idDaAbaAtiva: Long?, idDaAbaDona: Long?): Boolean {
        if (idDaAbaDona == null || idDaAbaAtiva == null) return false
        return idDaAbaAtiva == idDaAbaDona
    }
}

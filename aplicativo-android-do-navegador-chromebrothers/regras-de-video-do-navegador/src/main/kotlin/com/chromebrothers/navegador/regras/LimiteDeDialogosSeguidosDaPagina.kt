package com.chromebrothers.navegador.regras

/** Um site em laço de alert() prenderia o navegador: do 3º em diante a caixa oferece "parar". */
class LimiteDeDialogosSeguidosDaPagina {
    private var mostrados = 0
    private var ultimoFechadoEm: Long? = null
    var silenciado = false
        private set

    /** Só conta como seguida a caixa que surge logo depois de a anterior fechar. */
    fun registrar(agoraMs: Long) {
        val fechado = ultimoFechadoEm
        if (fechado == null || agoraMs - fechado > INTERVALO_DE_SEGUIDOS_MS) mostrados = 0
        mostrados++
    }

    fun fechou(agoraMs: Long) {
        ultimoFechadoEm = agoraMs
    }

    fun deveOferecerParar(): Boolean = mostrados >= 3

    fun silenciar() {
        silenciado = true
    }

    fun paginaNova() {
        mostrados = 0
        silenciado = false
        ultimoFechadoEm = null
    }

    private companion object {
        const val INTERVALO_DE_SEGUIDOS_MS = 1_000L
    }
}

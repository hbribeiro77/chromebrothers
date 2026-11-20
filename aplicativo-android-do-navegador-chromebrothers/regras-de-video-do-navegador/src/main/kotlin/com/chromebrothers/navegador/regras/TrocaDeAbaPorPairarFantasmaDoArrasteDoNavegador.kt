package com.chromebrothers.navegador.regras

/**
 * Durante o fantasma (página / coleção / biblioteca): pairar o dedo sobre outra
 * aba na faixa por [ATRASO_MS] troca para ela sem encerrar o arraste.
 */
data class EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador(
    val idDaAba: Long? = null,
    val desdeMs: Long = 0L,
)

object TrocaDeAbaPorPairarFantasmaDoArrasteDoNavegador {
    const val ATRASO_MS: Long = 700L

    fun atualizarEstado(
        idSobODedo: Long?,
        idDaAbaAtiva: Long,
        estado: EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador,
        agoraMs: Long,
    ): EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador {
        if (idSobODedo == null || idSobODedo == idDaAbaAtiva) {
            return EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador()
        }
        if (idSobODedo == estado.idDaAba) return estado
        return EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador(idDaAba = idSobODedo, desdeMs = agoraMs)
    }

    fun deveTrocarAgora(
        estado: EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador,
        idDaAbaAtiva: Long,
        agoraMs: Long,
        atrasoMs: Long = ATRASO_MS,
    ): Boolean {
        val id = estado.idDaAba ?: return false
        if (id == idDaAbaAtiva) return false
        if (estado.desdeMs <= 0L) return false
        return agoraMs - estado.desdeMs >= atrasoMs
    }
}

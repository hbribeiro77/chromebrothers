package com.chromebrothers.navegador.regras

/**
 * Valores numéricos iguais às flags de [android.content.Intent], para montar o Intent
 * da nova janela (padrão singleInstancePerTask + MULTIPLE_TASK, como o Chrome).
 */
object FlagsDaNovaJanelaDocumentCentricDoNavegador {
    const val MULTIPLE_TASK = 0x08000000
    const val RETAIN_IN_RECENTS = 0x00200000
    const val LAUNCH_ADJACENT = 0x00001000
    const val NEW_TASK = 0x10000000

    fun montar(emMultiJanela: Boolean): Int {
        var flags = NEW_TASK or MULTIPLE_TASK or RETAIN_IN_RECENTS
        if (emMultiJanela) {
            flags = flags or LAUNCH_ADJACENT
        }
        return flags
    }

    /**
     * Trampolim Samsung (`relinquishTaskIdentity`): a activity alvo deve herdar a
     * tarefa do hemisfério do split. NEW_TASK/MULTIPLE_TASK/LAUNCH_ADJACENT criam
     * outra task e deixam a janela preta do trampolim nos Recents.
     */
    fun montarParaTrampolimQueCedeIdentidadeDaTarefaDoSplit(): Int = 0
}

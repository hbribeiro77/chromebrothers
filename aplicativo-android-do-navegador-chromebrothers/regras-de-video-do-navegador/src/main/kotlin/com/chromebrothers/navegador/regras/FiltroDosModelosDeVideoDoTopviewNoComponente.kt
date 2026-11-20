package com.chromebrothers.navegador.regras

/**
 * A lista do Topview traz também modelos de editar/estender vídeo e controle de movimento.
 * O componente só monta texto→vídeo, imagem→vídeo e referência omni, então esses ficam de fora.
 */
object FiltroDosModelosDeVideoDoTopviewNoComponente {
    private val PALAVRAS_DE_FORA = listOf("extend", "edit", "motion control")

    fun serve(nome: String, inputModes: List<String>): Boolean {
        val baixo = nome.lowercase()
        if (PALAVRAS_DE_FORA.any { baixo.contains(it) }) return false
        return inputModes.none { modo ->
            val m = modo.lowercase()
            m.contains("video") || m.contains("motion")
        }
    }
}

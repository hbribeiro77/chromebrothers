package com.chromebrothers.navegador.regras

enum class ModoDoTecladoSobreAPaginaDoNavegador {
    AUTOMATICO,
    SEMPRE_DESLOCAR,
    NAO_DESLOCAR,
    ;

    companion object {
        fun peloId(id: String): ModoDoTecladoSobreAPaginaDoNavegador = when (id.trim().lowercase()) {
            "sempre", "sempre_deslocar" -> SEMPRE_DESLOCAR
            "nunca", "nao", "nao_deslocar", "não_deslocar" -> NAO_DESLOCAR
            else -> AUTOMATICO
        }

        fun idDe(modo: ModoDoTecladoSobreAPaginaDoNavegador): String = when (modo) {
            AUTOMATICO -> "automatico"
            SEMPRE_DESLOCAR -> "sempre"
            NAO_DESLOCAR -> "nunca"
        }
    }
}

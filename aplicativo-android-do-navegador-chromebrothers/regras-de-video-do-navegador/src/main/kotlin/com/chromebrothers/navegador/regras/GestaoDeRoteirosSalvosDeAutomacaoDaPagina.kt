package com.chromebrothers.navegador.regras

object GestaoDeRoteirosSalvosDeAutomacaoDaPagina {
    fun renomear(
        roteiro: RoteiroDeAutomacaoDaPagina,
        novoNome: String,
    ): RoteiroDeAutomacaoDaPagina? {
        val nome = novoNome.trim()
        if (nome.isEmpty()) return null
        if (nome.length > RoteiroDeAutomacaoDaPaginaDoNavegador.LIMITE_DO_NOME) return null
        return roteiro.copy(nome = nome)
    }

    fun duplicar(
        roteiro: RoteiroDeAutomacaoDaPagina,
        idsExistentes: Set<String>,
    ): RoteiroDeAutomacaoDaPagina {
        val nomeBase = "Cópia de ${roteiro.nome}"
            .take(RoteiroDeAutomacaoDaPaginaDoNavegador.LIMITE_DO_NOME)
        var id = RoteiroDeAutomacaoDaPaginaDoNavegador.slugDoNome(nomeBase)
        if (id.isEmpty() || id in idsExistentes) {
            id = "copia-${roteiro.id}-${System.currentTimeMillis()}"
                .take(80)
                .trim('-')
        }
        var tentativa = id
        var n = 2
        while (tentativa in idsExistentes) {
            tentativa = "${id.take(70)}-$n"
            n++
        }
        return roteiro.copy(id = tentativa, nome = nomeBase)
    }
}

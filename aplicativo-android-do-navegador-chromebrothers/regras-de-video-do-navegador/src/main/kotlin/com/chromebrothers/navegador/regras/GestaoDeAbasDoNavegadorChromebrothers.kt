package com.chromebrothers.navegador.regras

data class AbaDoNavegadorChromebrothers(
    val id: Long,
    val titulo: String,
    val endereco: String,
)

class GestaoDeAbasDoNavegadorChromebrothers(
    private val tituloDaAbaNova: String = "Nova aba",
    private val enderecoInicial: String = EnderecoDaPaginaInicialDoNavegadorChromebrothers.URL,
) {
    private val abas = mutableListOf<AbaDoNavegadorChromebrothers>()
    private var proximoId = 1L
    var idDaAbaAtiva: Long = 0
        private set

    fun iniciar(): AbaDoNavegadorChromebrothers {
        check(abas.isEmpty())
        return criarAba()
    }

    /**
     * Recria as abas salvas da última vez (só com a gestão vazia). Sem abas válidas,
     * cai no [iniciar]. Devolve as abas na mesma ordem das salvas.
     */
    fun restaurar(
        salvas: List<Pair<String, String>>,
        indiceDaAtiva: Int,
    ): List<AbaDoNavegadorChromebrothers> {
        check(abas.isEmpty())
        val validas = salvas.filter { (_, endereco) -> endereco.isNotBlank() }
        if (validas.isEmpty()) return listOf(iniciar())
        val criadas = validas.map { (titulo, endereco) ->
            AbaDoNavegadorChromebrothers(proximoId++, titulo.ifBlank { tituloDaAbaNova }, endereco)
        }
        abas.addAll(criadas)
        idDaAbaAtiva = criadas[indiceDaAtiva.coerceIn(0, criadas.lastIndex)].id
        return criadas
    }

    fun listar(): List<AbaDoNavegadorChromebrothers> = abas.toList()

    fun ativa(): AbaDoNavegadorChromebrothers? = abas.find { it.id == idDaAbaAtiva }

    fun criarAba(
        endereco: String = enderecoInicial,
        titulo: String = tituloDaAbaNova,
    ): AbaDoNavegadorChromebrothers {
        val aba = AbaDoNavegadorChromebrothers(proximoId++, titulo, endereco)
        abas.add(aba)
        idDaAbaAtiva = aba.id
        return aba
    }

    fun ativar(id: Long): Boolean {
        if (abas.none { it.id == id }) return false
        idDaAbaAtiva = id
        return true
    }

    fun atualizar(id: Long, titulo: String? = null, endereco: String? = null): AbaDoNavegadorChromebrothers? {
        val indice = abas.indexOfFirst { it.id == id }
        if (indice < 0) return null
        val atual = abas[indice]
        val nova = atual.copy(
            titulo = titulo?.takeIf { it.isNotBlank() } ?: atual.titulo,
            endereco = endereco ?: atual.endereco,
        )
        abas[indice] = nova
        return nova
    }

    fun mover(id: Long, indiceDestino: Int): Boolean {
        val origem = abas.indexOfFirst { it.id == id }
        if (origem < 0) return false
        val destino = indiceDestino.coerceIn(0, abas.lastIndex)
        if (origem == destino) return true
        val aba = abas.removeAt(origem)
        abas.add(destino, aba)
        return true
    }

    fun fechar(id: Long): Long {
        val indice = abas.indexOfFirst { it.id == id }
        if (indice < 0) return idDaAbaAtiva
        val fechouAAtiva = idDaAbaAtiva == id
        abas.removeAt(indice)
        if (abas.isEmpty()) return criarAba().id
        if (fechouAAtiva) {
            idDaAbaAtiva = (abas.getOrNull(indice) ?: abas.last()).id
        }
        return idDaAbaAtiva
    }
}

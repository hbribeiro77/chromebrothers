package com.chromebrothers.navegador.regras

data class NotaDoCampoDoNavegador(val id: Long, val texto: String)

object BancoDeNotasDoCampoDoNavegador {
    const val LIMITE = 30

    fun guardar(notas: List<NotaDoCampoDoNavegador>, texto: String, id: Long): List<NotaDoCampoDoNavegador> {
        val limpo = texto.trim()
        if (limpo.isEmpty()) return notas
        return (listOf(NotaDoCampoDoNavegador(id, limpo)) + notas.filter { it.texto != limpo }).take(LIMITE)
    }

    fun remover(notas: List<NotaDoCampoDoNavegador>, id: Long): List<NotaDoCampoDoNavegador> {
        return notas.filter { it.id != id }
    }

    fun texto(notas: List<NotaDoCampoDoNavegador>, id: Long): String? = notas.find { it.id == id }?.texto

    fun gravar(notas: List<NotaDoCampoDoNavegador>): String {
        return notas.joinToString("\n") { "${it.id}\t${escapar(it.texto)}" }
    }

    fun ler(bruto: String): List<NotaDoCampoDoNavegador> {
        if (bruto.isBlank()) return emptyList()
        return bruto.lineSequence().mapNotNull { linha ->
            val tab = linha.indexOf('\t')
            if (tab <= 0) return@mapNotNull null
            val id = linha.substring(0, tab).toLongOrNull() ?: return@mapNotNull null
            NotaDoCampoDoNavegador(id, desescapar(linha.substring(tab + 1)))
        }.toList()
    }

    private fun escapar(texto: String) = buildString {
        texto.forEach { caractere ->
            when (caractere) {
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(caractere)
            }
        }
    }

    private fun desescapar(texto: String) = buildString {
        var indice = 0
        while (indice < texto.length) {
            val caractere = texto[indice]
            if (caractere == '\\' && indice + 1 < texto.length) {
                when (texto[indice + 1]) {
                    'n' -> append('\n')
                    'r' -> append('\r')
                    't' -> append('\t')
                    '\\' -> append('\\')
                    else -> append(texto[indice + 1])
                }
                indice += 2
            } else {
                append(caractere)
                indice += 1
            }
        }
    }
}

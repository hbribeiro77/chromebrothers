package com.chromebrothers.navegador.regras

data class NotaDoSiteParaOChat(
    val hospedeiro: String,
    val texto: String,
)

sealed class ResultadoDaNotaDoSite {
    data class Guardada(
        val notas: List<NotaDoSiteParaOChat>,
        val nota: NotaDoSiteParaOChat,
    ) : ResultadoDaNotaDoSite()

    data class Recusada(val mensagem: String) : ResultadoDaNotaDoSite()
}

object CadernoDeNotasDosSitesParaOChat {
    const val LIMITE_DO_TEXTO = 2000
    const val LIMITE_DE_NOTAS = 40

    fun chave(site: String): String {
        val texto = site.trim()
        if (texto.isEmpty()) return ""
        val endereco = if (texto.contains("://")) texto else "https://$texto"
        return IconeDaPaginaNaBarraDeEndereco.hospedeiro(endereco).substringBefore(":")
    }

    fun guardar(notas: List<NotaDoSiteParaOChat>, site: String, texto: String): ResultadoDaNotaDoSite {
        val hospedeiro = chave(site)
        val corpo = texto.trim()
        val recusa = when {
            hospedeiro.isEmpty() -> "Faltou o site."
            EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh("https://$hospedeiro/") -> "A página inicial não recebe nota."
            corpo.isEmpty() -> "Faltou o texto."
            corpo.length > LIMITE_DO_TEXTO -> "O texto passou do limite."
            contemSegredo(corpo) -> "Não guardo chave nem segredo."
            else -> ""
        }
        if (recusa.isNotEmpty()) return ResultadoDaNotaDoSite.Recusada(recusa)
        val nota = NotaDoSiteParaOChat(hospedeiro, corpo)
        val restantes = notas.filterNot { it.hospedeiro == hospedeiro }
        if (restantes.size >= LIMITE_DE_NOTAS && notas.none { it.hospedeiro == hospedeiro }) {
            return ResultadoDaNotaDoSite.Recusada("O caderno chegou ao limite.")
        }
        return ResultadoDaNotaDoSite.Guardada(listOf(nota) + restantes, nota)
    }

    fun apagar(notas: List<NotaDoSiteParaOChat>, site: String): List<NotaDoSiteParaOChat> {
        val hospedeiro = chave(site)
        return notas.filterNot { it.hospedeiro == hospedeiro }
    }

    fun buscar(notas: List<NotaDoSiteParaOChat>, site: String): NotaDoSiteParaOChat? {
        val hospedeiro = chave(site)
        return notas.find { it.hospedeiro == hospedeiro }
    }

    fun paraOModelo(nota: NotaDoSiteParaOChat?): String {
        val texto = nota?.texto?.trim().orEmpty()
        if (texto.isEmpty()) return ""
        return "Nota deste site, escrita pelo usuário: $texto"
    }

    private fun contemSegredo(texto: String): Boolean {
        val baixo = texto.lowercase()
        return baixo.contains("sk-") || baixo.contains("bearer ")
    }
}

object TextoDoCadernoDeNotasDosSites {
    fun escrever(notas: List<NotaDoSiteParaOChat>): String {
        return notas.joinToString("\n") { "${escapar(it.hospedeiro)}\t${escapar(it.texto)}" }
    }

    fun ler(bruto: String): List<NotaDoSiteParaOChat> {
        if (bruto.isBlank()) return emptyList()
        return bruto.lineSequence().mapNotNull { linha ->
            val tab = linha.indexOf('\t')
            if (tab <= 0) return@mapNotNull null
            NotaDoSiteParaOChat(
                hospedeiro = desescapar(linha.substring(0, tab)),
                texto = desescapar(linha.substring(tab + 1)),
            )
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
                indice++
            }
        }
    }
}

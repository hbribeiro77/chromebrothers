package com.chromebrothers.navegador.regras

data class MetadadosDoComponenteNoIndiceDaBiblioteca(
    val id: String,
    val nome: String,
    val descricao: String,
    val tags: List<String>,
    val atualizadoEm: Long,
    val larguraPx: Int? = null,
    val alturaPx: Int? = null,
)

/**
 * Lê o índice antigo (`titulo`) ou o novo (`nome`, `descricao`, `tags`) e grava só o formato novo.
 * O HTML continua em arquivo separado.
 */
object LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml {
    fun ler(json: String): List<MetadadosDoComponenteNoIndiceDaBiblioteca> {
        return objetosNoArray(json).mapNotNull { objeto -> metadadosDe(objeto) }
    }

    fun escrever(itens: List<MetadadosDoComponenteNoIndiceDaBiblioteca>): String {
        if (itens.isEmpty()) return "[]"
        return itens.joinToString(prefix = "[", postfix = "]") { item ->
            val tags = item.tags.joinToString(prefix = "[", postfix = "]") { tag ->
                "\"${TextoJsonDoChat.escapar(tag)}\""
            }
            val dimensoes = buildString {
                item.larguraPx?.takeIf { it > 0 }?.let { append(",\"larguraPx\":$it") }
                item.alturaPx?.takeIf { it > 0 }?.let { append(",\"alturaPx\":$it") }
            }
            "{" +
                "\"id\":\"${TextoJsonDoChat.escapar(item.id)}\"," +
                "\"nome\":\"${TextoJsonDoChat.escapar(item.nome)}\"," +
                "\"descricao\":\"${TextoJsonDoChat.escapar(item.descricao)}\"," +
                "\"tags\":$tags," +
                "\"atualizadoEm\":${item.atualizadoEm}" +
                dimensoes +
                "}"
        }
    }

    private fun metadadosDe(objeto: String): MetadadosDoComponenteNoIndiceDaBiblioteca? {
        val id = TextoJsonDoChat.valorDaChave(objeto, "id")?.trim().orEmpty()
        val nomeDireto = TextoJsonDoChat.valorDaChave(objeto, "nome")?.trim().orEmpty()
        val titulo = TextoJsonDoChat.valorDaChave(objeto, "titulo")?.trim().orEmpty()
        val nome = nomeDireto.ifBlank { titulo }
        if (id.isBlank() || nome.isBlank()) return null
        val descricao = TextoJsonDoChat.valorDaChave(objeto, "descricao")?.trim().orEmpty()
            .take(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DA_DESCRICAO)
        val tags = (TextoJsonDoChat.textosDaChave(objeto, "tags").orEmpty())
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .take(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DE_TAGS)
            .map { it.take(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_TAMANHO_DA_TAG) }
        val largura = inteiroPositivoDaChave(objeto, "larguraPx")
        val altura = inteiroPositivoDaChave(objeto, "alturaPx")
        return MetadadosDoComponenteNoIndiceDaBiblioteca(
            id = id,
            nome = nome.take(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_NOME),
            descricao = descricao,
            tags = tags,
            atualizadoEm = longoDaChave(objeto, "atualizadoEm"),
            larguraPx = largura,
            alturaPx = altura,
        )
    }

    private fun inteiroPositivoDaChave(json: String, chave: String): Int? {
        val valor = longoDaChave(json, chave)
        return valor.takeIf { it > 0L }?.toInt()
    }

    private fun longoDaChave(json: String, chave: String): Long {
        val marca = "\"$chave\""
        val achou = json.indexOf(marca)
        if (achou < 0) return 0L
        var cursor = achou + marca.length
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != ':') return 0L
        cursor++
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        val inicio = cursor
        if (cursor < json.length && json[cursor] == '-') cursor++
        if (cursor >= json.length || !json[cursor].isDigit()) return 0L
        while (cursor < json.length && json[cursor].isDigit()) cursor++
        return json.substring(inicio, cursor).toLongOrNull() ?: 0L
    }

    private fun objetosNoArray(json: String): List<String> {
        val inicio = json.indexOf('[')
        if (inicio < 0) return emptyList()
        val objetos = mutableListOf<String>()
        var indice = inicio + 1
        while (indice < json.length) {
            val caractere = json[indice]
            when {
                caractere.isWhitespace() || caractere == ',' -> indice++
                caractere == ']' -> return objetos
                caractere == '{' -> {
                    val fim = fimDoObjeto(json, indice) ?: return objetos
                    objetos += json.substring(indice, fim)
                    indice = fim
                }
                else -> return objetos
            }
        }
        return objetos
    }

    private fun fimDoObjeto(json: String, inicio: Int): Int? {
        var profundidade = 0
        var indice = inicio
        while (indice < json.length) {
            val caractere = json[indice]
            if (caractere == '"') {
                indice = TextoJsonDoChat.textoNaAspa(json, indice)?.second ?: return null
                continue
            }
            if (caractere == '{') profundidade++
            if (caractere == '}') {
                profundidade--
                if (profundidade == 0) return indice + 1
            }
            indice++
        }
        return null
    }
}

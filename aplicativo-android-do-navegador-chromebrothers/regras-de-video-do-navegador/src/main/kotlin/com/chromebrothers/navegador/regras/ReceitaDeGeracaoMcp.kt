package com.chromebrothers.navegador.regras

/**
 * Uma referência guardada pela receita. O arquivo fica em `refs-das-receitas/<hash>.<extensao>`
 * (armazenamento por conteúdo: a mesma imagem usada em várias gerações ocupa espaço uma vez).
 * Vídeo também guarda um quadro JPEG ([hashDoQuadro]) para prévia e metadados da biblioteca.
 */
data class RefDaReceitaDeGeracaoMcp(
    val tipo: String,
    val nome: String,
    val hash: String,
    val extensao: String,
    val duracaoMs: Long = 0L,
    val hashDoQuadro: String = "",
) {
    val ehVideo: Boolean get() = tipo == ReceitaDeGeracaoMcp.TIPO_VIDEO
    val nomeDoArquivo: String get() = "$hash.$extensao"
    val nomeDoQuadro: String get() = if (hashDoQuadro.isBlank()) "" else "$hashDoQuadro.jpg"
}

/** Tudo o que foi enviado num pedido de geração: o JSON do componente + as referências. */
data class ReceitaDeGeracaoMcp(
    val id: String,
    val tipo: String,
    val criadoEmMs: Long,
    /** O pedido exatamente como o componente enviou (mcps, modelo, prompt, quantidade, proporção…). */
    val pedidoJson: String,
    val refs: List<RefDaReceitaDeGeracaoMcp>,
) {
    companion object {
        const val TIPO_IMAGEM = "imagem"
        const val TIPO_VIDEO = "video"

        fun paraJson(r: ReceitaDeGeracaoMcp): String {
            val refs = r.refs.joinToString(",") { ref ->
                """{"tipo":"${e(ref.tipo)}","nome":"${e(ref.nome)}","hash":"${e(ref.hash)}","extensao":"${e(ref.extensao)}","duracaoMs":${ref.duracaoMs},"hashDoQuadro":"${e(ref.hashDoQuadro)}"}"""
            }
            return """{"id":"${e(r.id)}","tipo":"${e(r.tipo)}","criadoEmMs":${r.criadoEmMs},"pedidoJson":"${e(r.pedidoJson)}","refs":[$refs]}"""
        }

        fun deJson(json: String): ReceitaDeGeracaoMcp? {
            val bruto = json.trim()
            if (bruto.isEmpty()) return null
            val id = TextoJsonDoChat.valorDaChave(bruto, "id").orEmpty()
            if (id.isBlank()) return null
            val refs = TextoJsonDoChat.objetosDaChave(bruto, "refs").orEmpty().mapNotNull { b ->
                val hash = TextoJsonDoChat.valorDaChave(b, "hash").orEmpty()
                if (!hashValido(hash)) return@mapNotNull null
                RefDaReceitaDeGeracaoMcp(
                    tipo = TextoJsonDoChat.valorDaChave(b, "tipo").orEmpty().ifBlank { TIPO_IMAGEM },
                    nome = TextoJsonDoChat.valorDaChave(b, "nome").orEmpty(),
                    hash = hash,
                    extensao = extensaoSegura(TextoJsonDoChat.valorDaChave(b, "extensao").orEmpty()),
                    duracaoMs = TextoJsonDoChat.numeroDaChave(b, "duracaoMs")?.toLong() ?: 0L,
                    hashDoQuadro = TextoJsonDoChat.valorDaChave(b, "hashDoQuadro").orEmpty()
                        .takeIf { hashValido(it) }.orEmpty(),
                )
            }
            return ReceitaDeGeracaoMcp(
                id = id,
                tipo = TextoJsonDoChat.valorDaChave(bruto, "tipo").orEmpty().ifBlank { TIPO_IMAGEM },
                criadoEmMs = TextoJsonDoChat.numeroDaChave(bruto, "criadoEmMs")?.toLong() ?: 0L,
                pedidoJson = TextoJsonDoChat.valorDaChave(bruto, "pedidoJson").orEmpty(),
                refs = refs,
            )
        }

        /** Nomes de arquivo de `refs-das-receitas/` usados por alguma das [receitas]. */
        fun arquivosEmUso(receitas: List<ReceitaDeGeracaoMcp>): Set<String> =
            receitas.flatMap { r -> r.refs.flatMap { listOf(it.nomeDoArquivo, it.nomeDoQuadro) } }
                .filter { it.isNotBlank() }
                .toSet()

        /** Só hex: o hash vira nome de arquivo, então nada de barra ou ponto vindo do JSON. */
        fun hashValido(hash: String): Boolean = hash.length in 16..128 && hash.all { it in '0'..'9' || it in 'a'..'f' }

        fun extensaoSegura(ext: String): String {
            val limpa = ext.trim().lowercase().takeWhile { it.isLetterOrDigit() }
            return if (limpa.length in 2..5) limpa else "bin"
        }

        private fun e(texto: String) = TextoJsonDoChat.escapar(texto)
    }
}

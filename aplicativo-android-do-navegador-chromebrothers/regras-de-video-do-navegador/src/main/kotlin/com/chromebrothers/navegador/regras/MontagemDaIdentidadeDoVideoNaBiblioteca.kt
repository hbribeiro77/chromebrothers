package com.chromebrothers.navegador.regras

data class IdentidadeDoVideoNaBiblioteca(
    val chave: String,
    val enderecoParaAbrir: String,
    val abreNoPlayerDeArquivo: Boolean,
    val titulo: String,
)

object MontagemDaIdentidadeDoVideoNaBiblioteca {
    fun montar(
        enderecoDaPagina: String,
        enderecoDoVideo: String,
        titulo: String,
    ): IdentidadeDoVideoNaBiblioteca? {
        val video = enderecoDoVideo.trim()
        val pagina = enderecoDaPagina.trim()
        val arquivo = video.isNotEmpty() &&
            DecisaoDeDestinoDoToqueNoBotaoDoVideo.destino(video) == DestinoDoToqueNoBotaoDoVideo.PLAYER_DE_ARQUIVO
        val endereco = if (arquivo) video else pagina
        if (!endereco.startsWith("http://") && !endereco.startsWith("https://")) return null
        val nome = titulo.trim().ifBlank { nomePeloEndereco(endereco) }
        return IdentidadeDoVideoNaBiblioteca(
            chave = normalizar(endereco),
            enderecoParaAbrir = endereco,
            abreNoPlayerDeArquivo = arquivo,
            titulo = nome,
        )
    }

    fun normalizar(endereco: String): String {
        return endereco.trim().substringBefore('#').trimEnd('/')
    }

    private fun nomePeloEndereco(endereco: String): String {
        val semQuery = endereco.substringBefore('?').substringBefore('#')
        val ultimo = semQuery.trimEnd('/').substringAfterLast('/')
        return ultimo.ifBlank { endereco }
    }
}

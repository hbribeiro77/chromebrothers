package com.chromebrothers.navegador.regras

data class ImagemDoMarkdownDoChat(
    val endereco: String,
    val nome: String,
)

data class PedidoGerarMarkdownDoChat(
    val titulo: String,
    val conteudo: String,
    val imagens: List<ImagemDoMarkdownDoChat>,
)

data class ItemDoZipDoChat(
    val endereco: String,
    val caminhoNoZip: String,
)

data class PedidoGerarZipDoChat(
    val nome: String,
    val itens: List<ItemDoZipDoChat>,
    val conteudoMarkdown: String?,
)

sealed class ResultadoDaGeracaoDeMarkdownDoChat {
    data class Pronto(val pedido: PedidoDeAcaoConfirmadaDoChat.GerarMarkdown) : ResultadoDaGeracaoDeMarkdownDoChat()
    data class Recusado(val mensagem: String) : ResultadoDaGeracaoDeMarkdownDoChat()
}

sealed class ResultadoDaGeracaoDeZipDoChat {
    data class Pronto(val pedido: PedidoDeAcaoConfirmadaDoChat.GerarZip) : ResultadoDaGeracaoDeZipDoChat()
    data class Recusado(val mensagem: String) : ResultadoDaGeracaoDeZipDoChat()
}

object GeracaoDeArquivoPeloChatDaOpenRouter {
    const val LIMITE_DO_TITULO = 120
    const val LIMITE_DO_CONTEUDO = 100_000
    const val LIMITE_DE_IMAGENS = 40
    const val LIMITE_DE_ITENS_ZIP = 50
    const val LIMITE_DO_ENDERECO = 2000
    const val LIMITE_DO_NOME = 120

    fun lerMarkdown(argumentos: String): ResultadoDaGeracaoDeMarkdownDoChat {
        val titulo = TextoJsonDoChat.valorDaChave(argumentos, "titulo")?.trim().orEmpty()
        val conteudo = TextoJsonDoChat.valorDaChave(argumentos, "conteudo").orEmpty()
        when {
            titulo.isBlank() -> return ResultadoDaGeracaoDeMarkdownDoChat.Recusado("Faltou o título.")
            titulo.length > LIMITE_DO_TITULO ->
                return ResultadoDaGeracaoDeMarkdownDoChat.Recusado("O título passou do limite.")
            conteudo.isEmpty() -> return ResultadoDaGeracaoDeMarkdownDoChat.Recusado("Faltou o conteúdo.")
            conteudo.length > LIMITE_DO_CONTEUDO ->
                return ResultadoDaGeracaoDeMarkdownDoChat.Recusado("O conteúdo passou do limite.")
        }
        val objetos = TextoJsonDoChat.objetosDaChave(argumentos, "imagens").orEmpty().take(LIMITE_DE_IMAGENS)
        val imagens = LinkedHashMap<String, ImagemDoMarkdownDoChat>()
        objetos.forEach { objeto ->
            val item = imagemDe(objeto) ?: return@forEach
            if (item.endereco !in imagens) imagens[item.endereco] = item
        }
        return ResultadoDaGeracaoDeMarkdownDoChat.Pronto(
            PedidoDeAcaoConfirmadaDoChat.GerarMarkdown(
                titulo = titulo,
                conteudo = conteudo,
                imagens = imagens.values.toList(),
            ),
        )
    }

    fun lerZip(argumentos: String): ResultadoDaGeracaoDeZipDoChat {
        val nome = TextoJsonDoChat.valorDaChave(argumentos, "nome")?.trim().orEmpty()
        if (nome.isBlank()) return ResultadoDaGeracaoDeZipDoChat.Recusado("Faltou o nome do pacote.")
        if (nome.length > LIMITE_DO_TITULO) {
            return ResultadoDaGeracaoDeZipDoChat.Recusado("O nome passou do limite.")
        }
        val markdown = TextoJsonDoChat.valorDaChave(argumentos, "conteudo_markdown")
        if (markdown != null && markdown.length > LIMITE_DO_CONTEUDO) {
            return ResultadoDaGeracaoDeZipDoChat.Recusado("O markdown passou do limite.")
        }
        val objetos = TextoJsonDoChat.objetosDaChave(argumentos, "itens").orEmpty().take(LIMITE_DE_ITENS_ZIP)
        val itens = LinkedHashMap<String, ItemDoZipDoChat>()
        objetos.forEach { objeto ->
            val item = itemZipDe(objeto) ?: return@forEach
            if (item.caminhoNoZip !in itens) itens[item.caminhoNoZip] = item
        }
        if (itens.isEmpty() && markdown.isNullOrBlank()) {
            return ResultadoDaGeracaoDeZipDoChat.Recusado("Faltou item ou markdown no ZIP.")
        }
        return ResultadoDaGeracaoDeZipDoChat.Pronto(
            PedidoDeAcaoConfirmadaDoChat.GerarZip(
                nome = nome,
                itens = itens.values.toList(),
                conteudoMarkdown = markdown?.takeIf { it.isNotBlank() },
            ),
        )
    }

    fun nomeDeArquivoSeguro(titulo: String): String {
        val limpo = titulo.trim()
            .replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
        return limpo.take(LIMITE_DO_NOME).ifBlank { "arquivo" }
    }

    fun enderecoDeMidiaPermitido(endereco: String): Boolean {
        val texto = endereco.trim()
        if (texto.length > LIMITE_DO_ENDERECO) return false
        return texto.startsWith("http://") ||
            texto.startsWith("https://") ||
            texto.startsWith("file:")
    }

    fun reescreverComAssets(conteudo: String, mapaEnderecoParaAsset: Map<String, String>): String {
        var saida = conteudo
        mapaEnderecoParaAsset.forEach { (endereco, asset) ->
            saida = saida.replace(endereco, asset)
        }
        return saida
    }

    fun perguntaMarkdown(pedido: PedidoDeAcaoConfirmadaDoChat.GerarMarkdown): String {
        val n = pedido.imagens.size
        val imagens = if (n == 0) "sem imagens" else if (n == 1) "1 imagem" else "$n imagens"
        return "Gerar markdown ($imagens)?\n${pedido.titulo}"
    }

    fun perguntaZip(pedido: PedidoDeAcaoConfirmadaDoChat.GerarZip): String {
        val n = pedido.itens.size + if (pedido.conteudoMarkdown.isNullOrBlank()) 0 else 1
        return "Gerar ZIP com $n arquivo(s)?\n${pedido.nome}"
    }

    private fun imagemDe(objeto: String): ImagemDoMarkdownDoChat? {
        val endereco = TextoJsonDoChat.valorDaChave(objeto, "endereco")?.trim().orEmpty()
        if (!enderecoDeMidiaPermitido(endereco)) return null
        val nomeBruto = TextoJsonDoChat.valorDaChave(objeto, "nome")?.trim().orEmpty()
            .ifBlank { nomeDoEndereco(endereco) }
        val nome = nomeDeArquivoSeguro(nomeBruto).ifBlank { "imagem.png" }
        return ImagemDoMarkdownDoChat(endereco, nome)
    }

    private fun itemZipDe(objeto: String): ItemDoZipDoChat? {
        val endereco = TextoJsonDoChat.valorDaChave(objeto, "endereco")?.trim().orEmpty()
        if (!enderecoDeMidiaPermitido(endereco)) return null
        val caminho = TextoJsonDoChat.valorDaChave(objeto, "caminho_no_zip")?.trim().orEmpty()
            .ifBlank { nomeDoEndereco(endereco) }
        val seguro = caminho
            .replace('\\', '/')
            .split('/')
            .filter { it.isNotBlank() && it != "." && it != ".." }
            .joinToString("/") { nomeDeArquivoSeguro(it) }
        if (seguro.isBlank()) return null
        return ItemDoZipDoChat(endereco, seguro)
    }

    private fun nomeDoEndereco(endereco: String): String {
        val semQuery = endereco.substringBefore('?').substringBefore('#')
        val base = semQuery.substringAfterLast('/').ifBlank { "arquivo" }
        return nomeDeArquivoSeguro(base)
    }
}

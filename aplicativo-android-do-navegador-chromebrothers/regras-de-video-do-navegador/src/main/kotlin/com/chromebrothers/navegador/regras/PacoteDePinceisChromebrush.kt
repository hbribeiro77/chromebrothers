package com.chromebrothers.navegador.regras

/**
 * Pacote `.chromebrush`: zip com `pinceis.json` (`{"versao":1,"pinceis":[…]}`) e as imagens de forma e
 * granulado em `imagens/<nome>`. Na importação só entram essas entradas, com nomes seguros e tamanho limitado.
 */
object PacoteDePinceisChromebrush {
    const val EXTENSAO = "chromebrush"
    const val ENTRADA_DO_MANIFESTO = "pinceis.json"
    const val PASTA_DAS_IMAGENS = "imagens/"
    const val LIMITE_POR_IMAGEM = 5 * 1024 * 1024
    const val LIMITE_DO_MANIFESTO = 1024 * 1024
    const val LIMITE_DE_PINCEIS = 200

    fun manifesto(pinceis: List<PincelDoMotor>): String =
        "{\"versao\":1,\"pinceis\":[" + pinceis.joinToString(",") { PincelDoMotorEmJson.paraJson(it) } + "]}"

    fun lerManifesto(json: String): List<PincelDoMotor> =
        TextoJsonDoChat.objetosDaChave(json.trim(), "pinceis").orEmpty()
            .take(LIMITE_DE_PINCEIS)
            .mapNotNull { PincelDoMotorEmJson.deJson(it) }

    /** Imagens que os pincéis usam de fato (forma e granulado de imagem). */
    fun arquivosUsados(pinceis: List<PincelDoMotor>): Set<String> =
        pinceis.flatMap { p ->
            listOfNotNull(
                p.arquivoDaForma.takeIf { p.forma == FormaDoCarimbo.IMAGEM && it.isNotBlank() },
                p.arquivoDoGranulado.takeIf { p.granulado == FonteDoGranulado.IMAGEM && it.isNotBlank() },
            )
        }.toSet()

    /** Nome do arquivo dentro de `imagens/`, ou null se a entrada não for uma imagem aceita (bloqueia `../`). */
    fun imagemDaEntrada(nomeDaEntrada: String): String? {
        if (!nomeDaEntrada.startsWith(PASTA_DAS_IMAGENS)) return null
        val nome = nomeDaEntrada.removePrefix(PASTA_DAS_IMAGENS)
        val seguro = nome.isNotEmpty() && nome.length <= 120 && !nome.startsWith(".") &&
            nome.all { it.isLetterOrDigit() || it == '-' || it == '_' || it == '.' }
        return nome.takeIf { seguro }
    }

    /**
     * Pincéis prontos para entrar em "Meus pincéis": id que já existe (ou repetido no pacote) ganha id novo e
     * "(importado)" no nome; cada imagem passa por [guardarImagem] (que devolve o nome local novo); imagem que
     * faltou faz a forma voltar a redonda / o granulado sumir, em vez de quebrar o pincel.
     */
    fun prepararImportacao(
        lidos: List<PincelDoMotor>,
        idsExistentes: Set<String>,
        novoId: () -> String,
        guardarImagem: (String) -> String?,
    ): List<PincelDoMotor> {
        val usados = idsExistentes.toMutableSet()
        val renomeadas = mutableMapOf<String, String?>()
        fun local(nome: String): String? = renomeadas.getOrPut(nome) { guardarImagem(nome) }
        return lidos.map { original ->
            var p = original
            if (p.id in usados) p = p.copy(id = novoId(), nome = "${p.nome} (importado)")
            usados += p.id
            if (p.forma == FormaDoCarimbo.IMAGEM) {
                val nome = local(p.arquivoDaForma)
                p = if (nome == null) p.copy(forma = FormaDoCarimbo.REDONDA, arquivoDaForma = "") else p.copy(arquivoDaForma = nome)
            }
            if (p.granulado == FonteDoGranulado.IMAGEM) {
                val nome = local(p.arquivoDoGranulado)
                p = if (nome == null) p.copy(granulado = FonteDoGranulado.NENHUM, arquivoDoGranulado = "") else p.copy(arquivoDoGranulado = nome)
            }
            p
        }
    }

    /** Nome do arquivo exportado, sem caracteres que o sistema de arquivos recusa. */
    fun nomeDoArquivo(base: String): String {
        val limpo = base.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_").take(60).ifBlank { "pinceis" }
        return "$limpo.$EXTENSAO"
    }
}

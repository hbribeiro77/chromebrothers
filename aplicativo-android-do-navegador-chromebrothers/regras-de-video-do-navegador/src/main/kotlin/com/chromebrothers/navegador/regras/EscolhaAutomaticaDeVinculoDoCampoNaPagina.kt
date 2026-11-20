package com.chromebrothers.navegador.regras

data class ResultadoDaEscolhaAutomaticaDeVinculoDoCampoNaPagina(
    val campo: MarcaDoElementoVinculado?,
    val envio: MarcaDoElementoVinculado?,
)

object EscolhaAutomaticaDeVinculoDoCampoNaPagina {
    private val palavrasDeCaixa = listOf(
        "prompt", "message", "mensagem", "chat", "pergunta", "describe", "descreva",
        "description", "descricao", "texto", "text", "query", "input", "composer",
        "escreva", "write", "ask",
    )
    private val palavrasRuinsDeCaixa = listOf(
        "search", "buscar", "busca", "email", "password", "senha", "user", "usuario",
        "login", "username", "filter", "filtro",
    )
    private val palavrasDeEnvio = listOf(
        "generate", "gerar", "create", "criar", "send", "enviar", "submit", "run",
        "executar", "execute", "go", "imagine", "make", "fazer", "inventar", "apply",
        "aplicar", "ok", "confirmar", "confirm", "start", "iniciar",
    )
    private val palavrasRuinsDeEnvio = listOf(
        "cancel", "cancelar", "close", "fechar", "clear", "limpar", "delete", "excluir",
        "back", "voltar", "login", "sign", "dismiss", "remove", "remover",
    )

    fun escolher(itens: List<CampoVinculavelDaPagina>): ResultadoDaEscolhaAutomaticaDeVinculoDoCampoNaPagina {
        val campos = itens.filter { it.modo == "campo" }.map { it.marca }
        val envios = itens.filter { it.modo == "envio" }.map { it.marca }
        return ResultadoDaEscolhaAutomaticaDeVinculoDoCampoNaPagina(
            campo = campos.maxByOrNull { pontuarCampo(it) },
            envio = envios.maxByOrNull { pontuarEnvio(it) },
        )
    }

    fun mensagem(escolha: ResultadoDaEscolhaAutomaticaDeVinculoDoCampoNaPagina): String {
        val caixa = escolha.campo?.let { VinculoDoCampoParaOChat.nomeCurto(it) }
        val botao = escolha.envio?.let { VinculoDoCampoParaOChat.nomeCurto(it) }
        return when {
            caixa != null && botao != null -> "Vinculei a caixa $caixa e o botão $botao."
            caixa != null -> "Vinculei a caixa $caixa. Não achei botão de envio."
            botao != null -> "Vinculei o botão $botao. Não achei caixa."
            else -> "Não achei caixa nem botão nesta página."
        }
    }

    private fun pontuarCampo(marca: MarcaDoElementoVinculado): Int {
        var pontos = 0
        val tag = marca.tag.uppercase()
        val papel = marca.papel.lowercase()
        val texto = textoDaMarca(marca)
        when (tag) {
            "TEXTAREA" -> pontos += 50
            "DIV", "P" -> if (papel == "textbox") pontos += 35 else pontos += 5
            "INPUT" -> pontos += 10
        }
        if (papel == "textbox") pontos += 20
        pontos += coincidencias(texto, palavrasDeCaixa) * 25
        pontos -= coincidencias(texto, palavrasRuinsDeCaixa) * 40
        if (marca.id.equals("prompt", ignoreCase = true) || marca.nome.equals("prompt", ignoreCase = true)) {
            pontos += 30
        }
        return pontos
    }

    private fun pontuarEnvio(marca: MarcaDoElementoVinculado): Int {
        var pontos = 0
        val tag = marca.tag.uppercase()
        val texto = textoDaMarca(marca)
        when (tag) {
            "BUTTON" -> pontos += 20
            "INPUT" -> pontos += 15
            else -> pontos += 5
        }
        if (marca.papel.equals("button", ignoreCase = true)) pontos += 5
        pontos += coincidencias(texto, palavrasDeEnvio) * 35
        pontos -= coincidencias(texto, palavrasRuinsDeEnvio) * 50
        return pontos
    }

    private fun textoDaMarca(marca: MarcaDoElementoVinculado): String {
        return listOf(marca.id, marca.nome, marca.rotulo, marca.papel)
            .joinToString(" ")
            .lowercase()
    }

    private fun coincidencias(texto: String, palavras: List<String>): Int {
        return palavras.count { palavra ->
            texto.contains(palavra) || texto.split(Regex("[^a-z0-9à-ü]+")).any { it == palavra }
        }
    }
}

package com.chromebrothers.navegador.regras

object GeracaoDeVideoPeloVivixNoChat {
    const val SEM_SESSAO = "Vivix sem token. Cole o token em Engrenagem → Vivix W1 (token gerado em platform.vivix.ai)."

    fun lerVideo(argumentos: String): ResultadoDaGeracaoVivixDoChat {
        val prompt = TextoJsonDoChat.valorDaChave(argumentos, "prompt")?.trim().orEmpty()
        val modeloBruto = TextoJsonDoChat.valorDaChave(argumentos, "modelo")?.trim().orEmpty()
        val duracao = TextoJsonDoChat.inteiroDaChave(argumentos, "duracao") ?: PedidoDeVideoDoVivixW1.DURACAO_PADRAO
        val resolucao = TextoJsonDoChat.valorDaChave(argumentos, "resolucao")?.trim().orEmpty()
        val proporcao = TextoJsonDoChat.valorDaChave(argumentos, "proporcao")?.trim().orEmpty()
        return when {
            prompt.isEmpty() -> ResultadoDaGeracaoVivixDoChat.Recusado("Faltou o prompt.")
            prompt.length > PedidoDeVideoDoVivixW1.LIMITE_DO_PROMPT ->
                ResultadoDaGeracaoVivixDoChat.Recusado("O prompt passou do limite.")
            modeloBruto.isNotEmpty() && !CatalogoDeModelosDeVideoDoVivixNaColecao.ehModeloVivix(modeloBruto) ->
                ResultadoDaGeracaoVivixDoChat.Recusado(
                    "Modelo Vivix desconhecido: $modeloBruto. Use vivix-w1, vivix-w1-fast ou vivix-w1-ultrafast.",
                )
            duracao !in PedidoDeVideoDoVivixW1.DURACAO_MINIMA..PedidoDeVideoDoVivixW1.DURACAO_MAXIMA ->
                ResultadoDaGeracaoVivixDoChat.Recusado("A duração no Vivix vai de 5 a 15 segundos.")
            proporcao.isNotEmpty() && proporcao !in PedidoDeVideoDoVivixW1.PROPORCOES_ACEITAS ->
                ResultadoDaGeracaoVivixDoChat.Recusado("Proporção não aceita pelo Vivix (16:9, 9:16, 1:1, 4:3 ou 3:4).")
            else -> ResultadoDaGeracaoVivixDoChat.Pronto(
                PedidoDeAcaoConfirmadaDoChat.GerarVideoVivix(
                    prompt = prompt,
                    modelo = CatalogoDeModelosDeVideoDoVivixNaColecao.idDaApi(modeloBruto),
                    duracao = duracao,
                    resolucao = PedidoDeVideoDoVivixW1.resolucaoParaApi(resolucao),
                    proporcao = proporcao.ifBlank { PedidoDeVideoDoVivixW1.PROPORCAO_PADRAO },
                ),
            )
        }
    }

    fun perguntaVideo(pedido: PedidoDeAcaoConfirmadaDoChat.GerarVideoVivix): String {
        val creditos = PedidoDeVideoDoVivixW1.creditosEstimados(pedido.modelo, pedido.duracao)
        return "Gerar vídeo no Vivix W1?\n" +
            "Modelo: ${CatalogoDeModelosDeVideoDoVivixNaColecao.nome(pedido.modelo)} · ${pedido.duracao}s · " +
            "${PedidoDeVideoDoVivixW1.rotuloDaResolucao(pedido.resolucao)} · ${pedido.proporcao} · " +
            PedidoDeVideoDoVivixW1.rotuloDosCreditos(creditos) + "\n" +
            pedido.prompt.take(160)
    }
}

sealed class ResultadoDaGeracaoVivixDoChat {
    data class Pronto(val pedido: PedidoDeAcaoConfirmadaDoChat.GerarVideoVivix) : ResultadoDaGeracaoVivixDoChat()
    data class Recusado(val mensagem: String) : ResultadoDaGeracaoVivixDoChat()
}

object LiberacaoDoVivixParaOChat {
    fun liberar(): Boolean = true

    fun revogar(): Boolean = false

    fun segueSemPerguntar(pedido: PedidoDeAcaoConfirmadaDoChat, liberado: Boolean): Boolean =
        liberado && pedido is PedidoDeAcaoConfirmadaDoChat.GerarVideoVivix

    fun ehPedidoVivix(pedido: PedidoDeAcaoConfirmadaDoChat): Boolean =
        pedido is PedidoDeAcaoConfirmadaDoChat.GerarVideoVivix
}

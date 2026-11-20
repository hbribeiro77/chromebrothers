package com.chromebrothers.navegador.regras

data class PassoDaEsperaDoCarregamento(
    val agendarPausa: Boolean = false,
    val cancelarPausa: Boolean = false,
    val lerTexto: Boolean = false,
    val pronto: Boolean = false,
)

class EsperaDoCarregamentoDaPaginaParaOChat(jaViuComeco: Boolean = false) {
    var sucesso: Boolean = true
        private set
    var tempoEsgotado: Boolean = false
        private set
    var texto: String = ""
        private set

    private var fase = if (jaViuComeco) Fase.AGUARDANDO_PARADA else Fase.AGUARDANDO_COMECO
    private var releu = false

    fun comeco(): PassoDaEsperaDoCarregamento {
        if (fase == Fase.ENTREGUE || fase == Fase.AGUARDANDO_PARADA) return PassoDaEsperaDoCarregamento()
        val cancelar = fase == Fase.PAUSA || fase == Fase.PAUSA_DA_RELEITURA || fase == Fase.AGUARDANDO_TEXTO
        fase = Fase.AGUARDANDO_PARADA
        return PassoDaEsperaDoCarregamento(cancelarPausa = cancelar)
    }

    fun parada(deuCerto: Boolean): PassoDaEsperaDoCarregamento {
        if (fase != Fase.AGUARDANDO_PARADA) return PassoDaEsperaDoCarregamento()
        sucesso = deuCerto
        fase = Fase.PAUSA
        return PassoDaEsperaDoCarregamento(agendarPausa = true)
    }

    fun pausaAcabou(): PassoDaEsperaDoCarregamento {
        if (fase != Fase.PAUSA && fase != Fase.PAUSA_DA_RELEITURA) return PassoDaEsperaDoCarregamento()
        fase = Fase.AGUARDANDO_TEXTO
        return PassoDaEsperaDoCarregamento(lerTexto = true)
    }

    fun textoLido(lido: String): PassoDaEsperaDoCarregamento {
        if (fase != Fase.AGUARDANDO_TEXTO) return PassoDaEsperaDoCarregamento()
        texto = lido
        if (!releu && lido.trim().length < LIMIAR_DO_TEXTO_CURTO) {
            releu = true
            fase = Fase.PAUSA_DA_RELEITURA
            return PassoDaEsperaDoCarregamento(agendarPausa = true)
        }
        fase = Fase.ENTREGUE
        return PassoDaEsperaDoCarregamento(pronto = true)
    }

    fun tempoEsgotado(): PassoDaEsperaDoCarregamento {
        if (fase == Fase.ENTREGUE) return PassoDaEsperaDoCarregamento()
        tempoEsgotado = true
        fase = Fase.ENTREGUE
        return PassoDaEsperaDoCarregamento(cancelarPausa = true, pronto = true)
    }

    private enum class Fase {
        AGUARDANDO_COMECO,
        AGUARDANDO_PARADA,
        PAUSA,
        AGUARDANDO_TEXTO,
        PAUSA_DA_RELEITURA,
        ENTREGUE,
    }

    companion object {
        const val PAUSA_MS = 1_500L
        const val LIMITE_MS = 20_000L
        const val LIMIAR_DO_TEXTO_CURTO = 80
    }
}

object RelatoDoCarregamentoDaPaginaParaOChat {
    fun relato(
        endereco: String,
        titulo: String,
        texto: String,
        sucesso: Boolean,
        tempoEsgotado: Boolean,
        criouAba: Boolean = false,
    ): String {
        val cabeca = when {
            tempoEsgotado -> "Ainda carregando depois de ${EsperaDoCarregamentoDaPaginaParaOChat.LIMITE_MS / 1000}s."
            !sucesso -> "O carregamento falhou."
            texto.trim().length < EsperaDoCarregamentoDaPaginaParaOChat.LIMIAR_DO_TEXTO_CURTO ->
                "A página terminou de carregar, mas o texto visível ainda é curto. O documento pode ter terminado antes de o site montar o conteúdo."
            else -> "A página carregou."
        }
        val abertura = if (criouAba) "Criei uma aba.\n" else ""
        return abertura + cabeca + "\n" + ContextoDaPaginaParaOChat.mensagem(titulo, endereco, texto)
    }
}

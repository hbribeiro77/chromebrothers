package com.chromebrothers.navegador.regras

/**
 * Onde um componente da biblioteca aparece ao ser aberto: no chat (aba da gaveta) ou flutuando.
 * O usuário escolhe o padrão por componente; com o editor de imagem aberto ele sempre flutua,
 * e onde ele já está aberto vale mais que o padrão (só reabrir depois de fechado segue o padrão).
 */
object AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando {
    enum class Modo(val chave: String) {
        FLUTUANDO("flutuando"),
        NO_CHAT("chat"),
        ;

        companion object {
            fun daChave(chave: String?): Modo = entries.firstOrNull { it.chave == chave } ?: FLUTUANDO
        }
    }

    /** OCULTO = janela pré-carregada (warm-start) ou fechada pelo usuário: conta como não aberta. */
    enum class Onde { FECHADO, OCULTO, FLUTUANDO, NO_CHAT }

    enum class Acao {
        ABRIR_FLUTUANDO,
        ABRIR_NO_CHAT,
        FOCAR_FLUTUANDO,
        FOCAR_NO_CHAT,
        TIRAR_DO_CHAT_E_FLUTUAR,
    }

    fun decidir(padrao: Modo, editorDeImagemAberto: Boolean, onde: Onde): Acao = when {
        editorDeImagemAberto -> when (onde) {
            Onde.NO_CHAT -> Acao.TIRAR_DO_CHAT_E_FLUTUAR
            Onde.FLUTUANDO -> Acao.FOCAR_FLUTUANDO
            else -> Acao.ABRIR_FLUTUANDO
        }
        onde == Onde.NO_CHAT -> Acao.FOCAR_NO_CHAT
        onde == Onde.FLUTUANDO -> Acao.FOCAR_FLUTUANDO
        padrao == Modo.NO_CHAT -> Acao.ABRIR_NO_CHAT
        else -> Acao.ABRIR_FLUTUANDO
    }

    /**
     * Ao abrir a gaveta do chat: só entra quem está como "No chat" com a opção ligada, e só se ainda
     * não estiver aberto (flutuando o usuário já escolheu onde ver); editor de imagem aberto não puxa nada.
     */
    fun deveAbrirJuntoComOChat(padrao: Modo, abrirJunto: Boolean, editorDeImagemAberto: Boolean, onde: Onde): Boolean =
        padrao == Modo.NO_CHAT && abrirJunto && !editorDeImagemAberto && (onde == Onde.FECHADO || onde == Onde.OCULTO)

    fun chaveDaPreferencia(idDoComponente: String): String = "abertura_padrao_do_componente_$idDoComponente"

    fun chaveDeAbrirJuntoComOChat(idDoComponente: String): String = "abrir_junto_com_o_chat_$idDoComponente"
}

package com.chromebrothers.navegador.regras

enum class ModoDaBarraDoNavegadorPorLargura { COMPACTA, AMPLA }

enum class ControleDaBarraDoNavegador {
    VOLTAR,
    AVANCAR,
    RECARREGAR,
    INICIO,
    ENDERECO,
    FIXAR_ATALHO,
    VERSAO_COMPUTADOR,
    CAMPO_TEXTO,
    BLOQUEIO_ANUNCIO,
    CAPTURA_TELA,
    CAPTURA_MIDIA,
    BUSCAR_NA_PAGINA,
    EXTENSOES,
    BIBLIOTECA,
    CHAT,
    MENU_OVERFLOW,
    FAIXA_DE_ABAS,
    NOVA_ABA_NA_FAIXA,
}

/**
 * Em telas estreitas (phone / capa do Fold) a barra fica enxuta:
 * voltar + endereço + overflow; abas só pelo contador.
 * Em telas amplas o overflow fica com ações ocasionais
 * (favoritar, extensões, bloqueio, capturar mídia).
 * Apresentação dos sites (user agent) fica só no menu ⋮ como ajuste global.
 */
object DisposicaoDaBarraDoNavegadorPorLarguraDaTela {
    const val LIMITE_AMPLA_EM_DP = 700

    fun modo(larguraEmDp: Int): ModoDaBarraDoNavegadorPorLargura {
        return if (larguraEmDp >= LIMITE_AMPLA_EM_DP) {
            ModoDaBarraDoNavegadorPorLargura.AMPLA
        } else {
            ModoDaBarraDoNavegadorPorLargura.COMPACTA
        }
    }

    fun visivelNaBarra(
        controle: ControleDaBarraDoNavegador,
        modo: ModoDaBarraDoNavegadorPorLargura,
    ): Boolean {
        if (controle == ControleDaBarraDoNavegador.VERSAO_COMPUTADOR) return false
        return when (modo) {
            ModoDaBarraDoNavegadorPorLargura.AMPLA -> when (controle) {
                ControleDaBarraDoNavegador.FIXAR_ATALHO,
                ControleDaBarraDoNavegador.BLOQUEIO_ANUNCIO,
                ControleDaBarraDoNavegador.CAPTURA_MIDIA,
                ControleDaBarraDoNavegador.EXTENSOES,
                -> false
                else -> true
            }
            ModoDaBarraDoNavegadorPorLargura.COMPACTA -> when (controle) {
                ControleDaBarraDoNavegador.VOLTAR,
                ControleDaBarraDoNavegador.ENDERECO,
                ControleDaBarraDoNavegador.MENU_OVERFLOW,
                -> true
                ControleDaBarraDoNavegador.FAIXA_DE_ABAS,
                ControleDaBarraDoNavegador.NOVA_ABA_NA_FAIXA,
                -> false
                else -> false
            }
        }
    }

    fun vaiParaOMenuOverflow(
        controle: ControleDaBarraDoNavegador,
        modo: ModoDaBarraDoNavegadorPorLargura,
    ): Boolean {
        if (controle == ControleDaBarraDoNavegador.VERSAO_COMPUTADOR) return false
        return when (modo) {
            ModoDaBarraDoNavegadorPorLargura.AMPLA -> when (controle) {
                ControleDaBarraDoNavegador.FIXAR_ATALHO,
                ControleDaBarraDoNavegador.EXTENSOES,
                ControleDaBarraDoNavegador.BLOQUEIO_ANUNCIO,
                ControleDaBarraDoNavegador.CAPTURA_MIDIA,
                -> true
                else -> false
            }
            ModoDaBarraDoNavegadorPorLargura.COMPACTA -> when (controle) {
                ControleDaBarraDoNavegador.AVANCAR,
                ControleDaBarraDoNavegador.RECARREGAR,
                ControleDaBarraDoNavegador.INICIO,
                ControleDaBarraDoNavegador.FIXAR_ATALHO,
                ControleDaBarraDoNavegador.CAMPO_TEXTO,
                ControleDaBarraDoNavegador.BLOQUEIO_ANUNCIO,
                ControleDaBarraDoNavegador.CAPTURA_TELA,
                ControleDaBarraDoNavegador.CAPTURA_MIDIA,
                ControleDaBarraDoNavegador.BUSCAR_NA_PAGINA,
                ControleDaBarraDoNavegador.EXTENSOES,
                ControleDaBarraDoNavegador.BIBLIOTECA,
                ControleDaBarraDoNavegador.CHAT,
                ControleDaBarraDoNavegador.NOVA_ABA_NA_FAIXA,
                -> true
                else -> false
            }
        }
    }
}

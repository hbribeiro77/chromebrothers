package com.chromebrothers.navegador.regras

/**
 * Modo imersivo: esconde barra de status, softkeys e a barra de tarefas da Samsung; elas voltam por
 * alguns segundos ao deslizar da borda. Liga no navegador todo (opção do menu), no editor de imagem
 * e quando um site pede tela cheia.
 */
object ModoImersivoDaJanelaDoNavegador {
    fun deveEsconderAsBarras(
        ligadoNoMenu: Boolean,
        editorDeImagemAberto: Boolean,
        siteEmTelaCheia: Boolean,
        /** Player sobreposto da página aberto: tela cheia como o player nativo (sem status e teclas). */
        playerDaPaginaAberto: Boolean = false,
    ): Boolean = ligadoNoMenu || editorDeImagemAberto || siteEmTelaCheia || playerDaPaginaAberto
}

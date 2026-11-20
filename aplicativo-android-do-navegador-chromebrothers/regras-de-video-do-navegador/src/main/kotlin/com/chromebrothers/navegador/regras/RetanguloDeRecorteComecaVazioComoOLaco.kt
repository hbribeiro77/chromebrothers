package com.chromebrothers.navegador.regras

/**
 * Ao entrar em retângulo (sem atalho Imagem), a seleção começa vazia —
 * o usuário desenha, como no laço.
 */
object RetanguloDeRecorteComecaVazioComoOLaco {
    fun devePreSelecionarAoEntrar(selecaoInteligenteArmada: Boolean): Boolean = selecaoInteligenteArmada

    fun retanguloInicialVazio(): CaixaDoLacoLivre = CaixaDoLacoLivre(0f, 0f, 0f, 0f)
}

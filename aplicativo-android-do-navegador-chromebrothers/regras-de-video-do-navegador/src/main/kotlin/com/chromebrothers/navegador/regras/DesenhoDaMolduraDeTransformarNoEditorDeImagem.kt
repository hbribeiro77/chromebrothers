package com.chromebrothers.navegador.regras

/**
 * Depois do Mover (nova camada) ficamos em Recorte sem seleção ativa;
 * a moldura de transformar (cor distinta, alças, cabo) ainda precisa aparecer.
 */
object DesenhoDaMolduraDeTransformarNoEditorDeImagem {
    fun sobreFerramentaDeRecorte(
        pecaEmModoTransformar: Boolean,
        selecaoAtivaDeRecorte: Boolean,
    ): Boolean = pecaEmModoTransformar && !selecaoAtivaDeRecorte
}

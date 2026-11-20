package com.chromebrothers.navegador.regras

object DecisaoDeGravarRascunhoDaEdicaoDeImagem {
    fun deveGravar(quantidadeDePassos: Int): Boolean = quantidadeDePassos > 0
}

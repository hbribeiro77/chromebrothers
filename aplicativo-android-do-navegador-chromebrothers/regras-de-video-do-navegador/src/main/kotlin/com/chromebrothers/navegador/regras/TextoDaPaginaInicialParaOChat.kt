package com.chromebrothers.navegador.regras

object TextoDaPaginaInicialParaOChat {
    fun descrever(atalhos: List<AtalhoDaPaginaInicial>): String {
        val lista = if (atalhos.isEmpty()) {
            "Nenhum atalho fixado."
        } else {
            atalhos.joinToString("\n") { atalho ->
                val nome = atalho.titulo.trim().ifBlank { atalho.endereco.trim() }
                "- $nome — ${atalho.endereco.trim()}"
            }
        }
        return "Esta é a página inicial do ChromeBrothers, não um site. " +
            "Os atalhos abaixo estão fixados nesta aba e podem ser abertos pelo endereço.\n" +
            lista +
            "\nHá também o atalho Vídeos, da biblioteca do navegador, e o campo para pesquisar ou digitar um endereço."
    }
}

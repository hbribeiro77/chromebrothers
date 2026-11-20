package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDaEscolhaAutomaticaDeVinculoDoCampoNaPagina {
    @Test
    fun escolheTextareaDePromptEBotaoDeGerarEntreDistracoes() {
        val itens = listOf(
            campo("search", "q", "Buscar", "INPUT", ""),
            campo("prompt", "prompt", "Prompt", "TEXTAREA", ""),
            campo("email", "email", "E-mail", "INPUT", ""),
            envio("cancel", "", "Cancelar", "BUTTON", "button"),
            envio("go", "", "Gerar", "BUTTON", "button"),
            envio("clear", "", "Limpar", "BUTTON", "button"),
        )
        val escolha = EscolhaAutomaticaDeVinculoDoCampoNaPagina.escolher(itens)
        assertEquals("prompt", escolha.campo?.id)
        assertEquals("Gerar", escolha.envio?.rotulo)
        assertEquals(
            "Vinculei a caixa Prompt e o botão Gerar.",
            EscolhaAutomaticaDeVinculoDoCampoNaPagina.mensagem(escolha),
        )
    }

    @Test
    fun seSoHouverUmaCaixaEUmBotaoUsaEles() {
        val itens = listOf(
            campo("", "msg", "Mensagem", "TEXTAREA", "textbox"),
            envio("", "", "Enviar", "BUTTON", ""),
        )
        val escolha = EscolhaAutomaticaDeVinculoDoCampoNaPagina.escolher(itens)
        assertEquals("msg", escolha.campo?.nome)
        assertEquals("Enviar", escolha.envio?.rotulo)
    }

    @Test
    fun paginaVaziaNaoEscolheNada() {
        val escolha = EscolhaAutomaticaDeVinculoDoCampoNaPagina.escolher(emptyList())
        assertNull(escolha.campo)
        assertNull(escolha.envio)
        assertEquals(
            "Não achei caixa nem botão nesta página.",
            EscolhaAutomaticaDeVinculoDoCampoNaPagina.mensagem(escolha),
        )
    }

    @Test
    fun soCaixaSemBotaoRelataParcial() {
        val escolha = EscolhaAutomaticaDeVinculoDoCampoNaPagina.escolher(
            listOf(campo("a", "", "Descreva", "TEXTAREA", "")),
        )
        assertEquals("a", escolha.campo?.id)
        assertNull(escolha.envio)
        assertEquals(
            "Vinculei a caixa Descreva. Não achei botão de envio.",
            EscolhaAutomaticaDeVinculoDoCampoNaPagina.mensagem(escolha),
        )
    }

    private fun campo(
        id: String,
        nome: String,
        rotulo: String,
        tag: String,
        papel: String,
    ) = CampoVinculavelDaPagina(
        "campo",
        MarcaDoElementoVinculado(id, nome, rotulo, tag, papel),
    )

    private fun envio(
        id: String,
        nome: String,
        rotulo: String,
        tag: String,
        papel: String,
    ) = CampoVinculavelDaPagina(
        "envio",
        MarcaDoElementoVinculado(id, nome, rotulo, tag, papel),
    )
}

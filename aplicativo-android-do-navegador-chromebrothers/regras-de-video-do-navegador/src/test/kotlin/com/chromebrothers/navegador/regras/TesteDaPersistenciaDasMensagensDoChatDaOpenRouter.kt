package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDaPersistenciaDasMensagensDoChatDaOpenRouter {
    @Test
    fun roundtripDeMensagemUserComTexto() {
        val original = listOf(MensagemDeChat(papel = "user", conteudo = "Olá \"mundo\""))
        val json = PersistenciaDasMensagensDoChatDaOpenRouter.escrever(original)
        val lido = PersistenciaDasMensagensDoChatDaOpenRouter.ler(json)
        assertEquals(original, lido)
    }

    @Test
    fun roundtripComPartesImagemChamadasEMensagemTool() {
        val original = listOf(
            MensagemDeChat(
                papel = "user",
                conteudo = "veja a imagem",
                partes = listOf(
                    ParteDeConteudoDoChat(
                        tipo = "imagem",
                        mime = "image/png",
                        dados = "iVBORw0KGgo=",
                    ),
                ),
            ),
            MensagemDeChat(
                papel = "assistant",
                chamadas = listOf(
                    ChamadaDeFerramenta(
                        id = "call_1",
                        nome = "ler_pagina",
                        argumentos = """{"seletor":"h1"}""",
                    ),
                ),
            ),
            MensagemDeChat(
                papel = "tool",
                conteudo = "título",
                idDaChamada = "call_1",
            ),
        )
        val lido = PersistenciaDasMensagensDoChatDaOpenRouter.ler(
            PersistenciaDasMensagensDoChatDaOpenRouter.escrever(original),
        )
        assertEquals(original, lido)
    }

    @Test
    fun jsonInvalidoDevolveListaVazia() {
        assertEquals(emptyList(), PersistenciaDasMensagensDoChatDaOpenRouter.ler("{}"))
        assertEquals(emptyList(), PersistenciaDasMensagensDoChatDaOpenRouter.ler("lixo"))
    }
}

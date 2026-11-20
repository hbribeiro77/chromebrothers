package com.chromebrothers.navegador.regras

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

class TesteDasFerramentasDeExploracaoDaPaginaNoChatDaOpenRouter {
    @Test
    fun jsonTrazAsTresFerramentasDeExploracao() {
        val json = FerramentasDeLeituraDoChatDaOpenRouter.json()
        assertTrue(json.contains("\"ler_texto_da_pagina\""))
        assertTrue(json.contains("\"consultar_elementos\""))
        assertTrue(json.contains("\"rolar_pagina\""))
    }

    @Test
    fun jsonTrazAsPropriedadesChave() {
        val json = FerramentasDeLeituraDoChatDaOpenRouter.json()
        listOf("offset", "limite", "seletor", "y", "paginas", "pixels").forEach {
            assertTrue(json.contains("\"$it\""), "faltou $it")
        }
        assertTrue(json.contains("\"required\":[\"seletor\"]"))
    }

    @Test
    fun jsonContinuaSendoUmaListaBalanceada() {
        val json = FerramentasDeLeituraDoChatDaOpenRouter.json()
        assertEquals(json.count { it == '{' }, json.count { it == '}' })
        assertEquals(json.count { it == '[' }, json.count { it == ']' })
    }

    @Test
    fun permitidasContemAsTresFerramentas() {
        val permitidas = FerramentasDeLeituraDoChatDaOpenRouter.permitidas
        assertTrue(FerramentasDeLeituraDoChatDaOpenRouter.TEXTO_PAGINADO in permitidas)
        assertTrue(FerramentasDeLeituraDoChatDaOpenRouter.ELEMENTOS in permitidas)
        assertTrue(FerramentasDeLeituraDoChatDaOpenRouter.ROLAR in permitidas)
    }

    @Test
    fun instrucaoOrientaPaginaGrande() {
        val texto = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("aba")
        assertTrue(texto.contains("consultar_elementos"))
        assertTrue(texto.contains("rolar_pagina"))
        assertTrue(texto.contains("ler_texto_da_pagina"))
    }

    @Test
    fun rotulosConhecemAsNovasFerramentas() {
        listOf("ler_texto_da_pagina", "consultar_elementos", "rolar_pagina").forEach {
            assertFalse(FerramentasDeLeituraDoChatDaOpenRouter.rotuloVisivel(it) == "Ferramenta ignorada")
            assertFalse(FerramentasDeLeituraDoChatDaOpenRouter.rotuloEmAndamento(it) == "Usando uma ferramenta…")
        }
    }
}

package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaListagemDeModelosDoHiggsfieldNoChat {
    @Test
    fun filtroOpcionalEhTrimadoETemLimite() {
        assertEquals(
            "",
            ListagemDeModelosDoHiggsfieldNoChat.filtroDosArgumentos("""{}"""),
        )
        assertEquals(
            "nano banana",
            ListagemDeModelosDoHiggsfieldNoChat.filtroDosArgumentos("""{"filtro":" nano banana "}"""),
        )
        val longo = "x".repeat(ListagemDeModelosDoHiggsfieldNoChat.LIMITE_DO_FILTRO + 1)
        assertEquals(
            "O filtro passou do limite.",
            ListagemDeModelosDoHiggsfieldNoChat.mensagemSeFiltroInvalido(
                """{"filtro":"$longo"}""",
            ),
        )
    }

    @Test
    fun formatarFiltraPorTrechoSemCase() {
        val bruto = """
            nano_banana — Nano Banana
            nano_banana_2_1 — Nano Banana 2.1
            nano_banana_2 — Nano Banana Pro
            seedance_2_0 — Seedance 2.0
            wan2_7 — Wan 2.7
        """.trimIndent()
        val filtrado = ListagemDeModelosDoHiggsfieldNoChat.formatarCatalogo(bruto, "nanobanana")
        assertTrue(filtrado.contains("nano_banana"))
        assertTrue(filtrado.contains("nano_banana_2_1"))
        assertFalse(filtrado.contains("seedance"))
        val vazio = ListagemDeModelosDoHiggsfieldNoChat.formatarCatalogo(bruto, "xyz-inexistente")
        assertTrue(vazio.contains("Nenhum modelo"))
    }

    @Test
    fun formatarLêItemsJsonDoModelsExplore() {
        val bruto = """
            {"items":[
              {"id":"nano_banana","name":"Nano Banana","description":"budget","output_type":"image"},
              {"id":"nano_banana_2_1","name":"Nano Banana 2.1","description":"refs","output_type":"image"},
              {"id":"seedance_2_0","name":"Seedance","description":"video","output_type":"video"}
            ]}
        """.trimIndent()
        val soImagem = ListagemDeModelosDoHiggsfieldNoChat.formatarCatalogo(bruto, "imagem")
        assertTrue(soImagem.contains("nano_banana_2_1"))
        assertTrue(soImagem.contains("Nano Banana 2.1"))
        assertFalse(soImagem.contains("seedance_2_0"))
    }

    @Test
    fun jsonDosItensParaPonteExtraiIdENome() {
        val bruto = """{"items":[{"id":"soul_2","name":"Soul","output_type":"image"}]}"""
        val json = ListagemDeModelosDoHiggsfieldNoChat.jsonDosItensParaPonte(bruto)
        assertTrue(json.contains("\"ok\":true"))
        assertTrue(json.contains("soul_2"))
        assertTrue(json.contains("Soul"))
    }

    @Test
    fun cursorDaProximaPaginaLeNextPageToken() {
        val comMais = """{"items":[],"has_more":true,"next_page_token":"20"}"""
        assertEquals("20", ListagemDeModelosDoHiggsfieldNoChat.cursorDaProximaPagina(comMais))
        val fim = """{"items":[],"has_more":false,"next_page_token":"40"}"""
        assertEquals(null, ListagemDeModelosDoHiggsfieldNoChat.cursorDaProximaPagina(fim))
    }

    @Test
    fun mesclarPaginasUneSemDuplicarEPreferidoVaiNaFrente() {
        val p1 = """{"items":[{"id":"a","name":"A","output_type":"video"}],"has_more":true,"next_page_token":"1"}"""
        val p2 = """{"items":[{"id":"wan3_0","name":"Wan 3.0","output_type":"video"},{"id":"a","name":"A","output_type":"video"}]}"""
        val mesclado = ListagemDeModelosDoHiggsfieldNoChat.mesclarPaginasDeCatalogo(listOf(p1, p2))
        assertTrue(mesclado.contains("wan3_0"))
        assertEquals(2, Regex(""""id":""").findAll(mesclado).count())
        val ponte = ListagemDeModelosDoHiggsfieldNoChat.jsonDosItensParaPonte(mesclado, idPreferido = "wan3_0")
        assertTrue(ponte.indexOf("wan3_0") < ponte.indexOf("\"id\":\"a\""))
    }

    @Test
    fun semFiltroDevolveCatalogoTruncadoSePreciso() {
        val bruto = "modelo_a\n".repeat(5000)
        val texto = ListagemDeModelosDoHiggsfieldNoChat.formatarCatalogo(bruto, "")
        assertTrue(texto.length <= ListagemDeModelosDoHiggsfieldNoChat.LIMITE_DO_TEXTO + 80)
        assertTrue(texto.contains("truncado") || texto.length <= ListagemDeModelosDoHiggsfieldNoChat.LIMITE_DO_TEXTO)
    }
}

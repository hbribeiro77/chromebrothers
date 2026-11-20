package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaAprovacaoDoPlanoDoChat {
    private val plano = """
        ## Plano
        1. Abrir https://www.sjinn.ai/app com abrir_endereco.
        2. clicar_no_texto em "Generate".
        3. escrever_na_pagina com o prompt.
        4. gerar_video_vivix 9:16, 10 s (gasta créditos).
        5. guardar_nota_do_site com o caminho.
    """.trimIndent()

    @Test
    fun aprovaAsAcoesCitadasMenosCreditosENota() {
        val aprovacao = AprovacaoDoPlanoDoChat.doPlano(plano, "https://example.com/x")
        assertEquals(
            setOf("abrir_endereco", "clicar_no_texto", "escrever_na_pagina"),
            aprovacao.acoes,
        )
        assertEquals(setOf("sjinn.ai", "example.com"), aprovacao.sites)
        assertEquals(
            listOf("gerar_video_vivix", "guardar_nota_do_site"),
            AprovacaoDoPlanoDoChat.aindaPedem(plano).sorted(),
        )
    }

    @Test
    fun soCobreOsSitesDoPlanoEDaAba() {
        val aprovacao = AprovacaoDoPlanoDoChat.doPlano(plano, "https://example.com/x")
        val clique = PedidoDeAcaoConfirmadaDoChat.Clicar("Generate")
        assertTrue(aprovacao.cobre("clicar_no_texto", clique, "https://sjinn.ai/app"))
        assertTrue(aprovacao.cobre("clicar_no_texto", clique, "https://example.com/y"))
        assertFalse(aprovacao.cobre("clicar_no_texto", clique, "https://banco.com.br"))
        val outro = PedidoDeAcaoConfirmadaDoChat.AbrirEndereco("https://banco.com.br")
        assertFalse(aprovacao.cobre("abrir_endereco", outro, "https://sjinn.ai"))
    }

    @Test
    fun acaoNaoCitadaContinuaPedindo() {
        val aprovacao = AprovacaoDoPlanoDoChat.doPlano(plano, "https://sjinn.ai")
        val preencher = PedidoDeAcaoConfirmadaDoChat.PreencherCampo("oi")
        assertFalse(aprovacao.cobre("preencher_campo_de_texto", preencher, "https://sjinn.ai"))
    }

    @Test
    fun nomeDentroDeOutraPalavraNaoConta() {
        val aprovacao = AprovacaoDoPlanoDoChat.doPlano("## Plano\n1. xclicar_no_textox\n2. nada", "")
        assertTrue(aprovacao.acoes.isEmpty())
    }
}

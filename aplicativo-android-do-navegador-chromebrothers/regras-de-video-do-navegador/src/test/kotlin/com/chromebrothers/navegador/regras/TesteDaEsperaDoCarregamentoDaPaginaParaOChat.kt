package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaEsperaDoCarregamentoDaPaginaParaOChat {
    @Test
    fun ignoraAParadaAntigaESoContaAParadaDepoisDoComeco() {
        val espera = EsperaDoCarregamentoDaPaginaParaOChat()
        assertEquals(PassoDaEsperaDoCarregamento(), espera.parada(true))
        assertEquals(PassoDaEsperaDoCarregamento(), espera.comeco())
        assertEquals(PassoDaEsperaDoCarregamento(agendarPausa = true), espera.parada(true))
    }

    @Test
    fun textoCurtoPedeOutraLeituraEAvisaQueAindaPodeEstarMontando() {
        val espera = EsperaDoCarregamentoDaPaginaParaOChat(jaViuComeco = true)
        assertEquals(PassoDaEsperaDoCarregamento(agendarPausa = true), espera.parada(true))
        assertEquals(PassoDaEsperaDoCarregamento(lerTexto = true), espera.pausaAcabou())
        assertEquals(PassoDaEsperaDoCarregamento(agendarPausa = true), espera.textoLido("oi"))
        assertEquals(PassoDaEsperaDoCarregamento(lerTexto = true), espera.pausaAcabou())
        assertEquals(PassoDaEsperaDoCarregamento(pronto = true), espera.textoLido("oi"))
        val relato = RelatoDoCarregamentoDaPaginaParaOChat.relato(
            endereco = "https://exemplo",
            titulo = "Exemplo",
            texto = espera.texto,
            sucesso = espera.sucesso,
            tempoEsgotado = espera.tempoEsgotado,
        )
        assertTrue(relato.startsWith("A página terminou de carregar, mas o texto visível ainda é curto."))
        assertTrue(relato.contains("oi"))
    }

    @Test
    fun textoLongoEncerraNaPrimeiraLeitura() {
        val espera = EsperaDoCarregamentoDaPaginaParaOChat(jaViuComeco = true)
        espera.parada(true)
        espera.pausaAcabou()
        val longo = "a".repeat(EsperaDoCarregamentoDaPaginaParaOChat.LIMIAR_DO_TEXTO_CURTO)
        assertEquals(PassoDaEsperaDoCarregamento(pronto = true), espera.textoLido(longo))
        val relato = RelatoDoCarregamentoDaPaginaParaOChat.relato(
            endereco = "https://exemplo",
            titulo = "Exemplo",
            texto = longo,
            sucesso = true,
            tempoEsgotado = false,
        )
        assertTrue(relato.startsWith("A página carregou."))
    }

    @Test
    fun tempoEsgotadoDizQueAindaEstaCarregando() {
        val espera = EsperaDoCarregamentoDaPaginaParaOChat()
        assertEquals(
            PassoDaEsperaDoCarregamento(cancelarPausa = true, pronto = true),
            espera.tempoEsgotado(),
        )
        val relato = RelatoDoCarregamentoDaPaginaParaOChat.relato(
            endereco = "https://exemplo",
            titulo = "",
            texto = "",
            sucesso = true,
            tempoEsgotado = true,
        )
        assertTrue(relato.startsWith("Ainda carregando depois de 20s."))
    }

    @Test
    fun comecoDuranteAPausaVoltaAEsperarEAFalhaNaoViraTextoCurto() {
        val espera = EsperaDoCarregamentoDaPaginaParaOChat(jaViuComeco = true)
        espera.parada(true)
        assertEquals(PassoDaEsperaDoCarregamento(cancelarPausa = true), espera.comeco())
        assertEquals(PassoDaEsperaDoCarregamento(agendarPausa = true), espera.parada(false))
        espera.pausaAcabou()
        val longo = "a".repeat(EsperaDoCarregamentoDaPaginaParaOChat.LIMIAR_DO_TEXTO_CURTO)
        assertEquals(PassoDaEsperaDoCarregamento(pronto = true), espera.textoLido(longo))
        val relato = RelatoDoCarregamentoDaPaginaParaOChat.relato(
            endereco = "https://exemplo",
            titulo = "Exemplo",
            texto = longo,
            sucesso = espera.sucesso,
            tempoEsgotado = false,
            criouAba = true,
        )
        assertTrue(relato.startsWith("Criei uma aba.\nO carregamento falhou."))
    }

    @Test
    fun aInstrucaoNaoTrataTextoCurtoComoFalhaDeRenderizacao() {
        val texto = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: exemplo")
        assertTrue(texto.contains("não renderizou"))
    }
}

package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoScrubFinoPorToqueLongoNoPlayerDeVideo {
    @Test
    fun filmstripCobreDoZeroAoFimComPasso() {
        val tempos = ScrubFinoPorToqueLongoNoPlayerDeVideo.temposDaFilmstrip(
            duracaoMs = 3_500L,
            passoMs = 1_000L,
        )
        assertEquals(listOf(0L, 1_000L, 2_000L, 3_000L, 3_500L), tempos)
    }

    @Test
    fun deslocamentoZeroNoInicioEMaximoNoFim() {
        val passo = 1_000L
        val celula = 40f
        val tempos = ScrubFinoPorToqueLongoNoPlayerDeVideo.temposDaFilmstrip(5_000L, passo)
        assertEquals(6, tempos.size)
        assertEquals(
            0f,
            ScrubFinoPorToqueLongoNoPlayerDeVideo.deslocamentoDaFaixaPx(0L, tempos, celula),
            absoluteTolerance = 0.01f,
        )
        assertEquals(
            -200f,
            ScrubFinoPorToqueLongoNoPlayerDeVideo.deslocamentoDaFaixaPx(5_000L, tempos, celula),
            absoluteTolerance = 0.01f,
        )
    }

    @Test
    fun arrasteParaEsquerdaAvancaPeloDeslocamentoDaFaixa() {
        val tempos = ScrubFinoPorToqueLongoNoPlayerDeVideo.temposDaFilmstrip(60_000L, 1_000L)
        val celula = 50f
        val inicio = ScrubFinoPorToqueLongoNoPlayerDeVideo.deslocamentoDaFaixaPx(10_000L, tempos, celula)
        // Dedo para a esquerda: deslocamento do dedo negativo → faixa mais à esquerda → tempo maior.
        val novoDeslocamento = inicio + (-100f)
        val tempo = ScrubFinoPorToqueLongoNoPlayerDeVideo.tempoApartirDoDeslocamentoPx(
            novoDeslocamento,
            tempos,
            celula,
        )
        assertEquals(12_000L, tempo)
    }

    @Test
    fun preenchimentoCentralizaAPrimeiraCelula() {
        assertEquals(
            180,
            ScrubFinoPorToqueLongoNoPlayerDeVideo.preenchimentoLateralParaCentralizar(
                larguraDoViewportPx = 400,
                larguraDaCelulaPx = 40,
            ),
        )
    }

    @Test
    fun formataMinutosEHoras() {
        assertEquals("0:05", ScrubFinoPorToqueLongoNoPlayerDeVideo.formatarTempo(5_000L))
        assertEquals("1:01", ScrubFinoPorToqueLongoNoPlayerDeVideo.formatarTempo(61_000L))
        assertEquals("1:01:01", ScrubFinoPorToqueLongoNoPlayerDeVideo.formatarTempo(3_661_000L))
    }

    @Test
    fun passoAumentaSeOVideoForMuitoLongo() {
        val passo = ScrubFinoPorToqueLongoNoPlayerDeVideo.passoDaFilmstrip(
            duracaoMs = 600_000L,
            maximoDeCelulas = 24,
        )
        assertEquals(26_086L, passo)
        val quantidade = ScrubFinoPorToqueLongoNoPlayerDeVideo.temposDaFilmstrip(600_000L, passo).size
        assertEquals(true, quantidade in 23..25)
    }

    @Test
    fun larguraTotalDaFaixaUltrapassaATelaParaNaoCortarCelulas() {
        val largura = ScrubFinoPorToqueLongoNoPlayerDeVideo.larguraTotalDaFaixaPx(
            quantidadeDeCelulas = 24,
            larguraDaCelulaPx = 168,
            espacoEntreCelulasPx = 2,
            preenchimentoLateralPx = 400,
        )
        // 24*168 + 23*2 + 800 = 4032+46+800 = 4878 — bem maior que uma tela típica
        assertEquals(4_878, largura)
    }

    @Test
    fun chaveDoCacheArredondaTempoAoPasso() {
        assertEquals(
            "https://cdn/a.mp4|2000",
            ScrubFinoPorToqueLongoNoPlayerDeVideo.chaveDoQuadroEmCache(
                chaveDoVideo = "https://cdn/a.mp4",
                tempoMs = 2_400L,
                passoMs = 1_000L,
            ),
        )
        assertEquals(
            "",
            ScrubFinoPorToqueLongoNoPlayerDeVideo.chaveDoQuadroEmCache("", 1_000L),
        )
    }

    @Test
    fun trilhaSoAcompanhaComDedoSoltoEVideoTocando() {
        assertEquals(
            true,
            ScrubFinoPorToqueLongoNoPlayerDeVideo.deveAcompanharReproducaoNaTrilha(
                modoAtivo = true,
                dedoArrastando = false,
                videoTocando = true,
            ),
        )
        assertEquals(
            false,
            ScrubFinoPorToqueLongoNoPlayerDeVideo.deveAcompanharReproducaoNaTrilha(
                modoAtivo = true,
                dedoArrastando = true,
                videoTocando = true,
            ),
        )
        assertEquals(
            false,
            ScrubFinoPorToqueLongoNoPlayerDeVideo.deveAcompanharReproducaoNaTrilha(
                modoAtivo = true,
                dedoArrastando = false,
                videoTocando = false,
            ),
        )
    }

    @Test
    fun toqueCurtoNaoConfirmaArrasteAteOLimiar() {
        assertEquals(
            false,
            ScrubFinoPorToqueLongoNoPlayerDeVideo.arrasteConfirmadoPeloDeslocamento(8f, 24f),
        )
        assertEquals(
            true,
            ScrubFinoPorToqueLongoNoPlayerDeVideo.arrasteConfirmadoPeloDeslocamento(24f, 24f),
        )
    }
}

package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDoAcompanhamentoDosDownloadsDeMidiaNaGaveta {
    @Test
    fun downloadEmAndamentoFicaNaFrenteDoArquivoSalvo() {
        val acompanhamento = AcompanhamentoDosDownloadsDeMidiaNaGaveta()
        acompanhamento.iniciar("1", "Aula", 10)
        acompanhamento.progresso("1", 40, 100)
        val lista = acompanhamento.listar(
            listOf(ArquivoDeMidiaSalvoNaGaveta("content://salvo", "chromebrothers-Antigo-1700000000000.mp4", 5)),
        )
        assertEquals(listOf("1", "content://salvo"), lista.map { it.id })
        assertEquals(EstadoDoDownloadDeMidiaNaGaveta.EM_ANDAMENTO, lista.first().estado)
        assertEquals("40% · 100 B", TextoDoProgressoDoDownloadDeMidiaNaGaveta.texto(lista.first()))
    }

    @Test
    fun arquivoSalvoNaoRepeteODownloadQueJaTerminou() {
        val acompanhamento = AcompanhamentoDosDownloadsDeMidiaNaGaveta()
        acompanhamento.iniciar("1", "Aula", 10)
        acompanhamento.concluir("1", "content://novo", "Aula", 20)
        val lista = acompanhamento.listar(
            listOf(ArquivoDeMidiaSalvoNaGaveta("content://novo", "chromebrothers-Aula-1700000000000.mp4", 20)),
        )
        assertEquals(1, lista.size)
        assertEquals(EstadoDoDownloadDeMidiaNaGaveta.PRONTO, lista.single().estado)
        assertEquals("content://novo", lista.single().uri)
    }

    @Test
    fun listaMostraSoVideoComOTamanho() {
        val lista = AcompanhamentoDosDownloadsDeMidiaNaGaveta().listar(
            listOf(
                ArquivoDeMidiaSalvoNaGaveta("content://som", "faixa.m4a", 1, 1_000, "audio/mp4"),
                ArquivoDeMidiaSalvoNaGaveta(
                    "content://filme",
                    "chromebrothers-Aula-1700000000000.mp4",
                    2,
                    80_000_000,
                    "video/mp4",
                ),
            ),
        )
        assertEquals("content://filme", lista.single().uri)
        assertEquals("Pronto · 76,3 MB", TextoDoProgressoDoDownloadDeMidiaNaGaveta.texto(lista.single()))
    }

    @Test
    fun renomearConservaAExtensaoDoVideo() {
        assertEquals(
            "Aula nova.mp4",
            NomeRenomeadoDoArquivoDeMidiaBaixado.montar("chromebrothers-Aula-1.mp4", "Aula nova"),
        )
        assertEquals(null, NomeRenomeadoDoArquivoDeMidiaBaixado.montar("a.mp4", "   "))
    }

    @Test
    fun falhaContinuaNaListaECancelamentoSome() {
        val acompanhamento = AcompanhamentoDosDownloadsDeMidiaNaGaveta()
        acompanhamento.iniciar("1", "Aula", 10)
        acompanhamento.falhar("1", "O site recusou o download.")
        acompanhamento.iniciar("2", "Outro", 11)
        acompanhamento.cancelar("2")
        val lista = acompanhamento.listar(emptyList())
        assertEquals("1", lista.single().id)
        assertEquals("O site recusou o download.", TextoDoProgressoDoDownloadDeMidiaNaGaveta.texto(lista.single()))
    }

    @Test
    fun tituloDoArquivoTiraOPrefixoEOInstante() {
        assertEquals(
            "Aula de hoje",
            TituloDoArquivoDeMidiaBaixadoParaAGaveta.texto("chromebrothers-Aula de hoje-1700000000000.mp4"),
        )
        assertTrue(TituloDoArquivoDeMidiaBaixadoParaAGaveta.texto("video.mp4") == "video")
    }
}

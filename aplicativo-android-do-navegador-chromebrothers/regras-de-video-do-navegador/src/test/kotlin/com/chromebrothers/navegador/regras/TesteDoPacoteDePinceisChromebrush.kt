package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.PacoteDePinceisChromebrush as Pacote
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDoPacoteDePinceisChromebrush {

    private val comImagens = PincelDoMotor(
        id = "meu-aaaa", nome = "Carimbo",
        forma = FormaDoCarimbo.IMAGEM, arquivoDaForma = "forma-1.png",
        granulado = FonteDoGranulado.IMAGEM, arquivoDoGranulado = "forma-2.png",
    )
    private val simples = PincelDoMotor(id = "meu-bbbb", nome = "Simples")

    @Test
    fun manifestoIdaEVolta() {
        val lidos = Pacote.lerManifesto(Pacote.manifesto(listOf(comImagens, simples)))
        assertEquals(listOf(comImagens, simples), lidos)
        assertEquals(setOf("forma-1.png", "forma-2.png"), Pacote.arquivosUsados(lidos))
    }

    @Test
    fun soImagensComNomeSeguro() {
        assertEquals("forma-1.png", Pacote.imagemDaEntrada("imagens/forma-1.png"))
        assertNull(Pacote.imagemDaEntrada("imagens/../segredo"))
        assertNull(Pacote.imagemDaEntrada("imagens/sub/x.png"))
        assertNull(Pacote.imagemDaEntrada("imagens/.oculta"))
        assertNull(Pacote.imagemDaEntrada("outra/x.png"))
    }

    @Test
    fun idExistenteViraNovoEImagensSaoRenomeadas() {
        var n = 0
        val prontos = Pacote.prepararImportacao(
            listOf(comImagens, simples, simples),
            idsExistentes = setOf("meu-aaaa", "giz"),
            novoId = { "novo-${++n}" },
            guardarImagem = { "local-$it" },
        )
        assertEquals("novo-1", prontos[0].id)
        assertEquals("Carimbo (importado)", prontos[0].nome)
        assertEquals("local-forma-1.png", prontos[0].arquivoDaForma)
        assertEquals("local-forma-2.png", prontos[0].arquivoDoGranulado)
        assertEquals("meu-bbbb", prontos[1].id)
        assertEquals("novo-2", prontos[2].id) // repetido no próprio pacote
    }

    @Test
    fun imagemQueFaltaNaoQuebraOPincel() {
        val pronto = Pacote.prepararImportacao(listOf(comImagens), emptySet(), { "x" }, { null }).single()
        assertEquals(FormaDoCarimbo.REDONDA, pronto.forma)
        assertEquals(FonteDoGranulado.NENHUM, pronto.granulado)
    }

    @Test
    fun nomeDoArquivoLimpo() {
        assertEquals("Meus_pincéis_v2.chromebrush", Pacote.nomeDoArquivo("Meus/pincéis:v2"))
        assertEquals("pinceis.chromebrush", Pacote.nomeDoArquivo("  "))
    }
}

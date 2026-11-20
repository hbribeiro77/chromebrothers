package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDoTextoDoRascunhoDaEdicaoDeImagem {
    @Test
    fun oDocumentoVoltaComImagemTextoETraco() {
        val original = DocumentoDoRascunhoDaEdicaoDeImagem(
            largura = 800,
            altura = 600,
            proximoId = 4,
            ferramenta = FerramentaGravadaDoRascunhoDaEdicao.RECORTE,
            imagens = listOf(ImagemDoRascunhoDaEdicao(1, "peca-0.png", 0f, 10f, 800f, 500f)),
            textos = listOf(
                TextoDoRascunhoDaEdicao(2, "Oi\nmundo", 48f, -16777216, 12f, 20f, 100f, 80f),
            ),
            tracos = listOf(
                TracoDoRascunhoDaEdicao(
                    cor = -65536,
                    borracha = false,
                    pontos = listOf(PontoDoTracoDoRascunhoDaEdicao(1.5f, 2.5f, 8f)),
                ),
            ),
        )
        val lido = TextoDoRascunhoDaEdicaoDeImagem.ler(TextoDoRascunhoDaEdicaoDeImagem.escrever(original))
        assertEquals(original, lido)
    }

    @Test
    fun oPincelDoMotorEASementeVoltamSemAtrapalharPontaEQuadro() {
        val original = documento(emptyList()).copy(
            tracos = listOf(
                TracoDoRascunhoDaEdicao(
                    cor = -16776961,
                    borracha = false,
                    pontos = listOf(PontoDoTracoDoRascunhoDaEdicao(0.1f, 0.2f, 0.01f, 128)),
                    idDaPeca = 3,
                    coordenadasDoQuadro = true,
                    pincel = "pincel-seco",
                    semente = -4_611_686_018_427_387_904L,
                ),
                TracoDoRascunhoDaEdicao(
                    cor = -65536,
                    borracha = false,
                    pontos = listOf(PontoDoTracoDoRascunhoDaEdicao(0.3f, 0.4f, 0.02f)),
                    ponta = PontaDoTracoDaComposicao.MARCADOR,
                ),
            ),
        )
        val lido = TextoDoRascunhoDaEdicaoDeImagem.ler(TextoDoRascunhoDaEdicaoDeImagem.escrever(original))
        assertEquals(original.tracos, lido?.tracos)
    }

    @Test
    fun oEspelhoDaPecaVoltaNoDocumento() {
        val original = documento(emptyList()).copy(
            espelhos = listOf(EspelhoGravadoDaPecaDaComposicao(3, horizontal = true, vertical = false)),
        )
        val lido = TextoDoRascunhoDaEdicaoDeImagem.ler(TextoDoRascunhoDaEdicaoDeImagem.escrever(original))
        assertEquals(original.espelhos, lido?.espelhos)
    }

    @Test
    fun aFolhaBrancaContinuaValendoQuandoODocumentoNaoGuardaACor() {
        val texto = """
            v1
            moldura 10 10
            proximo 1
            ferramenta VERMELHO
        """.trimIndent()
        val lido = TextoDoRascunhoDaEdicaoDeImagem.ler(texto)
        assertEquals(PaletaDoEditorDeImagem.BRANCO, lido?.corDaFolha)
        assertEquals(emptyMap(), lido?.angulos)
        assertEquals(emptyList(), lido?.ordem)
    }

    @Test
    fun aCamadaVaziaGuardaOTracoNaPeca() {
        val original = documento(listOf(imagem("peca-0.png"))).copy(
            camadasVazias = listOf(CamadaVaziaDoRascunhoDaEdicao(3, 0f, 0f, 10f, 10f)),
            tracos = listOf(
                TracoDoRascunhoDaEdicao(
                    cor = -65536,
                    borracha = false,
                    pontos = listOf(PontoDoTracoDoRascunhoDaEdicao(0.2f, 0.4f, 0.05f)),
                    idDaPeca = 1,
                ),
            ),
        )
        val lido = TextoDoRascunhoDaEdicaoDeImagem.ler(TextoDoRascunhoDaEdicaoDeImagem.escrever(original))
        assertEquals(original, lido)
    }

    @Test
    fun aPontaDoTracoVoltaEODocumentoAntigoContinuaCaneta() {
        val original = documento(emptyList()).copy(
            tracos = listOf(
                TracoDoRascunhoDaEdicao(
                    cor = -65536,
                    borracha = false,
                    pontos = listOf(PontoDoTracoDoRascunhoDaEdicao(0.2f, 0.3f, 0.04f)),
                    idDaPeca = 1,
                    ponta = PontaDoTracoDaComposicao.MARCADOR,
                ),
                TracoDoRascunhoDaEdicao(
                    cor = -16777216,
                    borracha = false,
                    pontos = listOf(
                        PontoDoTracoDoRascunhoDaEdicao(0.1f, 0.1f, 0.02f),
                        PontoDoTracoDoRascunhoDaEdicao(0.8f, 0.6f, 0.02f),
                    ),
                    idDaPeca = 1,
                    coordenadasDoQuadro = true,
                    ponta = PontaDoTracoDaComposicao.SETA,
                ),
                TracoDoRascunhoDaEdicao(
                    cor = -65536,
                    borracha = false,
                    pontos = listOf(PontoDoTracoDoRascunhoDaEdicao(0.2f, 0.2f, 0.02f, 120)),
                    idDaPeca = 1,
                    ponta = PontaDoTracoDaComposicao.LAPIS,
                ),
            ),
        )
        val lido = TextoDoRascunhoDaEdicaoDeImagem.ler(TextoDoRascunhoDaEdicaoDeImagem.escrever(original))
        assertEquals(original, lido)
        val antigo = """
            v1
            moldura 10 10
            proximo 1
            ferramenta VERMELHO
            traco -65536 0
            p 1 2 3
        """.trimIndent()
        assertEquals(
            PontaDoTracoDaComposicao.CANETA,
            TextoDoRascunhoDaEdicaoDeImagem.ler(antigo)?.tracos?.single()?.ponta,
        )
    }

    @Test
    fun oTracoDoQuadroVoltaMarcado() {
        val original = documento(emptyList()).copy(
            tracos = listOf(
                TracoDoRascunhoDaEdicao(
                    cor = -65536,
                    borracha = false,
                    pontos = listOf(PontoDoTracoDoRascunhoDaEdicao(0.15f, 0.4f, 0.02f)),
                    idDaPeca = 1,
                    coordenadasDoQuadro = true,
                ),
            ),
        )
        val lido = TextoDoRascunhoDaEdicaoDeImagem.ler(TextoDoRascunhoDaEdicaoDeImagem.escrever(original))
        assertEquals(true, lido?.tracos?.single()?.coordenadasDoQuadro)
        assertEquals(original, lido)
    }

    @Test
    fun oGiroEAOrdemVoltamNoDocumento() {
        val original = documento(listOf(imagem("peca-0.png"))).copy(
            angulos = mapOf(1 to 90f),
            ordem = listOf(2, 1),
        )
        val lido = TextoDoRascunhoDaEdicaoDeImagem.ler(TextoDoRascunhoDaEdicaoDeImagem.escrever(original))
        assertEquals(original, lido)
    }

    @Test
    fun umTextoSemVersaoNaoViraDocumento() {
        assertNull(TextoDoRascunhoDaEdicaoDeImagem.ler("moldura 10 10"))
        assertNull(TextoDoRascunhoDaEdicaoDeImagem.ler("v1\nmoldura 0 10"))
    }

    @Test
    fun aMiniaturaUsaAPrimeiraImagemComNomeSeguro() {
        val comFoto = documento(listOf(imagem("peca-0.png"), imagem("peca-1.png")))
        val semFoto = documento(emptyList())
        val comCaminho = documento(listOf(imagem("../peca-0.png"), imagem("pasta/peca-0.png")))
        assertEquals("peca-0.png", comFoto.arquivoDaPrimeiraImagem())
        assertNull(semFoto.arquivoDaPrimeiraImagem())
        assertNull(comCaminho.arquivoDaPrimeiraImagem())
    }

    @Test
    fun oNomeDaPastaDoRascunhoNaoAceitaCaminho() {
        assertEquals(true, IdentificadorDaPastaDoRascunhoDaEdicao.aceito("1710000000000"))
        assertEquals(true, IdentificadorDaPastaDoRascunhoDaEdicao.aceito("1710000000000-1000"))
        assertEquals(false, IdentificadorDaPastaDoRascunhoDaEdicao.aceito("../peca"))
        assertEquals(false, IdentificadorDaPastaDoRascunhoDaEdicao.aceito("12-"))
        assertEquals(false, IdentificadorDaPastaDoRascunhoDaEdicao.aceito("12--3"))
    }

    private fun documento(imagens: List<ImagemDoRascunhoDaEdicao>) = DocumentoDoRascunhoDaEdicaoDeImagem(
        largura = 10,
        altura = 10,
        proximoId = 1,
        ferramenta = FerramentaGravadaDoRascunhoDaEdicao.VERMELHO,
        imagens = imagens,
        textos = emptyList(),
        tracos = emptyList(),
    )

    private fun imagem(arquivo: String) = ImagemDoRascunhoDaEdicao(1, arquivo, 0f, 0f, 10f, 10f)
}

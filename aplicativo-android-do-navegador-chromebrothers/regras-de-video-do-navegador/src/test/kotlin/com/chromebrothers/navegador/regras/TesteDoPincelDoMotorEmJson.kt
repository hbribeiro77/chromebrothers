package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class TesteDoPincelDoMotorEmJson {

    private val completo = PincelDoMotor(
        id = "meu-a1b2", nome = "Meu \"giz\" grosso",
        forma = FormaDoCarimbo.IMAGEM, arquivoDaForma = "forma-1a2b3c.png",
        dureza = 0.35f, proporcao = 0.5f, espacamento = 0.12f, dispersao = 0.2f,
        tremorDeTamanho = 0.1f, tremorDeOpacidade = 0.3f, tremorDeRotacao = 0.75f,
        seguirTraco = true, anguloFixo = -30f, fluxo = 0.6f,
        afinamentoInicio = 0.4f, afinamentoFim = 0.5f, pressaoNoTamanho = 0.7f, pressaoNaOpacidade = 0.2f,
        multiplicadorDeLargura = 2.5f, tamanhoMinimo = 0.1f, tamanhoMaximo = 0.8f,
        opacidadeMinima = 0.2f, opacidadeMaxima = 0.9f,
        granulado = FonteDoGranulado.IMAGEM, arquivoDoGranulado = "forma-gran01.png",
        granuladoFixoNoPapel = false, escalaDoGranulado = 1.75f, intensidadeDoGranulado = 0.8f,
        velocidadeNoTamanho = 0.45f, velocidadeNaOpacidade = 0.3f,
    )

    @Test
    fun granuladoDeImagemSemArquivoViraNenhum() {
        val lido = PincelDoMotorEmJson.deJson("""{"id":"g","granulado":"IMAGEM","arquivoDoGranulado":"../x"}""")!!
        assertEquals(FonteDoGranulado.NENHUM, lido.granulado)
        assertEquals(false, lido.temGranulado)
        assertEquals(true, PinceisPredefinidosDoMotor.GIZ.temGranulado)
    }

    @Test
    fun idaEVoltaPreservaTudo() {
        assertEquals(completo, PincelDoMotorEmJson.deJson(PincelDoMotorEmJson.paraJson(completo)))
        PinceisPredefinidosDoMotor.TODOS.forEach { p ->
            assertEquals(p, PincelDoMotorEmJson.deJson(PincelDoMotorEmJson.paraJson(p)))
        }
    }

    @Test
    fun jsonSemQuebraDeLinhaParaCaberNumaLinhaDoRascunho() {
        val comQuebra = completo.copy(nome = "linha 1\nlinha 2")
        val json = PincelDoMotorEmJson.paraJson(comQuebra)
        assertEquals(false, json.contains('\n'))
        assertEquals(comQuebra, PincelDoMotorEmJson.deJson(json))
    }

    @Test
    fun idOuArquivoPerigososSaoRecusados() {
        assertNull(PincelDoMotorEmJson.deJson("""{"id":"../x","nome":"x"}"""))
        val lido = PincelDoMotorEmJson.deJson("""{"id":"ok","forma":"IMAGEM","arquivoDaForma":"../../segredo"}""")
        assertEquals(FormaDoCarimbo.REDONDA, lido?.forma)
        assertEquals("", lido?.arquivoDaForma)
    }

    @Test
    fun camposAusentesUsamOPadrao() {
        val lido = PincelDoMotorEmJson.deJson("""{"id":"x"}""")!!
        assertEquals(PincelDoMotor(id = "x", nome = "x"), lido)
    }

    @Test
    fun chaveMudaQuandoOPincelMuda() {
        val a = PincelDoMotorEmJson.chave(completo)
        assertEquals("meu-a1b2", PincelDoMotorEmJson.idDaChave(a))
        assertEquals(a, PincelDoMotorEmJson.chave(completo.copy()))
        assertNotEquals(a, PincelDoMotorEmJson.chave(completo.copy(dureza = 0.9f)))
    }

    @Test
    fun limitesMapeiamOControle() {
        assertEquals(0.1f, completo.tamanhoNoControle(0f), 0.0001f)
        assertEquals(0.8f, completo.tamanhoNoControle(1f), 0.0001f)
        assertEquals(0.45f, completo.tamanhoNoControle(0.5f), 0.0001f)
        assertEquals(0.9f, completo.opacidadeNoControle(1f), 0.0001f)
        // Limites invertidos não quebram: máximo vira o mínimo.
        assertEquals(0.6f, completo.copy(tamanhoMinimo = 0.6f, tamanhoMaximo = 0.2f).tamanhoNoControle(1f), 0.0001f)
    }

    @Test
    fun rascunhoGuardaADefinicaoDoPincelEOTracoApontaPelaChave() {
        val chave = PincelDoMotorEmJson.chave(completo)
        val doc = DocumentoDoRascunhoDaEdicaoDeImagem(
            largura = 100, altura = 100, proximoId = 2,
            ferramenta = FerramentaGravadaDoRascunhoDaEdicao.VERMELHO,
            imagens = emptyList(), textos = emptyList(),
            tracos = listOf(
                TracoDoRascunhoDaEdicao(
                    cor = -16777216, borracha = false,
                    pontos = listOf(PontoDoTracoDoRascunhoDaEdicao(0.1f, 0.1f, 0.01f)),
                    pincel = chave, semente = 42L,
                ),
            ),
            pinceis = mapOf(chave to PincelDoMotorEmJson.paraJson(completo)),
        )
        val lido = TextoDoRascunhoDaEdicaoDeImagem.ler(TextoDoRascunhoDaEdicaoDeImagem.escrever(doc))!!
        assertEquals(chave, lido.tracos.single().pincel)
        assertEquals(completo, PincelDoMotorEmJson.deJson(lido.pinceis.getValue(chave)))
    }
}

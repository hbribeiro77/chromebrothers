package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaBarraContextualDoEditorEDoToqueNaImagemDaGaveta {
    @Test
    fun cadaFerramentaMostraSoALinhaQueServeParaEla() {
        assertEquals(
            LinhaSecundariaDoEditorDeImagem.SELECAO,
            BarraContextualDoEditorDeImagem.linha(FerramentaDaBarraDoEditorDeImagem.RECORTE),
        )
        assertEquals(
            LinhaSecundariaDoEditorDeImagem.ESPESSURA,
            BarraContextualDoEditorDeImagem.linha(FerramentaDaBarraDoEditorDeImagem.TRACO),
        )
        assertEquals(
            LinhaSecundariaDoEditorDeImagem.COR,
            BarraContextualDoEditorDeImagem.linha(FerramentaDaBarraDoEditorDeImagem.TEXTO),
        )
    }

    @Test
    fun aFileiraContextualDesceJuntoComAFerramenta() {
        assertEquals(0, FaixaLateralDoEditorDeImagem.indice(FerramentaEmpilhadaDoEditorDeImagem.RECORTE))
        assertEquals(1, FaixaLateralDoEditorDeImagem.indice(FerramentaEmpilhadaDoEditorDeImagem.VERMELHO))
        assertEquals(1, FaixaLateralDoEditorDeImagem.indice(FerramentaEmpilhadaDoEditorDeImagem.BRANCO))
        assertEquals(2, FaixaLateralDoEditorDeImagem.indice(FerramentaEmpilhadaDoEditorDeImagem.BORRACHA))
        assertEquals(3, FaixaLateralDoEditorDeImagem.indice(FerramentaEmpilhadaDoEditorDeImagem.TEXTO))
    }

    @Test
    fun oSegundoToqueNoIconeQueAbriuOMenuRecolhe() {
        assertFalse(MenuDaFerramentaDaFaixaLateral.ficaAberto(jaNesseIcone = true, aberto = true))
        assertTrue(MenuDaFerramentaDaFaixaLateral.ficaAberto(jaNesseIcone = true, aberto = false))
        assertTrue(MenuDaFerramentaDaFaixaLateral.ficaAberto(jaNesseIcone = false, aberto = true))
    }

    @Test
    fun aSelecaoSoPermaneceSeACamadaAindaExiste() {
        assertEquals(4, SelecaoObrigatoriaDaCamada.manter(selecionada = 4, idsNaOrdem = listOf(1, 4)))
        assertEquals(-1, SelecaoObrigatoriaDaCamada.manter(selecionada = 9, idsNaOrdem = listOf(1)))
        assertEquals(-1, SelecaoObrigatoriaDaCamada.manter(selecionada = -1, idsNaOrdem = listOf(3, 7)))
        assertEquals(-1, SelecaoObrigatoriaDaCamada.manter(selecionada = 2, idsNaOrdem = emptyList()))
    }

    @Test
    fun toqueNoQuadroSoSelecionaPecaComModoImagemArmado() {
        assertTrue(ToqueParaSelecionarPecaNoEditor.podeSelecionarPeloToque(modoImagemArmado = true))
        assertFalse(ToqueParaSelecionarPecaNoEditor.podeSelecionarPeloToque(modoImagemArmado = false))
        assertTrue(ToqueParaSelecionarPecaNoEditor.podeMoverPecaTocada(pecaTocadaId = 3, pecaSelecionadaId = 3))
        assertFalse(ToqueParaSelecionarPecaNoEditor.podeMoverPecaTocada(pecaTocadaId = 2, pecaSelecionadaId = 3))
        assertFalse(ToqueParaSelecionarPecaNoEditor.podeMoverPecaTocada(pecaTocadaId = 3, pecaSelecionadaId = -1))
    }

    @Test
    fun aoAbrirEdicaoCamadaAtivaExisteMasPecaNaoFicaSelecionadaNoQuadro() {
        assertEquals(7, CamadaAtivaDaEdicaoDeImagem.idAoAbrirComImagem(7))
        assertEquals(-1, PecaSelecionadaDaEdicaoDeImagem.idAoAbrir())
    }

    @Test
    fun novaComposicaoCriaCamadaInicialJaSelecionadaParaPintar() {
        assertTrue(AbrirComposicaoVaziaDaEdicao.criaCamadaInicialSelecionada())
    }

    @Test
    fun toqueNaListaDeCamadasNaoSelecionaPecaNoQuadro() {
        assertFalse(ToqueNaListaDeCamadasDaEdicao.definePecaSelecionadaNoQuadro())
    }

    @Test
    fun dedoSobreLacoFechadoNaoMovePecaNemSelecao() {
        assertFalse(ToqueComDedoSobreLacoFechado.podeMoverPeca(lacoFechado = true))
        assertFalse(ToqueComDedoSobreLacoFechado.podeMoverSelecao(lacoFechado = true))
        assertTrue(ToqueComDedoSobreLacoFechado.podeMoverPeca(lacoFechado = false))
        assertFalse(ToqueComDedoSobreLacoFechado.podeMoverSelecao(lacoFechado = false))
    }

    @Test
    fun imagemAlvoDoRecorteExigeIdExplicito() {
        assertEquals(5, ImagemAlvoDoRecorteDaComposicao.idExplicito(5, listOf(2, 5)))
        assertEquals(null, ImagemAlvoDoRecorteDaComposicao.idExplicito(-1, listOf(2, 5)))
        assertEquals(null, ImagemAlvoDoRecorteDaComposicao.idExplicito(9, listOf(2, 5)))
    }

    @Test
    fun destacarAceitaCamadaAtivaDeImagemOuFolhaComTinta() {
        assertTrue(FonteDoDestacarDaCamadaAtiva.aceita(ehImagem = true, ehFolha = false))
        assertTrue(FonteDoDestacarDaCamadaAtiva.aceita(ehImagem = false, ehFolha = true))
        assertFalse(FonteDoDestacarDaCamadaAtiva.aceita(ehImagem = false, ehFolha = false))
        assertTrue(FonteDoDestacarDaCamadaAtiva.precisaPromoverFolha(ehFolha = true))
        assertFalse(FonteDoDestacarDaCamadaAtiva.precisaPromoverFolha(ehFolha = false))
    }

    @Test
    fun oSegundoToqueNoPontoDePinturaRecolheOMenu() {
        assertFalse(MenuDoPontoDePintura.ficaAberto(jaNaPintura = true, aberto = true))
        assertTrue(MenuDoPontoDePintura.ficaAberto(jaNaPintura = true, aberto = false))
        assertTrue(MenuDoPontoDePintura.ficaAberto(jaNaPintura = false, aberto = true))
    }

    @Test
    fun aBorrachaNaoMostraOMenuDeCores() {
        assertFalse(CoresNoMenuDaFerramenta.aparecem(ehBorracha = true))
        assertTrue(CoresNoMenuDaFerramenta.aparecem(ehBorracha = false))
    }

    @Test
    fun aReguaRecebeOToqueMesmoForaDaPintura() {
        assertTrue(ToqueNaReguaDoEditor.temPrioridade(comecouNaRegua = true, jaSegurandoARegua = false))
        assertTrue(ToqueNaReguaDoEditor.temPrioridade(comecouNaRegua = false, jaSegurandoARegua = true))
        assertFalse(ToqueNaReguaDoEditor.temPrioridade(comecouNaRegua = false, jaSegurandoARegua = false))
    }

    @Test
    fun toqueNaImagemTrocaOVisorIncluiNaEdicaoOuAbreNormal() {
        assertEquals(
            ToqueNaImagemDaGaveta.ABRIR_NO_VISOR,
            DestinoDoToqueNaImagemDaGaveta.decidir(editando = false, gavetaSobreAImagem = false),
        )
        assertEquals(
            ToqueNaImagemDaGaveta.TROCAR_A_IMAGEM_DO_VISOR,
            DestinoDoToqueNaImagemDaGaveta.decidir(editando = false, gavetaSobreAImagem = true),
        )
        assertEquals(
            ToqueNaImagemDaGaveta.INCLUIR_NA_COMPOSICAO,
            DestinoDoToqueNaImagemDaGaveta.decidir(editando = true, gavetaSobreAImagem = true),
        )
        assertEquals(
            ToqueNaImagemDaGaveta.ABRIR_NO_VISOR,
            DestinoDoToqueNaImagemDaGaveta.decidir(editando = true, gavetaSobreAImagem = false),
        )
    }
}

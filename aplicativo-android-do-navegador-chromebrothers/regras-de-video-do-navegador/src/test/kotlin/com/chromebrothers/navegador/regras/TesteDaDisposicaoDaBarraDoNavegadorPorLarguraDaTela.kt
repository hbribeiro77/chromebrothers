package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaDisposicaoDaBarraDoNavegadorPorLarguraDaTela {
    @Test
    fun telefoneFicaCompactoETabletAbertoFicaAmplo() {
        assertEquals(
            ModoDaBarraDoNavegadorPorLargura.COMPACTA,
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.modo(360),
        )
        assertEquals(
            ModoDaBarraDoNavegadorPorLargura.COMPACTA,
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.modo(699),
        )
        assertEquals(
            ModoDaBarraDoNavegadorPorLargura.AMPLA,
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.modo(700),
        )
    }

    @Test
    fun naBarraCompactaSoVoltarEnderecoEMenu() {
        val modo = ModoDaBarraDoNavegadorPorLargura.COMPACTA
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.VOLTAR,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.ENDERECO,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.MENU_OVERFLOW,
                modo,
            ),
        )
        assertFalse(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.CHAT,
                modo,
            ),
        )
        assertFalse(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.FAIXA_DE_ABAS,
                modo,
            ),
        )
    }

    @Test
    fun chatECapturaVaoParaOOverflowNoModoCompacto() {
        val modo = ModoDaBarraDoNavegadorPorLargura.COMPACTA
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                ControleDaBarraDoNavegador.CHAT,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                ControleDaBarraDoNavegador.CAPTURA_TELA,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                ControleDaBarraDoNavegador.FIXAR_ATALHO,
                modo,
            ),
        )
        assertFalse(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                ControleDaBarraDoNavegador.VOLTAR,
                modo,
            ),
        )
    }

    @Test
    fun noModoAmploOOverflowFicaComAcoesOcasionaisEFerramentasFicamNaBarra() {
        val modo = ModoDaBarraDoNavegadorPorLargura.AMPLA
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.MENU_OVERFLOW,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.FAIXA_DE_ABAS,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.CHAT,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.CAPTURA_TELA,
                modo,
            ),
        )
        assertFalse(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.FIXAR_ATALHO,
                modo,
            ),
        )
        assertFalse(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.VERSAO_COMPUTADOR,
                modo,
            ),
        )
        assertFalse(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.EXTENSOES,
                modo,
            ),
        )
        assertFalse(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.BLOQUEIO_ANUNCIO,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                ControleDaBarraDoNavegador.FIXAR_ATALHO,
                modo,
            ),
        )
        assertFalse(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                ControleDaBarraDoNavegador.VERSAO_COMPUTADOR,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                ControleDaBarraDoNavegador.EXTENSOES,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                ControleDaBarraDoNavegador.BLOQUEIO_ANUNCIO,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                ControleDaBarraDoNavegador.CAPTURA_MIDIA,
                modo,
            ),
        )
        assertFalse(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.CAPTURA_MIDIA,
                modo,
            ),
        )
        assertFalse(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                ControleDaBarraDoNavegador.CHAT,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.BUSCAR_NA_PAGINA,
                modo,
            ),
        )
        assertFalse(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                ControleDaBarraDoNavegador.BUSCAR_NA_PAGINA,
                modo,
            ),
        )
    }

    @Test
    fun buscarNaPaginaNoCompactoVaiAoOverflow() {
        val modo = ModoDaBarraDoNavegadorPorLargura.COMPACTA
        assertFalse(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.BUSCAR_NA_PAGINA,
                modo,
            ),
        )
        assertTrue(
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                ControleDaBarraDoNavegador.BUSCAR_NA_PAGINA,
                modo,
            ),
        )
    }
}

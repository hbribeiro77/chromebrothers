package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoEspacoDoTecladoSobreAPaginaDoNavegador {
    @Test
    fun paddingInferiorUsaOMaiorEntreBarraDoSistemaETeclado() {
        assertEquals(0, EspacoDoTecladoSobreAPaginaDoNavegador.paddingInferior(barraDoSistema = 0, teclado = 0))
        assertEquals(48, EspacoDoTecladoSobreAPaginaDoNavegador.paddingInferior(barraDoSistema = 48, teclado = 0))
        assertEquals(720, EspacoDoTecladoSobreAPaginaDoNavegador.paddingInferior(barraDoSistema = 48, teclado = 720))
        assertEquals(48, EspacoDoTecladoSobreAPaginaDoNavegador.paddingInferior(barraDoSistema = 48, teclado = 20))
    }

    @Test
    fun folgaExtraDoTecladoParaPaineisFlutuantes() {
        assertEquals(0, EspacoDoTecladoSobreAPaginaDoNavegador.folgaExtra(barraDoSistema = 48, teclado = 0))
        assertEquals(672, EspacoDoTecladoSobreAPaginaDoNavegador.folgaExtra(barraDoSistema = 48, teclado = 720))
        assertEquals(0, EspacoDoTecladoSobreAPaginaDoNavegador.folgaExtra(barraDoSistema = 48, teclado = 20))
    }

    @Test
    fun paddingInferiorDaRaizIgnoraOTecladoParaNaoRedimensionarOGecko() {
        assertEquals(0, EspacoDoTecladoSobreAPaginaDoNavegador.paddingInferiorDaRaiz(barraDoSistema = 0))
        assertEquals(48, EspacoDoTecladoSobreAPaginaDoNavegador.paddingInferiorDaRaiz(barraDoSistema = 48))
    }

    @Test
    fun deslocamentoVerticalSobeAPaginaPelaFolgaDoTeclado() {
        assertEquals(0f, EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoVerticalDaPagina(barraDoSistema = 48, teclado = 0))
        assertEquals(-672f, EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoVerticalDaPagina(barraDoSistema = 48, teclado = 720))
        assertEquals(0f, EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoVerticalDaPagina(barraDoSistema = 48, teclado = 20))
    }

    @Test
    fun interpolaPaddingDuranteAAnimacaoDoTeclado() {
        assertEquals(48, EspacoDoTecladoSobreAPaginaDoNavegador.interpolar(inicio = 48, fim = 720, fracao = 0f))
        assertEquals(720, EspacoDoTecladoSobreAPaginaDoNavegador.interpolar(inicio = 48, fim = 720, fracao = 1f))
        assertEquals(384, EspacoDoTecladoSobreAPaginaDoNavegador.interpolar(inicio = 48, fim = 720, fracao = 0.5f))
    }
}

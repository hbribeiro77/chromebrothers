package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoPacoteDoDesfazerDoMoverDaSelecaoAtiva {
    private val destaque = PacoteDoDesfazerDoMoverDaSelecaoAtiva.TipoDePassoDoMover.DESTAQUE_NOVA
    private val transformacao = PacoteDoDesfazerDoMoverDaSelecaoAtiva.TipoDePassoDoMover.TRANSFORMACAO
    private val modo = PacoteDoDesfazerDoMoverDaSelecaoAtiva.TipoDePassoDoMover.MODO_TRANSFORMAR
    private val fundir = PacoteDoDesfazerDoMoverDaSelecaoAtiva.TipoDePassoDoMover.FUNDIR
    private val outro = PacoteDoDesfazerDoMoverDaSelecaoAtiva.TipoDePassoDoMover.OUTRO

    private fun e(
        tipo: PacoteDoDesfazerDoMoverDaSelecaoAtiva.TipoDePassoDoMover,
        id: Int,
    ) = PacoteDoDesfazerDoMoverDaSelecaoAtiva.Entrada(tipo, id)

    @Test
    fun comMolduraAtivaDesfazPacoteAteODestaque() {
        val passos = listOf(e(destaque, 7), e(transformacao, 7), e(transformacao, 7))
        assertEquals(
            0,
            PacoteDoDesfazerDoMoverDaSelecaoAtiva.indiceInicioDoPacoteNoTopo(
                passos = passos,
                idPecaEmTransformar = 7,
            ),
        )
    }

    @Test
    fun aposEncerrarMolduraDesfazModoTransformarComOPacote() {
        val passos = listOf(e(destaque, 3), e(transformacao, 3), e(modo, 3))
        assertEquals(
            0,
            PacoteDoDesfazerDoMoverDaSelecaoAtiva.indiceInicioDoPacoteNoTopo(
                passos = passos,
                idPecaEmTransformar = -1,
            ),
        )
    }

    @Test
    fun aposFundirDesfazPacoteInteiro() {
        val passos = listOf(e(destaque, 5), e(fundir, 5))
        assertEquals(
            0,
            PacoteDoDesfazerDoMoverDaSelecaoAtiva.indiceInicioDoPacoteNoTopo(
                passos = passos,
                idPecaEmTransformar = -1,
            ),
        )
    }

    @Test
    fun transformacaoDePecaComumNaoViraPacoteSemDestaque() {
        val passos = listOf(e(outro, 1), e(transformacao, 9))
        assertEquals(
            -1,
            PacoteDoDesfazerDoMoverDaSelecaoAtiva.indiceInicioDoPacoteNoTopo(
                passos = passos,
                idPecaEmTransformar = -1,
            ),
        )
    }

    @Test
    fun passoEstranhoNoMeioQuebraOPacote() {
        val passos = listOf(e(destaque, 2), e(outro, 0), e(modo, 2))
        assertEquals(
            -1,
            PacoteDoDesfazerDoMoverDaSelecaoAtiva.indiceInicioDoPacoteNoTopo(
                passos = passos,
                idPecaEmTransformar = -1,
            ),
        )
    }
}

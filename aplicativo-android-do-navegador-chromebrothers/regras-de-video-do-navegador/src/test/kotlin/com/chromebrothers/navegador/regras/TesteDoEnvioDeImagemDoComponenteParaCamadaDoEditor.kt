package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
    @Test
    fun imagemAtivaAceitaFundir() {
        assertEquals(
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
                editorAtivo = true,
                idCamadaAtiva = 3,
                destinoEhImagem = true,
                destinoEhFolha = false,
            ),
        )
    }

    @Test
    fun folhaVaziaPromoveEFunde() {
        assertEquals(
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.PROMOVER_FOLHA_E_FUNDIR,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
                editorAtivo = true,
                idCamadaAtiva = 1,
                destinoEhImagem = false,
                destinoEhFolha = true,
            ),
        )
    }

    @Test
    fun semEditorOuSemCamadaRecusa() {
        assertEquals(
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
                editorAtivo = false,
                idCamadaAtiva = 1,
                destinoEhImagem = true,
                destinoEhFolha = false,
            ),
        )
        assertEquals(
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
                editorAtivo = true,
                idCamadaAtiva = -1,
                destinoEhImagem = false,
                destinoEhFolha = false,
            ),
        )
    }

    @Test
    fun soltarNaMolduraOuNaCamadaFundeEForaCancela() {
        assertEquals(
            ResultadoDoGestoDeArrasteDaImagemDoComponente.FUNDIR,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.resultadoDoGestoDeSoltar(
                soltouSobreJanelaDoComponente = false,
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = true,
            ),
        )
        assertEquals(
            ResultadoDoGestoDeArrasteDaImagemDoComponente.FUNDIR,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.resultadoDoGestoDeSoltar(
                soltouSobreJanelaDoComponente = false,
                soltouSobreLinhaDeCamada = true,
                soltouSobreMoldura = false,
            ),
        )
        assertEquals(
            ResultadoDoGestoDeArrasteDaImagemDoComponente.CANCELAR,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.resultadoDoGestoDeSoltar(
                soltouSobreJanelaDoComponente = false,
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = false,
            ),
        )
    }

    @Test
    fun soltarAindaNoComponenteCancelaMesmoComMolduraAtras() {
        assertEquals(
            ResultadoDoGestoDeArrasteDaImagemDoComponente.CANCELAR,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.resultadoDoGestoDeSoltar(
                soltouSobreJanelaDoComponente = true,
                soltouSobreLinhaDeCamada = false,
                soltouSobreMoldura = true,
            ),
        )
    }
}

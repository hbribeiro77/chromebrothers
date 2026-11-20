package com.chromebrothers.navegador.regras

enum class ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
    FUNDIR_NA_IMAGEM,
    PROMOVER_FOLHA_E_FUNDIR,
    RECUSAR_SEM_DESTINO,
}

enum class ResultadoDoGestoDeArrasteDaImagemDoComponente {
    FUNDIR,
    CANCELAR,
}

object EnvioDeImagemDoComponenteParaCamadaDoEditor {
    fun decidirDestino(
        editorAtivo: Boolean,
        idCamadaAtiva: Int,
        destinoEhImagem: Boolean,
        destinoEhFolha: Boolean,
    ): ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
        if (!editorAtivo || idCamadaAtiva == -1) {
            return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
        }
        if (destinoEhImagem) {
            return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM
        }
        if (destinoEhFolha) {
            return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.PROMOVER_FOLHA_E_FUNDIR
        }
        return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
    }

    /**
     * Soltar ainda sobre a janela do componente cancela (mesmo que a moldura esteja atrás).
     * Senão: lista de camadas ou moldura funde; fora cancela.
     */
    fun resultadoDoGestoDeSoltar(
        soltouSobreJanelaDoComponente: Boolean,
        soltouSobreLinhaDeCamada: Boolean = false,
        soltouSobreMoldura: Boolean = false,
    ): ResultadoDoGestoDeArrasteDaImagemDoComponente {
        if (soltouSobreJanelaDoComponente) {
            return ResultadoDoGestoDeArrasteDaImagemDoComponente.CANCELAR
        }
        return if (soltouSobreLinhaDeCamada || soltouSobreMoldura) {
            ResultadoDoGestoDeArrasteDaImagemDoComponente.FUNDIR
        } else {
            ResultadoDoGestoDeArrasteDaImagemDoComponente.CANCELAR
        }
    }
}

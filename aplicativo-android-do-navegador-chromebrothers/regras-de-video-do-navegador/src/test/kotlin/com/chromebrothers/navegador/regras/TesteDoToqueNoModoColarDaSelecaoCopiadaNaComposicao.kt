package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoToqueNoModoColarDaSelecaoCopiadaNaComposicao {
    @Test
    fun chipConfirmaColar() {
        assertEquals(
            DestinoDoToqueNoModoColarDaSelecaoCopiada.CONFIRMAR_COLAR,
            ToqueNoModoColarDaSelecaoCopiadaNaComposicao.destino(
                modoColarAtivo = true,
                toqueNoChipColar = true,
                toqueNaPreviaOuChrome = false,
            ),
        )
    }

    @Test
    fun toqueNaPreviaSegueGesto() {
        assertEquals(
            DestinoDoToqueNoModoColarDaSelecaoCopiada.GESTO_DA_PREVIA,
            ToqueNoModoColarDaSelecaoCopiadaNaComposicao.destino(
                modoColarAtivo = true,
                toqueNoChipColar = false,
                toqueNaPreviaOuChrome = true,
            ),
        )
    }

    @Test
    fun toqueForaCancelaMesmoComCamadaEmbaixo() {
        assertEquals(
            DestinoDoToqueNoModoColarDaSelecaoCopiada.CANCELAR,
            ToqueNoModoColarDaSelecaoCopiadaNaComposicao.destino(
                modoColarAtivo = true,
                toqueNoChipColar = false,
                toqueNaPreviaOuChrome = false,
            ),
        )
    }

    @Test
    fun toqueNoRetanguloDaPreviaNaoCancelaMesmoSeOutraCamadaEstiverNoTopo() {
        // toqueNaPreviaOuChrome já vem do retângulo da selecionada (não do pecaEm no topo).
        assertEquals(
            DestinoDoToqueNoModoColarDaSelecaoCopiada.GESTO_DA_PREVIA,
            ToqueNoModoColarDaSelecaoCopiadaNaComposicao.destino(
                modoColarAtivo = true,
                toqueNoChipColar = false,
                toqueNaPreviaOuChrome = true,
            ),
        )
    }

    @Test
    fun caboDeGiroTemPrioridadeSobreChipColar() {
        assertEquals(
            DestinoDoToqueNoModoColarDaSelecaoCopiada.GESTO_DA_PREVIA,
            ToqueNoModoColarDaSelecaoCopiadaNaComposicao.destino(
                modoColarAtivo = true,
                toqueNoChipColar = true,
                toqueNaPreviaOuChrome = false,
                toqueNoCaboDeGiro = true,
            ),
        )
    }
}

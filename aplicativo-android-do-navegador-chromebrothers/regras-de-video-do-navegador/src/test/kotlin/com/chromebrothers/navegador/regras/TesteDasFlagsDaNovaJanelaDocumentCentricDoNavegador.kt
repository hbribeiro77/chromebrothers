package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDasFlagsDaNovaJanelaDocumentCentricDoNavegador {
    @Test
    fun foraDoSplitUsaNovaTaskEMultiplasTasks() {
        val flags = FlagsDaNovaJanelaDocumentCentricDoNavegador.montar(emMultiJanela = false)
        assertTrue(flags and FlagsDaNovaJanelaDocumentCentricDoNavegador.NEW_TASK != 0)
        assertTrue(flags and FlagsDaNovaJanelaDocumentCentricDoNavegador.MULTIPLE_TASK != 0)
        assertTrue(flags and FlagsDaNovaJanelaDocumentCentricDoNavegador.RETAIN_IN_RECENTS != 0)
        assertEquals(0, flags and FlagsDaNovaJanelaDocumentCentricDoNavegador.LAUNCH_ADJACENT)
    }

    @Test
    fun noSplitPedeJanelaAdjacente() {
        val flags = FlagsDaNovaJanelaDocumentCentricDoNavegador.montar(emMultiJanela = true)
        assertTrue(flags and FlagsDaNovaJanelaDocumentCentricDoNavegador.NEW_TASK != 0)
        assertTrue(flags and FlagsDaNovaJanelaDocumentCentricDoNavegador.MULTIPLE_TASK != 0)
        assertTrue(flags and FlagsDaNovaJanelaDocumentCentricDoNavegador.LAUNCH_ADJACENT != 0)
    }

    @Test
    fun trampolimSamsungNaoCriaOutraTaskParaNaoDeixarJanelaPretaNosRecents() {
        val flags = FlagsDaNovaJanelaDocumentCentricDoNavegador
            .montarParaTrampolimQueCedeIdentidadeDaTarefaDoSplit()
        assertEquals(0, flags and FlagsDaNovaJanelaDocumentCentricDoNavegador.NEW_TASK)
        assertEquals(0, flags and FlagsDaNovaJanelaDocumentCentricDoNavegador.MULTIPLE_TASK)
        assertEquals(0, flags and FlagsDaNovaJanelaDocumentCentricDoNavegador.LAUNCH_ADJACENT)
        assertEquals(0, flags)
    }
}

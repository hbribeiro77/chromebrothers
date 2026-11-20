package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoEnvioDoVideoDoPlayerParaAGaleria {
    private val regra = EnvioDoVideoDoPlayerParaAGaleria

    @Test
    fun videoDeSiteBaixa() {
        assertEquals(SituacaoDoVideoParaAGaleria.BAIXAR_DO_SITE, regra.situacao("https://cdn.x/v.mp4", null))
    }

    @Test
    fun videoGeradoDentroDoAppVaiParaAGaleria() {
        assertEquals(
            SituacaoDoVideoParaAGaleria.MOVER_DA_PASTA_DO_APP,
            regra.situacao("file:///data/user/0/x/files/biblioteca-de-videos-higgsfield/vivix-1.mp4", null),
        )
    }

    @Test
    fun videoDoAparelhoNaoEhBaixadoDeNovo() {
        assertEquals(
            SituacaoDoVideoParaAGaleria.JA_NA_GALERIA,
            regra.situacao("content://media/external/video/media/9", "Movies/ChromeBrothers/"),
        )
        assertEquals(
            SituacaoDoVideoParaAGaleria.JA_NO_APARELHO,
            regra.situacao("content://media/external/downloads/5", "Download/ChromeBrothers/"),
        )
        assertEquals(SituacaoDoVideoParaAGaleria.NAO_SUPORTADO, regra.situacao("blob:https://x/1", null))
    }

    @Test
    fun avisosDizemOndeEsta() {
        assertTrue(regra.aviso(SituacaoDoVideoParaAGaleria.JA_NO_APARELHO, "Download/ChromeBrothers/")!!.contains("Download/ChromeBrothers"))
        assertTrue(regra.aviso(SituacaoDoVideoParaAGaleria.JA_NA_GALERIA, "Movies/ChromeBrothers/")!!.contains("já está na Galeria"))
        assertNull(regra.aviso(SituacaoDoVideoParaAGaleria.BAIXAR_DO_SITE, null))
    }
}

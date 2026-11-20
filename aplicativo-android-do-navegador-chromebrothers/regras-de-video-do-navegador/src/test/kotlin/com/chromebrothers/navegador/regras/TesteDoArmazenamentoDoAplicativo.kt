package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.ArmazenamentoDoAplicativo as A
import com.chromebrothers.navegador.regras.CategoriaDoArmazenamento as C
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoArmazenamentoDoAplicativo {

    @Test
    fun categoriaPeloCaminho() {
        assertEquals(C.VIDEOS_GERADOS, A.categoria("files/biblioteca-de-videos-higgsfield/higgsfield-1.mp4"))
        assertEquals(C.REFERENCIAS, A.categoria("files/refs-de-geracao-da-biblioteca/x-ref-0.jpg"))
        assertEquals(C.REFERENCIAS, A.categoria("files/refs-das-receitas/ab.mp4"))
        assertEquals(C.NAVEGADOR, A.categoria("files/mozilla/perfil/x"))
        assertEquals(C.CACHE, A.categoria("cache/navrtxxz.default/cache2/x"))
        assertEquals(C.OUTROS, A.categoria("files/abas-abertas/slot-1.json"))
        assertEquals(C.OUTROS, A.categoria("shared_prefs/x.xml"))
        assertEquals(C.CACHE, A.categoria("\\cache\\x"))
    }

    @Test
    fun agrupaNaOrdemDaListaESoComOQueExiste() {
        val totais = A.agrupar(
            listOf(
                ArquivoMedido("cache/a", 10),
                ArquivoMedido("files/biblioteca-de-videos-higgsfield/v.mp4", 1000),
                ArquivoMedido("files/biblioteca-de-videos-higgsfield/w.mp4", 500),
                ArquivoMedido("files/x.json", 3),
            ),
        )
        assertEquals(listOf(C.VIDEOS_GERADOS, C.CACHE, C.OUTROS), totais.keys.toList())
        assertEquals(TotalDoArmazenamento(1500, 2), totais[C.VIDEOS_GERADOS])
    }

    @Test
    fun formataEmPortugues() {
        assertEquals("0 B", A.formatarBytes(0))
        assertEquals("1,0 KB", A.formatarBytes(1024))
        assertEquals("418 MB", A.formatarBytes(418L * 1024 * 1024))
        assertEquals("3,4 GB", A.formatarBytes((3.4 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun refsSemUsoContraAsReceitasVivas() {
        val h1 = "a".repeat(64)
        val h2 = "b".repeat(64)
        val viva = ReceitaDeGeracaoMcp("r1", "imagem", 0, "{}", listOf(RefDaReceitaDeGeracaoMcp("imagem", "x", h1, "jpg")))
        val livres = A.refsDeReceitaSemUso(setOf("$h1.jpg", "$h2.mp4", "lixo.tmp"), listOf(viva))
        assertEquals(setOf("$h2.mp4", "lixo.tmp"), livres)
        assertEquals(setOf("r2"), A.receitasOrfas(setOf("r1", "r2"), setOf("r1", "r9")))
    }

    @Test
    fun refsAntigasSemUsoAceitamCaminhoCompletoNaLista() {
        val livres = A.refsAntigasSemUso(
            setOf("v1-ref-0.jpg", "v2-ref-0.jpg"),
            setOf("/data/user/0/app/files/refs-de-geracao-da-biblioteca/v1-ref-0.jpg"),
        )
        assertEquals(setOf("v2-ref-0.jpg"), livres)
    }

    @Test
    fun limpezaAutomaticaSoComOpcaoLigadaEUmaVezPorDia() {
        val dia = 24L * 60 * 60 * 1000
        assertFalse(A.deveRodarLimpezaAutomatica(0, 0, 10 * dia))
        assertTrue(A.deveRodarLimpezaAutomatica(30, 0, 10 * dia))
        assertFalse(A.deveRodarLimpezaAutomatica(30, 10 * dia - 1000, 10 * dia))
        assertTrue(A.antigo(0 + 1, 31 * dia + 2, 30))
        assertFalse(A.antigo(29 * dia, 31 * dia, 30))
        assertFalse(A.antigo(1, 100 * dia, 0))
    }
}

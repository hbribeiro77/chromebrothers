package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaTransicaoEstiloTelaDivididaDaGaveta {
    private val t = TransicaoEstiloTelaDivididaDaGaveta

    @Test
    fun noInicioOCartaoDaPaginaEhATelaInteiraENitida() {
        val q = t.quadro(largura = 2000, larguraDoChat = 600, divisao = 0f, progresso = 0f, densidade = 2f)
        assertEquals(2000f, q.paginaDireita)
        assertEquals(2000f, q.chatEsquerda)
        assertEquals(0f, q.raioDosCantos)
        assertEquals(0f, q.desfoque)
        assertEquals(0f, q.barrinha)
    }

    @Test
    fun abertoTemVaoCentradoNaBarrinhaECantosCheios() {
        val q = t.quadro(largura = 2000, larguraDoChat = 600, divisao = 1f, progresso = 1f, densidade = 2f)
        assertEquals(1400f, q.divisorX)
        assertEquals(1390f, q.paginaDireita)
        assertEquals(1410f, q.chatEsquerda)
        assertEquals(44f, q.raioDosCantos)
        assertEquals(1f, q.desfoque)
        assertEquals(t.ESCURECIMENTO_MAXIMO, q.escurecimento)
    }

    @Test
    fun desfoqueSegueOProgressoNaoADivisao() {
        // Fechando: divisão ainda alta, mas o desfoque começa do zero (sai nítido da tela atual).
        val q = t.quadro(2000, 600, divisao = 0.95f, progresso = 0.05f, densidade = 1f)
        assertTrue(q.desfoque < 0.5f)
        assertEquals(1f, q.raioDosCantos / t.RAIO_DOS_CANTOS_DP)
    }

    @Test
    fun borrarMantemCorUniformeEAmaciaBorda() {
        val w = 10
        val h = 4
        val uniforme = IntArray(w * h) { 0xFF336699.toInt() }
        assertTrue(t.borrar(uniforme, w, h, 2).all { it == 0xFF336699.toInt() })
        val degrau = IntArray(w * h) { i -> if (i % w < 5) 0xFF000000.toInt() else 0xFFFFFFFF.toInt() }
        val borrado = t.borrar(degrau, w, h, 2)
        val meio = borrado[1 * w + 4] and 0xFF
        assertTrue(meio in 1..254, "a borda deveria ficar cinza, ficou $meio")
    }

    @Test
    fun escalaHorizontalEncaixaAFonte() {
        assertEquals(0.5f, t.escalaHorizontal(500f, 1000f))
        assertEquals(1f, t.escalaHorizontal(500f, 0f))
    }

    @Test
    fun paginaParadaSoQuandoAsAmostrasBatem() {
        val a = IntArray(100) { 0xFF808080.toInt() }
        assertTrue(!t.paginaParada(null, a))
        assertTrue(t.paginaParada(a, a.copyOf()))
        val quase = a.copyOf().also { it[0] = 0xFF818181.toInt() }
        assertTrue(t.paginaParada(a, quase))
        val mudou = IntArray(100) { i -> if (i < 40) 0xFFFFFFFF.toInt() else 0xFF808080.toInt() }
        assertTrue(!t.paginaParada(a, mudou))
    }

    @Test
    fun niveisDeDesfoqueSobemEmSequencia() {
        assertTrue(t.opacidadesDosNiveis(0f).all { it == 0f })
        assertTrue(t.opacidadesDosNiveis(1f).all { it == 1f })
        val meio = t.opacidadesDosNiveis(0.5f)
        assertEquals(1f, meio[0])
        assertEquals(0.5f, meio[1], 0.0001f)
        assertEquals(0f, meio[2])
    }

    @Test
    fun focoEntraDoMaisForteParaOMaisLeve() {
        assertTrue(t.opacidadesDoFoco(0f).all { it == 0f })
        val terco = t.opacidadesDoFoco(1f / 3f)
        assertEquals(1f, terco[2], 0.0001f)
        assertEquals(0f, terco[0], 0.0001f)
        assertTrue(t.opacidadesDoFoco(1f).all { it == 1f })
    }

    @Test
    fun revelaSoDepoisDoPisoComPaginaQuietaOuNoTeto() {
        assertTrue(!t.podeRevelar(desdeAMudancaMs = 200, quietaHaMs = 200))
        assertTrue(!t.podeRevelar(desdeAMudancaMs = 400, quietaHaMs = 100))
        assertTrue(t.podeRevelar(desdeAMudancaMs = 400, quietaHaMs = 150))
        assertTrue(t.podeRevelar(desdeAMudancaMs = t.ESPERA_MAXIMA_DA_PAGINA_MS, quietaHaMs = 0))
    }
}

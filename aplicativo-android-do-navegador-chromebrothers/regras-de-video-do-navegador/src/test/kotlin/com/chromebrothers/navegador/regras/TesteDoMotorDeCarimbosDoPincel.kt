package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.MotorDeCarimbosDoPincel as Motor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TesteDoMotorDeCarimbosDoPincel {

    private val reta = (0..10).map { PontoParaCarimbar(it * 10f, 0f, 10f, 1f) } // 100 px, largura 10
    private val simples = PincelDoMotor(id = "t", nome = "t", espacamento = 0.5f) // passo = 5 px

    @Test
    fun espacamentoDefineQuantosCarimbos() {
        val c = Motor.gerar(reta, simples, 1L, comAfinamentoDoFim = true)
        assertEquals(21, c.size) // 0, 5, ..., 100
        assertEquals(0f, c.first().x, 0.001f)
        assertEquals(5f, c[1].x, 0.001f)
        assertTrue(c.all { it.largura == 10f && it.alpha == 1f })
    }

    @Test
    fun mesmaSementeMesmoResultadoEOutraSementeMuda() {
        val p = simples.copy(tremorDeTamanho = 0.5f, dispersao = 0.3f, tremorDeRotacao = 1f)
        assertEquals(Motor.gerar(reta, p, 7L, true), Motor.gerar(reta, p, 7L, true))
        assertNotEquals(Motor.gerar(reta, p, 7L, true), Motor.gerar(reta, p, 8L, true))
    }

    @Test
    fun acrescentarPontosNaoMudaCarimbosJaGerados() {
        val p = simples.copy(tremorDeTamanho = 0.5f, dispersao = 0.3f, afinamentoInicio = 0.5f)
        val antes = Motor.gerar(reta.take(6), p, 3L, comAfinamentoDoFim = false)
        val depois = Motor.gerar(reta, p, 3L, comAfinamentoDoFim = false)
        assertEquals(antes, depois.take(antes.size))
    }

    @Test
    fun afinamentoDoInicioComecaFinoEOFimSoComTracoConcluido() {
        val p = simples.copy(espacamento = 0.1f, afinamentoInicio = 0.25f, afinamentoFim = 0.25f) // 20 px cada
        val emAndamento = Motor.gerar(reta, p, 1L, comAfinamentoDoFim = false)
        assertTrue(emAndamento.first().largura < 1f)
        assertEquals(10f, emAndamento.last().largura, 0.001f)
        val concluido = Motor.gerar(reta, p, 1L, comAfinamentoDoFim = true)
        assertTrue(concluido.last().largura < 1f)
    }

    @Test
    fun tremoresFicamNosLimites() {
        val p = simples.copy(tremorDeTamanho = 0.4f, tremorDeOpacidade = 0.5f, dispersao = 0.5f)
        Motor.gerar(reta, p, 11L, true).forEachIndexed { i, c ->
            assertTrue(c.largura in 6f..10f, "largura $i = ${c.largura}")
            assertTrue(c.alpha in 0.5f..1f)
            assertTrue(kotlin.math.abs(c.y) <= 5f + 0.001f)
        }
    }

    @Test
    fun seguirOTracoGiraOCarimboEAProporcaoAchata() {
        val vertical = (0..4).map { PontoParaCarimbar(0f, it * 10f, 10f, 1f) }
        val c = Motor.gerar(vertical, simples.copy(seguirTraco = true, proporcao = 0.25f), 1L, true)
        assertEquals(90f, c[1].rotacaoGraus, 0.001f)
        assertEquals(2.5f, c[1].altura, 0.001f)
    }

    @Test
    fun pontoUnicoEIgualAoPrimeiroCarimboDoTracoQueCresce() {
        // Antes o ponto único saía em tamanho cheio e ficava uma "bolota" até soltar.
        val p = simples.copy(afinamentoInicio = 0.5f, tremorDeTamanho = 0.3f)
        val sozinho = Motor.gerar(reta.take(1), p, 5L, comAfinamentoDoFim = false).single()
        val doTraco = Motor.gerar(reta, p, 5L, comAfinamentoDoFim = false).first()
        assertEquals(doTraco, sozinho)
        assertTrue(sozinho.largura < 1f)
    }

    @Test
    fun carimbosAntesDaCaudaNaoMudamComOAfinamentoDoFimAoVivo() {
        val p = simples.copy(espacamento = 0.2f, afinamentoFim = 0.5f, tremorDeTamanho = 0.2f) // cauda de 15 px (3 diâmetros × 50%)
        val curto = reta.take(7) // 60 px
        val cauda = Motor.inicioDaCauda(curto, p)
        assertEquals(45f, cauda, 0.001f)
        val antes = Motor.gerar(curto, p, 9L, comAfinamentoDoFim = true).filter { it.distancia < cauda }
        val depois = Motor.gerar(reta, p, 9L, comAfinamentoDoFim = true)
        assertEquals(antes, depois.take(antes.size))
        assertEquals(Float.MAX_VALUE, Motor.inicioDaCauda(reta, simples))
    }

    @Test
    fun umPontoSoViraUmCarimbo() {
        assertEquals(1, Motor.gerar(listOf(PontoParaCarimbar(3f, 4f, 8f, 0.5f)), simples, 1L, true).size)
        assertTrue(Motor.gerar(emptyList(), simples, 1L, true).isEmpty())
    }

    @Test
    fun pressaoNormalNaoMudaOTamanhoSoApertarOuAliviar() {
        val normal = Motor.PRESSAO_NORMAL
        assertEquals(1f, Motor.fatorDaPressaoNoTamanho(null, 1f))
        // Na pressão normal, qualquer sensibilidade deixa o tamanho igual (o traço não muda todo).
        listOf(0f, 0.5f, 1f).forEach { s -> assertEquals(1f, Motor.fatorDaPressaoNoTamanho(normal, s), 0.0001f) }
        assertEquals(2f, Motor.fatorDaPressaoNoTamanho(1f, 1f), 0.0001f)
        assertEquals(1.5f, Motor.fatorDaPressaoNoTamanho(1f, 0.5f), 0.0001f)
        assertEquals(0.5f, Motor.fatorDaPressaoNoTamanho(normal / 2f, 1f), 0.0001f)
        assertEquals(1f, Motor.fatorDaPressaoNoTamanho(0.1f, 0f))
    }

    @Test
    fun velocidadeAfinaOuClareiaConformeASensibilidade() {
        assertEquals(1f, Motor.fatorDaVelocidade(5f, 0f))
        assertEquals(1f, Motor.fatorDaVelocidade(0f, 1f))
        assertEquals(0.1f, Motor.fatorDaVelocidade(Motor.VELOCIDADE_RAPIDA, 1f), 0.0001f)
        assertEquals(0.1f, Motor.fatorDaVelocidade(Motor.VELOCIDADE_RAPIDA * 3, 1f), 0.0001f)
        assertEquals(0.55f, Motor.fatorDaVelocidade(Motor.VELOCIDADE_RAPIDA, 0.5f), 0.0001f)
        assertEquals(0.775f, Motor.fatorDaVelocidade(Motor.VELOCIDADE_RAPIDA / 2, 0.5f), 0.0001f)
    }

    @Test
    fun velocidadeSuavizadaNaoPula() {
        assertEquals(2f, Motor.suavizarVelocidade(null, 2f))
        assertEquals(1.25f, Motor.suavizarVelocidade(1f, 2f), 0.0001f)
    }

    @Test
    fun opacidadeSoClareiaNoToqueLeve() {
        val normal = Motor.PRESSAO_NORMAL
        assertEquals(1f, Motor.fatorDaPressaoNaOpacidade(null, 1f))
        assertEquals(1f, Motor.fatorDaPressaoNaOpacidade(normal, 1f))
        assertEquals(1f, Motor.fatorDaPressaoNaOpacidade(1f, 1f))
        assertEquals(0.5f, Motor.fatorDaPressaoNaOpacidade(normal / 2f, 1f), 0.0001f)
    }

    @Test
    fun catalogoTemIdsUnicos() {
        val ids = PinceisPredefinidosDoMotor.TODOS.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertEquals("giz", PinceisPredefinidosDoMotor.porId("giz")?.id)
    }
}

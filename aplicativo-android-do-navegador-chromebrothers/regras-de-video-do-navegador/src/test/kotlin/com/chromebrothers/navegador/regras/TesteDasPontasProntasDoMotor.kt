package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDasPontasProntasDoMotor {
    private val lado = 64

    @Test
    fun cadaPontaTemTintaEBordaVazia() {
        PontasProntasDoMotor.TODAS.forEach { ponta ->
            val a = PontasProntasDoMotor.alfas(ponta, lado)
            assertEquals(lado * lado, a.size)
            assertTrue(a.any { it > 60 }, "${ponta.id} sem tinta")
            // Cantos vazios: o carimbo não deixa quadrado aparecendo.
            listOf(0, lado - 1, (lado - 1) * lado, lado * lado - 1).forEach { canto ->
                assertTrue(a[canto] < 40, "${ponta.id} com canto preenchido (${a[canto]})")
            }
        }
    }

    @Test
    fun desenhoSempreIgual() {
        PontasProntasDoMotor.TODAS.forEach { ponta ->
            assertContentEquals(PontasProntasDoMotor.alfas(ponta, lado), PontasProntasDoMotor.alfas(ponta, lado))
        }
    }

    @Test
    fun pontasSaoDiferentesEntreSi() {
        val desenhos = PontasProntasDoMotor.TODAS.map { PontasProntasDoMotor.alfas(it, lado).toList() }
        assertEquals(desenhos.size, desenhos.toSet().size)
    }

    @Test
    fun arquivosUnicosEPincelUsaAPonta() {
        val arquivos = PontasProntasDoMotor.TODAS.map { it.arquivo }
        assertEquals(arquivos.size, arquivos.toSet().size)
        assertEquals(PontasProntasDoMotor.CARVAO, PontasProntasDoMotor.porArquivo(PinceisPredefinidosDoMotor.CARVAO.arquivoDaForma))
        assertEquals(FormaDoCarimbo.IMAGEM, PinceisPredefinidosDoMotor.AQUARELA.forma)
    }
}

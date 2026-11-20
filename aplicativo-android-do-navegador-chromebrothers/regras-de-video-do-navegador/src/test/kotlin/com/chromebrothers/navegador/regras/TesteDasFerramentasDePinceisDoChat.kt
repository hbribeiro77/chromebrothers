package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDasFerramentasDePinceisDoChat {
    private val ferramentas = FerramentasDePinceisDoChat
    private val base = PincelDoMotor(id = "meu-x", nome = "X")

    @Test
    fun aplicaSoOQueVeioEPrendeNaFaixa() {
        val r = ferramentas.aplicarParametros(base, """{"espacamento":0.05,"fluxo":3,"seguirTraco":true,"nome":"Meu nanquim"}""")
        assertEquals(0.05f, r.pincel.espacamento, 0.0001f)
        assertEquals(1f, r.pincel.fluxo, 0.0001f)
        assertTrue(r.pincel.seguirTraco)
        assertEquals("Meu nanquim", r.pincel.nome)
        assertEquals(base.dureza, r.pincel.dureza)
        assertTrue(r.avisos.any { it.startsWith("fluxo") })
    }

    @Test
    fun pontaProntaViraFormaDeImagem() {
        val r = ferramentas.aplicarParametros(base, """{"ponta":"carvao"}""")
        assertEquals(FormaDoCarimbo.IMAGEM, r.pincel.forma)
        assertEquals(PontasProntasDoMotor.CARVAO.arquivo, r.pincel.arquivoDaForma)
        val invalida = ferramentas.aplicarParametros(base, """{"ponta":"unicornio"}""")
        assertEquals(base, invalida.pincel)
        assertTrue(invalida.avisos.single().contains("não existe"))
    }

    @Test
    fun formaEGranuladoValidados() {
        val r = ferramentas.aplicarParametros(base, """{"forma":"quadrada","granulado":"PAPEL"}""")
        assertEquals(FormaDoCarimbo.QUADRADA, r.pincel.forma)
        assertEquals(FonteDoGranulado.PAPEL, r.pincel.granulado)
        val semArquivo = ferramentas.aplicarParametros(base, """{"forma":"IMAGEM"}""")
        assertEquals(FormaDoCarimbo.REDONDA, semArquivo.pincel.forma)
        assertTrue(semArquivo.avisos.isNotEmpty())
    }

    @Test
    fun limitesMinimoEMaximoFicamCoerentes() {
        val r = ferramentas.aplicarParametros(base, """{"tamanhoMinimo":0.8,"tamanhoMaximo":0.5}""")
        assertTrue(r.pincel.tamanhoMinimo <= r.pincel.tamanhoMaximo)
    }

    @Test
    fun definicoesTemTodasAsFerramentasEAsPontas() {
        val json = ferramentas.definicoes().joinToString(",")
        ferramentas.nomes.forEach { assertTrue(json.contains("\"name\":\"$it\""), it) }
        assertTrue(json.contains("\"carvao\""))
        assertTrue(json.contains("\"espacamento\""))
    }

    @Test
    fun modoConversaSoConsultaETesta() {
        assertTrue(ModoConversaDoChat.podeUsar(FerramentasDePinceisDoChat.LISTAR))
        assertTrue(ModoConversaDoChat.podeUsar(FerramentasDePinceisDoChat.TESTAR))
        assertFalse(ModoConversaDoChat.podeUsar(FerramentasDePinceisDoChat.CRIAR))
        assertFalse(ModoConversaDoChat.podeUsar(FerramentasDePinceisDoChat.USAR))
    }

    @Test
    fun manualDePinceisCobreParametrosEPontas() {
        val manual = ManualDoAgenteDoChat.texto("""{"topico":"pinceis"}""")
        assertTrue(manual.contains("espacamento"))
        assertTrue(manual.contains("cerdas"))
        assertTrue(manual.contains("testar_pincel"))
    }
}

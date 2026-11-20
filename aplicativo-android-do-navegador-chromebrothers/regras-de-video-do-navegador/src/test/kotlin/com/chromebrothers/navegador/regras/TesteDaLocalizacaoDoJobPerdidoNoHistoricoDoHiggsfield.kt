package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.LocalizacaoDoJobPerdidoNoHistoricoDoHiggsfield as Loc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDaLocalizacaoDoJobPerdidoNoHistoricoDoHiggsfield {

    private val t0 = 1_794_652_130_000L
    private val prompt = "Fight scene where the skull masked henchman  beating Batgirl"

    private fun item(id: String, deltaMs: Long, p: String = prompt, modelo: String = "wan3_0", status: String = "completed") =
        ItemDoHistoricoDoHiggsfield(id, p, modelo, t0 + deltaMs, status)

    @Test
    fun achaOJobComMesmoPromptLogoDepoisDoCard() {
        val itens = listOf(item("velho", -3_600_000), item("certo", 6_000), item("outro", 7_000, p = "outra coisa"))
        assertEquals("certo", Loc.escolher(itens, prompt, "wan3_0", t0, emptySet()))
    }

    @Test
    fun ignoraEspacosDiferentesNoPrompt() {
        val itens = listOf(item("a", 5_000, p = "Fight scene where the skull masked henchman beating Batgirl "))
        assertEquals("a", Loc.escolher(itens, prompt, "", t0, emptySet()))
    }

    @Test
    fun naoPegaJobJaLigadoAOutroCard() {
        val itens = listOf(item("do-card-1", 4_000), item("do-card-2", 5_000))
        assertEquals("do-card-2", Loc.escolher(itens, prompt, "wan3_0", t0, setOf("do-card-1")))
        assertNull(Loc.escolher(itens, prompt, "wan3_0", t0, setOf("do-card-1", "do-card-2")))
    }

    @Test
    fun foraDaJanelaNaoServe() {
        assertNull(Loc.escolher(listOf(item("tarde", Loc.JANELA_DEPOIS_MS + 1)), prompt, "", t0, emptySet()))
        assertNull(Loc.escolher(listOf(item("cedo", -Loc.FOLGA_ANTES_MS - 1)), prompt, "", t0, emptySet()))
    }

    @Test
    fun prefereMesmoModeloEDepoisOMaisProximo() {
        val itens = listOf(item("kling", 1_000, modelo = "kling"), item("wan-longe", 60_000), item("wan-perto", 9_000))
        assertEquals("wan-perto", Loc.escolher(itens, prompt, "wan3_0", t0, emptySet()))
    }

    @Test
    fun promptVazioNaoProcura() {
        assertNull(Loc.escolher(listOf(item("x", 1_000, p = "")), "  ", "", t0, emptySet()))
    }
}

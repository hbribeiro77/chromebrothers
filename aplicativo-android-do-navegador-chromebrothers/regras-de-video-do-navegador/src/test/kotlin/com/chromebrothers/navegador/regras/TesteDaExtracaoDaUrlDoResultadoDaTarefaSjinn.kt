package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaExtracaoDaUrlDoResultadoDaTarefaSjinn {
    @Test
    fun pendenteComSuccessTrueNaoEstaConcluida() {
        val bruto = """
            {"success":true,"data":{"status":0,"task_id":"t1",
             "input":{"image_list":["https://cdn.sjinn.ai/uploads/ref.jpg"]},
             "output_urls":[]}}
        """.trimIndent()
        assertFalse(ExtracaoDaUrlDoResultadoDaTarefaSjinn.tarefaConcluida(bruto))
        assertNull(ExtracaoDaUrlDoResultadoDaTarefaSjinn.urlDoResultado(bruto))
    }

    @Test
    fun naoDevolveUrlDaReferenciaDoInput() {
        val ref = "https://cdn.sjinn.ai/comfy-online/ref-abc.jpg"
        val out = "https://cdn.sjinn.ai/outputs/gerada-xyz.png"
        val bruto = """
            {"success":true,"data":{"status":1,
             "input":{"prompt":"x","image_list":["$ref"]},
             "output_urls":["$out"]}}
        """.trimIndent()
        assertEquals(out, ExtracaoDaUrlDoResultadoDaTarefaSjinn.urlDoResultado(bruto))
        assertEquals(
            out,
            ExtracaoDaUrlDoResultadoDaTarefaSjinn.urlDoResultado(bruto, urlsDeEntrada = setOf(ref)),
        )
    }

    @Test
    fun ignoraOutputQueCoincideComReferencia() {
        val ref = "https://cdn.sjinn.ai/comfy-online/ref-abc.jpg"
        val bruto = """
            {"success":true,"data":{"status":1,
             "input":{"image_list":["$ref"]},
             "output_urls":["$ref"]}}
        """.trimIndent()
        assertNull(
            ExtracaoDaUrlDoResultadoDaTarefaSjinn.urlDoResultado(bruto, urlsDeEntrada = setOf(ref)),
        )
    }

    @Test
    fun status1ComOutputOk() {
        val bruto = """{"success":true,"data":{"status":1,"output_urls":["https://cdn.sjinn.ai/out.png"]}}"""
        assertTrue(ExtracaoDaUrlDoResultadoDaTarefaSjinn.tarefaConcluida(bruto))
        assertEquals(
            "https://cdn.sjinn.ai/out.png",
            ExtracaoDaUrlDoResultadoDaTarefaSjinn.urlDoResultado(bruto),
        )
    }
}

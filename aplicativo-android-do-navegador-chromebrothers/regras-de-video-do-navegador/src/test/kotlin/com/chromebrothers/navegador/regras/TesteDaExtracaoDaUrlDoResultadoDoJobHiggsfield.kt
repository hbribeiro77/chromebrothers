package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaExtracaoDaUrlDoResultadoDoJobHiggsfield {
    @Test
    fun inProgressComMediaInputNaoDevolveUrlDaReferencia() {
        val status = """
            {"content":[{"type":"text","text":"Job x — in_progress"}],
            "structuredContent":{"generation":{"id":"e571","status":"in_progress","params":{
            "medias":[{"role":"image","data":{"id":"8d31","type":"media_input",
            "url":"https://d2ol7oe51mr4n9.cloudfront.net/user/8d31_resize.jpg"}}]}}}}
        """.trimIndent()
        assertFalse(ExtracaoDaUrlDoResultadoDoJobHiggsfield.jobConcluido(status))
        assertNull(ExtracaoDaUrlDoResultadoDoJobHiggsfield.urlDoResultado(status))
    }

    @Test
    fun jobsWaitComResultUrlDevolveAGerada() {
        val wait = """
            jobs[1]{index,job_id,status,type,model,result_url}:
              0,5ea9d980-5337-4078-a628-75a29ae6cb8d,completed,image,gpt_image_2_6,"https://d8j0ntlcm91z4.cloudfront.net/user/hf_20261105_211115_5ea9d980.png"
            all_found: true
        """.trimIndent()
        assertTrue(ExtracaoDaUrlDoResultadoDoJobHiggsfield.jobConcluido(wait))
        assertEquals(
            "https://d8j0ntlcm91z4.cloudfront.net/user/hf_20261105_211115_5ea9d980.png",
            ExtracaoDaUrlDoResultadoDoJobHiggsfield.urlDoResultado(wait),
        )
    }

    @Test
    fun ignoraUploadEResizeMesmoSeHouverVariasUrls() {
        val misturado = """
            {"status":"completed","params":{"medias":[{"data":{"type":"media_input",
            "url":"https://d2ol7oe51mr4n9.cloudfront.net/user/abc_resize.jpg"}}]},
            "result_url":"https://d8j0ntlcm91z4.cloudfront.net/user/hf_out.png"}
        """.trimIndent()
        assertEquals(
            "https://d8j0ntlcm91z4.cloudfront.net/user/hf_out.png",
            ExtracaoDaUrlDoResultadoDoJobHiggsfield.urlDoResultado(misturado),
        )
    }

    @Test
    fun falhaNaoDevolveUrl() {
        val falha = """{"generation":{"status":"failed","error":"boom"}}"""
        assertTrue(ExtracaoDaUrlDoResultadoDoJobHiggsfield.jobFalhou(falha))
        assertNull(ExtracaoDaUrlDoResultadoDoJobHiggsfield.urlDoResultado(falha))
    }

    @Test
    fun summaryFailedZeroNaoEhFalhaEDevolveResultUrl() {
        // Regressão: `"fail"` casava dentro de `"failed":0` e abortava o poll em ~30s.
        val wait = """
            {"content":[{"type":"text","text":"jobs[1]{index,job_id,status,type,model,result_url}:\n  0,264cc491-9214-4f73-9402-a05e483970e8,completed,image,nano_banana_2_1,\"https://d8j0ntlcm91z4.cloudfront.net/user/hf_20261106_034252_264cc491.png\"\nsummary:\n  total: 1\n  completed: 1\n  failed: 0\n  active: 0\n  errors: 0\nall_terminal: true"}],
            "structuredContent":{"jobs":[{"index":0,"job_id":"264cc491","status":"completed","result_url":"https://d8j0ntlcm91z4.cloudfront.net/user/hf_20261106_034252_264cc491.png"}],
            "summary":{"total":1,"completed":1,"failed":0,"active":0,"errors":0},"all_terminal":true}}
        """.trimIndent()
        assertFalse(ExtracaoDaUrlDoResultadoDoJobHiggsfield.jobFalhou(wait))
        assertTrue(ExtracaoDaUrlDoResultadoDoJobHiggsfield.jobConcluido(wait))
        assertEquals(
            "https://d8j0ntlcm91z4.cloudfront.net/user/hf_20261106_034252_264cc491.png",
            ExtracaoDaUrlDoResultadoDoJobHiggsfield.urlDoResultado(wait),
        )
    }

    @Test
    fun jobsWaitTimedOutComFailedZeroContinuaEmProgresso() {
        val wait = """
            {"content":[{"type":"text","text":"jobs[1]{index,job_id,status,type,model}:\n  0,c0b75e20,in_progress,image,nano_banana_2_1\nsummary:\n  failed: 0\nall_terminal: false\ntimed_out: true"}],
            "structuredContent":{"jobs":[{"status":"in_progress"}],"summary":{"failed":0},"timed_out":true}}
        """.trimIndent()
        assertFalse(ExtracaoDaUrlDoResultadoDoJobHiggsfield.jobFalhou(wait))
        assertFalse(ExtracaoDaUrlDoResultadoDoJobHiggsfield.jobConcluido(wait))
        assertNull(ExtracaoDaUrlDoResultadoDoJobHiggsfield.urlDoResultado(wait))
    }
}

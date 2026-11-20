package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaReceitaDeGeracaoMcp {

    private val hashA = "a".repeat(64)
    private val hashB = "0123456789abcdef".repeat(4)
    private val hashQ = "f".repeat(64)

    @Test
    fun idaEVoltaPreservaPedidoBrutoERefs() {
        val pedido = """{"mcps":["sjinn","topview"],"prompt":"chuva \"forte\"\nà noite","quantidade":2,"duracao":5}"""
        val receita = ReceitaDeGeracaoMcp(
            id = "r1",
            tipo = ReceitaDeGeracaoMcp.TIPO_VIDEO,
            criadoEmMs = 99L,
            pedidoJson = pedido,
            refs = listOf(
                RefDaReceitaDeGeracaoMcp("imagem", "foto.png", hashA, "png"),
                RefDaReceitaDeGeracaoMcp("video", "clipe.mp4", hashB, "mp4", duracaoMs = 4200L, hashDoQuadro = hashQ),
            ),
        )
        val lida = ReceitaDeGeracaoMcp.deJson(ReceitaDeGeracaoMcp.paraJson(receita))
        assertEquals(receita, lida)
        assertEquals(pedido, lida!!.pedidoJson)
        assertTrue(lida.refs[1].ehVideo)
        assertEquals("$hashB.mp4", lida.refs[1].nomeDoArquivo)
        assertEquals("$hashQ.jpg", lida.refs[1].nomeDoQuadro)
    }

    @Test
    fun hashOuExtensaoMaliciososNaoViramCaminho() {
        val json = """{"id":"r","tipo":"imagem","criadoEmMs":1,"pedidoJson":"{}","refs":[
            {"tipo":"imagem","nome":"x","hash":"../../segredo","extensao":"png"},
            {"tipo":"imagem","nome":"y","hash":"$hashA","extensao":"p/../ng"}]}"""
        val lida = ReceitaDeGeracaoMcp.deJson(json)!!
        assertEquals(1, lida.refs.size)
        assertEquals("bin", lida.refs[0].extensao)
        assertTrue(lida.refs[0].nomeDoArquivo.none { it == '/' })
    }

    @Test
    fun jsonVazioOuSemIdDevolveNulo() {
        assertNull(ReceitaDeGeracaoMcp.deJson(""))
        assertNull(ReceitaDeGeracaoMcp.deJson("""{"tipo":"imagem"}"""))
    }

    @Test
    fun arquivosEmUsoIncluiQuadroDoVideoEDeduplica() {
        val r1 = ReceitaDeGeracaoMcp("1", "imagem", 0, "{}", listOf(RefDaReceitaDeGeracaoMcp("imagem", "a", hashA, "jpg")))
        val r2 = ReceitaDeGeracaoMcp(
            "2", "video", 0, "{}",
            listOf(
                RefDaReceitaDeGeracaoMcp("imagem", "a", hashA, "jpg"),
                RefDaReceitaDeGeracaoMcp("video", "v", hashB, "mp4", hashDoQuadro = hashQ),
            ),
        )
        assertEquals(setOf("$hashA.jpg", "$hashB.mp4", "$hashQ.jpg"), ReceitaDeGeracaoMcp.arquivosEmUso(listOf(r1, r2)))
    }
}

# Review package Task 1 (no git — full new files)
Base: (none) Head: working tree

## FILE: regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\PedidoDeGeracaoDeImagemViaMcpNaColecao.kt

```kotlin
package com.chromebrothers.navegador.regras

data class PedidoDeGeracaoDeImagemViaMcpNaColecao(
    val mcp: String,
    val prompt: String,
    val modelo: String,
    val quantidade: Int,
    val temReferencia: Boolean,
) {
    companion object {
        const val MCP_HIGGSFIELD = "higgsfield"
        val MCPS_CONHECIDOS = setOf(MCP_HIGGSFIELD)

        fun ler(json: String): ResultadoDoPedidoDeGeracaoDeImagemViaMcp {
            val mcp = TextoJsonDoChat.valorDaChave(json, "mcp")?.trim()?.lowercase().orEmpty().ifBlank { MCP_HIGGSFIELD }
            val prompt = TextoJsonDoChat.valorDaChave(json, "prompt")?.trim().orEmpty()
            val modelo = TextoJsonDoChat.valorDaChave(json, "modelo")?.trim().orEmpty()
            val q = TextoJsonDoChat.inteiroDaChave(json, "quantidade") ?: 1
            val temRef = TextoJsonDoChat.valorDaChave(json, "temReferencia")?.equals("true", true) == true
                || json.contains("\"temReferencia\":true")
            return when {
                prompt.isEmpty() -> ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("Faltou o prompt.")
                prompt.length > GeracaoDeImagemEVideoPeloHiggsfieldNoChat.LIMITE_DO_PROMPT ->
                    ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("O prompt passou do limite.")
                mcp !in MCPS_CONHECIDOS -> ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("MCP não suportado: $mcp")
                q !in 1..4 -> ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("A quantidade deve ser entre 1 e 4.")
                else -> ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok(
                    PedidoDeGeracaoDeImagemViaMcpNaColecao(mcp, prompt, modelo, q, temRef),
                )
            }
        }
    }
}

sealed class ResultadoDoPedidoDeGeracaoDeImagemViaMcp {
    data class Ok(val pedido: PedidoDeGeracaoDeImagemViaMcpNaColecao) : ResultadoDoPedidoDeGeracaoDeImagemViaMcp()
    data class Recusado(val mensagem: String) : ResultadoDoPedidoDeGeracaoDeImagemViaMcp()
}

object RespostaJsonDaPonteDeGeracaoMcp {
    fun erro(mensagem: String) =
        """{"ok":false,"erro":"${TextoJsonDoChat.escapar(mensagem)}"}"""
    fun okAndamento() = """{"ok":true,"andamento":true}"""
    fun okMcps() = """{"ok":true,"itens":[{"id":"higgsfield","nome":"Higgsfield"}]}"""
    fun okLimpar() = """{"ok":true}"""
}

```

## FILE: regras-de-video-do-navegador\src\test\kotlin\com\chromebrothers\navegador\regras\TesteDoPedidoDeGeracaoDeImagemViaMcpNaColecao.kt

```kotlin
package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TesteDoPedidoDeGeracaoDeImagemViaMcpNaColecao {
    @Test
    fun exigePromptEQuantidadeEntre1e4() {
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(
            PedidoDeGeracaoDeImagemViaMcpNaColecao.ler("""{"mcp":"higgsfield","prompt":"","quantidade":1}"""),
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(
            PedidoDeGeracaoDeImagemViaMcpNaColecao.ler("""{"mcp":"higgsfield","prompt":"x","quantidade":5}"""),
        )
        val ok = PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(
            """{"mcp":"higgsfield","prompt":"gato","modelo":"nano_banana_2_1","quantidade":2,"temReferencia":true}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok>(ok)
        assertEquals("higgsfield", ok.pedido.mcp)
        assertEquals(2, ok.pedido.quantidade)
        assertTrue(ok.pedido.temReferencia)
    }

    @Test
    fun recusaMcpDesconhecido() {
        val r = PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(
            """{"mcp":"outro","prompt":"x","quantidade":1}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(r)
    }
}

```

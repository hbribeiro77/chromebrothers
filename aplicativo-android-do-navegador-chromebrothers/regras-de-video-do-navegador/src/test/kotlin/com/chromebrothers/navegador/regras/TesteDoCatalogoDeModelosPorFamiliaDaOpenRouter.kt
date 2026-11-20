package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoCatalogoDeModelosPorFamiliaDaOpenRouter {
    @Test
    fun aConsultaPedeAsCincoCasasEmTextoDoMaisNovo() {
        val url = CatalogoDeModelosPorFamiliaDaOpenRouter.url(1000)
        assertTrue(url.startsWith("https://openrouter.ai/api/v1/models?"))
        assertTrue(url.contains("model_authors=deepseek,openai,anthropic,google,x-ai,z-ai,moonshotai"))
        assertTrue(url.contains("output_modalities=text"))
        assertTrue(url.contains("sort=newest"))
        assertTrue(url.contains("limit=1000"))
        assertTrue(url.contains("offset=1000"))
    }

    @Test
    fun aPaginaSeparaAsFamiliasEDescartaLoteEApelido() {
        val json = """
            {"data":[
              {"id":"deepseek/deepseek-v4.1-flash-1017","name":"Deep \"Seek\"","architecture":{"tokenizer":"GPT"}},
              {"id":"openai/gpt-6.1-terra","name":"Terra"},
              {"id":"openai/gpt-4o-mini:free","name":"Mini"},
              {"id":"openai/gpt-4o-mini:batch","name":"Lote"},
              {"id":"~openai/gpt-latest","name":"Alias"},
              {"id":"openai/o3","name":"O3"},
              {"id":"z-ai/glm-5.3-flash","name":"GLM"},
              {"id":"moonshotai/kimi-k3.1-pro","name":""},
              {"id":"google/gemini-4-argon","name":"Gemini"},
              {"id":"meta-llama/llama-5","name":"Llama","id":"nao-conta"}
            ]}
        """.trimIndent()
        val pagina = CatalogoDeModelosPorFamiliaDaOpenRouter.pagina(json)
        assertEquals(10, pagina.quantidadeBruta)
        assertEquals(
            listOf(
                "deepseek/deepseek-v4.1-flash-1017",
                "openai/gpt-6.1-terra",
                "openai/gpt-4o-mini:free",
                "z-ai/glm-5.3-flash",
                "moonshotai/kimi-k3.1-pro",
                "google/gemini-4-argon",
            ),
            pagina.modelos.map { it.id },
        )
        assertEquals("Deep \"Seek\"", pagina.modelos.first().nome)
        assertEquals("moonshotai/kimi-k3.1-pro", pagina.modelos[4].nome)
        assertEquals(
            listOf("DeepSeek", "GPT", "GLM", "Kimi", "Gemini"),
            pagina.modelos.map { it.familia }.distinct(),
        )
        assertNull(CatalogoDeModelosPorFamiliaDaOpenRouter.familiaDe("openai/gpt-4o-mini:batch"))
        assertNull(CatalogoDeModelosPorFamiliaDaOpenRouter.familiaDe("~openai/gpt-latest"))
        assertEquals(0, CatalogoDeModelosPorFamiliaDaOpenRouter.pagina("""{"data":[]}""").quantidadeBruta)
    }

    @Test
    fun claudeEGrokTemFamiliaPropria() {
        assertEquals("Claude", CatalogoDeModelosPorFamiliaDaOpenRouter.familiaDe("anthropic/claude-sonnet-5.5"))
        assertEquals("Grok", CatalogoDeModelosPorFamiliaDaOpenRouter.familiaDe("x-ai/grok-5"))
        assertTrue(CatalogoDeModelosPorFamiliaDaOpenRouter.familias.containsAll(listOf("Claude", "Grok")))
        assertTrue(CatalogoDeModelosPorFamiliaDaOpenRouter.url(0).contains("anthropic"))
        assertTrue(CatalogoDeModelosPorFamiliaDaOpenRouter.url(0).contains("x-ai"))
    }
}

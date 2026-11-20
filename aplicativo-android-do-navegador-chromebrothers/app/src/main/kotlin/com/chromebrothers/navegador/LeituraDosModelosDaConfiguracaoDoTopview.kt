package com.chromebrothers.navegador

import com.chromebrothers.navegador.regras.FiltroDosModelosDeVideoDoTopviewNoComponente
import org.json.JSONArray
import org.json.JSONObject

/**
 * Lê a resposta de `topview_get_generation_config(type=video)` e monta `{ok, itens:[{id, nome}]}`.
 * `id` = `submitModel` (o valor que a geração exige); `nome` = `displayName`.
 * Tolerante ao formato: procura `models[]` no `structuredContent` ou no texto JSON do `content`.
 */
object LeituraDosModelosDaConfiguracaoDoTopview {
    fun jsonParaPonte(brutoDoToolsCall: String): String {
        val modelos = modelos(brutoDoToolsCall)
        if (modelos.isEmpty()) error("O Topview não devolveu modelos de vídeo.")
        val itens = JSONArray()
        modelos.forEach { (id, nome) -> itens.put(JSONObject().put("id", id).put("nome", nome)) }
        return JSONObject().put("ok", true).put("itens", itens).toString()
    }

    fun modelos(bruto: String): List<Pair<String, String>> {
        val raiz = runCatching { JSONObject(bruto.trim()) }.getOrNull() ?: return emptyList()
        val candidatos = mutableListOf<Any>()
        raiz.optJSONObject("structuredContent")?.let { candidatos += it }
        raiz.optJSONArray("content")?.let { content ->
            for (i in 0 until content.length()) {
                val texto = content.optJSONObject(i)?.optString("text").orEmpty().trim()
                runCatching { JSONObject(texto) }.getOrNull()?.let { candidatos += it }
            }
        }
        candidatos += raiz
        for (c in candidatos) {
            val lista = acharModelos(c, 0) ?: continue
            val saida = linkedMapOf<String, String>()
            for (i in 0 until lista.length()) {
                val m = lista.optJSONObject(i) ?: continue
                val id = m.optString("submitModel").ifBlank { m.optString("model") }.ifBlank { m.optString("id") }.trim()
                if (id.isEmpty()) continue
                val nome = m.optString("displayName").ifBlank { m.optString("name") }.ifBlank { id }.trim()
                val modos = m.optJSONArray("inputModes")?.let { a -> (0 until a.length()).map { a.optString(it) } }.orEmpty()
                if (!FiltroDosModelosDeVideoDoTopviewNoComponente.serve(nome, modos)) continue
                saida.putIfAbsent(id, nome)
            }
            if (saida.isNotEmpty()) return saida.toList()
        }
        return emptyList()
    }

    private fun acharModelos(no: Any?, profundidade: Int): JSONArray? {
        if (profundidade > 6) return null
        return when (no) {
            is JSONObject -> {
                no.optJSONArray("models")?.let { return it }
                val chaves = no.keys()
                while (chaves.hasNext()) {
                    acharModelos(no.opt(chaves.next()), profundidade + 1)?.let { return it }
                }
                null
            }
            is JSONArray -> {
                for (i in 0 until no.length()) acharModelos(no.opt(i), profundidade + 1)?.let { return it }
                null
            }
            else -> null
        }
    }
}

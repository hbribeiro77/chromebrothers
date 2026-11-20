package com.chromebrothers.navegador.regras

/** Uma geração do histórico do Higgsfield (`show_generations`). */
data class ItemDoHistoricoDoHiggsfield(
    val id: String,
    val prompt: String,
    val modelo: String,
    val criadoEmMs: Long,
    val status: String,
)

/**
 * Card sem id da tarefa (a resposta da criação se perdeu na rede, mas o Higgsfield pode ter criado o job):
 * acha no histórico a geração com o mesmo prompt, criada pouco depois do card e que nenhum outro card usa.
 */
object LocalizacaoDoJobPerdidoNoHistoricoDoHiggsfield {
    /** O card nasce antes do envio (upload das refs vem antes): aceita uma folga para trás por relógio. */
    const val FOLGA_ANTES_MS = 2 * 60_000L
    const val JANELA_DEPOIS_MS = 15 * 60_000L

    fun escolher(
        itens: List<ItemDoHistoricoDoHiggsfield>,
        prompt: String,
        modelo: String,
        criadoEmMs: Long,
        idsJaUsados: Set<String>,
    ): String? {
        val alvo = normalizar(prompt)
        if (alvo.isEmpty()) return null
        val candidatos = itens.filter { item ->
            item.id.isNotBlank() &&
                item.id !in idsJaUsados &&
                normalizar(item.prompt) == alvo &&
                item.criadoEmMs in (criadoEmMs - FOLGA_ANTES_MS)..(criadoEmMs + JANELA_DEPOIS_MS)
        }
        if (candidatos.isEmpty()) return null
        // Mesmo modelo primeiro (quando os dois lados informam); depois o mais próximo do card no tempo.
        val mesmoModelo = candidatos.filter { modelo.isNotBlank() && it.modelo.isNotBlank() && it.modelo.equals(modelo, ignoreCase = true) }
        val base = mesmoModelo.ifEmpty { candidatos }
        return base.minByOrNull { kotlin.math.abs(it.criadoEmMs - criadoEmMs) }?.id
    }

    private fun normalizar(texto: String): String = texto.trim().replace(Regex("\\s+"), " ")
}

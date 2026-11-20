package com.chromebrothers.navegador.regras

/**
 * Cada modelo do Magnific aceita durações/resoluções/proporções próprias. O componente tem opções
 * fixas; aqui o pedido é encaixado no valor mais próximo que o modelo aceita (ou omitido).
 */
object AjusteDoPedidoAoModeloDoMagnific {
    /** Duração aceita mais próxima; empate vai para a maior. Sem lista, mantém. */
    fun duracao(pedida: Int, aceitas: List<Int>): Int {
        if (aceitas.isEmpty()) return pedida
        return aceitas.sortedDescending().minBy { kotlin.math.abs(it - pedida) }
    }

    /**
     * Resolução aceita mais próxima, comparando a altura/lado (480p, 1k, 4K…). Valores como "Draft"
     * nunca são escolhidos. Sem lista ou sem nada comparável → null (o servidor usa o padrão do modelo).
     */
    fun resolucao(pedida: String, aceitas: List<String>): String? {
        val alvo = pixels(pedida) ?: return null
        aceitas.firstOrNull { it.equals(pedida, ignoreCase = true) }?.let { return it }
        return aceitas
            .filter { !it.contains("draft", ignoreCase = true) }
            .mapNotNull { r -> pixels(r)?.let { r to it } }
            .sortedByDescending { it.second }
            .minByOrNull { kotlin.math.abs(it.second - alvo) }
            ?.first
    }

    /** Proporção só vai se o modelo aceitar; `adaptive` (UI) vira omitida. */
    fun proporcao(pedida: String, aceitas: List<String>): String? {
        if (pedida.equals("adaptive", ignoreCase = true) || pedida.isBlank()) return null
        if (aceitas.isEmpty()) return pedida
        return aceitas.firstOrNull { it == pedida }
    }

    /** 480p → 480; 1080p → 1080; 1k → 1024; 1.5k → 1536; 4K → 2160 (vídeo) / 4096 (imagem): só a ordem importa. */
    private fun pixels(valor: String): Int? {
        val v = valor.trim().lowercase()
        Regex("""^(\d+)p\b""").find(v)?.let { return it.groupValues[1].toInt() }
        Regex("""^(\d+(?:\.\d+)?)k$""").find(v)?.let { return (it.groupValues[1].toDouble() * 1024).toInt() }
        return null
    }
}

package com.chromebrothers.navegador.regras

import java.util.Locale

/** Grupos mostrados na tela de Armazenamento, na ordem da lista. */
enum class CategoriaDoArmazenamento(val rotulo: String) {
    VIDEOS_GERADOS("Vídeos gerados"),
    IMAGENS("Imagens"),
    REFERENCIAS("Referências das gerações"),
    VIDEOS_DA_WEB("Vídeos salvos da web"),
    CONVERSAS("Conversas do chat"),
    PINCEIS_E_RASCUNHOS("Pincéis e rascunhos do editor"),
    NAVEGADOR("Dados do navegador"),
    CACHE("Cache"),
    OUTROS("Outros (páginas, roteiros, abas…)"),
}

/** Um arquivo medido, com caminho relativo à pasta de dados do app (ex.: `files/biblioteca-…/x.mp4`). */
data class ArquivoMedido(val caminho: String, val bytes: Long)

data class TotalDoArmazenamento(val bytes: Long, val quantidade: Int)

object ArmazenamentoDoAplicativo {

    private val pastasPorCategoria: List<Pair<String, CategoriaDoArmazenamento>> = listOf(
        "files/biblioteca-de-videos-higgsfield/" to CategoriaDoArmazenamento.VIDEOS_GERADOS,
        "files/biblioteca-de-imagens-capturadas/" to CategoriaDoArmazenamento.IMAGENS,
        "files/refs-das-receitas/" to CategoriaDoArmazenamento.REFERENCIAS,
        "files/receitas-de-geracao/" to CategoriaDoArmazenamento.REFERENCIAS,
        "files/miniaturas-dos-cards-mcp/" to CategoriaDoArmazenamento.REFERENCIAS,
        "files/refs-de-geracao-da-biblioteca/" to CategoriaDoArmazenamento.REFERENCIAS,
        "files/biblioteca-de-videos-com-miniatura/" to CategoriaDoArmazenamento.VIDEOS_DA_WEB,
        "files/conversas_do_chat/" to CategoriaDoArmazenamento.CONVERSAS,
        "files/pinceis/" to CategoriaDoArmazenamento.PINCEIS_E_RASCUNHOS,
        "files/rascunhos-da-edicao-de-imagem/" to CategoriaDoArmazenamento.PINCEIS_E_RASCUNHOS,
        "files/mozilla/" to CategoriaDoArmazenamento.NAVEGADOR,
        "app_webview/" to CategoriaDoArmazenamento.NAVEGADOR,
        "cache/" to CategoriaDoArmazenamento.CACHE,
        "code_cache/" to CategoriaDoArmazenamento.CACHE,
    )

    fun categoria(caminho: String): CategoriaDoArmazenamento {
        val normalizado = caminho.replace('\\', '/').trimStart('/')
        return pastasPorCategoria.firstOrNull { (prefixo, _) -> normalizado.startsWith(prefixo) }?.second
            ?: CategoriaDoArmazenamento.OUTROS
    }

    /** Totais por categoria, na ordem do enum, só as que têm algo. */
    fun agrupar(arquivos: List<ArquivoMedido>): LinkedHashMap<CategoriaDoArmazenamento, TotalDoArmazenamento> {
        val somas = mutableMapOf<CategoriaDoArmazenamento, TotalDoArmazenamento>()
        arquivos.forEach { a ->
            val c = categoria(a.caminho)
            val atual = somas[c] ?: TotalDoArmazenamento(0L, 0)
            somas[c] = TotalDoArmazenamento(atual.bytes + a.bytes.coerceAtLeast(0L), atual.quantidade + 1)
        }
        val saida = LinkedHashMap<CategoriaDoArmazenamento, TotalDoArmazenamento>()
        CategoriaDoArmazenamento.entries.forEach { c -> somas[c]?.let { saida[c] = it } }
        return saida
    }

    /** "3,4 GB", "418 MB", "12 KB", "0 B" (base 1024, uma casa decimal até 10, vírgula pt-BR). */
    fun formatarBytes(bytes: Long): String {
        if (bytes < 1024) return "${bytes.coerceAtLeast(0)} B"
        val unidades = listOf("KB", "MB", "GB", "TB")
        var valor = bytes / 1024.0
        var indice = 0
        while (valor >= 1024 && indice < unidades.lastIndex) {
            valor /= 1024
            indice++
        }
        val texto = if (valor < 10) String.format(Locale("pt", "BR"), "%.1f", valor) else valor.toLong().toString()
        return "$texto ${unidades[indice]}"
    }

    /** Arquivos de `refs-das-receitas/` que nenhuma receita viva usa. */
    fun refsDeReceitaSemUso(arquivosNaPasta: Set<String>, receitasVivas: List<ReceitaDeGeracaoMcp>): Set<String> =
        arquivosNaPasta - ReceitaDeGeracaoMcp.arquivosEmUso(receitasVivas)

    /** Receitas que nenhum card nem item da biblioteca aponta mais. */
    fun receitasOrfas(idsDasReceitas: Set<String>, idsVivos: Set<String>): Set<String> = idsDasReceitas - idsVivos

    /** Refs do formato antigo (`refs-de-geracao-da-biblioteca/`) que nenhum item da biblioteca lista. */
    fun refsAntigasSemUso(arquivosNaPasta: Set<String>, refsListadasPelosItens: Set<String>): Set<String> =
        arquivosNaPasta - refsListadasPelosItens.map { it.substringAfterLast('/').substringAfterLast('\\') }.toSet()

    /** Para a limpeza automática: só mexe no que está parado há mais de [dias]. */
    fun antigo(modificadoMs: Long, agoraMs: Long, dias: Int): Boolean =
        dias > 0 && modificadoMs > 0 && agoraMs - modificadoMs > dias * 24L * 60 * 60 * 1000

    /** Limpeza automática roda no máximo uma vez por dia. */
    fun deveRodarLimpezaAutomatica(dias: Int, ultimaMs: Long, agoraMs: Long): Boolean =
        dias > 0 && agoraMs - ultimaMs > 24L * 60 * 60 * 1000
}

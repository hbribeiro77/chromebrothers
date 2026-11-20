package com.chromebrothers.navegador.regras

/**
 * Lista de modelos do Magnific (`images_models_list` / `video_models_list`).
 * A resposta vem em texto indentado (`models[N]:` e um `- slug:` por modelo); aqui só lemos
 * os campos que o componente usa.
 */
object CatalogoDeModelosDoMagnific {
    data class Modelo(
        val slug: String,
        val nome: String,
        val duracoes: List<Int> = emptyList(),
        val resolucoes: List<String> = emptyList(),
        val proporcoes: List<String> = emptyList(),
        val aceitaVideoDeReferencia: Boolean = false,
        val aceitaImagemDeReferencia: Boolean = false,
    )

    private val regexCampoLista = Regex("""^ {4}(\w+)\[\d+\]: ?(.*)$""")
    private val regexCampo = Regex("""^ {4}(\w+): ?(.*)$""")

    fun ler(texto: String): List<Modelo> {
        val blocos = texto.replace("\r", "").split(Regex("""\n {2}- slug: """)).drop(1)
        return blocos.mapNotNull { bloco ->
            val linhas = bloco.split('\n')
            val slug = linhas.first().trim().trim('"')
            if (slug.isEmpty()) return@mapNotNull null
            var nome = slug
            var duracoes = emptyList<Int>()
            var resolucoes = emptyList<String>()
            var proporcoes = emptyList<String>()
            for (linha in linhas.drop(1)) {
                regexCampoLista.find(linha)?.let { m ->
                    val valores = m.groupValues[2].split(',').map { it.trim().trim('"') }.filter { it.isNotEmpty() }
                    when (m.groupValues[1]) {
                        "durations" -> duracoes = valores.mapNotNull { it.toDoubleOrNull()?.toInt() }
                        "resolutions" -> resolucoes = valores
                        "aspectRatios" -> proporcoes = valores
                    }
                    return@let
                } ?: regexCampo.find(linha)?.let { m ->
                    if (m.groupValues[1] == "name") nome = m.groupValues[2].trim().trim('"').ifBlank { slug }
                }
            }
            Modelo(slug, nome, duracoes, resolucoes, proporcoes, aceita(bloco, "video"), aceita(bloco, "image"))
        }
    }

    /** `references[]` com `- type: <tipo>` seguido de `allowed: true` (com `assetType` opcional no meio). */
    private fun aceita(bloco: String, tipo: String): Boolean =
        Regex("""- type: $tipo\n(\s+assetType: \w+\n)?\s+allowed: true""").containsMatchIn(bloco)

    private val VIDEO_DE_FORA = listOf("motion-control", "edit", "animate", "act-two", "lipsync", "live2d", "veed-")

    /** Vídeo: só modelos de geração comum (com durações); fora edição, lip-sync, controle de movimento etc. */
    fun serveParaVideo(m: Modelo): Boolean =
        m.duracoes.isNotEmpty() && VIDEO_DE_FORA.none { m.slug.lowercase().contains(it) }

    /** `auto` deixa o Magnific escolher; o componente sempre manda um modelo explícito. */
    fun serveParaImagem(m: Modelo): Boolean = m.slug != "auto" && !m.slug.contains("vector")

    /** JSON da ponte com os detalhes que o ajuste do pedido usa depois (guardados no cache). */
    fun jsonParaPonte(modelos: List<Modelo>): String {
        val itens = modelos.joinToString(",") { m ->
            fun lista(v: List<String>) = v.joinToString(",") { "\"${TextoJsonDoChat.escapar(it)}\"" }
            """{"id":"${TextoJsonDoChat.escapar(m.slug)}","nome":"${TextoJsonDoChat.escapar(m.nome)}",""" +
                """"duracoes":[${m.duracoes.joinToString(",")}],"resolucoes":[${lista(m.resolucoes)}],""" +
                """"proporcoes":[${lista(m.proporcoes)}],"aceitaVideo":${m.aceitaVideoDeReferencia},"aceitaImagem":${m.aceitaImagemDeReferencia}}"""
        }
        return """{"ok":true,"itens":[$itens]}"""
    }
}

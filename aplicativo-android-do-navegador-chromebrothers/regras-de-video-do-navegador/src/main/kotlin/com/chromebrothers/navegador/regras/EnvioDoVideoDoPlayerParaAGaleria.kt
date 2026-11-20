package com.chromebrothers.navegador.regras

/** O que o botão "Enviar para a Galeria" do player faz com o vídeo que está tocando. */
enum class SituacaoDoVideoParaAGaleria {
    /** Vídeo de site (http/https): baixa. */
    BAIXAR_DO_SITE,
    /** Gerado via MCP, guardado dentro do app: vai para Movies/ChromeBrothers. */
    MOVER_DA_PASTA_DO_APP,
    /** Já está numa pasta que a Galeria mostra como vídeo (Movies, DCIM). */
    JA_NA_GALERIA,
    /** Já está no aparelho, noutra pasta pública (ex.: Downloads/ChromeBrothers). */
    JA_NO_APARELHO,
    /** blob:, data: e afins: o player não tem um arquivo para enviar. */
    NAO_SUPORTADO,
}

object EnvioDoVideoDoPlayerParaAGaleria {
    /** @param caminhoRelativo RELATIVE_PATH do MediaStore quando o endereço é content:// (null se não deu para ler). */
    fun situacao(endereco: String, caminhoRelativo: String?): SituacaoDoVideoParaAGaleria {
        val limpo = endereco.trim()
        val esquema = limpo.substringBefore(':', "").lowercase()
        return when {
            esquema == "http" || esquema == "https" -> SituacaoDoVideoParaAGaleria.BAIXAR_DO_SITE
            esquema == "file" && MovimentoDaMidiaGeradaParaAGaleria.podeMover(limpo) ->
                SituacaoDoVideoParaAGaleria.MOVER_DA_PASTA_DO_APP
            esquema == "content" || esquema == "file" -> {
                val caminho = caminhoRelativo.orEmpty().replace('\\', '/').trimStart('/')
                if (caminho.startsWith("Movies/", ignoreCase = true) || caminho.startsWith("DCIM/", ignoreCase = true)) {
                    SituacaoDoVideoParaAGaleria.JA_NA_GALERIA
                } else {
                    SituacaoDoVideoParaAGaleria.JA_NO_APARELHO
                }
            }
            else -> SituacaoDoVideoParaAGaleria.NAO_SUPORTADO
        }
    }

    fun aviso(situacao: SituacaoDoVideoParaAGaleria, caminhoRelativo: String?): String? {
        val pasta = caminhoRelativo?.replace('\\', '/')?.trim('/')?.takeIf { it.isNotBlank() }
        return when (situacao) {
            SituacaoDoVideoParaAGaleria.JA_NA_GALERIA ->
                "Este vídeo já está na Galeria" + (pasta?.let { " ($it)" } ?: "") + "."
            SituacaoDoVideoParaAGaleria.JA_NO_APARELHO ->
                "Este vídeo já está salvo no aparelho" + (pasta?.let { ", em $it" } ?: "") + " — ele aparece na Galeria."
            SituacaoDoVideoParaAGaleria.NAO_SUPORTADO ->
                "Este vídeo não tem um arquivo que dê para salvar."
            SituacaoDoVideoParaAGaleria.BAIXAR_DO_SITE,
            SituacaoDoVideoParaAGaleria.MOVER_DA_PASTA_DO_APP,
            -> null
        }
    }
}

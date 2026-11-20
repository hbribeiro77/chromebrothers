package com.chromebrothers.navegador.regras

/**
 * Placeholders `{{nome}}` em valores de passos (preencher, abrir, etc.).
 * Nomes: letra/underscore no início, depois alfanumérico/underscore.
 */
object VariaveisDoRoteiroDeAutomacaoDaPagina {
    private val PADRAO = Regex("""\{\{\s*([A-Za-z_][A-Za-z0-9_]*)\s*\}\}""")

    fun nomesEm(texto: String): List<String> =
        PADRAO.findAll(texto).map { it.groupValues[1] }.distinct().toList()

    fun nomesNosPassos(passos: List<PassoDoRoteiroDeAutomacao>): List<String> {
        val vistos = linkedSetOf<String>()
        passos.forEach { passo ->
            when (passo) {
                is PassoDoRoteiroDeAutomacao.Abrir -> vistos += nomesEm(passo.url)
                is PassoDoRoteiroDeAutomacao.Preencher -> vistos += nomesEm(passo.valor)
                is PassoDoRoteiroDeAutomacao.AguardarUrl -> vistos += nomesEm(passo.contem)
                is PassoDoRoteiroDeAutomacao.Assertar -> {
                    vistos += nomesEm(passo.texto)
                    vistos += nomesEm(passo.contem)
                }
                is PassoDoRoteiroDeAutomacao.Extrair -> Unit
                is PassoDoRoteiroDeAutomacao.IrPara -> vistos += nomesEm(passo.se)
                is PassoDoRoteiroDeAutomacao.Aguardar,
                is PassoDoRoteiroDeAutomacao.AguardarSumir,
                is PassoDoRoteiroDeAutomacao.AguardarQualquer,
                is PassoDoRoteiroDeAutomacao.Marcar,
                is PassoDoRoteiroDeAutomacao.Clicar,
                is PassoDoRoteiroDeAutomacao.EsperarMs,
                is PassoDoRoteiroDeAutomacao.Rolar,
                is PassoDoRoteiroDeAutomacao.ClicarSe -> Unit
            }
        }
        return vistos.toList()
    }

    fun substituir(texto: String, variaveis: Map<String, String>): String {
        if (texto.isEmpty() || !texto.contains("{{")) return texto
        return PADRAO.replace(texto) { match ->
            val nome = match.groupValues[1]
            variaveis[nome] ?: match.value
        }
    }

    fun faltando(texto: String, variaveis: Map<String, String>): List<String> =
        nomesEm(texto).filter { !variaveis.containsKey(it) }
}

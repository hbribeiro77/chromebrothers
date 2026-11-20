package com.chromebrothers.navegador.regras

/**
 * Texto curto e prescritivo para a IA montar ramificação A/B no DSL.
 */
object ReceitaDoFluxoCondicionalDoRoteiroDeAutomacaoParaOChat {
    const val RESUMO: String =
        "Fluxo condicional (A ou B): NÃO use clicar_se para isso. Use aguardar_qualquer → grava id em {{_ramo}} → ir_para com se → marcar como destino → no fim de cada ramo ir_para o marcar fim."

    fun receita(): String = """
$RESUMO
Ordem obrigatória dos passos:
1) abrir / aguardar o que for comum
2) {"acao":"aguardar_qualquer","timeout_ms":15000,"opcoes":[{"id":"livre","texto":"Generate"},{"id":"creditos","texto":"credits"}]}
   — 2 a 4 opções; cada uma precisa de id + (texto e/ou seletor); o primeiro que aparecer vence; grava em _ramo (ou "variavel")
3) {"acao":"ir_para","destino":"fluxo_livre","se":"{{_ramo}}=livre"}
4) {"acao":"ir_para","destino":"fluxo_creditos"}
   — sem "se" = salto incondicional (caminho padrão depois do fallthrough)
5) {"acao":"marcar","id":"fluxo_livre"} … passos só do ramo livre …
   {"acao":"ir_para","destino":"fim"}
6) {"acao":"marcar","id":"fluxo_creditos"} … passos só do ramo créditos …
   {"acao":"ir_para","destino":"fim"}
7) {"acao":"marcar","id":"fim"} … passos comuns (assertar/extrair) …

Regras:
- ir_para usa "destino" (id de um marcar) OU "passo" (número 1-based), nunca os dois.
- se a condição do ir_para NÃO bater, o executor SEGUIR o próximo passo (por isso o 2º ir_para costuma ser sem se).
- Não invente if/else aninhado no JSON; não use clicar_se como ramificação de fluxo.
- Antes de inventar um roteiro novo, listar_roteiros_de_automacao + ler_roteiro_de_automacao e copie o padrão.
""".trimIndent()
}

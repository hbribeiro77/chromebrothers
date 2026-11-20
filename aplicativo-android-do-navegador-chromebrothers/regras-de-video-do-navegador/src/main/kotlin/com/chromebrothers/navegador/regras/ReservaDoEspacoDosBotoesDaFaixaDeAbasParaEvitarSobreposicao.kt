package com.chromebrothers.navegador.regras

/**
 * Garante que a rolagem das abas nunca invada o botão "+" nem o contador.
 * Quando as abas cabem, a rolagem encolhe ao conteúdo e o vão antes do
 * contador absorve o resto (contador fica à direita). Quando não cabem,
 * a rolagem usa só o espaço restante (peso 1) e o vão some.
 */
object ReservaDoEspacoDosBotoesDaFaixaDeAbasParaEvitarSobreposicao {

    data class Resultado(
        val larguraDaRolagemPx: Int,
        val pesoDaRolagem: Float,
        val pesoDoVaoAntesDoContador: Float,
    )

    fun larguraReservadaAosBotoesPx(
        paddingStartPx: Int,
        paddingEndPx: Int,
        larguraDoBotaoNovaAbaPx: Int,
        margemStartDoBotaoNovaAbaPx: Int,
        larguraDoContadorPx: Int,
        margemStartDoContadorPx: Int,
        fallbackLarguraDoBotaoNovaAbaPx: Int = 0,
        fallbackLarguraDoContadorPx: Int = 0,
    ): Int {
        val novaAba = if (larguraDoBotaoNovaAbaPx > 0) {
            larguraDoBotaoNovaAbaPx
        } else {
            fallbackLarguraDoBotaoNovaAbaPx.coerceAtLeast(0)
        }
        val contador = if (larguraDoContadorPx > 0) {
            larguraDoContadorPx
        } else {
            fallbackLarguraDoContadorPx.coerceAtLeast(0)
        }
        return paddingStartPx.coerceAtLeast(0) +
            paddingEndPx.coerceAtLeast(0) +
            novaAba +
            margemStartDoBotaoNovaAbaPx.coerceAtLeast(0) +
            contador +
            margemStartDoContadorPx.coerceAtLeast(0)
    }

    fun calcular(
        larguraDaFaixaPx: Int,
        larguraDoConteudoDasAbasPx: Int,
        larguraReservadaAosBotoesPx: Int,
    ): Resultado {
        if (larguraDaFaixaPx <= 0 || larguraDoConteudoDasAbasPx <= 0) {
            return Resultado(
                larguraDaRolagemPx = 0,
                pesoDaRolagem = 1f,
                pesoDoVaoAntesDoContador = 0f,
            )
        }
        val limite = (larguraDaFaixaPx - larguraReservadaAosBotoesPx.coerceAtLeast(0))
            .coerceAtLeast(0)
        if (larguraDoConteudoDasAbasPx in 1..limite) {
            return Resultado(
                larguraDaRolagemPx = larguraDoConteudoDasAbasPx.coerceAtMost(limite),
                pesoDaRolagem = 0f,
                pesoDoVaoAntesDoContador = 1f,
            )
        }
        return Resultado(
            larguraDaRolagemPx = 0,
            pesoDaRolagem = 1f,
            pesoDoVaoAntesDoContador = 0f,
        )
    }
}

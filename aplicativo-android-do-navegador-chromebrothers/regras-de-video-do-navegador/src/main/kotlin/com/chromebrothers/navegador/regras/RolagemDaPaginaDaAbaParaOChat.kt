package com.chromebrothers.navegador.regras

sealed class PedidoDeRolagemDaPagina {
    data class PorSeletor(val seletor: String) : PedidoDeRolagemDaPagina()
    data class Absoluto(val y: Int) : PedidoDeRolagemDaPagina()
    data class PorPaginas(val paginas: Double) : PedidoDeRolagemDaPagina()
    data class Relativo(val pixels: Int) : PedidoDeRolagemDaPagina()
}

sealed class ResultadoDoPedidoDeRolagem {
    data class Ok(val pedido: PedidoDeRolagemDaPagina) : ResultadoDoPedidoDeRolagem()
    data class Erro(val mensagem: String) : ResultadoDoPedidoDeRolagem()
}

object RolagemDaPaginaDaAbaParaOChat {
    const val LIMITE_DO_SELETOR = 300
    private const val MARGEM_DO_FIM_EM_PIXELS = 2

    /** Prioridade: seletor > y > paginas > pixels. */
    fun lerPedido(argumentos: String): ResultadoDoPedidoDeRolagem {
        val seletor = TextoJsonDoChat.valorDaChave(argumentos, "seletor")?.trim().orEmpty()
        if (seletor.isNotEmpty()) {
            if (seletor.length > LIMITE_DO_SELETOR) {
                return ResultadoDoPedidoDeRolagem.Erro("Seletor CSS muito longo (máximo $LIMITE_DO_SELETOR caracteres).")
            }
            return ResultadoDoPedidoDeRolagem.Ok(PedidoDeRolagemDaPagina.PorSeletor(seletor))
        }
        if (chavePresente(argumentos, "y")) {
            val y = inteiro(argumentos, "y")
                ?: return ResultadoDoPedidoDeRolagem.Erro("O valor de y precisa ser um número.")
            return ResultadoDoPedidoDeRolagem.Ok(PedidoDeRolagemDaPagina.Absoluto(y.coerceAtLeast(0)))
        }
        if (chavePresente(argumentos, "paginas")) {
            val paginas = TextoJsonDoChat.numeroDaChave(argumentos, "paginas")?.takeIf { it.isFinite() }
                ?: return ResultadoDoPedidoDeRolagem.Erro("O valor de paginas precisa ser um número.")
            return ResultadoDoPedidoDeRolagem.Ok(PedidoDeRolagemDaPagina.PorPaginas(paginas))
        }
        if (chavePresente(argumentos, "pixels")) {
            val pixels = inteiro(argumentos, "pixels")
                ?: return ResultadoDoPedidoDeRolagem.Erro("O valor de pixels precisa ser um número.")
            return ResultadoDoPedidoDeRolagem.Ok(PedidoDeRolagemDaPagina.Relativo(pixels))
        }
        return ResultadoDoPedidoDeRolagem.Erro("Informe seletor, y, paginas ou pixels para rolar a página.")
    }

    /** A chave existe com valor diferente de null (mesmo que o valor seja inválido). */
    private fun chavePresente(argumentos: String, chave: String): Boolean =
        Regex("\"" + Regex.escape(chave) + "\"\\s*:\\s*(?!null)\\S").containsMatchIn(argumentos)

    private fun inteiro(argumentos: String, chave: String): Int? =
        TextoJsonDoChat.inteiroDaChave(argumentos, chave)
            ?: TextoJsonDoChat.numeroDaChave(argumentos, chave)?.takeIf { it.isFinite() }?.toInt()

    fun mensagem(bruto: String): String {
        val linha = bruto.lines().firstOrNull { it.isNotBlank() }
            ?: return "Não foi possível rolar a página (resposta vazia)."
        val partes = linha.split('\t')
        if (partes.firstOrNull() == "erro") {
            val motivo = partes.getOrNull(1)?.trim().orEmpty().ifBlank { "erro desconhecido" }
            return "Erro ao rolar a página: $motivo"
        }
        if (partes.firstOrNull() != "ok") return "Não foi possível rolar a página (resposta inesperada)."
        val y = partes.getOrNull(1)?.trim()?.toIntOrNull()
        val altura = partes.getOrNull(2)?.trim()?.toIntOrNull()
        val viewport = partes.getOrNull(3)?.trim()?.toIntOrNull()
        if (y == null || altura == null || viewport == null) {
            return "Não foi possível rolar a página (resposta inesperada)."
        }
        val noFim = if (y + viewport >= altura - MARGEM_DO_FIM_EM_PIXELS) "sim" else "nao"
        return "Página rolada: y=$y, altura_pagina=$altura, altura_viewport=$viewport, no_fim=$noFim"
    }

    fun script(pedido: PedidoDeRolagemDaPagina): String {
        val acao = when (pedido) {
            is PedidoDeRolagemDaPagina.PorSeletor -> {
                val seletorJs = "\"" + TextoJsonDoChat.escapar(pedido.seletor) + "\""
                """
                var alvo;
                try {
                  alvo = document.querySelector($seletorJs);
                } catch (erro) {
                  return "erro\tSeletor CSS inválido.";
                }
                if (!alvo) return "erro\tElemento não encontrado.";
                alvo.scrollIntoView({ block: "center", inline: "nearest", behavior: "instant" });
                """
            }
            is PedidoDeRolagemDaPagina.Absoluto ->
                "window.scrollTo({ top: ${pedido.y}, left: window.scrollX, behavior: \"instant\" });"
            is PedidoDeRolagemDaPagina.PorPaginas ->
                "window.scrollBy({ top: Math.round(${pedido.paginas} * window.innerHeight), left: 0, behavior: \"instant\" });"
            is PedidoDeRolagemDaPagina.Relativo ->
                "window.scrollBy({ top: ${pedido.pixels}, left: 0, behavior: \"instant\" });"
        }
        return """
        (function () {
          try {
            $acao
            var raiz = document.documentElement || {};
            var corpo = document.body || {};
            var y = Math.round(window.scrollY || window.pageYOffset || raiz.scrollTop || 0);
            var altura = Math.round(Math.max(raiz.scrollHeight || 0, corpo.scrollHeight || 0));
            var viewport = Math.round(window.innerHeight || raiz.clientHeight || 0);
            return "ok\t" + y + "\t" + altura + "\t" + viewport;
          } catch (erro) {
            return "erro\tNão deu para rolar a página.";
          }
        })()
        """.trimIndent()
    }
}

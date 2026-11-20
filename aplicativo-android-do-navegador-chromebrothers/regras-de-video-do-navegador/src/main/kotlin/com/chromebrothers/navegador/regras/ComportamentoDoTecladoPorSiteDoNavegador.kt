package com.chromebrothers.navegador.regras

/**
 * Override por hospedeiro do teclado.
 * Automático: pan só o necessário (medida do foco) + setVerticalClipping;
 * se a medida falhar, sobe a folga (ChatGPT e a maioria dos sites) — exceto X/Twitter,
 * onde o pan cego esconde a busca no topo.
 * Sempre: translationY cheio. Nunca: nenhum dos dois.
 * Sem entrada no mapa = [ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO].
 */
data class AjusteVisualDoTecladoNaPaginaDoNavegador(
    val deslocamentoY: Float,
    val clippingInferiorPx: Int,
)

enum class EstadoDaMedidaDoFocoParaOTeclado {
    AGUARDANDO,
    PRONTA,
    FALHOU,
}

object ComportamentoDoTecladoPorSiteDoNavegador {
    fun hospedeiro(endereco: String): String =
        IconeDaPaginaNaBarraDeEndereco.hospedeiro(endereco)

    fun modoPara(
        mapa: Map<String, ModoDoTecladoSobreAPaginaDoNavegador>,
        hospedeiro: String,
    ): ModoDoTecladoSobreAPaginaDoNavegador {
        if (hospedeiro.isBlank()) return ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO
        return mapa[hospedeiro] ?: ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO
    }

    fun guardar(
        mapa: Map<String, ModoDoTecladoSobreAPaginaDoNavegador>,
        hospedeiro: String,
        modo: ModoDoTecladoSobreAPaginaDoNavegador,
    ): Map<String, ModoDoTecladoSobreAPaginaDoNavegador> {
        if (hospedeiro.isBlank()) return mapa
        val atual = mapa.toMutableMap()
        if (modo == ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO) {
            atual.remove(hospedeiro)
        } else {
            atual[hospedeiro] = modo
        }
        return atual
    }

    fun gravar(mapa: Map<String, ModoDoTecladoSobreAPaginaDoNavegador>): String {
        if (mapa.isEmpty()) return ""
        return mapa.entries
            .sortedBy { it.key }
            .joinToString("\n") { (host, modo) ->
                "${escapar(host)}\t${ModoDoTecladoSobreAPaginaDoNavegador.idDe(modo)}"
            }
    }

    fun ler(bruto: String): Map<String, ModoDoTecladoSobreAPaginaDoNavegador> {
        if (bruto.isBlank()) return emptyMap()
        val saida = linkedMapOf<String, ModoDoTecladoSobreAPaginaDoNavegador>()
        bruto.lineSequence().forEach { linha ->
            val partes = linha.split('\t')
            if (partes.size < 2) return@forEach
            val host = desescapar(partes[0]).trim().lowercase().removePrefix("www.")
            if (host.isBlank()) return@forEach
            val modo = ModoDoTecladoSobreAPaginaDoNavegador.peloId(partes[1])
            if (modo == ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO) return@forEach
            saida[host] = modo
        }
        return saida
    }

    /** Pan cego no fallback quebra busca/composer no topo nesses hosts. */
    fun evitaPanCegoQuandoMedidaFalhar(hospedeiro: String): Boolean {
        val host = hospedeiro.trim().lowercase().removePrefix("www.")
        return host == "x.com" ||
            host == "twitter.com" ||
            host == "mobile.twitter.com" ||
            host.endsWith(".x.com") ||
            host.endsWith(".twitter.com")
    }

    fun ajusteVisual(
        modo: ModoDoTecladoSobreAPaginaDoNavegador,
        folgaDoTecladoPx: Float,
        deslocamentoAutomatico: Float = 0f,
        estadoDaMedidaDoFoco: EstadoDaMedidaDoFocoParaOTeclado =
            EstadoDaMedidaDoFocoParaOTeclado.AGUARDANDO,
        hospedeiro: String = "",
        focoNoChromeNativo: Boolean = false,
    ): AjusteVisualDoTecladoNaPaginaDoNavegador {
        val folga = folgaDoTecladoPx.coerceAtLeast(0f)
        val folgaInt = folga.toInt()
        // Foco na barra/omnibox: a página não sobe (campo já está no chrome); só clipping do IME.
        if (focoNoChromeNativo && modo != ModoDoTecladoSobreAPaginaDoNavegador.NAO_DESLOCAR) {
            return AjusteVisualDoTecladoNaPaginaDoNavegador(
                deslocamentoY = 0f,
                clippingInferiorPx = folgaInt,
            )
        }
        return when (modo) {
            ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO -> {
                val pan = when (estadoDaMedidaDoFoco) {
                    EstadoDaMedidaDoFocoParaOTeclado.PRONTA -> deslocamentoAutomatico
                    EstadoDaMedidaDoFocoParaOTeclado.AGUARDANDO -> 0f
                    EstadoDaMedidaDoFocoParaOTeclado.FALHOU -> when {
                        folga == 0f -> 0f
                        evitaPanCegoQuandoMedidaFalhar(hospedeiro) -> 0f
                        else -> -folga
                    }
                }
                AjusteVisualDoTecladoNaPaginaDoNavegador(
                    deslocamentoY = pan,
                    clippingInferiorPx = folgaInt,
                )
            }
            ModoDoTecladoSobreAPaginaDoNavegador.SEMPRE_DESLOCAR ->
                AjusteVisualDoTecladoNaPaginaDoNavegador(
                    deslocamentoY = if (folga == 0f) 0f else -folga,
                    clippingInferiorPx = 0,
                )
            ModoDoTecladoSobreAPaginaDoNavegador.NAO_DESLOCAR ->
                AjusteVisualDoTecladoNaPaginaDoNavegador(
                    deslocamentoY = 0f,
                    clippingInferiorPx = 0,
                )
        }
    }

    private fun escapar(texto: String): String =
        texto.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n")

    private fun desescapar(texto: String): String {
        val saida = StringBuilder()
        var i = 0
        while (i < texto.length) {
            val c = texto[i]
            if (c == '\\' && i + 1 < texto.length) {
                when (texto[i + 1]) {
                    't' -> { saida.append('\t'); i += 2; continue }
                    'n' -> { saida.append('\n'); i += 2; continue }
                    '\\' -> { saida.append('\\'); i += 2; continue }
                }
            }
            saida.append(c)
            i += 1
        }
        return saida.toString()
    }
}

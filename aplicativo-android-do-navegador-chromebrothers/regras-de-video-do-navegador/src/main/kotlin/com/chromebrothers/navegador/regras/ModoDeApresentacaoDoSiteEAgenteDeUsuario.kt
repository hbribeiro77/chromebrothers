package com.chromebrothers.navegador.regras

enum class ModoDeApresentacaoDoSite {
    TABLET,
    CELULAR,
    COMPUTADOR,
    ;

    companion object {
        fun peloId(id: String): ModoDeApresentacaoDoSite {
            return when (id) {
                "celular" -> CELULAR
                "tablet" -> TABLET
                else -> COMPUTADOR
            }
        }

        fun idDe(modo: ModoDeApresentacaoDoSite): String {
            return when (modo) {
                TABLET -> "tablet"
                CELULAR -> "celular"
                COMPUTADOR -> "computador"
            }
        }
    }
}

object AgenteDeUsuarioDoNavegadorChromebrothers {
    fun montar(modo: ModoDeApresentacaoDoSite, agenteOriginal: String): String {
        val semMarcaDeWebView = agenteOriginal.replace("; wv", "").replace(" Version/4.0", "")
        return when (modo) {
            ModoDeApresentacaoDoSite.CELULAR -> semMarcaDeWebView
            ModoDeApresentacaoDoSite.TABLET -> semMarcaDeWebView.replace(" Mobile", "")
            ModoDeApresentacaoDoSite.COMPUTADOR -> {
                val versao = Regex("""Chrome/([\d.]+)""").find(semMarcaDeWebView)?.groupValues?.get(1) ?: "131.0.0.0"
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$versao Safari/537.36"
            }
        }
    }

    fun encolherParaCaberNaTela(modo: ModoDeApresentacaoDoSite): Boolean {
        return modo == ModoDeApresentacaoDoSite.COMPUTADOR
    }
}

object AlternanciaDaVersaoParaComputadorDaAba {
    fun proximo(
        efetivo: ModoDeApresentacaoDoSite,
        padrao: ModoDeApresentacaoDoSite,
    ): ModoDeApresentacaoDoSite {
        if (efetivo != ModoDeApresentacaoDoSite.COMPUTADOR) return ModoDeApresentacaoDoSite.COMPUTADOR
        if (padrao == ModoDeApresentacaoDoSite.COMPUTADOR) return ModoDeApresentacaoDoSite.TABLET
        return padrao
    }
}

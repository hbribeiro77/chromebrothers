package com.chromebrothers.navegador.regras

/**
 * "Aprovar e executar" de um plano do modo chat: as ações citadas no plano seguem sem cartão
 * de confirmação durante a resposta que executa o plano, e só nos sites da aba no momento da
 * aprovação ou citados no plano. Fora disso, o cartão volta.
 *
 * Nunca entram na aprovação: geração com créditos, apagar componente e guardar nota
 * (o prompt trata a nota como palavra do usuário).
 */
data class AprovacaoDoPlanoDoChat(
    val acoes: Set<String>,
    val sites: Set<String>,
) {
    fun cobre(nome: String, pedido: PedidoDeAcaoConfirmadaDoChat, siteDaAba: String): Boolean {
        if (nome !in acoes) return false
        val hospedeiro = LiberacaoDeNavegacaoDosSitesParaOChat.hospedeiroDaAcao(pedido, siteDaAba)
        return hospedeiro.isEmpty() || hospedeiro in sites
    }

    companion object {
        val semAprovacao: Set<String> = setOf(
            AcoesConfirmadasDoChatDaOpenRouter.GERAR_IMAGEM_HIGGSFIELD,
            AcoesConfirmadasDoChatDaOpenRouter.GERAR_VIDEO_HIGGSFIELD,
            AcoesConfirmadasDoChatDaOpenRouter.GERAR_VIDEO_TOPVIEW,
            AcoesConfirmadasDoChatDaOpenRouter.GERAR_VIDEO_VIVIX,
            AcoesConfirmadasDoChatDaOpenRouter.APAGAR_COMPONENTE,
            AcoesConfirmadasDoChatDaOpenRouter.GUARDAR_NOTA,
        )

        private val endereco = Regex("https?://([A-Za-z0-9.-]+)")

        /** Lê as ferramentas e os sites citados no plano. */
        fun doPlano(plano: String, siteDaAba: String): AprovacaoDoPlanoDoChat {
            val acoes = AcoesConfirmadasDoChatDaOpenRouter.nomes
                .filter { it !in semAprovacao && Regex("\\b${Regex.escape(it)}\\b").containsMatchIn(plano) }
                .toSet()
            val sites = (endereco.findAll(plano).map { it.groupValues[1] } + siteDaAba)
                .map { LiberacaoDeNavegacaoDosSitesParaOChat.chave(it) }
                .filter { it.isNotEmpty() }
                .toSet()
            return AprovacaoDoPlanoDoChat(acoes, sites)
        }

        /** Ações citadas no plano que vão continuar pedindo confirmação. */
        fun aindaPedem(plano: String): List<String> = semAprovacao.filter {
            Regex("\\b${Regex.escape(it)}\\b").containsMatchIn(plano)
        }
    }
}

package com.chromebrothers.navegador.regras

/**
 * Estado das abas na gaveta do chat (Chat + Gravador sempre + N componentes).
 * [Estado.gravadorAberto] = sessão/painel do gravador montado no host (a aba existe sempre).
 */
object AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador {
    const val ID_ABA_CHAT = "chat"
    const val ID_ABA_GRAVADOR = "gravador"
    /** Abas de componente no chat, além das fixas (Chat, Gravador). Independe das janelas flutuantes. */
    const val MAXIMO_DE_COMPONENTES = 5

    data class Aba(val id: String, val titulo: String)

    data class Estado(
        val componentes: List<Aba> = emptyList(),
        val idAtiva: String = ID_ABA_CHAT,
        val gravadorAberto: Boolean = false,
    )

    sealed class Resultado {
        data class Ok(val estado: Estado) : Resultado()
        data class Recusado(val mensagem: String) : Resultado()
    }

    fun podeAncorar(quantidadeAtual: Int): Boolean =
        quantidadeAtual < MAXIMO_DE_COMPONENTES

    fun mensagemLimite(): String =
        "Já há $MAXIMO_DE_COMPONENTES componentes ancorados na gaveta. Desvincule um para ancorar outro."

    /** Faixa sempre presente (Chat + Gravador no mínimo). */
    fun deveMostrarFaixa(estado: Estado): Boolean = true

    fun abasVisiveis(estado: Estado, tituloGravador: String = "Gravador"): List<Aba> {
        val lista = mutableListOf(
            Aba(ID_ABA_CHAT, "Chat"),
            Aba(ID_ABA_GRAVADOR, tituloGravador),
        )
        lista.addAll(estado.componentes)
        return lista
    }

    fun abrirGravador(estado: Estado): Estado =
        estado.copy(gravadorAberto = true, idAtiva = ID_ABA_GRAVADOR)

    /** Encerra a sessão; a aba Gravador permanece. Se estava ativa, volta ao Chat. */
    fun fecharGravador(estado: Estado): Estado {
        if (!estado.gravadorAberto) {
            return if (estado.idAtiva == ID_ABA_GRAVADOR) {
                estado.copy(idAtiva = ID_ABA_CHAT)
            } else {
                estado
            }
        }
        val ativa = if (estado.idAtiva == ID_ABA_GRAVADOR) ID_ABA_CHAT else estado.idAtiva
        return estado.copy(gravadorAberto = false, idAtiva = ativa)
    }

    /**
     * [ativar]=false: a aba entra no chat sem ir para a frente (componentes que abrem junto
     * com o chat não devem tirar o foco da aba do Chat).
     */
    fun ancorar(estado: Estado, id: String, titulo: String, ativar: Boolean = true): Resultado {
        if (id.isBlank() || id == ID_ABA_CHAT || id == ID_ABA_GRAVADOR) {
            return Resultado.Recusado("Componente inválido.")
        }
        if (estado.componentes.any { it.id == id }) {
            return Resultado.Ok(if (ativar) estado.copy(idAtiva = id) else estado)
        }
        if (!podeAncorar(estado.componentes.size)) {
            return Resultado.Recusado(mensagemLimite())
        }
        val tituloLimpo = titulo.trim().ifBlank { "Componente" }
        return Resultado.Ok(
            Estado(
                componentes = estado.componentes + Aba(id, tituloLimpo),
                idAtiva = if (ativar) id else estado.idAtiva,
                gravadorAberto = estado.gravadorAberto,
            ),
        )
    }

    fun desancorar(estado: Estado, id: String): Resultado {
        if (estado.componentes.none { it.id == id }) {
            return Resultado.Recusado("Esse componente não está ancorado.")
        }
        val resto = estado.componentes.filterNot { it.id == id }
        val ativa = if (estado.idAtiva == id) ID_ABA_CHAT else estado.idAtiva
        return Resultado.Ok(
            estado.copy(componentes = resto, idAtiva = ativa),
        )
    }

    fun ativar(estado: Estado, id: String): Resultado {
        when {
            id == ID_ABA_CHAT || id == ID_ABA_GRAVADOR -> Unit
            estado.componentes.none { it.id == id } -> {
                return Resultado.Recusado("Aba inexistente.")
            }
        }
        return Resultado.Ok(estado.copy(idAtiva = id))
    }

    /** Swipe passa por todas as abas: chegar no Gravador sem sessão abre a sessão, como o toque. */
    fun idsDoSwipe(estado: Estado): List<String> =
        abasVisiveis(estado).map { it.id }

    /** Fantasma: o Gravador sem sessão fica de fora (pairar não deve abrir sessão no meio do arraste). */
    fun idsNavegaveis(estado: Estado): List<String> =
        abasVisiveis(estado).map { it.id }
            .filter { it != ID_ABA_GRAVADOR || estado.gravadorAberto }

    /** Reordena só entre os componentes (Chat e Gravador ficam sempre na frente). */
    fun moverComponente(estado: Estado, id: String, novoIndice: Int): Estado {
        val atual = estado.componentes.indexOfFirst { it.id == id }
        if (atual < 0) return estado
        val destino = novoIndice.coerceIn(0, estado.componentes.lastIndex)
        if (destino == atual) return estado
        val lista = estado.componentes.toMutableList()
        val aba = lista.removeAt(atual)
        lista.add(destino, aba)
        return estado.copy(componentes = lista)
    }

    fun ehAbaDeComponente(id: String): Boolean =
        id != ID_ABA_CHAT && id != ID_ABA_GRAVADOR

    /** Precisa abrir a sessão ao focar a aba Gravador. */
    fun precisaAbrirSessaoDoGravador(estado: Estado, idPedida: String): Boolean =
        idPedida == ID_ABA_GRAVADOR && !estado.gravadorAberto
}

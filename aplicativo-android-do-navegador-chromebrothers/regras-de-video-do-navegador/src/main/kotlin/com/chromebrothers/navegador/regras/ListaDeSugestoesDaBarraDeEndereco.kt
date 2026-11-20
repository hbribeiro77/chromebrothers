package com.chromebrothers.navegador.regras

enum class TipoDaSugestaoDaBarraDeEndereco {
    BUSCA,
    ENDERECO,
    PAGINA,

    /** Sugestão de pesquisa vinda do motor de busca (linha com lupa no Chrome). */
    SUGESTAO_DE_BUSCA,
}

data class SugestaoDaBarraDeEndereco(
    val tipo: TipoDaSugestaoDaBarraDeEndereco,
    val titulo: String,
    val endereco: String,
)

object ListaDeSugestoesDaBarraDeEndereco {
    const val LIMITE_DE_PAGINAS = 6

    fun montar(
        digitado: String,
        visitas: List<VisitaDePaginaNoNavegador>,
        motor: MotorDeBuscaDaBarraDeEndereco = MotorDeBuscaDaBarraDeEndereco.GOOGLE,
        limiteDePaginas: Int = LIMITE_DE_PAGINAS,
    ): List<SugestaoDaBarraDeEndereco> {
        val texto = digitado.trim()
        if (texto.isEmpty()) return emptyList()
        val destino = InterpretacaoDoTextoDaBarraDeEndereco.resolver(texto, motor)
        if (destino.isEmpty()) return emptyList()
        val cauda = if (destino.startsWith(motor.prefixo)) {
            SugestaoDaBarraDeEndereco(TipoDaSugestaoDaBarraDeEndereco.BUSCA, texto, destino)
        } else {
            SugestaoDaBarraDeEndereco(TipoDaSugestaoDaBarraDeEndereco.ENDERECO, destino, destino)
        }
        val consulta = texto.lowercase()
        val paginas = visitas
            .mapIndexedNotNull { indice, visita ->
                if (visita.endereco.equals(cauda.endereco, ignoreCase = true)) return@mapIndexedNotNull null
                val pontos = pontuarPagina(consulta, visita)
                if (pontos <= 0) null else Triple(pontos, indice, visita)
            }
            .sortedWith(
                compareByDescending<Triple<Int, Int, VisitaDePaginaNoNavegador>> { it.first }
                    .thenBy { it.second },
            )
            .take(limiteDePaginas)
            .map { (_, _, visita) ->
                SugestaoDaBarraDeEndereco(
                    TipoDaSugestaoDaBarraDeEndereco.PAGINA,
                    visita.titulo,
                    visita.endereco,
                )
            }
        // Histórico ranqueado primeiro; busca/endereco digitado sempre por último (estilo Chrome).
        return paginas + cauda
    }

    /** Maior = melhor. Prefixo do host vence (ex.: "b" → boosty.to). */
    fun pontuarPagina(consulta: String, visita: VisitaDePaginaNoNavegador): Int {
        val q = consulta.trim().lowercase()
        if (q.isEmpty()) return 0
        val host = IconeDaPaginaNaBarraDeEndereco.hospedeiro(visita.endereco)
        val titulo = visita.titulo.lowercase()
        val url = visita.endereco.lowercase()
        return when {
            host.startsWith(q) -> 400
            host.contains(q) -> 300
            titulo.startsWith(q) -> 200
            titulo.contains(q) -> 150
            url.contains(q) -> 100
            else -> 0
        }
    }

    const val LIMITE_COMO_CHROME = 8
    private const val PAGINAS_ANTES_DAS_PESQUISAS = 3

    /**
     * Lista no estilo do Chrome:
     * 1. o que o Enter vai fazer (página autocompletada, endereço ou "pesquisar X");
     * 2. até 3 páginas do histórico mais relevantes;
     * 3. sugestões de pesquisa do motor;
     * 4. mais páginas do histórico até completar o limite.
     */
    fun montarComoChrome(
        digitado: String,
        historico: List<EntradaDoHistoricoDoNavegador>,
        pesquisasSugeridas: List<String>,
        motor: MotorDeBuscaDaBarraDeEndereco,
        agoraMs: Long,
        autocompletado: String? = null,
        limite: Int = LIMITE_COMO_CHROME,
    ): List<SugestaoDaBarraDeEndereco> {
        val texto = digitado.trim()
        if (texto.isEmpty()) return emptyList()
        val saida = mutableListOf<SugestaoDaBarraDeEndereco>()
        val usados = mutableSetOf<String>()
        fun chave(endereco: String) = RanqueamentoDoHistoricoNaBarraDeEndereco.enderecoSemEsquema(endereco).removeSuffix("/")
        fun pagina(entrada: EntradaDoHistoricoDoNavegador) =
            SugestaoDaBarraDeEndereco(TipoDaSugestaoDaBarraDeEndereco.PAGINA, entrada.titulo, entrada.endereco)

        // 1) Ação padrão.
        val alvoDoEnter = autocompletado?.takeIf { it.isNotBlank() } ?: texto
        val destino = InterpretacaoDoTextoDaBarraDeEndereco.resolver(alvoDoEnter, motor)
        if (destino.isNotEmpty()) {
            val padrao = when {
                destino.startsWith(motor.prefixo) ->
                    SugestaoDaBarraDeEndereco(TipoDaSugestaoDaBarraDeEndereco.BUSCA, texto, destino)
                else -> {
                    val conhecida = historico.firstOrNull { chave(it.endereco) == chave(destino) }
                    if (conhecida != null) {
                        SugestaoDaBarraDeEndereco(TipoDaSugestaoDaBarraDeEndereco.PAGINA, conhecida.titulo, conhecida.endereco)
                    } else {
                        SugestaoDaBarraDeEndereco(TipoDaSugestaoDaBarraDeEndereco.ENDERECO, destino, destino)
                    }
                }
            }
            saida.add(padrao)
            usados.add(chave(padrao.endereco))
        }

        val paginas = RanqueamentoDoHistoricoNaBarraDeEndereco
            .melhores(texto, historico, agoraMs, limite * 2)
            .filter { usados.add(chave(it.endereco)) }
        val pesquisas = pesquisasSugeridas
            .filter { it.isNotBlank() && !it.equals(texto, ignoreCase = true) }
            .map { termo ->
                SugestaoDaBarraDeEndereco(
                    TipoDaSugestaoDaBarraDeEndereco.SUGESTAO_DE_BUSCA,
                    termo,
                    motor.prefixo + java.net.URLEncoder.encode(termo, "UTF-8"),
                )
            }

        // 2) Algumas páginas, 3) pesquisas, 4) o resto das páginas.
        val primeiras = paginas.take(PAGINAS_ANTES_DAS_PESQUISAS)
        primeiras.forEach { if (saida.size < limite) saida.add(pagina(it)) }
        pesquisas.forEach { if (saida.size < limite) saida.add(it) }
        paginas.drop(primeiras.size).forEach { if (saida.size < limite) saida.add(pagina(it)) }
        // Com autocompletar o Enter abre o site; "pesquisar X" vem logo abaixo, como no Chrome.
        if (autocompletado != null && saida.none { it.tipo == TipoDaSugestaoDaBarraDeEndereco.BUSCA }) {
            val busca = motor.prefixo + java.net.URLEncoder.encode(texto, "UTF-8")
            saida.add(
                minOf(1, saida.size),
                SugestaoDaBarraDeEndereco(TipoDaSugestaoDaBarraDeEndereco.BUSCA, texto, busca),
            )
        }
        return saida.take(limite)
    }

    /** Ao focar a barra vazia: páginas mais usadas e recentes (o Chrome mostra as mais visitadas). */
    fun paginasMaisUsadas(
        historico: List<EntradaDoHistoricoDoNavegador>,
        agoraMs: Long,
        limite: Int = 4,
    ): List<SugestaoDaBarraDeEndereco> = historico
        .sortedByDescending { RanqueamentoDoHistoricoNaBarraDeEndereco.frecencia(it, agoraMs) }
        .take(limite)
        .map { SugestaoDaBarraDeEndereco(TipoDaSugestaoDaBarraDeEndereco.PAGINA, it.titulo, it.endereco) }

    fun paginasRecentes(
        visitas: List<VisitaDePaginaNoNavegador>,
        limite: Int = 4,
    ): List<SugestaoDaBarraDeEndereco> {
        return visitas.take(limite).map { visita ->
            SugestaoDaBarraDeEndereco(TipoDaSugestaoDaBarraDeEndereco.PAGINA, visita.titulo, visita.endereco)
        }
    }
}

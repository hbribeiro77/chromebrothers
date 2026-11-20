package com.chromebrothers.navegador.regras

object MontagemDoPassoDoRoteiroAPartirDaCandidata {
    fun aguardar(
        candidata: CandidataDoElementoParaORoteiro,
        timeoutMs: Int = 10_000,
    ): PassoDoRoteiroDeAutomacao.Aguardar =
        PassoDoRoteiroDeAutomacao.Aguardar(
            seletor = candidata.seletor,
            texto = if (candidata.seletor.isEmpty()) candidata.texto else "",
            timeoutMs = timeoutMs.coerceIn(200, RoteiroDeAutomacaoDaPaginaDoNavegador.TIMEOUT_MAX_MS),
        )

    fun preencher(
        candidata: CandidataDoElementoParaORoteiro,
        valor: String,
    ): PassoDoRoteiroDeAutomacao.Preencher =
        PassoDoRoteiroDeAutomacao.Preencher(
            seletor = candidata.seletor,
            texto = if (candidata.seletor.isEmpty()) candidata.texto else "",
            valor = valor,
        )

    fun clicar(candidata: CandidataDoElementoParaORoteiro): PassoDoRoteiroDeAutomacao.Clicar =
        PassoDoRoteiroDeAutomacao.Clicar(
            seletor = candidata.seletor,
            texto = if (candidata.seletor.isEmpty()) candidata.texto else "",
        )

    fun clicarSe(
        seletorDaLista: String,
        indice: Int,
        contem: String,
        seNao: String = "parar",
    ): PassoDoRoteiroDeAutomacao.ClicarSe =
        PassoDoRoteiroDeAutomacao.ClicarSe(
            seletor = seletorDaLista,
            indice = indice.coerceIn(0, 50),
            contem = contem,
            seNao = if (seNao == "seguir") "seguir" else "parar",
        )

    fun esperarMs(ms: Int): PassoDoRoteiroDeAutomacao.EsperarMs =
        PassoDoRoteiroDeAutomacao.EsperarMs(
            ms.coerceIn(1, RoteiroDeAutomacaoDaPaginaDoNavegador.ESPERA_MAX_MS),
        )

    fun rolar(pixels: Int): PassoDoRoteiroDeAutomacao.Rolar =
        PassoDoRoteiroDeAutomacao.Rolar(pixels = pixels.coerceIn(-20_000, 20_000))

    fun abrir(url: String): PassoDoRoteiroDeAutomacao.Abrir =
        PassoDoRoteiroDeAutomacao.Abrir(url.trim())

    fun aguardarSumir(
        candidata: CandidataDoElementoParaORoteiro,
        timeoutMs: Int = 10_000,
    ): PassoDoRoteiroDeAutomacao.AguardarSumir =
        PassoDoRoteiroDeAutomacao.AguardarSumir(
            seletor = candidata.seletor,
            texto = if (candidata.seletor.isEmpty()) candidata.texto else "",
            timeoutMs = timeoutMs.coerceIn(200, RoteiroDeAutomacaoDaPaginaDoNavegador.TIMEOUT_MAX_MS),
        )

    fun aguardarUrl(
        contem: String,
        timeoutMs: Int = 10_000,
    ): PassoDoRoteiroDeAutomacao.AguardarUrl =
        PassoDoRoteiroDeAutomacao.AguardarUrl(
            contem = contem.trim(),
            timeoutMs = timeoutMs.coerceIn(200, RoteiroDeAutomacaoDaPaginaDoNavegador.TIMEOUT_MAX_MS),
        )

    fun assertar(
        candidata: CandidataDoElementoParaORoteiro,
        contem: String = "",
    ): PassoDoRoteiroDeAutomacao.Assertar =
        PassoDoRoteiroDeAutomacao.Assertar(
            seletor = candidata.seletor,
            texto = if (candidata.seletor.isEmpty()) candidata.texto else "",
            contem = contem.trim(),
        )

    fun extrair(
        candidata: CandidataDoElementoParaORoteiro,
        variavel: String,
    ): PassoDoRoteiroDeAutomacao.Extrair =
        PassoDoRoteiroDeAutomacao.Extrair(
            seletor = candidata.seletor,
            texto = if (candidata.seletor.isEmpty()) candidata.texto else "",
            variavel = variavel.trim(),
        )

    fun opcaoAguardarQualquer(
        id: String,
        candidata: CandidataDoElementoParaORoteiro,
    ): OpcaoDoAguardarQualquer =
        OpcaoDoAguardarQualquer(
            id = id.trim(),
            seletor = candidata.seletor,
            texto = if (candidata.seletor.isEmpty()) candidata.texto else "",
        )

    fun aguardarQualquer(
        opcoes: List<OpcaoDoAguardarQualquer>,
        timeoutMs: Int = 10_000,
        variavel: String = RoteiroDeAutomacaoDaPaginaDoNavegador.VARIAVEL_RAMO_PADRAO,
    ): PassoDoRoteiroDeAutomacao.AguardarQualquer =
        PassoDoRoteiroDeAutomacao.AguardarQualquer(
            opcoes = opcoes,
            timeoutMs = timeoutMs.coerceIn(200, RoteiroDeAutomacaoDaPaginaDoNavegador.TIMEOUT_MAX_MS),
            variavel = variavel.trim().ifBlank {
                RoteiroDeAutomacaoDaPaginaDoNavegador.VARIAVEL_RAMO_PADRAO
            },
        )

    fun marcar(id: String): PassoDoRoteiroDeAutomacao.Marcar =
        PassoDoRoteiroDeAutomacao.Marcar(id.trim())

    fun irPara(
        passo: Int = 0,
        destino: String = "",
        se: String = "",
    ): PassoDoRoteiroDeAutomacao.IrPara =
        PassoDoRoteiroDeAutomacao.IrPara(
            passo = passo.coerceAtLeast(0),
            destino = destino.trim(),
            se = se.trim(),
        )
}

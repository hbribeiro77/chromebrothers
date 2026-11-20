package com.chromebrothers.navegador.regras

/**
 * Ferramentas do chat para criar pincéis do motor: listar, ler, criar, ajustar, testar (devolve a imagem
 * de um traço de amostra para a IA ver), usar no editor e transformar uma imagem anexada em ponta/textura.
 * Os pincéis que vêm com o app nunca são alterados: criar/ajustar sempre grava em "Meus pincéis".
 */
object FerramentasDePinceisDoChat {
    const val LISTAR = "listar_pinceis"
    const val LER = "ler_pincel"
    const val CRIAR = "criar_pincel"
    const val AJUSTAR = "ajustar_pincel"
    const val TESTAR = "testar_pincel"
    const val USAR = "usar_pincel"
    const val IMAGEM_COMO_PONTA = "usar_imagem_como_ponta"

    val nomes = setOf(LISTAR, LER, CRIAR, AJUSTAR, TESTAR, USAR, IMAGEM_COMO_PONTA)

    /** Mudam algo (pincel gravado, editor, arquivo): ficam para o modo agente. */
    val alteram = setOf(CRIAR, AJUSTAR, USAR, IMAGEM_COMO_PONTA)

    /** Um parâmetro numérico do pincel e seus limites. */
    private data class Faixa(val chave: String, val min: Float, val max: Float, val aplicar: (PincelDoMotor, Float) -> PincelDoMotor)

    private val faixas = listOf(
        Faixa("dureza", 0f, 1f) { p, v -> p.copy(dureza = v) },
        Faixa("proporcao", 0.05f, 1f) { p, v -> p.copy(proporcao = v) },
        Faixa("espacamento", 0.01f, 2f) { p, v -> p.copy(espacamento = v) },
        Faixa("dispersao", 0f, 2f) { p, v -> p.copy(dispersao = v) },
        Faixa("tremorDeTamanho", 0f, 1f) { p, v -> p.copy(tremorDeTamanho = v) },
        Faixa("tremorDeOpacidade", 0f, 1f) { p, v -> p.copy(tremorDeOpacidade = v) },
        Faixa("tremorDeRotacao", 0f, 1f) { p, v -> p.copy(tremorDeRotacao = v) },
        Faixa("anguloFixo", -180f, 180f) { p, v -> p.copy(anguloFixo = v) },
        Faixa("fluxo", 0.01f, 1f) { p, v -> p.copy(fluxo = v) },
        Faixa("afinamentoInicio", 0f, 1f) { p, v -> p.copy(afinamentoInicio = v) },
        Faixa("afinamentoFim", 0f, 1f) { p, v -> p.copy(afinamentoFim = v) },
        Faixa("pressaoNoTamanho", 0f, 1f) { p, v -> p.copy(pressaoNoTamanho = v) },
        Faixa("pressaoNaOpacidade", 0f, 1f) { p, v -> p.copy(pressaoNaOpacidade = v) },
        Faixa("multiplicadorDeLargura", 0.1f, 6f) { p, v -> p.copy(multiplicadorDeLargura = v) },
        Faixa("tamanhoMinimo", 0f, 1f) { p, v -> p.copy(tamanhoMinimo = v) },
        Faixa("tamanhoMaximo", 0f, 1f) { p, v -> p.copy(tamanhoMaximo = v) },
        Faixa("opacidadeMinima", 0f, 1f) { p, v -> p.copy(opacidadeMinima = v) },
        Faixa("opacidadeMaxima", 0.02f, 1f) { p, v -> p.copy(opacidadeMaxima = v) },
        Faixa("escalaDoGranulado", 0.1f, 8f) { p, v -> p.copy(escalaDoGranulado = v) },
        Faixa("intensidadeDoGranulado", 0f, 1f) { p, v -> p.copy(intensidadeDoGranulado = v) },
        Faixa("velocidadeNoTamanho", 0f, 1f) { p, v -> p.copy(velocidadeNoTamanho = v) },
        Faixa("velocidadeNaOpacidade", 0f, 1f) { p, v -> p.copy(velocidadeNaOpacidade = v) },
        Faixa("streamline", 0f, 1f) { p, v -> p.copy(streamline = v) },
    )

    data class ParametrosAplicados(val pincel: PincelDoMotor, val avisos: List<String>)

    /**
     * Aplica os parâmetros que vieram nos argumentos (só os presentes mudam), com cada valor preso à faixa
     * válida. Aceita também "ponta" (id de uma ponta pronta), "forma", "granulado", "seguirTraco",
     * "granuladoFixoNoPapel" e "nome". Valores fora da faixa viram aviso (o modelo aprende o limite).
     */
    fun aplicarParametros(base: PincelDoMotor, argumentos: String): ParametrosAplicados {
        var pincel = base
        val avisos = mutableListOf<String>()
        faixas.forEach { faixa ->
            val valor = TextoJsonDoChat.numeroDaChave(argumentos, faixa.chave)?.toFloat() ?: return@forEach
            val preso = valor.coerceIn(faixa.min, faixa.max)
            if (preso != valor) avisos += "${faixa.chave}=$valor fora de ${faixa.min}..${faixa.max}; usei $preso."
            pincel = faixa.aplicar(pincel, preso)
        }
        TextoJsonDoChat.valorDaChave(argumentos, "nome")?.trim()?.takeIf { it.isNotBlank() }?.let {
            pincel = pincel.copy(nome = it.take(40))
        }
        booleano(argumentos, "seguirTraco")?.let { pincel = pincel.copy(seguirTraco = it) }
        booleano(argumentos, "granuladoFixoNoPapel")?.let { pincel = pincel.copy(granuladoFixoNoPapel = it) }
        TextoJsonDoChat.valorDaChave(argumentos, "forma")?.trim()?.uppercase()?.takeIf { it.isNotBlank() }?.let { nome ->
            val forma = FormaDoCarimbo.entries.firstOrNull { it.name == nome }
            when {
                forma == null -> avisos += "forma «$nome» não existe (REDONDA, QUADRADA, RUIDOSA)."
                forma == FormaDoCarimbo.IMAGEM && pincel.arquivoDaForma.isBlank() ->
                    avisos += "forma IMAGEM precisa de ponta (id de ponta pronta) ou de usar_imagem_como_ponta."
                else -> pincel = pincel.copy(forma = forma)
            }
        }
        TextoJsonDoChat.valorDaChave(argumentos, "ponta")?.trim()?.lowercase()?.takeIf { it.isNotBlank() }?.let { id ->
            val ponta = PontasProntasDoMotor.TODAS.firstOrNull { it.id == id }
            if (ponta == null) {
                avisos += "ponta «$id» não existe. Pontas prontas: ${PontasProntasDoMotor.TODAS.joinToString(", ") { it.id }}."
            } else {
                pincel = pincel.copy(forma = FormaDoCarimbo.IMAGEM, arquivoDaForma = ponta.arquivo)
            }
        }
        TextoJsonDoChat.valorDaChave(argumentos, "granulado")?.trim()?.uppercase()?.takeIf { it.isNotBlank() }?.let { nome ->
            val fonte = FonteDoGranulado.entries.firstOrNull { it.name == nome }
            when {
                fonte == null -> avisos += "granulado «$nome» não existe (NENHUM, PAPEL, TELA)."
                fonte == FonteDoGranulado.IMAGEM && pincel.arquivoDoGranulado.isBlank() ->
                    avisos += "granulado IMAGEM precisa de usar_imagem_como_ponta com como=granulado."
                else -> pincel = pincel.copy(granulado = fonte)
            }
        }
        if (pincel.tamanhoMinimo > pincel.tamanhoMaximo) {
            pincel = pincel.copy(tamanhoMinimo = pincel.tamanhoMaximo)
            avisos += "tamanhoMinimo passou do máximo; igualei."
        }
        if (pincel.opacidadeMinima > pincel.opacidadeMaxima) {
            pincel = pincel.copy(opacidadeMinima = pincel.opacidadeMaxima)
            avisos += "opacidadeMinima passou da máxima; igualei."
        }
        return ParametrosAplicados(pincel, avisos)
    }

    private fun booleano(json: String, chave: String): Boolean? =
        Regex("\"" + Regex.escape(chave) + "\"\\s*:\\s*(true|false)").find(json)?.groupValues?.get(1)?.toBooleanStrictOrNull()
            ?: TextoJsonDoChat.valorDaChave(json, chave)?.trim()?.lowercase()?.let { if (it == "true") true else if (it == "false") false else null }

    /** Uma linha por pincel (para listar_pinceis). */
    fun textoDaLista(pinceis: List<PincelDoMotor>, ehPredefinido: (String) -> Boolean): String {
        if (pinceis.isEmpty()) return "Nenhum pincel."
        return pinceis.joinToString("\n") { p ->
            val origem = if (ehPredefinido(p.id)) "vem com o app (não muda; ajustar cria cópia)" else "meu pincel"
            "- ${p.id} — ${p.nome} — ${descricaoCurta(p)} — $origem"
        }
    }

    fun descricaoCurta(p: PincelDoMotor): String {
        val ponta = PontasProntasDoMotor.porArquivo(p.arquivoDaForma)?.id
        val forma = when {
            ponta != null -> "ponta $ponta"
            p.forma == FormaDoCarimbo.IMAGEM -> "ponta de imagem"
            else -> p.forma.name.lowercase()
        }
        return "$forma, espaçamento ${"%.2f".format(java.util.Locale.ROOT, p.espacamento)}, fluxo ${"%.2f".format(java.util.Locale.ROOT, p.fluxo)}"
    }

    /** JSON do pincel + o nome da ponta pronta (para ler_pincel). */
    fun textoDoPincel(p: PincelDoMotor, ehPredefinido: Boolean): String {
        val ponta = PontasProntasDoMotor.porArquivo(p.arquivoDaForma)?.let { "\nponta pronta: ${it.id}" }.orEmpty()
        val origem = if (ehPredefinido) "\nVem com o app: ajustar_pincel cria uma cópia em Meus pincéis." else ""
        return PincelDoMotorEmJson.paraJson(p) + ponta + origem
    }

    fun relatoDoTeste(p: PincelDoMotor): String =
        "Amostra de «${p.nome}» (${p.id}): curva em S com pressão subindo e descendo (traço de cima) e traço reto com pressão constante e leve (embaixo). " +
            "Compare com o pedido: borda, textura, afinamento nas pontas, densidade e falhas. Ajuste e teste de novo se precisar."

    // ---------- Definições (JSON das ferramentas) ----------

    private fun propriedadesDoPincel(): String {
        val numeros = faixas.joinToString(",") { f ->
            """"${f.chave}":{"type":"number","minimum":${f.min},"maximum":${f.max}}"""
        }
        val pontas = PontasProntasDoMotor.TODAS.joinToString(",") { "\"${it.id}\"" }
        return "{" + listOf(
            """"nome":{"type":"string"}""",
            """"ponta":{"type":"string","enum":[$pontas]}""",
            """"forma":{"type":"string","enum":["REDONDA","QUADRADA","RUIDOSA"]}""",
            """"granulado":{"type":"string","enum":["NENHUM","PAPEL","TELA"]}""",
            """"seguirTraco":{"type":"boolean"}""",
            """"granuladoFixoNoPapel":{"type":"boolean"}""",
            numeros,
        ).joinToString(",") + "}"
    }

    fun definicoes(): List<String> {
        val props = propriedadesDoPincel()
        val idEProps = "{\"id\":{\"type\":\"string\"}," + props.removePrefix("{")
        return listOf(
            definicao(LISTAR, "Lista os pincéis do editor de imagem (id, nome, resumo, se vem com o app ou é do usuário).", "{}", "[]"),
            definicao(LER, "Lê todos os parâmetros de um pincel (JSON) pelo id.", """{"id":{"type":"string"}}""", """["id"]"""),
            definicao(
                CRIAR,
                "Cria um pincel em Meus pincéis. Leia antes ler_manual(topico=pinceis). base = id de um pincel para partir dele (opcional); " +
                    "os demais campos sobrescrevem. Devolve o id. Depois teste com testar_pincel.",
                "{\"base\":{\"type\":\"string\"}," + props.removePrefix("{"),
                """["nome"]""",
            ),
            definicao(
                AJUSTAR,
                "Muda parâmetros de um pincel (só os campos passados). Se o pincel vem com o app, cria uma cópia em Meus pincéis e devolve o novo id.",
                idEProps,
                """["id"]""",
            ),
            definicao(
                TESTAR,
                "Pinta um traço de amostra com o pincel e devolve a IMAGEM para você ver o resultado. Use depois de criar ou ajustar e itere até bater com o pedido.",
                """{"id":{"type":"string"},"cor":{"type":"string","description":"#RRGGBB, padrão preto"}}""",
                """["id"]""",
            ),
            definicao(USAR, "Deixa o pincel selecionado no editor de imagem (se o editor estiver aberto).", """{"id":{"type":"string"}}""", """["id"]"""),
            definicao(
                IMAGEM_COMO_PONTA,
                "Transforma a última imagem anexada no chat em ponta (forma do carimbo) ou em textura de granulado (como=granulado). " +
                    "Claro vira tinta (inverter=true troca). Com id, já aplica no pincel (cópia se vier com o app).",
                """{"id":{"type":"string"},"como":{"type":"string","enum":["ponta","granulado"]},"inverter":{"type":"boolean"}}""",
                "[]",
            ),
        )
    }

    private fun definicao(nome: String, descricao: String, propriedades: String, obrigatorios: String): String =
        """{"type":"function","function":{"name":"$nome","description":"${TextoJsonDoChat.escapar(descricao)}","parameters":{"type":"object","properties":$propriedades,"required":$obrigatorios}}}"""

    fun rotuloConcluido(nome: String): String? = when (nome) {
        LISTAR -> "Listou os pincéis"
        LER -> "Leu o pincel"
        CRIAR -> "Criou um pincel"
        AJUSTAR -> "Ajustou o pincel"
        TESTAR -> "Testou o pincel"
        USAR -> "Selecionou o pincel"
        IMAGEM_COMO_PONTA -> "Criou uma ponta"
        else -> null
    }

    fun rotuloEmAndamento(nome: String): String? = when (nome) {
        LISTAR -> "Listando pincéis…"
        LER -> "Lendo o pincel…"
        CRIAR -> "Criando o pincel…"
        AJUSTAR -> "Ajustando o pincel…"
        TESTAR -> "Desenhando a amostra…"
        USAR -> "Selecionando o pincel…"
        IMAGEM_COMO_PONTA -> "Criando a ponta…"
        else -> null
    }
}

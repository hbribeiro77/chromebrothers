package com.chromebrothers.navegador.regras

/**
 * Roteiro determinístico (estilo Playwright) para a página do navegador.
 * A IA grava uma vez; o app executa sem chamar o modelo.
 */
data class RoteiroDeAutomacaoDaPagina(
    val id: String,
    val nome: String,
    val passos: List<PassoDoRoteiroDeAutomacao>,
)

sealed class PassoDoRoteiroDeAutomacao {
    data class Abrir(val url: String) : PassoDoRoteiroDeAutomacao()
    data class Aguardar(
        val seletor: String = "",
        val texto: String = "",
        val timeoutMs: Int = 10_000,
    ) : PassoDoRoteiroDeAutomacao()
    data class Clicar(
        val seletor: String = "",
        val texto: String = "",
    ) : PassoDoRoteiroDeAutomacao()
    data class Preencher(
        val seletor: String = "",
        val texto: String = "",
        val valor: String,
    ) : PassoDoRoteiroDeAutomacao()
    data class EsperarMs(val ms: Int) : PassoDoRoteiroDeAutomacao()
    data class Rolar(
        val seletor: String = "",
        val pixels: Int = 0,
    ) : PassoDoRoteiroDeAutomacao()
    /**
     * Clica no N‑ésimo elemento do seletor só se o texto contiver [contem].
     * [seNao]: "parar" aborta o roteiro; "seguir" pula o passo.
     */
    data class ClicarSe(
        val seletor: String,
        val indice: Int = 0,
        val contem: String,
        val seNao: String = "parar",
    ) : PassoDoRoteiroDeAutomacao()
    /** Espera o elemento sumir (seletor ou texto). */
    data class AguardarSumir(
        val seletor: String = "",
        val texto: String = "",
        val timeoutMs: Int = 10_000,
    ) : PassoDoRoteiroDeAutomacao()
    /** Espera a URL da aba conter o trecho. */
    data class AguardarUrl(
        val contem: String,
        val timeoutMs: Int = 10_000,
    ) : PassoDoRoteiroDeAutomacao()
    /** Falha se o elemento não existir (ou se [contem] não estiver no texto). */
    data class Assertar(
        val seletor: String = "",
        val texto: String = "",
        val contem: String = "",
    ) : PassoDoRoteiroDeAutomacao()
    /** Lê o texto do elemento e guarda em [variavel] para `{{variavel}}` nos próximos passos. */
    data class Extrair(
        val seletor: String = "",
        val texto: String = "",
        val variavel: String,
    ) : PassoDoRoteiroDeAutomacao()
    /**
     * Espera o primeiro de [opcoes] aparecer; grava o `id` em [variavel] (padrão `_ramo`).
     * Na mesma checagem, a ordem da lista decide o vencedor.
     */
    data class AguardarQualquer(
        val opcoes: List<OpcaoDoAguardarQualquer>,
        val timeoutMs: Int = 10_000,
        val variavel: String = "_ramo",
    ) : PassoDoRoteiroDeAutomacao()
    /** Âncora no-op para [IrPara.destino]. */
    data class Marcar(val id: String) : PassoDoRoteiroDeAutomacao()
    /**
     * Salta para [passo] (1-based) ou [destino] (id de [Marcar]).
     * [se] vazio = incondicional; senão `esquerda=direita` após `{{vars}}`.
     * Se a condição falhar, segue o próximo passo.
     */
    data class IrPara(
        val passo: Int = 0,
        val destino: String = "",
        val se: String = "",
    ) : PassoDoRoteiroDeAutomacao()
}

data class OpcaoDoAguardarQualquer(
    val id: String,
    val seletor: String = "",
    val texto: String = "",
)

sealed class ResultadoDoRoteiroDeAutomacao {
    data class Ok(val roteiro: RoteiroDeAutomacaoDaPagina) : ResultadoDoRoteiroDeAutomacao()
    data class Erro(val mensagem: String) : ResultadoDoRoteiroDeAutomacao()
}

object RoteiroDeAutomacaoDaPaginaDoNavegador {
    const val LIMITE_DO_NOME = 80
    const val LIMITE_DE_PASSOS = 40
    const val LIMITE_DA_URL = 2000
    const val LIMITE_DO_SELETOR = 300
    const val LIMITE_DO_TEXTO = 120
    const val LIMITE_DO_VALOR = 4000
    const val LIMITE_DE_OPCOES_AGUARDAR_QUALQUER = 4
    const val TIMEOUT_MAX_MS = 60_000
    const val ESPERA_MAX_MS = 30_000
    const val VARIAVEL_RAMO_PADRAO = "_ramo"

    fun textoDaLista(itens: List<RoteiroDeAutomacaoDaPagina>): String {
        if (itens.isEmpty()) return "Nenhum roteiro guardado."
        return itens.joinToString("\n") { item ->
            "- ${item.id} — ${item.nome} — ${item.passos.size} passo(s)"
        }
    }

    fun ler(json: String): ResultadoDoRoteiroDeAutomacao {
        val nome = TextoJsonDoChat.valorDaChave(json, "nome")?.trim().orEmpty()
        if (nome.isEmpty()) return ResultadoDoRoteiroDeAutomacao.Erro("Faltou o nome do roteiro.")
        if (nome.length > LIMITE_DO_NOME) {
            return ResultadoDoRoteiroDeAutomacao.Erro("O nome passou do limite.")
        }
        val idBruto = TextoJsonDoChat.valorDaChave(json, "id")?.trim().orEmpty()
        val id = idBruto.ifBlank { slugDoNome(nome) }
        if (id.isEmpty() || id.length > 80) {
            return ResultadoDoRoteiroDeAutomacao.Erro("Id do roteiro inválido.")
        }
        val objetos = TextoJsonDoChat.objetosDaChave(json, "passos")
            ?: return ResultadoDoRoteiroDeAutomacao.Erro("Faltou a lista de passos.")
        if (objetos.isEmpty()) {
            return ResultadoDoRoteiroDeAutomacao.Erro("O roteiro precisa de pelo menos um passo.")
        }
        if (objetos.size > LIMITE_DE_PASSOS) {
            return ResultadoDoRoteiroDeAutomacao.Erro("Muitos passos (máximo $LIMITE_DE_PASSOS).")
        }
        val passos = mutableListOf<PassoDoRoteiroDeAutomacao>()
        objetos.forEachIndexed { indice, objeto ->
            when (val passo = lerPasso(objeto)) {
                is ResultadoDoPassoDoRoteiro.Ok -> passos.add(passo.passo)
                is ResultadoDoPassoDoRoteiro.Erro ->
                    return ResultadoDoRoteiroDeAutomacao.Erro("Passo ${indice + 1}: ${passo.mensagem}")
            }
        }
        return ResultadoDoRoteiroDeAutomacao.Ok(
            RoteiroDeAutomacaoDaPagina(id = id, nome = nome, passos = passos),
        )
    }

    fun escrever(roteiro: RoteiroDeAutomacaoDaPagina): String {
        val passosJson = roteiro.passos.joinToString(",") { escreverPasso(it) }
        return """{"id":"${TextoJsonDoChat.escapar(roteiro.id)}","nome":"${TextoJsonDoChat.escapar(roteiro.nome)}","passos":[$passosJson]}"""
    }

    fun slugDoNome(nome: String): String =
        nome.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(60)
            .ifBlank { "roteiro-${System.currentTimeMillis()}" }

    fun rotuloDoPasso(passo: PassoDoRoteiroDeAutomacao): String = when (passo) {
        is PassoDoRoteiroDeAutomacao.Abrir -> "abrir ${passo.url.take(40)}"
        is PassoDoRoteiroDeAutomacao.Aguardar ->
            "aguardar ${passo.seletor.ifBlank { passo.texto }.take(30)}"
        is PassoDoRoteiroDeAutomacao.AguardarSumir ->
            "sumir ${passo.seletor.ifBlank { passo.texto }.take(30)}"
        is PassoDoRoteiroDeAutomacao.AguardarUrl ->
            "url… «${passo.contem.take(30)}»"
        is PassoDoRoteiroDeAutomacao.Clicar ->
            "clicar ${passo.seletor.ifBlank { passo.texto }.take(30)}"
        is PassoDoRoteiroDeAutomacao.Preencher ->
            "preencher ${passo.seletor.ifBlank { passo.texto }.take(20)} = ${passo.valor.take(20)}"
        is PassoDoRoteiroDeAutomacao.EsperarMs -> "esperar ${passo.ms}ms"
        is PassoDoRoteiroDeAutomacao.Rolar -> "rolar ${passo.pixels}px"
        is PassoDoRoteiroDeAutomacao.ClicarSe ->
            "clicar_se[${passo.indice}] se «${passo.contem.take(20)}»"
        is PassoDoRoteiroDeAutomacao.Assertar ->
            "assert ${passo.seletor.ifBlank { passo.texto }.take(24)}"
        is PassoDoRoteiroDeAutomacao.Extrair ->
            "extrair → {{${passo.variavel}}}"
        is PassoDoRoteiroDeAutomacao.AguardarQualquer -> {
            val ids = passo.opcoes.joinToString("|") { it.id }
            "aguardar A|B ($ids) → {{${passo.variavel}}}"
        }
        is PassoDoRoteiroDeAutomacao.Marcar -> "marcar «${passo.id}»"
        is PassoDoRoteiroDeAutomacao.IrPara -> {
            val alvo = when {
                passo.destino.isNotEmpty() -> "@${passo.destino}"
                passo.passo > 0 -> "#${passo.passo}"
                else -> "?"
            }
            if (passo.se.isEmpty()) "ir_para $alvo" else "ir_para $alvo se ${passo.se.take(24)}"
        }
    }

    private fun lerPasso(json: String): ResultadoDoPassoDoRoteiro {
        val acao = TextoJsonDoChat.valorDaChave(json, "acao")?.trim()?.lowercase().orEmpty()
        return when (acao) {
            "abrir" -> {
                val url = TextoJsonDoChat.valorDaChave(json, "url")?.trim().orEmpty()
                when {
                    url.isEmpty() -> ResultadoDoPassoDoRoteiro.Erro("abrir precisa de url.")
                    url.length > LIMITE_DA_URL -> ResultadoDoPassoDoRoteiro.Erro("url longa demais.")
                    !url.startsWith("http://") && !url.startsWith("https://") ->
                        ResultadoDoPassoDoRoteiro.Erro("url precisa ser http ou https.")
                    else -> ResultadoDoPassoDoRoteiro.Ok(PassoDoRoteiroDeAutomacao.Abrir(url))
                }
            }
            "aguardar" -> {
                val seletor = TextoJsonDoChat.valorDaChave(json, "seletor")?.trim().orEmpty()
                val texto = TextoJsonDoChat.valorDaChave(json, "texto")?.trim().orEmpty()
                val timeout = (TextoJsonDoChat.inteiroDaChave(json, "timeout_ms") ?: 10_000)
                    .coerceIn(200, TIMEOUT_MAX_MS)
                when {
                    seletor.isEmpty() && texto.isEmpty() ->
                        ResultadoDoPassoDoRoteiro.Erro("aguardar precisa de seletor ou texto.")
                    seletor.length > LIMITE_DO_SELETOR ->
                        ResultadoDoPassoDoRoteiro.Erro("seletor longo demais.")
                    texto.length > LIMITE_DO_TEXTO ->
                        ResultadoDoPassoDoRoteiro.Erro("texto longo demais.")
                    else -> ResultadoDoPassoDoRoteiro.Ok(
                        PassoDoRoteiroDeAutomacao.Aguardar(seletor, texto, timeout),
                    )
                }
            }
            "clicar" -> {
                val seletor = TextoJsonDoChat.valorDaChave(json, "seletor")?.trim().orEmpty()
                val texto = TextoJsonDoChat.valorDaChave(json, "texto")?.trim().orEmpty()
                when {
                    seletor.isEmpty() && texto.isEmpty() ->
                        ResultadoDoPassoDoRoteiro.Erro("clicar precisa de seletor ou texto.")
                    seletor.length > LIMITE_DO_SELETOR ->
                        ResultadoDoPassoDoRoteiro.Erro("seletor longo demais.")
                    texto.length > LIMITE_DO_TEXTO ->
                        ResultadoDoPassoDoRoteiro.Erro("texto longo demais.")
                    else -> ResultadoDoPassoDoRoteiro.Ok(
                        PassoDoRoteiroDeAutomacao.Clicar(seletor, texto),
                    )
                }
            }
            "preencher" -> {
                val seletor = TextoJsonDoChat.valorDaChave(json, "seletor")?.trim().orEmpty()
                val texto = TextoJsonDoChat.valorDaChave(json, "texto")?.trim().orEmpty()
                val valor = TextoJsonDoChat.valorDaChave(json, "valor") ?: ""
                when {
                    seletor.isEmpty() && texto.isEmpty() ->
                        ResultadoDoPassoDoRoteiro.Erro("preencher precisa de seletor ou texto.")
                    valor.length > LIMITE_DO_VALOR ->
                        ResultadoDoPassoDoRoteiro.Erro("valor longo demais.")
                    seletor.length > LIMITE_DO_SELETOR ->
                        ResultadoDoPassoDoRoteiro.Erro("seletor longo demais.")
                    else -> ResultadoDoPassoDoRoteiro.Ok(
                        PassoDoRoteiroDeAutomacao.Preencher(seletor, texto, valor),
                    )
                }
            }
            "esperar_ms" -> {
                val ms = TextoJsonDoChat.inteiroDaChave(json, "ms") ?: 0
                if (ms !in 1..ESPERA_MAX_MS) {
                    ResultadoDoPassoDoRoteiro.Erro("esperar_ms precisa de ms entre 1 e $ESPERA_MAX_MS.")
                } else {
                    ResultadoDoPassoDoRoteiro.Ok(PassoDoRoteiroDeAutomacao.EsperarMs(ms))
                }
            }
            "rolar" -> {
                val seletor = TextoJsonDoChat.valorDaChave(json, "seletor")?.trim().orEmpty()
                val pixels = TextoJsonDoChat.inteiroDaChave(json, "pixels") ?: 0
                when {
                    seletor.isEmpty() && pixels == 0 ->
                        ResultadoDoPassoDoRoteiro.Erro("rolar precisa de seletor ou pixels.")
                    seletor.length > LIMITE_DO_SELETOR ->
                        ResultadoDoPassoDoRoteiro.Erro("seletor longo demais.")
                    else -> ResultadoDoPassoDoRoteiro.Ok(
                        PassoDoRoteiroDeAutomacao.Rolar(seletor, pixels.coerceIn(-20_000, 20_000)),
                    )
                }
            }
            "clicar_se" -> {
                val seletor = TextoJsonDoChat.valorDaChave(json, "seletor")?.trim().orEmpty()
                val contem = TextoJsonDoChat.valorDaChave(json, "contem")?.trim().orEmpty()
                val indice = (TextoJsonDoChat.inteiroDaChave(json, "indice") ?: 0).coerceIn(0, 50)
                val seNao = TextoJsonDoChat.valorDaChave(json, "se_nao")?.trim()?.lowercase().orEmpty()
                    .ifBlank { "parar" }
                when {
                    seletor.isEmpty() -> ResultadoDoPassoDoRoteiro.Erro("clicar_se precisa de seletor.")
                    contem.isEmpty() -> ResultadoDoPassoDoRoteiro.Erro("clicar_se precisa de contem.")
                    seletor.length > LIMITE_DO_SELETOR ->
                        ResultadoDoPassoDoRoteiro.Erro("seletor longo demais.")
                    contem.length > LIMITE_DO_TEXTO ->
                        ResultadoDoPassoDoRoteiro.Erro("contem longo demais.")
                    seNao != "parar" && seNao != "seguir" ->
                        ResultadoDoPassoDoRoteiro.Erro("se_nao deve ser parar ou seguir.")
                    else -> ResultadoDoPassoDoRoteiro.Ok(
                        PassoDoRoteiroDeAutomacao.ClicarSe(seletor, indice, contem, seNao),
                    )
                }
            }
            "aguardar_sumir" -> {
                val seletor = TextoJsonDoChat.valorDaChave(json, "seletor")?.trim().orEmpty()
                val texto = TextoJsonDoChat.valorDaChave(json, "texto")?.trim().orEmpty()
                val timeout = (TextoJsonDoChat.inteiroDaChave(json, "timeout_ms") ?: 10_000)
                    .coerceIn(200, TIMEOUT_MAX_MS)
                when {
                    seletor.isEmpty() && texto.isEmpty() ->
                        ResultadoDoPassoDoRoteiro.Erro("aguardar_sumir precisa de seletor ou texto.")
                    seletor.length > LIMITE_DO_SELETOR ->
                        ResultadoDoPassoDoRoteiro.Erro("seletor longo demais.")
                    texto.length > LIMITE_DO_TEXTO ->
                        ResultadoDoPassoDoRoteiro.Erro("texto longo demais.")
                    else -> ResultadoDoPassoDoRoteiro.Ok(
                        PassoDoRoteiroDeAutomacao.AguardarSumir(seletor, texto, timeout),
                    )
                }
            }
            "aguardar_url" -> {
                val contem = TextoJsonDoChat.valorDaChave(json, "contem")?.trim().orEmpty()
                val timeout = (TextoJsonDoChat.inteiroDaChave(json, "timeout_ms") ?: 10_000)
                    .coerceIn(200, TIMEOUT_MAX_MS)
                when {
                    contem.isEmpty() ->
                        ResultadoDoPassoDoRoteiro.Erro("aguardar_url precisa de contem.")
                    contem.length > LIMITE_DO_TEXTO ->
                        ResultadoDoPassoDoRoteiro.Erro("contem longo demais.")
                    else -> ResultadoDoPassoDoRoteiro.Ok(
                        PassoDoRoteiroDeAutomacao.AguardarUrl(contem, timeout),
                    )
                }
            }
            "assertar" -> {
                val seletor = TextoJsonDoChat.valorDaChave(json, "seletor")?.trim().orEmpty()
                val texto = TextoJsonDoChat.valorDaChave(json, "texto")?.trim().orEmpty()
                val contem = TextoJsonDoChat.valorDaChave(json, "contem")?.trim().orEmpty()
                when {
                    seletor.isEmpty() && texto.isEmpty() ->
                        ResultadoDoPassoDoRoteiro.Erro("assertar precisa de seletor ou texto.")
                    seletor.length > LIMITE_DO_SELETOR ->
                        ResultadoDoPassoDoRoteiro.Erro("seletor longo demais.")
                    texto.length > LIMITE_DO_TEXTO ->
                        ResultadoDoPassoDoRoteiro.Erro("texto longo demais.")
                    contem.length > LIMITE_DO_TEXTO ->
                        ResultadoDoPassoDoRoteiro.Erro("contem longo demais.")
                    else -> ResultadoDoPassoDoRoteiro.Ok(
                        PassoDoRoteiroDeAutomacao.Assertar(seletor, texto, contem),
                    )
                }
            }
            "extrair" -> {
                val seletor = TextoJsonDoChat.valorDaChave(json, "seletor")?.trim().orEmpty()
                val texto = TextoJsonDoChat.valorDaChave(json, "texto")?.trim().orEmpty()
                val variavel = TextoJsonDoChat.valorDaChave(json, "variavel")?.trim().orEmpty()
                when {
                    seletor.isEmpty() && texto.isEmpty() ->
                        ResultadoDoPassoDoRoteiro.Erro("extrair precisa de seletor ou texto.")
                    !variavelValida(variavel) ->
                        ResultadoDoPassoDoRoteiro.Erro("variavel inválida (use letras/_ e números).")
                    seletor.length > LIMITE_DO_SELETOR ->
                        ResultadoDoPassoDoRoteiro.Erro("seletor longo demais.")
                    else -> ResultadoDoPassoDoRoteiro.Ok(
                        PassoDoRoteiroDeAutomacao.Extrair(seletor, texto, variavel),
                    )
                }
            }
            "aguardar_qualquer" -> lerAguardarQualquer(json)
            "marcar" -> {
                val id = TextoJsonDoChat.valorDaChave(json, "id")?.trim().orEmpty()
                when {
                    !variavelValida(id) ->
                        ResultadoDoPassoDoRoteiro.Erro("marcar precisa de id válido.")
                    else -> ResultadoDoPassoDoRoteiro.Ok(PassoDoRoteiroDeAutomacao.Marcar(id))
                }
            }
            "ir_para" -> {
                val passoN = TextoJsonDoChat.inteiroDaChave(json, "passo") ?: 0
                val destino = TextoJsonDoChat.valorDaChave(json, "destino")?.trim().orEmpty()
                val se = TextoJsonDoChat.valorDaChave(json, "se")?.trim().orEmpty()
                when {
                    passoN <= 0 && destino.isEmpty() ->
                        ResultadoDoPassoDoRoteiro.Erro("ir_para precisa de passo ou destino.")
                    passoN > 0 && destino.isNotEmpty() ->
                        ResultadoDoPassoDoRoteiro.Erro("ir_para: use passo ou destino, não os dois.")
                    destino.isNotEmpty() && !variavelValida(destino) ->
                        ResultadoDoPassoDoRoteiro.Erro("destino inválido.")
                    se.length > LIMITE_DO_TEXTO ->
                        ResultadoDoPassoDoRoteiro.Erro("se longo demais.")
                    else -> ResultadoDoPassoDoRoteiro.Ok(
                        PassoDoRoteiroDeAutomacao.IrPara(
                            passo = passoN.coerceAtLeast(0),
                            destino = destino,
                            se = se,
                        ),
                    )
                }
            }
            else -> ResultadoDoPassoDoRoteiro.Erro("ação desconhecida: $acao")
        }
    }

    private fun lerAguardarQualquer(json: String): ResultadoDoPassoDoRoteiro {
        val variavel = TextoJsonDoChat.valorDaChave(json, "variavel")?.trim()
            .orEmpty()
            .ifBlank { VARIAVEL_RAMO_PADRAO }
        val timeout = (TextoJsonDoChat.inteiroDaChave(json, "timeout_ms") ?: 10_000)
            .coerceIn(200, TIMEOUT_MAX_MS)
        val objetos = TextoJsonDoChat.objetosDaChave(json, "opcoes")
            ?: return ResultadoDoPassoDoRoteiro.Erro("aguardar_qualquer precisa de opcoes.")
        if (objetos.size < 2) {
            return ResultadoDoPassoDoRoteiro.Erro("aguardar_qualquer precisa de pelo menos 2 opções.")
        }
        if (objetos.size > LIMITE_DE_OPCOES_AGUARDAR_QUALQUER) {
            return ResultadoDoPassoDoRoteiro.Erro(
                "aguardar_qualquer: no máximo $LIMITE_DE_OPCOES_AGUARDAR_QUALQUER opções.",
            )
        }
        if (!variavelValida(variavel)) {
            return ResultadoDoPassoDoRoteiro.Erro("variavel inválida.")
        }
        val opcoes = mutableListOf<OpcaoDoAguardarQualquer>()
        val ids = mutableSetOf<String>()
        objetos.forEachIndexed { indice, objeto ->
            val id = TextoJsonDoChat.valorDaChave(objeto, "id")?.trim().orEmpty()
            val seletor = TextoJsonDoChat.valorDaChave(objeto, "seletor")?.trim().orEmpty()
            val texto = TextoJsonDoChat.valorDaChave(objeto, "texto")?.trim().orEmpty()
            when {
                !variavelValida(id) ->
                    return ResultadoDoPassoDoRoteiro.Erro("opção ${indice + 1}: id inválido.")
                !ids.add(id) ->
                    return ResultadoDoPassoDoRoteiro.Erro("opção ${indice + 1}: id duplicado.")
                seletor.isEmpty() && texto.isEmpty() ->
                    return ResultadoDoPassoDoRoteiro.Erro(
                        "opção ${indice + 1}: precisa de seletor ou texto.",
                    )
                seletor.length > LIMITE_DO_SELETOR ->
                    return ResultadoDoPassoDoRoteiro.Erro("opção ${indice + 1}: seletor longo.")
                texto.length > LIMITE_DO_TEXTO ->
                    return ResultadoDoPassoDoRoteiro.Erro("opção ${indice + 1}: texto longo.")
                else -> opcoes.add(OpcaoDoAguardarQualquer(id, seletor, texto))
            }
        }
        return ResultadoDoPassoDoRoteiro.Ok(
            PassoDoRoteiroDeAutomacao.AguardarQualquer(opcoes, timeout, variavel),
        )
    }

    fun variavelValida(nome: String): Boolean =
        nome.isNotEmpty() &&
            nome.length <= 40 &&
            (nome[0].isLetter() || nome[0] == '_') &&
            nome.all { it.isLetterOrDigit() || it == '_' }

    private fun escreverPasso(passo: PassoDoRoteiroDeAutomacao): String = when (passo) {
        is PassoDoRoteiroDeAutomacao.Abrir ->
            """{"acao":"abrir","url":"${TextoJsonDoChat.escapar(passo.url)}"}"""
        is PassoDoRoteiroDeAutomacao.Aguardar -> buildString {
            append("{\"acao\":\"aguardar\"")
            if (passo.seletor.isNotEmpty()) {
                append(",\"seletor\":\"${TextoJsonDoChat.escapar(passo.seletor)}\"")
            }
            if (passo.texto.isNotEmpty()) {
                append(",\"texto\":\"${TextoJsonDoChat.escapar(passo.texto)}\"")
            }
            append(",\"timeout_ms\":${passo.timeoutMs}}")
        }
        is PassoDoRoteiroDeAutomacao.Clicar -> buildString {
            append("{\"acao\":\"clicar\"")
            if (passo.seletor.isNotEmpty()) {
                append(",\"seletor\":\"${TextoJsonDoChat.escapar(passo.seletor)}\"")
            }
            if (passo.texto.isNotEmpty()) {
                append(",\"texto\":\"${TextoJsonDoChat.escapar(passo.texto)}\"")
            }
            append("}")
        }
        is PassoDoRoteiroDeAutomacao.Preencher -> buildString {
            append("{\"acao\":\"preencher\"")
            if (passo.seletor.isNotEmpty()) {
                append(",\"seletor\":\"${TextoJsonDoChat.escapar(passo.seletor)}\"")
            }
            if (passo.texto.isNotEmpty()) {
                append(",\"texto\":\"${TextoJsonDoChat.escapar(passo.texto)}\"")
            }
            append(",\"valor\":\"${TextoJsonDoChat.escapar(passo.valor)}\"}")
        }
        is PassoDoRoteiroDeAutomacao.EsperarMs ->
            """{"acao":"esperar_ms","ms":${passo.ms}}"""
        is PassoDoRoteiroDeAutomacao.Rolar -> buildString {
            append("{\"acao\":\"rolar\"")
            if (passo.seletor.isNotEmpty()) {
                append(",\"seletor\":\"${TextoJsonDoChat.escapar(passo.seletor)}\"")
            }
            if (passo.pixels != 0) append(",\"pixels\":${passo.pixels}")
            append("}")
        }
        is PassoDoRoteiroDeAutomacao.ClicarSe ->
            "{\"acao\":\"clicar_se\",\"seletor\":\"${TextoJsonDoChat.escapar(passo.seletor)}\"," +
                "\"indice\":${passo.indice}," +
                "\"contem\":\"${TextoJsonDoChat.escapar(passo.contem)}\"," +
                "\"se_nao\":\"${TextoJsonDoChat.escapar(passo.seNao)}\"}"
        is PassoDoRoteiroDeAutomacao.AguardarSumir -> buildString {
            append("{\"acao\":\"aguardar_sumir\"")
            if (passo.seletor.isNotEmpty()) {
                append(",\"seletor\":\"${TextoJsonDoChat.escapar(passo.seletor)}\"")
            }
            if (passo.texto.isNotEmpty()) {
                append(",\"texto\":\"${TextoJsonDoChat.escapar(passo.texto)}\"")
            }
            append(",\"timeout_ms\":${passo.timeoutMs}}")
        }
        is PassoDoRoteiroDeAutomacao.AguardarUrl ->
            "{\"acao\":\"aguardar_url\",\"contem\":\"${TextoJsonDoChat.escapar(passo.contem)}\"," +
                "\"timeout_ms\":${passo.timeoutMs}}"
        is PassoDoRoteiroDeAutomacao.Assertar -> buildString {
            append("{\"acao\":\"assertar\"")
            if (passo.seletor.isNotEmpty()) {
                append(",\"seletor\":\"${TextoJsonDoChat.escapar(passo.seletor)}\"")
            }
            if (passo.texto.isNotEmpty()) {
                append(",\"texto\":\"${TextoJsonDoChat.escapar(passo.texto)}\"")
            }
            if (passo.contem.isNotEmpty()) {
                append(",\"contem\":\"${TextoJsonDoChat.escapar(passo.contem)}\"")
            }
            append("}")
        }
        is PassoDoRoteiroDeAutomacao.Extrair -> buildString {
            append("{\"acao\":\"extrair\"")
            if (passo.seletor.isNotEmpty()) {
                append(",\"seletor\":\"${TextoJsonDoChat.escapar(passo.seletor)}\"")
            }
            if (passo.texto.isNotEmpty()) {
                append(",\"texto\":\"${TextoJsonDoChat.escapar(passo.texto)}\"")
            }
            append(",\"variavel\":\"${TextoJsonDoChat.escapar(passo.variavel)}\"}")
        }
        is PassoDoRoteiroDeAutomacao.AguardarQualquer -> buildString {
            append("{\"acao\":\"aguardar_qualquer\"")
            if (passo.variavel != VARIAVEL_RAMO_PADRAO) {
                append(",\"variavel\":\"${TextoJsonDoChat.escapar(passo.variavel)}\"")
            }
            append(",\"timeout_ms\":${passo.timeoutMs}")
            append(",\"opcoes\":[")
            passo.opcoes.forEachIndexed { i, op ->
                if (i > 0) append(",")
                append("{\"id\":\"${TextoJsonDoChat.escapar(op.id)}\"")
                if (op.seletor.isNotEmpty()) {
                    append(",\"seletor\":\"${TextoJsonDoChat.escapar(op.seletor)}\"")
                }
                if (op.texto.isNotEmpty()) {
                    append(",\"texto\":\"${TextoJsonDoChat.escapar(op.texto)}\"")
                }
                append("}")
            }
            append("]}")
        }
        is PassoDoRoteiroDeAutomacao.Marcar ->
            """{"acao":"marcar","id":"${TextoJsonDoChat.escapar(passo.id)}"}"""
        is PassoDoRoteiroDeAutomacao.IrPara -> buildString {
            append("{\"acao\":\"ir_para\"")
            if (passo.passo > 0) append(",\"passo\":${passo.passo}")
            if (passo.destino.isNotEmpty()) {
                append(",\"destino\":\"${TextoJsonDoChat.escapar(passo.destino)}\"")
            }
            if (passo.se.isNotEmpty()) {
                append(",\"se\":\"${TextoJsonDoChat.escapar(passo.se)}\"")
            }
            append("}")
        }
    }

    /** Avalia `esquerda=direita` (após trim); sem `=` trata como verdade se texto não-vazio. */
    fun condicaoIrParaBate(se: String, variaveis: Map<String, String>): Boolean {
        if (se.isBlank()) return true
        val resolvido = VariaveisDoRoteiroDeAutomacaoDaPagina.substituir(se, variaveis)
        val eq = resolvido.indexOf('=')
        if (eq < 0) return resolvido.isNotBlank()
        val esq = resolvido.substring(0, eq).trim()
        val dir = resolvido.substring(eq + 1).trim()
        return esq.equals(dir, ignoreCase = true)
    }

    fun indiceDoDestino(
        passos: List<PassoDoRoteiroDeAutomacao>,
        passo1Based: Int,
        destino: String,
    ): Int? {
        if (destino.isNotEmpty()) {
            val i = passos.indexOfFirst {
                it is PassoDoRoteiroDeAutomacao.Marcar && it.id == destino
            }
            return if (i >= 0) i else null
        }
        if (passo1Based in 1..passos.size) return passo1Based - 1
        return null
    }

    private sealed class ResultadoDoPassoDoRoteiro {
        data class Ok(val passo: PassoDoRoteiroDeAutomacao) : ResultadoDoPassoDoRoteiro()
        data class Erro(val mensagem: String) : ResultadoDoPassoDoRoteiro()
    }
}

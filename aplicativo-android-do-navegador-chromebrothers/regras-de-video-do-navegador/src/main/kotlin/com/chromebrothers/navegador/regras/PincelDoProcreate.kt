package com.chromebrothers.navegador.regras

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Pincel do Procreate (`.brush` / `.brushset`) → [PincelDoMotor].
 *
 * O `.brush` é um ZIP com `Brush.archive` (plist binário do NSKeyedArchiver, objeto `SilicaBrush`),
 * `Shape.png` (forma, branco = tinta) e `Grain.png` (granulado). O `.brushset` tem uma pasta por pincel
 * e um `brushset.plist` com a ordem. O formato não é público: os nomes vêm de engenharia reversa da
 * comunidade e a escala de alguns valores é calibrada com arquivos reais (ver [CHAVES_LIDAS]).
 */
object PincelDoProcreate {
    const val ARQUIVO_DAS_CONFIGURACOES = "Brush.archive"
    const val ARQUIVO_DA_FORMA = "Shape.png"
    const val ARQUIVO_DO_GRANULADO = "Grain.png"
    const val ARQUIVO_DA_ORDEM = "brushset.plist"

    /** Pastas internas que não são pincéis. */
    private val PASTAS_IGNORADAS = setOf("reset", "quicklook", "signature")

    /** Chaves que o mapeamento usa (o resto vai para o relatório de calibração). */
    val CHAVES_LIDAS = setOf(
        "name", "plotSpacing", "plotJitter", "plotSmoothing", "shapeScatter", "shapeRotation", "shapeRoundness",
        "shapeOrientation", "shapeInverted", "textureInverted", "textureScale", "textureDepth", "textureMovement",
        "textureOrientation", "dynamicsPressureSize", "dynamicsPressureOpacity", "dynamicsJitterSize",
        "dynamicsJitterOpacity", "dynamicsSpeedSize", "dynamicsSpeedOpacity", "maxSize", "minSize", "maxOpacity",
        "minOpacity", "taperStartLength", "taperEndLength", "pencilTaperStartLength", "pencilTaperEndLength",
        "dynamicsFlow", "paintFlow", "bundledShapePath", "bundledGrainPath", "shapeAngle",
    )

    /** Recursos do Procreate que o nosso motor não tem: o pincel entra sem eles (e o aviso diz quais). */
    private val RECURSOS_SEM_EQUIVALENTE = listOf(
        "dynamicsMix" to "mistura molhada", "dynamicsLoad" to "mistura molhada", "dynamicsWetness" to "mistura molhada",
        "dynamicsDilution" to "mistura molhada", "dynamicsCharge" to "mistura molhada",
        "blendMode" to "modo de mesclagem", "dualBrush" to "pincel duplo", "isDual" to "pincel duplo",
        "colorDynamics" to "variação de cor", "dynamicsColor" to "variação de cor",
    )

    data class Leitura(
        /** Pincel sem id nem arquivos de imagem (quem importa preenche). */
        val pincel: PincelDoMotor,
        val temFormaPropria: Boolean,
        val formaInvertida: Boolean,
        val granuladoInvertido: Boolean,
        /** O granulado é do pacote interno do Procreate (sem PNG): usamos o papel. */
        val granuladoInterno: Boolean,
        val semEquivalente: List<String>,
        /** `chave=valor` de tudo o que veio (para calibrar o mapeamento com arquivos reais). */
        val relatorio: List<String>,
    )

    /**
     * Pastas (com `/` no fim, ou "" na raiz) que têm um `Brush.archive`, na ordem do `brushset.plist`
     * quando houver ([ordem] = nomes das pastas); as outras vêm depois, em ordem alfabética.
     */
    fun pastasDosPinceis(entradas: Collection<String>, ordem: List<String> = emptyList()): List<String> {
        val pastas = entradas.mapNotNull { nome ->
            val limpo = nome.replace('\\', '/')
            if (!limpo.substringAfterLast('/').equals(ARQUIVO_DAS_CONFIGURACOES, ignoreCase = true)) return@mapNotNull null
            val pasta = limpo.substring(0, limpo.length - ARQUIVO_DAS_CONFIGURACOES.length)
            val partes = pasta.trimEnd('/').split('/').filter { it.isNotEmpty() }
            // Sub* = pincel secundário do pincel duplo (não temos); pastas de UUID nunca começam com "sub".
            if (partes.any { val p = it.lowercase(); p in PASTAS_IGNORADAS || p.startsWith("__macosx") || p.startsWith("sub") }) {
                return@mapNotNull null
            }
            pasta
        }.distinct()
        val posicao = ordem.withIndex().associate { (i, n) -> n.trim('/') to i }
        return pastas.sortedWith(compareBy({ posicao[it.trimEnd('/').substringAfterLast('/')] ?: Int.MAX_VALUE }, { it }))
    }

    /** Ordem das pastas no `brushset.plist` (chave `brushes`); vazio se não der para ler. */
    fun ordemDoBrushset(plist: Any?): List<String> {
        val mapa = plist as? Map<*, *> ?: return emptyList()
        return (mapa["brushes"] as? List<*>)?.mapNotNull { it as? String }.orEmpty()
    }

    fun nomeDoConjunto(plist: Any?): String? = ((plist as? Map<*, *>)?.get("name") as? String)?.takeIf { it.isNotBlank() }

    /** Lê o `SilicaBrush` já resolvido ([PlistBinarioDaApple.raizDoArquivoChaveado]). */
    fun ler(silica: Map<String, Any?>, nomeReserva: String, temFormaPropria: Boolean, temGranuladoProprio: Boolean): Leitura {
        fun numero(vararg chaves: String): Float? = chaves.firstNotNullOfOrNull { numeroDe(silica[it]) }
        fun f01(vararg chaves: String, padrao: Float) = (numero(*chaves) ?: padrao).coerceIn(0f, 1f)

        val nome = (silica["name"] as? String)?.trim()?.takeIf { it.isNotEmpty() } ?: nomeReserva

        // Espaçamento: guardado ao quadrado (5% → 0,0025). Procreate 100% ≈ 1,5 diâmetro de intervalo.
        val espacamento = numero("plotSpacing")?.let { (sqrt(it.coerceIn(0f, 1f)) * 1.5f).coerceIn(0.01f, 1.5f) } ?: 0.1f

        val rotacao = numero("shapeRotation") ?: 0f
        val granuladoInterno = !temGranuladoProprio && silica["bundledGrainPath"] is String
        val profundidadeDoGranulado = numero("textureDepth")
        val temGranulado = (temGranuladoProprio || granuladoInterno) && (profundidadeDoGranulado ?: 1f) > 0.01f
        // Grain "Movendo" acompanha o traço; "Texturizado" (padrão) fica fixo no papel.
        val granuladoAcompanha = (numero("textureMovement") ?: 0f) > 0.5f

        val afinamentoInicio = maxOf(numero("taperStartLength") ?: 0f, numero("pencilTaperStartLength") ?: 0f)
        val afinamentoFim = maxOf(numero("taperEndLength") ?: 0f, numero("pencilTaperEndLength") ?: 0f)

        val escalaDoGranulado = numero("textureScale")?.let { v ->
            // Slider 0..1 do Procreate → 0,25×..4×; valores > 1 já vêm como multiplicador.
            if (v <= 1f) 0.25f + v.coerceAtLeast(0f) * 3.75f else v.coerceAtMost(4f)
        } ?: 1f

        val pincel = PincelDoMotor(
            id = "procreate",
            nome = nome.take(60),
            forma = if (temFormaPropria) FormaDoCarimbo.IMAGEM else FormaDoCarimbo.REDONDA,
            dureza = if (temFormaPropria) 1f else 0.85f,
            proporcao = f01("shapeRoundness", padrao = 1f).coerceAtLeast(0.05f),
            espacamento = espacamento,
            dispersao = f01("plotJitter", padrao = 0f),
            tremorDeTamanho = f01("dynamicsJitterSize", padrao = 0f),
            tremorDeOpacidade = f01("dynamicsJitterOpacity", padrao = 0f),
            tremorDeRotacao = f01("shapeScatter", padrao = 0f),
            seguirTraco = abs(rotacao) >= 0.5f,
            anguloFixo = (numero("shapeAngle") ?: 0f).let { if (abs(it) <= 1f) it * 180f else it }.coerceIn(-180f, 180f),
            fluxo = f01("dynamicsFlow", "paintFlow", padrao = 1f).coerceAtLeast(0.02f),
            afinamentoInicio = afinamentoInicio.coerceIn(0f, 1f),
            afinamentoFim = afinamentoFim.coerceIn(0f, 1f),
            pressaoNoTamanho = f01("dynamicsPressureSize", padrao = 0f),
            pressaoNaOpacidade = f01("dynamicsPressureOpacity", padrao = 0f),
            multiplicadorDeLargura = 1f,
            tamanhoMinimo = f01("minSize", padrao = 0f),
            tamanhoMaximo = f01("maxSize", padrao = 1f).coerceAtLeast(f01("minSize", padrao = 0f)),
            opacidadeMinima = f01("minOpacity", padrao = 0f),
            opacidadeMaxima = f01("maxOpacity", padrao = 1f).coerceAtLeast(f01("minOpacity", padrao = 0f)),
            granulado = when {
                !temGranulado -> FonteDoGranulado.NENHUM
                temGranuladoProprio -> FonteDoGranulado.IMAGEM
                else -> FonteDoGranulado.PAPEL
            },
            granuladoFixoNoPapel = !granuladoAcompanha,
            escalaDoGranulado = escalaDoGranulado.coerceIn(0.25f, 4f),
            intensidadeDoGranulado = (profundidadeDoGranulado ?: 0.6f).coerceIn(0f, 1f),
            velocidadeNoTamanho = abs(numero("dynamicsSpeedSize") ?: 0f).coerceIn(0f, 1f),
            velocidadeNaOpacidade = abs(numero("dynamicsSpeedOpacity") ?: 0f).coerceIn(0f, 1f),
            streamline = f01("plotSmoothing", padrao = 0f),
        )

        val semEquivalente = RECURSOS_SEM_EQUIVALENTE.filter { (chave, _) ->
            silica.keys.any { it.startsWith(chave, ignoreCase = true) && ativo(silica[it]) }
        }.map { it.second }.distinct()

        val relatorio = silica.entries.sortedBy { it.key }.map { (k, v) ->
            val marca = if (k in CHAVES_LIDAS) "" else "  (não usada)"
            "$k=${resumo(v)}$marca"
        }

        return Leitura(
            pincel = pincel,
            temFormaPropria = temFormaPropria,
            formaInvertida = booleano(silica["shapeInverted"]),
            granuladoInvertido = booleano(silica["textureInverted"]),
            granuladoInterno = granuladoInterno,
            semEquivalente = semEquivalente,
            relatorio = relatorio,
        )
    }

    fun numeroDe(v: Any?): Float? = when (v) {
        is Double -> v.toFloat().takeIf { it.isFinite() }
        is Float -> v.takeIf { it.isFinite() }
        is Long -> v.toFloat()
        is Int -> v.toFloat()
        is Boolean -> if (v) 1f else 0f
        else -> null
    }

    private fun booleano(v: Any?): Boolean = (numeroDe(v) ?: 0f) >= 0.5f

    private fun ativo(v: Any?): Boolean = when (v) {
        null -> false
        is String -> v.isNotBlank()
        is Map<*, *> -> v.isNotEmpty()
        else -> abs(numeroDe(v) ?: 0f) > 0.001f
    }

    private fun resumo(v: Any?): String = when (v) {
        null -> "null"
        is ByteArray -> "<${v.size} bytes>"
        is Map<*, *> -> "{${v.size} campos}"
        is List<*> -> "[${v.size}]"
        is String -> "\"" + v.take(80) + "\""
        else -> v.toString()
    }
}

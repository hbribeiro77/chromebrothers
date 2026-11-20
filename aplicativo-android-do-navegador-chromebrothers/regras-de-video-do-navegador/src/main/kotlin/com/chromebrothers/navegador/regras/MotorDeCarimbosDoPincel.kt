package com.chromebrothers.navegador.regras

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

/** Forma do carimbo: o motor carimba esta forma ao longo do caminho do traço. */
enum class FormaDoCarimbo { REDONDA, QUADRADA, RUIDOSA, IMAGEM }

/** Textura do traço (granulado): gerada pelo app ou imagem da galeria (claro = tinta). */
enum class FonteDoGranulado { NENHUM, PAPEL, TELA, IMAGEM }

/**
 * Parâmetros de um pincel do motor de carimbos (estilo Brush Studio).
 * Frações são 0..1; [espacamento] é fração do diâmetro do carimbo.
 */
data class PincelDoMotor(
    val id: String,
    val nome: String,
    val forma: FormaDoCarimbo = FormaDoCarimbo.REDONDA,
    /** 0 = borda bem macia, 1 = borda dura. */
    val dureza: Float = 1f,
    /** Altura/largura do carimbo: 1 = redondo, menor = achatado (ponta de pena). */
    val proporcao: Float = 1f,
    val espacamento: Float = 0.1f,
    /** Deslocamento aleatório do carimbo, em frações do diâmetro. */
    val dispersao: Float = 0f,
    val tremorDeTamanho: Float = 0f,
    val tremorDeOpacidade: Float = 0f,
    /** 0..1 → até ±180° de giro aleatório por carimbo. */
    val tremorDeRotacao: Float = 0f,
    /** true: o carimbo gira com a direção do traço; false: fica no [anguloFixo]. */
    val seguirTraco: Boolean = false,
    val anguloFixo: Float = 0f,
    /** Opacidade de cada carimbo; o traço acumula até a opacidade escolhida. */
    val fluxo: Float = 1f,
    /** Comprimento do afinamento em unidades de até 8 diâmetros (0 = sem afinamento). */
    val afinamentoInicio: Float = 0f,
    val afinamentoFim: Float = 0f,
    /** Quanto a pressão da caneta reduz tamanho / opacidade quando é leve (0 = não reduz). */
    val pressaoNoTamanho: Float = 0f,
    val pressaoNaOpacidade: Float = 0f,
    /** Diâmetro do pincel em relação ao tamanho escolhido no controle. */
    val multiplicadorDeLargura: Float = 1f,
    /** Forma [FormaDoCarimbo.IMAGEM]: nome do arquivo da máscara (branco = tinta) na pasta das formas. */
    val arquivoDaForma: String = "",
    /** Limites: o controle de tamanho (0..1) anda só entre mínimo e máximo deste pincel; idem a opacidade. */
    val tamanhoMinimo: Float = 0f,
    val tamanhoMaximo: Float = 1f,
    val opacidadeMinima: Float = 0f,
    val opacidadeMaxima: Float = 1f,
    /** Granulado: textura aplicada ao traço. */
    val granulado: FonteDoGranulado = FonteDoGranulado.NENHUM,
    val arquivoDoGranulado: String = "",
    /** true: textura presa ao papel (repassar revela o mesmo grão); false: acompanha cada carimbo. */
    val granuladoFixoNoPapel: Boolean = true,
    /** Tamanho da textura (1 = padrão). */
    val escalaDoGranulado: Float = 1f,
    /** 0 = textura não aparece; 1 = só onde a textura é clara entra tinta. */
    val intensidadeDoGranulado: Float = 0.6f,
    /** Dinâmica: quanto o traço rápido afina / clareia (0 = velocidade não muda nada). */
    val velocidadeNoTamanho: Float = 0f,
    val velocidadeNaOpacidade: Float = 0f,
    /** StreamLine: o traço segue a ponta com atraso suave e a alcança no fim (0 = desligado). */
    val streamline: Float = 0f,
) {
    val temDinamicaDeVelocidade: Boolean get() = velocidadeNoTamanho > 0.001f || velocidadeNaOpacidade > 0.001f

    val temGranulado: Boolean get() = granulado != FonteDoGranulado.NENHUM && intensidadeDoGranulado > 0.001f

    /** Valor do controle de tamanho (0..1) dentro dos limites do pincel. */
    fun tamanhoNoControle(valor: Float): Float {
        val minimo = tamanhoMinimo.coerceIn(0f, 1f)
        val maximo = tamanhoMaximo.coerceIn(minimo, 1f)
        return minimo + valor.coerceIn(0f, 1f) * (maximo - minimo)
    }

    /** Opacidade escolhida (0..1) dentro dos limites do pincel. */
    fun opacidadeNoControle(valor: Float): Float {
        val minimo = opacidadeMinima.coerceIn(0f, 1f)
        val maximo = opacidadeMaxima.coerceIn(minimo, 1f)
        return (minimo + valor.coerceIn(0f, 1f) * (maximo - minimo)).coerceAtLeast(0.02f)
    }
}

/** Pincel do motor ⇄ JSON (uma linha), para "Meus pincéis" e para o rascunho levar a cópia usada. */
object PincelDoMotorEmJson {
    fun paraJson(p: PincelDoMotor): String {
        fun n(v: Float) = String.format(java.util.Locale.ROOT, "%.4f", v)
        fun t(v: String) = "\"" + TextoJsonDoChat.escapar(v) + "\""
        return "{" + listOf(
            "\"id\":" + t(p.id),
            "\"nome\":" + t(p.nome),
            "\"forma\":" + t(p.forma.name),
            "\"arquivoDaForma\":" + t(p.arquivoDaForma),
            "\"dureza\":" + n(p.dureza),
            "\"proporcao\":" + n(p.proporcao),
            "\"espacamento\":" + n(p.espacamento),
            "\"dispersao\":" + n(p.dispersao),
            "\"tremorDeTamanho\":" + n(p.tremorDeTamanho),
            "\"tremorDeOpacidade\":" + n(p.tremorDeOpacidade),
            "\"tremorDeRotacao\":" + n(p.tremorDeRotacao),
            "\"seguirTraco\":" + (if (p.seguirTraco) 1 else 0),
            "\"anguloFixo\":" + n(p.anguloFixo),
            "\"fluxo\":" + n(p.fluxo),
            "\"afinamentoInicio\":" + n(p.afinamentoInicio),
            "\"afinamentoFim\":" + n(p.afinamentoFim),
            "\"pressaoNoTamanho\":" + n(p.pressaoNoTamanho),
            "\"pressaoNaOpacidade\":" + n(p.pressaoNaOpacidade),
            "\"multiplicadorDeLargura\":" + n(p.multiplicadorDeLargura),
            "\"tamanhoMinimo\":" + n(p.tamanhoMinimo),
            "\"tamanhoMaximo\":" + n(p.tamanhoMaximo),
            "\"opacidadeMinima\":" + n(p.opacidadeMinima),
            "\"opacidadeMaxima\":" + n(p.opacidadeMaxima),
            "\"granulado\":" + t(p.granulado.name),
            "\"arquivoDoGranulado\":" + t(p.arquivoDoGranulado),
            "\"granuladoFixoNoPapel\":" + (if (p.granuladoFixoNoPapel) 1 else 0),
            "\"escalaDoGranulado\":" + n(p.escalaDoGranulado),
            "\"intensidadeDoGranulado\":" + n(p.intensidadeDoGranulado),
            "\"velocidadeNoTamanho\":" + n(p.velocidadeNoTamanho),
            "\"velocidadeNaOpacidade\":" + n(p.velocidadeNaOpacidade),
            "\"streamline\":" + n(p.streamline),
        ).joinToString(",") + "}"
    }

    fun deJson(json: String): PincelDoMotor? {
        val bruto = json.trim()
        val id = TextoJsonDoChat.valorDaChave(bruto, "id").orEmpty()
        if (id.isBlank() || id.any { !it.isLetterOrDigit() && it != '-' }) return null
        val padrao = PincelDoMotor(id = id, nome = id)
        fun f(chave: String, reserva: Float) = TextoJsonDoChat.numeroDaChave(bruto, chave)?.toFloat() ?: reserva
        val forma = runCatching { FormaDoCarimbo.valueOf(TextoJsonDoChat.valorDaChave(bruto, "forma").orEmpty()) }
            .getOrDefault(FormaDoCarimbo.REDONDA)
        fun arquivoSeguro(chave: String) = TextoJsonDoChat.valorDaChave(bruto, chave).orEmpty()
            .takeIf { a -> a.isNotEmpty() && a.all { it.isLetterOrDigit() || it == '-' || it == '.' } && !a.startsWith(".") }
            .orEmpty()
        val arquivo = arquivoSeguro("arquivoDaForma")
        val arquivoDoGranulado = arquivoSeguro("arquivoDoGranulado")
        val granulado = runCatching { FonteDoGranulado.valueOf(TextoJsonDoChat.valorDaChave(bruto, "granulado").orEmpty()) }
            .getOrDefault(FonteDoGranulado.NENHUM)
        return PincelDoMotor(
            id = id,
            nome = TextoJsonDoChat.valorDaChave(bruto, "nome").orEmpty().ifBlank { id },
            forma = if (forma == FormaDoCarimbo.IMAGEM && arquivo.isEmpty()) FormaDoCarimbo.REDONDA else forma,
            arquivoDaForma = arquivo,
            dureza = f("dureza", padrao.dureza),
            proporcao = f("proporcao", padrao.proporcao),
            espacamento = f("espacamento", padrao.espacamento),
            dispersao = f("dispersao", padrao.dispersao),
            tremorDeTamanho = f("tremorDeTamanho", padrao.tremorDeTamanho),
            tremorDeOpacidade = f("tremorDeOpacidade", padrao.tremorDeOpacidade),
            tremorDeRotacao = f("tremorDeRotacao", padrao.tremorDeRotacao),
            seguirTraco = f("seguirTraco", 0f) >= 0.5f,
            anguloFixo = f("anguloFixo", padrao.anguloFixo),
            fluxo = f("fluxo", padrao.fluxo),
            afinamentoInicio = f("afinamentoInicio", padrao.afinamentoInicio),
            afinamentoFim = f("afinamentoFim", padrao.afinamentoFim),
            pressaoNoTamanho = f("pressaoNoTamanho", padrao.pressaoNoTamanho),
            pressaoNaOpacidade = f("pressaoNaOpacidade", padrao.pressaoNaOpacidade),
            multiplicadorDeLargura = f("multiplicadorDeLargura", padrao.multiplicadorDeLargura),
            tamanhoMinimo = f("tamanhoMinimo", padrao.tamanhoMinimo),
            tamanhoMaximo = f("tamanhoMaximo", padrao.tamanhoMaximo),
            opacidadeMinima = f("opacidadeMinima", padrao.opacidadeMinima),
            opacidadeMaxima = f("opacidadeMaxima", padrao.opacidadeMaxima),
            granulado = if (granulado == FonteDoGranulado.IMAGEM && arquivoDoGranulado.isEmpty()) FonteDoGranulado.NENHUM else granulado,
            arquivoDoGranulado = arquivoDoGranulado,
            granuladoFixoNoPapel = f("granuladoFixoNoPapel", 1f) >= 0.5f,
            escalaDoGranulado = f("escalaDoGranulado", padrao.escalaDoGranulado),
            intensidadeDoGranulado = f("intensidadeDoGranulado", padrao.intensidadeDoGranulado),
            velocidadeNoTamanho = f("velocidadeNoTamanho", padrao.velocidadeNoTamanho),
            velocidadeNaOpacidade = f("velocidadeNaOpacidade", padrao.velocidadeNaOpacidade),
            streamline = f("streamline", padrao.streamline).coerceIn(0f, 1f),
        )
    }

    /** Chave da versão exata do pincel (id + resumo dos parâmetros): o traço aponta para ela no rascunho. */
    fun chave(p: PincelDoMotor): String = p.id + "~" + String.format("%08x", paraJson(p).hashCode())

    fun idDaChave(chave: String): String = chave.substringBefore('~')
}

/** Um ponto do traço como o motor vê: posição, largura (já com pressão) e opacidade 0..1. */
data class PontoParaCarimbar(val x: Float, val y: Float, val largura: Float, val alpha: Float)

/** Um carimbo pronto para desenhar: centro, tamanho, opacidade 0..1 e rotação. */
data class Carimbo(
    val x: Float,
    val y: Float,
    val largura: Float,
    val altura: Float,
    val alpha: Float,
    val rotacaoGraus: Float,
    /** Distância ao longo do traço onde o carimbo caiu (separa a parte assentada da cauda que afina). */
    val distancia: Float = 0f,
)

object MotorDeCarimbosDoPincel {
    const val COMPRIMENTO_MAXIMO_DO_AFINAMENTO = 8f
    /**
     * O fim afina ao vivo, logo atrás da ponta: uma cauda longa fazia o traço todo "encher" atrás da caneta
     * (com 95% eram ~8 diâmetros, quase o traço inteiro). Curta, parece a ponta natural do pincel.
     */
    const val COMPRIMENTO_MAXIMO_DO_AFINAMENTO_DO_FIM = 3f
    const val PASSO_MINIMO = 0.5f
    const val LIMITE_DE_CARIMBOS = 30_000
    private const val FATOR_MINIMO_DO_AFINAMENTO = 0.05f

    /**
     * Carimbos ao longo do traço. Determinístico pela [semente]: o carimbo i depende só do caminho até ele
     * e de i, então acrescentar pontos no fim não muda os carimbos já gerados (desenho incremental).
     * O afinamento do fim só entra com [comAfinamentoDoFim] (traço concluído), como no Procreate.
     */
    fun gerar(
        pontos: List<PontoParaCarimbar>,
        pincel: PincelDoMotor,
        semente: Long,
        comAfinamentoDoFim: Boolean,
    ): List<Carimbo> {
        if (pontos.isEmpty()) return emptyList()
        if (pontos.size == 1) {
            val p = pontos[0]
            var fator = 1f
            if (pincel.afinamentoInicio > 0f) fator = FATOR_MINIMO_DO_AFINAMENTO
            if (comAfinamentoDoFim && pincel.afinamentoFim > 0f) fator = FATOR_MINIMO_DO_AFINAMENTO
            return listOf(carimbo(p.x, p.y, p.largura, p.alpha, pincel.anguloFixo, fator, pincel, semente, 0, 0f))
        }
        val acumulado = FloatArray(pontos.size)
        for (i in 1 until pontos.size) {
            acumulado[i] = acumulado[i - 1] + hypot(pontos[i].x - pontos[i - 1].x, pontos[i].y - pontos[i - 1].y)
        }
        val total = acumulado.last()
        val referencia = max(pontos[0].largura, 1f)
        val compInicio = pincel.afinamentoInicio * COMPRIMENTO_MAXIMO_DO_AFINAMENTO * referencia
        val compFim = pincel.afinamentoFim * COMPRIMENTO_MAXIMO_DO_AFINAMENTO_DO_FIM * referencia
        val saida = ArrayList<Carimbo>()
        var s = 0f
        var seg = 1
        var indice = 0
        while (s <= total && indice < LIMITE_DE_CARIMBOS) {
            while (seg < pontos.size - 1 && acumulado[seg] < s) seg++
            val a = pontos[seg - 1]
            val b = pontos[seg]
            val comprimento = acumulado[seg] - acumulado[seg - 1]
            val t = if (comprimento > 0f) ((s - acumulado[seg - 1]) / comprimento).coerceIn(0f, 1f) else 0f
            val x = a.x + (b.x - a.x) * t
            val y = a.y + (b.y - a.y) * t
            val largura = a.largura + (b.largura - a.largura) * t
            val alpha = a.alpha + (b.alpha - a.alpha) * t
            var fator = 1f
            if (compInicio > 0f) fator *= (s / compInicio).coerceIn(FATOR_MINIMO_DO_AFINAMENTO, 1f)
            if (comAfinamentoDoFim && compFim > 0f) fator *= ((total - s) / compFim).coerceIn(FATOR_MINIMO_DO_AFINAMENTO, 1f)
            val direcao = if (pincel.seguirTraco) {
                Math.toDegrees(atan2((b.y - a.y).toDouble(), (b.x - a.x).toDouble())).toFloat()
            } else {
                pincel.anguloFixo
            }
            saida += carimbo(x, y, largura, alpha, direcao, fator, pincel, semente, indice, s)
            s += max(pincel.espacamento * largura * fator, PASSO_MINIMO)
            indice++
        }
        return saida
    }

    private fun carimbo(
        x: Float,
        y: Float,
        largura: Float,
        alpha: Float,
        direcaoGraus: Float,
        fatorDoAfinamento: Float,
        pincel: PincelDoMotor,
        semente: Long,
        indice: Int,
        distancia: Float,
    ): Carimbo {
        // Sorteio próprio por carimbo: o mesmo índice sempre dá o mesmo tremor.
        val sorteio = kotlin.random.Random(semente xor (indice.toLong() * -0x61c8864680b583ebL))
        val tamanho = largura * fatorDoAfinamento * (1f - pincel.tremorDeTamanho * sorteio.nextFloat())
        val opacidade = (alpha * (1f - pincel.tremorDeOpacidade * sorteio.nextFloat())).coerceIn(0f, 1f)
        val anguloDaDispersao = sorteio.nextFloat() * 2f * PI.toFloat()
        val raioDaDispersao = pincel.dispersao * largura * sorteio.nextFloat()
        val rotacao = direcaoGraus + (sorteio.nextFloat() * 2f - 1f) * pincel.tremorDeRotacao * 180f
        return Carimbo(
            x = x + cos(anguloDaDispersao) * raioDaDispersao,
            y = y + sin(anguloDaDispersao) * raioDaDispersao,
            largura = tamanho,
            altura = tamanho * pincel.proporcao.coerceIn(0.05f, 1f),
            alpha = opacidade,
            rotacaoGraus = rotacao,
            distancia = distancia,
        )
    }

    /**
     * Onde começa a cauda que afina no fim: antes disso os carimbos já são definitivos enquanto o traço cresce
     * (o desenho ao vivo assenta esses e redesenha só a cauda a cada quadro). Sem afinamento do fim: tudo assentado.
     */
    fun inicioDaCauda(pontos: List<PontoParaCarimbar>, pincel: PincelDoMotor): Float {
        if (pontos.size < 2 || pincel.afinamentoFim <= 0f) return Float.MAX_VALUE
        var total = 0f
        for (i in 1 until pontos.size) total += hypot(pontos[i].x - pontos[i - 1].x, pontos[i].y - pontos[i - 1].y)
        val referencia = max(pontos[0].largura, 1f)
        return total - pincel.afinamentoFim * COMPRIMENTO_MAXIMO_DO_AFINAMENTO_DO_FIM * referencia
    }

    /** Limites (esquerda, topo, direita, base) cobrindo todos os carimbos, para a camada do traço. */
    fun limites(carimbos: List<Carimbo>): FloatArray? {
        if (carimbos.isEmpty()) return null
        var esquerda = Float.MAX_VALUE
        var topo = Float.MAX_VALUE
        var direita = -Float.MAX_VALUE
        var base = -Float.MAX_VALUE
        carimbos.forEach { c ->
            val r = max(c.largura, c.altura) / 2f + 1f
            esquerda = minOf(esquerda, c.x - r)
            topo = minOf(topo, c.y - r)
            direita = maxOf(direita, c.x + r)
            base = maxOf(base, c.y + r)
        }
        return floatArrayOf(esquerda, topo, direita, base)
    }

    /**
     * Pressão com que se desenha normalmente: nela o pincel tem o tamanho/opacidade escolhidos.
     * Assim a sensibilidade só muda onde se aperta (ou alivia), e não o traço todo.
     */
    const val PRESSAO_NORMAL = 0.4f

    /**
     * Fator de tamanho pela pressão (null = dedo: 1). Pressão normal = 1; apertar engrossa até 1 + sensibilidade
     * (2× com 100%); aliviar afina. Mesma ideia das pontas antigas (caneta/nanquim).
     */
    fun fatorDaPressaoNoTamanho(pressao: Float?, sensibilidade: Float): Float {
        if (pressao == null) return 1f
        val s = sensibilidade.coerceIn(0f, 1f)
        val p = pressao.coerceIn(0f, 1f)
        val fator = if (p >= PRESSAO_NORMAL) {
            1f + s * (p - PRESSAO_NORMAL) / (1f - PRESSAO_NORMAL)
        } else {
            1f - s * (PRESSAO_NORMAL - p) / PRESSAO_NORMAL
        }
        return fator.coerceIn(0.05f, 2f)
    }

    /** Velocidade (dp/ms) a partir da qual o traço conta como "bem rápido" para a dinâmica. */
    const val VELOCIDADE_RAPIDA = 2.5f

    /**
     * Fator da dinâmica de velocidade: parado = 1; quanto mais rápido (até [VELOCIDADE_RAPIDA]), menor,
     * até 1 − sensibilidade × 0,9. Vale para tamanho e opacidade, cada um com sua sensibilidade.
     */
    fun fatorDaVelocidade(velocidadeDpPorMs: Float, sensibilidade: Float): Float {
        val s = sensibilidade.coerceIn(0f, 1f)
        if (s <= 0f) return 1f
        val rapidez = (velocidadeDpPorMs / VELOCIDADE_RAPIDA).coerceIn(0f, 1f)
        return (1f - s * 0.9f * rapidez).coerceIn(0.1f, 1f)
    }

    /** Suaviza a velocidade entre eventos do toque (os eventos chegam irregulares e fariam o traço tremer). */
    fun suavizarVelocidade(anterior: Float?, nova: Float): Float =
        if (anterior == null) nova else anterior * 0.75f + nova * 0.25f

    /** Fator de opacidade pela pressão (null = dedo: 1). Da pressão normal para cima fica cheia; só o toque leve clareia. */
    fun fatorDaPressaoNaOpacidade(pressao: Float?, sensibilidade: Float): Float {
        if (pressao == null) return 1f
        val p = pressao.coerceIn(0f, 1f)
        if (p >= PRESSAO_NORMAL) return 1f
        return (1f - sensibilidade.coerceIn(0f, 1f) * (PRESSAO_NORMAL - p) / PRESSAO_NORMAL).coerceIn(0.02f, 1f)
    }
}

/**
 * Pincéis do motor que já vêm no app. Tamanho base calibrado para a pressão normal (40%) ficar como era
 * antes da pressão ser ancorada nela; apertar ainda engrossa até 1 + pressaoNoTamanho.
 */
object PinceisPredefinidosDoMotor {
    val AEROGRAFO = PincelDoMotor(
        id = "aerografo", nome = "Aerógrafo",
        forma = FormaDoCarimbo.REDONDA, dureza = 0f, espacamento = 0.08f, fluxo = 0.07f,
        pressaoNoTamanho = 0.2f, pressaoNaOpacidade = 0.8f, multiplicadorDeLargura = 2.6f,
    )
    val GIZ = PincelDoMotor(
        id = "giz", nome = "Giz",
        forma = FormaDoCarimbo.RUIDOSA, dureza = 0.7f, espacamento = 0.15f, dispersao = 0.08f,
        tremorDeTamanho = 0.25f, tremorDeOpacidade = 0.3f, tremorDeRotacao = 1f, fluxo = 0.7f,
        pressaoNaOpacidade = 0.5f, pressaoNoTamanho = 0.2f, multiplicadorDeLargura = 1.4f,
        granulado = FonteDoGranulado.PAPEL, granuladoFixoNoPapel = true, intensidadeDoGranulado = 0.65f,
    )
    val PINCEL_SECO = PincelDoMotor(
        id = "pincel-seco", nome = "Pincel seco",
        forma = FormaDoCarimbo.RUIDOSA, dureza = 1f, proporcao = 0.45f, seguirTraco = true, espacamento = 0.05f,
        tremorDeOpacidade = 0.4f, fluxo = 0.5f, afinamentoInicio = 0.3f, afinamentoFim = 0.4f,
        pressaoNoTamanho = 0.6f, multiplicadorDeLargura = 1.4f, streamline = 0.1f,
    )
    val PENA = PincelDoMotor(
        id = "pena", nome = "Pena caligráfica",
        forma = FormaDoCarimbo.REDONDA, dureza = 1f, proporcao = 0.25f, anguloFixo = 45f, espacamento = 0.03f,
        afinamentoInicio = 0.25f, afinamentoFim = 0.35f, pressaoNoTamanho = 0.5f, multiplicadorDeLargura = 1f,
        streamline = 0.2f,
    )
    val NANQUIM_AFINADO = PincelDoMotor(
        id = "nanquim-afinado", nome = "Nanquim afinado",
        forma = FormaDoCarimbo.REDONDA, dureza = 1f, espacamento = 0.04f,
        afinamentoInicio = 0.5f, afinamentoFim = 0.6f, pressaoNoTamanho = 0.8f, multiplicadorDeLargura = 0.55f,
        streamline = 0.3f,
    )

    // ---- Com as pontas prontas (máscaras desenhadas pelo app, ver PontasProntasDoMotor) ----
    private fun comPonta(ponta: PontaPronta) = PincelDoMotor(
        id = "ponta-${ponta.id}",
        nome = ponta.nome,
        forma = FormaDoCarimbo.IMAGEM,
        arquivoDaForma = ponta.arquivo,
    )

    val LAPIS_GRAFITE = comPonta(PontasProntasDoMotor.LAPIS).copy(
        espacamento = 0.06f, tremorDeRotacao = 1f, fluxo = 0.85f,
        pressaoNoTamanho = 0.35f, pressaoNaOpacidade = 0.75f, multiplicadorDeLargura = 0.5f,
        granulado = FonteDoGranulado.PAPEL, intensidadeDoGranulado = 0.45f,
    )
    val CARVAO = comPonta(PontasProntasDoMotor.CARVAO).copy(
        espacamento = 0.08f, tremorDeRotacao = 1f, dispersao = 0.04f, fluxo = 0.75f,
        pressaoNoTamanho = 0.3f, pressaoNaOpacidade = 0.6f, multiplicadorDeLargura = 1.6f,
        granulado = FonteDoGranulado.PAPEL, intensidadeDoGranulado = 0.7f,
    )
    val GIZ_PASTEL = comPonta(PontasProntasDoMotor.GIZ_PASTEL).copy(
        espacamento = 0.1f, tremorDeRotacao = 1f, tremorDeTamanho = 0.15f, fluxo = 0.8f,
        pressaoNoTamanho = 0.3f, pressaoNaOpacidade = 0.5f, multiplicadorDeLargura = 1.5f,
        granulado = FonteDoGranulado.PAPEL, intensidadeDoGranulado = 0.55f,
    )
    val AQUARELA = comPonta(PontasProntasDoMotor.AQUARELA).copy(
        espacamento = 0.12f, tremorDeRotacao = 1f, tremorDeTamanho = 0.2f, fluxo = 0.22f,
        pressaoNoTamanho = 0.4f, pressaoNaOpacidade = 0.6f, multiplicadorDeLargura = 2.4f,
    )
    val PINCEL_DE_CERDAS = comPonta(PontasProntasDoMotor.CERDAS).copy(
        nome = "Pincel de cerdas", seguirTraco = true, espacamento = 0.02f, fluxo = 0.6f,
        afinamentoInicio = 0.2f, afinamentoFim = 0.3f, pressaoNoTamanho = 0.6f, pressaoNaOpacidade = 0.3f,
        multiplicadorDeLargura = 1.8f, streamline = 0.15f,
    )
    val PINCEL_SECO_DE_CERDAS = comPonta(PontasProntasDoMotor.PINCEL_SECO).copy(
        nome = "Cerdas secas", seguirTraco = true, espacamento = 0.02f, fluxo = 0.55f, tremorDeOpacidade = 0.3f,
        afinamentoFim = 0.3f, pressaoNoTamanho = 0.5f, multiplicadorDeLargura = 1.8f, streamline = 0.1f,
    )
    val RESPINGOS = comPonta(PontasProntasDoMotor.RESPINGOS).copy(
        espacamento = 0.9f, dispersao = 0.6f, tremorDeTamanho = 0.6f, tremorDeRotacao = 1f, fluxo = 1f,
        pressaoNoTamanho = 0.5f, multiplicadorDeLargura = 3f,
    )
    val ESPONJA = comPonta(PontasProntasDoMotor.ESPONJA).copy(
        espacamento = 0.25f, tremorDeRotacao = 1f, fluxo = 0.6f, pressaoNaOpacidade = 0.5f, multiplicadorDeLargura = 2.5f,
    )
    val MARCADOR_CHANFRADO = comPonta(PontasProntasDoMotor.CHANFRADO).copy(
        anguloFixo = 35f, espacamento = 0.03f, fluxo = 0.9f, pressaoNoTamanho = 0.2f, multiplicadorDeLargura = 1.4f,
        streamline = 0.2f,
    )
    val PENA_TINTEIRO = comPonta(PontasProntasDoMotor.TINTEIRO).copy(
        anguloFixo = 40f, espacamento = 0.03f, afinamentoInicio = 0.2f, afinamentoFim = 0.3f,
        pressaoNoTamanho = 0.7f, multiplicadorDeLargura = 0.9f, streamline = 0.25f,
    )
    val NEVOA = comPonta(PontasProntasDoMotor.NEVOA).copy(
        espacamento = 0.15f, tremorDeRotacao = 1f, fluxo = 0.12f, pressaoNaOpacidade = 0.7f, multiplicadorDeLargura = 3.5f,
    )
    val HACHURA = comPonta(PontasProntasDoMotor.HACHURA).copy(
        espacamento = 0.35f, fluxo = 0.9f, pressaoNaOpacidade = 0.5f, multiplicadorDeLargura = 2.2f,
    )

    val TODOS: List<PincelDoMotor> = listOf(
        NANQUIM_AFINADO, PENA, PINCEL_SECO, GIZ, AEROGRAFO,
        LAPIS_GRAFITE, CARVAO, GIZ_PASTEL, AQUARELA, PINCEL_DE_CERDAS, PINCEL_SECO_DE_CERDAS,
        RESPINGOS, ESPONJA, MARCADOR_CHANFRADO, PENA_TINTEIRO, NEVOA, HACHURA,
    )

    fun porId(id: String): PincelDoMotor? = TODOS.firstOrNull { it.id == id }
}

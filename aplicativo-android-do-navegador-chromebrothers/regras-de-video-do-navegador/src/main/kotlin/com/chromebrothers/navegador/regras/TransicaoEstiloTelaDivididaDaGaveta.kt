package com.chromebrothers.navegador.regras

/**
 * Abertura/fechamento do chat no estilo da tela dividida da Samsung: dois cartões arredondados
 * (página e chat) separados por um vão com a barrinha em destaque; a página fica borrada e
 * levemente escurecida durante o movimento.
 *
 * O layout real muda uma vez só, escondido sob a camada; a animação só move a camada.
 */
object TransicaoEstiloTelaDivididaDaGaveta {
    const val DURACAO_DE_ABRIR_MS = 360L
    const val DURACAO_DE_FECHAR_MS = 300L
    const val DURACAO_DO_CROSSFADE_MS = 140L

    /** Fração do TEMPO em que a camada começa a sumir (o movimento já está perto do fim). */
    const val INICIO_DO_CROSSFADE = 0.75f

    /**
     * Depois de mudar de largura, o site costuma se reorganizar (grade responsiva, imagens).
     * A camada só some quando duas miniaturas seguidas da página ficam praticamente iguais.
     */
    const val INTERVALO_DA_AMOSTRA_MS = 70L
    /** Tempo seguido com a página parada antes de revelar. */
    const val QUIETUDE_EXIGIDA_MS = 150L
    /**
     * Piso desde a mudança de largura: sites como o Instagram esperam ~0,4 s para começar a
     * refazer a grade; antes disso a página "parada" ainda não é a final. Subir se algum site pular.
     */
    const val PISO_DESDE_A_MUDANCA_MS = 350L
    /** Teto desde a mudança de largura (sites que nunca param: vídeo, GIF). */
    const val ESPERA_MAXIMA_DA_PAGINA_MS = 750L

    /** Pode revelar: já passou o piso e a página ficou quieta, ou estourou o teto. */
    fun podeRevelar(desdeAMudancaMs: Long, quietaHaMs: Long): Boolean =
        desdeAMudancaMs >= ESPERA_MAXIMA_DA_PAGINA_MS ||
            (desdeAMudancaMs >= PISO_DESDE_A_MUDANCA_MS && quietaHaMs >= QUIETUDE_EXIGIDA_MS)
    /**
     * Desfocar até virar cor e focar de volta: a página antiga passa por níveis de desfoque
     * cada vez mais fortes (o último é quase só mancha de cor); no nível máximo troca pela
     * página nova (as duas ficam praticamente iguais) e a nova ganha foco nível a nível.
     * Raios na imagem já reduzida por [FATOR_DE_REDUCAO], do mais leve ao mais forte.
     */
    val RAIOS_DOS_NIVEIS = intArrayOf(3, 8, 20)

    /** Foco da página nova: troca no nível máximo e volta pelos níveis até o mais leve. */
    const val DURACAO_DO_FOCO_MS = 220L

    /** Escurecimento que sobra no fim do foco (o crossfade final tira o resto). */
    const val ESCURECIMENTO_NO_FIM_DO_FOCO = 0.10f

    /**
     * Opacidade de cada nível empilhado (do mais leve ao mais forte) para um desfoque 0..1:
     * 0 = nítido (nenhum nível), 1 = só o nível mais forte aparece.
     */
    fun opacidadesDosNiveis(desfoque: Float): FloatArray {
        val n = RAIOS_DOS_NIVEIS.size
        val d = desfoque.coerceIn(0f, 1f) * n
        return FloatArray(n) { i -> (d - i).coerceIn(0f, 1f) }
    }

    /**
     * Foco da página nova, [foco] 0..1: os níveis novos entram do mais forte para o mais leve
     * (empilhados com o mais leve por cima). Devolve as opacidades na ordem dos raios.
     */
    fun opacidadesDoFoco(foco: Float): FloatArray {
        val n = RAIOS_DOS_NIVEIS.size
        val f = foco.coerceIn(0f, 1f) * n
        // índice 0 = mais leve (entra por último); n-1 = mais forte (entra primeiro)
        return FloatArray(n) { i -> (f - (n - 1 - i)).coerceIn(0f, 1f) }
    }
    const val LARGURA_DA_AMOSTRA = 96
    const val ALTURA_DA_AMOSTRA = 60
    /** Diferença média por canal (0..255) abaixo da qual a página é considerada parada. */
    const val LIMITE_DE_DIFERENCA = 2.0

    fun paginaParada(anterior: IntArray?, atual: IntArray): Boolean {
        if (anterior == null || anterior.size != atual.size || atual.isEmpty()) return false
        var soma = 0L
        for (i in atual.indices) {
            val a = anterior[i]
            val b = atual[i]
            soma += kotlin.math.abs(((a shr 16) and 0xFF) - ((b shr 16) and 0xFF)) +
                kotlin.math.abs(((a shr 8) and 0xFF) - ((b shr 8) and 0xFF)) +
                kotlin.math.abs((a and 0xFF) - (b and 0xFF))
        }
        return soma.toDouble() / (atual.size * 3) < LIMITE_DE_DIFERENCA
    }

    /** Parte do progresso em que o desfoque chega ao máximo (começa nítido = igual à tela). */
    const val FRACAO_DE_ENTRADA_DO_DESFOQUE = 0.7f

    /** Parte da divisão em que cantos e vão chegam ao tamanho cheio. */
    const val FRACAO_DE_ENTRADA_DOS_CANTOS = 0.25f

    /** A captura é reduzida por este fator antes de borrar (borrar 1 vez, barato). */
    const val FATOR_DE_REDUCAO = 8
    const val RAIO_DOS_CANTOS_DP = 22f
    const val VAO_DP = 10f
    const val ESCURECIMENTO_MAXIMO = 0.22f
    const val LARGURA_DA_BARRINHA_DP = 5f
    const val ALTURA_DA_BARRINHA_DP = 52f
    const val COR_DO_VAO = 0xFF0B0D11.toInt()
    const val COR_DO_CARTAO_DO_CHAT = 0xFF1A1D24.toInt()

    data class Quadro(
        /** Borda direita do cartão da página. */
        val paginaDireita: Float,
        /** Borda esquerda do cartão do chat. */
        val chatEsquerda: Float,
        /** Centro da barrinha. */
        val divisorX: Float,
        val raioDosCantos: Float,
        /** 0 = nítido; 1 = todo borrado. */
        val desfoque: Float,
        val escurecimento: Float,
        /** Opacidade da barrinha. */
        val barrinha: Float,
    )

    /**
     * @param divisao 0 = só a página; 1 = chat na largura alvo.
     * @param progresso 0..1 desde o início da animação (abrir ou fechar).
     */
    fun quadro(
        largura: Int,
        larguraDoChat: Int,
        divisao: Float,
        progresso: Float,
        densidade: Float,
    ): Quadro {
        val w = largura.coerceAtLeast(0).toFloat()
        val d = divisao.coerceIn(0f, 1f)
        val cantos = (d / FRACAO_DE_ENTRADA_DOS_CANTOS).coerceIn(0f, 1f)
        val vao = VAO_DP * densidade * cantos
        val divisor = (w - larguraDoChat.coerceAtLeast(0) * d).coerceIn(0f, w)
        val desfoque = (progresso.coerceIn(0f, 1f) / FRACAO_DE_ENTRADA_DO_DESFOQUE).coerceIn(0f, 1f)
        return Quadro(
            paginaDireita = (divisor - vao / 2f).coerceAtLeast(0f),
            chatEsquerda = (divisor + vao / 2f).coerceAtMost(w),
            divisorX = divisor,
            raioDosCantos = RAIO_DOS_CANTOS_DP * densidade * cantos,
            desfoque = desfoque,
            escurecimento = ESCURECIMENTO_MAXIMO * desfoque,
            barrinha = cantos,
        )
    }

    /** Escala horizontal para a parte da página na captura caber no cartão (borrada, não se nota). */
    fun escalaHorizontal(larguraDoCartao: Float, larguraDaFonte: Float): Float =
        if (larguraDaFonte <= 0f) 1f else (larguraDoCartao / larguraDaFonte).coerceAtLeast(0f)

    /**
     * Desfoque de caixa em 3 passadas (aproxima gaussiano), separável, com bordas presas.
     * Pensado para a captura já reduzida ([FATOR_DE_REDUCAO]); pixels ARGB.
     */
    fun borrar(pixels: IntArray, largura: Int, altura: Int, raio: Int): IntArray {
        if (largura <= 0 || altura <= 0 || raio <= 0 || pixels.size < largura * altura) return pixels.copyOf()
        var atual = pixels.copyOf()
        val temporario = IntArray(atual.size)
        repeat(3) {
            passada(atual, temporario, largura, altura, raio, horizontal = true)
            passada(temporario, atual, largura, altura, raio, horizontal = false)
        }
        return atual
    }

    private fun passada(
        origem: IntArray,
        destino: IntArray,
        largura: Int,
        altura: Int,
        raio: Int,
        horizontal: Boolean,
    ) {
        val linhas = if (horizontal) altura else largura
        val comprimento = if (horizontal) largura else altura
        val janela = raio * 2 + 1
        for (linha in 0 until linhas) {
            fun indice(i: Int): Int {
                val p = i.coerceIn(0, comprimento - 1)
                return if (horizontal) linha * largura + p else p * largura + linha
            }
            var a = 0
            var r = 0
            var g = 0
            var b = 0
            for (i in -raio..raio) {
                val c = origem[indice(i)]
                a += c ushr 24
                r += (c shr 16) and 0xFF
                g += (c shr 8) and 0xFF
                b += c and 0xFF
            }
            for (i in 0 until comprimento) {
                destino[indice(i)] = ((a / janela) shl 24) or ((r / janela) shl 16) or ((g / janela) shl 8) or (b / janela)
                val sai = origem[indice(i - raio)]
                val entra = origem[indice(i + raio + 1)]
                a += (entra ushr 24) - (sai ushr 24)
                r += ((entra shr 16) and 0xFF) - ((sai shr 16) and 0xFF)
                g += ((entra shr 8) and 0xFF) - ((sai shr 8) and 0xFF)
                b += (entra and 0xFF) - (sai and 0xFF)
            }
        }
    }
}

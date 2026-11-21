package com.chromebrothers.navegador

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chromebrothers.navegador.regras.ScrubFinoPorToqueLongoNoPlayerDeVideo
import java.util.concurrent.atomic.AtomicInteger

class ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo(
    activity: Activity,
    private val painel: View,
    private val faixa: LinearLayout,
    private val rotuloDoTempo: TextView,
    private val obterPosicaoMs: () -> Long,
    private val obterDuracaoMs: () -> Long,
    private val seekParaMs: (Long) -> Unit,
    private val estavaTocando: () -> Boolean,
    private val pausar: () -> Unit,
    private val retomar: () -> Unit,
    private val aoEntrar: () -> Unit = {},
    private val aoSair: () -> Unit = {},
    private val preencherFilmstrip: (
        tempos: List<Long>,
        larguraPx: Int,
        alturaPx: Int,
        indiceCentral: Int,
        aindaAtivo: () -> Boolean,
        aoQuadro: (indice: Int, bitmap: Bitmap?) -> Unit,
        aoFim: () -> Unit,
    ) -> Unit = { _, _, _, _, _, _, aoFim -> aoFim() },
    private val cancelarPreenchimentoDaFilmstrip: () -> Unit = {},
    private val capturarQuadroAvulso: (
        tempoMs: Long,
        larguraPx: Int,
        alturaPx: Int,
        aoPronto: (Bitmap?) -> Unit,
    ) -> Unit = { _, _, _, aoPronto -> aoPronto(null) },
    /** Chamado após seek do scrub — player da página captura o quadro atual sem seek extra. */
    private val aoAposSeekDoScrub: (tempoMs: Long) -> Unit = {},
    private val obterChaveDoVideo: () -> String = { "" },
    /** Arquivo do aparelho que está tocando (liga a tesoura de recorte); null = sem recorte (ex.: vídeo de site). */
    obterArquivoParaRecorte: (() -> String?)? = null,
) {
    private val principal = Handler(Looper.getMainLooper())
    private val densidade = activity.resources.displayMetrics.density
    private val limiarDoArrastePx = ViewConfiguration.get(activity).scaledTouchSlop.toFloat().coerceAtLeast(16f)
    private val miniaturaAltura = (96f * densidade).toInt().coerceAtLeast(72)
    private val miniaturaLargura = (168f * densidade).toInt().coerceAtLeast(120)
    private val espacoEntreCelulas = (2f * densidade).toInt().coerceAtLeast(1)
    private val vistas = mutableListOf<ImageView>()
    private val temQuadro = mutableListOf<Boolean>()
    private val pedindoQuadro = mutableSetOf<Int>()
    private val geracao = AtomicInteger(0)
    private val botaoSair: ImageButton =
        painel.findViewById(R.id.botao_sair_do_modo_de_scrub_fino_por_toque_longo_do_player_de_video)
    private val viewport: FrameLayout =
        painel.findViewById(R.id.viewport_da_filmstrip_do_scrub_fino_por_toque_longo_do_player_de_video)
    private val moldura: View =
        painel.findViewById(R.id.moldura_fixa_do_scrub_fino_por_toque_longo_do_player_de_video)
    private var ativo = false
    private var dedoArrastando = false
    private var arrasteConfirmado = false
    private var inicioX = 0f
    private var deslocamentoNoInicioDoArraste = 0f
    private var deslocamentoAtual = 0f
    private var duracaoMs = 0L
    private var tocarAoSairDoModo = false
    private var ultimoSeekMs = 0L
    private var ultimoPedidoDeVisiveisMs = 0L
    private var posicaoAlvoMs = 0L
    private var tempos: List<Long> = emptyList()
    private var passoDaFilmstripMs = ScrubFinoPorToqueLongoNoPlayerDeVideo.PASSO_PADRAO_MS
    private var chaveDoVideoAtual = ""
    /** Depois do long-press o Android manda CANCEL; ignora pra não matar o arraste sticky. */
    private var ignorarSoltarAteMs = 0L
    private var ultimoAcompanharMs = 0L
    private val acompanharReproducao = object : Runnable {
        override fun run() {
            if (!ativo) return
            sincronizarTrilhaComReproducao()
            principal.postDelayed(this, 50L)
        }
    }

    val estaAtivo: Boolean get() = ativo
    val estaComDedoBaixo: Boolean get() = dedoArrastando
    val posicaoAtualMs: Long get() = posicaoAlvoMs

    private val recorte: ModoDeRecorteDeTrechoNoScrubFinoDoPlayer? = obterArquivoParaRecorte?.let { arquivo ->
        ModoDeRecorteDeTrechoNoScrubFinoDoPlayer(
            activity = activity,
            painel = painel,
            obterArquivo = arquivo,
            obterPosicaoMs = { if (ativo) posicaoAlvoMs else obterPosicaoMs() },
            obterDuracaoMs = { duracaoMs.takeIf { it > 0L } ?: obterDuracaoMs() },
            seekParaMs = { ms -> irParaTempoPeloRecorte(ms) },
            tocar = retomar,
            pausar = pausar,
            estaTocando = estavaTocando,
        )
    }

    /** O modo recorte mudou a posição (barra ou "ver trecho"): tira de quadros e rótulo acompanham. */
    private fun irParaTempoPeloRecorte(ms: Long) {
        if (!ativo) {
            seekParaMs(ms)
            return
        }
        posicaoAlvoMs = ms.coerceIn(0L, duracaoMs)
        aplicarDeslocamentoParaTempo(posicaoAlvoMs)
        atualizarRotulo()
        seekParaMs(posicaoAlvoMs)
        aoAposSeekDoScrub(posicaoAlvoMs)
        pedirQuadrosAoRedorDoCentro()
    }

    init {
        botaoSair.setOnClickListener { sairDoModo() }
        val toque = View.OnTouchListener { _, evento ->
            if (!ativo) return@OnTouchListener false
            tratarToqueDeArraste(evento)
        }
        viewport.setOnTouchListener(toque)
        faixa.setOnTouchListener(toque)
        ajustarLarguraDaMoldura()
    }

    fun entrar(xNaSuperficie: Float, larguraDaSuperficie: Float) {
        if (ativo) return
        duracaoMs = obterDuracaoMs().coerceAtLeast(0L)
        if (duracaoMs <= 0L) return
        ativo = true
        // Mantém o dedo do long-press como arraste, antes do post/layout.
        comecarArraste(xNaSuperficie)
        ignorarSoltarAteMs = SystemClock.uptimeMillis() + 600L
        tocarAoSairDoModo = estavaTocando()
        pausar()
        aoEntrar()
        passoDaFilmstripMs = ScrubFinoPorToqueLongoNoPlayerDeVideo.passoDaFilmstrip(duracaoMs)
        tempos = ScrubFinoPorToqueLongoNoPlayerDeVideo.temposDaFilmstrip(duracaoMs, passoDaFilmstripMs)
        chaveDoVideoAtual = obterChaveDoVideo().trim()
        posicaoAlvoMs = obterPosicaoMs().coerceIn(0L, duracaoMs)
        painel.visibility = View.VISIBLE
        recorte?.aoEntrarNoScrub()
        painel.post {
            if (!ativo) return@post
            montarFilmstrip()
            aplicarQuadrosDoCache()
            aplicarDeslocamentoParaTempo(posicaoAlvoMs)
            atualizarRotulo()
            seekParaMs(posicaoAlvoMs)
            aoAposSeekDoScrub(posicaoAlvoMs)
            carregarMiniaturasEmLote()
            pedirQuadrosAoRedorDoCentro()
            // Reancora o X atual (layout pode ter mudado; dedo ainda pode estar em movimento).
            deslocamentoNoInicioDoArraste = deslocamentoAtual
            inicioX = xNaSuperficie
            iniciarAcompanhamentoDaReproducao()
        }
    }

    /** Quadro já no tempo atual (ex.: canvas do &lt;video&gt;) — preenche a célula mais próxima. */
    fun oferecerQuadroNaPosicaoAtual(bitmap: Bitmap?) {
        if (!ativo || bitmap == null || bitmap.isRecycled) return
        val pedido = geracao.get()
        aplicarQuadro(pedido, indiceMaisProximo(posicaoAlvoMs), bitmap)
    }

    fun precisaQuadroNaPosicaoAtual(): Boolean {
        if (!ativo || tempos.isEmpty()) return false
        val indice = indiceMaisProximo(posicaoAlvoMs)
        return temQuadro.getOrNull(indice) != true
    }

    fun sairDoModo() {
        if (!ativo) return
        pararAcompanhamentoDaReproducao()
        cancelarPreenchimentoDaFilmstrip()
        pedindoQuadro.clear()
        seekParaMs(posicaoAlvoMs)
        ativo = false
        dedoArrastando = false
        faixa.translationX = 0f
        recorte?.aoSairDoScrub()
        painel.visibility = View.GONE
        geracao.incrementAndGet()
        aoSair()
        // Se o usuário já deu play pelos controles, deixa; senão retoma se estava tocando ao entrar.
        if (!estavaTocando() && tocarAoSairDoModo) retomar()
    }

    fun cancelarSemRetomar() {
        if (!ativo) return
        tocarAoSairDoModo = false
        sairDoModo()
    }

    /**
     * [tomarToque] vê cada evento fora do scrub antes do detector; quando devolve true o toque passa
     * a ser dele (ex.: swipe entre vídeos) e o detector recebe CANCEL para não virar toque longo nem toque.
     */
    fun ligarNaSuperficie(
        superficie: View,
        detectorBase: GestureDetector.SimpleOnGestureListener,
        tomarToque: ((MotionEvent) -> Boolean)? = null,
    ) {
        var toqueTomado = false
        val detector = GestureDetector(
            superficie.context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    detectorBase.onDown(e)
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    // No scrub: mesmo gesto de sempre — mostra/esconde controles (play, velocidade…).
                    return detectorBase.onSingleTapConfirmed(e)
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (ativo) return true
                    return detectorBase.onDoubleTap(e)
                }

                override fun onLongPress(e: MotionEvent) {
                    if (ativo) return
                    superficie.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    entrar(e.x, superficie.width.toFloat())
                }
            },
        )
        superficie.setOnTouchListener { _, evento ->
            if (ativo) {
                // Arraste da trilha + toque simples pra controles (play, velocidade…).
                tratarToqueDeArraste(evento)
                detector.onTouchEvent(evento)
                return@setOnTouchListener true
            }
            if (evento.actionMasked == MotionEvent.ACTION_DOWN) toqueTomado = false
            if (tomarToque != null && tomarToque(evento)) {
                if (!toqueTomado) {
                    toqueTomado = true
                    val cancelar = MotionEvent.obtain(evento).apply { action = MotionEvent.ACTION_CANCEL }
                    detector.onTouchEvent(cancelar)
                    cancelar.recycle()
                }
                return@setOnTouchListener true
            }
            if (toqueTomado) return@setOnTouchListener true
            detector.onTouchEvent(evento)
            true
        }
    }

    private fun tratarToqueDeArraste(evento: MotionEvent): Boolean {
        if (!ativo) return false
        when (evento.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                comecarArraste(evento.x)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // Recupera arraste se o CANCEL do long-press zerou o estado.
                if (!dedoArrastando) comecarArraste(evento.x)
                mover(evento.x)
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                if (SystemClock.uptimeMillis() < ignorarSoltarAteMs) return true
                soltarDedo()
                return true
            }
            MotionEvent.ACTION_UP -> {
                ignorarSoltarAteMs = 0L
                soltarDedo()
                return true
            }
        }
        return dedoArrastando
    }

    private fun comecarArraste(x: Float) {
        dedoArrastando = true
        arrasteConfirmado = false
        inicioX = x
        deslocamentoNoInicioDoArraste = deslocamentoAtual
    }

    private fun mover(x: Float) {
        if (!ativo || !dedoArrastando) return
        if (!arrasteConfirmado) {
            if (!ScrubFinoPorToqueLongoNoPlayerDeVideo.arrasteConfirmadoPeloDeslocamento(
                    deslocamentoDoDedoPx = x - inicioX,
                    limiarPx = limiarDoArrastePx,
                )
            ) {
                return
            }
            arrasteConfirmado = true
        }
        val proposto = deslocamentoNoInicioDoArraste + (x - inicioX)
        val passo = passoHorizontalPx()
        deslocamentoAtual = ScrubFinoPorToqueLongoNoPlayerDeVideo.limitarDeslocamentoDaFaixaPx(
            proposto,
            tempos.size,
            passo,
        )
        faixa.translationX = deslocamentoAtual
        posicaoAlvoMs = ScrubFinoPorToqueLongoNoPlayerDeVideo.tempoApartirDoDeslocamentoPx(
            deslocamentoAtual,
            tempos,
            passo,
        )
        atualizarRotulo()
        val agora = SystemClock.uptimeMillis()
        if (agora - ultimoSeekMs >= 40L) {
            ultimoSeekMs = agora
            seekParaMs(posicaoAlvoMs)
            aoAposSeekDoScrub(posicaoAlvoMs)
        }
        if (agora - ultimoPedidoDeVisiveisMs >= 120L) {
            ultimoPedidoDeVisiveisMs = agora
            pedirQuadrosAoRedorDoCentro()
        }
    }

    private fun soltarDedo() {
        if (!ativo) return
        val moveuDeVerdade = arrasteConfirmado
        dedoArrastando = false
        arrasteConfirmado = false
        // Toque curto só pra controles: não seeka nem captura (evita spinner/travada).
        if (!moveuDeVerdade) return
        seekParaMs(posicaoAlvoMs)
        aoAposSeekDoScrub(posicaoAlvoMs)
        aplicarDeslocamentoParaTempo(posicaoAlvoMs)
        pedirQuadrosAoRedorDoCentro()
    }

    private fun iniciarAcompanhamentoDaReproducao() {
        principal.removeCallbacks(acompanharReproducao)
        principal.post(acompanharReproducao)
    }

    private fun pararAcompanhamentoDaReproducao() {
        principal.removeCallbacks(acompanharReproducao)
    }

    private fun sincronizarTrilhaComReproducao() {
        if (!ScrubFinoPorToqueLongoNoPlayerDeVideo.deveAcompanharReproducaoNaTrilha(
                modoAtivo = ativo,
                dedoArrastando = dedoArrastando,
                videoTocando = estavaTocando(),
            )
        ) {
            return
        }
        val agora = SystemClock.uptimeMillis()
        val atual = obterPosicaoMs().coerceIn(0L, duracaoMs)
        if (atual != posicaoAlvoMs) {
            posicaoAlvoMs = atual
            aplicarDeslocamentoParaTempo(posicaoAlvoMs)
            atualizarRotulo()
        }
        if (agora - ultimoAcompanharMs >= 160L) {
            ultimoAcompanharMs = agora
            aoAposSeekDoScrub(posicaoAlvoMs)
            pedirQuadrosAoRedorDoCentro()
        }
    }

    private fun passoHorizontalPx(): Float = (miniaturaLargura + espacoEntreCelulas).toFloat()

    private fun aplicarDeslocamentoParaTempo(tempoMs: Long) {
        deslocamentoAtual = ScrubFinoPorToqueLongoNoPlayerDeVideo.deslocamentoDaFaixaPx(
            tempoMs,
            tempos,
            passoHorizontalPx(),
        )
        faixa.translationX = deslocamentoAtual
    }

    private fun montarFilmstrip() {
        val larguraViewport = viewport.width.coerceAtLeast(painel.width).coerceAtLeast(1)
        val preenchimento = ScrubFinoPorToqueLongoNoPlayerDeVideo.preenchimentoLateralParaCentralizar(
            larguraViewport,
            miniaturaLargura,
        )
        faixa.removeAllViews()
        vistas.clear()
        temQuadro.clear()
        pedindoQuadro.clear()
        faixa.orientation = LinearLayout.HORIZONTAL
        faixa.clipChildren = false
        faixa.clipToPadding = false
        faixa.setPadding(preenchimento, 0, preenchimento, 0)
        ajustarLarguraDaMoldura()
        tempos.forEachIndexed { indice, _ ->
            val vista = ImageView(faixa.context)
            val params = LinearLayout.LayoutParams(miniaturaLargura, miniaturaAltura)
            if (indice < tempos.lastIndex) params.marginEnd = espacoEntreCelulas
            vista.layoutParams = params
            vista.scaleType = ImageView.ScaleType.CENTER_CROP
            vista.adjustViewBounds = false
            vista.background = GradientDrawable().apply {
                setColor(Color.parseColor("#5C6575"))
                setStroke((1f * densidade).toInt().coerceAtLeast(1), Color.parseColor("#B0B8C4"))
            }
            vista.setImageDrawable(null)
            faixa.addView(vista)
            vistas += vista
            temQuadro += false
        }
        // Largura explícita: com WRAP_CONTENT o FrameLayout mede AT_MOST=tela e a
        // LinearLayout corta tudo que passa da primeira tela (parece "faixa preta"/z-index).
        val paramsFaixa = faixa.layoutParams as FrameLayout.LayoutParams
        paramsFaixa.width = ScrubFinoPorToqueLongoNoPlayerDeVideo.larguraTotalDaFaixaPx(
            quantidadeDeCelulas = tempos.size,
            larguraDaCelulaPx = miniaturaLargura,
            espacoEntreCelulasPx = espacoEntreCelulas,
            preenchimentoLateralPx = preenchimento,
        )
        paramsFaixa.height = FrameLayout.LayoutParams.MATCH_PARENT
        paramsFaixa.gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
        faixa.layoutParams = paramsFaixa
        viewport.clipChildren = true
        viewport.clipToPadding = true
        viewport.layoutParams = viewport.layoutParams.apply { height = miniaturaAltura }
    }

    private fun ajustarLarguraDaMoldura() {
        val params = moldura.layoutParams as FrameLayout.LayoutParams
        params.width = miniaturaLargura
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        params.gravity = android.view.Gravity.CENTER
        moldura.layoutParams = params
    }

    private fun carregarMiniaturasEmLote() {
        val pedido = geracao.incrementAndGet()
        cancelarPreenchimentoDaFilmstrip()
        val centro = indiceMaisProximo(posicaoAlvoMs)
        // Só MMR, do centro pra fora — seek no player apagava a tela e não gerava miniatura.
        preencherFilmstrip(
            tempos,
            miniaturaLargura,
            miniaturaAltura,
            centro,
            { ativo && pedido == geracao.get() },
            { indice, bitmap ->
                principal.post { aplicarQuadro(pedido, indice, bitmap) }
            },
            {
                principal.post { pedirQuadrosAoRedorDoCentro() }
            },
        )
    }

    private fun aplicarQuadrosDoCache() {
        if (chaveDoVideoAtual.isEmpty() || tempos.isEmpty()) return
        val pedido = geracao.get()
        tempos.forEachIndexed { indice, tempo ->
            if (temQuadro.getOrNull(indice) == true) return@forEachIndexed
            val cached = CacheDeQuadrosDaFilmstripDoScrubFinoPorToqueLongoDoPlayerDeVideo.obter(
                chaveDoVideo = chaveDoVideoAtual,
                tempoMs = tempo,
                passoMs = passoDaFilmstripMs,
            ) ?: return@forEachIndexed
            aplicarQuadro(pedido, indice, cached, guardarNoCache = false)
        }
    }

    private fun pedirQuadrosAoRedorDoCentro() {
        if (!ativo || tempos.isEmpty()) return
        val pedido = geracao.get()
        val centro = indiceMaisProximo(posicaoAlvoMs)
        // Janela maior no arraste: o que entra na tela precisa de miniatura, não placeholder preto.
        for (indice in (centro - 6)..(centro + 6)) {
            if (indice !in tempos.indices) continue
            if (temQuadro.getOrNull(indice) == true) continue
            val tempo = tempos[indice]
            val cached = CacheDeQuadrosDaFilmstripDoScrubFinoPorToqueLongoDoPlayerDeVideo.obter(
                chaveDoVideo = chaveDoVideoAtual,
                tempoMs = tempo,
                passoMs = passoDaFilmstripMs,
            )
            if (cached != null) {
                aplicarQuadro(pedido, indice, cached, guardarNoCache = false)
                continue
            }
            if (!pedindoQuadro.add(indice)) continue
            capturarQuadroAvulso(tempo, miniaturaLargura, miniaturaAltura) { bitmap ->
                principal.post {
                    pedindoQuadro.remove(indice)
                    if (pedido != geracao.get() || !ativo) return@post
                    if (bitmap != null) aplicarQuadro(pedido, indice, bitmap)
                }
            }
        }
    }

    private fun aplicarQuadro(
        pedido: Int,
        indice: Int,
        bitmap: Bitmap?,
        guardarNoCache: Boolean = true,
    ) {
        if (pedido != geracao.get() || !ativo) return
        if (indice !in vistas.indices || bitmap == null || bitmap.isRecycled) return
        if (bitmapQuasePreto(bitmap)) return
        vistas[indice].setImageBitmap(bitmap)
        vistas[indice].scaleType = ImageView.ScaleType.CENTER_CROP
        if (indice in temQuadro.indices) temQuadro[indice] = true
        if (guardarNoCache && indice in tempos.indices && chaveDoVideoAtual.isNotEmpty()) {
            CacheDeQuadrosDaFilmstripDoScrubFinoPorToqueLongoDoPlayerDeVideo.guardar(
                chaveDoVideo = chaveDoVideoAtual,
                tempoMs = tempos[indice],
                passoMs = passoDaFilmstripMs,
                bitmap = bitmap,
            )
        }
    }

    private fun bitmapQuasePreto(bitmap: Bitmap): Boolean {
        val largura = bitmap.width.coerceAtLeast(1)
        val altura = bitmap.height.coerceAtLeast(1)
        val passoX = (largura / 8).coerceAtLeast(1)
        val passoY = (altura / 8).coerceAtLeast(1)
        var amostras = 0
        var soma = 0L
        var y = 0
        while (y < altura) {
            var x = 0
            while (x < largura) {
                val cor = bitmap.getPixel(x, y)
                soma += ((cor shr 16) and 0xFF) + ((cor shr 8) and 0xFF) + (cor and 0xFF)
                amostras++
                x += passoX
            }
            y += passoY
        }
        return amostras == 0 || (soma / amostras) < 18
    }

    private fun indiceMaisProximo(tempoMs: Long): Int {
        if (tempos.isEmpty()) return 0
        var melhor = 0
        var menor = Long.MAX_VALUE
        tempos.forEachIndexed { indice, tempo ->
            val d = kotlin.math.abs(tempo - tempoMs)
            if (d < menor) {
                menor = d
                melhor = indice
            }
        }
        return melhor
    }

    private fun atualizarRotulo() {
        rotuloDoTempo.text = ScrubFinoPorToqueLongoNoPlayerDeVideo.formatarTempo(posicaoAlvoMs)
    }
}

package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.util.TypedValue
import android.view.Gravity
import android.os.Looper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TextureView
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.chromebrothers.navegador.regras.AcumuloDoAvisoDeDeslocamentoDoPlayerDeVideo
import com.chromebrothers.navegador.regras.AjusteDeVelocidadeDeReproducaoDoVideo
import com.chromebrothers.navegador.regras.IncrementosDeDeslocamentoDoPlayerDeVideo
import com.chromebrothers.navegador.regras.RegrasDaJanelaFlutuanteDoPlayerDeVideoSobreANavegacao
import com.chromebrothers.navegador.regras.RegrasDaJanelaFlutuanteDoPlayerDeVideoSobreANavegacao.Retangulo
import com.chromebrothers.navegador.regras.RegrasDoSwipeEntreVideosNoPlayerEmbutidoDoVisorDaColecao
import com.chromebrothers.navegador.regras.RegrasDoSwipeEntreVideosNoPlayerEmbutidoDoVisorDaColecao.Gesto
import com.chromebrothers.navegador.regras.RegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat
import com.chromebrothers.navegador.regras.ToqueDuploNoPlayerDeVideo
import java.io.ByteArrayOutputStream

class VideoDoVisorAoLado(val uri: String, val capa: Bitmap?)

/**
 * Vídeos (componente de geração MCP ou grade do Visualizador de mídia) abertos por cima da página, no player nativo embutido
 * (velocidade, incremento, toque duplo, scrub fino, captura de quadro, favorito).
 * O swipe horizontal troca de vídeo; os vizinhos aparecem pela capa enquanto o dedo arrasta.
 */
class VisorDeVideosAoLadoComPlayerNativoEmbutidoNaColecao(
    private val activity: AppCompatActivity,
    private val host: FrameLayout,
    private val preferencias: PreferenciasVisuaisEDeNavegacaoDoChromebrothers,
    private val aoCapturarQuadro: (png: ByteArray, salvarNaGaleria: Boolean) -> Unit,
    private val estaFavorito: (uri: String) -> Boolean,
    private val alternarFavorito: (uri: String) -> Unit,
    private val aoMudarVideo: (uri: String) -> Unit,
    private val aoFechar: () -> Unit,
    private val aoPedirTelaCheia: (uri: String, posicaoMs: Long, velocidade: Double) -> Unit,
    /** Mudou entre ocupar a área da página (false) e flutuar (true). */
    private val aoMudarModo: (flutuante: Boolean) -> Unit = {},
) {
    private val regrasDaTroca = RegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat
    private val regrasDoSwipe = RegrasDoSwipeEntreVideosNoPlayerEmbutidoDoVisorDaColecao
    private val folga = ViewConfiguration.get(activity).scaledTouchSlop.toFloat()
    private val esperaDoToqueLongoMs = ViewConfiguration.getLongPressTimeout().toLong()
    private val relogio = Handler(Looper.getMainLooper())
    private val acumuloDoDeslocamento = AcumuloDoAvisoDeDeslocamentoDoPlayerDeVideo()
    private val botoesDeVelocidade = mutableListOf<Pair<Double, Button>>()
    private val botoesDeIncremento = mutableListOf<Pair<Int, Button>>()
    private val regrasDaJanela = RegrasDaJanelaFlutuanteDoPlayerDeVideoSobreANavegacao
    private val densidade = activity.resources.displayMetrics.density
    private val preferenciasDaJanela =
        activity.getSharedPreferences("janela_flutuante_do_player_de_video", Context.MODE_PRIVATE)
    /** Envolve o player: tela inteira da área das páginas ou janela flutuante com alça e canto. */
    private var janela: FrameLayout? = null
    private var alca: View? = null
    private var textoDaAlca: TextView? = null
    private var canto: View? = null
    private var flutuante = false
    private var retangulo: Retangulo? = null
    private var overlay: FrameLayout? = null
    private var palco: View? = null
    private var visor: PlayerView? = null
    private var capa: ImageView? = null
    private var anterior: ImageView? = null
    private var proxima: ImageView? = null
    private var player: ExoPlayer? = null
    private var scrub: ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo? = null
    private var provedorDeQuadros: ProvedorDeQuadrosDoVideoParaScrubFinoDoPlayerDeVideo? = null
    private var videos: List<VideoDoVisorAoLado> = emptyList()
    private var indice = 0
    private var velocidadeAtual = 1.0
    private var posicaoPendenteMs = 0L
    private var incremento = IncrementosDeDeslocamentoDoPlayerDeVideo.PADRAO
    private var arrasteX = 0f
    private var x0 = 0f
    private var y0 = 0f
    private var gesto = Gesto.INDEFINIDO
    private var rastreador: VelocityTracker? = null
    private var animacao: android.animation.ValueAnimator? = null
    private var sessao = 0
    private val esconderAvisoDoDeslocamento = Runnable {
        overlay?.findViewById<TextView>(R.id.aviso_do_deslocamento_do_player_embutido_da_colecao)?.visibility = View.GONE
        acumuloDoDeslocamento.reiniciar()
    }

    fun estaAberto(): Boolean = janela?.parent != null

    fun estaFlutuante(): Boolean = estaAberto() && flutuante

    /** Alterna entre ocupar a área das páginas e flutuar sobre a navegação, sem recarregar o vídeo. */
    fun definirFlutuante(sim: Boolean) {
        val mudou = flutuante != sim
        flutuante = sim
        if (estaAberto()) {
            // Flutuando ele fica visível sobre qualquer aba.
            if (sim) janela?.visibility = View.VISIBLE
            aplicarModo()
        }
        if (mudou || !sim) aoMudarModo(sim)
    }

    /** Modo escolhido (vale mesmo antes de abrir). */
    fun estaFlutuanteNoModo(): Boolean = flutuante

    fun uriAtual(): String? = if (estaAberto()) videos.getOrNull(indice)?.uri else null

    /** [posicaoInicialMs] e [velocidade] servem para continuar um vídeo vindo do player de tela cheia. */
    fun mostrar(
        lista: List<VideoDoVisorAoLado>,
        indiceInicial: Int,
        posicaoInicialMs: Long = 0L,
        velocidade: Double? = null,
    ) {
        val validos = lista.filter { it.uri.isNotBlank() }
        if (validos.isEmpty()) return
        animacao?.cancel()
        posicaoPendenteMs = posicaoInicialMs.coerceAtLeast(0L)
        if (velocidade != null) {
            velocidadeAtual = velocidade
            atualizarVelocidadeSelecionada()
        }
        videos = validos
        indice = indiceInicial.coerceIn(0, validos.lastIndex)
        arrasteX = 0f
        incremento = preferencias.incrementoDeDeslocamentoDoVideo
        if (overlay == null) montar()
        val moldura = janela ?: return
        if (moldura.parent == null) {
            host.addView(moldura, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        host.bringChildToFront(moldura)
        moldura.visibility = View.VISIBLE
        aplicarModo()
        if (player == null) criarPlayer()
        tocarAtual()
        visor?.showController()
    }

    /** Volta do sistema: primeiro sai do scrub fino, depois fecha o visor. Flutuando, a volta é da navegação. */
    fun tratarVolta(): Boolean {
        if (!estaAberto()) return false
        if (scrub?.estaAtivo == true) {
            scrub?.sairDoModo()
            return true
        }
        if (flutuante) return false
        return fechar()
    }

    fun pausar() {
        player?.pause()
    }

    /**
     * Ocupando a área da página, o player mora na aba Visor: some (pausado) quando outra aba está ativa.
     * Flutuando ele é de propósito sobre a navegação e não muda.
     */
    fun ocultarPorTrocaDeAba(ocultar: Boolean) {
        val moldura = janela ?: return
        if (moldura.parent == null || flutuante) return
        if (ocultar) {
            player?.pause()
            moldura.visibility = View.GONE
        } else {
            moldura.visibility = View.VISIBLE
        }
    }

    fun fechar(avisar: Boolean = true): Boolean {
        val raiz = janela ?: return false
        if (raiz.parent == null) return false
        sessao++
        animacao?.cancel()
        scrub?.cancelarSemRetomar()
        relogio.removeCallbacks(esconderAvisoDoDeslocamento)
        visor?.player = null
        player?.release()
        player = null
        provedorDeQuadros?.fechar()
        (raiz.parent as? ViewGroup)?.removeView(raiz)
        if (avisar) aoFechar()
        return true
    }

    private fun montar() {
        val raiz = FrameLayout(activity).apply {
            setBackgroundColor(Color.parseColor("#F20A0C10"))
            isClickable = true
            clipChildren = true
        }
        val alcaView = montarAlca()
        val coluna = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        coluna.addView(alcaView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, alturaDaAlca()))
        coluna.addView(raiz, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        val cantoView = TextView(activity).apply {
            text = "◢"
            gravity = Gravity.BOTTOM or Gravity.END
            setTextColor(Color.parseColor("#E6F5C518"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setPadding(0, 0, dp(3), 0)
            contentDescription = activity.getString(R.string.redimensionar_a_janela_flutuante_do_player)
            setOnTouchListener(ArrasteDaJanela(redimensionar = true))
        }
        val moldura = FrameLayout(activity).apply {
            elevation = 30f * densidade
            isClickable = true
        }
        moldura.addView(coluna, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        moldura.addView(cantoView, FrameLayout.LayoutParams(dp(36), dp(36), Gravity.BOTTOM or Gravity.END))
        janela = moldura
        alca = alcaView
        canto = cantoView
        host.addOnLayoutChangeListener { _, l, t, r, b, ol, ot, or, ob ->
            if (!flutuante || !estaAberto()) return@addOnLayoutChangeListener
            if (r - l == or - ol && b - t == ob - ot) return@addOnLayoutChangeListener
            host.post { if (flutuante && estaAberto()) aplicarModo() }
        }
        val anteriorView = capaVizinha()
        val proximaView = capaVizinha()
        val conteudo = LayoutInflater.from(activity)
            .inflate(R.layout.layout_player_nativo_embutido_no_visor_de_videos_da_colecao, raiz, false)
        raiz.addView(anteriorView)
        raiz.addView(proximaView)
        raiz.addView(conteudo)
        raiz.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> aplicarDeslocamento() }
        overlay = raiz
        palco = conteudo
        anterior = anteriorView
        proxima = proximaView
        capa = conteudo.findViewById(R.id.capa_do_player_embutido_da_colecao)
        val playerView = conteudo.findViewById<PlayerView>(R.id.visor_do_player_embutido_da_colecao)
        visor = playerView
        val barra = conteudo.findViewById<View>(R.id.barra_superior_do_player_embutido_da_colecao)
        val setaAnterior = conteudo.findViewById<View>(R.id.seta_video_anterior_do_player_embutido_da_colecao)
        val setaProxima = conteudo.findViewById<View>(R.id.seta_proximo_video_do_player_embutido_da_colecao)
        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibilidade ->
            barra.visibility = visibilidade
            atualizarSetas(visibilidade == View.VISIBLE)
        })
        setaAnterior.setOnClickListener { animarAte(indice - 1) }
        setaProxima.setOnClickListener { animarAte(indice + 1) }
        conteudo.findViewById<ImageButton>(R.id.botao_fechar_player_embutido_da_colecao).setOnClickListener { fechar() }
        conteudo.findViewById<ImageButton>(R.id.botao_janela_flutuante_do_player_embutido_da_colecao).setOnClickListener {
            definirFlutuante(!flutuante)
            playerView.showController()
        }
        conteudo.findViewById<ImageButton>(R.id.botao_tela_cheia_do_player_embutido_da_colecao).setOnClickListener {
            val uri = uriAtual() ?: return@setOnClickListener
            val posicao = player?.currentPosition ?: 0L
            val velocidade = velocidadeAtual
            fechar()
            aoPedirTelaCheia(uri, posicao, velocidade)
        }
        conteudo.findViewById<ImageButton>(R.id.botao_salvar_quadro_do_player_embutido_da_colecao).setOnClickListener {
            playerView.showController()
            capturarQuadro(salvarNaGaleria = true)
        }
        conteudo.findViewById<ImageButton>(R.id.botao_copiar_quadro_do_player_embutido_da_colecao).setOnClickListener {
            playerView.showController()
            capturarQuadro(salvarNaGaleria = false)
        }
        conteudo.findViewById<ImageButton>(R.id.botao_favoritar_video_do_player_embutido_da_colecao).setOnClickListener {
            playerView.showController()
            uriAtual()?.let { alternarFavorito(it) }
            atualizarEstrela()
        }
        montarVelocidades(conteudo.findViewById(R.id.lista_de_velocidades_do_player_embutido_da_colecao))
        montarIncrementos(conteudo)
        prepararGestos(playerView, conteudo)
    }

    private fun alturaDaAlca(): Int = dp(36)

    private fun dp(v: Int): Int = (v * densidade).toInt()

    private fun montarAlca(): LinearLayout {
        val linha = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#F2151820"))
            setPadding(dp(10), 0, dp(2), 0)
            contentDescription = activity.getString(R.string.arraste_para_mover_a_janela_flutuante_do_player)
            visibility = View.GONE
            setOnTouchListener(ArrasteDaJanela(redimensionar = false))
        }
        linha.addView(TextView(activity).apply {
            text = "⠿"
            setTextColor(Color.parseColor("#8B91A1"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        })
        val texto = TextView(activity).apply {
            setTextColor(Color.parseColor("#E8EAF0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(dp(8), 0, dp(8), 0)
        }
        textoDaAlca = texto
        linha.addView(texto, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        linha.addView(botaoDaAlca("⤢", R.string.voltar_a_janela_flutuante_para_o_tamanho_da_area) { definirFlutuante(false) })
        linha.addView(botaoDaAlca("✕", R.string.fechar_player_embutido_da_colecao) { fechar() })
        return linha
    }

    private fun botaoDaAlca(simbolo: String, descricao: Int, acao: () -> Unit): TextView = TextView(activity).apply {
        text = simbolo
        gravity = Gravity.CENTER
        setTextColor(Color.parseColor("#F4F7FB"))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
        contentDescription = activity.getString(descricao)
        minWidth = dp(40)
        minHeight = dp(36)
        setOnClickListener { acao() }
    }

    private fun aplicarModo() {
        val moldura = janela ?: return
        val raiz = overlay ?: return
        alca?.visibility = if (flutuante) View.VISIBLE else View.GONE
        canto?.visibility = if (flutuante) View.VISIBLE else View.GONE
        raiz.findViewById<ImageButton>(R.id.botao_janela_flutuante_do_player_embutido_da_colecao)?.contentDescription =
            activity.getString(
                if (flutuante) R.string.voltar_a_janela_flutuante_para_o_tamanho_da_area else R.string.abrir_em_janela_flutuante_o_player_de_video,
            )
        if (!flutuante) {
            raiz.setBackgroundColor(Color.parseColor("#F20A0C10"))
            moldura.background = null
            moldura.clipToOutline = false
            moldura.translationX = 0f
            moldura.translationY = 0f
            moldura.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            return
        }
        raiz.setBackgroundColor(Color.BLACK)
        moldura.background = GradientDrawable().apply {
            cornerRadius = 12f * densidade
            setColor(Color.BLACK)
            setStroke(dp(1), Color.parseColor("#3A3F4B"))
        }
        moldura.outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
        moldura.clipToOutline = true
        if (host.width <= 0 || host.height <= 0) {
            host.post { if (flutuante && estaAberto()) aplicarModo() }
            return
        }
        val base = retangulo
            ?: regrasDaJanela.deTexto(preferenciasDaJanela.getString(CHAVE_DO_RETANGULO, null))
            ?: regrasDaJanela.inicial(host.width, host.height, densidade, alturaDaAlca())
        retangulo = regrasDaJanela.ajustar(base, host.width, host.height, densidade)
        aplicarRetangulo(redimensionou = true)
    }

    private fun aplicarRetangulo(redimensionou: Boolean) {
        val moldura = janela ?: return
        val r = retangulo ?: return
        if (redimensionou) {
            val params = (moldura.layoutParams as? FrameLayout.LayoutParams)
                ?: FrameLayout.LayoutParams(r.largura, r.altura)
            if (params.width != r.largura || params.height != r.altura || params.gravity != (Gravity.TOP or Gravity.START)) {
                params.width = r.largura
                params.height = r.altura
                params.gravity = Gravity.TOP or Gravity.START
                moldura.layoutParams = params
            }
        }
        moldura.translationX = r.x.toFloat()
        moldura.translationY = r.y.toFloat()
    }

    /** Alça move a janela; o canto ◢ redimensiona. Os dois ficam fora do PlayerView, sem disputar gesto. */
    private inner class ArrasteDaJanela(private val redimensionar: Boolean) : View.OnTouchListener {
        private var x0 = 0f
        private var y0 = 0f
        private var inicio: Retangulo? = null

        override fun onTouch(v: View, evento: MotionEvent): Boolean {
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    x0 = evento.rawX
                    y0 = evento.rawY
                    inicio = retangulo
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    val base = inicio ?: return true
                    val dx = (evento.rawX - x0).toInt()
                    val dy = (evento.rawY - y0).toInt()
                    retangulo = if (redimensionar) {
                        regrasDaJanela.redimensionar(base, dx, dy, host.width, host.height, densidade)
                    } else {
                        regrasDaJanela.mover(base, dx, dy, host.width, host.height, densidade)
                    }
                    aplicarRetangulo(redimensionou = redimensionar)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    inicio = null
                    retangulo?.let {
                        preferenciasDaJanela.edit().putString(CHAVE_DO_RETANGULO, regrasDaJanela.paraTexto(it)).apply()
                    }
                }
            }
            return true
        }
    }

    private fun capaVizinha(): ImageView = ImageView(activity).apply {
        scaleType = ImageView.ScaleType.FIT_CENTER
        setBackgroundColor(Color.BLACK)
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun criarPlayer() {
        val reprodutor = ExoPlayer.Builder(activity)
            .setSeekBackIncrementMs(incremento * 1000L)
            .setSeekForwardIncrementMs(incremento * 1000L)
            .build()
        reprodutor.addListener(object : Player.Listener {
            override fun onRenderedFirstFrame() {
                capa?.visibility = View.GONE
            }

            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(activity, R.string.falha_ao_reproduzir_no_player_nativo, Toast.LENGTH_LONG).show()
            }

            override fun onEvents(player: Player, events: Player.Events) {
                visor?.post { if (this@VisorDeVideosAoLadoComPlayerNativoEmbutidoNaColecao.player != null) aplicarIncrementoNoVisor() }
            }
        })
        player = reprodutor
        visor?.player = reprodutor
        aplicarIncrementoNoVisor()
    }

    private fun tocarAtual() {
        val video = videos.getOrNull(indice) ?: return
        scrub?.cancelarSemRetomar()
        provedorDeQuadros?.fechar()
        capa?.setImageBitmap(video.capa)
        capa?.visibility = if (video.capa != null) View.VISIBLE else View.GONE
        anterior?.setImageBitmap(videos.getOrNull(indice - 1)?.capa)
        proxima?.setImageBitmap(videos.getOrNull(indice + 1)?.capa)
        player?.apply {
            setMediaItem(MediaItem.fromUri(video.uri), posicaoPendenteMs)
            posicaoPendenteMs = 0L
            setPlaybackSpeed(AjusteDeVelocidadeDeReproducaoDoVideo.ajustar(velocidadeAtual).toFloat())
            prepare()
            play()
        }
        overlay?.findViewById<TextView>(R.id.posicao_do_player_embutido_da_colecao)?.text =
            regrasDoSwipe.textoDaPosicao(indice, videos.size)
        textoDaAlca?.text = listOf(
            regrasDoSwipe.textoDaPosicao(indice, videos.size),
            android.net.Uri.decode(video.uri.substringBefore('?').substringAfterLast('/')),
        ).filter { it.isNotBlank() }.joinToString(" · ")
        atualizarEstrela()
        atualizarSetas(visor?.isControllerFullyVisible == true)
        aplicarDeslocamento()
        aoMudarVideo(video.uri)
    }

    private fun atualizarSetas(controlesVisiveis: Boolean) {
        val raiz = overlay ?: return
        raiz.findViewById<View>(R.id.seta_video_anterior_do_player_embutido_da_colecao).visibility =
            if (controlesVisiveis && indice > 0) View.VISIBLE else View.GONE
        raiz.findViewById<View>(R.id.seta_proximo_video_do_player_embutido_da_colecao).visibility =
            if (controlesVisiveis && indice < videos.lastIndex) View.VISIBLE else View.GONE
    }

    private fun atualizarEstrela() {
        val botao = overlay?.findViewById<ImageButton>(R.id.botao_favoritar_video_do_player_embutido_da_colecao) ?: return
        val favorito = uriAtual()?.let(estaFavorito) == true
        botao.setImageResource(
            if (favorito) R.drawable.icone_de_video_favorito_preenchido else R.drawable.icone_de_video_favorito_contorno,
        )
        botao.contentDescription = activity.getString(
            if (favorito) R.string.remover_video_dos_favoritos else R.string.favoritar_video,
        )
    }

    private fun prepararGestos(playerView: PlayerView, conteudo: View) {
        val painelScrub = conteudo.findViewById<View>(R.id.inclusao_da_faixa_de_scrub_fino_do_player_embutido_da_colecao)
        val extrator = ExtratorDeFilmstripDoScrubFinoPeloExoPlayerDoNavegador(
            obterPlayer = { player },
            obterTextura = { primeiraTextura(playerView) },
        )
        val provedor = ProvedorDeQuadrosDoVideoParaScrubFinoDoPlayerDeVideo(
            activity = activity,
            obterEndereco = { uriAtual().orEmpty() },
        )
        provedorDeQuadros = provedor
        val controlador = ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo(
            activity = activity,
            painel = painelScrub,
            faixa = painelScrub.findViewById(R.id.faixa_de_miniaturas_do_scrub_fino_por_toque_longo_do_player_de_video),
            rotuloDoTempo = painelScrub.findViewById(R.id.texto_do_tempo_do_scrub_fino_por_toque_longo_do_player_de_video),
            obterPosicaoMs = { player?.currentPosition ?: 0L },
            obterDuracaoMs = { (player?.duration ?: 0L).coerceAtLeast(0L) },
            seekParaMs = { ms ->
                player?.let { reprodutor ->
                    val limite = reprodutor.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
                    reprodutor.seekTo(ms.coerceIn(0L, limite))
                }
            },
            estavaTocando = { player?.isPlaying == true },
            pausar = { player?.pause() },
            retomar = { player?.play() },
            aoEntrar = {
                playerView.hideController()
                provedor.abrir()
            },
            aoSair = {
                extrator.cancelar()
                provedor.fechar()
                playerView.showController()
            },
            preencherFilmstrip = { tempos, _, _, indiceCentral, aindaAtivo, aoQuadro, aoFim ->
                provedor.preencher(
                    tempos = tempos,
                    aindaAtivo = aindaAtivo,
                    aoQuadro = aoQuadro,
                    aoFim = aoFim,
                    indiceCentral = indiceCentral,
                )
            },
            cancelarPreenchimentoDaFilmstrip = {
                extrator.cancelar()
                provedor.cancelar()
            },
            capturarQuadroAvulso = { tempoMs, _, _, aoPronto -> provedor.capturarEm(tempoMs, aoPronto) },
            obterChaveDoVideo = { uriAtual().orEmpty() },
            obterArquivoParaRecorte = { uriAtual() },
        )
        scrub = controlador
        controlador.ligarNaSuperficie(
            superficie = playerView,
            detectorBase = object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    if (playerView.isControllerFullyVisible) playerView.hideController() else playerView.showController()
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val segundos = ToqueDuploNoPlayerDeVideo.segundos(e.x, playerView.width.toFloat(), incremento)
                    if (segundos != 0) deslocar(segundos)
                    return true
                }
            },
            tomarToque = { evento -> tratarSwipe(evento) },
        )
    }

    private fun tratarSwipe(evento: MotionEvent): Boolean {
        when (evento.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                animacao?.cancel()
                x0 = evento.rawX
                y0 = evento.rawY
                gesto = Gesto.INDEFINIDO
                rastreador?.recycle()
                rastreador = VelocityTracker.obtain().also { it.addMovement(evento) }
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                rastreador?.addMovement(evento)
                val dx = evento.rawX - x0
                if (gesto == Gesto.INDEFINIDO) {
                    gesto = regrasDoSwipe.decidir(
                        dx = dx,
                        dy = evento.rawY - y0,
                        folga = folga,
                        decorridoMs = evento.eventTime - evento.downTime,
                        esperaDoToqueLongoMs = esperaDoToqueLongoMs,
                        scrubAtivo = scrub?.estaAtivo == true,
                        ponteiros = evento.pointerCount,
                    )
                    if (gesto == Gesto.SWIPE) visor?.hideController()
                }
                if (gesto != Gesto.SWIPE) return false
                val largura = overlay?.width?.toFloat() ?: return true
                arrasteX = regrasDaTroca.deslocamentoComBorda(dx, indice, videos.size, largura)
                aplicarDeslocamento()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (gesto != Gesto.SWIPE) {
                    rastreador?.recycle()
                    rastreador = null
                    return false
                }
                gesto = Gesto.INDEFINIDO
                rastreador?.addMovement(evento)
                rastreador?.computeCurrentVelocity(1000)
                val velocidade = rastreador?.xVelocity ?: 0f
                rastreador?.recycle()
                rastreador = null
                val largura = overlay?.width?.toFloat() ?: 0f
                val troca = if (evento.actionMasked == MotionEvent.ACTION_UP) {
                    regrasDaTroca.trocaAoSoltar(arrasteX, largura, velocidade, RegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat.ZOOM_MINIMO)
                } else {
                    RegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat.Troca.FICAR
                }
                animarAte(regrasDaTroca.indiceAposTroca(indice, videos.size, troca))
                return true
            }
        }
        return gesto == Gesto.SWIPE
    }

    private fun animarAte(destinoPedido: Int) {
        val destino = destinoPedido.coerceIn(0, (videos.size - 1).coerceAtLeast(0))
        val largura = overlay?.width?.toFloat() ?: return
        val alvo = when {
            destino > indice -> -largura
            destino < indice -> largura
            else -> 0f
        }
        if (destino != indice) player?.pause()
        val sessaoDesta = sessao
        animacao?.cancel()
        var cancelada = false
        animacao = android.animation.ValueAnimator.ofFloat(arrasteX, alvo).apply {
            duration = 260L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                arrasteX = it.animatedValue as Float
                aplicarDeslocamento()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    cancelada = true
                }

                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (cancelada || sessaoDesta != sessao || !estaAberto()) return
                    arrasteX = 0f
                    if (destino != indice) {
                        indice = destino
                        tocarAtual()
                    } else {
                        aplicarDeslocamento()
                    }
                }
            })
            start()
        }
    }

    private fun aplicarDeslocamento() {
        val largura = overlay?.width?.toFloat() ?: return
        if (largura <= 0f) return
        palco?.translationX = arrasteX
        anterior?.translationX = -largura + arrasteX
        proxima?.translationX = largura + arrasteX
        val arrastando = arrasteX != 0f
        anterior?.visibility = if (arrastando && indice > 0) View.VISIBLE else View.INVISIBLE
        proxima?.visibility = if (arrastando && indice < videos.lastIndex) View.VISIBLE else View.INVISIBLE
    }

    private fun deslocar(segundos: Int) {
        val reprodutor = player ?: return
        val aviso = acumuloDoDeslocamento.registrar(segundos, System.currentTimeMillis())
        overlay?.findViewById<TextView>(R.id.aviso_do_deslocamento_do_player_embutido_da_colecao)?.apply {
            text = aviso.texto
            visibility = if (aviso.visivel) View.VISIBLE else View.GONE
        }
        relogio.removeCallbacks(esconderAvisoDoDeslocamento)
        if (aviso.visivel) relogio.postDelayed(esconderAvisoDoDeslocamento, 1200)
        val limite = reprodutor.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
        reprodutor.seekTo((reprodutor.currentPosition + segundos * 1000L).coerceIn(0L, limite))
    }

    private fun aplicarIncrementoNoVisor() {
        val playerView = visor ?: return
        val voltar = playerView.findViewById<TextView>(androidx.media3.ui.R.id.exo_rew_with_amount)
        val avancar = playerView.findViewById<TextView>(androidx.media3.ui.R.id.exo_ffwd_with_amount)
        voltar?.text = incremento.toString()
        avancar?.text = incremento.toString()
        voltar?.setOnClickListener {
            deslocar(-incremento)
            playerView.showController()
        }
        avancar?.setOnClickListener {
            deslocar(incremento)
            playerView.showController()
        }
    }

    private fun montarVelocidades(lista: LinearLayout) {
        val margem = (8 * activity.resources.displayMetrics.density).toInt()
        AjusteDeVelocidadeDeReproducaoDoVideo.opcoes.forEach { taxa ->
            val botao = Button(activity)
            botao.text = if (taxa == taxa.toInt().toDouble()) "${taxa.toInt()}×" else "${taxa}×"
            botao.setOnClickListener {
                velocidadeAtual = taxa
                player?.setPlaybackSpeed(AjusteDeVelocidadeDeReproducaoDoVideo.ajustar(taxa).toFloat())
                atualizarVelocidadeSelecionada()
                visor?.showController()
            }
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.marginEnd = margem
            lista.addView(botao, params)
            botoesDeVelocidade += taxa to botao
        }
        atualizarVelocidadeSelecionada()
    }

    private fun atualizarVelocidadeSelecionada() {
        botoesDeVelocidade.forEach { (taxa, botao) ->
            AparenciaDosBotoesDeVelocidadeDoVideo.aplicar(activity, botao, taxa == velocidadeAtual)
        }
    }

    private fun montarIncrementos(conteudo: View) {
        val lista = conteudo.findViewById<LinearLayout>(R.id.lista_de_incrementos_do_player_embutido_da_colecao)
        val margem = (8 * activity.resources.displayMetrics.density).toInt()
        IncrementosDeDeslocamentoDoPlayerDeVideo.opcoes.forEach { segundos ->
            val botao = Button(activity)
            botao.text = "${segundos}s"
            botao.setOnClickListener {
                incremento = segundos
                preferencias.incrementoDeDeslocamentoDoVideo = segundos
                aplicarIncrementoNoVisor()
                atualizarIncrementoSelecionado()
                visor?.showController()
            }
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.marginEnd = margem
            lista.addView(botao, params)
            botoesDeIncremento += segundos to botao
        }
        atualizarIncrementoSelecionado()
        conteudo.findViewById<ImageButton>(R.id.botao_configurar_incremento_do_player_embutido_da_colecao).setOnClickListener {
            lista.visibility = if (lista.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            visor?.controllerShowTimeoutMs = if (lista.visibility == View.VISIBLE) 5000 else 3000
            visor?.showController()
        }
    }

    private fun atualizarIncrementoSelecionado() {
        botoesDeIncremento.forEach { (segundos, botao) ->
            AparenciaDosBotoesDeVelocidadeDoVideo.aplicar(activity, botao, segundos == incremento)
        }
    }

    private fun capturarQuadro(salvarNaGaleria: Boolean) {
        val quadro = visor?.let { primeiraTextura(it) }?.bitmap
        if (quadro == null) {
            Toast.makeText(activity, R.string.quadro_indisponivel, Toast.LENGTH_LONG).show()
            return
        }
        val saida = ByteArrayOutputStream()
        quadro.compress(Bitmap.CompressFormat.PNG, 100, saida)
        quadro.recycle()
        aoCapturarQuadro(saida.toByteArray(), salvarNaGaleria)
    }

    private fun primeiraTextura(view: View): TextureView? {
        if (view is TextureView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) primeiraTextura(view.getChildAt(i))?.let { return it }
        }
        return null
    }

    private companion object {
        const val CHAVE_DO_RETANGULO = "retangulo"
    }
}

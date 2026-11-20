package com.chromebrothers.navegador

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.chromebrothers.navegador.regras.ClassificacaoDoPedidoDeMidiaParaDownload
import com.chromebrothers.navegador.regras.TipoDePedidoDeMidiaParaDownload
import com.chromebrothers.navegador.regras.AjusteDeVelocidadeDeReproducaoDoVideo
import com.chromebrothers.navegador.regras.AcumuloDoAvisoDeDeslocamentoDoPlayerDeVideo
import com.chromebrothers.navegador.regras.IncrementosDeDeslocamentoDoPlayerDeVideo
import com.chromebrothers.navegador.regras.ToqueDuploNoPlayerDeVideo
import com.chromebrothers.navegador.regras.BibliotecaDeImagensCapturadasDoNavegador
import com.chromebrothers.navegador.regras.BibliotecaDeVideosDoNavegador
import com.chromebrothers.navegador.regras.ItemDaBibliotecaDeVideos
import com.chromebrothers.navegador.regras.MontagemDaIdentidadeDoVideoNaBiblioteca
import java.io.ByteArrayOutputStream

class AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private var velocidadeAtual = 1.0
    private var incremento = IncrementosDeDeslocamentoDoPlayerDeVideo.PADRAO
    private val botoesDeVelocidade = mutableListOf<Pair<Double, Button>>()
    private val botoesDeIncremento = mutableListOf<Pair<Int, Button>>()
    private val acumuloDoDeslocamento = AcumuloDoAvisoDeDeslocamentoDoPlayerDeVideo()
    private lateinit var preferencias: PreferenciasVisuaisEDeNavegacaoDoChromebrothers
    private lateinit var gravacaoDaBibliotecaDeVideos: GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo
    private lateinit var bibliotecaDeVideos: BibliotecaDeVideosDoNavegador
    private lateinit var painelDaBibliotecaDeVideos: PainelDaGavetaDaBibliotecaDeVideos
    private lateinit var gravacaoDaBibliotecaDeImagens: GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo
    private lateinit var bibliotecaDeImagens: BibliotecaDeImagensCapturadasDoNavegador
    private var enderecoAtual = ""
    private var tituloAtual = ""
    private var refererAtual = ""
    private var agenteAtual = ""
    private val pedirPermissaoDeNotificacaoParaODownload =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private var chaveDoVideoAtual: String? = null
    private var tentativasDeMiniatura = 0
    private val relogio = Handler(Looper.getMainLooper())
    private var controladorDoScrubFino: ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo? = null
    private val capturarMiniatura = Runnable { tentarGuardarMiniatura() }
    private val esconderAvisoDoDeslocamento = Runnable {
        findViewById<TextView>(R.id.aviso_do_deslocamento_do_player_nativo)?.visibility = View.GONE
        acumuloDoDeslocamento.reiniciar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_atividade_do_player_nativo_de_video)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        esconderBarrasDoSistema()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raiz_do_player_nativo)) { view, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }
        val endereco = intent.getStringExtra(EXTRA_ENDERECO)
        velocidadeAtual = AjusteDeVelocidadeDeReproducaoDoVideo.ajustar(
            intent.getDoubleExtra(EXTRA_VELOCIDADE, 1.0),
        )
        if (endereco.isNullOrBlank()) {
            finish()
            return
        }
        enderecoAtual = endereco
        tituloAtual = intent.getStringExtra(EXTRA_TITULO).orEmpty()
        refererAtual = intent.getStringExtra(EXTRA_REFERER).orEmpty()
        agenteAtual = intent.getStringExtra(EXTRA_AGENTE).orEmpty()
        gravacaoDaBibliotecaDeVideos = GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo(this)
        preferencias = PreferenciasVisuaisEDeNavegacaoDoChromebrothers(this)
        incremento = preferencias.incrementoDeDeslocamentoDoVideo
        bibliotecaDeVideos = gravacaoDaBibliotecaDeVideos.carregar()
        gravacaoDaBibliotecaDeImagens = GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo(this)
        bibliotecaDeImagens = gravacaoDaBibliotecaDeImagens.importarUltimoQuadroCopiadoSeAindaExistir(
            gravacaoDaBibliotecaDeImagens.carregar(),
        )
        painelDaBibliotecaDeVideos = PainelDaGavetaDaBibliotecaDeVideos(
            activity = this,
            biblioteca = bibliotecaDeVideos,
            gravacao = gravacaoDaBibliotecaDeVideos,
            bibliotecaDeImagens = bibliotecaDeImagens,
            gravacaoDeImagens = gravacaoDaBibliotecaDeImagens,
            aoAbrir = { item -> abrirItemDaBiblioteca(item) },
            aoFavoritoAlterado = { item ->
                if (item?.chave == chaveDoVideoAtual) atualizarEstrela()
            },
            acompanhamento = AcompanhamentoGlobalDosDownloadsDeMidiaNaGaveta.atual,
            aoAbrirDownload = { item -> abrirArquivoBaixado(item.uri, item.titulo) },
        )
        val visor = findViewById<PlayerView>(R.id.visor_do_player_nativo)
        val barraSuperior = findViewById<View>(R.id.barra_superior_do_player_nativo)
        visor.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibilidade ->
            barraSuperior.visibility = visibilidade
        })
        val reprodutor = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(incremento * 1000L)
            .setSeekForwardIncrementMs(incremento * 1000L)
            .build()
        player = reprodutor
        visor.player = reprodutor
        reprodutor.setMediaItem(MediaItem.fromUri(endereco), intent.getLongExtra(EXTRA_POSICAO_MS, 0L).coerceAtLeast(0L))
        reprodutor.setPlaybackSpeed(velocidadeAtual.toFloat())
        reprodutor.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(
                    this@AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro,
                    R.string.falha_ao_reproduzir_no_player_nativo,
                    Toast.LENGTH_LONG,
                ).show()
            }

            override fun onEvents(player: Player, events: Player.Events) {
                visor.post {
                    if (this@AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.player == null) return@post
                    aplicarIncrementoNoVisor(visor)
                }
            }
        })
        reprodutor.prepare()
        reprodutor.play()
        aplicarIncrementoNoVisor(visor)
        prepararGestosDoPlayerNativo(visor)
        montarIncrementosDoPlayerNativo(visor)
        montarVelocidades(findViewById(R.id.lista_de_velocidades_do_player_nativo), reprodutor)
        findViewById<SwitchCompat>(R.id.interruptor_de_captura_de_midia_no_player_nativo).apply {
            isChecked = preferencias.capturarMidiaParaDownload
            setOnCheckedChangeListener { _, ligado ->
                if (ligado == preferencias.capturarMidiaParaDownload) return@setOnCheckedChangeListener
                preferencias.capturarMidiaParaDownload = ligado
                atualizarBotaoDeBaixar()
                manterMenuDoPlayerVisivel()
                Toast.makeText(
                    this@AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro,
                    if (ligado) R.string.captura_de_midia_ligada else R.string.captura_de_midia_desligada,
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
        findViewById<ImageButton>(R.id.botao_voltar_ao_navegador_do_player_nativo).setOnClickListener {
            finish()
        }
        atualizarBotaoDeBaixar()
        findViewById<ImageButton>(R.id.botao_baixar_midia_do_player_nativo).apply {
            // Toque: melhor opção direto para a Galeria. Segurar: lista com resolução/duração/tamanho
            // (vídeo aberto de um site, com as opções que a página detectou); sem opções, baixa o que toca.
            contentDescription = getString(R.string.enviar_video_para_a_galeria)
            tooltipText = getString(R.string.dica_do_botao_enviar_video_para_a_galeria)
            setOnClickListener {
                manterMenuDoPlayerVisivel()
                val candidatos = candidatosDaPagina()
                if (candidatos.isNotEmpty()) {
                    baixarOpcaoDaPagina(
                        candidatos.first(),
                        com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia.GALERIA,
                        alternativas = candidatos.drop(1).take(4),
                    )
                } else {
                    baixarVideoAtual(com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia.GALERIA)
                }
            }
            setOnLongClickListener {
                manterMenuDoPlayerVisivel()
                // Segurar sempre abre a lista (nunca baixa direto): opções da página ou, sem elas,
                // o próprio vídeo que está tocando — com resolução/duração/tamanho para confirmar.
                val videoDeSite = com.chromebrothers.navegador.regras.EnvioDoVideoDoPlayerParaAGaleria.situacao(
                    enderecoAtual,
                    null,
                ) == com.chromebrothers.navegador.regras.SituacaoDoVideoParaAGaleria.BAIXAR_DO_SITE
                when {
                    candidatosDaPagina().isNotEmpty() -> mostrarListaDeOpcoesDaPagina()
                    videoDeSite -> mostrarListaDeOpcoesDaPagina(listOf(pedidoDoVideoAtual()))
                    else -> baixarVideoAtual(com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia.DOWNLOADS)
                }
                true
            }
        }
        findViewById<ImageButton>(R.id.botao_salvar_quadro_do_player_nativo).setOnClickListener {
            manterMenuDoPlayerVisivel()
            capturarQuadro(visor, salvarNaGaleria = true)
        }
        findViewById<ImageButton>(R.id.botao_copiar_quadro_do_player_nativo).setOnClickListener {
            manterMenuDoPlayerVisivel()
            capturarQuadro(visor, salvarNaGaleria = false)
        }
        findViewById<ImageButton>(R.id.botao_favoritar_o_video_da_biblioteca).setOnClickListener {
            manterMenuDoPlayerVisivel()
            alternarFavorito()
        }
        findViewById<ImageButton>(R.id.botao_ver_na_area_da_pagina_do_player_nativo).setOnClickListener {
            devolverParaOPlayerDaNavegacao(MODO_AREA_DA_PAGINA)
        }
        findViewById<ImageButton>(R.id.botao_janela_flutuante_do_player_nativo).setOnClickListener {
            devolverParaOPlayerDaNavegacao(MODO_JANELA_FLUTUANTE)
        }
        findViewById<ImageButton>(R.id.botao_abrir_gaveta_da_biblioteca_de_videos).setOnClickListener {
            manterMenuDoPlayerVisivel()
            painelDaBibliotecaDeVideos.mostrar()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (controladorDoScrubFino?.estaAtivo == true) {
                    controladorDoScrubFino?.sairDoModo()
                    return
                }
                if (!painelDaBibliotecaDeVideos.tratarVolta()) finish()
            }
        })
        registrarVideoAtual()
        agendarMiniatura()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) esconderBarrasDoSistema()
    }

    /** Vídeo sem barra de status, softkeys nem barra de tarefas; voltam por alguns segundos ao deslizar da borda. */
    private fun esconderBarrasDoSistema() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onResume() {
        super.onResume()
        esconderBarrasDoSistema()
        if (::bibliotecaDeVideos.isInitialized) {
            bibliotecaDeVideos.substituirPor(gravacaoDaBibliotecaDeVideos.carregar().listar())
            if (::bibliotecaDeImagens.isInitialized) {
                bibliotecaDeImagens.substituirPor(gravacaoDaBibliotecaDeImagens.carregar().listar())
            }
            atualizarEstrela()
            if (painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.atualizar()
            val visor = findViewById<PlayerView>(R.id.visor_do_player_nativo)
            incremento = preferencias.incrementoDeDeslocamentoDoVideo
            aplicarIncrementoNoVisor(visor)
            atualizarIncrementoSelecionado()
        }
    }

    private fun aplicarIncrementoNoVisor(visor: PlayerView) {
        val voltar = visor.findViewById<TextView>(androidx.media3.ui.R.id.exo_rew_with_amount)
        val avancar = visor.findViewById<TextView>(androidx.media3.ui.R.id.exo_ffwd_with_amount)
        val rotulo = incremento.toString()
        voltar?.text = rotulo
        avancar?.text = rotulo
        voltar?.setOnClickListener {
            deslocarPlayerNativo(-incremento)
            visor.showController()
        }
        avancar?.setOnClickListener {
            deslocarPlayerNativo(incremento)
            visor.showController()
        }
    }

    private fun prepararGestosDoPlayerNativo(visor: PlayerView) {
        val painelScrub = findViewById<View>(R.id.inclusao_da_faixa_de_scrub_fino_do_player_nativo)
        val faixa = findViewById<LinearLayout>(
            R.id.faixa_de_miniaturas_do_scrub_fino_por_toque_longo_do_player_de_video,
        )
        val rotulo = findViewById<TextView>(
            R.id.texto_do_tempo_do_scrub_fino_por_toque_longo_do_player_de_video,
        )
        val extratorDaFilmstrip = ExtratorDeFilmstripDoScrubFinoPeloExoPlayerDoNavegador(
            obterPlayer = { player },
            obterTextura = { primeiraTextura(visor) },
        )
        val provedorMmr = ProvedorDeQuadrosDoVideoParaScrubFinoDoPlayerDeVideo(
            activity = this,
            obterEndereco = { enderecoAtual },
            obterReferer = { refererAtual },
            obterAgente = { agenteAtual },
        )
        val controlador = ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo(
            activity = this,
            painel = painelScrub,
            faixa = faixa,
            rotuloDoTempo = rotulo,
            obterPosicaoMs = { player?.currentPosition ?: 0L },
            obterDuracaoMs = {
                val duracao = player?.duration ?: 0L
                if (duracao > 0L) duracao else 0L
            },
            seekParaMs = { ms ->
                val reprodutor = player ?: return@ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo
                val limite = reprodutor.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
                reprodutor.seekTo(ms.coerceIn(0L, limite))
            },
            estavaTocando = { player?.isPlaying == true },
            pausar = { player?.pause() },
            retomar = { player?.play() },
            aoEntrar = {
                visor.hideController()
                provedorMmr.abrir()
            },
            aoSair = {
                extratorDaFilmstrip.cancelar()
                provedorMmr.fechar()
                visor.showController()
            },
            preencherFilmstrip = { tempos, _, _, indiceCentral, aindaAtivo, aoQuadro, aoFim ->
                // Só MMR (centro → bordas). Seek no Exo apagava a tela e não gerava miniatura.
                provedorMmr.preencher(
                    tempos = tempos,
                    aindaAtivo = aindaAtivo,
                    aoQuadro = aoQuadro,
                    aoFim = aoFim,
                    indiceCentral = indiceCentral,
                )
            },
            cancelarPreenchimentoDaFilmstrip = {
                extratorDaFilmstrip.cancelar()
                provedorMmr.cancelar()
            },
            capturarQuadroAvulso = { tempoMs, _, _, aoPronto ->
                provedorMmr.capturarEm(tempoMs, aoPronto)
            },
            obterChaveDoVideo = { enderecoAtual },
        )
        controladorDoScrubFino = controlador
        controlador.ligarNaSuperficie(
            superficie = visor,
            detectorBase = object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    if (visor.isControllerFullyVisible) visor.hideController() else visor.showController()
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val segundos = ToqueDuploNoPlayerDeVideo.segundos(e.x, visor.width.toFloat(), incremento)
                    if (segundos != 0) deslocarPlayerNativo(segundos)
                    return true
                }
            },
        )
    }

    private fun deslocarPlayerNativo(segundos: Int) {
        val reprodutor = player ?: return
        val aviso = acumuloDoDeslocamento.registrar(segundos, System.currentTimeMillis())
        val rotulo = findViewById<TextView>(R.id.aviso_do_deslocamento_do_player_nativo)
        rotulo.text = aviso.texto
        rotulo.visibility = if (aviso.visivel) View.VISIBLE else View.GONE
        relogio.removeCallbacks(esconderAvisoDoDeslocamento)
        if (aviso.visivel) relogio.postDelayed(esconderAvisoDoDeslocamento, 1200)
        val duracao = reprodutor.duration
        val limite = if (duracao > 0L) duracao else Long.MAX_VALUE
        reprodutor.seekTo((reprodutor.currentPosition + segundos * 1000L).coerceIn(0L, limite))
    }

    private fun montarIncrementosDoPlayerNativo(visor: PlayerView) {
        val lista = findViewById<LinearLayout>(R.id.lista_de_incrementos_do_player_nativo)
        val margem = (8 * resources.displayMetrics.density).toInt()
        IncrementosDeDeslocamentoDoPlayerDeVideo.opcoes.forEach { segundos ->
            val botao = Button(this)
            botao.text = "${segundos}s"
            botao.setOnClickListener {
                incremento = segundos
                preferencias.incrementoDeDeslocamentoDoVideo = segundos
                aplicarIncrementoNoVisor(visor)
                atualizarIncrementoSelecionado()
                visor.showController()
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            params.marginEnd = margem
            lista.addView(botao, params)
            botoesDeIncremento += segundos to botao
        }
        atualizarIncrementoSelecionado()
        findViewById<ImageButton>(R.id.botao_configurar_incremento_do_player_nativo).setOnClickListener {
            lista.visibility = if (lista.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            visor.controllerShowTimeoutMs = if (lista.visibility == View.VISIBLE) 5000 else 3000
            visor.showController()
        }
    }

    private fun atualizarIncrementoSelecionado() {
        botoesDeIncremento.forEach { (segundos, botao) ->
            AparenciaDosBotoesDeVelocidadeDoVideo.aplicar(this, botao, segundos == incremento)
        }
    }

    private fun montarVelocidades(lista: LinearLayout, reprodutor: ExoPlayer) {
        val margem = (8 * resources.displayMetrics.density).toInt()
        AjusteDeVelocidadeDeReproducaoDoVideo.opcoes.forEach { taxa ->
            val botao = Button(this)
            botao.text = if (taxa == taxa.toInt().toDouble()) "${taxa.toInt()}×" else "${taxa}×"
            botao.setOnClickListener {
                velocidadeAtual = taxa
                reprodutor.setPlaybackSpeed(AjusteDeVelocidadeDeReproducaoDoVideo.ajustar(taxa).toFloat())
                atualizarVelocidadeSelecionada()
                manterMenuDoPlayerVisivel()
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            params.marginEnd = margem
            lista.addView(botao, params)
            botoesDeVelocidade += taxa to botao
        }
        atualizarVelocidadeSelecionada()
    }

    private fun manterMenuDoPlayerVisivel() {
        findViewById<PlayerView>(R.id.visor_do_player_nativo)?.showController()
    }

    private fun atualizarVelocidadeSelecionada() {
        botoesDeVelocidade.forEach { (taxa, botao) ->
            AparenciaDosBotoesDeVelocidadeDoVideo.aplicar(this, botao, taxa == velocidadeAtual)
        }
    }

    private fun registrarQuadroCapturado(
        png: ByteArray,
        naGaleria: Boolean,
    ): com.chromebrothers.navegador.regras.ItemDaBibliotecaDeImagensCapturadas? {
        if (!::bibliotecaDeImagens.isInitialized) return null
        val item = gravacaoDaBibliotecaDeImagens.guardarQuadro(bibliotecaDeImagens, png, naGaleria)
        if (painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.atualizar()
        return item
    }

    private fun copiarQuadroERegistrar(png: ByteArray) {
        val item = if (::bibliotecaDeImagens.isInitialized) {
            gravacaoDaBibliotecaDeImagens.guardarQuadro(bibliotecaDeImagens, png, naGaleria = false)
        } else {
            null
        }
        if (item != null) {
            gravacaoDaBibliotecaDeImagens.copiarItem(this, bibliotecaDeImagens, item)
        } else {
            GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.copiarParaAreaDeTransferencia(this, png)
        }
        if (painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.atualizar()
    }

    private fun capturarQuadro(visor: PlayerView, salvarNaGaleria: Boolean) {
        val textura = primeiraTextura(visor)
        val quadro = textura?.bitmap
        if (quadro == null) {
            Toast.makeText(this, R.string.quadro_indisponivel, Toast.LENGTH_LONG).show()
            return
        }
        val saida = ByteArrayOutputStream()
        quadro.compress(Bitmap.CompressFormat.PNG, 100, saida)
        val png = saida.toByteArray()
        try {
            if (salvarNaGaleria) {
                val publicada = GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.salvarNaGaleria(this, png)
                val item = registrarQuadroCapturado(png, naGaleria = true)
                if (item != null) {
                    gravacaoDaBibliotecaDeImagens.lembrarGaleria(
                        bibliotecaDeImagens,
                        item.id,
                        publicada.uri.toString(),
                        publicada.nome,
                    )
                }
                Toast.makeText(this, R.string.quadro_salvo, Toast.LENGTH_LONG).show()
            } else {
                copiarQuadroERegistrar(png)
                Toast.makeText(this, R.string.quadro_copiado, Toast.LENGTH_LONG).show()
            }
        } catch (_: Exception) {
            Toast.makeText(this, R.string.quadro_indisponivel, Toast.LENGTH_LONG).show()
        }
    }

    private fun miniaturaDaTextura(textura: TextureView): Bitmap? {
        val largura = textura.width
        val altura = textura.height
        if (largura <= 0 || altura <= 0) return null
        val destinoLargura = 960
        if (largura <= destinoLargura) return textura.bitmap
        val destinoAltura = (altura.toLong() * destinoLargura / largura).toInt().coerceAtLeast(1)
        return textura.getBitmap(destinoLargura, destinoAltura)
    }

    private fun primeiraTextura(view: View): TextureView? {
        if (view is TextureView) return view
        if (view is ViewGroup) {
            for (indice in 0 until view.childCount) {
                primeiraTextura(view.getChildAt(indice))?.let { return it }
            }
        }
        return null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        findViewById<View>(R.id.raiz_do_player_nativo)?.let { ViewCompat.requestApplyInsets(it) }
    }

    override fun onStop() {
        painelDaBibliotecaDeVideos.guardarRascunhoSeEstiverEditando()
        player?.pause()
        super.onStop()
    }

    override fun onDestroy() {
        apagarCopiasInternasJaEnviadasAGaleria()
        relogio.removeCallbacks(esconderAvisoDoDeslocamento)
        relogio.removeCallbacks(capturarMiniatura)
        findViewById<PlayerView>(R.id.visor_do_player_nativo)?.player = null
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun abrirItemDaBiblioteca(item: ItemDaBibliotecaDeVideos) {
        painelDaBibliotecaDeVideos.esconder()
        val uri = uriLocalSeCaminhoAbsoluto(item.enderecoParaAbrir)
        val arquivoLocal = uri.startsWith("file:", ignoreCase = true) ||
            uri.startsWith("content:", ignoreCase = true) ||
            uri.startsWith("/")
        if (item.abreNoPlayerDeArquivo || arquivoLocal) {
            if (uri == enderecoAtual) return
            enderecoAtual = uri
            tituloAtual = item.titulo
            player?.setMediaItem(MediaItem.fromUri(uri))
            player?.prepare()
            player?.play()
            registrarVideoAtual()
            agendarMiniatura()
        } else {
            bibliotecaDeVideos.registrar(
                item.chave,
                item.titulo,
                item.enderecoParaAbrir,
                false,
                System.currentTimeMillis(),
            )
            gravacaoDaBibliotecaDeVideos.gravar(bibliotecaDeVideos)
            setResult(RESULT_OK, Intent().putExtra(EXTRA_ABRIR_PAGINA, item.enderecoParaAbrir))
            finish()
        }
    }

    /** Fecha a tela cheia e pede à tela principal para seguir com o vídeo do mesmo ponto, na página ou flutuando. */
    private fun devolverParaOPlayerDaNavegacao(modo: String) {
        val endereco = enderecoAtual.ifBlank { return }
        val posicao = player?.currentPosition ?: 0L
        player?.pause()
        setResult(
            RESULT_OK,
            Intent()
                .putExtra(EXTRA_MODO_DO_PLAYER_NA_NAVEGACAO, modo)
                .putExtra(EXTRA_ENDERECO, endereco)
                .putExtra(EXTRA_POSICAO_MS, posicao)
                .putExtra(EXTRA_VELOCIDADE, velocidadeAtual),
        )
        finish()
    }

    private fun uriLocalSeCaminhoAbsoluto(endereco: String): String {
        val t = endereco.trim()
        if (t.startsWith("file:", ignoreCase = true) || t.startsWith("content:", ignoreCase = true)) {
            return t
        }
        if (t.startsWith("/")) return java.io.File(t).toURI().toString()
        return t
    }

    private fun abrirArquivoBaixado(uri: String, titulo: String) {
        painelDaBibliotecaDeVideos.esconder()
        if (uri == enderecoAtual) return
        enderecoAtual = uri
        tituloAtual = titulo
        chaveDoVideoAtual = null
        atualizarEstrela()
        player?.setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
        player?.prepare()
        player?.play()
    }

    private fun registrarVideoAtual() {
        val identidade = MontagemDaIdentidadeDoVideoNaBiblioteca.montar(
            enderecoAtual,
            enderecoAtual,
            tituloAtual,
        ) ?: return
        val item = bibliotecaDeVideos.registrar(
            identidade.chave,
            identidade.titulo,
            identidade.enderecoParaAbrir,
            identidade.abreNoPlayerDeArquivo,
            System.currentTimeMillis(),
        )
        gravacaoDaBibliotecaDeVideos.gravar(bibliotecaDeVideos)
        chaveDoVideoAtual = item.chave
        tituloAtual = item.titulo
        atualizarEstrela()
    }

    private fun alternarFavorito() {
        val chave = chaveDoVideoAtual ?: return
        val atual = bibliotecaDeVideos.buscar(chave) ?: return
        bibliotecaDeVideos.definirFavorito(chave, !atual.favorito)
        gravacaoDaBibliotecaDeVideos.gravar(bibliotecaDeVideos)
        atualizarEstrela()
        if (painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.atualizar()
    }

    private fun atualizarEstrela() {
        if (!::bibliotecaDeVideos.isInitialized) return
        val favorito = chaveDoVideoAtual?.let { bibliotecaDeVideos.buscar(it)?.favorito } == true
        val botao = findViewById<ImageButton>(R.id.botao_favoritar_o_video_da_biblioteca) ?: return
        botao.setImageResource(
            if (favorito) {
                R.drawable.icone_de_video_favorito_preenchido
            } else {
                R.drawable.icone_de_video_favorito_contorno
            },
        )
        botao.contentDescription = getString(
            if (favorito) R.string.remover_video_dos_favoritos else R.string.favoritar_video,
        )
    }

    private fun agendarMiniatura() {
        tentativasDeMiniatura = 0
        relogio.removeCallbacks(capturarMiniatura)
        relogio.postDelayed(capturarMiniatura, 900)
    }

    private fun tentarGuardarMiniatura() {
        val chave = chaveDoVideoAtual ?: return
        val visor = findViewById<PlayerView>(R.id.visor_do_player_nativo) ?: return
        val quadro = try {
            primeiraTextura(visor)?.let { miniaturaDaTextura(it) }
        } catch (_: RuntimeException) {
            null
        }
        val guardou = quadro != null && guardarMiniatura(chave, quadro)
        if (quadro != null && !quadro.isRecycled) quadro.recycle()
        if (!guardou && tentativasDeMiniatura < 1) {
            tentativasDeMiniatura += 1
            relogio.postDelayed(capturarMiniatura, 1200)
        }
    }

    private fun guardarMiniatura(chave: String, bitmap: Bitmap): Boolean {
        val nome = gravacaoDaBibliotecaDeVideos.salvarMiniatura(chave, bitmap) ?: return false
        bibliotecaDeVideos.definirMiniatura(chave, nome)
        gravacaoDaBibliotecaDeVideos.gravar(bibliotecaDeVideos)
        if (painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.atualizar()
        return true
    }

    private fun atualizarBotaoDeBaixar() {
        // Vídeo do aparelho (Visualizador) sempre pode ir para a Galeria; vídeo de site depende da captura ligada.
        val local = !enderecoAtual.startsWith("http", ignoreCase = true)
        findViewById<ImageButton>(R.id.botao_baixar_midia_do_player_nativo).visibility =
            if (preferencias.capturarMidiaParaDownload || local) View.VISIBLE else View.GONE
    }

    /** Opções de vídeo que a página detectou (vêm da tela principal ao abrir um vídeo de site). */
    private val opcoesDaPagina by lazy { OpcoesDeDownloadNoPlayerNativo.ler(intent) }
    private val sondagemDosDetalhes by lazy { SondagemDosDetalhesDaMidiaParaBaixar() }

    private fun candidatosDaPagina(): List<com.chromebrothers.navegador.regras.PedidoDeMidiaObservado> =
        com.chromebrothers.navegador.regras.EscolhaDoPedidoDeMidiaParaBaixar.candidatosParaAGaleria(opcoesDaPagina)

    /** Mesma lista do navegador: lê resolução/duração/som e mostra da melhor para a pior. */
    /** O vídeo que está tocando como uma opção de download (quando a página não mandou opções). */
    private fun pedidoDoVideoAtual(): com.chromebrothers.navegador.regras.PedidoDeMidiaObservado {
        val classificado = ClassificacaoDoPedidoDeMidiaParaDownload.classificar(enderecoAtual)
        val tipo = if (classificado == TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS ||
            classificado == TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH
        ) {
            classificado
        } else {
            TipoDePedidoDeMidiaParaDownload.ARQUIVO
        }
        return com.chromebrothers.navegador.regras.PedidoDeMidiaObservado(enderecoAtual, tipo, ordem = 1)
    }

    private fun mostrarListaDeOpcoesDaPagina(
        opcoes: List<com.chromebrothers.navegador.regras.PedidoDeMidiaObservado> = opcoesDaPagina,
    ) {
        Toast.makeText(this, R.string.lendo_detalhes_dos_videos, Toast.LENGTH_SHORT).show()
        val videos = com.chromebrothers.navegador.regras.EscolhaDoPedidoDeMidiaParaBaixar.listar(opcoes)
            .ifEmpty { opcoes }
        Thread {
            val detalhes = sondagemDosDetalhes.sondar(videos, referer = { refererAtual }, agente = { agenteAtual })
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                val texto = com.chromebrothers.navegador.regras.TextoDaOpcaoDeMidiaParaBaixar
                val temAudioSeparado = com.chromebrothers.navegador.regras.EscolhaDoPedidoDeMidiaParaBaixar
                    .melhorAudio(opcoesDaPagina) != null
                CaixaDeEscolhaDoNavegador.mostrar(
                    this,
                    getString(R.string.escolher_midia_para_baixar),
                    texto.ordenar(videos, detalhes).map { texto.texto(it, detalhes[it.endereco], temAudioSeparado) to it },
                ) { pedido ->
                    baixarOpcaoDaPagina(pedido, com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia.DOWNLOADS)
                }
            }
        }.start()
    }

    /** Baixa uma das opções da página (com o melhor áudio separado, se houver, para juntar). */
    private fun baixarOpcaoDaPagina(
        pedido: com.chromebrothers.navegador.regras.PedidoDeMidiaObservado,
        destino: com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia,
        alternativas: List<com.chromebrothers.navegador.regras.PedidoDeMidiaObservado> = emptyList(),
    ) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            pedirPermissaoDeNotificacaoParaODownload.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val servico = ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador
        val regra = com.chromebrothers.navegador.regras.EscolhaDoPedidoDeMidiaParaBaixar
        val intent = Intent(this, ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador::class.java).apply {
            putExtra(servico.EXTRA_ENDERECO, pedido.endereco)
            putExtra(servico.EXTRA_TIPO, pedido.tipo.name)
            putExtra(servico.EXTRA_REFERER, refererAtual)
            putExtra(servico.EXTRA_AGENTE, agenteAtual)
            putExtra(servico.EXTRA_TITULO, tituloAtual.ifBlank { "video" })
            putExtra(servico.EXTRA_DESTINO, destino.name)
            if (!regra.pareceAudio(pedido)) {
                regra.melhorAudio(opcoesDaPagina)?.let { putExtra(servico.EXTRA_AUDIO_SEPARADO, it.endereco) }
            }
            if (alternativas.isNotEmpty()) {
                putExtra(servico.EXTRA_ALTERNATIVAS_ENDERECOS, alternativas.map { it.endereco }.toTypedArray())
                putExtra(servico.EXTRA_ALTERNATIVAS_TIPOS, alternativas.map { it.tipo.name }.toTypedArray())
            }
        }
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(
            this,
            if (destino == com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia.GALERIA) {
                R.string.envio_do_video_para_a_galeria_iniciado
            } else {
                R.string.download_de_midia_iniciado
            },
            Toast.LENGTH_LONG,
        ).show()
    }

    /** Vídeos gerados (dentro do app) já copiados para a Galeria: a cópia interna sai quando o player fechar. */
    private val enviadosAGaleriaNestaSessao = linkedSetOf<String>()

    private fun caminhoRelativoNoAparelho(endereco: String): String? {
        if (!endereco.startsWith("content:", ignoreCase = true)) return null
        return runCatching {
            contentResolver.query(
                android.net.Uri.parse(endereco),
                arrayOf(android.provider.MediaStore.MediaColumns.RELATIVE_PATH),
                null,
                null,
                null,
            )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        }.getOrNull()
    }

    /** Vídeo gerado guardado no app: copia para Movies/ChromeBrothers agora; a cópia interna sai ao fechar o player. */
    private fun enviarVideoGeradoParaAGaleria(uri: String) {
        if (uri in enviadosAGaleriaNestaSessao) {
            Toast.makeText(this, R.string.video_ja_enviado_para_a_galeria, Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, R.string.envio_do_video_para_a_galeria_iniciado, Toast.LENGTH_SHORT).show()
        Thread {
            val ok = MovimentoDaMidiaGeradaParaAGaleriaDoAparelho(this).copiarParaAGaleria(uri)
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                if (ok) enviadosAGaleriaNestaSessao += uri
                Toast.makeText(
                    this,
                    if (ok) R.string.video_gerado_enviado_para_a_galeria else R.string.falha_ao_enviar_video_para_a_galeria,
                    Toast.LENGTH_LONG,
                ).show()
            }
        }.start()
    }

    private fun apagarCopiasInternasJaEnviadasAGaleria() {
        if (enviadosAGaleriaNestaSessao.isEmpty()) return
        val json = org.json.JSONArray().apply { enviadosAGaleriaNestaSessao.forEach { put(it) } }.toString()
        enviadosAGaleriaNestaSessao.clear()
        val contexto = applicationContext
        Thread { runCatching { ExclusaoDaMidiaComLimpezaDasBibliotecas.excluir(contexto, json) } }.start()
    }

    private fun baixarVideoAtual(
        destino: com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia =
            com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia.DOWNLOADS,
    ) {
        // Vídeo que já está no aparelho (veio do Visualizador) não é "baixado": vai para a Galeria ou avisa onde está.
        val uriLocal = com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
            .uriSemParametroDeMiniatura(enderecoAtual)
        val caminho = caminhoRelativoNoAparelho(uriLocal)
        val envio = com.chromebrothers.navegador.regras.EnvioDoVideoDoPlayerParaAGaleria
        when (val situacao = envio.situacao(uriLocal, caminho)) {
            com.chromebrothers.navegador.regras.SituacaoDoVideoParaAGaleria.BAIXAR_DO_SITE -> Unit
            com.chromebrothers.navegador.regras.SituacaoDoVideoParaAGaleria.MOVER_DA_PASTA_DO_APP -> {
                enviarVideoGeradoParaAGaleria(uriLocal)
                return
            }
            else -> {
                envio.aviso(situacao, caminho)?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
                return
            }
        }
        val classificado = ClassificacaoDoPedidoDeMidiaParaDownload.classificar(enderecoAtual)
        val tipo = when (classificado) {
            TipoDePedidoDeMidiaParaDownload.ARQUIVO,
            TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS,
            TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH,
            -> classificado
            else -> {
                val esquema = enderecoAtual.substringBefore(':', "").lowercase()
                if (esquema == "http" || esquema == "https") {
                    TipoDePedidoDeMidiaParaDownload.ARQUIVO
                } else {
                    TipoDePedidoDeMidiaParaDownload.IGNORAR
                }
            }
        }
        if (tipo == TipoDePedidoDeMidiaParaDownload.IGNORAR) {
            Toast.makeText(this, R.string.midia_ainda_nao_apareceu, Toast.LENGTH_LONG).show()
            return
        }
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            pedirPermissaoDeNotificacaoParaODownload.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val intent = Intent(this, ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador::class.java).apply {
            putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_ENDERECO, enderecoAtual)
            putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_TIPO, tipo.name)
            putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_REFERER, refererAtual)
            putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_AGENTE, agenteAtual)
            putExtra(
                ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_TITULO,
                tituloAtual.ifBlank { "video" },
            )
            putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_DESTINO, destino.name)
        }
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(
            this,
            if (destino == com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia.GALERIA) {
                R.string.envio_do_video_para_a_galeria_iniciado
            } else {
                R.string.download_de_midia_iniciado
            },
            Toast.LENGTH_LONG,
        ).show()
    }

    companion object {
        const val EXTRA_ENDERECO = "endereco_do_video"
        const val EXTRA_VELOCIDADE = "velocidade_do_video"
        const val EXTRA_TITULO = "titulo_do_video"
        const val EXTRA_POSICAO_MS = "posicao_inicial_do_video_ms"
        const val EXTRA_REFERER = "referer_do_video"
        const val EXTRA_AGENTE = "agente_do_video"
        const val EXTRA_ABRIR_PAGINA = "abrir_pagina_ao_voltar"
        const val EXTRA_MODO_DO_PLAYER_NA_NAVEGACAO = "modo_do_player_na_navegacao"
        const val MODO_AREA_DA_PAGINA = "area_da_pagina"
        const val MODO_JANELA_FLUTUANTE = "janela_flutuante"
    }
}

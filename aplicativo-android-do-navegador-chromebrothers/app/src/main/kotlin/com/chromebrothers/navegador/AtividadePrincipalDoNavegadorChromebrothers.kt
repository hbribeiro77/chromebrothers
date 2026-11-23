package com.chromebrothers.navegador

import android.Manifest
import android.app.ActivityManager
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.ValueCallback
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.view.WindowManager
import android.widget.ListView
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.TextureView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.ScriptHandler
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.mozilla.geckoview.GeckoSession
import com.chromebrothers.navegador.regras.AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando
import com.chromebrothers.navegador.regras.AcumuloDoAvisoDeDeslocamentoDoPlayerDeVideo
import com.chromebrothers.navegador.regras.ItemDaBibliotecaDeComponentesHtmlDaColecao
import com.chromebrothers.navegador.regras.AgenteDeUsuarioDoNavegadorChromebrothers
import com.chromebrothers.navegador.regras.AlternanciaDaVersaoParaComputadorDaAba
import com.chromebrothers.navegador.regras.AjusteDeVelocidadeDeReproducaoDoVideo
import com.chromebrothers.navegador.regras.CameraDoPedidoDeArquivoDaPagina
import com.chromebrothers.navegador.regras.ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina
import com.chromebrothers.navegador.regras.ClassificacaoDoPedidoDeMidiaParaDownload
import com.chromebrothers.navegador.regras.FonteDoEnderecoDaImagemDoToqueLongo
import com.chromebrothers.navegador.regras.ItemDaColecaoFlutuante
import com.chromebrothers.navegador.regras.EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers
import com.chromebrothers.navegador.regras.FlagsDaNovaJanelaDocumentCentricDoNavegador
import com.chromebrothers.navegador.regras.LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers
import com.chromebrothers.navegador.regras.MontagemDaColecaoFlutuanteDoChat
import com.chromebrothers.navegador.regras.TipoDoItemDaColecaoFlutuante
import android.content.ComponentName
import android.app.ActivityManager.AppTask
import com.chromebrothers.navegador.regras.OrigemDoDownloadPedidoPelaPagina
import com.chromebrothers.navegador.regras.PedidoDeArquivoDaPaginaDoNavegador
import com.chromebrothers.navegador.regras.TraducaoDoPedidoDeArquivoDoFirefox
import com.chromebrothers.navegador.regras.PedidoDeDownloadDaPagina
import com.chromebrothers.navegador.regras.ClassificacaoDoTipoDeRespostaDeMidiaParaDownload
import com.chromebrothers.navegador.regras.EnderecosDevolvidosPeloSeletorDeArquivoDaPagina
import com.chromebrothers.navegador.regras.EscolhaDoPedidoDeMidiaParaBaixar
import com.chromebrothers.navegador.regras.ConteudoCodificadoNoEnderecoDaPagina
import com.chromebrothers.navegador.regras.DecisaoDeAbrirVideoNoPlayerNativo
import com.chromebrothers.navegador.regras.DecisaoDeBloqueioDeAnuncio
import com.chromebrothers.navegador.regras.DecisaoDeNavegacaoParaOutroSite
import com.chromebrothers.navegador.regras.JanelaNovaDoSiteComoModalDeLoginDoNavegador
import com.chromebrothers.navegador.regras.VisibilidadeDaModalDeLoginPorAbaDoNavegador
import com.chromebrothers.navegador.regras.EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox
import com.chromebrothers.navegador.regras.DecisaoDeDestinoDoToqueNoBotaoDoVideo
import com.chromebrothers.navegador.regras.DecisaoDeMostrarBotaoFlutuanteDoVideo
import com.chromebrothers.navegador.regras.DestinoDoArrasteDaAbaNaFaixa
import com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaReferenciaMcp.Acao as AcaoDoDestinoMcp
import com.chromebrothers.navegador.regras.DestinoDoToqueNoBotaoDoVideo
import com.chromebrothers.navegador.regras.DeslocamentoDoVizinhoNoArrasteDaAba
import com.chromebrothers.navegador.regras.LeituraDoEstadoDoVideoControladoNaPagina
import com.chromebrothers.navegador.regras.EnvioDeTextoParaOCampoDaPagina
import com.chromebrothers.navegador.regras.EscritaDeTextoNoEditorDaPaginaParaOChat
import com.chromebrothers.navegador.regras.EsperaDoCarregamentoDaPaginaParaOChat
import com.chromebrothers.navegador.regras.PassoDaEsperaDoCarregamento
import com.chromebrothers.navegador.regras.RelatoDoCarregamentoDaPaginaParaOChat
import com.chromebrothers.navegador.regras.TextoDaPaginaInicialParaOChat
import com.chromebrothers.navegador.regras.CliqueNoTextoVisivelDaPaginaParaOChat
import com.chromebrothers.navegador.regras.ContextoDaPaginaParaOChat
import com.chromebrothers.navegador.regras.EnderecoDaPaginaInicialDoNavegadorChromebrothers
import com.chromebrothers.navegador.regras.GestaoDeAbasDoNavegadorChromebrothers
import com.chromebrothers.navegador.regras.IncrementosDeDeslocamentoDoPlayerDeVideo
import com.chromebrothers.navegador.regras.FaixaDeAtalhosDaBarraDeEndereco
import com.chromebrothers.navegador.regras.HistoricoDePaginasVisitadasNoNavegador
import com.chromebrothers.navegador.regras.IconeDaPaginaNaBarraDeEndereco
import com.chromebrothers.navegador.regras.InterpretacaoDoTextoDaBarraDeEndereco
import com.chromebrothers.navegador.regras.ItensDoMenuOverflowDosComponentesGravadosDaColecao
import com.chromebrothers.navegador.regras.ListaDeAtalhosDaPaginaInicialDoNavegador
import com.chromebrothers.navegador.regras.AlinhamentoDaListaDeSugestoesComACaixaDeEnderecoDoNavegador
import com.chromebrothers.navegador.regras.AnimacaoDaListaDeSugestoesDaBarraDeEnderecoDoNavegador
import com.chromebrothers.navegador.regras.FechamentoDaListaDeSugestoesDaBarraPorToqueForaDoNavegador
import com.chromebrothers.navegador.regras.ListaDeSugestoesDaBarraDeEndereco
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.chromebrothers.navegador.regras.PacoteDaListaDeBloqueioParaOFirefox
import com.chromebrothers.navegador.regras.TextoDaListaDeBloqueioDeAnuncio
import com.chromebrothers.navegador.regras.TipoDaSugestaoDaBarraDeEndereco
import com.chromebrothers.navegador.regras.ToqueDuploNoPlayerDeVideo
import com.chromebrothers.navegador.regras.ModoDeApresentacaoDoSite
import com.chromebrothers.navegador.regras.MotorDeBuscaDaBarraDeEndereco
import com.chromebrothers.navegador.regras.PosicionamentoDoBotaoFlutuantePertoDoVideo
import com.chromebrothers.navegador.regras.RotuloDoPedidoDeMidiaParaBaixar
import com.chromebrothers.navegador.regras.ResultadoDoAtalhoDaPaginaInicial
import com.chromebrothers.navegador.regras.LimiteDaLarguraDaCapturaDeQuadroDoVideo
import com.chromebrothers.navegador.regras.LimiteDeJanelasFlutuantesDaColecaoDoChat
import com.chromebrothers.navegador.regras.ScriptInjetadoParaDetectarVideoHtmlNaPagina
import com.chromebrothers.navegador.regras.TipoDePedidoDeMidiaParaDownload
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.UUID
import android.graphics.BitmapFactory
import android.util.Base64
import com.chromebrothers.navegador.regras.BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador
import com.chromebrothers.navegador.regras.BibliotecaDeVideosDoNavegador
import com.chromebrothers.navegador.regras.ItemDaBibliotecaDeVideos
import com.chromebrothers.navegador.regras.ControleDaBarraDoNavegador
import com.chromebrothers.navegador.regras.ContagemDaBuscaNaPaginaDoNavegador
import com.chromebrothers.navegador.regras.DisposicaoDaBarraDoNavegadorPorLarguraDaTela
import com.chromebrothers.navegador.regras.ComportamentoDoTecladoPorSiteDoNavegador
import com.chromebrothers.navegador.regras.EspacoDoTecladoSobreAPaginaDoNavegador
import com.chromebrothers.navegador.regras.EstadoDaMedidaDoFocoParaOTeclado
import com.chromebrothers.navegador.regras.ModoDoTecladoSobreAPaginaDoNavegador
import com.chromebrothers.navegador.regras.PosicaoDoCampoFocadoParaOTecladoDoNavegador
import com.chromebrothers.navegador.regras.ReservaDoEspacoDosBotoesDaFaixaDeAbasParaEvitarSobreposicao
import com.chromebrothers.navegador.regras.TecladoAoTrocarDeAbaDoNavegador
import com.chromebrothers.navegador.regras.OpcoesDaBuscaNaPaginaDoNavegador
import com.chromebrothers.navegador.regras.ModoDaBarraDoNavegadorPorLargura
import com.chromebrothers.navegador.regras.MontagemDaIdentidadeDoVideoNaBiblioteca
import androidx.appcompat.widget.PopupMenu

open class AtividadePrincipalDoNavegadorChromebrothers : AppCompatActivity() {
    private val gestao = GestaoDeAbasDoNavegadorChromebrothers()
    private lateinit var gravacaoDasAbas: GravacaoDasAbasAbertasDoNavegador

    /** Abas reabertas que só carregam quando forem mostradas (como o Chrome, para não pesar). */
    private val abasAindaNaoCarregadas = mutableMapOf<Long, AbaSalvaDoNavegador>()

    /** Aba de site pesado carregando escondida desde a abertura do app (ver PreCarregamentoDeSitePesadoNaAbertura). */
    private var idDaAbaPreCarregada: Long? = null
    private var abaPreCarregadaJaMostrada = false
    private val baixarPrioridadeDaAbaPreCarregada = Runnable { encerrarPreCarregamento() }
    private val manipuladorDasAbasSalvas = android.os.Handler(android.os.Looper.getMainLooper())
    private val gravarAbasDepois = Runnable { gravarAbasAgora() }
    private val ultimoEnderecoNoHistoricoPorAba = mutableMapOf<Long, String>()
    private val abasNavegandoPelaBarra = mutableSetOf<Long>()
    private val buscadorDeSugestoesDePesquisa = BuscadorDeSugestoesDePesquisaDoNavegador()
    private var ultimasPesquisasSugeridas: List<String> = emptyList()
    private var aplicandoAutocompletar = false
    private var digitouNoFimDaBarra = false

    /** (o que a pessoa digitou, texto completo na barra) enquanto o autocompletar está selecionado. */
    private var autocompletarAtual: Pair<String, String>? = null
    private val paginas = linkedMapOf<Long, PaginaDeUmaAbaDoNavegadorChromebrothers>()
    private val tokenDaPaginaInicial = UUID.randomUUID().toString()
    private lateinit var preferencias: PreferenciasVisuaisEDeNavegacaoDoChromebrothers
    private lateinit var cacheDeFavicons: CacheDeFaviconsPorHostDoNavegadorChromebrothers
    private lateinit var areaDasPaginas: FrameLayout
    private lateinit var areaVisivel: FrameLayout
    private lateinit var faixaSuperior: LinearLayout
    private lateinit var faixaDeAbas: LinearLayout
    private lateinit var barraDeNavegacao: LinearLayout
    private lateinit var rolagemDasAbas: HorizontalScrollView
    private lateinit var caixaDaBarraDeEndereco: View
    private lateinit var campoEndereco: EditText
    private lateinit var botaoIrAoEnderecoDaBarra: ImageButton
    private lateinit var botaoCopiarEnderecoDaBarra: ImageButton
    private lateinit var molduraDasSugestoes: FrameLayout
    private lateinit var listaDeSugestoes: ScrollView
    private lateinit var conteudoDasSugestoes: LinearLayout
    private var escolhendoSugestao = false
    private var animacaoDasSugestoesDaBarra: android.view.ViewPropertyAnimator? = null
    private lateinit var faixaDeBotoesDeExtensao: View
    private var arrasteDaAba: ArrasteDaAbaEmCurso? = null
    private var brutoXDoToqueDaAba = 0f
    private val esperasDoArrasteDaAba = mutableMapOf<View, Runnable>()
    private lateinit var botaoVoltar: ImageButton
    private lateinit var botaoAvancar: ImageButton
    private lateinit var botaoRecarregar: ImageButton
    private lateinit var botaoInicio: ImageButton
    private lateinit var botaoExtensoes: ImageButton
    private lateinit var painelDeExtensoes: PainelDeExtensoesInstaladasNoNavegadorChromebrothers
    private lateinit var botaoGerenciador: TextView
    private lateinit var botaoNovaAba: ImageButton
    private lateinit var espacoAntesDoContador: View
    private lateinit var botaoFixar: ImageButton
    private lateinit var botaoComputador: ImageButton
    private lateinit var botaoMenuOverflow: ImageButton
    private lateinit var botaoCampoDeTexto: View
    private lateinit var botaoCapturaDeTela: ImageButton
    private lateinit var botaoBuscarNaPagina: ImageButton
    private lateinit var faixaDeBuscaNaPagina: View
    private lateinit var campoDeBuscaNaPagina: EditText
    private lateinit var textoDaContagemDaBuscaNaPagina: TextView
    private var buscaNaPaginaAberta = false
    private var ignorarProximaMudancaDaBuscaNaPagina = false
    private var diferenciarMaiusculasNaBusca = false
    private val handlerDaBuscaNaPagina = Handler(Looper.getMainLooper())
    private val debouncedBuscaNaPagina = Runnable { executarBuscaNaPagina(paraTras = false) }
    private lateinit var botaoDiferenciarMaiusculasNaBusca: TextView
    private lateinit var botaoBibliotecaNaBarra: ImageButton
    private lateinit var botaoChatNaBarra: ImageButton
    private lateinit var interruptorDeCapturaDeMidia: SwitchCompat
    private lateinit var interruptorDeCapturaDeMidiaNoPlayer: SwitchCompat
    private lateinit var interruptorDeBloqueioDeAnuncio: SwitchCompat
    private lateinit var capturaNativaDeTela: CapturaNativaDeTelaDoNavegadorChromebrothers
    @Volatile
    private var listaDeBloqueioDeAnuncio = TextoDaListaDeBloqueioDeAnuncio.ler("")
    private lateinit var atualizacaoDaListaDeBloqueio: AtualizacaoDaListaDeBloqueioDeAnuncioDoNavegador
    private lateinit var botaoFlutuante: ImageButton
    private lateinit var botaoBaixarMidia: ImageButton
    private var vistaDaJanelaDeLogin: org.mozilla.geckoview.GeckoView? = null
    /** Aba que pediu o popup de login; a modal só aparece com essa aba ativa. */
    private var idDaAbaDaJanelaDeLogin: Long? = null
    private val pedirPermissaoDeNotificacaoParaODownload =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private val inicioDaCapturaPorAba = mutableMapOf<Long, ScriptHandler>()
    private val inicioDoCampoDeTextoPorAba = mutableMapOf<Long, ScriptHandler>()
    private lateinit var painelDoCampoDeTexto: PainelDoCampoDeTextoDoNavegador
    private lateinit var painelDoChat: PainelDoChatDaOpenRouterNoNavegador
    private val abasCarregando = mutableSetOf<Long>()
    private val comecosDoChat = mutableListOf<(Long) -> Unit>()
    private val paradasDoChat = mutableListOf<(Long, Boolean) -> Unit>()
    private val handlerDaEsperaDoChat = Handler(Looper.getMainLooper())
    private val ponteDoCampoDeTexto = PonteJavascriptDoCampoDeTextoDoNavegador()
    private val abasEmRecuperacao = mutableSetOf<Long>()
    private lateinit var painelDoPlayerDaPagina: FrameLayout
    private lateinit var superficieDoPlayerDaPagina: View
    private lateinit var botaoPausarPlayerDaPagina: ImageButton
    private lateinit var barraDePosicaoDoPlayerDaPagina: SeekBar
    private lateinit var textoDaPosicaoDoPlayerDaPagina: TextView
    private var vistaDeTelaCheiaDoVideo: View? = null
    /** Site da aba ativa em tela cheia (Gecko onFullScreen): barras do navegador e do sistema somem. */
    private var siteEmTelaCheia = false
    /** Visibilidade de cada vista escondida pela tela cheia do site, para devolver ao sair. */
    private val vistasEscondidasPelaTelaCheiaDoSite = mutableListOf<Pair<View, Int>>()
    private var callbackDeTelaCheiaDoVideo: WebChromeClient.CustomViewCallback? = null
    private var idDaAbaNoPlayerDaPagina: Long? = null
    private var idDoVideoNoPlayerDaPagina: String? = null
    private var usuarioArrastaPosicaoDoPlayer = false
    private var controladorDoScrubFinoDoPlayerDaPagina: ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo? = null
    private var usuarioQuerReproduzir = true
    private var videoDoPlayerEstaPausado = false
    private lateinit var barraSuperiorDoPlayerDaPagina: View
    private lateinit var barraInferiorDoPlayerDaPagina: View
    private lateinit var controlesCentraisDoPlayerDaPagina: View
    private lateinit var avisoDoDeslocamentoDoPlayerDaPagina: TextView
    private lateinit var listaDeIncrementosDoPlayerDaPagina: LinearLayout
    private val esconderControlesDoPlayerDaPagina = Runnable {
        if (painelDoPlayerDaPagina.visibility != View.VISIBLE) return@Runnable
        barraSuperiorDoPlayerDaPagina.visibility = View.GONE
        barraInferiorDoPlayerDaPagina.visibility = View.GONE
        controlesCentraisDoPlayerDaPagina.visibility = View.GONE
        listaDeIncrementosDoPlayerDaPagina.visibility = View.GONE
    }
    private val esconderAvisoDoDeslocamentoDoPlayer = Runnable {
        avisoDoDeslocamentoDoPlayerDaPagina.visibility = View.GONE
    }
    private val acumuloDoDeslocamentoDoPlayer = AcumuloDoAvisoDeDeslocamentoDoPlayerDeVideo()
    private var incrementoDoPlayerDaPagina = IncrementosDeDeslocamentoDoPlayerDeVideo.PADRAO
    private val botoesDeIncrementoDoPlayerDaPagina = mutableListOf<Pair<Int, Button>>()
    private var velocidadeDoPlayerDaPagina = 1.0
    private val botoesDeVelocidadeDoPlayerDaPagina = mutableListOf<Pair<Double, Button>>()
    private val relogioDoPlayerDaPagina = Handler(Looper.getMainLooper())
    private val pulsoDoPlayerDaPagina = object : Runnable {
        override fun run() {
            val idVideo = idDoVideoNoPlayerDaPagina
            val pagina = idDaAbaNoPlayerDaPagina?.let { paginas[it] }
            if (idVideo != null && pagina != null) {
                val idCitado = JSONObject.quote(idVideo)
                pagina.webView.evaluateJavascript("window.chromebrothersManterPalco($idCitado)", null)
            }
            lerEstadoDoPlayerDaPagina()
            relogioDoPlayerDaPagina.postDelayed(this, 400)
        }
    }
    private lateinit var progresso: ProgressBar
    private lateinit var painelGerenciador: LinearLayout
    private lateinit var botaoNovaAbaNaGestao: ImageButton
    private lateinit var botaoNovaJanelaNaGestao: TextView
    private lateinit var botaoFecharGestao: ImageButton
    private lateinit var gradeDeAbas: GridLayout
    private lateinit var gravacaoDaBibliotecaDeVideos: GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo
    private lateinit var bibliotecaDeVideos: BibliotecaDeVideosDoNavegador
    private lateinit var painelDaBibliotecaDeVideos: PainelDaGavetaDaBibliotecaDeVideos
    private lateinit var gravacaoDaBibliotecaDeImagens: GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo
    private lateinit var painelDaColecao: GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador
    private val leitorEmSegundoPlano by lazy { LeitorDePaginaEmSegundoPlanoParaOChat(this) }
    private lateinit var controladorDasAbasAncoradasNaGaveta: ControladorDasAbasAncoradasNaGavetaDoChatDoNavegador
    private val copiaDeImagemDaPagina by lazy {
        CopiaDeImagemDaPaginaParaABibliotecaDoNavegador(
            this,
            ::receberImagemDaPagina,
            { cofreDaChaveDaOpenRouter.temChave() },
        ) { endereco ->
            colocarNaColecaoPeloToque(TipoDoItemDaColecaoFlutuante.IMAGEM, endereco, "Imagem")
        }
    }
    private val cofreDaChaveDaOpenRouter by lazy { CofreDaChaveDaOpenRouter(this) }
    private val orquestradorDaExtracaoDeTextoDeImagem by lazy {
        OrquestradorDaExtracaoDeTextoDeImagemViaIaDoNavegador(
            activity = this,
            cofre = cofreDaChaveDaOpenRouter,
            lerModelo = { preferencias.modeloDoChat },
            aoAbrirChatComTexto = { texto -> painelDoChat.abrirComTextoNoCampo(texto) },
        )
    }
    private val menuDoLinkDaPagina by lazy {
        MenuDoToqueLongoNoLinkDaPaginaDoNavegadorChromebrothers(
            activity = this,
            aoAbrirEmNovaAba = { endereco -> abrirLinkEmNovaAba(endereco) },
            aoAbrirEmNovaJanela = { endereco -> abrirNovaJanela(endereco) },
            aoCopiar = { endereco -> copiarEnderecoDoLink(endereco) },
        )
    }
    private lateinit var bibliotecaDeImagens: com.chromebrothers.navegador.regras.BibliotecaDeImagensCapturadasDoNavegador
    private lateinit var gravacaoDaBibliotecaDeComponentes: GravacaoDaBibliotecaDeComponentesHtmlDaColecaoNoArmazenamentoDoAplicativo
    private lateinit var bibliotecaDeComponentesDaColecao: BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador
    private lateinit var gravacaoDosRoteirosDeAutomacao: GravacaoDosRoteirosDeAutomacaoDaPaginaNoArmazenamentoDoAplicativo
    private var executorDoRoteiroEmAndamento: ExecutorDosRoteirosDeAutomacaoDaPaginaDoNavegador? = null
    private lateinit var painelDoGravadorDeRoteiro: PainelDoGravadorDeRoteiroDeAutomacaoDoNavegador
    private val clienteDaIaDoGravadorDeRoteiro = ClienteDeChatDaOpenRouter()
    private var chaveDoVideoNoPlayerDaPagina: String? = null
    private var tentativasDeMiniaturaDoPlayerDaPagina = 0
    private val capturarMiniaturaDoPlayerDaPagina = Runnable { tentarGuardarMiniaturaDoPlayerDaPagina() }
    private var promptDeArquivoAberto: org.mozilla.geckoview.GeckoSession.PromptDelegate.FilePrompt? = null
    private var resultadoDoPromptDeArquivo: org.mozilla.geckoview.GeckoResult<org.mozilla.geckoview.GeckoSession.PromptDelegate.PromptResponse>? = null
    private var variosArquivosNoSeletor = false
    private var idDaAbaDoSeletorDeArquivoDaPagina: Long? = null
    private var arquivoDaFotoCapturadaPelaPagina: File? = null
    private var arquivoDoVideoCapturadoPelaPagina: File? = null
    private var uriDaFotoCapturadaPelaPagina: Uri? = null
    private var uriDoVideoCapturadoPelaPagina: Uri? = null
    private val arquivosEntreguesAPagina = mutableListOf<File>()
    private val retornoDoSeletorDeArquivoDaPagina = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { resultado -> entregarArquivoEscolhidoPelaPagina(resultado) }
    private val retornoDoPlayerDeArquivo = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { resultado ->
        sincronizarBibliotecaDeVideos()
        val dados = resultado.data
        val endereco = dados?.getStringExtra(
            AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_ABRIR_PAGINA,
        ).orEmpty()
        if (endereco.isNotBlank()) navegarNaAba(gestao.idDaAbaAtiva, endereco)
        val modo = dados?.getStringExtra(AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_MODO_DO_PLAYER_NA_NAVEGACAO)
        if (modo != null) continuarVideoDaTelaCheiaNaNavegacao(dados, modo)
    }
    private val retornoDoSeletorDeExtensao = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { resultado ->
        if (resultado.resultCode != RESULT_OK) return@registerForActivityResult
        val uri = resultado.data?.data ?: return@registerForActivityResult
        painelDeExtensoes.receberArquivo(uri)
    }
    private var aoReceberAnexoDoChat: ((Uri?) -> Unit)? = null
    private val leituraDeAnexoDoChat = LeituraDeAnexoDoChatDaOpenRouter(this)
    private val seletorDeImagemDoChat = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val receber = aoReceberAnexoDoChat
        aoReceberAnexoDoChat = null
        receber?.invoke(uri)
    }
    private var aoReceberImagemDaFormaDoPincel: ((Uri?) -> Unit)? = null
    private val seletorDeImagemDaFormaDoPincel = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val receber = aoReceberImagemDaFormaDoPincel
        aoReceberImagemDaFormaDoPincel = null
        receber?.invoke(uri)
    }
    private var aoReceberArquivoDePinceis: ((Uri?) -> Unit)? = null
    private val seletorDoArquivoDePinceis = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val receber = aoReceberArquivoDePinceis
        aoReceberArquivoDePinceis = null
        receber?.invoke(uri)
    }
    private val seletorDeArquivoDoChat = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val receber = aoReceberAnexoDoChat
        aoReceberAnexoDoChat = null
        receber?.invoke(uri)
    }
    private var callbackDeArquivoDaColecao: ValueCallback<Array<Uri>>? = null
    private val seletorDeArquivoDaColecao = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { resultado ->
        val callback = callbackDeArquivoDaColecao
        callbackDeArquivoDaColecao = null
        if (callback == null) return@registerForActivityResult
        if (::painelDaColecao.isInitialized) painelDaColecao.limparCallbackDeArquivoSeFor(callback)
        if (resultado.resultCode != RESULT_OK) {
            callback.onReceiveValue(null)
            return@registerForActivityResult
        }
        val uris = WebChromeClient.FileChooserParams.parseResult(RESULT_OK, resultado.data)
        callback.onReceiveValue(uris)
    }
    private var painelPedindoPastaDaColecao: PainelDaColecaoFlutuanteDoChatDoNavegador? = null
    private val leituraDePastaDaColecao by lazy {
        LeituraDePastaEscolhidaPeloComponenteDaColecaoNoAparelho(this)
    }
    private val leituraDaPastaVirtualDeMidiaDoChromeBrothers by lazy {
        LeituraDaPastaVirtualDeMidiaDoChromeBrothersNoAparelho(contentResolver) { listarMidiasGeradasViaMcp() }
    }
    private val geradorDeMiniaturasDaGradeDoVisualizador by lazy {
        GeradorDeMiniaturasDaGradeDoVisualizadorDeMidiaComCacheEmDisco(this)
    }
    private val pregeracaoDeMiniaturasEmAndamento = java.util.concurrent.atomic.AtomicBoolean(false)
    private val leituraDeMidiaDoComponenteDaColecao by lazy {
        LeituraDeBytesDeMidiaLocalOuRemotaParaOComponenteDaColecao(this)
    }
    private var visorExternoDaGavetaCriado: VisorExternoDeImagemDoVisualizadorDeMidiaQuandoEstaNaGavetaDoChat? = null
    private var visorDeVideosAoLadoCriado: VisorDeVideosAoLadoComPlayerNativoEmbutidoNaColecao? = null
    /** Quem abriu o player embutido recebe o vídeo em exibição (null = fechou) para pôr/tirar a moldura. */
    private var avisarDonoDoVisorDeVideosAoLado: ((String?) -> Unit)? = null
    /** Fantasma lift-and-drag (estilo iPad) da `<img>` do componente → editor / MCP / colar. */
    private var fantasmaDoArrasteDeImagemDoComponente: FrameLayout? = null
    /** O fantasma está sobre o Visualizador com o editor aberto (soltar cancela). */
    private var fantasmaNaZonaDeCancelar = false
    private val soltarDoArrasteDoComponenteJaReivindicado = java.util.concurrent.atomic.AtomicBoolean(false)
    private var maisDoFantasmaDoArrasteDeImagemDoComponente: TextView? = null
    private var bitmapDoFantasmaDoArrasteDeImagemDoComponente: Bitmap? = null
    private var uriDoArrasteDeImagemDoComponente: String? = null
    private var tamanhoDoFantasmaDoArrastePx: Int = 0
    /** Fantasma veio de toque longo numa `<img>` da aba (não da coleção). */
    private var arrasteDeImagemVeioDaPagina = false
    private var geracaoDoArrasteDeImagemDaPagina = 0L
    /** Src original da página (http/blob/data) — modal de copiar/salvar usa este, não o file:// do cache. */
    private var enderecoOriginalDaImagemDaPaginaNoArraste: String? = null
    private var idDaAbaDoArrasteDeImagemDaPagina: Long = -1L
    private var xInicioDoArrasteDeImagemDaPagina = 0f
    private var yInicioDoArrasteDeImagemDaPagina = 0f
    /** Início do fantasma vindo da coleção/Visualizador (modal vs arraste). */
    private var xInicioDoArrasteDeImagemDoComponente = 0f
    private var yInicioDoArrasteDeImagemDoComponente = 0f
    /** Pairar fantasma sobre outra aba na faixa → troca após ~700ms (página/coleção/biblioteca). */
    private var estadoDoPairarAbaNoArrasteDoFantasma =
        com.chromebrothers.navegador.regras.EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador()
    private var agendamentoDaTrocaDeAbaPorPairarFantasma: Runnable? = null
    /**
     * Bytes da imagem prontos para colar — só vão pro clipboard no soltar verde.
     * (Não preenche a área de transferência durante o arraste.)
     */
    private var bytesProntosParaColarNoSoltar: ByteArray? = null
    private var mimeProntosParaColarNoSoltar: String? = null
    /** true enquanto os bytes prontos ainda são da miniatura (a imagem cheia não terminou de ler). */
    private var bytesProntosSaoMiniatura = false
    private var editavelProximoNaPaginaNoArrasteDoComponente = false
    /** True se a sonda já viu campo editável neste gesto (não apaga se o dedo voltar ao painel). */
    private var teveEditavelNoArrasteDoComponente = false
    private var xPaginaOkDoArrasteDoComponente = 0f
    private var yPaginaOkDoArrasteDoComponente = 0f
    private var geracaoDaSondaDoArrasteDoComponente = 0L
    private var ultimoHorarioDaSondaDoArrasteDoComponenteMs = 0L
    private var ultimoXDoArrasteDoComponente = 0f
    private var ultimoYDoArrasteDoComponente = 0f
    private var geracaoDaRestauracaoFocoColecaoAposColar = 0L
    /** Fantasma do arraste de um item da gaveta da biblioteca → gerador MCP (campos separados do fantasma da coleção). */
    private var fantasmaDoArrasteDaBiblioteca: FrameLayout? = null
    private var maisDoFantasmaDaBiblioteca: TextView? = null
    private var estadoDoArrasteDaBibliotecaAtivo: EstadoDoArrasteDaBiblioteca? = null
    private var bitmapDoFantasmaDaBiblioteca: Bitmap? = null
    private var tamanhoDoFantasmaDaBibliotecaPx: Int = 0
    private var cancelarToquePendenteDaGaveta = false
    private var ultimoXDoToqueNaTela = 0f
    private var ultimoYDoToqueNaTela = 0f
    /** Sonda assíncrona: verde no + quando há campo editável próximo na página. */
    private var editavelProximoNaPaginaNoArrasteDaBiblioteca = false
    private var geracaoDaSondaDoArrasteDaBiblioteca = 0L
    private var ultimoHorarioDaSondaDoArrasteDaBibliotecaMs = 0L
    /** Depois do foco/Ctrl+V, quanto tempo a página fica liberada antes do fundo voltar. */
    private var geracaoDaRestauracaoAposColarDaBiblioteca = 0L
    private val atrasoParaRestaurarPaginaAposFocoMs = 1_000L
    private val corMaisArrasteNeutro = Color.parseColor("#F5C518")
    private val corMaisArrastePodeSoltar = Color.parseColor("#34C759")
    private val seletorDePastaDaColecao = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        val painel = painelPedindoPastaDaColecao
        painelPedindoPastaDaColecao = null
        if (painel == null) return@registerForActivityResult
        if (uri == null) {
            painel.entregarJsonDaPastaEscolhida(
                com.chromebrothers.navegador.regras.ListagemDePastaEscolhidaPeloComponenteDaColecao
                    .mensagemCancelada(),
            )
            return@registerForActivityResult
        }
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        painel.entregarJsonDaPastaEscolhida(leituraDePastaDaColecao.descrever(uri))
    }
    private val servicoDeGeracaoDeImagemViaMcpDaColecao by lazy {
        val sessao = SessaoDaContaHiggsfieldNoAparelho(this)
        val sessaoSjinn = SessaoDaContaSjinnNoAparelho(this)
        val sessaoTopview = SessaoDaContaTopviewNoAparelho(this)
        val sessaoMagnific = SessaoDaContaMagnificNoAparelho(this)
        val sessaoVivix = SessaoDaContaVivixNoAparelho(this)
        ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers(
            cliente = ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers(this, sessao),
            sessao = sessao,
            gravacao = GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers(this),
            clienteSjinn = ClienteApiDoSjinnParaGeracaoDeImagemNoChromebrothers(sessaoSjinn),
            sessaoSjinn = sessaoSjinn,
            clienteTopview = ClienteMcpDoTopviewParaGeracaoNoChromebrothers(this, sessaoTopview),
            sessaoTopview = sessaoTopview,
            cardsDeVideo = CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho(this),
            cardsDeImagem = CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho(
                this,
                CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho.ARQUIVO_DE_IMAGEM,
            ),
            catalogos = CatalogosDeModelosDosMcpsGuardadosNoAparelho(this),
            clienteMagnific = ClienteMcpDoMagnificParaGeracaoNoChromebrothers(this, sessaoMagnific),
            sessaoMagnific = sessaoMagnific,
            receitas = ReceitasDeGeracaoMcpGuardadasNoAparelho(this),
            clienteVivix = ClienteApiDoVivixParaGeracaoDeVideoNoChromebrothers(sessaoVivix),
            sessaoVivix = sessaoVivix,
            pastaDasMiniaturasDosCards = File(filesDir, "miniaturas-dos-cards-mcp"),
            existeConteudo = { uriTexto ->
                runCatching {
                    contentResolver.openFileDescriptor(Uri.parse(uriTexto), "r")?.use { true } ?: false
                }.getOrDefault(false)
            },
        )
    }
    private var painelPedindoImagemDeReferenciaDaGeracao: PainelDaColecaoFlutuanteDoChatDoNavegador? = null
    private val seletorDeImagensDeReferenciaDaGeracaoDaColecao = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        val painel = painelPedindoImagemDeReferenciaDaGeracao
        painelPedindoImagemDeReferenciaDaGeracao = null
        if (painel == null) return@registerForActivityResult
        if (uris.isNullOrEmpty()) {
            painel.entregarImagemDeReferenciaDaGeracao(
                com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
                    .erro("Nenhuma imagem escolhida."),
            )
            return@registerForActivityResult
        }
        Thread {
            val json = acrescentarVariasReferenciasDaGeracao(painel.tipoDasReferencias(), uris)
            painel.entregarImagemDeReferenciaDaGeracao(json)
        }.start()
    }

    /** Galeria do gerador de vídeo quando o modelo aceita vídeo: seletor do sistema com fotos e vídeos. */
    private val seletorDeImagensEVideosDeReferenciaDaGeracao = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(
            com.chromebrothers.navegador.regras.PedidoDeGeracaoDeImagemViaMcpNaColecao.LIMITE_DE_REFERENCIAS,
        ),
    ) { uris ->
        val painel = painelPedindoImagemDeReferenciaDaGeracao
        painelPedindoImagemDeReferenciaDaGeracao = null
        if (painel == null) return@registerForActivityResult
        val erro = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
        if (uris.isNullOrEmpty()) {
            painel.entregarImagemDeReferenciaDaGeracao(erro.erro("Nada escolhido."))
            return@registerForActivityResult
        }
        Thread {
            var json = erro.erro("Nada escolhido.")
            for (uri in uris) {
                val mime = contentResolver.getType(uri).orEmpty()
                json = if (mime.startsWith("video/")) {
                    adicionarVideoDeReferenciaDaGeracao(arquivo = null, uri = uri, nome = nomeDoArquivoDaUri(uri))
                } else {
                    acrescentarVariasReferenciasDaGeracao(painel.tipoDasReferencias(), listOf(uri))
                }
                if (json.contains("\"ok\":false")) break
            }
            painel.entregarImagemDeReferenciaDaGeracao(json)
        }.start()
    }

    private fun geradorDeVideoAceitaVideo(painel: PainelDaColecaoFlutuanteDoChatDoNavegador): Boolean =
        painel.tipoDoGeradorMcpAberto() ==
            com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaReferenciaMcp.COMPONENTE_VIDEO &&
            servicoDeGeracaoDeImagemViaMcpDaColecao.aceitaVideoNoModeloAtual()

    /**
     * Prepara um vídeo de referência: copia para o cache do app (a origem pode sumir), lê a duração
     * e tira um quadro para a prévia. Bloqueante (I/O): chamar fora da thread principal.
     */
    private fun adicionarVideoDeReferenciaDaGeracao(arquivo: File?, uri: Uri?, nome: String): String {
        val erro = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
        val regras = com.chromebrothers.navegador.regras.ReferenciaDeVideoNaGeracaoMcp
        val pasta = File(cacheDir, "referencias-de-video").apply { mkdirs() }
        val extensao = nome.substringAfterLast('.', "mp4").lowercase().takeIf { it in setOf("mp4", "mov", "webm", "m4v") } ?: "mp4"
        val destino = File(pasta, "ref-${java.util.UUID.randomUUID()}.$extensao")
        return try {
            when {
                arquivo != null && arquivo.isFile -> {
                    if (arquivo.length() > regras.TAMANHO_MAXIMO_BYTES) return erro.erro("Esse vídeo passa de 200 MB.")
                    arquivo.copyTo(destino, overwrite = true)
                }
                uri != null -> contentResolver.openInputStream(uri)?.use { entrada ->
                    destino.outputStream().use { saida ->
                        val buffer = ByteArray(64 * 1024)
                        var total = 0L
                        while (true) {
                            val lidos = entrada.read(buffer)
                            if (lidos < 0) break
                            total += lidos
                            if (total > regras.TAMANHO_MAXIMO_BYTES) {
                                destino.delete()
                                return erro.erro("Esse vídeo passa de 200 MB.")
                            }
                            saida.write(buffer, 0, lidos)
                        }
                    }
                } ?: return erro.erro("Não consegui ler esse vídeo.")
                else -> return erro.erro("Não consegui ler esse vídeo.")
            }
            val leitor = android.media.MediaMetadataRetriever()
            val (duracaoMs, quadro) = try {
                leitor.setDataSource(destino.absolutePath)
                val duracao = leitor.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L
                val bitmap = leitor.getFrameAtTime(0, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                duracao to bitmap
            } finally {
                runCatching { leitor.release() }
            }
            if (quadro == null || duracaoMs <= 0L) {
                destino.delete()
                return erro.erro("Esse arquivo não parece um vídeo válido.")
            }
            val escala = minOf(1f, 640f / maxOf(quadro.width, quadro.height))
            val reduzido = if (escala < 1f) {
                Bitmap.createScaledBitmap(quadro, (quadro.width * escala).toInt(), (quadro.height * escala).toInt(), true)
            } else {
                quadro
            }
            val jpeg = ByteArrayOutputStream().also { reduzido.compress(Bitmap.CompressFormat.JPEG, 82, it) }.toByteArray()
            if (reduzido !== quadro) reduzido.recycle()
            quadro.recycle()
            servicoDeGeracaoDeImagemViaMcpDaColecao.adicionarReferenciaDeVideo(destino, nome, jpeg, duracaoMs)
        } catch (e: Exception) {
            destino.delete()
            erro.erro(e.message ?: "Falha ao preparar o vídeo.")
        }
    }

    private fun escolherImagemDeReferenciaDaGeracao(
        painel: PainelDaColecaoFlutuanteDoChatDoNavegador,
        origem: String,
    ) {
        val erro = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
        if (origem == "biblioteca") {
            mostrarListaDaBibliotecaParaReferenciaDaGeracao(painel)
            return
        }
        painelPedindoImagemDeReferenciaDaGeracao = painel
        try {
            if (geradorDeVideoAceitaVideo(painel)) {
                seletorDeImagensEVideosDeReferenciaDaGeracao.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageAndVideo,
                    ),
                )
                return
            }
            seletorDeImagensDeReferenciaDaGeracaoDaColecao.launch("image/*")
        } catch (_: ActivityNotFoundException) {
            painelPedindoImagemDeReferenciaDaGeracao = null
            painel.entregarImagemDeReferenciaDaGeracao(erro.erro("Não há seletor de imagens neste aparelho."))
        }
    }

    private fun mostrarListaDaBibliotecaParaReferenciaDaGeracao(
        painel: PainelDaColecaoFlutuanteDoChatDoNavegador,
    ) {
        val erro = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
        val comVideos = geradorDeVideoAceitaVideo(painel)
        val imagens = gravacaoDaBibliotecaDeImagens.carregar().listar()
            .mapNotNull { item ->
                val arquivo = gravacaoDaBibliotecaDeImagens.arquivo(item.nomeDoArquivo) ?: return@mapNotNull null
                item.instante to ItemDaEscolhaDaBibliotecaParaReferencia(
                    id = "img:${item.id}",
                    nome = item.nomeExibido?.takeIf { it.isNotBlank() } ?: item.nomeDoArquivo,
                    favorito = item.favorito,
                    video = false,
                    miniatura = arquivo,
                )
            }
        // Vídeos da biblioteca que existem no aparelho, só quando o modelo aceita vídeo.
        val arquivosDosVideos = mutableMapOf<String, File>()
        val videos = if (!comVideos) emptyList() else bibliotecaDeVideos.listar().mapNotNull { item ->
            val arquivo = item.caminhoLocal.takeIf { it.isNotBlank() }?.let { File(it) }
                ?.takeIf { it.isFile && it.length() > 0L } ?: return@mapNotNull null
            val id = "vid:${item.chave}"
            arquivosDosVideos[id] = arquivo
            item.instante to ItemDaEscolhaDaBibliotecaParaReferencia(
                id = id,
                nome = item.titulo,
                favorito = item.favorito,
                video = true,
                miniatura = gravacaoDaBibliotecaDeVideos.arquivoDaMiniatura(item.nomeDaMiniatura),
            )
        }
        val itens = (imagens + videos).sortedByDescending { it.first }.map { it.second }
        val arquivoDaImagem = gravacaoDaBibliotecaDeImagens.carregar().listar()
            .associate { "img:${it.id}" to it.nomeDoArquivo }
        if (itens.isEmpty()) {
            painel.entregarImagemDeReferenciaDaGeracao(erro.erro("A biblioteca de imagens está vazia."))
            return
        }
        val tipoDasReferencias = painel.tipoDasReferencias()
        val vagas = (
            com.chromebrothers.navegador.regras.PedidoDeGeracaoDeImagemViaMcpNaColecao.LIMITE_DE_REFERENCIAS -
                servicoDeGeracaoDeImagemViaMcpDaColecao.quantidadeDeReferencias(tipoDasReferencias)
            ).coerceAtLeast(0)
        if (vagas <= 0) {
            painel.entregarImagemDeReferenciaDaGeracao(
                erro.erro(
                    "No máximo ${com.chromebrothers.navegador.regras.PedidoDeGeracaoDeImagemViaMcpNaColecao.LIMITE_DE_REFERENCIAS} referências.",
                ),
            )
            return
        }
        val vagasDeVideo = (
            com.chromebrothers.navegador.regras.ReferenciaDeVideoNaGeracaoMcp.LIMITE_DE_VIDEOS -
                servicoDeGeracaoDeImagemViaMcpDaColecao.quantidadeDeVideosDeReferencia()
            ).coerceAtLeast(0)
        ModalDeEscolhaDaBibliotecaParaReferenciaDaGeracao(
            activity = this,
            itens = itens,
            gerador = geradorDeMiniaturasDaGradeDoVisualizador,
            limite = vagas,
            limiteDeVideos = if (comVideos) vagasDeVideo else 0,
            titulo = if (videos.isNotEmpty()) "Imagens e vídeos da biblioteca" else null,
            aoCancelar = {
                painel.entregarImagemDeReferenciaDaGeracao(erro.erro("Nenhuma imagem escolhida."))
            },
            aoConfirmar = { escolhidos ->
                Thread {
                    var json = erro.erro("Nenhuma imagem escolhida.")
                    for (item in escolhidos) {
                        json = try {
                            if (item.video) {
                                val arquivo = arquivosDosVideos[item.id]
                                if (arquivo == null || !arquivo.isFile) {
                                    erro.erro("O vídeo não está mais no aparelho.")
                                } else {
                                    adicionarVideoDeReferenciaDaGeracao(arquivo, null, arquivo.name)
                                }
                            } else {
                                val nomeDoArquivo = arquivoDaImagem[item.id]
                                val bytes = nomeDoArquivo?.let { gravacaoDaBibliotecaDeImagens.ler(it) }
                                if (bytes == null) {
                                    erro.erro("A imagem não está mais no aplicativo.")
                                } else {
                                    definirReferenciaDaGeracaoEMontarJson(tipoDasReferencias, bytes, item.nome ?: nomeDoArquivo, "image/jpeg")
                                }
                            }
                        } catch (e: Exception) {
                            erro.erro(e.message ?: "Falha ao ler o item.")
                        }
                        if (json.contains("\"ok\":false")) break
                    }
                    painel.entregarImagemDeReferenciaDaGeracao(json)
                }.start()
            },
        ).mostrar()
    }

    private fun acrescentarVariasReferenciasDaGeracao(tipo: String, uris: List<Uri>): String {
        val erro = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
        val limite = com.chromebrothers.navegador.regras.PedidoDeGeracaoDeImagemViaMcpNaColecao.LIMITE_DE_REFERENCIAS
        val vagas = (limite - servicoDeGeracaoDeImagemViaMcpDaColecao.quantidadeDeReferencias(tipo)).coerceAtLeast(0)
        if (vagas <= 0) {
            return erro.erro("No máximo $limite referências.")
        }
        var json = erro.erro("Nenhuma imagem escolhida.")
        for (uri in uris.take(vagas)) {
            json = try {
                val bytes = lerBytesLimitadosParaReferenciaDeGeracao(uri)
                    ?: return erro.erro("Não consegui ler essa imagem (ou ela passa de 20 MB).")
                val mime = contentResolver.getType(uri).orEmpty().ifBlank { "image/jpeg" }
                definirReferenciaDaGeracaoEMontarJson(tipo, bytes, nomeDoArquivoDaUri(uri), mime)
            } catch (e: Exception) {
                erro.erro(e.message ?: "Falha ao ler a imagem.")
            }
            if (json.contains("\"ok\":false")) return json
        }
        return json
    }

    /** Valida/decodifica o preview e acrescenta a referência (até 4) no serviço. */
    private fun definirReferenciaDaGeracaoEMontarJson(tipo: String, bytes: ByteArray, nome: String, mime: String): String {
        val servico = servicoDeGeracaoDeImagemViaMcpDaColecao
        val preview = servico.previewDataUrl(bytes)
        if (preview.isEmpty()) {
            return com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
                .erro("Não consegui ler essa imagem.")
        }
        return servico.adicionarReferencia(tipo, bytes, nome, mime)
    }

    private fun lerBytesLimitadosParaReferenciaDeGeracao(uri: Uri): ByteArray? {
        val limite = 20 * 1024 * 1024
        contentResolver.openInputStream(uri)?.use { entrada ->
            val saida = ByteArrayOutputStream()
            val buffer = ByteArray(16 * 1024)
            while (true) {
                val lidos = entrada.read(buffer)
                if (lidos < 0) break
                if (saida.size() + lidos > limite) return null
                saida.write(buffer, 0, lidos)
            }
            return saida.toByteArray()
        }
        return null
    }

    private fun nomeDoArquivoDaUri(uri: Uri): String {
        val nome = runCatching {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        }.getOrNull()
        return nome?.takeIf { it.isNotBlank() } ?: uri.lastPathSegment.orEmpty().ifBlank { "referencia.jpg" }
    }

    /**
     * DROP de um item da gaveta da biblioteca sobre a coleção com o gerador MCP (imagem/vídeo):
     * vira imagem de referência pelo mesmo caminho do seletor (adicionarReferencia + entregarImagem...).
     * Vídeo vira um quadro JPEG. Roda o I/O fora da thread principal; sempre devolve true no DROP.
     */
    private fun soltarItemDaBibliotecaComoReferenciaDaGeracao(
        painel: PainelDaColecaoFlutuanteDoChatDoNavegador,
        evento: android.view.DragEvent,
        tipoDoGerador: String,
    ): Boolean {
        val estado = evento.localState as? EstadoDoArrasteDaBiblioteca
        val uri = evento.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri
        val mime = evento.clipDescription?.takeIf { it.mimeTypeCount > 0 }?.getMimeType(0).orEmpty()
        val permissao = runCatching { requestDragAndDropPermissions(evento) }.getOrNull()
        return soltarItemDaBibliotecaComoReferenciaDaGeracaoNucleo(
            painel = painel,
            estado = estado,
            tipoDoGerador = tipoDoGerador,
            uri = uri,
            mime = mime,
            permissao = permissao,
        )
    }

    /** Núcleo do drop da biblioteca no gerador MCP (sem DragEvent): serve ao arraste do sistema e ao fantasma. */
    private fun soltarItemDaBibliotecaComoReferenciaDaGeracaoNucleo(
        painel: PainelDaColecaoFlutuanteDoChatDoNavegador,
        estado: EstadoDoArrasteDaBiblioteca?,
        tipoDoGerador: String,
        uri: Uri? = null,
        mime: String = "",
        permissao: android.view.DragAndDropPermissions? = null,
    ): Boolean {
        val regra = com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaReferenciaMcp
        val erro = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
        val mimeEfetivo = mime.ifBlank {
            when (estado?.tipo) {
                "video" -> "video/mp4"
                "image" -> {
                    val ext = estado.caminhoLocal.substringAfterLast('.', "").lowercase()
                    when (ext) {
                        "png" -> "image/png"
                        "webp" -> "image/webp"
                        else -> "image/jpeg"
                    }
                }
                else -> ""
            }
        }
        val acao = regra.decidir(
            estado?.tipo.orEmpty(),
            mimeEfetivo,
            tipoDoGerador,
            modeloAceitaVideo = tipoDoGerador == regra.COMPONENTE_VIDEO &&
                servicoDeGeracaoDeImagemViaMcpDaColecao.aceitaVideoNoModeloAtual(),
        )
        if (acao == AcaoDoDestinoMcp.RECUSAR) {
            painel.entregarImagemDeReferenciaDaGeracao(erro.erro("Solte uma imagem ou vídeo da biblioteca."))
            return true
        }
        if (estado == null && uri == null) {
            painel.entregarImagemDeReferenciaDaGeracao(erro.erro("Não consegui ler o item arrastado."))
            return true
        }
        Thread {
            val temporarios = mutableListOf<File>()
            val json = try {
                val local = estado?.caminhoLocal?.takeIf { it.isNotBlank() }?.let { File(it) }
                    ?.takeIf { it.isFile && it.length() > 0L }
                if (acao == AcaoDoDestinoMcp.USAR_VIDEO) {
                    val nome = local?.name ?: uri?.let { nomeDoArquivoDaUri(it) } ?: "referencia.mp4"
                    adicionarVideoDeReferenciaDaGeracao(local, if (local == null) uri else null, nome)
                } else if (acao == AcaoDoDestinoMcp.USAR_FRAME) {
                    val video = local ?: uri?.let { copiarUriParaCacheDoArraste(it, temporarios) }
                    val quadro = video?.let {
                        ExtracaoDeQuadroJpegDeVideoLocalParaArraste.extrairParaArquivo(this, it)
                    }
                    if (quadro == null) {
                        erro.erro("Não consegui extrair um quadro desse vídeo.")
                    } else {
                        temporarios.add(quadro)
                        val bytes = quadro.readBytes()
                        runOnUiThread {
                            Toast.makeText(this, "Usei um frame do vídeo como referência", Toast.LENGTH_SHORT).show()
                        }
                        definirReferenciaDaGeracaoEMontarJson(tipoDoGerador, bytes, "quadro-do-video.jpg", "image/jpeg")
                    }
                } else {
                    val bytes = if (local != null && local.length() <= 20L * 1024 * 1024) {
                        local.readBytes()
                    } else {
                        uri?.let { lerBytesLimitadosParaReferenciaDeGeracao(it) }
                    }
                    if (bytes == null) {
                        erro.erro("Não consegui ler essa imagem (ou ela passa de 20 MB).")
                    } else {
                        val nome = local?.name ?: uri?.let { nomeDoArquivoDaUri(it) } ?: "referencia.jpg"
                        definirReferenciaDaGeracaoEMontarJson(tipoDoGerador, bytes, nome, mimeEfetivo.ifBlank { "image/jpeg" })
                    }
                }
            } catch (e: Exception) {
                erro.erro(e.message ?: "Falha ao ler o item arrastado.")
            } finally {
                temporarios.forEach { runCatching { it.delete() } }
                runCatching { permissao?.release() }
            }
            painel.entregarImagemDeReferenciaDaGeracao(json)
            avisarSeAReferenciaNaoEntrou(json)
        }.start()
        return true
    }

    /**
     * O erro do soltar ia só para a linha de status no topo do gerador, longe de onde o dedo soltou:
     * parecia que a referência "não foi". Agora também sai num aviso na tela.
     */
    private fun avisarSeAReferenciaNaoEntrou(json: String) {
        val resposta = runCatching { org.json.JSONObject(json) }.getOrNull() ?: return
        if (resposta.optBoolean("ok", true)) return
        val mensagem = resposta.optString("erro").ifBlank { "A referência não entrou." }
        runOnUiThread { if (!isDestroyed) Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show() }
    }

    /** Copia o conteúdo da URI para o cache (usado só quando o arraste não trouxe o caminho local). */
    private fun copiarUriParaCacheDoArraste(uri: Uri, temporarios: MutableList<File>): File? {
        val pasta = File(cacheDir, "arraste-da-biblioteca")
        if (!pasta.mkdirs() && !pasta.isDirectory) return null
        val destino = File(pasta, "video-${java.util.UUID.randomUUID()}.mp4")
        temporarios.add(destino)
        contentResolver.openInputStream(uri)?.use { entrada ->
            destino.outputStream().use { saida -> entrada.copyTo(saida) }
        } ?: return null
        return destino.takeIf { it.length() > 0L }
    }

    private val instalacoesDeExtensaoEmCurso = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencias = PreferenciasVisuaisEDeNavegacaoDoChromebrothers(this)
        cacheDeFavicons = CacheDeFaviconsPorHostDoNavegadorChromebrothers(this)
        sincronizarModoNoturnoComAPreferencia()
        aplicarFundoDaJanelaAntesDoConteudo()
        setContentView(R.layout.layout_atividade_principal_do_navegador_chromebrothers)
        aplicarMargemDasBarrasDoSistema(R.id.raiz_do_navegador)
        areaDasPaginas = findViewById(R.id.area_das_paginas)
        areaVisivel = findViewById(R.id.area_visivel_das_paginas)
        // Pinta já: evita faixa branca enquanto o resto do onCreate sobe.
        findViewById<View>(R.id.raiz_do_navegador).setBackgroundColor(paletaAtual().fundoDaFaixa)
        findViewById<View>(R.id.coluna_do_navegador).setBackgroundColor(paletaAtual().fundoDaPagina)
        areaVisivel.setBackgroundColor(paletaAtual().fundoDaPagina)
        areaDasPaginas.setBackgroundColor(paletaAtual().fundoDaPagina)
        faixaSuperior = findViewById(R.id.faixa_superior_das_abas)
        faixaDeAbas = findViewById(R.id.faixa_de_abas_do_navegador)
        barraDeNavegacao = findViewById(R.id.barra_de_navegacao)
        rolagemDasAbas = findViewById(R.id.rolagem_da_faixa_de_abas)
        caixaDaBarraDeEndereco = findViewById(R.id.caixa_da_barra_de_endereco)
        campoEndereco = findViewById(R.id.campo_endereco_da_pagina)
        botaoIrAoEnderecoDaBarra = findViewById(R.id.botao_ir_ao_endereco_da_barra)
        botaoCopiarEnderecoDaBarra = findViewById(R.id.botao_copiar_endereco_da_barra)
        molduraDasSugestoes = findViewById(R.id.moldura_da_lista_de_sugestoes_da_barra_de_endereco)
        listaDeSugestoes = findViewById(R.id.lista_de_sugestoes_da_barra_de_endereco)
        conteudoDasSugestoes = findViewById(R.id.conteudo_das_sugestoes_da_barra_de_endereco)
        faixaDeBotoesDeExtensao = findViewById(R.id.faixa_de_botoes_de_extensao)
        molduraDasSugestoes.clipChildren = true
        molduraDasSugestoes.clipToPadding = true
        botaoVoltar = findViewById(R.id.botao_voltar_na_pagina)
        botaoAvancar = findViewById(R.id.botao_avancar_na_pagina)
        botaoRecarregar = findViewById(R.id.botao_recarregar_pagina)
        botaoInicio = findViewById(R.id.botao_ir_para_pagina_inicial)
        botaoExtensoes = findViewById(R.id.botao_de_extensoes_do_navegador)
        painelDeExtensoes = PainelDeExtensoesInstaladasNoNavegadorChromebrothers(
            activity = this,
            faixaDeBotoes = findViewById(R.id.botoes_de_acao_das_extensoes),
            painelDoPopup = findViewById(R.id.painel_do_popup_da_extensao),
            aoEscolherArquivo = { escolherArquivoDeExtensao() },
            aoAbrirLoja = { abrirLojaDeExtensoesDoFirefox() },
        )
        painelDeExtensoes.ligar()
        MotorDoFirefoxDoNavegadorChromebrothers.definirNovaAbaDeExtensao { sessao, endereco ->
            runOnUiThread { abrirSessaoPedidaPelaExtensao(sessao, endereco) }
        }
        botaoExtensoes.setOnClickListener { painelDeExtensoes.mostrarLista() }
        findViewById<ImageButton>(R.id.botao_fechar_popup_da_extensao).setOnClickListener {
            painelDeExtensoes.fecharPopup()
        }
        botaoGerenciador = findViewById(R.id.botao_gerenciador_de_abas)
        botaoNovaAba = findViewById(R.id.botao_nova_aba)
        espacoAntesDoContador = findViewById(R.id.espaco_antes_do_contador_de_abas)
        faixaSuperior.addOnLayoutChangeListener { _, left, _, right, _, oldLeft, _, oldRight, _ ->
            if (right - left != oldRight - oldLeft) reservarEspacoDosBotoesDaFaixa()
        }
        botaoFixar = findViewById(R.id.botao_fixar_atalho_da_pagina)
        botaoComputador = findViewById(R.id.botao_versao_para_computador)
        botaoMenuOverflow = findViewById(R.id.botao_do_menu_overflow_da_barra_do_navegador)
        botaoCampoDeTexto = findViewById(R.id.botao_abrir_campo_de_texto_do_navegador)
        botaoCapturaDeTela = findViewById(R.id.botao_de_captura_nativa_de_tela_do_navegador)
        botaoBuscarNaPagina = findViewById(R.id.botao_buscar_na_pagina_do_navegador)
        faixaDeBuscaNaPagina = findViewById(R.id.faixa_de_busca_na_pagina_do_navegador)
        campoDeBuscaNaPagina = findViewById(R.id.campo_de_busca_na_pagina_do_navegador)
        textoDaContagemDaBuscaNaPagina = findViewById(R.id.texto_da_contagem_da_busca_na_pagina)
        botaoDiferenciarMaiusculasNaBusca =
            findViewById(R.id.botao_diferenciar_maiusculas_na_busca_na_pagina)
        botaoBibliotecaNaBarra = findViewById(R.id.botao_abrir_biblioteca_de_videos_na_barra_de_navegacao)
        botaoChatNaBarra = findViewById(R.id.botao_abrir_chat_da_openrouter)
        painelDoCampoDeTexto = PainelDoCampoDeTextoDoNavegador(
            this,
            preferencias,
            { paginas[gestao.idDaAbaAtiva]?.webView },
        ) { endereco ->
            navegarNaAba(gestao.idDaAbaAtiva, endereco)
        }
        painelDoCampoDeTexto.ligar(ponteDoCampoDeTexto)
        botaoCampoDeTexto.setOnClickListener {
            painelDoCampoDeTexto.alternar()
        }
        botaoMenuOverflow.setOnClickListener { mostrarMenuOverflowDaBarra() }
        painelDaColecao = GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador(
            this,
            ::baixarMarcadosDaColecao,
            ::abrirVideoDaColecao,
        ) { endereco ->
            navegarNaAba(gestao.idDaAbaAtiva, endereco)
        }
        painelDaColecao.ligar()
        gravacaoDosRoteirosDeAutomacao = GravacaoDosRoteirosDeAutomacaoDaPaginaNoArmazenamentoDoAplicativo(this)
        // Limpeza automática do armazenamento (se ligada), sem travar a abertura.
        Thread { runCatching { MedicaoELimpezaDoArmazenamentoDoAplicativo(this).rodarLimpezaAutomaticaSeVencida() } }.start()
        painelDoChat = PainelDoChatDaOpenRouterNoNavegador(
            this,
            cofreDaChaveDaOpenRouter,
            lerModelo = { preferencias.modeloDoChat },
            gravarModelo = { preferencias.modeloDoChat = it },
            lerModeloDoModoChat = { preferencias.modeloDoChatNoModoConversa },
            executarScriptNaPagina = { script, aoFim ->
                val vista = paginas[gestao.idDaAbaAtiva]?.webView
                if (vista == null) {
                    aoFim("")
                } else {
                    vista.evaluateJavascript(script) { bruto ->
                        val resultado = textoDoJavascript(bruto)
                        handlerDaEsperaDoChat.postDelayed({ aoFim(resultado) }, 400)
                    }
                }
            },
            lerPaginaEmSegundoPlano = { endereco, aoFim -> leitorEmSegundoPlano.ler(endereco, aoFim) },
            editarPaginaDaColecao = { titulo, editar -> painelDaColecao.editarPagina(titulo, editar) },
            gravarModeloDoModoChat = { preferencias.modeloDoChatNoModoConversa = it },
            lerTextoDaPagina = { aoReceber ->
                val id = gestao.idDaAbaAtiva
                val vista = paginas[id]?.webView
                if (vista == null) {
                    aoReceber("", "", "", "")
                } else if (id in abasCarregando) {
                    acompanharCarregamento(id, jaViuComeco = true) { espera ->
                        val relato = if (paginas[id] == null) {
                            "A aba foi fechada."
                        } else {
                            RelatoDoCarregamentoDaPaginaParaOChat.relato(
                                endereco = vista.url.orEmpty(),
                                titulo = vista.title.orEmpty(),
                                texto = espera.texto,
                                sucesso = espera.sucesso,
                                tempoEsgotado = espera.tempoEsgotado,
                            )
                        }
                        aoReceber(vista.title.orEmpty(), vista.url.orEmpty(), espera.texto, relato)
                    }
                } else {
                    lerTextoVisivelDaAba(id) { texto ->
                        aoReceber(vista.title.orEmpty(), vista.url.orEmpty(), texto, "")
                    }
                }
            },
            lerSelecao = { aoReceber ->
                val vista = paginas[gestao.idDaAbaAtiva]?.webView
                if (vista == null) {
                    aoReceber("")
                } else {
                    vista.evaluateJavascript(com.chromebrothers.navegador.regras.FerramentasDeLeituraDoChatDaOpenRouter.scriptDaSelecao()) { bruto ->
                        runOnUiThread { aoReceber(textoDoJavascript(bruto)) }
                    }
                }
            },
            listarMidia = { aoReceber ->
                lerDaPaginaDepoisDeCarregar(aoReceber) { pagina ->
                    if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(pagina.webView.url)) {
                        aoReceber("Nenhuma mídia encontrada nesta página.")
                        return@lerDaPaginaDepoisDeCarregar
                    }
                    val capturada = com.chromebrothers.navegador.regras.TextoDaMidiaObservadaParaOChat.descrever(
                        pagina.pedidosDeMidiaObservados(),
                    )
                    avaliarJavascriptDaPaginaComLimite(
                        pagina = pagina,
                        script = com.chromebrothers.navegador.regras.EnderecosVisiveisDaPaginaParaOChat.scriptDaMidia(),
                        aoFalhar = { aoReceber("Não consegui ler a mídia da página a tempo.") },
                    ) { bruto ->
                        val visivel = com.chromebrothers.navegador.regras.EnderecosVisiveisDaPaginaParaOChat.descreverMidia(
                            textoDoJavascript(bruto),
                        )
                        aoReceber(
                            com.chromebrothers.navegador.regras.TextoDaMidiaObservadaParaOChat.relato(capturada, visivel),
                        )
                    }
                }
            },
            lerEnderecos = { aoReceber ->
                lerDaPaginaDepoisDeCarregar(aoReceber) { pagina ->
                    if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(pagina.webView.url)) {
                        aoReceber("Nenhum endereço http encontrado na página.")
                        return@lerDaPaginaDepoisDeCarregar
                    }
                    avaliarJavascriptDaPaginaComLimite(
                        pagina = pagina,
                        script = com.chromebrothers.navegador.regras.EnderecosVisiveisDaPaginaParaOChat.scriptDosEnderecos(),
                        aoFalhar = { aoReceber("Não consegui ler os endereços da página a tempo.") },
                    ) { bruto ->
                        aoReceber(
                            com.chromebrothers.navegador.regras.EnderecosVisiveisDaPaginaParaOChat.descreverEnderecos(
                                textoDoJavascript(bruto),
                            ),
                        )
                    }
                }
            },
            listarCampos = { aoReceber ->
                lerDaPaginaDepoisDeCarregar(aoReceber) { pagina ->
                    if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(pagina.webView.url)) {
                        aoReceber(
                            com.chromebrothers.navegador.regras.CamposVinculaveisDaPaginaParaOChat.descrever(emptyList()),
                        )
                        return@lerDaPaginaDepoisDeCarregar
                    }
                    avaliarJavascriptDaPaginaComLimite(
                        pagina = pagina,
                        script = com.chromebrothers.navegador.regras.CamposVinculaveisDaPaginaParaOChat.script(),
                        aoFalhar = { aoReceber("Não consegui ler os campos da página a tempo.") },
                    ) { bruto ->
                        aoReceber(
                            com.chromebrothers.navegador.regras.CamposVinculaveisDaPaginaParaOChat.descrever(
                                com.chromebrothers.navegador.regras.CamposVinculaveisDaPaginaParaOChat.ler(
                                    textoDoJavascript(bruto),
                                ),
                            ),
                        )
                    }
                }
            },
            lerTextoPaginado = { argumentos, aoReceber ->
                lerDaPaginaDepoisDeCarregar(aoReceber) { pagina ->
                    val pedido = com.chromebrothers.navegador.regras.LeituraPaginadaDoTextoDaPaginaParaOChat.lerPedido(argumentos)
                    lerTextoCompletoDaAba(pagina.id) { texto ->
                        val normalizado = com.chromebrothers.navegador.regras.LeituraPaginadaDoTextoDaPaginaParaOChat.normalizar(texto)
                        val fatia = com.chromebrothers.navegador.regras.LeituraPaginadaDoTextoDaPaginaParaOChat.fatiar(
                            normalizado,
                            pedido.offset,
                            pedido.limite,
                        )
                        aoReceber(
                            com.chromebrothers.navegador.regras.LeituraPaginadaDoTextoDaPaginaParaOChat.mensagem(
                                pagina.webView.title.orEmpty(),
                                pagina.webView.url.orEmpty(),
                                fatia,
                            ),
                        )
                    }
                }
            },
            consultarElementos = { argumentos, aoReceber ->
                when (
                    val pedido = com.chromebrothers.navegador.regras.ConsultaDeElementosDaPaginaPorSeletorCssParaOChat.lerPedido(argumentos)
                ) {
                    is com.chromebrothers.navegador.regras.ResultadoDoPedidoDeConsulta.Erro -> aoReceber(pedido.mensagem)
                    is com.chromebrothers.navegador.regras.ResultadoDoPedidoDeConsulta.Ok ->
                        lerDaPaginaDepoisDeCarregar(aoReceber) { pagina ->
                            pagina.webView.evaluateJavascript(
                                com.chromebrothers.navegador.regras.ConsultaDeElementosDaPaginaPorSeletorCssParaOChat.script(
                                    pedido.seletor,
                                    pedido.offset,
                                    pedido.limite,
                                ),
                            ) { bruto ->
                                runOnUiThread {
                                    aoReceber(
                                        com.chromebrothers.navegador.regras.ConsultaDeElementosDaPaginaPorSeletorCssParaOChat.descrever(
                                            textoDoJavascript(bruto),
                                            pedido.offset,
                                            pedido.limite,
                                        ),
                                    )
                                }
                            }
                        }
                }
            },
            rolarPagina = { argumentos, aoReceber ->
                when (
                    val pedido = com.chromebrothers.navegador.regras.RolagemDaPaginaDaAbaParaOChat.lerPedido(argumentos)
                ) {
                    is com.chromebrothers.navegador.regras.ResultadoDoPedidoDeRolagem.Erro -> aoReceber(pedido.mensagem)
                    is com.chromebrothers.navegador.regras.ResultadoDoPedidoDeRolagem.Ok ->
                        lerDaPaginaDepoisDeCarregar(aoReceber) { pagina ->
                            pagina.webView.evaluateJavascript(
                                com.chromebrothers.navegador.regras.RolagemDaPaginaDaAbaParaOChat.script(pedido.pedido),
                            ) { bruto ->
                                runOnUiThread {
                                    aoReceber(
                                        com.chromebrothers.navegador.regras.RolagemDaPaginaDaAbaParaOChat.mensagem(
                                            textoDoJavascript(bruto),
                                        ),
                                    )
                                }
                            }
                        }
                }
            },
            capturarAba = capturar@{ aoReceber ->
                val vista = paginas[gestao.idDaAbaAtiva]?.webView
                if (vista == null) {
                    aoReceber(null, "Não há página aberta.")
                    return@capturar
                }
                vista.capturarImagemDaPagina { bitmap ->
                    if (bitmap == null || bitmap.isRecycled) {
                        aoReceber(null, "Não deu para capturar a aba.")
                        return@capturarImagemDaPagina
                    }
                    Thread {
                        val preparada = CompactacaoDoBitmapDaCapturaDaAbaParaOChatDoNavegador.preparar(
                            this,
                            bitmap,
                        )
                        val relato = if (preparada == null) {
                            "Não deu para preparar a captura."
                        } else {
                            com.chromebrothers.navegador.regras.PreparacaoDaCapturaDaAbaParaOChat.relato(
                                preparada.largura,
                                preparada.altura,
                                preparada.enderecoArquivo,
                            )
                        }
                        if (!bitmap.isRecycled) bitmap.recycle()
                        runOnUiThread {
                            if (!isDestroyed) aoReceber(preparada?.parte, relato)
                        }
                    }.start()
                }
            },
            descreverAbas = {
                com.chromebrothers.navegador.regras.FerramentasDeLeituraDoChatDaOpenRouter.textoDasAbas(
                    gestao.listar(),
                    gestao.idDaAbaAtiva,
                )
            },
            rotuloDaAbaAtual = {
                val titulo = gestao.ativa()?.titulo?.trim().orEmpty().ifBlank { "Sem título" }
                "Aba: $titulo"
            },
            escolherImagem = { aoReceber ->
                aoReceberAnexoDoChat = aoReceber
                seletorDeImagemDoChat.launch("image/*")
            },
            escolherArquivo = { aoReceber ->
                aoReceberAnexoDoChat = aoReceber
                seletorDeArquivoDoChat.launch(
                    arrayOf("application/pdf", "text/plain", "text/markdown", "text/x-markdown"),
                )
            },
            lerAnexo = { uri, aoReceber -> leituraDeAnexoDoChat.ler(uri, aoReceber) },
            abrirEndereco = { texto, aoFim ->
                val endereco = InterpretacaoDoTextoDaBarraDeEndereco.resolver(texto, preferencias.motor)
                if (!endereco.startsWith("http://") && !endereco.startsWith("https://")) {
                    aoFim("Não abri: só uso http ou https.")
                } else {
                    val id = gestao.idDaAbaAtiva
                    acompanharCarregamento(id, jaViuComeco = false) { espera ->
                        aoFim(relatoDoCarregamento(id, espera, criouAba = false))
                    }
                    navegarNaAba(id, texto)
                }
            },
            criarAba = { texto, aoFim ->
                if (texto.isBlank()) {
                    abrirNovaAba()
                    aoFim("Criei uma aba em branco.")
                } else {
                    val endereco = InterpretacaoDoTextoDaBarraDeEndereco.resolver(texto, preferencias.motor)
                    if (!endereco.startsWith("http://") && !endereco.startsWith("https://")) {
                        aoFim("Não criei a aba: só uso http ou https.")
                    } else {
                        val aba = gestao.criarAba(endereco = endereco, titulo = getString(R.string.nova_aba))
                        acompanharCarregamento(aba.id, jaViuComeco = false) { espera ->
                            aoFim(relatoDoCarregamento(aba.id, espera, criouAba = true))
                        }
                        criarPagina(aba.id, aba.endereco)
                        mostrarAba(aba.id)
                    }
                }
            },
            preencherCampo = { texto, aoFim ->
                val vista = paginas[gestao.idDaAbaAtiva]?.webView
                if (vista == null) {
                    aoFim("Não há página aberta.")
                } else {
                    vista.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.espelhar(texto)) { bruto ->
                        runOnUiThread {
                            when (textoDoJavascript(bruto)) {
                                "sem-campo" -> aoFim("Não há campo vinculado nesta página.")
                                "falhou" -> aoFim("Não consegui preencher o campo.")
                                else -> aoFim("Preenchi o campo da página.")
                            }
                        }
                    }
                }
            },
            escreverNaPagina = { texto, aoFim ->
                val vista = paginas[gestao.idDaAbaAtiva]?.webView
                if (vista == null) {
                    aoFim(EscritaDeTextoNoEditorDaPaginaParaOChat.relato(EscritaDeTextoNoEditorDaPaginaParaOChat.SEM_PAGINA))
                } else {
                    vista.requestFocus()
                    vista.evaluateJavascript(EscritaDeTextoNoEditorDaPaginaParaOChat.script(texto)) { bruto ->
                        runOnUiThread {
                            val codigo = textoDoJavascript(bruto).ifBlank {
                                EscritaDeTextoNoEditorDaPaginaParaOChat.FALHOU
                            }
                            aoFim(EscritaDeTextoNoEditorDaPaginaParaOChat.relato(codigo))
                        }
                    }
                }
            },
            clicarNoTexto = { texto, aoFim ->
                val vista = paginas[gestao.idDaAbaAtiva]?.webView
                if (vista == null) {
                    aoFim("Não há página aberta.")
                } else {
                    vista.evaluateJavascript(CliqueNoTextoVisivelDaPaginaParaOChat.script(texto)) { bruto ->
                        val resultado = textoDoJavascript(bruto)
                        handlerDaEsperaDoChat.postDelayed({
                            if (resultado == "ok") aoFim("Cliquei em $texto.")
                            else aoFim("Não achei \"$texto\" na página.")
                        }, 400)
                    }
                }
            },
            cadernoDeNotas = CadernoDeNotasDosSitesNoAparelho(this),
            sitesComNavegacaoLiberada = ListaDeSitesComNavegacaoLiberadaNoAparelho(this),
            descreverColecao = { painelDaColecao.descrever() },
            publicarPaginaDaColecao = { titulo, html -> painelDaColecao.publicarPagina(titulo, html) },
            iniciarEntregaDaColecao = { painelDaColecao.iniciarEntregaDoChat() },
            concluirEntregaDaColecao = { painelDaColecao.concluirEntregaDoChat() },
            listarComponentesDaBiblioteca = {
                val itens = if (::bibliotecaDeComponentesDaColecao.isInitialized) {
                    bibliotecaDeComponentesDaColecao.listar()
                } else {
                    emptyList()
                }
                com.chromebrothers.navegador.regras.FerramentasDeLeituraDoChatDaOpenRouter
                    .textoDosComponentesDaBiblioteca(itens)
            },
            guardarComponenteDaColecao = { descricao, tags ->
                if (!::bibliotecaDeComponentesDaColecao.isInitialized || !::gravacaoDaBibliotecaDeComponentes.isInitialized) {
                    "A biblioteca de componentes ainda não está pronta."
                } else {
                    val mensagem = painelDaColecao.guardarNaBiblioteca(
                        bibliotecaDeComponentesDaColecao,
                        gravacaoDaBibliotecaDeComponentes,
                        System.currentTimeMillis(),
                        descricao,
                        tags,
                    ) { UUID.randomUUID().toString() }
                    if (::painelDaBibliotecaDeVideos.isInitialized && painelDaBibliotecaDeVideos.estaAberto()) {
                        painelDaBibliotecaDeVideos.atualizar()
                    }
                    mensagem
                }
            },
            abrirComponenteDaBiblioteca = { id, nome ->
                if (!::bibliotecaDeComponentesDaColecao.isInitialized) {
                    "A biblioteca de componentes ainda não está pronta."
                } else {
                    val item = if (id.isNotBlank()) {
                        bibliotecaDeComponentesDaColecao.buscar(id)
                    } else {
                        bibliotecaDeComponentesDaColecao.buscarPorNome(nome)
                    }
                    if (item == null) {
                        "Não achei esse componente na biblioteca."
                    } else {
                        abrirComponenteDaBibliotecaRespeitandoAAberturaPadrao(item)
                    }
                }
            },
            apagarComponenteDaBiblioteca = { id ->
                if (!::bibliotecaDeComponentesDaColecao.isInitialized || !::gravacaoDaBibliotecaDeComponentes.isInitialized) {
                    "A biblioteca de componentes ainda não está pronta."
                } else if (!bibliotecaDeComponentesDaColecao.apagar(id)) {
                    "Não achei esse componente na biblioteca."
                } else {
                    gravacaoDaBibliotecaDeComponentes.apagarArquivos(id)
                    gravacaoDaBibliotecaDeComponentes.gravar(bibliotecaDeComponentesDaColecao)
                    painelDaColecao.limparVinculoDaOrigem(id)
                    if (::painelDaBibliotecaDeVideos.isInitialized && painelDaBibliotecaDeVideos.estaAberto()) {
                        painelDaBibliotecaDeVideos.atualizar()
                    }
                    "Apaguei o componente da biblioteca."
                }
            },
            lerCodigoDoComponente = { id ->
                if (!::bibliotecaDeComponentesDaColecao.isInitialized) {
                    "A biblioteca de componentes ainda não está pronta."
                } else {
                    val item = bibliotecaDeComponentesDaColecao.buscar(id)
                    if (item == null) {
                        "Não achei esse componente na biblioteca."
                    } else {
                        com.chromebrothers.navegador.regras.FerramentasDeLeituraDoChatDaOpenRouter
                            .textoDoCodigoDoComponente(item.html)
                    }
                }
            },
            capturarComponente = { id, aoReceber -> capturarComponenteParaOChat(id, aoReceber) },
            descreverVinculo = { painelDoCampoDeTexto.descreverVinculo() },
            vincularCampo = { campo, envio, aoFim -> vincularCampoPeloChat(campo, envio, aoFim) },
            siteDaAba = {
                paginas[gestao.idDaAbaAtiva]?.webView?.url.orEmpty()
            },
            descreverVideoDetectado = { descreverVideoDetectadoParaOChat() },
            listarVideosDaBiblioteca = { listarVideosDaBibliotecaParaOChat() },
            listarImagensDaBiblioteca = { listarImagensDaBibliotecaParaOChat() },
            mostrarBotaoFlutuanteDoVideo = { mostrarBotaoFlutuanteDoVideoPeloChat() },
            abrirPlayerDoVideoDetectado = { abrirPlayerDoVideoDetectadoPeloChat() },
            abrirVideoDaBiblioteca = { chave -> abrirVideoDaBibliotecaPeloChat(chave) },
            abrirImagemDaBiblioteca = { id -> abrirImagemDaBibliotecaPeloChat(id) },
            bytesDaImagemDaBiblioteca = { id -> bytesDaImagemDaBibliotecaParaOChat(id) },
            carregarRoteirosDeAutomacao = {
                if (!::gravacaoDosRoteirosDeAutomacao.isInitialized) {
                    emptyList()
                } else {
                    gravacaoDosRoteirosDeAutomacao.listar()
                }
            },
            guardarRoteiroDeAutomacao = { roteiro ->
                if (!::gravacaoDosRoteirosDeAutomacao.isInitialized) {
                    "Os roteiros ainda não estão prontos."
                } else {
                    gravacaoDosRoteirosDeAutomacao.guardar(roteiro)
                }
            },
            rodarRoteiroDeAutomacao = { idOuNome, aoPasso, aoFim ->
                rodarRoteiroDeAutomacaoPeloChat(idOuNome, aoPasso, aoFim)
            },
            cancelarRoteiroDeAutomacao = {
                executorDoRoteiroEmAndamento?.cancelar()
            },
        )
        painelDoChat.ligar()
        painelDoGravadorDeRoteiro = PainelDoGravadorDeRoteiroDeAutomacaoDoNavegador(
            activity = this,
            preferencias = preferencias,
            urlDaAba = {
                paginas[gestao.idDaAbaAtiva]?.webView?.url
                    ?: gestao.ativa()?.endereco.orEmpty()
            },
            densidadeDaPagina = { resources.displayMetrics.density },
            avaliarJavascript = { script, aoReceber ->
                val vista = paginas[gestao.idDaAbaAtiva]?.webView
                if (vista == null) {
                    aoReceber?.invoke(null)
                } else {
                    vista.evaluateJavascript(script) { bruto ->
                        runOnUiThread { aoReceber?.invoke(bruto) }
                    }
                }
            },
            navegar = { url -> navegarNaAba(gestao.idDaAbaAtiva, url) },
            salvar = { roteiro -> gravacaoDosRoteirosDeAutomacao.guardar(roteiro) },
            apagarRoteiro = { id -> gravacaoDosRoteirosDeAutomacao.apagar(id) },
            listarRoteiros = { gravacaoDosRoteirosDeAutomacao.listar() },
            rodar = { roteiro, variaveis, aoPasso, aoFim ->
                executarRoteiroDeAutomacaoEmMemoria(roteiro, aoPasso, aoFim, variaveis)
            },
            cancelarExecucao = { executorDoRoteiroEmAndamento?.cancelar() },
            enviarJsonAoChat = { json ->
                painelDoChat.mostrarHostDoGravador(false)
                painelDoChat.receberDoComponente(
                    "Receita de roteiro (JSON):\n$json\n\nPode guardar com guardar_roteiro_de_automacao ou só revisar.",
                )
            },
            abrirGavetaDoChat = { painelDoChat.abrirGavetaSeFechada() },
            fecharGavetaDoChat = { painelDoChat.fecharGavetaSeAberta() },
            mostrarHostNaGaveta = { mostrar -> painelDoChat.mostrarHostDoGravador(mostrar) },
            mostrarEscolhaSimples = { titulo, opcoes, indice, aoEscolher ->
                mostrarCaixaDeEscolhaSimplesDoNavegador(titulo, opcoes, indice, aoEscolher)
            },
            temChaveDaOpenRouter = { cofreDaChaveDaOpenRouter.temChave() },
            consultarIa = { mensagens, aoFim ->
                val chave = cofreDaChaveDaOpenRouter.ler()
                if (chave.isEmpty()) {
                    aoFim(null, getString(R.string.faltou_chave_openrouter_no_gravador))
                } else {
                    val acumulado = StringBuilder()
                    clienteDaIaDoGravadorDeRoteiro.cancelar()
                    clienteDaIaDoGravadorDeRoteiro.enviar(
                        chave = chave,
                        modelo = preferencias.modeloDoChat,
                        mensagens = mensagens,
                        comFerramentas = false,
                        aoTrecho = { acumulado.append(it) },
                        aoTerminar = { fim ->
                            if (fim.erro != null) {
                                aoFim(null, fim.erro)
                            } else {
                                aoFim(acumulado.toString(), null)
                            }
                        },
                    )
                }
            },
            cancelarConsultaIa = { clienteDaIaDoGravadorDeRoteiro.cancelar() },
        )
        painelDoChat.aoAbrirAGaveta = {
            abrirComponentesQueEntramJuntoComOChat()
            // Os componentes de geração MCP abrem com o catálogo do Higgsfield já em memória.
            servicoDeGeracaoDeImagemViaMcpDaColecao.preaquecerCatalogosEmSegundoPlano()
        }
        controladorDasAbasAncoradasNaGaveta = ControladorDasAbasAncoradasNaGavetaDoChatDoNavegador(
            activity = this,
            gestor = painelDaColecao,
            abrirGaveta = { painelDoChat.abrirGavetaSeFechada() },
            aoPedirAbrirGravadorNaGaveta = {
                if (::painelDoGravadorDeRoteiro.isInitialized) {
                    painelDoGravadorDeRoteiro.abrir("gaveta")
                }
            },
            aoPedirMenuDeModoDoGravador = {
                if (::painelDoGravadorDeRoteiro.isInitialized) {
                    painelDoGravadorDeRoteiro.abrirMenuDeModo()
                }
            },
        )
        painelDoChat.definirAoAlternarHostDoGravador { mostrar ->
            if (::controladorDasAbasAncoradasNaGaveta.isInitialized) {
                controladorDasAbasAncoradasNaGaveta.aoMostrarGravador(mostrar)
            }
        }
        painelDoChat.definirAoRestaurarPaineisDaGaveta {
            if (::controladorDasAbasAncoradasNaGaveta.isInitialized) {
                controladorDasAbasAncoradasNaGaveta.reaplicarPainelAtivo()
            }
        }
        painelDoChat.definirAoLayoutDaGavetaEstabilizado {
            if (::painelDaColecao.isInitialized) {
                painelDaColecao.notificarHostRedimensionadoNasJanelas()
            }
        }
        controladorDasAbasAncoradasNaGaveta.ligar()
        if (::painelDoCampoDeTexto.isInitialized) {
            controladorDasAbasAncoradasNaGaveta.ligarCampoDeTexto(painelDoCampoDeTexto)
            painelDoCampoDeTexto.ligarArrastePelaBarra { x, y, fase -> tratarArrasteDeJanelaPelaBarra(x, y, fase) }
        }
        painelDaColecao.ligarArrastePelaBarra { x, y, fase -> tratarArrasteDeJanelaPelaBarra(x, y, fase) }
        painelDoGravadorDeRoteiro.ligar()
        painelDaColecao.ligarEnvioAoChat { texto ->
            painelDoChat.receberDoComponente(texto)
        }
        painelDaColecao.ligarUrlDaAbaAtiva {
            paginas[gestao.idDaAbaAtiva]?.webView?.url
                ?: gestao.ativa()?.endereco.orEmpty()
        }
        painelDaColecao.ligarTituloDaAbaAtiva {
            paginas[gestao.idDaAbaAtiva]?.webView?.title.orEmpty()
        }
        painelDaColecao.ligarTextoSelecionadoDaAba { aoReceber ->
            val vista = paginas[gestao.idDaAbaAtiva]?.webView
            if (vista == null) {
                aoReceber("")
            } else {
                vista.evaluateJavascript(
                    com.chromebrothers.navegador.regras.FerramentasDeLeituraDoChatDaOpenRouter.scriptDaSelecao(),
                ) { bruto ->
                    aoReceber(textoDoJavascript(bruto))
                }
            }
        }
        painelDaColecao.ligarAbrirNaAba { destino ->
            navegarNaAba(gestao.idDaAbaAtiva, destino)
        }
        painelDaColecao.ligarRodarRoteiro { idOuNome, aoFim ->
            rodarRoteiroDeAutomacaoPeloChat(idOuNome, aoFim = aoFim)
        }
        painelDaColecao.ligarCopiarTexto { bruto ->
            val texto = bruto.trim()
            if (texto.isEmpty()) {
                avisar(R.string.texto_vazio_para_copiar_pelo_componente_da_colecao)
                return@ligarCopiarTexto
            }
            val area = getSystemService(ClipboardManager::class.java) ?: return@ligarCopiarTexto
            area.setPrimaryClip(ClipData.newPlainText("texto", texto.take(100_000)))
            avisar(R.string.texto_copiado_pelo_componente_da_colecao)
        }
        painelDaColecao.ligarSeletorDeArquivoDoAparelho { callback, params ->
            callbackDeArquivoDaColecao?.onReceiveValue(null)
            if (::painelDaColecao.isInitialized) {
                callbackDeArquivoDaColecao?.let { painelDaColecao.limparCallbackDeArquivoSeFor(it) }
            }
            callbackDeArquivoDaColecao = callback
            try {
                seletorDeArquivoDaColecao.launch(params.createIntent())
                true
            } catch (_: ActivityNotFoundException) {
                callbackDeArquivoDaColecao = null
                false
            }
        }
        painelDaColecao.ligarEscolherPastaDoAparelho { painel ->
            painelPedindoPastaDaColecao = painel
            try {
                seletorDePastaDaColecao.launch(null)
            } catch (_: ActivityNotFoundException) {
                painelPedindoPastaDaColecao = null
                painel.entregarJsonDaPastaEscolhida(
                    com.chromebrothers.navegador.regras.ListagemDePastaEscolhidaPeloComponenteDaColecao
                        .mensagemErro("Não há seletor de pasta neste aparelho."),
                )
            }
        }
        painelDaColecao.ligarListarPastaDoAparelho { uriTexto ->
            if (com.chromebrothers.navegador.regras.PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
                    .ehUriVirtual(uriTexto)
            ) {
                return@ligarListarPastaDoAparelho leituraDaPastaVirtualDeMidiaDoChromeBrothers.descrever()
            }
            val uri = runCatching { Uri.parse(uriTexto) }.getOrNull()
                ?: return@ligarListarPastaDoAparelho com.chromebrothers.navegador.regras
                    .ListagemDePastaEscolhidaPeloComponenteDaColecao.mensagemErro("Uri inválida.")
            leituraDePastaDaColecao.descrever(uri)
        }
        painelDaColecao.ligarExcluirArquivosDoAparelho { urisJson -> excluirArquivosDoVisualizadorDeMidia(urisJson) }
        painelDaColecao.ligarAbrirImagemNaAba { uriTexto ->
            abrirImagemDoComponenteNaAba(uriTexto)
        }
        painelDaColecao.ligarAbrirImagemNoVisor { uriTexto ->
            abrirImagemDoComponenteNoVisorOuEditor(uriTexto, noEditor = false)
        }
        painelDaColecao.ligarAbrirImagemNoEditor { uriTexto ->
            abrirImagemDoComponenteNoVisorOuEditor(uriTexto, noEditor = true)
        }
        painelDaColecao.ligarEnviarImagemParaCamadaDoEditor { _ ->
            // Colar automático desligado: só lift-and-drag.
            com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                .mensagemOk("camada", "use_arraste")
        }
        painelDaColecao.ligarForcarEncerrarArrasteDeImagemDoComponente {
            encerrarFantasmaDoArrasteDeImagemDoComponente()
        }
        painelDaColecao.ligarIniciarArrasteComMiniatura { uriTexto, x, y, miniatura ->
            iniciarFantasmaDoArrasteDeImagemDoComponenteComMiniatura(uriTexto, x, y, miniatura)
        }
        painelDaColecao.ligarIniciarArrasteDeImagemParaOEditor { uriTexto, x, y ->
            iniciarFantasmaDoArrasteDeImagemDoComponente(uriTexto, x, y)
        }
        painelDaColecao.ligarAtualizarArrasteDeImagemParaOEditor { x, y ->
            atualizarFantasmaDoArrasteDeImagemDoComponente(x, y)
        }
        painelDaColecao.ligarSoltarArrasteDeImagemParaOEditor { uriTexto, x, y ->
            soltarArrasteDoComponenteUmaVez(uriTexto, x, y)
        }
        painelDaColecao.ligarGuardarImagem { uriTexto ->
            guardarImagemDoComponenteNaBiblioteca(uriTexto)
        }
        painelDaColecao.ligarCopiarImagem { uriTexto ->
            copiarImagemDoComponenteParaAreaDeTransferencia(uriTexto)
        }
        painelDaColecao.ligarAbrirVideoNoPlayer { uriTexto ->
            abrirVideoDoComponenteNoPlayer(uriTexto)
        }
        painelDaColecao.ligarAbrirVisorExternoNaGaveta { uris, indice ->
            // A imagem mora na aba Visor (criada ou reaproveitada), não por cima da página atual.
            garantirAbaDoVisor()
            visorExternoDaGaveta().mostrar(uris, indice)
        }
        painelDaColecao.ligarAbrirVideosAoLado { painel, itensJson, indice ->
            abrirVideosDoComponenteAoLado(painel, itensJson, indice)
        }
        painelDaColecao.ligarVideosDaGradeDoVisualizador(
            abrirAoLado = { uris, indice -> abrirVideosDaGradeDoVisualizadorAoLado(uris, indice) },
            mostrarMenu = { uri, reproduzirAqui -> mostrarMenuDoVideoDaGradeDoVisualizador(uri, reproduzirAqui) },
        )
        painelDaColecao.ligarServicoDeGeracaoDeImagemViaMcp(servicoDeGeracaoDeImagemViaMcpDaColecao)
        painelDaColecao.ligarEscolherImagemDeReferenciaDaGeracao { painel, origem ->
            escolherImagemDeReferenciaDaGeracao(painel, origem)
        }
        painelDaColecao.ligarSoltarItemDaBibliotecaParaReferenciaMcp { painel, evento, tipoDoGerador ->
            soltarItemDaBibliotecaComoReferenciaDaGeracao(painel, evento, tipoDoGerador)
        }
        painelDaColecao.ligarAoTerminarGeracaoDeImagemViaMcp {
            // A geração grava direto no disco; recarrega a biblioteca em memória.
            sincronizarBibliotecaDeVideos()
            atualizarVisualizadorDeMidiaComArquivosDoChromeBrothers()
        }
        botaoChatNaBarra.setOnClickListener {
            painelDoChat.alternar()
        }
        botaoChatNaBarra.setOnLongClickListener {
            if (::painelDoGravadorDeRoteiro.isInitialized) {
                painelDoGravadorDeRoteiro.abrirComPadrao()
            }
            true
        }
        interruptorDeCapturaDeMidia = findViewById(R.id.interruptor_de_captura_de_midia_para_download)
        interruptorDeCapturaDeMidia.isChecked = preferencias.capturarMidiaParaDownload
        interruptorDeCapturaDeMidia.setOnCheckedChangeListener { _, ligado ->
            definirCapturaDeMidiaParaDownload(ligado)
        }
        interruptorDeBloqueioDeAnuncio = findViewById(R.id.interruptor_de_bloqueio_de_anuncio)
        interruptorDeBloqueioDeAnuncio.isChecked = preferencias.bloquearAnuncios
        atualizacaoDaListaDeBloqueio = AtualizacaoDaListaDeBloqueioDeAnuncioDoNavegador(
            this,
            instanteSalvo = { preferencias.instanteDaListaDeBloqueioDeAnuncio },
            gravarInstante = { preferencias.instanteDaListaDeBloqueioDeAnuncio = it },
            aoCarregar = {
                listaDeBloqueioDeAnuncio = it
                publicarPacoteDeBloqueioDeAnuncio()
            },
        )
        if (preferencias.bloquearAnuncios) atualizacaoDaListaDeBloqueio.carregar()
        interruptorDeBloqueioDeAnuncio.setOnCheckedChangeListener { _, ligado ->
            if (ligado == preferencias.bloquearAnuncios) return@setOnCheckedChangeListener
            preferencias.bloquearAnuncios = ligado
            paginas.values.forEach { it.webView.definirProtecaoContraRastreamento(ligado) }
            if (ligado) atualizacaoDaListaDeBloqueio.carregar()
            publicarPacoteDeBloqueioDeAnuncio()
            avisar(if (ligado) R.string.bloqueio_de_anuncio_ligado else R.string.bloqueio_de_anuncio_desligado)
        }
        publicarPacoteDeBloqueioDeAnuncio()
        capturaNativaDeTela = CapturaNativaDeTelaDoNavegadorChromebrothers(
            activity = this,
            raizDaPagina = areaVisivel,
            paginaAtiva = { paginas[gestao.idDaAbaAtiva]?.webView },
            aoAbrirCapturaEmNovaAba = { endereco -> abrirCapturaNativaEmNovaAba(endereco) },
        )
        botaoCapturaDeTela.setOnClickListener {
            capturaNativaDeTela.mostrarOpcoes()
        }
        ligarBuscaNaPagina()
        botaoFlutuante = findViewById(R.id.botao_flutuante_abrir_player_nativo)
        botaoBaixarMidia = findViewById(R.id.botao_baixar_midia_da_pagina)
        botaoBaixarMidia.imageTintList = ColorStateList.valueOf(Color.parseColor("#1A1D24"))
        // Toque: melhor qualidade direto para a Galeria. Segurar: lista de opções (salva em Downloads).
        botaoBaixarMidia.contentDescription = getString(R.string.enviar_video_para_a_galeria)
        botaoBaixarMidia.tooltipText = getString(R.string.dica_do_botao_enviar_video_para_a_galeria)
        botaoBaixarMidia.setOnClickListener { enviarMelhorMidiaDaAbaAtualParaAGaleria() }
        botaoBaixarMidia.setOnLongClickListener {
            oferecerDownloadDaMidiaDaAbaAtual()
            true
        }
        atualizarBotaoDeBaixarMidia()
        painelDoPlayerDaPagina = findViewById(R.id.painel_do_player_controlado_do_video_na_pagina)
        superficieDoPlayerDaPagina = findViewById(R.id.superficie_do_video_no_player_da_pagina)
        barraSuperiorDoPlayerDaPagina = findViewById(R.id.barra_superior_do_player_da_pagina)
        barraInferiorDoPlayerDaPagina = findViewById(R.id.barra_inferior_do_player_da_pagina)
        controlesCentraisDoPlayerDaPagina = findViewById(R.id.controles_centrais_do_player_da_pagina)
        avisoDoDeslocamentoDoPlayerDaPagina = findViewById(R.id.aviso_do_deslocamento_do_player_da_pagina)
        listaDeIncrementosDoPlayerDaPagina = findViewById(R.id.lista_de_incrementos_do_player_da_pagina)
        botaoPausarPlayerDaPagina = findViewById(R.id.botao_pausar_player_da_pagina)
        barraDePosicaoDoPlayerDaPagina = findViewById(R.id.barra_de_posicao_do_player_da_pagina)
        textoDaPosicaoDoPlayerDaPagina = findViewById(R.id.texto_da_posicao_do_player_da_pagina)
        prepararControlesDoPlayerDaPagina()
        progresso = findViewById(R.id.progresso_do_carregamento_da_pagina)
        painelGerenciador = findViewById(R.id.painel_do_gerenciador_de_abas)
        botaoNovaAbaNaGestao = findViewById(R.id.botao_nova_aba_na_gestao)
        botaoNovaJanelaNaGestao = findViewById(R.id.botao_nova_janela_na_gestao)
        botaoFecharGestao = findViewById(R.id.botao_fechar_gestao_de_abas)
        gradeDeAbas = findViewById(R.id.grade_de_abas_do_gerenciador)
        botaoVoltar.setOnClickListener { voltarNaPagina() }
        botaoAvancar.setOnClickListener { avancarNaPagina() }
        botaoRecarregar.setOnClickListener { recarregarPagina() }
        botaoInicio.setOnClickListener { irParaPaginaInicial() }
        botaoGerenciador.setOnClickListener { alternarGerenciadorDeAbas() }
        botaoNovaAba.setOnClickListener { abrirNovaAba() }
        botaoNovaAbaNaGestao.setOnClickListener { fecharGerenciadorDeAbas { abrirNovaAba() } }
        botaoNovaJanelaNaGestao.setOnClickListener { fecharGerenciadorDeAbas { abrirNovaJanela() } }
        botaoFecharGestao.setOnClickListener { fecharGerenciadorDeAbas() }
        botaoFixar.setOnClickListener { alternarAtalhoDaPaginaAtual() }
        botaoComputador.setOnClickListener { alternarVersaoParaComputador() }
        aplicarAparencia()
        botaoIrAoEnderecoDaBarra.setOnClickListener { abrirEnderecoDigitado() }
        botaoCopiarEnderecoDaBarra.setOnClickListener { copiarEnderecoDaBarra() }
        campoEndereco.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_NULL) {
                abrirEnderecoDigitado()
                true
            } else {
                false
            }
        }
        campoEndereco.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Inserção que termina no fim do texto = pessoa digitando (apagar não autocompleta).
                digitouNoFimDaBarra = count > 0 && start + count == (s?.length ?: 0)
            }
            override fun afterTextChanged(s: Editable?) {
                if (aplicandoAutocompletar) return
                autocompletarAtual = null
                if (!campoEndereco.hasFocus() || s == null) return
                val digitado = s.toString()
                if (digitouNoFimDaBarra && campoEndereco.selectionEnd == digitado.length) {
                    aplicarAutocompletarNaBarra(s, digitado)
                }
                mostrarSugestoesDaBarra(s.toString())
            }
        })
        campoEndereco.setOnFocusChangeListener { _, temFoco ->
            if (temFoco) {
                campoEndereco.post { if (campoEndereco.hasFocus()) campoEndereco.selectAll() }
                mostrarSugestoesDaBarra(campoEndereco.text.toString())
            } else if (!escolhendoSugestao) {
                esconderSugestoesDaBarra()
            }
        }
        botaoFlutuante.setOnClickListener { abrirPlayerNativo() }
        gravacaoDaBibliotecaDeVideos = GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo(this)
        bibliotecaDeVideos = gravacaoDaBibliotecaDeVideos.carregar()
        gravacaoDaBibliotecaDeImagens = GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo(this)
        bibliotecaDeImagens = gravacaoDaBibliotecaDeImagens.importarUltimoQuadroCopiadoSeAindaExistir(
            gravacaoDaBibliotecaDeImagens.carregar(),
        )
        gravacaoDaBibliotecaDeComponentes = GravacaoDaBibliotecaDeComponentesHtmlDaColecaoNoArmazenamentoDoAplicativo(this)
        bibliotecaDeComponentesDaColecao = gravacaoDaBibliotecaDeComponentes.carregar()
        semearComponenteDeGeracaoDeImagemViaMcpSeAusente()
        semearComponenteDeGeracaoDeVideoViaMcpSeAusente()
        semearComponenteDoVisualizadorDeMidiaSeAusente()
        pregerarMiniaturasDoVisualizadorDeMidiaEmSegundoPlano()
        // Listas de modelos dos MCPs: só vai à rede se a última busca passou de 6 h.
        servicoDeGeracaoDeImagemViaMcpDaColecao.preaquecerCatalogosEmSegundoPlano()
        registrarObservadorDeMidiaNovaNoAparelho()
        painelDaColecao.ligarGuardarNaBiblioteca {
            painelDaColecao.guardarNaBiblioteca(
                bibliotecaDeComponentesDaColecao,
                gravacaoDaBibliotecaDeComponentes,
                System.currentTimeMillis(),
            ) { UUID.randomUUID().toString() }
        }
        painelDaColecao.ligarPersistenciaDeDimensoes { id, larguraPx, alturaPx ->
            if (!bibliotecaDeComponentesDaColecao.atualizarDimensoes(
                    id = id,
                    larguraPx = larguraPx,
                    alturaPx = alturaPx,
                    agora = System.currentTimeMillis(),
                )
            ) {
                return@ligarPersistenciaDeDimensoes
            }
            gravacaoDaBibliotecaDeComponentes.gravar(bibliotecaDeComponentesDaColecao)
        }
        agendarWarmStartDosPaineisDaColecao(imediato = false)
        painelDaBibliotecaDeVideos = PainelDaGavetaDaBibliotecaDeVideos(
            activity = this,
            biblioteca = bibliotecaDeVideos,
            gravacao = gravacaoDaBibliotecaDeVideos,
            bibliotecaDeImagens = bibliotecaDeImagens,
            gravacaoDeImagens = gravacaoDaBibliotecaDeImagens,
            aoAbrir = { item -> abrirItemDaBiblioteca(item) },
            aoFavoritoAlterado = { item ->
                if (item?.chave == chaveDoVideoNoPlayerDaPagina) atualizarEstrelaDoPlayerDaPagina()
            },
            acompanhamento = AcompanhamentoGlobalDosDownloadsDeMidiaNaGaveta.atual,
            aoAbrirDownload = { item -> abrirArquivoBaixado(item.uri, item.titulo) },
            bibliotecaDePaginas = bibliotecaDeComponentesDaColecao,
            gravacaoDePaginas = gravacaoDaBibliotecaDeComponentes,
            aoAbrirPagina = { item ->
                painelDaBibliotecaDeVideos.esconder()
                abrirComponenteDaBibliotecaRespeitandoAAberturaPadrao(item)
            },
            idDaPaginaVinculadaNaColecao = { painelDaColecao.idOrigemDaBibliotecaAtual() },
            aoLimparVinculoDaPaginaNaColecao = { painelDaColecao.limparVinculoBiblioteca() },
        )
        painelDaBibliotecaDeVideos.aoIniciarArrasteDaBiblioteca = { estado, bitmap ->
            iniciarArrasteVisualDaBiblioteca(estado, bitmap)
        }
        painelDaBibliotecaDeVideos.aoAbrirComponentesDoEditor = {
            abrirComponentesPeloChromeDoEditorDeImagem()
        }
        painelDaBibliotecaDeVideos.aoUsarDeNovoNoGerador = { pedido -> usarDeNovoNoGerador(pedido) }
        painelDoChat.aoAbrirArmazenamento = { abrirArmazenamentoDoAplicativo() }
        painelDoChat.aoUsarPincelDoEditor = { id -> painelDaBibliotecaDeVideos.usarPincelDoMotorNoEditor(id) }
        painelDaBibliotecaDeVideos.aoAbrirArmazenamento = { abrirArmazenamentoDoAplicativo() }
        painelDaBibliotecaDeVideos.aoAbrirVisualizadorDeMidia = {
            val item = if (::bibliotecaDeComponentesDaColecao.isInitialized && ::painelDaColecao.isInitialized) {
                // Mesma busca da semente: o visualizador pode ter sido guardado antes com outro id (achado pelo nome).
                bibliotecaDeComponentesDaColecao.buscar(com.chromebrothers.navegador.regras.HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.ID)
                    ?: bibliotecaDeComponentesDaColecao.listar().firstOrNull {
                        com.chromebrothers.navegador.regras.PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
                            .ehVisualizadorDeMidiaPeloNome(it.nome)
                    }
            } else {
                null
            }
            // Com o editor aberto, a abertura padrão força flutuar sobre a imagem.
            if (item != null) abrirComponenteDaBibliotecaRespeitandoAAberturaPadrao(item)
            item != null
        }
        painelDaBibliotecaDeVideos.aoPedirArquivoDePinceis = { receber ->
            aoReceberArquivoDePinceis = receber
            // .chromebrush não tem tipo próprio no Android: qualquer arquivo; a importação confere o conteúdo.
            runCatching { seletorDoArquivoDePinceis.launch(arrayOf("*/*")) }.onFailure {
                aoReceberArquivoDePinceis = null
                receber(null)
            }
        }
        painelDaBibliotecaDeVideos.aoPedirImagemDaFormaDoPincel = { receber ->
            aoReceberImagemDaFormaDoPincel = receber
            runCatching { seletorDeImagemDaFormaDoPincel.launch("image/*") }.onFailure {
                aoReceberImagemDaFormaDoPincel = null
                receber(null)
            }
        }
        // Não encerra a coleção ao sair do editor: o componente de geração (e outros) devem
        // continuar abertos após colar/arrastar imagens para a composição.
        painelDaBibliotecaDeVideos.aoEncerrarEdicaoDeImagem = null
        painelDaBibliotecaDeVideos.ligarExtracaoDeTexto(
            temChave = { cofreDaChaveDaOpenRouter.temChave() },
            extrair = { bytes -> orquestradorDaExtracaoDeTextoDeImagem.extrairDeBytes(bytes) },
        )
        findViewById<ImageButton>(R.id.botao_favoritar_o_video_da_biblioteca).setOnClickListener {
            alternarFavoritoDoVideoNoPlayerDaPagina()
            mostrarControlesDoPlayerPorUmInstante()
        }
        findViewById<ImageButton>(R.id.botao_abrir_gaveta_da_biblioteca_de_videos).setOnClickListener {
            painelDaBibliotecaDeVideos.mostrar()
        }
        botaoBibliotecaNaBarra.setOnClickListener {
            painelDaBibliotecaDeVideos.mostrar()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (siteEmTelaCheia) {
                    paginas[gestao.idDaAbaAtiva]?.webView?.sairDaTelaCheia()
                    sairDaTelaCheiaDoSite()
                    return
                }
                // Os visores moram na aba Visor: em outra aba, o Voltar é da página dessa aba.
                val naAbaDoVisor = idDaAbaDoVisor == null || gestao.idDaAbaAtiva == idDaAbaDoVisor
                val videoFlutuante = visorDeVideosAoLadoCriado?.estaFlutuante() == true
                if ((naAbaDoVisor || videoFlutuante) && visorDeVideosAoLadoCriado?.tratarVolta() == true) return
                if (naAbaDoVisor && visorExternoDaGavetaCriado?.fechar() == true) return
                voltarNaPagina()
            }
        })
        painelDaBibliotecaDeVideos.aoMudarEdicaoDeImagem = { aplicarModoImersivo() }
        aplicarModoImersivo()
        findViewById<ImageButton>(R.id.botao_fechar_janela_de_login).setOnClickListener {
            fecharJanelaDeLogin()
        }
        HistoricoDoNavegadorChromebrothers.iniciar(this)
        gravacaoDasAbas = GravacaoDasAbasAbertasDoNavegador(this, slotDaClasseAtual())
        val salvas = if (preferencias.restaurarAbasAoAbrir) gravacaoDasAbas.ler() else null
        val restaurou = salvas != null && salvas.abas.isNotEmpty()
        val aba = if (restaurou) {
            restaurarAbasSalvas(salvas!!)
        } else {
            gestao.iniciar().also { nova ->
                criarPagina(nova.id, nova.endereco)
                mostrarAba(nova.id)
            }
        }
        val enderecoInicialDaJanela = intent
            ?.getStringExtra(LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.EXTRA_ENDERECO_INICIAL)
            ?.trim()
            .orEmpty()
        if (enderecoInicialDaJanela.isNotBlank()) {
            if (restaurou) abrirLinkEmNovaAba(enderecoInicialDaJanela)
            else navegarNaAba(aba.id, enderecoInicialDaJanela)
        }
        aplicarDisposicaoDaBarraDoNavegador()
        // Um respiro para a aba ativa começar primeiro; a pré-carregada vai em outro processo.
        manipuladorDasAbasSalvas.postDelayed({ preCarregarSitePesado() }, 1_500L)
    }

    override fun onResume() {
        super.onResume()
        sincronizarBibliotecaDeVideos()
        agendarAtualizacaoDoVisualizadorPorMidiaNova()
        if (botoesDeIncrementoDoPlayerDaPagina.isNotEmpty()) {
            incrementoDoPlayerDaPagina = preferencias.incrementoDeDeslocamentoDoVideo
            atualizarIncrementoSelecionado()
        }
        if (::interruptorDeCapturaDeMidia.isInitialized &&
            interruptorDeCapturaDeMidia.isChecked != preferencias.capturarMidiaParaDownload
        ) {
            publicarCapturaDeMidia(preferencias.capturarMidiaParaDownload)
        }
        reativarSessaoDaAbaVisivelAposRetorno()
        aplicarModoImersivo()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            reativarSessaoDaAbaVisivelAposRetorno()
            aplicarModoImersivo()
        }
    }

    /**
     * Esconde (ou devolve) barra de status, softkeys e barra de tarefas da Samsung. Escondidas, voltam
     * por alguns segundos ao deslizar da borda e somem de novo sozinhas.
     */
    private fun aplicarModoImersivo() {
        val esconder = com.chromebrothers.navegador.regras.ModoImersivoDaJanelaDoNavegador.deveEsconderAsBarras(
            ligadoNoMenu = preferencias.modoImersivo,
            editorDeImagemAberto = editorDeImagemAberto(),
            siteEmTelaCheia = siteEmTelaCheia,
            playerDaPaginaAberto = playerDaPaginaAberto(),
        )
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (esconder) hide(WindowInsetsCompat.Type.systemBars()) else show(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun playerDaPaginaAberto(): Boolean =
        ::painelDoPlayerDaPagina.isInitialized && painelDoPlayerDaPagina.visibility == View.VISIBLE

    /** Margens das barras do sistema no instante em que o player sobreposto abriu (null = player fechado). */
    private var barrasCongeladasPeloPlayerDaPagina: androidx.core.graphics.Insets? = null

    /** Congela (ao abrir) ou solta (ao fechar) as margens da raiz e reaplica o modo imersivo. */
    private fun congelarMargensParaOPlayerDaPagina(congelar: Boolean) {
        val raiz = findViewById<View>(R.id.raiz_do_navegador)
        barrasCongeladasPeloPlayerDaPagina = if (congelar) {
            ViewCompat.getRootWindowInsets(raiz)?.getInsets(WindowInsetsCompat.Type.systemBars())
                ?: androidx.core.graphics.Insets.of(raiz.paddingLeft, raiz.paddingTop, raiz.paddingRight, raiz.paddingBottom)
        } else {
            null
        }
        aplicarModoImersivo()
        ViewCompat.requestApplyInsets(raiz)
    }

    /** O site pediu tela cheia: só a página fica (sem abas, barra, busca, extensões nem chat). */
    private fun entrarNaTelaCheiaDoSite() {
        if (siteEmTelaCheia) return
        siteEmTelaCheia = true
        vistasEscondidasPelaTelaCheiaDoSite.clear()
        listOfNotNull(
            faixaSuperior,
            barraDeNavegacao,
            faixaDeBotoesDeExtensao,
            progresso,
            faixaDeBuscaNaPagina,
            findViewById<View>(R.id.gaveta_do_chat_da_openrouter),
        ).forEach { vista ->
            vistasEscondidasPelaTelaCheiaDoSite += vista to vista.visibility
            vista.visibility = View.GONE
        }
        aplicarModoImersivo()
    }

    private fun sairDaTelaCheiaDoSite() {
        if (!siteEmTelaCheia) return
        siteEmTelaCheia = false
        vistasEscondidasPelaTelaCheiaDoSite.forEach { (vista, visibilidade) -> vista.visibility = visibilidade }
        vistasEscondidasPelaTelaCheiaDoSite.clear()
        aplicarDisposicaoDaBarraDoNavegador()
        aplicarModoImersivo()
    }

    override fun onPause() {
        visorDeVideosAoLadoCriado?.pausar()
        if (::painelDaBibliotecaDeVideos.isInitialized) {
            painelDaBibliotecaDeVideos.guardarRascunhoSeEstiverEditando()
        }
        super.onPause()
    }

    override fun onStop() {
        // Sem isto a aba visível segue ativa minimizada (JS/vídeo a todo vapor) e o Android
        // mata o processo dela por "excessive cpu", deixando a página preta na volta.
        if (::areaDasPaginas.isInitialized) paginas[gestao.idDaAbaAtiva]?.webView?.onPause()
        gravarAbasAgora()
        super.onStop()
    }

    // ---- Abas salvas (reabrir ao iniciar) ----

    private fun restaurarAbasSalvas(
        janela: JanelaSalvaDoNavegador,
    ): com.chromebrothers.navegador.regras.AbaDoNavegadorChromebrothers {
        val abas = gestao.restaurar(
            janela.abas.map { it.titulo to it.endereco },
            janela.indiceDaAtiva,
        )
        // restaurar() descarta abas sem endereço; as salvas já vêm filtradas, então a ordem bate.
        abas.forEachIndexed { indice, aba ->
            val salva = janela.abas.getOrNull(indice) ?: return@forEachIndexed
            if (aba.id == gestao.idDaAbaAtiva) {
                criarPaginaRestaurada(aba.id, salva)
            } else {
                abasAindaNaoCarregadas[aba.id] = salva
            }
        }
        val ativa = gestao.ativa() ?: abas.first()
        mostrarAba(ativa.id)
        android.util.Log.i("ChromeBrothersAbas", "reabertas ${abas.size} abas")
        return ativa
    }

    private fun alternarManterHiggsfieldPronto() {
        val ligar = !preferencias.manterHiggsfieldPronto
        preferencias.manterHiggsfieldPronto = ligar
        if (ligar) {
            manipuladorDasAbasSalvas.post { preCarregarSitePesado() }
        } else {
            // Desligado: solta tudo agora (memória e processamento), as abas seguem como estão.
            manipuladorDasAbasSalvas.removeCallbacks(prepararReserva)
            descartarReserva()
            encerrarPreCarregamento()
        }
    }

    private fun preCarregarSitePesado() {
        if (isFinishing || isDestroyed || !preferencias.manterHiggsfieldPronto) return
        val regra = com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura
        val naoCarregadas = gestao.listar()
            .filter { abasAindaNaoCarregadas.containsKey(it.id) }
            .map { it.id to (abasAindaNaoCarregadas[it.id]?.endereco ?: it.endereco) }
        val id = regra.abaParaPreCarregar(naoCarregadas, gestao.idDaAbaAtiva)
        if (id != null && idDaAbaPreCarregada == null) preCarregarAbaReaberta(id)
        // Reserva para abrir o site em qualquer aba. Com o processador já ocupado com o site (a aba
        // ativa é dele ou uma aba dele está carregando escondida), espera um pouco para não disputar.
        val siteJaCarregando = id != null || regra.ehSitePesado(gestao.ativa()?.endereco)
        agendarReserva(if (siteJaCarregando) regra.ATRASO_DA_RESERVA_COM_O_SITE_CARREGANDO_MS else 0L)
    }

    /** Aba do site reaberta na partida: carrega escondida para estar pronta quando for aberta. */
    private fun preCarregarAbaReaberta(id: Long) {
        garantirPaginaDaAba(id)
        val pagina = paginas[id] ?: return
        if (gestao.idDaAbaAtiva != id) {
            // Ativa e desenhando, só que fora da tela: escondida (GONE) o Gecko carrega mas nunca pinta,
            // e a 1ª pintura dessa página enorme ficava para a hora em que a pessoa chega (3–5 s em branco).
            pagina.webView.translationX = maxOf(areaDasPaginas.width, resources.displayMetrics.widthPixels).toFloat() * 2f
            pagina.webView.visibility = View.VISIBLE
            pagina.webView.onResume()
        }
        pagina.webView.definirPrioridadeAlta(true)
        idDaAbaPreCarregada = id
        abaPreCarregadaJaMostrada = false
        manipuladorDasAbasSalvas.postDelayed(
            baixarPrioridadeDaAbaPreCarregada,
            com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura.DURACAO_DA_PRIORIDADE_ALTA_MS,
        )
        android.util.Log.i("ChromeBrothersAbas", "pré-carregando aba $id escondida")
    }

    private val prepararReserva = Runnable {
        if (isFinishing || isDestroyed || vistaPreCarregadaAvulsa != null || !preferencias.manterHiggsfieldPronto) return@Runnable
        // Preparar a reserva com a pessoa usando o site disputa CPU/GPU com a página visível
        // (ela engasga e fica preta ao rolar): espera a pessoa sair do site.
        if (abaAtivaEstaNoSitePesado()) {
            agendarReserva(com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura.ATRASO_ENQUANTO_NO_SITE_MS)
            return@Runnable
        }
        preCarregarPaginaAvulsa(com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura.ENDERECO_PADRAO)
    }

    private fun abaAtivaEstaNoSitePesado(): Boolean =
        com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura.ehSitePesado(
            paginas[gestao.idDaAbaAtiva]?.webView?.url ?: gestao.ativa()?.endereco,
        )

    /** Reserva ainda carregando: pausa enquanto a aba ativa é do site e continua quando a pessoa sai. */
    private fun atualizarReservaConformeAbaAtiva() {
        val reserva = vistaPreCarregadaAvulsa ?: return
        if (!reservaEmPreparo) return
        manipuladorDasAbasSalvas.removeCallbacks(adormecerPaginaAvulsa)
        if (abaAtivaEstaNoSitePesado()) {
            reserva.definirPrioridadeAlta(false)
            reserva.onPause()
        } else {
            reserva.onResume()
            reserva.definirPrioridadeAlta(true)
            manipuladorDasAbasSalvas.postDelayed(adormecerPaginaAvulsa, com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura.DURACAO_DA_RESERVA_MS)
        }
    }

    private var reservaAquecida = false

    /** Rola a reserva até o fim e volta: o site monta e desenha cada seção antes de a pessoa chegar. */
    private fun aquecerReserva(vista: SuperficieDaPaginaNoMotorDoFirefox) {
        if (vistaPreCarregadaAvulsa !== vista || !reservaEmPreparo || reservaAquecida) return
        if (abaAtivaEstaNoSitePesado()) {
            // Pausada enquanto a pessoa usa o site: tenta de novo depois.
            vista.postDelayed({ aquecerReserva(vista) }, com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura.ATRASO_ENQUANTO_NO_SITE_MS)
            return
        }
        reservaAquecida = true
        vista.evaluateJavascript(com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura.SCRIPT_DE_AQUECIMENTO, null)
        android.util.Log.i("ChromeBrothersAbas", "aquecendo a reserva (rolando a página inteira)")
    }

    private fun agendarReserva(atrasoMs: Long) {
        manipuladorDasAbasSalvas.removeCallbacks(prepararReserva)
        manipuladorDasAbasSalvas.postDelayed(prepararReserva, atrasoMs)
    }

    private fun encerrarPreCarregamento() {
        val id = idDaAbaPreCarregada ?: return
        idDaAbaPreCarregada = null
        manipuladorDasAbasSalvas.removeCallbacks(baixarPrioridadeDaAbaPreCarregada)
        val vista = paginas[id]?.webView ?: return
        vista.definirPrioridadeAlta(false)
        if (gestao.idDaAbaAtiva != id) {
            // Ninguém abriu: vira uma aba escondida comum.
            vista.visibility = View.GONE
            vista.onPause()
            vista.translationX = 0f
        }
    }

    /**
     * Reserva: página avulsa do site (fora da barra de abas), fora da tela e com prioridade alta.
     * Quando a pessoa abrir o site em qualquer aba, a página pronta entra nela e outra é preparada.
     */
    private var vistaPreCarregadaAvulsa: SuperficieDaPaginaNoMotorDoFirefox? = null
    private var modoDaPaginaAvulsa: ModoDeApresentacaoDoSite? = null
    private var reservaEmPreparo = false
    private val adormecerPaginaAvulsa = Runnable {
        reservaEmPreparo = false
        vistaPreCarregadaAvulsa?.let { vista ->
            // Prazo acabou sem uso: segue guardada, mas parada e sem prioridade.
            vista.definirPrioridadeAlta(false)
            vista.visibility = View.GONE
            vista.onPause()
        }
    }

    private fun preCarregarPaginaAvulsa(endereco: String) {
        val vista = SuperficieDaPaginaNoMotorDoFirefox(this)
        vista.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        vista.setBackgroundColor(paletaAtual().fundoDaPagina)
        vista.definirProtecaoContraRastreamento(preferencias.bloquearAnuncios)
        // Mesmo modo (computador/celular) que uma aba nova teria; senão o site monta o layout errado.
        val modo = preferencias.modoDeSite
        vista.aplicarModo(modo)
        vista.settings.loadWithOverviewMode = AgenteDeUsuarioDoNavegadorChromebrothers.encolherParaCaberNaTela(modo)
        modoDaPaginaAvulsa = modo
        vista.translationX = maxOf(areaDasPaginas.width, resources.displayMetrics.widthPixels).toFloat() * 2f
        areaDasPaginas.addView(vista, 0)
        vista.onResume()
        vista.definirPrioridadeAlta(true)
        // Terminou de carregar: daqui a pouco aquece (rola a página inteira fora da tela).
        vista.aoTerminarPagina = { _, sucesso ->
            if (sucesso) vista.postDelayed({ aquecerReserva(vista) }, com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura.ATRASO_PARA_AQUECER_MS)
        }
        vista.loadUrl(endereco)
        vistaPreCarregadaAvulsa = vista
        reservaEmPreparo = true
        reservaAquecida = false
        manipuladorDasAbasSalvas.postDelayed(adormecerPaginaAvulsa, com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura.DURACAO_DA_RESERVA_MS)
        android.util.Log.i("ChromeBrothersAbas", "pré-carregando $endereco em página avulsa")
    }

    /**
     * Página do site pesado que sai de uma aba (aba fechada, ou a aba foi para outro site): é
     * "devolvida" e vira a reserva, no lugar de uma reserva em preparo. Reabrir o site em seguida é
     * instantâneo, sem carregar nada em segundo plano. Fora da página inicial, ela clica no "Home"
     * do próprio site (o caminho normal dele) e só fica se chegar lá.
     */
    private fun guardarComoReservaSePossivel(pagina: PaginaDeUmaAbaDoNavegadorChromebrothers): Boolean {
        val regra = com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura
        if (isFinishing || isDestroyed || !preferencias.manterHiggsfieldPronto) return false
        if (!regra.ehSitePesado(pagina.webView.url)) return false
        manipuladorDasAbasSalvas.removeCallbacks(prepararReserva)
        manipuladorDasAbasSalvas.removeCallbacks(adormecerPaginaAvulsa)
        vistaPreCarregadaAvulsa?.let { emPreparo ->
            areaDasPaginas.removeView(emPreparo)
            emPreparo.stopLoading()
            emPreparo.destroy()
        }
        val vista = pagina.webView
        vista.translationX = maxOf(areaDasPaginas.width, resources.displayMetrics.widthPixels).toFloat() * 2f
        // Já carregada: fica parada fora da tela (guarda o último quadro), sem disputar CPU/GPU.
        vista.visibility = View.VISIBLE
        vista.definirPrioridadeAlta(false)
        vista.onPause()
        vistaPreCarregadaAvulsa = vista
        reservaEmPreparo = false
        modoDaPaginaAvulsa = pagina.modoForcado ?: preferencias.modoDeSite
        if (!regra.estaNaPaginaInicial(vista.url)) {
            vista.evaluateJavascript(regra.SCRIPT_DE_VOLTAR_A_PAGINA_INICIAL, null)
            vista.postDelayed({
                if (vistaPreCarregadaAvulsa === vista && !regra.estaNaPaginaInicial(vista.url)) {
                    android.util.Log.i("ChromeBrothersAbas", "reserva não voltou à página inicial (${vista.url}): descartada")
                    descartarReserva()
                }
            }, regra.PRAZO_PARA_VOLTAR_A_PAGINA_INICIAL_MS)
        }
        android.util.Log.i("ChromeBrothersAbas", "página da aba ${pagina.id} devolvida como reserva (${vista.url})")
        return true
    }

    private fun descartarReserva() {
        val reserva = vistaPreCarregadaAvulsa ?: return
        vistaPreCarregadaAvulsa = null
        reservaEmPreparo = false
        manipuladorDasAbasSalvas.removeCallbacks(adormecerPaginaAvulsa)
        areaDasPaginas.removeView(reserva)
        reserva.stopLoading()
        reserva.destroy()
    }

    /**
     * Aba do site pesado indo para outro site: a página do site é devolvida como reserva e a aba
     * recebe uma página nova para o destino (o "voltar" da aba não volta ao site; reabri-lo é instantâneo).
     */
    private fun devolverPaginaDoSiteAoSairSePossivel(id: Long, destino: String): Boolean {
        val regra = com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura
        if (!preferencias.manterHiggsfieldPronto) return false
        val antiga = paginas[id] ?: return false
        if (!regra.ehSitePesado(antiga.webView.url) || regra.ehSitePesado(destino)) return false
        paginas.remove(id)
        inicioDaCapturaPorAba.remove(id)?.remove()
        inicioDoCampoDeTextoPorAba.remove(id)?.remove()
        encerrarVideo(id)
        antiga.liberarMiniatura()
        if (!guardarComoReservaSePossivel(antiga)) {
            areaDasPaginas.removeView(antiga.webView)
            antiga.webView.stopLoading()
            antiga.webView.destroy()
        }
        criarPagina(id, "")
        paginas[id]?.let { aplicarModoDeSite(it) }
        mostrarAba(id)
        return true
    }

    /** Abrir o site com a página avulsa pronta: ela substitui a página da aba, sem carregar de novo. */
    private fun adotarPaginaAvulsaSePossivel(id: Long, endereco: String): Boolean {
        val vista = vistaPreCarregadaAvulsa ?: return false
        if (!com.chromebrothers.navegador.regras.PreCarregamentoDeSitePesadoNaAbertura
                .deveUsarReserva(endereco, vista.url)
        ) return false
        val antiga = paginas[id] ?: return false
        // Aba com modo forçado diferente (ex.: celular): a página pronta não serve, carrega normal.
        if ((antiga.modoForcado ?: preferencias.modoDeSite) != modoDaPaginaAvulsa) return false
        vistaPreCarregadaAvulsa = null
        reservaEmPreparo = false
        manipuladorDasAbasSalvas.removeCallbacks(adormecerPaginaAvulsa)
        val nova = PaginaDeUmaAbaDoNavegadorChromebrothers(id, vista)
        nova.agenteOriginal = vista.settings.userAgentString
        paginas[id] = nova
        prepararNavegador(vista, id)
        aplicarModoDeSite(nova)
        // A página de antes sai da aba (o "voltar" da aba começa no site novo).
        encerrarVideo(id)
        antiga.liberarMiniatura()
        areaDasPaginas.removeView(antiga.webView)
        antiga.webView.stopLoading()
        antiga.webView.destroy()
        vista.translationX = 0f
        vista.visibility = View.VISIBLE
        mostrarAba(id)
        // O que o app faz ao fim de cada carregamento (scripts de vídeo/arraste, histórico, barra).
        vista.aoTerminarPagina?.invoke(vista.url.orEmpty(), true)
        // O título chegou enquanto ela era reserva, sem aba para recebê-lo.
        vista.title?.takeIf { it.isNotBlank() }?.let { titulo ->
            gestao.atualizar(id, titulo = titulo)
            desenharAbas()
        }
        manipuladorDasAbasSalvas.postDelayed({ vista.definirPrioridadeAlta(false) }, 10_000L)
        android.util.Log.i("ChromeBrothersAbas", "página avulsa pré-carregada adotada pela aba $id")
        return true
    }

    private fun criarPaginaRestaurada(id: Long, salva: AbaSalvaDoNavegador) {
        criarPagina(id, "")
        val pagina = paginas[id] ?: return
        aplicarModoDeSite(pagina)
        val estado = salva.estadoDaSessao
        when {
            EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(salva.endereco) -> carregarPaginaInicial(pagina)
            estado != null && pagina.webView.restaurarEstadoDaSessao(estado, salva.endereco) -> Unit
            else -> pagina.webView.loadUrl(salva.endereco)
        }
    }

    /** Aba reaberta que ainda não tem página: cria agora (ao ser mostrada ou usada). */
    private fun garantirPaginaDaAba(id: Long) {
        if (paginas.containsKey(id)) return
        val salva = abasAindaNaoCarregadas.remove(id) ?: return
        criarPaginaRestaurada(id, salva)
    }

    private fun agendarGravacaoDasAbas() {
        if (!::gravacaoDasAbas.isInitialized || !preferencias.restaurarAbasAoAbrir) return
        manipuladorDasAbasSalvas.removeCallbacks(gravarAbasDepois)
        manipuladorDasAbasSalvas.postDelayed(gravarAbasDepois, 1_500L)
    }

    private fun gravarAbasAgora() {
        manipuladorDasAbasSalvas.removeCallbacks(gravarAbasDepois)
        if (!::gravacaoDasAbas.isInitialized || !preferencias.restaurarAbasAoAbrir) return
        // A aba Visor não é gravada: só faz sentido com uma mídia aberta.
        val (idsParaGravar, ativa) = com.chromebrothers.navegador.regras.AbaDoVisorDeMidiaDoNavegador.paraGravar(
            ids = gestao.listar().map { it.id },
            idAtiva = gestao.idDaAbaAtiva,
            idDoVisor = idDaAbaDoVisor,
            idDeOndeVeio = idDaAbaDeOndeVeioOVisor,
        )
        val lista = gestao.listar().filter { it.id in idsParaGravar }
        if (lista.isEmpty()) return
        val abas = lista.map { aba ->
            val vista = paginas[aba.id]?.webView
            val pendente = abasAindaNaoCarregadas[aba.id]
            AbaSalvaDoNavegador(
                titulo = aba.titulo,
                endereco = vista?.url?.takeIf { it.isNotBlank() } ?: aba.endereco,
                estadoDaSessao = vista?.estadoDaSessaoComoTexto() ?: pendente?.estadoDaSessao,
            )
        }
        gravacaoDasAbas.gravar(JanelaSalvaDoNavegador(abas, ativa))
    }

    private fun alternarRestaurarAbasAoAbrir() {
        val ligar = !preferencias.restaurarAbasAoAbrir
        preferencias.restaurarAbasAoAbrir = ligar
        if (ligar) gravarAbasAgora() else gravacaoDasAbas.apagar()
    }

    /**
     * Ao voltar do segundo plano o Gecko pode ficar com setActive(false) / capa presa
     * (home cinza sólida, páginas sem clique). A troca de aba já reativa; o Activity não.
     */
    private fun reativarSessaoDaAbaVisivelAposRetorno() {
        if (!::areaDasPaginas.isInitialized) return
        val pagina = paginas[gestao.idDaAbaAtiva] ?: return
        pagina.webView.onResume()
        MotorDoFirefoxDoNavegadorChromebrothers.marcarAbaAtiva(pagina.webView.sessao)
        pagina.webView.descobrirCapaDaPrimeiraPintura()
        val teclado = ViewCompat.getRootWindowInsets(window.decorView)
            ?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
        if (teclado == 0) {
            areaDasPaginas.translationY = 0f
            pagina.webView.definirClippingVerticalInferior(0)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        findViewById<View>(R.id.raiz_do_navegador)?.let { ViewCompat.requestApplyInsets(it) }
        if (::barraDeNavegacao.isInitialized) aplicarDisposicaoDaBarraDoNavegador()
    }

    private fun modoDaBarraDoNavegadorAtual(): ModoDaBarraDoNavegadorPorLargura {
        val densidade = resources.displayMetrics.density.coerceAtLeast(0.01f)
        val larguraEmDp = (resources.displayMetrics.widthPixels / densidade).toInt()
        return DisposicaoDaBarraDoNavegadorPorLarguraDaTela.modo(larguraEmDp)
    }

    private fun aplicarDisposicaoDaBarraDoNavegador() {
        if (!::barraDeNavegacao.isInitialized) return
        if (siteEmTelaCheia || (::painelDoPlayerDaPagina.isInitialized &&
            painelDoPlayerDaPagina.visibility == View.VISIBLE)
        ) {
            faixaSuperior.visibility = View.GONE
            barraDeNavegacao.visibility = View.GONE
            return
        }
        val modo = modoDaBarraDoNavegadorAtual()
        fun visibilidade(controle: ControleDaBarraDoNavegador): Int {
            return if (DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(controle, modo)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        barraDeNavegacao.visibility = View.VISIBLE
        faixaSuperior.visibility = View.VISIBLE
        botaoVoltar.visibility = visibilidade(ControleDaBarraDoNavegador.VOLTAR)
        botaoAvancar.visibility = visibilidade(ControleDaBarraDoNavegador.AVANCAR)
        botaoRecarregar.visibility = visibilidade(ControleDaBarraDoNavegador.RECARREGAR)
        botaoInicio.visibility = visibilidade(ControleDaBarraDoNavegador.INICIO)
        caixaDaBarraDeEndereco.visibility = visibilidade(ControleDaBarraDoNavegador.ENDERECO)
        botaoComputador.visibility = visibilidade(ControleDaBarraDoNavegador.VERSAO_COMPUTADOR)
        botaoCampoDeTexto.visibility = visibilidade(ControleDaBarraDoNavegador.CAMPO_TEXTO)
        interruptorDeBloqueioDeAnuncio.visibility =
            visibilidade(ControleDaBarraDoNavegador.BLOQUEIO_ANUNCIO)
        botaoCapturaDeTela.visibility = visibilidade(ControleDaBarraDoNavegador.CAPTURA_TELA)
        botaoBuscarNaPagina.visibility = visibilidade(ControleDaBarraDoNavegador.BUSCAR_NA_PAGINA)
        interruptorDeCapturaDeMidia.visibility =
            visibilidade(ControleDaBarraDoNavegador.CAPTURA_MIDIA)
        botaoExtensoes.visibility = visibilidade(ControleDaBarraDoNavegador.EXTENSOES)
        botaoBibliotecaNaBarra.visibility = visibilidade(ControleDaBarraDoNavegador.BIBLIOTECA)
        botaoChatNaBarra.visibility = visibilidade(ControleDaBarraDoNavegador.CHAT)
        botaoMenuOverflow.visibility = visibilidade(ControleDaBarraDoNavegador.MENU_OVERFLOW)
        val faixaDeAbasVisivel = DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
            ControleDaBarraDoNavegador.FAIXA_DE_ABAS,
            modo,
        )
        rolagemDasAbas.visibility = if (faixaDeAbasVisivel) View.VISIBLE else View.GONE
        botaoNovaAba.visibility = if (
            DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
                ControleDaBarraDoNavegador.NOVA_ABA_NA_FAIXA,
                modo,
            )
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }
        botaoGerenciador.visibility = View.VISIBLE
        val vazio = espacoAntesDoContador.layoutParams as LinearLayout.LayoutParams
        if (modo == ModoDaBarraDoNavegadorPorLargura.COMPACTA) {
            vazio.width = 0
            vazio.height = 0
            vazio.weight = 1f
            espacoAntesDoContador.layoutParams = vazio
        } else {
            reservarEspacoDosBotoesDaFaixa()
        }
        atualizarBotoesDaAba(paginas[gestao.idDaAbaAtiva])
    }

    private fun paginaAtualPermiteFixarAtalho(): Boolean {
        val pagina = paginas[gestao.idDaAbaAtiva] ?: return false
        val url = pagina.webView.url.orEmpty()
        return url.isNotBlank() && !EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)
    }

    private fun mostrarMenuOverflowDaBarra() {
        val modo = modoDaBarraDoNavegadorAtual()
        PopupMenu(this, botaoMenuOverflow).apply {
            val idAvancar = View.generateViewId()
            val idRecarregar = View.generateViewId()
            val idInicio = View.generateViewId()
            val idNovaAba = View.generateViewId()
            val idNovaJanela = View.generateViewId()
            val idFixar = View.generateViewId()
            val idCampoTexto = View.generateViewId()
            val idBloqueio = View.generateViewId()
            val idCapturaTela = View.generateViewId()
            val idCapturaMidia = View.generateViewId()
            val idBuscarNaPagina = View.generateViewId()
            val idTecladoNesteSite = View.generateViewId()
            val idExtensoes = View.generateViewId()
            val idBiblioteca = View.generateViewId()
            val idChat = View.generateViewId()
            fun incluir(controle: ControleDaBarraDoNavegador, id: Int, titulo: Int) {
                if (DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(controle, modo)) {
                    menu.add(0, id, 0, titulo)
                }
            }
            incluir(ControleDaBarraDoNavegador.AVANCAR, idAvancar, R.string.avancar_na_pagina)
            incluir(ControleDaBarraDoNavegador.RECARREGAR, idRecarregar, R.string.recarregar_pagina)
            incluir(ControleDaBarraDoNavegador.INICIO, idInicio, R.string.pagina_inicial)
            incluir(ControleDaBarraDoNavegador.NOVA_ABA_NA_FAIXA, idNovaAba, R.string.nova_aba)
            menu.add(0, idNovaJanela, 0, R.string.nova_janela_do_navegador)
            if (
                DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                    ControleDaBarraDoNavegador.FIXAR_ATALHO,
                    modo,
                ) && paginaAtualPermiteFixarAtalho()
            ) {
                val url = paginas[gestao.idDaAbaAtiva]?.webView?.url.orEmpty()
                val fixado = ListaDeAtalhosDaPaginaInicialDoNavegador(preferencias.lerAtalhos()).contem(url)
                menu.add(0, idFixar, 0, R.string.fixar_atalho).apply {
                    isCheckable = true
                    isChecked = fixado
                }
            }
            incluir(ControleDaBarraDoNavegador.EXTENSOES, idExtensoes, R.string.extensoes)
            if (DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                    ControleDaBarraDoNavegador.BLOQUEIO_ANUNCIO,
                    modo,
                )
            ) {
                menu.add(0, idBloqueio, 0, R.string.bloquear_anuncios).apply {
                    isCheckable = true
                    isChecked = interruptorDeBloqueioDeAnuncio.isChecked
                }
            }
            incluir(
                ControleDaBarraDoNavegador.CAMPO_TEXTO,
                idCampoTexto,
                R.string.abrir_campo_de_texto_do_navegador,
            )
            incluir(ControleDaBarraDoNavegador.CAPTURA_TELA, idCapturaTela, R.string.captura_nativa_de_tela)
            incluir(
                ControleDaBarraDoNavegador.BUSCAR_NA_PAGINA,
                idBuscarNaPagina,
                R.string.buscar_na_pagina_do_navegador,
            )
            if (DisposicaoDaBarraDoNavegadorPorLarguraDaTela.vaiParaOMenuOverflow(
                    ControleDaBarraDoNavegador.CAPTURA_MIDIA,
                    modo,
                )
            ) {
                menu.add(0, idCapturaMidia, 0, R.string.capturar_midia_para_download).apply {
                    isCheckable = true
                    isChecked = interruptorDeCapturaDeMidia.isChecked
                }
            }
            incluir(
                ControleDaBarraDoNavegador.BIBLIOTECA,
                idBiblioteca,
                R.string.abrir_biblioteca_de_videos,
            )
            menu.add(0, idTecladoNesteSite, 0, R.string.teclado_neste_site_do_navegador)
            val idPreCarregarPaineis = View.generateViewId()
            menu.add(0, idPreCarregarPaineis, 0, R.string.pre_carregar_paineis_da_colecao_no_menu).apply {
                isCheckable = true
                isChecked = preferencias.preCarregarPaineisDaColecao
            }
            val idGestoDeBordaDoChat = View.generateViewId()
            menu.add(0, idGestoDeBordaDoChat, 0, R.string.abrir_e_fechar_chat_por_gesto_de_borda_no_menu).apply {
                isCheckable = true
                isChecked = preferencias.abrirEFecharChatPorGestoDeBorda
            }
            val idModoImersivo = View.generateViewId()
            menu.add(0, idModoImersivo, 0, R.string.modo_imersivo_no_menu).apply {
                isCheckable = true
                isChecked = preferencias.modoImersivo
            }
            val idRestaurarAbas = View.generateViewId()
            menu.add(0, idRestaurarAbas, 0, R.string.restaurar_abas_ao_abrir_no_menu).apply {
                isCheckable = true
                isChecked = preferencias.restaurarAbasAoAbrir
            }
            val idManterHiggsfieldPronto = View.generateViewId()
            menu.add(0, idManterHiggsfieldPronto, 0, R.string.manter_higgsfield_pronto_no_menu).apply {
                isCheckable = true
                isChecked = preferencias.manterHiggsfieldPronto
            }
            val idAcessibilidade = View.generateViewId()
            menu.add(0, idAcessibilidade, 0, R.string.acessibilidade_das_paginas_no_menu).apply {
                isCheckable = true
                isChecked = preferencias.acessibilidadeDasPaginas
            }
            val idAtualizarModelos = View.generateViewId()
            menu.add(0, idAtualizarModelos, 0, R.string.atualizar_listas_de_modelos_no_menu)
            val idSugestoesOnline = View.generateViewId()
            menu.add(0, idSugestoesOnline, 0, R.string.sugestoes_de_pesquisa_online_no_menu).apply {
                isCheckable = true
                isChecked = preferencias.sugestoesDePesquisaOnline
            }
            val idAparencia = View.generateViewId()
            val idMotorBusca = View.generateViewId()
            val idApresentacaoSites = View.generateViewId()
            menu.add(0, idAparencia, 0, R.string.aparencia_do_navegador_no_menu)
            menu.add(0, idMotorBusca, 0, R.string.motor_de_busca_do_navegador_no_menu)
            menu.add(0, idApresentacaoSites, 0, R.string.apresentacao_dos_sites_do_navegador_no_menu)
            incluir(ControleDaBarraDoNavegador.CHAT, idChat, R.string.abrir_chat_da_openrouter)
            val componentePorIdDoItem = mutableMapOf<Int, String>()
            if (::bibliotecaDeComponentesDaColecao.isInitialized) {
                val componentes = ItensDoMenuOverflowDosComponentesGravadosDaColecao.montar(
                    bibliotecaDeComponentesDaColecao.listar(),
                )
                if (componentes.isNotEmpty()) {
                    (menu as? androidx.appcompat.view.menu.MenuBuilder)?.setGroupDividerEnabled(true)
                    val grupoComponentes = 1
                    menu.add(
                        grupoComponentes,
                        View.generateViewId(),
                        Menu.CATEGORY_SECONDARY,
                        R.string.secao_componentes_gravados_no_menu_overflow,
                    ).apply {
                        isEnabled = false
                    }
                    componentes.forEach { componente ->
                        val idItem = View.generateViewId()
                        menu.add(
                            grupoComponentes,
                            idItem,
                            Menu.CATEGORY_SECONDARY,
                            componente.rotulo,
                        )
                        componentePorIdDoItem[idItem] = componente.id
                    }
                }
            }
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    idAvancar -> avancarNaPagina()
                    idRecarregar -> recarregarPagina()
                    idInicio -> irParaPaginaInicial()
                    idNovaAba -> abrirNovaAba()
                    idNovaJanela -> abrirNovaJanela()
                    idFixar -> alternarAtalhoDaPaginaAtual()
                    idCampoTexto -> painelDoCampoDeTexto.alternar()
                    idBloqueio -> interruptorDeBloqueioDeAnuncio.isChecked =
                        !interruptorDeBloqueioDeAnuncio.isChecked
                    idCapturaTela -> capturaNativaDeTela.mostrarOpcoes()
                    idBuscarNaPagina -> abrirBuscaNaPagina()
                    idCapturaMidia -> definirCapturaDeMidiaParaDownload(
                        !interruptorDeCapturaDeMidia.isChecked,
                    )
                    idExtensoes -> painelDeExtensoes.mostrarLista()
                    idBiblioteca -> painelDaBibliotecaDeVideos.mostrar()
                    idTecladoNesteSite -> mostrarEscolhaDoTecladoNesteSite()
                    idPreCarregarPaineis -> {
                        preferencias.preCarregarPaineisDaColecao = !preferencias.preCarregarPaineisDaColecao
                        agendarWarmStartDosPaineisDaColecao(imediato = true)
                    }
                    idGestoDeBordaDoChat -> {
                        preferencias.abrirEFecharChatPorGestoDeBorda = !preferencias.abrirEFecharChatPorGestoDeBorda
                    }
                    idRestaurarAbas -> alternarRestaurarAbasAoAbrir()
                    idManterHiggsfieldPronto -> alternarManterHiggsfieldPronto()
                    idAcessibilidade -> {
                        preferencias.acessibilidadeDasPaginas = !preferencias.acessibilidadeDasPaginas
                        Toast.makeText(this@AtividadePrincipalDoNavegadorChromebrothers, R.string.acessibilidade_vale_ao_reabrir, Toast.LENGTH_SHORT).show()
                    }
                    idModoImersivo -> {
                        preferencias.modoImersivo = !preferencias.modoImersivo
                        aplicarModoImersivo()
                    }
                    idSugestoesOnline -> {
                        preferencias.sugestoesDePesquisaOnline = !preferencias.sugestoesDePesquisaOnline
                        if (!preferencias.sugestoesDePesquisaOnline) {
                            buscadorDeSugestoesDePesquisa.cancelar()
                            ultimasPesquisasSugeridas = emptyList()
                        }
                    }
                    idAtualizarModelos -> atualizarListasDeModelosDosMcps()
                    idAparencia -> mostrarEscolhaDaAparenciaDoNavegador()
                    idMotorBusca -> mostrarEscolhaDoMotorDeBuscaDoNavegador()
                    idApresentacaoSites -> mostrarEscolhaDaApresentacaoDosSitesDoNavegador()
                    idChat -> painelDoChat.alternar()
                    in componentePorIdDoItem -> {
                        abrirComponenteGravadoPeloMenuOverflow(
                            componentePorIdDoItem.getValue(item.itemId),
                        )
                    }
                    else -> return@setOnMenuItemClickListener false
                }
                true
            }
            show()
            if (componentePorIdDoItem.isNotEmpty()) {
                botaoMenuOverflow.post {
                    val lista = listViewDoMenuOverflow(this@apply) ?: return@post
                    lista.setOnItemLongClickListener { parent, _, posicao, _ ->
                        val bruto = parent.adapter?.getItem(posicao)
                        val menuItem = bruto as? MenuItem
                            ?: (bruto as? androidx.appcompat.view.menu.MenuItemImpl)
                            ?: return@setOnItemLongClickListener false
                        val idComponente = componentePorIdDoItem[menuItem.itemId]
                            ?: return@setOnItemLongClickListener false
                        dismiss()
                        mostrarOpcoesDoComponenteGravadoPeloMenuOverflow(idComponente)
                        true
                    }
                }
            }
        }
    }

    private fun ligarBuscaNaPagina() {
        botaoBuscarNaPagina.setOnClickListener { abrirBuscaNaPagina() }
        findViewById<ImageButton>(R.id.botao_ocorrencia_anterior_da_busca_na_pagina)
            .setOnClickListener {
                cancelarDebounceDaBuscaNaPagina()
                executarBuscaNaPagina(paraTras = true)
            }
        findViewById<ImageButton>(R.id.botao_ocorrencia_seguinte_da_busca_na_pagina)
            .setOnClickListener {
                cancelarDebounceDaBuscaNaPagina()
                executarBuscaNaPagina(paraTras = false)
            }
        findViewById<ImageButton>(R.id.botao_fechar_busca_na_pagina_do_navegador)
            .setOnClickListener { fecharBuscaNaPagina() }
        botaoDiferenciarMaiusculasNaBusca.setOnClickListener {
            diferenciarMaiusculasNaBusca = !diferenciarMaiusculasNaBusca
            atualizarBotaoDiferenciarMaiusculasNaBusca()
            cancelarDebounceDaBuscaNaPagina()
            if (buscaNaPaginaAberta) executarBuscaNaPagina(paraTras = false)
        }
        atualizarBotaoDiferenciarMaiusculasNaBusca()
        campoDeBuscaNaPagina.setOnEditorActionListener { _, acao, _ ->
            if (acao == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                cancelarDebounceDaBuscaNaPagina()
                executarBuscaNaPagina(paraTras = false)
                true
            } else {
                false
            }
        }
        campoDeBuscaNaPagina.setOnKeyListener { _, keyCode, evento ->
            if (keyCode != android.view.KeyEvent.KEYCODE_ENTER) return@setOnKeyListener false
            if (evento.action != android.view.KeyEvent.ACTION_DOWN) return@setOnKeyListener true
            cancelarDebounceDaBuscaNaPagina()
            val paraTras = OpcoesDaBuscaNaPaginaDoNavegador.buscaParaTrasNoEnter(
                shiftPressionado = evento.isShiftPressed,
            )
            executarBuscaNaPagina(paraTras = paraTras)
            true
        }
        campoDeBuscaNaPagina.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (ignorarProximaMudancaDaBuscaNaPagina) {
                    ignorarProximaMudancaDaBuscaNaPagina = false
                    return
                }
                if (!buscaNaPaginaAberta) return
                agendarBuscaNaPaginaComDebounce()
            }
        })
        atualizarContagemDaBuscaNaPagina(encontrado = false, atual = 0, total = 0)
    }

    private fun atualizarBotaoDiferenciarMaiusculasNaBusca() {
        val ligado = diferenciarMaiusculasNaBusca
        botaoDiferenciarMaiusculasNaBusca.setTextColor(
            Color.parseColor(if (ligado) "#1A73E8" else "#5F6368"),
        )
        botaoDiferenciarMaiusculasNaBusca.alpha = if (ligado) 1f else 0.85f
    }

    private fun agendarBuscaNaPaginaComDebounce() {
        handlerDaBuscaNaPagina.removeCallbacks(debouncedBuscaNaPagina)
        handlerDaBuscaNaPagina.postDelayed(
            debouncedBuscaNaPagina,
            OpcoesDaBuscaNaPaginaDoNavegador.ATRASO_DO_DEBOUNCE_MS,
        )
    }

    private fun cancelarDebounceDaBuscaNaPagina() {
        handlerDaBuscaNaPagina.removeCallbacks(debouncedBuscaNaPagina)
    }

    private fun abrirBuscaNaPagina() {
        buscaNaPaginaAberta = true
        faixaDeBuscaNaPagina.visibility = View.VISIBLE
        campoDeBuscaNaPagina.requestFocus()
        val teclado = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        teclado.showSoftInput(campoDeBuscaNaPagina, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        if (campoDeBuscaNaPagina.text.isNotBlank()) {
            cancelarDebounceDaBuscaNaPagina()
            executarBuscaNaPagina(paraTras = false)
        }
    }

    private fun fecharBuscaNaPagina() {
        cancelarDebounceDaBuscaNaPagina()
        buscaNaPaginaAberta = false
        faixaDeBuscaNaPagina.visibility = View.GONE
        paginas.values.forEach { it.webView.limparBuscaNaPagina() }
        val teclado = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        teclado.hideSoftInputFromWindow(campoDeBuscaNaPagina.windowToken, 0)
        atualizarContagemDaBuscaNaPagina(encontrado = false, atual = 0, total = 0)
    }

    private fun aoTrocarAbaLimparOuRepetirBuscaNaPagina(idAnterior: Long?, idNovo: Long) {
        if (idAnterior != null && idAnterior != idNovo) {
            paginas[idAnterior]?.webView?.limparBuscaNaPagina()
        }
        if (!OpcoesDaBuscaNaPaginaDoNavegador.repeteBuscaAoTrocarDeAba(buscaNaPaginaAberta)) return
        cancelarDebounceDaBuscaNaPagina()
        if (campoDeBuscaNaPagina.text.isNullOrBlank()) {
            atualizarContagemDaBuscaNaPagina(encontrado = false, atual = 0, total = 0)
            return
        }
        executarBuscaNaPagina(paraTras = false)
    }

    private fun atualizarContagemDaBuscaNaPagina(encontrado: Boolean, atual: Int, total: Int) {
        val texto = campoDeBuscaNaPagina.text?.toString().orEmpty()
        textoDaContagemDaBuscaNaPagina.text = ContagemDaBuscaNaPaginaDoNavegador.texto(
            atual = atual,
            total = total,
            encontrado = encontrado,
        )
        val semResultado = OpcoesDaBuscaNaPaginaDoNavegador.destacaSemResultado(texto, encontrado)
        val cor = Color.parseColor(if (semResultado) "#D93025" else "#5F6368")
        textoDaContagemDaBuscaNaPagina.setTextColor(cor)
        campoDeBuscaNaPagina.setTextColor(
            Color.parseColor(if (semResultado) "#D93025" else "#202124"),
        )
    }

    private fun executarBuscaNaPagina(paraTras: Boolean) {
        val vista = paginas[gestao.idDaAbaAtiva]?.webView ?: return
        val texto = campoDeBuscaNaPagina.text?.toString().orEmpty()
        vista.buscarNaPagina(
            texto = texto,
            paraTras = paraTras,
            diferenciarMaiusculas = diferenciarMaiusculasNaBusca,
        ) { encontrado, atual, total ->
            if (!buscaNaPaginaAberta) return@buscarNaPagina
            atualizarContagemDaBuscaNaPagina(encontrado = encontrado, atual = atual, total = total)
        }
    }

    private fun listViewDoMenuOverflow(popup: PopupMenu): ListView? {
        return runCatching {
            val campoPopup = PopupMenu::class.java.getDeclaredField("mPopup").also { it.isAccessible = true }
            val ajudante = campoPopup.get(popup) ?: return null
            val obterPopup = ajudante.javaClass.getDeclaredMethod("getPopup").also { it.isAccessible = true }
            val menuPopup = obterPopup.invoke(ajudante) ?: return null
            val obterLista = menuPopup.javaClass.getDeclaredMethod("getListView").also { it.isAccessible = true }
            obterLista.invoke(menuPopup) as? ListView
        }.getOrNull()
    }

    private fun agendarWarmStartDosPaineisDaColecao(imediato: Boolean) {
        if (!::painelDaColecao.isInitialized || !::bibliotecaDeComponentesDaColecao.isInitialized) return
        val raiz = findViewById<View>(R.id.raiz_do_navegador) ?: return
        val rodar = Runnable {
            if (isDestroyed || isFinishing) return@Runnable
            if (!::painelDaColecao.isInitialized || !::bibliotecaDeComponentesDaColecao.isInitialized) return@Runnable
            painelDaColecao.sincronizarWarmStart(
                itens = bibliotecaDeComponentesDaColecao.listar(),
                ligado = preferencias.preCarregarPaineisDaColecao,
            )
        }
        if (imediato) {
            raiz.post(rodar)
        } else {
            // Depois do 1º paint — não atrasa a abertura do navegador.
            raiz.postDelayed(rodar, 1_600L)
        }
    }

    /**
     * "Usar de novo" do ⓘ da gaveta: prepara o preenchimento (receita ou parcial, lendo do disco fora da UI),
     * guarda para o gerador do tipo e abre esse gerador. Aberto agora, ele busca ao carregar;
     * já aberto, recebe o aviso para buscar.
     */
    private fun usarDeNovoNoGerador(pedido: PedidoDeUsarDeNovoNoGerador) {
        if (!::bibliotecaDeComponentesDaColecao.isInitialized || !::painelDaColecao.isInitialized) return
        val ehVideo = pedido.tipo == com.chromebrothers.navegador.regras.ReceitaDeGeracaoMcp.TIPO_VIDEO
        val idDoComponente = if (ehVideo) {
            com.chromebrothers.navegador.regras.HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.ID
        } else {
            com.chromebrothers.navegador.regras.HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.ID
        }
        Thread {
            val servico = servicoDeGeracaoDeImagemViaMcpDaColecao
            val json = runCatching {
                if (pedido.idDaReceita.isNotBlank() && servico.tipoDaReceita(pedido.idDaReceita) != null) {
                    servico.prepararReceitaParaPreencher(pedido.idDaReceita)
                } else {
                    servico.prepararReceitaParcial(pedido.tipo, pedido.prompt, pedido.origem, pedido.modelo, pedido.imagensDeReferencia)
                }
            }.getOrNull()
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                val ok = json != null && runCatching { org.json.JSONObject(json).optBoolean("ok") }.getOrDefault(false)
                if (!ok) {
                    Toast.makeText(this, "Não consegui abrir a receita deste item.", Toast.LENGTH_LONG).show()
                    return@runOnUiThread
                }
                servico.guardarParaPreencher(pedido.tipo, json!!)
                val item = bibliotecaDeComponentesDaColecao.buscar(idDoComponente) ?: run {
                    Toast.makeText(this, R.string.paginas_da_biblioteca_vazias, Toast.LENGTH_LONG).show()
                    return@runOnUiThread
                }
                abrirComponenteDaBibliotecaRespeitandoAAberturaPadrao(item)
                painelDaColecao.janelaDaOrigem(idDoComponente)?.pedirParaPreencherReceita()
            }
        }.start()
    }

    private fun abrirComponenteGravadoPeloMenuOverflow(id: String) {
        if (!::bibliotecaDeComponentesDaColecao.isInitialized || !::painelDaColecao.isInitialized) return
        val item = bibliotecaDeComponentesDaColecao.buscar(id) ?: run {
            Toast.makeText(this, R.string.paginas_da_biblioteca_vazias, Toast.LENGTH_LONG).show()
            return
        }
        val mensagem = abrirComponenteDaBibliotecaRespeitandoAAberturaPadrao(item)
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
    }

    private val preferenciasDaAberturaDosComponentes by lazy {
        getSharedPreferences("abertura_padrao_dos_componentes_da_colecao", MODE_PRIVATE)
    }

    private fun aberturaPadraoDoComponente(id: String) =
        AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Modo.daChave(
            preferenciasDaAberturaDosComponentes.getString(
                AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.chaveDaPreferencia(id),
                null,
            ),
        )

    private fun abrirJuntoComOChat(id: String): Boolean =
        preferenciasDaAberturaDosComponentes.getBoolean(
            AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.chaveDeAbrirJuntoComOChat(id),
            false,
        )

    private fun editorDeImagemAberto(): Boolean =
        ::painelDaBibliotecaDeVideos.isInitialized && painelDaBibliotecaDeVideos.estaEditandoImagem()

    private fun ondeEstaOComponente(id: String): AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Onde {
        val janela = painelDaColecao.janelaDaOrigem(id)
        return when {
            janela == null -> AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Onde.FECHADO
            janela.estaAncoradaNaGaveta() -> AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Onde.NO_CHAT
            janela.estaVisivelNaColecao() -> AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Onde.FLUTUANDO
            else -> AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Onde.OCULTO
        }
    }

    private fun abrirComponentesQueEntramJuntoComOChat() {
        if (!::bibliotecaDeComponentesDaColecao.isInitialized || !::painelDaColecao.isInitialized) return
        val editorAberto = editorDeImagemAberto()
        bibliotecaDeComponentesDaColecao.listar()
            .filter {
                AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.deveAbrirJuntoComOChat(
                    aberturaPadraoDoComponente(it.id),
                    abrirJuntoComOChat(it.id),
                    editorAberto,
                    ondeEstaOComponente(it.id),
                )
            }
            // Entram no chat em segundo plano: a aba do Chat continua na frente.
            .forEach { abrirComponenteDaBibliotecaRespeitandoAAberturaPadrao(it, ativarNoChat = false) }
    }

    /** Chat ou flutuando conforme o padrão do componente; o editor de imagem aberto força flutuar. */
    private fun abrirComponenteDaBibliotecaRespeitandoAAberturaPadrao(
        item: ItemDaBibliotecaDeComponentesHtmlDaColecao,
        ativarNoChat: Boolean = true,
    ): String {
        val existente = painelDaColecao.janelaDaOrigem(item.id)
        val onde = ondeEstaOComponente(item.id)
        val editorAberto = editorDeImagemAberto()
        val temAbas = ::controladorDasAbasAncoradasNaGaveta.isInitialized
        val acao = AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.decidir(aberturaPadraoDoComponente(item.id), editorAberto, onde)
        return when (acao) {
            AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Acao.ABRIR_FLUTUANDO, AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Acao.FOCAR_FLUTUANDO ->
                painelDaColecao.carregarComponenteDaBiblioteca(item)
            AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Acao.ABRIR_NO_CHAT -> {
                val mensagem = painelDaColecao.carregarComponenteDaBiblioteca(item)
                val janela = painelDaColecao.janelaDaOrigem(item.id)
                if (janela != null && temAbas) controladorDasAbasAncoradasNaGaveta.ancorarJanela(janela, ativarNoChat)
                mensagem
            }
            AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Acao.FOCAR_NO_CHAT -> {
                val janela = existente ?: return painelDaColecao.carregarComponenteDaBiblioteca(item)
                janela.revelarSeEstiverOculta()
                if (temAbas && ativarNoChat) controladorDasAbasAncoradasNaGaveta.focarJanela(janela)
                "Abri o componente \"${item.nome}\" no chat."
            }
            AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Acao.TIRAR_DO_CHAT_E_FLUTUAR -> {
                val janela = existente ?: return painelDaColecao.carregarComponenteDaBiblioteca(item)
                if (temAbas) controladorDasAbasAncoradasNaGaveta.tirarDoChatEFlutuar(janela)
                painelDaColecao.carregarComponenteDaBiblioteca(item)
            }
        }
    }

    private fun abrirComponentesPeloChromeDoEditorDeImagem() {
        if (!::bibliotecaDeComponentesDaColecao.isInitialized || !::painelDaColecao.isInitialized) return
        val itens = bibliotecaDeComponentesDaColecao.listar()
            .sortedByDescending { it.atualizadoEm }
        if (itens.isEmpty()) {
            Toast.makeText(this, R.string.paginas_da_biblioteca_vazias, Toast.LENGTH_SHORT).show()
            return
        }
        val opcoes = itens.map { item ->
            (item.nome.trim().ifEmpty { item.id }) to item
        }
        mostrarCaixaDeEscolhaSimplesDoNavegador(
            titulo = getString(R.string.componentes_no_chrome_do_editor_de_imagem),
            opcoes = opcoes,
            indiceSelecionado = -1,
        ) { item ->
            val mensagem = abrirComponenteDaBibliotecaRespeitandoAAberturaPadrao(item)
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarOpcoesDoComponenteGravadoPeloMenuOverflow(id: String) {
        if (!::bibliotecaDeComponentesDaColecao.isInitialized) return
        val item = bibliotecaDeComponentesDaColecao.buscar(id) ?: return
        val nome = item.nome.trim().ifEmpty { item.id }
        val dialogo = Dialog(this)
        val raiz = layoutInflater.inflate(
            R.layout.layout_caixa_de_opcoes_do_componente_gravado_no_menu_overflow_do_navegador,
            null,
        )
        raiz.findViewById<TextView>(R.id.titulo_da_caixa_de_opcoes_do_componente_gravado).text = nome
        raiz.findViewById<Button>(R.id.botao_abrir_componente_gravado_nas_opcoes).setOnClickListener {
            dialogo.dismiss()
            abrirComponenteGravadoPeloMenuOverflow(item.id)
        }
        val modoFlutuando = AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Modo.FLUTUANDO
        val modoNoChat = AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Modo.NO_CHAT
        val botaoFlutuando = raiz.findViewById<Button>(R.id.botao_abertura_padrao_flutuando_do_componente_gravado)
        val botaoNoChat = raiz.findViewById<Button>(R.id.botao_abertura_padrao_no_chat_do_componente_gravado)
        val linhaAbrirJunto = raiz.findViewById<View>(R.id.linha_abrir_junto_com_o_chat_do_componente_gravado)
        val chaveAbrirJunto = raiz.findViewById<android.widget.Switch>(R.id.chave_abrir_junto_com_o_chat_do_componente_gravado)
        val estadosDaChave = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        chaveAbrirJunto.thumbTintList = android.content.res.ColorStateList(
            estadosDaChave,
            intArrayOf(Color.parseColor("#8AB4F8"), Color.parseColor("#C4C7CE")),
        )
        chaveAbrirJunto.trackTintList = android.content.res.ColorStateList(
            estadosDaChave,
            intArrayOf(Color.parseColor("#3A4A66"), Color.parseColor("#2A3140")),
        )
        chaveAbrirJunto.isChecked = abrirJuntoComOChat(item.id)
        chaveAbrirJunto.setOnCheckedChangeListener { _, ligado ->
            preferenciasDaAberturaDosComponentes.edit()
                .putBoolean(AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.chaveDeAbrirJuntoComOChat(item.id), ligado)
                .apply()
        }
        linhaAbrirJunto.setOnClickListener { chaveAbrirJunto.toggle() }
        fun pintarAberturaPadrao() {
            val atual = aberturaPadraoDoComponente(item.id)
            listOf(botaoFlutuando to modoFlutuando, botaoNoChat to modoNoChat).forEach { (botao, modo) ->
                val escolhido = atual == modo
                botao.setBackgroundResource(
                    if (escolhido) R.drawable.fundo_do_segmento_selecionado_da_abertura_padrao_do_componente_gravado
                    else android.R.color.transparent,
                )
                botao.backgroundTintList = null
                botao.setTextColor(Color.parseColor(if (escolhido) "#F4F7FB" else "#8A9099"))
                botao.setTypeface(null, if (escolhido) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            }
            linhaAbrirJunto.visibility = if (atual == modoNoChat) View.VISIBLE else View.GONE
        }
        fun escolherAberturaPadrao(modo: AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Modo) {
            preferenciasDaAberturaDosComponentes.edit()
                .putString(
                    AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.chaveDaPreferencia(item.id),
                    modo.chave,
                )
                .apply()
            pintarAberturaPadrao()
        }
        botaoFlutuando.setOnClickListener { escolherAberturaPadrao(modoFlutuando) }
        botaoNoChat.setOnClickListener { escolherAberturaPadrao(modoNoChat) }
        pintarAberturaPadrao()
        raiz.findViewById<Button>(R.id.botao_remover_componente_gravado_nas_opcoes).setOnClickListener {
            dialogo.dismiss()
            confirmarRemocaoDoComponenteGravadoPeloMenuOverflow(item.id)
        }
        raiz.findViewById<Button>(R.id.botao_cancelar_opcoes_do_componente_gravado).setOnClickListener {
            dialogo.dismiss()
        }
        listOf(
            R.id.botao_abrir_componente_gravado_nas_opcoes,
            R.id.botao_remover_componente_gravado_nas_opcoes,
            R.id.botao_cancelar_opcoes_do_componente_gravado,
        ).forEach { raiz.findViewById<Button>(it).backgroundTintList = null }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        val largura = (400 * resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun confirmarRemocaoDoComponenteGravadoPeloMenuOverflow(id: String) {
        if (!::bibliotecaDeComponentesDaColecao.isInitialized) return
        val item = bibliotecaDeComponentesDaColecao.buscar(id) ?: return
        val nome = item.nome.trim().ifEmpty { item.id }
        val dialogo = Dialog(this)
        val raiz = layoutInflater.inflate(R.layout.layout_caixa_de_dialogo_do_editor_de_imagem, null)
        raiz.findViewById<TextView>(R.id.titulo_da_caixa_de_dialogo_do_editor).text =
            getString(R.string.titulo_da_confirmacao_de_remocao_do_componente_gravado)
        val mensagem = raiz.findViewById<TextView>(R.id.mensagem_da_caixa_de_dialogo_do_editor)
        mensagem.visibility = View.VISIBLE
        mensagem.text = getString(R.string.confirmar_remocao_do_componente_gravado_pelo_menu, nome)
        raiz.findViewById<EditText>(R.id.campo_da_caixa_de_dialogo_do_editor).visibility = View.GONE
        val cancelar = raiz.findViewById<Button>(R.id.botao_cancelar_da_caixa_de_dialogo_do_editor)
        val confirmar = raiz.findViewById<Button>(R.id.botao_confirmar_da_caixa_de_dialogo_do_editor)
        cancelar.text = getString(R.string.cancelar_caixa_do_editor)
        cancelar.backgroundTintList = null
        cancelar.stateListAnimator = null
        confirmar.text = getString(R.string.remover_componente_gravado_pelo_menu_overflow)
        confirmar.setTextColor(Color.parseColor("#F28B82"))
        confirmar.backgroundTintList = null
        confirmar.stateListAnimator = null
        cancelar.setOnClickListener { dialogo.dismiss() }
        confirmar.setOnClickListener {
            dialogo.dismiss()
            removerComponenteGravadoPeloMenuOverflow(item.id)
        }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        val largura = (420 * resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun removerComponenteGravadoPeloMenuOverflow(id: String) {
        if (!::bibliotecaDeComponentesDaColecao.isInitialized || !::gravacaoDaBibliotecaDeComponentes.isInitialized) {
            return
        }
        if (!bibliotecaDeComponentesDaColecao.apagar(id)) return
        gravacaoDaBibliotecaDeComponentes.apagarArquivos(id)
        gravacaoDaBibliotecaDeComponentes.gravar(bibliotecaDeComponentesDaColecao)
        if (::painelDaColecao.isInitialized) painelDaColecao.limparVinculoDaOrigem(id)
        if (::painelDaBibliotecaDeVideos.isInitialized && painelDaBibliotecaDeVideos.estaAberto()) {
            painelDaBibliotecaDeVideos.atualizar()
        }
        avisar(R.string.componente_removido_pelo_menu_overflow)
    }

    override fun onDestroy() {
        manipuladorDasAbasSalvas.removeCallbacks(adormecerPaginaAvulsa)
        manipuladorDasAbasSalvas.removeCallbacks(prepararReserva)
        vistaPreCarregadaAvulsa?.let { avulsa ->
            vistaPreCarregadaAvulsa = null
            runCatching { avulsa.destroy() }
        }
        runCatching { contentResolver.unregisterContentObserver(observadorDeMidiaNovaNoAparelho) }
        window?.decorView?.removeCallbacks(avisoDeMidiaNovaNoAparelho)
        if (::painelDeExtensoes.isInitialized) painelDeExtensoes.fecharPopup()
        fecharJanelaDeLogin()
        cancelarSeletorDeArquivoDaPagina()
        relogioDoPlayerDaPagina.removeCallbacks(pulsoDoPlayerDaPagina)
        relogioDoPlayerDaPagina.removeCallbacks(capturarMiniaturaDoPlayerDaPagina)
        vistaDeTelaCheiaDoVideo?.let { telaCheia ->
            (telaCheia.parent as? ViewGroup)?.removeView(telaCheia)
            vistaDeTelaCheiaDoVideo = null
            callbackDeTelaCheiaDoVideo = null
        }
        visorExternoDaGavetaCriado?.fechar(avisar = false)
        visorDeVideosAoLadoCriado?.fechar(avisar = false)
        if (::painelDaColecao.isInitialized) painelDaColecao.encerrarTodas()
        if (::painelDoChat.isInitialized) painelDoChat.desligar()
        paginas.values.forEach { pagina ->
            inicioDaCapturaPorAba.remove(pagina.id)?.remove()
            inicioDoCampoDeTextoPorAba.remove(pagina.id)?.remove()
            pagina.liberarMiniatura()
            areaDasPaginas.removeView(pagina.webView)
            pagina.webView.destroy()
        }
        paginas.clear()
        super.onDestroy()
    }

    private fun criarPagina(id: Long, endereco: String) {
        val vista = SuperficieDaPaginaNoMotorDoFirefox(this)
        vista.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        val fundo = paletaAtual().fundoDaPagina
        vista.setBackgroundColor(fundo)
        vista.cobrirAteAPrimeiraPintura(fundo)
        val pagina = PaginaDeUmaAbaDoNavegadorChromebrothers(id, vista)
        pagina.agenteOriginal = vista.settings.userAgentString
        paginas[id] = pagina
        prepararNavegador(vista, id)
        areaDasPaginas.addView(vista)
        if (endereco.isNotBlank()) carregarEnderecoNaPagina(pagina, endereco)
    }

    private fun prepararNavegador(vista: SuperficieDaPaginaNoMotorDoFirefox, id: Long) {
        val paginaDona = paginas[id]
        val ponte = PonteJavascriptDaPaginaInicialDoNavegadorChromebrothers(
            tokenEsperado = tokenDaPaginaInicial,
            aoNavegar = { texto -> runOnUiThread { navegarNaAba(id, texto) } },
            aoDefinirAparencia = { escura -> runOnUiThread { definirAparencia(escura) } },
            aoDefinirMotor = { motor -> runOnUiThread { preferencias.motor = com.chromebrothers.navegador.regras.MotorDeBuscaDaBarraDeEndereco.peloId(motor) } },
            aoDefinirModo = { modo -> runOnUiThread { definirModoGlobal(modo) } },
            aoAdicionarAtalho = { titulo, url -> adicionarAtalho(titulo, url) },
            aoRemoverAtalho = { url -> runOnUiThread { removerAtalho(url) } },
            aoAbrirBiblioteca = { runOnUiThread { painelDaBibliotecaDeVideos.mostrar() } },
        )
        vista.definirProtecaoContraRastreamento(preferencias.bloquearAnuncios)
        vista.deveCancelarNavegacao = { destino, temGesto, ehRedirecionamento, pedidaPeloAplicativo ->
            if (EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.deveBaixar(destino, temGesto, ehRedirecionamento)) {
                vista.post { instalarExtensaoPelaLoja(destino) }
                true
            } else if (EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.eh(destino)) {
                true
            } else if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(destino)) {
                paginas[id]?.let { carregarPaginaInicial(it) }
                true
            } else if (
                DecisaoDeNavegacaoParaOutroSite.deveCancelar(
                    bloqueioLigado = preferencias.bloquearAnuncios,
                    temGestoDoUsuario = temGesto,
                    ehRedirecionamento = ehRedirecionamento,
                    urlAtual = vista.url.orEmpty(),
                    urlNova = destino,
                    lista = listaDeBloqueioDeAnuncio,
                    pedidaPeloAplicativo = pedidaPeloAplicativo,
                )
            ) {
                vista.post { avisar(R.string.abertura_de_outro_site_bloqueada) }
                true
            } else {
                false
            }
        }
        vista.aoPromptDaPagina = { mensagem ->
            when {
                mensagem.startsWith(
                    com.chromebrothers.navegador.regras.ScriptsDoPickDeElementoParaOGravadorDeRoteiro.PREFIXO_DO_PROMPT,
                ) -> {
                    val json = mensagem.removePrefix(
                        com.chromebrothers.navegador.regras.ScriptsDoPickDeElementoParaOGravadorDeRoteiro.PREFIXO_DO_PROMPT,
                    )
                    runOnUiThread {
                        if (::painelDoGravadorDeRoteiro.isInitialized) {
                            painelDoGravadorDeRoteiro.receberPick(json)
                        }
                    }
                    ""
                }
                mensagem.startsWith("chromebrothers:") ->
                    responderPonteDaPaginaInicial(ponte, mensagem.removePrefix("chromebrothers:"))
                else -> null
            }
        }
        vista.aoPedidoDeArquivo = { tipos, escolhePasta, varios, prompt, pendente ->
            abrirSeletorDeArquivoDaPagina(id, tipos, escolhePasta, varios, prompt, pendente)
        }
        vista.aoRespostaExterna = { resposta -> gravarRespostaExternaDaPagina(resposta) }
        vista.aoMenuDeContexto = menu@{ endereco ->
            // Toque longo com arraste já trata a imagem; não abre o menu por cima.
            if (arrasteDeImagemVeioDaPagina || fantasmaDoArrasteDeImagemDoComponente != null) return@menu
            copiaDeImagemDaPagina.perguntarPorEndereco(vista, endereco)
        }
        vista.aoMenuDeLink = { endereco -> menuDoLinkDaPagina.mostrar(endereco) }
        val ponteDeVideo = PonteJavascriptParaDetectarVideoNaPagina(
            aoTocar = { mensagem -> runOnUiThread { paginas[id]?.videoAtual = mensagem } },
            aoParar = { runOnUiThread { encerrarVideo(id) } },
            aoLimites = { idVideo, esquerda, topo, largura, altura, viewportLargura, viewportAltura ->
                runOnUiThread {
                    posicionarBotaoFlutuante(
                        id, idVideo, esquerda, topo, largura, altura, viewportLargura, viewportAltura,
                    )
                }
            },
            aoToqueLongo = { idVideo, endereco ->
                runOnUiThread { mostrarMenuDoToqueLongoNoVideo(id, idVideo, endereco) }
            },
            aoMidia = { enderecoBruto, tipoMime, tamanhoTexto ->
                // VK/Boosty põem a faixa no endereço (&bytes=0-…): sem ela é o arquivo inteiro (e um item só).
                val endereco = com.chromebrothers.navegador.regras.NormalizacaoDoEnderecoDeMidia.semFaixaNaQuery(enderecoBruto)
                runOnUiThread {
                    if (!preferencias.capturarMidiaParaDownload) return@runOnUiThread
                    val dona = paginas[id] ?: return@runOnUiThread
                    val peloMime = ClassificacaoDoTipoDeRespostaDeMidiaParaDownload.classificar(tipoMime)
                    val tipo = if (peloMime == TipoDePedidoDeMidiaParaDownload.IGNORAR) {
                        ClassificacaoDoPedidoDeMidiaParaDownload.classificar(endereco)
                    } else {
                        peloMime
                    }
                    if (
                        tipo != TipoDePedidoDeMidiaParaDownload.ARQUIVO &&
                        tipo != TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS &&
                        tipo != TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH
                    ) {
                        return@runOnUiThread
                    }
                    val novo = dona.registrarPedidoDeMidia(
                        endereco,
                        tipo,
                        dona.webView.url.orEmpty(),
                        dona.webView.settings.userAgentString.orEmpty(),
                        dona.geracaoAtualDosPedidosDeMidia(),
                        tamanhoTexto.toLongOrNull() ?: -1L,
                        tipoMime,
                    )
                    if (novo && gestao.idDaAbaAtiva == id) atualizarBotaoDeBaixarMidia()
                }
            },
        )
        vista.aoAvisoDaPagina = { nome, argumentos ->
            when (nome) {
                "onVideoPlaying" -> ponteDeVideo.onVideoPlaying(
                    argumentos.optString(0),
                    argumentos.optString(1),
                    argumentos.optString(2),
                )
                "onVideoStopped" -> ponteDeVideo.onVideoStopped()
                "onVideoBounds" -> ponteDeVideo.onVideoBounds(
                    argumentos.optString(0),
                    argumentos.optString(1),
                    argumentos.optString(2),
                    argumentos.optString(3),
                    argumentos.optString(4),
                    argumentos.optString(5),
                    argumentos.optString(6),
                )
                "onVideoLongPress" -> ponteDeVideo.onVideoLongPress(
                    argumentos.optString(0),
                    argumentos.optString(1),
                )
                "onMediaUrl" -> ponteDeVideo.onMediaUrl(
                    argumentos.optString(0),
                    argumentos.optString(1),
                    argumentos.optString(2),
                )
                com.chromebrothers.navegador.regras.ScriptInjetadoParaArrasteDeImagemDaPaginaDoNavegador.AVISO_INICIO ->
                    runOnUiThread {
                        val clienteX = argumentos.optString(1).toFloatOrNull() ?: 0f
                        val clienteY = argumentos.optString(2).toFloatOrNull() ?: 0f
                        // Preferir rawX do mesmo gesto (JS client/screen não batem no Gecko Android).
                        val tela = coordenadasDeClienteCssDaPaginaParaTela(id, clienteX, clienteY)
                        iniciarArrasteDeImagemDaPagina(
                            endereco = argumentos.optString(0),
                            xNaTela = ultimoXDoToqueNaTela.takeUnless { it == 0f && ultimoYDoToqueNaTela == 0f }
                                ?: tela[0],
                            yNaTela = ultimoYDoToqueNaTela.takeUnless { ultimoXDoToqueNaTela == 0f && it == 0f }
                                ?: tela[1],
                            idDaAba = id,
                        )
                    }
                com.chromebrothers.navegador.regras.ScriptInjetadoParaArrasteDeImagemDaPaginaDoNavegador.AVISO_MOVER ->
                    runOnUiThread {
                        if (!arrasteDeImagemVeioDaPagina) return@runOnUiThread
                        // Com o fantasma ativo o dispatchTouchEvent já move com rawX; JS é só backup.
                        if (fantasmaDoArrasteDeImagemDoComponente != null) return@runOnUiThread
                        val clienteX = argumentos.optString(0).toFloatOrNull() ?: return@runOnUiThread
                        val clienteY = argumentos.optString(1).toFloatOrNull() ?: return@runOnUiThread
                        val tela = coordenadasDeClienteCssDaPaginaParaTela(id, clienteX, clienteY)
                        atualizarFantasmaDoArrasteDeImagemDoComponente(tela[0], tela[1])
                    }
                com.chromebrothers.navegador.regras.ScriptInjetadoParaArrasteDeImagemDaPaginaDoNavegador.AVISO_FIM ->
                    runOnUiThread {
                        if (!arrasteDeImagemVeioDaPagina) return@runOnUiThread
                        val clienteX = argumentos.optString(0).toFloatOrNull()
                        val clienteY = argumentos.optString(1).toFloatOrNull()
                        val tela = if (clienteX != null && clienteY != null) {
                            coordenadasDeClienteCssDaPaginaParaTela(id, clienteX, clienteY)
                        } else {
                            floatArrayOf(ultimoXDoArrasteDoComponente, ultimoYDoArrasteDoComponente)
                        }
                        val x = ultimoXDoToqueNaTela.takeUnless { it == 0f && ultimoYDoToqueNaTela == 0f }
                            ?: tela[0]
                        val y = ultimoYDoToqueNaTela.takeUnless { ultimoXDoToqueNaTela == 0f && it == 0f }
                            ?: tela[1]
                        finalizarArrasteDeImagemDaPagina(x, y)
                    }
            }
        }
        vista.aoNovaSessao = { filha, uri ->
            if (JanelaNovaDoSiteComoModalDeLoginDoNavegador.deveAbrirComoModalDeLogin(uri)) {
                mostrarSessaoDeLogin(filha)
            } else {
                abrirSessaoEmNovaAba(filha, uri, forcarCarga = false)
            }
        }
        vista.aoInstalarExtensaoDaLoja = { endereco -> instalarExtensaoPelaLoja(endereco) }
        vista.aoMudarEndereco = { endereco ->
            // Título só chega depois (aoTitulo); aqui o vista.title ainda é o da página anterior.
            registrarNoHistorico(id, endereco, titulo = null)
            agendarGravacaoDasAbas()
        }
        vista.aoMudarEstadoDaSessao = { agendarGravacaoDasAbas() }
        vista.aoRecuperarDaPerdaDoProcesso = { urlAnterior, travamento ->
            abasCarregando.remove(id)
            val mensagem = if (travamento) {
                "A página travou e foi recarregada"
            } else {
                "O Android fechou esta página em segundo plano; recarregando"
            }
            Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
            val pagina = paginas[id]
            if (pagina != null && urlAnterior != null &&
                EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(urlAnterior)
            ) {
                carregarPaginaInicial(pagina)
                true
            } else {
                false
            }
        }
        vista.aoFechar = {
            if (vistaDaJanelaDeLogin != null) fecharJanelaDeLogin()
            else fecharAba(id)
        }
        vista.aoComecarPagina = { url ->
            abasCarregando.add(id)
            comecosDoChat.toList().forEach { ouvinte -> ouvinte(id) }
            paginaDona?.limparPedidosDeMidia()
            if (gestao.idDaAbaAtiva == id) {
                atualizarBotaoDeBaixarMidia()
                // A aba ativa entrou (ou saiu) do site pesado: a reserva em preparo pausa ou continua.
                vista.post { atualizarReservaConformeAbaAtiva() }
            }
            encerrarVideo(id)
            // Qualquer outro caminho (página inicial, atalho, link de fora): a aba Visor vira aba comum.
            converterAbaDoVisorEmAbaComumSePreciso(id, url)
            if (!EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) {
                vista.publicarScriptEmTodosOsQuadros(
                    ScriptInjetadoParaDetectarVideoHtmlNaPagina.texto(preferencias.capturarMidiaParaDownload),
                )
                vista.publicarScriptEmTodosOsQuadros(
                    com.chromebrothers.navegador.regras.ScriptInjetadoParaArrasteDeImagemDaPaginaDoNavegador.texto(),
                )
            }
            if (gestao.idDaAbaAtiva == id && !campoEndereco.hasFocus()) {
                campoEndereco.setText(textoDaBarra(url))
                atualizarBotoesDaAba(paginas[id])
            }
            gestao.atualizar(id, endereco = url)
        }
        vista.aoTerminarPagina = { url, sucesso ->
            abasCarregando.remove(id)
            paradasDoChat.toList().forEach { ouvinte -> ouvinte(id, sucesso) }
            gestao.atualizar(id, endereco = url)
            registrarNoHistorico(id, url, vista.title)
            pedirFaviconDaPaginaSeFaltar(vista, url)
            atualizarFaviconsDasAbas()
            if (gestao.idDaAbaAtiva == id && !campoEndereco.hasFocus()) {
                campoEndereco.setText(textoDaBarra(url))
                atualizarBotoesDeHistorico(vista)
                atualizarBotoesDaAba(paginas[id])
            }
            if (!EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) {
                vista.publicarScriptEmTodosOsQuadros(
                    ScriptInjetadoParaDetectarVideoHtmlNaPagina.texto(preferencias.capturarMidiaParaDownload),
                )
                vista.publicarScriptEmTodosOsQuadros(
                    com.chromebrothers.navegador.regras.ScriptInjetadoParaArrasteDeImagemDaPaginaDoNavegador.texto(),
                )
                vista.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.instalador(), null)
            }
            if (::painelDoCampoDeTexto.isInitialized) painelDoCampoDeTexto.consultarEstado()
            paginas[id]?.let { capturarMiniatura(it) }
        }
        vista.aoMudarTelaCheia = { cheia ->
            when {
                cheia && gestao.idDaAbaAtiva == id -> entrarNaTelaCheiaDoSite()
                cheia -> vista.sairDaTelaCheia()
                gestao.idDaAbaAtiva == id -> sairDaTelaCheiaDoSite()
            }
        }
        vista.aoTitulo = titulo@{ title ->
            // A aba Visor mantém o nome "Visor" (a página dela é vazia).
            if (id == idDaAbaDoVisor) return@titulo
            val texto = when {
                title.isNullOrBlank() || EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(title) -> getString(R.string.nova_aba)
                else -> title
            }
            gestao.atualizar(id, titulo = texto)
            vista.url?.let { HistoricoDoNavegadorChromebrothers.atualizarTitulo(it, title) }
            agendarGravacaoDasAbas()
            faixaDeAbas.findViewWithTag<TextView>(id)?.text = texto
            if (painelGerenciador.visibility == View.VISIBLE) desenharGerenciadorDeAbas()
        }
        vista.aoProgresso = progressoDaPagina@{ novo ->
            if (gestao.idDaAbaAtiva != id) return@progressoDaPagina
            progresso.progress = novo
            progresso.visibility = if (novo in 1..99) View.VISIBLE else View.GONE
        }
        vista.aoHistorico = {
            if (gestao.idDaAbaAtiva == id) atualizarBotoesDeHistorico(vista)
        }
    }

    private fun responderPonteDaPaginaInicial(
        ponte: PonteJavascriptDaPaginaInicialDoNavegadorChromebrothers,
        mensagem: String,
    ): String {
        val pedido = try {
            org.json.JSONObject(mensagem)
        } catch (_: Exception) {
            return ""
        }
        val token = pedido.optString("token")
        return when (pedido.optString("a")) {
            "navegar" -> {
                ponte.navegar(token, pedido.optString("texto"))
                ""
            }
            "aparencia" -> {
                ponte.definirAparencia(token, pedido.optString("valor"))
                ""
            }
            "motor" -> {
                ponte.definirMotor(token, pedido.optString("valor"))
                ""
            }
            "modo" -> {
                ponte.definirModo(token, pedido.optString("valor"))
                ""
            }
            "biblioteca" -> {
                ponte.abrirBiblioteca(token)
                ""
            }
            "remover" -> {
                ponte.removerAtalho(token, pedido.optString("endereco"))
                ""
            }
            "atalho" -> ponte.adicionarAtalho(token, pedido.optString("titulo"), pedido.optString("endereco"))
            else -> ""
        }
    }

    private fun publicarPacoteDeBloqueioDeAnuncio() {
        val lista = if (preferencias.bloquearAnuncios) {
            listaDeBloqueioDeAnuncio
        } else {
            TextoDaListaDeBloqueioDeAnuncio.ler("")
        }
        MotorDoFirefoxDoNavegadorChromebrothers.publicarPacoteDeBloqueio(
            PacoteDaListaDeBloqueioParaOFirefox.json(lista),
        )
    }

    private fun publicarInicioDaCaptura(pagina: PaginaDeUmaAbaDoNavegadorChromebrothers) {
        pagina.webView.publicarScriptEmTodosOsQuadros(
            ScriptInjetadoParaDetectarVideoHtmlNaPagina.inicio(preferencias.capturarMidiaParaDownload),
        )
    }

    private fun publicarCampoDeTexto(pagina: PaginaDeUmaAbaDoNavegadorChromebrothers) {
        pagina.webView.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.instalador(), null)
    }

    private var abaPedida: Long? = null

    private fun mostrarAba(id: Long) {
        // Tela cheia é da aba que a pediu: trocar de aba sai dela.
        paginas.values.forEach { if (it.id != id) it.webView.sairDaTelaCheia() }
        sairDaTelaCheiaDoSite()
        garantirPaginaDaAba(id)
        if (painelDoPlayerDaPagina.visibility == View.VISIBLE && idDaAbaNoPlayerDaPagina != id) {
            sairDoPlayerDaPagina()
        }
        abaPedida = id
        val visivel = paginas.values.firstOrNull { it.webView.visibility == View.VISIBLE && it.webView.isShown && it.id != id }
        if (visivel != null && visivel.webView.width > 0) {
            capturarMiniatura(visivel) {
                if (abaPedida == id) concluirMostrarAba(id)
            }
        } else {
            concluirMostrarAba(id)
        }
    }

    private fun concluirMostrarAba(id: Long) {
        val idAnterior = gestao.idDaAbaAtiva
        val focoChrome = focoDoTecladoNoChromeNativo()
        val tecladoVisivel = tecladoDoSistemaVisivel()
        if (idAnterior != id) {
            paginas[idAnterior]?.deveRestaurarTecladoAoFocar =
                TecladoAoTrocarDeAbaDoNavegador.deveMarcarRestauracao(
                    tecladoVisivel = tecladoVisivel,
                    focoEmChromeNativo = focoChrome,
                )
        }
        if (!gestao.ativar(id)) return
        sincronizarVisoresComAAbaAtiva(id)
        paginas.values.forEach { pagina ->
            val visivel = pagina.id == id
            // A pré-carregada segue desenhando fora da tela até ser aberta (ou acabar o prazo).
            if (!visivel && pagina.id == idDaAbaPreCarregada && !abaPreCarregadaJaMostrada) return@forEach
            if (visivel) pagina.webView.translationX = 0f
            // Sem coverUntilFirstPaint na troca: aba já pintada não dispara first paint e fica tapada.
            pagina.webView.visibility = if (visivel) View.VISIBLE else View.GONE
            if (visivel) pagina.webView.onResume() else pagina.webView.onPause()
        }
        val pagina = paginas[id] ?: return
        MotorDoFirefoxDoNavegadorChromebrothers.marcarAbaAtiva(pagina.webView.sessao)
        if (!campoEndereco.hasFocus()) {
            campoEndereco.setText(textoDaBarra(pagina.webView.url ?: gestao.ativa()?.endereco))
        }
        atualizarBotoesDeHistorico(pagina.webView)
        atualizarBotoesDaAba(pagina)
        desenharAbas()
        aplicarPosicaoDoBotao(pagina)
        atualizarBotaoDeBaixarMidia()
        progresso.visibility = View.GONE
        sincronizarVisibilidadeDaJanelaDeLogin()
        if (::painelDoCampoDeTexto.isInitialized) painelDoCampoDeTexto.consultarEstado()
        aoTrocarAbaLimparOuRepetirBuscaNaPagina(idAnterior, id)
        if (idAnterior != id) {
            ajustarTecladoAposTrocaDeAba(pagina)
        }
        if (id == idDaAbaPreCarregada) abaPreCarregadaJaMostrada = true
        atualizarReservaConformeAbaAtiva()
    }

    // ---- Aba "Visor": imagem/vídeo tocado na grade abre numa aba própria, não por cima da página atual ----

    private var idDaAbaDoVisor: Long? = null
    private var idDaAbaDeOndeVeioOVisor: Long? = null

    /** Ativa a aba Visor (cria se não existe) antes de mostrar imagem/vídeo nela. */
    private fun garantirAbaDoVisor() {
        val regra = com.chromebrothers.navegador.regras.AbaDoVisorDeMidiaDoNavegador
        val existente = idDaAbaDoVisor?.takeIf { id -> gestao.listar().any { it.id == id } }
        idDaAbaDeOndeVeioOVisor = regra.deOndeVeio(gestao.idDaAbaAtiva, existente, idDaAbaDeOndeVeioOVisor)
        if (existente == null) {
            val aba = gestao.criarAba(endereco = regra.ENDERECO, titulo = regra.TITULO)
            idDaAbaDoVisor = aba.id
            criarPagina(aba.id, aba.endereco)
            mostrarAba(aba.id)
        } else if (gestao.idDaAbaAtiva != existente) {
            mostrarAba(existente)
        }
    }

    /** Fora da aba Visor os visores somem (o da imagem fica na mesma foto; o vídeo pausa). */
    private fun sincronizarVisoresComAAbaAtiva(id: Long) {
        val noVisor = idDaAbaDoVisor == null || id == idDaAbaDoVisor
        visorExternoDaGavetaCriado?.ocultarPorTrocaDeAba(!noVisor)
        visorDeVideosAoLadoCriado?.ocultarPorTrocaDeAba(!noVisor)
    }

    /**
     * A aba Visor navegou para um endereço de verdade: vira aba comum (o visor sai, mas a aba fica).
     * Chamado no início da navegação, para o visor não cobrir a página enquanto ela carrega.
     */
    private fun converterAbaDoVisorEmAbaComumSePreciso(id: Long, url: String?) {
        if (id != idDaAbaDoVisor) return
        if (!com.chromebrothers.navegador.regras.AbaDoVisorDeMidiaDoNavegador.navegacaoTiraDoVisor(url)) return
        idDaAbaDoVisor = null
        idDaAbaDeOndeVeioOVisor = null
        if (visorExternoDaGavetaCriado?.fechar(avisar = false) == true && ::painelDaColecao.isInitialized) {
            painelDaColecao.limparDestaqueDeCartaDoVisualizadorNasJanelas()
        }
        // Com o id já zerado, o aoFechar do vídeo não fecha a aba.
        visorDeVideosAoLadoCriado?.let { if (it.estaAberto() && !it.estaFlutuante()) it.fechar() }
        // Até a página mandar o título, o nome "Visor" sai.
        gestao.atualizar(id, titulo = getString(R.string.nova_aba))
        desenharAbas()
    }

    /** Fechou a imagem ou o vídeo: sem mais nada na aba Visor, ela fecha e volta para a aba de onde veio. */
    private fun fecharAbaDoVisorSeVazia() {
        val id = idDaAbaDoVisor ?: return
        val imagemAberta = visorExternoDaGavetaCriado?.estaAberto() == true
        val videoNaArea = visorDeVideosAoLadoCriado?.let { it.estaAberto() && !it.estaFlutuante() } == true
        if (imagemAberta || videoNaArea) return
        if (gestao.listar().none { it.id == id }) {
            idDaAbaDoVisor = null
            idDaAbaDeOndeVeioOVisor = null
            return
        }
        fecharAba(id)
    }

    private fun focoDoTecladoNoChromeNativo(): Boolean {
        if (campoEndereco.hasFocus()) return true
        if (::campoDeBuscaNaPagina.isInitialized &&
            ::faixaDeBuscaNaPagina.isInitialized &&
            faixaDeBuscaNaPagina.visibility == View.VISIBLE &&
            campoDeBuscaNaPagina.hasFocus()
        ) {
            return true
        }
        return false
    }

    private fun tecladoDoSistemaVisivel(): Boolean {
        val insets = ViewCompat.getRootWindowInsets(window.decorView) ?: return false
        return insets.isVisible(WindowInsetsCompat.Type.ime())
    }

    private fun ajustarTecladoAposTrocaDeAba(destino: PaginaDeUmaAbaDoNavegadorChromebrothers) {
        val focoChrome = focoDoTecladoNoChromeNativo()
        val imm = getSystemService(InputMethodManager::class.java) ?: return
        when {
            TecladoAoTrocarDeAbaDoNavegador.deveRestaurarAoEntrar(
                destinoMarcadoParaRestaurar = destino.deveRestaurarTecladoAoFocar,
                focoEmChromeNativo = focoChrome,
            ) -> {
                destino.webView.pedirFocoParaOTeclado()
                destino.webView.evaluateJavascript(
                    "(function(){var e=document.activeElement;if(e&&e!==document.body&&e.focus){try{e.focus();}catch(x){}}})();",
                )
                destino.webView.post {
                    imm.showSoftInput(destino.webView, InputMethodManager.SHOW_IMPLICIT)
                }
            }
            TecladoAoTrocarDeAbaDoNavegador.deveEsconderAoEntrar(
                destinoMarcadoParaRestaurar = destino.deveRestaurarTecladoAoFocar,
                focoEmChromeNativo = focoChrome,
            ) -> {
                imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
            }
        }
    }

    private fun abrirSessaoPedidaPelaExtensao(sessao: GeckoSession, endereco: String) {
        abrirSessaoEmNovaAba(sessao, endereco, forcarCarga = true)
    }

    /**
     * Sessão já criada pelo Gecko (popup / extensão): vira aba normal.
     * [forcarCarga] só quando a URI ainda não foi pedida pelo motor (extensão).
     */
    private fun abrirSessaoEmNovaAba(sessao: GeckoSession, endereco: String, forcarCarga: Boolean) {
        val destino = endereco.ifBlank { "about:blank" }
        val aba = gestao.criarAba(destino)
        val vista = SuperficieDaPaginaNoMotorDoFirefox(this, sessao)
        vista.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        vista.setBackgroundColor(paletaAtual().fundoDaPagina)
        val pagina = PaginaDeUmaAbaDoNavegadorChromebrothers(aba.id, vista)
        pagina.agenteOriginal = vista.settings.userAgentString
        paginas[aba.id] = pagina
        prepararNavegador(vista, aba.id)
        areaDasPaginas.addView(vista)
        if (endereco.isNotBlank()) {
            if (forcarCarga) runCatching { sessao.loadUri(endereco) }
            gestao.atualizar(aba.id, endereco = endereco)
        }
        mostrarAba(aba.id)
    }

    private fun desenharAbas() {
        agendarGravacaoDasAbas()
        cancelarEsperasDoArrasteDaAba()
        arrasteDaAba = null
        faixaDeAbas.removeAllViews()
        val ativa = gestao.idDaAbaAtiva
        for (aba in gestao.listar()) {
            val item = layoutInflater.inflate(
                R.layout.layout_item_de_aba_do_navegador_chromebrothers,
                faixaDeAbas,
                false,
            )
            val titulo = item.findViewById<TextView>(R.id.titulo_da_aba_do_navegador)
            titulo.tag = aba.id
            titulo.text = aba.titulo
            val paleta = paletaAtual()
            val estaAtiva = aba.id == ativa
            item.background = fundoDaAba(if (estaAtiva) paleta.fundoDaBarra else paleta.fundoDaFaixa)
            titulo.setTextColor(if (estaAtiva) paleta.texto else paleta.textoSuave)
            val favicon = item.findViewById<ImageView>(R.id.favicon_da_aba_do_navegador)
            if (aba.id == idDaAbaDoVisor) {
                favicon?.setImageResource(android.R.drawable.ic_menu_gallery)
                favicon?.imageTintList = ColorStateList.valueOf(paleta.icone)
            } else {
                aplicarFaviconDaAba(favicon, aba.endereco, paleta)
            }
            item.setOnClickListener { mostrarAba(aba.id) }
            prepararArrasteDaAba(item, aba.id)
            item.findViewById<ImageButton>(R.id.botao_fechar_aba_do_navegador).apply {
                imageTintList = ColorStateList.valueOf(paleta.icone)
                setOnClickListener { fecharAba(aba.id) }
            }
            faixaDeAbas.addView(item)
        }
        atualizarContadorDeAbas()
        reservarEspacoDosBotoesDaFaixa()
        val indice = gestao.listar().indexOfFirst { it.id == ativa }
        if (indice >= 0) {
            faixaDeAbas.post {
                val alvo = faixaDeAbas.getChildAt(indice) ?: return@post
                rolagemDasAbas.smoothScrollTo(alvo.left, 0)
            }
        }
    }

    private fun abrirSeletorDeArquivoDaPagina(
        idDaAba: Long,
        tipos: List<String>,
        escolhePasta: Boolean,
        varios: Boolean,
        prompt: org.mozilla.geckoview.GeckoSession.PromptDelegate.FilePrompt,
        pendente: org.mozilla.geckoview.GeckoResult<org.mozilla.geckoview.GeckoSession.PromptDelegate.PromptResponse>,
    ): Boolean {
        encerrarPromptDeArquivoAberto()
        limparCapturasAnterioresDaCamera()
        limparArquivosEntreguesAPagina()
        val pasta = intentDoSeletorDeArquivo(tipos, escolhePasta, varios)
        val cameras = PedidoDeArquivoDaPaginaDoNavegador.cameras(tipos, escolhePasta)
        val iniciais = mutableListOf<Intent>()
        if (CameraDoPedidoDeArquivoDaPagina.FOTO in cameras) {
            intentDaCamera(MediaStore.ACTION_IMAGE_CAPTURE, "jpg")?.let { iniciais += it }
        }
        if (CameraDoPedidoDeArquivoDaPagina.VIDEO in cameras) {
            intentDaCamera(MediaStore.ACTION_VIDEO_CAPTURE, "mp4")?.let { iniciais += it }
        }
        val seletor = Intent.createChooser(pasta, getString(R.string.escolher_arquivo_da_pagina)).apply {
            if (iniciais.isNotEmpty()) putExtra(Intent.EXTRA_INITIAL_INTENTS, iniciais.toTypedArray())
        }
        promptDeArquivoAberto = prompt
        resultadoDoPromptDeArquivo = pendente
        variosArquivosNoSeletor = varios
        idDaAbaDoSeletorDeArquivoDaPagina = idDaAba
        return try {
            retornoDoSeletorDeArquivoDaPagina.launch(seletor)
            true
        } catch (_: ActivityNotFoundException) {
            cancelarSeletorDeArquivoDaPagina()
            true
        }
    }

    private fun intentDoSeletorDeArquivo(tipos: List<String>, escolhePasta: Boolean, varios: Boolean): Intent {
        if (escolhePasta) {
            return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
            )
        }
        val aceitos = TraducaoDoPedidoDeArquivoDoFirefox.tiposQueOSeletorEntende(tipos)
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            if (aceitos.isNotEmpty()) putExtra(Intent.EXTRA_MIME_TYPES, aceitos.toTypedArray())
            if (varios) putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun encerrarPromptDeArquivoAberto() {
        val prompt = promptDeArquivoAberto
        val pendente = resultadoDoPromptDeArquivo
        promptDeArquivoAberto = null
        resultadoDoPromptDeArquivo = null
        if (prompt != null && pendente != null) {
            runCatching { pendente.complete(prompt.dismiss()) }
        }
    }

    private fun intentDaCamera(acao: String, extensao: String): Intent? {
        return try {
            montarIntentDaCamera(acao, extensao)
        } catch (_: Exception) {
            null
        }
    }

    private fun montarIntentDaCamera(acao: String, extensao: String): Intent? {
        val pasta = File(cacheDir, "capturas-da-camera-pedidas-pela-pagina").apply { mkdirs() }
        val arquivo = File(pasta, "${System.currentTimeMillis()}.$extensao")
        if (!arquivo.createNewFile() && !arquivo.exists()) return null
        val uri = FileProvider.getUriForFile(this, "$packageName.arquivos", arquivo)
        val pedido = Intent(acao).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            clipData = ClipData.newUri(contentResolver, arquivo.name, uri)
        }
        val resolvidos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                pedido,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
            )
        } else {
            packageManager.queryIntentActivities(pedido, PackageManager.MATCH_DEFAULT_ONLY)
        }
        if (resolvidos.isEmpty()) {
            arquivo.delete()
            return null
        }
        val permissoes = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        resolvidos.forEach { info ->
            grantUriPermission(info.activityInfo.packageName, uri, permissoes)
        }
        if (acao == MediaStore.ACTION_IMAGE_CAPTURE) {
            arquivoDaFotoCapturadaPelaPagina = arquivo
            uriDaFotoCapturadaPelaPagina = uri
        } else {
            arquivoDoVideoCapturadoPelaPagina = arquivo
            uriDoVideoCapturadoPelaPagina = uri
        }
        return pedido
    }

    private fun entregarArquivoEscolhidoPelaPagina(resultado: androidx.activity.result.ActivityResult) {
        val prompt = promptDeArquivoAberto ?: return
        val pendente = resultadoDoPromptDeArquivo ?: return
        val varios = variosArquivosNoSeletor
        promptDeArquivoAberto = null
        resultadoDoPromptDeArquivo = null
        idDaAbaDoSeletorDeArquivoDaPagina = null
        val escolhidos = if (resultado.resultCode != RESULT_OK) {
            null
        } else {
            val lidos = enderecosDoResultado(resultado.data).mapNotNull { texto ->
                uriQueAPaginaConsegueLer(Uri.parse(texto))
            }
            lidos.takeIf { it.isNotEmpty() }?.toTypedArray()
                ?: arquivoDaCameraPreenchido()?.let { arrayOf(it) }
        }
        apagarCapturasDaCameraNaoUsadas(escolhidos)
        val resposta = when {
            escolhidos == null || escolhidos.isEmpty() -> prompt.dismiss()
            varios || escolhidos.size > 1 -> prompt.confirm(this, escolhidos)
            else -> prompt.confirm(this, escolhidos.first())
        }
        runCatching { pendente.complete(resposta) }
    }

    private fun arquivoDaCameraPreenchido(): Uri? {
        val pares = listOf(
            arquivoDaFotoCapturadaPelaPagina to uriDaFotoCapturadaPelaPagina,
            arquivoDoVideoCapturadoPelaPagina to uriDoVideoCapturadoPelaPagina,
        )
        return pares.firstOrNull { (arquivo, _) -> arquivo != null && arquivo.length() > 0L }
            ?.first
            ?.let { Uri.fromFile(it) }
    }

    private fun apagarCapturasDaCameraNaoUsadas(escolhidos: Array<Uri>?) {
        val caminhos = escolhidos?.mapNotNull { it.path }.orEmpty()
        if (arquivoDaFotoCapturadaPelaPagina?.absolutePath !in caminhos) {
            arquivoDaFotoCapturadaPelaPagina?.delete()
            arquivoDaFotoCapturadaPelaPagina = null
            uriDaFotoCapturadaPelaPagina = null
        }
        if (arquivoDoVideoCapturadoPelaPagina?.absolutePath !in caminhos) {
            arquivoDoVideoCapturadoPelaPagina?.delete()
            arquivoDoVideoCapturadoPelaPagina = null
            uriDoVideoCapturadoPelaPagina = null
        }
    }

    private fun limparCapturasAnterioresDaCamera() {
        arquivoDaFotoCapturadaPelaPagina?.delete()
        arquivoDoVideoCapturadoPelaPagina?.delete()
        arquivoDaFotoCapturadaPelaPagina = null
        arquivoDoVideoCapturadoPelaPagina = null
        uriDaFotoCapturadaPelaPagina = null
        uriDoVideoCapturadoPelaPagina = null
    }

    private fun limparArquivosEntreguesAPagina() {
        arquivosEntreguesAPagina.forEach { it.delete() }
        arquivosEntreguesAPagina.clear()
    }

    private fun enderecosDoResultado(data: Intent?): List<String> {
        if (data == null) return emptyList()
        val clipes = mutableListOf<String?>()
        val clip = data.clipData
        if (clip != null) {
            for (indice in 0 until clip.itemCount) {
                val item = clip.getItemAt(indice)
                clipes += item.uri?.toString()
                clipes += item.intent?.data?.toString()
            }
        }
        val extras = mutableListOf<String?>()
        listOf(Intent.EXTRA_STREAM, "selectedItems", "uris").forEach { chave ->
            urisDaChave(data, chave).forEach { extras += it }
        }
        val doFramework = runCatching {
            WebChromeClient.FileChooserParams.parseResult(RESULT_OK, data)?.map { it.toString() }.orEmpty()
        }.getOrDefault(emptyList())
        return EnderecosDevolvidosPeloSeletorDeArquivoDaPagina.escolher(
            data.data?.toString(),
            clipes + doFramework,
            extras,
        )
    }

    private fun urisDaChave(data: Intent, chave: String): List<String> {
        val encontrados = mutableListOf<String>()
        runCatching {
            val lista = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.getParcelableArrayListExtra(chave, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                data.getParcelableArrayListExtra<Uri>(chave)
            }
            lista?.forEach { encontrados += it.toString() }
        }
        if (encontrados.isNotEmpty()) return encontrados
        runCatching {
            val unico = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.getParcelableExtra(chave, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                data.getParcelableExtra<Uri>(chave)
            }
            if (unico != null) encontrados += unico.toString()
        }
        return encontrados
    }

    private fun uriQueAPaginaConsegueLer(origem: Uri): Uri? {
        if (ehCapturaVazia(origem)) return null
        arquivoLocalDaCaptura(origem)?.let { return Uri.fromFile(it) }
        val copiado = copiarArquivoParaAPagina(origem) ?: return null
        return Uri.fromFile(copiado)
    }

    private fun arquivoLocalDaCaptura(uri: Uri): File? {
        return when (uri) {
            uriDaFotoCapturadaPelaPagina -> arquivoDaFotoCapturadaPelaPagina
            uriDoVideoCapturadoPelaPagina -> arquivoDoVideoCapturadoPelaPagina
            else -> null
        }?.takeIf { it.length() > 0L }
    }

    private fun ehCapturaVazia(uri: Uri): Boolean {
        val arquivo = when (uri) {
            uriDaFotoCapturadaPelaPagina -> arquivoDaFotoCapturadaPelaPagina
            uriDoVideoCapturadoPelaPagina -> arquivoDoVideoCapturadoPelaPagina
            else -> return false
        }
        return arquivo == null || arquivo.length() == 0L
    }

    private fun copiarArquivoParaAPagina(origem: Uri): File? {
        val entrada = try {
            contentResolver.openInputStream(origem)
        } catch (_: Exception) {
            null
        } ?: return null
        val pasta = File(cacheDir, "arquivos-enviados-pela-pagina").apply { mkdirs() }
        val arquivo = File(pasta, "${System.currentTimeMillis()}-${nomeSeguroDoArquivo(origem)}")
        return try {
            entrada.use { lido ->
                arquivo.outputStream().use { saida -> lido.copyTo(saida) }
            }
            if (arquivo.length() == 0L) {
                arquivo.delete()
                null
            } else {
                arquivosEntreguesAPagina += arquivo
                arquivo
            }
        } catch (_: Exception) {
            arquivo.delete()
            null
        }
    }

    private fun nomeSeguroDoArquivo(uri: Uri): String {
        var nome: String? = null
        if (uri.scheme == "content") {
            try {
                contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) nome = cursor.getString(0)
                }
            } catch (_: Exception) {
            }
        }
        val bruto = nome ?: uri.lastPathSegment ?: "arquivo"
        val limpo = bruto.substringAfterLast('/').substringAfterLast('\\')
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .trim('_', '.')
            .take(80)
        return limpo.ifBlank { "arquivo" }
    }

    private fun cancelarSeletorDeArquivoDaPagina() {
        if (promptDeArquivoAberto == null && resultadoDoPromptDeArquivo == null) return
        idDaAbaDoSeletorDeArquivoDaPagina = null
        limparCapturasAnterioresDaCamera()
        encerrarPromptDeArquivoAberto()
    }

    private fun abrirNovaAba() {
        val aba = gestao.criarAba(titulo = getString(R.string.nova_aba))
        criarPagina(aba.id, aba.endereco)
        mostrarAba(aba.id)
    }

    private fun abrirNovaJanela(endereco: String? = null) {
        if (!LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.podeAbrir(quantidadeDeJanelasDoNavegador())) {
            avisar(R.string.limite_de_janelas_do_navegador_atingido)
            return
        }
        val ocupados = slotsDeJanelaOcupados()
        val slot = EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.proximoSlotLivre(ocupados)
            ?: run {
                avisar(R.string.limite_de_janelas_do_navegador_atingido)
                return
            }
        // Preferir Activity com nome diferente (padrão ChromeTabbedActivity2) para o split.
        val classeAlvo = classeDaJanelaParaSlot(slot)
        val pedido = Intent(this, classeAlvo).apply {
            addFlags(
                FlagsDaNovaJanelaDocumentCentricDoNavegador.montar(
                    emMultiJanela = isInMultiWindowMode,
                ),
            )
            val alvo = endereco?.trim().orEmpty()
            if (alvo.isNotEmpty()) {
                putExtra(
                    LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.EXTRA_ENDERECO_INICIAL,
                    alvo,
                )
            }
        }
        startActivity(pedido)
    }

    private fun quantidadeDeJanelasDoNavegador(): Int {
        val gerente = getSystemService(ActivityManager::class.java) ?: return 1
        val total = gerente.appTasks.count { tarefa ->
            val nome = componenteBaseDaTarefa(tarefa)?.className ?: return@count false
            slotDaClasse(nome) != null
        }
        return total.coerceAtLeast(1)
    }

    private fun slotsDeJanelaOcupados(): Set<Int> {
        val gerente = getSystemService(ActivityManager::class.java) ?: return setOf(
            slotDaClasseAtual(),
        )
        val ocupados = linkedSetOf<Int>()
        for (tarefa in gerente.appTasks) {
            val componente = componenteBaseDaTarefa(tarefa) ?: continue
            slotDaClasse(componente.className)?.let { ocupados += it }
        }
        if (ocupados.isEmpty()) ocupados += slotDaClasseAtual()
        return ocupados
    }

    private fun componenteBaseDaTarefa(tarefa: AppTask): ComponentName? {
        val info = runCatching { tarefa.taskInfo }.getOrNull() ?: return null
        return info.baseActivity ?: info.topActivity
    }

    private fun slotDaClasseAtual(): Int =
        slotDaClasse(this::class.java.name)
            ?: EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_PRIMARIA

    private fun slotDaClasse(nome: String): Int? = when (nome) {
        AtividadePrincipalDoNavegadorChromebrothers::class.java.name ->
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_PRIMARIA
        AtividadePrincipalSecundariaDoNavegadorChromebrothers::class.java.name ->
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_SECUNDARIA
        AtividadePrincipalTerciariaDoNavegadorChromebrothers::class.java.name ->
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_TERCIARIA
        else -> null
    }

    private fun classeDaJanelaParaSlot(slot: Int): Class<out AtividadePrincipalDoNavegadorChromebrothers> =
        when (slot) {
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_SECUNDARIA ->
                AtividadePrincipalSecundariaDoNavegadorChromebrothers::class.java
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_TERCIARIA ->
                AtividadePrincipalTerciariaDoNavegadorChromebrothers::class.java
            else -> AtividadePrincipalDoNavegadorChromebrothers::class.java
        }

    private fun abrirLinkEmNovaAba(endereco: String) {
        val aba = gestao.criarAba(endereco = endereco, titulo = getString(R.string.nova_aba))
        criarPagina(aba.id, aba.endereco)
        mostrarAba(aba.id)
    }

    private fun copiarEnderecoDoLink(endereco: String) {
        val area = getSystemService(ClipboardManager::class.java) ?: return
        area.setPrimaryClip(ClipData.newPlainText("link", endereco))
        avisar(R.string.endereco_do_link_copiado)
    }

    private fun copiarEnderecoDaBarra() {
        val endereco = campoEndereco.text?.toString()?.trim().orEmpty()
        if (endereco.isEmpty()) {
            avisar(R.string.endereco_da_barra_vazio_para_copiar)
            return
        }
        copiarEnderecoDoLink(endereco)
    }

    private fun fecharAba(id: Long) {
        // Aba Visor: fecha o que mora nela e volta para a aba de onde veio (se ainda existe).
        var voltarPara: Long? = null
        val fechouAAtiva = gestao.idDaAbaAtiva == id
        if (id == idDaAbaDoVisor) {
            voltarPara = com.chromebrothers.navegador.regras.AbaDoVisorDeMidiaDoNavegador
                .abaParaVoltar(idDaAbaDeOndeVeioOVisor, id, gestao.listar().map { it.id })
            idDaAbaDoVisor = null
            idDaAbaDeOndeVeioOVisor = null
            if (visorExternoDaGavetaCriado?.fechar(avisar = false) == true && ::painelDaColecao.isInitialized) {
                painelDaColecao.limparDestaqueDeCartaDoVisualizadorNasJanelas()
            }
            visorDeVideosAoLadoCriado?.let { if (it.estaAberto() && !it.estaFlutuante()) it.fechar() }
        }
        if (idDaAbaNoPlayerDaPagina == id) fecharPainelDoPlayerDaPagina()
        if (idDaAbaDoSeletorDeArquivoDaPagina == id) cancelarSeletorDeArquivoDaPagina()
        if (idDaAbaDaJanelaDeLogin == id) fecharJanelaDeLogin()
        abasCarregando.remove(id)
        abasAindaNaoCarregadas.remove(id)
        if (id == idDaAbaPreCarregada) encerrarPreCarregamento()
        ultimoEnderecoNoHistoricoPorAba.remove(id)
        abasNavegandoPelaBarra.remove(id)
        val novaAtiva = gestao.fechar(id).let { vizinha ->
            if (fechouAAtiva && voltarPara != null) voltarPara else vizinha
        }
        paginas.remove(id)?.let { pagina ->
            inicioDaCapturaPorAba.remove(id)?.remove()
            inicioDoCampoDeTextoPorAba.remove(id)?.remove()
            pagina.liberarMiniatura()
            if (!guardarComoReservaSePossivel(pagina)) {
                areaDasPaginas.removeView(pagina.webView)
                pagina.webView.stopLoading()
                pagina.webView.destroy()
            }
        }
        if (!paginas.containsKey(novaAtiva) && !abasAindaNaoCarregadas.containsKey(novaAtiva)) {
            val aba = gestao.ativa() ?: return
            criarPagina(aba.id, aba.endereco)
        }
        mostrarAba(novaAtiva)
        if (painelGerenciador.visibility == View.VISIBLE) desenharGerenciadorDeAbas()
    }

    private fun atualizarBotoesDeHistorico(vista: SuperficieDaPaginaNoMotorDoFirefox) {
        botaoVoltar.isEnabled = vista.canGoBack()
        botaoAvancar.isEnabled = vista.canGoForward()
        botaoVoltar.alpha = if (vista.canGoBack()) 1f else 0.38f
        botaoAvancar.alpha = if (vista.canGoForward()) 1f else 0.38f
    }

    private fun posicionarBotaoFlutuante(
        idAba: Long,
        idVideo: String,
        esquerda: Float,
        topo: Float,
        largura: Float,
        altura: Float,
        viewportLargura: Float,
        viewportAltura: Float,
    ) {
        val pagina = paginas[idAba] ?: return
        val video = pagina.videoAtual
        if (video == null || video.id != idVideo) return
        pagina.temLimitesDeVideo = true
        pagina.esquerdaDoVideo = esquerda
        pagina.topoDoVideo = topo
        pagina.larguraDoVideo = largura
        pagina.alturaDoVideo = altura
        pagina.larguraDoViewport = viewportLargura
        pagina.alturaDoViewport = viewportAltura
        if (gestao.idDaAbaAtiva == idAba) aplicarPosicaoDoBotao(pagina)
    }

    private fun aplicarPosicaoDoBotao(pagina: PaginaDeUmaAbaDoNavegadorChromebrothers) {
        val video = pagina.videoAtual
        val mostrar = DecisaoDeMostrarBotaoFlutuanteDoVideo.mostrar(
            temVideoTocando = video != null,
            temLimitesNaTela = pagina.temLimitesDeVideo,
        )
        if (!mostrar || painelDoPlayerDaPagina.visibility == View.VISIBLE) {
            botaoFlutuante.visibility = View.GONE
            return
        }
        val posicao = PosicionamentoDoBotaoFlutuantePertoDoVideo.calcular(
            esquerdaCss = pagina.esquerdaDoVideo,
            topoCss = pagina.topoDoVideo,
            larguraCss = pagina.larguraDoVideo,
            alturaCss = pagina.alturaDoVideo,
            larguraViewportCss = pagina.larguraDoViewport,
            alturaViewportCss = pagina.alturaDoViewport,
            larguraWebViewPx = pagina.webView.width.toFloat(),
            alturaWebViewPx = pagina.webView.height.toFloat(),
            tamanhoBotaoPx = botaoFlutuante.width.toFloat().takeIf { it > 0f } ?: 56f * resources.displayMetrics.density,
            margemPx = 12f * resources.displayMetrics.density,
        )
        if (!posicao.visivel) {
            botaoFlutuante.visibility = View.GONE
            return
        }
        botaoFlutuante.visibility = View.VISIBLE
        botaoFlutuante.x = posicao.x
        botaoFlutuante.y = posicao.y
    }

    private fun encerrarVideo(id: Long) {
        val pagina = paginas[id] ?: return
        pagina.videoAtual = null
        pagina.temLimitesDeVideo = false
        if (gestao.idDaAbaAtiva == id) botaoFlutuante.visibility = View.GONE
    }

    private fun abrirEnderecoDigitado() {
        esconderSugestoesDaBarra()
        val pagina = paginas[gestao.idDaAbaAtiva] ?: return
        val texto = campoEndereco.text.toString()
        marcarNavegacaoPelaBarra(pagina.id, texto)
        autocompletarAtual = null
        buscadorDeSugestoesDePesquisa.cancelar()
        navegarNaAba(pagina.id, texto)
        esconderTeclado()
    }

    /** Endereço aberto pela barra conta como "digitado" no histórico (pesquisas não). */
    private fun marcarNavegacaoPelaBarra(id: Long, textoOuEndereco: String) {
        val destino = InterpretacaoDoTextoDaBarraDeEndereco.resolver(textoOuEndereco, preferencias.motor)
        if (destino.isEmpty() || destino.startsWith(preferencias.motor.prefixo)) return
        abasNavegandoPelaBarra.add(id)
        // Mesmo endereço que já estava aberto: deixa registrar de novo como visita digitada.
        ultimoEnderecoNoHistoricoPorAba.remove(id)
    }

    /** Completa na própria barra o site mais provável, com o resto selecionado (como o Chrome). */
    private fun aplicarAutocompletarNaBarra(texto: Editable, digitado: String) {
        val completo = com.chromebrothers.navegador.regras.RanqueamentoDoHistoricoNaBarraDeEndereco
            .autocompletarEmLinha(digitado, HistoricoDoNavegadorChromebrothers.entradas(), System.currentTimeMillis())
            ?: return
        if (completo.length <= digitado.length) return
        aplicandoAutocompletar = true
        try {
            texto.append(completo.substring(digitado.length))
            campoEndereco.setSelection(digitado.length, texto.length)
        } finally {
            aplicandoAutocompletar = false
        }
        autocompletarAtual = digitado to texto.toString()
    }

    private fun vincularCampoPeloChat(
        campo: com.chromebrothers.navegador.regras.MarcaDoElementoVinculado?,
        envio: com.chromebrothers.navegador.regras.MarcaDoElementoVinculado?,
        aoFim: (String) -> Unit,
    ) {
        val pagina = paginas[gestao.idDaAbaAtiva]
        if (pagina == null) {
            aoFim("Não há página aberta.")
            return
        }
        pagina.webView.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.instalador(), null)
        conferirMarcaDoChat(pagina, campo, "campo") { okCampo ->
            conferirMarcaDoChat(pagina, envio, "envio") { okEnvio ->
                val partes = mutableListOf<String>()
                if (campo != null) {
                    if (okCampo) {
                        painelDoCampoDeTexto.recebeuMarca(
                            "campo",
                            campo.id,
                            campo.nome,
                            campo.rotulo,
                            campo.tag,
                            campo.papel,
                        )
                        partes += "Vinculei a caixa ${com.chromebrothers.navegador.regras.VinculoDoCampoParaOChat.nomeCurto(campo)}."
                    } else {
                        partes += "Não achei a caixa na página."
                    }
                }
                if (envio != null) {
                    if (okEnvio) {
                        painelDoCampoDeTexto.recebeuMarca(
                            "envio",
                            envio.id,
                            envio.nome,
                            envio.rotulo,
                            envio.tag,
                            envio.papel,
                        )
                        partes += "Vinculei o botão ${com.chromebrothers.navegador.regras.VinculoDoCampoParaOChat.nomeCurto(envio)}."
                    } else {
                        partes += "Não achei o botão na página."
                    }
                }
                painelDoCampoDeTexto.consultarEstado()
                aoFim(partes.joinToString(" "))
            }
        }
    }

    private fun conferirMarcaDoChat(
        pagina: PaginaDeUmaAbaDoNavegadorChromebrothers,
        marca: com.chromebrothers.navegador.regras.MarcaDoElementoVinculado?,
        modo: String,
        aoFim: (Boolean) -> Unit,
    ) {
        if (marca == null) {
            aoFim(false)
            return
        }
        pagina.webView.evaluateJavascript(
            com.chromebrothers.navegador.regras.VinculoDoCampoParaOChat.scriptDeConferencia(marca, modo),
        ) { bruto ->
            runOnUiThread { aoFim(textoDoJavascript(bruto) == "ok") }
        }
    }

    private fun lerDaPaginaDepoisDeCarregar(
        aoReceber: (String) -> Unit,
        ler: (PaginaDeUmaAbaDoNavegadorChromebrothers) -> Unit,
    ) {
        val id = gestao.idDaAbaAtiva
        val pagina = paginas[id]
        if (pagina == null) {
            aoReceber("Não há página aberta.")
            return
        }
        if (id in abasCarregando) {
            acompanharCarregamento(id, jaViuComeco = true) {
                val atual = paginas[id]
                if (atual == null) aoReceber("A aba foi fechada.") else ler(atual)
            }
        } else {
            ler(pagina)
        }
    }

    /** Evita travar o chat se o evaluateJavascript da aba nunca responder. */
    private fun avaliarJavascriptDaPaginaComLimite(
        pagina: PaginaDeUmaAbaDoNavegadorChromebrothers,
        script: String,
        limiteMs: Long = 8_000L,
        aoFalhar: () -> Unit,
        aoReceber: (String?) -> Unit,
    ) {
        var entregue = false
        val timeout = Runnable {
            if (entregue || isDestroyed) return@Runnable
            entregue = true
            aoFalhar()
        }
        handlerDaEsperaDoChat.postDelayed(timeout, limiteMs)
        pagina.webView.evaluateJavascript(script) { bruto ->
            runOnUiThread {
                if (entregue || isDestroyed) return@runOnUiThread
                entregue = true
                handlerDaEsperaDoChat.removeCallbacks(timeout)
                aoReceber(bruto)
            }
        }
    }

    private fun acompanharCarregamento(
        id: Long,
        jaViuComeco: Boolean,
        aoPronto: (EsperaDoCarregamentoDaPaginaParaOChat) -> Unit,
    ) {
        val espera = EsperaDoCarregamentoDaPaginaParaOChat(jaViuComeco)
        var pausa: Runnable? = null
        var timeout: Runnable? = null
        var noComeco: ((Long) -> Unit)? = null
        var naParada: ((Long, Boolean) -> Unit)? = null
        fun encerrar() {
            noComeco?.let { comecosDoChat.remove(it) }
            naParada?.let { paradasDoChat.remove(it) }
            pausa?.let { handlerDaEsperaDoChat.removeCallbacks(it) }
            timeout?.let { handlerDaEsperaDoChat.removeCallbacks(it) }
        }
        fun aplicar(passo: PassoDaEsperaDoCarregamento) {
            if (isDestroyed) {
                encerrar()
                return
            }
            if (passo.cancelarPausa) {
                pausa?.let { handlerDaEsperaDoChat.removeCallbacks(it) }
                pausa = null
            }
            if (passo.agendarPausa) {
                pausa?.let { handlerDaEsperaDoChat.removeCallbacks(it) }
                val tarefa = Runnable { aplicar(espera.pausaAcabou()) }
                pausa = tarefa
                handlerDaEsperaDoChat.postDelayed(tarefa, EsperaDoCarregamentoDaPaginaParaOChat.PAUSA_MS)
            }
            if (passo.lerTexto) {
                lerTextoVisivelDaAba(id) { lido -> aplicar(espera.textoLido(lido)) }
            }
            if (passo.pronto) {
                encerrar()
                aoPronto(espera)
            }
        }
        val comeco: (Long) -> Unit = { aba -> if (aba == id) aplicar(espera.comeco()) }
        val parada: (Long, Boolean) -> Unit = { aba, sucesso -> if (aba == id) aplicar(espera.parada(sucesso)) }
        noComeco = comeco
        naParada = parada
        comecosDoChat += comeco
        paradasDoChat += parada
        val tarefaTimeout = Runnable { aplicar(espera.tempoEsgotado()) }
        timeout = tarefaTimeout
        handlerDaEsperaDoChat.postDelayed(tarefaTimeout, EsperaDoCarregamentoDaPaginaParaOChat.LIMITE_MS)
    }

    private fun lerTextoVisivelDaAba(id: Long, aoReceber: (String) -> Unit) {
        lerTextoDaAbaComScript(id, ContextoDaPaginaParaOChat.scriptDoTextoVisivel()) { texto ->
            aoReceber(texto.take(ContextoDaPaginaParaOChat.LIMITE))
        }
    }

    /** Texto normalizado da aba sem corte de 6000 (só teto de segurança); usado na leitura paginada. */
    private fun lerTextoCompletoDaAba(id: Long, aoReceber: (String) -> Unit) {
        lerTextoDaAbaComScript(id, ContextoDaPaginaParaOChat.scriptDoTextoCompletoNormalizado(), aoReceber)
    }

    private fun lerTextoDaAbaComScript(id: Long, script: String, aoReceber: (String) -> Unit) {
        val vista = paginas[id]?.webView
        if (vista == null) {
            aoReceber("")
            return
        }
        if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(vista.url)) {
            aoReceber(TextoDaPaginaInicialParaOChat.descrever(preferencias.lerAtalhos()))
            return
        }
        vista.evaluateJavascript(script) { bruto ->
            runOnUiThread { aoReceber(textoDoJavascript(bruto)) }
        }
    }

    private fun relatoDoCarregamento(
        id: Long,
        espera: EsperaDoCarregamentoDaPaginaParaOChat,
        criouAba: Boolean,
    ): String {
        val vista = paginas[id]?.webView ?: return "A aba foi fechada."
        return RelatoDoCarregamentoDaPaginaParaOChat.relato(
            endereco = vista.url.orEmpty(),
            titulo = vista.title.orEmpty(),
            texto = espera.texto,
            sucesso = espera.sucesso,
            tempoEsgotado = espera.tempoEsgotado,
            criouAba = criouAba,
        )
    }

    private fun navegarNaAba(id: Long, texto: String) {
        garantirPaginaDaAba(id)
        val pagina = paginas[id] ?: return
        val endereco = InterpretacaoDoTextoDaBarraDeEndereco.resolver(texto, preferencias.motor)
        if (endereco.isEmpty()) return
        if (gestao.idDaAbaAtiva == id) esconderTeclado()
        if (adotarPaginaAvulsaSePossivel(id, endereco)) return
        devolverPaginaDoSiteAoSairSePossivel(id, endereco)
        val paginaDoDestino = paginas[id] ?: pagina
        encerrarVideo(id)
        // Endereço digitado na aba Visor: ela vira aba comum já, antes de a página carregar.
        converterAbaDoVisorEmAbaComumSePreciso(id, endereco)
        carregarEnderecoNaPagina(paginaDoDestino, endereco)
    }

    private fun carregarEnderecoNaPagina(pagina: PaginaDeUmaAbaDoNavegadorChromebrothers, endereco: String) {
        aplicarModoDeSite(pagina)
        if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(endereco)) {
            carregarPaginaInicial(pagina)
        } else {
            pagina.webView.loadUrl(endereco)
        }
    }

    private fun carregarPaginaInicial(pagina: PaginaDeUmaAbaDoNavegadorChromebrothers) {
        aplicarModoDeSite(pagina)
        val fundo = paletaAtual().fundoDaPagina
        // Sessão precisa estar ativa antes do cover+load; senão first paint não vem e fica cinza.
        pagina.webView.onResume()
        pagina.webView.setBackgroundColor(fundo)
        pagina.webView.cobrirAteAPrimeiraPintura(fundo)
        pagina.webView.loadDataWithBaseURL(
            EnderecoDaPaginaInicialDoNavegadorChromebrothers.URL,
            HtmlDaPaginaInicialDoNavegadorChromebrothers.montar(tokenDaPaginaInicial, preferencias.estadoDaPaginaInicial()),
            "text/html",
            "UTF-8",
            EnderecoDaPaginaInicialDoNavegadorChromebrothers.URL,
        )
    }

    private fun aplicarModoDeSite(pagina: PaginaDeUmaAbaDoNavegadorChromebrothers) {
        val modo = pagina.modoForcado ?: preferencias.modoDeSite
        pagina.webView.aplicarModo(modo)
        pagina.webView.settings.loadWithOverviewMode = AgenteDeUsuarioDoNavegadorChromebrothers.encolherParaCaberNaTela(modo)
    }

    private fun definirAparencia(escura: Boolean) {
        preferencias.aparenciaEscura = escura
        sincronizarModoNoturnoComAPreferencia()
        MotorDoFirefoxDoNavegadorChromebrothers.aplicarEsquemaDeCorPreferido(escura)
        aplicarAparencia()
        paginas.values.forEach { pagina ->
            if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(pagina.webView.url)) {
                carregarPaginaInicial(pagina)
            }
        }
    }

    private fun sincronizarModoNoturnoComAPreferencia() {
        val alvo = if (preferencias.aparenciaEscura) {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
        } else {
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        }
        if (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() != alvo) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(alvo)
        }
    }

    private fun aplicarFundoDaJanelaAntesDoConteudo() {
        val paleta = paletaAtual()
        window.setBackgroundDrawable(ColorDrawable(paleta.fundoDaPagina))
        window.statusBarColor = paleta.fundoDaFaixa
        window.navigationBarColor = paleta.fundoDaPagina
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !paleta.escura
            isAppearanceLightNavigationBars = !paleta.escura
        }
    }

    private fun definirModoGlobal(id: String) {
        preferencias.modoDeSite = ModoDeApresentacaoDoSite.peloId(id)
        paginas.values.forEach { pagina ->
            aplicarModoDeSite(pagina)
            val url = pagina.webView.url
            if (pagina.modoForcado == null && !url.isNullOrBlank() && !EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) {
                pagina.webView.reload()
            }
        }
        atualizarBotoesDaAba(paginas[gestao.idDaAbaAtiva])
    }

    private fun adicionarAtalho(titulo: String, url: String): ResultadoDoAtalhoDaPaginaInicial {
        val lista = ListaDeAtalhosDaPaginaInicialDoNavegador(preferencias.lerAtalhos())
        val resultado = lista.tentarAdicionar(titulo, url)
        if (resultado == ResultadoDoAtalhoDaPaginaInicial.ADICIONADO) {
            preferencias.gravarAtalhos(lista.listar())
            runOnUiThread { atualizarBotoesDaAba(paginas[gestao.idDaAbaAtiva]) }
        }
        return resultado
    }

    private fun removerAtalho(url: String) {
        val lista = ListaDeAtalhosDaPaginaInicialDoNavegador(preferencias.lerAtalhos())
        lista.remover(url)
        preferencias.gravarAtalhos(lista.listar())
        atualizarBotoesDaAba(paginas[gestao.idDaAbaAtiva])
    }

    private fun alternarAtalhoDaPaginaAtual() {
        val pagina = paginas[gestao.idDaAbaAtiva] ?: return
        val url = pagina.webView.url.orEmpty()
        val paginaHttp = url.startsWith("http://") || url.startsWith("https://")
        if (!paginaHttp || EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) {
            avisar(R.string.atalho_so_http)
            return
        }
        val lista = ListaDeAtalhosDaPaginaInicialDoNavegador(preferencias.lerAtalhos())
        if (lista.contem(url)) {
            lista.remover(url)
            preferencias.gravarAtalhos(lista.listar())
            avisar(R.string.atalho_removido)
        } else {
            val titulo = pagina.webView.title?.takeIf { it.isNotBlank() } ?: url
            when (lista.tentarAdicionar(titulo, url)) {
                ResultadoDoAtalhoDaPaginaInicial.ADICIONADO -> {
                    preferencias.gravarAtalhos(lista.listar())
                    avisar(R.string.atalho_fixado)
                }
                ResultadoDoAtalhoDaPaginaInicial.LIMITE -> avisar(R.string.limite_de_atalhos)
                else -> avisar(R.string.atalho_so_http)
            }
        }
        recarregarPaginasIniciais()
        atualizarBotoesDaAba(pagina)
    }

    private fun recarregarPaginasIniciais() {
        paginas.values.forEach { pagina ->
            if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(pagina.webView.url)) carregarPaginaInicial(pagina)
        }
    }

    private fun alternarVersaoParaComputador() {
        val pagina = paginas[gestao.idDaAbaAtiva] ?: return
        val padrao = preferencias.modoDeSite
        val efetivo = pagina.modoForcado ?: padrao
        val proximo = AlternanciaDaVersaoParaComputadorDaAba.proximo(efetivo, padrao)
        pagina.modoForcado = if (proximo == padrao) null else proximo
        aplicarModoDeSite(pagina)
        if (!EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(pagina.webView.url)) pagina.webView.reload()
        avisar(mensagemDoModo(proximo))
        atualizarBotoesDaAba(pagina)
    }

    private fun mensagemDoModo(modo: ModoDeApresentacaoDoSite): Int {
        return when (modo) {
            ModoDeApresentacaoDoSite.TABLET -> R.string.sites_como_tablet
            ModoDeApresentacaoDoSite.CELULAR -> R.string.sites_como_celular
            ModoDeApresentacaoDoSite.COMPUTADOR -> R.string.sites_como_computador
        }
    }

    private fun atualizarBotoesDaAba(pagina: PaginaDeUmaAbaDoNavegadorChromebrothers?) {
        val paleta = paletaAtual()
        val url = pagina?.webView?.url.orEmpty()
        val naInicial = pagina == null || url.isBlank() || EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)
        val fixarNaBarra = DisposicaoDaBarraDoNavegadorPorLarguraDaTela.visivelNaBarra(
            ControleDaBarraDoNavegador.FIXAR_ATALHO,
            modoDaBarraDoNavegadorAtual(),
        )
        botaoFixar.visibility = if (!naInicial && fixarNaBarra) View.VISIBLE else View.GONE
        val fixado = !naInicial && ListaDeAtalhosDaPaginaInicialDoNavegador(preferencias.lerAtalhos()).contem(url)
        botaoFixar.imageTintList = ColorStateList.valueOf(if (fixado) paleta.destaque else paleta.icone)
        val modo = pagina?.modoForcado ?: preferencias.modoDeSite
        botaoComputador.imageTintList = ColorStateList.valueOf(
            if (modo == ModoDeApresentacaoDoSite.COMPUTADOR) paleta.destaque else paleta.icone,
        )
    }

    private fun aplicarAparencia() {
        val paleta = paletaAtual()
        MotorDoFirefoxDoNavegadorChromebrothers.aplicarEsquemaDeCorPreferido(paleta.escura)
        findViewById<View>(R.id.raiz_do_navegador).setBackgroundColor(paleta.fundoDaFaixa)
        findViewById<View>(R.id.coluna_do_navegador).setBackgroundColor(paleta.fundoDaPagina)
        faixaSuperior.setBackgroundColor(paleta.fundoDaFaixa)
        barraDeNavegacao.setBackgroundColor(paleta.fundoDaBarra)
        areaVisivel.setBackgroundColor(paleta.fundoDaPagina)
        areaDasPaginas.setBackgroundColor(paleta.fundoDaPagina)
        window.setBackgroundDrawable(ColorDrawable(paleta.fundoDaPagina))
        campoEndereco.setTextColor(paleta.texto)
        campoEndereco.setHintTextColor(paleta.textoSuave)
        caixaDaBarraDeEndereco.background = GradientDrawable().apply {
            cornerRadius = 21f * resources.displayMetrics.density
            setColor(paleta.fundoDoCampo)
        }
        botaoIrAoEnderecoDaBarra.imageTintList = ColorStateList.valueOf(paleta.icone)
        botaoCopiarEnderecoDaBarra.imageTintList = ColorStateList.valueOf(paleta.icone)
        findViewById<View>(R.id.faixa_de_botoes_de_extensao).setBackgroundColor(paleta.fundoDaBarra)
        listOf(botaoVoltar, botaoAvancar, botaoRecarregar, botaoInicio, botaoExtensoes).forEach { botao ->
            botao.imageTintList = ColorStateList.valueOf(paleta.icone)
        }
        botaoBibliotecaNaBarra.imageTintList = ColorStateList.valueOf(paleta.icone)
        botaoChatNaBarra.imageTintList = ColorStateList.valueOf(paleta.icone)
        botaoCapturaDeTela.imageTintList = ColorStateList.valueOf(paleta.icone)
        botaoBuscarNaPagina.imageTintList = ColorStateList.valueOf(paleta.icone)
        botaoMenuOverflow.imageTintList = ColorStateList.valueOf(paleta.icone)
        interruptorDeCapturaDeMidia.setTextColor(paleta.texto)
        interruptorDeBloqueioDeAnuncio.setTextColor(paleta.texto)
        (botaoCampoDeTexto as? TextView)?.setTextColor(paleta.icone)
        botaoNovaAba.imageTintList = ColorStateList.valueOf(paleta.icone)
        botaoGerenciador.setTextColor(paleta.icone)
        val borda = botaoGerenciador.background?.mutate()
        val forma = when (borda) {
            is GradientDrawable -> borda
            is InsetDrawable -> borda.drawable?.mutate() as? GradientDrawable
            else -> null
        }
        forma?.setStroke((1.75f * resources.displayMetrics.density).toInt().coerceAtLeast(1), paleta.icone)
        botaoNovaAbaNaGestao.imageTintList = ColorStateList.valueOf(paleta.icone)
        botaoNovaJanelaNaGestao.setTextColor(paleta.icone)
        botaoFecharGestao.imageTintList = ColorStateList.valueOf(paleta.icone)
        painelGerenciador.setBackgroundColor(
            if (paleta.escura) android.graphics.Color.parseColor("#1F1F1F") else android.graphics.Color.parseColor("#E8EAED"),
        )
        window.statusBarColor = paleta.fundoDaFaixa
        window.navigationBarColor = paleta.fundoDaPagina
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !paleta.escura
            isAppearanceLightNavigationBars = !paleta.escura
        }
        paginas.values.forEach { it.webView.setBackgroundColor(paleta.fundoDaPagina) }
        if (faixaDeAbas.childCount > 0) desenharAbas()
        if (painelGerenciador.visibility == View.VISIBLE) desenharGerenciadorDeAbas()
        atualizarBotoesDaAba(paginas[gestao.idDaAbaAtiva])
    }

    private fun escolherArquivoDeExtensao() {
        val pedido = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("application/x-xpinstall", "application/zip", "application/octet-stream"),
            )
        }
        retornoDoSeletorDeExtensao.launch(pedido)
    }

    private fun abrirLojaDeExtensoesDoFirefox() {
        navegarNaAba(gestao.idDaAbaAtiva, EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.PAGINA)
    }

    private fun instalarExtensaoPelaLoja(endereco: String) {
        if (!EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.eh(endereco)) return
        if (!reservarInstalacaoDeExtensao(endereco)) return
        avisar(R.string.baixando_extensao_da_loja)
        Thread {
            val destino = File(cacheDir, "extensao-da-loja-${System.currentTimeMillis()}.xpi")
            val baixou = baixarArquivoDeExtensaoDaLoja(endereco, destino)
            runOnUiThread {
                liberarInstalacaoDeExtensao(endereco)
                concluirInstalacaoDeExtensao(destino, baixou)
            }
        }.start()
    }

    private fun instalarExtensaoPelosBytes(endereco: String, corpo: java.io.InputStream) {
        if (!reservarInstalacaoDeExtensao(endereco)) {
            runCatching { corpo.close() }
            return
        }
        avisar(R.string.baixando_extensao_da_loja)
        Thread {
            val destino = File(cacheDir, "extensao-da-loja-${System.currentTimeMillis()}.xpi")
            val baixou = runCatching {
                corpo.use { entrada -> gravarPacoteDeExtensao(entrada, destino) }
            }.getOrDefault(false)
            runOnUiThread {
                liberarInstalacaoDeExtensao(endereco)
                concluirInstalacaoDeExtensao(destino, baixou)
            }
        }.start()
    }

    private fun concluirInstalacaoDeExtensao(destino: File, baixou: Boolean) {
        if (isFinishing || isDestroyed) {
            destino.delete()
            return
        }
        if (!baixou) {
            destino.delete()
            avisar(R.string.extensao_nao_instalada)
            return
        }
        MotorDoFirefoxDoNavegadorChromebrothers.instalarExtensao(destino.toURI().toString()) { instalou ->
            runOnUiThread {
                destino.delete()
                avisar(if (instalou) R.string.extensao_instalada else R.string.extensao_nao_instalada)
            }
        }
    }

    private fun reservarInstalacaoDeExtensao(endereco: String): Boolean {
        synchronized(instalacoesDeExtensaoEmCurso) {
            return instalacoesDeExtensaoEmCurso.add(endereco)
        }
    }

    private fun liberarInstalacaoDeExtensao(endereco: String) {
        synchronized(instalacoesDeExtensaoEmCurso) {
            instalacoesDeExtensaoEmCurso.remove(endereco)
        }
    }

    private fun baixarArquivoDeExtensaoDaLoja(endereco: String, destino: File): Boolean {
        return runCatching {
            val conexao = java.net.URL(endereco).openConnection() as java.net.HttpURLConnection
            conexao.instanceFollowRedirects = true
            conexao.connectTimeout = 20_000
            conexao.readTimeout = 60_000
            conexao.setRequestProperty("Accept", "application/x-xpinstall,*/*")
            val agente = paginas[gestao.idDaAbaAtiva]?.webView?.settings?.userAgentString.orEmpty()
            if (agente.isNotBlank()) conexao.setRequestProperty("User-Agent", agente)
            conexao.connect()
            if (!conexao.url.toString().startsWith("https://")) return false
            if (conexao.responseCode !in 200..299) return false
            conexao.inputStream.use { entrada -> gravarPacoteDeExtensao(entrada, destino) }
        }.getOrDefault(false)
    }

    private fun gravarPacoteDeExtensao(entrada: java.io.InputStream, destino: File): Boolean {
        destino.outputStream().use { saida ->
            val buffer = ByteArray(16 * 1024)
            var total = 0L
            var cabecalho = true
            while (true) {
                val lido = entrada.read(buffer)
                if (lido < 0) break
                if (cabecalho) {
                    if (lido < 2 || buffer[0] != 'P'.code.toByte() || buffer[1] != 'K'.code.toByte()) return false
                    cabecalho = false
                }
                total += lido
                if (total > 80L * 1024L * 1024L) return false
                saida.write(buffer, 0, lido)
            }
            return total > 4L
        }
    }

    private fun irParaPaginaInicial() {
        val pagina = paginas[gestao.idDaAbaAtiva] ?: return
        fecharGerenciadorDeAbas {
            esconderTeclado()
            encerrarVideo(pagina.id)
            carregarPaginaInicial(pagina)
        }
    }

    private fun alternarGerenciadorDeAbas() {
        if (painelGerenciador.visibility == View.VISIBLE) fecharGerenciadorDeAbas()
        else abrirGerenciadorDeAbas()
    }

    private fun abrirGerenciadorDeAbas() {
        val pagina = paginas[gestao.idDaAbaAtiva]
        esconderTeclado()
        desenharGerenciadorDeAbas()
        animarEntradaDoGerenciador()
        if (pagina != null) capturarMiniatura(pagina)
    }

    private fun animarEntradaDoGerenciador() {
        areaDasPaginas.animate().cancel()
        areaDasPaginas.scaleX = 1f
        areaDasPaginas.scaleY = 1f
        areaDasPaginas.alpha = 1f
        painelGerenciador.animate().cancel()
        painelGerenciador.visibility = View.VISIBLE
        painelGerenciador.alpha = 0f
        painelGerenciador.translationY = 18f * resources.displayMetrics.density
        painelGerenciador.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(260)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun fecharGerenciadorDeAbas(aoTerminar: (() -> Unit)? = null) {
        if (painelGerenciador.visibility != View.VISIBLE) {
            aoTerminar?.invoke()
            return
        }
        painelGerenciador.animate().cancel()
        painelGerenciador.animate()
            .alpha(0f)
            .translationY(12f * resources.displayMetrics.density)
            .setDuration(220)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                if (isDestroyed) return@withEndAction
                painelGerenciador.visibility = View.GONE
                painelGerenciador.alpha = 1f
                painelGerenciador.translationY = 0f
                aoTerminar?.invoke()
            }
            .start()
    }

    private fun reservarEspacoDosBotoesDaFaixa() {
        faixaDeAbas.post {
            if (faixaSuperior.width <= 0) return@post
            if (modoDaBarraDoNavegadorAtual() == ModoDaBarraDoNavegadorPorLargura.COMPACTA) {
                val vazio = espacoAntesDoContador.layoutParams as LinearLayout.LayoutParams
                if (vazio.weight != 1f) {
                    vazio.width = 0
                    vazio.height = 0
                    vazio.weight = 1f
                    espacoAntesDoContador.layoutParams = vazio
                }
                return@post
            }
            val densidade = resources.displayMetrics.density
            val params = rolagemDasAbas.layoutParams as LinearLayout.LayoutParams
            val vazio = espacoAntesDoContador.layoutParams as LinearLayout.LayoutParams
            val margemNovaAba =
                (botaoNovaAba.layoutParams as? LinearLayout.LayoutParams)?.marginStart
                    ?: (4 * densidade).toInt()
            val margemContador =
                (botaoGerenciador.layoutParams as? LinearLayout.LayoutParams)?.marginStart
                    ?: (2 * densidade).toInt()
            val ocupado = ReservaDoEspacoDosBotoesDaFaixaDeAbasParaEvitarSobreposicao.larguraReservadaAosBotoesPx(
                paddingStartPx = faixaSuperior.paddingStart,
                paddingEndPx = faixaSuperior.paddingEnd,
                larguraDoBotaoNovaAbaPx = botaoNovaAba.width,
                margemStartDoBotaoNovaAbaPx = margemNovaAba,
                larguraDoContadorPx = botaoGerenciador.width,
                margemStartDoContadorPx = margemContador,
                fallbackLarguraDoBotaoNovaAbaPx = (40 * densidade).toInt(),
                fallbackLarguraDoContadorPx = (40 * densidade).toInt(),
            )
            val disposicao = ReservaDoEspacoDosBotoesDaFaixaDeAbasParaEvitarSobreposicao.calcular(
                larguraDaFaixaPx = faixaSuperior.width,
                larguraDoConteudoDasAbasPx = faixaDeAbas.width,
                larguraReservadaAosBotoesPx = ocupado,
            )
            if (
                params.width != disposicao.larguraDaRolagemPx ||
                params.weight != disposicao.pesoDaRolagem ||
                vazio.weight != disposicao.pesoDoVaoAntesDoContador
            ) {
                params.width = disposicao.larguraDaRolagemPx
                params.weight = disposicao.pesoDaRolagem
                rolagemDasAbas.layoutParams = params
                vazio.width = 0
                vazio.height = 0
                vazio.weight = disposicao.pesoDoVaoAntesDoContador
                espacoAntesDoContador.layoutParams = vazio
            }
            garantirBotoesDaFaixaAcimaDasAbas()
        }
    }

    private fun garantirBotoesDaFaixaAcimaDasAbas() {
        val elevacao = 8f * resources.displayMetrics.density
        if (botaoNovaAba.elevation < elevacao) botaoNovaAba.elevation = elevacao
        if (botaoGerenciador.elevation < elevacao) botaoGerenciador.elevation = elevacao
        botaoNovaAba.bringToFront()
        espacoAntesDoContador.bringToFront()
        botaoGerenciador.bringToFront()
    }

    private fun atualizarContadorDeAbas() {
        val quantidade = gestao.listar().size.coerceAtLeast(1)
        botaoGerenciador.text = if (quantidade > 99) "99+" else quantidade.toString()
        botaoGerenciador.textSize = if (quantidade > 99) 9f else 12f
        botaoGerenciador.contentDescription = "${getString(R.string.gerenciador_de_abas)}: $quantidade"
        botaoGerenciador.post { reservarEspacoDosBotoesDaFaixa() }
    }

    private fun desenharGerenciadorDeAbas() {
        gradeDeAbas.removeAllViews()
        val paleta = paletaAtual()
        val largura = painelGerenciador.width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels
        val passo = (320 * resources.displayMetrics.density).toInt()
        gradeDeAbas.columnCount = (largura / passo).coerceAtLeast(1)
        val ativa = gestao.idDaAbaAtiva
        for (aba in gestao.listar()) {
            val cartao = layoutInflater.inflate(
                R.layout.layout_cartao_de_aba_do_gerenciador_chromebrothers,
                gradeDeAbas,
                false,
            )
            val titulo = cartao.findViewById<TextView>(R.id.titulo_do_cartao_da_aba)
            titulo.text = aba.titulo
            titulo.setTextColor(paleta.texto)
            aplicarFaviconDaAba(cartao.findViewById(R.id.favicon_do_cartao_da_aba), aba.endereco, paleta)
            cartao.background = fundoDoCartao(paleta.fundoDaBarra, aba.id == ativa)
            cartao.outlineProvider = ViewOutlineProvider.BACKGROUND
            cartao.clipToOutline = true
            val miniatura = paginas[aba.id]?.miniaturaDaPagina
            cartao.findViewById<ImageView>(R.id.miniatura_do_cartao_da_aba).apply {
                if (miniatura != null && !miniatura.isRecycled) setImageBitmap(miniatura) else setImageDrawable(null)
                setBackgroundColor(if (paleta.escura) android.graphics.Color.parseColor("#111111") else android.graphics.Color.parseColor("#DADCE0"))
            }
            cartao.findViewById<ImageButton>(R.id.botao_fechar_cartao_da_aba).apply {
                imageTintList = ColorStateList.valueOf(paleta.icone)
                setOnClickListener { fecharAba(aba.id) }
            }
            cartao.setOnClickListener {
                fecharGerenciadorDeAbas { mostrarAba(aba.id) }
            }
            gradeDeAbas.addView(cartao, parametrosDoCartao())
        }
    }

    private fun fundoDoCartao(cor: Int, ativa: Boolean): GradientDrawable {
        val densidade = resources.displayMetrics.density
        return GradientDrawable().apply {
            setColor(cor)
            cornerRadius = 18f * densidade
            setStroke(
                ((if (ativa) 3f else 1f) * densidade).toInt().coerceAtLeast(1),
                if (ativa) paletaAtual().destaque else paletaAtual().textoSuave,
            )
        }
    }

    private fun parametrosDoCartao(): GridLayout.LayoutParams {
        val margem = (10 * resources.displayMetrics.density).toInt()
        val largura = (300 * resources.displayMetrics.density).toInt()
        return GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED),
            GridLayout.spec(GridLayout.UNDEFINED),
        ).apply {
            width = largura
            height = GridLayout.LayoutParams.WRAP_CONTENT
            setMargins(margem, margem, margem, margem)
        }
    }

    private fun capturarMiniatura(
        pagina: PaginaDeUmaAbaDoNavegadorChromebrothers,
        aoTerminar: (() -> Unit)? = null,
    ) {
        val vista = pagina.webView
        if (vista.width <= 0 || vista.height <= 0 || vista.visibility != View.VISIBLE) {
            aoTerminar?.invoke()
            return
        }
        vista.capturarImagemDaPagina { bitmap ->
            if (bitmap == null || bitmap.isRecycled) {
                aoTerminar?.invoke()
                return@capturarImagemDaPagina
            }
            val origem = if (bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false).also { bitmap.recycle() }
            } else {
                bitmap
            }
            if (origem == null) {
                aoTerminar?.invoke()
                return@capturarImagemDaPagina
            }
            val larguraAlvo = 480
            val reduzida = if (origem.width <= larguraAlvo) {
                origem
            } else {
                val alturaAlvo = (origem.height * (larguraAlvo.toFloat() / origem.width)).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(origem, larguraAlvo, alturaAlvo, true).also { origem.recycle() }
            }
            pagina.substituirMiniatura(reduzida)
            if (gradeDeAbas.childCount > 0) desenharGerenciadorDeAbas()
            aoTerminar?.invoke()
        }
    }

    private fun paletaAtual(): PaletaDaInterfaceDoNavegadorChromebrothers {
        return PaletaDaInterfaceDoNavegadorChromebrothers.escolher(preferencias.aparenciaEscura)
    }

    private fun fundoDaAba(cor: Int): GradientDrawable {
        val raio = 16f * resources.displayMetrics.density
        return GradientDrawable().apply {
            setColor(cor)
            cornerRadii = floatArrayOf(raio, raio, raio, raio, 0f, 0f, 0f, 0f)
        }
    }

    private fun textoDaBarra(url: String?): String {
        if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) return ""
        // Aba Visor (página vazia): barra limpa em vez de "about:blank".
        if (idDaAbaDoVisor != null && gestao.idDaAbaAtiva == idDaAbaDoVisor &&
            url == com.chromebrothers.navegador.regras.AbaDoVisorDeMidiaDoNavegador.ENDERECO
        ) {
            return ""
        }
        return url.orEmpty()
    }

    private fun esconderTeclado() {
        campoEndereco.clearFocus()
        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(campoEndereco.windowToken, 0)
    }

    /** Evita o select da barra ao ancorar o campo de texto (foco “vaza” do EditText escondido). */
    fun limparFocoDaBarraDeEnderecoESugestoes() {
        escolhendoSugestao = false
        if (::campoEndereco.isInitialized && campoEndereco.hasFocus()) {
            campoEndereco.clearFocus()
        }
        if (::listaDeSugestoes.isInitialized) {
            esconderSugestoesDaBarra()
        }
    }

    /**
     * Uma visita por mudança de endereço na aba (recarregar não conta de novo, como no Chrome).
     * Se a navegação saiu da barra de endereço, conta como digitada (pesa mais nas sugestões).
     */
    private fun registrarNoHistorico(id: Long, endereco: String?, titulo: String?) {
        val url = endereco?.trim().orEmpty()
        if (url.isEmpty()) return
        if (ultimoEnderecoNoHistoricoPorAba[id] == url) {
            HistoricoDoNavegadorChromebrothers.atualizarTitulo(url, titulo)
            return
        }
        if (!com.chromebrothers.navegador.regras.RegrasDoHistoricoCompletoDoNavegador.deveGuardar(url)) return
        ultimoEnderecoNoHistoricoPorAba[id] = url
        val digitada = abasNavegandoPelaBarra.remove(id)
        HistoricoDoNavegadorChromebrothers.registrarVisita(url, titulo, digitada)
    }

    private fun pedirFaviconDaPaginaSeFaltar(
        vista: SuperficieDaPaginaNoMotorDoFirefox,
        endereco: String?,
    ) {
        val url = endereco?.trim().orEmpty()
        if (!url.startsWith("http://") && !url.startsWith("https://")) return
        if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) return
        if (!::cacheDeFavicons.isInitialized || cacheDeFavicons.tem(url)) return
        vista.evaluateJavascript(
            CacheDeFaviconsPorHostDoNavegadorChromebrothers.scriptDoHrefDoFavicon(),
        ) { bruto ->
            val href = textoDoJavascript(bruto).trim().trim('"')
            cacheDeFavicons.pedirBaixar(
                enderecoDaPagina = url,
                urlDoIcone = href.ifBlank { null },
                aoProntoNaUi = {
                    atualizarFaviconsDasAbas()
                    if (listaDeSugestoesEstaoVisiveis()) {
                        mostrarSugestoesDaBarra(campoEndereco.text?.toString().orEmpty())
                    }
                },
                postarNaUi = { acao -> runOnUiThread(acao) },
            )
        }
    }

    /** Ícone de 16dp à esquerda do título da aba, como no Chrome. */
    private fun aplicarFaviconDaAba(
        icone: ImageView?,
        endereco: String,
        paleta: PaletaDaInterfaceDoNavegadorChromebrothers,
    ) {
        if (icone == null) return
        val url = endereco.trim()
        if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) {
            icone.setImageResource(R.drawable.icone_da_pagina_inicial_do_navegador_chromebrothers)
            icone.imageTintList = ColorStateList.valueOf(paleta.icone)
            return
        }
        val bmp = if (::cacheDeFavicons.isInitialized) cacheDeFavicons.ler(url) else null
        if (bmp != null) {
            icone.imageTintList = null
            icone.setImageBitmap(bmp)
            return
        }
        icone.setImageResource(R.drawable.icone_globo_da_aba_sem_favicon)
        icone.imageTintList = ColorStateList.valueOf(paleta.textoSuave)
        // Abas restauradas que ainda não carregaram: tenta o /favicon.ico do host.
        if (::cacheDeFavicons.isInitialized && (url.startsWith("http://") || url.startsWith("https://"))) {
            cacheDeFavicons.pedirBaixar(
                enderecoDaPagina = url,
                urlDoIcone = null,
                aoProntoNaUi = { atualizarFaviconsDasAbas() },
                postarNaUi = { acao -> runOnUiThread(acao) },
            )
        }
    }

    /** Atualiza só os ícones (sem redesenhar a faixa, para não interromper arrastes). */
    private fun atualizarFaviconsDasAbas() {
        val abas = gestao.listar()
        val paleta = paletaAtual()
        if (::faixaDeAbas.isInitialized && faixaDeAbas.childCount == abas.size) {
            abas.forEachIndexed { i, aba ->
                if (aba.id == idDaAbaDoVisor) return@forEachIndexed
                aplicarFaviconDaAba(
                    faixaDeAbas.getChildAt(i)?.findViewById(R.id.favicon_da_aba_do_navegador),
                    aba.endereco,
                    paleta,
                )
            }
        }
        if (::gradeDeAbas.isInitialized && gradeDeAbas.childCount == abas.size) {
            abas.forEachIndexed { i, aba ->
                aplicarFaviconDaAba(
                    gradeDeAbas.getChildAt(i)?.findViewById(R.id.favicon_do_cartao_da_aba),
                    aba.endereco,
                    paleta,
                )
            }
        }
    }

    private fun mostrarSugestoesDaBarra(textoNaBarra: String) {
        // Com autocompletar ativo, a consulta é só o que a pessoa digitou (sem o pedaço selecionado).
        val completo = autocompletarAtual?.takeIf { it.second == textoNaBarra }
        val digitado = completo?.first ?: textoNaBarra
        val paginaAtual = textoDaBarra(paginas[gestao.idDaAbaAtiva]?.webView?.url)
        val semConsulta = digitado.trim().isEmpty() || digitado.trim() == paginaAtual.trim()
        val agora = System.currentTimeMillis()
        val historico = HistoricoDoNavegadorChromebrothers.entradas()
        val visitas = HistoricoDoNavegadorChromebrothers.recentes().map { it.comoVisita() }
        val sugestoes = if (semConsulta) {
            buscadorDeSugestoesDePesquisa.cancelar()
            ListaDeSugestoesDaBarraDeEndereco.paginasRecentes(visitas)
        } else {
            ListaDeSugestoesDaBarraDeEndereco.montarComoChrome(
                digitado = digitado,
                historico = historico,
                pesquisasSugeridas = pesquisasSugeridasPara(digitado),
                motor = preferencias.motor,
                agoraMs = agora,
                autocompletado = completo?.second,
            )
        }
        val atalhos = if (semConsulta) {
            FaixaDeAtalhosDaBarraDeEndereco.montar(preferencias.lerAtalhos(), visitas)
        } else {
            emptyList()
        }
        if (sugestoes.isEmpty() && atalhos.isEmpty()) {
            esconderSugestoesDaBarra()
            return
        }
        val animarAbertura = AnimacaoDaListaDeSugestoesDaBarraDeEnderecoDoNavegador.deveAnimarAbertura(
            listaJaVisivel = listaDeSugestoesEstaoVisiveis() && listaDeSugestoes.alpha >= 0.99f,
        )
        cancelarAnimacaoDasSugestoesDaBarra()
        conteudoDasSugestoes.removeAllViews()
        val paleta = paletaAtual()
        listaDeSugestoes.setBackgroundColor(paleta.fundoDaBarra)
        if (atalhos.isNotEmpty()) conteudoDasSugestoes.addView(faixaDeAtalhosDaBarra(atalhos, paleta))
        sugestoes.forEach { sugestao -> adicionarLinhaDeSugestao(sugestao, paleta) }
        molduraDasSugestoes.visibility = View.VISIBLE
        molduraDasSugestoes.bringToFront()
        alinharListaDeSugestoesComACaixaDeEndereco()
        if (animarAbertura) {
            listaDeSugestoes.alpha = 0f
            listaDeSugestoes.post {
                if (!listaDeSugestoesEstaoVisiveis()) return@post
                alinharListaDeSugestoesComACaixaDeEndereco()
                val deslocamento = AnimacaoDaListaDeSugestoesDaBarraDeEnderecoDoNavegador
                    .deslocamentoInicialEmPixels(
                        listaDeSugestoes.height,
                        resources.displayMetrics.density,
                    )
                listaDeSugestoes.translationY = deslocamento
                animacaoDasSugestoesDaBarra = listaDeSugestoes.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(AnimacaoDaListaDeSugestoesDaBarraDeEnderecoDoNavegador.DURACAO_ABRIR_MS)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .withEndAction { animacaoDasSugestoesDaBarra = null }
                animacaoDasSugestoesDaBarra?.start()
            }
        } else {
            listaDeSugestoes.alpha = 1f
            listaDeSugestoes.translationY = 0f
            listaDeSugestoes.post { alinharListaDeSugestoesComACaixaDeEndereco() }
        }
    }

    /**
     * Sugestões do motor para o texto: usa o cache se já tiver; senão pede (chegam em seguida e
     * redesenham a lista) e, enquanto isso, reaproveita as anteriores que ainda combinam — sem piscar.
     */
    private fun pesquisasSugeridasPara(digitado: String): List<String> {
        if (!preferencias.sugestoesDePesquisaOnline) return emptyList()
        val texto = digitado.trim()
        val motor = preferencias.motor
        buscadorDeSugestoesDePesquisa.emCache(motor, texto)?.let { prontas ->
            ultimasPesquisasSugeridas = prontas
            return prontas
        }
        buscadorDeSugestoesDePesquisa.pedir(motor, texto) { pedido, _ ->
            if (!campoEndereco.hasFocus()) return@pedir
            val atual = campoEndereco.text?.toString().orEmpty()
            val consultaAtual = autocompletarAtual?.takeIf { it.second == atual }?.first ?: atual
            if (consultaAtual.trim() == pedido) mostrarSugestoesDaBarra(atual)
        }
        val baixo = texto.lowercase()
        return ultimasPesquisasSugeridas.filter { it.lowercase().startsWith(baixo) }
    }

    private fun listaDeSugestoesEstaoVisiveis(): Boolean =
        ::molduraDasSugestoes.isInitialized && molduraDasSugestoes.visibility == View.VISIBLE

    private fun alinharListaDeSugestoesComACaixaDeEndereco() {
        val pai = molduraDasSugestoes.parent as? View ?: return
        if (caixaDaBarraDeEndereco.width <= 0 || pai.width <= 0) return
        val ancora = if (
            ::faixaDeBotoesDeExtensao.isInitialized &&
            faixaDeBotoesDeExtensao.visibility == View.VISIBLE
        ) {
            faixaDeBotoesDeExtensao
        } else {
            barraDeNavegacao
        }
        val posCaixa = IntArray(2)
        val posAncora = IntArray(2)
        val posPai = IntArray(2)
        caixaDaBarraDeEndereco.getLocationInWindow(posCaixa)
        ancora.getLocationInWindow(posAncora)
        pai.getLocationInWindow(posPai)
        // Margens do FrameLayout são relativas à área interna (depois do padding das system bars).
        val margens = AlinhamentoDaListaDeSugestoesComACaixaDeEnderecoDoNavegador.margens(
            esquerdaDaCaixaNoPai = posCaixa[0] - posPai[0],
            larguraDaCaixa = caixaDaBarraDeEndereco.width,
            larguraDoPai = pai.width,
            paddingEsquerdoDoPai = pai.paddingLeft,
            paddingDireitoDoPai = pai.paddingRight,
        )
        val topo = AlinhamentoDaListaDeSugestoesComACaixaDeEnderecoDoNavegador.topoAbaixoDaAncora(
            topoDaAncoraNaJanela = posAncora[1] - posPai[1],
            alturaDaAncora = ancora.height,
            paddingTopoDoPai = pai.paddingTop,
        )
        val params = molduraDasSugestoes.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        if (params.marginStart == margens.inicio &&
            params.marginEnd == margens.fim &&
            params.topMargin == topo
        ) {
            return
        }
        params.marginStart = margens.inicio
        params.marginEnd = margens.fim
        params.topMargin = topo
        molduraDasSugestoes.layoutParams = params
    }

    private fun adicionarLinhaDeSugestao(
        sugestao: com.chromebrothers.navegador.regras.SugestaoDaBarraDeEndereco,
        paleta: PaletaDaInterfaceDoNavegadorChromebrothers,
    ) {
        val item = layoutInflater.inflate(
            R.layout.layout_item_de_sugestao_da_barra_de_endereco_do_navegador_chromebrothers,
            conteudoDasSugestoes,
            false,
        )
        val titulo = item.findViewById<TextView>(R.id.titulo_da_sugestao_da_barra_de_endereco)
        val detalhe = item.findViewById<TextView>(R.id.detalhe_da_sugestao_da_barra_de_endereco)
        val icone = item.findViewById<TextView>(R.id.icone_da_sugestao_da_barra_de_endereco)
        val favicon = item.findViewById<ImageView>(R.id.favicon_da_sugestao_da_barra_de_endereco)
        val remover = item.findViewById<TextView>(R.id.botao_remover_sugestao_da_barra_de_endereco)
        remover.setTextColor(paleta.textoSuave)
        if (sugestao.tipo == TipoDaSugestaoDaBarraDeEndereco.PAGINA) {
            remover.visibility = View.VISIBLE
            remover.setOnClickListener {
                escolhendoSugestao = false
                esquecerSugestaoDaBarra(sugestao.endereco)
            }
        }
        titulo.setTextColor(paleta.texto)
        detalhe.setTextColor(paleta.textoSuave)
        icone.text = IconeDaPaginaNaBarraDeEndereco.letra(sugestao.titulo, sugestao.endereco)
        icone.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(IconeDaPaginaNaBarraDeEndereco.cor(sugestao.endereco))
        }
        val ehPesquisa = sugestao.tipo == TipoDaSugestaoDaBarraDeEndereco.BUSCA ||
            sugestao.tipo == TipoDaSugestaoDaBarraDeEndereco.SUGESTAO_DE_BUSCA
        val bmp = if (::cacheDeFavicons.isInitialized && !ehPesquisa) {
            cacheDeFavicons.ler(sugestao.endereco)
        } else {
            null
        }
        if (ehPesquisa) {
            favicon.setImageResource(R.drawable.ic_lupa_da_sugestao_de_pesquisa)
            favicon.imageTintList = android.content.res.ColorStateList.valueOf(paleta.textoSuave)
            favicon.visibility = View.VISIBLE
            icone.visibility = View.INVISIBLE
        } else if (bmp != null) {
            favicon.setImageBitmap(bmp)
            favicon.visibility = View.VISIBLE
            icone.visibility = View.INVISIBLE
        } else {
            favicon.setImageDrawable(null)
            favicon.visibility = View.GONE
            icone.visibility = View.VISIBLE
        }
        when (sugestao.tipo) {
            TipoDaSugestaoDaBarraDeEndereco.BUSCA -> {
                titulo.text = getString(R.string.buscar_com_o_texto_da_barra, sugestao.titulo)
                detalhe.text = preferencias.motor.nome
            }
            TipoDaSugestaoDaBarraDeEndereco.ENDERECO -> {
                titulo.text = sugestao.endereco
                detalhe.text = getString(R.string.acessar_o_endereco_digitado)
            }
            TipoDaSugestaoDaBarraDeEndereco.PAGINA -> {
                titulo.text = sugestao.titulo
                val host = IconeDaPaginaNaBarraDeEndereco.hospedeiro(sugestao.endereco)
                detalhe.text = host.ifBlank { sugestao.endereco }
            }
            TipoDaSugestaoDaBarraDeEndereco.SUGESTAO_DE_BUSCA -> {
                titulo.text = sugestao.titulo
                detalhe.visibility = View.GONE
                // Seta ↖ do Chrome: põe a sugestão na barra para continuar digitando.
                remover.text = "↖"
                remover.contentDescription = getString(R.string.completar_com_esta_pesquisa)
                remover.visibility = View.VISIBLE
                remover.setOnClickListener {
                    escolhendoSugestao = false
                    val novo = sugestao.titulo + " "
                    campoEndereco.setText(novo)
                    campoEndereco.setSelection(novo.length)
                }
            }
        }
        item.setOnTouchListener { _, evento ->
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> escolhendoSugestao = true
                MotionEvent.ACTION_CANCEL -> {
                    escolhendoSugestao = false
                    esconderSugestoesDaBarra()
                }
            }
            false
        }
        item.setOnClickListener {
            escolhendoSugestao = false
            esconderSugestoesDaBarra()
            autocompletarAtual = null
            buscadorDeSugestoesDePesquisa.cancelar()
            marcarNavegacaoPelaBarra(gestao.idDaAbaAtiva, sugestao.endereco)
            navegarNaAba(gestao.idDaAbaAtiva, sugestao.endereco)
        }
        conteudoDasSugestoes.addView(item)
    }

    private fun faixaDeAtalhosDaBarra(
        atalhos: List<com.chromebrothers.navegador.regras.AtalhoDaPaginaInicial>,
        paleta: PaletaDaInterfaceDoNavegadorChromebrothers,
    ): View {
        val densidade = resources.displayMetrics.density
        val faixa = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val margem = (8 * densidade).toInt()
            setPadding(margem, margem, margem, (12 * densidade).toInt())
        }
        atalhos.forEach { atalho ->
            val coluna = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                val largura = (76 * densidade).toInt()
                layoutParams = LinearLayout.LayoutParams(largura, LinearLayout.LayoutParams.WRAP_CONTENT)
                setOnClickListener {
                    escolhendoSugestao = false
                    esconderSugestoesDaBarra()
                    navegarNaAba(gestao.idDaAbaAtiva, atalho.endereco)
                }
            }
            val lado = (44 * densidade).toInt()
            val bolha = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(lado, lado)
                // Igual ao Chrome Android: favicon inscrito num círculo escuro.
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#202124"))
                }
            }
            val letra = TextView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
                gravity = android.view.Gravity.CENTER
                textSize = 18f
                setTextColor(Color.WHITE)
                text = IconeDaPaginaNaBarraDeEndereco.letra(atalho.titulo, atalho.endereco)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(IconeDaPaginaNaBarraDeEndereco.cor(atalho.endereco))
                }
            }
            val favicon = ImageView(this).apply {
                val ladoDoIcone = (24 * densidade).toInt()
                layoutParams = FrameLayout.LayoutParams(ladoDoIcone, ladoDoIcone, android.view.Gravity.CENTER)
                scaleType = ImageView.ScaleType.FIT_CENTER
                visibility = View.GONE
            }
            val bmp = if (::cacheDeFavicons.isInitialized) {
                cacheDeFavicons.ler(atalho.endereco)
            } else {
                null
            }
            if (bmp != null) {
                favicon.setImageBitmap(bmp)
                favicon.visibility = View.VISIBLE
                letra.visibility = View.INVISIBLE
            } else if (::cacheDeFavicons.isInitialized) {
                cacheDeFavicons.pedirBaixar(
                    enderecoDaPagina = atalho.endereco,
                    urlDoIcone = null,
                    aoProntoNaUi = {
                        if (listaDeSugestoesEstaoVisiveis()) {
                            mostrarSugestoesDaBarra(campoEndereco.text?.toString().orEmpty())
                        }
                    },
                    postarNaUi = { acao -> runOnUiThread(acao) },
                )
            }
            bolha.addView(letra)
            bolha.addView(favicon)
            coluna.addView(bolha)
            coluna.addView(TextView(this).apply {
                text = atalho.titulo
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                textSize = 11f
                setTextColor(paleta.textoSuave)
                setPadding(0, (4 * densidade).toInt(), 0, 0)
            })
            faixa.addView(coluna)
        }
        return HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(faixa)
        }
    }

    private fun esquecerSugestaoDaBarra(endereco: String) {
        HistoricoDoNavegadorChromebrothers.esquecer(endereco)
        val atalhos = ListaDeAtalhosDaPaginaInicialDoNavegador(preferencias.lerAtalhos())
        if (atalhos.contem(endereco)) {
            atalhos.remover(endereco)
            preferencias.gravarAtalhos(atalhos.listar())
            atualizarBotoesDaAba(paginas[gestao.idDaAbaAtiva])
        }
        if (campoEndereco.hasFocus()) mostrarSugestoesDaBarra(campoEndereco.text.toString())
    }

    private fun esconderSugestoesDaBarra(imediato: Boolean = false) {
        escolhendoSugestao = false
        if (!::molduraDasSugestoes.isInitialized) return
        cancelarAnimacaoDasSugestoesDaBarra()
        if (!listaDeSugestoesEstaoVisiveis()) {
            conteudoDasSugestoes.removeAllViews()
            listaDeSugestoes.alpha = 1f
            listaDeSugestoes.translationY = 0f
            return
        }
        if (imediato) {
            concluirFechamentoDasSugestoesDaBarra()
            return
        }
        val deslocamento = AnimacaoDaListaDeSugestoesDaBarraDeEnderecoDoNavegador.deslocamentoInicialEmPixels(
            listaDeSugestoes.height,
            resources.displayMetrics.density,
        )
        animacaoDasSugestoesDaBarra = listaDeSugestoes.animate()
            .alpha(0f)
            .translationY(deslocamento)
            .setDuration(AnimacaoDaListaDeSugestoesDaBarraDeEnderecoDoNavegador.DURACAO_FECHAR_MS)
            .setInterpolator(FastOutSlowInInterpolator())
            .withEndAction {
                concluirFechamentoDasSugestoesDaBarra()
                animacaoDasSugestoesDaBarra = null
            }
        animacaoDasSugestoesDaBarra?.start()
    }

    private fun cancelarAnimacaoDasSugestoesDaBarra() {
        animacaoDasSugestoesDaBarra?.cancel()
        animacaoDasSugestoesDaBarra = null
        if (::listaDeSugestoes.isInitialized) {
            listaDeSugestoes.animate().cancel()
        }
    }

    private fun concluirFechamentoDasSugestoesDaBarra() {
        molduraDasSugestoes.visibility = View.GONE
        listaDeSugestoes.alpha = 1f
        listaDeSugestoes.translationY = 0f
        conteudoDasSugestoes.removeAllViews()
    }

    private fun toqueCaiNaVista(vista: View, rawX: Float, rawY: Float): Boolean {
        if (vista.visibility != View.VISIBLE || vista.width <= 0 || vista.height <= 0) return false
        val loc = IntArray(2)
        vista.getLocationOnScreen(loc)
        val x = rawX.toInt()
        val y = rawY.toInt()
        return x >= loc[0] && x < loc[0] + vista.width && y >= loc[1] && y < loc[1] + vista.height
    }

    private fun prepararArrasteDaAba(item: View, id: Long) {
        val limite = ViewConfiguration.get(this).scaledTouchSlop
        var inicioX = 0f
        var inicioY = 0f
        var ultimoX = 0f
        val espera = Runnable {
            esperasDoArrasteDaAba.remove(item)
            brutoXDoToqueDaAba = ultimoX
            comecarArrasteDaAba(item, id)
        }
        esperasDoArrasteDaAba[item] = espera
        item.setOnTouchListener { vista, evento ->
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    inicioX = evento.rawX
                    inicioY = evento.rawY
                    ultimoX = evento.rawX
                    vista.postDelayed(espera, ViewConfiguration.getLongPressTimeout().toLong())
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val arraste = arrasteDaAba
                    if (arraste != null && arraste.id == id) {
                        acompanharArrasteDaAba(evento.rawX)
                        true
                    } else {
                        ultimoX = evento.rawX
                        val andou = kotlin.math.abs(evento.rawX - inicioX) > limite ||
                            kotlin.math.abs(evento.rawY - inicioY) > limite
                        if (andou) {
                            vista.removeCallbacks(espera)
                            esperasDoArrasteDaAba.remove(vista)
                        }
                        false
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    vista.removeCallbacks(espera)
                    esperasDoArrasteDaAba.remove(vista)
                    if (arrasteDaAba?.id == id) {
                        soltarArrasteDaAba()
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }

    private fun cancelarEsperasDoArrasteDaAba() {
        esperasDoArrasteDaAba.forEach { (vista, espera) -> vista.removeCallbacks(espera) }
        esperasDoArrasteDaAba.clear()
    }

    private fun comecarArrasteDaAba(vista: View, id: Long) {
        if (arrasteDaAba != null) return
        val indice = gestao.listar().indexOfFirst { it.id == id }
        if (indice < 0) return
        val larguras = gestao.listar().indices.map { faixaDeAbas.getChildAt(it)?.width?.toFloat() ?: 0f }
        if (larguras.isEmpty() || larguras.any { it <= 0f }) return
        arrasteDaAba = ArrasteDaAbaEmCurso(id, indice, brutoXDoToqueDaAba, larguras, vista, indice)
        rolagemDasAbas.requestDisallowInterceptTouchEvent(true)
        vista.animate().scaleX(1.08f).scaleY(1.08f).setDuration(120).start()
        val elevacao = 14f * resources.displayMetrics.density
        vista.elevation = elevacao
        vista.translationZ = elevacao
    }

    private fun acompanharArrasteDaAba(rawX: Float) {
        val arraste = arrasteDaAba ?: return
        val deslocamento = rawX - arraste.inicioX
        arraste.vista.translationX = deslocamento
        val destino = DestinoDoArrasteDaAbaNaFaixa.indice(arraste.origem, deslocamento, arraste.larguras)
        if (destino == arraste.destino) return
        arraste.destino = destino
        arraste.larguras.indices.forEach { indice ->
            val filho = faixaDeAbas.getChildAt(indice) ?: return@forEach
            if (filho === arraste.vista) return@forEach
            val pixels = DeslocamentoDoVizinhoNoArrasteDaAba.pixels(
                indice,
                arraste.origem,
                destino,
                arraste.larguras[arraste.origem],
            )
            filho.animate().translationX(pixels).setDuration(160).start()
        }
    }

    private fun soltarArrasteDaAba() {
        val arraste = arrasteDaAba ?: return
        arrasteDaAba = null
        gestao.mover(arraste.id, arraste.destino)
        desenharAbas()
    }

    private class ArrasteDaAbaEmCurso(
        val id: Long,
        val origem: Int,
        val inicioX: Float,
        val larguras: List<Float>,
        val vista: View,
        var destino: Int,
    )

    private fun mostrarSessaoDeLogin(sessao: org.mozilla.geckoview.GeckoSession) {
        fecharJanelaDeLogin()
        val idDaAbaDona = gestao.idDaAbaAtiva
        idDaAbaDaJanelaDeLogin = idDaAbaDona
        val painel = findViewById<FrameLayout>(R.id.painel_da_janela_de_login)
        val visor = org.mozilla.geckoview.GeckoView(this)
        visor.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        sessao.navigationDelegate = object : org.mozilla.geckoview.GeckoSession.NavigationDelegate {
            override fun onLoadRequest(
                session: org.mozilla.geckoview.GeckoSession,
                request: org.mozilla.geckoview.GeckoSession.NavigationDelegate.LoadRequest,
            ): org.mozilla.geckoview.GeckoResult<org.mozilla.geckoview.AllowOrDeny> {
                val destino = request.uri
                val origem = paginas[idDaAbaDaJanelaDeLogin]?.webView
                if (origem != null && destinoDeLoginDeveVoltar(origem.url.orEmpty(), destino)) {
                    origem.loadUrl(destino)
                    visor.post { fecharJanelaDeLogin() }
                    return org.mozilla.geckoview.GeckoResult.fromValue(org.mozilla.geckoview.AllowOrDeny.DENY)
                }
                return org.mozilla.geckoview.GeckoResult.fromValue(org.mozilla.geckoview.AllowOrDeny.ALLOW)
            }
        }
        sessao.contentDelegate = object : org.mozilla.geckoview.GeckoSession.ContentDelegate {
            override fun onCloseRequest(session: org.mozilla.geckoview.GeckoSession) {
                runOnUiThread { fecharJanelaDeLogin() }
            }
        }
        painel.addView(visor, 0)
        visor.setSession(sessao)
        vistaDaJanelaDeLogin = visor
        sincronizarVisibilidadeDaJanelaDeLogin()
    }

    private fun sincronizarVisibilidadeDaJanelaDeLogin() {
        val painel = findViewById<FrameLayout>(R.id.painel_da_janela_de_login) ?: return
        if (vistaDaJanelaDeLogin == null) {
            painel.visibility = View.GONE
            return
        }
        val exibir = VisibilidadeDaModalDeLoginPorAbaDoNavegador.deveExibir(
            idDaAbaAtiva = gestao.idDaAbaAtiva,
            idDaAbaDona = idDaAbaDaJanelaDeLogin,
        )
        painel.visibility = if (exibir) View.VISIBLE else View.GONE
    }

    private fun destinoDeLoginDeveVoltar(urlAtual: String, urlNova: String): Boolean {
        if (!DecisaoDeNavegacaoParaOutroSite.ehPaginaDeEntregaDeLoginDoGoogle(urlAtual)) return false
        if (DecisaoDeNavegacaoParaOutroSite.ehPaginaDeEntregaDeLoginDoGoogle(urlNova)) return false
        if (DecisaoDeBloqueioDeAnuncio.urlsDoMesmoSite(urlAtual, urlNova)) return false
        return !DecisaoDeNavegacaoParaOutroSite.deveCancelar(
            bloqueioLigado = true,
            temGestoDoUsuario = false,
            ehRedirecionamento = false,
            urlAtual = urlAtual,
            urlNova = urlNova,
            lista = listaDeBloqueioDeAnuncio,
        )
    }

    private fun fecharJanelaDeLogin() {
        val visor = vistaDaJanelaDeLogin ?: return
        vistaDaJanelaDeLogin = null
        idDaAbaDaJanelaDeLogin = null
        val painel = findViewById<FrameLayout>(R.id.painel_da_janela_de_login)
        visor.releaseSession()
        painel.removeView(visor)
        painel.visibility = View.GONE
    }

    private fun abrirCapturaNativaEmNovaAba(endereco: String) {
        val aba = gestao.criarAba(
            endereco = endereco,
            titulo = getString(R.string.titulo_da_aba_da_captura_nativa_de_tela),
        )
        criarPagina(aba.id, aba.endereco)
        mostrarAba(aba.id)
    }

    private fun voltarNaPagina() {
        if (::capturaNativaDeTela.isInitialized && capturaNativaDeTela.cancelarSelecaoSeAberta()) return
        if (::painelDeExtensoes.isInitialized && painelDeExtensoes.fecharPopup()) return
        if (vistaDaJanelaDeLogin != null) {
            fecharJanelaDeLogin()
            return
        }
        if (painelDaBibliotecaDeVideos.tratarVolta()) return
        if (painelDoPlayerDaPagina.visibility == View.VISIBLE) {
            if (controladorDoScrubFinoDoPlayerDaPagina?.estaAtivo == true) {
                controladorDoScrubFinoDoPlayerDaPagina?.sairDoModo()
                return
            }
            sairDoPlayerDaPagina()
            return
        }
        if (painelGerenciador.visibility == View.VISIBLE) {
            fecharGerenciadorDeAbas()
            return
        }
        if (listaDeSugestoesEstaoVisiveis()) {
            esconderSugestoesDaBarra()
            esconderTeclado()
            return
        }
        val pagina = paginas[gestao.idDaAbaAtiva]
        if (pagina != null && pagina.webView.canGoBack()) {
            encerrarVideo(pagina.id)
            pagina.webView.goBack()
        } else if (campoEndereco.hasFocus()) {
            esconderTeclado()
        } else {
            finish()
        }
    }

    private fun avancarNaPagina() {
        val pagina = paginas[gestao.idDaAbaAtiva] ?: return
        if (!pagina.webView.canGoForward()) return
        encerrarVideo(pagina.id)
        pagina.webView.goForward()
    }

    private fun recarregarPagina() {
        val pagina = paginas[gestao.idDaAbaAtiva] ?: return
        encerrarVideo(pagina.id)
        if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(pagina.webView.url)) {
            carregarPaginaInicial(pagina)
        } else {
            pagina.webView.reload()
        }
    }

    private fun chamadaDeCapturaDeQuadro(idCitado: String, larguraMaxima: Int): String {
        return "window.chromebrothersCapturarQuadro($idCitado, $larguraMaxima)"
    }

    private fun recuperarAbaDepoisDaQuedaDoRenderizador(id: Long, vista: SuperficieDaPaginaNoMotorDoFirefox) {
        val pagina = paginas[id]
        if (pagina == null || pagina.webView !== vista) {
            abasEmRecuperacao.remove(id)
            return
        }
        val endereco = gestao.listar().find { it.id == id }?.endereco.orEmpty()
        if (idDaAbaNoPlayerDaPagina == id) fecharPainelDoPlayerDaPagina()
        encerrarVideo(id)
        inicioDaCapturaPorAba.remove(id)?.remove()
        inicioDoCampoDeTextoPorAba.remove(id)?.remove()
        pagina.liberarMiniatura()
        areaDasPaginas.removeView(vista)
        paginas.remove(id)
        try {
            vista.destroy()
        } catch (_: Exception) {
        }
        criarPagina(id, endereco)
        if (gestao.idDaAbaAtiva == id) mostrarAba(id)
        abasEmRecuperacao.remove(id)
        avisar(R.string.pagina_recarregada_depois_de_falha)
    }

    private fun mostrarMenuDoToqueLongoNoVideo(idAba: Long, idVideo: String, endereco: String) {
        MenuDoToqueLongoNoVideoDaPaginaDoNavegadorChromebrothers(
            activity = this,
            aoSalvarQuadro = { capturarQuadroDaPagina(idAba, idVideo) },
            aoColocarNaColecao = { url ->
                colocarNaColecaoPeloToque(TipoDoItemDaColecaoFlutuante.VIDEO, url, "Vídeo")
            },
        ).mostrar(endereco)
    }

    private fun colocarNaColecaoPeloToque(
        tipo: TipoDoItemDaColecaoFlutuante,
        endereco: String,
        reserva: String,
    ) {
        val aceito = when (tipo) {
            TipoDoItemDaColecaoFlutuante.IMAGEM ->
                ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.podeColocarNaColecaoComoImagem(endereco)
            TipoDoItemDaColecaoFlutuante.VIDEO ->
                endereco.startsWith("http://") || endereco.startsWith("https://")
        }
        if (!aceito) {
            avisar(R.string.arquivo_sem_endereco_http_para_a_colecao)
            return
        }
        val titulo = MontagemDaColecaoFlutuanteDoChat.tituloCurto(endereco, reserva)
        val mensagem = painelDaColecao.colocar(
            listOf(ItemDaColecaoFlutuante(tipo, endereco, titulo, "")),
        )
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
    }

    private fun abrirVideoDaColecao(item: ItemDaColecaoFlutuante) {
        val pagina = paginas[gestao.idDaAbaAtiva]
        if (pagina != null) {
            abrirPlayerComEndereco(pagina, item.endereco, 1.0, item.titulo)
            return
        }
        if (!DecisaoDeAbrirVideoNoPlayerNativo.podeAbrir(item.endereco)) {
            avisar(R.string.player_nativo_indisponivel)
            return
        }
        retornoDoPlayerDeArquivo.launch(
            Intent(this, AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro::class.java)
                .putExtra(AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_ENDERECO, item.endereco)
                .putExtra(AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_VELOCIDADE, 1.0)
                .putExtra(AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_TITULO, item.titulo),
        )
    }

    private fun capturarQuadroDaPagina(idAba: Long, idVideo: String) {
        val pagina = paginas[idAba] ?: return
        val idCitado = org.json.JSONObject.quote(idVideo)
        pagina.webView.evaluateJavascript(
            chamadaDeCapturaDeQuadro(idCitado, LimiteDaLarguraDaCapturaDeQuadroDoVideo.QUADRO_SALVO),
        ) { bruto ->
            val png = pngDoDataUrl(textoDoJavascript(bruto))
            if (png != null) {
                gravarQuadro(png)
            } else {
                copiarPixelsDaJanela(pagina.webView)
            }
        }
    }

    private fun copiarPixelsDaJanela(vista: View) {
        if (vista.width <= 0 || vista.height <= 0) {
            avisar(R.string.quadro_indisponivel)
            return
        }
        val bitmap = Bitmap.createBitmap(vista.width, vista.height, Bitmap.Config.ARGB_8888)
        val origem = IntArray(2)
        vista.getLocationInWindow(origem)
        val retangulo = android.graphics.Rect(
            origem[0],
            origem[1],
            origem[0] + vista.width,
            origem[1] + vista.height,
        )
        PixelCopy.request(window, retangulo, bitmap, { resultado ->
            if (resultado == PixelCopy.SUCCESS) {
                val saida = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)
                gravarQuadro(saida.toByteArray())
            } else {
                avisar(R.string.quadro_indisponivel)
            }
            bitmap.recycle()
        }, Handler(Looper.getMainLooper()))
    }

    private fun definirCapturaDeMidiaParaDownload(ligado: Boolean) {
        if (ligado == preferencias.capturarMidiaParaDownload) return
        preferencias.capturarMidiaParaDownload = ligado
        publicarCapturaDeMidia(ligado)
        avisar(if (ligado) R.string.captura_de_midia_ligada else R.string.captura_de_midia_desligada)
    }

    private fun publicarCapturaDeMidia(ligado: Boolean) {
        interruptorDeCapturaDeMidia.isChecked = ligado
        if (::interruptorDeCapturaDeMidiaNoPlayer.isInitialized) {
            interruptorDeCapturaDeMidiaNoPlayer.isChecked = ligado
        }
        if (!ligado) paginas.values.forEach { it.limparPedidosDeMidia() }
        paginas.values.forEach { pagina ->
            publicarInicioDaCaptura(pagina)
            pagina.webView.publicarScriptEmTodosOsQuadros(
                ScriptInjetadoParaDetectarVideoHtmlNaPagina.inicio(ligado),
            )
        }
        atualizarBotaoDeBaixarMidia()
    }

    private fun atualizarBotaoDeBaixarMidia() {
        val ligada = preferencias.capturarMidiaParaDownload
        val pagina = paginas[gestao.idDaAbaAtiva]
        val temMidia = ligada && pagina != null &&
            EscolhaDoPedidoDeMidiaParaBaixar.listar(pagina.pedidosDeMidiaObservados()).isNotEmpty()
        // Com o player sobreposto aberto o botão redondo some: o player tem o próprio botão de baixar.
        botaoBaixarMidia.visibility = if (temMidia && !playerDaPaginaAberto()) View.VISIBLE else View.GONE
        findViewById<ImageButton>(R.id.botao_baixar_midia_do_player_da_pagina).visibility =
            if (ligada) View.VISIBLE else View.GONE
    }

    /** "Enviar para a Galeria" de um toque: mede, escolhe a melhor opção de vídeo e baixa em Movies/ChromeBrothers. */
    private fun enviarMelhorMidiaDaAbaAtualParaAGaleria() {
        oferecerDownloadDaMidiaDaAbaAtual { pagina, opcoes ->
            val candidatos = com.chromebrothers.navegador.regras.EscolhaDoPedidoDeMidiaParaBaixar.candidatosParaAGaleria(opcoes)
            val melhor = candidatos.firstOrNull()
            if (melhor == null) {
                avisar(R.string.nenhum_video_para_enviar_a_galeria)
            } else {
                // Se a melhor opção falhar, o serviço tenta as seguintes.
                iniciarDownloadDaMidia(
                    pagina,
                    melhor,
                    com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia.GALERIA,
                    alternativas = candidatos.drop(1).take(4),
                )
            }
        }
    }

    private fun oferecerDownloadDaMidiaDaAbaAtual(
        aoTerOpcoes: (PaginaDeUmaAbaDoNavegadorChromebrothers, List<com.chromebrothers.navegador.regras.PedidoDeMidiaObservado>) -> Unit =
            { pagina, opcoes -> mostrarEscolhaDeMidia(pagina, opcoes) },
    ) {
        val pagina = paginas[gestao.idDaAbaAtiva] ?: return
        val opcoes = EscolhaDoPedidoDeMidiaParaBaixar.listar(pagina.pedidosDeMidiaObservados())
        if (opcoes.isEmpty()) {
            avisar(
                if (preferencias.capturarMidiaParaDownload) {
                    R.string.midia_ainda_nao_apareceu
                } else {
                    R.string.captura_de_midia_desligada
                },
            )
            return
        }
        val semTamanho = opcoes.filter {
            it.tipo == TipoDePedidoDeMidiaParaDownload.ARQUIVO && it.tamanho < 0
        }.take(16)
        if (semTamanho.isEmpty()) {
            aoTerOpcoes(pagina, opcoes)
            return
        }
        val enderecoDaPagina = pagina.webView.url.orEmpty()
        val agenteDaPagina = pagina.webView.settings.userAgentString.orEmpty()
        val paraMedir = semTamanho.map { pedido ->
            Triple(
                pedido.endereco,
                pagina.refererDoPedidoDeMidia(pedido.endereco).ifBlank { enderecoDaPagina },
                pagina.agenteDoPedidoDeMidia(pedido.endereco).ifBlank { agenteDaPagina },
            )
        }
        avisar(R.string.medindo_tamanho_da_midia)
        Thread {
            val pool = java.util.concurrent.Executors.newFixedThreadPool(4)
            try {
                val tarefas = paraMedir.map { (endereco, referer, agente) ->
                    java.util.concurrent.Callable {
                        endereco to MedidaDoTamanhoDoPedidoDeMidiaDetectadaNaPagina.medir(endereco, referer, agente)
                    }
                }
                pool.invokeAll(tarefas).forEach { futuro ->
                    val (endereco, tamanho) = try {
                        futuro.get()
                    } catch (_: Exception) {
                        return@forEach
                    }
                    pagina.atualizarTamanhoDoPedidoDeMidia(endereco, tamanho)
                }
            } finally {
                pool.shutdownNow()
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                val medidos = EscolhaDoPedidoDeMidiaParaBaixar.listar(pagina.pedidosDeMidiaObservados())
                if (medidos.isEmpty()) {
                    avisar(R.string.midia_ainda_nao_apareceu)
                } else {
                    aoTerOpcoes(pagina, medidos)
                }
            }
        }.start()
    }

    /** Resolução/duração/som de cada opção (guardados por endereço: reabrir a lista é instantâneo). */
    private val sondagemDosDetalhesDaMidia by lazy { SondagemDosDetalhesDaMidiaParaBaixar() }

    /** Lista do toque longo: lê resolução, duração e som de cada opção e mostra da maior para a menor. */
    private fun mostrarEscolhaDeMidia(
        pagina: PaginaDeUmaAbaDoNavegadorChromebrothers,
        opcoes: List<com.chromebrothers.navegador.regras.PedidoDeMidiaObservado>,
    ) {
        avisar(R.string.lendo_detalhes_dos_videos)
        val enderecoDaPagina = pagina.webView.url.orEmpty()
        val agenteDaPagina = pagina.webView.settings.userAgentString.orEmpty()
        Thread {
            val detalhes = sondagemDosDetalhesDaMidia.sondar(
                opcoes,
                referer = { endereco -> pagina.refererDoPedidoDeMidia(endereco).ifBlank { enderecoDaPagina } },
                agente = { endereco -> pagina.agenteDoPedidoDeMidia(endereco).ifBlank { agenteDaPagina } },
            )
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                val texto = com.chromebrothers.navegador.regras.TextoDaOpcaoDeMidiaParaBaixar
                val temAudioSeparado = com.chromebrothers.navegador.regras.EscolhaDoPedidoDeMidiaParaBaixar
                    .melhorAudio(pagina.pedidosDeMidiaObservados()) != null
                mostrarCaixaDeEscolhaSimplesDoNavegador(
                    titulo = getString(R.string.escolher_midia_para_baixar),
                    opcoes = texto.ordenar(opcoes, detalhes).map { texto.texto(it, detalhes[it.endereco], temAudioSeparado) to it },
                    indiceSelecionado = -1,
                ) { pedido ->
                    iniciarDownloadDaMidia(pagina, pedido)
                }
            }
        }.start()
    }

    private fun mostrarEscolhaDaAparenciaDoNavegador() {
        val opcoes = listOf(
            getString(R.string.aparencia_clara_do_navegador) to false,
            getString(R.string.aparencia_escura_do_navegador) to true,
        )
        mostrarCaixaDeEscolhaSimplesDoNavegador(
            titulo = getString(R.string.aparencia_do_navegador_no_menu),
            opcoes = opcoes,
            indiceSelecionado = if (preferencias.aparenciaEscura) 1 else 0,
        ) { escura ->
            definirAparencia(escura)
            avisar(
                if (escura) R.string.aparencia_escura_aplicada_do_navegador
                else R.string.aparencia_clara_aplicada_do_navegador,
            )
        }
    }

    private fun mostrarEscolhaDoMotorDeBuscaDoNavegador() {
        val motores = MotorDeBuscaDaBarraDeEndereco.entries
        mostrarCaixaDeEscolhaSimplesDoNavegador(
            titulo = getString(R.string.motor_de_busca_do_navegador_no_menu),
            opcoes = motores.map { it.nome to it },
            indiceSelecionado = motores.indexOf(preferencias.motor).coerceAtLeast(0),
        ) { motor ->
            preferencias.motor = motor
            recarregarPaginasIniciais()
            Toast.makeText(
                this,
                getString(R.string.motor_de_busca_aplicado_do_navegador, motor.nome),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun mostrarEscolhaDaApresentacaoDosSitesDoNavegador() {
        val modos = listOf(
            ModoDeApresentacaoDoSite.TABLET,
            ModoDeApresentacaoDoSite.CELULAR,
            ModoDeApresentacaoDoSite.COMPUTADOR,
        )
        val rotulos = listOf(
            getString(R.string.apresentacao_tablet_do_navegador),
            getString(R.string.apresentacao_celular_do_navegador),
            getString(R.string.apresentacao_computador_do_navegador),
        )
        mostrarCaixaDeEscolhaSimplesDoNavegador(
            titulo = getString(R.string.apresentacao_dos_sites_do_navegador_no_menu),
            opcoes = rotulos.zip(modos),
            indiceSelecionado = modos.indexOf(preferencias.modoDeSite).coerceAtLeast(0),
        ) { modo ->
            definirModoGlobal(ModoDeApresentacaoDoSite.idDe(modo))
            avisar(mensagemDoModo(modo))
        }
    }

    private fun mostrarEscolhaDoTecladoNesteSite() {
        val url = paginas[gestao.idDaAbaAtiva]?.webView?.url.orEmpty()
        val host = ComportamentoDoTecladoPorSiteDoNavegador.hospedeiro(url)
        if (host.isBlank() || EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) {
            avisar(R.string.teclado_neste_site_sem_pagina)
            return
        }
        val modos = listOf(
            ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO,
            ModoDoTecladoSobreAPaginaDoNavegador.SEMPRE_DESLOCAR,
            ModoDoTecladoSobreAPaginaDoNavegador.NAO_DESLOCAR,
        )
        val rotulos = listOf(
            getString(R.string.modo_do_teclado_automatico_do_navegador),
            getString(R.string.modo_do_teclado_sempre_deslocar_do_navegador),
            getString(R.string.modo_do_teclado_nao_deslocar_do_navegador),
        )
        val atual = preferencias.modoDoTecladoParaEndereco(url)
        mostrarCaixaDeEscolhaSimplesDoNavegador(
            titulo = getString(R.string.titulo_do_teclado_neste_site_do_navegador, host),
            opcoes = rotulos.zip(modos),
            indiceSelecionado = modos.indexOf(atual).coerceAtLeast(0),
        ) { modo ->
            preferencias.gravarModoDoTecladoParaEndereco(url, modo)
            val aviso = when (modo) {
                ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO ->
                    R.string.modo_do_teclado_automatico_aplicado_neste_site
                ModoDoTecladoSobreAPaginaDoNavegador.SEMPRE_DESLOCAR ->
                    R.string.modo_do_teclado_sempre_deslocar_aplicado_neste_site
                ModoDoTecladoSobreAPaginaDoNavegador.NAO_DESLOCAR ->
                    R.string.modo_do_teclado_nao_deslocar_aplicado_neste_site
            }
            Toast.makeText(this, getString(aviso, host), Toast.LENGTH_SHORT).show()
            ViewCompat.requestApplyInsets(findViewById(R.id.raiz_do_navegador))
        }
    }

    private fun <T> mostrarCaixaDeEscolhaSimplesDoNavegador(
        titulo: String,
        opcoes: List<Pair<String, T>>,
        indiceSelecionado: Int,
        aoEscolher: (T) -> Unit,
    ) = CaixaDeEscolhaDoNavegador.mostrar(this, titulo, opcoes, indiceSelecionado, aoEscolher)

    private fun receberDownloadPedidoPelaPagina(
        id: Long,
        endereco: String,
        agente: String,
        disposicao: String,
        mime: String,
    ) {
        val pagina = paginas[id] ?: return
        val nome = PedidoDeDownloadDaPagina.nome(disposicao, endereco, mime)
        when (PedidoDeDownloadDaPagina.origem(endereco)) {
            OrigemDoDownloadPedidoPelaPagina.REDE -> {
                avisar(R.string.download_de_midia_iniciado)
                val referer = pagina.webView.url.orEmpty()
                val agenteFinal = agente.ifBlank { pagina.webView.settings.userAgentString.orEmpty() }
                val cookie = try {
                    android.webkit.CookieManager.getInstance().getCookie(endereco)
                } catch (_: Exception) {
                    null
                }
                Thread {
                    val ok = GravacaoDoArquivoPedidoPelaPagina(this).baixarDaRede(
                        endereco,
                        nome,
                        mime,
                        referer,
                        agenteFinal,
                        cookie,
                    )
                    runOnUiThread {
                        avisar(if (ok) R.string.download_da_pagina_salvo else R.string.download_da_pagina_falhou)
                    }
                }.start()
            }
            OrigemDoDownloadPedidoPelaPagina.MEMORIA -> {
                val bytes = ConteudoCodificadoNoEnderecoDaPagina.ler(endereco)
                if (bytes != null) {
                    gravarBytesDaPagina(nome, mime, bytes)
                } else if (endereco.startsWith("blob:", ignoreCase = true)) {
                    avisar(R.string.download_de_midia_iniciado)
                    lerBlobNaPagina(pagina.webView, endereco, nome)
                } else {
                    avisar(R.string.download_da_pagina_falhou)
                }
            }
            OrigemDoDownloadPedidoPelaPagina.IGNORAR -> avisar(R.string.download_da_pagina_falhou)
        }
    }

    private fun gravarRespostaExternaDaPagina(resposta: org.mozilla.geckoview.WebResponse) {
        if (EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.eh(resposta.uri)) {
            val corpo = resposta.body
            if (corpo != null) instalarExtensaoPelosBytes(resposta.uri, corpo)
            else instalarExtensaoPelaLoja(resposta.uri)
            return
        }
        val mime = cabecalhoDaResposta(resposta, "content-type")
        val nome = PedidoDeDownloadDaPagina.nome(
            cabecalhoDaResposta(resposta, "content-disposition"),
            resposta.uri,
            mime,
        )
        val corpo = resposta.body
        if (corpo != null) {
            avisar(R.string.download_de_midia_iniciado)
            Thread {
                val bytes = runCatching { corpo.use { it.readBytes() } }.getOrNull()
                runOnUiThread {
                    if (isFinishing || isDestroyed) return@runOnUiThread
                    if (bytes == null) avisar(R.string.download_da_pagina_falhou)
                    else gravarBytesDaPagina(nome, mime, bytes)
                }
            }.start()
            return
        }
        if (resposta.uri.startsWith("blob:", ignoreCase = true)) {
            val pagina = paginas[gestao.idDaAbaAtiva]?.webView ?: run {
                avisar(R.string.download_da_pagina_falhou)
                return
            }
            avisar(R.string.download_de_midia_iniciado)
            lerBlobNaPagina(pagina, resposta.uri, nome)
            return
        }
        avisar(R.string.download_da_pagina_falhou)
    }

    private fun cabecalhoDaResposta(resposta: org.mozilla.geckoview.WebResponse, nome: String): String {
        return resposta.headers.entries.firstOrNull { it.key.equals(nome, ignoreCase = true) }?.value.orEmpty()
    }

    private fun lerBlobNaPagina(vista: SuperficieDaPaginaNoMotorDoFirefox, endereco: String, nome: String) {
        val script = """
            fetch(${JSONObject.quote(endereco)}).then(function(resposta) { return resposta.blob(); }).then(function(blob) {
              return new Promise(function(resolver) {
                var leitor = new FileReader();
                leitor.onloadend = function() { resolver(leitor.result || ""); };
                leitor.onerror = function() { resolver(""); };
                leitor.readAsDataURL(blob);
              });
            }).catch(function() { return ""; })
        """.trimIndent()
        vista.evaluateJavascript(script) { bruto ->
            val dados = textoDoJavascript(bruto)
            if (dados.isNullOrBlank() || dados == "null") avisar(R.string.download_da_pagina_falhou)
            else gravarConteudoEmbutidoDaPagina(nome, dados)
        }
    }

    private fun gravarConteudoEmbutidoDaPagina(nome: String, dados: String) {
        val bytes = ConteudoCodificadoNoEnderecoDaPagina.ler(dados)
        if (bytes == null) {
            avisar(R.string.download_da_pagina_falhou)
            return
        }
        val mime = dados.substringAfter("data:", "").substringBefore(',', "").substringBefore(';')
        gravarBytesDaPagina(nome, mime, bytes)
    }

    private fun gravarBytesDaPagina(nome: String, mime: String, bytes: ByteArray) {
        Thread {
            val ok = GravacaoDoArquivoPedidoPelaPagina(this).gravarBytes(nome, mime, bytes)
            runOnUiThread {
                avisar(if (ok) R.string.download_da_pagina_salvo else R.string.download_da_pagina_falhou)
            }
        }.start()
    }

    private fun iniciarDownloadDaMidia(
        pagina: PaginaDeUmaAbaDoNavegadorChromebrothers,
        pedido: com.chromebrothers.navegador.regras.PedidoDeMidiaObservado,
        destino: com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia =
            com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia.DOWNLOADS,
        alternativas: List<com.chromebrothers.navegador.regras.PedidoDeMidiaObservado> = emptyList(),
    ) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            pedirPermissaoDeNotificacaoParaODownload.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val titulo = gestao.ativa()?.titulo?.takeIf {
            it.isNotBlank() && it != getString(R.string.nova_aba)
        } ?: "video"
        val intent = Intent(this, ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador::class.java).apply {
            putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_ENDERECO, pedido.endereco)
            putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_TIPO, pedido.tipo.name)
            putExtra(
                ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_REFERER,
                pagina.refererDoPedidoDeMidia(pedido.endereco).ifBlank { pagina.webView.url.orEmpty() },
            )
            putExtra(
                ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_AGENTE,
                pagina.agenteDoPedidoDeMidia(pedido.endereco).ifBlank { pagina.webView.settings.userAgentString },
            )
            putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_TITULO, titulo)
            putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_DESTINO, destino.name)
            // Vídeo e áudio separados (VK/Boosty): manda o melhor áudio para juntar se o vídeo vier mudo.
            val regraDeEscolha = com.chromebrothers.navegador.regras.EscolhaDoPedidoDeMidiaParaBaixar
            if (!regraDeEscolha.pareceAudio(pedido)) {
                regraDeEscolha.melhorAudio(pagina.pedidosDeMidiaObservados())?.let { audio ->
                    putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_AUDIO_SEPARADO, audio.endereco)
                }
            }
            if (alternativas.isNotEmpty()) {
                putExtra(
                    ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_ALTERNATIVAS_ENDERECOS,
                    alternativas.map { it.endereco }.toTypedArray(),
                )
                putExtra(
                    ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_ALTERNATIVAS_TIPOS,
                    alternativas.map { it.tipo.name }.toTypedArray(),
                )
            }
            if (pedido.tipo == TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH) {
                val outros = pagina.pedidosDeMidiaObservados()
                    .filter { it.tipo == TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH && it.endereco != pedido.endereco }
                    .map { it.endereco }
                    .distinct()
                    .take(4)
                putExtra(
                    ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_OUTROS,
                    outros.toTypedArray(),
                )
            }
        }
        ContextCompat.startForegroundService(this, intent)
        avisar(
            if (destino == com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia.GALERIA) {
                R.string.envio_do_video_para_a_galeria_iniciado
            } else {
                R.string.download_de_midia_iniciado
            },
        )
    }

    private fun baixarMarcadosDaColecao(itens: List<ItemDaColecaoFlutuante>) {
        if (itens.isEmpty()) {
            avisar(R.string.marque_algum_item_da_colecao)
            return
        }
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            pedirPermissaoDeNotificacaoParaODownload.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val pagina = paginas[gestao.idDaAbaAtiva]
        val referer = pagina?.webView?.url.orEmpty()
        val agente = pagina?.webView?.settings?.userAgentString.orEmpty()
        val deMidia = itens.filter { item ->
            val tipo = ClassificacaoDoPedidoDeMidiaParaDownload.classificar(item.endereco)
            tipo == TipoDePedidoDeMidiaParaDownload.ARQUIVO ||
                tipo == TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS ||
                tipo == TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH
        }
        val deArquivo = itens.filter { it !in deMidia }
        deMidia.forEach { item ->
            val tipo = ClassificacaoDoPedidoDeMidiaParaDownload.classificar(item.endereco)
            val intent = Intent(this, ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador::class.java).apply {
                putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_ENDERECO, item.endereco)
                putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_TIPO, tipo.name)
                putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_REFERER, referer)
                putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_AGENTE, agente)
                putExtra(ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador.EXTRA_TITULO, item.titulo)
            }
            ContextCompat.startForegroundService(this, intent)
        }
        if (deArquivo.isNotEmpty()) {
            Thread {
                var falhas = 0
                deArquivo.forEach { item ->
                    val mime = mimeDoArquivoDaColecao(item.endereco)
                    val nome = PedidoDeDownloadDaPagina.nome("", item.endereco, mime)
                    val gravacao = GravacaoDoArquivoPedidoPelaPagina(this)
                    val ok = when (
                        ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.classificar(item.endereco)
                    ) {
                        FonteDoEnderecoDaImagemDoToqueLongo.ARQUIVO_LOCAL -> {
                            val bytes = CopiaDeImagemDaPaginaParaABibliotecaDoNavegador
                                .bytesDeArquivoLocal(item.endereco)
                            bytes != null && gravacao.gravarBytes(nome, mime, bytes)
                        }
                        else -> {
                            val cookie = try {
                                CookieManager.getInstance().getCookie(item.endereco)
                            } catch (_: Exception) {
                                null
                            }
                            gravacao.baixarDaRede(
                                item.endereco,
                                nome,
                                mime,
                                referer,
                                agente,
                                cookie,
                            )
                        }
                    }
                    if (!ok) falhas++
                }
                runOnUiThread {
                    if (!isDestroyed && falhas > 0) avisar(R.string.arquivos_da_colecao_incompletos)
                }
            }.start()
        }
        avisar(R.string.baixando_os_marcados_da_colecao)
    }

    private fun mimeDoArquivoDaColecao(endereco: String): String {
        val caminho = endereco.substringBefore('?').substringBefore('#').lowercase()
        return when {
            caminho.endsWith(".png") -> "image/png"
            caminho.endsWith(".webp") -> "image/webp"
            caminho.endsWith(".gif") -> "image/gif"
            caminho.endsWith(".jpg") || caminho.endsWith(".jpeg") -> "image/jpeg"
            caminho.endsWith(".mp4") -> "video/mp4"
            caminho.endsWith(".webm") -> "video/webm"
            else -> "application/octet-stream"
        }
    }

    private fun abrirPlayerNativo() {
        val pagina = paginas[gestao.idDaAbaAtiva] ?: return
        val video = pagina.videoAtual ?: return
        when (DecisaoDeDestinoDoToqueNoBotaoDoVideo.destino(video.endereco)) {
            DestinoDoToqueNoBotaoDoVideo.PLAYER_DE_ARQUIVO ->
                abrirPlayerComEndereco(pagina, video.endereco, video.velocidade)
            DestinoDoToqueNoBotaoDoVideo.PLAYER_DA_PAGINA ->
                entrarNoPlayerDaPagina(pagina, video.id, video.velocidade)
        }
    }

    private fun prepararControlesDoPlayerDaPagina() {
        val lista = findViewById<LinearLayout>(R.id.lista_de_velocidades_do_player_da_pagina)
        val margem = (8 * resources.displayMetrics.density).toInt()
        AjusteDeVelocidadeDeReproducaoDoVideo.opcoes.forEach { taxa ->
            val botao = Button(this)
            botao.text = if (taxa == taxa.toInt().toDouble()) "${taxa.toInt()}×" else "${taxa}×"
            botao.setOnClickListener {
                velocidadeDoPlayerDaPagina = taxa
                atualizarVelocidadeDoPlayerDaPagina()
                comandarPlayerDaPagina("chromebrothersDefinirVelocidade", taxa.toString())
                mostrarControlesDoPlayerPorUmInstante()
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            params.marginEnd = margem
            lista.addView(botao, params)
            botoesDeVelocidadeDoPlayerDaPagina += taxa to botao
        }
        atualizarVelocidadeDoPlayerDaPagina()
        val painelScrub = findViewById<View>(R.id.inclusao_da_faixa_de_scrub_fino_do_player_da_pagina)
        val faixaScrub = findViewById<LinearLayout>(
            R.id.faixa_de_miniaturas_do_scrub_fino_por_toque_longo_do_player_de_video,
        )
        val rotuloScrub = findViewById<TextView>(
            R.id.texto_do_tempo_do_scrub_fino_por_toque_longo_do_player_de_video,
        )
        val extratorDaFilmstripDaPagina = ExtratorDeFilmstripDoScrubFinoPeloVideoDaPaginaDoNavegador(
            seekParaSegundos = { segundos ->
                comandarPlayerDaPagina("chromebrothersDefinirPosicao", segundos.toString())
            },
            lerPosicaoSegundos = { aoReceber ->
                val idVideo = idDoVideoNoPlayerDaPagina
                val pagina = idDaAbaNoPlayerDaPagina?.let { paginas[it] }
                if (idVideo == null || pagina == null) {
                    aoReceber(null)
                    return@ExtratorDeFilmstripDoScrubFinoPeloVideoDaPaginaDoNavegador
                }
                val idCitado = JSONObject.quote(idVideo)
                pagina.webView.evaluateJavascript("window.chromebrothersLerEstado($idCitado)") { bruto ->
                    val estado = LeituraDoEstadoDoVideoControladoNaPagina.ler(textoDoJavascript(bruto))
                    aoReceber(estado?.posicaoSegundos)
                }
            },
            capturarBitmap = { aoReceber ->
                capturarBitmapDaFilmstripDoPlayerDaPagina(aoReceber)
            },
        )
        val provedorMmrDaPagina = ProvedorDeQuadrosDoVideoParaScrubFinoDoPlayerDeVideo(
            activity = this,
            obterEndereco = {
                idDaAbaNoPlayerDaPagina?.let { paginas[it]?.videoAtual?.endereco }.orEmpty()
            },
            obterReferer = {
                idDaAbaNoPlayerDaPagina?.let { paginas[it]?.webView?.url }.orEmpty()
            },
            obterAgente = {
                idDaAbaNoPlayerDaPagina?.let { paginas[it]?.webView?.settings?.userAgentString }.orEmpty()
            },
        )
        var ultimaCapturaOportunistaMs = 0L
        val controladorScrub = ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo(
            activity = this,
            painel = painelScrub,
            faixa = faixaScrub,
            rotuloDoTempo = rotuloScrub,
            obterPosicaoMs = {
                val duracao = barraDePosicaoDoPlayerDaPagina.tag as? Double ?: 0.0
                if (duracao <= 0.0) 0L
                else ((barraDePosicaoDoPlayerDaPagina.progress / 1000.0) * duracao * 1000.0).toLong()
            },
            obterDuracaoMs = {
                (((barraDePosicaoDoPlayerDaPagina.tag as? Double) ?: 0.0) * 1000.0).toLong()
            },
            seekParaMs = { ms ->
                val duracao = barraDePosicaoDoPlayerDaPagina.tag as? Double ?: return@ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo
                if (duracao <= 0.0) return@ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo
                val segundos = (ms / 1000.0).coerceIn(0.0, duracao)
                barraDePosicaoDoPlayerDaPagina.progress =
                    ((segundos / duracao) * 1000.0).toInt().coerceIn(0, 1000)
                comandarPlayerDaPagina("chromebrothersDefinirPosicao", segundos.toString())
            },
            estavaTocando = { !videoDoPlayerEstaPausado && usuarioQuerReproduzir },
            pausar = { definirReproducaoDoPlayer(tocar = false) },
            retomar = { definirReproducaoDoPlayer(tocar = true) },
            aoEntrar = {
                barraSuperiorDoPlayerDaPagina.visibility = View.GONE
                barraInferiorDoPlayerDaPagina.visibility = View.GONE
                controlesCentraisDoPlayerDaPagina.visibility = View.GONE
                relogioDoPlayerDaPagina.removeCallbacks(esconderControlesDoPlayerDaPagina)
                // Só MMR offline — NÃO varrer o <video> com seek (isso quebrava stream/HLS).
                provedorMmrDaPagina.abrir()
            },
            aoSair = {
                extratorDaFilmstripDaPagina.cancelar()
                provedorMmrDaPagina.fechar()
                mostrarControlesDoPlayerPorUmInstante()
            },
            preencherFilmstrip = { tempos, _, _, indiceCentral, aindaAtivo, aoQuadro, aoFim ->
                // Tentativa leve via MMR (MP4). Stream fica a cargo da captura oportunista no arraste.
                provedorMmrDaPagina.preencher(
                    tempos = tempos,
                    aindaAtivo = aindaAtivo,
                    aoQuadro = aoQuadro,
                    aoFim = aoFim,
                    indiceCentral = indiceCentral,
                )
            },
            cancelarPreenchimentoDaFilmstrip = {
                extratorDaFilmstripDaPagina.cancelar()
                provedorMmrDaPagina.cancelar()
            },
            capturarQuadroAvulso = { tempoMs, _, _, aoPronto ->
                val scrub = controladorDoScrubFinoDoPlayerDaPagina
                val atual = scrub?.posicaoAtualMs ?: tempoMs
                // Nunca seeka o vídeo da página só por miniatura — só captura se já estamos perto.
                if (kotlin.math.abs(atual - tempoMs) <= 700L) {
                    extratorDaFilmstripDaPagina.capturarAgora(aoPronto)
                } else {
                    aoPronto(null)
                }
            },
            aoAposSeekDoScrub = { _ ->
                val scrub = controladorDoScrubFinoDoPlayerDaPagina ?: return@ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo
                if (!scrub.precisaQuadroNaPosicaoAtual()) return@ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo
                val agora = android.os.SystemClock.uptimeMillis()
                if (agora - ultimaCapturaOportunistaMs < 180L) return@ControladorDoScrubFinoPorToqueLongoDoPlayerDeVideo
                ultimaCapturaOportunistaMs = agora
                // Espera o Gecko pintar o seek antes de capturar o quadro.
                relogioDoPlayerDaPagina.postDelayed({
                    if (controladorDoScrubFinoDoPlayerDaPagina?.estaAtivo != true) return@postDelayed
                    if (controladorDoScrubFinoDoPlayerDaPagina?.precisaQuadroNaPosicaoAtual() != true) return@postDelayed
                    extratorDaFilmstripDaPagina.capturarAgora { bitmap ->
                        controladorDoScrubFinoDoPlayerDaPagina?.oferecerQuadroNaPosicaoAtual(bitmap)
                    }
                }, 70L)
            },
            obterChaveDoVideo = {
                val pagina = idDaAbaNoPlayerDaPagina?.let { paginas[it] }
                pagina?.videoAtual?.endereco?.takeIf { it.isNotBlank() }
                    ?: idDoVideoNoPlayerDaPagina.orEmpty()
            },
        )
        controladorDoScrubFinoDoPlayerDaPagina = controladorScrub
        controladorScrub.ligarNaSuperficie(
            superficie = superficieDoPlayerDaPagina,
            detectorBase = object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    alternarControlesDoPlayerDaPagina()
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (controladorDoScrubFinoDoPlayerDaPagina?.estaAtivo == true) return true
                    val segundos = ToqueDuploNoPlayerDeVideo.segundos(
                        e.x,
                        superficieDoPlayerDaPagina.width.toFloat(),
                        incrementoDoPlayerDaPagina,
                    )
                    if (segundos != 0) deslocarPlayerDaPagina(segundos)
                    return true
                }
            },
        )
        botaoPausarPlayerDaPagina.setOnClickListener {
            definirReproducaoDoPlayer(tocar = videoDoPlayerEstaPausado)
        }
        findViewById<ImageButton>(R.id.botao_retroceder_dez_segundos_do_player_da_pagina).setOnClickListener {
            deslocarPlayerDaPagina(-incrementoDoPlayerDaPagina)
        }
        findViewById<ImageButton>(R.id.botao_avancar_dez_segundos_do_player_da_pagina).setOnClickListener {
            deslocarPlayerDaPagina(incrementoDoPlayerDaPagina)
        }
        findViewById<ImageButton>(R.id.botao_fechar_player_da_pagina).setOnClickListener {
            sairDoPlayerDaPagina()
        }
        findViewById<ImageButton>(R.id.botao_salvar_quadro_do_player_da_pagina).setOnClickListener {
            mostrarControlesDoPlayerPorUmInstante()
            capturarQuadroDoPlayerDaPagina(salvarNaGaleria = true)
        }
        findViewById<ImageButton>(R.id.botao_baixar_midia_do_player_da_pagina).apply {
            // Igual aos outros botões de download: toque = Galeria; segurar = lista (Downloads).
            contentDescription = getString(R.string.enviar_video_para_a_galeria)
            tooltipText = getString(R.string.dica_do_botao_enviar_video_para_a_galeria)
            setOnClickListener {
                mostrarControlesDoPlayerPorUmInstante()
                enviarMelhorMidiaDaAbaAtualParaAGaleria()
            }
            setOnLongClickListener {
                mostrarControlesDoPlayerPorUmInstante()
                oferecerDownloadDaMidiaDaAbaAtual()
                true
            }
        }
        interruptorDeCapturaDeMidiaNoPlayer =
            findViewById(R.id.interruptor_de_captura_de_midia_no_player_da_pagina)
        interruptorDeCapturaDeMidiaNoPlayer.isChecked = preferencias.capturarMidiaParaDownload
        interruptorDeCapturaDeMidiaNoPlayer.setOnCheckedChangeListener { _, ligado ->
            definirCapturaDeMidiaParaDownload(ligado)
            if (painelDoPlayerDaPagina.visibility == View.VISIBLE) {
                mostrarControlesDoPlayerPorUmInstante()
            }
        }
        findViewById<ImageButton>(R.id.botao_copiar_quadro_do_player_da_pagina).setOnClickListener {
            mostrarControlesDoPlayerPorUmInstante()
            capturarQuadroDoPlayerDaPagina(salvarNaGaleria = false)
        }
        findViewById<ImageButton>(R.id.botao_configurar_incremento_do_player_da_pagina).setOnClickListener {
            listaDeIncrementosDoPlayerDaPagina.visibility =
                if (listaDeIncrementosDoPlayerDaPagina.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            mostrarControlesDoPlayerPorUmInstante()
        }
        montarIncrementosDoPlayerDaPagina()
        barraDePosicaoDoPlayerDaPagina.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                usuarioArrastaPosicaoDoPlayer = true
                mostrarControlesDoPlayerPorUmInstante()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                usuarioArrastaPosicaoDoPlayer = false
                val duracao = barraDePosicaoDoPlayerDaPagina.tag as? Double ?: return
                if (duracao <= 0.0) return
                val segundos = duracao * barraDePosicaoDoPlayerDaPagina.progress / 1000.0
                comandarPlayerDaPagina("chromebrothersDefinirPosicao", segundos.toString())
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = Unit
        })
    }

    private fun entrarNoPlayerDaPagina(
        pagina: PaginaDeUmaAbaDoNavegadorChromebrothers,
        idVideo: String,
        velocidade: Double,
    ) {
        idDaAbaNoPlayerDaPagina = pagina.id
        idDoVideoNoPlayerDaPagina = idVideo
        velocidadeDoPlayerDaPagina = AjusteDeVelocidadeDeReproducaoDoVideo.ajustar(velocidade)
        atualizarVelocidadeDoPlayerDaPagina()
        val idCitado = JSONObject.quote(idVideo)
        pagina.webView.evaluateJavascript("window.chromebrothersEntrarNoPlayer($idCitado)") { bruto ->
            if (textoDoJavascript(bruto) == "ok") {
                abrirPainelDoPlayerDaPagina()
                registrarVideoDaPaginaNaBiblioteca(pagina)
                agendarMiniaturaDoPlayerDaPagina()
                comandarPlayerDaPagina("chromebrothersDefinirVelocidade", velocidadeDoPlayerDaPagina.toString())
            } else {
                idDaAbaNoPlayerDaPagina = null
                idDoVideoNoPlayerDaPagina = null
                avisar(R.string.player_da_pagina_indisponivel)
            }
        }
    }

    private fun abrirPainelDoPlayerDaPagina() {
        faixaSuperior.visibility = View.GONE
        barraDeNavegacao.visibility = View.GONE
        progresso.visibility = View.GONE
        painelDoPlayerDaPagina.visibility = View.VISIBLE
        botaoFlutuante.visibility = View.GONE
        // Como o player nativo em tela cheia: some status, teclas do Android e o botão redondo de baixar
        // (o player tem o próprio botão de baixar nos controles).
        congelarMargensParaOPlayerDaPagina(true)
        atualizarBotaoDeBaixarMidia()
        usuarioQuerReproduzir = true
        videoDoPlayerEstaPausado = false
        mostrarControlesDoPlayerPorUmInstante()
        relogioDoPlayerDaPagina.removeCallbacks(pulsoDoPlayerDaPagina)
        relogioDoPlayerDaPagina.post(pulsoDoPlayerDaPagina)
    }

    private fun mostrarControlesDoPlayerPorUmInstante() {
        barraSuperiorDoPlayerDaPagina.visibility = View.VISIBLE
        barraInferiorDoPlayerDaPagina.visibility = View.VISIBLE
        controlesCentraisDoPlayerDaPagina.visibility = View.VISIBLE
        val espera = if (listaDeIncrementosDoPlayerDaPagina.visibility == View.VISIBLE) 5000L else 3000L
        relogioDoPlayerDaPagina.removeCallbacks(esconderControlesDoPlayerDaPagina)
        relogioDoPlayerDaPagina.postDelayed(esconderControlesDoPlayerDaPagina, espera)
    }

    private fun alternarControlesDoPlayerDaPagina() {
        val visiveis = barraSuperiorDoPlayerDaPagina.visibility == View.VISIBLE ||
            controlesCentraisDoPlayerDaPagina.visibility == View.VISIBLE
        if (visiveis) {
            relogioDoPlayerDaPagina.removeCallbacks(esconderControlesDoPlayerDaPagina)
            esconderControlesDoPlayerDaPagina.run()
        } else {
            mostrarControlesDoPlayerPorUmInstante()
        }
    }

    private fun montarIncrementosDoPlayerDaPagina() {
        incrementoDoPlayerDaPagina = preferencias.incrementoDeDeslocamentoDoVideo
        val margem = (8 * resources.displayMetrics.density).toInt()
        IncrementosDeDeslocamentoDoPlayerDeVideo.opcoes.forEach { segundos ->
            val botao = Button(this)
            botao.text = "${segundos}s"
            botao.setOnClickListener {
                incrementoDoPlayerDaPagina = segundos
                preferencias.incrementoDeDeslocamentoDoVideo = segundos
                atualizarIncrementoSelecionado()
                mostrarControlesDoPlayerPorUmInstante()
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            params.marginEnd = margem
            listaDeIncrementosDoPlayerDaPagina.addView(botao, params)
            botoesDeIncrementoDoPlayerDaPagina += segundos to botao
        }
        atualizarIncrementoSelecionado()
    }

    private fun atualizarIncrementoSelecionado() {
        botoesDeIncrementoDoPlayerDaPagina.forEach { (segundos, botao) ->
            AparenciaDosBotoesDeVelocidadeDoVideo.aplicar(this, botao, segundos == incrementoDoPlayerDaPagina)
        }
        findViewById<ImageButton>(R.id.botao_retroceder_dez_segundos_do_player_da_pagina).contentDescription =
            getString(R.string.retroceder_video_por_segundos, incrementoDoPlayerDaPagina)
        findViewById<ImageButton>(R.id.botao_avancar_dez_segundos_do_player_da_pagina).contentDescription =
            getString(R.string.avancar_video_por_segundos, incrementoDoPlayerDaPagina)
    }

    private fun deslocarPlayerDaPagina(segundos: Int) {
        val aviso = acumuloDoDeslocamentoDoPlayer.registrar(segundos, System.currentTimeMillis())
        avisoDoDeslocamentoDoPlayerDaPagina.text = aviso.texto
        avisoDoDeslocamentoDoPlayerDaPagina.visibility = if (aviso.visivel) View.VISIBLE else View.GONE
        relogioDoPlayerDaPagina.removeCallbacks(esconderAvisoDoDeslocamentoDoPlayer)
        if (aviso.visivel) {
            relogioDoPlayerDaPagina.postDelayed(esconderAvisoDoDeslocamentoDoPlayer, 1200)
        }
        comandarPlayerDaPagina("chromebrothersDeslocar", segundos.toString())
        mostrarControlesDoPlayerPorUmInstante()
    }

    private fun definirReproducaoDoPlayer(tocar: Boolean) {
        usuarioQuerReproduzir = tocar
        videoDoPlayerEstaPausado = !tocar
        botaoPausarPlayerDaPagina.setImageResource(
            if (tocar) R.drawable.icone_de_pausa_do_player_de_video else R.drawable.icone_de_reproduzir_do_player_de_video,
        )
        botaoPausarPlayerDaPagina.contentDescription = getString(if (tocar) R.string.pausar_video else R.string.retomar_video)
        comandarPlayerDaPagina("chromebrothersDefinirReproducao", if (tocar) "true" else "false")
        mostrarControlesDoPlayerPorUmInstante()
    }

    private fun fecharPainelDoPlayerDaPagina() {
        painelDoPlayerDaPagina.visibility = View.GONE
        barraDeNavegacao.visibility = View.VISIBLE
        aplicarDisposicaoDaBarraDoNavegador()
        congelarMargensParaOPlayerDaPagina(false)
        atualizarBotaoDeBaixarMidia()
        relogioDoPlayerDaPagina.removeCallbacks(pulsoDoPlayerDaPagina)
        relogioDoPlayerDaPagina.removeCallbacks(esconderControlesDoPlayerDaPagina)
        relogioDoPlayerDaPagina.removeCallbacks(esconderAvisoDoDeslocamentoDoPlayer)
        relogioDoPlayerDaPagina.removeCallbacks(capturarMiniaturaDoPlayerDaPagina)
        avisoDoDeslocamentoDoPlayerDaPagina.visibility = View.GONE
        controladorDoScrubFinoDoPlayerDaPagina?.cancelarSemRetomar()
        chaveDoVideoNoPlayerDaPagina = null
        atualizarEstrelaDoPlayerDaPagina()
        acumuloDoDeslocamentoDoPlayer.reiniciar()
        idDaAbaNoPlayerDaPagina = null
        idDoVideoNoPlayerDaPagina = null
        paginas[gestao.idDaAbaAtiva]?.let { aplicarPosicaoDoBotao(it) }
    }

    private fun sairDoPlayerDaPagina() {
        val idVideo = idDoVideoNoPlayerDaPagina
        val pagina = idDaAbaNoPlayerDaPagina?.let { paginas[it] }
        if (idVideo != null && pagina != null) {
            val idCitado = JSONObject.quote(idVideo)
            pagina.webView.evaluateJavascript("window.chromebrothersSairDoPlayer($idCitado)", null)
        }
        fecharPainelDoPlayerDaPagina()
    }

    private fun comandarPlayerDaPagina(funcao: String, argumentoExtra: String? = null) {
        val idVideo = idDoVideoNoPlayerDaPagina ?: return
        val pagina = idDaAbaNoPlayerDaPagina?.let { paginas[it] } ?: return
        val idCitado = JSONObject.quote(idVideo)
        val chamada = if (argumentoExtra == null) {
            "window.$funcao($idCitado)"
        } else {
            "window.$funcao($idCitado, $argumentoExtra)"
        }
        pagina.webView.evaluateJavascript(chamada, null)
    }

    private fun lerEstadoDoPlayerDaPagina() {
        val idVideo = idDoVideoNoPlayerDaPagina ?: return
        val pagina = idDaAbaNoPlayerDaPagina?.let { paginas[it] } ?: return
        val idCitado = JSONObject.quote(idVideo)
        pagina.webView.evaluateJavascript("window.chromebrothersLerEstado($idCitado)") { bruto ->
            val estado = LeituraDoEstadoDoVideoControladoNaPagina.ler(textoDoJavascript(bruto)) ?: return@evaluateJavascript
            if (painelDoPlayerDaPagina.visibility != View.VISIBLE) return@evaluateJavascript
            botaoPausarPlayerDaPagina.setImageResource(
                if (estado.pausado) {
                    R.drawable.icone_de_reproduzir_do_player_de_video
                } else {
                    R.drawable.icone_de_pausa_do_player_de_video
                },
            )
            botaoPausarPlayerDaPagina.contentDescription = getString(
                if (estado.pausado) R.string.retomar_video else R.string.pausar_video,
            )
            videoDoPlayerEstaPausado = estado.pausado
            textoDaPosicaoDoPlayerDaPagina.text =
                textoDeTempo(estado.posicaoSegundos) + " / " + textoDeTempo(estado.duracaoSegundos)
            barraDePosicaoDoPlayerDaPagina.tag = estado.duracaoSegundos
            barraDePosicaoDoPlayerDaPagina.isEnabled = estado.duracaoSegundos > 0.0
            val scrub = controladorDoScrubFinoDoPlayerDaPagina
            // No scrub, só trava a barra enquanto o dedo arrasta; com play a trilha precisa da posição ao vivo.
            val travarBarraPeloScrub = scrub?.estaAtivo == true && scrub.estaComDedoBaixo
            if (!usuarioArrastaPosicaoDoPlayer &&
                !travarBarraPeloScrub &&
                estado.duracaoSegundos > 0.0
            ) {
                val progresso = (estado.posicaoSegundos / estado.duracaoSegundos * 1000.0)
                    .toInt()
                    .coerceIn(0, 1000)
                barraDePosicaoDoPlayerDaPagina.progress = progresso
            }
            if (estado.velocidade != velocidadeDoPlayerDaPagina &&
                AjusteDeVelocidadeDeReproducaoDoVideo.opcoes.contains(estado.velocidade)
            ) {
                velocidadeDoPlayerDaPagina = estado.velocidade
                atualizarVelocidadeDoPlayerDaPagina()
            }
        }
    }

    private fun atualizarVelocidadeDoPlayerDaPagina() {
        botoesDeVelocidadeDoPlayerDaPagina.forEach { (taxa, botao) ->
            AparenciaDosBotoesDeVelocidadeDoVideo.aplicar(this, botao, taxa == velocidadeDoPlayerDaPagina)
        }
    }

    private fun capturarQuadroDoPlayerDaPagina(salvarNaGaleria: Boolean) {
        val idVideo = idDoVideoNoPlayerDaPagina
        val pagina = idDaAbaNoPlayerDaPagina?.let { paginas[it] }
        if (idVideo == null || pagina == null) {
            copiarPixelsVisiveisDoPlayer(salvarNaGaleria)
            return
        }
        val idCitado = JSONObject.quote(idVideo)
        pagina.webView.evaluateJavascript(
            chamadaDeCapturaDeQuadro(idCitado, LimiteDaLarguraDaCapturaDeQuadroDoVideo.QUADRO_SALVO),
        ) { bruto ->
            val png = pngDoDataUrl(textoDoJavascript(bruto))
            if (png != null) {
                entregarQuadro(png, salvarNaGaleria)
            } else if (
                primeiraSuperficie(superficieDoPlayerDaPagina) != null ||
                primeiraTextura(superficieDoPlayerDaPagina) != null
            ) {
                copiarSuperficieDireta(salvarNaGaleria)
            } else {
                copiarPixelsVisiveisDoPlayer(salvarNaGaleria)
            }
        }
    }

    private fun capturarBitmapDaFilmstripDoPlayerDaPagina(aoReceber: (Bitmap?) -> Unit) {
        val idVideo = idDoVideoNoPlayerDaPagina
        val pagina = idDaAbaNoPlayerDaPagina?.let { paginas[it] }
        if (idVideo == null || pagina == null) {
            capturarBitmapDoGeckoDoPlayerDaPaginaParaFilmstrip(aoReceber)
            return
        }
        val idCitado = JSONObject.quote(idVideo)
        pagina.webView.evaluateJavascript(
            chamadaDeCapturaDeQuadro(idCitado, 240),
        ) { bruto ->
            val texto = textoDoJavascript(bruto)
            val bytes = pngDoDataUrl(texto)
            val peloCanvas = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            if (peloCanvas != null) {
                aoReceber(peloCanvas)
            } else {
                // Canvas costuma falhar (CORS). O vídeo está no GeckoView, não na View vazia do overlay.
                capturarBitmapDoGeckoDoPlayerDaPaginaParaFilmstrip(aoReceber)
            }
        }
    }

    private fun capturarBitmapDoGeckoDoPlayerDaPaginaParaFilmstrip(aoReceber: (Bitmap?) -> Unit) {
        val pagina = idDaAbaNoPlayerDaPagina?.let { paginas[it] }
        val webView = pagina?.webView
        if (webView == null) {
            aoReceber(null)
            return
        }
        val textura = primeiraTextura(webView)
        if (textura != null && textura.isAvailable && textura.width > 0 && textura.height > 0) {
            try {
                val bruto = textura.getBitmap(240, 135) ?: textura.bitmap
                if (bruto != null) {
                    val copia = bruto.copy(Bitmap.Config.ARGB_8888, false)
                    if (copia != null && copia !== bruto && !bruto.isRecycled) bruto.recycle()
                    aoReceber(copia ?: bruto)
                    return
                }
            } catch (_: Exception) {
            }
        }
        val superficie = primeiraSuperficie(webView)
        if (superficie == null || superficie.width <= 0 || superficie.height <= 0) {
            aoReceber(null)
            return
        }
        val captura = Bitmap.createBitmap(superficie.width, superficie.height, Bitmap.Config.ARGB_8888)
        try {
            PixelCopy.request(superficie, captura, { resultado ->
                if (resultado != PixelCopy.SUCCESS) {
                    captura.recycle()
                    aoReceber(null)
                    return@request
                }
                val destinoLargura = 240
                val destinoAltura = (captura.height * (destinoLargura.toFloat() / captura.width))
                    .toInt()
                    .coerceAtLeast(1)
                val reduzido = Bitmap.createScaledBitmap(captura, destinoLargura, destinoAltura, true)
                if (reduzido !== captura) captura.recycle()
                aoReceber(reduzido)
            }, Handler(Looper.getMainLooper()))
        } catch (_: Exception) {
            captura.recycle()
            aoReceber(null)
        }
    }

    private fun copiarPixelsVisiveisDoPlayer(salvarNaGaleria: Boolean) {
        val vista = superficieDoPlayerDaPagina
        if (vista.width <= 0 || vista.height <= 0 || painelDoPlayerDaPagina.childCount < 3) {
            avisar(R.string.quadro_indisponivel)
            return
        }
        val origem = IntArray(2)
        painelDoPlayerDaPagina.getLocationInWindow(origem)
        val acima = barraSuperiorDoPlayerDaPagina.visibility == View.VISIBLE
        val abaixo = barraInferiorDoPlayerDaPagina.visibility == View.VISIBLE
        val alturaAcima = if (acima) barraSuperiorDoPlayerDaPagina.height else 0
        val alturaAbaixo = if (abaixo) barraInferiorDoPlayerDaPagina.height else 0
        val retangulo = android.graphics.Rect(
            origem[0],
            origem[1] + alturaAcima,
            origem[0] + painelDoPlayerDaPagina.width,
            origem[1] + painelDoPlayerDaPagina.height - alturaAbaixo,
        )
        val janela = android.graphics.Rect(0, 0, window.decorView.width, window.decorView.height)
        if (!retangulo.intersect(janela) || retangulo.width() <= 0 || retangulo.height() <= 0) {
            avisar(R.string.quadro_indisponivel)
            return
        }
        val bitmap = Bitmap.createBitmap(retangulo.width(), retangulo.height(), Bitmap.Config.ARGB_8888)
        try {
            PixelCopy.request(window, retangulo, bitmap, { resultado ->
                if (resultado == PixelCopy.SUCCESS) {
                    val saida = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)
                    bitmap.recycle()
                    entregarQuadro(saida.toByteArray(), salvarNaGaleria)
                } else {
                    bitmap.recycle()
                    avisar(R.string.quadro_indisponivel)
                }
            }, Handler(Looper.getMainLooper()))
        } catch (_: Exception) {
            bitmap.recycle()
            avisar(R.string.quadro_indisponivel)
        }
    }

    private fun copiarSuperficieDireta(salvarNaGaleria: Boolean) {
        val textura = primeiraTextura(superficieDoPlayerDaPagina)
        val quadroDaTextura = textura?.bitmap
        if (quadroDaTextura != null) {
            val saida = ByteArrayOutputStream()
            quadroDaTextura.compress(Bitmap.CompressFormat.PNG, 100, saida)
            quadroDaTextura.recycle()
            entregarQuadro(saida.toByteArray(), salvarNaGaleria)
            return
        }
        val superficie = primeiraSuperficie(superficieDoPlayerDaPagina)
        if (superficie == null || superficie.width <= 0 || superficie.height <= 0) {
            copiarPixelsVisiveisDoPlayer(salvarNaGaleria)
            return
        }
        val bitmap = Bitmap.createBitmap(superficie.width, superficie.height, Bitmap.Config.ARGB_8888)
        try {
            PixelCopy.request(superficie, bitmap, { resultado ->
                if (resultado == PixelCopy.SUCCESS) {
                    val saida = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)
                    bitmap.recycle()
                    entregarQuadro(saida.toByteArray(), salvarNaGaleria)
                } else {
                    bitmap.recycle()
                    copiarPixelsVisiveisDoPlayer(salvarNaGaleria)
                }
            }, Handler(Looper.getMainLooper()))
        } catch (_: Exception) {
            bitmap.recycle()
            copiarPixelsVisiveisDoPlayer(salvarNaGaleria)
        }
    }

    private fun entregarQuadro(png: ByteArray, salvarNaGaleria: Boolean) {
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
                avisar(R.string.quadro_salvo)
            } else {
                copiarQuadroERegistrar(png)
                avisar(R.string.quadro_copiado)
            }
        } catch (_: Exception) {
            avisar(R.string.quadro_indisponivel)
        }
    }

    private fun esconderControlesEmbutidos(raiz: View) {
        if (raiz is SurfaceView || raiz is TextureView) return
        if (raiz !is ViewGroup) return
        val temImagem = (0 until raiz.childCount).any { contemImagemDeVideo(raiz.getChildAt(it)) }
        if (!temImagem) return
        for (indice in 0 until raiz.childCount) {
            val filho = raiz.getChildAt(indice)
            if (contemImagemDeVideo(filho)) {
                esconderControlesEmbutidos(filho)
            } else {
                filho.visibility = View.GONE
            }
        }
    }

    private fun contemImagemDeVideo(view: View): Boolean {
        if (view is SurfaceView || view is TextureView) return true
        if (view is ViewGroup) {
            for (indice in 0 until view.childCount) {
                if (contemImagemDeVideo(view.getChildAt(indice))) return true
            }
        }
        return false
    }

    private fun primeiraSuperficie(view: View): SurfaceView? {
        if (view is SurfaceView) return view
        if (view is ViewGroup) {
            for (indice in 0 until view.childCount) {
                primeiraSuperficie(view.getChildAt(indice))?.let { return it }
            }
        }
        return null
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

    private fun textoDeTempo(segundos: Double): String {
        val total = segundos.toInt().coerceAtLeast(0)
        return "%d:%02d".format(total / 60, total % 60)
    }

    private fun abrirPlayerComEndereco(
        pagina: PaginaDeUmaAbaDoNavegadorChromebrothers,
        endereco: String,
        velocidade: Double,
        titulo: String = "",
    ) {
        if (!DecisaoDeAbrirVideoNoPlayerNativo.podeAbrir(endereco)) {
            avisar(R.string.player_nativo_indisponivel)
            return
        }
        pagina.webView.evaluateJavascript(
            "Array.prototype.forEach.call(document.querySelectorAll('video'), function (video) { video.pause(); })",
            null,
        )
        retornoDoPlayerDeArquivo.launch(
            Intent(this, AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro::class.java)
                .putExtra(AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_ENDERECO, endereco)
                .putExtra(AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_VELOCIDADE, velocidade)
                .putExtra(AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_TITULO, titulo)
                .putExtra(
                    AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_REFERER,
                    pagina.webView.url.orEmpty(),
                )
                .putExtra(
                    AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_AGENTE,
                    pagina.webView.settings.userAgentString.orEmpty(),
                )
                // Opções de vídeo da página: o botão de download do player escolhe a melhor (toque) ou lista (segurar).
                .also { intent -> OpcoesDeDownloadNoPlayerNativo.colocar(intent, pagina.pedidosDeMidiaObservados()) },
        )
    }

    private fun abrirArquivoBaixado(uri: String, titulo: String, posicaoMs: Long = 0L, velocidade: Double = 1.0) {
        painelDaBibliotecaDeVideos.esconder()
        if (painelDoPlayerDaPagina.visibility == View.VISIBLE) sairDoPlayerDaPagina()
        paginas[gestao.idDaAbaAtiva]?.webView?.evaluateJavascript(
            "Array.prototype.forEach.call(document.querySelectorAll('video'), function (video) { video.pause(); })",
            null,
        )
        retornoDoPlayerDeArquivo.launch(
            Intent(this, AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro::class.java)
                .putExtra(AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_ENDERECO, uri)
                .putExtra(AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_VELOCIDADE, velocidade)
                .putExtra(AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_TITULO, titulo)
                .putExtra(AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.EXTRA_POSICAO_MS, posicaoMs),
        )
    }

    private fun abrirImagemDoComponenteNaAba(uriTexto: String): String {
        val regras = com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
        if (!regras.esquemaAceito(uriTexto)) {
            return regras.mensagemErro("Uri não suportada.")
        }
        val destino = if (regras.precisaCopiarParaArquivoLocal(uriTexto)) {
            val pasta = File(cacheDir, "midia-do-componente-da-colecao").apply { mkdirs() }
            val arquivo = leituraDeMidiaDoComponenteDaColecao.copiarParaArquivoLocal(
                uriTexto,
                pasta,
                "imagem-${System.currentTimeMillis()}",
            ) ?: return regras.mensagemErro("Não deu para copiar a imagem.")
            "file://${arquivo.absolutePath}"
        } else {
            uriTexto.trim()
        }
        val latch = java.util.concurrent.CountDownLatch(1)
        runOnUiThread {
            try {
                abrirLinkEmNovaAba(destino)
            } finally {
                latch.countDown()
            }
        }
        latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
        return regras.mensagemOk(
            com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao.Destino.ABA.rotulo,
            "Abri a imagem numa nova aba.",
        )
    }

    /**
     * Menu nativo do toque longo na mídia do Visualizador/coleção
     * (visor / editor / copiar / guardar / excluir).
     */
    private fun visorExternoDaGaveta(): VisorExternoDeImagemDoVisualizadorDeMidiaQuandoEstaNaGavetaDoChat {
        visorExternoDaGavetaCriado?.let { return it }
        val criado = VisorExternoDeImagemDoVisualizadorDeMidiaQuandoEstaNaGavetaDoChat(
            activity = this,
            host = findViewById(R.id.area_visivel_das_paginas),
            leitor = leituraDeMidiaDoComponenteDaColecao,
            aoMudarIndice = { uri ->
                if (::painelDaColecao.isInitialized) {
                    painelDaColecao.destacarCartaDoVisualizadorNasJanelas(uri)
                }
            },
            // Fantasma sob o dedo: arrastar leva a um destino; soltar no lugar abre o menu.
            aoToqueLongo = { uri, x, y -> iniciarArrasteFantasmaDoVisorDeImagem(uri, x, y) },
            aoFechar = {
                if (::painelDaColecao.isInitialized) {
                    painelDaColecao.limparDestaqueDeCartaDoVisualizadorNasJanelas()
                }
                fecharAbaDoVisorSeVazia()
            },
        )
        visorExternoDaGavetaCriado = criado
        return criado
    }

    /** Chamado pela ponte JS (fora da thread principal): decodifica as capas e abre o player ao lado. */
    private fun abrirVideosDoComponenteAoLado(
        painel: PainelDaColecaoFlutuanteDoChatDoNavegador,
        itensJson: String,
        indice: Int,
    ) {
        val videos = runCatching {
            val lista = org.json.JSONArray(itensJson)
            (0 until lista.length()).mapNotNull { i ->
                val item = lista.optJSONObject(i) ?: return@mapNotNull null
                val uri = item.optString("fileUrl").trim()
                if (uri.isEmpty()) return@mapNotNull null
                VideoDoVisorAoLado(uri, capaDoDataUrl(item.optString("previewDataUrl")))
            }
        }.getOrDefault(emptyList())
        if (videos.isEmpty()) return
        runOnUiThread {
            mostrarVideosAoLado(videos, indice) { uri -> painel.avisarVideoEmExibicaoNoVisorAoLado(uri) }
        }
    }

    /** Vídeos tocados na grade do Visualizador de mídia; a capa vem da miniatura já em memória. */
    private fun abrirVideosDaGradeDoVisualizadorAoLado(uris: List<String>, indice: Int) {
        val videos = uris.map { VideoDoVisorAoLado(it, GradeNativaDoVisualizadorDeMidiaNaColecaoDoNavegador.miniaturaPronta(it)) }
        if (videos.isEmpty()) return
        mostrarVideosAoLado(videos, indice) { uri ->
            if (uri == null) {
                painelDaColecao.limparDestaqueDeCartaDoVisualizadorNasJanelas()
            } else {
                painelDaColecao.destacarCartaDoVisualizadorNasJanelas(uri)
            }
        }
    }

    /** Botões «ver na área da página» / «janela flutuante» do player de tela cheia. */
    private fun continuarVideoDaTelaCheiaNaNavegacao(dados: Intent, modo: String) {
        val tela = AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro
        val uri = dados.getStringExtra(tela.EXTRA_ENDERECO).orEmpty()
        if (uri.isBlank()) return
        val video = VideoDoVisorAoLado(uri, GradeNativaDoVisualizadorDeMidiaNaColecaoDoNavegador.miniaturaPronta(uri))
        val anterior = avisarDonoDoVisorDeVideosAoLado
        avisarDonoDoVisorDeVideosAoLado = { exibido ->
            if (::painelDaColecao.isInitialized) {
                if (exibido == null) {
                    painelDaColecao.limparDestaqueDeCartaDoVisualizadorNasJanelas()
                } else {
                    painelDaColecao.destacarCartaDoVisualizadorNasJanelas(exibido)
                }
            }
        }
        anterior?.invoke(null)
        val visor = visorDeVideosAoLado()
        visor.definirFlutuante(modo == tela.MODO_JANELA_FLUTUANTE)
        if (!visor.estaFlutuanteNoModo()) garantirAbaDoVisor()
        visor.mostrar(
            listOf(video),
            0,
            posicaoInicialMs = dados.getLongExtra(tela.EXTRA_POSICAO_MS, 0L),
            velocidade = dados.getDoubleExtra(tela.EXTRA_VELOCIDADE, 1.0),
        )
    }

    private fun mostrarVideosAoLado(
        videos: List<VideoDoVisorAoLado>,
        indice: Int,
        avisarDono: (String?) -> Unit,
    ) {
        val anterior = avisarDonoDoVisorDeVideosAoLado
        avisarDonoDoVisorDeVideosAoLado = avisarDono
        anterior?.invoke(null)
        val visor = visorDeVideosAoLado()
        if (!visor.estaFlutuanteNoModo()) garantirAbaDoVisor()
        visor.mostrar(videos, indice)
    }

    private fun mostrarMenuDoVideoDaGradeDoVisualizador(uri: String, reproduzirAqui: () -> Unit) {
        val dialogo = Dialog(this)
        val raiz = layoutInflater.inflate(
            R.layout.layout_caixa_de_opcoes_do_video_do_visualizador_de_midia_no_toque_longo,
            null,
        )
        raiz.findViewById<TextView>(R.id.titulo_do_menu_do_video_do_visualizador_de_midia).text =
            Uri.decode(uri.substringBefore('?').substringAfterLast('/')).ifBlank {
                getString(R.string.titulo_do_menu_do_video_do_visualizador_de_midia)
            }
        listOf(
            R.id.botao_reproduzir_video_do_visualizador_no_player_embutido,
            R.id.botao_abrir_video_do_visualizador_em_janela_flutuante,
            R.id.botao_abrir_video_do_visualizador_em_tela_cheia,
            R.id.botao_mover_video_do_visualizador_para_a_galeria,
            R.id.botao_excluir_video_do_visualizador,
            R.id.botao_cancelar_menu_do_video_do_visualizador,
        ).forEach { id ->
            raiz.findViewById<Button>(id).apply {
                backgroundTintList = null
                stateListAnimator = null
            }
        }
        var manterDestaque = false
        raiz.findViewById<Button>(R.id.botao_reproduzir_video_do_visualizador_no_player_embutido).setOnClickListener {
            manterDestaque = true
            dialogo.dismiss()
            visorDeVideosAoLado().definirFlutuante(false)
            reproduzirAqui()
        }
        raiz.findViewById<Button>(R.id.botao_abrir_video_do_visualizador_em_janela_flutuante).setOnClickListener {
            manterDestaque = true
            dialogo.dismiss()
            visorDeVideosAoLado().definirFlutuante(true)
            reproduzirAqui()
        }
        raiz.findViewById<Button>(R.id.botao_abrir_video_do_visualizador_em_tela_cheia).setOnClickListener {
            dialogo.dismiss()
            visorDeVideosAoLadoCriado?.fechar()
            abrirArquivoBaixado(uri, uri.substringBefore('?').substringAfterLast('/').ifBlank { "vídeo" })
        }
        val uriLimpa = com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
            .uriSemParametroDeMiniatura(uri)
        raiz.findViewById<Button>(R.id.botao_mover_video_do_visualizador_para_a_galeria).apply {
            // Só vídeo gerado guardado dentro do app (downloads já estão em pasta pública).
            visibility = if (com.chromebrothers.navegador.regras.MovimentoDaMidiaGeradaParaAGaleria.podeMover(uriLimpa)) {
                View.VISIBLE
            } else {
                View.GONE
            }
            setOnClickListener {
                dialogo.dismiss()
                fecharVisorDeVideosSeMostrar(uriLimpa)
                moverMidiasGeradasParaAGaleria(listOf(uriLimpa))
            }
        }
        raiz.findViewById<Button>(R.id.botao_excluir_video_do_visualizador).setOnClickListener {
            dialogo.dismiss()
            CaixaDeConfirmacaoDoNavegador.mostrar(
                activity = this,
                titulo = CaixaDeConfirmacaoDoNavegador.tituloDeExcluir(1),
                mensagem = CaixaDeConfirmacaoDoNavegador.mensagemDeExcluir(1),
                textoConfirmar = "Excluir",
            ) {
                fecharVisorDeVideosSeMostrar(uriLimpa)
                Thread {
                    excluirArquivosDoVisualizadorDeMidia(org.json.JSONArray().put(uriLimpa).toString())
                    runOnUiThread { atualizarVisualizadorDeMidiaComArquivosDoChromeBrothers() }
                }.start()
            }
        }
        raiz.findViewById<Button>(R.id.botao_cancelar_menu_do_video_do_visualizador).setOnClickListener { dialogo.dismiss() }
        dialogo.setOnDismissListener {
            if (manterDestaque || !::painelDaColecao.isInitialized) return@setOnDismissListener
            painelDaColecao.limparDestaqueDeCartaDoVisualizadorNasJanelas()
            visorDeVideosAoLadoCriado?.uriAtual()?.let { painelDaColecao.destacarCartaDoVisualizadorNasJanelas(it) }
        }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        dialogo.window?.setLayout((420 * resources.displayMetrics.density).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun capaDoDataUrl(dataUrl: String): android.graphics.Bitmap? {
        val base64 = dataUrl.substringAfter("base64,", "")
        if (base64.isBlank()) return null
        return runCatching {
            val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

    /** O vídeo vai sair do lugar (mover/excluir): se é o que está tocando ao lado, fecha o player antes. */
    private fun fecharVisorDeVideosSeMostrar(uri: String) {
        val atual = visorDeVideosAoLadoCriado?.uriAtual() ?: return
        val semMiniatura = com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
        if (semMiniatura.uriSemParametroDeMiniatura(atual) == uri) visorDeVideosAoLadoCriado?.fechar()
    }

    private fun visorDeVideosAoLado(): VisorDeVideosAoLadoComPlayerNativoEmbutidoNaColecao {
        visorDeVideosAoLadoCriado?.let { return it }
        val criado = VisorDeVideosAoLadoComPlayerNativoEmbutidoNaColecao(
            activity = this,
            host = findViewById(R.id.area_visivel_das_paginas),
            preferencias = preferencias,
            aoCapturarQuadro = { png, salvarNaGaleria -> entregarQuadro(png, salvarNaGaleria) },
            estaFavorito = { uri -> itemDaBibliotecaDoVideoGerado(uri)?.favorito == true },
            alternarFavorito = { uri ->
                itemDaBibliotecaDoVideoGerado(uri)?.let { item ->
                    bibliotecaDeVideos.definirFavorito(item.chave, !item.favorito)
                    gravacaoDaBibliotecaDeVideos.gravar(bibliotecaDeVideos)
                    if (painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.atualizar()
                }
            },
            aoMudarVideo = { uri -> avisarDonoDoVisorDeVideosAoLado?.invoke(uri) },
            aoFechar = {
                avisarDonoDoVisorDeVideosAoLado?.invoke(null)
                avisarDonoDoVisorDeVideosAoLado = null
                fecharAbaDoVisorSeVazia()
            },
            // Na área da página o vídeo mora na aba Visor; flutuando ele é da navegação (a aba vazia fecha).
            aoMudarModo = { flutuante ->
                if (flutuante) fecharAbaDoVisorSeVazia() else garantirAbaDoVisor()
            },
            aoPedirTelaCheia = { uri, posicaoMs, velocidade ->
                abrirArquivoBaixado(
                    uri,
                    uri.substringBefore('?').substringAfterLast('/').ifBlank { "vídeo" },
                    posicaoMs,
                    velocidade,
                )
            },
        )
        visorDeVideosAoLadoCriado = criado
        return criado
    }

    /** Vídeo gerado via MCP: a chave na biblioteca é o nome do arquivo sem `.mp4`. */
    private fun itemDaBibliotecaDoVideoGerado(uri: String): com.chromebrothers.navegador.regras.ItemDaBibliotecaDeVideos? {
        if (!::bibliotecaDeVideos.isInitialized) return null
        val chave = uri.substringBefore('?').substringAfterLast('/').removeSuffix(".mp4")
        return bibliotecaDeVideos.buscar(chave)
            ?: bibliotecaDeVideos.listar().firstOrNull { it.enderecoParaAbrir == uri }
    }

    private fun mostrarOpcoesDaMidiaDoComponenteNoToqueLongo(uriTexto: String) {
        val uri = com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
            .uriSemParametroDeMiniatura(uriTexto)
        if (uri.isBlank()) return
        val dialogo = Dialog(this)
        val raiz = layoutInflater.inflate(
            R.layout.layout_caixa_de_opcoes_da_midia_do_componente_no_toque_longo_do_navegador,
            null,
        )
        listOf(
            R.id.botao_abrir_midia_do_componente_no_visor_pelo_toque_longo,
            R.id.botao_abrir_midia_do_componente_no_editor_pelo_toque_longo,
            R.id.botao_copiar_midia_do_componente_pelo_toque_longo,
            R.id.botao_guardar_midia_do_componente_na_biblioteca_pelo_toque_longo,
            R.id.botao_mover_midia_do_componente_para_a_galeria_pelo_toque_longo,
            R.id.botao_excluir_midia_do_componente_pelo_toque_longo,
            R.id.botao_cancelar_opcoes_da_midia_do_componente,
        ).forEach { id ->
            raiz.findViewById<Button>(id).apply {
                backgroundTintList = null
                stateListAnimator = null
            }
        }
        fun fechar() = dialogo.dismiss()
        raiz.findViewById<Button>(R.id.botao_abrir_midia_do_componente_no_visor_pelo_toque_longo)
            .setOnClickListener {
                fechar()
                Thread { abrirImagemDoComponenteNoVisorOuEditor(uri, noEditor = false) }.start()
            }
        raiz.findViewById<Button>(R.id.botao_abrir_midia_do_componente_no_editor_pelo_toque_longo)
            .setOnClickListener {
                fechar()
                Thread { abrirImagemDoComponenteNoVisorOuEditor(uri, noEditor = true) }.start()
            }
        raiz.findViewById<Button>(R.id.botao_copiar_midia_do_componente_pelo_toque_longo)
            .setOnClickListener {
                fechar()
                Thread { copiarImagemDoComponenteParaAreaDeTransferencia(uri) }.start()
            }
        raiz.findViewById<Button>(R.id.botao_guardar_midia_do_componente_na_biblioteca_pelo_toque_longo)
            .setOnClickListener {
                fechar()
                Thread { guardarImagemDoComponenteNaBiblioteca(uri) }.start()
            }
        raiz.findViewById<Button>(R.id.botao_mover_midia_do_componente_para_a_galeria_pelo_toque_longo).apply {
            // Só mídia gerada guardada dentro do app (o resto já está numa pasta pública).
            visibility = if (com.chromebrothers.navegador.regras.MovimentoDaMidiaGeradaParaAGaleria.podeMover(uri)) {
                View.VISIBLE
            } else {
                View.GONE
            }
            setOnClickListener {
                fechar()
                moverMidiasGeradasParaAGaleria(listOf(uri))
            }
        }
        raiz.findViewById<Button>(R.id.botao_excluir_midia_do_componente_pelo_toque_longo)
            .setOnClickListener {
                fechar()
                CaixaDeConfirmacaoDoNavegador.mostrar(
                    activity = this,
                    titulo = CaixaDeConfirmacaoDoNavegador.tituloDeExcluir(1),
                    mensagem = CaixaDeConfirmacaoDoNavegador.mensagemDeExcluir(1),
                    textoConfirmar = "Excluir",
                ) {
                    Thread {
                        val jsonUris = org.json.JSONArray().put(uri).toString()
                        excluirArquivosDoVisualizadorDeMidia(jsonUris)
                        runOnUiThread { atualizarVisualizadorDeMidiaComArquivosDoChromeBrothers() }
                    }.start()
                }
            }
        raiz.findViewById<Button>(R.id.botao_cancelar_opcoes_da_midia_do_componente)
            .setOnClickListener { fechar() }
        dialogo.setOnDismissListener {
            if (::painelDaColecao.isInitialized) {
                painelDaColecao.limparDestaqueDeCartaDoVisualizadorNasJanelas()
                visorExternoDaGavetaCriado?.uriAtual()?.let { uri ->
                    painelDaColecao.destacarCartaDoVisualizadorNasJanelas(uri)
                }
            }
        }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        val largura = (420 * resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
        if (::painelDaColecao.isInitialized) {
            painelDaColecao.destacarCartaDoVisualizadorNasJanelas(uri)
        }
    }

    private fun abrirImagemDoComponenteNoVisorOuEditor(uriTexto: String, noEditor: Boolean): String {
        val regras = com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
        if (!regras.esquemaAceito(uriTexto)) {
            return regras.mensagemErro("Uri não suportada.")
        }
        if (!::painelDaBibliotecaDeVideos.isInitialized) {
            return regras.mensagemErro("A biblioteca ainda não está pronta.")
        }
        val bytes = leituraDeMidiaDoComponenteDaColecao.ler(uriTexto)
            ?: return regras.mensagemErro("Não deu para ler a imagem.")
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return regras.mensagemErro("Esse arquivo não parece uma imagem.")
        val latch = java.util.concurrent.CountDownLatch(1)
        var detalhe = ""
        var falhou = false
        runOnUiThread {
            try {
                if (!painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.mostrar()
                detalhe = if (noEditor) {
                    painelDaBibliotecaDeVideos.editarImagemVindaDaPagina(bitmap)
                    "Abri a imagem no editor."
                } else {
                    painelDaBibliotecaDeVideos.mostrarImagemVindaDeFora(bitmap)
                }
            } catch (_: Exception) {
                falhou = true
                if (!bitmap.isRecycled) bitmap.recycle()
            } finally {
                latch.countDown()
            }
        }
        latch.await(8, java.util.concurrent.TimeUnit.SECONDS)
        if (falhou) return regras.mensagemErro("Não deu para abrir a imagem.")
        val destino = if (noEditor) {
            com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao.Destino.EDITOR
        } else {
            com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao.Destino.VISOR
        }
        return regras.mensagemOk(destino.rotulo, detalhe)
    }

    /** Enquanto o arraste da biblioteca está ativo, a Activity consome os toques e move o fantasma. */
    override fun dispatchTouchEvent(ev: android.view.MotionEvent): Boolean {
        if (estadoDoArrasteDaBibliotecaAtivo == null) {
            ultimoXDoToqueNaTela = ev.rawX
            ultimoYDoToqueNaTela = ev.rawY
        }
        if (ev.actionMasked == android.view.MotionEvent.ACTION_DOWN &&
            ::listaDeSugestoes.isInitialized &&
            ::caixaDaBarraDeEndereco.isInitialized &&
            FechamentoDaListaDeSugestoesDaBarraPorToqueForaDoNavegador.deveFechar(
                listaVisivel = listaDeSugestoesEstaoVisiveis(),
                toqueNaCaixaDeEndereco = toqueCaiNaVista(caixaDaBarraDeEndereco, ev.rawX, ev.rawY),
                toqueNaLista = toqueCaiNaVista(listaDeSugestoes, ev.rawX, ev.rawY),
            )
        ) {
            esconderSugestoesDaBarra()
            esconderTeclado()
        }
        if (estadoDoArrasteDaBibliotecaAtivo != null) {
            if (cancelarToquePendenteDaGaveta) {
                // O toque que disparou o long-press ainda está "aberto" na gaveta: cancela para o cartão/scroll.
                cancelarToquePendenteDaGaveta = false
                val cancelamento = android.view.MotionEvent.obtain(ev)
                cancelamento.action = android.view.MotionEvent.ACTION_CANCEL
                super.dispatchTouchEvent(cancelamento)
                cancelamento.recycle()
            }
            when (ev.actionMasked) {
                android.view.MotionEvent.ACTION_MOVE -> atualizarArrasteVisualDaBiblioteca(ev.rawX, ev.rawY)
                android.view.MotionEvent.ACTION_UP -> soltarArrasteVisualDaBiblioteca(ev.rawX, ev.rawY)
                android.view.MotionEvent.ACTION_CANCEL -> encerrarFantasmaDaBiblioteca()
            }
            return true
        }
        if (arrasteDeImagemVeioDaPagina && fantasmaDoArrasteDeImagemDoComponente != null) {
            when (ev.actionMasked) {
                android.view.MotionEvent.ACTION_MOVE ->
                    atualizarFantasmaDoArrasteDeImagemDoComponente(ev.rawX, ev.rawY)
                android.view.MotionEvent.ACTION_UP ->
                    finalizarArrasteDeImagemDaPagina(ev.rawX, ev.rawY)
                android.view.MotionEvent.ACTION_CANCEL -> encerrarFantasmaDoArrasteDeImagemDoComponente()
            }
            return true
        }
        if (!arrasteDeImagemVeioDaPagina && fantasmaDoArrasteDeImagemDoComponente != null) {
            when (ev.actionMasked) {
                android.view.MotionEvent.ACTION_MOVE ->
                    atualizarFantasmaDoArrasteDeImagemDoComponente(ev.rawX, ev.rawY)
                android.view.MotionEvent.ACTION_UP -> {
                    val uri = uriDoArrasteDeImagemDoComponente.orEmpty()
                    if (uri.isNotBlank()) {
                        val x = ev.rawX
                        val y = ev.rawY
                        Thread { soltarArrasteDoComponenteUmaVez(uri, x, y) }.start()
                    } else {
                        encerrarFantasmaDoArrasteDeImagemDoComponente()
                    }
                }
                android.view.MotionEvent.ACTION_CANCEL -> encerrarFantasmaDoArrasteDeImagemDoComponente()
            }
            return true
        }
        if (tratarGestoDeBordaDaGavetaDoChat(ev)) return true
        return super.dispatchTouchEvent(ev)
    }

    private var inicioDoGestoDeBordaDoChat =
        com.chromebrothers.navegador.regras.GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador.Inicio.NENHUM
    private var xDoInicioDoGestoDeBordaDoChat = 0f
    private var yDoInicioDoGestoDeBordaDoChat = 0f
    private var gestoDeBordaDoChatDisparou = false

    /** Deslize da borda direita abre o chat; da junção página/chat para a direita recolhe. */
    private fun tratarGestoDeBordaDaGavetaDoChat(ev: android.view.MotionEvent): Boolean {
        val regra = com.chromebrothers.navegador.regras.GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador
        val nenhum = com.chromebrothers.navegador.regras.GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador.Inicio.NENHUM
        when (ev.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                gestoDeBordaDoChatDisparou = false
                xDoInicioDoGestoDeBordaDoChat = ev.rawX
                yDoInicioDoGestoDeBordaDoChat = ev.rawY
                inicioDoGestoDeBordaDoChat = inicioDoGestoDeBordaDoChatNoPonto(ev.rawX, ev.rawY)
                return false
            }
            android.view.MotionEvent.ACTION_POINTER_DOWN -> {
                if (!gestoDeBordaDoChatDisparou) inicioDoGestoDeBordaDoChat = nenhum
                return gestoDeBordaDoChatDisparou
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                if (gestoDeBordaDoChatDisparou) return true
                if (inicioDoGestoDeBordaDoChat == nenhum) return false
                val densidade = resources.displayMetrics.density
                val decisao = regra.decidir(
                    inicioDoGestoDeBordaDoChat,
                    (ev.rawX - xDoInicioDoGestoDeBordaDoChat) / densidade,
                    (ev.rawY - yDoInicioDoGestoDeBordaDoChat) / densidade,
                )
                when (decisao) {
                    com.chromebrothers.navegador.regras.GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador.Decisao.AGUARDAR ->
                        return false
                    com.chromebrothers.navegador.regras.GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador.Decisao.DESISTIR -> {
                        inicioDoGestoDeBordaDoChat = nenhum
                        return false
                    }
                    com.chromebrothers.navegador.regras.GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador.Decisao.DISPARAR -> {
                        val abrir = inicioDoGestoDeBordaDoChat ==
                            com.chromebrothers.navegador.regras.GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador.Inicio.ABRIR_PELA_BORDA
                        gestoDeBordaDoChatDisparou = true
                        inicioDoGestoDeBordaDoChat = nenhum
                        val cancelamento = android.view.MotionEvent.obtain(ev)
                        cancelamento.action = android.view.MotionEvent.ACTION_CANCEL
                        super.dispatchTouchEvent(cancelamento)
                        cancelamento.recycle()
                        findViewById<View>(R.id.raiz_do_navegador)
                            ?.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                        if (abrir) painelDoChat.abrirGavetaSeFechada() else painelDoChat.fecharGavetaSeAberta()
                        return true
                    }
                }
            }
            android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                val consumir = gestoDeBordaDoChatDisparou
                gestoDeBordaDoChatDisparou = false
                inicioDoGestoDeBordaDoChat = nenhum
                return consumir
            }
        }
        return gestoDeBordaDoChatDisparou
    }

    private fun inicioDoGestoDeBordaDoChatNoPonto(
        xNaTela: Float,
        yNaTela: Float,
    ): com.chromebrothers.navegador.regras.GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador.Inicio {
        val nenhum = com.chromebrothers.navegador.regras.GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador.Inicio.NENHUM
        if (!::painelDoChat.isInitialized || !::preferencias.isInitialized) return nenhum
        if (!preferencias.abrirEFecharChatPorGestoDeBorda) return nenhum
        val area = findViewById<View>(R.id.area_visivel_das_paginas) ?: return nenhum
        if (!area.isShown || area.width <= 0) return nenhum
        val densidade = resources.displayMetrics.density
        val origemDaArea = IntArray(2)
        area.getLocationOnScreen(origemDaArea)
        val dentroDaAltura = yNaTela >= origemDaArea[1] && yNaTela <= origemDaArea[1] + area.height
        val aberta = painelDoChat.estaAberta()
        var esquerdaDaGaveta = 0f
        if (aberta) {
            val gaveta = findViewById<View>(R.id.gaveta_do_chat_da_openrouter) ?: return nenhum
            if (!gaveta.isShown || gaveta.width <= 0) return nenhum
            val origemDaGaveta = IntArray(2)
            gaveta.getLocationOnScreen(origemDaGaveta)
            esquerdaDaGaveta = origemDaGaveta[0] / densidade
        }
        return com.chromebrothers.navegador.regras.GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador.inicio(
            habilitado = true,
            gavetaAberta = aberta,
            xDp = xNaTela / densidade,
            dentroDaAlturaDaArea = dentroDaAltura,
            direitaDaAreaDasPaginasDp = (origemDaArea[0] + area.width) / densidade,
            esquerdaDaGavetaDp = esquerdaDaGaveta,
        )
    }

    fun iniciarArrasteVisualDaBiblioteca(estado: EstadoDoArrasteDaBiblioteca, bitmap: Bitmap?) {
        geracaoDaRestauracaoAposColarDaBiblioteca++
        encerrarFantasmaDaBiblioteca()
        if (::painelDaBibliotecaDeVideos.isInitialized) {
            painelDaBibliotecaDeVideos.liberarPaginaSobArrasteDaBiblioteca()
        }
        val densidade = resources.displayMetrics.density
        val tamanho = (96f * densidade).toInt().coerceAtLeast(72)
        val raiz = findViewById<ViewGroup>(R.id.raiz_do_navegador) ?: return
        val capa = FrameLayout(this).apply {
            elevation = 64f * densidade
            alpha = 0.82f
        }
        val foto = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            if (bitmap != null) setImageBitmap(bitmap)
            setBackgroundColor(Color.parseColor("#CC1B1D24"))
        }
        val mais = TextView(this).apply {
            text = "+"
            setTextColor(corMaisArrasteNeutro)
            textSize = 22f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(Color.parseColor("#E61B1D24"))
            val lado = (28f * densidade).toInt()
            layoutParams = FrameLayout.LayoutParams(lado, lado).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.END
                topMargin = (4f * densidade).toInt()
                marginEnd = (4f * densidade).toInt()
            }
        }
        capa.addView(
            foto,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        capa.addView(mais)
        raiz.addView(capa, ViewGroup.LayoutParams(tamanho, tamanho))
        capa.bringToFront()
        fantasmaDoArrasteDaBiblioteca = capa
        maisDoFantasmaDaBiblioteca = mais
        bitmapDoFantasmaDaBiblioteca = bitmap
        estadoDoArrasteDaBibliotecaAtivo = estado
        tamanhoDoFantasmaDaBibliotecaPx = tamanho
        cancelarToquePendenteDaGaveta = true
        bytesProntosParaColarNoSoltar = null
        bytesProntosSaoMiniatura = false
        mimeProntosParaColarNoSoltar = null
        // Pré-carrega bytes em memória; clipboard só no soltar verde.
        Thread {
            val preparado = prepararBytesParaColarDoArrasteDaBiblioteca(estado.caminhoLocal, estado.tipo)
            runOnUiThread {
                if (estadoDoArrasteDaBibliotecaAtivo !== estado) return@runOnUiThread
                if (preparado != null) {
                    bytesProntosParaColarNoSoltar = preparado.first
                    mimeProntosParaColarNoSoltar = preparado.second
                }
            }
        }.start()
        atualizarArrasteVisualDaBiblioteca(ultimoXDoToqueNaTela, ultimoYDoToqueNaTela)
    }

    fun atualizarArrasteVisualDaBiblioteca(xNaTela: Float, yNaTela: Float) {
        val capa = fantasmaDoArrasteDaBiblioteca ?: return
        val raiz = findViewById<View>(R.id.raiz_do_navegador) ?: return
        ultimoXDoToqueNaTela = xNaTela
        ultimoYDoToqueNaTela = yNaTela
        val loc = IntArray(2)
        raiz.getLocationOnScreen(loc)
        val metade = tamanhoDoFantasmaDaBibliotecaPx / 2f
        capa.x = xNaTela - loc[0] - metade
        capa.y = yNaTela - loc[1] - metade
        considerarTrocaDeAbaPorPairarFantasma(xNaTela, yNaTela)
        sondarEditavelProximoNaPaginaParaArrasteDaBiblioteca(xNaTela, yNaTela)
        atualizarCorDoMaisDoFantasmaDaBiblioteca(xNaTela, yNaTela)
    }

    fun soltarArrasteVisualDaBiblioteca(xNaTela: Float, yNaTela: Float) {
        val estado = estadoDoArrasteDaBibliotecaAtivo
        // Congela o verde antes do encerrar zerar o estado — é o sinal de que soltar deve colar.
        val verdeIndicavaColar = editavelProximoNaPaginaNoArrasteDaBiblioteca
        val coords = coordenadasLocaisNaPaginaAtiva(xNaTela, yNaTela)
        var adiarRestaurarParaColar = false
        try {
            if (estado != null && soltouSobreOChat(xNaTela, yNaTela)) {
                if (estado.tipo == "image") {
                    anexarAoChatPeloArrasteDoFantasma(
                        caminhoLocal = estado.caminhoLocal,
                        bytes = bytesProntosParaColarNoSoltar,
                        mime = mimeProntosParaColarNoSoltar,
                        uriTexto = null,
                    )
                } else {
                    Toast.makeText(this, R.string.so_imagens_podem_ser_anexadas_ao_chat_pelo_arraste, Toast.LENGTH_SHORT).show()
                }
                return
            }
            if (estado != null && ::painelDaColecao.isInitialized) {
                val painel = painelDaColecao.painelSobPonto(xNaTela, yNaTela)
                val tipo = painelDaColecao.tipoGeradorMcpSobPonto(xNaTela, yNaTela)
                if (painel != null && tipo.isNotEmpty()) {
                    soltarItemDaBibliotecaComoReferenciaDaGeracaoNucleo(painel, estado, tipo)
                    return
                }
            }
            // Só cola com “+” verde; amarelo = só some a thumb.
            adiarRestaurarParaColar = com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina
                .deveColarNaPaginaAoSoltar(
                    editavelProximoNaPagina = verdeIndicavaColar,
                    pontoSobrePagina = coords != null,
                )
            if (adiarRestaurarParaColar && estado != null) {
                val caminho = estado.caminhoLocal
                val tipo = estado.tipo
                val bytesJaProntos = bytesProntosParaColarNoSoltar
                val mimeJaProntos = mimeProntosParaColarNoSoltar
                Thread {
                    val preparado = bytesJaProntos?.takeIf { it.isNotEmpty() }?.let { it to (mimeJaProntos ?: "image/png") }
                        ?: prepararBytesParaColarDoArrasteDaBiblioteca(caminho, tipo)
                    runOnUiThread {
                        copiarParaAreaDeTransferenciaEColarNaPagina(
                            bytes = preparado?.first,
                            mime = preparado?.second ?: "image/png",
                            uriFallback = null,
                            xNaTela = xNaTela,
                            yNaTela = yNaTela,
                            coordsFixas = coords,
                            forcarColarPeloVerde = true,
                        )
                    }
                }.start()
            }
        } finally {
            encerrarFantasmaDaBiblioteca(restaurarBloqueioDaGaveta = !adiarRestaurarParaColar)
        }
    }

    /**
     * Página fica liberada [atrasoMs] após o foco; só então o fundo da gaveta volta.
     * Janela experimental longa para o Ctrl+V pegar no Gecko.
     */
    private fun agendarRestaurarBloqueioDaGavetaAposFoco(
        atrasoMs: Long = atrasoParaRestaurarPaginaAposFocoMs,
    ) {
        val geracao = ++geracaoDaRestauracaoAposColarDaBiblioteca
        val raiz = findViewById<View>(R.id.raiz_do_navegador)
        val rodar = Runnable {
            if (geracao != geracaoDaRestauracaoAposColarDaBiblioteca) return@Runnable
            if (::painelDaBibliotecaDeVideos.isInitialized) {
                painelDaBibliotecaDeVideos.restaurarBloqueioAposArrasteDaBiblioteca(comFade = true)
            }
        }
        if (raiz == null) {
            rodar.run()
            return
        }
        raiz.postDelayed(rodar, atrasoMs)
    }


    private fun atualizarCorDoMaisDoFantasmaDaBiblioteca(xNaTela: Float, yNaTela: Float) {
        val mais = maisDoFantasmaDaBiblioteca ?: return
        val sobreGerador = ::painelDaColecao.isInitialized &&
            painelDaColecao.tipoGeradorMcpSobPonto(xNaTela, yNaTela).isNotEmpty()
        val podeSoltar = com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina
            .corIndicaPodeSoltar(
                sobreGeradorMcp = sobreGerador,
                editavelProximoNaPagina = editavelProximoNaPaginaNoArrasteDaBiblioteca,
            ) || (estadoDoArrasteDaBibliotecaAtivo?.tipo == "image" && soltouSobreOChat(xNaTela, yNaTela))
        mais.setTextColor(if (podeSoltar) corMaisArrastePodeSoltar else corMaisArrasteNeutro)
    }

    /**
     * clientX/Y (CSS px do Gecko) → coordenadas de tela Android.
     * Preferir [ultimoXDoToqueNaTela] quando o dedo ainda está no gesto; isto é só fallback.
     */
    private fun coordenadasDeClienteCssDaPaginaParaTela(
        idDaAba: Long,
        clientX: Float,
        clientY: Float,
    ): FloatArray {
        val vista = paginas[idDaAba]?.webView ?: return floatArrayOf(clientX, clientY)
        val loc = IntArray(2)
        vista.getLocationOnScreen(loc)
        val escala = resources.displayMetrics.density.coerceAtLeast(0.01f)
        return floatArrayOf(loc[0] + clientX * escala, loc[1] + clientY * escala)
    }

    /**
     * Pixels locais na GeckoView (não CSS). O JS converte com innerWidth/viewW.
     * Retorna null se o ponto está fora da página.
     */
    private fun coordenadasLocaisNaPaginaAtiva(
        xNaTela: Float,
        yNaTela: Float,
    ): Triple<Double, Double, SuperficieDaPaginaNoMotorDoFirefox>? {
        val vista = paginas[gestao.idDaAbaAtiva]?.webView ?: return null
        if (vista.width <= 0 || vista.height <= 0) return null
        val loc = IntArray(2)
        vista.getLocationOnScreen(loc)
        if (
            xNaTela < loc[0] ||
            yNaTela < loc[1] ||
            xNaTela > loc[0] + vista.width ||
            yNaTela > loc[1] + vista.height
        ) {
            return null
        }
        val xLocal = (xNaTela - loc[0]).toDouble()
        val yLocal = (yNaTela - loc[1]).toDouble()
        return Triple(xLocal, yLocal, vista)
    }

    private fun sondarEditavelProximoNaPaginaParaArrasteDaBiblioteca(xNaTela: Float, yNaTela: Float) {
        if (fantasmaDoArrasteDaBiblioteca == null) return
        val ponto = coordenadasLocaisNaPaginaAtiva(xNaTela, yNaTela)
        if (ponto == null) {
            if (editavelProximoNaPaginaNoArrasteDaBiblioteca) {
                editavelProximoNaPaginaNoArrasteDaBiblioteca = false
            }
            return
        }
        val agora = android.os.SystemClock.uptimeMillis()
        if (agora - ultimoHorarioDaSondaDoArrasteDaBibliotecaMs < 70L) return
        ultimoHorarioDaSondaDoArrasteDaBibliotecaMs = agora
        val (xLocal, yLocal, vista) = ponto
        val geracao = ++geracaoDaSondaDoArrasteDaBiblioteca
        val script = com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina
            .scriptParaSondar(
                xLocalNaView = xLocal,
                yLocalNaView = yLocal,
                larguraDaViewPx = vista.width.toDouble(),
                alturaDaViewPx = vista.height.toDouble(),
            )
        vista.evaluateJavascript(script) { bruto ->
            runOnUiThread {
                if (geracao != geracaoDaSondaDoArrasteDaBiblioteca) return@runOnUiThread
                if (fantasmaDoArrasteDaBiblioteca == null) return@runOnUiThread
                val texto = textoDoJavascript(bruto).ifEmpty { bruto?.trim().orEmpty() }
                val ok = com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina
                    .ehResultadoOk(texto)
                if (ok == editavelProximoNaPaginaNoArrasteDaBiblioteca) return@runOnUiThread
                editavelProximoNaPaginaNoArrasteDaBiblioteca = ok
                atualizarCorDoMaisDoFantasmaDaBiblioteca(ultimoXDoToqueNaTela, ultimoYDoToqueNaTela)
            }
        }
    }

    /**
     * No soltar verde: grava a imagem na área de transferência e só então dispara o foco + Ctrl+V.
     * Amarelo/cancelar não chama isto — clipboard fica intacto.
     */
    private fun copiarParaAreaDeTransferenciaEColarNaPagina(
        bytes: ByteArray?,
        mime: String,
        uriFallback: String?,
        xNaTela: Float,
        yNaTela: Float,
        coordsFixas: Triple<Double, Double, SuperficieDaPaginaNoMotorDoFirefox>? = null,
        forcarColarPeloVerde: Boolean = false,
        liberarFocoDaColecao: Boolean = false,
        atrasoAntesDoToqueMs: Long = 0L,
    ) {
        Thread {
            val payload = when {
                bytes != null && bytes.isNotEmpty() -> bytes to mime
                !uriFallback.isNullOrBlank() -> {
                    val lidos = leituraDeMidiaDoComponenteDaColecao.ler(uriFallback)
                    if (lidos != null && lidos.isNotEmpty()) {
                        lidos to mime.ifBlank { mimeDaUriDeMidiaDoComponente(uriFallback) }
                    } else {
                        null
                    }
                }
                else -> null
            }
            if (payload != null) {
                runCatching {
                    GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.copiarParaAreaDeTransferencia(
                        this,
                        payload.first,
                        payload.second,
                    )
                }
            }
            runOnUiThread {
                colarItemDaBibliotecaNoCampoMaisProximoDaPagina(
                    xNaTela = xNaTela,
                    yNaTela = yNaTela,
                    coordsFixas = coordsFixas,
                    forcarColarPeloVerde = forcarColarPeloVerde,
                    liberarFocoDaColecao = liberarFocoDaColecao,
                    atrasoAntesDoToqueMs = atrasoAntesDoToqueMs,
                )
            }
        }.start()
    }

    private fun prepararBytesParaColarDoArrasteDaBiblioteca(
        caminhoLocal: String,
        tipo: String,
    ): Pair<ByteArray, String>? {
        val arquivo = caminhoLocal.takeIf { it.isNotBlank() }?.let { java.io.File(it) }
            ?.takeIf { it.isFile && it.length() > 0L }
            ?: return null
        return try {
            if (tipo == "video") {
                val quadro = ExtracaoDeQuadroJpegDeVideoLocalParaArraste.extrairParaArquivo(this, arquivo)
                    ?: return null
                val bytes = quadro.readBytes()
                runCatching { quadro.delete() }
                if (bytes.isEmpty()) null else bytes to "image/jpeg"
            } else if (arquivo.length() <= 20L * 1024 * 1024) {
                val bytes = arquivo.readBytes()
                if (bytes.isEmpty()) null
                else bytes to (CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.mimeDaImagem(bytes)
                    .ifBlank { "image/jpeg" })
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun colarItemDaBibliotecaNoCampoMaisProximoDaPagina(
        xNaTela: Float,
        yNaTela: Float,
        coordsFixas: Triple<Double, Double, SuperficieDaPaginaNoMotorDoFirefox>? = null,
        forcarColarPeloVerde: Boolean = false,
        liberarFocoDaColecao: Boolean = false,
        atrasoAntesDoToqueMs: Long = 0L,
    ) {
        val ponto = coordsFixas ?: coordenadasLocaisNaPaginaAtiva(xNaTela, yNaTela)
        val vista = ponto?.third ?: paginas[gestao.idDaAbaAtiva]?.webView ?: return
        if (liberarFocoDaColecao && ::painelDaColecao.isInitialized) {
            painelDaColecao.liberarFocoParaColarNaPagina()
            agendarRestaurarFocoDaColecaoAposColar(atrasoParaRestaurarPaginaAposFocoMs)
        }
        // Página liberada 1s; Ctrl+V no meio da janela.
        agendarRestaurarBloqueioDaGavetaAposFoco(atrasoParaRestaurarPaginaAposFocoMs)
        val rodar = Runnable {
            // 1) Toque nativo → caret.
            vista.dispararToqueParaFocarNaPosicaoNaTela(xNaTela, yNaTela)
            val aposFoco = {
                vista.postDelayed({
                    vista.pedirFocoParaOTeclado()
                    vista.dispararColarDaAreaDeTransferencia()
                }, 350L)
            }
            if (ponto == null) {
                if (forcarColarPeloVerde) aposFoco()
                return@Runnable
            }
            val (xLocal, yLocal, _) = ponto
            val script = com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina
                .scriptParaFocar(
                    xLocalNaView = xLocal,
                    yLocalNaView = yLocal,
                    larguraDaViewPx = vista.width.toDouble(),
                    alturaDaViewPx = vista.height.toDouble(),
                )
            vista.postDelayed({
                vista.evaluateJavascript(script) { _ ->
                    runOnUiThread { aposFoco() }
                }
            }, 80L)
        }
        if (atrasoAntesDoToqueMs > 0L) {
            vista.postDelayed(rodar, atrasoAntesDoToqueMs)
        } else {
            rodar.run()
        }
    }

    private fun agendarRestaurarFocoDaColecaoAposColar(
        atrasoMs: Long = atrasoParaRestaurarPaginaAposFocoMs,
    ) {
        val geracao = ++geracaoDaRestauracaoFocoColecaoAposColar
        val raiz = findViewById<View>(R.id.raiz_do_navegador) ?: return
        raiz.postDelayed({
            if (geracao != geracaoDaRestauracaoFocoColecaoAposColar) return@postDelayed
            restaurarColecaoAposArrasteDoComponente()
        }, atrasoMs)
    }

    private fun encerrarFantasmaDaBiblioteca(restaurarBloqueioDaGaveta: Boolean = true) {
        limparEstadoDoPairarAbaNoArrasteDoFantasma()
        fantasmaDoArrasteDaBiblioteca?.let { capa -> (capa.parent as? ViewGroup)?.removeView(capa) }
        fantasmaDoArrasteDaBiblioteca = null
        maisDoFantasmaDaBiblioteca = null
        estadoDoArrasteDaBibliotecaAtivo = null
        // O bitmap pode ser o drawable da miniatura da gaveta: não reciclar aqui.
        bitmapDoFantasmaDaBiblioteca = null
        tamanhoDoFantasmaDaBibliotecaPx = 0
        bytesProntosParaColarNoSoltar = null
        bytesProntosSaoMiniatura = false
        mimeProntosParaColarNoSoltar = null
        editavelProximoNaPaginaNoArrasteDaBiblioteca = false
        geracaoDaSondaDoArrasteDaBiblioteca++
        ultimoHorarioDaSondaDoArrasteDaBibliotecaMs = 0L
        if (restaurarBloqueioDaGaveta && ::painelDaBibliotecaDeVideos.isInitialized) {
            painelDaBibliotecaDeVideos.restaurarBloqueioAposArrasteDaBiblioteca()
        }
    }

    /**
     * Soltar o dedo após long-press na página:
     * - quase sem mover → modal (copiar/salvar/…);
     * - arrastou → destino do fantasma (colar/editor/MCP/cancelar).
     */
    private fun finalizarArrasteDeImagemDaPagina(xNaTela: Float, yNaTela: Float) {
        if (!arrasteDeImagemVeioDaPagina) return
        // Claim: evita modal/soltar duplicados se UP nativo e AVISO_FIM chegarem juntos.
        arrasteDeImagemVeioDaPagina = false
        val enderecoOriginal = enderecoOriginalDaImagemDaPaginaNoArraste
        val idAba = idDaAbaDoArrasteDeImagemDaPagina
        val x0 = xInicioDoArrasteDeImagemDaPagina
        val y0 = yInicioDoArrasteDeImagemDaPagina
        val abrirModal = com.chromebrothers.navegador.regras.DecisaoDoToqueLongoEmImagemDaPaginaEntreModalEArraste
            .deveAbrirModalEmVezDeSoltarArraste(
                xInicio = x0,
                yInicio = y0,
                xFim = xNaTela,
                yFim = yNaTela,
            )
        if (abrirModal) {
            encerrarFantasmaDoArrasteDeImagemDoComponente()
            val vista = paginas[idAba]?.webView
            if (vista != null && !enderecoOriginal.isNullOrBlank()) {
                copiaDeImagemDaPagina.perguntarPorEndereco(vista, enderecoOriginal)
            }
            return
        }
        val uri = uriDoArrasteDeImagemDoComponente
        if (uri.isNullOrBlank()) {
            encerrarFantasmaDoArrasteDeImagemDoComponente()
            return
        }
        Thread {
            soltarArrasteDeImagemDoComponenteParaOEditor(uri, xNaTela, yNaTela, veioDaPagina = true)
        }.start()
    }

    private fun iniciarArrasteDeImagemDaPagina(
        endereco: String,
        xNaTela: Float,
        yNaTela: Float,
        idDaAba: Long,
    ) {
        val vista = paginas[idDaAba]?.webView ?: return
        val src = endereco.trim()
        if (src.isEmpty()) return
        encerrarFantasmaDoArrasteDeImagemDoComponente()
        arrasteDeImagemVeioDaPagina = true
        enderecoOriginalDaImagemDaPaginaNoArraste = src
        idDaAbaDoArrasteDeImagemDaPagina = idDaAba
        xInicioDoArrasteDeImagemDaPagina = xNaTela
        yInicioDoArrasteDeImagemDaPagina = yNaTela
        val geracao = ++geracaoDoArrasteDeImagemDaPagina
        // Fantasma imediato (placeholder); bytes chegam em background.
        criarFantasmaVisualDoArrasteDeImagem(
            bitmap = null,
            xNaTela = xNaTela,
            yNaTela = yNaTela,
            veioDaPagina = true,
        )
        // Cancela o toque na WebView para não roubar o gesto.
        runCatching {
            val agora = android.os.SystemClock.uptimeMillis()
            val cancel = android.view.MotionEvent.obtain(
                agora, agora, android.view.MotionEvent.ACTION_CANCEL, 0f, 0f, 0,
            )
            vista.dispatchTouchEvent(cancel)
            cancel.recycle()
        }
        copiaDeImagemDaPagina.baixarBytes(vista, src) { bytes ->
            if (geracao != geracaoDoArrasteDeImagemDaPagina) return@baixarBytes
            if (!arrasteDeImagemVeioDaPagina) return@baixarBytes
            if (bytes == null || bytes.isEmpty()) {
                Toast.makeText(this, R.string.imagem_da_pagina_indisponivel, Toast.LENGTH_SHORT).show()
                encerrarFantasmaDoArrasteDeImagemDoComponente()
                return@baixarBytes
            }
            val mime = CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.mimeDaImagem(bytes)
            val ext = when {
                mime.contains("png") -> "png"
                mime.contains("webp") -> "webp"
                mime.contains("gif") -> "gif"
                else -> "jpg"
            }
            val pasta = java.io.File(cacheDir, "arraste-imagem-da-pagina").apply { mkdirs() }
            val arquivo = java.io.File(pasta, "pagina-${System.currentTimeMillis()}.$ext")
            if (!runCatching { arquivo.writeBytes(bytes) }.isSuccess) {
                encerrarFantasmaDoArrasteDeImagemDoComponente()
                return@baixarBytes
            }
            val uriCheia = arquivo.toURI().toString()
            uriDoArrasteDeImagemDoComponente = uriCheia
            bytesProntosParaColarNoSoltar = bytes
            mimeProntosParaColarNoSoltar = mime
            val opts = BitmapFactory.Options().apply {
                inSampleSize = 2
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            if (bmp != null) {
                atualizarBitmapDoFantasmaDoArrasteDeImagem(bmp)
            }
        }
    }

    private fun criarFantasmaVisualDoArrasteDeImagem(
        bitmap: Bitmap?,
        xNaTela: Float,
        yNaTela: Float,
        veioDaPagina: Boolean,
    ) {
        val densidade = resources.displayMetrics.density
        val tamanho = (96f * densidade).toInt().coerceAtLeast(72)
        val raiz = findViewById<ViewGroup>(R.id.raiz_do_navegador) ?: return
        val capa = FrameLayout(this).apply {
            elevation = 64f * densidade
            alpha = 0.82f
        }
        val foto = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            if (bitmap != null) setImageBitmap(bitmap)
            setBackgroundColor(Color.parseColor("#CC1B1D24"))
            tag = "foto_fantasma_arraste"
        }
        val mais = TextView(this).apply {
            text = "+"
            setTextColor(corMaisArrasteNeutro)
            textSize = 22f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(Color.parseColor("#E61B1D24"))
            val lado = (28f * densidade).toInt()
            layoutParams = FrameLayout.LayoutParams(lado, lado).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.END
                topMargin = (4f * densidade).toInt()
                marginEnd = (4f * densidade).toInt()
            }
        }
        capa.addView(
            foto,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        capa.addView(mais)
        raiz.addView(capa, ViewGroup.LayoutParams(tamanho, tamanho))
        capa.bringToFront()
        fantasmaDoArrasteDeImagemDoComponente = capa
        maisDoFantasmaDoArrasteDeImagemDoComponente = mais
        bitmapDoFantasmaDoArrasteDeImagemDoComponente = bitmap
        arrasteDeImagemVeioDaPagina = veioDaPagina
        tamanhoDoFantasmaDoArrastePx = tamanho
        editavelProximoNaPaginaNoArrasteDoComponente = false
        teveEditavelNoArrasteDoComponente = false
        xPaginaOkDoArrasteDoComponente = 0f
        yPaginaOkDoArrasteDoComponente = 0f
        geracaoDaSondaDoArrasteDoComponente++
        ultimoHorarioDaSondaDoArrasteDoComponenteMs = 0L
        ultimoXDoArrasteDoComponente = xNaTela
        ultimoYDoArrasteDoComponente = yNaTela
        // Antes do 1º layout a view nasce em (0,0); ancora já e de novo após o layout.
        posicionarFantasmaDoArrasteDeImagemSobODedo(capa, xNaTela, yNaTela)
        capa.post { posicionarFantasmaDoArrasteDeImagemSobODedo(capa, ultimoXDoArrasteDoComponente, ultimoYDoArrasteDoComponente) }
        capa.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        fantasmaNaZonaDeCancelar = false
        // Com o editor aberto o Visualizador continua sólido: é a zona de cancelar (+ amarelo).
        if (::painelDaColecao.isInitialized &&
            com.chromebrothers.navegador.regras.DestinoDoArrasteDoComponenteDaColecaoParaMcpEditorOuPagina
                .deveLiberarAPaginaSobOVisualizador(editorDeImagemAberto())
        ) {
            painelDaColecao.liberarPaginaSobArrasteDoComponente()
        }
        // Começa dentro do Visualizador quase sempre: estado inicial sem vibrar (já houve a do toque longo).
        atualizarZonaDeCancelarDoFantasma(
            xNaTela,
            yNaTela,
            if (::painelDaColecao.isInitialized) painelDaColecao.tipoGeradorMcpSobPonto(xNaTela, yNaTela) else "",
            vibrar = false,
        )
        sondarEditavelProximoNaPaginaParaArrasteDoComponente(xNaTela, yNaTela)
        atualizarCorDoMaisDoFantasmaDoArraste(xNaTela, yNaTela)
    }

    private fun posicionarFantasmaDoArrasteDeImagemSobODedo(
        capa: View,
        xNaTela: Float,
        yNaTela: Float,
    ) {
        val raiz = findViewById<View>(R.id.raiz_do_navegador) ?: return
        val loc = IntArray(2)
        raiz.getLocationOnScreen(loc)
        val metade = tamanhoDoFantasmaDoArrastePx / 2f
        capa.x = xNaTela - loc[0] - metade
        capa.y = yNaTela - loc[1] - metade
    }

    private fun atualizarBitmapDoFantasmaDoArrasteDeImagem(bitmap: Bitmap) {
        val capa = fantasmaDoArrasteDeImagemDoComponente ?: run {
            if (!bitmap.isRecycled) bitmap.recycle()
            return
        }
        val foto = (0 until capa.childCount)
            .map { capa.getChildAt(it) }
            .filterIsInstance<ImageView>()
            .firstOrNull { it.tag == "foto_fantasma_arraste" }
            ?: capa.getChildAt(0) as? ImageView
        if (foto == null) {
            if (!bitmap.isRecycled) bitmap.recycle()
            return
        }
        val antigo = bitmapDoFantasmaDoArrasteDeImagemDoComponente
        foto.setImageBitmap(bitmap)
        bitmapDoFantasmaDoArrasteDeImagemDoComponente = bitmap
        if (antigo != null && antigo !== bitmap && !antigo.isRecycled) antigo.recycle()
        // setImageBitmap pode relayoutar a capa pra (0,0); recoloca sob o dedo.
        posicionarFantasmaDoArrasteDeImagemSobODedo(
            capa,
            ultimoXDoArrasteDoComponente,
            ultimoYDoArrasteDoComponente,
        )
        capa.post {
            if (fantasmaDoArrasteDeImagemDoComponente !== capa) return@post
            posicionarFantasmaDoArrasteDeImagemSobODedo(
                capa,
                ultimoXDoArrasteDoComponente,
                ultimoYDoArrasteDoComponente,
            )
        }
    }

    private fun iniciarFantasmaDoArrasteDeImagemDoComponente(
        uriTexto: String,
        xNaTela: Float = 0f,
        yNaTela: Float = 0f,
    ) {
        // Moldura da carta fica acesa: quem apaga é o soltar após arrastar ou o fechar da modal.
        encerrarFantasmaDoArrasteDeImagemDoComponente(limparDestaqueDaCarta = false)
        soltarDoArrasteDoComponenteJaReivindicado.set(false)
        xInicioDoArrasteDeImagemDoComponente = xNaTela
        yInicioDoArrasteDeImagemDoComponente = yNaTela
        val uriCheia = com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
            .uriSemParametroDeMiniatura(uriTexto)
        if (!com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao.esquemaAceito(uriCheia)) {
            return
        }
        // Toque longo na foto do visor aberto: o visor some quando o dedo arrasta e volta se cancelar.
        arrasteNasceuNoVisor = uriDoVisorDeImagemAberto()?.let {
            com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
                .uriSemParametroDeMiniatura(it) == uriCheia
        } == true
        visorEscondidoPeloArraste = false
        // Arrastar do próprio Visualizador e soltar nele não move nada.
        arrasteNasceuNoVisualizador = !arrasteNasceuNoVisor &&
            ::painelDaColecao.isInitialized &&
            painelDaColecao.pontoSobreVisualizadorDeMidia(xNaTela, yNaTela)
        // Mesmo fantasma do toque longo em imagem de site: nasce já sob o dedo; a foto chega depois.
        criarFantasmaVisualDoArrasteDeImagem(
            bitmap = miniaturaJaCarregada?.copy(Bitmap.Config.ARGB_8888, false),
            xNaTela = xNaTela,
            yNaTela = yNaTela,
            veioDaPagina = false,
        )
        uriDoArrasteDeImagemDoComponente = uriCheia
        val capa = fantasmaDoArrasteDeImagemDoComponente
        val mime = mimeDaUriDeMidiaDoComponente(uriCheia)
        Thread {
            runCatching {
                val bytesThumb = leituraDeMidiaDoComponenteDaColecao.ler(uriDeMiniaturaParaArrasteDoComponente(uriCheia))
                if (bytesThumb != null && bytesThumb.isNotEmpty()) {
                    if (bytesProntosParaColarNoSoltar == null) {
                        bytesProntosParaColarNoSoltar = bytesThumb
                        mimeProntosParaColarNoSoltar = "image/jpeg"
                        bytesProntosSaoMiniatura = true
                    }
                    val desenho = BitmapFactory.Options().apply {
                        inSampleSize = 2
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    val bmp = BitmapFactory.decodeByteArray(bytesThumb, 0, bytesThumb.size, desenho)
                    if (bmp != null) {
                        runOnUiThread {
                            if (fantasmaDoArrasteDeImagemDoComponente === capa && capa != null) {
                                atualizarBitmapDoFantasmaDoArrasteDeImagem(bmp)
                            } else if (!bmp.isRecycled) {
                                bmp.recycle()
                            }
                        }
                    }
                }
                val full = leituraDeMidiaDoComponenteDaColecao.ler(uriCheia) ?: return@runCatching
                if (full.size in 1..(20L * 1024 * 1024).toInt() && fantasmaDoArrasteDeImagemDoComponente === capa) {
                    bytesProntosParaColarNoSoltar = full
                    mimeProntosParaColarNoSoltar = mime
                    bytesProntosSaoMiniatura = false
                }
            }
        }.start()
    }

    /** Bitmap da carta tocada na grade nativa (já em memória) para o fantasma nascer com a foto. */
    private var miniaturaJaCarregada: Bitmap? = null

    /** Toque longo na foto de um visor de imagem: mesmo fantasma da grade (já com a miniatura em memória). */
    private fun iniciarArrasteFantasmaDoVisorDeImagem(uri: String, xNaTela: Float, yNaTela: Float) {
        // Prévia de referência do gerador MCP é só para conferir: sem arraste nem menu de mover/excluir.
        if (com.chromebrothers.navegador.regras.PreviaDaReferenciaDaGeracaoMcpNoVisor.ehPrevia(uri)) return
        iniciarFantasmaDoArrasteDeImagemDoComponenteComMiniatura(
            uri,
            xNaTela,
            yNaTela,
            GradeNativaDoVisualizadorDeMidiaNaColecaoDoNavegador.miniaturaPronta(uri),
        )
    }

    /** O fantasma saiu da foto de um visor de imagem (externo da gaveta ou interno do componente). */
    private var arrasteNasceuNoVisor = false
    private var visorEscondidoPeloArraste = false
    private var arrasteNasceuNoVisualizador = false

    private fun uriDoVisorDeImagemAberto(): String? =
        visorExternoDaGavetaCriado?.uriAtual()
            ?: if (::painelDaColecao.isInitialized) painelDaColecao.uriDoVisorInternoAbertoNasJanelas() else null

    /** Dedo saiu do lugar com o fantasma nascido no visor: esconde o visor para mostrar os destinos. */
    private fun esconderVisorSeOArrasteSaiuDoLugar(xNaTela: Float, yNaTela: Float) {
        if (!arrasteNasceuNoVisor || visorEscondidoPeloArraste) return
        val parado = com.chromebrothers.navegador.regras.DecisaoDoToqueLongoEmImagemDaPaginaEntreModalEArraste
            .deveAbrirModalEmVezDeSoltarArraste(
                xInicio = xInicioDoArrasteDeImagemDoComponente,
                yInicio = yInicioDoArrasteDeImagemDoComponente,
                xFim = xNaTela,
                yFim = yNaTela,
            )
        if (parado) return
        visorEscondidoPeloArraste = true
        visorExternoDaGavetaCriado?.esconderDuranteArraste()
        if (::painelDaColecao.isInitialized) painelDaColecao.esconderVisorInternoDuranteArrasteNasJanelas()
    }

    /** [fechar] = soltou num destino; senão o visor volta na mesma foto (cancelou ou abriu o menu). */
    private fun restaurarVisorAposArraste(fechar: Boolean) {
        if (!arrasteNasceuNoVisor) return
        val escondido = visorEscondidoPeloArraste
        arrasteNasceuNoVisor = false
        visorEscondidoPeloArraste = false
        val fecharDeVez = fechar && escondido
        visorExternoDaGavetaCriado?.let { visor ->
            if (fecharDeVez && visor.estaEscondidoPeloArraste()) visor.fechar() else visor.reaparecerAposArraste()
        }
        if (::painelDaColecao.isInitialized) painelDaColecao.restaurarVisorInternoAposArrasteNasJanelas(fecharDeVez)
    }

    private fun iniciarFantasmaDoArrasteDeImagemDoComponenteComMiniatura(
        uriTexto: String,
        xNaTela: Float,
        yNaTela: Float,
        miniatura: Bitmap?,
    ) {
        miniaturaJaCarregada = miniatura
        try {
            iniciarFantasmaDoArrasteDeImagemDoComponente(uriTexto, xNaTela, yNaTela)
        } finally {
            miniaturaJaCarregada = null
        }
    }

    private fun uriDeMiniaturaParaArrasteDoComponente(uriTexto: String): String {
        if (uriTexto.contains("cb_thumb=")) return uriTexto
        val base = uriTexto.lowercase()
        if (!base.startsWith("content:") && !base.startsWith("file:")) return uriTexto
        return uriTexto + (if (uriTexto.contains('?')) "&" else "?") + "cb_thumb=512"
    }

    private fun mimeDaUriDeMidiaDoComponente(uriTexto: String): String {
        val caminho = uriTexto.substringBefore('?').lowercase()
        return when {
            caminho.endsWith(".png") -> "image/png"
            caminho.endsWith(".webp") -> "image/webp"
            caminho.endsWith(".gif") -> "image/gif"
            else -> "image/jpeg"
        }
    }

    private fun atualizarFantasmaDoArrasteDeImagemDoComponente(xNaTela: Float, yNaTela: Float) {
        val capa = fantasmaDoArrasteDeImagemDoComponente ?: return
        ultimoXDoArrasteDoComponente = xNaTela
        ultimoYDoArrasteDoComponente = yNaTela
        esconderVisorSeOArrasteSaiuDoLugar(xNaTela, yNaTela)
        posicionarFantasmaDoArrasteDeImagemSobODedo(capa, xNaTela, yNaTela)
        considerarTrocaDeAbaPorPairarFantasma(xNaTela, yNaTela)
        sondarEditavelProximoNaPaginaParaArrasteDoComponente(xNaTela, yNaTela)
        atualizarCorDoMaisDoFantasmaDoArraste(xNaTela, yNaTela)
    }

    /**
     * Hit-test na faixa: pairar ~700ms sobre outra aba troca sem soltar o fantasma.
     * Usa postDelayed porque com o dedo parado o Android para de mandar MOVE.
     */
    private fun considerarTrocaDeAbaPorPairarFantasma(xNaTela: Float, yNaTela: Float) {
        if (::controladorDasAbasAncoradasNaGaveta.isInitialized) {
            controladorDasAbasAncoradasNaGaveta.pairarFantasma(xNaTela, yNaTela)
        }
        considerarAbrirOChatPorPairarFantasmaNoBotao(xNaTela, yNaTela)
        val regra = com.chromebrothers.navegador.regras.TrocaDeAbaPorPairarFantasmaDoArrasteDoNavegador
        val agora = android.os.SystemClock.uptimeMillis()
        val idSob = idDaAbaSobPontoNaFaixa(xNaTela, yNaTela)
        val anterior = estadoDoPairarAbaNoArrasteDoFantasma
        estadoDoPairarAbaNoArrasteDoFantasma = regra.atualizarEstado(
            idSobODedo = idSob,
            idDaAbaAtiva = gestao.idDaAbaAtiva,
            estado = anterior,
            agoraMs = agora,
        )
        val destino = estadoDoPairarAbaNoArrasteDoFantasma.idDaAba
        if (destino == null) {
            cancelarAgendamentoDaTrocaDeAbaPorPairarFantasma()
            return
        }
        if (destino == anterior.idDaAba && agendamentoDaTrocaDeAbaPorPairarFantasma != null) return
        cancelarAgendamentoDaTrocaDeAbaPorPairarFantasma()
        val raiz = findViewById<View>(R.id.raiz_do_navegador) ?: return
        val tarefa = Runnable {
            if (estadoDoPairarAbaNoArrasteDoFantasma.idDaAba != destino) return@Runnable
            if (gestao.idDaAbaAtiva == destino) return@Runnable
            estadoDoPairarAbaNoArrasteDoFantasma =
                com.chromebrothers.navegador.regras.EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador()
            agendamentoDaTrocaDeAbaPorPairarFantasma = null
            mostrarAba(destino)
            // Nova página: zera sonda/verde grudado do gesto anterior.
            editavelProximoNaPaginaNoArrasteDoComponente = false
            teveEditavelNoArrasteDoComponente = false
            editavelProximoNaPaginaNoArrasteDaBiblioteca = false
            fantasmaDoArrasteDeImagemDoComponente?.bringToFront()
            fantasmaDoArrasteDaBiblioteca?.bringToFront()
            atualizarCorDoMaisDoFantasmaDoArraste(ultimoXDoArrasteDoComponente, ultimoYDoArrasteDoComponente)
            if (fantasmaDoArrasteDaBiblioteca != null) {
                atualizarCorDoMaisDoFantasmaDaBiblioteca(ultimoXDoToqueNaTela, ultimoYDoToqueNaTela)
            }
        }
        agendamentoDaTrocaDeAbaPorPairarFantasma = tarefa
        raiz.postDelayed(tarefa, regra.ATRASO_MS)
    }

    private fun idDaAbaSobPontoNaFaixa(xNaTela: Float, yNaTela: Float): Long? {
        if (!::faixaDeAbas.isInitialized || !::rolagemDasAbas.isInitialized) return null
        if (rolagemDasAbas.visibility != View.VISIBLE || faixaDeAbas.childCount == 0) return null
        for (i in 0 until faixaDeAbas.childCount) {
            val filho = faixaDeAbas.getChildAt(i) ?: continue
            if (filho.width <= 0 || filho.height <= 0) continue
            val loc = IntArray(2)
            filho.getLocationOnScreen(loc)
            if (
                xNaTela < loc[0] ||
                yNaTela < loc[1] ||
                xNaTela > loc[0] + filho.width ||
                yNaTela > loc[1] + filho.height
            ) {
                continue
            }
            val tag = filho.findViewById<TextView>(R.id.titulo_da_aba_do_navegador)?.tag
            return when (tag) {
                is Long -> tag
                is Number -> tag.toLong()
                else -> null
            }
        }
        return null
    }

    private fun cancelarAgendamentoDaTrocaDeAbaPorPairarFantasma() {
        val tarefa = agendamentoDaTrocaDeAbaPorPairarFantasma ?: return
        findViewById<View>(R.id.raiz_do_navegador)?.removeCallbacks(tarefa)
        agendamentoDaTrocaDeAbaPorPairarFantasma = null
    }

    private var aberturaDoChatPorPairarFantasma: Runnable? = null

    /** Fantasma parado sobre o botão do chat na barra abre a gaveta (para soltar no chat ou num componente dele). */
    private fun considerarAbrirOChatPorPairarFantasmaNoBotao(xNaTela: Float, yNaTela: Float) {
        val caixa = android.graphics.Rect()
        val sobre = ::botaoChatNaBarra.isInitialized && botaoChatNaBarra.isShown &&
            botaoChatNaBarra.getGlobalVisibleRect(caixa) && caixa.contains(xNaTela.toInt(), yNaTela.toInt())
        if (!sobre) {
            cancelarAberturaDoChatPorPairarFantasma()
            return
        }
        if (aberturaDoChatPorPairarFantasma != null) return
        val tarefa = Runnable {
            aberturaDoChatPorPairarFantasma = null
            painelDoChat.abrirGavetaSeFechada()
            fantasmaDoArrasteDeImagemDoComponente?.bringToFront()
            fantasmaDoArrasteDaBiblioteca?.bringToFront()
        }
        aberturaDoChatPorPairarFantasma = tarefa
        botaoChatNaBarra.postDelayed(
            tarefa,
            com.chromebrothers.navegador.regras.TrocaDeAbaPorPairarFantasmaDoArrasteDoNavegador.ATRASO_MS,
        )
    }

    private fun cancelarAberturaDoChatPorPairarFantasma() {
        val tarefa = aberturaDoChatPorPairarFantasma ?: return
        botaoChatNaBarra.removeCallbacks(tarefa)
        aberturaDoChatPorPairarFantasma = null
    }

    /** Janela flutuante arrastada pela barra até a faixa de abas do chat (o editor de imagem aberto recusa). */
    private fun tratarArrasteDeJanelaPelaBarra(
        xNaTela: Float,
        yNaTela: Float,
        fase: com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.FaseDoArrasteDaJanelaPelaBarra,
    ): Boolean {
        if (!::controladorDasAbasAncoradasNaGaveta.isInitialized) return false
        val aceita = !editorDeImagemAberto()
        return when (fase) {
            com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.FaseDoArrasteDaJanelaPelaBarra.MOVENDO -> {
                if (aceita) considerarAbrirOChatPorPairarFantasmaNoBotao(xNaTela, yNaTela)
                controladorDasAbasAncoradasNaGaveta.arrastandoJanelaFlutuante(xNaTela, yNaTela, aceita)
            }
            com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.FaseDoArrasteDaJanelaPelaBarra.CONSULTAR ->
                controladorDasAbasAncoradasNaGaveta.janelaFlutuanteArmadaSobreAFaixa(xNaTela, yNaTela, aceita)
            com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.FaseDoArrasteDaJanelaPelaBarra.SOLTOU -> {
                cancelarAberturaDoChatPorPairarFantasma()
                controladorDasAbasAncoradasNaGaveta.soltouJanelaFlutuante(xNaTela, yNaTela, aceita)
            }
            com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.FaseDoArrasteDaJanelaPelaBarra.CANCELOU -> {
                cancelarAberturaDoChatPorPairarFantasma()
                controladorDasAbasAncoradasNaGaveta.cancelouArrasteDaJanelaFlutuante()
                false
            }
        }
    }

    private fun soltouSobreOChat(xNaTela: Float, yNaTela: Float): Boolean =
        ::controladorDasAbasAncoradasNaGaveta.isInitialized &&
            controladorDasAbasAncoradasNaGaveta.pontoSobreOChat(xNaTela, yNaTela)

    /** Prefere o arquivo original; senão grava os bytes (ou o que der para ler da uri) no cache e anexa. */
    private fun anexarAoChatPeloArrasteDoFantasma(
        caminhoLocal: String,
        bytes: ByteArray?,
        mime: String?,
        uriTexto: String?,
    ) {
        Thread {
            val original = caminhoLocal.takeIf { it.isNotBlank() }?.let { File(it) }?.takeIf { it.isFile }
            val arquivo = original ?: run {
                val dados = bytes?.takeIf { it.isNotEmpty() }
                    ?: uriTexto?.let { runCatching { leituraDeMidiaDoComponenteDaColecao.ler(it) }.getOrNull() }
                    ?: return@run null
                val extensao = when {
                    mime?.contains("png") == true -> "png"
                    mime?.contains("webp") == true -> "webp"
                    mime?.contains("gif") == true -> "gif"
                    else -> "jpg"
                }
                runCatching {
                    val pasta = File(cacheDir, "imagens-arrastadas-para-o-chat").apply { mkdirs() }
                    File(pasta, "imagem-${System.currentTimeMillis()}.$extensao").apply { writeBytes(dados) }
                }.getOrNull()
            }
            runOnUiThread {
                if (arquivo == null) {
                    Toast.makeText(this, R.string.nao_deu_para_anexar_a_imagem_arrastada_ao_chat, Toast.LENGTH_SHORT).show()
                } else {
                    painelDoChat.anexarArquivoArrastado(Uri.fromFile(arquivo))
                }
            }
        }.start()
    }

    private fun limparEstadoDoPairarAbaNoArrasteDoFantasma() {
        if (::controladorDasAbasAncoradasNaGaveta.isInitialized) {
            controladorDasAbasAncoradasNaGaveta.pararPairarFantasma()
        }
        cancelarAberturaDoChatPorPairarFantasma()
        cancelarAgendamentoDaTrocaDeAbaPorPairarFantasma()
        estadoDoPairarAbaNoArrasteDoFantasma =
            com.chromebrothers.navegador.regras.EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador()
    }

    private fun atualizarCorDoMaisDoFantasmaDoArraste(xNaTela: Float, yNaTela: Float) {
        val mais = maisDoFantasmaDoArrasteDeImagemDoComponente ?: return
        val tipoMcp = if (::painelDaColecao.isInitialized) {
            painelDaColecao.tipoGeradorMcpSobPonto(xNaTela, yNaTela)
        } else {
            ""
        }
        val sobreEditor = if (::painelDaBibliotecaDeVideos.isInitialized) {
            val sobreCamada = painelDaBibliotecaDeVideos.idDaCamadaSobPontoNaTela(xNaTela, yNaTela) != null
            val quadro = painelDaBibliotecaDeVideos.retanguloDoQuadroDoEditorNaTela()
            val sobreMoldura = quadro != null &&
                xNaTela >= quadro.left &&
                xNaTela <= quadro.right &&
                yNaTela >= quadro.top &&
                yNaTela <= quadro.bottom
            val sobreColecaoNaoMcp =
                ::painelDaColecao.isInitialized &&
                    tipoMcp.isEmpty() &&
                    painelDaColecao.pontoSobreAlgumaJanelaDaColecao(xNaTela, yNaTela)
            // Passou sobre a imagem em edição (fora do Visualizador): abre o painel de camadas para soltar numa camada.
            if (sobreMoldura && !sobreColecaoNaoMcp) painelDaBibliotecaDeVideos.abrirCamadasDuranteOArraste()
            !sobreColecaoNaoMcp && (sobreCamada || sobreMoldura)
        } else {
            false
        }
        val podeSoltar =
            com.chromebrothers.navegador.regras.DestinoDoArrasteDoComponenteDaColecaoParaMcpEditorOuPagina
                .corIndicaPodeSoltar(
                    tipoGeradorMcp = tipoMcp,
                    sobreEditor = sobreEditor,
                    editavelProximoNaPagina = editavelProximoNaPaginaNoArrasteDoComponente,
                    salvarNoVisualizador = imagemDoSiteSobreOVisualizador(xNaTela, yNaTela) ||
                        midiaGeradaSobreOVisualizador(xNaTela, yNaTela),
                ) || soltouSobreOChat(xNaTela, yNaTela)
        mais.setTextColor(if (podeSoltar) corMaisArrastePodeSoltar else corMaisArrasteNeutro)
        atualizarZonaDeCancelarDoFantasma(xNaTela, yNaTela, tipoMcp)
    }

    /** Arraste de imagem de site com o dedo sobre um Visualizador de mídia: soltar salva a imagem nele. */
    private fun imagemDoSiteSobreOVisualizador(xNaTela: Float, yNaTela: Float): Boolean =
        com.chromebrothers.navegador.regras.DestinoDoArrasteDoComponenteDaColecaoParaMcpEditorOuPagina
            .deveSalvarNoVisualizador(
                veioDaPagina = arrasteDeImagemVeioDaPagina,
                soltouSobreVisualizador = ::painelDaColecao.isInitialized &&
                    painelDaColecao.pontoSobreVisualizadorDeMidia(xNaTela, yNaTela),
            )

    /** Mídia gerada vinda de fora do Visualizador (MCP, visor): soltar nele move para a pasta pública. */
    private fun midiaGeradaDoArrastePodeMover(uriCheia: String): Boolean =
        !arrasteNasceuNoVisualizador &&
            com.chromebrothers.navegador.regras.MovimentoDaMidiaGeradaParaAGaleria.podeMover(uriCheia)

    private fun midiaGeradaSobreOVisualizador(xNaTela: Float, yNaTela: Float): Boolean {
        val uri = uriDoArrasteDeImagemDoComponente ?: return false
        return com.chromebrothers.navegador.regras.DestinoDoArrasteDoComponenteDaColecaoParaMcpEditorOuPagina
            .deveMoverParaOVisualizador(
                veioDaPagina = arrasteDeImagemVeioDaPagina,
                soltouSobreVisualizador = ::painelDaColecao.isInitialized &&
                    painelDaColecao.pontoSobreVisualizadorDeMidia(xNaTela, yNaTela),
                midiaGeradaPodeMover = midiaGeradaDoArrastePodeMover(uri),
            )
    }

    /** Grava a imagem do site em Pictures/ChromeBrothers (formato original) e atualiza a grade do Visualizador. */
    private fun salvarImagemDoSiteNoVisualizador(bytes: ByteArray?, mime: String?, uriDoArquivo: String) {
        Thread {
            val dados = bytes?.takeIf { it.isNotEmpty() }
                ?: runCatching { leituraDeMidiaDoComponenteDaColecao.ler(uriDoArquivo) }.getOrNull()
            val tipo = mime?.takeIf { it.startsWith("image/") }
                ?: dados?.let { CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.mimeDaImagem(it) }
                ?: "image/jpeg"
            val salvou = dados != null && runCatching {
                GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.salvarNaGaleria(
                    this,
                    dados,
                    tipo,
                    prefixoDoNome = "chromebrothers-imagem-do-site",
                )
            }.isSuccess
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                Toast.makeText(
                    this,
                    if (salvou) R.string.imagem_do_site_salva_no_visualizador else R.string.falha_ao_salvar_imagem_do_site_no_visualizador,
                    Toast.LENGTH_SHORT,
                ).show()
                if (salvou) atualizarVisualizadorDeMidiaComArquivosDoChromeBrothers()
            }
        }.start()
    }

    /**
     * Sobre o Visualizador com o editor aberto (soltar ali cancela): miniatura meio transparente e um
     * toque de vibração ao entrar e ao sair da zona.
     */
    private fun atualizarZonaDeCancelarDoFantasma(xNaTela: Float, yNaTela: Float, tipoMcp: String, vibrar: Boolean = true) {
        val capa = fantasmaDoArrasteDeImagemDoComponente ?: return
        val naZona = com.chromebrothers.navegador.regras.DestinoDoArrasteDoComponenteDaColecaoParaMcpEditorOuPagina
            .estaNaZonaDeCancelar(
                // Imagem do site sobre o Visualizador não cancela: salva. Mídia gerada de fora: move.
                editorDeImagemAberto = editorDeImagemAberto() && !arrasteDeImagemVeioDaPagina &&
                    !midiaGeradaSobreOVisualizador(xNaTela, yNaTela),
                tipoGeradorMcp = tipoMcp,
                sobreJanelaDaColecao = ::painelDaColecao.isInitialized &&
                    painelDaColecao.pontoSobreAlgumaJanelaDaColecao(xNaTela, yNaTela),
            )
        if (naZona == fantasmaNaZonaDeCancelar) return
        fantasmaNaZonaDeCancelar = naZona
        capa.animate().cancel()
        // 0,82 é a opacidade normal do fantasma (criarFantasmaVisualDoArrasteDeImagem).
        capa.animate().alpha(if (naZona) 0.4f else 0.82f).setDuration(120L).start()
        if (vibrar) capa.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
    }

    private fun sondarEditavelProximoNaPaginaParaArrasteDoComponente(xNaTela: Float, yNaTela: Float) {
        if (fantasmaDoArrasteDeImagemDoComponente == null) return
        val ponto = coordenadasLocaisNaPaginaAtiva(xNaTela, yNaTela)
        if (ponto == null) {
            if (editavelProximoNaPaginaNoArrasteDoComponente) {
                editavelProximoNaPaginaNoArrasteDoComponente = false
                atualizarCorDoMaisDoFantasmaDoArraste(xNaTela, yNaTela)
            }
            return
        }
        val agora = android.os.SystemClock.uptimeMillis()
        if (agora - ultimoHorarioDaSondaDoArrasteDoComponenteMs < 70L) return
        ultimoHorarioDaSondaDoArrasteDoComponenteMs = agora
        val (xLocal, yLocal, vista) = ponto
        val geracao = ++geracaoDaSondaDoArrasteDoComponente
        val script = com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina
            .scriptParaSondar(
                xLocalNaView = xLocal,
                yLocalNaView = yLocal,
                larguraDaViewPx = vista.width.toDouble(),
                alturaDaViewPx = vista.height.toDouble(),
            )
        vista.evaluateJavascript(script) { bruto ->
            runOnUiThread {
                if (geracao != geracaoDaSondaDoArrasteDoComponente) return@runOnUiThread
                if (fantasmaDoArrasteDeImagemDoComponente == null) return@runOnUiThread
                val texto = textoDoJavascript(bruto).ifEmpty { bruto?.trim().orEmpty() }
                val ok = com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina
                    .ehResultadoOk(texto)
                if (ok) {
                    teveEditavelNoArrasteDoComponente = true
                    xPaginaOkDoArrasteDoComponente = ultimoXDoArrasteDoComponente
                    yPaginaOkDoArrasteDoComponente = ultimoYDoArrasteDoComponente
                }
                if (ok == editavelProximoNaPaginaNoArrasteDoComponente) return@runOnUiThread
                editavelProximoNaPaginaNoArrasteDoComponente = ok
                atualizarCorDoMaisDoFantasmaDoArraste(ultimoXDoArrasteDoComponente, ultimoYDoArrasteDoComponente)
            }
        }
    }

    private fun encerrarFantasmaDoArrasteDeImagemDoComponente(limparDestaqueDaCarta: Boolean = true) {
        // Sem destino decidido (cancelou, erro, menu): o visor de onde o fantasma saiu volta.
        restaurarVisorAposArraste(fechar = false)
        if (::painelDaBibliotecaDeVideos.isInitialized) painelDaBibliotecaDeVideos.fecharCamadasAbertasPeloArraste()
        geracaoDoArrasteDeImagemDaPagina++
        arrasteDeImagemVeioDaPagina = false
        fantasmaDoArrasteDeImagemDoComponente?.let { capa ->
            (capa.parent as? ViewGroup)?.removeView(capa)
        }
        fantasmaDoArrasteDeImagemDoComponente = null
        fantasmaNaZonaDeCancelar = false
        maisDoFantasmaDoArrasteDeImagemDoComponente = null
        bitmapDoFantasmaDoArrasteDeImagemDoComponente?.let { bmp ->
            if (!bmp.isRecycled) bmp.recycle()
        }
        bitmapDoFantasmaDoArrasteDeImagemDoComponente = null
        uriDoArrasteDeImagemDoComponente = null
        tamanhoDoFantasmaDoArrastePx = 0
        bytesProntosParaColarNoSoltar = null
        bytesProntosSaoMiniatura = false
        mimeProntosParaColarNoSoltar = null
        enderecoOriginalDaImagemDaPaginaNoArraste = null
        idDaAbaDoArrasteDeImagemDaPagina = -1L
        xInicioDoArrasteDeImagemDaPagina = 0f
        yInicioDoArrasteDeImagemDaPagina = 0f
        limparEstadoDoPairarAbaNoArrasteDoFantasma()
        editavelProximoNaPaginaNoArrasteDoComponente = false
        // Mantém teveEditavel/coords até o soltar ler; zera na próxima iniciação.
        geracaoDaSondaDoArrasteDoComponente++
        ultimoHorarioDaSondaDoArrasteDoComponenteMs = 0L
        if (limparDestaqueDaCarta && ::painelDaColecao.isInitialized) {
            painelDaColecao.limparDestaqueDeCartaDoVisualizadorNasJanelas()
        }
    }

    private fun restaurarColecaoAposArrasteDoComponente() {
        if (::painelDaColecao.isInitialized) {
            painelDaColecao.restaurarPaginaAposArrasteDoComponente()
            painelDaColecao.restaurarFocoAposColarNaPagina()
        }
    }

    /** UP nativo e `touchend` do JS podem chegar juntos: só o primeiro solta (evita modal dupla). */
    private fun soltarArrasteDoComponenteUmaVez(uriTexto: String, xNaTela: Float, yNaTela: Float): String {
        if (!soltarDoArrasteDoComponenteJaReivindicado.compareAndSet(false, true)) {
            return com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                .mensagemOk("camada", "ja_solto")
        }
        return soltarArrasteDeImagemDoComponenteParaOEditor(uriTexto, xNaTela, yNaTela)
    }

    private fun soltarArrasteDeImagemDoComponenteParaOEditor(
        uriTexto: String,
        xNaTela: Float,
        yNaTela: Float,
        /** A imagem veio de um site (o sinal arrasteDeImagemVeioDaPagina já foi zerado no fim do gesto). */
        veioDaPagina: Boolean = false,
    ): String {
        val regras = com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
        // Preferir URI cheia guardada no fantasma; senão tirar ?cb_thumb= da grade.
        val uriCheia = uriDoArrasteDeImagemDoComponente
            ?.takeIf { it.isNotBlank() }
            ?: com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
                .uriSemParametroDeMiniatura(uriTexto)
        // Toque longo sem arrastar (coleção/Visualizador) → modal própria, como imagem de site.
        if (!veioDaPagina && !arrasteDeImagemVeioDaPagina) {
            val abrirModal = com.chromebrothers.navegador.regras.DecisaoDoToqueLongoEmImagemDaPaginaEntreModalEArraste
                .deveAbrirModalEmVezDeSoltarArraste(
                    xInicio = xInicioDoArrasteDeImagemDoComponente,
                    yInicio = yInicioDoArrasteDeImagemDoComponente,
                    xFim = xNaTela,
                    yFim = yNaTela,
                )
            if (abrirModal) {
                runOnUiThread {
                    encerrarFantasmaDoArrasteDeImagemDoComponente(limparDestaqueDaCarta = false)
                    restaurarColecaoAposArrasteDoComponente()
                    mostrarOpcoesDaMidiaDoComponenteNoToqueLongo(uriCheia)
                }
                return regras.mensagemOk("modal", "opcoes")
            }
        }
        if (!regras.esquemaAceito(uriCheia)) {
            runOnUiThread { encerrarFantasmaDoArrasteDeImagemDoComponente() }
            return regras.mensagemErro("Uri não suportada.")
        }
        if (!::painelDaBibliotecaDeVideos.isInitialized) {
            runOnUiThread { encerrarFantasmaDoArrasteDeImagemDoComponente() }
            return regras.mensagemErro("A biblioteca ainda não está pronta.")
        }
        val latch = java.util.concurrent.CountDownLatch(1)
        var idCamadaSobPonto: Int? = null
        var destino = com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.CANCELAR
        var tipoMcp = ""
        var painelMcp: PainelDaColecaoFlutuanteDoChatDoNavegador? = null
        var anexouNoChat = false
        runOnUiThread {
            try {
                if (soltouSobreOChat(xNaTela, yNaTela)) {
                    val bytes = bytesProntosParaColarNoSoltar
                    val mime = mimeProntosParaColarNoSoltar ?: mimeDaUriDeMidiaDoComponente(uriCheia)
                    restaurarVisorAposArraste(fechar = true)
                    encerrarFantasmaDoArrasteDeImagemDoComponente()
                    restaurarColecaoAposArrasteDoComponente()
                    anexarAoChatPeloArrasteDoFantasma(caminhoLocalDeUriDeMidiaDoComponente(uriCheia), bytes, mime, uriCheia)
                    anexouNoChat = true
                    return@runOnUiThread
                }
                // Só o verde do momento (como a biblioteca). teveEditavel grudava e colava com + amarelo.
                val verdeIndicavaColar = editavelProximoNaPaginaNoArrasteDoComponente
                tipoMcp = if (::painelDaColecao.isInitialized) {
                    painelDaColecao.tipoGeradorMcpSobPonto(xNaTela, yNaTela)
                } else {
                    ""
                }
                painelMcp = if (tipoMcp.isNotEmpty() && ::painelDaColecao.isInitialized) {
                    painelDaColecao.painelSobPonto(xNaTela, yNaTela)
                } else {
                    null
                }
                idCamadaSobPonto = painelDaBibliotecaDeVideos.idDaCamadaSobPontoNaTela(xNaTela, yNaTela)
                val quadro = painelDaBibliotecaDeVideos.retanguloDoQuadroDoEditorNaTela()
                val soltouSobreMoldura = quadro != null &&
                    xNaTela >= quadro.left &&
                    xNaTela <= quadro.right &&
                    yNaTela >= quadro.top &&
                    yNaTela <= quadro.bottom
                val soltouSobreColecaoNaoMcp =
                    ::painelDaColecao.isInitialized &&
                        tipoMcp.isEmpty() &&
                        painelDaColecao.pontoSobreAlgumaJanelaDaColecao(xNaTela, yNaTela)
                val coordsNaPagina = coordenadasLocaisNaPaginaAtiva(xNaTela, yNaTela)
                destino = com.chromebrothers.navegador.regras.DestinoDoArrasteDoComponenteDaColecaoParaMcpEditorOuPagina
                    .decidir(
                        tipoGeradorMcp = tipoMcp,
                        soltouSobreLinhaDeCamada = idCamadaSobPonto != null,
                        soltouSobreMoldura = soltouSobreMoldura,
                        soltouSobreJanelaDaColecaoNaoMcp = soltouSobreColecaoNaoMcp,
                        editavelProximoNaPagina = verdeIndicavaColar,
                        pontoSobrePagina = coordsNaPagina != null && !soltouSobreColecaoNaoMcp,
                        veioDaPagina = veioDaPagina,
                        soltouSobreVisualizador = ::painelDaColecao.isInitialized &&
                            painelDaColecao.pontoSobreVisualizadorDeMidia(xNaTela, yNaTela),
                        midiaGeradaPodeMover = midiaGeradaDoArrastePodeMover(uriCheia),
                    )
                // Ainda é a miniatura (soltou rápido ou imagem > 20 MB): sem bytes, o colar lê a imagem cheia pela uri.
                val bytesParaColar = if (bytesProntosSaoMiniatura) null else bytesProntosParaColarNoSoltar
                val mimeParaColar = (if (bytesProntosSaoMiniatura) null else mimeProntosParaColarNoSoltar)
                    ?: mimeDaUriDeMidiaDoComponente(uriCheia)
                restaurarVisorAposArraste(
                    fechar = destino != com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.CANCELAR,
                )
                encerrarFantasmaDoArrasteDeImagemDoComponente()
                when (destino) {
                    com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.REFERENCIA_MCP -> {
                        restaurarColecaoAposArrasteDoComponente()
                        val painel = painelMcp
                        if (painel != null) {
                            val caminho = caminhoLocalDeUriDeMidiaDoComponente(uriCheia)
                            soltarItemDaBibliotecaComoReferenciaDaGeracaoNucleo(
                                painel = painel,
                                estado = EstadoDoArrasteDaBiblioteca("image", caminho, uriCheia),
                                tipoDoGerador = tipoMcp,
                                uri = runCatching { Uri.parse(uriCheia) }.getOrNull(),
                                mime = mimeDaUriDeMidiaDoComponente(uriCheia),
                            )
                        }
                    }
                    com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.COLAR_PAGINA -> {
                        // Verde + soltar: copia agora e cola no próximo tick do foco/Ctrl+V.
                        copiarParaAreaDeTransferenciaEColarNaPagina(
                            bytes = bytesParaColar,
                            mime = mimeParaColar,
                            uriFallback = uriCheia,
                            xNaTela = xNaTela,
                            yNaTela = yNaTela,
                            coordsFixas = coordsNaPagina,
                            forcarColarPeloVerde = true,
                            liberarFocoDaColecao = true,
                            atrasoAntesDoToqueMs = 160L,
                        )
                    }
                    com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.SALVAR_NO_VISUALIZADOR -> {
                        restaurarColecaoAposArrasteDoComponente()
                        salvarImagemDoSiteNoVisualizador(bytesParaColar, mimeParaColar, uriCheia)
                    }
                    com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.MOVER_PARA_O_VISUALIZADOR -> {
                        restaurarColecaoAposArrasteDoComponente()
                        moverMidiasGeradasParaAGaleria(listOf(uriCheia))
                    }
                    com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.FUNDIR_EDITOR,
                    com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.CANCELAR,
                    -> restaurarColecaoAposArrasteDoComponente()
                }
            } finally {
                latch.countDown()
            }
        }
        latch.await(2, java.util.concurrent.TimeUnit.SECONDS)
        if (anexouNoChat) return regras.mensagemOk("camada", "anexo_do_chat")
        return when (destino) {
            com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.FUNDIR_EDITOR ->
                enviarImagemDoComponenteParaCamadaDoEditor(
                    uriCheia,
                    idCamadaDestino = idCamadaSobPonto ?: -1,
                )
            com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.REFERENCIA_MCP ->
                regras.mensagemOk("camada", "referencia_mcp")
            com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.SALVAR_NO_VISUALIZADOR ->
                regras.mensagemOk("camada", "salvo_no_visualizador")
            com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.MOVER_PARA_O_VISUALIZADOR ->
                regras.mensagemOk("camada", "movido_para_o_visualizador")
            com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.COLAR_PAGINA ->
                regras.mensagemOk("camada", "colar_pagina")
            com.chromebrothers.navegador.regras.DestinoDoSoltarDoArrasteDoComponenteDaColecao.CANCELAR ->
                regras.mensagemOk("camada", "cancelado")
        }
    }

    private fun caminhoLocalDeUriDeMidiaDoComponente(uriTexto: String): String {
        if (!uriTexto.startsWith("file://", ignoreCase = true)) return ""
        return runCatching { Uri.parse(uriTexto).path.orEmpty() }.getOrDefault("")
    }

    private fun enviarImagemDoComponenteParaCamadaDoEditor(
        uriTexto: String,
        idCamadaDestino: Int = -1,
    ): String {
        val regras = com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
        val uriCheia = com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
            .uriSemParametroDeMiniatura(uriTexto)
        if (!regras.esquemaAceito(uriCheia)) {
            return regras.mensagemErro("Uri não suportada.")
        }
        if (!::painelDaBibliotecaDeVideos.isInitialized) {
            return regras.mensagemErro("A biblioteca ainda não está pronta.")
        }
        val bytes = leituraDeMidiaDoComponenteDaColecao.ler(uriCheia)
        if (bytes == null) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    R.string.falha_ao_ler_imagem_do_componente_para_o_editor,
                    Toast.LENGTH_SHORT,
                ).show()
            }
            return regras.mensagemErro("Não deu para ler a imagem.")
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (bitmap == null) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    R.string.falha_ao_ler_imagem_do_componente_para_o_editor,
                    Toast.LENGTH_SHORT,
                ).show()
            }
            return regras.mensagemErro("Esse arquivo não parece uma imagem.")
        }
        val latch = java.util.concurrent.CountDownLatch(1)
        var resultado =
            com.chromebrothers.navegador.regras.ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor
                .RECUSAR_SEM_DESTINO
        var falhou = false
        runOnUiThread {
            try {
                resultado = painelDaBibliotecaDeVideos.enviarBitmapDoComponenteParaCamadaAtiva(
                    bitmap,
                    idCamadaDestino = idCamadaDestino,
                )
                if (
                    resultado ==
                    com.chromebrothers.navegador.regras
                        .ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
                ) {
                    Toast.makeText(
                        this,
                        R.string.recusa_envio_de_imagem_do_componente_sem_camada_no_editor,
                        Toast.LENGTH_SHORT,
                    ).show()
                    if (!bitmap.isRecycled) bitmap.recycle()
                }
            } catch (_: Exception) {
                falhou = true
                if (!bitmap.isRecycled) bitmap.recycle()
            } finally {
                latch.countDown()
            }
        }
        latch.await(8, java.util.concurrent.TimeUnit.SECONDS)
        if (falhou) return regras.mensagemErro("Não deu para enviar a imagem.")
        return when (resultado) {
            com.chromebrothers.navegador.regras
                .ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO ->
                regras.mensagemErro("Selecione uma camada de imagem no editor.")
            com.chromebrothers.navegador.regras
                .ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM,
            com.chromebrothers.navegador.regras
                .ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.PROMOVER_FOLHA_E_FUNDIR,
            -> regras.mensagemOk("camada", "ok")
        }
    }

    private fun guardarImagemDoComponenteNaBiblioteca(uriTexto: String): String {
        val regras = com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
        if (!regras.esquemaAceito(uriTexto)) {
            return regras.mensagemErro("Uri não suportada.")
        }
        if (!::bibliotecaDeImagens.isInitialized) {
            return regras.mensagemErro("A biblioteca ainda não está pronta.")
        }
        val bytes = leituraDeMidiaDoComponenteDaColecao.ler(uriTexto)
            ?: return regras.mensagemErro("Não deu para ler a imagem.")
        val latch = java.util.concurrent.CountDownLatch(1)
        var itemId: String? = null
        runOnUiThread {
            try {
                val item = gravacaoDaBibliotecaDeImagens.guardarQuadro(
                    bibliotecaDeImagens,
                    bytes,
                    naGaleria = false,
                )
                itemId = item?.id
                if (item != null && painelDaBibliotecaDeVideos.estaAberto()) {
                    painelDaBibliotecaDeVideos.atualizar()
                }
            } finally {
                latch.countDown()
            }
        }
        latch.await(8, java.util.concurrent.TimeUnit.SECONDS)
        val id = itemId ?: return regras.mensagemErro("Não deu para guardar a imagem.")
        return regras.mensagemOk(
            com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao.Destino.BIBLIOTECA.rotulo,
            "Guardei a imagem na biblioteca ($id).",
        )
    }

    private fun copiarImagemDoComponenteParaAreaDeTransferencia(uriTexto: String): String {
        val regras = com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
        if (!regras.esquemaAceito(uriTexto)) {
            return regras.mensagemErro("Uri não suportada.")
        }
        val bytes = leituraDeMidiaDoComponenteDaColecao.ler(uriTexto)
            ?: return regras.mensagemErro("Não deu para ler a imagem.")
        val mime = CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.mimeDaImagem(bytes)
        val latch = java.util.concurrent.CountDownLatch(1)
        var ok = false
        runOnUiThread {
            try {
                GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.copiarParaAreaDeTransferencia(
                    this,
                    bytes,
                    mime,
                )
                Toast.makeText(this, R.string.quadro_copiado, Toast.LENGTH_LONG).show()
                ok = true
            } catch (_: Exception) {
                ok = false
            } finally {
                latch.countDown()
            }
        }
        latch.await(8, java.util.concurrent.TimeUnit.SECONDS)
        if (!ok) return regras.mensagemErro("Não deu para copiar a imagem.")
        return regras.mensagemOk(
            com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao.Destino.COPIAR.rotulo,
            "Imagem copiada.",
        )
    }

    private fun abrirVideoDoComponenteNoPlayer(uriTexto: String): String {
        val regras = com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
        if (!regras.esquemaAceito(uriTexto)) {
            return regras.mensagemErro("Uri não suportada.")
        }
        val titulo = uriTexto.substringBefore('?').substringAfterLast('/').ifBlank { "vídeo" }
        val latch = java.util.concurrent.CountDownLatch(1)
        runOnUiThread {
            try {
                abrirArquivoBaixado(uriTexto.trim(), titulo)
            } finally {
                latch.countDown()
            }
        }
        latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
        return regras.mensagemOk(
            com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao.Destino.PLAYER.rotulo,
            "Abri o vídeo no player.",
        )
    }

    private fun capturarComponenteParaOChat(
        id: String,
        aoReceber: (com.chromebrothers.navegador.regras.ParteDeConteudoDoChat?, String) -> Unit,
    ) {
        if (!::bibliotecaDeComponentesDaColecao.isInitialized) {
            aoReceber(null, "A biblioteca de componentes ainda não está pronta.")
            return
        }
        val item = bibliotecaDeComponentesDaColecao.buscar(id)
        if (item == null) {
            aoReceber(null, "Não achei esse componente na biblioteca.")
            return
        }
        if (!painelDaColecao.focarSeOrigemFor(id)) {
            val mensagem = painelDaColecao.carregarComponenteDaBiblioteca(item)
            if (mensagem == LimiteDeJanelasFlutuantesDaColecaoDoChat.mensagemRecusada()) {
                aoReceber(null, mensagem)
                return
            }
        }
        painelDaColecao.quandoAPaginaPuderSerCapturada {
            val bitmap = painelDaColecao.bitmapVisivelDaWebView()
            if (bitmap == null || bitmap.isRecycled) {
                aoReceber(null, "Não deu para capturar o componente.")
                return@quandoAPaginaPuderSerCapturada
            }
            Thread {
                val preparada = CompactacaoDoBitmapDaCapturaDaAbaParaOChatDoNavegador.preparar(this, bitmap)
                val relato = if (preparada == null) {
                    "Não deu para preparar a captura."
                } else {
                    com.chromebrothers.navegador.regras.PreparacaoDaCapturaDaAbaParaOChat.relatoDoComponente(
                        preparada.largura,
                        preparada.altura,
                        preparada.enderecoArquivo,
                    )
                }
                if (!bitmap.isRecycled) bitmap.recycle()
                runOnUiThread {
                    if (!isDestroyed) aoReceber(preparada?.parte, relato)
                }
            }.start()
        }
    }

    private fun descreverVideoDetectadoParaOChat(): String {
        val pagina = paginas[gestao.idDaAbaAtiva]
        val video = pagina?.videoAtual
        val pode = DecisaoDeMostrarBotaoFlutuanteDoVideo.mostrar(
            temVideoTocando = video != null,
            temLimitesNaTela = pagina?.temLimitesDeVideo == true,
        )
        return com.chromebrothers.navegador.regras.FerramentasDeLeituraDoChatDaOpenRouter
            .textoDoVideoDetectadoNaAba(video?.endereco, pode)
    }

    private fun listarVideosDaBibliotecaParaOChat(): String {
        if (!::bibliotecaDeVideos.isInitialized) return "A biblioteca de vídeos ainda não está pronta."
        return com.chromebrothers.navegador.regras.FerramentasDeLeituraDoChatDaOpenRouter
            .textoDosVideosDaBiblioteca(bibliotecaDeVideos.recentes())
    }

    private fun listarImagensDaBibliotecaParaOChat(): String {
        if (!::bibliotecaDeImagens.isInitialized) return "A biblioteca de imagens ainda não está pronta."
        return com.chromebrothers.navegador.regras.FerramentasDeLeituraDoChatDaOpenRouter
            .textoDasImagensDaBiblioteca(bibliotecaDeImagens.historico())
    }

    private fun mostrarBotaoFlutuanteDoVideoPeloChat(): String {
        val pagina = paginas[gestao.idDaAbaAtiva] ?: return "Nenhum vídeo detectado nesta aba."
        if (pagina.videoAtual == null) return "Nenhum vídeo detectado nesta aba."
        aplicarPosicaoDoBotao(pagina)
        return if (botaoFlutuante.visibility == View.VISIBLE) {
            "Mostrei o botão flutuante do vídeo."
        } else {
            "Há um vídeo detectado, mas o botão flutuante não apareceu nesta tela."
        }
    }

    private fun abrirPlayerDoVideoDetectadoPeloChat(): String {
        val pagina = paginas[gestao.idDaAbaAtiva] ?: return "Nenhum vídeo detectado nesta aba."
        if (pagina.videoAtual == null) return "Nenhum vídeo detectado nesta aba."
        abrirPlayerNativo()
        return "Abri o player do vídeo detectado."
    }

    private fun abrirVideoDaBibliotecaPeloChat(chave: String): String {
        if (!::bibliotecaDeVideos.isInitialized) return "A biblioteca de vídeos ainda não está pronta."
        val item = bibliotecaDeVideos.buscar(chave) ?: return "Não achei esse vídeo na biblioteca."
        if (item.abreNoPlayerDeArquivo && paginas[gestao.idDaAbaAtiva] == null) {
            return "Não há página aberta."
        }
        abrirItemDaBiblioteca(item)
        return "Abri \"${item.titulo}\" da biblioteca."
    }

    private fun abrirImagemDaBibliotecaPeloChat(id: String): String {
        if (!::painelDaBibliotecaDeVideos.isInitialized || !::bibliotecaDeImagens.isInitialized) {
            return "A biblioteca de imagens ainda não está pronta."
        }
        return painelDaBibliotecaDeVideos.abrirImagemDaBiblioteca(id)
    }

    private fun bytesDaImagemDaBibliotecaParaOChat(id: String): Pair<ByteArray, String>? {
        if (!::bibliotecaDeImagens.isInitialized || !::gravacaoDaBibliotecaDeImagens.isInitialized) {
            return null
        }
        val item = bibliotecaDeImagens.buscar(id.trim()) ?: return null
        val bytes = gravacaoDaBibliotecaDeImagens.ler(item.nomeDoArquivo) ?: return null
        return bytes to item.nomeDoArquivo
    }

    private fun rodarRoteiroDeAutomacaoPeloChat(
        idOuNome: String,
        aoPasso: (indice: Int, total: Int, rotulo: String) -> Unit = { _, _, _ -> },
        aoFim: (String) -> Unit,
    ) {
        if (!::gravacaoDosRoteirosDeAutomacao.isInitialized) {
            aoFim("Os roteiros ainda não estão prontos.")
            return
        }
        val roteiro = gravacaoDosRoteirosDeAutomacao.buscar(idOuNome)
        if (roteiro == null) {
            aoFim("Não achei o roteiro «$idOuNome».")
            return
        }
        executarRoteiroDeAutomacaoEmMemoria(roteiro, aoPasso = aoPasso, aoFim = aoFim)
    }

    private fun urlsDoRoteiroDeAutomacaoEquivalentes(a: String, b: String): Boolean {
        fun normalizar(u: String): String =
            u.trim().lowercase().substringBefore('#').trimEnd('/')
        val na = normalizar(a)
        val nb = normalizar(b)
        if (na.isEmpty() || nb.isEmpty()) return false
        if (na == nb) return true
        return na.substringBefore('?') == nb.substringBefore('?')
    }

    private fun executarRoteiroDeAutomacaoEmMemoria(
        roteiro: com.chromebrothers.navegador.regras.RoteiroDeAutomacaoDaPagina,
        aoPasso: ((indice: Int, total: Int, rotulo: String) -> Unit)? = null,
        aoFim: (String) -> Unit,
        variaveis: Map<String, String> = emptyMap(),
    ) {
        executorDoRoteiroEmAndamento?.cancelar()
        val executor = ExecutorDosRoteirosDeAutomacaoDaPaginaDoNavegador(
            handler = handlerDaEsperaDoChat,
            abrirEndereco = { url, aoAbrir ->
                val endereco = InterpretacaoDoTextoDaBarraDeEndereco.resolver(url, preferencias.motor)
                if (!endereco.startsWith("http://") && !endereco.startsWith("https://")) {
                    aoAbrir("Não abri: só uso http ou https.")
                } else {
                    val id = gestao.idDaAbaAtiva
                    val atual = (
                        paginas[id]?.webView?.url
                            ?: gestao.ativa()?.endereco.orEmpty()
                        ).trim()
                    // Mesma página (ou só query diferente): não espera reload eterno.
                    if (urlsDoRoteiroDeAutomacaoEquivalentes(atual, endereco) ||
                        urlsDoRoteiroDeAutomacaoEquivalentes(atual, url)
                    ) {
                        aoAbrir("Já estava na página.")
                    } else {
                        var concluido = false
                        fun terminar(msg: String) {
                            if (concluido) return
                            concluido = true
                            aoAbrir(msg)
                        }
                        acompanharCarregamento(id, jaViuComeco = false) { espera ->
                            terminar(relatoDoCarregamento(id, espera, criouAba = false))
                        }
                        // Rede/SPA às vezes não dispara “fim”; não trava o roteiro.
                        handlerDaEsperaDoChat.postDelayed({
                            terminar("Timeout ao abrir; sigo mesmo assim.")
                        }, 25_000L)
                        navegarNaAba(id, url)
                    }
                }
            },
            avaliarJavascript = { script, aoReceber ->
                val vista = paginas[gestao.idDaAbaAtiva]?.webView
                if (vista == null) {
                    aoReceber("")
                } else {
                    vista.evaluateJavascript(script) { bruto ->
                        runOnUiThread { aoReceber(bruto.orEmpty()) }
                    }
                }
            },
            textoDoJavascript = { bruto -> textoDoJavascript(bruto) },
            urlAtual = {
                paginas[gestao.idDaAbaAtiva]?.webView?.url
                    ?: gestao.ativa()?.endereco.orEmpty()
            },
            aoPasso = aoPasso,
            variaveisIniciais = variaveis,
        )
        executorDoRoteiroEmAndamento = executor
        executor.executar(roteiro) { relato ->
            if (executorDoRoteiroEmAndamento === executor) {
                executorDoRoteiroEmAndamento = null
            }
            aoFim(relato)
        }
    }

    private fun abrirItemDaBiblioteca(item: ItemDaBibliotecaDeVideos) {
        painelDaBibliotecaDeVideos.esconder()
        if (painelDoPlayerDaPagina.visibility == View.VISIBLE) sairDoPlayerDaPagina()
        val endereco = item.enderecoParaAbrir.trim()
        // Vídeos gerados / baixados (file://, content:// ou caminho absoluto) vão direto ao player.
        if (enderecoEhArquivoLocalDeVideo(endereco)) {
            abrirArquivoBaixado(uriDeArquivoLocalParaOPlayer(endereco), item.titulo)
            return
        }
        if (item.abreNoPlayerDeArquivo) {
            val pagina = paginas[gestao.idDaAbaAtiva]
            if (pagina != null) {
                abrirPlayerComEndereco(pagina, endereco, 1.0, item.titulo)
            } else if (DecisaoDeAbrirVideoNoPlayerNativo.podeAbrir(endereco)) {
                abrirArquivoBaixado(endereco, item.titulo)
            } else {
                avisar(R.string.player_nativo_indisponivel)
            }
        } else {
            navegarNaAba(gestao.idDaAbaAtiva, endereco)
        }
    }

    private fun enderecoEhArquivoLocalDeVideo(endereco: String): Boolean {
        val t = endereco.trim()
        return t.startsWith("file:", ignoreCase = true) ||
            t.startsWith("content:", ignoreCase = true) ||
            (t.startsWith("/") && (
                t.endsWith(".mp4", true) || t.endsWith(".webm", true) ||
                    t.endsWith(".mkv", true) || t.endsWith(".mov", true)
                ))
    }

    private fun uriDeArquivoLocalParaOPlayer(endereco: String): String {
        val t = endereco.trim()
        if (t.startsWith("file:", ignoreCase = true) || t.startsWith("content:", ignoreCase = true)) {
            return t
        }
        if (t.startsWith("/")) return File(t).toURI().toString()
        return t
    }

    private fun registrarVideoDaPaginaNaBiblioteca(pagina: PaginaDeUmaAbaDoNavegadorChromebrothers) {
        val video = pagina.videoAtual ?: return
        val identidade = MontagemDaIdentidadeDoVideoNaBiblioteca.montar(
            pagina.webView.url.orEmpty(),
            video.endereco,
            pagina.webView.title.orEmpty(),
        ) ?: return
        val item = bibliotecaDeVideos.registrar(
            identidade.chave,
            identidade.titulo,
            identidade.enderecoParaAbrir,
            identidade.abreNoPlayerDeArquivo,
            System.currentTimeMillis(),
        )
        gravacaoDaBibliotecaDeVideos.gravar(bibliotecaDeVideos)
        chaveDoVideoNoPlayerDaPagina = item.chave
        atualizarEstrelaDoPlayerDaPagina()
    }

    private fun alternarFavoritoDoVideoNoPlayerDaPagina() {
        val chave = chaveDoVideoNoPlayerDaPagina ?: return
        val atual = bibliotecaDeVideos.buscar(chave) ?: return
        bibliotecaDeVideos.definirFavorito(chave, !atual.favorito)
        gravacaoDaBibliotecaDeVideos.gravar(bibliotecaDeVideos)
        atualizarEstrelaDoPlayerDaPagina()
        if (painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.atualizar()
    }

    private fun atualizarEstrelaDoPlayerDaPagina() {
        val favorito = chaveDoVideoNoPlayerDaPagina?.let { bibliotecaDeVideos.buscar(it)?.favorito } == true
        val botao = findViewById<ImageButton>(R.id.botao_favoritar_o_video_da_biblioteca)
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

    private fun agendarMiniaturaDoPlayerDaPagina() {
        tentativasDeMiniaturaDoPlayerDaPagina = 0
        relogioDoPlayerDaPagina.removeCallbacks(capturarMiniaturaDoPlayerDaPagina)
        relogioDoPlayerDaPagina.postDelayed(capturarMiniaturaDoPlayerDaPagina, 900)
    }

    private fun tentarGuardarMiniaturaDoPlayerDaPagina() {
        val chave = chaveDoVideoNoPlayerDaPagina ?: return
        if (painelDoPlayerDaPagina.visibility != View.VISIBLE) return
        val idVideo = idDoVideoNoPlayerDaPagina ?: return
        val pagina = idDaAbaNoPlayerDaPagina?.let { paginas[it] } ?: return
        val idCitado = JSONObject.quote(idVideo)
        pagina.webView.evaluateJavascript(
            chamadaDeCapturaDeQuadro(idCitado, LimiteDaLarguraDaCapturaDeQuadroDoVideo.MINIATURA),
        ) { bruto ->
            if (chaveDoVideoNoPlayerDaPagina != chave) return@evaluateJavascript
            val png = pngDoDataUrl(textoDoJavascript(bruto))
            val bitmap = png?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            val guardou = bitmap != null && guardarMiniaturaDaBiblioteca(chave, bitmap)
            if (bitmap != null && !bitmap.isRecycled) bitmap.recycle()
            if (
                !guardou &&
                tentativasDeMiniaturaDoPlayerDaPagina < 1 &&
                painelDoPlayerDaPagina.visibility == View.VISIBLE
            ) {
                tentativasDeMiniaturaDoPlayerDaPagina += 1
                relogioDoPlayerDaPagina.postDelayed(capturarMiniaturaDoPlayerDaPagina, 1200)
            }
        }
    }

    private fun guardarMiniaturaDaBiblioteca(chave: String, bitmap: Bitmap): Boolean {
        val nome = gravacaoDaBibliotecaDeVideos.salvarMiniatura(chave, bitmap) ?: return false
        bibliotecaDeVideos.definirMiniatura(chave, nome)
        gravacaoDaBibliotecaDeVideos.gravar(bibliotecaDeVideos)
        if (painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.atualizar()
        return true
    }

    /** Semente idempotente: atualiza HTML empacotado se o seed já existir. */
    private fun semearComponenteDeGeracaoDeImagemViaMcpSeAusente() {
        val idFixo = com.chromebrothers.navegador.regras.HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.ID
        val htmlEmpacotado =
            com.chromebrothers.navegador.regras.HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html()
        val existente = bibliotecaDeComponentesDaColecao.buscar(idFixo)
        if (existente != null && existente.html == htmlEmpacotado) return
        val resultado = bibliotecaDeComponentesDaColecao.guardar(
            nome = com.chromebrothers.navegador.regras.HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.NOME,
            html = htmlEmpacotado,
            idExistente = existente?.id,
            agora = System.currentTimeMillis(),
            tags = com.chromebrothers.navegador.regras.HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.TAGS,
            novoId = { idFixo },
        )
        if (resultado is com.chromebrothers.navegador.regras.ResultadoDaBibliotecaDeComponentesHtml.Ok) {
            gravacaoDaBibliotecaDeComponentes.gravar(bibliotecaDeComponentesDaColecao)
        }
    }

    private fun semearComponenteDeGeracaoDeVideoViaMcpSeAusente() {
        val idFixo = com.chromebrothers.navegador.regras.HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.ID
        val htmlEmpacotado =
            com.chromebrothers.navegador.regras.HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.html()
        val existente = bibliotecaDeComponentesDaColecao.buscar(idFixo)
        if (existente != null && existente.html == htmlEmpacotado) return
        val resultado = bibliotecaDeComponentesDaColecao.guardar(
            nome = com.chromebrothers.navegador.regras.HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.NOME,
            html = htmlEmpacotado,
            idExistente = existente?.id,
            agora = System.currentTimeMillis(),
            tags = com.chromebrothers.navegador.regras.HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.TAGS,
            novoId = { idFixo },
        )
        if (resultado is com.chromebrothers.navegador.regras.ResultadoDaBibliotecaDeComponentesHtml.Ok) {
            gravacaoDaBibliotecaDeComponentes.gravar(bibliotecaDeComponentesDaColecao)
        }
    }

    /**
     * Atualiza o Visualizador existente (por id fixo ou pelo nome legado) com o HTML nativo de galeria.
     */
    private fun semearComponenteDoVisualizadorDeMidiaSeAusente() {
        val idFixo = com.chromebrothers.navegador.regras.HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.ID
        val htmlEmpacotado =
            com.chromebrothers.navegador.regras.HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.html()
        val existente = bibliotecaDeComponentesDaColecao.buscar(idFixo)
            ?: bibliotecaDeComponentesDaColecao.listar().firstOrNull {
                com.chromebrothers.navegador.regras.PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
                    .ehVisualizadorDeMidiaPeloNome(it.nome)
            }
        if (existente != null && existente.html == htmlEmpacotado) return
        val resultado = bibliotecaDeComponentesDaColecao.guardar(
            nome = com.chromebrothers.navegador.regras.HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.NOME,
            html = htmlEmpacotado,
            idExistente = existente?.id,
            agora = System.currentTimeMillis(),
            tags = com.chromebrothers.navegador.regras.HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.TAGS,
            novoId = { idFixo },
        )
        if (resultado is com.chromebrothers.navegador.regras.ResultadoDaBibliotecaDeComponentesHtml.Ok) {
            gravacaoDaBibliotecaDeComponentes.gravar(bibliotecaDeComponentesDaColecao)
        }
    }

    /**
     * Gera em disco as miniaturas das mídias que a grade do Visualizador vai mostrar,
     * para a WebView só ler arquivos prontos ao rolar. Já existentes são pulados.
     */
    /** Menu: apaga as listas de modelos guardadas e busca de novo em todos os MCPs com login. */
    private fun atualizarListasDeModelosDosMcps() {
        Toast.makeText(this, R.string.atualizando_listas_de_modelos, Toast.LENGTH_SHORT).show()
        servicoDeGeracaoDeImagemViaMcpDaColecao.apagarEBuscarCatalogosDeNovo { ok, total ->
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                val texto = when {
                    total == 0 -> getString(R.string.listas_de_modelos_sem_login)
                    ok == total -> getString(R.string.listas_de_modelos_atualizadas)
                    else -> getString(R.string.listas_de_modelos_atualizadas_em_parte, ok, total)
                }
                Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pregerarMiniaturasDoVisualizadorDeMidiaEmSegundoPlano() {
        if (!pregeracaoDeMiniaturasEmAndamento.compareAndSet(false, true)) return
        Thread {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            val lado = com.chromebrothers.navegador.regras.HtmlDoComponenteDoVisualizadorDeMidiaNaColecao
                .LADO_DA_MINIATURA_DA_GRADE
            val pool = java.util.concurrent.Executors.newFixedThreadPool(3)
            try {
                val itens = runCatching { leituraDaPastaVirtualDeMidiaDoChromeBrothers.listarExibidos() }
                    .getOrDefault(emptyList())
                itens.filter { !it.pasta }.forEach { item ->
                    pool.execute {
                        GradeNativaDoVisualizadorDeMidiaNaColecaoDoNavegador.aquecerNaMemoria(
                            geradorDeMiniaturasDaGradeDoVisualizador,
                            item,
                            lado,
                        )
                    }
                }
                // Miniaturas do modal "Biblioteca" das referências MCP: só em disco (memória só ao abrir).
                runCatching { gravacaoDaBibliotecaDeImagens.carregar().listar() }.getOrDefault(emptyList())
                    .forEach { item ->
                        val arquivo = gravacaoDaBibliotecaDeImagens.arquivo(item.nomeDoArquivo)
                        pool.execute { MiniaturasDaEscolhaDaBiblioteca.pregerarNoDisco(geradorDeMiniaturasDaGradeDoVisualizador, arquivo) }
                    }
                pool.shutdown()
                pool.awaitTermination(10, java.util.concurrent.TimeUnit.MINUTES)
            } finally {
                pool.shutdownNow()
                pregeracaoDeMiniaturasEmAndamento.set(false)
            }
        }.start()
    }

    /**
     * Empurra Downloads + Pictures/ChromeBrothers para janelas do Visualizador abertas.
     * Miniaturas que ainda não estão na memória são geradas antes, para a carta nova já nascer preenchida.
     */
    private fun atualizarVisualizadorDeMidiaComArquivosDoChromeBrothers() {
        if (!::painelDaColecao.isInitialized) return
        Thread {
            val regra = com.chromebrothers.navegador.regras.AtualizacaoAoVivoDaGradeDoVisualizadorDeMidiaAoChegarArquivoNovo
            val lado = com.chromebrothers.navegador.regras.HtmlDoComponenteDoVisualizadorDeMidiaNaColecao
                .LADO_DA_MINIATURA_DA_GRADE
            val itens = runCatching { leituraDaPastaVirtualDeMidiaDoChromeBrothers.listarExibidos() }
                .getOrDefault(emptyList())
            val porUri = itens.associateBy { it.uri }
            regra.urisParaAquecerAntesDeMostrar(
                itens.map { it.uri },
                GradeNativaDoVisualizadorDeMidiaNaColecaoDoNavegador::temNaMemoria,
            ).forEach { uri ->
                val item = porUri[uri] ?: return@forEach
                GradeNativaDoVisualizadorDeMidiaNaColecaoDoNavegador.aquecerNaMemoria(
                    geradorDeMiniaturasDaGradeDoVisualizador,
                    item,
                    lado,
                )
            }
            val json = runCatching { leituraDaPastaVirtualDeMidiaDoChromeBrothers.descrever() }.getOrNull()
                ?: return@Thread
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                painelDaColecao.entregarPastaVirtualDeMidiaDoChromeBrothersAosVisualizadores(json)
                pregerarMiniaturasDoVisualizadorDeMidiaEmSegundoPlano()
            }
        }.start()
    }

    /**
     * Exclui os arquivos e, se algum era mídia gerada via MCP, tira também a entrada das bibliotecas
     * (senão a gaveta da biblioteca ficaria com um item apontando para arquivo inexistente).
     */
    /** Tela de Armazenamento: usa a mesma lista e a mesma exclusão da mídia do visualizador (índices em ordem). */
    private fun abrirArmazenamentoDoAplicativo() {
        PainelDoArmazenamentoDoAplicativo(
            activity = this,
            medicao = MedicaoELimpezaDoArmazenamentoDoAplicativo(this),
            listarMidias = { listarMidiasGeradasViaMcp() },
            excluirMidias = { uris ->
                val json = uris.joinToString(",", "[", "]") { "\"" + it.replace("\\", "\\\\").replace("\"", "\\\"") + "\"" }
                excluirArquivosDoVisualizadorDeMidia(json)
            },
            moverMidias = { uris, aoTerminar -> moverMidiasGeradasParaAGaleria(uris, aoTerminar) },
            aoMudar = { if (::painelDoChat.isInitialized) painelDoChat.atualizarResumoDoArmazenamento() },
        ).mostrar()
    }

    /**
     * "Mover para a Galeria": copia para Movies/Pictures/ChromeBrothers, confere e só então apaga a
     * cópia interna (pela mesma rotina do excluir, que mantém as bibliotecas em ordem).
     * [aoTerminar] roda na thread principal.
     */
    private fun moverMidiasGeradasParaAGaleria(uris: List<String>, aoTerminar: (() -> Unit)? = null) {
        if (uris.isEmpty()) return
        Toast.makeText(this, if (uris.size == 1) "Movendo para a Galeria…" else "Movendo ${uris.size} arquivos para a Galeria…", Toast.LENGTH_SHORT).show()
        Thread {
            val resultado = MovimentoDaMidiaGeradaParaAGaleriaDoAparelho(this).mover(uris) { novos ->
                // Cards dos geradores passam a apontar para a cópia na Galeria (antes de o original sumir).
                val cardsMudados = runCatching {
                    servicoDeGeracaoDeImagemViaMcpDaColecao.acompanharArquivosMovidos(novos)
                }.getOrDefault(emptyList())
                if (cardsMudados.isNotEmpty() && ::painelDaColecao.isInitialized) {
                    runOnUiThread {
                        cardsMudados.forEach { (tipo, json) -> painelDaColecao.entregarCardAtualizadoAosGeradores(tipo, json) }
                    }
                }
                excluirArquivosDoVisualizadorDeMidia(org.json.JSONArray().apply { novos.keys.forEach { put(it) } }.toString())
            }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                Toast.makeText(
                    this,
                    com.chromebrothers.navegador.regras.MovimentoDaMidiaGeradaParaAGaleria
                        .resumo(resultado.movidos.size, resultado.falhas.size),
                    Toast.LENGTH_SHORT,
                ).show()
                atualizarVisualizadorDeMidiaComArquivosDoChromeBrothers()
                aoTerminar?.invoke()
            }
        }.start()
    }

    private fun excluirArquivosDoVisualizadorDeMidia(urisJson: String?): String {
        val (resposta, bibliotecasMudaram) = ExclusaoDaMidiaComLimpezaDasBibliotecas.excluir(this, urisJson)
        if (bibliotecasMudaram) runOnUiThread { sincronizarBibliotecaDeVideos() }
        return resposta
    }

    /** Lê as bibliotecas do disco (chamado fora da thread principal). */
    private fun listarMidiasGeradasViaMcp(): List<com.chromebrothers.navegador.regras.ArquivoDeMidiaSalvoNaGaveta> {
        val regra = com.chromebrothers.navegador.regras.MidiaGeradaViaMcpListadaNoVisualizadorDeMidiaDaColecao
        val imagens = runCatching {
            GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo(this).carregar().listar()
        }.getOrDefault(emptyList())
        val videos = runCatching {
            GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo(this).carregar().listar()
        }.getOrDefault(emptyList())
        return regra.listar(
            imagens = imagens,
            videos = videos,
            pastaDasImagens = File(filesDir, regra.PASTA_DAS_IMAGENS).absolutePath,
            tamanhoDoArquivo = { caminho -> File(caminho).takeIf { it.isFile }?.length() ?: -1L },
        )
    }

    private val avisoDeMidiaNovaNoAparelho = Runnable { atualizarVisualizadorDeMidiaComArquivosDoChromeBrothers() }

    /** Juntamos avisos em rajada (download em lote, IS_PENDING → pronto) numa releitura só. */
    private fun agendarAtualizacaoDoVisualizadorPorMidiaNova() {
        val raiz = window?.decorView ?: return
        raiz.removeCallbacks(avisoDeMidiaNovaNoAparelho)
        raiz.postDelayed(
            avisoDeMidiaNovaNoAparelho,
            com.chromebrothers.navegador.regras.AtualizacaoAoVivoDaGradeDoVisualizadorDeMidiaAoChegarArquivoNovo
                .ESPERA_PARA_JUNTAR_AVISOS_MS,
        )
    }

    private val observadorDeMidiaNovaNoAparelho by lazy {
        object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                agendarAtualizacaoDoVisualizadorPorMidiaNova()
            }
        }
    }

    private fun registrarObservadorDeMidiaNovaNoAparelho() {
        val volume = MediaStore.VOLUME_EXTERNAL_PRIMARY
        listOf(
            MediaStore.Downloads.getContentUri(volume),
            MediaStore.Images.Media.getContentUri(volume),
            MediaStore.Video.Media.getContentUri(volume),
        ).forEach { uri ->
            runCatching { contentResolver.registerContentObserver(uri, true, observadorDeMidiaNovaNoAparelho) }
        }
    }

    private fun sincronizarBibliotecaDeVideos() {
        if (!::bibliotecaDeVideos.isInitialized || !::gravacaoDaBibliotecaDeVideos.isInitialized) return
        bibliotecaDeVideos.substituirPor(gravacaoDaBibliotecaDeVideos.carregar().listar())
        if (::bibliotecaDeImagens.isInitialized && ::gravacaoDaBibliotecaDeImagens.isInitialized) {
            bibliotecaDeImagens.substituirPor(gravacaoDaBibliotecaDeImagens.carregar().listar())
        }
        if (::painelDaBibliotecaDeVideos.isInitialized && painelDaBibliotecaDeVideos.estaAberto()) {
            painelDaBibliotecaDeVideos.atualizar()
        }
        if (painelDoPlayerDaPagina.visibility == View.VISIBLE) atualizarEstrelaDoPlayerDaPagina()
    }

    private fun registrarQuadroCapturado(
        png: ByteArray,
        naGaleria: Boolean,
    ): com.chromebrothers.navegador.regras.ItemDaBibliotecaDeImagensCapturadas? {
        if (!::bibliotecaDeImagens.isInitialized) return null
        val item = gravacaoDaBibliotecaDeImagens.guardarQuadro(bibliotecaDeImagens, png, naGaleria)
        if (painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.atualizar()
        if (naGaleria) atualizarVisualizadorDeMidiaComArquivosDoChromeBrothers()
        return item
    }

    private fun receberImagemDaPagina(bytes: ByteArray, destino: DestinoDaImagemDaPagina) {
        if (destino == DestinoDaImagemDaPagina.EXTRAIR_TEXTO) {
            orquestradorDaExtracaoDeTextoDeImagem.extrairDeBytes(bytes)
            return
        }
        if (destino == DestinoDaImagemDaPagina.EDITAR || destino == DestinoDaImagemDaPagina.ACRESCENTAR) {
            abrirImagemDaPaginaNoEditor(bytes, destino)
            return
        }
        if (!::bibliotecaDeImagens.isInitialized) return
        val naGaleria = destino == DestinoDaImagemDaPagina.SALVAR
        try {
            val publicada = if (naGaleria) {
                GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.salvarNaGaleria(
                    this,
                    bytes,
                    CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.mimeDaImagem(bytes),
                )
            } else {
                null
            }
            val item = gravacaoDaBibliotecaDeImagens.guardarQuadro(bibliotecaDeImagens, bytes, naGaleria)
                ?: run {
                    avisar(R.string.imagem_da_pagina_indisponivel)
                    return
                }
            if (publicada != null) {
                gravacaoDaBibliotecaDeImagens.lembrarGaleria(
                    bibliotecaDeImagens,
                    item.id,
                    publicada.uri.toString(),
                    publicada.nome,
                )
                avisar(R.string.imagem_da_pagina_salva)
            } else {
                gravacaoDaBibliotecaDeImagens.copiarItem(this, bibliotecaDeImagens, item)
                avisar(R.string.imagem_da_pagina_copiada)
            }
            if (painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.atualizar()
        } catch (_: Exception) {
            avisar(R.string.imagem_da_pagina_indisponivel)
        }
    }

    private fun abrirImagemDaPaginaNoEditor(bytes: ByteArray, destino: DestinoDaImagemDaPagina) {
        if (!::painelDaBibliotecaDeVideos.isInitialized) {
            avisar(R.string.imagem_da_pagina_indisponivel)
            return
        }
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (bitmap == null) {
            avisar(R.string.imagem_da_pagina_indisponivel)
            return
        }
        try {
            if (!painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.mostrar()
            if (destino == DestinoDaImagemDaPagina.EDITAR) {
                painelDaBibliotecaDeVideos.editarImagemVindaDaPagina(bitmap)
                return
            }
            when (painelDaBibliotecaDeVideos.acrescentarImagemNaUltimaEdicao(bitmap)) {
                EntradaDaImagemNaEdicao.ENTROU -> Unit
                EntradaDaImagemNaEdicao.SEM_EDICAO -> avisar(R.string.nenhuma_edicao_para_receber_a_imagem)
                EntradaDaImagemNaEdicao.SEM_ESPACO -> avisar(R.string.limite_de_imagens_da_composicao)
                EntradaDaImagemNaEdicao.INDISPONIVEL -> Unit
            }
        } catch (_: Exception) {
            if (!bitmap.isRecycled) bitmap.recycle()
            avisar(R.string.imagem_da_pagina_indisponivel)
        }
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

    private fun gravarQuadro(png: ByteArray) {
        try {
            val publicada = GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.salvarECopiar(this, png)
            val item = registrarQuadroCapturado(png, naGaleria = true)
            if (item != null) {
                gravacaoDaBibliotecaDeImagens.lembrarGaleria(
                    bibliotecaDeImagens,
                    item.id,
                    publicada.uri.toString(),
                    publicada.nome,
                )
            }
            avisar(R.string.quadro_salvo_e_copiado)
        } catch (_: Exception) {
            avisar(R.string.quadro_indisponivel)
        }
    }

    private fun respostaDeAnuncioBloqueado(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            204,
            "No Content",
            mapOf("Access-Control-Allow-Origin" to "*"),
            ByteArrayInputStream(ByteArray(0)),
        )
    }

    private fun avisar(mensagem: Int) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
    }

    private fun aplicarMargemDasBarrasDoSistema(idDaRaiz: Int) {
        val raiz = findViewById<View>(idDaRaiz)
        // Só insets (adjustNothing): evita flash branco do resize do Gecko.
        // Automático: pan pela medida do foco + clipping; se a medida falhar, sobe a folga.
        var animandoTeclado = false
        var medidaDoFoco: PosicaoDoCampoFocadoParaOTecladoDoNavegador.Medida? = null
        var pedidoDaMedidaDoFoco = 0
        var tentativasDeMedidaDoFoco = 0
        var estadoDaMedidaDoFoco = EstadoDaMedidaDoFocoParaOTeclado.AGUARDANDO
        lateinit var aplicarInsetsDaPagina: (View, WindowInsetsCompat) -> Unit

        fun medirCampoFocadoParaOTeclado() {
            val vista = paginas[gestao.idDaAbaAtiva]?.webView ?: run {
                estadoDaMedidaDoFoco = EstadoDaMedidaDoFocoParaOTeclado.FALHOU
                return
            }
            val idDoPedido = ++pedidoDaMedidaDoFoco
            tentativasDeMedidaDoFoco += 1
            estadoDaMedidaDoFoco = EstadoDaMedidaDoFocoParaOTeclado.AGUARDANDO
            vista.evaluateJavascript(PosicaoDoCampoFocadoParaOTecladoDoNavegador.script()) { bruto ->
                if (idDoPedido != pedidoDaMedidaDoFoco) return@evaluateJavascript
                val texto = textoDoJavascript(bruto).ifEmpty { bruto?.trim().orEmpty() }
                val medida = PosicaoDoCampoFocadoParaOTecladoDoNavegador.lerMedida(texto)
                if (medida != null) {
                    medidaDoFoco = medida
                    estadoDaMedidaDoFoco = EstadoDaMedidaDoFocoParaOTeclado.PRONTA
                    ViewCompat.getRootWindowInsets(raiz)?.let { aplicarInsetsDaPagina(raiz, it) }
                    return@evaluateJavascript
                }
                if (tentativasDeMedidaDoFoco < 4) {
                    raiz.postDelayed({
                        if (idDoPedido != pedidoDaMedidaDoFoco) return@postDelayed
                        if (medidaDoFoco != null) return@postDelayed
                        val aindaTeclado = ViewCompat.getRootWindowInsets(raiz)
                            ?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
                        if (aindaTeclado > 0) medirCampoFocadoParaOTeclado()
                    }, 80L)
                } else {
                    estadoDaMedidaDoFoco = EstadoDaMedidaDoFocoParaOTeclado.FALHOU
                    ViewCompat.getRootWindowInsets(raiz)?.let { aplicarInsetsDaPagina(raiz, it) }
                }
            }
        }

        aplicarInsetsDaPagina = { view, insets ->
            // Player sobreposto aberto: as barras do sistema somem, mas a página mantém o tamanho (margens
            // congeladas, faixas pretas). Redimensionar a página fazia o player de sites como o Boosty se
            // remontar e soltar o vídeo que está no palco (ficava preto).
            val congeladas = barrasCongeladasPeloPlayerDaPagina
            val barras = congeladas ?: insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val paddingInferior = EspacoDoTecladoSobreAPaginaDoNavegador.paddingInferiorDaRaiz(
                barraDoSistema = barras.bottom,
            )
            val paleta = paletaAtual()
            view.setBackgroundColor(if (congeladas != null) Color.BLACK else paleta.fundoDaFaixa)
            view.setPadding(barras.left, barras.top, barras.right, paddingInferior)
            val extra = EspacoDoTecladoSobreAPaginaDoNavegador.folgaExtra(
                barraDoSistema = barras.bottom,
                teclado = teclado,
            )
            val url = paginas[gestao.idDaAbaAtiva]?.webView?.url.orEmpty()
            val modoTeclado = preferencias.modoDoTecladoParaEndereco(url)
            val focoChrome = focoDoTecladoNoChromeNativo()
            if (extra == 0) {
                medidaDoFoco = null
                tentativasDeMedidaDoFoco = 0
                estadoDaMedidaDoFoco = EstadoDaMedidaDoFocoParaOTeclado.AGUARDANDO
                // Usuário fechou o teclado nesta aba: não reabrir ao voltar.
                paginas[gestao.idDaAbaAtiva]?.deveRestaurarTecladoAoFocar = false
            } else if (
                !focoChrome &&
                modoTeclado == ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO &&
                medidaDoFoco == null &&
                tentativasDeMedidaDoFoco == 0 &&
                estadoDaMedidaDoFoco != EstadoDaMedidaDoFocoParaOTeclado.FALHOU
            ) {
                medirCampoFocadoParaOTeclado()
            }
            val altura = if (::areaVisivel.isInitialized) areaVisivel.height.toFloat() else 0f
            val automatico = EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoVerticalComFocoNoViewport(
                baseDoFocoCss = medidaDoFoco?.baseParaPan(),
                alturaDoViewportCss = medidaDoFoco?.viewportCss,
                folgaDoTecladoPx = extra.toFloat(),
                alturaDaAreaPx = altura,
                margemCss = 12f,
            )
            val ajuste = ComportamentoDoTecladoPorSiteDoNavegador.ajusteVisual(
                modo = modoTeclado,
                folgaDoTecladoPx = extra.toFloat(),
                deslocamentoAutomatico = automatico,
                estadoDaMedidaDoFoco = estadoDaMedidaDoFoco,
                hospedeiro = ComportamentoDoTecladoPorSiteDoNavegador.hospedeiro(url),
                focoNoChromeNativo = focoChrome,
            )
            if (::areaVisivel.isInitialized) {
                areaVisivel.clipChildren = true
                areaVisivel.setBackgroundColor(paleta.fundoDaPagina)
            }
            if (::areaDasPaginas.isInitialized) {
                areaDasPaginas.setBackgroundColor(paleta.fundoDaPagina)
                areaDasPaginas.translationY = ajuste.deslocamentoY
            }
            paginas[gestao.idDaAbaAtiva]?.webView?.definirClippingVerticalInferior(
                ajuste.clippingInferiorPx,
            )
            if (::painelDoCampoDeTexto.isInitialized) painelDoCampoDeTexto.ajustarAoTeclado(extra)
            if (::painelDoChat.isInitialized) painelDoChat.ajustarAoTeclado(extra)
        }

        ViewCompat.setOnApplyWindowInsetsListener(raiz) { view, insets ->
            if (!animandoTeclado) aplicarInsetsDaPagina(view, insets)
            insets
        }
        ViewCompat.setWindowInsetsAnimationCallback(
            raiz,
            object : WindowInsetsAnimationCompat.Callback(
                DISPATCH_MODE_CONTINUE_ON_SUBTREE,
            ) {
                override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                    if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
                        animandoTeclado = true
                        medidaDoFoco = null
                        tentativasDeMedidaDoFoco = 0
                        estadoDaMedidaDoFoco = EstadoDaMedidaDoFocoParaOTeclado.AGUARDANDO
                        val url = paginas[gestao.idDaAbaAtiva]?.webView?.url.orEmpty()
                        if (!focoDoTecladoNoChromeNativo() &&
                            preferencias.modoDoTecladoParaEndereco(url) ==
                            ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO
                        ) {
                            medirCampoFocadoParaOTeclado()
                        }
                    }
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>,
                ): WindowInsetsCompat {
                    if (runningAnimations.any { it.typeMask and WindowInsetsCompat.Type.ime() != 0 }) {
                        aplicarInsetsDaPagina(raiz, insets)
                    }
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    if (animation.typeMask and WindowInsetsCompat.Type.ime() == 0) return
                    animandoTeclado = false
                    ViewCompat.getRootWindowInsets(raiz)?.let { aplicarInsetsDaPagina(raiz, it) }
                }
            },
        )
    }

    companion object {
        fun textoDoJavascript(bruto: String?): String {
            if (bruto.isNullOrBlank() || bruto == "null") return ""
            return try {
                JSONArray("[$bruto]").getString(0)
            } catch (_: Exception) {
                ""
            }
        }

        fun pngDoDataUrl(texto: String): ByteArray? {
            val marcador = "base64,"
            val indice = texto.indexOf(marcador)
            val imagem = texto.startsWith("data:image/png") || texto.startsWith("data:image/jpeg")
            if (!imagem || indice < 0) return null
            return try {
                Base64.decode(texto.substring(indice + marcador.length), Base64.DEFAULT)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}

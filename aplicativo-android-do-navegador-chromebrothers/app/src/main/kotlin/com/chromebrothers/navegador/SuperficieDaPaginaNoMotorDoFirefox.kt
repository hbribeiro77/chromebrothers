package com.chromebrothers.navegador

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.FrameLayout
import com.chromebrothers.navegador.regras.ModoDeApresentacaoDoSite
import com.chromebrothers.navegador.regras.TipoDeErroDeCarregamento
import com.chromebrothers.navegador.regras.RoteamentoDosComandosDoVideoParaOQuadroDeOrigemNaPagina
import org.json.JSONArray
import org.json.JSONObject
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.NavigationDelegate
import org.mozilla.geckoview.GeckoSession.ProgressDelegate
import org.mozilla.geckoview.GeckoSession.PromptDelegate
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtensionController
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class SuperficieDaPaginaNoMotorDoFirefox(
    context: Context,
    sessaoExistente: GeckoSession? = null,
) : FrameLayout(context) {
    val sessao: GeckoSession = sessaoExistente ?: GeckoSession()

    /** Pede ao site para sair da tela cheia (voltar do Android, troca de aba). */
    fun sairDaTelaCheia() {
        if (emTelaCheia) sessao.exitFullScreen()
    }
    private val sessaoJaAberta = sessaoExistente != null
    private val visor = GeckoView(context)
    private val dialogos = DialogosDaPaginaNoMotorDoFirefox(this)
    private val permissoes = PermissoesDaPaginaNoMotorDoFirefox(this)

    /** A atividade, só se a página está mesmo à vista (a reserva fica "visível" fora da tela). */
    fun atividadeParaDialogos(): android.app.Activity? {
        val atividade = context as? android.app.Activity ?: return null
        val aVista = isShown && getGlobalVisibleRect(android.graphics.Rect())
        return atividade.takeIf { !destruida && aVista && !it.isFinishing && !it.isDestroyed }
    }

    var url: String? = null
        private set
    var title: String? = null
        private set
    var podeVoltar = false
        private set
    var podeAvancar = false
        private set
    var aoComecarPagina: ((String) -> Unit)? = null
    var aoTerminarPagina: ((String, Boolean) -> Unit)? = null
    var aoProgresso: ((Int) -> Unit)? = null
    var aoTitulo: ((String?) -> Unit)? = null
    var aoHistorico: (() -> Unit)? = null
    var aoFechar: (() -> Unit)? = null
    /** O site pediu (true) ou largou (false) a tela cheia (vídeo do YouTube etc.). */
    var aoMudarTelaCheia: ((Boolean) -> Unit)? = null
    var emTelaCheia = false
        private set
    var deveCancelarNavegacao: ((String, Boolean, Boolean, Boolean) -> Boolean)? = null
    var aoPromptDaPagina: ((String) -> String?)? = null
    var aoPedidoDeArquivo: ((List<String>, Boolean, Boolean, PromptDelegate.FilePrompt, GeckoResult<PromptDelegate.PromptResponse>) -> Unit)? = null
    var aoRespostaExterna: ((org.mozilla.geckoview.WebResponse) -> Unit)? = null
    var aoMenuDeContexto: ((String) -> Unit)? = null
    var aoMenuDeLink: ((String) -> Unit)? = null
    var aoNovaSessao: ((GeckoSession, String) -> Unit)? = null
    var aoInstalarExtensaoDaLoja: ((String) -> Unit)? = null
    var aoAvisoDaPagina: ((String, JSONArray) -> Unit)? = null
    val settings = AjustesDaSuperficie()
    private val filaDeJavascript = ArrayDeque<Pair<Long, String>>()
    private val retornosDeJavascript = mutableMapOf<Long, (String) -> Unit>()
    private val portasDosQuadros = mutableListOf<WebExtension.Port>()
    private val scriptsParaTodosOsQuadros = mutableListOf<String>()
    private var proximoJavascript = 1L
    private var portaDaPagina: WebExtension.Port? = null
    private val descobrirCapaSePresa = Runnable { descobrirCapaDaPrimeiraPintura() }

    /**
     * Chamado depois que o processo da página morreu (Android matou por CPU/memória ou o
     * Gecko travou) e a sessão foi reaberta. Recebe a URL anterior e se foi travamento.
     * Devolve true se quem chamou já recarregou a página por conta própria.
     */
    var aoRecuperarDaPerdaDoProcesso: ((String?, Boolean) -> Boolean)? = null

    /** URL mudou (inclui navegação dentro de sites como ChatGPT/X, que não recarregam a página). */
    var aoMudarEndereco: ((String) -> Unit)? = null

    /** O Gecko publicou um estado novo da sessão (histórico, rolagem, formulários). */
    var aoMudarEstadoDaSessao: (() -> Unit)? = null
    private var estadoDaSessao: GeckoSession.SessionState? = null
    private var ativa = false
    private var destruida = false
    private var processoPerdido = false
    private var perdaFoiTravamento = false

    init {
        visor.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        // Evita piscada branca da surface antes da 1ª pintura da página.
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        addView(visor)
        sessao.progressDelegate = object : ProgressDelegate {
            override fun onPageStart(session: GeckoSession, urlPagina: String) {
                dialogos.aoComecarPagina()
                permissoes.aoComecarPagina()
                if (!urlPagina.startsWith("data:") && urlPagina != "about:blank") url = urlPagina
                val atual = url.orEmpty()
                reaplicarPrioridade()
                post { aoComecarPagina?.invoke(atual) }
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                reaplicarPrioridade()
                val atual = url.orEmpty()
                post { aoTerminarPagina?.invoke(atual, success) }
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                post { aoProgresso?.invoke(progress) }
            }

            override fun onSessionStateChange(session: GeckoSession, sessionState: GeckoSession.SessionState) {
                post {
                    estadoDaSessao = sessionState
                    aoMudarEstadoDaSessao?.invoke()
                }
            }
        }
        sessao.contentDelegate = object : GeckoSession.ContentDelegate {
            // Processo da aba morto pelo sistema: a sessão fica fechada e a área da página preta.
            override fun onKill(session: GeckoSession) {
                post { registrarPerdaDoProcesso(travamento = false) }
            }

            override fun onCrash(session: GeckoSession) {
                post { registrarPerdaDoProcesso(travamento = true) }
            }

            override fun onTitleChange(session: GeckoSession, titulo: String?) {
                title = titulo
                post { aoTitulo?.invoke(titulo) }
            }

            override fun onCloseRequest(session: GeckoSession) {
                post { aoFechar?.invoke() }
            }

            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                post {
                    if (emTelaCheia == fullScreen) return@post
                    emTelaCheia = fullScreen
                    aoMudarTelaCheia?.invoke(fullScreen)
                }
            }

            override fun onExternalResponse(session: GeckoSession, response: org.mozilla.geckoview.WebResponse) {
                post { aoRespostaExterna?.invoke(response) }
            }

            override fun onContextMenu(
                session: GeckoSession,
                screenX: Int,
                screenY: Int,
                element: GeckoSession.ContentDelegate.ContextElement,
            ) {
                val ehImagem = element.type == GeckoSession.ContentDelegate.ContextElement.TYPE_IMAGE
                if (ehImagem) {
                    val endereco = element.srcUri?.takeIf { it.isNotBlank() }
                        ?: element.linkUri?.takeIf { it.isNotBlank() }
                        ?: return
                    post { aoMenuDeContexto?.invoke(endereco) }
                    return
                }
                val link = com.chromebrothers.navegador.regras.MenuDoToqueLongoNoLinkDaPagina.endereco(
                    ehImagem = false,
                    link = element.linkUri,
                ) ?: return
                post { aoMenuDeLink?.invoke(link) }
            }
        }
        sessao.navigationDelegate = object : NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                urlPagina: String?,
                perms: List<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean,
            ) {
                if (!urlPagina.isNullOrBlank() && !urlPagina.startsWith("data:") && urlPagina != "about:blank") {
                    url = urlPagina
                    post { aoMudarEndereco?.invoke(urlPagina) }
                }
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                podeVoltar = canGoBack
                post { aoHistorico?.invoke() }
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                podeAvancar = canGoForward
                post { aoHistorico?.invoke() }
            }

            // Sem isto o Gecko não mostra nada quando o carregamento falha: a aba ficava em branco
            // (certificado inválido) ou na página anterior com o endereço novo na barra (site inexistente).
            override fun onLoadError(
                session: GeckoSession,
                uri: String?,
                error: org.mozilla.geckoview.WebRequestError,
            ): GeckoResult<String>? {
                val tipo = tipoDoErro(error)
                Log.i(TAG_SUPERFICIE, "falha ao carregar ($tipo, código ${error.code}): $uri")
                val escura = PreferenciasVisuaisEDeNavegacaoDoChromebrothers(context).aparenciaEscura
                return GeckoResult.fromValue(
                    com.chromebrothers.navegador.regras.PaginaDeErroDeCarregamentoDoNavegador.enderecoDaPagina(tipo, uri, escura),
                )
            }

            override fun onLoadRequest(
                session: GeckoSession,
                request: NavigationDelegate.LoadRequest,
            ): GeckoResult<AllowOrDeny> {
                val destino = request.uri
                val cancelar = deveCancelarNavegacao?.invoke(
                    destino,
                    request.hasUserGesture,
                    request.isRedirect,
                    request.isDirectNavigation,
                ) == true
                return GeckoResult.fromValue(if (cancelar) AllowOrDeny.DENY else AllowOrDeny.ALLOW)
            }

            override fun onNewSession(
                session: GeckoSession,
                uri: String,
            ): GeckoResult<GeckoSession> {
                val filha = GeckoSession(session.settings)
                if (com.chromebrothers.navegador.regras.EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.eh(uri)) {
                    filha.navigationDelegate = object : NavigationDelegate {
                        override fun onLoadRequest(
                            session: GeckoSession,
                            request: NavigationDelegate.LoadRequest,
                        ): GeckoResult<AllowOrDeny> {
                            post { filha.close() }
                            return GeckoResult.fromValue(AllowOrDeny.DENY)
                        }
                    }
                    post { aoInstalarExtensaoDaLoja?.invoke(uri) }
                    return GeckoResult.fromValue(filha)
                }
                post { aoNovaSessao?.invoke(filha, uri) }
                return GeckoResult.fromValue(filha)
            }
        }
        sessao.permissionDelegate = permissoes
        sessao.promptDelegate = dialogos
        val motor = MotorDoFirefoxDoNavegadorChromebrothers.obter(context)
        if (!sessaoJaAberta) sessao.open(motor)
        visor.setSession(sessao)
        MotorDoFirefoxDoNavegadorChromebrothers.registrarSessao(sessao)
        aplicarModo(ModoDeApresentacaoDoSite.TABLET)
        definirProtecaoContraRastreamento(true)
        MotorDoFirefoxDoNavegadorChromebrothers.quandoExtensaoPronta { extensao ->
            post { ligarPonteDeJavascript(extensao) }
        }
    }

    fun loadUrl(endereco: String) {
        url = endereco
        sessao.loadUri(endereco)
    }

    fun loadDataWithBaseURL(
        base: String?,
        data: String,
        mime: String,
        encoding: String?,
        history: String?,
    ) {
        url = base
        val documento = if (base.isNullOrBlank() || data.contains("<base ", ignoreCase = true)) {
            data
        } else {
            data.replaceFirst("<head>", "<head><base href=\"$base\">")
        }
        sessao.load(
            GeckoSession.Loader().uri(
                com.chromebrothers.navegador.regras.EnderecoDoDocumentoHtmlEmbutidoNoFirefox.montar(
                    documento,
                    mime.ifBlank { "text/html" },
                ),
            ),
        )
    }

    fun reload() {
        sessao.reload()
    }

    /** Estado da sessão em texto (JSON do Gecko) para salvar a aba; null se ainda não houver. */
    fun estadoDaSessaoComoTexto(): String? = estadoDaSessao?.toString()

    /** Reabre a aba exatamente onde estava (com voltar/avançar). Devolve false se o texto não servir. */
    fun restaurarEstadoDaSessao(texto: String, enderecoAnterior: String?): Boolean {
        val estado = runCatching { GeckoSession.SessionState.fromString(texto) }.getOrNull() ?: return false
        // Estado gravado antes de a página sair do about:blank (app fechado logo depois de navegar):
        // restaurá-lo deixaria a aba vazia com o endereço na barra. Quem chamou carrega o endereço.
        val atualNoEstado = runCatching { estado[estado.currentIndex].uri }.getOrNull()
        if ((atualNoEstado.isNullOrBlank() || atualNoEstado == "about:blank") &&
            !enderecoAnterior.isNullOrBlank() && enderecoAnterior != "about:blank"
        ) {
            return false
        }
        estadoDaSessao = estado
        if (!enderecoAnterior.isNullOrBlank()) url = enderecoAnterior
        sessao.restoreState(estado)
        return true
    }

    fun stopLoading() {
        sessao.stop()
    }

    fun goBack() {
        sessao.goBack()
    }

    fun goForward() {
        sessao.goForward()
    }

    fun canGoBack(): Boolean = podeVoltar

    fun canGoForward(): Boolean = podeAvancar

    fun capturarImagemDaPagina(aoReceber: (Bitmap?) -> Unit) {
        try {
            visor.capturePixels().accept(
                { bitmap -> post { aoReceber(bitmap) } },
                { _ -> post { aoReceber(null) } },
            )
        } catch (_: RuntimeException) {
            aoReceber(null)
        }
    }

    fun buscarNaPagina(
        texto: String,
        paraTras: Boolean,
        diferenciarMaiusculas: Boolean = false,
        aoResultado: (encontrado: Boolean, atual: Int, total: Int) -> Unit,
    ) {
        val procura = texto.trim()
        if (procura.isEmpty()) {
            limparBuscaNaPagina()
            aoResultado(false, 0, 0)
            return
        }
        var flags = if (paraTras) {
            GeckoSession.FINDER_FIND_BACKWARDS
        } else {
            GeckoSession.FINDER_FIND_FORWARD
        }
        if (diferenciarMaiusculas) {
            flags = flags or GeckoSession.FINDER_FIND_MATCH_CASE
        }
        val finder = sessao.finder
        finder.displayFlags = GeckoSession.FINDER_DISPLAY_HIGHLIGHT_ALL
        finder.find(procura, flags).accept(
            { resultado ->
                post {
                    if (resultado == null) {
                        aoResultado(false, 0, 0)
                    } else {
                        aoResultado(resultado.found, resultado.current, resultado.total)
                    }
                }
            },
            { _ -> post { aoResultado(false, 0, 0) } },
        )
    }

    fun limparBuscaNaPagina() {
        runCatching { sessao.finder.clear() }
    }

    fun onResume() {
        ativa = true
        if (!destruida && processoPerdido) {
            recuperarSessaoAposPerdaDoProcesso()
            return
        }
        sessao.setActive(true)
    }

    fun onPause() {
        ativa = false
        if (sessao.isOpen) sessao.setActive(false)
    }

    /**
     * Aba escondida vai para o cgroup "background" do Android (só os 4 núcleos lentos, 1,78 GHz).
     * Com prioridade alta o processo dela continua com os núcleos rápidos mesmo invisível.
     */
    private fun tipoDoErro(erro: org.mozilla.geckoview.WebRequestError): TipoDeErroDeCarregamento {
        val e = org.mozilla.geckoview.WebRequestError::class.java
        fun c(nome: String) = runCatching { e.getField(nome).getInt(null) }.getOrDefault(Int.MIN_VALUE)
        return when (erro.code) {
            org.mozilla.geckoview.WebRequestError.ERROR_OFFLINE -> TipoDeErroDeCarregamento.SEM_INTERNET
            org.mozilla.geckoview.WebRequestError.ERROR_UNKNOWN_HOST -> TipoDeErroDeCarregamento.ENDERECO_NAO_ENCONTRADO
            org.mozilla.geckoview.WebRequestError.ERROR_CONNECTION_REFUSED -> TipoDeErroDeCarregamento.CONEXAO_RECUSADA
            org.mozilla.geckoview.WebRequestError.ERROR_NET_TIMEOUT -> TipoDeErroDeCarregamento.TEMPO_ESGOTADO
            org.mozilla.geckoview.WebRequestError.ERROR_NET_INTERRUPT,
            org.mozilla.geckoview.WebRequestError.ERROR_NET_RESET -> TipoDeErroDeCarregamento.CONEXAO_INTERROMPIDA
            org.mozilla.geckoview.WebRequestError.ERROR_SECURITY_BAD_CERT,
            org.mozilla.geckoview.WebRequestError.ERROR_BAD_HSTS_CERT -> TipoDeErroDeCarregamento.CERTIFICADO_INVALIDO
            org.mozilla.geckoview.WebRequestError.ERROR_SECURITY_SSL -> TipoDeErroDeCarregamento.CONEXAO_INSEGURA
            org.mozilla.geckoview.WebRequestError.ERROR_HTTPS_ONLY -> TipoDeErroDeCarregamento.SO_HTTPS
            org.mozilla.geckoview.WebRequestError.ERROR_MALFORMED_URI,
            org.mozilla.geckoview.WebRequestError.ERROR_DATA_URI_TOO_LONG -> TipoDeErroDeCarregamento.ENDERECO_INVALIDO
            org.mozilla.geckoview.WebRequestError.ERROR_UNKNOWN_PROTOCOL,
            org.mozilla.geckoview.WebRequestError.ERROR_UNKNOWN_SOCKET_TYPE -> TipoDeErroDeCarregamento.PROTOCOLO_DESCONHECIDO
            org.mozilla.geckoview.WebRequestError.ERROR_FILE_NOT_FOUND,
            org.mozilla.geckoview.WebRequestError.ERROR_FILE_ACCESS_DENIED -> TipoDeErroDeCarregamento.ARQUIVO_NAO_ENCONTRADO
            org.mozilla.geckoview.WebRequestError.ERROR_REDIRECT_LOOP -> TipoDeErroDeCarregamento.REDIRECIONAMENTO_EM_LOOP
            org.mozilla.geckoview.WebRequestError.ERROR_CORRUPTED_CONTENT,
            org.mozilla.geckoview.WebRequestError.ERROR_INVALID_CONTENT_ENCODING,
            org.mozilla.geckoview.WebRequestError.ERROR_UNSAFE_CONTENT_TYPE,
            org.mozilla.geckoview.WebRequestError.ERROR_CONTENT_CRASHED -> TipoDeErroDeCarregamento.CONTEUDO_COM_DEFEITO
            org.mozilla.geckoview.WebRequestError.ERROR_SAFEBROWSING_MALWARE_URI,
            org.mozilla.geckoview.WebRequestError.ERROR_SAFEBROWSING_UNWANTED_URI,
            org.mozilla.geckoview.WebRequestError.ERROR_SAFEBROWSING_HARMFUL_URI,
            org.mozilla.geckoview.WebRequestError.ERROR_SAFEBROWSING_PHISHING_URI -> TipoDeErroDeCarregamento.SITE_PERIGOSO
            org.mozilla.geckoview.WebRequestError.ERROR_PROXY_CONNECTION_REFUSED,
            org.mozilla.geckoview.WebRequestError.ERROR_UNKNOWN_PROXY_HOST -> TipoDeErroDeCarregamento.PROXY
            org.mozilla.geckoview.WebRequestError.ERROR_PORT_BLOCKED -> TipoDeErroDeCarregamento.PORTA_BLOQUEADA
            c("ERROR_HARMFULADDON_URI") -> TipoDeErroDeCarregamento.SITE_PERIGOSO
            else -> TipoDeErroDeCarregamento.DESCONHECIDO
        }
    }

    fun definirPrioridadeAlta(alta: Boolean) {
        prioridadeAlta = alta
        sessao.setPriorityHint(if (alta) GeckoSession.PRIORITY_HIGH else GeckoSession.PRIORITY_DEFAULT)
    }

    // Pedido feito antes de o processo da página existir se perde (não há aba remota para recebê-lo):
    // reaplica quando a página começa/termina, já com o processo de pé.
    private var prioridadeAlta = false

    private fun reaplicarPrioridade() {
        if (prioridadeAlta) sessao.setPriorityHint(GeckoSession.PRIORITY_HIGH)
    }

    private fun registrarPerdaDoProcesso(travamento: Boolean) {
        if (destruida) return
        Log.w(TAG_SUPERFICIE, "processo da página perdido (travamento=$travamento) url=$url ativa=$ativa")
        processoPerdido = true
        perdaFoiTravamento = perdaFoiTravamento || travamento
        // As portas da extensão morreram junto com o processo.
        portasDosQuadros.clear()
        quadroDoVideo.clear()
        portaDaPagina = null
        // Aba em segundo plano só volta quando for exibida, para não reabrir tudo de uma vez.
        if (ativa) recuperarSessaoAposPerdaDoProcesso()
    }

    private fun recuperarSessaoAposPerdaDoProcesso() {
        val travamento = perdaFoiTravamento
        processoPerdido = false
        perdaFoiTravamento = false
        val motor = MotorDoFirefoxDoNavegadorChromebrothers.obter(context)
        if (!sessao.isOpen) sessao.open(motor)
        MotorDoFirefoxDoNavegadorChromebrothers.quandoExtensaoPronta { extensao ->
            post { ligarPonteDeJavascript(extensao) }
        }
        sessao.setActive(ativa)
        val anterior = url
        val tratado = aoRecuperarDaPerdaDoProcesso?.invoke(anterior, travamento) == true
        if (!tratado) {
            val estado = estadoDaSessao
            when {
                estado != null -> sessao.restoreState(estado)
                !anterior.isNullOrBlank() -> sessao.loadUri(anterior)
            }
        }
        Log.i(TAG_SUPERFICIE, "sessão reaberta após perda do processo url=$anterior")
    }

    fun destroy() {
        destruida = true
        dialogos.fecharTudo()
        permissoes.fecharTudo()
        removeCallbacks(descobrirCapaSePresa)
        MotorDoFirefoxDoNavegadorChromebrothers.esquecerSessao(sessao)
        visor.releaseSession()
        sessao.close()
    }

    /** Informa ao Gecko a faixa inferior coberta (ex.: teclado) para o visual viewport. */
    fun definirClippingVerticalInferior(pixels: Int) {
        visor.setVerticalClipping(pixels.coerceAtLeast(0))
    }

    /**
     * Cobre a surface branca do Gecko até a primeira pintura do conteúdo.
     * Se a sessão estiver inativa / sem compositor, o first paint nunca chega e a capa
     * (cor sólida = “página cinza”) fica pra sempre — por isso há timeout + uncover.
     */
    fun cobrirAteAPrimeiraPintura(cor: Int) {
        removeCallbacks(descobrirCapaSePresa)
        visor.coverUntilFirstPaint(cor)
        postDelayed(descobrirCapaSePresa, ATRASO_MAXIMO_DA_CAPA_MS)
    }

    /** Espelha o uncover() privado do GeckoView (bg transparente na surface). */
    fun descobrirCapaDaPrimeiraPintura() {
        removeCallbacks(descobrirCapaSePresa)
        val uncover = runCatching {
            val metodo = visor.javaClass.getDeclaredMethod("uncover")
            metodo.isAccessible = true
            metodo.invoke(visor)
            true
        }.getOrDefault(false)
        if (!uncover && visor.childCount > 0) {
            visor.getChildAt(0).setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
    }

    fun pedirFocoParaOTeclado() {
        isFocusable = true
        isFocusableInTouchMode = true
        visor.isFocusable = true
        visor.isFocusableInTouchMode = true
        visor.requestFocus()
        val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
            as? android.view.inputmethod.InputMethodManager
        imm?.showSoftInput(visor, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * Toque nativo no [GeckoView] (coordenadas de tela) — sites tipo Higgsfield/ChatGPT
     * só mostram caret depois de um gesto real, não só com element.focus().
     */
    fun dispararToqueParaFocarNaPosicaoNaTela(xNaTela: Float, yNaTela: Float): Boolean {
        val loc = IntArray(2)
        visor.getLocationOnScreen(loc)
        val xLocal = xNaTela - loc[0]
        val yLocal = yNaTela - loc[1]
        if (xLocal < 0f || yLocal < 0f || xLocal > visor.width || yLocal > visor.height) {
            return false
        }
        pedirFocoParaOTeclado()
        val downTime = android.os.SystemClock.uptimeMillis()
        val down = android.view.MotionEvent.obtain(
            downTime,
            downTime,
            android.view.MotionEvent.ACTION_DOWN,
            xLocal,
            yLocal,
            0,
        )
        val up = android.view.MotionEvent.obtain(
            downTime,
            downTime + 48L,
            android.view.MotionEvent.ACTION_UP,
            xLocal,
            yLocal,
            0,
        )
        val okDown = visor.dispatchTouchEvent(down)
        down.recycle()
        val okUp = visor.dispatchTouchEvent(up)
        up.recycle()
        return okDown || okUp
    }

    /**
     * Cola a área de transferência no Gecko após o campo ter foco/caret.
     * Sequência principal: Ctrl↓ → V↓ → V↑ → Ctrl↑ (como um Ctrl+V real).
     */
    fun dispararColarDaAreaDeTransferencia(): Boolean {
        pedirFocoParaOTeclado()
        val agora = android.os.SystemClock.uptimeMillis()
        val metaCtrl =
            android.view.KeyEvent.META_CTRL_ON or android.view.KeyEvent.META_CTRL_LEFT_ON
        fun tecla(
            acao: Int,
            codigo: Int,
            meta: Int,
            quando: Long = agora,
        ): Boolean {
            val ev = android.view.KeyEvent(agora, quando, acao, codigo, 0, meta)
            return visor.dispatchKeyEvent(ev)
        }
        // Ctrl+V completo — o KEYCODE_PASTE sozinho costuma “consumir” sem colar imagem.
        var ok = tecla(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_CTRL_LEFT, metaCtrl)
        ok = tecla(
            android.view.KeyEvent.ACTION_DOWN,
            android.view.KeyEvent.KEYCODE_V,
            metaCtrl,
            agora + 1L,
        ) || ok
        ok = tecla(
            android.view.KeyEvent.ACTION_UP,
            android.view.KeyEvent.KEYCODE_V,
            metaCtrl,
            agora + 2L,
        ) || ok
        ok = tecla(
            android.view.KeyEvent.ACTION_UP,
            android.view.KeyEvent.KEYCODE_CTRL_LEFT,
            0,
            agora + 3L,
        ) || ok
        // Reforço com o atalho de colar do sistema.
        val pasteDown = android.view.KeyEvent(
            agora + 4L,
            agora + 4L,
            android.view.KeyEvent.ACTION_DOWN,
            android.view.KeyEvent.KEYCODE_PASTE,
            0,
        )
        val pasteUp = android.view.KeyEvent(
            agora + 5L,
            agora + 5L,
            android.view.KeyEvent.ACTION_UP,
            android.view.KeyEvent.KEYCODE_PASTE,
            0,
        )
        ok = visor.dispatchKeyEvent(pasteDown) || visor.dispatchKeyEvent(pasteUp) || ok
        return ok
    }

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
        visor.setBackgroundColor(color)
        // Não chama coverUntilFirstPaint aqui: em aba já pintada a capa fica pra sempre.
    }

    /** Vídeo anunciado por um iframe (ex.: Vimeo) → comandos vão para a porta daquele quadro. */
    private val quadroDoVideo = mutableMapOf<String, WebExtension.Port>()

    fun evaluateJavascript(script: String, recebedor: ((String) -> Unit)? = null) {
        val id = proximoJavascript++
        if (recebedor != null) retornosDeJavascript[id] = recebedor
        val portaDoVideo = RoteamentoDosComandosDoVideoParaOQuadroDeOrigemNaPagina.idDoVideoNoComando(script)
            ?.let { quadroDoVideo[it] }
            ?.takeIf { it in portasDosQuadros }
        val porta = portaDoVideo ?: portaDaPagina
        if (porta != null) enviarJavascript(porta, id, script) else filaDeJavascript.add(id to script)
    }

    fun publicarScriptEmTodosOsQuadros(script: String) {
        val ehDetector = script.contains("__chromebrothersInstalado")
        scriptsParaTodosOsQuadros.removeAll { existente ->
            existente.contains("__chromebrothersInstalado") == ehDetector
        }
        scriptsParaTodosOsQuadros.add(script)
        if (portasDosQuadros.isEmpty()) return
        portasDosQuadros.toList().forEach { porta ->
            runCatching { enviarJavascript(porta, 0L, script) }
        }
    }

    private fun ligarPonteDeJavascript(extensao: WebExtension) {
        sessao.webExtensionController.setMessageDelegate(
            extensao,
            object : WebExtension.MessageDelegate {
                override fun onConnect(port: WebExtension.Port) {
                    val topo = runCatching { port.sender.isTopLevel() }.getOrDefault(false)
                    port.setDelegate(object : WebExtension.PortDelegate {
                        override fun onPortMessage(message: Any, port: WebExtension.Port) {
                            post { entregarResultadoDeJavascript(message, port) }
                        }

                        override fun onDisconnect(port: WebExtension.Port) {
                            post {
                                portasDosQuadros.remove(port)
                                quadroDoVideo.values.removeAll { it === port }
                                if (portaDaPagina === port) portaDaPagina = portasDosQuadros.lastOrNull()
                            }
                        }
                    })
                    post {
                        if (port !in portasDosQuadros) portasDosQuadros.add(port)
                        if (topo || portaDaPagina == null) {
                            portaDaPagina = port
                            val pendentes = filaDeJavascript.toList()
                            filaDeJavascript.clear()
                            pendentes.forEach { enviarJavascript(port, it.first, it.second) }
                        }
                        scriptsParaTodosOsQuadros.forEach { script ->
                            runCatching { enviarJavascript(port, 0L, script) }
                        }
                    }
                }
            },
            "chromebrothers",
        )
    }

    private fun enviarJavascript(porta: WebExtension.Port, id: Long, codigo: String) {
        porta.postMessage(JSONObject().put("id", id).put("codigo", codigo))
    }

    private fun entregarResultadoDeJavascript(message: Any, porta: WebExtension.Port) {
        val json = when (message) {
            is JSONObject -> message
            is String -> runCatching { JSONObject(message) }.getOrNull()
            else -> null
        } ?: return
        if (json.has("pedirPacote")) {
            MotorDoFirefoxDoNavegadorChromebrothers.respostaDoPacoteDeBloqueio(json.optInt("pedirPacote", -1))
                ?.let { resposta -> runCatching { porta.postMessage(JSONObject().put("cbPacote", resposta)) } }
            return
        }
        if (json.has("aviso")) {
            val argumentos = when (val bruto = json.opt("argumentos")) {
                is JSONArray -> bruto
                is String -> runCatching { JSONArray(bruto) }.getOrDefault(JSONArray())
                else -> JSONArray()
            }
            RoteamentoDosComandosDoVideoParaOQuadroDeOrigemNaPagina
                .idDoVideoNoAviso(json.optString("aviso"), argumentos.optString(0, ""))
                ?.let { quadroDoVideo[it] = porta }
            aoAvisoDaPagina?.invoke(json.optString("aviso"), argumentos)
            return
        }
        val id = json.optLong("id", -1)
        val retorno = retornosDeJavascript.remove(id) ?: return
        retorno(json.optString("resultado", "null").ifEmpty { "null" })
    }

    fun aplicarModo(modo: ModoDeApresentacaoDoSite) {
        val gecko = if (modo == ModoDeApresentacaoDoSite.COMPUTADOR) {
            GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
        } else {
            GeckoSessionSettings.USER_AGENT_MODE_MOBILE
        }
        sessao.settings.userAgentMode = gecko
        settings.agenteExibido = if (modo == ModoDeApresentacaoDoSite.COMPUTADOR) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:143.0) Gecko/20100101 Firefox/143.0"
        } else {
            "Mozilla/5.0 (Android 14; Tablet; rv:143.0) Gecko/143.0 Firefox/143.0"
        }
    }

    fun definirProtecaoContraRastreamento(ligada: Boolean) {
        sessao.settings.useTrackingProtection = ligada
    }

    companion object {
        private const val ATRASO_MAXIMO_DA_CAPA_MS = 2_500L
        private const val TAG_SUPERFICIE = "ChromeBrothersPagina"
    }

    class AjustesDaSuperficie {
        var agenteExibido: String = ""
        var userAgentString: String
            get() = agenteExibido
            set(valor) {
                agenteExibido = valor
            }
        var loadWithOverviewMode: Boolean = false
        var javaScriptEnabled: Boolean
            get() = true
            set(_) = Unit
        var domStorageEnabled: Boolean
            get() = true
            set(_) = Unit
        var mediaPlaybackRequiresUserGesture: Boolean
            get() = false
            set(_) = Unit
        var useWideViewPort: Boolean
            get() = true
            set(_) = Unit
        var builtInZoomControls: Boolean
            get() = false
            set(_) = Unit
        var displayZoomControls: Boolean
            get() = false
            set(_) = Unit

        fun setSupportMultipleWindows(ligado: Boolean) = Unit

        fun setSupportZoom(ligado: Boolean) = Unit
    }
}

object MotorDoFirefoxDoNavegadorChromebrothers {
    private const val TAG_EXTENSAO = "ChromeBrothersExt"
    private const val ID_DA_EXTENSAO = "chromebrothers@navegador"
    private const val CAMINHO_DA_EXTENSAO =
        "resource://android/assets/extensao-embutida-de-injecao-do-chromebrothers/"
    private val principal = Handler(Looper.getMainLooper())
    private var runtime: GeckoRuntime? = null
    private var appContext: Context? = null
    private var extensao: WebExtension? = null
    private val quandoPronta = mutableListOf<(WebExtension) -> Unit>()
    private var pacoteDeBloqueio: JSONObject? = null
    private var versaoDoPacoteDeBloqueio = 0
    private var aoPedirPermissaoDeExtensao: ((String, List<String>, (Boolean) -> Unit) -> Unit)? = null
    private var aoAcaoDeExtensao: ((String, String, WebExtension.Action?, android.graphics.Bitmap?) -> Unit)? = null
    private var aoPopupDeExtensao: ((GeckoSession) -> Unit)? = null
    private var aoNovaAbaDeExtensao: ((GeckoSession, String) -> Unit)? = null
    private val sessoesAbertas = linkedSetOf<GeckoSession>()
    private val extensoesComAcao = linkedMapOf<String, WebExtension>()
    private val delegadosDeAcao = linkedMapOf<String, WebExtension.ActionDelegate>()
    private var sessaoAtiva: GeckoSession? = null
    private val proximoIdDeDownload = AtomicInteger(1)

    fun idDaExtensaoEmbutida(): String = ID_DA_EXTENSAO

    fun aplicarEsquemaDeCorPreferido(escura: Boolean) {
        val motor = runtime ?: return
        principal.post {
            motor.settings.setPreferredColorScheme(esquemaDeCorPreferido(escura))
        }
    }

    private fun esquemaDeCorPreferido(escura: Boolean): Int =
        if (escura) GeckoRuntimeSettings.COLOR_SCHEME_DARK
        else GeckoRuntimeSettings.COLOR_SCHEME_LIGHT

    fun definirPedidoDePermissaoDeExtensao(bloco: (String, List<String>, (Boolean) -> Unit) -> Unit) {
        aoPedirPermissaoDeExtensao = bloco
    }

    fun definirAcaoDeExtensao(bloco: (String, String, WebExtension.Action?, android.graphics.Bitmap?) -> Unit) {
        aoAcaoDeExtensao = bloco
    }

    fun definirPopupDeExtensao(bloco: (GeckoSession) -> Unit) {
        aoPopupDeExtensao = bloco
    }

    fun definirNovaAbaDeExtensao(bloco: (GeckoSession, String) -> Unit) {
        aoNovaAbaDeExtensao = bloco
    }

    fun registrarSessao(sessao: GeckoSession) {
        principal.post {
            sessoesAbertas.add(sessao)
            extensoesComAcao.values.forEach { ligarAcaoNaSessao(sessao, it) }
        }
    }

    fun esquecerSessao(sessao: GeckoSession) {
        principal.post {
            sessoesAbertas.remove(sessao)
            if (sessaoAtiva === sessao) sessaoAtiva = null
        }
    }

    fun marcarAbaAtiva(sessao: GeckoSession?) {
        val motor = runtime ?: return
        principal.post {
            val anterior = sessaoAtiva
            if (anterior != null && anterior !== sessao) {
                runCatching { motor.webExtensionController.setTabActive(anterior, false) }
            }
            sessaoAtiva = sessao
            if (sessao != null) {
                runCatching { motor.webExtensionController.setTabActive(sessao, true) }
            }
        }
    }

    fun publicarPacoteDeBloqueio(json: String) {
        val novo = JSONObject(json)
        principal.post {
            pacoteDeBloqueio = novo
            versaoDoPacoteDeBloqueio++
        }
    }

    /**
     * A extensão pergunta pela lista (sendNativeMessage) e o app responde. A lista só vai junto se a
     * versão que ela tem estiver velha; sem lista pronta, responde vazio e ela pergunta de novo.
     */
    /**
     * Resposta à ponte da página sobre a lista de bloqueio: só com a lista se a versão da extensão
     * for outra; null quando ela já está em dia ou o app ainda não tem lista.
     */
    fun respostaDoPacoteDeBloqueio(versaoDaExtensao: Int): JSONObject? {
        val atual = pacoteDeBloqueio ?: return null
        if (versaoDaExtensao == versaoDoPacoteDeBloqueio) return null
        Log.i("ChromeBrothersBloqueio", "lista de bloqueio entregue à extensão (versão $versaoDoPacoteDeBloqueio)")
        return JSONObject().put("versao", versaoDoPacoteDeBloqueio).put("pacote", atual)
    }

    // Preferências do motor que o GeckoRuntimeSettings não expõe, lidas pelo GeckoView na partida.
    // image.svg.blob-image: imagem SVG (<img src=*.svg>) vira "blob" desenhado pelo WebRender fora da
    // thread principal, só na parte visível. Sem isso o Gecko rasteriza o SVG inteiro na thread da
    // página, no tamanho exibido: no banner animado da higgsfield (SVG com blur, em zoom) eram imagens
    // de 13480x3889 px refeitas a cada quadro — até 16 s de 25 s de thread ocupada; com isso, 4 ms.
    // accessibility.force_disabled: com qualquer serviço de acessibilidade ligado (ex.: AirDroid) o
    // Gecko mantém a árvore de acessibilidade de cada página e a atualiza a cada mudança no DOM; quem
    // não usa leitor de tela pode desligar no menu. Vale ao reabrir o app (o motor lê ao iniciar).
    // Escrito sempre (1 ou 0): o valor fica gravado no perfil do motor e precisa ser revertido também.
    private fun escreverConfiguracaoDoMotor(aplicativo: Context): String {
        val arquivo = File(aplicativo.filesDir, "geckoview-config.yaml")
        val acessibilidade = PreferenciasVisuaisEDeNavegacaoDoChromebrothers(aplicativo).acessibilidadeDasPaginas
        val conteudo = "prefs:\n  image.svg.blob-image: true\n" +
            "  accessibility.force_disabled: ${if (acessibilidade) 0 else 1}\n"
        if (!arquivo.exists() || arquivo.readText() != conteudo) arquivo.writeText(conteudo)
        return arquivo.absolutePath
    }

    fun obter(context: Context): GeckoRuntime {
        val existente = runtime
        if (existente != null) return existente
        val aplicativo = context.applicationContext
        appContext = aplicativo
        // Sites usam prefers-color-scheme a partir daqui — alinhar à preferência do app,
        // não só ao tema do sistema (senão home escura e páginas claras).
        val escura = PreferenciasVisuaisEDeNavegacaoDoChromebrothers(aplicativo).aparenciaEscura
        // Só no build de teste (depurável): Firefox do computador → about:debugging → USB → Profiler,
        // para medir engasgos de rolagem de verdade. O build de produção fica sem.
        val depuravel = (aplicativo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val ajustes = GeckoRuntimeSettings.Builder()
            .preferredColorScheme(esquemaDeCorPreferido(escura))
            .remoteDebuggingEnabled(depuravel)
            // console.log de páginas e da extensão embutida no logcat (só no build de teste).
            .consoleOutput(depuravel)
            .configFilePath(escreverConfiguracaoDoMotor(aplicativo))
            .build()
        val criado = GeckoRuntime.create(aplicativo, ajustes)
        runtime = criado
        PermissoesDaPaginaNoMotorDoFirefox.desfazerNegadasAutomaticasUmaVez(aplicativo, criado)
        criado.webExtensionController.enableExtensionProcessSpawning()
        criado.webExtensionController.promptDelegate = delegadoDePromptDeExtensao()
        criado.webExtensionController
            .ensureBuiltIn(CAMINHO_DA_EXTENSAO, ID_DA_EXTENSAO)
            .then({ instalada ->
                if (instalada != null) {
                    extensao = instalada
                    val pendentes = quandoPronta.toList()
                    quandoPronta.clear()
                    pendentes.forEach { it(instalada) }
                }
                GeckoResult.fromValue(null)
            }, {
                GeckoResult.fromValue(null)
            })
        return criado
    }

    fun quandoExtensaoPronta(bloco: (WebExtension) -> Unit) {
        val instalada = extensao
        if (instalada != null) bloco(instalada) else quandoPronta.add(bloco)
    }

    fun listarExtensoes(aoTerminar: (List<WebExtension>) -> Unit) {
        val motor = runtime
        if (motor == null) {
            aoTerminar(emptyList())
            return
        }
        motor.webExtensionController.list().then({ lista ->
            lista.orEmpty().forEach { observarAcoesDaExtensao(it) }
            principal.post { aoTerminar(lista.orEmpty()) }
            GeckoResult.fromValue(null)
        }, {
            principal.post { aoTerminar(emptyList()) }
            GeckoResult.fromValue(null)
        })
    }

    fun instalarExtensao(uri: String, aoTerminar: (Boolean) -> Unit) {
        val motor = runtime
        if (motor == null) {
            aoTerminar(false)
            return
        }
        motor.webExtensionController.install(uri).then({ instalada ->
            if (instalada != null) observarAcoesDaExtensao(instalada)
            principal.post { aoTerminar(instalada != null) }
            GeckoResult.fromValue(null)
        }, {
            principal.post { aoTerminar(false) }
            GeckoResult.fromValue(null)
        })
    }

    fun desinstalarExtensao(extensao: WebExtension, aoTerminar: () -> Unit) {
        val motor = runtime ?: return aoTerminar()
        motor.webExtensionController.uninstall(extensao).then({
            principal.post {
                extensoesComAcao.remove(extensao.id)
                delegadosDeAcao.remove(extensao.id)
                sessoesAbertas.forEach { it.webExtensionController.setActionDelegate(extensao, null) }
                aoAcaoDeExtensao?.invoke(extensao.id, "", null, null)
                aoTerminar()
            }
            GeckoResult.fromValue(null)
        }, {
            principal.post { aoTerminar() }
            GeckoResult.fromValue(null)
        })
    }

    fun definirExtensaoLigada(extensao: WebExtension, ligada: Boolean, aoTerminar: () -> Unit) {
        val motor = runtime ?: return aoTerminar()
        val pedido = if (ligada) {
            motor.webExtensionController.enable(extensao, WebExtensionController.EnableSource.USER)
        } else {
            motor.webExtensionController.disable(extensao, WebExtensionController.EnableSource.USER)
        }
        pedido.then({
            principal.post { aoTerminar() }
            GeckoResult.fromValue(null)
        }, {
            principal.post { aoTerminar() }
            GeckoResult.fromValue(null)
        })
    }

    private fun observarAcoesDaExtensao(extensao: WebExtension) {
        if (extensao.isBuiltIn && extensao.id == ID_DA_EXTENSAO) return
        if (extensao.id == "screenshot-full-page@dcalculator.net" && extensao.isBuiltIn) {
            runtime?.webExtensionController?.uninstall(extensao)?.then({
                principal.post {
                    extensoesComAcao.remove(extensao.id)
                    delegadosDeAcao.remove(extensao.id)
                    aoAcaoDeExtensao?.invoke(extensao.id, "", null, null)
                }
                GeckoResult.fromValue(null)
            }, {
                GeckoResult.fromValue(null)
            })
            return
        }
        extensoesComAcao[extensao.id] = extensao
        val delegado = delegadosDeAcao.getOrPut(extensao.id) { delegadoDeAcaoDaExtensao() }
        extensao.setActionDelegate(delegado)
        extensao.setDownloadDelegate(delegadoDeDownloadDaExtensao())
        extensao.setTabDelegate(object : WebExtension.TabDelegate {
            override fun onNewTab(
                source: WebExtension,
                createDetails: WebExtension.CreateTabDetails,
            ): GeckoResult<GeckoSession>? {
                return abrirAbaDaExtensao(createDetails.url.orEmpty())
            }

            override fun onOpenOptionsPage(source: WebExtension) {
                val endereco = source.metaData?.optionsPageUrl.orEmpty()
                if (endereco.isNotBlank()) abrirAbaDaExtensao(endereco)
            }
        })
        sessoesAbertas.forEach { ligarAcaoNaSessao(it, extensao) }
        Log.i(TAG_EXTENSAO, "observando acao id=${extensao.id} nome=${extensao.metaData?.name}")
    }

    private fun delegadoDeDownloadDaExtensao(): WebExtension.DownloadDelegate {
        return object : WebExtension.DownloadDelegate {
            override fun onDownload(
                source: WebExtension,
                request: WebExtension.DownloadRequest,
            ): GeckoResult<WebExtension.DownloadInitData>? {
                val motor = runtime ?: return GeckoResult.fromValue(null)
                val contexto = appContext ?: return GeckoResult.fromValue(null)
                val uri = request.request.uri
                val nome = request.filename?.takeIf { it.isNotBlank() }
                    ?: "download-${System.currentTimeMillis()}"
                val bytes = bytesDoEnderecoDeDownload(uri) ?: run {
                    Log.w(TAG_EXTENSAO, "download sem bytes uri=${uri.take(40)}")
                    return GeckoResult.fromValue(null)
                }
                val mime = mimeDoEnderecoDeDownload(uri, nome)
                val salvou = salvarBytesNoDownloadPublico(contexto, nome, mime, bytes)
                val download = motor.webExtensionController.createDownload(proximoIdDeDownload.getAndIncrement())
                val inicio = System.currentTimeMillis()
                val info = object : WebExtension.Download.Info {
                    override fun bytesReceived(): Long = bytes.size.toLong()
                    override fun canResume(): Boolean = false
                    override fun endTime(): Long = inicio
                    override fun fileExists(): Boolean = salvou
                    override fun filename(): String = nome
                    override fun fileSize(): Long = bytes.size.toLong()
                    override fun mime(): String = mime
                    override fun paused(): Boolean = false
                    override fun startTime(): Long = inicio
                    override fun state(): Int =
                        if (salvou) WebExtension.Download.STATE_COMPLETE
                        else WebExtension.Download.STATE_INTERRUPTED
                    override fun totalBytes(): Long = bytes.size.toLong()
                    override fun error(): Int? =
                        if (salvou) null else WebExtension.Download.INTERRUPT_REASON_FILE_FAILED
                }
                Log.i(TAG_EXTENSAO, "download da extensao nome=$nome salvou=$salvou bytes=${bytes.size}")
                return GeckoResult.fromValue(WebExtension.DownloadInitData(download, info))
            }
        }
    }

    private fun bytesDoEnderecoDeDownload(uri: String): ByteArray? {
        if (!uri.startsWith("data:", ignoreCase = true)) return null
        val virgula = uri.indexOf(',')
        if (virgula <= 0) return null
        val meta = uri.substring(5, virgula)
        val dados = uri.substring(virgula + 1)
        return if (meta.contains(";base64", ignoreCase = true)) {
            runCatching { Base64.decode(dados, Base64.DEFAULT) }.getOrNull()
        } else {
            runCatching { java.net.URLDecoder.decode(dados, "UTF-8").toByteArray(Charsets.UTF_8) }.getOrNull()
        }
    }

    private fun mimeDoEnderecoDeDownload(uri: String, nome: String): String {
        if (uri.startsWith("data:", ignoreCase = true)) {
            val meta = uri.substringAfter("data:").substringBefore(',')
            val tipo = meta.substringBefore(';').trim()
            if (tipo.isNotBlank()) return tipo
        }
        return when (nome.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            else -> "application/octet-stream"
        }
    }

    private fun salvarBytesNoDownloadPublico(
        contexto: Context,
        nome: String,
        mime: String,
        bytes: ByteArray,
    ): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val valores = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, nome)
                    put(MediaStore.Downloads.MIME_TYPE, mime)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val colecao = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val resolver = contexto.contentResolver
                val item = resolver.insert(colecao, valores) ?: return false
                resolver.openOutputStream(item)?.use { it.write(bytes) } ?: return false
                valores.clear()
                valores.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(item, valores, null, null)
                true
            } else {
                val pasta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!pasta.exists()) pasta.mkdirs()
                val arquivo = File(pasta, nome)
                arquivo.outputStream().use { it.write(bytes) }
                true
            }
        }.getOrElse { erro ->
            Log.w(TAG_EXTENSAO, "falha ao salvar download: $erro")
            false
        }
    }

    private fun abrirAbaDaExtensao(endereco: String): GeckoResult<GeckoSession> {
        val motor = runtime
        if (motor == null) return GeckoResult.fromValue(null)
        val resultado = GeckoResult<GeckoSession>()
        principal.post {
            val sessao = GeckoSession()
            sessao.open(motor)
            registrarSessao(sessao)
            aoNovaAbaDeExtensao?.invoke(sessao, endereco)
            resultado.complete(sessao)
        }
        return resultado
    }

    private fun ligarAcaoNaSessao(sessao: GeckoSession, extensao: WebExtension) {
        val delegado = delegadosDeAcao[extensao.id] ?: return
        sessao.webExtensionController.setActionDelegate(extensao, delegado)
    }

    private fun delegadoDeAcaoDaExtensao(): WebExtension.ActionDelegate {
        return object : WebExtension.ActionDelegate {
            override fun onBrowserAction(
                extension: WebExtension,
                session: GeckoSession?,
                action: WebExtension.Action,
            ) {
                publicarAcao(extension, action)
            }

            override fun onPageAction(
                extension: WebExtension,
                session: GeckoSession?,
                action: WebExtension.Action,
            ) {
                publicarAcao(extension, action)
            }

            override fun onTogglePopup(
                extension: WebExtension,
                action: WebExtension.Action,
            ): GeckoResult<GeckoSession>? {
                Log.i(TAG_EXTENSAO, "onTogglePopup id=${extension.id}")
                return abrirPopup()
            }

            override fun onOpenPopup(
                extension: WebExtension,
                action: WebExtension.Action,
            ): GeckoResult<GeckoSession>? {
                Log.i(TAG_EXTENSAO, "onOpenPopup id=${extension.id}")
                return abrirPopup()
            }
        }
    }

    private fun publicarAcao(extensao: WebExtension, acao: WebExtension.Action) {
        val titulo = acao.title ?: extensao.metaData?.name ?: extensao.id
        Log.i(TAG_EXTENSAO, "acao id=${extensao.id} titulo=$titulo enabled=${acao.enabled}")
        principal.post { aoAcaoDeExtensao?.invoke(extensao.id, titulo, acao, null) }
        acao.icon?.getBitmap(64)?.then({ bitmap ->
            principal.post { aoAcaoDeExtensao?.invoke(extensao.id, titulo, acao, bitmap) }
            GeckoResult.fromValue(null)
        }, {
            GeckoResult.fromValue(null)
        })
    }

    private fun abrirPopup(): GeckoResult<GeckoSession> {
        val motor = runtime
        if (motor == null) {
            Log.w(TAG_EXTENSAO, "abrirPopup sem runtime")
            return GeckoResult.fromValue(null)
        }
        val resultado = GeckoResult<GeckoSession>()
        val rodar = Runnable {
            val sessao = GeckoSession()
            sessao.open(motor)
            Log.i(TAG_EXTENSAO, "abrirPopup sessao aberta, entregando a UI")
            aoPopupDeExtensao?.invoke(sessao)
            resultado.complete(sessao)
        }
        if (Looper.myLooper() == principal.looper) rodar.run() else principal.post(rodar)
        return resultado
    }

    private fun delegadoDePromptDeExtensao(): WebExtensionController.PromptDelegate {
        return object : WebExtensionController.PromptDelegate {
            override fun onInstallPromptRequest(
                extension: WebExtension,
                permissions: Array<out String>,
                origins: Array<out String>,
                dataCollectionPermissions: Array<out String>,
            ): GeckoResult<WebExtension.PermissionPromptResponse>? {
                return pedirAceite(extension, permissions, origins, dataCollectionPermissions) { aceitou ->
                    WebExtension.PermissionPromptResponse(aceitou, false, false)
                }
            }

            // GeckoView 153: as versões antigas (sem coleta de dados) de onUpdatePrompt/onOptionalPrompt saíram.
            override fun onUpdatePrompt(
                extension: WebExtension,
                newPermissions: Array<out String>,
                newOrigins: Array<out String>,
                newDataCollectionPermissions: Array<out String>,
            ): GeckoResult<AllowOrDeny>? {
                return pedirAceite(extension, newPermissions, newOrigins, newDataCollectionPermissions) { aceitou ->
                    if (aceitou) AllowOrDeny.ALLOW else AllowOrDeny.DENY
                }
            }

            override fun onOptionalPrompt(
                extension: WebExtension,
                permissions: Array<out String>,
                origins: Array<out String>,
                dataCollectionPermissions: Array<out String>,
            ): GeckoResult<AllowOrDeny>? {
                return pedirAceite(extension, permissions, origins, dataCollectionPermissions) { aceitou ->
                    if (aceitou) AllowOrDeny.ALLOW else AllowOrDeny.DENY
                }
            }
        }
    }

    private fun <T> pedirAceite(
        extensao: WebExtension,
        permissoes: Array<out String>,
        origens: Array<out String>,
        coleta: Array<out String>,
        resposta: (Boolean) -> T,
    ): GeckoResult<T> {
        val pendente = GeckoResult<T>()
        val tratar = aoPedirPermissaoDeExtensao
        val nome = extensao.metaData?.name ?: extensao.id
        val pedidos = permissoes.toList() + origens.toList() + coleta.toList()
        if (tratar == null) {
            pendente.complete(resposta(false))
        } else {
            tratar(nome, pedidos) { aceitou -> pendente.complete(resposta(aceitou)) }
        }
        return pendente
    }
}

package com.chromebrothers.navegador

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.FaseDoArrasteDaJanelaPelaBarra
import com.chromebrothers.navegador.regras.BancoDeNotasDoCampoDoNavegador
import com.chromebrothers.navegador.regras.BancoDeVinculosDoCampoNoSite
import com.chromebrothers.navegador.regras.CamposVinculaveisDaPaginaParaOChat
import com.chromebrothers.navegador.regras.DescricaoDoVinculoDoCampoParaOChat
import com.chromebrothers.navegador.regras.EnderecoDaPaginaInicialDoNavegadorChromebrothers
import com.chromebrothers.navegador.regras.EnvioDeTextoParaOCampoDaPagina
import com.chromebrothers.navegador.regras.EscolhaAutomaticaDeVinculoDoCampoNaPagina
import com.chromebrothers.navegador.regras.MarcaDoElementoVinculado
import com.chromebrothers.navegador.regras.NotaDoCampoDoNavegador
import com.chromebrothers.navegador.regras.PosicaoDaJanelaDoCampoDeTexto
import com.chromebrothers.navegador.regras.PosicaoDaJanelaFlutuanteAoAbrirPelaBolhaNoCantoMaisProximo
import kotlin.math.hypot

class PonteJavascriptDoCampoDeTextoDoNavegador(
    private val principal: Handler = Handler(Looper.getMainLooper()),
) {
    var aoVincular: ((String, String, String, String, String, String) -> Unit)? = null

    @JavascriptInterface
    fun vinculado(tipo: String, id: String, nome: String, rotulo: String, tag: String, papel: String) {
        principal.post { aoVincular?.invoke(tipo, id, nome, rotulo, tag, papel) }
    }
}

class PainelDoCampoDeTextoDoNavegador(
    private val activity: AtividadePrincipalDoNavegadorChromebrothers,
    private val preferencias: PreferenciasVisuaisEDeNavegacaoDoChromebrothers,
    private val paginaAtiva: () -> SuperficieDaPaginaNoMotorDoFirefox?,
    private val aoAbrirEndereco: (String) -> Unit,
) {
    private val painel: LinearLayout = activity.findViewById(R.id.painel_do_campo_de_texto_do_navegador)
    private val corpoParaAncorar: LinearLayout =
        activity.findViewById(R.id.corpo_do_campo_de_texto_para_ancorar)
    private val bolha: TextView = activity.findViewById(R.id.bolha_do_campo_de_texto_do_navegador)
    private val campo: EditText = activity.findViewById(R.id.campo_de_texto_do_navegador)
    private val lista: LinearLayout = activity.findViewById(R.id.lista_de_notas_do_campo_de_texto)
    private val listaSuspensa: View = activity.findViewById(R.id.lista_suspensa_do_campo_de_texto)
    private val textoDoSelo: TextView = activity.findViewById(R.id.texto_do_selo_do_site_do_campo_de_texto)
    private val setaDaLista: TextView = activity.findViewById(R.id.seta_da_lista_do_campo_de_texto)
    private val setaDasNotas: TextView = activity.findViewById(R.id.seta_das_notas_do_campo_de_texto)
    private val botaoImportar: ImageButton = activity.findViewById(R.id.botao_importar_texto_do_campo_vinculado)
    private val estrela: ImageButton = activity.findViewById(R.id.botao_guardar_nota_do_campo_de_texto)
    private val rotuloDoCampo: TextView = activity.findViewById(R.id.rotulo_do_campo_vinculado)
    private val rotuloDoEnvio: TextView = activity.findViewById(R.id.rotulo_do_envio_vinculado)
    private val removerCampo: Button = activity.findViewById(R.id.botao_remover_campo_vinculado)
    private val removerEnvio: Button = activity.findViewById(R.id.botao_remover_envio_vinculado)
    private val linhaDaCaixa: LinearLayout = activity.findViewById(R.id.linha_da_caixa_vinculada)
    private val linhaDoEnvio: LinearLayout = activity.findViewById(R.id.linha_do_botao_de_envio_vinculado)
    private val titulo: TextView = activity.findViewById(R.id.faixa_de_titulo_do_campo_de_texto)
    private var notas: List<NotaDoCampoDoNavegador> = preferencias.lerNotasDoCampoDeTexto()
    private var vinculos = preferencias.lerVinculosDoCampoDeTexto()
    private var listaDoCampo = ListaDoCampo.FECHADA
    private var forma = Forma.OCULTA
    private var espera = ""
    private var campoNaTela = false
    private var envioNaTela = false
    private var ancoraX = 0f
    private var ancoraY = 0f
    private var posicaoDefinida = false
    private var larguraAplicada = false
    private var alturaDesejada = 0
    private var folgaDoTeclado = 0
    private var lugarAntesDoTeclado: LugarAntesDoTeclado? = null
    private var pedido = 0
    private var consulta = 0
    private var geracaoDoEspelho = 0
    private var geracaoAgendada = 0
    private val tarefaDoEspelho = Runnable {
        if (geracaoAgendada == geracaoDoEspelho) espelharAgora(geracaoAgendada)
    }
    private var faixaSuja = true
    private var hostDesenhado = ""
    private var ancoradaNaGaveta = false
    private var paiOriginalDoCorpo: ViewGroup? = null
    private var indiceOriginalDoCorpo = -1
    private var paramsOriginaisDoCorpo: ViewGroup.LayoutParams? = null
    private var aoAncorarNaGaveta: (() -> Unit)? = null
    private var aoPedirFocoNaAbaAncorada: (() -> Unit)? = null
    private val curva = DecelerateInterpolator()
    private val fundoDaLinha: Int by lazy {
        val valor = TypedValue()
        activity.theme.resolveAttribute(android.R.attr.selectableItemBackground, valor, true)
        valor.resourceId
    }

    fun ligar(ponte: PonteJavascriptDoCampoDeTextoDoNavegador) {
        ponte.aoVincular = { tipo, id, nome, rotulo, tag, papel ->
            recebeuMarca(tipo, id, nome, rotulo, tag, papel)
        }
        activity.findViewById<ImageButton>(R.id.botao_fechar_campo_de_texto_do_navegador).setOnClickListener { fechar() }
        activity.findViewById<ImageButton>(R.id.botao_minimizar_campo_de_texto_do_navegador).setOnClickListener { minimizar() }
        activity.findViewById<ImageButton>(R.id.botao_ancorar_campo_de_texto_na_gaveta_do_chat)
            .setOnClickListener { aoAncorarNaGaveta?.invoke() }
        botaoImportar.setOnClickListener { importarTextoDaCaixa() }
        activity.findViewById<ImageButton>(R.id.botao_limpar_campo_de_texto_do_navegador).setOnClickListener {
            campo.setText("")
        }
        activity.findViewById<ImageButton>(R.id.botao_guardar_nota_do_campo_de_texto).setOnClickListener { guardarNota() }
        activity.findViewById<ImageButton>(R.id.botao_enviar_texto_ao_campo_do_site).setOnClickListener { enviar() }
        activity.findViewById<ImageButton>(R.id.botao_copiar_texto_do_campo_de_texto).setOnClickListener { copiar() }
        activity.findViewById<View>(R.id.selo_do_site_do_campo_de_texto).setOnClickListener { alternarSites() }
        activity.findViewById<View>(R.id.selo_das_notas_do_campo_de_texto).setOnClickListener { alternarNotas() }
        lista.setOnClickListener { fecharLista() }
        linhaDaCaixa.setOnClickListener { pedirVinculo("campo") }
        linhaDoEnvio.setOnClickListener { pedirVinculo("envio") }
        activity.findViewById<ImageButton>(R.id.botao_vincular_automaticamente_caixa_e_botao_do_site)
            .setOnClickListener { vincularAutomaticamente() }
        removerCampo.setOnClickListener { removerMarca("campo") }
        removerEnvio.setOnClickListener { removerMarca("envio") }
        prepararArraste(activity.findViewById(R.id.alca_para_arrastar_o_campo_de_texto), aoSoltar = null)
        prepararArraste(titulo, aoSoltar = null)
        prepararGestoDaBolha()
        prepararTamanho(activity.findViewById(R.id.alca_de_redimensionar_o_campo_de_texto))
        observarTamanhoDaTela()
        campo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                pintarEstrela()
                agendarEspelho()
            }
        })
        desenharFaixa()
        desenharStatus()
        pintarEstrela()
    }

    fun ligarAncorarNaGaveta(aoAncorar: () -> Unit) {
        aoAncorarNaGaveta = aoAncorar
    }

    /** MOVENDO responde se está sobre a faixa de abas do chat; SOLTOU responde se deve fixar no chat. */
    private var aoArrastarPelaBarra: ((Float, Float, FaseDoArrasteDaJanelaPelaBarra) -> Boolean)? = null

    fun ligarArrastePelaBarra(acao: (Float, Float, FaseDoArrasteDaJanelaPelaBarra) -> Boolean) {
        aoArrastarPelaBarra = acao
    }

    fun levarJanelaAoPontoDaTela(xNaTela: Float, yNaTela: Float) {
        painel.post {
            val pai = painel.parent as? View ?: return@post
            val origem = IntArray(2)
            pai.getLocationOnScreen(origem)
            val densidade = painel.resources.displayMetrics.density
            val largura = painel.width.takeIf { it > 0 }?.toFloat() ?: (360f * densidade)
            posicionar(xNaTela - origem[0] - largura / 2f, yNaTela - origem[1] - 20f * densidade)
        }
    }

    fun ligarPedirFocoNaAbaAncorada(aoPedir: () -> Unit) {
        aoPedirFocoNaAbaAncorada = aoPedir
    }

    fun tituloParaAbaNaGaveta(): String =
        activity.getString(R.string.abrir_campo_de_texto_do_navegador)

    fun estaAncoradaNaGaveta(): Boolean = ancoradaNaGaveta

    /**
     * Esconde flutuante/bolha e move o corpo do campo para o host da gaveta.
     * @return mensagem de erro ou vazio se ok.
     */
    fun ancorarConteudoNoHostDaGaveta(host: ViewGroup): String {
        if (ancoradaNaGaveta) return ""
        val pai = corpoParaAncorar.parent as? ViewGroup
            ?: return "Não achei o corpo do campo de texto."
        paiOriginalDoCorpo = pai
        indiceOriginalDoCorpo = pai.indexOfChild(corpoParaAncorar)
        paramsOriginaisDoCorpo = corpoParaAncorar.layoutParams
        painel.animate().cancel()
        bolha.animate().cancel()
        // Sem clearFocus, o Android joga o foco na barra de endereço e abre o select.
        campo.clearFocus()
        painel.clearFocus()
        esconderTeclado()
        fecharLista()
        activity.limparFocoDaBarraDeEnderecoESugestoes()
        pai.removeView(corpoParaAncorar)
        host.addView(
            corpoParaAncorar,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        corpoParaAncorar.visibility = View.VISIBLE
        painel.visibility = View.GONE
        bolha.visibility = View.GONE
        forma = Forma.OCULTA
        ancoradaNaGaveta = true
        // Redistribuição de foco pode ser assíncrona após o GONE.
        painel.post { activity.limparFocoDaBarraDeEnderecoESugestoes() }
        return ""
    }

    fun mostrarConteudoAncoradoNoHost(mostrar: Boolean) {
        if (!ancoradaNaGaveta) return
        corpoParaAncorar.visibility = if (mostrar) View.VISIBLE else View.GONE
    }

    /** Devolve o corpo à janela flutuante e abre o painel. */
    fun desancorarConteudoParaFlutuante(): String {
        if (!ancoradaNaGaveta) return ""
        val pai = paiOriginalDoCorpo
            ?: return "Não achei o painel original."
        (corpoParaAncorar.parent as? ViewGroup)?.removeView(corpoParaAncorar)
        val params = paramsOriginaisDoCorpo
            ?: LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
        val indice = indiceOriginalDoCorpo.coerceIn(0, pai.childCount)
        pai.addView(corpoParaAncorar, indice, params)
        ancoradaNaGaveta = false
        paiOriginalDoCorpo = null
        indiceOriginalDoCorpo = -1
        paramsOriginaisDoCorpo = null
        abrir()
        return ""
    }

    fun alternar() {
        if (ancoradaNaGaveta) {
            aoPedirFocoNaAbaAncorada?.invoke()
            return
        }
        if (forma == Forma.ABERTA) minimizar() else abrir()
    }

    fun recebeuMarca(tipo: String, id: String, nome: String, rotulo: String, tag: String, papel: String) {
        consulta++
        espera = ""
        val marca = marcaConfiavel(MarcaDoElementoVinculado(id, nome, rotulo, tag, papel))
        val host = hospedeiroAtual()
        if (host.isNotBlank()) {
            vinculos = if (tipo == "envio") {
                BancoDeVinculosDoCampoNoSite.guardarEnvio(vinculos, host, marca)
            } else {
                BancoDeVinculosDoCampoNoSite.guardarCampo(vinculos, host, marca)
            }
            val url = paginaAtiva()?.url.orEmpty()
            if (url.startsWith("http://") || url.startsWith("https://")) {
                vinculos = BancoDeVinculosDoCampoNoSite.guardarEndereco(vinculos, host, url)
            }
            preferencias.gravarVinculosDoCampoDeTexto(vinculos)
            faixaSuja = true
        }
        if (tipo == "envio") {
            envioNaTela = true
        } else {
            campoNaTela = true
            if (!campo.text.isNullOrEmpty()) {
                cancelarEspelho()
                espelharAgora(geracaoDoEspelho)
            }
        }
        desenharStatus()
        desenharFaixaSePrecisar()
    }

    fun descreverVinculo(): String {
        return DescricaoDoVinculoDoCampoParaOChat.escrever(
            BancoDeVinculosDoCampoNoSite.doSite(vinculos, hospedeiroAtual()),
        )
    }

    fun consultarEstado() {
        lembrarEnderecoDaPagina()
        val vista = paginaAtiva() ?: return
        procurarVinculo(vista, ++consulta, 0)
    }

    private fun procurarVinculo(vista: SuperficieDaPaginaNoMotorDoFirefox, pedidoConsulta: Int, tentativa: Int) {
        if (pedidoConsulta != consulta || paginaAtiva() != vista) return
        val salvo = BancoDeVinculosDoCampoNoSite.doSite(vinculos, BancoDeVinculosDoCampoNoSite.hospedeiro(vista.url ?: ""))
        val campoSalvo = salvo?.campo?.let { marcaConfiavel(it) }
        val envioSalvo = salvo?.envio?.let { marcaConfiavel(it) }
        vista.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.instalador(), null)
        vista.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.reaplicar(campoSalvo, envioSalvo)) { bruto ->
            activity.runOnUiThread {
                if (pedidoConsulta != consulta || paginaAtiva() != vista) return@runOnUiThread
                val codigo = AtividadePrincipalDoNavegadorChromebrothers.textoDoJavascript(bruto)
                campoNaTela = codigo == "campo" || codigo == "ambos"
                envioNaTela = codigo == "envio" || codigo == "ambos"
                desenharStatus()
                desenharFaixaSePrecisar()
                val achouCampo = campoSalvo == null || campoNaTela
                val achouEnvio = envioSalvo == null || envioNaTela
                if ((!achouCampo || !achouEnvio) && tentativa < 4) {
                    painel.postDelayed({ procurarVinculo(vista, pedidoConsulta, tentativa + 1) }, 450)
                }
            }
        }
    }

    private fun marcaConfiavel(marca: MarcaDoElementoVinculado): MarcaDoElementoVinculado {
        return marca.copy(id = EnvioDeTextoParaOCampoDaPagina.idConfiavel(marca.id))
    }

    fun ajustarAoTeclado(extra: Int) {
        if (folgaDoTeclado == extra) return
        folgaDoTeclado = extra
        if (!posicaoDefinida) return
        painel.post {
            if (!posicaoDefinida) return@post
            if (extra > 0 && forma == Forma.ABERTA && encaixarNaBarraAcimaDoTeclado()) return@post
            if (extra == 0) {
                val guardado = lugarAntesDoTeclado
                lugarAntesDoTeclado = null
                if (guardado != null) {
                    posicionar(guardado.x, guardado.y)
                    if (forma == Forma.ABERTA) ajustarAltura()
                    return@post
                }
            }
            if (forma == Forma.ABERTA) ajustarAltura()
            posicionar(ancoraX, ancoraY)
        }
    }

    private fun encaixarNaBarraAcimaDoTeclado(): Boolean {
        val pai = painel.parent as? View ?: return false
        val limite = (pai.height - folgaDoTeclado).toFloat()
        if (painel.y + painel.height <= limite) return false
        if (lugarAntesDoTeclado == null) {
            val altura = if (alturaDesejada > 0) alturaDesejada else painel.height
            lugarAntesDoTeclado = LugarAntesDoTeclado(ancoraX, ancoraY, altura)
        }
        val densidade = painel.resources.displayMetrics.density
        val topo = topoAbaixoDaBarraDeMenu()
        val desejada = lugarAntesDoTeclado?.altura?.toFloat() ?: painel.height.toFloat()
        val novaAltura = PosicaoDaJanelaDoCampoDeTexto.alturaEntreABarraEOTeclado(
            desejada,
            topo,
            limite,
            260f * densidade,
        ).toInt()
        if (painel.layoutParams.height != novaAltura) {
            painel.layoutParams.height = novaAltura
            painel.layoutParams = painel.layoutParams
        }
        painel.post {
            if (!posicaoDefinida || folgaDoTeclado <= 0 || lugarAntesDoTeclado == null) return@post
            posicionar(ancoraX, topoAbaixoDaBarraDeMenu())
        }
        return true
    }

    private fun topoAbaixoDaBarraDeMenu(): Float {
        val barra = activity.findViewById<View>(R.id.barra_de_navegacao) ?: return 0f
        val pai = painel.parent as? View ?: return 0f
        val origemBarra = IntArray(2)
        val origemPai = IntArray(2)
        barra.getLocationInWindow(origemBarra)
        pai.getLocationInWindow(origemPai)
        return (origemBarra[1] + barra.height - origemPai[1]).toFloat()
    }

    private fun abrir() {
        val pedidoAtual = ++pedido
        val veioDaBolha = forma == Forma.BOLHA && bolha.visibility == View.VISIBLE
        val xDaBolha = bolha.x
        val yDaBolha = bolha.y
        bolha.animate().cancel()
        bolha.visibility = View.GONE
        consultarEstado()
        aplicarLarguraSalvaSePrecisar()
        painel.animate().cancel()
        painel.scaleX = 1f
        painel.scaleY = 1f
        painel.alpha = 1f
        painel.visibility = View.INVISIBLE
        painel.post {
            if (pedidoAtual != pedido) return@post
            ajustarAltura()
            encaixarLargura()
            painel.post {
                if (pedidoAtual != pedido) return@post
                val pai = painel.parent as? View
                if (!posicaoDefinida && pai != null && painel.width > 0 && painel.height > 0) {
                    val margem = 16f * painel.resources.displayMetrics.density
                    posicionar(
                        pai.width - painel.width - margem,
                        pai.height - folgaDoTeclado - painel.height - margem,
                    )
                    posicaoDefinida = true
                } else if (veioDaBolha && pai != null && painel.width > 0 && painel.height > 0) {
                    val lugar = PosicaoDaJanelaFlutuanteAoAbrirPelaBolhaNoCantoMaisProximo.calcular(
                        xDaBolha,
                        yDaBolha,
                        56f * painel.resources.displayMetrics.density,
                        painel.width.toFloat(),
                        painel.height.toFloat(),
                        pai.width.toFloat(),
                        (pai.height - folgaDoTeclado).toFloat().coerceAtLeast(0f),
                    )
                    posicionar(lugar.first, lugar.second)
                    bolha.x = xDaBolha
                    bolha.y = yDaBolha
                    ancoraX = xDaBolha
                    ancoraY = yDaBolha
                } else if (posicaoDefinida) {
                    posicionar(ancoraX, ancoraY)
                }
                crescer(pedidoAtual)
            }
        }
    }

    private fun minimizar() {
        fecharLista()
        encolher(mostrarBolha = true)
    }

    private fun fechar() {
        fecharLista()
        encolher(mostrarBolha = false)
    }

    fun fecharPeloUsuario() {
        fechar()
    }

    private fun crescer(pedidoAtual: Int) {
        forma = Forma.ABERTA
        alinharPivo()
        painel.scaleX = 0.2f
        painel.scaleY = 0.2f
        painel.alpha = 0f
        painel.visibility = View.VISIBLE
        painel.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(220)
            .setInterpolator(curva)
            .withEndAction {
                if (pedidoAtual != pedido) return@withEndAction
                campo.requestFocus()
                val teclado = activity.getSystemService(InputMethodManager::class.java)
                teclado?.showSoftInput(campo, InputMethodManager.SHOW_IMPLICIT)
            }
            .start()
    }

    private fun encolher(mostrarBolha: Boolean) {
        if (forma != Forma.ABERTA) {
            if (!mostrarBolha) {
                bolha.visibility = View.GONE
                painel.visibility = View.GONE
                forma = Forma.OCULTA
            }
            return
        }
        val pedidoAtual = ++pedido
        esconderTeclado()
        alinharPivo()
        painel.animate().cancel()
        painel.animate()
            .scaleX(0.2f)
            .scaleY(0.2f)
            .alpha(0f)
            .setDuration(220)
            .setInterpolator(curva)
            .withEndAction {
                if (pedidoAtual != pedido) return@withEndAction
                painel.visibility = View.GONE
                painel.scaleX = 1f
                painel.scaleY = 1f
                painel.alpha = 1f
                if (mostrarBolha) {
                    posicionar(ancoraX, ancoraY)
                    bolha.scaleX = 0.6f
                    bolha.scaleY = 0.6f
                    bolha.visibility = View.VISIBLE
                    bolha.animate().scaleX(1f).scaleY(1f).setDuration(160).setInterpolator(curva).start()
                    forma = Forma.BOLHA
                } else {
                    bolha.visibility = View.GONE
                    forma = Forma.OCULTA
                }
            }
            .start()
    }

    private fun alinharPivo() {
        val largura = painel.width.toFloat().coerceAtLeast(1f)
        val altura = painel.height.toFloat().coerceAtLeast(1f)
        painel.pivotX = (bolha.x - painel.x).coerceIn(0f, largura)
        painel.pivotY = (bolha.y - painel.y).coerceIn(0f, altura)
    }

    private fun posicionar(x: Float, y: Float) {
        val pai = painel.parent as? View ?: return
        if (pai.width <= 0 || pai.height <= 0) return
        val limiteAltura = (pai.height - folgaDoTeclado).toFloat().coerceAtLeast(0f)
        val ladoBolha = 56f * painel.resources.displayMetrics.density
        val card = PosicaoDaJanelaDoCampoDeTexto.limitar(
            x,
            y,
            painel.width.toFloat().coerceAtLeast(1f),
            painel.height.toFloat().coerceAtLeast(1f),
            pai.width.toFloat(),
            limiteAltura,
        )
        val circulo = PosicaoDaJanelaDoCampoDeTexto.limitar(
            x,
            y,
            ladoBolha,
            ladoBolha,
            pai.width.toFloat(),
            limiteAltura,
        )
        painel.x = card.first
        painel.y = card.second
        bolha.x = circulo.first
        bolha.y = circulo.second
        ancoraX = circulo.first
        ancoraY = circulo.second
    }

    private fun prepararGestoDaBolha() {
        var inicioX = 0f
        var inicioY = 0f
        GestoDaBolhaFlutuanteComAlvoDeFecharEFixacaoNoChatDoNavegador(
            activity = activity,
            bolha = bolha,
            aoComecar = {
                inicioX = bolha.x
                inicioY = bolha.y
            },
            aoMover = { dx, dy ->
                lugarAntesDoTeclado = null
                posicionar(inicioX + dx, inicioY + dy)
            },
            aoAbrir = { if (forma == Forma.BOLHA) abrir() },
            aoFechar = { fechar() },
            aoFixarNoChat = { aoAncorarNaGaveta?.invoke() },
            consultarFaixaDoChat = { x, y, fase -> aoArrastarPelaBarra?.invoke(x, y, fase) == true },
        ).ligar()
    }

    private fun prepararArraste(alvo: View, aoSoltar: (() -> Unit)?) {
        val folga = ViewConfiguration.get(activity).scaledTouchSlop.toFloat()
        var origemX = 0f
        var origemY = 0f
        var moveu = false
        alvo.setOnTouchListener { _, evento ->
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    origemX = evento.rawX
                    origemY = evento.rawY
                    moveu = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = evento.rawX - origemX
                    val dy = evento.rawY - origemY
                    if (!moveu && hypot(dx, dy) < folga) return@setOnTouchListener true
                    if (!moveu) lugarAntesDoTeclado = null
                    moveu = true
                    origemX = evento.rawX
                    origemY = evento.rawY
                    posicionar(ancoraX + dx, ancoraY + dy)
                    val sobreAFaixa = aoArrastarPelaBarra?.invoke(
                        evento.rawX,
                        evento.rawY,
                        FaseDoArrasteDaJanelaPelaBarra.MOVENDO,
                    ) == true
                    painel.alpha = if (sobreAFaixa) 0.55f else 1f
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moveu) {
                        aoSoltar?.invoke()
                    } else {
                        painel.alpha = 1f
                        val fixar = aoArrastarPelaBarra?.invoke(
                            evento.rawX,
                            evento.rawY,
                            FaseDoArrasteDaJanelaPelaBarra.SOLTOU,
                        ) == true
                        if (fixar) aoAncorarNaGaveta?.invoke()
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (moveu) {
                        painel.alpha = 1f
                        aoArrastarPelaBarra?.invoke(evento.rawX, evento.rawY, FaseDoArrasteDaJanelaPelaBarra.CANCELOU)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun prepararTamanho(alca: View) {
        var origemX = 0f
        var origemY = 0f
        var larguraInicial = 0
        var alturaInicial = 0
        var ultimaLargura = 0
        var ultimaAltura = 0
        alca.setOnTouchListener { _, evento ->
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    origemX = evento.rawX
                    origemY = evento.rawY
                    larguraInicial = painel.width
                    alturaInicial = painel.height
                    ultimaLargura = larguraInicial
                    ultimaAltura = alturaInicial
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val pai = painel.parent as? View
                    if (pai != null && larguraInicial > 0 && alturaInicial > 0) {
                        val densidade = painel.resources.displayMetrics.density
                        val limiteAltura = (pai.height - folgaDoTeclado).toFloat().coerceAtLeast(0f)
                        val novaLargura = PosicaoDaJanelaDoCampoDeTexto.largura(
                            larguraInicial.toFloat(),
                            evento.rawX - origemX,
                            painel.x,
                            280f * densidade,
                            pai.width.toFloat(),
                        ).toInt()
                        val novaAltura = PosicaoDaJanelaDoCampoDeTexto.altura(
                            alturaInicial.toFloat(),
                            evento.rawY - origemY,
                            painel.y,
                            260f * densidade,
                            limiteAltura,
                        ).toInt()
                        val params = painel.layoutParams
                        if (params.width != novaLargura || params.height != novaAltura) {
                            params.width = novaLargura
                            params.height = novaAltura
                            painel.layoutParams = params
                        }
                        ultimaLargura = novaLargura
                        ultimaAltura = novaAltura
                        alturaDesejada = novaAltura
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (ultimaLargura > 0) preferencias.gravarLarguraDoCampoDeTexto(ultimaLargura)
                    if (ultimaAltura > 0) preferencias.gravarAlturaDoCampoDeTexto(ultimaAltura)
                    true
                }
                else -> false
            }
        }
    }

    private fun observarTamanhoDaTela() {
        (painel.parent as? View)?.addOnLayoutChangeListener { _, esquerda, topo, direita, baixo, antigaEsquerda, antigoTopo, antigaDireita, antigoBaixo ->
            if (direita - esquerda == antigaDireita - antigaEsquerda && baixo - topo == antigoBaixo - antigoTopo) return@addOnLayoutChangeListener
            if (!posicaoDefinida) return@addOnLayoutChangeListener
            if (forma == Forma.ABERTA) ajustarAltura()
            encaixarLargura()
            painel.post { if (posicaoDefinida) posicionar(ancoraX, ancoraY) }
        }
    }

    private fun aplicarLarguraSalvaSePrecisar() {
        if (larguraAplicada) return
        val salvaLargura = preferencias.larguraDoCampoDeTexto()
        val salvaAltura = preferencias.alturaDoCampoDeTexto()
        val params = painel.layoutParams
        if (salvaLargura > 0) params.width = salvaLargura
        if (salvaAltura > 0) {
            alturaDesejada = salvaAltura
            params.height = salvaAltura
        }
        painel.layoutParams = params
        larguraAplicada = true
    }

    private fun encaixarLargura() {
        val pai = painel.parent as? View ?: return
        if (pai.width <= 0) return
        val minimo = 280f * painel.resources.displayMetrics.density
        val atual = when {
            painel.width > 0 -> painel.width.toFloat()
            painel.layoutParams.width > 0 -> painel.layoutParams.width.toFloat()
            else -> minimo
        }
        val nova = PosicaoDaJanelaDoCampoDeTexto.largura(atual, 0f, painel.x, minimo, pai.width.toFloat()).toInt()
        if (painel.layoutParams.width != nova) {
            painel.layoutParams.width = nova
            painel.layoutParams = painel.layoutParams
        }
    }

    private fun ajustarAltura() {
        val pai = painel.parent as? View ?: return
        if (pai.height <= 0) return
        val densidade = painel.resources.displayMetrics.density
        val limite = (pai.height - folgaDoTeclado).toFloat()
        if (limite <= 0f) return
        val desejada = if (alturaDesejada > 0) alturaDesejada.toFloat() else 460f * densidade
        val nova = PosicaoDaJanelaDoCampoDeTexto.altura(
            desejada,
            0f,
            painel.y,
            260f * densidade,
            limite,
        ).toInt()
        if (painel.layoutParams.height != nova) {
            painel.layoutParams.height = nova
            painel.layoutParams = painel.layoutParams
        }
    }

    private fun pedirVinculo(tipo: String) {
        val vista = paginaAtiva() ?: return
        vista.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.instalador(), null)
        if (espera == tipo) {
            espera = ""
            vista.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.cancelarVinculo(), null)
            desenharStatus()
            return
        }
        espera = tipo
        val script = if (tipo == "envio") {
            EnvioDeTextoParaOCampoDaPagina.armarEnvio()
        } else {
            EnvioDeTextoParaOCampoDaPagina.armarCampo()
        }
        vista.evaluateJavascript(script, null)
        desenharStatus()
    }

    private fun vincularAutomaticamente() {
        val vista = paginaAtiva()
        if (vista == null) {
            Toast.makeText(
                activity,
                R.string.vinculo_automatico_sem_pagina_aberta,
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        if (espera.isNotEmpty()) {
            espera = ""
            vista.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.cancelarVinculo(), null)
            desenharStatus()
        }
        val pedidoLocal = ++pedido
        vista.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.instalador(), null)
        vista.evaluateJavascript(CamposVinculaveisDaPaginaParaOChat.script()) { bruto ->
            activity.runOnUiThread {
                if (pedidoLocal != pedido || paginaAtiva() != vista) return@runOnUiThread
                val itens = CamposVinculaveisDaPaginaParaOChat.ler(
                    AtividadePrincipalDoNavegadorChromebrothers.textoDoJavascript(bruto),
                )
                val escolha = EscolhaAutomaticaDeVinculoDoCampoNaPagina.escolher(itens)
                escolha.campo?.let { marca ->
                    recebeuMarca("campo", marca.id, marca.nome, marca.rotulo, marca.tag, marca.papel)
                }
                escolha.envio?.let { marca ->
                    recebeuMarca("envio", marca.id, marca.nome, marca.rotulo, marca.tag, marca.papel)
                }
                if (escolha.campo != null || escolha.envio != null) {
                    consultarEstado()
                }
                Toast.makeText(
                    activity,
                    EscolhaAutomaticaDeVinculoDoCampoNaPagina.mensagem(escolha),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun removerMarca(tipo: String) {
        consulta++
        val host = hospedeiroAtual()
        if (host.isNotBlank()) {
            vinculos = if (tipo == "envio") {
                BancoDeVinculosDoCampoNoSite.removerEnvio(vinculos, host)
            } else {
                BancoDeVinculosDoCampoNoSite.removerCampo(vinculos, host)
            }
            preferencias.gravarVinculosDoCampoDeTexto(vinculos)
            faixaSuja = true
        }
        if (tipo == "envio") envioNaTela = false else campoNaTela = false
        val vista = paginaAtiva()
        vista?.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.soltar(tipo), null)
        if (espera == tipo) {
            espera = ""
            vista?.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.cancelarVinculo(), null)
        }
        desenharStatus()
        desenharFaixaSePrecisar()
    }

    private fun removerSite(host: String) {
        consulta++
        vinculos = vinculos.filter { it.hospedeiro != host }
        preferencias.gravarVinculosDoCampoDeTexto(vinculos)
        faixaSuja = true
        if (host == hospedeiroAtual()) {
            espera = ""
            campoNaTela = false
            envioNaTela = false
            val vista = paginaAtiva()
            vista?.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.soltar("campo"), null)
            vista?.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.soltar("envio"), null)
            vista?.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.cancelarVinculo(), null)
        }
        desenharStatus()
        desenharFaixaSePrecisar()
    }

    private fun desenharStatus() {
        val salvo = BancoDeVinculosDoCampoNoSite.doSite(vinculos, hospedeiroAtual())
        escreverLinha(rotuloDoCampo, removerCampo, salvo?.campo, campoNaTela, espera == "campo", ehEnvio = false)
        escreverLinha(rotuloDoEnvio, removerEnvio, salvo?.envio, envioNaTela, espera == "envio", ehEnvio = true)
        marcarPedido(linhaDaCaixa, espera == "campo")
        marcarPedido(linhaDoEnvio, espera == "envio")
        val densidade = activity.resources.displayMetrics.density
        bolha.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#1A1D24"))
            when {
                campoNaTela -> setStroke((2f * densidade).toInt(), Color.parseColor("#1A73E8"))
                envioNaTela -> setStroke((2f * densidade).toInt(), Color.parseColor("#F9AB00"))
            }
        }
    }

    private fun escreverLinha(
        rotulo: TextView,
        remover: View,
        marca: MarcaDoElementoVinculado?,
        naTela: Boolean,
        aguardando: Boolean,
        ehEnvio: Boolean,
    ) {
        val nome = marca?.rotulo?.trim().orEmpty().ifEmpty { activity.getString(R.string.campo_de_texto_vinculado) }
        rotulo.text = when {
            aguardando -> activity.getString(if (ehEnvio) R.string.botao_aguardando_vinculo else R.string.caixa_aguardando_vinculo)
            marca == null && !naTela -> activity.getString(R.string.nenhum_vinculo_curto)
            naTela -> nome
            else -> activity.getString(R.string.vinculo_salvo_fora_da_tela, nome)
        }
        val cor = when {
            aguardando || naTela -> if (ehEnvio) "#F9AB00" else "#8AB4F8"
            else -> "#8A9099"
        }
        rotulo.setTextColor(Color.parseColor(cor))
        remover.visibility = if (marca != null || naTela) View.VISIBLE else View.GONE
        if (!ehEnvio) {
            botaoImportar.visibility = View.VISIBLE
            botaoImportar.isEnabled = naTela
            botaoImportar.alpha = if (naTela) 1f else 0.35f
        }
    }

    private fun marcarPedido(linha: View, ativo: Boolean) {
        if (ativo) {
            val densidade = linha.resources.displayMetrics.density
            linha.background = GradientDrawable().apply {
                cornerRadius = 12f * densidade
                setColor(Color.parseColor("#2A3140"))
            }
        } else if (fundoDaLinha != 0) {
            linha.setBackgroundResource(fundoDaLinha)
        }
    }

    private fun copiar() {
        val texto = campo.text?.toString().orEmpty()
        if (texto.isEmpty()) {
            Toast.makeText(activity, R.string.nada_para_copiar_do_campo_de_texto, Toast.LENGTH_SHORT).show()
            return
        }
        val area = activity.getSystemService(ClipboardManager::class.java) ?: return
        area.setPrimaryClip(ClipData.newPlainText("texto", texto))
        if (Build.VERSION.SDK_INT < 33) {
            Toast.makeText(activity, R.string.texto_do_campo_copiado, Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarNota() {
        val proxima = BancoDeNotasDoCampoDoNavegador.guardar(notas, campo.text?.toString().orEmpty(), System.currentTimeMillis())
        if (proxima == notas) {
            Toast.makeText(activity, R.string.nota_vazia_do_campo_de_texto, Toast.LENGTH_SHORT).show()
            return
        }
        notas = proxima
        faixaSuja = true
        pintarEstrela()
        preferencias.gravarNotasDoCampoDeTexto(notas)
        desenharFaixaSePrecisar()
    }

    private fun agendarEspelho() {
        geracaoAgendada = ++geracaoDoEspelho
        campo.removeCallbacks(tarefaDoEspelho)
        if (!campoNaTela) return
        if (campo.text.isNullOrEmpty()) {
            espelharAgora(geracaoAgendada)
            return
        }
        campo.postDelayed(tarefaDoEspelho, PAUSA_DO_ESPELHO_MS)
    }

    private fun cancelarEspelho() {
        geracaoDoEspelho++
        campo.removeCallbacks(tarefaDoEspelho)
    }

    private fun espelharAgora(geracao: Int) {
        val vista = paginaAtiva() ?: return
        if (!campoNaTela || geracao != geracaoDoEspelho) return
        val manterFoco = campo.hasFocus()
        val texto = campo.text?.toString().orEmpty()
        vista.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.espelhar(texto)) {
            activity.runOnUiThread {
                campo.postDelayed({
                    if (geracao != geracaoDoEspelho || !manterFoco || forma != Forma.ABERTA || campo.hasFocus()) return@postDelayed
                    campo.requestFocus()
                    val teclado = activity.getSystemService(InputMethodManager::class.java)
                    teclado?.showSoftInput(campo, InputMethodManager.SHOW_IMPLICIT)
                }, 60)
            }
        }
    }

    private fun enviar() {
        cancelarEspelho()
        val vista = paginaAtiva() ?: return
        val texto = campo.text?.toString().orEmpty()
        vista.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.escrever(texto)) { bruto ->
            activity.runOnUiThread {
                val resultado = AtividadePrincipalDoNavegadorChromebrothers.textoDoJavascript(bruto)
                if (resultado == "ok") {
                    Toast.makeText(activity, R.string.texto_enviado_ao_campo_do_site, Toast.LENGTH_SHORT).show()
                } else {
                    val temCaixa = BancoDeVinculosDoCampoNoSite.doSite(vinculos, hospedeiroAtual())?.campo != null
                    consultarEstado()
                    val mensagem = if (resultado == "sem-campo" && temCaixa) {
                        R.string.caixa_vinculada_fora_da_tela
                    } else {
                        R.string.campo_do_site_nao_vinculado
                    }
                    Toast.makeText(activity, mensagem, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun desenharFaixaSePrecisar() {
        val host = hospedeiroAtual()
        if (host == hostDesenhado && !faixaSuja) return
        desenharFaixa()
    }

    private fun desenharFaixa() {
        lista.removeAllViews()
        val densidade = activity.resources.displayMetrics.density
        val hostAtual = hospedeiroAtual()
        hostDesenhado = hostAtual
        faixaSuja = false
        textoDoSelo.text = nomeDoSelo()
        setaDaLista.text = if (listaDoCampo == ListaDoCampo.SITES) "▴" else "▾"
        setaDasNotas.text = if (listaDoCampo == ListaDoCampo.NOTAS) "▴" else "▾"
        when (listaDoCampo) {
            ListaDoCampo.SITES -> desenharSites(densidade, hostAtual)
            ListaDoCampo.NOTAS -> desenharNotas(densidade)
            ListaDoCampo.FECHADA -> Unit
        }
        listaSuspensa.visibility = if (listaDoCampo == ListaDoCampo.FECHADA) View.GONE else View.VISIBLE
    }

    private fun desenharSites(densidade: Float, hostAtual: String) {
        lista.addView(tituloDeBloco(activity.getString(R.string.sites_vinculados_do_campo_de_texto), densidade))
        if (vinculos.isEmpty()) {
            lista.addView(linhaVazia(activity.getString(R.string.nenhum_site_vinculado_do_campo_de_texto), densidade))
        }
        vinculos.forEach { vinculo ->
            val linha = linhaDeLista(densidade)
            val atual = vinculo.hospedeiro == hostAtual
            if (atual) {
                linha.background = GradientDrawable().apply {
                    cornerRadius = 12f * densidade
                    setColor(Color.parseColor("#1A2744"))
                }
            }
            linha.addView(TextView(activity).apply {
                text = vinculo.hospedeiro.removePrefix("www.")
                setTextColor(Color.parseColor(if (atual) "#8AB4F8" else "#F4F7FB"))
                textSize = 15f
                isSingleLine = true
                ellipsize = TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            linha.setOnClickListener { abrirSite(vinculo) }
            linha.addView(Button(activity).apply {
                text = "×"
                isAllCaps = false
                setTextColor(Color.parseColor("#8A9099"))
                textSize = 18f
                background = null
                stateListAnimator = null
                minWidth = 0
                minimumWidth = 0
                minHeight = 0
                minimumHeight = 0
                isFocusable = false
                contentDescription = activity.getString(R.string.remover_vinculos_deste_site, vinculo.hospedeiro)
                layoutParams = LinearLayout.LayoutParams((48f * densidade).toInt(), (48f * densidade).toInt())
                setOnClickListener { removerSite(vinculo.hospedeiro) }
            })
            lista.addView(linha)
        }
    }

    private fun desenharNotas(densidade: Float) {
        lista.addView(tituloDeBloco(activity.getString(R.string.notas_guardadas_do_campo_de_texto), densidade))
        if (notas.isEmpty()) {
            lista.addView(linhaVazia(activity.getString(R.string.nenhuma_nota_do_campo_de_texto), densidade))
        }
        notas.forEach { nota ->
            val cartao = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding((8f * densidade).toInt(), (8f * densidade).toInt(), (8f * densidade).toInt(), (4f * densidade).toInt())
            }
            cartao.addView(TextView(activity).apply {
                text = nota.texto.trim()
                setTextColor(Color.parseColor("#F4F7FB"))
                textSize = 15f
                maxLines = 6
                ellipsize = TextUtils.TruncateAt.END
            })
            val acoes = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.END
            }
            acoes.addView(botaoDaNota(activity.getString(R.string.usar_nota_do_campo_de_texto), "#8AB4F8") {
                campo.setText(nota.texto)
                fecharLista()
            })
            acoes.addView(botaoDaNota(activity.getString(R.string.excluir_nota_do_campo_de_texto), "#F28B82") {
                confirmarExclusao(nota.id)
            })
            cartao.addView(acoes)
            lista.addView(cartao)
        }
    }

    private fun botaoDaNota(texto: String, cor: String, aoClicar: () -> Unit): Button {
        return Button(activity).apply {
            text = texto
            isAllCaps = false
            setTextColor(Color.parseColor(cor))
            textSize = 15f
            background = null
            stateListAnimator = null
            minWidth = 0
            minimumWidth = 0
            setOnClickListener { aoClicar() }
        }
    }

    private fun alternarSites() {
        listaDoCampo = if (listaDoCampo == ListaDoCampo.SITES) ListaDoCampo.FECHADA else ListaDoCampo.SITES
        faixaSuja = true
        desenharFaixa()
    }

    private fun alternarNotas() {
        listaDoCampo = if (listaDoCampo == ListaDoCampo.NOTAS) ListaDoCampo.FECHADA else ListaDoCampo.NOTAS
        faixaSuja = true
        desenharFaixa()
    }

    private fun fecharLista() {
        if (listaDoCampo == ListaDoCampo.FECHADA) return
        listaDoCampo = ListaDoCampo.FECHADA
        faixaSuja = true
        desenharFaixa()
    }

    private fun importarTextoDaCaixa() {
        val vista = paginaAtiva() ?: return
        if (!campoNaTela) return
        vista.evaluateJavascript(EnvioDeTextoParaOCampoDaPagina.ler()) { bruto ->
            activity.runOnUiThread {
                val texto = AtividadePrincipalDoNavegadorChromebrothers.textoDoJavascript(bruto)
                if (texto.isBlank() || texto == "sem-campo") {
                    Toast.makeText(activity, R.string.texto_do_campo_vinculado_vazio, Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                campo.setText(texto)
                campo.setSelection(campo.text?.length ?: 0)
            }
        }
    }

    private fun tituloDeBloco(texto: String, densidade: Float): TextView {
        return TextView(activity).apply {
            this.text = texto
            setTextColor(Color.parseColor("#8A9099"))
            textSize = 12f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding((8f * densidade).toInt(), (10f * densidade).toInt(), (8f * densidade).toInt(), (4f * densidade).toInt())
            isClickable = true
            isFocusable = false
            setOnClickListener { }
        }
    }

    private fun linhaVazia(texto: String, densidade: Float): TextView {
        return TextView(activity).apply {
            this.text = texto
            setTextColor(Color.parseColor("#8A9099"))
            textSize = 14f
            gravity = android.view.Gravity.CENTER_VERTICAL
            minimumHeight = (48f * densidade).toInt()
            setPadding((8f * densidade).toInt(), 0, (8f * densidade).toInt(), 0)
        }
    }

    private fun linhaDeLista(densidade: Float): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            minimumHeight = (48f * densidade).toInt()
            setPadding((8f * densidade).toInt(), 0, 0, 0)
            isClickable = true
            isFocusable = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    private fun abrirSite(vinculo: com.chromebrothers.navegador.regras.VinculoDoCampoNoSite) {
        fecharLista()
        if (vinculo.hospedeiro == hospedeiroAtual()) {
            consultarEstado()
            return
        }
        aoAbrirEndereco(BancoDeVinculosDoCampoNoSite.enderecoParaAbrir(vinculo))
    }

    private fun lembrarEnderecoDaPagina() {
        val url = paginaAtiva()?.url ?: return
        if (!url.startsWith("http://") && !url.startsWith("https://")) return
        if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) return
        val host = BancoDeVinculosDoCampoNoSite.hospedeiro(url)
        val proximo = BancoDeVinculosDoCampoNoSite.guardarEndereco(vinculos, host, url)
        if (proximo === vinculos) return
        vinculos = proximo
        preferencias.gravarVinculosDoCampoDeTexto(vinculos)
    }

    private fun pintarEstrela() {
        val texto = campo.text?.toString().orEmpty()
        val guardada = texto.isNotEmpty() && notas.any { it.texto == texto }
        estrela.imageTintList = if (guardada) ColorStateList.valueOf(Color.parseColor("#F9AB00")) else null
    }

    private fun nomeDoSelo(): String {
        val url = paginaAtiva()?.url.orEmpty()
        if (url.isBlank() || EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) {
            return activity.getString(R.string.selo_sem_site_do_campo_de_texto)
        }
        val host = BancoDeVinculosDoCampoNoSite.hospedeiro(url)
        if (host.isBlank()) return activity.getString(R.string.selo_sem_site_do_campo_de_texto)
        return host.removePrefix("www.")
    }

    private fun confirmarExclusao(id: Long) {
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(R.layout.layout_caixa_de_dialogo_do_editor_de_imagem, null)
        raiz.findViewById<TextView>(R.id.titulo_da_caixa_de_dialogo_do_editor).text =
            activity.getString(R.string.titulo_da_confirmacao_de_exclusao_da_nota)
        val mensagem = raiz.findViewById<TextView>(R.id.mensagem_da_caixa_de_dialogo_do_editor)
        mensagem.visibility = View.VISIBLE
        mensagem.text = activity.getString(R.string.mensagem_da_confirmacao_de_exclusao_da_nota)
        raiz.findViewById<EditText>(R.id.campo_da_caixa_de_dialogo_do_editor).visibility = View.GONE
        val cancelar = raiz.findViewById<Button>(R.id.botao_cancelar_da_caixa_de_dialogo_do_editor)
        val confirmar = raiz.findViewById<Button>(R.id.botao_confirmar_da_caixa_de_dialogo_do_editor)
        cancelar.text = activity.getString(R.string.cancelar_caixa_do_editor)
        cancelar.backgroundTintList = null
        cancelar.stateListAnimator = null
        confirmar.text = activity.getString(R.string.excluir_na_caixa_do_editor)
        confirmar.setTextColor(Color.parseColor("#F28B82"))
        confirmar.backgroundTintList = null
        confirmar.stateListAnimator = null
        cancelar.setOnClickListener { dialogo.dismiss() }
        confirmar.setOnClickListener {
            dialogo.dismiss()
            notas = BancoDeNotasDoCampoDoNavegador.remover(notas, id)
            faixaSuja = true
            pintarEstrela()
            preferencias.gravarNotasDoCampoDeTexto(notas)
            desenharFaixaSePrecisar()
        }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun esconderTeclado() {
        val teclado = activity.getSystemService(InputMethodManager::class.java)
        teclado?.hideSoftInputFromWindow(campo.windowToken, 0)
    }

    private fun hospedeiroAtual(): String {
        return BancoDeVinculosDoCampoNoSite.hospedeiro(paginaAtiva()?.url ?: "")
    }

    private fun rotulo(texto: String): String {
        val linha = texto.lineSequence().firstOrNull().orEmpty().trim()
        return if (linha.length <= 48) linha else linha.take(47) + "…"
    }

    private data class LugarAntesDoTeclado(val x: Float, val y: Float, val altura: Int)

    private enum class ListaDoCampo { FECHADA, SITES, NOTAS }

    private enum class Forma { OCULTA, ABERTA, BOLHA }

    companion object {
        const val ID_ABA_ANCORADA = "campo-de-texto"
        private const val PAUSA_DO_ESPELHO_MS = 700L
    }
}

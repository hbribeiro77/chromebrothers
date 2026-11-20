package com.chromebrothers.navegador

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Switch
import android.widget.TextView
import com.chromebrothers.navegador.regras.FormaDaSelecaoSobreAImagem
import com.chromebrothers.navegador.regras.PaletaDoEditorDeImagem
import com.chromebrothers.navegador.regras.PontaDoTracoDaComposicao
import com.chromebrothers.navegador.regras.ProporcaoDaMolduraDaComposicao
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

/**
 * Chrome do editor de imagem em estilo "estúdio": barra fina no topo, barra lateral flutuante
 * (tamanho, conta-gotas, opacidade, desfazer/refazer), barra da seleção embaixo e painéis que
 * abrem a partir dos ícones (ações, moldura, pincel, cor). Fica por cima do editor, na mesma área.
 * Toda ação passa pelo [Hospede] (o painel da biblioteca), que já sabe mexer no editor.
 */
class ChromeDeEstudioDoEditorDeImagem(
    private val activity: Activity,
    private val area: FrameLayout,
    private val editor: EditorDeRecorteETracoDaImagemDaBiblioteca,
    private val hospede: Hospede,
) {
    interface Hospede {
        fun voltarParaABiblioteca()
        fun inserirDaBiblioteca()
        /** Abre o visualizador de mídia (componente flutuante da coleção) para arrastar imagens ao editor. */
        fun abrirVisualizadorDeMidia()
        fun concluir()
        fun componentes()
        fun tamanhoDaMoldura()
        fun rotuloDoTamanhoDaMoldura(): String
        fun voltarAoNavegador()
        fun descartar()
        fun escolherPincel()
        fun escolherBorracha()
        fun escolherTexto()
        fun escolherSelecao()
        fun textoAtivo(): Boolean
        fun definirProporcao(proporcao: ProporcaoDaMolduraDaComposicao)
        fun proporcaoOriginal()
        fun alternarCamadas()
        fun camadasVisiveis(): Boolean
        fun escolherCor(cor: Int)
        fun corAtual(): Int
        fun definirCorDaFolha(cor: Int)
        fun alternarContaGotas()
        fun escolherPonta(ponta: PontaDoTracoDaComposicao)
        /** Pincel do motor de carimbos; null volta às pontas comuns. */
        fun escolherPincelDoMotor(id: String?)
        /** Estúdio de pincéis: imagem da galeria para a forma (null = cancelou). */
        fun pedirImagemParaFormaDoPincel(receber: (android.net.Uri?) -> Unit)
        /** Arquivo .chromebrush para importar (null = cancelou). */
        fun pedirArquivoDePinceis(receber: (android.net.Uri?) -> Unit)
        fun alternarDesenhoComODedo()
        fun desfazer()
        fun refazer()
        fun espessuraMudou()
        fun definirFormaDaSelecao(forma: FormaDaSelecaoSobreAImagem)
        fun cancelarSelecao()
    }

    private val d = activity.resources.displayMetrics.density
    private fun dp(v: Float) = (v * d).toInt()
    private val prefs = activity.getSharedPreferences("editor-de-imagem-estudio", Context.MODE_PRIVATE)

    private val corDoIcone = Color.parseColor("#C4C7CE")
    private val corAtiva = Color.parseColor("#8AB4F8")
    private val fundoDasBarras = Color.parseColor("#DB121418")
    private val fundoDosPaineis = Color.parseColor("#1A1D24")

    // ---------- Barra do topo ----------
    private val barraDoTopo = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(8f), 0, dp(8f), 0)
        background = ColorDrawable(fundoDasBarras)
        elevation = 6f * d
        isClickable = true
    }
    private val botaoBiblioteca = TextView(activity).apply {
        text = activity.getString(R.string.estudio_biblioteca)
        setTextColor(corDoIcone)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        gravity = Gravity.CENTER_VERTICAL
        val seta = activity.getDrawable(R.drawable.icone_estudio_voltar)?.mutate()
        seta?.setTint(corDoIcone)
        seta?.setBounds(0, 0, dp(22f), dp(22f))
        setCompoundDrawables(seta, null, null, null)
        compoundDrawablePadding = dp(2f)
        setPadding(dp(4f), 0, dp(12f), 0)
        minHeight = dp(44f)
        background = fundoDeToque()
        contentDescription = activity.getString(R.string.estudio_voltar_a_biblioteca)
        setOnClickListener { fecharPaineis(); hospede.voltarParaABiblioteca() }
    }
    private val botaoAcoes = icone(R.drawable.icone_estudio_acoes, R.string.estudio_acoes) { abrirPainelDeAcoes(it) }
    private val botaoInserir = icone(R.drawable.icone_estudio_inserir, R.string.estudio_abrir_visualizador_de_midia) {
        fecharPaineis(); hospede.abrirVisualizadorDeMidia()
    }
    /** Atalho fixo (era só no submenu de Ações): volta ao navegador sem fechar a edição. */
    private val botaoNavegador = icone(R.drawable.frente_com_dois_aneis_do_icone_do_navegador_chromebrothers, R.string.voltar_ao_navegador) {
        fecharPaineis(); hospede.voltarAoNavegador()
    }.apply {
        // Ícone do Chromebrothers com as cores dele; a frente do ícone adaptativo tem margem grande, então menos espaço interno.
        imageTintList = null
        setPadding(dp(2f), dp(2f), dp(2f), dp(2f))
    }
    private val botaoSelecao = icone(R.drawable.icone_estudio_selecao, R.string.estudio_selecao) {
        fecharPaineis()
        if (editor.ferramentaAtual() == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE) {
            hospede.escolherPincel()
        } else {
            hospede.escolherSelecao()
        }
    }
    private val botaoTransformar = icone(R.drawable.icone_estudio_transformar, R.string.estudio_moldura) { abrirPainelDaMoldura(it) }
    private val botaoAjustes = icone(R.drawable.icone_estudio_ajustes, R.string.estudio_ajustes) { abrirPainelDeAjustes(it) }
    private val botaoPincel = icone(R.drawable.icone_estudio_pincel, R.string.estudio_pincel) {
        if (pincelAtivo()) abrirPainelDoPincel(it) else { fecharPaineis(); hospede.escolherPincel() }
    }
    private val botaoBorracha = icone(R.drawable.icone_estudio_borracha, R.string.estudio_borracha) {
        // Como no pincel: com a borracha já ativa, o toque abre a escolha do pincel com que ela apaga.
        if (borrachaAtiva()) abrirPainelDaBorracha(it) else { fecharPaineis(); hospede.escolherBorracha() }
    }
    private val botaoTexto = icone(R.drawable.icone_estudio_texto, R.string.estudio_texto) {
        fecharPaineis(); hospede.escolherTexto()
    }
    private val botaoCamadas = icone(R.drawable.icone_estudio_camadas, R.string.estudio_camadas) {
        fecharPaineis(); hospede.alternarCamadas(); sincronizar()
    }
    private val bolinhaDaCor = View(activity)
    private val botaoCor = FrameLayout(activity).apply {
        contentDescription = activity.getString(R.string.estudio_cor)
        background = fundoDeToque()
        isClickable = true
        addView(bolinhaDaCor, FrameLayout.LayoutParams(dp(28f), dp(28f), Gravity.CENTER))
        setOnClickListener { abrirPainelDaCor(this) }
        // Arrastar a bolinha até a folha = balde de tinta (toque simples segue abrindo o painel).
        setOnTouchListener { v, e -> tocarNaBolinhaDaCor(v, e) }
    }

    // ---------- Balde: arrastar a cor até a folha ----------
    private var inicioDoArrasteDaCorX = 0f
    private var inicioDoArrasteDaCorY = 0f
    private var arrastandoACor = false
    private val gotaDoBalde = View(activity).apply {
        visibility = View.GONE
        elevation = 12f * activity.resources.displayMetrics.density
    }

    private fun tocarNaBolinhaDaCor(v: View, e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                inicioDoArrasteDaCorX = e.rawX
                inicioDoArrasteDaCorY = e.rawY
                arrastandoACor = false
                v.isPressed = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!arrastandoACor && Math.hypot((e.rawX - inicioDoArrasteDaCorX).toDouble(), (e.rawY - inicioDoArrasteDaCorY).toDouble()) > dp(14f)) {
                    arrastandoACor = true
                    v.isPressed = false
                    fecharPaineis()
                    if (gotaDoBalde.parent == null) area.addView(gotaDoBalde, FrameLayout.LayoutParams(dp(34f), dp(34f)))
                    gotaDoBalde.background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(hospede.corAtual() or 0xFF000000.toInt())
                        setStroke(dp(2f), Color.WHITE)
                    }
                    gotaDoBalde.visibility = View.VISIBLE
                    gotaDoBalde.bringToFront()
                }
                if (arrastandoACor) {
                    val origem = IntArray(2).also { area.getLocationOnScreen(it) }
                    // A gota fica um pouco acima do dedo, para ver onde cai.
                    gotaDoBalde.x = e.rawX - origem[0] - dp(17f)
                    gotaDoBalde.y = e.rawY - origem[1] - dp(17f) - dp(28f)
                }
            }
            MotionEvent.ACTION_UP -> {
                v.isPressed = false
                if (!arrastandoACor) {
                    v.performClick()
                    return true
                }
                arrastandoACor = false
                gotaDoBalde.visibility = View.GONE
                val origem = IntArray(2).also { editor.getLocationOnScreen(it) }
                val resultado = editor.preencherComOBalde(e.rawX - origem[0], e.rawY - origem[1] - dp(28f), hospede.corAtual())
                val aviso = when (resultado) {
                    EditorDeRecorteETracoDaImagemDaBiblioteca.ResultadoDoBalde.PREENCHEU -> {
                        editor.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                        sincronizar()
                        null
                    }
                    EditorDeRecorteETracoDaImagemDaBiblioteca.ResultadoDoBalde.FORA_DA_FOLHA -> null
                    EditorDeRecorteETracoDaImagemDaBiblioteca.ResultadoDoBalde.SEM_CAMADA -> R.string.estudio_balde_sem_camada
                    EditorDeRecorteETracoDaImagemDaBiblioteca.ResultadoDoBalde.NADA_A_PREENCHER -> R.string.estudio_balde_nada
                    EditorDeRecorteETracoDaImagemDaBiblioteca.ResultadoDoBalde.SEM_MEMORIA -> R.string.estudio_balde_sem_memoria
                }
                if (aviso != null) android.widget.Toast.makeText(activity, aviso, android.widget.Toast.LENGTH_SHORT).show()
            }
            MotionEvent.ACTION_CANCEL -> {
                v.isPressed = false
                arrastandoACor = false
                gotaDoBalde.visibility = View.GONE
            }
        }
        return true
    }

    // ---------- Barra lateral ----------
    private companion object {
        const val ALTURA_MINIMA_DO_CONTROLE_DP = 150f
        const val ALTURA_MAXIMA_DO_CONTROLE_DP = 320f
        const val CHAVE_DA_CURVA_DE_PRESSAO = "curva-da-pressao-pontos"
    }
    private val previaDoTamanho = PreviaDoTamanho(activity)
    private val esconderPrevia = Runnable { previaDoTamanho.visibility = View.GONE }
    private val controleDoTamanho = ControleDeslizanteVertical(activity).apply {
        contentDescription = activity.getString(R.string.estudio_tamanho_do_traco)
        rotulo = { valor -> "${com.chromebrothers.navegador.regras.TamanhoDoPincelDoEditorDeImagem.percentual(valor)}%" }
        aoMudar = { valor ->
            editor.tamanhoDaFerramenta = valor
            hospede.espessuraMudou()
            mostrarPreviaDoTamanho()
        }
        aoSoltar = { previaDoTamanho.postDelayed(esconderPrevia, 450L) }
    }

    /** Círculo no meio da área com o diâmetro real do pincel, na cor atual. */
    private fun mostrarPreviaDoTamanho() {
        previaDoTamanho.removeCallbacks(esconderPrevia)
        previaDoTamanho.diametro = editor.diametroDoPincelNaTela()
        previaDoTamanho.cor = hospede.corAtual()
        previaDoTamanho.borracha = editor.ferramentaAtual() == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA
        previaDoTamanho.visibility = View.VISIBLE
        previaDoTamanho.bringToFront()
        previaDoTamanho.invalidate()
    }
    private val controleDaOpacidade = ControleDeslizanteVertical(activity).apply {
        contentDescription = activity.getString(R.string.estudio_opacidade_do_traco)
        rotulo = { valor -> "${Math.round((0.05f + valor * 0.95f) * 100f)}%" }
        aoMudar = { valor -> editor.opacidadeDaFerramenta = 0.05f + valor * 0.95f }
    }
    private val botaoContaGotas = icone(R.drawable.icone_estudio_conta_gotas, R.string.conta_gotas_da_cor, tamanho = 40f) {
        hospede.alternarContaGotas()
    }
    private val barraLateral = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(0, dp(14f), 0, dp(10f))
        background = GradientDrawable().apply { cornerRadius = 26f * d; setColor(fundoDasBarras) }
        elevation = 6f * d
        isClickable = true
        addView(controleDoTamanho, LinearLayout.LayoutParams(dp(52f), dp(ALTURA_MINIMA_DO_CONTROLE_DP)))
        addView(botaoContaGotas, LinearLayout.LayoutParams(dp(40f), dp(40f)).apply { setMargins(0, dp(8f), 0, dp(8f)) })
        addView(controleDaOpacidade, LinearLayout.LayoutParams(dp(52f), dp(ALTURA_MINIMA_DO_CONTROLE_DP)))
        addView(View(activity).apply { setBackgroundColor(Color.parseColor("#2E333C")) },
            LinearLayout.LayoutParams(dp(24f), dp(1f)).apply { setMargins(0, dp(10f), 0, dp(4f)) })
        addView(icone(R.drawable.icone_estudio_desfazer, R.string.desfazer_edicao_da_imagem, tamanho = 44f) { hospede.desfazer() })
        addView(icone(R.drawable.icone_estudio_refazer, R.string.refazer_edicao_da_imagem, tamanho = 44f) { hospede.refazer() })
    }

    // ---------- Barra da seleção ----------
    private val botaoRetangulo = acaoDaSelecao(R.drawable.icone_estudio_retangulo, R.string.selecao_retangular_da_imagem) {
        hospede.definirFormaDaSelecao(FormaDaSelecaoSobreAImagem.RETANGULO)
    }
    private val botaoLaco = acaoDaSelecao(R.drawable.icone_estudio_selecao, R.string.selecao_em_laco_da_imagem) {
        hospede.definirFormaDaSelecao(FormaDaSelecaoSobreAImagem.LACO)
    }
    private val botaoCancelarSelecao = icone(R.drawable.icone_estudio_fechar, R.string.estudio_cancelar_selecao) {
        hospede.cancelarSelecao()
    }
    private val barraDaSelecao = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(6f), dp(6f), dp(6f), dp(6f))
        background = GradientDrawable().apply { cornerRadius = 18f * d; setColor(Color.parseColor("#EB121418")) }
        elevation = 8f * d
        isClickable = true
        addView(botaoRetangulo)
        addView(botaoLaco)
        addView(botaoCancelarSelecao)
    }

    private var popupAberto: PopupWindow? = null
    private var aoSincronizarPainel: (() -> Unit)? = null
    private var montado = false

    init {
        barraDoTopo.addView(botaoBiblioteca)
        barraDoTopo.addView(botaoNavegador)
        barraDoTopo.addView(divisoria())
        listOf(botaoAcoes, botaoInserir, botaoSelecao, botaoTransformar, botaoAjustes).forEach { barraDoTopo.addView(it) }
        barraDoTopo.addView(View(activity), LinearLayout.LayoutParams(0, 1, 1f))
        listOf(botaoPincel, botaoBorracha, botaoTexto, botaoCamadas).forEach { barraDoTopo.addView(it) }
        barraDoTopo.addView(botaoCor, LinearLayout.LayoutParams(dp(44f), dp(44f)).apply { marginStart = dp(4f) })
    }

    // ---------- Ciclo de vida ----------

    fun mostrar() {
        if (!montado) {
            area.addView(barraDoTopo, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52f), Gravity.TOP))
            area.addView(barraLateral, parametrosDaBarraLateral())
            area.addOnLayoutChangeListener { _, _, topo, _, base, _, topoAntes, _, baseAntes ->
                if (base - topo != baseAntes - topoAntes) area.post { ajustarAlturaDosControles() }
            }
            area.post { ajustarAlturaDosControles() }
            previaDoTamanho.visibility = View.GONE
            area.addView(
                previaDoTamanho,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
            )
            area.addView(
                barraDaSelecao,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
                ).apply { bottomMargin = dp(24f) },
            )
            montado = true
        }
        barraDoTopo.visibility = View.VISIBLE
        barraLateral.visibility = View.VISIBLE
        editor.estabilizacaoDoTraco = prefs.getFloat("estabilizacao", 0f)
        editor.curvaDePressao = curvaDePressaoGravada()
        editor.toleranciaDoBalde = prefs.getFloat("tolerancia-do-balde", com.chromebrothers.navegador.regras.PreenchimentoDoBalde.TOLERANCIA_PADRAO)
        barraDoTopo.bringToFront()
        barraLateral.bringToFront()
        barraDaSelecao.bringToFront()
        sincronizar()
    }

    fun esconder() {
        fecharPaineis()
        if (!montado) return
        barraDoTopo.visibility = View.GONE
        barraLateral.visibility = View.GONE
        barraDaSelecao.visibility = View.GONE
        previaDoTamanho.visibility = View.GONE
    }

    fun fecharPaineis() {
        popupAberto?.dismiss()
        popupAberto = null
    }

    /**
     * Tamanho e opacidade bem altos, como no Procreate: dividem a altura livre da área
     * (descontados topo, conta-gotas, divisória e desfazer/refazer), entre 150 e 320 dp cada.
     */
    private fun ajustarAlturaDosControles() {
        if (area.height <= 0) return
        val livre = area.height - dp(52f) - dp(24f)
        val fixo = dp(14f) + dp(10f) + dp(56f) + dp(15f) + dp(88f)
        val cada = ((livre - fixo) / 2).coerceIn(dp(ALTURA_MINIMA_DO_CONTROLE_DP), dp(ALTURA_MAXIMA_DO_CONTROLE_DP))
        listOf(controleDoTamanho, controleDaOpacidade).forEach { controle ->
            val parametros = controle.layoutParams as? LinearLayout.LayoutParams ?: return@forEach
            if (parametros.height != cada) {
                parametros.height = cada
                controle.layoutParams = parametros
            }
        }
    }

    private fun parametrosDaBarraLateral() = FrameLayout.LayoutParams(
        dp(52f),
        ViewGroup.LayoutParams.WRAP_CONTENT,
        (if (barraLateralNaDireita()) Gravity.END else Gravity.START) or Gravity.CENTER_VERTICAL,
    ).apply { marginStart = dp(12f); marginEnd = dp(12f); topMargin = dp(52f) }

    private fun barraLateralNaDireita() = prefs.getBoolean("barra-lateral-na-direita", false)

    private fun trocarLadoDaBarraLateral() {
        prefs.edit().putBoolean("barra-lateral-na-direita", !barraLateralNaDireita()).apply()
        barraLateral.layoutParams = parametrosDaBarraLateral()
    }

    /** Reflete o estado do editor (ferramenta, cor, tamanho, opacidade, seleção). */
    fun sincronizar() {
        if (!montado) return
        val ferramenta = editor.ferramentaAtual()
        val texto = hospede.textoAtivo()
        marcar(botaoPincel, pincelAtivo())
        marcar(botaoBorracha, !texto && ferramenta == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA)
        marcar(botaoTexto, texto)
        val recorte = !texto && ferramenta == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE
        marcar(botaoSelecao, recorte)
        marcar(botaoCamadas, hospede.camadasVisiveis())
        marcar(botaoContaGotas, editor.contaGotasArmado())
        bolinhaDaCor.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(hospede.corAtual() or 0xFF000000.toInt())
            setStroke(dp(2f), Color.parseColor("#F4F7FB"))
        }
        controleDoTamanho.valor = editor.tamanhoDaFerramenta
        controleDaOpacidade.valor = (editor.opacidadeDaFerramenta - 0.05f) / 0.95f
        barraLateral.alpha = if (recorte) 0.45f else 1f
        barraDaSelecao.visibility = if (recorte && barraDoTopo.visibility == View.VISIBLE) View.VISIBLE else View.GONE
        val laco = editor.formaDaSelecaoAtual() == FormaDaSelecaoSobreAImagem.LACO
        marcarAcao(botaoRetangulo, !laco)
        marcarAcao(botaoLaco, laco)
        botaoCancelarSelecao.visibility = if (editor.selecaoAtivaDaFerramentaDeRecorte()) View.VISIBLE else View.GONE
        aoSincronizarPainel?.invoke()
    }

    private fun borrachaAtiva(): Boolean =
        !hospede.textoAtivo() && editor.ferramentaAtual() == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA

    /** Pincel da borracha: a comum (redonda) ou qualquer pincel do motor, que apaga com a forma e a textura dele. */
    private fun abrirPainelDaBorracha(ancora: View) {
        val lista = colunaDoPainel()
        lista.addView(tituloDoPainel(R.string.estudio_borracha))
        CatalogoDePinceisDoMotor.iniciar(activity)
        val corDaAmostra = { Color.parseColor("#C4C7CE") }
        val opcoes = listOf<Pair<String?, String>>(null to "Borracha comum") +
            CatalogoDePinceisDoMotor.todos().map { it.id to it.nome }
        val linhas = opcoes.map { (id, nome) ->
            val pincelDaAmostra = id?.let { CatalogoDePinceisDoMotor.porId(it) }
                ?: com.chromebrothers.navegador.regras.PincelDoMotor(id = "comum", nome = nome, espacamento = 0.05f)
            val linha = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(10f), 0, dp(10f), 0)
                isClickable = true
                contentDescription = "Apagar com $nome"
                addView(AmostraDoPincelDoMotor(activity, pincelDaAmostra, corDaAmostra), LinearLayout.LayoutParams(dp(110f), dp(32f)))
                addView(TextView(activity).apply {
                    text = nome
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    setPadding(dp(12f), 0, 0, 0)
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                })
                setOnClickListener {
                    editor.escolherPincelDaBorracha(id)
                    aoSincronizarPainel?.invoke()
                }
            }
            lista.addView(linha, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48f)))
            linha to id
        }
        val atualizar = {
            val atual = editor.pincelDaBorrachaAtual()
            linhas.forEach { (linha, id) ->
                val sel = id == atual
                linha.background = if (sel) GradientDrawable().apply { cornerRadius = 12f * d; setColor(Color.parseColor("#292F3D")) } else null
                (linha.getChildAt(1) as TextView).setTextColor(if (sel) Color.parseColor("#AECBFA") else Color.parseColor("#F4F7FB"))
            }
        }
        atualizar()
        abrirPainel(ancora, lista, 300f, alinharADireita = true, aoSincronizar = atualizar)
    }

    private fun pincelAtivo(): Boolean {
        if (hospede.textoAtivo()) return false
        val f = editor.ferramentaAtual()
        return f == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO ||
            f == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BRANCO
    }

    // ---------- Painéis ----------

    private fun abrirPainel(
        ancora: View,
        conteudo: View,
        larguraDp: Float,
        alinharADireita: Boolean,
        aoSincronizar: (() -> Unit)? = null,
        aoFechar: (() -> Unit)? = null,
    ) {
        val jaAbertoAqui = popupAberto?.isShowing == true && popupAberto?.contentView?.tag == ancora
        fecharPaineis()
        if (jaAbertoAqui) return
        val largura = dp(larguraDp)
        // Painéis longos (ex.: pincéis) rolam em vez de sair da tela abaixo do botão.
        val rolagem = android.widget.ScrollView(activity).apply {
            tag = ancora
            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            background = GradientDrawable().apply { cornerRadius = 18f * d; setColor(fundoDosPaineis) }
            clipToOutline = true
            elevation = 12f * d
            addView(conteudo, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        val posicao = IntArray(2)
        ancora.getLocationInWindow(posicao)
        val alturaLivre = (ancora.rootView.height - posicao[1] - ancora.height - dp(24f)).coerceAtLeast(dp(220f))
        rolagem.measure(
            View.MeasureSpec.makeMeasureSpec(largura, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        )
        val altura = minOf(rolagem.measuredHeight, alturaLivre)
        val popup = PopupWindow(rolagem, largura, altura, true).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isOutsideTouchable = true
            elevation = 12f * d
            setOnDismissListener {
                if (popupAberto === this) {
                    popupAberto = null
                    aoSincronizarPainel = null
                }
                aoFechar?.invoke()
            }
        }
        popupAberto = popup
        aoSincronizarPainel = aoSincronizar
        val x = if (alinharADireita) ancora.width - largura else 0
        popup.showAsDropDown(ancora, x, dp(6f))
    }

    private fun abrirPainelDeAcoes(ancora: View) {
        val lista = colunaDoPainel()
        lista.addView(tituloDoPainel(R.string.estudio_acoes))
        lista.addView(linhaDeAcao(activity.getString(R.string.estudio_concluir_e_copiar), corAtiva) { fecharPaineis(); hospede.concluir() })
        lista.addView(linhaDeAcao(activity.getString(R.string.estudio_componentes)) { fecharPaineis(); hospede.componentes() })
        lista.addView(
            linhaDeAcao(activity.getString(R.string.estudio_tamanho_da_moldura, hospede.rotuloDoTamanhoDaMoldura())) {
                fecharPaineis(); hospede.tamanhoDaMoldura()
            },
        )
        lista.addView(
            linhaDeAcao(
                activity.getString(
                    if (barraLateralNaDireita()) R.string.estudio_barra_lateral_a_esquerda else R.string.estudio_barra_lateral_a_direita,
                ),
            ) { fecharPaineis(); trocarLadoDaBarraLateral() },
        )
        lista.addView(linhaDeAcao(activity.getString(R.string.estudio_inserir_da_biblioteca)) { fecharPaineis(); hospede.inserirDaBiblioteca() })
        lista.addView(
            linhaDeAcao(activity.getString(R.string.estudio_descartar_edicao), Color.parseColor("#F28B82")) {
                fecharPaineis(); hospede.descartar()
            },
        )
        abrirPainel(ancora, lista, 300f, alinharADireita = false)
    }

    private fun abrirPainelDaMoldura(ancora: View) {
        val lista = colunaDoPainel()
        lista.addView(tituloDoPainel(R.string.estudio_moldura))
        val atual = editor.proporcaoAtual()
        val opcoes = listOf(
            R.string.moldura_um_por_um to ProporcaoDaMolduraDaComposicao.QUADRADO,
            R.string.moldura_quatro_por_tres to ProporcaoDaMolduraDaComposicao.QUATRO_POR_TRES,
            R.string.moldura_dezesseis_por_nove to ProporcaoDaMolduraDaComposicao.DEZESSEIS_POR_NOVE,
            R.string.moldura_nove_por_dezesseis to ProporcaoDaMolduraDaComposicao.NOVE_POR_DEZESSEIS,
        )
        val grade = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        opcoes.forEach { (rotulo, proporcao) ->
            grade.addView(
                pilula(activity.getString(rotulo), atual == proporcao) { fecharPaineis(); hospede.definirProporcao(proporcao) },
                LinearLayout.LayoutParams(0, dp(44f), 1f).apply { marginEnd = dp(6f) },
            )
        }
        lista.addView(grade)
        lista.addView(linhaDeAcao(activity.getString(R.string.estudio_proporcao_original)) { fecharPaineis(); hospede.proporcaoOriginal() })
        lista.addView(
            linhaDeAcao(activity.getString(R.string.estudio_tamanho_da_moldura, hospede.rotuloDoTamanhoDaMoldura())) {
                fecharPaineis(); hospede.tamanhoDaMoldura()
            },
        )
        abrirPainel(ancora, lista, 320f, alinharADireita = false)
    }

    /** Brilho, contraste, saturação, matiz, desfoque e preto e branco, com prévia ao vivo na camada ativa. */
    private fun abrirPainelDeAjustes(ancora: View) {
        if (!editor.camadaAtivaAceitaAjustes()) {
            android.widget.Toast.makeText(activity, R.string.estudio_ajuste_sem_camada, android.widget.Toast.LENGTH_LONG).show()
            return
        }
        var valores = com.chromebrothers.navegador.regras.AjustesDeImagemDaCamada.Valores()
        var aplicado = false
        val lista = colunaDoPainel()
        lista.addView(tituloDoPainel(R.string.estudio_ajustes_da_camada))
        val controles = mutableListOf<Pair<android.widget.SeekBar, TextView>>()
        fun controle(texto: Int, minimo: Int, maximo: Int, atual: () -> Int, definir: (Int) -> Unit) {
            val rotulo = rotuloDeAjuste()
            fun escrever(v: Int) { rotulo.text = activity.getString(texto, v) }
            val barra = barraDeAjuste(atual() - minimo) { progresso, _ ->
                definir(progresso + minimo)
                escrever(progresso + minimo)
                editor.mostrarPreviaDoAjuste(valores)
            }
            barra.max = maximo - minimo
            barra.progress = atual() - minimo
            escrever(atual())
            barra.tag = minimo
            lista.addView(rotulo)
            lista.addView(barra, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(34f)))
            controles += barra to rotulo
        }
        controle(R.string.estudio_brilho, -100, 100, { valores.brilho }) { valores = valores.copy(brilho = it) }
        controle(R.string.estudio_contraste, -100, 100, { valores.contraste }) { valores = valores.copy(contraste = it) }
        controle(R.string.estudio_saturacao, -100, 100, { valores.saturacao }) { valores = valores.copy(saturacao = it) }
        controle(R.string.estudio_matiz, -180, 180, { valores.matiz }) { valores = valores.copy(matiz = it) }
        controle(R.string.estudio_desfoque, 0, 25, { valores.desfoque }) { valores = valores.copy(desfoque = it) }
        val pb = Switch(activity).apply {
            text = activity.getString(R.string.estudio_preto_e_branco)
            setTextColor(corDoIcone)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setPadding(dp(10f), dp(8f), dp(6f), dp(8f))
            setOnCheckedChangeListener { _, marcado ->
                valores = valores.copy(pretoEBranco = marcado)
                editor.mostrarPreviaDoAjuste(valores)
            }
        }
        lista.addView(pb)
        val acoes = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }
        acoes.addView(linhaDeAcao(activity.getString(R.string.estudio_redefinir), corDoIcone) {
            valores = com.chromebrothers.navegador.regras.AjustesDeImagemDaCamada.Valores()
            controles.forEach { (barra, _) -> barra.progress = -(barra.tag as Int) }
            controles[0].second.text = activity.getString(R.string.estudio_brilho, 0)
            controles[1].second.text = activity.getString(R.string.estudio_contraste, 0)
            controles[2].second.text = activity.getString(R.string.estudio_saturacao, 0)
            controles[3].second.text = activity.getString(R.string.estudio_matiz, 0)
            controles[4].second.text = activity.getString(R.string.estudio_desfoque, 0)
            pb.isChecked = false
            editor.cancelarPreviaDoAjuste()
        })
        acoes.addView(linhaDeAcao(activity.getString(R.string.estudio_aplicar), corAtiva) {
            if (valores.neutro) {
                fecharPaineis()
                return@linhaDeAcao
            }
            aplicado = true
            fecharPaineis()
            android.widget.Toast.makeText(activity, R.string.estudio_aplicando, android.widget.Toast.LENGTH_SHORT).show()
            editor.aplicarAjusteNaCamada(valores) { ok ->
                if (!ok) {
                    editor.cancelarPreviaDoAjuste()
                    android.widget.Toast.makeText(activity, R.string.estudio_ajuste_falhou, android.widget.Toast.LENGTH_LONG).show()
                }
                sincronizar()
            }
        })
        lista.addView(acoes)
        abrirPainel(ancora, lista, 320f, alinharADireita = false, aoFechar = {
            // Fechar sem aplicar desfaz a prévia.
            if (!aplicado) editor.cancelarPreviaDoAjuste()
        })
    }

    private fun abrirPainelDoPincel(ancora: View) {
        val lista = colunaDoPainel()
        lista.addView(tituloDoPainel(R.string.estudio_pincel))
        val pontas = listOf(
            R.string.estudio_ponta_caneta to PontaDoTracoDaComposicao.CANETA,
            R.string.marcador_do_traco to PontaDoTracoDaComposicao.MARCADOR,
            R.string.nanquim_do_traco to PontaDoTracoDaComposicao.NANQUIM,
            R.string.lapis_do_traco to PontaDoTracoDaComposicao.LAPIS,
            R.string.seta_do_traco to PontaDoTracoDaComposicao.SETA,
        )
        val linhas = pontas.map { (rotulo, ponta) ->
            val amostra = AmostraDaPonta(activity, ponta) { hospede.corAtual() }
            val linha = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(10f), 0, dp(10f), 0)
                isClickable = true
                contentDescription = activity.getString(rotulo)
                addView(amostra, LinearLayout.LayoutParams(dp(110f), dp(32f)))
                addView(TextView(activity).apply {
                    text = activity.getString(rotulo)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    setPadding(dp(12f), 0, 0, 0)
                })
                setOnClickListener { escolherPonta(ponta) }
            }
            lista.addView(linha, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48f)))
            Triple(linha, ponta, amostra)
        }
        lista.addView(divisoriaHorizontal())
        CatalogoDePinceisDoMotor.iniciar(activity)
        val linhasDoMotor = CatalogoDePinceisDoMotor.todos().map { pincel ->
            val amostra = AmostraDoPincelDoMotor(activity, pincel) { hospede.corAtual() }
            val linha = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(10f), 0, dp(10f), 0)
                isClickable = true
                contentDescription = pincel.nome
                addView(amostra, LinearLayout.LayoutParams(dp(110f), dp(32f)))
                addView(TextView(activity).apply {
                    text = pincel.nome
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    setPadding(dp(12f), 0, 0, 0)
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                // Lápis: abre o Estúdio de pincéis (pronto vira cópia em "Meus pincéis").
                addView(TextView(activity).apply {
                    text = "✎"
                    gravity = Gravity.CENTER
                    setTextColor(corDoIcone)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                    contentDescription = "Editar ${pincel.nome} no Estúdio de pincéis"
                    background = fundoDeToque()
                    isClickable = true
                    setOnClickListener { abrirEstudioDePinceis(pincel, comoCopia = false) }
                }, LinearLayout.LayoutParams(dp(40f), dp(40f)))
                setOnClickListener { if (editor.pincelDoMotorAtual() != pincel.id) hospede.escolherPincelDoMotor(pincel.id) }
                setOnLongClickListener { abrirEstudioDePinceis(pincel, comoCopia = false); true }
            }
            lista.addView(linha, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48f)))
            Triple(linha, pincel.id, amostra)
        }
        lista.addView(linhaDeAcao("+ Novo pincel", corAtiva) {
            val base = editor.pincelDoMotorAtual()?.let { CatalogoDePinceisDoMotor.porId(it) }
                ?: com.chromebrothers.navegador.regras.PincelDoMotor(id = "novo", nome = "Novo pincel", espacamento = 0.08f)
            abrirEstudioDePinceis(base, comoCopia = true)
        })
        val meus = CatalogoDePinceisDoMotor.meusPinceis()
        if (meus.isNotEmpty()) {
            lista.addView(linhaDeAcao("Exportar meus pincéis (${meus.size})") {
                fecharPaineis()
                exportarPinceis(meus, "Meus pincéis")
            })
        }
        lista.addView(linhaDeAcao("Importar pincéis (também do Procreate)…") {
            fecharPaineis()
            hospede.pedirArquivoDePinceis { uri -> if (uri != null) importarPinceis(uri) }
        })
        val dedo = Switch(activity).apply {
            text = activity.getString(R.string.estudio_desenhar_com_o_dedo)
            setTextColor(corDoIcone)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setPadding(dp(10f), dp(8f), dp(6f), dp(8f))
            isChecked = editor.desenhaComODedo()
            setOnCheckedChangeListener { _, marcado -> if (marcado != editor.desenhaComODedo()) hospede.alternarDesenhoComODedo() }
        }
        lista.addView(divisoriaHorizontal())
        val rotuloDaEstabilizacao = rotuloDeAjuste()
        val estabilizacao = barraDeAjuste(Math.round(editor.estabilizacaoDoTraco * 100)) { valor, fim ->
            editor.estabilizacaoDoTraco = valor / 100f
            rotuloDaEstabilizacao.text = textoDaEstabilizacao(valor)
            if (fim) prefs.edit().putFloat("estabilizacao", valor / 100f).apply()
        }
        rotuloDaEstabilizacao.text = textoDaEstabilizacao(Math.round(editor.estabilizacaoDoTraco * 100))
        lista.addView(rotuloDaEstabilizacao)
        lista.addView(estabilizacao, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(36f)))
        // Curva de pressão: abre o gráfico (estilo Procreate) com área de teste para a caneta.
        val rotuloDaPressao = rotuloDeAjuste()
        rotuloDaPressao.text = textoDaCurvaDePressao(editor.curvaDePressao)
        rotuloDaPressao.setPadding(dp(10f), dp(12f), dp(6f), dp(12f))
        rotuloDaPressao.setOnClickListener {
            EditorDaCurvaDePressaoDaCaneta(activity, editor.curvaDePressao) { nova ->
                editor.curvaDePressao = nova
                prefs.edit().putString(CHAVE_DA_CURVA_DE_PRESSAO, nova.paraTexto()).apply()
                rotuloDaPressao.text = textoDaCurvaDePressao(nova)
            }.mostrar()
        }
        lista.addView(rotuloDaPressao)
        lista.addView(divisoriaHorizontal())
        lista.addView(dedo)
        val atualizar = {
            val atual = editor.pontaAtual()
            val doMotor = editor.pincelDoMotorAtual()
            fun pintarLinha(linha: LinearLayout, sel: Boolean) {
                linha.background = if (sel) {
                    GradientDrawable().apply { cornerRadius = 12f * d; setColor(Color.parseColor("#292F3D")) }
                } else {
                    null
                }
                (linha.getChildAt(1) as TextView).setTextColor(if (sel) Color.parseColor("#AECBFA") else Color.parseColor("#F4F7FB"))
            }
            linhas.forEach { (linha, ponta, amostra) ->
                pintarLinha(linha, doMotor == null && ponta == atual)
                amostra.invalidate()
            }
            linhasDoMotor.forEach { (linha, id, amostra) ->
                pintarLinha(linha, doMotor == id)
                amostra.invalidate()
            }
            dedo.isChecked = editor.desenhaComODedo()
        }
        atualizar()
        abrirPainel(ancora, lista, 300f, alinharADireita = true, aoSincronizar = atualizar)
    }

    private fun textoDaEstabilizacao(valor: Int) =
        if (valor <= 0) activity.getString(R.string.estudio_estabilizacao_desligada)
        else activity.getString(R.string.estudio_estabilizacao, valor)

    /** Curva gravada; quem só usava o controle antigo (−1..1) ganha a curva equivalente. */
    private fun curvaDePressaoGravada(): com.chromebrothers.navegador.regras.CurvaDePressaoDaCaneta {
        val regra = com.chromebrothers.navegador.regras.CurvaDePressaoDaCaneta
        return regra.deTexto(prefs.getString(CHAVE_DA_CURVA_DE_PRESSAO, null))
            ?: regra.doAjusteAntigo(prefs.getFloat("curva-da-pressao", 0f))
    }

    private fun textoDaCurvaDePressao(curva: com.chromebrothers.navegador.regras.CurvaDePressaoDaCaneta): String {
        val regra = com.chromebrothers.navegador.regras.CurvaDePressaoDaCaneta
        val nome = when (curva) {
            regra.LINEAR -> "normal"
            regra.LEVE -> "leve"
            regra.FIRME -> "firme"
            else -> "personalizada"
        }
        return "Curva de pressão: $nome  ›"
    }

    private fun rotuloDeAjuste() = TextView(activity).apply {
        setTextColor(corDoIcone)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        setPadding(dp(10f), dp(6f), 0, 0)
    }

    private fun barraDeAjuste(inicial: Int, aoMudar: (valor: Int, fim: Boolean) -> Unit) =
        android.widget.SeekBar(activity).apply {
            max = 100
            progress = inicial.coerceIn(0, 100)
            setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(barra: android.widget.SeekBar?, valor: Int, doUsuario: Boolean) {
                    if (doUsuario) aoMudar(valor, false)
                }
                override fun onStartTrackingTouch(barra: android.widget.SeekBar?) = Unit
                override fun onStopTrackingTouch(barra: android.widget.SeekBar?) {
                    aoMudar(barra?.progress ?: inicial, true)
                }
            })
        }

    private fun exportarPinceis(pinceis: List<com.chromebrothers.navegador.regras.PincelDoMotor>, nome: String) {
        Thread {
            val salvo = runCatching { PacoteDePinceisNoAparelho.exportar(activity, pinceis, nome) }.getOrNull()
            activity.runOnUiThread {
                android.widget.Toast.makeText(
                    activity,
                    if (salvo != null) "Salvo em Downloads/ChromeBrothers/$salvo" else "Não consegui exportar os pincéis.",
                    android.widget.Toast.LENGTH_LONG,
                ).show()
            }
        }.start()
    }

    private fun importarPinceis(uri: android.net.Uri) {
        Thread {
            val resultado = runCatching { PacoteDePinceisNoAparelho.importar(activity, uri) }
                .getOrElse { PacoteDePinceisNoAparelho.Importacao.Falhou("Não consegui importar.") }
            activity.runOnUiThread {
                val texto = when (resultado) {
                    is PacoteDePinceisNoAparelho.Importacao.Ok -> {
                        val base = if (resultado.pinceis.size == 1) "Pincel \"${resultado.pinceis[0].nome}\" importado." else "${resultado.pinceis.size} pincéis importados."
                        if (resultado.aviso != null) "$base ${resultado.aviso}" else base
                    }
                    is PacoteDePinceisNoAparelho.Importacao.Falhou -> resultado.mensagem
                }
                android.widget.Toast.makeText(activity, texto, android.widget.Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun abrirEstudioDePinceis(base: com.chromebrothers.navegador.regras.PincelDoMotor, comoCopia: Boolean) {
        fecharPaineis()
        EstudioDePinceisDoEditorDeImagem(
            activity = activity,
            base = base,
            comoCopia = comoCopia,
            cor = { hospede.corAtual() },
            pedirImagem = { receber -> hospede.pedirImagemParaFormaDoPincel(receber) },
            exportar = { pincel -> exportarPinceis(listOf(pincel), pincel.nome) },
            aoSalvar = { salvo ->
                // Recarrega do catálogo: o editor passa a desenhar com a versão nova.
                hospede.escolherPincelDoMotor(salvo.id)
                sincronizar()
            },
            aoApagar = { id ->
                if (editor.pincelDoMotorAtual() == id) hospede.escolherPincelDoMotor(null)
                sincronizar()
            },
        ).mostrar()
    }

    /** O editor alterna a ponta (tocar de novo volta à caneta); aqui o toque sempre escolhe a ponta tocada. */
    private fun escolherPonta(ponta: PontaDoTracoDaComposicao) {
        if (editor.pincelDoMotorAtual() != null) {
            // Saindo de um pincel do motor: a caneta já é a ponta de base.
            hospede.escolherPincelDoMotor(null)
            if (ponta == PontaDoTracoDaComposicao.CANETA) return
        }
        val atual = editor.pontaAtual()
        if (atual == ponta) return
        if (ponta == PontaDoTracoDaComposicao.CANETA) hospede.escolherPonta(atual) else hospede.escolherPonta(ponta)
    }

    private fun abrirPainelDaCor(ancora: View) {
        val lista = colunaDoPainel()
        lista.addView(tituloDoPainel(R.string.estudio_cor))
        val hsv = FloatArray(3)
        Color.colorToHSV(hospede.corAtual() or 0xFF000000.toInt(), hsv)
        val brilho = BarraDeBrilho(activity)
        val disco = DiscoDeCor(activity)
        disco.definir(hsv[0], hsv[1])
        brilho.valor = hsv[2]
        fun corEscolhida() = Color.HSVToColor(floatArrayOf(disco.matiz, disco.saturacao, brilho.valor))
        val aplicar = { fim: Boolean ->
            val cor = corEscolhida()
            brilho.corPura = Color.HSVToColor(floatArrayOf(disco.matiz, disco.saturacao, 1f))
            hospede.escolherCor(cor)
            if (fim) lembrarRecente(cor)
        }
        disco.aoMudar = aplicar
        brilho.aoMudar = aplicar
        brilho.corPura = Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], 1f))
        lista.addView(disco, LinearLayout.LayoutParams(dp(220f), dp(220f)).apply { gravity = Gravity.CENTER_HORIZONTAL })
        lista.addView(brilho, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(32f)).apply { setMargins(0, dp(10f), 0, dp(4f)) })

        lista.addView(rotuloDaSecao(R.string.estudio_paleta))
        lista.addView(linhaDeAmostras(PaletaDoEditorDeImagem.cores.toList()) { cor ->
            hospede.escolherCor(cor); lembrarRecente(cor); fecharPaineis()
        })
        val recentes = recentes()
        if (recentes.isNotEmpty()) {
            lista.addView(rotuloDaSecao(R.string.estudio_recentes))
            lista.addView(linhaDeAmostras(recentes) { cor -> hospede.escolherCor(cor); fecharPaineis() })
        }
        lista.addView(rotuloDaSecao(R.string.estudio_fundo_da_folha))
        lista.addView(linhaDeAmostras(PaletaDoEditorDeImagem.cores.toList(), tamanhoDp = 28f) { cor -> hospede.definirCorDaFolha(cor) })
        lista.addView(linhaDeAcao(activity.getString(R.string.estudio_pegar_cor_da_imagem)) {
            fecharPaineis()
            if (!editor.contaGotasArmado()) hospede.alternarContaGotas()
        })
        lista.addView(rotuloDaSecao(R.string.estudio_balde))
        val rotuloDaTolerancia = rotuloDeAjuste()
        val tolerancia = barraDeAjuste(Math.round(editor.toleranciaDoBalde * 100)) { valor, fim ->
            editor.toleranciaDoBalde = valor / 100f
            rotuloDaTolerancia.text = activity.getString(R.string.estudio_balde_tolerancia, valor)
            if (fim) prefs.edit().putFloat("tolerancia-do-balde", valor / 100f).apply()
        }
        rotuloDaTolerancia.text = activity.getString(R.string.estudio_balde_tolerancia, Math.round(editor.toleranciaDoBalde * 100))
        lista.addView(rotuloDaTolerancia)
        lista.addView(tolerancia, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(36f)))
        lista.addView(TextView(activity).apply {
            text = activity.getString(R.string.estudio_balde_dica)
            setTextColor(Color.parseColor("#9AA0A6"))
            textSize = 12f
            setPadding(dp(4f), dp(2f), dp(4f), dp(6f))
        })
        abrirPainel(ancora, lista, 300f, alinharADireita = true)
    }

    private fun recentes(): List<Int> =
        prefs.getString("cores-recentes", "").orEmpty().split(',').mapNotNull { it.toIntOrNull() }

    private fun lembrarRecente(cor: Int) {
        val lista = (listOf(cor or 0xFF000000.toInt()) + recentes()).distinct().take(6)
        prefs.edit().putString("cores-recentes", lista.joinToString(",")).apply()
    }

    // ---------- Peças de interface ----------

    private fun fundoDeToque(): android.graphics.drawable.Drawable? {
        val valor = TypedValue()
        activity.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, valor, true)
        return activity.getDrawable(valor.resourceId)
    }

    private fun icone(res: Int, descricao: Int, tamanho: Float = 44f, aoTocar: (View) -> Unit): ImageButton =
        ImageButton(activity).apply {
            setImageResource(res)
            imageTintList = ColorStateList.valueOf(corDoIcone)
            contentDescription = activity.getString(descricao)
            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            setPadding(dp(10f), dp(10f), dp(10f), dp(10f))
            background = fundoDeToque()
            layoutParams = LinearLayout.LayoutParams(dp(tamanho), dp(tamanho))
            setOnClickListener { aoTocar(it) }
        }

    private fun marcar(botao: ImageButton, ativo: Boolean) {
        botao.imageTintList = ColorStateList.valueOf(if (ativo) corAtiva else corDoIcone)
        botao.background = if (ativo) {
            GradientDrawable().apply { cornerRadius = 10f * d; setColor(Color.parseColor("#248AB4F8")) }
        } else {
            fundoDeToque()
        }
    }

    private fun acaoDaSelecao(res: Int, rotulo: Int, aoTocar: () -> Unit): TextView = TextView(activity).apply {
        text = activity.getString(rotulo)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        gravity = Gravity.CENTER_VERTICAL
        minHeight = dp(44f)
        setPadding(dp(12f), 0, dp(14f), 0)
        val ic = activity.getDrawable(res)?.mutate()
        ic?.setBounds(0, 0, dp(20f), dp(20f))
        setCompoundDrawables(ic, null, null, null)
        compoundDrawablePadding = dp(8f)
        isClickable = true
        setOnClickListener { aoTocar() }
    }

    private fun marcarAcao(botao: TextView, ativo: Boolean) {
        val cor = if (ativo) Color.parseColor("#AECBFA") else corDoIcone
        botao.setTextColor(cor)
        botao.compoundDrawables[0]?.setTint(cor)
        botao.background = if (ativo) {
            GradientDrawable().apply { cornerRadius = 12f * d; setColor(Color.parseColor("#298AB4F8")) }
        } else {
            null
        }
    }

    private fun divisoria() = View(activity).apply {
        setBackgroundColor(Color.parseColor("#2E333C"))
        layoutParams = LinearLayout.LayoutParams(dp(1f), dp(24f)).apply { setMargins(dp(6f), 0, dp(6f), 0) }
    }

    private fun divisoriaHorizontal() = View(activity).apply {
        setBackgroundColor(Color.parseColor("#2E333C"))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1f)).apply { setMargins(dp(4f), dp(6f), dp(4f), dp(6f)) }
    }

    private fun colunaDoPainel() = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14f), dp(12f), dp(14f), dp(12f))
    }

    private fun tituloDoPainel(texto: Int) = TextView(activity).apply {
        text = activity.getString(texto)
        setTextColor(Color.parseColor("#F4F7FB"))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        paint.isFakeBoldText = true
        setPadding(dp(4f), 0, 0, dp(10f))
    }

    private fun rotuloDaSecao(texto: Int) = TextView(activity).apply {
        text = activity.getString(texto)
        setTextColor(Color.parseColor("#8A9099"))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        isAllCaps = true
        setPadding(dp(4f), dp(10f), 0, dp(6f))
    }

    private fun linhaDeAcao(texto: String, cor: Int = Color.parseColor("#F4F7FB"), aoTocar: () -> Unit) = TextView(activity).apply {
        text = texto
        setTextColor(cor)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        gravity = Gravity.CENTER_VERTICAL
        minHeight = dp(46f)
        setPadding(dp(8f), 0, dp(8f), 0)
        background = fundoDeToque()
        isClickable = true
        setOnClickListener { aoTocar() }
    }

    private fun pilula(texto: String, ativa: Boolean, aoTocar: () -> Unit) = TextView(activity).apply {
        text = texto
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setTextColor(if (ativa) Color.parseColor("#AECBFA") else Color.parseColor("#F4F7FB"))
        background = GradientDrawable().apply {
            cornerRadius = 12f * d
            setColor(Color.parseColor(if (ativa) "#298AB4F8" else "#262A33"))
        }
        isClickable = true
        setOnClickListener { aoTocar() }
    }

    private fun linhaDeAmostras(cores: List<Int>, tamanhoDp: Float = 34f, aoTocar: (Int) -> Unit) = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        cores.forEach { cor ->
            addView(View(activity).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(cor)
                    setStroke(dp(1f), Color.parseColor("#33FFFFFF"))
                }
                contentDescription = String.format("#%06X", cor and 0xFFFFFF)
                isClickable = true
                setOnClickListener { aoTocar(cor) }
            }, LinearLayout.LayoutParams(dp(tamanhoDp), dp(tamanhoDp)).apply { marginEnd = dp(10f) })
        }
    }

    // ---------- Views próprias ----------

    /** Controle deslizante vertical (0 embaixo, 1 em cima), como na barra lateral do protótipo. */
    /**
     * Controle vertical estilo Procreate: arraste relativo (a alça não pula para o dedo, salvo tocando longe dela),
     * ajuste fino afastando o dedo para o lado e o valor em % na alça enquanto arrasta.
     */
    private class ControleDeslizanteVertical(context: Context) : View(context) {
        var aoMudar: ((Float) -> Unit)? = null
        var aoSoltar: (() -> Unit)? = null
        var rotulo: ((Float) -> String)? = null
        var valor = 0.5f
            set(v) { field = v.coerceIn(0f, 1f); invalidate() }
        private val dd = context.resources.displayMetrics.density
        private val trilho = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2E333C") }
        private val cheio = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#5C6570") }
        private val alca = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F4F7FB") }
        private val texto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1D24")
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textSize = 11f * context.resources.displayMetrics.scaledDensity
        }
        private val margem get() = 12f * dd
        private var arrastando = false
        private var ultimoY = 0f

        private fun yDoValor(): Float = height - margem - valor * (height - 2 * margem)

        override fun onDraw(canvas: Canvas) {
            val cx = width / 2f
            val topo = margem
            val base = height - margem
            val y = yDoValor()
            canvas.drawRoundRect(cx - 4f * dd, topo, cx + 4f * dd, base, 4f * dd, 4f * dd, trilho)
            canvas.drawRoundRect(cx - 4f * dd, y, cx + 4f * dd, base, 4f * dd, 4f * dd, cheio)
            val textoDaAlca = if (arrastando) rotulo?.invoke(valor) else null
            if (textoDaAlca != null) {
                val meia = (width / 2f - 1f * dd).coerceAtLeast(16f * dd)
                canvas.drawRoundRect(cx - meia, y - 12f * dd, cx + meia, y + 12f * dd, 8f * dd, 8f * dd, alca)
                canvas.drawText(textoDaAlca, cx, y - (texto.descent() + texto.ascent()) / 2f, texto)
            } else {
                canvas.drawRoundRect(cx - 16f * dd, y - 9f * dd, cx + 16f * dd, y + 9f * dd, 6f * dd, 6f * dd, alca)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val util = (height - 2 * margem).coerceAtLeast(1f)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    arrastando = true
                    // Longe da alça: ela vem até o dedo; perto: arraste relativo, sem pulo.
                    if (kotlin.math.abs(event.y - yDoValor()) > 28f * dd) {
                        valor = 1f - (event.y - margem) / util
                        aoMudar?.invoke(valor)
                    }
                    ultimoY = event.y
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val distanciaLateral = kotlin.math.abs(event.x - width / 2f) - width / 2f
                    val fator = com.chromebrothers.navegador.regras.TamanhoDoPincelDoEditorDeImagem
                        .fatorDoAjusteFino(distanciaLateral, 60f * dd)
                    valor -= (event.y - ultimoY) / util * fator
                    ultimoY = event.y
                    aoMudar?.invoke(valor)
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    arrastando = false
                    invalidate()
                    aoSoltar?.invoke()
                    return true
                }
            }
            return super.onTouchEvent(event)
        }
    }

    /** Amostra de um pincel do motor: uma curva em S desenhada pelo próprio motor, com pressão subindo e descendo. */
    private class AmostraDoPincelDoMotor(
        context: Context,
        private val pincel: com.chromebrothers.navegador.regras.PincelDoMotor,
        private val cor: () -> Int,
    ) : View(context) {
        private val renderizador = RenderizadorDeCarimbosDoPincel()
        private val dd = context.resources.displayMetrics.density

        override fun onDraw(canvas: Canvas) {
            if (width <= 0 || height <= 0) return
            val margem = 10f * dd
            val largura = (5f * dd * pincel.multiplicadorDeLargura).coerceAtMost(height * 0.7f)
            val pontos = (0..40).map { i ->
                val t = i / 40f
                val pressao = kotlin.math.sin(t * Math.PI).toFloat().coerceAtLeast(0.15f)
                com.chromebrothers.navegador.regras.PontoParaCarimbar(
                    x = margem + t * (width - 2 * margem),
                    y = height / 2f + kotlin.math.sin(t * 2 * Math.PI).toFloat() * height * 0.22f,
                    largura = largura * com.chromebrothers.navegador.regras.MotorDeCarimbosDoPincel.fatorDaPressaoNoTamanho(pressao, pincel.pressaoNoTamanho),
                    alpha = com.chromebrothers.navegador.regras.MotorDeCarimbosDoPincel.fatorDaPressaoNaOpacidade(pressao, pincel.pressaoNaOpacidade),
                )
            }
            val carimbos = com.chromebrothers.navegador.regras.MotorDeCarimbosDoPincel.gerar(pontos, pincel, 7L, comAfinamentoDoFim = true)
            renderizador.desenharTraco(canvas, carimbos, cor() or 0xFF000000.toInt(), pincel)
        }
    }

    /** Prévia do tamanho do pincel: círculo no centro, não recebe toques. */
    private class PreviaDoTamanho(context: Context) : View(context) {
        var diametro = 10f
        var cor = Color.RED
        var borracha = false
        private val dd = context.resources.displayMetrics.density
        private val preenchimento = Paint(Paint.ANTI_ALIAS_FLAG)
        private val contorno = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

        init {
            isClickable = false
            isFocusable = false
        }

        override fun onTouchEvent(event: MotionEvent): Boolean = false

        override fun onDraw(canvas: Canvas) {
            val r = (diametro / 2f).coerceAtLeast(1.5f * dd)
            val cx = width / 2f
            val cy = height / 2f
            if (borracha) {
                preenchimento.color = Color.parseColor("#55FFFFFF")
            } else {
                preenchimento.color = (cor and 0x00FFFFFF) or (0xE6 shl 24)
            }
            canvas.drawCircle(cx, cy, r, preenchimento)
            contorno.strokeWidth = 1.5f * dd
            contorno.color = Color.parseColor("#CC000000")
            canvas.drawCircle(cx, cy, r + 1.5f * dd, contorno)
            contorno.color = Color.WHITE
            canvas.drawCircle(cx, cy, r + 3f * dd, contorno)
        }
    }

    /** Disco de matiz (ângulo) × saturação (raio); o brilho fica na barra de baixo. */
    private class DiscoDeCor(context: Context) : View(context) {
        var matiz = 0f
        var saturacao = 1f
        var aoMudar: ((Boolean) -> Unit)? = null
        private val dd = context.resources.displayMetrics.density
        private val pinta = Paint(Paint.ANTI_ALIAS_FLAG)
        private val branco = Paint(Paint.ANTI_ALIAS_FLAG)
        private val marcador = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.WHITE }
        private val sombra = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.parseColor("#80000000") }

        fun definir(m: Float, s: Float) { matiz = m; saturacao = s; invalidate() }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            val cx = w / 2f
            val cy = h / 2f
            val cores = intArrayOf(
                Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED,
            )
            pinta.shader = SweepGradient(cx, cy, cores, null)
            branco.shader = RadialGradient(cx, cy, min(w, h) / 2f, Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP)
        }

        override fun onDraw(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f
            val r = min(width, height) / 2f
            canvas.drawCircle(cx, cy, r, pinta)
            canvas.drawCircle(cx, cy, r, branco)
            val ang = Math.toRadians(matiz.toDouble())
            val px = cx + (Math.cos(ang) * saturacao * r).toFloat()
            val py = cy + (Math.sin(ang) * saturacao * r).toFloat()
            sombra.strokeWidth = 5f * dd
            marcador.strokeWidth = 3f * dd
            canvas.drawCircle(px, py, 11f * dd, sombra)
            canvas.drawCircle(px, py, 11f * dd, marcador)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val cx = width / 2f
            val cy = height / 2f
            val r = min(width, height) / 2f
            val dx = event.x - cx
            val dy = event.y - cy
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    var graus = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
                    if (graus < 0) graus += 360f
                    definir(graus, (hypot(dx, dy) / r).coerceIn(0f, 1f))
                    aoMudar?.invoke(event.actionMasked == MotionEvent.ACTION_UP)
                    return true
                }
            }
            return super.onTouchEvent(event)
        }
    }

    /** Brilho: do preto à cor pura escolhida no disco. */
    private class BarraDeBrilho(context: Context) : View(context) {
        var valor = 1f
            set(v) { field = v.coerceIn(0f, 1f); invalidate() }
        var corPura = Color.RED
            set(v) { field = v; invalidate() }
        var aoMudar: ((Boolean) -> Unit)? = null
        private val dd = context.resources.displayMetrics.density
        private val pinta = Paint(Paint.ANTI_ALIAS_FLAG)
        private val alca = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.WHITE }

        override fun onDraw(canvas: Canvas) {
            val m = 12f * dd
            val cy = height / 2f
            pinta.shader = LinearGradient(m, 0f, width - m, 0f, Color.BLACK, corPura, Shader.TileMode.CLAMP)
            canvas.drawRoundRect(m, cy - 7f * dd, width - m, cy + 7f * dd, 7f * dd, 7f * dd, pinta)
            val x = m + valor * (width - 2 * m)
            alca.strokeWidth = 3f * dd
            canvas.drawCircle(x, cy, 10f * dd, alca)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    val m = 12f * dd
                    valor = (event.x - m) / (width - 2 * m).coerceAtLeast(1f)
                    aoMudar?.invoke(event.actionMasked == MotionEvent.ACTION_UP)
                    return true
                }
            }
            return super.onTouchEvent(event)
        }
    }

    /** Amostra de traço de cada ponta no painel do pincel, na cor atual. */
    private class AmostraDaPonta(
        context: Context,
        private val ponta: PontaDoTracoDaComposicao,
        private val cor: () -> Int,
    ) : View(context) {
        private val dd = context.resources.displayMetrics.density
        private val pinta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            val base = cor() or 0xFF000000.toInt()
            pinta.pathEffect = null
            pinta.color = base
            val curva = Path().apply {
                moveTo(6f * dd, h * 0.7f)
                cubicTo(w * 0.3f, h * 0.05f, w * 0.5f, h * 0.15f, w * 0.6f, h * 0.5f)
                cubicTo(w * 0.7f, h * 0.9f, w * 0.85f, h * 0.8f, w - 6f * dd, h * 0.25f)
            }
            when (ponta) {
                PontaDoTracoDaComposicao.CANETA -> { pinta.strokeWidth = 3f * dd; canvas.drawPath(curva, pinta) }
                PontaDoTracoDaComposicao.MARCADOR -> {
                    pinta.strokeWidth = 10f * dd
                    pinta.color = (base and 0x00FFFFFF) or (0x66 shl 24)
                    canvas.drawPath(curva, pinta)
                }
                PontaDoTracoDaComposicao.NANQUIM -> { pinta.strokeWidth = 5f * dd; canvas.drawPath(curva, pinta) }
                PontaDoTracoDaComposicao.LAPIS -> {
                    pinta.strokeWidth = 2f * dd
                    pinta.pathEffect = DashPathEffect(floatArrayOf(2f * dd, 2.5f * dd), 0f)
                    canvas.drawPath(curva, pinta)
                }
                PontaDoTracoDaComposicao.SETA -> {
                    pinta.strokeWidth = 3f * dd
                    canvas.drawLine(6f * dd, h * 0.75f, w - 10f * dd, h * 0.3f, pinta)
                    val seta = Path().apply {
                        moveTo(w - 22f * dd, h * 0.2f)
                        lineTo(w - 10f * dd, h * 0.3f)
                        lineTo(w - 19f * dd, h * 0.62f)
                    }
                    canvas.drawPath(seta, pinta)
                }
            }
        }
    }
}

package com.chromebrothers.navegador

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.chromebrothers.navegador.regras.AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador

/**
 * Faixa de abas (Chat + Gravador sempre + componentes) e hosts na gaveta.
 */
class ControladorDasAbasAncoradasNaGavetaDoChatDoNavegador(
    private val activity: AppCompatActivity,
    private val gestor: GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador,
    private val abrirGaveta: () -> Unit,
    private val aoPedirAbrirGravadorNaGaveta: () -> Unit,
    private val aoPedirMenuDeModoDoGravador: () -> Unit,
) {
    private val rolagemAbas: HorizontalScrollView =
        activity.findViewById(R.id.rolagem_das_abas_da_gaveta_do_chat)
    private val faixaAbas: LinearLayout =
        activity.findViewById(R.id.faixa_das_abas_da_gaveta_do_chat)
    private val barraContextoChat: View =
        activity.findViewById(R.id.barra_de_contexto_do_chat_na_gaveta)
    private val hostComponentes: FrameLayout =
        activity.findViewById(R.id.host_dos_componentes_ancorados_na_gaveta_do_chat)
    private val corpoChat: View =
        activity.findViewById(R.id.corpo_do_chat_da_openrouter)
    private val hostGravador: View =
        activity.findViewById(R.id.host_do_gravador_na_gaveta_do_chat)

    private val corpoComSwipe: CorpoDasAbasDaGavetaDoChatComSwipeEntreAbas =
        activity.findViewById(R.id.corpo_das_abas_da_gaveta_do_chat_com_swipe)
    private val painelDaConfiguracaoDoChat: View? =
        activity.findViewById(R.id.painel_da_chave_da_openrouter)

    private var estado = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Estado()
    private val janelasPorId = linkedMapOf<String, PainelDaColecaoFlutuanteDoChatDoNavegador>()
    private var campoDeTextoAncorado: PainelDoCampoDeTextoDoNavegador? = null
    private var idPairadoPeloFantasma: String? = null
    private var trocaPorPairarFantasma: Runnable? = null

    fun ligar() {
        gestor.ligarAncorarNaGaveta { janela -> ancorar(janela) }
        corpoComSwipe.temAbaVizinha = { paraAProxima ->
            painelDaConfiguracaoDoChat?.visibility != View.VISIBLE && abaVizinha(paraAProxima) != null
        }
        corpoComSwipe.trocarParaAbaVizinha = { paraAProxima ->
            abaVizinha(paraAProxima)?.let { ativar(it) }
        }
        corpoComSwipe.consultarSeAPaginaQuerOGesto = { x, y, responder ->
            val janela = janelasPorId[estado.idAtiva]
            if (janela == null) responder(false) else janela.consultarSeAPaginaQuerGestoHorizontal(x, y, responder)
        }
        redesenharAbas()
        aplicarPainelAtivo()
    }

    private fun abaVizinha(paraAProxima: Boolean): String? =
        com.chromebrothers.navegador.regras.GestoDeSwipeEntreAbasNoCorpoDaGavetaDoChat.abaVizinha(
            AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.idsDoSwipe(estado),
            estado.idAtiva,
            paraAProxima,
        )

    /** Fantasma parado ~700ms sobre uma aba do chat foca essa aba (mesma regra das abas do navegador). */
    fun pairarFantasma(xNaTela: Float, yNaTela: Float) {
        ultimoXDoFantasma = xNaTela
        ultimoYDoFantasma = yNaTela
        atualizarRolagemAutomaticaDaFaixa()
        avaliarAbaSobOFantasma(xNaTela, yNaTela)
    }

    private var ultimoXDoFantasma = 0f
    private var ultimoYDoFantasma = 0f
    private var rolagemAutomaticaDaFaixa: Runnable? = null

    private fun velocidadeDaRolagemAutomaticaDaFaixa(): Int {
        if (!rolagemAbas.isShown) return 0
        val caixa = android.graphics.Rect()
        if (!rolagemAbas.getGlobalVisibleRect(caixa)) return 0
        val regra = com.chromebrothers.navegador.regras.RolagemAutomaticaDaFaixaDeAbasDoChatComFantasmaNaBorda
        val densidade = activity.resources.displayMetrics.density
        val folga = regra.FOLGA_VERTICAL_DP * densidade
        if (ultimoYDoFantasma < caixa.top - folga || ultimoYDoFantasma > caixa.bottom + folga) return 0
        return regra.pixelsPorQuadro(
            xNaFaixa = ultimoXDoFantasma - caixa.left,
            larguraDaFaixa = caixa.width().toFloat(),
            densidade = densidade,
            podeRolarParaAEsquerda = rolagemAbas.canScrollHorizontally(-1),
            podeRolarParaADireita = rolagemAbas.canScrollHorizontally(1),
        )
    }

    private fun atualizarRolagemAutomaticaDaFaixa() {
        if (velocidadeDaRolagemAutomaticaDaFaixa() == 0) {
            pararRolagemAutomaticaDaFaixa()
            return
        }
        if (rolagemAutomaticaDaFaixa != null) return
        val passo = object : Runnable {
            override fun run() {
                val velocidade = velocidadeDaRolagemAutomaticaDaFaixa()
                if (velocidade != 0) rolagemAbas.scrollBy(velocidade, 0)
                if (abaArrastada != null) {
                    reordenarAbaArrastadaSob(ultimoXDoFantasma, ultimoYDoFantasma)
                } else {
                    avaliarAbaSobOFantasma(ultimoXDoFantasma, ultimoYDoFantasma)
                }
                if (velocidade == 0) {
                    rolagemAutomaticaDaFaixa = null
                    return
                }
                rolagemAbas.postOnAnimation(this)
            }
        }
        rolagemAutomaticaDaFaixa = passo
        rolagemAbas.postOnAnimation(passo)
    }

    private fun pararRolagemAutomaticaDaFaixa() {
        rolagemAutomaticaDaFaixa?.let { rolagemAbas.removeCallbacks(it) }
        rolagemAutomaticaDaFaixa = null
    }

    private fun avaliarAbaSobOFantasma(xNaTela: Float, yNaTela: Float) {
        val id = idDaAbaSobPonto(xNaTela, yNaTela)?.takeIf {
            it != estado.idAtiva &&
                it in AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.idsNavegaveis(estado)
        }
        if (id == idPairadoPeloFantasma) return
        cancelarTrocaPorPairarFantasma()
        if (id == null) return
        idPairadoPeloFantasma = id
        val tarefa = Runnable {
            trocaPorPairarFantasma = null
            idPairadoPeloFantasma = null
            ativar(id)
        }
        trocaPorPairarFantasma = tarefa
        faixaAbas.postDelayed(
            tarefa,
            com.chromebrothers.navegador.regras.TrocaDeAbaPorPairarFantasmaDoArrasteDoNavegador.ATRASO_MS,
        )
    }

    fun pararPairarFantasma() {
        pararRolagemAutomaticaDaFaixa()
        cancelarTrocaPorPairarFantasma()
    }

    private fun cancelarTrocaPorPairarFantasma() {
        trocaPorPairarFantasma?.let { faixaAbas.removeCallbacks(it) }
        trocaPorPairarFantasma = null
        idPairadoPeloFantasma = null
    }

    /** Ponto sobre a aba "Chat" da faixa ou sobre o corpo do chat aberto (soltar ali anexa a imagem). */
    fun pontoSobreOChat(xNaTela: Float, yNaTela: Float): Boolean {
        if (idDaAbaSobPonto(xNaTela, yNaTela) == AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_CHAT) {
            return true
        }
        return corpoChat.isShown && contemPonto(corpoChat, xNaTela, yNaTela)
    }

    private fun idDaAbaSobPonto(xNaTela: Float, yNaTela: Float): String? {
        if (!rolagemAbas.isShown) return null
        if (!contemPonto(rolagemAbas, xNaTela, yNaTela)) return null
        for (i in 0 until faixaAbas.childCount) {
            val chip = faixaAbas.getChildAt(i) ?: continue
            if (contemPonto(chip, xNaTela, yNaTela)) return chip.tag as? String
        }
        return null
    }

    private fun contemPonto(vista: View, xNaTela: Float, yNaTela: Float): Boolean {
        val caixa = android.graphics.Rect()
        if (!vista.getGlobalVisibleRect(caixa)) return false
        return caixa.contains(xNaTela.toInt(), yNaTela.toInt())
    }

    private fun rolarAteAAbaAtiva() {
        rolagemAbas.post {
            for (i in 0 until faixaAbas.childCount) {
                val chip = faixaAbas.getChildAt(i) ?: continue
                if (chip.tag != estado.idAtiva) continue
                val esquerda = chip.left - rolagemAbas.scrollX
                val direita = chip.right - rolagemAbas.scrollX
                when {
                    esquerda < 0 -> rolagemAbas.smoothScrollTo(chip.left, 0)
                    direita > rolagemAbas.width -> rolagemAbas.smoothScrollTo(chip.right - rolagemAbas.width, 0)
                }
                return@post
            }
        }
    }

    fun ligarCampoDeTexto(painel: PainelDoCampoDeTextoDoNavegador) {
        painel.ligarAncorarNaGaveta { ancorarCampoDeTexto(painel) }
        painel.ligarPedirFocoNaAbaAncorada {
            abrirGaveta()
            ativar(PainelDoCampoDeTextoDoNavegador.ID_ABA_ANCORADA)
        }
    }

    private fun ancorarCampoDeTexto(painel: PainelDoCampoDeTextoDoNavegador) {
        val resultado = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ancorar(
            estado = estado,
            id = PainelDoCampoDeTextoDoNavegador.ID_ABA_ANCORADA,
            titulo = painel.tituloParaAbaNaGaveta(),
        )
        when (resultado) {
            is AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Recusado -> {
                Toast.makeText(activity, resultado.mensagem, Toast.LENGTH_LONG).show()
                return
            }
            is AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok -> {
                val erro = painel.ancorarConteudoNoHostDaGaveta(hostComponentes)
                if (erro.isNotEmpty()) {
                    Toast.makeText(activity, erro, Toast.LENGTH_LONG).show()
                    return
                }
                estado = resultado.estado
                campoDeTextoAncorado = painel
                abrirGaveta()
                redesenharAbas()
                aplicarPainelAtivo()
            }
        }
    }

    fun aoMostrarGravador(mostrar: Boolean) {
        estado = if (mostrar) {
            AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.abrirGravador(estado)
        } else {
            AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.fecharGravador(estado)
        }
        redesenharAbas()
        aplicarPainelAtivo()
    }

    fun reaplicarPainelAtivo() {
        aplicarPainelAtivo()
    }

    fun ancorarJanela(janela: PainelDaColecaoFlutuanteDoChatDoNavegador, ativar: Boolean = true) {
        ancorar(janela, ativar)
    }

    /** Componente que já está no chat: abre a gaveta e traz a aba dele para a frente. */
    fun focarJanela(janela: PainelDaColecaoFlutuanteDoChatDoNavegador) {
        if (!janelasPorId.containsKey(janela.idDaJanelaFlutuante)) return
        abrirGaveta()
        ativar(janela.idDaJanelaFlutuante)
    }

    fun tirarDoChatEFlutuar(janela: PainelDaColecaoFlutuanteDoChatDoNavegador) {
        desancorar(janela.idDaJanelaFlutuante)
    }

    private fun fecharAba(id: String) {
        if (id == PainelDoCampoDeTextoDoNavegador.ID_ABA_ANCORADA) {
            val painel = campoDeTextoAncorado ?: return
            desancorarCampoDeTexto()
            painel.fecharPeloUsuario()
            return
        }
        val janela = janelasPorId[id] ?: return
        janela.fecharPeloUsuario { desancorar(id, mostrarDepois = false) }
    }

    private fun ancorar(janela: PainelDaColecaoFlutuanteDoChatDoNavegador, ativar: Boolean = true) {
        val resultado = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ancorar(
            estado = estado,
            id = janela.idDaJanelaFlutuante,
            titulo = janela.tituloParaAbaNaGaveta(),
            ativar = ativar,
        )
        when (resultado) {
            is AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Recusado -> {
                Toast.makeText(activity, resultado.mensagem, Toast.LENGTH_LONG).show()
                return
            }
            is AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok -> {
                val erro = janela.ancorarConteudoNoHostDaGaveta(hostComponentes)
                if (erro.isNotEmpty()) {
                    Toast.makeText(activity, erro, Toast.LENGTH_LONG).show()
                    return
                }
                estado = resultado.estado
                janelasPorId[janela.idDaJanelaFlutuante] = janela
                abrirGaveta()
                redesenharAbas()
                aplicarPainelAtivo()
            }
        }
    }

    private fun desancorar(id: String, mostrarDepois: Boolean = true) {
        if (id == PainelDoCampoDeTextoDoNavegador.ID_ABA_ANCORADA) {
            desancorarCampoDeTexto()
            return
        }
        val janela = janelasPorId[id] ?: return
        val resultado = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.desancorar(estado, id)
        if (resultado !is AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok) {
            Toast.makeText(
                activity,
                (resultado as AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Recusado).mensagem,
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        val erro = janela.desancorarConteudoParaFlutuante(mostrarDepois)
        if (erro.isNotEmpty()) {
            Toast.makeText(activity, erro, Toast.LENGTH_LONG).show()
            return
        }
        estado = resultado.estado
        janelasPorId.remove(id)
        redesenharAbas()
        aplicarPainelAtivo()
    }

    private fun desancorarCampoDeTexto() {
        val painel = campoDeTextoAncorado ?: return
        val resultado = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.desancorar(
            estado,
            PainelDoCampoDeTextoDoNavegador.ID_ABA_ANCORADA,
        )
        if (resultado !is AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok) {
            Toast.makeText(
                activity,
                (resultado as AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Recusado).mensagem,
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        val erro = painel.desancorarConteudoParaFlutuante()
        if (erro.isNotEmpty()) {
            Toast.makeText(activity, erro, Toast.LENGTH_LONG).show()
            return
        }
        estado = resultado.estado
        campoDeTextoAncorado = null
        redesenharAbas()
        aplicarPainelAtivo()
    }

    private fun ativar(id: String) {
        if (AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.precisaAbrirSessaoDoGravador(estado, id)) {
            aoPedirAbrirGravadorNaGaveta()
            return
        }
        val resultado = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ativar(estado, id)
        if (resultado !is AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.Resultado.Ok) return
        estado = resultado.estado
        redesenharAbas()
        aplicarPainelAtivo()
        rolarAteAAbaAtiva()
    }

    private fun aplicarPainelAtivo() {
        val ativa = estado.idAtiva
        val chat = ativa == AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_CHAT
        val gravador = ativa == AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_GRAVADOR &&
            estado.gravadorAberto
        // Aba Gravador focada sem sessão: cai no chat visualmente (abrir é pedido no toque).
        val mostrarChat = chat ||
            (ativa == AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_GRAVADOR &&
                !estado.gravadorAberto)
        corpoChat.visibility = if (mostrarChat) View.VISIBLE else View.GONE
        hostGravador.visibility = if (gravador) View.VISIBLE else View.GONE
        hostComponentes.visibility =
            if (!mostrarChat && !gravador) View.VISIBLE else View.GONE
        barraContextoChat.visibility = if (mostrarChat) View.VISIBLE else View.GONE
        janelasPorId.forEach { (id, janela) ->
            janela.mostrarConteudoAncoradoNoHost(id == ativa)
        }
        campoDeTextoAncorado?.mostrarConteudoAncoradoNoHost(
            ativa == PainelDoCampoDeTextoDoNavegador.ID_ABA_ANCORADA,
        )
        if (hostComponentes.visibility == View.VISIBLE) {
            hostComponentes.post { gestor.notificarHostRedimensionadoNasJanelas() }
        }
    }

    private fun redesenharAbas() {
        faixaAbas.removeAllViews()
        val tituloGravador = activity.getString(R.string.aba_gravador_da_gaveta_do_chat)
        val abas = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.abasVisiveis(
            estado,
            tituloGravador = tituloGravador,
        )
        rolagemAbas.visibility = View.VISIBLE
        abas.forEach { aba ->
            val componente = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ehAbaDeComponente(aba.id)
            val titulo = when (aba.id) {
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_CHAT ->
                    activity.getString(R.string.aba_chat_da_gaveta_do_chat)
                AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_GRAVADOR ->
                    activity.getString(R.string.aba_gravador_da_gaveta_do_chat)
                else -> aba.titulo
            }
            faixaAbas.addView(
                chipAba(
                    id = aba.id,
                    titulo = titulo,
                    componente = componente,
                    gravador = aba.id == AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.ID_ABA_GRAVADOR,
                ),
            )
        }
    }

    private fun chipAba(
        id: String,
        titulo: String,
        componente: Boolean,
        gravador: Boolean,
    ): TextView {
        val densidade = activity.resources.displayMetrics.density
        val ativa = estado.idAtiva == id
        return TextView(activity).apply {
            tag = id
            text = titulo
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            gravity = Gravity.CENTER
            textSize = 13f
            setPadding(
                (12 * densidade).toInt(),
                (6 * densidade).toInt(),
                (12 * densidade).toInt(),
                (6 * densidade).toInt(),
            )
            val margem = (4 * densidade).toInt()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { setMargins(0, 0, margem, 0) }
            setTextColor(if (ativa) Color.parseColor("#F4F7FB") else Color.parseColor("#8A9099"))
            background = GradientDrawable().apply {
                cornerRadius = 16f * densidade
                setColor(
                    if (ativa) Color.parseColor("#2A303C") else Color.parseColor("#22262E"),
                )
            }
            if (abaArrastada == id) alpha = 0.35f
            when {
                componente -> prepararToqueDaAbaDeComponente(this, id)
                gravador -> {
                    setOnClickListener { ativar(id) }
                    setOnLongClickListener {
                        aoPedirMenuDeModoDoGravador()
                        true
                    }
                }
                else -> setOnClickListener { ativar(id) }
            }
        }
    }

    private fun mostrarMenuDaAba(chip: View, id: String) {
        PopupMenu(activity, chip).apply {
            menu.add(0, 1, 0, R.string.desvincular_componente_da_gaveta_do_chat)
            menu.add(0, 2, 1, R.string.fechar_componente_da_gaveta_do_chat)
            setOnMenuItemClickListener { item ->
                if (item.itemId == 2) fecharAba(id) else desancorar(id)
                true
            }
            show()
        }
    }

    /** Toque ativa; toque longo parado abre o menu; toque longo e mover arrasta a aba (fora da gaveta = flutuar). */
    private fun prepararToqueDaAbaDeComponente(chip: TextView, id: String) {
        val folga = android.view.ViewConfiguration.get(activity).scaledTouchSlop
        val tempoDoLongo = android.view.ViewConfiguration.getLongPressTimeout().toLong()
        var xInicio = 0f
        var yInicio = 0f
        var longo = false
        var moveu = false
        var arrastando = false
        val marcarLongo = Runnable {
            longo = true
            chip.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            chip.parent?.requestDisallowInterceptTouchEvent(true)
            chip.animate().scaleX(1.06f).scaleY(1.06f).setDuration(120L).start()
        }
        chip.setOnTouchListener { _, evento ->
            when (evento.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    xInicio = evento.rawX
                    yInicio = evento.rawY
                    longo = false
                    moveu = false
                    arrastando = false
                    chip.postDelayed(marcarLongo, tempoDoLongo)
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    val dx = evento.rawX - xInicio
                    val dy = evento.rawY - yInicio
                    if (!moveu && dx * dx + dy * dy > folga * folga) {
                        moveu = true
                        if (!longo) chip.removeCallbacks(marcarLongo)
                    }
                    if (longo && moveu && !arrastando) {
                        arrastando = true
                        iniciarArrasteDaAba(chip, id)
                    }
                    if (arrastando) moverArrasteDaAba(evento.rawX, evento.rawY)
                }
                android.view.MotionEvent.ACTION_UP -> {
                    chip.removeCallbacks(marcarLongo)
                    chip.animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
                    when {
                        arrastando -> {
                            val x = evento.rawX
                            val y = evento.rawY
                            rolagemAbas.post { soltarArrasteDaAba(x, y) }
                        }
                        longo -> mostrarMenuDaAba(chip, id)
                        !moveu -> ativar(id)
                    }
                }
                android.view.MotionEvent.ACTION_CANCEL -> {
                    chip.removeCallbacks(marcarLongo)
                    chip.animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
                    if (arrastando) rolagemAbas.post { if (abaArrastada == id) encerrarArrasteDaAba() }
                }
            }
            true
        }
    }

    private var abaArrastada: String? = null
    private var fantasmaDaAba: TextView? = null
    private val raizDaTela: FrameLayout by lazy { activity.findViewById(R.id.raiz_do_navegador) }
    private val gavetaDoChat: View by lazy { activity.findViewById(R.id.gaveta_do_chat_da_openrouter) }

    private fun iniciarArrasteDaAba(chip: TextView, id: String) {
        abaArrastada = id
        chip.alpha = 0.35f
        val densidade = activity.resources.displayMetrics.density
        val fantasma = TextView(activity).apply {
            text = chip.text
            textSize = 13f
            maxLines = 1
            setTextColor(Color.parseColor("#F4F7FB"))
            setPadding((14 * densidade).toInt(), (8 * densidade).toInt(), (14 * densidade).toInt(), (8 * densidade).toInt())
            elevation = 24f * densidade
            background = fundoDoFantasmaDaAba(fora = false)
        }
        raizDaTela.addView(
            fantasma,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
        )
        fantasmaDaAba = fantasma
    }

    private fun fundoDoFantasmaDaAba(fora: Boolean): GradientDrawable {
        val densidade = activity.resources.displayMetrics.density
        return GradientDrawable().apply {
            cornerRadius = 16f * densidade
            setColor(Color.parseColor(if (fora) "#1E3A2A" else "#2A303C"))
            setStroke((2 * densidade).toInt(), Color.parseColor(if (fora) "#81C995" else "#8AB4F8"))
        }
    }

    private fun moverArrasteDaAba(xNaTela: Float, yNaTela: Float) {
        val fantasma = fantasmaDaAba ?: return
        val origem = IntArray(2)
        raizDaTela.getLocationOnScreen(origem)
        val densidade = activity.resources.displayMetrics.density
        val largura = fantasma.width.takeIf { it > 0 } ?: (120 * densidade).toInt()
        val altura = fantasma.height.takeIf { it > 0 } ?: (32 * densidade).toInt()
        fantasma.x = xNaTela - origem[0] - largura / 2f
        fantasma.y = yNaTela - origem[1] - altura - 12f * densidade
        val fora = !contemPonto(gavetaDoChat, xNaTela, yNaTela)
        fantasma.background = fundoDoFantasmaDaAba(fora)
        val titulo = estado.componentes.firstOrNull { it.id == abaArrastada }?.titulo.orEmpty()
        fantasma.text = if (fora) activity.getString(R.string.soltar_aba_do_chat_para_flutuar, titulo) else titulo
        ultimoXDoFantasma = xNaTela
        ultimoYDoFantasma = yNaTela
        atualizarRolagemAutomaticaDaFaixa()
        if (sobreAFaixa(xNaTela, yNaTela)) reordenarAbaArrastadaSob(xNaTela, yNaTela)
    }

    /** Move os chips vizinhos (nunca o arrastado, que perderia o toque se saísse da hierarquia). */
    private fun reordenarAbaArrastadaSob(xNaTela: Float, yNaTela: Float) {
        val id = abaArrastada ?: return
        val idsDosComponentes = estado.componentes.map { it.id }
        val alvo = (0 until faixaAbas.childCount)
            .map { faixaAbas.getChildAt(it) }
            .firstOrNull { it.tag != id && it.tag in idsDosComponentes && contemPontoNaHorizontal(it, xNaTela) }
            ?.tag as? String ?: return
        val novoIndice = idsDosComponentes.indexOf(alvo)
        val novoEstado = AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.moverComponente(estado, id, novoIndice)
        if (novoEstado == estado) return
        estado = novoEstado
        val chipArrastado = (0 until faixaAbas.childCount).map { faixaAbas.getChildAt(it) }.firstOrNull { it.tag == id } ?: return
        val primeiroComponente = (0 until faixaAbas.childCount).first { faixaAbas.getChildAt(it).tag in idsDosComponentes }
        val desejado = primeiroComponente + novoIndice
        var atual = faixaAbas.indexOfChild(chipArrastado)
        while (atual != desejado) {
            if (desejado > atual) {
                val vizinho = faixaAbas.getChildAt(atual + 1)
                faixaAbas.removeView(vizinho)
                faixaAbas.addView(vizinho, atual)
                atual++
            } else {
                val vizinho = faixaAbas.getChildAt(atual - 1)
                faixaAbas.removeView(vizinho)
                faixaAbas.addView(vizinho, atual)
                atual--
            }
        }
    }

    private fun contemPontoNaHorizontal(vista: View, xNaTela: Float): Boolean {
        val origem = IntArray(2)
        vista.getLocationOnScreen(origem)
        return xNaTela >= origem[0] && xNaTela <= origem[0] + vista.width
    }

    private fun sobreAFaixa(xNaTela: Float, yNaTela: Float): Boolean {
        if (!rolagemAbas.isShown) return false
        val caixa = android.graphics.Rect()
        if (!rolagemAbas.getGlobalVisibleRect(caixa)) return false
        val folga = com.chromebrothers.navegador.regras.RolagemAutomaticaDaFaixaDeAbasDoChatComFantasmaNaBorda
            .FOLGA_VERTICAL_DP * activity.resources.displayMetrics.density
        return xNaTela >= caixa.left && xNaTela <= caixa.right &&
            yNaTela >= caixa.top - folga && yNaTela <= caixa.bottom + folga
    }

    private fun soltarArrasteDaAba(xNaTela: Float, yNaTela: Float) {
        val id = abaArrastada ?: return
        val destino = com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante
            .destinoDaAbaArrastada(
                dentroDaGaveta = contemPonto(gavetaDoChat, xNaTela, yNaTela),
                sobreAFaixa = sobreAFaixa(xNaTela, yNaTela),
            )
        encerrarArrasteDaAba()
        if (destino != com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante
                .DestinoDaAbaArrastada.DESVINCULAR
        ) {
            return
        }
        if (id == PainelDoCampoDeTextoDoNavegador.ID_ABA_ANCORADA) {
            val painel = campoDeTextoAncorado ?: return
            desancorarCampoDeTexto()
            if (campoDeTextoAncorado == null) painel.levarJanelaAoPontoDaTela(xNaTela, yNaTela)
            return
        }
        val janela = janelasPorId[id] ?: return
        desancorar(id)
        if (!janelasPorId.containsKey(id)) janela.levarJanelaAoPontoDaTela(xNaTela, yNaTela)
    }

    private fun encerrarArrasteDaAba() {
        pararRolagemAutomaticaDaFaixa()
        fantasmaDaAba?.let { raizDaTela.removeView(it) }
        fantasmaDaAba = null
        abaArrastada = null
        redesenharAbas()
    }

    private var janelaSobreAFaixa =
        com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.JanelaSobreAFaixa()
    private var repintarAlvoArmado: Runnable? = null
    private var marcaDeFixarNoChat: TextView? = null

    /** Janela flutuante arrastada pela barra: responde se está sobre a faixa (a janela fica translúcida). */
    fun arrastandoJanelaFlutuante(xNaTela: Float, yNaTela: Float, aceita: Boolean): Boolean {
        val regra = com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante
        val sobre = sobreAFaixa(xNaTela, yNaTela)
        janelaSobreAFaixa = regra.atualizarJanelaSobreAFaixa(
            janelaSobreAFaixa,
            sobre,
            aceita,
            android.os.SystemClock.uptimeMillis(),
        )
        if (janelaSobreAFaixa.desdeMs == 0L) {
            limparAlvoDaFaixa()
            return false
        }
        pintarAlvoDaFaixa()
        if (repintarAlvoArmado == null && !regra.estaArmada(janelaSobreAFaixa, android.os.SystemClock.uptimeMillis())) {
            val tarefa = Runnable {
                repintarAlvoArmado = null
                pintarAlvoDaFaixa()
            }
            repintarAlvoArmado = tarefa
            rolagemAbas.postDelayed(tarefa, regra.ATRASO_MS)
        }
        return true
    }

    fun soltouJanelaFlutuante(xNaTela: Float, yNaTela: Float, aceita: Boolean): Boolean {
        val regra = com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante
        val fixa = regra.soltarFixa(sobreAFaixa(xNaTela, yNaTela), aceita)
        limparAlvoDaFaixa()
        return fixa
    }

    fun janelaFlutuanteArmadaSobreAFaixa(xNaTela: Float, yNaTela: Float, aceita: Boolean): Boolean {
        val regra = com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante
        return aceita && sobreAFaixa(xNaTela, yNaTela) &&
            regra.estaArmada(janelaSobreAFaixa, android.os.SystemClock.uptimeMillis())
    }

    fun cancelouArrasteDaJanelaFlutuante() {
        limparAlvoDaFaixa()
    }

    private fun pintarAlvoDaFaixa() {
        // Sobre a faixa, soltar já fixa: o alvo fica verde e com a dica desde o primeiro instante.
        val armada = janelaSobreAFaixa.desdeMs > 0L
        val densidade = activity.resources.displayMetrics.density
        rolagemAbas.background = GradientDrawable().apply {
            cornerRadius = 12f * densidade
            setColor(Color.parseColor(if (armada) "#1A2E22" else "#1A2230"))
            if (armada) {
                setStroke((2 * densidade).toInt(), Color.parseColor("#81C995"))
            } else {
                setStroke((2 * densidade).toInt(), Color.parseColor("#8AB4F8"), 8f * densidade, 6f * densidade)
            }
        }
        if (armada && marcaDeFixarNoChat == null) {
            val marca = TextView(activity).apply {
                text = activity.getString(R.string.solte_para_fixar_no_chat)
                textSize = 13f
                maxLines = 1
                setTextColor(Color.parseColor("#81C995"))
                setPadding((12 * densidade).toInt(), (6 * densidade).toInt(), (12 * densidade).toInt(), (6 * densidade).toInt())
                background = GradientDrawable().apply {
                    cornerRadius = 16f * densidade
                    setColor(Color.parseColor("#22382A"))
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
            faixaAbas.addView(marca)
            marcaDeFixarNoChat = marca
            rolagemAbas.post { rolagemAbas.fullScroll(View.FOCUS_RIGHT) }
        }
    }

    private fun limparAlvoDaFaixa() {
        repintarAlvoArmado?.let { rolagemAbas.removeCallbacks(it) }
        repintarAlvoArmado = null
        janelaSobreAFaixa =
            com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.JanelaSobreAFaixa()
        rolagemAbas.background = null
        marcaDeFixarNoChat?.let { faixaAbas.removeView(it) }
        marcaDeFixarNoChat = null
    }
}

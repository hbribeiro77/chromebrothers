package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.LruCache
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chromebrothers.navegador.regras.AtualizacaoAoVivoDaGradeDoVisualizadorDeMidiaAoChegarArquivoNovo
import com.chromebrothers.navegador.regras.HtmlDoComponenteDoVisualizadorDeMidiaNaColecao
import com.chromebrothers.navegador.regras.ItemDaPastaEscolhidaPeloComponente
import com.chromebrothers.navegador.regras.MidiaGeradaViaMcpListadaNoVisualizadorDeMidiaDaColecao
import com.chromebrothers.navegador.regras.RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao
import com.chromebrothers.navegador.regras.RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao.Filtro
import com.chromebrothers.navegador.regras.RegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat
import java.io.File
import java.util.concurrent.Executors

/**
 * Grade nativa do Visualizador de mídia (substitui a grade HTML, que piscava preto ao rolar).
 * Miniaturas ficam em memória ([CacheDeMiniaturasEmMemoria]) e são todas pré-carregadas
 * assim que a listagem chega — rolar não espera disco nem decodificação.
 */
class GradeNativaDoVisualizadorDeMidiaNaColecaoDoNavegador(
    private val activity: AppCompatActivity,
    private val host: FrameLayout,
    private val gerador: GeradorDeMiniaturasDaGradeDoVisualizadorDeMidiaComCacheEmDisco,
    private val listar: () -> String?,
    private val excluir: (String) -> String?,
    private val abrirVisor: (uris: List<String>, indice: Int) -> Unit,
    private val abrirVideos: (uris: List<String>, indice: Int) -> Unit,
    private val mostrarMenuDoVideo: (uri: String, reproduzirAqui: () -> Unit) -> Unit,
    private val iniciarArraste: (uri: String, xNaTela: Float, yNaTela: Float, miniatura: Bitmap?) -> Unit,
    private val aoTocar: () -> Unit,
) {
    private val regras = RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao
    private val densidade = activity.resources.displayMetrics.density
    private val principal = Handler(Looper.getMainLooper())
    private val preferencias = activity.getSharedPreferences("grade_nativa_do_visualizador_de_midia", Context.MODE_PRIVATE)

    private var todos: List<ItemDaPastaEscolhidaPeloComponente> = emptyList()
    private var visiveis: List<ItemDaPastaEscolhidaPeloComponente> = emptyList()
    private var cortados = 0
    private var totalEncontrado = 0
    private var filtro = Filtro.daChave(preferencias.getString("filtro", "tudo"))
    private var maiores = preferencias.getBoolean("maiores", false)
    private var modoSelecao = false
    private val marcados = linkedSetOf<String>()
    private var uriEmFoco: String? = null
    private var listagemPedida = false

    private val raiz: LinearLayout
    private val status: TextView
    private val recycler: RecyclerView
    private val gerenciador: GridLayoutManager
    private val adaptador = Adaptador()
    private lateinit var botaoSelecionar: TextView
    private lateinit var botaoMaiores: TextView
    private lateinit var botaoExcluir: TextView
    private lateinit var contagem: TextView
    private val botoesDeFiltro = mutableMapOf<Filtro, TextView>()

    init {
        raiz = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1D24"))
            isClickable = true
        }
        raiz.addView(montarBarra())
        status = TextView(activity).apply {
            setTextColor(Color.parseColor("#8B91A1"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(dp(12), dp(6), dp(12), dp(2))
            visibility = View.GONE
        }
        raiz.addView(status)
        gerenciador = GridLayoutManager(activity, 3)
        recycler = RecyclerView(activity).apply {
            layoutManager = gerenciador
            adapter = adaptador
            setHasFixedSize(true)
            itemAnimator = null
            setItemViewCacheSize(40)
            clipToPadding = false
            setPadding(dp(8), dp(6), dp(8), dp(20))
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        recycler.addOnLayoutChangeListener { _, l, _, r, _, ol, _, or, _ ->
            if (r - l != or - ol) ajustarColunas()
        }
        raiz.addView(recycler, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        atualizarBarra()
        CacheDeMiniaturasEmMemoria.ouvintes += { chave -> notificarUri(chave, CARGA_FOTO) }
    }

    fun estaVisivel(): Boolean = raiz.parent != null && raiz.visibility == View.VISIBLE

    fun mostrar() {
        if (raiz.parent == null) {
            host.addView(
                raiz,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
            )
        }
        raiz.visibility = View.VISIBLE
        if (!listagemPedida) recarregar()
    }

    fun esconder() {
        (raiz.parent as? ViewGroup)?.removeView(raiz)
    }

    fun recarregar() {
        listagemPedida = true
        Thread {
            val json = runCatching { listar() }.getOrNull() ?: return@Thread
            principal.post { receberJson(json) }
        }.start()
    }

    fun receberJson(json: String) {
        listagemPedida = true
        val obj = runCatching { org.json.JSONObject(json) }.getOrNull() ?: return
        if (!obj.optBoolean("ok", false)) {
            mostrarStatus(obj.optString("motivo").ifBlank { "Não deu para listar a mídia." })
            return
        }
        val arr = obj.optJSONArray("itens") ?: org.json.JSONArray()
        val novos = ArrayList<ItemDaPastaEscolhidaPeloComponente>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val uri = o.optString("uri").trim()
            if (uri.isEmpty() || o.optBoolean("pasta", false)) continue
            novos += ItemDaPastaEscolhidaPeloComponente(
                nome = o.optString("nome"),
                uri = uri,
                mime = o.optString("mime"),
                pasta = false,
                tamanho = o.optLong("tamanho", 0L),
            )
        }
        cortados = obj.optInt("cortados", 0)
        totalEncontrado = obj.optInt("totalEncontrado", novos.size + cortados)
        val jaTinhaLista = todos.isNotEmpty()
        todos = novos
        marcados.retainAll(novos.map { it.uri }.toSet())
        aplicarFiltro(diferencial = jaTinhaLista)
        preCarregarTodas()
    }

    fun destacar(uriTexto: String) {
        val alvo = semMiniatura(uriTexto)
        if (alvo.isEmpty()) return
        if (alvo != uriEmFoco) {
            val anterior = uriEmFoco
            uriEmFoco = alvo
            notificarUri(anterior)
            notificarUri(alvo)
        }
        val pos = visiveis.indexOfFirst { semMiniatura(it.uri) == alvo }
        val primeira = gerenciador.findFirstCompletelyVisibleItemPosition()
        val ultima = gerenciador.findLastCompletelyVisibleItemPosition()
        if (RegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat
                .deveRolarGradeAteACarta(pos, primeira, ultima)
        ) {
            recycler.smoothScrollToPosition(pos)
        }
    }

    fun limparDestaque() {
        val anterior = uriEmFoco ?: return
        uriEmFoco = null
        notificarUri(anterior)
    }

    // ---------- barra ----------

    private fun montarBarra(): View {
        val linha = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        botaoSelecionar = botao("Selecionar") {
            modoSelecao = !modoSelecao
            if (!modoSelecao) marcados.clear()
            atualizarBarra()
            adaptador.notifyItemRangeChanged(0, visiveis.size, CARGA_ESTADO)
        }
        linha.addView(botaoSelecionar)
        val grupo = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            background = GradientDrawable().apply {
                cornerRadius = dpf(10)
                setColor(Color.parseColor("#12141A"))
                setStroke(dp(1), Color.parseColor("#2E323D"))
            }
            setPadding(dp(2), dp(2), dp(2), dp(2))
        }
        listOf(
            Filtro.TUDO to "Tudo",
            Filtro.IMAGENS to "Imagens",
            Filtro.VIDEOS to "Vídeos",
            Filtro.MCP to "MCP",
        ).forEach { (f, rotulo) ->
            val b = TextView(activity).apply {
                text = rotulo
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setPadding(dp(10), dp(6), dp(10), dp(6))
                setOnClickListener {
                    filtro = f
                    preferencias.edit().putString("filtro", f.chave).apply()
                    aplicarFiltro()
                }
            }
            botoesDeFiltro[f] = b
            grupo.addView(b)
        }
        linha.addView(grupo, margemInicio())
        botaoMaiores = botao("Maiores") {
            maiores = !maiores
            preferencias.edit().putBoolean("maiores", maiores).apply()
            atualizarBarra()
            ajustarColunas()
        }
        linha.addView(botaoMaiores, margemInicio())
        botaoExcluir = botao("Excluir") { excluirMarcados() }
        linha.addView(botaoExcluir, margemInicio())
        contagem = TextView(activity).apply {
            setTextColor(Color.parseColor("#8B91A1"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f)
        }
        linha.addView(contagem, margemInicio())
        return HorizontalScrollView(activity).apply {
            isHorizontalScrollBarEnabled = false
            setBackgroundColor(Color.parseColor("#1B1D24"))
            addView(linha)
        }
    }

    private fun botao(rotulo: String, acao: () -> Unit): TextView = TextView(activity).apply {
        text = rotulo
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
        setPadding(dp(11), dp(7), dp(11), dp(7))
        setOnClickListener { acao() }
    }

    private fun margemInicio() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    ).apply { marginStart = dp(6) }

    private fun fundoDeBotao(cor: String, borda: String) = GradientDrawable().apply {
        cornerRadius = dpf(10)
        setColor(Color.parseColor(cor))
        setStroke(dp(1), Color.parseColor(borda))
    }

    private fun atualizarBarra() {
        botaoSelecionar.text = if (modoSelecao) "Pronto" else "Selecionar"
        if (modoSelecao) {
            botaoSelecionar.background = fundoDeBotao("#F5C518", "#F5C518")
            botaoSelecionar.setTextColor(Color.parseColor("#191A1F"))
        } else {
            botaoSelecionar.background = fundoDeBotao("#22252E", "#2E323D")
            botaoSelecionar.setTextColor(Color.parseColor("#E8EAF0"))
        }
        botaoMaiores.background = if (maiores) fundoDeBotao("#14F5C518", "#3DF5C518") else fundoDeBotao("#22252E", "#2E323D")
        botaoMaiores.setTextColor(Color.parseColor(if (maiores) "#F5C518" else "#E8EAF0"))
        botaoExcluir.visibility = if (modoSelecao) View.VISIBLE else View.GONE
        botaoExcluir.background = fundoDeBotao("#1CFF6B6B", "#3DFF6B6B")
        botaoExcluir.setTextColor(Color.parseColor("#FF8F8F"))
        val n = marcados.size
        contagem.visibility = if (modoSelecao && n > 0) View.VISIBLE else View.GONE
        contagem.text = if (n == 1) "1 selecionado" else "$n selecionados"
        botoesDeFiltro.forEach { (f, b) ->
            val ativo = f == filtro
            b.setTextColor(Color.parseColor(if (ativo) "#E8EAF0" else "#8B91A1"))
            b.background = if (ativo) {
                GradientDrawable().apply { cornerRadius = dpf(7); setColor(Color.parseColor("#22252E")) }
            } else {
                null
            }
        }
    }

    private fun mostrarStatus(texto: String?) {
        if (texto.isNullOrBlank()) {
            status.visibility = View.GONE
        } else {
            status.text = texto
            status.visibility = View.VISIBLE
        }
    }

    private fun aplicarFiltro(diferencial: Boolean = false) {
        val antigos = visiveis
        val novos = regras.filtrar(todos, filtro)
        visiveis = novos
        if (diferencial) {
            val primeiraVisivel = gerenciador.findFirstVisibleItemPosition()
            val urisAntigas = antigos.map { it.uri }
            val urisNovas = novos.map { it.uri }
            androidx.recyclerview.widget.DiffUtil.calculateDiff(
                object : androidx.recyclerview.widget.DiffUtil.Callback() {
                    override fun getOldListSize(): Int = urisAntigas.size
                    override fun getNewListSize(): Int = urisNovas.size
                    override fun areItemsTheSame(antiga: Int, nova: Int): Boolean =
                        urisAntigas[antiga] == urisNovas[nova]
                    override fun areContentsTheSame(antiga: Int, nova: Int): Boolean =
                        antigos[antiga] == novos[nova]
                },
                false,
            ).dispatchUpdatesTo(adaptador)
            val regraAoVivo = AtualizacaoAoVivoDaGradeDoVisualizadorDeMidiaAoChegarArquivoNovo
            if (regraAoVivo.deveVoltarAoTopoAposInserir(primeiraVisivel, regraAoVivo.inseriuNoTopo(urisAntigas, urisNovas))) {
                recycler.scrollToPosition(0)
            }
        } else {
            adaptador.notifyDataSetChanged()
        }
        atualizarBarra()
        mostrarStatus(
            when {
                todos.isEmpty() -> "Nenhuma mídia em ChromeBrothers."
                cortados > 0 -> "Mostrando ${todos.size} de $totalEncontrado (mais recentes)."
                else -> null
            },
        )
    }

    private fun ajustarColunas() {
        val largura = recycler.width - recycler.paddingLeft - recycler.paddingRight
        if (largura <= 0) return
        val colunas = regras.colunas(largura, densidade, maiores)
        if (gerenciador.spanCount != colunas) gerenciador.spanCount = colunas
    }

    private fun excluirMarcados() {
        if (marcados.isEmpty()) return
        val uris = marcados.toList()
        val bytes = todos.filter { it.uri in marcados }.sumOf { it.tamanho.coerceAtLeast(0L) }
        CaixaDeConfirmacaoDoNavegador.mostrar(
            activity = activity,
            titulo = CaixaDeConfirmacaoDoNavegador.tituloDeExcluir(uris.size),
            mensagem = CaixaDeConfirmacaoDoNavegador.mensagemDeExcluir(
                uris.size,
                bytes.takeIf { it > 0 }?.let { com.chromebrothers.navegador.regras.ArmazenamentoDoAplicativo.formatarBytes(it) },
            ),
            textoConfirmar = "Excluir",
        ) { excluirAgora(uris) }
    }

    private fun excluirAgora(uris: List<String>) {
        val json = org.json.JSONArray().apply { uris.forEach { put(it) } }.toString()
        Thread {
            val resposta = runCatching { excluir(json) }.getOrNull().orEmpty()
            val ok = runCatching { org.json.JSONObject(resposta).optBoolean("ok", false) }.getOrDefault(false)
            if (!ok) return@Thread
            principal.post {
                val removidos = uris.toSet()
                todos = todos.filterNot { it.uri in removidos }
                marcados.clear()
                aplicarFiltro()
            }
        }.start()
    }

    // ---------- miniaturas ----------

    private fun preCarregarTodas() {
        val lado = HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.LADO_DA_MINIATURA_DA_GRADE
        todos.forEach { item -> carregarSePreciso(item, lado) }
    }

    private fun carregarSePreciso(item: ItemDaPastaEscolhidaPeloComponente, lado: Int) {
        if (CacheDeMiniaturasEmMemoria.obter(item.uri) != null) return
        filaDeCarga.execute { aquecerNaMemoria(gerador, item, lado) }
    }

    private fun notificarUri(uri: String?, carga: Any = CARGA_ESTADO) {
        if (uri == null) return
        val pos = visiveis.indexOfFirst { it.uri == uri }
        if (pos >= 0) adaptador.notifyItemChanged(pos, carga)
    }

    private fun semMiniatura(uri: String): String =
        com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
            .uriSemParametroDeMiniatura(uri.trim())

    // ---------- toque ----------

    private fun tocar(item: ItemDaPastaEscolhidaPeloComponente) {
        if (modoSelecao) {
            if (!marcados.remove(item.uri)) marcados.add(item.uri)
            notificarUri(item.uri)
            atualizarBarra()
            return
        }
        if (regras.ehVideo(item)) {
            val (lista, indice) = regras.videosParaOPlayer(visiveis, item.uri)
            abrirVideos(lista, indice)
            return
        }
        val (lista, indice) = regras.imagensParaOVisor(visiveis, item.uri)
        abrirVisor(lista, indice)
    }

    private fun toqueLongo(item: ItemDaPastaEscolhidaPeloComponente, carta: View, xTela: Float, yTela: Float) {
        destacar(item.uri)
        carta.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        recycler.stopScroll()
        if (regras.ehVideo(item)) {
            mostrarMenuDoVideo(item.uri) { tocar(item) }
        } else {
            iniciarArraste(item.uri, xTela, yTela, CacheDeMiniaturasEmMemoria.obter(item.uri))
        }
        // O fantasma passa a consumir o gesto na Activity; limpa o toque pendente da grade.
        val agora = SystemClock.uptimeMillis()
        val cancelar = MotionEvent.obtain(agora, agora, MotionEvent.ACTION_CANCEL, 0f, 0f, 0)
        recycler.dispatchTouchEvent(cancelar)
        cancelar.recycle()
    }

    private inner class OuvinteDeToqueDaCarta(private val carta: View) : View.OnTouchListener {
        private val folga = ViewConfiguration.get(activity).scaledTouchSlop.toFloat()
        private var x0 = 0f
        private var y0 = 0f
        private var rawX = 0f
        private var rawY = 0f
        private var moveu = false
        private var longoDisparou = false
        private val disparo = Runnable {
            val item = itemDaCarta() ?: return@Runnable
            longoDisparou = true
            carta.parent?.requestDisallowInterceptTouchEvent(true)
            toqueLongo(item, carta, rawX, rawY)
        }

        private fun itemDaCarta(): ItemDaPastaEscolhidaPeloComponente? {
            val pos = recycler.getChildAdapterPosition(carta)
            return visiveis.getOrNull(pos)
        }

        override fun onTouch(v: View, ev: MotionEvent): Boolean {
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    aoTocar()
                    x0 = ev.rawX
                    y0 = ev.rawY
                    rawX = ev.rawX
                    rawY = ev.rawY
                    moveu = false
                    longoDisparou = false
                    val item = itemDaCarta()
                    if (!modoSelecao && item != null) {
                        v.postDelayed(disparo, regras.ESPERA_DO_TOQUE_LONGO_MS)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    rawX = ev.rawX
                    rawY = ev.rawY
                    if (!moveu && regras.moveuAlemDaFolga(ev.rawX - x0, ev.rawY - y0, folga)) {
                        moveu = true
                        v.removeCallbacks(disparo)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.removeCallbacks(disparo)
                    if (!moveu && !longoDisparou) itemDaCarta()?.let { tocar(it) }
                }
                MotionEvent.ACTION_CANCEL -> v.removeCallbacks(disparo)
            }
            return true
        }
    }

    // ---------- adaptador ----------

    private inner class Carta(val vista: CartaQuadrada) : RecyclerView.ViewHolder(vista)

    private inner class Adaptador : RecyclerView.Adapter<Carta>() {
        override fun getItemCount(): Int = visiveis.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Carta {
            val vista = CartaQuadrada(activity)
            vista.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply { setMargins(dp(4), dp(4), dp(4), dp(4)) }
            vista.setOnTouchListener(OuvinteDeToqueDaCarta(vista))
            return Carta(vista)
        }

        override fun onBindViewHolder(holder: Carta, position: Int) {
            val item = visiveis[position]
            holder.vista.nome.text = item.nome
            holder.vista.play.visibility = if (regras.ehVideo(item)) View.VISIBLE else View.GONE
            holder.vista.seloMcp.visibility =
                if (MidiaGeradaViaMcpListadaNoVisualizadorDeMidiaDaColecao.ehDoMcp(item.uri)) View.VISIBLE else View.GONE
            aplicarFoto(holder, item)
            aplicarEstado(holder, item)
        }

        override fun onBindViewHolder(holder: Carta, position: Int, payloads: MutableList<Any>) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position)
                return
            }
            val item = visiveis[position]
            if (payloads.contains(CARGA_FOTO)) aplicarFoto(holder, item)
            if (payloads.contains(CARGA_ESTADO)) aplicarEstado(holder, item)
        }

        private fun aplicarFoto(holder: Carta, item: ItemDaPastaEscolhidaPeloComponente) {
            val bmp = CacheDeMiniaturasEmMemoria.obter(item.uri)
            holder.vista.foto.setImageBitmap(bmp)
            if (bmp == null) {
                carregarSePreciso(item, HtmlDoComponenteDoVisualizadorDeMidiaNaColecao.LADO_DA_MINIATURA_DA_GRADE)
            }
        }

        private fun aplicarEstado(holder: Carta, item: ItemDaPastaEscolhidaPeloComponente) {
            val marcada = item.uri in marcados
            holder.vista.definirEstado(
                emFoco = item.uri == uriEmFoco,
                modoSelecao = modoSelecao,
                marcada = marcada,
            )
        }
    }

    /** Carta quadrada com foto, nome, ▶ de vídeo, marca de seleção e moldura amarela. */
    inner class CartaQuadrada(contexto: Context) : FrameLayout(contexto) {
        val foto = ImageView(contexto).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.parseColor("#0A0D12"))
        }
        val nome = TextView(contexto).apply {
            setTextColor(Color.parseColor("#E8EAF0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(dp(7), dp(18), dp(7), dp(6))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.TRANSPARENT, Color.parseColor("#EB080A0E")),
            )
        }
        val play = TextView(contexto).apply {
            text = "▶"
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#F5C518"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#B806121F"))
            }
        }
        val seloMcp = TextView(contexto).apply {
            text = "MCP"
            setTextColor(Color.parseColor("#191A1F"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(dp(5), dp(1), dp(5), dp(1))
            background = GradientDrawable().apply {
                cornerRadius = dpf(5)
                setColor(Color.parseColor("#F5C518"))
            }
            visibility = View.GONE
        }
        private val marca = TextView(contexto).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#191A1F"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }
        private val moldura = GradientDrawable().apply { cornerRadius = dpf(12) }

        init {
            background = GradientDrawable().apply {
                cornerRadius = dpf(12)
                setColor(Color.parseColor("#14161C"))
                setStroke(dp(1), Color.parseColor("#2A2E38"))
            }
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, dpf(12))
                }
            }
            clipToOutline = true
            addView(foto, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            addView(play, LayoutParams(dp(36), dp(36), Gravity.CENTER))
            addView(nome, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM))
            addView(
                seloMcp,
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.END).apply {
                    rightMargin = dp(6)
                    topMargin = dp(6)
                },
            )
            addView(
                marca,
                LayoutParams(dp(22), dp(22), Gravity.TOP or Gravity.START).apply {
                    leftMargin = dp(6)
                    topMargin = dp(6)
                },
            )
            foreground = moldura
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        }

        fun definirEstado(emFoco: Boolean, modoSelecao: Boolean, marcada: Boolean) {
            val destacada = emFoco || (modoSelecao && marcada)
            moldura.setStroke(if (destacada) dp(2) else 0, Color.parseColor("#F5C518"))
            marca.visibility = if (modoSelecao) View.VISIBLE else View.GONE
            marca.text = if (marcada) "✓" else ""
            marca.background = GradientDrawable().apply {
                cornerRadius = dpf(6)
                if (marcada) {
                    setColor(Color.parseColor("#F5C518"))
                    setStroke(dp(1), Color.parseColor("#F5C518"))
                } else {
                    setColor(Color.parseColor("#730F1014"))
                    setStroke((1.5f * densidade).toInt(), Color.parseColor("#E8EAF0"))
                }
            }
        }
    }

    private fun dp(v: Int): Int = (v * densidade).toInt()
    private fun dpf(v: Int): Float = v * densidade

    /**
     * Miniaturas decodificadas compartilhadas por todas as janelas (sobrevive a fechar/reabrir o Visualizador).
     * RGB_565 de 192px ≈ 72 KB cada: 500 cabem com folga no teto.
     */
    private object CacheDeMiniaturasEmMemoria {
        private val teto = (Runtime.getRuntime().maxMemory() / 4).coerceAtMost(160L * 1024 * 1024).toInt()
        private val cache = object : LruCache<String, Bitmap>(teto) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
        }
        private val pendentes = java.util.Collections.synchronizedSet(mutableSetOf<String>())
        private val principal = Handler(Looper.getMainLooper())

        /** Chamados na thread principal quando uma miniatura entra no cache. */
        val ouvintes = java.util.concurrent.CopyOnWriteArrayList<(String) -> Unit>()

        fun obter(chave: String): Bitmap? = cache.get(chave)

        fun marcarPendente(chave: String): Boolean = pendentes.add(chave)

        fun concluir(chave: String, bitmap: Bitmap?) {
            if (bitmap != null) cache.put(chave, bitmap)
            pendentes.remove(chave)
            if (bitmap != null) principal.post { ouvintes.forEach { it(chave) } }
        }
    }

    companion object {
        private val CARGA_FOTO = Any()
        private val CARGA_ESTADO = Any()
        private val filaDeCarga = Executors.newFixedThreadPool(4)

        /**
         * Gera (se preciso) e decodifica a miniatura para a memória. Bloqueia: chame fora da thread principal.
         * A Activity usa ao abrir o app para a grade já nascer com todas as fotos.
         */
        fun temNaMemoria(uri: String): Boolean = CacheDeMiniaturasEmMemoria.obter(uri) != null

        fun miniaturaPronta(uri: String): Bitmap? {
            val limpa = com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
                .uriSemParametroDeMiniatura(uri.trim())
            return CacheDeMiniaturasEmMemoria.obter(limpa) ?: CacheDeMiniaturasEmMemoria.obter(uri)
        }

        fun aquecerNaMemoria(
            gerador: GeradorDeMiniaturasDaGradeDoVisualizadorDeMidiaComCacheEmDisco,
            item: ItemDaPastaEscolhidaPeloComponente,
            lado: Int,
        ) {
            val chave = item.uri
            if (CacheDeMiniaturasEmMemoria.obter(chave) != null) return
            if (!CacheDeMiniaturasEmMemoria.marcarPendente(chave)) return
            val video = RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao.ehVideo(item)
            val bmp = runCatching { decodificar(gerador, item.uri, video, lado) }.getOrNull()
            CacheDeMiniaturasEmMemoria.concluir(chave, bmp)
        }

        private fun decodificar(
            gerador: GeradorDeMiniaturasDaGradeDoVisualizadorDeMidiaComCacheEmDisco,
            uriTexto: String,
            video: Boolean,
            lado: Int,
        ): Bitmap? {
            val uri = Uri.parse(uriTexto)
            val arquivo = when (uri.scheme) {
                "content" -> gerador.arquivoEmCacheDeConteudo(uri, video, lado)
                "file" -> gerador.arquivoEmCacheDeArquivo(File(uri.path.orEmpty()), video, lado)
                else -> null
            } ?: return null
            val opcoes = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }
            return BitmapFactory.decodeFile(arquivo.absolutePath, opcoes)
        }
    }
}

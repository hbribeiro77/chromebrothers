package com.chromebrothers.navegador

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.LruCache
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chromebrothers.navegador.regras.SelecaoDeImagensDaBibliotecaParaReferenciaDaGeracao
import com.chromebrothers.navegador.regras.SelecaoDeImagensDaBibliotecaParaReferenciaDaGeracao.Filtro
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

/** Célula da grade sempre quadrada (altura = largura). */
class CelulaQuadradaDaEscolhaDaBiblioteca @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}

/** Uma imagem ou um vídeo da biblioteca no modal ([miniatura] = arquivo da imagem ou miniatura do vídeo). */
data class ItemDaEscolhaDaBibliotecaParaReferencia(
    val id: String,
    val nome: String?,
    val favorito: Boolean,
    val video: Boolean,
    val miniatura: File?,
)

/**
 * Miniaturas do modal, no mesmo esquema da grade do Visualizador de mídia (o que deixa o scroll liso):
 * JPEG pequeno no cache de disco + bitmap RGB_565 num cache em memória que dura o processo inteiro.
 * Abrir o modal de novo não decodifica nada que já foi visto.
 */
object MiniaturasDaEscolhaDaBiblioteca {
    const val LADO = 320
    private val teto = (Runtime.getRuntime().maxMemory() / 6).coerceAtMost(96L * 1024 * 1024).toInt()
    private val cache = object : LruCache<String, Bitmap>(teto) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }
    private val pendentes = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    private val fila = Executors.newFixedThreadPool(4)
    private val principal = Handler(Looper.getMainLooper())

    /** Chamados na thread principal com a chave que ficou pronta. */
    val ouvintes = CopyOnWriteArrayList<(String) -> Unit>()

    /** Lê o disco (lastModified): calcule uma vez por item, nunca durante o scroll. */
    fun chave(arquivo: File): String = "${arquivo.absolutePath}:${arquivo.lastModified()}"

    fun obter(chave: String?): Bitmap? = chave?.let { cache.get(it) }

    /** Agenda a geração (disco) + decodificação (memória) se ainda não tiver. */
    fun aquecer(gerador: GeradorDeMiniaturasDaGradeDoVisualizadorDeMidiaComCacheEmDisco, arquivo: File?, chave: String?) {
        if (arquivo == null || chave == null) return
        val c = chave
        if (cache.get(c) != null || !pendentes.add(c)) return
        fila.execute {
            val bmp = runCatching {
                gerador.arquivoEmCacheDeArquivo(arquivo, false, LADO)?.let { pequeno ->
                    BitmapFactory.decodeFile(
                        pequeno.absolutePath,
                        BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 },
                    )?.let { quadradoArredondado(it) }
                }
            }.getOrNull()
            if (bmp != null) cache.put(c, bmp)
            pendentes.remove(c)
            if (bmp != null) principal.post { ouvintes.forEach { it(c) } }
        }
    }

    /** Cor do cartão do modal: pinta os cantos, então não precisa de recorte nem de alfa. */
    private const val FUNDO_DO_CARTAO = 0xFF1A1D24.toInt()

    /**
     * Recorte central quadrado com cantos já arredondados (pintados com a cor do cartão).
     * Assim a grade não usa clipToOutline: com ~45 miniaturas na tela, o recorte em tempo real
     * deixava a GPU acima do orçamento de um quadro a 120 Hz.
     */
    private fun quadradoArredondado(origem: Bitmap): Bitmap {
        val lado = min(origem.width, origem.height).coerceAtLeast(1)
        val saida = Bitmap.createBitmap(lado, lado, Bitmap.Config.RGB_565)
        val tela = android.graphics.Canvas(saida)
        tela.drawColor(FUNDO_DO_CARTAO)
        val pincel = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        pincel.shader = android.graphics.BitmapShader(
            origem,
            android.graphics.Shader.TileMode.CLAMP,
            android.graphics.Shader.TileMode.CLAMP,
        ).apply {
            setLocalMatrix(
                android.graphics.Matrix().apply {
                    setTranslate(-(origem.width - lado) / 2f, -(origem.height - lado) / 2f)
                },
            )
        }
        // ≈ 12dp numa célula de ~150dp: mesma curvatura da moldura de seleção.
        val raio = lado * 0.08f
        tela.drawRoundRect(0f, 0f, lado.toFloat(), lado.toFloat(), raio, raio, pincel)
        origem.recycle()
        return saida
    }

    /** Só gera o JPEG pequeno em disco (abertura do app): não ocupa memória. Bloqueante. */
    fun pregerarNoDisco(gerador: GeradorDeMiniaturasDaGradeDoVisualizadorDeMidiaComCacheEmDisco, arquivo: File?) {
        if (arquivo == null || !arquivo.isFile) return
        runCatching { gerador.arquivoEmCacheDeArquivo(arquivo, false, LADO) }
    }
}

/**
 * Modal para escolher imagens (e, quando o modelo aceita, vídeos) da biblioteca como referência
 * da geração MCP: grade de miniaturas, filtro Tudo/Imagens/Vídeos, seleção numerada na ordem do toque.
 */
class ModalDeEscolhaDaBibliotecaParaReferenciaDaGeracao(
    private val activity: Activity,
    private val itens: List<ItemDaEscolhaDaBibliotecaParaReferencia>,
    private val gerador: GeradorDeMiniaturasDaGradeDoVisualizadorDeMidiaComCacheEmDisco,
    private val limite: Int,
    private val aoConfirmar: (List<ItemDaEscolhaDaBibliotecaParaReferencia>) -> Unit,
    private val aoCancelar: () -> Unit,
    /** Quantos vídeos ainda cabem (só importa se houver vídeos na lista). */
    private val limiteDeVideos: Int = 0,
    private val titulo: String? = null,
) {
    private val regra = SelecaoDeImagensDaBibliotecaParaReferenciaDaGeracao
    private val densidade = activity.resources.displayMetrics.density
    private val porId = itens.associateBy { it.id }

    /** Chave de cache de cada item, calculada uma vez (o scroll não toca no disco). */
    private val chavePorId: Map<String, String> = buildMap {
        itens.forEach { item -> item.miniatura?.let { put(item.id, MiniaturasDaEscolhaDaBiblioteca.chave(it)) } }
    }
    private var selecionados: List<String> = emptyList()
    private var filtro = Filtro.TUDO
    private var visiveis: List<ItemDaEscolhaDaBibliotecaParaReferencia> = itens
    private var posicaoPorChave: Map<String, Int> = emptyMap()
    private var confirmou = false

    private lateinit var subtitulo: TextView
    private lateinit var adicionar: Button
    private lateinit var grade: RecyclerView
    private val adaptador = Adaptador()
    private val botoesDeFiltro = mutableMapOf<Filtro, TextView>()

    private val aoFicarPronta: (String) -> Unit = { chave ->
        posicaoPorChave[chave]?.let { adaptador.notifyItemChanged(it, CARGA_FOTO) }
    }

    private fun videosEscolhidos() = selecionados.count { porId[it]?.video == true }
    private fun videoBloqueado(item: ItemDaEscolhaDaBibliotecaParaReferencia) =
        item.video && item.id !in selecionados && videosEscolhidos() >= limiteDeVideos

    fun mostrar() {
        val dialogo = Dialog(activity)
        val raiz = LayoutInflater.from(activity)
            .inflate(R.layout.layout_modal_de_escolha_da_biblioteca_para_referencia_da_geracao, null)
        titulo?.let { raiz.findViewById<TextView>(R.id.titulo_da_escolha_da_biblioteca).text = it }
        subtitulo = raiz.findViewById(R.id.subtitulo_da_escolha_da_biblioteca)
        adicionar = raiz.findViewById(R.id.botao_adicionar_da_escolha_da_biblioteca)
        val cancelar = raiz.findViewById<Button>(R.id.botao_cancelar_da_escolha_da_biblioteca)
        listOf(adicionar, cancelar).forEach { it.backgroundTintList = null }
        montarFiltros(raiz.findViewById(R.id.filtros_da_escolha_da_biblioteca))

        val tela = activity.resources.displayMetrics
        val largura = min((tela.widthPixels * 0.92f).toInt(), (880 * densidade).toInt())
        val altura = (tela.heightPixels * 0.86f).toInt()
        val util = largura - (48 * densidade).toInt()
        val colunas = max(3, (util / (150 * densidade)).toInt())

        grade = raiz.findViewById<RecyclerView>(R.id.grade_da_escolha_da_biblioteca).apply {
            layoutManager = GridLayoutManager(activity, colunas).apply {
                // Puxa uma fileira inteira a mais enquanto rola.
                initialPrefetchItemCount = colunas * 2
            }
            adapter = adaptador
            setHasFixedSize(true)
            itemAnimator = null
            setItemViewCacheSize(colunas * 4)
            // Com várias colunas, um fling solta muitas células de uma vez; o padrão (5) jogava
            // as sobras fora e inflava XML de novo no meio do scroll.
            recycledViewPool.setMaxRecycledViews(0, colunas * 12)
        }
        preCriarCelulas(colunas * 10)
        recalcularPosicoes()

        cancelar.setOnClickListener { dialogo.dismiss() }
        adicionar.setOnClickListener {
            if (selecionados.isEmpty()) return@setOnClickListener
            confirmou = true
            val escolhidos = selecionados.mapNotNull { porId[it] }
            dialogo.dismiss()
            aoConfirmar(escolhidos)
        }
        atualizarCabecalho()

        // Mesma técnica do Visualizador: pré-carrega tudo já na abertura, de cima para baixo.
        MiniaturasDaEscolhaDaBiblioteca.ouvintes += aoFicarPronta
        itens.forEach { MiniaturasDaEscolhaDaBiblioteca.aquecer(gerador, it.miniatura, chavePorId[it.id]) }

        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.setOnDismissListener {
            MiniaturasDaEscolhaDaBiblioteca.ouvintes -= aoFicarPronta
            if (!confirmou) aoCancelar()
        }
        dialogo.show()
        dialogo.window?.setLayout(largura, altura)
    }

    /** Infla as células antes do primeiro scroll (custa uns ms na abertura, não durante a rolagem). */
    private fun preCriarCelulas(quantas: Int) {
        val pool = grade.recycledViewPool
        repeat(quantas) {
            pool.putRecycledView(adaptador.createViewHolder(grade, 0))
        }
    }

    // ---------- filtro ----------

    private fun montarFiltros(faixa: LinearLayout) {
        if (!regra.mostraFiltro(itens) { it.video }) return
        faixa.visibility = View.VISIBLE
        faixa.background = GradientDrawable().apply {
            cornerRadius = 10 * densidade
            setColor(Color.parseColor("#12141A"))
            setStroke(densidade.toInt().coerceAtLeast(1), Color.parseColor("#2E323D"))
        }
        Filtro.entries.forEach { f ->
            val botao = TextView(activity).apply {
                text = f.rotulo
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                val h = (14 * densidade).toInt()
                val v = (7 * densidade).toInt()
                setPadding(h, v, h, v)
                setOnClickListener { aplicarFiltro(f) }
            }
            botoesDeFiltro[f] = botao
            faixa.addView(botao)
        }
        pintarFiltros()
    }

    private fun pintarFiltros() {
        botoesDeFiltro.forEach { (f, b) ->
            val ativo = f == filtro
            b.setTextColor(Color.parseColor(if (ativo) "#E8EAF0" else "#8B91A1"))
            b.background = if (ativo) {
                GradientDrawable().apply { cornerRadius = 7 * densidade; setColor(Color.parseColor("#22252E")) }
            } else {
                null
            }
        }
    }

    private fun aplicarFiltro(novo: Filtro) {
        if (novo == filtro) return
        filtro = novo
        visiveis = regra.filtrar(itens, filtro) { it.video }
        recalcularPosicoes()
        pintarFiltros()
        adaptador.notifyDataSetChanged()
        grade.scrollToPosition(0)
    }

    private fun recalcularPosicoes() {
        posicaoPorChave = buildMap {
            visiveis.forEachIndexed { i, item ->
                chavePorId[item.id]?.let { put(it, i) }
            }
        }
    }

    // ---------- seleção ----------

    private fun alternar(item: ItemDaEscolhaDaBibliotecaParaReferencia) {
        val antes = selecionados
        val depois = if (videoBloqueado(item)) antes else regra.alternar(antes, item.id, limite)
        if (depois == antes) {
            // Limite cheio: sacudida curta no subtítulo avisa sem abrir outro aviso.
            subtitulo.animate().cancel()
            subtitulo.translationX = 0f
            subtitulo.animate().translationX(8 * densidade).setDuration(60).withEndAction {
                subtitulo.animate().translationX(0f).setDuration(90).start()
            }.start()
            return
        }
        selecionados = depois
        atualizarCabecalho()
        // Números e bloqueios podem mudar em várias células: só o estado, sem mexer nas fotos.
        adaptador.notifyItemRangeChanged(0, visiveis.size, CARGA_ESTADO)
    }

    private fun atualizarCabecalho() {
        subtitulo.text = regra.subtitulo(selecionados.size, limite)
        adicionar.text = regra.rotuloDoBotao(selecionados.size)
        adicionar.isEnabled = selecionados.isNotEmpty()
        adicionar.alpha = if (selecionados.isNotEmpty()) 1f else 0.45f
    }

    // ---------- grade ----------

    private inner class Celula(vista: View) : RecyclerView.ViewHolder(vista) {
        val imagem: ImageView = vista.findViewById(R.id.imagem_da_escolha_da_biblioteca)
        val moldura: View = vista.findViewById(R.id.moldura_da_escolha_da_biblioteca)
        val selo: TextView = vista.findViewById(R.id.selo_da_escolha_da_biblioteca)
        val estrela: TextView = vista.findViewById(R.id.estrela_da_escolha_da_biblioteca)
        val seloDeVideo: TextView = vista.findViewById(R.id.selo_de_video_da_escolha_da_biblioteca)
        // Criados uma vez por célula: trocar o selo no bind não recarrega recurso nenhum.
        private val seloVazio = androidx.core.content.ContextCompat.getDrawable(
            vista.context, R.drawable.fundo_do_selo_vazio_da_escolha_da_biblioteca,
        )
        private val seloMarcado = androidx.core.content.ContextCompat.getDrawable(
            vista.context, R.drawable.fundo_do_selo_marcado_da_escolha_da_biblioteca,
        )

        // Placeholder arredondado só enquanto a miniatura (que já vem arredondada) carrega.
        private val carregando = GradientDrawable().apply {
            cornerRadius = 12f * densidade
            setColor(Color.parseColor("#22262E"))
        }

        init {
            // Sem clipToOutline de propósito: ver MiniaturasDaEscolhaDaBiblioteca.quadradoArredondado.
            imagem.scaleType = ImageView.ScaleType.FIT_XY
            vista.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) visiveis.getOrNull(pos)?.let { alternar(it) }
            }
        }

        fun aplicarFoto(item: ItemDaEscolhaDaBibliotecaParaReferencia) {
            val chave = chavePorId[item.id]
            val bmp = MiniaturasDaEscolhaDaBiblioteca.obter(chave)
            imagem.setImageBitmap(bmp)
            // Com a foto (opaca) na tela, o fundo cinza por baixo só gastaria GPU a cada quadro.
            val fundo = if (bmp == null) carregando else null
            if (imagem.background !== fundo) imagem.background = fundo
            if (bmp == null) MiniaturasDaEscolhaDaBiblioteca.aquecer(gerador, item.miniatura, chave)
        }

        fun aplicarEstado(item: ItemDaEscolhaDaBibliotecaParaReferencia, animar: Boolean) {
            val numero = regra.numero(selecionados, item.id)
            val escolhida = numero != null
            moldura.visibility = if (escolhida) View.VISIBLE else View.GONE
            selo.text = numero?.toString().orEmpty()
            val fundo = if (escolhida) seloMarcado else seloVazio
            if (selo.background !== fundo) selo.background = fundo
            val escala = if (escolhida) 0.9f else 1f
            val bloqueada = regra.bloqueada(selecionados, item.id, limite) || videoBloqueado(item)
            val opacidade = if (bloqueada) 0.4f else 1f
            if (animar && (imagem.scaleX != escala || itemView.alpha != opacidade)) {
                imagem.animate().scaleX(escala).scaleY(escala).setDuration(140).start()
                itemView.animate().alpha(opacidade).setDuration(140).start()
            } else if (!animar) {
                imagem.animate().cancel()
                itemView.animate().cancel()
                imagem.scaleX = escala
                imagem.scaleY = escala
                itemView.alpha = opacidade
            }
            itemView.contentDescription = buildString {
                append(item.nome?.takeIf { it.isNotBlank() } ?: if (item.video) "Vídeo da biblioteca" else "Imagem da biblioteca")
                if (numero != null) append(", escolhida número $numero")
            }
        }
    }

    private inner class Adaptador : RecyclerView.Adapter<Celula>() {
        override fun getItemCount() = visiveis.size

        override fun onCreateViewHolder(pai: ViewGroup, tipo: Int): Celula {
            val vista = LayoutInflater.from(pai.context)
                .inflate(R.layout.layout_item_da_escolha_da_biblioteca_para_referencia, pai, false)
            return Celula(vista)
        }

        override fun onBindViewHolder(celula: Celula, posicao: Int, cargas: MutableList<Any>) {
            if (cargas.isEmpty()) {
                onBindViewHolder(celula, posicao)
                return
            }
            val item = visiveis[posicao]
            if (cargas.contains(CARGA_FOTO)) celula.aplicarFoto(item)
            if (cargas.contains(CARGA_ESTADO)) celula.aplicarEstado(item, animar = true)
        }

        override fun onBindViewHolder(celula: Celula, posicao: Int) {
            val item = visiveis[posicao]
            celula.estrela.visibility = if (item.favorito) View.VISIBLE else View.GONE
            celula.seloDeVideo.visibility = if (item.video) View.VISIBLE else View.GONE
            celula.aplicarFoto(item)
            celula.aplicarEstado(item, animar = false)
        }
    }

    private companion object {
        val CARGA_FOTO = Any()
        val CARGA_ESTADO = Any()
    }
}

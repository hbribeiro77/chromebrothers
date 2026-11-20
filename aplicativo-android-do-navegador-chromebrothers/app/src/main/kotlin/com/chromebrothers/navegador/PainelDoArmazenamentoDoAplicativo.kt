package com.chromebrothers.navegador

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.chromebrothers.navegador.regras.ArmazenamentoDoAplicativo
import com.chromebrothers.navegador.regras.ArquivoDeMidiaSalvoNaGaveta
import com.chromebrothers.navegador.regras.CategoriaDoArmazenamento
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tela de Armazenamento: quanto cada parte ocupa, limpezas seguras (cache, referências sem uso, rascunhos
 * antigos, limpeza automática) e os maiores arquivos gerados com seleção. Confirmações em dois toques.
 */
class PainelDoArmazenamentoDoAplicativo(
    private val activity: Activity,
    private val medicao: MedicaoELimpezaDoArmazenamentoDoAplicativo,
    /** Mídia gerada (com tamanho), lida do disco: chamado fora da thread principal. */
    private val listarMidias: () -> List<ArquivoDeMidiaSalvoNaGaveta>,
    /** Exclui pela rotina das bibliotecas (mantém os índices em ordem): chamado fora da thread principal. */
    private val excluirMidias: (List<String>) -> Unit,
    /** Move para Movies/Pictures/ChromeBrothers; o callback roda na thread principal ao terminar. */
    private val moverMidias: (List<String>, () -> Unit) -> Unit,
    private val aoMudar: () -> Unit,
) {
    private val d = activity.resources.displayMetrics.density
    private fun dp(v: Float) = (v * d).toInt()
    private val dialogo = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    private val conteudo = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20f), dp(8f), dp(20f), dp(32f))
    }
    private val selecionadas = mutableSetOf<String>()

    fun mostrar() {
        val raiz = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(FUNDO)
        }
        raiz.addView(LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20f), dp(12f), dp(12f), dp(12f))
            setBackgroundColor(Color.parseColor("#1A1D24"))
            addView(texto("Armazenamento", 18f, TEXTO, negrito = true), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(botao("Fechar") { dialogo.dismiss() })
        })
        val rolagem = ScrollView(activity).apply {
            addView(LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                addView(conteudo, LinearLayout.LayoutParams(dp(720f).coerceAtMost(activity.resources.displayMetrics.widthPixels), ViewGroup.LayoutParams.WRAP_CONTENT))
            })
        }
        raiz.addView(rolagem, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        dialogo.setContentView(raiz)
        dialogo.show()
        recarregar()
    }

    private class Estado(
        val medida: MedicaoELimpezaDoArmazenamentoDoAplicativo.Medicao,
        val cache: MedicaoELimpezaDoArmazenamentoDoAplicativo.Liberavel,
        val referencias: MedicaoELimpezaDoArmazenamentoDoAplicativo.Liberavel,
        val rascunhos: Pair<List<String>, Long>,
        val maiores: List<ArquivoDeMidiaSalvoNaGaveta>,
        val favoritas: Set<String>,
    )

    private fun recarregar() {
        conteudo.removeAllViews()
        conteudo.addView(texto("Calculando…", 15f, TEXTO_SUAVE).apply { setPadding(0, dp(24f), 0, 0) })
        Thread {
            val estado = runCatching {
                Estado(
                    medida = medicao.medir(),
                    cache = medicao.cacheProprio(),
                    referencias = medicao.referenciasSemUso(),
                    rascunhos = medicao.rascunhosAntigos(),
                    maiores = listarMidias().filter { it.tamanho > 0 }.sortedByDescending { it.tamanho }.take(40),
                    favoritas = medicao.urisFavoritas(),
                )
            }.getOrNull()
            activity.runOnUiThread {
                if (!dialogo.isShowing) return@runOnUiThread
                if (estado == null) {
                    conteudo.removeAllViews()
                    conteudo.addView(texto("Não consegui medir o armazenamento.", 15f, Color.parseColor("#F28B82")))
                } else {
                    pintar(estado)
                }
            }
        }.start()
    }

    private fun pintar(e: Estado) {
        conteudo.removeAllViews()
        selecionadas.retainAll(e.maiores.map { it.uri }.toSet())
        val total = e.medida.total
        conteudo.addView(texto("O app ocupa ${ArmazenamentoDoAplicativo.formatarBytes(total)}", 22f, TEXTO, negrito = true).apply { setPadding(0, dp(16f), 0, dp(10f)) })
        conteudo.addView(BarraEmpilhada(activity, e.medida.porCategoria.map { (c, t) -> cor(c) to t.bytes }), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(18f)))
        e.medida.porCategoria.forEach { (c, t) ->
            conteudo.addView(linhaDaCategoria(c, t.bytes, t.quantidade))
        }

        secao("Liberar espaço")
        val cacheTotal = e.medida.porCategoria[CategoriaDoArmazenamento.CACHE]?.bytes ?: e.cache.bytes
        acao(
            "Limpar cache",
            "Miniaturas, downloads temporários, capturas e o cache do navegador. Tudo é refeito quando precisar.",
            "Limpar ${ArmazenamentoDoAplicativo.formatarBytes(cacheTotal)}",
            habilitada = cacheTotal > 0,
        ) {
            medicao.limparCacheDosMotores()
            Thread { medicao.apagar(e.cache); activity.runOnUiThread { aoMudar(); recarregar() } }.start()
        }
        acao(
            "Referências sem uso",
            "Cópias de imagens e vídeos de referência de gerações que não existem mais (nem card, nem item na biblioteca).",
            if (e.referencias.quantidade == 0) "Nada para liberar"
            else "Liberar ${ArmazenamentoDoAplicativo.formatarBytes(e.referencias.bytes)} (${e.referencias.quantidade} arquivos)",
            habilitada = e.referencias.quantidade > 0,
        ) {
            Thread { medicao.apagar(e.referencias); activity.runOnUiThread { aoMudar(); recarregar() } }.start()
        }
        val (rascunhos, bytesDosRascunhos) = e.rascunhos
        acao(
            "Rascunhos antigos",
            "Rascunhos do editor de imagem sem abrir há mais de 30 dias (o mais recente nunca entra).",
            if (rascunhos.isEmpty()) "Nenhum" else "Apagar ${rascunhos.size} (${ArmazenamentoDoAplicativo.formatarBytes(bytesDosRascunhos)})",
            habilitada = rascunhos.isNotEmpty(),
        ) {
            Thread { medicao.apagarRascunhos(rascunhos); activity.runOnUiThread { aoMudar(); recarregar() } }.start()
        }
        limpezaAutomatica()

        secao("Maiores arquivos gerados")
        if (e.maiores.isEmpty()) {
            conteudo.addView(texto("Nenhuma mídia gerada guardada no aparelho.", 14f, TEXTO_SUAVE))
            return
        }
        conteudo.addView(texto("Mover para a Galeria tira o arquivo do app e põe em Movies/ChromeBrothers ou Pictures/ChromeBrothers: aparece na Galeria, continua no Visualizador e sobrevive se o app for desinstalado.", 13f, TEXTO_SUAVE).apply { setPadding(0, 0, 0, dp(8f)) })
        val rotuloDaSelecao = texto("", 14f, TEXTO_SUAVE)
        val apagar = botao("Apagar selecionados") {}
        val mover = botao("Mover para a Galeria") {}
        val caixas = mutableListOf<Pair<CheckBox, ArquivoDeMidiaSalvoNaGaveta>>()
        fun atualizarSelecao() {
            val bytes = e.maiores.filter { it.uri in selecionadas }.sumOf { it.tamanho }
            rotuloDaSelecao.text = if (selecionadas.isEmpty()) "Nada selecionado" else "${selecionadas.size} selecionados · ${ArmazenamentoDoAplicativo.formatarBytes(bytes)}"
            apagar.alpha = if (selecionadas.isEmpty()) 0.4f else 1f
            mover.alpha = if (selecionadas.isEmpty()) 0.4f else 1f
        }
        val barra = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(botao("Selecionar tudo (sem favoritos)") {
                selecionadas.clear()
                e.maiores.filter { it.uri !in e.favoritas }.forEach { selecionadas += it.uri }
                caixas.forEach { (caixa, item) -> caixa.isChecked = item.uri in selecionadas }
                atualizarSelecao()
            })
            addView(botao("Limpar seleção") {
                selecionadas.clear()
                caixas.forEach { it.first.isChecked = false }
                atualizarSelecao()
            }, margem())
        }
        conteudo.addView(barra)
        e.maiores.forEach { item ->
            val favorita = item.uri in e.favoritas
            val caixa = CheckBox(activity).apply {
                isChecked = item.uri in selecionadas
                setOnCheckedChangeListener { _, marcada ->
                    if (marcada) selecionadas += item.uri else selecionadas -= item.uri
                    atualizarSelecao()
                }
            }
            caixas += caixa to item
            val tipo = if (item.mime.startsWith("video") || item.nomeDoArquivo.endsWith(".mp4")) "Vídeo" else "Imagem"
            val data = if (item.instante > 0) SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(item.instante)) else ""
            conteudo.addView(LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(2f), 0, dp(2f))
                addView(caixa)
                addView(LinearLayout(activity).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(texto((if (favorita) "★ " else "") + item.nomeDoArquivo, 14f, TEXTO).apply { maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.MIDDLE })
                    addView(texto(listOf(tipo, data).filter { it.isNotBlank() }.joinToString(" · "), 12f, TEXTO_SUAVE))
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                addView(texto(ArmazenamentoDoAplicativo.formatarBytes(item.tamanho), 14f, Color.parseColor("#AECBFA")))
            })
        }
        apagar.setTextColor(CaixaDeConfirmacaoDoNavegador.COR_DE_EXCLUIR)
        apagar.setOnClickListener {
            if (selecionadas.isEmpty()) return@setOnClickListener
            val uris = selecionadas.toList()
            val bytes = e.maiores.filter { it.uri in selecionadas }.sumOf { it.tamanho }
            CaixaDeConfirmacaoDoNavegador.mostrar(
                activity = activity,
                titulo = CaixaDeConfirmacaoDoNavegador.tituloDeExcluir(uris.size),
                mensagem = CaixaDeConfirmacaoDoNavegador.mensagemDeExcluir(uris.size, ArmazenamentoDoAplicativo.formatarBytes(bytes)),
                textoConfirmar = "Excluir",
            ) {
                selecionadas.removeAll(uris.toSet())
                Thread { runCatching { excluirMidias(uris) }; activity.runOnUiThread { aoMudar(); recarregar() } }.start()
            }
        }
        mover.setOnClickListener {
            if (selecionadas.isEmpty()) return@setOnClickListener
            val uris = selecionadas.toList()
            selecionadas.removeAll(uris.toSet())
            moverMidias(uris) { aoMudar(); recarregar() }
        }
        conteudo.addView(LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12f), 0, 0)
            addView(rotuloDaSelecao, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(mover)
            addView(apagar, margem())
        })
        atualizarSelecao()
    }

    private fun limpezaAutomatica() {
        val atual = medicao.diasDaLimpezaAutomatica()
        conteudo.addView(texto("Limpeza automática", 15f, TEXTO, negrito = true).apply { setPadding(0, dp(18f), 0, 0) })
        conteudo.addView(texto("Ao abrir o app, apaga referências sem uso e cache do app parados há mais tempo que o prazo. Nunca toca na mídia da biblioteca nem em favoritos.", 13f, TEXTO_SUAVE).apply { setPadding(0, dp(2f), 0, dp(8f)) })
        val linha = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        listOf(0 to "Desligada", 30 to "30 dias", 90 to "90 dias").forEach { (dias, rotulo) ->
            linha.addView(botao(rotulo, destaque = dias == atual) {
                medicao.definirDiasDaLimpezaAutomatica(dias)
                recarregar()
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(8f) })
        }
        conteudo.addView(linha)
    }

    // ---------- Peças ----------

    private fun secao(titulo: String) {
        conteudo.addView(View(activity).apply { setBackgroundColor(Color.parseColor("#2A2F38")) },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1f)).apply { topMargin = dp(22f) })
        conteudo.addView(texto(titulo, 17f, TEXTO, negrito = true).apply { setPadding(0, dp(14f), 0, dp(4f)) })
    }

    /** Uma limpeza: título, explicação e botão com o espaço; primeiro toque pede confirmação. */
    private fun acao(titulo: String, descricao: String, rotulo: String, habilitada: Boolean, executar: () -> Unit) {
        var confirmando = false
        val botao = botao(rotulo) {}
        botao.alpha = if (habilitada) 1f else 0.4f
        botao.setOnClickListener {
            if (!habilitada) return@setOnClickListener
            if (!confirmando) {
                confirmando = true
                botao.text = "Toque de novo: $rotulo"
                botao.setTextColor(Color.parseColor("#F28B82"))
                return@setOnClickListener
            }
            botao.text = "Liberando…"
            botao.isEnabled = false
            executar()
        }
        conteudo.addView(LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10f), 0, dp(4f))
            addView(LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                addView(texto(titulo, 15f, TEXTO, negrito = true))
                addView(texto(descricao, 13f, TEXTO_SUAVE).apply { setPadding(0, dp(2f), dp(12f), 0) })
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(botao)
        })
    }

    private fun linhaDaCategoria(c: CategoriaDoArmazenamento, bytes: Long, quantidade: Int) = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(8f), 0, 0)
        addView(View(activity).apply { background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(cor(c)) } },
            LinearLayout.LayoutParams(dp(10f), dp(10f)).apply { marginEnd = dp(10f) })
        addView(texto(c.rotulo, 14f, TEXTO), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        addView(texto("$quantidade arquivos  ", 12f, TEXTO_SUAVE))
        addView(texto(ArmazenamentoDoAplicativo.formatarBytes(bytes), 14f, TEXTO))
    }

    private fun cor(c: CategoriaDoArmazenamento): Int = Color.parseColor(
        when (c) {
            CategoriaDoArmazenamento.VIDEOS_GERADOS -> "#8AB4F8"
            CategoriaDoArmazenamento.IMAGENS -> "#81C995"
            CategoriaDoArmazenamento.REFERENCIAS -> "#FDD663"
            CategoriaDoArmazenamento.VIDEOS_DA_WEB -> "#C58AF9"
            CategoriaDoArmazenamento.CONVERSAS -> "#78D9EC"
            CategoriaDoArmazenamento.PINCEIS_E_RASCUNHOS -> "#FCAD70"
            CategoriaDoArmazenamento.NAVEGADOR -> "#F28B82"
            CategoriaDoArmazenamento.CACHE -> "#9AA0A6"
            CategoriaDoArmazenamento.OUTROS -> "#5F6368"
        },
    )

    private fun texto(t: String, sp: Float, cor: Int, negrito: Boolean = false) = TextView(activity).apply {
        text = t
        setTextColor(cor)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        if (negrito) typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private fun margem() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(8f) }

    private fun botao(t: String, destaque: Boolean = false, aoTocar: () -> Unit) = TextView(activity).apply {
        text = t
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setTextColor(if (destaque) Color.parseColor("#14171D") else TEXTO)
        background = GradientDrawable().apply {
            cornerRadius = 10f * d
            setColor(if (destaque) Color.parseColor("#8AB4F8") else Color.parseColor("#2A2F38"))
        }
        setPadding(dp(14f), dp(9f), dp(14f), dp(9f))
        isClickable = true
        setOnClickListener { aoTocar() }
    }

    /** Barra com um segmento por categoria, proporcional ao espaço. */
    private class BarraEmpilhada(context: Context, private val partes: List<Pair<Int, Long>>) : View(context) {
        private val pintura = Paint(Paint.ANTI_ALIAS_FLAG)
        private val retangulo = RectF()
        override fun onDraw(canvas: Canvas) {
            val total = partes.sumOf { it.second }.coerceAtLeast(1L)
            val raio = height / 2f
            canvas.save()
            val caminho = android.graphics.Path().apply { addRoundRect(0f, 0f, width.toFloat(), height.toFloat(), raio, raio, android.graphics.Path.Direction.CW) }
            canvas.clipPath(caminho)
            pintura.color = Color.parseColor("#2A2F38")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), pintura)
            var x = 0f
            partes.forEach { (cor, bytes) ->
                val largura = width * (bytes.toFloat() / total)
                pintura.color = cor
                retangulo.set(x, 0f, x + largura, height.toFloat())
                canvas.drawRect(retangulo, pintura)
                x += largura
            }
            canvas.restore()
        }
    }

    private companion object {
        val FUNDO = Color.parseColor("#14171D")
        val TEXTO = Color.parseColor("#F4F7FB")
        val TEXTO_SUAVE = Color.parseColor("#9AA0A6")
    }
}

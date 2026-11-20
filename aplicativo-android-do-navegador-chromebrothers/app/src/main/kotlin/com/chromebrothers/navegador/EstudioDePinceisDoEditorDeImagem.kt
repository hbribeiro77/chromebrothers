package com.chromebrothers.navegador

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.chromebrothers.navegador.regras.FormaDoCarimbo
import com.chromebrothers.navegador.regras.MotorDeCarimbosDoPincel
import com.chromebrothers.navegador.regras.PincelDoMotor
import com.chromebrothers.navegador.regras.PontoParaCarimbar
import com.chromebrothers.navegador.regras.TamanhoDoPincelDoEditorDeImagem
import kotlin.math.roundToInt

/**
 * Estúdio de pincéis (estilo Brush Studio): seções à esquerda, controles no meio, área de teste à direita.
 * Pincel pronto (predefinido) ou "novo" é editado como cópia em "Meus pincéis"; Pronto grava, Cancelar descarta.
 */
class EstudioDePinceisDoEditorDeImagem(
    private val activity: Activity,
    base: PincelDoMotor,
    comoCopia: Boolean,
    private val cor: () -> Int,
    private val pedirImagem: ((Uri?) -> Unit) -> Unit,
    /** Exporta o pincel como está agora no estúdio (.chromebrush em Downloads/ChromeBrothers). */
    private val exportar: (PincelDoMotor) -> Unit,
    private val aoSalvar: (PincelDoMotor) -> Unit,
    private val aoApagar: (String) -> Unit,
) {
    private enum class Secao(val titulo: String) {
        TRACO("Traço"),
        AFINAMENTO("Afinamento"),
        FORMA("Forma"),
        GRANULADO("Granulado"),
        DINAMICA("Dinâmica"),
        ESTABILIZACAO("Estabilização"),
        CANETA("Caneta"),
        LIMITES("Limites"),
    }

    private val d = activity.resources.displayMetrics.density
    private fun dp(v: Float) = (v * d).toInt()

    private val ehCopia = comoCopia || CatalogoDePinceisDoMotor.ehPredefinido(base.id) ||
        CatalogoDePinceisDoMotor.porId(base.id) == null
    private val original: PincelDoMotor = if (ehCopia) {
        base.copy(id = CatalogoDePinceisDoMotor.novoId(), nome = if (comoCopia && !CatalogoDePinceisDoMotor.ehPredefinido(base.id)) "${base.nome} 2" else "${base.nome} (meu)")
    } else {
        base
    }
    private var atual = original
    private var secao = Secao.TRACO

    private val dialogo = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    private val area = AreaDeTesteDoPincel(activity, cor).apply { pincel = atual }
    private val controles = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16f), dp(8f), dp(16f), dp(24f))
    }
    private val botoesDasSecoes = mutableMapOf<Secao, TextView>()
    private val campoDoNome = EditText(activity).apply {
        setText(original.nome)
        setTextColor(TEXTO)
        setHintTextColor(TEXTO_SUAVE)
        hint = "Nome do pincel"
        setSingleLine(true)
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        background = GradientDrawable().apply { cornerRadius = 10f * d; setColor(Color.parseColor("#22262E")) }
        setPadding(dp(12f), dp(8f), dp(12f), dp(8f))
    }
    private var confirmandoApagar = false

    fun mostrar() {
        val largo = activity.resources.configuration.screenWidthDp >= 720
        val raiz = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(FUNDO)
        }
        raiz.addView(cabecalho(), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        val rolagemDosControles = ScrollView(activity).apply { addView(controles) }
        val areaComBotao = FrameLayout(activity).apply {
            setPadding(dp(12f), dp(12f), dp(12f), dp(12f))
            addView(area, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            addView(
                botao("Limpar") { area.limpar() },
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.END)
                    .apply { setMargins(0, dp(8f), dp(8f), 0) },
            )
        }
        if (largo) {
            val corpo = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
            val secoes = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12f), dp(8f), dp(4f), dp(8f))
            }
            Secao.entries.forEach { s -> secoes.addView(botaoDeSecao(s), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48f))) }
            corpo.addView(secoes, LinearLayout.LayoutParams(dp(160f), ViewGroup.LayoutParams.MATCH_PARENT))
            corpo.addView(rolagemDosControles, LinearLayout.LayoutParams(dp(380f), ViewGroup.LayoutParams.MATCH_PARENT))
            corpo.addView(areaComBotao, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
            raiz.addView(corpo, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        } else {
            raiz.addView(areaComBotao, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(240f)))
            val secoes = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(12f), 0, dp(12f), 0)
            }
            Secao.entries.forEach { s ->
                secoes.addView(botaoDeSecao(s), LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(44f)).apply { marginEnd = dp(6f) })
            }
            raiz.addView(HorizontalScrollView(activity).apply { isHorizontalScrollBarEnabled = false; addView(secoes) })
            raiz.addView(rolagemDosControles, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        }
        dialogo.setContentView(raiz)
        dialogo.setCancelable(true)
        mostrarSecao(Secao.TRACO)
        dialogo.show()
    }

    // ---------- Cabeçalho ----------

    private fun cabecalho(): View = LinearLayout(activity).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(16f), dp(10f), dp(12f), dp(10f))
        setBackgroundColor(Color.parseColor("#1A1D24"))
        addView(TextView(activity).apply {
            text = "Estúdio de pincéis"
            setTextColor(TEXTO)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 0, dp(16f), 0)
        })
        addView(campoDoNome, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(12f) })
        addView(botao("Exportar") {
            exportar(atual.copy(nome = campoDoNome.text.toString().trim().ifBlank { atual.nome }))
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(8f) })
        addView(botao("Desfazer tudo") {
            atual = original
            campoDoNome.setText(original.nome)
            atualizarArea()
            mostrarSecao(secao)
        })
        if (!ehCopia) {
            val apagar = botao("Apagar") {}
            apagar.setOnClickListener {
                if (!confirmandoApagar) {
                    confirmandoApagar = true
                    apagar.text = "Toque de novo para apagar"
                    apagar.setTextColor(Color.parseColor("#F28B82"))
                    return@setOnClickListener
                }
                CatalogoDePinceisDoMotor.apagar(original.id)
                aoApagar(original.id)
                dialogo.dismiss()
            }
            addView(apagar, margem())
        }
        addView(botao("Cancelar") { dialogo.dismiss() }, margem())
        addView(botao("Pronto", destaque = true) { salvar() }, margem())
    }

    private fun margem() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        .apply { marginStart = dp(8f) }

    private fun salvar() {
        val nome = campoDoNome.text.toString().trim().ifBlank { "Meu pincel" }.take(40)
        val final = atual.copy(nome = nome)
        CatalogoDePinceisDoMotor.salvar(final)
        aoSalvar(final)
        dialogo.dismiss()
    }

    // ---------- Seções ----------

    private fun botaoDeSecao(s: Secao): TextView = TextView(activity).apply {
        text = s.titulo
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(14f), 0, dp(14f), 0)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        isClickable = true
        setOnClickListener { mostrarSecao(s) }
        botoesDasSecoes[s] = this
    }

    private fun mostrarSecao(s: Secao) {
        secao = s
        botoesDasSecoes.forEach { (outra, botao) ->
            val sel = outra == s
            botao.setTextColor(if (sel) Color.parseColor("#AECBFA") else TEXTO)
            botao.background = if (sel) GradientDrawable().apply { cornerRadius = 12f * d; setColor(Color.parseColor("#292F3D")) } else null
        }
        controles.removeAllViews()
        when (s) {
            Secao.TRACO -> {
                titulo("Traço")
                deslizante("Espaçamento", 0.01f, 1.5f, atual.espacamento, ::porcento) { p, v -> p.copy(espacamento = v) }
                deslizante("Dispersão", 0f, 1f, atual.dispersao, ::porcento) { p, v -> p.copy(dispersao = v) }
                deslizante("Fluxo", 0.02f, 1f, atual.fluxo, ::porcento) { p, v -> p.copy(fluxo = v) }
                deslizante("Variação de tamanho", 0f, 1f, atual.tremorDeTamanho, ::porcento) { p, v -> p.copy(tremorDeTamanho = v) }
                deslizante("Variação de opacidade", 0f, 1f, atual.tremorDeOpacidade, ::porcento) { p, v -> p.copy(tremorDeOpacidade = v) }
                dica("Fluxo baixo acumula aos poucos (bom para aerógrafo); o traço nunca passa da opacidade escolhida.")
            }
            Secao.AFINAMENTO -> {
                titulo("Afinamento")
                deslizante("Início do traço", 0f, 1f, atual.afinamentoInicio, ::porcento) { p, v -> p.copy(afinamentoInicio = v) }
                deslizante("Fim do traço", 0f, 1f, atual.afinamentoFim, ::porcento) { p, v -> p.copy(afinamentoFim = v) }
                dica("Vale com o dedo e com a caneta. O fim afina ao vivo, sob a ponta.")
            }
            Secao.FORMA -> mostrarForma()
            Secao.GRANULADO -> mostrarGranulado()
            Secao.DINAMICA -> {
                titulo("Dinâmica")
                deslizante("Velocidade → tamanho", 0f, 1f, atual.velocidadeNoTamanho, ::porcento) { p, v -> p.copy(velocidadeNoTamanho = v) }
                deslizante("Velocidade → opacidade", 0f, 1f, atual.velocidadeNaOpacidade, ::porcento) { p, v -> p.copy(velocidadeNaOpacidade = v) }
                dica("Traço rápido afina e/ou clareia; devagar fica cheio, como pincel de verdade. Funciona com o dedo também.")
            }
            Secao.ESTABILIZACAO -> {
                titulo("Estabilização")
                deslizante("StreamLine", 0f, 1f, atual.streamline, ::porcento) { p, v -> p.copy(streamline = v) }
                dica("O traço segue a ponta com um atraso suave: some o tremor da mão e as curvas saem redondas. Ao levantar, o traço alcança a ponta, então o fim não fica curto. Bom para letras e contornos; no lápis e no giz deixe baixo.")
            }
            Secao.CANETA -> {
                titulo("Caneta")
                deslizante("Pressão → tamanho", 0f, 1f, atual.pressaoNoTamanho, ::porcento) { p, v -> p.copy(pressaoNoTamanho = v) }
                deslizante("Pressão → opacidade", 0f, 1f, atual.pressaoNaOpacidade, ::porcento) { p, v -> p.copy(pressaoNaOpacidade = v) }
                dica("Na pressão normal o traço tem o tamanho escolhido. Apertar engrossa (até 2× com 100%) e aliviar afina; a opacidade só clareia no toque leve. Com o dedo vale o tamanho escolhido.")
            }
            Secao.LIMITES -> {
                titulo("Limites")
                deslizante("Tamanho base", 0.2f, 6f, atual.multiplicadorDeLargura, { "${(it * 100).roundToInt()}%" }) { p, v -> p.copy(multiplicadorDeLargura = v) }
                deslizante("Tamanho mínimo", 0f, 1f, atual.tamanhoMinimo, ::porcento) { p, v -> p.copy(tamanhoMinimo = v) }
                deslizante("Tamanho máximo", 0f, 1f, atual.tamanhoMaximo, ::porcento) { p, v -> p.copy(tamanhoMaximo = v) }
                deslizante("Opacidade mínima", 0f, 1f, atual.opacidadeMinima, ::porcento) { p, v -> p.copy(opacidadeMinima = v) }
                deslizante("Opacidade máxima", 0f, 1f, atual.opacidadeMaxima, ::porcento) { p, v -> p.copy(opacidadeMaxima = v) }
                dica("Os controles de tamanho e opacidade da barra lateral andam só entre o mínimo e o máximo deste pincel.")
            }
        }
    }

    private fun mostrarForma() {
        titulo("Forma")
        val fontes = listOf(
            "Redonda" to FormaDoCarimbo.REDONDA,
            "Quadrada" to FormaDoCarimbo.QUADRADA,
            "Granulada" to FormaDoCarimbo.RUIDOSA,
            "Imagem" to FormaDoCarimbo.IMAGEM,
        )
        val linha = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        fontes.forEach { (rotulo, forma) ->
            val sel = atual.forma == forma
            linha.addView(botao(rotulo, destaque = sel) {
                if (forma == FormaDoCarimbo.IMAGEM && atual.arquivoDaForma.isBlank()) {
                    importarImagem()
                } else {
                    atual = atual.copy(forma = forma)
                    atualizarArea()
                    mostrarSecao(Secao.FORMA)
                }
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(6f) })
        }
        controles.addView(HorizontalScrollView(activity).apply { isHorizontalScrollBarEnabled = false; addView(linha) })
        mostrarPontasProntas()
        if (atual.forma == FormaDoCarimbo.IMAGEM) {
            val previa = ImageView(activity).apply {
                setBackgroundColor(Color.parseColor("#0E1014"))
                scaleType = ImageView.ScaleType.FIT_CENTER
                CatalogoDePinceisDoMotor.arquivoDaForma(atual.arquivoDaForma)?.let { setImageBitmap(BitmapFactory.decodeFile(it.absolutePath)) }
            }
            controles.addView(previa, LinearLayout.LayoutParams(dp(112f), dp(112f)).apply { topMargin = dp(10f) })
        }
        val acoes = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        acoes.addView(botao("Importar imagem…") { importarImagem() })
        if (atual.forma == FormaDoCarimbo.IMAGEM) {
            acoes.addView(botao("Inverter") {
                val invertida = CatalogoDePinceisDoMotor.inverterForma(atual.arquivoDaForma)
                if (invertida != null) {
                    atual = atual.copy(arquivoDaForma = invertida)
                    atualizarArea()
                    mostrarSecao(Secao.FORMA)
                }
            }, margem())
        }
        controles.addView(acoes, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10f) })
        dica("Na imagem, o claro vira tinta e o escuro fica transparente (use Inverter para o contrário).")
        if (atual.forma == FormaDoCarimbo.REDONDA || atual.forma == FormaDoCarimbo.RUIDOSA) {
            deslizante("Dureza da borda", 0f, 1f, atual.dureza, ::porcento) { p, v -> p.copy(dureza = v) }
        }
        deslizante("Arredondamento", 0.05f, 1f, atual.proporcao, ::porcento) { p, v -> p.copy(proporcao = v) }
        alternancia("Girar com a direção do traço", atual.seguirTraco) { p, v -> p.copy(seguirTraco = v) }
        deslizante("Ângulo", -180f, 180f, atual.anguloFixo, { "${it.roundToInt()}°" }) { p, v -> p.copy(anguloFixo = v) }
        deslizante("Rotação aleatória", 0f, 1f, atual.tremorDeRotacao, ::porcento) { p, v -> p.copy(tremorDeRotacao = v) }
    }

    /** Pontas prontas (desenhadas pelo app): miniaturas; tocar troca a forma do pincel por ela. */
    private fun mostrarPontasProntas() {
        controles.addView(TextView(activity).apply {
            text = "Pontas prontas"
            textSize = 13f
            setTextColor(Color.parseColor("#9AA0AA"))
            setPadding(0, dp(12f), 0, dp(6f))
        })
        val linha = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        com.chromebrothers.navegador.regras.PontasProntasDoMotor.TODAS.forEach { ponta ->
            val selecionada = atual.forma == FormaDoCarimbo.IMAGEM && atual.arquivoDaForma == ponta.arquivo
            val miniatura = ImageView(activity).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                setPadding(dp(6f), dp(6f), dp(6f), dp(6f))
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 12f * activity.resources.displayMetrics.density
                    setColor(Color.parseColor("#0E1014"))
                    if (selecionada) setStroke(dp(2f), Color.parseColor("#8AB4F8"))
                }
            }
            // Gera/decodifica fora da thread principal (a primeira vez desenha a máscara).
            Thread {
                val arquivo = CatalogoDePinceisDoMotor.arquivoDaForma(ponta.arquivo)
                val imagem = arquivo?.let { BitmapFactory.decodeFile(it.absolutePath) }
                activity.runOnUiThread { if (imagem != null) miniatura.setImageBitmap(imagem) }
            }.start()
            val bloco = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                addView(miniatura, LinearLayout.LayoutParams(dp(64f), dp(64f)))
                addView(TextView(activity).apply {
                    text = ponta.nome
                    textSize = 11f
                    gravity = android.view.Gravity.CENTER
                    maxLines = 2
                    setTextColor(if (selecionada) Color.parseColor("#AECBFA") else Color.parseColor("#C4C7CE"))
                }, LinearLayout.LayoutParams(dp(72f), ViewGroup.LayoutParams.WRAP_CONTENT))
                setOnClickListener {
                    atual = atual.copy(forma = FormaDoCarimbo.IMAGEM, arquivoDaForma = ponta.arquivo)
                    atualizarArea()
                    mostrarSecao(Secao.FORMA)
                }
            }
            linha.addView(bloco, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = dp(6f)
            })
        }
        controles.addView(HorizontalScrollView(activity).apply { isHorizontalScrollBarEnabled = false; addView(linha) })
    }

    private fun mostrarGranulado() {
        titulo("Granulado")
        val fontes = listOf(
            "Nenhum" to com.chromebrothers.navegador.regras.FonteDoGranulado.NENHUM,
            "Papel" to com.chromebrothers.navegador.regras.FonteDoGranulado.PAPEL,
            "Tela" to com.chromebrothers.navegador.regras.FonteDoGranulado.TELA,
            "Imagem" to com.chromebrothers.navegador.regras.FonteDoGranulado.IMAGEM,
        )
        val linha = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        fontes.forEach { (rotulo, fonte) ->
            linha.addView(botao(rotulo, destaque = atual.granulado == fonte) {
                if (fonte == com.chromebrothers.navegador.regras.FonteDoGranulado.IMAGEM && atual.arquivoDoGranulado.isBlank()) {
                    importarImagem(paraGranulado = true)
                } else {
                    atual = atual.copy(granulado = fonte)
                    atualizarArea()
                    mostrarSecao(Secao.GRANULADO)
                }
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(6f) })
        }
        controles.addView(HorizontalScrollView(activity).apply { isHorizontalScrollBarEnabled = false; addView(linha) })
        controles.addView(botao("Importar textura…") { importarImagem(paraGranulado = true) },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10f) })
        if (atual.granulado == com.chromebrothers.navegador.regras.FonteDoGranulado.NENHUM) {
            dica("Escolha Papel, Tela ou uma imagem para dar textura ao traço.")
            return
        }
        alternancia("Preso ao papel", atual.granuladoFixoNoPapel) { p, v -> p.copy(granuladoFixoNoPapel = v) }
        dica(
            "Preso ao papel: a textura fica parada na folha e repassar revela o mesmo grão (giz em papel áspero). " +
                "Desligado: a textura vai dentro de cada carimbo e anda com o traço.",
        )
        deslizante("Escala da textura", 0.25f, 4f, atual.escalaDoGranulado, { String.format(java.util.Locale("pt", "BR"), "%.1f×", it) }) { p, v -> p.copy(escalaDoGranulado = v) }
        deslizante("Intensidade", 0f, 1f, atual.intensidadeDoGranulado, ::porcento) { p, v -> p.copy(intensidadeDoGranulado = v) }
        dica("Na imagem de textura, o claro deixa a tinta passar e o escuro segura.")
    }

    private fun importarImagem(paraGranulado: Boolean = false) {
        pedirImagem { uri ->
            if (uri == null) return@pedirImagem
            Thread {
                val nome = runCatching {
                    val bitmap = decodificarReduzido(uri) ?: return@runCatching null
                    val mascara = CatalogoDePinceisDoMotor.mascaraDaImagem(bitmap, inverter = false)
                    bitmap.recycle()
                    CatalogoDePinceisDoMotor.salvarForma(mascara).also { mascara.recycle() }
                }.getOrNull()
                activity.runOnUiThread {
                    if (!dialogo.isShowing) return@runOnUiThread
                    if (nome == null) {
                        Toast.makeText(activity, "Não consegui usar essa imagem.", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }
                    atual = if (paraGranulado) {
                        atual.copy(granulado = com.chromebrothers.navegador.regras.FonteDoGranulado.IMAGEM, arquivoDoGranulado = nome)
                    } else {
                        atual.copy(forma = FormaDoCarimbo.IMAGEM, arquivoDaForma = nome)
                    }
                    atualizarArea()
                    mostrarSecao(if (paraGranulado) Secao.GRANULADO else Secao.FORMA)
                }
            }.start()
        }
    }

    private fun decodificarReduzido(uri: Uri): Bitmap? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        activity.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
        var amostra = 1
        while (maxOf(limites.outWidth, limites.outHeight) / amostra > 1024) amostra *= 2
        val opcoes = BitmapFactory.Options().apply { inSampleSize = amostra }
        return activity.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opcoes) }
    }

    // ---------- Controles ----------

    private fun atualizarArea() {
        area.pincel = atual
    }

    private fun porcento(v: Float) = "${(v * 100).roundToInt()}%"

    private fun titulo(texto: String) {
        controles.addView(TextView(activity).apply {
            text = texto
            setTextColor(TEXTO)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, dp(8f), 0, dp(8f))
        })
    }

    private fun dica(texto: String) {
        controles.addView(TextView(activity).apply {
            text = texto
            setTextColor(TEXTO_SUAVE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(0, dp(6f), 0, dp(10f))
        })
    }

    private fun deslizante(
        rotulo: String,
        minimo: Float,
        maximo: Float,
        inicial: Float,
        formatar: (Float) -> String,
        aplicar: (PincelDoMotor, Float) -> PincelDoMotor,
    ) {
        val valor = TextView(activity).apply {
            text = formatar(inicial)
            setTextColor(Color.parseColor("#AECBFA"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
        val cabeca = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(TextView(activity).apply {
                text = rotulo
                setTextColor(TEXTO)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(valor)
        }
        val barra = SeekBar(activity).apply {
            max = 1000
            progress = (((inicial - minimo) / (maximo - minimo)).coerceIn(0f, 1f) * 1000).roundToInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(b: SeekBar?, p: Int, doUsuario: Boolean) {
                    if (!doUsuario) return
                    val v = minimo + p / 1000f * (maximo - minimo)
                    atual = aplicar(atual, v)
                    valor.text = formatar(v)
                    atualizarArea()
                }
                override fun onStartTrackingTouch(b: SeekBar?) = Unit
                override fun onStopTrackingTouch(b: SeekBar?) = Unit
            })
        }
        controles.addView(cabeca, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10f) })
        controles.addView(barra, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(36f)))
    }

    private fun alternancia(rotulo: String, inicial: Boolean, aplicar: (PincelDoMotor, Boolean) -> PincelDoMotor) {
        controles.addView(Switch(activity).apply {
            text = rotulo
            isChecked = inicial
            setTextColor(TEXTO)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(0, dp(10f), 0, dp(6f))
            setOnCheckedChangeListener { _, marcado ->
                atual = aplicar(atual, marcado)
                atualizarArea()
            }
        })
    }

    private fun botao(texto: String, destaque: Boolean = false, aoTocar: () -> Unit): TextView = TextView(activity).apply {
        text = texto
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

    /**
     * Área de teste: desenha com o pincel em edição (dedo ou caneta, com pressão) e já traz um traço de exemplo.
     * Mudar o pincel redesenha todos os rabiscos, para comparar na hora.
     */
    private class AreaDeTesteDoPincel(context: Context, private val cor: () -> Int) : View(context) {
        var pincel: PincelDoMotor = PincelDoMotor(id = "teste", nome = "teste")
            set(valor) {
                field = valor
                cacheDoPincel = null
                invalidate()
            }
        private val dd = context.resources.displayMetrics.density
        private val renderizador = RenderizadorDeCarimbosDoPincel()
        /** Cada ponto: x, y, pressão (−1 = dedo), tempo em ms desde o início do traço. */
        private val tracos = mutableListOf<List<FloatArray>>()
        private var tracoAtual: MutableList<FloatArray>? = null
        private var inicioDoTracoMs = 0L
        private var cache: Bitmap? = null
        private var cacheDoPincel: PincelDoMotor? = null
        private var tracosNoCache = -1

        init {
            background = GradientDrawable().apply { cornerRadius = 14f * dd; setColor(PAPEL) }
            clipToOutline = true
        }

        fun limpar() {
            tracos.clear()
            cacheDoPincel = null
            invalidate()
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            cache?.recycle()
            cache = if (w > 0 && h > 0) Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888) else null
            cacheDoPincel = null
            if (tracos.isEmpty() && w > 0 && h > 0) {
                // Traço de exemplo: S com pressão subindo e descendo.
                // Velocidade variando: devagar nas pontas, rápido no meio (mostra a dinâmica sem desenhar).
                var tempo = 0f
                tracos += (0..60).map { i ->
                    val t = i / 60f
                    tempo += 34f * (1.15f - kotlin.math.sin(t * Math.PI).toFloat())
                    floatArrayOf(
                        w * (0.12f + 0.76f * t),
                        h * 0.5f + kotlin.math.sin(t * 2 * Math.PI).toFloat() * h * 0.22f,
                        kotlin.math.sin(t * Math.PI).toFloat().coerceAtLeast(0.1f),
                        tempo,
                    )
                }
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            parent?.requestDisallowInterceptTouchEvent(true)
            val caneta = event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS
            if (event.actionMasked == MotionEvent.ACTION_DOWN) inicioDoTracoMs = event.eventTime
            fun ponto(x: Float, y: Float, p: Float, tempo: Long) =
                floatArrayOf(x, y, if (caneta) p.coerceIn(0f, 1f) else -1f, (tempo - inicioDoTracoMs).toFloat())
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> tracoAtual = mutableListOf(ponto(event.x, event.y, event.pressure, event.eventTime))
                MotionEvent.ACTION_MOVE -> tracoAtual?.let { lista ->
                    for (h in 0 until event.historySize) {
                        lista += ponto(event.getHistoricalX(h), event.getHistoricalY(h), event.getHistoricalPressure(h), event.getHistoricalEventTime(h))
                    }
                    lista += ponto(event.x, event.y, event.pressure, event.eventTime)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    tracoAtual?.let { tracos += it }
                    tracoAtual = null
                    while (tracos.size > 14) tracos.removeAt(0)
                }
            }
            invalidate()
            return true
        }

        override fun onDraw(canvas: Canvas) {
            val bitmap = cache ?: return
            if (cacheDoPincel != pincel || tracosNoCache != tracos.size) {
                bitmap.eraseColor(Color.TRANSPARENT)
                val tela = Canvas(bitmap)
                tracos.forEachIndexed { i, t -> desenhar(tela, t, i) }
                cacheDoPincel = pincel
                tracosNoCache = tracos.size
            }
            // Cor clara (ex.: branco) some no papel claro: troca para papel escuro.
            val c = cor()
            if ((Color.red(c) * 299 + Color.green(c) * 587 + Color.blue(c) * 114) / 1000 > 200) {
                canvas.drawColor(Color.parseColor("#2A2F38"))
            }
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            tracoAtual?.let { desenhar(canvas, it, tracos.size) }
        }

        private fun desenhar(tela: Canvas, pontos: List<FloatArray>, indice: Int) {
            val base = (TamanhoDoPincelDoEditorDeImagem.diametroDp(pincel.tamanhoNoControle(0.55f), false) *
                dd * pincel.multiplicadorDeLargura).coerceAtMost(height * 0.45f)
            var velocidade: Float? = null
            // Velocidade pelos pontos crus; posição já com o StreamLine (e o alcance no fim).
            val suaves = com.chromebrothers.navegador.regras.StreamLineDoTraco.suavizar(pontos, pincel.streamline, 0.6f * dd)
            val lista = suaves.mapIndexed { i, p ->
                val pressao = if (p[2] < 0f) null else p[2]
                if (i > 0) {
                    val anterior = suaves[i - 1]
                    val intervalo = p[3] - anterior[3]
                    if (intervalo > 0f) {
                        val bruta = kotlin.math.hypot(p[0] - anterior[0], p[1] - anterior[1]) / dd / intervalo
                        velocidade = MotorDeCarimbosDoPincel.suavizarVelocidade(velocidade, bruta)
                    }
                }
                val v = velocidade ?: 0f
                PontoParaCarimbar(
                    p[0],
                    p[1],
                    base * MotorDeCarimbosDoPincel.fatorDaPressaoNoTamanho(pressao, pincel.pressaoNoTamanho) *
                        MotorDeCarimbosDoPincel.fatorDaVelocidade(v, pincel.velocidadeNoTamanho),
                    MotorDeCarimbosDoPincel.fatorDaPressaoNaOpacidade(pressao, pincel.pressaoNaOpacidade) *
                        MotorDeCarimbosDoPincel.fatorDaVelocidade(v, pincel.velocidadeNaOpacidade),
                )
            }
            val carimbos = MotorDeCarimbosDoPincel.gerar(lista, pincel, 1_000L + indice, comAfinamentoDoFim = true)
            val alpha = (255f * pincel.opacidadeNoControle(1f)).roundToInt().coerceIn(1, 255)
            renderizador.desenharTraco(tela, carimbos, (cor() and 0x00FFFFFF) or (alpha shl 24), pincel)
        }

        private companion object {
            val PAPEL = Color.parseColor("#F4F1EA")
        }
    }

    private companion object {
        val FUNDO = Color.parseColor("#14171D")
        val TEXTO = Color.parseColor("#F4F7FB")
        val TEXTO_SUAVE = Color.parseColor("#9AA0A6")
    }
}

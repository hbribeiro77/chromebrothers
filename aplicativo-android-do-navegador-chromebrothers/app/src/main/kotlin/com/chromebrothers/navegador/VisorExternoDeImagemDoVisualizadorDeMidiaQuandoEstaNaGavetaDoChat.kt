package com.chromebrothers.navegador

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
import com.chromebrothers.navegador.regras.RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao
import com.chromebrothers.navegador.regras.RegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * Imagem aberta por cima da página quando o Visualizador está na gaveta.
 * A gaveta continua visível: o deslize troca a foto e a moldura da grade acompanha.
 * Pinça amplia; toque longo arma o fantasma (soltar no lugar abre a mesma modal da miniatura).
 */
class VisorExternoDeImagemDoVisualizadorDeMidiaQuandoEstaNaGavetaDoChat(
    private val activity: AppCompatActivity,
    private val host: FrameLayout,
    private val leitor: LeituraDeBytesDeMidiaLocalOuRemotaParaOComponenteDaColecao,
    private val aoMudarIndice: (uri: String) -> Unit,
    /** [xNaTela]/[yNaTela] = rawX/rawY do dedo. */
    private val aoToqueLongo: (uri: String, xNaTela: Float, yNaTela: Float) -> Unit,
    private val aoFechar: () -> Unit,
) {
    private val regras = RegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat
    private val folga = ViewConfiguration.get(activity).scaledTouchSlop.toFloat()
    private var overlay: FrameLayout? = null
    private var palco: FrameLayout? = null
    private var anterior: ImageView? = null
    private var atual: ImageView? = null
    private var proxima: ImageView? = null
    private var progresso: ProgressBar? = null
    private var uris: List<String> = emptyList()
    private var indice = 0
    private var fotoAnterior: Foto? = null
    private var fotoAtual: Foto? = null
    private var fotoProxima: Foto? = null
    private var tokenAnterior = 0
    private var tokenAtual = 0
    private var tokenProxima = 0
    private var arrasteX = 0f
    private var zoom = 1f
    private var panX = 0f
    private var panY = 0f
    private var animacao: android.animation.ValueAnimator? = null
    private var sessao = 0

    fun estaAberto(): Boolean = overlay?.parent != null

    fun uriAtual(): String? = if (estaAberto()) uris.getOrNull(indice) else null

    /** Fantasma saiu do lugar: some para mostrar a gaveta e os destinos por baixo. */
    fun esconderDuranteArraste() {
        if (!estaAberto()) return
        overlay?.visibility = View.INVISIBLE
    }

    fun estaEscondidoPeloArraste(): Boolean = estaAberto() && overlay?.visibility == View.INVISIBLE

    fun reaparecerAposArraste() {
        if (!estaAberto() || foraDaAba) return
        overlay?.visibility = View.VISIBLE
    }

    /** O visor mora na aba Visor: some (GONE) quando outra aba está ativa e volta na mesma foto. */
    private var foraDaAba = false

    fun ocultarPorTrocaDeAba(ocultar: Boolean) {
        foraDaAba = ocultar
        if (!estaAberto()) return
        overlay?.visibility = if (ocultar) View.GONE else View.VISIBLE
    }

    fun mostrar(listaDeUris: List<String>, indiceInicial: Int) {
        val limpa = listaDeUris
            .map { InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao.uriSemParametroDeMiniatura(it) }
            .filter { it.isNotBlank() }
        if (limpa.isEmpty()) return
        val antigaAnterior = fotoAnterior
        val antigaAtual = fotoAtual
        val antigaProxima = fotoProxima
        fotoAnterior = null
        fotoAtual = null
        fotoProxima = null
        soltar(antigaAnterior)
        soltar(antigaAtual)
        soltar(antigaProxima)
        tokenAnterior++
        tokenAtual++
        tokenProxima++
        uris = limpa
        indice = indiceInicial.coerceIn(0, limpa.lastIndex)
        arrasteX = 0f
        zoom = 1f
        panX = 0f
        panY = 0f
        if (overlay == null) montar()
        val raiz = overlay ?: return
        if (raiz.parent == null) {
            host.addView(
                raiz,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
        }
        host.bringChildToFront(raiz)
        foraDaAba = false
        raiz.visibility = View.VISIBLE
        aplicarFotos()
        aplicar()
        carregar(indice - 1, Slot.ANTERIOR)
        carregar(indice, Slot.ATUAL)
        carregar(indice + 1, Slot.PROXIMA)
        aoMudarIndice(uris[indice])
    }

    fun fechar(avisar: Boolean = true): Boolean {
        val raiz = overlay ?: return false
        if (raiz.parent == null) return false
        sessao++
        animacao?.cancel()
        tokenAnterior++
        tokenAtual++
        tokenProxima++
        val a = fotoAnterior
        val b = fotoAtual
        val c = fotoProxima
        fotoAnterior = null
        fotoAtual = null
        fotoProxima = null
        soltar(a)
        soltar(b)
        soltar(c)
        (raiz.parent as? ViewGroup)?.removeView(raiz)
        if (avisar) aoFechar()
        return true
    }

    private fun montar() {
        val densidade = activity.resources.displayMetrics.density
        val raiz = FrameLayout(activity).apply {
            setBackgroundColor(Color.parseColor("#F20A0C10"))
            isClickable = true
            elevation = 30f * densidade
        }
        val area = FrameLayout(activity).apply { clipChildren = true }
        val fotoAnteriorView = foto()
        val fotoAtualView = foto()
        val fotoProximaView = foto()
        val barra = ProgressBar(activity).apply {
            isIndeterminate = true
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER,
            )
        }
        val fechar = TextView(activity).apply {
            text = activity.getString(R.string.fechar_imagem_ampliada_da_colecao)
            setTextColor(Color.parseColor("#F4F7FB"))
            textSize = 15f
            val margem = (12 * densidade).toInt()
            setPadding(margem, margem, margem, margem)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END,
            )
            setOnClickListener { fechar() }
        }
        area.addView(fotoAnteriorView)
        area.addView(fotoAtualView)
        area.addView(fotoProximaView)
        area.addView(barra)
        raiz.addView(area, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        raiz.addView(fechar)
        area.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> aplicar() }
        raiz.setOnTouchListener { _, evento -> tratarToque(evento) }
        overlay = raiz
        palco = area
        anterior = fotoAnteriorView
        atual = fotoAtualView
        proxima = fotoProximaView
        progresso = barra
    }

    private fun foto(): ImageView = ImageView(activity).apply {
        scaleType = ImageView.ScaleType.FIT_CENTER
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
    }

    private fun tratarToque(evento: MotionEvent): Boolean {
        detectorDePinca.onTouchEvent(evento)
        when (evento.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                animacao?.cancel()
                x0 = evento.x
                y0 = evento.y
                rawX = evento.rawX
                rawY = evento.rawY
                arrasteNoToque = arrasteX
                panXNoToque = panX
                panYNoToque = panY
                moveu = false
                horizontal = false
                longoDisparou = false
                overlay?.removeCallbacks(disparoLongo)
                if (!regras.estaAmpliada(zoom)) {
                    overlay?.postDelayed(disparoLongo, RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao.ESPERA_DO_TOQUE_LONGO_MS)
                }
                rastreador?.recycle()
                rastreador = VelocityTracker.obtain()
                rastreador?.addMovement(evento)
            }
            MotionEvent.ACTION_POINTER_DOWN -> overlay?.removeCallbacks(disparoLongo)
            MotionEvent.ACTION_MOVE -> {
                rastreador?.addMovement(evento)
                rawX = evento.rawX
                rawY = evento.rawY
                if (evento.pointerCount >= 2 || detectorDePinca.isInProgress) return true
                val dx = evento.x - x0
                val dy = evento.y - y0
                if (!moveu && RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao.moveuAlemDaFolga(dx, dy, folga)) {
                    moveu = true
                    horizontal = abs(dx) > abs(dy)
                    overlay?.removeCallbacks(disparoLongo)
                }
                if (regras.estaAmpliada(zoom)) {
                    val largura = palco?.width?.toFloat() ?: return true
                    val altura = palco?.height?.toFloat() ?: return true
                    panX = regras.panLimitado(panXNoToque + dx, zoom, largura)
                    panY = regras.panLimitado(panYNoToque + dy, zoom, altura)
                    aplicar()
                } else if (horizontal) {
                    val largura = palco?.width?.toFloat() ?: return true
                    arrasteX = regras.deslocamentoComBorda(arrasteNoToque + dx, indice, uris.size, largura)
                    aplicar()
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (evento.pointerCount - 1 == 1) {
                    val restou = if (evento.actionIndex == 0) 1 else 0
                    x0 = evento.getX(restou)
                    y0 = evento.getY(restou)
                    panXNoToque = panX
                    panYNoToque = panY
                    arrasteNoToque = arrasteX
                    moveu = true
                    overlay?.removeCallbacks(disparoLongo)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                overlay?.removeCallbacks(disparoLongo)
                rastreador?.addMovement(evento)
                rastreador?.computeCurrentVelocity(1000)
                val velocidade = rastreador?.xVelocity ?: 0f
                rastreador?.recycle()
                rastreador = null
                if (!longoDisparou && !regras.estaAmpliada(zoom) && evento.actionMasked == MotionEvent.ACTION_UP) {
                    val largura = palco?.width?.toFloat() ?: 0f
                    val troca = regras.trocaAoSoltar(arrasteX, largura, velocidade, zoom)
                    animarAte(regras.indiceAposTroca(indice, uris.size, troca))
                }
            }
        }
        return true
    }

    private var x0 = 0f
    private var y0 = 0f
    private var rawX = 0f
    private var rawY = 0f
    private var arrasteNoToque = 0f
    private var panXNoToque = 0f
    private var panYNoToque = 0f
    private var moveu = false
    private var horizontal = false
    private var longoDisparou = false
    private var rastreador: VelocityTracker? = null
    private val disparoLongo = Runnable {
        val uri = uris.getOrNull(indice) ?: return@Runnable
        if (!regras.deveDispararToqueLongo(0f, 0f, folga, zoom)) return@Runnable
        longoDisparou = true
        arrasteX = 0f
        aplicar()
        overlay?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        aoToqueLongo(uri, rawX, rawY)
    }
    private val detectorDePinca = ScaleGestureDetector(
        activity,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                overlay?.removeCallbacks(disparoLongo)
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                zoom = regras.zoomAposPinca(zoom, detector.scaleFactor)
                if (!regras.estaAmpliada(zoom)) {
                    zoom = 1f
                    panX = 0f
                    panY = 0f
                } else {
                    val largura = palco?.width?.toFloat() ?: return true
                    val altura = palco?.height?.toFloat() ?: return true
                    panX = regras.panLimitado(panX, zoom, largura)
                    panY = regras.panLimitado(panY, zoom, altura)
                }
                aplicar()
                return true
            }
        },
    )

    private fun animarAte(destino: Int) {
        val largura = palco?.width?.toFloat() ?: return
        val alvo = when {
            destino > indice -> -largura
            destino < indice -> largura
            else -> 0f
        }
        val sessaoDesta = sessao
        animacao?.cancel()
        var cancelada = false
        animacao = android.animation.ValueAnimator.ofFloat(arrasteX, alvo).apply {
            duration = 260L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                arrasteX = it.animatedValue as Float
                aplicar()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    cancelada = true
                }

                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (cancelada || sessaoDesta != sessao) return
                    concluirTroca(destino)
                }
            })
            start()
        }
    }

    private fun concluirTroca(destino: Int) {
        if (!estaAberto()) return
        val passo = destino - indice
        if (passo == 0) {
            arrasteX = 0f
            aplicar()
            return
        }
        if (passo > 0) {
            val antiga = fotoAnterior
            fotoAnterior = fotoAtual
            fotoAtual = fotoProxima
            fotoProxima = null
            soltar(antiga)
        } else {
            val antiga = fotoProxima
            fotoProxima = fotoAtual
            fotoAtual = fotoAnterior
            fotoAnterior = null
            soltar(antiga)
        }
        indice = destino
        arrasteX = 0f
        zoom = 1f
        panX = 0f
        panY = 0f
        aplicarFotos()
        aplicar()
        uris.getOrNull(indice)?.let(aoMudarIndice)
        if (fotoAtual?.propria != true) carregar(indice, Slot.ATUAL)
        if (passo > 0) carregar(indice + 1, Slot.PROXIMA) else carregar(indice - 1, Slot.ANTERIOR)
    }

    private enum class Slot { ANTERIOR, ATUAL, PROXIMA }

    private fun carregar(indiceAlvo: Int, slot: Slot) {
        val uri = uris.getOrNull(indiceAlvo)
        val token = when (slot) {
            Slot.ANTERIOR -> ++tokenAnterior
            Slot.ATUAL -> ++tokenAtual
            Slot.PROXIMA -> ++tokenProxima
        }
        if (uri == null) {
            publicar(slot, null)
            return
        }
        val previa = GradeNativaDoVisualizadorDeMidiaNaColecaoDoNavegador.miniaturaPronta(uri)
        if (previa != null) {
            publicar(slot, Foto(previa, propria = false))
        } else if (slot == Slot.ATUAL) {
            progresso?.visibility = View.VISIBLE
        }
        val lado = regras.ladoAlvoDaDecodificacao(
            maxOf(
                activity.resources.displayMetrics.widthPixels,
                activity.resources.displayMetrics.heightPixels,
            ),
        )
        fila.execute {
            val cheia = runCatching {
                val bytes = leitor.ler(uri) ?: return@runCatching null
                decodificar(bytes, lado)
            }.getOrNull()
            activity.runOnUiThread {
                val vigente = when (slot) {
                    Slot.ANTERIOR -> tokenAnterior
                    Slot.ATUAL -> tokenAtual
                    Slot.PROXIMA -> tokenProxima
                }
                if (token != vigente || !estaAberto()) {
                    cheia?.recycle()
                    return@runOnUiThread
                }
                if (cheia != null) {
                    publicar(slot, Foto(cheia, propria = true))
                } else if (slot == Slot.ATUAL) {
                    progresso?.visibility = View.GONE
                }
            }
        }
    }

    private fun publicar(slot: Slot, foto: Foto?) {
        val antiga = fotoDo(slot)
        when (slot) {
            Slot.ANTERIOR -> fotoAnterior = foto
            Slot.ATUAL -> fotoAtual = foto
            Slot.PROXIMA -> fotoProxima = foto
        }
        soltar(antiga)
        aplicarFotos()
    }

    private fun fotoDo(slot: Slot): Foto? = when (slot) {
        Slot.ANTERIOR -> fotoAnterior
        Slot.ATUAL -> fotoAtual
        Slot.PROXIMA -> fotoProxima
    }

    private fun aplicarFotos() {
        anterior?.setImageBitmap(fotoAnterior?.bitmap)
        atual?.setImageBitmap(fotoAtual?.bitmap)
        proxima?.setImageBitmap(fotoProxima?.bitmap)
        progresso?.visibility = if (fotoAtual == null) View.VISIBLE else View.GONE
    }

    private fun aplicar() {
        val largura = palco?.width?.toFloat() ?: return
        if (largura <= 0f) return
        val ampliada = regras.estaAmpliada(zoom)
        anterior?.translationX = -largura + arrasteX
        proxima?.translationX = largura + arrasteX
        atual?.translationX = arrasteX + if (ampliada) panX else 0f
        atual?.translationY = if (ampliada) panY else 0f
        atual?.scaleX = zoom
        atual?.scaleY = zoom
        anterior?.visibility = if (ampliada || indice <= 0) View.INVISIBLE else View.VISIBLE
        proxima?.visibility = if (ampliada || indice >= uris.lastIndex) View.INVISIBLE else View.VISIBLE
    }

    private fun soltar(foto: Foto?) {
        if (foto == null || !foto.propria || foto.bitmap.isRecycled) return
        if (foto.bitmap === fotoAnterior?.bitmap || foto.bitmap === fotoAtual?.bitmap || foto.bitmap === fotoProxima?.bitmap) return
        foto.bitmap.recycle()
    }

    private fun decodificar(bytes: ByteArray, ladoAlvo: Int): Bitmap? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, limites)
        val opcoes = BitmapFactory.Options().apply {
            inSampleSize = regras.amostraParaCaber(limites.outWidth, limites.outHeight, ladoAlvo)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opcoes)
    }

    private class Foto(val bitmap: Bitmap, val propria: Boolean)

    private companion object {
        val fila = Executors.newFixedThreadPool(2)
    }
}

package com.chromebrothers.navegador

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta as Regras

/**
 * Camada (no overlay da faixa página+chat) que desenha a transição no estilo da tela dividida
 * da Samsung: cartão da página, vão escuro com a barrinha e cartão do chat.
 *
 * A página "desfoca até virar cor e foca de volta": a captura antiga passa por níveis de
 * desfoque cada vez mais fortes; no nível máximo troca pela captura do layout novo (as duas
 * ficam praticamente iguais) e a nova ganha foco nível a nível. Só move views do overlay.
 */
class CamadaDaTransicaoEstiloTelaDivididaDaGaveta(
    private val activity: Activity,
    private val faixa: ViewGroup,
) {
    private var raiz: FrameLayout? = null
    private var cartaoDaPagina: FrameLayout? = null
    private var imagemNitida: ImageView? = null
    private var niveisAntigos: List<ImageView> = emptyList()
    private var niveisNovos: List<ImageView> = emptyList()
    private var escurecer: View? = null
    private var cartaoDoChat: FrameLayout? = null
    private var barrinha: View? = null
    private var captura: Bitmap? = null
    private val bitmapsAntigos = mutableListOf<Bitmap>()
    private val bitmapsNovos = mutableListOf<Bitmap>()
    private var larguraReduzida = 1
    private var alturaReduzida = 1
    private var larguraDaFonte = 0f
    private var larguraDoChat = 0
    private var raioAtual = 0f
    private val matrizNitida = Matrix()
    private val matrizAntiga = Matrix()
    private var focoEmAndamento: android.animation.ValueAnimator? = null

    val instalada: Boolean get() = raiz != null

    /**
     * @param captura tela da faixa (pode ser null: cartões em cor sólida).
     * @param larguraDaFonteDaPagina largura, na captura, da parte que é a página.
     */
    fun instalar(captura: Bitmap?, larguraDaFonteDaPagina: Int, larguraDoChat: Int) {
        remover()
        val largura = faixa.width
        val altura = faixa.height
        if (largura <= 0 || altura <= 0) {
            captura?.recycle()
            return
        }
        val densidade = activity.resources.displayMetrics.density
        this.captura = captura
        this.larguraDaFonte = larguraDaFonteDaPagina.toFloat().coerceAtLeast(1f)
        this.larguraDoChat = larguraDoChat
        captura?.let { origem ->
            val reduzida = reduzir(origem)
            if (reduzida != null) {
                larguraReduzida = reduzida.width
                alturaReduzida = reduzida.height
                bitmapsAntigos += niveis(reduzida)
                reduzida.recycle()
            }
        }
        val corDaPagina = ContextCompat.getColor(activity, R.color.fundo_da_pagina_do_navegador_chromebrothers)

        val nitida = imagem(captura)
        val antigos = bitmapsAntigos.map { imagem(it).apply { alpha = 0f } }
        val novos = Regras.RAIOS_DOS_NIVEIS.map { imagem(null).apply { alpha = 0f } }
        val sombra = View(activity).apply {
            setBackgroundColor(Color.BLACK)
            alpha = 0f
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        val pagina = cartao(corDaPagina).apply {
            addView(nitida)
            antigos.forEach { addView(it) }
            // Novos empilhados do mais forte (embaixo) ao mais leve (em cima).
            novos.asReversed().forEach { addView(it) }
            addView(sombra)
        }
        // Sem ícone: o cartão do chat é só a cor do chat (um ícone grande parecia artefato).
        val chat = cartao(Regras.COR_DO_CARTAO_DO_CHAT)
        val larguraDaBarrinha = (Regras.LARGURA_DA_BARRINHA_DP * densidade).toInt().coerceAtLeast(2)
        val alturaDaBarrinha = (Regras.ALTURA_DA_BARRINHA_DP * densidade).toInt().coerceAtLeast(16)
        val barra = View(activity).apply {
            background = GradientDrawable().apply {
                setColor(Color.argb(242, 255, 255, 255))
                cornerRadius = larguraDaBarrinha / 2f
            }
            alpha = 0f
        }
        val camada = FrameLayout(activity).apply {
            setBackgroundColor(Regras.COR_DO_VAO)
            isClickable = false
            isFocusable = false
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            addView(pagina)
            addView(chat)
            addView(barra)
        }
        camada.measure(exato(largura), exato(altura))
        camada.layout(0, 0, largura, altura)
        barra.measure(exato(larguraDaBarrinha), exato(alturaDaBarrinha))
        barra.layout(0, 0, larguraDaBarrinha, alturaDaBarrinha)
        barra.translationY = (altura - alturaDaBarrinha) / 2f
        faixa.overlay.add(camada)
        raiz = camada
        cartaoDaPagina = pagina
        imagemNitida = nitida
        niveisAntigos = antigos
        niveisNovos = novos
        escurecer = sombra
        cartaoDoChat = chat
        barrinha = barra
    }

    fun aplicar(divisao: Float, progresso: Float) {
        val camada = raiz ?: return
        val largura = camada.width
        val altura = camada.height
        val densidade = activity.resources.displayMetrics.density
        val q = Regras.quadro(largura, larguraDoChat, divisao, progresso, densidade)
        raioAtual = q.raioDosCantos

        val pagina = cartaoDaPagina ?: return
        val larguraDaPagina = q.paginaDireita.toInt().coerceAtLeast(0)
        posicionar(pagina, 0, larguraDaPagina, altura)
        val escala = Regras.escalaHorizontal(larguraDaPagina.toFloat(), larguraDaFonte)
        captura?.let { imagemNitida?.imageMatrix = matrizNitida.apply { setScale(escala, 1f) } }
        if (niveisAntigos.isNotEmpty()) {
            // Parte da página na imagem reduzida → largura do cartão.
            val fonteReduzida = (larguraDaFonte * larguraReduzida / larguraDaCaptura()).coerceAtLeast(1f)
            matrizAntiga.setScale(larguraDaPagina / fonteReduzida, altura / alturaReduzida.toFloat().coerceAtLeast(1f))
            val opacidades = Regras.opacidadesDosNiveis(q.desfoque)
            niveisAntigos.forEachIndexed { i, nivel ->
                nivel.imageMatrix = matrizAntiga
                nivel.alpha = opacidades.getOrElse(i) { 1f }
            }
        }
        escurecer?.alpha = q.escurecimento

        val chat = cartaoDoChat ?: return
        val esquerdaDoChat = q.chatEsquerda.toInt().coerceAtMost(largura)
        posicionar(chat, esquerdaDoChat, largura, altura)

        val barra = barrinha ?: return
        barra.translationX = q.divisorX - barra.width / 2f
        barra.alpha = q.barrinha
        pagina.invalidateOutline()
        chat.invalidateOutline()
    }

    /**
     * O site terminou de se reorganizar: no nível de desfoque máximo troca pela captura do
     * layout novo e foca de volta, nível a nível (do mais forte ao mais leve).
     *
     * @param reduzida captura já reduzida da superfície da página (no layout novo).
     * @param larguraReal largura real da superfície, em px.
     * @param dx deslocamento da superfície dentro da faixa, em px.
     */
    fun focarNoLayoutNovo(
        reduzida: Bitmap,
        larguraReal: Int,
        alturaReal: Int,
        dx: Float,
        dy: Float,
        duracaoMs: Long,
        aoFim: () -> Unit,
    ) {
        if (raiz == null || niveisNovos.isEmpty()) {
            reduzida.recycle()
            return aoFim()
        }
        bitmapsNovos.forEach { it.recycle() }
        bitmapsNovos.clear()
        bitmapsNovos += niveis(reduzida)
        val w = reduzida.width
        val h = reduzida.height
        reduzida.recycle()
        // No lugar real da página (já no tamanho final): sem esticar.
        val matriz = Matrix().apply {
            setScale(larguraReal / w.toFloat().coerceAtLeast(1f), alturaReal / h.toFloat().coerceAtLeast(1f))
            postTranslate(dx, dy)
        }
        niveisNovos.forEachIndexed { i, nivel ->
            bitmapsNovos.getOrNull(i)?.let { nivel.setImageBitmap(it) }
            nivel.imageMatrix = matriz
            nivel.alpha = 0f
        }
        val escurecimentoInicial = escurecer?.alpha ?: Regras.ESCURECIMENTO_MAXIMO
        focoEmAndamento?.cancel()
        focoEmAndamento = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
            duration = duracaoMs
            interpolator = PathInterpolator(0.4f, 0f, 0.6f, 1f)
            addUpdateListener { anim ->
                val f = anim.animatedValue as Float
                val opacidades = Regras.opacidadesDoFoco(f)
                niveisNovos.forEachIndexed { i, nivel -> nivel.alpha = opacidades.getOrElse(i) { 1f } }
                escurecer?.alpha = escurecimentoInicial +
                    (Regras.ESCURECIMENTO_NO_FIM_DO_FOCO - escurecimentoInicial) * f
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                private var cancelada = false
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    cancelada = true
                }
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    focoEmAndamento = null
                    if (!cancelada) aoFim()
                }
            })
            start()
        }
    }

    /** Desvanece a camada (revela o conteúdo real, já no lugar) e remove. */
    fun sumir(duracaoMs: Long, aoFim: () -> Unit) {
        val camada = raiz ?: return aoFim()
        camada.animate().cancel()
        camada.animate()
            .alpha(0f)
            .setDuration(duracaoMs)
            .setInterpolator(PathInterpolator(0.2f, 0f, 0f, 1f))
            .withEndAction {
                remover()
                aoFim()
            }
            .start()
    }

    fun remover() {
        focoEmAndamento?.cancel()
        focoEmAndamento = null
        raiz?.animate()?.cancel()
        raiz?.let { faixa.overlay.remove(it) }
        raiz = null
        cartaoDaPagina = null
        imagemNitida = null
        niveisAntigos = emptyList()
        niveisNovos = emptyList()
        escurecer = null
        cartaoDoChat = null
        barrinha = null
        captura?.recycle()
        captura = null
        bitmapsAntigos.forEach { it.recycle() }
        bitmapsAntigos.clear()
        bitmapsNovos.forEach { it.recycle() }
        bitmapsNovos.clear()
    }

    private fun larguraDaCaptura(): Float = (captura?.width ?: larguraReduzida).toFloat().coerceAtLeast(1f)

    private fun reduzir(origem: Bitmap): Bitmap? = runCatching {
        val fator = Regras.FATOR_DE_REDUCAO
        val pequena = Bitmap.createScaledBitmap(
            origem,
            (origem.width / fator).coerceAtLeast(1),
            (origem.height / fator).coerceAtLeast(1),
            true,
        )
        if (pequena === origem) origem.copy(Bitmap.Config.ARGB_8888, true) else pequena
    }.getOrNull()

    /** Um bitmap por raio de [Regras.RAIOS_DOS_NIVEIS], do mais leve ao mais forte. */
    private fun niveis(reduzida: Bitmap): List<Bitmap> {
        val w = reduzida.width
        val h = reduzida.height
        val pixels = IntArray(w * h)
        reduzida.getPixels(pixels, 0, w, 0, 0, w, h)
        return Regras.RAIOS_DOS_NIVEIS.toList().mapNotNull { raio ->
            runCatching {
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                    setPixels(Regras.borrar(pixels, w, h, raio), 0, w, 0, 0, w, h)
                }
            }.getOrNull()
        }
    }

    private fun imagem(bitmap: Bitmap?): ImageView = ImageView(activity).apply {
        scaleType = ImageView.ScaleType.MATRIX
        if (bitmap != null) setImageBitmap(bitmap)
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun cartao(cor: Int): FrameLayout = FrameLayout(activity).apply {
        setBackgroundColor(cor)
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, raioAtual)
            }
        }
        clipToOutline = true
    }

    private fun posicionar(vista: View, esquerda: Int, direita: Int, altura: Int) {
        val largura = (direita - esquerda).coerceAtLeast(0)
        vista.measure(exato(largura), exato(altura))
        vista.layout(esquerda, 0, esquerda + largura, altura)
    }

    private fun exato(tamanho: Int) = View.MeasureSpec.makeMeasureSpec(tamanho.coerceAtLeast(0), View.MeasureSpec.EXACTLY)
}

package com.chromebrothers.navegador

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.chromebrothers.navegador.regras.Carimbo
import com.chromebrothers.navegador.regras.FormaDoCarimbo
import com.chromebrothers.navegador.regras.MotorDeCarimbosDoPincel
import com.chromebrothers.navegador.regras.PincelDoMotor
import kotlin.math.roundToInt

/**
 * Desenha os carimbos do motor de pincéis: a forma (máscara branca com alpha) fica em cache e é
 * tingida com a cor do traço. O traço inteiro vai numa camada limitada pela opacidade escolhida,
 * para os carimbos acumularem pelo fluxo sem passar dessa opacidade.
 */
class RenderizadorDeCarimbosDoPincel {
    private val formas = HashMap<String, Bitmap>()
    private val tinta = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val retangulo = RectF()

    /** Só os carimbos a partir de [desde], direto na tela (traço em andamento, camada própria). */
    fun desenhar(
        tela: Canvas,
        carimbos: List<Carimbo>,
        desde: Int,
        cor: Int,
        pincel: PincelDoMotor,
        ate: Int = carimbos.size,
    ) {
        if (desde >= ate) return
        val forma = forma(pincel)
        tinta.xfermode = null
        tinta.colorFilter = PorterDuffColorFilter(cor or 0xFF000000.toInt(), PorterDuff.Mode.SRC_IN)
        for (i in desde until ate.coerceAtMost(carimbos.size)) {
            val c = carimbos[i]
            if (c.largura < 0.3f) continue
            val alpha = (c.alpha * pincel.fluxo * 255f).roundToInt().coerceIn(0, 255)
            if (alpha == 0) continue
            tinta.alpha = alpha
            tela.save()
            tela.translate(c.x, c.y)
            if (c.rotacaoGraus != 0f) tela.rotate(c.rotacaoGraus)
            retangulo.set(-c.largura / 2f, -c.altura / 2f, c.largura / 2f, c.altura / 2f)
            tela.drawBitmap(formaParaOTamanho(forma, maxOf(c.largura, c.altura)), null, retangulo, tinta)
            tela.restore()
        }
    }

    /**
     * Versões da forma já reduzidas pela metade (256, 128, 64… — "mipmaps"), por forma. Encolher 256 px
     * para um carimbo de 6 px de uma vez, com filtro bilinear, só lê 4 pixels da forma e a borda sai em
     * escadinha; partir do nível mais próximo deixa a borda lisa.
     */
    private val niveisDaForma = HashMap<Int, MutableList<Bitmap>>()

    private fun formaParaOTamanho(forma: Bitmap, tamanhoPx: Float): Bitmap {
        if (tamanhoPx >= forma.width / 2f) return forma
        val niveis = niveisDaForma.getOrPut(System.identityHashCode(forma)) { mutableListOf(forma) }
        if (niveis.first() !== forma || niveis.any { it.isRecycled }) {
            niveis.clear()
            niveis += forma
        }
        var menor = niveis.last()
        while (menor.width / 2f >= tamanhoPx && menor.width > 4 && menor.height > 4) {
            menor = Bitmap.createScaledBitmap(menor, (menor.width / 2).coerceAtLeast(1), (menor.height / 2).coerceAtLeast(1), true)
            niveis += menor
        }
        return niveis.lastOrNull { it.width >= tamanhoPx } ?: forma
    }

    private val apagador = Paint().apply { xfermode = android.graphics.PorterDuffXfermode(PorterDuff.Mode.DST_OUT) }

    /**
     * Borracha do motor: os carimbos formam uma máscara numa camada que, ao fechar, apaga o que está embaixo
     * (DST_OUT) com a opacidade [alpha] (0..255). O traço apaga no máximo essa opacidade, como no pincel.
     */
    fun apagarTraco(tela: Canvas, carimbos: List<Carimbo>, alpha: Int, pincel: PincelDoMotor, escalaDoPapel: Float = 1f) {
        val limites = MotorDeCarimbosDoPincel.limites(carimbos) ?: return
        apagador.alpha = alpha.coerceIn(0, 255)
        val camada = tela.saveLayer(limites[0], limites[1], limites[2], limites[3], apagador)
        desenhar(tela, carimbos, 0, Color.BLACK, pincel)
        aplicarGranuladoNoPapel(tela, limites[0], limites[1], limites[2], limites[3], pincel, escalaDoPapel)
        tela.restoreToCount(camada)
    }

    /**
     * Traço completo: camada do tamanho dos carimbos com a opacidade (alpha) da [cor].
     * [escalaDoPapel]: pixels desta tela por unidade da folha (o granulado fixo fica no mesmo lugar do papel).
     */
    fun desenharTraco(tela: Canvas, carimbos: List<Carimbo>, cor: Int, pincel: PincelDoMotor, escalaDoPapel: Float = 1f) {
        val limites = MotorDeCarimbosDoPincel.limites(carimbos) ?: return
        val camada = tela.saveLayerAlpha(limites[0], limites[1], limites[2], limites[3], (cor ushr 24).coerceIn(0, 255))
        desenhar(tela, carimbos, 0, cor, pincel)
        aplicarGranuladoNoPapel(tela, limites[0], limites[1], limites[2], limites[3], pincel, escalaDoPapel)
        tela.restoreToCount(camada)
    }

    // ---------- Granulado ----------

    private val mascaras = HashMap<String, Bitmap>()
    private val pinturaDoGranulado = Paint(Paint.FILTER_BITMAP_FLAG).apply {
        xfermode = android.graphics.PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    private val matrizDoGranulado = android.graphics.Matrix()

    /**
     * Granulado preso ao papel: dentro da camada do traço (já com os carimbos), mantém a tinta só onde a textura
     * deixa (DST_IN). A textura é ladrilhada nas coordenadas da folha, então repassar revela o mesmo grão.
     */
    fun aplicarGranuladoNoPapel(tela: Canvas, esquerda: Float, topo: Float, direita: Float, base: Float, pincel: PincelDoMotor, escalaDoPapel: Float) {
        if (!pincel.temGranulado || !pincel.granuladoFixoNoPapel) return
        val mascara = mascaraDoGranulado(pincel) ?: return
        val shader = android.graphics.BitmapShader(mascara, android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT)
        val escala = (pincel.escalaDoGranulado.coerceIn(0.1f, 8f) * escalaDoPapel).coerceAtLeast(0.02f)
        matrizDoGranulado.setScale(escala, escala)
        shader.setLocalMatrix(matrizDoGranulado)
        pinturaDoGranulado.shader = shader
        tela.drawRect(esquerda, topo, direita, base, pinturaDoGranulado)
        pinturaDoGranulado.shader = null
    }

    /** Máscara (alpha) do granulado: 1 − intensidade × (1 − luz da textura). Em cache por fonte e intensidade. */
    private fun mascaraDoGranulado(pincel: PincelDoMotor): Bitmap? {
        val intensidade = (pincel.intensidadeDoGranulado.coerceIn(0f, 1f) * 20f).roundToInt()
        val chave = "${pincel.granulado}-${pincel.arquivoDoGranulado}-$intensidade"
        mascaras[chave]?.takeIf { !it.isRecycled }?.let { return it }
        val luz = luzDoGranulado(pincel) ?: return null
        val pixels = IntArray(luz.size) { i ->
            Color.argb((255f * (1f - intensidade / 20f * (1f - luz[i] / 255f))).roundToInt().coerceIn(0, 255), 255, 255, 255)
        }
        val lado = kotlin.math.sqrt(luz.size.toDouble()).toInt()
        return Bitmap.createBitmap(pixels, lado, lado, Bitmap.Config.ARGB_8888).also { mascaras[chave] = it }
    }

    /** Luz 0..255 da textura (quadrada). Papel e tela se emendam sem costura ao ladrilhar. */
    private fun luzDoGranulado(pincel: PincelDoMotor): IntArray? {
        val lado = LADO_DO_GRANULADO
        return when (pincel.granulado) {
            com.chromebrothers.navegador.regras.FonteDoGranulado.NENHUM -> null
            com.chromebrothers.navegador.regras.FonteDoGranulado.PAPEL -> {
                val sorteio = kotlin.random.Random(SEMENTE_DO_PAPEL)
                val bruto = IntArray(lado * lado) { 70 + sorteio.nextInt(186) }
                // Desfoque leve com volta nas bordas: fibras macias e ladrilho sem emenda.
                IntArray(lado * lado) { i ->
                    val x = i % lado
                    val y = i / lado
                    var soma = 0
                    for (dy in -1..1) for (dx in -1..1) soma += bruto[((y + dy + lado) % lado) * lado + (x + dx + lado) % lado]
                    soma / 9
                }
            }
            com.chromebrothers.navegador.regras.FonteDoGranulado.TELA -> IntArray(lado * lado) { i ->
                val x = i % lado
                val y = i / lado
                val fio = kotlin.math.abs(kotlin.math.sin(Math.PI * x / 8.0)) * 0.5 + kotlin.math.abs(kotlin.math.sin(Math.PI * y / 8.0)) * 0.5
                (60 + 195 * fio).toInt().coerceIn(0, 255)
            }
            com.chromebrothers.navegador.regras.FonteDoGranulado.IMAGEM -> {
                val arquivo = CatalogoDePinceisDoMotor.arquivoDaForma(pincel.arquivoDoGranulado) ?: return null
                val imagem = android.graphics.BitmapFactory.decodeFile(arquivo.absolutePath) ?: return null
                val quadrada = Bitmap.createScaledBitmap(imagem, lado, lado, true)
                if (quadrada !== imagem) imagem.recycle()
                val pixels = IntArray(lado * lado)
                quadrada.getPixels(pixels, 0, lado, 0, 0, lado, lado)
                quadrada.recycle()
                // A máscara guardada é branca com alpha = luz.
                IntArray(lado * lado) { Color.alpha(pixels[it]) }
            }
        }
    }

    /** Forma do carimbo; com granulado "acompanha", a textura vem embutida na forma. */
    private fun forma(pincel: PincelDoMotor): Bitmap {
        val base = formaBase(pincel)
        if (!pincel.temGranulado || pincel.granuladoFixoNoPapel) return base
        val intensidade = (pincel.intensidadeDoGranulado.coerceIn(0f, 1f) * 20f).roundToInt()
        val escala = (pincel.escalaDoGranulado.coerceIn(0.1f, 8f) * 10f).roundToInt()
        val chave = "acompanha-${System.identityHashCode(base)}-${pincel.granulado}-${pincel.arquivoDoGranulado}-$intensidade-$escala"
        formas[chave]?.takeIf { !it.isRecycled }?.let { return it }
        val mascara = mascaraDoGranulado(pincel) ?: return base
        val comTextura = base.copy(Bitmap.Config.ARGB_8888, true)
        val shader = android.graphics.BitmapShader(mascara, android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT)
        matrizDoGranulado.setScale(escala / 10f, escala / 10f)
        shader.setLocalMatrix(matrizDoGranulado)
        pinturaDoGranulado.shader = shader
        Canvas(comTextura).drawRect(0f, 0f, comTextura.width.toFloat(), comTextura.height.toFloat(), pinturaDoGranulado)
        pinturaDoGranulado.shader = null
        formas[chave] = comTextura
        return comTextura
    }

    private fun formaBase(pincel: PincelDoMotor): Bitmap {
        if (pincel.forma == FormaDoCarimbo.IMAGEM) {
            val chaveDaImagem = "img-" + pincel.arquivoDaForma
            formas[chaveDaImagem]?.takeIf { !it.isRecycled }?.let { return it }
            val carregada = CatalogoDePinceisDoMotor.arquivoDaForma(pincel.arquivoDaForma)
                ?.let { android.graphics.BitmapFactory.decodeFile(it.absolutePath) }
            // Forma sumiu do disco: desenha redondo em vez de não desenhar nada.
            if (carregada == null) return formaBase(pincel.copy(forma = FormaDoCarimbo.REDONDA))
            formas[chaveDaImagem] = carregada
            return carregada
        }
        val dureza = (pincel.dureza.coerceIn(0f, 1f) * 20f).roundToInt()
        val chave = "${pincel.forma}-$dureza"
        formas[chave]?.takeIf { !it.isRecycled }?.let { return it }
        return gerarForma(pincel.forma, dureza / 20f).also { formas[chave] = it }
    }

    private fun gerarForma(forma: FormaDoCarimbo, dureza: Float): Bitmap {
        val lado = LADO_DA_FORMA
        val bitmap = Bitmap.createBitmap(lado, lado, Bitmap.Config.ARGB_8888)
        val tela = Canvas(bitmap)
        val centro = lado / 2f
        val raio = lado / 2f - 1f
        val pintura = Paint(Paint.ANTI_ALIAS_FLAG)
        when (forma) {
            FormaDoCarimbo.IMAGEM,
            FormaDoCarimbo.REDONDA -> {
                pintura.shader = gradienteRadial(centro, raio, dureza)
                tela.drawCircle(centro, centro, raio, pintura)
            }
            FormaDoCarimbo.QUADRADA -> {
                pintura.color = Color.WHITE
                tela.drawRect(1f, 1f, lado - 1f, lado - 1f, pintura)
            }
            FormaDoCarimbo.RUIDOSA -> {
                // Grãos fixos (semente constante): textura de giz/cerdas, igual em todo traço.
                val sorteio = kotlin.random.Random(SEMENTE_DA_TEXTURA)
                pintura.color = Color.WHITE
                repeat(QUANTIDADE_DE_GRAOS) {
                    val angulo = sorteio.nextFloat() * 2f * Math.PI.toFloat()
                    val distancia = raio * kotlin.math.sqrt(sorteio.nextFloat())
                    val borda = distancia / raio
                    val chance = if (borda < dureza.coerceAtMost(0.95f)) 1f else 1f - (borda - dureza) / (1f - dureza).coerceAtLeast(0.05f)
                    if (sorteio.nextFloat() > chance) return@repeat
                    pintura.alpha = 110 + sorteio.nextInt(146)
                    tela.drawCircle(
                        centro + kotlin.math.cos(angulo) * distancia,
                        centro + kotlin.math.sin(angulo) * distancia,
                        lado * (0.006f + sorteio.nextFloat() * 0.014f),
                        pintura,
                    )
                }
            }
        }
        return bitmap
    }

    private fun gradienteRadial(centro: Float, raio: Float, dureza: Float): Shader {
        val miolo = dureza.coerceIn(0f, 0.97f)
        return RadialGradient(
            centro,
            centro,
            raio,
            intArrayOf(Color.WHITE, Color.WHITE, Color.TRANSPARENT),
            floatArrayOf(0f, miolo, 1f),
            Shader.TileMode.CLAMP,
        )
    }

    private companion object {
        const val LADO_DA_FORMA = 256
        const val LADO_DO_GRANULADO = 256
        const val SEMENTE_DO_PAPEL = 0x9A9E_2026L
        const val SEMENTE_DA_TEXTURA = 0x5EED_C0DEL
        const val QUANTIDADE_DE_GRAOS = 2600
    }
}

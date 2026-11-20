package com.chromebrothers.navegador

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chromebrothers.navegador.regras.PlanilhaDeRolagemParaCapturaDaPaginaInteira
import com.chromebrothers.navegador.regras.RetanguloNormalizadoDaSelecaoDeCaptura
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class CapturaNativaDeTelaDoNavegadorChromebrothers(
    private val activity: AppCompatActivity,
    private val raizDaPagina: FrameLayout,
    private val paginaAtiva: () -> SuperficieDaPaginaNoMotorDoFirefox?,
    private val aoAbrirCapturaEmNovaAba: (String) -> Unit,
) {
    private val principal = Handler(Looper.getMainLooper())
    private var dialogo: Dialog? = null
    private var camadaDeSelecao: CamadaDeSelecaoRetangularDaCapturaNativaDeTela? = null
    private var capturando = false

    fun mostrarOpcoes() {
        if (capturando) {
            avisar(R.string.captura_nativa_de_tela_em_andamento)
            return
        }
        if (paginaAtiva() == null) {
            avisar(R.string.captura_nativa_de_tela_sem_pagina)
            return
        }
        dialogo?.dismiss()
        val caixa = Dialog(activity)
        caixa.requestWindowFeature(Window.FEATURE_NO_TITLE)
        caixa.setContentView(R.layout.layout_caixa_de_opcoes_da_captura_nativa_de_tela_do_navegador)
        caixa.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        caixa.window?.setLayout(
            (activity.resources.displayMetrics.widthPixels * 0.86f).toInt().coerceAtMost(
                (420 * activity.resources.displayMetrics.density).toInt(),
            ),
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        caixa.window?.setGravity(Gravity.CENTER)
        caixa.findViewById<Button>(R.id.botao_captura_da_area_visivel_da_tela).setOnClickListener {
            caixa.dismiss()
            capturarAreaVisivel()
        }
        caixa.findViewById<Button>(R.id.botao_captura_da_area_selecionada_da_tela).setOnClickListener {
            caixa.dismiss()
            iniciarSelecao()
        }
        caixa.findViewById<Button>(R.id.botao_captura_da_pagina_inteira_da_tela).setOnClickListener {
            caixa.dismiss()
            capturarPaginaInteira()
        }
        caixa.findViewById<Button>(R.id.botao_cancelar_captura_nativa_de_tela).setOnClickListener {
            caixa.dismiss()
        }
        dialogo = caixa
        caixa.show()
    }

    fun cancelarSelecaoSeAberta(): Boolean {
        val camada = camadaDeSelecao ?: return false
        raizDaPagina.removeView(camada)
        camadaDeSelecao = null
        return true
    }

    private fun capturarAreaVisivel() {
        val vista = paginaAtiva() ?: return avisar(R.string.captura_nativa_de_tela_sem_pagina)
        capturando = true
        vista.capturarImagemDaPagina { bitmap ->
            capturando = false
            if (bitmap == null) {
                avisar(R.string.captura_nativa_de_tela_falhou)
                return@capturarImagemDaPagina
            }
            concluirComBitmap(bitmap)
        }
    }

    private fun iniciarSelecao() {
        cancelarSelecaoSeAberta()
        val camada = CamadaDeSelecaoRetangularDaCapturaNativaDeTela(
            context = activity,
            aoConfirmar = { area ->
                cancelarSelecaoSeAberta()
                capturarAreaSelecionada(area)
            },
            aoCancelar = {
                cancelarSelecaoSeAberta()
                avisar(R.string.captura_nativa_de_tela_selecao_cancelada)
            },
        )
        camadaDeSelecao = camada
        raizDaPagina.addView(camada)
    }

    private fun capturarAreaSelecionada(area: RetanguloNormalizadoDaSelecaoDeCaptura) {
        val vista = paginaAtiva() ?: return avisar(R.string.captura_nativa_de_tela_sem_pagina)
        capturando = true
        vista.capturarImagemDaPagina { bitmap ->
            capturando = false
            if (bitmap == null || bitmap.width <= 0 || bitmap.height <= 0) {
                avisar(R.string.captura_nativa_de_tela_falhou)
                return@capturarImagemDaPagina
            }
            val escalaX = bitmap.width.toFloat() / vista.width.coerceAtLeast(1)
            val escalaY = bitmap.height.toFloat() / vista.height.coerceAtLeast(1)
            val esquerda = (area.esquerda * escalaX).toInt().coerceIn(0, bitmap.width - 1)
            val topo = (area.topo * escalaY).toInt().coerceIn(0, bitmap.height - 1)
            val largura = (area.largura * escalaX).toInt().coerceAtLeast(1)
                .coerceAtMost(bitmap.width - esquerda)
            val altura = (area.altura * escalaY).toInt().coerceAtLeast(1)
                .coerceAtMost(bitmap.height - topo)
            val recorte = runCatching {
                Bitmap.createBitmap(bitmap, esquerda, topo, largura, altura)
            }.getOrNull()
            if (recorte == null || recorte === bitmap) {
                if (recorte == null && !bitmap.isRecycled) bitmap.recycle()
                avisar(R.string.captura_nativa_de_tela_falhou)
            } else {
                if (!bitmap.isRecycled) bitmap.recycle()
                concluirComBitmap(recorte)
            }
        }
    }

    private fun capturarPaginaInteira() {
        val vista = paginaAtiva() ?: return avisar(R.string.captura_nativa_de_tela_sem_pagina)
        capturando = true
        avisar(R.string.captura_nativa_de_tela_costurando)
        medirPagina(vista) { medida ->
            if (medida == null) {
                capturando = false
                avisar(R.string.captura_nativa_de_tela_falhou)
                return@medirPagina
            }
            val sobreposicao = 80
            val offsets = PlanilhaDeRolagemParaCapturaDaPaginaInteira.offsets(
                alturaDaPagina = medida.alturaDaPagina,
                alturaDaJanela = medida.alturaDaJanela,
                sobreposicao = sobreposicao,
                maximoDeFatias = 40,
            )
            val fatias = mutableListOf<Bitmap>()
            val scrollOriginal = medida.scrollAtual
            capturarFatia(
                vista = vista,
                offsets = offsets,
                indice = 0,
                sobreposicao = sobreposicao,
                alturaDaPagina = medida.alturaDaPagina,
                alturaDaJanela = medida.alturaDaJanela,
                fatias = fatias,
                aoTerminar = { ok ->
                    rolarPara(vista, scrollOriginal) {
                        capturando = false
                        if (!ok || fatias.isEmpty()) {
                            fatias.forEach { it.recycle() }
                            avisar(R.string.captura_nativa_de_tela_falhou)
                            return@rolarPara
                        }
                        val costurada = costurarFatias(
                            fatias = fatias,
                            alturaDaJanelaCss = medida.alturaDaJanela,
                            alturaDaPaginaCss = medida.alturaDaPagina,
                            sobreposicaoCss = sobreposicao,
                        )
                        fatias.forEach { it.recycle() }
                        if (costurada == null) {
                            avisar(R.string.captura_nativa_de_tela_falhou)
                        } else {
                            concluirComBitmap(costurada)
                        }
                    }
                },
            )
        }
    }

    private data class MedidaDaPagina(
        val alturaDaPagina: Int,
        val alturaDaJanela: Int,
        val scrollAtual: Int,
    )

    private fun medirPagina(
        vista: SuperficieDaPaginaNoMotorDoFirefox,
        aoReceber: (MedidaDaPagina?) -> Unit,
    ) {
        val script = """
            (function(){
              var doc = document.documentElement;
              var body = document.body;
              var h = Math.max(
                doc ? doc.scrollHeight : 0,
                body ? body.scrollHeight : 0,
                doc ? doc.offsetHeight : 0,
                body ? body.offsetHeight : 0
              );
              var w = window.innerHeight || (doc ? doc.clientHeight : 0) || 1;
              var s = window.scrollY || window.pageYOffset || 0;
              return JSON.stringify({h:h,w:w,s:s});
            })();
        """.trimIndent()
        vista.evaluateJavascript(script) { bruto ->
            principal.post {
                val texto = textoDoJavascript(bruto)
                val json = runCatching { JSONObject(texto) }.getOrNull()
                if (json == null) {
                    aoReceber(null)
                    return@post
                }
                aoReceber(
                    MedidaDaPagina(
                        alturaDaPagina = json.optInt("h").coerceAtLeast(1),
                        alturaDaJanela = json.optInt("w").coerceAtLeast(1),
                        scrollAtual = json.optInt("s").coerceAtLeast(0),
                    ),
                )
            }
        }
    }

    private fun capturarFatia(
        vista: SuperficieDaPaginaNoMotorDoFirefox,
        offsets: List<Int>,
        indice: Int,
        sobreposicao: Int,
        alturaDaPagina: Int,
        alturaDaJanela: Int,
        fatias: MutableList<Bitmap>,
        aoTerminar: (Boolean) -> Unit,
    ) {
        if (indice >= offsets.size) {
            aoTerminar(true)
            return
        }
        rolarPara(vista, offsets[indice]) {
            principal.postDelayed({
                vista.capturarImagemDaPagina { bitmap ->
                    if (bitmap == null) {
                        aoTerminar(false)
                        return@capturarImagemDaPagina
                    }
                    fatias += bitmap
                    capturarFatia(
                        vista = vista,
                        offsets = offsets,
                        indice = indice + 1,
                        sobreposicao = sobreposicao,
                        alturaDaPagina = alturaDaPagina,
                        alturaDaJanela = alturaDaJanela,
                        fatias = fatias,
                        aoTerminar = aoTerminar,
                    )
                }
            }, 450)
        }
    }

    private fun rolarPara(
        vista: SuperficieDaPaginaNoMotorDoFirefox,
        y: Int,
        aoPronto: () -> Unit,
    ) {
        vista.evaluateJavascript("window.scrollTo(0, $y); true;") {
            principal.postDelayed(aoPronto, 80)
        }
    }

    private fun costurarFatias(
        fatias: List<Bitmap>,
        alturaDaJanelaCss: Int,
        alturaDaPaginaCss: Int,
        sobreposicaoCss: Int,
    ): Bitmap? {
        if (fatias.isEmpty()) return null
        val primeira = fatias.first()
        val escala = primeira.height.toFloat() / alturaDaJanelaCss.coerceAtLeast(1).toFloat()
        val alturaAlvo = (alturaDaPaginaCss * escala).toInt().coerceAtLeast(primeira.height)
        val largura = primeira.width
        val destino = runCatching {
            Bitmap.createBitmap(largura, alturaAlvo, Bitmap.Config.ARGB_8888)
        }.getOrNull() ?: return null
        val canvas = Canvas(destino)
        var yDestino = 0
        fatias.forEachIndexed { indice, fatia ->
            val resto = (alturaAlvo - yDestino).coerceAtLeast(0)
            if (resto <= 0) return@forEachIndexed
            val utilCss = PlanilhaDeRolagemParaCapturaDaPaginaInteira.alturaUtilDaFatia(
                indice = indice,
                alturaDaJanela = alturaDaJanelaCss,
                sobreposicao = sobreposicaoCss,
                restoAteOFundo = ((alturaAlvo - yDestino) / escala).toInt().coerceAtLeast(1),
            )
            val pular = if (indice == 0) 0 else (sobreposicaoCss * escala).toInt().coerceAtLeast(0)
            val copiar = (utilCss * escala).toInt().coerceAtLeast(1)
                .coerceAtMost(fatia.height - pular)
                .coerceAtMost(resto)
            if (copiar <= 0 || pular >= fatia.height) return@forEachIndexed
            val pedaco = Bitmap.createBitmap(fatia, 0, pular, fatia.width.coerceAtMost(largura), copiar)
            canvas.drawBitmap(pedaco, 0f, yDestino.toFloat(), null)
            if (pedaco !== fatia) pedaco.recycle()
            yDestino += copiar
        }
        return destino
    }

    private fun concluirComBitmap(bitmap: Bitmap) {
        val pasta = File(activity.cacheDir, "capturas-nativas-de-tela").apply { mkdirs() }
        val arquivo = File(pasta, "captura-${System.currentTimeMillis()}.png")
        val ok = runCatching {
            FileOutputStream(arquivo).use { saida ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)
            }
        }.isSuccess
        if (!bitmap.isRecycled) bitmap.recycle()
        if (!ok || !arquivo.exists()) {
            avisar(R.string.captura_nativa_de_tela_falhou)
            return
        }
        aoAbrirCapturaEmNovaAba("file://${arquivo.absolutePath}")
        avisar(R.string.captura_nativa_de_tela_aberta_em_nova_aba)
    }

    private fun avisar(mensagem: Int) {
        Toast.makeText(activity, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun textoDoJavascript(bruto: String?): String {
        if (bruto.isNullOrBlank() || bruto == "null") return ""
        return try {
            org.json.JSONArray("[$bruto]").getString(0)
        } catch (_: Exception) {
            bruto.trim().removeSurrounding("\"")
        }
    }
}

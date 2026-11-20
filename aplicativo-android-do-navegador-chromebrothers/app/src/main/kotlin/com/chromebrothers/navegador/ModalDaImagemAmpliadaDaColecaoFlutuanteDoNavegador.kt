package com.chromebrothers.navegador

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chromebrothers.navegador.regras.ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina
import com.chromebrothers.navegador.regras.FonteDoEnderecoDaImagemDoToqueLongo
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ModalDaImagemAmpliadaDaColecaoFlutuanteDoNavegador(
    private val activity: AppCompatActivity,
) {
    private val clienteHttp = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
    private var geracao = 0
    private var dialogoAberto: Dialog? = null

    fun mostrar(endereco: String) {
        dialogoAberto?.dismiss()
        val esta = ++geracao
        val dialogo = Dialog(activity)
        dialogoAberto = dialogo
        val raiz = FrameLayout(activity).apply { setBackgroundColor(Color.BLACK) }
        val foto = ImageView(activity).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
        }
        val aviso = TextView(activity).apply {
            text = activity.getString(R.string.abrindo_imagem_ampliada_da_colecao)
            setTextColor(Color.parseColor("#F4F7FB"))
            textSize = 16f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER,
            )
        }
        val fechar = TextView(activity).apply {
            text = activity.getString(R.string.fechar_imagem_ampliada_da_colecao)
            setTextColor(Color.parseColor("#F4F7FB"))
            textSize = 16f
            val margem = (16 * resources.displayMetrics.density).toInt()
            setPadding(margem, margem, margem, margem)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END,
            )
            setOnClickListener { dialogo.dismiss() }
        }
        prepararPinch(foto)
        raiz.addView(foto)
        raiz.addView(aviso)
        raiz.addView(fechar)
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialogo.setOnDismissListener {
            if (dialogoAberto === dialogo) dialogoAberto = null
            geracao++
        }
        dialogo.show()
        dialogo.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        Thread {
            val bitmap = baixar(endereco)
            activity.runOnUiThread {
                if (activity.isDestroyed || esta != geracao || !dialogo.isShowing) {
                    bitmap?.recycle()
                    return@runOnUiThread
                }
                if (bitmap == null) {
                    Toast.makeText(
                        activity,
                        R.string.imagem_ampliada_da_colecao_indisponivel,
                        Toast.LENGTH_LONG,
                    ).show()
                    dialogo.dismiss()
                } else {
                    aviso.visibility = View.GONE
                    foto.setImageBitmap(bitmap)
                }
            }
        }.start()
    }

    private fun prepararPinch(foto: ImageView) {
        var escala = 1f
        var transX = 0f
        var transY = 0f
        var ultimoX = 0f
        var ultimoY = 0f
        val detector = ScaleGestureDetector(
            activity,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    escala = (escala * detector.scaleFactor).coerceIn(1f, 6f)
                    if (escala <= 1f) {
                        transX = 0f
                        transY = 0f
                    }
                    foto.scaleX = escala
                    foto.scaleY = escala
                    foto.translationX = transX
                    foto.translationY = transY
                    return true
                }
            },
        )
        foto.setOnTouchListener { _, evento ->
            detector.onTouchEvent(evento)
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    ultimoX = evento.rawX
                    ultimoY = evento.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!detector.isInProgress && escala > 1f && evento.pointerCount == 1) {
                        transX += evento.rawX - ultimoX
                        transY += evento.rawY - ultimoY
                        foto.translationX = transX
                        foto.translationY = transY
                    }
                    ultimoX = evento.rawX
                    ultimoY = evento.rawY
                }
            }
            true
        }
    }

    private fun baixar(endereco: String): Bitmap? {
        val bytes = when (ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.classificar(endereco)) {
            FonteDoEnderecoDaImagemDoToqueLongo.ARQUIVO_LOCAL ->
                CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.bytesDeArquivoLocal(endereco)
            FonteDoEnderecoDaImagemDoToqueLongo.HTTP -> try {
                val resposta = clienteHttp.newCall(Request.Builder().url(endereco).build()).execute()
                resposta.use { corpo ->
                    if (!corpo.isSuccessful) null
                    else corpo.body?.bytes()
                }
            } catch (_: Exception) {
                null
            }
            else -> null
        } ?: return null
        return try {
            if (bytes.isEmpty() || bytes.size > LIMITE_DOS_BYTES) return null
            val medida = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, medida)
            var amostra = 1
            while (
                amostra < 32 &&
                (medida.outWidth / amostra > LADO_MAXIMO || medida.outHeight / amostra > LADO_MAXIMO)
            ) {
                amostra *= 2
            }
            val desenho = BitmapFactory.Options().apply { inSampleSize = amostra }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, desenho)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val LADO_MAXIMO = 4096
        private const val LIMITE_DOS_BYTES = 40_000_000
    }
}

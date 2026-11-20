package com.chromebrothers.navegador

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import android.webkit.WebView
import com.chromebrothers.navegador.regras.GeracaoDeArquivoPeloChatDaOpenRouter
import java.io.ByteArrayOutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Grava um PDF com a aparência atual da WebView da coleção (mesmo layout da página).
 */
class GravacaoDePdfDaWebViewDaColecaoFlutuanteNoArmazenamentoDoAplicativo(
    private val contexto: Context,
) {
    fun gravar(
        webView: WebView,
        nomeSugerido: String,
        aoFim: (String) -> Unit,
    ) {
        val base = GeracaoDeArquivoPeloChatDaOpenRouter.nomeDeArquivoSeguro(nomeSugerido)
            .ifBlank { "pagina" }
        val nome = if (base.lowercase().endsWith(".pdf")) base else "$base.pdf"
        try {
            val bytes = montarPdf(webView)
            if (bytes == null || bytes.isEmpty()) {
                aoFim("Não deu para gerar o PDF.")
                return
            }
            val ok = publicar(bytes, nome)
            aoFim(
                if (ok) "PDF salvo em Downloads/ChromeBrothers/$nome."
                else "Não deu para gravar o PDF.",
            )
        } catch (_: Exception) {
            aoFim("Não deu para gerar o PDF.")
        }
    }

    private fun montarPdf(webView: WebView): ByteArray? {
        val larguraVista = max(webView.width, 1)
        val escala = webView.scale.coerceAtLeast(0.01f)
        val alturaConteudo = max((webView.contentHeight * escala).toInt(), webView.height)
        if (alturaConteudo <= 0) return null
        val alturaCaptura = min(alturaConteudo, ALTURA_MAXIMA_CAPTURA)
        val bitmap = Bitmap.createBitmap(larguraVista, alturaCaptura, Bitmap.Config.ARGB_8888)
        val canvasBitmap = Canvas(bitmap)
        webView.draw(canvasBitmap)

        val documento = PdfDocument()
        val paginaLargura = LARGURA_A4
        val paginaAltura = ALTURA_A4
        val escalaPagina = paginaLargura.toFloat() / larguraVista.toFloat()
        val alturaUtil = (alturaCaptura * escalaPagina).toInt().coerceAtLeast(1)
        val totalPaginas = ceil(alturaUtil / paginaAltura.toFloat()).toInt().coerceAtLeast(1)
        for (indice in 0 until totalPaginas) {
            val info = PdfDocument.PageInfo.Builder(paginaLargura, paginaAltura, indice + 1).create()
            val pagina = documento.startPage(info)
            val canvas = pagina.canvas
            canvas.save()
            canvas.translate(0f, (-indice * paginaAltura).toFloat())
            canvas.scale(escalaPagina, escalaPagina)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            canvas.restore()
            documento.finishPage(pagina)
        }
        bitmap.recycle()
        val saida = ByteArrayOutputStream()
        documento.writeTo(saida)
        documento.close()
        return saida.toByteArray()
    }

    private fun publicar(bytes: ByteArray, nome: String): Boolean {
        if (bytes.isEmpty() || bytes.size > LIMITE) return false
        val valores = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, nome)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ChromeBrothers")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val colecao = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = contexto.contentResolver.insert(colecao, valores) ?: return false
        return try {
            contexto.contentResolver.openOutputStream(uri)?.use { saida ->
                saida.write(bytes)
            } ?: error("sem saida")
            valores.clear()
            valores.put(MediaStore.Downloads.IS_PENDING, 0)
            contexto.contentResolver.update(uri, valores, null, null)
            true
        } catch (_: Exception) {
            contexto.contentResolver.delete(uri, null, null)
            false
        }
    }

    private companion object {
        const val LARGURA_A4 = 595
        const val ALTURA_A4 = 842
        const val ALTURA_MAXIMA_CAPTURA = 16000
        const val LIMITE = 80L * 1024L * 1024L
    }
}

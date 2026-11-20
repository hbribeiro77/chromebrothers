package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.chromebrothers.navegador.regras.ParteDeConteudoDoChat
import com.chromebrothers.navegador.regras.PreparacaoDaCapturaDaAbaParaOChat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

data class CapturaDaAbaPreparadaParaOChat(
    val parte: ParteDeConteudoDoChat,
    val enderecoArquivo: String,
    val largura: Int,
    val altura: Int,
)

object CompactacaoDoBitmapDaCapturaDaAbaParaOChatDoNavegador {
    fun preparar(contexto: Context, bitmap: Bitmap): CapturaDaAbaPreparadaParaOChat? {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return null
        val origem = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return null
        } else {
            bitmap
        }
        val (largura, altura) = PreparacaoDaCapturaDaAbaParaOChat.medidasReduzidas(
            origem.width,
            origem.height,
        )
        val reduzido = if (largura != origem.width || altura != origem.height) {
            Bitmap.createScaledBitmap(origem, largura, altura, true)
        } else {
            origem
        }
        val saida = ByteArrayOutputStream()
        val ok = reduzido.compress(
            Bitmap.CompressFormat.JPEG,
            PreparacaoDaCapturaDaAbaParaOChat.QUALIDADE_JPEG,
            saida,
        )
        if (reduzido !== origem) reduzido.recycle()
        if (origem !== bitmap) origem.recycle()
        if (!ok || saida.size() <= 0) return null
        val bytes = saida.toByteArray()
        val pasta = File(contexto.cacheDir, PASTA).apply { mkdirs() }
        val arquivo = File(pasta, "captura-da-aba-${System.currentTimeMillis()}.jpg")
        val gravou = runCatching {
            FileOutputStream(arquivo).use { fluxo -> fluxo.write(bytes) }
        }.isSuccess
        if (!gravou || !arquivo.exists()) return null
        return CapturaDaAbaPreparadaParaOChat(
            parte = ParteDeConteudoDoChat(
                tipo = "imagem",
                mime = "image/jpeg",
                nome = arquivo.name,
                dados = Base64.encodeToString(bytes, Base64.NO_WRAP),
            ),
            enderecoArquivo = "file://${arquivo.absolutePath}",
            largura = largura,
            altura = altura,
        )
    }

    /** Mantido para compatibilidade de testes/chamadas que só precisam da parte multimodal. */
    fun paraParte(bitmap: Bitmap): ParteDeConteudoDoChat? {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return null
        val origem = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return null
        } else {
            bitmap
        }
        val (largura, altura) = PreparacaoDaCapturaDaAbaParaOChat.medidasReduzidas(
            origem.width,
            origem.height,
        )
        val reduzido = if (largura != origem.width || altura != origem.height) {
            Bitmap.createScaledBitmap(origem, largura, altura, true)
        } else {
            origem
        }
        val saida = ByteArrayOutputStream()
        val ok = reduzido.compress(
            Bitmap.CompressFormat.JPEG,
            PreparacaoDaCapturaDaAbaParaOChat.QUALIDADE_JPEG,
            saida,
        )
        if (reduzido !== origem) reduzido.recycle()
        if (origem !== bitmap) origem.recycle()
        if (!ok || saida.size() <= 0) return null
        return ParteDeConteudoDoChat(
            tipo = "imagem",
            mime = "image/jpeg",
            nome = "captura-da-aba.jpg",
            dados = Base64.encodeToString(saida.toByteArray(), Base64.NO_WRAP),
        )
    }

    const val PASTA = "capturas-do-chat-da-aba"
}

package com.chromebrothers.navegador

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.View
import android.text.TextPaint
import com.chromebrothers.navegador.regras.EstiloDoTrechoDoMarkdownDoChat
import com.chromebrothers.navegador.regras.RenderizacaoDoMarkdownDoChat

object TextoFormatadoDoMarkdownDoChat {
    fun montar(markdown: String, aoAbrir: (String) -> Unit): CharSequence {
        val builder = SpannableStringBuilder()
        RenderizacaoDoMarkdownDoChat.renderizar(markdown).forEach { trecho ->
            val inicio = builder.length
            builder.append(trecho.texto)
            val fim = builder.length
            if (inicio == fim) return@forEach
            when (trecho.estilo) {
                EstiloDoTrechoDoMarkdownDoChat.NEGRITO,
                EstiloDoTrechoDoMarkdownDoChat.TITULO,
                -> builder.setSpan(StyleSpan(Typeface.BOLD), inicio, fim, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                EstiloDoTrechoDoMarkdownDoChat.CODIGO -> {
                    builder.setSpan(TypefaceSpan("monospace"), inicio, fim, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(
                        BackgroundColorSpan(Color.parseColor("#12141A")),
                        inicio,
                        fim,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )
                }
                EstiloDoTrechoDoMarkdownDoChat.NORMAL -> Unit
            }
            if (trecho.estilo == EstiloDoTrechoDoMarkdownDoChat.TITULO) {
                builder.setSpan(RelativeSizeSpan(1.15f), inicio, fim, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (trecho.endereco.isNotEmpty()) {
                val endereco = trecho.endereco
                builder.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            aoAbrir(endereco)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            ds.color = Color.parseColor("#8AB4F8")
                            ds.isUnderlineText = true
                        }
                    },
                    inicio,
                    fim,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        }
        return builder
    }
}

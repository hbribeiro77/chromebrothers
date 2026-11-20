package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.Button

object AparenciaDosBotoesDeVelocidadeDoVideo {
    fun aplicar(context: Context, botao: Button, selecionado: Boolean) {
        val densidade = context.resources.displayMetrics.density
        val fundo = GradientDrawable().apply {
            cornerRadius = 20f * densidade
            setColor(if (selecionado) Color.parseColor("#2F6FED") else Color.parseColor("#2A2D34"))
        }
        botao.background = fundo
        botao.setTextColor(Color.WHITE)
        botao.isAllCaps = false
        botao.textSize = 14f
        val horizontal = (14 * densidade).toInt()
        val vertical = (8 * densidade).toInt()
        botao.setPadding(horizontal, vertical, horizontal, vertical)
        botao.minWidth = 0
        botao.minimumWidth = 0
    }
}

package com.chromebrothers.navegador

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Lista de opções no visual do app (a mesma caixa escura): navegador e player nativo usam. */
object CaixaDeEscolhaDoNavegador {
    fun <T> mostrar(
        activity: Activity,
        titulo: String,
        opcoes: List<Pair<String, T>>,
        indiceSelecionado: Int = -1,
        aoEscolher: (T) -> Unit,
    ) {
        if (activity.isFinishing || activity.isDestroyed) return
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(R.layout.layout_caixa_de_escolha_simples_do_navegador_chromebrothers, null)
        raiz.findViewById<TextView>(R.id.titulo_da_caixa_de_escolha_simples_do_navegador).text = titulo
        val lista = raiz.findViewById<LinearLayout>(R.id.lista_de_opcoes_da_caixa_de_escolha_simples_do_navegador)
        val densidade = activity.resources.displayMetrics.density
        opcoes.forEachIndexed { indice, (rotulo, valor) ->
            val botao = Button(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (48f * densidade).toInt(),
                )
                backgroundTintList = null
                stateListAnimator = null
                setBackgroundColor(Color.TRANSPARENT)
                gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
                setPadding((8f * densidade).toInt(), 0, (8f * densidade).toInt(), 0)
                isAllCaps = false
                textSize = 16f
                text = if (indice == indiceSelecionado) "✓  $rotulo" else rotulo
                setTextColor(if (indice == indiceSelecionado) Color.parseColor("#8AB4F8") else Color.parseColor("#F4F7FB"))
                setOnClickListener {
                    dialogo.dismiss()
                    aoEscolher(valor)
                }
            }
            lista.addView(botao)
        }
        val cancelar = raiz.findViewById<Button>(R.id.botao_cancelar_da_caixa_de_escolha_simples_do_navegador)
        cancelar.backgroundTintList = null
        cancelar.stateListAnimator = null
        cancelar.setOnClickListener { dialogo.dismiss() }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        dialogo.window?.setLayout((420 * densidade).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    }
}

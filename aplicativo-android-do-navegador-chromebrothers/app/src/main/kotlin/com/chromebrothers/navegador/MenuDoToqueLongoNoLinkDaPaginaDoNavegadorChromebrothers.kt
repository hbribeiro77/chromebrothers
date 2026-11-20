package com.chromebrothers.navegador

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MenuDoToqueLongoNoLinkDaPaginaDoNavegadorChromebrothers(
    private val activity: AppCompatActivity,
    private val aoAbrirEmNovaAba: (String) -> Unit,
    private val aoAbrirEmNovaJanela: (String) -> Unit,
    private val aoCopiar: (String) -> Unit,
) {
    fun mostrar(endereco: String) {
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(
            R.layout.layout_caixa_de_opcoes_do_link_tocado_na_pagina_do_navegador,
            null,
        )
        raiz.findViewById<TextView>(R.id.endereco_da_caixa_de_opcoes_do_link_da_pagina).text = endereco
        listOf(
            R.id.botao_abrir_link_em_nova_aba,
            R.id.botao_abrir_link_em_nova_janela,
            R.id.botao_copiar_endereco_do_link,
            R.id.botao_cancelar_opcoes_do_link_da_pagina,
        ).forEach { id ->
            raiz.findViewById<Button>(id).apply {
                backgroundTintList = null
                stateListAnimator = null
            }
        }
        fun escolher(acao: () -> Unit) {
            dialogo.dismiss()
            acao()
        }
        raiz.findViewById<View>(R.id.botao_abrir_link_em_nova_aba)
            .setOnClickListener { escolher { aoAbrirEmNovaAba(endereco) } }
        raiz.findViewById<View>(R.id.botao_abrir_link_em_nova_janela)
            .setOnClickListener { escolher { aoAbrirEmNovaJanela(endereco) } }
        raiz.findViewById<View>(R.id.botao_copiar_endereco_do_link)
            .setOnClickListener { escolher { aoCopiar(endereco) } }
        raiz.findViewById<View>(R.id.botao_cancelar_opcoes_do_link_da_pagina)
            .setOnClickListener { dialogo.dismiss() }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }
}

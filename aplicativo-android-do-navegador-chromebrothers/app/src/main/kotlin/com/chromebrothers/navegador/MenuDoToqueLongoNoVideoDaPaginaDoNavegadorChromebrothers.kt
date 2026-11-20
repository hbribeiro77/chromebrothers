package com.chromebrothers.navegador

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuDoToqueLongoNoVideoDaPaginaDoNavegadorChromebrothers(
    private val activity: AppCompatActivity,
    private val aoSalvarQuadro: () -> Unit,
    private val aoColocarNaColecao: (String) -> Unit,
) {
    fun mostrar(endereco: String) {
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(
            R.layout.layout_caixa_de_opcoes_do_video_tocado_na_pagina_do_navegador,
            null,
        )
        listOf(
            R.id.botao_salvar_quadro_do_video_no_toque_longo,
            R.id.botao_colocar_video_na_colecao_no_toque_longo,
            R.id.botao_cancelar_opcoes_do_video_da_pagina,
        ).forEach { id ->
            raiz.findViewById<Button>(id).apply {
                backgroundTintList = null
                stateListAnimator = null
            }
        }
        val colecao = raiz.findViewById<View>(R.id.botao_colocar_video_na_colecao_no_toque_longo)
        colecao.visibility = View.GONE
        fun escolher(acao: () -> Unit) {
            dialogo.dismiss()
            acao()
        }
        raiz.findViewById<View>(R.id.botao_salvar_quadro_do_video_no_toque_longo)
            .setOnClickListener { escolher(aoSalvarQuadro) }
        colecao.setOnClickListener { escolher { aoColocarNaColecao(endereco) } }
        raiz.findViewById<View>(R.id.botao_cancelar_opcoes_do_video_da_pagina)
            .setOnClickListener { dialogo.dismiss() }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }
}

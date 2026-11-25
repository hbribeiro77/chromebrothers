package com.chromebrothers.navegador

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Caixa escura do app para o que a página pede (alert, confirm, select, senha, permissão).
 * Toda saída avisa uma única vez: confirmar, extra ou cancelar (botão, voltar, toque fora, fechar()).
 */
class CaixaDoPedidoDaPagina(
    private val activity: Activity,
    titulo: String,
    mensagem: String?,
    conteudo: View? = null,
    textoConfirmar: String? = "OK",
    textoCancelar: String? = "Cancelar",
    textoExtra: String? = null,
    fecharAoTocarFora: Boolean = true,
    aoExtra: () -> Unit = {},
    aoConfirmar: () -> Unit = {},
    aoCancelar: () -> Unit,
) {
    private val dialogo = Dialog(activity)
    private var respondido = false

    init {
        val densidade = activity.resources.displayMetrics.density
        val raiz = activity.layoutInflater
            .inflate(R.layout.layout_caixa_de_dialogo_do_editor_de_imagem, null) as LinearLayout
        raiz.findViewById<TextView>(R.id.titulo_da_caixa_de_dialogo_do_editor).apply {
            text = titulo
            maxLines = 3
        }
        raiz.findViewById<TextView>(R.id.mensagem_da_caixa_de_dialogo_do_editor).apply {
            visibility = if (mensagem.isNullOrBlank()) View.GONE else View.VISIBLE
            text = mensagem
            maxHeight = activity.resources.displayMetrics.heightPixels * 2 / 5
            movementMethod = ScrollingMovementMethod()
        }
        val campo = raiz.findViewById<EditText>(R.id.campo_da_caixa_de_dialogo_do_editor)
        campo.visibility = View.GONE
        if (conteudo != null) {
            val parametros = conteudo.layoutParams as? LinearLayout.LayoutParams
                ?: LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            parametros.topMargin = (12 * densidade).toInt()
            raiz.addView(conteudo, raiz.indexOfChild(campo), parametros)
        }
        val cancelar = raiz.findViewById<Button>(R.id.botao_cancelar_da_caixa_de_dialogo_do_editor)
        val confirmar = raiz.findViewById<Button>(R.id.botao_confirmar_da_caixa_de_dialogo_do_editor)
        preparar(cancelar, textoCancelar) { fechar() }
        preparar(confirmar, textoConfirmar) { responder(aoConfirmar) }
        confirmar.setTextColor(Color.parseColor("#F5C518"))
        if (textoExtra != null) {
            val extra = Button(activity).apply {
                setBackgroundColor(Color.TRANSPARENT)
                isAllCaps = false
                textSize = 15f
                setTextColor(CaixaDeConfirmacaoDoNavegador.COR_DE_EXCLUIR)
            }
            preparar(extra, textoExtra) { responder(aoExtra) }
            (cancelar.parent as LinearLayout).addView(extra, 0)
        }
        dialogo.setContentView(raiz)
        dialogo.setCanceledOnTouchOutside(fecharAoTocarFora)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.setOnDismissListener {
            if (!respondido) {
                respondido = true
                aoCancelar()
            }
        }
    }

    private fun preparar(botao: Button, texto: String?, aoTocar: () -> Unit) {
        botao.visibility = if (texto == null) View.GONE else View.VISIBLE
        botao.text = texto
        botao.backgroundTintList = null
        botao.stateListAnimator = null
        botao.setOnClickListener { aoTocar() }
    }

    /** Responde por fora dos botões (ex.: toque num item da lista). */
    fun responder(acao: () -> Unit) {
        if (respondido) return
        respondido = true
        acao()
        fechar()
    }

    fun mostrar() {
        dialogo.show()
        val medidas = activity.resources.displayMetrics
        val largura = minOf((460 * medidas.density).toInt(), medidas.widthPixels - (32 * medidas.density).toInt())
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogo.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    fun fechar() {
        runCatching { dialogo.dismiss() }
    }
}

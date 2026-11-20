package com.chromebrothers.navegador

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

/**
 * Modal de confirmação no visual do app (a mesma caixa escura do editor e do chat), para ações que
 * não têm volta, como excluir mídia.
 */
object CaixaDeConfirmacaoDoNavegador {
    /** Vermelho das ações destrutivas (o mesmo do descartar edição / apagar conversa). */
    val COR_DE_EXCLUIR: Int = Color.parseColor("#F28B82")

    fun mostrar(
        activity: Activity,
        titulo: String,
        mensagem: String,
        textoConfirmar: String,
        corConfirmar: Int = COR_DE_EXCLUIR,
        aoConfirmar: () -> Unit,
    ) {
        if (activity.isFinishing || activity.isDestroyed) return
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(R.layout.layout_caixa_de_dialogo_do_editor_de_imagem, null)
        raiz.findViewById<TextView>(R.id.titulo_da_caixa_de_dialogo_do_editor).text = titulo
        raiz.findViewById<TextView>(R.id.mensagem_da_caixa_de_dialogo_do_editor).apply {
            visibility = View.VISIBLE
            text = mensagem
        }
        raiz.findViewById<EditText>(R.id.campo_da_caixa_de_dialogo_do_editor).visibility = View.GONE
        val cancelar = raiz.findViewById<Button>(R.id.botao_cancelar_da_caixa_de_dialogo_do_editor)
        val confirmar = raiz.findViewById<Button>(R.id.botao_confirmar_da_caixa_de_dialogo_do_editor)
        cancelar.text = activity.getString(R.string.cancelar_caixa_do_editor)
        cancelar.backgroundTintList = null
        cancelar.stateListAnimator = null
        confirmar.text = textoConfirmar
        confirmar.setTextColor(corConfirmar)
        confirmar.backgroundTintList = null
        confirmar.stateListAnimator = null
        cancelar.setOnClickListener { dialogo.dismiss() }
        confirmar.setOnClickListener {
            dialogo.dismiss()
            aoConfirmar()
        }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    /** Texto padrão para excluir [quantidade] mídias (com o tamanho, quando conhecido). */
    fun mensagemDeExcluir(quantidade: Int, tamanhoTexto: String? = null): String {
        val oQue = if (quantidade == 1) "Este arquivo será apagado do aparelho" else "$quantidade arquivos serão apagados do aparelho"
        val tamanho = tamanhoTexto?.let { " ($it)" }.orEmpty()
        return "$oQue$tamanho. Não dá para desfazer."
    }

    fun tituloDeExcluir(quantidade: Int): String = if (quantidade == 1) "Excluir arquivo?" else "Excluir $quantidade arquivos?"
}

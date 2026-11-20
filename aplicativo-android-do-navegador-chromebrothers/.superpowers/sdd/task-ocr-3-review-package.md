# Task 3 after fixes
## Orquestrador
```
package com.chromebrothers.navegador

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chromebrothers.navegador.regras.ExtracaoDeTextoDeImagemViaIaDoNavegador
import com.chromebrothers.navegador.regras.MensagemDeChat

class OrquestradorDaExtracaoDeTextoDeImagemViaIaDoNavegador(
    private val activity: AppCompatActivity,
    private val cofre: CofreDaChaveDaOpenRouter,
    private val lerModelo: () -> String,
    private val aoAbrirChatComTexto: (String) -> Unit,
) {
    // Cliente próprio: não cancela um envio em andamento do painel do chat.
    private val cliente = ClienteDeChatDaOpenRouter()
    private var emAndamento = false

    // Token de geração: callbacks de uma extração cancelada/antiga são ignorados.
    private var geracao = 0

    private val modal: ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador = ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador(
        activity = activity,
        aoCopiar = { texto -> copiarParaAreaDeTransferencia(texto) },
        aoEnviarAoChat = { texto ->
            modal.fechar()
            aoAbrirChatComTexto(texto)
        },
        aoDispensar = { cancelarExtracaoEmAndamento() },
    )

    private fun cancelarExtracaoEmAndamento() {
        if (!emAndamento) return
        geracao++
        emAndamento = false
        cliente.cancelar()
    }

    fun extrairDeBytes(bytes: ByteArray) {
        if (emAndamento) return
        val bitmap = if (bytes.isEmpty()) null else BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (bitmap == null) {
            mostrarFalhaImediata()
            return
        }
        try {
            extrairDeBitmap(bitmap)
        } finally {
            // paraParte lê o bitmap de forma síncrona e não guarda referência; a cópia é nossa.
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    /** O chamador continua dono do [bitmap]; ele não é reciclado aqui. */
    fun extrairDeBitmap(bitmap: Bitmap) {
        if (emAndamento) return
        val parte = CompactacaoDoBitmapDaCapturaDaAbaParaOChatDoNavegador.paraParte(bitmap)
        if (parte == null) {
            mostrarFalhaImediata()
            return
        }
        val chave = cofre.ler()
        if (chave.isEmpty()) {
            mostrarFalhaImediata()
            return
        }
        val mensagem = MensagemDeChat(
            papel = "user",
            conteudo = ExtracaoDeTextoDeImagemViaIaDoNavegador.promptDoUsuario(),
            partes = listOf(parte),
        )
        val minhaGeracao = ++geracao
        emAndamento = true
        modal.mostrarCarregando()
        // aoTrecho entrega o texto ACUMULADO a cada chamada: substitui, não concatena.
        var acumulado = ""
        cliente.enviar(
            chave = chave,
            modelo = lerModelo(),
            mensagens = listOf(mensagem),
            comFerramentas = false,
            aoTrecho = { parcial -> if (minhaGeracao == geracao) acumulado = parcial },
            aoTerminar = { fim ->
                // Cancelada (modal dispensado) ou substituída: não reabrir o modal.
                if (minhaGeracao != geracao) return@enviar
                emAndamento = false
                if (fim.erro != null) {
                    modal.mostrarErro(ExtracaoDeTextoDeImagemViaIaDoNavegador.mensagemDeFalha(fim.erro))
                } else {
                    modal.mostrarResultado(
                        ExtracaoDeTextoDeImagemViaIaDoNavegador.textoParaExibir(acumulado),
                    )
                }
            },
        )
    }

    private fun mostrarFalhaImediata() {
        modal.mostrarErro(ExtracaoDeTextoDeImagemViaIaDoNavegador.FALHA_GENERICA)
    }

    private fun copiarParaAreaDeTransferencia(texto: String) {
        val gerenciador = activity.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        gerenciador.setPrimaryClip(ClipData.newPlainText("texto extraído da imagem", texto))
        Toast.makeText(activity, R.string.texto_extraido_copiado, Toast.LENGTH_SHORT).show()
    }
}

```
## Modal (aoDispensar)
```
package com.chromebrothers.navegador

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chromebrothers.navegador.regras.ExtracaoDeTextoDeImagemViaIaDoNavegador

class RolagemComTetoDe320DpDoTextoExtraidoDaImagemDoNavegador(
    context: Context,
    attrs: AttributeSet?,
) : ScrollView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val teto = (320 * resources.displayMetrics.density).toInt()
        val pedido = MeasureSpec.getSize(heightMeasureSpec)
        val modo = MeasureSpec.getMode(heightMeasureSpec)
        val limite = if (modo == MeasureSpec.UNSPECIFIED) teto else minOf(pedido, teto)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(limite, MeasureSpec.AT_MOST))
    }
}

class ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador(
    private val activity: AppCompatActivity,
    private val aoCopiar: (String) -> Unit,
    private val aoEnviarAoChat: (String) -> Unit,
    /** Chamado quando o diálogo é fechado/dispensado (botão, toque fora, voltar ou [fechar]). */
    private val aoDispensar: (() -> Unit)? = null,
) {
    private var dialogo: Dialog? = null
    private var vistas: VistasDoModal? = null
    private var textoAtual: String = ""
    private var erroEmExibicao: Boolean = false

    fun mostrarCarregando() {
        val caixa = garantirCaixa() ?: return
        textoAtual = ""
        erroEmExibicao = false
        caixa.faixaDeCarregamento.visibility = View.VISIBLE
        caixa.status.setText(R.string.extraindo_texto_da_imagem)
        caixa.rolagem.visibility = View.GONE
        caixa.texto.text = ""
        aplicarBotoesDeAcao(caixa, visiveis = false, texto = "", forcarDesabilitados = false)
        exibir(caixa.dialogo)
    }

    fun mostrarResultado(texto: String) {
        apresentarTexto(texto, erro = false)
    }

    fun mostrarErro(mensagem: String) {
        apresentarTexto(mensagem, erro = true)
    }

    fun fechar() {
        dialogo?.dismiss()
    }

    private fun apresentarTexto(texto: String, erro: Boolean) {
        val caixa = garantirCaixa() ?: return
        textoAtual = texto
        erroEmExibicao = erro
        caixa.faixaDeCarregamento.visibility = View.GONE
        caixa.rolagem.visibility = View.VISIBLE
        caixa.texto.text = texto
        caixa.rolagem.scrollTo(0, 0)
        aplicarBotoesDeAcao(caixa, visiveis = true, texto = texto, forcarDesabilitados = erro)
        exibir(caixa.dialogo)
    }

    private fun aplicarBotoesDeAcao(
        caixa: VistasDoModal,
        visiveis: Boolean,
        texto: String,
        forcarDesabilitados: Boolean,
    ) {
        val habilitados = visiveis &&
            !forcarDesabilitados &&
            ExtracaoDeTextoDeImagemViaIaDoNavegador.botoesDeAcaoHabilitados(texto)
        listOf(caixa.copiar, caixa.enviar).forEach { botao ->
            botao.visibility = if (visiveis) View.VISIBLE else View.GONE
            botao.isEnabled = habilitados
            botao.alpha = if (habilitados) 1f else 0.4f
        }
    }

    private fun garantirCaixa(): VistasDoModal? {
        if (activity.isFinishing || activity.isDestroyed) return null
        val atual = vistas
        if (atual != null && dialogo?.isShowing == true) return atual
        val criado = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(
            R.layout.layout_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador,
            null,
        )
        val copiar = raiz.findViewById<Button>(R.id.botao_copiar_texto_extraido_da_imagem_via_ia_do_navegador)
        val enviar = raiz.findViewById<Button>(R.id.botao_enviar_texto_extraido_ao_chat_do_navegador)
        val fechar = raiz.findViewById<Button>(R.id.botao_fechar_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador)
        listOf(copiar, enviar, fechar).forEach { botao ->
            botao.backgroundTintList = null
            botao.stateListAnimator = null
        }
        copiar.setOnClickListener { acionarSeHabilitado(aoCopiar) }
        enviar.setOnClickListener { acionarSeHabilitado(aoEnviarAoChat) }
        fechar.setOnClickListener { this.fechar() }
        criado.setContentView(raiz)
        criado.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        criado.setOnDismissListener {
            if (dialogo === criado) {
                dialogo = null
                vistas = null
                textoAtual = ""
                erroEmExibicao = false
                aoDispensar?.invoke()
            }
        }
        val caixa = VistasDoModal(
            dialogo = criado,
            faixaDeCarregamento = raiz.findViewById(R.id.faixa_de_carregamento_do_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador),
            status = raiz.findViewById(R.id.status_do_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador),
            rolagem = raiz.findViewById(R.id.rolagem_do_texto_extraido_da_imagem_via_ia_do_navegador),
            texto = raiz.findViewById(R.id.texto_extraido_da_imagem_via_ia_do_navegador),
            copiar = copiar,
            enviar = enviar,
        )
        dialogo = criado
        vistas = caixa
        return caixa
    }

    private fun acionarSeHabilitado(acao: (String) -> Unit) {
        if (erroEmExibicao) return
        val texto = textoAtual
        if (!ExtracaoDeTextoDeImagemViaIaDoNavegador.botoesDeAcaoHabilitados(texto)) return
        acao(texto)
    }

    private fun exibir(dialogo: Dialog) {
        if (activity.isFinishing || activity.isDestroyed) return
        if (!dialogo.isShowing) dialogo.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private class VistasDoModal(
        val dialogo: Dialog,
        val faixaDeCarregamento: View,
        val status: TextView,
        val rolagem: View,
        val texto: TextView,
        val copiar: Button,
        val enviar: Button,
    )
}

```

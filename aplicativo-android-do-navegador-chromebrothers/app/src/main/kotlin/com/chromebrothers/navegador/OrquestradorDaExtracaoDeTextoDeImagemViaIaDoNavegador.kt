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
        val bitmap = try {
            if (bytes.isEmpty()) null else BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
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
        val parte = try {
            CompactacaoDoBitmapDaCapturaDaAbaParaOChatDoNavegador.paraParte(bitmap)
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
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

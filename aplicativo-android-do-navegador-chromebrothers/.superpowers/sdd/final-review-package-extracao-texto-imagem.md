# Final review package — Extração de texto via IA
Spec: docs/superpowers/specs/2026-11-03-extracao-de-texto-de-imagem-via-ia-com-modal-selecionavel-design.md
Plan: docs/superpowers/plans/2026-11-03-extracao-de-texto-de-imagem-via-ia-com-modal-selecionavel.md
Minors backlog: Task1 does-not-support heuristic; Task2 short-height/title; Task3 main-thread compact; adb smoke pending

## regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\ExtracaoDeTextoDeImagemViaIaDoNavegador.kt
```
package com.chromebrothers.navegador.regras

object ExtracaoDeTextoDeImagemViaIaDoNavegador {
    const val SEM_TEXTO = "Não encontrei texto nesta imagem."
    const val FALHA_GENERICA = "Não deu para extrair o texto. Tente de novo."
    const val FALHA_MODELO_SEM_VISAO =
        "Este modelo não lê imagem. Troque o modelo no chat e tente de novo."

    fun promptDoUsuario(): String =
        "Extraia todo o texto visível nesta imagem. Devolva somente o texto literal, " +
            "sem comentários, sem markdown e sem introdução."

    fun limparResposta(bruto: String): String {
        var t = bruto.trim()
        if (!t.startsWith("```")) return t
        t = t.removePrefix("```")
        val fimDaLinhaDaCerca = t.indexOf('\n')
        if (fimDaLinhaDaCerca >= 0) {
            t = t.substring(fimDaLinhaDaCerca + 1)
        }
        t = t.trimEnd()
        if (t.endsWith("```")) t = t.removeSuffix("```")
        return t.trim()
    }

    fun textoParaExibir(bruto: String): String {
        val limpo = limparResposta(bruto)
        return limpo.ifBlank { SEM_TEXTO }
    }

    fun botoesDeAcaoHabilitados(textoExibido: String): Boolean =
        textoExibido.isNotBlank() && textoExibido != SEM_TEXTO

    fun mensagemDeFalha(erroApi: String?): String {
        val e = erroApi.orEmpty().lowercase()
        val semVisao =
            e.contains("vision") ||
                e.contains("multimodal") ||
                e.contains("does not support") ||
                e.contains("não suport") ||
                e.contains("image_url")
        if (e.isNotBlank() && semVisao) return FALHA_MODELO_SEM_VISAO
        return FALHA_GENERICA
    }

    fun mostrarAcao(temChave: Boolean): Boolean = temChave
}

```

## regras-de-video-do-navegador\src\test\kotlin\com\chromebrothers\navegador\regras\TesteDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt
```
package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaExtracaoDeTextoDeImagemViaIaDoNavegador {
    @Test
    fun promptPedeSoTextoLiteral() {
        val p = ExtracaoDeTextoDeImagemViaIaDoNavegador.promptDoUsuario()
        assertTrue(p.contains("texto"))
        assertTrue(p.lowercase().contains("sem coment"))
    }

    @Test
    fun limparRespostaRemoveCercasMarkdown() {
        assertEquals(
            "ABC-123",
            ExtracaoDeTextoDeImagemViaIaDoNavegador.limparResposta("```\nABC-123\n```"),
        )
    }

    @Test
    fun limparRespostaMantemTodasAsLinhasQuandoCercaNaoTemIdioma() {
        assertEquals(
            "ABC-123\nRua das Flores",
            ExtracaoDeTextoDeImagemViaIaDoNavegador.limparResposta(
                "```\nABC-123\nRua das Flores\n```",
            ),
        )
    }

    @Test
    fun cercaVaziaComIdiomaViraSemTexto() {
        assertEquals(
            "",
            ExtracaoDeTextoDeImagemViaIaDoNavegador.limparResposta("```text\n```"),
        )
        assertEquals(
            ExtracaoDeTextoDeImagemViaIaDoNavegador.SEM_TEXTO,
            ExtracaoDeTextoDeImagemViaIaDoNavegador.textoParaExibir("```text\n```"),
        )
    }

    @Test
    fun mensagemDeFalhaNullBrancoOuSemVisaoUsaGenerica() {
        val regra = ExtracaoDeTextoDeImagemViaIaDoNavegador
        assertEquals(regra.FALHA_GENERICA, regra.mensagemDeFalha(null))
        assertEquals(regra.FALHA_GENERICA, regra.mensagemDeFalha("   "))
        val erroComum = "timeout ao conectar no provedor"
        assertEquals(regra.FALHA_GENERICA, regra.mensagemDeFalha(erroComum))
        val erroQueSoMencionaImagem = "failed to download image: 413 payload too large"
        assertEquals(regra.FALHA_GENERICA, regra.mensagemDeFalha(erroQueSoMencionaImagem))
    }

    @Test
    fun mensagemDeFalhaVisionOuMultimodalUsaModeloSemVisao() {
        val regra = ExtracaoDeTextoDeImagemViaIaDoNavegador
        assertEquals(
            regra.FALHA_MODELO_SEM_VISAO,
            regra.mensagemDeFalha("model does not support vision"),
        )
        assertEquals(
            regra.FALHA_MODELO_SEM_VISAO,
            regra.mensagemDeFalha("This model is not multimodal"),
        )
        assertEquals(
            regra.FALHA_MODELO_SEM_VISAO,
            regra.mensagemDeFalha("o modelo não suporta image_url"),
        )
    }

    @Test
    fun textoParaExibirQuandoVazioUsaMensagemPadrao() {
        assertEquals(
            ExtracaoDeTextoDeImagemViaIaDoNavegador.SEM_TEXTO,
            ExtracaoDeTextoDeImagemViaIaDoNavegador.textoParaExibir("   "),
        )
    }

    @Test
    fun textoParaExibirQuandoHaConteudoDevolveLimpo() {
        assertEquals(
            "olá",
            ExtracaoDeTextoDeImagemViaIaDoNavegador.textoParaExibir("  olá  "),
        )
    }

    @Test
    fun mostrarAcaoSoComChave() {
        assertFalse(ExtracaoDeTextoDeImagemViaIaDoNavegador.mostrarAcao(false))
        assertTrue(ExtracaoDeTextoDeImagemViaIaDoNavegador.mostrarAcao(true))
    }

    @Test
    fun botoesDeAcaoSoComTextoUtil() {
        assertFalse(
            ExtracaoDeTextoDeImagemViaIaDoNavegador.botoesDeAcaoHabilitados(
                ExtracaoDeTextoDeImagemViaIaDoNavegador.SEM_TEXTO,
            ),
        )
        assertTrue(ExtracaoDeTextoDeImagemViaIaDoNavegador.botoesDeAcaoHabilitados("placa 123"))
    }
}

```

## app\src\main\kotlin\com\chromebrothers\navegador\ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt
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

## app\src\main\kotlin\com\chromebrothers\navegador\OrquestradorDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt
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

## app\src\main\res\layout\layout_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador.xml
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@drawable/fundo_arredondado_da_caixa_de_dialogo_do_editor_de_imagem"
    android:orientation="vertical"
    android:paddingStart="16dp"
    android:paddingTop="20dp"
    android:paddingEnd="16dp"
    android:paddingBottom="12dp">

    <TextView
        android:id="@+id/titulo_do_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:text="@string/titulo_da_extracao_de_texto_da_imagem"
        android:textColor="#F4F7FB"
        android:textSize="20sp"
        android:textStyle="bold" />

    <LinearLayout
        android:id="@+id/faixa_de_carregamento_do_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:gravity="center_vertical"
        android:orientation="horizontal"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:visibility="gone">

        <ProgressBar
            android:id="@+id/progresso_do_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador"
            style="?android:attr/progressBarStyle"
            android:layout_width="28dp"
            android:layout_height="28dp"
            android:indeterminate="true"
            android:indeterminateTint="#8AB4F8" />

        <TextView
            android:id="@+id/status_do_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:layout_weight="1"
            android:text="@string/extraindo_texto_da_imagem"
            android:textColor="#C4C7CE"
            android:textSize="15sp" />
    </LinearLayout>

    <com.chromebrothers.navegador.RolagemComTetoDe320DpDoTextoExtraidoDaImagemDoNavegador
        android:id="@+id/rolagem_do_texto_extraido_da_imagem_via_ia_do_navegador"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:fadeScrollbars="false"
        android:overScrollMode="ifContentScrolls"
        android:scrollbars="vertical"
        android:visibility="gone">

        <TextView
            android:id="@+id/texto_extraido_da_imagem_via_ia_do_navegador"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingStart="8dp"
            android:paddingEnd="8dp"
            android:textColor="#F4F7FB"
            android:textIsSelectable="true"
            android:textSize="16sp" />
    </com.chromebrothers.navegador.RolagemComTetoDe320DpDoTextoExtraidoDaImagemDoNavegador>

    <Button
        android:id="@+id/botao_copiar_texto_extraido_da_imagem_via_ia_do_navegador"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:layout_marginTop="12dp"
        android:background="@android:color/transparent"
        android:gravity="start|center_vertical"
        android:minWidth="0dp"
        android:minHeight="48dp"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:stateListAnimator="@null"
        android:text="@string/copiar_texto_extraido_da_imagem"
        android:textAllCaps="false"
        android:textColor="#F4F7FB"
        android:textSize="16sp"
        android:visibility="gone" />

    <Button
        android:id="@+id/botao_enviar_texto_extraido_ao_chat_do_navegador"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:background="@android:color/transparent"
        android:gravity="start|center_vertical"
        android:minWidth="0dp"
        android:minHeight="48dp"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:stateListAnimator="@null"
        android:text="@string/enviar_texto_extraido_ao_chat"
        android:textAllCaps="false"
        android:textColor="#F4F7FB"
        android:textSize="16sp"
        android:visibility="gone" />

    <Button
        android:id="@+id/botao_fechar_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador"
        android:layout_width="wrap_content"
        android:layout_height="40dp"
        android:layout_gravity="end"
        android:layout_marginTop="8dp"
        android:background="@android:color/transparent"
        android:minWidth="0dp"
        android:minHeight="0dp"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:stateListAnimator="@null"
        android:text="@string/fechar_modal_da_extracao_de_texto_da_imagem"
        android:textAllCaps="false"
        android:textColor="#8A9099"
        android:textSize="15sp" />
</LinearLayout>

```

## app\src\main\res\layout\layout_caixa_de_opcoes_da_imagem_tocada_na_pagina_do_navegador.xml
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@drawable/fundo_arredondado_da_caixa_de_dialogo_do_editor_de_imagem"
    android:orientation="vertical"
    android:paddingStart="16dp"
    android:paddingTop="20dp"
    android:paddingEnd="16dp"
    android:paddingBottom="12dp">

    <TextView
        android:id="@+id/titulo_da_caixa_de_opcoes_da_imagem_da_pagina"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:text="@string/titulo_da_imagem_da_pagina"
        android:textColor="#F4F7FB"
        android:textSize="20sp"
        android:textStyle="bold" />

    <Button
        android:id="@+id/botao_copiar_imagem_da_pagina_no_toque_longo"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:layout_marginTop="12dp"
        android:background="@android:color/transparent"
        android:gravity="start|center_vertical"
        android:minWidth="0dp"
        android:minHeight="48dp"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:stateListAnimator="@null"
        android:text="@string/copiar_imagem_da_pagina"
        android:textAllCaps="false"
        android:textColor="#F4F7FB"
        android:textSize="16sp" />

    <Button
        android:id="@+id/botao_salvar_imagem_da_pagina_no_toque_longo"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:background="@android:color/transparent"
        android:gravity="start|center_vertical"
        android:minWidth="0dp"
        android:minHeight="48dp"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:stateListAnimator="@null"
        android:text="@string/salvar_imagem_da_pagina"
        android:textAllCaps="false"
        android:textColor="#F4F7FB"
        android:textSize="16sp" />

    <Button
        android:id="@+id/botao_editar_imagem_da_pagina_no_toque_longo"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:background="@android:color/transparent"
        android:gravity="start|center_vertical"
        android:minWidth="0dp"
        android:minHeight="48dp"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:stateListAnimator="@null"
        android:text="@string/editar_imagem_da_pagina"
        android:textAllCaps="false"
        android:textColor="#F4F7FB"
        android:textSize="16sp" />

    <Button
        android:id="@+id/botao_colocar_imagem_na_colecao_no_toque_longo"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:background="@android:color/transparent"
        android:gravity="start|center_vertical"
        android:minWidth="0dp"
        android:minHeight="48dp"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:stateListAnimator="@null"
        android:text="@string/colocar_na_colecao_pelo_toque_longo"
        android:textAllCaps="false"
        android:textColor="#F4F7FB"
        android:textSize="16sp"
        android:visibility="gone" />

    <Button
        android:id="@+id/botao_acrescentar_imagem_a_ultima_edicao"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:background="@android:color/transparent"
        android:gravity="start|center_vertical"
        android:minWidth="0dp"
        android:minHeight="48dp"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:stateListAnimator="@null"
        android:text="@string/acrescentar_imagem_a_ultima_edicao"
        android:textAllCaps="false"
        android:textColor="#F4F7FB"
        android:textSize="16sp" />

    <Button
        android:id="@+id/botao_extrair_texto_da_imagem_da_pagina_no_toque_longo"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:background="@android:color/transparent"
        android:gravity="start|center_vertical"
        android:minWidth="0dp"
        android:minHeight="48dp"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:stateListAnimator="@null"
        android:text="@string/extrair_texto_da_imagem"
        android:textAllCaps="false"
        android:textColor="#F4F7FB"
        android:textSize="16sp"
        android:visibility="gone" />

    <Button
        android:id="@+id/botao_cancelar_opcoes_da_imagem_da_pagina"
        android:layout_width="wrap_content"
        android:layout_height="40dp"
        android:layout_gravity="end"
        android:layout_marginTop="8dp"
        android:background="@android:color/transparent"
        android:minWidth="0dp"
        android:minHeight="0dp"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:stateListAnimator="@null"
        android:text="@string/cancelar_caixa_do_editor"
        android:textAllCaps="false"
        android:textColor="#8A9099"
        android:textSize="15sp" />
</LinearLayout>

```

## app\src\main\res\layout\layout_caixa_de_opcoes_do_toque_longo_na_imagem_ampliada_do_visor_da_biblioteca.xml
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@drawable/fundo_arredondado_da_caixa_de_dialogo_do_editor_de_imagem"
    android:orientation="vertical"
    android:paddingStart="16dp"
    android:paddingTop="20dp"
    android:paddingEnd="16dp"
    android:paddingBottom="12dp">

    <TextView
        android:id="@+id/titulo_das_opcoes_do_toque_longo_na_imagem_ampliada"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:text="@string/titulo_da_imagem_da_pagina"
        android:textColor="#F4F7FB"
        android:textSize="20sp"
        android:textStyle="bold" />

    <Button
        android:id="@+id/botao_copiar_imagem_ampliada_pelo_toque_longo"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:layout_marginTop="12dp"
        android:background="@android:color/transparent"
        android:gravity="start|center_vertical"
        android:minWidth="0dp"
        android:minHeight="48dp"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:stateListAnimator="@null"
        android:text="@string/copiar_esta_imagem"
        android:textAllCaps="false"
        android:textColor="#F4F7FB"
        android:textSize="16sp" />

    <Button
        android:id="@+id/botao_extrair_texto_da_imagem_ampliada_pelo_toque_longo"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:background="@android:color/transparent"
        android:gravity="start|center_vertical"
        android:minWidth="0dp"
        android:minHeight="48dp"
        android:paddingStart="8dp"
        android:paddingEnd="8dp"
        android:stateListAnimator="@null"
        android:text="@string/extrair_texto_da_imagem"
        android:textAllCaps="false"
        android:textColor="#F4F7FB"
        android:textSize="16sp"
        android:visibility="gone" />

    <Button
        android:id="@+id/botao_cancelar_opcoes_do_toque_longo_na_imagem_ampliada"
        android:layout_width="wrap_content"
        android:layout_height="40dp"
        android:layout_gravity="end"
        android:layout_marginTop="8dp"
        android:background="@android:color/transparent"
        android:minWidth="0dp"
        android:minHeight="0dp"
        android:paddingStart="16dp"
        android:paddingEnd="16dp"
        android:stateListAnimator="@null"
        android:text="@string/cancelar_caixa_do_editor"
        android:textAllCaps="false"
        android:textColor="#8A9099"
        android:textSize="15sp" />
</LinearLayout>

```

## app\src\main\kotlin\com\chromebrothers\navegador\CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.kt
```
package com.chromebrothers.navegador

import android.app.Dialog
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Base64
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.chromebrothers.navegador.regras.ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina
import com.chromebrothers.navegador.regras.ExtracaoDeTextoDeImagemViaIaDoNavegador
import com.chromebrothers.navegador.regras.FonteDoEnderecoDaImagemDoToqueLongo
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

enum class DestinoDaImagemDaPagina { COPIAR, SALVAR, EDITAR, ACRESCENTAR, EXTRAIR_TEXTO }

class CopiaDeImagemDaPaginaParaABibliotecaDoNavegador(
    private val activity: AppCompatActivity,
    private val aoReceber: (ByteArray, DestinoDaImagemDaPagina) -> Unit,
    private val temChave: () -> Boolean,
    private val aoColocarNaColecao: (String) -> Unit,
) {
    fun perguntarPorEndereco(vista: SuperficieDaPaginaNoMotorDoFirefox, endereco: String) {
        perguntar(vista, endereco)
    }

    private fun perguntar(vista: SuperficieDaPaginaNoMotorDoFirefox, endereco: String) {
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(
            R.layout.layout_caixa_de_opcoes_da_imagem_tocada_na_pagina_do_navegador,
            null,
        )
        fun escolher(destino: DestinoDaImagemDaPagina) {
            dialogo.dismiss()
            baixar(vista, endereco, destino)
        }
        listOf(
            R.id.botao_copiar_imagem_da_pagina_no_toque_longo,
            R.id.botao_salvar_imagem_da_pagina_no_toque_longo,
            R.id.botao_editar_imagem_da_pagina_no_toque_longo,
            R.id.botao_colocar_imagem_na_colecao_no_toque_longo,
            R.id.botao_acrescentar_imagem_a_ultima_edicao,
            R.id.botao_extrair_texto_da_imagem_da_pagina_no_toque_longo,
            R.id.botao_cancelar_opcoes_da_imagem_da_pagina,
        ).forEach { id ->
            raiz.findViewById<Button>(id).apply {
                backgroundTintList = null
                stateListAnimator = null
            }
        }
        raiz.findViewById<View>(R.id.botao_copiar_imagem_da_pagina_no_toque_longo)
            .setOnClickListener { escolher(DestinoDaImagemDaPagina.COPIAR) }
        raiz.findViewById<View>(R.id.botao_salvar_imagem_da_pagina_no_toque_longo)
            .setOnClickListener { escolher(DestinoDaImagemDaPagina.SALVAR) }
        raiz.findViewById<View>(R.id.botao_editar_imagem_da_pagina_no_toque_longo)
            .setOnClickListener { escolher(DestinoDaImagemDaPagina.EDITAR) }
        raiz.findViewById<View>(R.id.botao_colocar_imagem_na_colecao_no_toque_longo)
            .setOnClickListener {
                dialogo.dismiss()
                aoColocarNaColecao(endereco)
            }
        raiz.findViewById<View>(R.id.botao_acrescentar_imagem_a_ultima_edicao)
            .setOnClickListener { escolher(DestinoDaImagemDaPagina.ACRESCENTAR) }
        raiz.findViewById<View>(R.id.botao_extrair_texto_da_imagem_da_pagina_no_toque_longo).apply {
            visibility = if (ExtracaoDeTextoDeImagemViaIaDoNavegador.mostrarAcao(temChave())) View.VISIBLE else View.GONE
            setOnClickListener { escolher(DestinoDaImagemDaPagina.EXTRAIR_TEXTO) }
        }
        raiz.findViewById<View>(R.id.botao_cancelar_opcoes_da_imagem_da_pagina)
            .setOnClickListener { dialogo.dismiss() }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun baixar(vista: SuperficieDaPaginaNoMotorDoFirefox, endereco: String, destino: DestinoDaImagemDaPagina) {
        when (ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.classificar(endereco)) {
            FonteDoEnderecoDaImagemDoToqueLongo.DATA -> {
                val bytes = bytesDeDataUrl(endereco)
                if (bytes == null || !ehImagem(bytes)) falhar() else aoReceber(bytes, destino)
            }
            FonteDoEnderecoDaImagemDoToqueLongo.BLOB -> pedirPelaPagina(vista, endereco, destino)
            FonteDoEnderecoDaImagemDoToqueLongo.ARQUIVO_LOCAL -> {
                Thread {
                    val bytes = bytesDeArquivoLocal(endereco)?.takeIf { ehImagem(it) }
                    activity.runOnUiThread {
                        if (activity.isFinishing) return@runOnUiThread
                        if (bytes != null) aoReceber(bytes, destino) else falhar()
                    }
                }.start()
            }
            FonteDoEnderecoDaImagemDoToqueLongo.HTTP -> {
                val agente = vista.settings.userAgentString.orEmpty()
                val pagina = vista.url
                Thread {
                    val bytes = baixarPelaRede(endereco, agente, pagina, null)?.takeIf { ehImagem(it) }
                    activity.runOnUiThread {
                        if (activity.isFinishing) return@runOnUiThread
                        if (bytes != null) aoReceber(bytes, destino)
                        else pedirPelaPagina(vista, endereco, destino)
                    }
                }.start()
            }
            FonteDoEnderecoDaImagemDoToqueLongo.INSUPORTADO -> falhar()
        }
    }

    private fun pedirPelaPagina(vista: SuperficieDaPaginaNoMotorDoFirefox, endereco: String, destino: DestinoDaImagemDaPagina) {
        val citado = JSONObject.quote(endereco)
        val script = """
            (function(){
              var url = $citado;
              function desenhar(img) {
                try {
                  var w = img.naturalWidth || img.width;
                  var h = img.naturalHeight || img.height;
                  if (!w || !h) return "";
                  var tela = document.createElement("canvas");
                  tela.width = w;
                  tela.height = h;
                  tela.getContext("2d").drawImage(img, 0, 0);
                  return tela.toDataURL("image/png");
                } catch (e) { return ""; }
              }
              var lista = document.querySelectorAll("img");
              for (var i = 0; i < lista.length; i++) {
                var img = lista[i];
                if (img.currentSrc === url || img.src === url) {
                  var pronto = desenhar(img);
                  if (pronto) return pronto;
                }
              }
              return "";
            })()
        """.trimIndent()
        vista.evaluateJavascript(script) { bruto ->
            if (activity.isFinishing) return@evaluateJavascript
            val texto = textoDoJavascript(bruto)
            val bytes = texto?.let { bytesDeDataUrl(it) }
            if (bytes != null && ehImagem(bytes)) aoReceber(bytes, destino) else falhar()
        }
    }

    private fun falhar() {
        android.widget.Toast.makeText(
            activity,
            R.string.imagem_da_pagina_indisponivel,
            android.widget.Toast.LENGTH_LONG,
        ).show()
    }

    companion object {
        private const val LIMITE_DE_BYTES = 20 * 1024 * 1024

        fun bytesDeDataUrl(data: String): ByteArray? {
            val virgula = data.indexOf(',')
            if (!data.startsWith("data:") || virgula < 0) return null
            val cabeca = data.substring(0, virgula)
            val corpo = data.substring(virgula + 1)
            return try {
                if (cabeca.contains(";base64", ignoreCase = true)) {
                    Base64.decode(corpo, Base64.DEFAULT)
                } else {
                    java.net.URLDecoder.decode(corpo, "UTF-8").toByteArray(Charsets.UTF_8)
                }
            } catch (_: Exception) {
                null
            }
        }

        fun bytesDeArquivoLocal(endereco: String): ByteArray? {
            val caminho = ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.caminhoDoArquivoLocal(endereco)
                ?: return null
            val arquivo = File(caminho)
            if (!arquivo.isFile) return null
            if (arquivo.length() <= 0L || arquivo.length() > LIMITE_DE_BYTES) return null
            return runCatching { arquivo.readBytes() }.getOrNull()?.takeIf { it.isNotEmpty() }
        }

        fun mimeDaImagem(bytes: ByteArray): String {
            if (bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) return "image/jpeg"
            if (bytes.size >= 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte()) return "image/png"
            if (bytes.size >= 6 && bytes[0] == 'G'.code.toByte() && bytes[1] == 'I'.code.toByte()) return "image/gif"
            if (bytes.size >= 12 && bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte()) return "image/webp"
            return "image/jpeg"
        }

        private fun ehImagem(bytes: ByteArray): Boolean {
            if (bytes.isEmpty()) return false
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return false
            bitmap.recycle()
            return true
        }

        private fun baixarPelaRede(
            endereco: String,
            agente: String,
            pagina: String?,
            cookie: String?,
        ): ByteArray? {
            val conexao = runCatching { URL(endereco).openConnection() as HttpURLConnection }.getOrNull()
                ?: return null
            return try {
                conexao.instanceFollowRedirects = true
                conexao.connectTimeout = 15000
                conexao.readTimeout = 20000
                if (agente.isNotBlank()) conexao.setRequestProperty("User-Agent", agente)
                if (!pagina.isNullOrBlank()) conexao.setRequestProperty("Referer", pagina)
                if (!cookie.isNullOrBlank()) conexao.setRequestProperty("Cookie", cookie)
                conexao.setRequestProperty("Accept", "image/avif,image/webp,image/*,*/*;q=0.8")
                conexao.connect()
                if (conexao.responseCode !in 200..299) return null
                val anunciado = conexao.contentLengthLong
                if (anunciado > LIMITE_DE_BYTES) return null
                conexao.inputStream.use { entrada ->
                    val saida = ByteArrayOutputStream()
                    val bloco = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val lido = entrada.read(bloco)
                        if (lido < 0) break
                        total += lido
                        if (total > LIMITE_DE_BYTES) return null
                        saida.write(bloco, 0, lido)
                    }
                    saida.toByteArray().takeIf { it.isNotEmpty() }
                }
            } catch (_: Exception) {
                null
            } finally {
                conexao.disconnect()
            }
        }

        private fun textoDoJavascript(bruto: String?): String? {
            if (bruto.isNullOrBlank() || bruto == "null") return null
            return try {
                JSONArray("[$bruto]").getString(0).takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            }
        }
    }
}

```

## PainelDoChat abrirComTextoNoCampo
```
    fun abrirComTextoNoCampo(texto: String) {
        if (!aberta) abrir()
        campoMensagem.setText(texto)
        campoMensagem.setSelection(campoMensagem.text?.length ?: 0)
        campoMensagem.requestFocus()
    }
```

## Painel biblioteca extracao
```
        copiar.backgroundTintList = null
        copiar.stateListAnimator = null
        cancelar.backgroundTintList = null
        cancelar.stateListAnimator = null
        copiar.setOnClickListener {
            dialogo.dismiss()
            copiarImagemAberta()
        }
        val extrairTexto = raiz.findViewById<Button>(R.id.botao_extrair_texto_da_imagem_ampliada_pelo_toque_longo)
        extrairTexto.backgroundTintList = null
        extrairTexto.stateListAnimator = null
        extrairTexto.visibility =
            if (ExtracaoDeTextoDeImagemViaIaDoNavegador.mostrarAcao(temChaveDaExtracaoDeTexto())) View.VISIBLE else View.GONE
        extrairTexto.setOnClickListener {
            dialogo.dismiss()
            extrairTextoDaImagemAberta()
        }
        cancelar.setOnClickListener { dialogo.dismiss() }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    fun ligarExtracaoDeTexto(
        temChave: () -> Boolean,
        extrair: (ByteArray) -> Unit,
    ) {
        temChaveDaExtracaoDeTexto = temChave
        aoExtrairTextoDaImagem = extrair
    }

    private fun extrairTextoDaImagemAberta() {
        val extrair = aoExtrairTextoDaImagem ?: return
        val id = imagemAbertaId
        val bytes: ByteArray? = if (id != null) {
            bibliotecaDeImagens.buscar(id)?.let { gravacaoDeImagens.ler(it.nomeDoArquivo) }
        } else {
            val bitmap = quadroAmpliado
            if (bitmap == null || bitmap.isRecycled) {
                null
            } else {
                try {
                    ByteArrayOutputStream().use { saida ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)
                        saida.toByteArray()
                    }
                } catch (_: Exception) {
                    null
                }
            }
        }
        if (bytes == null || bytes.isEmpty()) {
            Toast.makeText(activity, R.string.quadro_indisponivel, Toast.LENGTH_LONG).show()
            return
        }
        extrair(bytes)
    }

    private fun copiarImagemAberta() {
        val id = imagemAbertaId
        if (id != null) {
            val item = bibliotecaDeImagens.buscar(id) ?: return
            copiar(item)
            return
```

## Activity wire snippets
```
        CopiaDeImagemDaPaginaParaABibliotecaDoNavegador(
            this,
            ::receberImagemDaPagina,
            { cofreDaChaveDaOpenRouter.temChave() },
        ) { endereco ->
            colocarNaColecaoPeloToque(TipoDoItemDaColecaoFlutuante.IMAGEM, endereco, "Imagem")
        }
    }
    private val cofreDaChaveDaOpenRouter by lazy { CofreDaChaveDaOpenRouter(this) }
    private val orquestradorDaExtracaoDeTextoDeImagem by lazy {
        OrquestradorDaExtracaoDeTextoDeImagemViaIaDoNavegador(
            activity = this,
            cofre = cofreDaChaveDaOpenRouter,
            lerModelo = { preferencias.modeloDoChat },
            aoAbrirChatComTexto = { texto -> painelDoChat.abrirComTextoNoCampo(texto) },
        )
    }
    private val menuDoLinkDaPagina by lazy {
        MenuDoToqueLongoNoLinkDaPaginaDoNavegadorChromebrothers(
            activity = this,
            aoAbrirEmNovaAba = { endereco -> abrirLinkEmNovaAba(endereco) },
            aoAbrirEmNovaJanela = { endereco -> abrirNovaJanela(endereco) },
            aoCopiar = { endereco -> copiarEnderecoDoLink(endereco) },
        )
    }
    private lateinit var bibliotecaDeImagens: com.chromebrothers.navegador.regras.BibliotecaDeImagensCapturadasDoNavegador

---

                painelDaColecao.carregarComponenteDaBiblioteca(item)
            },
            idDaPaginaVinculadaNaColecao = { painelDaColecao.idOrigemDaBibliotecaAtual() },
            aoLimparVinculoDaPaginaNaColecao = { painelDaColecao.limparVinculoBiblioteca() },
        )
        painelDaBibliotecaDeVideos.ligarExtracaoDeTexto(
            temChave = { cofreDaChaveDaOpenRouter.temChave() },
            extrair = { bytes -> orquestradorDaExtracaoDeTextoDeImagem.extrairDeBytes(bytes) },
        )
        findViewById<ImageButton>(R.id.botao_favoritar_o_video_da_biblioteca).setOnClickListener {
            alternarFavoritoDoVideoNoPlayerDaPagina()
            mostrarControlesDoPlayerPorUmInstante()
        }
        findViewById<ImageButton>(R.id.botao_abrir_gaveta_da_biblioteca_de_videos).setOnClickListener {
            painelDaBibliotecaDeVideos.mostrar()
        }

---

        if (!::bibliotecaDeImagens.isInitialized) return null
        val item = gravacaoDaBibliotecaDeImagens.guardarQuadro(bibliotecaDeImagens, png, naGaleria)
        if (painelDaBibliotecaDeVideos.estaAberto()) painelDaBibliotecaDeVideos.atualizar()
        return item
    }

    private fun receberImagemDaPagina(bytes: ByteArray, destino: DestinoDaImagemDaPagina) {
        if (destino == DestinoDaImagemDaPagina.EXTRAIR_TEXTO) {
            orquestradorDaExtracaoDeTextoDeImagem.extrairDeBytes(bytes)
            return
        }
```


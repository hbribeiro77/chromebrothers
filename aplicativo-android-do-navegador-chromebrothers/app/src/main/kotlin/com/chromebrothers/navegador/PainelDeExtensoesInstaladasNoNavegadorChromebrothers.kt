package com.chromebrothers.navegador

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.chromebrothers.navegador.regras.ExtensoesQueOUsuarioPodeGerirNoNavegador
import com.chromebrothers.navegador.regras.TextoDaPermissaoDeExtensaoDoFirefox
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebExtension
import java.io.File

class PainelDeExtensoesInstaladasNoNavegadorChromebrothers(
    private val activity: AppCompatActivity,
    private val faixaDeBotoes: LinearLayout,
    private val painelDoPopup: FrameLayout,
    private val aoEscolherArquivo: () -> Unit,
    private val aoAbrirLoja: () -> Unit,
) {
    private var dialogo: Dialog? = null
    private var visorDoPopup: GeckoView? = null
    private var sessaoDoPopup: GeckoSession? = null
    private val acoes = linkedMapOf<String, WebExtension.Action>()
    private val faixaDaRolagem: View? = faixaDeBotoes.parent as? View

    fun ligar() {
        painelDoPopup.clipToOutline = true
        painelDoPopup.outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
        painelDoPopup.setBackgroundResource(R.drawable.fundo_arredondado_da_caixa_de_dialogo_do_editor_de_imagem)
        MotorDoFirefoxDoNavegadorChromebrothers.definirPedidoDePermissaoDeExtensao { nome, permissoes, responder ->
            activity.runOnUiThread { perguntarPermissao(nome, permissoes, responder) }
        }
        MotorDoFirefoxDoNavegadorChromebrothers.definirAcaoDeExtensao { id, titulo, acao, icone ->
            activity.runOnUiThread {
                if (acao == null) acoes.remove(id) else acoes[id] = acao
                desenharBotoes(id, titulo, icone)
            }
        }
        MotorDoFirefoxDoNavegadorChromebrothers.definirPopupDeExtensao { sessao ->
            activity.runOnUiThread { mostrarPopup(sessao) }
        }
        MotorDoFirefoxDoNavegadorChromebrothers.quandoExtensaoPronta {
            MotorDoFirefoxDoNavegadorChromebrothers.listarExtensoes { }
        }
    }

    fun mostrarLista() {
        dialogo?.dismiss()
        MotorDoFirefoxDoNavegadorChromebrothers.listarExtensoes { extensoes ->
            activity.runOnUiThread { abrirLista(extensoes) }
        }
    }

    fun receberArquivo(uri: Uri) {
        val destino = File(activity.cacheDir, "extensao-pedida-${System.currentTimeMillis()}.xpi")
        val copiou = runCatching {
            activity.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { saida -> entrada.copyTo(saida) }
            } ?: 0L
        }.getOrDefault(0L)
        if (copiou <= 0L || destino.length() == 0L) {
            destino.delete()
            avisar(R.string.arquivo_de_extensao_invalido)
            return
        }
        MotorDoFirefoxDoNavegadorChromebrothers.instalarExtensao(destino.toURI().toString()) { instalou ->
            activity.runOnUiThread {
                destino.delete()
                avisar(if (instalou) R.string.extensao_instalada else R.string.extensao_nao_instalada)
                if (instalou) mostrarLista()
            }
        }
    }

    fun fecharPopup(): Boolean {
        if (painelDoPopup.visibility != View.VISIBLE) return false
        visorDoPopup?.releaseSession()
        sessaoDoPopup?.close()
        visorDoPopup?.let { painelDoPopup.removeView(it) }
        visorDoPopup = null
        sessaoDoPopup = null
        painelDoPopup.visibility = View.GONE
        return true
    }

    private fun abrirLista(extensoes: List<WebExtension>) {
        val visiveis = extensoes.filter {
            ExtensoesQueOUsuarioPodeGerirNoNavegador.visivel(
                id = it.id,
                embutida = it.isBuiltIn,
                idEmbutido = MotorDoFirefoxDoNavegadorChromebrothers.idDaExtensaoEmbutida(),
            )
        }
        val raiz = caixaDoPadrao()
        raiz.findViewById<TextView>(R.id.titulo_da_caixa_de_dialogo_do_editor).text =
            activity.getString(R.string.extensoes)
        val lista = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        if (visiveis.isEmpty()) {
            lista.addView(TextView(activity).apply {
                text = activity.getString(R.string.nenhuma_extensao_instalada)
                setTextColor(Color.parseColor("#C4C7CE"))
                textSize = 15f
            })
        }
        visiveis.forEach { extensao -> lista.addView(linhaDaExtensao(extensao)) }
        val rolagem = ScrollView(activity).apply {
            isFillViewport = false
            addView(lista)
        }
        val miolo = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            val margem = (12 * activity.resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = margem }
            addView(rolagem, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ))
            addView(botaoDaLista(R.string.loja_de_extensoes_do_firefox, aoAbrirLoja))
            addView(botaoDaLista(R.string.instalar_extensao_de_arquivo, aoEscolherArquivo))
        }
        raiz.addView(miolo, raiz.childCount - 1)
        val cancelar = raiz.findViewById<Button>(R.id.botao_cancelar_da_caixa_de_dialogo_do_editor)
        val confirmar = raiz.findViewById<Button>(R.id.botao_confirmar_da_caixa_de_dialogo_do_editor)
        cancelar.text = activity.getString(R.string.fechar_lista_de_extensoes)
        confirmar.visibility = View.GONE
        val janela = apresentarCaixa(raiz)
        cancelar.setOnClickListener { janela.dismiss() }
        limitarAltura(rolagem)
        dialogo = janela
    }

    private fun botaoDaLista(texto: Int, aoClicar: () -> Unit): Button {
        val densidade = activity.resources.displayMetrics.density
        return Button(activity).apply {
            text = activity.getString(texto)
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            setTextColor(Color.parseColor("#F4F7FB"))
            textSize = 16f
            isAllCaps = false
            backgroundTintList = null
            stateListAnimator = null
            setBackgroundColor(Color.TRANSPARENT)
            minWidth = 0
            minimumWidth = 0
            val altura = (48 * densidade).toInt()
            minHeight = altura
            minimumHeight = altura
            setPadding((8 * densidade).toInt(), 0, (8 * densidade).toInt(), 0)
            setOnClickListener {
                dialogo?.dismiss()
                aoClicar()
            }
        }
    }

    private fun linhaDaExtensao(extensao: WebExtension): View {
        val linha = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        val nome = extensao.metaData?.name?.takeIf { it.isNotBlank() } ?: extensao.id
        val densidade = activity.resources.displayMetrics.density
        linha.minimumHeight = (48 * densidade).toInt()
        linha.addView(TextView(activity).apply {
            text = nome
            setTextColor(Color.parseColor("#F4F7FB"))
            textSize = 16f
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        linha.addView(SwitchCompat(activity).apply {
            isChecked = extensao.metaData?.enabled != false
            setOnCheckedChangeListener { _, ligado ->
                if (ligado == (extensao.metaData?.enabled != false)) return@setOnCheckedChangeListener
                MotorDoFirefoxDoNavegadorChromebrothers.definirExtensaoLigada(extensao, ligado) {
                    activity.runOnUiThread { mostrarLista() }
                }
            }
        })
        linha.addView(Button(activity).apply {
            text = activity.getString(R.string.remover_extensao)
            setTextColor(Color.parseColor("#F28B82"))
            textSize = 15f
            isAllCaps = false
            backgroundTintList = null
            stateListAnimator = null
            setBackgroundColor(Color.TRANSPARENT)
            minWidth = 0
            minimumWidth = 0
            minHeight = 0
            minimumHeight = 0
            setOnClickListener {
                MotorDoFirefoxDoNavegadorChromebrothers.desinstalarExtensao(extensao) {
                    activity.runOnUiThread {
                        acoes.remove(extensao.id)
                        desenharBotoes(extensao.id, nome, null)
                        mostrarLista()
                    }
                }
            }
        })
        return linha
    }

    private fun perguntarPermissao(nome: String, permissoes: List<String>, responder: (Boolean) -> Unit) {
        if (activity.isFinishing) {
            responder(false)
            return
        }
        var respondeu = false
        fun umaVez(aceitou: Boolean) {
            if (respondeu) return
            respondeu = true
            responder(aceitou)
        }
        val frases = permissoes.map { TextoDaPermissaoDeExtensaoDoFirefox.frase(it) }
        val texto = if (frases.isEmpty()) {
            activity.getString(R.string.extensao_sem_permissao_extra)
        } else {
            frases.joinToString("\n") { "• $it" }
        }
        val raiz = caixaDoPadrao()
        raiz.findViewById<TextView>(R.id.titulo_da_caixa_de_dialogo_do_editor).text =
            nome.ifBlank { activity.getString(R.string.extensoes) }
        val mensagem = raiz.findViewById<TextView>(R.id.mensagem_da_caixa_de_dialogo_do_editor)
        mensagem.visibility = View.VISIBLE
        mensagem.text = texto
        val cancelar = raiz.findViewById<Button>(R.id.botao_cancelar_da_caixa_de_dialogo_do_editor)
        val confirmar = raiz.findViewById<Button>(R.id.botao_confirmar_da_caixa_de_dialogo_do_editor)
        cancelar.text = activity.getString(R.string.cancelar_caixa_do_editor)
        confirmar.text = activity.getString(R.string.instalar_extensao)
        confirmar.setTextColor(Color.parseColor("#8AB4F8"))
        val janela = apresentarCaixa(raiz)
        cancelar.setOnClickListener {
            janela.dismiss()
            umaVez(false)
        }
        confirmar.setOnClickListener {
            janela.dismiss()
            umaVez(true)
        }
        janela.setOnCancelListener { umaVez(false) }
    }

    private fun caixaDoPadrao(): LinearLayout {
        val raiz = activity.layoutInflater.inflate(
            R.layout.layout_caixa_de_dialogo_do_editor_de_imagem,
            null,
        ) as LinearLayout
        raiz.findViewById<EditText>(R.id.campo_da_caixa_de_dialogo_do_editor).visibility = View.GONE
        listOf(
            R.id.botao_cancelar_da_caixa_de_dialogo_do_editor,
            R.id.botao_confirmar_da_caixa_de_dialogo_do_editor,
        ).forEach { id ->
            raiz.findViewById<Button>(id).apply {
                backgroundTintList = null
                stateListAnimator = null
            }
        }
        return raiz
    }

    private fun apresentarCaixa(raiz: LinearLayout): Dialog {
        val janela = Dialog(activity)
        janela.setContentView(raiz)
        janela.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        janela.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        janela.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
        return janela
    }

    private fun limitarAltura(rolagem: ScrollView) {
        val maximo = (320 * activity.resources.displayMetrics.density).toInt()
        rolagem.post {
            if (rolagem.height <= maximo) return@post
            rolagem.layoutParams = rolagem.layoutParams.apply { height = maximo }
        }
    }

    private fun desenharBotoes(idAtualizado: String, titulo: String, icone: Bitmap?) {
        val acao = acoes[idAtualizado]
        val existente = faixaDeBotoes.findViewWithTag<ImageButton>(idAtualizado)
        if (acao == null) {
            existente?.let { faixaDeBotoes.removeView(it) }
            atualizarVisibilidadeDaFaixa()
            return
        }
        val botao = existente ?: ImageButton(activity).apply {
            tag = idAtualizado
            val lado = (40 * activity.resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(lado, lado).apply {
                marginEnd = (4 * activity.resources.displayMetrics.density).toInt()
            }
            isClickable = true
            isFocusable = true
            setBackgroundResource(android.R.drawable.list_selector_background)
            scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            setImageResource(R.drawable.icone_de_extensoes_do_navegador_chromebrothers)
            setOnClickListener {
                val atual = acoes[idAtualizado]
                if (atual == null) {
                    Log.w("ChromeBrothersExt", "clique sem acao id=$idAtualizado")
                    return@setOnClickListener
                }
                if (painelDoPopup.visibility == View.VISIBLE) {
                    fecharPopup()
                    return@setOnClickListener
                }
                Log.i("ChromeBrothersExt", "clique na acao id=$idAtualizado enabled=${atual.enabled}")
                atual.click()
                Handler(Looper.getMainLooper()).postDelayed({
                    if (painelDoPopup.visibility != View.VISIBLE) {
                        Log.w("ChromeBrothersExt", "popup nao abriu apos clique id=$idAtualizado")
                        Toast.makeText(
                            activity,
                            R.string.popup_da_extensao_nao_abriu,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }, 900)
            }
            faixaDeBotoes.addView(this)
        }
        botao.contentDescription = titulo.ifBlank { idAtualizado }
        botao.isEnabled = true
        botao.alpha = if (acao.enabled == false) 0.45f else 1f
        if (icone != null) botao.setImageBitmap(icone)
        atualizarVisibilidadeDaFaixa()
    }

    private fun atualizarVisibilidadeDaFaixa() {
        val tem = faixaDeBotoes.childCount > 0
        faixaDeBotoes.visibility = View.VISIBLE
        faixaDaRolagem?.visibility = if (tem) View.VISIBLE else View.GONE
    }

    private fun mostrarPopup(sessao: GeckoSession) {
        Log.i("ChromeBrothersExt", "mostrarPopup")
        fecharPopup()
        val visor = GeckoView(activity)
        visor.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        painelDoPopup.addView(visor, 0)
        painelDoPopup.elevation = 40f * activity.resources.displayMetrics.density
        painelDoPopup.visibility = View.VISIBLE
        painelDoPopup.bringToFront()
        visor.setSession(sessao)
        visorDoPopup = visor
        sessaoDoPopup = sessao
    }

    private fun avisar(mensagem: Int) {
        Toast.makeText(activity, mensagem, Toast.LENGTH_SHORT).show()
    }
}

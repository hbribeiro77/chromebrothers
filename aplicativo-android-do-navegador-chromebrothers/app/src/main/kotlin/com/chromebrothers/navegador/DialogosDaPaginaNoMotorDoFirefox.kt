package com.chromebrothers.navegador

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.SystemClock
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import com.chromebrothers.navegador.regras.LimiteDeDialogosSeguidosDaPagina
import com.chromebrothers.navegador.regras.TraducaoDoPedidoDeArquivoDoFirefox
import com.chromebrothers.navegador.regras.ValorDeDataEHoraDoCampoDaPagina
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.PromptDelegate
import org.mozilla.geckoview.GeckoSession.PromptDelegate.PromptResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Diálogos que a página pede. Sem isto o GeckoView descarta tudo: `<select>` não abre, confirm() vira
 * "cancelar" e alert() some. Página escondida (aba ao fundo, reserva) segue descartando, sem caixa.
 */
class DialogosDaPaginaNoMotorDoFirefox(
    private val superficie: SuperficieDaPaginaNoMotorDoFirefox,
) : PromptDelegate {
    private val limite = LimiteDeDialogosSeguidosDaPagina()
    private val abertas = mutableSetOf<() -> Unit>()

    fun aoComecarPagina() = limite.paginaNova()

    fun fecharTudo() {
        abertas.toList().forEach { it() }
        abertas.clear()
    }

    private fun atividade(): Activity? = superficie.atividadeParaDialogos()

    private fun tituloDaPagina(): String {
        val host = runCatching { Uri.parse(superficie.url).host }.getOrNull().orEmpty()
        return if (host.isBlank()) "A página diz" else "$host diz"
    }

    /** Uma resposta só por pedido; o Gecko pode fechar o pedido sozinho (página saiu, campo perdeu o foco). */
    private inner class Resposta(private val pedido: PromptDelegate.BasePrompt) {
        val resultado = GeckoResult<PromptResponse>()
        private var respondida = false
        private var fecharCaixa: (() -> Unit)? = null

        fun completar(resposta: () -> PromptResponse) {
            if (respondida) return
            respondida = true
            fecharCaixa?.let { abertas.remove(it) }
            runCatching { resultado.complete(resposta()) }
        }

        fun descartar() = completar { pedido.dismiss() }

        fun ligar(aoAtualizar: (PromptDelegate.BasePrompt) -> Unit = {}, fechar: () -> Unit) {
            fecharCaixa?.let { abertas.remove(it) }
            fecharCaixa = fechar
            if (respondida) return
            abertas.add(fechar)
            pedido.delegate = object : PromptDelegate.PromptInstanceDelegate {
                override fun onPromptDismiss(prompt: PromptDelegate.BasePrompt) = fechar()
                override fun onPromptUpdate(prompt: PromptDelegate.BasePrompt) = aoAtualizar(prompt)
            }
        }
    }

    private fun descartado(pedido: PromptDelegate.BasePrompt): GeckoResult<PromptResponse> =
        GeckoResult.fromValue(pedido.dismiss())

    private fun mostrar(
        pedido: PromptDelegate.BasePrompt,
        titulo: String,
        mensagem: String?,
        conteudo: View? = null,
        textoConfirmar: String = "OK",
        textoCancelar: String? = "Cancelar",
        contaNoLimite: Boolean = false,
        aoCancelar: () -> PromptResponse = { pedido.dismiss() },
        aoConfirmar: () -> PromptResponse,
    ): GeckoResult<PromptResponse> {
        val activity = atividade()
        if (activity == null || (contaNoLimite && limite.silenciado)) return GeckoResult.fromValue(aoCancelar())
        if (contaNoLimite) limite.registrar(SystemClock.elapsedRealtime())
        val resposta = Resposta(pedido)
        val caixa = CaixaDoPedidoDaPagina(
            activity,
            titulo,
            mensagem,
            conteudo,
            textoConfirmar,
            textoCancelar,
            textoExtra = if (contaNoLimite && limite.deveOferecerParar()) "Parar diálogos" else null,
            fecharAoTocarFora = false,
            aoExtra = {
                limite.silenciar()
                resposta.completar(aoCancelar)
            },
            aoConfirmar = {
                limite.fechou(SystemClock.elapsedRealtime())
                resposta.completar(aoConfirmar)
            },
            aoCancelar = {
                limite.fechou(SystemClock.elapsedRealtime())
                resposta.completar(aoCancelar)
            },
        )
        resposta.ligar { caixa.fechar() }
        caixa.mostrar()
        return resposta.resultado
    }

    override fun onAlertPrompt(session: GeckoSession, prompt: PromptDelegate.AlertPrompt) =
        mostrar(prompt, tituloDaPagina(), prompt.message, textoCancelar = null, contaNoLimite = true) { prompt.dismiss() }

    override fun onButtonPrompt(session: GeckoSession, prompt: PromptDelegate.ButtonPrompt) =
        mostrar(
            prompt,
            tituloDaPagina(),
            prompt.message,
            contaNoLimite = true,
            aoCancelar = { prompt.confirm(PromptDelegate.ButtonPrompt.Type.NEGATIVE) },
        ) { prompt.confirm(PromptDelegate.ButtonPrompt.Type.POSITIVE) }

    override fun onTextPrompt(
        session: GeckoSession,
        prompt: PromptDelegate.TextPrompt,
    ): GeckoResult<PromptResponse> {
        // Pontes internas (página inicial, gravador de roteiro) chegam por prompt() e nunca viram caixa.
        val interna = superficie.aoPromptDaPagina?.invoke(prompt.message.orEmpty())
        if (interna != null) return GeckoResult.fromValue(prompt.confirm(interna))
        val activity = atividade() ?: return descartado(prompt)
        val campo = campoDeTexto(activity, "").apply {
            setText(prompt.defaultValue.orEmpty())
            selectAll()
        }
        return mostrar(prompt, tituloDaPagina(), prompt.message, campo, contaNoLimite = true) {
            prompt.confirm(campo.text.toString())
        }
    }

    override fun onBeforeUnloadPrompt(
        session: GeckoSession,
        prompt: PromptDelegate.BeforeUnloadPrompt,
    ): GeckoResult<PromptResponse> {
        // Escondida (ex.: reserva voltando à inicial): sai sem perguntar, não há quem responda.
        if (atividade() == null) return GeckoResult.fromValue(prompt.confirm(AllowOrDeny.ALLOW))
        return mostrar(
            prompt,
            "Sair desta página?",
            "As alterações que você fez podem não ser salvas.",
            textoConfirmar = "Sair",
            textoCancelar = "Ficar",
            aoCancelar = { prompt.confirm(AllowOrDeny.DENY) },
        ) { prompt.confirm(AllowOrDeny.ALLOW) }
    }

    override fun onRepostConfirmPrompt(session: GeckoSession, prompt: PromptDelegate.RepostConfirmPrompt) =
        mostrar(
            prompt,
            "Enviar o formulário de novo?",
            "Recarregar esta página repete o último envio (uma compra ou mensagem, por exemplo).",
            textoConfirmar = "Enviar de novo",
            aoCancelar = { prompt.confirm(AllowOrDeny.DENY) },
        ) { prompt.confirm(AllowOrDeny.ALLOW) }

    override fun onAuthPrompt(
        session: GeckoSession,
        prompt: PromptDelegate.AuthPrompt,
    ): GeckoResult<PromptResponse> {
        val activity = atividade() ?: return descartado(prompt)
        val opcoes = prompt.authOptions
        fun tem(bandeira: Int) = opcoes.flags and bandeira != 0
        val soSenha = tem(PromptDelegate.AuthPrompt.AuthOptions.Flags.ONLY_PASSWORD)
        val usuario = campoDeTexto(activity, "Usuário").apply { setText(opcoes.username.orEmpty()) }
        val senha = campoDeTexto(activity, "Senha").apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(opcoes.password.orEmpty())
        }
        val campos = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            if (!soSenha) addView(usuario)
            addView(senha)
        }
        val host = runCatching { Uri.parse(opcoes.uri).host }.getOrNull()
        val aviso = listOfNotNull(
            "Usuário ou senha incorretos.".takeIf { tem(PromptDelegate.AuthPrompt.AuthOptions.Flags.PREVIOUS_FAILED) },
            prompt.message?.takeIf { it.isNotBlank() },
            "A conexão não é segura: a senha vai sem proteção."
                .takeIf { opcoes.level == PromptDelegate.AuthPrompt.AuthOptions.Level.NONE },
        ).joinToString("\n")
        return mostrar(prompt, "Entrar em ${host ?: "site protegido"}", aviso, campos, textoConfirmar = "Entrar") {
            if (soSenha) {
                prompt.confirm(senha.text.toString())
            } else {
                prompt.confirm(usuario.text.toString(), senha.text.toString())
            }
        }
    }

    override fun onSharePrompt(
        session: GeckoSession,
        prompt: PromptDelegate.SharePrompt,
    ): GeckoResult<PromptResponse> {
        val activity = atividade() ?: return descartado(prompt)
        val envio = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, listOfNotNull(prompt.text, prompt.uri).filter { it.isNotBlank() }.joinToString("\n"))
            prompt.title?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        }
        val abriu = runCatching { activity.startActivity(Intent.createChooser(envio, null)) }.isSuccess
        val resultado = PromptDelegate.SharePrompt.Result.SUCCESS.takeIf { abriu }
            ?: PromptDelegate.SharePrompt.Result.FAILURE
        return GeckoResult.fromValue(prompt.confirm(resultado))
    }

    override fun onFilePrompt(
        session: GeckoSession,
        prompt: PromptDelegate.FilePrompt,
    ): GeckoResult<PromptResponse> {
        val pendente = GeckoResult<PromptResponse>()
        val tipos = TraducaoDoPedidoDeArquivoDoFirefox.tipos(prompt.mimeTypes)
        val pasta = TraducaoDoPedidoDeArquivoDoFirefox.escolhePasta(prompt.type)
        val varios = TraducaoDoPedidoDeArquivoDoFirefox.variosArquivos(prompt.type)
        superficie.post {
            val tratado = superficie.aoPedidoDeArquivo
            if (tratado == null) {
                pendente.complete(prompt.dismiss())
            } else {
                tratado(tipos, pasta, varios, prompt, pendente)
            }
        }
        return pendente
    }

    // ---------- <select> ----------

    private class Linha(
        val id: String,
        val rotulo: String,
        val ehGrupo: Boolean,
        val dentroDeGrupo: Boolean,
        val desabilitada: Boolean,
        val marcada: Boolean,
    )

    private fun linhas(
        opcoes: Array<out PromptDelegate.ChoicePrompt.Choice>,
        emGrupo: Boolean = false,
    ): List<Linha> = opcoes.flatMap { opcao ->
        val itens = opcao.items
        when {
            opcao.separator -> emptyList()
            itens != null ->
                listOf(Linha(opcao.id, opcao.label, true, emGrupo, true, false)) + linhas(itens, true)
            else -> listOf(Linha(opcao.id, opcao.label, false, emGrupo, opcao.disabled, opcao.selected))
        }
    }

    override fun onChoicePrompt(
        session: GeckoSession,
        prompt: PromptDelegate.ChoicePrompt,
    ): GeckoResult<PromptResponse> {
        val activity = atividade() ?: return descartado(prompt)
        val varias = prompt.type == PromptDelegate.ChoicePrompt.Type.MULTIPLE
        var atual = prompt
        var itens = linhas(prompt.choices)
        val marcadas = itens.filter { it.marcada }.map { it.id }.toMutableSet()
        val densidade = activity.resources.displayMetrics.density
        val adaptador = object : BaseAdapter() {
            override fun getCount() = itens.size
            override fun getItem(posicao: Int) = itens[posicao]
            override fun getItemId(posicao: Int) = posicao.toLong()
            override fun areAllItemsEnabled() = false
            override fun isEnabled(posicao: Int) = !itens[posicao].desabilitada
            override fun getView(posicao: Int, reuso: View?, pai: ViewGroup?): View {
                val linha = itens[posicao]
                val vista = reuso as? TextView ?: TextView(activity).apply {
                    minHeight = (48 * densidade).toInt()
                    gravity = Gravity.CENTER_VERTICAL
                }
                val marcada = linha.id in marcadas
                val recuo = if (linha.dentroDeGrupo) 20 else 8
                vista.text = if (marcada) "✓  ${linha.rotulo}" else linha.rotulo
                vista.textSize = if (linha.ehGrupo) 13f else 16f
                vista.setPadding((recuo * densidade).toInt(), 0, (8 * densidade).toInt(), 0)
                val cor = when {
                    marcada -> "#8AB4F8"
                    linha.desabilitada -> "#8A9099"
                    else -> "#F4F7FB"
                }
                vista.setTextColor(Color.parseColor(cor))
                return vista
            }
        }
        val lista = ListView(activity).apply {
            divider = null
            adapter = adaptador
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                minOf(
                    (itens.size.coerceAtLeast(1) * 48 * densidade).toInt(),
                    activity.resources.displayMetrics.heightPixels * 11 / 20,
                ),
            )
        }
        val resposta = Resposta(prompt)
        val caixa = CaixaDoPedidoDaPagina(
            activity,
            titulo = prompt.title?.takeIf { it.isNotBlank() }
                ?: if (varias) "Escolha uma ou mais opções" else "Escolha uma opção",
            mensagem = prompt.message,
            conteudo = lista,
            textoConfirmar = if (varias) "OK" else null,
            aoConfirmar = { resposta.completar { atual.confirm(marcadas.toTypedArray()) } },
            aoCancelar = { resposta.completar { atual.dismiss() } },
        )
        lista.setOnItemClickListener { _, _, posicao, _ ->
            val id = itens[posicao].id
            if (varias) {
                if (!marcadas.remove(id)) marcadas.add(id)
                adaptador.notifyDataSetChanged()
            } else {
                caixa.responder { resposta.completar { atual.confirm(id) } }
            }
        }
        // O site pode trocar as opções com a lista aberta; os ids mudam e precisam ser relidos.
        resposta.ligar(
            aoAtualizar = { novo ->
                if (novo is PromptDelegate.ChoicePrompt) {
                    atual = novo
                    itens = linhas(novo.choices)
                    marcadas.clear()
                    itens.filter { it.marcada }.forEach { marcadas.add(it.id) }
                    adaptador.notifyDataSetChanged()
                }
            },
        ) { caixa.fechar() }
        caixa.mostrar()
        itens.indexOfFirst { it.marcada }.takeIf { it > 0 }?.let { lista.setSelection(it) }
        return resposta.resultado
    }

    // ---------- Data, hora e cor ----------

    override fun onDateTimePrompt(
        session: GeckoSession,
        prompt: PromptDelegate.DateTimePrompt,
    ): GeckoResult<PromptResponse> {
        val activity = atividade() ?: return descartado(prompt)
        val regra = ValorDeDataEHoraDoCampoDaPagina
        val resposta = Resposta(prompt)
        val dataInicial = regra.data(prompt.defaultValue) ?: LocalDate.now()
        val horaInicial = regra.hora(prompt.defaultValue) ?: LocalTime.now()
        fun responder(valor: String) = resposta.completar { prompt.confirm(valor) }

        // Fechar sem escolher só descarta se a etapa seguinte (a hora) não assumiu.
        fun abrir(dialogo: AlertDialog, seguiu: () -> Boolean) {
            // No relógio deitado não cabem três botões (o "Limpar" cortava o "Cancelar").
            if (dialogo is DatePickerDialog) {
                dialogo.setButton(DialogInterface.BUTTON_NEUTRAL, "Limpar") { _, _ -> responder("") }
            }
            dialogo.setOnDismissListener { if (!seguiu()) resposta.descartar() }
            resposta.ligar { runCatching { dialogo.dismiss() } }
            dialogo.show()
        }

        fun pedirHora(aoEscolher: (LocalTime) -> Unit) {
            val dialogo = TimePickerDialog(
                activity,
                { _, hora, minuto -> aoEscolher(LocalTime.of(hora, minuto)) },
                horaInicial.hour,
                horaInicial.minute,
                true,
            )
            abrir(dialogo) { false }
        }

        fun pedirData(temEtapaSeguinte: Boolean = false, aoEscolher: (LocalDate) -> Unit) {
            var seguiu = false
            val dialogo = DatePickerDialog(
                activity,
                { _, ano, mes, dia ->
                    seguiu = temEtapaSeguinte
                    aoEscolher(LocalDate.of(ano, mes + 1, dia))
                },
                dataInicial.year,
                dataInicial.monthValue - 1,
                dataInicial.dayOfMonth,
            )
            fun instante(dia: LocalDate) = dia.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val minima = regra.data(prompt.minValue)
            val maxima = regra.data(prompt.maxValue)?.let {
                when (prompt.type) {
                    PromptDelegate.DateTimePrompt.Type.MONTH -> it.plusMonths(1).minusDays(1)
                    PromptDelegate.DateTimePrompt.Type.WEEK -> it.plusDays(6)
                    else -> it
                }
            }
            if (minima == null || maxima == null || !maxima.isBefore(minima)) {
                minima?.let { dialogo.datePicker.minDate = instante(it) }
                maxima?.let { dialogo.datePicker.maxDate = instante(it) }
            }
            abrir(dialogo) { seguiu }
        }

        when (prompt.type) {
            PromptDelegate.DateTimePrompt.Type.TIME -> pedirHora { responder(regra.textoDaHora(it)) }
            PromptDelegate.DateTimePrompt.Type.MONTH -> pedirData { responder(regra.textoDoMes(it)) }
            PromptDelegate.DateTimePrompt.Type.WEEK -> pedirData { responder(regra.textoDaSemana(it)) }
            PromptDelegate.DateTimePrompt.Type.DATETIME_LOCAL ->
                pedirData(temEtapaSeguinte = true) { data ->
                    pedirHora { hora -> responder(regra.textoDaDataEHora(data, hora)) }
                }
            else -> pedirData { responder(regra.textoDaData(it)) }
        }
        return resposta.resultado
    }

    override fun onColorPrompt(
        session: GeckoSession,
        prompt: PromptDelegate.ColorPrompt,
    ): GeckoResult<PromptResponse> {
        val activity = atividade() ?: return descartado(prompt)
        val densidade = activity.resources.displayMetrics.density
        var cor = runCatching { Color.parseColor(prompt.defaultValue) }.getOrDefault(Color.BLACK)
        val amostra = TextView(activity).apply {
            gravity = Gravity.CENTER
            minHeight = (56 * densidade).toInt()
            textSize = 16f
        }
        fun hex() = "#%06x".format(cor and 0xFFFFFF)
        fun pintar() {
            amostra.setBackgroundColor(cor or 0xFF000000.toInt())
            amostra.text = hex()
            amostra.setTextColor(if (Color.luminance(cor) > 0.4f) Color.BLACK else Color.WHITE)
        }
        val conteudo = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            addView(amostra)
        }
        listOf(16 to "#F28B82", 8 to "#81C995", 0 to "#8AB4F8").forEach { (desvio, tom) ->
            val barra = SeekBar(activity).apply {
                max = 255
                progress = (cor shr desvio) and 0xFF
                progressTintList = ColorStateList.valueOf(Color.parseColor(tom))
                thumbTintList = progressTintList
                setPadding(paddingLeft, (14 * densidade).toInt(), paddingRight, (14 * densidade).toInt())
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(barra: SeekBar?, valor: Int, doUsuario: Boolean) {
                        cor = (cor and (0xFF shl desvio).inv()) or (valor shl desvio)
                        pintar()
                    }

                    override fun onStartTrackingTouch(barra: SeekBar?) = Unit
                    override fun onStopTrackingTouch(barra: SeekBar?) = Unit
                })
            }
            conteudo.addView(barra)
        }
        pintar()
        return mostrar(prompt, "Escolha uma cor", null, conteudo) { prompt.confirm(hex()) }
    }

    private fun campoDeTexto(activity: Activity, dica: String): EditText {
        val densidade = activity.resources.displayMetrics.density
        return EditText(activity).apply {
            hint = dica
            isSingleLine = true
            textSize = 16f
            setTextColor(Color.parseColor("#F4F7FB"))
            setHintTextColor(Color.parseColor("#8A9099"))
            setBackgroundResource(R.drawable.fundo_do_campo_da_caixa_de_dialogo_do_editor_de_imagem)
            setPadding((14 * densidade).toInt(), 0, (14 * densidade).toInt(), 0)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (48 * densidade).toInt())
                .apply { topMargin = (8 * densidade).toInt() }
        }
    }
}

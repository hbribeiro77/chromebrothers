package com.chromebrothers.navegador

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import com.chromebrothers.navegador.regras.CandidataDoElementoParaORoteiro
import com.chromebrothers.navegador.regras.CandidatasDoElementoParaORoteiroDeAutomacao
import com.chromebrothers.navegador.regras.EscolhaDeCandidataPelaIaDoGravadorDeRoteiro
import com.chromebrothers.navegador.regras.GestaoDeRoteirosSalvosDeAutomacaoDaPagina
import com.chromebrothers.navegador.regras.MensagemDeChat
import com.chromebrothers.navegador.regras.MontagemDoPassoDoRoteiroAPartirDaCandidata
import com.chromebrothers.navegador.regras.PassoDoRoteiroDeAutomacao
import com.chromebrothers.navegador.regras.ResultadoDaEscolhaDeCandidataPelaIa
import com.chromebrothers.navegador.regras.RoteiroDeAutomacaoDaPagina
import com.chromebrothers.navegador.regras.RoteiroDeAutomacaoDaPaginaDoNavegador
import com.chromebrothers.navegador.regras.ScriptsDoPickDeElementoParaOGravadorDeRoteiro
import com.chromebrothers.navegador.regras.VariaveisDoRoteiroDeAutomacaoDaPagina

class PainelDoGravadorDeRoteiroDeAutomacaoDoNavegador(
    private val activity: AppCompatActivity,
    private val preferencias: PreferenciasVisuaisEDeNavegacaoDoChromebrothers,
    private val urlDaAba: () -> String,
    private val densidadeDaPagina: () -> Float,
    private val avaliarJavascript: (script: String, aoReceber: ((String?) -> Unit)?) -> Unit,
    private val navegar: (url: String) -> Unit,
    private val salvar: (RoteiroDeAutomacaoDaPagina) -> String,
    private val apagarRoteiro: (id: String) -> Boolean,
    private val listarRoteiros: () -> List<RoteiroDeAutomacaoDaPagina>,
    private val rodar: (
        RoteiroDeAutomacaoDaPagina,
        variaveis: Map<String, String>,
        aoPasso: (indice: Int, total: Int, rotulo: String) -> Unit,
        aoFim: (String) -> Unit,
    ) -> Unit,
    private val cancelarExecucao: () -> Unit,
    private val enviarJsonAoChat: (String) -> Unit,
    private val abrirGavetaDoChat: () -> Unit,
    private val fecharGavetaDoChat: () -> Unit,
    private val mostrarHostNaGaveta: (Boolean) -> Unit,
    private val mostrarEscolhaSimples: (
        titulo: String,
        opcoes: List<Pair<String, String>>,
        indiceSelecionado: Int,
        aoEscolher: (String) -> Unit,
    ) -> Unit,
    private val temChaveDaOpenRouter: () -> Boolean,
    private val consultarIa: (
        mensagens: List<MensagemDeChat>,
        aoFim: (texto: String?, erro: String?) -> Unit,
    ) -> Unit,
    private val cancelarConsultaIa: () -> Unit,
) {
    private val faixa: LinearLayout = activity.findViewById(R.id.faixa_do_gravador_de_roteiro_de_automacao)
    private val hostGaveta: FrameLayout = activity.findViewById(R.id.host_do_gravador_na_gaveta_do_chat)
    private val raiz: ViewGroup = activity.findViewById(R.id.raiz_do_navegador)
    private val camadaPick: View = activity.findViewById(R.id.camada_do_pick_do_gravador_de_roteiro)
    private val campoNome: EditText = activity.findViewById(R.id.campo_nome_do_gravador_de_roteiro)
    private val campoUrl: EditText = activity.findViewById(R.id.campo_url_do_gravador_de_roteiro)
    private val chips: LinearLayout = activity.findViewById(R.id.chips_do_gravador_de_roteiro)
    private val avisoPick: TextView = activity.findViewById(R.id.aviso_do_pick_do_gravador_de_roteiro)
    private val lista: LinearLayout = activity.findViewById(R.id.lista_de_passos_do_gravador_de_roteiro)
    private val rolagemPassos: ScrollView = activity.findViewById(R.id.rolagem_dos_passos_do_gravador_de_roteiro)

    private val passos = mutableListOf<PassoDoRoteiroDeAutomacao>()
    private var modoPick: ModoPick? = null
    private var modoAberto: String? = null
    private var dialogoAtivo: Dialog? = null
    private var indiceParaSubstituir: Int? = null
    private var passoEmExecucao: Int = -1
    private var executando = false
    private var idDoRoteiro: String? = null
    private val botaoPlay: ImageButton =
        activity.findViewById(R.id.botao_play_do_gravador_de_roteiro)
    private val opcoesAguardarQualquerEmMontagem =
        mutableListOf<com.chromebrothers.navegador.regras.OpcaoDoAguardarQualquer>()
    private var idRamoAguardarQualquerPendente: String = ""

    private sealed class ModoPick {
        data object Aguardar : ModoPick()
        data object Preencher : ModoPick()
        data object Clicar : ModoPick()
        data object ClicarSe : ModoPick()
        data object AguardarSumir : ModoPick()
        data object Assertar : ModoPick()
        data object Extrair : ModoPick()
        data object AguardarQualquer : ModoPick()
    }

    fun ligar() {
        botaoPlay.setOnClickListener {
            if (executando) parar() else play()
        }
        activity.findViewById<ImageButton>(R.id.botao_salvar_do_gravador_de_roteiro)
            .setOnClickListener { salvarAgora() }
        activity.findViewById<ImageButton>(R.id.botao_fechar_do_gravador_de_roteiro)
            .setOnClickListener { fechar() }
        chips.removeAllViews()
        chip("Salvos", Color.parseColor("#8AB4F8")) { abrirListaDeSalvos() }
        chip("Novo", Color.parseColor("#C5C8CE")) { limparParaNovo() }
        chip("URL", Color.parseColor("#8AB4F8")) { confirmarUrlInicial() }
        chip("Aguardar", Color.parseColor("#F4F7FB")) { iniciarPick(ModoPick.Aguardar, null) }
        chip("Sumir", Color.parseColor("#F4F7FB")) { iniciarPick(ModoPick.AguardarSumir, null) }
        chip("URL…", Color.parseColor("#F4F7FB")) { pedirAguardarUrl() }
        chip("Preencher", Color.parseColor("#F4F7FB")) { iniciarPick(ModoPick.Preencher, null) }
        chip("Achar editor", Color.parseColor("#FDD663")) { acharEditorNaPaginaSemPick() }
        chip("Clicar", Color.parseColor("#F4F7FB")) { iniciarPick(ModoPick.Clicar, null) }
        chip("Clicar se", Color.parseColor("#F4F7FB")) { iniciarPick(ModoPick.ClicarSe, null) }
        chip("A|B", Color.parseColor("#FDD663")) { iniciarAguardarQualquer() }
        chip("Marcar", Color.parseColor("#FDD663")) { pedirMarcar() }
        chip("Ir para", Color.parseColor("#FDD663")) { pedirIrPara() }
        chip("Assert", Color.parseColor("#F4F7FB")) { iniciarPick(ModoPick.Assertar, null) }
        chip("Extrair", Color.parseColor("#F4F7FB")) { iniciarPick(ModoPick.Extrair, null) }
        chip("Esperar ms", Color.parseColor("#F4F7FB")) { pedirEsperarMs() }
        chip("Rolar", Color.parseColor("#F4F7FB")) { pedirRolar() }
        chip("JSON→chat", Color.parseColor("#C5C8CE")) { enviarAoChat() }
        camadaPick.setOnTouchListener { _, evento -> tratarToqueDoPick(evento) }
    }

    fun abrirComPadrao() {
        abrir(preferencias.modoDoGravadorDeRoteiro)
    }

    fun abrirMenuDeModo() {
        val atual = preferencias.modoDoGravadorDeRoteiro
        mostrarEscolhaSimples(
            activity.getString(R.string.titulo_opcoes_do_gravador),
            listOf(
                activity.getString(R.string.modo_do_gravador_na_gaveta) to "abrir_gaveta",
                activity.getString(R.string.modo_do_gravador_na_faixa) to "abrir_faixa",
                activity.getString(R.string.definir_padrao_gravador_gaveta) to "padrao_gaveta",
                activity.getString(R.string.definir_padrao_gravador_faixa) to "padrao_faixa",
            ),
            when (atual) {
                "faixa" -> 1
                else -> 0
            },
        ) { escolha ->
            when (escolha) {
                "abrir_gaveta" -> abrir("gaveta")
                "abrir_faixa" -> abrir("faixa")
                "padrao_gaveta" -> {
                    preferencias.modoDoGravadorDeRoteiro = "gaveta"
                    Toast.makeText(activity, R.string.definir_padrao_gravador_gaveta, Toast.LENGTH_SHORT).show()
                }
                "padrao_faixa" -> {
                    preferencias.modoDoGravadorDeRoteiro = "faixa"
                    Toast.makeText(activity, R.string.definir_padrao_gravador_faixa, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun abrir(modo: String) {
        if (estaAberto()) fechar(manterGaveta = true)
        if (campoUrl.text.isNullOrBlank()) campoUrl.setText(urlDaAba())
        if (campoNome.text.isNullOrBlank()) campoNome.setText("Roteiro")
        modoAberto = if (modo == "faixa") "faixa" else "gaveta"
        reparentarFaixa(modoAberto!!)
        faixa.visibility = View.VISIBLE
        redesenharLista()
        if (modoAberto == "gaveta") {
            mostrarHostNaGaveta(true)
            abrirGavetaDoChat()
        }
    }

    fun fechar(manterGaveta: Boolean = false) {
        if (executando) parar()
        cancelarPick()
        limparPreview()
        dialogoAtivo?.dismiss()
        dialogoAtivo = null
        faixa.visibility = View.GONE
        val eraGaveta = modoAberto == "gaveta"
        modoAberto = null
        if (eraGaveta) {
            mostrarHostNaGaveta(false)
            if (!manterGaveta) fecharGavetaDoChat()
        }
        // Devolve a faixa ao root (embaixo) para o próximo open.
        reparentarFaixa("faixa")
        faixa.visibility = View.GONE
    }

    fun estaAberto(): Boolean = modoAberto != null && faixa.visibility == View.VISIBLE

    fun receberPick(json: String) {
        if (!estaAberto()) return
        val modo = modoPick ?: return
        // Encerra o overlay de pick, mas mantém __cbGravadorAlvo para os direcionais.
        modoPick = null
        avisoPick.visibility = View.GONE
        camadaPick.visibility = View.INVISIBLE
        val marca = CandidatasDoElementoParaORoteiroDeAutomacao.lerMarca(json) ?: run {
            Toast.makeText(activity, "Não li o elemento.", Toast.LENGTH_SHORT).show()
            limparAlvoDoPick()
            return
        }
        abrirEscolhaDeCandidatas(modo, marca)
    }

    private fun abrirEscolhaDeCandidatas(
        modo: ModoPick,
        marca: com.chromebrothers.navegador.regras.MarcaDoElementoParaORoteiroDeAutomacao,
    ) {
        val candidatas = CandidatasDoElementoParaORoteiroDeAutomacao.ranquear(marca)
        if (candidatas.isEmpty()) {
            Toast.makeText(activity, "Sem candidatas para este elemento.", Toast.LENGTH_SHORT).show()
            limparAlvoDoPick()
            return
        }
        enriquecerCandidatasComContagem(candidatas) { comContagem ->
            mostrarEscolhaDeCandidatas(
                candidatas = comContagem,
                modo = modo,
                marca = marca,
            )
        }
    }

    private fun aplicarEscolhaDeCandidata(
        modo: ModoPick,
        marca: com.chromebrothers.navegador.regras.MarcaDoElementoParaORoteiroDeAutomacao,
        escolhida: CandidataDoElementoParaORoteiro,
    ) {
        when (modo) {
            ModoPick.Aguardar -> aplicarPasso(
                MontagemDoPassoDoRoteiroAPartirDaCandidata.aguardar(escolhida),
            )
            ModoPick.AguardarSumir -> aplicarPasso(
                MontagemDoPassoDoRoteiroAPartirDaCandidata.aguardarSumir(escolhida),
            )
            ModoPick.Clicar -> aplicarPasso(
                MontagemDoPassoDoRoteiroAPartirDaCandidata.clicar(escolhida),
            )
            ModoPick.Preencher -> pedirTexto(activity.getString(R.string.valor_do_preencher_no_gravador)) { valor ->
                aplicarPasso(
                    MontagemDoPassoDoRoteiroAPartirDaCandidata.preencher(escolhida, valor),
                )
            }
            ModoPick.ClicarSe -> {
                val seletor = escolhida.seletor.ifBlank {
                    "${marca.tag.ifBlank { "a" }}:nth-of-type(${marca.indiceNaTag + 1})"
                }
                val sugestao = marca.texto.ifBlank { escolhida.texto }
                pedirTexto(activity.getString(R.string.contem_do_clicar_se_no_gravador), sugestao) { contem ->
                    aplicarPasso(
                        MontagemDoPassoDoRoteiroAPartirDaCandidata.clicarSe(
                            seletorDaLista = seletor,
                            indice = 0,
                            contem = contem,
                            seNao = "parar",
                        ),
                    )
                }
            }
            ModoPick.Assertar -> pedirTexto(
                activity.getString(R.string.contem_do_assertar_no_gravador),
                "",
            ) { contem ->
                aplicarPasso(
                    MontagemDoPassoDoRoteiroAPartirDaCandidata.assertar(escolhida, contem),
                )
            }
            ModoPick.Extrair -> pedirTexto(
                activity.getString(R.string.nome_da_variavel_extrair_no_gravador),
                "valor",
            ) { nome ->
                if (!RoteiroDeAutomacaoDaPaginaDoNavegador.variavelValida(nome.trim())) {
                    Toast.makeText(activity, "Nome de variável inválido.", Toast.LENGTH_SHORT).show()
                } else {
                    aplicarPasso(
                        MontagemDoPassoDoRoteiroAPartirDaCandidata.extrair(escolhida, nome.trim()),
                    )
                }
            }
            ModoPick.AguardarQualquer -> {
                val id = idRamoAguardarQualquerPendente
                if (!RoteiroDeAutomacaoDaPaginaDoNavegador.variavelValida(id)) {
                    Toast.makeText(activity, "Id do ramo inválido.", Toast.LENGTH_SHORT).show()
                } else {
                    opcoesAguardarQualquerEmMontagem.add(
                        MontagemDoPassoDoRoteiroAPartirDaCandidata.opcaoAguardarQualquer(id, escolhida),
                    )
                    if (opcoesAguardarQualquerEmMontagem.size >= 2) {
                        // Pede mais ou fecha: com 2 já fecha; usuário pode regravar se quiser 3+.
                        aplicarPasso(
                            MontagemDoPassoDoRoteiroAPartirDaCandidata.aguardarQualquer(
                                opcoesAguardarQualquerEmMontagem.toList(),
                            ),
                        )
                        opcoesAguardarQualquerEmMontagem.clear()
                        idRamoAguardarQualquerPendente = ""
                    } else {
                        val proximo = if (opcoesAguardarQualquerEmMontagem.size == 1) "creditos" else "ramo"
                        pedirTexto(
                            activity.getString(R.string.id_do_ramo_aguardar_qualquer_no_gravador),
                            proximo,
                        ) { novoId ->
                            idRamoAguardarQualquerPendente = novoId.trim()
                            iniciarPick(ModoPick.AguardarQualquer, null)
                        }
                        limparAlvoDoPick()
                        return
                    }
                }
            }
        }
        limparAlvoDoPick()
    }

    private fun enriquecerCandidatasComContagem(
        candidatas: List<CandidataDoElementoParaORoteiro>,
        aoPronto: (List<CandidataDoElementoParaORoteiro>) -> Unit,
    ) {
        val pares = candidatas.map { it.seletor to it.texto }
        avaliarJavascript(ScriptsDoPickDeElementoParaOGravadorDeRoteiro.contarCandidatas(pares)) { bruto ->
            val json = textoDoJavascript(bruto)
            val contagens = ScriptsDoPickDeElementoParaOGravadorDeRoteiro.lerContagens(json)
            val enriquecidas = candidatas.mapIndexed { i, c ->
                val n = contagens.getOrNull(i)
                if (n == null) c else c.comMatches(n)
            }
            val ordenadas = enriquecidas.withIndex()
                .sortedWith(
                    compareBy(
                        { (_, c) ->
                            when (c.matches) {
                                1 -> 0
                                null -> 1
                                0 -> 3
                                else -> 2
                            }
                        },
                        { it.index },
                    ),
                )
                .map { it.value }
            aoPronto(ordenadas)
        }
    }

    private fun aplicarPasso(passo: PassoDoRoteiroDeAutomacao) {
        val alvo = indiceParaSubstituir
        indiceParaSubstituir = null
        if (alvo != null && alvo in passos.indices) {
            passos[alvo] = passo
        } else {
            passos += passo
        }
        redesenharLista()
    }

    private fun iniciarPick(modo: ModoPick, substituirEm: Int?) {
        cancelarPick()
        modoPick = modo
        indiceParaSubstituir = substituirEm
        avisoPick.visibility = View.VISIBLE
        avisoPick.setText(
            if (substituirEm != null) {
                activity.getString(R.string.substituir_passo_do_gravador) +
                    " ${substituirEm + 1}: toque no elemento…"
            } else {
                activity.getString(R.string.pick_toque_no_elemento_do_gravador)
            },
        )
        camadaPick.visibility = View.VISIBLE
        camadaPick.bringToFront()
        camadaPick.requestLayout()
        Toast.makeText(activity, avisoPick.text, Toast.LENGTH_SHORT).show()
    }

    /** Atalho p/ ChatGPT etc.: acha .ProseMirror / textbox sem depender do toque. */
    private fun acharEditorNaPaginaSemPick() {
        cancelarPick()
        modoPick = ModoPick.Preencher
        Toast.makeText(activity, "Procurando editor na página…", Toast.LENGTH_SHORT).show()
        avaliarJavascript(
            ScriptsDoPickDeElementoParaOGravadorDeRoteiro.marcaDoMelhorEditorVisivel(),
        ) { bruto ->
            android.util.Log.d("CbGravadorPick", "acharEditor bruto=${bruto?.take(240)}")
            val json = textoDoJavascript(bruto)
            if (json.startsWith("CB_ERR:")) {
                modoPick = null
                Toast.makeText(activity, "JS falhou: ${json.take(80)}", Toast.LENGTH_LONG).show()
                return@avaliarJavascript
            }
            if (json.isBlank()) {
                modoPick = null
                Toast.makeText(
                    activity,
                    "Não achei editor. (${bruto?.take(48) ?: "null"})",
                    Toast.LENGTH_LONG,
                ).show()
                return@avaliarJavascript
            }
            receberPick(json)
        }
    }

    private fun cancelarPick(manterAviso: Boolean = false) {
        modoPick = null
        if (!manterAviso) {
            avisoPick.visibility = View.GONE
            indiceParaSubstituir = null
        }
        camadaPick.visibility = View.INVISIBLE
        limparAlvoDoPick()
    }

    private fun limparAlvoDoPick() {
        avaliarJavascript(ScriptsDoPickDeElementoParaOGravadorDeRoteiro.limparAlvo(), null)
    }

    private fun tratarToqueDoPick(evento: MotionEvent): Boolean {
        if (modoPick == null) return false
        // Coordenadas da camada em px de view; o JS mapeia para CSS via innerWidth/Height.
        val xView = evento.x.toDouble()
        val yView = evento.y.toDouble()
        val pai = camadaPick.parent as? View
        val largura = when {
            camadaPick.width > 1 -> camadaPick.width
            pai != null && pai.width > 1 -> pai.width
            else -> 1
        }
        val altura = when {
            camadaPick.height > 1 -> camadaPick.height
            pai != null && pai.height > 1 -> pai.height
            else -> 1
        }
        when (evento.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                avaliarJavascript(
                    ScriptsDoPickDeElementoParaOGravadorDeRoteiro.destacarNoPonto(
                        xView,
                        yView,
                        largura,
                        altura,
                    ),
                    null,
                )
            }
            MotionEvent.ACTION_UP -> {
                avaliarJavascript(
                    ScriptsDoPickDeElementoParaOGravadorDeRoteiro.marcaNoPonto(
                        xView,
                        yView,
                        largura,
                        altura,
                    ),
                ) { bruto ->
                    android.util.Log.d("CbGravadorPick", "marcaNoPonto bruto=${bruto?.take(180)}")
                    var json = textoDoJavascript(bruto)
                    if (json.isBlank()) {
                        // Fallback: composer tipo ChatGPT (.ProseMirror) fora do hit-test.
                        avaliarJavascript(
                            ScriptsDoPickDeElementoParaOGravadorDeRoteiro.marcaDoMelhorEditorVisivel(),
                        ) { brutoEditor ->
                            android.util.Log.d("CbGravadorPick", "melhorEditor bruto=${brutoEditor?.take(180)}")
                            json = textoDoJavascript(brutoEditor)
                            if (json.isBlank()) {
                                Toast.makeText(
                                    activity,
                                    "Nada ali. (${brutoEditor?.take(40) ?: "null"})",
                                    Toast.LENGTH_LONG,
                                ).show()
                                cancelarPick()
                            } else {
                                receberPick(json)
                            }
                        }
                    } else {
                        receberPick(json)
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> cancelarPick()
        }
        return true
    }

    private fun mostrarEscolhaDeCandidatas(
        candidatas: List<CandidataDoElementoParaORoteiro>,
        modo: ModoPick,
        marca: com.chromebrothers.navegador.regras.MarcaDoElementoParaORoteiroDeAutomacao,
        veioSugestaoDaIa: Boolean = false,
    ) {
        dialogoAtivo?.dismiss()
        val densidade = activity.resources.displayMetrics.density
        val dialogo = Dialog(activity)
        val raizDlg = LayoutInflater.from(activity)
            .inflate(R.layout.layout_caixa_de_escolha_simples_do_navegador_chromebrothers, null)
        val tituloDlg = raizDlg.findViewById<TextView>(R.id.titulo_da_caixa_de_escolha_simples_do_navegador)
        val rotuloAlvo = buildString {
            append(activity.getString(R.string.escolha_a_candidata_do_gravador))
            val tag = marca.tag.ifBlank { "?" }
            val trecho = marca.texto.ifBlank { marca.ariaLabel }.ifBlank { marca.id }.take(28)
            append("\n")
            append("<$tag>")
            if (trecho.isNotEmpty()) append(" «$trecho»")
        }
        tituloDlg.text = rotuloAlvo
        val listaDlg = raizDlg.findViewById<LinearLayout>(
            R.id.lista_de_opcoes_da_caixa_de_escolha_simples_do_navegador,
        )
        if (veioSugestaoDaIa) {
            listaDlg.addView(
                TextView(activity).apply {
                    text = activity.getString(R.string.banner_sugestao_ia_do_gravador)
                    setTextColor(Color.parseColor("#FDD663"))
                    textSize = 14f
                    setPadding((8 * densidade).toInt(), (8 * densidade).toInt(), (8 * densidade).toInt(), (8 * densidade).toInt())
                },
            )
        }
        listaDlg.addView(montarCruzDirecionalDoAlvo(densidade) { direcao ->
            avaliarJavascript(ScriptsDoPickDeElementoParaOGravadorDeRoteiro.navegarAlvo(direcao)) { bruto ->
                val json = textoDoJavascript(bruto)
                if (json.isBlank()) {
                    Toast.makeText(activity, R.string.sem_vizinho_do_gravador, Toast.LENGTH_SHORT).show()
                    return@avaliarJavascript
                }
                val novaMarca = CandidatasDoElementoParaORoteiroDeAutomacao.lerMarca(json) ?: run {
                    Toast.makeText(activity, R.string.sem_vizinho_do_gravador, Toast.LENGTH_SHORT).show()
                    return@avaliarJavascript
                }
                dialogo.dismiss()
                abrirEscolhaDeCandidatas(modo, novaMarca)
            }
        })
        val statusIa = TextView(activity).apply {
            text = activity.getString(R.string.dica_pedir_ia_do_gravador)
            setTextColor(Color.parseColor("#8A9099"))
            textSize = 12f
            setPadding((8 * densidade).toInt(), 0, (8 * densidade).toInt(), (4 * densidade).toInt())
        }
        val pedirIa = TextView(activity).apply {
            text = activity.getString(R.string.pedir_candidata_a_ia_do_gravador)
            setTextColor(Color.parseColor("#FDD663"))
            textSize = 15f
            setPadding((8 * densidade).toInt(), (4 * densidade).toInt(), (8 * densidade).toInt(), (8 * densidade).toInt())
            setOnClickListener {
                if (!temChaveDaOpenRouter()) {
                    Toast.makeText(activity, R.string.faltou_chave_openrouter_no_gravador, Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                pedirTexto(activity.getString(R.string.intencao_da_candidata_para_ia_do_gravador), "") { intencao ->
                    if (intencao.isBlank()) {
                        Toast.makeText(activity, "Descreva o que quer selecionar.", Toast.LENGTH_SHORT).show()
                        return@pedirTexto
                    }
                    isEnabled = false
                    setTextColor(Color.parseColor("#8A9099"))
                    text = activity.getString(R.string.consultando_ia_do_gravador)
                    statusIa.setTextColor(Color.parseColor("#FDD663"))
                    statusIa.text = activity.getString(R.string.consultando_ia_do_gravador)
                    tituloDlg.text = activity.getString(R.string.consultando_ia_do_gravador)
                    consultarIaParaCandidata(modo, marca, candidatas, intencao.trim(), dialogo) {
                        isEnabled = true
                        setTextColor(Color.parseColor("#FDD663"))
                        text = activity.getString(R.string.pedir_candidata_a_ia_do_gravador)
                        statusIa.setTextColor(Color.parseColor("#8A9099"))
                        statusIa.text = activity.getString(R.string.dica_pedir_ia_do_gravador)
                        tituloDlg.text = rotuloAlvo
                    }
                }
            }
        }
        listaDlg.addView(pedirIa)
        listaDlg.addView(statusIa)
        fun aoEscolher(escolhida: CandidataDoElementoParaORoteiro) {
            aplicarEscolhaDeCandidata(modo, marca, escolhida)
        }
        candidatas.forEachIndexed { indice, candidata ->
            val destaqueIa = veioSugestaoDaIa && indice == 0
            val linha = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding((8 * densidade).toInt(), (6 * densidade).toInt(), (8 * densidade).toInt(), (6 * densidade).toInt())
                if (destaqueIa) setBackgroundColor(Color.parseColor("#2A3340"))
            }
            val titulo = TextView(activity).apply {
                text = if (destaqueIa && !candidata.rotulo.startsWith("IA") && !candidata.rotulo.startsWith("★")) {
                    "★ ${candidata.rotulo}"
                } else {
                    candidata.rotulo
                }
                setTextColor(if (destaqueIa) Color.parseColor("#FDD663") else Color.parseColor("#F4F7FB"))
                textSize = if (destaqueIa) 17f else 16f
            }
            val detalhe = TextView(activity).apply {
                text = candidata.seletor.ifBlank { "texto: ${candidata.texto}" }
                setTextColor(Color.parseColor("#8A9099"))
                textSize = 12f
            }
            val acoes = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
            val ver = TextView(activity).apply {
                text = activity.getString(R.string.preview_candidata_do_gravador)
                setTextColor(Color.parseColor("#8AB4F8"))
                setPadding(0, (4 * densidade).toInt(), (16 * densidade).toInt(), 0)
                setOnClickListener {
                    avaliarJavascript(
                        ScriptsDoPickDeElementoParaOGravadorDeRoteiro.destacarCandidata(
                            candidata.seletor,
                            candidata.texto,
                        ),
                        null,
                    )
                }
            }
            val usar = TextView(activity).apply {
                text = activity.getString(R.string.confirmar_candidata_do_gravador)
                setTextColor(Color.parseColor("#81C995"))
                setPadding(0, (4 * densidade).toInt(), 0, 0)
                setOnClickListener {
                    dialogo.dismiss()
                    aoEscolher(candidata)
                }
            }
            acoes.addView(ver)
            acoes.addView(usar)
            linha.addView(titulo)
            linha.addView(detalhe)
            linha.addView(acoes)
            linha.setOnClickListener {
                avaliarJavascript(
                    ScriptsDoPickDeElementoParaOGravadorDeRoteiro.destacarCandidata(
                        candidata.seletor,
                        candidata.texto,
                    ),
                    null,
                )
            }
            listaDlg.addView(linha)
        }
        val manual = TextView(activity).apply {
            text = activity.getString(R.string.seletor_manual_do_gravador)
            setTextColor(Color.parseColor("#FDD663"))
            textSize = 15f
            setPadding((8 * densidade).toInt(), (12 * densidade).toInt(), (8 * densidade).toInt(), (8 * densidade).toInt())
            setOnClickListener {
                dialogo.dismiss()
                pedirTexto(activity.getString(R.string.digite_seletor_manual_do_gravador), "") { css ->
                    val seletor = css.trim()
                    if (seletor.isEmpty()) {
                        Toast.makeText(activity, "Seletor vazio.", Toast.LENGTH_SHORT).show()
                        return@pedirTexto
                    }
                    aoEscolher(
                        CandidataDoElementoParaORoteiro(
                            rotulo = "manual",
                            seletor = seletor,
                            texto = "",
                        ),
                    )
                }
            }
        }
        listaDlg.addView(manual)
        val cancelar = raizDlg.findViewById<Button>(R.id.botao_cancelar_da_caixa_de_escolha_simples_do_navegador)
        cancelar.backgroundTintList = null
        cancelar.stateListAnimator = null
        cancelar.setOnClickListener {
            cancelarConsultaIa()
            limparAlvoDoPick()
            dialogo.dismiss()
        }
        dialogo.setOnCancelListener {
            cancelarConsultaIa()
            limparAlvoDoPick()
        }
        dialogo.setOnDismissListener {
            if (dialogoAtivo === dialogo) dialogoAtivo = null
        }
        dialogo.setContentView(raizDlg)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogoAtivo = dialogo
        dialogo.show()
        dialogo.window?.setLayout((420 * densidade).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        // Prefere preview da primeira única; senão a primeira da lista
        val preview = candidatas.firstOrNull { it.matches == 1 } ?: candidatas.firstOrNull()
        preview?.let {
            avaliarJavascript(
                ScriptsDoPickDeElementoParaOGravadorDeRoteiro.destacarCandidata(it.seletor, it.texto),
                null,
            )
        }
    }

    private fun limparPreview() {
        avaliarJavascript(ScriptsDoPickDeElementoParaOGravadorDeRoteiro.limparDestaque(), null)
    }

    private fun consultarIaParaCandidata(
        modo: ModoPick,
        marca: com.chromebrothers.navegador.regras.MarcaDoElementoParaORoteiroDeAutomacao,
        candidatas: List<CandidataDoElementoParaORoteiro>,
        intencao: String,
        dialogoAtual: Dialog,
        aoFalharOuCancelarUi: () -> Unit,
    ) {
        avaliarJavascript(
            ScriptsDoPickDeElementoParaOGravadorDeRoteiro.trechoDomDoAlvo(
                EscolhaDeCandidataPelaIaDoGravadorDeRoteiro.LIMITE_DO_TRECHO_DOM,
            ),
        ) { bruto ->
            val trecho = textoDoJavascript(bruto)
            val mensagens = EscolhaDeCandidataPelaIaDoGravadorDeRoteiro.mensagens(
                intencao = intencao,
                url = urlDaAba(),
                marca = marca,
                candidatas = candidatas,
                trechoDom = trecho,
            )
            consultarIa(mensagens) { texto, erro ->
                if (erro != null) {
                    aoFalharOuCancelarUi()
                    Toast.makeText(activity, erro.take(160), Toast.LENGTH_LONG).show()
                    return@consultarIa
                }
                when (
                    val resultado = EscolhaDeCandidataPelaIaDoGravadorDeRoteiro.interpretar(
                        texto.orEmpty(),
                        candidatas,
                    )
                ) {
                    is ResultadoDaEscolhaDeCandidataPelaIa.Erro -> {
                        aoFalharOuCancelarUi()
                        Toast.makeText(activity, resultado.mensagem, Toast.LENGTH_LONG).show()
                    }
                    is ResultadoDaEscolhaDeCandidataPelaIa.Candidata -> {
                        val sugestao = resultado.candidata.let { c ->
                            if (c.rotulo.startsWith("IA ·") || c.rotulo.startsWith("★")) c
                            else c.copy(rotulo = "★ IA · ${c.rotulo}")
                        }
                        val resto = candidatas.filterNot {
                            it.seletor == sugestao.seletor && it.texto == sugestao.texto
                        }
                        if (dialogoAtivo === dialogoAtual) {
                            dialogoAtual.dismiss()
                        }
                        enriquecerCandidatasComContagem(listOf(sugestao) + resto) { lista ->
                            mostrarEscolhaDeCandidatas(
                                candidatas = lista,
                                modo = modo,
                                marca = marca,
                                veioSugestaoDaIa = true,
                            )
                            Toast.makeText(
                                activity,
                                R.string.sugestao_da_ia_do_gravador,
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun montarCruzDirecionalDoAlvo(
        densidade: Float,
        aoMover: (direcao: String) -> Unit,
    ): View {
        val pad = (6 * densidade).toInt()
        fun botao(rotulo: String, direcao: String): TextView =
            TextView(activity).apply {
                text = rotulo
                setTextColor(Color.parseColor("#8AB4F8"))
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(pad * 2, pad, pad * 2, pad)
                setOnClickListener { aoMover(direcao) }
            }
        val coluna = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(pad, pad, pad, pad)
        }
        val titulo = TextView(activity).apply {
            text = activity.getString(R.string.ajuste_o_alvo_do_gravador)
            setTextColor(Color.parseColor("#C5C8CE"))
            textSize = 12f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, pad)
        }
        val linhaMeio = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            addView(botao("←", "left"))
            addView(botao(activity.getString(R.string.mover_alvo_pai_do_gravador), "pai"))
            addView(botao("→", "right"))
        }
        coluna.addView(titulo)
        coluna.addView(botao("↑", "up"))
        coluna.addView(linhaMeio)
        coluna.addView(botao("↓", "down"))
        return coluna
    }

    private fun reparentarFaixa(modo: String) {
        val pai = faixa.parent as? ViewGroup
        pai?.removeView(faixa)
        if (modo == "gaveta") {
            hostGaveta.addView(
                faixa,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                ),
            )
            faixa.setPadding(0, 0, 0, 0)
            (faixa.layoutParams as? FrameLayout.LayoutParams)?.gravity = android.view.Gravity.FILL
            rolagemPassos.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
        } else {
            raiz.addView(
                faixa,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    android.view.Gravity.BOTTOM,
                ),
            )
            rolagemPassos.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (120 * activity.resources.displayMetrics.density).toInt(),
            )
        }
    }

    private fun confirmarUrlInicial() {
        val url = campoUrl.text?.toString()?.trim().orEmpty()
        if (url.isEmpty() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            indiceParaSubstituir = null
            Toast.makeText(activity, "Informe uma URL http(s).", Toast.LENGTH_SHORT).show()
            return
        }
        val atual = urlDaAba().trim()
        val passo = MontagemDoPassoDoRoteiroAPartirDaCandidata.abrir(url)
        val substituindo = indiceParaSubstituir != null
        if (substituindo) {
            aplicarPasso(passo)
        } else {
            passos.removeAll { it is PassoDoRoteiroDeAutomacao.Abrir }
            passos.add(0, passo)
            redesenharLista()
        }
        if (atual != url) navegar(url)
        Toast.makeText(activity, "Passo abrir pronto.", Toast.LENGTH_SHORT).show()
    }

    private fun pedirEsperarMs(substituirEm: Int? = null) {
        indiceParaSubstituir = substituirEm
        pedirTexto(activity.getString(R.string.ms_do_esperar_no_gravador), "500") { bruto ->
            val ms = bruto.toIntOrNull()
            if (ms == null || ms < 1) {
                indiceParaSubstituir = null
                Toast.makeText(activity, "ms inválido.", Toast.LENGTH_SHORT).show()
            } else {
                aplicarPasso(MontagemDoPassoDoRoteiroAPartirDaCandidata.esperarMs(ms))
            }
        }
    }

    private fun pedirRolar(substituirEm: Int? = null) {
        indiceParaSubstituir = substituirEm
        pedirTexto(activity.getString(R.string.pixels_do_rolar_no_gravador), "400") { bruto ->
            val px = bruto.toIntOrNull()
            if (px == null || px == 0) {
                indiceParaSubstituir = null
                Toast.makeText(activity, "pixels inválidos.", Toast.LENGTH_SHORT).show()
            } else {
                aplicarPasso(MontagemDoPassoDoRoteiroAPartirDaCandidata.rolar(px))
            }
        }
    }

    private fun pedirAguardarUrl(substituirEm: Int? = null) {
        indiceParaSubstituir = substituirEm
        pedirTexto(activity.getString(R.string.contem_da_url_no_gravador), "") { contem ->
            if (contem.isBlank()) {
                indiceParaSubstituir = null
                Toast.makeText(activity, "Informe o trecho da URL.", Toast.LENGTH_SHORT).show()
            } else {
                aplicarPasso(MontagemDoPassoDoRoteiroAPartirDaCandidata.aguardarUrl(contem))
            }
        }
    }

    private fun iniciarAguardarQualquer() {
        opcoesAguardarQualquerEmMontagem.clear()
        pedirTexto(
            activity.getString(R.string.id_do_ramo_aguardar_qualquer_no_gravador),
            "livre",
        ) { id ->
            if (!RoteiroDeAutomacaoDaPaginaDoNavegador.variavelValida(id.trim())) {
                Toast.makeText(activity, "Id do ramo inválido.", Toast.LENGTH_SHORT).show()
            } else {
                idRamoAguardarQualquerPendente = id.trim()
                Toast.makeText(
                    activity,
                    activity.getString(R.string.pick_ramo_aguardar_qualquer_no_gravador, id.trim()),
                    Toast.LENGTH_SHORT,
                ).show()
                iniciarPick(ModoPick.AguardarQualquer, null)
            }
        }
    }

    private fun pedirMarcar() {
        pedirTexto(activity.getString(R.string.id_do_marcar_no_gravador), "fluxo_a") { id ->
            if (!RoteiroDeAutomacaoDaPaginaDoNavegador.variavelValida(id.trim())) {
                Toast.makeText(activity, "Id inválido.", Toast.LENGTH_SHORT).show()
            } else {
                aplicarPasso(MontagemDoPassoDoRoteiroAPartirDaCandidata.marcar(id.trim()))
            }
        }
    }

    private fun pedirIrPara() {
        if (passos.isEmpty()) {
            Toast.makeText(activity, "Grave um passo (ou marcar) antes de saltar.", Toast.LENGTH_SHORT).show()
            return
        }
        val opcoes = mutableListOf<Pair<String, String>>()
        passos.forEachIndexed { i, p ->
            if (p is PassoDoRoteiroDeAutomacao.Marcar) {
                opcoes.add("marcar «${p.id}»" to "d:${p.id}")
            }
            opcoes.add("${i + 1}. ${rotuloDoPasso(p)}" to "p:${i + 1}")
        }
        mostrarEscolhaSimples(
            activity.getString(R.string.destino_do_ir_para_no_gravador),
            opcoes,
            -1,
        ) { chave ->
            val passoN = if (chave.startsWith("p:")) chave.removePrefix("p:").toIntOrNull() ?: 0 else 0
            val destino = if (chave.startsWith("d:")) chave.removePrefix("d:") else ""
            pedirTexto(
                activity.getString(R.string.se_do_ir_para_no_gravador),
                "{{_ramo}}=",
            ) { se ->
                aplicarPasso(
                    MontagemDoPassoDoRoteiroAPartirDaCandidata.irPara(
                        passo = passoN,
                        destino = destino,
                        se = se.trim(),
                    ),
                )
            }
        }
    }

    private fun abrirListaDeSalvos() {
        val salvos = listarRoteiros()
        if (salvos.isEmpty()) {
            Toast.makeText(activity, "Nenhum roteiro salvo.", Toast.LENGTH_SHORT).show()
            return
        }
        mostrarEscolhaSimples(
            activity.getString(R.string.abrir_roteiro_salvo_no_gravador),
            salvos.map { "${it.nome} (${it.passos.size})" to it.id },
            -1,
        ) { id ->
            val roteiro = salvos.firstOrNull { it.id == id } ?: return@mostrarEscolhaSimples
            menuDoRoteiroSalvo(roteiro)
        }
    }

    private fun menuDoRoteiroSalvo(roteiro: RoteiroDeAutomacaoDaPagina) {
        mostrarEscolhaSimples(
            activity.getString(R.string.gestao_do_roteiro_salvo_no_gravador, roteiro.nome),
            listOf(
                activity.getString(R.string.abrir_roteiro_salvo_acao_no_gravador) to "abrir",
                activity.getString(R.string.renomear_roteiro_salvo_no_gravador) to "renomear",
                activity.getString(R.string.duplicar_roteiro_salvo_no_gravador) to "duplicar",
                activity.getString(R.string.apagar_roteiro_salvo_no_gravador) to "apagar",
            ),
            -1,
        ) { acao ->
            when (acao) {
                "abrir" -> carregarRoteiro(roteiro)
                "renomear" -> pedirRenomearRoteiroSalvo(roteiro)
                "duplicar" -> duplicarRoteiroSalvo(roteiro)
                "apagar" -> confirmarApagarRoteiroSalvo(roteiro)
            }
        }
    }

    private fun pedirRenomearRoteiroSalvo(roteiro: RoteiroDeAutomacaoDaPagina) {
        pedirTexto(
            activity.getString(R.string.nome_do_roteiro_no_gravador),
            roteiro.nome,
        ) { bruto ->
            val novo = GestaoDeRoteirosSalvosDeAutomacaoDaPagina.renomear(roteiro, bruto)
            if (novo == null) {
                Toast.makeText(activity, "Nome inválido.", Toast.LENGTH_SHORT).show()
                return@pedirTexto
            }
            val relato = salvar(novo)
            if (idDoRoteiro == roteiro.id) {
                campoNome.setText(novo.nome)
            }
            Toast.makeText(activity, relato, Toast.LENGTH_SHORT).show()
        }
    }

    private fun duplicarRoteiroSalvo(roteiro: RoteiroDeAutomacaoDaPagina) {
        val ids = listarRoteiros().map { it.id }.toSet()
        val copia = GestaoDeRoteirosSalvosDeAutomacaoDaPagina.duplicar(roteiro, ids)
        val relato = salvar(copia)
        carregarRoteiro(copia)
        Toast.makeText(activity, relato, Toast.LENGTH_SHORT).show()
    }

    private fun confirmarApagarRoteiroSalvo(roteiro: RoteiroDeAutomacaoDaPagina) {
        mostrarEscolhaSimples(
            activity.getString(R.string.confirmar_apagar_roteiro_salvo_no_gravador, roteiro.nome),
            listOf(
                activity.getString(R.string.apagar_roteiro_salvo_confirmar_sim) to "sim",
                activity.getString(R.string.apagar_roteiro_salvo_confirmar_nao) to "nao",
            ),
            1,
        ) { escolha ->
            if (escolha != "sim") return@mostrarEscolhaSimples
            if (!apagarRoteiro(roteiro.id)) {
                Toast.makeText(activity, "Não consegui apagar.", Toast.LENGTH_SHORT).show()
                return@mostrarEscolhaSimples
            }
            if (idDoRoteiro == roteiro.id) {
                limparParaNovo()
            }
            Toast.makeText(
                activity,
                activity.getString(R.string.roteiro_salvo_apagado_no_gravador, roteiro.nome),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun carregarRoteiro(roteiro: RoteiroDeAutomacaoDaPagina) {
        idDoRoteiro = roteiro.id
        campoNome.setText(roteiro.nome)
        val urlAbrir = (roteiro.passos.firstOrNull { it is PassoDoRoteiroDeAutomacao.Abrir }
            as? PassoDoRoteiroDeAutomacao.Abrir)?.url
        if (!urlAbrir.isNullOrBlank()) campoUrl.setText(urlAbrir)
        passos.clear()
        passos.addAll(roteiro.passos)
        redesenharLista()
        Toast.makeText(activity, "Carreguei «${roteiro.nome}».", Toast.LENGTH_SHORT).show()
    }

    private fun limparParaNovo() {
        idDoRoteiro = null
        passos.clear()
        campoNome.setText("Roteiro")
        campoUrl.setText(urlDaAba())
        redesenharLista()
        Toast.makeText(activity, R.string.novo_roteiro_no_gravador, Toast.LENGTH_SHORT).show()
    }

    private fun pedirTexto(titulo: String, inicial: String = "", aoOk: (String) -> Unit) {
        dialogoAtivo?.dismiss()
        val densidade = activity.resources.displayMetrics.density
        val dialogo = Dialog(activity)
        val raizDlg = LayoutInflater.from(activity)
            .inflate(R.layout.layout_caixa_de_escolha_simples_do_navegador_chromebrothers, null)
        raizDlg.findViewById<TextView>(R.id.titulo_da_caixa_de_escolha_simples_do_navegador).text = titulo
        val listaDlg = raizDlg.findViewById<LinearLayout>(
            R.id.lista_de_opcoes_da_caixa_de_escolha_simples_do_navegador,
        )
        val campo = EditText(activity).apply {
            setText(inicial)
            setTextColor(Color.parseColor("#F4F7FB"))
            setHintTextColor(Color.parseColor("#8A9099"))
            setSelection(text.length)
        }
        listaDlg.addView(campo)
        val ok = Button(activity).apply {
            text = activity.getString(android.R.string.ok)
            backgroundTintList = null
            stateListAnimator = null
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.parseColor("#8AB4F8"))
            isAllCaps = false
            setOnClickListener {
                dialogo.dismiss()
                aoOk(campo.text?.toString().orEmpty())
            }
        }
        listaDlg.addView(ok)
        val cancelar = raizDlg.findViewById<Button>(R.id.botao_cancelar_da_caixa_de_escolha_simples_do_navegador)
        cancelar.backgroundTintList = null
        cancelar.stateListAnimator = null
        cancelar.setOnClickListener { dialogo.dismiss() }
        dialogo.setContentView(raizDlg)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogoAtivo = dialogo
        dialogo.show()
        dialogo.window?.setLayout((420 * densidade).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun roteiroAtual(): RoteiroDeAutomacaoDaPagina? {
        val nome = campoNome.text?.toString()?.trim().orEmpty()
        if (nome.isEmpty()) {
            Toast.makeText(activity, "Faltou o nome.", Toast.LENGTH_SHORT).show()
            return null
        }
        if (passos.isEmpty()) {
            Toast.makeText(activity, "Adicione pelo menos um passo.", Toast.LENGTH_SHORT).show()
            return null
        }
        val id = idDoRoteiro?.takeIf { it.isNotBlank() }
            ?: RoteiroDeAutomacaoDaPaginaDoNavegador.slugDoNome(nome)
        return RoteiroDeAutomacaoDaPagina(id = id, nome = nome, passos = passos.toList())
    }

    private fun play(de: Int = 0, ateExclusivo: Int? = null) {
        val completo = roteiroAtual() ?: return
        val fim = ateExclusivo ?: completo.passos.size
        if (de !in completo.passos.indices || de >= fim) return
        val fatia = completo.copy(passos = completo.passos.subList(de, fim))
        val nomes = VariaveisDoRoteiroDeAutomacaoDaPagina.nomesNosPassos(fatia.passos)
        pedirVariaveisEmCadeia(nomes, emptyMap()) { mapa ->
            iniciarExecucao(completo, fatia, de, mapa)
        }
    }

    private fun pedirVariaveisEmCadeia(
        nomes: List<String>,
        acumulado: Map<String, String>,
        aoPronto: (Map<String, String>) -> Unit,
    ) {
        if (nomes.isEmpty()) {
            aoPronto(acumulado)
            return
        }
        val resto = nomes.toMutableList()
        val nome = resto.removeAt(0)
        pedirTexto(
            activity.getString(R.string.variavel_do_roteiro_no_gravador, nome),
            "",
        ) { valor ->
            pedirVariaveisEmCadeia(resto, acumulado + (nome to valor), aoPronto)
        }
    }

    private fun iniciarExecucao(
        completo: RoteiroDeAutomacaoDaPagina,
        fatia: RoteiroDeAutomacaoDaPagina,
        de: Int,
        variaveis: Map<String, String>,
    ) {
        Toast.makeText(activity, R.string.chat_rodando_roteiro, Toast.LENGTH_SHORT).show()
        executando = true
        passoEmExecucao = de
        atualizarBotaoPlay()
        redesenharLista()
        rodar(
            fatia,
            variaveis,
            { indiceRelativo, _, rotulo ->
                val indice = de + indiceRelativo
                passoEmExecucao = indice
                avisoPick.visibility = View.VISIBLE
                avisoPick.text = activity.getString(
                    R.string.passo_em_execucao_do_gravador,
                    indice + 1,
                    completo.passos.size,
                ) + " " + rotulo
                redesenharLista()
                rolagemPassos.post {
                    val filho = lista.getChildAt(indice) ?: return@post
                    rolagemPassos.smoothScrollTo(0, filho.top)
                }
            },
            { relato ->
                executando = false
                passoEmExecucao = -1
                atualizarBotaoPlay()
                avisoPick.visibility = View.GONE
                redesenharLista()
                Toast.makeText(activity, relato.take(180), Toast.LENGTH_LONG).show()
            },
        )
    }

    private fun parar() {
        cancelarExecucao()
        executando = false
        passoEmExecucao = -1
        atualizarBotaoPlay()
        avisoPick.visibility = View.GONE
        redesenharLista()
    }

    private fun atualizarBotaoPlay() {
        if (executando) {
            botaoPlay.setImageResource(R.drawable.icone_de_parar_resposta_do_chat_da_openrouter)
            botaoPlay.contentDescription = activity.getString(R.string.parar_roteiro_no_gravador)
        } else {
            botaoPlay.setImageResource(R.drawable.icone_de_reproduzir_do_player_de_video)
            botaoPlay.contentDescription = activity.getString(R.string.rodar_roteiro_de_automacao_do_chat)
        }
    }

    private fun salvarAgora() {
        val roteiro = roteiroAtual() ?: return
        val msg = salvar(roteiro)
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
    }

    private fun enviarAoChat() {
        val roteiro = roteiroAtual() ?: return
        val json = RoteiroDeAutomacaoDaPaginaDoNavegador.escrever(roteiro)
        fechar(manterGaveta = true)
        enviarJsonAoChat(json)
        abrirGavetaDoChat()
        Toast.makeText(activity, "JSON enviado ao chat.", Toast.LENGTH_SHORT).show()
    }

    private fun redesenharLista() {
        lista.removeAllViews()
        passos.forEachIndexed { indice, passo ->
            val ativo = indice == passoEmExecucao
            val linha = TextView(activity).apply {
                text = if (ativo) {
                    "▶ ${indice + 1}. ${rotuloDoPasso(passo)}"
                } else {
                    "${indice + 1}. ${rotuloDoPasso(passo)}"
                }
                setTextColor(
                    if (ativo) Color.parseColor("#8AB4F8") else Color.parseColor("#F4F7FB"),
                )
                textSize = if (ativo) 14f else 13f
                setPadding(0, 8, 0, 8)
                setOnClickListener { menuDoPasso(indice) }
            }
            lista.addView(linha)
        }
    }

    private fun menuDoPasso(indice: Int) {
        mostrarEscolhaSimples(
            "Passo ${indice + 1}",
            listOf(
                activity.getString(R.string.rodar_este_passo_do_gravador) to "rodar_este",
                activity.getString(R.string.rodar_a_partir_deste_passo_do_gravador) to "rodar_daqui",
                activity.getString(R.string.editar_passo_do_gravador) to "editar",
                activity.getString(R.string.substituir_passo_do_gravador) to "substituir",
                "Subir" to "subir",
                "Descer" to "descer",
                "Remover" to "remover",
            ),
            -1,
        ) { opcao ->
            when (opcao) {
                "rodar_este" -> play(de = indice, ateExclusivo = indice + 1)
                "rodar_daqui" -> play(de = indice)
                "editar" -> editarParametrosDoPasso(indice)
                "substituir" -> pedirTipoParaSubstituir(indice)
                "subir" -> if (indice > 0) {
                    val t = passos.removeAt(indice)
                    passos.add(indice - 1, t)
                    redesenharLista()
                }
                "descer" -> if (indice < passos.lastIndex) {
                    val t = passos.removeAt(indice)
                    passos.add(indice + 1, t)
                    redesenharLista()
                }
                "remover" -> {
                    passos.removeAt(indice)
                    redesenharLista()
                }
            }
        }
    }

    private fun editarParametrosDoPasso(indice: Int) {
        if (indice !in passos.indices) return
        when (val passo = passos[indice]) {
            is PassoDoRoteiroDeAutomacao.Abrir ->
                pedirTexto(activity.getString(R.string.url_inicial_do_gravador_de_roteiro), passo.url) { url ->
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        passos[indice] = passo.copy(url = url.trim())
                        campoUrl.setText(url.trim())
                        redesenharLista()
                    } else {
                        Toast.makeText(activity, "URL inválida.", Toast.LENGTH_SHORT).show()
                    }
                }
            is PassoDoRoteiroDeAutomacao.Aguardar ->
                pedirTexto(
                    activity.getString(R.string.timeout_ms_do_passo_no_gravador),
                    passo.timeoutMs.toString(),
                ) { bruto ->
                    val ms = bruto.toIntOrNull()
                    if (ms == null || ms < 200) {
                        Toast.makeText(activity, "Timeout inválido.", Toast.LENGTH_SHORT).show()
                    } else {
                        passos[indice] = passo.copy(
                            timeoutMs = ms.coerceIn(200, RoteiroDeAutomacaoDaPaginaDoNavegador.TIMEOUT_MAX_MS),
                        )
                        redesenharLista()
                    }
                }
            is PassoDoRoteiroDeAutomacao.AguardarSumir ->
                pedirTexto(
                    activity.getString(R.string.timeout_ms_do_passo_no_gravador),
                    passo.timeoutMs.toString(),
                ) { bruto ->
                    val ms = bruto.toIntOrNull()
                    if (ms == null || ms < 200) {
                        Toast.makeText(activity, "Timeout inválido.", Toast.LENGTH_SHORT).show()
                    } else {
                        passos[indice] = passo.copy(
                            timeoutMs = ms.coerceIn(200, RoteiroDeAutomacaoDaPaginaDoNavegador.TIMEOUT_MAX_MS),
                        )
                        redesenharLista()
                    }
                }
            is PassoDoRoteiroDeAutomacao.AguardarUrl ->
                pedirTexto(activity.getString(R.string.contem_da_url_no_gravador), passo.contem) { contem ->
                    if (contem.isBlank()) {
                        Toast.makeText(activity, "Trecho vazio.", Toast.LENGTH_SHORT).show()
                    } else {
                        passos[indice] = passo.copy(contem = contem.trim())
                        redesenharLista()
                    }
                }
            is PassoDoRoteiroDeAutomacao.Preencher ->
                pedirTexto(activity.getString(R.string.valor_do_preencher_no_gravador), passo.valor) { valor ->
                    passos[indice] = passo.copy(valor = valor)
                    redesenharLista()
                }
            is PassoDoRoteiroDeAutomacao.EsperarMs ->
                pedirTexto(activity.getString(R.string.ms_do_esperar_no_gravador), passo.ms.toString()) { bruto ->
                    val ms = bruto.toIntOrNull()
                    if (ms == null || ms < 1) {
                        Toast.makeText(activity, "ms inválido.", Toast.LENGTH_SHORT).show()
                    } else {
                        passos[indice] = MontagemDoPassoDoRoteiroAPartirDaCandidata.esperarMs(ms)
                        redesenharLista()
                    }
                }
            is PassoDoRoteiroDeAutomacao.Rolar ->
                pedirTexto(activity.getString(R.string.pixels_do_rolar_no_gravador), passo.pixels.toString()) { bruto ->
                    val px = bruto.toIntOrNull()
                    if (px == null || px == 0) {
                        Toast.makeText(activity, "pixels inválidos.", Toast.LENGTH_SHORT).show()
                    } else {
                        passos[indice] = MontagemDoPassoDoRoteiroAPartirDaCandidata.rolar(px)
                        redesenharLista()
                    }
                }
            is PassoDoRoteiroDeAutomacao.ClicarSe ->
                pedirTexto(activity.getString(R.string.contem_do_clicar_se_no_gravador), passo.contem) { contem ->
                    mostrarEscolhaSimples(
                        activity.getString(R.string.se_nao_do_clicar_se_no_gravador),
                        listOf("Parar" to "parar", "Seguir" to "seguir"),
                        if (passo.seNao == "seguir") 1 else 0,
                    ) { seNao ->
                        pedirTexto("Índice (0=primeiro)", passo.indice.toString()) { bruto ->
                            val idx = bruto.toIntOrNull() ?: 0
                            passos[indice] = passo.copy(
                                contem = contem.trim(),
                                seNao = seNao,
                                indice = idx.coerceIn(0, 50),
                            )
                            redesenharLista()
                        }
                    }
                }
            is PassoDoRoteiroDeAutomacao.Assertar ->
                pedirTexto(
                    activity.getString(R.string.contem_do_assertar_no_gravador),
                    passo.contem,
                ) { contem ->
                    passos[indice] = passo.copy(contem = contem.trim())
                    redesenharLista()
                }
            is PassoDoRoteiroDeAutomacao.Extrair ->
                pedirTexto(
                    activity.getString(R.string.nome_da_variavel_extrair_no_gravador),
                    passo.variavel,
                ) { nome ->
                    if (!RoteiroDeAutomacaoDaPaginaDoNavegador.variavelValida(nome.trim())) {
                        Toast.makeText(activity, "Nome de variável inválido.", Toast.LENGTH_SHORT).show()
                    } else {
                        passos[indice] = passo.copy(variavel = nome.trim())
                        redesenharLista()
                    }
                }
            is PassoDoRoteiroDeAutomacao.Clicar ->
                Toast.makeText(
                    activity,
                    "Clicar não tem parâmetro editável — use Substituir para regravar.",
                    Toast.LENGTH_SHORT,
                ).show()
            is PassoDoRoteiroDeAutomacao.AguardarQualquer ->
                pedirTexto(
                    activity.getString(R.string.timeout_ms_do_passo_no_gravador),
                    passo.timeoutMs.toString(),
                ) { bruto ->
                    val ms = bruto.toIntOrNull()
                    if (ms == null || ms < 200) {
                        Toast.makeText(activity, "Timeout inválido.", Toast.LENGTH_SHORT).show()
                    } else {
                        passos[indice] = passo.copy(
                            timeoutMs = ms.coerceIn(
                                200,
                                RoteiroDeAutomacaoDaPaginaDoNavegador.TIMEOUT_MAX_MS,
                            ),
                        )
                        redesenharLista()
                    }
                }
            is PassoDoRoteiroDeAutomacao.Marcar ->
                pedirTexto(activity.getString(R.string.id_do_marcar_no_gravador), passo.id) { id ->
                    if (!RoteiroDeAutomacaoDaPaginaDoNavegador.variavelValida(id.trim())) {
                        Toast.makeText(activity, "Id inválido.", Toast.LENGTH_SHORT).show()
                    } else {
                        passos[indice] = passo.copy(id = id.trim())
                        redesenharLista()
                    }
                }
            is PassoDoRoteiroDeAutomacao.IrPara ->
                pedirTexto(
                    activity.getString(R.string.se_do_ir_para_no_gravador),
                    passo.se,
                ) { se ->
                    passos[indice] = passo.copy(se = se.trim())
                    redesenharLista()
                }
        }
    }

    private fun pedirTipoParaSubstituir(indice: Int) {
        mostrarEscolhaSimples(
            activity.getString(R.string.substituir_passo_do_gravador) + " ${indice + 1}",
            listOf(
                "Aguardar" to "aguardar",
                "Sumir" to "sumir",
                "URL contém" to "aguardar_url",
                "Preencher" to "preencher",
                "Clicar" to "clicar",
                "Clicar se" to "clicar_se",
                "Assert" to "assertar",
                "Extrair" to "extrair",
                "Esperar ms" to "esperar",
                "Rolar" to "rolar",
                "Abrir URL" to "abrir",
            ),
            -1,
        ) { tipo ->
            when (tipo) {
                "aguardar" -> iniciarPick(ModoPick.Aguardar, indice)
                "sumir" -> iniciarPick(ModoPick.AguardarSumir, indice)
                "aguardar_url" -> pedirAguardarUrl(indice)
                "preencher" -> iniciarPick(ModoPick.Preencher, indice)
                "clicar" -> iniciarPick(ModoPick.Clicar, indice)
                "clicar_se" -> iniciarPick(ModoPick.ClicarSe, indice)
                "assertar" -> iniciarPick(ModoPick.Assertar, indice)
                "extrair" -> iniciarPick(ModoPick.Extrair, indice)
                "esperar" -> pedirEsperarMs(indice)
                "rolar" -> pedirRolar(indice)
                "abrir" -> {
                    indiceParaSubstituir = indice
                    confirmarUrlInicial()
                }
            }
        }
    }

    private fun rotuloDoPasso(passo: PassoDoRoteiroDeAutomacao): String =
        RoteiroDeAutomacaoDaPaginaDoNavegador.rotuloDoPasso(passo)

    private fun chip(rotulo: String, cor: Int, aoClicar: () -> Unit) {
        val densidade = activity.resources.displayMetrics.density
        val botao = TextView(activity).apply {
            text = rotulo
            setTextColor(cor)
            textSize = 13f
            val p = (8 * densidade).toInt()
            setPadding(p, p, p, p)
            setOnClickListener { aoClicar() }
        }
        chips.addView(
            botao,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { marginEnd = (6 * densidade).toInt() },
        )
    }

    private fun textoDoJavascript(bruto: String?): String {
        if (bruto.isNullOrBlank() || bruto == "null") return ""
        return try {
            when (val valor = org.json.JSONTokener(bruto).nextValue()) {
                is String -> valor
                is org.json.JSONObject, is org.json.JSONArray -> valor.toString()
                else -> valor?.toString().orEmpty()
            }
        } catch (_: Exception) {
            bruto.trim().removeSurrounding("\"")
        }
    }
}

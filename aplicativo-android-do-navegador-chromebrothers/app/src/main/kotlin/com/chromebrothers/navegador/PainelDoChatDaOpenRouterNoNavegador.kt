package com.chromebrothers.navegador

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.text.method.LinkMovementMethod
import android.net.Uri
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.PathInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.chromebrothers.navegador.regras.AcoesConfirmadasDoChatDaOpenRouter
import com.chromebrothers.navegador.regras.AnimacaoDaGavetaDoChatDoNavegador
import com.chromebrothers.navegador.regras.CadernoDeNotasDosSitesParaOChat
import com.chromebrothers.navegador.regras.CompactacaoDaConversaDoChatDaOpenRouter
import com.chromebrothers.navegador.regras.EntradaDoIndiceDaConversaDoChat
import com.chromebrothers.navegador.regras.CatalogoDeModelosPorFamiliaDaOpenRouter
import com.chromebrothers.navegador.regras.ChamadaDeFerramenta
import com.chromebrothers.navegador.regras.ContinuacaoDoPedidoDoChat
import com.chromebrothers.navegador.regras.ContextoDaPaginaParaOChat
import com.chromebrothers.navegador.regras.EnderecoDaPaginaInicialDoNavegadorChromebrothers
import com.chromebrothers.navegador.regras.FerramentasDeLeituraDoChatDaOpenRouter
import com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat
import com.chromebrothers.navegador.regras.GeracaoDeVideoPeloTopviewNoChat
import com.chromebrothers.navegador.regras.OrigemDaMidiaNaBibliotecaDoNavegador
import com.chromebrothers.navegador.regras.LiberacaoDeNavegacaoDosSitesParaOChat
import com.chromebrothers.navegador.regras.LiberacaoDoHiggsfieldParaOChat
import com.chromebrothers.navegador.regras.LiberacaoDoTopviewParaOChat
import com.chromebrothers.navegador.regras.LiberacaoDoVivixParaOChat
import com.chromebrothers.navegador.regras.GeracaoDeVideoPeloVivixNoChat
import com.chromebrothers.navegador.regras.CatalogoDeModelosDeVideoDoVivixNaColecao
import com.chromebrothers.navegador.regras.LeitorDeMarkdownNaColecaoDoNavegador
import com.chromebrothers.navegador.regras.MarcaDoElementoVinculado
import com.chromebrothers.navegador.regras.MensagemDeChat
import com.chromebrothers.navegador.regras.MensagemDoComponenteParaOChat
import com.chromebrothers.navegador.regras.ModeloListadoDaOpenRouter
import com.chromebrothers.navegador.regras.ParteDeConteudoDoChat
import com.chromebrothers.navegador.regras.PedidoDeAcaoConfirmadaDoChat
import com.chromebrothers.navegador.regras.PreparacaoDaCapturaDaAbaParaOChat
import com.chromebrothers.navegador.regras.ResultadoDaInterpretacaoDaAcaoDoChat
import java.util.ArrayDeque

class PainelDoChatDaOpenRouterNoNavegador(
    private val activity: AppCompatActivity,
    private val cofre: CofreDaChaveDaOpenRouter,
    private val lerModelo: () -> String,
    private val gravarModelo: (String) -> Unit,
    private val lerTextoDaPagina: (aoReceber: (titulo: String, endereco: String, texto: String, relato: String) -> Unit) -> Unit,
    private val lerSelecao: (aoReceber: (String) -> Unit) -> Unit,
    private val listarMidia: (aoReceber: (String) -> Unit) -> Unit,
    private val lerEnderecos: (aoReceber: (String) -> Unit) -> Unit,
    private val listarCampos: (aoReceber: (String) -> Unit) -> Unit,
    private val lerTextoPaginado: (argumentos: String, aoReceber: (String) -> Unit) -> Unit,
    private val consultarElementos: (argumentos: String, aoReceber: (String) -> Unit) -> Unit,
    private val rolarPagina: (argumentos: String, aoReceber: (String) -> Unit) -> Unit,
    private val capturarAba: (
        aoReceber: (imagem: ParteDeConteudoDoChat?, relato: String) -> Unit,
    ) -> Unit,
    private val descreverAbas: () -> String,
    private val rotuloDaAbaAtual: () -> String,
    private val escolherImagem: (aoReceber: (Uri?) -> Unit) -> Unit,
    private val escolherArquivo: (aoReceber: (Uri?) -> Unit) -> Unit,
    private val lerAnexo: (uri: Uri, aoReceber: (AnexoDoChatDaOpenRouter?, String?) -> Unit) -> Unit,
    private val abrirEndereco: (endereco: String, aoFim: (String) -> Unit) -> Unit,
    private val criarAba: (endereco: String, aoFim: (String) -> Unit) -> Unit,
    private val preencherCampo: (texto: String, aoFim: (String) -> Unit) -> Unit,
    private val escreverNaPagina: (texto: String, aoFim: (String) -> Unit) -> Unit,
    private val clicarNoTexto: (texto: String, aoFim: (String) -> Unit) -> Unit,
    private val cadernoDeNotas: CadernoDeNotasDosSitesNoAparelho,
    private val sitesComNavegacaoLiberada: ListaDeSitesComNavegacaoLiberadaNoAparelho,
    private val descreverColecao: () -> String,
    private val publicarPaginaDaColecao: (titulo: String, html: String) -> String,
    private val listarComponentesDaBiblioteca: () -> String,
    private val guardarComponenteDaColecao: (descricao: String?, tags: List<String>?) -> String,
    private val abrirComponenteDaBiblioteca: (id: String, nome: String) -> String,
    private val apagarComponenteDaBiblioteca: (id: String) -> String,
    private val lerCodigoDoComponente: (id: String) -> String,
    private val capturarComponente: (
        id: String,
        aoReceber: (imagem: ParteDeConteudoDoChat?, relato: String) -> Unit,
    ) -> Unit,
    private val descreverVinculo: () -> String,
    private val vincularCampo: (
        campo: MarcaDoElementoVinculado?,
        envio: MarcaDoElementoVinculado?,
        aoFim: (String) -> Unit,
    ) -> Unit,
    private val siteDaAba: () -> String,
    private val descreverVideoDetectado: () -> String,
    private val listarVideosDaBiblioteca: () -> String,
    private val listarImagensDaBiblioteca: () -> String,
    private val mostrarBotaoFlutuanteDoVideo: () -> String,
    private val abrirPlayerDoVideoDetectado: () -> String,
    private val abrirVideoDaBiblioteca: (chave: String) -> String,
    private val abrirImagemDaBiblioteca: (id: String) -> String,
    private val bytesDaImagemDaBiblioteca: (id: String) -> Pair<ByteArray, String>?,
    private val carregarRoteirosDeAutomacao: () -> List<com.chromebrothers.navegador.regras.RoteiroDeAutomacaoDaPagina>,
    private val guardarRoteiroDeAutomacao: (roteiro: com.chromebrothers.navegador.regras.RoteiroDeAutomacaoDaPagina) -> String,
    private val rodarRoteiroDeAutomacao: (
        idOuNome: String,
        aoPasso: (indice: Int, total: Int, rotulo: String) -> Unit,
        aoFim: (String) -> Unit,
    ) -> Unit,
    private val cancelarRoteiroDeAutomacao: () -> Unit,
    /** Início de uma resposta: páginas publicadas ficam escondidas e na mesma janela. */
    private val iniciarEntregaDaColecao: () -> Unit = {},
    /** Fim da resposta (ok, erro ou interrupção): mostra a janela entregue. */
    private val concluirEntregaDaColecao: () -> Unit = {},
    /** Modelo do modo chat (consultar/planejar); por padrão o mesmo do agente. */
    private val lerModeloDoModoChat: () -> String = lerModelo,
    private val gravarModeloDoModoChat: (String) -> Unit = gravarModelo,
    /** Roda um script na aba ativa e devolve o texto do resultado (vazio se não houver página). */
    private val executarScriptNaPagina: (script: String, aoFim: (String) -> Unit) -> Unit = { _, aoFim -> aoFim("") },
    private val lerPaginaEmSegundoPlano: (
        endereco: String,
        aoFim: (titulo: String, endereco: String, texto: String, erro: String) -> Unit,
    ) -> Unit = { _, aoFim -> aoFim("", "", "", "indisponível") },
    private val editarPaginaDaColecao: (
        titulo: String,
        editar: (String) -> com.chromebrothers.navegador.regras.ResultadoDaEdicaoPorTrechos,
    ) -> String = { _, _ -> "Indisponível." },
) {
    private var roteiroEmAndamentoNoChat = false
    private val gaveta: LinearLayout = activity.findViewById(R.id.gaveta_do_chat_da_openrouter)
    private val areaVisivelDasPaginas: FrameLayout = activity.findViewById(R.id.area_visivel_das_paginas)
    private val faixaDaPaginaEDoChat: ViewGroup = activity.findViewById(R.id.faixa_da_pagina_e_do_chat)
    private var geracaoDaTransicao = 0
    /** O movimento passou de [TransicaoEstiloTelaDivididaDaGaveta.INICIO_DO_CROSSFADE]: já pode revelar. */
    private var movimentoNoFim = false

    /** Abertura/fechamento no estilo da tela dividida (cartões + barrinha + desfoque). */
    private val transicaoDaGaveta by lazy {
        CamadaDaTransicaoEstiloTelaDivididaDaGaveta(activity, faixaDaPaginaEDoChat)
    }
    private val campoModelo: EditText = activity.findViewById(R.id.campo_do_modelo_do_chat_da_openrouter)
    private val botaoModelo: TextView = activity.findViewById(R.id.botao_escolher_modelo_do_chat)
    private val avisoModelos: TextView = activity.findViewById(R.id.aviso_dos_modelos_do_chat)
    private val rolagemFamilias: View = activity.findViewById(R.id.rolagem_das_familias_de_modelo_do_chat)
    private val faixaFamilias: LinearLayout = activity.findViewById(R.id.faixa_das_familias_de_modelo_do_chat)
    private val rolagemModelos: View = activity.findViewById(R.id.rolagem_dos_modelos_da_familia_do_chat)
    private val listaModelos: LinearLayout = activity.findViewById(R.id.lista_de_modelos_da_familia_do_chat)
    private val painelDaChave: View = activity.findViewById(R.id.painel_da_chave_da_openrouter)
    private val corpoDoChat: View = activity.findViewById(R.id.corpo_do_chat_da_openrouter)
    private val tituloDaGaveta: TextView = activity.findViewById(R.id.titulo_da_gaveta_do_chat_da_openrouter)
    private val linhaDaChave: View = activity.findViewById(R.id.linha_da_chave_da_openrouter)
    private val linhaDaChaveGuardada: View = activity.findViewById(R.id.linha_da_chave_guardada_da_openrouter)
    private val campoChave: EditText = activity.findViewById(R.id.campo_da_chave_da_openrouter)
    private val resumoConta: TextView = activity.findViewById(R.id.resumo_da_secao_conta_do_chat)
    private val resumoHiggsfield: TextView = activity.findViewById(R.id.resumo_da_secao_higgsfield_do_chat)
    private val resumoTopview: TextView = activity.findViewById(R.id.resumo_da_secao_topview_do_chat)
    private val resumoSjinn: TextView = activity.findViewById(R.id.resumo_da_secao_sjinn_do_chat)
    private val resumoNotas: TextView = activity.findViewById(R.id.resumo_da_secao_notas_do_chat)
    private val resumoNavegacao: TextView = activity.findViewById(R.id.resumo_da_secao_navegacao_do_chat)
    private val detalheConta: View = activity.findViewById(R.id.detalhe_da_secao_conta_do_chat)
    private val detalheHiggsfield: View = activity.findViewById(R.id.detalhe_da_secao_higgsfield_do_chat)
    private val detalheTopview: View = activity.findViewById(R.id.detalhe_da_secao_topview_do_chat)
    private val detalheSjinn: View = activity.findViewById(R.id.detalhe_da_secao_sjinn_do_chat)
    private val detalheNotas: View = activity.findViewById(R.id.detalhe_da_secao_notas_do_chat)
    private val detalheNavegacao: View = activity.findViewById(R.id.detalhe_da_secao_navegacao_do_chat)
    private val setaConta: TextView = activity.findViewById(R.id.seta_da_secao_conta_do_chat)
    private val setaHiggsfield: TextView = activity.findViewById(R.id.seta_da_secao_higgsfield_do_chat)
    private val setaTopview: TextView = activity.findViewById(R.id.seta_da_secao_topview_do_chat)
    private val resumoMagnific: TextView = activity.findViewById(R.id.resumo_da_secao_magnific_do_chat)
    private val detalheMagnific: View = activity.findViewById(R.id.detalhe_da_secao_magnific_do_chat)
    private val setaMagnific: TextView = activity.findViewById(R.id.seta_da_secao_magnific_do_chat)
    private val rotuloDoMagnific: TextView = activity.findViewById(R.id.rotulo_do_estado_do_magnific_no_chat)
    private val botaoLigarMagnific: View = activity.findViewById(R.id.botao_ligar_magnific_no_chat)
    private val botaoDesligarMagnific: View = activity.findViewById(R.id.botao_desligar_magnific_no_chat)
    private val sessaoMagnific = SessaoDaContaMagnificNoAparelho(activity)
    private val clienteMagnific = ClienteMcpDoMagnificParaGeracaoNoChromebrothers(activity, sessaoMagnific)
    private val setaSjinn: TextView = activity.findViewById(R.id.seta_da_secao_sjinn_do_chat)
    private val setaNotas: TextView = activity.findViewById(R.id.seta_da_secao_notas_do_chat)
    private val setaNavegacao: TextView = activity.findViewById(R.id.seta_da_secao_navegacao_do_chat)
    private val seloDaPagina: TextView = activity.findViewById(R.id.selo_da_pagina_no_chat)
    private val rotuloDaAba: TextView = activity.findViewById(R.id.rotulo_da_aba_do_chat)
    private val rolagemAnexos: View = activity.findViewById(R.id.rolagem_dos_anexos_do_chat)
    private val listaAnexos: LinearLayout = activity.findViewById(R.id.lista_de_anexos_do_chat)
    private val botaoAsk: TextView = activity.findViewById(R.id.botao_modo_ask_do_chat)
    private val botaoAgente: TextView = activity.findViewById(R.id.botao_modo_agente_do_chat)
    private val lista: LinearLayout = activity.findViewById(R.id.lista_de_mensagens_do_chat)
    private val rolagem: ScrollView = activity.findViewById(R.id.rolagem_das_mensagens_do_chat)
    private val campoMensagem: EditText = activity.findViewById(R.id.campo_da_mensagem_do_chat)
    private val botaoEnviar: ImageButton = activity.findViewById(R.id.botao_enviar_mensagem_do_chat)
    private val medidorDoHistorico: VistaDoMedidorCircularDoHistoricoDoChat =
        activity.findViewById(R.id.medidor_do_historico_do_chat)
    private val cliente = ClienteDeChatDaOpenRouter()
    // Cliente próprio: `enviar` cancela a chamada anterior do mesmo cliente, e o resumo não pode derrubar o chat.
    private val clienteDoResumo = ClienteDeChatDaOpenRouter()
    private val sessaoHiggsfield = SessaoDaContaHiggsfieldNoAparelho(activity)
    private val clienteHiggsfield = ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers(activity, sessaoHiggsfield)
    private val gravacaoHiggsfield = GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers(activity)
    private val sessaoTopview = SessaoDaContaTopviewNoAparelho(activity)
    private val clienteTopview = ClienteMcpDoTopviewParaGeracaoNoChromebrothers(activity, sessaoTopview)
    private val sessaoSjinn = SessaoDaContaSjinnNoAparelho(activity)
    private val rotuloDoHiggsfield: TextView = activity.findViewById(R.id.rotulo_do_estado_do_higgsfield_no_chat)
    private val botaoLigarHiggsfield: View = activity.findViewById(R.id.botao_ligar_higgsfield_no_chat)
    private val botaoDesligarHiggsfield: View = activity.findViewById(R.id.botao_desligar_higgsfield_no_chat)
    private val botaoRevogarHiggsfield: View = activity.findViewById(R.id.botao_revogar_liberacao_do_higgsfield_no_chat)
    private val rotuloDoTopview: TextView = activity.findViewById(R.id.rotulo_do_estado_do_topview_no_chat)
    private val botaoLigarTopview: View = activity.findViewById(R.id.botao_ligar_topview_no_chat)
    private val botaoDesligarTopview: View = activity.findViewById(R.id.botao_desligar_topview_no_chat)
    private val botaoRevogarTopview: View = activity.findViewById(R.id.botao_revogar_liberacao_do_topview_no_chat)
    private val rotuloDoSjinn: TextView = activity.findViewById(R.id.rotulo_do_estado_do_sjinn_no_chat)
    private val campoTokenSjinn: EditText = activity.findViewById(R.id.campo_do_token_do_sjinn_no_chat)
    private val botaoGuardarTokenSjinn: View = activity.findViewById(R.id.botao_guardar_token_do_sjinn_no_chat)
    private val botaoApagarTokenSjinn: View = activity.findViewById(R.id.botao_apagar_token_do_sjinn_no_chat)
    private val sessaoVivix = SessaoDaContaVivixNoAparelho(activity)
    private val clienteVivix = ClienteApiDoVivixParaGeracaoDeVideoNoChromebrothers(sessaoVivix)
    private val resumoVivix: TextView = activity.findViewById(R.id.resumo_da_secao_vivix_do_chat)
    private val detalheVivix: View = activity.findViewById(R.id.detalhe_da_secao_vivix_do_chat)
    private val setaVivix: TextView = activity.findViewById(R.id.seta_da_secao_vivix_do_chat)
    private val rotuloDoVivix: TextView = activity.findViewById(R.id.rotulo_do_estado_do_vivix_no_chat)
    private val campoTokenVivix: EditText = activity.findViewById(R.id.campo_do_token_do_vivix_no_chat)
    private val botaoGuardarTokenVivix: View = activity.findViewById(R.id.botao_guardar_token_do_vivix_no_chat)
    private val botaoApagarTokenVivix: View = activity.findViewById(R.id.botao_apagar_token_do_vivix_no_chat)
    private val botaoRevogarVivix: View = activity.findViewById(R.id.botao_revogar_liberacao_do_vivix_no_chat)
    private val conversasSendoResumidas = mutableSetOf<String>()
    private var dialogoDaLista: Dialog? = null
    private var redesenharListaDeConversas: (() -> Unit)? = null
    private val historico = mutableListOf<MensagemDeChat>()

    /** Seleciona um pincel no editor de imagem (a tela principal liga): false se o editor não estiver aberto. */
    var aoUsarPincelDoEditor: ((String) -> Boolean)? = null

    private val ferramentasDePinceis by lazy {
        ExecucaoDasFerramentasDePinceisDoChat(
            activity,
            usarNoEditor = { id -> aoUsarPincelDoEditor?.invoke(id) == true },
            // Última imagem que o usuário anexou (ou a última imagem da conversa).
            ultimaImagemAnexada = {
                historico.asReversed().asSequence()
                    .filter { it.papel == "user" }
                    .flatMap { it.partes.asReversed().asSequence() }
                    .firstOrNull { it.tipo == "imagem" && it.dados.isNotBlank() }
            },
        )
    }
    private val anexos = mutableListOf<AnexoDoChatDaOpenRouter>()
    private var rodadasDeFerramenta = 0
    private var continuacoesDoEnvio = 0
    private var pedidoDoEnvio = ""
    private val acoesFeitasNoEnvio = mutableSetOf<String>()
    /** Modo fixado no início da resposta: trocar o botão no meio não muda as ferramentas. */
    private var agenteNoEnvio = false
    private var usoDoEnvio: com.chromebrothers.navegador.regras.UsoDoChat? = null
    /** Aprovação dada no botão do plano; vale só para a próxima resposta (a que executa o plano). */
    private var aprovacaoPendente: com.chromebrothers.navegador.regras.AprovacaoDoPlanoDoChat? = null
    private var aprovacaoDoEnvio: com.chromebrothers.navegador.regras.AprovacaoDoPlanoDoChat? = null
    /** Última pergunta enviada: base do "Tentar de novo" e do "Editar". */
    private var ultimaTroca: TrocaDoChat? = null
    /** Pergunta em edição: anexos e contexto da página voltam junto ao reenviar. */
    private var edicaoPendente: TrocaDoChat? = null

    private class TrocaDoChat(
        val indiceNoHistorico: Int,
        val mensagem: MensagemDeChat,
        val resumo: String,
        val pergunta: String,
        val contexto: String,
    ) {
        /** A bolha da pergunta: a posição na lista muda quando avisos de andamento entram e saem. */
        var bolha: View? = null
    }
    private var textoAcumuladoDaResposta = ""
    private var aberta = false
    private var animacaoDaGaveta: android.animation.ValueAnimator? = null
    private var enviando = false
    private var geracaoDoEnvio = 0
    private var cartaoDeConfirmacao: View? = null
    private var acaoEmConfirmacao: ChamadaDeFerramenta? = null
    private var incluirPagina = false
    private var modoAgente = false
    private var menuDeModelosAberto = false
    private var vistaDaResposta: TextView? = null
    private var avisoDeAndamento: TextView? = null
    private var modelosCarregados = listOf<ModeloListadoDaOpenRouter>()
    private var familiaAberta = CatalogoDeModelosPorFamiliaDaOpenRouter.familias.first()
    private var geracaoDaLista = 0
    private val filaDoComponente = ArrayDeque<String>()
    /** Mensagens digitadas enquanto o modelo trabalha: entram no próximo passo, sem interromper. */
    private val filaDeOrientacoes = mutableListOf<String>()
    private val bolhasDasOrientacoes = mutableListOf<TextView>()
    private var idDaConversaAtual: String? = null
    private val repositorio = RepositorioDasConversasDoChatNoAparelho(activity.applicationContext)
    private val ouvinteDasConversas = object : OuvinteDasConversasDoChat {
        override fun conversaExcluida(id: String) {
            activity.runOnUiThread {
                // Excluída: não regravar. Apenas começa uma sessão limpa.
                if (id == idDaConversaAtual) descartarSessaoAtual()
                atualizarMedidorDoHistorico()
            }
        }

        override fun conversaResumida(id: String) {
            activity.runOnUiThread {
                if (id == idDaConversaAtual) {
                    val mensagens = repositorio.carregar(id)
                    descartarSessaoAtual()
                    if (mensagens != null) {
                        idDaConversaAtual = id
                        historico.addAll(mensagens)
                        remontarBolhasDoHistorico()
                        rolagem.post { rolarAteOFimSemTirarOFoco() }
                    }
                }
                atualizarMedidorDoHistorico()
            }
        }
    }

    /** Chamado no onDestroy da activity: evita que o registro global segure a activity. */
    fun desligar() {
        repositorio.removerOuvinte(ouvinteDasConversas)
    }

    fun ligar() {
        repositorio.registrarOuvinte(ouvinteDasConversas)
        medidorDoHistorico.setOnClickListener { abrirListaDeConversas() }
        activity.findViewById<View>(R.id.botao_historico_de_conversas_do_chat)
            .setOnClickListener { abrirListaDeConversas() }
        atualizarMedidorDoHistorico()
        campoModelo.setText(modeloAtual())
        atualizarRotuloDoModelo()
        desenharModo()
        desenharSeloDaPagina()
        desenharChave()
        ligarSecoesDaConfiguracao()
        if (cofre.temChave()) {
            fecharConfiguracaoDoChat()
        } else {
            abrirConfiguracaoDoChat(expandirConta = true)
        }
        activity.findViewById<View>(R.id.botao_fechar_chat_da_openrouter).setOnClickListener { fechar() }
        activity.findViewById<View>(R.id.botao_rodar_roteiro_de_automacao_do_chat)
            .setOnClickListener {
                if (roteiroEmAndamentoNoChat) {
                    cancelarRoteiroDeAutomacao()
                    mostrarAndamento(activity.getString(R.string.chat_parando_roteiro))
                } else {
                    abrirMenuDosRoteirosDeAutomacao()
                }
            }
        atualizarBotaoDoRoteiroNoChat()
        activity.findViewById<View>(R.id.botao_configurar_chave_da_openrouter).setOnClickListener {
            if (configuracaoDoChatAberta()) {
                fecharConfiguracaoDoChat()
            } else {
                abrirConfiguracaoDoChat()
            }
        }
        activity.findViewById<View>(R.id.botao_guardar_chave_da_openrouter).setOnClickListener { guardarChave() }
        activity.findViewById<View>(R.id.botao_trocar_chave_da_openrouter).setOnClickListener {
            campoChave.setText("")
            mostrarCampoDaChave()
        }
        botaoLigarHiggsfield.setOnClickListener { ligarHiggsfield() }
        botaoDesligarHiggsfield.setOnClickListener {
            sessaoHiggsfield.apagar()
            desenharEstadoDoHiggsfield()
            avisar(R.string.higgsfield_desligado_aviso)
        }
        botaoRevogarHiggsfield.setOnClickListener {
            sessaoHiggsfield.definirHiggsfieldLiberado(LiberacaoDoHiggsfieldParaOChat.revogar())
            desenharEstadoDoHiggsfield()
            avisar(R.string.higgsfield_liberacao_revogada)
        }
        desenharEstadoDoHiggsfield()
        botaoLigarTopview.setOnClickListener { ligarTopview() }
        botaoLigarMagnific.setOnClickListener { ligarMagnific() }
        botaoDesligarMagnific.setOnClickListener {
            sessaoMagnific.apagar()
            desenharEstadoDoMagnific()
            avisar(R.string.magnific_desligado_aviso)
        }
        desenharEstadoDoMagnific()
        botaoDesligarTopview.setOnClickListener {
            sessaoTopview.apagar()
            desenharEstadoDoTopview()
            avisar(R.string.topview_desligado_aviso)
        }
        botaoRevogarTopview.setOnClickListener {
            sessaoTopview.definirTopviewLiberado(LiberacaoDoTopviewParaOChat.revogar())
            desenharEstadoDoTopview()
            avisar(R.string.topview_liberacao_revogada)
        }
        desenharEstadoDoTopview()
        botaoGuardarTokenSjinn.setOnClickListener {
            val token = campoTokenSjinn.text?.toString().orEmpty().trim()
            if (token.isEmpty()) {
                avisar(R.string.sjinn_token_vazio_aviso)
                return@setOnClickListener
            }
            sessaoSjinn.gravarToken(token)
            campoTokenSjinn.setText("")
            desenharEstadoDoSjinn()
            avisar(R.string.sjinn_token_guardado_aviso)
        }
        botaoApagarTokenSjinn.setOnClickListener {
            sessaoSjinn.apagar()
            campoTokenSjinn.setText("")
            desenharEstadoDoSjinn()
            avisar(R.string.sjinn_token_apagado_aviso)
        }
        desenharEstadoDoSjinn()
        botaoGuardarTokenVivix.setOnClickListener {
            val token = campoTokenVivix.text?.toString().orEmpty().trim()
            if (token.isEmpty()) {
                avisar(R.string.vivix_token_vazio_aviso)
                return@setOnClickListener
            }
            sessaoVivix.gravarToken(token)
            campoTokenVivix.setText("")
            desenharEstadoDoVivix()
            avisar(R.string.vivix_token_guardado_aviso)
        }
        botaoApagarTokenVivix.setOnClickListener {
            sessaoVivix.apagar()
            campoTokenVivix.setText("")
            desenharEstadoDoVivix()
            avisar(R.string.vivix_token_apagado_aviso)
        }
        botaoRevogarVivix.setOnClickListener {
            sessaoVivix.definirVivixLiberado(LiberacaoDoVivixParaOChat.revogar())
            desenharEstadoDoVivix()
            avisar(R.string.vivix_liberacao_revogada)
        }
        desenharEstadoDoVivix()
        activity.findViewById<View>(R.id.botao_apagar_chave_da_openrouter).setOnClickListener {
            cofre.apagar()
            campoChave.setText("")
            desenharChave()
            geracaoDaLista++
            modelosCarregados = emptyList()
            esconderCatalogo()
        }
        activity.findViewById<View>(R.id.botao_guardar_nota_do_site).setOnClickListener { guardarNotaDaTela() }
        activity.findViewById<View>(R.id.botao_apagar_nota_do_site).setOnClickListener { apagarNotaDaTela() }
        activity.findViewById<View>(R.id.botao_liberar_navegacao_do_site).setOnClickListener { liberarSiteDaTela() }
        botaoAsk.setOnClickListener {
            modoAgente = false
            desenharModo()
        }
        botaoAgente.setOnClickListener {
            modoAgente = true
            desenharModo()
        }
        botaoModelo.setOnClickListener { alternarMenuDeModelos() }
        activity.findViewById<View>(R.id.botao_limpar_o_chat_da_openrouter).setOnClickListener { novaConversa() }
        activity.findViewById<View>(R.id.botao_mais_do_chat).setOnClickListener { abrirMenuDoMais(it) }
        seloDaPagina.setOnClickListener {
            incluirPagina = false
            desenharSeloDaPagina()
        }
        botaoEnviar.setOnClickListener {
            when {
                enviando && campoMensagem.text?.toString().orEmpty().isNotBlank() -> enfileirarOrientacao()
                enviando -> interromper()
                else -> enviar()
            }
        }
        botaoEnviar.setOnLongClickListener {
            if (!enviando) return@setOnLongClickListener false
            interromper()
            true
        }
        campoMensagem.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: android.text.Editable?) {
                if (enviando) atualizarBotaoEnviar()
            }
        })
    }

    fun alternar() {
        if (aberta) fechar() else abrir()
    }

    fun estaAberta(): Boolean = aberta

    fun abrirGavetaSeFechada() {
        if (!aberta) abrir()
    }

    fun fecharGavetaSeAberta() {
        if (aberta) fechar()
    }

    private var aoAlternarHostDoGravador: ((Boolean) -> Unit)? = null
    private var aoRestaurarPaineisDaGaveta: (() -> Unit)? = null

    fun definirAoAlternarHostDoGravador(acao: (Boolean) -> Unit) {
        aoAlternarHostDoGravador = acao
    }

    fun definirAoRestaurarPaineisDaGaveta(acao: () -> Unit) {
        aoRestaurarPaineisDaGaveta = acao
    }

    private var aoLayoutDaGavetaEstabilizado: (() -> Unit)? = null

    /** Após o split da gaveta (animação + crossfade), para a WebView recalcular a grade. */
    fun definirAoLayoutDaGavetaEstabilizado(acao: () -> Unit) {
        aoLayoutDaGavetaEstabilizado = acao
    }

    private fun notificarLayoutDaGavetaEstabilizado() {
        aoLayoutDaGavetaEstabilizado?.invoke()
    }

    /**
     * Notifica a faixa de abas: Gravador vira aba (não troca mais o chrome do cabeçalho).
     * Visibilidade dos hosts fica a cargo do [ControladorDasAbasAncoradasNaGavetaDoChatDoNavegador].
     */
    fun mostrarHostDoGravador(mostrar: Boolean) {
        aoAlternarHostDoGravador?.invoke(mostrar)
    }

    /** Abre a gaveta se estiver fechada e deixa o texto no campo, sem enviar. */
    fun abrirComTextoNoCampo(texto: String) {
        if (!aberta) abrir()
        campoMensagem.setText(texto)
        campoMensagem.setSelection(campoMensagem.text?.length ?: 0)
        campoMensagem.requestFocus()
    }

    /** Mensagem vinda da WebView do componente (`chromebrothers.enviarAoChat`). Não abre o chat. */
    fun receberDoComponente(textoBruto: String) {
        val limpo = MensagemDoComponenteParaOChat.limpar(textoBruto)
        if (limpo == null) {
            Toast.makeText(activity, R.string.chat_mensagem_do_componente_vazia, Toast.LENGTH_SHORT).show()
            return
        }
        if (cofre.ler().isEmpty()) {
            if (!aberta) abrir()
            abrirConfiguracaoDoChat(expandirConta = true)
            mostrarCampoDaChave()
            campoMensagem.setText(limpo)
            Toast.makeText(activity, R.string.chat_sem_chave, Toast.LENGTH_LONG).show()
            return
        }
        if (enviando) {
            filaDoComponente.addLast(limpo)
            Toast.makeText(activity, R.string.chat_mensagem_do_componente_na_fila, Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(activity, R.string.chat_mensagem_do_componente_enviada, Toast.LENGTH_SHORT).show()
        dispararDoComponente(limpo)
    }

    private fun dispararDoComponente(texto: String) {
        incluirPagina = false
        disparar(texto, "")
    }

    private fun tentarProximaDaFilaDoComponente() {
        if (enviando) return
        val proxima = filaDoComponente.pollFirst() ?: return
        Toast.makeText(activity, R.string.chat_mensagem_do_componente_enviada, Toast.LENGTH_SHORT).show()
        dispararDoComponente(proxima)
    }

    fun ajustarAoTeclado(extra: Int) {
        gaveta.setPadding(
            gaveta.paddingLeft,
            gaveta.paddingTop,
            gaveta.paddingRight,
            extra + (12 * gaveta.resources.displayMetrics.density).toInt(),
        )
        if (extra > 0 && aberta) rolagem.post { rolarAteOFimSemTirarOFoco() }
    }

    var aoAbrirAGaveta: (() -> Unit)? = null

    private fun abrir() {
        aberta = true
        gaveta.post { if (aberta) aoAbrirAGaveta?.invoke() }
        cancelarAnimacaoDaGaveta()
        val densidade = gaveta.resources.displayMetrics.density
        val larguraFaixa = faixaDaPaginaEDoChat.width
        val alvo = AnimacaoDaGavetaDoChatDoNavegador.calcularLarguraAlvo(densidade, larguraFaixa)
        // Captura COM o site ainda visível: o cartão da página começa idêntico à tela.
        capturarFaixaParaBlocosEsqueleto { captura ->
            if (!aberta) {
                captura?.recycle()
                return@capturarFaixaParaBlocosEsqueleto
            }
            transicaoDaGaveta.instalar(captura, larguraDaFonteDaPagina = larguraFaixa, larguraDoChat = alvo)
            transicaoDaGaveta.aplicar(divisao = 0f, progresso = 0f)
            // Layout real UMA vez, escondido sob a camada — mas só depois de a camada estar na tela
            // (no mesmo quadro, a página encolhia antes da camada aparecer: faixa preta).
            areaVisivelDasPaginas.alpha = 1f
            gaveta.alpha = 1f
            gaveta.translationX = 0f
            depoisDeDesenharACamada {
                if (!aberta) return@depoisDeDesenharACamada
                gaveta.visibility = View.VISIBLE
                aplicarModoDaGaveta(largura = alvo, margemStart = AnimacaoDaGavetaDoChatDoNavegador.margemAoLadoDoSite())
                sumirQuandoAPaginaAssentar { notificarLayoutDaGavetaEstabilizado() }
                // O relayout (chat + site) pode travar uns quadros: acontece com a camada parada e
                // idêntica à tela; o movimento só começa depois, sem pular o início.
                depoisDeDesenharACamada { if (aberta) iniciarAnimacaoDeAbrir() }
            }
        }
        carregarModelos()
        atualizarRotuloDaAba()
    }

    private fun iniciarAnimacaoDeAbrir() {
            animacaoDaGaveta = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
                duration = com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.DURACAO_DE_ABRIR_MS
                // Desaceleração padrão: o deslize fica visível e assenta suave.
                interpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
                var sumindo = false
                addUpdateListener { anim ->
                    // Desfoque pelo tempo (linear); posição pela curva.
                    transicaoDaGaveta.aplicar(divisao = anim.animatedValue as Float, progresso = anim.animatedFraction)
                    if (!sumindo && anim.animatedFraction >= com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.INICIO_DO_CROSSFADE) {
                        sumindo = true
                        movimentoNoFim = true
                    }
                }
                doOnEndDaAnimacao {
                    animacaoDaGaveta = null
                    notificarLayoutDaGavetaEstabilizado()
                }
                start()
            }
    }

    private fun fechar() {
        aberta = false
        fecharConfiguracaoDoChat()
        cancelarAnimacaoDaGaveta()
        val larguraFaixa = faixaDaPaginaEDoChat.width
        val largura = when {
            gaveta.width > 0 -> gaveta.width
            gaveta.layoutParams.width > 0 -> gaveta.layoutParams.width
            else -> AnimacaoDaGavetaDoChatDoNavegador.calcularLarguraAlvo(
                gaveta.resources.displayMetrics.density,
                larguraFaixa,
            )
        }
        capturarFaixaParaBlocosEsqueleto { captura ->
            if (aberta) {
                captura?.recycle()
                return@capturarFaixaParaBlocosEsqueleto
            }
            transicaoDaGaveta.instalar(
                captura,
                larguraDaFonteDaPagina = (larguraFaixa - largura).coerceAtLeast(1),
                larguraDoChat = largura,
            )
            transicaoDaGaveta.aplicar(divisao = 1f, progresso = 0f)
            // Página volta à largura cheia uma vez, sob a camada (depois de a camada aparecer).
            areaVisivelDasPaginas.alpha = 1f
            gaveta.translationX = 0f
            depoisDeDesenharACamada {
                if (aberta) return@depoisDeDesenharACamada
                gaveta.visibility = View.GONE
                aplicarModoDaGaveta(largura = 0, margemStart = AnimacaoDaGavetaDoChatDoNavegador.margemAoLadoDoSite())
                sumirQuandoAPaginaAssentar {}
                depoisDeDesenharACamada { if (!aberta) iniciarAnimacaoDeFechar() }
            }
        }
    }

    private fun iniciarAnimacaoDeFechar() {
            animacaoDaGaveta = android.animation.ValueAnimator.ofFloat(1f, 0f).apply {
                duration = com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.DURACAO_DE_FECHAR_MS
                interpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
                var sumindo = false
                addUpdateListener { anim ->
                    transicaoDaGaveta.aplicar(divisao = anim.animatedValue as Float, progresso = anim.animatedFraction)
                    if (!sumindo && anim.animatedFraction >= com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.INICIO_DO_CROSSFADE) {
                        sumindo = true
                        movimentoNoFim = true
                    }
                }
                doOnEndDaAnimacao { animacaoDaGaveta = null }
                start()
            }
    }

    private fun capturarFaixaParaBlocosEsqueleto(aoPronta: (Bitmap?) -> Unit) {
        val largura = faixaDaPaginaEDoChat.width
        val altura = faixaDaPaginaEDoChat.height
        if (largura <= 0 || altura <= 0) {
            aoPronta(null)
            return
        }
        val bitmap = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
        val origem = IntArray(2)
        faixaDaPaginaEDoChat.getLocationInWindow(origem)
        val retangulo = Rect(
            origem[0],
            origem[1],
            origem[0] + largura,
            origem[1] + altura,
        )
        PixelCopy.request(activity.window, retangulo, bitmap, { resultado ->
            if (resultado == PixelCopy.SUCCESS) {
                // O motor do Firefox desenha o site numa SurfaceView, que a cópia da janela não
                // enxerga (sai transparente): copia cada superfície visível e cola por cima.
                val superficies = mutableListOf<android.view.SurfaceView>()
                acharSuperficiesVisiveis(faixaDaPaginaEDoChat, superficies)
                colarSuperficies(bitmap, origem, superficies, 0) { aoPronta(bitmap) }
            } else {
                bitmap.recycle()
                aoPronta(null)
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun acharSuperficiesVisiveis(vista: View, saida: MutableList<android.view.SurfaceView>) {
        if (vista.visibility != View.VISIBLE || vista.width <= 0 || vista.height <= 0) return
        if (vista is android.view.SurfaceView) {
            saida += vista
            return
        }
        if (vista is ViewGroup) {
            for (i in 0 until vista.childCount) acharSuperficiesVisiveis(vista.getChildAt(i), saida)
        }
    }

    private fun colarSuperficies(
        destino: Bitmap,
        origemDaFaixa: IntArray,
        superficies: List<android.view.SurfaceView>,
        indice: Int,
        aoFim: () -> Unit,
    ) {
        val superficie = superficies.getOrNull(indice) ?: return aoFim()
        val seguir = { colarSuperficies(destino, origemDaFaixa, superficies, indice + 1, aoFim) }
        val valida = superficie.holder.surface?.isValid == true
        if (!valida) return seguir()
        val copia = runCatching {
            Bitmap.createBitmap(superficie.width, superficie.height, Bitmap.Config.ARGB_8888)
        }.getOrNull() ?: return seguir()
        runCatching {
            PixelCopy.request(superficie, copia, { resultado ->
                if (resultado == PixelCopy.SUCCESS && !destino.isRecycled) {
                    val posicao = IntArray(2)
                    superficie.getLocationInWindow(posicao)
                    android.graphics.Canvas(destino).drawBitmap(
                        copia,
                        (posicao[0] - origemDaFaixa[0]).toFloat(),
                        (posicao[1] - origemDaFaixa[1]).toFloat(),
                        null,
                    )
                }
                copia.recycle()
                seguir()
            }, Handler(Looper.getMainLooper()))
        }.onFailure {
            copia.recycle()
            seguir()
        }
    }

    /**
     * Desfaz a camada só quando a página parou de se reorganizar (grade responsiva do site,
     * imagens): compara miniaturas da superfície do site a cada poucos ms, com piso e teto.
     * Começa já na mudança de largura (em paralelo com o movimento), mas só revela quando o
     * movimento está no fim ([movimentoNoFim]). Sem superfície (WebView comum), revela no fim.
     */
    private fun sumirQuandoAPaginaAssentar(aoFim: () -> Unit) {
        val minha = geracaoDaTransicao
        val principal = Handler(Looper.getMainLooper())
        val superficies = mutableListOf<android.view.SurfaceView>()
        acharSuperficiesVisiveis(areaVisivelDasPaginas, superficies)
        val alvo = superficies.firstOrNull { it.holder.surface?.isValid == true }
        fun sumir() {
            if (minha != geracaoDaTransicao) return
            transicaoDaGaveta.sumir(com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.DURACAO_DO_CROSSFADE_MS, aoFim)
        }
        if (alvo == null) {
            fun esperarOMovimento() {
                if (minha != geracaoDaTransicao) return
                if (movimentoNoFim) sumir()
                else principal.postDelayed({ esperarOMovimento() }, com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.INTERVALO_DA_AMOSTRA_MS / 2)
            }
            return esperarOMovimento()
        }

        /** Captura a página já no layout novo, troca o borrão e só então revela. */
        fun revelarLayoutNovoESumir() {
            if (minha != geracaoDaTransicao) return
            val fator = com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.FATOR_DE_REDUCAO
            val w = (alvo.width / fator).coerceAtLeast(1)
            val h = (alvo.height / fator).coerceAtLeast(1)
            val reduzida = runCatching { Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888) }.getOrNull()
                ?: return sumir()
            runCatching {
                PixelCopy.request(alvo, reduzida, { resultado ->
                    if (minha != geracaoDaTransicao) {
                        reduzida.recycle()
                        return@request
                    }
                    if (resultado != PixelCopy.SUCCESS) {
                        reduzida.recycle()
                        sumir()
                        return@request
                    }
                    val posicaoDaSuperficie = IntArray(2)
                    val posicaoDaFaixa = IntArray(2)
                    alvo.getLocationInWindow(posicaoDaSuperficie)
                    faixaDaPaginaEDoChat.getLocationInWindow(posicaoDaFaixa)
                    transicaoDaGaveta.focarNoLayoutNovo(
                        reduzida,
                        larguraReal = alvo.width,
                        alturaReal = alvo.height,
                        dx = (posicaoDaSuperficie[0] - posicaoDaFaixa[0]).toFloat(),
                        dy = (posicaoDaSuperficie[1] - posicaoDaFaixa[1]).toFloat(),
                        duracaoMs = com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.DURACAO_DO_FOCO_MS,
                    ) { sumir() }
                }, principal)
            }.onFailure {
                reduzida.recycle()
                sumir()
            }
        }
        val inicio = android.os.SystemClock.uptimeMillis()
        var anterior: IntArray? = null
        var paradaDesde = 0L
        fun amostrar() {
            if (minha != geracaoDaTransicao) return
            val w = com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.LARGURA_DA_AMOSTRA
            val h = com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.ALTURA_DA_AMOSTRA
            val miniatura = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val seguir = { pixels: IntArray? ->
                val agora = android.os.SystemClock.uptimeMillis()
                val parada = pixels != null && com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.paginaParada(anterior, pixels)
                anterior = pixels
                if (!parada) paradaDesde = 0L else if (paradaDesde == 0L) paradaDesde = agora
                val quietaHa = if (paradaDesde == 0L) 0L else agora - paradaDesde
                val pronta = com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.podeRevelar(agora - inicio, quietaHa)
                if (pixels == null && movimentoNoFim) sumir()
                else if (pronta && movimentoNoFim) revelarLayoutNovoESumir()
                else principal.postDelayed({ amostrar() }, com.chromebrothers.navegador.regras.TransicaoEstiloTelaDivididaDaGaveta.INTERVALO_DA_AMOSTRA_MS)
            }
            runCatching {
                PixelCopy.request(alvo, miniatura, { resultado ->
                    val pixels = if (resultado == PixelCopy.SUCCESS) {
                        IntArray(w * h).also { miniatura.getPixels(it, 0, w, 0, 0, w, h) }
                    } else {
                        null
                    }
                    miniatura.recycle()
                    seguir(pixels)
                }, principal)
            }.onFailure {
                miniatura.recycle()
                seguir(null)
            }
        }
        amostrar()
    }

    /** Roda [acao] dois quadros depois: a camada recém-instalada já foi desenhada na tela. */
    private fun depoisDeDesenharACamada(acao: () -> Unit) {
        val minha = geracaoDaTransicao
        val coreografo = android.view.Choreographer.getInstance()
        coreografo.postFrameCallback {
            coreografo.postFrameCallback {
                if (minha == geracaoDaTransicao) acao()
            }
        }
    }

    private fun cancelarAnimacaoDaGaveta() {
        geracaoDaTransicao++
        movimentoNoFim = false
        gaveta.animate().cancel()
        animacaoDaGaveta?.cancel()
        animacaoDaGaveta = null
        transicaoDaGaveta.remover()
        areaVisivelDasPaginas.alpha = 1f
        gaveta.alpha = 1f
    }

    private fun android.animation.ValueAnimator.doOnEndDaAnimacao(bloco: () -> Unit) {
        addListener(object : android.animation.AnimatorListenerAdapter() {
            private var cancelado = false
            override fun onAnimationCancel(animation: android.animation.Animator) {
                cancelado = true
            }
            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (!cancelado) bloco()
            }
        })
    }

    private fun aplicarModoDaGaveta(largura: Int, margemStart: Int) {
        val params = gaveta.layoutParams as LinearLayout.LayoutParams
        if (params.width == largura && params.marginStart == margemStart) return
        params.width = largura
        params.marginStart = margemStart
        gaveta.layoutParams = params
        gaveta.requestLayout()
    }

    private fun guardarChave() {
        val chave = campoChave.text?.toString().orEmpty().trim()
        if (chave.isEmpty()) {
            avisar(R.string.chat_sem_chave)
            return
        }
        cofre.gravar(chave)
        campoChave.setText("")
        desenharChave()
        fecharConfiguracaoDoChat()
        modelosCarregados = emptyList()
        carregarModelos()
    }

    private fun ligarHiggsfield() {
        avisarTexto(activity.getString(R.string.chat_carregando_a_pagina))
        clienteHiggsfield.ligarConta(activity) { erro ->
            if (erro != null) {
                avisarTexto(erro)
            } else {
                avisar(R.string.higgsfield_ligado_aviso)
            }
            desenharEstadoDoHiggsfield()
        }
    }

    private fun desenharEstadoDoHiggsfield() {
        val ligado = sessaoHiggsfield.temSessao()
        val liberado = sessaoHiggsfield.higgsfieldLiberado()
        rotuloDoHiggsfield.text = buildString {
            append(
                if (ligado) {
                    activity.getString(R.string.higgsfield_ligado_no_chat)
                } else {
                    activity.getString(R.string.higgsfield_desligado_no_chat)
                },
            )
            if (ligado && liberado) {
                append('\n')
                append(activity.getString(R.string.higgsfield_liberado_no_chat))
            }
        }
        botaoDesligarHiggsfield.isEnabled = ligado
        botaoRevogarHiggsfield.visibility = if (liberado) View.VISIBLE else View.GONE
        resumoHiggsfield.text = when {
            ligado && liberado ->
                activity.getString(R.string.higgsfield_ligado_no_chat) + " · liberado"
            ligado -> activity.getString(R.string.higgsfield_ligado_no_chat)
            else -> activity.getString(R.string.higgsfield_desligado_no_chat)
        }
    }

    private fun ligarTopview() {
        avisarTexto(activity.getString(R.string.chat_carregando_a_pagina))
        clienteTopview.ligarConta(activity) { erro ->
            if (erro != null) {
                avisarTexto(erro)
            } else {
                avisar(R.string.topview_ligado_aviso)
            }
            desenharEstadoDoTopview()
        }
    }

    private fun ligarMagnific() {
        avisarTexto(activity.getString(R.string.chat_carregando_a_pagina))
        clienteMagnific.ligarConta(activity) { erro ->
            if (erro != null) avisarTexto(erro) else avisar(R.string.magnific_ligado_aviso)
            desenharEstadoDoMagnific()
        }
    }

    private fun desenharEstadoDoMagnific() {
        val texto = activity.getString(
            if (sessaoMagnific.temSessao()) R.string.estado_magnific_ligado_no_chat
            else R.string.estado_magnific_desligado_no_chat,
        )
        rotuloDoMagnific.text = texto
        resumoMagnific.text = texto
        botaoDesligarMagnific.isEnabled = sessaoMagnific.temSessao()
    }

    private fun desenharEstadoDoTopview() {
        val ligado = sessaoTopview.temSessao()
        val liberado = sessaoTopview.topviewLiberado()
        rotuloDoTopview.text = buildString {
            append(
                if (ligado) {
                    activity.getString(R.string.estado_topview_ligado_no_chat)
                } else {
                    activity.getString(R.string.estado_topview_desligado_no_chat)
                },
            )
            if (ligado && liberado) {
                append(activity.getString(R.string.estado_topview_liberado_no_chat))
            }
        }
        botaoDesligarTopview.isEnabled = ligado
        botaoRevogarTopview.visibility = if (liberado) View.VISIBLE else View.GONE
        resumoTopview.text = when {
            ligado && liberado -> activity.getString(R.string.estado_topview_ligado_no_chat) +
                activity.getString(R.string.estado_topview_liberado_no_chat)
            ligado -> activity.getString(R.string.estado_topview_ligado_no_chat)
            else -> activity.getString(R.string.estado_topview_desligado_no_chat)
        }
    }

    private fun desenharEstadoDoSjinn() {
        val ligado = sessaoSjinn.temSessao()
        rotuloDoSjinn.text = activity.getString(
            if (ligado) R.string.sjinn_ligado_no_chat else R.string.sjinn_desligado_no_chat,
        )
        botaoApagarTokenSjinn.isEnabled = ligado
        campoTokenSjinn.visibility = if (ligado) View.GONE else View.VISIBLE
        resumoSjinn.text = rotuloDoSjinn.text
    }

    private fun desenharEstadoDoVivix() {
        val ligado = sessaoVivix.temSessao()
        val liberado = sessaoVivix.vivixLiberado()
        rotuloDoVivix.text = activity.getString(
            if (ligado) R.string.vivix_ligado_no_chat else R.string.vivix_desligado_no_chat,
        ) + if (ligado && liberado) activity.getString(R.string.estado_vivix_liberado_no_chat) else ""
        botaoApagarTokenVivix.isEnabled = ligado
        campoTokenVivix.visibility = if (ligado) View.GONE else View.VISIBLE
        botaoRevogarVivix.visibility = if (liberado) View.VISIBLE else View.GONE
        resumoVivix.text = rotuloDoVivix.text
    }

    private fun desenharChave() {
        val guardada = cofre.temChave()
        linhaDaChave.visibility = if (guardada) View.GONE else View.VISIBLE
        linhaDaChaveGuardada.visibility = if (guardada) View.VISIBLE else View.GONE
        resumoConta.text = activity.getString(
            if (guardada) {
                R.string.resumo_chave_guardada_na_configuracao_do_chat
            } else {
                R.string.resumo_chave_ausente_na_configuracao_do_chat
            },
        )
    }

    private fun mostrarCampoDaChave() {
        linhaDaChave.visibility = View.VISIBLE
        linhaDaChaveGuardada.visibility = View.GONE
    }

    private fun configuracaoDoChatAberta(): Boolean =
        painelDaChave.visibility == View.VISIBLE

    private fun abrirConfiguracaoDoChat(expandirConta: Boolean = false) {
        prepararNotas()
        atualizarResumosDasSecoesDeSites()
        painelDaChave.visibility = View.VISIBLE
        corpoDoChat.visibility = View.INVISIBLE
        activity.findViewById<View>(R.id.barra_de_contexto_do_chat_na_gaveta).visibility = View.GONE
        activity.findViewById<View>(R.id.host_do_gravador_na_gaveta_do_chat).visibility = View.GONE
        activity.findViewById<View>(R.id.host_dos_componentes_ancorados_na_gaveta_do_chat).visibility = View.GONE
        tituloDaGaveta.setText(R.string.titulo_da_configuracao_do_chat)
        activity.findViewById<View>(R.id.botao_configurar_chave_da_openrouter)
            .contentDescription = activity.getString(R.string.voltar_do_chat_para_a_conversa)
        if (expandirConta || !cofre.temChave()) {
            definirSecaoExpandida(detalheConta, setaConta, true)
        }
    }

    private fun fecharConfiguracaoDoChat() {
        painelDaChave.visibility = View.GONE
        tituloDaGaveta.setText(R.string.titulo_do_chat_da_openrouter)
        activity.findViewById<View>(R.id.botao_configurar_chave_da_openrouter)
            .contentDescription = activity.getString(R.string.configurar_chave_da_openrouter)
        val restaurar = aoRestaurarPaineisDaGaveta
        if (restaurar != null) {
            restaurar()
        } else {
            corpoDoChat.visibility = View.VISIBLE
        }
    }

    private fun ligarSecoesDaConfiguracao() {
        activity.findViewById<View>(R.id.cabecalho_da_secao_conta_do_chat)
            .setOnClickListener { alternarSecao(detalheConta, setaConta) }
        activity.findViewById<View>(R.id.cabecalho_da_secao_higgsfield_do_chat)
            .setOnClickListener { alternarSecao(detalheHiggsfield, setaHiggsfield) }
        activity.findViewById<View>(R.id.cabecalho_da_secao_topview_do_chat)
            .setOnClickListener { alternarSecao(detalheTopview, setaTopview) }
        activity.findViewById<View>(R.id.cabecalho_da_secao_magnific_do_chat)
            .setOnClickListener { alternarSecao(detalheMagnific, setaMagnific) }
        activity.findViewById<View>(R.id.cabecalho_da_secao_sjinn_do_chat)
            .setOnClickListener { alternarSecao(detalheSjinn, setaSjinn) }
        activity.findViewById<View>(R.id.cabecalho_da_secao_vivix_do_chat)
            .setOnClickListener { alternarSecao(detalheVivix, setaVivix) }
        activity.findViewById<View>(R.id.cabecalho_da_secao_notas_do_chat)
            .setOnClickListener { alternarSecao(detalheNotas, setaNotas) }
        activity.findViewById<View>(R.id.cabecalho_da_secao_navegacao_do_chat)
            .setOnClickListener { alternarSecao(detalheNavegacao, setaNavegacao) }
        val detalheArmazenamento = activity.findViewById<View>(R.id.detalhe_da_secao_armazenamento_do_chat)
        val setaArmazenamento = activity.findViewById<TextView>(R.id.seta_da_secao_armazenamento_do_chat)
        resumoArmazenamento.text = "Toque para ver o uso"
        activity.findViewById<View>(R.id.cabecalho_da_secao_armazenamento_do_chat).setOnClickListener {
            alternarSecao(detalheArmazenamento, setaArmazenamento)
            if (detalheArmazenamento.visibility == View.VISIBLE) atualizarResumoDoArmazenamento()
        }
        activity.findViewById<View>(R.id.botao_gerenciar_armazenamento_do_aplicativo)
            .setOnClickListener { aoAbrirArmazenamento?.invoke() }
    }

    /** Abre a tela de Armazenamento (a atividade monta, porque lista e exclui mídia das bibliotecas). */
    var aoAbrirArmazenamento: (() -> Unit)? = null

    /** Busca direta (não propriedade): a seção é ligada durante a construção do painel. */
    private val resumoArmazenamento: TextView
        get() = activity.findViewById(R.id.resumo_da_secao_armazenamento_do_chat)

    /** Linha-resumo da seção: mede em segundo plano. */
    fun atualizarResumoDoArmazenamento() {
        resumoArmazenamento.text = "Calculando…"
        Thread {
            val total = runCatching { MedicaoELimpezaDoArmazenamentoDoAplicativo(activity).medir().total }.getOrNull()
            activity.runOnUiThread {
                resumoArmazenamento.text = if (total == null) "Não consegui medir" else
                    "O app ocupa ${com.chromebrothers.navegador.regras.ArmazenamentoDoAplicativo.formatarBytes(total)}"
            }
        }.start()
    }

    private fun alternarSecao(detalhe: View, seta: TextView) {
        definirSecaoExpandida(detalhe, seta, detalhe.visibility != View.VISIBLE)
    }

    private fun definirSecaoExpandida(detalhe: View, seta: TextView, expandida: Boolean) {
        detalhe.visibility = if (expandida) View.VISIBLE else View.GONE
        seta.text = if (expandida) "▾" else "▸"
    }

    private fun atualizarResumosDasSecoesDeSites() {
        resumoNotas.text = activity.getString(
            R.string.resumo_notas_na_configuracao_do_chat,
            cadernoDeNotas.listar().size,
        )
        resumoNavegacao.text = activity.getString(
            R.string.resumo_sites_liberados_na_configuracao_do_chat,
            sitesComNavegacaoLiberada.listar().size,
        )
    }

    private fun enviar() {
        val pergunta = campoMensagem.text?.toString().orEmpty().trim()
        if (pergunta.isEmpty() && anexos.isEmpty()) {
            avisar(R.string.chat_sem_mensagem)
            return
        }
        val chave = cofre.ler()
        if (chave.isEmpty()) {
            abrirConfiguracaoDoChat(expandirConta = true)
            mostrarCampoDaChave()
            avisar(R.string.chat_sem_chave)
            return
        }
        val modelo = campoModelo.text?.toString().orEmpty()
        gravarModeloAtual(modelo)
        campoModelo.setText(modeloAtual())
        atualizarRotuloDaAba()
        val texto = pergunta.ifBlank { "Veja o anexo." }
        if (!incluirPagina) {
            disparar(texto, "")
            return
        }
        enviando = true
        geracaoDoEnvio++
        atualizarBotaoEnviar()
        mostrarAndamento(FerramentasDeLeituraDoChatDaOpenRouter.rotuloEmAndamento(FerramentasDeLeituraDoChatDaOpenRouter.TEXTO))
        lerTextoDaPagina { titulo, endereco, texto, relato ->
            if (!enviando) return@lerTextoDaPagina
            val contexto = relato.ifBlank { ContextoDaPaginaParaOChat.mensagem(titulo, endereco, texto) }
            disparar(pergunta.ifBlank { "Veja o anexo." }, contexto)
        }
    }

    private fun disparar(pergunta: String, contexto: String) {
        val partes = anexos.map { anexo ->
            when (anexo.tipo) {
                "imagem" -> ParteDeConteudoDoChat("imagem", mime = anexo.mime, nome = anexo.nome, dados = anexo.dados)
                "arquivo" -> ParteDeConteudoDoChat("arquivo", mime = anexo.mime, nome = anexo.nome, dados = anexo.dados)
                else -> ParteDeConteudoDoChat("texto", texto = "Arquivo ${anexo.nome}:\n${anexo.texto}")
            }
        }
        val editada = edicaoPendente
        edicaoPendente = null
        val todasAsPartes = partes + (editada?.mensagem?.partes ?: emptyList())
        val contextoFinal = contexto.ifBlank { editada?.contexto.orEmpty() }
        val conteudo = if (contextoFinal.isBlank()) pergunta else "$contextoFinal\n\nPergunta:\n$pergunta"
        val resumo = if (todasAsPartes.isEmpty()) pergunta else "$pergunta\n${todasAsPartes.size} anexo(s)"
        anexos.clear()
        desenharAnexos()
        incluirPagina = false
        desenharSeloDaPagina()
        campoMensagem.setText("")
        comecarTroca(MensagemDeChat("user", conteudo, todasAsPartes), resumo, pergunta, contextoFinal)
    }

    private fun comecarTroca(mensagem: MensagemDeChat, resumo: String, pergunta: String, contexto: String) {
        // Respostas anteriores já terminaram: resultados longos de ferramenta e capturas saem do histórico.
        val compacto = com.chromebrothers.navegador.regras.CompactacaoDoHistoricoDoChat.compactar(historico)
        historico.clear()
        historico.addAll(compacto)
        val troca = TrocaDoChat(historico.size, mensagem, resumo, pergunta, contexto)
        ultimaTroca = troca
        historico += mensagem
        val bolha = adicionarBolha(resumo, doUsuario = true)
        troca.bolha = bolha
        bolha.setOnLongClickListener {
            abrirMenuDaPergunta(bolha, troca)
            true
        }
        aprovacaoDoEnvio = if (modoAgente) aprovacaoPendente else null
        aprovacaoPendente = null
        vistaDaResposta = adicionarBolha("", doUsuario = false, formatar = true)
        vistaDaResposta?.visibility = View.GONE
        textoAcumuladoDaResposta = ""
        rodadasDeFerramenta = 0
        continuacoesDoEnvio = 0
        pedidoDoEnvio = pergunta
        acoesFeitasNoEnvio.clear()
        agenteNoEnvio = modoAgente
        usoDoEnvio = null
        enviando = true
        geracaoDoEnvio++
        atualizarBotaoEnviar()
        iniciarEntregaDaColecao()
        pedirResposta()
    }

    private fun pedirResposta(continuando: Boolean = false) {
        injetarOrientacoes()
        val geracao = geracaoDoEnvio
        mostrarAndamento(
            activity.getString(if (continuando) R.string.chat_continuando else R.string.chat_pensando),
        )
        // Prompt fixo primeiro e histórico intacto: o provedor reaproveita o cache desse começo.
        // O que muda a cada rodada (aba, nota, coleção, vínculo) vai numa mensagem no fim, fora do histórico.
        val (fixa, contexto) = if (agenteNoEnvio) {
            FerramentasDeLeituraDoChatDaOpenRouter.instrucaoFixa() to
                FerramentasDeLeituraDoChatDaOpenRouter.contextoDaRodada(
                    rotuloDaAba.text.toString(),
                    cadernoDeNotas.notaDe(siteDaAba()),
                    sitesComNavegacaoLiberada.estaLiberado(siteDaAba()),
                    descreverColecao(),
                    descreverVinculo(),
                )
        } else {
            com.chromebrothers.navegador.regras.ModoConversaDoChat.instrucaoFixa() to
                com.chromebrothers.navegador.regras.ModoConversaDoChat.contextoDaRodada(
                    rotuloDaAba.text.toString(),
                    cadernoDeNotas.notaDe(siteDaAba()),
                    descreverColecao(),
                    descreverVinculo(),
                )
        }
        val mensagens = listOf(MensagemDeChat("system", fixa)) + historico + MensagemDeChat("user", contexto)
        val prefixo = textoAcumuladoDaResposta
        var textoDestaRodada = ""
        cliente.enviar(
            cofre.ler(),
            if (agenteNoEnvio) lerModelo() else lerModeloDoModoChat(),
            mensagens,
            comFerramentas = true,
            somenteLeitura = !agenteNoEnvio,
            aoTrecho = { parcial ->
            textoDestaRodada = parcial
            if (parcial.isNotBlank()) {
                vistaDaResposta?.visibility = View.VISIBLE
                esconderAndamento()
            }
            escreverResposta(
                vistaDaResposta,
                listOf(prefixo, parcial).filter { it.isNotBlank() }.joinToString("\n\n"),
            )
            rolagem.post { rolarAteOFimSemTirarOFoco() }
        }, aoTerminar = { fim ->
            if (!enviando || geracao != geracaoDoEnvio) return@enviar
            fim.uso?.let { uso -> usoDoEnvio = usoDoEnvio?.plus(uso) ?: uso }
            val erro = fim.erro
            if (erro != null) {
                falharEnvio(erro)
                return@enviar
            }
            if (textoDestaRodada.isNotBlank()) {
                textoAcumuladoDaResposta = listOf(prefixo, textoDestaRodada).filter { it.isNotBlank() }.joinToString("\n\n")
            }
            val chamadas = fim.chamadas
            if (chamadas.isNotEmpty() && rodadasDeFerramenta < FerramentasDeLeituraDoChatDaOpenRouter.LIMITE_DE_RODADAS) {
                rodadasDeFerramenta++
                historico += MensagemDeChat("assistant", textoDestaRodada, chamadas = chamadas)
                executarFerramentas(chamadas, 0) { pedirResposta() }
                return@enviar
            }
            val faltam = ContinuacaoDoPedidoDoChat.faltam(pedidoDoEnvio, acoesFeitasNoEnvio)
            if (
                agenteNoEnvio &&
                chamadas.isEmpty() &&
                ContinuacaoDoPedidoDoChat.precisa(
                    pedidoDoEnvio,
                    acoesFeitasNoEnvio,
                    continuacoesDoEnvio,
                    textoDestaRodada,
                )
            ) {
                continuacoesDoEnvio++
                if (textoDestaRodada.isNotBlank()) historico += MensagemDeChat("assistant", textoDestaRodada)
                historico += MensagemDeChat("user", ContinuacaoDoPedidoDoChat.mensagem(faltam))
                pedirResposta(continuando = true)
                return@enviar
            }
            if (filaDeOrientacoes.isNotEmpty()) {
                // Chegou orientação depois do último passo: o modelo lê e continua nesta mesma resposta.
                if (textoDestaRodada.isNotBlank()) historico += MensagemDeChat("assistant", textoDestaRodada)
                pedirResposta(continuando = true)
                return@enviar
            }
            if (textoDestaRodada.isNotBlank()) historico += MensagemDeChat("assistant", textoDestaRodada)
            else if (textoAcumuladoDaResposta.isBlank()) {
                vistaDaResposta?.visibility = View.VISIBLE
                vistaDaResposta?.text = activity.getString(R.string.chat_sem_resposta)
            }
            esconderAndamento()
            enviando = false
            atualizarBotaoEnviar()
            val vistaFinal = vistaDaResposta
            vistaDaResposta = null
            concluirEntregaDaColecao()
            mostrarUsoDoEnvio(vistaFinal)
            if (!agenteNoEnvio &&
                com.chromebrothers.navegador.regras.PlanoDoModoConversaDoChat.temPlano(textoAcumuladoDaResposta)
            ) {
                oferecerExecucaoDoPlano(vistaFinal, textoAcumuladoDaResposta)
            }
            persistirConversaAtual()
            tentarProximaDaFilaDoComponente()
        })
    }

    private fun executarFerramentas(chamadas: List<ChamadaDeFerramenta>, indice: Int, aoFim: () -> Unit) {
        val geracao = geracaoDoEnvio
        if (!enviando || geracao != geracaoDoEnvio) return
        if (indice >= chamadas.size) {
            aoFim()
            return
        }
        val chamada = chamadas[indice]
        acoesFeitasNoEnvio += chamada.nome
        fun seguir(
            resultado: String,
            imagem: ParteDeConteudoDoChat? = null,
            textoDaImagem: String = PreparacaoDaCapturaDaAbaParaOChat.mensagemComAImagem(),
        ) {
            if (!enviando || geracao != geracaoDoEnvio) return
            if (acaoEmConfirmacao?.id == chamada.id) acaoEmConfirmacao = null
            historico += MensagemDeChat(papel = "tool", conteudo = resultado, idDaChamada = chamada.id)
            if (imagem != null) {
                historico += MensagemDeChat(
                    papel = "user",
                    conteudo = textoDaImagem,
                    partes = listOf(imagem),
                )
            }
            executarFerramentas(chamadas, indice + 1, aoFim)
        }
        if (chamada.nome !in FerramentasDeLeituraDoChatDaOpenRouter.permitidas) {
            seguir("Ferramenta indisponível.")
            return
        }
        if (!agenteNoEnvio && !com.chromebrothers.navegador.regras.ModoConversaDoChat.podeUsar(chamada.nome)) {
            seguir(com.chromebrothers.navegador.regras.ModoConversaDoChat.RECUSA)
            return
        }
        if (chamada.nome in AcoesConfirmadasDoChatDaOpenRouter.nomes) {
            esconderAndamento()
            when (val resultado = AcoesConfirmadasDoChatDaOpenRouter.interpretar(chamada.nome, chamada.argumentos)) {
                is ResultadoDaInterpretacaoDaAcaoDoChat.Recusada -> seguir(resultado.mensagem)
                is ResultadoDaInterpretacaoDaAcaoDoChat.Pronta -> {
                    val pedido = resultado.pedido
                    if (LiberacaoDoHiggsfieldParaOChat.ehPedidoHiggsfield(pedido) &&
                        !sessaoHiggsfield.temSessao()
                    ) {
                        seguir(GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO)
                    } else if (LiberacaoDoTopviewParaOChat.ehPedidoTopview(pedido) &&
                        !sessaoTopview.temSessao()
                    ) {
                        seguir(GeracaoDeVideoPeloTopviewNoChat.SEM_SESSAO)
                    } else if (LiberacaoDoVivixParaOChat.ehPedidoVivix(pedido) && !sessaoVivix.temSessao()) {
                        seguir(GeracaoDeVideoPeloVivixNoChat.SEM_SESSAO)
                    } else if (
                        com.chromebrothers.navegador.regras.AcoesInternasDoChatSemConfirmacao.segueSemPerguntar(pedido) ||
                        aprovacaoDoEnvio?.cobre(chamada.nome, pedido, siteDaAba()) == true ||
                        LiberacaoDoHiggsfieldParaOChat.segueSemPerguntar(
                            pedido,
                            sessaoHiggsfield.higgsfieldLiberado(),
                        ) ||
                        LiberacaoDoTopviewParaOChat.segueSemPerguntar(
                            pedido,
                            sessaoTopview.topviewLiberado(),
                        ) ||
                        LiberacaoDoVivixParaOChat.segueSemPerguntar(pedido, sessaoVivix.vivixLiberado()) ||
                        LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(
                            pedido,
                            siteDaAba(),
                            sitesComNavegacaoLiberada.listar(),
                        )
                    ) {
                        acaoEmConfirmacao = chamada
                        executarAcao(pedido) { texto -> seguir(texto) }
                    } else {
                        pedirConfirmacao(chamada, pedido) { texto -> seguir(texto) }
                    }
                }
            }
            return
        }
        mostrarAndamento(FerramentasDeLeituraDoChatDaOpenRouter.rotuloEmAndamento(chamada.nome))
        // Pincéis do editor: criar/ajustar/testar (a amostra volta como imagem para o modelo ver).
        if (chamada.nome in com.chromebrothers.navegador.regras.FerramentasDePinceisDoChat.nomes) {
            ferramentasDePinceis.executar(chamada.nome, chamada.argumentos) { texto, imagem ->
                if (imagem != null) seguir(texto, imagem, "Segue a amostra do pincel.") else seguir(texto)
            }
            return
        }
        when (chamada.nome) {
            FerramentasDeLeituraDoChatDaOpenRouter.ENDERECO -> lerTextoDaPagina { titulo, endereco, _, relato ->
                seguir(relato.ifBlank { ContextoDaPaginaParaOChat.mensagem(titulo, endereco, "") })
            }
            FerramentasDeLeituraDoChatDaOpenRouter.TEXTO -> lerTextoDaPagina { titulo, endereco, texto, relato ->
                seguir(relato.ifBlank { ContextoDaPaginaParaOChat.mensagem(titulo, endereco, texto) })
            }
            FerramentasDeLeituraDoChatDaOpenRouter.SELECAO -> lerSelecao { texto ->
                seguir(FerramentasDeLeituraDoChatDaOpenRouter.textoDaSelecao(texto))
            }
            FerramentasDeLeituraDoChatDaOpenRouter.ABAS -> seguir(descreverAbas())
            com.chromebrothers.navegador.regras.ManualDoAgenteDoChat.NOME ->
                seguir(com.chromebrothers.navegador.regras.ManualDoAgenteDoChat.texto(chamada.argumentos))
            FerramentasDeLeituraDoChatDaOpenRouter.MIDIA -> listarMidia { texto -> seguir(texto) }
            FerramentasDeLeituraDoChatDaOpenRouter.COMPONENTES_DA_BIBLIOTECA ->
                seguir(listarComponentesDaBiblioteca())
            FerramentasDeLeituraDoChatDaOpenRouter.LER_CODIGO_DO_COMPONENTE ->
                when (val id = FerramentasDeLeituraDoChatDaOpenRouter.idDoComponente(chamada.argumentos)) {
                    is com.chromebrothers.navegador.regras.ResultadoDoIdDoComponenteParaOChat.Recusado ->
                        seguir(id.mensagem)
                    is com.chromebrothers.navegador.regras.ResultadoDoIdDoComponenteParaOChat.Ok ->
                        seguir(lerCodigoDoComponente(id.id))
                }
            FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_COMPONENTE ->
                when (val id = FerramentasDeLeituraDoChatDaOpenRouter.idDoComponente(chamada.argumentos)) {
                    is com.chromebrothers.navegador.regras.ResultadoDoIdDoComponenteParaOChat.Recusado ->
                        seguir(id.mensagem)
                    is com.chromebrothers.navegador.regras.ResultadoDoIdDoComponenteParaOChat.Ok ->
                        capturarComponente(id.id) { imagem, relato ->
                            seguir(
                                relato,
                                imagem,
                                PreparacaoDaCapturaDaAbaParaOChat.mensagemComAImagemDoComponente(),
                            )
                        }
                }
            FerramentasDeLeituraDoChatDaOpenRouter.VIDEO_DETECTADO ->
                seguir(descreverVideoDetectado())
            FerramentasDeLeituraDoChatDaOpenRouter.VIDEOS_DA_BIBLIOTECA ->
                seguir(listarVideosDaBiblioteca())
            FerramentasDeLeituraDoChatDaOpenRouter.IMAGENS_DA_BIBLIOTECA ->
                seguir(listarImagensDaBiblioteca())
            FerramentasDeLeituraDoChatDaOpenRouter.MODELOS_HIGGSFIELD -> {
                val invalido = com.chromebrothers.navegador.regras.ListagemDeModelosDoHiggsfieldNoChat
                    .mensagemSeFiltroInvalido(chamada.argumentos)
                if (invalido != null) {
                    seguir(invalido)
                } else {
                    val filtro = com.chromebrothers.navegador.regras.ListagemDeModelosDoHiggsfieldNoChat
                        .filtroDosArgumentos(chamada.argumentos)
                    Thread {
                        val texto = clienteHiggsfield.listarModelos(filtro)
                        activity.runOnUiThread {
                            if (!activity.isFinishing) seguir(texto)
                        }
                    }.start()
                }
            }
            FerramentasDeLeituraDoChatDaOpenRouter.ENDERECOS -> lerEnderecos { texto -> seguir(texto) }
            FerramentasDeLeituraDoChatDaOpenRouter.CAMPOS -> listarCampos { texto -> seguir(texto) }
            FerramentasDeLeituraDoChatDaOpenRouter.TEXTO_PAGINADO ->
                lerTextoPaginado(chamada.argumentos) { texto -> seguir(texto) }
            FerramentasDeLeituraDoChatDaOpenRouter.ELEMENTOS ->
                consultarElementos(chamada.argumentos) { texto -> seguir(texto) }
            FerramentasDeLeituraDoChatDaOpenRouter.ROLAR ->
                rolarPagina(chamada.argumentos) { texto -> seguir(texto) }
            FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_ABA -> capturarAba { imagem, relato ->
                seguir(relato, imagem)
            }
            FerramentasDeLeituraDoChatDaOpenRouter.ROTEIROS_DE_AUTOMACAO ->
                seguir(
                    com.chromebrothers.navegador.regras.RoteiroDeAutomacaoDaPaginaDoNavegador
                        .textoDaLista(carregarRoteirosDeAutomacao()),
                )
            FerramentasDeLeituraDoChatDaOpenRouter.LER_ROTEIRO_DE_AUTOMACAO ->
                when (
                    val chave = FerramentasDeLeituraDoChatDaOpenRouter.chaveDoRoteiro(chamada.argumentos)
                ) {
                    is com.chromebrothers.navegador.regras.ResultadoDaChaveDoRoteiroParaOChat.Recusado ->
                        seguir(chave.mensagem)
                    is com.chromebrothers.navegador.regras.ResultadoDaChaveDoRoteiroParaOChat.Ok ->
                        seguir(
                            FerramentasDeLeituraDoChatDaOpenRouter.textoDoRoteiroLido(
                                chave.chave,
                                carregarRoteirosDeAutomacao(),
                            ),
                        )
                }
            else -> seguir("Ferramenta indisponível.")
        }
    }

    private fun falharEnvio(erro: String) {
        devolverOrientacoesAoCampo()
        enviando = false
        esconderAndamento()
        atualizarBotaoEnviar()
        val vista = vistaDaResposta
        if (vista != null) {
            vista.visibility = View.VISIBLE
            if (vista.text.isNullOrBlank()) vista.text = erro
        } else {
            adicionarBolha(erro, doUsuario = false)
        }
        if (historico.lastOrNull()?.papel == "user") historico.removeAt(historico.lastIndex)
        vistaDaResposta = null
        concluirEntregaDaColecao()
        ultimaTroca?.let { troca ->
            val botao = botaoDaConfirmacao(activity.getString(R.string.chat_tentar_de_novo_com_icone), "#8AB4F8") {
                tentarDeNovo(troca)
            }
            lista.addView(botao)
        }
        tentarProximaDaFilaDoComponente()
    }

    private fun mostrarAndamento(texto: String) {
        val densidade = lista.resources.displayMetrics.density
        val aviso = avisoDeAndamento ?: TextView(activity).apply {
            setTextColor(Color.parseColor("#8AB4F8"))
            textSize = 14f
        }.also { avisoDeAndamento = it }
        aviso.text = texto
        aviso.visibility = View.VISIBLE
        (aviso.parent as? android.view.ViewGroup)?.removeView(aviso)
        lista.addView(
            aviso,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = (8 * densidade).toInt()
                bottomMargin = (4 * densidade).toInt()
            },
        )
        rolagem.post { rolarAteOFimSemTirarOFoco() }
    }

    private fun esconderAndamento() {
        avisoDeAndamento?.visibility = View.GONE
    }

    private fun adicionarBolha(texto: String, doUsuario: Boolean, formatar: Boolean = false): TextView {
        val densidade = lista.resources.displayMetrics.density
        val bolha = TextView(activity).apply {
            setTextColor(Color.parseColor("#F4F7FB"))
            textSize = 15f
            val margem = (8 * densidade).toInt()
            setPadding(margem, margem, margem, margem)
            background = GradientDrawable().apply {
                cornerRadius = 12f * densidade
                setColor(Color.parseColor(if (doUsuario) "#2A3140" else "#242833"))
            }
            if (formatar) {
                movementMethod = LinkMovementMethod.getInstance()
                escreverResposta(this, texto)
            } else {
                this.text = texto
            }
        }
        val params = LinearLayout.LayoutParams(
            if (doUsuario) LinearLayout.LayoutParams.WRAP_CONTENT else LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            val margem = (6 * densidade).toInt()
            topMargin = margem
            gravity = if (doUsuario) android.view.Gravity.END else android.view.Gravity.START
        }
        lista.addView(bolha, params)
        rolagem.post { rolarAteOFimSemTirarOFoco() }
        return bolha
    }

    private fun escreverResposta(vista: TextView?, texto: String) {
        vista?.text = TextoFormatadoDoMarkdownDoChat.montar(texto) { endereco ->
            abrirEndereco(endereco) { }
        }
    }

    private fun carregarModelos() {
        val chave = cofre.ler()
        if (chave.isEmpty()) {
            esconderCatalogo()
            return
        }
        if (modelosCarregados.isNotEmpty()) {
            atualizarRotuloDoModelo()
            if (menuDeModelosAberto) mostrarFamilias()
            return
        }
        val geracao = ++geracaoDaLista
        if (menuDeModelosAberto) {
            avisoModelos.text = activity.getString(R.string.carregando_modelos_do_chat)
            avisoModelos.visibility = View.VISIBLE
        }
        cliente.listarModelos(chave) { modelos, erro ->
            if (geracao != geracaoDaLista) return@listarModelos
            if (erro != null) {
                if (menuDeModelosAberto) {
                    avisoModelos.text = erro
                    avisoModelos.visibility = View.VISIBLE
                }
                return@listarModelos
            }
            modelosCarregados = modelos
            val atual = campoModelo.text?.toString().orEmpty()
            familiaAberta = modelos.firstOrNull { it.id == atual }?.familia
                ?: CatalogoDeModelosPorFamiliaDaOpenRouter.familias.firstOrNull { familia ->
                    modelos.any { it.familia == familia }
                }
                ?: familiaAberta
            atualizarRotuloDoModelo()
            if (menuDeModelosAberto) mostrarFamilias()
        }
    }

    private fun esconderCatalogo() {
        avisoModelos.visibility = View.GONE
        campoModelo.visibility = View.GONE
        rolagemFamilias.visibility = View.GONE
        rolagemModelos.visibility = View.GONE
        faixaFamilias.removeAllViews()
        listaModelos.removeAllViews()
    }

    private fun mostrarFamilias() {
        if (!menuDeModelosAberto) {
            esconderCatalogo()
            return
        }
        campoModelo.visibility = View.VISIBLE
        val presentes = CatalogoDeModelosPorFamiliaDaOpenRouter.familias.filter { familia ->
            modelosCarregados.any { it.familia == familia }
        }
        if (presentes.isEmpty()) {
            avisoModelos.text = activity.getString(R.string.nenhum_modelo_na_familia_do_chat)
            avisoModelos.visibility = View.VISIBLE
            rolagemFamilias.visibility = View.GONE
            rolagemModelos.visibility = View.GONE
            return
        }
        if (familiaAberta !in presentes) familiaAberta = presentes.first()
        avisoModelos.visibility = View.GONE
        rolagemFamilias.visibility = View.VISIBLE
        faixaFamilias.removeAllViews()
        val densidade = faixaFamilias.resources.displayMetrics.density
        val fechar = TextView(activity).apply {
            text = activity.getString(R.string.fechar_lista_de_modelos_do_chat)
            contentDescription = activity.getString(R.string.fechar_lista_de_modelos_do_chat_descricao)
            textSize = 13f
            setTextColor(Color.parseColor("#F28B82"))
            val horizontal = (10 * densidade).toInt()
            val vertical = (6 * densidade).toInt()
            setPadding(horizontal, vertical, horizontal, vertical)
            background = GradientDrawable().apply {
                cornerRadius = 14f * densidade
                setColor(Color.parseColor("#2A3140"))
            }
            setOnClickListener { fecharListaDeModelosSemTrocar() }
        }
        faixaFamilias.addView(
            fechar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { marginEnd = (6 * densidade).toInt() },
        )
        presentes.forEach { familia ->
            val chip = TextView(activity).apply {
                text = familia
                textSize = 13f
                setTextColor(Color.parseColor(if (familia == familiaAberta) "#8AB4F8" else "#F4F7FB"))
                val horizontal = (10 * densidade).toInt()
                val vertical = (6 * densidade).toInt()
                setPadding(horizontal, vertical, horizontal, vertical)
                background = GradientDrawable().apply {
                    cornerRadius = 14f * densidade
                    setColor(Color.parseColor("#2A3140"))
                }
                setOnClickListener {
                    familiaAberta = familia
                    mostrarFamilias()
                }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { marginEnd = (6 * densidade).toInt() }
            faixaFamilias.addView(chip, params)
        }
        desenharModelosDaFamilia()
    }

    private fun desenharModelosDaFamilia() {
        val daFamilia = modelosCarregados.filter { it.familia == familiaAberta }
        listaModelos.removeAllViews()
        rolagemModelos.visibility = View.VISIBLE
        if (daFamilia.isEmpty()) {
            avisoModelos.text = activity.getString(R.string.nenhum_modelo_na_familia_do_chat)
            avisoModelos.visibility = View.VISIBLE
            return
        }
        val densidade = listaModelos.resources.displayMetrics.density
        val escolhido = campoModelo.text?.toString().orEmpty()
        daFamilia.forEach { modelo ->
            val linha = TextView(activity).apply {
                text = modelo.nome
                textSize = 14f
                setTextColor(Color.parseColor(if (modelo.id == escolhido) "#8AB4F8" else "#F4F7FB"))
                val margem = (8 * densidade).toInt()
                setPadding(margem, margem, margem, margem)
                setOnClickListener {
                    campoModelo.setText(modelo.id)
                    gravarModeloAtual(modelo.id)
                    menuDeModelosAberto = false
                    atualizarRotuloDoModelo()
                    esconderCatalogo()
                }
            }
            listaModelos.addView(
                linha,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
    }

    /** Fecha a lista mantendo o modelo atual (descarta o que foi digitado no campo). */
    private fun fecharListaDeModelosSemTrocar() {
        menuDeModelosAberto = false
        campoModelo.setText(modeloAtual())
        atualizarRotuloDoModelo()
        esconderCatalogo()
    }

    private fun alternarMenuDeModelos() {
        menuDeModelosAberto = !menuDeModelosAberto
        if (!menuDeModelosAberto) {
            val digitado = campoModelo.text?.toString().orEmpty()
            if (digitado.isNotBlank()) gravarModeloAtual(digitado)
            campoModelo.setText(modeloAtual())
            atualizarRotuloDoModelo()
            esconderCatalogo()
            return
        }
        if (cofre.ler().isEmpty()) {
            menuDeModelosAberto = false
            abrirConfiguracaoDoChat(expandirConta = true)
            avisar(R.string.chat_sem_chave)
            return
        }
        campoModelo.visibility = View.VISIBLE
        carregarModelos()
    }

    private fun atualizarRotuloDoModelo() {
        val id = campoModelo.text?.toString().orEmpty().ifBlank { modeloAtual() }
        botaoModelo.text = id.substringAfter('/').substringBefore(':').ifBlank { id }
    }

    /** Linha discreta com tokens e custo da resposta, logo abaixo dela. */
    private fun mostrarUsoDoEnvio(vista: View?) {
        val uso = usoDoEnvio ?: return
        val densidade = lista.resources.displayMetrics.density
        val linha = TextView(activity).apply {
            text = uso.linha()
            setTextColor(Color.parseColor("#7F8694"))
            textSize = 11f
            val margem = (8 * densidade).toInt()
            setPadding(margem, 0, margem, margem / 2)
        }
        val indice = vista?.let { lista.indexOfChild(it) } ?: -1
        if (indice >= 0) lista.addView(linha, indice + 1) else lista.addView(linha)
        rolagem.post { rolarAteOFimSemTirarOFoco() }
    }

    /** Botão abaixo de um plano do modo chat: troca para o agente e pede para executar. */
    private fun oferecerExecucaoDoPlano(vista: View?, plano: String) {
        val caixa = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        val botao = botaoDaConfirmacao(activity.getString(R.string.executar_plano_no_modo_agente), "#81C995") {}
        caixa.addView(botao)
        val aindaPedem = com.chromebrothers.navegador.regras.AprovacaoDoPlanoDoChat.aindaPedem(plano)
        if (aindaPedem.isNotEmpty()) {
            val densidade = lista.resources.displayMetrics.density
            caixa.addView(TextView(activity).apply {
                text = activity.getString(R.string.plano_ainda_pede_confirmacao, aindaPedem.joinToString(", "))
                setTextColor(Color.parseColor("#7F8694"))
                textSize = 11f
                val margem = (8 * densidade).toInt()
                setPadding(margem, 0, margem, margem / 2)
            })
        }
        botao.setOnClickListener {
            if (enviando) return@setOnClickListener
            lista.removeView(caixa)
            aprovacaoPendente = com.chromebrothers.navegador.regras.AprovacaoDoPlanoDoChat.doPlano(plano, siteDaAba())
            modoAgente = true
            desenharModo()
            campoMensagem.setText(
                com.chromebrothers.navegador.regras.PlanoDoModoConversaDoChat.MENSAGEM_DE_EXECUCAO,
            )
            enviar()
        }
        val indice = vista?.let { lista.indexOfChild(it) } ?: -1
        if (indice >= 0) lista.addView(caixa, indice + 1) else lista.addView(caixa)
        rolagem.post { rolarAteOFimSemTirarOFoco() }
    }

    /** Toque longo na última pergunta: editar ou tentar de novo. */
    private fun abrirMenuDaPergunta(ancora: View, troca: TrocaDoChat) {
        if (enviando || troca !== ultimaTroca) {
            avisarTexto(activity.getString(R.string.chat_so_a_ultima_pergunta))
            return
        }
        PopupMenu(activity, ancora).apply {
            menu.add(0, 1, 0, activity.getString(R.string.chat_editar_pergunta))
            menu.add(0, 2, 1, activity.getString(R.string.chat_tentar_de_novo))
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> editarPergunta(troca)
                    2 -> tentarDeNovo(troca)
                }
                true
            }
        }.show()
    }

    /** Tira da tela e do histórico a última pergunta e tudo o que veio depois dela. */
    private fun desfazerTroca(troca: TrocaDoChat) {
        val inicio = troca.bolha?.let { lista.indexOfChild(it) } ?: -1
        if (inicio >= 0) {
            while (lista.childCount > inicio) lista.removeViewAt(lista.childCount - 1)
        }
        if (historico.size > troca.indiceNoHistorico) {
            historico.subList(troca.indiceNoHistorico, historico.size).clear()
        }
        ultimaTroca = null
        cartaoDeConfirmacao = null
        avisoDeAndamento = null
        persistirConversaAtual()
    }

    private fun tentarDeNovo(troca: TrocaDoChat) {
        if (enviando || troca !== ultimaTroca) return
        desfazerTroca(troca)
        comecarTroca(troca.mensagem, troca.resumo, troca.pergunta, troca.contexto)
    }

    private fun editarPergunta(troca: TrocaDoChat) {
        if (enviando || troca !== ultimaTroca) return
        desfazerTroca(troca)
        edicaoPendente = troca
        campoMensagem.setText(troca.pergunta)
        campoMensagem.setSelection(campoMensagem.text?.length ?: 0)
        campoMensagem.requestFocus()
    }

    private fun modeloAtual(): String = if (modoAgente) lerModelo() else lerModeloDoModoChat()

    private fun gravarModeloAtual(modelo: String) {
        if (modoAgente) gravarModelo(modelo) else gravarModeloDoModoChat(modelo)
    }

    private fun desenharModo() {
        campoModelo.setText(modeloAtual())
        atualizarRotuloDoModelo()
        val densidade = botaoAsk.resources.displayMetrics.density
        fun pintar(botao: TextView, ativo: Boolean) {
            botao.setTextColor(Color.parseColor(if (ativo) "#1A1D24" else "#C4C7CE"))
            botao.background = if (ativo) {
                GradientDrawable().apply {
                    cornerRadius = 16f * densidade
                    setColor(Color.parseColor("#F4F7FB"))
                }
            } else {
                null
            }
        }
        pintar(botaoAsk, !modoAgente)
        pintar(botaoAgente, modoAgente)
        campoMensagem.hint = activity.getString(
            if (modoAgente) R.string.mensagem_do_chat_no_modo_agente else R.string.mensagem_do_chat,
        )
    }

    private fun desenharSeloDaPagina() {
        seloDaPagina.visibility = if (incluirPagina) View.VISIBLE else View.GONE
    }

    private fun abrirMenuDoMais(ancora: View) {
        PopupMenu(activity, ancora).apply {
            menu.add(activity.getString(R.string.adicionar_pagina_ao_chat))
            menu.add(activity.getString(R.string.adicionar_imagem_ao_chat))
            menu.add(activity.getString(R.string.adicionar_arquivo_ao_chat))
            setOnMenuItemClickListener { item ->
                when (item.title.toString()) {
                    activity.getString(R.string.adicionar_imagem_ao_chat) -> escolherImagem { uri ->
                        if (uri != null) receberAnexo(uri)
                    }
                    activity.getString(R.string.adicionar_arquivo_ao_chat) -> escolherArquivo { uri ->
                        if (uri != null) receberAnexo(uri)
                    }
                    else -> {
                        incluirPagina = !incluirPagina
                        desenharSeloDaPagina()
                    }
                }
                true
            }
            show()
        }
    }

    fun anexarArquivoArrastado(uri: Uri) {
        receberAnexo(uri)
    }

    private fun receberAnexo(uri: Uri) {
        if (anexos.size >= 4) {
            avisar(R.string.anexo_do_chat_demais)
            return
        }
        lerAnexo(uri) { anexo, erro ->
            if (erro != null) Toast.makeText(activity, erro, Toast.LENGTH_SHORT).show()
            if (anexo != null) {
                anexos += anexo
                desenharAnexos()
            }
        }
    }

    private fun desenharAnexos() {
        listaAnexos.removeAllViews()
        rolagemAnexos.visibility = if (anexos.isEmpty()) View.GONE else View.VISIBLE
        val densidade = listaAnexos.resources.displayMetrics.density
        anexos.toList().forEach { anexo ->
            val chip = TextView(activity).apply {
                text = anexo.nome
                textSize = 12f
                setTextColor(Color.parseColor("#F4F7FB"))
                val margem = (8 * densidade).toInt()
                setPadding(margem, margem / 2, margem, margem / 2)
                background = GradientDrawable().apply {
                    cornerRadius = 12f * densidade
                    setColor(Color.parseColor("#2A3140"))
                }
                setOnClickListener {
                    anexos.remove(anexo)
                    desenharAnexos()
                }
            }
            listaAnexos.addView(
                chip,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { marginEnd = (6 * densidade).toInt() },
            )
        }
    }

    private fun atualizarRotuloDaAba() {
        rotuloDaAba.text = rotuloDaAbaAtual()
    }

    /** Salva a conversa atual (se houver) e começa uma nova, vazia e sem id. */
    fun novaConversa() {
        if (historico.isNotEmpty()) persistirConversaAtual()
        descartarSessaoAtual()
    }

    /**
     * Carrega uma conversa salva e remonta as bolhas. Devolve false se não foi possível ler.
     * A conversa que estava aberta (se diferente e não vazia) é salva antes da troca.
     */
    fun carregarConversa(id: String): Boolean {
        val mensagens = repositorio.carregar(id) ?: return false
        if (id != idDaConversaAtual && historico.isNotEmpty()) persistirConversaAtual()
        descartarSessaoAtual()
        idDaConversaAtual = id
        historico.addAll(mensagens)
        remontarBolhasDoHistorico()
        rolagem.post { rolarAteOFimSemTirarOFoco() }
        return true
    }

    fun idDaConversaEmUso(): String? = idDaConversaAtual

    fun repositorioDasConversas(): RepositorioDasConversasDoChatNoAparelho = repositorio

    /** Grava a sessão no aparelho. Não trava a conversa em caso de falha: só avisa. */
    private fun persistirConversaAtual() {
        if (historico.isEmpty()) return
        val resultado = repositorio.salvar(idDaConversaAtual, historico.toList())
        if (resultado.id != null) idDaConversaAtual = resultado.id
        when {
            resultado.bloqueadoPorTetoDuro ->
                Toast.makeText(activity, R.string.chat_conversa_grande_demais_para_salvar, Toast.LENGTH_LONG).show()
            resultado.erro != null ->
                Toast.makeText(
                    activity,
                    activity.getString(R.string.chat_erro_ao_salvar_conversa, resultado.erro),
                    Toast.LENGTH_LONG,
                ).show()
            resultado.avisoDeLimite ->
                Toast.makeText(activity, R.string.chat_historico_cheio_aviso, Toast.LENGTH_LONG).show()
        }
        atualizarMedidorDoHistorico()
    }

    private fun atualizarMedidorDoHistorico() {
        medidorDoHistorico.setProgresso(repositorio.percentualDoMedidor(), repositorio.faixaDoMedidor())
        redesenharListaDeConversas?.invoke()
    }

    private fun abrirListaDeConversas() {
        dialogoDaLista?.dismiss()
        val densidade = activity.resources.displayMetrics.density
        val dialogo = Dialog(activity)
        val raiz = LayoutInflater.from(activity)
            .inflate(R.layout.layout_caixa_de_dialogo_do_historico_de_conversas_do_chat, null)
        val resumoDoUso = raiz.findViewById<TextView>(R.id.resumo_do_uso_do_historico_do_chat)
        val itens = raiz.findViewById<LinearLayout>(R.id.lista_de_conversas_do_historico_do_chat)
        val fechar = raiz.findViewById<Button>(R.id.botao_fechar_a_caixa_do_historico_do_chat)
        fechar.backgroundTintList = null
        fechar.stateListAnimator = null
        fechar.setOnClickListener { dialogo.dismiss() }
        val formatoDaData = java.text.DateFormat.getDateTimeInstance(
            java.text.DateFormat.SHORT,
            java.text.DateFormat.SHORT,
        )
        val redesenhar = {
            resumoDoUso.text = activity.getString(R.string.chat_historico_resumo_do_uso, repositorio.percentualDoMedidor())
            itens.removeAllViews()
            val conversas = repositorio.listar().sortedByDescending { it.atualizadaEm }
            if (conversas.isEmpty()) {
                itens.addView(
                    TextView(activity).apply {
                        setText(R.string.chat_historico_vazio)
                        setTextColor(Color.parseColor("#8B91A1"))
                        textSize = 14f
                        setPadding(0, (16 * densidade).toInt(), 0, (16 * densidade).toInt())
                    },
                )
            }
            conversas.forEach { entrada ->
                val linha = LayoutInflater.from(activity)
                    .inflate(R.layout.layout_item_da_lista_de_conversas_do_chat, itens, false)
                linha.findViewById<TextView>(R.id.titulo_da_conversa_do_chat).text = entrada.titulo
                val data = formatoDaData.format(java.util.Date(entrada.atualizadaEm))
                linha.findViewById<TextView>(R.id.data_da_conversa_do_chat).text =
                    if (entrada.resumida) "$data · ${activity.getString(R.string.chat_conversa_com_resumo)}" else data
                linha.findViewById<View>(R.id.area_do_texto_da_conversa_do_chat).setOnClickListener {
                    if (carregarConversa(entrada.id)) {
                        dialogo.dismiss()
                    } else {
                        avisar(R.string.chat_conversa_nao_carregada)
                    }
                }
                val resumindo = entrada.id in conversasSendoResumidas
                linha.findViewById<TextView>(R.id.botao_resumir_a_conversa_do_chat).apply {
                    isEnabled = !resumindo
                    alpha = if (resumindo) 0.4f else 1f
                    setOnClickListener { confirmarResumoDaConversa(entrada) }
                }
                linha.findViewById<View>(R.id.botao_excluir_a_conversa_do_chat).setOnClickListener {
                    confirmarExclusaoDaConversa(entrada)
                }
                itens.addView(linha)
            }
        }
        redesenhar()
        redesenharListaDeConversas = redesenhar
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.setOnDismissListener {
            if (dialogoDaLista === dialogo) {
                dialogoDaLista = null
                redesenharListaDeConversas = null
            }
        }
        dialogoDaLista = dialogo
        dialogo.show()
        val largura = (420 * densidade).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun confirmarExclusaoDaConversa(entrada: EntradaDoIndiceDaConversaDoChat) {
        mostrarCaixaDeConfirmacaoDoChat(
            titulo = activity.getString(R.string.chat_excluir_conversa_titulo),
            mensagem = activity.getString(R.string.chat_excluir_conversa_confirmacao, entrada.titulo),
            textoConfirmar = activity.getString(R.string.chat_excluir_conversa),
            corConfirmar = Color.parseColor("#F28B82"),
        ) {
            repositorio.excluir(entrada.id)
            atualizarMedidorDoHistorico()
            avisar(R.string.chat_conversa_excluida)
        }
    }

    private fun confirmarResumoDaConversa(entrada: EntradaDoIndiceDaConversaDoChat) {
        if (entrada.id in conversasSendoResumidas) return
        mostrarCaixaDeConfirmacaoDoChat(
            titulo = activity.getString(R.string.chat_resumir_conversa_titulo),
            mensagem = activity.getString(R.string.chat_resumir_conversa_confirmacao, entrada.titulo),
            textoConfirmar = activity.getString(R.string.chat_resumir_conversa),
            corConfirmar = Color.parseColor("#8AB4F8"),
        ) {
            resumirConversa(entrada.id)
        }
    }

    private fun mostrarCaixaDeConfirmacaoDoChat(
        titulo: String,
        mensagem: String,
        textoConfirmar: String,
        corConfirmar: Int,
        aoConfirmar: () -> Unit,
    ) {
        val dialogo = Dialog(activity)
        val raiz = LayoutInflater.from(activity)
            .inflate(R.layout.layout_caixa_de_dialogo_do_editor_de_imagem, null)
        raiz.findViewById<TextView>(R.id.titulo_da_caixa_de_dialogo_do_editor).text = titulo
        val texto = raiz.findViewById<TextView>(R.id.mensagem_da_caixa_de_dialogo_do_editor)
        texto.visibility = View.VISIBLE
        texto.text = mensagem
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

    private fun resumirConversa(id: String) {
        val chave = cofre.ler()
        if (chave.isEmpty()) {
            avisar(R.string.chat_resumo_sem_chave)
            return
        }
        if (id == idDaConversaAtual) {
            if (enviando) {
                avisar(R.string.chat_resumo_conversa_em_envio)
                return
            }
            if (historico.isNotEmpty()) persistirConversaAtual()
        }
        val mensagens = repositorio.carregar(id)
        if (mensagens.isNullOrEmpty()) {
            avisar(R.string.chat_resumo_conversa_nao_lida)
            return
        }
        conversasSendoResumidas += id
        redesenharListaDeConversas?.invoke()
        avisar(R.string.chat_resumindo_conversa)
        var resumo = ""
        val terminar = { conversasSendoResumidas -= id; redesenharListaDeConversas?.invoke() }
        clienteDoResumo.enviar(
            chave,
            modeloAtual(),
            CompactacaoDaConversaDoChatDaOpenRouter.promptParaResumir(mensagens),
            comFerramentas = false,
            aoTrecho = { parcial -> resumo = parcial },
            aoTerminar = { fim ->
                terminar()
                when {
                    fim.erro != null ->
                        avisarTexto(activity.getString(R.string.chat_resumo_erro, fim.erro))
                    resumo.isBlank() -> avisar(R.string.chat_resumo_vazio)
                    id == idDaConversaAtual && enviando -> avisar(R.string.chat_resumo_conversa_em_envio)
                    else -> {
                        // Compacta sobre a versão mais recente gravada (outra janela pode ter avançado a conversa).
                        val base = repositorio.carregar(id) ?: mensagens
                        val compactado = CompactacaoDaConversaDoChatDaOpenRouter.aplicar(resumo, base)
                        val resultado = repositorio.salvar(id, compactado, resumida = true)
                        if (resultado.erro != null || resultado.bloqueadoPorTetoDuro || resultado.id == null) {
                            avisar(R.string.chat_resumo_conversa_sem_espaco)
                        } else {
                            repositorio.notificarConversaResumida(id)
                            avisar(R.string.chat_conversa_resumida)
                        }
                    }
                }
                atualizarMedidorDoHistorico()
            },
        )
    }

    /** Zera a sessão em memória e na tela, sem gravar nada. */
    private fun descartarSessaoAtual() {
        if (enviando) cliente.cancelar()
        enviando = false
        geracaoDoEnvio++
        acaoEmConfirmacao = null
        cartaoDeConfirmacao = null
        historico.clear()
        idDaConversaAtual = null
        ultimaTroca = null
        edicaoPendente = null
        aprovacaoPendente = null
        filaDeOrientacoes.clear()
        bolhasDasOrientacoes.clear()
        lista.removeAllViews()
        vistaDaResposta = null
        avisoDeAndamento = null
        atualizarBotaoEnviar()
        concluirEntregaDaColecao()
        tentarProximaDaFilaDoComponente()
    }

    private fun remontarBolhasDoHistorico() {
        var anterior: MensagemDeChat? = null
        for (mensagem in historico) {
            when (mensagem.papel) {
                "user" -> {
                    val ehInjecaoDeFerramenta = anterior?.papel == "tool" ||
                        mensagem.conteudo.startsWith("Continue ")
                    if (!ehInjecaoDeFerramenta) {
                        val texto = mensagem.conteudo.substringAfter("\n\nPergunta:\n", mensagem.conteudo)
                        val anexosDaMensagem = mensagem.partes.count { it.tipo == "imagem" || it.tipo == "arquivo" } +
                            mensagem.partes.count { it.tipo == "texto" && it.texto.startsWith("Arquivo ") }
                        val resumo = if (anexosDaMensagem == 0) texto else "$texto\n$anexosDaMensagem anexo(s)"
                        if (resumo.isNotBlank()) adicionarBolha(resumo, doUsuario = true)
                    }
                }
                "assistant" -> if (mensagem.conteudo.isNotBlank()) {
                    adicionarBolha(mensagem.conteudo, doUsuario = false, formatar = true)
                }
            }
            anterior = mensagem
        }
    }

    private fun interromper() {
        val pendente = acaoEmConfirmacao
        enviando = false
        geracaoDoEnvio++
        cliente.cancelar()
        // Um toque para tudo: roteiro iniciado pelo chat, fila de componentes e orientações pendentes.
        cancelarRoteiroDeAutomacao()
        devolverOrientacoesAoCampo()
        if (filaDoComponente.isNotEmpty()) {
            avisarTexto(activity.getString(R.string.chat_fila_do_componente_descartada, filaDoComponente.size))
            filaDoComponente.clear()
        }
        removerCartaoDeConfirmacao()
        esconderAndamento()
        val vista = vistaDaResposta
        if (vista != null && (vista.visibility != View.VISIBLE || vista.text.isNullOrBlank())) {
            vista.visibility = View.VISIBLE
            vista.text = activity.getString(R.string.chat_interrompido)
        }
        if (pendente != null) {
            historico += MensagemDeChat(
                papel = "tool",
                conteudo = "Interrompido pelo usuário.",
                idDaChamada = pendente.id,
            )
            acaoEmConfirmacao = null
        }
        val fechado = com.chromebrothers.navegador.regras.FechamentoDasChamadasPendentesDoChat.fechar(historico)
        historico.clear()
        historico.addAll(fechado)
        atualizarBotaoEnviar()
        concluirEntregaDaColecao()
        persistirConversaAtual()
    }

    /**
     * Rola a conversa até o fim sem mexer no foco. ScrollView.fullScroll(FOCUS_DOWN) dá foco ao
     * último item focável (bolhas com link), e o campo perdia o teclado no meio da digitação.
     */
    private fun rolarAteOFimSemTirarOFoco() {
        val conteudo = rolagem.getChildAt(0) ?: return
        rolagem.scrollTo(0, (conteudo.height - rolagem.height).coerceAtLeast(0))
    }

    private fun enfileirarOrientacao() {
        val texto = campoMensagem.text?.toString().orEmpty().trim()
        if (texto.isEmpty()) return
        filaDeOrientacoes += texto
        campoMensagem.setText("")
        val bolha = adicionarBolha(activity.getString(R.string.chat_orientacao_na_fila, texto), doUsuario = true)
        bolha.alpha = 0.7f
        bolhasDasOrientacoes += bolha
        atualizarBotaoEnviar()
    }

    /** Entrega as orientações pendentes ao modelo, antes da próxima chamada. */
    private fun injetarOrientacoes() {
        val mensagem = com.chromebrothers.navegador.regras.OrientacaoDuranteARespostaDoChat.mensagem(filaDeOrientacoes) ?: return
        historico += mensagem
        filaDeOrientacoes.forEachIndexed { i, texto ->
            bolhasDasOrientacoes.getOrNull(i)?.apply {
                text = activity.getString(R.string.chat_orientacao_entregue, texto)
                alpha = 1f
            }
        }
        filaDeOrientacoes.clear()
        bolhasDasOrientacoes.clear()
        // O que o modelo escrever depois da orientação vai numa bolha nova, abaixo dela.
        val atual = vistaDaResposta ?: return
        if (atual.visibility == View.VISIBLE && textoAcumuladoDaResposta.isNotBlank()) {
            vistaDaResposta = adicionarBolha("", doUsuario = false, formatar = true).apply { visibility = View.GONE }
            textoAcumuladoDaResposta = ""
        } else {
            lista.removeView(atual)
            lista.addView(atual)
        }
    }

    /** Parou ou falhou antes de entregar: o texto volta para o campo, nada se perde. */
    private fun devolverOrientacoesAoCampo() {
        if (filaDeOrientacoes.isEmpty()) return
        bolhasDasOrientacoes.forEach { lista.removeView(it) }
        val atual = campoMensagem.text?.toString().orEmpty().trim()
        campoMensagem.setText((listOf(atual) + filaDeOrientacoes).filter { it.isNotBlank() }.joinToString("\n"))
        filaDeOrientacoes.clear()
        bolhasDasOrientacoes.clear()
    }

    private fun pedirConfirmacao(
        chamada: ChamadaDeFerramenta,
        pedido: PedidoDeAcaoConfirmadaDoChat,
        seguir: (String) -> Unit,
    ) {
        acaoEmConfirmacao = chamada
        val densidade = lista.resources.displayMetrics.density
        val margem = (8 * densidade).toInt()
        val caixa = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(margem, margem, margem, margem)
            background = GradientDrawable().apply {
                cornerRadius = 12f * densidade
                setColor(Color.parseColor("#242833"))
            }
        }
        caixa.addView(TextView(activity).apply {
            text = AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedido)
            setTextColor(Color.parseColor("#F4F7FB"))
            textSize = 14f
        })
        val botoes = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        botoes.addView(botaoDaConfirmacao(activity.getString(R.string.confirmar_acao_do_chat), "#8AB4F8") {
            if (!enviando) return@botaoDaConfirmacao
            removerCartaoDeConfirmacao()
            executarAcao(pedido, seguir)
        })
        botoes.addView(botaoDaConfirmacao(activity.getString(R.string.recusar_acao_do_chat), "#F28B82") {
            if (!enviando) return@botaoDaConfirmacao
            removerCartaoDeConfirmacao()
            seguir("O usuário recusou.")
        })
        caixa.addView(botoes, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = margem })
        val hospedeiro = LiberacaoDeNavegacaoDosSitesParaOChat.hospedeiroDaAcao(pedido, siteDaAba())
        if (hospedeiro.isNotEmpty()) {
            val liberar = botaoDaConfirmacao(
                activity.getString(R.string.liberar_este_site_na_confirmacao),
                "#81C995",
            ) {
                if (!enviando) return@botaoDaConfirmacao
                val erro = sitesComNavegacaoLiberada.liberar(hospedeiro)
                if (erro.isNotEmpty()) {
                    avisarTexto(erro)
                    return@botaoDaConfirmacao
                }
                desenharSitesLiberados()
                avisarTexto(activity.getString(R.string.navegacao_do_site_liberada))
                removerCartaoDeConfirmacao()
                executarAcao(pedido, seguir)
            }
            caixa.addView(liberar, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ))
        }
        if (LiberacaoDoHiggsfieldParaOChat.ehPedidoHiggsfield(pedido)) {
            val liberar = botaoDaConfirmacao(
                activity.getString(R.string.liberar_higgsfield_na_confirmacao),
                "#81C995",
            ) {
                if (!enviando) return@botaoDaConfirmacao
                sessaoHiggsfield.definirHiggsfieldLiberado(LiberacaoDoHiggsfieldParaOChat.liberar())
                desenharEstadoDoHiggsfield()
                removerCartaoDeConfirmacao()
                executarAcao(pedido, seguir)
            }
            caixa.addView(liberar, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ))
        }
        if (LiberacaoDoTopviewParaOChat.ehPedidoTopview(pedido)) {
            val liberar = botaoDaConfirmacao(
                activity.getString(R.string.liberar_topview_na_confirmacao),
                "#81C995",
            ) {
                if (!enviando) return@botaoDaConfirmacao
                sessaoTopview.definirTopviewLiberado(LiberacaoDoTopviewParaOChat.liberar())
                desenharEstadoDoTopview()
                removerCartaoDeConfirmacao()
                executarAcao(pedido, seguir)
            }
            caixa.addView(liberar, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ))
        }
        if (LiberacaoDoVivixParaOChat.ehPedidoVivix(pedido)) {
            val liberar = botaoDaConfirmacao(
                activity.getString(R.string.liberar_vivix_na_confirmacao),
                "#81C995",
            ) {
                if (!enviando) return@botaoDaConfirmacao
                sessaoVivix.definirVivixLiberado(LiberacaoDoVivixParaOChat.liberar())
                desenharEstadoDoVivix()
                removerCartaoDeConfirmacao()
                executarAcao(pedido, seguir)
            }
            caixa.addView(liberar, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ))
        }
        lista.addView(caixa, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = (6 * densidade).toInt() })
        cartaoDeConfirmacao = caixa
        rolagem.post { rolarAteOFimSemTirarOFoco() }
    }

    private fun executarAcao(pedido: PedidoDeAcaoConfirmadaDoChat, seguir: (String) -> Unit) {
        when (pedido) {
            is PedidoDeAcaoConfirmadaDoChat.AbrirEndereco -> {
                mostrarAndamento(activity.getString(R.string.chat_carregando_a_pagina))
                abrirEndereco(pedido.endereco, seguir)
            }
            is PedidoDeAcaoConfirmadaDoChat.CriarAba -> {
                if (pedido.endereco.isNotBlank()) {
                    mostrarAndamento(activity.getString(R.string.chat_carregando_a_pagina))
                }
                criarAba(pedido.endereco, seguir)
            }
            is PedidoDeAcaoConfirmadaDoChat.PreencherCampo -> preencherCampo(pedido.texto, seguir)
            is PedidoDeAcaoConfirmadaDoChat.EscreverNaPagina -> escreverNaPagina(pedido.texto, seguir)
            is PedidoDeAcaoConfirmadaDoChat.Clicar -> {
                mostrarAndamento(activity.getString(R.string.chat_clicando))
                clicarNoTexto(pedido.texto, seguir)
            }
            is PedidoDeAcaoConfirmadaDoChat.ClicarElemento -> {
                mostrarAndamento(activity.getString(R.string.chat_clicando))
                executarScriptNaPagina(
                    com.chromebrothers.navegador.regras.ElementosDaPaginaPeloSeletorParaOChat.scriptDeClicar(pedido.seletor, pedido.indice),
                ) { resultado ->
                    seguir(com.chromebrothers.navegador.regras.ElementosDaPaginaPeloSeletorParaOChat.relato(resultado, pedido.seletor))
                }
            }
            is PedidoDeAcaoConfirmadaDoChat.PreencherElemento ->
                executarScriptNaPagina(
                    com.chromebrothers.navegador.regras.ElementosDaPaginaPeloSeletorParaOChat.scriptDePreencher(pedido.seletor, pedido.valor, pedido.indice),
                ) { resultado ->
                    seguir(com.chromebrothers.navegador.regras.ElementosDaPaginaPeloSeletorParaOChat.relato(resultado, pedido.seletor))
                }
            is PedidoDeAcaoConfirmadaDoChat.ApertarTecla ->
                executarScriptNaPagina(
                    com.chromebrothers.navegador.regras.ElementosDaPaginaPeloSeletorParaOChat.scriptDeTecla(pedido.tecla, pedido.seletor, pedido.indice),
                ) { resultado ->
                    val alvo = pedido.seletor.ifBlank { "o elemento com foco" }
                    seguir("${pedido.tecla}: " + com.chromebrothers.navegador.regras.ElementosDaPaginaPeloSeletorParaOChat.relato(resultado, alvo))
                }
            is PedidoDeAcaoConfirmadaDoChat.LerPaginaEmSegundoPlano -> {
                mostrarAndamento(activity.getString(R.string.chat_lendo_em_segundo_plano))
                lerPaginaEmSegundoPlano(pedido.endereco) { titulo, endereco, texto, erro ->
                    seguir(com.chromebrothers.navegador.regras.LeituraEmSegundoPlanoParaOChat.relato(titulo, endereco, texto, pedido.offset, erro))
                }
            }
            is PedidoDeAcaoConfirmadaDoChat.EditarPaginaDaColecao ->
                seguir(
                    editarPaginaDaColecao(pedido.titulo) { html ->
                        com.chromebrothers.navegador.regras.EdicaoPorTrechosDoHtml.aplicar(html, pedido.trocas)
                    },
                )
            is PedidoDeAcaoConfirmadaDoChat.GuardarNota -> {
                val site = pedido.site.ifBlank { siteDaAba() }
                val erro = cadernoDeNotas.guardar(site, pedido.texto)
                if (erro.isEmpty()) {
                    desenharNotas()
                    val hospedeiro = CadernoDeNotasDosSitesParaOChat.chave(site)
                    seguir("Guardei a nota de $hospedeiro.")
                } else {
                    seguir(erro)
                }
            }
            is PedidoDeAcaoConfirmadaDoChat.PublicarPaginaDaColecao ->
                seguir(publicarPaginaDaColecao(pedido.titulo, pedido.html))
            is PedidoDeAcaoConfirmadaDoChat.MostrarMarkdownNaColecao -> {
                mostrarAndamento(activity.getString(R.string.chat_gerando_arquivo))
                Thread {
                    val gravacao = GravacaoDePacoteGeradoPeloChatNoArmazenamentoDoAplicativo(activity)
                        .mostrarMarkdownNaColecao(pedido)
                    val html = LeitorDeMarkdownNaColecaoDoNavegador.htmlDoLeitor(
                        titulo = pedido.titulo,
                        markdown = pedido.conteudo,
                    )
                    activity.runOnUiThread {
                        if (activity.isFinishing) return@runOnUiThread
                        val publicacao = publicarPaginaDaColecao(pedido.titulo, html)
                        seguir("$gravacao $publicacao")
                    }
                }.start()
            }
            is PedidoDeAcaoConfirmadaDoChat.GuardarComponenteDaColecao ->
                seguir(guardarComponenteDaColecao(pedido.descricao, pedido.tags))
            is PedidoDeAcaoConfirmadaDoChat.AbrirComponenteDaBiblioteca ->
                seguir(abrirComponenteDaBiblioteca(pedido.id, pedido.nome))
            is PedidoDeAcaoConfirmadaDoChat.ApagarComponenteDaBiblioteca ->
                seguir(apagarComponenteDaBiblioteca(pedido.id))
            is PedidoDeAcaoConfirmadaDoChat.MostrarBotaoFlutuanteDoVideo ->
                seguir(mostrarBotaoFlutuanteDoVideo())
            is PedidoDeAcaoConfirmadaDoChat.AbrirPlayerDoVideoDetectado ->
                seguir(abrirPlayerDoVideoDetectado())
            is PedidoDeAcaoConfirmadaDoChat.AbrirVideoDaBiblioteca ->
                seguir(abrirVideoDaBiblioteca(pedido.chave))
            is PedidoDeAcaoConfirmadaDoChat.AbrirImagemDaBiblioteca ->
                seguir(abrirImagemDaBiblioteca(pedido.id))
            is PedidoDeAcaoConfirmadaDoChat.VincularCampo -> vincularCampo(pedido.campo, pedido.envio, seguir)
            is PedidoDeAcaoConfirmadaDoChat.GerarMarkdown -> {
                mostrarAndamento(activity.getString(R.string.chat_gerando_arquivo))
                val referer = siteDaAba()
                Thread {
                    val mensagem = GravacaoDePacoteGeradoPeloChatNoArmazenamentoDoAplicativo(activity)
                        .gerarMarkdown(pedido, referer, "")
                    activity.runOnUiThread {
                        if (!activity.isFinishing) seguir(mensagem)
                    }
                }.start()
            }
            is PedidoDeAcaoConfirmadaDoChat.GerarZip -> {
                mostrarAndamento(activity.getString(R.string.chat_gerando_arquivo))
                val referer = siteDaAba()
                Thread {
                    val mensagem = GravacaoDePacoteGeradoPeloChatNoArmazenamentoDoAplicativo(activity)
                        .gerarZip(pedido, referer, "")
                    activity.runOnUiThread {
                        if (!activity.isFinishing) seguir(mensagem)
                    }
                }.start()
            }
            is PedidoDeAcaoConfirmadaDoChat.GerarImagemHiggsfield -> {
                mostrarAndamento(activity.getString(R.string.chat_gerando_higgsfield))
                val refDoAnexo = if (GeracaoDeImagemEVideoPeloHiggsfieldNoChat.ehReferenciaDeAnexoDoChat(pedido.idImagem)) {
                    bytesDaUltimaImagemAnexadaNoHistoricoDoChat()
                } else {
                    null
                }
                Thread {
                    val ref = when {
                        pedido.idImagem.isBlank() -> null
                        GeracaoDeImagemEVideoPeloHiggsfieldNoChat.ehReferenciaDeAnexoDoChat(pedido.idImagem) -> refDoAnexo
                        else -> bytesDaImagemDaBiblioteca(pedido.idImagem)
                    }
                    if (pedido.idImagem.isNotBlank() && ref == null) {
                        val aviso = if (GeracaoDeImagemEVideoPeloHiggsfieldNoChat.ehReferenciaDeAnexoDoChat(pedido.idImagem)) {
                            "Não achei imagem anexada nesta conversa para usar de referência. Peça ao usuário para anexar de novo ou use id da biblioteca."
                        } else {
                            "Não achei a imagem ${pedido.idImagem} na biblioteca para usar de referência."
                        }
                        activity.runOnUiThread {
                            if (!activity.isFinishing) seguir(aviso)
                        }
                        return@Thread
                    }
                    val resultado = clienteHiggsfield.gerarImagem(
                        prompt = pedido.prompt,
                        modelo = pedido.modelo,
                        referencia = ref?.first,
                        nomeArquivoReferencia = ref?.second ?: "referencia.jpg",
                        roleReferencia = pedido.roleReferencia,
                    )
                    val mensagem = when (resultado) {
                        is ResultadoDaGeracaoMcpHiggsfield.Erro -> resultado.mensagem
                        is ResultadoDaGeracaoMcpHiggsfield.Pronto ->
                            gravacaoHiggsfield.guardarImagem(
                                resultado.bytes,
                                pedido.prompt,
                                origem = OrigemDaMidiaNaBibliotecaDoNavegador.HIGGSFIELD,
                                modelo = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.resolverModeloImagem(pedido.modelo),
                                refsBytes = listOfNotNull(ref),
                            )
                    }
                    activity.runOnUiThread {
                        if (!activity.isFinishing) seguir(mensagem)
                    }
                }.start()
            }
            is PedidoDeAcaoConfirmadaDoChat.GerarVideoHiggsfield -> {
                mostrarAndamento(activity.getString(R.string.chat_gerando_higgsfield))
                Thread {
                    val resultado = clienteHiggsfield.gerarVideo(pedido.prompt, pedido.modelo)
                    val mensagem = when (resultado) {
                        is ResultadoDaGeracaoMcpHiggsfield.Erro -> resultado.mensagem
                        is ResultadoDaGeracaoMcpHiggsfield.Pronto ->
                            gravacaoHiggsfield.guardarVideo(
                                resultado.bytes,
                                pedido.prompt,
                                origem = OrigemDaMidiaNaBibliotecaDoNavegador.HIGGSFIELD,
                                modelo = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.resolverModeloVideo(pedido.modelo),
                            )
                    }
                    activity.runOnUiThread {
                        if (!activity.isFinishing) seguir(mensagem)
                    }
                }.start()
            }
            is PedidoDeAcaoConfirmadaDoChat.GerarVideoTopview -> {
                mostrarAndamento(activity.getString(R.string.chat_gerando_topview))
                Thread {
                    val resultado = clienteTopview.gerarVideo(
                        prompt = pedido.prompt,
                        modelo = pedido.modelo,
                    )
                    val mensagem = when (resultado) {
                        is ResultadoDaGeracaoMcpHiggsfield.Erro -> resultado.mensagem
                        is ResultadoDaGeracaoMcpHiggsfield.Pronto ->
                            gravacaoHiggsfield.guardarVideo(
                                resultado.bytes,
                                pedido.prompt,
                                origem = OrigemDaMidiaNaBibliotecaDoNavegador.TOPVIEW,
                                modelo = GeracaoDeVideoPeloTopviewNoChat.resolverModelo(pedido.modelo),
                            )
                    }
                    activity.runOnUiThread {
                        if (!activity.isFinishing) seguir(mensagem)
                    }
                }.start()
            }
            is PedidoDeAcaoConfirmadaDoChat.GerarVideoVivix -> {
                mostrarAndamento(activity.getString(R.string.chat_gerando_vivix))
                Thread {
                    val resultado = clienteVivix.gerarVideo(
                        prompt = pedido.prompt,
                        modelo = pedido.modelo,
                        aspectRatio = pedido.proporcao,
                        resolution = pedido.resolucao,
                        duracaoSegundos = pedido.duracao,
                    )
                    val mensagem = when (resultado) {
                        is ResultadoDaGeracaoMcpHiggsfield.Erro -> resultado.mensagem
                        is ResultadoDaGeracaoMcpHiggsfield.Pronto ->
                            gravacaoHiggsfield.guardarVideo(
                                resultado.bytes,
                                pedido.prompt,
                                origem = OrigemDaMidiaNaBibliotecaDoNavegador.VIVIX,
                                modelo = CatalogoDeModelosDeVideoDoVivixNaColecao.nome(pedido.modelo),
                            )
                    }
                    activity.runOnUiThread {
                        if (!activity.isFinishing) seguir(mensagem)
                    }
                }.start()
            }
            is PedidoDeAcaoConfirmadaDoChat.GuardarRoteiroDeAutomacao ->
                seguir(guardarRoteiroDeAutomacao(pedido.roteiro))
            is PedidoDeAcaoConfirmadaDoChat.RodarRoteiroDeAutomacao -> {
                iniciarRoteiroNoChat(pedido.idOuNome, seguir)
            }
        }
    }

    private fun botaoDaConfirmacao(rotulo: String, cor: String, aoClicar: () -> Unit): TextView {
        val densidade = lista.resources.displayMetrics.density
        return TextView(activity).apply {
            text = rotulo
            setTextColor(Color.parseColor(cor))
            textSize = 14f
            val margem = (8 * densidade).toInt()
            setPadding(margem, margem, margem, margem)
            setOnClickListener { aoClicar() }
        }
    }

    private fun removerCartaoDeConfirmacao() {
        cartaoDeConfirmacao?.let { lista.removeView(it) }
        cartaoDeConfirmacao = null
    }

    private fun atualizarBotaoEnviar() {
        val parar = enviando && campoMensagem.text?.toString().orEmpty().isBlank()
        botaoEnviar.setImageResource(
            if (parar) R.drawable.icone_de_parar_resposta_do_chat_da_openrouter
            else R.drawable.icone_de_enviar_mensagem_do_chat_da_openrouter,
        )
        botaoEnviar.contentDescription = activity.getString(
            when {
                parar -> R.string.parar_resposta_do_chat
                enviando -> R.string.orientar_resposta_do_chat
                else -> R.string.enviar_mensagem_do_chat
            },
        )
    }

    private fun prepararNotas() {
        val campoSite = activity.findViewById<EditText>(R.id.campo_do_site_da_nota_do_chat)
        if (campoSite.text.isNullOrBlank()) {
            campoSite.setText(CadernoDeNotasDosSitesParaOChat.chave(siteDaAba()))
        }
        desenharNotas()
        prepararLiberacao()
    }

    private fun prepararLiberacao() {
        val campo = activity.findViewById<EditText>(R.id.campo_do_site_com_navegacao_liberada)
        if (campo.text.isNullOrBlank()) {
            val chave = CadernoDeNotasDosSitesParaOChat.chave(siteDaAba())
            val paginaInicial = chave.isNotEmpty() &&
                EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh("https://$chave/")
            if (chave.isNotEmpty() && !paginaInicial) campo.setText(chave)
        }
        desenharSitesLiberados()
    }

    private fun liberarSiteDaTela() {
        val campo = activity.findViewById<EditText>(R.id.campo_do_site_com_navegacao_liberada)
        val site = campo.text?.toString().orEmpty()
        val erro = sitesComNavegacaoLiberada.liberar(site)
        if (erro.isEmpty()) {
            campo.setText(LiberacaoDeNavegacaoDosSitesParaOChat.chave(site))
            desenharSitesLiberados()
            avisarTexto(activity.getString(R.string.navegacao_do_site_liberada))
        } else {
            avisarTexto(erro)
        }
    }

    private fun desenharSitesLiberados() {
        val listaSites = activity.findViewById<LinearLayout>(R.id.lista_de_sites_com_navegacao_liberada)
        listaSites.removeAllViews()
        val sites = sitesComNavegacaoLiberada.listar()
        atualizarResumosDasSecoesDeSites()
        if (sites.isEmpty()) {
            listaSites.addView(TextView(activity).apply {
                text = activity.getString(R.string.nenhum_site_com_navegacao_liberada)
                setTextColor(Color.parseColor("#8A9099"))
                textSize = 13f
            })
            return
        }
        val densidade = listaSites.resources.displayMetrics.density
        sites.forEach { hospedeiro ->
            listaSites.addView(TextView(activity).apply {
                text = hospedeiro
                setTextColor(Color.parseColor("#F4F7FB"))
                textSize = 13f
                val margem = (8 * densidade).toInt()
                setPadding(0, margem, 0, margem)
                setOnClickListener {
                    sitesComNavegacaoLiberada.tirar(hospedeiro)
                    desenharSitesLiberados()
                    avisarTexto(activity.getString(R.string.liberacao_da_navegacao_removida))
                }
            })
        }
    }

    private fun guardarNotaDaTela() {
        val site = activity.findViewById<EditText>(R.id.campo_do_site_da_nota_do_chat).text?.toString().orEmpty()
        val texto = activity.findViewById<EditText>(R.id.campo_do_texto_da_nota_do_site).text?.toString().orEmpty()
        val erro = cadernoDeNotas.guardar(site, texto)
        if (erro.isEmpty()) {
            val campoSite = activity.findViewById<EditText>(R.id.campo_do_site_da_nota_do_chat)
            campoSite.setText(CadernoDeNotasDosSitesParaOChat.chave(site))
            desenharNotas()
            avisarTexto(activity.getString(R.string.nota_do_site_guardada))
        } else {
            avisarTexto(erro)
        }
    }

    private fun apagarNotaDaTela() {
        val campoSite = activity.findViewById<EditText>(R.id.campo_do_site_da_nota_do_chat)
        val site = campoSite.text?.toString().orEmpty()
        if (CadernoDeNotasDosSitesParaOChat.chave(site).isEmpty()) {
            avisarTexto("Faltou o site.")
            return
        }
        cadernoDeNotas.apagar(site)
        activity.findViewById<EditText>(R.id.campo_do_texto_da_nota_do_site).setText("")
        desenharNotas()
    }

    private fun desenharNotas() {
        val listaNotas = activity.findViewById<LinearLayout>(R.id.lista_de_notas_dos_sites)
        listaNotas.removeAllViews()
        val notas = cadernoDeNotas.listar()
        atualizarResumosDasSecoesDeSites()
        if (notas.isEmpty()) {
            listaNotas.addView(TextView(activity).apply {
                text = activity.getString(R.string.nenhuma_nota_de_site)
                setTextColor(Color.parseColor("#8A9099"))
                textSize = 13f
            })
            return
        }
        val densidade = listaNotas.resources.displayMetrics.density
        notas.forEach { nota ->
            val linha = TextView(activity).apply {
                text = nota.hospedeiro + "\n" + nota.texto.replace('\n', ' ')
                setTextColor(Color.parseColor("#F4F7FB"))
                textSize = 13f
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                val margem = (8 * densidade).toInt()
                setPadding(0, margem, 0, margem)
                setOnClickListener {
                    activity.findViewById<EditText>(R.id.campo_do_site_da_nota_do_chat).setText(nota.hospedeiro)
                    activity.findViewById<EditText>(R.id.campo_do_texto_da_nota_do_site).setText(nota.texto)
                }
            }
            listaNotas.addView(linha)
        }
    }

    private fun bytesDaUltimaImagemAnexadaNoHistoricoDoChat(): Pair<ByteArray, String>? {
        for (mensagem in historico.asReversed()) {
            if (mensagem.papel != "user") continue
            for (parte in mensagem.partes.asReversed()) {
                if (parte.tipo != "imagem" || parte.dados.isBlank()) continue
                val bytes = runCatching {
                    Base64.decode(parte.dados, Base64.DEFAULT)
                }.getOrNull() ?: continue
                if (bytes.isEmpty()) continue
                val nome = parte.nome.ifBlank {
                    when {
                        parte.mime.contains("png") -> "anexo.png"
                        parte.mime.contains("webp") -> "anexo.webp"
                        else -> "anexo.jpg"
                    }
                }
                return bytes to nome
            }
        }
        return null
    }

    private fun abrirMenuDosRoteirosDeAutomacao() {
        val botao = activity.findViewById<View>(R.id.botao_rodar_roteiro_de_automacao_do_chat)
        val itens = carregarRoteirosDeAutomacao()
        if (itens.isEmpty()) {
            avisar(R.string.nenhum_roteiro_de_automacao_guardado)
            return
        }
        val menu = PopupMenu(activity, botao)
        itens.forEachIndexed { indice, roteiro ->
            menu.menu.add(0, indice, indice, "▶ ${roteiro.nome}")
        }
        menu.setOnMenuItemClickListener { item ->
            val roteiro = itens.getOrNull(item.itemId) ?: return@setOnMenuItemClickListener false
            iniciarRoteiroNoChat(roteiro.id) { relato ->
                adicionarBolha(relato, doUsuario = false)
            }
            true
        }
        menu.show()
    }

    private fun iniciarRoteiroNoChat(idOuNome: String, aoFim: (String) -> Unit) {
        roteiroEmAndamentoNoChat = true
        atualizarBotaoDoRoteiroNoChat()
        mostrarAndamento(activity.getString(R.string.chat_rodando_roteiro))
        rodarRoteiroDeAutomacao(
            idOuNome,
            { indice, total, rotulo ->
                mostrarAndamento(
                    activity.getString(
                        R.string.chat_passo_do_roteiro,
                        indice + 1,
                        total,
                        rotulo,
                    ),
                )
            },
            { relato ->
                roteiroEmAndamentoNoChat = false
                atualizarBotaoDoRoteiroNoChat()
                esconderAndamento()
                aoFim(relato)
            },
        )
    }

    private fun atualizarBotaoDoRoteiroNoChat() {
        val botao = activity.findViewById<ImageButton>(R.id.botao_rodar_roteiro_de_automacao_do_chat)
        if (roteiroEmAndamentoNoChat) {
            botao.setImageResource(R.drawable.icone_de_parar_resposta_do_chat_da_openrouter)
            botao.contentDescription = activity.getString(R.string.parar_roteiro_de_automacao_do_chat)
        } else {
            botao.setImageResource(R.drawable.icone_de_reproduzir_do_player_de_video)
            botao.contentDescription = activity.getString(R.string.rodar_roteiro_de_automacao_do_chat)
        }
    }

    private fun avisarTexto(texto: String) {
        Toast.makeText(activity, texto, Toast.LENGTH_SHORT).show()
    }

    private fun avisar(mensagem: Int) {
        Toast.makeText(activity, mensagem, Toast.LENGTH_SHORT).show()
    }

}

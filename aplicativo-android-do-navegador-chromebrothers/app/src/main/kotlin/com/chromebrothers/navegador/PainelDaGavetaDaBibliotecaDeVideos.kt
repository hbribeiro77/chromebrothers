package com.chromebrothers.navegador

import android.content.ClipData
import android.content.ClipDescription
import android.view.HapticFeedbackConstants
import android.view.View.DragShadowBuilder
import android.view.ViewGroup
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import java.io.ByteArrayOutputStream
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.view.animation.PathInterpolator
import android.net.Uri
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs
import kotlin.math.max
import java.util.concurrent.Executors
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.chromebrothers.navegador.regras.AbrirComposicaoVaziaDaEdicao
import com.chromebrothers.navegador.regras.AcompanhamentoDosDownloadsDeMidiaNaGaveta
import com.chromebrothers.navegador.regras.BarraContextualDoEditorDeImagem
import com.chromebrothers.navegador.regras.CoresNoMenuDaFerramenta
import com.chromebrothers.navegador.regras.BibliotecaDeImagensCapturadasDoNavegador
import com.chromebrothers.navegador.regras.DestinoDoToqueNaImagemDaGaveta
import com.chromebrothers.navegador.regras.DownloadDeMidiaNaGaveta
import com.chromebrothers.navegador.regras.FaixaLateralDoEditorDeImagem
import com.chromebrothers.navegador.regras.FerramentaDaBarraDoEditorDeImagem
import com.chromebrothers.navegador.regras.FerramentaEmpilhadaDoEditorDeImagem
import com.chromebrothers.navegador.regras.LinhaSecundariaDoEditorDeImagem
import com.chromebrothers.navegador.regras.MolduraDaComposicaoDeImagem
import com.chromebrothers.navegador.regras.FormaDaSelecaoSobreAImagem
import com.chromebrothers.navegador.regras.MenuDaFerramentaDaFaixaLateral
import com.chromebrothers.navegador.regras.MenuDoPontoDePintura
import com.chromebrothers.navegador.regras.ToqueNaImagemDaGaveta
import com.chromebrothers.navegador.regras.EstadoDoDownloadDeMidiaNaGaveta
import com.chromebrothers.navegador.regras.ExtracaoDeTextoDeImagemViaIaDoNavegador
import com.chromebrothers.navegador.regras.PaletaDoEditorDeImagem
import com.chromebrothers.navegador.regras.PontaDoTracoDaComposicao
import com.chromebrothers.navegador.regras.ProporcaoDaMolduraDaComposicao
import com.chromebrothers.navegador.regras.BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador
import com.chromebrothers.navegador.regras.BibliotecaDeVideosDoNavegador
import com.chromebrothers.navegador.regras.ItemDaBibliotecaDeImagensCapturadas
import com.chromebrothers.navegador.regras.ItemDaBibliotecaDeComponentesHtmlDaColecao
import com.chromebrothers.navegador.regras.ItemDaBibliotecaDeVideos
import com.chromebrothers.navegador.regras.FiltroPorOrigemNaBibliotecaDeMidia
import com.chromebrothers.navegador.regras.OrigemDaMidiaNaBibliotecaDoNavegador
import java.io.File
import com.chromebrothers.navegador.regras.ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor
import com.chromebrothers.navegador.regras.TextoDoProgressoDoDownloadDeMidiaNaGaveta
import com.chromebrothers.navegador.regras.ZoomDaVistaDoEditorDeImagem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class EntradaDaImagemNaEdicao { ENTROU, SEM_EDICAO, SEM_ESPACO, INDISPONIVEL }

class PainelDaGavetaDaBibliotecaDeVideos(
    private val activity: AppCompatActivity,
    private val biblioteca: BibliotecaDeVideosDoNavegador,
    private val gravacao: GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo,
    private val bibliotecaDeImagens: BibliotecaDeImagensCapturadasDoNavegador,
    private val gravacaoDeImagens: GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo,
    private val aoAbrir: (ItemDaBibliotecaDeVideos) -> Unit,
    private val aoFavoritoAlterado: (ItemDaBibliotecaDeVideos?) -> Unit,
    private val acompanhamento: AcompanhamentoDosDownloadsDeMidiaNaGaveta,
    private val aoAbrirDownload: (DownloadDeMidiaNaGaveta) -> Unit,
    private val bibliotecaDePaginas: BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador? = null,
    private val gravacaoDePaginas: GravacaoDaBibliotecaDeComponentesHtmlDaColecaoNoArmazenamentoDoAplicativo? = null,
    private val aoAbrirPagina: (ItemDaBibliotecaDeComponentesHtmlDaColecao) -> Unit = {},
    private val idDaPaginaVinculadaNaColecao: () -> String? = { null },
    private val aoLimparVinculoDaPaginaNaColecao: () -> Unit = {},
) {
    /** Ligado pela Activity: inicia o fantasma "+" do arraste de um item da gaveta (bitmap pode ser null). */
    var aoIniciarArrasteDaBiblioteca: ((EstadoDoArrasteDaBiblioteca, Bitmap?) -> Unit)? = null

    private val painel: View = activity.findViewById(R.id.painel_da_gaveta_da_biblioteca_de_videos)
    private val fundo: View = activity.findViewById(R.id.fundo_da_gaveta_da_biblioteca_de_videos)
    private val gaveta: View = activity.findViewById(R.id.gaveta_da_biblioteca_de_videos)
    private val lista: LinearLayout = activity.findViewById(R.id.lista_da_biblioteca_de_videos)
    private val textoVazio: TextView = activity.findViewById(R.id.texto_vazio_da_biblioteca_de_videos)
    private val botaoRecentes: Button = activity.findViewById(R.id.botao_secao_recentes_da_biblioteca_de_videos)
    private val botaoFavoritos: Button = activity.findViewById(R.id.botao_secao_favoritos_da_biblioteca_de_videos)
    private val botaoVideos: Button = activity.findViewById(R.id.botao_secao_videos_da_biblioteca)
    private val botaoImagens: Button = activity.findViewById(R.id.botao_secao_imagens_da_biblioteca)
    private val botaoBaixados: Button = activity.findViewById(R.id.botao_secao_baixados_da_biblioteca)
    private val botaoPaginas: Button = activity.findViewById(R.id.botao_secao_paginas_da_biblioteca)
    private val botaoBaixarCopiadas: Button = activity.findViewById(R.id.botao_baixar_imagens_copiadas_da_biblioteca)
    private val faixaDeChipsDeOrigem: View = activity.findViewById(R.id.faixa_chips_origem_da_biblioteca)
    private val linhaDeChipsDeOrigem: LinearLayout = activity.findViewById(R.id.linha_chips_origem_da_biblioteca)
    private var origemFiltroAtual = "todos"
    private val grupoDeAcoesDaComposicao: View = activity.findViewById(R.id.grupo_de_acoes_da_composicao_de_imagem)
    private val listaDeRascunhos: LinearLayout = activity.findViewById(R.id.lista_de_rascunhos_da_edicao_de_imagem)
    private val botaoNovaComposicao: Button = activity.findViewById(R.id.botao_nova_composicao_de_imagem)
    private val botaoFecharImagem: ImageButton = activity.findViewById(R.id.botao_fechar_imagem_ampliada_da_biblioteca)
    private val gravacaoDoRascunho = GravacaoDoRascunhoDaEdicaoDeImagem(activity)
    private val gravadorDeRascunho = Executors.newSingleThreadExecutor()
    private var gravacaoPendente: Runnable? = null

    private data class GravacaoAgendada(
        val id: String,
        val exportacao: EditorDeRecorteETracoDaImagemDaBiblioteca.Exportacao,
    )
    private val titulo: TextView = activity.findViewById(R.id.titulo_da_gaveta_da_biblioteca)
    private val visor: View = activity.findViewById(R.id.visor_da_imagem_capturada_da_biblioteca)
    private val imagemAmpliada: ImageView = activity.findViewById(R.id.imagem_ampliada_da_biblioteca)
    private val editor: EditorDeRecorteETracoDaImagemDaBiblioteca = activity.findViewById(R.id.editor_de_recorte_e_traco_da_imagem)
    private val chipDoZoomDaVista: TextView = activity.findViewById(R.id.chip_do_zoom_da_vista_do_editor_de_imagem)
    private val ocultarChipDoZoomDaVista = Runnable { chipDoZoomDaVista.visibility = View.GONE }
    private val atrasoParaOcultarChipDoZoomDaVistaMs = 1000L
    private val barraDeAcoesDaImagem: View = activity.findViewById(R.id.barra_de_acoes_da_imagem_ampliada)
    private val grupoDeAcoesDaImagem: View = activity.findViewById(R.id.grupo_de_acoes_da_imagem_ampliada)
    private val grupoDeFerramentas: View = activity.findViewById(R.id.grupo_de_ferramentas_do_editor_de_imagem)
    private val listaDeCamadas: View = activity.findViewById(R.id.lista_de_camadas_da_edicao)
    private val linhasDasCamadas: LinearLayout = activity.findViewById(R.id.linhas_das_camadas_da_edicao)
    private var arrastandoCamada = false
    private val botaoConcluirEdicao: View = activity.findViewById(R.id.botao_concluir_edicao_da_imagem)
    private val botaoComponentesNoChromeDoEditor: View =
        activity.findViewById(R.id.botao_componentes_no_chrome_do_editor_de_imagem)
    var aoAbrirComponentesDoEditor: (() -> Unit)? = null

    /** Estúdio de pincéis pede uma imagem da galeria para virar forma do pincel. */
    var aoPedirImagemDaFormaDoPincel: (((android.net.Uri?) -> Unit) -> Unit)? = null

    /** Importar pincéis: a atividade abre o seletor de arquivo. */
    var aoPedirArquivoDePinceis: (((android.net.Uri?) -> Unit) -> Unit)? = null

    /** Atalho da gaveta para a tela de Armazenamento (a mesma da Configuração). */
    var aoAbrirArmazenamento: (() -> Unit)? = null

    /** Abre o visualizador de mídia da coleção (flutuando sobre o editor); false se não deu. */
    var aoAbrirVisualizadorDeMidia: (() -> Boolean)? = null

    /** "Usar de novo" no ⓘ: a atividade abre o gerador do tipo e preenche com a receita (ou parcial). */
    var aoUsarDeNovoNoGerador: ((PedidoDeUsarDeNovoNoGerador) -> Unit)? = null
    var aoEncerrarEdicaoDeImagem: (() -> Unit)? = null
    private val barraDeEdicaoDaImagem: View = activity.findViewById(R.id.barra_de_edicao_da_imagem_ampliada)
    private val linhaDeProporcao: View = activity.findViewById(R.id.linha_de_proporcao_do_editor_de_imagem)
    private val linhaDeAcaoDoRecorte: View = activity.findViewById(R.id.linha_de_acao_do_recorte_da_imagem)
    private val linhaDeEspessura: View = activity.findViewById(R.id.linha_de_espessura_do_editor_de_imagem)
    private val linhaDeCor: View = activity.findViewById(R.id.linha_de_cor_do_editor_de_imagem)
    private val botaoRecortar: ImageButton = activity.findViewById(R.id.botao_recortar_imagem_da_biblioteca)
    private val botaoTracoVermelho: ImageButton = activity.findViewById(R.id.botao_traco_vermelho_da_imagem)
    private val botaoTracoBranco: ImageButton = activity.findViewById(R.id.botao_traco_branco_da_imagem)
    private val botaoBorracha: ImageButton = activity.findViewById(R.id.botao_borracha_do_traco_da_imagem)
    private val botaoTexto: ImageButton = activity.findViewById(R.id.botao_texto_da_composicao)
    private val botaoCorDaFolha: ImageButton = activity.findViewById(R.id.botao_cor_da_folha_da_edicao)
    private val botaoOpacidadeDoTraco: Button = activity.findViewById(R.id.botao_opacidade_do_traco)
    private val botaoNanquimDoTraco: Button = activity.findViewById(R.id.botao_nanquim_do_traco)
    private val botaoLapisDoTraco: Button = activity.findViewById(R.id.botao_lapis_do_traco)
    private val botaoSetaDoTraco: Button = activity.findViewById(R.id.botao_seta_do_traco)
    private val botaoDesenharComODedo: Button = activity.findViewById(R.id.botao_desenhar_com_o_dedo)
    private val botaoSelecaoRetangular: Button = activity.findViewById(R.id.botao_selecao_retangular_da_imagem)
    private val botaoSelecaoEmLaco: Button = activity.findViewById(R.id.botao_selecao_em_laco_da_imagem)
    private val botaoSelecaoInteligente: Button = activity.findViewById(R.id.botao_selecao_inteligente_da_imagem)
    private val preferenciasDoEditor = PreferenciasVisuaisEDeNavegacaoDoChromebrothers(activity)
    private val botaoNovaCamada: Button = activity.findViewById(R.id.botao_nova_camada_da_edicao)
    private val amostrasDaPaleta = listOf(
        R.id.botao_cor_branca_do_editor,
        R.id.botao_cor_preta_do_editor,
        R.id.botao_cor_vermelha_do_editor,
        R.id.botao_cor_azul_do_editor,
        R.id.botao_cor_amarela_do_editor,
        R.id.botao_cor_verde_do_editor,
    ).map { activity.findViewById<ImageButton>(it) }
    private val botaoContaGotas: ImageButton = activity.findViewById(R.id.botao_conta_gotas_do_editor)
    private val indicadorDaEspessura: TextView = activity.findViewById(R.id.indicador_da_espessura_do_traco)
    private val quadros = mutableListOf<Bitmap>()
    private val miniaturasDosDownloads = mutableMapOf<String, Bitmap>()
    private val miniaturasEmCarga = mutableSetOf<String>()
    private var geracaoDasMiniaturas = 0
    private val miniaturasDasImagens = mutableMapOf<String, Bitmap>()
    private val imagensEmCarga = mutableSetOf<String>()
    private var geracaoDasMiniaturasDeImagem = 0
    private val rascunhosEmCarga = mutableSetOf<String>()
    private var geracaoDasMiniaturasDeRascunho = 0
    private val miniaturasDasCamadas = mutableMapOf<Int, Bitmap>()
    private var montagemDeCamadasAgendada = false
    private val curvaDaGaveta = PathInterpolator(0.2f, 0f, 0f, 1f)
    private val formatoDaData = SimpleDateFormat("d MMM, HH:mm", Locale("pt", "BR"))
    private var mostrandoFavoritos = false
    private var secao = SecaoDaGaveta.VIDEOS
    private var observacaoDosDownloads: (() -> Unit)? = null
    private var fechando = false
    /** Estado temporário: página descoberta enquanto o arraste da biblioteca está ativo. */
    private var paginaLiberadaNoArrasteDaBiblioteca = false
    private var alphaDoFundoAntesDoArrasteDaBiblioteca = 1f
    private var larguraDoPainelAntesDoArrasteDaBiblioteca = ViewGroup.LayoutParams.MATCH_PARENT
    private var gravityDoPainelAntesDoArrasteDaBiblioteca = Gravity.NO_GRAVITY
    private var guiaDoArrasteDaBiblioteca: androidx.constraintlayout.widget.Guideline? = null
    private var temChaveDaExtracaoDeTexto: () -> Boolean = { false }
    private var aoExtrairTextoDaImagem: ((ByteArray) -> Unit)? = null
    private var imagemAbertaId: String? = null
    private var quadroAmpliado: Bitmap? = null
    private var editandoImagem = false
        set(valor) {
            if (field == valor) return
            field = valor
            aoMudarEdicaoDeImagem?.invoke(valor)
        }

    /** O editor de imagem abriu (true) ou fechou (false): a tela principal entra/sai do modo imersivo. */
    var aoMudarEdicaoDeImagem: ((Boolean) -> Unit)? = null
    private var inserindoNaComposicao = false
    private var gavetaSobreAImagem = false
    private var geracaoDaGavetaSobreAImagem = 0
    private var miniaturaDosRascunhos = mutableMapOf<String, Bitmap>()
    private var chaveDaListaDeRascunhos: String? = null
    private var idDoRascunhoAberto: String? = null
    private var idRetomavelNaFoto: String? = null
    private var ferramentaDaEdicao = EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO
    private var contextoDaBarra = FerramentaDaBarraDoEditorDeImagem.TRACO

    // Chrome "estúdio" do editor (barra do topo, barra lateral, painéis). As fileiras antigas ficam escondidas.
    private var chromeEstudio: ChromeDeEstudioDoEditorDeImagem? = null
    private var sincronizacaoDoChromeAgendada = false
    private val preferenciasDoEstudio = activity.getSharedPreferences("editor-de-imagem-estudio", android.content.Context.MODE_PRIVATE)

    private fun chromeEstudio(): ChromeDeEstudioDoEditorDeImagem =
        chromeEstudio ?: ChromeDeEstudioDoEditorDeImagem(activity, editor.parent as FrameLayout, editor, hospedeDoChrome)
            .also { chromeEstudio = it }

    /** Junta várias mudanças de estado num único redesenho do chrome, depois que o estado já mudou. */
    private fun agendarSincronizacaoDoChrome() {
        if (!editandoImagem || sincronizacaoDoChromeAgendada) return
        sincronizacaoDoChromeAgendada = true
        editor.post {
            sincronizacaoDoChromeAgendada = false
            if (editandoImagem) chromeEstudio?.sincronizar()
        }
    }

    private fun camadasVisiveisNoEstudio() = preferenciasDoEstudio.getBoolean("painel-de-camadas-aberto", false)

    /** Camada com o controle de opacidade aberto no painel (toque na camada ativa). */
    private var idComOpacidadeAberta = -1
    private var camadasAbertasPeloArraste = false
    private val miniaturasDeDesenho = mutableListOf<Bitmap>()

    private val hospedeDoChrome = object : ChromeDeEstudioDoEditorDeImagem.Hospede {
        override fun voltarParaABiblioteca() = sairDaEdicaoGuardandoRascunho()
        override fun inserirDaBiblioteca() = mostrarGavetaSobreAImagem()
        override fun abrirVisualizadorDeMidia() {
            val abrir = aoAbrirVisualizadorDeMidia
            // Sem o componente (ainda não semeado): cai no inserir da biblioteca de antes.
            if (abrir == null || !abrir()) mostrarGavetaSobreAImagem()
        }
        override fun concluir() = mostrarOpcoesDeSalvar()
        override fun componentes() { aoAbrirComponentesDoEditor?.invoke() }
        override fun tamanhoDaMoldura() = pedirTamanhoDaMoldura()
        override fun rotuloDoTamanhoDaMoldura(): String =
            activity.findViewById<Button>(R.id.botao_resolucao_da_moldura_da_composicao).text?.toString().orEmpty()
        override fun voltarAoNavegador() = this@PainelDaGavetaDaBibliotecaDeVideos.voltarAoNavegador()
        override fun descartar() = confirmarDescarteDaEdicao()
        override fun escolherPincel() = alternarMenuDaPintura()
        override fun escolherBorracha() = escolherFerramenta(EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA)
        override fun escolherTexto() = this@PainelDaGavetaDaBibliotecaDeVideos.escolherTexto()
        override fun escolherSelecao() = escolherFerramenta(EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE)
        override fun textoAtivo(): Boolean = contextoDaBarra == FerramentaDaBarraDoEditorDeImagem.TEXTO
        override fun definirProporcao(proporcao: ProporcaoDaMolduraDaComposicao) {
            editor.definirProporcao(proporcao)
            marcarMoldura()
        }
        override fun proporcaoOriginal() {
            if (!editor.definirProporcaoOriginal()) {
                Toast.makeText(activity, R.string.moldura_sem_imagem_para_proporcao_original, Toast.LENGTH_LONG).show()
                return
            }
            marcarMoldura()
        }
        override fun alternarCamadas() {
            val visiveis = !camadasVisiveisNoEstudio()
            preferenciasDoEstudio.edit().putBoolean("painel-de-camadas-aberto", visiveis).apply()
            camadasAbertasPeloArraste = false
            listaDeCamadas.visibility = if (visiveis) View.VISIBLE else View.GONE
            if (visiveis) atualizarListaDeCamadas()
        }
        override fun camadasVisiveis(): Boolean = listaDeCamadas.visibility == View.VISIBLE
        override fun escolherCor(cor: Int) = escolherCorDaPaleta(cor)
        override fun corAtual(): Int =
            if (contextoDaBarra == FerramentaDaBarraDoEditorDeImagem.TEXTO) editor.corDoTextoAtual() else editor.corDoTraco()
        override fun definirCorDaFolha(cor: Int) {
            editor.definirCorDaFolha(cor)
            mostrarCores()
        }
        override fun alternarContaGotas() {
            editor.alternarContaGotas()
            mostrarCores()
        }
        override fun escolherPonta(ponta: PontaDoTracoDaComposicao) {
            editor.escolherPonta(ponta)
            mostrarCores()
        }
        override fun escolherPincelDoMotor(id: String?) {
            editor.escolherPincelDoMotor(id)
            mostrarCores()
        }
        override fun pedirArquivoDePinceis(receber: (android.net.Uri?) -> Unit) {
            val pedir = aoPedirArquivoDePinceis
            if (pedir == null) receber(null) else pedir(receber)
        }
        override fun pedirImagemParaFormaDoPincel(receber: (android.net.Uri?) -> Unit) {
            val pedir = aoPedirImagemDaFormaDoPincel
            if (pedir == null) receber(null) else pedir(receber)
        }
        override fun alternarDesenhoComODedo() {
            editor.alternarDesenhoComODedo()
            mostrarCores()
        }
        override fun desfazer() {
            editor.desfazer()
            marcarMoldura()
        }
        override fun refazer() {
            editor.refazer()
            marcarMoldura()
        }
        override fun espessuraMudou() = mostrarEspessura()
        override fun definirFormaDaSelecao(forma: FormaDaSelecaoSobreAImagem) {
            editor.definirFormaDaSelecao(forma)
            marcarFormaDaSelecao()
        }
        override fun cancelarSelecao() {
            editor.cancelarSelecaoAtivaDoLaco()
            agendarSincronizacaoDoChrome()
        }
    }

    init {
        fundo.setOnClickListener { fecharGavetaOuRecolher() }
        activity.findViewById<View>(R.id.botao_fechar_gaveta_da_biblioteca_de_videos).setOnClickListener { fecharGavetaOuRecolher() }
        activity.findViewById<View>(R.id.botao_armazenamento_da_gaveta_da_biblioteca).setOnClickListener { aoAbrirArmazenamento?.invoke() }
        botaoRecentes.setOnClickListener {
            mostrandoFavoritos = false
            atualizar()
        }
        botaoFavoritos.setOnClickListener {
            mostrandoFavoritos = true
            atualizar()
        }
        botaoVideos.setOnClickListener {
            secao = SecaoDaGaveta.VIDEOS
            atualizar()
        }
        botaoImagens.setOnClickListener {
            secao = SecaoDaGaveta.IMAGENS
            atualizar()
        }
        botaoBaixados.setOnClickListener {
            secao = SecaoDaGaveta.BAIXADOS
            atualizar()
        }
        botaoPaginas.setOnClickListener {
            secao = SecaoDaGaveta.PAGINAS
            atualizar()
        }
        botaoBaixarCopiadas.setOnClickListener { baixarCopiadas() }
        botaoBaixarCopiadas.isAllCaps = false
        botaoBaixarCopiadas.backgroundTintList = null
        botaoBaixarCopiadas.stateListAnimator = null
        botaoBaixarCopiadas.setTextColor(Color.parseColor("#8AB4F8"))
        botaoBaixarCopiadas.background = ColorDrawable(Color.TRANSPARENT)
        botaoBaixarCopiadas.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        activity.findViewById<View>(R.id.botao_mostrar_gaveta_na_imagem_ampliada).setOnClickListener {
            mostrarGavetaSobreAImagem()
        }
        botaoComponentesNoChromeDoEditor.setOnClickListener {
            aoAbrirComponentesDoEditor?.invoke()
        }
        activity.findViewById<View>(R.id.botao_fechar_imagem_ampliada_da_biblioteca).setOnClickListener {
            if (editandoImagem) confirmarDescarteDaEdicao() else fecharVisor()
        }
        activity.findViewById<View>(R.id.botao_voltar_ao_navegador_a_partir_da_imagem).setOnClickListener {
            voltarAoNavegador()
        }
        botaoNovaComposicao.isAllCaps = false
        botaoNovaComposicao.backgroundTintList = null
        botaoNovaComposicao.stateListAnimator = null
        botaoNovaComposicao.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        botaoNovaComposicao.minWidth = 0
        botaoNovaComposicao.minimumWidth = 0
        botaoNovaComposicao.setTextColor(Color.parseColor("#F4F7FB"))
        botaoNovaComposicao.background = ColorDrawable(Color.TRANSPARENT)
        activity.findViewById<View>(R.id.botao_favoritar_imagem_ampliada_da_biblioteca).setOnClickListener {
            val id = imagemAbertaId ?: return@setOnClickListener
            val atual = bibliotecaDeImagens.buscar(id) ?: return@setOnClickListener
            bibliotecaDeImagens.definirFavorito(id, !atual.favorito)
            gravacaoDeImagens.gravar(bibliotecaDeImagens)
            atualizar()
        }
        activity.findViewById<View>(R.id.botao_copiar_imagem_ampliada_da_biblioteca).setOnClickListener {
            copiarImagemAberta()
        }
        imagemAmpliada.setOnLongClickListener {
            if (editandoImagem || imagemAmpliada.visibility != View.VISIBLE) return@setOnLongClickListener false
            mostrarOpcoesDoToqueLongoNaImagemAmpliada()
            true
        }
        activity.findViewById<View>(R.id.botao_renomear_imagem_ampliada_da_biblioteca).setOnClickListener {
            val id = imagemAbertaId ?: return@setOnClickListener
            val item = bibliotecaDeImagens.buscar(id) ?: return@setOnClickListener
            pedirNovoNome(item)
        }
        activity.findViewById<View>(R.id.botao_baixar_imagem_ampliada_da_biblioteca).setOnClickListener {
            val id = imagemAbertaId ?: return@setOnClickListener
            val item = bibliotecaDeImagens.buscar(id) ?: return@setOnClickListener
            baixar(item)
        }
        activity.findViewById<View>(R.id.botao_editar_imagem_ampliada_da_biblioteca).setOnClickListener { abrirEdicao() }
        botaoRecortar.setOnClickListener { alternarMenuDaFaixa(EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE) }
        botaoTracoVermelho.setOnClickListener { alternarMenuDaPintura() }
        botaoBorracha.setOnClickListener { alternarMenuDaFaixa(EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA) }
        activity.findViewById<View>(R.id.botao_diminuir_espessura_do_traco).setOnClickListener {
            editor.diminuirEspessura()
            mostrarEspessura()
        }
        activity.findViewById<View>(R.id.botao_aumentar_espessura_do_traco).setOnClickListener {
            editor.aumentarEspessura()
            mostrarEspessura()
        }
        activity.findViewById<View>(R.id.botao_desfazer_edicao_da_imagem).setOnClickListener {
            editor.desfazer()
            marcarMoldura()
        }
        activity.findViewById<View>(R.id.botao_refazer_edicao_da_imagem).setOnClickListener {
            editor.refazer()
            marcarMoldura()
        }
        botaoCorDaFolha.setOnClickListener {
            val cores = PaletaDoEditorDeImagem.cores
            val proxima = cores[(PaletaDoEditorDeImagem.indice(editor.corDaFolha()) + 1) % cores.size]
            editor.definirCorDaFolha(proxima)
            mostrarCores()
        }
        botaoOpacidadeDoTraco.setOnClickListener {
            editor.escolherPonta(PontaDoTracoDaComposicao.MARCADOR)
            mostrarCores()
        }
        botaoNanquimDoTraco.setOnClickListener {
            editor.escolherPonta(PontaDoTracoDaComposicao.NANQUIM)
            mostrarCores()
        }
        botaoLapisDoTraco.setOnClickListener {
            editor.escolherPonta(PontaDoTracoDaComposicao.LAPIS)
            mostrarCores()
        }
        botaoSetaDoTraco.setOnClickListener {
            editor.escolherPonta(PontaDoTracoDaComposicao.SETA)
            mostrarCores()
        }
        botaoDesenharComODedo.setOnClickListener {
            editor.alternarDesenhoComODedo()
            mostrarCores()
        }
        botaoNovaCamada.isAllCaps = false
        botaoNovaCamada.backgroundTintList = null
        botaoNovaCamada.stateListAnimator = null
        botaoNovaCamada.setOnClickListener {
            if (!editor.criarCamadaVazia()) {
                Toast.makeText(activity, R.string.limite_de_imagens_da_composicao, Toast.LENGTH_LONG).show()
            }
        }
        amostrasDaPaleta.forEachIndexed { indice, botao ->
            botao.setOnClickListener { escolherCorDaPaleta(PaletaDoEditorDeImagem.cores[indice]) }
        }
        botaoContaGotas.setOnClickListener {
            editor.alternarContaGotas()
            mostrarCores()
        }
        botaoConcluirEdicao.setOnClickListener { concluirEdicao() }
        activity.findViewById<View>(R.id.botao_texto_da_composicao).setOnClickListener { escolherTexto() }
        activity.findViewById<View>(R.id.botao_moldura_um_por_um).setOnClickListener {
            editor.definirProporcao(ProporcaoDaMolduraDaComposicao.QUADRADO)
            marcarMoldura()
        }
        activity.findViewById<View>(R.id.botao_moldura_quatro_por_tres).setOnClickListener {
            editor.definirProporcao(ProporcaoDaMolduraDaComposicao.QUATRO_POR_TRES)
            marcarMoldura()
        }
        activity.findViewById<View>(R.id.botao_moldura_dezesseis_por_nove).setOnClickListener {
            editor.definirProporcao(ProporcaoDaMolduraDaComposicao.DEZESSEIS_POR_NOVE)
            marcarMoldura()
        }
        activity.findViewById<View>(R.id.botao_moldura_nove_por_dezesseis).setOnClickListener {
            editor.definirProporcao(ProporcaoDaMolduraDaComposicao.NOVE_POR_DEZESSEIS)
            marcarMoldura()
        }
        activity.findViewById<View>(R.id.botao_moldura_proporcao_original).setOnClickListener {
            if (!editor.definirProporcaoOriginal()) {
                Toast.makeText(
                    activity,
                    R.string.moldura_sem_imagem_para_proporcao_original,
                    Toast.LENGTH_LONG,
                ).show()
                return@setOnClickListener
            }
            marcarMoldura()
        }
        activity.findViewById<View>(R.id.botao_moldura_livre).setOnClickListener { pedirTamanhoDaMoldura() }
        activity.findViewById<View>(R.id.botao_resolucao_da_moldura_da_composicao).setOnClickListener {
            pedirTamanhoDaMoldura()
        }
        botaoSelecaoRetangular.setOnClickListener {
            editor.definirFormaDaSelecao(FormaDaSelecaoSobreAImagem.RETANGULO)
            marcarFormaDaSelecao()
        }
        botaoSelecaoEmLaco.setOnClickListener {
            editor.definirFormaDaSelecao(FormaDaSelecaoSobreAImagem.LACO)
            marcarFormaDaSelecao()
        }
        botaoSelecaoInteligente.setOnClickListener {
            editor.alternarSelecaoInteligente()
            marcarFormaDaSelecao()
        }
        // Mover mexe dentro da própria camada (o pedaço volta a ela ao encerrar a moldura);
        // quem quer o pedaço noutra camada usa Copiar e cola na camada escolhida.
        editor.moverCriandoNovaCamada = false
        activity.findViewById<View>(R.id.botao_aplicar_recorte_da_imagem).setOnClickListener {
            if (
                editor.ferramentaAtual() == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE &&
                !editor.temImagemAlvoDoRecorte()
            ) {
                Toast.makeText(activity, R.string.recorte_precisa_escolher_imagem, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            editor.definirFerramenta(EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE)
        }
        activity.findViewById<View>(R.id.botao_destacar_pedaco_da_imagem).setOnClickListener {
            if (!editor.temImagemAlvoDoRecorte()) {
                Toast.makeText(activity, R.string.recorte_precisa_escolher_imagem, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!editor.destacarRetangulo()) {
                Toast.makeText(activity, R.string.limite_de_imagens_da_composicao, Toast.LENGTH_LONG).show()
            }
        }
        botaoNovaComposicao.setOnClickListener { abrirComposicaoVazia() }
        editor.aoMudarFerramenta = {
            ferramentaDaEdicao = it
            contextoDaBarra = contextoDaFerramenta(it)
            marcarFerramenta(it)
            atualizarLinhaSecundaria()
        }
        editor.aoMudarSelecao = {
            mostrarCores()
            if (!arrastandoCamada) atualizarListaDeCamadas()
            if (linhaDeAcaoDoRecorte.visibility == View.VISIBLE) marcarFormaDaSelecao()
        }
        editor.aoMudarModoDoDedo = {
            if (linhaDeAcaoDoRecorte.visibility == View.VISIBLE) marcarFormaDaSelecao()
        }
        editor.aoMudarSelecaoAtivaDoLaco = { atualizarAcoesDoRecorteComSelecaoAtiva() }
        editor.aoPedirConfirmacaoDeRecorteDaSelecaoAtiva = { pedirConfirmacaoDeRecorteDaSelecaoAtiva() }
        editor.aoPegarCor = { escolherCorDaPaleta(it) }
        editor.aoMudarZoomDaVista = { zoom -> mostrarChipDoZoomDaVista(zoom) }
        editor.aoGestoDesfazer = {
            // O editor já desfez (gesto antigo de 2 dedos): só atualiza a tela e avisa.
            if (editandoImagem) {
                marcarMoldura()
                mostrarAvisoNoChip(activity.getString(R.string.desfazer_edicao_da_imagem))
            }
        }
        editor.aoGestoRefazer = {
            if (editandoImagem) {
                editor.refazer()
                marcarMoldura()
                mostrarAvisoNoChip(activity.getString(R.string.refazer_edicao_da_imagem))
            }
        }
        chipDoZoomDaVista.setOnClickListener { editor.restaurarZoomDaVista() }
    }

    private fun mostrarChipDoZoomDaVista(zoom: Float) {
        if (!editandoImagem) return
        chipDoZoomDaVista.removeCallbacks(ocultarChipDoZoomDaVista)
        val percentual = ZoomDaVistaDoEditorDeImagem.percentualInteiro(zoom)
        val graus = editor.anguloDaVistaEmGraus()
        chipDoZoomDaVista.text = com.chromebrothers.navegador.regras.RotacaoDaVistaDoEditorDeImagem.rotuloDoChip(zoom, graus)
        val descricaoDoZoom = activity.getString(R.string.descricao_do_chip_do_zoom_da_vista_do_editor, percentual)
        val grausInteiros = com.chromebrothers.navegador.regras.RotacaoDaVistaDoEditorDeImagem.grausInteiros(graus)
        chipDoZoomDaVista.contentDescription =
            if (grausInteiros == 0) descricaoDoZoom else "$descricaoDoZoom Vista girada $grausInteiros graus."
        chipDoZoomDaVista.visibility = View.VISIBLE
        chipDoZoomDaVista.bringToFront()
        chipDoZoomDaVista.postDelayed(ocultarChipDoZoomDaVista, atrasoParaOcultarChipDoZoomDaVistaMs)
    }

    /** Aviso curto no chip do canto (o mesmo do zoom): “Desfazer”, “Refazer”. */
    private fun mostrarAvisoNoChip(texto: String) {
        if (!editandoImagem) return
        chipDoZoomDaVista.removeCallbacks(ocultarChipDoZoomDaVista)
        chipDoZoomDaVista.text = texto
        chipDoZoomDaVista.contentDescription = texto
        chipDoZoomDaVista.visibility = View.VISIBLE
        chipDoZoomDaVista.bringToFront()
        chipDoZoomDaVista.postDelayed(ocultarChipDoZoomDaVista, 900L)
    }

    private fun ocultarChipDoZoomDaVistaAgora() {
        chipDoZoomDaVista.removeCallbacks(ocultarChipDoZoomDaVista)
        chipDoZoomDaVista.visibility = View.GONE
    }

    fun estaAberto(): Boolean = painel.visibility == View.VISIBLE

    /**
     * Durante o arraste da biblioteca: tira o escurecimento e encolhe o painel à largura da
     * gaveta (direita), deixando a página à esquerda descoberta e ativa. A gaveta permanece aberta.
     */
    fun liberarPaginaSobArrasteDaBiblioteca() {
        if (paginaLiberadaNoArrasteDaBiblioteca) return
        if (painel.visibility != View.VISIBLE) return
        paginaLiberadaNoArrasteDaBiblioteca = true
        fundo.animate().cancel()
        alphaDoFundoAntesDoArrasteDaBiblioteca = fundo.alpha
        fundo.alpha = 0f
        fundo.isClickable = false
        fundo.visibility = View.INVISIBLE
        val guia = activity.findViewById<androidx.constraintlayout.widget.Guideline>(
            R.id.meio_da_tela_da_gaveta_da_biblioteca,
        )
        guiaDoArrasteDaBiblioteca = guia
        guia.setGuidelinePercent(0f)
        val params = painel.layoutParams
        larguraDoPainelAntesDoArrasteDaBiblioteca = params.width
        val larguraDaGaveta = gaveta.width.takeIf { it > 0 }
            ?: (painel.width / 2).coerceAtLeast(1)
        params.width = larguraDaGaveta
        if (params is FrameLayout.LayoutParams) {
            gravityDoPainelAntesDoArrasteDaBiblioteca = params.gravity
            params.gravity = android.view.Gravity.END or android.view.Gravity.TOP
        }
        painel.layoutParams = params
        painel.isClickable = true
        gaveta.isClickable = true
    }

    /**
     * Restaura fundo e painel em tela cheia após o arraste.
     * [comFade] evita o flash preto no soltar (o dim volta aos poucos).
     */
    fun restaurarBloqueioAposArrasteDaBiblioteca(comFade: Boolean = false) {
        if (!paginaLiberadaNoArrasteDaBiblioteca) return
        paginaLiberadaNoArrasteDaBiblioteca = false
        guiaDoArrasteDaBiblioteca?.setGuidelinePercent(0.5f)
        guiaDoArrasteDaBiblioteca = null
        val params = painel.layoutParams
        params.width = larguraDoPainelAntesDoArrasteDaBiblioteca
            .takeIf { it != 0 }
            ?: ViewGroup.LayoutParams.MATCH_PARENT
        if (params is FrameLayout.LayoutParams) {
            params.gravity = gravityDoPainelAntesDoArrasteDaBiblioteca
        }
        painel.layoutParams = params
        painel.isClickable = true
        val alphaAlvo = alphaDoFundoAntesDoArrasteDaBiblioteca.coerceIn(0f, 1f).let {
            if (it < 0.05f) 1f else it
        }
        fundo.animate().cancel()
        fundo.visibility = View.VISIBLE
        if (painel.visibility == View.VISIBLE && !fechando && comFade) {
            fundo.alpha = 0f
            fundo.isClickable = true
            fundo.animate()
                .alpha(alphaAlvo)
                .setDuration(280L)
                .setInterpolator(curvaDaGaveta)
                .start()
        } else {
            fundo.alpha = if (painel.visibility == View.VISIBLE && !fechando) alphaAlvo else 1f
            fundo.isClickable = true
        }
        larguraDoPainelAntesDoArrasteDaBiblioteca = ViewGroup.LayoutParams.MATCH_PARENT
        gravityDoPainelAntesDoArrasteDaBiblioteca = Gravity.NO_GRAVITY
        alphaDoFundoAntesDoArrasteDaBiblioteca = 1f
    }

    fun tratarVolta(): Boolean {
        if (editandoImagem && editor.selecaoAtivaDaFerramentaDeRecorte()) {
            editor.cancelarSelecaoAtivaDoLaco()
            return true
        }
        if (gavetaSobreAImagem) {
            recolherGavetaSobreAImagem()
            return true
        }
        if (editandoImagem) {
            sairDaEdicaoGuardandoRascunho()
            return true
        }
        if (visor.visibility == View.VISIBLE) {
            fecharVisor()
            return true
        }
        if (estaAberto()) {
            esconder()
            return true
        }
        return false
    }

    /** Sai da edição guardando o rascunho (voltar do sistema e "Biblioteca" no topo do editor). */
    private fun sairDaEdicaoGuardandoRascunho() {
        if (!editandoImagem) return
        prepararGravacaoAoSair()
        val tinhaFoto = quadroAmpliado != null
        val id = idDoRascunhoAberto
        abandonarEdicaoNaMemoria()
        idDoRascunhoAberto = null
        idRetomavelNaFoto = if (tinhaFoto) id else null
        if (tinhaFoto) {
            imagemAmpliada.visibility = View.VISIBLE
            visor.bringToFront()
        } else {
            visor.visibility = View.GONE
        }
        atualizar()
    }

    fun mostrar() {
        fechando = false
        gaveta.animate().cancel()
        fundo.animate().cancel()
        biblioteca.substituirPor(gravacao.carregar().listar())
        bibliotecaDeImagens.substituirPor(gravacaoDeImagens.carregar().listar())
        val paginas = bibliotecaDePaginas
        val gravacaoDasPaginas = gravacaoDePaginas
        if (paginas != null && gravacaoDasPaginas != null) {
            paginas.substituirPor(gravacaoDasPaginas.carregar().listar())
        }
        fundo.alpha = 0f
        painel.visibility = View.VISIBLE
        atualizar()
        val observador = gaveta.viewTreeObserver
        observador.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (observador.isAlive) observador.removeOnPreDrawListener(this)
                if (painel.visibility != View.VISIBLE || fechando) return true
                val distancia = gaveta.width.toFloat()
                if (distancia <= 0f) return true
                gaveta.translationX = distancia
                gaveta.animate()
                    .translationX(0f)
                    .setDuration(320)
                    .setInterpolator(curvaDaGaveta)
                    .start()
                fundo.animate()
                    .alpha(1f)
                    .setDuration(320)
                    .setInterpolator(curvaDaGaveta)
                    .start()
                return true
            }
        })
    }

    fun guardarRascunhoSeEstiverEditando() {
        val exportacao = exportacaoParaGravar() ?: return
        editor.liberarCachesDeTinta()
        gravadorDeRascunho.execute { gravacaoDoRascunho.guardar(exportacao.id, exportacao.exportacao) }
    }

    private fun prepararGravacaoAoSair() {
        gravacaoPendente = null
        val exportacao = exportacaoParaGravar() ?: return
        val bitmaps = exportacao.exportacao.imagens.map { it.bitmap }.distinct()
        editor.emprestarBitmaps(bitmaps)
        gravacaoPendente = Runnable {
            try {
                gravacaoDoRascunho.guardar(exportacao.id, exportacao.exportacao)
            } finally {
                bitmaps.forEach { editor.devolverBitmap(it) }
            }
        }
    }

    private fun exportacaoParaGravar(): GravacaoAgendada? {
        if (!editandoImagem || !editor.temGravacaoPendente()) return null
        val exportacao = editor.exportar() ?: return null
        val id = idDoRascunhoAberto ?: gravacaoDoRascunho.novoId().also { idDoRascunhoAberto = it }
        chaveDaListaDeRascunhos = null
        return GravacaoAgendada(id, exportacao)
    }

    private fun dispararGravacaoDoRascunho() {
        val tarefa = gravacaoPendente ?: return
        gravacaoPendente = null
        gravadorDeRascunho.execute(tarefa)
    }

    fun voltarAoNavegador() {
        esconder()
    }

    fun esconder() {
        if (painel.visibility != View.VISIBLE || fechando) return
        restaurarBloqueioAposArrasteDaBiblioteca()
        prepararGravacaoAoSair()
        if (editandoImagem) abandonarEdicaoNaMemoria()
        idDoRascunhoAberto = null
        idRetomavelNaFoto = null
        soltarSobreposicaoDaGaveta()
        fecharVisor()
        soltarObservacaoDosDownloads()
        fechando = true
        gaveta.animate().cancel()
        fundo.animate().cancel()
        val distancia = gaveta.width.toFloat().takeIf { it > 0f } ?: (painel.width * 0.5f)
        gaveta.animate()
            .translationX(distancia)
            .setDuration(240)
            .setInterpolator(curvaDaGaveta)
            .withEndAction {
                fechando = false
                painel.visibility = View.GONE
                gaveta.translationX = 0f
                fundo.alpha = 1f
                esvaziarLista()
                soltarMiniaturasDosDownloads()
                soltarMiniaturasDasImagens()
            }
            .start()
        fundo.animate()
            .alpha(0f)
            .setDuration(240)
            .setInterpolator(curvaDaGaveta)
            .start()
    }

    fun atualizar() {
        marcarAba(botaoVideos, selecionado = secao == SecaoDaGaveta.VIDEOS, principal = true)
        marcarAba(botaoImagens, selecionado = secao == SecaoDaGaveta.IMAGENS, principal = true)
        marcarAba(botaoBaixados, selecionado = secao == SecaoDaGaveta.BAIXADOS, principal = true)
        botaoPaginas.visibility = if (bibliotecaDePaginas == null) View.GONE else View.VISIBLE
        marcarAba(botaoPaginas, selecionado = secao == SecaoDaGaveta.PAGINAS, principal = true)
        val mostraFiltro = secao == SecaoDaGaveta.VIDEOS || secao == SecaoDaGaveta.IMAGENS
        botaoRecentes.visibility = if (mostraFiltro) View.VISIBLE else View.GONE
        botaoFavoritos.visibility = if (mostraFiltro) View.VISIBLE else View.GONE
        if (mostraFiltro) {
            marcarAba(botaoRecentes, selecionado = !mostrandoFavoritos, principal = false)
            marcarAba(botaoFavoritos, selecionado = mostrandoFavoritos, principal = false)
        }
        titulo.setText(
            when (secao) {
                SecaoDaGaveta.IMAGENS -> R.string.secao_imagens_da_biblioteca
                SecaoDaGaveta.BAIXADOS -> R.string.secao_baixados_da_biblioteca
                SecaoDaGaveta.PAGINAS -> R.string.secao_paginas_da_biblioteca
                SecaoDaGaveta.VIDEOS -> R.string.biblioteca_de_videos
            },
        )
        val naAbaDeImagens = secao == SecaoDaGaveta.IMAGENS
        grupoDeAcoesDaComposicao.visibility = if (naAbaDeImagens) View.VISIBLE else View.GONE
        desenharRascunhos(naAbaDeImagens)
        desenharChipsDeOrigem()
        val temCopiaSemBaixar = bibliotecaDeImagens.copiadasSemBaixar().isNotEmpty()
        botaoBaixarCopiadas.visibility = if (secao == SecaoDaGaveta.IMAGENS && !mostrandoFavoritos && temCopiaSemBaixar) {
            View.VISIBLE
        } else {
            View.GONE
        }
        desenharLista()
        if (secao == SecaoDaGaveta.BAIXADOS && estaAberto()) {
            ligarObservacaoDosDownloads()
        } else {
            soltarObservacaoDosDownloads()
        }
    }

    private fun desenharRascunhos(naAbaDeImagens: Boolean) {
        if (!naAbaDeImagens) {
            if (chaveDaListaDeRascunhos != null) soltarMiniaturasDosRascunhos()
            return
        }
        val ids = gravacaoDoRascunho.listar()
        val chave = ids.joinToString("|") { "$it:${gravacaoDoRascunho.instante(it)}" }
        if (chave == chaveDaListaDeRascunhos) return
        soltarMiniaturasDosRascunhos()
        val densidade = activity.resources.displayMetrics.density
        val lado = (64 * densidade).toInt().coerceAtLeast(1)
        ids.forEach { id -> listaDeRascunhos.addView(linhaDoRascunho(id, lado, densidade)) }
        chaveDaListaDeRascunhos = chave
    }

    private fun linhaDoRascunho(id: String, lado: Int, densidade: Float): View {
        val linha = LinearLayout(activity)
        linha.orientation = LinearLayout.HORIZONTAL
        linha.gravity = Gravity.CENTER_VERTICAL
        val folga = (4 * densidade).toInt()
        linha.setPadding(0, folga, 0, folga)
        linha.contentDescription = activity.getString(R.string.continuar_edicao_da_imagem)
        linha.setOnClickListener { continuarEdicao(id) }
        val toque = android.util.TypedValue()
        activity.theme.resolveAttribute(android.R.attr.selectableItemBackground, toque, true)
        linha.setBackgroundResource(toque.resourceId)
        val imagem = ImageView(activity)
        imagem.layoutParams = LinearLayout.LayoutParams(lado, lado)
        imagem.scaleType = ImageView.ScaleType.CENTER_CROP
        imagem.background = GradientDrawable().apply {
            cornerRadius = 8f * densidade
            setColor(Color.parseColor("#22262E"))
        }
        imagem.outlineProvider = ViewOutlineProvider.BACKGROUND
        imagem.clipToOutline = true
        mostrarMiniaturaDoRascunho(id, imagem, lado)
        val texto = TextView(activity)
        texto.setText(R.string.continuar_edicao_da_imagem)
        texto.setTextColor(Color.parseColor("#8AB4F8"))
        texto.textSize = 16f
        texto.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { marginStart = (12 * densidade).toInt() }
        linha.addView(imagem)
        linha.addView(texto)
        return linha
    }

    private fun soltarMiniaturasDosRascunhos() {
        geracaoDasMiniaturasDeRascunho += 1
        rascunhosEmCarga.clear()
        listaDeRascunhos.removeAllViews()
        miniaturaDosRascunhos.values.forEach { if (!it.isRecycled) it.recycle() }
        miniaturaDosRascunhos.clear()
        chaveDaListaDeRascunhos = null
    }

    private fun mostrarMiniaturaDoRascunho(id: String, imagem: ImageView, lado: Int) {
        val pronta = miniaturaDosRascunhos[id]
        if (pronta != null && !pronta.isRecycled) {
            imagem.setImageBitmap(pronta)
            return
        }
        if (id in rascunhosEmCarga) return
        rascunhosEmCarga += id
        val geracao = geracaoDasMiniaturasDeRascunho
        Thread {
            val quadro = gravacaoDoRascunho.arquivoDaMiniatura(id)?.let { gravacaoDoRascunho.miniatura(it, lado) }
            activity.runOnUiThread {
                rascunhosEmCarga.remove(id)
                if (geracao != geracaoDasMiniaturasDeRascunho || quadro == null) {
                    if (quadro != null && !quadro.isRecycled) quadro.recycle()
                    return@runOnUiThread
                }
                val existente = miniaturaDosRascunhos[id]
                if (existente != null && !existente.isRecycled) {
                    quadro.recycle()
                    if (imagem.parent != null) imagem.setImageBitmap(existente)
                    return@runOnUiThread
                }
                miniaturaDosRascunhos[id] = quadro
                if (imagem.parent != null) imagem.setImageBitmap(quadro)
            }
        }.start()
    }

    private fun videosBaseDaSecao(): List<ItemDaBibliotecaDeVideos> =
        if (mostrandoFavoritos) biblioteca.favoritos() else biblioteca.recentes()

    private fun imagensBaseDaSecao(): List<ItemDaBibliotecaDeImagensCapturadas> =
        if (mostrandoFavoritos) bibliotecaDeImagens.favoritos() else bibliotecaDeImagens.historico()

    private val origensDosChips = listOf(
        OrigemDaMidiaNaBibliotecaDoNavegador.HIGGSFIELD,
        OrigemDaMidiaNaBibliotecaDoNavegador.SJINN,
        OrigemDaMidiaNaBibliotecaDoNavegador.TOPVIEW,
        OrigemDaMidiaNaBibliotecaDoNavegador.MAGNIFIC,
        OrigemDaMidiaNaBibliotecaDoNavegador.VIVIX,
        OrigemDaMidiaNaBibliotecaDoNavegador.PAGINA,
        OrigemDaMidiaNaBibliotecaDoNavegador.DESCONHECIDO,
    )

    private fun desenharChipsDeOrigem() {
        val naSecaoDeMidia = secao == SecaoDaGaveta.VIDEOS || secao == SecaoDaGaveta.IMAGENS
        if (!naSecaoDeMidia) {
            faixaDeChipsDeOrigem.visibility = View.GONE
            linhaDeChipsDeOrigem.removeAllViews()
            return
        }
        val contagem = mutableMapOf<String, Int>()
        val origensDaLista = if (secao == SecaoDaGaveta.VIDEOS) {
            videosBaseDaSecao().map { it.origem }
        } else {
            imagensBaseDaSecao().map { it.origem }
        }
        origensDaLista.forEach { bruto ->
            val origem = OrigemDaMidiaNaBibliotecaDoNavegador.parse(bruto)
            contagem[origem] = (contagem[origem] ?: 0) + 1
        }
        val visiveis = origensDosChips.filter { (contagem[it] ?: 0) > 0 }
        if (origemFiltroAtual != "todos" && origemFiltroAtual !in visiveis) origemFiltroAtual = "todos"
        faixaDeChipsDeOrigem.visibility = View.VISIBLE
        linhaDeChipsDeOrigem.removeAllViews()
        linhaDeChipsDeOrigem.addView(
            chipDeOrigem(activity.getString(R.string.chip_todos_da_origem_da_biblioteca), "todos"),
        )
        visiveis.forEach { origem ->
            linhaDeChipsDeOrigem.addView(
                chipDeOrigem(OrigemDaMidiaNaBibliotecaDoNavegador.rotulo(origem), origem),
            )
        }
    }

    private fun chipDeOrigem(rotulo: String, valor: String): Button {
        val densidade = activity.resources.displayMetrics.density
        val selecionado = origemFiltroAtual == valor
        val chip = Button(activity)
        chip.text = rotulo
        chip.isAllCaps = false
        chip.textSize = 13f
        chip.minWidth = 0
        chip.minimumWidth = 0
        chip.minHeight = 0
        chip.minimumHeight = 0
        chip.backgroundTintList = null
        chip.stateListAnimator = null
        chip.setTypeface(null, if (selecionado) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        chip.setTextColor(Color.parseColor(if (selecionado) "#8AB4F8" else "#8A9099"))
        chip.background = GradientDrawable().apply {
            cornerRadius = 16f * densidade
            setColor(Color.parseColor(if (selecionado) "#2A3447" else "#22262E"))
        }
        val horizontal = (12 * densidade).toInt()
        val vertical = (6 * densidade).toInt()
        chip.setPadding(horizontal, vertical, horizontal, vertical)
        chip.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            (32 * densidade).toInt(),
        ).apply { marginEnd = (8 * densidade).toInt() }
        chip.setOnClickListener {
            origemFiltroAtual = valor
            atualizar()
        }
        return chip
    }

    private fun botaoDeInformacao(lado: Int, aoToque: () -> Unit): TextView {
        val botao = TextView(activity)
        botao.text = "ⓘ"
        botao.gravity = Gravity.CENTER
        botao.textSize = 20f
        botao.setTextColor(Color.parseColor("#8AB4F8"))
        botao.contentDescription = activity.getString(R.string.detalhe_da_midia_na_biblioteca)
        botao.isClickable = true
        botao.layoutParams = LinearLayout.LayoutParams(lado, lado)
        botao.setOnClickListener { aoToque() }
        return botao
    }

    private fun desenharLista() {
        esvaziarLista()
        when (secao) {
            SecaoDaGaveta.IMAGENS -> {
                val imagens = FiltroPorOrigemNaBibliotecaDeMidia.filtrarImagens(
                    imagensBaseDaSecao(),
                    origemFiltroAtual,
                )
                textoVazio.setText(
                    if (mostrandoFavoritos) R.string.favoritos_de_imagens_vazio else R.string.historico_de_imagens_vazio,
                )
                textoVazio.visibility = if (imagens.isEmpty()) View.VISIBLE else View.GONE
                imagens.forEach { item -> lista.addView(cartaoDeImagem(item)) }
                val aberta = imagemAbertaId?.let { bibliotecaDeImagens.buscar(it) }
                if (imagemAbertaId != null && aberta == null && !editandoImagem) fecharVisor() else if (aberta != null) atualizarBotoesDoVisor(aberta)
            }
            SecaoDaGaveta.BAIXADOS -> {
                val baixados = acompanhamento.listar(
                    LeituraDosArquivosDeMidiaBaixadosNaPastaDoChromeBrothers.listar(activity.contentResolver),
                )
                textoVazio.setText(R.string.baixados_da_biblioteca_vazios)
                textoVazio.visibility = if (baixados.isEmpty()) View.VISIBLE else View.GONE
                baixados.forEach { item -> lista.addView(cartaoDeDownload(item)) }
            }
            SecaoDaGaveta.VIDEOS -> {
                val itens = FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(
                    videosBaseDaSecao(),
                    origemFiltroAtual,
                )
                textoVazio.setText(
                    if (mostrandoFavoritos) R.string.favoritos_de_videos_vazio else R.string.historico_de_videos_vazio,
                )
                textoVazio.visibility = if (itens.isEmpty()) View.VISIBLE else View.GONE
                itens.forEach { item -> lista.addView(cartao(item)) }
            }
            SecaoDaGaveta.PAGINAS -> {
                val paginas = bibliotecaDePaginas?.listar().orEmpty()
                textoVazio.setText(R.string.paginas_da_biblioteca_vazias)
                textoVazio.visibility = if (paginas.isEmpty()) View.VISIBLE else View.GONE
                paginas.forEach { item -> lista.addView(cartaoDePagina(item)) }
            }
        }
    }

    private fun cartaoDePagina(item: ItemDaBibliotecaDeComponentesHtmlDaColecao): View {
        val densidade = activity.resources.displayMetrics.density
        val cartao = LinearLayout(activity)
        cartao.orientation = LinearLayout.HORIZONTAL
        cartao.gravity = Gravity.CENTER_VERTICAL
        cartao.setPadding(0, (8 * densidade).toInt(), 0, (8 * densidade).toInt())
        val toque = android.util.TypedValue()
        activity.theme.resolveAttribute(android.R.attr.selectableItemBackground, toque, true)
        cartao.setBackgroundResource(toque.resourceId)
        cartao.contentDescription = item.nome
        cartao.setOnClickListener { aoAbrirPagina(item) }
        cartao.setOnLongClickListener {
            apagarPaginaDaBiblioteca(item)
            true
        }
        val textos = LinearLayout(activity)
        textos.orientation = LinearLayout.VERTICAL
        textos.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginEnd = (8 * densidade).toInt()
        }
        val titulo = TextView(activity)
        titulo.text = item.nome
        titulo.setTextColor(Color.parseColor("#F4F7FB"))
        titulo.textSize = 16f
        titulo.maxLines = 2
        titulo.ellipsize = android.text.TextUtils.TruncateAt.END
        textos.addView(titulo)
        if (item.descricao.isNotBlank()) {
            val descricao = TextView(activity)
            descricao.text = item.descricao
            descricao.setTextColor(Color.parseColor("#C5CAD3"))
            descricao.textSize = 13f
            descricao.maxLines = 2
            descricao.ellipsize = android.text.TextUtils.TruncateAt.END
            descricao.setPadding(0, (2 * densidade).toInt(), 0, 0)
            textos.addView(descricao)
        }
        if (item.tags.isNotEmpty()) {
            val tags = TextView(activity)
            tags.text = item.tags.joinToString(" · ")
            tags.setTextColor(Color.parseColor("#8AB4F8"))
            tags.textSize = 12f
            tags.maxLines = 1
            tags.ellipsize = android.text.TextUtils.TruncateAt.END
            tags.setPadding(0, (2 * densidade).toInt(), 0, 0)
            textos.addView(tags)
        }
        val data = TextView(activity)
        data.text = formatoDaData.format(Date(item.atualizadoEm))
        data.setTextColor(Color.parseColor("#C5CAD3"))
        data.textSize = 13f
        data.setPadding(0, (4 * densidade).toInt(), 0, 0)
        textos.addView(data)
        cartao.addView(textos)
        val lado = (40 * densidade).toInt()
        cartao.addView(
            botaoDeAcao(
                R.drawable.icone_de_remover_video_do_historico,
                R.string.apagar_pagina_da_biblioteca,
                lado,
            ) { apagarPaginaDaBiblioteca(item) },
        )
        return cartao
    }

    private fun apagarPaginaDaBiblioteca(item: ItemDaBibliotecaDeComponentesHtmlDaColecao) {
        val paginas = bibliotecaDePaginas ?: return
        val gravacaoDasPaginas = gravacaoDePaginas ?: return
        if (!paginas.apagar(item.id)) return
        gravacaoDasPaginas.apagarArquivos(item.id)
        gravacaoDasPaginas.gravar(paginas)
        if (idDaPaginaVinculadaNaColecao() == item.id) aoLimparVinculoDaPaginaNaColecao()
        atualizar()
    }

    private fun ligarObservacaoDosDownloads() {
        if (observacaoDosDownloads != null) return
        observacaoDosDownloads = acompanhamento.observar {
            activity.runOnUiThread {
                if (estaAberto() && secao == SecaoDaGaveta.BAIXADOS && !fechando) desenharLista()
            }
        }
    }

    private fun soltarObservacaoDosDownloads() {
        observacaoDosDownloads?.invoke()
        observacaoDosDownloads = null
    }

    private enum class SecaoDaGaveta { VIDEOS, IMAGENS, BAIXADOS, PAGINAS }

    private fun marcarAba(botao: Button, selecionado: Boolean, principal: Boolean) {
        val densidade = activity.resources.displayMetrics.density
        botao.isAllCaps = false
        botao.textSize = if (principal) 18f else 14f
        botao.minWidth = 0
        botao.minimumWidth = 0
        botao.backgroundTintList = null
        botao.stateListAnimator = null
        botao.setTextColor(if (selecionado) Color.parseColor("#F4F7FB") else Color.parseColor("#8A9099"))
        val horizontal = (4 * densidade).toInt()
        val vertical = ((if (principal) 10 else 6) * densidade).toInt()
        botao.setPadding(horizontal, vertical, horizontal, vertical)
        botao.background = if (selecionado) {
            LayerDrawable(arrayOf(GradientDrawable().apply { setColor(Color.parseColor("#8AB4F8")) })).apply {
                setLayerGravity(0, Gravity.BOTTOM)
                setLayerHeight(0, (3 * densidade).toInt().coerceAtLeast(2))
            }
        } else {
            ColorDrawable(Color.TRANSPARENT)
        }
    }

    private fun cartao(item: ItemDaBibliotecaDeVideos): View {
        val densidade = activity.resources.displayMetrics.density
        val cartao = LinearLayout(activity)
        cartao.orientation = LinearLayout.HORIZONTAL
        cartao.gravity = Gravity.CENTER_VERTICAL
        cartao.setPadding(0, (8 * densidade).toInt(), 0, (8 * densidade).toInt())
        cartao.setOnClickListener { aoAbrir(item) }
        cartao.setOnLongClickListener {
            iniciarArrasteDoVideoDaBiblioteca(cartao, item)
            true
        }

        val poster = FrameLayout(activity)
        val fundo = GradientDrawable()
        fundo.setColor(Color.parseColor("#22262E"))
        fundo.cornerRadius = 8f * densidade
        poster.background = fundo
        poster.outlineProvider = ViewOutlineProvider.BACKGROUND
        poster.clipToOutline = true
        poster.layoutParams = LinearLayout.LayoutParams(
            (148 * densidade).toInt(),
            (84 * densidade).toInt(),
        )

        val imagem = ImageView(activity)
        imagem.scaleType = ImageView.ScaleType.CENTER_CROP
        imagem.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        val quadro = gravacao.arquivoDaMiniatura(item.nomeDaMiniatura)?.let { arquivo ->
            BitmapFactory.decodeFile(
                arquivo.absolutePath,
                BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 },
            )
        }
        if (quadro != null) {
            quadros += quadro
            imagem.setImageBitmap(quadro)
        }
        poster.addView(imagem)

        val letra = TextView(activity)
        letra.gravity = Gravity.CENTER
        letra.setTextColor(Color.parseColor("#F4F7FB"))
        letra.textSize = 28f
        letra.text = item.titulo.trim().ifBlank { "?" }.take(1).uppercase()
        letra.visibility = if (quadro == null) View.VISIBLE else View.GONE
        letra.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        poster.addView(letra)
        cartao.addView(poster)

        val titulo = TextView(activity)
        titulo.text = item.titulo
        titulo.setTextColor(Color.parseColor("#F4F7FB"))
        titulo.textSize = 16f
        titulo.maxLines = 2
        titulo.ellipsize = android.text.TextUtils.TruncateAt.END
        titulo.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f,
        ).apply {
            marginStart = (12 * densidade).toInt()
            marginEnd = (8 * densidade).toInt()
        }
        cartao.addView(titulo)

        val estrela = ImageButton(activity)
        estrela.setImageResource(
            if (item.favorito) {
                R.drawable.icone_de_video_favorito_preenchido
            } else {
                R.drawable.icone_de_video_favorito_contorno
            },
        )
        estrela.contentDescription = activity.getString(
            if (item.favorito) R.string.remover_video_dos_favoritos else R.string.favoritar_video,
        )
        estrela.scaleType = ImageView.ScaleType.CENTER_INSIDE
        estrela.setPadding((8 * densidade).toInt(), (8 * densidade).toInt(), (8 * densidade).toInt(), (8 * densidade).toInt())
        estrela.background = null
        val lado = (40 * densidade).toInt()
        estrela.layoutParams = LinearLayout.LayoutParams(lado, lado)
        estrela.setOnClickListener {
            val atualizado = biblioteca.definirFavorito(item.chave, !item.favorito)
            gravacao.gravar(biblioteca)
            aoFavoritoAlterado(atualizado ?: item.copy(favorito = false))
            atualizar()
        }
        cartao.addView(botaoDeInformacao(lado) { mostrarModalDoDetalheDoVideo(item) })
        cartao.addView(estrela)

        if (!mostrandoFavoritos) {
            val remover = ImageButton(activity)
            remover.setImageResource(R.drawable.icone_de_remover_video_do_historico)
            remover.contentDescription = activity.getString(R.string.remover_video_do_historico)
            remover.scaleType = ImageView.ScaleType.CENTER_INSIDE
            remover.setPadding((8 * densidade).toInt(), (8 * densidade).toInt(), (8 * densidade).toInt(), (8 * densidade).toInt())
            remover.background = null
            remover.layoutParams = LinearLayout.LayoutParams(lado, lado)
            remover.setOnClickListener { removerDoHistorico(item) }
            cartao.addView(remover)
        }
        return cartao
    }

    private fun cartaoDeDownload(item: DownloadDeMidiaNaGaveta): View {
        val densidade = activity.resources.displayMetrics.density
        val cartao = LinearLayout(activity)
        cartao.orientation = LinearLayout.HORIZONTAL
        cartao.gravity = Gravity.CENTER_VERTICAL
        cartao.setPadding(0, (8 * densidade).toInt(), 0, (8 * densidade).toInt())
        if (item.estado == EstadoDoDownloadDeMidiaNaGaveta.PRONTO && item.uri.isNotBlank()) {
            cartao.setOnClickListener { aoAbrirDownload(item) }
        }

        val poster = FrameLayout(activity)
        val fundo = GradientDrawable()
        fundo.setColor(Color.parseColor("#22262E"))
        fundo.cornerRadius = 8f * densidade
        poster.background = fundo
        poster.outlineProvider = ViewOutlineProvider.BACKGROUND
        poster.clipToOutline = true
        poster.layoutParams = LinearLayout.LayoutParams((148 * densidade).toInt(), (84 * densidade).toInt())
        val imagem = ImageView(activity)
        imagem.scaleType = ImageView.ScaleType.CENTER_CROP
        imagem.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        val letra = TextView(activity)
        letra.gravity = Gravity.CENTER
        letra.setTextColor(Color.parseColor("#F4F7FB"))
        letra.textSize = 28f
        letra.text = item.titulo.trim().ifBlank { "?" }.take(1).uppercase()
        letra.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        poster.addView(imagem)
        poster.addView(letra)
        if (item.estado == EstadoDoDownloadDeMidiaNaGaveta.PRONTO && item.uri.isNotBlank()) {
            mostrarMiniaturaDoDownload(item.uri, imagem, letra)
        }
        cartao.addView(poster)

        val textos = LinearLayout(activity)
        textos.orientation = LinearLayout.VERTICAL
        val pronto = item.estado == EstadoDoDownloadDeMidiaNaGaveta.PRONTO && item.uri.isNotBlank()
        textos.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = (12 * densidade).toInt()
            if (pronto) marginEnd = (8 * densidade).toInt()
        }
        val titulo = TextView(activity)
        titulo.text = item.titulo
        titulo.setTextColor(Color.parseColor("#F4F7FB"))
        titulo.textSize = 16f
        titulo.maxLines = 2
        titulo.ellipsize = android.text.TextUtils.TruncateAt.END
        textos.addView(titulo)
        val detalhe = TextView(activity)
        detalhe.text = TextoDoProgressoDoDownloadDeMidiaNaGaveta.texto(item)
        detalhe.setTextColor(
            if (item.estado == EstadoDoDownloadDeMidiaNaGaveta.FALHOU) {
                Color.parseColor("#F2B8B5")
            } else {
                Color.parseColor("#C5CAD3")
            },
        )
        detalhe.textSize = 14f
        detalhe.setPadding(0, (4 * densidade).toInt(), 0, 0)
        textos.addView(detalhe)
        if (item.estado == EstadoDoDownloadDeMidiaNaGaveta.EM_ANDAMENTO) {
            val barra = ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal)
            barra.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = (8 * densidade).toInt() }
            if (item.total <= 0) {
                barra.isIndeterminate = true
            } else {
                barra.isIndeterminate = false
                barra.max = 100
                barra.progress = ((item.baixado * 100) / item.total).toInt().coerceIn(0, 100)
            }
            textos.addView(barra)
        }
        cartao.addView(textos)
        if (pronto) {
            val lado = (40 * densidade).toInt()
            cartao.addView(botaoDeAcao(R.drawable.icone_de_renomear_imagem_da_galeria, R.string.renomear_video_baixado, lado) {
                pedirNovoNomeDoDownload(item)
            })
            cartao.addView(botaoDeAcao(R.drawable.icone_de_remover_video_do_historico, R.string.excluir_video_baixado, lado) {
                confirmarExclusaoDoDownload(item)
            })
        }
        return cartao
    }

    private fun mostrarMiniaturaDoDownload(uri: String, imagem: ImageView, letra: TextView) {
        val pronta = miniaturasDosDownloads[uri]
        if (pronta != null && !pronta.isRecycled) {
            imagem.setImageBitmap(pronta)
            letra.visibility = View.GONE
            return
        }
        if (uri in miniaturasEmCarga) return
        miniaturasEmCarga += uri
        val geracao = geracaoDasMiniaturas
        Thread {
            val quadro = try {
                activity.contentResolver.loadThumbnail(Uri.parse(uri), android.util.Size(296, 168), null)
            } catch (_: Exception) {
                null
            }
            activity.runOnUiThread {
                miniaturasEmCarga.remove(uri)
                if (geracao != geracaoDasMiniaturas || quadro == null) {
                    if (quadro != null && !quadro.isRecycled) quadro.recycle()
                    return@runOnUiThread
                }
                val existente = miniaturasDosDownloads[uri]
                if (existente != null && !existente.isRecycled) {
                    quadro.recycle()
                    if (imagem.parent != null) {
                        imagem.setImageBitmap(existente)
                        letra.visibility = View.GONE
                    }
                    return@runOnUiThread
                }
                miniaturasDosDownloads[uri] = quadro
                if (imagem.parent != null) {
                    imagem.setImageBitmap(quadro)
                    letra.visibility = View.GONE
                } else if (estaAberto() && secao == SecaoDaGaveta.BAIXADOS && !fechando) {
                    desenharLista()
                }
            }
        }.start()
    }

    private fun soltarMiniaturasDosDownloads() {
        geracaoDasMiniaturas += 1
        miniaturasEmCarga.clear()
        miniaturasDosDownloads.values.forEach { quadro ->
            if (!quadro.isRecycled) quadro.recycle()
        }
        miniaturasDosDownloads.clear()
    }

    private fun mostrarMiniaturaDaImagem(
        item: ItemDaBibliotecaDeImagensCapturadas,
        imagem: ImageView,
        lado: Int,
    ) {
        val pronta = miniaturasDasImagens[item.id]
        if (pronta != null && !pronta.isRecycled) {
            imagem.setImageBitmap(pronta)
            return
        }
        if (item.id in imagensEmCarga) return
        imagensEmCarga += item.id
        val id = item.id
        val caminho = gravacaoDeImagens.arquivo(item.nomeDoArquivo)?.absolutePath
        val geracao = geracaoDasMiniaturasDeImagem
        Thread {
            val quadro = caminho?.let { decodificarMiniatura(it, lado) }
            activity.runOnUiThread {
                imagensEmCarga.remove(id)
                if (geracao != geracaoDasMiniaturasDeImagem || quadro == null) {
                    if (quadro != null && !quadro.isRecycled) quadro.recycle()
                    return@runOnUiThread
                }
                val existente = miniaturasDasImagens[id]
                if (existente != null && !existente.isRecycled) {
                    if (quadro !== existente && !quadro.isRecycled) quadro.recycle()
                    if (imagem.parent != null) imagem.setImageBitmap(existente)
                    return@runOnUiThread
                }
                miniaturasDasImagens[id] = quadro
                if (imagem.parent != null) {
                    imagem.setImageBitmap(quadro)
                } else if (estaAberto() && secao == SecaoDaGaveta.IMAGENS && !fechando) {
                    desenharLista()
                }
            }
        }.start()
    }

    private fun decodificarMiniatura(caminho: String, lado: Int): Bitmap? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(caminho, limites)
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null
        val maior = max(limites.outWidth, limites.outHeight)
        var amostra = 1
        val alvo = lado.coerceAtLeast(1) * 2
        while (amostra < 4096 && amostra < maior && maior / amostra > alvo) amostra *= 2
        return BitmapFactory.decodeFile(
            caminho,
            BitmapFactory.Options().apply {
                inSampleSize = amostra
                inPreferredConfig = Bitmap.Config.RGB_565
            },
        )
    }

    private fun soltarMiniaturasDasImagens() {
        geracaoDasMiniaturasDeImagem += 1
        imagensEmCarga.clear()
        miniaturasDasImagens.values.forEach { if (!it.isRecycled) it.recycle() }
        miniaturasDasImagens.clear()
    }

    private fun pedirNovoNomeDoDownload(item: DownloadDeMidiaNaGaveta) {
        val campo = EditText(activity)
        campo.setSingleLine(true)
        campo.setText(item.titulo)
        campo.setSelection(campo.text.length)
        AlertDialog.Builder(activity)
            .setTitle(R.string.titulo_renomear_video_baixado)
            .setView(campo)
            .setPositiveButton(R.string.renomear_video_baixado) { _, _ ->
                try {
                    val nomeAtual = item.nomeDoArquivo.ifBlank { "${item.titulo}.mp4" }
                    AlteracaoDoArquivoDeMidiaBaixadoNaPastaDoChromeBrothers.renomear(
                        activity.contentResolver,
                        item.uri,
                        nomeAtual,
                        campo.text.toString(),
                    )
                    Toast.makeText(activity, R.string.video_baixado_renomeado, Toast.LENGTH_LONG).show()
                    if (estaAberto() && secao == SecaoDaGaveta.BAIXADOS) desenharLista()
                } catch (_: Exception) {
                    Toast.makeText(activity, R.string.nome_de_video_baixado_invalido, Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun confirmarExclusaoDoDownload(item: DownloadDeMidiaNaGaveta) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.excluir_video_baixado)
            .setMessage(R.string.confirmar_exclusao_do_video_baixado)
            .setPositiveButton(R.string.excluir_video_baixado) { _, _ ->
                val apagou = try {
                    AlteracaoDoArquivoDeMidiaBaixadoNaPastaDoChromeBrothers.excluir(
                        activity.contentResolver,
                        item.uri,
                    )
                } catch (_: Exception) {
                    false
                }
                if (!apagou) {
                    Toast.makeText(activity, R.string.video_baixado_nao_excluido, Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                miniaturasDosDownloads.remove(item.uri)?.let { quadro ->
                    if (!quadro.isRecycled) quadro.recycle()
                }
                acompanhamento.esquecer(item.uri)
                Toast.makeText(activity, R.string.video_baixado_excluido, Toast.LENGTH_LONG).show()
                if (estaAberto() && secao == SecaoDaGaveta.BAIXADOS) desenharLista()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun removerDoHistorico(item: ItemDaBibliotecaDeVideos) {
        val resultado = biblioteca.removerDoHistorico(item.chave) ?: return
        if (resultado.apagouOItem) gravacao.apagarMiniatura(resultado.nomeDaMiniatura)
        gravacao.gravar(biblioteca)
        val restante = biblioteca.buscar(item.chave)
        aoFavoritoAlterado(restante ?: item.copy(favorito = false))
        atualizar()
    }

    private fun esvaziarLista() {
        for (indice in 0 until lista.childCount) {
            val cartao = lista.getChildAt(indice) as? LinearLayout ?: continue
            val poster = cartao.getChildAt(0) as? FrameLayout ?: continue
            val imagem = poster.getChildAt(0) as? ImageView
            imagem?.setImageDrawable(null)
        }
        lista.removeAllViews()
        quadros.forEach { quadro ->
            if (!quadro.isRecycled) quadro.recycle()
        }
        quadros.clear()
    }

    private fun cartaoDeImagem(item: ItemDaBibliotecaDeImagensCapturadas): View {
        val densidade = activity.resources.displayMetrics.density
        val cartao = LinearLayout(activity)
        cartao.orientation = LinearLayout.HORIZONTAL
        cartao.gravity = Gravity.CENTER_VERTICAL
        cartao.setPadding(0, (8 * densidade).toInt(), 0, (8 * densidade).toInt())
        cartao.setOnClickListener {
            when (DestinoDoToqueNaImagemDaGaveta.decidir(editandoImagem, gavetaSobreAImagem)) {
                ToqueNaImagemDaGaveta.INCLUIR_NA_COMPOSICAO -> incluirImagemNaComposicao(item)
                ToqueNaImagemDaGaveta.TROCAR_A_IMAGEM_DO_VISOR -> {
                    abrirVisor(item)
                    recolherGavetaSobreAImagem()
                }
                ToqueNaImagemDaGaveta.ABRIR_NO_VISOR -> abrirVisor(item)
            }
        }
        cartao.setOnLongClickListener {
            iniciarArrasteDaImagemDaBiblioteca(cartao, item)
            true
        }

        val poster = FrameLayout(activity)
        val fundoDoPoster = GradientDrawable()
        fundoDoPoster.setColor(Color.parseColor("#22262E"))
        fundoDoPoster.cornerRadius = 8f * densidade
        poster.background = fundoDoPoster
        poster.outlineProvider = ViewOutlineProvider.BACKGROUND
        poster.clipToOutline = true
        poster.layoutParams = LinearLayout.LayoutParams(
            (168 * densidade).toInt(),
            (112 * densidade).toInt(),
        )
        val imagem = ImageView(activity)
        imagem.scaleType = ImageView.ScaleType.CENTER_CROP
        imagem.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        poster.addView(imagem)
        mostrarMiniaturaDaImagem(item, imagem, (168 * densidade).toInt().coerceAtLeast(1))
        cartao.addView(poster)

        val textos = LinearLayout(activity)
        textos.orientation = LinearLayout.VERTICAL
        textos.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = (12 * densidade).toInt()
            marginEnd = (8 * densidade).toInt()
        }
        val quando = TextView(activity)
        val nomeVisivel = item.nomeExibido?.substringBeforeLast('.')?.takeIf { it.isNotBlank() }
        quando.text = nomeVisivel ?: formatoDaData.format(Date(item.instante))
        quando.setTextColor(Color.parseColor("#F4F7FB"))
        quando.textSize = 15f
        quando.maxLines = 2
        val estado = TextView(activity)
        estado.setText(if (item.naGaleria) R.string.imagem_na_galeria else R.string.imagem_somente_copiada)
        estado.setTextColor(Color.parseColor(if (item.naGaleria) "#8AB4F8" else "#C5CAD3"))
        estado.textSize = 13f
        textos.addView(quando)
        textos.addView(estado)
        cartao.addView(textos)

        val lado = (40 * densidade).toInt()
        cartao.addView(botaoDeInformacao(lado) { mostrarModalDoDetalheDaImagem(item) })
        cartao.addView(botaoDeImagem(item.favorito, lado) {
            val atualizado = bibliotecaDeImagens.definirFavorito(item.id, !item.favorito)
            gravacaoDeImagens.gravar(bibliotecaDeImagens)
            if (imagemAbertaId == item.id) {
                val aberto = atualizado ?: item.copy(favorito = false)
                if (bibliotecaDeImagens.buscar(item.id) == null) fecharVisor() else atualizarBotoesDoVisor(aberto)
            }
            atualizar()
        })
        cartao.addView(botaoDeAcao(R.drawable.icone_de_copiar_quadro_do_player_de_video, R.string.copiar_esta_imagem, lado) {
            copiar(item)
        })
        if (!item.naGaleria) {
            val botaoBaixar = ImageButton(activity)
            botaoBaixar.setImageResource(R.drawable.icone_de_salvar_quadro_do_player_de_video)
            botaoBaixar.contentDescription = activity.getString(R.string.baixar_esta_imagem)
            botaoBaixar.scaleType = ImageView.ScaleType.CENTER_INSIDE
            botaoBaixar.setPadding((8 * densidade).toInt(), (8 * densidade).toInt(), (8 * densidade).toInt(), (8 * densidade).toInt())
            botaoBaixar.background = null
            botaoBaixar.layoutParams = LinearLayout.LayoutParams(lado, lado)
            botaoBaixar.setOnClickListener { baixar(item) }
            cartao.addView(botaoBaixar)
        }
        if (!item.uriDaGaleria.isNullOrBlank()) {
            cartao.addView(botaoDeAcao(R.drawable.icone_de_renomear_imagem_da_galeria, R.string.renomear_imagem_da_galeria, lado) {
                pedirNovoNome(item)
            })
        }
        if (!mostrandoFavoritos) {
            val remover = ImageButton(activity)
            remover.setImageResource(R.drawable.icone_de_remover_video_do_historico)
            remover.contentDescription = activity.getString(R.string.remover_imagem_do_historico)
            remover.scaleType = ImageView.ScaleType.CENTER_INSIDE
            remover.setPadding((8 * densidade).toInt(), (8 * densidade).toInt(), (8 * densidade).toInt(), (8 * densidade).toInt())
            remover.background = null
            remover.layoutParams = LinearLayout.LayoutParams(lado, lado)
            remover.setOnClickListener { removerImagemDoHistorico(item) }
            cartao.addView(remover)
        }
        return cartao
    }

    private fun botaoDeImagem(favorito: Boolean, lado: Int, aoToque: () -> Unit): ImageButton {
        val densidade = activity.resources.displayMetrics.density
        val estrela = ImageButton(activity)
        estrela.setImageResource(
            if (favorito) R.drawable.icone_de_video_favorito_preenchido else R.drawable.icone_de_video_favorito_contorno,
        )
        estrela.contentDescription = activity.getString(
            if (favorito) R.string.remover_imagem_dos_favoritos else R.string.favoritar_imagem,
        )
        estrela.scaleType = ImageView.ScaleType.CENTER_INSIDE
        val margem = (8 * densidade).toInt()
        estrela.setPadding(margem, margem, margem, margem)
        estrela.background = null
        estrela.layoutParams = LinearLayout.LayoutParams(lado, lado)
        estrela.setOnClickListener { aoToque() }
        return estrela
    }

    private fun botaoDeAcao(icone: Int, descricao: Int, lado: Int, aoToque: () -> Unit): ImageButton {
        val densidade = activity.resources.displayMetrics.density
        val botao = ImageButton(activity)
        botao.setImageResource(icone)
        botao.contentDescription = activity.getString(descricao)
        botao.scaleType = ImageView.ScaleType.CENTER_INSIDE
        val margem = (8 * densidade).toInt()
        botao.setPadding(margem, margem, margem, margem)
        botao.background = null
        botao.layoutParams = LinearLayout.LayoutParams(lado, lado)
        botao.setOnClickListener { aoToque() }
        return botao
    }

    fun abrirImagemDaBiblioteca(id: String): String {
        val item = bibliotecaDeImagens.buscar(id) ?: return "Não achei essa imagem na biblioteca."
        val arquivo = gravacaoDeImagens.arquivo(item.nomeDoArquivo)
        if (arquivo == null || !arquivo.exists()) return "Não deu para abrir essa imagem."
        if (painel.visibility != View.VISIBLE) {
            fechando = false
            fundo.alpha = 1f
            painel.visibility = View.VISIBLE
        }
        painel.bringToFront()
        abrirVisor(item)
        visor.bringToFront()
        val nome = item.nomeExibido?.trim().orEmpty().ifBlank { item.nomeDoArquivo }
        return if (imagemAbertaId == item.id && visor.visibility == View.VISIBLE) {
            "Abri a imagem $nome no visor."
        } else {
            "Não deu para abrir essa imagem."
        }
    }

    private fun abrirVisor(item: ItemDaBibliotecaDeImagensCapturadas) {
        idRetomavelNaFoto = null
        val arquivo = gravacaoDeImagens.arquivo(item.nomeDoArquivo) ?: return
        val quadro = BitmapFactory.decodeFile(arquivo.absolutePath) ?: return
        quadroAmpliado?.let { if (!it.isRecycled) it.recycle() }
        quadroAmpliado = quadro
        imagemAbertaId = item.id
        editor.visibility = View.GONE
        imagemAmpliada.visibility = View.VISIBLE
        imagemAmpliada.setImageBitmap(quadro)
        atualizarBotoesDoVisor(item)
        visor.visibility = View.VISIBLE
    }

    private fun abrirEdicao() {
        val origem = quadroAmpliado ?: return
        val retomar = idRetomavelNaFoto
        idRetomavelNaFoto = null
        if (retomar != null) {
            continuarEdicao(retomar)
            return
        }
        idDoRascunhoAberto = null
        editor.definirImagem(origem)
        escolherFerramenta(EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO)
        imagemAmpliada.visibility = View.GONE
        editor.visibility = View.VISIBLE
        editandoImagem = true
        mostrarChromeDeEdicao()
        visor.bringToFront()
        mostrarEspessura()
        mostrarCores()
        marcarMoldura()
        atualizarLinhaSecundaria()
    }

    private fun abrirComposicaoVazia() {
        if (editandoImagem) estacionarEdicaoAtual()
        idDoRascunhoAberto = null
        idRetomavelNaFoto = null
        quadroAmpliado?.let { if (!it.isRecycled) it.recycle() }
        quadroAmpliado = null
        imagemAbertaId = null
        imagemAmpliada.setImageDrawable(null)
        editor.definirMolduraVazia()
        if (AbrirComposicaoVaziaDaEdicao.criaCamadaInicialSelecionada()) {
            editor.criarCamadaVazia()
        }
        escolherFerramenta(EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO)
        imagemAmpliada.visibility = View.GONE
        editor.visibility = View.VISIBLE
        visor.visibility = View.VISIBLE
        editandoImagem = true
        mostrarChromeDeEdicao()
        visor.bringToFront()
        mostrarEspessura()
        mostrarCores()
        marcarMoldura()
        atualizarLinhaSecundaria()
        atualizarListaDeCamadas()
        if (gavetaSobreAImagem) recolherGavetaSobreAImagem()
        atualizar()
    }

    private fun estacionarEdicaoAtual() {
        if (!editandoImagem) return
        prepararGravacaoAoSair()
        abandonarEdicaoNaMemoria()
        idDoRascunhoAberto = null
        idRetomavelNaFoto = null
    }

    private fun continuarEdicao(id: String) {
        if (editandoImagem && id == idDoRascunhoAberto) {
            if (gavetaSobreAImagem) recolherGavetaSobreAImagem()
            return
        }
        if (editandoImagem) estacionarEdicaoAtual()
        idRetomavelNaFoto = null
        val carregado = gravacaoDoRascunho.carregar(id)
        if (carregado == null) {
            gravacaoDoRascunho.apagar(id)
            chaveDaListaDeRascunhos = null
            atualizar()
            Toast.makeText(activity, R.string.rascunho_da_edicao_indisponivel, Toast.LENGTH_LONG).show()
            return
        }
        quadroAmpliado?.let { if (!it.isRecycled) it.recycle() }
        quadroAmpliado = null
        imagemAbertaId = null
        imagemAmpliada.setImageDrawable(null)
        idDoRascunhoAberto = id
        editandoImagem = true
        mostrarChromeDeEdicao()
        editor.restaurar(carregado.documento, carregado.bitmaps)
        ferramentaDaEdicao = editor.ferramentaAtual()
        contextoDaBarra = contextoDaFerramenta(ferramentaDaEdicao)
        marcarFerramenta(ferramentaDaEdicao)
        atualizarLinhaSecundaria()
        imagemAmpliada.visibility = View.GONE
        editor.visibility = View.VISIBLE
        visor.visibility = View.VISIBLE
        visor.bringToFront()
        if (gavetaSobreAImagem) recolherGavetaSobreAImagem()
    }

    private fun abandonarEdicaoNaMemoria() {
        editandoImagem = false
        ocultarChipDoZoomDaVistaAgora()
        limparListaDeCamadas()
        editor.soltar()
        dispararGravacaoDoRascunho()
        editor.visibility = View.GONE
        imagemAmpliada.visibility = View.VISIBLE
        mostrarChromeDeVisita()
        // Único ponto de saída da edição: cancelarEdicao, esconder (se editando) e tratarVolta passam por aqui.
        aoEncerrarEdicaoDeImagem?.invoke()
    }

    private fun mostrarChromeDeEdicao() {
        grupoDeAcoesDaImagem.visibility = View.GONE
        // Estúdio: barra lateral antiga, fileiras e barra de baixo saem; a imagem ganha a tela toda.
        grupoDeFerramentas.visibility = View.GONE
        activity.findViewById<View>(R.id.botao_resolucao_da_moldura_da_composicao).visibility = View.VISIBLE
        botaoConcluirEdicao.visibility = View.VISIBLE
        botaoComponentesNoChromeDoEditor.visibility = View.VISIBLE
        barraDeAcoesDaImagem.visibility = View.GONE
        barraDeEdicaoDaImagem.visibility = View.GONE
        listaDeCamadas.visibility = if (camadasVisiveisNoEstudio()) View.VISIBLE else View.GONE
        chromeEstudio().mostrar()
        botaoFecharImagem.contentDescription = activity.getString(R.string.cancelar_edicao_da_imagem)
        atualizarLinhaSecundaria()
        marcarMoldura()
        atualizarListaDeCamadas()
    }

    private fun mostrarChromeDeVisita() {
        chromeEstudio?.esconder()
        grupoDeAcoesDaImagem.visibility = View.VISIBLE
        grupoDeFerramentas.visibility = View.GONE
        activity.findViewById<View>(R.id.botao_resolucao_da_moldura_da_composicao).visibility = View.GONE
        listaDeCamadas.visibility = View.GONE
        limparListaDeCamadas()
        botaoConcluirEdicao.visibility = View.GONE
        botaoComponentesNoChromeDoEditor.visibility = View.GONE
        barraDeAcoesDaImagem.visibility = View.VISIBLE
        barraDeEdicaoDaImagem.visibility = View.GONE
        botaoFecharImagem.contentDescription = activity.getString(R.string.fechar_imagem_capturada)
    }

    private fun atualizarListaDeCamadas() {
        if (arrastandoCamada || montagemDeCamadasAgendada) return
        montagemDeCamadasAgendada = true
        linhasDasCamadas.post {
            montagemDeCamadasAgendada = false
            if (arrastandoCamada || !editandoImagem) return@post
            montarListaDeCamadas()
        }
    }

    private fun montarListaDeCamadas() {
        linhasDasCamadas.removeAllViews()
        miniaturasDeDesenho.forEach { if (!it.isRecycled) it.recycle() }
        miniaturasDeDesenho.clear()
        val densidade = activity.resources.displayMetrics.density
        val selecionada = editor.idDaCamadaAtiva()
        val camadas = editor.camadasDaFrenteParaOFundo()
        if (camadas.none { it.id == idComOpacidadeAberta }) idComOpacidadeAberta = -1
        camadas.forEachIndexed { indice, camada ->
            val linha = activity.layoutInflater.inflate(
                R.layout.layout_linha_da_camada_da_edicao_de_imagem,
                linhasDasCamadas,
                false,
            )
            val miniatura = linha.findViewById<ImageView>(R.id.miniatura_da_camada_da_edicao)
            val rotulo = linha.findViewById<TextView>(R.id.rotulo_da_camada_da_edicao)
            val detalhe = linha.findViewById<TextView>(R.id.detalhe_da_camada_da_edicao)
            // Miniatura da camada como aparece na folha (foto + traços), no formato da moldura.
            val lado = (44 * densidade).toInt().coerceAtLeast(1)
            val reduzida = if (camada.texto != null) null else editor.miniaturaDaCamada(camada.id, lado)
            if (reduzida != null) miniaturasDeDesenho += reduzida
            miniatura.setImageBitmap(reduzida)
            val numero = camadas.size - indice
            rotulo.text = camada.texto?.takeIf { it.isNotBlank() } ?: activity.getString(R.string.estudio_nome_da_camada, numero)
            // Camada é camada: só o texto ganha rótulo de tipo.
            val partes = mutableListOf<String>()
            if (camada.texto != null) partes += activity.getString(R.string.estudio_tipo_texto)
            if (camada.opacidade < 0.995f) partes += "${Math.round(camada.opacidade * 100)}%"
            if (!camada.visivel) partes += activity.getString(R.string.estudio_camada_oculta)
            if (editor.transparenciaBloqueada(camada.id)) partes += activity.getString(R.string.estudio_transparencia_bloqueada)
            detalhe.text = partes.joinToString(" · ")
            detalhe.visibility = if (partes.isEmpty()) View.GONE else View.VISIBLE
            linha.tag = camada.id
            linha.alpha = if (camada.visivel) 1f else 0.55f
            linha.background = if (camada.id == selecionada) {
                GradientDrawable().apply {
                    cornerRadius = 12f * densidade
                    setColor(Color.parseColor("#263247"))
                    setStroke((1f * densidade).toInt().coerceAtLeast(1), Color.parseColor("#8AB4F8"))
                }
            } else {
                null
            }
            val botaoVisibilidade = linha.findViewById<ImageButton>(R.id.botao_visibilidade_da_camada_da_edicao)
            botaoVisibilidade.setImageResource(
                if (camada.visivel) {
                    R.drawable.icone_de_camada_visivel_da_edicao_de_imagem
                } else {
                    R.drawable.icone_de_camada_oculta_da_edicao_de_imagem
                },
            )
            botaoVisibilidade.contentDescription = activity.getString(
                if (camada.visivel) R.string.ocultar_camada_da_edicao else R.string.mostrar_camada_da_edicao,
            )
            botaoVisibilidade.setOnClickListener {
                editor.alternarVisibilidadeDaCamada(camada.id)
            }
            linha.findViewById<View>(R.id.botao_lixeira_da_camada_da_edicao).setOnClickListener {
                editor.definirCamadaAtiva(camada.id)
                pedirConfirmacaoParaExcluirCamada()
            }
            ligarArrasteDaCamada(linha, indice)
            linhasDasCamadas.addView(linha)
            if (camada.id == idComOpacidadeAberta) linhasDasCamadas.addView(linhaDaOpacidade(camada.id, camada.opacidade))
        }
    }

    /** Controle de opacidade embaixo da camada ativa (muda ao vivo; o desfazer entra ao soltar). */
    private fun linhaDaOpacidade(id: Int, opacidade: Float): View {
        val densidade = activity.resources.displayMetrics.density
        val rotulo = TextView(activity).apply {
            text = activity.getString(R.string.estudio_opacidade_da_camada, Math.round(opacidade * 100))
            setTextColor(Color.parseColor("#C4C7CE"))
            textSize = 13f
        }
        val barra = android.widget.SeekBar(activity).apply {
            max = 100
            progress = Math.round(opacidade * 100)
            contentDescription = activity.getString(R.string.estudio_opacidade)
            setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(barra: android.widget.SeekBar?, valor: Int, doUsuario: Boolean) {
                    if (!doUsuario) return
                    editor.ajustarOpacidadeDaCamada(id, valor / 100f)
                    rotulo.text = activity.getString(R.string.estudio_opacidade_da_camada, valor)
                }
                override fun onStartTrackingTouch(barra: android.widget.SeekBar?) {
                    barra?.parent?.requestDisallowInterceptTouchEvent(true)
                }
                override fun onStopTrackingTouch(barra: android.widget.SeekBar?) {
                    editor.concluirAjusteDeOpacidade()
                }
            })
        }
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((10 * densidade).toInt(), (4 * densidade).toInt(), (6 * densidade).toInt(), (8 * densidade).toInt())
            addView(rotulo)
            addView(barra, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (36 * densidade).toInt()))
        }
    }

    /** Miniatura da camada desenhada à mão (a tinta muda a cada traço: sempre refeita, nunca em cache). */
    private fun miniaturaDoDesenho(tinta: Bitmap): Bitmap? {
        if (tinta.isRecycled) return null
        val lado = (44 * activity.resources.displayMetrics.density).toInt().coerceAtLeast(1)
        val escala = lado.toFloat() / max(tinta.width, tinta.height)
        val copia = try {
            Bitmap.createScaledBitmap(
                tinta,
                (tinta.width * escala).toInt().coerceAtLeast(1),
                (tinta.height * escala).toInt().coerceAtLeast(1),
                true,
            )
        } catch (_: OutOfMemoryError) {
            null
        } ?: return null
        if (copia !== tinta) miniaturasDeDesenho += copia
        return copia
    }

    /** Arraste de imagem (componente da coleção) passando sobre a imagem em edição: abre o painel. */
    fun abrirCamadasDuranteOArraste() {
        if (!editandoImagem || listaDeCamadas.visibility == View.VISIBLE) return
        camadasAbertasPeloArraste = true
        listaDeCamadas.visibility = View.VISIBLE
        listaDeCamadas.bringToFront()
        atualizarListaDeCamadas()
        agendarSincronizacaoDoChrome()
    }

    /** Fim do arraste: fecha o painel se foi o arraste que abriu. */
    fun fecharCamadasAbertasPeloArraste() {
        if (!camadasAbertasPeloArraste) return
        camadasAbertasPeloArraste = false
        listaDeCamadas.post {
            if (!camadasVisiveisNoEstudio()) listaDeCamadas.visibility = View.GONE
            agendarSincronizacaoDoChrome()
        }
    }

    private fun miniaturaDaPeca(origem: Bitmap): Bitmap? {
        val chave = System.identityHashCode(origem)
        miniaturasDasCamadas[chave]?.takeIf { !it.isRecycled }?.let { return it }
        val lado = (44 * activity.resources.displayMetrics.density).toInt().coerceAtLeast(1)
        if (origem.width <= lado && origem.height <= lado) return origem
        val escala = lado.toFloat() / max(origem.width, origem.height)
        val largura = (origem.width * escala).toInt().coerceAtLeast(1)
        val altura = (origem.height * escala).toInt().coerceAtLeast(1)
        val copia = try {
            Bitmap.createScaledBitmap(origem, largura, altura, true)
        } catch (_: OutOfMemoryError) {
            null
        } ?: return null
        if (copia !== origem) miniaturasDasCamadas[chave] = copia
        return copia
    }

    private fun limparListaDeCamadas() {
        montagemDeCamadasAgendada = false
        arrastandoCamada = false
        for (indice in 0 until linhasDasCamadas.childCount) {
            linhasDasCamadas.getChildAt(indice)
                .findViewById<ImageView>(R.id.miniatura_da_camada_da_edicao)
                ?.setImageDrawable(null)
        }
        linhasDasCamadas.removeAllViews()
        val copias = miniaturasDasCamadas.values.toList()
        miniaturasDasCamadas.clear()
        copias.forEach { if (!it.isRecycled) it.recycle() }
        miniaturasDeDesenho.forEach { if (!it.isRecycled) it.recycle() }
        miniaturasDeDesenho.clear()
    }

    private fun ligarArrasteDaCamada(linha: View, indiceInicial: Int) {
        var indice = indiceInicial
        var inicioY = 0f
        var moveu = false
        var longoDisparou = false
        val folga = 8f * activity.resources.displayMetrics.density
        val disparoLongo = Runnable {
            if (moveu) return@Runnable
            longoDisparou = true
            arrastandoCamada = false
            editor.cancelarArrasteDaCamada()
            linha.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        linha.setOnTouchListener { vista, evento ->
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    inicioY = evento.rawY
                    moveu = false
                    longoDisparou = false
                    arrastandoCamada = true
                    editor.iniciarArrasteDaCamada()
                    vista.postDelayed(disparoLongo, ViewConfiguration.getLongPressTimeout().toLong())
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = evento.rawY - inicioY
                    if (!moveu && abs(dy) < folga) return@setOnTouchListener true
                    if (!moveu) vista.removeCallbacks(disparoLongo)
                    moveu = true
                    vista.translationY = dy
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    vista.removeCallbacks(disparoLongo)
                    vista.translationY = 0f
                    if (longoDisparou) {
                        editor.cancelarArrasteDaCamada()
                        if (evento.actionMasked == MotionEvent.ACTION_UP) mostrarMenuDaCamada(vista)
                    } else if (moveu && evento.actionMasked == MotionEvent.ACTION_UP) {
                        val altura = vista.height.coerceAtLeast(1)
                        val passos = ((evento.rawY - inicioY) / altura).toInt()
                        val destino = (indice + passos).coerceIn(0, linhasDasCamadas.childCount - 1)
                        editor.moverCamadaNaLista(indice, destino)
                        editor.concluirArrasteDaCamada()
                    } else if (!moveu && evento.actionMasked == MotionEvent.ACTION_UP) {
                        editor.cancelarArrasteDaCamada()
                        val id = vista.tag as Int
                        if (editor.idDaCamadaAtiva() == id) {
                            idComOpacidadeAberta = if (idComOpacidadeAberta == id) -1 else id
                        } else {
                            idComOpacidadeAberta = -1
                        }
                        editor.definirCamadaAtiva(id)
                    } else {
                        editor.cancelarArrasteDaCamada()
                    }
                    arrastandoCamada = false
                    if (!longoDisparou) atualizarListaDeCamadas()
                    true
                }
                else -> false
            }
        }
    }

    private fun mostrarMenuDaCamada(linha: View) {
        val id = linha.tag as? Int ?: return
        val menu = PopupMenu(activity, linha)
        if (editor.podeDuplicarCamada(id)) {
            menu.menu.add(activity.getString(R.string.duplicar_camada_da_edicao)).setOnMenuItemClickListener {
                editor.duplicarCamada(id)
                true
            }
        }
        if (editor.podeSelecionarCamadaInteira(id)) {
            menu.menu.add(activity.getString(R.string.selecionar_toda_camada_da_edicao)).setOnMenuItemClickListener {
                editor.selecionarCamadaInteira(id)
                true
            }
        }
        if (editor.aceitaBloqueioDeTransparencia(id)) {
            val titulo = if (editor.transparenciaBloqueada(id)) R.string.estudio_desbloquear_transparencia else R.string.estudio_bloquear_transparencia
            menu.menu.add(activity.getString(titulo)).setOnMenuItemClickListener {
                editor.alternarBloqueioDeTransparencia(id)
                true
            }
        }
        if (editor.aceitaEspelhoNaCamada(id)) {
            menu.menu.add(activity.getString(R.string.espelhar_camada_na_horizontal)).setOnMenuItemClickListener {
                editor.espelharCamada(id, horizontal = true)
                true
            }
            menu.menu.add(activity.getString(R.string.espelhar_camada_na_vertical)).setOnMenuItemClickListener {
                editor.espelharCamada(id, horizontal = false)
                true
            }
        }
        menu.menu.add(activity.getString(R.string.estudio_opacidade)).setOnMenuItemClickListener {
            editor.definirCamadaAtiva(id)
            idComOpacidadeAberta = id
            atualizarListaDeCamadas()
            true
        }
        if (editor.podeMesclarComADeBaixo(id)) {
            menu.menu.add(activity.getString(R.string.estudio_mesclar_com_a_de_baixo)).setOnMenuItemClickListener {
                if (!editor.mesclarComADeBaixo(id)) {
                    Toast.makeText(activity, R.string.quadro_indisponivel, Toast.LENGTH_LONG).show()
                }
                idComOpacidadeAberta = -1
                marcarMoldura()
                true
            }
        }
        menu.menu.add(activity.getString(R.string.estudio_excluir_camada)).setOnMenuItemClickListener {
            editor.definirCamadaAtiva(id)
            pedirConfirmacaoParaExcluirCamada()
            true
        }
        menu.show()
    }

    private fun mostrarGavetaSobreAImagem() {
        if (gavetaSobreAImagem) return
        gavetaSobreAImagem = true
        inserindoNaComposicao = editandoImagem
        geracaoDaGavetaSobreAImagem += 1
        val geracao = geracaoDaGavetaSobreAImagem
        secao = SecaoDaGaveta.IMAGENS
        mostrandoFavoritos = false
        atualizar()
        gaveta.animate().cancel()
        fundo.animate().cancel()
        fundo.alpha = 0f
        val deslizar = deslizar@{
            if (geracao != geracaoDaGavetaSobreAImagem || !gavetaSobreAImagem) return@deslizar
            val distancia = gaveta.width.toFloat()
            if (distancia <= 0f) return@deslizar
            gaveta.translationX = distancia
            fundo.bringToFront()
            gaveta.bringToFront()
            gaveta.animate()
                .translationX(0f)
                .setDuration(320)
                .setInterpolator(curvaDaGaveta)
                .start()
            fundo.animate()
                .alpha(1f)
                .setDuration(320)
                .setInterpolator(curvaDaGaveta)
                .start()
        }
        if (gaveta.width > 0) {
            deslizar()
        } else {
            val observador = gaveta.viewTreeObserver
            observador.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (observador.isAlive) observador.removeOnPreDrawListener(this)
                    deslizar()
                    return true
                }
            })
        }
    }

    private fun recolherGavetaSobreAImagem() {
        if (!gavetaSobreAImagem) return
        val geracao = geracaoDaGavetaSobreAImagem
        gaveta.animate().cancel()
        fundo.animate().cancel()
        val distancia = gaveta.width.toFloat().takeIf { it > 0f } ?: (painel.width * 0.5f)
        gaveta.animate()
            .translationX(distancia)
            .setDuration(240)
            .setInterpolator(curvaDaGaveta)
            .withEndAction {
                if (geracao != geracaoDaGavetaSobreAImagem) return@withEndAction
                gavetaSobreAImagem = false
                inserindoNaComposicao = false
                gaveta.translationX = 0f
                fundo.alpha = 1f
                if (visor.visibility == View.VISIBLE) visor.bringToFront()
            }
            .start()
        fundo.animate()
            .alpha(0f)
            .setDuration(240)
            .setInterpolator(curvaDaGaveta)
            .start()
    }

    private fun soltarSobreposicaoDaGaveta() {
        geracaoDaGavetaSobreAImagem += 1
        gavetaSobreAImagem = false
        inserindoNaComposicao = false
        gaveta.animate().cancel()
        fundo.animate().cancel()
        gaveta.translationX = 0f
        fundo.alpha = 1f
    }

    private fun fecharGavetaOuRecolher() {
        if (gavetaSobreAImagem) recolherGavetaSobreAImagem() else esconder()
    }

    private fun incluirImagemNaComposicao(item: ItemDaBibliotecaDeImagensCapturadas) {
        val arquivo = gravacaoDeImagens.arquivo(item.nomeDoArquivo) ?: return
        val quadro = BitmapFactory.decodeFile(arquivo.absolutePath) ?: return
        val entrou = editor.adicionarImagem(quadro)
        if (!quadro.isRecycled) quadro.recycle()
        if (!entrou) Toast.makeText(activity, R.string.limite_de_imagens_da_composicao, Toast.LENGTH_LONG).show()
        recolherGavetaSobreAImagem()
    }

    private fun pedirConfirmacaoParaExcluirCamada() {
        mostrarCaixaDoEditor(
            titulo = activity.getString(R.string.titulo_da_confirmacao_de_exclusao_da_camada),
            mensagem = activity.getString(R.string.mensagem_da_confirmacao_de_exclusao_da_camada),
            textoInicial = null,
            textoConfirmar = activity.getString(R.string.excluir_na_caixa_do_editor),
            corConfirmar = Color.parseColor("#F28B82"),
        ) {
            editor.excluirCamadaSelecionada()
        }
    }

    private fun pedirTextoDaComposicao() {
        val atual = editor.textoSelecionado()
        mostrarCaixaDoEditor(
            titulo = activity.getString(R.string.titulo_texto_da_composicao),
            mensagem = null,
            textoInicial = atual ?: "",
            textoConfirmar = activity.getString(R.string.concluir_caixa_do_editor),
            corConfirmar = Color.parseColor("#8AB4F8"),
            abrirTeclado = true,
        ) { escrito ->
            if (atual != null) editor.atualizarTextoSelecionado(escrito.orEmpty())
            else if (!editor.adicionarTexto(escrito.orEmpty())) {
                Toast.makeText(activity, R.string.limite_de_imagens_da_composicao, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarCaixaDoEditor(
        titulo: String,
        mensagem: String?,
        textoInicial: String?,
        textoConfirmar: String,
        corConfirmar: Int,
        abrirTeclado: Boolean = false,
        aoConfirmar: (String?) -> Unit,
    ) {
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(R.layout.layout_caixa_de_dialogo_do_editor_de_imagem, null)
        raiz.findViewById<TextView>(R.id.titulo_da_caixa_de_dialogo_do_editor).text = titulo
        val mensagemNaCaixa = raiz.findViewById<TextView>(R.id.mensagem_da_caixa_de_dialogo_do_editor)
        if (mensagem == null) {
            mensagemNaCaixa.visibility = View.GONE
        } else {
            mensagemNaCaixa.visibility = View.VISIBLE
            mensagemNaCaixa.text = mensagem
        }
        val campo = raiz.findViewById<EditText>(R.id.campo_da_caixa_de_dialogo_do_editor)
        if (textoInicial == null) {
            campo.visibility = View.GONE
        } else {
            campo.visibility = View.VISIBLE
            campo.setText(textoInicial)
            campo.setSelection(campo.text.length)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                campo.isAutoHandwritingEnabled = false
            }
        }
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
            val escrito = if (textoInicial == null) null else campo.text.toString()
            dialogo.dismiss()
            aoConfirmar(escrito)
        }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (abrirTeclado) {
            dialogo.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
        dialogo.setOnShowListener {
            if (!abrirTeclado) return@setOnShowListener
            campo.requestFocus()
            campo.postDelayed({
                if (!dialogo.isShowing) return@postDelayed
                val teclado = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                teclado.showSoftInput(campo, InputMethodManager.SHOW_IMPLICIT)
            }, 300)
        }
        dialogo.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun pedirTamanhoDaMoldura() {
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(
            R.layout.layout_caixa_de_dialogo_do_tamanho_livre_da_moldura_do_editor_de_imagem,
            null,
        )
        val campoLargura = raiz.findViewById<EditText>(R.id.campo_da_largura_livre_da_moldura)
        val campoAltura = raiz.findViewById<EditText>(R.id.campo_da_altura_livre_da_moldura)
        fun preencherCampos() {
            campoLargura.setText(editor.larguraDaMoldura().toString())
            campoAltura.setText(editor.alturaDaMoldura().toString())
            campoLargura.setSelection(campoLargura.text.length)
        }
        preencherCampos()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            campoLargura.isAutoHandwritingEnabled = false
            campoAltura.isAutoHandwritingEnabled = false
        }
        val densidade = activity.resources.displayMetrics.density
        fun marcarChip(botao: Button, selecionado: Boolean) {
            botao.backgroundTintList = null
            botao.stateListAnimator = null
            botao.setTextColor(Color.parseColor(if (selecionado) "#F4F7FB" else "#8A9099"))
            botao.background = if (selecionado) {
                GradientDrawable().apply {
                    cornerRadius = 16f * densidade
                    setColor(Color.parseColor("#2A3140"))
                }
            } else {
                null
            }
        }
        fun atualizarChipsDaCaixa(forcarLivre: Boolean = false) {
            val atual = editor.proporcaoAtual()
            val dimensoesDaImagem = editor.dimensoesDaImagemAlvo()
            val proporcaoOriginal = dimensoesDaImagem != null &&
                MolduraDaComposicaoDeImagem.ehProporcaoOriginal(
                    editor.larguraDaMoldura(),
                    editor.alturaDaMoldura(),
                    dimensoesDaImagem.first,
                    dimensoesDaImagem.second,
                )
            val livre = forcarLivre || (atual == null && !proporcaoOriginal)
            marcarChip(
                raiz.findViewById(R.id.botao_proporcao_um_por_um_da_caixa_da_moldura),
                !forcarLivre && atual == ProporcaoDaMolduraDaComposicao.QUADRADO,
            )
            marcarChip(
                raiz.findViewById(R.id.botao_proporcao_quatro_por_tres_da_caixa_da_moldura),
                !forcarLivre && atual == ProporcaoDaMolduraDaComposicao.QUATRO_POR_TRES,
            )
            marcarChip(
                raiz.findViewById(R.id.botao_proporcao_dezesseis_por_nove_da_caixa_da_moldura),
                !forcarLivre && atual == ProporcaoDaMolduraDaComposicao.DEZESSEIS_POR_NOVE,
            )
            marcarChip(
                raiz.findViewById(R.id.botao_proporcao_nove_por_dezesseis_da_caixa_da_moldura),
                !forcarLivre && atual == ProporcaoDaMolduraDaComposicao.NOVE_POR_DEZESSEIS,
            )
            marcarChip(
                raiz.findViewById(R.id.botao_proporcao_original_da_caixa_da_moldura),
                !forcarLivre && proporcaoOriginal,
            )
            marcarChip(raiz.findViewById(R.id.botao_proporcao_livre_da_caixa_da_moldura), livre)
        }
        fun aplicarProporcaoEFechar(acao: () -> Boolean) {
            if (!acao()) return
            marcarMoldura()
            dialogo.dismiss()
        }
        raiz.findViewById<Button>(R.id.botao_proporcao_um_por_um_da_caixa_da_moldura).setOnClickListener {
            aplicarProporcaoEFechar {
                editor.definirProporcao(ProporcaoDaMolduraDaComposicao.QUADRADO)
                true
            }
        }
        raiz.findViewById<Button>(R.id.botao_proporcao_quatro_por_tres_da_caixa_da_moldura).setOnClickListener {
            aplicarProporcaoEFechar {
                editor.definirProporcao(ProporcaoDaMolduraDaComposicao.QUATRO_POR_TRES)
                true
            }
        }
        raiz.findViewById<Button>(R.id.botao_proporcao_dezesseis_por_nove_da_caixa_da_moldura).setOnClickListener {
            aplicarProporcaoEFechar {
                editor.definirProporcao(ProporcaoDaMolduraDaComposicao.DEZESSEIS_POR_NOVE)
                true
            }
        }
        raiz.findViewById<Button>(R.id.botao_proporcao_nove_por_dezesseis_da_caixa_da_moldura).setOnClickListener {
            aplicarProporcaoEFechar {
                editor.definirProporcao(ProporcaoDaMolduraDaComposicao.NOVE_POR_DEZESSEIS)
                true
            }
        }
        raiz.findViewById<Button>(R.id.botao_proporcao_original_da_caixa_da_moldura).setOnClickListener {
            if (!editor.definirProporcaoOriginal()) {
                Toast.makeText(
                    activity,
                    R.string.moldura_sem_imagem_para_proporcao_original,
                    Toast.LENGTH_LONG,
                ).show()
                return@setOnClickListener
            }
            marcarMoldura()
            dialogo.dismiss()
        }
        raiz.findViewById<Button>(R.id.botao_proporcao_livre_da_caixa_da_moldura).setOnClickListener {
            atualizarChipsDaCaixa(forcarLivre = true)
            campoLargura.requestFocus()
            val teclado = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            teclado.showSoftInput(campoLargura, InputMethodManager.SHOW_IMPLICIT)
        }
        atualizarChipsDaCaixa()
        val cancelar = raiz.findViewById<Button>(R.id.botao_cancelar_do_tamanho_livre_da_moldura)
        val confirmar = raiz.findViewById<Button>(R.id.botao_confirmar_do_tamanho_livre_da_moldura)
        cancelar.backgroundTintList = null
        cancelar.stateListAnimator = null
        confirmar.backgroundTintList = null
        confirmar.stateListAnimator = null
        cancelar.setOnClickListener { dialogo.dismiss() }
        confirmar.setOnClickListener {
            val larguraEscrita = campoLargura.text.toString().toIntOrNull()
            val alturaEscrita = campoAltura.text.toString().toIntOrNull()
            if (larguraEscrita == null || alturaEscrita == null) {
                Toast.makeText(activity, R.string.tamanho_de_moldura_invalido, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            dialogo.dismiss()
            editor.definirTamanhoLivre(larguraEscrita, alturaEscrita)
            marcarMoldura()
        }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        dialogo.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun escolherCorDaPaleta(cor: Int) {
        if (contextoDaBarra == FerramentaDaBarraDoEditorDeImagem.TEXTO) {
            editor.definirCorDoTexto(cor)
        } else {
            editor.definirCorDoTraco(cor)
            if (editor.ferramentaAtual() != EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA) {
                val ferramenta = if (cor == PaletaDoEditorDeImagem.BRANCO) {
                    EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BRANCO
                } else {
                    EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO
                }
                if (editor.ferramentaAtual() != ferramenta) escolherFerramenta(ferramenta)
            }
        }
        mostrarCores()
    }

    private fun marcarBotaoDaPonta(botao: Button, ligado: Boolean, densidade: Float) {
        botao.backgroundTintList = null
        botao.stateListAnimator = null
        botao.setTextColor(Color.parseColor(if (ligado) "#F4F7FB" else "#8A9099"))
        botao.background = if (ligado) {
            GradientDrawable().apply {
                cornerRadius = 16f * densidade
                setColor(Color.parseColor("#2A3140"))
            }
        } else {
            null
        }
    }

    private fun mostrarCores() {
        agendarSincronizacaoDoChrome()
        val nomes = intArrayOf(
            R.string.cor_branca_do_texto,
            R.string.cor_preta_do_texto,
            R.string.cor_vermelha_do_texto,
            R.string.cor_azul_do_editor,
            R.string.cor_amarela_do_editor,
            R.string.cor_verde_do_editor,
        )
        val alvo = if (contextoDaBarra == FerramentaDaBarraDoEditorDeImagem.TEXTO) {
            editor.corDoTextoAtual()
        } else {
            editor.corDoTraco()
        }
        val escolhida = PaletaDoEditorDeImagem.posicao(alvo)
        val densidade = activity.resources.displayMetrics.density
        amostrasDaPaleta.forEachIndexed { indice, botao ->
            botao.setColorFilter(PaletaDoEditorDeImagem.cores[indice])
            botao.contentDescription = activity.getString(nomes[indice])
            botao.background = if (indice == escolhida) {
                GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.TRANSPARENT)
                    setStroke((2f * densidade).toInt(), Color.parseColor("#8AB4F8"))
                }
            } else {
                null
            }
        }
        botaoCorDaFolha.setColorFilter(editor.corDaFolha())
        botaoCorDaFolha.contentDescription = activity.getString(
            R.string.cor_da_folha_da_edicao,
        ) + ", " + activity.getString(nomes[PaletaDoEditorDeImagem.indice(editor.corDaFolha())])
        botaoTracoVermelho.setColorFilter(editor.corDoTraco())
        botaoTracoBranco.setColorFilter(Color.WHITE)
        botaoContaGotas.setColorFilter(Color.parseColor("#F4F7FB"))
        botaoContaGotas.background = if (editor.contaGotasArmado()) {
            GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#2A3140"))
            }
        } else {
            null
        }
        val ponta = editor.pontaAtual()
        marcarBotaoDaPonta(botaoOpacidadeDoTraco, ponta == PontaDoTracoDaComposicao.MARCADOR, densidade)
        marcarBotaoDaPonta(botaoNanquimDoTraco, ponta == PontaDoTracoDaComposicao.NANQUIM, densidade)
        marcarBotaoDaPonta(botaoLapisDoTraco, ponta == PontaDoTracoDaComposicao.LAPIS, densidade)
        marcarBotaoDaPonta(botaoSetaDoTraco, ponta == PontaDoTracoDaComposicao.SETA, densidade)
        val dedo = editor.desenhaComODedo()
        botaoDesenharComODedo.backgroundTintList = null
        botaoDesenharComODedo.stateListAnimator = null
        botaoDesenharComODedo.setTextColor(Color.parseColor(if (dedo) "#F4F7FB" else "#8A9099"))
        botaoDesenharComODedo.background = if (dedo) {
            GradientDrawable().apply {
                cornerRadius = 16f * densidade
                setColor(Color.parseColor("#2A3140"))
            }
        } else {
            null
        }
    }

    private fun marcarMoldura() {
        agendarSincronizacaoDoChrome()
        val atual = editor.proporcaoAtual()
        val dimensoesDaImagem = editor.dimensoesDaImagemAlvo()
        val proporcaoOriginal = dimensoesDaImagem != null &&
            MolduraDaComposicaoDeImagem.ehProporcaoOriginal(
                editor.larguraDaMoldura(),
                editor.alturaDaMoldura(),
                dimensoesDaImagem.first,
                dimensoesDaImagem.second,
            )
        val densidade = activity.resources.displayMetrics.density
        fun marcarSelecionado(id: Int, selecionado: Boolean) {
            val botao = activity.findViewById<Button>(id)
            botao.backgroundTintList = null
            botao.stateListAnimator = null
            botao.setTextColor(Color.parseColor(if (selecionado) "#F4F7FB" else "#8A9099"))
            botao.background = if (selecionado) {
                GradientDrawable().apply {
                    cornerRadius = 16f * densidade
                    setColor(Color.parseColor("#2A3140"))
                }
            } else {
                null
            }
        }
        marcarSelecionado(R.id.botao_moldura_um_por_um, atual == ProporcaoDaMolduraDaComposicao.QUADRADO)
        marcarSelecionado(R.id.botao_moldura_quatro_por_tres, atual == ProporcaoDaMolduraDaComposicao.QUATRO_POR_TRES)
        marcarSelecionado(R.id.botao_moldura_dezesseis_por_nove, atual == ProporcaoDaMolduraDaComposicao.DEZESSEIS_POR_NOVE)
        marcarSelecionado(R.id.botao_moldura_nove_por_dezesseis, atual == ProporcaoDaMolduraDaComposicao.NOVE_POR_DEZESSEIS)
        marcarSelecionado(R.id.botao_moldura_proporcao_original, proporcaoOriginal)
        marcarSelecionado(R.id.botao_moldura_livre, atual == null && !proporcaoOriginal)
        val resolucao = activity.findViewById<Button>(R.id.botao_resolucao_da_moldura_da_composicao)
        val texto = MolduraDaComposicaoDeImagem.resolucao(editor.larguraDaMoldura(), editor.alturaDaMoldura())
        resolucao.text = texto
        resolucao.contentDescription = activity.getString(R.string.resolucao_da_moldura_da_composicao, texto)
        resolucao.backgroundTintList = null
        resolucao.stateListAnimator = null
        resolucao.setTextColor(Color.parseColor("#8AB4F8"))
        resolucao.background = null
    }

    fun mostrarImagemVindaDeFora(origem: Bitmap): String {
        if (editandoImagem) estacionarEdicaoAtual()
        if (painel.visibility != View.VISIBLE) {
            fechando = false
            fundo.alpha = 1f
            painel.visibility = View.VISIBLE
        }
        painel.bringToFront()
        idDoRascunhoAberto = null
        idRetomavelNaFoto = null
        quadroAmpliado?.let { if (!it.isRecycled) it.recycle() }
        quadroAmpliado = origem
        imagemAbertaId = null
        editor.visibility = View.GONE
        editandoImagem = false
        ocultarChipDoZoomDaVistaAgora()
        imagemAmpliada.visibility = View.VISIBLE
        imagemAmpliada.setImageBitmap(origem)
        mostrarChromeDeVisita()
        activity.findViewById<View>(R.id.botao_baixar_imagem_ampliada_da_biblioteca).visibility = View.GONE
        activity.findViewById<View>(R.id.botao_renomear_imagem_ampliada_da_biblioteca).visibility = View.GONE
        activity.findViewById<View>(R.id.botao_favoritar_imagem_ampliada_da_biblioteca).visibility = View.GONE
        visor.visibility = View.VISIBLE
        visor.bringToFront()
        if (gavetaSobreAImagem) recolherGavetaSobreAImagem()
        return "Abri a imagem no visor."
    }

    fun editarImagemVindaDaPagina(origem: Bitmap) {
        if (editandoImagem) estacionarEdicaoAtual()
        idDoRascunhoAberto = null
        idRetomavelNaFoto = null
        quadroAmpliado?.let { if (!it.isRecycled) it.recycle() }
        quadroAmpliado = null
        imagemAbertaId = null
        imagemAmpliada.setImageDrawable(null)
        editor.definirImagem(origem)
        if (!origem.isRecycled) origem.recycle()
        escolherFerramenta(EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO)
        imagemAmpliada.visibility = View.GONE
        editor.visibility = View.VISIBLE
        visor.visibility = View.VISIBLE
        editandoImagem = true
        mostrarChromeDeEdicao()
        visor.bringToFront()
        mostrarEspessura()
        mostrarCores()
        marcarMoldura()
        atualizarLinhaSecundaria()
        if (gavetaSobreAImagem) recolherGavetaSobreAImagem()
    }

    fun acrescentarImagemNaUltimaEdicao(origem: Bitmap): EntradaDaImagemNaEdicao {
        try {
            if (!editandoImagem) {
                val id = gravacaoDoRascunho.listar().firstOrNull() ?: return EntradaDaImagemNaEdicao.SEM_EDICAO
                continuarEdicao(id)
                if (!editandoImagem) return EntradaDaImagemNaEdicao.INDISPONIVEL
            }
            if (!editor.adicionarImagem(origem)) return EntradaDaImagemNaEdicao.SEM_ESPACO
            visor.bringToFront()
            if (gavetaSobreAImagem) recolherGavetaSobreAImagem()
            marcarMoldura()
            return EntradaDaImagemNaEdicao.ENTROU
        } finally {
            if (!origem.isRecycled) origem.recycle()
        }
    }

    fun enviarBitmapDoComponenteParaCamadaAtiva(
        origem: Bitmap,
        idCamadaDestino: Int = -1,
    ): ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
        if (!editandoImagem) {
            return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
        }
        return if (idCamadaDestino != -1) {
            editor.fundirBitmapNaCamadaAtiva(origem, idCamadaDestino)
        } else {
            editor.fundirBitmapNaCamadaAtiva(origem)
        }
    }

    /** Bounds do quadro do editor em coordenadas de tela, ou null se não estiver editando. */
    fun retanguloDoQuadroDoEditorNaTela(): Rect? {
        if (!editandoImagem) return null
        if (editor.visibility != View.VISIBLE || editor.width <= 0 || editor.height <= 0) return null
        val loc = IntArray(2)
        editor.getLocationOnScreen(loc)
        return Rect(loc[0], loc[1], loc[0] + editor.width, loc[1] + editor.height)
    }

    /** Id da camada sob o ponto de tela na lista, ou null. */
    fun idDaCamadaSobPontoNaTela(xNaTela: Float, yNaTela: Float): Int? {
        if (!editandoImagem || listaDeCamadas.visibility != View.VISIBLE) return null
        val loc = IntArray(2)
        for (indice in 0 until linhasDasCamadas.childCount) {
            val linha = linhasDasCamadas.getChildAt(indice) ?: continue
            if (linha.visibility != View.VISIBLE) continue
            linha.getLocationOnScreen(loc)
            if (
                xNaTela >= loc[0] &&
                xNaTela <= loc[0] + linha.width &&
                yNaTela >= loc[1] &&
                yNaTela <= loc[1] + linha.height
            ) {
                return linha.tag as? Int
            }
        }
        return null
    }

    private fun mostrarEspessura() {
        agendarSincronizacaoDoChrome()
        val atual = editor.espessuraAtual()
        indicadorDaEspessura.text = atual.toString()
        indicadorDaEspessura.contentDescription = activity.getString(
            R.string.espessura_do_traco,
            atual,
            editor.quantidadeDeEspessuras(),
        )
    }

    private fun escolherTexto() {
        val aberto = barraDeEdicaoDaImagem.visibility == View.VISIBLE
        val mesmoIcone = contextoDaBarra == FerramentaDaBarraDoEditorDeImagem.TEXTO
        if (!MenuDaFerramentaDaFaixaLateral.ficaAberto(jaNesseIcone = mesmoIcone, aberto = aberto)) {
            barraDeEdicaoDaImagem.visibility = View.GONE
            return
        }
        if (editor.ferramentaAtual() == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE) {
            editor.definirFerramenta(EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO)
            ferramentaDaEdicao = EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO
        }
        contextoDaBarra = FerramentaDaBarraDoEditorDeImagem.TEXTO
        marcarFerramenta(ferramentaDaEdicao)
        atualizarLinhaSecundaria()
        mostrarCores()
        pedirTextoDaComposicao()
    }

    private fun contextoDaFerramenta(
        ferramenta: EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta,
    ): FerramentaDaBarraDoEditorDeImagem {
        return if (ferramenta == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE) {
            FerramentaDaBarraDoEditorDeImagem.RECORTE
        } else {
            FerramentaDaBarraDoEditorDeImagem.TRACO
        }
    }

    private fun atualizarLinhaSecundaria() {
        agendarSincronizacaoDoChrome()
        val linha = BarraContextualDoEditorDeImagem.linha(contextoDaBarra)
        linhaDeProporcao.visibility = View.GONE
        linhaDeAcaoDoRecorte.visibility =
            if (linha == LinhaSecundariaDoEditorDeImagem.SELECAO) View.VISIBLE else View.GONE
        if (linhaDeAcaoDoRecorte.visibility == View.VISIBLE) marcarFormaDaSelecao()
        linhaDeEspessura.visibility = if (linha == LinhaSecundariaDoEditorDeImagem.ESPESSURA) View.VISIBLE else View.GONE
        val borracha = ferramentaDaEdicao == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA &&
            contextoDaBarra == FerramentaDaBarraDoEditorDeImagem.TRACO
        val linhaDeCorOuEspessura = linha == LinhaSecundariaDoEditorDeImagem.COR ||
            linha == LinhaSecundariaDoEditorDeImagem.ESPESSURA
        linhaDeCor.visibility = if (CoresNoMenuDaFerramenta.aparecem(borracha) && linhaDeCorOuEspessura) {
            View.VISIBLE
        } else {
            View.GONE
        }
        val densidade = activity.resources.displayMetrics.density
        val indice = FaixaLateralDoEditorDeImagem.indice(ferramentaEmpilhada())
        barraDeEdicaoDaImagem.translationY = (8f + indice * 48f) * densidade
    }

    private fun ferramentaEmpilhada(): FerramentaEmpilhadaDoEditorDeImagem {
        if (contextoDaBarra == FerramentaDaBarraDoEditorDeImagem.TEXTO) {
            return FerramentaEmpilhadaDoEditorDeImagem.TEXTO
        }
        return when (ferramentaDaEdicao) {
            EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE -> FerramentaEmpilhadaDoEditorDeImagem.RECORTE
            EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BRANCO -> FerramentaEmpilhadaDoEditorDeImagem.BRANCO
            EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA -> FerramentaEmpilhadaDoEditorDeImagem.BORRACHA
            EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO -> FerramentaEmpilhadaDoEditorDeImagem.VERMELHO
        }
    }

    private fun alternarMenuDaFaixa(ferramenta: EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta) {
        val mesmoIcone = when (ferramenta) {
            EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE ->
                contextoDaBarra == FerramentaDaBarraDoEditorDeImagem.RECORTE
            EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA ->
                contextoDaBarra == FerramentaDaBarraDoEditorDeImagem.TRACO &&
                    ferramentaDaEdicao == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA
            else -> false
        }
        val aberto = barraDeEdicaoDaImagem.visibility == View.VISIBLE
        if (!MenuDaFerramentaDaFaixaLateral.ficaAberto(jaNesseIcone = mesmoIcone, aberto = aberto)) {
            barraDeEdicaoDaImagem.visibility = View.GONE
            return
        }
        escolherFerramenta(ferramenta)
    }

    private fun escolherFerramenta(ferramenta: EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta) {
        val repetiuRecorte = ferramenta == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE &&
            ferramentaDaEdicao == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE
        if (!repetiuRecorte) {
            editor.definirFerramenta(ferramenta)
            if (ferramenta == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA) editor.desarmarContaGotas()
        }
        ferramentaDaEdicao = editor.ferramentaAtual()
        contextoDaBarra = contextoDaFerramenta(ferramentaDaEdicao)
        marcarFerramenta(ferramentaDaEdicao)
        atualizarLinhaSecundaria()
    }

    private fun marcarFormaDaSelecao() {
        agendarSincronizacaoDoChrome()
        val densidade = activity.resources.displayMetrics.density
        val laco = editor.formaDaSelecaoAtual() == FormaDaSelecaoSobreAImagem.LACO
        fun marcar(botao: Button, ativo: Boolean) {
            botao.isAllCaps = false
            botao.backgroundTintList = null
            botao.stateListAnimator = null
            botao.setTextColor(Color.parseColor(if (ativo) "#F4F7FB" else "#8A9099"))
            botao.background = if (ativo) {
                GradientDrawable().apply {
                    cornerRadius = 16f * densidade
                    setColor(Color.parseColor("#2A3140"))
                }
            } else {
                null
            }
        }
        marcar(botaoSelecaoRetangular, !laco)
        marcar(botaoSelecaoEmLaco, laco)
        botaoSelecaoInteligente.visibility = View.GONE
        atualizarAcoesDoRecorteComSelecaoAtiva()
    }

    private fun atualizarAcoesDoRecorteComSelecaoAtiva() {
        agendarSincronizacaoDoChrome()
        activity.findViewById<View>(R.id.botao_aplicar_recorte_da_imagem).visibility = View.GONE
        activity.findViewById<View>(R.id.botao_destacar_pedaco_da_imagem).visibility = View.GONE
    }

    private fun pedirConfirmacaoDeRecorteDaSelecaoAtiva() {
        mostrarCaixaDoEditor(
            titulo = activity.getString(R.string.titulo_recortar_moldura_pela_selecao_do_laco),
            mensagem = activity.getString(R.string.mensagem_recortar_moldura_pela_selecao_do_laco),
            textoInicial = null,
            textoConfirmar = activity.getString(R.string.confirmar_recortar_moldura_pela_selecao_do_laco),
            corConfirmar = Color.parseColor("#F4F7FB"),
        ) {
            if (!editor.confirmarRecorteDaSelecaoAtivaNaMoldura()) {
                Toast.makeText(activity, R.string.recorte_precisa_escolher_imagem, Toast.LENGTH_LONG).show()
            }
            atualizarAcoesDoRecorteComSelecaoAtiva()
            marcarMoldura()
            atualizarListaDeCamadas()
        }
        // Cancelar no modal mantém a seleção (só dismiss)
    }

    private fun alternarMenuDaPintura() {
        val pintura = contextoDaBarra == FerramentaDaBarraDoEditorDeImagem.TRACO &&
            (ferramentaDaEdicao == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO ||
                ferramentaDaEdicao == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BRANCO)
        val aberto = barraDeEdicaoDaImagem.visibility == View.VISIBLE
        if (!MenuDoPontoDePintura.ficaAberto(jaNaPintura = pintura, aberto = aberto)) {
            barraDeEdicaoDaImagem.visibility = View.GONE
            return
        }
        val ferramenta = if (editor.corDoTraco() == PaletaDoEditorDeImagem.BRANCO) {
            EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BRANCO
        } else {
            EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO
        }
        escolherFerramenta(ferramenta)
        mostrarCores()
    }

    private fun marcarFerramenta(ferramenta: EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta) {
        agendarSincronizacaoDoChrome()
        val densidade = activity.resources.displayMetrics.density
        fun marcar(botao: ImageButton, selecionado: Boolean) {
            botao.background = if (selecionado) {
                GradientDrawable().apply {
                    cornerRadius = 24f * densidade
                    setColor(Color.parseColor("#2A3140"))
                }
            } else {
                null
            }
        }
        val texto = contextoDaBarra == FerramentaDaBarraDoEditorDeImagem.TEXTO
        marcar(botaoRecortar, !texto && ferramenta == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.RECORTE)
        val pintura = ferramenta == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.VERMELHO ||
            ferramenta == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BRANCO
        marcar(botaoTracoVermelho, !texto && pintura)
        marcar(botaoTracoBranco, false)
        marcar(botaoBorracha, !texto && ferramenta == EditorDeRecorteETracoDaImagemDaBiblioteca.Ferramenta.BORRACHA)
        marcar(botaoTexto, texto)
    }

    /** Formato, fundo e tamanho da imagem final; as escolhas ficam lembradas. */
    private fun mostrarOpcoesDeSalvar() {
        if (!editor.temAlteracao()) {
            Toast.makeText(activity, R.string.edicao_sem_alteracao, Toast.LENGTH_LONG).show()
            return
        }
        val regra = com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao
        val opcoes = regra.opcoes(editor.larguraDaMoldura(), editor.alturaDaMoldura(), editor.densidadeMaximaDasCamadas())
        if (opcoes.isEmpty()) return
        val d = activity.resources.displayMetrics.density
        fun dp(v: Float) = (v * d).toInt()
        var formato = runCatching {
            com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Formato.valueOf(preferenciasDoEstudio.getString("exportar-formato", "PNG").orEmpty())
        }.getOrDefault(com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Formato.PNG)
        var transparente = preferenciasDoEstudio.getBoolean("exportar-fundo-transparente", false)
        val tipoLembrado = preferenciasDoEstudio.getString("exportar-tamanho", com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Tipo.LADO_2048.name)
        var escolhida = opcoes.firstOrNull { it.tipo.name == tipoLembrado }
            ?: opcoes.firstOrNull { it.tipo == com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Tipo.LADO_2048 } ?: opcoes.last()
        val dialogo = Dialog(activity)
        val corpo = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22f), dp(20f), dp(22f), dp(14f))
            background = GradientDrawable().apply { cornerRadius = 18f * d; setColor(Color.parseColor("#1A1D24")) }
        }
        fun titulo(texto: Int, tamanho: Float, cor: String, topo: Float = 0f) = TextView(activity).apply {
            text = activity.getString(texto)
            textSize = tamanho
            setTextColor(Color.parseColor(cor))
            setPadding(0, dp(topo), 0, dp(6f))
        }
        fun pilula(texto: String, ativa: Boolean, aoTocar: () -> Unit) = TextView(activity).apply {
            this.text = texto
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(Color.parseColor(if (ativa) "#AECBFA" else "#F4F7FB"))
            background = GradientDrawable().apply {
                cornerRadius = 12f * d
                setColor(Color.parseColor(if (ativa) "#298AB4F8" else "#262A33"))
            }
            minHeight = dp(44f)
            setPadding(dp(14f), 0, dp(14f), 0)
            isClickable = true
            setOnClickListener { aoTocar() }
        }
        val areaDoFormato = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        val chaveDoFundo = android.widget.Switch(activity).apply {
            text = activity.getString(R.string.estudio_fundo_transparente)
            setTextColor(Color.parseColor("#C4C7CE"))
            textSize = 15f
            setPadding(0, dp(10f), 0, dp(4f))
        }
        val areaDoTamanho = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        fun rotuloDoTamanho(o: com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Opcao) = when (o.tipo) {
            com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Tipo.MOLDURA -> activity.getString(R.string.estudio_tamanho_moldura, o.largura, o.altura)
            com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Tipo.MAXIMO -> activity.getString(R.string.estudio_tamanho_maximo, o.largura, o.altura)
            else -> activity.getString(R.string.estudio_tamanho_lado, o.largura, o.altura)
        }
        lateinit var redesenhar: () -> Unit
        redesenhar = {
            areaDoFormato.removeAllViews()
            listOf(com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Formato.PNG to R.string.estudio_formato_png, com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Formato.JPEG to R.string.estudio_formato_jpeg)
                .forEach { (f, rotulo) ->
                    areaDoFormato.addView(
                        pilula(activity.getString(rotulo), formato == f) { formato = f; redesenhar() },
                        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(8f) },
                    )
                }
            chaveDoFundo.visibility = if (formato == com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Formato.PNG) View.VISIBLE else View.GONE
            chaveDoFundo.setOnCheckedChangeListener(null)
            chaveDoFundo.isChecked = transparente
            chaveDoFundo.setOnCheckedChangeListener { _, marcado -> transparente = marcado }
            areaDoTamanho.removeAllViews()
            opcoes.forEach { o ->
                areaDoTamanho.addView(
                    pilula(rotuloDoTamanho(o), o == escolhida) { escolhida = o; redesenhar() },
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        .apply { bottomMargin = dp(6f) },
                )
            }
        }
        corpo.addView(titulo(R.string.estudio_salvar_titulo, 20f, "#F4F7FB").apply { paint.isFakeBoldText = true })
        corpo.addView(titulo(R.string.estudio_formato, 13f, "#8A9099", topo = 8f))
        corpo.addView(areaDoFormato)
        corpo.addView(chaveDoFundo)
        corpo.addView(titulo(R.string.estudio_tamanho, 13f, "#8A9099", topo = 12f))
        corpo.addView(areaDoTamanho)
        val acoes = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(10f), 0, 0)
        }
        fun botaoDeTexto(texto: Int, cor: String, aoTocar: () -> Unit) = TextView(activity).apply {
            text = activity.getString(texto)
            textSize = 15f
            setTextColor(Color.parseColor(cor))
            minHeight = dp(44f)
            gravity = Gravity.CENTER
            setPadding(dp(16f), 0, dp(16f), 0)
            isClickable = true
            setOnClickListener { aoTocar() }
        }
        acoes.addView(botaoDeTexto(R.string.estudio_cancelar, "#8A9099") { dialogo.dismiss() })
        acoes.addView(botaoDeTexto(R.string.estudio_salvar, "#8AB4F8") {
            preferenciasDoEstudio.edit()
                .putString("exportar-formato", formato.name)
                .putBoolean("exportar-fundo-transparente", transparente)
                .putString("exportar-tamanho", escolhida.tipo.name)
                .apply()
            dialogo.dismiss()
            concluirEdicao(formato, escolhida.largura, escolhida.altura, transparente && formato == com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Formato.PNG)
        })
        corpo.addView(acoes)
        redesenhar()
        dialogo.setContentView(corpo)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.window?.setLayout(dp(380f), ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogo.show()
    }

    private fun concluirEdicao(
        formato: com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Formato =
            com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Formato.JPEG,
        largura: Int = editor.larguraDaMoldura(),
        altura: Int = editor.alturaDaMoldura(),
        fundoTransparente: Boolean = false,
    ) {
        if (!editor.temAlteracao()) {
            Toast.makeText(activity, R.string.edicao_sem_alteracao, Toast.LENGTH_LONG).show()
            return
        }
        val composto = editor.compor(largura, altura, fundoTransparente) ?: run {
            Toast.makeText(activity, R.string.quadro_indisponivel, Toast.LENGTH_LONG).show()
            return
        }
        val gravado = try {
            gravacaoDeImagens.guardarImagemExportada(bibliotecaDeImagens, composto, formato)
        } catch (_: Exception) {
            null
        } finally {
            composto.recycle()
        }
        if (gravado == null) {
            Toast.makeText(activity, R.string.quadro_indisponivel, Toast.LENGTH_LONG).show()
            return
        }
        cancelarEdicao()
        try {
            val item = gravado.first
            gravacaoDeImagens.copiarItem(activity, bibliotecaDeImagens, item)
            abrirVisor(item)
            Toast.makeText(activity, R.string.imagem_editada_copiada, Toast.LENGTH_LONG).show()
            atualizar()
        } catch (_: Exception) {
            Toast.makeText(activity, R.string.quadro_indisponivel, Toast.LENGTH_LONG).show()
        }
    }

    fun estaEditandoImagem(): Boolean = editandoImagem

    /** Chat (ferramenta usar_pincel): seleciona o pincel no editor aberto. */
    fun usarPincelDoMotorNoEditor(id: String): Boolean {
        if (!editandoImagem) return false
        editor.escolherPincelDoMotor(id)
        chromeEstudio?.sincronizar()
        return true
    }

    private fun confirmarDescarteDaEdicao() {
        mostrarCaixaDoEditor(
            titulo = activity.getString(R.string.titulo_da_confirmacao_de_descartar_edicao_da_imagem),
            mensagem = activity.getString(R.string.mensagem_da_confirmacao_de_descartar_edicao_da_imagem),
            textoInicial = null,
            textoConfirmar = activity.getString(R.string.descartar_edicao_da_imagem),
            corConfirmar = Color.parseColor("#F28B82"),
        ) {
            if (editandoImagem) cancelarEdicao()
        }
    }

    private fun cancelarEdicao() {
        val tinhaFoto = quadroAmpliado != null
        idDoRascunhoAberto?.let { gravacaoDoRascunho.apagar(it) }
        idDoRascunhoAberto = null
        idRetomavelNaFoto = null
        chaveDaListaDeRascunhos = null
        abandonarEdicaoNaMemoria()
        soltarSobreposicaoDaGaveta()
        imagemAmpliada.visibility = View.VISIBLE
        if (tinhaFoto) {
            visor.bringToFront()
        } else {
            visor.visibility = View.GONE
        }
        atualizar()
    }

    private fun fecharVisor() {
        if (editandoImagem) cancelarEdicao()
        idRetomavelNaFoto = null
        soltarSobreposicaoDaGaveta()
        visor.visibility = View.GONE
        imagemAmpliada.visibility = View.VISIBLE
        imagemAmpliada.setImageDrawable(null)
        quadroAmpliado?.let { if (!it.isRecycled) it.recycle() }
        quadroAmpliado = null
        imagemAbertaId = null
    }

    private fun atualizarBotoesDoVisor(item: ItemDaBibliotecaDeImagensCapturadas) {
        val favoritar = activity.findViewById<ImageButton>(R.id.botao_favoritar_imagem_ampliada_da_biblioteca)
        favoritar.visibility = View.VISIBLE
        favoritar.setImageResource(
            if (item.favorito) R.drawable.icone_de_video_favorito_preenchido else R.drawable.icone_de_video_favorito_contorno,
        )
        favoritar.contentDescription = activity.getString(
            if (item.favorito) R.string.remover_imagem_dos_favoritos else R.string.favoritar_imagem,
        )
        activity.findViewById<View>(R.id.botao_baixar_imagem_ampliada_da_biblioteca).visibility =
            if (item.naGaleria) View.GONE else View.VISIBLE
        activity.findViewById<View>(R.id.botao_renomear_imagem_ampliada_da_biblioteca).visibility =
            if (item.uriDaGaleria.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    private fun mostrarOpcoesDoToqueLongoNaImagemAmpliada() {
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(
            R.layout.layout_caixa_de_opcoes_do_toque_longo_na_imagem_ampliada_do_visor_da_biblioteca,
            null,
        )
        val copiar = raiz.findViewById<Button>(R.id.botao_copiar_imagem_ampliada_pelo_toque_longo)
        val cancelar = raiz.findViewById<Button>(R.id.botao_cancelar_opcoes_do_toque_longo_na_imagem_ampliada)
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
        }
        val bitmap = quadroAmpliado
        if (bitmap == null || bitmap.isRecycled) {
            Toast.makeText(activity, R.string.quadro_indisponivel, Toast.LENGTH_LONG).show()
            return
        }
        try {
            val png = ByteArrayOutputStream().use { saida ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)
                saida.toByteArray()
            }
            GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.copiarParaAreaDeTransferencia(
                activity,
                png,
                "image/png",
            )
            Toast.makeText(activity, R.string.quadro_copiado, Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(activity, R.string.quadro_indisponivel, Toast.LENGTH_LONG).show()
        }
    }

    private fun copiar(item: ItemDaBibliotecaDeImagensCapturadas) {
        try {
            val atualizado = gravacaoDeImagens.copiarItem(activity, bibliotecaDeImagens, item)
            Toast.makeText(activity, R.string.quadro_copiado, Toast.LENGTH_LONG).show()
            if (imagemAbertaId == item.id) atualizarBotoesDoVisor(atualizado)
            atualizar()
        } catch (_: Exception) {
            Toast.makeText(activity, R.string.quadro_indisponivel, Toast.LENGTH_LONG).show()
        }
    }

    private fun pedirNovoNome(item: ItemDaBibliotecaDeImagensCapturadas) {
        val uri = item.uriDaGaleria ?: return
        val campo = EditText(activity)
        campo.setSingleLine(true)
        campo.setText(item.nomeExibido?.substringBeforeLast('.') ?: "")
        campo.setSelection(campo.text.length)
        AlertDialog.Builder(activity)
            .setTitle(R.string.titulo_renomear_imagem_da_galeria)
            .setView(campo)
            .setPositiveButton(R.string.renomear_imagem_da_galeria) { _, _ ->
                try {
                    val nome = GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.renomearNaGaleria(
                        activity,
                        Uri.parse(uri),
                        item.nomeExibido ?: "imagem.jpg",
                        campo.text.toString(),
                    )
                    bibliotecaDeImagens.renomear(item.id, nome)
                    gravacaoDeImagens.gravar(bibliotecaDeImagens)
                    Toast.makeText(activity, R.string.imagem_renomeada, Toast.LENGTH_LONG).show()
                    atualizar()
                } catch (_: Exception) {
                    Toast.makeText(activity, R.string.nome_de_imagem_invalido, Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun baixar(item: ItemDaBibliotecaDeImagensCapturadas): Boolean {
        val bytes = gravacaoDeImagens.ler(item.nomeDoArquivo) ?: return false
        return try {
            val publicada = GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.salvarNaGaleria(
                activity,
                bytes,
                "image/jpeg",
            )
            gravacaoDeImagens.lembrarGaleria(bibliotecaDeImagens, item.id, publicada.uri.toString(), publicada.nome)
            if (bibliotecaDeImagens.buscar(item.id)?.exportado != true) {
                bibliotecaDeImagens.marcarExportado(item.id)
                gravacaoDeImagens.gravar(bibliotecaDeImagens)
            }
            Toast.makeText(activity, R.string.quadro_salvo, Toast.LENGTH_LONG).show()
            atualizar()
            true
        } catch (_: Exception) {
            Toast.makeText(activity, R.string.quadro_indisponivel, Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun iniciarArrasteDoVideoDaBiblioteca(cartao: View, item: ItemDaBibliotecaDeVideos) {
        val arquivo = item.caminhoLocal.takeIf { it.isNotBlank() }
            ?.let { File(it) }
            ?.takeIf { it.isFile && it.length() > 0L }
            ?: item.enderecoParaAbrir.takeIf { it.startsWith("file://", ignoreCase = true) }
                ?.let { runCatching { File(Uri.parse(it).path.orEmpty()) }.getOrNull() }
                ?.takeIf { it.isFile && it.length() > 0L }
        iniciarArrasteDeArquivoLocalDaBiblioteca(
            cartao = cartao,
            arquivo = arquivo,
            estado = EstadoDoArrasteDaBiblioteca("video", arquivo?.absolutePath.orEmpty(), item.chave),
            mimeForcado = "video/mp4",
        )
    }

    private fun iniciarArrasteDaImagemDaBiblioteca(cartao: View, item: ItemDaBibliotecaDeImagensCapturadas) {
        val arquivo = (
            gravacaoDeImagens.arquivo(item.nomeDoArquivo)
                ?: item.caminhoLocal.takeIf { it.isNotBlank() }?.let { File(it) }
            )?.takeIf { it.isFile && it.length() > 0L }
        iniciarArrasteDeArquivoLocalDaBiblioteca(
            cartao = cartao,
            arquivo = arquivo,
            estado = EstadoDoArrasteDaBiblioteca("image", arquivo?.absolutePath.orEmpty(), item.id),
            mimeForcado = null,
        )
    }

    private fun iniciarArrasteDeArquivoLocalDaBiblioteca(
        cartao: View,
        arquivo: File?,
        estado: EstadoDoArrasteDaBiblioteca,
        mimeForcado: String?,
    ) {
        if (arquivo == null) {
            Toast.makeText(activity, R.string.arraste_da_biblioteca_so_link_baixe_ou_exporte_antes, Toast.LENGTH_SHORT).show()
            return
        }
        val payload = PreparacaoDeUriContentParaArrasteDaBiblioteca.deArquivoLocal(activity, arquivo, mimeForcado)
        if (payload == null) {
            Toast.makeText(activity, R.string.arraste_da_biblioteca_so_link_baixe_ou_exporte_antes, Toast.LENGTH_SHORT).show()
            return
        }
        val aoIniciar = aoIniciarArrasteDaBiblioteca ?: return
        // Fantasma só; clipboard fica pro soltar verde na Activity.
        val miniatura = ((cartao as? ViewGroup)?.getChildAt(0) as? ViewGroup)?.getChildAt(0) as? ImageView
        var bitmapDoFantasma: Bitmap? = (miniatura?.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
        try {
            if (estado.tipo == "video") {
                val quadro = ExtracaoDeQuadroJpegDeVideoLocalParaArraste.extrairParaArquivo(activity, arquivo)
                if (quadro != null) {
                    val bytes = quadro.readBytes()
                    if (bitmapDoFantasma == null) {
                        bitmapDoFantasma = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, miniaturaOpcoes())
                    }
                    runCatching { quadro.delete() }
                }
            } else if (bitmapDoFantasma == null && arquivo.length() <= 20L * 1024 * 1024) {
                bitmapDoFantasma = BitmapFactory.decodeFile(arquivo.absolutePath, miniaturaOpcoes())
            }
        } catch (_: Exception) {
            // Miniatura opcional; o arraste segue.
        }
        cartao.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        aoIniciar(estado, bitmapDoFantasma)
    }

    private fun miniaturaOpcoes(): BitmapFactory.Options =
        BitmapFactory.Options().apply { inSampleSize = 4; inPreferredConfig = Bitmap.Config.ARGB_8888 }

    private fun mostrarModalDoDetalheDoVideo(item: ItemDaBibliotecaDeVideos) {
        val arquivoLocal = item.caminhoLocal.takeIf { it.isNotBlank() }
            ?.let { File(it) }
            ?.takeIf { it.isFile && it.length() > 0L }
        val enderecoDeArquivo = item.enderecoParaAbrir.takeIf { it.startsWith("file://", ignoreCase = true) }
        mostrarModalDoDetalheDaMidia(
            origem = item.origem,
            modelo = item.modelo,
            prompt = item.prompt.ifBlank { item.titulo },
            refs = item.refs,
            arquivoLocal = arquivoLocal,
            enderecoDeArquivo = enderecoDeArquivo,
            jaExportado = item.exportado,
            podeBaixar = arquivoLocal != null,
            usarDeNovo = pedidoDeUsarDeNovo(
                tipo = com.chromebrothers.navegador.regras.ReceitaDeGeracaoMcp.TIPO_VIDEO,
                idDaReceita = item.idDaReceita,
                origem = item.origem,
                modelo = item.modelo,
                prompt = item.prompt,
                refs = item.refs,
            ),
        ) {
            val arquivo = arquivoLocal ?: return@mostrarModalDoDetalheDaMidia false
            val nome = item.titulo.trim().ifBlank { arquivo.nameWithoutExtension }
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                .take(80)
            val uri = ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers.exportar(activity, arquivo, nome)
            if (uri != null) {
                biblioteca.marcarExportado(item.chave)
                gravacao.gravar(biblioteca)
                Toast.makeText(activity, R.string.video_exportado_para_downloads_do_chromebrothers, Toast.LENGTH_LONG).show()
                true
            } else {
                Toast.makeText(activity, R.string.falha_ao_exportar_video_da_biblioteca, Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    private fun mostrarModalDoDetalheDaImagem(item: ItemDaBibliotecaDeImagensCapturadas) {
        val arquivoLocal = gravacaoDeImagens.arquivo(item.nomeDoArquivo)
            ?: item.caminhoLocal.takeIf { it.isNotBlank() }?.let { File(it) }?.takeIf { it.isFile }
        mostrarModalDoDetalheDaMidia(
            origem = item.origem,
            modelo = item.modelo,
            prompt = item.prompt.ifBlank { item.nomeExibido?.substringBeforeLast('.').orEmpty() },
            refs = item.refs,
            arquivoLocal = arquivoLocal,
            enderecoDeArquivo = null,
            jaExportado = item.exportado || item.naGaleria,
            podeBaixar = arquivoLocal != null,
            usarDeNovo = pedidoDeUsarDeNovo(
                tipo = com.chromebrothers.navegador.regras.ReceitaDeGeracaoMcp.TIPO_IMAGEM,
                idDaReceita = item.idDaReceita,
                origem = item.origem,
                modelo = item.modelo,
                prompt = item.prompt,
                refs = item.refs,
            ),
        ) {
            val atual = bibliotecaDeImagens.buscar(item.id) ?: item
            baixar(atual)
        }
    }

    /** Só itens gerados por MCP (com receita ou, se antigos, com prompt) oferecem "Usar de novo". */
    private fun pedidoDeUsarDeNovo(
        tipo: String,
        idDaReceita: String,
        origem: String,
        modelo: String,
        prompt: String,
        refs: List<String>,
    ): PedidoDeUsarDeNovoNoGerador? {
        if (!com.chromebrothers.navegador.regras.MidiaGeradaViaMcpListadaNoVisualizadorDeMidiaDaColecao.ehOrigemDoMcp(origem)) return null
        if (idDaReceita.isBlank() && prompt.isBlank()) return null
        return PedidoDeUsarDeNovoNoGerador(
            tipo = tipo,
            idDaReceita = idDaReceita,
            origem = origem,
            modelo = modelo,
            prompt = prompt,
            imagensDeReferencia = refs.mapNotNull { arquivoDaReferenciaDeGeracao(it) },
        )
    }

    private fun arquivoDaReferenciaDeGeracao(nomeOuCaminho: String): File? {
        if (nomeOuCaminho.isBlank()) return null
        val absoluto = File(nomeOuCaminho)
        if (absoluto.isAbsolute && absoluto.isFile) return absoluto
        if (nomeOuCaminho.contains('/') || nomeOuCaminho.contains('\\')) return null
        val naPasta = File(File(activity.filesDir, "refs-de-geracao-da-biblioteca"), nomeOuCaminho)
        return naPasta.takeIf { it.isFile }
    }

    private fun encurtarCaminho(caminho: String, limite: Int = 60): String {
        return if (caminho.length <= limite) caminho else "…" + caminho.takeLast(limite - 1)
    }

    private fun copiarTextoDoDetalhe(rotulo: String, texto: String, mensagem: Int) {
        if (texto.isBlank()) {
            Toast.makeText(activity, R.string.nada_para_copiar_do_detalhe_da_midia, Toast.LENGTH_SHORT).show()
            return
        }
        val area = activity.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        area.setPrimaryClip(android.content.ClipData.newPlainText(rotulo, texto))
        Toast.makeText(activity, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun mostrarModalDoDetalheDaMidia(
        origem: String,
        modelo: String,
        prompt: String,
        refs: List<String>,
        arquivoLocal: File?,
        enderecoDeArquivo: String?,
        jaExportado: Boolean,
        podeBaixar: Boolean,
        usarDeNovo: PedidoDeUsarDeNovoNoGerador? = null,
        aoBaixar: () -> Boolean,
    ) {
        val densidade = activity.resources.displayMetrics.density
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(R.layout.layout_modal_do_detalhe_da_midia_na_biblioteca, null)
        val rotuloDaOrigem = OrigemDaMidiaNaBibliotecaDoNavegador.rotulo(origem)
        raiz.findViewById<TextView>(R.id.origem_e_modelo_do_detalhe_da_midia).text =
            if (modelo.isBlank()) rotuloDaOrigem else "$rotuloDaOrigem · $modelo"
        val textoDoPrompt = prompt.ifBlank { "—" }
        raiz.findViewById<TextView>(R.id.prompt_do_detalhe_da_midia).text = textoDoPrompt
        raiz.findViewById<View>(R.id.rolagem_do_prompt_do_detalhe_da_midia).apply {
            val alturaMaxima = (220 * densidade).toInt()
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (height > alturaMaxima) {
                        layoutParams = layoutParams.apply { height = alturaMaxima }
                    } else if (height > 0) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            })
        }

        val textoDoCaminho: String
        val caminhoParaCopiar: String
        if (arquivoLocal != null) {
            caminhoParaCopiar = arquivoLocal.absolutePath
            textoDoCaminho = activity.getString(
                R.string.no_aparelho_do_detalhe_da_midia,
                encurtarCaminho(arquivoLocal.absolutePath),
            )
        } else if (enderecoDeArquivo != null) {
            caminhoParaCopiar = enderecoDeArquivo
            textoDoCaminho = encurtarCaminho(enderecoDeArquivo)
        } else {
            caminhoParaCopiar = ""
            textoDoCaminho = activity.getString(R.string.so_link_do_detalhe_da_midia)
        }
        raiz.findViewById<TextView>(R.id.caminho_do_detalhe_da_midia).text = textoDoCaminho

        val faixaDeRefs = raiz.findViewById<View>(R.id.linha_refs_do_detalhe)
        val conteudoDasRefs = raiz.findViewById<LinearLayout>(R.id.conteudo_das_refs_do_detalhe)
        val arquivosDasRefs = refs.mapNotNull { arquivoDaReferenciaDeGeracao(it) }
        if (arquivosDasRefs.isNotEmpty()) {
            faixaDeRefs.visibility = View.VISIBLE
            val lado = (64 * densidade).toInt()
            arquivosDasRefs.forEach { arquivoDaRef ->
                val miniatura = ImageView(activity)
                miniatura.scaleType = ImageView.ScaleType.CENTER_CROP
                miniatura.background = GradientDrawable().apply {
                    cornerRadius = 8f * densidade
                    setColor(Color.parseColor("#22262E"))
                }
                miniatura.outlineProvider = ViewOutlineProvider.BACKGROUND
                miniatura.clipToOutline = true
                miniatura.layoutParams = LinearLayout.LayoutParams(lado, lado).apply {
                    marginEnd = (8 * densidade).toInt()
                }
                conteudoDasRefs.addView(miniatura)
                Thread {
                    val quadro = decodificarMiniatura(arquivoDaRef.absolutePath, lado)
                    activity.runOnUiThread {
                        if (quadro == null) return@runOnUiThread
                        if (dialogo.isShowing) {
                            miniatura.setImageBitmap(quadro)
                        } else if (!quadro.isRecycled) {
                            quadro.recycle()
                        }
                    }
                }.start()
            }
        }

        raiz.findViewById<Button>(R.id.botao_fechar_do_detalhe_da_midia).apply {
            backgroundTintList = null
            stateListAnimator = null
            setOnClickListener { dialogo.dismiss() }
        }
        raiz.findViewById<Button>(R.id.botao_copiar_prompt_do_detalhe_da_midia).apply {
            backgroundTintList = null
            stateListAnimator = null
            setOnClickListener {
                copiarTextoDoDetalhe("prompt", prompt, R.string.prompt_copiado_do_detalhe_da_midia)
            }
        }
        raiz.findViewById<Button>(R.id.botao_copiar_caminho_do_detalhe_da_midia).apply {
            backgroundTintList = null
            stateListAnimator = null
            visibility = if (caminhoParaCopiar.isBlank()) View.GONE else View.VISIBLE
            setOnClickListener {
                copiarTextoDoDetalhe("caminho", caminhoParaCopiar, R.string.caminho_copiado_do_detalhe_da_midia)
            }
        }
        raiz.findViewById<Button>(R.id.botao_usar_de_novo_do_detalhe_da_midia).apply {
            backgroundTintList = null
            stateListAnimator = null
            val acao = aoUsarDeNovoNoGerador
            visibility = if (usarDeNovo != null && acao != null) View.VISIBLE else View.GONE
            setOnClickListener {
                val pedido = usarDeNovo ?: return@setOnClickListener
                dialogo.dismiss()
                acao?.invoke(pedido)
            }
        }
        val botaoBaixar = raiz.findViewById<Button>(R.id.botao_baixar_do_detalhe_da_midia)
        botaoBaixar.backgroundTintList = null
        botaoBaixar.stateListAnimator = null
        if (!podeBaixar) {
            botaoBaixar.visibility = View.GONE
        } else {
            if (jaExportado) botaoBaixar.setText(R.string.ja_na_pasta_publica_do_detalhe_da_midia)
            botaoBaixar.setOnClickListener {
                botaoBaixar.isEnabled = false
                val ok = aoBaixar()
                if (ok) {
                    botaoBaixar.setText(R.string.ja_na_pasta_publica_do_detalhe_da_midia)
                }
                botaoBaixar.isEnabled = true
            }
        }

        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        val largura = (420 * densidade).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun baixarCopiadas() {
        val pendentes = bibliotecaDeImagens.copiadasSemBaixar()
        if (pendentes.isEmpty()) return
        var salvas = 0
        pendentes.forEach { item ->
            val bytes = gravacaoDeImagens.ler(item.nomeDoArquivo) ?: return@forEach
            try {
                val publicada = GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.salvarNaGaleria(
                    activity,
                    bytes,
                    "image/jpeg",
                )
                bibliotecaDeImagens.vincularGaleria(item.id, publicada.uri.toString(), publicada.nome)
                salvas += 1
            } catch (_: Exception) {
            }
        }
        gravacaoDeImagens.gravar(bibliotecaDeImagens)
        Toast.makeText(
            activity,
            if (salvas > 0) R.string.imagens_copiadas_salvas else R.string.quadro_indisponivel,
            Toast.LENGTH_LONG,
        ).show()
        atualizar()
    }

    private fun removerImagemDoHistorico(item: ItemDaBibliotecaDeImagensCapturadas) {
        val resultado = bibliotecaDeImagens.removerDoHistorico(item.id) ?: return
        if (resultado.apagouOItem) {
            gravacaoDeImagens.apagar(resultado.nomeDoArquivo)
            miniaturasDasImagens.remove(item.id)?.let { if (!it.isRecycled) it.recycle() }
        }
        gravacaoDeImagens.gravar(bibliotecaDeImagens)
        if (imagemAbertaId == item.id && bibliotecaDeImagens.buscar(item.id) == null) fecharVisor()
        atualizar()
    }
}

/** O que o ⓘ entrega para "Usar de novo": a receita, ou os dados de um item antigo sem receita. */
data class PedidoDeUsarDeNovoNoGerador(
    val tipo: String,
    val idDaReceita: String,
    val origem: String,
    val modelo: String,
    val prompt: String,
    val imagensDeReferencia: List<File>,
)

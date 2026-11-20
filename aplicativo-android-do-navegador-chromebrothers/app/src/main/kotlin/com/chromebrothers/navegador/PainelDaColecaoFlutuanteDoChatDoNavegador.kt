package com.chromebrothers.navegador

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.util.LruCache
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chromebrothers.navegador.regras.AjusteDaOrdemDaColecao
import com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.FaseDoArrasteDaJanelaPelaBarra
import com.chromebrothers.navegador.regras.ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina
import com.chromebrothers.navegador.regras.FonteDoEnderecoDaImagemDoToqueLongo
import com.chromebrothers.navegador.regras.BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador
import com.chromebrothers.navegador.regras.ItemDaBibliotecaDeComponentesHtmlDaColecao
import com.chromebrothers.navegador.regras.ItemDaColecaoFlutuante
import com.chromebrothers.navegador.regras.MontagemDaColecaoFlutuanteDoChat
import com.chromebrothers.navegador.regras.PosicaoDaJanelaDoCampoDeTexto
import com.chromebrothers.navegador.regras.PosicaoDaJanelaFlutuanteAoAbrirPelaBolhaNoCantoMaisProximo
import com.chromebrothers.navegador.regras.PublicacaoDaPaginaDaColecaoFlutuanteDoChat
import com.chromebrothers.navegador.regras.ResultadoDaBibliotecaDeComponentesHtml
import com.chromebrothers.navegador.regras.SanitizacaoDoHtmlEstaticoDaPaginaDaColecaoFlutuante
import com.chromebrothers.navegador.regras.TamanhoDoItemDaColecaoFlutuante
import com.chromebrothers.navegador.regras.TipoDoItemDaColecaoFlutuante
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PainelDaColecaoFlutuanteDoChatDoNavegador(
    private val activity: AppCompatActivity,
    private val painel: View,
    private val selo: TextView,
    private val baixar: (List<ItemDaColecaoFlutuante>) -> Unit,
    private val abrirVideo: (ItemDaColecaoFlutuante) -> Unit,
    private val abrirEnderecoDaPagina: (String) -> Unit = {},
    private val aoEncerrar: () -> Unit = {},
    private val aoFocar: () -> Unit = {},
    private val deslocamentoInicialPx: Float = 0f,
) {
    private val lista: LinearLayout = painel.findViewById(R.id.lista_da_colecao_flutuante)
    private val rolagem: ScrollView = painel.findViewById(R.id.rolagem_da_colecao_flutuante)
    private val conteudoMidia: View = painel.findViewById(R.id.conteudo_midia_da_colecao_flutuante)
    private val conteudoPagina: View = painel.findViewById(R.id.conteudo_pagina_da_colecao_flutuante)
    private val webDaPagina: WebView = painel.findViewById(R.id.webview_da_pagina_da_colecao_flutuante)
    private val avisoPaginaVazia: TextView = painel.findViewById(R.id.aviso_pagina_da_colecao_ainda_vazia)
    private val botaoModoMidia: TextView = painel.findViewById(R.id.botao_modo_midia_da_colecao_flutuante)
    private val botaoModoPagina: TextView = painel.findViewById(R.id.botao_modo_pagina_da_colecao_flutuante)
    private val botaoGuardarPagina: View =
        painel.findViewById(R.id.botao_guardar_pagina_da_colecao_na_biblioteca)
    private val tituloDaJanela: TextView =
        painel.findViewById(R.id.titulo_da_janela_da_colecao_flutuante)
    private val subtituloDaJanela: TextView =
        painel.findViewById(R.id.subtitulo_da_janela_da_colecao_flutuante)
    private val itens = mutableListOf<ItemDaColecaoFlutuante>()
    private val marcados = linkedSetOf<String>()
    private val miniaturas = object : LruCache<String, Bitmap>(20) {}
    private var geracao = 0
    private val clienteHttp = OkHttpClient.Builder()
        .callTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
    private val filaDeMiniaturas = java.util.concurrent.Executors.newFixedThreadPool(3)
    private var reserva: Bitmap? = null
    private var posicaoDefinida = false
    private var seloPosicionado = false
    private var forma = Forma.OCULTA
    private var encerrada = false
    private var pedido = 0
    /** Mantém a WebView viva ao “fechar”: só esconde (warm-start). */
    private var reservadoParaWarmStart = false
    /** Conteúdo da página está no host da gaveta do chat. */
    private var ancoradaNaGaveta = false
    private var paiOriginalDoConteudoPagina: ViewGroup? = null
    private var indiceOriginalDoConteudoPagina = -1
    private var paramsOriginaisDoConteudoPagina: ViewGroup.LayoutParams? = null
    val idDaJanelaFlutuante: String = "colecao-${System.nanoTime()}"
    private var aoAncorarNaGaveta: (() -> Unit)? = null
    private val curva = DecelerateInterpolator()
    private val modal = ModalDaImagemAmpliadaDaColecaoFlutuanteDoNavegador(activity)
    private val geradorDeMiniaturas by lazy {
        GeradorDeMiniaturasDaGradeDoVisualizadorDeMidiaComCacheEmDisco(activity)
    }
    private val leitorDeMidiaDoVisorDoVisualizador by lazy {
        LeituraDeBytesDeMidiaLocalOuRemotaParaOComponenteDaColecao(activity)
    }
    private var visorDoVisualizadorCriado = false
    private val visorDoVisualizadorDeMidia by lazy {
        visorDoVisualizadorCriado = true
        VisorDePaginaDoVisualizadorDeMidiaNaColecaoDoNavegador(
            activity = activity,
            host = conteudoPagina as FrameLayout,
            leitor = leitorDeMidiaDoVisorDoVisualizador,
            aoMudarIndice = { uri -> destacarCartaDoVisualizador(uri) },
            aoFechar = { avisarImagemNoVisorAoComponente(null) },
            aoToqueLongo = toque@{ uri, x, y ->
                // Prévia de referência é só para conferir: sem arraste nem menu de mover/excluir.
                if (com.chromebrothers.navegador.regras.PreviaDaReferenciaDaGeracaoMcpNoVisor.ehPrevia(uri)) return@toque
                aoIniciarArrasteComMiniatura?.invoke(
                    uri,
                    x,
                    y,
                    GradeNativaDoVisualizadorDeMidiaNaColecaoDoNavegador.miniaturaPronta(uri),
                )
            },
        )
    }

    /** Uri da foto no visor interno desta janela, se aberto. */
    fun uriDoVisorInternoAberto(): String? =
        if (visorDoVisualizadorCriado) visorDoVisualizadorDeMidia.uriAtual() else null

    fun esconderVisorInternoDuranteArraste() {
        if (visorDoVisualizadorCriado) visorDoVisualizadorDeMidia.esconderDuranteArraste()
    }

    fun restaurarVisorInternoAposArraste(fechar: Boolean) {
        if (!visorDoVisualizadorCriado) return
        val visor = visorDoVisualizadorDeMidia
        if (fechar && visor.estaEscondidoPeloArraste()) visor.fechar() else visor.reaparecerAposArraste()
    }
    private var gradeNativaCriada = false
    private val gradeNativaDoVisualizador by lazy {
        gradeNativaCriada = true
        GradeNativaDoVisualizadorDeMidiaNaColecaoDoNavegador(
            activity = activity,
            host = conteudoPagina as FrameLayout,
            gerador = geradorDeMiniaturas,
            listar = {
                aoListarPastaDoAparelho?.invoke(
                    com.chromebrothers.navegador.regras.PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao.URI,
                )
            },
            excluir = { json -> aoExcluirArquivosDoAparelho?.invoke(json) },
            abrirVisor = { uris, indice ->
                aoForcarEncerrarArrasteDeImagemDoComponente?.invoke()
                mostrarVisorDasImagens(uris, indice)
            },
            abrirVideos = { uris, indice ->
                aoForcarEncerrarArrasteDeImagemDoComponente?.invoke()
                aoAbrirVideosDaGradeAoLado?.invoke(uris, indice) ?: uris.getOrNull(indice)?.let { aoAbrirVideoNoPlayer?.invoke(it) }
            },
            mostrarMenuDoVideo = { uri, reproduzirAqui -> aoMostrarMenuDoVideoDaGrade?.invoke(uri, reproduzirAqui) },
            iniciarArraste = { uri, x, y, miniatura ->
                aoIniciarArrasteComMiniatura?.invoke(uri, x, y, miniatura)
            },
            aoTocar = { aoFocar() },
        )
    }
    private var aoIniciarArrasteComMiniatura: ((String, Float, Float, Bitmap?) -> Unit)? = null
    private var aoAbrirVisorExternoNaGaveta: ((List<String>, Int) -> Unit)? = null
    private var tituloDaPagina = ""
    private var htmlDaPagina = ""
    private var idOrigemDaBiblioteca: String? = null
    private var larguraPendentePx: Int? = null
    private var alturaPendentePx: Int? = null
    private var aoPersistirDimensoes: ((id: String, larguraPx: Int, alturaPx: Int) -> Unit)? = null
    private var gerandoPdf = false
    private val gravacaoDePdf = GravacaoDePdfDaWebViewDaColecaoFlutuanteNoArmazenamentoDoAplicativo(activity)
    private var cargaDaPaginaPronta = false
    private var aoTerminarCargaDaPagina: (() -> Unit)? = null
    private var geracaoDaCaptura = 0
    private var focoLiberadoParaColarNaPagina = false
    private var webEraFocusableAntesDoColar = true
    private var webEraFocusableInTouchModeAntesDoColar = true
    /** Durante arraste → página: não conta como hit da coleção (a página sob o painel recebe o soltar). */
    private var ignorarHitTestDuranteArrasteParaPagina = false

    private enum class Forma { OCULTA, BOLHA, ABERTA }

    private var aoEnviarMensagemAoChat: ((String) -> Unit)? = null
    private var aoLerUrlDaAbaAtiva: (() -> String)? = null
    private var aoLerTituloDaAbaAtiva: (() -> String)? = null
    private var aoLerTextoSelecionadoDaAba: (((String) -> Unit) -> Unit)? = null
    private var aoAbrirNaAba: ((String) -> Unit)? = null
    /** idOuNome + callback com o relato ao terminar. */
    private var aoRodarRoteiro: ((String, (String) -> Unit) -> Unit)? = null
    private var aoCopiarTexto: ((String) -> Unit)? = null
    private var aoPedirArquivoDoAparelho: (
        (
            ValueCallback<Array<Uri>>,
            WebChromeClient.FileChooserParams,
        ) -> Boolean
    )? = null
    private var callbackDeArquivoPendente: ValueCallback<Array<Uri>>? = null
    private var aoEscolherPastaDoAparelho: (() -> Unit)? = null
    private var aoListarPastaDoAparelho: ((String) -> String)? = null
    private var aoExcluirArquivosDoAparelho: ((String) -> String)? = null
    private var uriDaPastaEntregue: String? = null
    private var geracaoDaAlimentacaoDoVisualizador = 0
    private var aoAbrirImagemNaAba: ((String) -> String)? = null
    private var aoAbrirImagemNoVisor: ((String) -> String)? = null
    private var aoAbrirImagemNoEditor: ((String) -> String)? = null
    private var aoEnviarImagemParaCamadaDoEditor: ((String) -> String)? = null
    private var aoIniciarArrasteDeImagemParaOEditor: ((String, Float, Float) -> Unit)? = null
    private var aoForcarEncerrarArrasteDeImagemDoComponente: (() -> Unit)? = null
    private var aoAtualizarArrasteDeImagemParaOEditor: ((Float, Float) -> Unit)? = null
    private var aoSoltarArrasteDeImagemParaOEditor: ((String, Float, Float) -> String)? = null
    private var aoGuardarImagem: ((String) -> String)? = null
    private var aoCopiarImagem: ((String) -> String)? = null
    private var aoAbrirVideoNoPlayer: ((String) -> String)? = null
    private var servicoDeGeracaoDeImagemViaMcp: ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers? = null
    private var aoEscolherImagemDeReferenciaDaGeracao: ((String) -> Unit)? = null
    private var aoAbrirVideosAoLado: ((itensJson: String, indice: Int) -> Unit)? = null
    private var aoAbrirVideosDaGradeAoLado: ((uris: List<String>, indice: Int) -> Unit)? = null
    private var aoMostrarMenuDoVideoDaGrade: ((uri: String, reproduzirAqui: () -> Unit) -> Unit)? = null
    private var aoTerminarGeracaoDeImagemViaMcp: (() -> Unit)? = null
    private var aoSoltarItemDaBibliotecaParaReferenciaMcp: ((DragEvent, String) -> Boolean)? = null
    private inner class PonteJavascriptDaPaginaDaColecao {
        @JavascriptInterface
        fun listarMcpsGeracao(): String =
            servicoDeGeracaoDeImagemViaMcp?.listarMcpsJson()
                ?: com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
                    .erro("Geração ainda não está pronta.")

        @JavascriptInterface
        fun listarMcpsGeracaoVideo(): String =
            servicoDeGeracaoDeImagemViaMcp?.listarMcpsDeVideoJson()
                ?: com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
                    .erro("Geração ainda não está pronta.")

        /** Chamada na thread da ponte do WebView (fora da principal): a rede bloqueante é segura aqui. */
        @JavascriptInterface
        fun listarModelosGeracao(mcpId: String?): String {
            val servico = servicoDeGeracaoDeImagemViaMcp
                ?: return com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
                    .erro("Geração ainda não está pronta.")
            return try {
                servico.listarModelosJson(mcpId.orEmpty())
            } catch (e: Exception) {
                com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
                    .erro(e.message ?: "Falha ao listar modelos.")
            }
        }

        @JavascriptInterface
        fun listarModelosGeracaoVideo(mcpId: String?): String {
            val servico = servicoDeGeracaoDeImagemViaMcp
                ?: return com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
                    .erro("Geração ainda não está pronta.")
            return try {
                servico.listarModelosDeVideoJson(mcpId.orEmpty())
            } catch (e: Exception) {
                com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
                    .erro(e.message ?: "Falha ao listar modelos de vídeo.")
            }
        }

        /** O componente de vídeo avisa MCPs/modelo atuais; devolve `{ok, aceitaVideo, dica}`. */
        @JavascriptInterface
        fun informarModeloGeracaoVideo(mcps: String?, modelo: String?): String {
            val servico = servicoDeGeracaoDeImagemViaMcp
                ?: return com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
                    .erro("Geração ainda não está pronta.")
            return servico.definirModeloDeVideoAtual(mcps.orEmpty(), modelo.orEmpty())
        }

        @JavascriptInterface
        fun escolherImagemGeracao(origem: String?): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val escolha = origem?.trim()?.lowercase().orEmpty()
            if (escolha != "galeria" && escolha != "biblioteca") {
                return respostas.erro("Origem inválida: use galeria ou biblioteca.")
            }
            val pedir = aoEscolherImagemDeReferenciaDaGeracao
                ?: return respostas.erro("Escolha de imagem ainda não está pronta.")
            activity.runOnUiThread {
                if (encerrada) return@runOnUiThread
                aoFocar()
                pedir(escolha)
            }
            return """{"ok":true,"aguardando":true}"""
        }

        @JavascriptInterface
        fun limparImagemGeracao(): String {
            servicoDeGeracaoDeImagemViaMcp?.limparReferencia(tipoDasReferencias())
            return com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp.okLimpar()
        }

        @JavascriptInterface
        fun limparImagemGeracaoIndice(indice: String?): String {
            val i = indice?.trim()?.toIntOrNull()
                ?: return com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
                    .erro("Índice de referência inválido.")
            val servico = servicoDeGeracaoDeImagemViaMcp
                ?: return com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
                    .erro("Geração ainda não está pronta.")
            return servico.limparReferenciaNoIndice(tipoDasReferencias(), i)
        }

        /**
         * Toque numa referência: imagem abre no visor do Visualizador com os bytes exatos que serão enviados
         * (desliza entre as refs de imagem); vídeo toca no player. Devolve `{ok, uris}` (uma por slot, "" se falhou)
         * para o HTML pôr a moldura na miniatura que está no visor.
         */
        @JavascriptInterface
        fun abrirReferenciaNoVisor(indice: String?): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp ?: return respostas.erro("Geração ainda não está pronta.")
            val idx = indice?.trim()?.toIntOrNull() ?: return respostas.erro("Índice de referência inválido.")
            val arquivos = try {
                servico.arquivosDasReferencias(
                    tipoDasReferencias(),
                    java.io.File(activity.cacheDir, com.chromebrothers.navegador.regras.PreviaDaReferenciaDaGeracaoMcpNoVisor.PASTA),
                )
            } catch (e: Exception) {
                return respostas.erro(e.message ?: "Não consegui preparar a referência.")
            }
            val alvo = arquivos.getOrNull(idx) ?: return respostas.erro("Não consegui abrir essa referência.")
            val (ehVideo, uri) = alvo
            if (ehVideo) {
                aoAbrirVideoNoPlayer?.invoke(uri)
            } else {
                val imagens = arquivos.filterNotNull().filter { !it.first }.map { it.second }
                val posicao = imagens.indexOf(uri).coerceAtLeast(0)
                activity.runOnUiThread {
                    if (encerrada) return@runOnUiThread
                    aoForcarEncerrarArrasteDeImagemDoComponente?.invoke()
                    mostrarVisorDasImagens(imagens, posicao)
                }
            }
            val uris = org.json.JSONArray().apply { arquivos.forEach { put(it?.second.orEmpty()) } }
            return org.json.JSONObject().put("ok", true).put("uris", uris).toString()
        }

        /**
         * "×" da referência: confirma no modal padrão antes de tirar. Se confirmar, a lista nova chega
         * por `__chromebrothersImagemGeracao`; devolve `{ok:true}` na hora.
         */
        @JavascriptInterface
        fun pedirParaRemoverReferencia(indice: String?): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp ?: return respostas.erro("Geração ainda não está pronta.")
            val i = indice?.trim()?.toIntOrNull() ?: return respostas.erro("Índice de referência inválido.")
            val tipo = tipoDasReferencias()
            val textos = com.chromebrothers.navegador.regras.ConfirmacaoDeTirarReferenciaDaGeracaoMcp
            activity.runOnUiThread {
                if (encerrada) return@runOnUiThread
                CaixaDeConfirmacaoDoNavegador.mostrar(
                    activity = activity,
                    titulo = textos.tituloDeUma(),
                    mensagem = textos.mensagemDeUma(i),
                    textoConfirmar = textos.CONFIRMAR,
                ) {
                    entregarImagemDeReferenciaDaGeracao(servico.limparReferenciaNoIndice(tipo, i))
                }
            }
            return """{"ok":true}"""
        }

        /** Botão "Limpar": com referências, confirma antes de tirar todas. */
        @JavascriptInterface
        fun pedirParaLimparReferencias(): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp ?: return respostas.erro("Geração ainda não está pronta.")
            val tipo = tipoDasReferencias()
            val quantidade = servico.quantidadeDeReferencias(tipo)
            if (quantidade == 0) return servico.jsonDasReferencias(tipo)
            val textos = com.chromebrothers.navegador.regras.ConfirmacaoDeTirarReferenciaDaGeracaoMcp
            activity.runOnUiThread {
                if (encerrada) return@runOnUiThread
                CaixaDeConfirmacaoDoNavegador.mostrar(
                    activity = activity,
                    titulo = textos.tituloDeTodas(quantidade),
                    mensagem = textos.mensagemDeTodas(quantidade),
                    textoConfirmar = textos.CONFIRMAR,
                ) {
                    servico.limparReferencia(tipo)
                    entregarImagemDeReferenciaDaGeracao(servico.jsonDasReferencias(tipo))
                }
            }
            return """{"ok":true}"""
        }

        /** Referências guardadas para este gerador: o HTML pinta ao abrir, para nada ficar escondido. */
        @JavascriptInterface
        fun listarReferenciasGeracao(): String {
            val servico = servicoDeGeracaoDeImagemViaMcp
                ?: return com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
                    .erro("Geração ainda não está pronta.")
            return servico.jsonDasReferencias(tipoDasReferencias())
        }

        @JavascriptInterface
        fun gerarImagensMcp(pedidoJson: String?): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp
                ?: return respostas.erro("Geração ainda não está pronta.")
            val bruto = pedidoJson.orEmpty()
            val lido = com.chromebrothers.navegador.regras.PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(bruto)
            if (lido is com.chromebrothers.navegador.regras.ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado) {
                return respostas.erro(lido.mensagem)
            }
            return try {
                servico.iniciarImagensPorCards(
                    pedidoJson = bruto,
                    aoCard = { json -> entregarGeracaoMcpAoHtml(json) },
                    aoTerminarCard = { activity.runOnUiThread { aoTerminarGeracaoDeImagemViaMcp?.invoke() } },
                )
            } catch (e: Exception) {
                respostas.erro(e.message ?: "Não foi possível iniciar a geração.")
            }
        }

        @JavascriptInterface
        fun listarCardsDeImagemMcp(): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp ?: return respostas.erro("Geração ainda não está pronta.")
            return try {
                servico.listarCardsDeImagemJson()
            } catch (e: Exception) {
                respostas.erro(e.message ?: "Falha ao listar as imagens pedidas.")
            }
        }

        @JavascriptInterface
        fun verificarCardDeImagemMcp(id: String?): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp ?: return respostas.erro("Geração ainda não está pronta.")
            val alvo = id.orEmpty().trim()
            if (alvo.isEmpty()) return respostas.erro("Card sem id.")
            Thread {
                try {
                    servico.verificarCardDeImagem(alvo) { json -> entregarGeracaoMcpAoHtml(json) }
                } catch (_: Exception) {
                }
                activity.runOnUiThread { aoTerminarGeracaoDeImagemViaMcp?.invoke() }
            }.start()
            return respostas.okAndamento()
        }

        @JavascriptInterface
        fun removerCardDeImagemMcp(id: String?): String {
            val servico = servicoDeGeracaoDeImagemViaMcp
                ?: return com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp.erro("Geração ainda não está pronta.")
            return servico.removerCardDeImagem(id.orEmpty().trim())
        }

        @JavascriptInterface
        fun gerarVideosMcp(pedidoJson: String?): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp
                ?: return respostas.erro("Geração ainda não está pronta.")
            val bruto = pedidoJson.orEmpty()
            val lido = com.chromebrothers.navegador.regras.PedidoDeGeracaoDeVideoViaMcpNaColecao.ler(bruto)
            if (lido is com.chromebrothers.navegador.regras.ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Recusado) {
                return respostas.erro(lido.mensagem)
            }
            return try {
                servico.iniciarVideosPorCards(
                    pedidoJson = bruto,
                    aoCard = { json -> entregarGeracaoMcpAoHtml(json) },
                    aoTerminarCard = { activity.runOnUiThread { aoTerminarGeracaoDeImagemViaMcp?.invoke() } },
                )
            } catch (e: Exception) {
                respostas.erro(e.message ?: "Não foi possível iniciar a geração de vídeo.")
            }
        }

        @JavascriptInterface
        fun listarCardsDeVideoMcp(): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp ?: return respostas.erro("Geração ainda não está pronta.")
            return try {
                servico.listarCardsDeVideoJson()
            } catch (e: Exception) {
                respostas.erro(e.message ?: "Falha ao listar os vídeos pedidos.")
            }
        }

        @JavascriptInterface
        fun verificarCardDeVideoMcp(id: String?): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp ?: return respostas.erro("Geração ainda não está pronta.")
            val alvo = id.orEmpty().trim()
            if (alvo.isEmpty()) return respostas.erro("Card sem id.")
            Thread {
                try {
                    servico.verificarCardDeVideo(alvo) { json -> entregarGeracaoMcpAoHtml(json) }
                } catch (_: Exception) {
                }
                activity.runOnUiThread { aoTerminarGeracaoDeImagemViaMcp?.invoke() }
            }.start()
            return respostas.okAndamento()
        }

        @JavascriptInterface
        fun removerCardDeVideoMcp(id: String?): String {
            val servico = servicoDeGeracaoDeImagemViaMcp
                ?: return com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp.erro("Geração ainda não está pronta.")
            return servico.removerCardDeVideo(id.orEmpty().trim())
        }

        /** "Usar de novo" no card: recoloca as refs da receita e devolve o preenchimento do formulário. */
        @JavascriptInterface
        fun usarReceitaDeNovo(idDaReceita: String?): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp ?: return respostas.erro("Geração ainda não está pronta.")
            val id = idDaReceita.orEmpty().trim()
            if (id.isEmpty()) return respostas.erro("Este pedido é de antes das receitas.")
            return try {
                servico.prepararReceitaParaPreencher(id)
            } catch (e: Exception) {
                respostas.erro(e.message ?: "Não consegui abrir a receita.")
            }
        }

        /** "Usar de novo" em qualquer card (com receita ou antigo, só com prompt/MCP/modelo). */
        @JavascriptInterface
        fun usarCardDeNovo(tipo: String?, idDoCard: String?): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp ?: return respostas.erro("Geração ainda não está pronta.")
            return try {
                servico.prepararPreenchimentoDoCard(tipo.orEmpty().trim().lowercase(), idDoCard.orEmpty().trim())
            } catch (e: Exception) {
                respostas.erro(e.message ?: "Não consegui preencher com este pedido.")
            }
        }

        /** Preenchimento pedido pelo ⓘ da gaveta, entregue uma vez quando o componente abre. */
        @JavascriptInterface
        fun receitaParaPreencher(tipo: String?): String =
            servicoDeGeracaoDeImagemViaMcp?.receitaParaPreencher(tipo.orEmpty()) ?: """{"ok":false}"""

        @JavascriptInterface
        fun repetirCardDeImagemMcp(id: String?): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp ?: return respostas.erro("Geração ainda não está pronta.")
            return try {
                servico.repetirCardDeImagem(
                    id.orEmpty().trim(),
                    aoCard = { json -> entregarGeracaoMcpAoHtml(json) },
                    aoTerminarCard = { activity.runOnUiThread { aoTerminarGeracaoDeImagemViaMcp?.invoke() } },
                )
            } catch (e: Exception) {
                respostas.erro(e.message ?: "Não foi possível tentar de novo.")
            }
        }

        @JavascriptInterface
        fun repetirCardDeVideoMcp(id: String?): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val servico = servicoDeGeracaoDeImagemViaMcp ?: return respostas.erro("Geração ainda não está pronta.")
            return try {
                servico.repetirCardDeVideo(
                    id.orEmpty().trim(),
                    aoCard = { json -> entregarGeracaoMcpAoHtml(json) },
                    aoTerminarCard = { activity.runOnUiThread { aoTerminarGeracaoDeImagemViaMcp?.invoke() } },
                )
            } catch (e: Exception) {
                respostas.erro(e.message ?: "Não foi possível tentar de novo.")
            }
        }

        @JavascriptInterface
        fun gerarPdf(nome: String?) {
            activity.runOnUiThread {
                pedirGeracaoDePdf(nome.orEmpty())
            }
        }

        @JavascriptInterface
        fun enviarAoChat(texto: String?) {
            activity.runOnUiThread {
                aoEnviarMensagemAoChat?.invoke(texto.orEmpty())
            }
        }

        @JavascriptInterface
        fun urlDaAbaAtiva(): String = lerNaUi(aoLerUrlDaAbaAtiva)

        @JavascriptInterface
        fun tituloDaAbaAtiva(): String = lerNaUi(aoLerTituloDaAbaAtiva)

        @JavascriptInterface
        fun textoSelecionado(): String {
            val latch = CountDownLatch(1)
            var resultado = ""
            activity.runOnUiThread {
                val leitor = aoLerTextoSelecionadoDaAba
                if (leitor == null) {
                    latch.countDown()
                } else {
                    leitor { texto ->
                        resultado = texto
                        latch.countDown()
                    }
                }
            }
            latch.await(3, TimeUnit.SECONDS)
            return resultado
        }

        @JavascriptInterface
        fun abrirNaAba(destino: String?) {
            val texto = destino?.trim().orEmpty()
            if (texto.isEmpty()) return
            activity.runOnUiThread {
                aoAbrirNaAba?.invoke(texto)
            }
        }

        @JavascriptInterface
        fun rodarRoteiro(idOuNome: String?): String {
            val chave = idOuNome?.trim().orEmpty()
            if (chave.isEmpty()) {
                return """{"ok":false,"motivo":"faltou_id"}"""
            }
            if (chave.length > 80) {
                return """{"ok":false,"motivo":"id_longo"}"""
            }
            val rodar = aoRodarRoteiro
                ?: return """{"ok":false,"motivo":"ponte_ausente"}"""
            activity.runOnUiThread {
                if (encerrada || activity.isDestroyed) return@runOnUiThread
                Toast.makeText(activity, "Rodando roteiro…", Toast.LENGTH_SHORT).show()
                rodar(chave) { relato ->
                    if (encerrada || activity.isDestroyed) return@rodar
                    Toast.makeText(activity, relato.take(160), Toast.LENGTH_LONG).show()
                    entregarJsonDoFimDoRoteiro(relato)
                }
            }
            return """{"ok":true}"""
        }

        @JavascriptInterface
        fun copiar(texto: String?) {
            activity.runOnUiThread {
                aoCopiarTexto?.invoke(texto.orEmpty())
            }
        }

        @JavascriptInterface
        fun toast(mensagem: String?) {
            val aviso = mensagem?.trim().orEmpty().take(200)
            if (aviso.isEmpty()) return
            activity.runOnUiThread {
                Toast.makeText(activity, aviso, Toast.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun escolherPasta() {
            activity.runOnUiThread {
                aoFocar()
                aoEscolherPastaDoAparelho?.invoke()
            }
        }

        @JavascriptInterface
        fun listarPasta(uri: String?): String {
            val alvo = uri?.trim().orEmpty()
            if (alvo.isEmpty()) {
                return com.chromebrothers.navegador.regras.ListagemDePastaEscolhidaPeloComponenteDaColecao
                    .mensagemErro("Faltou a uri da pasta.")
            }
            // SAF não precisa da UI thread — listar na main travava a 1ª abertura da galeria.
            val latch = CountDownLatch(1)
            var resultado = com.chromebrothers.navegador.regras.ListagemDePastaEscolhidaPeloComponenteDaColecao
                .mensagemErro("Não deu para listar a pasta.")
            filaDeMiniaturas.execute {
                try {
                    resultado = aoListarPastaDoAparelho?.invoke(alvo) ?: resultado
                } finally {
                    latch.countDown()
                }
            }
            latch.await(20, TimeUnit.SECONDS)
            return resultado
        }

        @JavascriptInterface
        fun excluirArquivos(urisJson: String?): String {
            val latch = CountDownLatch(1)
            var resultado = com.chromebrothers.navegador.regras.ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador
                .mensagemSemUris()
            filaDeMiniaturas.execute {
                try {
                    resultado = aoExcluirArquivosDoAparelho?.invoke(urisJson.orEmpty()) ?: resultado
                } finally {
                    latch.countDown()
                }
            }
            latch.await(30, TimeUnit.SECONDS)
            return resultado
        }

        @JavascriptInterface
        fun abrirImagemNaAba(uri: String?): String = chamarAberturaDeMidia(uri, aoAbrirImagemNaAba)

        @JavascriptInterface
        fun abrirImagemNoVisor(uri: String?): String = chamarAberturaDeMidia(uri, aoAbrirImagemNoVisor)

        @JavascriptInterface
        fun abrirImagemNoEditor(uri: String?): String = chamarAberturaDeMidia(uri, aoAbrirImagemNoEditor)

        @JavascriptInterface
        fun enviarImagemParaCamadaDoEditor(uri: String?): String =
            chamarAberturaDeMidia(uri, aoEnviarImagemParaCamadaDoEditor)

        /**
         * Recebe `data:image/...;base64,...` do canvas do `<video>` pausado,
         * grava JPEG/PNG no cache e devolve `file://` para o lift-and-drag.
         */
        @JavascriptInterface
        fun gravarQuadroDoVideoParaArraste(dataUrl: String?): String {
            val bruto = dataUrl.orEmpty()
            val bytes = com.chromebrothers.navegador.regras.ExtracaoDeBytesDeDataUrlDeImagemDaColecao.bytes(bruto)
                ?: return ""
            if (bytes.isEmpty()) return ""
            return try {
                val pasta = java.io.File(activity.cacheDir, "quadros-de-video-da-colecao").apply { mkdirs() }
                val ext = com.chromebrothers.navegador.regras.ExtracaoDeBytesDeDataUrlDeImagemDaColecao.extensao(bruto)
                val arquivo = java.io.File(pasta, "quadro-${System.currentTimeMillis()}.$ext")
                arquivo.writeBytes(bytes)
                arquivo.toURI().toString()
            } catch (_: Exception) {
                ""
            }
        }

        @JavascriptInterface
        fun iniciarArrasteDeImagemParaOEditor(uri: String?, x: String?, y: String?): String {
            val alvo = uri?.trim().orEmpty()
            if (alvo.isEmpty()) {
                return com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Faltou a uri da mídia.")
            }
            if (!com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao.esquemaAceito(alvo)) {
                return com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Uri não suportada.")
            }
            val cx = x?.trim()?.toFloatOrNull() ?: 0f
            val cy = y?.trim()?.toFloatOrNull() ?: 0f
            return try {
                activity.runOnUiThread {
                    val tela = clienteDaWebViewParaTela(cx, cy)
                    destacarCartaDoVisualizador(alvo)
                    aoIniciarArrasteDeImagemParaOEditor?.invoke(alvo, tela[0], tela[1])
                    aoAtualizarArrasteDeImagemParaOEditor?.invoke(tela[0], tela[1])
                }
                com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemOk("camada", "arraste")
            } catch (_: Exception) {
                com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Não deu para iniciar o arraste.")
            }
        }

        @JavascriptInterface
        fun atualizarArrasteDeImagemParaOEditor(x: String?, y: String?): String {
            val cx = x?.trim()?.toFloatOrNull()
            val cy = y?.trim()?.toFloatOrNull()
            if (cx == null || cy == null) {
                return com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Coordenadas inválidas.")
            }
            return try {
                activity.runOnUiThread {
                    val tela = clienteDaWebViewParaTela(cx, cy)
                    aoAtualizarArrasteDeImagemParaOEditor?.invoke(tela[0], tela[1])
                }
                com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemOk("camada", "arraste")
            } catch (_: Exception) {
                com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Não deu para atualizar o arraste.")
            }
        }

        @JavascriptInterface
        fun soltarArrasteDeImagemParaOEditor(uri: String?, x: String?, y: String?): String {
            val alvo = uri?.trim().orEmpty()
            if (alvo.isEmpty()) {
                return com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Faltou a uri da mídia.")
            }
            if (!com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao.esquemaAceito(alvo)) {
                return com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Uri não suportada.")
            }
            val cx = x?.trim()?.toFloatOrNull()
            val cy = y?.trim()?.toFloatOrNull()
            if (cx == null || cy == null) {
                return com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Coordenadas inválidas.")
            }
            return try {
                val latch = java.util.concurrent.CountDownLatch(1)
                var sx = 0f
                var sy = 0f
                activity.runOnUiThread {
                    try {
                        val tela = clienteDaWebViewParaTela(cx, cy)
                        sx = tela[0]
                        sy = tela[1]
                    } finally {
                        latch.countDown()
                    }
                }
                latch.await(2, java.util.concurrent.TimeUnit.SECONDS)
                aoSoltarArrasteDeImagemParaOEditor?.invoke(alvo, sx, sy)
                    ?: com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                        .mensagemErro("Ação ainda não está pronta.")
            } catch (_: Exception) {
                com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Não deu para soltar o arraste.")
            }
        }

        @JavascriptInterface
        fun guardarImagem(uri: String?): String = chamarAberturaDeMidia(uri, aoGuardarImagem)

        @JavascriptInterface
        fun copiarImagem(uri: String?): String = chamarAberturaDeMidia(uri, aoCopiarImagem)

        @JavascriptInterface
        fun abrirVideoNoPlayer(uri: String?): String = chamarAberturaDeMidia(uri, aoAbrirVideoNoPlayer)

        /** [itensJson] = `[{fileUrl, previewDataUrl}]` na ordem da grade; abre o player embutido ao lado. */
        @JavascriptInterface
        fun abrirVideosAoLado(itensJson: String?, indice: String?): String {
            val respostas = com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
            val abrir = aoAbrirVideosAoLado ?: return respostas.erro("Player ao lado ainda não está pronto.")
            abrir(itensJson.orEmpty(), indice?.trim()?.toIntOrNull() ?: 0)
            return """{"ok":true}"""
        }

        /** O Visualizador desenha a grade em nativo; o HTML não deve montar a sua. */
        @JavascriptInterface
        fun gradeNativaAtiva(): Boolean = ehVisualizadorDeMidiaAtual()

        @JavascriptInterface
        fun abrirVisorDaMidiaNoVisualizador(uri: String?, listaJson: String?, indice: String?): String {
            val alvo = uri?.trim().orEmpty()
            if (alvo.isEmpty()) {
                return com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Faltou a uri da mídia.")
            }
            val idx = indice?.trim()?.toIntOrNull() ?: 0
            return try {
                activity.runOnUiThread {
                    abrirVisorDoVisualizadorDeMidia(alvo, listaJson.orEmpty(), idx)
                }
                com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemOk("visor", "aberto")
            } catch (_: Exception) {
                com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Não deu para abrir o visor.")
            }
        }

        /**
         * [urisJson] = `["file://…", …]` na ordem da grade do componente (ex.: Geração MCP — imagem).
         * Abre o mesmo visor do Visualizador de mídia (deslize + moldura na miniatura via `__chromebrothersImagemNoVisor`).
         */
        @JavascriptInterface
        fun abrirImagensNoVisorDaColecao(urisJson: String?, indice: String?): String {
            val uris = runCatching {
                val arr = org.json.JSONArray(urisJson.orEmpty())
                (0 until arr.length()).map { arr.optString(it).trim() }.filter { it.isNotEmpty() }
            }.getOrDefault(emptyList())
            if (uris.isEmpty()) {
                return com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Faltou a uri da mídia.")
            }
            val idx = (indice?.trim()?.toIntOrNull() ?: 0).coerceIn(0, uris.lastIndex)
            activity.runOnUiThread {
                aoForcarEncerrarArrasteDeImagemDoComponente?.invoke()
                mostrarVisorDasImagens(uris, idx)
            }
            return com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                .mensagemOk("visor", "aberto")
        }

        private fun chamarAberturaDeMidia(uri: String?, acao: ((String) -> String)?): String {
            val alvo = uri?.trim().orEmpty()
            if (alvo.isEmpty()) {
                return com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Faltou a uri da mídia.")
            }
            if (!com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao.esquemaAceito(alvo)) {
                return com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Uri não suportada.")
            }
            return try {
                acao?.invoke(alvo)
                    ?: com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                        .mensagemErro("Ação ainda não está pronta.")
            } catch (_: Exception) {
                com.chromebrothers.navegador.regras.AberturaDeMidiaLocalDoComponenteDaColecao
                    .mensagemErro("Não deu para abrir a mídia.")
            }
        }

        private fun lerNaUi(ler: (() -> String)?): String {
            val latch = CountDownLatch(1)
            var resultado = ""
            activity.runOnUiThread {
                resultado = ler?.invoke().orEmpty()
                latch.countDown()
            }
            latch.await(2, TimeUnit.SECONDS)
            return resultado
        }
    }

    fun ligarEnvioAoChat(enviar: (String) -> Unit) {
        aoEnviarMensagemAoChat = enviar
    }

    fun ligarUrlDaAbaAtiva(ler: () -> String) {
        aoLerUrlDaAbaAtiva = ler
    }

    fun ligarTituloDaAbaAtiva(ler: () -> String) {
        aoLerTituloDaAbaAtiva = ler
    }

    fun ligarTextoSelecionadoDaAba(ler: ((String) -> Unit) -> Unit) {
        aoLerTextoSelecionadoDaAba = ler
    }

    fun ligarAbrirNaAba(abrir: (String) -> Unit) {
        aoAbrirNaAba = abrir
    }

    fun ligarRodarRoteiro(rodar: (idOuNome: String, aoFim: (String) -> Unit) -> Unit) {
        aoRodarRoteiro = rodar
    }

    fun ligarCopiarTexto(copiar: (String) -> Unit) {
        aoCopiarTexto = copiar
    }

    fun entregarJsonDoFimDoRoteiro(relato: String) {
        if (encerrada) return
        val ok = !relato.contains("Falha", ignoreCase = true) &&
            !relato.contains("Timeout", ignoreCase = true) &&
            !relato.contains("Não achei", ignoreCase = true) &&
            !relato.contains("cancelado", ignoreCase = true)
        val json = org.json.JSONObject()
            .put("ok", ok)
            .put("mensagem", relato.take(500))
            .toString()
        val literal = org.json.JSONObject.quote(json)
        webDaPagina.evaluateJavascript(
            "(function(){try{var j=JSON.parse($literal);" +
                "if(typeof window.chromebrothersAoTerminarRoteiro==='function')" +
                "{window.chromebrothersAoTerminarRoteiro(j);}" +
                "}catch(e){}})();",
            null,
        )
    }

    fun ligarEscolherPastaDoAparelho(escolher: () -> Unit) {
        aoEscolherPastaDoAparelho = escolher
    }

    fun ligarListarPastaDoAparelho(listar: (String) -> String) {
        aoListarPastaDoAparelho = listar
    }

    fun ligarExcluirArquivosDoAparelho(excluir: (String) -> String) {
        aoExcluirArquivosDoAparelho = excluir
    }

    fun ligarAbrirImagemNaAba(abrir: (String) -> String) {
        aoAbrirImagemNaAba = abrir
    }

    fun ligarAbrirImagemNoVisor(abrir: (String) -> String) {
        aoAbrirImagemNoVisor = abrir
    }

    fun ligarAbrirImagemNoEditor(abrir: (String) -> String) {
        aoAbrirImagemNoEditor = abrir
    }

    fun ligarEnviarImagemParaCamadaDoEditor(enviar: (String) -> String) {
        aoEnviarImagemParaCamadaDoEditor = enviar
    }

    fun ligarForcarEncerrarArrasteDeImagemDoComponente(encerrar: () -> Unit) {
        aoForcarEncerrarArrasteDeImagemDoComponente = encerrar
    }

    fun ligarIniciarArrasteComMiniatura(iniciar: (String, Float, Float, Bitmap?) -> Unit) {
        aoIniciarArrasteComMiniatura = iniciar
    }

    fun ligarAbrirVisorExternoNaGaveta(abrir: (List<String>, Int) -> Unit) {
        aoAbrirVisorExternoNaGaveta = abrir
    }

    /** Na gaveta a imagem abre por cima da página; na janela flutuante, dentro do componente. */
    private fun mostrarVisorDasImagens(uris: List<String>, indice: Int) {
        val fora = com.chromebrothers.navegador.regras.RegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat
            .deveAbrirForaDaGaveta(ancoradaNaGaveta)
        if (fora) aoAbrirVisorExternoNaGaveta?.invoke(uris, indice)
        else visorDoVisualizadorDeMidia.mostrar(uris, indice)
    }

    /** Visualizador de mídia usa a grade nativa por cima da WebView; outros componentes, só a WebView. */
    private fun sincronizarGradeNativaDoVisualizador() {
        if (ehVisualizadorDeMidiaAtual()) {
            gradeNativaDoVisualizador.mostrar()
            webDaPagina.visibility = View.INVISIBLE
        } else if (gradeNativaCriada) {
            gradeNativaDoVisualizador.esconder()
            webDaPagina.visibility = View.VISIBLE
        }
    }

    fun ligarIniciarArrasteDeImagemParaOEditor(iniciar: (String, Float, Float) -> Unit) {
        aoIniciarArrasteDeImagemParaOEditor = iniciar
    }

    fun ligarAtualizarArrasteDeImagemParaOEditor(atualizar: (Float, Float) -> Unit) {
        aoAtualizarArrasteDeImagemParaOEditor = atualizar
    }

    fun ligarSoltarArrasteDeImagemParaOEditor(soltar: (String, Float, Float) -> String) {
        aoSoltarArrasteDeImagemParaOEditor = soltar
    }

    fun ligarGuardarImagem(guardar: (String) -> String) {
        aoGuardarImagem = guardar
    }

    fun ligarCopiarImagem(copiar: (String) -> String) {
        aoCopiarImagem = copiar
    }

    fun ligarAbrirVideoNoPlayer(abrir: (String) -> String) {
        aoAbrirVideoNoPlayer = abrir
    }

    fun ligarServicoDeGeracaoDeImagemViaMcp(servico: ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers) {
        servicoDeGeracaoDeImagemViaMcp = servico
    }

    /** [escolher] recebe `"galeria"` ou `"biblioteca"` e roda na thread principal. */
    fun ligarEscolherImagemDeReferenciaDaGeracao(escolher: (String) -> Unit) {
        aoEscolherImagemDeReferenciaDaGeracao = escolher
    }

    fun ligarAbrirVideosAoLado(abrir: (itensJson: String, indice: Int) -> Unit) {
        aoAbrirVideosAoLado = abrir
    }

    fun ligarVideosDaGradeDoVisualizador(
        abrirAoLado: (uris: List<String>, indice: Int) -> Unit,
        mostrarMenu: (uri: String, reproduzirAqui: () -> Unit) -> Unit,
    ) {
        aoAbrirVideosDaGradeAoLado = abrirAoLado
        aoMostrarMenuDoVideoDaGrade = mostrarMenu
    }

    /** Moldura do vídeo em exibição e fim do badge «new» no HTML; null limpa a moldura. Qualquer thread. */
    fun avisarVideoEmExibicaoNoVisorAoLado(uri: String?) {
        val argumento = uri?.let { org.json.JSONObject.quote(it) } ?: "null"
        executarNaPaginaDaColecao(
            "window.__chromebrothersVideoNoVisor && window.__chromebrothersVideoNoVisor($argumento)",
        )
    }

    fun ligarAoTerminarGeracaoDeImagemViaMcp(terminou: () -> Unit) {
        aoTerminarGeracaoDeImagemViaMcp = terminou
    }

    /** Notifica o HTML (`window.__chromebrothersImagemGeracao`) com o JSON da imagem escolhida. Qualquer thread. */
    fun entregarImagemDeReferenciaDaGeracao(json: String) {
        executarNaPaginaDaColecao("window.__chromebrothersImagemGeracao && window.__chromebrothersImagemGeracao(" +
            org.json.JSONObject.quote(json) + ")")
    }

    private fun entregarGeracaoMcpAoHtml(json: String) {
        executarNaPaginaDaColecao("window.__chromebrothersGeracaoMcp && window.__chromebrothersGeracaoMcp(" +
            org.json.JSONObject.quote(json) + ")")
    }

    /** Card atualizado fora do gerador (ex.: mídia movida para a Galeria); só chega ao gerador do mesmo tipo. */
    fun entregarCardAtualizadoAoGerador(tipo: String, json: String) {
        if (encerrada || tipoDoGeradorMcpAtual() != tipo) return
        entregarGeracaoMcpAoHtml(json)
    }

    /** Componente gerador já aberto: pede que ele busque o preenchimento guardado ("Usar de novo" do ⓘ). */
    fun pedirParaPreencherReceita() {
        executarNaPaginaDaColecao("window.__chromebrothersPreencherReceita && window.__chromebrothersPreencherReceita()")
    }

    private fun executarNaPaginaDaColecao(script: String) {
        activity.runOnUiThread {
            if (encerrada || activity.isDestroyed) return@runOnUiThread
            webDaPagina.evaluateJavascript(script, null)
        }
    }

    /** O HTML espera `{progresso:"texto"}`; o serviço emite `{andamento:true, atual, total}`. */
    private fun progressoParaHtml(andamentoJson: String): String {
        return try {
            val o = org.json.JSONObject(andamentoJson)
            val texto = o.optString("texto", "")
            if (texto.isNotBlank()) {
                return org.json.JSONObject().put("ok", true).put("progresso", "Gerando… $texto").toString()
            }
            val atual = o.optInt("atual", 1)
            val total = o.optInt("total", 1)
            org.json.JSONObject().put("ok", true).put("progresso", "Gerando imagem $atual de $total…").toString()
        } catch (_: Exception) {
            """{"ok":true,"progresso":"Gerando…"}"""
        }
    }

    fun entregarJsonDaPastaEscolhida(json: String) {
        if (encerrada) return
        lembrarUriDaPastaEntregue(json)
        if (ehVisualizadorDeMidiaAtual()) {
            gradeNativaDoVisualizador.receberJson(json)
            return
        }
        val literal = org.json.JSONObject.quote(json)
        // Retry: o HTML às vezes ainda não registrou o callback no 1º tick.
        webDaPagina.evaluateJavascript(
            """
            (function(){
              var j = null;
              try { j = JSON.parse($literal); } catch (e) { return; }
              function tentar(restantes) {
                if (typeof window.chromebrothersAoEscolherPasta === 'function') {
                  window.chromebrothersAoEscolherPasta(j);
                  return;
                }
                if (restantes <= 0) return;
                setTimeout(function(){ tentar(restantes - 1); }, 120);
              }
              tentar(40);
            })();
            """.trimIndent(),
            null,
        )
    }

    fun deveReceberAtualizacaoDaPastaVirtualDeMidia(): Boolean {
        if (encerrada) return false
        if (!com.chromebrothers.navegador.regras.PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
                .ehVisualizadorDeMidiaPeloNome(tituloDaPagina)
        ) {
            return false
        }
        val uri = uriDaPastaEntregue
        return uri == null ||
            com.chromebrothers.navegador.regras.PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
                .ehUriVirtual(uri)
    }

    private fun lembrarUriDaPastaEntregue(json: String) {
        try {
            val o = org.json.JSONObject(json)
            if (!o.optBoolean("ok", false)) return
            uriDaPastaEntregue = o.optString("uri").ifBlank { null }
        } catch (_: Exception) {
        }
    }

    /**
     * Ligado pela Activity: recebe o DROP de um item da gaveta da biblioteca quando esta janela
     * mostra um gerador MCP. Parâmetros: evento, tipo do componente ("imagem"/"video").
     */
    fun ligarSoltarItemDaBibliotecaParaReferenciaMcp(soltar: (DragEvent, String) -> Boolean) {
        aoSoltarItemDaBibliotecaParaReferenciaMcp = soltar
    }

    /** "imagem" | "video" quando esta janela mostra um gerador MCP; vazio caso contrário. */
    fun tipoDoGeradorMcpAberto(): String = tipoDoGeradorMcpAtual()

    /** Lista de referências do serviço que pertence a esta janela ("imagem" quando não é gerador). */
    fun tipoDasReferencias(): String =
        tipoDoGeradorMcpAtual().ifBlank {
            com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaReferenciaMcp.COMPONENTE_IMAGEM
        }

    private fun tipoDoGeradorMcpAtual(): String =
        com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaReferenciaMcp
            .tipoDoComponentePeloId(idOrigemDaBiblioteca)

    /**
     * Só participa de arrastes vindos da gaveta (localState da biblioteca ou clip de imagem/vídeo)
     * e apenas com o gerador MCP aberto; qualquer outro caso devolve false e o WebView segue o fluxo normal.
     */
    private val ouvinteDeArrasteDaBibliotecaParaReferenciaMcp = View.OnDragListener { _, evento ->
        val tipo = tipoDoGeradorMcpAtual()
        val soltar = aoSoltarItemDaBibliotecaParaReferenciaMcp
        if (encerrada || tipo.isEmpty() || soltar == null) return@OnDragListener false
        val descricao = evento.clipDescription
        val ehDaBiblioteca = evento.localState is EstadoDoArrasteDaBiblioteca ||
            descricao?.hasMimeType("image/*") == true ||
            descricao?.hasMimeType("video/*") == true
        if (!ehDaBiblioteca) return@OnDragListener false
        when (evento.action) {
            DragEvent.ACTION_DRAG_STARTED,
            DragEvent.ACTION_DRAG_ENTERED,
            DragEvent.ACTION_DRAG_LOCATION,
            DragEvent.ACTION_DRAG_EXITED,
            -> true
            DragEvent.ACTION_DROP -> {
                aoFocar()
                soltar(evento, tipo)
            }
            else -> false
        }
    }

    fun ligar() {
        painel.setOnDragListener(ouvinteDeArrasteDaBibliotecaParaReferenciaMcp)
        webDaPagina.setOnDragListener(ouvinteDeArrasteDaBibliotecaParaReferenciaMcp)
        val densidade = selo.resources.displayMetrics.density
        selo.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#1B1D24"))
            setStroke((1 * densidade).toInt(), Color.parseColor("#2E323D"))
        }
        selo.setTextColor(Color.parseColor("#F5C518"))
        selo.outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
        selo.clipToOutline = true
        painel.findViewById<View>(R.id.botao_ancorar_colecao_na_gaveta_do_chat).setOnClickListener {
            aoFocar()
            aoAncorarNaGaveta?.invoke()
        }
        painel.findViewById<View>(R.id.botao_minimizar_colecao_flutuante).setOnClickListener { botao ->
            aoFocar()
            minimizar(origem = botao)
        }
        painel.findViewById<View>(R.id.botao_fechar_colecao_flutuante).setOnClickListener {
            aoFocar()
            fechar()
        }
        painel.findViewById<View>(R.id.botao_limpar_colecao_flutuante).setOnClickListener { limpar() }
        painel.findViewById<View>(R.id.botao_excluir_marcados_da_colecao).setOnClickListener { excluirMarcados() }
        painel.findViewById<View>(R.id.botao_marcar_todos_da_colecao).setOnClickListener { marcarTodos() }
        painel.findViewById<View>(R.id.botao_baixar_marcados_da_colecao).setOnClickListener {
            baixar(itens.filter { it.endereco in marcados })
        }
        botaoModoMidia.visibility = View.GONE
        botaoModoPagina.visibility = View.GONE
        conteudoMidia.visibility = View.GONE
        conteudoPagina.visibility = View.VISIBLE
        atualizarBotaoDeGuardar()
        prepararWebDaPagina()
        prepararBolha()
        painel.outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
        painel.clipToOutline = true
        prepararArraste(painel.findViewById(R.id.alca_para_arrastar_a_colecao_flutuante))
        prepararTamanho(painel.findViewById(R.id.alca_de_redimensionar_a_colecao_flutuante))
        aplicarModoNaInterface()
    }

    fun limparVinculoBiblioteca() {
        idOrigemDaBiblioteca = null
    }

    fun idOrigemDaBibliotecaAtual(): String? = idOrigemDaBiblioteca

    fun bitmapVisivelDaWebView(): Bitmap? {
        val largura = webDaPagina.width
        val altura = webDaPagina.height
        if (largura <= 0 || altura <= 0) return null
        val bitmap = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
        webDaPagina.draw(Canvas(bitmap))
        return bitmap
    }

    fun quandoAPaginaPuderSerCapturada(aoPronto: () -> Unit) {
        val geracao = ++geracaoDaCaptura
        var entregue = false
        fun entregar() {
            if (entregue || geracao != geracaoDaCaptura) return
            entregue = true
            aoTerminarCargaDaPagina = null
            aoPronto()
        }
        fun tentarTamanho(restantes: Int) {
            if (geracao != geracaoDaCaptura || entregue) return
            if ((webDaPagina.width > 0 && webDaPagina.height > 0) || restantes <= 0) {
                entregar()
                return
            }
            webDaPagina.postDelayed({ tentarTamanho(restantes - 1) }, 80)
        }
        if (cargaDaPaginaPronta) {
            webDaPagina.post { tentarTamanho(15) }
        } else {
            aoTerminarCargaDaPagina = { webDaPagina.postDelayed({ tentarTamanho(15) }, 80) }
            webDaPagina.postDelayed({
                if (geracao == geracaoDaCaptura && !entregue) tentarTamanho(8)
            }, 1600)
        }
    }

    /** Título e HTML da página desta janela, ou null se não houver página. */
    fun tituloEHtml(): Pair<String, String>? =
        if (htmlDaPagina.isBlank()) null else tituloDaPagina to htmlDaPagina

    /**
     * [mostrarAgora]=false: carrega o HTML mas deixa a janela escondida (INVISIBLE, com layout,
     * como no warm-start) até [revelar]. Usado enquanto o chat ainda está respondendo.
     */
    fun publicarPagina(titulo: String, html: String, mostrarAgora: Boolean = true): String {
        aoFocar()
        tituloDaPagina = titulo
        htmlDaPagina = html
        carregarHtmlDaPagina()
        aplicarModoNaInterface()
        atualizarCabecalhoDaJanela(
            titulo = titulo,
            subtitulo = activity.getString(R.string.subtitulo_pagina_publicada_da_colecao_flutuante),
        )
        if (mostrarAgora || forma == Forma.ABERTA || ancoradaNaGaveta) {
            mostrar()
        } else {
            pedido++
            painel.animate().cancel()
            selo.animate().cancel()
            painel.visibility = View.INVISIBLE
            selo.visibility = View.GONE
            forma = Forma.OCULTA
        }
        return PublicacaoDaPaginaDaColecaoFlutuanteDoChat.mensagemPublicada(titulo)
    }

    fun carregarComponenteDaBiblioteca(item: ItemDaBibliotecaDeComponentesHtmlDaColecao): String {
        aoFocar()
        // Visualizador: carcaça antes do loadData; Fechar só esconde (warm-start sem bolha).
        if (com.chromebrothers.navegador.regras.PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
                .ehVisualizadorDeMidiaPeloNome(item.nome)
        ) {
            aplicarConteudoDoComponenteDaBiblioteca(item, carregarHtmlAgora = false)
            reservadoParaWarmStart = true
            prepararCarcacaVisivelSemAnimacao()
            carregarHtmlQuandoWebViewTiverTamanho()
            return "Abri o componente \"${item.nome}\" na coleção."
        }
        aplicarConteudoDoComponenteDaBiblioteca(item)
        mostrar()
        return "Abri o componente \"${item.nome}\" na coleção."
    }

    /**
     * Pré-carga o HTML sem mostrar a janela (warm-start). Fechar depois só esconde, sem bolha.
     */
    fun carregarComponenteEmWarmStart(item: ItemDaBibliotecaDeComponentesHtmlDaColecao) {
        reservadoParaWarmStart = true
        aplicarConteudoDoComponenteDaBiblioteca(item)
        pedido++
        painel.animate().cancel()
        selo.animate().cancel()
        // INVISIBLE (não GONE): WebView mantém layout/compositor — evita tela preta ao revelar.
        painel.visibility = View.INVISIBLE
        selo.visibility = View.GONE
        forma = Forma.OCULTA
        retomarWebViewDaColecao()
    }

    fun revelarSeEstiverOculta(): Boolean {
        if (!temConteudo()) return false
        if (ancoradaNaGaveta) {
            aoFocar()
            retomarWebViewDaColecao()
            return true
        }
        if (forma == Forma.ABERTA) {
            aoFocar()
            trazerParaFrente()
            retomarWebViewDaColecao()
            return true
        }
        aoFocar()
        // Visualizador/warm-start: só revelar — HTML/grade já estão na WebView.
        mostrar()
        return true
    }

    fun recolherSemBolhaMantendoConteudo() {
        if (!temConteudo()) {
            esconderTudoSemAnimacao()
            return
        }
        if (forma == Forma.ABERTA) {
            encolher(mostrarBolha = false)
            return
        }
        if (forma == Forma.BOLHA) {
            selo.visibility = View.GONE
            forma = Forma.OCULTA
        }
        pausarMidiaDaPaginaEmWarmStart()
    }

    fun estaVisivelNaColecao(): Boolean =
        !ancoradaNaGaveta && (forma == Forma.ABERTA || forma == Forma.BOLHA)

    fun estaAncoradaNaGaveta(): Boolean = ancoradaNaGaveta

    fun tituloParaAbaNaGaveta(): String =
        tituloDaPagina.trim().ifBlank {
            activity.getString(R.string.colecao_flutuante_do_chat)
        }

    fun ligarAncorarNaGaveta(acao: () -> Unit) {
        aoAncorarNaGaveta = acao
    }

    /** MOVENDO responde se está sobre a faixa de abas do chat; SOLTOU responde se deve fixar no chat. */
    private var aoArrastarPelaBarra: ((Float, Float, FaseDoArrasteDaJanelaPelaBarra) -> Boolean)? = null

    fun ligarArrastePelaBarra(acao: (Float, Float, FaseDoArrasteDaJanelaPelaBarra) -> Boolean) {
        aoArrastarPelaBarra = acao
    }

    /** Depois de sair do chat pelo arraste da aba: a janela aparece com a barra sob o dedo. */
    fun levarJanelaAoPontoDaTela(xNaTela: Float, yNaTela: Float) {
        painel.post {
            val pai = painel.parent as? View ?: return@post
            val origem = IntArray(2)
            pai.getLocationOnScreen(origem)
            val densidade = painel.resources.displayMetrics.density
            val largura = painel.width.takeIf { it > 0 }?.toFloat() ?: (420f * densidade)
            posicionar(xNaTela - origem[0] - largura / 2f, yNaTela - origem[1] - 20f * densidade)
        }
    }

    /**
     * Esconde flutuante/bolha e move [conteudoPagina] para o host da gaveta.
     * @return mensagem de erro ou vazio se ok.
     */
    fun ancorarConteudoNoHostDaGaveta(host: ViewGroup): String {
        if (ancoradaNaGaveta) return ""
        if (!temConteudo()) return "Publique uma página antes de ancorar."
        // Invalida o crescer() agendado por mostrar(): senão a moldura reaparece vazia depois de ancorar.
        pedido++
        painel.animate().cancel()
        selo.animate().cancel()
        val pai = conteudoPagina.parent as? ViewGroup
            ?: return "Não achei o conteúdo da página."
        paiOriginalDoConteudoPagina = pai
        indiceOriginalDoConteudoPagina = pai.indexOfChild(conteudoPagina)
        paramsOriginaisDoConteudoPagina = conteudoPagina.layoutParams
        pai.removeView(conteudoPagina)
        host.addView(
            conteudoPagina,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        conteudoPagina.visibility = View.VISIBLE
        painel.visibility = View.GONE
        selo.visibility = View.GONE
        forma = Forma.OCULTA
        ancoradaNaGaveta = true
        marcarGradeAguardandoLarguraEstavel()
        val atrasoMs = com.chromebrothers.navegador.regras.AnimacaoDaGavetaDoChatDoNavegador.DURACAO_MS +
            com.chromebrothers.navegador.regras.AnimacaoDaGavetaDoChatDoNavegador.ESPERA_ANTES_DO_CROSSFADE_MS +
            com.chromebrothers.navegador.regras.AnimacaoDaGavetaDoChatDoNavegador.DURACAO_DO_CROSSFADE_MS +
            40L
        // Enquanto a gaveta anima a largura muda a cada quadro: some e só aparece na largura final.
        conteudoPagina.animate().cancel()
        conteudoPagina.alpha = 0f
        host.post { avisarWebViewRedimensionada() }
        host.postDelayed({
            avisarWebViewRedimensionada()
            conteudoPagina.animate().alpha(1f).setDuration(160L).start()
        }, atrasoMs)
        return ""
    }

    fun consultarSeAPaginaQuerGestoHorizontal(xNaTela: Float, yNaTela: Float, responder: (Boolean) -> Unit) {
        val caixa = android.graphics.Rect()
        if (!webDaPagina.isShown || !webDaPagina.getGlobalVisibleRect(caixa) ||
            !caixa.contains(xNaTela.toInt(), yNaTela.toInt())
        ) {
            responder(false)
            return
        }
        val origem = IntArray(2)
        webDaPagina.getLocationOnScreen(origem)
        val densidade = webDaPagina.resources.displayMetrics.density
        val regra = com.chromebrothers.navegador.regras.GestoDeSwipeEntreAbasNoCorpoDaGavetaDoChat
        webDaPagina.evaluateJavascript(
            regra.scriptDaConsulta((xNaTela - origem[0]) / densidade, (yNaTela - origem[1]) / densidade),
        ) { bruto -> responder(regra.lerRespostaDaConsulta(bruto)) }
    }

    fun mostrarConteudoAncoradoNoHost(mostrar: Boolean) {
        if (!ancoradaNaGaveta) return
        conteudoPagina.visibility = if (mostrar) View.VISIBLE else View.GONE
    }

    /**
     * Devolve [conteudoPagina] à janela flutuante e (se [mostrarDepois]) mostra a janela aberta.
     */
    fun desancorarConteudoParaFlutuante(mostrarDepois: Boolean = true): String {
        if (!ancoradaNaGaveta) return ""
        val pai = paiOriginalDoConteudoPagina
            ?: return "Não achei o painel original."
        (conteudoPagina.parent as? ViewGroup)?.removeView(conteudoPagina)
        val params = paramsOriginaisDoConteudoPagina
            ?: LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
        val indice = indiceOriginalDoConteudoPagina.coerceIn(0, pai.childCount)
        pai.addView(conteudoPagina, indice, params)
        conteudoPagina.animate().cancel()
        conteudoPagina.alpha = 1f
        ancoradaNaGaveta = false
        paiOriginalDoConteudoPagina = null
        indiceOriginalDoConteudoPagina = -1
        paramsOriginaisDoConteudoPagina = null
        if (mostrarDepois) mostrar()
        return ""
    }

    fun estaReservadoParaWarmStart(): Boolean = reservadoParaWarmStart

    private fun aplicarConteudoDoComponenteDaBiblioteca(
        item: ItemDaBibliotecaDeComponentesHtmlDaColecao,
        carregarHtmlAgora: Boolean = true,
    ) {
        tituloDaPagina = item.nome
        htmlDaPagina = SanitizacaoDoHtmlEstaticoDaPaginaDaColecaoFlutuante.aplicar(item.html)
        idOrigemDaBiblioteca = item.id
        uriDaPastaEntregue = null
        larguraPendentePx = null
        alturaPendentePx = null
        aplicarDimensoesPersistidas(item.larguraPx, item.alturaPx)
        if (carregarHtmlAgora) {
            carregarHtmlDaPagina()
        }
        aplicarModoNaInterface()
        atualizarCabecalhoDaJanela(
            titulo = item.nome,
            subtitulo = activity.getString(R.string.subtitulo_componente_da_biblioteca_na_colecao),
        )
    }

    /** Carcaça aberta sem fade — evita WebView carregar “no escuro”. */
    private fun prepararCarcacaVisivelSemAnimacao() {
        val pedidoAtual = ++pedido
        selo.animate().cancel()
        selo.visibility = View.GONE
        painel.animate().cancel()
        painel.scaleX = 1f
        painel.scaleY = 1f
        painel.alpha = 1f
        aplicarModoNaInterface()
        painel.visibility = View.VISIBLE
        painel.bringToFront()
        forma = Forma.ABERTA
        garantirPosicao()
        retomarWebViewDaColecao()
        if (pedidoAtual != pedido) return
    }

    private fun carregarHtmlQuandoWebViewTiverTamanho(tentativas: Int = 20) {
        if (webDaPagina.width > 0 && webDaPagina.height > 0) {
            carregarHtmlDaPagina()
            return
        }
        if (tentativas <= 0) {
            carregarHtmlDaPagina()
            return
        }
        webDaPagina.post {
            carregarHtmlQuandoWebViewTiverTamanho(tentativas - 1)
        }
    }

    private fun retomarWebViewDaColecao() {
        runCatching {
            webDaPagina.onResume()
            webDaPagina.resumeTimers()
            webDaPagina.invalidate()
            webDaPagina.evaluateJavascript(
                "void(document.body&&(document.body.offsetHeight,document.body.style.transform='translateZ(0)'))",
                null,
            )
        }
    }

    private fun ehVisualizadorDeMidiaAtual(): Boolean =
        com.chromebrothers.navegador.regras.PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
            .ehVisualizadorDeMidiaPeloNome(tituloDaPagina)

    /** Moldura na miniatura de componentes com grade própria (ex.: Geração MCP — imagem); null limpa. */
    private fun avisarImagemNoVisorAoComponente(uriTexto: String?) {
        if (encerrada || ehVisualizadorDeMidiaAtual()) return
        val uri = uriTexto?.trim()?.let {
            com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
                .uriSemParametroDeMiniatura(it)
        }?.takeIf { it.isNotEmpty() }
        val argumento = uri?.let { org.json.JSONObject.quote(it) } ?: "null"
        webDaPagina.evaluateJavascript(
            "(function(){if(window.__chromebrothersImagemNoVisor)window.__chromebrothersImagemNoVisor($argumento);})();",
            null,
        )
    }

    fun destacarCartaDoVisualizador(uriTexto: String) {
        avisarImagemNoVisorAoComponente(uriTexto)
        if (!ehVisualizadorDeMidiaAtual()) return
        gradeNativaDoVisualizador.destacar(uriTexto)
        injetarScriptDeDestaqueDeCartaNoVisualizador()
        val uri = com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
            .uriSemParametroDeMiniatura(uriTexto.trim())
        if (uri.isEmpty()) return
        val jsUri = org.json.JSONObject.quote(uri)
        webDaPagina.evaluateJavascript(
            "(function(){if(window.cbVizDestacarCarta)window.cbVizDestacarCarta($jsUri);})();",
            null,
        )
    }

    fun limparDestaqueDeCartaDoVisualizador() {
        avisarImagemNoVisorAoComponente(null)
        if (!ehVisualizadorDeMidiaAtual()) return
        gradeNativaDoVisualizador.limparDestaque()
        webDaPagina.evaluateJavascript(
            "(function(){if(window.cbVizLimparDestaqueCarta)window.cbVizLimparDestaqueCarta();})();",
            null,
        )
    }

    fun avisarWebViewRedimensionada() {
        if (!ehVisualizadorDeMidiaAtual()) return
        webDaPagina.requestLayout()
        retomarWebViewDaColecao()
        injetarScriptDeDestaqueDeCartaNoVisualizador()
        webDaPagina.evaluateJavascript(
            "(function(){if(window.cbVizAvisarLarguraEstavel)window.cbVizAvisarLarguraEstavel();})();",
            null,
        )
    }

    private fun marcarGradeAguardandoLarguraEstavel() {
        if (!ehVisualizadorDeMidiaAtual()) return
        webDaPagina.evaluateJavascript(
            "document.body&&document.body.classList.add('cb-viz-aguarda-largura');",
            null,
        )
    }

    fun fecharVisorDoVisualizadorDeMidia() {
        visorDoVisualizadorDeMidia.fechar()
    }

    private fun abrirVisorDoVisualizadorDeMidia(uri: String, listaJson: String, indice: Int) {
        if (!ehVisualizadorDeMidiaAtual()) return
        aoForcarEncerrarArrasteDeImagemDoComponente?.invoke()
        val imagens = mutableListOf<String>()
        try {
            val arr = org.json.JSONArray(listaJson)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                if (obj.optString("tipo") != "imagem") continue
                val u = obj.optString("uri").trim()
                if (u.isNotEmpty()) imagens.add(u)
            }
        } catch (_: Exception) {
            imagens.add(uri)
        }
        if (imagens.isEmpty()) return
        val alvo = indice.coerceIn(0, imagens.lastIndex)
        val uriNorm = com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
            .uriSemParametroDeMiniatura(uri)
        val indiceNaLista = imagens.indexOfFirst {
            com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
                .uriSemParametroDeMiniatura(it) == uriNorm
        }.let { if (it >= 0) it else alvo }
        mostrarVisorDasImagens(imagens, indiceNaLista)
    }

    private fun injetarScriptDeDestaqueDeCartaNoVisualizador() {
        webDaPagina.evaluateJavascript(SCRIPT_DE_DESTAQUE_DE_CARTA_NO_VISUALIZADOR_DE_MIDIA, null)
    }

    private fun pausarMidiaDaPaginaEmWarmStart() {
        webDaPagina.evaluateJavascript(
            """
            (function(){
              document.querySelectorAll('video,audio').forEach(function(m){
                try { m.pause(); } catch (e) {}
              });
            })();
            """.trimIndent(),
            null,
        )
    }

    fun ligarPersistenciaDeDimensoes(aoPersistir: (id: String, larguraPx: Int, alturaPx: Int) -> Unit) {
        aoPersistirDimensoes = aoPersistir
    }

    private fun atualizarCabecalhoDaJanela(titulo: String, subtitulo: String) {
        tituloDaJanela.text = titulo.ifBlank { activity.getString(R.string.colecao_flutuante_do_chat) }
        subtituloDaJanela.text = subtitulo
    }

    fun guardarNaBiblioteca(
        biblioteca: BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador,
        gravacao: GravacaoDaBibliotecaDeComponentesHtmlDaColecaoNoArmazenamentoDoAplicativo,
        agoraMs: Long,
        descricao: String? = null,
        tags: List<String>? = null,
        novoId: () -> String,
    ): String {
        if (htmlDaPagina.isBlank()) return "Não há componente para guardar."
        val html = SanitizacaoDoHtmlEstaticoDaPaginaDaColecaoFlutuante.aplicar(htmlDaPagina)
        val (larguraAtual, alturaAtual) = dimensoesAtuaisDaJanela()
        val resultado = biblioteca.guardar(
            nome = tituloDaPagina,
            html = html,
            idExistente = idOrigemDaBiblioteca,
            agora = agoraMs,
            novoId = novoId,
            descricao = descricao,
            tags = tags,
            larguraPx = larguraAtual ?: larguraPendentePx,
            alturaPx = alturaAtual ?: alturaPendentePx,
        )
        return when (resultado) {
            is ResultadoDaBibliotecaDeComponentesHtml.Recusada -> resultado.mensagem
            is ResultadoDaBibliotecaDeComponentesHtml.Ok -> {
                gravacao.escreverHtml(resultado.item.id, resultado.item.html)
                gravacao.gravar(biblioteca)
                idOrigemDaBiblioteca = resultado.item.id
                larguraPendentePx = null
                alturaPendentePx = null
                if (resultado.criou) {
                    "Guardei o componente \"${resultado.item.nome}\" na biblioteca."
                } else {
                    "Atualizei o componente \"${resultado.item.nome}\" na biblioteca."
                }
            }
        }
    }

    private var aoGuardarNaBiblioteca: (() -> String)? = null

    fun ligarGuardarNaBiblioteca(guardar: () -> String) {
        aoGuardarNaBiblioteca = guardar
        botaoGuardarPagina.setOnClickListener {
            if (!botaoGuardarPagina.isEnabled) return@setOnClickListener
            aoFocar()
            Toast.makeText(activity, guardar(), Toast.LENGTH_SHORT).show()
        }
    }

    fun resumoParaLista(): String {
        val titulo = tituloDaPagina.ifBlank { "sem título" }
        val estado = when (forma) {
            Forma.BOLHA -> "bolha"
            Forma.ABERTA -> "aberta"
            Forma.OCULTA -> "oculta"
        }
        return "$titulo ($estado)"
    }

    fun trazerParaFrente() {
        if (encerrada) return
        // Acima da gaveta/editor (32dp) e do chat (40dp), senão a janela nasce atrás do visor.
        val densidade = activity.resources.displayMetrics.density
        if (painel.visibility == View.VISIBLE) {
            painel.elevation = 48f * densidade
            painel.bringToFront()
        }
        if (selo.visibility == View.VISIBLE) {
            selo.elevation = 52f * densidade
            selo.bringToFront()
        }
    }

    /** clientX/Y da página → coordenadas de tela (screenX do WebView é instável no Android). */
    fun clienteDaWebViewParaTela(clientX: Float, clientY: Float): FloatArray {
        val loc = IntArray(2)
        webDaPagina.getLocationOnScreen(loc)
        val escala = webDaPagina.scale.coerceAtLeast(0.01f)
        return floatArrayOf(loc[0] + clientX * escala, loc[1] + clientY * escala)
    }

    /** Esta janela mostra o Visualizador de mídia (destino de "salvar" no arraste vindo do site). */
    fun ehVisualizadorDeMidia(): Boolean = !encerrada && ehVisualizadorDeMidiaAtual()

    /**
     * @param mesmoLiberada testa a área mesmo com a janela liberada para a página durante o arraste
     * (o Visualizador continua alvo para salvar a imagem que veio do site).
     */
    fun contemPontoNaTela(xNaTela: Float, yNaTela: Float, mesmoLiberada: Boolean = false): Boolean {
        if (encerrada) return false
        if (ignorarHitTestDuranteArrasteParaPagina && !mesmoLiberada) return false
        val regra = com.chromebrothers.navegador.regras.HitTestDaJanelaDaColecaoNoArrasteDoFantasma
        if (
            !regra.podeTestar(
                ancoradaNaGaveta = ancoradaNaGaveta,
                painelVisivel = painel.visibility == View.VISIBLE,
                painelLargura = painel.width,
                painelAltura = painel.height,
                conteudoVisivel = conteudoPagina.visibility == View.VISIBLE,
                conteudoLargura = conteudoPagina.width,
                conteudoAltura = conteudoPagina.height,
            )
        ) {
            return false
        }
        val vista = when (regra.alvo(ancoradaNaGaveta)) {
            com.chromebrothers.navegador.regras.HitTestDaJanelaDaColecaoNoArrasteDoFantasma.Alvo.PAINEL_FLUTUANTE ->
                painel
            com.chromebrothers.navegador.regras.HitTestDaJanelaDaColecaoNoArrasteDoFantasma.Alvo.CONTEUDO_NA_GAVETA ->
                conteudoPagina
        }
        val loc = IntArray(2)
        vista.getLocationOnScreen(loc)
        return regra.pontoDentro(
            x = xNaTela,
            y = yNaTela,
            esquerda = loc[0].toFloat(),
            topo = loc[1].toFloat(),
            largura = vista.width,
            altura = vista.height,
        )
    }

    /**
     * Enquanto arrasta mídia para a página, o Visualizador deixa de “comer” o hit-test
     * para o soltar cair na página (ChatGPT sem verde). Sem baixar o alpha — o painel
     * continua opaco. Geradores MCP não entram (precisam do drop de referência).
     */
    fun liberarPaginaSobArrasteDoComponente() {
        if (encerrada || forma != Forma.ABERTA || ignorarHitTestDuranteArrasteParaPagina) return
        val id = idOrigemDaBiblioteca.orEmpty()
        val tipoMcp = com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaReferenciaMcp
            .tipoDoComponentePeloId(id)
        if (tipoMcp.isNotEmpty()) return
        ignorarHitTestDuranteArrasteParaPagina = true
    }

    fun restaurarPaginaAposArrasteDoComponente() {
        if (!ignorarHitTestDuranteArrasteParaPagina) return
        ignorarHitTestDuranteArrasteParaPagina = false
    }

    /**
     * Tira o foco do WebView da coleção para o Ctrl+V ir para a página (Gecko).
     * O Visualizador/MCP usam Chromium e roubam o foco após o long-press.
     */
    fun liberarFocoParaColarNaPagina() {
        if (encerrada || focoLiberadoParaColarNaPagina) return
        focoLiberadoParaColarNaPagina = true
        webEraFocusableAntesDoColar = webDaPagina.isFocusable
        webEraFocusableInTouchModeAntesDoColar = webDaPagina.isFocusableInTouchMode
        webDaPagina.clearFocus()
        webDaPagina.isFocusable = false
        webDaPagina.isFocusableInTouchMode = false
    }

    fun restaurarFocoAposColarNaPagina() {
        if (!focoLiberadoParaColarNaPagina) return
        focoLiberadoParaColarNaPagina = false
        webDaPagina.isFocusable = webEraFocusableAntesDoColar
        webDaPagina.isFocusableInTouchMode = webEraFocusableInTouchModeAntesDoColar
    }

    fun revelar() {
        if (encerrada || !temConteudo()) return
        if (forma != Forma.ABERTA) mostrar()
    }

    fun encerrar() {
        if (encerrada) return
        encerrada = true
        pedido++
        cancelarSeletorDeArquivoPendente()
        painel.animate().cancel()
        selo.animate().cancel()
        painel.visibility = View.GONE
        selo.visibility = View.GONE
        filaDeMiniaturas.shutdownNow()
        runCatching {
            webDaPagina.stopLoading()
            webDaPagina.removeJavascriptInterface("chromebrothers")
            (webDaPagina.parent as? ViewGroup)?.removeView(webDaPagina)
            webDaPagina.destroy()
        }
        (painel.parent as? ViewGroup)?.removeView(painel)
        (selo.parent as? ViewGroup)?.removeView(selo)
        aoEncerrar()
    }

    fun descrever(): String {
        if (htmlDaPagina.isBlank()) return "A coleção está vazia."
        val titulo = tituloDaPagina.ifBlank { "sem título" }
        return "Página na coleção: $titulo"
    }

    private fun atualizarBotaoDeGuardar() {
        val podeGuardar = htmlDaPagina.isNotBlank()
        botaoGuardarPagina.isEnabled = podeGuardar
        botaoGuardarPagina.alpha = if (podeGuardar) 1f else 0.4f
    }

    fun ligarSeletorDeArquivoDoAparelho(
        pedir: (
            ValueCallback<Array<Uri>>,
            WebChromeClient.FileChooserParams,
        ) -> Boolean,
    ) {
        aoPedirArquivoDoAparelho = pedir
    }

    private fun interceptarArquivoLocalDaColecao(
        uri: Uri,
        request: WebResourceRequest?,
    ): WebResourceResponse {
        val uriLimpa = uriSemParametroDeMiniatura(uri)
        val arquivo = File(uriLimpa.path.orEmpty())
        val canonico = runCatching { arquivo.canonicalFile }.getOrNull()
            ?: return WebResourceResponse("text/plain", "utf-8", null)
        if (!canonico.exists() || !arquivoLocalEstaDentroDoAplicativo(canonico)) {
            return WebResourceResponse("text/plain", "utf-8", null)
        }
        return runCatching {
            val mime = mimePeloNome(canonico.name)
            val ladoThumb = com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
                .ladoDaMiniatura(uri.toString())
            if (ladoThumb != null && (mime.startsWith("image/") || mime.startsWith("video/"))) {
                return@runCatching respostaMiniaturaDeArquivo(canonico, mime, ladoThumb)
                    ?: WebResourceResponse("text/plain", "utf-8", null)
            }
            val tamanho = canonico.length()
            val range = cabecalhoRange(request)
            val faixa = com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
                .interpretar(range, tamanho)
            if (faixa != null) {
                val entrada = FileInputStream(canonico).also { it.skip(faixa.inicio) }
                val limitado = EntradaLimitadaPorBytes(entrada, faixa.tamanhoDaFaixa)
                val headers = mapOf(
                    "Accept-Ranges" to "bytes",
                    "Content-Range" to faixa.cabecalhoContentRange,
                    "Content-Length" to faixa.tamanhoDaFaixa.toString(),
                )
                return@runCatching WebResourceResponse(mime, null, 206, "Partial Content", headers, limitado)
            }
            val headers = mapOf(
                "Content-Length" to tamanho.toString(),
                "Accept-Ranges" to "bytes",
            )
            WebResourceResponse(mime, null, 200, "OK", headers, FileInputStream(canonico))
        }.getOrElse {
            WebResourceResponse("text/plain", "utf-8", null)
        }
    }

    private fun arquivoLocalEstaDentroDoAplicativo(canonico: File): Boolean {
        val raizes = listOf(activity.cacheDir, activity.filesDir).mapNotNull {
            runCatching { it.canonicalFile.path }.getOrNull()
        }
        return raizes.any { raiz -> canonico.path.startsWith(raiz) }
    }

    private fun interceptarConteudoDoAparelhoNaColecao(
        uri: Uri,
        request: WebResourceRequest?,
    ): WebResourceResponse {
        return runCatching {
            val uriLimpa = uriSemParametroDeMiniatura(uri)
            val tipo = activity.contentResolver.getType(uriLimpa)
                ?.takeIf { it.isNotBlank() }
                ?: mimePeloNome(uriLimpa.lastPathSegment.orEmpty())
            val ladoThumb = com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
                .ladoDaMiniatura(uri.toString())
            if (ladoThumb != null && (tipo.startsWith("image/") || tipo.startsWith("video/"))) {
                return@runCatching respostaMiniaturaDeConteudo(uriLimpa, tipo, ladoThumb)
                    ?: WebResourceResponse("text/plain", "utf-8", null)
            }
            val afd = activity.contentResolver.openAssetFileDescriptor(uriLimpa, "r")
                ?: return@runCatching WebResourceResponse("text/plain", "utf-8", null)
            val tamanho = afd.length
            val range = cabecalhoRange(request)
            val faixa = if (tamanho > 0L) {
                com.chromebrothers.navegador.regras.InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao
                    .interpretar(range, tamanho)
            } else {
                null
            }
            if (faixa != null) {
                val entrada = afd.createInputStream().also { it.skip(faixa.inicio) }
                val limitado = EntradaLimitadaPorBytes(entrada, faixa.tamanhoDaFaixa)
                val headers = mapOf(
                    "Accept-Ranges" to "bytes",
                    "Content-Range" to faixa.cabecalhoContentRange,
                    "Content-Length" to faixa.tamanhoDaFaixa.toString(),
                )
                return@runCatching WebResourceResponse(tipo, null, 206, "Partial Content", headers, limitado)
            }
            val entrada = afd.createInputStream()
            val headers = buildMap {
                put("Accept-Ranges", "bytes")
                if (tamanho > 0L) put("Content-Length", tamanho.toString())
            }
            WebResourceResponse(tipo, null, 200, "OK", headers, entrada)
        }.getOrElse {
            WebResourceResponse("text/plain", "utf-8", null)
        }
    }

    private fun cabecalhoRange(request: WebResourceRequest?): String? =
        request?.requestHeaders?.entries
            ?.firstOrNull { it.key.equals("Range", ignoreCase = true) }
            ?.value

    private fun uriSemParametroDeMiniatura(uri: Uri): Uri {
        if (uri.getQueryParameter("cb_thumb") == null) return uri
        val builder = uri.buildUpon().clearQuery()
        for (nome in uri.queryParameterNames) {
            if (nome == "cb_thumb") continue
            for (valor in uri.getQueryParameters(nome)) {
                builder.appendQueryParameter(nome, valor)
            }
        }
        return builder.build()
    }

    private fun respostaMiniaturaDeArquivo(arquivo: File, mime: String, ladoMax: Int): WebResourceResponse? =
        respostaDoArquivoDeMiniatura(
            geradorDeMiniaturas.arquivoEmCacheDeArquivo(arquivo, mime.startsWith("video/"), ladoMax),
        )

    private fun respostaMiniaturaDeConteudo(uri: Uri, mime: String, ladoMax: Int): WebResourceResponse? =
        respostaDoArquivoDeMiniatura(
            geradorDeMiniaturas.arquivoEmCacheDeConteudo(uri, mime.startsWith("video/"), ladoMax),
        )

    private fun respostaDoArquivoDeMiniatura(cache: File?): WebResourceResponse? {
        if (cache == null) return null
        val headers = mapOf(
            "Content-Length" to cache.length().toString(),
            "Cache-Control" to "max-age=86400",
            "Accept-Ranges" to "none",
        )
        return WebResourceResponse("image/jpeg", null, 200, "OK", headers, FileInputStream(cache))
    }

    /** InputStream que entrega no máximo [limite] bytes (para HTTP 206). */
    private class EntradaLimitadaPorBytes(
        private val origem: java.io.InputStream,
        private val limite: Long,
    ) : java.io.InputStream() {
        private var lido = 0L
        override fun read(): Int {
            if (lido >= limite) return -1
            val r = origem.read()
            if (r >= 0) lido++
            return r
        }
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (lido >= limite) return -1
            val permitir = minOf(len.toLong(), limite - lido).toInt()
            val r = origem.read(b, off, permitir)
            if (r > 0) lido += r.toLong()
            return r
        }
        override fun close() = origem.close()
    }

    private fun mimePeloNome(nome: String): String {
        val minusculo = nome.lowercase()
        return when {
            minusculo.endsWith(".png") -> "image/png"
            minusculo.endsWith(".webp") -> "image/webp"
            minusculo.endsWith(".gif") -> "image/gif"
            minusculo.endsWith(".jpg") || minusculo.endsWith(".jpeg") -> "image/jpeg"
            minusculo.endsWith(".mp4") -> "video/mp4"
            minusculo.endsWith(".webm") -> "video/webm"
            minusculo.endsWith(".pdf") -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    fun cancelarSeletorDeArquivoPendente() {
        callbackDeArquivoPendente?.onReceiveValue(null)
        callbackDeArquivoPendente = null
    }

    fun limparCallbackDeArquivoSeFor(callback: ValueCallback<Array<Uri>>) {
        if (callbackDeArquivoPendente === callback) callbackDeArquivoPendente = null
    }

    private fun prepararWebDaPagina() {
        webDaPagina.settings.javaScriptEnabled = true
        webDaPagina.settings.domStorageEnabled = false
        webDaPagina.settings.allowFileAccess = true
        webDaPagina.settings.allowContentAccess = true
        // Página carrega com base https://; vídeos/imagens remotas em http:// precisam de mixed content.
        webDaPagina.settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webDaPagina.addJavascriptInterface(PonteJavascriptDaPaginaDaColecao(), "chromebrothers")
        webDaPagina.setBackgroundColor(Color.parseColor("#1A1D24"))
        webDaPagina.setOnTouchListener { _, evento ->
            if (evento.actionMasked == MotionEvent.ACTION_DOWN) aoFocar()
            false
        }
        webDaPagina.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?,
            ): Boolean {
                if (filePathCallback == null || fileChooserParams == null) return false
                cancelarSeletorDeArquivoPendente()
                callbackDeArquivoPendente = filePathCallback
                aoFocar()
                val pedido = aoPedirArquivoDoAparelho
                if (pedido == null) {
                    cancelarSeletorDeArquivoPendente()
                    return false
                }
                val ok = runCatching { pedido(filePathCallback, fileChooserParams) }.getOrDefault(false)
                if (!ok) cancelarSeletorDeArquivoPendente()
                return ok
            }

            /** Em vez de fullscreen HTML, abre o player nativo com o vídeo da página. */
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                callback?.onCustomViewHidden()
                webDaPagina.evaluateJavascript(
                    """
                    (function(){
                      var v = document.querySelector('video');
                      if (!v) return '';
                      var uri = v.getAttribute('data-cb-uri') || v.currentSrc || v.src || '';
                      if (uri && window.chromebrothers && chromebrothers.abrirVideoNoPlayer) {
                        return chromebrothers.abrirVideoNoPlayer(uri);
                      }
                      return '';
                    })();
                    """.trimIndent(),
                    null,
                )
            }

            override fun onHideCustomView() = Unit
        }
        webDaPagina.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                cargaDaPaginaPronta = true
                if (htmlDaPagina.isNotBlank()) {
                    injetarScriptDeEnvioDeImagemParaCamadaDoEditor()
                    injetarScriptDeMiniaturasNaGradeDoVisualizador()
                    injetarScriptDeDestaqueDeCartaNoVisualizador()
                    agendarAlimentacaoDoVisualizadorComPastaVirtualDeMidia()
                }
                val pendente = aoTerminarCargaDaPagina
                aoTerminarCargaDaPagina = null
                pendente?.invoke()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val endereco = request?.url?.toString().orEmpty()
                if (endereco.startsWith("http://") || endereco.startsWith("https://")) {
                    abrirEnderecoDaPagina(endereco)
                    return true
                }
                return true
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?,
            ): WebResourceResponse? {
                val uri = request?.url ?: return super.shouldInterceptRequest(view, request)
                return when (uri.scheme) {
                    "file" -> interceptarArquivoLocalDaColecao(uri, request)
                    "content" -> interceptarConteudoDoAparelhoNaColecao(uri, request)
                    else -> super.shouldInterceptRequest(view, request)
                }
            }
        }
    }

    private fun pedirGeracaoDePdf(nomeSugerido: String) {
        if (htmlDaPagina.isBlank()) {
            Toast.makeText(
                activity,
                activity.getString(R.string.pagina_da_colecao_ainda_vazia),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        if (gerandoPdf) {
            Toast.makeText(
                activity,
                activity.getString(R.string.pdf_da_pagina_da_colecao_em_andamento),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        gerandoPdf = true
        Toast.makeText(
            activity,
            activity.getString(R.string.pdf_da_pagina_da_colecao_gerando),
            Toast.LENGTH_SHORT,
        ).show()
        val nome = nomeSugerido.trim().ifBlank { tituloDaPagina.ifBlank { "pagina" } }
        gravacaoDePdf.gravar(webDaPagina, nome) { mensagem ->
            gerandoPdf = false
            Toast.makeText(activity, mensagem, Toast.LENGTH_LONG).show()
        }
    }

    private fun carregarHtmlDaPagina() {
        cargaDaPaginaPronta = false
        if (htmlDaPagina.isBlank()) {
            webDaPagina.loadUrl("about:blank")
        } else {
            webDaPagina.loadDataWithBaseURL(
                "https://colecao.chromebrothers.local/",
                htmlDaPagina,
                "text/html",
                "utf-8",
                null,
            )
        }
        atualizarAvisoDaPagina()
    }

    private fun injetarScriptDeEnvioDeImagemParaCamadaDoEditor() {
        webDaPagina.evaluateJavascript(SCRIPT_DE_ENVIO_DE_IMAGEM_PARA_CAMADA_DO_EDITOR, null)
    }

    private fun atualizarAvisoDaPagina() {
        val vazia = htmlDaPagina.isBlank()
        avisoPaginaVazia.visibility = if (vazia) View.VISIBLE else View.GONE
        webDaPagina.visibility = if (vazia) View.INVISIBLE else View.VISIBLE
    }

    fun colocar(novos: List<ItemDaColecaoFlutuante>): String {
        val resultado = MontagemDaColecaoFlutuanteDoChat.acrescentar(itens.toList(), novos)
        itens.clear()
        itens.addAll(resultado.itens)
        marcados.retainAll(itens.map { it.endereco }.toSet())
        mostrar()
        return resultado.mensagem
    }

    fun organizar(ajustes: List<AjusteDaOrdemDaColecao>): String {
        val resultado = MontagemDaColecaoFlutuanteDoChat.organizar(itens.toList(), ajustes)
        substituir(resultado.itens)
        return resultado.mensagem
    }

    fun tirar(enderecos: List<String>): String {
        val resultado = MontagemDaColecaoFlutuanteDoChat.tirar(itens.toList(), enderecos)
        substituir(resultado.itens)
        return resultado.mensagem
    }

    private fun temConteudo(): Boolean = htmlDaPagina.isNotBlank()

    private fun aplicarModoNaInterface() {
        conteudoMidia.visibility = View.GONE
        conteudoPagina.visibility = View.VISIBLE
        botaoModoMidia.visibility = View.GONE
        botaoModoPagina.visibility = View.GONE
        atualizarAvisoDaPagina()
        atualizarBotaoDeGuardar()
        sincronizarGradeNativaDoVisualizador()
    }

    private fun substituir(novos: List<ItemDaColecaoFlutuante>) {
        val estavaAberta = forma == Forma.ABERTA
        val estavaNaBolha = forma == Forma.BOLHA
        itens.clear()
        itens.addAll(novos)
        marcados.retainAll(itens.map { it.endereco }.toSet())
        when {
            !temConteudo() -> esconderTudo()
            estavaAberta -> atualizarSelo()
            estavaNaBolha -> atualizarSelo()
            else -> mostrar()
        }
    }

    private fun mostrar() {
        if (!temConteudo()) {
            esconderTudoSemAnimacao()
            return
        }
        if (forma == Forma.ABERTA) {
            // Já aberto: não recarregar HTML (Visualizador/warm-start mantêm a grade).
            aplicarModoNaInterface()
            trazerParaFrente()
            retomarWebViewDaColecao()
            return
        }
        val pedidoAtual = ++pedido
        val veioDaBolha = forma == Forma.BOLHA && selo.visibility == View.VISIBLE
        val xDaBolha = selo.x
        val yDaBolha = selo.y
        val ladoDaBolha = ladoDoSelo()
        selo.animate().cancel()
        selo.visibility = View.GONE
        aplicarModoNaInterface()
        painel.animate().cancel()
        painel.scaleX = 1f
        painel.scaleY = 1f
        painel.alpha = 1f
        painel.visibility = View.INVISIBLE
        painel.post {
            if (pedidoAtual != pedido) return@post
            garantirPosicao()
            if (veioDaBolha) abrirJuntoDaBolha(xDaBolha, yDaBolha, ladoDaBolha)
            crescer(pedidoAtual)
            rolagem.post { rolagem.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun crescer(pedidoAtual: Int) {
        forma = Forma.ABERTA
        alinharPivo()
        retomarWebViewDaColecao()
        painel.visibility = View.VISIBLE
        painel.bringToFront()
        // Warm-start / Visualizador: sem fade de alpha 0 (mascara a grade).
        if (reservadoParaWarmStart || ehVisualizadorDeMidiaAtual()) {
            painel.scaleX = 1f
            painel.scaleY = 1f
            painel.alpha = 1f
            retomarWebViewDaColecao()
            if (ehVisualizadorDeMidiaAtual() && cargaDaPaginaPronta && uriDaPastaEntregue == null) {
                agendarAlimentacaoDoVisualizadorComPastaVirtualDeMidia()
            }
            return
        }
        painel.scaleX = 0.2f
        painel.scaleY = 0.2f
        painel.alpha = 0f
        painel.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(220)
            .setInterpolator(curva)
            .withEndAction {
                if (pedidoAtual != pedido) return@withEndAction
                painel.scaleX = 1f
                painel.scaleY = 1f
                painel.alpha = 1f
                retomarWebViewDaColecao()
            }
            .start()
    }

    private fun encolher(mostrarBolha: Boolean, aoFim: (() -> Unit)? = null) {
        if (forma != Forma.ABERTA) {
            if (!mostrarBolha) esconderTudoSemAnimacao()
            aoFim?.invoke()
            return
        }
        val pedidoAtual = ++pedido
        alinharPivo()
        painel.animate().cancel()
        painel.animate()
            .scaleX(0.2f)
            .scaleY(0.2f)
            .alpha(0f)
            .setDuration(220)
            .setInterpolator(curva)
            .withEndAction {
                if (pedidoAtual != pedido) return@withEndAction
                painel.visibility =
                    if (!mostrarBolha && reservadoParaWarmStart) View.INVISIBLE else View.GONE
                painel.scaleX = 1f
                painel.scaleY = 1f
                painel.alpha = 1f
                if (mostrarBolha && temConteudo()) {
                    atualizarSelo()
                    if (!seloPosicionado) colocarSeloInicial()
                    else posicionarSelo(selo.x, selo.y)
                    selo.scaleX = 0.6f
                    selo.scaleY = 0.6f
                    selo.visibility = View.VISIBLE
                    selo.bringToFront()
                    selo.animate().cancel()
                    selo.animate().scaleX(1f).scaleY(1f).setDuration(160).setInterpolator(curva).start()
                    forma = Forma.BOLHA
                } else {
                    selo.visibility = View.GONE
                    forma = Forma.OCULTA
                }
                aoFim?.invoke()
            }
            .start()
    }

    private fun alinharPivo() {
        val largura = painel.width.toFloat().coerceAtLeast(1f)
        val altura = painel.height.toFloat().coerceAtLeast(1f)
        if (seloPosicionado || selo.visibility == View.VISIBLE) {
            painel.pivotX = (selo.x - painel.x).coerceIn(0f, largura)
            painel.pivotY = (selo.y - painel.y).coerceIn(0f, altura)
        } else {
            painel.pivotX = largura / 2f
            painel.pivotY = altura / 2f
        }
    }

    private fun esconderTudo() {
        when (forma) {
            Forma.ABERTA -> encolher(mostrarBolha = false) {
                limparConteudo()
            }
            Forma.BOLHA -> {
                val pedidoAtual = ++pedido
                selo.animate().cancel()
                selo.animate()
                    .scaleX(0.6f)
                    .scaleY(0.6f)
                    .alpha(0f)
                    .setDuration(160)
                    .setInterpolator(curva)
                    .withEndAction {
                        if (pedidoAtual != pedido) return@withEndAction
                        limparConteudo()
                        esconderTudoSemAnimacao()
                        selo.scaleX = 1f
                        selo.scaleY = 1f
                        selo.alpha = 1f
                    }
                    .start()
            }
            Forma.OCULTA -> limparConteudo()
        }
    }

    private fun esconderTudoSemAnimacao() {
        pedido++
        painel.animate().cancel()
        selo.animate().cancel()
        painel.visibility = View.GONE
        selo.visibility = View.GONE
        painel.scaleX = 1f
        painel.scaleY = 1f
        painel.alpha = 1f
        selo.scaleX = 1f
        selo.scaleY = 1f
        selo.alpha = 1f
        forma = Forma.OCULTA
    }

    private fun limparConteudo() {
        geracao++
        itens.clear()
        marcados.clear()
        miniaturas.evictAll()
        lista.removeAllViews()
        tituloDaPagina = ""
        htmlDaPagina = ""
        idOrigemDaBiblioteca = null
        webDaPagina.loadUrl("about:blank")
        aplicarModoNaInterface()
        atualizarCabecalhoDaJanela(
            titulo = activity.getString(R.string.colecao_flutuante_do_chat),
            subtitulo = activity.getString(R.string.subtitulo_padrao_da_janela_da_colecao_flutuante),
        )
        seloPosicionado = false
    }

    private fun garantirPosicao() {
        if (posicaoDefinida) return
        val pai = painel.parent as? View ?: return
        if (pai.width <= 0 || pai.height <= 0) return
        val densidade = painel.resources.displayMetrics.density
        val largura = if (painel.width > 0) painel.width.toFloat() else 420f * densidade
        val altura = if (painel.height > 0) painel.height.toFloat() else 480f * densidade
        val lugar = PosicaoDaJanelaDoCampoDeTexto.limitar(
            16f * densidade + deslocamentoInicialPx,
            pai.height - altura - 24f * densidade - deslocamentoInicialPx,
            largura,
            altura,
            pai.width.toFloat(),
            pai.height.toFloat(),
        )
        painel.x = lugar.first
        painel.y = lugar.second
        posicaoDefinida = true
    }

    private fun posicionar(x: Float, y: Float) {
        val pai = painel.parent as? View ?: return
        if (pai.width <= 0 || pai.height <= 0) return
        val lugar = PosicaoDaJanelaDoCampoDeTexto.limitar(
            x,
            y,
            painel.width.toFloat().coerceAtLeast(1f),
            painel.height.toFloat().coerceAtLeast(1f),
            pai.width.toFloat(),
            pai.height.toFloat(),
        )
        painel.x = lugar.first
        painel.y = lugar.second
        posicaoDefinida = true
    }

    private fun colocarSeloInicial() {
        val pai = selo.parent as? View ?: return
        if (pai.width <= 0 || pai.height <= 0) return
        val densidade = selo.resources.displayMetrics.density
        val lado = ladoDoSelo()
        if (painel.width > 0 && painel.height > 0) {
            posicionarSelo(painel.x + painel.width - lado - 8f * densidade, painel.y + 8f * densidade)
            return
        }
        val utilTopo = pai.paddingTop + 16f * densidade
        val utilBaixo = pai.height - pai.paddingBottom - 16f * densidade
        val meio = utilTopo + ((utilBaixo - utilTopo - lado) / 2f).coerceAtLeast(0f)
        posicionarSelo(
            pai.paddingLeft + 16f * densidade + deslocamentoInicialPx,
            meio + deslocamentoInicialPx,
        )
    }

    private fun posicionarSelo(x: Float, y: Float) {
        val pai = selo.parent as? View ?: return
        if (pai.width <= 0 || pai.height <= 0) return
        val densidade = selo.resources.displayMetrics.density
        val margem = 16f * densidade
        val largura = selo.width.toFloat().coerceAtLeast(1f)
        val altura = selo.height.toFloat().coerceAtLeast(1f)
        val minX = pai.paddingLeft + margem
        val minY = pai.paddingTop + margem
        val maxX = (pai.width - pai.paddingRight - largura - margem).coerceAtLeast(minX)
        val maxY = (pai.height - pai.paddingBottom - altura - margem).coerceAtLeast(minY)
        selo.x = x.coerceIn(minX, maxX)
        selo.y = y.coerceIn(minY, maxY)
        seloPosicionado = true
    }

    private fun prepararBolha() {
        var ancoraX = 0f
        var ancoraY = 0f
        GestoDaBolhaFlutuanteComAlvoDeFecharEFixacaoNoChatDoNavegador(
            activity = activity,
            bolha = selo,
            aoComecar = {
                aoFocar()
                ancoraX = selo.x
                ancoraY = selo.y
            },
            aoMover = { dx, dy -> posicionarSelo(ancoraX + dx, ancoraY + dy) },
            aoAbrir = { mostrar() },
            aoFechar = { fechar() },
            aoFixarNoChat = { aoAncorarNaGaveta?.invoke() },
            consultarFaixaDoChat = { x, y, fase -> aoArrastarPelaBarra?.invoke(x, y, fase) == true },
        ).ligar()
    }

    private fun prepararArraste(alvo: View) {
        var origemX = 0f
        var origemY = 0f
        var ancoraX = 0f
        var ancoraY = 0f
        var arrastou = false
        val limiar = 8f * alvo.resources.displayMetrics.density
        alvo.setOnTouchListener { _, evento ->
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    aoFocar()
                    origemX = evento.rawX
                    origemY = evento.rawY
                    ancoraX = painel.x
                    ancoraY = painel.y
                    arrastou = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = evento.rawX - origemX
                    val dy = evento.rawY - origemY
                    if (!arrastou && (kotlin.math.abs(dx) > limiar || kotlin.math.abs(dy) > limiar)) {
                        arrastou = true
                    }
                    if (arrastou) {
                        posicionar(ancoraX + dx, ancoraY + dy)
                        val sobreAFaixa = aoArrastarPelaBarra?.invoke(
                            evento.rawX,
                            evento.rawY,
                            FaseDoArrasteDaJanelaPelaBarra.MOVENDO,
                        ) == true
                        painel.alpha = if (sobreAFaixa) 0.55f else 1f
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!arrastou) {
                        pedirNovoNomeDoComponente()
                    } else {
                        painel.alpha = 1f
                        val fixar = aoArrastarPelaBarra?.invoke(
                            evento.rawX,
                            evento.rawY,
                            FaseDoArrasteDaJanelaPelaBarra.SOLTOU,
                        ) == true
                        if (fixar) aoAncorarNaGaveta?.invoke()
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (arrastou) {
                        painel.alpha = 1f
                        aoArrastarPelaBarra?.invoke(evento.rawX, evento.rawY, FaseDoArrasteDaJanelaPelaBarra.CANCELOU)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun pedirNovoNomeDoComponente() {
        if (encerrada) return
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(R.layout.layout_caixa_de_dialogo_do_editor_de_imagem, null)
        raiz.findViewById<TextView>(R.id.titulo_da_caixa_de_dialogo_do_editor).text =
            activity.getString(R.string.titulo_renomear_componente_da_colecao)
        raiz.findViewById<TextView>(R.id.mensagem_da_caixa_de_dialogo_do_editor).visibility = View.GONE
        val campo = raiz.findViewById<EditText>(R.id.campo_da_caixa_de_dialogo_do_editor)
        campo.visibility = View.VISIBLE
        campo.hint = activity.getString(R.string.colecao_flutuante_do_chat)
        campo.setText(tituloDaPagina)
        campo.setSelection(campo.text.length)
        val cancelar = raiz.findViewById<Button>(R.id.botao_cancelar_da_caixa_de_dialogo_do_editor)
        val confirmar = raiz.findViewById<Button>(R.id.botao_confirmar_da_caixa_de_dialogo_do_editor)
        cancelar.text = activity.getString(R.string.cancelar_caixa_do_editor)
        cancelar.backgroundTintList = null
        cancelar.stateListAnimator = null
        confirmar.text = activity.getString(R.string.renomear_componente_da_colecao)
        confirmar.setTextColor(Color.parseColor("#8AB4F8"))
        confirmar.backgroundTintList = null
        confirmar.stateListAnimator = null
        cancelar.setOnClickListener { dialogo.dismiss() }
        confirmar.setOnClickListener {
            dialogo.dismiss()
            aplicarNovoNomeDoComponente(campo.text?.toString().orEmpty())
        }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        dialogo.setOnShowListener {
            campo.requestFocus()
        }
        dialogo.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun aplicarNovoNomeDoComponente(bruto: String) {
        val limpo = bruto.trim()
        if (limpo.isEmpty() || limpo.length > BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_NOME) {
            Toast.makeText(activity, R.string.nome_do_componente_da_colecao_invalido, Toast.LENGTH_LONG).show()
            return
        }
        tituloDaPagina = limpo
        sincronizarGradeNativaDoVisualizador()
        val subtitulo = when {
            idOrigemDaBiblioteca != null ->
                activity.getString(R.string.subtitulo_componente_da_biblioteca_na_colecao)
            htmlDaPagina.isNotBlank() ->
                activity.getString(R.string.subtitulo_pagina_publicada_da_colecao_flutuante)
            else ->
                activity.getString(R.string.subtitulo_padrao_da_janela_da_colecao_flutuante)
        }
        atualizarCabecalhoDaJanela(limpo, subtitulo)
        atualizarSelo()
        if (idOrigemDaBiblioteca != null) {
            aoFocar()
            val mensagem = aoGuardarNaBiblioteca?.invoke()
                ?: activity.getString(R.string.componente_da_colecao_renomeado)
            Toast.makeText(activity, mensagem, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, R.string.componente_da_colecao_renomeado, Toast.LENGTH_SHORT).show()
        }
    }

    private fun prepararTamanho(alca: View) {
        var origemX = 0f
        var origemY = 0f
        var larguraInicial = 0
        var alturaInicial = 0
        alca.setOnTouchListener { _, evento ->
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    aoFocar()
                    origemX = evento.rawX
                    origemY = evento.rawY
                    larguraInicial = painel.width
                    alturaInicial = painel.height
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val pai = painel.parent as? View
                    if (pai != null && larguraInicial > 0 && alturaInicial > 0) {
                        val densidade = painel.resources.displayMetrics.density
                        val novaLargura = PosicaoDaJanelaDoCampoDeTexto.largura(
                            larguraInicial.toFloat(),
                            evento.rawX - origemX,
                            painel.x,
                            280f * densidade,
                            pai.width.toFloat(),
                        ).toInt()
                        val novaAltura = PosicaoDaJanelaDoCampoDeTexto.altura(
                            alturaInicial.toFloat(),
                            evento.rawY - origemY,
                            painel.y,
                            260f * densidade,
                            pai.height.toFloat(),
                        ).toInt()
                        val params = painel.layoutParams
                        if (params.width != novaLargura || params.height != novaAltura) {
                            params.width = novaLargura
                            params.height = novaAltura
                            painel.layoutParams = params
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    persistirDimensoesAtuais()
                    true
                }
                else -> false
            }
        }
    }

    private fun aplicarDimensoesPersistidas(larguraPx: Int?, alturaPx: Int?) {
        val largura = larguraPx?.takeIf { it > 0 } ?: return
        val altura = alturaPx?.takeIf { it > 0 } ?: return
        val params = painel.layoutParams
        params.width = largura
        params.height = altura
        painel.layoutParams = params
    }

    private fun dimensoesAtuaisDaJanela(): Pair<Int?, Int?> {
        val largura = when {
            painel.layoutParams.width > 0 -> painel.layoutParams.width
            painel.width > 0 -> painel.width
            else -> null
        }
        val altura = when {
            painel.layoutParams.height > 0 -> painel.layoutParams.height
            painel.height > 0 -> painel.height
            else -> null
        }
        return largura to altura
    }

    private fun persistirDimensoesAtuais() {
        val (largura, altura) = dimensoesAtuaisDaJanela()
        if (largura == null || altura == null) return
        val id = idOrigemDaBiblioteca
        if (id != null) {
            aoPersistirDimensoes?.invoke(id, largura, altura)
        } else {
            larguraPendentePx = largura
            alturaPendentePx = altura
        }
    }

    /** A bolha nasce sobre [origem] (o botão tocado), e a janela encolhe em direção a ela. */
    private fun minimizar(origem: View? = null) {
        if (!temConteudo()) {
            esconderTudoSemAnimacao()
            return
        }
        if (origem != null && forma == Forma.ABERTA) centralizarSeloSobre(origem)
        encolher(mostrarBolha = true)
    }

    private fun centralizarSeloSobre(vista: View) {
        val pai = selo.parent as? View ?: return
        if (vista.width <= 0 || vista.height <= 0) return
        val origemDoPai = IntArray(2)
        val origemDaVista = IntArray(2)
        pai.getLocationOnScreen(origemDoPai)
        vista.getLocationOnScreen(origemDaVista)
        val lado = ladoDoSelo()
        posicionarSelo(
            origemDaVista[0] - origemDoPai[0] + vista.width / 2f - lado / 2f,
            origemDaVista[1] - origemDoPai[1] + vista.height / 2f - lado / 2f,
        )
    }

    private fun abrirJuntoDaBolha(xDaBolha: Float, yDaBolha: Float, ladoDaBolha: Float) {
        val pai = painel.parent as? View ?: return
        if (pai.width <= 0 || pai.height <= 0 || painel.width <= 0 || painel.height <= 0) return
        val lugar = PosicaoDaJanelaFlutuanteAoAbrirPelaBolhaNoCantoMaisProximo.calcular(
            xDaBolha,
            yDaBolha,
            ladoDaBolha,
            painel.width.toFloat(),
            painel.height.toFloat(),
            pai.width.toFloat(),
            pai.height.toFloat(),
        )
        painel.x = lugar.first
        painel.y = lugar.second
        posicaoDefinida = true
    }

    private fun ladoDoSelo(): Float =
        selo.height.takeIf { it > 0 }?.toFloat() ?: (56f * selo.resources.displayMetrics.density)

    /** Fechar pela aba do chat: [antesDeFechar] tira a aba só depois da confirmação (cancelar deixa tudo como está). */
    fun fecharPeloUsuario(antesDeFechar: () -> Unit) {
        fechar(antesDeFechar)
    }

    private fun fechar(antesDeFechar: () -> Unit = {}) {
        if (reservadoParaWarmStart || ehVisualizadorDeMidiaAtual()) {
            antesDeFechar()
            reservadoParaWarmStart = true
            recolherSemBolhaMantendoConteudo()
            return
        }
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(R.layout.layout_caixa_de_dialogo_do_editor_de_imagem, null)
        raiz.findViewById<TextView>(R.id.titulo_da_caixa_de_dialogo_do_editor).text =
            activity.getString(R.string.titulo_da_confirmacao_de_fechar_colecao_flutuante)
        val mensagem = raiz.findViewById<TextView>(R.id.mensagem_da_caixa_de_dialogo_do_editor)
        mensagem.visibility = View.VISIBLE
        mensagem.text = activity.getString(R.string.mensagem_da_confirmacao_de_fechar_colecao_flutuante)
        raiz.findViewById<EditText>(R.id.campo_da_caixa_de_dialogo_do_editor).visibility = View.GONE
        val cancelar = raiz.findViewById<Button>(R.id.botao_cancelar_da_caixa_de_dialogo_do_editor)
        val confirmar = raiz.findViewById<Button>(R.id.botao_confirmar_da_caixa_de_dialogo_do_editor)
        cancelar.text = activity.getString(R.string.cancelar_caixa_do_editor)
        cancelar.backgroundTintList = null
        cancelar.stateListAnimator = null
        confirmar.text = activity.getString(R.string.fechar_colecao_flutuante)
        confirmar.setTextColor(Color.parseColor("#F28B82"))
        confirmar.backgroundTintList = null
        confirmar.stateListAnimator = null
        cancelar.setOnClickListener { dialogo.dismiss() }
        confirmar.setOnClickListener {
            dialogo.dismiss()
            antesDeFechar()
            encerrar()
        }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun atualizarSelo() {
        val titulo = tituloDaPagina.trim()
        val inicial = titulo.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
        selo.text = inicial
        selo.contentDescription = titulo.ifBlank {
            activity.getString(R.string.descricao_do_selo_da_colecao_flutuante, 1)
        }
    }

    private fun excluirMarcados() {
        if (marcados.isEmpty()) return
        tirar(marcados.toList())
    }

    private fun limpar() {
        esconderTudo()
    }

    private fun marcarTodos() {
        marcados.clear()
        marcados.addAll(itens.map { it.endereco })
        desenhar()
    }

    private fun desenhar() {
        lista.removeAllViews()
        val densidade = lista.resources.displayMetrics.density
        itens.forEach { item -> lista.addView(bloco(item, densidade)) }
    }

    private fun bloco(item: ItemDaColecaoFlutuante, densidade: Float): View {
        val margem = (8 * densidade).toInt()
        val (largura, altura) = medidas(item.tamanho, densidade)
        val foto = ImageView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(largura, altura).apply {
                marginEnd = if (item.tamanho == TamanhoDoItemDaColecaoFlutuante.GRANDE) 0 else margem
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.parseColor("#242833"))
            tag = item.endereco
            contentDescription = item.titulo
            setOnClickListener { abrir(item) }
        }
        mostrarMiniatura(foto, item)
        val controles = linhaDeControles(item, densidade)
        if (item.tamanho != TamanhoDoItemDaColecaoFlutuante.GRANDE) {
            return LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, margem, 0, margem)
                addView(controles.caixa)
                addView(foto)
                addView(controles.textos)
                addView(controles.excluir)
            }
        }
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, margem, 0, margem)
            addView(foto)
            addView(LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, margem, 0, 0)
                addView(controles.caixa)
                addView(controles.textos)
                addView(controles.excluir)
            })
        }
    }

    private fun linhaDeControles(item: ItemDaColecaoFlutuante, densidade: Float): ControlesDaLinha {
        val caixa = CheckBox(activity).apply {
            isChecked = item.endereco in marcados
            setOnCheckedChangeListener { _, marcado ->
                if (marcado) marcados.add(item.endereco) else marcados.remove(item.endereco)
            }
        }
        val textos = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        textos.addView(TextView(activity).apply {
            text = item.titulo
            setTextColor(Color.parseColor("#F4F7FB"))
            textSize = 14f
            maxLines = 2
        })
        val detalhe = item.nota.ifBlank {
            if (item.tipo == TipoDoItemDaColecaoFlutuante.VIDEO) "vídeo" else ""
        }
        if (detalhe.isNotBlank()) {
            textos.addView(TextView(activity).apply {
                text = detalhe
                setTextColor(Color.parseColor("#8A9099"))
                textSize = 12f
                maxLines = 4
            })
        }
        val margem = (8 * densidade).toInt()
        val botaoExcluir = TextView(activity).apply {
            text = activity.getString(R.string.tirar_item_da_colecao)
            setTextColor(Color.parseColor("#F28B82"))
            textSize = 14f
            setPadding(margem, margem, margem, margem)
            minWidth = (64 * densidade).toInt()
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            setOnClickListener { this@PainelDaColecaoFlutuanteDoChatDoNavegador.tirar(listOf(item.endereco)) }
        }
        return ControlesDaLinha(caixa, textos, botaoExcluir)
    }

    private fun medidas(tamanho: TamanhoDoItemDaColecaoFlutuante, densidade: Float): Pair<Int, Int> {
        return when (tamanho) {
            TamanhoDoItemDaColecaoFlutuante.PEQUENO -> (72 * densidade).toInt() to (72 * densidade).toInt()
            TamanhoDoItemDaColecaoFlutuante.MEDIO -> (168 * densidade).toInt() to (94 * densidade).toInt()
            TamanhoDoItemDaColecaoFlutuante.GRANDE ->
                LinearLayout.LayoutParams.MATCH_PARENT to (200 * densidade).toInt()
        }
    }

    private fun abrir(item: ItemDaColecaoFlutuante) {
        if (item.tipo == TipoDoItemDaColecaoFlutuante.VIDEO) abrirVideo(item)
        else modal.mostrar(item.endereco)
    }

    private data class ControlesDaLinha(
        val caixa: CheckBox,
        val textos: LinearLayout,
        val excluir: TextView,
    )

    private fun mostrarMiniatura(foto: ImageView, item: ItemDaColecaoFlutuante) {
        miniaturas.get(item.endereco)?.let {
            foto.setImageBitmap(it)
            return
        }
        if (item.tipo == TipoDoItemDaColecaoFlutuante.VIDEO) {
            foto.setImageBitmap(miniaturaDeReserva())
        }
        val geracaoDesta = geracao
        val endereco = item.endereco
        filaDeMiniaturas.execute {
            val bitmap = if (item.tipo == TipoDoItemDaColecaoFlutuante.VIDEO) {
                quadroDoVideo(endereco)
            } else {
                baixarMiniatura(endereco)
            } ?: return@execute
            activity.runOnUiThread {
                if (activity.isDestroyed || geracaoDesta != geracao) return@runOnUiThread
                miniaturas.put(endereco, bitmap)
                if (foto.tag == endereco) foto.setImageBitmap(bitmap)
            }
        }
    }

    private fun miniaturaDeReserva(): Bitmap {
        reserva?.let { return it }
        val bitmap = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
        val tela = Canvas(bitmap)
        tela.drawColor(Color.parseColor("#242833"))
        val tinta = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F4F7FB") }
        val caminho = Path().apply {
            moveTo(58f, 46f)
            lineTo(58f, 114f)
            lineTo(118f, 80f)
            close()
        }
        tela.drawPath(caminho, tinta)
        reserva = bitmap
        return bitmap
    }

    private fun quadroDoVideo(endereco: String): Bitmap? {
        if (!endereco.startsWith("http://") && !endereco.startsWith("https://")) return null
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(endereco, hashMapOf("User-Agent" to "Mozilla/5.0"))
            retriever.getScaledFrameAtTime(
                1_000_000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                960,
                540,
            )
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun baixarMiniatura(endereco: String): Bitmap? {
        val bytes = when (ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.classificar(endereco)) {
            FonteDoEnderecoDaImagemDoToqueLongo.ARQUIVO_LOCAL ->
                CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.bytesDeArquivoLocal(endereco)
            FonteDoEnderecoDaImagemDoToqueLongo.HTTP -> try {
                val resposta = clienteHttp.newCall(Request.Builder().url(endereco).build()).execute()
                resposta.use { corpo ->
                    if (!corpo.isSuccessful) null
                    else corpo.body?.bytes()
                }
            } catch (_: Exception) {
                null
            }
            else -> null
        } ?: return null
        return try {
            if (bytes.isEmpty() || bytes.size > LIMITE_DA_MINIATURA) return null
            val medida = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, medida)
            var amostra = 1
            while (
                amostra < 32 &&
                (medida.outWidth / amostra > 960 || medida.outHeight / amostra > 960)
            ) {
                amostra *= 2
            }
            val desenho = BitmapFactory.Options().apply { inSampleSize = amostra }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, desenho)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val LIMITE_DA_MINIATURA = 8_000_000

        /** Moldura amarela na carta em foco (toque longo / modal), inclusive em HTML antigo da biblioteca. */
        private val SCRIPT_DE_DESTAQUE_DE_CARTA_NO_VISUALIZADOR_DE_MIDIA = """
            (function(){
              if (window.cbVizDestacarCarta) return;
              var grade = document.getElementById('grade');
              if (!grade) return;
              if (!document.getElementById('cb-viz-foco-style')) {
                var st = document.createElement('style');
                st.id = 'cb-viz-foco-style';
                st.textContent = '.carta.em-foco{outline:2px solid #f5c518;outline-offset:1px;z-index:3}';
                (document.head || document.documentElement).appendChild(st);
              }
              function limparQ(u) {
                if (!u) return '';
                var i = u.indexOf('?');
                return i >= 0 ? u.substring(0, i) : u;
              }
              function uriDaCarta(carta) {
                return carta.getAttribute('data-uri') || '';
              }
              window.cbVizLimparDestaqueCarta = function () {
                var cs = grade.querySelectorAll('.carta.em-foco');
                for (var i = 0; i < cs.length; i++) cs[i].classList.remove('em-foco');
              };
              window.cbVizDestacarCarta = function (uri) {
                window.cbVizLimparDestaqueCarta();
                if (!uri) return;
                var alvo = limparQ(uri);
                var cs = grade.querySelectorAll('.carta');
                for (var i = 0; i < cs.length; i++) {
                  if (limparQ(uriDaCarta(cs[i])) === alvo) {
                    cs[i].classList.add('em-foco');
                    break;
                  }
                }
              };
              window.cbVizAvisarLarguraEstavel = function () {
                document.body.classList.remove('cb-viz-aguarda-largura');
                try { window.dispatchEvent(new Event('resize')); } catch (e) {}
                if (window.cbVizPrefetchThumbs) window.cbVizPrefetchThumbs();
              };
              window.cbVizListarMidiasVisiveis = window.cbVizListarMidiasVisiveis || function () { return '[]'; };
              window.cbVizPrefetchThumbs = window.cbVizPrefetchThumbs || function () {};
            })();
        """.trimIndent()

        /**
         * Lift-and-drag estilo iPad: toque longo (~300ms) na `<img>` ou no `<video>` pausado
         * (captura o frame → file://) arma fantasma nativo; arrastar/soltar funde no editor.
         * Tela cheia do `<video>` abre o player nativo da gaveta.
         */
        private val SCRIPT_DE_ENVIO_DE_IMAGEM_PARA_CAMADA_DO_EDITOR = """
            (function(){
              if (window.__chromebrothersLiftDragImgV10) return;
              window.__chromebrothersLiftDragImgV10 = true;
              function limparQGlobal(u) {
                if (!u) return '';
                var i = u.indexOf('?');
                return i >= 0 ? u.substring(0, i) : u;
              }
              function emModoSelecao() {
                return document.body && document.body.classList.contains('selecao');
              }
              function uriDeImg(img) {
                // data-cb-uri = original sem ?cb_thumb= (gravado ao reescrever a grade).
                return img.getAttribute('data-cb-uri') || img.currentSrc || img.src || '';
              }
              function uriDeVideo(video) {
                return video.getAttribute('data-cb-uri') || video.currentSrc || video.src || '';
              }
              function temBridge(nome) {
                return window.chromebrothers && typeof chromebrothers[nome] === 'function';
              }
              function telaDe(toque) {
                return { x: toque.clientX, y: toque.clientY };
              }
              function abrirPlayerNativo(video) {
                var uri = uriDeVideo(video);
                if (!uri || !temBridge('abrirVideoNoPlayer')) return;
                try { chromebrothers.abrirVideoNoPlayer(uri); } catch (e) {}
              }
              function quadroDoVideoComoFileUri(video) {
                if (!video || video.readyState < 2) return '';
                if (video.videoWidth < 2 || video.videoHeight < 2) return '';
                try {
                  var c = document.createElement('canvas');
                  var maxLado = 1280;
                  var vw = video.videoWidth;
                  var vh = video.videoHeight;
                  var escala = Math.min(1, maxLado / Math.max(vw, vh));
                  c.width = Math.max(2, Math.round(vw * escala));
                  c.height = Math.max(2, Math.round(vh * escala));
                  c.getContext('2d').drawImage(video, 0, 0, c.width, c.height);
                  var dataUrl = c.toDataURL('image/jpeg', 0.82);
                  if (!dataUrl || dataUrl.length < 32) return '';
                  if (!temBridge('gravarQuadroDoVideoParaArraste')) return '';
                  return chromebrothers.gravarQuadroDoVideoParaArraste(dataUrl) || '';
                } catch (e) {
                  return '';
                }
              }
              function cartaDe(el) {
                var n = el;
                while (n) {
                  if (n.classList && n.classList.contains('carta')) return n;
                  n = n.parentElement;
                }
                return null;
              }
              function armarGestosDeArraste(el, obterUri, opcoes) {
                if (!el || el.__cbLiftDragImgV10) return;
                el.__cbLiftDragImgV10 = true;
                opcoes = opcoes || {};
                var ehVideo = !!opcoes.video;
                var uriToque = opcoes.uriToque || '';
                function destacarPorElemento() {
                  var u = uriToque || obterUri();
                  if (window.cbVizDestacarCarta && u) window.cbVizDestacarCarta(u);
                  else {
                    var c = cartaDe(el);
                    if (!c) return;
                    var cs = document.querySelectorAll('.carta.em-foco');
                    for (var i = 0; i < cs.length; i++) cs[i].classList.remove('em-foco');
                    c.classList.add('em-foco');
                  }
                }
                function tentarToqueSimples() {
                  if (emModoSelecao()) return;
                  if (ehVideo) {
                    var vu = uriToque || obterUri();
                    if (vu && temBridge('abrirVideoNoPlayer')) {
                      try { chromebrothers.abrirVideoNoPlayer(vu); } catch (e) {}
                    }
                    return;
                  }
                  var uri = uriToque || obterUri();
                  if (!uri || !temBridge('abrirVisorDaMidiaNoVisualizador')) return;
                  var idx = 0;
                  var listaFiltrada = '[]';
                  try {
                    var brutos = JSON.parse(
                      window.cbVizListarMidiasVisiveis
                        ? window.cbVizListarMidiasVisiveis()
                        : '[]',
                    );
                    var imgs = [];
                    var alvo = limparQGlobal(uri);
                    for (var i = 0; i < brutos.length; i++) {
                      if (brutos[i].tipo === 'imagem') imgs.push(brutos[i]);
                    }
                    for (var j = 0; j < imgs.length; j++) {
                      if (limparQGlobal(imgs[j].uri) === alvo) { idx = j; break; }
                    }
                    listaFiltrada = JSON.stringify(imgs);
                  } catch (e) {}
                  try {
                    chromebrothers.abrirVisorDaMidiaNoVisualizador(uri, listaFiltrada, String(idx));
                  } catch (e) {}
                }
                var espera = null;
                var arrastando = false;
                var longoDisparado = false;
                var moveuScroll = false;
                var uriArraste = '';
                var ultimoX = 0;
                var ultimoY = 0;
                var inicioX = 0;
                var inicioY = 0;
                var noMoveEspera = null;
                function limparEspera() {
                  if (espera) { clearTimeout(espera); espera = null; }
                  if (noMoveEspera) {
                    document.removeEventListener('touchmove', noMoveEspera, true);
                    noMoveEspera = null;
                  }
                }
                function noMove(evento) {
                  if (!arrastando || !evento.touches || !evento.touches[0]) return;
                  try { evento.preventDefault(); } catch (e) {}
                  var tela = telaDe(evento.touches[0]);
                  ultimoX = tela.x;
                  ultimoY = tela.y;
                  try {
                    if (temBridge('atualizarArrasteDeImagemParaOEditor')) {
                      chromebrothers.atualizarArrasteDeImagemParaOEditor(String(tela.x), String(tela.y));
                    }
                  } catch (e) {}
                }
                function noFim(evento) {
                  document.removeEventListener('touchmove', noMove, true);
                  document.removeEventListener('touchend', noFim, true);
                  document.removeEventListener('touchcancel', noFim, true);
                  limparEspera();
                  if (!arrastando) return;
                  arrastando = false;
                  var uri = uriArraste;
                  uriArraste = '';
                  var x = ultimoX;
                  var y = ultimoY;
                  if (evento && evento.changedTouches && evento.changedTouches[0]) {
                    var tela = telaDe(evento.changedTouches[0]);
                    x = tela.x;
                    y = tela.y;
                  }
                  try {
                    if (uri && temBridge('soltarArrasteDeImagemParaOEditor')) {
                      chromebrothers.soltarArrasteDeImagemParaOEditor(uri, String(x), String(y));
                    }
                  } catch (e) {}
                }
                function iniciarSePreciso(tela) {
                  if (arrastando) return;
                  if (!uriArraste) return;
                  arrastando = true;
                  document.addEventListener('touchmove', noMove, { capture: true, passive: false });
                  document.addEventListener('touchend', noFim, true);
                  document.addEventListener('touchcancel', noFim, true);
                  try {
                    if (temBridge('iniciarArrasteDeImagemParaOEditor')) {
                      chromebrothers.iniciarArrasteDeImagemParaOEditor(
                        uriArraste, String(tela.x), String(tela.y));
                    }
                  } catch (e) {}
                }
                el.addEventListener('contextmenu', function (evento) {
                  evento.preventDefault();
                  evento.stopPropagation();
                });
                el.addEventListener('dragstart', function (evento) {
                  evento.preventDefault();
                });
                el.addEventListener('touchstart', function (evento) {
                  if (!evento.touches || !evento.touches[0]) return;
                  arrastando = false;
                  longoDisparado = false;
                  moveuScroll = false;
                  uriArraste = '';
                  var tela = telaDe(evento.touches[0]);
                  ultimoX = tela.x;
                  ultimoY = tela.y;
                  inicioX = tela.x;
                  inicioY = tela.y;
                  limparEspera();
                  noMoveEspera = function (ev) {
                    if (!espera || !ev.touches || !ev.touches[0]) return;
                    var t = telaDe(ev.touches[0]);
                    ultimoX = t.x;
                    ultimoY = t.y;
                    var dx = t.x - inicioX;
                    var dy = t.y - inicioY;
                    if ((dx * dx + dy * dy) > 196) {
                      moveuScroll = true;
                      limparEspera();
                    }
                  };
                  document.addEventListener('touchmove', noMoveEspera, { capture: true, passive: true });
                  espera = setTimeout(function () {
                    espera = null;
                    longoDisparado = true;
                    if (noMoveEspera) {
                      document.removeEventListener('touchmove', noMoveEspera, true);
                      noMoveEspera = null;
                    }
                    uriArraste = obterUri() || '';
                    if (!uriArraste) return;
                    destacarPorElemento();
                    iniciarSePreciso({ x: ultimoX, y: ultimoY });
                  }, 300);
                }, { passive: true });
                el.addEventListener('touchend', function () {
                  var eraLongo = longoDisparado;
                  var eraArraste = arrastando;
                  limparEspera();
                  if (!eraLongo && !eraArraste && !moveuScroll) tentarToqueSimples();
                  moveuScroll = false;
                  longoDisparado = false;
                });
                el.addEventListener('touchcancel', function () {
                  limparEspera();
                  moveuScroll = false;
                  longoDisparado = false;
                });
              }
              function armarImg(img) {
                var carta = cartaDe(img);
                if (carta && carta.getAttribute('data-tipo') === 'video') {
                  // Capa do vídeo na grade: toque abre o player; não arrasta a capa como imagem.
                  armarGestosDeArraste(img, function () { return ''; }, {
                    video: true,
                    uriToque: uriDeImg(img),
                  });
                  return;
                }
                armarGestosDeArraste(img, function () { return uriDeImg(img); }, {
                  uriToque: uriDeImg(img),
                });
              }
              function armarVideo(video) {
                if (!video || video.__cbVideoFsEDrag) return;
                video.__cbVideoFsEDrag = true;
                armarGestosDeArraste(video, function () {
                  if (!video.paused) return '';
                  return quadroDoVideoComoFileUri(video);
                }, {
                  video: true,
                  uriToque: uriDeVideo(video),
                });
                function irAoPlayer(evento) {
                  if (evento) {
                    try { evento.preventDefault(); evento.stopPropagation(); } catch (e) {}
                  }
                  abrirPlayerNativo(video);
                }
                video.addEventListener('webkitbeginfullscreen', irAoPlayer);
                video.addEventListener('beginfullscreen', irAoPlayer);
                if (!video.__cbFsPatched) {
                  video.__cbFsPatched = true;
                  var rf = video.requestFullscreen || video.webkitRequestFullscreen || video.mozRequestFullScreen;
                  if (typeof rf === 'function') {
                    video.requestFullscreen = function () { irAoPlayer(); return Promise.resolve(); };
                    video.webkitRequestFullscreen = function () { irAoPlayer(); };
                  }
                  if (typeof video.webkitEnterFullscreen === 'function') {
                    video.webkitEnterFullscreen = function () { irAoPlayer(); };
                  }
                }
              }
              function armarNo(no) {
                if (!no || no.nodeType !== 1) return;
                if (no.tagName === 'IMG') armarImg(no);
                else if (no.tagName === 'VIDEO') armarVideo(no);
                if (no.getElementsByTagName) {
                  var imgs = no.getElementsByTagName('img');
                  for (var i = 0; i < imgs.length; i++) armarImg(imgs[i]);
                  var videos = no.getElementsByTagName('video');
                  for (var j = 0; j < videos.length; j++) armarVideo(videos[j]);
                }
              }
              function varrer() {
                armarNo(document.body || document.documentElement);
              }
              varrer();
              var raiz = document.documentElement || document.body;
              if (raiz) {
                new MutationObserver(function (muts) {
                  for (var m = 0; m < muts.length; m++) {
                    var lista = muts[m].addedNodes;
                    for (var n = 0; n < lista.length; n++) armarNo(lista[n]);
                  }
                }).observe(raiz, { childList: true, subtree: true });
              }
            })();
        """.trimIndent()
    }

    /**
     * No Visualizador, após o HTML montar o callback, empurra Downloads+Pictures/ChromeBrothers
     * no mesmo formato de escolherPasta — sem depender do usuário abrir o seletor SAF.
     */
    private fun agendarAlimentacaoDoVisualizadorComPastaVirtualDeMidia() {
        if (!ehVisualizadorDeMidiaAtual()) return
        val listar = aoListarPastaDoAparelho ?: return
        val geracao = ++geracaoDaAlimentacaoDoVisualizador
        painel.postDelayed({
            if (encerrada || geracao != geracaoDaAlimentacaoDoVisualizador) return@postDelayed
            if (!deveReceberAtualizacaoDaPastaVirtualDeMidia()) return@postDelayed
            Thread {
                val json = runCatching {
                    listar(
                        com.chromebrothers.navegador.regras
                            .PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao.URI,
                    )
                }.getOrNull() ?: return@Thread
                if (encerrada || geracao != geracaoDaAlimentacaoDoVisualizador) return@Thread
                activity.runOnUiThread {
                    if (encerrada || geracao != geracaoDaAlimentacaoDoVisualizador) return@runOnUiThread
                    if (!deveReceberAtualizacaoDaPastaVirtualDeMidia()) return@runOnUiThread
                    entregarJsonDaPastaEscolhida(json)
                }
            }.start()
        }, 40)
    }

    /**
     * Grade do Visualizador de mídia usa `img.src = content://` full-res.
     * Reescreve para `?cb_thumb=512` (intercept nativo devolve JPEG leve).
     */
    private fun injetarScriptDeMiniaturasNaGradeDoVisualizador() {
        webDaPagina.evaluateJavascript(
            """
            (function () {
              if (window.__cbMiniaturasGrade) return;
              window.__cbMiniaturasGrade = true;
              function comThumb(uri) {
                if (!uri) return uri;
                if (uri.indexOf('cb_thumb=') >= 0) return uri;
                if (uri.indexOf('content:') !== 0 && uri.indexOf('file:') !== 0) return uri;
                return uri + (uri.indexOf('?') >= 0 ? '&' : '?') + 'cb_thumb=512';
              }
              function naCarta(img) {
                var n = img;
                while (n) {
                  if (n.classList && n.classList.contains('carta')) return true;
                  if (n.classList && n.classList.contains('visor')) return false;
                  n = n.parentElement;
                }
                return false;
              }
              function aplicar(img) {
                if (!img || img.__cbThumb) return;
                if (!naCarta(img)) return;
                var u = img.getAttribute('src') || img.src || '';
                if (!u) return;
                if (u.indexOf('content:') !== 0 && u.indexOf('file:') !== 0) return;
                img.__cbThumb = true;
                if (!img.getAttribute('data-cb-uri')) img.setAttribute('data-cb-uri', u);
                var novo = comThumb(u);
                if (novo !== u) img.src = novo;
              }
              function varrer(raiz) {
                if (!raiz) return;
                if (raiz.tagName === 'IMG') aplicar(raiz);
                var imgs = (raiz.querySelectorAll ? raiz.querySelectorAll('.carta img') : []);
                for (var i = 0; i < imgs.length; i++) aplicar(imgs[i]);
              }
              varrer(document);
              var obs = new MutationObserver(function (muts) {
                for (var m = 0; m < muts.length; m++) {
                  var nos = muts[m].addedNodes;
                  for (var n = 0; n < nos.length; n++) varrer(nos[n]);
                }
              });
              obs.observe(document.documentElement || document.body, { childList: true, subtree: true });
            })();
            """.trimIndent(),
            null,
        )
    }
}

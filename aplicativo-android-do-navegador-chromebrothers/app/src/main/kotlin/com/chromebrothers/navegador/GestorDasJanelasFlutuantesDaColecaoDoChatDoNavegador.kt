package com.chromebrothers.navegador

import android.graphics.Bitmap
import android.net.Uri
import android.view.DragEvent
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chromebrothers.navegador.regras.ArrasteDeComponenteEntreAFaixaDoChatEAJanelaFlutuante.FaseDoArrasteDaJanelaPelaBarra
import com.chromebrothers.navegador.regras.BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador
import com.chromebrothers.navegador.regras.ItemDaBibliotecaDeComponentesHtmlDaColecao
import com.chromebrothers.navegador.regras.ItemDaColecaoFlutuante
import com.chromebrothers.navegador.regras.LimiteDeJanelasFlutuantesDaColecaoDoChat

class GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador(
    private val activity: AppCompatActivity,
    private val baixar: (List<ItemDaColecaoFlutuante>) -> Unit,
    private val abrirVideo: (ItemDaColecaoFlutuante) -> Unit,
    private val abrirEnderecoDaPagina: (String) -> Unit = {},
) {
    private val janelas = mutableListOf<PainelDaColecaoFlutuanteDoChatDoNavegador>()
    private var janelaFocada: PainelDaColecaoFlutuanteDoChatDoNavegador? = null
    private val entregaDoChat =
        com.chromebrothers.navegador.regras.EntregaDaColecaoPorRespostaDoChat<PainelDaColecaoFlutuanteDoChatDoNavegador>()
    private var aoEnviarMensagemAoChat: ((String) -> Unit)? = null
    private var aoLerUrlDaAbaAtiva: (() -> String)? = null
    private var aoLerTituloDaAbaAtiva: (() -> String)? = null
    private var aoLerTextoSelecionadoDaAba: (((String) -> Unit) -> Unit)? = null
    private var aoAbrirNaAba: ((String) -> Unit)? = null
    private var aoRodarRoteiro: ((String, (String) -> Unit) -> Unit)? = null
    private var aoCopiarTexto: ((String) -> Unit)? = null
    private var aoGuardarNaBiblioteca: (() -> String)? = null
    private var aoPersistirDimensoes: ((id: String, larguraPx: Int, alturaPx: Int) -> Unit)? = null
    private var aoPedirArquivoDoAparelho: (
        (
            ValueCallback<Array<Uri>>,
            WebChromeClient.FileChooserParams,
        ) -> Boolean
    )? = null
    private var aoEscolherPastaDoAparelho: ((PainelDaColecaoFlutuanteDoChatDoNavegador) -> Unit)? = null
    private var aoListarPastaDoAparelho: ((String) -> String)? = null
    private var aoExcluirArquivosDoAparelho: ((String) -> String)? = null
    private var aoAbrirImagemNaAba: ((String) -> String)? = null
    private var aoAbrirImagemNoVisor: ((String) -> String)? = null
    private var aoAbrirImagemNoEditor: ((String) -> String)? = null
    private var aoEnviarImagemParaCamadaDoEditor: ((String) -> String)? = null
    private var aoForcarEncerrarArrasteDeImagemDoComponente: (() -> Unit)? = null
    private var aoIniciarArrasteComMiniatura: ((String, Float, Float, android.graphics.Bitmap?) -> Unit)? = null
    private var aoAbrirVisorExternoNaGaveta: ((List<String>, Int) -> Unit)? = null
    private var aoIniciarArrasteDeImagemParaOEditor: ((String, Float, Float) -> Unit)? = null
    private var aoAtualizarArrasteDeImagemParaOEditor: ((Float, Float) -> Unit)? = null
    private var aoSoltarArrasteDeImagemParaOEditor: ((String, Float, Float) -> String)? = null
    private var aoGuardarImagem: ((String) -> String)? = null
    private var aoCopiarImagem: ((String) -> String)? = null
    private var aoAbrirVideoNoPlayer: ((String) -> String)? = null
    private var servicoDeGeracaoDeImagemViaMcp: ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers? = null
    private var aoEscolherImagemDeReferenciaDaGeracao:
        ((PainelDaColecaoFlutuanteDoChatDoNavegador, String) -> Unit)? = null
    private var aoAbrirVideosAoLado:
        ((PainelDaColecaoFlutuanteDoChatDoNavegador, String, Int) -> Unit)? = null
    private var videosDaGradeDoVisualizador:
        Pair<(List<String>, Int) -> Unit, (String, () -> Unit) -> Unit>? = null
    private var aoTerminarGeracaoDeImagemViaMcp: (() -> Unit)? = null
    private var aoSoltarItemDaBibliotecaParaReferenciaMcp:
        ((PainelDaColecaoFlutuanteDoChatDoNavegador, DragEvent, String) -> Boolean)? = null
    private var aoAncorarNaGaveta: ((PainelDaColecaoFlutuanteDoChatDoNavegador) -> Unit)? = null

    fun ligar() = Unit

    fun ligarAncorarNaGaveta(acao: (PainelDaColecaoFlutuanteDoChatDoNavegador) -> Unit) {
        aoAncorarNaGaveta = acao
        janelas.forEach { janela ->
            janela.ligarAncorarNaGaveta { acao(janela) }
        }
    }

    private var aoArrastarPelaBarra: ((Float, Float, FaseDoArrasteDaJanelaPelaBarra) -> Boolean)? = null

    fun ligarArrastePelaBarra(acao: (Float, Float, FaseDoArrasteDaJanelaPelaBarra) -> Boolean) {
        aoArrastarPelaBarra = acao
        janelas.forEach { it.ligarArrastePelaBarra(acao) }
    }

    fun ligarEnvioAoChat(enviar: (String) -> Unit) {
        aoEnviarMensagemAoChat = enviar
        janelas.forEach { it.ligarEnvioAoChat(enviar) }
    }

    fun ligarSeletorDeArquivoDoAparelho(
        pedir: (
            ValueCallback<Array<Uri>>,
            WebChromeClient.FileChooserParams,
        ) -> Boolean,
    ) {
        aoPedirArquivoDoAparelho = pedir
        janelas.forEach { it.ligarSeletorDeArquivoDoAparelho(pedir) }
    }

    fun limparCallbackDeArquivoSeFor(callback: ValueCallback<Array<Uri>>) {
        janelas.forEach { it.limparCallbackDeArquivoSeFor(callback) }
    }

    fun ligarEscolherPastaDoAparelho(escolher: (PainelDaColecaoFlutuanteDoChatDoNavegador) -> Unit) {
        aoEscolherPastaDoAparelho = escolher
        janelas.forEach { janela ->
            janela.ligarEscolherPastaDoAparelho { escolher(janela) }
        }
    }

    fun ligarListarPastaDoAparelho(listar: (String) -> String) {
        aoListarPastaDoAparelho = listar
        janelas.forEach { it.ligarListarPastaDoAparelho(listar) }
    }

    fun ligarExcluirArquivosDoAparelho(excluir: (String) -> String) {
        aoExcluirArquivosDoAparelho = excluir
        janelas.forEach { it.ligarExcluirArquivosDoAparelho(excluir) }
    }

    fun entregarPastaVirtualDeMidiaDoChromeBrothersAosVisualizadores(json: String) {
        janelas.filter { it.deveReceberAtualizacaoDaPastaVirtualDeMidia() }
            .forEach { it.entregarJsonDaPastaEscolhida(json) }
    }

    fun destacarCartaDoVisualizadorNasJanelas(uriTexto: String) {
        janelas.forEach { it.destacarCartaDoVisualizador(uriTexto) }
    }

    fun limparDestaqueDeCartaDoVisualizadorNasJanelas() {
        janelas.forEach { it.limparDestaqueDeCartaDoVisualizador() }
    }

    fun entregarCardAtualizadoAosGeradores(tipo: String, json: String) {
        janelas.forEach { it.entregarCardAtualizadoAoGerador(tipo, json) }
    }

    fun uriDoVisorInternoAbertoNasJanelas(): String? =
        janelas.firstNotNullOfOrNull { it.uriDoVisorInternoAberto() }

    fun esconderVisorInternoDuranteArrasteNasJanelas() {
        janelas.forEach { it.esconderVisorInternoDuranteArraste() }
    }

    fun restaurarVisorInternoAposArrasteNasJanelas(fechar: Boolean) {
        janelas.forEach { it.restaurarVisorInternoAposArraste(fechar) }
    }

    fun notificarHostRedimensionadoNasJanelas() {
        janelas.forEach { it.avisarWebViewRedimensionada() }
    }

    fun ligarAbrirImagemNaAba(abrir: (String) -> String) {
        aoAbrirImagemNaAba = abrir
        janelas.forEach { it.ligarAbrirImagemNaAba(abrir) }
    }

    fun ligarAbrirImagemNoVisor(abrir: (String) -> String) {
        aoAbrirImagemNoVisor = abrir
        janelas.forEach { it.ligarAbrirImagemNoVisor(abrir) }
    }

    fun ligarAbrirImagemNoEditor(abrir: (String) -> String) {
        aoAbrirImagemNoEditor = abrir
        janelas.forEach { it.ligarAbrirImagemNoEditor(abrir) }
    }

    fun ligarEnviarImagemParaCamadaDoEditor(enviar: (String) -> String) {
        aoEnviarImagemParaCamadaDoEditor = enviar
        janelas.forEach { it.ligarEnviarImagemParaCamadaDoEditor(enviar) }
    }

    fun ligarIniciarArrasteComMiniatura(iniciar: (String, Float, Float, android.graphics.Bitmap?) -> Unit) {
        aoIniciarArrasteComMiniatura = iniciar
        janelas.forEach { it.ligarIniciarArrasteComMiniatura(iniciar) }
    }

    fun ligarAbrirVisorExternoNaGaveta(abrir: (List<String>, Int) -> Unit) {
        aoAbrirVisorExternoNaGaveta = abrir
        janelas.forEach { it.ligarAbrirVisorExternoNaGaveta(abrir) }
    }

    fun ligarForcarEncerrarArrasteDeImagemDoComponente(encerrar: () -> Unit) {
        aoForcarEncerrarArrasteDeImagemDoComponente = encerrar
        janelas.forEach { it.ligarForcarEncerrarArrasteDeImagemDoComponente(encerrar) }
    }

    fun ligarIniciarArrasteDeImagemParaOEditor(iniciar: (String, Float, Float) -> Unit) {
        aoIniciarArrasteDeImagemParaOEditor = iniciar
        janelas.forEach { it.ligarIniciarArrasteDeImagemParaOEditor(iniciar) }
    }

    fun ligarAtualizarArrasteDeImagemParaOEditor(atualizar: (Float, Float) -> Unit) {
        aoAtualizarArrasteDeImagemParaOEditor = atualizar
        janelas.forEach { it.ligarAtualizarArrasteDeImagemParaOEditor(atualizar) }
    }

    fun ligarSoltarArrasteDeImagemParaOEditor(soltar: (String, Float, Float) -> String) {
        aoSoltarArrasteDeImagemParaOEditor = soltar
        janelas.forEach { it.ligarSoltarArrasteDeImagemParaOEditor(soltar) }
    }

    fun ligarGuardarImagem(guardar: (String) -> String) {
        aoGuardarImagem = guardar
        janelas.forEach { it.ligarGuardarImagem(guardar) }
    }

    fun ligarCopiarImagem(copiar: (String) -> String) {
        aoCopiarImagem = copiar
        janelas.forEach { it.ligarCopiarImagem(copiar) }
    }

    fun ligarAbrirVideoNoPlayer(abrir: (String) -> String) {
        aoAbrirVideoNoPlayer = abrir
        janelas.forEach { it.ligarAbrirVideoNoPlayer(abrir) }
    }

    fun ligarServicoDeGeracaoDeImagemViaMcp(servico: ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers) {
        servicoDeGeracaoDeImagemViaMcp = servico
        janelas.forEach { it.ligarServicoDeGeracaoDeImagemViaMcp(servico) }
    }

    fun ligarAbrirVideosAoLado(abrir: (PainelDaColecaoFlutuanteDoChatDoNavegador, String, Int) -> Unit) {
        aoAbrirVideosAoLado = abrir
        janelas.forEach { janela ->
            janela.ligarAbrirVideosAoLado { itens, indice -> abrir(janela, itens, indice) }
        }
    }

    fun ligarVideosDaGradeDoVisualizador(
        abrirAoLado: (uris: List<String>, indice: Int) -> Unit,
        mostrarMenu: (uri: String, reproduzirAqui: () -> Unit) -> Unit,
    ) {
        videosDaGradeDoVisualizador = abrirAoLado to mostrarMenu
        janelas.forEach { it.ligarVideosDaGradeDoVisualizador(abrirAoLado, mostrarMenu) }
    }

    fun ligarEscolherImagemDeReferenciaDaGeracao(
        escolher: (PainelDaColecaoFlutuanteDoChatDoNavegador, String) -> Unit,
    ) {
        aoEscolherImagemDeReferenciaDaGeracao = escolher
        janelas.forEach { janela ->
            janela.ligarEscolherImagemDeReferenciaDaGeracao { origem -> escolher(janela, origem) }
        }
    }

    fun ligarAoTerminarGeracaoDeImagemViaMcp(terminou: () -> Unit) {
        aoTerminarGeracaoDeImagemViaMcp = terminou
        janelas.forEach { it.ligarAoTerminarGeracaoDeImagemViaMcp(terminou) }
    }

    fun ligarSoltarItemDaBibliotecaParaReferenciaMcp(
        soltar: (PainelDaColecaoFlutuanteDoChatDoNavegador, DragEvent, String) -> Boolean,
    ) {
        aoSoltarItemDaBibliotecaParaReferenciaMcp = soltar
        janelas.forEach { janela ->
            janela.ligarSoltarItemDaBibliotecaParaReferenciaMcp { evento, tipo -> soltar(janela, evento, tipo) }
        }
    }

    fun ligarUrlDaAbaAtiva(ler: () -> String) {
        aoLerUrlDaAbaAtiva = ler
        janelas.forEach { it.ligarUrlDaAbaAtiva(ler) }
    }

    fun ligarTituloDaAbaAtiva(ler: () -> String) {
        aoLerTituloDaAbaAtiva = ler
        janelas.forEach { it.ligarTituloDaAbaAtiva(ler) }
    }

    fun ligarTextoSelecionadoDaAba(ler: ((String) -> Unit) -> Unit) {
        aoLerTextoSelecionadoDaAba = ler
        janelas.forEach { it.ligarTextoSelecionadoDaAba(ler) }
    }

    fun ligarAbrirNaAba(abrir: (String) -> Unit) {
        aoAbrirNaAba = abrir
        janelas.forEach { it.ligarAbrirNaAba(abrir) }
    }

    fun ligarRodarRoteiro(rodar: (idOuNome: String, aoFim: (String) -> Unit) -> Unit) {
        aoRodarRoteiro = rodar
        janelas.forEach { it.ligarRodarRoteiro(rodar) }
    }

    fun ligarCopiarTexto(copiar: (String) -> Unit) {
        aoCopiarTexto = copiar
        janelas.forEach { it.ligarCopiarTexto(copiar) }
    }

    fun ligarGuardarNaBiblioteca(guardar: () -> String) {
        aoGuardarNaBiblioteca = guardar
        janelas.forEach { it.ligarGuardarNaBiblioteca(guardar) }
    }

    fun ligarPersistenciaDeDimensoes(aoPersistir: (id: String, larguraPx: Int, alturaPx: Int) -> Unit) {
        aoPersistirDimensoes = aoPersistir
        janelas.forEach { it.ligarPersistenciaDeDimensoes(aoPersistir) }
    }

    /**
     * Durante uma resposta do chat ([iniciarEntregaDoChat]), publicar de novo atualiza a mesma
     * janela e ela fica escondida até [concluirEntregaDoChat]. Fora disso, abre e mostra na hora.
     */
    fun publicarPagina(titulo: String, html: String): String {
        val mostrarAgora = !entregaDoChat.emAndamento
        val reaproveitada = entregaDoChat.reaproveitar { it in janelas }
        // A janela escondida ainda não conta como visível, mas o limite vale porque ela aparece no fim.
        val janela = reaproveitada
            ?: abrirJanela()
            ?: return LimiteDeJanelasFlutuantesDaColecaoDoChat.mensagemRecusada()
        entregaDoChat.guardar(janela)
        val resultado = janela.publicarPagina(titulo, html, mostrarAgora)
        if (reaproveitada == null || mostrarAgora) return resultado
        return "$resultado Atualizei a mesma janela desta resposta; ela aparece para o usuário quando você terminar."
    }

    /**
     * Edita a página de uma janela (a de [titulo] ou a em foco) aplicando [editar] ao HTML.
     * Mantém a janela; se for a entrega escondida desta resposta, continua escondida.
     */
    fun editarPagina(
        titulo: String,
        editar: (String) -> com.chromebrothers.navegador.regras.ResultadoDaEdicaoPorTrechos,
    ): String {
        val janela = if (titulo.isBlank()) {
            janelaFocada?.takeIf { it.tituloEHtml() != null } ?: janelas.lastOrNull { it.tituloEHtml() != null }
        } else {
            janelas.lastOrNull { it.tituloEHtml()?.first == titulo }
        } ?: return if (titulo.isBlank()) "Não há página aberta na coleção." else "Não achei a janela \"$titulo\" na coleção."
        val (nome, html) = janela.tituloEHtml() ?: return "Não há página aberta na coleção."
        return when (val resultado = editar(html)) {
            is com.chromebrothers.navegador.regras.ResultadoDaEdicaoPorTrechos.Recusada -> resultado.mensagem
            is com.chromebrothers.navegador.regras.ResultadoDaEdicaoPorTrechos.Ok -> {
                val escondida = entregaDoChat.emAndamento && entregaDoChat.janela === janela
                janela.publicarPagina(nome, resultado.html, mostrarAgora = !escondida)
                "Editei \"$nome\" (${resultado.quantas} troca(s)). A página foi recarregada; use guardar_componente_da_colecao se quiser gravar na biblioteca."
            }
        }
    }

    fun iniciarEntregaDoChat() {
        entregaDoChat.concluir()?.revelar()
        entregaDoChat.iniciar()
    }

    fun concluirEntregaDoChat() {
        val janela = entregaDoChat.concluir() ?: return
        if (janela !in janelas) return
        janela.revelar()
        focar(janela)
    }

    fun carregarComponenteDaBiblioteca(item: ItemDaBibliotecaDeComponentesHtmlDaColecao): String {
        val existente = janelas.findLast { it.idOrigemDaBibliotecaAtual() == item.id }
        if (existente != null) {
            existente.revelarSeEstiverOculta()
            focar(existente)
            return "Abri o componente \"${item.nome}\" na coleção."
        }
        val janela = abrirJanela() ?: return LimiteDeJanelasFlutuantesDaColecaoDoChat.mensagemRecusada()
        return janela.carregarComponenteDaBiblioteca(item)
    }

    /**
     * Pré-carrega componentes elegíveis (MCP + Visualizador / tag warm-start) invisíveis.
     * [ligado]=false encerra as reservas warm-start existentes.
     */
    fun sincronizarWarmStart(
        itens: List<ItemDaBibliotecaDeComponentesHtmlDaColecao>,
        ligado: Boolean,
    ) {
        if (!ligado) {
            janelas.filter { it.estaReservadoParaWarmStart() && !it.estaVisivelNaColecao() }
                .toList()
                .forEach { it.encerrar() }
            return
        }
        val elegiveis = com.chromebrothers.navegador.regras.WarmStartDeComponentesDaColecaoDoNavegador
            .filtrarParaPreCarga(itens, preferenciaGlobalLigada = true)
        elegiveis.forEach { item ->
            if (janelas.any { it.idOrigemDaBibliotecaAtual() == item.id }) return@forEach
            val janela = abrirJanelaParaWarmStart() ?: return@forEach
            janela.carregarComponenteEmWarmStart(item)
        }
    }

    fun descrever(): String {
        if (janelas.isEmpty()) return "A coleção está vazia."
        return janelas.joinToString("\n") { janela ->
            val foco = if (janela === janelaFocada) "em foco" else "ao fundo"
            "- ${janela.resumoParaLista()} — $foco"
        }
    }

    fun guardarNaBiblioteca(
        biblioteca: BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador,
        gravacao: GravacaoDaBibliotecaDeComponentesHtmlDaColecaoNoArmazenamentoDoAplicativo,
        agoraMs: Long,
        descricao: String? = null,
        tags: List<String>? = null,
        novoId: () -> String,
    ): String {
        val janela = janelaFocada ?: return "Não há janela da coleção aberta para guardar."
        return janela.guardarNaBiblioteca(
            biblioteca,
            gravacao,
            agoraMs,
            descricao,
            tags,
            novoId,
        )
    }

    fun colocar(novos: List<ItemDaColecaoFlutuante>): String {
        val janela = janelaFocada ?: return "Não há janela da coleção aberta."
        return janela.colocar(novos)
    }

    fun idOrigemDaBibliotecaAtual(): String? = janelaFocada?.idOrigemDaBibliotecaAtual()

    fun limparVinculoBiblioteca() {
        janelaFocada?.limparVinculoBiblioteca()
    }

    fun limparVinculoDaOrigem(id: String) {
        janelas.forEach { janela ->
            if (janela.idOrigemDaBibliotecaAtual() == id) janela.limparVinculoBiblioteca()
        }
    }

    fun janelaDaOrigem(id: String): PainelDaColecaoFlutuanteDoChatDoNavegador? =
        janelas.findLast { it.idOrigemDaBibliotecaAtual() == id }

    fun focarSeOrigemFor(id: String): Boolean {
        val existente = janelas.findLast { it.idOrigemDaBibliotecaAtual() == id } ?: return false
        focar(existente)
        return true
    }

    fun bitmapVisivelDaWebView(): Bitmap? = janelaFocada?.bitmapVisivelDaWebView()

    fun quandoAPaginaPuderSerCapturada(aoPronto: () -> Unit) {
        val janela = janelaFocada
        if (janela == null) {
            aoPronto()
            return
        }
        janela.revelar()
        janela.quandoAPaginaPuderSerCapturada(aoPronto)
    }

    fun encerrarTodas() {
        janelas.toList().forEach { it.encerrar() }
    }

    /** Libera foco dos WebViews da coleção para o paste ir à página (Gecko). */
    fun liberarFocoParaColarNaPagina() {
        janelas.forEach { it.liberarFocoParaColarNaPagina() }
    }

    fun restaurarFocoAposColarNaPagina() {
        janelas.forEach { it.restaurarFocoAposColarNaPagina() }
    }

    /** Visualizador fica semi-transparente e sem hit-test; MCP continua recebendo drop. */
    fun liberarPaginaSobArrasteDoComponente() {
        janelas.forEach { it.liberarPaginaSobArrasteDoComponente() }
    }

    fun restaurarPaginaAposArrasteDoComponente() {
        janelas.forEach { it.restaurarPaginaAposArrasteDoComponente() }
    }

    /** True se o ponto (tela) cai sobre alguma janela flutuante aberta da coleção. */
    fun pontoSobreAlgumaJanelaDaColecao(xNaTela: Float, yNaTela: Float): Boolean =
        janelas.any { it.contemPontoNaTela(xNaTela, yNaTela) }

    /** True se o ponto (tela) cai sobre um Visualizador de mídia aberto, mesmo liberado para a página no arraste. */
    fun pontoSobreVisualizadorDeMidia(xNaTela: Float, yNaTela: Float): Boolean =
        janelas.any { it.ehVisualizadorDeMidia() && it.contemPontoNaTela(xNaTela, yNaTela, mesmoLiberada = true) }

    /** Janela da coleção mais acima sob o ponto (tela), ou null. */
    fun painelSobPonto(xNaTela: Float, yNaTela: Float): PainelDaColecaoFlutuanteDoChatDoNavegador? =
        janelas.findLast { it.contemPontoNaTela(xNaTela, yNaTela) }

    /** "imagem"/"video" se a janela sob o ponto mostra um gerador MCP; "" caso contrário. */
    fun tipoGeradorMcpSobPonto(xNaTela: Float, yNaTela: Float): String {
        val janela = painelSobPonto(xNaTela, yNaTela) ?: return ""
        return com.chromebrothers.navegador.regras.DestinoDoArrasteDaBibliotecaParaReferenciaMcp
            .tipoDoComponentePeloId(janela.idOrigemDaBibliotecaAtual())
    }

    private fun quantidadeVisivel(): Int = janelas.count { it.estaVisivelNaColecao() }

    private fun abrirJanela(): PainelDaColecaoFlutuanteDoChatDoNavegador? {
        if (!LimiteDeJanelasFlutuantesDaColecaoDoChat.podeAbrir(quantidadeVisivel())) return null
        if (!LimiteDeJanelasFlutuantesDaColecaoDoChat.podeCriarTotal(janelas.size)) return null
        return criarJanela(focarAoCriar = true)
    }

    private fun abrirJanelaParaWarmStart(): PainelDaColecaoFlutuanteDoChatDoNavegador? {
        if (!LimiteDeJanelasFlutuantesDaColecaoDoChat.podeCriarTotal(janelas.size)) return null
        return criarJanela(focarAoCriar = false)
    }

    private fun criarJanela(focarAoCriar: Boolean): PainelDaColecaoFlutuanteDoChatDoNavegador? {
        val raiz = activity.findViewById<ViewGroup>(R.id.raiz_do_navegador) ?: return null
        val antes = raiz.childCount
        activity.layoutInflater.inflate(
            R.layout.layout_janela_flutuante_da_colecao_do_chat_do_navegador,
            raiz,
            true,
        )
        val painel = raiz.getChildAt(antes)
        val selo = raiz.getChildAt(antes + 1) as TextView
        val deslocamento = janelas.size * 24f * activity.resources.displayMetrics.density
        lateinit var criada: PainelDaColecaoFlutuanteDoChatDoNavegador
        criada = PainelDaColecaoFlutuanteDoChatDoNavegador(
            activity = activity,
            painel = painel,
            selo = selo,
            baixar = baixar,
            abrirVideo = abrirVideo,
            abrirEnderecoDaPagina = abrirEnderecoDaPagina,
            aoEncerrar = { remover(criada) },
            aoFocar = { focar(criada) },
            deslocamentoInicialPx = deslocamento,
        )
        criada.ligar()
        aplicarPontes(criada)
        janelas.add(criada)
        if (focarAoCriar) focar(criada)
        return criada
    }

    private fun aplicarPontes(janela: PainelDaColecaoFlutuanteDoChatDoNavegador) {
        aoEnviarMensagemAoChat?.let { janela.ligarEnvioAoChat(it) }
        aoLerUrlDaAbaAtiva?.let { janela.ligarUrlDaAbaAtiva(it) }
        aoLerTituloDaAbaAtiva?.let { janela.ligarTituloDaAbaAtiva(it) }
        aoLerTextoSelecionadoDaAba?.let { janela.ligarTextoSelecionadoDaAba(it) }
        aoAbrirNaAba?.let { janela.ligarAbrirNaAba(it) }
        aoRodarRoteiro?.let { janela.ligarRodarRoteiro(it) }
        aoCopiarTexto?.let { janela.ligarCopiarTexto(it) }
        aoGuardarNaBiblioteca?.let { janela.ligarGuardarNaBiblioteca(it) }
        aoPersistirDimensoes?.let { janela.ligarPersistenciaDeDimensoes(it) }
        aoPedirArquivoDoAparelho?.let { janela.ligarSeletorDeArquivoDoAparelho(it) }
        aoEscolherPastaDoAparelho?.let { escolher ->
            janela.ligarEscolherPastaDoAparelho { escolher(janela) }
        }
        aoListarPastaDoAparelho?.let { janela.ligarListarPastaDoAparelho(it) }
        aoExcluirArquivosDoAparelho?.let { janela.ligarExcluirArquivosDoAparelho(it) }
        aoAbrirImagemNaAba?.let { janela.ligarAbrirImagemNaAba(it) }
        aoAbrirImagemNoVisor?.let { janela.ligarAbrirImagemNoVisor(it) }
        aoAbrirImagemNoEditor?.let { janela.ligarAbrirImagemNoEditor(it) }
        aoEnviarImagemParaCamadaDoEditor?.let { janela.ligarEnviarImagemParaCamadaDoEditor(it) }
        aoForcarEncerrarArrasteDeImagemDoComponente?.let { janela.ligarForcarEncerrarArrasteDeImagemDoComponente(it) }
        aoIniciarArrasteComMiniatura?.let { janela.ligarIniciarArrasteComMiniatura(it) }
        aoAbrirVisorExternoNaGaveta?.let { janela.ligarAbrirVisorExternoNaGaveta(it) }
        aoIniciarArrasteDeImagemParaOEditor?.let { janela.ligarIniciarArrasteDeImagemParaOEditor(it) }
        aoAtualizarArrasteDeImagemParaOEditor?.let { janela.ligarAtualizarArrasteDeImagemParaOEditor(it) }
        aoSoltarArrasteDeImagemParaOEditor?.let { janela.ligarSoltarArrasteDeImagemParaOEditor(it) }
        aoGuardarImagem?.let { janela.ligarGuardarImagem(it) }
        aoCopiarImagem?.let { janela.ligarCopiarImagem(it) }
        aoAbrirVideoNoPlayer?.let { janela.ligarAbrirVideoNoPlayer(it) }
        servicoDeGeracaoDeImagemViaMcp?.let { janela.ligarServicoDeGeracaoDeImagemViaMcp(it) }
        aoEscolherImagemDeReferenciaDaGeracao?.let { escolher ->
            janela.ligarEscolherImagemDeReferenciaDaGeracao { origem -> escolher(janela, origem) }
        }
        aoAbrirVideosAoLado?.let { abrir ->
            janela.ligarAbrirVideosAoLado { itens, indice -> abrir(janela, itens, indice) }
        }
        videosDaGradeDoVisualizador?.let { (abrirAoLado, mostrarMenu) ->
            janela.ligarVideosDaGradeDoVisualizador(abrirAoLado, mostrarMenu)
        }
        aoTerminarGeracaoDeImagemViaMcp?.let { janela.ligarAoTerminarGeracaoDeImagemViaMcp(it) }
        aoSoltarItemDaBibliotecaParaReferenciaMcp?.let { soltar ->
            janela.ligarSoltarItemDaBibliotecaParaReferenciaMcp { evento, tipo -> soltar(janela, evento, tipo) }
        }
        aoAncorarNaGaveta?.let { acao ->
            janela.ligarAncorarNaGaveta { acao(janela) }
        }
        aoArrastarPelaBarra?.let { janela.ligarArrastePelaBarra(it) }
    }

    private fun focar(janela: PainelDaColecaoFlutuanteDoChatDoNavegador) {
        janelaFocada = janela
        janela.trazerParaFrente()
    }

    private fun remover(janela: PainelDaColecaoFlutuanteDoChatDoNavegador) {
        janelas.remove(janela)
        if (janelaFocada === janela) {
            janelaFocada = janelas.lastOrNull()
            janelaFocada?.trazerParaFrente()
        }
    }
}

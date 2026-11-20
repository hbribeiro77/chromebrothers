package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.chromebrothers.navegador.regras.DocumentoDoRascunhoDaEdicaoDeImagem
import com.chromebrothers.navegador.regras.FerramentaGravadaDoRascunhoDaEdicao
import com.chromebrothers.navegador.regras.PontoDoTracoDoRascunhoDaEdicao
import com.chromebrothers.navegador.regras.TextoDoRascunhoDaEdicao
import com.chromebrothers.navegador.regras.TracoDoRascunhoDaEdicao
import com.chromebrothers.navegador.regras.CantoDaComposicao
import com.chromebrothers.navegador.regras.DecisaoDeGravarRascunhoDaEdicaoDeImagem
import com.chromebrothers.navegador.regras.DecisaoDoRetanguloSobreAImagemDaComposicao
import com.chromebrothers.navegador.regras.EfeitoDoRetanguloSobreAImagem
import com.chromebrothers.navegador.regras.EnvioDeImagemDoComponenteParaCamadaDoEditor
import com.chromebrothers.navegador.regras.MolduraDaComposicaoDeImagem
import com.chromebrothers.navegador.regras.ProporcaoDaMolduraDaComposicao
import com.chromebrothers.navegador.regras.ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor
import com.chromebrothers.navegador.regras.RetanguloDaComposicao
import com.chromebrothers.navegador.regras.encaixarNaMoldura
import com.chromebrothers.navegador.regras.redimensionarMantendoProporcao
import java.util.Collections
import java.util.IdentityHashMap
import com.chromebrothers.navegador.regras.GestoDaReguaSobreAFolha
import com.chromebrothers.navegador.regras.ParteDaReguaSobreAFolha
import com.chromebrothers.navegador.regras.CaixaDoLacoLivre
import com.chromebrothers.navegador.regras.AlcaDoRetanguloDeRecorteDaComposicao
import com.chromebrothers.navegador.regras.FormaDaSelecaoSobreAImagem
import com.chromebrothers.navegador.regras.EspelhoDaPecaDaComposicao
import com.chromebrothers.navegador.regras.EspelhoGravadoDaPecaDaComposicao
import com.chromebrothers.navegador.regras.GiroDaPecaDaComposicao
import com.chromebrothers.navegador.regras.LacoLivreDaSelecaoSobreAImagemDaComposicao
import com.chromebrothers.navegador.regras.LimiteDoRetanguloDeRecorteDentroDaImagemDaComposicao
import com.chromebrothers.navegador.regras.PontoDoLacoLivre
import com.chromebrothers.navegador.regras.RetanguloInicialDoRecorteSobreAImagemDaComposicao
import com.chromebrothers.navegador.regras.SelecaoInteligenteDoRecorteSobreAComposicao
import com.chromebrothers.navegador.regras.AreaDaImagemParaSelecaoInteligenteDoRecorte
import com.chromebrothers.navegador.regras.CamadaAtivaDaEdicaoDeImagem
import com.chromebrothers.navegador.regras.AcaoDaSelecaoAtivaDoLaco
import com.chromebrothers.navegador.regras.EfeitoDaAcaoDaSelecaoAtivaDoLaco
import com.chromebrothers.navegador.regras.FonteDoDestacarDaCamadaAtiva
import com.chromebrothers.navegador.regras.InverterSelecaoAtivaDoLaco
import com.chromebrothers.navegador.regras.LadoDaBarraFlutuanteDaSelecaoAtiva
import com.chromebrothers.navegador.regras.MapeamentoDoLacoParaPixelsDoRecorteDaImagem
import com.chromebrothers.navegador.regras.SelecaoAtivaDoRetanguloDaComposicao
import com.chromebrothers.navegador.regras.RetanguloDeRecorteComecaVazioComoOLaco
import com.chromebrothers.navegador.regras.DecisaoDoMoverDaSelecaoAtivaDaComposicao
import com.chromebrothers.navegador.regras.DesenhoDaMolduraDeTransformarNoEditorDeImagem
import com.chromebrothers.navegador.regras.DestinoDoToqueAposMoverComMolduraDeTransformar
import com.chromebrothers.navegador.regras.DestinoDoToqueNoModoColarDaSelecaoCopiada
import com.chromebrothers.navegador.regras.ModoColarDaSelecaoCopiadaNaComposicao
import com.chromebrothers.navegador.regras.PacoteDoDesfazerDoMoverDaSelecaoAtiva
import com.chromebrothers.navegador.regras.ToqueAposMoverComMolduraDeTransformarDaPeca
import com.chromebrothers.navegador.regras.ToqueComSelecaoAtivaDoRecorteNoEditorDeImagem
import com.chromebrothers.navegador.regras.ToqueForaDaSelecaoAtivaDoRecorteDaComposicao
import com.chromebrothers.navegador.regras.ToqueNoCorpoDaPecaSelecionadaDaComposicao
import com.chromebrothers.navegador.regras.ToqueNoModoColarDaSelecaoCopiadaNaComposicao
import com.chromebrothers.navegador.regras.AlinhamentoDoRetanguloAGradeDePixelsDaImagem
import com.chromebrothers.navegador.regras.PecaSelecionadaDaEdicaoDeImagem
import com.chromebrothers.navegador.regras.PosicaoDaBarraFlutuanteDaSelecaoAtiva
import com.chromebrothers.navegador.regras.PosicaoDoChipColarDaPreviaTransformavelDaComposicao
import com.chromebrothers.navegador.regras.SelecaoObrigatoriaDaCamada
import com.chromebrothers.navegador.regras.ToqueComDedoSobreLacoFechado
import com.chromebrothers.navegador.regras.ToqueParaSelecionarPecaNoEditor
import com.chromebrothers.navegador.regras.ImagemAlvoDoRecorteDaComposicao
import com.chromebrothers.navegador.regras.ToqueNasAlcasDoRetanguloDeRecorteDaComposicao
import com.chromebrothers.navegador.regras.OrdemDasPecasDaComposicao
import com.chromebrothers.navegador.regras.PaletaDoEditorDeImagem
import com.chromebrothers.navegador.regras.PontaDoTracoDaComposicao
import com.chromebrothers.navegador.regras.GeometriaDaPontaDoTraco
import com.chromebrothers.navegador.regras.SetaSobreAFolha
import com.chromebrothers.navegador.regras.PressaoDaPontaDoTraco
import com.chromebrothers.navegador.regras.PilhaDeCamadasDaComposicao
import com.chromebrothers.navegador.regras.ApagadorSobreAImagemDaComposicao
import com.chromebrothers.navegador.regras.BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem
import com.chromebrothers.navegador.regras.AjusteDoTracoDaCaneta
import com.chromebrothers.navegador.regras.AjustesDeImagemDaCamada
import com.chromebrothers.navegador.regras.CrescimentoDaCamadaDeImagemParaOTraco
import com.chromebrothers.navegador.regras.FormaLimpaNoFimDoTraco
import com.chromebrothers.navegador.regras.StreamLineDoTraco
import com.chromebrothers.navegador.regras.PreenchimentoDoBalde
import com.chromebrothers.navegador.regras.TintaEmCacheDaCamadaDaComposicao
import com.chromebrothers.navegador.regras.ToqueNaReguaDoEditor
import com.chromebrothers.navegador.regras.DestinoDoTracoNaCamadaAtiva
import com.chromebrothers.navegador.regras.TracoNaCamadaDaComposicao
import com.chromebrothers.navegador.regras.AreaDoRecorteSobreAImagemDaComposicao
import com.chromebrothers.navegador.regras.VisibilidadeDaCamadaDaComposicao
import com.chromebrothers.navegador.regras.ReguaSobreAFolha
import com.chromebrothers.navegador.regras.RetanguloEmPixelsDaImagem
import com.chromebrothers.navegador.regras.RotacaoDaVistaDoEditorDeImagem
import com.chromebrothers.navegador.regras.TamanhoDoPincelDoEditorDeImagem
import com.chromebrothers.navegador.regras.Carimbo
import com.chromebrothers.navegador.regras.MotorDeCarimbosDoPincel
import com.chromebrothers.navegador.regras.EscalaDoPincelNaFolha
import com.chromebrothers.navegador.regras.PinceisPredefinidosDoMotor
import com.chromebrothers.navegador.regras.PincelDoMotor
import com.chromebrothers.navegador.regras.PincelDoMotorEmJson
import com.chromebrothers.navegador.regras.PontoParaCarimbar
import com.chromebrothers.navegador.regras.ZoomDaVistaDoEditorDeImagem
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class EditorDeRecorteETracoDaImagemDaBiblioteca(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    enum class Ferramenta { VERMELHO, BRANCO, BORRACHA, RECORTE }

    var aoMudarFerramenta: ((Ferramenta) -> Unit)? = null
    var aoMudarSelecao: (() -> Unit)? = null
    var aoPegarCor: ((Int) -> Unit)? = null
    var aoMudarModoDoDedo: (() -> Unit)? = null
    var aoMudarSelecaoAtivaDoLaco: (() -> Unit)? = null
    var aoPedirConfirmacaoDeRecorteDaSelecaoAtiva: (() -> Unit)? = null
    var aoMudarZoomDaVista: ((Float) -> Unit)? = null
    /** Gestos: [aoGestoDesfazer] avisa que o toque com 2 dedos JÁ desfez; [aoGestoRefazer] pede o refazer (3 dedos). */
    var aoGestoDesfazer: (() -> Unit)? = null
    var aoGestoRefazer: (() -> Unit)? = null
    var aoFixarFormaLimpa: (() -> Unit)? = null

    // Forma limpa: caneta parada no fim do traço.
    private var formaFixadaNoTraco = false
    private var ancoraParadaX = 0f
    private var ancoraParadaY = 0f
    /** Pontos do traço antes de a caneta parar: os de depois (parada, apertando) não entram na largura da forma limpa. */
    private var pontosAntesDaParada = 0
    private val fixarFormaLimpa = Runnable { tentarFixarFormaLimpa() }

    // Toque com vários dedos (desfazer/refazer) e toque longo (conta-gotas).
    private var inicioDoToqueComDedosMs = 0L
    private var maiorQuantidadeDeDedos = 0
    private var dedosMexeram = false
    private var toqueSoComDedos = true
    private val inicioDosDedos = HashMap<Int, android.graphics.PointF>()
    private var pontoDoToqueLongoX = 0f
    private var pontoDoToqueLongoY = 0f
    private val contaGotasPeloToqueLongo = Runnable { pegarCorPeloToqueLongo() }
    /** Com a Seleção: dedo parado ~0,5 s seleciona tudo da camada ativa (como arrastar o retângulo inteiro). */
    private val selecionarTudoPeloToqueLongo = Runnable { selecionarTudoDaCamadaPeloToqueLongo() }
    private var contaGotasArmado = false

    private var molduraLargura = 0
    private var molduraAltura = 0
    private val objetos = mutableListOf<Peca>()
    private var idDaCamadaAtiva = -1
    private var selecionadoId = -1
    private var ordemAntesDoArraste: List<Int>? = null
    private var proximoId = 1
    private var ferramenta = Ferramenta.VERMELHO
    /** Último pincel da barra (vermelho/branco) — volta a ele ao soltar o botão da S Pen. */
    private var ultimaFerramentaDePincel = Ferramenta.VERMELHO
    private var ultimaPontaDePincel = PontaDoTracoDaComposicao.CANETA
    /** Hold-to-erase: botão lateral pressionado (estilo Samsung Notes). */
    private var botaoLateralDaCanetaPressionado = false
    private var desenharComODedo = false
    private var usarDedoNoLaco = false
    private val passos = mutableListOf<Passo>()
    private val desfeitos = mutableListOf<Passo>()
    private val pecasFora = mutableMapOf<Int, Peca>()
    private var tracoAtual: Traco? = null
    private var idDoPonteiroDoTraco = -1
    private var recorte = RectF()
    private var formaDaSelecao = FormaDaSelecaoSobreAImagem.RETANGULO
    private val pontosDoLaco = mutableListOf<PontoDoLacoLivre>()
    private val iniciosDosTracosDoLaco = mutableListOf<Int>()
    private var lacoFechado = false
    private var selecaoDoLacoInvertida = false
    private var selecaoDoRetanguloInvertida = false
    private var pontosDoLacoNaOrigem = listOf<PontoDoLacoLivre>()
    private val recorteNaOrigem = RectF()
    private var temOrigemDoRetangulo = false
    /** Preferência: Mover mantém peça como camada (true) ou funde na origem ao encerrar (false). */
    var moverCriandoNovaCamada: Boolean = true
    private var fundirPecaAoEncerrarTransformar = false
    private var idOrigemDoTransformar = -1
    private var modoDaMascaraDoLaco = ModoDaMascaraDoLaco.NENHUM
    private var origemDaMascaraX = 0f
    private var origemDaMascaraY = 0f
    private var anguloInicialDaMascara = 0f
    private val chipsDaSelecaoAtiva = mutableListOf<ChipDaSelecaoAtivaDoLaco>()
    private val botoesDeEspelho = mutableListOf<EspelhoDaPecaDaComposicao.Botao>()
    private val tintaDoChipDaSelecao = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F4F7FB")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    private val fundoDoChipDaSelecao = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2A3140")
    }
    private var idDaImagemDoRecorte = -1
    private var recortarDentroDaMoldura = true
    private var selecaoInteligenteArmada = false
    private var arraste: Arraste? = null
    private var camadaSuja = true
    private var bufferDaMolduraEmUmParaUm: Bitmap? = null
    private val destino = RectF()
    private val pincel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val filtroDaTinta = Paint(Paint.FILTER_BITMAP_FLAG)
    /** Sem filtro: cópia/mover do retângulo não “escorrega” meio pixel por bilinear. */
    private val tintaDaPecaSemFiltro = Paint().apply { isFilterBitmap = false }
    private var pontosJaNaTinta = 0
    private val apagador = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val apagadorDaImagem = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val escurecer = Paint().apply { color = Color.parseColor("#99000000") }
    private val contornoEscuro = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#1A1D24")
        strokeWidth = 5f * resources.displayMetrics.density
    }
    private val contornoClaro = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#F4F7FB")
        strokeWidth = 2f * resources.displayMetrics.density
    }
    private val alcaAnel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1A1D24") }
    private val alcaMiolo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#8AB4F8") }
    private val contornoClaroDeTransformar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#FFE08A")
        strokeWidth = 2.5f * resources.displayMetrics.density
    }
    private val alcaMioloDeTransformar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F0B429")
    }
    private var pecaEmModoTransformar = false
    /** Evita que o mesmo toque do chip Mover encerre a moldura amarela ao soltar fora da peça. */
    private var aguardandoSoltarAposArmarTransformar = false
    /** Prévia do Copiar aguardando Colar (fundir) ou cancelar. */
    private var modoColarDaSelecao = false
    private var idPecaPreviaDoColar = -1
    private val fundoDaFolha = Paint().apply { color = Color.WHITE }
    private val tintaDoTexto = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    private var corDoProximoTexto = PaletaDoEditorDeImagem.PRETO
    private var corDoTraco = PaletaDoEditorDeImagem.VERMELHO
    private var corDaFolha = PaletaDoEditorDeImagem.corDaFolhaInicial
    private var pontaDoTraco = PontaDoTracoDaComposicao.CANETA
    private var origemDaSeta: Ponto? = null
    private var previaDaSeta: Ponto? = null
    private val alcanceDaAlca = 28f * resources.displayMetrics.density
    private val folgaDoToqueComDoisDedos = 32f * resources.displayMetrics.density
    private val folgaDoArraste = 10f * resources.displayMetrics.density
    /** Tamanho contínuo 0..1 (curva exponencial em [TamanhoDoPincelDoEditorDeImagem]); pincel e borracha separados. */
    private var tamanhoDoPincel = TamanhoDoPincelDoEditorDeImagem.PADRAO_PINCEL
    private var tamanhoDaBorracha = TamanhoDoPincelDoEditorDeImagem.PADRAO_BORRACHA
    private var zoom = 1f
    private var panX = 0f
    private var panY = 0f
    private var modoDoDedo = ModoDoDedo.NENHUM
    private var modoDaPeca = ModoDaPeca.NENHUM
    private var idDoGesto = -1
    private var cantoDoGesto: CantoDaComposicao? = null
    private var origemDoGestoX = 0f
    private var origemDoGestoY = 0f
    private var estadoDoGesto: EstadoDaPeca? = null
    private var inicioDoToque = 0L
    private var origemX0 = 0f
    private var origemY0 = 0f
    private var origemX1 = 0f
    private var origemY1 = 0f
    private var toqueX = 0f
    private var toqueY = 0f
    private var ultimoArrasteX = 0f
    private var ultimoArrasteY = 0f
    private var distanciaDaPinca = 1f
    private var zoomNoInicioDaPinca = 1f
    private var focoDaPincaX = 0f
    private var focoDaPincaY = 0f
    private var regua: ReguaSobreAFolha? = null
    private var parteDaRegua = ParteDaReguaSobreAFolha.NENHUMA
    private var ultimoAnguloDoDedo = 0f
    private var ancoraDoTracoX: Float? = null
    private var ancoraDoTracoY: Float? = null
    private var anguloDoTracoNaRegua: Float? = null
    private val corpoDaRegua = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bordaEscuraDaRegua = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val bordaClaraDaRegua = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val caboDaRegua = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setBackgroundColor(Color.parseColor("#101114"))
    }

    fun definirImagem(origem: Bitmap) {
        soltar()
        val copia = origem.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        molduraLargura = copia.width.coerceAtLeast(1)
        molduraAltura = copia.height.coerceAtLeast(1)
        val peca = Peca.Imagem(novoId(), copia)
        peca.largura = molduraLargura.toFloat()
        peca.altura = molduraAltura.toFloat()
        objetos += peca
        idDaCamadaAtiva = CamadaAtivaDaEdicaoDeImagem.idAoAbrirComImagem(peca.id)
        selecionadoId = PecaSelecionadaDaEdicaoDeImagem.idAoAbrir()
        ferramenta = Ferramenta.VERMELHO
        colocarRegua()
        invalidate()
    }

    fun definirMolduraVazia() {
        soltar()
        val (largura, altura) = MolduraDaComposicaoDeImagem.tamanho(ProporcaoDaMolduraDaComposicao.QUADRADO)
        molduraLargura = largura
        molduraAltura = altura
        ferramenta = Ferramenta.VERMELHO
        colocarRegua()
        invalidate()
    }

    fun adicionarImagem(origem: Bitmap): Boolean {
        if (molduraLargura <= 0 || !PilhaDeCamadasDaComposicao.cabeMaisUma(objetos.size)) return false
        val copia = origem.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        val ocupacao = if (objetos.none { it is Peca.Imagem }) 0.92f else 0.72f
        val encaixe = encaixarNaMoldura(
            copia.width.toFloat(),
            copia.height.toFloat(),
            molduraLargura.toFloat(),
            molduraAltura.toFloat(),
            ocupacao,
        )
        val peca = Peca.Imagem(novoId(), copia)
        peca.esquerda = encaixe.esquerda
        peca.topo = encaixe.topo
        peca.largura = encaixe.largura
        peca.altura = encaixe.altura
        objetos += peca
        idDaCamadaAtiva = peca.id
        empilhar(Passo.Inclusao(peca.id))
        aoMudarSelecao?.invoke()
        invalidate()
        return true
    }

    /** Funde o bitmap do componente na camada ativa (imagem ou folha promovida). */
    /**
     * Funde o bitmap na camada (ativa ou [idCamadaDestino]) sem criar peça temporária —
     * undo via [Passo.AlteraBitmap] (dois dedos não “duplica” camada).
     */
    fun fundirBitmapNaCamadaAtiva(
        origem: Bitmap,
        idCamadaDestino: Int = idDaCamadaAtiva,
    ): ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
        if (idCamadaDestino != -1 && idCamadaDestino != idDaCamadaAtiva) {
            idDaCamadaAtiva = idCamadaDestino
        }
        var destino = objetos.find { it.id == idDaCamadaAtiva }
        val decisao = EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
            editorAtivo = true,
            idCamadaAtiva = idDaCamadaAtiva,
            destinoEhImagem = destino is Peca.Imagem,
            destinoEhFolha = destino is Peca.Folha,
        )
        when (decisao) {
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO -> return decisao
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.PROMOVER_FOLHA_E_FUNDIR -> {
                val folha = destino as? Peca.Folha
                    ?: return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
                destino = promoverFolhaParaImagem(folha)
                    ?: return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
                idDaCamadaAtiva = destino.id
            }
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM -> Unit
        }
        val imagemDestino = destino as? Peca.Imagem
            ?: return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
        if (imagemDestino.bitmap.isRecycled) {
            return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
        }
        val copia = origem.copy(Bitmap.Config.ARGB_8888, false) ?: origem
        val bitmapAntes = imagemDestino.bitmap
        // A imagem entra centrada e inteira dentro da camada (em unidades da moldura).
        val larguraDaCamada = imagemDestino.largura.coerceAtLeast(1f)
        val alturaDaCamada = imagemDestino.altura.coerceAtLeast(1f)
        val encaixe = min(larguraDaCamada / copia.width.coerceAtLeast(1), alturaDaCamada / copia.height.coerceAtLeast(1))
        val larguraNaCamada = copia.width * encaixe
        val alturaNaCamada = copia.height * encaixe
        // Antes a foto era espremida na resolução da camada (folha = tamanho da moldura) e sem filtro:
        // agora a camada sobe para a densidade da imagem que chega (até o limite de memória).
        val densidadeDaCamada = bitmapAntes.width / larguraDaCamada
        var densidade = maxOf(densidadeDaCamada, copia.width / larguraNaCamada.coerceAtLeast(1f))
        densidade *= CrescimentoDaCamadaDeImagemParaOTraco.escala(larguraDaCamada * densidade, alturaDaCamada * densidade)
        densidade = maxOf(densidade, densidadeDaCamada)
        val suave = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        val base = if (densidade > densidadeDaCamada * 1.01f) {
            val maior = try {
                Bitmap.createBitmap(
                    CrescimentoDaCamadaDeImagemParaOTraco.tamanhoEmPixels(larguraDaCamada, densidade),
                    CrescimentoDaCamadaDeImagemParaOTraco.tamanhoEmPixels(alturaDaCamada, densidade),
                    Bitmap.Config.ARGB_8888,
                )
            } catch (_: OutOfMemoryError) {
                null
            }
            maior?.also {
                it.setHasAlpha(true)
                Canvas(it).drawBitmap(bitmapAntes, null, RectF(0f, 0f, it.width.toFloat(), it.height.toFloat()), suave)
            } ?: bitmapAntes.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        } else {
            bitmapAntes.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        }
        val tela = Canvas(base)
        val pxPorUnidade = base.width / larguraDaCamada
        val largura = larguraNaCamada * pxPorUnidade
        val altura = alturaNaCamada * pxPorUnidade
        val esquerda = (base.width - largura) / 2f
        val topo = (base.height - altura) / 2f
        tela.drawBitmap(
            copia,
            null,
            RectF(esquerda, topo, esquerda + largura, topo + altura),
            suave,
        )
        if (copia !== origem && !copia.isRecycled) copia.recycle()
        imagemDestino.bitmap = base
        empilhar(Passo.AlteraBitmap(imagemDestino.id, bitmapAntes))
        camadaSuja = true
        aoMudarSelecao?.invoke()
        invalidate()
        return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM
    }

    fun adicionarTexto(texto: String): Boolean {
        val escrito = texto.trim()
        if (escrito.isEmpty() || molduraLargura <= 0) return true
        if (!PilhaDeCamadasDaComposicao.cabeMaisUma(objetos.size)) return false
        val peca = Peca.Texto(
            id = novoId(),
            texto = escrito,
            tamanho = (molduraAltura / 14f).coerceIn(48f, 240f),
            cor = corDoProximoTexto,
        )
        medir(peca)
        peca.esquerda = (molduraLargura - peca.largura) / 2f
        peca.topo = (molduraAltura - peca.altura) / 2f
        objetos += peca
        idDaCamadaAtiva = peca.id
        empilhar(Passo.Inclusao(peca.id))
        aoMudarSelecao?.invoke()
        invalidate()
        return true
    }

    fun textoSelecionado(): String? = (objetos.find { it.id == idDaCamadaAtiva } as? Peca.Texto)?.texto

    fun atualizarTextoSelecionado(texto: String) {
        val peca = objetos.find { it.id == idDaCamadaAtiva } as? Peca.Texto ?: return
        val escrito = texto.trim()
        if (escrito.isEmpty() || escrito == peca.texto) return
        empilhar(Passo.TextoAlterado(peca.id, peca.texto, peca.cor))
        peca.texto = escrito
        medir(peca)
        aoMudarSelecao?.invoke()
        invalidate()
    }

    fun definirCorDoTexto(cor: Int) {
        val opaca = cor or 0xFF000000.toInt()
        corDoProximoTexto = opaca
        val peca = objetos.find { it.id == idDaCamadaAtiva } as? Peca.Texto
        if (peca != null && peca.cor != opaca) {
            empilhar(Passo.TextoAlterado(peca.id, peca.texto, peca.cor))
            peca.cor = opaca
            invalidate()
        }
        aoMudarSelecao?.invoke()
    }

    fun corDoTextoAtual(): Int {
        return (objetos.find { it.id == idDaCamadaAtiva } as? Peca.Texto)?.cor ?: corDoProximoTexto
    }

    fun indiceDaCorDoTexto(): Int = PaletaDoEditorDeImagem.indice(corDoTextoAtual())

    fun definirCorDoTraco(cor: Int) {
        corDoTraco = cor or 0xFF000000.toInt()
    }

    fun corDoTraco(): Int = corDoTraco

    fun definirCorDaFolha(cor: Int) {
        val opaca = cor or 0xFF000000.toInt()
        if (opaca == corDaFolha) return
        empilhar(Passo.Folha(corDaFolha))
        aplicarCorDaFolha(opaca)
        invalidate()
    }

    fun corDaFolha(): Int = corDaFolha

    fun pontaAtual(): PontaDoTracoDaComposicao = pontaDoTraco

    // ---- Motor de carimbos ----

    private var pincelDoMotor: PincelDoMotor? = null
    private val renderizadorDoMotor = RenderizadorDeCarimbosDoPincel()
    /** Camada do traço do motor em andamento (mostrada por cima da tinta com a opacidade do traço). */
    private var tintaDoTracoDoMotor: Bitmap? = null
    private var carimbosNaTintaDoMotor = 0
    /** Fim do traço em andamento que ainda afina (refeito a cada quadro, por cima da camada). */
    private var caudaDoTracoDoMotor: List<Carimbo> = emptyList()
    private val pinturaDaCamadaDoMotor = Paint(Paint.FILTER_BITMAP_FLAG)
    private val pinturaDaBorrachaDoMotor = Paint(Paint.FILTER_BITMAP_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    fun pincelDoMotorAtual(): String? = pincelDoMotor?.id

    /** Pincel com que a borracha apaga (null = borracha comum, redonda). */
    private var pincelDaBorracha: PincelDoMotor? = null

    fun pincelDaBorrachaAtual(): String? = pincelDaBorracha?.id

    fun escolherPincelDaBorracha(id: String?) {
        CatalogoDePinceisDoMotor.iniciar(context)
        pincelDaBorracha = id?.let { CatalogoDePinceisDoMotor.porId(it) }
        invalidate()
    }

    /** Cor guardada no traço da borracha do motor: o alpha é a opacidade (1..255) depois dos limites do pincel. */
    private fun corDaBorrachaDoMotor(pincel: PincelDoMotor): Int =
        Color.argb((255f * pincel.opacidadeNoControle(opacidadeDaBorracha)).toInt().coerceIn(1, 255), 0, 0, 0)

    /** Escolhe um pincel do motor (null volta às pontas comuns). */
    fun escolherPincelDoMotor(id: String?) {
        CatalogoDePinceisDoMotor.iniciar(context)
        pincelDoMotor = id?.let { CatalogoDePinceisDoMotor.porId(it) }
        if (pincelDoMotor != null) {
            pontaDoTraco = PontaDoTracoDaComposicao.CANETA
            origemDaSeta = null
            previaDaSeta = null
            ultimaPontaDePincel = PontaDoTracoDaComposicao.CANETA
        }
        invalidate()
    }

    /** Cor do traço do motor: a opacidade escolhida passa pelos limites do pincel. */
    private fun corDoTracoDoMotor(pincel: PincelDoMotor): Int {
        val base = PaletaDoEditorDeImagem.comOpacidade(corDoTraco, false)
        val alpha = ((base ushr 24) * pincel.opacidadeNoControle(opacidadeDoTraco)).toInt().coerceIn(1, 255)
        return (base and 0x00FFFFFF) or (alpha shl 24)
    }

    /** Pincel de um traço do rascunho: a cópia gravada no documento; senão o do catálogo pelo id. */
    private fun pincelDoRascunho(documento: DocumentoDoRascunhoDaEdicaoDeImagem, chave: String): PincelDoMotor? {
        if (chave.isBlank()) return null
        CatalogoDePinceisDoMotor.iniciar(context)
        return documento.pinceis[chave]?.let { PincelDoMotorEmJson.deJson(it) }
            ?: CatalogoDePinceisDoMotor.porId(PincelDoMotorEmJson.idDaChave(chave))
    }

    private fun pontosParaCarimbar(traco: Traco, larguraAlvo: Float, alturaAlvo: Float): List<PontoParaCarimbar> =
        traco.pontos.map { PontoParaCarimbar(it.x * larguraAlvo, it.y * alturaAlvo, it.largura * larguraAlvo, it.alpha / 255f) }

    private fun desenharTracoDoMotor(tela: Canvas, traco: Traco, larguraAlvo: Float, alturaAlvo: Float, concluido: Boolean) {
        val pincel = traco.pincel ?: return
        val carimbos = MotorDeCarimbosDoPincel.gerar(pontosParaCarimbar(traco, larguraAlvo, alturaAlvo), pincel, traco.semente, concluido)
        // Granulado fixo no papel: escala desta tela em relação à folha (a tinta pode ser menor que a moldura).
        val escalaDoPapel = larguraAlvo / molduraLargura.toFloat().coerceAtLeast(1f)
        if (traco.borracha) {
            renderizadorDoMotor.apagarTraco(tela, carimbos, alphaDaBorracha(traco), pincel, escalaDoPapel)
        } else {
            renderizadorDoMotor.desenharTraco(tela, carimbos, traco.cor, pincel, escalaDoPapel)
        }
    }

    /** Traço do motor em andamento: só os carimbos novos, na camada própria (sem o afinamento do fim). */
    private fun aplicarTrechoDoMotor(atual: Traco, pincel: PincelDoMotor, tinta: Bitmap) {
        var camada = tintaDoTracoDoMotor
        if (camada == null || camada.isRecycled || camada.width != tinta.width || camada.height != tinta.height) {
            camada?.takeIf { !it.isRecycled }?.recycle()
            camada = try {
                Bitmap.createBitmap(tinta.width, tinta.height, Bitmap.Config.ARGB_8888)
            } catch (_: OutOfMemoryError) {
                null
            } ?: return
            tintaDoTracoDoMotor = camada
            pontosJaNaTinta = 0
        }
        if (pontosJaNaTinta == 0) {
            camada.eraseColor(Color.TRANSPARENT)
            carimbosNaTintaDoMotor = 0
        }
        // Afinamento do fim ao vivo: o que vem antes da cauda já é definitivo e vai para a camada;
        // a cauda (que afina sob o dedo) é refeita a cada quadro. Ao soltar o traço não muda.
        val pontosNaTinta = pontosParaCarimbar(atual, tinta.width.toFloat(), tinta.height.toFloat())
        val carimbos = MotorDeCarimbosDoPincel.gerar(pontosNaTinta, pincel, atual.semente, comAfinamentoDoFim = true)
        val inicioDaCauda = MotorDeCarimbosDoPincel.inicioDaCauda(pontosNaTinta, pincel)
        val assentados = carimbos.indexOfFirst { it.distancia >= inicioDaCauda }.let { if (it < 0) carimbos.size else it }
        if (assentados > carimbosNaTintaDoMotor) {
            renderizadorDoMotor.desenhar(Canvas(camada), carimbos, carimbosNaTintaDoMotor, atual.cor, pincel, ate = assentados)
            carimbosNaTintaDoMotor = assentados
        }
        caudaDoTracoDoMotor = carimbos.subList(assentados.coerceAtMost(carimbos.size), carimbos.size).toList()
        pontosJaNaTinta = atual.pontos.size
    }

    // Dinâmica de velocidade (estado do traço em andamento).
    private var velocidadeDoTraco: Float? = null
    private var ultimoXDaVelocidade = 0f
    private var ultimoYDaVelocidade = 0f
    private var ultimoTempoDaVelocidade = 0L

    /** Velocidade suavizada do dedo/caneta em dp/ms; zera no primeiro ponto do traço. */
    private fun velocidadeDoPonto(event: MotionEvent, ponteiro: Int, historico: Int?, inicioDoTraco: Boolean): Float {
        val x = if (historico == null) event.getX(ponteiro) else event.getHistoricalX(ponteiro, historico)
        val y = if (historico == null) event.getY(ponteiro) else event.getHistoricalY(ponteiro, historico)
        val tempo = if (historico == null) event.eventTime else event.getHistoricalEventTime(historico)
        if (inicioDoTraco) {
            velocidadeDoTraco = null
            ultimoXDaVelocidade = x
            ultimoYDaVelocidade = y
            ultimoTempoDaVelocidade = tempo
            return 0f
        }
        val intervalo = tempo - ultimoTempoDaVelocidade
        if (intervalo <= 0L) return velocidadeDoTraco ?: 0f
        val bruta = hypot(x - ultimoXDaVelocidade, y - ultimoYDaVelocidade) / resources.displayMetrics.density / intervalo
        ultimoXDaVelocidade = x
        ultimoYDaVelocidade = y
        ultimoTempoDaVelocidade = tempo
        val suave = MotorDeCarimbosDoPincel.suavizarVelocidade(velocidadeDoTraco, bruta)
        velocidadeDoTraco = suave
        return suave
    }

    private fun pressaoDaCaneta(event: MotionEvent, ponteiro: Int, historico: Int?): Float? {
        val tipo = event.getToolType(ponteiro)
        if (tipo != MotionEvent.TOOL_TYPE_STYLUS && tipo != MotionEvent.TOOL_TYPE_ERASER) return null
        val bruta = if (historico == null) event.getPressure(ponteiro) else event.getHistoricalPressure(ponteiro, historico)
        return curvaDePressao.aplicar(bruta)
    }

    private fun larguraDoMotor(event: MotionEvent, ponteiro: Int, historico: Int?, pincel: PincelDoMotor, borracha: Boolean = false): Float {
        // Limites do pincel: o controle de tamanho anda só entre o mínimo e o máximo dele (borracha usa o tamanho dela).
        val tamanho = if (borracha) tamanhoDaBorracha else tamanhoDoPincel
        val nominal = TamanhoDoPincelDoEditorDeImagem.diametroDp(pincel.tamanhoNoControle(tamanho), borracha) *
            resources.displayMetrics.density * pincel.multiplicadorDeLargura
        // Preso à folha: o zoom da vista não muda o traço na imagem.
        return EscalaDoPincelNaFolha.diametroNaFolha(nominal, escalaDeReferenciaDoPincel()) *
            MotorDeCarimbosDoPincel.fatorDaPressaoNoTamanho(pressaoDaCaneta(event, ponteiro, historico), pincel.pressaoNoTamanho)
    }

    /** Tela inteira do aparelho (fixa): referência do tamanho do pincel, sem zoom nem gaveta do chat. */
    private val telaDeReferenciaDoPincel: android.graphics.Rect by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            context.getSystemService(android.view.WindowManager::class.java).maximumWindowMetrics.bounds
        } else {
            android.graphics.Rect(0, 0, resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
        }
    }

    private fun escalaDeReferenciaDoPincel(): Float = EscalaDoPincelNaFolha.escalaDeReferencia(
        telaDeReferenciaDoPincel.width(),
        telaDeReferenciaDoPincel.height(),
        molduraLargura,
        molduraAltura,
    )

    private fun alphaDoMotor(event: MotionEvent, ponteiro: Int, historico: Int?, pincel: PincelDoMotor): Int =
        (255f * MotorDeCarimbosDoPincel.fatorDaPressaoNaOpacidade(pressaoDaCaneta(event, ponteiro, historico), pincel.pressaoNaOpacidade))
            .toInt().coerceIn(1, 255)

    fun escolherPonta(proxima: PontaDoTracoDaComposicao) {
        pincelDoMotor = null
        pontaDoTraco = if (pontaDoTraco == proxima) PontaDoTracoDaComposicao.CANETA else proxima
        if (pontaDoTraco != PontaDoTracoDaComposicao.SETA) {
            origemDaSeta = null
            previaDaSeta = null
            ultimaPontaDePincel = pontaDoTraco
        }
        invalidate()
    }

    fun alternarContaGotas() {
        contaGotasArmado = !contaGotasArmado
    }

    fun desarmarContaGotas() {
        contaGotasArmado = false
    }

    fun excluirCamadaSelecionada(): Boolean {
        val peca = objetos.find { it.id == idDaCamadaAtiva } ?: return false
        if (tracoAtual?.idDaPeca == peca.id) cancelarTracoAtual()
        if (idDaImagemDoRecorte == peca.id) {
            idDaImagemDoRecorte = -1
            if (ferramenta == Ferramenta.RECORTE) {
                ferramenta = Ferramenta.VERMELHO
                aoMudarFerramenta?.invoke(ferramenta)
            }
        }
        objetos.remove(peca)
        pecasFora[peca.id] = peca
        reciclarTinta(peca)
        idDaCamadaAtiva = objetos.lastOrNull()?.id ?: -1
        selecionadoId = SelecaoObrigatoriaDaCamada.manter(selecionadoId, objetos.map { it.id })
        empilhar(Passo.Inclusao(peca.id))
        aoMudarSelecao?.invoke()
        invalidate()
        return true
    }

    fun contaGotasArmado(): Boolean = contaGotasArmado

    fun trazerSelecionadaParaFrente() = reordenarSelecionada(paraFrente = true)

    fun enviarSelecionadaParaTras() = reordenarSelecionada(paraFrente = false)

    private fun reordenarSelecionada(paraFrente: Boolean) {
        val id = idDaCamadaAtiva
        if (objetos.none { it.id == id }) return
        val antes = objetos.map { it.id }
        val depois = if (paraFrente) {
            OrdemDasPecasDaComposicao.trazerParaFrente(antes, id)
        } else {
            OrdemDasPecasDaComposicao.enviarParaTras(antes, id)
        }
        if (depois == antes) return
        empilhar(Passo.Ordem(antes))
        aplicarOrdem(depois)
        invalidate()
    }

    private fun aplicarOrdem(ids: List<Int>) {
        val porId = objetos.associateBy { it.id }
        val ordenados = ids.mapNotNull { porId[it] }.toMutableList()
        objetos.filter { it.id !in ids }.forEach { ordenados += it }
        objetos.clear()
        objetos.addAll(ordenados)
    }

    fun idDaCamadaAtiva(): Int = idDaCamadaAtiva

    fun idDaPecaSelecionada(): Int = selecionadoId

    fun camadasDaFrenteParaOFundo(): List<CamadaDaPilha> {
        return objetos.asReversed().mapNotNull { peca ->
            if (
                ModoColarDaSelecaoCopiadaNaComposicao.deveOcultarPreviaNaListaDeCamadas(
                    modoColarAtivo = modoColarDaSelecao,
                    idDaCamada = peca.id,
                    idPrevia = idPecaPreviaDoColar,
                )
            ) {
                return@mapNotNull null
            }
            when (peca) {
                is Peca.Imagem -> CamadaDaPilha(peca.id, null, peca.bitmap, peca.visivel, peca.opacidade)
                is Peca.Texto -> CamadaDaPilha(peca.id, peca.texto, null, peca.visivel, peca.opacidade)
                is Peca.Folha -> CamadaDaPilha(
                    peca.id, null, null, peca.visivel, peca.opacidade,
                    peca.tinta?.takeIf { !it.isRecycled && peca.tracos.isNotEmpty() },
                )
            }
        }
    }

    private val pinturaDoBloqueio = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP) }

    fun aceitaBloqueioDeTransparencia(id: Int): Boolean = ehCamadaDePintura(objetos.find { it.id == id })

    fun transparenciaBloqueada(id: Int): Boolean = objetos.find { it.id == id }?.alfaBloqueado == true

    /** Liga/desliga o bloqueio de transparência (preferência da camada; não entra no desfazer). */
    fun alternarBloqueioDeTransparencia(id: Int) {
        val peca = objetos.find { it.id == id } ?: return
        if (!ehCamadaDePintura(peca)) return
        peca.alfaBloqueado = !peca.alfaBloqueado
        aoMudarSelecao?.invoke()
        invalidate()
    }

    /** Tolerância do balde (0..1): quanto a cor pode variar e ainda contar como a mesma área. */
    var toleranciaDoBalde: Float = PreenchimentoDoBalde.TOLERANCIA_PADRAO
        set(valor) {
            field = valor.coerceIn(0f, 1f)
        }

    enum class ResultadoDoBalde { PREENCHEU, FORA_DA_FOLHA, SEM_CAMADA, NADA_A_PREENCHER, SEM_MEMORIA }

    /**
     * Balde de tinta no ponto da tela ([xNaView], [yNaView], coordenadas desta view): a área contínua
     * com cor parecida (olhando todas as camadas visíveis) é pintada na camada ativa com [cor].
     * Respeita a transparência bloqueada. Entra no desfazer.
     */
    fun preencherComOBalde(xNaView: Float, yNaView: Float, cor: Int): ResultadoDoBalde {
        if (molduraLargura <= 0 || molduraAltura <= 0) return ResultadoDoBalde.SEM_CAMADA
        if (modoColarDaSelecao) confirmarColarNaCamadaAtiva()
        if (pecaEmModoTransformar || selecionadoId != -1) limparSelecaoDaPeca()
        calcularDestino()
        val real = floatArrayOf(xNaView, yNaView)
        matrizInversaDaVista.setRotate(-anguloDaVista, width / 2f, height / 2f)
        matrizInversaDaVista.mapPoints(real)
        val ponto = paraAComposicao(real[0], real[1]) ?: return ResultadoDoBalde.FORA_DA_FOLHA
        if (ponto[0] !in 0f..molduraLargura.toFloat() || ponto[1] !in 0f..molduraAltura.toFloat()) {
            return ResultadoDoBalde.FORA_DA_FOLHA
        }
        val ativa = objetos.find { it.id == idDaCamadaAtiva }
        if (!ehCamadaDePintura(ativa)) return ResultadoDoBalde.SEM_CAMADA
        // Referência: o que se vê de todas as camadas (sem o papel), em até 4 Mpx.
        val escala = PreenchimentoDoBalde.escalaDaReferencia(molduraLargura, molduraAltura)
        val w = (molduraLargura * escala).toInt().coerceAtLeast(1)
        val h = (molduraAltura * escala).toInt().coerceAtLeast(1)
        val referencia = try {
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        } catch (_: OutOfMemoryError) {
            return ResultadoDoBalde.SEM_MEMORIA
        }
        val pixels = try {
            val tela = Canvas(referencia)
            tela.scale(w / molduraLargura.toFloat(), h / molduraAltura.toFloat())
            objetos.forEach { peca ->
                if (!peca.visivel) return@forEach
                val salvo = tela.saveLayer(null, null)
                desenharPeca(tela, peca, usarCache = false)
                tela.restoreToCount(salvo)
            }
            IntArray(w * h).also { referencia.getPixels(it, 0, w, 0, 0, w, h) }
        } catch (_: OutOfMemoryError) {
            referencia.recycle()
            return ResultadoDoBalde.SEM_MEMORIA
        }
        val area = PreenchimentoDoBalde.area(
            pixels, w, h,
            (ponto[0] * w / molduraLargura).toInt().coerceIn(0, w - 1),
            (ponto[1] * h / molduraAltura).toInt().coerceIn(0, h - 1),
            toleranciaDoBalde,
        )
        if (area == null || area.quantidade == 0) {
            referencia.recycle()
            return ResultadoDoBalde.NADA_A_PREENCHER
        }
        // A própria referência vira a tinta do balde: cor onde a área marcou, transparente no resto.
        val opaca = cor or (0xFF shl 24)
        for (i in pixels.indices) pixels[i] = if (area.mascara[i]) opaca else 0
        referencia.setPixels(pixels, 0, w, 0, 0, w, h)
        val alvo = when (ativa) {
            is Peca.Folha -> promoverFolhaParaImagem(ativa)
            is Peca.Imagem -> ativa
            else -> null
        }
        if (alvo == null || alvo.bitmap.isRecycled) {
            referencia.recycle()
            return ResultadoDoBalde.SEM_MEMORIA
        }
        val original = alvo.bitmap
        val nova = try {
            original.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        } catch (_: OutOfMemoryError) {
            referencia.recycle()
            return ResultadoDoBalde.SEM_MEMORIA
        }
        // Moldura → pixels da camada (desfaz giro e espelho da peça, como no desenho).
        val cx = alvo.esquerda + alvo.largura / 2f
        val cy = alvo.topo + alvo.altura / 2f
        val matriz = android.graphics.Matrix()
        matriz.setScale(molduraLargura.toFloat() / w, molduraAltura.toFloat() / h)
        matriz.postRotate(-alvo.angulo, cx, cy)
        if (alvo.espelhoHorizontal || alvo.espelhoVertical) {
            matriz.postScale(if (alvo.espelhoHorizontal) -1f else 1f, if (alvo.espelhoVertical) -1f else 1f, cx, cy)
        }
        matriz.postTranslate(-alvo.esquerda, -alvo.topo)
        matriz.postScale(nova.width / alvo.largura.coerceAtLeast(1f), nova.height / alvo.altura.coerceAtLeast(1f))
        val pintura = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            alpha = (opacidadeDaFerramenta.coerceIn(0.05f, 1f) * 255f).toInt()
            if (alvo.alfaBloqueado) xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        }
        Canvas(nova).drawBitmap(referencia, matriz, pintura)
        referencia.recycle()
        alvo.bitmap = nova
        empilhar(Passo.AlteraBitmap(alvo.id, original))
        idDaCamadaAtiva = alvo.id
        camadaSuja = true
        aoMudarSelecao?.invoke()
        invalidate()
        return ResultadoDoBalde.PREENCHEU
    }

    /** Miniatura da camada (imagem + traços) no formato da moldura, com [lado] px no lado maior. */
    fun miniaturaDaCamada(id: Int, lado: Int): Bitmap? {
        val peca = objetos.find { it.id == id } ?: return null
        if (peca is Peca.Texto || molduraLargura <= 0 || molduraAltura <= 0) return null
        val escala = lado.toFloat() / max(molduraLargura, molduraAltura)
        val bitmap = try {
            Bitmap.createBitmap(
                (molduraLargura * escala).toInt().coerceAtLeast(1),
                (molduraAltura * escala).toInt().coerceAtLeast(1),
                Bitmap.Config.ARGB_8888,
            )
        } catch (_: OutOfMemoryError) {
            return null
        }
        val tela = Canvas(bitmap)
        tela.scale(escala, escala)
        val opacidade = peca.opacidade
        peca.opacidade = 1f
        try {
            desenharPeca(tela, peca, usarCache = true)
        } finally {
            peca.opacidade = opacidade
        }
        return bitmap
    }

    fun alternarVisibilidadeDaCamada(id: Int) {
        val peca = objetos.find { it.id == id } ?: return
        peca.visivel = VisibilidadeDaCamadaDaComposicao.aposAlternar(peca.visivel)
        aoMudarSelecao?.invoke()
        invalidate()
    }

    private var opacidadeAntesDoAjuste: Pair<Int, Float>? = null

    fun opacidadeDaCamada(id: Int): Float = objetos.find { it.id == id }?.opacidade ?: 1f

    /** Durante o arraste do controle: muda ao vivo; o desfazer entra só em [concluirAjusteDeOpacidade]. */
    fun ajustarOpacidadeDaCamada(id: Int, valor: Float) {
        val peca = objetos.find { it.id == id } ?: return
        if (opacidadeAntesDoAjuste?.first != id) opacidadeAntesDoAjuste = id to peca.opacidade
        peca.opacidade = valor.coerceIn(0f, 1f)
        invalidate()
    }

    fun concluirAjusteDeOpacidade() {
        val (id, antes) = opacidadeAntesDoAjuste ?: return
        opacidadeAntesDoAjuste = null
        val peca = objetos.find { it.id == id } ?: return
        if (kotlin.math.abs(peca.opacidade - antes) < 0.005f) return
        empilhar(Passo.Opacidade(id, antes))
        aoMudarSelecao?.invoke()
    }

    fun podeMesclarComADeBaixo(id: Int): Boolean =
        objetos.indexOfFirst { it.id == id } > 0 && !modoColarDaSelecao && tracoAtual == null

    /**
     * Junta a camada [id] com a de baixo numa camada de imagem nova (moldura ∪ as duas caixas, na maior
     * densidade das duas). Opacidade e camadas escondidas: o que se vê é o que fica. Desfazer volta as duas.
     */
    fun mesclarComADeBaixo(id: Int): Boolean {
        if (molduraLargura <= 0 || molduraAltura <= 0 || modoColarDaSelecao || tracoAtual != null) return false
        if (pecaEmModoTransformar || selecionadoId != -1) limparSelecaoDaPeca()
        val indice = objetos.indexOfFirst { it.id == id }
        if (indice <= 0) return false
        val cima = objetos[indice]
        val baixo = objetos[indice - 1]
        var esquerda = 0f
        var topo = 0f
        var direita = molduraLargura.toFloat()
        var base = molduraAltura.toFloat()
        listOf(cima, baixo).forEach { peca ->
            cantosDaPeca(peca).forEach { c ->
                esquerda = minOf(esquerda, c.x); topo = minOf(topo, c.y)
                direita = maxOf(direita, c.x); base = maxOf(base, c.y)
            }
        }
        val largura = direita - esquerda
        val altura = base - topo
        var densidade = 1f
        listOf(cima, baixo).forEach { peca ->
            if (peca is Peca.Imagem && !peca.bitmap.isRecycled) {
                densidade = maxOf(densidade, peca.bitmap.width / peca.largura.coerceAtLeast(1f))
            }
        }
        densidade *= CrescimentoDaCamadaDeImagemParaOTraco.escala(largura * densidade, altura * densidade)
        val bitmap = try {
            Bitmap.createBitmap(
                CrescimentoDaCamadaDeImagemParaOTraco.tamanhoEmPixels(largura, densidade),
                CrescimentoDaCamadaDeImagemParaOTraco.tamanhoEmPixels(altura, densidade),
                Bitmap.Config.ARGB_8888,
            )
        } catch (_: OutOfMemoryError) {
            return false
        }
        bitmap.setHasAlpha(true)
        val tela = Canvas(bitmap)
        tela.scale(bitmap.width / largura, bitmap.height / altura)
        tela.translate(-esquerda, -topo)
        listOf(baixo, cima).forEach { peca ->
            if (!peca.visivel) return@forEach
            // Camada própria: a borracha de uma camada não fura a outra.
            val salvo = tela.saveLayer(null, null)
            desenharPeca(tela, peca, usarCache = false)
            tela.restoreToCount(salvo)
        }
        val mesclada = Peca.Imagem(novoId(), bitmap, esquerda, topo, largura, altura)
        objetos[indice - 1] = mesclada
        objetos.removeAt(indice)
        idDaCamadaAtiva = mesclada.id
        selecionadoId = -1
        empilhar(Passo.MesclarCamadas(cima, baixo, mesclada, aplicado = true))
        camadaSuja = true
        aoMudarSelecao?.invoke()
        invalidate()
        return true
    }

    fun podeDuplicarCamada(id: Int): Boolean {
        val peca = objetos.find { it.id == id }
        return EspelhoDaPecaDaComposicao.podeDuplicar(ehCamadaDePintura(peca), objetos.size)
    }

    fun podeSelecionarCamadaInteira(id: Int): Boolean {
        val peca = objetos.find { it.id == id }
        return EspelhoDaPecaDaComposicao.podeSelecionarInteira(ehCamadaDePintura(peca))
    }

    fun aceitaEspelhoNaCamada(id: Int): Boolean {
        val peca = objetos.find { it.id == id }
        return EspelhoDaPecaDaComposicao.aceitaEspelho(ehCamadaDePintura(peca))
    }

    /** Imagem ou desenho: para quem usa é tudo "camada"; o desenho vira imagem quando a ação precisa de pixels. */
    private fun ehCamadaDePintura(peca: Peca?): Boolean = peca is Peca.Imagem || peca is Peca.Folha

    fun duplicarCamada(id: Int): Boolean {
        if (!podeDuplicarCamada(id)) return false
        val folha = objetos.find { it.id == id } as? Peca.Folha
        if (folha != null) return duplicarFolha(folha)
        val origem = objetos.find { it.id == id } as? Peca.Imagem ?: return false
        if (origem.bitmap.isRecycled) return false
        val copia = try {
            origem.bitmap.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        } catch (_: OutOfMemoryError) {
            null
        } ?: return false
        val peca = Peca.Imagem(novoId(), copia)
        val salto = max(24f, min(origem.largura, origem.altura) * 0.06f)
        peca.largura = origem.largura
        peca.altura = origem.altura
        peca.esquerda = (origem.esquerda + salto).coerceIn(0f, (molduraLargura - peca.largura).coerceAtLeast(0f))
        peca.topo = (origem.topo + salto).coerceIn(0f, (molduraAltura - peca.altura).coerceAtLeast(0f))
        peca.angulo = origem.angulo
        peca.espelhoHorizontal = origem.espelhoHorizontal
        peca.espelhoVertical = origem.espelhoVertical
        peca.alfaBloqueado = origem.alfaBloqueado
        val indice = objetos.indexOf(origem)
        objetos.add(if (indice < 0) objetos.size else indice + 1, peca)
        idDaCamadaAtiva = peca.id
        selecionarPeca(peca.id)
        pecaEmModoTransformar = true
        empilhar(Passo.Inclusao(peca.id))
        aoMudarSelecao?.invoke()
        invalidate()
        return true
    }

    /** Desenho duplicado: a cópia nasce já como imagem logo acima; a original segue como está. */
    private fun duplicarFolha(folha: Peca.Folha): Boolean {
        if (pecaEmModoTransformar || selecionadoId != -1) limparSelecaoDaPeca()
        val bitmap = bitmapDosTracosDaFolha(folha) ?: return false
        val peca = imagemComOsDadosDaFolha(folha, novoId(), bitmap)
        val indice = objetos.indexOf(folha)
        objetos.add(if (indice < 0) objetos.size else indice + 1, peca)
        idDaCamadaAtiva = peca.id
        empilhar(Passo.Inclusao(peca.id))
        camadaSuja = true
        aoMudarSelecao?.invoke()
        invalidate()
        return true
    }

    /**
     * Cobre a camada com a seleção ativa do retângulo (mesma barra: Copiar, Mover, Excluir…).
     * Imagem = a camada inteira; desenho = a folha inteira (moldura), como arrastar à mão.
     * Não entra no modo transformar da peça.
     */
    fun selecionarCamadaInteira(id: Int): Boolean {
        if (!podeSelecionarCamadaInteira(id)) return false
        if (!EspelhoDaPecaDaComposicao.selecionarTudoAbreSelecaoAtivaDaFerramenta()) return false
        val folha = objetos.find { it.id == id } as? Peca.Folha
        if (folha != null) return selecionarFolhaInteira(folha)
        val peca = objetos.find { it.id == id } as? Peca.Imagem ?: return false
        if (peca.bitmap.isRecycled || peca.largura <= 0f || peca.altura <= 0f) return false
        if (ModoColarDaSelecaoCopiadaNaComposicao.trocaDeFerramentaCancelaModo(modoColarDaSelecao)) {
            cancelarModoColarDaSelecao()
        }
        selecionadoId = -1
        pecaEmModoTransformar = false
        aguardandoSoltarAposArmarTransformar = false
        fundirPecaAoEncerrarTransformar = false
        idOrigemDoTransformar = -1
        ferramenta = Ferramenta.RECORTE
        formaDaSelecao = FormaDaSelecaoSobreAImagem.RETANGULO
        limparLaco()
        selecaoInteligenteArmada = false
        idDaCamadaAtiva = peca.id
        idDaImagemDoRecorte = peca.id
        prepararRecorteInicial(peca)
        registrarOrigemDoRetangulo(forcar = true)
        aoMudarFerramenta?.invoke(ferramenta)
        aoMudarSelecao?.invoke()
        aoMudarSelecaoAtivaDoLaco?.invoke()
        invalidate()
        return true
    }

    private fun selecionarFolhaInteira(folha: Peca.Folha): Boolean {
        if (molduraLargura <= 0 || molduraAltura <= 0) return false
        if (ModoColarDaSelecaoCopiadaNaComposicao.trocaDeFerramentaCancelaModo(modoColarDaSelecao)) {
            cancelarModoColarDaSelecao()
        }
        selecionadoId = -1
        pecaEmModoTransformar = false
        aguardandoSoltarAposArmarTransformar = false
        fundirPecaAoEncerrarTransformar = false
        idOrigemDoTransformar = -1
        ferramenta = Ferramenta.RECORTE
        formaDaSelecao = FormaDaSelecaoSobreAImagem.RETANGULO
        limparLaco()
        selecaoInteligenteArmada = false
        idDaCamadaAtiva = folha.id
        idDaImagemDoRecorte = -1
        prepararRecorteDaMoldura()
        registrarOrigemDoRetangulo(forcar = true)
        aoMudarFerramenta?.invoke(ferramenta)
        aoMudarSelecao?.invoke()
        aoMudarSelecaoAtivaDoLaco?.invoke()
        invalidate()
        return true
    }

    fun espelharCamada(id: Int, horizontal: Boolean): Boolean {
        val folha = objetos.find { it.id == id } as? Peca.Folha
        if (folha != null && promoverFolhaParaImagem(folha) == null) return false
        val peca = objetos.find { it.id == id } as? Peca.Imagem ?: return false
        if (peca.bitmap.isRecycled) return false
        selecionarPeca(id)
        pecaEmModoTransformar = true
        idDaCamadaAtiva = id
        val antes = capturar(peca)
        if (horizontal) {
            peca.espelhoHorizontal = EspelhoDaPecaDaComposicao.alternar(peca.espelhoHorizontal)
        } else {
            peca.espelhoVertical = EspelhoDaPecaDaComposicao.alternar(peca.espelhoVertical)
        }
        empilharTransformacaoOuFundirComAAnterior(peca.id, antes)
        aoMudarSelecao?.invoke()
        invalidate()
        return true
    }

    fun criarCamadaVazia(): Boolean {
        if (molduraLargura <= 0 || molduraAltura <= 0) return true
        if (!PilhaDeCamadasDaComposicao.cabeMaisUma(objetos.size)) return false
        val peca = Peca.Folha(novoId())
        peca.esquerda = 0f
        peca.topo = 0f
        peca.largura = molduraLargura.toFloat()
        peca.altura = molduraAltura.toFloat()
        objetos += peca
        // No modo Colar a nova Folha é destino válido (promovida ao confirmar).
        idDaCamadaAtiva = peca.id
        empilhar(Passo.Inclusao(peca.id))
        aoMudarSelecao?.invoke()
        invalidate()
        return true
    }

    fun desenhaComODedo(): Boolean = desenharComODedo

    fun alternarDesenhoComODedo() {
        desenharComODedo = !desenharComODedo
        aoMudarModoDoDedo?.invoke()
    }

    fun lacoComODedo(): Boolean = usarDedoNoLaco

    fun alternarLacoComODedo() {
        usarDedoNoLaco = !usarDedoNoLaco
        aoMudarModoDoDedo?.invoke()
    }

    fun definirCamadaAtiva(id: Int) {
        val peca = objetos.find { it.id == id } ?: return
        // No modo Colar a prévia continua selecionada; a lista só muda o destino do fundir.
        if (modoColarDaSelecao) {
            if (id == idPecaPreviaDoColar) return
            // Imagem ou Folha (Nova camada); texto não serve de destino.
            if (peca !is Peca.Imagem && peca !is Peca.Folha) return
        }
        idDaCamadaAtiva = id
        if (ferramenta == Ferramenta.RECORTE && !modoColarDaSelecao) {
            idDaImagemDoRecorte = if (peca is Peca.Imagem) peca.id else -1
        }
        aoMudarSelecao?.invoke()
        invalidate()
    }

    fun selecionarPeca(id: Int) {
        val peca = objetos.find { it.id == id } ?: return
        if (id != selecionadoId) pecaEmModoTransformar = false
        selecionadoId = id
        if (peca is Peca.Imagem && ferramenta == Ferramenta.RECORTE) {
            idDaImagemDoRecorte = peca.id
        }
        aoMudarSelecao?.invoke()
        invalidate()
    }

    fun limparSelecaoDaPeca() {
        if (
            ModoColarDaSelecaoCopiadaNaComposicao.toqueForaCancelaModo(modoColarDaSelecao)
        ) {
            cancelarModoColarDaSelecao()
            return
        }
        val idPeca = selecionadoId
        val deveFundir = fundirPecaAoEncerrarTransformar &&
            idPeca != -1 &&
            idOrigemDoTransformar != -1
        if (deveFundir) {
            fundirPecaTransformadaNaOrigem(idPeca, idOrigemDoTransformar)
        } else if (
            ToqueAposMoverComMolduraDeTransformarDaPeca.encerrarEntraNoHistorico(
                pecaEmModoTransformar = pecaEmModoTransformar,
                idDaPecaSelecionada = idPeca,
            )
        ) {
            empilhar(Passo.ModoTransformarDaPeca(idPeca, ativo = true))
        }
        fundirPecaAoEncerrarTransformar = false
        idOrigemDoTransformar = -1
        selecionadoId = -1
        pecaEmModoTransformar = false
        aguardandoSoltarAposArmarTransformar = false
        aoMudarSelecao?.invoke()
        invalidate()
    }

    fun temImagemAlvoDoRecorte(): Boolean {
        if (imagemDoRecorte() != null) return true
        val ativa = objetos.find { it.id == idDaCamadaAtiva }
        return FonteDoDestacarDaCamadaAtiva.aceita(
            ehImagem = ativa is Peca.Imagem,
            ehFolha = ativa is Peca.Folha,
        )
    }

    fun selecaoAtivaDoLaco(): Boolean =
        ferramenta == Ferramenta.RECORTE &&
            formaDaSelecao == FormaDaSelecaoSobreAImagem.LACO &&
            lacoFechado

    fun selecaoAtivaDoRetangulo(): Boolean =
        SelecaoAtivaDoRetanguloDaComposicao.estaAtiva(
            ferramentaDeRecorte = ferramenta == Ferramenta.RECORTE,
            formaRetangulo = formaDaSelecao == FormaDaSelecaoSobreAImagem.RETANGULO,
            largura = recorte.width(),
            altura = recorte.height(),
        )

    fun selecaoAtivaDaFerramentaDeRecorte(): Boolean =
        selecaoAtivaDoLaco() || selecaoAtivaDoRetangulo()

    fun cancelarSelecaoAtivaDoLaco() {
        if (selecaoAtivaDoRetangulo()) {
            selecaoDoRetanguloInvertida = false
            sairDoRecorte()
            aoMudarSelecaoAtivaDoLaco?.invoke()
            return
        }
        if (!lacoFechado) return
        empilhar(
            Passo.SelecaoDoLaco(
                pontos = pontosDoLaco.map { PontoDoLacoLivre(it.x, it.y) },
                inicios = iniciosDosTracosDoLaco.toList(),
                fechado = true,
                invertida = selecaoDoLacoInvertida,
            ),
        )
        limparLaco()
        modoDaMascaraDoLaco = ModoDaMascaraDoLaco.NENHUM
        aoMudarSelecaoAtivaDoLaco?.invoke()
        invalidate()
    }

    fun confirmarRecorteDaSelecaoAtivaNaMoldura(): Boolean {
        val ok = when {
            selecaoAtivaDoLaco() -> reduzirMolduraASelecaoDoLaco()
            selecaoAtivaDoRetangulo() -> reduzirMolduraASelecaoDoRetangulo()
            else -> false
        }
        if (ok) aoMudarSelecaoAtivaDoLaco?.invoke()
        return ok
    }

    fun iniciarArrasteDaCamada() {
        ordemAntesDoArraste = objetos.map { it.id }
    }

    fun cancelarArrasteDaCamada() {
        ordemAntesDoArraste = null
    }

    fun moverCamadaNaLista(indice: Int, destino: Int) {
        val depois = PilhaDeCamadasDaComposicao.mover(objetos.map { it.id }, indice, destino)
        aplicarOrdem(depois)
        invalidate()
    }

    fun concluirArrasteDaCamada() {
        val antes = ordemAntesDoArraste
        ordemAntesDoArraste = null
        if (antes == null || antes == objetos.map { it.id }) return
        empilhar(Passo.Ordem(antes))
    }

    class CamadaDaPilha(
        val id: Int,
        val texto: String?,
        val imagem: Bitmap?,
        val visivel: Boolean,
        val opacidade: Float = 1f,
        /** Cache de tinta da camada (miniatura de folha desenhada à mão); pode ser null. */
        val tinta: Bitmap? = null,
    )

    private fun aplicarCorDaFolha(cor: Int) {
        corDaFolha = cor
        fundoDaFolha.color = cor
    }

    fun larguraDaMoldura(): Int = molduraLargura

    fun alturaDaMoldura(): Int = molduraAltura

    fun proporcaoAtual(): ProporcaoDaMolduraDaComposicao? {
        return MolduraDaComposicaoDeImagem.reconhecer(molduraLargura, molduraAltura)
    }

    fun definirProporcao(proporcao: ProporcaoDaMolduraDaComposicao) {
        val (largura, altura) = MolduraDaComposicaoDeImagem.tamanho(proporcao)
        aplicarTamanho(largura, altura)
    }

    fun dimensoesDaImagemAlvo(): Pair<Int, Int>? {
        val imagem = imagemAlvo() ?: return null
        if (imagem.bitmap.isRecycled) return null
        return imagem.bitmap.width to imagem.bitmap.height
    }

    fun definirProporcaoOriginal(): Boolean {
        val dimensoes = dimensoesDaImagemAlvo() ?: return false
        val (largura, altura) = MolduraDaComposicaoDeImagem.tamanhoDaProporcaoOriginal(
            dimensoes.first,
            dimensoes.second,
        )
        aplicarTamanho(largura, altura)
        return true
    }

    fun definirTamanhoLivre(largura: Int, altura: Int) {
        aplicarTamanho(MolduraDaComposicaoDeImagem.limitar(largura), MolduraDaComposicaoDeImagem.limitar(altura))
    }

    private fun ferramentaEhBorracha() = ferramenta == Ferramenta.BORRACHA

    /** Tamanho (0..1) da ferramenta atual; pincel e borracha guardam cada um o seu (como no Procreate). */
    var tamanhoDaFerramenta: Float
        get() = if (ferramentaEhBorracha()) tamanhoDaBorracha else tamanhoDoPincel
        set(valor) {
            val limitado = valor.coerceIn(0f, 1f)
            if (ferramentaEhBorracha()) tamanhoDaBorracha = limitado else tamanhoDoPincel = limitado
        }

    /** Opacidade da ferramenta atual: a do pincel ([opacidadeDoTraco]) ou a da borracha. */
    var opacidadeDaFerramenta: Float
        get() = if (ferramentaEhBorracha()) opacidadeDaBorracha else opacidadeDoTraco
        set(valor) {
            if (ferramentaEhBorracha()) opacidadeDaBorracha = valor else opacidadeDoTraco = valor
        }

    /** Borracha com opacidade: abaixo de 1 apaga só em parte. */
    var opacidadeDaBorracha: Float = 1f
        set(valor) {
            field = valor.coerceIn(0.05f, 1f)
        }

    fun diminuirEspessura() {
        tamanhoDaFerramenta -= TamanhoDoPincelDoEditorDeImagem.PASSO
    }

    fun aumentarEspessura() {
        tamanhoDaFerramenta += TamanhoDoPincelDoEditorDeImagem.PASSO
    }

    /** Tamanho atual em % (1..100). */
    fun espessuraAtual(): Int = TamanhoDoPincelDoEditorDeImagem.percentual(tamanhoDaFerramenta)

    /** Compat: nível 0..99 vira o tamanho contínuo. */
    fun definirNivelDeEspessura(nivel: Int) {
        tamanhoDaFerramenta = nivel / 99f
    }

    /**
     * Opacidade dos próximos traços (0.05..1), multiplicada na transparência da ponta
     * (o marcador já é translúcido). A cor do traço guarda o resultado, então o rascunho não muda.
     */
    var opacidadeDoTraco: Float = 1f
        set(valor) {
            field = valor.coerceIn(0.05f, 1f)
        }

    /** Estabilização do traço (0 = desligada, 1 = bem suave). */
    var estabilizacaoDoTraco: Float = 0f
        set(valor) {
            field = valor.coerceIn(0f, 1f)
        }

    /** Curva de pressão da caneta (editável no gráfico, estilo Procreate). */
    var curvaDePressao: com.chromebrothers.navegador.regras.CurvaDePressaoDaCaneta =
        com.chromebrothers.navegador.regras.CurvaDePressaoDaCaneta.LINEAR
    private var suavizadoX = 0f
    private var suavizadoY = 0f
    // StreamLine do pincel: ponto que segue a ponta com atraso (e o tempo do último evento).
    private var streamlineX = 0f
    private var streamlineY = 0f
    private var tempoDoStreamline = 0L
    /** Último ponto do traço (ao levantar a caneta) vai cru: o traço chega onde a caneta estava. */
    private var finalizandoTraco = false

    // ---------- Ajustes de imagem da camada (painel "Ajustes") ----------
    private var ajusteEmPrevia: Pair<Int, AjustesDeImagemDaCamada.Valores>? = null
    private var previaDesfocada: Bitmap? = null
    private var previaDesfocadaDe: Bitmap? = null
    private var previaDesfocadaRaio = 0
    private val pinturaDoAjuste = Paint(Paint.FILTER_BITMAP_FLAG)
    var aplicandoAjuste = false
        private set

    /** Camada ativa aceita ajustes (imagem ou desenho; texto não). */
    fun camadaAtivaAceitaAjustes(): Boolean {
        val peca = objetos.find { it.id == idDaCamadaAtiva }
        return peca is Peca.Imagem || (peca is Peca.Folha && peca.tracos.isNotEmpty())
    }

    /** Prévia ao vivo na camada ativa (não altera a camada). */
    fun mostrarPreviaDoAjuste(valores: AjustesDeImagemDaCamada.Valores) {
        if (aplicandoAjuste) return
        ajusteEmPrevia = if (valores.neutro) null else idDaCamadaAtiva to valores
        invalidate()
    }

    fun cancelarPreviaDoAjuste() {
        if (ajusteEmPrevia == null) return
        ajusteEmPrevia = null
        previaDesfocada?.takeIf { !it.isRecycled }?.recycle()
        previaDesfocada = null
        previaDesfocadaDe = null
        invalidate()
    }

    /** Pintura com a matriz de cor da prévia, se [peca] estiver em prévia. */
    private fun pinturaDaPrevia(peca: Peca): Paint? {
        val (id, valores) = ajusteEmPrevia ?: return null
        if (id != peca.id || valores.semCor) return null
        pinturaDoAjuste.colorFilter = android.graphics.ColorMatrixColorFilter(AjustesDeImagemDaCamada.matriz(valores))
        return pinturaDoAjuste
    }

    /** Versão desfocada (prévia) de um bitmap da tela; cache de uma entrada. */
    private fun comDesfoqueDaPrevia(peca: Peca, fonte: Bitmap): Bitmap {
        val (id, valores) = ajusteEmPrevia ?: return fonte
        if (id != peca.id || valores.desfoque <= 0 || fonte.isRecycled) return fonte
        val atual = previaDesfocada
        if (atual != null && !atual.isRecycled && previaDesfocadaDe === fonte && previaDesfocadaRaio == valores.desfoque) {
            return atual
        }
        val pixels = IntArray(fonte.width * fonte.height)
        fonte.getPixels(pixels, 0, fonte.width, 0, 0, fonte.width, fonte.height)
        // Raio pedido em unidades da moldura; a fonte de tela já está ~na escala da moldura.
        val escala = fonte.width / peca.largura.coerceAtLeast(1f)
        AjustesDeImagemDaCamada.desfocar(pixels, fonte.width, fonte.height, (valores.desfoque * escala).toInt().coerceAtLeast(1))
        val saida = Bitmap.createBitmap(fonte.width, fonte.height, Bitmap.Config.ARGB_8888)
        saida.setPixels(pixels, 0, fonte.width, 0, 0, fonte.width, fonte.height)
        atual?.takeIf { !it.isRecycled }?.recycle()
        previaDesfocada = saida
        previaDesfocadaDe = fonte
        previaDesfocadaRaio = valores.desfoque
        return saida
    }

    /**
     * Grava o ajuste na camada ativa em resolução total (desenho vira imagem antes). Roda numa thread;
     * [aoTerminar] volta na thread principal. Entra no desfazer como um passo.
     */
    fun aplicarAjusteNaCamada(valores: AjustesDeImagemDaCamada.Valores, aoTerminar: (Boolean) -> Unit) {
        if (aplicandoAjuste || valores.neutro) {
            aoTerminar(false)
            return
        }
        var peca = objetos.find { it.id == idDaCamadaAtiva }
        if (peca is Peca.Folha) peca = promoverFolhaParaImagem(peca)
        val imagem = peca as? Peca.Imagem
        if (imagem == null || imagem.bitmap.isRecycled) {
            aoTerminar(false)
            return
        }
        fundirTracosNaImagem(imagem)
        val fonte = imagem.bitmap
        val id = imagem.id
        val matriz = AjustesDeImagemDaCamada.matriz(valores)
        val raioNaFonte = valores.desfoque * fonte.width / imagem.largura.coerceAtLeast(1f)
        aplicandoAjuste = true
        Thread {
            val nova = try {
                val saida = Bitmap.createBitmap(fonte.width, fonte.height, Bitmap.Config.ARGB_8888)
                saida.setHasAlpha(true)
                val pintura = Paint(Paint.FILTER_BITMAP_FLAG)
                if (!valores.semCor) pintura.colorFilter = android.graphics.ColorMatrixColorFilter(matriz)
                Canvas(saida).drawBitmap(fonte, 0f, 0f, pintura)
                if (raioNaFonte >= 0.5f) desfocarGrande(saida, raioNaFonte)
                saida
            } catch (_: OutOfMemoryError) {
                null
            }
            post {
                aplicandoAjuste = false
                val alvo = objetos.find { it.id == id } as? Peca.Imagem
                if (nova == null || alvo == null || alvo.bitmap !== fonte) {
                    nova?.recycle()
                    aoTerminar(false)
                    return@post
                }
                alvo.bitmap = nova
                empilhar(Passo.AlteraBitmap(id, fonte))
                cancelarPreviaDoAjuste()
                camadaSuja = true
                aoMudarSelecao?.invoke()
                invalidate()
                aoTerminar(true)
            }
        }.start()
    }

    /**
     * Desfoque de um bitmap grande sem estourar a memória: desfoca uma cópia de no máximo ~1,5 MP
     * (o desfoque some com os detalhes, então reduzir antes não aparece) e desenha de volta por cima.
     */
    private fun desfocarGrande(alvo: Bitmap, raio: Float) {
        val pixelsTotais = alvo.width.toLong() * alvo.height
        val reducao = if (pixelsTotais > 1_500_000L) Math.sqrt(1_500_000.0 / pixelsTotais).toFloat() else 1f
        val w = (alvo.width * reducao).toInt().coerceAtLeast(1)
        val h = (alvo.height * reducao).toInt().coerceAtLeast(1)
        val pequeno = if (reducao < 1f) Bitmap.createScaledBitmap(alvo, w, h, true) else alvo.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(w * h)
        pequeno.getPixels(pixels, 0, w, 0, 0, w, h)
        AjustesDeImagemDaCamada.desfocar(pixels, w, h, (raio * reducao).toInt().coerceAtLeast(1))
        pequeno.setPixels(pixels, 0, w, 0, 0, w, h)
        alvo.eraseColor(Color.TRANSPARENT)
        Canvas(alvo).drawBitmap(pequeno, null, RectF(0f, 0f, alvo.width.toFloat(), alvo.height.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
        pequeno.recycle()
    }

    /** Diâmetro do pincel na tela (px) com o zoom atual, para a prévia do controle de tamanho. */
    fun diametroDoPincelNaTela(): Float {
        val borracha = ferramenta == Ferramenta.BORRACHA
        val multiplicador = if (borracha) 1f else GeometriaDaPontaDoTraco.multiplicadorDeLargura(pontaDoTraco)
        val tamanho = if (borracha) tamanhoDaBorracha else tamanhoDoPincel
        val nominal = TamanhoDoPincelDoEditorDeImagem.diametroDp(tamanho, borracha) * resources.displayMetrics.density * multiplicador
        if (molduraLargura <= 0 || destino.width() <= 0f) return nominal
        val escalaDaVista = destino.width() / molduraLargura.toFloat()
        return EscalaDoPincelNaFolha.diametroNaTela(nominal, escalaDeReferenciaDoPincel(), escalaDaVista)
    }

    /** Pressão do ponto × transparência da cor (opacidade escolhida); lápis antigo (cor opaca) fica igual. */
    private fun corComAlphaDoPonto(cor: Int, alphaDoPonto: Int): Int {
        val alpha = ((cor ushr 24) * alphaDoPonto.coerceIn(0, 255)) / 255
        return (cor and 0x00FFFFFF) or (alpha shl 24)
    }

    private fun comOpacidadeDoTraco(cor: Int): Int {
        val alpha = ((cor ushr 24) * opacidadeDoTraco).toInt().coerceIn(1, 255)
        return (cor and 0x00FFFFFF) or (alpha shl 24)
    }

    fun quantidadeDeEspessuras(): Int = 100

    fun ferramentaAtual(): Ferramenta = ferramenta

    fun definirFerramenta(proxima: Ferramenta) {
        if (ferramenta == Ferramenta.RECORTE && proxima == Ferramenta.RECORTE) {
            aplicarRecorte()
            return
        }
        if (
            proxima != ferramenta &&
            ModoColarDaSelecaoCopiadaNaComposicao.trocaDeFerramentaCancelaModo(modoColarDaSelecao)
        ) {
            cancelarModoColarDaSelecao()
        } else if (proxima != ferramenta && pecaEmModoTransformar) {
            limparSelecaoDaPeca()
        }
        ferramenta = proxima
        if (proxima == Ferramenta.VERMELHO || proxima == Ferramenta.BRANCO) {
            ultimaFerramentaDePincel = proxima
        }
        origemDaSeta = null
        previaDaSeta = null
        if (proxima == Ferramenta.RECORTE) {
            val pelaPeca = objetos.find { it.id == selecionadoId } as? Peca.Imagem
            val pelaCamada = objetos.find { it.id == idDaCamadaAtiva } as? Peca.Imagem
            idDaImagemDoRecorte = pelaPeca?.id ?: pelaCamada?.id ?: -1
            formaDaSelecao = FormaDaSelecaoSobreAImagem.RETANGULO
            limparLaco()
            selecaoInteligenteArmada = false
            limparSelecaoDoRetangulo()
            aoMudarSelecaoAtivaDoLaco?.invoke()
        } else {
            idDaImagemDoRecorte = -1
            selecaoInteligenteArmada = false
        }
        invalidate()
    }

    fun formaDaSelecaoAtual(): FormaDaSelecaoSobreAImagem = formaDaSelecao

    fun selecaoInteligenteArmada(): Boolean = selecaoInteligenteArmada

    fun alternarSelecaoInteligente() {
        if (ferramenta != Ferramenta.RECORTE) return
        if (formaDaSelecao != FormaDaSelecaoSobreAImagem.RETANGULO) return
        selecaoInteligenteArmada = !selecaoInteligenteArmada
        aoMudarModoDoDedo?.invoke()
        invalidate()
    }

    fun definirFormaDaSelecao(forma: FormaDaSelecaoSobreAImagem) {
        if (ferramenta != Ferramenta.RECORTE) return
        formaDaSelecao = forma
        limparLaco()
        selecaoDoRetanguloInvertida = false
        selecaoInteligenteArmada = false
        if (forma == FormaDaSelecaoSobreAImagem.LACO) {
            recortarDentroDaMoldura = true
        } else if (forma == FormaDaSelecaoSobreAImagem.RETANGULO) {
            if (!RetanguloDeRecorteComecaVazioComoOLaco.devePreSelecionarAoEntrar(selecaoInteligenteArmada)) {
                limparSelecaoDoRetangulo()
            }
        }
        aoMudarSelecaoAtivaDoLaco?.invoke()
        invalidate()
    }

    /** Quantidade de passos do último pacote do Mover colocado em [desfeitos] (para refazer em pacote). */
    private var tamanhoDoPacoteDoMoverEmDesfeitos = 0

    fun desfazer() {
        if (desfazerUltimoTracoDoLaco()) {
            aoMudarSelecaoAtivaDoLaco?.invoke()
            return
        }
        if (desfazerPacoteDoMoverSeHouver()) return
        tamanhoDoPacoteDoMoverEmDesfeitos = 0
        val passo = passos.removeLastOrNull() ?: return
        val inverso = reverter(passo)
        if (inverso == null) {
            passos += passo
            return
        }
        desfeitos += inverso
        desfazerPromocoesNoTopo()
        garantirCamadaSelecionada()
        aoMudarSelecao?.invoke()
        invalidate()
    }

    /** Desfaz a promoção (desenho -> imagem) que ficou exposta: sozinha ela não muda nada na tela. */
    private fun desfazerPromocoesNoTopo(): Int {
        var quantas = 0
        while (true) {
            val topo = passos.lastOrNull() as? Passo.Promocao ?: break
            if (!topo.aplicado) break
            passos.removeAt(passos.lastIndex)
            val inverso = reverter(topo)
            if (inverso == null) {
                passos += topo
                break
            }
            desfeitos += inverso
            quantas++
        }
        return quantas
    }

    fun refazer() {
        if (refazerPacoteDoMoverSeHouver()) return
        val passo = desfeitos.removeLastOrNull() ?: return
        val inverso = reverter(passo)
        if (inverso == null) {
            desfeitos += passo
            return
        }
        passos += inverso
        while (passos.size > 12) descartarPassoMaisAntigoComCuidadoDoMover()
        garantirCamadaSelecionada()
        aoMudarSelecao?.invoke()
        invalidate()
        // Refez só a promoção: refaz também a ação que a pediu.
        if (passo is Passo.Promocao && desfeitos.isNotEmpty()) refazer()
    }

    private fun desfazerPacoteDoMoverSeHouver(): Boolean {
        val idTransformar = if (pecaEmModoTransformar && selecionadoId != -1) selecionadoId else -1
        val entradas = passos.map { resumirPassoParaPacoteDoMover(it) }
        val inicio = PacoteDoDesfazerDoMoverDaSelecaoAtiva.indiceInicioDoPacoteNoTopo(
            passos = entradas,
            idPecaEmTransformar = idTransformar,
        )
        if (inicio < 0) return false
        val quantidade = passos.size - inicio
        val inversos = ArrayList<Passo>(quantidade)
        repeat(quantidade) {
            val passo = passos.removeLastOrNull() ?: return@repeat
            val inverso = reverter(passo)
            if (inverso == null) {
                passos += passo
                while (inversos.isNotEmpty()) {
                    val voltar = inversos.removeLast()
                    val reaplicado = reverter(voltar)
                    if (reaplicado != null) passos += reaplicado
                }
                return false
            }
            inversos += inverso
        }
        desfeitos.addAll(inversos)
        tamanhoDoPacoteDoMoverEmDesfeitos = inversos.size + desfazerPromocoesNoTopo()
        garantirCamadaSelecionada()
        aoMudarSelecao?.invoke()
        invalidate()
        return true
    }

    private fun refazerPacoteDoMoverSeHouver(): Boolean {
        val n = tamanhoDoPacoteDoMoverEmDesfeitos
        if (n <= 0 || desfeitos.size < n) {
            tamanhoDoPacoteDoMoverEmDesfeitos = 0
            return false
        }
        val refeitos = ArrayList<Passo>(n)
        repeat(n) {
            val passo = desfeitos.removeLastOrNull() ?: return@repeat
            val inverso = reverter(passo)
            if (inverso == null) {
                desfeitos += passo
                while (refeitos.isNotEmpty()) {
                    val voltar = refeitos.removeLast()
                    val desfeito = reverter(voltar)
                    if (desfeito != null) desfeitos += desfeito
                }
                tamanhoDoPacoteDoMoverEmDesfeitos = 0
                return false
            }
            refeitos += inverso
            passos += inverso
        }
        while (passos.size > 12) descartarPassoMaisAntigoComCuidadoDoMover()
        tamanhoDoPacoteDoMoverEmDesfeitos = 0
        garantirCamadaSelecionada()
        aoMudarSelecao?.invoke()
        invalidate()
        return true
    }

    private fun resumirPassoParaPacoteDoMover(passo: Passo): PacoteDoDesfazerDoMoverDaSelecaoAtiva.Entrada {
        return when (passo) {
            is Passo.Destaque -> PacoteDoDesfazerDoMoverDaSelecaoAtiva.Entrada(
                PacoteDoDesfazerDoMoverDaSelecaoAtiva.TipoDePassoDoMover.DESTAQUE_NOVA,
                passo.idNova,
            )
            is Passo.Transformacao -> PacoteDoDesfazerDoMoverDaSelecaoAtiva.Entrada(
                PacoteDoDesfazerDoMoverDaSelecaoAtiva.TipoDePassoDoMover.TRANSFORMACAO,
                passo.id,
            )
            is Passo.ModoTransformarDaPeca -> PacoteDoDesfazerDoMoverDaSelecaoAtiva.Entrada(
                PacoteDoDesfazerDoMoverDaSelecaoAtiva.TipoDePassoDoMover.MODO_TRANSFORMAR,
                passo.idDaPeca,
            )
            is Passo.FundirPecaTransformada -> PacoteDoDesfazerDoMoverDaSelecaoAtiva.Entrada(
                PacoteDoDesfazerDoMoverDaSelecaoAtiva.TipoDePassoDoMover.FUNDIR,
                passo.idPeca,
            )
            else -> PacoteDoDesfazerDoMoverDaSelecaoAtiva.Entrada(
                PacoteDoDesfazerDoMoverDaSelecaoAtiva.TipoDePassoDoMover.OUTRO,
                -1,
            )
        }
    }

    private fun limparEstadoDoTransformarDaPeca(idPeca: Int) {
        if (selecionadoId == idPeca) selecionadoId = -1
        if (idOrigemDoTransformar != -1 && (selecionadoId == -1 || idPeca == selecionadoId)) {
            fundirPecaAoEncerrarTransformar = false
            idOrigemDoTransformar = -1
        }
        if (pecaEmModoTransformar && (selecionadoId == -1 || selecionadoId == idPeca)) {
            pecaEmModoTransformar = false
        }
        aguardandoSoltarAposArmarTransformar = false
    }

    private fun reverter(passo: Passo): Passo? {
        return when (passo) {
            is Passo.Risco -> {
                val peca = objetos.find { it.id == passo.traco.idDaPeca }
                    ?: pecasFora[passo.traco.idDaPeca]
                    ?: return null
                val anterior = passo.bitmapAnterior
                val imagem = if (anterior == null) {
                    null
                } else {
                    (objetos + pecasFora.values).find { it.id == passo.idDaImagem } as? Peca.Imagem
                }
                camadaSuja = true
                if (imagem == null || anterior == null) {
                    if (!peca.tracos.remove(passo.traco)) peca.tracos += passo.traco
                    peca.tintaSuja = true
                    passo
                } else {
                    val agora = imagem.bitmap
                    val estadoAgora = passo.estadoAnterior?.let { capturar(imagem) }
                    imagem.bitmap = anterior
                    passo.estadoAnterior?.let { restaurar(imagem, it) }
                    Passo.Risco(passo.traco, agora, passo.idDaImagem, estadoAgora)
                }
            }
            is Passo.Inclusao -> {
                val naFolha = objetos.find { it.id == passo.id }
                if (naFolha != null) {
                    objetos.remove(naFolha)
                    pecasFora[naFolha.id] = naFolha
                    if (selecionadoId == naFolha.id) selecionadoId = -1
                    if (idDaCamadaAtiva == naFolha.id) idDaCamadaAtiva = objetos.lastOrNull()?.id ?: -1
                } else {
                    val fora = pecasFora.remove(passo.id) ?: return null
                    objetos += fora
                    idDaCamadaAtiva = fora.id
                }
                passo
            }
            is Passo.Transformacao -> {
                val peca = objetos.find { it.id == passo.id } ?: return null
                val agora = capturar(peca)
                restaurar(peca, passo.estado)
                Passo.Transformacao(passo.id, agora)
            }
            is Passo.TextoAlterado -> {
                val peca = objetos.find { it.id == passo.id } as? Peca.Texto ?: return null
                val inverso = Passo.TextoAlterado(peca.id, peca.texto, peca.cor)
                peca.texto = passo.texto
                peca.cor = passo.cor
                medir(peca)
                inverso
            }
            is Passo.Moldura -> {
                val inverso = Passo.Moldura(molduraLargura, molduraAltura)
                molduraLargura = passo.largura
                molduraAltura = passo.altura
                zoom = 1f
                anguloDaVista = 0f
                panX = 0f
                panY = 0f
                invalidarTintas()
                camadaSuja = true
                inverso
            }
            is Passo.Folha -> {
                val inverso = Passo.Folha(corDaFolha)
                aplicarCorDaFolha(passo.cor)
                inverso
            }
            is Passo.Ordem -> {
                val inverso = Passo.Ordem(objetos.map { it.id })
                aplicarOrdem(passo.ids)
                inverso
            }
            is Passo.Corte -> {
                val imagem = objetos.find { it.id == passo.id } as? Peca.Imagem ?: return null
                val inverso = Passo.Corte(
                    passo.id,
                    imagem.bitmap,
                    capturar(imagem),
                    molduraLargura,
                    molduraAltura,
                    copiarTracos(),
                )
                imagem.bitmap = passo.bitmapAnterior
                restaurar(imagem, passo.estado)
                molduraLargura = passo.molduraLargura
                molduraAltura = passo.molduraAltura
                restaurarTracos(passo.riscos)
                zoom = 1f
                anguloDaVista = 0f
                panX = 0f
                panY = 0f
                invalidarTintas()
                camadaSuja = true
                inverso
            }
            is Passo.Destaque -> {
                val origem = objetos.find { it.id == passo.idOrigem } as? Peca.Imagem
                val naFolha = objetos.find { it.id == passo.idNova }
                val fora = if (naFolha == null) pecasFora[passo.idNova] else null
                val bitmapAgora = origem?.bitmap
                if (origem != null && passo.bitmapAnterior != null) origem.bitmap = passo.bitmapAnterior
                if (naFolha != null) {
                    objetos.remove(naFolha)
                    pecasFora[naFolha.id] = naFolha
                    if (idDaCamadaAtiva == naFolha.id) idDaCamadaAtiva = origem?.id ?: objetos.lastOrNull()?.id ?: -1
                    limparEstadoDoTransformarDaPeca(naFolha.id)
                } else if (fora != null) {
                    pecasFora.remove(passo.idNova)
                    val posicao = if (origem == null) -1 else objetos.indexOf(origem)
                    val indice = if (posicao < 0) objetos.size else posicao + 1
                    objetos.add(indice, fora)
                    idDaCamadaAtiva = fora.id
                } else if (origem == null || passo.bitmapAnterior == null) {
                    limparEstadoDoTransformarDaPeca(passo.idNova)
                    return null
                } else {
                    limparEstadoDoTransformarDaPeca(passo.idNova)
                }
                camadaSuja = true
                Passo.Destaque(passo.idOrigem, bitmapAgora, passo.idNova)
            }
            is Passo.SelecaoDoLaco -> {
                val inverso = Passo.SelecaoDoLaco(
                    pontos = pontosDoLaco.map { PontoDoLacoLivre(it.x, it.y) },
                    inicios = iniciosDosTracosDoLaco.toList(),
                    fechado = lacoFechado,
                    invertida = selecaoDoLacoInvertida,
                )
                aplicarEstadoDoLaco(passo)
                inverso
            }
            is Passo.AlteraBitmap -> {
                val imagem = objetos.find { it.id == passo.id } as? Peca.Imagem ?: return null
                val agora = imagem.bitmap
                imagem.bitmap = passo.bitmapAnterior
                camadaSuja = true
                Passo.AlteraBitmap(passo.id, agora)
            }
            is Passo.ModoTransformarDaPeca -> {
                if (passo.ativo && objetos.none { it.id == passo.idDaPeca }) {
                    limparEstadoDoTransformarDaPeca(passo.idDaPeca)
                    return Passo.ModoTransformarDaPeca(passo.idDaPeca, ativo = false)
                }
                val inverso = Passo.ModoTransformarDaPeca(
                    idDaPeca = if (pecaEmModoTransformar && selecionadoId != -1) {
                        selecionadoId
                    } else {
                        passo.idDaPeca
                    },
                    ativo = pecaEmModoTransformar,
                )
                if (passo.ativo) {
                    selecionadoId = passo.idDaPeca
                    pecaEmModoTransformar = true
                    idDaCamadaAtiva = passo.idDaPeca
                } else {
                    if (selecionadoId == passo.idDaPeca) selecionadoId = -1
                    pecaEmModoTransformar = false
                }
                inverso
            }
            is Passo.Opacidade -> {
                val peca = objetos.find { it.id == passo.id } ?: return null
                val inverso = Passo.Opacidade(passo.id, peca.opacidade)
                peca.opacidade = passo.valor
                camadaSuja = true
                aoMudarSelecao?.invoke()
                inverso
            }
            is Passo.Promocao -> {
                val indice = objetos.indexOfFirst { it.id == passo.folha.id }
                if (indice < 0) return null
                if (passo.aplicado) {
                    passo.folha.alfaBloqueado = passo.imagem.alfaBloqueado
                    objetos[indice] = passo.folha
                    passo.folha.tintaSuja = true
                    if (idDaImagemDoRecorte == passo.folha.id) idDaImagemDoRecorte = -1
                } else {
                    objetos[indice] = passo.imagem
                }
                camadaSuja = true
                Passo.Promocao(passo.folha, passo.imagem, !passo.aplicado)
            }
            is Passo.MesclarCamadas -> {
                if (passo.aplicado) {
                    val indice = objetos.indexOfFirst { it.id == passo.mesclada.id }
                    if (indice < 0) return null
                    objetos[indice] = passo.baixo
                    objetos.add(indice + 1, passo.cima)
                    idDaCamadaAtiva = passo.cima.id
                } else {
                    val iBaixo = objetos.indexOfFirst { it.id == passo.baixo.id }
                    val iCima = objetos.indexOfFirst { it.id == passo.cima.id }
                    if (iBaixo < 0 || iCima < 0) return null
                    objetos.removeAt(maxOf(iBaixo, iCima))
                    objetos[minOf(iBaixo, iCima)] = passo.mesclada
                    idDaCamadaAtiva = passo.mesclada.id
                }
                selecionadoId = -1
                camadaSuja = true
                aoMudarSelecao?.invoke()
                Passo.MesclarCamadas(passo.cima, passo.baixo, passo.mesclada, !passo.aplicado)
            }
            is Passo.FundirPecaTransformada -> {
                val origem = objetos.find { it.id == passo.idOrigem } as? Peca.Imagem ?: return null
                val peca = pecasFora[passo.idPeca] as? Peca.Imagem
                    ?: objetos.find { it.id == passo.idPeca } as? Peca.Imagem
                    ?: return null
                val bitmapAgora = origem.bitmap
                val estadoAgora = passo.estadoOrigemAntes?.let { capturar(origem) }
                origem.bitmap = passo.bitmapOrigemAntes
                passo.estadoOrigemAntes?.let { restaurar(origem, it) }
                if (pecasFora.containsKey(passo.idPeca)) {
                    pecasFora.remove(passo.idPeca)
                    val posicao = objetos.indexOf(origem)
                    val indice = if (posicao < 0) objetos.size else posicao + 1
                    objetos.add(indice, peca)
                }
                selecionadoId = passo.idPeca
                pecaEmModoTransformar = true
                idDaCamadaAtiva = passo.idPeca
                fundirPecaAoEncerrarTransformar = true
                idOrigemDoTransformar = passo.idOrigem
                camadaSuja = true
                Passo.FundirPecaTransformada(passo.idPeca, passo.idOrigem, bitmapAgora, estadoAgora)
            }
        }
    }

    private fun aplicarEstadoDoLaco(passo: Passo.SelecaoDoLaco) {
        val mudouFerramenta = ferramenta != Ferramenta.RECORTE
        ferramenta = Ferramenta.RECORTE
        formaDaSelecao = FormaDaSelecaoSobreAImagem.LACO
        pontosDoLaco.clear()
        pontosDoLaco.addAll(passo.pontos)
        iniciosDosTracosDoLaco.clear()
        iniciosDosTracosDoLaco.addAll(passo.inicios)
        lacoFechado = passo.fechado && passo.pontos.size >= 3
        selecaoDoLacoInvertida = passo.invertida && lacoFechado
        modoDaMascaraDoLaco = ModoDaMascaraDoLaco.NENHUM
        chipsDaSelecaoAtiva.clear()
        pontosDoLacoNaOrigem = if (lacoFechado) {
            passo.pontos.map { PontoDoLacoLivre(it.x, it.y) }
        } else {
            emptyList()
        }
        if (mudouFerramenta) aoMudarFerramenta?.invoke(ferramenta)
        aoMudarModoDoDedo?.invoke()
        aoMudarSelecaoAtivaDoLaco?.invoke()
    }

    private var restauradoDeRascunho = false
    private val bitmapsEmprestados = Collections.newSetFromMap(IdentityHashMap<Bitmap, Boolean>())

    fun temAlteracao(): Boolean = passos.isNotEmpty() || restauradoDeRascunho

    fun temGravacaoPendente(): Boolean = DecisaoDeGravarRascunhoDaEdicaoDeImagem.deveGravar(passos.size)

    fun emprestarBitmaps(bitmaps: Collection<Bitmap>) {
        synchronized(bitmapsEmprestados) { bitmapsEmprestados.addAll(bitmaps) }
    }

    fun devolverBitmap(bitmap: Bitmap) {
        synchronized(bitmapsEmprestados) {
            bitmapsEmprestados.remove(bitmap)
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    fun liberarCachesDeTinta() {
        reciclarTodasAsTintas()
    }

    fun exportar(): Exportacao? {
        if (molduraLargura <= 0 || molduraAltura <= 0) return null
        return Exportacao(
            largura = molduraLargura,
            altura = molduraAltura,
            proximoId = proximoId,
            ferramenta = ferramenta,
            imagens = objetos.filterIsInstance<Peca.Imagem>().map { peca ->
                ImagemExportada(peca.id, peca.bitmap, peca.esquerda, peca.topo, peca.largura, peca.altura)
            },
            textos = objetos.filterIsInstance<Peca.Texto>().map { peca ->
                TextoDoRascunhoDaEdicao(
                    peca.id,
                    peca.texto,
                    peca.tamanho,
                    peca.cor,
                    peca.esquerda,
                    peca.topo,
                    peca.largura,
                    peca.altura,
                )
            },
            tracos = objetos.flatMap { peca ->
                peca.tracos.map { traco ->
                    TracoDoRascunhoDaEdicao(
                        traco.cor,
                        traco.borracha,
                        traco.pontos.map { PontoDoTracoDoRascunhoDaEdicao(it.x, it.y, it.largura, it.alpha) },
                        peca.id,
                        coordenadasDoQuadro = true,
                        ponta = traco.ponta,
                        pincel = traco.pincel?.let { PincelDoMotorEmJson.chave(it) }.orEmpty(),
                        semente = traco.semente,
                    )
                }
            },
            corDaFolha = corDaFolha,
            angulos = objetos.filter { it.angulo != 0f }.associate { it.id to it.angulo },
            ordem = objetos.map { it.id },
            camadasVazias = objetos.filterIsInstance<Peca.Folha>().map { peca ->
                com.chromebrothers.navegador.regras.CamadaVaziaDoRascunhoDaEdicao(
                    peca.id,
                    peca.esquerda,
                    peca.topo,
                    peca.largura,
                    peca.altura,
                )
            },
            espelhos = objetos.mapNotNull { peca ->
                if (!peca.espelhoHorizontal && !peca.espelhoVertical) null
                else EspelhoGravadoDaPecaDaComposicao(peca.id, peca.espelhoHorizontal, peca.espelhoVertical)
            },
            opacidades = objetos.filter { it.opacidade < 0.999f }.associate { it.id to it.opacidade },
            ocultas = objetos.filter { !it.visivel }.map { it.id }.toSet(),
            bloqueadas = objetos.filter { it.alfaBloqueado }.map { it.id }.toSet(),
            pinceis = objetos.flatMap { it.tracos }.mapNotNull { it.pincel }
                .associate { PincelDoMotorEmJson.chave(it) to PincelDoMotorEmJson.paraJson(it) },
        )
    }

    fun restaurar(documento: DocumentoDoRascunhoDaEdicaoDeImagem, bitmaps: Map<String, Bitmap>) {
        soltar()
        molduraLargura = documento.largura
        molduraAltura = documento.altura
        proximoId = documento.proximoId.coerceAtLeast(1)
        documento.imagens.forEach { imagem ->
            val bitmap = bitmaps[imagem.arquivo] ?: return@forEach
            val peca = Peca.Imagem(imagem.id, bitmap)
            peca.esquerda = imagem.esquerda
            peca.topo = imagem.topo
            peca.largura = imagem.largura
            peca.altura = imagem.altura
            objetos += peca
        }
        documento.textos.forEach { texto ->
            val peca = Peca.Texto(texto.id, texto.texto, texto.tamanho, texto.cor)
            peca.esquerda = texto.esquerda
            peca.topo = texto.topo
            peca.largura = texto.largura
            peca.altura = texto.altura
            objetos += peca
        }
        documento.camadasVazias.forEach { camada ->
            val peca = Peca.Folha(camada.id)
            peca.esquerda = camada.esquerda
            peca.topo = camada.topo
            peca.largura = camada.largura
            peca.altura = camada.altura
            objetos += peca
        }
        val tracosSoltos = mutableListOf<TracoDoRascunhoDaEdicao>()
        documento.tracos.forEach { traco ->
            val peca = objetos.find { it.id == traco.idDaPeca }
            if (peca == null) {
                tracosSoltos += traco
            } else {
                peca.tracos += Traco(
                    idDaPeca = peca.id,
                    pontos = pontosNoQuadro(traco, peca),
                    cor = traco.cor,
                    borracha = traco.borracha,
                    ponta = traco.ponta,
                    pincel = pincelDoRascunho(documento, traco.pincel),
                    semente = traco.semente,
                )
            }
        }
        if (tracosSoltos.isNotEmpty()) {
            val folha = Peca.Folha(novoId())
            folha.esquerda = 0f
            folha.topo = 0f
            folha.largura = molduraLargura.toFloat().coerceAtLeast(1f)
            folha.altura = molduraAltura.toFloat().coerceAtLeast(1f)
            tracosSoltos.forEach { traco ->
                folha.tracos += Traco(
                    idDaPeca = folha.id,
                    pontos = traco.pontos.map { ponto ->
                        Ponto(
                            ponto.x / folha.largura,
                            ponto.y / folha.altura,
                            ponto.largura / folha.largura,
                            ponto.alpha,
                        )
                    }.toMutableList(),
                    cor = traco.cor,
                    borracha = traco.borracha,
                    ponta = traco.ponta,
                    pincel = pincelDoRascunho(documento, traco.pincel),
                    semente = traco.semente,
                )
            }
            objetos += folha
        }
        ferramenta = when (documento.ferramenta) {
            FerramentaGravadaDoRascunhoDaEdicao.BRANCO -> Ferramenta.BRANCO
            FerramentaGravadaDoRascunhoDaEdicao.BORRACHA -> Ferramenta.BORRACHA
            FerramentaGravadaDoRascunhoDaEdicao.RECORTE -> Ferramenta.RECORTE
            FerramentaGravadaDoRascunhoDaEdicao.VERMELHO -> Ferramenta.VERMELHO
        }
        aplicarCorDaFolha(documento.corDaFolha)
        objetos.forEach { peca -> peca.angulo = documento.angulos[peca.id] ?: 0f }
        documento.espelhos.forEach { espelho ->
            val peca = objetos.find { it.id == espelho.id } ?: return@forEach
            peca.espelhoHorizontal = espelho.horizontal
            peca.espelhoVertical = espelho.vertical
        }
        if (documento.ordem.isNotEmpty()) aplicarOrdem(documento.ordem)
        objetos.forEach { peca ->
            peca.opacidade = documento.opacidades[peca.id] ?: 1f
            peca.visivel = peca.id !in documento.ocultas
            peca.alfaBloqueado = peca.id in documento.bloqueadas
        }
        restauradoDeRascunho = true
        camadaSuja = true
        idDaCamadaAtiva = objetos.lastOrNull()?.id ?: -1
        selecionadoId = PecaSelecionadaDaEdicaoDeImagem.idAoAbrir()
        garantirCamadaSelecionada()
        colocarRegua()
        aoMudarFerramenta?.invoke(ferramenta)
        aoMudarSelecao?.invoke()
        invalidate()
    }

    class Exportacao(
        val largura: Int,
        val altura: Int,
        val proximoId: Int,
        val ferramenta: Ferramenta,
        val imagens: List<ImagemExportada>,
        val textos: List<TextoDoRascunhoDaEdicao>,
        val tracos: List<TracoDoRascunhoDaEdicao>,
        val corDaFolha: Int,
        val angulos: Map<Int, Float>,
        val ordem: List<Int>,
        val camadasVazias: List<com.chromebrothers.navegador.regras.CamadaVaziaDoRascunhoDaEdicao>,
        val espelhos: List<EspelhoGravadoDaPecaDaComposicao> = emptyList(),
        val opacidades: Map<Int, Float> = emptyMap(),
        val ocultas: Set<Int> = emptySet(),
        val bloqueadas: Set<Int> = emptySet(),
        /** Cópia de cada versão de pincel do motor usada (chave → JSON). */
        val pinceis: Map<String, String> = emptyMap(),
    )

    class ImagemExportada(
        val id: Int,
        val bitmap: Bitmap,
        val esquerda: Float,
        val topo: Float,
        val largura: Float,
        val altura: Float,
    )

    fun compor(): Bitmap? = compor(molduraLargura, molduraAltura, fundoTransparente = false)

    /**
     * Imagem final em [largura]×[altura] px (proporção da moldura), com as camadas desenhadas na
     * resolução delas e reamostradas com suavização. [fundoTransparente]: sem a cor da folha (PNG).
     */
    fun compor(largura: Int, altura: Int, fundoTransparente: Boolean): Bitmap? {
        if (molduraLargura <= 0 || molduraAltura <= 0 || largura <= 0 || altura <= 0) return null
        val saida = try {
            Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
        } catch (_: OutOfMemoryError) {
            return null
        }
        val tela = Canvas(saida)
        if (fundoTransparente) saida.eraseColor(Color.TRANSPARENT) else tela.drawColor(corDaFolha)
        tela.scale(largura / molduraLargura.toFloat(), altura / molduraAltura.toFloat())
        val filtroAntes = tintaDaPecaSemFiltro.isFilterBitmap
        tintaDaPecaSemFiltro.isFilterBitmap = largura != molduraLargura
        try {
            objetos.forEach { if (it.visivel) desenharPeca(tela, it, usarCache = false) }
        } finally {
            tintaDaPecaSemFiltro.isFilterBitmap = filtroAntes
        }
        return saida
    }

    /** Maior detalhe (px por unidade da moldura) entre as camadas de imagem; 1 = só a moldura. */
    fun densidadeMaximaDasCamadas(): Float =
        objetos.filterIsInstance<Peca.Imagem>()
            .filter { it.visivel && !it.bitmap.isRecycled && it.largura > 0f }
            .maxOfOrNull { it.bitmap.width / it.largura }
            ?.coerceAtLeast(1f) ?: 1f

    fun soltar() {
        reciclarTodasAsTintas()
        limparDesfeitos()
        val bitmaps = objetos.filterIsInstance<Peca.Imagem>().map { it.bitmap } +
            pecasFora.values.filterIsInstance<Peca.Imagem>().map { it.bitmap } +
            passos.filterIsInstance<Passo.Corte>().map { it.bitmapAnterior } +
            passos.filterIsInstance<Passo.Destaque>().mapNotNull { it.bitmapAnterior } +
            passos.filterIsInstance<Passo.Risco>().mapNotNull { it.bitmapAnterior }
        synchronized(bitmapsEmprestados) {
            bitmaps.distinct().forEach { if (!it.isRecycled && it !in bitmapsEmprestados) it.recycle() }
        }
        objetos.clear()
        passos.clear()
        pecasFora.clear()
        tracoAtual = null
        idDaCamadaAtiva = -1
        selecionadoId = -1
        pecaEmModoTransformar = false
        aguardandoSoltarAposArmarTransformar = false
        modoColarDaSelecao = false
        idPecaPreviaDoColar = -1
        formaDaSelecao = FormaDaSelecaoSobreAImagem.RETANGULO
        limparLaco()
        ordemAntesDoArraste = null
        proximoId = 1
        idDoPonteiroDoTraco = -1
        idDaImagemDoRecorte = -1
        recortarDentroDaMoldura = true
        selecaoInteligenteArmada = false
        modoDoDedo = ModoDoDedo.NENHUM
        modoDaPeca = ModoDaPeca.NENHUM
        arraste = null
        zoom = 1f
        anguloDaVista = 0f
        panX = 0f
        panY = 0f
        molduraLargura = 0
        molduraAltura = 0
        descartarBufferDaMolduraEmUmParaUm()
        regua = null
        parteDaRegua = ParteDaReguaSobreAFolha.NENHUMA
        restauradoDeRascunho = false
        aplicarCorDaFolha(PaletaDoEditorDeImagem.corDaFolhaInicial)
        corDoTraco = PaletaDoEditorDeImagem.VERMELHO
        corDoProximoTexto = PaletaDoEditorDeImagem.PRETO
        pontaDoTraco = PontaDoTracoDaComposicao.CANETA
        pincelDoMotor = null
        origemDaSeta = null
        previaDaSeta = null
        desenharComODedo = false
        usarDedoNoLaco = false
        contaGotasArmado = false
        lupaAtiva = false
        pontosJaNaTinta = 0
        camadaSuja = true
    }

    override fun onDraw(canvas: Canvas) {
        if (molduraLargura <= 0 || molduraAltura <= 0) return
        // Rotação só da vista: tudo abaixo desenha numa "tela reta" e o canvas gira em volta do centro.
        val girada = anguloDaVista != 0f
        desenhosEmPe.clear()
        if (girada) {
            canvas.save()
            canvas.rotate(anguloDaVista, width / 2f, height / 2f)
        }
        botoesDeEspelho.clear()
        calcularDestino()
        canvas.drawRect(destino, fundoDaFolha)
        aplicarTrechoAtualNaTinta()
        // Compõe camadas em 1:1 na moldura e só depois escala — evita costura de 1px
        // entre cópia e original (cada uma era amostrada separado no zoom).
        val buffer = garantirBufferDaMolduraEmUmParaUm()
        canvas.drawBitmap(buffer, null, destino, filtroDaTinta)
        if (ferramenta == Ferramenta.RECORTE) {
            desenharRecorte(canvas)
            if (
                DesenhoDaMolduraDeTransformarNoEditorDeImagem.sobreFerramentaDeRecorte(
                    pecaEmModoTransformar = pecaEmModoTransformar,
                    selecaoAtivaDeRecorte = selecaoAtivaDaFerramentaDeRecorte(),
                )
            ) {
                desenharSelecao(canvas)
            }
        } else {
            desenharSelecao(canvas)
        }
        if (girada) canvas.restore()
        // Barra da seleção e "Colar" ficam em pé, na posição real da seleção.
        desenhosEmPe.forEach { it(canvas) }
        desenhosEmPe.clear()
        // A régua fica no vidro da tela (coordenadas reais): não gira com a folha.
        desenharRegua(canvas)
        if (lupaAtiva) desenharLupaDoContaGotas(canvas)
    }

    private fun garantirBufferDaMolduraEmUmParaUm(): Bitmap {
        var buffer = bufferDaMolduraEmUmParaUm
        if (
            buffer == null ||
            buffer.isRecycled ||
            buffer.width != molduraLargura ||
            buffer.height != molduraAltura
        ) {
            buffer?.takeIf { !it.isRecycled }?.recycle()
            buffer = Bitmap.createBitmap(molduraLargura, molduraAltura, Bitmap.Config.ARGB_8888)
            bufferDaMolduraEmUmParaUm = buffer
            camadaSuja = true
        }
        if (camadaSuja) {
            val tela = Canvas(buffer)
            tela.drawColor(corDaFolha)
            objetos.forEach { if (it.visivel) desenharPeca(tela, it, usarCache = true) }
            desenharPreviaDaSeta(tela)
            camadaSuja = false
        }
        return buffer
    }

    private fun descartarBufferDaMolduraEmUmParaUm() {
        bufferDaMolduraEmUmParaUm?.takeIf { !it.isRecycled }?.recycle()
        bufferDaMolduraEmUmParaUm = null
        camadaSuja = true
    }

    override fun invalidate() {
        camadaSuja = true
        super.invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (anguloDaVista == 0f) return processarToque(event)
        // Vista girada: a lógica do editor continua vendo uma tela reta (coordenadas desgiradas).
        val desgirado = MotionEvent.obtain(event)
        matrizInversaDaVista.setRotate(-anguloDaVista, width / 2f, height / 2f)
        desgirado.transform(matrizInversaDaVista)
        return try {
            processarToque(desgirado)
        } finally {
            desgirado.recycle()
        }
    }

    private fun processarToque(event: MotionEvent): Boolean {
        if (molduraLargura <= 0) return false
        parent?.requestDisallowInterceptTouchEvent(true)
        calcularDestino()
        atualizarBotaoLateralDaCanetaSPen(event)
        observarGestosComDedos(event)
        if (modoDoDedo == ModoDoDedo.IGNORAR && event.actionMasked != MotionEvent.ACTION_DOWN &&
            toqueLongoPegouCor
        ) {
            // Toque longo abriu a lupa: o dedo desliza até a cor e soltar pega.
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> moverLupa(event.x, event.y)
                MotionEvent.ACTION_UP -> soltarLupa(pegar = true)
                MotionEvent.ACTION_CANCEL -> soltarLupa(pegar = false)
            }
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                toqueLongoPegouCor = false
                modoDoDedo = ModoDoDedo.NENHUM
            }
            return true
        }
        if (lupaAtiva && event.actionMasked != MotionEvent.ACTION_DOWN) {
            // Conta-gotas armado (botão): mesma lupa, pega ao soltar.
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> moverLupa(event.x, event.y)
                MotionEvent.ACTION_UP -> soltarLupa(pegar = true)
                MotionEvent.ACTION_CANCEL -> soltarLupa(pegar = false)
            }
            return true
        }
        if (event.actionMasked == MotionEvent.ACTION_DOWN && contaGotasArmado) {
            cancelarToquesLongos()
            abrirLupa(event.x, event.y)
            return true
        }
        if (
            event.actionMasked == MotionEvent.ACTION_BUTTON_PRESS ||
            event.actionMasked == MotionEvent.ACTION_BUTTON_RELEASE
        ) {
            // Só o botão da caneta (hover): não inicia traço; estado já atualizado acima.
            return true
        }
        val seguraARegua = ToqueNaReguaDoEditor.temPrioridade(
            comecouNaRegua = event.actionMasked == MotionEvent.ACTION_DOWN && toqueNaRegua(event),
            jaSegurandoARegua = modoDoDedo == ModoDoDedo.REGUA,
        )
        val caneta = indiceDaCaneta(event)
        if (caneta >= 0 && !seguraARegua) {
            val pontaFisicaBorracha = event.getToolType(caneta) == MotionEvent.TOOL_TYPE_ERASER
            val botaoLateral = botaoLateralDaCanetaPressionado ||
                BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.botaoLateralPressionado(
                    event.buttonState,
                )
            val priorizaTraco = BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem
                .canetaPriorizaTracoSobreSelecaoOuTransformar(
                    botaoLateralPressionado = botaoLateral,
                    pontaFisicaEhBorracha = pontaFisicaBorracha,
                )
            if (
                !priorizaTraco && (
                    modoDoDedo == ModoDoDedo.IGNORAR ||
                        ToqueAposMoverComMolduraDeTransformarDaPeca.deveConsumirToqueEnquantoAguardaSoltarAposArmar(
                            aguardandoSoltarAposArmarTransformar,
                        )
                    )
            ) {
                if (
                    event.actionMasked == MotionEvent.ACTION_UP ||
                    event.actionMasked == MotionEvent.ACTION_CANCEL ||
                    (
                        event.actionMasked == MotionEvent.ACTION_POINTER_UP &&
                            event.getPointerId(event.actionIndex) == event.getPointerId(caneta)
                    )
                ) {
                    modoDoDedo = ModoDoDedo.NENHUM
                    aguardandoSoltarAposArmarTransformar = false
                }
                return true
            }
            if (
                !priorizaTraco &&
                ToqueAposMoverComMolduraDeTransformarDaPeca.canetaSegueGestoSemCancelar(
                    pecaEmModoTransformar = pecaEmModoTransformar,
                    selecaoAtivaDeRecorte = selecaoAtivaDaFerramentaDeRecorte(),
                )
            ) {
                // Não cancelar o gesto a cada MOVE — senão a caneta não move a peça.
                return tocarComDedos(event)
            }
            if (priorizaTraco) {
                aguardandoSoltarAposArmarTransformar = false
                modoDaMascaraDoLaco = ModoDaMascaraDoLaco.NENHUM
            }
            cancelarGestoDaPeca()
            // Não zerar SELECAO_LACO: a caneta precisa manter o gesto de mover/girar a máscara.
            if (priorizaTraco || modoDoDedo != ModoDoDedo.SELECAO_LACO) {
                modoDoDedo = ModoDoDedo.NENHUM
            }
            val id = event.getPointerId(caneta)
            if (!priorizaTraco && ferramenta == Ferramenta.RECORTE) {
                return tocarRecorte(event, id)
            }
            return tocarTraco(event, id)
        }
        if (
            ToqueAposMoverComMolduraDeTransformarDaPeca.prendeCanetaEDedoNoGestoDaPeca(
                pecaEmModoTransformar = pecaEmModoTransformar,
                selecaoAtivaDeRecorte = selecaoAtivaDaFerramentaDeRecorte(),
            )
        ) {
            return tocarComDedos(event)
        }
        val setaNoDedo = pontaDoTraco == PontaDoTracoDaComposicao.SETA && ferramenta != Ferramenta.BORRACHA
        if (!seguraARegua && (desenharComODedo || setaNoDedo) && ferramentaDeTraco() &&
            (pecaDoTraco() != null || tracoAtual != null || origemDaSeta != null)
        ) {
            if (event.pointerCount <= 1 && modoDoDedo != ModoDoDedo.REGUA) {
                return tocarTraco(event, event.getPointerId(0))
            }
            if (tracoAtual != null) cancelarTracoAtual()
        }
        return tocarComDedos(event)
    }

    private fun aplicarTamanho(largura: Int, altura: Int) {
        if (largura == molduraLargura && altura == molduraAltura) return
        if (molduraLargura > 0) empilhar(Passo.Moldura(molduraLargura, molduraAltura))
        molduraLargura = largura
        molduraAltura = altura
        descartarBufferDaMolduraEmUmParaUm()
        zoom = 1f
        anguloDaVista = 0f
        panX = 0f
        panY = 0f
        if (ferramenta == Ferramenta.RECORTE && formaDaSelecao == FormaDaSelecaoSobreAImagem.RETANGULO) {
            selecaoInteligenteArmada = false
            limparSelecaoDoRetangulo()
            aoMudarSelecaoAtivaDoLaco?.invoke()
        }
        objetos.filterIsInstance<Peca.Folha>().forEach { garantirTamanhoDaFolhaNaMoldura(it) }
        invalidarTintas()
        camadaSuja = true
        invalidate()
    }

    fun destacarRetangulo(): Boolean {
        if (formaDaSelecao == FormaDaSelecaoSobreAImagem.LACO) return destacarLaco()
        if (ferramenta != Ferramenta.RECORTE) return true
        val imagem = garantirImagemAlvoParaRecorte() ?: return false
        fundirTracosNaImagem(imagem)
        val area = areaDoRecorteSobre(imagem) ?: return true
        if (!DecisaoDoRetanguloSobreAImagemDaComposicao.cabe(area.largura, area.altura)) return true
        if (!PilhaDeCamadasDaComposicao.cabeMaisUma(objetos.size)) return false
        val cobre = DecisaoDoRetanguloSobreAImagemDaComposicao.cobreQuaseTudo(
            area.esquerda,
            area.topo,
            area.largura,
            area.altura,
            imagem.esquerda,
            imagem.topo,
            imagem.largura,
            imagem.altura,
        )
        when (DecisaoDoRetanguloSobreAImagemDaComposicao.efeito(destacar = true, cabe = true, cobreQuaseTudo = cobre)) {
            EfeitoDoRetanguloSobreAImagem.COPIAR_POR_CIMA -> {
                val copia = imagem.bitmap.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
                colocarPecaDestacada(
                    imagem,
                    copia,
                    imagem.esquerda,
                    imagem.topo,
                    imagem.largura,
                    imagem.altura,
                    null,
                )
            }
            EfeitoDoRetanguloSobreAImagem.DESTACAR_COM_BURACO -> {
                val pixels = pixelsDoRecorte(imagem) ?: return true
                val extraido = copiaSolta(imagem.bitmap, pixels.esquerda, pixels.topo, pixels.largura, pixels.altura)
                val anterior = imagem.bitmap
                imagem.bitmap = bitmapComBuraco(anterior, pixels.esquerda, pixels.topo, pixels.largura, pixels.altura)
                colocarPecaDestacada(
                    imagem,
                    extraido,
                    area.esquerda,
                    area.topo,
                    area.largura,
                    area.altura,
                    anterior,
                )
            }
            else -> return true
        }
        return true
    }

    private fun aplicarRecorte() {
        if (formaDaSelecao == FormaDaSelecaoSobreAImagem.LACO) {
            aplicarRecorteDoLaco()
            return
        }
        val imagem = garantirImagemAlvoParaRecorte() ?: return
        fundirTracosNaImagem(imagem)
        val area = areaDoRecorteSobre(imagem) ?: return
        if (!DecisaoDoRetanguloSobreAImagemDaComposicao.cabe(area.largura, area.altura)) return
        val cobre = DecisaoDoRetanguloSobreAImagemDaComposicao.cobreQuaseTudo(
            area.esquerda,
            area.topo,
            area.largura,
            area.altura,
            imagem.esquerda,
            imagem.topo,
            imagem.largura,
            imagem.altura,
        )
        if (DecisaoDoRetanguloSobreAImagemDaComposicao.efeito(destacar = false, cabe = true, cobreQuaseTudo = cobre) !=
            EfeitoDoRetanguloSobreAImagem.RECORTAR
        ) {
            sairDoRecorte()
            return
        }
        val pixels = pixelsDoRecorte(imagem) ?: return sairDoRecorte()
        val esquerda = pixels.esquerda
        val topo = pixels.topo
        val largura = pixels.largura
        val altura = pixels.altura
        val cortada = Bitmap.createBitmap(imagem.bitmap, esquerda, topo, largura, altura)
        val nova = if (cortada == imagem.bitmap) imagem.bitmap.copy(Bitmap.Config.ARGB_8888, true).comTransparencia() else cortada
        val riscosAntes = copiarTracos()
        val estadoAntes = capturar(imagem)
        val bitmapAntes = imagem.bitmap
        val molduraAntesLargura = molduraLargura
        val molduraAntesAltura = molduraAltura
        imagem.bitmap = nova
        val soEssaImagem = objetos.size == 1
        if (soEssaImagem) {
            val deslocamentoX = -area.esquerda
            val deslocamentoY = -area.topo
            imagem.esquerda = 0f
            imagem.topo = 0f
            imagem.largura = area.largura
            imagem.altura = area.altura
            molduraLargura = area.largura.toInt().coerceAtLeast(1)
            molduraAltura = area.altura.toInt().coerceAtLeast(1)
            remapearTracosNoQuadro(molduraAntesLargura.toFloat(), molduraAntesAltura.toFloat(), deslocamentoX, deslocamentoY)
            zoom = 1f
            anguloDaVista = 0f
            panX = 0f
            panY = 0f
        } else {
            imagem.esquerda = area.esquerda
            imagem.topo = area.topo
            imagem.largura = area.largura
            imagem.altura = area.altura
        }
        camadaSuja = true
        empilhar(Passo.Corte(imagem.id, bitmapAntes, estadoAntes, molduraAntesLargura, molduraAntesAltura, riscosAntes))
        sairDoRecorte()
    }

    private fun areaDoRecorteSobre(imagem: Peca.Imagem) = DecisaoDoRetanguloSobreAImagemDaComposicao.intersecao(
        recorte.left,
        recorte.top,
        recorte.width(),
        recorte.height(),
        imagem.esquerda,
        imagem.topo,
        imagem.largura,
        imagem.altura,
    )

    private fun pixelsDoRecorte(imagem: Peca.Imagem) = DecisaoDoRetanguloSobreAImagemDaComposicao.pixels(
        recorte.left,
        recorte.top,
        recorte.width(),
        recorte.height(),
        imagem.esquerda,
        imagem.topo,
        imagem.largura,
        imagem.altura,
        imagem.bitmap.width,
        imagem.bitmap.height,
    )

    private fun copiaSolta(origem: Bitmap, esquerda: Int, topo: Int, largura: Int, altura: Int): Bitmap {
        val recorteDoBitmap = Bitmap.createBitmap(origem, esquerda, topo, largura, altura)
        val solta = recorteDoBitmap.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        if (solta !== recorteDoBitmap && recorteDoBitmap !== origem && !recorteDoBitmap.isRecycled) {
            recorteDoBitmap.recycle()
        }
        return solta
    }

    private fun bitmapComBuraco(origem: Bitmap, esquerda: Int, topo: Int, largura: Int, altura: Int): Bitmap {
        val mutavel = origem.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        val tela = Canvas(mutavel)
        val limpar = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
        tela.drawRect(
            esquerda.toFloat(),
            topo.toFloat(),
            (esquerda + largura).toFloat(),
            (topo + altura).toFloat(),
            limpar,
        )
        return mutavel
    }

    private fun colocarPecaDestacada(
        origem: Peca.Imagem,
        bitmap: Bitmap,
        esquerda: Float,
        topo: Float,
        largura: Float,
        altura: Float,
        bitmapAnterior: Bitmap?,
    ) {
        val peca = Peca.Imagem(novoId(), bitmap)
        peca.esquerda = esquerda
        peca.topo = topo
        peca.largura = largura
        peca.altura = altura
        objetos.add(objetos.indexOf(origem).coerceAtLeast(0) + 1, peca)
        idDaCamadaAtiva = peca.id
        selecionadoId = peca.id
        pecaEmModoTransformar = true
        aguardandoSoltarAposArmarTransformar = true
        fundirPecaAoEncerrarTransformar = false
        idOrigemDoTransformar = -1
        empilhar(Passo.Destaque(origem.id, bitmapAnterior, peca.id))
        aoMudarSelecao?.invoke()
        camadaSuja = true
        encerrarRecorteMantendoTransformacao()
    }

    /**
     * Copiar: prévia transformável até Colar (fundir no destino) ou cancelar.
     * Não entra no histórico até o Colar.
     */
    private fun armarModoColarComPrevia(
        origem: Peca.Imagem,
        bitmap: Bitmap,
        esquerda: Float,
        topo: Float,
        largura: Float,
        altura: Float,
    ): Boolean {
        if (!PilhaDeCamadasDaComposicao.cabeMaisUma(objetos.size)) return false
        if (modoColarDaSelecao) cancelarModoColarDaSelecao()
        val peca = Peca.Imagem(novoId(), bitmap)
        peca.esquerda = esquerda
        peca.topo = topo
        peca.largura = largura
        peca.altura = altura
        peca.angulo = origem.angulo
        // Prévia no topo: fica visível e arrastável mesmo com outra camada por cima da origem.
        objetos.add(peca)
        idPecaPreviaDoColar = peca.id
        modoColarDaSelecao = true
        idDaCamadaAtiva = origem.id
        selecionadoId = peca.id
        pecaEmModoTransformar = true
        aguardandoSoltarAposArmarTransformar = true
        fundirPecaAoEncerrarTransformar = false
        idOrigemDoTransformar = -1
        aoMudarSelecao?.invoke()
        camadaSuja = true
        encerrarRecorteMantendoTransformacao()
        return true
    }

    private fun confirmarColarNaCamadaAtiva(): Boolean {
        if (!modoColarDaSelecao) return false
        val idPrevia = idPecaPreviaDoColar
        if (objetos.none { it.id == idPrevia }) return false
        var destino = objetos.find { it.id == idDaCamadaAtiva }
        if (
            !ModoColarDaSelecaoCopiadaNaComposicao.destinoAceitaFundir(
                idDestino = idDaCamadaAtiva,
                idPrevia = idPrevia,
                destinoEhImagem = destino is Peca.Imagem,
                destinoEhFolha = destino is Peca.Folha,
            )
        ) {
            return false
        }
        if (destino is Peca.Folha) {
            destino = promoverFolhaParaImagem(destino) ?: return false
            idDaCamadaAtiva = destino.id
        }
        if (destino !is Peca.Imagem) return false
        fundirPecaTransformadaNaOrigem(idPrevia, destino.id)
        modoColarDaSelecao = false
        idPecaPreviaDoColar = -1
        selecionadoId = -1
        pecaEmModoTransformar = false
        aguardandoSoltarAposArmarTransformar = false
        fundirPecaAoEncerrarTransformar = false
        idOrigemDoTransformar = -1
        aoMudarSelecao?.invoke()
        invalidate()
        return true
    }

    /** @return true se o toque foi consumido pelo modo Colar. */
    private fun prepararDedoNoModoColar(x: Float, y: Float): Boolean {
        val toqueNoChip = chipNoToque(x, y)?.acao == AcaoDaSelecaoAtivaDoLaco.COLAR
        val toqueNoCabo = toqueNoChrome(x, y) == ToqueNoChrome.GIRO
        val noPrevia = toqueNaPecaSelecionadaOuChrome(x, y)
        val ponto = paraAComposicao(x, y)
        when (
            ToqueNoModoColarDaSelecaoCopiadaNaComposicao.destino(
                modoColarAtivo = true,
                toqueNoChipColar = toqueNoChip,
                toqueNaPreviaOuChrome = noPrevia,
                toqueNoCaboDeGiro = toqueNoCabo,
            )
        ) {
            DestinoDoToqueNoModoColarDaSelecaoCopiada.CONFIRMAR_COLAR -> {
                modoDoDedo = ModoDoDedo.IGNORAR
                confirmarColarNaCamadaAtiva()
                return true
            }
            DestinoDoToqueNoModoColarDaSelecaoCopiada.CANCELAR -> {
                cancelarModoColarDaSelecao()
                modoDoDedo = ModoDoDedo.IGNORAR
                return true
            }
            DestinoDoToqueNoModoColarDaSelecaoCopiada.GESTO_DA_PREVIA -> {
                if (consumirToqueDeEspelho(x, y)) return true
                when (toqueNoChrome(x, y)) {
                    ToqueNoChrome.GIRO -> {
                        val local = ponto ?: floatArrayOf(0f, 0f)
                        prepararPeca(ModoDaPeca.GIRAR, null, local, selecionadoId)
                        return true
                    }
                    ToqueNoChrome.ESPELHO_HORIZONTAL, ToqueNoChrome.ESPELHO_VERTICAL -> Unit
            ToqueNoChrome.NENHUM -> Unit
                }
                val local = ponto ?: return true
                val canto = cantoDaSelecao(x, y)
                if (canto != null) {
                    prepararPeca(ModoDaPeca.REDIMENSIONAR, canto, local, selecionadoId)
                } else {
                    prepararPeca(ModoDaPeca.AGUARDANDO, null, local, selecionadoId)
                }
                return true
            }
            DestinoDoToqueNoModoColarDaSelecaoCopiada.IGNORAR -> return false
        }
    }

    private fun cancelarModoColarDaSelecao() {
        if (!modoColarDaSelecao) return
        val idPrevia = idPecaPreviaDoColar
        val peca = objetos.find { it.id == idPrevia }
        if (peca != null) {
            objetos.remove(peca)
            reciclarTinta(peca)
            val imagem = peca as? Peca.Imagem
            if (imagem != null && !imagem.bitmap.isRecycled && !bitmapEmUso(imagem.bitmap)) {
                imagem.bitmap.recycle()
            }
        }
        modoColarDaSelecao = false
        idPecaPreviaDoColar = -1
        if (selecionadoId == idPrevia) selecionadoId = -1
        pecaEmModoTransformar = false
        aguardandoSoltarAposArmarTransformar = false
        fundirPecaAoEncerrarTransformar = false
        idOrigemDoTransformar = -1
        camadaSuja = true
        aoMudarSelecao?.invoke()
        invalidate()
    }

    private fun marcarFundirAoEncerrarAposMover(idOrigem: Int) {
        fundirPecaAoEncerrarTransformar =
            DecisaoDoMoverDaSelecaoAtivaDaComposicao.fundirAoEncerrarTransformar(moverCriandoNovaCamada)
        idOrigemDoTransformar = if (fundirPecaAoEncerrarTransformar) idOrigem else -1
    }

    /** Funde a peça do Mover na origem (preferência “sem nova camada”) e guarda undo. */
    private fun fundirPecaTransformadaNaOrigem(idPeca: Int, idOrigem: Int) {
        val peca = objetos.find { it.id == idPeca } as? Peca.Imagem ?: return
        val origem = objetos.find { it.id == idOrigem } as? Peca.Imagem ?: return
        val bitmapAntes = origem.bitmap
        val estadoAntes = capturar(origem)
        // Pedaço colado/movido para fora da área da camada: ela cresce (antes a parte de fora sumia).
        val cresceu = crescerImagemParaCaber(origem, cantosDaPeca(peca))
        val base = if (cresceu) origem.bitmap else bitmapAntes.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        val tela = Canvas(base)
        val escalaX = base.width / origem.largura.coerceAtLeast(1f)
        val escalaY = base.height / origem.altura.coerceAtLeast(1f)
        tela.save()
        tela.scale(escalaX, escalaY)
        tela.translate(-origem.esquerda, -origem.topo)
        val centroX = peca.esquerda + peca.largura / 2f
        val centroY = peca.topo + peca.altura / 2f
        tela.rotate(peca.angulo, centroX, centroY)
        if (!peca.bitmap.isRecycled) {
            tela.drawBitmap(
                peca.bitmap,
                null,
                RectF(peca.esquerda, peca.topo, peca.esquerda + peca.largura, peca.topo + peca.altura),
                tintaDaPecaSemFiltro,
            )
        }
        tela.restore()
        origem.bitmap = base
        objetos.remove(peca)
        pecasFora[peca.id] = peca
        if (idDaCamadaAtiva == idPeca) idDaCamadaAtiva = idOrigem
        empilhar(Passo.FundirPecaTransformada(idPeca, idOrigem, bitmapAntes, if (cresceu) estadoAntes else null))
        camadaSuja = true
    }

    /** Sai da seleção de recorte sem ir para a ferramenta de pintura. */
    private fun encerrarRecorteMantendoTransformacao() {
        idDaImagemDoRecorte = -1
        selecaoInteligenteArmada = false
        selecaoDoRetanguloInvertida = false
        limparLaco()
        limparSelecaoDoRetangulo()
        arraste = null
        aoMudarSelecaoAtivaDoLaco?.invoke()
        invalidate()
    }

    private fun sairDoRecorte() {
        ferramenta = Ferramenta.VERMELHO
        idDaImagemDoRecorte = -1
        formaDaSelecao = FormaDaSelecaoSobreAImagem.RETANGULO
        recortarDentroDaMoldura = true
        selecaoInteligenteArmada = false
        selecaoDoRetanguloInvertida = false
        limparLaco()
        chipsDaSelecaoAtiva.clear()
        arraste = null
        aoMudarFerramenta?.invoke(Ferramenta.VERMELHO)
        invalidate()
    }

    private fun garantirCamadaSelecionada() {
        val ids = objetos.map { it.id }
        idDaCamadaAtiva = SelecaoObrigatoriaDaCamada.manter(idDaCamadaAtiva, ids)
        selecionadoId = SelecaoObrigatoriaDaCamada.manter(selecionadoId, ids)
    }

    private fun limparLaco() {
        pontosDoLaco.clear()
        iniciosDosTracosDoLaco.clear()
        pontosDoLacoNaOrigem = emptyList()
        lacoFechado = false
        selecaoDoLacoInvertida = false
        modoDaMascaraDoLaco = ModoDaMascaraDoLaco.NENHUM
        chipsDaSelecaoAtiva.clear()
    }

    private fun registrarOrigemDoLacoSePreciso() {
        if (!lacoFechado) return
        if (pontosDoLacoNaOrigem.isEmpty()) {
            pontosDoLacoNaOrigem = pontosDoLaco.map { PontoDoLacoLivre(it.x, it.y) }
        }
    }

    private fun desfazerUltimoTracoDoLaco(): Boolean {
        if (iniciosDosTracosDoLaco.isEmpty()) return false
        val (contorno, inicios) = LacoLivreDaSelecaoSobreAImagemDaComposicao.desfazerUltimoTraco(
            pontosDoLaco.toList(),
            iniciosDosTracosDoLaco.toList(),
        )
        pontosDoLaco.clear()
        pontosDoLaco.addAll(contorno)
        iniciosDosTracosDoLaco.clear()
        iniciosDosTracosDoLaco.addAll(inicios)
        lacoFechado = false
        invalidate()
        return true
    }

    private fun lacoPronto(): Pair<List<PontoDoLacoLivre>, CaixaDoLacoLivre>? {
        if (!lacoFechado) return null
        val pontos = pontosDoLaco.toList()
        val caixa = LacoLivreDaSelecaoSobreAImagemDaComposicao.caixa(pontos) ?: return null
        return pontos to caixa
    }

    private fun aplicarRecorteDoLaco() {
        val imagem = garantirImagemAlvoParaRecorte() ?: return
        fundirTracosNaImagem(imagem)
        val (pontos, caixa) = lacoPronto() ?: return
        val area = areaDaCaixaSobre(imagem, caixa) ?: return
        val pixels = pixelsDaCaixa(imagem, caixa) ?: return
        val mascarada = bitmapMascaradoPeloLaco(
            imagem.bitmap,
            pontos,
            imagem.esquerda,
            imagem.topo,
            imagem.largura,
            imagem.altura,
            pixels,
        )
        val riscosAntes = copiarTracos()
        val estadoAntes = capturar(imagem)
        val bitmapAntes = imagem.bitmap
        val molduraAntesLargura = molduraLargura
        val molduraAntesAltura = molduraAltura
        imagem.bitmap = mascarada
        val soEssaImagem = objetos.size == 1
        if (soEssaImagem) {
            imagem.esquerda = 0f
            imagem.topo = 0f
            imagem.largura = area.largura
            imagem.altura = area.altura
            molduraLargura = area.largura.toInt().coerceAtLeast(1)
            molduraAltura = area.altura.toInt().coerceAtLeast(1)
            remapearTracosNoQuadro(
                molduraAntesLargura.toFloat(),
                molduraAntesAltura.toFloat(),
                -area.esquerda,
                -area.topo,
            )
            zoom = 1f
            anguloDaVista = 0f
            panX = 0f
            panY = 0f
        } else {
            imagem.esquerda = area.esquerda
            imagem.topo = area.topo
            imagem.largura = area.largura
            imagem.altura = area.altura
        }
        camadaSuja = true
        empilhar(Passo.Corte(imagem.id, bitmapAntes, estadoAntes, molduraAntesLargura, molduraAntesAltura, riscosAntes))
        sairDoRecorte()
    }

    private fun destacarLaco(): Boolean {
        if (ferramenta != Ferramenta.RECORTE) return true
        val imagem = garantirImagemAlvoParaRecorte() ?: return false
        fundirTracosNaImagem(imagem)
        registrarOrigemDoLacoSePreciso()
        val pontosOrigem = pontosDoLacoNaOrigem.ifEmpty { pontosDoLaco.toList() }
        val caixaOrigem = LacoLivreDaSelecaoSobreAImagemDaComposicao.caixa(pontosOrigem) ?: return true
        val caixaAtual = LacoLivreDaSelecaoSobreAImagemDaComposicao.caixa(pontosDoLaco.toList()) ?: return true
        val montagem = montarBitmapDaSelecaoDoLaco(imagem, pontosOrigem, caixaOrigem) ?: return true
        val anterior = imagem.bitmap
        imagem.bitmap = if (selecaoDoLacoInvertida) {
            bitmapComBuracoForaDoLaco(anterior, pontosOrigem, imagem)
        } else {
            bitmapComBuracoDoLaco(anterior, pontosOrigem, imagem)
        }
        if (!PilhaDeCamadasDaComposicao.cabeMaisUma(objetos.size)) {
            imagem.bitmap = anterior
            return false
        }
        colocarPecaDestacada(
            imagem,
            montagem.bitmap,
            caixaAtual.esquerda,
            caixaAtual.topo,
            caixaAtual.largura,
            caixaAtual.altura,
            anterior,
        )
        objetos.find { it.id == idDaCamadaAtiva }?.angulo = imagem.angulo
        marcarFundirAoEncerrarAposMover(imagem.id)
        return true
    }

    private fun colarMontagemNaMesmaImagem(
        imagem: Peca.Imagem,
        montagem: MontagemDaSelecaoDoLaco,
        destinoEsquerda: Float,
        destinoTopo: Float,
        destinoLargura: Float,
        destinoAltura: Float,
    ) {
        val base = if (imagem.bitmap.isMutable) {
            imagem.bitmap
        } else {
            imagem.bitmap.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        }
        val escalaX = base.width / imagem.largura.coerceAtLeast(1f)
        val escalaY = base.height / imagem.altura.coerceAtLeast(1f)
        val alinhado = AlinhamentoDoRetanguloAGradeDePixelsDaImagem.daSelecao(
            destinoEsquerda,
            destinoTopo,
            destinoLargura,
            destinoAltura,
            imagem.esquerda,
            imagem.topo,
            imagem.largura,
            imagem.altura,
            base.width,
            base.height,
        )
        val esq = alinhado?.esquerda ?: destinoEsquerda
        val top = alinhado?.topo ?: destinoTopo
        // Destino em pixels inteiros 1:1 com o extrato — sem subpixel nem stretch.
        val left = ((esq - imagem.esquerda) * escalaX).toInt()
        val topPx = ((top - imagem.topo) * escalaY).toInt()
        Canvas(base).drawBitmap(
            montagem.bitmap,
            null,
            android.graphics.Rect(left, topPx, left + montagem.bitmap.width, topPx + montagem.bitmap.height),
            tintaDaPecaSemFiltro,
        )
        imagem.bitmap = base
    }

    private fun copiarSelecaoAtivaDoLaco(): Boolean {
        if (ferramenta != Ferramenta.RECORTE) return true
        val imagem = garantirImagemAlvoParaRecorte() ?: return false
        fundirTracosNaImagem(imagem)
        val (pontos, caixa) = lacoPronto() ?: return true
        val montagem = montarBitmapDaSelecaoDoLaco(imagem, pontos, caixa) ?: return true
        return armarModoColarComPrevia(
            origem = imagem,
            bitmap = montagem.bitmap,
            esquerda = montagem.esquerda,
            topo = montagem.topo,
            largura = montagem.largura,
            altura = montagem.altura,
        )
    }

    private class MontagemDaSelecaoDoLaco(
        val bitmap: Bitmap,
        val esquerda: Float,
        val topo: Float,
        val largura: Float,
        val altura: Float,
    )

    /** Bitmap na caixa do laço (moldura), conteúdo alinhado ao mesmo lugar da seleção. */
    private fun montarBitmapDaSelecaoDoLaco(
        imagem: Peca.Imagem,
        pontos: List<PontoDoLacoLivre>,
        caixa: CaixaDoLacoLivre,
    ): MontagemDaSelecaoDoLaco? {
        val caixaAlvo = caixaDaSelecaoAtivaParaExtrair(imagem, caixa)
        val area = areaDaCaixaSobre(imagem, caixaAlvo) ?: return null
        val pixels = pixelsDaCaixa(imagem, caixaAlvo) ?: return null
        val extraido = bitmapMascaradoPeloLaco(
            imagem.bitmap,
            pontos,
            imagem.esquerda,
            imagem.topo,
            imagem.largura,
            imagem.altura,
            pixels,
        )
        val escalaX = imagem.bitmap.width / imagem.largura.coerceAtLeast(1f)
        val escalaY = imagem.bitmap.height / imagem.altura.coerceAtLeast(1f)
        val alinhadaEsquerda = imagem.esquerda + pixels.esquerda.toFloat() * imagem.largura / imagem.bitmap.width
        val alinhadaTopo = imagem.topo + pixels.topo.toFloat() * imagem.altura / imagem.bitmap.height
        val alinhadaLargura = pixels.largura.toFloat() * imagem.largura / imagem.bitmap.width
        val alinhadaAltura = pixels.altura.toFloat() * imagem.altura / imagem.bitmap.height
        val mesmaCaixa =
            abs(alinhadaEsquerda - caixaAlvo.esquerda) < 0.5f &&
                abs(alinhadaTopo - caixaAlvo.topo) < 0.5f &&
                abs(alinhadaLargura - caixaAlvo.largura) < 0.5f &&
                abs(alinhadaAltura - caixaAlvo.altura) < 0.5f
        if (mesmaCaixa) {
            return MontagemDaSelecaoDoLaco(
                extraido,
                alinhadaEsquerda,
                alinhadaTopo,
                alinhadaLargura,
                alinhadaAltura,
            )
        }
        val outW = (caixaAlvo.largura * escalaX).toInt().coerceAtLeast(1)
        val outH = (caixaAlvo.altura * escalaY).toInt().coerceAtLeast(1)
        val saida = try {
            Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        } catch (_: OutOfMemoryError) {
            return MontagemDaSelecaoDoLaco(
                extraido,
                alinhadaEsquerda,
                alinhadaTopo,
                alinhadaLargura,
                alinhadaAltura,
            )
        }
        saida.eraseColor(Color.TRANSPARENT)
        val dx = (alinhadaEsquerda - caixaAlvo.esquerda) * escalaX
        val dy = (alinhadaTopo - caixaAlvo.topo) * escalaY
        val dest = RectF(dx, dy, dx + alinhadaLargura * escalaX, dy + alinhadaAltura * escalaY)
        Canvas(saida).drawBitmap(extraido, null, dest, tintaDaPecaSemFiltro)
        if (extraido !== saida && !extraido.isRecycled) extraido.recycle()
        return MontagemDaSelecaoDoLaco(
            saida,
            caixaAlvo.esquerda,
            caixaAlvo.topo,
            caixaAlvo.largura,
            caixaAlvo.altura,
        )
    }

    private fun excluirSelecaoAtivaDoLaco(): Boolean {
        if (ferramenta != Ferramenta.RECORTE || !lacoFechado) return true
        val imagem = garantirImagemAlvoParaRecorte() ?: return false
        fundirTracosNaImagem(imagem)
        val (pontos, _) = lacoPronto() ?: return true
        val anterior = imagem.bitmap
        imagem.bitmap = if (selecaoDoLacoInvertida) {
            bitmapComBuracoForaDoLaco(anterior, pontos, imagem)
        } else {
            bitmapComBuracoDoLaco(anterior, pontos, imagem)
        }
        empilhar(Passo.AlteraBitmap(imagem.id, anterior))
        limparLaco()
        aoMudarSelecao?.invoke()
        aoMudarSelecaoAtivaDoLaco?.invoke()
        camadaSuja = true
        invalidate()
        return true
    }

    private fun bitmapComBuracoForaDoLaco(
        origem: Bitmap,
        pontos: List<PontoDoLacoLivre>,
        imagem: Peca.Imagem,
    ): Bitmap {
        val mutavel = origem.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        val tela = Canvas(mutavel)
        val limpar = tintaDoLaco(PorterDuff.Mode.CLEAR)
        val escalaX = mutavel.width / imagem.largura.coerceAtLeast(1f)
        val escalaY = mutavel.height / imagem.altura.coerceAtLeast(1f)
        desenharForaDaAreaDoLaco(
            tela,
            pontos,
            imagem.esquerda,
            imagem.topo,
            escalaX,
            escalaY,
            0,
            0,
            mutavel.width,
            mutavel.height,
            limpar,
        )
        return mutavel
    }

    private fun inverterSelecaoAtivaDoLaco() {
        if (!lacoFechado) return
        selecaoDoLacoInvertida = InverterSelecaoAtivaDoLaco.aposInverter(selecaoDoLacoInvertida)
        invalidate()
    }

    private fun reduzirMolduraASelecaoDoLaco(): Boolean {
        val (pontos, caixa) = lacoPronto() ?: return false
        val composto = compor() ?: return false
        val area = AreaDoRecorteSobreAImagemDaComposicao(
            caixa.esquerda,
            caixa.topo,
            caixa.largura,
            caixa.altura,
        )
        if (!DecisaoDoRetanguloSobreAImagemDaComposicao.cabe(area.largura, area.altura)) {
            composto.recycle()
            return false
        }
        val pixels = DecisaoDoRetanguloSobreAImagemDaComposicao.pixels(
            caixa.esquerda,
            caixa.topo,
            caixa.largura,
            caixa.altura,
            0f,
            0f,
            molduraLargura.toFloat(),
            molduraAltura.toFloat(),
            composto.width,
            composto.height,
        )
        if (pixels == null) {
            composto.recycle()
            return false
        }
        val extraido = bitmapMascaradoPeloLaco(
            composto,
            pontos,
            0f,
            0f,
            molduraLargura.toFloat(),
            molduraAltura.toFloat(),
            pixels,
        )
        if (extraido !== composto && !composto.isRecycled) composto.recycle()
        val molduraAntesLargura = molduraLargura
        val molduraAntesAltura = molduraAltura
        empilhar(Passo.Moldura(molduraAntesLargura, molduraAntesAltura))
        objetos.toList().forEach { peca ->
            objetos.remove(peca)
            pecasFora[peca.id] = peca
            reciclarTinta(peca)
        }
        val peca = Peca.Imagem(novoId(), extraido)
        peca.esquerda = 0f
        peca.topo = 0f
        peca.largura = area.largura
        peca.altura = area.altura
        objetos += peca
        idDaCamadaAtiva = peca.id
        selecionadoId = -1
        molduraLargura = area.largura.toInt().coerceAtLeast(1)
        molduraAltura = area.altura.toInt().coerceAtLeast(1)
        zoom = 1f
        anguloDaVista = 0f
        panX = 0f
        panY = 0f
        limparLaco()
        ferramenta = Ferramenta.VERMELHO
        idDaImagemDoRecorte = -1
        aoMudarFerramenta?.invoke(Ferramenta.VERMELHO)
        aoMudarSelecao?.invoke()
        camadaSuja = true
        invalidate()
        return true
    }

    private fun prepararGestoDaSelecaoAtivaDoLaco(xTela: Float, yTela: Float) {
        val chip = chipNoToque(xTela, yTela)
        if (chip != null) {
            // Consome o resto do toque (MOVE/UP) para o laço não nascer no chip.
            modoDoDedo = ModoDoDedo.IGNORAR
            executarAcaoDaSelecaoAtiva(chip.acao)
            return
        }
        val ponto = paraAComposicao(xTela, yTela) ?: run {
            cancelarSelecaoAtivaSemIniciarNova()
            return
        }
        val caixaLaco = LacoLivreDaSelecaoSobreAImagemDaComposicao.caixa(pontosDoLaco.toList())
        if (caixaLaco == null) {
            cancelarSelecaoAtivaSemIniciarNova()
            return
        }
        val caixaGesto = if (selecaoDoLacoInvertida) {
            CaixaDoLacoLivre(0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat())
        } else {
            caixaLaco
        }
        val bbox = rectNaTela(caixaGesto.esquerda, caixaGesto.topo, caixaGesto.largura, caixaGesto.altura)
        val densidade = resources.displayMetrics.density
        val caboY = bbox.top - 36f * densidade
        val caboX = bbox.centerX()
        val raioCabo = 18f * densidade
        if (hypot(xTela - caboX, yTela - caboY) <= raioCabo) {
            modoDaMascaraDoLaco = ModoDaMascaraDoLaco.GIRAR
            anguloInicialDaMascara = GiroDaPecaDaComposicao.angulo(
                caixaLaco.esquerda + caixaLaco.largura / 2f,
                caixaLaco.topo + caixaLaco.altura / 2f,
                ponto[0],
                ponto[1],
            )
            origemDaMascaraX = caixaLaco.esquerda + caixaLaco.largura / 2f
            origemDaMascaraY = caixaLaco.topo + caixaLaco.altura / 2f
            modoDoDedo = ModoDoDedo.SELECAO_LACO
            return
        }
        val dentro = ponto[0] in caixaGesto.esquerda..(caixaGesto.esquerda + caixaGesto.largura) &&
            ponto[1] in caixaGesto.topo..(caixaGesto.topo + caixaGesto.altura)
        if (dentro) {
            modoDaMascaraDoLaco = ModoDaMascaraDoLaco.MOVER
            origemDaMascaraX = ponto[0]
            origemDaMascaraY = ponto[1]
            modoDoDedo = ModoDoDedo.SELECAO_LACO
            return
        }
        if (
            ToqueForaDaSelecaoAtivaDoRecorteDaComposicao.soCancelaSemIniciarNova(
                selecaoAtiva = true,
                toqueNoInteriorOuChromeDaSelecao = false,
            )
        ) {
            cancelarSelecaoAtivaSemIniciarNova()
        }
    }

    /** Cancela laço/retângulo ativos e consome o gesto — não inicia nova seleção neste toque. */
    private fun cancelarSelecaoAtivaSemIniciarNova() {
        if (selecaoAtivaDoRetangulo()) {
            limparSelecaoDoRetangulo()
            aoMudarSelecaoAtivaDoLaco?.invoke()
            invalidate()
        } else if (selecaoAtivaDoLaco()) {
            cancelarSelecaoAtivaDoLaco()
        }
        modoDoDedo = ModoDoDedo.IGNORAR
        arraste = null
    }

    private fun atualizarGestoDaSelecaoAtivaDoLaco(event: MotionEvent) {
        if (modoDoDedo != ModoDoDedo.SELECAO_LACO) return
        val ponto = paraAComposicao(event.x, event.y) ?: return
        when (modoDaMascaraDoLaco) {
            ModoDaMascaraDoLaco.MOVER -> {
                val dx = ponto[0] - origemDaMascaraX
                val dy = ponto[1] - origemDaMascaraY
                if (dx == 0f && dy == 0f) return
                val movidos = pontosDoLaco.map { PontoDoLacoLivre(it.x + dx, it.y + dy) }
                pontosDoLaco.clear()
                pontosDoLaco.addAll(movidos)
                origemDaMascaraX = ponto[0]
                origemDaMascaraY = ponto[1]
                invalidate()
            }
            ModoDaMascaraDoLaco.GIRAR -> {
                val angulo = GiroDaPecaDaComposicao.angulo(
                    origemDaMascaraX,
                    origemDaMascaraY,
                    ponto[0],
                    ponto[1],
                )
                val delta = angulo - anguloInicialDaMascara
                if (abs(delta) < 0.05f) return
                val rad = Math.toRadians(delta.toDouble())
                val cos = kotlin.math.cos(rad).toFloat()
                val sin = kotlin.math.sin(rad).toFloat()
                val girados = pontosDoLaco.map { pontoLaco ->
                    val dx = pontoLaco.x - origemDaMascaraX
                    val dy = pontoLaco.y - origemDaMascaraY
                    PontoDoLacoLivre(
                        origemDaMascaraX + dx * cos - dy * sin,
                        origemDaMascaraY + dx * sin + dy * cos,
                    )
                }
                pontosDoLaco.clear()
                pontosDoLaco.addAll(girados)
                anguloInicialDaMascara = angulo
                invalidate()
            }
            ModoDaMascaraDoLaco.NENHUM -> Unit
        }
    }

    private fun executarAcaoDaSelecaoAtiva(acao: AcaoDaSelecaoAtivaDoLaco) {
        when (EfeitoDaAcaoDaSelecaoAtivaDoLaco.da(acao)) {
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.CONFIRMAR_COLAR_FUNDIR -> {
                confirmarColarNaCamadaAtiva()
                return
            }
            else -> Unit
        }
        if (selecaoAtivaDoRetangulo()) {
            when (EfeitoDaAcaoDaSelecaoAtivaDoLaco.da(acao)) {
                EfeitoDaAcaoDaSelecaoAtivaDoLaco.COPIAR_SEM_BURACO -> copiarSelecaoAtivaDoRetangulo()
                EfeitoDaAcaoDaSelecaoAtivaDoLaco.DESTACAR_COM_BURACO -> moverSelecaoAtivaDoRetangulo()
                EfeitoDaAcaoDaSelecaoAtivaDoLaco.EXCLUIR_COM_BURACO -> excluirSelecaoAtivaDoRetangulo()
                EfeitoDaAcaoDaSelecaoAtivaDoLaco.INVERTER_MASCARA -> inverterSelecaoAtivaDoRetangulo()
                EfeitoDaAcaoDaSelecaoAtivaDoLaco.REDUZIR_MOLDURA_A_SELECAO ->
                    aoPedirConfirmacaoDeRecorteDaSelecaoAtiva?.invoke()
                EfeitoDaAcaoDaSelecaoAtivaDoLaco.CONFIRMAR_COLAR_FUNDIR -> Unit
            }
            return
        }
        when (EfeitoDaAcaoDaSelecaoAtivaDoLaco.da(acao)) {
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.COPIAR_SEM_BURACO -> copiarSelecaoAtivaDoLaco()
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.DESTACAR_COM_BURACO -> destacarLaco()
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.EXCLUIR_COM_BURACO -> excluirSelecaoAtivaDoLaco()
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.INVERTER_MASCARA -> inverterSelecaoAtivaDoLaco()
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.REDUZIR_MOLDURA_A_SELECAO ->
                aoPedirConfirmacaoDeRecorteDaSelecaoAtiva?.invoke()
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.CONFIRMAR_COLAR_FUNDIR -> Unit
        }
    }

    private fun inverterSelecaoAtivaDoRetangulo() {
        if (!selecaoAtivaDoRetangulo()) return
        selecaoDoRetanguloInvertida = InverterSelecaoAtivaDoLaco.aposInverter(selecaoDoRetanguloInvertida)
        invalidate()
    }

    private fun montarBitmapDaSelecaoDoRetangulo(imagem: Peca.Imagem): MontagemDaSelecaoDoLaco? {
        val pixels = pixelsDoRecorte(imagem) ?: return null
        if (selecaoDoRetanguloInvertida) {
            val saida = imagem.bitmap.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
            val tela = Canvas(saida)
            val limpar = tintaDoLaco(PorterDuff.Mode.CLEAR)
            tela.drawRect(
                pixels.esquerda.toFloat(),
                pixels.topo.toFloat(),
                (pixels.esquerda + pixels.largura).toFloat(),
                (pixels.topo + pixels.altura).toFloat(),
                limpar,
            )
            return MontagemDaSelecaoDoLaco(
                saida,
                imagem.esquerda,
                imagem.topo,
                imagem.largura,
                imagem.altura,
            )
        }
        val extraido = copiaSolta(imagem.bitmap, pixels.esquerda, pixels.topo, pixels.largura, pixels.altura)
        val alinhada = AlinhamentoDoRetanguloAGradeDePixelsDaImagem.daSelecao(
            recorte.left,
            recorte.top,
            recorte.width(),
            recorte.height(),
            imagem.esquerda,
            imagem.topo,
            imagem.largura,
            imagem.altura,
            imagem.bitmap.width,
            imagem.bitmap.height,
        ) ?: return MontagemDaSelecaoDoLaco(
            extraido,
            imagem.esquerda + pixels.esquerda.toFloat() * imagem.largura / imagem.bitmap.width,
            imagem.topo + pixels.topo.toFloat() * imagem.altura / imagem.bitmap.height,
            pixels.largura.toFloat() * imagem.largura / imagem.bitmap.width,
            pixels.altura.toFloat() * imagem.altura / imagem.bitmap.height,
        )
        return MontagemDaSelecaoDoLaco(
            extraido,
            alinhada.esquerda,
            alinhada.topo,
            alinhada.largura,
            alinhada.altura,
        )
    }

    private fun bitmapComBuracoForaDoRetangulo(
        origem: Bitmap,
        pixels: RetanguloEmPixelsDaImagem,
    ): Bitmap {
        val mutavel = origem.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        val tela = Canvas(mutavel)
        val limpar = tintaDoLaco(PorterDuff.Mode.CLEAR)
        val dir = pixels.esquerda + pixels.largura
        val baixo = pixels.topo + pixels.altura
        tela.drawRect(0f, 0f, mutavel.width.toFloat(), pixels.topo.toFloat(), limpar)
        tela.drawRect(0f, baixo.toFloat(), mutavel.width.toFloat(), mutavel.height.toFloat(), limpar)
        tela.drawRect(0f, pixels.topo.toFloat(), pixels.esquerda.toFloat(), baixo.toFloat(), limpar)
        tela.drawRect(dir.toFloat(), pixels.topo.toFloat(), mutavel.width.toFloat(), baixo.toFloat(), limpar)
        return mutavel
    }

    private fun copiarSelecaoAtivaDoRetangulo(): Boolean {
        if (!selecaoAtivaDoRetangulo()) return true
        val imagem = garantirImagemAlvoParaRecorte() ?: return false
        fundirTracosNaImagem(imagem)
        val montagem = montarBitmapDaSelecaoDoRetangulo(imagem) ?: return true
        return armarModoColarComPrevia(
            origem = imagem,
            bitmap = montagem.bitmap,
            esquerda = montagem.esquerda,
            topo = montagem.topo,
            largura = montagem.largura,
            altura = montagem.altura,
        )
    }

    private fun moverSelecaoAtivaDoRetangulo(): Boolean {
        if (!selecaoAtivaDoRetangulo()) return true
        val imagem = garantirImagemAlvoParaRecorte() ?: return false
        fundirTracosNaImagem(imagem)
        registrarOrigemDoRetangulo(forcar = false)
        val origem = RectF(recorteNaOrigem)
        val atualEsquerda = recorte.left
        val atualTopo = recorte.top
        val atualLargura = recorte.width()
        val atualAltura = recorte.height()
        // Extrai/fura na origem; coloca no retângulo atual.
        recorte.set(origem)
        val montagem = montarBitmapDaSelecaoDoRetangulo(imagem) ?: run {
            recorte.set(atualEsquerda, atualTopo, atualEsquerda + atualLargura, atualTopo + atualAltura)
            return true
        }
        val pixels = pixelsDoRecorte(imagem) ?: run {
            recorte.set(atualEsquerda, atualTopo, atualEsquerda + atualLargura, atualTopo + atualAltura)
            return true
        }
        val anterior = imagem.bitmap
        imagem.bitmap = if (selecaoDoRetanguloInvertida) {
            bitmapComBuracoForaDoRetangulo(anterior, pixels)
        } else {
            bitmapComBuraco(anterior, pixels.esquerda, pixels.topo, pixels.largura, pixels.altura)
        }
        val invertida = selecaoDoRetanguloInvertida
        selecaoDoRetanguloInvertida = false
        // Destino alinhado à grade (mesmo critério do extrato) — sem drift no Mover.
        val destino = AlinhamentoDoRetanguloAGradeDePixelsDaImagem.daSelecao(
            atualEsquerda,
            atualTopo,
            atualLargura,
            atualAltura,
            imagem.esquerda,
            imagem.topo,
            imagem.largura,
            imagem.altura,
            imagem.bitmap.width,
            imagem.bitmap.height,
        )
        val destEsq = destino?.esquerda ?: montagem.esquerda
        val destTop = destino?.topo ?: montagem.topo
        // Mesmo tamanho do extrato (pixels) para não esticar fração de pixel.
        val destLar = montagem.largura
        val destAlt = montagem.altura
        if (!PilhaDeCamadasDaComposicao.cabeMaisUma(objetos.size)) {
            imagem.bitmap = anterior
            selecaoDoRetanguloInvertida = invertida
            recorte.set(atualEsquerda, atualTopo, atualEsquerda + atualLargura, atualTopo + atualAltura)
            return false
        }
        colocarPecaDestacada(
            imagem,
            montagem.bitmap,
            destEsq,
            destTop,
            destLar,
            destAlt,
            anterior,
        )
        objetos.find { it.id == idDaCamadaAtiva }?.angulo = imagem.angulo
        marcarFundirAoEncerrarAposMover(imagem.id)
        return true
    }

    private fun excluirSelecaoAtivaDoRetangulo(): Boolean {
        if (!selecaoAtivaDoRetangulo()) return true
        val imagem = garantirImagemAlvoParaRecorte() ?: return false
        fundirTracosNaImagem(imagem)
        val pixels = pixelsDoRecorte(imagem) ?: return true
        val anterior = imagem.bitmap
        imagem.bitmap = if (selecaoDoRetanguloInvertida) {
            bitmapComBuracoForaDoRetangulo(anterior, pixels)
        } else {
            bitmapComBuraco(anterior, pixels.esquerda, pixels.topo, pixels.largura, pixels.altura)
        }
        empilhar(Passo.AlteraBitmap(imagem.id, anterior))
        selecaoDoRetanguloInvertida = false
        limparSelecaoDoRetangulo()
        aoMudarSelecao?.invoke()
        aoMudarSelecaoAtivaDoLaco?.invoke()
        camadaSuja = true
        invalidate()
        return true
    }

    private fun reduzirMolduraASelecaoDoRetangulo(): Boolean {
        if (!selecaoAtivaDoRetangulo()) return false
        val composto = compor() ?: return false
        val area = AreaDoRecorteSobreAImagemDaComposicao(
            recorte.left,
            recorte.top,
            recorte.width(),
            recorte.height(),
        )
        if (!DecisaoDoRetanguloSobreAImagemDaComposicao.cabe(area.largura, area.altura)) {
            composto.recycle()
            return false
        }
        val pixels = DecisaoDoRetanguloSobreAImagemDaComposicao.pixels(
            area.esquerda,
            area.topo,
            area.largura,
            area.altura,
            0f,
            0f,
            molduraLargura.toFloat(),
            molduraAltura.toFloat(),
            composto.width,
            composto.height,
        )
        if (pixels == null) {
            composto.recycle()
            return false
        }
        val extraido = copiaSolta(composto, pixels.esquerda, pixels.topo, pixels.largura, pixels.altura)
        if (extraido !== composto && !composto.isRecycled) composto.recycle()
        val molduraAntesLargura = molduraLargura
        val molduraAntesAltura = molduraAltura
        empilhar(Passo.Moldura(molduraAntesLargura, molduraAntesAltura))
        objetos.toList().forEach { peca ->
            objetos.remove(peca)
            pecasFora[peca.id] = peca
            reciclarTinta(peca)
        }
        val peca = Peca.Imagem(novoId(), extraido)
        peca.esquerda = 0f
        peca.topo = 0f
        peca.largura = area.largura
        peca.altura = area.altura
        objetos += peca
        idDaCamadaAtiva = peca.id
        selecionadoId = -1
        molduraLargura = area.largura.toInt().coerceAtLeast(1)
        molduraAltura = area.altura.toInt().coerceAtLeast(1)
        zoom = 1f
        anguloDaVista = 0f
        panX = 0f
        panY = 0f
        selecaoDoRetanguloInvertida = false
        ferramenta = Ferramenta.VERMELHO
        idDaImagemDoRecorte = -1
        aoMudarFerramenta?.invoke(Ferramenta.VERMELHO)
        aoMudarSelecao?.invoke()
        camadaSuja = true
        invalidate()
        return true
    }

    /** Desenhos que ficam em pé com a vista girada: rodam depois de desfazer a rotação do canvas. */
    private val desenhosEmPe = mutableListOf<(Canvas) -> Unit>()

    /** Caixa (coordenadas desgiradas) → menor retângulo reto que a cobre na tela real. */
    private fun caixaNaTelaReal(caixa: RectF): RectF {
        val cantos = listOf(
            paraATelaReal(caixa.left, caixa.top),
            paraATelaReal(caixa.right, caixa.top),
            paraATelaReal(caixa.right, caixa.bottom),
            paraATelaReal(caixa.left, caixa.bottom),
        )
        return RectF(cantos.minOf { it[0] }, cantos.minOf { it[1] }, cantos.maxOf { it[0] }, cantos.maxOf { it[1] })
    }

    /** Botões da seleção ficam em coordenadas reais: o toque (desgirado) é convertido antes de testar. */
    private fun chipNoToque(xVista: Float, yVista: Float): ChipDaSelecaoAtivaDoLaco? {
        val (x, y) = paraATelaReal(xVista, yVista)
        return chipsDaSelecaoAtiva.firstOrNull { it.area.contains(x, y) }
    }

    private fun desenharBarraFlutuanteDaSelecaoAtiva(canvas: Canvas, bbox: RectF) {
        if (anguloDaVista != 0f) {
            val caixa = caixaNaTelaReal(bbox)
            desenhosEmPe += { tela -> desenharBarraFlutuanteDaSelecaoAtivaEmPe(tela, caixa) }
            return
        }
        desenharBarraFlutuanteDaSelecaoAtivaEmPe(canvas, bbox)
    }

    private fun desenharBarraFlutuanteDaSelecaoAtivaEmPe(canvas: Canvas, bbox: RectF) {
        chipsDaSelecaoAtiva.clear()
        val densidade = resources.displayMetrics.density
        val alturaBarra = 36f * densidade
        val folga = 12f * densidade
        val lado = PosicaoDaBarraFlutuanteDaSelecaoAtiva.decidir(
            bboxTopo = bbox.top,
            bboxBaixo = bbox.bottom,
            alturaVisivel = height.toFloat(),
            alturaBarra = alturaBarra,
            folga = folga,
        )
        val rotulos = listOf(
            AcaoDaSelecaoAtivaDoLaco.COPIAR to context.getString(R.string.copiar_selecao_ativa_do_laco),
            AcaoDaSelecaoAtivaDoLaco.MOVER to context.getString(R.string.mover_selecao_ativa_do_laco),
            AcaoDaSelecaoAtivaDoLaco.EXCLUIR to context.getString(R.string.excluir_selecao_ativa_do_laco),
            AcaoDaSelecaoAtivaDoLaco.INVERTER to context.getString(R.string.inverter_selecao_ativa_do_laco),
            AcaoDaSelecaoAtivaDoLaco.RECORTAR to context.getString(R.string.recortar_selecao_ativa_do_laco),
        )
        tintaDoChipDaSelecao.textSize = 13f * densidade
        val paddingX = 14f * densidade
        val gap = 8f * densidade
        val larguras = rotulos.map { (_, texto) ->
            tintaDoChipDaSelecao.measureText(texto) + paddingX * 2f
        }
        val larguraTotal = larguras.sum() + gap * (larguras.size - 1)
        var x = (bbox.centerX() - larguraTotal / 2f).coerceIn(8f * densidade, width - larguraTotal - 8f * densidade)
        val y = when (lado) {
            LadoDaBarraFlutuanteDaSelecaoAtiva.ACIMA -> bbox.top - folga - alturaBarra
            LadoDaBarraFlutuanteDaSelecaoAtiva.ABAIXO -> bbox.bottom + folga
        }
        rotulos.forEachIndexed { indice, (acao, texto) ->
            val w = larguras[indice]
            val area = RectF(x, y, x + w, y + alturaBarra)
            canvas.drawRoundRect(area, 8f * densidade, 8f * densidade, fundoDoChipDaSelecao)
            val textoY = area.centerY() - (tintaDoChipDaSelecao.ascent() + tintaDoChipDaSelecao.descent()) / 2f
            canvas.drawText(texto, area.centerX(), textoY, tintaDoChipDaSelecao)
            chipsDaSelecaoAtiva += ChipDaSelecaoAtivaDoLaco(acao, RectF(area))
            x += w + gap
        }
    }

    private fun garantirImagemAlvoParaRecorte(): Peca.Imagem? {
        when (val ativa = objetos.find { it.id == idDaCamadaAtiva }) {
            is Peca.Imagem -> {
                idDaImagemDoRecorte = ativa.id
                return ativa
            }
            is Peca.Folha -> {
                if (!FonteDoDestacarDaCamadaAtiva.precisaPromoverFolha(ehFolha = true)) return null
                return promoverFolhaParaImagem(ativa)
            }
            else -> Unit
        }
        return imagemDoRecorte()
    }

    private fun promoverFolhaParaImagem(folha: Peca.Folha): Peca.Imagem? {
        if (molduraLargura <= 0 || molduraAltura <= 0) return null
        if (tracoAtual?.idDaPeca == folha.id) {
            val atual = tracoAtual
            if (atual != null) {
                folha.tracos += atual
                cancelarTracoAtual()
            }
        }
        val bitmap = bitmapDosTracosDaFolha(folha) ?: return null
        // A folha fica intacta (com os traços) dentro do passo: desfazer a devolve e os riscos dela voltam a funcionar.
        reciclarTinta(folha)
        folha.tintaSuja = true
        val imagem = imagemComOsDadosDaFolha(folha, folha.id, bitmap)
        val indice = objetos.indexOfFirst { it.id == folha.id }
        if (indice < 0) {
            bitmap.recycle()
            return null
        }
        objetos[indice] = imagem
        empilhar(Passo.Promocao(folha, imagem, aplicado = true))
        idDaCamadaAtiva = imagem.id
        idDaImagemDoRecorte = imagem.id
        camadaSuja = true
        aoMudarSelecao?.invoke()
        return imagem
    }

    /** Os traços da folha pintados num bitmap do tamanho da moldura (null sem memória). */
    private fun bitmapDosTracosDaFolha(folha: Peca.Folha): Bitmap? {
        val largura = molduraLargura
        val altura = molduraAltura
        if (largura <= 0 || altura <= 0) return null
        val bitmap = try {
            Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
        } catch (_: OutOfMemoryError) {
            return null
        }
        bitmap.eraseColor(Color.TRANSPARENT)
        val tela = Canvas(bitmap)
        folha.tracos.forEach { desenharTraco(tela, it, 0, largura.toFloat(), altura.toFloat()) }
        return bitmap
    }

    private fun imagemComOsDadosDaFolha(folha: Peca.Folha, id: Int, bitmap: Bitmap): Peca.Imagem {
        val imagem = Peca.Imagem(id, bitmap)
        imagem.esquerda = 0f
        imagem.topo = 0f
        imagem.largura = molduraLargura.toFloat()
        imagem.altura = molduraAltura.toFloat()
        imagem.angulo = folha.angulo
        imagem.visivel = folha.visivel
        imagem.opacidade = folha.opacidade
        imagem.alfaBloqueado = folha.alfaBloqueado
        return imagem
    }

    private fun areaDaCaixaSobre(imagem: Peca.Imagem, caixa: CaixaDoLacoLivre) =
        DecisaoDoRetanguloSobreAImagemDaComposicao.intersecao(
            caixa.esquerda,
            caixa.topo,
            caixa.largura,
            caixa.altura,
            imagem.esquerda,
            imagem.topo,
            imagem.largura,
            imagem.altura,
        )

    private fun pixelsDaCaixa(imagem: Peca.Imagem, caixa: CaixaDoLacoLivre) = DecisaoDoRetanguloSobreAImagemDaComposicao.pixels(
        caixa.esquerda,
        caixa.topo,
        caixa.largura,
        caixa.altura,
        imagem.esquerda,
        imagem.topo,
        imagem.largura,
        imagem.altura,
        imagem.bitmap.width,
        imagem.bitmap.height,
    )

    private fun bitmapMascaradoPeloLaco(
        origem: Bitmap,
        pontos: List<PontoDoLacoLivre>,
        imagemEsquerda: Float,
        imagemTopo: Float,
        imagemLargura: Float,
        imagemAltura: Float,
        pixels: RetanguloEmPixelsDaImagem,
        invertida: Boolean = selecaoDoLacoInvertida,
    ): Bitmap {
        val recorteDoBitmap = Bitmap.createBitmap(origem, pixels.esquerda, pixels.topo, pixels.largura, pixels.altura)
        val saida = recorteDoBitmap.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        if (saida !== recorteDoBitmap && recorteDoBitmap !== origem && !recorteDoBitmap.isRecycled) recorteDoBitmap.recycle()
        val tela = Canvas(saida)
        val limpar = tintaDoLaco(PorterDuff.Mode.CLEAR)
        val escalaX = origem.width / imagemLargura.coerceAtLeast(1f)
        val escalaY = origem.height / imagemAltura.coerceAtLeast(1f)
        if (invertida) {
            desenharAreaDoLaco(
                tela,
                pontos,
                imagemEsquerda,
                imagemTopo,
                escalaX,
                escalaY,
                pixels.esquerda,
                pixels.topo,
                limpar,
            )
        } else {
            desenharForaDaAreaDoLaco(
                tela,
                pontos,
                imagemEsquerda,
                imagemTopo,
                escalaX,
                escalaY,
                pixels.esquerda,
                pixels.topo,
                saida.width,
                saida.height,
                limpar,
            )
        }
        // Path no Skia ainda deixa cobertura parcial na borda → “traço” sobre a original.
        endurecerAlphaDaMascaraDoLaco(saida)
        return saida
    }

    /** Torna a máscara binária (opaco ou transparente) para não sobrar franja semitransparente. */
    private fun endurecerAlphaDaMascaraDoLaco(bitmap: Bitmap) {
        val largura = bitmap.width
        val altura = bitmap.height
        if (largura <= 0 || altura <= 0) return
        val pixels = IntArray(largura * altura)
        bitmap.getPixels(pixels, 0, largura, 0, 0, largura, altura)
        for (i in pixels.indices) {
            val alpha = pixels[i] ushr 24
            pixels[i] = when {
                alpha == 0 -> 0
                alpha == 255 -> pixels[i]
                alpha < 128 -> 0
                else -> pixels[i] or 0xFF000000.toInt()
            }
        }
        bitmap.setPixels(pixels, 0, largura, 0, 0, largura, altura)
    }

    private fun bitmapComBuracoDoLaco(
        origem: Bitmap,
        pontos: List<PontoDoLacoLivre>,
        imagem: Peca.Imagem,
    ): Bitmap {
        val mutavel = origem.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        val tela = Canvas(mutavel)
        val limpar = tintaDoLaco(PorterDuff.Mode.CLEAR)
        val escalaX = mutavel.width / imagem.largura.coerceAtLeast(1f)
        val escalaY = mutavel.height / imagem.altura.coerceAtLeast(1f)
        desenharAreaDoLaco(
            tela,
            pontos,
            imagem.esquerda,
            imagem.topo,
            escalaX,
            escalaY,
            0,
            0,
            limpar,
        )
        return mutavel
    }

    /** Extração usa CLEAR sem AA; o endurecerAlpha remove franja residual do Path. */
    private fun tintaDoLaco(modo: PorterDuff.Mode, comAntiAlias: Boolean = false) = Paint().apply {
        isAntiAlias = comAntiAlias
        isDither = false
        isFilterBitmap = false
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(modo)
    }

    private fun desenharAreaDoLaco(
        tela: Canvas,
        pontos: List<PontoDoLacoLivre>,
        imagemEsquerda: Float,
        imagemTopo: Float,
        escalaX: Float,
        escalaY: Float,
        pixelsEsquerda: Int,
        pixelsTopo: Int,
        tinta: Paint,
    ) {
        if (pontos.size < 3) return
        val caminho = Path()
        pontos.forEachIndexed { indice, ponto ->
            val noRecorte = MapeamentoDoLacoParaPixelsDoRecorteDaImagem.pontoNoRecorte(
                ponto.x,
                ponto.y,
                imagemEsquerda,
                imagemTopo,
                escalaX,
                escalaY,
                pixelsEsquerda,
                pixelsTopo,
            )
            if (indice == 0) caminho.moveTo(noRecorte.x, noRecorte.y) else caminho.lineTo(noRecorte.x, noRecorte.y)
        }
        caminho.close()
        tela.drawPath(caminho, tinta)
    }

    private fun desenharForaDaAreaDoLaco(
        tela: Canvas,
        pontos: List<PontoDoLacoLivre>,
        imagemEsquerda: Float,
        imagemTopo: Float,
        escalaX: Float,
        escalaY: Float,
        pixelsEsquerda: Int,
        pixelsTopo: Int,
        pixelsLargura: Int,
        pixelsAltura: Int,
        tinta: Paint,
    ) {
        if (pontos.size < 3) return
        val caminho = Path().apply { fillType = Path.FillType.EVEN_ODD }
        caminho.addRect(0f, 0f, pixelsLargura.toFloat(), pixelsAltura.toFloat(), Path.Direction.CW)
        val area = Path()
        pontos.forEachIndexed { indice, ponto ->
            val noRecorte = MapeamentoDoLacoParaPixelsDoRecorteDaImagem.pontoNoRecorte(
                ponto.x,
                ponto.y,
                imagemEsquerda,
                imagemTopo,
                escalaX,
                escalaY,
                pixelsEsquerda,
                pixelsTopo,
            )
            if (indice == 0) area.moveTo(noRecorte.x, noRecorte.y) else area.lineTo(noRecorte.x, noRecorte.y)
        }
        area.close()
        caminho.addPath(area)
        tela.drawPath(caminho, tinta)
    }

    private fun tocarLaco(event: MotionEvent, id: Int): Boolean {
        if (lacoFechado || modoDoDedo == ModoDoDedo.IGNORAR) return true
        val indice = indiceDoId(event, id)
        if (indice < 0) return true
        val ponto = paraAComposicao(event.getX(indice), event.getY(indice))
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.getPointerId(event.actionIndex) != id) return true
                iniciosDosTracosDoLaco.add(pontosDoLaco.size)
                if (ponto != null) acrescentarPontoDoLaco(ponto[0], ponto[1])
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (ponto == null) return true
                for (historico in 0 until event.historySize) {
                    val historicoX = event.getHistoricalX(indice, historico)
                    val historicoY = event.getHistoricalY(indice, historico)
                    val antigo = paraAComposicao(historicoX, historicoY) ?: continue
                    acrescentarPontoDoLaco(antigo[0], antigo[1])
                }
                acrescentarPontoDoLaco(ponto[0], ponto[1])
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.actionMasked != MotionEvent.ACTION_CANCEL && event.getPointerId(event.actionIndex) != id) return true
                if (event.actionMasked != MotionEvent.ACTION_CANCEL && ponto != null) {
                    acrescentarPontoDoLaco(ponto[0], ponto[1])
                }
                val solto = LacoLivreDaSelecaoSobreAImagemDaComposicao.soltar(pontosDoLaco.toList())
                pontosDoLaco.clear()
                pontosDoLaco.addAll(solto)
                val inicio = iniciosDosTracosDoLaco.lastOrNull()
                if (inicio != null && inicio >= pontosDoLaco.size) {
                    iniciosDosTracosDoLaco.removeAt(iniciosDosTracosDoLaco.lastIndex)
                }
                invalidate()
            }
        }
        return true
    }

    private fun acrescentarPontoDoLaco(x: Float, y: Float) {
        if (lacoFechado) return
        val naMoldura = LacoLivreDaSelecaoSobreAImagemDaComposicao.pontoNaMoldura(
            x,
            y,
            molduraLargura.toFloat(),
            molduraAltura.toFloat(),
        )
        val limitadoX = naMoldura.x
        val limitadoY = naMoldura.y
        val anterior = pontosDoLaco.lastOrNull()
        val inicio = pontosDoLaco.firstOrNull()
        val raio = raioDeFechamentoDoLaco()
        val pertoDoInicio = inicio != null && hypot(inicio.x - limitadoX, inicio.y - limitadoY) <= raio
        if (anterior != null && !pertoDoInicio && hypot(anterior.x - limitadoX, anterior.y - limitadoY) < 3f) return
        val retomada = LacoLivreDaSelecaoSobreAImagemDaComposicao.continuar(
            pontosDoLaco.toList(),
            listOf(PontoDoLacoLivre(limitadoX, limitadoY)),
        )
        pontosDoLaco.clear()
        pontosDoLaco.addAll(retomada)
        if (!LacoLivreDaSelecaoSobreAImagemDaComposicao.deveFechar(pontosDoLaco, raio)) return
        val fechado = LacoLivreDaSelecaoSobreAImagemDaComposicao.fechar(pontosDoLaco, raio)
        pontosDoLaco.clear()
        pontosDoLaco.addAll(fechado)
        lacoFechado = true
        modoDaMascaraDoLaco = ModoDaMascaraDoLaco.NENHUM
        registrarOrigemDoLacoSePreciso()
        aoMudarSelecaoAtivaDoLaco?.invoke()
    }

    private fun raioDeFechamentoDoLaco(): Float {
        val escala = destino.width() / molduraLargura.toFloat().coerceAtLeast(1f)
        return 36f * resources.displayMetrics.density / escala.coerceAtLeast(0.01f)
    }

    private fun desenharLaco(canvas: Canvas) {
        if (pontosDoLaco.size < 2) return
        val quadro = rectNaTela(0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat())
        if (lacoFechado) {
            val caminhoArea = caminhoDoLacoNaTela(pontosDoLaco)
            caminhoArea.close()
            if (selecaoDoLacoInvertida) {
                canvas.drawPath(caminhoArea, escurecer)
            } else {
                val camada = canvas.saveLayer(quadro, null)
                canvas.drawRect(quadro, escurecer)
                canvas.drawPath(caminhoArea, tintaDoLaco(PorterDuff.Mode.CLEAR))
                canvas.restoreToCount(camada)
            }
        }
        val caminho = caminhoDoLacoNaTela(pontosDoLaco)
        if (lacoFechado) caminho.close()
        canvas.drawPath(caminho, contornoEscuro)
        canvas.drawPath(caminho, contornoClaro)
        if (!lacoFechado) return
        val bbox = bboxDaSelecaoAtivaNaTela() ?: return
        desenharContorno(canvas, bbox)
        desenharAlcas(canvas, bbox)
        desenharCaboDaPeca(canvas, bbox)
        desenharBarraFlutuanteDaSelecaoAtiva(canvas, bbox)
    }

    private fun bboxDaSelecaoAtivaNaTela(): RectF? {
        if (selecaoDoLacoInvertida) {
            return rectNaTela(0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat())
        }
        val caixa = LacoLivreDaSelecaoSobreAImagemDaComposicao.caixa(pontosDoLaco.toList()) ?: return null
        return rectNaTela(caixa.esquerda, caixa.topo, caixa.largura, caixa.altura)
    }

    private fun caixaDaSelecaoAtivaParaExtrair(imagem: Peca.Imagem, caixaDoLaco: CaixaDoLacoLivre): CaixaDoLacoLivre {
        if (!selecaoDoLacoInvertida) return caixaDoLaco
        return CaixaDoLacoLivre(imagem.esquerda, imagem.topo, imagem.largura, imagem.altura)
    }

    private fun caminhoDoLacoNaTela(pontos: List<PontoDoLacoLivre>): Path {
        val caminho = Path()
        pontos.forEachIndexed { indice, ponto ->
            val tela = rectNaTela(ponto.x, ponto.y, 0f, 0f)
            if (indice == 0) caminho.moveTo(tela.left, tela.top) else caminho.lineTo(tela.left, tela.top)
        }
        return caminho
    }

    private fun limparSelecaoDoRetangulo() {
        val vazio = RetanguloDeRecorteComecaVazioComoOLaco.retanguloInicialVazio()
        recortarDentroDaMoldura = true
        selecaoDoRetanguloInvertida = false
        temOrigemDoRetangulo = false
        recorteNaOrigem.set(0f, 0f, 0f, 0f)
        chipsDaSelecaoAtiva.clear()
        recorte.set(vazio.esquerda, vazio.topo, vazio.esquerda + vazio.largura, vazio.topo + vazio.altura)
        arraste = null
    }

    private fun encaixarRecorteNaGradeDePixels(imagem: Peca.Imagem?) {
        val alvo = imagem ?: imagemDoRecorte() ?: return
        if (recorte.width() <= 0f || recorte.height() <= 0f) return
        val alinhada = AlinhamentoDoRetanguloAGradeDePixelsDaImagem.daSelecao(
            recorte.left,
            recorte.top,
            recorte.width(),
            recorte.height(),
            alvo.esquerda,
            alvo.topo,
            alvo.largura,
            alvo.altura,
            alvo.bitmap.width,
            alvo.bitmap.height,
        ) ?: return
        recorte.set(
            alinhada.esquerda,
            alinhada.topo,
            alinhada.esquerda + alinhada.largura,
            alinhada.topo + alinhada.altura,
        )
    }

    private fun registrarOrigemDoRetangulo(forcar: Boolean) {
        if (!selecaoAtivaDoRetangulo()) return
        encaixarRecorteNaGradeDePixels(imagemDoRecorte())
        if (forcar || !temOrigemDoRetangulo) {
            recorteNaOrigem.set(recorte)
            temOrigemDoRetangulo = true
        }
    }

    private fun prepararRecorteInicial(imagem: Peca.Imagem) {
        recortarDentroDaMoldura = false
        val area = RetanguloInicialDoRecorteSobreAImagemDaComposicao.de(
            esquerda = imagem.esquerda,
            topo = imagem.topo,
            largura = imagem.largura,
            altura = imagem.altura,
        )
        recorte.set(area.esquerda, area.topo, area.direita, area.baixo)
        encaixarRecorteNaGradeDePixels(imagem)
    }

    private fun prepararRecorteDaMoldura() {
        recortarDentroDaMoldura = true
        val area = RetanguloInicialDoRecorteSobreAImagemDaComposicao.daMoldura(
            molduraLargura.toFloat(),
            molduraAltura.toFloat(),
        )
        recorte.set(area.esquerda, area.topo, area.direita, area.baixo)
    }

    private fun tentarSelecaoInteligenteNoPonto(x: Float, y: Float): Boolean {
        if (!selecaoInteligenteArmada) return false
        val imagens = objetos.mapNotNull { peca ->
            val imagem = peca as? Peca.Imagem ?: return@mapNotNull null
            AreaDaImagemParaSelecaoInteligenteDoRecorte(
                id = imagem.id,
                esquerda = imagem.esquerda,
                topo = imagem.topo,
                largura = imagem.largura,
                altura = imagem.altura,
            )
        }
        val idPelaRegra = SelecaoInteligenteDoRecorteSobreAComposicao.idDaImagemNoPonto(x, y, imagens)
        val imagem = (objetos.find { it.id == idPelaRegra } as? Peca.Imagem)
            ?: (pecaEm(x, y) as? Peca.Imagem)
            ?: return true
        idDaImagemDoRecorte = imagem.id
        selecionadoId = imagem.id
        selecaoInteligenteArmada = false
        prepararRecorteInicial(imagem)
        registrarOrigemDoRetangulo(forcar = true)
        aoMudarSelecao?.invoke()
        aoMudarModoDoDedo?.invoke()
        aoMudarSelecaoAtivaDoLaco?.invoke()
        invalidate()
        return true
    }

    private fun imagemAlvo(): Peca.Imagem? {
        return (objetos.find { it.id == idDaCamadaAtiva } as? Peca.Imagem)
            ?: (objetos.find { it.id == selecionadoId } as? Peca.Imagem)
    }

    private fun imagemDoRecorte(): Peca.Imagem? {
        val ids = objetos.mapNotNull { (it as? Peca.Imagem)?.id }
        val id = ImagemAlvoDoRecorteDaComposicao.idExplicito(idDaImagemDoRecorte, ids) ?: return null
        return objetos.find { it.id == id } as? Peca.Imagem
    }

    private fun tocarTraco(event: MotionEvent, idDaCaneta: Int): Boolean {
        val indice = indiceDoId(event, idDaCaneta)
        if (indice < 0) {
            cancelarTracoAtual()
            return true
        }
        val botaoLateral = botaoLateralDaCanetaPressionado ||
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.botaoLateralPressionado(
                event.buttonState,
            )
        val borracha = BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.tracoEhBorracha(
            ferramentaEhBorrachaNaBarra = ferramenta == Ferramenta.BORRACHA,
            pontaFisicaEhBorracha = event.getToolType(indice) == MotionEvent.TOOL_TYPE_ERASER,
            botaoLateralPressionado = botaoLateral,
        )
        if (!borracha && pontaDoTraco == PontaDoTracoDaComposicao.SETA) return tocarSeta(event, idDaCaneta, indice)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.getPointerId(event.actionIndex) != idDaCaneta || tracoAtual != null) return true
                // Cópia ainda flutuando (antes do “Colar”): o traço iria para a camada de baixo e a
                // prévia por cima ficava intacta. Começar a pintar/apagar cola a cópia primeiro.
                if (modoColarDaSelecao) confirmarColarNaCamadaAtiva()
                // Pedaço movido ainda na moldura de transformar: funde de volta na camada antes do traço.
                if (fundirPecaAoEncerrarTransformar && selecionadoId != -1) limparSelecaoDaPeca()
                val peca = pecaDoTraco() ?: return true
                val ponta = when (pontaDoTraco) {
                    PontaDoTracoDaComposicao.MARCADOR,
                    PontaDoTracoDaComposicao.NANQUIM,
                    PontaDoTracoDaComposicao.LAPIS,
                    -> pontaDoTraco
                    else -> PontaDoTracoDaComposicao.CANETA
                }
                formaFixadaNoTraco = false
                ancoraParadaX = event.getX(indice)
                ancoraParadaY = event.getY(indice)
                pontosAntesDaParada = 0
                removeCallbacks(fixarFormaLimpa)
                postDelayed(fixarFormaLimpa, FormaLimpaNoFimDoTraco.TEMPO_PARADO_MS)
                tracoAtual = Traco(
                    idDaPeca = peca.id,
                    cor = if (borracha && pincelDaBorracha != null) {
                        corDaBorrachaDoMotor(pincelDaBorracha!!)
                    } else if (borracha) {
                        corDaBorracha()
                    } else if (pincelDoMotor != null) {
                        corDoTracoDoMotor(pincelDoMotor!!)
                    } else {
                        comOpacidadeDoTraco(PaletaDoEditorDeImagem.comOpacidade(corDoTraco, GeometriaDaPontaDoTraco.translucida(ponta)))
                    },
                    borracha = borracha,
                    ponta = ponta,
                    pincel = if (borracha) pincelDaBorracha else pincelDoMotor,
                    semente = kotlin.random.Random.nextLong(),
                )
                idDoPonteiroDoTraco = idDaCaneta
                pontosJaNaTinta = 0
                acrescentarPonto(event, indice, tracoAtual!!, null)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val atual = tracoAtual ?: return true
                if (idDoPonteiroDoTraco != idDaCaneta) return true
                // Forma já fixada: a caneta pode tremer até soltar sem estragar a forma.
                if (formaFixadaNoTraco) return true
                for (historico in 0 until event.historySize) acrescentarPonto(event, indice, atual, historico)
                acrescentarPonto(event, indice, atual, null)
                val mexeu = Math.hypot(
                    (event.getX(indice) - ancoraParadaX).toDouble(),
                    (event.getY(indice) - ancoraParadaY).toDouble(),
                ) > 6.0 * resources.displayMetrics.density
                if (mexeu) {
                    ancoraParadaX = event.getX(indice)
                    ancoraParadaY = event.getY(indice)
                    pontosAntesDaParada = atual.pontos.size
                    removeCallbacks(fixarFormaLimpa)
                    postDelayed(fixarFormaLimpa, FormaLimpaNoFimDoTraco.TEMPO_PARADO_MS)
                }
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(event.actionIndex) != idDaCaneta) return true
                removeCallbacks(fixarFormaLimpa)
                val atual = tracoAtual
                if (atual != null && atual.pontos.isNotEmpty()) {
                    if (!formaFixadaNoTraco) {
                        for (historico in 0 until event.historySize) acrescentarPonto(event, indice, atual, historico)
                        finalizandoTraco = true
                        try {
                            acrescentarPonto(event, indice, atual, null)
                        } finally {
                            finalizandoTraco = false
                        }
                    }
                    formaFixadaNoTraco = false
                    aplicarTrechoAtualNaTinta()
                    val peca = objetos.find { it.id == atual.idDaPeca }
                    if (peca != null) {
                        concluirTracoNaPeca(peca, atual)
                    }
                }
                tracoAtual = null
                idDoPonteiroDoTraco = -1
                pontosJaNaTinta = 0
                soltarAncoraDoTraco()
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                cancelarTracoAtual()
                invalidate()
            }
        }
        return true
    }

    private fun tocarSeta(event: MotionEvent, idDaCaneta: Int, indice: Int): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.getPointerId(event.actionIndex) != idDaCaneta) return true
                idDoPonteiroDoTraco = idDaCaneta
                origemDaSeta = pontoDoToque(event, indice)
                previaDaSeta = null
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (idDoPonteiroDoTraco != idDaCaneta || origemDaSeta == null) return true
                previaDaSeta = pontoDoToque(event, indice)
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(event.actionIndex) != idDaCaneta) return true
                concluirToqueDaSeta(event, indice)
                idDoPonteiroDoTraco = -1
            }
            MotionEvent.ACTION_CANCEL -> {
                origemDaSeta = null
                previaDaSeta = null
                idDoPonteiroDoTraco = -1
                invalidate()
            }
        }
        return true
    }

    private fun concluirToqueDaSeta(event: MotionEvent, ponteiro: Int) {
        val origem = origemDaSeta
        val dedo = pontoDoToque(event, ponteiro)
        origemDaSeta = null
        previaDaSeta = null
        if (origem == null || dedo == null) {
            invalidate()
            return
        }
        val peca = pecaDoTraco() ?: return
        val larguraPx = dedo.largura * molduraLargura
        val flecha = SetaSobreAFolha.flechaDoArraste(
            origem.x * molduraLargura,
            origem.y * molduraAltura,
            dedo.x * molduraLargura,
            dedo.y * molduraAltura,
            larguraPx,
        )
        if (flecha == null) {
            invalidate()
            return
        }
        val traco = Traco(
            idDaPeca = peca.id,
            pontos = mutableListOf(
                Ponto(flecha.caudaX / molduraLargura, flecha.caudaY / molduraAltura, dedo.largura),
                Ponto(flecha.pontaX / molduraLargura, flecha.pontaY / molduraAltura, dedo.largura),
            ),
            cor = comOpacidadeDoTraco(PaletaDoEditorDeImagem.comOpacidade(corDoTraco, false)),
            borracha = false,
            ponta = PontaDoTracoDaComposicao.SETA,
        )
        concluirTracoNaPeca(peca, traco)
        invalidate()
    }

    private fun concluirTracoNaPeca(peca: Peca, traco: Traco) {
        if (peca is Peca.Imagem) {
            val original = peca.bitmap
            val estadoAntes = capturar(peca)
            // Pintura que passa da borda: a camada cresce antes (borracha fora da imagem não tem o que apagar).
            val cresceu = !traco.borracha && !peca.alfaBloqueado && crescerImagemParaOTraco(peca, traco)
            val anterior = gravarTracoNaImagem(peca, traco)
            if (anterior != null) {
                val paraDesfazer = if (cresceu) {
                    if (anterior !== original && !anterior.isRecycled) anterior.recycle()
                    original
                } else {
                    anterior
                }
                empilhar(Passo.Risco(traco, paraDesfazer, peca.id, if (cresceu) estadoAntes else null))
                camadaSuja = true
                aoMudarSelecao?.invoke()
                return
            }
            if (cresceu) {
                val crescida = peca.bitmap
                peca.bitmap = original
                restaurar(peca, estadoAntes)
                if (crescida !== original && !crescida.isRecycled) crescida.recycle()
            }
        }
        peca.tracos += traco
        // A tinta já recebeu o traço aos poucos (aplicarTrechoAtualNaTinta, inclusive no soltar).
        // Marcar suja redesenhava TODOS os traços da camada a cada traço novo: com muito rabisco,
        // o soltar da caneta ficava cada vez mais lento.
        val tintaJaTemOTraco = when {
            traco.pincel != null -> {
                // O traço em andamento estava na camada própria: desenha o final (com afinamento do fim) uma vez.
                val tinta = peca.tinta
                if (!peca.tintaSuja && tinta != null && !tinta.isRecycled && tracoAtual === traco) {
                    desenharTracoDoMotor(Canvas(tinta), traco, tinta.width.toFloat(), tinta.height.toFloat(), concluido = true)
                    true
                } else {
                    false
                }
            }
            // Em andamento a borracha parcial sai em trechos: refazer a camada deixa o apagado uniforme.
            borrachaParcial(traco) -> false
            else -> !peca.tintaSuja && peca.tinta?.isRecycled == false &&
                tracoAtual === traco && pontosJaNaTinta >= traco.pontos.size
        }
        if (!tintaJaTemOTraco) peca.tintaSuja = true
        empilhar(Passo.Risco(traco, null, -1))
        camadaSuja = true
    }

    private fun pontoDoToque(event: MotionEvent, ponteiro: Int): Ponto? {
        val bruto = paraAComposicao(event.getX(ponteiro), event.getY(ponteiro)) ?: return null
        val normal = TracoNaCamadaDaComposicao.pontoNormalizado(
            0f,
            0f,
            molduraLargura.toFloat(),
            molduraAltura.toFloat(),
            0f,
            bruto[0],
            bruto[1],
        ) ?: return null
        if (!TracoNaCamadaDaComposicao.cabeNoRetangulo(normal.x, normal.y)) return null
        val largura = larguraPelaPressao(event, ponteiro, null, false, PontaDoTracoDaComposicao.SETA) /
            molduraLargura.toFloat().coerceAtLeast(1f)
        return Ponto(normal.x, normal.y, largura)
    }

    private fun desenharPreviaDaSeta(tela: Canvas) {
        val origem = origemDaSeta ?: return
        val ox = origem.x * molduraLargura
        val oy = origem.y * molduraAltura
        val largura = origem.largura * molduraLargura
        pincel.xfermode = null
        pincel.color = PaletaDoEditorDeImagem.comOpacidade(corDoTraco, false)
        pincel.strokeCap = Paint.Cap.ROUND
        pincel.strokeJoin = Paint.Join.ROUND
        pincel.strokeWidth = largura
        val destino = previaDaSeta
        if (destino == null) {
            pincel.style = Paint.Style.FILL
            tela.drawCircle(ox, oy, largura / 2f, pincel)
            pincel.style = Paint.Style.STROKE
            return
        }
        val dx = destino.x * molduraLargura
        val dy = destino.y * molduraAltura
        val flecha = SetaSobreAFolha.flechaDoArraste(ox, oy, dx, dy, largura)
        if (flecha == null) {
            pincel.style = Paint.Style.FILL
            tela.drawCircle(ox, oy, largura / 2f, pincel)
            pincel.style = Paint.Style.STROKE
            return
        }
        pincel.style = Paint.Style.STROKE
        tela.drawLine(flecha.caudaX, flecha.caudaY, flecha.pontaX, flecha.pontaY, pincel)
        val asas = SetaSobreAFolha.asas(flecha.caudaX, flecha.caudaY, flecha.pontaX, flecha.pontaY, largura) ?: return
        tela.drawLine(flecha.pontaX, flecha.pontaY, asas.esquerdaX, asas.esquerdaY, pincel)
        tela.drawLine(flecha.pontaX, flecha.pontaY, asas.direitaX, asas.direitaY, pincel)
    }

    private fun tocarComDedos(event: MotionEvent): Boolean {
        if (
            modoDoDedo == ModoDoDedo.IGNORAR ||
            ToqueAposMoverComMolduraDeTransformarDaPeca.deveConsumirToqueEnquantoAguardaSoltarAposArmar(
                aguardandoSoltarAposArmarTransformar,
            )
        ) {
            if (
                event.actionMasked == MotionEvent.ACTION_UP ||
                event.actionMasked == MotionEvent.ACTION_CANCEL
            ) {
                modoDoDedo = ModoDoDedo.NENHUM
                aguardandoSoltarAposArmarTransformar = false
            }
            return true
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                toqueX = event.x
                toqueY = event.y
                ultimoArrasteX = event.x
                ultimoArrasteY = event.y
                if (encostarNaRegua(event.x, event.y)) {
                    Unit
                } else if (
                    ToqueAposMoverComMolduraDeTransformarDaPeca.prendeCanetaEDedoNoGestoDaPeca(
                        pecaEmModoTransformar = pecaEmModoTransformar,
                        selecaoAtivaDeRecorte = selecaoAtivaDaFerramentaDeRecorte(),
                    )
                ) {
                    prepararDedo(event.x, event.y)
                } else if (
                    ToqueComSelecaoAtivaDoRecorteNoEditorDeImagem.priorizaSelecaoSobrePeca(
                        ferramentaDeRecorte = ferramenta == Ferramenta.RECORTE,
                        selecaoAtiva = selecaoAtivaDaFerramentaDeRecorte(),
                    )
                ) {
                    if (
                        formaDaSelecao == FormaDaSelecaoSobreAImagem.LACO &&
                        lacoFechado
                    ) {
                        prepararGestoDaSelecaoAtivaDoLaco(event.x, event.y)
                    } else {
                        modoDoDedo = ModoDoDedo.RECORTE
                        tocarRecorte(event, event.getPointerId(0))
                    }
                } else if (ferramenta == Ferramenta.RECORTE && dedoDesenhaASelecao()) {
                    modoDoDedo = ModoDoDedo.RECORTE
                    tocarRecorte(event, event.getPointerId(0))
                } else {
                    prepararDedo(event.x, event.y)
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount >= 2 && modoDoDedo != ModoDoDedo.IGNORAR) {
                    if (modoDaPeca == ModoDaPeca.MOVER || modoDaPeca == ModoDaPeca.REDIMENSIONAR || modoDaPeca == ModoDaPeca.GIRAR) confirmarGesto()
                    else cancelarGestoDaPeca()
                    arraste = null
                    origemX0 = event.getX(0)
                    origemY0 = event.getY(0)
                    origemX1 = event.getX(1)
                    origemY1 = event.getY(1)
                    inicioDoToque = event.eventTime
                    distanciaDaPinca = distanciaEntre(event).coerceAtLeast(1f)
                    zoomNoInicioDaPinca = zoom
                    focoDaPincaX = (origemX0 + origemX1) / 2f
                    focoDaPincaY = (origemY0 + origemY1) / 2f
                    iniciarRotacaoDaPinca(event)
                    modoDoDedo = ModoDoDedo.TOQUE
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (modoDoDedo == ModoDoDedo.TOQUE && event.pointerCount >= 2 && afastouDoToque(event)) {
                    modoDoDedo = ModoDoDedo.PINCA
                    distanciaDaPinca = distanciaEntre(event).coerceAtLeast(1f)
                    zoomNoInicioDaPinca = zoom
                    focoDaPincaX = (event.getX(0) + event.getX(1)) / 2f
                    focoDaPincaY = (event.getY(0) + event.getY(1)) / 2f
                    iniciarRotacaoDaPinca(event)
                }
                atualizarGestoDaPeca(event)
                atualizarGestoDaSelecaoAtivaDoLaco(event)
                when (modoDoDedo) {
                    ModoDoDedo.PINCA -> if (event.pointerCount >= 2) aplicarPinca(event)
                    ModoDoDedo.ARRASTE -> if (modoDaPeca == ModoDaPeca.NENHUM || modoDaPeca == ModoDaPeca.AGUARDANDO_SELECAO) {
                        arrastarEnquadramento(event.x - ultimoArrasteX, event.y - ultimoArrasteY)
                        ultimoArrasteX = event.x
                        ultimoArrasteY = event.y
                    }
                    ModoDoDedo.RECORTE -> tocarRecorte(event, event.getPointerId(0))
                    ModoDoDedo.REGUA -> atualizarRegua(event)
                    else -> Unit
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                // Só com 2 dedos: 3 dedos é o refazer (observarGestosComDedos).
                val toqueCurto = modoDoDedo == ModoDoDedo.TOQUE &&
                    event.pointerCount >= 2 &&
                    maiorQuantidadeDeDedos == 2 &&
                    event.eventTime - inicioDoToque <= 320L &&
                    !afastouDoToque(event)
                if (toqueCurto) {
                    desfazer()
                    aoGestoDesfazer?.invoke()
                }
                modoDoDedo = ModoDoDedo.IGNORAR
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    when (modoDaPeca) {
                        ModoDaPeca.AGUARDANDO_SELECAO -> {
                            selecionadoId = idDoGesto
                            val peca = objetos.find { it.id == idDoGesto }
                            if (peca is Peca.Imagem && ferramenta == Ferramenta.RECORTE) {
                                idDaImagemDoRecorte = peca.id
                                if (selecaoInteligenteArmada) {
                                    selecaoInteligenteArmada = false
                                    prepararRecorteInicial(peca)
                                    aoMudarModoDoDedo?.invoke()
                                }
                            }
                            aoMudarSelecao?.invoke()
                        }
                        ModoDaPeca.MOVER, ModoDaPeca.REDIMENSIONAR, ModoDaPeca.GIRAR -> confirmarGesto()
                        else -> Unit
                    }
                } else {
                    cancelarGestoDaPeca()
                }
                if (modoDoDedo == ModoDoDedo.RECORTE) tocarRecorte(event, event.getPointerId(0))
                modoDaPeca = ModoDaPeca.NENHUM
                modoDaMascaraDoLaco = ModoDaMascaraDoLaco.NENHUM
                modoDoDedo = ModoDoDedo.NENHUM
                parteDaRegua = ParteDaReguaSobreAFolha.NENHUMA
                arraste = null
                invalidate()
            }
        }
        return true
    }

    private fun encostarNaRegua(xVista: Float, yVista: Float): Boolean {
        val atual = regua ?: return false
        val (x, y) = paraATelaReal(xVista, yVista)
        when (GestoDaReguaSobreAFolha.parte(atual, x, y)) {
            ParteDaReguaSobreAFolha.INTERRUPTOR -> {
                regua = GestoDaReguaSobreAFolha.alternar(atual)
                modoDoDedo = ModoDoDedo.IGNORAR
                invalidate()
                return true
            }
            ParteDaReguaSobreAFolha.GIRO -> {
                parteDaRegua = ParteDaReguaSobreAFolha.GIRO
                ultimoAnguloDoDedo = atual.anguloGraus
                modoDoDedo = ModoDoDedo.REGUA
                return true
            }
            ParteDaReguaSobreAFolha.CORPO -> {
                parteDaRegua = ParteDaReguaSobreAFolha.CORPO
                origemDoGestoX = x
                origemDoGestoY = y
                modoDoDedo = ModoDoDedo.REGUA
                return true
            }
            ParteDaReguaSobreAFolha.NENHUMA -> return false
        }
    }

    private fun dedoDesenhaASelecao(): Boolean {
        if (formaDaSelecao != FormaDaSelecaoSobreAImagem.LACO) return true
        return LacoLivreDaSelecaoSobreAImagemDaComposicao.segueOToque(ehCaneta = false, comODedo = usarDedoNoLaco)
    }

    private fun toqueNaPecaSelecionadaOuChrome(xTela: Float, yTela: Float): Boolean {
        if (selecionadoId == -1) return false
        if (toqueNoChrome(xTela, yTela) != ToqueNoChrome.NENHUM) return true
        if (cantoDaSelecao(xTela, yTela) != null) return true
        val ponto = paraAComposicao(xTela, yTela) ?: return false
        val peca = objetos.find { it.id == selecionadoId } ?: return false
        if (peca is Peca.Folha) return false
        return ToqueNoCorpoDaPecaSelecionadaDaComposicao.acertaCorpo(
            idSelecionado = selecionadoId,
            pontoDentroDoRetanguloDaSelecionada = pontoDentroDaPecaComFolga(peca, ponto[0], ponto[1]),
        )
    }

    private fun pontoDentroDaPecaComFolga(peca: Peca, x: Float, y: Float): Boolean {
        val folga = 24f
        val local = GiroDaPecaDaComposicao.pontoLocal(
            peca.angulo,
            x,
            y,
            peca.esquerda + peca.largura / 2f,
            peca.topo + peca.altura / 2f,
        )
        return local.x in (peca.esquerda - folga)..(peca.esquerda + peca.largura + folga) &&
            local.y in (peca.topo - folga)..(peca.topo + peca.altura + folga)
    }

    private fun prepararDedo(x: Float, y: Float) {
        if (encostarNaRegua(x, y)) return
        if (modoColarDaSelecao && prepararDedoNoModoColar(x, y)) return
        if (
            ToqueAposMoverComMolduraDeTransformarDaPeca.prendeCanetaEDedoNoGestoDaPeca(
                pecaEmModoTransformar = pecaEmModoTransformar,
                selecaoAtivaDeRecorte = selecaoAtivaDaFerramentaDeRecorte(),
            )
        ) {
            val noAlvo = toqueNaPecaSelecionadaOuChrome(x, y)
            when (
                ToqueAposMoverComMolduraDeTransformarDaPeca.destino(
                    pecaEmModoTransformar = true,
                    toqueNaPecaOuChrome = noAlvo,
                    aguardandoSoltarAposArmarTransformar = aguardandoSoltarAposArmarTransformar,
                )
            ) {
                DestinoDoToqueAposMoverComMolduraDeTransformar.ENCERRAR_MOLDURA -> {
                    limparSelecaoDaPeca()
                    modoDoDedo = ModoDoDedo.IGNORAR
                    return
                }
                DestinoDoToqueAposMoverComMolduraDeTransformar.GESTO_DA_PECA -> {
                    if (consumirToqueDeEspelho(x, y)) return
                    when (toqueNoChrome(x, y)) {
                        ToqueNoChrome.GIRO -> {
                            val ponto = paraAComposicao(x, y) ?: floatArrayOf(0f, 0f)
                            prepararPeca(ModoDaPeca.GIRAR, null, ponto, selecionadoId)
                            return
                        }
                        ToqueNoChrome.ESPELHO_HORIZONTAL, ToqueNoChrome.ESPELHO_VERTICAL -> Unit
            ToqueNoChrome.NENHUM -> Unit
                    }
                    val ponto = paraAComposicao(x, y) ?: return
                    val canto = cantoDaSelecao(x, y)
                    if (canto != null) {
                        prepararPeca(ModoDaPeca.REDIMENSIONAR, canto, ponto, selecionadoId)
                    } else {
                        prepararPeca(ModoDaPeca.AGUARDANDO, null, ponto, selecionadoId)
                    }
                    return
                }
                DestinoDoToqueAposMoverComMolduraDeTransformar.FLUXO_NORMAL -> {
                    // Chip Mover ainda pressionado: não cair no limparSelecaoDaPeca abaixo.
                    if (pecaEmModoTransformar) {
                        modoDoDedo = ModoDoDedo.IGNORAR
                        return
                    }
                }
            }
        }
        if (consumirToqueDeEspelho(x, y)) return
        when (toqueNoChrome(x, y)) {
            ToqueNoChrome.GIRO -> {
                if (selecionadoId == -1) {
                    modoDoDedo = ModoDoDedo.ARRASTE
                    return
                }
                val ponto = paraAComposicao(x, y) ?: floatArrayOf(0f, 0f)
                prepararPeca(ModoDaPeca.GIRAR, null, ponto, selecionadoId)
                return
            }
            ToqueNoChrome.ESPELHO_HORIZONTAL, ToqueNoChrome.ESPELHO_VERTICAL -> Unit
            ToqueNoChrome.NENHUM -> Unit
        }
        val ponto = paraAComposicao(x, y) ?: return
        val canto = cantoDaSelecao(x, y)
        val peca = pecaEm(ponto[0], ponto[1])
        when {
            canto != null && selecionadoId != -1 -> prepararPeca(ModoDaPeca.REDIMENSIONAR, canto, ponto, selecionadoId)
            ToqueParaSelecionarPecaNoEditor.podeMoverPecaTocada(peca?.id, selecionadoId) &&
                ToqueComDedoSobreLacoFechado.podeMoverPeca(lacoFechado) ->
                prepararPeca(ModoDaPeca.AGUARDANDO, null, ponto, peca!!.id)
            peca != null && ToqueParaSelecionarPecaNoEditor.podeSelecionarPeloToque(selecaoInteligenteArmada) -> {
                idDoGesto = peca.id
                modoDaPeca = ModoDaPeca.AGUARDANDO_SELECAO
                modoDoDedo = ModoDoDedo.ARRASTE
            }
            peca == null && selecionadoId != -1 -> {
                limparSelecaoDaPeca()
                modoDoDedo = ModoDoDedo.IGNORAR
            }
            else -> modoDoDedo = ModoDoDedo.ARRASTE
        }
    }

    private fun prepararPeca(modo: ModoDaPeca, canto: CantoDaComposicao?, ponto: FloatArray, id: Int) {
        val peca = objetos.find { it.id == id } ?: return
        if (peca is Peca.Imagem) fundirTracosNaImagem(peca)
        modoDaPeca = modo
        modoDoDedo = ModoDoDedo.PECA
        idDoGesto = id
        cantoDoGesto = canto
        origemDoGestoX = ponto[0]
        origemDoGestoY = ponto[1]
        estadoDoGesto = capturar(peca)
    }

    private fun atualizarGestoDaPeca(event: MotionEvent) {
        val passou = hypot(event.x - toqueX, event.y - toqueY) > folgaDoArraste
        if (modoDaPeca == ModoDaPeca.AGUARDANDO && passou) modoDaPeca = ModoDaPeca.MOVER
        if (modoDaPeca == ModoDaPeca.AGUARDANDO_SELECAO && passou) modoDaPeca = ModoDaPeca.NENHUM
        if (modoDaPeca != ModoDaPeca.MOVER && modoDaPeca != ModoDaPeca.REDIMENSIONAR && modoDaPeca != ModoDaPeca.GIRAR) return
        val ponto = paraAComposicao(event.x, event.y) ?: return
        val peca = objetos.find { it.id == idDoGesto } ?: return
        val estado = estadoDoGesto ?: return
        val centroX = estado.esquerda + estado.largura / 2f
        val centroY = estado.topo + estado.altura / 2f
        if (modoDaPeca == ModoDaPeca.GIRAR) {
            peca.angulo = GiroDaPecaDaComposicao.angulo(centroX, centroY, ponto[0], ponto[1])
        } else if (modoDaPeca == ModoDaPeca.MOVER) {
            restaurar(peca, estado)
            peca.esquerda += ponto[0] - origemDoGestoX
            peca.topo += ponto[1] - origemDoGestoY
            limitar(peca)
        } else {
            val canto = cantoDoGesto ?: return
            val local = GiroDaPecaDaComposicao.pontoLocal(estado.angulo, ponto[0], ponto[1], centroX, centroY)
            val novo = redimensionarMantendoProporcao(
                RetanguloDaComposicao(estado.esquerda, estado.topo, estado.largura, estado.altura),
                canto,
                local.x,
                local.y,
            )
            if (peca is Peca.Texto) {
                peca.tamanho = (estado.tamanho * (novo.largura / estado.largura.coerceAtLeast(1f))).coerceIn(18f, 800f)
                medir(peca)
                posicionarPelaAncora(peca, estado, canto)
            } else {
                peca.esquerda = novo.esquerda
                peca.topo = novo.topo
                peca.largura = novo.largura
                peca.altura = novo.altura
            }
            peca.angulo = estado.angulo
            limitar(peca)
        }
        invalidate()
    }

    private fun posicionarPelaAncora(peca: Peca, estado: EstadoDaPeca, canto: CantoDaComposicao) {
        val ancoraNaDireita = canto == CantoDaComposicao.NOROESTE || canto == CantoDaComposicao.SUDOESTE
        val ancoraEmbaixo = canto == CantoDaComposicao.NOROESTE || canto == CantoDaComposicao.NORDESTE
        val ancoraX = if (ancoraNaDireita) estado.esquerda + estado.largura else estado.esquerda
        val ancoraY = if (ancoraEmbaixo) estado.topo + estado.altura else estado.topo
        peca.esquerda = if (ancoraNaDireita) ancoraX - peca.largura else ancoraX
        peca.topo = if (ancoraEmbaixo) ancoraY - peca.altura else ancoraY
    }

    private fun confirmarGesto() {
        val peca = objetos.find { it.id == idDoGesto }
        val estado = estadoDoGesto
        if (peca != null && estado != null && mudou(peca, estado)) {
            empilharTransformacaoOuFundirComAAnterior(peca.id, estado)
        }
        modoDaPeca = ModoDaPeca.NENHUM
        estadoDoGesto = null
    }

    /**
     * Arrastes seguidos da mesma peça (ex.: Mover) viram um único passo:
     * o estado anterior permanece o do primeiro gesto, para um desfazer voltar ao início.
     */
    private fun empilharTransformacaoOuFundirComAAnterior(id: Int, estado: EstadoDaPeca) {
        if (
            !ModoColarDaSelecaoCopiadaNaComposicao.gestosDaPreviaEntramNoHistorico(modoColarDaSelecao) &&
            id == idPecaPreviaDoColar
        ) {
            return
        }
        val topo = passos.lastOrNull()
        if (topo is Passo.Transformacao && topo.id == id) {
            limparDesfeitos()
            tamanhoDoPacoteDoMoverEmDesfeitos = 0
            return
        }
        empilhar(Passo.Transformacao(id, estado))
    }

    private fun cancelarGestoDaPeca() {
        val peca = objetos.find { it.id == idDoGesto }
        val estado = estadoDoGesto
        if (peca != null && estado != null &&
            (modoDaPeca == ModoDaPeca.MOVER || modoDaPeca == ModoDaPeca.REDIMENSIONAR || modoDaPeca == ModoDaPeca.GIRAR)
        ) {
            restaurar(peca, estado)
        }
        modoDaPeca = ModoDaPeca.NENHUM
        estadoDoGesto = null
    }

    private fun mudou(peca: Peca, estado: EstadoDaPeca): Boolean {
        return abs(peca.esquerda - estado.esquerda) > 0.5f ||
            abs(peca.topo - estado.topo) > 0.5f ||
            abs(peca.largura - estado.largura) > 0.5f ||
            abs(peca.altura - estado.altura) > 0.5f ||
            abs(peca.angulo - estado.angulo) > 0.5f ||
            peca.espelhoHorizontal != estado.espelhoHorizontal ||
            peca.espelhoVertical != estado.espelhoVertical ||
            (peca is Peca.Texto && abs(peca.tamanho - estado.tamanho) > 0.5f)
    }

    // ---- Rotação da vista (só a vista: imagem e histórico não mudam) ----

    /** Graus; 0 = reta. O desenho gira o canvas e o toque chega "desgirado" à lógica do editor. */
    private var anguloDaVista = 0f
    private val matrizInversaDaVista = android.graphics.Matrix()
    private var anguloNoInicioDaPinca = 0f
    private var ultimoAnguloDosDedosNaTela = 0f
    private var giroAcumuladoNaPinca = 0f
    private var baseDoGiroNaPinca = 0f
    private var rotacaoAtivaNaPinca = false
    private var vistaEncaixadaNoAnguloReto = true

    fun anguloDaVistaEmGraus(): Float = anguloDaVista

    /** Coordenada desgirada (a que a lógica usa) → posição real na tela. */
    private fun paraATelaReal(x: Float, y: Float): FloatArray {
        if (anguloDaVista == 0f) return floatArrayOf(x, y)
        return girarEmTornoDoCentro(x, y, anguloDaVista)
    }

    private fun girarEmTornoDoCentro(x: Float, y: Float, graus: Float): FloatArray {
        val cx = width / 2f
        val cy = height / 2f
        val rad = Math.toRadians(graus.toDouble())
        val c = kotlin.math.cos(rad).toFloat()
        val sn = kotlin.math.sin(rad).toFloat()
        val dx = x - cx
        val dy = y - cy
        return floatArrayOf(cx + dx * c - dy * sn, cy + dx * sn + dy * c)
    }

    /** Ângulo da reta entre os dois dedos na tela real = ângulo nas coordenadas desgiradas + giro da vista. */
    private fun anguloDosDedosNaTela(event: MotionEvent): Float =
        Math.toDegrees(
            kotlin.math.atan2((event.getY(1) - event.getY(0)).toDouble(), (event.getX(1) - event.getX(0)).toDouble()),
        ).toFloat() + anguloDaVista

    private fun iniciarRotacaoDaPinca(event: MotionEvent) {
        if (event.pointerCount < 2) return
        anguloNoInicioDaPinca = anguloDaVista
        ultimoAnguloDosDedosNaTela = anguloDosDedosNaTela(event)
        giroAcumuladoNaPinca = 0f
        baseDoGiroNaPinca = 0f
        rotacaoAtivaNaPinca = false
        vistaEncaixadaNoAnguloReto = RotacaoDaVistaDoEditorDeImagem.estaEncaixado(anguloDaVista)
    }

    private fun aplicarRotacaoDaPinca(event: MotionEvent) {
        if (event.pointerCount < 2) return
        val agora = anguloDosDedosNaTela(event)
        giroAcumuladoNaPinca += RotacaoDaVistaDoEditorDeImagem.diferenca(agora, ultimoAnguloDosDedosNaTela)
        ultimoAnguloDosDedosNaTela = agora
        if (!rotacaoAtivaNaPinca) {
            if (abs(giroAcumuladoNaPinca) < RotacaoDaVistaDoEditorDeImagem.LIMIAR_PARA_COMECAR_GRAUS) return
            rotacaoAtivaNaPinca = true
            baseDoGiroNaPinca = giroAcumuladoNaPinca // sem pulo: o giro conta a partir daqui
        }
        val novo = RotacaoDaVistaDoEditorDeImagem.comEncaixe(
            anguloNoInicioDaPinca + giroAcumuladoNaPinca - baseDoGiroNaPinca,
        )
        if (novo == anguloDaVista) return
        girarVistaEmTornoDe((event.getX(0) + event.getX(1)) / 2f, (event.getY(0) + event.getY(1)) / 2f, novo)
        val encaixada = RotacaoDaVistaDoEditorDeImagem.estaEncaixado(novo)
        if (encaixada && !vistaEncaixadaNoAnguloReto) {
            performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
        }
        vistaEncaixadaNoAnguloReto = encaixada
        aoMudarZoomDaVista?.invoke(zoom)
    }

    /**
     * Gira a vista mantendo parado o ponto da tela sob os dedos ([focoX],[focoY] em coordenadas desgiradas):
     * o mesmo ponto real, desgirado com o novo ângulo, vira o novo foco; o pan compensa a diferença.
     */
    private fun girarVistaEmTornoDe(focoX: Float, focoY: Float, novoAngulo: Float) {
        val real = paraATelaReal(focoX, focoY)
        anguloDaVista = novoAngulo
        val desgirado = girarEmTornoDoCentro(real[0], real[1], -novoAngulo)
        panX += desgirado[0] - focoX
        panY += desgirado[1] - focoY
        focoDaPincaX = desgirado[0]
        focoDaPincaY = desgirado[1]
        calcularDestino()
    }

    private fun aplicarPinca(event: MotionEvent) {
        val focoX = (event.getX(0) + event.getX(1)) / 2f
        val focoY = (event.getY(0) + event.getY(1)) / 2f
        panX += focoX - focoDaPincaX
        panY += focoY - focoDaPincaY
        focoDaPincaX = focoX
        focoDaPincaY = focoY
        calcularDestino()
        val novo = ZoomDaVistaDoEditorDeImagem.limitar(
            zoomNoInicioDaPinca * (distanciaEntre(event) / distanciaDaPinca),
        )
        definirZoom(novo, focoX, focoY)
        aplicarRotacaoDaPinca(event)
        invalidate()
    }

    private fun arrastarEnquadramento(dx: Float, dy: Float) {
        panX += dx
        panY += dy
        calcularDestino()
        invalidate()
    }

    fun restaurarZoomDaVista() {
        zoom = ZoomDaVistaDoEditorDeImagem.ENCAIXE
        anguloDaVista = 0f
        panX = 0f
        panY = 0f
        calcularDestino()
        invalidate()
        aoMudarZoomDaVista?.invoke(zoom)
    }

    private fun definirZoom(novo: Float, focoX: Float, focoY: Float) {
        val limitado = ZoomDaVistaDoEditorDeImagem.limitar(novo)
        val aplicado = limitado / zoom.coerceAtLeast(0.01f)
        val esquerda = destino.left
        val topo = destino.top
        zoom = limitado
        val escalaBase = min(width.toFloat() / molduraLargura, height.toFloat() / molduraAltura)
        val largura = molduraLargura * escalaBase * zoom
        val altura = molduraAltura * escalaBase * zoom
        panX = focoX - (focoX - esquerda) * aplicado - (width - largura) / 2f
        panY = focoY - (focoY - topo) * aplicado - (height - altura) / 2f
        calcularDestino()
        aoMudarZoomDaVista?.invoke(zoom)
    }

    private fun afastouDoToque(event: MotionEvent): Boolean {
        if (event.pointerCount < 2) return true
        return hypot(event.getX(0) - origemX0, event.getY(0) - origemY0) > folgaDoToqueComDoisDedos ||
            hypot(event.getX(1) - origemX1, event.getY(1) - origemY1) > folgaDoToqueComDoisDedos
    }

    private fun distanciaEntre(event: MotionEvent): Float {
        return hypot(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))
    }

    private fun indiceDaCaneta(event: MotionEvent): Int {
        for (indice in 0 until event.pointerCount) {
            val tipo = event.getToolType(indice)
            if (tipo == MotionEvent.TOOL_TYPE_STYLUS || tipo == MotionEvent.TOOL_TYPE_ERASER) return indice
        }
        return -1
    }

    /**
     * Samsung Notes: botão lateral pressionado → ponta apaga;
     * ao soltar → volta ao último pincel da barra (se a borracha da UI não estiver escolhida).
     */
    private fun atualizarBotaoLateralDaCanetaSPen(event: MotionEvent) {
        val estado = BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.botaoLateralPressionado(
            event.buttonState,
        )
        val estava = botaoLateralDaCanetaPressionado
        when (event.actionMasked) {
            MotionEvent.ACTION_BUTTON_PRESS -> {
                val acao = BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.botaoLateralPressionado(
                    event.actionButton,
                )
                if (acao || estado) botaoLateralDaCanetaPressionado = true
            }
            MotionEvent.ACTION_BUTTON_RELEASE -> {
                botaoLateralDaCanetaPressionado = estado
            }
            else -> botaoLateralDaCanetaPressionado = estado
        }
        if (
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.soltouBotaoLateral(
                estavaPressionado = estava,
                estaPressionado = botaoLateralDaCanetaPressionado,
            )
        ) {
            restaurarUltimoPincelAposSoltarBotaoLateralDaCaneta()
        }
    }

    private fun restaurarUltimoPincelAposSoltarBotaoLateralDaCaneta() {
        val id = BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.pincelAposSoltarBotao(
            ferramentaDaBarraEhBorracha = ferramenta == Ferramenta.BORRACHA,
            idDoUltimoPincel = ultimaFerramentaDePincel.name,
        ) ?: return
        // Troca na hora (mesmo no meio do traço de borracha): o traço atual já nasceu
        // como borracha; o próximo toque precisa ser pincel, não voltar ao laço.
        val alvo = when (id) {
            Ferramenta.BRANCO.name -> Ferramenta.BRANCO
            else -> Ferramenta.VERMELHO
        }
        if (ferramenta != alvo) {
            if (ferramenta == Ferramenta.RECORTE) {
                limparLaco()
                limparSelecaoDoRetangulo()
                idDaImagemDoRecorte = -1
                selecaoInteligenteArmada = false
                aoMudarSelecaoAtivaDoLaco?.invoke()
            }
            if (pecaEmModoTransformar) limparSelecaoDaPeca()
            ferramenta = alvo
            if (alvo == Ferramenta.VERMELHO || alvo == Ferramenta.BRANCO) {
                ultimaFerramentaDePincel = alvo
            }
            aoMudarFerramenta?.invoke(alvo)
        }
        if (pontaDoTraco == PontaDoTracoDaComposicao.SETA) return
        if (pontaDoTraco != ultimaPontaDePincel) {
            pontaDoTraco = ultimaPontaDePincel
        }
        invalidate()
    }

    private fun indiceDoId(event: MotionEvent, id: Int): Int {
        for (indice in 0 until event.pointerCount) {
            if (event.getPointerId(indice) == id) return indice
        }
        return -1
    }

    private fun acrescentarPonto(event: MotionEvent, ponteiro: Int, traco: Traco, historico: Int?) {
        val x = if (historico == null) event.getX(ponteiro) else event.getHistoricalX(ponteiro, historico)
        val y = if (historico == null) event.getY(ponteiro) else event.getHistoricalY(ponteiro, historico)
        val ponto = paraAComposicao(x, y) ?: return
        val projetado = if (!traco.borracha && regua?.armada == true) {
            if (ancoraDoTracoX == null || ancoraDoTracoY == null || anguloDoTracoNaRegua == null) {
                ancoraDoTracoX = ponto[0]
                ancoraDoTracoY = ponto[1]
                anguloDoTracoNaRegua = regua?.anguloGraus?.minus(anguloDaVista)
            }
            val ancoraX = ancoraDoTracoX
            val ancoraY = ancoraDoTracoY
            val angulo = anguloDoTracoNaRegua
            if (ancoraX != null && ancoraY != null && angulo != null) {
                GestoDaReguaSobreAFolha.pontoDoTraco(
                    armada = true,
                    borracha = false,
                    x = ponto[0],
                    y = ponto[1],
                    ancoraX = ancoraX,
                    ancoraY = ancoraY,
                    anguloGraus = angulo,
                )
            } else {
                null
            }
        } else {
            null
        }
        var px = projetado?.x ?: ponto[0]
        var py = projetado?.y ?: ponto[1]
        val streamline = if (projetado == null) traco.pincel?.streamline ?: 0f else 0f
        if (projetado == null && estabilizacaoDoTraco > 0f) {
            if (traco.pontos.isEmpty()) {
                suavizadoX = px
                suavizadoY = py
            } else if (finalizandoTraco) {
                // Ao levantar a caneta o traço termina no ponto puxado (sem risco reto até a caneta);
                // com StreamLine, ele ainda alcança o ponto da corda.
                if (streamline <= 0.001f) return
                px = suavizadoX
                py = suavizadoY
            } else {
                // Raio da corda em dp na tela, convertido para unidades da moldura (depende do zoom).
                val escalaDaTela = (destino.width() / molduraLargura.toFloat().coerceAtLeast(1f)).coerceAtLeast(0.01f)
                val raio = AjusteDoTracoDaCaneta.raioDaCordaEmDp(estabilizacaoDoTraco) *
                    resources.displayMetrics.density / escalaDaTela
                val puxado = AjusteDoTracoDaCaneta.puxarPelaCorda(suavizadoX, suavizadoY, px, py, raio) ?: return
                suavizadoX = puxado.first
                suavizadoY = puxado.second
                px = suavizadoX
                py = suavizadoY
            }
        }
        if (streamline > 0.001f) {
            val tempo = if (historico == null) event.eventTime else event.getHistoricalEventTime(historico)
            if (traco.pontos.isEmpty()) {
                streamlineX = px
                streamlineY = py
            } else if (finalizandoTraco) {
                // Alcança a ponta em curva: o fim do traço chega onde a caneta saiu.
                val escalaDaTela = (destino.width() / molduraLargura.toFloat().coerceAtLeast(1f)).coerceAtLeast(0.01f)
                val minimo = 0.6f * resources.displayMetrics.density / escalaDaTela
                StreamLineDoTraco.alcancar(streamlineX, streamlineY, px, py, streamline, minimo).forEach {
                    gravarPontoNoTraco(event, ponteiro, traco, historico, it[0], it[1])
                }
                streamlineX = px
                streamlineY = py
                return
            } else {
                val seguido = StreamLineDoTraco.seguir(
                    streamlineX, streamlineY, px, py, streamline, (tempo - tempoDoStreamline).toFloat(),
                )
                streamlineX = seguido[0]
                streamlineY = seguido[1]
            }
            tempoDoStreamline = tempo
            px = streamlineX
            py = streamlineY
        }
        gravarPontoNoTraco(event, ponteiro, traco, historico, px, py)
    }

    private fun gravarPontoNoTraco(event: MotionEvent, ponteiro: Int, traco: Traco, historico: Int?, px: Float, py: Float) {
        if (objetos.none { it.id == traco.idDaPeca }) return
        val normal = TracoNaCamadaDaComposicao.pontoNormalizado(
            0f,
            0f,
            molduraLargura.toFloat(),
            molduraAltura.toFloat(),
            0f,
            px,
            py,
        ) ?: return
        if (!TracoNaCamadaDaComposicao.cabeNoRetangulo(normal.x, normal.y)) return
        val pincelDoTraco = traco.pincel
        // Dinâmica de velocidade: traço rápido afina/clareia (calculada no ponto, como a pressão).
        val fatorDeVelocidadeNoTamanho: Float
        val fatorDeVelocidadeNaOpacidade: Float
        if (pincelDoTraco != null && pincelDoTraco.temDinamicaDeVelocidade) {
            val velocidade = velocidadeDoPonto(event, ponteiro, historico, inicioDoTraco = traco.pontos.isEmpty())
            fatorDeVelocidadeNoTamanho = MotorDeCarimbosDoPincel.fatorDaVelocidade(velocidade, pincelDoTraco.velocidadeNoTamanho)
            fatorDeVelocidadeNaOpacidade = MotorDeCarimbosDoPincel.fatorDaVelocidade(velocidade, pincelDoTraco.velocidadeNaOpacidade)
        } else {
            fatorDeVelocidadeNoTamanho = 1f
            fatorDeVelocidadeNaOpacidade = 1f
        }
        val largura = (
            if (pincelDoTraco != null) larguraDoMotor(event, ponteiro, historico, pincelDoTraco, traco.borracha) * fatorDeVelocidadeNoTamanho
            else larguraPelaPressao(event, ponteiro, historico, traco.borracha, traco.ponta)
            ) / molduraLargura.toFloat().coerceAtLeast(1f)
        val folga = 0.4f / molduraLargura.toFloat().coerceAtLeast(1f)
        val anterior = traco.pontos.lastOrNull()
        if (anterior != null &&
            abs(anterior.x - normal.x) < folga &&
            abs(anterior.y - normal.y) < folga &&
            abs(anterior.largura - largura) < folga
        ) {
            return
        }
        traco.pontos += Ponto(
            normal.x,
            normal.y,
            largura,
            when {
                pincelDoTraco != null -> (alphaDoMotor(event, ponteiro, historico, pincelDoTraco) * fatorDeVelocidadeNaOpacidade).toInt().coerceIn(1, 255)
                traco.borracha -> 255
                else -> alphaDoPonto(event, ponteiro, historico, traco.ponta)
            },
        )
    }

    private fun larguraPelaPressao(
        event: MotionEvent,
        ponteiro: Int,
        historico: Int?,
        borracha: Boolean,
        ponta: PontaDoTracoDaComposicao,
    ): Float {
        val multiplicador = if (borracha) 1f else GeometriaDaPontaDoTraco.multiplicadorDeLargura(ponta)
        val nominal = TamanhoDoPincelDoEditorDeImagem.diametroDp(
            if (borracha) tamanhoDaBorracha else tamanhoDoPincel,
            borracha,
        ) * resources.displayMetrics.density * multiplicador
        val base = EscalaDoPincelNaFolha.diametroNaFolha(nominal, escalaDeReferenciaDoPincel())
        val tipo = event.getToolType(ponteiro)
        val caneta = tipo == MotionEvent.TOOL_TYPE_STYLUS || tipo == MotionEvent.TOOL_TYPE_ERASER
        if (!caneta || borracha) return base
        val bruta = if (historico == null) event.getPressure(ponteiro) else event.getHistoricalPressure(ponteiro, historico)
        val pressao = curvaDePressao.aplicar(bruta)
        return base * PressaoDaPontaDoTraco.fatorDeLargura(ponta, pressao)
    }

    private fun alphaDoPonto(event: MotionEvent, ponteiro: Int, historico: Int?, ponta: PontaDoTracoDaComposicao): Int {
        val tipo = event.getToolType(ponteiro)
        val caneta = tipo == MotionEvent.TOOL_TYPE_STYLUS || tipo == MotionEvent.TOOL_TYPE_ERASER
        val pressao = if (!caneta) {
            0.45f
        } else if (historico == null) {
            curvaDePressao.aplicar(event.getPressure(ponteiro))
        } else {
            curvaDePressao.aplicar(event.getHistoricalPressure(ponteiro, historico))
        }
        return PressaoDaPontaDoTraco.alpha(ponta, pressao)
    }

    private fun tocarRecorte(event: MotionEvent, id: Int): Boolean {
        if (modoDoDedo == ModoDoDedo.IGNORAR) return true
        val indice = indiceDoId(event, id)
        if (indice < 0) {
            arraste = null
            return true
        }
        val bruto = paraAComposicao(event.getX(indice), event.getY(indice))
        if (
            (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) &&
            event.getPointerId(event.actionIndex) == id &&
            bruto != null &&
            tentarSelecaoInteligenteNoPonto(bruto[0], bruto[1])
        ) {
            arraste = null
            return true
        }
        if (formaDaSelecao == FormaDaSelecaoSobreAImagem.LACO) {
            if (lacoFechado) {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                        if (event.getPointerId(event.actionIndex) == id) {
                            val indice = indiceDoId(event, id)
                            if (indice >= 0) prepararGestoDaSelecaoAtivaDoLaco(event.getX(indice), event.getY(indice))
                        }
                    }
                    MotionEvent.ACTION_MOVE -> atualizarGestoDaSelecaoAtivaDoLaco(event)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                        modoDaMascaraDoLaco = ModoDaMascaraDoLaco.NENHUM
                        if (modoDoDedo == ModoDoDedo.SELECAO_LACO) modoDoDedo = ModoDoDedo.NENHUM
                    }
                }
                return true
            }
            return tocarLaco(event, id)
        }
        val imagem = imagemDoRecorte()
        if (imagem == null && !recortarDentroDaMoldura) return true
        if (
            (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) &&
            event.getPointerId(event.actionIndex) == id &&
            selecaoAtivaDoRetangulo()
        ) {
            val ix = indiceDoId(event, id)
            if (ix >= 0) {
                val chip = chipNoToque(event.getX(ix), event.getY(ix))
                if (chip != null) {
                    modoDoDedo = ModoDoDedo.IGNORAR
                    executarAcaoDaSelecaoAtiva(chip.acao)
                    arraste = null
                    return true
                }
            }
        }
        val ponto = bruto?.let { coords ->
            if (recortarDentroDaMoldura || imagem == null) {
                floatArrayOf(coords[0], coords[1])
            } else {
                val centroX = imagem.esquerda + imagem.largura / 2f
                val centroY = imagem.topo + imagem.altura / 2f
                val local = GiroDaPecaDaComposicao.pontoLocal(imagem.angulo, coords[0], coords[1], centroX, centroY)
                floatArrayOf(local.x, local.y)
            }
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.getPointerId(event.actionIndex) != id) return true
                arraste = if (ponto == null) {
                    null
                } else {
                    val alca = if (selecaoAtivaDoRetangulo()) alcaEm(ponto[0], ponto[1]) else null
                    when {
                        ToqueForaDaSelecaoAtivaDoRecorteDaComposicao.soCancelaSemIniciarNova(
                            selecaoAtiva = selecaoAtivaDoRetangulo(),
                            toqueNoInteriorOuChromeDaSelecao = alca != null,
                        ) -> {
                            cancelarSelecaoAtivaSemIniciarNova()
                            null
                        }
                        alca == null -> Arraste.Novo(ponto[0], ponto[1])
                        alca == TipoDeAlca.MOVER -> Arraste.Alca(alca, ponto[0] - recorte.left, ponto[1] - recorte.top)
                        else -> Arraste.Alca(alca, 0f, 0f)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val x = ponto?.get(0) ?: return true
                val y = ponto[1]
                when (val modo = arraste) {
                    is Arraste.Novo -> recorte.set(min(modo.x, x), min(modo.y, y), max(modo.x, x), max(modo.y, y))
                    is Arraste.Alca -> ajustar(modo.tipo, x, y)
                    null -> Unit
                }
                val preservaTamanho = (arraste as? Arraste.Alca)?.tipo == TipoDeAlca.MOVER
                limitarRecorte(imagem, preservarTamanho = preservaTamanho)
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.actionMasked == MotionEvent.ACTION_CANCEL || event.getPointerId(event.actionIndex) == id) {
                    val modo = arraste
                    arraste = null
                    when (modo) {
                        is Arraste.Novo -> registrarOrigemDoRetangulo(forcar = true)
                        is Arraste.Alca -> if (modo.tipo != TipoDeAlca.MOVER) {
                            registrarOrigemDoRetangulo(forcar = true)
                        } else {
                            registrarOrigemDoRetangulo(forcar = false)
                        }
                        null -> registrarOrigemDoRetangulo(forcar = false)
                    }
                    aoMudarSelecaoAtivaDoLaco?.invoke()
                    invalidate()
                }
            }
        }
        return true
    }

    private fun alcaEm(x: Float, y: Float): TipoDeAlca? {
        val escala = (destino.width() / molduraLargura.toFloat().coerceAtLeast(1f)).coerceAtLeast(0.01f)
        val alcance = alcanceDaAlca / escala
        val tipo = ToqueNasAlcasDoRetanguloDeRecorteDaComposicao.tipo(
            x = x,
            y = y,
            esquerda = recorte.left,
            topo = recorte.top,
            direita = recorte.right,
            baixo = recorte.bottom,
            alcance = alcance,
        ) ?: return null
        return when (tipo) {
            AlcaDoRetanguloDeRecorteDaComposicao.MOVER -> TipoDeAlca.MOVER
            AlcaDoRetanguloDeRecorteDaComposicao.NOROESTE -> TipoDeAlca.NOROESTE
            AlcaDoRetanguloDeRecorteDaComposicao.NORDESTE -> TipoDeAlca.NORDESTE
            AlcaDoRetanguloDeRecorteDaComposicao.SUDOESTE -> TipoDeAlca.SUDOESTE
            AlcaDoRetanguloDeRecorteDaComposicao.SUDESTE -> TipoDeAlca.SUDESTE
        }
    }

    private fun ajustar(tipo: TipoDeAlca, x: Float, y: Float) {
        val modo = arraste as? Arraste.Alca ?: return
        when (tipo) {
            TipoDeAlca.MOVER -> {
                val largura = recorte.width()
                val altura = recorte.height()
                recorte.set(
                    x - modo.deslocamentoX,
                    y - modo.deslocamentoY,
                    x - modo.deslocamentoX + largura,
                    y - modo.deslocamentoY + altura,
                )
            }
            TipoDeAlca.NOROESTE -> recorte.set(x, y, recorte.right, recorte.bottom)
            TipoDeAlca.NORDESTE -> recorte.set(recorte.left, y, x, recorte.bottom)
            TipoDeAlca.SUDOESTE -> recorte.set(x, recorte.top, recorte.right, y)
            TipoDeAlca.SUDESTE -> recorte.set(recorte.left, recorte.top, x, y)
        }
    }

    private fun limitarRecorte(imagem: Peca.Imagem?, preservarTamanho: Boolean) {
        val limitado = if (recortarDentroDaMoldura || imagem == null) {
            LimiteDoRetanguloDeRecorteDentroDaImagemDaComposicao.aplicar(
                esquerda = recorte.left,
                topo = recorte.top,
                direita = recorte.right,
                baixo = recorte.bottom,
                imagemEsquerda = 0f,
                imagemTopo = 0f,
                imagemLargura = molduraLargura.toFloat(),
                imagemAltura = molduraAltura.toFloat(),
                preservarTamanho = preservarTamanho,
                ladoMinimo = 32f,
            )
        } else {
            LimiteDoRetanguloDeRecorteDentroDaImagemDaComposicao.aplicar(
                esquerda = recorte.left,
                topo = recorte.top,
                direita = recorte.right,
                baixo = recorte.bottom,
                imagemEsquerda = imagem.esquerda,
                imagemTopo = imagem.topo,
                imagemLargura = imagem.largura,
                imagemAltura = imagem.altura,
                preservarTamanho = preservarTamanho,
                ladoMinimo = 32f,
            )
        }
        recorte.set(limitado.esquerda, limitado.topo, limitado.direita, limitado.baixo)
        encaixarRecorteNaGradeDePixels(imagem)
    }

    private fun desenharRecorte(canvas: Canvas) {
        if (formaDaSelecao == FormaDaSelecaoSobreAImagem.LACO) {
            desenharLaco(canvas)
            return
        }
        chipsDaSelecaoAtiva.clear()
        if (recorte.width() <= 0f || recorte.height() <= 0f) return
        val area = rectNaTela(recorte.left, recorte.top, recorte.width(), recorte.height())
        val ativa = selecaoAtivaDoRetangulo()
        if (recortarDentroDaMoldura) {
            val quadro = rectNaTela(0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat())
            canvas.save()
            canvas.clipRect(quadro)
            if (selecaoDoRetanguloInvertida) {
                canvas.drawRect(area, escurecer)
            } else {
                canvas.drawRect(quadro.left, quadro.top, quadro.right, area.top, escurecer)
                canvas.drawRect(quadro.left, area.bottom, quadro.right, quadro.bottom, escurecer)
                canvas.drawRect(quadro.left, area.top, area.left, area.bottom, escurecer)
                canvas.drawRect(area.right, area.top, quadro.right, area.bottom, escurecer)
            }
            canvas.restore()
            desenharContorno(canvas, area)
            if (ativa) {
                desenharAlcas(canvas, area)
                desenharBarraFlutuanteDaSelecaoAtiva(canvas, area)
            }
            return
        }
        val imagem = imagemDoRecorte() ?: return
        val foto = rectNaTela(imagem.esquerda, imagem.topo, imagem.largura, imagem.altura)
        canvas.save()
        canvas.rotate(imagem.angulo, foto.centerX(), foto.centerY())
        canvas.save()
        canvas.clipRect(foto)
        if (selecaoDoRetanguloInvertida) {
            canvas.drawRect(area, escurecer)
        } else {
            canvas.drawRect(foto.left, foto.top, foto.right, area.top, escurecer)
            canvas.drawRect(foto.left, area.bottom, foto.right, foto.bottom, escurecer)
            canvas.drawRect(foto.left, area.top, area.left, area.bottom, escurecer)
            canvas.drawRect(area.right, area.top, foto.right, area.bottom, escurecer)
        }
        canvas.restore()
        desenharContorno(canvas, area)
        if (ativa) desenharAlcas(canvas, area)
        canvas.restore()
        if (ativa) desenharBarraFlutuanteDaSelecaoAtiva(canvas, area)
    }

    private fun desenharSelecao(canvas: Canvas) {
        val peca = objetos.find { it.id == selecionadoId } ?: return
        if (!peca.visivel || peca is Peca.Folha) return
        val transformar = pecaEmModoTransformar
        val claro = if (transformar) contornoClaroDeTransformar else contornoClaro
        val miolo = if (transformar) alcaMioloDeTransformar else alcaMiolo
        val area = rectNaTela(peca.esquerda, peca.topo, peca.largura, peca.altura)
        canvas.save()
        canvas.rotate(peca.angulo, area.centerX(), area.centerY())
        desenharContorno(canvas, area, claro)
        desenharAlcas(canvas, area, miolo)
        desenharCaboDaPeca(canvas, area, claro, miolo)
        if (transformar && peca is Peca.Imagem) desenharBotoesDeEspelho(canvas, area)
        canvas.restore()
        if (modoColarDaSelecao && transformar) {
            desenharChipColarDaPrevia(canvas, area)
        } else if (!modoColarDaSelecao) {
            chipsDaSelecaoAtiva.removeAll { it.acao == AcaoDaSelecaoAtivaDoLaco.COLAR }
        }
    }

    private fun desenharChipColarDaPrevia(canvas: Canvas, bbox: RectF) {
        if (anguloDaVista != 0f) {
            val caixa = caixaNaTelaReal(bbox)
            desenhosEmPe += { tela -> desenharChipColarDaPreviaEmPe(tela, caixa) }
            return
        }
        desenharChipColarDaPreviaEmPe(canvas, bbox)
    }

    private fun desenharChipColarDaPreviaEmPe(canvas: Canvas, bbox: RectF) {
        chipsDaSelecaoAtiva.removeAll { it.acao == AcaoDaSelecaoAtivaDoLaco.COLAR }
        val densidade = resources.displayMetrics.density
        val texto = context.getString(R.string.colar_selecao_copiada_do_editor_de_imagem)
        tintaDoChipDaSelecao.textSize = 13f * densidade
        val paddingX = 14f * densidade
        val alturaBarra = 36f * densidade
        val folga = 12f * densidade
        val reservaDoCabo = 48f * densidade
        val largura = tintaDoChipDaSelecao.measureText(texto) + paddingX * 2f
        val lado = PosicaoDoChipColarDaPreviaTransformavelDaComposicao.decidir(
            bboxTopo = bbox.top,
            bboxBaixo = bbox.bottom,
            alturaVisivel = height.toFloat(),
            alturaChip = alturaBarra,
            folga = folga,
        )
        val extraAcima = PosicaoDoChipColarDaPreviaTransformavelDaComposicao.folgaExtraAcimaParaCaboDeGiro(
            lado = lado,
            reservaDoCabo = reservaDoCabo,
        )
        val x = (bbox.centerX() - largura / 2f).coerceIn(8f * densidade, width - largura - 8f * densidade)
        val y = when (lado) {
            LadoDaBarraFlutuanteDaSelecaoAtiva.ACIMA -> bbox.top - folga - alturaBarra - extraAcima
            LadoDaBarraFlutuanteDaSelecaoAtiva.ABAIXO -> bbox.bottom + folga
        }
        val area = RectF(x, y, x + largura, y + alturaBarra)
        canvas.drawRoundRect(area, 8f * densidade, 8f * densidade, fundoDoChipDaSelecao)
        val textoY = area.centerY() - (tintaDoChipDaSelecao.ascent() + tintaDoChipDaSelecao.descent()) / 2f
        canvas.drawText(texto, area.centerX(), textoY, tintaDoChipDaSelecao)
        chipsDaSelecaoAtiva += ChipDaSelecaoAtivaDoLaco(AcaoDaSelecaoAtivaDoLaco.COLAR, RectF(area))
    }

    private fun desenharBotoesDeEspelho(canvas: Canvas, area: RectF) {
        val densidade = resources.displayMetrics.density
        val botoes = EspelhoDaPecaDaComposicao.botoes(area.centerX(), area.bottom, densidade)
        botoesDeEspelho.clear()
        botoesDeEspelho.addAll(botoes)
        tintaDoChipDaSelecao.textSize = 13f * densidade
        botoes.forEach { botao ->
            val rect = RectF(botao.esquerda, botao.topo, botao.direita(), botao.topo + botao.altura)
            canvas.drawRoundRect(rect, 8f * densidade, 8f * densidade, fundoDoChipDaSelecao)
            val texto = context.getString(
                if (botao.eixo == EspelhoDaPecaDaComposicao.Eixo.HORIZONTAL) {
                    R.string.espelhar_horizontal_da_peca
                } else {
                    R.string.espelhar_vertical_da_peca
                },
            )
            val textoY = rect.centerY() - (tintaDoChipDaSelecao.ascent() + tintaDoChipDaSelecao.descent()) / 2f
            canvas.drawText(texto, rect.centerX(), textoY, tintaDoChipDaSelecao)
        }
    }

    private fun desenharCaboDaPeca(
        canvas: Canvas,
        area: RectF,
        linha: Paint = contornoClaro,
        miolo: Paint = alcaMiolo,
    ) {
        val densidade = resources.displayMetrics.density
        val y = area.top - 36f * densidade
        val centro = area.centerX()
        val raio = 11f * densidade
        canvas.drawLine(centro, area.top, centro, y + raio, linha)
        desenharBotaoDaPeca(canvas, centro, y, raio, miolo)
    }

    private fun desenharBotaoDaPeca(canvas: Canvas, x: Float, y: Float, raio: Float, miolo: Paint = alcaMiolo) {
        canvas.drawCircle(x, y, raio, alcaAnel)
        canvas.drawCircle(x, y, raio * 0.72f, miolo)
    }

    private fun desenharContorno(canvas: Canvas, area: android.graphics.RectF, claro: Paint = contornoClaro) {
        canvas.drawRect(area, contornoEscuro)
        canvas.drawRect(area, claro)
    }

    private fun desenharAlcas(canvas: Canvas, area: android.graphics.RectF, miolo: Paint = alcaMiolo) {
        val raio = 7f * resources.displayMetrics.density
        listOf(area.left to area.top, area.right to area.top, area.left to area.bottom, area.right to area.bottom)
            .forEach { (x, y) ->
                canvas.drawCircle(x, y, raio, alcaAnel)
                canvas.drawCircle(x, y, raio * 0.62f, miolo)
            }
    }

    private fun cantoDaSelecao(xTela: Float, yTela: Float): CantoDaComposicao? {
        val peca = objetos.find { it.id == selecionadoId } ?: return null
        val area = rectNaTela(peca.esquerda, peca.topo, peca.largura, peca.altura)
        val local = GiroDaPecaDaComposicao.pontoLocal(peca.angulo, xTela, yTela, area.centerX(), area.centerY())
        fun perto(px: Float, py: Float) = hypot(local.x - px, local.y - py) <= alcanceDaAlca
        return when {
            perto(area.left, area.top) -> CantoDaComposicao.NOROESTE
            perto(area.right, area.top) -> CantoDaComposicao.NORDESTE
            perto(area.left, area.bottom) -> CantoDaComposicao.SUDOESTE
            perto(area.right, area.bottom) -> CantoDaComposicao.SUDESTE
            else -> null
        }
    }

    private fun pecaEm(x: Float, y: Float): Peca? {
        return objetos.lastOrNull { peca ->
            if (!peca.visivel) return@lastOrNull false
            if (peca is Peca.Folha) return@lastOrNull false
            pontoDentroDaPecaComFolga(peca, x, y)
        }
    }

    private enum class ToqueNoChrome { NENHUM, GIRO, ESPELHO_HORIZONTAL, ESPELHO_VERTICAL }

    private fun toqueNoChrome(xTela: Float, yTela: Float): ToqueNoChrome {
        val peca = objetos.find { it.id == selecionadoId } ?: return ToqueNoChrome.NENHUM
        if (peca is Peca.Folha) return ToqueNoChrome.NENHUM
        val area = rectNaTela(peca.esquerda, peca.topo, peca.largura, peca.altura)
        val local = GiroDaPecaDaComposicao.pontoLocal(peca.angulo, xTela, yTela, area.centerX(), area.centerY())
        if (peca is Peca.Imagem && pecaEmModoTransformar) {
            when (EspelhoDaPecaDaComposicao.eixoNoPonto(botoesDeEspelho, local.x, local.y)) {
                EspelhoDaPecaDaComposicao.Eixo.HORIZONTAL -> return ToqueNoChrome.ESPELHO_HORIZONTAL
                EspelhoDaPecaDaComposicao.Eixo.VERTICAL -> return ToqueNoChrome.ESPELHO_VERTICAL
                null -> Unit
            }
        }
        val densidade = resources.displayMetrics.density
        val y = area.top - 36f * densidade
        val alcance = 16f * densidade
        fun perto(px: Float) = hypot(local.x - px, local.y - y) <= alcance
        val centro = area.centerX()
        return if (perto(centro)) ToqueNoChrome.GIRO else ToqueNoChrome.NENHUM
    }

    private fun consumirToqueDeEspelho(x: Float, y: Float): Boolean {
        val horizontal = when (toqueNoChrome(x, y)) {
            ToqueNoChrome.ESPELHO_HORIZONTAL -> true
            ToqueNoChrome.ESPELHO_VERTICAL -> false
            else -> return false
        }
        espelharCamada(selecionadoId, horizontal)
        modoDoDedo = ModoDoDedo.IGNORAR
        return true
    }

    /** Troca os pontos do traço em andamento pela forma limpa reconhecida (linha, círculo, retângulo). */
    private fun tentarFixarFormaLimpa() {
        val atual = tracoAtual ?: return
        if (formaFixadaNoTraco || atual.pontos.size < 3 || molduraLargura <= 0 || molduraAltura <= 0) return
        val ml = molduraLargura.toFloat()
        val ma = molduraAltura.toFloat()
        val forma = FormaLimpaNoFimDoTraco.reconhecer(
            atual.pontos.map { FormaLimpaNoFimDoTraco.Ponto(it.x * ml, it.y * ma) },
        ) ?: return
        // Só o trecho desenhado em movimento: parada e apertando, a caneta gera pontos gordos no mesmo lugar,
        // e a média com eles engordava o traço inteiro.
        val desenhados = atual.pontos.take(pontosAntesDaParada.coerceIn(2, atual.pontos.size))
        val largura = desenhados.map { it.largura }.average().toFloat()
        val alpha = desenhados.map { it.alpha }.average().toInt().coerceIn(0, 255)
        val novos = FormaLimpaNoFimDoTraco.pontosDa(forma).map { Ponto(it.x / ml, it.y / ma, largura, alpha) }
        atual.pontos.clear()
        atual.pontos.addAll(novos)
        formaFixadaNoTraco = true
        // A tinta já tinha o rabisco: refaz a camada e redesenha o traço desde o começo.
        objetos.find { it.id == atual.idDaPeca }?.tintaSuja = true
        pontosJaNaTinta = 0
        performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        aoFixarFormaLimpa?.invoke()
        invalidate()
    }

    private var toqueLongoPegouCor = false

    /**
     * Só observa (não consome): toque rápido com 2 dedos = desfazer, 3 = refazer;
     * 1 dedo parado ~0,5 s sobre a imagem = conta-gotas.
     */
    private fun observarGestosComDedos(event: MotionEvent) {
        val limiar = 18f * resources.displayMetrics.density
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                inicioDoToqueComDedosMs = event.eventTime
                maiorQuantidadeDeDedos = 1
                dedosMexeram = false
                toqueSoComDedos = event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER
                inicioDosDedos.clear()
                inicioDosDedos[event.getPointerId(0)] = android.graphics.PointF(event.x, event.y)
                cancelarToquesLongos()
                if (toqueSoComDedos && podePegarCorPeloToqueLongo()) {
                    pontoDoToqueLongoX = event.x
                    pontoDoToqueLongoY = event.y
                    postDelayed(contaGotasPeloToqueLongo, 500L)
                } else if (toqueSoComDedos && podeSelecionarTudoPeloToqueLongo()) {
                    pontoDoToqueLongoX = event.x
                    pontoDoToqueLongoY = event.y
                    postDelayed(selecionarTudoPeloToqueLongo, 500L)
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                cancelarToquesLongos()
                maiorQuantidadeDeDedos = maxOf(maiorQuantidadeDeDedos, event.pointerCount)
                val i = event.actionIndex
                if (event.getToolType(i) != MotionEvent.TOOL_TYPE_FINGER) toqueSoComDedos = false
                inicioDosDedos[event.getPointerId(i)] = android.graphics.PointF(event.getX(i), event.getY(i))
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val inicio = inicioDosDedos[event.getPointerId(i)] ?: continue
                    if (Math.hypot((event.getX(i) - inicio.x).toDouble(), (event.getY(i) - inicio.y).toDouble()) > limiar) {
                        dedosMexeram = true
                        cancelarToquesLongos()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                cancelarToquesLongos()
                val rapido = event.eventTime - inicioDoToqueComDedosMs < 400L
                if (toqueSoComDedos && !dedosMexeram && rapido) {
                    // 2 dedos já desfaz no gesto antigo (tocarComDedos); aqui só o refazer com 3.
                    if (maiorQuantidadeDeDedos == 3) post { aoGestoRefazer?.invoke() }
                }
            }
            MotionEvent.ACTION_CANCEL -> cancelarToquesLongos()
        }
    }

    private fun cancelarToquesLongos() {
        removeCallbacks(contaGotasPeloToqueLongo)
        removeCallbacks(selecionarTudoPeloToqueLongo)
    }

    private fun podeSelecionarTudoPeloToqueLongo(): Boolean =
        ferramenta == Ferramenta.RECORTE && !modoColarDaSelecao && !pecaEmModoTransformar && !lupaAtiva

    private fun selecionarTudoDaCamadaPeloToqueLongo() {
        if (!podeSelecionarTudoPeloToqueLongo() || dedosMexeram || maiorQuantidadeDeDedos > 1) return
        // Toque que caiu num botão da barra da seleção já foi tratado.
        if (modoDoDedo == ModoDoDedo.IGNORAR) return
        val ponto = paraAComposicao(pontoDoToqueLongoX, pontoDoToqueLongoY) ?: return
        if (ponto[0] !in 0f..molduraLargura.toFloat() || ponto[1] !in 0f..molduraAltura.toFloat()) return
        if (!selecionarTudoDaCamadaAtiva()) return
        modoDoDedo = ModoDoDedo.IGNORAR
        arraste = null
        performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
    }

    /**
     * Seleciona tudo da camada ativa com o retângulo: imagem = a camada inteira;
     * folha de desenho = a folha inteira (moldura), igual a arrastar a seleção à mão sobre ela.
     */
    fun selecionarTudoDaCamadaAtiva(): Boolean = selecionarCamadaInteira(idDaCamadaAtiva)

    /** Toque longo só pega cor quando o dedo não desenha nem está segurando uma peça/seleção. */
    private fun podePegarCorPeloToqueLongo(): Boolean =
        !desenharComODedo && ferramentaDeTraco() && tracoAtual == null && !pecaEmModoTransformar &&
            !modoColarDaSelecao && selecionadoId == -1

    private fun pegarCorPeloToqueLongo() {
        if (lupaAtiva) return
        if (!podePegarCorPeloToqueLongo() || dedosMexeram || maiorQuantidadeDeDedos > 1) return
        val ponto = paraAComposicao(pontoDoToqueLongoX, pontoDoToqueLongoY) ?: return
        if (ponto[0] !in 0f..molduraLargura.toFloat() || ponto[1] !in 0f..molduraAltura.toFloat()) return
        toqueLongoPegouCor = true
        modoDoDedo = ModoDoDedo.IGNORAR
        performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        abrirLupa(pontoDoToqueLongoX, pontoDoToqueLongoY)
    }

    // ---- Lupa do conta-gotas: pixels ampliados sob o dedo; soltar pega a cor do centro. ----

    private var lupaAtiva = false
    private var lupaX = 0f
    private var lupaY = 0f
    private val pixelsNaLupa = 15 // ímpar: o do meio é o que será pego
    private val pinturaDosPixelsDaLupa = Paint().apply { isFilterBitmap = false; isAntiAlias = false }
    private val pinturaDaGradeDaLupa = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.argb(45, 0, 0, 0)
        strokeWidth = 1f
    }
    private val pinturaDoAnelDaLupa = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val pinturaDoContornoDaLupa = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val pinturaDoFundoDaLupa = Paint().apply { color = Color.rgb(60, 60, 60) }
    private val pinturaDoRotuloDaLupa = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val pinturaDaPilulaDaLupa = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(200, 20, 20, 20) }
    private val caminhoDaLupa = Path()
    private val retanguloDaLupa = RectF()
    private val origemDaLupa = android.graphics.Rect()

    private fun abrirLupa(x: Float, y: Float) {
        lupaAtiva = true
        lupaX = x
        lupaY = y
        redesenharSoALupa()
    }

    private fun moverLupa(x: Float, y: Float) {
        if (!lupaAtiva) return
        lupaX = x
        lupaY = y
        redesenharSoALupa()
    }

    private fun soltarLupa(pegar: Boolean) {
        if (!lupaAtiva) return
        val cor = if (pegar) corSobALupa() else null
        lupaAtiva = false
        if (cor != null) {
            contaGotasArmado = false
            performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            aoPegarCor?.invoke(cor)
        }
        invalidate()
    }

    /** Só repinta a tela: não recompõe as camadas (o buffer 1:1 não mudou). */
    private fun redesenharSoALupa() {
        super.invalidate()
    }

    /** Pixel da composição sob o dedo, exatamente como aparece na tela; null fora da imagem. */
    private fun pixelSobALupa(): IntArray? {
        val ponto = paraAComposicao(lupaX, lupaY) ?: return null
        val px = kotlin.math.floor(ponto[0]).toInt()
        val py = kotlin.math.floor(ponto[1]).toInt()
        if (px !in 0 until molduraLargura || py !in 0 until molduraAltura) return null
        return intArrayOf(px, py)
    }

    private fun corSobALupa(): Int? {
        val (px, py) = pixelSobALupa() ?: return null
        val buffer = garantirBufferDaMolduraEmUmParaUm()
        return buffer.getPixel(px, py) or 0xFF000000.toInt()
    }

    private fun desenharLupaDoContaGotas(canvas: Canvas) {
        val d = resources.displayMetrics.density
        val raio = 56f * d
        val anel = 8f * d
        val folga = 8f * d
        val distanciaDoDedo = 104f * d
        val externo = raio + anel
        val dedo = paraATelaReal(lupaX, lupaY)
        var cx = dedo[0]
        var cy = dedo[1] - distanciaDoDedo
        if (cy - externo < folga) cy = dedo[1] + distanciaDoDedo // perto do topo: lupa vai pra baixo do dedo
        cx = cx.coerceIn(externo + folga, (width - externo - folga).coerceAtLeast(externo + folga))

        val pixel = pixelSobALupa()
        val corNova = corSobALupa()
        val corAnterior = corDoTraco or 0xFF000000.toInt()

        // Pixels ampliados (sem suavização), recortados em círculo.
        canvas.save()
        caminhoDaLupa.reset()
        caminhoDaLupa.addCircle(cx, cy, raio, Path.Direction.CW)
        canvas.clipPath(caminhoDaLupa)
        canvas.drawCircle(cx, cy, raio, pinturaDoFundoDaLupa)
        val n = pixelsNaLupa
        val meio = n / 2
        val celula = 2f * raio / n
        val esquerda0 = cx - raio
        val topo0 = cy - raio
        if (pixel != null) {
            val buffer = garantirBufferDaMolduraEmUmParaUm()
            val x0 = pixel[0] - meio
            val y0 = pixel[1] - meio
            val sx0 = x0.coerceAtLeast(0)
            val sy0 = y0.coerceAtLeast(0)
            val sx1 = (x0 + n).coerceAtMost(buffer.width)
            val sy1 = (y0 + n).coerceAtMost(buffer.height)
            if (sx0 < sx1 && sy0 < sy1) {
                origemDaLupa.set(sx0, sy0, sx1, sy1)
                retanguloDaLupa.set(
                    esquerda0 + (sx0 - x0) * celula,
                    topo0 + (sy0 - y0) * celula,
                    esquerda0 + (sx1 - x0) * celula,
                    topo0 + (sy1 - y0) * celula,
                )
                canvas.drawBitmap(buffer, origemDaLupa, retanguloDaLupa, pinturaDosPixelsDaLupa)
            }
        }
        for (i in 1 until n) {
            val x = esquerda0 + i * celula
            val y = topo0 + i * celula
            canvas.drawLine(x, topo0, x, topo0 + 2f * raio, pinturaDaGradeDaLupa)
            canvas.drawLine(esquerda0, y, esquerda0 + 2f * raio, y, pinturaDaGradeDaLupa)
        }
        // Quadradinho do pixel que será pego: preto por fora, branco por dentro (visível em qualquer cor).
        retanguloDaLupa.set(
            esquerda0 + meio * celula,
            topo0 + meio * celula,
            esquerda0 + (meio + 1) * celula,
            topo0 + (meio + 1) * celula,
        )
        pinturaDoContornoDaLupa.color = Color.BLACK
        pinturaDoContornoDaLupa.strokeWidth = 3f * d
        canvas.drawRect(retanguloDaLupa, pinturaDoContornoDaLupa)
        pinturaDoContornoDaLupa.color = Color.WHITE
        pinturaDoContornoDaLupa.strokeWidth = 1.5f * d
        canvas.drawRect(retanguloDaLupa, pinturaDoContornoDaLupa)
        canvas.restore()

        // Anel: metade de cima = cor sob o dedo; metade de baixo = cor atual do pincel.
        val raioDoAnel = raio + anel / 2f
        retanguloDaLupa.set(cx - raioDoAnel, cy - raioDoAnel, cx + raioDoAnel, cy + raioDoAnel)
        pinturaDoAnelDaLupa.strokeWidth = anel
        pinturaDoAnelDaLupa.color = corNova ?: corAnterior
        canvas.drawArc(retanguloDaLupa, 180f, 180f, false, pinturaDoAnelDaLupa)
        pinturaDoAnelDaLupa.color = corAnterior
        canvas.drawArc(retanguloDaLupa, 0f, 180f, false, pinturaDoAnelDaLupa)
        pinturaDoContornoDaLupa.strokeWidth = 1.5f * d
        pinturaDoContornoDaLupa.color = Color.argb(160, 0, 0, 0)
        canvas.drawCircle(cx, cy, externo, pinturaDoContornoDaLupa)
        pinturaDoContornoDaLupa.color = Color.argb(200, 255, 255, 255)
        canvas.drawCircle(cx, cy, raio, pinturaDoContornoDaLupa)

        // Código da cor numa pílula abaixo da lupa.
        if (corNova != null) {
            val texto = String.format("#%06X", corNova and 0xFFFFFF)
            pinturaDoRotuloDaLupa.textSize = 12f * resources.displayMetrics.scaledDensity
            val largura = pinturaDoRotuloDaLupa.measureText(texto) + 16f * d
            val altura = 22f * d
            val topo = cy + externo + 6f * d
            retanguloDaLupa.set(cx - largura / 2f, topo, cx + largura / 2f, topo + altura)
            canvas.drawRoundRect(retanguloDaLupa, altura / 2f, altura / 2f, pinturaDaPilulaDaLupa)
            val base = retanguloDaLupa.centerY() -
                (pinturaDoRotuloDaLupa.descent() + pinturaDoRotuloDaLupa.ascent()) / 2f
            canvas.drawText(texto, cx, base, pinturaDoRotuloDaLupa)
        }
    }

    private fun corVisivel(x: Float, y: Float): Int {
        for (indice in objetos.indices.reversed()) {
            val peca = objetos[indice]
            if (!peca.visivel) continue
            val u = x / molduraLargura.toFloat().coerceAtLeast(1f)
            val v = y / molduraAltura.toFloat().coerceAtLeast(1f)
            if (TracoNaCamadaDaComposicao.cabeNoRetangulo(u, v)) {
                corDoTraco(peca, u, v)?.let { return it }
            }
            val local = GiroDaPecaDaComposicao.pontoLocal(
                peca.angulo,
                x,
                y,
                peca.esquerda + peca.largura / 2f,
                peca.topo + peca.altura / 2f,
            )
            when (peca) {
                is Peca.Folha -> continue
                is Peca.Imagem -> {
                    if (peca.bitmap.isRecycled || peca.largura <= 0f || peca.altura <= 0f) continue
                    if (local.x !in peca.esquerda..(peca.esquerda + peca.largura)) continue
                    if (local.y !in peca.topo..(peca.topo + peca.altura)) continue
                    val u = ((local.x - peca.esquerda) / peca.largura * peca.bitmap.width).toInt()
                        .coerceIn(0, peca.bitmap.width - 1)
                    val v = ((local.y - peca.topo) / peca.altura * peca.bitmap.height).toInt()
                        .coerceIn(0, peca.bitmap.height - 1)
                    val pixel = peca.bitmap.getPixel(u, v)
                    if (Color.alpha(pixel) > 16) return pixel or 0xFF000000.toInt()
                }
                is Peca.Texto -> {
                    val dentro = local.x in peca.esquerda..(peca.esquerda + peca.largura) &&
                        local.y in peca.topo..(peca.topo + peca.altura)
                    if (dentro) return peca.cor or 0xFF000000.toInt()
                }
            }
        }
        return corDaFolha
    }

    private fun limitar(peca: Peca) {
        val margem = 48f
        val minimoX = margem - peca.largura
        val maximoX = molduraLargura - margem
        val minimoY = margem - peca.altura
        val maximoY = molduraAltura - margem
        peca.esquerda = peca.esquerda.coerceIn(min(minimoX, maximoX), max(minimoX, maximoX))
        peca.topo = peca.topo.coerceIn(min(minimoY, maximoY), max(minimoY, maximoY))
    }

    private fun medir(peca: Peca.Texto) {
        tintaDoTexto.textSize = peca.tamanho
        peca.largura = tintaDoTexto.measureText(peca.texto).coerceAtLeast(1f)
        val metricas = tintaDoTexto.fontMetrics
        peca.altura = (metricas.descent - metricas.ascent).coerceAtLeast(1f)
    }

    private fun desenharPeca(tela: Canvas, peca: Peca, usarCache: Boolean) {
        val camadaDaOpacidade = if (peca.opacidade < 0.999f) {
            tela.saveLayerAlpha(null, (peca.opacidade * 255f).toInt().coerceIn(0, 255))
        } else {
            -1
        }
        // Borracha na layer de imagem sempre fura o bitmap (não só a tinta vetorial).
        val apagarAgora = usarCache && peca is Peca.Imagem &&
            tracoAtual?.borracha == true && tracoAtual?.idDaPeca == peca.id
        val camadaDoApagador = if (apagarAgora) {
            tela.saveLayer(0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat(), null)
        } else {
            -1
        }
        val camadaIsoladaDoBloqueio = if (peca.alfaBloqueado && peca is Peca.Imagem) tela.saveLayer(null, null) else -1
        val centroX = peca.esquerda + peca.largura / 2f
        val centroY = peca.topo + peca.altura / 2f
        tela.save()
        tela.rotate(peca.angulo, centroX, centroY)
        if (peca !is Peca.Texto && (peca.espelhoHorizontal || peca.espelhoVertical)) {
            tela.scale(
                if (peca.espelhoHorizontal) -1f else 1f,
                if (peca.espelhoVertical) -1f else 1f,
                centroX,
                centroY,
            )
        }
        when (peca) {
            is Peca.Imagem -> {
                if (!peca.bitmap.isRecycled) {
                    val previa = if (usarCache) pinturaDaPrevia(peca) else null
                    tela.drawBitmap(
                        if (usarCache) comDesfoqueDaPrevia(peca, bitmapParaATela(peca)) else peca.bitmap,
                        null,
                        RectF(peca.esquerda, peca.topo, peca.esquerda + peca.largura, peca.topo + peca.altura),
                        previa ?: tintaDaPecaSemFiltro,
                    )
                }
            }
            is Peca.Texto -> {
                tintaDoTexto.color = peca.cor
                tintaDoTexto.textSize = peca.tamanho
                tela.drawText(peca.texto, peca.esquerda, peca.topo - tintaDoTexto.ascent(), tintaDoTexto)
            }
            is Peca.Folha -> Unit
        }
        tela.restore()
        if (apagarAgora) {
            val atual = tracoAtual
            if (atual != null) desenharTraco(tela, atual, 0, molduraLargura.toFloat(), molduraAltura.toFloat())
            tela.restoreToCount(camadaDoApagador)
        }
        val camadaDaTintaBloqueada = if (camadaIsoladaDoBloqueio >= 0) tela.saveLayer(null, pinturaDoBloqueio) else -1
        if (usarCache) desenharTintaEmCache(tela, peca) else desenharTracosDaPeca(tela, peca)
        if (camadaDaTintaBloqueada >= 0) tela.restoreToCount(camadaDaTintaBloqueada)
        if (camadaIsoladaDoBloqueio >= 0) tela.restoreToCount(camadaIsoladaDoBloqueio)
        if (camadaDaOpacidade >= 0) tela.restoreToCount(camadaDaOpacidade)
    }

    /**
     * Para redesenhar a tela a cada movimento da caneta: camada bem maior que o seu tamanho na moldura
     * usa uma cópia reduzida (refeita só quando o bitmap muda ou o tamanho muda muito). Salvar, mesclar
     * e pintar continuam usando o bitmap inteiro.
     */
    private fun bitmapParaATela(imagem: Peca.Imagem): Bitmap {
        val original = imagem.bitmap
        val alvoLargura = Math.ceil(imagem.largura.toDouble()).toInt().coerceAtLeast(1)
        val alvoAltura = Math.ceil(imagem.altura.toDouble()).toInt().coerceAtLeast(1)
        if (original.width <= alvoLargura * 1.5f) return original
        val atual = imagem.exibicao
        if (atual != null && !atual.isRecycled && imagem.exibicaoDe === original) {
            val proporcao = atual.width / alvoLargura.toFloat()
            if (proporcao in 0.8f..1.25f) return atual
        }
        val reduzida = try {
            Bitmap.createScaledBitmap(original, alvoLargura, alvoAltura, true)
        } catch (_: OutOfMemoryError) {
            return original
        }
        if (atual != null && atual !== original && !atual.isRecycled) atual.recycle()
        imagem.exibicao = reduzida
        imagem.exibicaoDe = original
        return reduzida
    }

    private fun fundirTracosNaImagem(imagem: Peca.Imagem) {
        if (imagem.tracos.isEmpty()) return
        val pendentes = imagem.tracos.toList()
        imagem.tracos.clear()
        imagem.tintaSuja = true
        reciclarTinta(imagem)
        pendentes.forEach { traco ->
            gravarTracoNaImagem(imagem, traco)
        }
        aoMudarSelecao?.invoke()
    }

    /**
     * Troca o bitmap da camada por um maior (imagem ∪ traço, sem giro nem espelho, mesma densidade
     * quando cabe na memória) e devolve true. Não mexe se o traço cabe ou se faltar memória.
     */
    private fun crescerImagemParaOTraco(imagem: Peca.Imagem, traco: Traco): Boolean {
        if (molduraLargura <= 0) return false
        val pontos = traco.pontos.map {
            CrescimentoDaCamadaDeImagemParaOTraco.PontoDoTraco(
                it.x * molduraLargura,
                it.y * molduraAltura,
                // Meia largura do pincel; a seta ganha folga para as asas.
                it.largura * molduraLargura * (if (traco.ponta == PontaDoTracoDaComposicao.SETA) 3f else 0.6f),
            )
        }
        return crescerImagemParaCaber(imagem, pontos)
    }

    /** Cantos (girados) de uma peça, em coordenadas da moldura: para a camada de destino caber a peça colada. */
    private fun cantosDaPeca(peca: Peca): List<CrescimentoDaCamadaDeImagemParaOTraco.PontoDoTraco> {
        val cx = peca.esquerda + peca.largura / 2f
        val cy = peca.topo + peca.altura / 2f
        val rad = Math.toRadians(peca.angulo.toDouble())
        val c = Math.cos(rad).toFloat()
        val s = Math.sin(rad).toFloat()
        return listOf(-1f to -1f, 1f to -1f, 1f to 1f, -1f to 1f).map { (dx, dy) ->
            val lx = dx * peca.largura / 2f
            val ly = dy * peca.altura / 2f
            CrescimentoDaCamadaDeImagemParaOTraco.PontoDoTraco(cx + lx * c - ly * s, cy + lx * s + ly * c, 0f)
        }
    }

    private fun crescerImagemParaCaber(
        imagem: Peca.Imagem,
        pontos: List<CrescimentoDaCamadaDeImagemParaOTraco.PontoDoTraco>,
    ): Boolean {
        if (imagem.bitmap.isRecycled) return false
        val novo = CrescimentoDaCamadaDeImagemParaOTraco.novoRetangulo(
            imagem.esquerda, imagem.topo, imagem.largura, imagem.altura, imagem.angulo, pontos,
        ) ?: return false
        val densidadeAtual = imagem.bitmap.width / imagem.largura.coerceAtLeast(1f)
        val escala = CrescimentoDaCamadaDeImagemParaOTraco.escala(novo.largura * densidadeAtual, novo.altura * densidadeAtual)
        val densidade = densidadeAtual * escala
        val maior = try {
            Bitmap.createBitmap(
                CrescimentoDaCamadaDeImagemParaOTraco.tamanhoEmPixels(novo.largura, densidade),
                CrescimentoDaCamadaDeImagemParaOTraco.tamanhoEmPixels(novo.altura, densidade),
                Bitmap.Config.ARGB_8888,
            )
        } catch (_: OutOfMemoryError) {
            return false
        }
        maior.setHasAlpha(true)
        val tela = Canvas(maior)
        tela.scale(maior.width / novo.largura, maior.height / novo.altura)
        tela.translate(-novo.esquerda, -novo.topo)
        val cx = imagem.esquerda + imagem.largura / 2f
        val cy = imagem.topo + imagem.altura / 2f
        tela.rotate(imagem.angulo, cx, cy)
        if (imagem.espelhoHorizontal || imagem.espelhoVertical) {
            tela.scale(if (imagem.espelhoHorizontal) -1f else 1f, if (imagem.espelhoVertical) -1f else 1f, cx, cy)
        }
        tela.drawBitmap(
            imagem.bitmap,
            null,
            RectF(imagem.esquerda, imagem.topo, imagem.esquerda + imagem.largura, imagem.topo + imagem.altura),
            Paint(Paint.FILTER_BITMAP_FLAG),
        )
        imagem.bitmap = maior
        imagem.esquerda = novo.esquerda
        imagem.topo = novo.topo
        imagem.largura = novo.largura
        imagem.altura = novo.altura
        imagem.angulo = 0f
        imagem.espelhoHorizontal = false
        imagem.espelhoVertical = false
        return true
    }

    private fun gravarTracoNaImagem(imagem: Peca.Imagem, traco: Traco): Bitmap? {
        val anterior = imagem.bitmap
        if (anterior.isRecycled || traco.pontos.isEmpty()) return null
        val nova = try {
            anterior.copy(Bitmap.Config.ARGB_8888, true).comTransparencia()
        } catch (_: OutOfMemoryError) {
            return null
        }
        val tela = Canvas(nova)
        val tinta = if (traco.borracha) {
            apagadorDaImagem
        } else {
            pincel
        }
        val xfermodeAntes = tinta.xfermode
        val corAntes = tinta.color
        val estiloAntes = tinta.style
        val capAntes = tinta.strokeCap
        val joinAntes = tinta.strokeJoin
        if (traco.borracha) {
            tinta.xfermode = apagador
            tinta.color = Color.TRANSPARENT
        } else {
            tinta.xfermode = null
            tinta.color = traco.cor
        }
        fun pixel(ponto: Ponto) = ApagadorSobreAImagemDaComposicao.pixel(
            u = ponto.x,
            v = ponto.y,
            molduraLargura = molduraLargura.toFloat(),
            molduraAltura = molduraAltura.toFloat(),
            esquerda = imagem.esquerda,
            topo = imagem.topo,
            largura = imagem.largura,
            altura = imagem.altura,
            angulo = imagem.angulo,
            bitmapLargura = nova.width,
            bitmapAltura = nova.height,
        )
        fun espessura(ponto: Ponto) = ApagadorSobreAImagemDaComposicao.espessura(
            ponto.largura,
            molduraLargura.toFloat(),
            imagem.largura,
            nova.width,
        )
        var desenhou = false
        // Transparência bloqueada: o traço vai numa camada que entra só onde já há pixel (SRC_ATOP).
        val camadaDoBloqueio = if (imagem.alfaBloqueado && !traco.borracha) tela.saveLayer(null, pinturaDoBloqueio) else -1
        val pontos = traco.pontos
        val reta = GeometriaDaPontaDoTraco.reta(traco.ponta) && !traco.borracha
        tinta.strokeCap = if (reta) Paint.Cap.SQUARE else Paint.Cap.ROUND
        tinta.strokeJoin = if (reta) Paint.Join.BEVEL else Paint.Join.ROUND
        val pincelDoTraco = traco.pincel
        if (pincelDoTraco != null) {
            val pontosNaImagem = pontos.mapNotNull { ponto ->
                pixel(ponto)?.let { PontoParaCarimbar(it.x, it.y, espessura(ponto), ponto.alpha / 255f) }
            }
            val carimbos = MotorDeCarimbosDoPincel.gerar(pontosNaImagem, pincelDoTraco, traco.semente, comAfinamentoDoFim = true)
            // Pixels da imagem por unidade da folha (o grão fica no mesmo lugar que no resto do papel).
            val escalaDoPapel = nova.width / imagem.largura.coerceAtLeast(1f)
            if (traco.borracha) {
                renderizadorDoMotor.apagarTraco(tela, carimbos, alphaDaBorracha(traco), pincelDoTraco, escalaDoPapel)
            } else {
                renderizadorDoMotor.desenharTraco(tela, carimbos, traco.cor, pincelDoTraco, escalaDoPapel)
            }
            desenhou = carimbos.isNotEmpty()
        } else if (borrachaParcial(traco)) {
            // Um caminho só: apagar em parte por igual, sem marcar mais nas emendas.
            tinta.xfermode = apagadorParcial
            tinta.color = Color.argb(alphaDaBorracha(traco), 0, 0, 0)
            tinta.strokeCap = Paint.Cap.ROUND
            tinta.strokeJoin = Paint.Join.ROUND
            val caminho = android.graphics.Path()
            var aberto = false
            pontos.forEach { ponto ->
                val p = pixel(ponto)
                if (p == null) {
                    aberto = false
                } else if (!aberto) {
                    caminho.moveTo(p.x, p.y)
                    aberto = true
                } else {
                    caminho.lineTo(p.x, p.y)
                }
            }
            val largura = espessura(pontos.last())
            if (pontos.size == 1) {
                pixel(pontos[0])?.let { centro ->
                    tinta.style = Paint.Style.FILL
                    tela.drawCircle(centro.x, centro.y, largura / 2f, tinta)
                    desenhou = true
                }
            } else if (!caminho.isEmpty) {
                tinta.style = Paint.Style.STROKE
                tinta.strokeWidth = largura
                tela.drawPath(caminho, tinta)
                desenhou = true
            }
        } else if (traco.ponta == PontaDoTracoDaComposicao.SETA && pontos.size >= 2) {
            val cauda = pixel(pontos.first())
            val ponta = pixel(pontos.last())
            if (cauda != null && ponta != null) {
                val largura = espessura(pontos.last())
                tinta.style = Paint.Style.STROKE
                tinta.strokeWidth = largura
                tela.drawLine(cauda.x, cauda.y, ponta.x, ponta.y, tinta)
                val asas = SetaSobreAFolha.asas(cauda.x, cauda.y, ponta.x, ponta.y, largura)
                if (asas != null) {
                    tinta.style = Paint.Style.STROKE
                    tela.drawLine(ponta.x, ponta.y, asas.esquerdaX, asas.esquerdaY, tinta)
                    tela.drawLine(ponta.x, ponta.y, asas.direitaX, asas.direitaY, tinta)
                }
                desenhou = true
            }
        } else if (pontos.size == 1) {
            val centro = pixel(pontos[0])
            if (centro != null) {
                tinta.style = Paint.Style.FILL
                val raio = espessura(pontos[0]) / 2f
                if (reta) {
                    tela.drawRect(centro.x - raio, centro.y - raio, centro.x + raio, centro.y + raio, tinta)
                } else {
                    tela.drawCircle(centro.x, centro.y, raio, tinta)
                }
                desenhou = true
            }
        } else {
            tinta.style = Paint.Style.STROKE
            for (indice in 1 until pontos.size) {
                val saida = pixel(pontos[indice - 1]) ?: continue
                val chegada = pixel(pontos[indice]) ?: continue
                if (!traco.borracha && pontos[indice].alpha < 255) {
                    tinta.color = corComAlphaDoPonto(traco.cor, pontos[indice].alpha)
                }
                tinta.strokeWidth = (espessura(pontos[indice - 1]) + espessura(pontos[indice])) / 2f
                tela.drawLine(saida.x, saida.y, chegada.x, chegada.y, tinta)
                desenhou = true
            }
        }
        if (camadaDoBloqueio >= 0) tela.restoreToCount(camadaDoBloqueio)
        tinta.xfermode = xfermodeAntes
        tinta.color = corAntes
        tinta.style = estiloAntes
        tinta.strokeCap = capAntes
        tinta.strokeJoin = joinAntes
        if (!desenhou) {
            nova.recycle()
            return null
        }
        imagem.bitmap = nova
        imagem.tintaSuja = true
        reciclarTinta(imagem)
        return anterior
    }

    private fun desenharTintaEmCache(tela: Canvas, peca: Peca) {
        val atual = tracoAtual?.takeIf { it.idDaPeca == peca.id }
        if (peca.tracos.isEmpty() && atual == null) {
            if (peca.tinta != null) reciclarTinta(peca)
            return
        }
        val tinta = garantirTinta(peca)
        if (tinta == null) {
            desenharTracosDaPeca(tela, peca)
            return
        }
        if (peca.tintaSuja) {
            reconstruirTinta(peca, tinta)
            if (atual != null) pontosJaNaTinta = 0
        }
        // Borracha do motor em andamento: a tinta é desenhada só na camada isolada abaixo (senão a cópia daqui apareceria por baixo do apagado).
        val borrachaDoMotorEmAndamento = atual?.pincel != null && atual.borracha && tintaDoTracoDoMotor?.isRecycled == false &&
            (carimbosNaTintaDoMotor > 0 || caudaDoTracoDoMotor.isNotEmpty())
        if (!borrachaDoMotorEmAndamento) {
            tela.drawBitmap(
                if (atual == null) comDesfoqueDaPrevia(peca, tinta) else tinta,
                null,
                RectF(0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat()),
                pinturaDaPrevia(peca) ?: filtroDaTinta,
            )
        }
        val camadaDoMotor = tintaDoTracoDoMotor
        val pincelAtual = atual?.pincel
        if (atual != null && pincelAtual != null && atual.borracha &&
            (carimbosNaTintaDoMotor > 0 || caudaDoTracoDoMotor.isNotEmpty()) &&
            camadaDoMotor != null && !camadaDoMotor.isRecycled
        ) {
            // A tinta já foi desenhada acima; aqui ela é redesenhada numa camada isolada e a borracha fura só ela.
            val isolada = tela.saveLayer(0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat(), null)
            tela.drawBitmap(tinta, null, RectF(0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat()), filtroDaTinta)
            pinturaDaBorrachaDoMotor.alpha = alphaDaBorracha(atual)
            val mascara = tela.saveLayer(0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat(), pinturaDaBorrachaDoMotor)
            tela.drawBitmap(camadaDoMotor, null, RectF(0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat()), pinturaDaCamadaDoMotor.apply { alpha = 255 })
            if (caudaDoTracoDoMotor.isNotEmpty()) {
                tela.save()
                tela.scale(molduraLargura.toFloat() / camadaDoMotor.width, molduraAltura.toFloat() / camadaDoMotor.height)
                renderizadorDoMotor.desenhar(tela, caudaDoTracoDoMotor, 0, Color.BLACK, pincelAtual)
                tela.restore()
            }
            renderizadorDoMotor.aplicarGranuladoNoPapel(tela, 0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat(), pincelAtual, 1f)
            tela.restoreToCount(mascara)
            tela.restoreToCount(isolada)
            return
        }
        if (atual != null && pincelAtual != null && !atual.borracha &&
            (carimbosNaTintaDoMotor > 0 || caudaDoTracoDoMotor.isNotEmpty()) &&
            camadaDoMotor != null && !camadaDoMotor.isRecycled
        ) {
            // Parte assentada + cauda na mesma camada, limitada pela opacidade do traço (como o traço final).
            val camada = tela.saveLayerAlpha(0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat(), (atual.cor ushr 24).coerceIn(0, 255))
            pinturaDaCamadaDoMotor.alpha = 255
            tela.drawBitmap(camadaDoMotor, null, RectF(0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat()), pinturaDaCamadaDoMotor)
            if (caudaDoTracoDoMotor.isNotEmpty()) {
                tela.save()
                tela.scale(molduraLargura.toFloat() / camadaDoMotor.width, molduraAltura.toFloat() / camadaDoMotor.height)
                renderizadorDoMotor.desenhar(tela, caudaDoTracoDoMotor, 0, atual.cor, pincelAtual)
                tela.restore()
            }
            renderizadorDoMotor.aplicarGranuladoNoPapel(tela, 0f, 0f, molduraLargura.toFloat(), molduraAltura.toFloat(), pincelAtual, 1f)
            tela.restoreToCount(camada)
        }
    }

    private fun aplicarTrechoAtualNaTinta() {
        val atual = tracoAtual ?: return
        if (atual.pontos.isEmpty()) return
        val peca = objetos.find { it.id == atual.idDaPeca } ?: return
        val tinta = garantirTinta(peca) ?: return
        if (peca.tintaSuja) {
            reconstruirTinta(peca, tinta)
            pontosJaNaTinta = 0
        }
        val pincelAtual = atual.pincel
        if (pincelAtual != null) {
            aplicarTrechoDoMotor(atual, pincelAtual, tinta)
            return
        }
        val desde = TintaEmCacheDaCamadaDaComposicao.primeiroIndice(pontosJaNaTinta, atual.pontos.size)
        if (desde >= atual.pontos.size) return
        desenharTraco(Canvas(tinta), atual, desde, tinta.width.toFloat(), tinta.height.toFloat())
        pontosJaNaTinta = atual.pontos.size
    }

    private fun garantirTinta(peca: Peca): Bitmap? {
        val (largura, altura) = TintaEmCacheDaCamadaDaComposicao.dimensoes(molduraLargura, molduraAltura)
        val atual = peca.tinta
        if (atual != null && !atual.isRecycled && atual.width == largura && atual.height == altura) return atual
        reciclarTinta(peca)
        return try {
            Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888).also {
                peca.tinta = it
                peca.tintaSuja = true
            }
        } catch (_: OutOfMemoryError) {
            null
        }
    }

    private fun reconstruirTinta(peca: Peca, tinta: Bitmap) {
        tinta.eraseColor(Color.TRANSPARENT)
        val destinoDaTinta = Canvas(tinta)
        peca.tracos.forEach { desenharTraco(destinoDaTinta, it, 0, tinta.width.toFloat(), tinta.height.toFloat()) }
        peca.tintaSuja = false
    }

    private fun invalidarTintas() {
        (objetos + pecasFora.values).forEach { peca ->
            reciclarTinta(peca)
            peca.tintaSuja = true
        }
        pontosJaNaTinta = 0
    }

    private fun reciclarTodasAsTintas() {
        (objetos + pecasFora.values).forEach { reciclarTinta(it) }
    }

    private fun reciclarTinta(peca: Peca) {
        val tinta = peca.tinta ?: return
        peca.tinta = null
        if (!tinta.isRecycled) tinta.recycle()
    }

    private fun cancelarTracoAtual() {
        removeCallbacks(fixarFormaLimpa)
        formaFixadaNoTraco = false
        val id = tracoAtual?.idDaPeca
        tracoAtual = null
        idDoPonteiroDoTraco = -1
        pontosJaNaTinta = 0
        if (id != null) objetos.find { it.id == id }?.tintaSuja = true
        soltarAncoraDoTraco()
    }

    private val apagadorParcial = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

    /** Cor guardada no traço da borracha: transparente = apaga tudo (traços antigos); alpha 1..254 = apaga em parte. */
    private fun corDaBorracha(): Int {
        if (opacidadeDaBorracha >= 0.999f) return Color.TRANSPARENT
        return Color.argb((opacidadeDaBorracha * 255f).toInt().coerceIn(1, 254), 0, 0, 0)
    }

    private fun alphaDaBorracha(traco: Traco): Int = (traco.cor ushr 24).let { if (it == 0) 255 else it }

    private fun borrachaParcial(traco: Traco): Boolean = traco.borracha && traco.pincel == null && alphaDaBorracha(traco) < 255

    /**
     * Borracha com opacidade: largura constante (borracha não usa pressão), desenhada como um caminho
     * para apagar por igual. No traço em andamento cada trecho sai sem pontas redondas (não sobrepõe o anterior);
     * ao terminar, a camada é refeita com o traço inteiro.
     */
    private fun desenharBorrachaParcial(tela: Canvas, traco: Traco, desde: Int, larguraAlvo: Float, alturaAlvo: Float) {
        val pontos = traco.pontos
        pincel.xfermode = apagadorParcial
        pincel.color = Color.argb(alphaDaBorracha(traco), 0, 0, 0)
        val largura = pontos.last().largura * larguraAlvo
        if (pontos.size == 1) {
            pincel.style = Paint.Style.FILL
            tela.drawCircle(pontos[0].x * larguraAlvo, pontos[0].y * alturaAlvo, largura / 2f, pincel)
            pincel.style = Paint.Style.STROKE
            return
        }
        val inicio = (desde - 1).coerceAtLeast(0)
        val caminho = android.graphics.Path()
        caminho.moveTo(pontos[inicio].x * larguraAlvo, pontos[inicio].y * alturaAlvo)
        for (i in inicio + 1 until pontos.size) caminho.lineTo(pontos[i].x * larguraAlvo, pontos[i].y * alturaAlvo)
        pincel.style = Paint.Style.STROKE
        pincel.strokeWidth = largura
        pincel.strokeJoin = Paint.Join.ROUND
        pincel.strokeCap = if (desde <= 0) Paint.Cap.ROUND else Paint.Cap.BUTT
        tela.drawPath(caminho, pincel)
    }

    private fun desenharTracosDaPeca(tela: Canvas, peca: Peca) {
        val atual = tracoAtual?.takeIf { it.idDaPeca == peca.id }
        if (peca.tracos.isEmpty() && atual == null) return
        val camadaDosTracos = tela.saveLayer(
            0f,
            0f,
            molduraLargura.toFloat(),
            molduraAltura.toFloat(),
            null,
        )
        peca.tracos.forEach { desenharTraco(tela, it, 0, molduraLargura.toFloat(), molduraAltura.toFloat()) }
        if (atual != null) desenharTraco(tela, atual, 0, molduraLargura.toFloat(), molduraAltura.toFloat())
        tela.restoreToCount(camadaDosTracos)
    }

    private fun desenharTraco(
        tela: Canvas,
        traco: Traco,
        desde: Int,
        larguraAlvo: Float,
        alturaAlvo: Float,
    ) {
        if (desde >= traco.pontos.size) return
        if (traco.pincel != null) {
            desenharTracoDoMotor(tela, traco, larguraAlvo, alturaAlvo, concluido = true)
            return
        }
        if (borrachaParcial(traco)) {
            desenharBorrachaParcial(tela, traco, desde, larguraAlvo, alturaAlvo)
            return
        }
        if (traco.borracha) {
            pincel.xfermode = apagador
            pincel.color = Color.TRANSPARENT
        } else {
            pincel.xfermode = null
            pincel.color = traco.cor
        }
        val pontos = traco.pontos
        fun x(ponto: Ponto) = ponto.x * larguraAlvo
        fun y(ponto: Ponto) = ponto.y * alturaAlvo
        fun largura(ponto: Ponto) = ponto.largura * larguraAlvo
        fun aplicarAlpha(ponto: Ponto) {
            if (traco.borracha || ponto.alpha >= 255) return
            pincel.color = corComAlphaDoPonto(traco.cor, ponto.alpha)
        }
        val reta = GeometriaDaPontaDoTraco.reta(traco.ponta) && !traco.borracha
        pincel.strokeCap = if (reta) Paint.Cap.SQUARE else Paint.Cap.ROUND
        pincel.strokeJoin = if (reta) Paint.Join.BEVEL else Paint.Join.ROUND
        if (traco.ponta == PontaDoTracoDaComposicao.SETA && pontos.size >= 2) {
            val cauda = pontos.first()
            val ponta = pontos.last()
            pincel.style = Paint.Style.STROKE
            pincel.strokeCap = Paint.Cap.ROUND
            pincel.strokeJoin = Paint.Join.ROUND
            pincel.strokeWidth = largura(ponta)
            aplicarAlpha(ponta)
            tela.drawLine(x(cauda), y(cauda), x(ponta), y(ponta), pincel)
            val asas = SetaSobreAFolha.asas(x(cauda), y(cauda), x(ponta), y(ponta), largura(ponta)) ?: return
            tela.drawLine(x(ponta), y(ponta), asas.esquerdaX, asas.esquerdaY, pincel)
            tela.drawLine(x(ponta), y(ponta), asas.direitaX, asas.direitaY, pincel)
            return
        }
        if (desde == 0 && pontos.size == 1) {
            pincel.style = Paint.Style.FILL
            aplicarAlpha(pontos[0])
            if (reta) {
                val metade = largura(pontos[0]) / 2f
                tela.drawRect(x(pontos[0]) - metade, y(pontos[0]) - metade, x(pontos[0]) + metade, y(pontos[0]) + metade, pincel)
            } else {
                tela.drawCircle(x(pontos[0]), y(pontos[0]), largura(pontos[0]) / 2f, pincel)
            }
            pincel.style = Paint.Style.STROKE
            return
        }
        pincel.style = Paint.Style.STROKE
        val inicio = if (desde <= 0) 1 else desde + 1
        for (indice in inicio until pontos.size) {
            val anterior = pontos[indice - 1]
            val atual = pontos[indice]
            aplicarAlpha(atual)
            pincel.strokeWidth = (largura(anterior) + largura(atual)) / 2f
            tela.drawLine(x(anterior), y(anterior), x(atual), y(atual), pincel)
        }
    }

    private fun pecaDoTraco(): Peca? {
        if (molduraLargura <= 0 || molduraAltura <= 0) return null
        val ativa = objetos.find { it.id == idDaCamadaAtiva }
        val compativel = ativa is Peca.Imagem || ativa is Peca.Folha
        if (!DestinoDoTracoNaCamadaAtiva.aceitaPintura(temCamadaAtivaCompativel = compativel)) {
            return null
        }
        if (ativa is Peca.Folha && ativa.alfaBloqueado) return promoverFolhaParaImagem(ativa) ?: ativa
        return ativa
    }

    private fun garantirTamanhoDaFolhaNaMoldura(folha: Peca.Folha) {
        folha.esquerda = 0f
        folha.topo = 0f
        folha.largura = molduraLargura.toFloat().coerceAtLeast(1f)
        folha.altura = molduraAltura.toFloat().coerceAtLeast(1f)
    }

    private fun ferramentaDeTraco(): Boolean {
        return ferramenta == Ferramenta.VERMELHO ||
            ferramenta == Ferramenta.BRANCO ||
            ferramenta == Ferramenta.BORRACHA
    }

    private fun toqueNaRegua(event: MotionEvent): Boolean {
        val atual = regua ?: return false
        val (x, y) = paraATelaReal(event.x, event.y)
        return GestoDaReguaSobreAFolha.parte(atual, x, y) != ParteDaReguaSobreAFolha.NENHUMA
    }

    private fun corDoTraco(peca: Peca, u: Float, v: Float): Int? {
        var cor: Int? = null
        val larguraDoQuadro = molduraLargura.toFloat()
        val alturaDoQuadro = molduraAltura.toFloat()
        val x = u * larguraDoQuadro
        val y = v * alturaDoQuadro
        peca.tracos.forEach { traco ->
            val perto = traco.pontos.any { ponto ->
                val raio = (ponto.largura * larguraDoQuadro) / 2f
                hypot(ponto.x * larguraDoQuadro - x, ponto.y * alturaDoQuadro - y) <= raio.coerceAtLeast(1f)
            }
            if (perto) cor = if (traco.borracha) null else traco.cor
        }
        return cor
    }

    private fun calcularDestino() {
        if (width == 0 || height == 0 || molduraLargura <= 0 || molduraAltura <= 0) return
        val escalaBase = min(width.toFloat() / molduraLargura, height.toFloat() / molduraAltura)
        val largura = molduraLargura * escalaBase * zoom
        val altura = molduraAltura * escalaBase * zoom
        val margem = 64f * resources.displayMetrics.density
        val centroX = (width - largura) / 2f
        val centroY = (height - altura) / 2f
        if (largura <= width) {
            panX = 0f
        } else {
            val minimo = margem - largura - centroX
            val maximo = width - margem - centroX
            panX = panX.coerceIn(min(minimo, maximo), max(minimo, maximo))
        }
        if (altura <= height) {
            panY = 0f
        } else {
            val minimo = margem - altura - centroY
            val maximo = height - margem - centroY
            panY = panY.coerceIn(min(minimo, maximo), max(minimo, maximo))
        }
        destino.set(centroX + panX, centroY + panY, centroX + panX + largura, centroY + panY + altura)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (regua == null && molduraLargura > 0) colocarRegua()
    }

    private fun soltarAncoraDoTraco() {
        ancoraDoTracoX = null
        ancoraDoTracoY = null
        anguloDoTracoNaRegua = null
    }

    private fun colocarRegua() {
        if (width <= 0 || height <= 0) return
        val densidade = resources.displayMetrics.density
        regua = GestoDaReguaSobreAFolha.posicionar(
            width,
            height,
            280f * densidade,
            64f * densidade,
            18f * densidade,
        )
        parteDaRegua = ParteDaReguaSobreAFolha.NENHUMA
    }

    private fun atualizarRegua(event: MotionEvent) {
        val atual = regua ?: return
        val (ex, ey) = paraATelaReal(event.x, event.y)
        regua = when (parteDaRegua) {
            ParteDaReguaSobreAFolha.CORPO -> {
                val movida = GestoDaReguaSobreAFolha.mover(
                    atual,
                    ex - origemDoGestoX,
                    ey - origemDoGestoY,
                )
                origemDoGestoX = ex
                origemDoGestoY = ey
                movida
            }
            ParteDaReguaSobreAFolha.GIRO -> {
                val graus = Math.toDegrees(
                    atan2((ey - atual.centroY).toDouble(), (ex - atual.centroX).toDouble()),
                ).toFloat()
                val delta = GestoDaReguaSobreAFolha.variacao(ultimoAnguloDoDedo, graus)
                ultimoAnguloDoDedo = graus
                atual.copy(anguloGraus = GestoDaReguaSobreAFolha.angulo(graus, delta))
            }
            else -> atual
        }
        invalidate()
    }

    private fun desenharRegua(canvas: Canvas) {
        val atual = regua ?: return
        val densidade = resources.displayMetrics.density
        val canto = 12f * densidade
        corpoDaRegua.color = Color.argb(120, 94, 234, 212)
        bordaEscuraDaRegua.color = Color.parseColor("#1A1D24")
        bordaEscuraDaRegua.strokeWidth = 5f * densidade
        bordaClaraDaRegua.color = if (atual.armada) Color.parseColor("#8AB4F8") else Color.parseColor("#F4F7FB")
        bordaClaraDaRegua.strokeWidth = 2f * densidade
        caboDaRegua.color = if (atual.armada) Color.parseColor("#8AB4F8") else Color.argb(220, 244, 247, 251)
        canvas.save()
        canvas.translate(atual.centroX, atual.centroY)
        canvas.rotate(atual.anguloGraus)
        val metade = atual.comprimento / 2f
        val meiaEspessura = atual.espessura / 2f
        val corpo = RectF(-metade, -meiaEspessura, metade, meiaEspessura)
        canvas.drawRoundRect(corpo, canto, canto, corpoDaRegua)
        canvas.drawRoundRect(corpo, canto, canto, bordaEscuraDaRegua)
        canvas.drawRoundRect(corpo, canto, canto, bordaClaraDaRegua)
        var marca = -metade + atual.comprimento / 10f
        val passo = atual.comprimento / 10f
        while (marca < metade) {
            canvas.drawLine(marca, -meiaEspessura + 8f * densidade, marca, -meiaEspessura + 18f * densidade, bordaClaraDaRegua)
            marca += passo
        }
        canvas.drawCircle(metade + atual.raioDoCabo, 0f, atual.raioDoCabo, caboDaRegua)
        canvas.drawCircle(-metade - atual.raioDoCabo, 0f, atual.raioDoCabo * 0.72f, caboDaRegua)
        canvas.restore()
    }

    private fun paraAComposicao(x: Float, y: Float): FloatArray? {
        if (destino.width() <= 0f || molduraLargura <= 0) return null
        val px = (x - destino.left) / destino.width() * molduraLargura
        val py = (y - destino.top) / destino.height() * molduraAltura
        return floatArrayOf(px, py)
    }

    private fun rectNaTela(esquerda: Float, topo: Float, largura: Float, altura: Float): RectF {
        val escalaX = destino.width() / molduraLargura
        val escalaY = destino.height() / molduraAltura
        return RectF(
            destino.left + esquerda * escalaX,
            destino.top + topo * escalaY,
            destino.left + (esquerda + largura) * escalaX,
            destino.top + (topo + altura) * escalaY,
        )
    }

    private fun capturar(peca: Peca) = EstadoDaPeca(
        peca.esquerda,
        peca.topo,
        peca.largura,
        peca.altura,
        (peca as? Peca.Texto)?.tamanho ?: 0f,
        peca.angulo,
        peca.espelhoHorizontal,
        peca.espelhoVertical,
    )

    private fun restaurar(peca: Peca, estado: EstadoDaPeca) {
        if (peca is Peca.Texto) {
            peca.tamanho = estado.tamanho
            medir(peca)
        } else {
            peca.largura = estado.largura
            peca.altura = estado.altura
        }
        peca.esquerda = estado.esquerda
        peca.topo = estado.topo
        peca.angulo = estado.angulo
        peca.espelhoHorizontal = estado.espelhoHorizontal
        peca.espelhoVertical = estado.espelhoVertical
    }

    private fun copiarTracos(): Map<Int, List<Traco>> = objetos.associate { peca ->
        peca.id to peca.tracos.map { traco ->
            Traco(
                idDaPeca = traco.idDaPeca,
                pontos = traco.pontos.map { Ponto(it.x, it.y, it.largura, it.alpha) }.toMutableList(),
                cor = traco.cor,
                borracha = traco.borracha,
                ponta = traco.ponta,
                pincel = traco.pincel,
                semente = traco.semente,
            )
        }
    }

    private fun restaurarTracos(copia: Map<Int, List<Traco>>) {
        objetos.forEach { peca ->
            peca.tracos.clear()
            copia[peca.id].orEmpty().forEach { traco ->
                peca.tracos += Traco(
                    idDaPeca = peca.id,
                    pontos = traco.pontos.map { Ponto(it.x, it.y, it.largura, it.alpha) }.toMutableList(),
                    cor = traco.cor,
                    borracha = traco.borracha,
                    ponta = traco.ponta,
                    pincel = traco.pincel,
                    semente = traco.semente,
                )
            }
        }
    }

    private fun pontosNoQuadro(traco: TracoDoRascunhoDaEdicao, peca: Peca): MutableList<Ponto> {
        if (traco.coordenadasDoQuadro) {
            return traco.pontos.map { Ponto(it.x, it.y, it.largura, it.alpha) }.toMutableList()
        }
        val quadroLargura = molduraLargura.toFloat().coerceAtLeast(1f)
        val quadroAltura = molduraAltura.toFloat().coerceAtLeast(1f)
        return traco.pontos.map { ponto ->
            val noQuadro = TracoNaCamadaDaComposicao.paraOQuadro(
                u = ponto.x,
                v = ponto.y,
                esquerda = peca.esquerda,
                topo = peca.topo,
                largura = peca.largura,
                altura = peca.altura,
                larguraDoQuadro = quadroLargura,
                alturaDoQuadro = quadroAltura,
            )
            Ponto(
                noQuadro?.x ?: ponto.x,
                noQuadro?.y ?: ponto.y,
                TracoNaCamadaDaComposicao.larguraRemapeada(ponto.largura, peca.largura, quadroLargura),
                ponto.alpha,
            )
        }.toMutableList()
    }

    private fun remapearTracosNoQuadro(
        antigaLargura: Float,
        antigaAltura: Float,
        deslocamentoX: Float,
        deslocamentoY: Float,
    ) {
        val novaLargura = molduraLargura.toFloat()
        val novaAltura = molduraAltura.toFloat()
        objetos.forEach { peca ->
            peca.tracos.forEach { traco ->
                val novos = traco.pontos.mapNotNull { ponto ->
                    val novo = TracoNaCamadaDaComposicao.remapear(
                        u = ponto.x,
                        v = ponto.y,
                        antigaEsquerda = 0f,
                        antigaTopo = 0f,
                        antigaLargura = antigaLargura,
                        antigaAltura = antigaAltura,
                        novaEsquerda = 0f,
                        novaTopo = 0f,
                        novaLargura = novaLargura,
                        novaAltura = novaAltura,
                        deslocamentoX = deslocamentoX,
                        deslocamentoY = deslocamentoY,
                    ) ?: return@mapNotNull null
                    Ponto(
                        novo.x,
                        novo.y,
                        TracoNaCamadaDaComposicao.larguraRemapeada(ponto.largura, antigaLargura, novaLargura),
                        ponto.alpha,
                    )
                }
                traco.pontos.clear()
                traco.pontos.addAll(novos)
            }
        }
        invalidarTintas()
    }

    private fun empilhar(passo: Passo) {
        passos += passo
        while (passos.size > 12) descartarPassoMaisAntigoComCuidadoDoMover()
        limparDesfeitos()
        tamanhoDoPacoteDoMoverEmDesfeitos = 0
    }

    /**
     * Evita descartar o [Passo.Destaque] enquanto ainda há passos do pacote do Mover
     * (senão o buraco na imagem original nunca mais volta com desfazer).
     */
    private fun descartarPassoMaisAntigoComCuidadoDoMover() {
        if (passos.isEmpty()) return
        val primeiro = passos.first()
        if (primeiro is Passo.Destaque) {
            val idNova = primeiro.idNova
            val temDependente = passos.asSequence().drop(1).any { passo ->
                when (passo) {
                    is Passo.Transformacao -> passo.id == idNova
                    is Passo.ModoTransformarDaPeca -> passo.idDaPeca == idNova
                    is Passo.FundirPecaTransformada -> passo.idPeca == idNova
                    else -> false
                }
            }
            if (temDependente) {
                // Descarta o pacote inteiro do fundo (Destaque + dependentes) de uma vez.
                descartar(passos.removeAt(0))
                while (passos.isNotEmpty()) {
                    val seguinte = passos.first()
                    val doPacote = when (seguinte) {
                        is Passo.Transformacao -> seguinte.id == idNova
                        is Passo.ModoTransformarDaPeca -> seguinte.idDaPeca == idNova
                        is Passo.FundirPecaTransformada -> seguinte.idPeca == idNova
                        else -> false
                    }
                    if (!doPacote) break
                    descartar(passos.removeAt(0))
                }
                return
            }
        }
        descartar(passos.removeAt(0))
    }

    private fun limparDesfeitos() {
        val guardados = desfeitos.toList()
        desfeitos.clear()
        guardados.forEach { descartar(it) }
        val ids = guardados.mapNotNull {
            when (it) {
                is Passo.Inclusao -> it.id
                is Passo.Destaque -> it.idNova
                is Passo.FundirPecaTransformada -> it.idPeca
                else -> null
            }
        }
        ids.forEach { id ->
            val peca = pecasFora.remove(id) ?: return@forEach
            reciclarTinta(peca)
            val imagem = peca as? Peca.Imagem ?: return@forEach
            if (imagem.bitmap.isRecycled || bitmapEmUso(imagem.bitmap)) return@forEach
            imagem.bitmap.recycle()
        }
    }

    private fun bitmapEmUso(bitmap: Bitmap): Boolean {
        if (objetos.any { it is Peca.Imagem && it.bitmap === bitmap }) return true
        if (pecasFora.values.any { it is Peca.Imagem && it.bitmap === bitmap }) return true
        val guardados = passos + desfeitos
        return guardados.any { passo ->
            (passo is Passo.Corte && passo.bitmapAnterior === bitmap) ||
                (passo is Passo.Destaque && passo.bitmapAnterior === bitmap) ||
                (passo is Passo.FundirPecaTransformada && passo.bitmapOrigemAntes === bitmap) ||
                (passo is Passo.Risco && passo.bitmapAnterior === bitmap) ||
                (passo is Passo.Promocao && passo.imagem.bitmap === bitmap) ||
                (
                    passo is Passo.MesclarCamadas &&
                        listOf(passo.cima, passo.baixo, passo.mesclada).any { (it as? Peca.Imagem)?.bitmap === bitmap }
                    )
        }
    }

    private fun descartar(passo: Passo) {
        if (passo is Passo.Risco) {
            val anterior = passo.bitmapAnterior ?: return
            if (!bitmapEmUso(anterior) && !anterior.isRecycled) anterior.recycle()
            return
        }
        if (passo is Passo.Corte) {
            val emUso = objetos.any { it is Peca.Imagem && it.bitmap === passo.bitmapAnterior }
            if (!emUso && !passo.bitmapAnterior.isRecycled) passo.bitmapAnterior.recycle()
        }
        if (passo is Passo.Destaque) {
            val anterior = passo.bitmapAnterior ?: return
            val emUso = objetos.any { it is Peca.Imagem && it.bitmap === anterior }
            if (!emUso && !anterior.isRecycled) anterior.recycle()
        }
        if (passo is Passo.AlteraBitmap) {
            val emUso = objetos.any { it is Peca.Imagem && it.bitmap === passo.bitmapAnterior }
            if (!emUso && !passo.bitmapAnterior.isRecycled) passo.bitmapAnterior.recycle()
        }
        if (passo is Passo.FundirPecaTransformada) {
            val emUso = objetos.any { it is Peca.Imagem && it.bitmap === passo.bitmapOrigemAntes }
            if (!emUso && !passo.bitmapOrigemAntes.isRecycled) passo.bitmapOrigemAntes.recycle()
        }
    }

    private fun novoId(): Int = proximoId++

    private class Ponto(val x: Float, val y: Float, val largura: Float, val alpha: Int = 255)

    private class Traco(
        val idDaPeca: Int,
        val pontos: MutableList<Ponto> = mutableListOf(),
        val cor: Int,
        val borracha: Boolean,
        val ponta: PontaDoTracoDaComposicao = PontaDoTracoDaComposicao.CANETA,
        /** Pincel do motor de carimbos (null = traço comum, desenhado com linhas). */
        val pincel: PincelDoMotor? = null,
        /** Semente dos tremores: redesenhar dá o mesmo resultado. */
        val semente: Long = 0L,
    )

    private class EstadoDaPeca(
        val esquerda: Float,
        val topo: Float,
        val largura: Float,
        val altura: Float,
        val tamanho: Float,
        val angulo: Float,
        val espelhoHorizontal: Boolean = false,
        val espelhoVertical: Boolean = false,
    )

    private sealed class Peca {
        abstract val id: Int
        abstract var esquerda: Float
        abstract var topo: Float
        abstract var largura: Float
        abstract var altura: Float
        abstract var angulo: Float
        var espelhoHorizontal = false
        var espelhoVertical = false
        var visivel = true
        /** Opacidade da camada inteira (0..1), aplicada ao desenhar a peça. */
        var opacidade = 1f
        /** Transparência bloqueada: a tinta só entra onde a camada já tem pixel. */
        var alfaBloqueado = false
        var tinta: Bitmap? = null
        var tintaSuja = true
        val tracos = mutableListOf<Traco>()

        class Folha(
            override val id: Int,
            override var esquerda: Float = 0f,
            override var topo: Float = 0f,
            override var largura: Float = 0f,
            override var altura: Float = 0f,
            override var angulo: Float = 0f,
        ) : Peca()

        class Imagem(
            override val id: Int,
            var bitmap: Bitmap,
            override var esquerda: Float = 0f,
            override var topo: Float = 0f,
            override var largura: Float = 0f,
            override var altura: Float = 0f,
            override var angulo: Float = 0f,
        ) : Peca() {
            /** Cópia reduzida para a tela (a camada pode ter milhares de px; a tela é na escala da moldura). */
            var exibicao: Bitmap? = null
            var exibicaoDe: Bitmap? = null
        }

        class Texto(
            override val id: Int,
            var texto: String,
            var tamanho: Float,
            var cor: Int,
            override var esquerda: Float = 0f,
            override var topo: Float = 0f,
            override var largura: Float = 0f,
            override var altura: Float = 0f,
            override var angulo: Float = 0f,
        ) : Peca()
    }

    private sealed class Passo {
        class Risco(
            val traco: Traco,
            val bitmapAnterior: Bitmap? = null,
            val idDaImagem: Int = -1,
            /** Preenchido quando a camada cresceu para caber o traço: desfazer volta ao tamanho antigo. */
            val estadoAnterior: EstadoDaPeca? = null,
        ) : Passo()
        class Inclusao(val id: Int) : Passo()
        class Folha(val cor: Int) : Passo()
        class Ordem(val ids: List<Int>) : Passo()
        class Transformacao(val id: Int, val estado: EstadoDaPeca) : Passo()
        class TextoAlterado(val id: Int, val texto: String, val cor: Int) : Passo()
        class Moldura(val largura: Int, val altura: Int) : Passo()
        class Corte(
            val id: Int,
            val bitmapAnterior: Bitmap,
            val estado: EstadoDaPeca,
            val molduraLargura: Int,
            val molduraAltura: Int,
            val riscos: Map<Int, List<Traco>>,
        ) : Passo()

        class Destaque(
            val idOrigem: Int,
            val bitmapAnterior: Bitmap?,
            val idNova: Int,
        ) : Passo()

        class SelecaoDoLaco(
            val pontos: List<PontoDoLacoLivre>,
            val inicios: List<Int>,
            val fechado: Boolean,
            val invertida: Boolean = false,
        ) : Passo()

        class AlteraBitmap(
            val id: Int,
            val bitmapAnterior: Bitmap,
        ) : Passo()

        /** Estado da moldura de transformar após Mover — toque fora / troca de ferramenta. */
        class ModoTransformarDaPeca(
            val idDaPeca: Int,
            val ativo: Boolean,
        ) : Passo()

        /** Preferência sem nova camada: funde a peça na origem ao encerrar a moldura. */
        class Opacidade(val id: Int, val valor: Float) : Passo()
        class MesclarCamadas(val cima: Peca, val baixo: Peca, val mesclada: Peca, val aplicado: Boolean) : Passo()
        /** Camada de desenho virou imagem para uma ação de pixels; desfaz junto com a ação que a pediu. */
        class Promocao(val folha: Peca.Folha, val imagem: Peca.Imagem, val aplicado: Boolean) : Passo()
        class FundirPecaTransformada(
            val idPeca: Int,
            val idOrigem: Int,
            val bitmapOrigemAntes: Bitmap,
            /** Geometria da origem antes de crescer para caber a peça (null se não cresceu). */
            val estadoOrigemAntes: EstadoDaPeca? = null,
        ) : Passo()
    }

    private enum class ModoDoDedo { NENHUM, ARRASTE, RECORTE, TOQUE, PINCA, IGNORAR, PECA, REGUA, SELECAO_LACO }

    private enum class ModoDaPeca { NENHUM, AGUARDANDO, AGUARDANDO_SELECAO, MOVER, REDIMENSIONAR, GIRAR }

    private enum class ModoDaMascaraDoLaco { NENHUM, MOVER, GIRAR }

    private class ChipDaSelecaoAtivaDoLaco(val acao: AcaoDaSelecaoAtivaDoLaco, val area: RectF)

    private enum class TipoDeAlca { MOVER, NOROESTE, NORDESTE, SUDOESTE, SUDESTE }

    private sealed class Arraste {
        class Novo(val x: Float, val y: Float) : Arraste()
        class Alca(val tipo: TipoDeAlca, val deslocamentoX: Float, val deslocamentoY: Float) : Arraste()
    }
}

/**
 * Foto/JPEG chega como bitmap "opaco" (hasAlpha = false) e a cópia herda isso. Borracha, laço e
 * recortes zeram pixels, que então não ficavam transparentes (na tela e no PNG do rascunho apareciam
 * como cor sólida). Toda cópia editável de camada passa a aceitar transparência.
 */
private fun Bitmap.comTransparencia(): Bitmap {
    setHasAlpha(true)
    return this
}

package com.chromebrothers.navegador

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.chromebrothers.navegador.regras.RecorteDeTrechoDeVideo

/**
 * Tesoura no modo de progresso fino: marcar início/fim, ver o trecho e salvar o recorte na Galeria
 * (corte inteligente; sem reencodar o vídeo inteiro). Só aparece quando [obterArquivo] devolve um
 * arquivo do aparelho.
 */
class ModoDeRecorteDeTrechoNoScrubFinoDoPlayer(
    private val activity: Activity,
    painel: View,
    private val obterArquivo: () -> String?,
    private val obterPosicaoMs: () -> Long,
    private val obterDuracaoMs: () -> Long,
    private val seekParaMs: (Long) -> Unit,
    private val tocar: () -> Unit,
    private val pausar: () -> Unit,
    private val estaTocando: () -> Boolean,
    /** Posição real do player (para parar o "ver trecho" em cima do fim). */
    private val obterPosicaoDoPlayerMs: () -> Long = obterPosicaoMs,
) {
    private val regra = RecorteDeTrechoDeVideo
    private val principal = Handler(Looper.getMainLooper())
    private val recortador = RecortadorDeTrechoDeVideoSemReencode(activity)
    private val botaoTesoura: ImageButton =
        painel.findViewById(R.id.botao_abrir_recorte_de_trecho_do_scrub_fino_do_player_de_video)
    private val area: View = painel.findViewById(R.id.painel_do_recorte_de_trecho_do_scrub_fino_do_player_de_video)
    private val barra: BarraDoTrechoDoRecorteDeVideo =
        painel.findViewById(R.id.barra_do_trecho_do_recorte_do_scrub_fino_do_player_de_video)
    private val resumo: TextView = painel.findViewById(R.id.resumo_do_recorte_de_trecho_do_scrub_fino_do_player_de_video)
    private val botaoInicio: Button = painel.findViewById(R.id.botao_marcar_inicio_do_recorte_do_scrub_fino_do_player_de_video)
    private val botaoFim: Button = painel.findViewById(R.id.botao_marcar_fim_do_recorte_do_scrub_fino_do_player_de_video)
    private val botaoVer: Button = painel.findViewById(R.id.botao_ver_trecho_do_recorte_do_scrub_fino_do_player_de_video)
    private val botaoSalvar: Button = painel.findViewById(R.id.botao_salvar_recorte_do_scrub_fino_do_player_de_video)
    private val botaoFechar: Button = painel.findViewById(R.id.botao_fechar_recorte_do_scrub_fino_do_player_de_video)

    private var arquivo: String? = null
    private var tamanhoDoArquivo = -1L
    private var quadrosChave: List<Long> = emptyList()
    private var lendoQuadrosChave = false
    private var inicioMs: Long? = null
    private var fimMs: Long? = null
    private var salvando = false
    private var progressoDoSalvamento = 0
    private var fimDoTrechoEmExibicao: Long? = null
    /** O "ver trecho" já começou a tocar de fato (antes disso o player ainda está carregando após o seek). */
    private var trechoComecouATocar = false

    private val acompanhar = object : Runnable {
        override fun run() {
            if (area.visibility != View.VISIBLE) return
            barra.posicaoMs = obterPosicaoMs()
            val fim = fimDoTrechoEmExibicao
            if (fim != null) {
                val tocando = estaTocando()
                if (tocando) trechoComecouATocar = true
                when {
                    obterPosicaoDoPlayerMs() >= fim -> {
                        // Chegou no fim: para e deixa o vídeo exatamente na marca.
                        fimDoTrechoEmExibicao = null
                        pausar()
                        seekParaMs(fim)
                    }
                    // Só conta como "o usuário pausou" depois de ter começado a tocar.
                    trechoComecouATocar && !tocando -> fimDoTrechoEmExibicao = null
                }
            }
            // Rápido durante o trecho (para em cima do fim); calmo no resto.
            principal.postDelayed(this, if (fimDoTrechoEmExibicao != null) 30L else 100L)
        }
    }

    init {
        listOf(botaoInicio, botaoFim, botaoVer, botaoSalvar, botaoFechar).forEach {
            it.backgroundTintList = null
            it.stateListAnimator = null
        }
        botaoTesoura.setOnClickListener { if (area.visibility == View.VISIBLE) fechar() else abrir() }
        botaoInicio.setOnClickListener { marcar(inicio = true) }
        botaoFim.setOnClickListener { marcar(inicio = false) }
        botaoVer.setOnClickListener { verTrecho() }
        botaoSalvar.setOnClickListener { pedirNomeESalvar() }
        botaoFechar.setOnClickListener { fechar() }
        barra.aoEscolherTempo = { ms ->
            fimDoTrechoEmExibicao = null
            seekParaMs(ms)
        }
    }

    /** Entrou no modo de progresso fino: a tesoura aparece se o vídeo é um arquivo do aparelho. */
    fun aoEntrarNoScrub() {
        val atual = obterArquivo()?.takeIf { regra.podeRecortar(it) }
        botaoTesoura.visibility = if (atual != null) View.VISIBLE else View.GONE
        if (atual != arquivo) {
            // Outro vídeo: zera marcas e quadros-chave.
            arquivo = atual
            tamanhoDoArquivo = -1L
            quadrosChave = emptyList()
            inicioMs = null
            fimMs = null
        }
        if (area.visibility == View.VISIBLE) atualizar()
    }

    fun aoSairDoScrub() {
        fimDoTrechoEmExibicao = null
        area.visibility = View.GONE
        principal.removeCallbacks(acompanhar)
    }

    private fun abrir() {
        val atual = arquivo ?: return
        area.visibility = View.VISIBLE
        barra.duracaoMs = obterDuracaoMs()
        if (quadrosChave.isEmpty() && !lendoQuadrosChave) {
            lendoQuadrosChave = true
            Thread {
                val chaves = recortador.listarQuadrosChave(atual)
                val tamanho = recortador.tamanhoDoArquivo(atual)
                principal.post {
                    lendoQuadrosChave = false
                    if (arquivo != atual) return@post
                    quadrosChave = chaves
                    tamanhoDoArquivo = tamanho
                    atualizar()
                }
            }.start()
        }
        atualizar()
        principal.removeCallbacks(acompanhar)
        principal.post(acompanhar)
    }

    private fun fechar() {
        fimDoTrechoEmExibicao = null
        area.visibility = View.GONE
        principal.removeCallbacks(acompanhar)
    }

    private fun marcar(inicio: Boolean) {
        // Botão já marcado: o toque desmarca (para trocar o ponto, desmarca e marca de novo).
        if ((if (inicio) inicioMs else fimMs) != null) {
            if (inicio) inicioMs = null else fimMs = null
            fimDoTrechoEmExibicao = null
            val botao = if (inicio) botaoInicio else botaoFim
            botao.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
            atualizar()
            return
        }
        val agora = obterPosicaoMs().coerceIn(0L, obterDuracaoMs().coerceAtLeast(0L))
        if (inicio) inicioMs = agora else fimMs = agora
        // Marcas invertidas: troca, para o trecho sempre ir do menor ao maior.
        val a = inicioMs
        val b = fimMs
        if (a != null && b != null && a > b) {
            inicioMs = b
            fimMs = a
        }
        // Retorno na hora: vibra e o botão "pulsa" (antes parecia que nada acontecia).
        val botao = if (inicio) botaoInicio else botaoFim
        botao.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
        botao.animate().cancel()
        botao.scaleX = 1.12f
        botao.scaleY = 1.12f
        botao.animate().scaleX(1f).scaleY(1f).setDuration(180L).start()
        atualizar()
    }

    /** Botão marcado: tempo no texto e moldura amarela; desmarcado: o convite de sempre. */
    private fun pintarBotaoDeMarca(botao: Button, rotulo: String, marcaMs: Long?) {
        if (marcaMs != null) {
            botao.text = "$rotulo · ${regra.tempoComDecimos(marcaMs)}"
            botao.setBackgroundResource(R.drawable.fundo_do_botao_marcado_do_recorte_de_trecho_do_video)
            botao.setTextColor(android.graphics.Color.parseColor("#F5C518"))
        } else {
            botao.text = "Marcar ${rotulo.lowercase()}"
            botao.setBackgroundResource(R.drawable.fundo_do_campo_da_caixa_de_dialogo_do_editor_de_imagem)
            botao.setTextColor(android.graphics.Color.parseColor("#F4F7FB"))
        }
        botao.backgroundTintList = null
    }

    private fun verTrecho() {
        val a = inicioMs ?: return
        val b = fimMs ?: return
        seekParaMs(a)
        fimDoTrechoEmExibicao = b
        trechoComecouATocar = false
        tocar()
        principal.removeCallbacks(acompanhar)
        principal.post(acompanhar)
    }

    private fun pedirNomeESalvar() {
        val origem = arquivo ?: return
        val trecho = regra.trechoValido(inicioMs ?: return, fimMs ?: return, obterDuracaoMs()) ?: run {
            Toast.makeText(activity, "O trecho precisa ter pelo menos meio segundo.", Toast.LENGTH_SHORT).show()
            return
        }
        if (salvando) return
        pausar()
        fimDoTrechoEmExibicao = null
        val sugerido = regra.nomeSugerido(recortador.nomeDoArquivo(origem), trecho.first, trecho.second)
        CaixaDeConfirmacaoDoNavegador.pedirTexto(
            activity = activity,
            titulo = "Salvar recorte",
            mensagem = "Vai para a Galeria (Movies/ChromeBrothers).",
            valorInicial = sugerido,
            textoConfirmar = "Salvar",
        ) { digitado ->
            salvar(origem, trecho.first, trecho.second, regra.nomeFinal(digitado, sugerido))
        }
    }

    private fun salvar(origem: String, inicio: Long, fim: Long, nome: String) {
        salvando = true
        progressoDoSalvamento = 0
        atualizar()
        recortador.recortar(
            uri = origem,
            inicioMs = inicio,
            fimMs = fim,
            nomeDoArquivo = nome,
            quadrosChave = quadrosChave,
            aoProgresso = { progresso ->
                progressoDoSalvamento = progresso
                atualizar()
            },
            aoTerminar = { resultado ->
                salvando = false
                atualizar()
                resultado.onSuccess { pronto ->
                    val ajuste = if (pronto.inicioAjustado) {
                        " O início ficou em ${regra.tempoComDecimos(pronto.inicioEfetivoMs)} (quadro-chave)."
                    } else {
                        ""
                    }
                    Toast.makeText(activity, "Recorte salvo na Galeria: $nome.$ajuste", Toast.LENGTH_LONG).show()
                }.onFailure { erro ->
                    Toast.makeText(activity, "Não consegui salvar o recorte: ${erro.message ?: "erro desconhecido"}", Toast.LENGTH_LONG).show()
                }
            },
        )
    }

    private fun atualizar() {
        val duracao = obterDuracaoMs()
        barra.duracaoMs = duracao
        barra.quadrosChave = quadrosChave
        barra.inicioMs = inicioMs
        barra.fimMs = fimMs
        barra.posicaoMs = obterPosicaoMs()
        val a = inicioMs
        val b = fimMs
        pintarBotaoDeMarca(botaoInicio, "Início", a)
        pintarBotaoDeMarca(botaoFim, "Fim", b)
        val temTrecho = a != null && b != null && regra.trechoValido(a, b, duracao) != null
        botaoVer.isEnabled = temTrecho && !salvando
        botaoSalvar.isEnabled = temTrecho && !salvando
        botaoInicio.isEnabled = !salvando
        botaoFim.isEnabled = !salvando
        listOf(botaoVer, botaoSalvar, botaoInicio, botaoFim).forEach { it.alpha = if (it.isEnabled) 1f else 0.4f }
        resumo.text = when {
            salvando -> "Salvando recorte… $progressoDoSalvamento%"
            a == null && b == null -> "Mova o vídeo e marque o início e o fim do trecho." +
                if (lendoQuadrosChave) " (lendo quadros-chave…)" else ""
            a == null -> "Fim em ${regra.tempoComDecimos(b!!)} · falta marcar o início."
            b == null -> "Início em ${regra.tempoComDecimos(a)} · falta marcar o fim."
            else -> {
                val tamanho = regra.tamanhoEstimadoBytes(tamanhoDoArquivo, duracao, a, b)
                val corte = if (quadrosChave.isNotEmpty() && !regra.inicioJaEhQuadroChave(quadrosChave, a)) {
                    " · início exato, sem reencode"
                } else {
                    ""
                }
                "${regra.tempoComDecimos(a)} → ${regra.tempoComDecimos(b)} · " +
                    "${regra.tempoComDecimos(b - a + 50)} · ~${regra.tamanhoLegivel(tamanho)}$corte"
            }
        }
    }
}

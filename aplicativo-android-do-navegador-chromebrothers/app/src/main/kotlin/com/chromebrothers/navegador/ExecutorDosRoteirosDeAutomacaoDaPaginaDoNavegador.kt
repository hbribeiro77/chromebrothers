package com.chromebrothers.navegador

import android.os.Handler
import android.os.Looper
import com.chromebrothers.navegador.regras.PassoDoRoteiroDeAutomacao
import com.chromebrothers.navegador.regras.RoteiroDeAutomacaoDaPagina
import com.chromebrothers.navegador.regras.RoteiroDeAutomacaoDaPaginaDoNavegador
import com.chromebrothers.navegador.regras.ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina
import com.chromebrothers.navegador.regras.TextoJsonDoChat
import com.chromebrothers.navegador.regras.VariaveisDoRoteiroDeAutomacaoDaPagina

/**
 * Executa um roteiro na UI thread, sem IA.
 */
class ExecutorDosRoteirosDeAutomacaoDaPaginaDoNavegador(
    private val handler: Handler = Handler(Looper.getMainLooper()),
    private val abrirEndereco: (url: String, aoFim: (String) -> Unit) -> Unit,
    private val avaliarJavascript: (script: String, aoReceber: (String) -> Unit) -> Unit,
    private val textoDoJavascript: (String?) -> String,
    private val urlAtual: () -> String = { "" },
    private val aoPasso: ((indice: Int, total: Int, rotulo: String) -> Unit)? = null,
    variaveisIniciais: Map<String, String> = emptyMap(),
) {
    private var cancelado = false
    private var execucoesDePasso = 0
    private val variaveis = variaveisIniciais.toMutableMap()

    fun cancelar() {
        cancelado = true
    }

    fun executar(roteiro: RoteiroDeAutomacaoDaPagina, aoFim: (String) -> Unit) {
        cancelado = false
        execucoesDePasso = 0
        executarPasso(roteiro, 0, aoFim)
    }

    private fun executarPasso(
        roteiro: RoteiroDeAutomacaoDaPagina,
        indice: Int,
        aoFim: (String) -> Unit,
    ) {
        if (cancelado) {
            aoFim("Roteiro cancelado no passo ${indice + 1}.")
            return
        }
        if (indice >= roteiro.passos.size) {
            aoFim("Roteiro «${roteiro.nome}» concluído (${roteiro.passos.size} passos).")
            return
        }
        execucoesDePasso++
        if (execucoesDePasso > 200) {
            aoFim("Abortado: possível loop no ir_para (mais de 200 passos executados).")
            return
        }
        val passo = roteiro.passos[indice]
        aoPasso?.invoke(
            indice,
            roteiro.passos.size,
            RoteiroDeAutomacaoDaPaginaDoNavegador.rotuloDoPasso(passo),
        )
        when (passo) {
            is PassoDoRoteiroDeAutomacao.Abrir -> {
                val url = VariaveisDoRoteiroDeAutomacaoDaPagina.substituir(passo.url, variaveis)
                val faltam = VariaveisDoRoteiroDeAutomacaoDaPagina.faltando(passo.url, variaveis)
                if (faltam.isNotEmpty()) {
                    aoFim("Faltou variável {{${faltam.first()}}} no passo ${indice + 1}.")
                    return
                }
                abrirEndereco(url) { _ ->
                    if (cancelado) {
                        aoFim("Roteiro cancelado após abrir.")
                        return@abrirEndereco
                    }
                    executarPasso(roteiro, indice + 1, aoFim)
                }
            }
            is PassoDoRoteiroDeAutomacao.Aguardar -> {
                pollingAte(
                    timeoutMs = passo.timeoutMs,
                    aoFim = aoFim,
                    rotuloCancel = "aguardar",
                    script = {
                        ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.aguardar(passo.seletor, passo.texto)
                    },
                    aoOk = { _ -> executarPasso(roteiro, indice + 1, aoFim) },
                    aoTimeout = {
                        val alvo = passo.texto.ifBlank { passo.seletor }
                        aoFim("Timeout no passo ${indice + 1}: não achei «$alvo».")
                    },
                )
            }
            is PassoDoRoteiroDeAutomacao.AguardarSumir -> {
                pollingAte(
                    timeoutMs = passo.timeoutMs,
                    aoFim = aoFim,
                    rotuloCancel = "aguardar_sumir",
                    script = {
                        ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.aguardarSumir(passo.seletor, passo.texto)
                    },
                    aoOk = { _ -> executarPasso(roteiro, indice + 1, aoFim) },
                    aoTimeout = {
                        val alvo = passo.texto.ifBlank { passo.seletor }
                        aoFim("Timeout no passo ${indice + 1}: «$alvo» não sumiu.")
                    },
                )
            }
            is PassoDoRoteiroDeAutomacao.AguardarUrl -> {
                val trechoBruto = passo.contem
                val faltam = VariaveisDoRoteiroDeAutomacaoDaPagina.faltando(trechoBruto, variaveis)
                if (faltam.isNotEmpty()) {
                    aoFim("Faltou variável {{${faltam.first()}}} no passo ${indice + 1}.")
                    return
                }
                val trecho = VariaveisDoRoteiroDeAutomacaoDaPagina.substituir(trechoBruto, variaveis)
                val inicio = System.currentTimeMillis()
                fun tentar() {
                    if (cancelado) {
                        aoFim("Roteiro cancelado no aguardar_url.")
                        return
                    }
                    if (urlAtual().contains(trecho, ignoreCase = true)) {
                        executarPasso(roteiro, indice + 1, aoFim)
                    } else if (System.currentTimeMillis() - inicio >= passo.timeoutMs) {
                        aoFim("Timeout no passo ${indice + 1}: URL não contém «$trecho».")
                    } else {
                        handler.postDelayed({ tentar() }, 250L)
                    }
                }
                tentar()
            }
            is PassoDoRoteiroDeAutomacao.Clicar -> {
                val script = ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.clicar(
                    passo.seletor,
                    passo.texto,
                )
                avaliarJavascript(script) { bruto ->
                    val json = textoDoJavascript(bruto)
                    if (!scriptOk(json)) {
                        val alvo = passo.texto.ifBlank { passo.seletor }
                        val matches = TextoJsonDoChat.inteiroDaChave(json, "matches")
                        val extra = when {
                            matches == null -> ""
                            matches == 0 -> " (nenhum candidato)"
                            else -> " ($matches candidatos)"
                        }
                        aoFim("Falha no passo ${indice + 1}: não cliquei em «$alvo»$extra.")
                    } else {
                        handler.postDelayed({
                            executarPasso(roteiro, indice + 1, aoFim)
                        }, 350L)
                    }
                }
            }
            is PassoDoRoteiroDeAutomacao.Preencher -> {
                val faltam = VariaveisDoRoteiroDeAutomacaoDaPagina.faltando(passo.valor, variaveis)
                if (faltam.isNotEmpty()) {
                    aoFim("Faltou variável {{${faltam.first()}}} no passo ${indice + 1}.")
                    return
                }
                val valor = VariaveisDoRoteiroDeAutomacaoDaPagina.substituir(passo.valor, variaveis)
                val script = ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.preencher(
                    passo.seletor,
                    passo.texto,
                    valor,
                )
                avaliarJavascript(script) { bruto ->
                    val json = textoDoJavascript(bruto)
                    if (!scriptOk(json)) {
                        val alvo = passo.texto.ifBlank { passo.seletor }
                        aoFim("Falha no passo ${indice + 1}: não preenchi «$alvo».")
                    } else {
                        executarPasso(roteiro, indice + 1, aoFim)
                    }
                }
            }
            is PassoDoRoteiroDeAutomacao.EsperarMs -> {
                handler.postDelayed({
                    if (cancelado) {
                        aoFim("Roteiro cancelado na espera.")
                        return@postDelayed
                    }
                    executarPasso(roteiro, indice + 1, aoFim)
                }, passo.ms.toLong())
            }
            is PassoDoRoteiroDeAutomacao.Rolar -> {
                val script = ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.rolar(
                    passo.seletor,
                    passo.pixels,
                )
                avaliarJavascript(script) { bruto ->
                    val json = textoDoJavascript(bruto)
                    if (!scriptOk(json)) {
                        aoFim("Falha no passo ${indice + 1}: não rolei a página.")
                    } else {
                        executarPasso(roteiro, indice + 1, aoFim)
                    }
                }
            }
            is PassoDoRoteiroDeAutomacao.ClicarSe -> {
                val script = ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.clicarSe(
                    passo.seletor,
                    passo.indice,
                    passo.contem,
                )
                avaliarJavascript(script) { bruto ->
                    val json = textoDoJavascript(bruto)
                    if (scriptOk(json)) {
                        handler.postDelayed({
                            executarPasso(roteiro, indice + 1, aoFim)
                        }, 350L)
                        return@avaliarJavascript
                    }
                    val motivo = TextoJsonDoChat.valorDaChave(json, "motivo").orEmpty()
                    val condicional = motivo == "nao_bateu" || motivo == "nao_achou"
                    if (passo.seNao == "seguir" && condicional) {
                        executarPasso(roteiro, indice + 1, aoFim)
                    } else {
                        aoFim(
                            "Falha no passo ${indice + 1}: clicar_se não bateu " +
                                "(índice ${passo.indice}, contem «${passo.contem}»).",
                        )
                    }
                }
            }
            is PassoDoRoteiroDeAutomacao.Assertar -> {
                val texto = VariaveisDoRoteiroDeAutomacaoDaPagina.substituir(passo.texto, variaveis)
                val contem = VariaveisDoRoteiroDeAutomacaoDaPagina.substituir(passo.contem, variaveis)
                val faltam = VariaveisDoRoteiroDeAutomacaoDaPagina.faltando(passo.texto, variaveis) +
                    VariaveisDoRoteiroDeAutomacaoDaPagina.faltando(passo.contem, variaveis)
                if (faltam.isNotEmpty()) {
                    aoFim("Faltou variável {{${faltam.first()}}} no passo ${indice + 1}.")
                    return
                }
                val script = ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.assertar(
                    passo.seletor,
                    texto,
                    contem,
                )
                avaliarJavascript(script) { bruto ->
                    val json = textoDoJavascript(bruto)
                    if (!scriptOk(json)) {
                        val motivo = TextoJsonDoChat.valorDaChave(json, "motivo").orEmpty()
                        val alvo = passo.texto.ifBlank { passo.seletor }
                        aoFim("Assert falhou no passo ${indice + 1}: «$alvo» ($motivo).")
                    } else {
                        executarPasso(roteiro, indice + 1, aoFim)
                    }
                }
            }
            is PassoDoRoteiroDeAutomacao.Extrair -> {
                val script = ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.extrair(
                    passo.seletor,
                    passo.texto,
                )
                avaliarJavascript(script) { bruto ->
                    val json = textoDoJavascript(bruto)
                    if (!scriptOk(json)) {
                        val alvo = passo.texto.ifBlank { passo.seletor }
                        aoFim("Falha no passo ${indice + 1}: não extraí «$alvo».")
                    } else {
                        val lido = TextoJsonDoChat.valorDaChave(json, "texto").orEmpty()
                        variaveis[passo.variavel] = lido
                        executarPasso(roteiro, indice + 1, aoFim)
                    }
                }
            }
            is PassoDoRoteiroDeAutomacao.AguardarQualquer -> {
                val ids = passo.opcoes.joinToString(", ") { it.id }
                pollingAte(
                    timeoutMs = passo.timeoutMs,
                    aoFim = aoFim,
                    rotuloCancel = "aguardar_qualquer",
                    script = {
                        ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.aguardarQualquer(passo.opcoes)
                    },
                    aoOk = { json ->
                        val id = TextoJsonDoChat.valorDaChave(json, "id").orEmpty()
                        if (id.isEmpty()) {
                            aoFim("Falha no passo ${indice + 1}: aguardar_qualquer sem id.")
                        } else {
                            variaveis[passo.variavel] = id
                            executarPasso(roteiro, indice + 1, aoFim)
                        }
                    },
                    aoTimeout = {
                        aoFim("Timeout no passo ${indice + 1}: nenhum de [$ids].")
                    },
                )
            }
            is PassoDoRoteiroDeAutomacao.Marcar -> {
                executarPasso(roteiro, indice + 1, aoFim)
            }
            is PassoDoRoteiroDeAutomacao.IrPara -> {
                if (!RoteiroDeAutomacaoDaPaginaDoNavegador.condicaoIrParaBate(passo.se, variaveis)) {
                    executarPasso(roteiro, indice + 1, aoFim)
                    return
                }
                val alvo = RoteiroDeAutomacaoDaPaginaDoNavegador.indiceDoDestino(
                    roteiro.passos,
                    passo.passo,
                    passo.destino,
                )
                if (alvo == null) {
                    val nome = passo.destino.ifBlank { "#${passo.passo}" }
                    aoFim("Falha no passo ${indice + 1}: destino «$nome» não existe.")
                } else {
                    executarPasso(roteiro, alvo, aoFim)
                }
            }
        }
    }

    private fun pollingAte(
        timeoutMs: Int,
        aoFim: (String) -> Unit,
        rotuloCancel: String,
        script: () -> String,
        aoOk: (json: String) -> Unit,
        aoTimeout: () -> Unit,
    ) {
        val inicio = System.currentTimeMillis()
        fun tentar() {
            if (cancelado) {
                aoFim("Roteiro cancelado no $rotuloCancel.")
                return
            }
            avaliarJavascript(script()) { bruto ->
                val json = textoDoJavascript(bruto)
                if (scriptOk(json)) {
                    aoOk(json)
                } else if (System.currentTimeMillis() - inicio >= timeoutMs) {
                    aoTimeout()
                } else {
                    handler.postDelayed({ tentar() }, 250L)
                }
            }
        }
        tentar()
    }

    private fun scriptOk(json: String): Boolean {
        if (json.isBlank()) return false
        val ok = TextoJsonDoChat.valorDaChave(json, "ok")
        if (ok != null) return ok.equals("true", ignoreCase = true)
        return json.contains("\"ok\":true") || json.contains("\"ok\": true")
    }
}

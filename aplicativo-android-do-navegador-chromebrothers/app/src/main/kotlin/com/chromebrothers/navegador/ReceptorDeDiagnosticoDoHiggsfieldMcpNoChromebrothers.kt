package com.chromebrothers.navegador

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Disparo via adb para testar o MCP Higgsfield sem depender do chat:
 * adb shell am broadcast -a com.chromebrothers.navegador.DIAGNOSTICO_HIGGSFIELD_MCP
 *   -n com.chromebrothers.navegador/.ReceptorDeDiagnosticoDoHiggsfieldMcpNoChromebrothers
 */
class ReceptorDeDiagnosticoDoHiggsfieldMcpNoChromebrothers : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendente = goAsync()
        Thread {
            try {
                val sessao = SessaoDaContaHiggsfieldNoAparelho(context.applicationContext)
                val cliente = ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers(
                    context.applicationContext,
                    sessao,
                )
                val acao = intent?.getStringExtra("acao").orEmpty().trim().lowercase()
                val resumo = if (acao == "resgatar") {
                    resgatarJob(context, cliente, intent?.getStringExtra("job").orEmpty().trim())
                } else if (acao == "historico") {
                    val tamanho = intent?.getStringExtra("tamanho")?.toIntOrNull() ?: 5
                    val texto = cliente.listarGeracoesRecentesBruto(intent?.getStringExtra("tipo") ?: "video", tamanho)
                    java.io.File(context.cacheDir, "higgsfield-historico.json").writeText(texto)
                    texto
                } else if (acao == "ferramentas" || acao == "tools") {
                    val texto = cliente.diagnosticarFerramentas()
                    java.io.File(context.cacheDir, "higgsfield-ferramentas.json").writeText(texto)
                    texto
                } else if (acao == "schema_ref" || acao == "ref") {
                    cliente.diagnosticarSchemasDeReferenciaDeImagem(
                        intent?.getStringExtra("modelo") ?: "nano_banana_2_1",
                    )
                } else if (acao == "listar" || acao == "modelos") {
                    val filtro = intent?.getStringExtra("filtro").orEmpty()
                    val texto = cliente.listarModelos(filtro)
                    java.io.File(context.cacheDir, "higgsfield-modelos-listagem.txt")
                        .writeText(texto)
                    texto
                } else if (acao == "gerar_com_referencia" || acao == "img2img") {
                    val prompt = intent?.getStringExtra("prompt")
                        ?: "Same subject, wearing a simple black eye mask, photorealistic"
                    val modelo = intent?.getStringExtra("modelo") ?: "nano_banana_2_1"
                    cliente.diagnosticarGeracaoComReferenciaDeTeste(prompt, modelo)
                } else {
                    val prompt = intent?.getStringExtra("prompt")
                        ?: "Um círculo azul sólido centralizado em fundo branco puro, minimalista"
                    val modelo = intent?.getStringExtra("modelo")
                        ?: com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat.MODELO_PADRAO_IMAGEM
                    cliente.diagnosticarPayloadsEGerarImagemDeTeste(prompt, modelo)
                }
                Log.i(TAG, resumo.take(4000))
            } catch (e: Exception) {
                Log.e(TAG, "diagnostico falhou: ${e.message}", e)
            } finally {
                pendente.finish()
            }
        }.start()
    }

    /**
     * Baixa um job do Higgsfield que ficou sem card (resposta da criação perdida) e salva na biblioteca,
     * com prompt e modelo lidos do histórico. Resultado em cache/higgsfield-resgate.txt.
     */
    private fun resgatarJob(
        context: Context,
        cliente: ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers,
        jobId: String,
    ): String {
        val saida = java.io.File(context.cacheDir, "higgsfield-resgate.txt")
        val texto = run {
            if (jobId.isBlank()) return@run "erro: informe --es job <id>"
            val item = runCatching {
                val itens = org.json.JSONObject(cliente.listarGeracoesRecentesBruto("video", 100))
                    .optJSONObject("structuredContent")?.optJSONArray("items")
                (0 until (itens?.length() ?: 0)).mapNotNull { itens?.optJSONObject(it) }.firstOrNull { it.optString("id") == jobId }
            }.getOrNull()
            val prompt = item?.optJSONObject("params")?.optString("prompt").orEmpty()
            val modelo = item?.optString("model").orEmpty()
            when (val consulta = cliente.consultarVideoDoJobUmaVez(jobId)) {
                is ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Pronto -> {
                    val guardado = GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers(context)
                        .guardarVideoComMetadados(consulta.bytes, prompt, "higgsfield", modelo)
                    if (guardado == null) "erro: baixei ${consulta.bytes.size} bytes mas não salvei na biblioteca"
                    else "ok: ${guardado.id} (${consulta.bytes.size} bytes) prompt=${prompt.take(60)}"
                }
                ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.AindaGerando -> "ainda gerando"
                is ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou -> "erro: ${consulta.mensagem}"
            }
        }
        saida.writeText(texto)
        return texto
    }

    companion object {
        private const val TAG = "HiggsfieldCB"
        const val ACAO = "com.chromebrothers.navegador.DIAGNOSTICO_HIGGSFIELD_MCP"
    }
}

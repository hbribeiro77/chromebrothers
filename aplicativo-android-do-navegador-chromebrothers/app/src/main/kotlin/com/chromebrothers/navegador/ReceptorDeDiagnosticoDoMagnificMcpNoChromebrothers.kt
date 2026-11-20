package com.chromebrothers.navegador

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Diagnóstico do MCP Magnific via adb (só ações de leitura, nunca gera nem gasta crédito):
 * adb shell am broadcast -a com.chromebrothers.navegador.DIAGNOSTICO_MAGNIFIC_MCP
 *   -n com.chromebrothers.navegador/.ReceptorDeDiagnosticoDoMagnificMcpNoChromebrothers --es acao modelos
 * Saída em cache/magnific-mcp-diag.txt. Protegido por android.permission.DUMP (só o shell do adb tem).
 */
class ReceptorDeDiagnosticoDoMagnificMcpNoChromebrothers : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendente = goAsync()
        Thread {
            try {
                val cliente = ClienteMcpDoMagnificParaGeracaoNoChromebrothers(
                    context.applicationContext,
                    SessaoDaContaMagnificNoAparelho(context.applicationContext),
                )
                val resumo = when (intent?.getStringExtra("acao").orEmpty().trim().lowercase()) {
                    "modelos" -> {
                        val img = cliente.listarModelosBruto(ClienteMcpDoMagnificParaGeracaoNoChromebrothers.TOOL_IMAGES_MODELS_LIST)
                        val vid = cliente.listarModelosBruto(ClienteMcpDoMagnificParaGeracaoNoChromebrothers.TOOL_VIDEO_MODELS_LIST)
                        "modelos ok: imagem=${img.length} vídeo=${vid.length} caracteres"
                    }
                    // Só ferramentas grátis (lista fechada no cliente). Ex.: --es ferramenta account_balance --es args '{}'
                    "chamar" -> cliente.chamarParaDiagnostico(
                        intent?.getStringExtra("ferramenta").orEmpty(),
                        org.json.JSONObject(intent?.getStringExtra("args") ?: "{}"),
                    ).take(500)
                    // PNG 8x8 de teste no endereço de upload devolvido por creations_request_upload.
                    "put" -> {
                        val bmp = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
                        bmp.eraseColor(android.graphics.Color.GRAY)
                        val saida = java.io.ByteArrayOutputStream()
                        bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, saida)
                        "PUT HTTP " + cliente.enviarBytesParaDiagnostico(
                            intent?.getStringExtra("url").orEmpty(),
                            saida.toByteArray(),
                            "image/png",
                        )
                    }
                    else -> cliente.diagnosticar()
                }
                Log.i(TAG, resumo)
            } catch (e: Exception) {
                Log.e(TAG, "diagnóstico falhou: ${e.message}", e)
            } finally {
                pendente.finish()
            }
        }.start()
    }

    companion object {
        private const val TAG = "MagnificCB"
    }
}

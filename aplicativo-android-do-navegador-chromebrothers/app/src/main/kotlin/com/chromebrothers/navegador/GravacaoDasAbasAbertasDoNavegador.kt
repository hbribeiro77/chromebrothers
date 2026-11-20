package com.chromebrothers.navegador

import android.content.Context
import android.util.AtomicFile
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors

/** Uma aba como foi salva: endereço, título e o estado do Gecko (histórico de voltar/avançar). */
data class AbaSalvaDoNavegador(
    val titulo: String,
    val endereco: String,
    val estadoDaSessao: String?,
)

data class JanelaSalvaDoNavegador(
    val abas: List<AbaSalvaDoNavegador>,
    val indiceDaAtiva: Int,
)

/**
 * Guarda as abas abertas de cada janela (slot) num arquivo próprio, para reabrir tudo
 * na próxima vez que o app abrir — como o Chrome.
 */
class GravacaoDasAbasAbertasDoNavegador(context: Context, slot: Int) {
    private val arquivo = AtomicFile(
        File(File(context.filesDir, "abas-abertas").apply { mkdirs() }, "janela-$slot.json"),
    )

    fun ler(): JanelaSalvaDoNavegador? = runCatching {
        if (!arquivo.baseFile.exists()) return null
        val json = JSONObject(arquivo.readFully().toString(Charsets.UTF_8))
        val lista = json.optJSONArray("abas") ?: JSONArray()
        val abas = (0 until lista.length()).mapNotNull { indice ->
            val item = lista.optJSONObject(indice) ?: return@mapNotNull null
            val endereco = item.optString("endereco").trim()
            if (endereco.isEmpty()) return@mapNotNull null
            AbaSalvaDoNavegador(
                titulo = item.optString("titulo"),
                endereco = endereco,
                estadoDaSessao = item.optString("estado").takeIf { it.isNotBlank() },
            )
        }
        JanelaSalvaDoNavegador(abas, json.optInt("ativa", 0))
    }.onFailure { Log.w(TAG, "falha ao ler abas salvas: $it") }.getOrNull()

    fun gravar(janela: JanelaSalvaDoNavegador) {
        val texto = JSONObject()
            .put("versao", 1)
            .put("ativa", janela.indiceDaAtiva)
            .put(
                "abas",
                JSONArray().apply {
                    janela.abas.forEach { aba ->
                        put(
                            JSONObject()
                                .put("titulo", aba.titulo)
                                .put("endereco", aba.endereco)
                                .apply { aba.estadoDaSessao?.let { put("estado", it) } },
                        )
                    }
                },
            )
            .toString()
        gravador.execute {
            var saida: java.io.FileOutputStream? = null
            try {
                saida = arquivo.startWrite()
                saida.write(texto.toByteArray(Charsets.UTF_8))
                arquivo.finishWrite(saida)
            } catch (erro: Exception) {
                saida?.let { arquivo.failWrite(it) }
                Log.w(TAG, "falha ao salvar abas: $erro")
            }
        }
    }

    fun apagar() {
        gravador.execute { runCatching { arquivo.delete() } }
    }

    companion object {
        private const val TAG = "ChromeBrothersAbas"
        private val gravador = Executors.newSingleThreadExecutor { r -> Thread(r, "abas-salvas") }
    }
}

package com.chromebrothers.navegador

import android.os.Handler
import android.os.Looper
import com.chromebrothers.navegador.regras.MotorDeBuscaDaBarraDeEndereco
import com.chromebrothers.navegador.regras.SugestoesDePesquisaDoMotorDeBusca
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * Busca as sugestões de pesquisa do motor escolhido enquanto se digita na barra.
 * Só a resposta do pedido mais recente é entregue; respostas recentes ficam num cache curto
 * para não piscar ao apagar/redigitar.
 */
class BuscadorDeSugestoesDePesquisaDoNavegador {
    private val principal = Handler(Looper.getMainLooper())
    private val rede = Executors.newSingleThreadExecutor { r -> Thread(r, "sugestoes-de-pesquisa") }
    private val ultimoPedido = AtomicLong(0)
    private val cache = object : LinkedHashMap<String, List<String>>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<String>>?) = size > 60
    }
    private var agendado: Runnable? = null

    /** Resultado já conhecido para este texto (thread principal). */
    fun emCache(motor: MotorDeBuscaDaBarraDeEndereco, texto: String): List<String>? =
        cache[chave(motor, texto)]

    fun pedir(
        motor: MotorDeBuscaDaBarraDeEndereco,
        texto: String,
        aoReceber: (texto: String, sugestoes: List<String>) -> Unit,
    ) {
        agendado?.let { principal.removeCallbacks(it) }
        val limpo = texto.trim()
        if (!SugestoesDePesquisaDoMotorDeBusca.deveConsultar(limpo)) return
        val numero = ultimoPedido.incrementAndGet()
        val tarefa = Runnable {
            rede.execute {
                if (numero != ultimoPedido.get()) return@execute
                val lidas = runCatching { baixar(motor, limpo) }.getOrNull() ?: return@execute
                principal.post {
                    cache[chave(motor, limpo)] = lidas
                    if (numero == ultimoPedido.get()) aoReceber(limpo, lidas)
                }
            }
        }
        agendado = tarefa
        principal.postDelayed(tarefa, ATRASO_MS)
    }

    fun cancelar() {
        agendado?.let { principal.removeCallbacks(it) }
        agendado = null
        ultimoPedido.incrementAndGet()
    }

    private fun baixar(motor: MotorDeBuscaDaBarraDeEndereco, texto: String): List<String> {
        val conexao = URL(SugestoesDePesquisaDoMotorDeBusca.enderecoDaConsulta(motor, texto))
            .openConnection() as HttpURLConnection
        return try {
            conexao.connectTimeout = TEMPO_LIMITE_MS
            conexao.readTimeout = TEMPO_LIMITE_MS
            conexao.setRequestProperty("Accept", "application/json")
            if (conexao.responseCode !in 200..299) return emptyList()
            val corpo = conexao.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            SugestoesDePesquisaDoMotorDeBusca.ler(corpo, texto)
        } finally {
            conexao.disconnect()
        }
    }

    private fun chave(motor: MotorDeBuscaDaBarraDeEndereco, texto: String) =
        motor.id + "\u0000" + texto.trim().lowercase()

    companion object {
        private const val ATRASO_MS = 120L
        private const val TEMPO_LIMITE_MS = 2_500
    }
}

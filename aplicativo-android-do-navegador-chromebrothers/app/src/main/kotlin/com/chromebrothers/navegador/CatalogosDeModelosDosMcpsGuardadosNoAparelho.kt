package com.chromebrothers.navegador

import android.content.Context
import com.chromebrothers.navegador.regras.ValidadeDosCatalogosDeModelosDosMcps

/**
 * Listas de modelos dos MCPs guardadas em disco (sobrevivem ao app ser fechado).
 * Genérico: cada catálogo tem um id (ex. `higgsfield-imagem`) e uma função de busca.
 */
class CatalogosDeModelosDosMcpsGuardadosNoAparelho(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("catalogos-de-modelos-dos-mcps", Context.MODE_PRIVATE)
    private val catalogos = linkedMapOf<String, Catalogo>()

    inner class Catalogo(val id: String, private val buscar: () -> String) {
        private val travaDaBusca = Any()

        private fun guardado(): String? = prefs.getString("json:$id", null)
        private fun guardadoEmMs(): Long = prefs.getLong("em:$id", 0L)

        fun estaVencido(agoraMs: Long = System.currentTimeMillis()): Boolean =
            guardado() == null || ValidadeDosCatalogosDeModelosDosMcps.vencido(guardadoEmMs(), agoraMs)

        /**
         * Usa o guardado; só vai à rede se não houver nada guardado (ou [forcar]).
         * Busca que falha devolve a lista antiga, se houver.
         */
        fun obter(forcar: Boolean = false): String {
            if (!forcar) guardado()?.let { return it }
            synchronized(travaDaBusca) {
                if (!forcar) guardado()?.let { return it }
                val novo = buscar()
                if (ValidadeDosCatalogosDeModelosDosMcps.valeGuardar(novo)) {
                    prefs.edit()
                        .putString("json:$id", novo)
                        .putLong("em:$id", System.currentTimeMillis())
                        .apply()
                    return novo
                }
                return guardado() ?: novo
            }
        }

        /** Para a busca automática: só vai à rede quando vencido. */
        fun atualizarSeVencido() {
            if (estaVencido()) obter(forcar = true)
        }
    }

    fun registrar(id: String, buscar: () -> String): Catalogo =
        Catalogo(id, buscar).also { synchronized(catalogos) { catalogos[id] = it } }

    private fun todos(): List<Catalogo> = synchronized(catalogos) { catalogos.values.toList() }

    /** Início do app / abertura da gaveta: busca em segundo plano só o que passou de 6 h. */
    fun atualizarVencidosEmSegundoPlano(podeBuscar: (String) -> Boolean) {
        val vencidos = todos().filter { podeBuscar(it.id) && it.estaVencido() }
        if (vencidos.isEmpty() || !emAndamento.compareAndSet(false, true)) return
        Thread {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            try {
                vencidos.forEach { runCatching { it.atualizarSeVencido() } }
            } finally {
                emAndamento.set(false)
            }
        }.apply { name = "catalogos-mcp"; isDaemon = true }.start()
    }

    /** Menu "Atualizar listas de modelos": apaga tudo e busca de novo. [aoTerminar] recebe quantas deram certo. */
    fun apagarEBuscarDeNovo(podeBuscar: (String) -> Boolean, aoTerminar: (ok: Int, total: Int) -> Unit) {
        prefs.edit().clear().apply()
        val alvo = todos().filter { podeBuscar(it.id) }
        Thread {
            var ok = 0
            alvo.forEach { c ->
                val json = runCatching { c.obter(forcar = true) }.getOrNull().orEmpty()
                if (ValidadeDosCatalogosDeModelosDosMcps.valeGuardar(json)) ok++
            }
            aoTerminar(ok, alvo.size)
        }.apply { name = "catalogos-mcp-forcado"; isDaemon = true }.start()
    }

    private val emAndamento = java.util.concurrent.atomic.AtomicBoolean(false)
}

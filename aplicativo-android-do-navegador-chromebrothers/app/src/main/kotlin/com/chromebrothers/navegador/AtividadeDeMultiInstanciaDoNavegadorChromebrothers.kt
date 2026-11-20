package com.chromebrothers.navegador

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chromebrothers.navegador.regras.EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers
import com.chromebrothers.navegador.regras.FlagsDaNovaJanelaDocumentCentricDoNavegador
import com.chromebrothers.navegador.regras.LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers

/**
 * Trampolim no padrão Samsung Internet ([MultiInstanceActivity]): o System UI / multi-window
 * Samsung redireciona a segunda instância para cá via
 * `com.samsung.android.multiwindow.activity.alias.targetactivity`.
 */
class AtividadeDeMultiInstanciaDoNavegadorChromebrothers : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ocupados = slotsOcupados()
        val quantidade = ocupados.size.coerceAtLeast(contarTarefasDasJanelas())
        if (!LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.podeAbrir(quantidade)) {
            Toast.makeText(this, R.string.limite_de_janelas_do_navegador_atingido, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        // No trampolim Samsung, evitar reabrir a primária: preferir slot livre ≠ 0.
        val ocupadosParaNova = ocupados + EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_PRIMARIA
        val slot = EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.proximoSlotLivre(ocupadosParaNova)
            ?: EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.proximoSlotLivre(ocupados)
            ?: run {
                Toast.makeText(this, R.string.limite_de_janelas_do_navegador_atingido, Toast.LENGTH_LONG).show()
                finish()
                return
            }
        val classe = when (slot) {
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_TERCIARIA ->
                AtividadePrincipalTerciariaDoNavegadorChromebrothers::class.java
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_PRIMARIA ->
                AtividadePrincipalDoNavegadorChromebrothers::class.java
            else -> AtividadePrincipalSecundariaDoNavegadorChromebrothers::class.java
        }
        val pedido = Intent(this, classe).apply {
            val flagsDoTrampolim =
                FlagsDaNovaJanelaDocumentCentricDoNavegador.montarParaTrampolimQueCedeIdentidadeDaTarefaDoSplit()
            if (flagsDoTrampolim != 0) {
                addFlags(flagsDoTrampolim)
            }
            val endereco = intent
                ?.getStringExtra(LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.EXTRA_ENDERECO_INICIAL)
                ?.trim()
                .orEmpty()
            if (endereco.isNotEmpty()) {
                putExtra(
                    LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.EXTRA_ENDERECO_INICIAL,
                    endereco,
                )
            }
            data = intent?.data
            action = intent?.action
            intent?.extras?.let { putExtras(it) }
        }
        startActivity(pedido)
        finish()
    }

    private fun contarTarefasDasJanelas(): Int {
        val gerente = getSystemService(ActivityManager::class.java) ?: return 1
        return gerente.appTasks.count { tarefa ->
            val nome = componenteBase(tarefa)?.className ?: return@count false
            slotDaClasse(nome) != null
        }.coerceAtLeast(1)
    }

    private fun slotsOcupados(): Set<Int> {
        val gerente = getSystemService(ActivityManager::class.java) ?: return emptySet()
        val ocupados = linkedSetOf<Int>()
        for (tarefa in gerente.appTasks) {
            val nome = componenteBase(tarefa)?.className ?: continue
            slotDaClasse(nome)?.let { ocupados += it }
        }
        return ocupados
    }

    private fun componenteBase(tarefa: ActivityManager.AppTask): ComponentName? {
        val info = runCatching { tarefa.taskInfo }.getOrNull() ?: return null
        return info.baseActivity ?: info.topActivity
    }

    private fun slotDaClasse(nome: String): Int? = when (nome) {
        AtividadePrincipalDoNavegadorChromebrothers::class.java.name ->
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_PRIMARIA
        AtividadePrincipalSecundariaDoNavegadorChromebrothers::class.java.name ->
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_SECUNDARIA
        AtividadePrincipalTerciariaDoNavegadorChromebrothers::class.java.name ->
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_TERCIARIA
        else -> null
    }
}

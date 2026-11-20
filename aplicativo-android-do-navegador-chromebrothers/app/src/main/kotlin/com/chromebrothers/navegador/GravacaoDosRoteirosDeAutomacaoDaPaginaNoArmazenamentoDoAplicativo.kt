package com.chromebrothers.navegador

import android.content.Context
import com.chromebrothers.navegador.regras.ResultadoDoRoteiroDeAutomacao
import com.chromebrothers.navegador.regras.RoteiroDeAutomacaoDaPagina
import com.chromebrothers.navegador.regras.RoteiroDeAutomacaoDaPaginaDoNavegador
import java.io.File

class GravacaoDosRoteirosDeAutomacaoDaPaginaNoArmazenamentoDoAplicativo(
    private val pasta: File,
) {
    constructor(contexto: Context) : this(
        File(contexto.filesDir, "roteiros-de-automacao"),
    )

    fun listar(): List<RoteiroDeAutomacaoDaPagina> {
        if (!pasta.isDirectory) return emptyList()
        return pasta.listFiles { arquivo -> arquivo.isFile && arquivo.name.endsWith(".json") }
            ?.mapNotNull { arquivo ->
                try {
                    when (val r = RoteiroDeAutomacaoDaPaginaDoNavegador.ler(arquivo.readText())) {
                        is ResultadoDoRoteiroDeAutomacao.Ok ->
                            arquivo.lastModified() to r.roteiro
                        is ResultadoDoRoteiroDeAutomacao.Erro -> null
                    }
                } catch (_: Exception) {
                    null
                }
            }
            ?.sortedByDescending { it.first }
            ?.map { it.second }
            .orEmpty()
    }

    fun buscar(idOuNome: String): RoteiroDeAutomacaoDaPagina? {
        val chave = idOuNome.trim()
        if (chave.isEmpty()) return null
        val todos = listar()
        return todos.firstOrNull { it.id.equals(chave, ignoreCase = true) }
            ?: todos.firstOrNull { it.nome.equals(chave, ignoreCase = true) }
    }

    fun guardar(roteiro: RoteiroDeAutomacaoDaPagina): String {
        if (!idServeComoNomeDeArquivo(roteiro.id)) {
            return "Id do roteiro inválido para gravar."
        }
        pasta.mkdirs()
        File(pasta, "${roteiro.id}.json").writeText(
            RoteiroDeAutomacaoDaPaginaDoNavegador.escrever(roteiro),
        )
        return "Guardei o roteiro «${roteiro.nome}» (${roteiro.id}) com ${roteiro.passos.size} passo(s)."
    }

    fun apagar(id: String): Boolean {
        if (!idServeComoNomeDeArquivo(id)) return false
        val arquivo = File(pasta, "$id.json")
        return arquivo.isFile && arquivo.delete()
    }

    private fun idServeComoNomeDeArquivo(id: String): Boolean {
        if (id.isBlank() || id.length > 80) return false
        return id.all { it.isLetterOrDigit() || it == '-' || it == '_' }
    }
}

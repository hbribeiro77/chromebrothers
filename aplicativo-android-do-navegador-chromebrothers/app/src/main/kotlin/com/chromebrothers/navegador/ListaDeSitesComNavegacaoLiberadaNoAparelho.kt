package com.chromebrothers.navegador

import android.content.Context
import com.chromebrothers.navegador.regras.LiberacaoDeNavegacaoDosSitesParaOChat
import com.chromebrothers.navegador.regras.ResultadoDaLiberacaoDeNavegacao
import com.chromebrothers.navegador.regras.TextoDaListaDeSitesComNavegacaoLiberada

class ListaDeSitesComNavegacaoLiberadaNoAparelho(context: Context) {
    private val prefs = context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    fun listar(): List<String> {
        return TextoDaListaDeSitesComNavegacaoLiberada.ler(prefs.getString(CHAVE, "").orEmpty())
    }

    fun liberar(site: String): String {
        return when (val resultado = LiberacaoDeNavegacaoDosSitesParaOChat.liberar(listar(), site)) {
            is ResultadoDaLiberacaoDeNavegacao.Recusada -> resultado.mensagem
            is ResultadoDaLiberacaoDeNavegacao.Liberada -> {
                prefs.edit().putString(CHAVE, TextoDaListaDeSitesComNavegacaoLiberada.escrever(resultado.sites)).apply()
                ""
            }
        }
    }

    fun tirar(site: String) {
        val sites = LiberacaoDeNavegacaoDosSitesParaOChat.tirar(listar(), site)
        prefs.edit().putString(CHAVE, TextoDaListaDeSitesComNavegacaoLiberada.escrever(sites)).apply()
    }

    fun estaLiberado(site: String): Boolean {
        return LiberacaoDeNavegacaoDosSitesParaOChat.contem(listar(), site)
    }

    companion object {
        private const val ARQUIVO = "sites_com_navegacao_liberada_do_chat_do_chromebrothers"
        private const val CHAVE = "sites"
    }
}

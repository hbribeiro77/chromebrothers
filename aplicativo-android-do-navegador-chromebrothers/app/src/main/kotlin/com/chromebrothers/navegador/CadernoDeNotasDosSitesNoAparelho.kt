package com.chromebrothers.navegador

import android.content.Context
import com.chromebrothers.navegador.regras.CadernoDeNotasDosSitesParaOChat
import com.chromebrothers.navegador.regras.NotaDoSiteParaOChat
import com.chromebrothers.navegador.regras.ResultadoDaNotaDoSite
import com.chromebrothers.navegador.regras.TextoDoCadernoDeNotasDosSites

class CadernoDeNotasDosSitesNoAparelho(context: Context) {
    private val prefs = context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    fun listar(): List<NotaDoSiteParaOChat> {
        return TextoDoCadernoDeNotasDosSites.ler(prefs.getString(CHAVE, "").orEmpty())
    }

    fun guardar(site: String, texto: String): String {
        return when (val resultado = CadernoDeNotasDosSitesParaOChat.guardar(listar(), site, texto)) {
            is ResultadoDaNotaDoSite.Recusada -> resultado.mensagem
            is ResultadoDaNotaDoSite.Guardada -> {
                prefs.edit().putString(CHAVE, TextoDoCadernoDeNotasDosSites.escrever(resultado.notas)).apply()
                ""
            }
        }
    }

    fun apagar(site: String) {
        val notas = CadernoDeNotasDosSitesParaOChat.apagar(listar(), site)
        prefs.edit().putString(CHAVE, TextoDoCadernoDeNotasDosSites.escrever(notas)).apply()
    }

    fun notaDe(endereco: String): String {
        return CadernoDeNotasDosSitesParaOChat.paraOModelo(
            CadernoDeNotasDosSitesParaOChat.buscar(listar(), endereco),
        )
    }

    companion object {
        private const val ARQUIVO = "notas_dos_sites_do_chat_do_chromebrothers"
        private const val CHAVE = "notas"
    }
}

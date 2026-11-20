package com.chromebrothers.navegador

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.chromebrothers.navegador.regras.GeracaoDeVideoPeloVivixNoChat

/** Guarda o token Bearer da API Vivix (W1) e a liberação do chat. */
class SessaoDaContaVivixNoAparelho(context: Context) {
    private val prefs: SharedPreferences = criar(context)

    fun temSessao(): Boolean = apiToken().isNotEmpty()

    fun apiToken(): String = prefs.getString(TOKEN, "").orEmpty().trim()

    fun gravarToken(token: String) {
        prefs.edit().putString(TOKEN, token.trim()).apply()
    }

    fun vivixLiberado(): Boolean = prefs.getBoolean(LIBERADO, false)

    fun definirVivixLiberado(valor: Boolean) {
        prefs.edit().putBoolean(LIBERADO, valor).apply()
    }

    fun apagar() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val ARQUIVO = "sessao_da_conta_vivix"
        private const val TOKEN = "api_token"
        private const val LIBERADO = "vivix_liberado"
        const val SEM_SESSAO = GeracaoDeVideoPeloVivixNoChat.SEM_SESSAO

        private fun criar(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                context,
                ARQUIVO,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }
    }
}

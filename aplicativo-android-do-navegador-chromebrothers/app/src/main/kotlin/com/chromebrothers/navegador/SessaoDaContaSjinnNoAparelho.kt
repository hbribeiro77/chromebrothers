package com.chromebrothers.navegador

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Guarda o API Token do Sjinn (Tool API).
 * OAuth MCP fica bloqueado para clientes Android custom (CIMD allowlist do AS).
 */
class SessaoDaContaSjinnNoAparelho(context: Context) {
    private val prefs: SharedPreferences = criar(context)

    fun temSessao(): Boolean = apiToken().isNotEmpty()

    fun apiToken(): String = prefs.getString(TOKEN, "").orEmpty().trim()

    fun gravarToken(token: String) {
        prefs.edit().putString(TOKEN, token.trim()).apply()
    }

    fun apagar() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val ARQUIVO = "sessao_da_conta_sjinn"
        private const val TOKEN = "api_token"
        const val SEM_SESSAO =
            "Sjinn sem API Token. Cole o token em Engrenagem → Sjinn (conta em sjinn.ai → API Token)."

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

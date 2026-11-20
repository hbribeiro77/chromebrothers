package com.chromebrothers.navegador

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Tokens OAuth da conta Magnific (MCP), criptografados no aparelho. */
class SessaoDaContaMagnificNoAparelho(context: Context) {
    private val prefs: SharedPreferences = criar(context)

    fun temSessao(): Boolean = refreshToken().isNotEmpty() || accessToken().isNotEmpty()

    fun accessToken(): String = prefs.getString(ACCESS, "").orEmpty().trim()

    fun refreshToken(): String = prefs.getString(REFRESH, "").orEmpty().trim()

    /** Client registrado uma vez (registro dinâmico) e reaproveitado nos próximos logins. */
    fun clientId(): String = prefs.getString(CLIENT_ID, "").orEmpty().trim()

    fun gravarClientId(id: String) {
        prefs.edit().putString(CLIENT_ID, id.trim()).apply()
    }

    /** Momento (ms) em que o access token vence. */
    fun expiraEmMs(): Long = prefs.getLong(EXPIRA_EM, 0L)

    fun gravarTokens(access: String, refresh: String, expiraEmMs: Long) {
        prefs.edit()
            .putString(ACCESS, access.trim())
            .putString(REFRESH, refresh.trim())
            .putLong(EXPIRA_EM, expiraEmMs)
            .apply()
    }

    /** Sai da conta, mas guarda o client registrado. */
    fun apagar() {
        val id = clientId()
        prefs.edit().clear().putString(CLIENT_ID, id).apply()
    }

    companion object {
        const val SEM_SESSAO = "Ligue a conta Magnific na engrenagem do chat."
        private const val ARQUIVO = "sessao_da_conta_magnific"
        private const val ACCESS = "access_token"
        private const val REFRESH = "refresh_token"
        private const val CLIENT_ID = "client_id"
        private const val EXPIRA_EM = "expira_em_ms"

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

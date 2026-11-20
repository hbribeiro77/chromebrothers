package com.chromebrothers.navegador

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessaoDaContaHiggsfieldNoAparelho(context: Context) {
    private val prefs: SharedPreferences = criar(context)

    fun temSessao(): Boolean = accessToken().isNotEmpty()

    fun accessToken(): String = prefs.getString(ACCESS, "").orEmpty().trim()

    fun refreshToken(): String = prefs.getString(REFRESH, "").orEmpty().trim()

    fun clientId(): String = prefs.getString(CLIENT_ID, "").orEmpty().trim()

    fun gravarTokens(access: String, refresh: String, clientId: String) {
        prefs.edit()
            .putString(ACCESS, access.trim())
            .putString(REFRESH, refresh.trim())
            .putString(CLIENT_ID, clientId.trim())
            .apply()
    }

    fun atualizarAccess(access: String) {
        prefs.edit().putString(ACCESS, access.trim()).apply()
    }

    fun apagar() {
        prefs.edit().clear().apply()
    }

    fun higgsfieldLiberado(): Boolean = prefs.getBoolean(LIBERADO, false)

    fun definirHiggsfieldLiberado(valor: Boolean) {
        prefs.edit().putBoolean(LIBERADO, valor).apply()
    }

    companion object {
        private const val ARQUIVO = "sessao_da_conta_higgsfield"
        private const val ACCESS = "access_token"
        private const val REFRESH = "refresh_token"
        private const val CLIENT_ID = "client_id"
        private const val LIBERADO = "higgsfield_liberado"

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

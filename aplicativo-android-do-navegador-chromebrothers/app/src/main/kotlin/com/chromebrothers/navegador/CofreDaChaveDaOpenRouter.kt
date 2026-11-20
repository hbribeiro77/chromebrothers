package com.chromebrothers.navegador

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CofreDaChaveDaOpenRouter(context: Context) {
    private val prefs: SharedPreferences = criar(context)

    fun ler(): String = prefs.getString(CHAVE, "").orEmpty().trim()

    fun temChave(): Boolean = ler().isNotEmpty()

    fun gravar(chave: String) {
        prefs.edit().putString(CHAVE, chave.trim()).apply()
    }

    fun apagar() {
        prefs.edit().remove(CHAVE).apply()
    }

    companion object {
        private const val ARQUIVO = "cofre_da_chave_da_openrouter"
        private const val CHAVE = "chave"

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

package com.setupassistant.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class VaultRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "vault_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getAll(): List<Pair<String, String>> =
        prefs.all.entries
            .map { it.key to (it.value as? String ?: "") }
            .sortedBy { it.first }

    fun put(label: String, value: String) {
        prefs.edit().putString(label, value).apply()
    }

    fun delete(label: String) {
        prefs.edit().remove(label).apply()
    }
}

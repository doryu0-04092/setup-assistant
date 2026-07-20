package com.setupassistant.app.data

import android.app.KeyguardManager
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * パスワードだけを保持する。他の情報より一段厚く守る。
 *
 * 鍵自体に [MasterKey.Builder.setUserAuthenticationRequired] を設定しているため、
 * 生体認証か画面ロックのPINを通していない状態では復号そのものができない。
 * 端末を他人に渡した状態でも中身は読めない。
 *
 * APIキーやアクセストークン、二要素認証のシードはここに入れない。
 * それらは現地で発行してPC側に設定する運用としている。
 */
class SecretRepository(private val context: Context) {

    /** 端末に画面ロックが設定されているか。未設定だと認証必須の鍵を作れない */
    fun isDeviceSecure(): Boolean {
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguard.isDeviceSecure
    }

    /**
     * 認証済みの間だけ使える保存領域を開く。
     *
     * 認証を通していない場合や画面ロックが未設定の場合は失敗するため、
     * 呼び出し側は [Result] を見てユーザーに案内すること。
     */
    private fun openPrefs(): Result<android.content.SharedPreferences> = runCatching {
        val masterKey = MasterKey.Builder(context, KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(true, AUTH_VALIDITY_SECONDS)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "account_secrets",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** 認証を通した直後に呼ぶこと */
    fun getPassword(profileId: String): Result<String?> =
        openPrefs().map { it.getString(passwordKey(profileId), null) }

    fun savePassword(profileId: String, password: String): Result<Unit> =
        openPrefs().map { prefs ->
            prefs.edit().apply {
                if (password.isBlank()) {
                    remove(passwordKey(profileId))
                } else {
                    putString(passwordKey(profileId), password)
                }
            }.apply()
        }

    fun deletePassword(profileId: String): Result<Unit> =
        openPrefs().map { it.edit().remove(passwordKey(profileId)).apply() }

    private fun passwordKey(profileId: String) = "password:$profileId"

    companion object {
        private const val KEY_ALIAS = "account_secret_master_key"

        /** 認証後この秒数だけ鍵を使える。短くしすぎると操作のたびに認証が挟まる */
        private const val AUTH_VALIDITY_SECONDS = 30
    }
}

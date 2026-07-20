package com.setupassistant.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

/**
 * アカウントの識別情報を保持する。
 *
 * ここで扱うのは公開されうる識別子(メールアドレスや表示名はコミットの記録に残る)だが、
 * 端末を失くしたときに現場の情報が読めてしまわないよう暗号化している。
 * パスワードは含めない。[SecretRepository] を使うこと。
 */
class AccountProfileRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "account_profiles",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getAll(): List<AccountProfile> =
        AccountProfile.listFromJson(prefs.getString(KEY_PROFILES, "[]").orEmpty())

    fun getActive(): AccountProfile? {
        val profiles = getAll()
        val activeId = prefs.getString(KEY_ACTIVE_ID, null)
        return profiles.find { it.id == activeId } ?: profiles.firstOrNull()
    }

    fun setActive(profileId: String) {
        prefs.edit().putString(KEY_ACTIVE_ID, profileId).apply()
    }

    /** 新規なら追加、既存idなら置き換える */
    fun save(profile: AccountProfile) {
        val profiles = getAll().toMutableList()
        val index = profiles.indexOfFirst { it.id == profile.id }
        if (index >= 0) profiles[index] = profile else profiles.add(profile)
        writeAll(profiles)

        if (prefs.getString(KEY_ACTIVE_ID, null) == null) setActive(profile.id)
    }

    fun delete(profileId: String) {
        writeAll(getAll().filterNot { it.id == profileId })
        if (prefs.getString(KEY_ACTIVE_ID, null) == profileId) {
            prefs.edit().remove(KEY_ACTIVE_ID).apply()
        }
    }

    /** 登録内容を全て消す。離任時のデータ消去とテストの初期化で使う */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun writeAll(profiles: List<AccountProfile>) {
        prefs.edit().putString(KEY_PROFILES, AccountProfile.listToJson(profiles)).apply()
    }

    companion object {
        private const val KEY_PROFILES = "profiles"
        private const val KEY_ACTIVE_ID = "active_id"

        fun newId(): String = UUID.randomUUID().toString()
    }
}

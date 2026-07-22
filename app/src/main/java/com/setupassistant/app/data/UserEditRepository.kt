package com.setupassistant.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** ステップに対して利用者が加えた変更 */
data class StepEdit(
    val note: String = "",
    val description: String? = null,
    val command: String? = null
) {
    val hasOverride: Boolean get() = description != null || command != null
    val isEmpty: Boolean get() = note.isBlank() && !hasOverride
}

/**
 * 手順に対する利用者の書き込みを保持する。
 *
 * メモ欄にうっかりトークンが書かれても平文で残らないよう暗号化しているが、
 * 秘密情報を保存するための機能ではない。UI側で注意書きを出している。
 */
class UserEditRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "user_edits",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun get(stepId: String): StepEdit = StepEdit(
        note = prefs.getString(noteKey(stepId), "").orEmpty(),
        description = prefs.getString(descriptionKey(stepId), null),
        command = prefs.getString(commandKey(stepId), null)
    )

    fun save(stepId: String, edit: StepEdit) {
        prefs.edit().apply {
            if (edit.note.isBlank()) remove(noteKey(stepId)) else putString(noteKey(stepId), edit.note)
            putOrRemove(descriptionKey(stepId), edit.description)
            putOrRemove(commandKey(stepId), edit.command)
        }.apply()
    }

    /** 手順の上書きだけを消し、メモは残す */
    fun clearOverride(stepId: String) {
        prefs.edit()
            .remove(descriptionKey(stepId))
            .remove(commandKey(stepId))
            .apply()
    }

    /** メモと上書きを全て消す。離任時のデータ消去とテストの初期化で使う */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun android.content.SharedPreferences.Editor.putOrRemove(key: String, value: String?) {
        if (value.isNullOrBlank()) remove(key) else putString(key, value)
    }

    private fun noteKey(stepId: String) = "note:$stepId"
    private fun descriptionKey(stepId: String) = "override:$stepId:description"
    private fun commandKey(stepId: String) = "override:$stepId:command"
}

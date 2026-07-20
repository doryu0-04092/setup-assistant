package com.setupassistant.app.data

import org.json.JSONArray
import org.json.JSONObject

enum class TwoFactorMethod(val label: String) {
    UNSET("未設定・未確認"),
    AUTH_APP("認証アプリ"),
    SMS("SMS"),
    SECURITY_KEY("セキュリティキー"),
    PASSKEY("パスキー"),
    OTHER("その他")
}

/**
 * 現場で使うアカウントの識別情報。
 *
 * パスワードはここには含めない。[SecretRepository] が生体認証つきで別に保持する。
 */
data class AccountProfile(
    val id: String,
    /** 「自分用」「常駐先A」など、利用者が付ける名前 */
    val label: String,
    val service: String = "",
    val accountId: String = "",
    val email: String = "",
    val displayName: String = "",
    val twoFactorMethod: TwoFactorMethod = TwoFactorMethod.UNSET,
    val note: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("label", label)
        put("service", service)
        put("accountId", accountId)
        put("email", email)
        put("displayName", displayName)
        put("twoFactorMethod", twoFactorMethod.name)
        put("note", note)
    }

    companion object {
        fun fromJson(json: JSONObject) = AccountProfile(
            id = json.getString("id"),
            label = json.optString("label"),
            service = json.optString("service"),
            accountId = json.optString("accountId"),
            email = json.optString("email"),
            displayName = json.optString("displayName"),
            twoFactorMethod = runCatching {
                TwoFactorMethod.valueOf(json.optString("twoFactorMethod"))
            }.getOrDefault(TwoFactorMethod.UNSET),
            note = json.optString("note")
        )

        fun listFromJson(raw: String): List<AccountProfile> = runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { fromJson(array.getJSONObject(it)) }
        }.getOrDefault(emptyList())

        fun listToJson(profiles: List<AccountProfile>): String =
            JSONArray().apply { profiles.forEach { put(it.toJson()) } }.toString()
    }
}

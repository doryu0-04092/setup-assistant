package com.setupassistant.app.data

/**
 * 手順のコマンドに含まれる {{email}} などを、選択中のアカウントの値に置き換える。
 *
 * 未登録の項目は <登録したメールアドレス> のような案内文にする。
 * 空欄のまま実行されるより、何を入れるべきか分かる方が安全なため。
 */
fun String.withAccountValues(profile: AccountProfile?): String =
    replacePlaceholder("email", profile?.email, "登録したメールアドレス")
        .replacePlaceholder("username", profile?.accountId, "アカウントID")
        .replacePlaceholder("displayName", profile?.displayName, "表示名")

private fun String.replacePlaceholder(
    name: String,
    value: String?,
    fallbackLabel: String
): String {
    val replacement = value?.takeIf { it.isNotBlank() } ?: "<$fallbackLabel>"
    return replace("{{$name}}", replacement)
}

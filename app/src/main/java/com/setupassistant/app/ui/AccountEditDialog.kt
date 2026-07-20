package com.setupassistant.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.setupassistant.app.data.AccountProfile
import com.setupassistant.app.data.SecretRepository
import com.setupassistant.app.data.TwoFactorMethod

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AccountEditDialog(
    profile: AccountProfile,
    secrets: SecretRepository,
    onDismiss: () -> Unit,
    onSave: (AccountProfile) -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current

    var label by remember { mutableStateOf(profile.label) }
    var service by remember { mutableStateOf(profile.service) }
    var accountId by remember { mutableStateOf(profile.accountId) }
    var email by remember { mutableStateOf(profile.email) }
    var displayName by remember { mutableStateOf(profile.displayName) }
    var twoFactor by remember { mutableStateOf(profile.twoFactorMethod) }
    var note by remember { mutableStateOf(profile.note) }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (profile.label.isBlank()) "アカウントを追加" else "アカウントを編集") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("この登録の名前 (例: 常駐先A)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = service,
                    onValueChange = { service = it },
                    label = { Text("サービス (例: GitHub)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = accountId,
                    onValueChange = { accountId = it },
                    label = { Text("アカウントID・ユーザー名") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("登録メールアドレス") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("表示名 (コミットに残る名前)") },
                    singleLine = true
                )

                Text("二要素認証の方式", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TwoFactorMethod.entries.forEach { method ->
                        FilterChip(
                            selected = twoFactor == method,
                            onClick = { twoFactor = method },
                            label = { Text(method.label) }
                        )
                    }
                }
                Text(
                    text = "方式の名前だけを記録します。認証アプリに登録するQRコードや文字列は保存しません。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("備考") },
                    minLines = 2
                )

                Text("パスワード", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("変更する場合のみ入力") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Text(
                    text = "パスワードは端末の生体認証・PINを通さないと読み出せない形で保存します。" +
                        "APIキーやアクセストークンは、ここには入れずPC側に直接設定してください。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = label.isNotBlank(),
                onClick = {
                    val updated = profile.copy(
                        label = label.trim(),
                        service = service.trim(),
                        accountId = accountId.trim(),
                        email = email.trim(),
                        displayName = displayName.trim(),
                        twoFactorMethod = twoFactor,
                        note = note
                    )

                    if (password.isBlank()) {
                        onSave(updated)
                        return@TextButton
                    }

                    if (!secrets.isDeviceSecure()) {
                        onMessage("パスワードを保存するには、端末に画面ロック(PINや指紋)を設定してください。")
                        return@TextButton
                    }

                    // 保存も鍵の使用にあたるため、ここでも認証を求める
                    promptForAuth(
                        context = context,
                        title = "パスワードを保存",
                        subtitle = label,
                        onSuccess = {
                            secrets.savePassword(updated.id, password)
                                .onSuccess { onSave(updated) }
                                .onFailure { onMessage("パスワードを保存できませんでした") }
                        },
                        onFailure = onMessage
                    )
                }
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}

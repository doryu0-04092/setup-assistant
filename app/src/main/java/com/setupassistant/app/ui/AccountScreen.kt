package com.setupassistant.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.setupassistant.app.ui.theme.Spacing
import com.setupassistant.app.data.AccountProfile
import com.setupassistant.app.data.AccountProfileRepository
import com.setupassistant.app.data.Repositories
import com.setupassistant.app.data.SecretRepository

@Composable
fun AccountScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember { Repositories.accountProfiles(context) }
    val secrets = remember { Repositories.secrets(context) }

    var version by remember { mutableIntStateOf(0) }
    val profiles = remember(version) { repository.getAll() }
    val activeProfile = remember(version) { repository.getActive() }

    var editing by remember { mutableStateOf<AccountProfile?>(null) }
    var pendingDelete by remember { mutableStateOf<AccountProfile?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = AccountProfile(id = AccountProfileRepository.newId(), label = "")
            }) {
                Icon(Icons.Default.Add, contentDescription = "アカウントを追加")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            // 下端はFABが重なるぶん余分に空ける
            contentPadding = PaddingValues(start = Spacing.Large, end = Spacing.Large, top = Spacing.Large, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {
            item {
                Text(
                    text = "現場で使うアカウントを控えておけます。選んだアカウントの値は、手順のコマンドに差し込まれます。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (profiles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("まだ登録がありません")
                    }
                }
            }

            items(profiles, key = { it.id }) { profile ->
                ProfileCard(
                    profile = profile,
                    isActive = profile.id == activeProfile?.id,
                    secrets = secrets,
                    onSelect = {
                        repository.setActive(profile.id)
                        version++
                    },
                    onEdit = { editing = profile },
                    onDelete = { pendingDelete = profile },
                    onMessage = { message = it }
                )
            }
        }
    }

    editing?.let { profile ->
        AccountEditDialog(
            profile = profile,
            secrets = secrets,
            onDismiss = { editing = null },
            onSave = { updated ->
                repository.save(updated)
                version++
                editing = null
            },
            onMessage = { message = it }
        )
    }

    pendingDelete?.let { profile ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("削除しますか?") },
            text = { Text("「${profile.label}」の登録内容とパスワードを削除します。") },
            confirmButton = {
                TextButton(onClick = {
                    repository.delete(profile.id)
                    secrets.deletePassword(profile.id)
                    version++
                    pendingDelete = null
                }) { Text("削除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("キャンセル") }
            }
        )
    }

    message?.let { text ->
        AlertDialog(
            onDismissRequest = { message = null },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = { message = null }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun ProfileCard(
    profile: AccountProfile,
    isActive: Boolean,
    secrets: SecretRepository,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var revealedPassword by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isActive) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = profile.label.ifBlank { "(名前なし)" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "編集")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "削除")
                }
            }

            FilterChip(
                selected = isActive,
                onClick = onSelect,
                label = { Text(if (isActive) "手順で使用中" else "手順で使う") }
            )

            if (profile.service.isNotBlank()) FieldRow("サービス", profile.service)
            if (profile.accountId.isNotBlank()) FieldRow("アカウントID", profile.accountId, copyable = true)
            if (profile.email.isNotBlank()) FieldRow("メールアドレス", profile.email, copyable = true)
            if (profile.displayName.isNotBlank()) FieldRow("表示名", profile.displayName, copyable = true)
            FieldRow("二要素認証", profile.twoFactorMethod.label)
            if (profile.note.isNotBlank()) FieldRow("備考", profile.note)

            PasswordRow(
                password = revealedPassword,
                onReveal = {
                    if (!secrets.isDeviceSecure()) {
                        onMessage("パスワードを扱うには、端末に画面ロック(PINや指紋)を設定してください。")
                        return@PasswordRow
                    }
                    promptForAuth(
                        context = context,
                        title = "パスワードを表示",
                        subtitle = profile.label,
                        onSuccess = {
                            secrets.getPassword(profile.id)
                                .onSuccess { revealedPassword = it ?: "(未登録)" }
                                .onFailure { onMessage("パスワードを読み出せませんでした") }
                        },
                        onFailure = onMessage
                    )
                },
                onHide = { revealedPassword = null }
            )
        }
    }
}

@Composable
private fun FieldRow(label: String, value: String, copyable: Boolean = false) {
    val context = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
        if (copyable) {
            IconButton(onClick = { copyToClipboard(context, label, value) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "$label をコピー")
            }
        }
    }
}

@Composable
private fun PasswordRow(
    password: String?,
    onReveal: () -> Unit,
    onHide: () -> Unit
) {
    val context = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "パスワード",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = password ?: "••••••••",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (password == null) {
            TextButton(onClick = onReveal) { Text("認証して表示") }
        } else {
            IconButton(onClick = {
                copyToClipboard(context, "password", password, sensitive = true)
            }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "パスワードをコピー")
            }
            TextButton(onClick = onHide) { Text("隠す") }
        }
    }
}

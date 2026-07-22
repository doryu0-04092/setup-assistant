package com.setupassistant.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 本人確認を通すまで中身を出さない。
 *
 * 端末に画面ロックが設定されていない場合は確認する手段がないため素通しにする。
 * パスワードはもともとロック必須の鍵で守られているので、そちらは影響を受けない。
 */
@Composable
fun AuthGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val required = remember { canAuthenticate(context) }

    var unlocked by remember { mutableStateOf(!required) }
    var error by remember { mutableStateOf<String?>(null) }

    fun authenticate() {
        promptForAuth(
            context = context,
            title = "本人確認",
            subtitle = "アカウント情報を扱うため確認します",
            onSuccess = {
                error = null
                unlocked = true
            },
            onFailure = { error = it }
        )
    }

    // 起動直後に一度だけ求める。以降は利用者がボタンで再試行する
    LaunchedEffect(Unit) {
        if (required) authenticate()
    }

    if (unlocked) {
        content()
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "ロックを解除してください",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "指紋・顔認証、または画面ロックのPINで確認します。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Button(onClick = { authenticate() }) {
            Text("認証する")
        }
    }
}

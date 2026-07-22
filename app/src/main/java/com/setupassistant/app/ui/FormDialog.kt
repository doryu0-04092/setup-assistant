package com.setupassistant.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 入力フォーム用の全画面ダイアログ。
 *
 * 保存は画面上部のバーに置く。ソフトウェアキーボードは下から出るため、
 * 下部に置くとどうしても隠れて押せなくなる。上部なら常に見えている。
 *
 * decorFitsSystemWindows を false にしているのは、既定のままだと
 * キーボードの高さが imePadding に伝わらず、余白が付かないため。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // システムバーとキーボードの大きい方を採る。
                    // systemBarsPadding と imePadding を重ねると余白が二重になり、
                    // キーボードが出ているときに下端が欠ける
                    .safeDrawingPadding()
            ) {
                TopAppBar(
                    title = { Text(text = title, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "閉じる")
                        }
                    },
                    actions = {
                        TextButton(onClick = onSave, enabled = saveEnabled) {
                            Text("保存")
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    content()

                    // 最後の入力欄でも、キーボードの上まで持ち上げられるだけの
                    // スクロールの余地を残しておく
                    Spacer(Modifier.height(160.dp))
                }
            }
        }
    }
}

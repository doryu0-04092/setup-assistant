package com.setupassistant.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontFamily
import com.setupassistant.app.data.SetupStep
import com.setupassistant.app.data.StepEdit

/**
 * メモの記入と、手順本文の上書きを行うフォーム。
 *
 * 上書き欄は元の文面を初期値として入れておき、書き換えた場合だけ保存する。
 */
@Composable
fun StepEditDialog(
    step: SetupStep,
    edit: StepEdit,
    onDismiss: () -> Unit,
    onSave: (StepEdit) -> Unit,
    onResetOverride: () -> Unit
) {
    var note by remember { mutableStateOf(edit.note) }
    var description by remember { mutableStateOf(edit.description ?: step.description) }
    var command by remember { mutableStateOf(edit.command ?: step.command.orEmpty()) }

    FormDialog(
        title = step.title,
        onDismiss = onDismiss,
        onSave = {
            onSave(
                StepEdit(
                    note = note,
                    // 元の文面と同じなら上書きとして保存しない
                    description = description.takeIf { it != step.description },
                    command = command.takeIf { it != step.command.orEmpty() }
                )
            )
        }
    ) {
        Text(
            text = "トークンやパスワードは書かないでください。現場固有の値や気づいたことを残す欄です。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("メモ") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "手順を自分用に書き換える",
            style = MaterialTheme.typography.titleSmall
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("説明") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        if (step.command != null) {
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                label = { Text("コマンド") },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (edit.hasOverride) {
            TextButton(onClick = onResetOverride) {
                Text("書き換えを元に戻す")
            }
        }
    }
}

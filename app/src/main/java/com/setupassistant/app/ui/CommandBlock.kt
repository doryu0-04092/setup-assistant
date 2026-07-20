package com.setupassistant.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

private val TerminalBackground = Color(0xFF1E1E1E)
private val TerminalForeground = Color(0xFFE6E6E6)
private val TerminalOutput = Color(0xFF9AA0A6)

/**
 * ターミナルで実行するコマンドを、コピーボタンと期待される出力つきで表示する。
 *
 * 黒地・等幅にすることで、画面上の操作と見分けられるようにしている。
 */
@Composable
fun CommandBlock(
    command: String,
    expectedOutput: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalBackground)
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = command,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = TerminalForeground,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { copyToClipboard(context, "command", command) }) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "コマンドをコピー",
                    tint = TerminalForeground
                )
            }
        }

        if (expectedOutput != null) {
            Column(
                modifier = Modifier.padding(top = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "実行するとこう表示されます",
                    style = MaterialTheme.typography.labelSmall,
                    color = TerminalOutput
                )
                Text(
                    text = expectedOutput,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = TerminalOutput
                )
            }
        }
    }
}

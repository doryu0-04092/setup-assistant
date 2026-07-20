package com.setupassistant.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class DataPolicy(
    val kind: String,
    val examples: String,
    val handling: String,
    val stored: Boolean
)

private val policies = listOf(
    DataPolicy(
        kind = "アカウントの識別子",
        examples = "アカウントID、登録メールアドレス、表示名、二要素認証の方式",
        handling = "暗号化して保存します。メールアドレスと名前はコミットの記録に残るもので、そもそも隠す性質の情報ではありません。",
        stored = true
    ),
    DataPolicy(
        kind = "パスワード",
        examples = "アカウントのログインパスワード",
        handling = "暗号化した上で、見るたびに指紋・顔認証または画面ロックのPINを求めます。認証を通さなければ復号できない鍵で保護しているため、端末を他人に渡した状態でも中身は見えません。",
        stored = true
    ),
    DataPolicy(
        kind = "APIキー・アクセストークン",
        examples = "GitHubのPAT、各種APIキー",
        handling = "保存しません。現地で発行し、その場でPC側(環境変数や認証情報マネージャ)に設定してください。",
        stored = false
    ),
    DataPolicy(
        kind = "二要素認証のシークレット",
        examples = "認証アプリに登録するQRコードや文字列",
        handling = "保存しません。方式の名前(認証アプリ / SMS など)だけを記録します。",
        stored = false
    )
)

private val cleanupItems = listOf(
    "Cursor からサインアウトする",
    "Claude Code からサインアウトする",
    "ターミナルで gh auth logout を実行する",
    "git config --global --unset user.email と user.name で設定を消す",
    "クローンした作業フォルダを削除する",
    "ブラウザに保存したパスワードとログイン状態を消す"
)

@Composable
fun PrinciplesScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "このアプリが保存するもの・しないもの",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "情報の性質によって扱いを分けています。まとめて「保存する / しない」とは決めていません。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(policies.size) { index ->
            PolicyCard(policies[index])
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "なぜトークンを端末に置かないのか",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "アクセストークンは発行したらすぐPC側に設定するもので、手元に持ち続ける必要がありません。" +
                            "必要な権限だけに絞って発行し、使わなくなったら失効させるのが正しい使い方です。" +
                            "持ち歩く前提を作らないこと自体が、漏れたときの被害を小さくします。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "会社から支給されたアカウントの情報を個人の端末に置くことは、" +
                            "多くの現場でルール違反にあたります。判断に迷う場合は担当者に確認してください。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "現場のPCを離れるときに消すもの",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "貸与PCや共有PCでは、離任時に自分の認証情報を残さないようにします。" +
                        "現場のルールがある場合はそちらに従ってください。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(cleanupItems.size) { index ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "・", style = MaterialTheme.typography.bodyMedium)
                Text(text = cleanupItems[index], style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PolicyCard(policy: DataPolicy) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = policy.kind,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (policy.stored) "保存する" else "保存しない",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (policy.stored) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
            Text(
                text = policy.examples,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = policy.handling, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

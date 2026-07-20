package com.setupassistant.app.data

data class SetupGuide(
    val id: String,
    val title: String,
    val steps: List<String>,
    val officialUrl: String,
    val lastVerified: String
)

object SetupGuides {
    val all: List<SetupGuide> = listOf(
        SetupGuide(
            id = "cursor",
            title = "Cursor",
            steps = listOf(
                "公式サイトからOSに合ったインストーラをダウンロードする",
                "インストーラを実行してセットアップを完了する",
                "初回起動時にアカウントを作成、またはログインする",
                "起動して正常に立ち上がることを確認する"
            ),
            officialUrl = "https://www.cursor.com/downloads",
            lastVerified = "2026-07-17"
        ),
        SetupGuide(
            id = "claude-code",
            title = "Claude Code",
            steps = listOf(
                "Cursorを起動し、拡張機能タブ(Extensions)を開く",
                "「Claude Code」で検索し、Anthropic公式の拡張機能をインストールする",
                "インストール後に表示されるサインイン画面からAnthropicアカウントでログインする(有料プランがあればAPIキー不要)",
                "エディタ右上のスパークアイコンからClaude Codeパネルが開くことを確認する"
            ),
            officialUrl = "https://code.claude.com/docs/en/vscode",
            lastVerified = "2026-07-17"
        ),
        SetupGuide(
            id = "chatgpt",
            title = "ChatGPT",
            steps = listOf(
                "OpenAIアカウントを作成、またはログインする",
                "APIキー管理画面で新しいキーを発行する(用途ごとに分けるのが安全)",
                "発行されたキーは再表示できないため、その場でPC側(環境変数や認証情報マネージャ)に設定する",
                "必要に応じて使用量上限(Usage limits)を設定する"
            ),
            officialUrl = "https://platform.openai.com/api-keys",
            lastVerified = "2026-07-17"
        ),
        SetupGuide(
            id = "github",
            title = "GitHub",
            steps = listOf(
                "GitHubアカウントを作成、またはログインする",
                "PCにGitをインストールする(公式サイトのインストーラを使用)",
                "git config --global user.name / user.email を設定する",
                "初回pushの認証方法(HTTPS+PAT、またはSSH鍵)を確認しておく"
            ),
            officialUrl = "https://docs.github.com/en/get-started/quickstart/set-up-git",
            lastVerified = "2026-07-17"
        )
    )
}

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
            id = "claude-code",
            title = "Claude Code",
            steps = listOf(
                "Node.js(18以上)をインストールする",
                "ターミナルで npm install -g @anthropic-ai/claude-code を実行する",
                "claude コマンドを実行し、案内に従ってログイン(またはAPIキーを設定)する",
                "プロジェクトフォルダで claude を起動し、正常に動くか確認する"
            ),
            officialUrl = "https://docs.claude.com/en/docs/claude-code/overview",
            lastVerified = "2026-07-17"
        ),
        SetupGuide(
            id = "vscode",
            title = "VSCode",
            steps = listOf(
                "公式サイトからOSに合ったインストーラをダウンロードする",
                "インストーラを実行してセットアップを完了する",
                "拡張機能タブから必要な拡張機能を検索してインストールする",
                "起動して正常に立ち上がることを確認する"
            ),
            officialUrl = "https://code.visualstudio.com/download",
            lastVerified = "2026-07-17"
        ),
        SetupGuide(
            id = "chatgpt",
            title = "ChatGPT",
            steps = listOf(
                "OpenAIアカウントを作成、またはログインする",
                "APIキー管理画面で新しいキーを発行する(用途ごとに分けるのが安全)",
                "発行されたキーは再表示できないため、その場でこのアプリのvaultに保存する",
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

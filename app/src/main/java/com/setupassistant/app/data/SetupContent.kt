package com.setupassistant.app.data

/**
 * 手順の本文。
 *
 * コマンド内の {{email}} {{username}} {{displayName}} は、
 * 登録済みのアカウント情報に置き換えて表示される。
 */
object SetupContent {

    private const val VERIFIED = "2026-07-20"

    val phases: List<SetupPhase> = listOf(
        onsiteRules(),
        inventory(),
        git(),
        nodejs(),
        cursor(),
        claudeCode(),
        github(),
        smokeTest(),
        selfSufficiency()
    )

    fun findPhase(id: String): SetupPhase? = phases.find { it.id == id }

    // ── 0. 現場ルールの確認 ────────────────────────────────

    private fun onsiteRules() = SetupPhase(
        id = "onsite-rules",
        title = "現場ルールの確認",
        summary = "作業を始める前に、使うアカウントと、その現場で許されていることを確かめます。ここを飛ばすと後で作り直しになります。",
        lastVerified = VERIFIED,
        commonSteps = listOf(
            SetupStep(
                id = "rules-account",
                title = "使うアカウントを確認する",
                surface = Surface.UI,
                description = "その現場で使うアカウントが「会社から支給されたもの」なのか「自分のもの」なのかを、担当者に確認します。支給される場合は、ID・登録メールアドレス・初期パスワード・二要素認証の方式を受け取ります。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "受け取った情報を見返す",
                    expected = "ID、メールアドレス、パスワード、二要素認証の方式が全て分かっている"
                ),
                pitfall = "後の手順でメールアドレスを使います。分からないまま進めると、間違ったアカウントでコミットしてしまい、やり直しになります。"
            ),
            SetupStep(
                id = "rules-admin",
                title = "管理者権限があるか確認する",
                surface = Surface.UI,
                description = "ソフトをインストールするには管理者権限が必要なことがあります。スタートメニューを右クリックし、表示されるメニューに「ターミナル(管理者)」があるか見てみます。実際にインストールを試したときに管理者パスワードを求められる場合は、権限がない可能性が高いです。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "管理者権限の有無を担当者に確認する",
                    expected = "自分でインストールしてよいか、依頼が必要かがはっきりしている"
                ),
                pitfall = "権限がない現場では、インストール自体を情報システム部門に依頼することになります。時間がかかるので早めに確認します。"
            ),
            SetupStep(
                id = "rules-network",
                title = "ネットワークの制限を確認する",
                surface = Surface.UI,
                description = "社内ネットワークではプロキシ(通信を中継するサーバー)を経由しないと外部に接続できないことがあります。プロキシの設定値が必要かどうかを担当者に確認します。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "ブラウザで github.com を開いてみる",
                    expected = "普通に表示される。表示されない場合はプロキシ設定が必要"
                ),
                pitfall = "プロキシ環境では、この後のインストールやGitの通信も個別に設定が必要になります。設定値(サーバー名とポート番号)を控えておきます。"
            ),
            SetupStep(
                id = "rules-software",
                title = "インストールしてよいソフトを確認する",
                surface = Surface.UI,
                description = "現場によっては、許可されたソフトしか入れられません。これから入れるもの(Git、Cursor、必要ならNode.js)が許可されているかを確認します。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "担当者の回答を確認する",
                    expected = "入れてよいソフトがはっきりしている"
                )
            )
        )
    )

    // ── 1. 現状の棚卸し ───────────────────────────────────

    private fun inventory() = SetupPhase(
        id = "inventory",
        title = "現状の棚卸し",
        summary = "PCに何がすでに入っているかを確認します。まっさらとは限らず、入っていればインストールは不要でアカウントの切り替えだけで済みます。",
        lastVerified = VERIFIED,
        commonSteps = listOf(
            SetupStep(
                id = "inv-open-terminal",
                title = "ターミナルを開く",
                surface = Surface.UI,
                description = "ターミナルは、文字でコマンドを打ってPCを操作する画面です。キーボードの Windows キーを押し、そのまま「powershell」と入力して Enter を押します。黒か青の画面が開きます。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "開いた画面を見る",
                    expected = "「PS C:\\Users\\(あなたの名前)>」のような行が表示され、文字が打てる状態になっている"
                ),
                pitfall = "画面が開かない場合は、スタートメニューを右クリックして「ターミナル」または「Windows PowerShell」を選んでも開けます。"
            ),
            SetupStep(
                id = "inv-check-all",
                title = "何が入っているかまとめて確認する",
                surface = Surface.TERMINAL,
                description = "下のコマンドをコピーしてターミナルに貼り付け、Enter を押します。貼り付けは右クリックでできます。入っているものはバージョン番号が表示され、入っていないものはエラーメッセージが出ます。エラーが出ても壊れたわけではないので、そのまま進めて構いません。",
                command = "git --version; node --version; gh --version",
                expectedOutput = "git version 2.55.0.windows.3\nv22.14.0\ngh version 2.96.0 (2026-07-01)",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "表示された内容を見る",
                    expected = "それぞれについて、バージョン番号が出たか、エラーが出たかが分かる"
                ),
                pitfall = "「用語 'git' は、コマンドレット、関数、スクリプト ファイル、または操作可能なプログラムの名前として認識されません」というエラーは「入っていない」という意味です。"
            ),
            SetupStep(
                id = "inv-check-cursor",
                title = "Cursorが入っているか確認する",
                surface = Surface.UI,
                description = "Windows キーを押して「cursor」と入力します。アプリが候補に出てくれば、すでに入っています。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "スタートメニューの検索結果を見る",
                    expected = "Cursor がアプリとして表示されるか、表示されないかが分かる"
                )
            ),
            SetupStep(
                id = "inv-note-result",
                title = "確認した結果を控える",
                surface = Surface.UI,
                description = "この後のフェーズで「入っているか」を選ぶ場面があります。Git・Node.js・GitHub CLI・Cursor のそれぞれについて、入っていたかどうかを控えておきます。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "控えた内容を見返す",
                    expected = "4つそれぞれについて、入っている / 入っていない が分かる"
                )
            )
        )
    )

    // ── 2. Git ───────────────────────────────────────────

    private fun git() = SetupPhase(
        id = "git",
        title = "Git",
        summary = "Gitはソースコードの変更履歴を記録する道具です。すでに入っている場合は、コミットに記録される名前とメールアドレスを、今回使うアカウントのものに切り替えます。",
        lastVerified = VERIFIED,
        statusCheck = StatusCheck(
            question = "Gitは入っていますか?",
            surface = Surface.TERMINAL,
            howToCheck = "ターミナルで次のコマンドを実行します。バージョン番号が出れば入っています。",
            checkCommand = "git --version",
            expectedIfPresent = "git version 2.55.0.windows.3"
        ),
        stepsIfAbsent = listOf(
            SetupStep(
                id = "git-install",
                title = "Gitをインストールする",
                surface = Surface.TERMINAL,
                description = "下のコマンドを実行すると、Windowsの標準機能(winget)を使ってGitが入ります。途中で確認を求められたら y を入力して Enter を押します。",
                command = "winget install --id Git.Git -e --source winget",
                expectedOutput = "インストールが完了しました",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "ターミナルを一度閉じて開き直し、バージョンを確認する",
                    command = "git --version",
                    expected = "git version 2.55.0.windows.3 のようにバージョン番号が表示される"
                ),
                pitfall = "インストール直後は、開いているターミナルではまだ git コマンドが見つかりません。必ずターミナルを閉じて開き直してください。winget が使えない場合は公式サイトからインストーラをダウンロードします。",
                officialUrl = "https://git-scm.com/install/windows"
            )
        ),
        stepsIfPresent = listOf(
            SetupStep(
                id = "git-show-config",
                title = "今の設定を確認する",
                surface = Surface.TERMINAL,
                description = "前に使った人の設定が残っていることがあります。今どの名前とメールアドレスが設定されているかを確認します。",
                command = "git config --global --list",
                expectedOutput = "user.name=前任者の名前\nuser.email=maeninsha@example.com",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "user.name と user.email の行を見る",
                    expected = "今の設定値が分かる。何も表示されない場合は未設定"
                ),
                pitfall = "ここが他人の設定のままだと、あなたのコミットが他人の名前で記録されてしまいます。次の手順で必ず上書きします。"
            )
        ),
        commonSteps = listOf(
            SetupStep(
                id = "git-set-name",
                title = "コミットに記録される名前を設定する",
                surface = Surface.TERMINAL,
                description = "コミット(変更の記録)に残る名前を設定します。{{displayName}} の部分を、今回使うアカウントの表示名に置き換えてください。",
                command = "git config --global user.name \"{{displayName}}\"",
                expectedOutput = "(何も表示されなければ成功です)",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "設定された値を読み出す",
                    command = "git config --global user.name",
                    expected = "設定した名前が表示される"
                )
            ),
            SetupStep(
                id = "git-set-email",
                title = "コミットに記録されるメールアドレスを設定する",
                surface = Surface.TERMINAL,
                description = "GitHubアカウントに登録されているメールアドレスを設定します。ここが一致していないと、コミットがGitHub上であなたのものと認識されません。",
                command = "git config --global user.email \"{{email}}\"",
                expectedOutput = "(何も表示されなければ成功です)",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "設定された値を読み出す",
                    command = "git config --global user.email",
                    expected = "設定したメールアドレスが表示される"
                ),
                pitfall = "会社のポリシーでメールアドレスを公開したくない場合は、GitHubが用意する noreply アドレスを使えます。GitHubの Settings → Emails で確認できます。"
            )
        )
    )

    // ── 3. Node.js ───────────────────────────────────────

    private fun nodejs() = SetupPhase(
        id = "nodejs",
        title = "Node.js(必要な場合のみ)",
        summary = "Node.jsはJavaScriptを動かすための土台です。担当するプロジェクトで使う場合や、ターミナルから claude コマンドを直接使いたい場合に入れます。Cursorの拡張機能としてClaude Codeを使うだけなら不要です。",
        lastVerified = VERIFIED,
        statusCheck = StatusCheck(
            question = "Node.jsは入っていますか?",
            surface = Surface.TERMINAL,
            howToCheck = "ターミナルで次のコマンドを実行します。",
            checkCommand = "node --version",
            expectedIfPresent = "v22.14.0"
        ),
        stepsIfAbsent = listOf(
            SetupStep(
                id = "node-need-check",
                title = "本当に必要か確認する",
                surface = Surface.UI,
                description = "担当するプロジェクトがNode.jsを使うかどうかを確認します。使わない場合、このフェーズは飛ばして構いません。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "プロジェクトの資料やチームメンバーに確認する",
                    expected = "必要か不要かがはっきりしている"
                )
            ),
            SetupStep(
                id = "node-install",
                title = "Node.jsをインストールする",
                surface = Surface.TERMINAL,
                description = "長期サポート版(LTS)を入れます。安定していて、実務ではこちらを使うのが基本です。",
                command = "winget install --id OpenJS.NodeJS.LTS -e --source winget",
                expectedOutput = "インストールが完了しました",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "ターミナルを開き直してバージョンを確認する",
                    command = "node --version",
                    expected = "v22.14.0 のようにバージョン番号が表示される"
                ),
                pitfall = "ここでもインストール後はターミナルを開き直す必要があります。",
                officialUrl = "https://nodejs.org/en/download"
            )
        ),
        stepsIfPresent = listOf(
            SetupStep(
                id = "node-version-check",
                title = "バージョンがプロジェクトの要求を満たすか確認する",
                surface = Surface.TERMINAL,
                description = "入っていても、バージョンが古くてプロジェクトで使えないことがあります。プロジェクトの指定バージョンと照らし合わせます。",
                command = "node --version; npm --version",
                expectedOutput = "v22.14.0\n10.9.2",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "表示されたバージョンをプロジェクトの要求と比べる",
                    expected = "要求を満たしている"
                ),
                pitfall = "複数バージョンを切り替えたい場合は、nvm-windows などのバージョン管理ツールを使います。現場のルールを確認してから導入してください。"
            )
        )
    )

    // ── 4. Cursor ────────────────────────────────────────

    private fun cursor() = SetupPhase(
        id = "cursor",
        title = "Cursor",
        summary = "CursorはAI機能が組み込まれたコードエディタです。すでに入っている場合は、前任者のアカウントのままになっていないかを確認し、必要なら切り替えます。",
        lastVerified = VERIFIED,
        statusCheck = StatusCheck(
            question = "Cursorは入っていますか?",
            surface = Surface.UI,
            howToCheck = "Windows キーを押して「cursor」と入力し、アプリが候補に出るかを見ます。",
            expectedIfPresent = "Cursor がアプリとして表示される"
        ),
        stepsIfAbsent = listOf(
            SetupStep(
                id = "cursor-download",
                title = "インストーラをダウンロードする",
                surface = Surface.UI,
                description = "公式サイトを開き、Windows 用のインストーラをダウンロードします。ほとんどの場合は x64 版を選びます。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "ダウンロードフォルダを見る",
                    expected = "インストーラのファイルが保存されている"
                ),
                officialUrl = "https://www.cursor.com/downloads"
            ),
            SetupStep(
                id = "cursor-install",
                title = "インストールする",
                surface = Surface.UI,
                description = "ダウンロードしたファイルをダブルクリックし、画面の指示に従って進めます。設定はそのままで構いません。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "インストール完了後にCursorを起動する",
                    expected = "Cursorの画面が開く"
                ),
                pitfall = "管理者権限がない場合は、ユーザー単位のインストールを選ぶと入れられることがあります。それも不可なら情報システム部門に依頼します。"
            ),
            SetupStep(
                id = "cursor-signin",
                title = "指定されたアカウントでサインインする",
                surface = Surface.UI,
                description = "Cursorの画面右上、または初回起動時の案内からサインインします。ブラウザが開くので、フェーズ0で確認したアカウントでログインします。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "Cursorの右上や設定画面でアカウント名を見る",
                    expected = "今回使うアカウントの名前が表示されている"
                )
            )
        ),
        stepsIfPresent = listOf(
            SetupStep(
                id = "cursor-check-account",
                title = "今サインインしているアカウントを確認する",
                surface = Surface.UI,
                description = "Cursorを起動し、画面右上のアイコン、または設定画面(歯車 → Settings)でアカウント情報を見ます。前任者のアカウントのままになっていないかを確認します。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "表示されているアカウント名やメールアドレスを見る",
                    expected = "今回使うアカウントかどうかが分かる"
                ),
                pitfall = "他人のアカウントのまま使うと、その人の利用枠を消費してしまいます。必ず確認します。"
            ),
            SetupStep(
                id = "cursor-switch-account",
                title = "必要ならアカウントを切り替える",
                surface = Surface.UI,
                description = "違うアカウントだった場合は、いったんサインアウトしてから、今回使うアカウントでサインインし直します。設定画面またはアカウントメニューからサインアウトできます。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "サインインし直した後にアカウント名を見る",
                    expected = "今回使うアカウントの名前が表示されている"
                )
            )
        ),
        commonSteps = listOf(
            SetupStep(
                id = "cursor-open-folder",
                title = "作業フォルダを開けることを確認する",
                surface = Surface.UI,
                description = "Cursorのメニューから File → Open Folder を選び、任意のフォルダを開いてみます。左側にファイル一覧が表示されれば正常です。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "左側のサイドバーを見る",
                    expected = "開いたフォルダの中身が一覧表示されている"
                )
            )
        )
    )

    // ── 5. Claude Code ───────────────────────────────────

    private fun claudeCode() = SetupPhase(
        id = "claude-code",
        title = "Claude Code",
        summary = "Claude CodeはAIにコードを書かせたり質問したりできる拡張機能です。CursorはVSCodeをもとに作られているため、VSCode用の拡張機能がそのまま使えます。",
        lastVerified = VERIFIED,
        statusCheck = StatusCheck(
            question = "Claude Codeの拡張機能は入っていますか?",
            surface = Surface.UI,
            howToCheck = "Cursorで Ctrl+Shift+X を押して拡張機能の一覧を開き、「Claude Code」で検索します。すでに入っている場合は「インストール済み」と表示されます。",
            expectedIfPresent = "Claude Code がインストール済みとして表示される"
        ),
        stepsIfAbsent = listOf(
            SetupStep(
                id = "cc-install",
                title = "拡張機能をインストールする",
                surface = Surface.UI,
                description = "Cursorで Ctrl+Shift+X を押して拡張機能の画面を開きます。検索欄に「Claude Code」と入力し、Anthropic が提供しているものを選んで Install を押します。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "拡張機能の画面を見る",
                    expected = "Claude Code に「インストール済み」と表示される"
                ),
                pitfall = "似た名前の拡張機能があります。提供元が Anthropic であることを必ず確認してください。",
                officialUrl = "https://code.claude.com/docs/en/vscode"
            )
        ),
        stepsIfPresent = listOf(
            SetupStep(
                id = "cc-check-signin",
                title = "サインイン状態を確認する",
                surface = Surface.UI,
                description = "Cursorでファイルを開き、右上に表示されるスパークのアイコンをクリックしてClaude Codeのパネルを開きます。前任者のアカウントのままサインインされていないかを確認します。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "パネル内でアカウント情報を見る",
                    expected = "サインインしていないか、誰のアカウントでサインインしているかが分かる"
                )
            )
        ),
        commonSteps = listOf(
            SetupStep(
                id = "cc-signin",
                title = "指定されたアカウントでサインインする",
                surface = Surface.UI,
                description = "Claude Codeのパネルに表示される Sign in を押すとブラウザが開きます。フェーズ0で確認したアカウントでログインし、許可を与えます。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "Cursorに戻ってパネルを見る",
                    expected = "サインイン画面が消え、メッセージを入力できる状態になっている"
                ),
                pitfall = "サインイン画面が消えない場合は、Ctrl+Shift+P を押して「Developer: Reload Window」を実行するとパネルが再読み込みされます。"
            ),
            SetupStep(
                id = "cc-first-prompt",
                title = "動くことを確かめる",
                surface = Surface.UI,
                description = "パネルの入力欄に「このプロジェクトの構成を教えて」などと入力して送信します。返事が返ってくれば正常に動いています。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "パネルの表示を見る",
                    expected = "AIからの返事が表示される"
                )
            )
        )
    )

    // ── 6. GitHub連携 ────────────────────────────────────

    private fun github() = SetupPhase(
        id = "github",
        title = "GitHub連携",
        summary = "GitHubはソースコードを共有する場所です。ここで認証を通すと、コードの取得や送信ができるようになります。すでに認証済みの場合は、どのアカウントで入っているかを必ず確認します。",
        lastVerified = VERIFIED,
        statusCheck = StatusCheck(
            question = "GitHub CLI(ghコマンド)で認証済みですか?",
            surface = Surface.TERMINAL,
            howToCheck = "ターミナルで次のコマンドを実行します。ログイン済みならアカウント名が表示されます。",
            checkCommand = "gh auth status",
            expectedIfPresent = "✓ Logged in to github.com account (アカウント名)"
        ),
        stepsIfAbsent = listOf(
            SetupStep(
                id = "gh-install",
                title = "GitHub CLIをインストールする",
                surface = Surface.TERMINAL,
                description = "GitHub CLI は、ターミナルからGitHubを操作する道具です。認証をブラウザ経由で安全に行えるため、これを使うのが簡単です。",
                command = "winget install --id GitHub.cli -e --source winget",
                expectedOutput = "インストールが完了しました",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "ターミナルを開き直してバージョンを確認する",
                    command = "gh --version",
                    expected = "gh version 2.96.0 のようにバージョン番号が表示される"
                ),
                pitfall = "インストール後はターミナルを閉じて開き直してください。",
                officialUrl = "https://cli.github.com/"
            ),
            SetupStep(
                id = "gh-login",
                title = "GitHubにログインする",
                surface = Surface.TERMINAL,
                description = "下のコマンドを実行すると、質問がいくつか表示されます。矢印キーで選んで Enter を押します。「GitHub.com」→「HTTPS」→「Yes」→「Login with a web browser」を選ぶと、ブラウザが開いてコードを入力する画面になります。ターミナルに表示された8桁のコードを入力します。",
                command = "gh auth login",
                expectedOutput = "✓ Authentication complete.\n✓ Logged in as (アカウント名)",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "認証状態を確認する",
                    command = "gh auth status",
                    expected = "Logged in to github.com account に、今回使うアカウント名が表示される"
                ),
                pitfall = "ブラウザで別のアカウントにログインしたままだと、そちらで認証されてしまいます。先にブラウザ側でログアウトしておくと確実です。"
            )
        ),
        stepsIfPresent = listOf(
            SetupStep(
                id = "gh-check-account",
                title = "どのアカウントで認証されているか確認する",
                surface = Surface.TERMINAL,
                description = "前任者のアカウントのまま残っていることがあります。表示されたアカウント名が、今回使うものと一致するかを確認します。",
                command = "gh auth status",
                expectedOutput = "✓ Logged in to github.com account (アカウント名)\n  - Token scopes: 'gist', 'read:org', 'repo'",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "表示されたアカウント名を見る",
                    expected = "今回使うアカウントかどうかがはっきりしている"
                )
            ),
            SetupStep(
                id = "gh-switch-account",
                title = "必要ならアカウントを切り替える",
                surface = Surface.TERMINAL,
                description = "違うアカウントだった場合は、いったんログアウトしてからログインし直します。ログアウト後に gh auth login を実行してください。",
                command = "gh auth logout",
                expectedOutput = "✓ Logged out of github.com account (アカウント名)",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "ログインし直した後に認証状態を確認する",
                    command = "gh auth status",
                    expected = "今回使うアカウント名が表示される"
                )
            )
        ),
        commonSteps = listOf(
            SetupStep(
                id = "gh-sso",
                title = "組織の承認が必要か確認する",
                surface = Surface.UI,
                description = "会社のGitHub組織がシングルサインオン(SSO)を使っている場合、認証しただけでは組織のリポジトリにアクセスできません。ブラウザでGitHubにログインし、組織のリポジトリを開けるか確認します。開けない場合は、GitHubの Settings → Applications などから組織へのアクセスを承認します。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "ブラウザで組織のリポジトリを開く",
                    expected = "リポジトリの内容が表示される"
                ),
                pitfall = "SSOの承認漏れは「認証は通っているのにアクセスできない」という分かりにくい状態を生みます。この段階で確認しておきます。"
            ),
            SetupStep(
                id = "gh-pat",
                title = "必要な場合のみアクセストークンを発行する",
                surface = Surface.UI,
                description = "gh auth login で認証できていれば、通常このステップは不要です。CI や外部ツールから使うなど、トークンが別途必要な場合だけ発行します。GitHubの Settings → Developer settings → Personal access tokens → Fine-grained tokens から作成し、権限は必要最小限に絞り、有効期限を必ず設定します。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "発行後のトークン一覧を見る",
                    expected = "作成したトークンが、意図した権限と有効期限で表示されている"
                ),
                pitfall = "発行したトークンは一度しか表示されません。その場でPC側(環境変数や認証情報マネージャ)に設定してください。このアプリには保存しません。",
                officialUrl = "https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens"
            )
        )
    )

    // ── 7. 疎通確認 ──────────────────────────────────────

    private fun smokeTest() = SetupPhase(
        id = "smoke-test",
        title = "疎通確認",
        summary = "ここまでの設定が本当に通っているかを、実際にコードを取得して送信することで確かめます。ここが通れば、実務を始められる状態です。",
        goalLabel = "立ち上げ完了",
        lastVerified = VERIFIED,
        commonSteps = listOf(
            SetupStep(
                id = "smoke-clone",
                title = "リポジトリを取得する",
                surface = Surface.TERMINAL,
                description = "担当するリポジトリのURLを使って、コードを手元にコピーします。クローンとは、GitHub上のコードを自分のPCに複製することです。URLはリポジトリのページの Code ボタンから取得できます。",
                command = "git clone (リポジトリのURL)",
                expectedOutput = "Cloning into '(リポジトリ名)'...\nReceiving objects: 100% ...",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "フォルダができたか確認する",
                    command = "ls",
                    expected = "リポジトリ名のフォルダが一覧に表示される"
                ),
                pitfall = "認証エラーが出る場合は、フェーズ6の認証かSSO承認が済んでいません。戻って確認してください。"
            ),
            SetupStep(
                id = "smoke-branch",
                title = "作業用のブランチを作る",
                surface = Surface.TERMINAL,
                description = "いきなり本流(main)を変更せず、作業用の枝を作ってから編集するのが実務の基本です。まずクローンしたフォルダに移動してから実行します。",
                command = "cd (リポジトリ名); git switch -c setup-check",
                expectedOutput = "Switched to a new branch 'setup-check'",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "今いるブランチを確認する",
                    command = "git branch --show-current",
                    expected = "setup-check と表示される"
                )
            ),
            SetupStep(
                id = "smoke-edit",
                title = "ファイルを編集する",
                surface = Surface.UI,
                description = "Cursorでこのフォルダを開き、README などのファイルを少しだけ編集して保存します。動作確認用なので、内容は空行の追加程度で構いません。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "Cursorの画面を見る",
                    expected = "編集したファイル名の横に、変更を示す印が付いている"
                )
            ),
            SetupStep(
                id = "smoke-commit",
                title = "変更を記録する",
                surface = Surface.TERMINAL,
                description = "変更を記録します。add は記録する対象を選ぶ操作、commit はその記録を確定する操作です。",
                command = "git add .; git commit -m \"疎通確認\"",
                expectedOutput = "[setup-check 1a2b3c4] 疎通確認\n 1 file changed, 1 insertion(+)",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "コミットした人を確認する",
                    command = "git log -1 --format=\"%an <%ae>\"",
                    expected = "フェーズ2で設定した、自分の名前とメールアドレスが表示される"
                ),
                pitfall = "ここで他人の名前が出た場合は、フェーズ2の設定が反映されていません。戻って設定し直し、このコミットはやり直してください。"
            ),
            SetupStep(
                id = "smoke-push",
                title = "GitHubへ送信する",
                surface = Surface.TERMINAL,
                description = "手元の記録をGitHub上に送ります。これが通れば、認証も含めて一通りの設定ができています。",
                command = "git push -u origin setup-check",
                expectedOutput = "To https://github.com/(組織名)/(リポジトリ名).git\n * [new branch]      setup-check -> setup-check",
                verification = Verification(
                    surface = Surface.UI,
                    how = "ブラウザでリポジトリのページを開く",
                    expected = "setup-check ブランチが表示され、自分のコミットが載っている"
                ),
                pitfall = "権限エラーが出る場合は、そのリポジトリへの書き込み権限がない可能性があります。担当者に確認してください。"
            ),
            SetupStep(
                id = "smoke-cleanup",
                title = "確認用のブランチを片付ける",
                surface = Surface.TERMINAL,
                description = "動作確認のために作ったブランチは、共有リポジトリに残さず消しておきます。",
                command = "git push origin --delete setup-check; git switch main",
                expectedOutput = "- [deleted]         setup-check\nSwitched to branch 'main'",
                verification = Verification(
                    surface = Surface.UI,
                    how = "ブラウザでリポジトリのブランチ一覧を見る",
                    expected = "setup-check が残っていない"
                )
            )
        )
    )

    // ── 8. 自走準備 ──────────────────────────────────────

    private fun selfSufficiency() = SetupPhase(
        id = "self-sufficiency",
        title = "自走準備",
        summary = "ここからは、分からないことをAIとGitHubで自分で解決していける状態を作ります。この状態になれば、この手順書がなくても進められます。",
        goalLabel = "自己対処可能",
        lastVerified = VERIFIED,
        commonSteps = listOf(
            SetupStep(
                id = "self-ask-ai",
                title = "AIにコードについて質問してみる",
                surface = Surface.UI,
                description = "Cursorで担当するリポジトリを開き、Claude Codeのパネルで「このプロジェクトは何をするものか説明して」と聞いてみます。コードを読む前に全体像をつかむ使い方です。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "返ってきた説明を読む",
                    expected = "プロジェクトの概要が説明されている"
                )
            ),
            SetupStep(
                id = "self-mention",
                title = "ファイルを指定して質問する方法を覚える",
                surface = Surface.UI,
                description = "入力欄で @ を打つとファイル名の候補が出ます。@ファイル名 の形で指定すると、そのファイルの内容を踏まえて答えてくれます。エラーが出たファイルを指定して原因を聞く、といった使い方ができます。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "実際に @ を打って任意のファイルを指定し、質問してみる",
                    expected = "そのファイルの内容に基づいた回答が返ってくる"
                )
            ),
            SetupStep(
                id = "self-error-flow",
                title = "エラーが出たときの調べ方を決めておく",
                surface = Surface.UI,
                description = "エラーメッセージをそのままコピーしてAIに貼り付けるのが最短です。それでも解決しない場合は、リポジトリのissue(課題の記録場所)を検索し、同じ問題が報告されていないか探します。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "自分の中で手順を言葉にしてみる",
                    expected = "「まずAIに貼る、次にissueを検索する、それでも駄目ならチームに聞く」と説明できる"
                )
            ),
            SetupStep(
                id = "self-team-flow",
                title = "チームの進め方を確認する",
                surface = Surface.UI,
                description = "ブランチの名前の付け方、プルリクエスト(変更を取り込んでもらう依頼)の出し方、レビューを誰に依頼するかは、チームごとにルールがあります。リポジトリの README や CONTRIBUTING というファイルに書かれていることが多いので、まずそこを読みます。",
                verification = Verification(
                    surface = Surface.UI,
                    how = "リポジトリのREADMEやCONTRIBUTINGを開く",
                    expected = "チームの進め方が書かれた場所を把握している。なければ担当者に確認する"
                )
            ),
            SetupStep(
                id = "self-cleanup",
                title = "作業後に残さないものを確認する",
                surface = Surface.UI,
                description = "共有PCや貸与PCの場合、離任時にアカウント情報を残さないようにします。Cursor と Claude Code からサインアウトし、ターミナルで gh auth logout を実行し、git config --global --unset user.email で設定を消します。現場のルールに従ってください。",
                verification = Verification(
                    surface = Surface.TERMINAL,
                    how = "離任時に認証が残っていないことを確認する",
                    command = "gh auth status",
                    expected = "ログインしていない旨が表示される"
                ),
                pitfall = "これは離任時に行う作業です。今すぐ実行する必要はありませんが、何を消すべきかを今のうちに把握しておきます。"
            )
        )
    )
}

# リリース手順

タグを打つと、GitHub Actions が署名付きAPKをビルドして Releases に添付する。

## 初回のみ: 署名鍵の準備

Androidアプリは署名がないとインストールできない。**この鍵は再発行できず、紛失すると同じ鍵で更新版を出せなくなる**ため、作成後の保管まで含めて行う。

### 1. 鍵を作る

PowerShell で実行する(JDKに付属する `keytool` を使う)。

```powershell
keytool -genkeypair -v -keystore release.keystore -alias setup-assistant `
  -keyalg RSA -keysize 2048 -validity 10000
```

- パスワードを2回聞かれる。**控えておく**(後でSecretsに登録する)
- 名前や組織名を聞かれるが、個人開発なら実在の氏名でなくてよい。最後に `yes` を入力する
- `-validity 10000` は約27年。ストア公開の要件に合わせた慣例的な値

### 2. 鍵を保管する

- `release.keystore` を、リポジトリの外の安全な場所に保存する
- **リポジトリにコミットしない**(`.gitignore` で除外済み)
- パスワードは鍵と別の場所に保管する

### 3. GitHub Secrets に登録する

鍵をテキスト化する。

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) | Set-Clipboard
```

リポジトリの **Settings → Secrets and variables → Actions → New repository secret** から、4つ登録する。

| 名前 | 値 |
|---|---|
| `KEYSTORE_BASE64` | 上でクリップボードにコピーした文字列 |
| `KEYSTORE_PASSWORD` | keystore のパスワード |
| `KEY_ALIAS` | `setup-assistant`(手順1で指定した alias) |
| `KEY_PASSWORD` | 鍵のパスワード(keystore と同じにした場合は同じ値) |

## リリースする

### 1. バージョンを上げる

`app/build.gradle.kts` の `versionCode` と `versionName` を更新する。`versionCode` は整数で、リリースのたびに必ず増やす。

### 2. タグを打つ

```powershell
git tag v0.1.0
git push origin v0.1.0
```

タグ名は `v` から始める(ワークフローがこの形で反応する)。

### 3. 結果を確認する

Actions の Release ワークフローが完了すると、Releases にAPKが添付される。

ワークフローは公開前に `apksigner verify` で署名を確かめる。署名されていないAPKが配布されることはない。

## 配布URL

- 最新版: https://github.com/doryu0-04092/setup-assistant/releases/latest
- 一覧: https://github.com/doryu0-04092/setup-assistant/releases

## 配布前に確認すること

- [ ] `docs/manual-test.md` の手動確認を通す(特にパスワードの生体認証。E2Eでは検証できない)
- [ ] 開発に関わっていない人に、READMEの手順だけを見てインストールしてもらい、詰まる箇所がないか確認する

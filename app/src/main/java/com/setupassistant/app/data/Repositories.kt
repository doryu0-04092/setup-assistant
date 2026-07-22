package com.setupassistant.app.data

import android.content.Context

/**
 * リポジトリをアプリ全体で1つずつ共有する。
 *
 * 暗号化ストレージの生成は Keystore アクセスを伴うため、画面を開くたびに
 * 作り直したくない。あわせて applicationContext を渡し、Activity を
 * 抱え込まないようにしている。
 */
object Repositories {

    @Volatile private var progress: ProgressRepository? = null
    @Volatile private var userEdits: UserEditRepository? = null
    @Volatile private var accountProfiles: AccountProfileRepository? = null
    @Volatile private var secrets: SecretRepository? = null

    fun progress(context: Context): ProgressRepository =
        progress ?: synchronized(this) {
            progress ?: ProgressRepository(context.applicationContext).also { progress = it }
        }

    fun userEdits(context: Context): UserEditRepository =
        userEdits ?: synchronized(this) {
            userEdits ?: UserEditRepository(context.applicationContext).also { userEdits = it }
        }

    fun accountProfiles(context: Context): AccountProfileRepository =
        accountProfiles ?: synchronized(this) {
            accountProfiles
                ?: AccountProfileRepository(context.applicationContext).also { accountProfiles = it }
        }

    fun secrets(context: Context): SecretRepository =
        secrets ?: synchronized(this) {
            secrets ?: SecretRepository(context.applicationContext).also { secrets = it }
        }
}

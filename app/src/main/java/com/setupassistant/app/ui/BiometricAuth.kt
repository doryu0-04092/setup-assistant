package com.setupassistant.app.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val AUTHENTICATORS =
    BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL

/** 生体認証か画面ロックのPINが使える状態か */
fun canAuthenticate(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) ==
        BiometricManager.BIOMETRIC_SUCCESS

/**
 * パスワードを扱う前に本人確認を求める。
 *
 * 指紋・顔認証のほか、端末の画面ロックのPINでも通過できるようにしている。
 * 生体情報を登録していない端末でも使えるようにするため。
 */
fun promptForAuth(
    context: Context,
    title: String,
    subtitle: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val activity = context.findActivity()
    if (activity == null) {
        onFailure("認証画面を開けませんでした")
        return
    }

    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(context),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // 利用者が自分で閉じた場合はエラー表示しない
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                ) {
                    onFailure(errString.toString())
                }
            }
        }
    )

    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setAllowedAuthenticators(AUTHENTICATORS)
        .build()

    prompt.authenticate(info)
}

private fun Context.findActivity(): FragmentActivity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return null
}

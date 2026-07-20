package com.setupassistant.app.data

/** 操作をどこで行うか。表示の出し分けに使う */
enum class Surface { TERMINAL, UI }

/** そのステップが成功したかどうかの確かめ方 */
data class Verification(
    val surface: Surface,
    val how: String,
    val command: String? = null,
    val expected: String
)

data class SetupStep(
    /** 進捗保存のキーになるため、一度決めたら変えない */
    val id: String,
    val title: String,
    val surface: Surface,
    val description: String,
    val command: String? = null,
    val expectedOutput: String? = null,
    val verification: Verification? = null,
    val pitfall: String? = null,
    val officialUrl: String? = null
)

/** フェーズ冒頭で現状を確かめ、以降の手順を分岐させる */
data class StatusCheck(
    val question: String,
    val surface: Surface,
    val howToCheck: String,
    val checkCommand: String? = null,
    val expectedIfPresent: String
)

/** 現状確認に対する利用者の回答 */
enum class InstallState { UNKNOWN, ABSENT, PRESENT }

data class SetupPhase(
    val id: String,
    val title: String,
    val summary: String,
    /** 到達目標を示すフェーズにだけ付ける(例: 立ち上げ完了) */
    val goalLabel: String? = null,
    /** null なら分岐なし。commonSteps だけを使う */
    val statusCheck: StatusCheck? = null,
    val stepsIfAbsent: List<SetupStep> = emptyList(),
    val stepsIfPresent: List<SetupStep> = emptyList(),
    val commonSteps: List<SetupStep> = emptyList(),
    /** 公式サイトで手順を最終確認した日 */
    val lastVerified: String
) {
    /** 現状確認の回答に応じて、実際に表示するステップを返す */
    fun stepsFor(state: InstallState): List<SetupStep> = when {
        statusCheck == null -> commonSteps
        state == InstallState.ABSENT -> stepsIfAbsent + commonSteps
        state == InstallState.PRESENT -> stepsIfPresent + commonSteps
        else -> emptyList()
    }
}

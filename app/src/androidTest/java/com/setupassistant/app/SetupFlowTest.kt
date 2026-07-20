package com.setupassistant.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.setupassistant.app.data.AccountProfileRepository
import com.setupassistant.app.data.ProgressRepository
import com.setupassistant.app.data.UserEditRepository
import com.setupassistant.app.ui.AppScaffold
import com.setupassistant.app.ui.TAB_TAG_ACCOUNTS
import com.setupassistant.app.ui.TAB_TAG_PRINCIPLES
import com.setupassistant.app.ui.TAB_TAG_SETUP
import com.setupassistant.app.ui.theme.SetupAssistantTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 主要フローのE2Eテスト。
 *
 * パスワードまわりは BiometricPrompt がシステムUIのため自動化できない。
 * そこは docs/manual-test.md の手動確認に委ねている。
 */
@RunWith(AndroidJUnit4::class)
class SetupFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        clearStoredState()
        composeRule.setContent {
            SetupAssistantTheme {
                AppScaffold()
            }
        }
    }

    @Test
    fun 一覧が初期状態で表示される() {
        composeRule.onNodeWithText("現場ルールの確認").assertIsDisplayed()
        composeRule.onNodeWithText("0 / 4 完了").assertIsDisplayed()
    }

    @Test
    fun 一覧に全フェーズが並ぶ() {
        composeRule.onNodeWithText("現場ルールの確認").assertIsDisplayed()

        // 末尾のフェーズまでスクロールできる = 一覧が最後まで構築されている
        composeRule.scrollTo("自走準備")
        composeRule.onNodeWithText("自走準備").assertIsDisplayed()
        composeRule.onNodeWithText("自己対処可能").assertIsDisplayed()
    }

    @Test
    fun フェーズ詳細に入って戻れる() {
        composeRule.onNodeWithText("現場ルールの確認").performClick()

        // 詳細にだけ出るステップが見えること
        composeRule.onNodeWithText("使うアカウントを確認する").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("戻る").performClick()
        composeRule.onNodeWithText("現状の棚卸し").assertIsDisplayed()
    }

    @Test
    fun 現状確認の選択で表示される手順が変わる() {
        composeRule.openPhase("Git")

        // 選ぶまではどちらの手順も出さない
        composeRule.onNodeWithText("上の確認結果を選ぶと、あなたに必要な手順だけが表示されます。")
            .assertIsDisplayed()

        composeRule.onNodeWithText("入っていない").performClick()
        composeRule.onNodeWithText("Gitをインストールする").assertIsDisplayed()

        composeRule.onNodeWithText("入っている").performClick()
        composeRule.onNodeWithText("今の設定を確認する").assertIsDisplayed()
        composeRule.onAllNodesWithText("Gitをインストールする").assertCountEquals(0)
    }

    @Test
    fun フェーズをまとめて完了にできる() {
        composeRule.onNodeWithText("現場ルールの確認").performClick()

        composeRule.onNodeWithText("このフェーズを完了にする", substring = true).performClick()
        composeRule.onNodeWithText("完了済み — 取り消す").assertIsDisplayed()

        // 一覧にも反映される
        composeRule.onNodeWithContentDescription("戻る").performClick()
        composeRule.onNodeWithText("4 / 4 完了").assertIsDisplayed()

        // 取り消せる
        composeRule.onNodeWithText("現場ルールの確認").performClick()
        composeRule.onNodeWithText("完了済み — 取り消す").performClick()
        composeRule.onNodeWithText("このフェーズを完了にする", substring = true).assertIsDisplayed()
    }

    @Test
    fun 分岐未選択のフェーズには一括完了ボタンを出さない() {
        composeRule.openPhase("Git")

        composeRule.onAllNodesWithText("このフェーズを完了にする", substring = true)
            .assertCountEquals(0)
    }

    @Test
    fun タブを移動できる() {
        composeRule.onNodeWithTag(TAB_TAG_ACCOUNTS).performClick()
        composeRule.onNodeWithText("まだ登録がありません").assertIsDisplayed()

        composeRule.onNodeWithTag(TAB_TAG_PRINCIPLES).performClick()
        composeRule.onNodeWithText("このアプリが保存するもの・しないもの").assertIsDisplayed()

        composeRule.onNodeWithTag(TAB_TAG_SETUP).performClick()
        composeRule.onNodeWithText("現場ルールの確認").assertIsDisplayed()
    }

    @Test
    fun 登録したメールアドレスがコマンドに差し込まれる() {
        val email = "onsite@example.com"

        composeRule.onNodeWithTag(TAB_TAG_ACCOUNTS).performClick()
        composeRule.onNodeWithContentDescription("アカウントを追加").performClick()

        composeRule.onNodeWithText("この登録の名前 (例: 常駐先A)").performTextInput("常駐先A")
        composeRule.onNodeWithText("登録メールアドレス").performTextInput(email)
        composeRule.onNodeWithText("保存").performClick()

        composeRule.onNodeWithText("常駐先A").assertIsDisplayed()

        // Git設定のコマンドに実値が入ること
        composeRule.onNodeWithTag(TAB_TAG_SETUP).performClick()
        composeRule.openPhase("Git")
        composeRule.onNodeWithText("入っていない").performClick()

        composeRule.scrollTo(email, substring = true)
        composeRule.onAllNodesWithText(email, substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun メールアドレス未登録なら何を入れるべきか示す() {
        composeRule.openPhase("Git")
        composeRule.onNodeWithText("入っていない").performClick()

        composeRule.scrollTo("<登録したメールアドレス>", substring = true)
        composeRule.onAllNodesWithText("<登録したメールアドレス>", substring = true)
            .onFirst()
            .assertIsDisplayed()
    }
}

private fun ComposeContentTestRule.scrollTo(text: String, substring: Boolean = false) {
    onNode(hasScrollAction()).performScrollToNode(hasText(text, substring = substring))
}

private fun ComposeContentTestRule.openPhase(title: String) {
    scrollTo(title)
    onNodeWithText(title).performClick()
}

/**
 * テスト間で進捗や登録内容が引き継がれないようにする。
 *
 * ファイルを消すだけでは SharedPreferences のメモリ上のキャッシュが残るため、
 * リポジトリ経由で消す。
 */
private fun clearStoredState() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    runBlocking { ProgressRepository(context).clearAll() }
    AccountProfileRepository(context).clearAll()
    UserEditRepository(context).clearAll()
}

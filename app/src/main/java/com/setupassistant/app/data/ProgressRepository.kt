package com.setupassistant.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progressDataStore by preferencesDataStore(name = "setup_progress")

/**
 * チェック済みのステップと、フェーズごとの分岐の選択状態を保持する。
 *
 * ここで扱うのは進捗だけで、秘密情報は含まないため通常のDataStoreを使う。
 */
class ProgressRepository(private val context: Context) {

    private val checkedStepsKey = stringSetPreferencesKey("checked_steps")

    val checkedSteps: Flow<Set<String>> =
        context.progressDataStore.data.map { it[checkedStepsKey] ?: emptySet() }

    suspend fun setStepChecked(stepId: String, checked: Boolean) {
        setStepsChecked(listOf(stepId), checked)
    }

    /** フェーズをまとめて完了にする / 完了を取り消す */
    suspend fun setStepsChecked(stepIds: List<String>, checked: Boolean) {
        context.progressDataStore.edit { prefs ->
            val current = prefs[checkedStepsKey] ?: emptySet()
            prefs[checkedStepsKey] = if (checked) current + stepIds else current - stepIds.toSet()
        }
    }

    /** 進捗を全て消す。離任時のデータ消去とテストの初期化で使う */
    suspend fun clearAll() {
        context.progressDataStore.edit { it.clear() }
    }

    fun installState(phaseId: String): Flow<InstallState> =
        context.progressDataStore.data.map { prefs -> prefs.readInstallState(phaseId) }

    /** 一覧画面で全フェーズの進捗を出すために、まとめて読む */
    val allInstallStates: Flow<Map<String, InstallState>> =
        context.progressDataStore.data.map { prefs ->
            SetupContent.phases.associate { it.id to prefs.readInstallState(it.id) }
        }

    private fun androidx.datastore.preferences.core.Preferences.readInstallState(
        phaseId: String
    ): InstallState =
        this[installStateKey(phaseId)]
            ?.let { runCatching { InstallState.valueOf(it) }.getOrNull() }
            ?: InstallState.UNKNOWN

    suspend fun setInstallState(phaseId: String, state: InstallState) {
        context.progressDataStore.edit { prefs ->
            prefs[installStateKey(phaseId)] = state.name
        }
    }

    private fun installStateKey(phaseId: String) = stringPreferencesKey("install_state_$phaseId")
}

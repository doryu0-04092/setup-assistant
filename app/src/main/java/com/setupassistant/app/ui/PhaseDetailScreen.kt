package com.setupassistant.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setupassistant.app.data.AccountProfile
import com.setupassistant.app.data.InstallState
import com.setupassistant.app.data.Repositories
import com.setupassistant.app.data.SetupContent
import com.setupassistant.app.data.SetupStep
import com.setupassistant.app.data.StatusCheck
import com.setupassistant.app.data.StepEdit
import com.setupassistant.app.data.Surface
import com.setupassistant.app.data.Verification
import com.setupassistant.app.data.withAccountValues
import com.setupassistant.app.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun PhaseDetailScreen(phaseId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val progressRepository = remember { Repositories.progress(context) }
    val editRepository = remember { Repositories.userEdits(context) }
    val profileRepository = remember { Repositories.accountProfiles(context) }

    // 参照のたびに読み直す。remember に包むと、後からアカウントを登録・切り替えしても
    // 開いたことのある画面が古い値のままになる
    val activeProfile = profileRepository.getActive()
    val scope = rememberCoroutineScope()
    val phase = remember(phaseId) { SetupContent.findPhase(phaseId) }

    if (phase == null) {
        Text("手順が見つかりませんでした", modifier = modifier.padding(16.dp))
        return
    }

    val checkedSteps by progressRepository.checkedSteps.collectAsStateWithLifecycle(emptySet())
    val installState by progressRepository.installState(phaseId)
        .collectAsStateWithLifecycle(InstallState.UNKNOWN)

    val steps = phase.stepsFor(installState)

    // 保存のたびに読み直すため、編集内容はバージョンを上げて再取得する
    var editVersion by remember { mutableIntStateOf(0) }
    val edits = remember(steps, editVersion) {
        steps.associate { it.id to editRepository.get(it.id) }
    }
    var editingStep by remember { mutableStateOf<SetupStep?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Spacing.Large),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                Text(text = phase.summary, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "公式サイトで最終確認: ${phase.lastVerified}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PhaseCompleteButton(
                    stepCount = steps.size,
                    doneCount = steps.count { checkedSteps.contains(it.id) },
                    onToggle = { complete ->
                        scope.launch {
                            progressRepository.setStepsChecked(steps.map { it.id }, complete)
                        }
                    }
                )
            }
        }

        phase.statusCheck?.let { check ->
            item {
                StatusCheckCard(
                    check = check,
                    selected = installState,
                    onSelect = { scope.launch { progressRepository.setInstallState(phaseId, it) } }
                )
            }
        }

        if (phase.statusCheck != null && installState == InstallState.UNKNOWN) {
            item {
                Text(
                    text = "上の確認結果を選ぶと、あなたに必要な手順だけが表示されます。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(steps, key = { it.id }) { step ->
            StepCard(
                step = step,
                edit = edits[step.id] ?: StepEdit(),
                activeProfile = activeProfile,
                checked = checkedSteps.contains(step.id),
                onCheckedChange = { scope.launch { progressRepository.setStepChecked(step.id, it) } },
                onEditClick = { editingStep = step }
            )
        }
    }

    editingStep?.let { step ->
        StepEditDialog(
            step = step,
            edit = edits[step.id] ?: StepEdit(),
            onDismiss = { editingStep = null },
            onSave = {
                editRepository.save(step.id, it)
                editVersion++
                editingStep = null
            },
            onResetOverride = {
                editRepository.clearOverride(step.id)
                editVersion++
                editingStep = null
            }
        )
    }
}

/**
 * フェーズをまとめて完了にする。
 *
 * すでに設定済みの環境では、1つずつチェックを付けるより
 * フェーズごと完了にできた方が早い。
 */
@Composable
private fun PhaseCompleteButton(
    stepCount: Int,
    doneCount: Int,
    onToggle: (Boolean) -> Unit
) {
    // 分岐が未選択だと表示するステップがなく、完了にする対象もない
    if (stepCount == 0) return

    val allDone = doneCount == stepCount

    if (allDone) {
        OutlinedButton(
            onClick = { onToggle(false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("完了済み — 取り消す")
        }
    } else {
        Button(
            onClick = { onToggle(true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("このフェーズを完了にする ($doneCount / $stepCount)")
        }
    }
}

@Composable
private fun StatusCheckCard(
    check: StatusCheck,
    selected: InstallState,
    onSelect: (InstallState) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            Text(text = "まず確認", style = MaterialTheme.typography.labelMedium)
            Text(text = check.question, style = MaterialTheme.typography.titleMedium)
            SurfaceLabel(check.surface)
            Text(text = check.howToCheck, style = MaterialTheme.typography.bodyMedium)

            check.checkCommand?.let {
                CommandBlock(command = it, expectedOutput = check.expectedIfPresent)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                FilterChip(
                    selected = selected == InstallState.ABSENT,
                    onClick = { onSelect(InstallState.ABSENT) },
                    label = { Text("入っていない") }
                )
                FilterChip(
                    selected = selected == InstallState.PRESENT,
                    onClick = { onSelect(InstallState.PRESENT) },
                    label = { Text("入っている") }
                )
            }
        }
    }
}

@Composable
private fun StepCard(
    step: SetupStep,
    edit: StepEdit,
    activeProfile: AccountProfile?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, onCheckedChange = onCheckedChange)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Tight)
                ) {
                    Text(text = step.title, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                        SurfaceLabel(step.surface)
                        if (edit.hasOverride) {
                            Text(
                                text = "編集済み",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = if (edit.isEmpty) Icons.Default.EditNote else Icons.Default.Edit,
                        contentDescription = "メモと手順の編集"
                    )
                }
            }

            Text(
                text = edit.description ?: step.description,
                style = MaterialTheme.typography.bodyMedium
            )

            (edit.command ?: step.command)?.let {
                CommandBlock(
                    command = it.withAccountValues(activeProfile),
                    expectedOutput = step.expectedOutput
                )
            }

            step.verification?.let { VerificationBlock(it) }

            step.pitfall?.let {
                NoticeBlock(
                    label = "詰まったら",
                    container = MaterialTheme.colorScheme.errorContainer,
                    onContainer = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (edit.note.isNotBlank()) {
                NoticeBlock(
                    label = "メモ",
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    onContainer = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Text(text = edit.note, style = MaterialTheme.typography.bodySmall)
                }
            }

            step.officialUrl?.let { url ->
                TextButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("公式サイトを開く")
                }
            }
        }
    }
}

/**
 * 本文とは別の性質の情報を、背景を敷いて見分けられるようにする。
 * 説明・コマンド・確認方法・詰まりポイントが同じ見た目で並ぶと、
 * どれが何なのか読み取れないため。
 */
@Composable
private fun NoticeBlock(
    label: String,
    container: Color,
    onContainer: Color,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Surface(
        color = container,
        contentColor = onContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.Tight)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            content()
        }
    }
}

@Composable
private fun VerificationBlock(verification: Verification) {
    NoticeBlock(
        label = "できたかどうかの確認",
        container = MaterialTheme.colorScheme.primaryContainer,
        onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
            Text(text = verification.how, style = MaterialTheme.typography.bodySmall)

            verification.command?.let { CommandBlock(command = it) }

            Text(
                text = "✓ ${verification.expected}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** ターミナルでの操作か、画面上の操作かを示すラベル */
@Composable
private fun SurfaceLabel(surface: Surface) {
    val (icon, label) = when (surface) {
        Surface.TERMINAL -> Icons.Default.Terminal to "ターミナルで操作"
        Surface.UI -> Icons.Default.TouchApp to "画面上で操作"
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(end = 2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

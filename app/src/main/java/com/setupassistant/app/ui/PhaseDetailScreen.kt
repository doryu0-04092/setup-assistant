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
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setupassistant.app.data.InstallState
import com.setupassistant.app.data.ProgressRepository
import com.setupassistant.app.data.SetupContent
import com.setupassistant.app.data.SetupStep
import com.setupassistant.app.data.StatusCheck
import com.setupassistant.app.data.Surface
import com.setupassistant.app.data.Verification
import kotlinx.coroutines.launch

@Composable
fun PhaseDetailScreen(phaseId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember { ProgressRepository(context) }
    val scope = rememberCoroutineScope()
    val phase = remember(phaseId) { SetupContent.findPhase(phaseId) }

    if (phase == null) {
        Text("手順が見つかりませんでした", modifier = modifier.padding(16.dp))
        return
    }

    val checkedSteps by repository.checkedSteps.collectAsStateWithLifecycle(emptySet())
    val installState by repository.installState(phaseId)
        .collectAsStateWithLifecycle(InstallState.UNKNOWN)

    val steps = phase.stepsFor(installState)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = phase.summary, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "公式サイトで最終確認: ${phase.lastVerified}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        phase.statusCheck?.let { check ->
            item {
                StatusCheckCard(
                    check = check,
                    selected = installState,
                    onSelect = { scope.launch { repository.setInstallState(phaseId, it) } }
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
                checked = checkedSteps.contains(step.id),
                onCheckedChange = { scope.launch { repository.setStepChecked(step.id, it) } }
            )
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "まず確認", style = MaterialTheme.typography.labelMedium)
            Text(
                text = check.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            SurfaceLabel(check.surface)
            Text(text = check.howToCheck, style = MaterialTheme.typography.bodyMedium)

            check.checkCommand?.let {
                CommandBlock(command = it, expectedOutput = check.expectedIfPresent)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, onCheckedChange = onCheckedChange)
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            SurfaceLabel(step.surface)

            Text(text = step.description, style = MaterialTheme.typography.bodyMedium)

            step.command?.let {
                CommandBlock(command = it, expectedOutput = step.expectedOutput)
            }

            step.verification?.let { VerificationBlock(it) }

            step.pitfall?.let {
                Text(
                    text = "⚠ $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            step.officialUrl?.let { url ->
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }) {
                    Text("公式サイトを開く")
                }
            }
        }
    }
}

@Composable
private fun VerificationBlock(verification: Verification) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "できたかどうかの確認",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = verification.how, style = MaterialTheme.typography.bodySmall)

        verification.command?.let { CommandBlock(command = it) }

        Text(
            text = "✓ ${verification.expected}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
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

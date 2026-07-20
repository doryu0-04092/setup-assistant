package com.setupassistant.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setupassistant.app.data.InstallState
import com.setupassistant.app.data.ProgressRepository
import com.setupassistant.app.data.SetupContent
import com.setupassistant.app.data.SetupPhase

@Composable
fun SetupGuideScreen(
    onPhaseClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { ProgressRepository(context) }

    val checkedSteps by repository.checkedSteps.collectAsStateWithLifecycle(emptySet())
    val installStates by repository.allInstallStates.collectAsStateWithLifecycle(emptyMap())

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "上から順に進めます。現場のPCに何が入っているかで手順が変わるため、各フェーズの最初に現状を確認します。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        itemsIndexed(SetupContent.phases, key = { _, phase -> phase.id }) { index, phase ->
            PhaseCard(
                index = index,
                phase = phase,
                installState = installStates[phase.id] ?: InstallState.UNKNOWN,
                checkedSteps = checkedSteps,
                onClick = { onPhaseClick(phase.id) }
            )
        }
    }
}

@Composable
private fun PhaseCard(
    index: Int,
    phase: SetupPhase,
    installState: InstallState,
    checkedSteps: Set<String>,
    onClick: () -> Unit
) {
    val steps = phase.stepsFor(installState)
    val doneCount = steps.count { checkedSteps.contains(it.id) }
    val needsChoice = phase.statusCheck != null && installState == InstallState.UNKNOWN

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = phase.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            phase.goalLabel?.let { label ->
                AssistChip(
                    onClick = onClick,
                    label = { Text(label) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Text(
                text = phase.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                needsChoice -> Text(
                    text = "現状の確認が必要です",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                steps.isNotEmpty() -> {
                    LinearProgressIndicator(
                        progress = { doneCount.toFloat() / steps.size },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "$doneCount / ${steps.size} 完了",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

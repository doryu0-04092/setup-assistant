package com.setupassistant.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.setupassistant.app.data.SetupGuide
import com.setupassistant.app.data.SetupGuides

@Composable
fun SetupGuideScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(SetupGuides.all) { guide ->
            SetupGuideCard(guide)
        }
    }
}

@Composable
private fun SetupGuideCard(guide: SetupGuide) {
    val context = LocalContext.current
    val checkedSteps = remember { mutableStateMapOf<Int, Boolean>() }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = guide.title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "公式サイトで最終確認: ${guide.lastVerified}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            guide.steps.forEachIndexed { index, step ->
                Row {
                    Checkbox(
                        checked = checkedSteps[index] ?: false,
                        onCheckedChange = { isChecked -> checkedSteps[index] = isChecked }
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(guide.officialUrl))
                context.startActivity(intent)
            }) {
                Text("公式サイトを開く")
            }
        }
    }
}

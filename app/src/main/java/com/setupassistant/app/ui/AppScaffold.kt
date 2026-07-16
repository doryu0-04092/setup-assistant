package com.setupassistant.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

private enum class AppTab(val label: String) {
    SETUP("セットアップ"),
    VAULT("Vault")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = AppTab.entries

    Scaffold(
        topBar = { TopAppBar(title = { Text(tabs[selectedTab].label) }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                    label = { Text(AppTab.SETUP.label) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    label = { Text(AppTab.VAULT.label) }
                )
            }
        }
    ) { innerPadding ->
        when (tabs[selectedTab]) {
            AppTab.SETUP -> SetupGuideScreen(modifier = Modifier.padding(innerPadding))
            AppTab.VAULT -> Box(modifier = Modifier.padding(innerPadding)) {
                VaultScreen()
            }
        }
    }
}

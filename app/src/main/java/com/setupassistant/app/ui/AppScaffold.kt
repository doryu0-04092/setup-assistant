package com.setupassistant.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.setupassistant.app.data.SetupContent

private const val ROUTE_LIST = "phases"
private const val ROUTE_DETAIL = "phases/{phaseId}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentPhaseId = backStackEntry?.arguments?.getString("phaseId")
    val isDetail = backStackEntry?.destination?.route == ROUTE_DETAIL
    val title = if (isDetail) {
        currentPhaseId?.let { SetupContent.findPhase(it)?.title } ?: "セットアップ"
    } else {
        "セットアップ"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (isDetail) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "戻る"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_LIST,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_LIST) {
                SetupGuideScreen(
                    onPhaseClick = { phaseId -> navController.navigate("phases/$phaseId") }
                )
            }
            composable(
                route = ROUTE_DETAIL,
                arguments = listOf(navArgument("phaseId") { type = NavType.StringType })
            ) { entry ->
                PhaseDetailScreen(phaseId = entry.arguments?.getString("phaseId").orEmpty())
            }
        }
    }
}

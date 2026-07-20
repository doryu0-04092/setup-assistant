package com.setupassistant.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.setupassistant.app.data.SetupContent

private const val ROUTE_LIST = "phases"
private const val ROUTE_DETAIL = "phases/{phaseId}"
private const val ROUTE_PRINCIPLES = "principles"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    val route = backStackEntry?.destination?.route
    val isDetail = route == ROUTE_DETAIL

    val title = when {
        isDetail -> backStackEntry?.arguments?.getString("phaseId")
            ?.let { SetupContent.findPhase(it)?.title }
            ?: "セットアップ"

        route == ROUTE_PRINCIPLES -> "安全な進め方"
        else -> "セットアップ"
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
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = route == ROUTE_LIST || isDetail,
                    onClick = { navController.switchTab(ROUTE_LIST) },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                    label = { Text("セットアップ") }
                )
                NavigationBarItem(
                    selected = route == ROUTE_PRINCIPLES,
                    onClick = { navController.switchTab(ROUTE_PRINCIPLES) },
                    icon = { Icon(Icons.Default.Shield, contentDescription = null) },
                    label = { Text("安全な進め方") }
                )
            }
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
            composable(ROUTE_PRINCIPLES) {
                PrinciplesScreen()
            }
        }
    }
}

/** タブ切り替え。同じタブを積み上げず、状態は保持する */
private fun androidx.navigation.NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

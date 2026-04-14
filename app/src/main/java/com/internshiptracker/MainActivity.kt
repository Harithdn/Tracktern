package com.internshiptracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.internshiptracker.ui.navigation.AppNavGraph
import com.internshiptracker.ui.navigation.Screen
import com.internshiptracker.ui.theme.InternshipTrackerTheme
import com.internshiptracker.viewmodel.ApplicationViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity — hosts the full Compose UI.
 * Navigation is handled by NavController inside [AppNavGraph].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InternshipTrackerTheme {
                TrackternApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackternApp() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    // Bottom navigation items
    val navItems = listOf(
        BottomNavItem("Dashboard",    Screen.Dashboard.route,    Icons.Default.Dashboard),
        BottomNavItem("Applications", Screen.Applications.route, Icons.Default.List),
        BottomNavItem("Board",        Screen.Kanban.route,       Icons.Default.ViewKanban),
        BottomNavItem("Search",       Screen.Search.route,       Icons.Default.Search),
        BottomNavItem("Analytics",    Screen.Analytics.route,    Icons.Default.Analytics)
    )

    // Only show bottom bar on the five top-level screens
    val topLevelRoutes = navItems.map { it.route }
    val showBottomBar  = topLevelRoutes.any { currentRoute?.startsWith(it) == true }

    // Seed demo data once when the DB is empty
    val appViewModel: ApplicationViewModel = hiltViewModel()
    val stats by appViewModel.stats.collectAsState()
    LaunchedEffect(stats) {
        if (stats != null && stats!!.total == 0) {
            appViewModel.seedDemoData()
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        val selected = currentRoute?.startsWith(item.route) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(item.icon, item.label) },
                            label = {
                                Text(
                                    item.label,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Only apply bottom padding to avoid double-padding at the top
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            AppNavGraph(navController = navController)
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon:  ImageVector
)

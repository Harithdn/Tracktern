package com.internshiptracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.internshiptracker.ui.screens.add_edit.AddEditScreen
import com.internshiptracker.ui.screens.analytics.AnalyticsScreen
import com.internshiptracker.ui.screens.applications.ApplicationsScreen
import com.internshiptracker.ui.screens.dashboard.DashboardScreen
import com.internshiptracker.ui.screens.kanban.KanbanScreen
import com.internshiptracker.ui.screens.resume.ResumeMatchScreen
import com.internshiptracker.ui.screens.search.SearchScreen

/**
 * Sealed class defining all navigation destinations.
 * Using sealed class gives type-safety and prevents typos in route strings.
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Applications : Screen("applications")
    object Kanban : Screen("kanban")
    object Search : Screen("search")
    object Analytics : Screen("analytics")
    object ResumeMatch : Screen("resume_match/{applicationId}") {
        fun createRoute(applicationId: Long) = "resume_match/$applicationId"
    }
    object AddEdit : Screen("add_edit?applicationId={applicationId}") {
        fun createRoute(applicationId: Long? = null) =
            if (applicationId != null) "add_edit?applicationId=$applicationId"
            else "add_edit"
    }
}

/**
 * Central NavHost — all screens are registered here.
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onAddClick = { navController.navigate(Screen.AddEdit.createRoute()) },
                onApplicationClick = { id ->
                    navController.navigate(Screen.AddEdit.createRoute(id))
                },
                onViewAllClick = { navController.navigate(Screen.Applications.route) }
            )
        }

        composable(Screen.Applications.route) {
            ApplicationsScreen(
                onAddClick = { navController.navigate(Screen.AddEdit.createRoute()) },
                onApplicationClick = { id ->
                    navController.navigate(Screen.AddEdit.createRoute(id))
                }
            )
        }

        composable(Screen.Kanban.route) {
            KanbanScreen(
                onApplicationClick = { id ->
                    navController.navigate(Screen.AddEdit.createRoute(id))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onApplicationClick = { id ->
                    navController.navigate(Screen.AddEdit.createRoute(id))
                }
            )
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen()
        }

        composable(
            route = Screen.AddEdit.route,
            arguments = listOf(
                navArgument("applicationId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getLong("applicationId") ?: -1L
            AddEditScreen(
                applicationId = if (applicationId == -1L) null else applicationId,
                onNavigateBack = { navController.popBackStack() },
                onResumeMatch = { id ->
                    navController.navigate(Screen.ResumeMatch.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.ResumeMatch.route,
            arguments = listOf(
                navArgument("applicationId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getLong("applicationId") ?: return@composable
            ResumeMatchScreen(
                applicationId = applicationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

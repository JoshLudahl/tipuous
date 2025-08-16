package com.tips.tipuous.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tips.tipuous.ui.main.MainScreen
import com.tips.tipuous.ui.receipts.AddReceiptScreen
import com.tips.tipuous.ui.receipts.ReceiptsListScreen
import com.tips.tipuous.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface Navigation {
    @Serializable
    object Main : Navigation

    @Serializable
    object AddReceipt : Navigation

    @Serializable
    object Receipts : Navigation

    @Serializable
    object Settings : Navigation
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Navigation.Main) {
        composable<Navigation.Main> {
            MainScreen(
                mainViewModel = viewModel(),
                onAddReceipt = { navController.navigate(Navigation.AddReceipt) },
                onViewReceipts = { navController.navigate(Navigation.Receipts) },
                onNavigateToSettings = { navController.navigate(Navigation.Settings) },
            )
        }

        composable<Navigation.AddReceipt> { AddReceiptScreen(navController) }
        composable<Navigation.Receipts> { ReceiptsListScreen(navController) }
        composable<Navigation.Settings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}

package com.tips.tipuous.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tips.tipuous.ui.main.MainScreen
import com.tips.tipuous.ui.receipts.AddReceiptScreen
import com.tips.tipuous.ui.receipts.ReceiptsListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(mainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(), onAddReceipt = { navController.navigate("addReceipt") }, onViewReceipts = { navController.navigate("receipts") })
        }
        composable("addReceipt") { AddReceiptScreen(navController) }
        composable("receipts") { ReceiptsListScreen(navController) }
    }
}

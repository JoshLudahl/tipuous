package com.tips.tipuous.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.tips.tipuous.ui.main.MainScreen
import com.tips.tipuous.ui.receipts.AddReceiptScreen
import com.tips.tipuous.ui.receipts.ReceiptsListScreen
import com.tips.tipuous.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface Navigation : NavKey {
    @Serializable
    object Main : Navigation

    @Serializable
    data class AddReceipt(
        val receiptId: String? = null,
        val bill: String? = null,
        val tax: String? = null,
        val tip: String? = null,
        val total: String? = null,
        val advancedSplitJson: String? = null,
        val splitCount: Int = 1,
    ) : Navigation

    @Serializable
    object Receipts : Navigation

    @Serializable
    object Settings : Navigation

    @Serializable
    object TippingGuide : Navigation
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navigationState = rememberNavigationState(
        startRoute = Navigation.Main,
        topLevelRoutes = setOf(Navigation.Main, Navigation.Receipts, Navigation.Settings, Navigation.TippingGuide)
    )
    val navigator = remember { Navigator(navigationState) }

    val entryProvider = entryProvider<NavKey> {
        entry<Navigation.Main> {
            MainScreen(
                mainViewModel = viewModel(),
                onAddReceipt = { navigator.navigate(Navigation.AddReceipt()) },
                onViewReceipts = { navigator.navigate(Navigation.Receipts) },
                onNavigateToSettings = { navigator.navigate(Navigation.Settings) },
                onNavigateToGuide = { navigator.navigate(Navigation.TippingGuide) },
                onSaveBill = { bill, tax, tip, total, splitCount, advancedJson ->
                    navigator.navigate(
                        Navigation.AddReceipt(
                            bill = bill,
                            tax = tax,
                            tip = tip,
                            total = total,
                            splitCount = splitCount,
                            advancedSplitJson = advancedJson
                        )
                    )
                },
            )
        }

        entry<Navigation.AddReceipt> { key ->
            AddReceiptScreen(
                navigator = navigator,
                receiptId = key.receiptId,
                bill = key.bill,
                tax = key.tax,
                tip = key.tip,
                total = key.total,
                splitCount = key.splitCount,
                advancedSplitJson = key.advancedSplitJson
            )
        }

        entry<Navigation.Receipts> {
            ReceiptsListScreen(navigator = navigator)
        }

        entry<Navigation.Settings> {
            SettingsScreen(
                onBack = { navigator.goBack() },
            )
        }

        entry<Navigation.TippingGuide> {
            com.tips.tipuous.ui.guide.TippingGuideScreen(navigator = navigator)
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() }
    )
}

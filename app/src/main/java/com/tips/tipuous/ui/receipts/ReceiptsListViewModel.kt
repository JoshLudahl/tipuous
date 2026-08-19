package com.tips.tipuous.ui.receipts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tips.tipuous.data.ReceiptRepository
import com.tips.tipuous.model.Receipt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Locale

data class MonthTrend(
    val label: String,
    val total: Double,
    val isCurrentMonth: Boolean,
)

class ReceiptsListViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ReceiptRepository(application)

    val receipts: StateFlow<List<Receipt>> =
        repo
            .getAllFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    val monthTrend: StateFlow<List<MonthTrend>> =
        receipts.map { receiptList ->
            val trends = mutableListOf<MonthTrend>()
            val cal = Calendar.getInstance()
            val currentMonth = cal.get(Calendar.MONTH)
            val currentYear = cal.get(Calendar.YEAR)

            for (i in 12 downTo 0) {
                val targetCal = Calendar.getInstance()
                targetCal.add(Calendar.MONTH, -i)
                val m = targetCal.get(Calendar.MONTH)
                val y = targetCal.get(Calendar.YEAR)

                val monthTotal =
                    receiptList.filter { r ->
                        val rCal = Calendar.getInstance().apply { timeInMillis = r.dateEpochMillis }
                        rCal.get(Calendar.MONTH) == m && rCal.get(Calendar.YEAR) == y
                    }.sumOf { it.grandTotal }

                val label = targetCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())?.take(1) ?: "?"
                trends.add(MonthTrend(label, monthTotal, m == currentMonth && y == currentYear))
            }
            trends
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun delete(receipt: Receipt) {
        repo.remove(receipt.id)
    }
}

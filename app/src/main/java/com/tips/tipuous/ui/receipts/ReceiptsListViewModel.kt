package com.tips.tipuous.ui.receipts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tips.tipuous.data.ReceiptRepository
import com.tips.tipuous.model.Receipt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ReceiptsListViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ReceiptRepository(application)

    val receipts: StateFlow<List<Receipt>> = repo
        .getAllFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun delete(receipt: Receipt) {
        repo.remove(receipt.id)
    }
}

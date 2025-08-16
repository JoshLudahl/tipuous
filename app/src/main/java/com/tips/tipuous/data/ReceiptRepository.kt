package com.tips.tipuous.data

import android.content.Context
import com.tips.tipuous.data.local.AppDatabase
import com.tips.tipuous.data.local.ReceiptEntity
import com.tips.tipuous.model.Receipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persistence using Room database. Kept synchronous via allowMainThreadQueries for minimal changes.
 */
class ReceiptRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).receiptDao()

    fun getAll(): List<Receipt> {
        return dao.getAll().map { it.toModel() }
    }

    fun getAllFlow(): Flow<List<Receipt>> = dao.getAllFlow().map { list -> list.map { it.toModel() } }

    fun add(receipt: Receipt) {
        dao.insert(receipt.toEntity())
    }

    fun remove(id: String) {
        dao.deleteById(id)
    }
}

private fun ReceiptEntity.toModel(): Receipt =
    Receipt(
        id = id,
        dateEpochMillis = dateEpochMillis,
        billTotal = billTotal,
        tipAmount = tipAmount,
        grandTotal = grandTotal,
        locationName = locationName,
        imagePath = imagePath,
    )

private fun Receipt.toEntity(): ReceiptEntity =
    ReceiptEntity(
        id = id,
        dateEpochMillis = dateEpochMillis,
        billTotal = billTotal,
        tipAmount = tipAmount,
        grandTotal = grandTotal,
        locationName = locationName,
        imagePath = imagePath,
    )

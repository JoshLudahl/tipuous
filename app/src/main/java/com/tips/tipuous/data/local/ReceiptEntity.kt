package com.tips.tipuous.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import com.tips.tipuous.model.AdvancedSplit

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "date_epoch_millis") val dateEpochMillis: Long,
    @ColumnInfo(name = "bill_total") val billTotal: Double,
    @ColumnInfo(name = "tax_amount") val taxAmount: Double = 0.0,
    @ColumnInfo(name = "tip_amount") val tipAmount: Double,
    @ColumnInfo(name = "grand_total") val grandTotal: Double,
    @ColumnInfo(name = "location_name") val locationName: String?,
    @ColumnInfo(name = "image_path") val imagePath: String?,
    @ColumnInfo(name = "advanced_split_json") val advancedSplit: AdvancedSplit? = null,
    @ColumnInfo(name = "split_count") val splitCount: Int = 1,
)

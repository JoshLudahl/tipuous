package com.tips.tipuous.model

import android.net.Uri
import java.util.UUID

/**
 * Minimal persisted model for a scanned or manually entered receipt.
 * Image is stored as a file on internal storage; we persist its absolute path as a String.
 */
data class Receipt(
    val id: String = UUID.randomUUID().toString(),
    val dateEpochMillis: Long,
    val billTotal: Double,
    val tipAmount: Double,
    val grandTotal: Double,
    val locationName: String?,
    val imagePath: String? // absolute path to the saved image in app storage
)

package com.tips.tipuous.utilities

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility to run on-device OCR and parse basic receipt fields.
 */
object ReceiptOcr {

    data class Parsed(
        val billTotal: Double?,
        val tipAmount: Double?,
        val grandTotal: Double?,
        val dateEpochMillis: Long?,
        val location: String?
    )

    suspend fun parseFromBitmap(context: Context, bitmap: Bitmap): Parsed {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = recognizer.process(image).await()
        val fullText = result.text.lowercase(Locale.getDefault())

        // Extract amounts
        val amounts = Regex("\\$?\\s*([0-9]+(?:\\.[0-9]{1,2})?)").findAll(fullText)
            .mapNotNull { it.groupValues.getOrNull(1)?.toDoubleOrNull() }
            .toList()

        val tip = Regex("tip\\s*[:=]?\\s*\\$?([0-9]+(?:\\.[0-9]{1,2})?)").find(fullText)
            ?.groupValues?.getOrNull(1)?.toDoubleOrNull()

        val grandCandidates = listOf(
            Regex("grand\\s*total\\s*[:=]?\\s*\\$?([0-9]+(?:\\.[0-9]{1,2})?)"),
            Regex("total\\s*[:=]?\\s*\\$?([0-9]+(?:\\.[0-9]{1,2})?)")
        )
        val grand = grandCandidates.asSequence()
            .mapNotNull { it.find(fullText)?.groupValues?.getOrNull(1)?.toDoubleOrNull() }
            .firstOrNull() ?: amounts.maxOrNull()

        val bill = if (grand != null && tip != null) {
            (grand - tip).let { kotlin.math.abs(it) }
        } else {
            // Try find subtotal
            Regex("sub\\s*total\\s*[:=]?\\s*\\$?([0-9]+(?:\\.[0-9]{1,2})?)").find(fullText)
                ?.groupValues?.getOrNull(1)?.toDoubleOrNull()
        }

        // Extract a date (very simple heuristics)
        val dateRegexes = listOf(
            Regex("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})"), // 08/15/2025 or 8-5-25
            Regex("(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})") // 2025-08-15
        )
        val dateMillis = dateRegexes.firstNotNullOfOrNull { rx ->
            rx.find(fullText)?.groupValues?.let { groups ->
                try {
                    // Try multiple formats
                    val formats = listOf("MM/dd/yyyy", "M/d/yy", "M-d-yy", "yyyy-MM-dd", "yyyy/M/d")
                    formats.firstNotNullOfOrNull { fmt ->
                        try {
                            SimpleDateFormat(fmt, Locale.getDefault()).parse(groups[0])?.time
                        } catch (_: Exception) { null }
                    }
                } catch (_: Exception) { null }
            }
        }

        // Heuristic location: first line without amounts, near top
        val lines = fullText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val location = lines.firstOrNull { line ->
            line.length in 3..40 && !line.contains("total") && !line.contains("tip") && !Regex("[0-9]+\\.[0-9]{1,2}").containsMatchIn(line)
        }

        return Parsed(
            billTotal = bill,
            tipAmount = tip,
            grandTotal = grand,
            dateEpochMillis = dateMillis,
            location = location?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        )
    }
}

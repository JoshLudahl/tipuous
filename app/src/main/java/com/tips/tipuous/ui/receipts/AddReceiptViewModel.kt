package com.tips.tipuous.ui.receipts

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tips.tipuous.data.ReceiptRepository
import com.tips.tipuous.model.Receipt
import com.tips.tipuous.utilities.ReceiptOcr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * ViewModel for AddReceiptScreen. Holds form state, image preview, parsing, validation and saving.
 */
class AddReceiptViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ReceiptRepository(application)

    data class UiState(
        val bill: String = "",
        val tip: String = "",
        val total: String = "",
        val dateMillis: Long? = System.currentTimeMillis(),
        val location: String = "",
        val previewBitmap: Bitmap? = null,
        val showDatePicker: Boolean = false,
        val isFormValid: Boolean = false,
        val saved: Boolean = false,
        val errorMessage: String? = null,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    // ------- Field updates -------
    fun onBillChange(input: String) =
        _state.update {
            val filtered = input.filter { ch -> ch.isDigit() || ch == '.' }
            it.copy(bill = filtered).recomputeValidity()
        }

    fun onTipChange(input: String) =
        _state.update {
            val filtered = input.filter { ch -> ch.isDigit() || ch == '.' }
            it.copy(tip = filtered).recomputeValidity()
        }

    fun onTotalChange(input: String) =
        _state.update {
            val filtered = input.filter { ch -> ch.isDigit() || ch == '.' }
            it.copy(total = filtered).recomputeValidity()
        }

    fun onLocationChange(input: String) = _state.update { it.copy(location = input) }

    fun setShowDatePicker(show: Boolean) = _state.update { it.copy(showDatePicker = show) }

    fun setDate(millis: Long?) = _state.update { it.copy(dateMillis = millis) }

    // ------- Image handling / OCR -------
    fun handleCaptureBitmap(bitmap: Bitmap) {
        _state.update { it.copy(previewBitmap = bitmap) }
        parseReceiptFromBitmap(bitmap)
    }

    fun handlePickedImage(uri: Uri?) {
        if (uri == null) return
        val bmp = decodeBitmapFromUri(uri)
        if (bmp != null) {
            _state.update { it.copy(previewBitmap = bmp) }
            parseReceiptFromBitmap(bmp)
        }
    }

    private fun parseReceiptFromBitmap(bmp: Bitmap) {
        val context = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            val parsed = ReceiptOcr.parseFromBitmap(context, bmp)
            _state.update { current ->
                current.copy(
                    bill = parsed.billTotal?.toString() ?: current.bill,
                    tip = parsed.tipAmount?.toString() ?: current.tip,
                    total = parsed.grandTotal?.toString() ?: current.total,
                    location = parsed.location ?: current.location,
                    dateMillis = parsed.dateEpochMillis ?: current.dateMillis,
                ).recomputeValidity()
            }
        }
    }

    // ------- Saving -------
    fun saveReceipt() {
        val snapshot = _state.value
        if (!snapshot.isFormValid) return

        val billD = snapshot.bill.toDoubleOrNull() ?: 0.0
        val tipD = snapshot.tip.toDoubleOrNull() ?: 0.0
        val totalD = snapshot.total.toDoubleOrNull() ?: 0.0
        val millis = snapshot.dateMillis ?: System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.IO) {
            val imagePath = snapshot.previewBitmap?.let { saveBitmapToInternal(it) }
            val receipt =
                Receipt(
                    dateEpochMillis = millis,
                    billTotal = billD,
                    tipAmount = tipD,
                    grandTotal = totalD,
                    locationName = snapshot.location.ifBlank { null },
                    imagePath = imagePath,
                )
            try {
                repo.add(receipt)
                _state.update { it.copy(saved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    // ------- Helpers -------
    private fun decodeBitmapFromUri(
        uri: Uri,
        maxSize: Int = 2048,
    ): Bitmap? {
        val context = getApplication<Application>()
        return try {
            if (Build.VERSION.SDK_INT >= 28) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                val original =
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                val w = original.width
                val h = original.height
                val maxDim = maxOf(w, h)
                if (maxDim > maxSize) {
                    val ratio = maxDim.toFloat() / maxSize
                    val newW = (w / ratio).toInt().coerceAtLeast(1)
                    val newH = (h / ratio).toInt().coerceAtLeast(1)
                    Bitmap.createScaledBitmap(original, newW, newH, true)
                } else {
                    original
                }
            } else {
                // Two-pass decode with inSampleSize
                context.contentResolver.openInputStream(uri)?.use { stream1 ->
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(stream1, null, bounds)
                }
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream2 ->
                    BitmapFactory.decodeStream(stream2, null, bounds)
                }
                val outW = bounds.outWidth
                val outH = bounds.outHeight
                val maxDim = maxOf(outW, outH).coerceAtLeast(1)
                val sample =
                    if (maxDim > maxSize) {
                        var s = 1
                        while (maxDim / s > maxSize) s *= 2
                        s
                    } else {
                        1
                    }
                val opts =
                    BitmapFactory.Options().apply {
                        inSampleSize = sample
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                context.contentResolver.openInputStream(uri)?.use { stream3 ->
                    BitmapFactory.decodeStream(stream3, null, opts)
                }
            }
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun saveBitmapToInternal(bitmap: Bitmap): String? {
        return try {
            val context = getApplication<Application>()
            val dir = File(context.filesDir, "receipts")
            if (!dir.exists()) dir.mkdirs()
            val filename = "receipt_${System.currentTimeMillis()}.png"
            val file = File(dir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    // Utility for UI date text if needed by previews
    fun formattedDate(): String {
        val millis = _state.value.dateMillis ?: System.currentTimeMillis()
        val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date(millis))
    }
}

private fun AddReceiptViewModel.UiState.recomputeValidity(): AddReceiptViewModel.UiState {
    val billD = bill.toDoubleOrNull()
    val tipD = tip.toDoubleOrNull()
    val totalD = total.toDoubleOrNull()
    val valid =
        !bill.isBlank() && !tip.isBlank() && !total.isBlank() &&
            billD != null && tipD != null && totalD != null &&
            billD > 0.0 && tipD >= 0.0 && totalD > 0.0
    return copy(isFormValid = valid)
}

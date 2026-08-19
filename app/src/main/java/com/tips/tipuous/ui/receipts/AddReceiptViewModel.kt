package com.tips.tipuous.ui.receipts

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.graphics.scale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tips.tipuous.data.ReceiptRepository
import com.tips.tipuous.domain.TipCalculator
import com.tips.tipuous.model.AdvancedSplit
import com.tips.tipuous.model.Item
import com.tips.tipuous.model.Percent
import com.tips.tipuous.model.Person
import com.tips.tipuous.model.Receipt
import com.tips.tipuous.model.RoundingMode
import com.tips.tipuous.model.TipMode
import com.tips.tipuous.utilities.ReceiptOcr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for AddReceiptScreen. Holds form state, image preview, parsing, validation and saving.
 */
class AddReceiptViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ReceiptRepository(application)
    private val tipCalculator = TipCalculator()
    private var editingId: String? = null

    data class UiState(
        val bill: String = "",
        val tax: String = "",
        val tip: String = "",
        val total: String = "",
        val dateMillis: Long? = System.currentTimeMillis(),
        val location: String = "",
        val previewBitmap: Bitmap? = null,
        val showDatePicker: Boolean = false,
        val isFormValid: Boolean = false,
        val saved: Boolean = false,
        val errorMessage: String? = null,
        val advancedSplit: AdvancedSplit? = null,
        val splitCount: Int = 1,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    // ------- Field updates -------
    fun onBillChange(input: String) =
        _state.update {
            val filtered = input.filter { ch -> ch.isDigit() || ch == '.' }
            it.copy(bill = filtered).recomputeValidity()
        }

    fun onTaxChange(input: String) =
        _state.update {
            val filtered = input.filter { ch -> ch.isDigit() || ch == '.' }
            it.copy(tax = filtered).recomputeValidity()
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

    fun addPerson(name: String) {
        val current = _state.value.advancedSplit ?: AdvancedSplit()
        val newPeople = current.people + Person(name = name)
        _state.update {
            val newSplitCount = if (it.splitCount < newPeople.size) newPeople.size else it.splitCount
            it.copy(
                advancedSplit = current.copy(people = newPeople),
                splitCount = newSplitCount,
            ).recomputeValidity()
        }
    }

    fun removePerson(personId: String) {
        val current = _state.value.advancedSplit ?: return
        val newPeople = current.people.filter { it.id != personId }
        _state.update { it.copy(advancedSplit = current.copy(people = newPeople)).recomputeValidity() }
    }

    fun addItemToPerson(
        personId: String,
        itemName: String,
        amount: Double,
    ) {
        val current = _state.value.advancedSplit ?: return
        val newPeople =
            current.people.map { person ->
                if (person.id == personId) {
                    person.copy(items = person.items + Item(name = itemName, amount = amount))
                } else {
                    person
                }
            }
        _state.update { it.copy(advancedSplit = current.copy(people = newPeople)).recomputeValidity() }
    }

    fun removeItemFromPerson(
        personId: String,
        itemId: String,
    ) {
        val current = _state.value.advancedSplit ?: return
        val newPeople =
            current.people.map { person ->
                if (person.id == personId) {
                    person.copy(items = person.items.filter { it.id != itemId })
                } else {
                    person
                }
            }
        _state.update { it.copy(advancedSplit = current.copy(people = newPeople)).recomputeValidity() }
    }

    fun prefillData(
        bill: String?,
        tax: String?,
        tip: String?,
        total: String?,
        splitCount: Int = 1,
        advancedSplitJson: String? = null,
    ) {
        _state.update {
            val filteredBill = bill?.filter { ch -> ch.isDigit() || ch == '.' } ?: it.bill
            val filteredTax = tax?.filter { ch -> ch.isDigit() || ch == '.' } ?: it.tax
            val filteredTip = tip?.filter { ch -> ch.isDigit() || ch == '.' } ?: it.tip
            val filteredTotal = total?.filter { ch -> ch.isDigit() || ch == '.' } ?: it.total
            val advanced =
                try {
                    advancedSplitJson?.let { json -> Json.decodeFromString<AdvancedSplit>(json) }
                } catch (e: Exception) {
                    null
                }
            it.copy(
                bill = filteredBill,
                tax = filteredTax,
                tip = filteredTip,
                total = filteredTotal,
                advancedSplit = advanced ?: it.advancedSplit,
                splitCount = if (splitCount > 0) splitCount else it.splitCount,
            ).recomputeValidity()
        }
    }

    fun setShowDatePicker(show: Boolean) = _state.update { it.copy(showDatePicker = show) }

    fun setDate(millis: Long?) = _state.update { it.copy(dateMillis = millis) }

    // ------- Image handling / OCR -------
    fun loadForEdit(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val rec = repo.getById(id)
            if (rec != null) {
                editingId = rec.id
                val bmp =
                    try {
                        rec.imagePath?.let { path ->
                            val file = File(path)
                            if (file.exists()) BitmapFactory.decodeFile(path) else null
                        }
                    } catch (_: Exception) {
                        null
                    }
                _state.update {
                    it.copy(
                        bill = rec.billTotal.toString(),
                        tax = rec.taxAmount.toString(),
                        tip = rec.tipAmount.toString(),
                        total = rec.grandTotal.toString(),
                        dateMillis = rec.dateEpochMillis,
                        location = rec.locationName ?: "",
                        previewBitmap = bmp ?: it.previewBitmap,
                        advancedSplit = rec.advancedSplit,
                        splitCount = rec.splitCount,
                    ).recomputeValidity()
                }
            }
        }
    }

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
                    tax = parsed.taxAmount?.toString() ?: current.tax,
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
        val taxD = snapshot.tax.toDoubleOrNull() ?: 0.0
        val tipD = snapshot.tip.toDoubleOrNull() ?: 0.0
        val totalD = snapshot.total.toDoubleOrNull() ?: 0.0
        val millis = snapshot.dateMillis ?: System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.IO) {
            val imagePath = snapshot.previewBitmap?.let { saveBitmapToInternal(it) }
            val receipt =
                Receipt(
                    id = editingId ?: java.util.UUID.randomUUID().toString(),
                    dateEpochMillis = millis,
                    billTotal = billD,
                    taxAmount = taxD,
                    tipAmount = tipD,
                    grandTotal = totalD,
                    locationName = snapshot.location.ifBlank { null },
                    imagePath = imagePath,
                    advancedSplit = snapshot.advancedSplit,
                    splitCount = snapshot.splitCount,
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
                    original.scale(newW, newH)
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

    data class PersonTotal(
        val personId: String,
        val total: Double,
        val isShared: Boolean = false,
    )

    fun getPersonTotalsList(): List<PersonTotal> {
        val snapshot = _state.value
        val billD = snapshot.bill.toDoubleOrNull() ?: 0.0
        val taxD = snapshot.tax.toDoubleOrNull() ?: 0.0
        val tipD = snapshot.tip.toDoubleOrNull() ?: 0.0
        val advanced = snapshot.advancedSplit ?: return emptyList()

        val result =
            tipCalculator.calculate(
                billAmount = billD,
                tipPercentEnum = Percent.NONE,
                customTipPercent = 0,
                splitCount = snapshot.splitCount,
                taxAmount = taxD,
                calculateTipOnPreTax = false,
                roundingMode = RoundingMode.NONE,
                tipMode = TipMode.AMOUNT,
                fixedTipAmount = tipD,
                advancedSplit = advanced,
            )

        val list = mutableListOf<PersonTotal>()
        advanced.people.forEach { person ->
            list.add(PersonTotal(person.id, result.personResults[person.id] ?: 0.0))
        }

        val othersCount = snapshot.splitCount - advanced.people.size
        if (othersCount > 0) {
            list.add(PersonTotal("others", result.amountPerPerson, isShared = true))
        }

        return list
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

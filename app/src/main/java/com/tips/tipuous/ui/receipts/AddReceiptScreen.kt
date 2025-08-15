package com.tips.tipuous.ui.receipts

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.tips.tipuous.data.ReceiptRepository
import com.tips.tipuous.model.Receipt
import com.tips.tipuous.utilities.ReceiptOcr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AddReceiptScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = remember { ReceiptRepository(context) }

    var bill by remember { mutableStateOf("") }
    var tip by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var location by remember { mutableStateOf("") }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            previewBitmap = bmp
            CoroutineScope(Dispatchers.IO).launch {
                val parsed = ReceiptOcr.parseFromBitmap(context, bmp)
                launch(Dispatchers.Main) {
                    parsed.billTotal?.let { bill = it.toString() }
                    parsed.tipAmount?.let { tip = it.toString() }
                    parsed.grandTotal?.let { total = it.toString() }
                    parsed.location?.let { location = it }
                    parsed.dateEpochMillis?.let { dateMillis = it }
                }
            }
        }
    }

    // Request CAMERA permission and proceed to capture when granted
    var pendingCameraAction by remember { mutableStateOf(false) }
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            // Permission denied; reset any pending action
            pendingCameraAction = false
        }
    }

    // Safely decode a bitmap from a content Uri, downsampling large images to avoid OOM
    fun decodeBitmapFromUri(uri: Uri, maxSize: Int = 2048): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= 28) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                val original = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
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
                } else original
            } else {
                // Two-pass decode with inSampleSize
                context.contentResolver.openInputStream(uri)?.use { stream1 ->
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(stream1, null, bounds)
                }
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                // Need dimensions; reopen stream
                context.contentResolver.openInputStream(uri)?.use { stream2 ->
                    BitmapFactory.decodeStream(stream2, null, bounds)
                }
                val outW = bounds.outWidth
                val outH = bounds.outHeight
                val maxDim = maxOf(outW, outH).coerceAtLeast(1)
                val sample = if (maxDim > maxSize) {
                    var s = 1
                    while (maxDim / s > maxSize) s *= 2
                    s
                } else 1
                val opts = BitmapFactory.Options().apply {
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

    fun handlePickedImage(uri: Uri?) {
        if (uri != null) {
            val bmp = decodeBitmapFromUri(uri)
            if (bmp != null) {
                previewBitmap = bmp
                CoroutineScope(Dispatchers.IO).launch {
                    val parsed = ReceiptOcr.parseFromBitmap(context, bmp)
                    launch(Dispatchers.Main) {
                        parsed.billTotal?.let { bill = it.toString() }
                        parsed.tipAmount?.let { tip = it.toString() }
                        parsed.grandTotal?.let { total = it.toString() }
                        parsed.location?.let { location = it }
                        parsed.dateEpochMillis?.let { dateMillis = it }
                    }
                }
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        handlePickedImage(uri)
    }

    val getContentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        handlePickedImage(uri)
    }

    fun saveBitmapToInternal(bitmap: Bitmap): String? {
        return try {
            val dir = File(context.filesDir, "receipts")
            if (!dir.exists()) dir.mkdirs()
            val filename = "receipt_${System.currentTimeMillis()}.png"
            val file = File(dir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) { null }
    }

    fun parseDoubleOrZero(s: String): Double = s.toDoubleOrNull() ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Receipt") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add Tip / Receipt", style = MaterialTheme.typography.titleLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        takePictureLauncher.launch(null)
                    } else {
                        pendingCameraAction = true
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                    Text("Capture Receipt")
                }
                Button(onClick = {
                    // Use system Photo Picker when available; otherwise fall back to SAF GetContent.
                    // Additionally, guard against OEM/runtime issues by catching exceptions and falling back.
                    if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                        try {
                            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } catch (e: Exception) {
                            // Fallback if launching the picker fails for any reason
                            getContentLauncher.launch("image/*")
                        }
                    } else {
                        getContentLauncher.launch("image/*")
                    }
                }) { Text("Pick Image") }
            }

            if (previewBitmap != null) {
                Image(bitmap = previewBitmap!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bill,
                        onValueChange = { bill = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Bill Total") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                    )
                    OutlinedTextField(
                        value = tip,
                        onValueChange = { tip = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Tip Amount") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                    )
                    OutlinedTextField(
                        value = total,
                        onValueChange = { total = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Grand Total") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
                    )

                    val dateText = remember(dateMillis) {
                        val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        fmt.format(Date(dateMillis ?: System.currentTimeMillis()))
                    }
                    Text("Date: $dateText", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = {
                    val bmp = previewBitmap
                    val path = if (bmp != null) saveBitmapToInternal(bmp) else null
                    val billD = parseDoubleOrZero(bill)
                    val tipD = parseDoubleOrZero(tip)
                    val totalD = if (total.isNotBlank()) parseDoubleOrZero(total) else billD + tipD
                    val receipt = Receipt(
                        dateEpochMillis = dateMillis ?: System.currentTimeMillis(),
                        billTotal = billD,
                        tipAmount = tipD,
                        grandTotal = totalD,
                        locationName = location.ifBlank { null },
                        imagePath = path
                    )
                    repo.add(receipt)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Save Receipt")
            }
        }
    }
}

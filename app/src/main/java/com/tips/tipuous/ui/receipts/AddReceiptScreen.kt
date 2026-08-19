package com.tips.tipuous.ui.receipts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CameraEnhance
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.tips.tipuous.model.AdvancedSplit
import com.tips.tipuous.navigation.Navigator
import com.tips.tipuous.ui.composeables.AdvancedSplitSection
import com.tips.tipuous.utilities.Conversion

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AddReceiptScreen(
    navigator: Navigator,
    receiptId: String? = null,
    bill: String? = null,
    tax: String? = null,
    tip: String? = null,
    total: String? = null,
    splitCount: Int = 1,
    advancedSplitJson: String? = null,
) {
    val context = LocalContext.current
    val viewModel: AddReceiptViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.state.collectAsState()

    val numericKeyboardOptions = remember {
        KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
    }
    val locationKeyboardOptions = remember {
        KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done)
    }
    val textFieldColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)

    // Load existing receipt if editing
    androidx.compose.runtime.LaunchedEffect(receiptId) {
        if (receiptId != null) {
            viewModel.loadForEdit(receiptId)
        }
    }

    // Prefill data if provided
    androidx.compose.runtime.LaunchedEffect(bill, tax, tip, total, splitCount, advancedSplitJson) {
        if (receiptId == null && (bill != null || tax != null || tip != null || total != null)) {
            viewModel.prefillData(bill, tax, tip, total, splitCount, advancedSplitJson)
        }
    }

    // Navigate back once saved
    androidx.compose.runtime.LaunchedEffect(state.saved) {
        if (state.saved) {
            navigator.goBack()
        }
    }

    val takePictureLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicturePreview(),
        ) { bmp ->
            if (bmp != null) {
                viewModel.handleCaptureBitmap(bmp)
            }
        }

    // Request CAMERA permission and proceed to capture when granted
    var pendingCameraAction by remember { mutableStateOf(false) }
    val requestCameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                takePictureLauncher.launch(null)
            } else {
                // Permission denied; reset any pending action
                pendingCameraAction = false
            }
        }

    val pickImageLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            viewModel.handlePickedImage(uri)
        }

    val getContentLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent(),
        ) { uri ->
            viewModel.handlePickedImage(uri)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (receiptId != null) "Edit Receipt" else "Add Receipt") },
                navigationIcon = {
                    IconButton(onClick = { navigator.goBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick = {
                        val hasPermission =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA,
                            ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            takePictureLauncher.launch(null)
                        } else {
                            pendingCameraAction = true
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.weight(.5f),
                ) {
                    Icon(Icons.Rounded.CameraEnhance, contentDescription = "Capture Receipt", modifier = Modifier.padding(end = 8.dp))
                    Text("Capture Receipt")
                }
                Button(
                    onClick = {
                        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                            try {
                                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } catch (e: Exception) {
                                getContentLauncher.launch("image/*")
                            }
                        } else {
                            getContentLauncher.launch("image/*")
                        }
                    },
                    modifier = Modifier.weight(.5f),
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = "Pick Image", modifier = Modifier.padding(end = 8.dp))
                    Text("Pick Image")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(30.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.bill,
                        onValueChange = viewModel::onBillChange,
                        shape = RoundedCornerShape(30.dp),
                        label = { Text("Bill Total") },
                        keyboardOptions = numericKeyboardOptions,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                    )
                    OutlinedTextField(
                        value = state.tax,
                        onValueChange = viewModel::onTaxChange,
                        shape = RoundedCornerShape(30.dp),
                        label = { Text("Tax Amount") },
                        keyboardOptions = numericKeyboardOptions,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                    )
                    OutlinedTextField(
                        value = state.tip,
                        onValueChange = viewModel::onTipChange,
                        shape = RoundedCornerShape(30.dp),
                        label = { Text("Tip Amount") },
                        keyboardOptions = numericKeyboardOptions,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                    )
                    OutlinedTextField(
                        value = state.total,
                        onValueChange = viewModel::onTotalChange,
                        shape = RoundedCornerShape(30.dp),
                        label = { Text("Grand Total") },
                        keyboardOptions = numericKeyboardOptions,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                    )
                    OutlinedTextField(
                        value = state.location,
                        onValueChange = viewModel::onLocationChange,
                        shape = RoundedCornerShape(30.dp),
                        label = { Text("Location (optional)") },
                        keyboardOptions = locationKeyboardOptions,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                    )

                    val dateText =
                        remember(state.dateMillis) {
                            val fmt = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                            fmt.timeZone = java.util.TimeZone.getTimeZone("UTC")
                            fmt.format(java.util.Date(state.dateMillis ?: System.currentTimeMillis()))
                        }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Date: $dateText", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { viewModel.setShowDatePicker(true) }) { Text("Select Date") }
                    }

                    if (state.showDatePicker) {
                        val dpState =
                            rememberDatePickerState(
                                initialSelectedDateMillis = state.dateMillis ?: System.currentTimeMillis(),
                            )
                        DatePickerDialog(
                            onDismissRequest = { viewModel.setShowDatePicker(false) },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.setDate(dpState.selectedDateMillis ?: state.dateMillis)
                                    viewModel.setShowDatePicker(false)
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(onClick = { viewModel.setShowDatePicker(false) }) { Text("Cancel") }
                            },
                        ) {
                            DatePicker(state = dpState)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(30.dp),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Advanced Split",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    val personTotalsList = viewModel.getPersonTotalsList()
                    val personResultsMap = personTotalsList.filter { !it.isShared }.associate { it.personId to it.total }

                    AdvancedSplitSection(
                        advancedSplit = state.advancedSplit ?: AdvancedSplit(),
                        personResults = personResultsMap,
                        onAddPerson = { name -> viewModel.addPerson(name) },
                        onRemovePerson = { id -> viewModel.removePerson(id) },
                        onAddItem = { pid, name, amount -> viewModel.addItemToPerson(pid, name, amount) },
                        onRemoveItem = { pid, iid -> viewModel.removeItemFromPerson(pid, iid) }
                    )

                    val sharedItems = personTotalsList.filter { it.isShared }
                    if (sharedItems.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        sharedItems.forEach { shared ->
                            val othersCount = state.splitCount - state.advancedSplit!!.people.size
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Others ($othersCount ${if (othersCount > 1) "people" else "person"})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$${Conversion.formatNumberToIncludeTrailingZero(shared.total)}/each",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.saveReceipt()
                },
                enabled = state.isFormValid,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
            ) {
                Text("Save Receipt")
            }

            if (state.previewBitmap != null) {
                Image(
                    bitmap = state.previewBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                )
            }
        }
    }
}

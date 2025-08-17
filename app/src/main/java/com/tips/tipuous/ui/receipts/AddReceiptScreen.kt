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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AddReceiptScreen(navController: NavController, receiptId: String? = null) {
    val context = LocalContext.current
    val viewModel: AddReceiptViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.state.collectAsState()

        // Load existing receipt if editing
        androidx.compose.runtime.LaunchedEffect(receiptId) {
            if (receiptId != null) {
                viewModel.loadForEdit(receiptId)
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
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp),
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

            if (state.previewBitmap != null) {
                Image(
                    bitmap = state.previewBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.bill,
                        onValueChange = viewModel::onBillChange,
                        label = { Text("Bill Total") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary),
                    )
                    OutlinedTextField(
                        value = state.tip,
                        onValueChange = viewModel::onTipChange,
                        label = { Text("Tip Amount") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary),
                    )
                    OutlinedTextField(
                        value = state.total,
                        onValueChange = viewModel::onTotalChange,
                        label = { Text("Grand Total") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary),
                    )
                    OutlinedTextField(
                        value = state.location,
                        onValueChange = viewModel::onLocationChange,
                        label = { Text("Location (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary),
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

            // Navigate back once saved
            if (state.saved) {
                navController.popBackStack()
            }
        }
    }
}

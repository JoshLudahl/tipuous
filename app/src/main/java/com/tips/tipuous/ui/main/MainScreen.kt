package com.tips.tipuous.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel = viewModel()) {
    // Observe StateFlows from ViewModel
    val billAmount by mainViewModel.bill.collectAsStateWithLifecycle()

    // Local state for the TextField to manage text input directly
    var billText by remember(billAmount) {
        mutableStateOf(if (billAmount == 0.0 || billAmount.toString() == "0.0") "" else billAmount.toString())
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tipuous") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bill",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = billText,
                onValueChange = { newText ->
                    // Allow empty, or numbers with optional decimal point
                    if (newText.isEmpty() || newText.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        billText = newText
                        val newBill = newText.toDoubleOrNull() ?: 0.0
                        if (billAmount != newBill) {
                           mainViewModel.setBill(newBill)
                           // Trigger calculation when bill amount changes and is valid
                           mainViewModel.calculateTip()
                        } else if (newText.isEmpty() && billAmount != 0.0) {
                            // Handle case where field is cleared
                            mainViewModel.setBill(0.0)
                            mainViewModel.calculateTip()
                        }
                    }
                },
                label = { Text("Enter Bill Amount") },
                leadingIcon = { Icon(Icons.Filled.AttachMoney, contentDescription = "Bill Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Placeholder for Tip Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tip Section - Placeholder", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Placeholder for Split Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Split Section - Placeholder", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Placeholder for Total Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Section - Placeholder", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

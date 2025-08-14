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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tips.tipuous.model.Percent // Import Percent
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel = viewModel()) {
    // Observe StateFlows from ViewModel
    val billAmount by mainViewModel.bill.collectAsStateWithLifecycle()
    val selectedTipType by mainViewModel.tip.collectAsStateWithLifecycle()
    val customTipPercentValue by mainViewModel.customTip.collectAsStateWithLifecycle()
    val splitCount by mainViewModel.split.collectAsStateWithLifecycle()
    val splitValue by mainViewModel.splitValue.collectAsStateWithLifecycle()


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
                    if (newText.isEmpty() || newText.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        billText = newText
                        val newBill = newText.toDoubleOrNull() ?: 0.0
                        if (billAmount != newBill) {
                           mainViewModel.setBill(newBill)
                           mainViewModel.calculateTip()
                        } else if (newText.isEmpty() && billAmount != 0.0) {
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

            // Tip Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tip Percentage",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Assuming Percent.FIVE, Percent.TEN, Percent.FIFTEEN exist
                        val tipOptions = listOf(
                            "5%" to Percent.FIVE, 
                            "10%" to Percent.TEN, 
                            "15%" to Percent.FIFTEEN
                        )

                        tipOptions.forEach { (label, percentEnum) ->
                            AssistChip(
                                onClick = { mainViewModel.updateTipPercentage(percentEnum) },
                                label = { Text(label) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (selectedTipType == percentEnum && selectedTipType != Percent.CUSTOM) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }

                        AssistChip(
                            onClick = { mainViewModel.handleCustomPercentageClick() },
                            label = { Text("Other") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selectedTipType == Percent.CUSTOM) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    if (selectedTipType == Percent.CUSTOM) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Custom Tip: ${customTipPercentValue}%", fontSize = 16.sp)
                        Slider(
                            value = customTipPercentValue.toFloat(),
                            onValueChange = { newValue ->
                                mainViewModel.updateCustomValue(newValue.toInt())
                                // No need to call calculateTip here if updateCustomValue triggers it or if onValueChangeFinished is used
                            },
                            onValueChangeFinished = {
                                mainViewModel.calculateTip() // Calculate tip when slider interaction finishes
                            },
                            valueRange = 1f..50f, // e.g., 1% to 50%
                            steps = (50 - 1) - 1, // (End - Start) - 1 for discrete steps
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }


            // Split Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Split Bill",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Number of people: ${splitCount.roundToInt()}", fontSize = 16.sp)
                    Slider(
                        value = splitCount,
                        onValueChange = { newValue ->
                            mainViewModel.updateSplit(newValue)
                        },
                        onValueChangeFinished = {
                            mainViewModel.calculateTip() // Recalculate everything including split value
                        },
                        valueRange = 1f..75f,
                        steps = (75 - 1) - 1, // For discrete steps from 1 to 75
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (splitCount > 1f) {
                        Text(
                            text = "Amount per person: $${splitValue}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
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

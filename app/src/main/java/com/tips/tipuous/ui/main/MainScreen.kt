package com.tips.tipuous.ui.main

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tips.tipuous.model.Percent
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
    val tipAmountValue by mainViewModel.tipValue.collectAsStateWithLifecycle() // Observe tipValue (FIXED)
    val totalAmountValue by mainViewModel.total.collectAsStateWithLifecycle() // Observe totalAmount

    // Local state for the TextField to manage text input directly
    var billText by remember(billAmount) {
        mutableStateOf(if (billAmount == 0.0 || billAmount.toString() == "0.0") "" else billAmount.toString())
    }

    val context = LocalContext.current

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle("Bill Amount")
            OutlinedTextField(
                value = billText,
                shape = RoundedCornerShape(16.dp),
                onValueChange = { newText ->
                    // Regex updated to use raw string and match standard decimal input
                    if (newText.isEmpty() || newText.matches(Regex("""^\d*\.?\d*$"""))) {
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
                leadingIcon = {
                    Icon(
                        Icons.Filled.AttachMoney, contentDescription = "Bill Amount"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary
                )
            )

            SectionTitle("Tip Percentage")

            // Tip Section
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val tipOptions = listOf(
                            "5%" to Percent.FIVE, "10%" to Percent.TEN, "15%" to Percent.FIFTEEN
                        )

                        tipOptions.forEach { (label, percentEnum) ->
                            AssistChip(
                                onClick = { mainViewModel.updateTipPercentage(percentEnum) },
                                label = { Text(label) },
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = if (selectedTipType == percentEnum && selectedTipType != Percent.CUSTOM) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface,
                                    containerColor = if (selectedTipType == percentEnum && selectedTipType != Percent.CUSTOM) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.24f
                                    ),
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = false
                                )
                            )
                        }

                        AssistChip(
                            onClick = { mainViewModel.handleCustomPercentageClick() },
                            label = { Text("Other") },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = if (selectedTipType == Percent.CUSTOM) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface,
                                containerColor = if (selectedTipType == Percent.CUSTOM) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface.copy(
                                    alpha = 0.24f
                                ),
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = false
                            )
                        )
                    }

                    if (selectedTipType == Percent.CUSTOM) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 8.dp,
                                    bottom = 8.dp
                                ), // Added bottom padding to the Row
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Slider(
                                value = customTipPercentValue.toFloat(),
                                onValueChange = { newValue ->
                                    mainViewModel.updateCustomValue(newValue.toInt())
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.tertiary,
                                    activeTrackColor = MaterialTheme.colorScheme.tertiary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.tertiary.copy(
                                        alpha = 0.24f
                                    ),
                                    inactiveTickColor = Color.Transparent,
                                    activeTickColor = MaterialTheme.colorScheme.surfaceBright
                                ),
                                onValueChangeFinished = {
                                    mainViewModel.calculateTip()
                                },
                                valueRange = 1f..50f,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$customTipPercentValue%",
                                    color = MaterialTheme.colorScheme.surface, // Text color as requested
                                    fontSize = 16.sp,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }

            SectionTitle("Split Bill")

            // Split Section
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp), // Added bottom padding to the Row
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Slider(
                            value = splitCount,
                            onValueChange = { newValue ->
                                mainViewModel.updateSplit(newValue)
                            },
                            onValueChangeFinished = {
                                mainViewModel.calculateTip()
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.tertiary,
                                activeTrackColor = MaterialTheme.colorScheme.tertiary,
                                inactiveTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f),
                                inactiveTickColor = Color.Transparent,
                                activeTickColor = MaterialTheme.colorScheme.surfaceBright
                            ),
                            valueRange = 1f..75f, // Updated range
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = splitCount.roundToInt().toString(),
                                color = MaterialTheme.colorScheme.surface, // Text color as requested
                                fontSize = 16.sp,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            SectionTitle("Totals")

            // Total Section
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tip Amount $${tipAmountValue}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Text(
                        text = "Total Amount $${totalAmountValue}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    if (splitCount > 1f) {
                        HorizontalDivider(
                            Modifier, DividerDefaults.Thickness, DividerDefaults.color
                        )
                        Text(
                            text = "Amount Per Person: $${splitValue}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val shareMessage = if (splitCount > 1f) {
                                "Total bill (including tip): $${totalAmountValue}. Your share is $${splitValue}."
                            } else {
                                "Total bill (including tip): $${totalAmountValue}"
                            }
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        enabled = totalAmountValue != "-",
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share Bill",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Share Bill")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth()
    )
}

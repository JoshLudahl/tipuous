package com.tips.tipuous.ui.main

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tips.tipuous.model.Percent
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel(),
    onAddReceipt: () -> Unit,
    onViewReceipts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSaveBill: (String, String, String) -> Unit,
) {
    // Observe StateFlows from ViewModel
    val billAmount by mainViewModel.bill.collectAsStateWithLifecycle()
    val selectedTipPercentEnum by mainViewModel.tipPercentEnum.collectAsStateWithLifecycle()
    val customTipPercentState by mainViewModel.customTipPercent.collectAsStateWithLifecycle()
    val splitCountState by mainViewModel.splitCount.collectAsStateWithLifecycle()

    val customTipSliderState = rememberSliderState(
        value = customTipPercentState.toFloat(),
        valueRange = 1f..50f,
    )
    LaunchedEffect(customTipSliderState.value) {
        mainViewModel.updateCustomTipValue(customTipSliderState.value.toInt())
    }
    LaunchedEffect(customTipPercentState) {
        if (customTipSliderState.value != customTipPercentState.toFloat()) {
            customTipSliderState.value = customTipPercentState.toFloat()
        }
    }

    val splitSliderState = rememberSliderState(
        value = splitCountState.toFloat(),
        valueRange = 1f..25f,
    )
    LaunchedEffect(splitSliderState.value) {
        mainViewModel.updateSplitCount(splitSliderState.value.roundToInt())
    }
    LaunchedEffect(splitCountState) {
        if (splitSliderState.value != splitCountState.toFloat()) {
            splitSliderState.value = splitCountState.toFloat()
        }
    }

    val tipAmountFormatted by mainViewModel.tipAmountFormatted.collectAsStateWithLifecycle()
    val totalAmountFormatted by mainViewModel.totalAmountFormatted.collectAsStateWithLifecycle()
    val amountPerPersonFormatted by mainViewModel.amountPerPersonFormatted.collectAsStateWithLifecycle()
    val isShareEnabled by mainViewModel.isShareable.collectAsStateWithLifecycle()


    // Local state for the TextField to manage text input directly
    var billText by remember(billAmount) {
        mutableStateOf(if ((billAmount == 0.0) || (billAmount.toString() == "0.0")) "" else billAmount.toString())
    }

    val context = LocalContext.current

    val floatingToolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom,
    )

    Scaffold(
        modifier = Modifier.nestedScroll(floatingToolbarScrollBehavior),
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {

                SectionTitle("Bill Amount")
                OutlinedTextField(
                    value = billText,
                    shape = RoundedCornerShape(30.dp),
                    onValueChange = { newText ->
                        if (newText.isEmpty() || newText.matches(Regex("""^\d*\.?\d*$"""))) {
                            billText = newText
                            val newBill = newText.toDoubleOrNull() ?: 0.0
                            // ViewModel now handles recalculation internally via combine flow
                            mainViewModel.setBill(newBill)
                        }
                    },
                    label = { Text("Enter Bill Amount") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.AttachMoney,
                            contentDescription = "Bill Amount",
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )

                SectionTitle("Tip Percentage")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            val tipOptions =
                                listOf(
                                    "5%" to Percent.FIVE,
                                    "10%" to Percent.TEN,
                                    "15%" to Percent.FIFTEEN,
                                )

                            tipOptions.forEach { (label, percentEnum) ->
                                AssistChip(
                                    onClick = { mainViewModel.updateTipPercentage(percentEnum) },
                                    label = { Text(label) },
                                    colors =
                                    AssistChipDefaults.assistChipColors(
                                        labelColor = if (selectedTipPercentEnum == percentEnum && selectedTipPercentEnum != Percent.CUSTOM) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface,
                                        containerColor =
                                        if (selectedTipPercentEnum == percentEnum && selectedTipPercentEnum != Percent.CUSTOM) {
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.tertiaryContainer.copy(
                                                alpha = 0.24f,
                                            )
                                        },
                                    ),
                                    border = BorderStroke(0.dp, Color.Transparent),
                                )
                            }

                            AssistChip(
                                onClick = { mainViewModel.handleCustomPercentageClick() },
                                label = { Text("Other") }, // Or use mainViewModel.customTipLabel.collectAsStateWithLifecycle() if it provides more dynamic text
                                colors =
                                AssistChipDefaults.assistChipColors(
                                    labelColor = if (selectedTipPercentEnum == Percent.CUSTOM) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface,
                                    containerColor =
                                    if (selectedTipPercentEnum == Percent.CUSTOM) {
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(
                                            alpha = 0.24f,
                                        )
                                    },
                                ),
                                border = BorderStroke(0.dp, Color.Transparent),
                            )
                        }

                        if (selectedTipPercentEnum == Percent.CUSTOM) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 8.dp,
                                        bottom = 8.dp,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Slider(
                                    state = customTipSliderState,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.tertiary,
                                        activeTrackColor = MaterialTheme.colorScheme.tertiary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f),
                                    ),
                                    track = { sliderState ->
                                        Box(
                                            modifier = Modifier.height(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            SliderDefaults.Track(
                                                colors = SliderDefaults.colors(
                                                    activeTrackColor = MaterialTheme.colorScheme.tertiary,
                                                    inactiveTrackColor = MaterialTheme.colorScheme.tertiary.copy(
                                                        alpha = 0.24f
                                                    ),
                                                ),
                                                sliderState = sliderState,
                                                modifier = Modifier.height(32.dp),
                                                thumbTrackGapSize = 0.dp,
                                                trackInsideCornerSize = 0.dp,
                                                drawStopIndicator = null
                                            )
                                            Box(
                                                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Percent,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = Color.White
                                                )
                                            }
                                            Box(
                                                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Box(
                                    modifier =
                                    Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "$customTipPercentState%",
                                        color = MaterialTheme.colorScheme.surface,
                                        fontSize = 16.sp,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                        }
                    }
                }

                SectionTitle("Split Bill")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Slider(
                                state = splitSliderState,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.tertiary,
                                    activeTrackColor = MaterialTheme.colorScheme.tertiary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f),
                                ),
                                track = { sliderState ->
                                    Box(
                                        modifier = Modifier.height(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        SliderDefaults.Track(
                                            colors = SliderDefaults.colors(
                                                activeTrackColor = MaterialTheme.colorScheme.tertiary,
                                                inactiveTrackColor = MaterialTheme.colorScheme.tertiary.copy(
                                                    alpha = 0.24f
                                                ),
                                            ),
                                            sliderState = sliderState,
                                            modifier = Modifier.height(32.dp),
                                            thumbTrackGapSize = 0.dp,
                                            trackInsideCornerSize = 0.dp,
                                            drawStopIndicator = null
                                        )
                                        Box(
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Group,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color.White
                                            )
                                        }
                                        Box(
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Groups,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier =
                                Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = splitCountState.toString(),
                                    color = MaterialTheme.colorScheme.surface,
                                    fontSize = 16.sp,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                }

                SectionTitle("Totals")

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 200.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tip Amount $$tipAmountFormatted",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                        )

                        Text(
                            text = "Total Amount $$totalAmountFormatted",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                        if (splitCountState > 1) { // Use Int state for logic
                            HorizontalDivider(
                                Modifier.padding(vertical = 8.dp), // Added padding for visual separation
                                thickness = DividerDefaults.Thickness,
                                color = DividerDefaults.color,
                            )
                            Text(
                                text = "Amount Per Person: $$amountPerPersonFormatted",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                onSaveBill(
                                    billAmount.toString(),
                                    tipAmountFormatted,
                                    totalAmountFormatted
                                )
                            },
                            enabled = isShareEnabled,
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Icon(
                                Icons.Filled.Save,
                                contentDescription = "Save to Receipts",
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Save to Receipts")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val shareMessage = mainViewModel.formatBillWithTipForSharing()
                                val sendIntent: Intent =
                                    Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareMessage)
                                        type = "text/plain"
                                    }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            },
                            enabled = isShareEnabled, // Use new StateFlow
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "Share Bill",
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Share Bill")
                        }
                    }
                }
            }

            HorizontalFloatingToolbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -FloatingToolbarDefaults.ScreenOffset)
                    .zIndex(1f),
                expanded = true,
                leadingContent = {
                    IconButton(onClick = onViewReceipts) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.List,
                            contentDescription = "Saved items",
                        )
                    }
                },
                trailingContent = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
                content = {
                    FilledIconButton(
                        modifier = Modifier.width(64.dp),
                        onClick = onAddReceipt,
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add receipt")
                    }
                },
                scrollBehavior = floatingToolbarScrollBehavior,
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
    )
}

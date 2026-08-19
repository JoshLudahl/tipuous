package com.tips.tipuous.ui.main

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderDefaults.Track
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tips.tipuous.model.Percent
import com.tips.tipuous.model.RoundingMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel(),
    onAddReceipt: () -> Unit,
    onViewReceipts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onSaveBill: (String, String, String, String) -> Unit,
) {
    // Observe StateFlows from ViewModel
    val billAmount by mainViewModel.bill.collectAsStateWithLifecycle()
    val billAmountFormatted by mainViewModel.billAmountFormatted.collectAsStateWithLifecycle()
    val taxAmount by mainViewModel.taxAmount.collectAsStateWithLifecycle()
    val taxAmountFormatted by mainViewModel.taxAmountFormatted.collectAsStateWithLifecycle()
    val calculateTipOnPreTax by mainViewModel.calculateTipOnPreTax.collectAsStateWithLifecycle()
    val roundingMode by mainViewModel.roundingMode.collectAsStateWithLifecycle()
    
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


    // Local state for TextFields to manage text input directly
    var billText by remember(billAmount) {
        mutableStateOf(if ((billAmount == 0.0)) "" else billAmount.toString())
    }
    var taxText by remember(taxAmount) {
        mutableStateOf(if ((taxAmount == 0.0)) "" else taxAmount.toString())
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "Bill Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        OutlinedTextField(
                            value = billText,
                            shape = RoundedCornerShape(30.dp),
                            onValueChange = { newText ->
                                if (newText.isEmpty() || newText.matches(Regex("""^\d*\.?\d*$"""))) {
                                    billText = newText
                                    mainViewModel.setBill(newText.toDoubleOrNull() ?: 0.0)
                                }
                            },
                            label = { Text("Subtotal") },
                            leadingIcon = { Icon(Icons.Filled.AttachMoney, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tax (Optional)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Tip on pre-tax", style = MaterialTheme.typography.labelSmall)
                                Switch(
                                    modifier = Modifier.scale(0.8f),
                                    checked = calculateTipOnPreTax,
                                    onCheckedChange = { mainViewModel.setCalculateTipOnPreTax(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        checkedTrackColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = taxText,
                            shape = RoundedCornerShape(30.dp),
                            onValueChange = { newText ->
                                if (newText.isEmpty() || newText.matches(Regex("""^\d*\.?\d*$"""))) {
                                    taxText = newText
                                    mainViewModel.setTaxAmount(newText.toDoubleOrNull() ?: 0.0)
                                }
                            },
                            label = { Text("Tax Amount") },
                            leadingIcon = { Icon(Icons.Filled.AttachMoney, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            ),
                        )
                    }
                }

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
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Tip Percentage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ToggleButtonDefaults.IconSpacing),
                        ) {
                            val tipOptions = listOf(
                                "15%" to Percent.FIFTEEN,
                                "18%" to Percent.EIGHTEEN,
                                "20%" to Percent.TWENTY,
                                "Other" to Percent.CUSTOM
                            )

                            tipOptions.forEachIndexed { index, (label, percentEnum) ->
                                val isSelected = selectedTipPercentEnum == percentEnum
                                ToggleButton(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        if (percentEnum == Percent.CUSTOM) {
                                            mainViewModel.handleCustomPercentageClick()
                                        } else {
                                            mainViewModel.updateTipPercentage(percentEnum)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shapes = when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        tipOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Done,
                                            contentDescription = null,
                                            modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                        )
                                        Spacer(modifier = Modifier.size(ToggleButtonDefaults.IconSpacing))
                                    }
                                    Text(label, maxLines = 1)
                                }
                            }
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
                        Text(
                            text = "Split Bill",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.fillMaxWidth(),
                        )
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

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 200.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "Bill Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SummaryRow(label = "Subtotal", value = "$$billAmountFormatted")
                            if (taxAmount > 0.0) {
                                SummaryRow(label = "Tax", value = "$$taxAmountFormatted")
                            }
                            SummaryRow(label = "Tip", value = "$$tipAmountFormatted")
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = DividerDefaults.Thickness,
                                color = DividerDefaults.color,
                            )
                            
                            SummaryRow(label = "Total", value = "$$totalAmountFormatted", isTotal = true)
                        }

                        if (splitCountState > 1) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Per Person", style = MaterialTheme.typography.labelMedium)
                                        Text("Split $splitCountState ways", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(
                                        text = "$$amountPerPersonFormatted",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(ToggleButtonDefaults.IconSpacing),
                            ) {
                                val roundingOptions = listOf(
                                    "Round Up" to RoundingMode.UP,
                                    "Round Down" to RoundingMode.DOWN
                                )
                                roundingOptions.forEachIndexed { index, (label, mode) ->
                                    val isSelected = roundingMode == mode
                                    ToggleButton(
                                        checked = isSelected,
                                        onCheckedChange = { mainViewModel.setRoundingMode(mode) },
                                        modifier = Modifier.weight(1f),
                                        shapes = when (index) {
                                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                            roundingOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                        },
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ToggleButtonDefaults.toggleButtonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Done,
                                                contentDescription = null,
                                                modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                            )
                                            Spacer(modifier = Modifier.size(ToggleButtonDefaults.IconSpacing))
                                        }
                                        Text(label, maxLines = 1)
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onSaveBill(
                                        billAmount.toString(),
                                        taxAmountFormatted,
                                        tipAmountFormatted,
                                        totalAmountFormatted
                                    )
                                },
                                enabled = isShareEnabled,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
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

                            Button(
                                onClick = {
                                    val shareMessage = mainViewModel.formatBillWithTipForSharing()
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareMessage)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                },
                                enabled = isShareEnabled,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
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
            }

            HorizontalFloatingToolbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -FloatingToolbarDefaults.ScreenOffset)
                    .zIndex(1f),
                expanded = true,
                colors = FloatingToolbarDefaults.standardFloatingToolbarColors (
                   toolbarContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                leadingContent = {
                    IconButton(onClick = onViewReceipts) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.List,
                            contentDescription = "Saved items",
                        )
                    }
                },
                trailingContent = {
                    IconButton(onClick = onNavigateToGuide) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Tipping Guide",
                        )
                    }
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
fun SummaryRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleSmall,
            fontWeight = if (isTotal) FontWeight.Black else FontWeight.Bold,
            color = if (isTotal) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
        )
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

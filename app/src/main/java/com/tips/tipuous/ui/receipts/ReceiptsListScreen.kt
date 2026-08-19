package com.tips.tipuous.ui.receipts

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tips.tipuous.model.Receipt
import com.tips.tipuous.navigation.Navigation
import com.tips.tipuous.navigation.Navigator
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ReceiptsListScreenPreview() {
    val mockReceipts =
        listOf(
            Receipt(
                id = "1",
                locationName = "Cassin's Coffee",
                dateEpochMillis = System.currentTimeMillis(),
                billTotal = 15.50,
                taxAmount = 1.24,
                tipAmount = 3.10,
                grandTotal = 19.84,
                imagePath = null,
            ),
            Receipt(
                id = "2",
                locationName = "Blue Jay Bistro",
                dateEpochMillis = System.currentTimeMillis() - 86400000,
                billTotal = 42.00,
                taxAmount = 3.36,
                tipAmount = 8.40,
                grandTotal = 53.76,
                imagePath = null,
            ),
        )
    MaterialTheme {
        ReceiptsListContent(
            receipts = mockReceipts,
            onBack = {},
            onReceiptClick = {},
            onDeleteReceipt = {},
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun ReceiptsListScreen(
    navigator: Navigator,
    viewModel: ReceiptsListViewModel = viewModel(),
) {
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()
    val trends by viewModel.monthTrend.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var receiptToDelete by remember { mutableStateOf<Receipt?>(null) }

    if (showDeleteDialog && receiptToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Receipt") },
            text = { Text("Are you sure you want to delete this receipt?") },
            confirmButton = {
                TextButton(onClick = {
                    receiptToDelete?.let { viewModel.delete(it) }
                    showDeleteDialog = false
                    receiptToDelete = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    ReceiptsListContent(
        receipts = receipts,
        trends = trends,
        onBack = { navigator.goBack() },
        onReceiptClick = { id -> navigator.navigate(Navigation.AddReceipt(receiptId = id)) },
        onDeleteReceipt = { r ->
            receiptToDelete = r
            showDeleteDialog = true
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun ReceiptsListContent(
    receipts: List<Receipt>,
    trends: List<MonthTrend> = emptyList(),
    onBack: () -> Unit,
    onReceiptClick: (String) -> Unit,
    onDeleteReceipt: (Receipt) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (receipts.isEmpty()) {
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("No receipts yet.", style = MaterialTheme.typography.titleMedium)
                Text("Add a receipt from the main screen to see it here.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            val totalSpent = receipts.sumOf { it.grandTotal }
            val totalTipped = receipts.sumOf { it.tipAmount }
            val totalBill = receipts.sumOf { it.billTotal }
            val count = receipts.size

            LazyColumn(
                Modifier
                    .padding(padding)
                    .padding(16.dp),
            ) {
                item {
                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .padding(vertical = 20.dp, horizontal = 8.dp)
                                    .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SummaryItem(
                                label = "Items",
                                value = count.toString(),
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                modifier = Modifier.weight(1f),
                            )
                            VerticalDivider(
                                modifier = Modifier.height(40.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                            SummaryItem(
                                label = "Spent",
                                value = "$${"%.2f".format(totalSpent)}",
                                icon = Icons.Default.Payments,
                                modifier = Modifier.weight(1f),
                            )
                            VerticalDivider(
                                modifier = Modifier.height(40.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                            SummaryItem(
                                label = "Tipped",
                                value = "$${"%.2f".format(totalTipped)}",
                                icon = Icons.Default.Savings,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp, start = 8.dp, end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Allocation",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Total: $${"%.2f".format(totalSpent)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        val billColor = MaterialTheme.colorScheme.outline
                        val tipColor = MaterialTheme.colorScheme.tertiary

                        // Segmented Bar
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(billColor),
                        ) {
                            if (totalSpent > 0) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxHeight()
                                            .weight(totalBill.toFloat())
                                            .clip(RoundedCornerShape(30.dp))
                                            .background(tipColor),
                                )
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxHeight()
                                            .weight(totalTipped.toFloat())
                                            .background(billColor),
                                )
                            }
                        }

                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            LegendItem(
                                label = "Bill",
                                amount = totalBill,
                                color = tipColor,
                            )

                            LegendItem(
                                label = "Tips",
                                amount = totalTipped,
                                color = billColor,
                            )
                        }
                    }
                }

                item {
                    TrendChart(trends)
                }

                item {
                    SectionHeading("Receipts")
                }

                items(receipts, key = { it.id }) { r ->
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .combinedClickable(
                                    onClick = { onReceiptClick(r.id) },
                                    onLongClick = { onDeleteReceipt(r) },
                                ),
                        shape = RoundedCornerShape(24.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Image / Icon placeholder
                            val bmp =
                                r.imagePath?.let { path ->
                                    try {
                                        val file = File(path)
                                        if (file.exists()) BitmapFactory.decodeFile(path) else null
                                    } catch (_: Exception) {
                                        null
                                    }
                                }

                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(64.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Image,
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(16.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                val fmt = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)
                                Text(
                                    text = r.locationName ?: "Receipt",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                )
                                Text(
                                    text = fmt.format(Date(r.dateEpochMillis)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "Bill: $${"%.2f".format(r.billTotal)} • Tax: $${"%.2f".format(r.taxAmount)} • Tip: $${"%.2f".format(r.tipAmount)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                )
                                if (r.advancedSplit != null && r.advancedSplit.people.isNotEmpty()) {
                                    Text(
                                        text = "Split with: ${r.advancedSplit.people.joinToString { it.name }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        maxLines = 1,
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = "$${"%.2f".format(r.grandTotal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$label: $${"%.2f".format(amount)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun SectionHeading(text: String) {
    Column(
        modifier =
            Modifier
                .padding(bottom = 12.dp)
                .fillMaxWidth(),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TrendChart(trends: List<MonthTrend>) {
    val maxTotal = trends.maxOfOrNull { it.total }?.coerceAtLeast(1.0) ?: 1.0
    val scope = rememberCoroutineScope()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "13-Month Trend",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(130.dp) // Increased height to accommodate shadow
                    .padding(top = 8.dp),
            // Padding for shadow
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            trends.forEach { trend ->
                val barHeight = (trend.total / maxTotal).toFloat()
                val tooltipState = rememberTooltipState()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .alpha(if (trend.total == 0.0) 0.4f else 1f),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        TooltipBox(
                            positionProvider =
                                TooltipDefaults.rememberTooltipPositionProvider(
                                    positioning = TooltipAnchorPosition.Above,
                                ),
                            tooltip = {
                                PlainTooltip {
                                    Text("$${"%.2f".format(trend.total)}")
                                }
                            },
                            state = tooltipState,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth(0.8f)
                                        .fillMaxHeight(barHeight.coerceAtLeast(0.05f))
                                        .clickable(
                                            enabled = trend.total > 0,
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                scope.launch {
                                                    tooltipState.show(
                                                        mutatePriority = MutatePriority.UserInput,
                                                    )
                                                }
                                            },
                                        )
                                        .then(
                                            if (trend.isCurrentMonth) {
                                                Modifier.shadow(
                                                    elevation = 8.dp,
                                                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                                                    spotColor = MaterialTheme.colorScheme.tertiary,
                                                    ambientColor = MaterialTheme.colorScheme.tertiary,
                                                )
                                            } else {
                                                Modifier
                                            },
                                        )
                                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                                        .background(
                                            if (trend.isCurrentMonth) {
                                                MaterialTheme.colorScheme.tertiary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            },
                                        ),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trend.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (trend.isCurrentMonth) FontWeight.Bold else FontWeight.Normal,
                        color =
                            if (trend.isCurrentMonth) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

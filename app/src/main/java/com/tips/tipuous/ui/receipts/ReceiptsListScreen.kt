package com.tips.tipuous.ui.receipts

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ReceiptsListScreenPreview() {
    val mockReceipts = listOf(
        Receipt(
            id = "1",
            locationName = "Cassin's Coffee",
            dateEpochMillis = System.currentTimeMillis(),
            billTotal = 15.50,
            tipAmount = 3.10,
            grandTotal = 18.60,
            imagePath = null
        ),
        Receipt(
            id = "2",
            locationName = "Blue Jay Bistro",
            dateEpochMillis = System.currentTimeMillis() - 86400000,
            billTotal = 42.00,
            tipAmount = 8.40,
            grandTotal = 50.40,
            imagePath = null
        )
    )
    MaterialTheme {
        ReceiptsListContent(
            receipts = mockReceipts,
            onBack = {},
            onReceiptClick = {},
            onDeleteReceipt = {}
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
                    Text("Continue")
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
        onBack = { navigator.goBack() },
        onReceiptClick = { id -> navigator.navigate(Navigation.AddReceipt(receiptId = id)) },
        onDeleteReceipt = { r ->
            receiptToDelete = r
            showDeleteDialog = true
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun ReceiptsListContent(
    receipts: List<Receipt>,
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
                modifier = Modifier.padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("No receipts yet.", style = MaterialTheme.typography.titleMedium)
                Text("Add a receipt from the main screen to see it here.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(Modifier.padding(padding).padding(16.dp)) {
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
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Image / Icon placeholder
                            val bmp = r.imagePath?.let { path ->
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
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(64.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
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
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val fmt = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)
                                Text(
                                    text = r.locationName ?: "Receipt",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = fmt.format(Date(r.dateEpochMillis)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Bill: $${"%.2f".format(r.billTotal)} • Tip: $${"%.2f".format(r.tipAmount)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = "$${"%.2f".format(r.grandTotal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

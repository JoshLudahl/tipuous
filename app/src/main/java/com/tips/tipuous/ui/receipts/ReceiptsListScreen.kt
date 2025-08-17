package com.tips.tipuous.ui.receipts

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tips.tipuous.model.Receipt
import com.tips.tipuous.navigation.Navigation
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun ReceiptsListScreen(
    navController: NavController,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipts") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                                    onClick = { navController.navigate(Navigation.AddReceipt(r.id)) },
                                    onLongClick = {
                                        receiptToDelete = r
                                        showDeleteDialog = true
                                    },
                                ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
                                Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.height(64.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = null,
                                    modifier = Modifier.height(64.dp),
                                    tint = MaterialTheme.colorScheme.outline,
                                )
                            }
                            Spacer(Modifier.padding(horizontal = 8.dp))
                            // Middle column: name and date, takes remaining space
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                Text(r.locationName ?: "Receipt", style = MaterialTheme.typography.titleMedium)
                                Text("Date: ${fmt.format(Date(r.dateEpochMillis))}", style = MaterialTheme.typography.bodySmall)
                            }
                            // Right column: bill, tip, total
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp), horizontalAlignment = Alignment.End) {
                                Text(
                                    "Bill: $${"%.2f".format(r.billTotal)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                                Text(
                                    "Tip: $${"%.2f".format(r.tipAmount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                                Text(
                                    "Total: $${"%.2f".format(r.grandTotal)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.tips.tipuous.ui.receipts

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tips.tipuous.data.ReceiptRepository
import com.tips.tipuous.model.Receipt
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun ReceiptsListScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { ReceiptRepository(context) }
    val receipts = remember { mutableStateListOf<Receipt>() }

    LaunchedEffect(Unit) {
        receipts.clear()
        receipts.addAll(repo.getAll())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipts") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (receipts.isEmpty()) {
            Column(
                modifier = Modifier.padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("No receipts yet.", style = MaterialTheme.typography.titleMedium)
                Text("Add a receipt from the main screen to see it here.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(Modifier.padding(padding).padding(16.dp)) {
                items(receipts) { r ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { /* future: open detail */ },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Row(Modifier.padding(12.dp)) {
                            val bmp = r.imagePath?.let { path ->
                                try {
                                    val file = File(path)
                                    if (file.exists()) BitmapFactory.decodeFile(path) else null
                                } catch (_: Exception) { null }
                            }
                            if (bmp != null) {
                                Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.height(64.dp))
                                Spacer(Modifier.padding(horizontal = 8.dp))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                Text(r.locationName ?: "Receipt", style = MaterialTheme.typography.titleMedium)
                                Text("Date: ${fmt.format(Date(r.dateEpochMillis))}", style = MaterialTheme.typography.bodySmall)
                                Text("Bill: $${"%.2f".format(r.billTotal)}  Tip: $${"%.2f".format(r.tipAmount)}  Total: $${"%.2f".format(r.grandTotal)}",
                                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }
        }
    }
}

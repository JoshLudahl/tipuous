package com.tips.tipuous.ui.composeables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tips.tipuous.model.AdvancedSplit
import com.tips.tipuous.model.Person
import com.tips.tipuous.utilities.Conversion

@Composable
fun AdvancedSplitSection(
    advancedSplit: AdvancedSplit,
    personResults: Map<String, Double>,
    onAddPerson: (String) -> Unit,
    onRemovePerson: (String) -> Unit,
    onAddItem: (String, String, Double) -> Unit,
    onRemoveItem: (String, String) -> Unit,
) {
    var newPersonName by remember { mutableStateOf("") }
    var isAddingPerson by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        advancedSplit.people.forEach { person ->
            PersonCard(
                person = person,
                total = personResults[person.id] ?: 0.0,
                onRemovePerson = { onRemovePerson(person.id) },
                onAddItem = { name, amount -> onAddItem(person.id, name, amount) },
                onRemoveItem = { itemId -> onRemoveItem(person.id, itemId) },
            )
        }

        if (isAddingPerson) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = newPersonName,
                    onValueChange = { newPersonName = it },
                    label = { Text("Person Name") },
                    modifier = Modifier
                        .weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                )
                IconButton(
                    onClick = {
                        if (newPersonName.isNotBlank()) {
                            onAddPerson(newPersonName)
                            newPersonName = ""
                            isAddingPerson = false
                        }
                    },
                ) {
                    Icon(Icons.Rounded.Done, contentDescription = "Add")
                }
            }
        } else {
            Button(
                onClick = { isAddingPerson = true },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                shape = RoundedCornerShape(20.dp),
            ) {
                Icon(Icons.Rounded.PersonAdd, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Person")
            }
        }
    }
}

@Composable
fun PersonCard(
    person: Person,
    total: Double,
    onRemovePerson: () -> Unit,
    onAddItem: (String, Double) -> Unit,
    onRemoveItem: (String) -> Unit,
) {
    var itemName by remember { mutableStateOf("") }
    var itemAmount by remember { mutableStateOf("") }
    var isAddingItem by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onRemovePerson) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Remove person",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            person.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "\$${Conversion.formatNumberToIncludeTrailingZero(item.amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Remove item",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onRemoveItem(item.id) },
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (isAddingItem) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item", fontSize = 10.sp) },
                        modifier = Modifier
                            .weight(1.5f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                    OutlinedTextField(
                        value = itemAmount,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d*$"""))) itemAmount = it },
                        label = { Text("$", fontSize = 10.sp) },
                        modifier = Modifier
                            .weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                    )
                    IconButton(
                        onClick = {
                            val amount = itemAmount.toDoubleOrNull() ?: 0.0
                            if (itemName.isNotBlank() && amount > 0) {
                                onAddItem(itemName, amount)
                                itemName = ""
                                itemAmount = ""
                                isAddingItem = false
                            }
                        },
                    ) {
                        Icon(Icons.Rounded.Done, contentDescription = "Add")
                    }
                }
            } else {
                TextButton(
                    onClick = { isAddingItem = true },
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(Icons.Rounded.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Item", style = MaterialTheme.typography.labelSmall)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Total (Incl. Tax/Tip)",
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = "\$${Conversion.formatNumberToIncludeTrailingZero(total)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

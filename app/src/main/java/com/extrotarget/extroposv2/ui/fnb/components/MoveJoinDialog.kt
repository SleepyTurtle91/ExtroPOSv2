package com.extrotarget.extroposv2.ui.fnb.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.core.data.model.fnb.Table
import com.extrotarget.extroposv2.core.data.model.fnb.TableStatus

@Composable
fun MoveJoinDialog(
    sourceTable: Table,
    isMove: Boolean,
    targetTables: List<Table>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedTableId by remember { mutableStateOf<String?>(null) }
    
    val filteredTables = if (isMove) {
        targetTables.filter { it.status == TableStatus.AVAILABLE }
    } else {
        targetTables.filter { it.status == TableStatus.AVAILABLE || it.status == TableStatus.OCCUPIED }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isMove) "Move ${sourceTable.name} to..." else "Join ${sourceTable.name} with...") },
        text = {
            if (filteredTables.isEmpty()) {
                Text("No available tables to ${if (isMove) "move" else "join"}.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(filteredTables) { table ->
                        ListItem(
                            headlineContent = { Text(table.name) },
                            supportingContent = { Text("${table.status} - ${table.capacity} Pax") },
                            trailingContent = {
                                RadioButton(
                                    selected = selectedTableId == table.id,
                                    onClick = { selectedTableId = table.id }
                                )
                            },
                            modifier = Modifier.clickable { selectedTableId = table.id }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedTableId?.let { onConfirm(it) } },
                enabled = selectedTableId != null
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

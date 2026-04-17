package com.extrotarget.extroposv2.ui.fnb.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.extrotarget.extroposv2.core.data.model.fnb.Table
import com.extrotarget.extroposv2.core.data.model.fnb.TableStatus

@Composable
fun TableActionDialog(
    table: Table,
    onDismiss: () -> Unit,
    onAction: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Actions for ${table.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (table.status == TableStatus.OCCUPIED) {
                    Button(
                        onClick = { onAction("OPEN") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open Order")
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onAction("MOVE") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.MoveUp, contentDescription = null)
                            Text("Move")
                        }
                        Button(
                            onClick = { onAction("JOIN") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.AddLink, contentDescription = null)
                            Text("Join")
                        }
                    }
                }

                if (table.status == TableStatus.AVAILABLE) {
                    Button(
                        onClick = { onAction("DELETE") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Table")
                    }
                }

                if (table.status == TableStatus.DIRTY) {
                    Button(
                        onClick = { onAction("CLEAN") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Mark Cleaned")
                    }
                } else {
                    OutlinedButton(
                        onClick = { onAction("DIRTY") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Mark Dirty")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Button(
                    onClick = { onAction("QR_GEN") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate Order QR")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

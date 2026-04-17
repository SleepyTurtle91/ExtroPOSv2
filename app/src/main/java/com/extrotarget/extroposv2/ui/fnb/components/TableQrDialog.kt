package com.extrotarget.extroposv2.ui.fnb.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.extrotarget.extroposv2.core.data.model.fnb.Table
import com.extrotarget.extroposv2.ui.components.qr.QrCodeView

@Composable
fun TableQrDialog(
    table: Table,
    onDismiss: () -> Unit,
    onPrint: (String) -> Unit
) {
    // In a real production environment, this URL would point to your ordering portal
    // with the branch ID and table ID as parameters.
    val orderUrl = "https://order.extropos.com/order?branch=MAIN&table=${table.id}"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TABLE QR ORDER",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = table.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFF3B82F6)
                )
                
                Text(
                    text = "Scan to view menu & order",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Surface(
                    modifier = Modifier.size(260.dp),
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(24.dp)) {
                        QrCodeView(
                            content = orderUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onPrint(orderUrl) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("PRINT STICKER", fontWeight = FontWeight.Black)
                }
                
                Text(
                    text = "Stickers include secure table verification tokens.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

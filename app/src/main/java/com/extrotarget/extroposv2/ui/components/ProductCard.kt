package com.extrotarget.extroposv2.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.util.CurrencyUtils

@Composable
fun ProductCard(
    product: Product,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp,
        onClick = { onProductClick(product) },
        enabled = product.isAvailable,
        color = if (product.isAvailable) Color.White else Color(0xFFF1F5F9)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Category Strip (Top)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(if (product.isAvailable) Color(0xFF3B82F6) else Color(0xFFCBD5E1))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = product.name.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (product.isAvailable) Color(0xFF1E293B) else Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )
                    
                    if (product.sku.isNotEmpty()) {
                        Text(
                            text = product.sku,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = CurrencyUtils.format(product.price),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Black
                    )

                    if (product.isWeightBased) {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "KG",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

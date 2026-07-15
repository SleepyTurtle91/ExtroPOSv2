package com.extrotarget.extroposv2.ui.components.stitch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps
import java.math.BigDecimal

@Composable
fun StitchProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    categoryColor: Color = StitchColor.Primary,
    isSelected: Boolean = false,
    selectedQuantity: BigDecimal = BigDecimal.ZERO
) {
    Surface(
        modifier = modifier
            .height(140.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) StitchColor.Primary else StitchColor.OutlineVariant,
                shape = RoundedCornerShape(8.dp)
            ),
        color = if (isSelected) StitchColor.SurfaceContainerHighest else StitchColor.SurfaceContainerLowest,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Category Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(categoryColor)
                    .align(Alignment.TopCenter)
            )

            if (isSelected && selectedQuantity > BigDecimal.ZERO) {
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                        .align(Alignment.TopEnd),
                    shape = CircleShape,
                    color = StitchColor.Primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = selectedQuantity.stripTrailingZeros().toPlainString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelCaps.copy(fontSize = 10.sp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = product.name.uppercase(),
                        style = MaterialTheme.typography.labelCaps.copy(
                            color = StitchColor.OnSurface,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        maxLines = 2
                    )
                    Text(
                        text = "SKU: ${product.sku}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = StitchColor.Outline,
                            fontSize = 10.sp
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (product.stockQuantity <= BigDecimal("5")) {
                        Surface(
                            color = StitchColor.ErrorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "STK: ${product.stockQuantity.toInt()}",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelCaps.copy(
                                    fontSize = 9.sp,
                                    color = StitchColor.Error
                                )
                            )
                        }
                    } else {
                        Surface(
                            color = StitchColor.TertiaryContainer.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "STK: ${product.stockQuantity.toInt()}",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelCaps.copy(
                                    fontSize = 9.sp,
                                    color = StitchColor.Tertiary
                                )
                            )
                        }
                    }

                    Text(
                        text = "RM ${product.price}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = StitchColor.Primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
}

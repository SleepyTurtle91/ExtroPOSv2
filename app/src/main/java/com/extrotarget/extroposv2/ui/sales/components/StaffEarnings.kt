package com.extrotarget.extroposv2.ui.sales.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.core.data.model.carwash.Staff
import com.extrotarget.extroposv2.core.util.CurrencyUtils
import java.math.BigDecimal

@Composable
fun StaffEarnings(
    staffList: List<Staff>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Slate 50
            .padding(24.dp)
            .widthIn(max = 1000.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Commission Formula Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E293B)) // Slate 800 (Professional/Genius Style)
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = null,
                        tint = Color(0xFF10B981), // Success Green
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "COMMISSION FORMULA APPLIED",
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp
                    )
                }
                
                Text(
                    "E = Σ (Price × Rate%) + Fixed Fee",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "TOP PERFORMER",
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            staffList.firstOrNull()?.name?.uppercase() ?: "N/A",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "TOTAL PAYOUT",
                            color = Color(0xFF10B981), // Success Green
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            CurrencyUtils.format(BigDecimal("245.50")), // Mock data or from DB
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Staff List Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "STAFF PERFORMANCE",
                color = Color(0xFF64748B), // Slate 500
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Text(
                "${staffList.size} ACTIVE",
                color = Color(0xFF3B82F6), // Primary Blue
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        // Staff List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(staffList) { staff ->
                StaffEarningsCard(staff)
            }
        }
    }
}

@Composable
fun StaffEarningsCard(staff: Staff) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)) // Slate 200
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFF1F5F9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null, 
                        tint = Color(0xFF475569), // Slate 600
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        staff.name.uppercase(),
                        color = Color(0xFF0F172A), // Slate 900
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        "12 JOBS COMPLETED", // Mock
                        color = Color(0xFF64748B), // Slate 500
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyUtils.format(BigDecimal("105.50")), // Mock
                    color = Color(0xFF0F172A), // Slate 900
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    "COMMISSION",
                    color = Color(0xFF10B981), // Success Green
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

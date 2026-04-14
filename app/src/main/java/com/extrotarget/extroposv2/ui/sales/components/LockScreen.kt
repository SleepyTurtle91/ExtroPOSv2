package com.extrotarget.extroposv2.ui.sales.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LockScreen(
    onUnlock: (String) -> Unit,
    errorMessage: String? = null
) {
    var pin by remember { mutableStateOf("") }
    var currentTime by remember { mutableStateOf(Date()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Slate 950
    ) {
        // Background Glows
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(100.dp)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = (-100).dp, y = (-100).dp)
                    .size(400.dp)
                    .background(Color(0xFF3B82F6).copy(alpha = 0.1f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 100.dp, y = 100.dp)
                    .size(300.dp)
                    .background(Color(0xFF10B981).copy(alpha = 0.1f), CircleShape)
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Left Side: Time & Branding (Visible on Tablets/Wide Screens)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(64.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF60A5FA))
                    }
                    Column {
                        Text(
                            text = "EXTROPOS V2",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "SECURE TERMINAL KL-01",
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }

                // Time
                Column {
                    Text(
                        text = timeFormat.format(currentTime),
                        color = Color.White,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-4).sp
                    )
                    Text(
                        text = dateFormat.format(currentTime).uppercase(),
                        color = Color(0xFF94A3B8),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                // Status Cards
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    StatusCard(Icons.Default.Fingerprint, "DEVICE STATUS", "SYSTEM SECURED", Color(0xFF10B981))
                    StatusCard(Icons.Default.CloudUpload, "LAST SYNC", "2 MINS AGO", Color.White)
                }
            }

            // Right Side: PIN Entry
            Surface(
                modifier = Modifier
                    .width(500.dp)
                    .fillMaxHeight(),
                color = Color(0xFF0F172A), // Slate 900
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Lock Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFF2563EB), RoundedCornerShape(32.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        "STAFF ACCESS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        "ENTER 4-DIGIT SECURITY PIN",
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // PIN Indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        repeat(4) { index ->
                            val isActive = pin.length > index
                            val color = if (errorMessage != null) Color.Red else if (isActive) Color(0xFF10B981) else Color(0xFF334155)
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(if (isActive && errorMessage == null) Modifier.background(color.copy(alpha = 0.5f)) else Modifier)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Number Pad
                    val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "CLR", "0", "FP")
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        for (i in 0 until 4) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                for (j in 0 until 3) {
                                    val key = numbers[i * 3 + j]
                                    KeyButton(
                                        text = key,
                                        onClick = {
                                            when (key) {
                                                "CLR" -> pin = ""
                                                "FP" -> {} // Fingerprint
                                                else -> {
                                                    if (pin.length < 4) {
                                                        pin += key
                                                        if (pin.length == 4) {
                                                            onUnlock(pin)
                                                            pin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                    
                    TextButton(onClick = { }) {
                        Text(
                            "FORGOT PIN? CONTACT ADMIN",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(20.dp))
        }
        Column {
            Text(label, color = Color(0xFF64748B), fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.sp)
            Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun KeyButton(text: String, onClick: () -> Unit) {
    val isSpecial = text == "CLR" || text == "FP"
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 80.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSpecial) Color.Transparent else Color(0xFF1E293B).copy(alpha = 0.5f))
            .border(1.dp, if (isSpecial) Color.Transparent else Color(0xFF334155), RoundedCornerShape(24.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text == "FP") {
            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF334155), modifier = Modifier.size(28.dp))
        } else {
            Text(
                text = text,
                color = if (text == "CLR") Color(0xFFEF4444) else Color.White,
                fontSize = if (isSpecial) 10.sp else 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = if (isSpecial) 1.sp else 0.sp
            )
        }
    }
}

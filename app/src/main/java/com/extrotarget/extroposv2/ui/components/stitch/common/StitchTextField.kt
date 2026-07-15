package com.extrotarget.extroposv2.ui.components.stitch.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extrotarget.extroposv2.ui.theme.StitchColor
import com.extrotarget.extroposv2.ui.theme.labelCaps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    error: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelCaps.copy(
                color = if (error != null) StitchColor.Error else StitchColor.OnSurfaceVariant,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { 
                if (placeholder.isNotEmpty()) {
                    Text(
                        placeholder, 
                        style = MaterialTheme.typography.bodySmall.copy(color = StitchColor.OnSurfaceVariant.copy(alpha = 0.5f))
                    )
                } 
            },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = StitchColor.Outline) } },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(8.dp),
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StitchColor.Primary,
                unfocusedBorderColor = StitchColor.OutlineVariant,
                errorBorderColor = StitchColor.Error,
                focusedContainerColor = StitchColor.SurfaceContainerLowest,
                unfocusedContainerColor = StitchColor.SurfaceContainerLowest,
                errorContainerColor = StitchColor.SurfaceContainerLowest
            )
        )
        
        if (error != null) {
            Text(
                text = error,
                color = StitchColor.Error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

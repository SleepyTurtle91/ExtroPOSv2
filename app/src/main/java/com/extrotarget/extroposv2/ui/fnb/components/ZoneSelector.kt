package com.extrotarget.extroposv2.ui.fnb.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ZoneSelector(
    zones: List<String>,
    selectedZone: String,
    onZoneSelected: (String) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = zones.indexOf(selectedZone).coerceAtLeast(0),
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        divider = {}
    ) {
        zones.forEach { zone ->
            Tab(
                selected = selectedZone == zone,
                onClick = { onZoneSelected(zone) },
                text = {
                    Text(
                        text = zone,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            )
        }
    }
}

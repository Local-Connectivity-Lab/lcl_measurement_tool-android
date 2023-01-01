package com.lcl.lclmeasurementtool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun TagIcon(modifier: Modifier, icon: ImageVector) {
    Icon(modifier = modifier.border(width = 2.dp, color = Color.LightGray, shape = RoundedCornerShape(10.dp)).padding(4.dp)
        .background(MaterialTheme.colorScheme.surface),
        imageVector = icon,
        contentDescription = null)
}

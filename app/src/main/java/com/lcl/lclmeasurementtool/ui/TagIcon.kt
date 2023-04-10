package com.lcl.lclmeasurementtool.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TagIcon(modifier: Modifier, icon: ImageVector) {
    Icon(modifier = modifier
        .border(width = 2.dp, color = Color.LightGray, shape = RoundedCornerShape(10.dp))
        .padding(2.dp)
        .background(MaterialTheme.colorScheme.surface),
        imageVector = icon,
        contentDescription = null)
}

@Composable
fun TagLabel(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(BorderStroke(0.8.dp, MaterialTheme.colorScheme.onPrimaryContainer),
                shape = RoundedCornerShape(CornerSize(8.dp))
            )
            .padding(4.dp)

    ) {
        
        Text(text = label, fontSize = 10.sp, fontStyle = FontStyle.Italic)
    }
}

@Preview
@Composable
fun TagLabel_Preview() {
    TagLabel(label = "Reported")
}
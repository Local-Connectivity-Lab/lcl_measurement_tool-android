package com.lcl.lclmeasurementtool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeRoute() {
    HomeScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = modifier.fillMaxHeight()) {
            SignalStrengthCard(modifier = modifier)
            ConnectivityCard(modifier = modifier)
        }

        FloatingActionButton(onClick = {}, modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 12.dp, bottom = 12.dp)) {
            Icon(imageVector = Filled.PlayArrow, contentDescription = null)
        }
    }
}

@Composable
private fun SignalStrengthCard(modifier: Modifier = Modifier) {
    val fontSize = 20.sp
    Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(20.dp)
        ) {

            Icon(modifier = modifier,
                imageVector = Filled.SignalCellularAlt,
                contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Signal Strength:", fontSize = fontSize)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "val", fontWeight = FontWeight.Bold, fontSize = fontSize)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "dBm", fontSize = fontSize)
            Spacer(modifier = Modifier.width(20.dp))
            Box(modifier = Modifier
                .size(10.dp)
                .clip(
                    CircleShape
                )
                .background(Color.Green))
        }
    }
}

@Composable
private fun ConnectivityCard(modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(12.dp)
        ) {

            Icon(modifier = modifier.padding(end = 12.dp),
                imageVector = Filled.NetworkCheck,
                contentDescription = null)
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    DataEntry(icon = Rounded.CloudUpload, text = "123 mbps")
                    DataEntry(icon = Rounded.CloudDownload, text = "123 mbps")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    DataEntry(icon = Rounded.NetworkPing, text = "123 ms")
                    DataEntry(icon = Rounded.NetworkPing, text = "123 ms")
                }
            }
        }
    }
}

@Composable
fun DataEntry(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}


@Preview
@Composable
fun HomePreview() {
    BoxWithConstraints {
        HomeScreen()
    }
}
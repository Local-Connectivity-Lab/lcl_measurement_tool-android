package com.lcl.lclmeasurementtool.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel

@Composable
fun ConnectivityItem(
    data: ConnectivityReportModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    itemSeparation: Dp = 12.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = itemSeparation, horizontal = 10.dp)
        ) {
            TagIcon(modifier = modifier, icon = Rounded.NetworkCheck)
            Spacer(modifier = Modifier.width(12.dp))
            ConnectivityContent(data = data)
        }
    }
}

@Composable
private fun ConnectivityContent(data: ConnectivityReportModel, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Speed")
                    DataEntry(icon = Rounded.CloudUpload, text = "${data.uploadSpeed} mbps")
                    DataEntry(icon = Rounded.CloudDownload, text = "${data.downloadSpeed} mbps")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Ping")
                    DataEntry(icon = Rounded.NetworkPing, text = "${data.ping} ms")
                    DataEntry(icon = Rounded.Cancel, text = "${data.packetLoss}%")
                }
            }
        }
        Text(
            text = data.timestamp,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Preview
@Composable
private fun InterestsCardPreview() {
        val data = ConnectivityReportModel(123.123, 345.345, "timestamp2", "hi2", "deviceID2", 123.32, 345.52, 23.22, 10.3)
        Surface {
            ConnectivityItem(
                data = data,
                onClick = { }
            )
        }
}

@Preview
@Composable
private fun InterestsCardLongNamePreview() {
    val data = ConnectivityReportModel(123.123, 345.345, "timestamp2", "hi2", "deviceID2", 123.32, 345.52, 23.22, 10.3)
        Surface {
            ConnectivityItem(
                data = data,
                onClick = { }
            )
        }
}

@Preview
@Composable
private fun InterestsCardLongDescriptionPreview() {
    val data = ConnectivityReportModel(123.123, 345.345, "timestamp2", "hi2", "deviceID2", 123.32, 345.52, 23.22, 10.3)
    Surface {
        ConnectivityItem(
            data = data,
            onClick = { }
        )
    }
}

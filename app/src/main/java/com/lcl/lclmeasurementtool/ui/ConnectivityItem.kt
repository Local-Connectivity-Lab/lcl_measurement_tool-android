package com.lcl.lclmeasurementtool.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ConnectivityItem(
    name: String,
    following: Boolean,
    topicImageUrl: String,
    onClick: () -> Unit,
    onFollowButtonClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    description: String = "",
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
            ConnectivityContent(name, description)
        }
    }
}

@Composable
private fun ConnectivityContent(value: String, time: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DataEntry(icon = Rounded.CloudUpload, text = "123 mbps")
                    DataEntry(icon = Rounded.CloudDownload, text = "123 mbps")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DataEntry(icon = Rounded.NetworkPing, text = "123 ms")
                    DataEntry(icon = Rounded.NetworkPing, text = "123 ms")
                }
            }
        }
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Preview
@Composable
private fun InterestsCardPreview() {
        Surface {
            ConnectivityItem(
                name = "-85",
                description = "fhewukrhweurhuwe",
                following = false,
                topicImageUrl = "",
                onClick = { },
                onFollowButtonClick = { }
            )
        }
}

@Preview
@Composable
private fun InterestsCardLongNamePreview() {
        Surface {
            ConnectivityItem(
                name = "-100",
                description = "rhewkrweruwier",
                following = true,
                topicImageUrl = "",
                onClick = { },
                onFollowButtonClick = { }
            )
        }
}

@Preview
@Composable
private fun InterestsCardLongDescriptionPreview() {
    Surface {
        ConnectivityItem(
            name = "Compose",
            description = "This is a very very very very very very very " +
                    "very very very long description",
            following = false,
            topicImageUrl = "",
            onClick = { },
            onFollowButtonClick = { }
        )
    }
}

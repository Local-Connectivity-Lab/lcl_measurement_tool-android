package com.lcl.lclmeasurementtool.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SignalStrengthItem(
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
            TagIcon(modifier, Icons.Rounded.SignalCellularAlt)
            Spacer(modifier = Modifier.width(12.dp))
            ConnectivityContent(name, description)
        }
    }
}


@Composable
private fun ConnectivityContent(value: String, time: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row {
            Text(
                text = "$value dBm",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    vertical = 4.dp
                ),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.padding(start = 10.dp))
            Text(text = "(level)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    vertical = 4.dp
                ))
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
            SignalStrengthItem(
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
            SignalStrengthItem(
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
        SignalStrengthItem(
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

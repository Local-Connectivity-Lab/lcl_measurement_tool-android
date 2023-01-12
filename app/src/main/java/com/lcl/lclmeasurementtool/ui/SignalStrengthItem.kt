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
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel

@Composable
fun SignalStrengthItem(
    data: SignalStrengthReportModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
            SignalStrengthContent(data = data)
        }
    }
}


@Composable
private fun SignalStrengthContent(data: SignalStrengthReportModel, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row {
            Text(
                text = "${data.dbm} dBm",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    vertical = 4.dp
                ),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.padding(start = 10.dp))
            Text(text = "(level code: ${data.levelCode})",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    vertical = 4.dp
                ))
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
    val data = SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp2", "cellID2", -82, 2)
        Surface {
            SignalStrengthItem(
                data = data,
                onClick = { },
            )
        }
}

@Preview
@Composable
private fun InterestsCardLongNamePreview() {
    val data = SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp2", "cellID2", -82, 2)
        Surface {
            SignalStrengthItem(
                data = data,
                onClick = { },
            )
        }

}

@Preview
@Composable
private fun InterestsCardLongDescriptionPreview() {
    val data = SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp2", "cellID2", -82, 2)
    Surface {

        SignalStrengthItem(
            data = data,
            onClick = { },
        )
    }
}

package com.lcl.lclmeasurementtool.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun Dialog(
    icon: ImageVector,
    onConfirmClicked: () -> Unit,
    title: String,
    text: String,

) {
    val openDialog = remember { mutableStateOf(true) }
    if (openDialog.value) {
        AlertDialog(
            icon = { Icon(imageVector = icon, contentDescription = null)},
            onDismissRequest = {  },
            title = {
                Text(text = title)
            },
            text = {
                Text(text)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmClicked()
                        openDialog.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
        )
    }
}
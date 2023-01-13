package com.lcl.lclmeasurementtool.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lcl.lclmeasurementtool.R

@Composable
fun Login(
    onLoginButtonClicked: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(painter = painterResource(id = R.drawable.lcl_purple_gold_uw), contentDescription = null, modifier = Modifier
            .width(300.dp)
            .height(300.dp).padding(top = 50.dp))
        Button(onClick = onLoginButtonClicked, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer), modifier = Modifier.padding(top = 30.dp)) {
            Text(text = "Scan to Login")
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    Login({})
}
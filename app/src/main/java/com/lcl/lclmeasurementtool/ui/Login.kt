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
fun Login() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(painter = painterResource(id = R.drawable.lcl_purple_gold_uw), contentDescription = null, modifier = Modifier
            .width(300.dp)
            .height(300.dp).padding(top = 50.dp))
        Button(onClick = {  }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
            Text(text = "Scan to Login")
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    Login()
}
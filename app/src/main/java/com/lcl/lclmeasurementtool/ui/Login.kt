package com.lcl.lclmeasurementtool.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kongzue.dialogx.dialogs.TipDialog
import com.lcl.lclmeasurementtool.LoginStatus
import com.lcl.lclmeasurementtool.MainActivityViewModel
import com.lcl.lclmeasurementtool.R
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun Login(
    viewModel: MainActivityViewModel
) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
//    TipDialog.init(context)
    val qrCodeLauncher = rememberLauncherForActivityResult(ScanQRCode()) {qrResult ->
        when(qrResult) {
            is QRResult.QRSuccess -> {
                coroutineScope.launch {
                    val result = viewModel.saveAndSend(qrResult.content.rawValue)
                    if (result != LoginStatus.RegistrationSucceeded) {
                        Log.d("MainActivityVM", "Login Failed")
//                        TipDialog.show("Login Failed")
                    }
                }
            }
            else -> {
                Log.d("MainActivityVM", "Login Failed QRCode Scan")
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(painter = painterResource(id = R.drawable.lcl_purple_gold_uw), contentDescription = null, modifier = Modifier
            .width(300.dp)
            .height(300.dp)
            .padding(top = 50.dp))
        Button(onClick = {qrCodeLauncher.launch(null)}, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer), modifier = Modifier.padding(top = 30.dp)) {
            Text(text = "Scan to Login")
        }
    }
}

//@Preview
//@Composable
//fun LoginPreview() {
//    Login()
//}
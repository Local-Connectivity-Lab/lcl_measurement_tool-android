package com.lcl.lclmeasurementtool.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lcl.lclmeasurementtool.LoginStatus
import com.lcl.lclmeasurementtool.MainActivityViewModel
import com.lcl.lclmeasurementtool.R
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(
    viewModel: MainActivityViewModel
) {
    val coroutineScope = rememberCoroutineScope()

//    val context = LocalContext.current
//    TipDialog.init(context)
    val snackbarHostState = remember { SnackbarHostState() }
    val loginStatus = viewModel.loginState.collectAsStateWithLifecycle()
    val qrCodeLauncher = rememberLauncherForActivityResult(ScanQRCode()) {qrResult ->
        when(qrResult) {
            is QRResult.QRSuccess -> {
                coroutineScope.launch {
                    viewModel.login(qrResult.content.rawValue)
                }
            }
            else -> {
                Log.d("MainActivityVM", "Login Failed QRCode Scan")
            }
        }
    }

    LaunchedEffect(loginStatus.value) {
        when(val status = loginStatus.value) {
            is LoginStatus.Initial -> {}

            is LoginStatus.RegistrationFailed -> {
                Log.d("Login", "⚠️ ${status.reason}")
                snackbarHostState.showSnackbar("⚠️ ${status.reason}", duration = SnackbarDuration.Long)
            }

            is LoginStatus.RegistrationSucceeded -> {
                Log.d("Login", "⚠️ yeaaaa!")
            }
        }
    }

    Scaffold(
        snackbarHost = {SnackbarHost(hostState = snackbarHostState)}
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .padding(padding)
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
}
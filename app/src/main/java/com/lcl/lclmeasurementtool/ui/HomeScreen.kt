package com.lcl.lclmeasurementtool.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lcl.lclmeasurementtool.BuildConfig
import com.lcl.lclmeasurementtool.ConnectivityTestResult
import com.lcl.lclmeasurementtool.MainActivityViewModel
import com.lcl.lclmeasurementtool.SignalStrengthResult
import kotlinx.coroutines.cancel

@Composable
fun HomeRoute(isOffline: Boolean, mainActivityViewModel: MainActivityViewModel) {
    HomeScreen(isOffline = isOffline, mainActivityViewModel = mainActivityViewModel)
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, isOffline: Boolean, mainActivityViewModel: MainActivityViewModel) {

    val offline by remember { mutableStateOf(isOffline) }
    val snackbarHostState = remember { SnackbarHostState() }

    val isMLabTestActive = mainActivityViewModel.isMLabTestActive.collectAsStateWithLifecycle()
    val mlabRttResult = mainActivityViewModel.mlabRttResult.collectAsStateWithLifecycle()
    val mlabUploadResult = mainActivityViewModel.mlabUploadResult.collectAsStateWithLifecycle()
    val mlabDownloadResult = mainActivityViewModel.mlabDownloadResult.collectAsStateWithLifecycle()
    val signalStrength = mainActivityViewModel.signalStrengthResult.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignalStrengthCard(modifier = modifier, signalStrengthResult = signalStrength.value)
            ConnectivityCard(
                label = "MLab",
                modifier = modifier,
                rttValue = mlabRttResult.value,
                uploadResult = mlabUploadResult.value,
                downloadResult = mlabDownloadResult.value
            )

            if (isMLabTestActive.value) {
                LCLLoadingWheel(contentDesc = "")
            }
        }

        FloatingActionButton(onClick = {

            if (BuildConfig.FLAVOR == "full" && offline) {
                Log.d("HomeScreen", "device is currently offline")
                return@FloatingActionButton
            }

            if (!isMLabTestActive.value) {
                mainActivityViewModel.runMLabTest()
            } else {
                coroutineScope.cancel("User initiated the cancellation (mlab)")
                mainActivityViewModel.cancelMLabTest()
            }
        },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 12.dp)) {
            Icon(imageVector = if (isMLabTestActive.value) Filled.Pause else Filled.PlayArrow, contentDescription = null)
        }
    }

    if (BuildConfig.FLAVOR == "full" && offline) {
        ShowMessage(isOffline = true, msg = "Your Device is offline. Please connect to the Internet via Cellular network", snackbarHostState = snackbarHostState)
    }
}

@Composable
fun ShowMessage(isOffline: Boolean, msg: String, snackbarHostState: SnackbarHostState) {
    LaunchedEffect(isOffline) {
        snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Long)
    }
}

@Composable
private fun SignalStrengthCard(
    modifier: Modifier = Modifier,
    signalStrengthResult: SignalStrengthResult
) {
    val fontSize = 18.sp

    val (dbm, level) = signalStrengthResult

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
            Text(text = "$dbm", fontWeight = FontWeight.Bold, fontSize = fontSize)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "dBm", fontSize = fontSize)
            Spacer(modifier = Modifier.width(20.dp))
            Box(modifier = Modifier
                .size(10.dp)
                .clip(
                    CircleShape
                )
                .background(level.color()))
        }
    }
}

@Composable
private fun ConnectivityCard(
    label: String,
    modifier: Modifier = Modifier,
    rttValue: ConnectivityTestResult,
    uploadResult: ConnectivityTestResult,
    downloadResult: ConnectivityTestResult,
) {
    Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)
        ) {

            Icon(modifier = modifier.padding(end = 12.dp),
                imageVector = Filled.NetworkCheck,
                contentDescription = null)
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    when (uploadResult) {
                        is ConnectivityTestResult.Result -> {
                            DataEntry(icon = Rounded.CloudUpload, text = "${uploadResult.result} Mbps")
                        }
                        else -> DataEntry(icon = Rounded.CloudUpload, text = "0.0 Mbps")
                    }


                    when (downloadResult) {
                        is ConnectivityTestResult.Result -> {
                            DataEntry(icon = Rounded.CloudDownload, text = "${downloadResult.result} Mbps")
                        }
                        else -> {
                            DataEntry(icon = Rounded.CloudDownload, text = "0.0 Mbps")
                        }
                    }

                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    val formattedRtt = when (rttValue) {
                        is ConnectivityTestResult.Result -> {
                            val numeric = rttValue.result.toDoubleOrNull() ?: 0.0
                            if (numeric > 0) String.format("%.1f", numeric) else "0.0"
                        }
                        else -> {
                            "0.0" // or rttValue.error if you want to display the error message
                        }
                    }

                    DataEntry(icon = Rounded.NetworkPing, text = "$formattedRtt ms")
                    DataEntry(icon = Rounded.Cancel, text = "0 % loss")
                }

            }
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Powered by $label", fontWeight = FontWeight.Thin, fontSize = 10.sp, modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 4.dp))
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
fun ConnectivityCardPreview() {

    Column {
        ConnectivityCard(
            label = "IperfRunner",
            rttValue = ConnectivityTestResult.Result("1", Color.Red),
            uploadResult = ConnectivityTestResult.Result("1", Color.Blue),
            downloadResult = ConnectivityTestResult.Result("1", Color.Green)
        )

        ConnectivityCard(
            label = "MLab",
            rttValue = ConnectivityTestResult.Result("1", Color.Red),
            uploadResult = ConnectivityTestResult.Result("1", Color.Blue),
            downloadResult = ConnectivityTestResult.Result("1", Color.Green)
        )
    }
}


//@Preview
//@Composable
//fun HomePreview() {
//    BoxWithConstraints {
//        HomeScreen(isOffline = false)
//    }
//}
package com.lcl.lclmeasurementtool.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.filled.NetworkCheck
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lcl.lclmeasurementtool.ConnectivityTestResult
import com.lcl.lclmeasurementtool.MainActivityViewModel
import com.lcl.lclmeasurementtool.PingResultState
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin

@Composable
fun HomeRoute(isOffline: Boolean, mainActivityViewModel: MainActivityViewModel) {
    HomeScreen(isOffline = isOffline, mainActivityViewModel = mainActivityViewModel)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, isOffline: Boolean, mainActivityViewModel: MainActivityViewModel) {

    val offline by remember { mutableStateOf(isOffline) }
    var isTestActive = mainActivityViewModel.isTestActive.collectAsStateWithLifecycle()
    val pingResult = mainActivityViewModel.pingResult.collectAsStateWithLifecycle()

    val jobs = mutableListOf<Job>()
    val context = LocalContext.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = modifier.fillMaxHeight()) {
            SignalStrengthCard(modifier = modifier)
            ConnectivityCard(
                modifier = modifier,
                pingResult = pingResult.value,
                uploadResult = mainActivityViewModel.uploadResult,
                downloadResult = mainActivityViewModel.downloadResult
            )
        }

        FloatingActionButton(onClick = {

            if (!isTestActive.value) {
                jobs.clear()
                jobs.add(mainActivityViewModel.doPing())
                Log.d("HOMEScreen", "Test Starts")
            } else {
                jobs.forEach { job -> job.cancel() }
                Log.d("HOMEScreen", "Test Cancelled")
            }

        },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 12.dp)) {
            Icon(imageVector = Filled.PlayArrow, contentDescription = null)
        }
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
) {
    val fontSize = 18.sp


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
            Text(text = "val", fontWeight = FontWeight.Bold, fontSize = fontSize)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "dBm", fontSize = fontSize)
            Spacer(modifier = Modifier.width(20.dp))
            Box(modifier = Modifier
                .size(10.dp)
                .clip(
                    CircleShape
                )
                .background(Color.Green))
        }
    }
}

@Composable
private fun ConnectivityCard(
    modifier: Modifier = Modifier,
    pingResult: PingResultState,
    uploadResult: ConnectivityTestResult.Result?,
    downloadResult: ConnectivityTestResult.Result?,
) {
    Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(12.dp)
        ) {

            Icon(modifier = modifier.padding(end = 12.dp),
                imageVector = Filled.NetworkCheck,
                contentDescription = null)
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    DataEntry(icon = Rounded.CloudUpload, text = "${uploadResult?.result ?: "0"} mbps")
                    DataEntry(icon = Rounded.CloudDownload, text = "${downloadResult?.result ?: "0"} mbps")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    var pingNum = "0"
                    var pingLoss = "0"
                    when(pingResult) {
                        is PingResultState.Success -> {
                            pingNum = pingResult.result.avg!!
                            pingLoss = pingResult.result.numLoss!!
                        }
                        is PingResultState.Error -> {
                            pingNum = "0"
                            pingLoss = "0"
                            Log.d("HOMEScreen", pingResult.error.message ?: "Error occurred")
                            // TODO: show error message
                        }
                    }

                    DataEntry(icon = Rounded.NetworkPing, text = "$pingNum ms")
                    DataEntry(icon = Rounded.Cancel, text = "$pingLoss % loss")
                }
            }
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


//@Preview
//@Composable
//fun HomePreview() {
//    BoxWithConstraints {
//        HomeScreen(isOffline = false)
//    }
//}
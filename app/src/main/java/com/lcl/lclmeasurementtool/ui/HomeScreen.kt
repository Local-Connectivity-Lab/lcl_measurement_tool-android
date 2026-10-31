package com.lcl.lclmeasurementtool.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val DashboardSurface = Color(0xFFF5F7FB)
private val DashboardTile = Color(0xFFFFFFFF)
private val DashboardBorder = Color(0xFFE3E8F3)
private val DashboardTextMuted = Color(0xFF677085)
private val DashboardTextStrong = Color(0xFF1C2434)
private val DashboardLive = Color(0xFF1FA56A)
private val DashboardPending = Color(0xFF7A8090)

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
    val pingPacketLoss = mainActivityViewModel.pingPacketLoss.collectAsStateWithLifecycle()
    val pingRttResult = mainActivityViewModel.pingRttResult.collectAsStateWithLifecycle()
    val signalStrength = mainActivityViewModel.signalStrengthResult.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignalStrengthCard(modifier = modifier, signalStrengthResult = signalStrength.value)
            ConnectivityCard(
                packetLoss = pingPacketLoss.value,
                pingRtt = pingRttResult.value,
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

    Card(
        colors = CardDefaults.cardColors(DashboardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DashboardBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(horizontal = 16.dp, vertical = 18.dp)
        ) {

            Icon(modifier = modifier,
                imageVector = Filled.SignalCellularAlt,
                contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Signal Strength:", fontSize = fontSize, color = DashboardTextStrong)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "$dbm", fontWeight = FontWeight.Bold, fontSize = fontSize, color = DashboardTextStrong)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "dBm", fontSize = fontSize, color = DashboardTextMuted)
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
    packetLoss: String = "-- %",
    pingRtt: String = "-- ms",
) {
    val uploadText = when (uploadResult) {
        is ConnectivityTestResult.Result -> "${uploadResult.result} Mbps"
        else -> "--"
    }
    val downloadText = when (downloadResult) {
        is ConnectivityTestResult.Result -> "${downloadResult.result} Mbps"
        else -> "--"
    }
    val mlabRttText = when (rttValue) {
        is ConnectivityTestResult.Result -> {
            val numeric = rttValue.result.toDoubleOrNull() ?: 0.0
            if (numeric > 0) "${String.format("%.1f", numeric)} ms" else "--"
        }
        else -> "--"
    }
    val hasLivePing = pingRtt != "-- ms" && packetLoss != "-- %"

    Card(
        colors = CardDefaults.cardColors(DashboardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DashboardBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Filled.NetworkCheck,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Mobile Connectivity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DashboardTextStrong
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (hasLivePing) DashboardLive.copy(alpha = 0.16f) else DashboardPending.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (hasLivePing) "Live" else "Pending",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (hasLivePing) DashboardLive else DashboardPending,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardMetricTile(
                    icon = Rounded.CloudDownload,
                    label = "Download",
                    value = downloadText,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricTile(
                    icon = Rounded.CloudUpload,
                    label = "Upload",
                    value = uploadText,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardMetricTile(
                    icon = Rounded.NetworkPing,
                    label = "MLab Latency",
                    value = mlabRttText,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricTile(
                    icon = Rounded.Cancel,
                    label = "Packet Loss",
                    value = packetLoss,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                DashboardMetricTile(
                    icon = Rounded.NetworkPing,
                    label = "Custom Ping",
                    value = pingRtt,
                    modifier = Modifier.fillMaxWidth(0.49f),
                )
            }

            Text(
                text = "Powered by $label",
                fontWeight = FontWeight.Thin,
                fontSize = 10.sp,
                color = DashboardTextMuted,
                modifier = Modifier
                .align(Alignment.End)
                .padding(end = 10.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun DashboardMetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(DashboardTile),
        border = androidx.compose.foundation.BorderStroke(1.dp, DashboardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = DashboardTextMuted
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = DashboardTextStrong
            )
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
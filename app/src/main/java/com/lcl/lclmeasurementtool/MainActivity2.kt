package com.lcl.lclmeasurementtool

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lcl.lclmeasurementtool.datastore.Dispatcher
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import com.lcl.lclmeasurementtool.model.viewmodels.ConnectivityViewModel
import com.lcl.lclmeasurementtool.model.viewmodels.SignalStrengthViewModel
import com.lcl.lclmeasurementtool.networking.NetworkMonitor
import com.lcl.lclmeasurementtool.ui.LCLApp
import com.lcl.lclmeasurementtool.ui.Login
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity2 : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    val viewModel: MainActivityViewModel by viewModels()
    val signalVM: SignalStrengthViewModel by viewModels()
    val connectivityVM: ConnectivityViewModel by viewModels()

    val signals = listOf(
        SignalStrengthReportModel("deviceID1", 123.121, 456.451, "timestamp1", "cellID1", -81, 1),
        SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp2", "cellID2", -82, 2),
        SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp3", "cellID2", -82, 2),
        SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp4", "cellID2", -82, 2),
        SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp5", "cellID2", -82, 2),
        SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp6", "cellID2", -82, 2),
        SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp7", "cellID2", -82, 2),
        SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp8", "cellID2", -82, 2),
        SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp9", "cellID2", -82, 2),
        SignalStrengthReportModel("deviceID2", 123.122, 456.452, "timestamp10", "cellID2", -82, 2),
    )

    val connectivities = listOf(
        ConnectivityReportModel(123.123, 345.345, "timestamp1", "hi1", "deviceID1", 123.31, 345.51, 23.21, 0.0),
        ConnectivityReportModel(123.123, 345.345, "timestamp2", "hi2", "deviceID2", 123.32, 345.52, 23.22, 10.3),
        ConnectivityReportModel(123.123, 345.345, "timestamp3", "hi2", "deviceID2", 123.32, 345.52, 23.22, 10.3),
        ConnectivityReportModel(123.123, 345.345, "timestamp4", "hi2", "deviceID2", 123.32, 345.52, 23.22, 10.3),
        ConnectivityReportModel(123.123, 345.345, "timestamp5", "hi2", "deviceID2", 123.32, 345.52, 23.22, 10.3),
        ConnectivityReportModel(123.123, 345.345, "timestamp6", "hi2", "deviceID2", 123.32, 345.52, 23.22, 10.3),
        ConnectivityReportModel(123.123, 345.345, "timestamp7", "hi2", "deviceID2", 123.32, 345.52, 23.22, 10.3),
        ConnectivityReportModel(123.123, 345.345, "timestamp8", "hi2", "deviceID2", 123.32, 345.52, 23.22, 10.3),
        ConnectivityReportModel(123.123, 345.345, "timestamp9", "hi2", "deviceID2", 123.32, 345.52, 23.22, 10.3),
        ConnectivityReportModel(123.123, 345.345, "timestamp10", "hi2", "deviceID2", 123.32, 345.52, 23.22, 10.3),
    )

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        signals.forEach {
            signalVM.insert(it)
        }

        connectivities.forEach {
            connectivityVM.insert(it)
        }



        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Login)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach {
                        uiState = it
                    }
                    .collect()
            }
        }

        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LCLApp(windowSizeClass = calculateWindowSizeClass(activity = this), networkMonitor)
//            when(uiState) {
//                MainActivityUiState.Login -> Login()
//                else -> {}
//            }
        }

    }
}
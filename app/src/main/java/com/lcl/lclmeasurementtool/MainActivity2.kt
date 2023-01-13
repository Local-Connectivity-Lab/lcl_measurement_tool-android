package com.lcl.lclmeasurementtool

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.jsoniter.JsonIterator
import com.jsoniter.spi.JsonException
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lcl.lclmeasurementtool.Utils.AnalyticsUtils
import com.lcl.lclmeasurementtool.Utils.ECDSA
import com.lcl.lclmeasurementtool.Utils.Hex
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.model.datamodel.QRCodeKeysModel
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import com.lcl.lclmeasurementtool.model.viewmodels.ConnectivityViewModel
import com.lcl.lclmeasurementtool.model.viewmodels.SignalStrengthViewModel
import com.lcl.lclmeasurementtool.networking.NetworkMonitor
import com.lcl.lclmeasurementtool.ui.LCLApp
import com.lcl.lclmeasurementtool.ui.Login
import com.microsoft.appcenter.analytics.Analytics
import com.yzq.zxinglibrary.android.CaptureActivity
import com.yzq.zxinglibrary.bean.ZxingConfig
import com.yzq.zxinglibrary.common.Constant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.util.*
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

        Log.d("MAIN Activity", "uiState is ${uiState is MainActivityUiState.Login}")

        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            if (uiState == MainActivityUiState.Login) {
                Login { showScanner() }
                return@setContent
            }
            LCLApp(windowSizeClass = calculateWindowSizeClass(activity = this), networkMonitor)
        }
    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) {
            return@registerForActivityResult
        }
        val data = result.data
        if (data != null) {
            val content = data.getStringExtra(Constant.CODED_CONTENT)
            val jsonObj: QRCodeKeysModel = try {
                JsonIterator.deserialize(
                    content,
                    QRCodeKeysModel::class.java
                )
            } catch (e: JsonException) {
                TipDialog.show(getString(R.string.qrcode_invalid_format), WaitDialog.TYPE.ERROR)
                val reasons =
                    AnalyticsUtils.formatProperties(e.message, Arrays.toString(e.stackTrace))
                Analytics.trackEvent(AnalyticsUtils.QR_CODE_PARSING_FAILED, reasons)
                return@registerForActivityResult
            }
            val sigma_t = jsonObj.sigmaT
            val sk_t = jsonObj.skT
            val pk_a = jsonObj.pk_a
            WaitDialog.show(getString(R.string.validation))
            when(val validationResult = validate(sigma_t, pk_a, sk_t)) {
                is LoginStatus.KeyVerificationFailed -> {

                }
                is LoginStatus.KeyVerificationExceptionOccurred -> {

                }
                is LoginStatus.Success -> {
                    saveAndSend(validationResult.pkAHex, validationResult.skTHex, validationResult.sigmaTHex)
                }
            }
        }
    }

    private fun saveAndSend(pkAHex: ByteArray, skTHex: ByteArray, sigmaTHex: ByteArray) {
        var pk_t: ECPublicKey
        var ecPrivateKey: ECPrivateKey
        try {
            ecPrivateKey = ECDSA.DeserializePrivateKey(skTHex)
            pk_t = ECDSA.DerivePublicKey(ecPrivateKey)
        } catch (e: Exception) {
            return
        }
    }

    private fun validate(sigmaT: String, pkA: String, skT: String): LoginStatus {
        val sigmaTHex: ByteArray
        val pkAHex: ByteArray
        val skTHex: ByteArray
        try {
            sigmaTHex = Hex.decodeHex(sigmaT)
            pkAHex = Hex.decodeHex(pkA)
            skTHex = Hex.decodeHex(skT)
            if (!ECDSA.Verify(
                    skTHex,
                    sigmaTHex,
                    ECDSA.DeserializePublicKey(pkAHex))
            ) {
                // TODO: Add Analytics TrackEvent
                return LoginStatus.KeyVerificationFailed
            }
        } catch (e: Exception) {
            // TODO: return some status
            return LoginStatus.KeyVerificationExceptionOccurred(e)
        }

        return LoginStatus.Success(sigmaTHex, pkAHex, skTHex)
    }

    private fun showScanner() {
        val intent = Intent(this, CaptureActivity::class.java)
        val config = ZxingConfig()
        config.isFullScreenScan = false
        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config)
        activityResultLauncher.launch(intent)
    }
}

sealed interface LoginStatus {
    object KeyVerificationFailed: LoginStatus
    class KeyVerificationExceptionOccurred(e: Exception): LoginStatus
    data class Success(val sigmaTHex: ByteArray, val pkAHex: ByteArray, val skTHex: ByteArray): LoginStatus {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Success

            if (!sigmaTHex.contentEquals(other.sigmaTHex)) return false
            if (!pkAHex.contentEquals(other.pkAHex)) return false
            if (!skTHex.contentEquals(other.skTHex)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = sigmaTHex.contentHashCode()
            result = 31 * result + pkAHex.contentHashCode()
            result = 31 * result + skTHex.contentHashCode()
            return result
        }
    }
}
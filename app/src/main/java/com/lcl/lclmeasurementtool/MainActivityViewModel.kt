package com.lcl.lclmeasurementtool

import android.content.Context
import android.location.Location
import android.os.Build
import android.telephony.CellSignalStrength
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.protobuf.ByteString
import com.jsoniter.JsonIterator
import com.jsoniter.spi.JsonException
import com.kongzue.dialogx.dialogs.TipDialog
import com.lcl.lclmeasurementtool.Utils.ECDSA
import com.lcl.lclmeasurementtool.Utils.Hex
import com.lcl.lclmeasurementtool.Utils.SecurityUtils
import com.lcl.lclmeasurementtool.constants.NetworkConstants
import com.lcl.lclmeasurementtool.features.iperf.IperfRunner
import com.lcl.lclmeasurementtool.features.iperf.IperfStatus
import com.lcl.lclmeasurementtool.features.ping.Ping
import com.lcl.lclmeasurementtool.features.ping.PingError
import com.lcl.lclmeasurementtool.features.ping.PingErrorCase
import com.lcl.lclmeasurementtool.features.ping.PingResult
import com.lcl.lclmeasurementtool.location.LocationService
import com.lcl.lclmeasurementtool.model.datamodel.*
import com.lcl.lclmeasurementtool.model.repository.ConnectivityRepository
import com.lcl.lclmeasurementtool.model.repository.NetworkApiRepository
import com.lcl.lclmeasurementtool.model.repository.SignalStrengthRepository
import com.lcl.lclmeasurementtool.model.repository.UserDataRepository
import com.lcl.lclmeasurementtool.telephony.SignalStrengthLevelEnum
import com.lcl.lclmeasurementtool.telephony.SignalStrengthMonitor
import com.lcl.lclmeasurementtool.ui.LCLLoadingWheel
import com.lcl.lclmeasurementtool.util.TimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val networkApi: NetworkApiRepository,
    private val locationService: LocationService,
    private val signalStrengthMonitor: SignalStrengthMonitor,
    private val connectivityRepository: ConnectivityRepository,
    private val signalStrengthRepository: SignalStrengthRepository
) : ViewModel() {

    companion object {
        const val TAG = "MainActivityViewModel"
    }

    // UI
    val uiState: StateFlow<MainActivityUiState> = userDataRepository.userData.map {
        if (it.loggedIn) MainActivityUiState.Success(it) else MainActivityUiState.Login
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Login,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    // Authentication
    fun login(hPKR: ByteString, skT: ByteString) = viewModelScope.launch {
        userDataRepository.setKeys(hPKR, skT)
    }

    fun logout() = viewModelScope.launch {
        userDataRepository.logout()
    }

    fun setR(R: ByteString) = viewModelScope.launch {
        userDataRepository.setR(R)
    }

    fun setDeviceId(id: String) = viewModelScope.launch {
        userDataRepository.setDeviceID(id)
    }

    private val getDeviceID = userDataRepository.userData.map { it.deviceID }



    private suspend fun register(registration: RegistrationModel) = networkApi.register(registration)

    suspend fun saveAndSend(result: String): LoginStatus {

        val job = viewModelScope.async {
            val jsonObj: QRCodeKeysModel = try {
                JsonIterator.deserialize(
                    result,
                    QRCodeKeysModel::class.java
                )
            } catch (e: JsonException) {
//                TipDialog.show("The QR Code is invalid. Please rescan the code or contact the administrator at lcl@seattlecommunitynetwork.org.", WaitDialog.TYPE.ERROR)
                Log.d(TAG, "The QR Code is invalid. Please rescan the code or contact the administrator at lcl@seattlecommunitynetwork.org.")
//                val reasons =
//                    AnalyticsUtils.formatProperties(e.message, Arrays.toString(e.stackTrace))
//                Analytics.trackEvent(AnalyticsUtils.QR_CODE_PARSING_FAILED, reasons)
                return@async LoginStatus.QRCodeParseFailed
            }

            val sigma_t = jsonObj.sigmaT
            val sk_t = jsonObj.skT
            val pk_a = jsonObj.pk_a

            when (val validationResult = validate(sigmaT = sigma_t, pkA = pk_a, skT = sk_t)) {
                is LoginStatus.KeyVerificationFailed -> {
                    return@async validationResult
                }

                is LoginStatus.KeyVerificationException -> {
                    return@async validationResult
                }

                is LoginStatus.ScanSuccess -> {
                    val skTHex = validationResult.skTHex
                    val pk_t: ECPublicKey
                    val ecPrivateKey: ECPrivateKey
                    try {
                        ecPrivateKey = ECDSA.DeserializePrivateKey(skTHex)
                        pk_t = ECDSA.DerivePublicKey(ecPrivateKey)
                    } catch (e: Exception) {
                        TipDialog.show("Key Deserialization Failed")
                        // TODO: return some error code
                        return@async LoginStatus.KeyGenerationFailed
                    }

                    val secureRandom = SecureRandom()
                    val r = ByteArray(16)
                    secureRandom.nextBytes(r)
                    setR(ByteString.copyFrom(r))
                    // TODO: maybe zero out the R array
                    val byteArray = ByteArrayOutputStream()
                    val h_pkr: ByteArray
                    val h_sec: ByteArray
                    val h_concat: ByteArray
                    val sigma_r: ByteArray
                    try {
                        withContext(Dispatchers.IO) {
                            byteArray.write(pk_t.encoded)
                            byteArray.write(r)
                            h_pkr = SecurityUtils.digest(byteArray.toByteArray(), SecurityUtils.SHA_256_HASH)
                            byteArray.reset()
                            byteArray.write(skTHex)
                            byteArray.write(pk_t.encoded)
                            h_sec = SecurityUtils.digest(byteArray.toByteArray(), SecurityUtils.SHA_256_HASH)
                            byteArray.reset()
                            byteArray.write(h_pkr)
                            byteArray.write(h_sec)
                            h_concat = byteArray.toByteArray()
                        }
                        sigma_r = ECDSA.Sign(h_concat, ecPrivateKey)
                    } catch (e: Exception) {
                        TipDialog.show("Key Signature Failed")
                        // TODO: return error code
                        return@async LoginStatus.KeySignFailed
                    }

                    val registration = RegistrationModel(
                        Hex.encodeHexString(sigma_r, false),
                        Hex.encodeHexString(h_concat, false),
                        Hex.encodeHexString(r, false))

                    val response = register(registration)
                    if (response.isSuccessful) {
                        login(ByteString.copyFrom(h_pkr), ByteString.copyFrom(skTHex))
                    } else {
//                    TipDialog.show("Registration failed. Please try again.", WaitDialog.TYPE.ERROR)
                        response.close()
                        return@async LoginStatus.RegistrationFailed
                    }

                    response.close()
                    return@async LoginStatus.RegistrationSucceeded
                }
                else -> {return@async LoginStatus.UnexpectedErrorOccurred}
            }
        }
        return job.await() as LoginStatus
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
                return LoginStatus.KeyVerificationFailed
            }
        } catch (e: Exception) {
            return LoginStatus.KeyVerificationException(e)
        }

        return LoginStatus.ScanSuccess(sigmaTHex, pkAHex, skTHex)
    }


    // Network Testing
    private var _pingResult = MutableStateFlow(PingResultState())
    private var _downloadResult = MutableStateFlow(ConnectivityTestResult())
    private var _uploadResult = MutableStateFlow(ConnectivityTestResult())
    private val _isTestActive = MutableStateFlow(false)


    val isTestActive = _isTestActive.asStateFlow()
    val pingResult: StateFlow<PingResultState> = _pingResult.asStateFlow()
    var downloadResult: StateFlow<ConnectivityTestResult> = _downloadResult.asStateFlow()
    var uploadResult: StateFlow<ConnectivityTestResult> = _uploadResult.asStateFlow()
    var signalStrengthResult = signalStrengthMonitor.signalStrength.map {s ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val report = s.getCellSignalStrengths(CellSignalStrength::class.java)
            if (report.isEmpty()) {
                SignalStrengthResult(0, SignalStrengthLevelEnum.POOR)
            } else {
                val data = report[0]
                SignalStrengthResult(data.dbm, SignalStrengthLevelEnum.init(data.level))
            }
        } else {
            val dBm = s.getGsmSignalStrength()
            val level = SignalStrengthLevelEnum.init(s.level)
            SignalStrengthResult(dBm, level)
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = SignalStrengthResult(0, SignalStrengthLevelEnum.POOR),
        started = SharingStarted.WhileSubscribed(5_000)
    )

    private suspend fun runPing() {
        try {
            Ping.cancellableStart(address = NetworkConstants.PING_TEST_ADDRESS, timeout = 1000)
                .onStart {
                    Log.d(TAG, "isActive = true")
                    _isTestActive.value = true
                }
                .onCompletion {
                    if (it != null) {
                        Log.d(TAG, "Error is ${it.message}")
                        _isTestActive.value = false
                    }
                }
                .collect {
                    _pingResult.value = when(it.error.code) {
                        PingErrorCase.OK ->  PingResultState.Success(it)
                        else -> PingResultState.Error(it.error)
                    }
                }
        } catch (e: IllegalArgumentException) {
            _pingResult.value = PingResultState.Error(PingError(PingErrorCase.OTHER, e.message))
            Log.e(TAG, "Ping Config error")
        }
    }

    private suspend fun getUploadResult(context: Context) {
        IperfRunner().getTestResult(IperfRunner.iperfUploadConfig, context.cacheDir)
            .onStart {
                _isTestActive.value = true
            }
            .onCompletion {
                if (it != null) {
                    // save to DB and send over the network
                    Log.d(TAG, "Error is ${it.message}")
                    _isTestActive.value = false
                }
            }
            .collectLatest { result ->
                _uploadResult.value = when(result.status) {
                IperfStatus.RUNNING -> ConnectivityTestResult.Result(result.bandWidth, Color.LightGray)
                IperfStatus.FINISHED -> ConnectivityTestResult.Result(result.bandWidth, Color.Black)
                IperfStatus.ERROR -> {
                    _isTestActive.value = false
                    ConnectivityTestResult.Error(result.errorMSg!!)
                }
            }
        }
    }


    private suspend fun getDownloadResult(context: Context) {
        IperfRunner().getTestResult(IperfRunner.iperfDownloadConfig, context.cacheDir)
            .onStart {
                _isTestActive.value = true
            }
            .onCompletion {
                if (it != null) {
                    // save to DB and send over the network
                    Log.d(TAG, "Error is ${it.message}")
                    _isTestActive.value = false
                }
            }
            .collectLatest { result ->
                _downloadResult.value = when(result.status) {
                IperfStatus.RUNNING -> ConnectivityTestResult.Result(result.bandWidth, Color.LightGray)
                IperfStatus.FINISHED -> ConnectivityTestResult.Result(result.bandWidth, Color.Black)
                IperfStatus.ERROR -> {
                    _isTestActive.value = false
                    ConnectivityTestResult.Error(result.errorMSg!!)
                }
            }
        }
    }

    private var testJob: Job? = null

    fun runTest(context: Context) {
        if (testJob?.isActive == true) {
            testJob?.cancel()
        }

        testJob = viewModelScope.launch {
            resetTestResult()

            try {
                runPing()

                if (_pingResult.value is PingResultState.Error) {
                    this.cancel("Ping Test Failed")
                }
                ensureActive()

                getUploadResult(context = context)

                if (_uploadResult.value is ConnectivityTestResult.Error) {
                    this.cancel("Upload Test Failed")
                }

                ensureActive()

                getDownloadResult(context = context)

                if (_downloadResult.value is ConnectivityTestResult.Error) {
                    this.cancel("Download Test Failed")
                }

                ensureActive()

                _isTestActive.value = false
                Log.d(TAG, "ping, upload, download are finished")
                val curTime = TimeUtil.getCurrentTime()
                val cellID = signalStrengthMonitor.getCellID()

                locationService.lastLocation().combine(getDeviceID) { locaion, deviceID ->
                    Pair(locaion, deviceID)
                }.collect {
                    Log.d(TAG, it.first.toString())
                    Log.d(TAG, it.second)
                    val connectivityResult = ConnectivityReportModel(
                        it.first.latitude,
                        it.first.longitude,
                        curTime,
                        cellID,
                        it.second,
                        (_uploadResult.value as ConnectivityTestResult.Result).result.dropLast(5)
                            .toDouble(),
                        (_downloadResult.value as ConnectivityTestResult.Result).result.dropLast(5)
                            .toDouble(),
                        (_pingResult.value as PingResultState.Success).result.avg!!.toDouble(),
                        (_pingResult.value as PingResultState.Success).result.numLoss!!.toDouble(),
                    )

                    connectivityRepository.insert(connectivityResult)

                    val signalStrengthResult = SignalStrengthReportModel(
                        it.first.latitude,
                        it.first.longitude,
                        curTime,
                        cellID,
                        it.second,
                        signalStrengthResult.value.dbm,
                        signalStrengthResult.value.level.level
                    )
                    signalStrengthRepository.insert(signalStrengthResult)
                }
            } catch (e: Exception) {
                Log.d(TAG, "catch $e")
            }
        }
    }

    fun cancelTest() {
        Log.d(TAG, "cancellation: the test job is $testJob")
        testJob?.cancel(CancellationException("Shit, cancel this test!!!"))
        Log.d(TAG, "Tests cancelled")
        resetTestResult()
    }

    private fun resetTestResult() {
        _pingResult.value = PingResultState.Error(PingError(PingErrorCase.OK, null))
        _uploadResult.value = ConnectivityTestResult.Result("0.0 Mbit", Color.LightGray)
        _downloadResult.value = ConnectivityTestResult.Result("0.0 Mbit", Color.LightGray)
    }
}

sealed interface MainActivityUiState {
    object Login : MainActivityUiState
    data class Success(val userData: UserData) : MainActivityUiState
}

open class ConnectivityTestResult {
    data class Result (val result: String, val color: Color): ConnectivityTestResult()
    data class Error(val error: String): ConnectivityTestResult()
}

open class PingResultState {
    data class Success(val result: PingResult): PingResultState()
    data class Error(val error: PingError): PingResultState()
}

data class SignalStrengthResult(val dbm: Int, val level: SignalStrengthLevelEnum)
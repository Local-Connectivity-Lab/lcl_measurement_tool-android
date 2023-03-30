package com.lcl.lclmeasurementtool

import android.content.Context
import android.os.Build
import android.telephony.CellSignalStrength
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.protobuf.ByteString
import com.lcl.lclmeasurementtool.Utils.ECDSA
import com.lcl.lclmeasurementtool.Utils.Hex
import com.lcl.lclmeasurementtool.Utils.SecurityUtils
import com.lcl.lclmeasurementtool.constants.NetworkConstants
import com.lcl.lclmeasurementtool.features.iperf.IperfRunner
import com.lcl.lclmeasurementtool.features.iperf.IperfStatus
import com.lcl.lclmeasurementtool.features.mlab.MLabRunner
import com.lcl.lclmeasurementtool.features.mlab.MLabTestStatus
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
import com.lcl.lclmeasurementtool.util.TimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.measurementlab.ndt7.android.NDTTest
import retrofit2.HttpException
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
    var uiState: StateFlow<MainActivityUiState> = userDataRepository.userData.map {
        if (it.loggedIn) MainActivityUiState.Success(it) else MainActivityUiState.Login
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Login,
        started = SharingStarted.WhileSubscribed(5_000)
    )
        private set

    private var _loginState = MutableStateFlow(LoginStatus())
    var loginState = _loginState.asStateFlow()

    // Authentication
    fun login(hPKR: ByteString, skT: ByteString) = viewModelScope.launch {
        userDataRepository.setKeys(hPKR, skT)
    }

    fun logout() = viewModelScope.launch {
        _loginState.value = LoginStatus.Initial
        userDataRepository.logout()
    }

    fun setR(R: ByteString) = viewModelScope.launch {
        userDataRepository.setR(R)
    }

    fun setDeviceId(id: String) = viewModelScope.launch {
        userDataRepository.setDeviceID(id)
    }

    private val getDeviceID = userDataRepository.userData.map { it.deviceID }
    private val getUserPreference = userDataRepository.userData.map { it }

    suspend fun login(result: String) {
        val job = viewModelScope.async {
            val jsonObj: QRCodeKeysModel
            try {
                jsonObj = Json.decodeFromString<QRCodeKeysModel>(result)
            } catch (e: SerializationException) {
                Log.d(TAG, "The QR Code is invalid. Please rescan the code or contact the administrator at lcl@seattlecommunitynetwork.org.")
//                val reasons =
//                    AnalyticsUtils.formatProperties(e.message, Arrays.toString(e.stackTrace))
//                Analytics.trackEvent(AnalyticsUtils.QR_CODE_PARSING_FAILED, reasons)
                _loginState.value = LoginStatus.RegistrationFailed("QRCodeParseFailed")
                return@async
            }

            Log.d(TAG, "the scanner result is $result")

            val sigma_t = jsonObj.sigmaT
            val sk_t = jsonObj.skT
            val pk_a = jsonObj.pk_a

            when (val validationResult = validate(sigmaT = sigma_t, pkA = pk_a, skT = sk_t)) {
                is ScanStatus.KeyVerificationFailed -> {
                    Log.d(TAG, "KeyVerificationFailed")
                    _loginState.value = LoginStatus.RegistrationFailed("KeyVerificationFailed")
                    return@async
                }

                is ScanStatus.KeyVerificationException -> {
                    Log.d(TAG, "KeyVerificationException")
                    _loginState.value = LoginStatus.RegistrationFailed(validationResult.exception.toString())
                    return@async
                }

                is ScanStatus.ScanSuccess -> {
                    val skTHex = validationResult.skTHex
                    val pk_t: ECPublicKey
                    val ecPrivateKey: ECPrivateKey
                    try {
                        ecPrivateKey = ECDSA.DeserializePrivateKey(skTHex)
                        pk_t = ECDSA.DerivePublicKey(ecPrivateKey)
                    } catch (e: Exception) {
                        Log.d(TAG, "KeyGenerationFailed")
                        _loginState.value = LoginStatus.RegistrationFailed("KeyGenerationFailed")
                        return@async
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
                        Log.d(TAG, "KeySignFailed")
                        _loginState.value = LoginStatus.RegistrationFailed("KeySignFailed")
                        return@async
                    }

                    val registration = Json.encodeToString(
                        RegistrationModel(
                        Hex.encodeHexString(sigma_r, false),
                        Hex.encodeHexString(h_concat, false),
                        Hex.encodeHexString(r, false)
                        )
                    )

                    try {
                        val response = networkApi.register(registration)
                        Log.d(TAG, "response is ${response.isSuccessful}")
                        if (response.isSuccessful) {
                            login(ByteString.copyFrom(h_pkr), ByteString.copyFrom(Hex.decodeHex(sk_t)))
                            _loginState.value = LoginStatus.RegistrationSucceeded
                            return@async
                        }

                        Log.d(TAG, "response registration failed. error: ${response.message()}")
                        _loginState.value = LoginStatus.RegistrationFailed(response.message())
                        return@async
                    } catch (e: HttpException) {
                        Log.d(TAG, "error occurred during registration. error is $e")
                        _loginState.value = LoginStatus.RegistrationFailed(e.message())
                    }

                    return@async
                }
                else -> {
                    Log.d(TAG, "UnexpectedErrorOccurred")
                    _loginState.value = LoginStatus.RegistrationFailed("UnexpectedErrorOccurred")
                    return@async
                }
            }
        }
        return job.await()
    }

    private fun validate(sigmaT: String, pkA: String, skT: String): ScanStatus {
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
                return ScanStatus.KeyVerificationFailed
            }
        } catch (e: Exception) {
            return ScanStatus.KeyVerificationException(e)
        }

        return ScanStatus.ScanSuccess(sigmaTHex, pkAHex, skTHex)
    }


    // Network Testing
    private var _pingResult = MutableStateFlow(PingResultState())
    private var _downloadResult = MutableStateFlow(ConnectivityTestResult())
    private var _uploadResult = MutableStateFlow(ConnectivityTestResult())
    private val _isIperfTestActive = MutableStateFlow(false)
    private val _isMLabTestActive = MutableStateFlow(false)

    private var _mLabPingResult = MutableStateFlow(PingResultState())
    private var _mLabUploadResult = MutableStateFlow(ConnectivityTestResult())
    private var _mLabDownloadResult = MutableStateFlow(ConnectivityTestResult())

    var mLabPingResult = _mLabPingResult.asStateFlow()
    var mlabUploadResult = _mLabUploadResult.asStateFlow()
    var mlabDownloadResult = _mLabDownloadResult.asStateFlow()


    val isIperfTestActive = _isIperfTestActive.asStateFlow()
    val isMLabTestActive = _isMLabTestActive.asStateFlow()

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
                    _isIperfTestActive.value = true
                }
                .onCompletion {
                    if (it != null) {
                        Log.d(TAG, "Error is ${it.message}")
                        _isIperfTestActive.value = false
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

    private suspend fun runMLabPing() {
        try {
            Ping.cancellableStart(address = NetworkConstants.PING_TEST_ADDRESS, timeout = 1000)
                .onStart {
                    Log.d(TAG, "isActive = true")
                    _isMLabTestActive.value = true
                }
                .onCompletion {
                    if (it != null) {
                        Log.d(TAG, "Error is ${it.message}")
                        _isMLabTestActive.value = false
                    }
                }
                .collect {
                    _mLabPingResult.value = when(it.error.code) {
                        PingErrorCase.OK ->  PingResultState.Success(it)
                        else ->  {
                            _isMLabTestActive.value = false
                            PingResultState.Error(it.error)
                        }
                    }
                }
        } catch (e: IllegalArgumentException) {
            _mLabPingResult.value = PingResultState.Error(PingError(PingErrorCase.OTHER, e.message))
            Log.e(TAG, "Ping Config error")
        }
    }

    private suspend fun getUploadResult(context: Context) {
        IperfRunner().getTestResult(IperfRunner.iperfUploadConfig, context.cacheDir)
            .onStart {
                _isIperfTestActive.value = true
            }
            .onCompletion {
                if (it != null) {
                    // save to DB and send over the network
                    Log.d(TAG, "Error is ${it.message}")
                    _isIperfTestActive.value = false
                }
            }
            .collectLatest { result ->
                _uploadResult.value = when(result.status) {
                IperfStatus.RUNNING -> ConnectivityTestResult.Result(result.bandWidth, Color.LightGray)
                IperfStatus.FINISHED -> ConnectivityTestResult.Result(result.bandWidth, Color.Black)
                IperfStatus.ERROR -> {
                    _isIperfTestActive.value = false
                    ConnectivityTestResult.Error(result.errorMsg!!)
                }
            }
        }
    }


    private suspend fun getDownloadResult(context: Context) {
        IperfRunner().getTestResult(IperfRunner.iperfDownloadConfig, context.cacheDir)
            .onStart {
                _isIperfTestActive.value = true
            }
            .onCompletion {
                if (it != null) {
                    // save to DB and send over the network
                    Log.d(TAG, "Error is ${it.message}")
                    _isIperfTestActive.value = false
                }
            }
            .collectLatest { result ->
                _downloadResult.value = when(result.status) {
                IperfStatus.RUNNING -> ConnectivityTestResult.Result(result.bandWidth, Color.LightGray)
                IperfStatus.FINISHED -> ConnectivityTestResult.Result(result.bandWidth, Color.Black)
                IperfStatus.ERROR -> {
                    _isIperfTestActive.value = false
                    ConnectivityTestResult.Error(result.errorMsg!!)
                }
            }
        }
    }

    private var testJob: Job? = null
    private var mlabTestJob: Job? = null

    fun runIperfTest(context: Context) {
        if (testJob?.isActive == true) {
            testJob?.cancel()
        }

        testJob = viewModelScope.launch {
            resetIperfTestResult()

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

                _isIperfTestActive.value = false
                Log.d(TAG, "ping, upload, download are finished")
                val curTime = TimeUtil.getCurrentTime()
                val cellID = signalStrengthMonitor.getCellID()

                locationService.lastLocation().combine(getUserPreference) { locaion, userPreference ->
                    Pair(locaion, userPreference)
                }.collect {
//                    Log.d(TAG, it.first.toString())
//                    Log.d(TAG, it.second)
                    val connectivityResult = ConnectivityReportModel(
                        it.first.latitude,
                        it.first.longitude,
                        curTime,
                        cellID,
                        it.second.deviceID,
                        (_uploadResult.value as ConnectivityTestResult.Result).result.toDouble(),
                        (_downloadResult.value as ConnectivityTestResult.Result).result.toDouble(),
                        (_pingResult.value as PingResultState.Success).result.avg!!.toDouble(),
                        (_pingResult.value as PingResultState.Success).result.numLoss!!.toDouble(),
                    )

                    connectivityRepository.insert(connectivityResult)
                    val connectivityReport = prepareReportData(connectivityResult, it.second)

                    val signalStrengthResult = SignalStrengthReportModel(
                        it.first.latitude,
                        it.first.longitude,
                        curTime,
                        cellID,
                        it.second.deviceID,
                        signalStrengthResult.value.dbm,
                        signalStrengthResult.value.level.level
                    )
                    signalStrengthRepository.insert(signalStrengthResult)
                    val signalReport = prepareReportData(signalStrengthResult, it.second)

                    try {
                        val response = networkApi.uploadSignalStrength(signalReport)
                        Log.d(TAG, "response from signal strength endpoint is ${response.message()}")
                    } catch (e: HttpException) {
                        Log.e(TAG, "trying to send signal strength report, but got $e")
                    }

                    try {
                        val response = networkApi.uploadConnectivity(connectivityReport)
                        Log.d(TAG, "response from connectivity endpoint is ${response.message()}")
                    } catch (e: HttpException) {
                        Log.e(TAG, "trying to send connectivity report, but got $e")
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "catch $e")
            }
        }
    }

    fun cancelIperfTest() {
        Log.d(TAG, "cancellation: the test job is $testJob")
        testJob?.cancel(CancellationException("Shit, cancel this test!!!"))
        Log.d(TAG, "Tests cancelled")
        resetIperfTestResult()
    }

    fun cancelMLabTest() {
        Log.d(TAG, "cancellation: the test job is $mlabTestJob")
        mlabTestJob?.cancel(CancellationException("Shit, cancel this test!!!"))
        Log.d(TAG, "Tests cancelled")
        resetMLabTestResult()
    }

    private suspend fun getMLabTestResult() {
        try {
            MLabRunner.runTest(NDTTest.TestType.DOWNLOAD_AND_UPLOAD)
                .onStart {
                    _isMLabTestActive.value = true
                }
                .onCompletion {
                    if (it != null) {
                        Log.d(TAG, "Error is ${it.message}")
                        _isMLabTestActive.value = false
                    }
                }
                .collect{
                    when(it.type) {
                        NDTTest.TestType.UPLOAD -> {
                            _mLabUploadResult.value = when(it.status) {
                                MLabTestStatus.RUNNING -> { ConnectivityTestResult.Result(it.speed!!, Color.LightGray) }

                                MLabTestStatus.FINISHED -> { ConnectivityTestResult.Result(it.speed!!, Color.Black) }

                                MLabTestStatus.ERROR -> {
                                    _isMLabTestActive.value = false
                                    ConnectivityTestResult.Error(it.errorMsg!!)
                                }
                            }
                        }
                        NDTTest.TestType.DOWNLOAD -> {
                            _mLabDownloadResult.value = when(it.status) {
                                MLabTestStatus.RUNNING -> { ConnectivityTestResult.Result(it.speed!!, Color.LightGray) }

                                MLabTestStatus.FINISHED -> { ConnectivityTestResult.Result(it.speed!!, Color.Black) }

                                MLabTestStatus.ERROR -> {
                                    _isMLabTestActive.value = false
                                    ConnectivityTestResult.Error(it.errorMsg!!)
                                }
                            }
                        }
                        else -> { }
                    }
                }
        } catch (e: Exception) {
            Log.d(TAG, "catch $e")
        }
    }

    fun runMLabTest() {

        if (mlabTestJob?.isActive == true) {
            mlabTestJob?.cancel()
        }

        mlabTestJob = viewModelScope.launch {
            try {
                resetMLabTestResult()

                runMLabPing()
                if (_mLabPingResult.value is PingResultState.Error) {
                    this.cancel("Ping Test Failed")
                }
                ensureActive()

                getMLabTestResult()
                if (_mLabUploadResult.value is ConnectivityTestResult.Error || _mLabDownloadResult.value is ConnectivityTestResult.Error) {
                    Log.d(TAG, "mlab test job is cancelled")
                    this.cancel("MLab test failed")
                }
                if (isActive) {
                    Log.d(TAG, "mlab test job is still active")
                } else {
                    Log.d(TAG, "mlab test job is completed")
                }

                ensureActive()

                _isMLabTestActive.value = false
                Log.d(TAG, "ping, upload, download are finished. isMLabTestActive.value=${isMLabTestActive.value}")
                val curTime = TimeUtil.getCurrentTime()
                val cellID = signalStrengthMonitor.getCellID()
            } catch (e: Exception) {
                Log.d(TAG, "catch $e")
            }
        }
    }

    private fun saveToDB() {

    }

    private fun report() {

    }

    private fun prepareReportData(measureDataModel: BaseMeasureDataModel, userData: UserData): String {
        val serialized = Json.encodeToString(measureDataModel).toByteArray()
        val sig_m = ECDSA.Sign(serialized, ECDSA.DeserializePrivateKey(userData.skT.toByteArray()))
        val report = MeasurementReportModel(sig_m.toString(), userData.hPKR.toStringUtf8(), serialized.toString(), userData.showData)
        return Json.encodeToString(report)
    }

    private fun resetIperfTestResult() {
        _pingResult.value = PingResultState.Error(PingError(PingErrorCase.OK, null))
        _uploadResult.value = ConnectivityTestResult.Result("0.0", Color.LightGray)
        _downloadResult.value = ConnectivityTestResult.Result("0.0", Color.LightGray)
    }

    private fun resetMLabTestResult() {
        _mLabPingResult.value = PingResultState.Error(PingError(PingErrorCase.OK, null))
        _mLabUploadResult.value = ConnectivityTestResult.Result("0.0", Color.LightGray)
        _mLabDownloadResult.value = ConnectivityTestResult.Result("0.0", Color.LightGray)
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
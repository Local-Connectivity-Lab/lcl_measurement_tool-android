package com.lcl.lclmeasurementtool

import android.os.Build
import android.telephony.CellSignalStrength
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.protobuf.ByteString
import com.lcl.lclmeasurementtool.constants.NetworkConstants
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
import com.lcl.lclmeasurementtool.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.measurementlab.ndt7.android.NDTTest
import okhttp3.ResponseBody
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

    // Network Testing
    private val _isMLabTestActive = MutableStateFlow(false)

    private var _mLabPingResult = MutableStateFlow(PingResultState())
    private var _mLabUploadResult = MutableStateFlow(ConnectivityTestResult())
    private var _mLabDownloadResult = MutableStateFlow(ConnectivityTestResult())

    var mLabPingResult = _mLabPingResult.asStateFlow()
    var mlabUploadResult = _mLabUploadResult.asStateFlow()
    var mlabDownloadResult = _mLabDownloadResult.asStateFlow()
    val isMLabTestActive = _isMLabTestActive.asStateFlow()
    private var mlabTestJob: Job? = null

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
                            h_pkr = SecurityUtil.digest(byteArray.toByteArray(), SecurityUtil.SHA_256_HASH)
                            byteArray.reset()
                            byteArray.write(skTHex)
                            byteArray.write(pk_t.encoded)
                            h_sec = SecurityUtil.digest(byteArray.toByteArray(), SecurityUtil.SHA_256_HASH)
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
                        networkApi.register(registration)
                        login(ByteString.copyFrom(h_pkr), ByteString.copyFrom(Hex.decodeHex(sk_t)))
                        _loginState.value = LoginStatus.RegistrationSucceeded
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

                // add data to db + report to remote server
                locationService.lastLocation().combine(userDataRepository.userData) { location, userPreference ->
                    Pair(location, userPreference)
                }.collect {
                    val signalStrengthReportModel = SignalStrengthReportModel(
                        it.first.latitude,
                        it.first.longitude,
                        curTime,
                        cellID,
                        it.second.deviceID,
                        signalStrengthResult.value.dbm,
                        signalStrengthResult.value.level.level
                    )

                    val connectivityReportModel = ConnectivityReportModel(
                        it.first.latitude,
                        it.first.longitude,
                        curTime,
                        cellID,
                        it.second.deviceID,
                        (_mLabUploadResult.value as ConnectivityTestResult.Result).result.toDouble(),
                        (_mLabDownloadResult.value as ConnectivityTestResult.Result).result.toDouble(),
                        (_mLabPingResult.value as PingResultState.Success).result.avg!!.toDouble(),
                        (_mLabPingResult.value as PingResultState.Success).result.numLoss!!.toDouble(),
                    )

                    saveToDB(signalStrengthReportModel, connectivityReportModel)

                    report(signalStrengthReportModel, it.second)
                    report(connectivityReportModel, it.second)
                }
            } catch (e: Exception) {
                Log.d(TAG, "catch $e")
            }
        }
    }

    private suspend fun saveToDB(signalStrengthReportModel: SignalStrengthReportModel, connectivityReportModel: ConnectivityReportModel) {
        signalStrengthRepository.insert(signalStrengthReportModel)
        connectivityRepository.insert(connectivityReportModel)
    }

    private suspend fun report(reportModel: BaseMeasureDataModel, userData: UserData) {
        try {
            val reportString = prepareReportData(reportModel, userData)
            val response: ResponseBody = if (reportModel is SignalStrengthReportModel) {
                networkApi.uploadSignalStrength(reportString)
            } else {
                networkApi.uploadConnectivity(reportString)
            }

            Log.i(TAG, "report success! response is $response")

            // update DB to reflect the change
            if (reportModel is SignalStrengthReportModel) {
                signalStrengthRepository.update(reportModel.copy(reported = true))
            } else if (reportModel is ConnectivityReportModel) {
                connectivityRepository.update(reportModel.copy(reported = true))
            }
            return

        } catch (e: HttpException) {
            // TODO: retry
            if (e.code() in 400..499) {
                Log.d(TAG, "client error")
            }

            if (e.code() in 500..599) {
                Log.d(TAG, "server error")
            }
        } catch (e: Exception) {
            Log.d(TAG, "unknown exception occurred when uploading data. $e")
        }
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
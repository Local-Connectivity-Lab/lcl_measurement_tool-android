package com.lcl.lclmeasurementtool

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.protobuf.ByteString
import com.jsoniter.JsonIterator
import com.jsoniter.spi.JsonException
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lcl.lclmeasurementtool.Utils.ECDSA
import com.lcl.lclmeasurementtool.Utils.Hex
import com.lcl.lclmeasurementtool.Utils.SecurityUtils
import com.lcl.lclmeasurementtool.model.datamodel.QRCodeKeysModel
import com.lcl.lclmeasurementtool.model.datamodel.RegistrationModel
import com.lcl.lclmeasurementtool.model.datamodel.UserData
import com.lcl.lclmeasurementtool.model.repository.NetworkApiRepository
import com.lcl.lclmeasurementtool.model.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val networkApi: NetworkApiRepository,
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = userDataRepository.userData.map {
        if (it.loggedIn) MainActivityUiState.Success(it) else MainActivityUiState.Login
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Login,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    fun login(hPKR: ByteString, skT: ByteString) = viewModelScope.launch {
        userDataRepository.setKeys(hPKR, skT)
    }

    fun setR(R: ByteString) = viewModelScope.launch {
        userDataRepository.setR(R)
    }

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
                Log.d("MainViewModel", "The QR Code is invalid. Please rescan the code or contact the administrator at lcl@seattlecommunitynetwork.org.")
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
}

sealed interface MainActivityUiState {
    object Login : MainActivityUiState
    data class Success(val userData: UserData) : MainActivityUiState
}
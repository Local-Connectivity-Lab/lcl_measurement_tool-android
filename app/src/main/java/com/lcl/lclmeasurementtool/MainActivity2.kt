package com.lcl.lclmeasurementtool

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import androidx.viewbinding.BuildConfig
import com.azhon.appupdate.manager.DownloadManager
import com.azhon.appupdate.util.ApkUtil
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.MessageDialog
import com.lcl.lclmeasurementtool.datasource.APKAutoUpdaterDataSource
import com.lcl.lclmeasurementtool.networking.APKAutoUpdater
import com.lcl.lclmeasurementtool.networking.NetworkMonitor
import com.lcl.lclmeasurementtool.networking.SimStateMonitor
import com.lcl.lclmeasurementtool.ui.LCLApp
import com.lcl.lclmeasurementtool.ui.Login
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity2 : ComponentActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var simStateMonitor: SimStateMonitor

    @Inject
    lateinit var autoUpdater: APKAutoUpdater

    private val viewModel: MainActivityViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (!hasPermission()) {
            XXPermissions.with(this)
                .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE, Permission.ACCESS_FINE_LOCATION)
                .request { _, allGranted ->
                    run {
                        if (!allGranted) {

                            // Permission denied.

                            // Notify the user via a dialog that they have rejected a core permission for the
                            // app, which makes the Activity useless.
                            MessageDialog.build()
                                .setTitle(R.string.location_message_title)
                                .setMessage(R.string.permission_denied_explanation)
                                .setOkButton(R.string.settings) { baseDialog, v ->
                                    // Build intent that displays the App settings screen.
                                    val intent = Intent()
                                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    val uri = Uri.fromParts(
                                        "package",
                                        BuildConfig.LIBRARY_PACKAGE_NAME, null
                                    )
                                    intent.data = uri
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    false
                                }.setOkButton(android.R.string.cancel).show()
                        }
                    }
                }
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

        Log.d(TAG, "uiState is ${uiState is MainActivityUiState.Login}")

        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            if (com.lcl.lclmeasurementtool.BuildConfig.FLAVOR != "dev") {
                if (uiState == MainActivityUiState.Login) {
                    viewModel.setDeviceId(UUID.randomUUID().toString())
                    Login(viewModel = viewModel)
                    return@setContent
                }
            }
            LCLApp(windowSizeClass = calculateWindowSizeClass(activity = this), networkMonitor, simStateMonitor)
        }

        ApkUtil.deleteOldApk(this, "${externalCacheDir?.path}/${APKAutoUpdaterDataSource.APKNAME}")
        val downloader = DownloadManager.Builder(this)
        lifecycleScope.launch {
            val updateInfo = autoUpdater.canUpdate() ?: return@launch
            Log.d(TAG, "can update")
            val asset = updateInfo.release.assets.firstOrNull { it.name == APKAutoUpdaterDataSource.APKNAME } ?: return@launch
            asset.let {
                val apkName = it.name
                val apkVersion = updateInfo.release.tagName
                val apkURL = it.browserDownloadUrl
                val apkDownloader = downloader.run {
                    smallIcon(R.mipmap.icon)
                    apkUrl(apkURL)
                    apkName(apkName)
                    showNotification(true)
                    showNewerToast(true)
                    enableLog(true)
                    apkVersionName(apkVersion)
                    forcedUpgrade(updateInfo.shouldForceUpdate)
                    build()
                }

                apkDownloader.download()
            }
        }
    }

    private fun hasPermission(): Boolean {
        return XXPermissions.isGranted(
            this,
            Permission.CAMERA,
            Permission.READ_EXTERNAL_STORAGE,
            Permission.ACCESS_FINE_LOCATION
        )
    }
}

sealed interface ScanStatus {
    data class ScanSuccess(val sigmaTHex: ByteArray, val pkAHex: ByteArray, val skTHex: ByteArray): ScanStatus {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ScanSuccess

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

    object KeyVerificationFailed: ScanStatus
    data class KeyVerificationException(val exception: Exception) : ScanStatus
}

open class LoginStatus {
    object Initial: LoginStatus()
    data class RegistrationFailed(val reason: String): LoginStatus()
    object RegistrationSucceeded: LoginStatus()
}
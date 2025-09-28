package com.lcl.lclmeasurementtool.model.viewmodels

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lcl.lclmeasurementtool.model.repository.ConnectivityRepository
import com.lcl.lclmeasurementtool.model.repository.SignalStrengthRepository
import com.lcl.lclmeasurementtool.model.repository.UserDataRepository
import com.lcl.lclmeasurementtool.util.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val signalStrengthRepository: SignalStrengthRepository,
    private val connectivityRepository: ConnectivityRepository,
    application: Application
) : AndroidViewModel(application) {

    val shouldShowData: StateFlow<Boolean> = userDataRepository
        .userData
        .map { it.showData }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    private var _exportedFileUri: Uri? = null
    val exportedFileUri get() = _exportedFileUri

    fun toggleShowData(showData: Boolean) {
        viewModelScope.launch {
            userDataRepository.toggleShowData(showData = showData)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userDataRepository.logout()
        }
    }

    /**
     * Unified export function for signal strength or connectivity data
     */
    fun exportData(type: ExportType, onComplete: (Uri?) -> Unit) {
        viewModelScope.launch {
            try {
                val uri: Uri?
                when (type) {
                    ExportType.SIGNAL -> {
                        val signalData = signalStrengthRepository.getAll().first()
                        if (signalData.isEmpty()) {
                            showToast("No signal strength data to export")
                            onComplete(null)
                            return@launch
                        }
                        uri = CsvExporter.exportSignalStrengthToCsv(getApplication(), signalData)
                        if (uri != null) {
                            showToast("Signal strength data exported successfully")
                            shareFile(uri, "Signal Strength Data", "text/csv")
                        } else {
                            showToast("Failed to export signal strength data")
                        }
                    }
                    ExportType.CONNECTIVITY -> {
                        val connectivityData = connectivityRepository.getAll().first()
                        if (connectivityData.isEmpty()) {
                            showToast("No connectivity data to export")
                            onComplete(null)
                            return@launch
                        }
                        uri = CsvExporter.exportConnectivityToCsv(getApplication(), connectivityData)
                        if (uri != null) {
                            showToast("Connectivity data exported successfully")
                            shareFile(uri, "Speed Test Data", "text/csv")
                        } else {
                            showToast("Failed to export connectivity data")
                        }
                    }
                }
                _exportedFileUri = uri
                onComplete(uri)
            } catch (e: Exception) {
                showToast("Error exporting data: ${e.message}")
                onComplete(null)
            }
        }
    }

    private fun shareFile(uri: Uri, subject: String, mimeType: String) {
        val context = getApplication<Application>()
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share $subject")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }

    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }
}

enum class ExportType {
    SIGNAL,
    CONNECTIVITY
}

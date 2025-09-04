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

    // Holds the URI of the exported file after a successful export
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
     * Export signal strength data to a CSV file
     */
    fun exportSignalStrengthData(onComplete: (Uri?) -> Unit) {
        viewModelScope.launch {
            try {
                val signalData = signalStrengthRepository.getAll().first()
                if (signalData.isEmpty()) {
                    showToast("No signal strength data to export")
                    onComplete(null)
                    return@launch
                }

                val uri = CsvExporter.exportSignalStrengthToCsv(getApplication(), signalData)
                _exportedFileUri = uri
                
                if (uri != null) {
                    showToast("Signal strength data exported successfully")
                    shareFile(uri, "Signal Strength Data", "text/csv")
                } else {
                    showToast("Failed to export signal strength data")
                }
                
                onComplete(uri)
            } catch (e: Exception) {
                showToast("Error exporting signal strength data: ${e.message}")
                onComplete(null)
            }
        }
    }

    /**
     * Export connectivity data to a CSV file
     */
    fun exportConnectivityData(onComplete: (Uri?) -> Unit) {
        viewModelScope.launch {
            try {
                val connectivityData = connectivityRepository.getAll().first()
                if (connectivityData.isEmpty()) {
                    showToast("No connectivity data to export")
                    onComplete(null)
                    return@launch
                }
                
                val uri = CsvExporter.exportConnectivityToCsv(getApplication(), connectivityData)
                _exportedFileUri = uri
                
                if (uri != null) {
                    showToast("Connectivity data exported successfully")
                    shareFile(uri, "Speed Test Data", "text/csv")
                } else {
                    showToast("Failed to export connectivity data")
                }
                
                onComplete(uri)
            } catch (e: Exception) {
                showToast("Error exporting connectivity data: ${e.message}")
                onComplete(null)
            }
        }
    }
    
    /**
     * Share a file using Android's share functionality
     */
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
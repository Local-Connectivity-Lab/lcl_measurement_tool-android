package com.lcl.lclmeasurementtool.model.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import com.lcl.lclmeasurementtool.model.repository.SignalStrengthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignalStrengthViewModel @Inject constructor(
    private val measurementRepository: SignalStrengthRepository
): ViewModel() {

    fun insert(data: SignalStrengthReportModel) {
        viewModelScope.launch {
            measurementRepository.insert(data)
        }
    }

    val dataFlow: StateFlow<SignalStrengthUiState> =
        measurementRepository
            .getAll()
            .map(SignalStrengthUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SignalStrengthUiState.Loading
            )
//    private val repository = MeasurementsRepository(AppDatabase.getDatabase(application))
//    val signalStrengthData = repository.signalStrengthData
//
//    fun insert(data: SignalStrengthReportModel) {
//        viewModelScope.launch {
//            repository.insertSignalStrengthData(data)
//        }
//    }
//
//    companion object {
//        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
//            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
//                if (modelClass.isAssignableFrom(SignalStrengthViewModel::class.java)) {
//                    val application = checkNotNull(extras[APPLICATION_KEY])
//
//                    @Suppress("UNCHECKED_CAST")
//                    return SignalStrengthViewModel(application) as T
//                }
//                throw IllegalArgumentException("Unknown ViewModel class")
//            }
//        }
//    }
}

sealed interface SignalStrengthUiState {
    data class Success(val signalStrengths: List<SignalStrengthReportModel>) : SignalStrengthUiState
    object Error : SignalStrengthUiState
    object Loading : SignalStrengthUiState
}
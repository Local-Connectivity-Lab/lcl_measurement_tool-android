package com.lcl.lclmeasurementtool.model.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import com.lcl.lclmeasurementtool.model.repository.ConnectivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    private val measurementsRepository: ConnectivityRepository
): ViewModel() {
    fun insert(data: ConnectivityReportModel) {
        viewModelScope.launch {
            measurementsRepository.insert(data)
        }
    }

    val dataFlow: StateFlow<ConnectivityUiState> =
        measurementsRepository
            .getAll()
            .map(ConnectivityUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ConnectivityUiState.Loading
            )
}


sealed interface ConnectivityUiState {
    data class Success(val connectivities: List<ConnectivityReportModel>) : ConnectivityUiState
    object Error : ConnectivityUiState
    object Loading : ConnectivityUiState
}
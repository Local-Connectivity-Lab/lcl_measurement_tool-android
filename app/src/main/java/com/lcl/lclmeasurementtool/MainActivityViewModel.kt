package com.lcl.lclmeasurementtool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.protobuf.ByteString
import com.lcl.lclmeasurementtool.model.datamodel.UserData
import com.lcl.lclmeasurementtool.model.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val userDataRepository: UserDataRepository
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
}

sealed interface MainActivityUiState {
    object Login : MainActivityUiState
    data class Success(val userData: UserData) : MainActivityUiState
}
package com.lcl.lclmeasurementtool.model.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lcl.lclmeasurementtool.model.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    val shouldShowData: StateFlow<Boolean> = userDataRepository
        .userData
        .map { it.showData }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )


    fun toggleShowData(showData: Boolean) {
        viewModelScope.launch {
            userDataRepository.toggleShowData(showData = showData)
        }
    }

    fun logout()  {
        viewModelScope.launch {
            userDataRepository.logout()
        }
    }

}
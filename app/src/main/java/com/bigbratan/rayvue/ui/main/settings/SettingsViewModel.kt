package com.bigbratan.rayvue.ui.main.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.services.AuthService
import com.bigbratan.rayvue.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authService: AuthService,
    private val userService: UserService,
) : ViewModel() {
    val obtainedUserState =
        MutableStateFlow<ObtainedUserState>(ObtainedUserState.Loading)
    val sentLogOutDataState = MutableStateFlow<SentLogOutDataState>(SentLogOutDataState.Idle)
    val sentSignOutDataState = MutableStateFlow<SentSignOutDataState>(SentSignOutDataState.Idle)
    val areLoginSignupVisible = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            areLoginSignupVisible.value = userService.user.value?.id == null
        }
    }

    fun getData() {
        viewModelScope.launch {
            userService.user.collect { user ->
                try {
                    val userName = user?.userName

                    obtainedUserState.value = ObtainedUserState.Success(userName)
                } catch (e: Exception) {
                    obtainedUserState.value = ObtainedUserState.Error
                }
            }
        }
    }

    fun exitAccount() {
        viewModelScope.launch {
            sentLogOutDataState.value = SentLogOutDataState.Loading

            try {
                authService.exitAccount()

                sentLogOutDataState.value = SentLogOutDataState.Success
            } catch (e: Exception) {
                sentLogOutDataState.value = SentLogOutDataState.Error
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            sentSignOutDataState.value = SentSignOutDataState.Loading

            try {
                authService.deleteAccount()

                sentSignOutDataState.value = SentSignOutDataState.Success
            } catch (e: Exception) {
                sentSignOutDataState.value = SentSignOutDataState.Error
            }
        }
    }

    fun resetStates() {
        sentLogOutDataState.value = SentLogOutDataState.Idle
        sentSignOutDataState.value = SentSignOutDataState.Idle
    }
}

sealed class SentLogOutDataState {
    object Idle : SentLogOutDataState()

    object Loading : SentLogOutDataState()

    object Success : SentLogOutDataState()

    object Error : SentLogOutDataState()
}

sealed class SentSignOutDataState {
    object Idle : SentSignOutDataState()

    object Loading : SentSignOutDataState()

    object Success : SentSignOutDataState()

    object Error : SentSignOutDataState()
}

sealed class ObtainedUserState {
    object Loading : ObtainedUserState()

    data class Success(
        val userName: String?,
    ) : ObtainedUserState()

    object Error : ObtainedUserState()
}
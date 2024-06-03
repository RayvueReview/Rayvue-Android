package com.bigbratan.rayvue.ui.auth.inputName

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.services.AccountService
import com.bigbratan.rayvue.services.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InputNameViewModel @Inject constructor(
    private val authService: AuthService,
    private val accountService: AccountService,
) : ViewModel() {
    val sentSignupDataState = MutableStateFlow<SentSignupDataState>(SentSignupDataState.Idle)

    fun addName(
        userName: String,
    ) {
        viewModelScope.launch {
            accountService.addName(
                userName = userName,
            )
        }
    }

    fun removeAllData() {
        viewModelScope.launch {
            accountService.removeAllData()
        }
    }

    fun createAccount() {
        viewModelScope.launch {
            sentSignupDataState.value = SentSignupDataState.Loading

            try {
                authService.createAccount()

                sentSignupDataState.value = SentSignupDataState.Success
            } catch (e: Exception) {
                sentSignupDataState.value = SentSignupDataState.Error
            }
        }
    }

    fun resetState() {
        sentSignupDataState.value = SentSignupDataState.Idle
    }
}

sealed class SentSignupDataState {
    object Idle : SentSignupDataState()

    object Loading : SentSignupDataState()

    object Success : SentSignupDataState()

    object Error : SentSignupDataState()
}
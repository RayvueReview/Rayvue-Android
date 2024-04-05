package com.bigbratan.rayvue.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.services.AuthService
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
) : ViewModel() {
    val sentLoginDataState = MutableStateFlow<SentLoginDataState>(SentLoginDataState.Idle)

    fun enterAccount(
        email: String,
        password: String,
    ) {
        viewModelScope.launch {
            sentLoginDataState.value = SentLoginDataState.Loading

            try {
                authService.enterAccount(email, password)

                sentLoginDataState.value = SentLoginDataState.Success
            } catch (e: Exception) {
                when (e) {
                    is FirebaseAuthInvalidUserException -> {
                        sentLoginDataState.value = SentLoginDataState.EmailBad
                    }

                    is FirebaseAuthInvalidCredentialsException -> {
                        sentLoginDataState.value = SentLoginDataState.PasswordBad
                    }

                    else -> {
                        sentLoginDataState.value = SentLoginDataState.Error
                    }
                }
            }
        }
    }

    fun resetState() {
        sentLoginDataState.value = SentLoginDataState.Idle
    }
}

sealed class SentLoginDataState {
    object Idle : SentLoginDataState()

    object Loading : SentLoginDataState()

    object Success : SentLoginDataState()

    object EmailBad : SentLoginDataState()

    object PasswordBad : SentLoginDataState()

    object Error : SentLoginDataState()
}
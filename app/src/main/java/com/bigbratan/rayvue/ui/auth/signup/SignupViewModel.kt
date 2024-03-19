package com.bigbratan.rayvue.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.services.AccountService
import com.bigbratan.rayvue.services.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authService: AuthService,
    private val accountService: AccountService,
) : ViewModel() {
    val validatedAuthDataState = MutableStateFlow<ValidatedAuthDataState>(ValidatedAuthDataState.Idle)

    fun addAuthData(
        email: String,
        password: String,
    ) {
        viewModelScope.launch {
            accountService.addAuthData(
                email = email,
                password = password,
            )
        }
    }

    fun checkEmail(
        email: String,
    ) {
        viewModelScope.launch {
            validatedAuthDataState.value = ValidatedAuthDataState.Loading

            try {
                val emailExists = authService.checkEmail(email = email)

                if (!emailExists)
                    validatedAuthDataState.value = ValidatedAuthDataState.True
                else validatedAuthDataState.value = ValidatedAuthDataState.False
            } catch (e: Exception) {
                validatedAuthDataState.value = ValidatedAuthDataState.Error
            }
        }
    }

    fun removeAllData() {
        viewModelScope.launch {
            accountService.removeAllData()
        }
    }

    fun resetState() {
        validatedAuthDataState.value = ValidatedAuthDataState.Idle
    }
}

sealed class ValidatedAuthDataState {
    object Idle : ValidatedAuthDataState()

    object Loading : ValidatedAuthDataState()

    object True : ValidatedAuthDataState()

    object False: ValidatedAuthDataState()

    object Error : ValidatedAuthDataState()
}
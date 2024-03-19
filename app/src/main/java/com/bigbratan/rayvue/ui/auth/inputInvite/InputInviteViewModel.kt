package com.bigbratan.rayvue.ui.auth.inputInvite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.services.AccountService
import com.bigbratan.rayvue.services.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InputInviteViewModel @Inject constructor(
    private val authService: AuthService,
    private val accountService: AccountService,
) : ViewModel() {
    val sentSignupDataState = MutableStateFlow<SentSignupDataState>(SentSignupDataState.Idle)
    val validatedCodeState = MutableStateFlow<ValidatedCodeState>(ValidatedCodeState.Idle)

    fun createAccount(
        isReviewer: Boolean,
    ) {
        viewModelScope.launch {
            sentSignupDataState.value = SentSignupDataState.Loading

            try {
                authService.createAccount(isReviewer)

                sentSignupDataState.value = SentSignupDataState.Success
            } catch (e: Exception) {
                sentSignupDataState.value = SentSignupDataState.Error
            }
        }
    }

    fun checkCode(
        inviteCode: Int,
    ) {
        viewModelScope.launch {
            validatedCodeState.value = ValidatedCodeState.Loading

            try {
                val isCodeCorrect = authService.checkCode(inviteCode)

                if (isCodeCorrect)
                    validatedCodeState.value = ValidatedCodeState.True
                else validatedCodeState.value = ValidatedCodeState.False
            } catch (e: Exception) {
                validatedCodeState.value = ValidatedCodeState.Error
            }
        }
    }

    fun removeName() {
        viewModelScope.launch {
            accountService.removeName()
        }
    }

    fun resetStates() {
        sentSignupDataState.value = SentSignupDataState.Idle
        validatedCodeState.value = ValidatedCodeState.Idle
    }
}

sealed class SentSignupDataState {
    object Idle : SentSignupDataState()

    object Loading : SentSignupDataState()

    object Success : SentSignupDataState()

    object Error : SentSignupDataState()
}

sealed class ValidatedCodeState {
    object Idle : ValidatedCodeState()

    object Loading : ValidatedCodeState()

    object True : ValidatedCodeState()

    object False : ValidatedCodeState()

    object Error : ValidatedCodeState()
}
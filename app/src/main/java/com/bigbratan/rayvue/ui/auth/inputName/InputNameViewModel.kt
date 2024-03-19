package com.bigbratan.rayvue.ui.auth.inputName

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.services.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InputNameViewModel @Inject constructor(
    private val accountService: AccountService,
) : ViewModel() {
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
}
package com.bigbratan.rayvue.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    userService: UserService,
) : ViewModel() {
    val canUserAccessContent: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        if (userService.isUserLoggedIn) {
            viewModelScope.launch {
                userService.user.collect { user ->
                    if (user != null) {
                        canUserAccessContent.value = true
                    }
                }
            }
        } else {
            canUserAccessContent.value = false
        }
    }
}

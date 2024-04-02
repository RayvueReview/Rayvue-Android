package com.bigbratan.rayvue.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.services.GamesService
import com.bigbratan.rayvue.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val userService: UserService,
) : ViewModel() {
    val startDestination: MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        if (userService.isUserLoggedIn) {
            viewModelScope.launch {
                userService.user.collect { user ->
                    if (user != null) {
                        startDestination.value = Screen.Main.route
                    }
                }
            }
        } else {
            startDestination.value = Screen.Auth.route
        }
    }
}
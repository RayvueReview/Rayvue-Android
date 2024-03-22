package com.bigbratan.rayvue.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.bigbratan.rayvue.ui.auth.AuthApp
import com.bigbratan.rayvue.ui.main.MainApp

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val viewModel: NavigationViewModel = hiltViewModel()
    val canUserAccessContent = viewModel.canUserAccessContent.collectAsState()

    if (canUserAccessContent.value)
        MainApp(navController = navController)
    else
        AuthApp(navController = navController)
}
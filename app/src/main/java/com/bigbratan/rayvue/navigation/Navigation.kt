package com.bigbratan.rayvue.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.bigbratan.rayvue.ui.auth.AuthApp
import com.bigbratan.rayvue.ui.main.MainApp

/*private val navItems = listOf(
    Screen.Main.GamesScreen,
    Screen.Main.AwardsScreen,
    Screen.Main.JournalScreen,
)*/

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val viewModel: NavigationViewModel = hiltViewModel()
    val canUserAccessContent = viewModel.canUserAccessContent.collectAsState()
    /*val startDestination = viewModel.startDestination.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    startDestination.value?.let { foundDestination ->
        Scaffold(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize(),
            bottomBar = {
                if (currentDestination == Screen.Main.route) {
                    NavigationBar {
                        // val navBackStackEntry by navController.currentBackStackEntryAsState()
                        // val currentRoute = navBackStackEntry?.destination?.route

                        navItems.forEach { item ->
                            BottomNavigationItem(
                                icon = { Icon(Icons.Default.Gamepad, contentDescription = null) },
                                selected = foundDestination == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            },
            content = { paddingValues ->
                NavHost(
                    modifier = Modifier.padding(paddingValues),
                    navController = navController,
                    startDestination = foundDestination,
                ) {
                    navigation(
                        route = Screen.Auth.route,
                        startDestination = Screen.Auth.AccountScreen.route
                    ) {
                        authApp(
                            navController = navController,
                        )
                    }

                    navigation(
                        route = Screen.Main.route,
                        startDestination = Screen.Main.GamesScreen.route
                    ) {
                        mainApp(
                            navController = navController,
                        )
                    }
                }
            }
        )
    }*/

    if (canUserAccessContent.value)
        MainApp(navController = navController)
    else
        AuthApp(navController = navController)
}
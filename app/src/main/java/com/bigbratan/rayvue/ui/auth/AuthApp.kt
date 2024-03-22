package com.bigbratan.rayvue.ui.auth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bigbratan.rayvue.navigation.NavigationViewModel
import com.bigbratan.rayvue.navigation.Screen
import com.bigbratan.rayvue.ui.auth.inputInvite.InputInviteScreen
import com.bigbratan.rayvue.ui.auth.inputName.InputNameScreen
import com.bigbratan.rayvue.ui.auth.login.LoginScreen
import com.bigbratan.rayvue.ui.auth.signup.SignupScreen

@Composable
fun AuthApp(
    navController: NavHostController,
    viewModel: NavigationViewModel = hiltViewModel()
) {
    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize(),
        content = { paddingValues ->
            NavHost(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                startDestination = Screen.Auth.AccountScreen.route,
            ) {
                composable(route = Screen.Auth.AccountScreen.route) {
                    AccountScreen(
                        onLoginClick = {
                            navController.navigate(
                                route = Screen.Auth.LoginScreen.route
                            )
                        },
                        onSignupClick = {
                            navController.navigate(
                                route = Screen.Auth.SignupScreen.route
                            )
                        },
                        onSkipClick = {
                            viewModel.setUserCanAccessContent(true)
                            /*navController.navigate(
                                route = Screen.Main.GamesScreen.route
                            ) {
                                popUpTo(Screen.Auth.AccountScreen.route) { inclusive = true }
                            }*/
                        }
                    )
                }

                composable(route = Screen.Auth.LoginScreen.route) {
                    LoginScreen(
                        onFinishClick = {
                            viewModel.setUserCanAccessContent(true)
                            /*navController.navigate(
                                route = Screen.Main.GamesScreen.route
                            ) {
                                popUpTo(Screen.Auth.AccountScreen.route) { inclusive = true }
                                popUpTo(Screen.Main.GamesScreen.route) { inclusive = true }
                            }*/
                        },
                        onBackClick = {
                            navController.popBackStack()
                        },
                    )
                }

                composable(route = Screen.Auth.SignupScreen.route) {
                    SignupScreen(
                        onNextClick = {
                            navController.navigate(
                                route = Screen.Auth.InputNameScreen.route
                            )
                        },
                        onBackClick = {
                            navController.popBackStack()
                        },
                    )
                }

                composable(route = Screen.Auth.InputNameScreen.route) {
                    InputNameScreen(
                        onNextClick = {
                            navController.navigate(
                                route = Screen.Auth.InputInviteScreen.route
                            )
                        },
                        onBackClick = {
                            navController.popBackStack()
                        },
                    )
                }

                composable(route = Screen.Auth.InputInviteScreen.route) {
                    InputInviteScreen(
                        onFinishClick = {
                            viewModel.setUserCanAccessContent(true)
                            /*navController.navigate(
                                route = Screen.Main.GamesScreen.route
                            ) {
                                popUpTo(Screen.Auth.AccountScreen.route) { inclusive = true }
                                popUpTo(Screen.Main.GamesScreen.route) { inclusive = true }
                            }*/
                        },
                        onBackClick = {
                            navController.popBackStack()
                        },
                    )
                }
            }
        }
    )
}

/*fun NavGraphBuilder.authApp(
    navController: NavHostController,
) {
    composable(route = Screen.Auth.AccountScreen.route) {
        AccountScreen(
            onLoginClick = {
                navController.navigate(
                    route = Screen.Auth.LoginScreen.route
                )
            },
            onSignupClick = {
                navController.navigate(
                    route = Screen.Auth.SignupScreen.route
                )
            },
            onSkipClick = {
                navController.navigate(
                    route = Screen.Main.GamesScreen.route
                ) {
                    popUpTo(Screen.Auth.AccountScreen.route) { inclusive = true }
                }
            }
        )
    }

    composable(route = Screen.Auth.LoginScreen.route) {
        LoginScreen(
            onFinishClick = {
                navController.navigate(
                    route = Screen.Main.GamesScreen.route
                ) {
                    popUpTo(Screen.Auth.AccountScreen.route) { inclusive = true }
                    popUpTo(Screen.Main.GamesScreen.route) { inclusive = true }
                }
            },
            onBackClick = {
                navController.popBackStack()
            },
        )
    }

    composable(route = Screen.Auth.SignupScreen.route) {
        SignupScreen(
            onNextClick = {
                navController.navigate(
                    route = Screen.Auth.InputNameScreen.route
                )
            },
            onBackClick = {
                navController.popBackStack()
            },
        )
    }

    composable(route = Screen.Auth.InputNameScreen.route) {
        InputNameScreen(
            onNextClick = {
                navController.navigate(
                    route = Screen.Auth.InputInviteScreen.route
                )
            },
            onBackClick = {
                navController.popBackStack()
            },
        )
    }

    composable(route = Screen.Auth.InputInviteScreen.route) {
        InputInviteScreen(
            onFinishClick = {
                navController.navigate(
                    route = Screen.Main.GamesScreen.route
                ) {
                    popUpTo(Screen.Auth.AccountScreen.route) { inclusive = true }
                    popUpTo(Screen.Main.GamesScreen.route) { inclusive = true }
                }
            },
            onBackClick = {
                navController.popBackStack()
            },
        )
    }
}*/

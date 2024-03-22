package com.bigbratan.rayvue.ui.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bigbratan.rayvue.navigation.Screen
import com.bigbratan.rayvue.ui.auth.inputInvite.InputInviteScreen
import com.bigbratan.rayvue.ui.auth.inputName.InputNameScreen
import com.bigbratan.rayvue.ui.auth.login.LoginScreen
import com.bigbratan.rayvue.ui.auth.signup.SignupScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AuthApp(
    navController: NavHostController,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = {
            NavHost(
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
            }
        }
    )
}

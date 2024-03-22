package com.bigbratan.rayvue.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.navigation.NavigationViewModel
import com.bigbratan.rayvue.navigation.Screen
import com.bigbratan.rayvue.ui.main.awards.AwardsScreen
import com.bigbratan.rayvue.ui.main.games.GamesScreen
import com.bigbratan.rayvue.ui.main.games.gameDetails.GameDetailsScreen
import com.bigbratan.rayvue.ui.main.games.tagsInfo.TagsInfoScreen
import com.bigbratan.rayvue.ui.main.journal.JournalScreen
import com.bigbratan.rayvue.ui.main.reviews.ReviewsScreen
import com.bigbratan.rayvue.ui.main.settings.SettingsScreen
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private val navItems = listOf(
    Screen.Main.GamesScreen,
    Screen.Main.AwardsScreen,
    Screen.Main.JournalScreen,
)

@Composable
fun MainApp(
    navController: NavHostController,
    viewModel: NavigationViewModel = hiltViewModel()
) {
    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = NavigationBarDefaults.containerColor,
                tonalElevation = 0.dp,
                windowInsets = NavigationBarDefaults.windowInsets,
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItems.forEach { item ->
                    val navIcon = when (item.route) {
                        Screen.Main.GamesScreen.route -> Icons.Default.Gamepad
                        Screen.Main.AwardsScreen.route -> Icons.Default.EmojiEvents
                        Screen.Main.JournalScreen.route -> Icons.Default.Book
                        else -> Icons.Default.Error
                    }

                    val navLabel = when (item.route) {
                        Screen.Main.GamesScreen.route -> R.string.nav_games_title
                        Screen.Main.AwardsScreen.route -> R.string.nav_awards_title
                        Screen.Main.JournalScreen.route -> R.string.nav_journal_title
                        else -> R.string.error_title
                    }

                    NavigationBarItem(
                        icon = {
                            Icon(navIcon, contentDescription = null)
                        },
                        label = {
                            Text(
                                text = stringResource(id = navLabel),
                                fontFamily = plusJakartaSans,
                                fontWeight = FontWeight(500),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(platformStyle = noFontPadding)
                            )
                        },
                        selected = currentRoute == item.route,
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
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = Screen.Main.GamesScreen.route,
        ) {
            composable(route = Screen.Main.GamesScreen.route) {
                GamesScreen(
                    onGameClick = { gameId ->
                        navController.navigate(
                            route = Screen.Main.GameDetailsScreen.routeWithArgs(
                                gameId
                            )
                        )
                    },
                    onSettingsClick = {
                        navController.navigate(
                            route = Screen.Main.SettingsScreen.route
                        )
                    }
                )
            }

            composable(route = Screen.Main.SettingsScreen.route) {
                SettingsScreen(
                    onLoginClick = {
                        viewModel.setUserCanAccessContent(false)
                        /*navController.navigate(
                            route = Screen.Auth.LoginScreen.route
                        )*/
                    },
                    onSignupClick = {
                        viewModel.setUserCanAccessContent(false)
                        /*navController.navigate(
                            route = Screen.Auth.SignupScreen.route
                        )*/
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }

            composable(
                route = Screen.Main.GameDetailsScreen.routeWithArgs("{id}"),
                arguments = listOf(navArgument(name = "id") { type = NavType.StringType }),
            ) { backStackEntry ->
                val gameIdArg = backStackEntry.arguments?.getString("id")

                gameIdArg?.let { gameId ->
                    GameDetailsScreen(
                        gameId = gameId,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onReviewClick = { reviewedGameId, reviewedGameName, reviewedGameEncodedIcon ->
                            navController.navigate(
                                route = Screen.Main.ReviewsScreen.routeWithArgs(
                                    reviewedGameId,
                                    reviewedGameName,
                                    reviewedGameEncodedIcon,
                                )
                            )
                        },
                        onTagsInfoClick = {
                            navController.navigate(
                                route = Screen.Main.TagsInfoScreen.route
                            )
                        }
                    )
                }
            }

            composable(route = Screen.Main.TagsInfoScreen.route) {
                TagsInfoScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }

            composable(
                route = Screen.Main.ReviewsScreen.routeWithArgs("{id}", "{name}", "{encodedIcon}"),
                arguments = listOf(
                    navArgument(name = "id") { type = NavType.StringType },
                    navArgument(name = "name") { type = NavType.StringType },
                    navArgument(name = "encodedIcon") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val gameIdArg = backStackEntry.arguments?.getString("id")
                val gameNameArg = backStackEntry.arguments?.getString("name")
                val gameIconArg = backStackEntry.arguments?.getString("encodedIcon")

                gameIdArg?.let { gameId ->
                    gameNameArg?.let { gameName ->
                        gameIconArg?.let { gameIcon ->
                            ReviewsScreen(
                                gameId = gameId,
                                gameName = gameName,
                                gameIcon = URLDecoder.decode(
                                    gameIcon,
                                    StandardCharsets.UTF_8.toString()
                                ),
                                onBackClick = {
                                    navController.popBackStack()
                                },
                            )
                        }
                    }
                }
            }

            composable(route = Screen.Main.AwardsScreen.route) {
                AwardsScreen()
            }

            composable(route = Screen.Main.JournalScreen.route) {
                JournalScreen()
            }
        }
    }
}

/*
fun NavGraphBuilder.mainApp(
    navController: NavHostController,
) {
    composable(route = Screen.Main.GamesScreen.route) {
        GamesScreen(
            onGameClick = { gameId ->
                navController.navigate(
                    route = Screen.Main.GameDetailsScreen.routeWithArgs(
                        gameId
                    )
                )
            },
            onSettingsClick = {
                navController.navigate(
                    route = Screen.Main.SettingsScreen.route
                )
            }
        )
    }

    composable(route = Screen.Main.SettingsScreen.route) {
        SettingsScreen(
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
            onBackClick = {
                navController.popBackStack()
            },
        )
    }

    composable(
        route = Screen.Main.GameDetailsScreen.routeWithArgs("{id}"),
        arguments = listOf(navArgument(name = "id") { type = NavType.StringType }),
    ) { backStackEntry ->
        val gameIdArg = backStackEntry.arguments?.getString("id")

        gameIdArg?.let { gameId ->
            GameDetailsScreen(
                gameId = gameId,
                onBackClick = {
                    navController.popBackStack()
                },
                onReviewClick = { reviewedGameId, reviewedGameName, reviewedGameEncodedIcon ->
                    navController.navigate(
                        route = Screen.Main.ReviewsScreen.routeWithArgs(
                            reviewedGameId,
                            reviewedGameName,
                            reviewedGameEncodedIcon,
                        )
                    )
                },
                onTagsInfoClick = {
                    navController.navigate(
                        route = Screen.Main.TagsInfoScreen.route
                    )
                }
            )
        }
    }

    composable(route = Screen.Main.TagsInfoScreen.route) {
        TagsInfoScreen(
            onBackClick = {
                navController.popBackStack()
            },
        )
    }

    composable(
        route = Screen.Main.ReviewsScreen.routeWithArgs("{id}", "{name}", "{encodedIcon}"),
        arguments = listOf(
            navArgument(name = "id") { type = NavType.StringType },
            navArgument(name = "name") { type = NavType.StringType },
            navArgument(name = "encodedIcon") { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val gameIdArg = backStackEntry.arguments?.getString("id")
        val gameNameArg = backStackEntry.arguments?.getString("name")
        val gameIconArg = backStackEntry.arguments?.getString("encodedIcon")

        gameIdArg?.let { gameId ->
            gameNameArg?.let { gameName ->
                gameIconArg?.let { gameIcon ->
                    ReviewsScreen(
                        gameId = gameId,
                        gameName = gameName,
                        gameIcon = URLDecoder.decode(
                            gameIcon,
                            StandardCharsets.UTF_8.toString()
                        ),
                        onBackClick = {
                            navController.popBackStack()
                        },
                    )
                }
            }
        }
    }

    composable(route = Screen.Main.AwardsScreen.route) {
        AwardsScreen()
    }

    composable(route = Screen.Main.JournalScreen.route) {
        JournalScreen()
    }
}*/

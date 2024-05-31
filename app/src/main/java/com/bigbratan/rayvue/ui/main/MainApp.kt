package com.bigbratan.rayvue.ui.main

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bigbratan.rayvue.navigation.Screen
import com.bigbratan.rayvue.ui.main.awards.AwardsScreen
import com.bigbratan.rayvue.ui.main.games.GamesScreen
import com.bigbratan.rayvue.ui.main.games.gameDetails.GameDetailsScreen
import com.bigbratan.rayvue.ui.main.games.tagsInfo.TagsInfoScreen
import com.bigbratan.rayvue.ui.main.journal.JournalScreen
import com.bigbratan.rayvue.ui.main.reviews.ReviewsScreen
import com.bigbratan.rayvue.ui.main.search.SearchScreen
import com.bigbratan.rayvue.ui.main.settings.SettingsScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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
            }
        )
    }

    composable(route = Screen.Main.AwardsScreen.route) {
        AwardsScreen(
            onGameClick = { gameId ->
                navController.navigate(
                    route = Screen.Main.GameDetailsScreen.routeWithArgs(
                        gameId
                    )
                )
            }
        )
    }

    composable(route = Screen.Main.JournalScreen.route) {
        JournalScreen()
    }

    composable(route = Screen.Main.SearchScreen.route) {
        SearchScreen(
            onGameClick = { gameId ->
                navController.navigate(
                    route = Screen.Main.GameDetailsScreen.routeWithArgs(
                        gameId
                    )
                )
            },
            onBackClick = {
                navController.popBackStack()
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
                    navController.popBackStack(
                        route = Screen.Main.GamesScreen.route,
                        inclusive = false
                    )
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
}

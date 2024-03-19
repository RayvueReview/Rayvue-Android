package com.bigbratan.rayvue.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth") {
        object AccountScreen : Screen("account_screen")

        object LoginScreen : Screen("login_screen")

        object SignupScreen : Screen("signup_screen")

        object InputNameScreen : Screen("input_name_screen")

        object InputInviteScreen : Screen("input_invite_screen")
    }

    object Main : Screen("main") {
        object GamesScreen : Screen("games_screen")

        object SettingsScreen: Screen("settings_screen")

        object GameDetailsScreen : Screen("game_details_screen")

        object ReviewsScreen : Screen("reviews_screen")
    }

    fun routeWithArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
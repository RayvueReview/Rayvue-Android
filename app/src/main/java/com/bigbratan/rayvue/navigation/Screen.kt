package com.bigbratan.rayvue.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth") {
        object AccountScreen : Screen("account_screen")

        object LoginScreen : Screen("login_screen")

        object SignupScreen : Screen("signup_screen")

        object InputNameScreen : Screen("input_name_screen")
    }

    object Main : Screen("main") {
        object GamesScreen : Screen("games_screen")

        object AwardsScreen : Screen("awards_screen")

        object JournalScreen : Screen("journal_screen")

        object EntryScreen : Screen("entry_screen") {
            const val customRouteWithArgs = "entry_screen/{gameId}/{gameName}/{gameIcon}"
            fun createRouteWithArgs(gameId: String, gameName: String, gameIcon: String) =
                "entry_screen/$gameId/$gameName/$gameIcon"
        }

        object SearchScreen : Screen("search_screen")

        object SettingsScreen : Screen("settings_screen")

        object GameDetailsScreen : Screen("game_details_screen")

        object ReviewsScreen : Screen("reviews_screen")

        object TagsInfoScreen : Screen("tags_info_screen")
    }

    fun routeWithArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.filter { it.isNotEmpty() }.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
package com.bigbratan.rayvue.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.ui.auth.authApp
import com.bigbratan.rayvue.ui.main.mainApp
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans

private val navItems = listOf(
    Screen.Main.GamesScreen,
    Screen.Main.AwardsScreen,
    Screen.Main.JournalScreen,
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigation() {
    val viewModel: NavigationViewModel = hiltViewModel()
    val navController = rememberNavController()
    val startDestination = viewModel.startDestination.collectAsState()
    val shouldShowBottomBar =
        navController.currentBackStackEntryAsState().value?.destination?.route in listOf(
            Screen.Main.GamesScreen.route,
            Screen.Main.AwardsScreen.route,
            Screen.Main.JournalScreen.route,
        )

    startDestination.value?.let { startDestinationValue ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (shouldShowBottomBar) {
                    EmbeddedBottomBar(
                        navController = navController,
                    )
                }
            }
        ) { paddingValues ->
            val navModifier = if (shouldShowBottomBar) {
                Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding(),
                    )
            } else {
                Modifier
            }
            NavHost(
                modifier = navModifier,
                navController = navController,
                startDestination = startDestinationValue,
            ) {
                navigation(
                    route = Screen.Auth.route,
                    startDestination = Screen.Auth.AccountScreen.route
                ) {
                    authApp(
                        navController = navController
                    )
                }

                navigation(
                    route = Screen.Main.route,
                    startDestination = Screen.Main.GamesScreen.route
                ) {
                    mainApp(
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
private fun EmbeddedBottomBar(
    navController: NavHostController
) {
    NavigationBar(
        tonalElevation = 0.dp,
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
                    Icon(
                        navIcon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = navLabel),
                        fontFamily = plusJakartaSans,
                        fontWeight = FontWeight.Normal,
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
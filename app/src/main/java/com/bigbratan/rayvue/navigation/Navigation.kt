package com.bigbratan.rayvue.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.bigbratan.rayvue.ui.views.TonalIconButton

private val navItems = listOf(
    Screen.Main.GamesScreen,
    Screen.Main.AwardsScreen,
    Screen.Main.JournalScreen,
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val viewModel: NavigationViewModel = hiltViewModel()
    val startDestination = viewModel.startDestination.collectAsState()

    startDestination.value?.let { startDestinationValue ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                val shouldShowTopBar =
                    navController.currentBackStackEntryAsState().value?.destination?.route in listOf(
                        Screen.Main.GamesScreen.route,
                        Screen.Main.AwardsScreen.route,
                        Screen.Main.JournalScreen.route,
                    )

                if (shouldShowTopBar) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        var isSearchActive by rememberSaveable { mutableStateOf(false) }
                        val searchedGamesState = viewModel.searchedGamesState.collectAsState()

                        EmbeddedSearchBar(
                            isSearchActive = isSearchActive,
                            onQueryChange = { query ->
                                viewModel.searchGames(query)
                                Log.d("search", "${searchedGamesState.value}")
                            },
                            onActiveChanged = { isSearchActive = it },
                            onSearch = { query ->
                                viewModel.searchGames(query)
                            }
                        )

                        TonalIconButton(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            imageVector = Icons.Filled.Settings,
                            onClick = {
                                navController.navigate(
                                    route = Screen.Main.SettingsScreen.route
                                )
                            },
                        )
                    }
                }
            },
            bottomBar = {
                val shouldShowBottomBar =
                    navController.currentBackStackEntryAsState().value?.destination?.route in listOf(
                        Screen.Main.GamesScreen.route,
                        Screen.Main.AwardsScreen.route,
                        Screen.Main.JournalScreen.route,
                    )

                if (shouldShowBottomBar) {
                    EmbeddedBottomBar(
                        navController = navController
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                // modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EmbeddedSearchBar(
    modifier: Modifier = Modifier,
    isSearchActive: Boolean,
    onQueryChange: (String) -> Unit,
    onActiveChanged: (Boolean) -> Unit,
    onSearch: ((String) -> Unit)? = null,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val activeChanged: (Boolean) -> Unit = { active ->
        searchQuery = ""
        onQueryChange("")
        onActiveChanged(active)
    }
    if (onSearch != null) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { query ->
                searchQuery = query
                onQueryChange(query)
            },
            onSearch = onSearch,
            active = isSearchActive,
            onActiveChange = activeChanged,
            modifier = modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(id = R.string.search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null,
                )
            },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            tonalElevation = 0.dp,
            content = {

            }
        )
    }
}
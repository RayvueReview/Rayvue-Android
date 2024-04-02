package com.bigbratan.rayvue.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val viewModel: NavigationViewModel = hiltViewModel()
    val navController = rememberNavController()
    val startDestination = viewModel.startDestination.collectAsState()

    val shouldShowBottomBar =
        navController.currentBackStackEntryAsState().value?.destination?.route in listOf(
            Screen.Main.GamesScreen.route,
            Screen.Main.AwardsScreen.route,
            Screen.Main.JournalScreen.route,
        )
    val shouldShowTopBar =
        navController.currentBackStackEntryAsState().value?.destination?.route in listOf(
            Screen.Main.GamesScreen.route,
            Screen.Main.AwardsScreen.route,
            Screen.Main.JournalScreen.route,
        )

    startDestination.value?.let { startDestinationValue ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (shouldShowTopBar) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp)
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        /*EmbeddedSearchBar(
                            modifier = Modifier.fillMaxWidth(),
                            searchedGamesState = searchedGamesState.value,
                            isSearchActive = isSearchActive,
                            onQueryChange = { query ->
                                viewModel.searchGames(query)
                            },
                            onActiveChanged = { isSearchActive = it },
                            onSearch = { query ->
                                viewModel.searchGames(query)
                            },
                            onGameClick = { gameId ->
                                navController.navigate(
                                    route = Screen.Main.GameDetailsScreen.routeWithArgs(
                                        gameId
                                    )
                                )
                            }
                        )*/

                        EmbeddedSearchBar(
                            placeholder = if (navController.currentBackStackEntryAsState().value?.destination?.route == Screen.Main.JournalScreen.route)
                                stringResource(id = R.string.search_local_placeholder)
                            else
                                stringResource(id = R.string.search_database_placeholder),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                navController.navigate(
                                    route = Screen.Main.SettingsScreen.route
                                )
                            }
                        )

                        TonalIconButton(
                            modifier = Modifier.padding(start = 24.dp),
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
                if (shouldShowBottomBar) {
                    EmbeddedBottomBar(
                        navController = navController
                    )
                }
            }
        ) { paddingValues ->
            val navModifier = if (shouldShowBottomBar) {
                Modifier.padding(
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
private fun EmbeddedSearchBar(
    modifier: Modifier,
    placeholder: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(64.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = onClick,
            )
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(64.dp)
            )
            .padding(
                horizontal = 24.dp,
                vertical = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null,
        )

        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = placeholder,
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight(500),
            color = MaterialTheme.colorScheme.onSurface,
            style = TextStyle(platformStyle = noFontPadding)
        )
    }
}

/*@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EmbeddedSearchBar(
    modifier: Modifier,
    searchedGamesState: SearchedGamesState,
    isSearchActive: Boolean,
    onQueryChange: (String) -> Unit,
    onActiveChanged: (Boolean) -> Unit,
    onSearch: ((String) -> Unit)? = null,
    onGameClick: (gameId: String) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val activeChanged: (Boolean) -> Unit = { active ->
        searchQuery = ""
        onQueryChange("")
        onActiveChanged(active)
    }

    val searchBarModifier = if (isSearchActive) {
        modifier
    } else {
        modifier
            .padding(
                start = 24.dp,
                end = 64.dp,
            )
    }

    if (onSearch != null) {
        SearchBar(
            modifier = searchBarModifier,
            query = searchQuery,
            onQueryChange = { query ->
                searchQuery = query
                onQueryChange(query)
            },
            onSearch = onSearch,
            active = isSearchActive,
            onActiveChange = activeChanged,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.search_database_placeholder),
                    fontFamily = plusJakartaSans,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = TextStyle(platformStyle = noFontPadding)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            onQueryChange("")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            tonalElevation = 0.dp,
            content = {
                when (searchedGamesState) {
                    is SearchedGamesState.Loading -> {}

                    is SearchedGamesState.Error -> {
                        ErrorMessage(
                            message = stringResource(
                                id = R.string.games_get_data_error_message
                            ),
                            isInHomeScreen = true,
                            onBackClick = { }
                        )
                    }

                    is SearchedGamesState.Success -> {
                        val games = searchedGamesState.games

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(SEARCH_GRID_SIZE)
                        ) {
                            items(games.size) { gameIndex ->
                                val game = games[gameIndex]

                                HorizontalGameCard(
                                    game = game,
                                    onGameClick = { onGameClick(game.id) },
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}*/
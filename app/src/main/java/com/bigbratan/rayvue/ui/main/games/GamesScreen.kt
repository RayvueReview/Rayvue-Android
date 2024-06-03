package com.bigbratan.rayvue.ui.main.games

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.ui.theme.Black75
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import com.bigbratan.rayvue.ui.views.ContentSectionHeader
import com.bigbratan.rayvue.ui.views.ErrorMessage
import com.bigbratan.rayvue.ui.views.FadingScrimBackground
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.TonalIconButton
import kotlinx.coroutines.flow.distinctUntilChanged

const val MAIN_GRID_SIZE = 2

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun GamesScreen(
    viewModel: GamesViewModel = hiltViewModel(),
    onGameClick: (gameId: String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val obtainedGamesState = viewModel.obtainedGamesState.collectAsState()
    val obtainedFilteredGamesState = viewModel.obtainedFilteredGamesState.collectAsState()
    val isRefreshing = viewModel.isRefreshing.collectAsState()
    val gridState = rememberLazyGridState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value,
        onRefresh = {
            if (obtainedGamesState.value !is ObtainedGamesState.Loading) {
                viewModel.resetStates()
                viewModel.getAllGames()
                viewModel.getFilteredGames()
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.getFilteredGames(canRefresh = false)
        viewModel.getAllGames(canRefresh = false)
    }

    Box(
        modifier = Modifier
            .pullRefresh(pullRefreshState)
            .fillMaxSize()
    ) {
        when (obtainedFilteredGamesState.value) {
            is ObtainedFilteredGamesState.Loading -> {
                LoadingAnimation(
                    modifier = Modifier.fillMaxSize()
                )
            }

            is ObtainedFilteredGamesState.Error -> {
                ErrorMessage(
                    message = stringResource(
                        id = R.string.games_get_data_error_message
                    ),
                    isInHomeScreen = true,
                    onBackClick = { }
                )
            }

            is ObtainedFilteredGamesState.Success -> {
                LaunchedEffect(gridState) {
                    snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .distinctUntilChanged()
                        .collect { lastIndex ->
                            val totalItemCount =
                                (obtainedGamesState.value as? ObtainedGamesState.Success)?.allGames?.size
                                    ?: 0

                            if (lastIndex != null && lastIndex >= totalItemCount - 1) {
                                viewModel.getAllGames(
                                    canRefresh = false,
                                    canLoadMore = true
                                )
                            }
                        }
                }

                when (obtainedGamesState.value) {
                    is ObtainedGamesState.Loading -> {
                        LoadingAnimation(
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is ObtainedGamesState.Error -> {
                        ErrorMessage(
                            message = stringResource(
                                id = R.string.games_get_data_error_message
                            ),
                            isInHomeScreen = true,
                            onBackClick = { }
                        )
                    }

                    is ObtainedGamesState.Success -> {
                        val allGames =
                            (obtainedGamesState.value as ObtainedGamesState.Success).allGames
                        val recentGames =
                            (obtainedFilteredGamesState.value as ObtainedFilteredGamesState.Success).recentGames
                        val randomGames =
                            (obtainedFilteredGamesState.value as ObtainedFilteredGamesState.Success).randomGames

                        GamesView(
                            allGames = allGames,
                            recentGames = recentGames,
                            randomGames = randomGames,
                            gridState = gridState,
                            onGameClick = onGameClick,
                            onSearchClick = onSearchClick,
                            onSettingsClick = onSettingsClick,
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
            refreshing = isRefreshing.value,
            state = pullRefreshState,
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun GamesView(
    allGames: List<Game>,
    recentGames: List<Game>,
    randomGames: List<Game>,
    gridState: LazyGridState,
    onGameClick: (gameId: String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            EmbeddedSearchBar(
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick,
            )
        },
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(MAIN_GRID_SIZE),
            state = gridState
        ) {
            item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                ContentSectionHeader(
                    modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                    text = stringResource(id = R.string.games_section_recent_title),
                    onClick = null,
                )
            }

            item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                ) {
                    items(recentGames.size) { gameIndex ->
                        val game = recentGames[gameIndex]

                        GameCard(
                            game = game,
                            onGameClick = { onGameClick(game.id) },
                        )
                    }
                }
            }

            item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                ContentSectionHeader(
                    modifier = Modifier.padding(top = 12.dp),
                    text = stringResource(id = R.string.games_section_discover_title),
                    onClick = null,
                )
            }

            item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                ) {
                    items(randomGames.size) { gameIndex ->
                        val game = randomGames[gameIndex]

                        GameCard(
                            game = game,
                            onGameClick = { onGameClick(game.id) },
                        )
                    }
                }
            }

            item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                ContentSectionHeader(
                    modifier = Modifier.padding(top = 12.dp),
                    text = stringResource(id = R.string.games_section_all_title),
                    onClick = null,
                )
            }

            items(allGames.size) { gameIndex ->
                val modifier = when {
                    gameIndex % 2 == 0 -> Modifier.padding(
                        start = 24.dp,
                        end = 6.dp,
                        bottom = 12.dp,
                    )

                    gameIndex % 2 == 1 -> Modifier.padding(
                        start = 6.dp,
                        end = 24.dp,
                        bottom = 12.dp,
                    )

                    else -> Modifier
                }

                val game = allGames[gameIndex]

                GameCard(
                    modifier = modifier,
                    game = game,
                    onGameClick = { onGameClick(game.id) },
                )
            }
        }
    }
}

@Composable
private fun GameCard(
    modifier: Modifier = Modifier,
    game: Game,
    onGameClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onGameClick)
            .width(148.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .aspectRatio(1f),
            model = game.icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        FadingScrimBackground(
            aspectRatio = 1f,
            bottomColor = Black75,
            roundedCornerShape = RoundedCornerShape(16.dp),
        )

        Text(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.BottomStart),
            text = game.displayName ?: "",
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Color.White,
            style = TextStyle(
                platformStyle = noFontPadding,
                letterSpacing = 0.25.sp,
            ),
        )
    }
}

@Composable
private fun EmbeddedSearchBar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SearchBarButton(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .weight(1f),
                placeholder = stringResource(id = R.string.search_database_placeholder),
                onClick = onSearchClick,
                shape = RoundedCornerShape(64.dp),
            )

            TonalIconButton(
                modifier = Modifier.padding(horizontal = 24.dp),
                imageVector = Icons.Filled.Settings,
                onClick = onSettingsClick,
            )
        }
    }
}

@Composable
internal fun SearchBarButton(
    modifier: Modifier = Modifier,
    placeholder: String,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = onClick,
            )
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = shape
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null,
        )

        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = placeholder,
            fontSize = 16.sp,
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            style = TextStyle(platformStyle = noFontPadding)
        )
    }
}
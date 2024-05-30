package com.bigbratan.rayvue.ui.main.games

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.whenResumed
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

const val MAIN_GRID_SIZE = 2

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun GamesScreen(
    viewModel: GamesViewModel = hiltViewModel(),
    onGameClick: (gameId: String) -> Unit,
) {
    val obtainedGamesState = viewModel.obtainedGamesState.collectAsState()
    val obtainedFilteredGamesState = viewModel.obtainedFilteredGamesState.collectAsState()
    val isRefreshing = viewModel.isRefreshing.collectAsState()
    val gridState = rememberLazyGridState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value,
        onRefresh = {
            if (obtainedGamesState.value !is ObtainedGamesState.Loading) {
                viewModel.resetStates(keepLastSnapshot = true)
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
) {
    Scaffold(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(MAIN_GRID_SIZE),
            state = gridState
        ) {
            item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                ContentSectionHeader(
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
            text = game.displayName,
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

/*LaunchedEffect(listStateRecent) {
        snapshotFlow { listStateRecent.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && obtainedRecentGamesState.value is Resource.Success) {
                    val recentItemCount =
                        (obtainedRecentGamesState.value as Resource.Success).recentGames.size

                    if (lastIndex >= recentItemCount - 1) {
                        viewModel.getAllGames(
                            canRefresh = false,
                            canLoadMore = true,
                        )
                    }
                }
            }
    }

    LaunchedEffect(listStateRandom) {
        snapshotFlow { listStateRandom.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && obtainedRandomGamesState.value is Resource.Success) {
                    val randomItemCount =
                        (obtainedRandomGamesState.value as Resource.Success).randomGames.size

                    if (lastIndex >= randomItemCount - 1) {
                        viewModel.getAllGames(
                            canRefresh = false,
                            canLoadMore = true,
                        )
                    }
                }
            }
    }*/
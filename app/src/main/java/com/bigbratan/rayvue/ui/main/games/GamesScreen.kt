package com.bigbratan.rayvue.ui.main.games

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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

const val MAIN_GRID_SIZE = 2

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun GamesScreen(
    viewModel: GamesViewModel = hiltViewModel(),
    onGameClick: (gameId: String) -> Unit,
) {
    val obtainedGamesState = viewModel.obtainedGamesState.collectAsState()
    val isRefreshing = viewModel.isRefreshing.collectAsState()
    val gridState = rememberLazyGridState()
    val listState1 = rememberLazyListState()
    val listState2 = rememberLazyListState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value,
        onRefresh = {
            if (obtainedGamesState.value !is ObtainedGamesState.Loading) {
                viewModel.getData()
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.getData(canRefresh = false)
    }

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && obtainedGamesState.value is ObtainedGamesState.Success) {
                    val totalItemCount =
                        (obtainedGamesState.value as ObtainedGamesState.Success).games.allGames.size

                    if (lastIndex >= totalItemCount - 1) {
                        viewModel.getData(canRefresh = false, loadMore = true)
                    }
                }
            }
    }

    LaunchedEffect(listState1) {
        snapshotFlow { listState1.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && obtainedGamesState.value is ObtainedGamesState.Success) {
                    val recentItemCount =
                        (obtainedGamesState.value as ObtainedGamesState.Success).games.recentGames.size

                    if (lastIndex >= recentItemCount - 1) {
                        viewModel.getData(canRefresh = false, loadMore = true)
                    }
                }
            }
    }

    LaunchedEffect(listState2) {
        snapshotFlow { listState2.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && obtainedGamesState.value is ObtainedGamesState.Success) {
                    val randomItemCount =
                        (obtainedGamesState.value as ObtainedGamesState.Success).games.randomGames.size

                    if (lastIndex >= randomItemCount - 1) {
                        viewModel.getData(canRefresh = false, loadMore = true)
                    }
                }
            }
    }

    Box(
        modifier = Modifier
            .pullRefresh(pullRefreshState)
            .fillMaxSize()
    ) {
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
                val games = (obtainedGamesState.value as ObtainedGamesState.Success).games

                GamesView(
                    games = games,
                    gridState = gridState,
                    listState1 = listState1,
                    listState2 = listState2,
                    onGameClick = onGameClick,
                )
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
    games: GamesItemViewModel,
    gridState: LazyGridState,
    listState1: LazyListState,
    listState2: LazyListState,
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
                    state = listState1,
                ) {
                    items(games.recentGames.size) { gameIndex ->
                        val game = games.recentGames[gameIndex]

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
                    state = listState2,
                ) {
                    items(games.randomGames.size) { gameIndex ->
                        val game = games.randomGames[gameIndex]

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

            items(games.allGames.size) { gameIndex ->
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

                val game = games.allGames[gameIndex]

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
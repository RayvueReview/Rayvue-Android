package com.bigbratan.rayvue.ui.games

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.bigbratan.rayvue.ui.views.ErrorMessage
import com.bigbratan.rayvue.ui.views.FadingScrimBackground
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.SectionHeader
import com.bigbratan.rayvue.ui.views.TonalIconButton

const val GRID_SIZE = 2

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun GamesScreen(
    viewModel: GamesViewModel = hiltViewModel(),
    onGameClick: (gameId: String) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val obtainedGamesState = viewModel.obtainedGamesState.collectAsState()
    val isRefreshing = viewModel.isRefreshing.collectAsState()

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

    Box(Modifier.pullRefresh(pullRefreshState)) {
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
                val userName = (obtainedGamesState.value as ObtainedGamesState.Success).userName

                GamesView(
                    games = games,
                    userName = userName,
                    onGameClick = onGameClick,
                    onSettingsClick = onSettingsClick,
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
    userName: String?,
    onGameClick: (gameId: String) -> Unit,
    onSettingsClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_SIZE),
        ) {
            item(span = { GridItemSpan(GRID_SIZE) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 32.dp,
                            bottom = 20.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .weight(1f),
                        text = userName?.let { userName ->
                            stringResource(
                                id = R.string.games_welcome_account_message,
                                userName
                            )
                        }
                            ?: stringResource(id = R.string.games_welcome_no_account_message),
                        fontFamily = plusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = TextStyle(
                            platformStyle = noFontPadding,
                            letterSpacing = 0.15.sp,
                        ),
                    )

                    TonalIconButton(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        imageVector = Icons.Filled.Settings,
                        onClick = { onSettingsClick() },
                    )
                }
            }

            item(span = { GridItemSpan(GRID_SIZE) }) {
                SectionHeader(
                    text = stringResource(id = R.string.games_section_recent_title),
                    onClick = null,
                )
            }

            item(span = { GridItemSpan(GRID_SIZE) }) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
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

            item(span = { GridItemSpan(GRID_SIZE) }) {
                SectionHeader(
                    modifier = Modifier.padding(top = 12.dp),
                    text = stringResource(id = R.string.games_section_discover_title),
                    onClick = null,
                )
            }

            item(span = { GridItemSpan(GRID_SIZE) }) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
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

            item(span = { GridItemSpan(GRID_SIZE) }) {
                SectionHeader(
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
            text = game.name,
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
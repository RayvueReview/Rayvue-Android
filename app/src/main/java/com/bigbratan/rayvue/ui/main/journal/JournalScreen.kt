package com.bigbratan.rayvue.ui.main.journal

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import com.bigbratan.rayvue.ui.views.ErrorMessage
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel(),
    onGameClick: (
        gameId: String,
        gameName: String,
        gameIcon: String,
    ) -> Unit,
    onAddClick: () -> Unit,
) {
    val obtainedJournalGamesState = viewModel.obtainedJournalGamesState.collectAsState()
    val isRefreshing = viewModel.isRefreshing.collectAsState()
    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value,
        onRefresh = {
            if (obtainedJournalGamesState.value !is ObtainedJournalGamesState.Loading) {
                viewModel.resetObtainedState()
                viewModel.getJournalGames()
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.getJournalGames(canRefresh = false)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastIndex ->
                val totalItemCount =
                    (obtainedJournalGamesState.value as? ObtainedJournalGamesState.Success)?.journalGames?.size
                        ?: 0

                if (lastIndex != null && lastIndex >= totalItemCount - 1) {
                    viewModel.getJournalGames(
                        canRefresh = false,
                        canLoadMore = true
                    )
                }
            }
    }

    Box(
        modifier = Modifier
            .pullRefresh(pullRefreshState)
            .fillMaxSize()
    ) {
        when (obtainedJournalGamesState.value) {
            is ObtainedJournalGamesState.Loading -> {
                LoadingAnimation(
                    modifier = Modifier.fillMaxSize()
                )
            }

            is ObtainedJournalGamesState.Error -> {
                ErrorMessage(
                    message = stringResource(
                        id = R.string.journal_get_data_error_message
                    ),
                    isInHomeScreen = true,
                    onBackClick = { }
                )

                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    onClick = onAddClick,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                }
            }

            is ObtainedJournalGamesState.Success -> {
                val journalGames =
                    (obtainedJournalGamesState.value as ObtainedJournalGamesState.Success).journalGames

                JournalView(
                    journalGames = journalGames,
                    listState = listState,
                    onGameClick = onGameClick,
                )

                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    onClick = onAddClick,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
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
private fun JournalView(
    journalGames: List<Game>,
    listState: LazyListState,
    onGameClick: (
        gameId: String,
        gameName: String,
        gameIcon: String,
    ) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
        ) {
            items(journalGames.size) { gameIndex ->
                val game = journalGames[gameIndex]

                JournalGameCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(
                            top = if (gameIndex == 0) 24.dp else 12.dp,
                            bottom = if (gameIndex == journalGames.size) 24.dp else 12.dp
                        ),
                    icon = game.icon,
                    banner = game.banner ?: "",
                    title = game.displayName,
                    onGameClick = {
                        onGameClick(
                            game.id,
                            game.displayName,
                            game.icon
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun JournalGameCard(
    modifier: Modifier = Modifier,
    icon: String,
    banner: String,
    title: String,
    onGameClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onGameClick)
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .aspectRatio(16f / 8f),
            model = banner,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .size(56.dp),
                model = icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = title,
                fontFamily = plusJakartaSans,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(platformStyle = noFontPadding),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
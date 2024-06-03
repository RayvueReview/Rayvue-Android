package com.bigbratan.rayvue.ui.main.awards

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import com.bigbratan.rayvue.ui.views.ContentSectionHeader
import com.bigbratan.rayvue.ui.views.ErrorMessage
import com.bigbratan.rayvue.ui.views.FadingScrimBackground
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.TonalIconButton
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun AwardsScreen(
    viewModel: AwardsViewModel = hiltViewModel(),
    onGameClick: (gameId: String) -> Unit,
) {
    val obtainedTopGamesState = viewModel.obtainedTopGamesState.collectAsState()
    val isRefreshing = viewModel.isRefreshing.collectAsState()
    val listState = rememberLazyListState()
    val selectedDateType = viewModel.selectedDateType

    val pagerState = rememberPagerState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value,
        onRefresh = {
            if (obtainedTopGamesState.value !is ObtainedTopGamesState.Loading) {
                viewModel.resetState()
                viewModel.getTopGames(
                    dateType = selectedDateType.value.dateType,
                    canRefresh = false
                )
            }
        }
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastIndex ->
                val totalItemCount =
                    (obtainedTopGamesState.value as? ObtainedTopGamesState.Success)?.topGames?.size
                        ?: 0

                if (lastIndex != null && lastIndex >= totalItemCount - 1) {
                    viewModel.getTopGames(
                        dateType = selectedDateType.value.dateType,
                        canRefresh = false,
                        canLoadMore = true
                    )
                }
            }
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(DateType.values()[pagerState.currentPage])
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TabRow(
            modifier = Modifier.statusBarsPadding(),
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier
                        .tabIndicatorOffset(
                            currentTabPosition = tabPositions[pagerState.currentPage],
                        )
                        .padding(horizontal = 32.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    ,
                )
            },
            divider = {},
        ) {
            DateType.values().forEachIndexed { index, dateType ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        if (pagerState.currentPage != index) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                            viewModel.onTabSelected(dateType)
                        }
                    },
                    text = {
                        Text(
                            fontSize = 16.sp,
                            fontFamily = plusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            style = TextStyle(platformStyle = noFontPadding),
                            text = when (dateType) {
                                DateType.WEEK -> stringResource(id = R.string.awards_tab_week_title).uppercase()
                                DateType.MONTH -> stringResource(id = R.string.awards_tab_month_title).uppercase()
                                DateType.YEAR -> stringResource(id = R.string.awards_tab_year_title).uppercase()
                            }
                        )
                    },
                )
            }
        }

        Box(
            modifier = Modifier
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                pageCount = DateType.values().size,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                if (page == pagerState.currentPage) {
                    when (obtainedTopGamesState.value) {
                        is ObtainedTopGamesState.Loading -> {
                            LoadingAnimation(
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        is ObtainedTopGamesState.Error -> {
                            ErrorMessage(
                                message = stringResource(
                                    id = R.string.awards_get_data_error_message
                                ),
                                isInHomeScreen = true,
                                onBackClick = { }
                            )
                        }

                        is ObtainedTopGamesState.Success -> {
                            val topGames =
                                (obtainedTopGamesState.value as ObtainedTopGamesState.Success).topGames

                            AwardsView(
                                topGames = topGames,
                                awardDate = selectedDateType.value.dateType,
                                listState = listState,
                                onGameClick = onGameClick
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
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun AwardsView(
    topGames: List<AwardGameItemViewModel>,
    awardDate: String,
    listState: LazyListState,
    onGameClick: (gameId: String) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(state = listState) {
            item {
                CurrentAwardGameCard(
                    modifier = Modifier.fillMaxWidth(),
                    banner = topGames[0].banner,
                    title = topGames[0].displayName,
                    onArrowClick = { onGameClick(topGames[0].id) },
                )
            }

            item {
                ContentSectionHeader(
                    modifier = Modifier.padding(top = 12.dp),
                    text = stringResource(id = R.string.awards_section_past_title),
                    onClick = null,
                )
            }

            items(topGames.size) { gameIndex ->
                val game = topGames[gameIndex]

                if (gameIndex != 0)
                    PastAwardsGameCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        icon = game.icon,
                        title = game.displayName,
                        awardDate = if (awardDate == DateType.WEEK.dateType) game.gameOfTheWeek else if (awardDate == DateType.MONTH.dateType) game.gameOfTheMonth else game.gameOfTheYear,
                        onGameClick = { onGameClick(game.id) },
                    )
            }
        }
    }
}

@Composable
private fun CurrentAwardGameCard(
    modifier: Modifier = Modifier,
    banner: String,
    title: String,
    onArrowClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        AsyncImage(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .aspectRatio(1f),
            model = banner,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        FadingScrimBackground(
            aspectRatio = 1f,
            bottomColor = MaterialTheme.colorScheme.surface,
            roundedCornerShape = RoundedCornerShape(16.dp),
        )

        Column(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TonalIconButton(
                imageVector = Icons.Filled.ArrowForward,
                onClick = onArrowClick,
            )

            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = title ?: "",
                fontFamily = plusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = Color.White,
                style = TextStyle(platformStyle = noFontPadding),
            )
        }
    }
}

@Composable
private fun PastAwardsGameCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    awardDate: String,
    onGameClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onGameClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .size(56.dp),
            model = icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier.padding(start = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = title,
                fontFamily = plusJakartaSans,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(platformStyle = noFontPadding),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = awardDate,
                fontFamily = plusJakartaSans,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = TextStyle(platformStyle = noFontPadding),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
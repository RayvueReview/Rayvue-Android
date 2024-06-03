package com.bigbratan.rayvue.ui.main.gameDetails

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.ui.main.games.MAIN_GRID_SIZE
import com.bigbratan.rayvue.ui.main.reviews.ReviewItemViewModel
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import com.bigbratan.rayvue.ui.views.ContentSectionHeader
import com.bigbratan.rayvue.ui.views.ErrorMessage
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.SolidScrimBackground
import com.bigbratan.rayvue.ui.views.TransparentIconButton

@Composable
internal fun GameDetailsScreen(
    gameId: String,
    viewModel: GameDetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onReviewClick: (
        gameId: String,
        gameName: String,
        gameIcon: String,
    ) -> Unit,
    onTagsInfoClick: () -> Unit,
) {
    val obtainedGameDetailsState by viewModel.obtainedGameDetailsState.collectAsState()

    val view = LocalView.current
    val window = (view.context as Activity).window
    val usesDarkTheme = isSystemInDarkTheme()

    LaunchedEffect(gameId) {
        viewModel.getData(gameId)
    }

    DisposableEffect(Unit) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false

        onDispose {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !usesDarkTheme
        }
    }

    when (obtainedGameDetailsState) {
        is ObtainedGameDetailsState.Loading -> {
            LoadingAnimation(
                modifier = Modifier.fillMaxSize()
            )
        }

        is ObtainedGameDetailsState.Error -> {
            ErrorMessage(
                message = stringResource(
                    id = R.string.game_details_get_data_error_message
                ),
                isInHomeScreen = false,
                onBackClick = { onBackClick() }
            )
        }

        is ObtainedGameDetailsState.Success -> {
            val gameDetails =
                (obtainedGameDetailsState as ObtainedGameDetailsState.Success).gameDetails
            val reviews = (obtainedGameDetailsState as ObtainedGameDetailsState.Success).reviews

            GameDetailsView(
                gameDetails = gameDetails,
                reviews = reviews,
                onBackClick = onBackClick,
                onReviewClick = onReviewClick,
                onTagsInfoClick = onTagsInfoClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun GameDetailsView(
    gameDetails: GameDetailsItemViewModel,
    reviews: List<ReviewItemViewModel>,
    onBackClick: () -> Unit,
    onReviewClick: (
        gameId: String,
        gameName: String,
        gameIcon: String,
    ) -> Unit,
    onTagsInfoClick: () -> Unit,
) {
    val sheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BottomSheetScaffold(
            scaffoldState = sheetScaffoldState,
            sheetShape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp),
            sheetBackgroundColor = MaterialTheme.colorScheme.surface,
            backgroundColor = MaterialTheme.colorScheme.surface,
            sheetPeekHeight = LocalConfiguration.current.screenHeightDp.dp * 0.65f,
            sheetElevation = 0.dp,
            drawerElevation = 0.dp,
            sheetContent = {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    columns = GridCells.Fixed(MAIN_GRID_SIZE)
                ) {
                    item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            text = gameDetails.name,
                            fontFamily = plusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(
                                platformStyle = noFontPadding,
                                letterSpacing = 0.25.sp,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }

                    item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 36.dp)
                                .padding(horizontal = 24.dp),
                            text = "\"${gameDetails.description}\"",
                            fontFamily = plusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(
                                platformStyle = noFontPadding,
                                letterSpacing = 0.25.sp,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }

                    item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                        ContentSectionHeader(
                            modifier = Modifier.padding(top = 24.dp),
                            text = stringResource(id = R.string.game_details_section_reviews_title),
                            imageVector = Icons.Outlined.ArrowForward,
                            onClick = {
                                onReviewClick(
                                    gameDetails.id,
                                    gameDetails.name,
                                    gameDetails.encodedIcon,
                                )
                            }
                        )
                    }

                    if (reviews.isEmpty()) {
                        item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                            ReviewsMissingCard(
                                modifier = Modifier.fillMaxWidth(),
                                onReviewClick = {
                                    onReviewClick(
                                        gameDetails.id,
                                        gameDetails.name,
                                        gameDetails.encodedIcon,
                                    )
                                }
                            )
                        }
                    } else {
                        item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp),
                            ) {
                                items(reviews.size) { reviewIndex ->
                                    reviews[reviewIndex].let { review ->
                                        ReviewCard(
                                            name = review.userName,
                                            date = review.formattedDate,
                                            content = review.content,
                                            onReviewClick = {
                                                onReviewClick(
                                                    gameDetails.id,
                                                    gameDetails.name,
                                                    gameDetails.encodedIcon,
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                        ContentSectionHeader(
                            text = stringResource(id = R.string.game_details_section_keep_in_mind_title),
                            imageVector = Icons.Outlined.Info,
                            onClick = { onTagsInfoClick() },
                        )
                    }

                    items(gameDetails.tags.size) { tagIndex ->
                        val tagModifier = when {
                            tagIndex % 2 == 0 -> Modifier.padding(
                                start = 24.dp,
                                end = 6.dp,
                                bottom = 12.dp,
                            )

                            tagIndex % 2 == 1 -> Modifier.padding(
                                start = 6.dp,
                                end = 24.dp,
                                bottom = 12.dp,
                            )

                            else -> Modifier
                        }

                        TagCard(
                            modifier = tagModifier,
                            content = if (tagIndex == 0) {
                                if (gameDetails.tags[tagIndex] != "0.0")
                                    stringResource(
                                        id = R.string.game_details_tag_price_symbol,
                                        gameDetails.tags[tagIndex]
                                    )
                                else stringResource(id = R.string.game_details_tag_price_free)
                            } else gameDetails.tags[tagIndex]
                        )
                    }

                    item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                        ContentSectionHeader(
                            text = stringResource(id = R.string.game_details_section_categories_title),
                            onClick = null,
                        )
                    }

                    items(gameDetails.categories.size) { categoryIndex ->
                        val categoryModifier = when {
                            categoryIndex % 2 == 0 -> Modifier.padding(
                                start = 24.dp,
                                end = 6.dp,
                                bottom = 12.dp,
                            )

                            categoryIndex % 2 == 1 -> Modifier.padding(
                                start = 6.dp,
                                end = 24.dp,
                                bottom = 12.dp,
                            )

                            else -> Modifier
                        }

                        TagCard(
                            modifier = categoryModifier,
                            content = gameDetails.categories[categoryIndex]
                        )
                    }
                }
            },
            content = {
                Box {
                    AsyncImage(
                        modifier = Modifier.aspectRatio(16f / 14f),
                        model = gameDetails.banner,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )

                    SolidScrimBackground()
                }
            }
        )

        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            title = {},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            ),
            navigationIcon = {
                TransparentIconButton(
                    modifier = Modifier.padding(start = 16.dp),
                    imageVector = Icons.Filled.ArrowBack,
                    onClick = { onBackClick() },
                )
            }
        )
    }
}


@Composable
private fun ReviewCard(
    name: String,
    date: String,
    content: String,
    onReviewClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onReviewClick)
            .width(220.dp)
            .heightIn(min = 132.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = name,
                    fontFamily = plusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = TextStyle(
                        platformStyle = noFontPadding,
                        letterSpacing = 0.15.sp,
                    ),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )

                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = date,
                    fontFamily = plusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = TextStyle(
                        platformStyle = noFontPadding,
                        letterSpacing = 0.15.sp,
                    ),
                )
            }

            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = content,
                fontFamily = plusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(
                    platformStyle = noFontPadding,
                    letterSpacing = 0.15.sp,
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 4,
            )
        }
    }
}

@Composable
private fun ReviewsMissingCard(
    modifier: Modifier,
    onReviewClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .padding(bottom = 12.dp)
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onReviewClick)
            .fillMaxWidth()
            .height(132.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.padding(12.dp),
                text = stringResource(id = R.string.game_details_empty_data_error_message),
                fontFamily = plusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(
                    platformStyle = noFontPadding,
                    letterSpacing = 0.15.sp,
                    textAlign = TextAlign.Center
                ),
            )
        }
    }
}

@Composable
private fun TagCard(
    modifier: Modifier,
    content: String,
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Text(
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
                .align(CenterHorizontally),
            text = content,
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            style = TextStyle(
                platformStyle = noFontPadding,
                letterSpacing = 0.15.sp,
            ),
        )
    }
}
package com.bigbratan.rayvue.ui.main.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import com.bigbratan.rayvue.ui.views.ErrorMessage
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.Popup
import com.bigbratan.rayvue.ui.views.TransparentIconButton
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ReviewsScreen(
    viewModel: ReviewsViewModel = hiltViewModel(),
    gameId: String,
    gameName: String,
    gameIcon: String,
    onBackClick: () -> Unit,
) {
    val obtainedReviewsState = viewModel.obtainedReviewsState.collectAsState()
    val sentReviewState = viewModel.sentReviewState.collectAsState()
    val isUserLoggedIn = viewModel.isUserLoggedIn.collectAsState()
    val hasUserReviewedGame = viewModel.hasUserReviewedGame.collectAsState()
    val isUserAccredited = viewModel.isUserAccredited.collectAsState()
    val isRefreshing = viewModel.isRefreshing.collectAsState()
    val listState = rememberLazyListState()

    val typedReviewState = remember { mutableStateOf(TextFieldValue()) }
    val focusManager = LocalFocusManager.current

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value,
        onRefresh = {
            if (obtainedReviewsState.value !is ObtainedReviewsState.Loading &&
                sentReviewState.value !is SentReviewState.Loading
            ) {
                viewModel.resetSentState()
                viewModel.resetReceivedState()
                viewModel.getReviews(gameId)
            }
        }
    )

    LaunchedEffect(gameId) {
        viewModel.getReviews(
            gameId = gameId,
            canRefresh = false,
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastIndex ->
                val totalItemCount =
                    (obtainedReviewsState.value as? ObtainedReviewsState.Success)?.reviews?.size
                        ?: 0

                if (lastIndex != null && lastIndex >= totalItemCount - 1) {
                    viewModel.getReviews(
                        gameId,
                        canRefresh = false,
                        canLoadMore = true
                    )
                }
            }
    }

    Box(
        Modifier
            .pullRefresh(pullRefreshState)
            .fillMaxSize()
    ) {
        when (obtainedReviewsState.value) {
            is ObtainedReviewsState.Loading -> {
                LoadingAnimation(
                    modifier = Modifier.fillMaxSize()
                )
            }

            is ObtainedReviewsState.Error -> {
                ErrorMessage(
                    message = stringResource(
                        id = R.string.reviews_get_data_error_message
                    ),
                    isInHomeScreen = false,
                    onBackClick = { onBackClick() }
                )
            }

            is ObtainedReviewsState.Success -> {
                val reviews = (obtainedReviewsState.value as ObtainedReviewsState.Success).reviews

                var isPopupVisible by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (sentReviewState.value) {
                        SentReviewState.Idle -> {
                            Unit
                        }

                        SentReviewState.Loading -> {
                            Unit
                        }

                        SentReviewState.Success -> {
                            Unit
                        }

                        SentReviewState.Error -> {
                            isPopupVisible = true
                        }
                    }

                    ReviewsView(
                        gameName = gameName,
                        gameIcon = gameIcon,
                        reviews = reviews,
                        typedReviewState = typedReviewState,
                        focusManager = focusManager,
                        isUserLoggedIn = isUserLoggedIn.value,
                        hasUserReviewedGame = hasUserReviewedGame.value,
                        isUserAccredited = isUserAccredited.value,
                        listState = listState,
                        onBackClick = onBackClick,
                        onSendClick = { typedReview ->
                            viewModel.addReview(
                                gameId = gameId,
                                content = typedReview,
                            )
                            typedReviewState.value = TextFieldValue("")
                            focusManager.clearFocus()
                            viewModel.getReviews(
                                gameId = gameId,
                                canRefresh = false,
                            )
                        }
                    )

                    Popup(
                        title = stringResource(id = R.string.error_title),
                        message = stringResource(id = R.string.reviews_send_data_error_message),
                        hasNegativeAction = false,
                        isPopupVisible = isPopupVisible,
                        onConfirm = {
                            isPopupVisible = false
                            viewModel.resetSentState()
                        },
                        onDismiss = {
                            isPopupVisible = false
                            viewModel.resetSentState()
                        }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun ReviewsView(
    gameName: String,
    gameIcon: String,
    reviews: List<ReviewItemViewModel>,
    typedReviewState: MutableState<TextFieldValue>,
    focusManager: FocusManager,
    isUserLoggedIn: Boolean,
    isUserAccredited: Boolean,
    hasUserReviewedGame: Boolean,
    listState: LazyListState,
    onSendClick: (typedReview: String) -> Unit,
    onBackClick: () -> Unit,
) {
    var typedReviewError by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .size(36.dp),
                            model = gameIcon,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                        )

                        Text(
                            modifier = Modifier.padding(end = 16.dp),
                            text = gameName,
                            fontFamily = plusJakartaSans,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(platformStyle = noFontPadding),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    TransparentIconButton(
                        modifier = Modifier.padding(start = 16.dp),
                        imageVector = Icons.Filled.ArrowBack,
                        onClick = { onBackClick() },
                    )
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                if (isUserLoggedIn) {
                    if (isUserAccredited) {
                        if (!hasUserReviewedGame) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 24.dp,
                                        vertical = 12.dp
                                    )
                                    .clip(RoundedCornerShape(8.dp)),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (typedReviewError) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    unfocusedBorderColor = if (typedReviewError) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                ),
                                label = {
                                    Text(
                                        text = stringResource(id = R.string.reviews_write_hint),
                                        fontFamily = plusJakartaSans,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        style = TextStyle(platformStyle = noFontPadding),
                                        maxLines = 1,
                                    )
                                },
                                maxLines = 5,
                                shape = RoundedCornerShape(8.dp),
                                trailingIcon = {
                                    TransparentIconButton(
                                        imageVector = Icons.Default.Send,
                                        onClick = {
                                            if (typedReviewState.value.text.isEmpty()) {
                                                typedReviewError = true
                                            } else {
                                                onSendClick(typedReviewState.value.text)
                                                typedReviewState.value = TextFieldValue("")
                                                keyboardController?.hide()
                                                focusManager.clearFocus()
                                            }
                                        }
                                    )
                                },
                                value = typedReviewState.value,
                                onValueChange = { newValue ->
                                    typedReviewState.value = newValue
                                },
                            )
                        } else {
                            ReviewInfo(text = stringResource(id = R.string.reviews_info_review_exists))
                        }
                    } else {
                        ReviewInfo(text = stringResource(id = R.string.reviews_info_not_accredited))
                    }
                } else {
                    ReviewInfo(text = stringResource(id = R.string.reviews_info_no_account))
                }
            }
        },
        content = { paddingValues ->
            if (reviews.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(id = R.string.reviews_empty_data_error_message),
                        fontFamily = plusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = TextStyle(
                            platformStyle = noFontPadding,
                            letterSpacing = 0.15.sp,
                        ),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    state = listState,
                ) {
                    items(reviews.size) { reviewIndex ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(
                                    top = if (reviewIndex == 0) 12.dp else 0.dp,
                                    bottom = if (reviewIndex == reviews.size - 1) 24.dp else 0.dp,
                                )
                        ) {
                            reviews[reviewIndex].let { review ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = review.userName,
                                        fontFamily = plusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 24.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = TextStyle(
                                            platformStyle = noFontPadding,
                                            letterSpacing = 0.15.sp,
                                        ),
                                    )

                                    Text(
                                        text = review.formattedDate,
                                        fontFamily = plusJakartaSans,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = TextStyle(
                                            platformStyle = noFontPadding,
                                            letterSpacing = 0.15.sp,
                                        ),
                                    )
                                }

                                Text(
                                    modifier = Modifier.padding(top = 12.dp),
                                    text = review.content,
                                    fontFamily = plusJakartaSans,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = TextStyle(
                                        platformStyle = noFontPadding,
                                        letterSpacing = 0.15.sp,
                                    ),
                                )
                            }

                            if (reviewIndex != reviews.size - 1) {
                                Divider(
                                    modifier = Modifier.padding(
                                        vertical = 24.dp,
                                        horizontal = 16.dp
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ReviewInfo(
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 16.dp)
                .padding(vertical = 8.dp),
            imageVector = Icons.Filled.Info,
            contentDescription = null,
        )

        Text(
            text = text,
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            style = TextStyle(
                platformStyle = noFontPadding,
                letterSpacing = 0.15.sp,
            ),
        )
    }
}
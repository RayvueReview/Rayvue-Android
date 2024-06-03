package com.bigbratan.rayvue.ui.main.journalEntry

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.models.JournalEntry
import com.bigbratan.rayvue.ui.main.games.SearchBarButton
import com.bigbratan.rayvue.ui.main.search.MIN_QUERY_LENGTH
import com.bigbratan.rayvue.ui.main.search.SearchViewModel
import com.bigbratan.rayvue.ui.main.search.SearchedGameCard
import com.bigbratan.rayvue.ui.main.search.SearchedGamesState
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import com.bigbratan.rayvue.ui.views.BackNavigationBar
import com.bigbratan.rayvue.ui.views.ErrorMessage
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.Popup
import com.bigbratan.rayvue.ui.views.TonalTextButton
import com.bigbratan.rayvue.ui.views.TransparentIconButton
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
internal fun EntryScreen(
    viewModel: EntryViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    gameId: String = "",
    gameName: String = "",
    gameIcon: String = "",
    onBackClick: () -> Unit,
) {
    val entryAlreadyExists by viewModel.entryAlreadyExists.collectAsState()
    val sentEntryState = viewModel.sentEntryState.collectAsState()
    val currentEntryState by viewModel.currentEntryState.collectAsState()
    val userIdState by viewModel.userIdState.collectAsState()

    val typedContentState = remember { mutableStateOf(TextFieldValue()) }
    val selectedGameState = remember { mutableStateOf<Game?>(null) }
    val originalGameState = remember { mutableStateOf<Game?>(null) }
    val pendingGameState = remember { mutableStateOf<Game?>(null) }

    val sheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )
    val snackbarHostState = remember { SnackbarHostState() }

    var isErrorPopupVisible by remember { mutableStateOf(false) }
    val isGameConfirmationPopupVisible = remember { mutableStateOf(false) }
    var isSuccessSnackbarVisible by remember { mutableStateOf(false) }
    val isSendButtonEnabled = remember(
        selectedGameState.value,
        typedContentState.value.text
    ) {
        (selectedGameState.value != null || gameId.isNotEmpty() && gameIcon.isNotEmpty() && gameName.isNotEmpty()) && typedContentState.value.text.isNotBlank()
    }

    val successMessage = stringResource(id = R.string.entry_send_data_success_message)
    val successConfirmMessage = stringResource(id = R.string.action_positive_title)

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(currentEntryState) {
        currentEntryState?.let { entry ->
            typedContentState.value = TextFieldValue(entry.content)
        }
    }

    LaunchedEffect(isSuccessSnackbarVisible) {
        if (isSuccessSnackbarVisible) {
            val result = snackbarHostState.showSnackbar(
                message = successMessage,
                actionLabel = successConfirmMessage,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                isSuccessSnackbarVisible = false
            }
        }
    }

    LaunchedEffect(
        key1 = gameId,
        key2 = gameName,
        key3 = gameIcon
    ) {
        if (gameId.isNotEmpty() && gameName.isNotEmpty() && gameIcon.isNotEmpty() && gameId != "{gameId}" && gameName != "{gameName}" && gameIcon != "{gameIcon}") {
            selectedGameState.value = Game(
                id = gameId,
                displayName = gameName,
                icon = gameIcon,
                banner = "",
            )

            viewModel.loadEntry(gameId)
        }
    }

    BottomSheetScaffold(
        scaffoldState = sheetScaffoldState,
        sheetShape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp),
        sheetBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        backgroundColor = MaterialTheme.colorScheme.surface,
        sheetElevation = 0.dp,
        drawerElevation = 0.dp,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            SearchView(
                searchViewModel = searchViewModel,
                onGameClick = { game ->
                    pendingGameState.value = game
                    originalGameState.value = selectedGameState.value

                    viewModel.checkEntryAlreadyExists(game.id)
                    coroutineScope.launch {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        sheetScaffoldState.bottomSheetState.collapse()
                    }
                },
                isSheetExpanded = sheetScaffoldState.bottomSheetState.isExpanded,
            )
        },
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                when (sentEntryState.value) {
                    SentEntryState.Idle -> {
                        Unit
                    }

                    SentEntryState.Loading -> {
                        Unit
                    }

                    SentEntryState.Success -> {
                        isSuccessSnackbarVisible = true
                        viewModel.resetSentState()
                    }

                    SentEntryState.Error -> {
                        isErrorPopupVisible = true
                    }
                }

                LaunchedEffect(entryAlreadyExists) {
                    if (entryAlreadyExists == true) {
                        isGameConfirmationPopupVisible.value = true
                    } else if (entryAlreadyExists == false) {
                        isGameConfirmationPopupVisible.value = false

                        if (pendingGameState.value != null)
                            selectedGameState.value = pendingGameState.value
                    }
                }

                EntryView(
                    game = selectedGameState.value,
                    typedEntryState = typedContentState,
                    focusManager = focusManager,
                    isButtonEnabled = isSendButtonEnabled,
                    onSearchClick = {
                        coroutineScope.launch {
                            sheetScaffoldState.bottomSheetState.expand()
                        }
                    },
                    onDeleteClick = {
                        selectedGameState.value = null
                    },
                    onSaveClick = { existingContent ->
                        if (isSendButtonEnabled) {
                            val journalEntry =
                                if (gameId.isNotEmpty()) {
                                    currentEntryState?.let { currentEntry ->
                                        selectedGameState.value?.let { selectedGameId ->
                                            JournalEntry(
                                                userId = userIdState,
                                                gameId = selectedGameId.id,
                                                id = currentEntry.id,
                                                content = existingContent,
                                                dateAdded = Timestamp.now(),
                                            )
                                        }
                                    }
                                } else {
                                    selectedGameState.value?.id?.let { selectedGameId ->
                                        JournalEntry(
                                            userId = userIdState,
                                            gameId = selectedGameId,
                                            id = UUID.randomUUID().toString(),
                                            content = typedContentState.value.text,
                                            dateAdded = Timestamp.now(),
                                        )
                                    }
                                }

                            if (journalEntry != null) {
                                viewModel.saveEntry(journalEntry)
                                viewModel.uploadEntry(journalEntry)
                            }
                        }
                    },
                    onBackClick = onBackClick,
                )

                Popup(
                    title = stringResource(id = R.string.error_title),
                    message = stringResource(id = R.string.entry_send_data_error_message),
                    hasNegativeAction = false,
                    isPopupVisible = isErrorPopupVisible,
                    onConfirm = {
                        isErrorPopupVisible = false
                        viewModel.resetSentState()
                    },
                    onDismiss = {
                        isErrorPopupVisible = false
                        viewModel.resetSentState()
                    }
                )

                if (isGameConfirmationPopupVisible.value) {
                    Popup(
                        title = stringResource(id = R.string.question_title),
                        message = stringResource(id = R.string.entry_replace_confirm_message),
                        hasNegativeAction = true,
                        isPopupVisible = isGameConfirmationPopupVisible.value,
                        onConfirm = {
                            selectedGameState.value = pendingGameState.value
                            isGameConfirmationPopupVisible.value = false
                            viewModel.resetEntryAlreadyExists()
                        },
                        onDismiss = {
                            pendingGameState.value = null
                            selectedGameState.value = originalGameState.value
                            isGameConfirmationPopupVisible.value = false
                            viewModel.resetEntryAlreadyExists()
                        }
                    )
                } else {
                    if (pendingGameState.value != null)
                        selectedGameState.value = pendingGameState.value
                }

                SnackbarHost(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp),
                    hostState = snackbarHostState,
                ) { snackbarData: SnackbarData ->
                    Snackbar(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        snackbarData = snackbarData,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        actionColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchView(
    searchViewModel: SearchViewModel,
    onGameClick: (game: Game) -> Unit,
    isSheetExpanded: Boolean,
) {
    val searchedGamesState = searchViewModel.searchedGamesState.collectAsState()
    val typedQueryState = remember { mutableStateOf(TextFieldValue()) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isSheetExpanded) {
        if (isSheetExpanded) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxHeight(0.7f)) {
        item {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 16.dp
                    )
                    .focusRequester(focusRequester),
                singleLine = true,
                value = typedQueryState.value,
                onValueChange = { newValue ->
                    typedQueryState.value = newValue
                    if (typedQueryState.value.text.length > MIN_QUERY_LENGTH)
                        searchViewModel.searchGames(newValue.text)
                },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontFamily = plusJakartaSans,
                    color = MaterialTheme.colorScheme.onSurface,
                    platformStyle = noFontPadding,
                ),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_general_placeholder),
                        fontSize = 16.sp,
                        fontFamily = plusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = TextStyle(platformStyle = noFontPadding)
                    )
                },
                trailingIcon = {
                    if (typedQueryState.value.text.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                typedQueryState.value = TextFieldValue()
                                searchViewModel.searchGames(typedQueryState.value.text)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                contentDescription = null,
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (typedQueryState.value.text.length > MIN_QUERY_LENGTH)
                            searchViewModel.searchGames(typedQueryState.value.text)
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
            )
        }

        when (searchedGamesState.value) {
            is SearchedGamesState.Idle -> {
                Unit
            }

            is SearchedGamesState.Loading -> {
                item {
                    LoadingAnimation(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            is SearchedGamesState.Error -> {
                item {
                    ErrorMessage(
                        message = stringResource(
                            id = R.string.games_get_data_error_message
                        ),
                        isInHomeScreen = true,
                        onBackClick = { }
                    )
                }
            }

            is SearchedGamesState.Success -> {
                val games =
                    if (typedQueryState.value.text.length > 3) (searchedGamesState.value as SearchedGamesState.Success).games else null

                if (games != null) {
                    items(games.size) { gameIndex ->
                        val game = games[gameIndex]

                        SearchedGameCard(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(
                                    top = if (gameIndex == 0) 16.dp else 4.dp,
                                    bottom = if (gameIndex == games.size) 16.dp else 4.dp
                                ),
                            game = game,
                            onGameClick = { onGameClick(game) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryView(
    game: Game?,
    typedEntryState: MutableState<TextFieldValue>,
    focusManager: FocusManager,
    isButtonEnabled: Boolean,
    onSearchClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: (typedEntry: String) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BackNavigationBar(
                title = stringResource(id = R.string.entry_title),
                onBackClick = { onBackClick() },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(
                    horizontal = 24.dp,
                    vertical = 12.dp
                )
                .fillMaxSize()
        ) {
            if (game == null) {
                SearchBarButton(
                    placeholder = stringResource(id = R.string.entry_select_placeholder),
                    shape = RoundedCornerShape(64.dp),
                    onClick = onSearchClick,
                )
            } else {
                SelectedGameCard(
                    modifier = Modifier.fillMaxWidth(),
                    icon = game.icon ?: "",
                    title = game.displayName ?: "",
                    onGameClick = onSearchClick,
                    onDeleteClick = onDeleteClick,
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .clipToBounds()
                    .weight(1f),
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(top = 12.dp)
                        .requiredWidth(maxWidth + 16.dp)
                        .offset(x = (-8).dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = plusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        platformStyle = noFontPadding,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.entry_write_hint),
                            fontFamily = plusJakartaSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp,
                            style = TextStyle(platformStyle = noFontPadding),
                            maxLines = 1,
                        )
                    },
                    value = typedEntryState.value,
                    onValueChange = { newValue ->
                        typedEntryState.value = newValue
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RectangleShape,
                )
            }

            TonalTextButton(
                modifier = Modifier.padding(bottom = 12.dp),
                label = stringResource(id = R.string.entry_button_save_title),
                onClick = {
                    if (typedEntryState.value.text.isNotEmpty()) {
                        onSaveClick(typedEntryState.value.text)
                        typedEntryState.value = TextFieldValue("")
                        focusManager.clearFocus()
                    }
                },
                isButtonEnabled = isButtonEnabled
            )
        }
    }
}

@Composable
private fun SelectedGameCard(
    modifier: Modifier,
    icon: String,
    title: String,
    onGameClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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

        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
            text = title,
            fontFamily = plusJakartaSans,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            style = TextStyle(platformStyle = noFontPadding),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        TransparentIconButton(
            imageVector = Icons.Filled.Close,
            onClick = onDeleteClick
        )
    }
}
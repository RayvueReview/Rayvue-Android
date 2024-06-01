package com.bigbratan.rayvue.ui.main.journalEntry

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun EntryScreen(
    viewModel: EntryViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    gameId: String = "",
    gameName: String = "",
    gameIcon: String = "",
    onBackClick: () -> Unit,
) {
    val sentEntryState = viewModel.sentEntryState.collectAsState()
    val currentJournalEntry by viewModel.currentJournalEntry.collectAsState()
    val selectedGameState = remember { mutableStateOf<Game?>(null) }
    val typedEntryState = remember { mutableStateOf(TextFieldValue()) }
    val sheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    var isPopupVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    /*LaunchedEffect(selectedGameState.value) {
        selectedGameState.value?.let { game ->
            viewModel.loadJournalEntryForGame(game.id)
        }
    }*/

    LaunchedEffect(currentJournalEntry) {
        currentJournalEntry?.let { entry ->
            typedEntryState.value = TextFieldValue(entry.content)
        }
    }

    LaunchedEffect(
        key1 = gameId,
        key2 = gameName,
        key3 = gameIcon
    ) {
        if (gameId.isNotEmpty() && gameName.isNotEmpty() && gameIcon.isNotEmpty()) {
            selectedGameState.value = Game(
                id = gameId,
                displayName = gameName,
                icon = gameIcon,
                banner = "",
            )
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
            Box {
                SearchView(
                    searchViewModel = searchViewModel,
                    onGameClick = { game ->
                        selectedGameState.value = game

                        coroutineScope.launch {
                            sheetScaffoldState.bottomSheetState.collapse()
                        }
                    },
                )
            }
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
                        Unit
                    }

                    SentEntryState.Error -> {
                        isPopupVisible = true
                    }
                }

                EntryView(
                    selectedGame = selectedGameState.value,
                    typedEntryState = typedEntryState,
                    focusManager = focusManager,
                    onSearchClick = {
                        coroutineScope.launch {
                            sheetScaffoldState.bottomSheetState.expand()
                        }
                    },
                    onDeleteClick = {
                        selectedGameState.value = null
                    },
                    onSaveClick = {
                        val journalEntry = selectedGameState.value?.id?.let { gameId ->
                            JournalEntry(
                                userId = viewModel.userIdState.value,
                                gameId = gameId,
                                id = UUID.randomUUID().toString(),
                                content = typedEntryState.value.text,
                                dateAdded = Timestamp.now(),
                            )
                        }

                        if (journalEntry != null) {
                            viewModel.saveJournalEntry(journalEntry)

                            Log.d("screen", "Journal entry saved: $journalEntry")
                            viewModel.uploadJournalEntry(journalEntry)
                        }
                    },
                    onBackClick = onBackClick,
                )

                Popup(
                    title = stringResource(id = R.string.error_title),
                    message = stringResource(id = R.string.entry_send_data_error_message),
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
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchView(
    searchViewModel: SearchViewModel,
    onGameClick: (game: Game) -> Unit,
) {
    val searchedGamesState = searchViewModel.searchedGamesState.collectAsState()
    val typedQueryState = remember { mutableStateOf(TextFieldValue()) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var textFieldLoaded by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxHeight(0.7f)) {
        item {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .focusRequester(focusRequester),
                singleLine = true,
                value = typedQueryState.value,
                onValueChange = { newValue ->
                    typedQueryState.value = newValue
                    if (typedQueryState.value.text.length > MIN_QUERY_LENGTH)
                        searchViewModel.searchGames(newValue.text)
                },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    fontFamily = plusJakartaSans,
                    color = MaterialTheme.colorScheme.onSurface,
                    platformStyle = noFontPadding,
                ),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_general_placeholder),
                        fontSize = 14.sp,
                        fontFamily = plusJakartaSans,
                        fontWeight = FontWeight(500),
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun EntryView(
    selectedGame: Game?,
    typedEntryState: MutableState<TextFieldValue>,
    focusManager: FocusManager,
    onSearchClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: (typedEntry: String) -> Unit,
    onBackClick: () -> Unit,
) {
    var typedEntryError by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize(),
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
                .padding(24.dp)
                .fillMaxSize()
        ) {
            if (selectedGame == null) {
                SearchBarButton(
                    placeholder = stringResource(id = R.string.entry_select_placeholder),
                    shape = RoundedCornerShape(64.dp),
                    onClick = onSearchClick,
                )
            } else {
                SelectedGameCard(
                    modifier = Modifier.fillMaxWidth(),
                    icon = selectedGame.icon ?: "",
                    title = selectedGame.displayName ?: "",
                    onGameClick = onSearchClick,
                    onDeleteClick = onDeleteClick,
                )
            }

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = 12.dp)
                    .weight(1f),
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

            TonalTextButton(
                label = stringResource(id = R.string.entry_button_save_title),
                onClick = {
                    if (typedEntryState.value.text.isEmpty()) {
                        typedEntryError = true
                    } else {
                        onSaveClick(typedEntryState.value.text)
                        typedEntryState.value = TextFieldValue("")
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                },
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

/*SelectedGameCard(
                    modifier = Modifier.fillMaxWidth(),
                    icon = selectedGame!!.icon,
                    title = selectedGame!!.displayName,
                    // TODO: DO NOT USE DOUBLE BANG FOR selectedGame, THERE HAS TO BE A BETTER WAY
                    // ALSO, IF gameId + gameName + gameIcon, OBTAINED FROM THE JOURNAL SCREEN, ARE NOT NULL OR EMPTY
                    // USE THEM TO POPULATE THE SelectedGameCard (INSTEAD OF selectedGame)
                    onGameClick = { sheetScaffoldState.bottomSheetState.expand() },
                    onDeleteClick = {
                        selectedGame = null
                    } // TODO: CLEAR THE SELECTED GAME FOR BOTH INSTANCES (SELECTED GAME FROM SEARCH OR OBTAINED GAME DATA FROM SCREEN)
                )*/
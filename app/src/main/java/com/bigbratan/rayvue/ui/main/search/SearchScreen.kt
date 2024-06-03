package com.bigbratan.rayvue.ui.main.search

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import com.bigbratan.rayvue.ui.views.ErrorMessage
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.TransparentIconButton

const val MIN_QUERY_LENGTH = 3

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onGameClick: (gameId: String) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        val searchedGamesState = viewModel.searchedGamesState.collectAsState()
        val typedQueryState = remember { mutableStateOf(TextFieldValue()) }

        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        var textFieldLoaded by remember { mutableStateOf(false) }

        LazyColumn {
            item {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp)
                                .focusRequester(focusRequester)
                                .onGloballyPositioned {
                                    if (!textFieldLoaded) {
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                        textFieldLoaded = true
                                    }
                                },
                            singleLine = true,
                            value = typedQueryState.value,
                            onValueChange = { newValue ->
                                typedQueryState.value = newValue
                                if (typedQueryState.value.text.length > MIN_QUERY_LENGTH)
                                    viewModel.searchGames(newValue.text)
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
                                            viewModel.searchGames(typedQueryState.value.text)
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
                                        viewModel.searchGames(typedQueryState.value.text)
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
                    },
                    navigationIcon = {
                        TransparentIconButton(
                            modifier = Modifier.padding(start = 16.dp),
                            imageVector = Icons.Filled.ArrowBack,
                            onClick = { onBackClick() },
                        )
                    }
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
                                onGameClick = { onGameClick(game.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SearchedGameCard(
    modifier: Modifier,
    game: Game,
    onGameClick: (gameId: String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                onClick = {
                    onGameClick(game.id)
                }
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .size(56.dp),
            model = game.icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = game.displayName,
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
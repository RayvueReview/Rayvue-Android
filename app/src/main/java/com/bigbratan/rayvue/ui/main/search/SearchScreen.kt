package com.bigbratan.rayvue.ui.main.search

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.bigbratan.rayvue.ui.main.games.MAIN_GRID_SIZE
import com.bigbratan.rayvue.ui.theme.noFontPadding
import com.bigbratan.rayvue.ui.theme.plusJakartaSans
import com.bigbratan.rayvue.ui.views.ErrorMessage
import com.bigbratan.rayvue.ui.views.LoadingAnimation
import com.bigbratan.rayvue.ui.views.TransparentIconButton

const val SEARCH_GRID_SIZE = 1

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onGameClick: (gameId: String) -> Unit,
    onBackClick: () -> Unit,
) {
    val searchedGamesState = viewModel.searchedGamesState.collectAsState()
    val typedQueryState = remember { mutableStateOf(TextFieldValue()) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(SEARCH_GRID_SIZE)
        ) {
            item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .padding(start = 16.dp),
                            singleLine = true,
                            value = typedQueryState.value,
                            onValueChange = { newValue ->
                                typedQueryState.value = newValue

                                if (typedQueryState.value.text.length > 3)
                                    viewModel.searchGames(newValue.text)
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
                                    viewModel.searchGames(
                                        typedQueryState.value.text
                                    )
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
                    item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
                        LoadingAnimation(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                is SearchedGamesState.Error -> {
                    item(span = { GridItemSpan(MAIN_GRID_SIZE) }) {
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
                    val games = (searchedGamesState.value as SearchedGamesState.Success).games

                    items(games.size) { gameIndex ->
                        val game = games[gameIndex]

                        SearchedGameCard(
                            game = game,
                            onGameClick = { onGameClick(game.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchedGameCard(
    game: Game,
    onGameClick: (gameId: String) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
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
            text = game.name,
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
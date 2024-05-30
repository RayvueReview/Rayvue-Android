package com.bigbratan.rayvue.ui.main.games

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.services.GamesService
import com.bigbratan.rayvue.ui.main.reviews.DEBOUNCE_COOLDOWN
import com.bigbratan.rayvue.ui.main.reviews.LAST_FETCH
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val gamesService: GamesService,
) : ViewModel() {
    val obtainedGamesState =
        MutableStateFlow<ObtainedGamesState>(ObtainedGamesState.Loading)
    val obtainedFilteredGamesState =
        MutableStateFlow<ObtainedFilteredGamesState>(ObtainedFilteredGamesState.Loading)

    val isRefreshing = MutableStateFlow(false)
    private var isLoadingMoreGames = false
    private var lastGame: DocumentSnapshot? = null
    private var fetchJob: Job? = null

    private var lastFetchTimeMillis = LAST_FETCH
    private val fetchCooldownMillis = DEBOUNCE_COOLDOWN

    fun getFilteredGames(
        canRefresh: Boolean = true,
    ) {
        viewModelScope.launch {
            obtainedFilteredGamesState.value = ObtainedFilteredGamesState.Loading
            if (canRefresh) isRefreshing.value = true

            try {
                val recentGames = gamesService.fetchRecentGames(10)
                val randomGames = gamesService.fetchRandomGames("all", 10)

                obtainedFilteredGamesState.value = ObtainedFilteredGamesState.Success(
                    recentGames,
                    randomGames,
                )
            } catch (e: Exception) {
                obtainedFilteredGamesState.value = ObtainedFilteredGamesState.Error
            } finally {
                if (canRefresh) isRefreshing.value = false
            }
        }
    }

    fun getAllGames(
        canRefresh: Boolean = true,
        canLoadMore: Boolean = false,
    ) {
        val currentTime = System.currentTimeMillis()
        if (canLoadMore && isLoadingMoreGames && currentTime - lastFetchTimeMillis < fetchCooldownMillis) return

        isLoadingMoreGames = true
        lastFetchTimeMillis = currentTime
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            if (canRefresh) isRefreshing.value = true

            try {
                if (!canLoadMore) obtainedGamesState.value =
                    ObtainedGamesState.Loading.apply { lastGame = null }

                val (games, lastSnapshot) = gamesService.fetchAllGames(
                    4,
                    if (canLoadMore) lastGame else null
                )
                lastGame = lastSnapshot.takeIf { games.isNotEmpty() }
                val mergedGames = mergeGames(games, canLoadMore)

                obtainedGamesState.value = ObtainedGamesState.Success(mergedGames)
            } catch (e: Exception) {
                obtainedGamesState.value = ObtainedGamesState.Error
            } finally {
                if (canRefresh) isRefreshing.value = false

                isLoadingMoreGames = false
            }
        }
    }

    private fun mergeGames(
        newGames: List<Game>,
        canLoadMore: Boolean
    ): List<Game> {
        val currentGames =
            (obtainedGamesState.value as? ObtainedGamesState.Success)?.allGames.orEmpty()

        return if (!canLoadMore) {
            newGames
        } else {
            val existingIds = currentGames.map { it.id }.toSet()
            val newFilteredGames = newGames.filter { it.id !in existingIds }
            currentGames + newFilteredGames
        }
    }

    fun resetStates(keepLastSnapshot: Boolean = false) {
        obtainedGamesState.value = ObtainedGamesState.Loading
        obtainedFilteredGamesState.value = ObtainedFilteredGamesState.Loading

        if (!keepLastSnapshot) {
            lastGame = null
        }
    }
}

sealed class ObtainedGamesState {
    object Loading : ObtainedGamesState()

    data class Success(
        val allGames: List<Game>,
    ) : ObtainedGamesState()

    object Error : ObtainedGamesState()
}

sealed class ObtainedFilteredGamesState {
    object Loading : ObtainedFilteredGamesState()

    data class Success(
        val recentGames: List<Game>,
        val randomGames: List<Game>,
    ) : ObtainedFilteredGamesState()

    object Error : ObtainedFilteredGamesState()
}

/*fun getAllGames(
        canRefresh: Boolean = true,
        loadMore: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                if (canRefresh)
                    isRefreshing.value = true

                val (allGames, lastSnapshot) = gamesService.fetchAllGames(10, lastGame)
                val recentGames = gamesService.fetchRecentGames(10)
                val randomGames = gamesService.fetchRandomGames("all", 10)
                lastGame = lastSnapshot

                val newAllGames =
                    if (loadMore && obtainedAllGamesState.value is ObtainedAllGamesState.Success) {
                        (obtainedAllGamesState.value as ObtainedAllGamesState.Success).allGames + allGames
                    } else {
                        allGames
                    }

                obtainedAllGamesState.value =
                    ObtainedAllGamesState.Success(
                        recentGames,
                        randomGames,
                        newAllGames,
                    )
            } catch (e: Exception) {
                if (canRefresh)
                    isRefreshing.value = true

                obtainedAllGamesState.value = ObtainedAllGamesState.Error
            } finally {
                if (canRefresh)
                    isRefreshing.value = false
            }
        }
    }*/
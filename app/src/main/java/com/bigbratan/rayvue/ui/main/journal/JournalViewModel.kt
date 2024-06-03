package com.bigbratan.rayvue.ui.main.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.services.GamesService
import com.bigbratan.rayvue.services.JournalService
import com.bigbratan.rayvue.services.LocalStorageService
import com.bigbratan.rayvue.services.UserService
import com.bigbratan.rayvue.ui.main.reviews.DEBOUNCE_COOLDOWN
import com.bigbratan.rayvue.ui.main.reviews.LAST_FETCH
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val localStorageService: LocalStorageService,
    private val journalService: JournalService,
    private val gamesService: GamesService,
    private val userService: UserService,
) : ViewModel() {
    val obtainedJournalGamesState =
        MutableStateFlow<ObtainedJournalGamesState>(ObtainedJournalGamesState.Loading)
    val sentJournalGamesState =
        MutableStateFlow<SentJournalGamesState>(SentJournalGamesState.Idle)
    private val userIdState = MutableStateFlow<String?>(null)

    val isRefreshing = MutableStateFlow(false)
    private var isLoadingMoreGames = false
    private var lastGame: DocumentSnapshot? = null
    private var fetchJob: Job? = null

    private var lastFetchTimeMillis = LAST_FETCH
    private val fetchCooldownMillis = DEBOUNCE_COOLDOWN

    init {
        viewModelScope.launch {
            userService.user.collect { user ->
                userIdState.value = user?.id
            }
        }
    }

    fun getJournalGames(
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
                var loadedEntries = journalService.getLocalJournalEntries()

                if (loadedEntries.isEmpty()) {
                    userIdState.value?.let { userId ->
                        loadedEntries = journalService.getFirebaseJournalEntries(userId)
                    }
                }

                val gameIds = loadedEntries.map { journalEntry ->
                    journalEntry.gameId
                }.distinct()

                if (gameIds.isEmpty()) {
                    obtainedJournalGamesState.value = ObtainedJournalGamesState.Error
                    return@launch
                }

                if (!canLoadMore) obtainedJournalGamesState.value =
                    ObtainedJournalGamesState.Loading.apply { lastGame = null }

                val (games, lastSnapshot) = gamesService.fetchJournalGames(
                    gameIds,
                    10,
                    if (canLoadMore) lastGame else null
                )
                lastGame = lastSnapshot.takeIf { games.isNotEmpty() }
                val mergedGames = mergeGames(games, canLoadMore)

                obtainedJournalGamesState.value = ObtainedJournalGamesState.Success(mergedGames)
            } catch (e: Exception) {
                obtainedJournalGamesState.value = ObtainedJournalGamesState.Error
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
            (obtainedJournalGamesState.value as? ObtainedJournalGamesState.Success)?.journalGames.orEmpty()

        return if (!canLoadMore) {
            newGames
        } else {
            val existingIds = currentGames.map { it.id }.toSet()
            val newFilteredGames = newGames.filter { it.id !in existingIds }
            currentGames + newFilteredGames
        }
    }

    fun uploadAllJournalEntries() {
        viewModelScope.launch {
            sentJournalGamesState.value = SentJournalGamesState.Loading

            userIdState.value?.let { userId ->
                try {
                    journalService.updateAndUploadJournalEntries(userId)

                    sentJournalGamesState.value = SentJournalGamesState.Success
                } catch (e: Exception) {
                    sentJournalGamesState.value = SentJournalGamesState.Error
                }
            } ?: run {
                sentJournalGamesState.value = SentJournalGamesState.Error
            }
        }
    }

    fun resetObtainedState() {
        obtainedJournalGamesState.value = ObtainedJournalGamesState.Loading
    }
}

sealed class ObtainedJournalGamesState {
    object Loading : ObtainedJournalGamesState()

    data class Success(
        val journalGames: List<Game>,
    ) : ObtainedJournalGamesState()

    object Error : ObtainedJournalGamesState()
}

sealed class SentJournalGamesState {
    object Idle : SentJournalGamesState()

    object Loading : SentJournalGamesState()

    object Success : SentJournalGamesState()

    object Error : SentJournalGamesState()
}

sealed class ObtainedJournalUserState {
    object Loading : ObtainedJournalUserState()

    data class Success(
        val userId: String?,
    ) : ObtainedJournalUserState()

    object Error : ObtainedJournalUserState()
}
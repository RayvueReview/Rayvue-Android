package com.bigbratan.rayvue.ui.main.awards

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.models.AwardGame
import com.bigbratan.rayvue.services.GamesService
import com.bigbratan.rayvue.ui.main.reviews.DEBOUNCE_COOLDOWN
import com.bigbratan.rayvue.ui.main.reviews.LAST_FETCH
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class DateType(val dateType: String) {
    WEEK("gameOfTheWeek"),
    MONTH("gameOfTheMonth"),
    YEAR("gameOfTheYear"),
}

@HiltViewModel
class AwardsViewModel @Inject constructor(
    private val gamesService: GamesService,
) : ViewModel() {
    val obtainedTopGamesState =
        MutableStateFlow<ObtainedTopGamesState>(ObtainedTopGamesState.Loading)
    val selectedDateType = mutableStateOf(DateType.MONTH)

    val isRefreshing = MutableStateFlow(false)
    private var isLoadingMoreGames = false
    private var lastGame: DocumentSnapshot? = null
    private var fetchJob: Job? = null

    private var lastFetchTimeMillis = LAST_FETCH
    private val fetchCooldownMillis = DEBOUNCE_COOLDOWN

    fun getTopGames(
        dateType: String,
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
                if (!canLoadMore) obtainedTopGamesState.value =
                    ObtainedTopGamesState.Loading.apply { lastGame = null }

                val (games, lastSnapshot) = gamesService.fetchTopGames(
                    dateType,
                    4,
                    if (canLoadMore) lastGame else null
                )
                lastGame = lastSnapshot.takeIf { games.isNotEmpty() }

                val gamesVMs = games.map(::AwardGameItemViewModel)
                val mergedGames = mergeGames(gamesVMs, canLoadMore)

                if (mergedGames.isEmpty()) {
                    obtainedTopGamesState.value = ObtainedTopGamesState.Error
                } else {
                    obtainedTopGamesState.value = ObtainedTopGamesState.Success(mergedGames)
                }
            } catch (e: Exception) {
                obtainedTopGamesState.value = ObtainedTopGamesState.Error
            } finally {
                if (canRefresh) isRefreshing.value = false

                isLoadingMoreGames = false
            }
        }
    }

    private fun mergeGames(
        newGames: List<AwardGameItemViewModel>,
        canLoadMore: Boolean
    ): List<AwardGameItemViewModel> {
        val currentGames =
            (obtainedTopGamesState.value as? ObtainedTopGamesState.Success)?.topGames.orEmpty()

        return if (!canLoadMore) {
            newGames
        } else {
            val existingIds = currentGames.map { it.id }.toSet()
            val newFilteredGames = newGames.filter { it.id !in existingIds }
            currentGames + newFilteredGames
        }
    }

    fun resetState() {
        obtainedTopGamesState.value = ObtainedTopGamesState.Loading
    }

    fun onTabSelected(newDateType: DateType) {
        if (selectedDateType.value != newDateType) {
            selectedDateType.value = newDateType
            resetState()
            getTopGames(
                dateType = newDateType.dateType,
                canRefresh = false
            )
        }
    }
}

sealed class ObtainedTopGamesState {
    object Loading : ObtainedTopGamesState()

    data class Success(
        val topGames: List<AwardGameItemViewModel>,
    ) : ObtainedTopGamesState()

    object Error : ObtainedTopGamesState()
}

data class AwardGameItemViewModel(
    private val awardGame: AwardGame,
) {
    val gameOfTheWeek: String = let {
        val date = awardGame.gameOfTheWeek?.toDate()?.time ?: System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        "Week $weekOfYear of $year"
    }

    val gameOfTheMonth: String = SimpleDateFormat(
        "MMMM yyyy",
        Locale.getDefault()
    ).format(awardGame.gameOfTheMonth?.toDate() ?: System.currentTimeMillis())

    val gameOfTheYear: String = SimpleDateFormat(
        "yyyy",
        Locale.getDefault()
    ).format(awardGame.gameOfTheYear?.toDate() ?: System.currentTimeMillis())

    val id: String = awardGame.id

    val displayName: String = awardGame.displayName

    val icon: String = awardGame.icon

    val banner: String = awardGame.banner
}
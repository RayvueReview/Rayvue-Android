package com.bigbratan.rayvue.ui.main.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.models.Review
import com.bigbratan.rayvue.services.ReviewsService
import com.bigbratan.rayvue.services.UserService
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

const val LAST_FETCH = 0L
const val DEBOUNCE_COOLDOWN = 1000L

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val reviewsService: ReviewsService,
    private val userService: UserService,
) : ViewModel() {
    val obtainedReviewsState = MutableStateFlow<ObtainedReviewsState>(ObtainedReviewsState.Loading)
    val sentReviewState = MutableStateFlow<SentReviewState>(SentReviewState.Idle)

    val isRefreshing = MutableStateFlow(false)
    val isUserLoggedIn = MutableStateFlow(true)
    val hasUserReviewedGame = MutableStateFlow(false)
    val isUserAccredited = MutableStateFlow(false)
    private var isLoadingMoreReviews = false
    private var lastReview: DocumentSnapshot? = null
    private var fetchJob: Job? = null

    private var lastFetchTimeMillis = LAST_FETCH
    private val fetchCooldownMillis = DEBOUNCE_COOLDOWN

    init {
        viewModelScope.launch {
            userService.user.collect { user ->
                isUserLoggedIn.value = user != null

                if (user != null) {
                    isUserAccredited.value = user.isReviewer == true
                }
            }
        }
    }

    fun getReviews(
        gameId: String,
        canRefresh: Boolean = true,
        canLoadMore: Boolean = false,
    ) {
        val currentTime = System.currentTimeMillis()
        if (canLoadMore && isLoadingMoreReviews && currentTime - lastFetchTimeMillis < fetchCooldownMillis) return

        isLoadingMoreReviews = true
        lastFetchTimeMillis = currentTime
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            if (canRefresh) isRefreshing.value = true

            try {
                if (!canLoadMore) obtainedReviewsState.value =
                    ObtainedReviewsState.Loading.apply { lastReview = null }

                val (reviews, lastSnapshot) = reviewsService.fetchReviews(
                    gameId,
                    5,
                    if (canLoadMore) lastReview else null
                )
                lastReview = lastSnapshot.takeIf { reviews.isNotEmpty() }

                val reviewsVMs = reviews.map(::ReviewItemViewModel)
                val mergedReviews = mergeReviews(reviewsVMs, canLoadMore, gameId)

                obtainedReviewsState.value = ObtainedReviewsState.Success(mergedReviews)
            } catch (e: Exception) {
                obtainedReviewsState.value = ObtainedReviewsState.Error
            } finally {
                if (canRefresh) isRefreshing.value = false

                isLoadingMoreReviews = false
            }
        }
    }

    private suspend fun mergeReviews(
        newReviews: List<ReviewItemViewModel>,
        canLoadMore: Boolean,
        gameId: String
    ): List<ReviewItemViewModel> {
        val currentReviews = (obtainedReviewsState.value as? ObtainedReviewsState.Success)?.reviews.orEmpty()

        val currentUserReview = userService.user.value?.id?.let { _ ->
            reviewsService.fetchCurrentUserReview(gameId)?.let { ReviewItemViewModel(it) }
        }

        hasUserReviewedGame.value = currentUserReview != null

        return if (!canLoadMore) {
            currentUserReview?.let { listOf(it) + newReviews } ?: newReviews
        } else {
            val existingIds = currentReviews.map { it.id }.toSet()
            val newFilteredReviews = newReviews.filter { it.id !in existingIds }
            currentReviews + newFilteredReviews
        }
    }

    fun addReview(
        gameId: String,
        content: String,
    ) {
        viewModelScope.launch {
            sentReviewState.value = SentReviewState.Loading

            try {
                reviewsService.addReview(
                    gameId,
                    content,
                )

                sentReviewState.value = SentReviewState.Success
            } catch (e: Exception) {
                sentReviewState.value = SentReviewState.Error
            }
        }
    }

    fun resetSentState() {
        sentReviewState.value = SentReviewState.Idle
    }

    fun resetReceivedState() {
        obtainedReviewsState.value = ObtainedReviewsState.Loading
    }
}

sealed class ObtainedReviewsState {
    object Loading : ObtainedReviewsState()

    data class Success(
        val reviews: List<ReviewItemViewModel>
    ) : ObtainedReviewsState()

    object Error : ObtainedReviewsState()
}

sealed class SentReviewState {
    object Idle : SentReviewState()

    object Loading : SentReviewState()

    object Success : SentReviewState()

    object Error : SentReviewState()
}

data class ReviewItemViewModel(
    private val review: Review
) {
    val formattedDate: String = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(review.dateAdded.toDate())

    val id: String = review.id

    val content: String = review.content

    val userName: String = review.userName
}
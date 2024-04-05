package com.bigbratan.rayvue.ui.main.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.models.Review
import com.bigbratan.rayvue.services.ReviewsService
import com.bigbratan.rayvue.services.UserService
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val reviewsService: ReviewsService,
    private val userService: UserService,
) : ViewModel() {
    val obtainedReviewsState = MutableStateFlow<ObtainedReviewsState>(ObtainedReviewsState.Loading)
    val sentReviewState = MutableStateFlow<SentReviewState>(SentReviewState.Idle)
    val isRefreshing = MutableStateFlow(false)
    private var lastDocumentSnapshot: DocumentSnapshot? = null

    val isUserLoggedIn = MutableStateFlow(true)
    val hasUserReviewedGame = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            userService.user.collect { user ->
                isUserLoggedIn.value = user != null
            }
        }
    }

    fun getData(
        gameId: String,
        canRefresh: Boolean = true,
        loadMore: Boolean = false
    ) {
        viewModelScope.launch {
            if (canRefresh) {
                isRefreshing.value = true
                if (!loadMore) {
                    lastDocumentSnapshot = null
                }
            }

            try {
                if (canRefresh)
                    isRefreshing.value = true

                val (reviews, lastSnapshot) = reviewsService.fetchReviews(
                    gameId = gameId,
                    limit = 10, // Define your limit
                    startAfter = if (loadMore) lastDocumentSnapshot else null
                )
                lastDocumentSnapshot = lastSnapshot

                val reviewsViewModels =
                    reviews.sortedByDescending { it.dateAdded }.map { ReviewItemViewModel(it) }

                if (loadMore) {
                    val currentReviews =
                        if (obtainedReviewsState.value is ObtainedReviewsState.Success) {
                            (obtainedReviewsState.value as ObtainedReviewsState.Success).reviews + reviewsViewModels
                        } else reviewsViewModels
                    obtainedReviewsState.value = ObtainedReviewsState.Success(currentReviews)
                } else {
                    obtainedReviewsState.value = ObtainedReviewsState.Success(reviewsViewModels)
                }

                checkIfUserReviewedGame(gameId)
            } catch (e: Exception) {
                obtainedReviewsState.value = ObtainedReviewsState.Error
            } finally {
                if (canRefresh)
                    isRefreshing.value = false
            }
        }
    }

    private suspend fun checkIfUserReviewedGame(gameId: String) {
        val userId = userService.user.value?.id
        if (userId != null) {
            val currentUserReview = reviewsService.fetchCurrentUserReview(gameId)
            hasUserReviewedGame.value = currentUserReview != null
        } else {
            hasUserReviewedGame.value = false
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
                getData(gameId)

                sentReviewState.value = SentReviewState.Success
            } catch (e: Exception) {
                sentReviewState.value = SentReviewState.Error
            }
        }
    }

    fun resetState() {
        sentReviewState.value = SentReviewState.Idle
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
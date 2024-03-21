package com.bigbratan.rayvue.ui.main.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.models.Review
import com.bigbratan.rayvue.services.ReviewsService
import com.bigbratan.rayvue.services.UserService
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
    val isUserLoggedIn = MutableStateFlow(true)
    val hasUserReviewedGame = MutableStateFlow(false)
    val isRefreshing = MutableStateFlow(false)

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
    ) {
        viewModelScope.launch {
            try {
                if (canRefresh)
                    isRefreshing.value = true

                val userId = userService.user.value?.id
                val reviews = reviewsService.fetchReviews(gameId)
                    .sortedByDescending { review -> review.dateAdded }
                    .map { ReviewItemViewModel(it) }
                val currentUserReview = if (userId != null) {
                    reviewsService.fetchCurrentUserReview(gameId)?.let { ReviewItemViewModel(it) }
                } else {
                    null
                }

                if (currentUserReview != null) {
                    obtainedReviewsState.value =
                        ObtainedReviewsState.Success(listOf(currentUserReview) + reviews)
                    hasUserReviewedGame.value = true
                } else {
                    obtainedReviewsState.value = ObtainedReviewsState.Success(reviews)
                    hasUserReviewedGame.value = false
                }
            } catch (e: Exception) {
                if (canRefresh)
                    isRefreshing.value = true

                obtainedReviewsState.value = ObtainedReviewsState.Error
            } finally {
                if (canRefresh)
                    isRefreshing.value = false
            }
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
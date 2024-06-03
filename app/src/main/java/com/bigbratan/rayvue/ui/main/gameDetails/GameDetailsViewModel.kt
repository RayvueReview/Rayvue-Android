package com.bigbratan.rayvue.ui.main.gameDetails

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.R
import com.bigbratan.rayvue.models.GameDetails
import com.bigbratan.rayvue.services.GamesService
import com.bigbratan.rayvue.services.ReviewsService
import com.bigbratan.rayvue.services.UserService
import com.bigbratan.rayvue.ui.main.reviews.ReviewItemViewModel
import com.bigbratan.rayvue.ui.utils.encodeField
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameDetailsViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val gamesService: GamesService,
    private val reviewsService: ReviewsService,
    private val userService: UserService,
) : ViewModel() {
    val obtainedGameDetailsState =
        MutableStateFlow<ObtainedGameDetailsState>(ObtainedGameDetailsState.Loading)

    fun getData(
        gameId: String,
    ) {
        viewModelScope.launch {
            try {
                val gameDetails = gamesService.fetchGameDetails(gameId)
                val reviews = reviewsService.fetchReviewsOnce(
                    gameId,
                    10,
                ).map(::ReviewItemViewModel)

                obtainedGameDetailsState.value = ObtainedGameDetailsState.Success(
                    GameDetailsItemViewModel(
                        gameDetails.copy(encodedIcon = encodeField(gameDetails.icon)),
                        context
                    ),
                    mergeReviews(reviews, gameId)
                )
            } catch (e: Exception) {
                obtainedGameDetailsState.value = ObtainedGameDetailsState.Error
            }
        }
    }

    private suspend fun mergeReviews(
        reviews: List<ReviewItemViewModel>,
        gameId: String
    ): List<ReviewItemViewModel> {
        val currentUserReview = userService.user.value?.id?.let { _ ->
            reviewsService.fetchCurrentUserReview(gameId)
        }

        return if (currentUserReview != null) {
            listOf(ReviewItemViewModel(currentUserReview)) + reviews
        } else {
            reviews
        }
    }
}

sealed class ObtainedGameDetailsState {
    object Loading : ObtainedGameDetailsState()

    data class Success(
        val gameDetails: GameDetailsItemViewModel,
        val reviews: List<ReviewItemViewModel>,
    ) : ObtainedGameDetailsState()

    object Error : ObtainedGameDetailsState()
}

data class GameDetailsItemViewModel(
    private val gameDetails: GameDetails,
    private val context: Context,
) {
    val id: String = gameDetails.id

    val name: String = gameDetails.displayName

    val encodedIcon: String = gameDetails.encodedIcon

    val banner: String = gameDetails.banner

    val description: String = gameDetails.description

    private val price: String = gameDetails.price.toString()

    val tags: List<String> = buildList {
        add(price)

        addAll(
            gameDetails.tags?.mapNotNull { tag ->
                when (tag) {
                    1 -> context.getString(R.string.game_details_tag_sub_message)
                    2 -> context.getString(R.string.game_details_tag_micro_message)
                    3 -> context.getString(R.string.game_details_tag_pass_message)
                    4 -> context.getString(R.string.game_details_tag_loot_message)
                    5 -> context.getString(R.string.game_details_tag_p2w_message)
                    6 -> context.getString(R.string.game_details_tag_malicious_message)
                    7 -> context.getString(R.string.game_details_tag_tos_message)
                    8 -> context.getString(R.string.game_details_tag_fake_message)
                    9 -> context.getString(R.string.game_details_tag_ad_message)
                    10 -> context.getString(R.string.game_details_tag_dev_message)
                    11 -> context.getString(R.string.game_details_tag_scam_message)
                    else -> null
                }
            } ?: emptyList()
        )
    }

    val categories: List<String> = gameDetails.categories
}
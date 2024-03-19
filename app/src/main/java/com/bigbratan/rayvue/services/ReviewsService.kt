package com.bigbratan.rayvue.services

import com.google.firebase.Timestamp
import com.bigbratan.rayvue.models.Review
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewsService @Inject constructor(
    private val storageService: StorageService,
    private val userService: UserService,
) {
    suspend fun fetchReviews(
        gameId: String,
    ): List<Review> {
        val userId = userService.user.value?.id
        val reviews = storageService.getDocuments<Review>(
            collection = "reviews",
            documentFields = arrayOf(
                "id",
                "gameId",
                "userId",
                "dateAdded",
                "userName",
                "content"
            ),
            filters = mapOf(
                "gameId" to gameId,
                "isUserAccredited" to true
            )
        )

        return if (userId != null) {
            reviews.filterNot { review -> review.userId == userId }
        } else {
            reviews
        }
    }

    suspend fun fetchCurrentUserReview(
        gameId: String,
    ): Review? {
        val userId = userService.user.value?.id

        if (userId != null) {
            return storageService.getDocuments<Review>(
                collection = "reviews",
                documentFields = arrayOf(
                    "id",
                    "gameId",
                    "userId",
                    "dateAdded",
                    "userName",
                    "content"
                ),
                filters = mapOf(
                    "userId" to userId,
                    "gameId" to gameId
                )
            ).firstOrNull()
        } else {
            return null
        }
    }

    suspend fun addReview(
        gameId: String,
        content: String,
    ) {
        val userId = userService.user.value?.id
        val userName = userService.user.value?.userName
        val isReviewer = userService.user.value?.isReviewer
        val reviewId = UUID.randomUUID().toString()

        if (userId != null && userName != null) {
            val reviewData = isReviewer?.let {
                hashMapOf(
                    "id" to reviewId,
                    "gameId" to gameId,
                    "userId" to userId,
                    "dateAdded" to Timestamp.now(),
                    "userName" to userName,
                    "content" to content,
                    "isUserAccredited" to it,
                )
            }

            if (reviewData != null) {
                storageService.addDocument(
                    collection = "reviews",
                    documentId = reviewId,
                    data = reviewData,
                )
            }
        }
    }

    suspend fun updateReview(
        reviewId: String,
        content: String,
    ) {
        val userId = userService.user.value?.id

        if (userId != null) {
            storageService.updateDocument(
                collection = "reviews",
                documentId = reviewId,
                field = "content",
                value = content,
            )
        }
    }

    suspend fun deleteReview(
        reviewId: String,
    ) {
        val userId = userService.user.value?.id

        if (userId != null) {
            storageService.deleteDocument(
                collection = "reviews",
                documentId = reviewId,
            )
        }
    }

    suspend fun deleteReviews(
        userId: String
    ) {
        storageService.deleteDocuments(
            collection = "reviews",
            internalId = "userId",
            matchingId = userId,
        )
    }
}
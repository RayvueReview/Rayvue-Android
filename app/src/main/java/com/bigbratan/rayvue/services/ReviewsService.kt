package com.bigbratan.rayvue.services

import com.bigbratan.rayvue.models.Review
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewsService @Inject constructor(
    private val firebaseStorageService: FirebaseStorageService,
    private val userService: UserService,
) {
    suspend fun fetchReviews(
        gameId: String,
        limit: Long,
        startAfter: DocumentSnapshot? = null
    ): Pair<List<Review>, DocumentSnapshot?> {
        val userId = userService.user.value?.id
        val (reviews, lastSnapshot) = firebaseStorageService.getDocuments<Review>(
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
            ),
            limit = limit,
            startAfter = startAfter
        )

        val filteredReviews = if (userId != null) {
            reviews.filterNot { review -> review.userId == userId }
        } else {
            reviews
        }

        return Pair(filteredReviews, lastSnapshot)
    }

    suspend fun fetchCurrentUserReview(
        gameId: String,
    ): Review? {
        val userId = userService.user.value?.id

        if (userId != null) {
            val (reviews, _) = firebaseStorageService.getDocuments<Review>(
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
                ),
                limit = 1
            )
            return reviews.firstOrNull()
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
                firebaseStorageService.addDocument(
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
            firebaseStorageService.updateDocument(
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
            firebaseStorageService.deleteDocument(
                collection = "reviews",
                documentId = reviewId,
            )
        }
    }

    suspend fun deleteReviews(
        userId: String
    ) {
        firebaseStorageService.deleteDocuments(
            collection = "reviews",
            internalId = "userId",
            matchingId = userId,
        )
    }
}
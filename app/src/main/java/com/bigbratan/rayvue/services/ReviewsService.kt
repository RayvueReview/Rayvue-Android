package com.bigbratan.rayvue.services

import com.bigbratan.rayvue.models.Review
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
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
        val documentFields = arrayOf("id", "gameId", "userId", "dateAdded", "userName", "content")
        val filters = mapOf("gameId" to gameId, "isUserAccredited" to true)

        val (reviews, lastSnapshot) = firebaseStorageService.getDocumentsRepeatedly<Review>(
            collectionId = "reviews",
            documentFields = documentFields,
            filters = filters,
            orderBy = "dateAdded",
            direction = Query.Direction.DESCENDING,
            limit = limit,
            startAfter = startAfter
        )

        val filteredReviews = userId?.let { reviews.filterNot { it.userId == userId } } ?: reviews
        return filteredReviews to lastSnapshot
    }

    suspend fun fetchCurrentUserReview(
        gameId: String
    ): Review? {
        val userId = userService.user.value?.id ?: return null
        val documentFields = arrayOf("id", "gameId", "userId", "dateAdded", "userName", "content")
        val filters = mapOf("userId" to userId, "gameId" to gameId)

        val (reviews, _) = firebaseStorageService.getDocumentsRepeatedly<Review>(
            collectionId = "reviews",
            documentFields = documentFields,
            filters = filters,
            limit = 1
        )

        return reviews.firstOrNull()
    }

    suspend fun fetchReviewsOnce(
        gameId: String,
        limit: Long,
    ): List<Review> {
        val userId = userService.user.value?.id
        val documentFields = arrayOf("id", "gameId", "userId", "dateAdded", "userName", "content")
        val filters = mapOf("gameId" to gameId, "isUserAccredited" to true)

        val reviews = firebaseStorageService.getDocuments<Review>(
            collectionId = "reviews",
            documentFields = documentFields,
            filters = filters,
            orderBy = "dateAdded",
            direction = Query.Direction.DESCENDING,
            limit = limit,
        )

        return userId?.let { reviews.filterNot { it.userId == userId } } ?: reviews
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
                    collectionId = "reviews",
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
                collectionId = "reviews",
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
                collectionId = "reviews",
                documentId = reviewId,
            )
        }
    }

    suspend fun deleteReviews(
        userId: String
    ) {
        firebaseStorageService.deleteDocuments(
            collectionId = "reviews",
            documentField = "userId",
            value = userId,
        )
    }
}
package com.bigbratan.rayvue.services

import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.models.GameDetails
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesService @Inject constructor(
    private val firebaseStorageService: FirebaseStorageService
) {
    suspend fun fetchGames(
        limit: Long,
        startAfter: DocumentSnapshot? = null
    ): Pair<List<Game>, DocumentSnapshot?> {
        return firebaseStorageService.getDocuments(
            collection = "games",
            documentFields = arrayOf(
                "id",
                "displayName",
                "icon",
            ),
            limit = limit,
            startAfter = startAfter
        )
    }

    suspend fun fetchGameDetails(
        gameId: String,
    ): GameDetails {
        return firebaseStorageService.getDocument<GameDetails>(
            collection = "games",
            documentId = gameId,
            documentFields = arrayOf(
                "id",
                "displayName",
                "icon",
                "banner",
                "description",
                "price",
                "tags",
            ),
        )
    }

    suspend fun searchGames(
        searchQuery: String
    ): List<Game> {
        return firebaseStorageService.searchDocuments<Game>(
            collection = "games",
            documentFields = arrayOf(
                "id",
                "displayName",
                "icon",
            ),
            searchField = "searchName",
            searchQuery = searchQuery
        )
    }
}

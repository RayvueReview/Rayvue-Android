package com.bigbratan.rayvue.services

import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.models.GameDetails
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesService @Inject constructor(
    private val firebaseStorageService: FirebaseStorageService
) {
    suspend fun fetchGames(): List<Game> {
        return firebaseStorageService.getDocuments(
            collection = "games",
            documentFields = arrayOf(
                "id",
                "displayName",
                "icon",
            ),
        )
    }

    suspend fun fetchGameDetails(
        gameId: String,
    ): GameDetails {
        return firebaseStorageService.getDocuments<GameDetails>(
            collection = "games",
            documentFields = arrayOf(
                "id",
                "displayName",
                "icon",
                "banner",
                "description",
                "price",
                "tags",
            ),
            filters = mapOf("id" to gameId),
        ).first()
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

package com.bigbratan.rayvue.services

import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.models.GameDetails
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesService @Inject constructor(
    private val storageService: StorageService
) {
    suspend fun fetchGames(): List<Game> {
        return storageService.getDocuments(
            collection = "games",
            documentFields = arrayOf(
                "id",
                "name",
                "icon",
            ),
        )
    }

    suspend fun fetchGameDetails(
        gameId: String,
    ): GameDetails {
        return storageService.getDocuments<GameDetails>(
            collection = "games",
            documentFields = arrayOf(
                "id",
                "name",
                "icon",
                "banner",
                "description",
                "price",
                "tags",
            ),
            filters = mapOf("id" to gameId),
        ).first()
    }
}

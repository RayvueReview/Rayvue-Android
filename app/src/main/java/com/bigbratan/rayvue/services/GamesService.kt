package com.bigbratan.rayvue.services

import com.bigbratan.rayvue.models.AwardGame
import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.models.GameDetails
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamesService @Inject constructor(
    private val firebaseStorageService: FirebaseStorageService
) {
    private suspend fun fetchRandomGameIds(
        genre: String,
        limit: Long,
    ): List<String> {
        return firebaseStorageService.getDocumentAsList(
            collectionId = "gameIds",
            documentId = genre,
            documentField = "idsList",
        ).shuffled().take(limit.toInt())
    }

    suspend fun fetchRandomGames(
        genre: String,
        limit: Long,
    ): List<Game> {
        return fetchRandomGameIds(genre, limit).map { gameId ->
            firebaseStorageService.getDocument<Game>(
                collectionId = "games",
                documentId = gameId,
                documentFields = arrayOf(
                    "id",
                    "displayName",
                    "icon",
                ),
            )
        }
    }

    suspend fun fetchRecentGames(
        limit: Long,
    ): List<Game> {
        return firebaseStorageService.getDocuments(
            collectionId = "games",
            documentFields = arrayOf(
                "id",
                "displayName",
                "icon",
            ),
            limit = limit,
            orderBy = "dateAdded",
            direction = Query.Direction.DESCENDING,
        )
    }

    suspend fun fetchAllGames(
        limit: Long,
        startAfter: DocumentSnapshot? = null
    ): Pair<List<Game>, DocumentSnapshot?> {
        return firebaseStorageService.getDocumentsRepeatedly(
            collectionId = "games",
            documentFields = arrayOf(
                "id",
                "displayName",
                "icon",
            ),
            orderBy = "displayName",
            limit = limit,
            startAfter = startAfter
        )
    }

    suspend fun fetchAllGamesByGenre(
        genre: String,
        limit: Long,
        startAfter: DocumentSnapshot? = null
    ): Pair<List<Game>, DocumentSnapshot?> {
        return firebaseStorageService.getDocumentsRepeatedly(
            collectionId = "games",
            documentFields = arrayOf(
                "id",
                "displayName",
                "icon",
            ),
            filters = mapOf("genre" to genre),
            limit = limit,
            startAfter = startAfter
        )
    }

    suspend fun fetchGameDetails(
        gameId: String,
    ): GameDetails {
        return firebaseStorageService.getDocument<GameDetails>(
            collectionId = "games",
            documentId = gameId,
            documentFields = arrayOf(
                "id",
                "displayName",
                "developer",
                "icon",
                "banner",
                "description",
                "price",
                "categories",
                "tags",
            ),
        )
    }

    suspend fun fetchTopGames(
        dateType: String,
        limit: Long,
        startAfter: DocumentSnapshot? = null
    ): Pair<List<AwardGame>, DocumentSnapshot?> {
        return firebaseStorageService.getDocumentsRepeatedly(
            collectionId = "games",
            documentFields = arrayOf(
                "id",
                "displayName",
                "icon",
                "banner",
                "gameOfTheWeek",
                "gameOfTheMonth",
                "gameOfTheYear",
            ),
            limit = limit,
            orderBy = dateType,
            direction = Query.Direction.DESCENDING,
            startAfter = startAfter
        )
    }

    suspend fun fetchJournalGames(
        gameIds: List<String>,
        limit: Long,
        startAfter: DocumentSnapshot? = null
    ): Pair<List<Game>, DocumentSnapshot?> {
        return firebaseStorageService.getDocumentsRepeatedly(
            collectionId = "games",
            documentFields = arrayOf(
                "id",
                "displayName",
                "icon",
                "banner",
            ),
            ids = gameIds,
            limit = limit,
            startAfter = startAfter
        )
    }

    suspend fun searchGames(
        searchQuery: String
    ): List<Game> {
        return firebaseStorageService.searchDocuments<Game>(
            collectionId = "games",
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

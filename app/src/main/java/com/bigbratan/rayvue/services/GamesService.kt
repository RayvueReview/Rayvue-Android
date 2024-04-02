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
                "name",
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

    suspend fun searchGames(
        query: String
    ): List<Game> {
        val potentialMatches = firebaseStorageService.getDocuments<Game>(
            collection = "games",
            documentFields = arrayOf(
                "id",
                "name",
                "icon"
            ),
        )

        val maxDistance = 5

        return potentialMatches.filter { game ->
            levenshtein(
                game.name.filter { it.isLetterOrDigit() }.lowercase(),
                query.filter { it.isLetterOrDigit() }.lowercase(),
            ) <= maxDistance
        }
    }

    private fun levenshtein(
        lhs: CharSequence,
        rhs: CharSequence
    ): Int {
        if (lhs == rhs) {
            return 0
        }
        if (lhs.isEmpty()) {
            return rhs.length
        }
        if (rhs.isEmpty()) {
            return lhs.length
        }

        val len0 = lhs.length + 1
        val len1 = rhs.length + 1

        var prev = IntArray(len0)
        var cost = IntArray(len0)
        var tmp: IntArray

        for (i in 0 until len0) {
            prev[i] = i
        }

        for (j in 1 until len1) {
            cost[0] = j
            for (i in 1 until len0) {
                val add = prev[i] + 1
                val delete = cost[i - 1] + 1
                val replace = prev[i - 1] + if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                cost[i] = minOf(add, delete, replace)
            }
            tmp = prev
            prev = cost
            cost = tmp
        }

        return prev[len0 - 1]
    }
}

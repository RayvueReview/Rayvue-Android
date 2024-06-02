package com.bigbratan.rayvue.services

import com.bigbratan.rayvue.models.JournalEntry
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalService @Inject constructor(
    private val localStorageService: LocalStorageService,
    private val firebaseStorageService: FirebaseStorageService
) {
    private val journalKey = "journal_entries"

    suspend fun saveJournalEntry(
        entry: JournalEntry
    ) {
        val currentEntries = localStorageService.readData<List<JournalEntry>>(
            key = journalKey,
            defaultValue = emptyList()
        ).first().toMutableList()
        val index =
            currentEntries.indexOfFirst { it.id == entry.id && it.gameId == entry.gameId && it.userId == entry.userId }

        if (index != -1) {
            currentEntries[index] = entry
        } else {
            currentEntries.add(entry)
        }

        localStorageService.saveData(
            key = journalKey,
            value = currentEntries
        )
    }

    suspend fun uploadJournalEntry(
        entry: JournalEntry
    ) {
        firebaseStorageService.addOrUpdateDocument(
            collectionId = "journalEntries",
            documentId = entry.id,
            data = entry,
        )
    }

    suspend fun updateAndUploadJournalEntries(
        userId: String
    ) {
        val entries = getLocalJournalEntries()
        val updatedEntries = entries.map { it.copy(userId = userId) }

        localStorageService.saveData(
            key = journalKey,
            value = updatedEntries
        )

        updatedEntries.forEach { entry ->
            uploadJournalEntry(entry)
        }
    }

    suspend fun getLocalJournalEntries(): List<JournalEntry> {
        return localStorageService.readData(
            key = journalKey,
            defaultValue = emptyList<JournalEntry>()
        ).first()
    }

    suspend fun getFirebaseJournalEntries(
        userId: String,
    ): List<JournalEntry> {
        return firebaseStorageService.getDocuments(
            collectionId = "journalEntries",
            documentFields = arrayOf(
                "id",
                "gameId",
                "userId",
                "content",
                "dateAdded"
            ),
            filters = mapOf("userId" to userId),
            orderBy = "dateAdded",
            direction = Query.Direction.DESCENDING,
        )
    }
}
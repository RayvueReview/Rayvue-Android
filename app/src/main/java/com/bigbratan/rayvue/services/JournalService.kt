package com.bigbratan.rayvue.services

import com.bigbratan.rayvue.models.JournalEntry
import com.google.firebase.firestore.DocumentSnapshot
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

    suspend fun updateAndUploadAllJournalEntries(
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

    suspend fun fetchJournalEntriesFromFirebase(
        userId: String,
        limit: Long,
        startAfter: DocumentSnapshot? = null
    ): Pair<List<JournalEntry>, DocumentSnapshot?> {
        return firebaseStorageService.getDocumentsRepeatedly(
            collectionId = "journalEntries",
            documentFields = arrayOf(
                "id",
                "gameId",
                "userId",
                "content",
                "dateAdded"
            ),
            filters = mapOf("userId" to userId),
            limit = limit,
            startAfter = startAfter
        )
    }
}

/*suspend fun uploadJournalEntries(
        userId: String
    ) {
        val entries =
            runBlocking {
                localStorageService.readData(
                    key = journalKey,
                    defaultValue = listOf<JournalEntry>(),
                ).first()
            }

        entries.forEach { entry ->
            firebaseStorageService.addDocument(
                collectionId = "journalEntries",
                documentId = entry.id,
                data = entry.copy(userId = userId),
            )
        }
    }*/
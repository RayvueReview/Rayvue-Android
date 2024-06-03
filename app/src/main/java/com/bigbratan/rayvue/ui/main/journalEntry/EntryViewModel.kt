package com.bigbratan.rayvue.ui.main.journalEntry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.models.JournalEntry
import com.bigbratan.rayvue.services.JournalService
import com.bigbratan.rayvue.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val journalService: JournalService,
    private val userService: UserService
) : ViewModel() {
    val entryAlreadyExists = MutableStateFlow<Boolean?>(null)
    val sentEntryState = MutableStateFlow<SentEntryState>(SentEntryState.Idle)
    val currentEntryState = MutableStateFlow<JournalEntry?>(null)
    val userIdState = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            userService.user.collect { user ->
                userIdState.value = user?.id
            }
        }
    }

    fun loadEntry(gameId: String) {
        viewModelScope.launch {
            var loadedEntry = journalService.getLocalJournalEntries().find { it.gameId == gameId }

            if (loadedEntry != null) {
                userIdState.value?.let { userId ->
                    loadedEntry = journalService.getFirebaseJournalEntries(userId).find { it.gameId == gameId }
                }
            }

            currentEntryState.value = loadedEntry
        }
    }

    fun saveEntry(entry: JournalEntry) {
        viewModelScope.launch {
            journalService.saveJournalEntry(entry)
            currentEntryState.value = entry
        }
    }

    fun uploadEntry(entry: JournalEntry) {
        viewModelScope.launch {
            sentEntryState.value = SentEntryState.Loading

            try {
                if (userIdState.value != null) {
                    journalService.uploadJournalEntry(entry.copy(userId = userIdState.value))

                    sentEntryState.value = SentEntryState.Success
                } else {
                    sentEntryState.value = SentEntryState.Error
                }
            } catch (e: Exception) {
                sentEntryState.value = SentEntryState.Error
            }
        }
    }

    fun resetSentState() {
        sentEntryState.value = SentEntryState.Idle
    }

    fun checkEntryAlreadyExists(gameId: String) {
        viewModelScope.launch {
            val localEntries = journalService.getLocalJournalEntries()
            val localExists = localEntries.any { it.gameId == gameId }

            if (!localExists && userIdState.value != null) {
                val remoteEntries = journalService.getFirebaseJournalEntries(userIdState.value!!)

                entryAlreadyExists.value = remoteEntries.any { it.gameId == gameId }
            } else {
                entryAlreadyExists.value = localExists
            }
        }
    }

    fun resetEntryAlreadyExists() {
        entryAlreadyExists.value = null
    }
}

sealed class SentEntryState {
    object Idle : SentEntryState()

    object Loading : SentEntryState()

    object Success : SentEntryState()

    object Error : SentEntryState()
}

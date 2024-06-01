package com.bigbratan.rayvue.ui.main.journalEntry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigbratan.rayvue.models.Game
import com.bigbratan.rayvue.models.JournalEntry
import com.bigbratan.rayvue.services.JournalService
import com.bigbratan.rayvue.services.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val journalService: JournalService,
    private val userService: UserService
) : ViewModel() {
    val sentEntryState = MutableStateFlow<SentEntryState>(SentEntryState.Idle)
    val userIdState = MutableStateFlow<String?>(null)
    val currentJournalEntry = MutableStateFlow<JournalEntry?>(null)
    private val _entryExists = MutableStateFlow<Boolean?>(null)
    val entryExists = _entryExists.asStateFlow()
    val selectedGameFromJournal = MutableStateFlow<Game?>(null)

    init {
        viewModelScope.launch {
            userService.user.collect { user ->
                userIdState.value = user?.id
            }
        }
    }

    fun saveJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            journalService.saveJournalEntry(entry)
            currentJournalEntry.value = entry
        }
    }

    fun uploadJournalEntry(entry: JournalEntry) {
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

    fun checkEntryExists(gameId: String) {
        viewModelScope.launch {
            val localEntries = journalService.getLocalJournalEntries()
            val localExists = localEntries.any { it.gameId == gameId }

            if (!localExists && userIdState.value != null) {
                val remoteEntries = journalService.getFirebaseJournalEntries(userIdState.value!!)
                _entryExists.value = remoteEntries.any { it.gameId == gameId }
            } else {
                _entryExists.value = localExists
            }
        }
    }

    fun resetEntryExists() {
        _entryExists.value = null
    }

    fun setSelectedGameFromJournal(game: Game) {
        selectedGameFromJournal.value = game
    }

    fun clearSelectedGameFromJournal() {
        selectedGameFromJournal.value = null
    }

    fun loadJournalEntryForGame(gameId: String) {
        viewModelScope.launch {
            val entry = journalService.getLocalJournalEntries().find { it.gameId == gameId }

            currentJournalEntry.value = entry
        }
    }
}

sealed class SentEntryState {
    object Idle : SentEntryState()

    object Loading : SentEntryState()

    object Success : SentEntryState()

    object Error : SentEntryState()
}

/*fun checkEntryExists(gameId: String) {
        viewModelScope.launch {
            val entries = journalService.getLocalJournalEntries()
            _entryExists.value = entries.any { it.gameId == gameId }
        }
    }*/
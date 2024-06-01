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
    val sentEntryState = MutableStateFlow<SentEntryState>(SentEntryState.Idle)
    val userIdState = MutableStateFlow<String?>(null)
    val currentJournalEntry = MutableStateFlow<JournalEntry?>(null)

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

    fun loadJournalEntryForGame(gameId: String) {
        viewModelScope.launch {
            val entries = journalService.getLocalJournalEntries()
            currentJournalEntry.value = entries.find { it.gameId == gameId }
        }
    }
}

sealed class SentEntryState {
    object Idle : SentEntryState()

    object Loading : SentEntryState()

    object Success : SentEntryState()

    object Error : SentEntryState()
}
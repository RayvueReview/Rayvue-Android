package com.bigbratan.rayvue.models

import com.google.firebase.Timestamp

data class JournalEntry(
    val userId: String? = null,
    val gameId: String,
    val id: String,
    val content: String,
    val dateAdded: Timestamp,
)
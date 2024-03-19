package com.bigbratan.rayvue.models

import com.google.firebase.Timestamp

data class Review(
    val id: String,
    val gameId: String,
    val userId: String,
    val dateAdded: Timestamp,
    val userName: String,
    val content: String,
    val isUserAccredited: Boolean,
)

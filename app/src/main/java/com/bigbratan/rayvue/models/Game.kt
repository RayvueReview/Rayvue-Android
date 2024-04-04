package com.bigbratan.rayvue.models

import com.google.firebase.Timestamp

data class Game(
    val id: String,
    val displayName: String,
    val icon: String,
    val dateAdded: Timestamp,
)
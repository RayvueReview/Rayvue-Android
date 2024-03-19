package com.bigbratan.rayvue.models

import com.google.firebase.Timestamp

data class Game(
    val id: String,
    val name: String,
    val icon: String,
    val dateAdded: Timestamp,
)
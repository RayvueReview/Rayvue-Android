package com.bigbratan.rayvue.models

import com.google.firebase.Timestamp

data class Game(
    val id: String,
    val displayName: String,
    val icon: String,
    val banner: String? = null,
)

data class AwardGame(
    val id: String,
    val displayName: String,
    val icon: String,
    val banner: String,
    val gameOfTheWeek: Timestamp? = null,
    val gameOfTheMonth: Timestamp? = null,
    val gameOfTheYear: Timestamp? = null,
)

data class GameDetails(
    val id: String,
    val displayName: String,
    val developer: String,
    val icon: String,
    val encodedIcon: String,
    val banner: String,
    val description: String,
    val price: Double,
    val categories: List<String>,
    val tags: List<Int>? = null,
)
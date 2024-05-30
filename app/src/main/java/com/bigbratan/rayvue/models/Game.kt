package com.bigbratan.rayvue.models

data class Game(
    val id: String,
    val displayName: String,
    val icon: String,
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
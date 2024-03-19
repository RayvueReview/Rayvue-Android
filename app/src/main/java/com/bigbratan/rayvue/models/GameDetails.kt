package com.bigbratan.rayvue.models

data class GameDetails(
    val id: String,
    val name: String,
    val icon: String,
    val encodedIcon: String,
    val banner: String,
    val description: String,
    val price: Double,
    val tags: List<Int>? = null,
)

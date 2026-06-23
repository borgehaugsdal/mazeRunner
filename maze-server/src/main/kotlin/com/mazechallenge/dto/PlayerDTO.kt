package com.mazechallenge.dto

data class PlayerDTO(
    val id: Int,
    val name: String,
    val position: Pair<Int, Int>,
    val color: String
)

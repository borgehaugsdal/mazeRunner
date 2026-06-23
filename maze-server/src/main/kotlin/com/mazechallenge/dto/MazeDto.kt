package com.mazechallenge.dto

data class MazeDto(
    val width: Int,
    val height: Int,
    val layout: List<String>,
    val startPosition: Pair<Int, Int>,
    val exitPosition: Pair<Int, Int>
)

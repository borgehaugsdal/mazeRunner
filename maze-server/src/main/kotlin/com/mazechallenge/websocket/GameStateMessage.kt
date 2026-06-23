package com.mazechallenge.websocket

data class GameStateMessage(
    val playerId: Int,
    val playerName: String,
    val position: Pair<Int, Int>,
    val mazeState: Array<CharArray>
)
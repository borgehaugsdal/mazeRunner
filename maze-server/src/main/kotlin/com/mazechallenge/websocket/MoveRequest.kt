package com.mazechallenge.websocket

data class MoveRequest(
    val playerId: Int,
    val direction: String
)


package com.mazechallenge.domain

data class Player(
    val id: Int,
    val name: String,
    var position: Pair<Int, Int> = Pair(0, 0),
    val color: String = "black",
    var moveCount: Int = 0,
    var returnMoveCount: Int? = null
) {
    init {
        require(name.length <= 10) { "Player name must be up to 10 characters long." }
    }
}
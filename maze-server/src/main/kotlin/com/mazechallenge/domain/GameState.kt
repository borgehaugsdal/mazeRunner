package com.mazechallenge.domain

data class GameState(
    val players: MutableList<Player> = mutableListOf(),
    val maze: Maze? = null,
    var currentPlayerIndex: Int = 0
) {
    fun addPlayer(player: Player) {
        players.add(player)
    }

    fun movePlayer(playerId: Int, newPosition: Pair<Int, Int>) {
        val player = players.find { it.id == playerId }
        player?.let {
            it.position = newPosition
        }
    }

    fun getPlayerPositions(): Map<Int, Pair<Int, Int>> {
        return players.associate { it.id to it.position }
    }

    fun nextPlayer() {
        if (players.isNotEmpty()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        }
    }
}
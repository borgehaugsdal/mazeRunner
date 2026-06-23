package com.mazechallenge.service

import com.mazechallenge.domain.GameState
import com.mazechallenge.domain.Player
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class GameService(private val mazeService: MazeService) {

    private val players: ConcurrentHashMap<Int, Player> = ConcurrentHashMap()
    private var gameState: GameState? = null

    fun startGame(): GameState {
        val maze = mazeService.loadMaze(1) ?: throw IllegalStateException("Failed to load maze")
        gameState = GameState(maze = maze)
        return gameState!!
    }

    fun joinGame(name: String): GameState {
        val currentState = gameState ?: startGame()
        val playerId = players.size + 1
        val startPos = currentState.maze?.startPosition ?: Pair(0, 0)
        val player = Player(playerId, name.take(10), startPos, getColorForPlayer(playerId))
        players[playerId] = player
        currentState.addPlayer(player)
        return currentState
    }

    fun registerPlayer(name: String): Int {
        val currentState = gameState ?: startGame()
        val playerId = players.size + 1
        val startPos = currentState.maze?.startPosition ?: Pair(0, 0)
        val player = Player(playerId, name.take(10), startPos, getColorForPlayer(playerId))
        players[playerId] = player
        currentState.addPlayer(player)
        return playerId
    }

    fun getPlayer(playerId: Int): Player? {
        return players[playerId]
    }

    fun movePlayer(playerId: Int, direction: String): GameState {
        val currentState = gameState ?: throw IllegalStateException("Game not started")
        val player = players[playerId] ?: throw IllegalArgumentException("Player not found")
        val (x, y) = player.position
        val newPosition = when (direction.uppercase()) {
            "UP" -> Pair(x, y - 1)
            "DOWN" -> Pair(x, y + 1)
            "LEFT" -> Pair(x - 1, y)
            "RIGHT" -> Pair(x + 1, y)
            else -> player.position
        }
        val maze = currentState.maze
        if (maze == null || maze.isValidMove(newPosition.first, newPosition.second)) {
            player.position = newPosition
            currentState.movePlayer(playerId, newPosition)
        }
        return currentState
    }

    fun getGameState(): GameState {
        return gameState ?: throw IllegalStateException("Game not started")
    }

    fun getPlayerView(playerId: Int): List<String> {
        val currentState = getGameState()
        val player = players[playerId] ?: throw IllegalArgumentException("Player not found")
        return currentState.maze?.getVisibleArea(player.position, 3) ?: emptyList()
    }

    fun setGameState(state: GameState) {
        this.gameState = state
    }

    private fun getColorForPlayer(playerId: Int): String {
        return when (playerId % 7) {
            1 -> "red"
            2 -> "blue"
            3 -> "green"
            4 -> "yellow"
            5 -> "purple"
            6 -> "orange"
            else -> "black"
        }
    }
}
package com.mazechallenge.service

import com.mazechallenge.controller.DiscoveredTile
import com.mazechallenge.domain.Player
import com.mazechallenge.domain.PlayerView
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class PlayerService(private val mazeService: MazeService) {

    private val players = ConcurrentHashMap<Int, Player>()
    private val playerIdGenerator = AtomicInteger(1)
    private val escapedPlayers = ConcurrentHashMap.newKeySet<Int>()
    private val returningPlayers = ConcurrentHashMap.newKeySet<Int>()
    private val discoveredMaps = ConcurrentHashMap<Int, List<DiscoveredTile>>()

    fun registerPlayer(name: String): Player {
        if (name.length > 10) {
            throw IllegalArgumentException("Player name must be up to 10 characters long.")
        }
        val playerId = playerIdGenerator.getAndIncrement()
        val maze = mazeService.loadMaze(1)
        val startPos = maze?.startPosition ?: Pair(0, 0)
        val player = Player(playerId, name, startPos, getColorForPlayer(playerId))
        players[playerId] = player
        return player
    }

    fun getPlayer(playerId: Int): Player? {
        return players[playerId]
    }

    fun movePlayer(playerId: Int, direction: String): Player {
        val player = players[playerId] ?: throw IllegalArgumentException("Player not found")
        val maze = mazeService.loadMaze(1)
        val (x, y) = player.position
        val newPosition = when (direction.uppercase()) {
            "UP" -> Pair(x, y - 1)
            "DOWN" -> Pair(x, y + 1)
            "LEFT" -> Pair(x - 1, y)
            "RIGHT" -> Pair(x + 1, y)
            "NORTH" -> Pair(x, y - 1)
            "SOUTH" -> Pair(x, y + 1)
            "WEST" -> Pair(x - 1, y)
            "EAST" -> Pair(x + 1, y)
            else -> player.position
        }
        if (maze != null && !maze.isValidMove(newPosition.first, newPosition.second)) {
            throw IllegalStateException("Cannot move through walls")
        }
        player.position = newPosition
        player.moveCount += 1
        if (maze != null && player.position == maze.exitPosition) {
            escapedPlayers.add(playerId)
            returningPlayers.add(playerId)
            player.returnMoveCount = 0
        } else if (maze != null && returningPlayers.contains(playerId)) {
            player.returnMoveCount = (player.returnMoveCount ?: 0) + 1
            if (player.position == maze.startPosition) {
                returningPlayers.remove(playerId)
            }
        }
        return player
    }

    fun getPlayerView(playerId: Int): PlayerView {
        val player = players[playerId] ?: throw IllegalArgumentException("Player not found")
        val maze = mazeService.loadMaze(1)
        val visibleArea = maze?.getVisibleArea(player.position, 3) ?: emptyList()
        return PlayerView(player.id, visibleArea)
    }

    fun getAllPlayers(): List<Player> {
        return players.values.toList()
    }

    fun hasEscaped(playerId: Int): Boolean {
        return escapedPlayers.contains(playerId)
    }

    fun getDirectionalView(playerId: Int, maxDistance: Int = 4): Map<String, List<String>> {
        val player = players[playerId] ?: throw IllegalArgumentException("Player not found")
        val maze = mazeService.loadMaze(1) ?: return mapOf(
            "north" to emptyList(),
            "south" to emptyList(),
            "east" to emptyList(),
            "west" to emptyList()
        )

        val (x, y) = player.position
        fun ray(dx: Int, dy: Int): List<String> {
            val cells = mutableListOf<String>()
            for (step in 1..maxDistance) {
                val nx = x + (dx * step)
                val ny = y + (dy * step)
                if (ny !in maze.layout.indices || nx !in maze.layout[ny].indices) {
                    break
                }
                val ch = maze.layout[ny][nx]
                cells.add(ch.toString())
                if (ch == '#') {
                    break
                }
            }
            return cells
        }

        return mapOf(
            "north" to ray(0, -1),
            "south" to ray(0, 1),
            "east" to ray(1, 0),
            "west" to ray(-1, 0)
        )
    }

    private fun getColorForPlayer(playerId: Int): String {
        return when (playerId % 6) {
            1 -> "red"
            2 -> "blue"
            3 -> "green"
            4 -> "yellow"
            5 -> "purple"
            0 -> "orange"
            else -> "black"
        }
    }

    fun updateDiscoveredMap(playerId: Int, tiles: List<DiscoveredTile>) {
        discoveredMaps[playerId] = tiles
    }


    fun getDiscoveredMap(playerId: Int): List<DiscoveredTile> =
        discoveredMaps[playerId] ?: emptyList()
}
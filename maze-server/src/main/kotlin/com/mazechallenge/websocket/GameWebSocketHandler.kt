package com.mazechallenge.websocket

import com.mazechallenge.domain.Player
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.context.event.EventListener

@Controller
class GameWebSocketHandler {

    private val players = mutableListOf<Player>()

    @MessageMapping("/register")
    @SendTo("/topic/game-state")
    fun registerPlayer(player: Player): GameStateMessage {
        players.add(player)
        return GameStateMessage(
            playerId = player.id,
            playerName = player.name,
            position = player.position,
            mazeState = emptyArray()
        )
    }

    @MessageMapping("/move")
    @SendTo("/topic/game-state")
    fun movePlayer(moveRequest: MoveRequest): GameStateMessage {
        val player = players.find { it.id == moveRequest.playerId }
        player?.let {
            // Update player position based on moveRequest
        }
        val currentPlayer = player ?: players.firstOrNull()
        return GameStateMessage(
            playerId = currentPlayer?.id ?: 0,
            playerName = currentPlayer?.name ?: "",
            position = currentPlayer?.position ?: Pair(0, 0),
            mazeState = emptyArray()
        )
    }

    @EventListener
    fun handleWebSocketConnect(event: SessionConnectEvent) {
        // Handle new connections if needed
    }

    @EventListener
    fun handleWebSocketDisconnect(event: SessionDisconnectEvent) {
        // Handle disconnections if needed
    }
}
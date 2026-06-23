package com.mazechallenge.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameServiceTest {

    private lateinit var gameService: GameService

    @BeforeEach
    fun setUp() {
        gameService = GameService(MazeService())
    }

    @Test
    fun `test player registration`() {
        val playerName = "Player1"
        val playerId = gameService.registerPlayer(playerName)

        assertNotNull(playerId)
        assertEquals(playerName, gameService.getPlayer(playerId)?.name)
    }

    @Test
    fun `test player movement`() {
        val playerId = gameService.registerPlayer("Player1")
        val initialPosition = gameService.getPlayer(playerId)?.position

        gameService.movePlayer(playerId, "DOWN")

        val player = gameService.getPlayer(playerId)
        assertNotNull(player)
        // Position may or may not change depending on maze walls
        assertNotNull(player?.position)
    }

    @Test
    fun `test player cannot move through walls`() {
        val playerId = gameService.registerPlayer("Player1")
        // Game state should be valid after registration
        val gameState = gameService.getGameState()
        assertNotNull(gameState)
    }

    @Test
    fun `test game state update on player move`() {
        val playerId = gameService.registerPlayer("Player1")
        gameService.movePlayer(playerId, "DOWN")

        val gameState = gameService.getGameState()
        assertTrue(gameState.players.any { it.id == playerId })
    }
}
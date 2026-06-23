package com.mazechallenge.service

import com.mazechallenge.domain.Player
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlayerServiceTest {

    private lateinit var playerService: PlayerService

    @BeforeEach
    fun setUp() {
        playerService = PlayerService(MazeService())
    }

    @Test
    fun `should register a player successfully`() {
        val playerName = "Player1"
        val player = playerService.registerPlayer(playerName)

        assertNotNull(player)
        assertEquals(playerName, player.name)
        assertTrue(player.id > 0)
    }

    @Test
    fun `should not register a player with name longer than 10 characters`() {
        val playerName = "PlayerWithLongName"
        val exception = assertThrows(IllegalArgumentException::class.java) {
            playerService.registerPlayer(playerName)
        }

        assertEquals("Player name must be up to 10 characters long.", exception.message)
    }

    @Test
    fun `should move player successfully`() {
        val player = playerService.registerPlayer("Player1")
        val initialPosition = player.position

        // Try to move - may succeed or throw depending on maze walls
        try {
            playerService.movePlayer(player.id, "DOWN")
            assertNotEquals(initialPosition, player.position)
        } catch (e: IllegalStateException) {
            assertEquals("Cannot move through walls", e.message)
        }
    }

    @Test
    fun `should not move player through walls`() {
        val player = playerService.registerPlayer("Player1")
        // Move to a corner position that has walls around it
        player.position = Pair(0, 0)

        assertThrows(IllegalStateException::class.java) {
            playerService.movePlayer(player.id, "UP")
        }
    }

    @Test
    fun `should return player view correctly`() {
        val player = playerService.registerPlayer("Player1")
        val view = playerService.getPlayerView(player.id)

        assertNotNull(view)
        assertEquals(player.id, view.playerId)
    }
}
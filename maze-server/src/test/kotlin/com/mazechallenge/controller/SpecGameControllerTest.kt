package com.mazechallenge.controller

import com.mazechallenge.service.PlayerService
import com.mazechallenge.service.MazeService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(MazeController::class)
class MazeControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var playerService: PlayerService

    @MockBean
    private lateinit var mazeService: MazeService

    @MockBean
    private lateinit var messagingTemplate: SimpMessagingTemplate

    @Test
    fun `should create player with spec endpoint`() {
        `when`(playerService.registerPlayer(org.mockito.ArgumentMatchers.anyString()))
            .thenReturn(com.mazechallenge.domain.Player(1, "Player1", Pair(1, 1), "blue"))

        mockMvc.perform(post("/game"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.playerId").value("1"))
    }


    @Test
    fun `should move player with direction in path and return view and escaped status`() {
        `when`(playerService.movePlayer(1, "NORTH"))
            .thenReturn(com.mazechallenge.domain.Player(1, "Player1", Pair(1, 0), "blue"))
        `when`(playerService.hasEscaped(1)).thenReturn(false)
        `when`(playerService.getDirectionalView(1)).thenReturn(
            mapOf(
                "north" to listOf(" ", "#"),
                "south" to listOf("#"),
                "east" to listOf(" ", "#"),
                "west" to listOf("#")
            )
        )

        mockMvc.perform(post("/game/1/move/NORTH"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.escaped").value(false))
            .andExpect(jsonPath("$.north[0]").value(" "))
            .andExpect(jsonPath("$.south[0]").value("#"))
    }

    @Test
    fun `should return view from current position when move is blocked and include escaped status`() {
        `when`(playerService.movePlayer(1, "NORTH"))
            .thenThrow(IllegalStateException("Blocked"))
        `when`(playerService.hasEscaped(1)).thenReturn(false)
        `when`(playerService.getDirectionalView(1)).thenReturn(
            mapOf(
                "north" to listOf("#"),
                "south" to listOf(" "),
                "east" to listOf(" "),
                "west" to listOf("#")
            )
        )

        mockMvc.perform(post("/game/1/move/NORTH"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.escaped").value(false))
            .andExpect(jsonPath("$.north[0]").value("#"))
    }
}
package com.mazechallenge.controller

import com.mazechallenge.service.MazeService
import com.mazechallenge.service.PlayerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Schema(description = "Spillerinformasjon")
data class PlayerResponse(
    @field:Schema(example = "1")
    val id: Int,
    @field:Schema(example = "MazeBot")
    val name: String,
    @field:Schema(description = "Farge tildelt spilleren", example = "red")
    val color: String,
    @field:Schema(description = "Nåværende posisjon")
    val position: PositionResponse,
    @field:Schema(description = "Totalt antall vellykkede flyttinger", example = "412")
    val moveCount: Int,
    @field:Schema(description = "Antall returtrekk fra mål til start, hvis registrert", nullable = true, example = "54")
    val returnMoveCount: Int?
)

@RestController
@RequestMapping("/gamecontrol")
class MazeFrontendController(
    private val playerService: PlayerService,
    private val mazeService: MazeService
) {

    @GetMapping("/players")
    @Operation(
        tags = ["Maze frontend API"],
        operationId = "frontendGetPlayers",
        summary = "Hent alle spillere",
        description = "Returnerer spillerliste med navn, farge, posisjon, total moveCount og eventuelt returnMoveCount."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Spillerliste returnert",
                content = [Content(schema = Schema(implementation = PlayerResponse::class))]
            )
        ]
    )
    fun getPlayers(): List<PlayerResponse> {
        return playerService.getAllPlayers().map {
            PlayerResponse(
                id = it.id,
                name = it.name,
                color = it.color,
                position = PositionResponse(it.position.first, it.position.second),
                moveCount = it.moveCount,
                returnMoveCount = it.returnMoveCount
            )
        }
    }

    @GetMapping("/maze")
    @Operation(
        tags = ["Maze frontend API"],
        operationId = "frontendGetMaze",
        summary = "Hent labyrint",
        description = "Returnerer full labyrint med bredde/høyde, layout, startposisjon og målposisjon."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Labyrint returnert",
                content = [Content(schema = Schema(implementation = MazeResponse::class))]
            )
        ]
    )
    fun getMaze(): MazeResponse {
        val maze = mazeService.loadMaze(1) ?: throw IllegalStateException("Maze not found")
        return MazeResponse(
            width = maze.width,
            height = maze.height,
            layout = maze.layout,
            startPosition = PositionResponse(maze.startPosition.first, maze.startPosition.second),
            exitPosition = PositionResponse(maze.exitPosition.first, maze.exitPosition.second)
        )
    }

    @GetMapping("/{playerId}/discovered-map")
    @Operation(
        tags = ["Maze frontend API"],
        operationId = "frontendGetDiscoveredMap",
        summary = "Hent oppdaget kart",
        description = "Returnerer lagret oppdaget kart for valgt spiller, inkludert oppdagede tiles."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Oppdaget kart returnert",
                content = [Content(schema = Schema(implementation = DiscoveredMapResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "Spiller ikke funnet")
        ]
    )
    fun getDiscoveredMap(
        @Parameter(description = "ID til spiller", example = "1")
        @PathVariable playerId: String
    ): DiscoveredMapResponse {
        val tiles = playerService.getDiscoveredMap(playerId.toInt())
        return DiscoveredMapResponse(playerId.toInt(), tiles)
    }
}


package com.mazechallenge.controller

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.mazechallenge.service.PlayerService
import com.mazechallenge.service.MazeService
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as OasRequestBody
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Schema(description = "Respons etter opprettelse av spiller")
data class GameCreatedResponse(
    @field:Schema(description = "Spiller-ID", example = "1")
    val playerId: String
)


@Schema(description = "Spillerinformasjon")
data class PositionResponse(
    @field:Schema(description = "X-koordinat", example = "31")
    val first: Int,
    @field:Schema(description = "Y-koordinat", example = "30")
    val second: Int
)


@Schema(description = "Labyrintdata")
data class MazeResponse(
    @field:Schema(example = "36")
    val width: Int,
    @field:Schema(example = "31")
    val height: Int,
    @field:Schema(description = "En rad per streng. Tegn: '#', ' ', 'S', 'E'")
    val layout: List<String>,
    @field:Schema(description = "Startposisjon")
    val startPosition: PositionResponse,
    @field:Schema(description = "Målposisjon")
    val exitPosition: PositionResponse
)

@Schema(description = "Synsfelt i fire retninger")
data class ViewResponse(
    @field:Schema(description = "Celler nordover til vegg")
    val north: List<String>,
    @field:Schema(description = "Celler sørover til vegg")
    val south: List<String>,
    @field:Schema(description = "Celler østover til vegg")
    val east: List<String>,
    @field:Schema(description = "Celler vestover til vegg")
    val west: List<String>
)

@Schema(description = "Resultat av et trekk: om trekket lyktes, om spiller har nådd mål, og synsfelt fra ny posisjon")
data class MoveViewResponse(
    @field:Schema(description = "true ved vellykket trekk, false ved blokkert/ugyldig trekk", example = "true")
    val success: Boolean,
    @field:Schema(description = "true når spilleren står på mål (E)", example = "false")
    val escaped: Boolean,
    @field:Schema(description = "Celler nordover til vegg")
    val north: List<String>,
    @field:Schema(description = "Celler sørover til vegg")
    val south: List<String>,
    @field:Schema(description = "Celler østover til vegg")
    val east: List<String>,
    @field:Schema(description = "Celler vestover til vegg")
    val west: List<String>
)

@Schema(description = "En oppdaget celle i kartet")
data class DiscoveredTile @JsonCreator constructor(
    @field:Schema(example = "0")
    @JsonProperty("x") val x: Int,
    @field:Schema(example = "0")
    @JsonProperty("y") val y: Int,
    @field:Schema(description = "Celle-type", example = "START")
    @JsonProperty("tile") val tile: String
)

@Schema(description = "Request for oppdatert oppdaget kart")
data class DiscoveredMapRequest @JsonCreator constructor(
    @JsonProperty("tiles") val tiles: List<DiscoveredTile>
)

@Schema(description = "Request for antall returtrekk")
data class DiscoveredMapResponse(val playerId: Int, val tiles: List<DiscoveredTile>)

@RestController
@RequestMapping("/game")
class MazeController(
    private val playerService: PlayerService,
    private val mazeService: MazeService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @PostMapping
    @Operation(
        tags = ["Maze Player API"],
        summary = "Registrer ny spiller",
        description = "Oppretter en ny spiller i spillet og returnerer tildelt playerId. Navn er valgfritt."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Spiller opprettet",
                content = [Content(schema = Schema(implementation = GameCreatedResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Ugyldig input (f.eks. for langt navn)")
        ]
    )
    fun createGame(
        @Parameter(description = "Valgfritt spillernavn (maks 10 tegn)", example = "Bot1")
        @RequestParam(required = false) name: String?
    ): GameCreatedResponse {
        val playerName = name?.takeIf { it.isNotBlank() } ?: "Player${System.currentTimeMillis() % 1000}"
        val player = playerService.registerPlayer(playerName)
        messagingTemplate.convertAndSend(
            "/topic/game-state",
            mapOf(
                "event" to "PLAYER_CREATED",
                "playerId" to player.id,
                "playerName" to player.name,
                "position" to mapOf("first" to player.position.first, "second" to player.position.second)
            )
        )
        return GameCreatedResponse(playerId = player.id.toString())
    }


    @PostMapping("/{playerId}/move/{direction}")
    @Operation(
        tags = ["Maze Player API"],
        summary = "Flytt spiller ett steg",
        description = "Flytter spilleren ett steg i ønsket retning (NORTH, SOUTH, EAST, WEST). Returnerer success, escaped-status, og synsfelt fra ny posisjon ved vellykket trekk, eller fra nåværende posisjon dersom trekket er blokkert."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Trekkresultat med escaped-status og synsfelt",
                content = [Content(schema = Schema(implementation = MoveViewResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Ugyldig retning oppgitt"),
            ApiResponse(responseCode = "404", description = "Spiller ikke funnet")
        ]
    )
    fun movePlayer(
        @Parameter(description = "ID til spiller", example = "1")
        @PathVariable playerId: String,
        @Parameter(description = "Trekkretning: NORTH, SOUTH, EAST, WEST", example = "NORTH")
        @PathVariable direction: String
    ): MoveViewResponse {
        val id = playerId.toInt()
        val success = try {
            val moved = playerService.movePlayer(id, direction)
            messagingTemplate.convertAndSend(
                "/topic/game-state",
                mapOf(
                    "event" to "PLAYER_MOVED",
                    "playerId" to moved.id,
                    "playerName" to moved.name,
                    "position" to mapOf("first" to moved.position.first, "second" to moved.position.second)
                )
            )
            if (playerService.hasEscaped(moved.id)) {
                messagingTemplate.convertAndSend(
                    "/topic/game-state",
                    mapOf(
                        "event" to "PLAYER_EXITED",
                        "playerId" to moved.id,
                        "playerName" to moved.name,
                        "position" to mapOf("first" to moved.position.first, "second" to moved.position.second)
                    )
                )
            }
            true
        } catch (_: Exception) {
            false
        }
        val escaped = playerService.hasEscaped(id)
        val view = playerService.getDirectionalView(id)
        return MoveViewResponse(
            success = success,
            escaped = escaped,
            north = view["north"] ?: emptyList(),
            south = view["south"] ?: emptyList(),
            east = view["east"] ?: emptyList(),
            west = view["west"] ?: emptyList()
        )
    }


    @PostMapping("/{playerId}/discovered-map")
    @Operation(
        tags = ["Maze Player API"],
        summary = "Oppdater oppdaget kart",
        description = "Lagrer maze-player sitt oppdagede kart for spilleren. Relativt kart konverteres til absolutte koordinater."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Oppdaget kart lagret"),
            ApiResponse(responseCode = "404", description = "Spiller ikke funnet")
        ]
    )
    fun updateDiscoveredMap(
        @Parameter(description = "ID til spiller", example = "1")
        @PathVariable playerId: String,
        @OasRequestBody(
            description = "Oppdaget kart fra maze-player",
            required = true,
            content = [Content(schema = Schema(implementation = DiscoveredMapRequest::class))]
        )
        @RequestBody request: DiscoveredMapRequest
    ) {
        val maze = mazeService.loadMaze(1)
        val offsetX = maze?.startPosition?.first ?: 0
        val offsetY = maze?.startPosition?.second ?: 0
        // Konverter relative koordinater (0,0 = start) til absolutte maze-koordinater
        val absolute = request.tiles.map { it.copy(x = it.x + offsetX, y = it.y + offsetY) }
        playerService.updateDiscoveredMap(playerId.toInt(), absolute)
    }
}


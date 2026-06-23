package com.mazechallenge.mapper

import com.mazechallenge.domain.Player
import com.mazechallenge.dto.PlayerDTO

class PlayerMapper {
    fun toDto(player: Player): PlayerDTO {
        return PlayerDTO(
            id = player.id,
            name = player.name,
            position = player.position,
            color = player.color
        )
    }

    fun fromDto(playerDTO: PlayerDTO): Player {
        return Player(
            id = playerDTO.id,
            name = playerDTO.name,
            position = playerDTO.position,
            color = playerDTO.color
        )
    }
}
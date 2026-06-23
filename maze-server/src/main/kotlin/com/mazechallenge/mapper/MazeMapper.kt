package com.mazechallenge.mapper

import com.mazechallenge.domain.Maze
import com.mazechallenge.dto.MazeDto

class MazeMapper {
    fun toDto(maze: Maze): MazeDto {
        return MazeDto(
            width = maze.width,
            height = maze.height,
            layout = maze.layout,
            startPosition = maze.startPosition,
            exitPosition = maze.exitPosition
        )
    }

    fun fromDto(mazeDto: MazeDto): Maze {
        return Maze(
            width = mazeDto.width,
            height = mazeDto.height,
            layout = mazeDto.layout,
            startPosition = mazeDto.startPosition,
            exitPosition = mazeDto.exitPosition
        )
    }
}
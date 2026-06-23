package com.mazechallenge.service

import com.mazechallenge.domain.Maze
import org.springframework.stereotype.Service

@Service
class MazeService {

    fun loadMaze(mazeNumber: Int): Maze? {
        return try {
            val lines = javaClass.getResourceAsStream("/Maze$mazeNumber.txt")
                ?.bufferedReader()
                ?.readLines()
                ?: return null
            buildMaze(lines)
        } catch (e: Exception) {
            null
        }
    }

    fun buildMaze(lines: List<String>): Maze {
        val normalizedLines = lines
        val height = normalizedLines.size
        val width = if (normalizedLines.isNotEmpty()) normalizedLines.maxOf { it.length } else 0
        var startPosition = Pair(0, 0)
        var exitPosition = Pair(0, 0)
        for (y in normalizedLines.indices) {
            for (x in normalizedLines[y].indices) {
                when (normalizedLines[y][x]) {
                    'S' -> startPosition = Pair(x, y)
                    'E' -> exitPosition = Pair(x, y)
                }
            }
        }
        return Maze(width, height, normalizedLines, startPosition, exitPosition)
    }

    fun isValidMove(maze: Maze, x: Int, y: Int): Boolean {
        return maze.isValidMove(x, y)
    }
}
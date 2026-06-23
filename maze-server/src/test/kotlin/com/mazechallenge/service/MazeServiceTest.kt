package com.mazechallenge.service

import com.mazechallenge.domain.Maze
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class MazeServiceTest {

    private val mazeService: MazeService = MazeService()

    @Test
    fun `should load maze from file`() {
        val maze = mazeService.loadMaze(1)
        assertNotNull(maze)
    }

    @Test
    fun `should return null for non-existing maze`() {
        val maze = mazeService.loadMaze(99)
        assertEquals(null, maze)
    }

    @Test
    fun `should find start position in maze`() {
        val maze = mazeService.loadMaze(1)
        assertNotNull(maze?.startPosition)
    }

    @Test
    fun `should find exit position in maze`() {
        val maze = mazeService.loadMaze(1)
        assertNotNull(maze?.exitPosition)
    }
}
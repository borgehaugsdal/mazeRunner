package com.mazechallenge.domain

data class Maze(
    val width: Int,
    val height: Int,
    val layout: List<String>,
    val startPosition: Pair<Int, Int>,
    val exitPosition: Pair<Int, Int>
) {
    fun isValidMove(x: Int, y: Int): Boolean {
        return x in 0 until width && y in 0 until height && layout[y][x] != '#'
    }

    fun getVisibleArea(playerPosition: Pair<Int, Int>, viewDistance: Int): List<String> {
        val (playerX, playerY) = playerPosition
        val visibleArea = mutableListOf<String>()

        for (dy in -viewDistance..viewDistance) {
            val row = StringBuilder()
            for (dx in -viewDistance..viewDistance) {
                val x = playerX + dx
                val y = playerY + dy
                if (isValidMove(x, y) && Math.abs(dx) + Math.abs(dy) <= viewDistance) {
                    row.append(layout[y][x])
                } else {
                    row.append('#') // Treat out of bounds as walls
                }
            }
            visibleArea.add(row.toString())
        }
        return visibleArea
    }
}
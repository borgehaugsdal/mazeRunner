package com.mazechallenge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MazeServerApplication

fun main(args: Array<String>) {
    runApplication<MazeServerApplication>(*args)
}
package com.mazechallenge.integration

import com.mazechallenge.MazeServerApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [MazeServerApplication::class])
class GameIntegrationTest @Autowired constructor(
    private val restTemplate: TestRestTemplate
) {

    @Test
    fun `test game creation`() {
        val response: ResponseEntity<String> = restTemplate.postForEntity("/game", null, String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `test player move returns view and success and escaped status`() {
        val created: ResponseEntity<Map<*, *>> = restTemplate.postForEntity("/game", null, Map::class.java)
        val playerId = created.body?.get("playerId")?.toString() ?: "1"

        val response: ResponseEntity<Map<*, *>> = restTemplate.postForEntity("/game/$playerId/move/NORTH", null, Map::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        assert(response.body?.containsKey("success") == true)
        assert(response.body?.containsKey("escaped") == true)
        assert(response.body?.containsKey("north") == true)
    }
}
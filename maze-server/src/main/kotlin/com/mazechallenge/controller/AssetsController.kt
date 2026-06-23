package com.mazechallenge.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/assets")
class AssetsController {

    @GetMapping("/elevator.png")
    fun elevator(): ResponseEntity<Resource> {
        val res = ClassPathResource("elevator.png")
        val headers = HttpHeaders()
        headers.contentType = MediaType.IMAGE_PNG
        return ResponseEntity.ok().headers(headers).body(res)
    }

    @GetMapping("/TheRunnerLogo.png")
    fun logo(): ResponseEntity<Resource> {
        val res = ClassPathResource("TheRunnerLogo.png")
        val headers = HttpHeaders()
        headers.contentType = MediaType.IMAGE_PNG
        return ResponseEntity.ok().headers(headers).body(res)
    }
}

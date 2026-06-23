package com.mazechallenge.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openApi() = OpenAPI().apply {
        info = Info().apply {
            title = "Maze Challenge API"
            version = "1.0"
            description = "API for the Maze Challenge game"
        }
    }
}
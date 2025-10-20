package com.pswidersk

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    install(CORS) {
        allowHost("localhost")
        allowHost("127.0.0.1")
    }
    routing {
        swaggerUI(path = "openapi")
    }
}

package com.pswidersk

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.csrf.*

fun Application.configureSecurity() {
    val publicHost = System.getenv("PUBLIC_HOSTNAME")

    install(CSRF) {
        allowOrigin("http://localhost")
        allowOrigin("http://127.0.0.1")
        allowOrigin("http://localhost:8080")
        allowOrigin("http://127.0.0.1:8080")
        allowOrigin("http://127.0.0.1:5500")
        publicHost?.let {
            allowOrigin("https://$it")
        }

        originMatchesHost()
    }

    install(CORS) {
        allowHost("localhost", schemes = listOf("http"))
        allowHost("127.0.0.1", schemes = listOf("http"))
        publicHost?.let {
            allowHost(it, schemes = listOf("https"))
        }
        anyMethod()

        allowHeaders { true }

        allowCredentials = true

        maxAgeInSeconds = 24 * 60 * 60
    }
}
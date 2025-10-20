package com.pswidersk

import io.ktor.server.application.*
import io.ktor.server.plugins.csrf.*

fun Application.configureSecurity() {
    install(CSRF) {
        // tests Origin is an expected value
        allowOrigin("http://localhost")
        allowOrigin("http://127.0.0.1")
        allowOrigin("https://printamos-homelab.pswidersk.com")

        // tests Origin matches Host header
        originMatchesHost()
    }
}

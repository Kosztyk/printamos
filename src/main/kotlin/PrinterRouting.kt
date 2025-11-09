package com.pswidersk

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Route.printerRouting() {

    get("/printers") {
        @Serializable
        data class Printer(val name: String, val description: String)

        val command = listOf("lpstat", "-Ev")

        val out = execCommand(command)

        if (out.success) {
            val printerLines = out.output.lines()
            val printers = printerLines
                .filter { it.isNotBlank() }
                .map {
                    val parts = it.split(" ")
                    val name = if (parts.size > 2) {
                        it.split(" ")[2]
                    } else {
                        "NaN"
                    }
                    Printer(name, it)
                }
            call.respond(printers)
        } else {
            if (out.errorMessage.contains("No destinations")) {
                call.respond(HttpStatusCode.OK, emptyList<Printer>())
            }
            call.respond(HttpStatusCode.InternalServerError, out.errorMessage)
        }
    }
}
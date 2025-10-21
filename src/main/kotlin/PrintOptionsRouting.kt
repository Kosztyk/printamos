package com.pswidersk

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.printOptionsRouting() {

    post("/list-options") {
        val parameters = call.receiveParameters()
        val printerName = parameters["printer_name"]

        if (printerName == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing printer_name")
            return@post
        }

        val command = listOf("lpoptions", "-E", "-p", printerName, "-l")
        val out = execCommand(command)

        if (out.success) {
            call.respond(HttpStatusCode.OK, out.output.ifEmpty { "No output" })
        } else {
            call.respond(HttpStatusCode.InternalServerError, out.errorMessage)
        }
    }

}
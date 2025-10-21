package com.pswidersk

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.printerRouting() {
    post("/add-printer") {
        val parameters = call.receiveParameters()
        val printerName = parameters["printer_name"]
        val printerUri = parameters["printer_uri"]
        val printerModel = parameters["printer_model"]

        if (printerName == null || printerUri == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing printer_name or printer_uri")
            return@post
        }

        var command = mutableListOf(
            "lpadmin",
            "-p", printerName,
            "-E",
            "-v", printerUri
        )
        if (printerModel != null && printerModel.isNotBlank()) {
            command += listOf("-m", printerModel)
        }
        val out = execCommand(command)

        if (out.success) {
            call.respond(HttpStatusCode.OK, out.output.ifEmpty { "Command executed successfully" })
        } else {
            call.respond(HttpStatusCode.InternalServerError, out.errorMessage)
        }
    }

    delete("/remove-printer") {
        val parameters = call.receiveParameters()
        val printerName = parameters["printer_name"]

        if (printerName.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Missing printer_name")
            return@delete
        }

        val command = listOf("lpadmin", "-x", printerName)
        val out = execCommand(command)

        if (out.success) {
            call.respond(HttpStatusCode.OK, out.output.ifEmpty { "Printer removed successfully" })
        } else {
            call.respond(HttpStatusCode.InternalServerError, out.errorMessage)
        }
    }

    get("/printers") {
        val command = listOf("lpstat", "-p")

        val out = execCommand(command)

        if (out.success) {
            call.respond(HttpStatusCode.OK, out.output.ifEmpty { "Command executed successfully" })
        } else {
            call.respond(HttpStatusCode.InternalServerError, out.errorMessage)
        }
    }
}
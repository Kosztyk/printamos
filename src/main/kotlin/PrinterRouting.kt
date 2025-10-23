package com.pswidersk

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

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

        val command = mutableListOf(
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
        @Serializable
        data class Printer(val name: String, val description: String)

        val command = listOf("lpstat", "-p")

        val out = execCommand(command)

        if (out.success) {
            val printerLines = out.output.lines()
            val printers = printerLines
                .filter { it.isNotBlank() }
                .map {
                    val parts = it.split(" ")
                    val name = if (parts.size > 2) {
                        it.split(" ")[1]
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
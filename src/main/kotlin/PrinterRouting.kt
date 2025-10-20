package com.pswidersk

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

fun Route.printerRouting() {
    post("/add-printer") {
        // Receive parameters from the request body (JSON or form-data)
        val parameters = call.receiveParameters()
        val printerName = parameters["printer_name"]
        val printerIp = parameters["printer_ip"]

        // Validate inputs
        if (printerName == null || printerIp == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing printer_name or printer_ip")
            return@post
        }

        // Sanitize inputs to prevent injection
        if (!isValidPrinterName(printerName) || !isValidIpAddress(printerIp)) {
            call.respond(HttpStatusCode.BadRequest, "Invalid printer_name or printer_ip")
            return@post
        }

        // Build and execute the lpadmin command securely
        try {
            val command = listOf(
                "lpadmin",
                "-p", printerName,
                "-E",
                "-v", "ipp://$printerIp/ipp/print",
                "-m", "everywhere"
            )
            val process = ProcessBuilder(command).start()
            val output = StringBuilder()
            val error = StringBuilder()

            // Capture standard output
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.lines().forEach { output.append(it).append("\n") }
            }

            // Capture standard error
            BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                reader.lines().forEach { error.append(it).append("\n") }
            }

            val exitCode = process.waitFor()

            if (exitCode == 0) {
                call.respond(HttpStatusCode.OK, output.toString().ifEmpty { "Command executed successfully" })
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    error.toString().ifEmpty { "Command failed with exit code $exitCode" })
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error executing command: ${e.message}")
        }
    }

    get("/printers") {
        try {
            val command = listOf("lpstat", "-p")
            val process = ProcessBuilder(command).start()
            val output = StringBuilder()
            val error = StringBuilder()

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.lines().forEach { output.append(it).append("\n") }
            }

            BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                reader.lines().forEach { error.append(it).append("\n") }
            }

            val exitCode = process.waitFor()

            if (exitCode == 0) {
                call.respond(HttpStatusCode.OK, output.toString().ifEmpty { "No printers found" })
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    error.toString().ifEmpty { "Command failed with exit code $exitCode" })
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error executing lpstat command: ${e.message}")
        }
    }
}

// Validate printer_ip (basic IPv4 or hostname validation)
fun isValidIpAddress(ip: String): Boolean {
    // IPv4 pattern
    val ipv4Pattern = Pattern.compile(
        "^([0-9]{1,3}\\.){3}[0-9]{1,3}$"
    )
    // Basic hostname pattern (allows letters, numbers, dots, and hyphens)
    val hostnamePattern = Pattern.compile(
        "^[a-zA-Z0-9.-]{1,255}$"
    )
    return ipv4Pattern.matcher(ip).matches() || hostnamePattern.matcher(ip).matches()
}
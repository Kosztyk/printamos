package com.pswidersk

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files

fun Route.printJobRouting() {

    post("/print-job") {
        // Receive multipart form data
        val multipart = call.receiveMultipart()
        var printerName: String? = null
        var file: PartData.FileItem? = null

        // Process each part
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    if (part.name == "printer_name") {
                        printerName = part.value
                    }
                }

                is PartData.FileItem -> {
                    if (part.name == "file") {
                        file = part
                    }
                }

                else -> {
                    // Ignore other part types
                }
            }
            part.dispose()
        }

        // Validate inputs
        if (printerName == null || file == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing printer_name or file")
            return@post
        }

        if (!isValidPrinterName(printerName)) {
            call.respond(HttpStatusCode.BadRequest, "Invalid printer_name")
            return@post
        }

        // Validate file type (PDF or JPG)
        val fileName = file.originalFileName?.lowercase() ?: ""
        if (!fileName.endsWith(".pdf") && !fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
            call.respond(HttpStatusCode.BadRequest, "Invalid file type (must be PDF or JPG)")
            return@post
        }

        // Save the file temporarily
        val tempFile = Files.createTempFile("print-", fileName).toFile()
        try {
            file.provider().toInputStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Execute lp command to print the file
            val command = listOf(
                "lp",
                "-d", printerName,
                tempFile.absolutePath
            )
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
                call.respond(HttpStatusCode.OK, output.toString().ifEmpty { "Print job sent successfully" })
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    error.toString().ifEmpty { "Command failed with exit code $exitCode" })
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error processing print job: ${e.message}")
        } finally {
            tempFile.delete()
        }
    }
}
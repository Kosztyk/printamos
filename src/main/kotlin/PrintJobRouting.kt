package com.pswidersk

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.jvm.javaio.*
import java.nio.file.Files

private val validFileSuffixes = setOf(
    ".pdf",
    ".jpg",
    ".jpeg",
    ".png"
)

fun Route.printJobRouting() {

    post("/print-job") {
        val multipart = call.receiveMultipart()
        var printerName: String? = null
        var file: PartData.FileItem? = null
        var fileContent: ByteArray? = null
        var copies = 1
        val options = mutableListOf<String>()

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "printer_name" -> printerName = part.value
                        "copies" -> copies = part.value.toIntOrNull() ?: 1
                        "options" -> {
                            // Assuming options are sent as repeated parts or comma-separated string
                            options.addAll(part.value.splitByCommas())
                        }
                    }
                }

                is PartData.FileItem -> {
                    if (part.name == "file") {
                        file = part
                        fileContent = part.provider().toInputStream().readBytes()
                    }
                }

                else -> {
                    // Ignore other part types
                }
            }
            part.dispose()
        }

        if (printerName == null || file == null || fileContent == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing printer_name or file")
            return@post
        }

        val fileName = file.originalFileName?.lowercase() ?: "uploaded_file"
        val isValidFileType = validFileSuffixes.any {
            fileName.endsWith(it)
        }

        if (!isValidFileType) {
            call.respond(HttpStatusCode.BadRequest, "Invalid file type (must be PDF, JPG or PNG)")
            return@post
        }
        val tempFile = Files.createTempFile("print_", "_$fileName").toFile()
        try {
            tempFile.writeBytes(fileContent)
            val command = mutableListOf(
                "lp",
                "-E",
                "-d", printerName,
            )
            command += listOf("-n", copies.toString())
            options.forEach { command += listOf("-o", it) }
            command += tempFile.absolutePath

            val out = execCommand(command)

            if (out.success) {
                call.respond(HttpStatusCode.OK, out.output.ifEmpty { "Print job sent successfully" })
            } else {
                call.respond(HttpStatusCode.InternalServerError, out.errorMessage)
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error processing print job: ${e.message}")
        } finally {
            tempFile.delete()
        }
    }

    get("/list-jobs") {
        val command = listOf(
            "lpstat",
            "-E",
            "-lW", "all"
        )

        val out = execCommand(command)

        if (out.success) {
            call.respond(HttpStatusCode.OK, out.output.ifEmpty { "No output" })
        } else {
            call.respond(HttpStatusCode.InternalServerError, out.errorMessage)
        }
    }

}
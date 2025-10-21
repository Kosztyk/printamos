package com.pswidersk

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader

data class ExecOut(val output: String, val errorMessage: String, val success: Boolean)

private val log = LoggerFactory.getLogger("CmdLogger")

fun execCommand(command: List<String>): ExecOut {
    try {
        log.info("Running cmd: $command")
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
            return ExecOut(output.toString(), "", true)
        } else {
            val errorMessage = "Error executing command: $command, error output: $error"
            log.error(errorMessage)
            return ExecOut("", errorMessage, false)
        }
    } catch (e: Exception) {
        val errorMessage = "Error executing command: $command, error message: ${e.message}"
        log.error(errorMessage)
        return ExecOut("", errorMessage, false)
    }
}

fun String.splitByCommas(): List<String> {
    return this.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}